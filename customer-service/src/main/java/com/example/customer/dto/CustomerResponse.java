package com.example.customer.dto;

import com.example.customer.entity.CustomerStatus;
import com.example.customer.entity.CustomerType;
import com.example.customer.entity.Gender;
import com.example.customer.entity.MaritalStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class CustomerResponse {

    private Long id;
    private String customerNo;
    private CustomerType customerType;
    private String firstName;
    private String lastName;
    private String fullName;
    private Gender gender;
    private String email;
    private String phone;
    private String nationalId;
    private String address;
    private LocalDate dateOfBirth;
    private String nationality;
    private MaritalStatus maritalStatus;
    private CustomerStatus status;
    private Long branchId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
