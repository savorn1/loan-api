package com.example.auth.dto;

import com.example.auth.entity.Role;
import com.example.auth.entity.UserStatus;
import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

import java.time.LocalDate;

@Data
@ParameterObject
public class UserFilterRequest {

    private String username;
    private Role role;
    private UserStatus status;
    private LocalDate startDate;
    private LocalDate endDate;

    private String sortBy = "createdAt";
    private String sortOrder = "desc";
    private int page = 1;
    private int size = 10;
}
