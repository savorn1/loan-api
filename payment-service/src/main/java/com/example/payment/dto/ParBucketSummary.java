package com.example.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParBucketSummary {

    private CollectionBucket bucket;
    private long loanCount;
    private BigDecimal overdueAmount;
}
