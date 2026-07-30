package com.example.payment.dto;

import com.example.payment.entity.PromiseStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ResolveCollectionPromiseRequest {

    @NotNull
    private PromiseStatus status;

    private BigDecimal amountPaid;
}
