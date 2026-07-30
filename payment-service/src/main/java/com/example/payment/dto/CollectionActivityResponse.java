package com.example.payment.dto;

import com.example.payment.entity.ContactMethod;
import com.example.payment.entity.ContactOutcome;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class CollectionActivityResponse {

    private Long id;
    private Long loanId;
    private String authorName;
    private ContactMethod contactMethod;
    private ContactOutcome outcome;
    private String note;
    private LocalDate followUpDate;
    private LocalDateTime createdAt;
}
