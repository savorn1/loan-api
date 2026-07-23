package com.example.loanproduct.dto;

import com.example.loanproduct.entity.InterestCalculationMethod;
import com.example.loanproduct.entity.InterestSchemeStatus;
import com.example.loanproduct.entity.InterestType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class InterestSchemeResponse {

    private UUID id;
    private String code;
    private String name;
    private InterestType interestType;
    private InterestCalculationMethod calculationMethod;
    private InterestSchemeStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
