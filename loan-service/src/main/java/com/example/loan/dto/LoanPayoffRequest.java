package com.example.loan.dto;

import com.example.loan.entity.DisbursementMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// amount isn't a field here — it's computed server-side from the payoff quote as of today,
// not something the caller supplies (a stale/incorrect client-side quote could otherwise
// under- or over-close the loan).
@Data
public class LoanPayoffRequest {

    @NotNull
    private DisbursementMethod method;

    private String reference;
}
