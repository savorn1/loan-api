package com.example.customer.dto;

import com.example.customer.entity.NotificationMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CustomerPreferenceRequest {

    private String language;
    private String currency;

    @NotNull
    private NotificationMethod notificationMethod;
}
