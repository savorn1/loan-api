package com.example.loan.controller;

import com.example.loan.common.ApiResponse;
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
import com.example.loan.service.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping
    public ResponseEntity<PageResponse<LoanResponse>> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder) {
        return ResponseEntity.ok(loanService.getAll(page, size, sortBy, sortOrder));
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
    @PutMapping("/{id}/collaterals/{collateralId}/release")
    public ResponseEntity<ApiResponse<LoanCollateralResponse>> releaseCollateral(
            @PathVariable Long id, @PathVariable Long collateralId) {
        return ResponseEntity.ok(ApiResponse.success("Collateral released", loanService.releaseCollateral(id, collateralId)));
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
                .body(ApiResponse.success("Loan restructured", loanService.addRestructure(id, request)));
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
