package com.example.payment.repository;

import com.example.payment.entity.PaymentTransactionItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentTransactionItemRepository extends JpaRepository<PaymentTransactionItem, Long> {

    List<PaymentTransactionItem> findByPaymentTransactionId(Long paymentTransactionId);
}
