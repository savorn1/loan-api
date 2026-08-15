package com.example.loan.config;

import com.example.loan.entity.Application;
import com.example.loan.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

// One-time backfill for applications left with a null applicationNo — either rows that
// predate the column (see the comment on Application.applicationNo) or, until the fix in
// ApplicationServiceImpl.create(), any application at all, since a plain entity save()
// silently can't write an updatable=false column once the row exists. Idempotent: only
// rows still null get touched, so this is a no-op on every startup after the first.
@Component
@RequiredArgsConstructor
@Slf4j
public class ApplicationNoBackfill implements CommandLineRunner {

    private final ApplicationRepository applicationRepository;

    @Override
    @Transactional
    public void run(String... args) {
        List<Application> missing = applicationRepository.findByApplicationNoIsNull();
        if (missing.isEmpty()) {
            return;
        }
        for (Application application : missing) {
            applicationRepository.updateApplicationNo(application.getId(), generateApplicationNo(application));
        }
        log.info("Backfilled applicationNo for {} application(s)", missing.size());
    }

    private String generateApplicationNo(Application application) {
        String datePart = application.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "APP-" + datePart + "-" + String.format("%06d", application.getId());
    }
}
