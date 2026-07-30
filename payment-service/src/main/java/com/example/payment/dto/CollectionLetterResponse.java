package com.example.payment.dto;

import com.example.payment.entity.LetterDeliveryMethod;
import com.example.payment.entity.LetterStatus;
import com.example.payment.entity.LetterType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CollectionLetterResponse {

    private Long id;
    private Long loanId;
    private LetterType letterType;
    private LetterDeliveryMethod deliveryMethod;
    private LetterStatus status;
    private String recipientAddress;
    private String content;
    private String generatedByName;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;
}
