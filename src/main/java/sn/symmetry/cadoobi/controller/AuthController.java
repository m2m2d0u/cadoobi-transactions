package sn.symmetry.cadoobi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sn.symmetry.cadoobi.config.JwtProperties;
import sn.symmetry.cadoobi.domain.entity.Permission;
import sn.symmetry.cadoobi.domain.entity.Role;
import sn.symmetry.cadoobi.domain.entity.User;
import sn.symmetry.cadoobi.dto.AuthResponse;
import sn.symmetry.cadoobi.dto.LoginRequest;
import sn.symmetry.cadoobi.dto.RefreshTokenRequest;
import sn.symmetry.cadoobi.dto.common.ControllerApiResponse;
import sn.symmetry.cadoobi.repository.UserRepository;
import sn.symmetry.cadoobi.security.CustomUserDetails;
import sn.symmetry.cadoobi.security.JwtService;

import java.util.List;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Login and token management")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final UserRepository userRepository;

    @PostMapping("/login")
    @Operation(
            summary = "Login",
            description = "Authenticate with email and password. Returns a JWT token containing roles and permissions."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials",
                    content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
            )
    })
    public ResponseEntity<ControllerApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        String token = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        List<String> roles = user.getRoles().stream()
                .map(Role::getCode)
                .toList();

        List<String> permissions = user.getRoles().stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(Permission::getCode)
                .distinct()
                .toList();

        AuthResponse authResponse = AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .expiresIn(jwtProperties.getExpirationMs())
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .status(user.getStatus())
                .roles(roles)
                .permissions(permissions)
                .build();

        log.info("User logged in: {}", user.getEmail());
        return ResponseEntity.ok(ControllerApiResponse.success(authResponse, "Login successful"));
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "Refresh Token",
            description = "Get a new JWT token using a valid refresh token."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token refreshed successfully",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid or expired refresh token",
                    content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
            )
    })
    public ResponseEntity<ControllerApiResponse<AuthResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        String refreshToken = request.getRefreshToken();

        try {
            // Check if token is expired first
            if (jwtService.isTokenExpired(refreshToken)) {
                return ResponseEntity.status(401)
                        .body(ControllerApiResponse.error("Refresh token has expired", 401, "TOKEN_EXPIRED"));
            }

            String email = jwtService.extractEmail(refreshToken);
            User user = userRepository.findByEmailWithRolesAndPermissions(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Verify the token is mathematically valid for this user
            if (!jwtService.isTokenValid(refreshToken, user.getEmail())) {
                return ResponseEntity.status(401)
                        .body(ControllerApiResponse.error("Invalid refresh token", 401, "INVALID_TOKEN"));
            }

            String newToken = jwtService.generateToken(user);
            String newRefreshToken = jwtService.generateRefreshToken(user);

            List<String> roles = user.getRoles().stream()
                    .map(Role::getCode)
                    .toList();

            List<String> permissions = user.getRoles().stream()
                    .flatMap(r -> r.getPermissions().stream())
                    .map(Permission::getCode)
                    .distinct()
                    .toList();

            AuthResponse authResponse = AuthResponse.builder()
                    .token(newToken)
                    .refreshToken(newRefreshToken)
                    .expiresIn(jwtProperties.getExpirationMs())
                    .userId(user.getId())
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .status(user.getStatus())
                    .roles(roles)
                    .permissions(permissions)
                    .build();

            log.info("Token refreshed for user: {}", user.getEmail());
            return ResponseEntity.ok(ControllerApiResponse.success(authResponse, "Token refreshed successfully"));

        } catch (Exception e) {
            log.warn("Token refresh failed: {}", e.getMessage());
            return ResponseEntity.status(401)
                    .body(ControllerApiResponse.error("Invalid refresh token", 401,  "INVALID_TOKEN"));
        }
    }

    @PostMapping("/logout")
    @Operation(
            summary = "Logout",
            description = "Logout the current user (client-side token deletion is expected)"
    )
    @ApiResponse(responseCode = "200", description = "Logout successful")
    public ResponseEntity<ControllerApiResponse<Void>> logout() {
        return ResponseEntity.ok(ControllerApiResponse.success(null, "Logout successful"));
    }
}
