package com.example.accounting.service.impl;

import com.example.accounting.dto.BudgetLineRequest;
import com.example.accounting.dto.BudgetLineResponse;
import com.example.accounting.entity.BudgetLine;
import com.example.accounting.entity.FinancialPeriod;
import com.example.accounting.entity.GlAccount;
import com.example.accounting.exception.AppException;
import com.example.accounting.exception.ResourceNotFoundException;
import com.example.accounting.repository.BudgetLineRepository;
import com.example.accounting.repository.FinancialPeriodRepository;
import com.example.accounting.repository.GlAccountRepository;
import com.example.accounting.service.BudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetServiceImpl implements BudgetService {

    private final BudgetLineRepository budgetLineRepository;
    private final FinancialPeriodRepository financialPeriodRepository;
    private final GlAccountRepository glAccountRepository;

    @Override
    public BudgetLineResponse create(BudgetLineRequest request) {
        FinancialPeriod period = financialPeriodRepository.findById(request.getFinancialPeriodId())
                .orElseThrow(() -> new ResourceNotFoundException("Financial period", request.getFinancialPeriodId()));
        GlAccount account = glAccountRepository.findById(request.getGlAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("GL account", request.getGlAccountId()));

        if (budgetLineRepository.existsByFinancialPeriodIdAndGlAccountId(period.getId(), account.getId())) {
            throw new AppException(HttpStatus.CONFLICT, "A budget line already exists for this account and period");
        }

        BudgetLine saved = budgetLineRepository.save(BudgetLine.builder()
                .financialPeriod(period)
                .glAccount(account)
                .budgetAmount(request.getBudgetAmount())
                .build());
        return toResponse(saved);
    }

    // Only the amount is editable — the (period, account) pair is the line's identity,
    // so changing either is really "delete this line, create a different one."
    @Override
    public BudgetLineResponse update(Long id, BudgetLineRequest request) {
        BudgetLine line = budgetLineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget line", id));
        line.setBudgetAmount(request.getBudgetAmount());
        return toResponse(budgetLineRepository.save(line));
    }

    @Override
    public List<BudgetLineResponse> getByFinancialPeriod(Long financialPeriodId) {
        return budgetLineRepository.findByFinancialPeriodId(financialPeriodId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public void delete(Long id) {
        if (!budgetLineRepository.existsById(id)) {
            throw new ResourceNotFoundException("Budget line", id);
        }
        budgetLineRepository.deleteById(id);
    }

    private BudgetLineResponse toResponse(BudgetLine line) {
        return BudgetLineResponse.builder()
                .id(line.getId())
                .financialPeriodId(line.getFinancialPeriod().getId())
                .financialPeriodName(line.getFinancialPeriod().getPeriodName())
                .glAccountId(line.getGlAccount().getId())
                .accountNo(line.getGlAccount().getAccountNo())
                .accountName(line.getGlAccount().getAccountName())
                .budgetAmount(line.getBudgetAmount())
                .build();
    }
}
