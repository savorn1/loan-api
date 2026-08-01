package com.example.branch.dto;

import com.example.branch.entity.BranchStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BranchRequest {

    @NotBlank
    private String code;

    @NotBlank
    private String name;

    private String address;

    private String phone;

    @NotNull
    private BranchStatus status = BranchStatus.ACTIVE;
}
