package com.example.loan.dto;

import com.example.loan.entity.LoanStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class LoanStatusHistoryResponse {

    private Long id;
    private Long loanId;
    private LoanStatus fromStatus;
    private LoanStatus toStatus;
    private String note;
    private String changedBy;
    private LocalDateTime changedAt;
}
