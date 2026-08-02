package com.example.loan.dto;

import com.example.loan.entity.GroupStatus;
import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

@Data
@ParameterObject
public class GroupFilterRequest {

    // Matches against name or code.
    private String search;
    private GroupStatus status;
    private Long branchId;

    private String sortBy = "createdAt";
    private String sortOrder = "desc";
    private int page = 1;
    private int size = 10;
}
