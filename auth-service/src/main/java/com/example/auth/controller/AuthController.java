package com.example.auth.controller;

import com.example.auth.dto.ApiResponse;
import com.example.auth.dto.AuthResponse;
import com.example.auth.dto.ChangePasswordRequest;
import com.example.auth.dto.LoginRequest;
import com.example.auth.dto.LogoutRequest;
import com.example.auth.dto.RefreshRequest;
import com.example.auth.dto.RegisterRequest;
import com.example.auth.exception.AppException;
import com.example.auth.service.AuthService;
import com.example.auth.service.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registered successfully", authService.register(request)));
    }

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

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(Authentication authentication,
                                                              @Valid @RequestBody ChangePasswordRequest request) {
        if (authentication == null || authentication.getName() == null) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        authService.changePassword(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
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
