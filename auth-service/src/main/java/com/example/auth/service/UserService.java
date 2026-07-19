package com.example.auth.service;

import com.example.auth.dto.CreateUserRequest;
import com.example.auth.dto.PageResponse;
import com.example.auth.dto.UpdateRoleRequest;
import com.example.auth.dto.UpdateStatusRequest;
import com.example.auth.dto.UserFilterRequest;
import com.example.auth.dto.UserResponse;

import java.util.List;

public interface UserService {

    PageResponse<UserResponse> listUsers(UserFilterRequest filter);

    UserResponse getUser(Long id);

    UserResponse createUser(CreateUserRequest request);

    UserResponse updateRole(Long id, UpdateRoleRequest request, String actingUsername);

    UserResponse updateStatus(Long id, UpdateStatusRequest request, String actingUsername);

    void deleteUser(Long id, String actingUsername);

    List<UserResponse> getUsersByIds(List<Long> ids);

    String deleteUsers(List<Long> ids, String actingUsername);

    void forceLogout(Long id);

    void forceLogoutAll();
}
