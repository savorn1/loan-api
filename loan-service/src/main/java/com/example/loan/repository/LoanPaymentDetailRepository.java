package com.example.loan.repository;

import com.example.loan.entity.LoanPaymentDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanPaymentDetailRepository extends JpaRepository<LoanPaymentDetail, Long> {

    List<LoanPaymentDetail> findByPaymentIdOrderByIdAsc(Long paymentId);

    List<LoanPaymentDetail> findByScheduleInstallmentId(Long scheduleInstallmentId);

    // Navigates LoanPaymentDetail.payment.loan.id — used by the payoff quote calc to sum
    // principal paid so far across every payment on the loan, not just one.
    List<LoanPaymentDetail> findByPayment_LoanId(Long loanId);
}
