package com.example.payment.scheduler;

import com.example.payment.entity.Payment;
import com.example.payment.entity.PaymentStatus;
import com.example.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DueSoonReminderScheduler {

    private final PaymentRepository paymentRepository;
    private final PaymentReminderNotifier reminderNotifier;

    @Value("${notification.reminder.due-soon-days:3}")
    private int dueSoonDays;

    // Runs shortly before OverdueScheduler — no ordering dependency between
    // the two, they touch disjoint payments (PENDING due in N days vs PENDING
    // already past due), but keeping both in the 00:xx-01:xx window keeps
    // reminder timing predictable.
    @Scheduled(cron = "0 55 0 * * *")
    public void sendDueSoonReminders() {
        LocalDate targetDate = LocalDate.now().plusDays(dueSoonDays);
        List<Payment> dueSoon = paymentRepository.findByDueDateAndStatus(targetDate, PaymentStatus.PENDING);

        for (Payment payment : dueSoon) {
            reminderNotifier.notify(payment, "Payment due soon",
                    String.format("Your payment of %s for loan #%d is due on %s.",
                            payment.getAmount(), payment.getLoanId(), payment.getDueDate()));
        }
    }
}
