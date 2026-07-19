package com.example.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleInstallmentRequest {

    @NotNull
    private Integer installmentNumber;

    @NotNull
    private LocalDate dueDate;

    @NotNull
    private BigDecimal principalComponent;

    @NotNull
    private BigDecimal interestComponent;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;
}
