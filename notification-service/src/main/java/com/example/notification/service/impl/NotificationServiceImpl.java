package com.example.notification.service.impl;

import com.example.notification.common.PageResponse;
import com.example.notification.dto.NotificationFilterRequest;
import com.example.notification.dto.NotificationRequest;
import com.example.notification.dto.NotificationResponse;
import com.example.notification.entity.Notification;
import com.example.notification.entity.NotificationStatus;
import com.example.notification.exception.ResourceNotFoundException;
import com.example.notification.repository.NotificationRepository;
import com.example.notification.service.NotificationSender;
import com.example.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationSender notificationSender;

    // Creates the record and attempts delivery synchronously in the same
    // request — there's no queue/worker in this service, so "send" happens
    // inline via NotificationSender before the response goes back.
    @Override
    public NotificationResponse create(NotificationRequest request) {
        Notification notification = Notification.builder()
                .recipientType(request.getRecipientType())
                .recipientId(request.getRecipientId())
                .channel(request.getChannel())
                .subject(request.getSubject())
                .message(request.getMessage())
                .status(NotificationStatus.PENDING)
                .build();

        String error = notificationSender.send(notification);
        if (error == null) {
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
        } else {
            notification.setStatus(NotificationStatus.FAILED);
            notification.setErrorMessage(error);
        }

        return toResponse(notificationRepository.save(notification));
    }

    @Override
    public NotificationResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    public PageResponse<NotificationResponse> list(NotificationFilterRequest filter) {
        List<Specification<Notification>> conditions = new ArrayList<>();

        if (filter.getRecipientType() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("recipientType"), filter.getRecipientType()));
        }
        if (filter.getRecipientId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("recipientId"), filter.getRecipientId()));
        }
        if (filter.getChannel() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("channel"), filter.getChannel()));
        }
        if (filter.getStatus() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("status"), filter.getStatus()));
        }

        Specification<Notification> spec = Specification.allOf(conditions);

        Sort sort = "asc".equalsIgnoreCase(filter.getSortOrder())
                ? Sort.by(filter.getSortBy()).ascending()
                : Sort.by(filter.getSortBy()).descending();
        Pageable pageable = PageRequest.of(Math.max(filter.getPage() - 1, 0), filter.getSize(), sort);

        Page<NotificationResponse> page = notificationRepository.findAll(spec, pageable).map(this::toResponse);
        return PageResponse.of(page);
    }

    private Notification findOrThrow(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", id));
    }

    private NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .recipientType(notification.getRecipientType())
                .recipientId(notification.getRecipientId())
                .channel(notification.getChannel())
                .subject(notification.getSubject())
                .message(notification.getMessage())
                .status(notification.getStatus())
                .sentAt(notification.getSentAt())
                .errorMessage(notification.getErrorMessage())
                .createdAt(notification.getCreatedAt())
                .updatedAt(notification.getUpdatedAt())
                .build();
    }
}
