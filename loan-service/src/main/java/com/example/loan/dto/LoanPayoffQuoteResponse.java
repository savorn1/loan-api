package com.example.loan.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

// What it actually costs to close this loan today, as opposed to Loan.outstandingBalance —
// which is set at disbursement to the sum of every future installment (principal *and*
// interest for the full term) and never discounted for interest not yet accrued. Paying
// outstandingBalance early means paying for months you never actually borrowed the money.
// See LoanServiceImpl.computePayoffQuote for the accrual methodology (simple daily interest
// on the remaining principal since the last payment, not Rule-of-78 or any other
// front-loaded method).
@Data
@Builder
public class LoanPayoffQuoteResponse {

    private Long loanId;
    private LocalDate asOfDate;
    private BigDecimal remainingPrincipal;
    private BigDecimal accruedInterest;
    private BigDecimal outstandingFees;
    private BigDecimal outstandingPenalties;
    private BigDecimal totalPayoffAmount;
}
