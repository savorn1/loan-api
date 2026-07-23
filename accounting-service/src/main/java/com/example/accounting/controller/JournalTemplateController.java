package com.example.accounting.controller;

import com.example.accounting.common.ApiResponse;
import com.example.accounting.dto.JournalTemplateRequest;
import com.example.accounting.dto.JournalTemplateResponse;
import com.example.accounting.service.JournalTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/journal-templates")
@RequiredArgsConstructor
public class JournalTemplateController {

    private final JournalTemplateService journalTemplateService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<JournalTemplateResponse>> create(@Valid @RequestBody JournalTemplateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Journal template created", journalTemplateService.create(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<JournalTemplateResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(journalTemplateService.getById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<JournalTemplateResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(journalTemplateService.getAll()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<JournalTemplateResponse>> update(
            @PathVariable Long id, @Valid @RequestBody JournalTemplateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Journal template updated", journalTemplateService.update(id, request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        journalTemplateService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Journal template deleted", null));
    }
}
