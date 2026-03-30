package sn.symmetry.cadoobi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.symmetry.cadoobi.domain.entity.Permission;
import sn.symmetry.cadoobi.dto.CreatePermissionRequest;
import sn.symmetry.cadoobi.dto.PermissionResponse;
import sn.symmetry.cadoobi.exception.DuplicateResourceException;
import sn.symmetry.cadoobi.exception.ResourceNotFoundException;
import sn.symmetry.cadoobi.repository.PermissionRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionService {

    private final PermissionRepository permissionRepository;

    @Transactional(readOnly = true)
    public List<PermissionResponse> getAllPermissions() {
        log.debug("Fetching all permissions");
        return permissionRepository.findAll().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PermissionResponse> getActivePermissions() {
        log.debug("Fetching active permissions");
        return permissionRepository.findByIsActiveTrue().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<PermissionResponse> getAllPermissions(Pageable pageable) {
        log.debug("Fetching all permissions (paginated)");
        return permissionRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<PermissionResponse> getActivePermissions(Pageable pageable) {
        log.debug("Fetching active permissions (paginated)");
        return permissionRepository.findByIsActiveTrue(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<PermissionResponse> searchPermissions(String search, Pageable pageable) {
        log.debug("Searching permissions with query: {}", search);
        return permissionRepository.searchPermissions(search.trim(), pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<PermissionResponse> searchActivePermissions(String search, Pageable pageable) {
        log.debug("Searching active permissions with query: {}", search);
        return permissionRepository.searchActivePermissions(search.trim(), pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public PermissionResponse getPermissionById(UUID id) {
        log.debug("Fetching permission by id: {}", id);
        Permission permission = permissionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Permission not found with id: " + id));
        return toResponse(permission);
    }

    @Transactional(readOnly = true)
    public PermissionResponse getPermissionByCode(String code) {
        log.debug("Fetching permission by code: {}", code);
        Permission permission = permissionRepository.findByCode(code)
            .orElseThrow(() -> new ResourceNotFoundException("Permission not found with code: " + code));
        return toResponse(permission);
    }

    @Transactional(readOnly = true)
    public List<PermissionResponse> getPermissionsByResource(String resource) {
        log.debug("Fetching permissions by resource: {}", resource);
        return permissionRepository.findByResource(resource).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<PermissionResponse> getPermissionsByResource(String resource, Pageable pageable) {
        log.debug("Fetching permissions by resource (paginated): {}", resource);
        return permissionRepository.findByResource(resource, pageable).map(this::toResponse);
    }

    @Transactional
    public PermissionResponse createPermission(CreatePermissionRequest request) {
        log.info("Creating permission: {}", request.getCode());

        if (permissionRepository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException("Permission already exists with code: " + request.getCode());
        }

        Permission permission = Permission.builder()
            .code(request.getCode())
            .name(request.getName())
            .description(request.getDescription())
            .resource(request.getResource())
            .action(request.getAction())
            .isActive(request.getIsActive())
            .build();

        permission = permissionRepository.save(permission);
        log.info("Permission created successfully: {}", permission.getId());

        return toResponse(permission);
    }

    @Transactional
    public PermissionResponse updatePermission(UUID id, CreatePermissionRequest request) {
        log.info("Updating permission: {}", id);

        Permission permission = permissionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Permission not found with id: " + id));

        // Check if code is being changed and if new code already exists
        if (!permission.getCode().equals(request.getCode()) &&
            permissionRepository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException("Permission already exists with code: " + request.getCode());
        }

        permission.setCode(request.getCode());
        permission.setName(request.getName());
        permission.setDescription(request.getDescription());
        permission.setResource(request.getResource());
        permission.setAction(request.getAction());
        permission.setIsActive(request.getIsActive());

        permission = permissionRepository.save(permission);
        log.info("Permission updated successfully: {}", permission.getId());

        return toResponse(permission);
    }

    @Transactional
    public void deletePermission(UUID id) {
        log.info("Deleting permission: {}", id);

        if (!permissionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Permission not found with id: " + id);
        }

        permissionRepository.deleteById(id);
        log.info("Permission deleted successfully: {}", id);
    }

    private PermissionResponse toResponse(Permission permission) {
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
