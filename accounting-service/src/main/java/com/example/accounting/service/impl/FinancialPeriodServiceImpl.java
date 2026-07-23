package com.example.accounting.service.impl;

import com.example.accounting.dto.FinancialPeriodRequest;
import com.example.accounting.dto.FinancialPeriodResponse;
import com.example.accounting.entity.FinancialPeriod;
import com.example.accounting.entity.FinancialPeriodStatus;
import com.example.accounting.exception.AppException;
import com.example.accounting.exception.ResourceNotFoundException;
import com.example.accounting.repository.FinancialPeriodRepository;
import com.example.accounting.service.FinancialPeriodService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FinancialPeriodServiceImpl implements FinancialPeriodService {

    private final FinancialPeriodRepository financialPeriodRepository;

    @Override
    public FinancialPeriodResponse create(FinancialPeriodRequest request) {
        if (financialPeriodRepository.existsByPeriodName(request.getPeriodName())) {
            throw new AppException(HttpStatus.CONFLICT, "Financial period already exists: " + request.getPeriodName());
        }
        if (!request.getStartDate().isBefore(request.getEndDate())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "startDate must be before endDate");
        }
        FinancialPeriod period = FinancialPeriod.builder()
                .periodName(request.getPeriodName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(FinancialPeriodStatus.OPEN)
                .build();
        return toResponse(financialPeriodRepository.save(period));
    }

    @Override
    public FinancialPeriodResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    public List<FinancialPeriodResponse> getAll() {
        return financialPeriodRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public FinancialPeriodResponse update(Long id, FinancialPeriodRequest request) {
        FinancialPeriod period = findOrThrow(id);
        if (period.getStatus() == FinancialPeriodStatus.CLOSED) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Cannot modify a closed financial period");
        }
        if (!period.getPeriodName().equals(request.getPeriodName())
                && financialPeriodRepository.existsByPeriodName(request.getPeriodName())) {
            throw new AppException(HttpStatus.CONFLICT, "Financial period already exists: " + request.getPeriodName());
        }
        if (!request.getStartDate().isBefore(request.getEndDate())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "startDate must be before endDate");
        }
        period.setPeriodName(request.getPeriodName());
        period.setStartDate(request.getStartDate());
        period.setEndDate(request.getEndDate());
        return toResponse(financialPeriodRepository.save(period));
    }

    @Override
    public FinancialPeriodResponse close(Long id) {
        FinancialPeriod period = findOrThrow(id);
        if (period.getStatus() == FinancialPeriodStatus.CLOSED) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Financial period is already closed");
        }
        period.setStatus(FinancialPeriodStatus.CLOSED);
        return toResponse(financialPeriodRepository.save(period));
    }

    @Override
    public void delete(Long id) {
        FinancialPeriod period = findOrThrow(id);
        if (period.getStatus() == FinancialPeriodStatus.CLOSED) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Cannot delete a closed financial period");
        }
        financialPeriodRepository.delete(period);
    }

    private FinancialPeriod findOrThrow(Long id) {
        return financialPeriodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Financial period", id));
    }

    private FinancialPeriodResponse toResponse(FinancialPeriod period) {
        return FinancialPeriodResponse.builder()
                .id(period.getId())
                .periodName(period.getPeriodName())
                .startDate(period.getStartDate())
                .endDate(period.getEndDate())
                .status(period.getStatus())
                .createdAt(period.getCreatedAt())
                .updatedAt(period.getUpdatedAt())
                .build();
    }
}
