package com.example.payment.dto;

import lombok.Data;

// Minimal projection of customer-service's CustomerResponse — only what this
// service needs to resolve a transaction's customerName. Extra fields on the
// real payload are ignored by Jackson rather than causing a deserialize error.
@Data
public class CustomerResponse {

    private Long id;
    private String fullName;
    private String phone;
    private String email;
}
