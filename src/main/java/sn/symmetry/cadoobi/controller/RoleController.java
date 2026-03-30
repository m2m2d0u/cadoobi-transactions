package sn.symmetry.cadoobi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.symmetry.cadoobi.dto.common.ControllerApiResponse;
import sn.symmetry.cadoobi.dto.CreateRoleRequest;
import sn.symmetry.cadoobi.dto.RoleResponse;
import sn.symmetry.cadoobi.dto.UpdateRoleRequest;
import sn.symmetry.cadoobi.service.RoleService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Roles", description = "Role management endpoints for defining collections of permissions")
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @Operation(
        summary = "List all roles",
        description = "Retrieves roles with pagination. Supports free-text search across name and code, and optional active-only filter."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Roles retrieved successfully",
            content = @Content(schema = @Schema(implementation = RoleResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        )
    })
    public ResponseEntity<ControllerApiResponse<List<RoleResponse>>> getAllRoles(
        @Parameter(description = "Free-text search across name and code")
        @RequestParam(required = false) String search,
        @Parameter(description = "Filter by active status only")
        @RequestParam(required = false, defaultValue = "false") boolean activeOnly,
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<RoleResponse> page;
        if (search != null && !search.isBlank()) {
            page = activeOnly
                ? roleService.searchActiveRoles(search, pageable)
                : roleService.searchRoles(search, pageable);
        } else {
            page = activeOnly
                ? roleService.getActiveRoles(pageable)
                : roleService.getAllRoles(pageable);
        }

        return ResponseEntity.ok(ControllerApiResponse.paged(
            page,
            page.getTotalElements() + " role(s) found"
        ));
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get role by ID",
        description = "Retrieves detailed information about a specific role including its permissions"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Role retrieved successfully",
            content = @Content(schema = @Schema(implementation = RoleResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Role not found",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        )
    })
    public ResponseEntity<ControllerApiResponse<RoleResponse>> getRoleById(
        @Parameter(description = "Unique role identifier (UUID)", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable UUID id
    ) {
        return ResponseEntity.ok(ControllerApiResponse.success(
            roleService.getRoleById(id),
            "Role retrieved successfully"
        ));
    }

    @GetMapping("/code/{code}")
    @Operation(
        summary = "Get role by code",
        description = "Retrieves a role by its unique code including its permissions"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Role retrieved successfully",
            content = @Content(schema = @Schema(implementation = RoleResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Role not found",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        )
    })
    public ResponseEntity<ControllerApiResponse<RoleResponse>> getRoleByCode(
        @Parameter(description = "Role code", required = true, example = "ADMIN")
        @PathVariable String code
    ) {
        return ResponseEntity.ok(ControllerApiResponse.success(
            roleService.getRoleByCode(code),
            "Role retrieved successfully"
        ));
    }

    @PostMapping
    @Operation(
        summary = "Create new role",
        description = "Creates a new role with specified permissions"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Role created successfully",
            content = @Content(schema = @Schema(implementation = RoleResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid role data or permissions",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Role with this code already exists",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        )
    })
    public ResponseEntity<ControllerApiResponse<RoleResponse>> createRole(
        @Valid @RequestBody CreateRoleRequest request
    ) {
        log.info("Creating role: {}", request.getCode());
        RoleResponse role = roleService.createRole(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ControllerApiResponse.created(role, "Role created successfully"));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Update role",
        description = "Updates an existing role's details and permissions"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Role updated successfully",
            content = @Content(schema = @Schema(implementation = RoleResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid role data or permissions",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Role not found",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        )
    })
    public ResponseEntity<ControllerApiResponse<RoleResponse>> updateRole(
        @Parameter(description = "Unique role identifier (UUID)", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable UUID id,
        @Valid @RequestBody UpdateRoleRequest request
    ) {
        log.info("Updating role: {}", id);
        RoleResponse role = roleService.updateRole(id, request);
        return ResponseEntity.ok(ControllerApiResponse.success(role, "Role updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete role",
        description = "Deletes a role from the system (cannot delete system roles)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Role deleted successfully",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Cannot delete system role",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Role not found",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        )
    })
    public ResponseEntity<ControllerApiResponse<Void>> deleteRole(
        @Parameter(description = "Unique role identifier (UUID)", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable UUID id
    ) {
        log.info("Deleting role: {}", id);
        roleService.deleteRole(id);
        return ResponseEntity.ok(ControllerApiResponse.success("Role deleted successfully"));
    }

    @PostMapping("/{id}/permissions")
    @Operation(
        summary = "Add permissions to role",
        description = "Adds additional permissions to an existing role"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Permissions added successfully",
            content = @Content(schema = @Schema(implementation = RoleResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid permission IDs",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Role or permissions not found",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        )
    })
    public ResponseEntity<ControllerApiResponse<RoleResponse>> addPermissionsToRole(
        @Parameter(description = "Unique role identifier (UUID)", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable UUID id,
        @Parameter(description = "List of permission IDs to add", required = true)
        @RequestBody List<UUID> permissionIds
    ) {
        log.info("Adding permissions to role: {}", id);
        RoleResponse role = roleService.addPermissionsToRole(id, permissionIds);
        return ResponseEntity.ok(ControllerApiResponse.success(role, "Permissions added to role successfully"));
    }

    @DeleteMapping("/{id}/permissions")
    @Operation(
        summary = "Remove permissions from role",
        description = "Removes permissions from an existing role (must keep at least one permission)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Permissions removed successfully",
            content = @Content(schema = @Schema(implementation = RoleResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Cannot remove all permissions from role",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Role not found",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        )
    })
    public ResponseEntity<ControllerApiResponse<RoleResponse>> removePermissionsFromRole(
        @Parameter(description = "Unique role identifier (UUID)", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable UUID id,
        @Parameter(description = "List of permission IDs to remove", required = true)
        @RequestBody List<UUID> permissionIds
    ) {
        log.info("Removing permissions from role: {}", id);
        RoleResponse role = roleService.removePermissionsFromRole(id, permissionIds);
        return ResponseEntity.ok(ControllerApiResponse.success(role, "Permissions removed from role successfully"));
    }
}
