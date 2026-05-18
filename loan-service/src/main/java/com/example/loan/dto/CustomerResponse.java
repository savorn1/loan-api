package com.example.loan.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CustomerResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String nationalId;
    private LocalDate dateOfBirth;
}
