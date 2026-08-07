package com.example.auth.service;

import com.example.auth.dto.AuthResponse;
import com.example.auth.dto.ChangePasswordRequest;
import com.example.auth.dto.JwtUserClaims;
import com.example.auth.dto.LoginRequest;
import com.example.auth.dto.LogoutRequest;
import com.example.auth.dto.RefreshRequest;
import com.example.auth.dto.RegisterRequest;
import com.example.auth.dto.UpdateProfileRequest;
import com.example.auth.dto.UserResponse;
import org.springframework.web.multipart.MultipartFile;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(RefreshRequest request);

    void logout(LogoutRequest request);

    JwtUserClaims getProfile(String uuid);

    void changePassword(String username, ChangePasswordRequest request);

    UserResponse getMyProfile(String username);

    UserResponse updateMyProfile(String username, UpdateProfileRequest request);

    UserResponse uploadAvatar(String username, MultipartFile file);

    UserResponse deleteAvatar(String username);
}
