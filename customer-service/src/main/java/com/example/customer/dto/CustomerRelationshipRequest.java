package com.example.customer.dto;

import com.example.customer.entity.RelationshipType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CustomerRelationshipRequest {

    @NotNull
    private Long relatedCustomerId;

    @NotNull
    private RelationshipType relationshipType;
}
