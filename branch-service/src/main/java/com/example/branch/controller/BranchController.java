package com.example.branch.controller;

import com.example.branch.common.ApiResponse;
import com.example.branch.dto.BranchBusinessHoursRequest;
import com.example.branch.dto.BranchBusinessHoursResponse;
import com.example.branch.dto.BranchRequest;
import com.example.branch.dto.BranchResponse;
import com.example.branch.service.BranchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.util.List;

// Branch/office management. Reads are open to any authenticated user (staff/customer/
// loan forms across the platform need to populate a branch dropdown); writes are
// ADMIN-only. Unlike auth-service's RoleController/UserController, this can't gate on
// hasAuthority('BRANCH_*') — that RBAC permission data lives in auth-service's own DB
// and isn't part of the JWT, so hasRole('ADMIN') is the only signal available here.
@RestController
@RequestMapping("/api/branches")
@RequiredArgsConstructor
public class BranchController {

    private final BranchService branchService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<BranchResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success(branchService.listBranches()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BranchResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(branchService.getBranch(id)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<BranchResponse>> create(@Valid @RequestBody BranchRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Branch created", branchService.createBranch(request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BranchResponse>> update(@PathVariable Long id, @Valid @RequestBody BranchRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Branch updated", branchService.updateBranch(id, request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        branchService.deleteBranch(id);
        return ResponseEntity.ok(ApiResponse.success("Branch deleted", null));
    }

    // Weekly operating-hours schedule — at most one row per day of week, upserted
    // by day (PUT is idempotent, keyed on dayOfWeek) rather than exposed as an
    // open-ended add/remove list, since a branch always has exactly 7 possible slots.
    @GetMapping("/{id}/business-hours")
    public ResponseEntity<ApiResponse<List<BranchBusinessHoursResponse>>> getBusinessHours(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(branchService.getBusinessHours(id)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/business-hours/{dayOfWeek}")
    public ResponseEntity<ApiResponse<BranchBusinessHoursResponse>> upsertBusinessHours(
            @PathVariable Long id, @PathVariable DayOfWeek dayOfWeek,
            @Valid @RequestBody BranchBusinessHoursRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Business hours updated",
                branchService.upsertBusinessHours(id, dayOfWeek, request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}/business-hours/{dayOfWeek}")
    public ResponseEntity<ApiResponse<Void>> deleteBusinessHours(@PathVariable Long id, @PathVariable DayOfWeek dayOfWeek) {
        branchService.deleteBusinessHours(id, dayOfWeek);
        return ResponseEntity.ok(ApiResponse.success("Business hours removed", null));
    }
}
