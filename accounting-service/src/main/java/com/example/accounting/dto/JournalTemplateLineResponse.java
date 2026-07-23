package com.example.accounting.dto;

import com.example.accounting.entity.EntrySide;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JournalTemplateLineResponse {

    private Long id;
    private Integer lineNo;
    private String accountRole;
    private EntrySide entrySide;
    private String description;
}
