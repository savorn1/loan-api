package com.example.payment.dto;

import com.example.payment.entity.CollectionCaseStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class CollectionCaseResponse {

    private Long loanId;
    private CollectionCaseStatus status;
    private Long assignedToUserId;
    private LocalDateTime lastContactAt;
    private LocalDate nextFollowUpAt;
}
