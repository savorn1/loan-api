package com.example.customer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CustomerContactRequest {

    @NotBlank
    private String name;

    private String relationship;
    private String phone;

    @Email
    private String email;

    private String address;
}
