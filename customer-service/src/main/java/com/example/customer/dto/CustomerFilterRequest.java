package com.example.customer.dto;

import com.example.customer.entity.CustomerStatus;
import com.example.customer.entity.CustomerType;
import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

@Data
@ParameterObject
public class CustomerFilterRequest {

    // Matches against customerNo, firstName, lastName, fullName or email.
    private String search;
    private CustomerType customerType;
    private CustomerStatus status;
    private Long branchId;

    private String sortBy = "createdAt";
    private String sortOrder = "desc";
    private int page = 1;
    private int size = 10;
}
