package com.example.customer.dto;

import com.example.customer.entity.EmploymentStatus;
import com.example.customer.entity.EmploymentType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class CustomerEmploymentResponse {

    private Long id;
    private Long customerId;
    private String companyName;
    private String occupation;
    private EmploymentType employmentType;
    private BigDecimal salary;
    private String currency;
    private LocalDate startDate;
    private EmploymentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
