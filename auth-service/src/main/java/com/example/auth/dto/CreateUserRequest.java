package com.example.auth.dto;

import com.example.auth.entity.Role;
import com.example.auth.entity.UserStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateUserRequest {

    @NotBlank
    private String username;

    @NotBlank
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotNull
    private Role role = Role.USER;

    @NotNull
    private UserStatus status = UserStatus.ACTIVE;
}
