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
import sn.symmetry.cadoobi.dto.CreatePermissionRequest;
import sn.symmetry.cadoobi.dto.PermissionResponse;
import sn.symmetry.cadoobi.service.PermissionService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Permissions", description = "Permission management endpoints for defining system capabilities")
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping
    @Operation(
        summary = "List all permissions",
        description = "Retrieves permissions with pagination. Supports free-text search across code, name and resource, and optional active-only filter."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Permissions retrieved successfully",
            content = @Content(schema = @Schema(implementation = PermissionResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        )
    })
    public ResponseEntity<ControllerApiResponse<List<PermissionResponse>>> getAllPermissions(
        @Parameter(description = "Free-text search across code, name and resource")
        @RequestParam(required = false) String search,
        @Parameter(description = "Filter by active status only")
        @RequestParam(required = false, defaultValue = "false") boolean activeOnly,
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<PermissionResponse> page;
        if (search != null && !search.isBlank()) {
            page = activeOnly
                ? permissionService.searchActivePermissions(search, pageable)
                : permissionService.searchPermissions(search, pageable);
        } else {
            page = activeOnly
                ? permissionService.getActivePermissions(pageable)
                : permissionService.getAllPermissions(pageable);
        }

        return ResponseEntity.ok(ControllerApiResponse.paged(
            page,
            page.getTotalElements() + " permission(s) found"
        ));
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get permission by ID",
        description = "Retrieves detailed information about a specific permission"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Permission retrieved successfully",
            content = @Content(schema = @Schema(implementation = PermissionResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Permission not found",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        )
    })
    public ResponseEntity<ControllerApiResponse<PermissionResponse>> getPermissionById(
        @Parameter(description = "Unique permission identifier (UUID)", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable UUID id
    ) {
        return ResponseEntity.ok(ControllerApiResponse.success(
            permissionService.getPermissionById(id),
            "Permission retrieved successfully"
        ));
    }

    @GetMapping("/code/{code}")
    @Operation(
        summary = "Get permission by code",
        description = "Retrieves a permission by its unique code"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Permission retrieved successfully",
            content = @Content(schema = @Schema(implementation = PermissionResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Permission not found",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        )
    })
    public ResponseEntity<ControllerApiResponse<PermissionResponse>> getPermissionByCode(
        @Parameter(description = "Permission code", required = true, example = "payment:create")
        @PathVariable String code
    ) {
        return ResponseEntity.ok(ControllerApiResponse.success(
            permissionService.getPermissionByCode(code),
            "Permission retrieved successfully"
        ));
    }

    @GetMapping("/resource/{resource}")
    @Operation(
        summary = "Get permissions by resource",
        description = "Retrieves permissions for a specific resource category with pagination"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Permissions retrieved successfully",
            content = @Content(schema = @Schema(implementation = PermissionResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        )
    })
    public ResponseEntity<ControllerApiResponse<List<PermissionResponse>>> getPermissionsByResource(
        @Parameter(description = "Resource category", required = true, example = "payment")
        @PathVariable String resource,
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<PermissionResponse> page = permissionService.getPermissionsByResource(resource, pageable);
        return ResponseEntity.ok(ControllerApiResponse.paged(
            page,
            page.getTotalElements() + " permission(s) found for resource: " + resource
        ));
    }

    @PostMapping
    @Operation(
        summary = "Create new permission",
        description = "Creates a new permission in the system"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Permission created successfully",
            content = @Content(schema = @Schema(implementation = PermissionResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid permission data",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Permission with this code already exists",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        )
    })
    public ResponseEntity<ControllerApiResponse<PermissionResponse>> createPermission(
        @Valid @RequestBody CreatePermissionRequest request
    ) {
        log.info("Creating permission: {}", request.getCode());
        PermissionResponse permission = permissionService.createPermission(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ControllerApiResponse.created(permission, "Permission created successfully"));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Update permission",
        description = "Updates an existing permission"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Permission updated successfully",
            content = @Content(schema = @Schema(implementation = PermissionResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid permission data",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Permission not found",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Permission with this code already exists",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        )
    })
    public ResponseEntity<ControllerApiResponse<PermissionResponse>> updatePermission(
        @Parameter(description = "Unique permission identifier (UUID)", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable UUID id,
        @Valid @RequestBody CreatePermissionRequest request
    ) {
        log.info("Updating permission: {}", id);
        PermissionResponse permission = permissionService.updatePermission(id, request);
        return ResponseEntity.ok(ControllerApiResponse.success(permission, "Permission updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete permission",
        description = "Deletes a permission from the system"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Permission deleted successfully",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Permission not found",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        )
    })
    public ResponseEntity<ControllerApiResponse<Void>> deletePermission(
        @Parameter(description = "Unique permission identifier (UUID)", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable UUID id
    ) {
        log.info("Deleting permission: {}", id);
        permissionService.deletePermission(id);
        return ResponseEntity.ok(ControllerApiResponse.success("Permission deleted successfully"));
    }
}
