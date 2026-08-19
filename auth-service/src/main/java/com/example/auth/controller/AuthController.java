package com.example.auth.controller;

import com.example.auth.annotation.RateLimit;
import com.example.auth.dto.ApiResponse;
import com.example.auth.dto.AuthResponse;
import com.example.auth.dto.ChangePasswordRequest;
import com.example.auth.dto.JwtUserClaims;
import com.example.auth.dto.LoginRequest;
import com.example.auth.dto.LogoutRequest;
import com.example.auth.dto.RefreshRequest;
import com.example.auth.dto.RegisterRequest;
import com.example.auth.dto.UpdateProfileRequest;
import com.example.auth.dto.UserResponse;
import com.example.auth.exception.AppException;
import com.example.auth.service.AuthService;
import com.example.auth.service.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    // Not public — see AuthenticationFilter.PUBLIC_PATHS in api-gateway, which no longer
    // lets anonymous requests reach this at all. This check is defense-in-depth for a
    // request that reaches auth-service directly, bypassing the gateway.
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registered successfully", authService.register(request)));
    }

    // 5 attempts per minute per IP — previously unthrottled, so scripted password
    // guessing against a known username had no cost at all.
    @RateLimit(max = 5, window = 60)
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.login(request)));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.refresh(request)));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request);
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<JwtUserClaims>> profile(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Missing token");
        }
        String token = authHeader.substring(7);
        if (!jwtService.isValid(token)) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }
        return ResponseEntity.ok(ApiResponse.success(authService.getProfile(jwtService.extractUuid(token))));
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(Authentication authentication,
                                                              @Valid @RequestBody ChangePasswordRequest request) {
        requireAuthentication(authentication);
        authService.changePassword(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> me(Authentication authentication) {
        requireAuthentication(authentication);
        return ResponseEntity.ok(ApiResponse.success(authService.getMyProfile(authentication.getName())));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateMe(Authentication authentication,
                                                                @Valid @RequestBody UpdateProfileRequest request) {
        requireAuthentication(authentication);
        return ResponseEntity.ok(
                ApiResponse.success("Profile updated", authService.updateMyProfile(authentication.getName(), request)));
    }

    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserResponse>> uploadAvatar(Authentication authentication,
                                                                    @RequestParam("file") MultipartFile file) {
        requireAuthentication(authentication);
        return ResponseEntity.ok(
                ApiResponse.success("Avatar uploaded", authService.uploadAvatar(authentication.getName(), file)));
    }

    @DeleteMapping("/me/avatar")
    public ResponseEntity<ApiResponse<UserResponse>> deleteAvatar(Authentication authentication) {
        requireAuthentication(authentication);
        return ResponseEntity.ok(
                ApiResponse.success("Avatar removed", authService.deleteAvatar(authentication.getName())));
    }

    private void requireAuthentication(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validate(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("valid", false, "message", "Missing token"));
        }
        String token = authHeader.substring(7);
        boolean valid = jwtService.isValid(token);
        if (!valid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("valid", false, "message", "Invalid or expired token"));
        }
        return ResponseEntity.ok(Map.of(
                "valid", true,
                "username", jwtService.extractUsername(token)
        ));
    }
}
