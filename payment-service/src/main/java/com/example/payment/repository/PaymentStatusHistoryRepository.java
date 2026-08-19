package com.example.payment.repository;

import com.example.payment.entity.PaymentStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentStatusHistoryRepository extends JpaRepository<PaymentStatusHistory, Long> {

    List<PaymentStatusHistory> findByPaymentIdOrderByCreatedAtDesc(Long paymentId);
}
