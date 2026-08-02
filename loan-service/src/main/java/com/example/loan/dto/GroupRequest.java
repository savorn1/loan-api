package com.example.loan.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class GroupRequest {

    @NotBlank
    private String name;

    private String code;
    private UUID loanProductId;
    private Long branchId;
    private Long leaderCustomerId;
    private LocalDate formationDate;
}
