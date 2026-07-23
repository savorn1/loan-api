package com.example.accounting.dto;

import com.example.accounting.entity.EntrySide;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class JournalTemplateLineRequest {

    @NotNull
    private Integer lineNo;

    @NotBlank
    @Size(max = 50)
    private String accountRole;

    @NotNull
    private EntrySide entrySide;

    @Size(max = 255)
    private String description;
}
