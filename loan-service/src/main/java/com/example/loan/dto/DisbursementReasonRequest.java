package com.example.loan.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DisbursementReasonRequest {

    @NotBlank
    private String reason;
}
