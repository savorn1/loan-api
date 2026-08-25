package com.example.payment.controller;

import com.example.payment.dto.PaymentWalletWebhookPayload;
import com.example.payment.service.PaymentTransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Locale;

// Receives async status callbacks from payment-wallet for deposits created by
// PaymentWalletClient. Deliberately not JWT-authenticated (this is a server-to-server
// call from payment-wallet, which holds no session token for this system) — the HMAC
// signature below IS the auth boundary, mirroring payment-wallet's own docs. This
// path must also be added to api-gateway's AuthenticationFilter.PUBLIC_PATHS, since
// the gateway would otherwise reject it for lacking an Authorization header before it
// ever reaches this controller.
@RestController
@RequestMapping("/api/payments/webhooks")
@RequiredArgsConstructor
@Slf4j
public class PaymentWalletWebhookController {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final PaymentTransactionService paymentTransactionService;
    private final ObjectMapper objectMapper;

    @Value("${payment-wallet.webhook-secret:}")
    private String webhookSecret;

    @PostMapping("/payment-wallet")
    public ResponseEntity<Void> receive(
            @RequestBody String rawBody,
            @RequestHeader(value = "X-Webhook-Signature", required = false) String signature) {
        if (!StringUtils.hasText(webhookSecret)) {
            log.error("Rejecting payment-wallet webhook: payment-wallet.webhook-secret is not configured");
            return ResponseEntity.status(500).build();
        }
        if (!StringUtils.hasText(signature) || !isValidSignature(rawBody, signature)) {
            log.warn("Rejecting payment-wallet webhook: missing or invalid X-Webhook-Signature");
            return ResponseEntity.status(401).build();
        }

        PaymentWalletWebhookPayload payload;
        try {
            payload = objectMapper.readValue(rawBody, PaymentWalletWebhookPayload.class);
        } catch (Exception e) {
            log.warn("Rejecting payment-wallet webhook: unparseable body", e);
            return ResponseEntity.badRequest().build();
        }

        if ("DEPOSIT".equals(payload.getEventType())) {
            paymentTransactionService.applyWalletDepositWebhook(
                    payload.getTransactionId(), payload.getStatus(), payload.getFailureReason());
        }
        return ResponseEntity.ok().build();
    }

    // Constant-time comparison against a hex HMAC-SHA256 of the exact raw request
    // body bytes — verifying a re-parsed/re-serialized JSON object here would silently
    // break on a legitimate delivery (whitespace/key-order can shift on reserialize).
    private boolean isValidSignature(String rawBody, String signature) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            String expectedHex = HexFormat.of().formatHex(mac.doFinal(rawBody.getBytes(StandardCharsets.UTF_8)));
            return MessageDigest.isEqual(
                    expectedHex.getBytes(StandardCharsets.UTF_8),
                    signature.trim().toLowerCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("Failed to verify payment-wallet webhook signature", e);
            return false;
        }
    }
}
