package com.example.customer.dto;

import com.example.customer.entity.IdentityStatus;
import com.example.customer.entity.IdentityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CustomerIdentityRequest {

    @NotNull
    private IdentityType identityType;

    @NotBlank
    private String identityNumber;

    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String issuingCountry;

    // Defaults to ACTIVE in CustomerServiceImpl when omitted.
    private IdentityStatus status;
}
