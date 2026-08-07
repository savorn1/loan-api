package com.example.customer.dto;

import com.example.customer.entity.IdentityStatus;
import com.example.customer.entity.IdentityType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class CustomerIdentityResponse {

    private Long id;
    private Long customerId;
    private IdentityType identityType;
    private String identityNumber;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String issuingCountry;
    private IdentityStatus status;
    private String scanFileName;
    private String scanFileUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
