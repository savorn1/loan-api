package com.example.loan.dto;

import com.example.loan.entity.GroupStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GroupStatusUpdateRequest {

    @NotNull
    private GroupStatus status;
}
