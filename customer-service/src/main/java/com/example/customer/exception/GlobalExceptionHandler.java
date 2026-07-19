package com.example.customer.exception;

import com.example.customer.common.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
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
    ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean anonymous = auth == null || auth instanceof AnonymousAuthenticationToken;
        int code = anonymous ? 401 : 403;
        ApiResponse<Void> response = ApiResponse.error(code, anonymous ? "Authentication required" : "Access denied");
        log.warn("traceId={} status={} message={}", response.getTraceId(), code, response.getMessage());
        return ResponseEntity.status(code).body(response);
    }

    @ExceptionHandler(AppException.class)
    ResponseEntity<ApiResponse<Void>> handleAppException(AppException ex) {
        int code = ex.getStatus().value();
        ApiResponse<Void> response = ApiResponse.error(code, ex.getMessage());
        log.warn("traceId={} status={} message={}", response.getTraceId(), code, ex.getMessage());
        return ResponseEntity.status(code).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ApiResponse<Void>> handleBadRequest(IllegalArgumentException ex) {
        ApiResponse<Void> response = ApiResponse.error(400, ex.getMessage());
        log.warn("traceId={} status=400 message={}", response.getTraceId(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (a, b) -> a));
        ApiResponse<Map<String, String>> response = new ApiResponse<>(400, "Validation failed", errors);
        log.warn("traceId={} status=400 validation={}", response.getTraceId(), errors);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiResponse<Void>> handleDataIntegrity(DataIntegrityViolationException ex) {
        ApiResponse<Void> response = ApiResponse.error(409, "Data conflict: resource already exists or constraint violated");
        log.warn("traceId={} status=409 message={}", response.getTraceId(), ex.getMostSpecificCause().getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<ApiResponse<Void>> handleNoResource(NoResourceFoundException ex) {
        ApiResponse<Void> response = ApiResponse.error(404, ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiResponse<Void>> handleGeneral(Exception ex) {
        ApiResponse<Void> response = ApiResponse.error(500, "Internal server error");
        log.error("traceId={} status=500 message={}", response.getTraceId(), ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
