package com.example.notification.service;

import com.example.notification.entity.Notification;

// Stands in for a real delivery integration (email/SMS/push provider). For now
// it only logs what would have been sent and always succeeds — there's no
// provider wired up, so every notification is recorded but nothing actually
// leaves this service. Swap this implementation out (without touching
// NotificationServiceImpl) once a real provider is chosen per channel.
public interface NotificationSender {

    // Attempts delivery and returns null on success, or an error message on
    // failure — never throws, so a bad send doesn't fail the create request.
    String send(Notification notification);
}
