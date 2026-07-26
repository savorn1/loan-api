package com.example.notification.controller;

import com.example.notification.common.ApiResponse;
import com.example.notification.common.PageResponse;
import com.example.notification.dto.NotificationFilterRequest;
import com.example.notification.dto.NotificationRequest;
import com.example.notification.dto.NotificationResponse;
import com.example.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // Meant to be called by other backend services (e.g. loan-service when a
    // loan is approved, payment-service when a payment is due) — creates the
    // record and attempts delivery inline. No auth restriction beyond a valid
    // JWT; there's no service-to-service auth concept in this stack yet.
    @PostMapping
    public ResponseEntity<ApiResponse<NotificationResponse>> create(@Valid @RequestBody NotificationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Notification created", notificationService.create(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NotificationResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(notificationService.getById(id)));
    }

    @GetMapping
    public ResponseEntity<PageResponse<NotificationResponse>> getAll(@ModelAttribute NotificationFilterRequest filter) {
        return ResponseEntity.ok(notificationService.list(filter));
    }
}
