package com.example.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectionTrendPointResponse {

    private String month;
    private long paymentCount;
    private BigDecimal totalCollected;
}
