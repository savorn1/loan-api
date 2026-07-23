package com.example.loan.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ApplicationNoteRequest {

    @NotBlank
    private String note;
}
