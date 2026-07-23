package com.example.loanproduct.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends AppException {

    // Object id (not Long) — LoanProduct's own id is a UUID while its child
    // resources (rates/fees/terms/rules/documents) use Long ids.
    public ResourceNotFoundException(String resource, Object id) {
        super(HttpStatus.NOT_FOUND, resource + " not found with id: " + id);
    }
}
