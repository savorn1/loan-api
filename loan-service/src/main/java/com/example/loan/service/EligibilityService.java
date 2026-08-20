package com.example.loan.service;

import com.example.loan.dto.EligibilityCheckResponse;

import java.util.List;
import java.util.UUID;

public interface EligibilityService {

    // Evaluates every ACTIVE rule assigned to loanProductId against the customer.
    // Never throws for a rule it can't evaluate — see EligibilityResult.NOT_EVALUABLE.
    List<EligibilityCheckResponse> checkEligibility(Long customerId, UUID loanProductId);
}
