package com.example.customer.scheduler;

import com.example.customer.client.NotificationClient;
import com.example.customer.common.ApiResponse;
import com.example.customer.dto.NotificationChannel;
import com.example.customer.dto.NotificationRequest;
import com.example.customer.dto.NotificationResponse;
import com.example.customer.entity.Customer;
import com.example.customer.entity.CustomerIdentity;
import com.example.customer.entity.IdentityStatus;
import com.example.customer.entity.IdentityType;
import com.example.customer.repository.CustomerIdentityRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdentityExpirySchedulerTest {

    @Mock private CustomerIdentityRepository customerIdentityRepository;
    @Mock private NotificationClient notificationClient;

    @Test
    void flagExpiredIdentities_flipsStatusAndNotifiesOnEveryChannelOnFile() {
        Customer customer = Customer.builder().id(4L).email("dara@example.com").phone("+855123456789").build();
        CustomerIdentity identity = CustomerIdentity.builder()
                .customer(customer).identityType(IdentityType.NATIONAL_ID).identityNumber("ID123")
                .expiryDate(LocalDate.now().minusDays(1)).status(IdentityStatus.ACTIVE).build();
        identity.setId(1L);

        when(customerIdentityRepository.findByStatusAndExpiryDateBefore(IdentityStatus.ACTIVE, LocalDate.now()))
                .thenReturn(List.of(identity));
        when(notificationClient.create(any(NotificationRequest.class)))
                .thenReturn(ApiResponse.success(new NotificationResponse()));

        new IdentityExpiryScheduler(customerIdentityRepository, notificationClient).flagExpiredIdentities();

        assertThat(identity.getStatus()).isEqualTo(IdentityStatus.EXPIRED);
        verify(customerIdentityRepository).saveAll(List.of(identity));

        ArgumentCaptor<NotificationRequest> captor = ArgumentCaptor.forClass(NotificationRequest.class);
        verify(notificationClient, times(2)).create(captor.capture());
        List<NotificationRequest> requests = captor.getAllValues();
        assertThat(requests).anySatisfy(r -> {
            assertThat(r.getChannel()).isEqualTo(NotificationChannel.EMAIL);
            assertThat(r.getRecipientContact()).isEqualTo("dara@example.com");
        });
        assertThat(requests).anySatisfy(r -> {
            assertThat(r.getChannel()).isEqualTo(NotificationChannel.SMS);
            assertThat(r.getRecipientContact()).isEqualTo("+855123456789");
        });
    }

    @Test
    void flagExpiredIdentities_doesNothingWhenNoIdentitiesHaveExpired() {
        when(customerIdentityRepository.findByStatusAndExpiryDateBefore(IdentityStatus.ACTIVE, LocalDate.now()))
                .thenReturn(List.of());

        new IdentityExpiryScheduler(customerIdentityRepository, notificationClient).flagExpiredIdentities();

        verify(notificationClient, times(0)).create(any());
    }
}
