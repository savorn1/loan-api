package com.example.accounting.scheduler;

import com.example.accounting.entity.FinancialPeriod;
import com.example.accounting.entity.FinancialPeriodStatus;
import com.example.accounting.repository.FinancialPeriodRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Every auto-generated journal entry (JournalEntryServiceImpl.generate) needs an OPEN
// FinancialPeriod covering today, or every disbursement/payment across the whole loan
// lifecycle hard-fails — this scheduler is the only thing standing between "someone forgot
// to open next month's period" and a system-wide outage, so its trigger boundary and
// idempotency matter more than most schedulers would.
@ExtendWith(MockitoExtension.class)
class FinancialPeriodRolloverSchedulerTest {

    @Mock
    private FinancialPeriodRepository financialPeriodRepository;

    private FinancialPeriodRolloverScheduler scheduler;

    private FinancialPeriod periodEndingIn(long daysFromNow) {
        LocalDate end = LocalDate.now().plusDays(daysFromNow);
        FinancialPeriod period = FinancialPeriod.builder()
                .periodName("current")
                .startDate(end.minusDays(29))
                .endDate(end)
                .status(FinancialPeriodStatus.OPEN)
                .build();
        period.setId(1L);
        return period;
    }

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        scheduler = new FinancialPeriodRolloverScheduler(financialPeriodRepository);
    }

    @Test
    void doesNothing_whenNoPeriodHasEverBeenCreated() {
        when(financialPeriodRepository.findTopByOrderByEndDateDesc()).thenReturn(Optional.empty());

        scheduler.ensureNextPeriodExists();

        verify(financialPeriodRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void doesNothing_whenLatestPeriodEndsWellInTheFuture() {
        when(financialPeriodRepository.findTopByOrderByEndDateDesc())
                .thenReturn(Optional.of(periodEndingIn(30)));

        scheduler.ensureNextPeriodExists();

        verify(financialPeriodRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void createsNextPeriod_whenLatestPeriodEndsWithinThreshold() {
        FinancialPeriod latest = periodEndingIn(3);
        when(financialPeriodRepository.findTopByOrderByEndDateDesc()).thenReturn(Optional.of(latest));
        when(financialPeriodRepository.existsByPeriodName(org.mockito.ArgumentMatchers.any())).thenReturn(false);

        scheduler.ensureNextPeriodExists();

        ArgumentCaptor<FinancialPeriod> captor = ArgumentCaptor.forClass(FinancialPeriod.class);
        verify(financialPeriodRepository).save(captor.capture());
        FinancialPeriod created = captor.getValue();

        LocalDate expectedStart = latest.getEndDate().plusDays(1);
        LocalDate expectedEnd = expectedStart.with(TemporalAdjusters.lastDayOfMonth());
        String expectedName = expectedStart.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        assertThat(created.getStartDate()).isEqualTo(expectedStart);
        assertThat(created.getEndDate()).isEqualTo(expectedEnd);
        assertThat(created.getPeriodName()).isEqualTo(expectedName);
        assertThat(created.getStatus()).isEqualTo(FinancialPeriodStatus.OPEN);
    }

    @Test
    void doesNothing_whenNextPeriodNameAlreadyExists() {
        // e.g. an admin already created next month's period by hand before the scheduler ran.
        when(financialPeriodRepository.findTopByOrderByEndDateDesc())
                .thenReturn(Optional.of(periodEndingIn(1)));
        when(financialPeriodRepository.existsByPeriodName(org.mockito.ArgumentMatchers.any())).thenReturn(true);

        scheduler.ensureNextPeriodExists();

        verify(financialPeriodRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
