package com.example.auth.dto;

import com.example.auth.entity.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateStatusRequest {

    @NotNull
    private UserStatus status;
}
