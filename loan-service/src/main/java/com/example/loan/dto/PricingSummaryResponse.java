package com.example.loan.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

// "Product/interest-type" pricing isn't derivable yet — individual Loan records don't
// carry a loanProductId (only group loans do, via Group.loanProductId), so this reports
// pricing by the two axes Loan actually stores: interest rate and term.
@Data
@Builder
public class PricingSummaryResponse {

    private List<PricingBandRow> byRateBand;
    private List<PricingBandRow> byTermBand;
}
