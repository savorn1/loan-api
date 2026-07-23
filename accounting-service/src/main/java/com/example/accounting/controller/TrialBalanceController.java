package com.example.accounting.controller;

import com.example.accounting.common.ApiResponse;
import com.example.accounting.dto.TrialBalanceRowResponse;
import com.example.accounting.service.TrialBalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/trial-balance")
@RequiredArgsConstructor
public class TrialBalanceController {

    private final TrialBalanceService trialBalanceService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TrialBalanceRowResponse>>> getTrialBalance(
            @RequestParam Long financialPeriodId) {
        return ResponseEntity.ok(ApiResponse.success(trialBalanceService.getTrialBalance(financialPeriodId)));
    }
}
