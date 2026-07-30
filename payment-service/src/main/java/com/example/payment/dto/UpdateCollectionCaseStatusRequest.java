package com.example.payment.dto;

import com.example.payment.entity.CollectionCaseStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateCollectionCaseStatusRequest {

    @NotNull
    private CollectionCaseStatus status;

    private String note;
}
