package com.example.auth.client;

import com.example.auth.dto.ApiResponse;
import com.example.auth.dto.BranchResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "branch-service")
public interface BranchClient {

    // branch-service wraps every payload in ApiResponse ({traceId, statusCode,
    // message, data}) — the return type has to mirror that envelope so Feign
    // deserializes into `data` instead of silently leaving every field null.
    @GetMapping("/api/branches/{id}")
    ApiResponse<BranchResponse> getById(@PathVariable Long id);
}
