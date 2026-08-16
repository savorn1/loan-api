package com.example.loan.config;

import com.example.loan.entity.LoanDisbursement;
import com.example.loan.repository.LoanDisbursementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

// One-time backfill for loan disbursements left with a null disbursementNo — every
// disbursement ever recorded until the fix in LoanServiceImpl.addDisbursement(), since a
// plain entity save() silently can't write an updatable=false column once the row exists
// (see the comment on LoanDisbursement.disbursementNo). Idempotent: only rows still null
// get touched, so this is a no-op on every startup after the first. Mirrors LoanNoBackfill.
@Component
@RequiredArgsConstructor
@Slf4j
public class LoanDisbursementNoBackfill implements CommandLineRunner {

    private final LoanDisbursementRepository loanDisbursementRepository;

    @Override
    @Transactional
    public void run(String... args) {
        List<LoanDisbursement> missing = loanDisbursementRepository.findByDisbursementNoIsNull();
        if (missing.isEmpty()) {
            return;
        }
        for (LoanDisbursement disbursement : missing) {
            loanDisbursementRepository.updateDisbursementNo(disbursement.getId(), generateDisbursementNo(disbursement));
        }
        log.info("Backfilled disbursementNo for {} loan disbursement(s)", missing.size());
    }

    private String generateDisbursementNo(LoanDisbursement disbursement) {
        String datePart = disbursement.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "DSB-" + datePart + "-" + String.format("%06d", disbursement.getId());
    }
}
