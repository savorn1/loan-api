package com.example.loan.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddGroupMemberRequest {

    @NotNull
    private Long customerId;
}
