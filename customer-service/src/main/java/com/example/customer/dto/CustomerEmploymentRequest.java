package com.example.customer.dto;

import com.example.customer.entity.EmploymentStatus;
import com.example.customer.entity.EmploymentType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CustomerEmploymentRequest {

    @NotBlank
    private String companyName;

    private String occupation;

    @NotNull
    private EmploymentType employmentType;

    @DecimalMin(value = "0", inclusive = true)
    private BigDecimal salary;

    private String currency;
    private LocalDate startDate;

    // Defaults to ACTIVE in CustomerServiceImpl when omitted.
    private EmploymentStatus status;
}
