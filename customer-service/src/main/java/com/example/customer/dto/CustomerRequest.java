package com.example.customer.dto;

import com.example.customer.entity.CustomerStatus;
import com.example.customer.entity.CustomerType;
import com.example.customer.entity.Gender;
import com.example.customer.entity.MaritalStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CustomerRequest {

    @NotNull
    private CustomerType customerType;

    // Required when customerType is INDIVIDUAL — enforced in CustomerServiceImpl
    // rather than here, since the rule is conditional on customerType.
    private String firstName;
    private String lastName;

    // Required when customerType is CORPORATE (the company/legal name); derived
    // from firstName/lastName for INDIVIDUAL and ignored if supplied for one.
    private String fullName;

    private Gender gender;

    @NotBlank
    @Email
    private String email;

    private String phone;
    private String nationalId;
    private String address;
    private LocalDate dateOfBirth;
    private String nationality;
    private MaritalStatus maritalStatus;

    // Defaults to ACTIVE in CustomerServiceImpl when omitted.
    private CustomerStatus status;

    private Long branchId;
}
