package com.example.customer.dto;

import com.example.customer.entity.RelationshipType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CustomerRelationshipResponse {

    private Long id;
    private Long customerId;
    private Long relatedCustomerId;
    // Denormalized at read time from the related Customer — saves the frontend
    // an extra lookup just to show who a relationship points to.
    private String relatedCustomerName;
    private RelationshipType relationshipType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
