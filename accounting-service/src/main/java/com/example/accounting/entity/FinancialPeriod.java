package com.example.accounting.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "financial_periods")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialPeriod extends BaseEntity {

    // e.g. "2026-07" — display label, not parsed for date logic.
    @Column(name = "period_name", nullable = false, unique = true, length = 20)
    private String periodName;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private FinancialPeriodStatus status = FinancialPeriodStatus.OPEN;
}
