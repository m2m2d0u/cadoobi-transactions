package sn.symmetry.cadoobi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.symmetry.cadoobi.domain.entity.Permission;
import sn.symmetry.cadoobi.domain.entity.Role;
import sn.symmetry.cadoobi.dto.CreateRoleRequest;
import sn.symmetry.cadoobi.dto.PermissionResponse;
import sn.symmetry.cadoobi.dto.RoleResponse;
import sn.symmetry.cadoobi.dto.UpdateRoleRequest;
import sn.symmetry.cadoobi.exception.BusinessException;
import sn.symmetry.cadoobi.exception.DuplicateResourceException;
import sn.symmetry.cadoobi.exception.ResourceNotFoundException;
import sn.symmetry.cadoobi.repository.PermissionRepository;
import sn.symmetry.cadoobi.repository.RoleRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        log.debug("Fetching all roles");
        return roleRepository.findAll().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> getActiveRoles() {
        log.debug("Fetching active roles");
        return roleRepository.findByIsActiveTrue().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<RoleResponse> getAllRoles(Pageable pageable) {
        log.debug("Fetching all roles (paginated)");
        return roleRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<RoleResponse> getActiveRoles(Pageable pageable) {
        log.debug("Fetching active roles (paginated)");
        return roleRepository.findByIsActiveTrue(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<RoleResponse> searchRoles(String search, Pageable pageable) {
        log.debug("Searching roles with query: {}", search);
        return roleRepository.searchRoles(search.trim(), pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<RoleResponse> searchActiveRoles(String search, Pageable pageable) {
        log.debug("Searching active roles with query: {}", search);
        return roleRepository.searchActiveRoles(search.trim(), pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public RoleResponse getRoleById(UUID id) {
        log.debug("Fetching role by id: {}", id);
        Role role = roleRepository.findByIdWithPermissions(id)
            .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
        return toResponseWithPermissions(role);
    }

    @Transactional(readOnly = true)
    public RoleResponse getRoleByCode(String code) {
        log.debug("Fetching role by code: {}", code);
        Role role = roleRepository.findByCodeWithPermissions(code)
            .orElseThrow(() -> new ResourceNotFoundException("Role not found with code: " + code));
        return toResponseWithPermissions(role);
    }

    @Transactional
    public RoleResponse createRole(CreateRoleRequest request) {
        log.info("Creating role: {}", request.getCode());

        if (roleRepository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException("Role already exists with code: " + request.getCode());
        }

        // Fetch permissions
        Set<Permission> permissions = fetchPermissions(request.getPermissionIds());

        Role role = Role.builder()
            .code(request.getCode())
            .name(request.getName())
            .description(request.getDescription())
            .isActive(request.getIsActive())
            .isSystemRole(false) // User-created roles are never system roles
            .permissions(permissions)
            .build();

        role = roleRepository.save(role);
        log.info("Role created successfully: {}", role.getId());

        return toResponseWithPermissions(role);
    }

    @Transactional
    public RoleResponse updateRole(UUID id, UpdateRoleRequest request) {
        log.info("Updating role: {}", id);

        Role role = roleRepository.findByIdWithPermissions(id)
            .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));

        // Fetch permissions
        Set<Permission> permissions = fetchPermissions(request.getPermissionIds());

        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setPermissions(permissions);

        if (request.getIsActive() != null) {
            role.setIsActive(request.getIsActive());
        }

        role = roleRepository.save(role);
        log.info("Role updated successfully: {}", role.getId());

        return toResponseWithPermissions(role);
    }

    @Transactional
    public void deleteRole(UUID id) {
        log.info("Deleting role: {}", id);

        Role role = roleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));

        if (role.getIsSystemRole()) {
            throw new BusinessException("Cannot delete system role: " + role.getCode());
        }

        roleRepository.deleteById(id);
        log.info("Role deleted successfully: {}", id);
    }

    @Transactional
    public RoleResponse addPermissionsToRole(UUID roleId, List<UUID> permissionIds) {
        log.info("Adding permissions to role: {}", roleId);

        Role role = roleRepository.findByIdWithPermissions(roleId)
            .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));

        Set<Permission> newPermissions = fetchPermissions(permissionIds);
        role.getPermissions().addAll(newPermissions);

        role = roleRepository.save(role);
        log.info("Permissions added to role successfully: {}", roleId);

        return toResponseWithPermissions(role);
    }

    @Transactional
    public RoleResponse removePermissionsFromRole(UUID roleId, List<UUID> permissionIds) {
        log.info("Removing permissions from role: {}", roleId);

        Role role = roleRepository.findByIdWithPermissions(roleId)
            .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));

        role.getPermissions().removeIf(permission -> permissionIds.contains(permission.getId()));

        if (role.getPermissions().isEmpty()) {
            throw new BusinessException("Role must have at least one permission");
        }

        role = roleRepository.save(role);
        log.info("Permissions removed from role successfully: {}", roleId);

        return toResponseWithPermissions(role);
    }

    private Set<Permission> fetchPermissions(List<UUID> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            throw new BusinessException("At least one permission is required");
        }

        List<Permission> permissions = permissionRepository.findAllById(permissionIds);

        if (permissions.size() != permissionIds.size()) {
            throw new ResourceNotFoundException("One or more permissions not found");
        }

        return new HashSet<>(permissions);
    }

    private RoleResponse toResponse(Role role) {
        List<PermissionResponse> permissions = role.getPermissions().stream()
                .map(this::toPermissionResponse)
                .toList();
        return RoleResponse.builder()
            .id(role.getId())
            .code(role.getCode())
            .name(role.getName())
            .description(role.getDescription())
            .permissions(permissions)
            .isActive(role.getIsActive())
            .isSystemRole(role.getIsSystemRole())
            .createdAt(role.getCreatedAt())
            .updatedAt(role.getUpdatedAt())
            .build();
    }

    private RoleResponse toResponseWithPermissions(Role role) {
        List<PermissionResponse> permissions = role.getPermissions().stream()
            .map(this::toPermissionResponse)
            .collect(Collectors.toList());

        return RoleResponse.builder()
            .id(role.getId())
            .code(role.getCode())
            .name(role.getName())
            .description(role.getDescription())
            .isActive(role.getIsActive())
            .isSystemRole(role.getIsSystemRole())
            .permissions(permissions)
            .createdAt(role.getCreatedAt())
            .updatedAt(role.getUpdatedAt())
            .build();
    }

    private PermissionResponse toPermissionResponse(Permission permission) {
        return PermissionResponse.builder()
            .id(permission.getId())
            .code(permission.getCode())
            .name(permission.getName())
            .description(permission.getDescription())
            .resource(permission.getResource())
            .action(permission.getAction())
            .isActive(permission.getIsActive())
            .createdAt(permission.getCreatedAt())
            .updatedAt(permission.getUpdatedAt())
            .build();
    }
}
