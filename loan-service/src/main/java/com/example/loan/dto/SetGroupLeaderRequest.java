package com.example.loan.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SetGroupLeaderRequest {

    @NotNull
    private Long customerId;
}
