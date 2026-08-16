package com.example.loan.service;

import com.example.loan.common.PageResponse;
import com.example.loan.dto.ApplyPaymentRequest;
import com.example.loan.dto.LoanAdjustmentRequest;
import com.example.loan.dto.LoanAdjustmentResponse;
import com.example.loan.dto.DisbursementReasonRequest;
import com.example.loan.dto.LoanCollateralRequest;
import com.example.loan.dto.LoanCollateralResponse;
import com.example.loan.dto.LoanCollateralSeizeRequest;
import com.example.loan.dto.LoanDisbursementRequest;
import com.example.loan.dto.LoanDisbursementResponse;
import com.example.loan.dto.LoanDocumentRequest;
import com.example.loan.dto.LoanDocumentResponse;
import com.example.loan.dto.LoanDocumentStatusUpdateRequest;
import com.example.loan.dto.LoanNoteRequest;
import com.example.loan.dto.LoanNoteResponse;
import com.example.loan.dto.LoanFeeRequest;
import com.example.loan.dto.LoanFeeResponse;
import com.example.loan.dto.LoanGuarantorRequest;
import com.example.loan.dto.LoanGuarantorResponse;
import com.example.loan.dto.LoanInterestRequest;
import com.example.loan.dto.LoanInterestResponse;
import com.example.loan.dto.LoanPaymentRequest;
import com.example.loan.dto.LoanPaymentResponse;
import com.example.loan.dto.LoanPaymentReversalRejectRequest;
import com.example.loan.dto.LoanPaymentReverseRequest;
import com.example.loan.dto.LoanPayoffQuoteResponse;
import com.example.loan.dto.LoanPayoffRequest;
import com.example.loan.dto.LoanPenaltyRequest;
import com.example.loan.dto.LoanPenaltyResponse;
import com.example.loan.dto.LoanRefinanceRequest;
import com.example.loan.dto.LoanRefinanceResponse;
import com.example.loan.dto.LoanRequest;
import com.example.loan.dto.LoanResponse;
import com.example.loan.dto.LoanRestructureRejectRequest;
import com.example.loan.dto.LoanRestructureRequest;
import com.example.loan.dto.LoanRestructureResponse;
import com.example.loan.dto.LoanScheduleInstallmentResponse;
import com.example.loan.dto.LoanScheduleResponse;
import com.example.loan.dto.LoanSettlementRequest;
import com.example.loan.dto.LoanSettlementResponse;
import com.example.loan.dto.LoanStatusHistoryResponse;
import com.example.loan.dto.LoanTransactionResponse;
import com.example.loan.dto.LoanWriteoffRecoveryRequest;
import com.example.loan.dto.LoanWriteoffRecoveryResponse;
import com.example.loan.dto.LoanWriteoffRequest;
import com.example.loan.dto.LoanWriteoffResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface LoanService {

    LoanResponse create(LoanRequest request);

    LoanResponse update(Long id, LoanRequest request);

    LoanResponse getById(Long id);

    PageResponse<LoanResponse> getAll(int page, int size, String sortBy, String sortOrder,
                                       Long customerId, Long branchId,
                                       BigDecimal minPrincipal, BigDecimal maxPrincipal,
                                       LocalDate dateFrom, LocalDate dateTo);

    List<LoanResponse> getByCustomer(Long customerId);

    LoanResponse approve(Long id);

    LoanResponse reject(Long id);

    LoanResponse disburse(Long id);

    LoanResponse applyPayment(Long id, ApplyPaymentRequest request);

    void delete(Long id);

    List<LoanStatusHistoryResponse> getStatusHistory(Long id);

    LoanDisbursementResponse addDisbursement(Long id, LoanDisbursementRequest request);

    List<LoanDisbursementResponse> getDisbursements(Long id);

    LoanDisbursementResponse updateDisbursement(Long id, Long disbursementId, LoanDisbursementRequest request);

    void deleteDisbursement(Long id, Long disbursementId);

    LoanDisbursementResponse approveDisbursement(Long id, Long disbursementId);

    LoanDisbursementResponse rejectDisbursement(Long id, Long disbursementId, DisbursementReasonRequest request);

    LoanDisbursementResponse voidDisbursement(Long id, Long disbursementId, DisbursementReasonRequest request);

    LoanGuarantorResponse addGuarantor(Long id, LoanGuarantorRequest request);

    List<LoanGuarantorResponse> getGuarantors(Long id);

    LoanGuarantorResponse updateGuarantor(Long id, Long guarantorId, LoanGuarantorRequest request);

    void deleteGuarantor(Long id, Long guarantorId);

    LoanGuarantorResponse releaseGuarantor(Long id, Long guarantorId);

    LoanCollateralResponse addCollateral(Long id, LoanCollateralRequest request);

    List<LoanCollateralResponse> getCollaterals(Long id);

    LoanCollateralResponse updateCollateral(Long id, Long collateralId, LoanCollateralRequest request);

    void deleteCollateral(Long id, Long collateralId);

    LoanCollateralResponse releaseCollateral(Long id, Long collateralId);

    LoanCollateralResponse seizeCollateral(Long id, Long collateralId, LoanCollateralSeizeRequest request);

    LoanDocumentResponse addDocument(Long id, LoanDocumentRequest request);

    List<LoanDocumentResponse> getDocuments(Long id);

    LoanDocumentResponse updateDocumentStatus(Long id, Long documentId, LoanDocumentStatusUpdateRequest request);

    void deleteDocument(Long id, Long documentId);

    LoanNoteResponse addNote(Long id, LoanNoteRequest request);

    List<LoanNoteResponse> getNotes(Long id);

    List<LoanScheduleResponse> getSchedules(Long id);

    List<LoanScheduleInstallmentResponse> getScheduleInstallments(Long id, Long scheduleId);

    LoanPaymentResponse addPayment(Long id, LoanPaymentRequest request);

    List<LoanPaymentResponse> getPayments(Long id);

    LoanPaymentResponse reversePayment(Long id, Long paymentId, LoanPaymentReverseRequest request);

    LoanPaymentResponse approvePaymentReversal(Long id, Long paymentId);

    LoanPaymentResponse rejectPaymentReversal(Long id, Long paymentId, LoanPaymentReversalRejectRequest request);

    LoanPayoffQuoteResponse getPayoffQuote(Long id);

    LoanResponse payoff(Long id, LoanPayoffRequest request);

    LoanInterestResponse addInterestAccrual(Long id, LoanInterestRequest request);

    List<LoanInterestResponse> getInterestAccruals(Long id);

    LoanInterestResponse updateInterestAccrual(Long id, Long accrualId, LoanInterestRequest request);

    void deleteInterestAccrual(Long id, Long accrualId);

    PageResponse<LoanInterestResponse> getAllInterestAccruals(int page, int size, String sortBy, String sortOrder);

    LoanPenaltyResponse addPenalty(Long id, LoanPenaltyRequest request);

    List<LoanPenaltyResponse> getPenalties(Long id);

    LoanPenaltyResponse updatePenalty(Long id, Long penaltyId, LoanPenaltyRequest request);

    void deletePenalty(Long id, Long penaltyId);

    LoanPenaltyResponse payPenalty(Long id, Long penaltyId);

    LoanPenaltyResponse waivePenalty(Long id, Long penaltyId);

    PageResponse<LoanPenaltyResponse> getAllPenalties(int page, int size, String sortBy, String sortOrder);

    LoanFeeResponse addFee(Long id, LoanFeeRequest request);

    List<LoanFeeResponse> getFees(Long id);

    LoanFeeResponse updateFee(Long id, Long feeId, LoanFeeRequest request);

    void deleteFee(Long id, Long feeId);

    LoanFeeResponse payFee(Long id, Long feeId);

    LoanFeeResponse waiveFee(Long id, Long feeId);

    LoanRestructureResponse addRestructure(Long id, LoanRestructureRequest request);

    List<LoanRestructureResponse> getRestructures(Long id);

    LoanRestructureResponse approveRestructure(Long id, Long restructureId);

    LoanRestructureResponse rejectRestructure(Long id, Long restructureId, LoanRestructureRejectRequest request);

    PageResponse<LoanRestructureResponse> getAllRestructures(int page, int size, String sortBy, String sortOrder);

    LoanRefinanceResponse addRefinance(Long id, LoanRefinanceRequest request);

    List<LoanRefinanceResponse> getRefinances(Long id);

    PageResponse<LoanRefinanceResponse> getAllRefinances(int page, int size, String sortBy, String sortOrder);

    LoanSettlementResponse addSettlement(Long id, LoanSettlementRequest request);

    LoanSettlementResponse getSettlement(Long id);

    LoanSettlementResponse completeSettlement(Long id);

    LoanWriteoffResponse addWriteoff(Long id, LoanWriteoffRequest request);

    LoanWriteoffResponse getWriteoff(Long id);

    LoanWriteoffResponse completeWriteoff(Long id);

    PageResponse<LoanWriteoffResponse> getAllWriteoffs(int page, int size, String sortBy, String sortOrder);

    LoanWriteoffRecoveryResponse recordWriteoffRecovery(Long id, LoanWriteoffRecoveryRequest request);

    List<LoanWriteoffRecoveryResponse> getWriteoffRecoveries(Long id);

    LoanAdjustmentResponse addAdjustment(Long id, LoanAdjustmentRequest request);

    List<LoanAdjustmentResponse> getAdjustments(Long id);

    List<LoanTransactionResponse> getTransactions(Long id);
}
