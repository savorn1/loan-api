package com.example.auth.dto;

import com.example.auth.entity.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateRoleRequest {

    @NotNull
    private Role role;
}
