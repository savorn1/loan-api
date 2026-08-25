package com.example.payment.repository;

import com.example.payment.entity.PaymentTransaction;
import com.example.payment.entity.TransactionStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    Optional<PaymentTransaction> findByWalletDepositId(Long walletDepositId);

    Optional<PaymentTransaction> findByIdempotencyKey(String idempotencyKey);

    // Used by PaymentWalletReconciliationScheduler to catch deposits whose webhook
    // delivery never arrived (payment-wallet doesn't retry failed deliveries). Eagerly
    // fetches paymentGateway — the scheduler reads gateway.apiUrl outside of any
    // request/transaction scope, where the lazy proxy can't otherwise be initialized.
    @EntityGraph(attributePaths = "paymentGateway")
    List<PaymentTransaction> findByStatusAndWalletDepositIdIsNotNullAndRequestedAtBefore(
            TransactionStatus status, LocalDateTime cutoff);
}
