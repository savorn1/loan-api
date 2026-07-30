package com.example.auth.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PermissionResponse {

    private Long id;
    private String name;
    private String module;
    private String action;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
