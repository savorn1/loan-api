package com.example.loan.service.impl;

import com.example.loan.client.CustomerClient;
import com.example.loan.dto.CustomerResponse;
import com.example.loan.dto.LoanRequest;
import com.example.loan.dto.LoanResponse;
import com.example.loan.entity.Loan;
import com.example.loan.entity.LoanStatus;
import com.example.loan.exception.AppException;
import com.example.loan.exception.ResourceNotFoundException;
import com.example.loan.repository.LoanRepository;
import com.example.loan.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final CustomerClient customerClient;

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
    public List<LoanResponse> getAll() {
        return loanRepository.findAll().stream()
                .map(loan -> toResponse(loan, customerClient.getById(loan.getCustomerId())))
                .toList();
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
                .createdAt(loan.getCreatedAt())
                .updatedAt(loan.getUpdatedAt())
                .build();
    }
}
