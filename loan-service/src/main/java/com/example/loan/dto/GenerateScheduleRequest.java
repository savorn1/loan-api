package com.example.loan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateScheduleRequest {

    private Long loanId;
    private List<ScheduleInstallmentRequest> installments;
}
