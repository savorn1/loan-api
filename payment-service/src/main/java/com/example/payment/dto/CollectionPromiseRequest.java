package com.example.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CollectionPromiseRequest {

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal promisedAmount;

    @NotNull
    private LocalDate promisedDate;

    private String notes;
}
