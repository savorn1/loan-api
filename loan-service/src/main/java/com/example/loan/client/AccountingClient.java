package com.example.loan.client;

import com.example.loan.common.ApiResponse;
import com.example.loan.dto.JournalEntryGenerateRequest;
import com.example.loan.dto.JournalEntryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "accounting-service")
public interface AccountingClient {

    // accounting-service wraps every payload in ApiResponse ({traceId, statusCode,
    // message, data}) just like the other services — see LoanProductClient's comment
    // for what happens when a Feign client's return type doesn't mirror that envelope.
    @PostMapping("/api/journal-entries/generate")
    ApiResponse<JournalEntryResponse> generate(@RequestBody JournalEntryGenerateRequest request);

    // Hits /system-reverse, not the user-facing /{id}/reverse — that one is
    // @PreAuthorize("hasRole('ADMIN')") and would reject this unauthenticated Feign call.
    @PostMapping("/api/journal-entries/{id}/system-reverse")
    ApiResponse<JournalEntryResponse> reverse(@PathVariable Long id);
}
