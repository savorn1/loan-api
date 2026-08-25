package com.example.payment.client;

import com.example.payment.dto.PaymentWalletApiEnvelope;
import com.example.payment.dto.PaymentWalletDepositRequest;
import com.example.payment.dto.PaymentWalletDepositResponse;
import com.example.payment.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;

// Talks to payment-wallet's merchant API (see PaymentGateway.provider ==
// "PAYMENT_WALLET"). Unlike the internal Feign clients (CustomerClient, LoanClient,
// ...), payment-wallet is an external service not registered in this monorepo's
// Eureka registry, so it's called with a plain RestClient against the gateway row's
// own apiUrl instead of an @FeignClient service name. The X-API-Key credential is
// shared across every PAYMENT_WALLET gateway row (one merchant identity) and is
// never persisted on PaymentGateway itself, since that entity is readable via
// GET /api/payments/gateways by any authenticated user.
@Component
@RequiredArgsConstructor
public class PaymentWalletClient {

    private final RestClient.Builder restClientBuilder;

    @Value("${payment-wallet.api-key:}")
    private String apiKey;

    public PaymentWalletDepositResponse createDeposit(String baseUrl, BigDecimal amount, String idempotencyKey) {
        if (!StringUtils.hasText(apiKey)) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "payment-wallet.api-key is not configured");
        }
        try {
            PaymentWalletApiEnvelope<PaymentWalletDepositResponse> envelope = restClientBuilder.build()
                    .post()
                    .uri(baseUrl + "/deposits")
                    .header("X-API-Key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new PaymentWalletDepositRequest(amount, idempotencyKey))
                    .retrieve()
                    .body(new ParameterizedTypeReference<PaymentWalletApiEnvelope<PaymentWalletDepositResponse>>() { });
            if (envelope == null || envelope.getData() == null) {
                throw new AppException(HttpStatus.BAD_GATEWAY, "payment-wallet returned an empty deposit response");
            }
            return envelope.getData();
        } catch (RestClientResponseException e) {
            throw new AppException(HttpStatus.BAD_GATEWAY,
                    "payment-wallet deposit request failed (" + e.getStatusCode() + ")");
        } catch (RestClientException e) {
            throw new AppException(HttpStatus.BAD_GATEWAY, "payment-wallet is unreachable: " + e.getMessage());
        }
    }

    // Used by PaymentWalletReconciliationScheduler as a fallback for deposits whose
    // webhook delivery never arrived — payment-wallet doesn't retry failed deliveries,
    // so this is the only way to eventually learn a stuck PENDING deposit's real status.
    public PaymentWalletDepositResponse getDeposit(String baseUrl, Long walletDepositId) {
        if (!StringUtils.hasText(apiKey)) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "payment-wallet.api-key is not configured");
        }
        try {
            PaymentWalletApiEnvelope<PaymentWalletDepositResponse> envelope = restClientBuilder.build()
                    .get()
                    .uri(baseUrl + "/deposits/{id}", walletDepositId)
                    .header("X-API-Key", apiKey)
                    .retrieve()
                    .body(new ParameterizedTypeReference<PaymentWalletApiEnvelope<PaymentWalletDepositResponse>>() { });
            if (envelope == null || envelope.getData() == null) {
                throw new AppException(HttpStatus.BAD_GATEWAY, "payment-wallet returned an empty deposit response");
            }
            return envelope.getData();
        } catch (RestClientResponseException e) {
            throw new AppException(HttpStatus.BAD_GATEWAY,
                    "payment-wallet deposit lookup failed (" + e.getStatusCode() + ")");
        } catch (RestClientException e) {
            throw new AppException(HttpStatus.BAD_GATEWAY, "payment-wallet is unreachable: " + e.getMessage());
        }
    }
}
