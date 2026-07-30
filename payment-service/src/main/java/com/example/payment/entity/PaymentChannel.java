package com.example.payment.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "payment_channels")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentChannel extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    // Free-text (e.g. "WEB", "MOBILE_APP", "BRANCH", "USSD") rather than an enum —
    // channels are operator-defined and open-ended, unlike PaymentMethod's fixed set.
    @Column(name = "channel_type", nullable = false, length = 50)
    private String channelType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PaymentChannelStatus status = PaymentChannelStatus.ACTIVE;
}
