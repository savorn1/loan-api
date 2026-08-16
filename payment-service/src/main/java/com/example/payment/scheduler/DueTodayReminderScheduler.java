package com.example.payment.scheduler;

import com.example.payment.entity.Payment;
import com.example.payment.entity.PaymentStatus;
import com.example.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

// Same-day reminder, distinct from DueSoonReminderScheduler's N-days-ahead heads up —
// a payment due today gets both, since they fire at different points and neither
// implies the other (a customer could miss the 3-days-ahead one and still act on this).
@Component
@RequiredArgsConstructor
public class DueTodayReminderScheduler {

    private final PaymentRepository paymentRepository;
    private final PaymentReminderNotifier reminderNotifier;

    // Runs after DueSoonReminderScheduler/before OverdueScheduler — see that class's
    // comment on why both live in the same 00:xx-01:xx window.
    @Scheduled(cron = "0 58 0 * * *")
    public void sendDueTodayReminders() {
        List<Payment> dueToday = paymentRepository.findByDueDateAndStatus(LocalDate.now(), PaymentStatus.PENDING);

        for (Payment payment : dueToday) {
            reminderNotifier.notify(payment, "Payment due today",
                    String.format("Your payment of %s for loan #%d is due today.",
                            payment.getAmount(), payment.getLoanId()));
        }
    }
}
