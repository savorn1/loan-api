package com.example.auth.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateStatusRequest {

    @NotNull
    private Boolean active;
}
