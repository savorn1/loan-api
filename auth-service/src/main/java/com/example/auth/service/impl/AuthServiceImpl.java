package com.example.auth.service.impl;

import com.example.auth.dto.AuthResponse;
import com.example.auth.dto.ChangePasswordRequest;
import com.example.auth.dto.LoginRequest;
import com.example.auth.dto.LogoutRequest;
import com.example.auth.dto.RefreshRequest;
import com.example.auth.dto.RegisterRequest;
import com.example.auth.entity.RefreshToken;
import com.example.auth.entity.User;
import com.example.auth.exception.AppException;
import com.example.auth.repository.RefreshTokenRepository;
import com.example.auth.repository.UserRepository;
import com.example.auth.service.AuthService;
import com.example.auth.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(HttpStatus.CONFLICT, "Username already taken: " + request.getUsername());
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(user.getUsername(), user.getRole().name());
        String refreshToken = issueRefreshToken(user);
        return buildAuthResponse(token, refreshToken, user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        if (!user.isActive()) {
            throw new AppException(HttpStatus.FORBIDDEN, "Account is disabled");
        }

        String token = jwtService.generateToken(user.getUsername(), user.getRole().name());
        String refreshToken = issueRefreshToken(user);
        return buildAuthResponse(token, refreshToken, user);
    }

    @Override
    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        RefreshToken stored = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        if (stored.isRevoked() || stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Refresh token expired or revoked");
        }

        User user = userRepository.findById(stored.getUserId())
                .orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        if (!user.isActive()) {
            throw new AppException(HttpStatus.FORBIDDEN, "Account is disabled");
        }

        // Rotate: the presented refresh token is single-use.
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        String token = jwtService.generateToken(user.getUsername(), user.getRole().name());
        String newRefreshToken = issueRefreshToken(user);
        return buildAuthResponse(token, newRefreshToken, user);
    }

    @Override
    public void logout(LogoutRequest request) {
        refreshTokenRepository.findByToken(request.getRefreshToken())
                .ifPresent(stored -> {
                    stored.setRevoked(true);
                    refreshTokenRepository.save(stored);
                });
    }

    @Override
    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        // Force re-login everywhere else after a password change.
        refreshTokenRepository.revokeAllForUser(user.getId());
    }

    private String issueRefreshToken(User user) {
        String token = UUID.randomUUID().toString();
        refreshTokenRepository.save(RefreshToken.builder()
                .token(token)
                .userId(user.getId())
                .expiresAt(LocalDateTime.now().plus(Duration.ofMillis(refreshExpiration)))
                .build());
        return token;
    }

    private AuthResponse buildAuthResponse(String token, String refreshToken, User user) {
        return AuthResponse.builder()
                .accessToken(token)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpiration() / 1000)
                .username(user.getUsername())
                .role(user.getRole().name())
                .build();
    }
}
