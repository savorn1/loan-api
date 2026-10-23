package com.example.accounting.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * One source of cash in, or use of cash out, named by the non-cash account that sat
 * on the other side of the entry. {@code amount} is always positive — which direction
 * it represents is determined by the list it appears in.
 */
@Data
@Builder
public class CashFlowLine {

    /** Null for the synthetic "Unclassified" bucket (see CashFlowStatementResponse). */
    private Long glAccountId;
    private String accountNo;
    private String accountName;
    private BigDecimal amount;
}
