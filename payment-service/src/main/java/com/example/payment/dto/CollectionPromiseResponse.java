package com.example.payment.dto;

import com.example.payment.entity.PromiseStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class CollectionPromiseResponse {

    private Long id;
    private Long loanId;
    private BigDecimal promisedAmount;
    private LocalDate promisedDate;
    private PromiseStatus status;
    private BigDecimal amountPaid;
    private String notes;
    private String createdByName;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;
}
