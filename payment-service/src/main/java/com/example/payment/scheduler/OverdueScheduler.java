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

    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void flagOverduePayments() {
        List<Payment> overdue = paymentRepository.findByDueDateBeforeAndStatus(LocalDate.now(), PaymentStatus.PENDING);
        overdue.forEach(p -> p.setStatus(PaymentStatus.OVERDUE));
        paymentRepository.saveAll(overdue);
    }
}
