package com.example.payment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignCollectionCaseRequest {

    @NotNull
    private Long userId;

    private String note;
}
