package com.example.payment.dto;

import com.example.payment.entity.ContactMethod;
import com.example.payment.entity.ContactOutcome;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CollectionActivityRequest {

    @NotNull
    private ContactMethod contactMethod;

    @NotNull
    private ContactOutcome outcome;

    @NotBlank
    private String note;

    private LocalDate followUpDate;
}
