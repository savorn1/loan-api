package com.example.loan.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CustomerRequest {

    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String nationalId;
    private String address;
    private LocalDate dateOfBirth;
}
