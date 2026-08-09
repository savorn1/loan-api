package com.example.accounting.scheduler;

import com.example.accounting.client.NotificationClient;
import com.example.accounting.dto.NotificationChannel;
import com.example.accounting.dto.NotificationRequest;
import com.example.accounting.dto.RecipientType;
import com.example.accounting.dto.ReconciliationSnapshotResponse;
import com.example.accounting.service.ReconciliationService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

// Daily reconciliation check, persisted so the report can show a trend instead of only "right
// now" — see ReconciliationSnapshot. On a mismatch, attempts to email reconciliation.alert-email
// (unset by default: this system has no real ops distribution list configured, so alerting is
// opt-in rather than sending to a made-up address). Runs after FinancialPeriodRolloverScheduler
// (3:00) so a same-day period rollover doesn't get flagged as a data problem.
@Component
@RequiredArgsConstructor
@Slf4j
public class ReconciliationScheduler {

    private final ReconciliationService reconciliationService;
    private final NotificationClient notificationClient;

    @Value("${reconciliation.alert-email:}")
    private String alertEmail;

    @Scheduled(cron = "0 30 3 * * *")
    public void dailyCheck() {
        ReconciliationSnapshotResponse snapshot = reconciliationService.takeSnapshot();
        if (snapshot.isMatched()) {
            return;
        }

        log.warn("Loans Receivable reconciliation mismatch: glBalance={} loanServiceTotal={} variance={}",
                snapshot.getGlBalance(), snapshot.getLoanServiceOutstandingTotal(), snapshot.getVariance());

        if (!StringUtils.hasText(alertEmail)) {
            return;
        }
        try {
            notificationClient.create(NotificationRequest.builder()
                    .recipientType(RecipientType.USER)
                    .channel(NotificationChannel.EMAIL)
                    .recipientContact(alertEmail)
                    .subject("Loans Receivable reconciliation mismatch")
                    .message("GL balance (" + snapshot.getGlBalance() + ") and the loan portfolio total ("
                            + snapshot.getLoanServiceOutstandingTotal() + ") disagree by "
                            + snapshot.getVariance() + " as of " + snapshot.getCheckedAt() + ".")
                    .build());
        } catch (FeignException ex) {
            log.warn("Failed to send reconciliation mismatch alert: {}", ex.getMessage());
        }
    }
}
