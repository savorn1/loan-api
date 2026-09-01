package com.example.customer.dto;

import com.example.customer.entity.ComplaintCategory;
import com.example.customer.entity.ComplaintStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CustomerComplaintResponse {

    private Long id;
    private Long customerId;
    private String customerName;
    private ComplaintCategory category;
    private String description;
    private ComplaintStatus status;
    private String resolutionNotes;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
