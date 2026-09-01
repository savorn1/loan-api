package com.example.customer.dto;

import com.example.customer.entity.ComplaintCategory;
import com.example.customer.entity.ComplaintStatus;
import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@ParameterObject
public class ComplaintFilterRequest {

    private Long customerId;
    private ComplaintStatus status;
    private ComplaintCategory category;

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
