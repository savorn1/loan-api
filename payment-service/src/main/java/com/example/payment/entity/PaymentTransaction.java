package com.example.payment.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// The customer lives in customer-service, so customerId stays a plain foreign
// key (resolved via CustomerClient); method/channel/gateway are local lookup
// entities in this service, so they're real JPA relations.
@Entity
@Table(name = "payment_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Assigned after the initial save, once the id is known (same two-phase-save
    // pattern as customer-service's Customer.customerNo / accounting-service's
    // JournalEntry.entryNo).
    @Column(name = "payment_no", unique = true, length = 20)
    private String paymentNo;

    @Column(name = "reference_no", length = 100)
    private String referenceNo;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_method_id", nullable = false)
    private PaymentMethod paymentMethod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_channel_id", nullable = false)
    private PaymentChannel paymentChannel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_gateway_id", nullable = false)
    private PaymentGateway paymentGateway;

    @Column(name = "business_type", nullable = false, length = 50)
    private String businessType;

    @Column(name = "business_reference", length = 100)
    private String businessReference;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
