package com.example.payment.scheduler;

import com.example.payment.entity.Payment;
import com.example.payment.entity.PaymentStatus;
import com.example.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OverdueScheduler {

    private final PaymentRepository paymentRepository;
    private final PaymentReminderNotifier reminderNotifier;

    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void flagOverduePayments() {
        List<Payment> overdue = paymentRepository.findByDueDateBeforeAndStatus(LocalDate.now(), PaymentStatus.PENDING);
        overdue.forEach(p -> p.setStatus(PaymentStatus.OVERDUE));
        paymentRepository.saveAll(overdue);

        // Notify for exactly the rows just flipped in this run — querying by
        // status=OVERDUE instead would re-notify every day for as long as a
        // payment stays unpaid, since nothing else changes its status back.
        for (Payment payment : overdue) {
            reminderNotifier.notify(payment, "Payment overdue",
                    String.format("Your payment of %s for loan #%d (due %s) is now overdue.",
                            payment.getAmount(), payment.getLoanId(), payment.getDueDate()));
        }
    }
}
