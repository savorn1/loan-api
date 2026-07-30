package com.example.payment.dto;

import com.example.payment.entity.PaymentMethodStatus;
import com.example.payment.entity.PaymentMethodType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PaymentMethodRequest {

    @NotBlank
    @Size(max = 30)
    private String code;

    @NotBlank
    @Size(max = 150)
    private String name;

    @NotNull
    private PaymentMethodType type;

    @NotNull
    private PaymentMethodStatus status;
}
