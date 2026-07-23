package com.example.loanproduct.dto;

import com.example.loanproduct.entity.InterestCalculationMethod;
import com.example.loanproduct.entity.InterestSchemeStatus;
import com.example.loanproduct.entity.InterestType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class InterestSchemeRequest {

    @NotBlank
    @Size(max = 30)
    private String code;

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotNull
    private InterestType interestType;

    @NotNull
    private InterestCalculationMethod calculationMethod;

    @NotNull
    private InterestSchemeStatus status;
}
