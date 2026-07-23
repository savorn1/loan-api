package com.example.loanproduct.dto;

import com.example.loanproduct.entity.RuleField;
import com.example.loanproduct.entity.RuleOperator;
import com.example.loanproduct.entity.RuleTemplateStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RuleTemplateRequest {

    @NotBlank
    @Size(max = 30)
    private String code;

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotNull
    private RuleField field;

    @NotNull
    private RuleOperator operator;

    @NotBlank
    private String value;

    // Required only when operator is BETWEEN — checked in the service layer since it's a
    // cross-field rule, not expressible with a single bean-validation annotation.
    private String value2;

    private String description;

    @NotNull
    private RuleTemplateStatus status;
}
