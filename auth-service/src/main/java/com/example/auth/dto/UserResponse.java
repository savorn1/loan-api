package com.example.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String uuid;
    private String username;
    private String email;
    private String avatarUrl;
    private String role;
    private String status;
    private Long branchId;
    private String branchName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
