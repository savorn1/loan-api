package com.example.loan.config;

import com.example.loan.entity.GroupLoanApplication;
import com.example.loan.repository.GroupLoanApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

// One-time backfill for group loan applications left with a null applicationNo — rows
// that predate the column (see the comment on GroupLoanApplication.applicationNo).
// Idempotent: only rows still null get touched, so this is a no-op on every startup
// after the first. Mirrors ApplicationNoBackfill.
@Component
@RequiredArgsConstructor
@Slf4j
public class GroupLoanApplicationNoBackfill implements CommandLineRunner {

    private final GroupLoanApplicationRepository applicationRepository;

    @Override
    @Transactional
    public void run(String... args) {
        List<GroupLoanApplication> missing = applicationRepository.findByApplicationNoIsNull();
        if (missing.isEmpty()) {
            return;
        }
        for (GroupLoanApplication application : missing) {
            applicationRepository.updateApplicationNo(application.getId(), generateApplicationNo(application));
        }
        log.info("Backfilled applicationNo for {} group loan application(s)", missing.size());
    }

    private String generateApplicationNo(GroupLoanApplication application) {
        String datePart = application.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "GAPP-" + datePart + "-" + String.format("%06d", application.getId());
    }
}
