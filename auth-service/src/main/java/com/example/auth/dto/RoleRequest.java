package com.example.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RoleRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String code;

    private boolean isDefault;

    private String description;
}
