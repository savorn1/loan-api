package com.example.loan.dto;

import lombok.Data;

// Local mirror of customer-service's dto.CustomerIdentityResponse — only the
// field GroupServiceImpl needs (to derive GroupMemberResponse.kycStatus).
@Data
public class CustomerIdentityResponse {

    private String status;
}
