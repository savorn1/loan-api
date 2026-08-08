package com.example.accounting.scheduler;

import com.example.accounting.entity.FinancialPeriod;
import com.example.accounting.entity.FinancialPeriodStatus;
import com.example.accounting.repository.FinancialPeriodRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

// Every posting — manual or via JournalEntryServiceImpl.generate — requires an OPEN
// FinancialPeriod covering its transaction date. Periods are created manually today
// (see FinancialPeriodController), so if nobody opens next month's before this one ends,
// every disbursement/payment across the whole loan lifecycle starts hard-failing. This
// only ever creates the *next* period ahead of time — it never closes one, since deciding
// a period is done (reconciled, reported on) is a deliberate human action via /{id}/close,
// often well after month-end.
@Component
@RequiredArgsConstructor
@Slf4j
public class FinancialPeriodRolloverScheduler {

    private static final int DAYS_AHEAD = 5;
    private static final DateTimeFormatter PERIOD_NAME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

    private final FinancialPeriodRepository financialPeriodRepository;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void ensureNextPeriodExists() {
        FinancialPeriod latest = financialPeriodRepository.findTopByOrderByEndDateDesc().orElse(null);
        if (latest == null) {
            // No period has ever been created — that's the first one's setup, a deliberate
            // choice of calendar (not necessarily "this month"), left to a human.
            return;
        }
        if (latest.getEndDate().isAfter(LocalDate.now().plusDays(DAYS_AHEAD))) {
            return;
        }

        LocalDate nextStart = latest.getEndDate().plusDays(1);
        LocalDate nextEnd = nextStart.with(TemporalAdjusters.lastDayOfMonth());
        String periodName = nextStart.format(PERIOD_NAME_FORMAT);

        if (financialPeriodRepository.existsByPeriodName(periodName)) {
            return;
        }

        financialPeriodRepository.save(FinancialPeriod.builder()
                .periodName(periodName)
                .startDate(nextStart)
                .endDate(nextEnd)
                .status(FinancialPeriodStatus.OPEN)
                .build());
        log.info("Auto-created financial period {} ({} to {}), {} days before {} ends",
                periodName, nextStart, nextEnd, DAYS_AHEAD, latest.getPeriodName());
    }
}
