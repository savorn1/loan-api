package com.example.loan.scheduler;

import com.example.loan.entity.Loan;
import com.example.loan.entity.LoanPenalty;
import com.example.loan.entity.LoanScheduleInstallment;
import com.example.loan.entity.PenaltyStatus;
import com.example.loan.entity.ScheduleInstallmentStatus;
import com.example.loan.repository.LoanPenaltyRepository;
import com.example.loan.repository.LoanScheduleInstallmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

// Flags PENDING/PARTIALLY_PAID installments past their dueDate as OVERDUE —
// ScheduleInstallmentStatus.OVERDUE existed as an enum value with nothing that ever
// set it until this (mirrors payment-service's OverdueScheduler, which does the same
// for its own separate Payment ledger). Optionally auto-charges a late-payment penalty
// on the same run for whatever just transitioned — see the rate-percent property below.
@Component
@RequiredArgsConstructor
public class OverdueInstallmentScheduler {

    private final LoanScheduleInstallmentRepository loanScheduleInstallmentRepository;
    private final LoanPenaltyRepository loanPenaltyRepository;

    @Value("${loan.penalty.overdue.auto-charge-enabled:true}")
    private boolean autoChargeEnabled;

    // Percentage of the overdue installment's total amount. Not sourced from any
    // per-product fee scheme: Loan doesn't carry a productId at runtime today, so
    // loan-product-service's FeeScheme/FeeType.LATE_PAYMENT config (the natural home
    // for a real per-product late-fee rate) isn't reachable from here. This is a flat,
    // system-wide default until that link exists — tune per environment as needed.
    @Value("${loan.penalty.overdue.rate-percent:5}")
    private BigDecimal overdueRatePercent;

    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void flagOverdueInstallments() {
        List<LoanScheduleInstallment> overdue = loanScheduleInstallmentRepository
                .findByDueDateBeforeAndStatusIn(LocalDate.now(),
                        List.of(ScheduleInstallmentStatus.PENDING, ScheduleInstallmentStatus.PARTIALLY_PAID));

        overdue.forEach(installment -> installment.setStatus(ScheduleInstallmentStatus.OVERDUE));
        loanScheduleInstallmentRepository.saveAll(overdue);

        if (autoChargeEnabled) {
            overdue.forEach(this::chargePenaltyIfNotAlreadyCharged);
        }
    }

    // A partial payment on an already-OVERDUE installment resets its status back to
    // PARTIALLY_PAID (see LoanServiceImpl.allocatePayment, which only ever sets
    // PAID/PARTIALLY_PAID), which would otherwise make it eligible for this query
    // again the next day. This guard is what keeps that from double-charging it.
    private void chargePenaltyIfNotAlreadyCharged(LoanScheduleInstallment installment) {
        if (loanPenaltyRepository.existsByScheduleInstallmentId(installment.getId())) {
            return;
        }
        BigDecimal amount = installment.getTotalAmount()
                .multiply(overdueRatePercent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        Loan loan = installment.getLoan();
        loanPenaltyRepository.save(LoanPenalty.builder()
                .loan(loan)
                .amount(amount)
                .reason("Automatic late payment penalty — installment #" + installment.getInstallmentNumber())
                .appliedDate(LocalDate.now())
                .status(PenaltyStatus.PENDING)
                .scheduleInstallment(installment)
                .build());
    }
}
