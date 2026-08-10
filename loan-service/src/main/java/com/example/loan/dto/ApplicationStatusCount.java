package com.example.loan.dto;

import com.example.loan.entity.ApplicationStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApplicationStatusCount {

    private ApplicationStatus status;
    private long count;
}
