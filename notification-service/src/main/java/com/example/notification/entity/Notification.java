package com.example.notification.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecipientType recipientType;

    // Opaque cross-service id — a customer-service customer id or an
    // auth-service user id, depending on recipientType. No FK/join.
    @Column(nullable = false)
    private Long recipientId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationChannel channel;

    // The actual delivery address (email/phone) for a real sender to send to.
    // Notification deliberately has no FK to customer-service/auth-service (see
    // recipientId above), so the caller — who already has this from its own
    // lookup — passes it in directly instead of this service resolving it itself.
    private String recipientContact;

    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private NotificationStatus status = NotificationStatus.PENDING;

    private LocalDateTime sentAt;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;
}
