package com.example.customer.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends AppException {

    public ResourceNotFoundException(String resource, Long id) {
        super(HttpStatus.NOT_FOUND, resource + " not found with id: " + id);
    }

    public ResourceNotFoundException(String resource, String field) {
        super(HttpStatus.NOT_FOUND, resource + " not found: " + field);
    }
}
