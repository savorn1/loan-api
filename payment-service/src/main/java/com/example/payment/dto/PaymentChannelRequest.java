package com.example.payment.dto;

import com.example.payment.entity.PaymentChannelStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PaymentChannelRequest {

    @NotBlank
    @Size(max = 30)
    private String code;

    @NotBlank
    @Size(max = 150)
    private String name;

    @NotBlank
    @Size(max = 50)
    private String channelType;

    @NotNull
    private PaymentChannelStatus status;
}
