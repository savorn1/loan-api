package com.example.loan.dto;

import lombok.Data;

import java.util.UUID;

// Local mirror of loan-product-service's dto.LoanProductResponse — only the
// fields this service needs (display name) for enriching GroupResponse.loanProductName.
@Data
public class LoanProductResponse {

    private UUID id;
    private String name;
}
