package com.example.customer.dto;

import com.example.customer.entity.ComplaintCategory;
import com.example.customer.entity.ComplaintStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CustomerComplaintRequest {

    @NotNull
    private ComplaintCategory category;

    @NotBlank
    private String description;

    // Optional on create (defaults to OPEN server-side); set on update to move
    // a complaint through its lifecycle.
    private ComplaintStatus status;

    private String resolutionNotes;
}
