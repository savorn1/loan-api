package com.example.auth.service.impl;

import com.example.auth.client.BranchClient;
import com.example.auth.dto.AuthResponse;
import com.example.auth.dto.ChangePasswordRequest;
import com.example.auth.dto.JwtUserClaims;
import com.example.auth.dto.LoginRequest;
import com.example.auth.dto.LogoutRequest;
import com.example.auth.dto.RefreshRequest;
import com.example.auth.dto.RegisterRequest;
import com.example.auth.dto.UpdateProfileRequest;
import com.example.auth.dto.UserResponse;
import com.example.auth.entity.Permission;
import com.example.auth.entity.RefreshToken;
import com.example.auth.entity.SysUser;
import com.example.auth.entity.UserStatus;
import com.example.auth.exception.AppException;
import com.example.auth.redis.UserSessionStore;
import com.example.auth.repository.RefreshTokenRepository;
import com.example.auth.repository.SysUserRepository;
import com.example.auth.service.AuthService;
import com.example.auth.service.JwtService;
import com.example.storage.FileStorageService;
import com.example.storage.StoredFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final SysUserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserSessionStore userSessionStore;
    private final BranchClient branchClient;
    private final FileStorageService fileStorageService;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(HttpStatus.CONFLICT, "Username already taken: " + request.getUsername());
        }

        SysUser user = SysUser.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);
        saveSession(user);

        String token = jwtService.generateToken(user.getUsername(), user.getRole().name(), user.getUuid().toString(),
                user.getBranchId());
        String refreshToken = issueRefreshToken(user);
        return buildAuthResponse(token, refreshToken, user);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        SysUser user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AppException(HttpStatus.FORBIDDEN, "Account is disabled");
        }

        saveSession(user);

        String token = jwtService.generateToken(user.getUsername(), user.getRole().name(), user.getUuid().toString(),
                user.getBranchId());
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

        SysUser user = userRepository.findById(stored.getUserId())
                .orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AppException(HttpStatus.FORBIDDEN, "Account is disabled");
        }

        // Rotate: the presented refresh token is single-use.
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        // Re-save the cached session too: permissions may have changed since the last login.
        saveSession(user);

        String token = jwtService.generateToken(user.getUsername(), user.getRole().name(), user.getUuid().toString(),
                user.getBranchId());
        String newRefreshToken = issueRefreshToken(user);
        return buildAuthResponse(token, newRefreshToken, user);
    }

    @Override
    @Transactional
    public void logout(LogoutRequest request) {
        refreshTokenRepository.findByToken(request.getRefreshToken())
                .ifPresent(stored -> {
                    stored.setRevoked(true);
                    refreshTokenRepository.save(stored);
                    userRepository.findById(stored.getUserId())
                            .ifPresent(user -> userSessionStore.delete(user.getUuid().toString()));
                });
    }

    @Override
    public JwtUserClaims getProfile(String uuid) {
        JwtUserClaims claims = userSessionStore.get(uuid);
        if (claims == null) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Session expired or not found — please log in again");
        }
        return claims;
    }

    @Override
    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        SysUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        // Force re-login everywhere else after a password change.
        refreshTokenRepository.revokeAllForUser(user.getId());
        userSessionStore.delete(user.getUuid().toString());
    }

    @Override
    public UserResponse getMyProfile(String username) {
        return toUserResponse(findByUsername(username));
    }

    @Override
    @Transactional
    public UserResponse updateMyProfile(String username, UpdateProfileRequest request) {
        SysUser user = findByUsername(username);
        user.setEmail(request.getEmail());
        return toUserResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse uploadAvatar(String username, MultipartFile file) {
        SysUser user = findByUsername(username);
        StoredFile stored = fileStorageService.upload(file, "users/" + user.getId() + "/avatar");
        user.setAvatarFileName(file.getOriginalFilename());
        user.setAvatarUrl(stored.url());
        return toUserResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse deleteAvatar(String username) {
        SysUser user = findByUsername(username);
        user.setAvatarFileName(null);
        user.setAvatarUrl(null);
        return toUserResponse(userRepository.save(user));
    }

    private SysUser findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
    }

    private UserResponse toUserResponse(SysUser user) {
        return UserResponse.builder()
                .id(user.getId())
                .uuid(user.getUuid().toString())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .branchId(user.getBranchId())
                .branchName(resolveBranchName(user.getBranchId()))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private void saveSession(SysUser user) {
        List<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getName)
                .distinct()
                .sorted()
                .toList();

        userSessionStore.save(JwtUserClaims.builder()
                .id(user.getId())
                .username(user.getUsername())
                .uuid(user.getUuid().toString())
                .role(user.getRole().name())
                .permissions(permissions)
                .branchId(user.getBranchId())
                .branchName(resolveBranchName(user.getBranchId()))
                .build());
    }

    // Best-effort — soft-fails to null rather than blocking login/session-refresh if
    // branch-service is unreachable or the referenced branch was since deleted.
    private String resolveBranchName(Long branchId) {
        if (branchId == null) {
            return null;
        }
        try {
            return branchClient.getById(branchId).getData().getName();
        } catch (Exception e) {
            log.warn("Could not resolve branch name for branchId={}: {}", branchId, e.getMessage());
            return null;
        }
    }

    private String issueRefreshToken(SysUser user) {
        String token = UUID.randomUUID().toString();
        refreshTokenRepository.save(RefreshToken.builder()
                .token(token)
                .userId(user.getId())
                .expiresAt(LocalDateTime.now().plus(Duration.ofMillis(refreshExpiration)))
                .build());
        return token;
    }

    private AuthResponse buildAuthResponse(String token, String refreshToken, SysUser user) {
        return AuthResponse.builder()
                .accessToken(token)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpiration() / 1000)
                .username(user.getUsername())
                .role(user.getRole().name())
                .branchId(user.getBranchId())
                .branchName(resolveBranchName(user.getBranchId()))
                .build();
    }
}
