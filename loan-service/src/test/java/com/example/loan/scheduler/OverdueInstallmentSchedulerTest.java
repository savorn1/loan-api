package com.example.loan.scheduler;

import com.example.loan.entity.Loan;
import com.example.loan.entity.LoanPenalty;
import com.example.loan.entity.LoanSchedule;
import com.example.loan.entity.LoanScheduleInstallment;
import com.example.loan.entity.ScheduleInstallmentStatus;
import com.example.loan.repository.LoanPenaltyRepository;
import com.example.loan.repository.LoanScheduleInstallmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OverdueInstallmentSchedulerTest {

    @Mock private LoanScheduleInstallmentRepository loanScheduleInstallmentRepository;
    @Mock private LoanPenaltyRepository loanPenaltyRepository;

    private OverdueInstallmentScheduler scheduler;
    private Loan loan;
    private LoanScheduleInstallment installment;

    @BeforeEach
    void setUp() {
        scheduler = new OverdueInstallmentScheduler(loanScheduleInstallmentRepository, loanPenaltyRepository);
        ReflectionTestUtils.setField(scheduler, "autoChargeEnabled", true);
        ReflectionTestUtils.setField(scheduler, "overdueRatePercent", new BigDecimal("5"));

        loan = Loan.builder().build();
        loan.setId(7L);

        LoanSchedule schedule = LoanSchedule.builder().loan(loan).build();
        schedule.setId(1L);

        installment = LoanScheduleInstallment.builder()
                .schedule(schedule).loan(loan).installmentNumber(1)
                .principalAmount(new BigDecimal("45.83")).interestAmount(new BigDecimal("4.17"))
                .totalAmount(new BigDecimal("50.00")).status(ScheduleInstallmentStatus.PENDING)
                .dueDate(LocalDate.now().minusDays(1)).build();
        installment.setId(115L);
    }

    @Test
    void flagOverdueInstallments_flagsOverdueAndChargesAPenalty() {
        when(loanScheduleInstallmentRepository.findByDueDateBeforeAndStatusIn(any(), any()))
                .thenReturn(List.of(installment));
        when(loanPenaltyRepository.existsByScheduleInstallmentId(115L)).thenReturn(false);

        scheduler.flagOverdueInstallments();

        assertThat(installment.getStatus()).isEqualTo(ScheduleInstallmentStatus.OVERDUE);
        verify(loanScheduleInstallmentRepository).saveAll(List.of(installment));

        ArgumentCaptor<LoanPenalty> captor = ArgumentCaptor.forClass(LoanPenalty.class);
        verify(loanPenaltyRepository).save(captor.capture());
        assertThat(captor.getValue().getAmount()).isEqualByComparingTo("2.50"); // 5% of 50.00
        assertThat(captor.getValue().getScheduleInstallment()).isSameAs(installment);
    }

    @Test
    void flagOverdueInstallments_doesNotDoubleChargeAnAlreadyPenalizedInstallment() {
        when(loanScheduleInstallmentRepository.findByDueDateBeforeAndStatusIn(any(), any()))
                .thenReturn(List.of(installment));
        when(loanPenaltyRepository.existsByScheduleInstallmentId(115L)).thenReturn(true);

        scheduler.flagOverdueInstallments();

        assertThat(installment.getStatus()).isEqualTo(ScheduleInstallmentStatus.OVERDUE);
        verify(loanPenaltyRepository, never()).save(any());
    }

    @Test
    void flagOverdueInstallments_skipsPenaltyWhenAutoChargeDisabled() {
        ReflectionTestUtils.setField(scheduler, "autoChargeEnabled", false);
        when(loanScheduleInstallmentRepository.findByDueDateBeforeAndStatusIn(any(), any()))
                .thenReturn(List.of(installment));

        scheduler.flagOverdueInstallments();

        assertThat(installment.getStatus()).isEqualTo(ScheduleInstallmentStatus.OVERDUE);
        verify(loanPenaltyRepository, never()).existsByScheduleInstallmentId(anyLong());
        verify(loanPenaltyRepository, never()).save(any());
    }
}
