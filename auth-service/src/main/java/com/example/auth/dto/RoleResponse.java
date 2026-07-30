package com.example.auth.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RoleResponse {

    private Long id;
    private String name;
    private String code;
    private boolean isDefault;
    private String description;
    private long permissionCount;
    private long userCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
