package com.example.payment.entity;

import jakarta.persistence.*;
import lombok.*;

// Read-only audit trail — one row is appended by PaymentServiceImpl on every payment
// status transition (mirrors CollectionStatusHistory). No update/delete; createdAt
// (from BaseEntity) doubles as the changed-at timestamp.
@Entity
@Table(name = "payment_status_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentStatusHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status")
    private PaymentStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false)
    private PaymentStatus toStatus;

    @Column(nullable = false)
    private String changedBy;

    private String note;
}
