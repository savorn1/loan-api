package com.example.payment.scheduler;

import com.example.payment.client.PaymentWalletClient;
import com.example.payment.dto.PaymentWalletDepositResponse;
import com.example.payment.entity.PaymentTransaction;
import com.example.payment.entity.TransactionStatus;
import com.example.payment.repository.PaymentTransactionRepository;
import com.example.payment.service.PaymentTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

// Fallback for payment-wallet deposits whose webhook delivery (see
// PaymentWalletWebhookController) never arrived — the docs are explicit that failed
// deliveries are NOT retried automatically, so without this a dropped delivery leaves
// a PaymentTransaction stuck PENDING forever with money that may have already moved on
// payment-wallet's side. Polls GET /deposits/{id} for anything still PENDING past a
// grace period and applies the same status mapping the webhook uses.
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentWalletReconciliationScheduler {

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final PaymentWalletClient paymentWalletClient;
    private final PaymentTransactionService paymentTransactionService;

    // Deliberately not near-instant: the webhook is the primary, near-real-time path.
    // This grace period just keeps a normal in-flight deposit (a customer still
    // sitting on the payment page) from being polled before it's had a fair chance to
    // settle on its own.
    @Value("${payment-wallet.reconciliation.grace-period-minutes:5}")
    private long gracePeriodMinutes;

    @Scheduled(fixedDelayString = "${payment-wallet.reconciliation.interval-ms:300000}")
    public void reconcilePendingDeposits() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(gracePeriodMinutes);
        List<PaymentTransaction> stale = paymentTransactionRepository
                .findByStatusAndWalletDepositIdIsNotNullAndRequestedAtBefore(TransactionStatus.PENDING, cutoff);
        if (stale.isEmpty()) {
            return;
        }
        log.info("Reconciling {} stale payment-wallet deposit(s) still PENDING past {} minute(s)",
                stale.size(), gracePeriodMinutes);
        for (PaymentTransaction transaction : stale) {
            reconcileOne(transaction);
        }
    }

    private void reconcileOne(PaymentTransaction transaction) {
        try {
            PaymentWalletDepositResponse deposit = paymentWalletClient.getDeposit(
                    transaction.getPaymentGateway().getApiUrl(), transaction.getWalletDepositId());
            paymentTransactionService.applyWalletDepositWebhook(
                    deposit.getId(), deposit.getStatus(), deposit.getFailureReason());
        } catch (Exception e) {
            // One bad lookup (e.g. payment-wallet briefly unreachable) shouldn't stop the
            // rest of the batch — it'll simply be picked up again on the next run.
            log.warn("Failed to reconcile payment-wallet deposit id {} for transaction {}",
                    transaction.getWalletDepositId(), transaction.getId(), e);
        }
    }
}
