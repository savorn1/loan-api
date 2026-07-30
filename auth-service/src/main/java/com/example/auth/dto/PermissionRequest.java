package com.example.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PermissionRequest {

    @NotBlank
    private String module;

    @NotBlank
    private String action;

    private String description;
}
