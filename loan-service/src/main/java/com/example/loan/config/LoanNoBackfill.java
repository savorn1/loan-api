package com.example.loan.config;

import com.example.loan.entity.Loan;
import com.example.loan.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

// One-time backfill for loans left with a null loanNo — every loan ever created until
// the fix in LoanServiceImpl.create(), since a plain entity save() silently can't write
// an updatable=false column once the row exists (see the comment on Loan.loanNo).
// Idempotent: only rows still null get touched, so this is a no-op on every startup
// after the first. Mirrors ApplicationNoBackfill.
@Component
@RequiredArgsConstructor
@Slf4j
public class LoanNoBackfill implements CommandLineRunner {

    private final LoanRepository loanRepository;

    @Override
    @Transactional
    public void run(String... args) {
        List<Loan> missing = loanRepository.findByLoanNoIsNull();
        if (missing.isEmpty()) {
            return;
        }
        for (Loan loan : missing) {
            loanRepository.updateLoanNo(loan.getId(), generateLoanNo(loan));
        }
        log.info("Backfilled loanNo for {} loan(s)", missing.size());
    }

    private String generateLoanNo(Loan loan) {
        String datePart = loan.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "LN-" + datePart + "-" + String.format("%06d", loan.getId());
    }
}
