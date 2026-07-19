package com.example.loan.service.impl;

import com.example.loan.client.CustomerClient;
import com.example.loan.client.PaymentClient;
import com.example.loan.common.PageResponse;
import com.example.loan.dto.ApplyPaymentRequest;
import com.example.loan.dto.CustomerResponse;
import com.example.loan.dto.GenerateScheduleRequest;
import com.example.loan.dto.LoanRequest;
import com.example.loan.dto.LoanResponse;
import com.example.loan.dto.ScheduleInstallmentRequest;
import com.example.loan.entity.Loan;
import com.example.loan.entity.LoanStatus;
import com.example.loan.exception.AppException;
import com.example.loan.exception.ResourceNotFoundException;
import com.example.loan.repository.LoanRepository;
import com.example.loan.service.LoanService;
import com.example.loan.util.AmortizationCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final CustomerClient customerClient;
    private final PaymentClient paymentClient;

    @Override
    public LoanResponse create(LoanRequest request) {
        CustomerResponse customer = customerClient.getById(request.getCustomerId());

        Loan loan = Loan.builder()
                .customerId(request.getCustomerId())
                .principal(request.getPrincipal())
                .interestRate(request.getInterestRate())
                .termMonths(request.getTermMonths())
                .purpose(request.getPurpose())
                .build();

        return toResponse(loanRepository.save(loan), customer);
    }

    @Override
    public LoanResponse getById(Long id) {
        Loan loan = findOrThrow(id);
        CustomerResponse customer = customerClient.getById(loan.getCustomerId());
        return toResponse(loan, customer);
    }

    @Override
    public PageResponse<LoanResponse> getAll(int page, int size, String sortBy, String sortOrder) {
        Sort sort = "asc".equalsIgnoreCase(sortOrder)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size, sort);
        return PageResponse.of(loanRepository.findAll(pageable)
                .map(loan -> toResponse(loan, customerClient.getById(loan.getCustomerId()))));
    }

    @Override
    public List<LoanResponse> getByCustomer(Long customerId) {
        customerClient.getById(customerId);
        return loanRepository.findByCustomerId(customerId).stream()
                .map(loan -> toResponse(loan, customerClient.getById(loan.getCustomerId())))
                .toList();
    }

    @Override
    public LoanResponse approve(Long id) {
        Loan loan = findOrThrow(id);
        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new AppException(HttpStatus.CONFLICT, "Only PENDING loans can be approved");
        }
        loan.setStatus(LoanStatus.APPROVED);
        loan.setApprovedAt(LocalDateTime.now());
        CustomerResponse customer = customerClient.getById(loan.getCustomerId());
        return toResponse(loanRepository.save(loan), customer);
    }

    @Override
    public LoanResponse reject(Long id) {
        Loan loan = findOrThrow(id);
        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new AppException(HttpStatus.CONFLICT, "Only PENDING loans can be rejected");
        }
        loan.setStatus(LoanStatus.REJECTED);
        loan.setRejectedAt(LocalDateTime.now());
        CustomerResponse customer = customerClient.getById(loan.getCustomerId());
        return toResponse(loanRepository.save(loan), customer);
    }

    @Override
    public LoanResponse disburse(Long id) {
        Loan loan = findOrThrow(id);
        if (loan.getStatus() != LoanStatus.APPROVED) {
            throw new AppException(HttpStatus.CONFLICT, "Only APPROVED loans can be disbursed");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDate disbursementDate = now.toLocalDate();

        List<AmortizationCalculator.Installment> schedule = AmortizationCalculator.generateSchedule(
                loan.getPrincipal(), loan.getInterestRate(), loan.getTermMonths(), disbursementDate);
        BigDecimal emi = AmortizationCalculator.calculateEmi(
                loan.getPrincipal(), loan.getInterestRate(), loan.getTermMonths());
        BigDecimal totalOutstanding = schedule.stream()
                .map(AmortizationCalculator.Installment::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<ScheduleInstallmentRequest> installmentRequests = schedule.stream()
                .map(i -> ScheduleInstallmentRequest.builder()
                        .installmentNumber(i.installmentNumber())
                        .dueDate(i.dueDate())
                        .principalComponent(i.principalComponent())
                        .interestComponent(i.interestComponent())
                        .amount(i.amount())
                        .build())
                .toList();

        paymentClient.createSchedule(GenerateScheduleRequest.builder()
                .loanId(loan.getId())
                .installments(installmentRequests)
                .build());

        loan.setStatus(LoanStatus.ACTIVE);
        loan.setDisbursedAt(now);
        loan.setMaturityDate(disbursementDate.plusMonths(loan.getTermMonths()));
        loan.setMonthlyInstallment(emi);
        loan.setOutstandingBalance(totalOutstanding);
        Loan saved = loanRepository.save(loan);

        CustomerResponse customer = customerClient.getById(saved.getCustomerId());
        return toResponse(saved, customer);
    }

    @Override
    public LoanResponse applyPayment(Long id, ApplyPaymentRequest request) {
        Loan loan = findOrThrow(id);
        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new AppException(HttpStatus.CONFLICT, "Payments can only be applied to ACTIVE loans");
        }
        BigDecimal newBalance = loan.getOutstandingBalance().subtract(request.getAmount());
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            newBalance = BigDecimal.ZERO;
        }
        loan.setOutstandingBalance(newBalance);
        if (newBalance.compareTo(BigDecimal.ZERO) == 0) {
            loan.setStatus(LoanStatus.CLOSED);
            loan.setClosedAt(LocalDateTime.now());
        }
        Loan saved = loanRepository.save(loan);
        CustomerResponse customer = customerClient.getById(saved.getCustomerId());
        return toResponse(saved, customer);
    }

    @Override
    public void delete(Long id) {
        loanRepository.delete(findOrThrow(id));
    }

    private Loan findOrThrow(Long id) {
        return loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan", id));
    }

    private LoanResponse toResponse(Loan loan, CustomerResponse customer) {
        return LoanResponse.builder()
                .id(loan.getId())
                .customerId(loan.getCustomerId())
                .customerName(customer != null
                        ? customer.getFirstName() + " " + customer.getLastName()
                        : null)
                .principal(loan.getPrincipal())
                .interestRate(loan.getInterestRate())
                .termMonths(loan.getTermMonths())
                .status(loan.getStatus())
                .purpose(loan.getPurpose())
                .approvedAt(loan.getApprovedAt())
                .rejectedAt(loan.getRejectedAt())
                .disbursedAt(loan.getDisbursedAt())
                .closedAt(loan.getClosedAt())
                .maturityDate(loan.getMaturityDate())
                .monthlyInstallment(loan.getMonthlyInstallment())
                .outstandingBalance(loan.getOutstandingBalance())
                .createdAt(loan.getCreatedAt())
                .updatedAt(loan.getUpdatedAt())
                .build();
    }
}
