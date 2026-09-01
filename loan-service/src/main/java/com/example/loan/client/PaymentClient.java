package com.example.loan.client;

import com.example.loan.dto.GenerateScheduleRequest;
import com.example.loan.dto.LoanRepaymentTransactionRequest;
import com.example.loan.dto.MarkPaidByInstallmentRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "payment-service")
public interface PaymentClient {

    @PostMapping("/api/payments/schedule")
    void createSchedule(@RequestBody GenerateScheduleRequest request);

    // Books a payment-transactions ledger entry for a loan repayment that already
    // happened here — see LoanServiceImpl.applyPayment/addPayment, the two chokepoints
    // that call this regardless of who triggered the payment (payment-service's
    // markAsPaid, or a payment recorded directly on the loan).
    @PostMapping("/api/payments/transactions/loan-repayment")
    void createLoanRepaymentTransaction(@RequestBody LoanRepaymentTransactionRequest request);

    // Keeps payment-service's own Payment row for this installment in sync after
    // it's paid off through THIS service's own LoanPayment/LoanPaymentDetail
    // allocation flow (addPayment) — that flow never goes through payment-service's
    // markAsPaid, so without this call its row would silently stay PENDING/OVERDUE.
    // Only called from addPayment(), never from applyPayment() — applyPayment() is
    // itself triggered BY payment-service's markAsPaid, so calling back here from
    // there would just be re-syncing a row to itself.
    @PutMapping("/api/payments/mark-paid-by-installment")
    void markPaidByInstallment(@RequestBody MarkPaidByInstallmentRequest request);
}
