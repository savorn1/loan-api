package com.example.loan.config;

import com.example.loan.entity.LoanPayment;
import com.example.loan.repository.LoanPaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

// One-time backfill for loan payments left with a null paymentNo — every payment ever
// recorded until the fix in LoanServiceImpl (addPayment/payoff/applyPayment), since a
// plain entity save() silently can't write an updatable=false column once the row exists
// (see the comment on LoanPayment.paymentNo). Idempotent: only rows still null get
// touched, so this is a no-op on every startup after the first. Mirrors LoanNoBackfill.
@Component
@RequiredArgsConstructor
@Slf4j
public class LoanPaymentNoBackfill implements CommandLineRunner {

    private final LoanPaymentRepository loanPaymentRepository;

    @Override
    @Transactional
    public void run(String... args) {
        List<LoanPayment> missing = loanPaymentRepository.findByPaymentNoIsNull();
        if (missing.isEmpty()) {
            return;
        }
        for (LoanPayment payment : missing) {
            loanPaymentRepository.updatePaymentNo(payment.getId(), generatePaymentNo(payment));
        }
        log.info("Backfilled paymentNo for {} loan payment(s)", missing.size());
    }

    private String generatePaymentNo(LoanPayment payment) {
        String datePart = payment.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "PMT-" + datePart + "-" + String.format("%06d", payment.getId());
    }
}
