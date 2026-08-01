package com.example.branch.service;

import com.example.branch.dto.BranchBusinessHoursRequest;
import com.example.branch.dto.BranchBusinessHoursResponse;
import com.example.branch.dto.BranchRequest;
import com.example.branch.dto.BranchResponse;

import java.time.DayOfWeek;
import java.util.List;

public interface BranchService {

    List<BranchResponse> listBranches();

    BranchResponse getBranch(Long id);

    BranchResponse createBranch(BranchRequest request);

    BranchResponse updateBranch(Long id, BranchRequest request);

    void deleteBranch(Long id);

    List<BranchBusinessHoursResponse> getBusinessHours(Long branchId);

    BranchBusinessHoursResponse upsertBusinessHours(Long branchId, DayOfWeek dayOfWeek, BranchBusinessHoursRequest request);

    void deleteBusinessHours(Long branchId, DayOfWeek dayOfWeek);
}
