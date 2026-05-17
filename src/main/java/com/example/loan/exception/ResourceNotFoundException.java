package com.example.loan.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends AppException {
    public ResourceNotFoundException(String resource, Long id) {
        super(HttpStatus.NOT_FOUND, resource + " not found with id: " + id);
    }

    public ResourceNotFoundException(String resource, String name) {
        super(HttpStatus.NOT_FOUND, resource + " not found with name: " + name);
    }
}
