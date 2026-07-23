package com.example.loan.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

// An in-place term/rate change on an ACTIVE loan — not a PENDING/APPROVED
// workflow of its own. Applying one re-amortizes the loan's *outstanding*
// balance (not its original principal) over the new term/rate as of
// effectiveDate: LoanServiceImpl.addRestructure() updates the loan's
// termMonths/interestRate/maturityDate/monthlyInstallment and regenerates
// its LoanSchedule in the same transaction as saving this history row.
@Entity
@Table(name = "loan_restructures")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanRestructure extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @Column(name = "new_term_months", nullable = false)
    private Integer newTermMonths;

    @Column(name = "new_interest_rate", precision = 5, scale = 2)
    private BigDecimal newInterestRate;

    @Column(nullable = false)
    private String reason;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;
}
