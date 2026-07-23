package com.example.loanproduct.controller;

import com.example.loanproduct.common.ApiResponse;
import com.example.loanproduct.dto.RuleTemplateRequest;
import com.example.loanproduct.dto.RuleTemplateResponse;
import com.example.loanproduct.service.RuleTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/rule-templates")
@RequiredArgsConstructor
public class RuleTemplateController {

    private final RuleTemplateService ruleTemplateService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<RuleTemplateResponse>> create(@Valid @RequestBody RuleTemplateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Rule template created", ruleTemplateService.create(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RuleTemplateResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(ruleTemplateService.getById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RuleTemplateResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(ruleTemplateService.getAll()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RuleTemplateResponse>> update(
            @PathVariable UUID id, @Valid @RequestBody RuleTemplateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Rule template updated", ruleTemplateService.update(id, request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        ruleTemplateService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Rule template deleted", null));
    }
}
