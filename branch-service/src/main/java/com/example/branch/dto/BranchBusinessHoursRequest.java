package com.example.branch.dto;

import lombok.Data;

import java.time.LocalTime;

@Data
public class BranchBusinessHoursRequest {

    // Both required unless isClosed is true — enforced in BranchServiceImpl
    // rather than here, since the rule is conditional on isClosed.
    private LocalTime openingTime;
    private LocalTime closingTime;

    // Defaults to false in BranchServiceImpl when omitted. Boxed (not primitive)
    // so Jackson reads/writes the "isClosed" JSON key as-is — see the same note
    // on BranchBusinessHoursResponse.
    private Boolean isClosed;
}
