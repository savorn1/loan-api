package com.example.branch.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchBusinessHoursResponse {

    private Long id;
    private Long branchId;
    private DayOfWeek dayOfWeek;
    private LocalTime openingTime;
    private LocalTime closingTime;
    // Boxed (not primitive) so Lombok emits getIsClosed() rather than the special
    // isClosed() boolean-getter form — Jackson strips a leading "is" from
    // is-prefixed getters, which would otherwise serialize this as "closed"
    // instead of "isClosed".
    private Boolean isClosed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
