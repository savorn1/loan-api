package com.example.accounting.common;

import java.util.UUID;

public class ApiResponse<T> {

    private String traceId;
    private int statusCode;
    private String message;
    private T data;

    public ApiResponse() { this.traceId = UUID.randomUUID().toString(); }

    public ApiResponse(int statusCode, String message, T data) {
        this.traceId = UUID.randomUUID().toString();
        this.statusCode = statusCode;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(T data) { return new ApiResponse<>(200, "success", data); }
    public static <T> ApiResponse<T> success(String message, T data) { return new ApiResponse<>(200, message, data); }
    public static <T> ApiResponse<T> error(int statusCode, String message) { return new ApiResponse<>(statusCode, message, null); }

    public String getTraceId() { return traceId; }
    public void setTraceId(String v) { this.traceId = v; }
    public int getStatusCode() { return statusCode; }
    public void setStatusCode(int v) { this.statusCode = v; }
    public String getMessage() { return message; }
    public void setMessage(String v) { this.message = v; }
    public T getData() { return data; }
    public void setData(T v) { this.data = v; }
}
