package com.example.loan.controller;

import com.example.loan.common.ApiResponse;
import com.example.loan.common.PageResponse;
import com.example.loan.dto.ApplyPaymentRequest;
import com.example.loan.dto.DisbursementReasonRequest;
import com.example.loan.dto.LoanAdjustmentRequest;
import com.example.loan.dto.LoanAdjustmentResponse;
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
import com.example.loan.service.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @PostMapping
    public ResponseEntity<ApiResponse<LoanResponse>> create(@Valid @RequestBody LoanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Loan created", loanService.create(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LoanResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(loanService.getById(id)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LoanResponse>> update(
            @PathVariable Long id, @Valid @RequestBody LoanRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Loan updated", loanService.update(id, request)));
    }

    @GetMapping
    public ResponseEntity<PageResponse<LoanResponse>> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) BigDecimal minPrincipal,
            @RequestParam(required = false) BigDecimal maxPrincipal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        return ResponseEntity.ok(loanService.getAll(page, size, sortBy, sortOrder,
                customerId, branchId, minPrincipal, maxPrincipal, dateFrom, dateTo));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<LoanResponse>>> getByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(ApiResponse.success(loanService.getByCustomer(customerId)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<LoanResponse>> approve(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Loan approved", loanService.approve(id)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<LoanResponse>> reject(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Loan rejected", loanService.reject(id)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/disburse")
    public ResponseEntity<ApiResponse<LoanResponse>> disburse(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Loan disbursed", loanService.disburse(id)));
    }

    @PutMapping("/{id}/apply-payment")
    public ResponseEntity<ApiResponse<LoanResponse>> applyPayment(@PathVariable Long id,
                                                                    @Valid @RequestBody ApplyPaymentRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Payment applied", loanService.applyPayment(id, request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        loanService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Loan deleted", null));
    }

    @GetMapping("/{id}/status-history")
    public ResponseEntity<ApiResponse<List<LoanStatusHistoryResponse>>> getStatusHistory(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(loanService.getStatusHistory(id)));
    }

    @GetMapping("/{id}/disbursements")
    public ResponseEntity<ApiResponse<List<LoanDisbursementResponse>>> getDisbursements(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(loanService.getDisbursements(id)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/disbursements")
    public ResponseEntity<ApiResponse<LoanDisbursementResponse>> addDisbursement(
            @PathVariable Long id, @Valid @RequestBody LoanDisbursementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Disbursement recorded", loanService.addDisbursement(id, request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/disbursements/{disbursementId}")
    public ResponseEntity<ApiResponse<LoanDisbursementResponse>> updateDisbursement(
            @PathVariable Long id, @PathVariable Long disbursementId,
            @Valid @RequestBody LoanDisbursementRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Disbursement updated", loanService.updateDisbursement(id, disbursementId, request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}/disbursements/{disbursementId}")
    public ResponseEntity<ApiResponse<Void>> deleteDisbursement(
            @PathVariable Long id, @PathVariable Long disbursementId) {
        loanService.deleteDisbursement(id, disbursementId);
        return ResponseEntity.ok(ApiResponse.success("Disbursement deleted", null));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/disbursements/{disbursementId}/approve")
    public ResponseEntity<ApiResponse<LoanDisbursementResponse>> approveDisbursement(
            @PathVariable Long id, @PathVariable Long disbursementId) {
        return ResponseEntity.ok(
                ApiResponse.success("Disbursement approved", loanService.approveDisbursement(id, disbursementId)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/disbursements/{disbursementId}/reject")
    public ResponseEntity<ApiResponse<LoanDisbursementResponse>> rejectDisbursement(
            @PathVariable Long id, @PathVariable Long disbursementId,
            @Valid @RequestBody DisbursementReasonRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Disbursement rejected", loanService.rejectDisbursement(id, disbursementId, request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/disbursements/{disbursementId}/void")
    public ResponseEntity<ApiResponse<LoanDisbursementResponse>> voidDisbursement(
            @PathVariable Long id, @PathVariable Long disbursementId,
            @Valid @RequestBody DisbursementReasonRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Disbursement voided", loanService.voidDisbursement(id, disbursementId, request)));
    }

    @GetMapping("/{id}/guarantors")
    public ResponseEntity<ApiResponse<List<LoanGuarantorResponse>>> getGuarantors(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(loanService.getGuarantors(id)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/guarantors")
    public ResponseEntity<ApiResponse<LoanGuarantorResponse>> addGuarantor(
            @PathVariable Long id, @Valid @RequestBody LoanGuarantorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Guarantor added", loanService.addGuarantor(id, request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/guarantors/{guarantorId}")
    public ResponseEntity<ApiResponse<LoanGuarantorResponse>> updateGuarantor(
            @PathVariable Long id, @PathVariable Long guarantorId,
            @Valid @RequestBody LoanGuarantorRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Guarantor updated", loanService.updateGuarantor(id, guarantorId, request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}/guarantors/{guarantorId}")
    public ResponseEntity<ApiResponse<Void>> deleteGuarantor(
            @PathVariable Long id, @PathVariable Long guarantorId) {
        loanService.deleteGuarantor(id, guarantorId);
        return ResponseEntity.ok(ApiResponse.success("Guarantor deleted", null));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/guarantors/{guarantorId}/release")
    public ResponseEntity<ApiResponse<LoanGuarantorResponse>> releaseGuarantor(
            @PathVariable Long id, @PathVariable Long guarantorId) {
        return ResponseEntity.ok(ApiResponse.success("Guarantor released", loanService.releaseGuarantor(id, guarantorId)));
    }

    @GetMapping("/{id}/collaterals")
    public ResponseEntity<ApiResponse<List<LoanCollateralResponse>>> getCollaterals(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(loanService.getCollaterals(id)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/collaterals")
    public ResponseEntity<ApiResponse<LoanCollateralResponse>> addCollateral(
            @PathVariable Long id, @Valid @RequestBody LoanCollateralRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Collateral recorded", loanService.addCollateral(id, request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/collaterals/{collateralId}")
    public ResponseEntity<ApiResponse<LoanCollateralResponse>> updateCollateral(
            @PathVariable Long id, @PathVariable Long collateralId,
            @Valid @RequestBody LoanCollateralRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Collateral updated", loanService.updateCollateral(id, collateralId, request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}/collaterals/{collateralId}")
    public ResponseEntity<ApiResponse<Void>> deleteCollateral(
            @PathVariable Long id, @PathVariable Long collateralId) {
        loanService.deleteCollateral(id, collateralId);
        return ResponseEntity.ok(ApiResponse.success("Collateral deleted", null));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/collaterals/{collateralId}/release")
    public ResponseEntity<ApiResponse<LoanCollateralResponse>> releaseCollateral(
            @PathVariable Long id, @PathVariable Long collateralId) {
        return ResponseEntity.ok(ApiResponse.success("Collateral released", loanService.releaseCollateral(id, collateralId)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/collaterals/{collateralId}/seize")
    public ResponseEntity<ApiResponse<LoanCollateralResponse>> seizeCollateral(
            @PathVariable Long id, @PathVariable Long collateralId,
            @Valid @RequestBody LoanCollateralSeizeRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Collateral seized", loanService.seizeCollateral(id, collateralId, request)));
    }

    @GetMapping("/{id}/documents")
    public ResponseEntity<ApiResponse<List<LoanDocumentResponse>>> getDocuments(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(loanService.getDocuments(id)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/documents")
    public ResponseEntity<ApiResponse<LoanDocumentResponse>> addDocument(
            @PathVariable Long id, @Valid @RequestBody LoanDocumentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Document added", loanService.addDocument(id, request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/documents/{documentId}/status")
    public ResponseEntity<ApiResponse<LoanDocumentResponse>> updateDocumentStatus(
            @PathVariable Long id, @PathVariable Long documentId,
            @Valid @RequestBody LoanDocumentStatusUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Document status updated", loanService.updateDocumentStatus(id, documentId, request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}/documents/{documentId}")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(
            @PathVariable Long id, @PathVariable Long documentId) {
        loanService.deleteDocument(id, documentId);
        return ResponseEntity.ok(ApiResponse.success("Document deleted", null));
    }

    @GetMapping("/{id}/notes")
    public ResponseEntity<ApiResponse<List<LoanNoteResponse>>> getNotes(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(loanService.getNotes(id)));
    }

    @PostMapping("/{id}/notes")
    public ResponseEntity<ApiResponse<LoanNoteResponse>> addNote(
            @PathVariable Long id, @Valid @RequestBody LoanNoteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Note added", loanService.addNote(id, request)));
    }

    @GetMapping("/{id}/schedules")
    public ResponseEntity<ApiResponse<List<LoanScheduleResponse>>> getSchedules(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(loanService.getSchedules(id)));
    }

    @GetMapping("/{id}/schedules/{scheduleId}/details")
    public ResponseEntity<ApiResponse<List<LoanScheduleInstallmentResponse>>> getScheduleInstallments(
            @PathVariable Long id, @PathVariable Long scheduleId) {
        return ResponseEntity.ok(ApiResponse.success(loanService.getScheduleInstallments(id, scheduleId)));
    }

    @GetMapping("/{id}/payments")
    public ResponseEntity<ApiResponse<List<LoanPaymentResponse>>> getPayments(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(loanService.getPayments(id)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/payments")
    public ResponseEntity<ApiResponse<LoanPaymentResponse>> addPayment(
            @PathVariable Long id, @Valid @RequestBody LoanPaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment recorded", loanService.addPayment(id, request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/payments/{paymentId}/reverse")
    public ResponseEntity<ApiResponse<LoanPaymentResponse>> reversePayment(
            @PathVariable Long id, @PathVariable Long paymentId,
            @Valid @RequestBody LoanPaymentReverseRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Reversal requested", loanService.reversePayment(id, paymentId, request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/payments/{paymentId}/reverse/approve")
    public ResponseEntity<ApiResponse<LoanPaymentResponse>> approvePaymentReversal(
            @PathVariable Long id, @PathVariable Long paymentId) {
        return ResponseEntity.ok(
                ApiResponse.success("Reversal approved", loanService.approvePaymentReversal(id, paymentId)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/payments/{paymentId}/reverse/reject")
    public ResponseEntity<ApiResponse<LoanPaymentResponse>> rejectPaymentReversal(
            @PathVariable Long id, @PathVariable Long paymentId,
            @Valid @RequestBody LoanPaymentReversalRejectRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Reversal rejected", loanService.rejectPaymentReversal(id, paymentId, request)));
    }

    // What it actually costs to close this loan today — see LoanServiceImpl.computePayoffQuote
    // for why this differs from (is lower than) the loan's outstandingBalance.
    @GetMapping("/{id}/payoff-quote")
    public ResponseEntity<ApiResponse<LoanPayoffQuoteResponse>> getPayoffQuote(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(loanService.getPayoffQuote(id)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/payoff")
    public ResponseEntity<ApiResponse<LoanResponse>> payoff(
            @PathVariable Long id, @Valid @RequestBody LoanPayoffRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Loan paid off", loanService.payoff(id, request)));
    }

    @GetMapping("/{id}/interest")
    public ResponseEntity<ApiResponse<List<LoanInterestResponse>>> getInterestAccruals(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(loanService.getInterestAccruals(id)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/interest")
    public ResponseEntity<ApiResponse<LoanInterestResponse>> addInterestAccrual(
            @PathVariable Long id, @Valid @RequestBody LoanInterestRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Interest accrual recorded", loanService.addInterestAccrual(id, request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/interest/{accrualId}")
    public ResponseEntity<ApiResponse<LoanInterestResponse>> updateInterestAccrual(
            @PathVariable Long id, @PathVariable Long accrualId,
            @Valid @RequestBody LoanInterestRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Interest accrual updated", loanService.updateInterestAccrual(id, accrualId, request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}/interest/{accrualId}")
    public ResponseEntity<ApiResponse<Void>> deleteInterestAccrual(
            @PathVariable Long id, @PathVariable Long accrualId) {
        loanService.deleteInterestAccrual(id, accrualId);
        return ResponseEntity.ok(ApiResponse.success("Interest accrual deleted", null));
    }

    @GetMapping("/interest")
    public ResponseEntity<PageResponse<LoanInterestResponse>> getAllInterestAccruals(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder) {
        return ResponseEntity.ok(loanService.getAllInterestAccruals(page, size, sortBy, sortOrder));
    }

    @GetMapping("/{id}/penalties")
    public ResponseEntity<ApiResponse<List<LoanPenaltyResponse>>> getPenalties(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(loanService.getPenalties(id)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/penalties")
    public ResponseEntity<ApiResponse<LoanPenaltyResponse>> addPenalty(
            @PathVariable Long id, @Valid @RequestBody LoanPenaltyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Penalty added", loanService.addPenalty(id, request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/penalties/{penaltyId}")
    public ResponseEntity<ApiResponse<LoanPenaltyResponse>> updatePenalty(
            @PathVariable Long id, @PathVariable Long penaltyId,
            @Valid @RequestBody LoanPenaltyRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Penalty updated", loanService.updatePenalty(id, penaltyId, request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}/penalties/{penaltyId}")
    public ResponseEntity<ApiResponse<Void>> deletePenalty(
            @PathVariable Long id, @PathVariable Long penaltyId) {
        loanService.deletePenalty(id, penaltyId);
        return ResponseEntity.ok(ApiResponse.success("Penalty deleted", null));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/penalties/{penaltyId}/pay")
    public ResponseEntity<ApiResponse<LoanPenaltyResponse>> payPenalty(
            @PathVariable Long id, @PathVariable Long penaltyId) {
        return ResponseEntity.ok(ApiResponse.success("Penalty marked as paid", loanService.payPenalty(id, penaltyId)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/penalties/{penaltyId}/waive")
    public ResponseEntity<ApiResponse<LoanPenaltyResponse>> waivePenalty(
            @PathVariable Long id, @PathVariable Long penaltyId) {
        return ResponseEntity.ok(ApiResponse.success("Penalty waived", loanService.waivePenalty(id, penaltyId)));
    }

    @GetMapping("/penalties")
    public ResponseEntity<PageResponse<LoanPenaltyResponse>> getAllPenalties(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder) {
        return ResponseEntity.ok(loanService.getAllPenalties(page, size, sortBy, sortOrder));
    }

    @GetMapping("/{id}/fees")
    public ResponseEntity<ApiResponse<List<LoanFeeResponse>>> getFees(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(loanService.getFees(id)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/fees")
    public ResponseEntity<ApiResponse<LoanFeeResponse>> addFee(
            @PathVariable Long id, @Valid @RequestBody LoanFeeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Fee added", loanService.addFee(id, request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/fees/{feeId}")
    public ResponseEntity<ApiResponse<LoanFeeResponse>> updateFee(
            @PathVariable Long id, @PathVariable Long feeId, @Valid @RequestBody LoanFeeRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Fee updated", loanService.updateFee(id, feeId, request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}/fees/{feeId}")
    public ResponseEntity<ApiResponse<Void>> deleteFee(@PathVariable Long id, @PathVariable Long feeId) {
        loanService.deleteFee(id, feeId);
        return ResponseEntity.ok(ApiResponse.success("Fee deleted", null));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/fees/{feeId}/pay")
    public ResponseEntity<ApiResponse<LoanFeeResponse>> payFee(
            @PathVariable Long id, @PathVariable Long feeId) {
        return ResponseEntity.ok(ApiResponse.success("Fee marked as paid", loanService.payFee(id, feeId)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/fees/{feeId}/waive")
    public ResponseEntity<ApiResponse<LoanFeeResponse>> waiveFee(
            @PathVariable Long id, @PathVariable Long feeId) {
        return ResponseEntity.ok(ApiResponse.success("Fee waived", loanService.waiveFee(id, feeId)));
    }

    @GetMapping("/{id}/restructures")
    public ResponseEntity<ApiResponse<List<LoanRestructureResponse>>> getRestructures(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(loanService.getRestructures(id)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/restructures")
    public ResponseEntity<ApiResponse<LoanRestructureResponse>> addRestructure(
            @PathVariable Long id, @Valid @RequestBody LoanRestructureRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Restructure requested", loanService.addRestructure(id, request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/restructures/{restructureId}/approve")
    public ResponseEntity<ApiResponse<LoanRestructureResponse>> approveRestructure(
            @PathVariable Long id, @PathVariable Long restructureId) {
        return ResponseEntity.ok(
                ApiResponse.success("Restructure approved", loanService.approveRestructure(id, restructureId)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/restructures/{restructureId}/reject")
    public ResponseEntity<ApiResponse<LoanRestructureResponse>> rejectRestructure(
            @PathVariable Long id, @PathVariable Long restructureId,
            @Valid @RequestBody LoanRestructureRejectRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Restructure rejected", loanService.rejectRestructure(id, restructureId, request)));
    }

    @GetMapping("/restructures")
    public ResponseEntity<PageResponse<LoanRestructureResponse>> getAllRestructures(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder) {
        return ResponseEntity.ok(loanService.getAllRestructures(page, size, sortBy, sortOrder));
    }

    @GetMapping("/{id}/refinances")
    public ResponseEntity<ApiResponse<List<LoanRefinanceResponse>>> getRefinances(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(loanService.getRefinances(id)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/refinances")
    public ResponseEntity<ApiResponse<LoanRefinanceResponse>> addRefinance(
            @PathVariable Long id, @Valid @RequestBody LoanRefinanceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Refinance recorded", loanService.addRefinance(id, request)));
    }

    @GetMapping("/refinances")
    public ResponseEntity<PageResponse<LoanRefinanceResponse>> getAllRefinances(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder) {
        return ResponseEntity.ok(loanService.getAllRefinances(page, size, sortBy, sortOrder));
    }

    @GetMapping("/{id}/settlement")
    public ResponseEntity<ApiResponse<LoanSettlementResponse>> getSettlement(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(loanService.getSettlement(id)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/settlement")
    public ResponseEntity<ApiResponse<LoanSettlementResponse>> addSettlement(
            @PathVariable Long id, @Valid @RequestBody LoanSettlementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Settlement recorded", loanService.addSettlement(id, request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/settlement/complete")
    public ResponseEntity<ApiResponse<LoanSettlementResponse>> completeSettlement(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Settlement completed", loanService.completeSettlement(id)));
    }

    @GetMapping("/{id}/writeoff")
    public ResponseEntity<ApiResponse<LoanWriteoffResponse>> getWriteoff(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(loanService.getWriteoff(id)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/writeoff")
    public ResponseEntity<ApiResponse<LoanWriteoffResponse>> addWriteoff(
            @PathVariable Long id, @Valid @RequestBody LoanWriteoffRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Write-off recorded", loanService.addWriteoff(id, request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/writeoff/complete")
    public ResponseEntity<ApiResponse<LoanWriteoffResponse>> completeWriteoff(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Write-off completed", loanService.completeWriteoff(id)));
    }

    @GetMapping("/writeoffs")
    public ResponseEntity<PageResponse<LoanWriteoffResponse>> getAllWriteoffs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder) {
        return ResponseEntity.ok(loanService.getAllWriteoffs(page, size, sortBy, sortOrder));
    }

    @GetMapping("/{id}/writeoff/recoveries")
    public ResponseEntity<ApiResponse<List<LoanWriteoffRecoveryResponse>>> getWriteoffRecoveries(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(loanService.getWriteoffRecoveries(id)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/writeoff/recoveries")
    public ResponseEntity<ApiResponse<LoanWriteoffRecoveryResponse>> recordWriteoffRecovery(
            @PathVariable Long id, @Valid @RequestBody LoanWriteoffRecoveryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Recovery recorded", loanService.recordWriteoffRecovery(id, request)));
    }

    @GetMapping("/{id}/adjustments")
    public ResponseEntity<ApiResponse<List<LoanAdjustmentResponse>>> getAdjustments(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(loanService.getAdjustments(id)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/adjustments")
    public ResponseEntity<ApiResponse<LoanAdjustmentResponse>> addAdjustment(
            @PathVariable Long id, @Valid @RequestBody LoanAdjustmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Adjustment applied", loanService.addAdjustment(id, request)));
    }

    @GetMapping("/{id}/transactions")
    public ResponseEntity<ApiResponse<List<LoanTransactionResponse>>> getTransactions(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(loanService.getTransactions(id)));
    }
}
