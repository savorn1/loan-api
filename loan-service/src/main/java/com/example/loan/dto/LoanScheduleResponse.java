package com.example.loan.dto;

import com.example.loan.entity.ScheduleStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class LoanScheduleResponse {

    private Long id;
    private Long loanId;
    private LocalDateTime generatedAt;
    private Integer totalInstallments;
    private ScheduleStatus status;
    private LocalDateTime createdAt;
}
