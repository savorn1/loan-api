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
    // RbacRole.name (e.g. "LOAN_OFFICER") — the additive RBAC layer, distinct from
    // the `role` enum field above. Lets a caller fetch e.g. every loan officer for
    // an assignment dropdown without a dedicated endpoint per role.
    private String roleName;
    private UserStatus status;
    private LocalDate startDate;
    private LocalDate endDate;

    private String sortBy = "createdAt";
    private String sortOrder = "desc";
    private int page = 1;
    private int size = 10;
}
