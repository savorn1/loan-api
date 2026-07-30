package com.example.payment.dto;

import com.example.payment.entity.LetterDeliveryMethod;
import com.example.payment.entity.LetterType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CollectionLetterRequest {

    @NotNull
    private LetterType letterType;

    @NotNull
    private LetterDeliveryMethod deliveryMethod;

    private String recipientAddress;

    private String content;
}
