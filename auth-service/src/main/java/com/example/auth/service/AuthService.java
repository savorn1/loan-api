package com.example.auth.service;

import com.example.auth.dto.AuthResponse;
import com.example.auth.dto.ChangePasswordRequest;
import com.example.auth.dto.LoginRequest;
import com.example.auth.dto.LogoutRequest;
import com.example.auth.dto.RefreshRequest;
import com.example.auth.dto.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(RefreshRequest request);

    void logout(LogoutRequest request);

    void changePassword(String username, ChangePasswordRequest request);
}
