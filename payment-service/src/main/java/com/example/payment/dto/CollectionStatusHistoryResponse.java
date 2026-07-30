package com.example.payment.dto;

import com.example.payment.entity.CollectionCaseStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CollectionStatusHistoryResponse {

    private Long id;
    private Long loanId;
    private CollectionCaseStatus fromStatus;
    private CollectionCaseStatus toStatus;
    private String changedBy;
    private String note;
    private LocalDateTime changedAt;
}
