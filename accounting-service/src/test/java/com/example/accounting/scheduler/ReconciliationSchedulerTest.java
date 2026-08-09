package com.example.accounting.scheduler;

import com.example.accounting.client.NotificationClient;
import com.example.accounting.common.ApiResponse;
import com.example.accounting.dto.NotificationRequest;
import com.example.accounting.dto.NotificationResponse;
import com.example.accounting.dto.ReconciliationSnapshotResponse;
import com.example.accounting.service.ReconciliationService;
import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// ReconciliationScheduler alerting is opt-in (reconciliation.alert-email unset by default —
// no real ops distribution list exists for this system), so these tests cover both states:
// silent when unconfigured, and correctly notifying when it is.
@ExtendWith(MockitoExtension.class)
class ReconciliationSchedulerTest {

    @Mock
    private ReconciliationService reconciliationService;
    @Mock
    private NotificationClient notificationClient;

    private ReconciliationScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new ReconciliationScheduler(reconciliationService, notificationClient);
    }

    private ReconciliationSnapshotResponse snapshot(boolean matched) {
        return ReconciliationSnapshotResponse.builder()
                .id(1L).checkedAt(LocalDateTime.of(2026, 8, 9, 3, 30))
                .glAccountNo("1100").glBalance(new BigDecimal("1000.00"))
                .loanServiceOutstandingTotal(new BigDecimal("1200.00"))
                .variance(new BigDecimal("-200.00")).matched(matched).build();
    }

    @Test
    void dailyCheck_sendsNoNotificationWhenMatched() {
        when(reconciliationService.takeSnapshot()).thenReturn(snapshot(true));

        scheduler.dailyCheck();

        verify(notificationClient, never()).create(any());
    }

    @Test
    void dailyCheck_sendsNoNotificationWhenAlertEmailUnconfigured() {
        when(reconciliationService.takeSnapshot()).thenReturn(snapshot(false));
        // alertEmail left at its default (unset) — matches production default.

        scheduler.dailyCheck();

        verify(notificationClient, never()).create(any());
    }

    @Test
    void dailyCheck_notifiesConfiguredAddressOnMismatch() {
        ReflectionTestUtils.setField(scheduler, "alertEmail", "ops@example.com");
        when(reconciliationService.takeSnapshot()).thenReturn(snapshot(false));
        when(notificationClient.create(any())).thenReturn(ApiResponse.success(new NotificationResponse()));

        scheduler.dailyCheck();

        ArgumentCaptor<NotificationRequest> captor = ArgumentCaptor.forClass(NotificationRequest.class);
        verify(notificationClient).create(captor.capture());
        assertThat(captor.getValue().getRecipientContact()).isEqualTo("ops@example.com");
        assertThat(captor.getValue().getMessage()).contains("-200.00");
    }

    @Test
    void dailyCheck_doesNotPropagateWhenNotificationCallFails() {
        ReflectionTestUtils.setField(scheduler, "alertEmail", "ops@example.com");
        when(reconciliationService.takeSnapshot()).thenReturn(snapshot(false));
        Request request = Request.create(Request.HttpMethod.POST,
                "http://notification-service/api/notifications", Map.of(), null, StandardCharsets.UTF_8, null);
        Response response = Response.builder().status(503).reason("Service Unavailable").request(request).build();
        when(notificationClient.create(any())).thenThrow(FeignException.errorStatus("NotificationClient#create", response));

        // Should not throw — a failed alert must not fail the scheduled job itself.
        scheduler.dailyCheck();
    }
}
