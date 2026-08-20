package com.example.loan.dto;

import lombok.Data;

// Local mirror of customer-service's dto.CustomerEmploymentResponse — only what
// EligibilityServiceImpl needs for an EMPLOYMENT_STATUS rule. employmentType/status
// are plain Strings rather than shared enums, same reasoning as LoanProductResponse.
@Data
public class CustomerEmploymentResponse {

    private String employmentType;
    private String status;
}
