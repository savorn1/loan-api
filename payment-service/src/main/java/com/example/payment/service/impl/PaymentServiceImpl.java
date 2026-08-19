package com.example.payment.service.impl;

import com.example.payment.client.LoanClient;
import com.example.payment.common.PageResponse;
import com.example.payment.dto.ApplyPaymentRequest;
import com.example.payment.dto.GenerateScheduleRequest;
import com.example.payment.dto.PaymentRequest;
import com.example.payment.dto.PaymentResponse;
import com.example.payment.dto.PaymentStatusHistoryResponse;
import com.example.payment.entity.Payment;
import com.example.payment.entity.PaymentStatus;
import com.example.payment.entity.PaymentStatusHistory;
import com.example.payment.exception.AppException;
import com.example.payment.exception.ResourceNotFoundException;
import com.example.payment.repository.PaymentRepository;
import com.example.payment.repository.PaymentStatusHistoryRepository;
import com.example.payment.scheduler.PaymentReminderNotifier;
import com.example.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentStatusHistoryRepository paymentStatusHistoryRepository;
    private final LoanClient loanClient;
    private final PaymentReminderNotifier reminderNotifier;

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
    public PageResponse<PaymentResponse> getAll(int page, int size, String sortBy, String sortOrder) {
        Sort sort = "asc".equalsIgnoreCase(sortOrder)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size, sort);
        return PageResponse.of(paymentRepository.findAll(pageable).map(this::toResponse));
    }

    @Override
    public List<PaymentResponse> getByLoan(Long loanId) {
        loanClient.getById(loanId);
        return paymentRepository.findByLoanId(loanId).stream().map(this::toResponse).toList();
    }

    @Override
    public PaymentResponse markAsPaid(Long id, String changedBy) {
        Payment payment = findOrThrow(id);
        if (payment.getStatus() == PaymentStatus.PAID) {
            throw new AppException(HttpStatus.CONFLICT, "Payment is already marked as PAID");
        }
        PaymentStatus fromStatus = payment.getStatus();
        // loan-service's applyPayment() records this in the payment-transactions ledger
        // itself (see LoanServiceImpl.applyPayment) — it's the one chokepoint shared by
        // this call and the loan's own "Apply payment to balance" action, so doing it
        // here too would double-book the same repayment.
        loanClient.applyPayment(payment.getLoanId(), new ApplyPaymentRequest(payment.getAmount()));
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDate.now());
        payment.setPaidBy(changedBy);
        Payment saved = paymentRepository.save(payment);

        paymentStatusHistoryRepository.save(PaymentStatusHistory.builder()
                .payment(saved)
                .fromStatus(fromStatus)
                .toStatus(PaymentStatus.PAID)
                .changedBy(changedBy)
                .build());

        reminderNotifier.notify(saved, "Payment received",
                String.format("We received your payment of %s for loan #%d.", saved.getAmount(), saved.getLoanId()));
        return toResponse(saved);
    }

    @Override
    public List<PaymentStatusHistoryResponse> getStatusHistory(Long id) {
        findOrThrow(id);
        return paymentStatusHistoryRepository.findByPaymentIdOrderByCreatedAtDesc(id).stream()
                .map(this::toStatusHistoryResponse)
                .toList();
    }

    @Override
    public List<PaymentResponse> createSchedule(GenerateScheduleRequest request) {
        if (!paymentRepository.findByLoanId(request.getLoanId()).isEmpty()) {
            throw new AppException(HttpStatus.CONFLICT,
                    "Payment schedule already exists for loan " + request.getLoanId());
        }
        List<Payment> payments = request.getInstallments().stream()
                .map(i -> Payment.builder()
                        .loanId(request.getLoanId())
                        .installmentNumber(i.getInstallmentNumber())
                        .amount(i.getAmount())
                        .dueDate(i.getDueDate())
                        .principalComponent(i.getPrincipalComponent())
                        .interestComponent(i.getInterestComponent())
                        .build())
                .toList();
        return paymentRepository.saveAll(payments).stream().map(this::toResponse).toList();
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
                .paidBy(payment.getPaidBy())
                .status(payment.getStatus())
                .note(payment.getNote())
                .installmentNumber(payment.getInstallmentNumber())
                .principalComponent(payment.getPrincipalComponent())
                .interestComponent(payment.getInterestComponent())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }

    private PaymentStatusHistoryResponse toStatusHistoryResponse(PaymentStatusHistory history) {
        return PaymentStatusHistoryResponse.builder()
                .id(history.getId())
                .paymentId(history.getPayment().getId())
                .fromStatus(history.getFromStatus())
                .toStatus(history.getToStatus())
                .changedBy(history.getChangedBy())
                .note(history.getNote())
                .changedAt(history.getCreatedAt())
                .build();
    }
}
