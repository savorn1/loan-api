package com.example.loan.client;

import com.example.loan.common.ApiResponse;
import com.example.loan.dto.CustomerEmploymentResponse;
import com.example.loan.dto.CustomerIdentityResponse;
import com.example.loan.dto.CustomerIncomeResponse;
import com.example.loan.dto.CustomerResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "customer-service")
public interface CustomerClient {

    // customer-service wraps every payload in ApiResponse ({traceId, statusCode,
    // message, data}) — the return type has to mirror that envelope so Feign
    // deserializes into `data` instead of silently leaving every field null.
    @GetMapping("/api/customers/{id}")
    ApiResponse<CustomerResponse> getById(@PathVariable Long id);

    // Used by GroupServiceImpl to derive GroupMemberResponse.kycStatus (VERIFIED
    // if the customer has >=1 ACTIVE identity on file).
    @GetMapping("/api/customers/{id}/identities")
    ApiResponse<List<CustomerIdentityResponse>> getIdentities(@PathVariable Long id);

    // Used by EligibilityServiceImpl for EMPLOYMENT_STATUS rules.
    @GetMapping("/api/customers/{id}/employments")
    ApiResponse<List<CustomerEmploymentResponse>> getEmployments(@PathVariable Long id);

    // Used by EligibilityServiceImpl for MONTHLY_INCOME rules.
    @GetMapping("/api/customers/{id}/incomes")
    ApiResponse<List<CustomerIncomeResponse>> getIncomes(@PathVariable Long id);
}
