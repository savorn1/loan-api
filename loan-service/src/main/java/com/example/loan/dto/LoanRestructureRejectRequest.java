package com.example.loan.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoanRestructureRejectRequest {

    @NotBlank
    private String reason;
}
