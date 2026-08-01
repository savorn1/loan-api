package com.example.branch.service.impl;

import com.example.branch.dto.BranchBusinessHoursRequest;
import com.example.branch.dto.BranchBusinessHoursResponse;
import com.example.branch.dto.BranchRequest;
import com.example.branch.dto.BranchResponse;
import com.example.branch.entity.Branch;
import com.example.branch.entity.BranchBusinessHours;
import com.example.branch.exception.AppException;
import com.example.branch.repository.BranchBusinessHoursRepository;
import com.example.branch.repository.BranchRepository;
import com.example.branch.service.BranchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepository;
    private final BranchBusinessHoursRepository businessHoursRepository;

    @Override
    public List<BranchResponse> listBranches() {
        return branchRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public BranchResponse getBranch(Long id) {
        return toResponse(findBranch(id));
    }

    @Override
    @Transactional
    public BranchResponse createBranch(BranchRequest request) {
        if (branchRepository.existsByCode(request.getCode())) {
            throw new AppException(HttpStatus.CONFLICT, "Branch code already taken: " + request.getCode());
        }
        Branch branch = Branch.builder()
                .code(request.getCode())
                .name(request.getName())
                .address(request.getAddress())
                .phone(request.getPhone())
                .status(request.getStatus())
                .build();
        return toResponse(branchRepository.save(branch));
    }

    @Override
    @Transactional
    public BranchResponse updateBranch(Long id, BranchRequest request) {
        Branch branch = findBranch(id);
        if (!branch.getCode().equals(request.getCode()) && branchRepository.existsByCode(request.getCode())) {
            throw new AppException(HttpStatus.CONFLICT, "Branch code already taken: " + request.getCode());
        }
        branch.setCode(request.getCode());
        branch.setName(request.getName());
        branch.setAddress(request.getAddress());
        branch.setPhone(request.getPhone());
        branch.setStatus(request.getStatus());
        return toResponse(branchRepository.save(branch));
    }

    @Override
    @Transactional
    public void deleteBranch(Long id) {
        branchRepository.delete(findBranch(id));
    }

    @Override
    public List<BranchBusinessHoursResponse> getBusinessHours(Long branchId) {
        findBranch(branchId);
        return businessHoursRepository.findByBranch_Id(branchId).stream()
                // DayOfWeek's natural ordering follows its declaration order (MONDAY..SUNDAY,
                // ISO-8601 week order) — sorting here instead of via a DB ORDER BY avoids the
                // common EnumType.STRING gotcha where the column would otherwise sort
                // alphabetically (FRIDAY, MONDAY, SATURDAY, ...).
                .sorted(Comparator.comparing(BranchBusinessHours::getDayOfWeek))
                .map(this::toBusinessHoursResponse)
                .toList();
    }

    @Override
    @Transactional
    public BranchBusinessHoursResponse upsertBusinessHours(Long branchId, DayOfWeek dayOfWeek, BranchBusinessHoursRequest request) {
        Branch branch = findBranch(branchId);
        boolean closed = Boolean.TRUE.equals(request.getIsClosed());
        if (!closed) {
            if (request.getOpeningTime() == null || request.getClosingTime() == null) {
                throw new AppException(HttpStatus.BAD_REQUEST,
                        "openingTime and closingTime are required unless isClosed is true");
            }
            if (!request.getOpeningTime().isBefore(request.getClosingTime())) {
                throw new AppException(HttpStatus.BAD_REQUEST, "openingTime must be before closingTime");
            }
        }

        BranchBusinessHours hours = businessHoursRepository.findByBranch_IdAndDayOfWeek(branchId, dayOfWeek)
                .orElseGet(() -> BranchBusinessHours.builder().branch(branch).dayOfWeek(dayOfWeek).build());
        hours.setClosed(closed);
        hours.setOpeningTime(closed ? null : request.getOpeningTime());
        hours.setClosingTime(closed ? null : request.getClosingTime());
        return toBusinessHoursResponse(businessHoursRepository.save(hours));
    }

    @Override
    @Transactional
    public void deleteBusinessHours(Long branchId, DayOfWeek dayOfWeek) {
        findBranch(branchId);
        businessHoursRepository.findByBranch_IdAndDayOfWeek(branchId, dayOfWeek)
                .ifPresent(businessHoursRepository::delete);
    }

    private Branch findBranch(Long id) {
        return branchRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Branch not found with id: " + id));
    }

    private BranchBusinessHoursResponse toBusinessHoursResponse(BranchBusinessHours hours) {
        return BranchBusinessHoursResponse.builder()
                .id(hours.getId())
                .branchId(hours.getBranch().getId())
                .dayOfWeek(hours.getDayOfWeek())
                .openingTime(hours.getOpeningTime())
                .closingTime(hours.getClosingTime())
                .isClosed(hours.isClosed())
                .createdAt(hours.getCreatedAt())
                .updatedAt(hours.getUpdatedAt())
                .build();
    }

    private BranchResponse toResponse(Branch branch) {
        return BranchResponse.builder()
                .id(branch.getId())
                .code(branch.getCode())
                .name(branch.getName())
                .address(branch.getAddress())
                .phone(branch.getPhone())
                .status(branch.getStatus())
                .createdAt(branch.getCreatedAt())
                .updatedAt(branch.getUpdatedAt())
                .build();
    }
}
