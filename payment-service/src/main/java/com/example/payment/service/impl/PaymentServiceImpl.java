package com.example.payment.service.impl;

import com.example.payment.client.LoanClient;
import com.example.payment.dto.PaymentRequest;
import com.example.payment.dto.PaymentResponse;
import com.example.payment.entity.Payment;
import com.example.payment.entity.PaymentStatus;
import com.example.payment.exception.AppException;
import com.example.payment.exception.ResourceNotFoundException;
import com.example.payment.repository.PaymentRepository;
import com.example.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final LoanClient loanClient;

    @Override
    public PaymentResponse create(PaymentRequest request) {
        loanClient.getById(request.getLoanId());

        Payment payment = Payment.builder()
                .loanId(request.getLoanId())
                .amount(request.getAmount())
                .dueDate(request.getDueDate())
                .note(request.getNote())
                .build();

        return toResponse(paymentRepository.save(payment));
    }

    @Override
    public PaymentResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    public List<PaymentResponse> getAll() {
        return paymentRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public List<PaymentResponse> getByLoan(Long loanId) {
        loanClient.getById(loanId);
        return paymentRepository.findByLoanId(loanId).stream().map(this::toResponse).toList();
    }

    @Override
    public PaymentResponse markAsPaid(Long id) {
        Payment payment = findOrThrow(id);
        if (payment.getStatus() == PaymentStatus.PAID) {
            throw new AppException(HttpStatus.CONFLICT, "Payment is already marked as PAID");
        }
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDate.now());
        return toResponse(paymentRepository.save(payment));
    }

    @Override
    public void delete(Long id) {
        paymentRepository.delete(findOrThrow(id));
    }

    private Payment findOrThrow(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", id));
    }

    private PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .loanId(payment.getLoanId())
                .amount(payment.getAmount())
                .dueDate(payment.getDueDate())
                .paidAt(payment.getPaidAt())
                .status(payment.getStatus())
                .note(payment.getNote())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}
