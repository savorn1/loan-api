package com.example.loan.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "group_loan_members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupLoanMember extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private GroupLoanApplication application;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "requested_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal requestedAmount;

    // Interpreted per requestedTermUnit — see Loan.termMonths for the DAY/MONTH/YEAR rules.
    @Column(name = "requested_term_months", nullable = false)
    private Integer requestedTermMonths;

    // Default backfills existing rows when Hibernate adds this NOT NULL column
    // to a non-empty table under ddl-auto=update.
    @Enumerated(EnumType.STRING)
    @Column(name = "requested_term_unit", nullable = false, columnDefinition = "varchar(10) default 'MONTH'")
    @Builder.Default
    private TermUnit requestedTermUnit = TermUnit.MONTH;

    @Column(name = "approved_amount", precision = 15, scale = 2)
    private BigDecimal approvedAmount;

    @Column(name = "approved_interest_rate", precision = 5, scale = 2)
    private BigDecimal approvedInterestRate;

    @Column(name = "approved_term_months")
    private Integer approvedTermMonths;

    @Enumerated(EnumType.STRING)
    @Column(name = "approved_term_unit")
    private TermUnit approvedTermUnit;

    @Column(name = "loan_id")
    private Long loanId;
}
