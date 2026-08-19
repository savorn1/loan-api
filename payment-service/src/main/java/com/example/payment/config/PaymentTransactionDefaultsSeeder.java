package com.example.payment.config;

import com.example.payment.entity.PaymentChannel;
import com.example.payment.entity.PaymentChannelStatus;
import com.example.payment.entity.PaymentGateway;
import com.example.payment.entity.PaymentGatewayStatus;
import com.example.payment.entity.PaymentMethod;
import com.example.payment.entity.PaymentMethodStatus;
import com.example.payment.entity.PaymentMethodType;
import com.example.payment.repository.PaymentChannelRepository;
import com.example.payment.repository.PaymentGatewayRepository;
import com.example.payment.repository.PaymentMethodRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// Every PaymentTransaction requires a method/channel/gateway (all NOT NULL FKs), but a
// loan repayment recorded internally (markAsPaid, or a payment recorded directly on a
// loan) never went through a real gateway. These three rows are the fixed "no real
// gateway" identity that PaymentTransactionServiceImpl.createForLoanRepayment() looks
// up by code — same idempotent-seed pattern as accounting-service's GlAccountSeeder.
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentTransactionDefaultsSeeder implements CommandLineRunner {

    public static final String INTERNAL_METHOD_CODE = "INTERNAL";
    public static final String INTERNAL_CHANNEL_CODE = "LOAN_REPAYMENT";
    public static final String INTERNAL_GATEWAY_CODE = "INTERNAL";

    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentChannelRepository paymentChannelRepository;
    private final PaymentGatewayRepository paymentGatewayRepository;

    @Value("${seed.payment-transaction-defaults.enabled:true}")
    private boolean enabled;

    @Override
    public void run(String... args) {
        if (!enabled) {
            return;
        }
        if (!paymentMethodRepository.existsByCode(INTERNAL_METHOD_CODE)) {
            paymentMethodRepository.save(PaymentMethod.builder()
                    .code(INTERNAL_METHOD_CODE)
                    .name("Internal / Manual")
                    .type(PaymentMethodType.CASH)
                    .status(PaymentMethodStatus.ACTIVE)
                    .build());
            log.info("Seeded default payment method {}", INTERNAL_METHOD_CODE);
        }
        if (!paymentChannelRepository.existsByCode(INTERNAL_CHANNEL_CODE)) {
            paymentChannelRepository.save(PaymentChannel.builder()
                    .code(INTERNAL_CHANNEL_CODE)
                    .name("Loan Repayment (Internal)")
                    .channelType("INTERNAL")
                    .status(PaymentChannelStatus.ACTIVE)
                    .build());
            log.info("Seeded default payment channel {}", INTERNAL_CHANNEL_CODE);
        }
        if (!paymentGatewayRepository.existsByCode(INTERNAL_GATEWAY_CODE)) {
            paymentGatewayRepository.save(PaymentGateway.builder()
                    .code(INTERNAL_GATEWAY_CODE)
                    .name("Internal (No Gateway)")
                    .provider("internal")
                    .apiUrl("n/a")
                    .status(PaymentGatewayStatus.ACTIVE)
                    .build());
            log.info("Seeded default payment gateway {}", INTERNAL_GATEWAY_CODE);
        }
    }
}
