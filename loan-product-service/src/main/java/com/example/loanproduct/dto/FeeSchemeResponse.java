package com.example.loanproduct.dto;

import com.example.loanproduct.entity.FeeSchemeStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class FeeSchemeResponse {

    private UUID id;
    private String code;
    private String name;
    private FeeSchemeStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
