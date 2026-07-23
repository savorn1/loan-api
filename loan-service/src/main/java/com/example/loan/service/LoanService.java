package com.example.loan.service;

import com.example.loan.common.PageResponse;
import com.example.loan.dto.ApplyPaymentRequest;
import com.example.loan.dto.LoanAdjustmentRequest;
import com.example.loan.dto.LoanAdjustmentResponse;
import com.example.loan.dto.LoanCollateralRequest;
import com.example.loan.dto.LoanCollateralResponse;
import com.example.loan.dto.LoanDisbursementRequest;
import com.example.loan.dto.LoanDisbursementResponse;
import com.example.loan.dto.LoanFeeRequest;
import com.example.loan.dto.LoanFeeResponse;
import com.example.loan.dto.LoanGuarantorRequest;
import com.example.loan.dto.LoanGuarantorResponse;
import com.example.loan.dto.LoanInterestRequest;
import com.example.loan.dto.LoanInterestResponse;
import com.example.loan.dto.LoanPaymentRequest;
import com.example.loan.dto.LoanPaymentResponse;
import com.example.loan.dto.LoanPenaltyRequest;
import com.example.loan.dto.LoanPenaltyResponse;
import com.example.loan.dto.LoanRequest;
import com.example.loan.dto.LoanResponse;
import com.example.loan.dto.LoanRestructureRequest;
import com.example.loan.dto.LoanRestructureResponse;
import com.example.loan.dto.LoanScheduleInstallmentResponse;
import com.example.loan.dto.LoanScheduleResponse;
import com.example.loan.dto.LoanSettlementRequest;
import com.example.loan.dto.LoanSettlementResponse;
import com.example.loan.dto.LoanStatusHistoryResponse;
import com.example.loan.dto.LoanTransactionResponse;
import com.example.loan.dto.LoanWriteoffRequest;
import com.example.loan.dto.LoanWriteoffResponse;

import java.util.List;

public interface LoanService {

    LoanResponse create(LoanRequest request);

    LoanResponse getById(Long id);

    PageResponse<LoanResponse> getAll(int page, int size, String sortBy, String sortOrder);

    List<LoanResponse> getByCustomer(Long customerId);

    LoanResponse approve(Long id);

    LoanResponse reject(Long id);

    LoanResponse disburse(Long id);

    LoanResponse applyPayment(Long id, ApplyPaymentRequest request);

    void delete(Long id);

    List<LoanStatusHistoryResponse> getStatusHistory(Long id);

    LoanDisbursementResponse addDisbursement(Long id, LoanDisbursementRequest request);

    List<LoanDisbursementResponse> getDisbursements(Long id);

    LoanGuarantorResponse addGuarantor(Long id, LoanGuarantorRequest request);

    List<LoanGuarantorResponse> getGuarantors(Long id);

    LoanGuarantorResponse releaseGuarantor(Long id, Long guarantorId);

    LoanCollateralResponse addCollateral(Long id, LoanCollateralRequest request);

    List<LoanCollateralResponse> getCollaterals(Long id);

    LoanCollateralResponse releaseCollateral(Long id, Long collateralId);

    List<LoanScheduleResponse> getSchedules(Long id);

    List<LoanScheduleInstallmentResponse> getScheduleInstallments(Long id, Long scheduleId);

    LoanPaymentResponse addPayment(Long id, LoanPaymentRequest request);

    List<LoanPaymentResponse> getPayments(Long id);

    LoanInterestResponse addInterestAccrual(Long id, LoanInterestRequest request);

    List<LoanInterestResponse> getInterestAccruals(Long id);

    LoanPenaltyResponse addPenalty(Long id, LoanPenaltyRequest request);

    List<LoanPenaltyResponse> getPenalties(Long id);

    LoanPenaltyResponse payPenalty(Long id, Long penaltyId);

    LoanPenaltyResponse waivePenalty(Long id, Long penaltyId);

    LoanFeeResponse addFee(Long id, LoanFeeRequest request);

    List<LoanFeeResponse> getFees(Long id);

    LoanFeeResponse payFee(Long id, Long feeId);

    LoanFeeResponse waiveFee(Long id, Long feeId);

    LoanRestructureResponse addRestructure(Long id, LoanRestructureRequest request);

    List<LoanRestructureResponse> getRestructures(Long id);

    LoanSettlementResponse addSettlement(Long id, LoanSettlementRequest request);

    LoanSettlementResponse getSettlement(Long id);

    LoanSettlementResponse completeSettlement(Long id);

    LoanWriteoffResponse addWriteoff(Long id, LoanWriteoffRequest request);

    LoanWriteoffResponse getWriteoff(Long id);

    LoanWriteoffResponse completeWriteoff(Long id);

    LoanAdjustmentResponse addAdjustment(Long id, LoanAdjustmentRequest request);

    List<LoanAdjustmentResponse> getAdjustments(Long id);

    List<LoanTransactionResponse> getTransactions(Long id);
}
