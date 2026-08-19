package com.example.loan.client;

import com.example.loan.dto.GenerateScheduleRequest;
import com.example.loan.dto.LoanRepaymentTransactionRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
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
}
