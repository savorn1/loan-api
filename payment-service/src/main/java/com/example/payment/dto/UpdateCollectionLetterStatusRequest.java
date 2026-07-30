package com.example.payment.dto;

import com.example.payment.entity.LetterStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateCollectionLetterStatusRequest {

    @NotNull
    private LetterStatus status;
}
