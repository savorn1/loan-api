package com.example.auth.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean anonymous = auth == null || auth instanceof AnonymousAuthenticationToken;
        HttpStatus status = anonymous ? HttpStatus.UNAUTHORIZED : HttpStatus.FORBIDDEN;
        log.warn("status={} message=Access denied", status.value());
        return ResponseEntity.status(status).body(Map.of(
                "statusCode", status.value(),
                "message", anonymous ? "Authentication required" : "Access denied"));
    }

    @ExceptionHandler(AppException.class)
    ResponseEntity<Map<String, Object>> handleAppException(AppException ex) {
        log.warn("status={} message={}", ex.getStatus().value(), ex.getMessage());
        return ResponseEntity.status(ex.getStatus())
                .body(Map.of("statusCode", ex.getStatus().value(), "message", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (a, b) -> a));
        return ResponseEntity.badRequest()
                .body(Map.of("statusCode", 400, "message", "Validation failed", "errors", errors));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<Map<String, Object>> handleNoResource(NoResourceFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("statusCode", 404, "message", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("statusCode", 500, "message", "Internal server error"));
    }
}
