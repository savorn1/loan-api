package com.example.customer.dto;

import com.example.customer.entity.CustomerStatus;
import com.example.customer.entity.CustomerType;
import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@ParameterObject
public class CustomerFilterRequest {

    // Matches against customerNo, firstName, lastName, fullName or email.
    private String search;
    private CustomerType customerType;
    private CustomerStatus status;
    private Long branchId;

    // Inclusive bounds on createdAt (by calendar day, not timestamp).
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateFrom;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateTo;

    private String sortBy = "createdAt";
    private String sortOrder = "desc";
    private int page = 1;
    private int size = 10;
}
