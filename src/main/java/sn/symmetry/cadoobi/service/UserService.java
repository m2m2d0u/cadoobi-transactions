package sn.symmetry.cadoobi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.symmetry.cadoobi.domain.entity.Role;
import sn.symmetry.cadoobi.domain.entity.User;
import sn.symmetry.cadoobi.domain.enums.UserStatus;
import sn.symmetry.cadoobi.dto.*;
import sn.symmetry.cadoobi.exception.BusinessException;
import sn.symmetry.cadoobi.exception.DuplicateResourceException;
import sn.symmetry.cadoobi.exception.ResourceNotFoundException;
import sn.symmetry.cadoobi.repository.RoleRepository;
import sn.symmetry.cadoobi.repository.UserRepository;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        log.debug("Fetching all users");
        return userRepository.findAll().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByStatus(UserStatus status) {
        log.debug("Fetching users by status: {}", status);
        return userRepository.findByStatus(status).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        log.debug("Fetching user by id: {}", id);
        User user = userRepository.findByIdWithRoles(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return toResponseWithRoles(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        log.debug("Fetching user by email: {}", email);
        User user = userRepository.findByEmailWithRoles(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return toResponseWithRoles(user);
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        log.info("Creating user: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User already exists with email: " + request.getEmail());
        }

        // Fetch roles
        Set<Role> roles = fetchRoles(request.getRoleIds());

        // Hash password (NOTE: In production, use BCryptPasswordEncoder from Spring Security)
        String passwordHash = hashPassword(request.getPassword());

        User user = User.builder()
            .email(request.getEmail())
            .passwordHash(passwordHash)
            .fullName(request.getFullName())
            .phone(request.getPhone())
            .status(UserStatus.PENDING)
            .emailVerified(false)
            .failedLoginAttempts(0)
            .roles(roles)
            .build();

        user = userRepository.save(user);
        log.info("User created successfully: {}", user.getId());

        // TODO: Send welcome email if requested
        if (request.getSendWelcomeEmail()) {
            log.info("Welcome email would be sent to: {}", user.getEmail());
        }

        return toResponseWithRoles(user);
    }

    @Transactional
    public UserResponse updateUser(UUID id, UpdateUserRequest request) {
        log.info("Updating user: {}", id);

        User user = userRepository.findByIdWithRoles(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Check if email is being changed and if new email already exists
        if (!user.getEmail().equals(request.getEmail()) &&
            userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User already exists with email: " + request.getEmail());
        }

        // Fetch roles
        Set<Role> roles = fetchRoles(request.getRoleIds());

        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setRoles(roles);

        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }

        user = userRepository.save(user);
        log.info("User updated successfully: {}", user.getId());

        return toResponseWithRoles(user);
    }

    @Transactional
    public void deleteUser(UUID id) {
        log.info("Deleting user: {}", id);

        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }

        userRepository.deleteById(id);
        log.info("User deleted successfully: {}", id);
    }

    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        log.info("Changing password for user: {}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Verify current password
        if (!verifyPassword(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BusinessException("Current password is incorrect");
        }

        // Verify new password confirmation
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("New password and confirmation do not match");
        }

        // Hash and update password
        String newPasswordHash = hashPassword(request.getNewPassword());
        user.setPasswordHash(newPasswordHash);
        user.setPasswordChangedAt(Instant.now());
        user.setFailedLoginAttempts(0);

        userRepository.save(user);
        log.info("Password changed successfully for user: {}", userId);
    }

    @Transactional
    public UserResponse activateUser(UUID userId) {
        log.info("Activating user: {}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerified(true);

        user = userRepository.save(user);
        log.info("User activated successfully: {}", userId);

        return toResponse(user);
    }

    @Transactional
    public UserResponse suspendUser(UUID userId) {
        log.info("Suspending user: {}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setStatus(UserStatus.SUSPENDED);

        user = userRepository.save(user);
        log.info("User suspended successfully: {}", userId);

        return toResponse(user);
    }

    @Transactional
    public void resetFailedLoginAttempts(UUID userId) {
        log.info("Resetting failed login attempts for user: {}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setFailedLoginAttempts(0);
        if (user.getStatus() == UserStatus.LOCKED) {
            user.setStatus(UserStatus.ACTIVE);
        }

        userRepository.save(user);
        log.info("Failed login attempts reset for user: {}", userId);
    }

    private Set<Role> fetchRoles(List<UUID> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            throw new BusinessException("At least one role is required");
        }

        List<Role> roles = roleRepository.findAllById(roleIds);

        if (roles.size() != roleIds.size()) {
            throw new ResourceNotFoundException("One or more roles not found");
        }

        return new HashSet<>(roles);
    }

    /**
     * Simple password hashing (NOTE: In production, use BCryptPasswordEncoder)
     * This is a placeholder - replace with proper password encoder
     */
    private String hashPassword(String password) {
        // TODO: Replace with BCryptPasswordEncoder.encode(password)
        return "HASHED_" + password;
    }

    /**
     * Simple password verification (NOTE: In production, use BCryptPasswordEncoder)
     * This is a placeholder - replace with proper password verification
     */
    private boolean verifyPassword(String rawPassword, String hashedPassword) {
        // TODO: Replace with BCryptPasswordEncoder.matches(rawPassword, hashedPassword)
        return hashedPassword.equals("HASHED_" + rawPassword);
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
            .id(user.getId())
            .email(user.getEmail())
            .fullName(user.getFullName())
            .phone(user.getPhone())
            .status(user.getStatus())
            .emailVerified(user.getEmailVerified())
            .failedLoginAttempts(user.getFailedLoginAttempts())
            .lastLoginAt(user.getLastLoginAt())
            .passwordChangedAt(user.getPasswordChangedAt())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .build();
    }

    private UserResponse toResponseWithRoles(User user) {
        List<RoleResponse> roles = user.getRoles().stream()
            .map(this::toRoleResponse)
            .collect(Collectors.toList());

        return UserResponse.builder()
            .id(user.getId())
            .email(user.getEmail())
            .fullName(user.getFullName())
            .phone(user.getPhone())
            .status(user.getStatus())
            .emailVerified(user.getEmailVerified())
            .failedLoginAttempts(user.getFailedLoginAttempts())
            .lastLoginAt(user.getLastLoginAt())
            .passwordChangedAt(user.getPasswordChangedAt())
            .roles(roles)
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .build();
    }

    private RoleResponse toRoleResponse(Role role) {
        return RoleResponse.builder()
            .id(role.getId())
            .code(role.getCode())
            .name(role.getName())
            .description(role.getDescription())
            .isActive(role.getIsActive())
            .isSystemRole(role.getIsSystemRole())
            .createdAt(role.getCreatedAt())
            .updatedAt(role.getUpdatedAt())
            .build();
    }
}
