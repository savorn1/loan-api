package com.example.customer.controller;

import com.example.customer.annotation.RateLimit;
import com.example.customer.common.ApiResponse;
import com.example.customer.common.PageResponse;
import com.example.customer.dto.CustomerAddressRequest;
import com.example.customer.dto.CustomerAddressResponse;
import com.example.customer.dto.CustomerAuditLogResponse;
import com.example.customer.dto.CustomerContactRequest;
import com.example.customer.dto.CustomerContactResponse;
import com.example.customer.dto.CustomerEmploymentRequest;
import com.example.customer.dto.CustomerEmploymentResponse;
import com.example.customer.dto.CustomerFilterRequest;
import com.example.customer.dto.CustomerIdentityRequest;
import com.example.customer.dto.CustomerIdentityResponse;
import com.example.customer.dto.CustomerIncomeRequest;
import com.example.customer.dto.CustomerIncomeResponse;
import com.example.customer.dto.CustomerPreferenceRequest;
import com.example.customer.dto.CustomerPreferenceResponse;
import com.example.customer.dto.CustomerRelationshipRequest;
import com.example.customer.dto.CustomerRelationshipResponse;
import com.example.customer.dto.CustomerRequest;
import com.example.customer.dto.CustomerResponse;
import com.example.customer.dto.CustomerRiskProfileRequest;
import com.example.customer.dto.CustomerRiskProfileResponse;
import com.example.customer.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    // Only endpoint in this service actually wired to @RateLimit/RateLimitAspect — the
    // annotation and its Redis-backed aspect existed but were never applied to anything,
    // so the rate limiter was dead code. Customer creation is the natural candidate: it's
    // unauthenticated-adjacent (any authenticated user, not just admins) and writes to the DB.
    @RateLimit(max = 20, window = 60)
    @PostMapping
    public ResponseEntity<ApiResponse<CustomerResponse>> create(@Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Customer created", customerService.create(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(customerService.getById(id)));
    }

    @GetMapping
    public ResponseEntity<PageResponse<CustomerResponse>> getAll(@ModelAttribute CustomerFilterRequest filter) {
        return ResponseEntity.ok(customerService.list(filter));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> update(@PathVariable Long id,
                                                                  @Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Customer updated", customerService.update(id, request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        customerService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Customer deleted", null));
    }

    @GetMapping("/{id}/identities")
    public ResponseEntity<ApiResponse<List<CustomerIdentityResponse>>> getIdentities(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(customerService.getIdentities(id)));
    }

    @PostMapping("/{id}/identities")
    public ResponseEntity<ApiResponse<CustomerIdentityResponse>> addIdentity(@PathVariable Long id,
                                                                               @Valid @RequestBody CustomerIdentityRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Identity added", customerService.addIdentity(id, request)));
    }

    @PutMapping("/{id}/identities/{identityId}")
    public ResponseEntity<ApiResponse<CustomerIdentityResponse>> updateIdentity(@PathVariable Long id,
                                                                                  @PathVariable Long identityId,
                                                                                  @Valid @RequestBody CustomerIdentityRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Identity updated", customerService.updateIdentity(id, identityId, request)));
    }

    @DeleteMapping("/{id}/identities/{identityId}")
    public ResponseEntity<ApiResponse<Void>> deleteIdentity(@PathVariable Long id, @PathVariable Long identityId) {
        customerService.deleteIdentity(id, identityId);
        return ResponseEntity.ok(ApiResponse.success("Identity deleted", null));
    }

    @PostMapping(value = "/{id}/identities/{identityId}/scan", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CustomerIdentityResponse>> uploadIdentityScan(
            @PathVariable Long id, @PathVariable Long identityId, @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(
                ApiResponse.success("Scan uploaded", customerService.uploadIdentityScan(id, identityId, file)));
    }

    @DeleteMapping("/{id}/identities/{identityId}/scan")
    public ResponseEntity<ApiResponse<CustomerIdentityResponse>> deleteIdentityScan(
            @PathVariable Long id, @PathVariable Long identityId) {
        return ResponseEntity.ok(
                ApiResponse.success("Scan removed", customerService.deleteIdentityScan(id, identityId)));
    }

    @GetMapping("/{id}/addresses")
    public ResponseEntity<ApiResponse<List<CustomerAddressResponse>>> getAddresses(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(customerService.getAddresses(id)));
    }

    @PostMapping("/{id}/addresses")
    public ResponseEntity<ApiResponse<CustomerAddressResponse>> addAddress(@PathVariable Long id,
                                                                             @Valid @RequestBody CustomerAddressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Address added", customerService.addAddress(id, request)));
    }

    @PutMapping("/{id}/addresses/{addressId}")
    public ResponseEntity<ApiResponse<CustomerAddressResponse>> updateAddress(@PathVariable Long id,
                                                                                @PathVariable Long addressId,
                                                                                @Valid @RequestBody CustomerAddressRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Address updated", customerService.updateAddress(id, addressId, request)));
    }

    @DeleteMapping("/{id}/addresses/{addressId}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(@PathVariable Long id, @PathVariable Long addressId) {
        customerService.deleteAddress(id, addressId);
        return ResponseEntity.ok(ApiResponse.success("Address deleted", null));
    }

    @GetMapping("/{id}/contacts")
    public ResponseEntity<ApiResponse<List<CustomerContactResponse>>> getContacts(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(customerService.getContacts(id)));
    }

    @PostMapping("/{id}/contacts")
    public ResponseEntity<ApiResponse<CustomerContactResponse>> addContact(@PathVariable Long id,
                                                                             @Valid @RequestBody CustomerContactRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Contact added", customerService.addContact(id, request)));
    }

    @PutMapping("/{id}/contacts/{contactId}")
    public ResponseEntity<ApiResponse<CustomerContactResponse>> updateContact(@PathVariable Long id,
                                                                                @PathVariable Long contactId,
                                                                                @Valid @RequestBody CustomerContactRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Contact updated", customerService.updateContact(id, contactId, request)));
    }

    @DeleteMapping("/{id}/contacts/{contactId}")
    public ResponseEntity<ApiResponse<Void>> deleteContact(@PathVariable Long id, @PathVariable Long contactId) {
        customerService.deleteContact(id, contactId);
        return ResponseEntity.ok(ApiResponse.success("Contact deleted", null));
    }

    @GetMapping("/{id}/employments")
    public ResponseEntity<ApiResponse<List<CustomerEmploymentResponse>>> getEmployments(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(customerService.getEmployments(id)));
    }

    @PostMapping("/{id}/employments")
    public ResponseEntity<ApiResponse<CustomerEmploymentResponse>> addEmployment(@PathVariable Long id,
                                                                                   @Valid @RequestBody CustomerEmploymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Employment added", customerService.addEmployment(id, request)));
    }

    @PutMapping("/{id}/employments/{employmentId}")
    public ResponseEntity<ApiResponse<CustomerEmploymentResponse>> updateEmployment(@PathVariable Long id,
                                                                                      @PathVariable Long employmentId,
                                                                                      @Valid @RequestBody CustomerEmploymentRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Employment updated", customerService.updateEmployment(id, employmentId, request)));
    }

    @DeleteMapping("/{id}/employments/{employmentId}")
    public ResponseEntity<ApiResponse<Void>> deleteEmployment(@PathVariable Long id, @PathVariable Long employmentId) {
        customerService.deleteEmployment(id, employmentId);
        return ResponseEntity.ok(ApiResponse.success("Employment deleted", null));
    }

    @GetMapping("/{id}/incomes")
    public ResponseEntity<ApiResponse<List<CustomerIncomeResponse>>> getIncomes(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(customerService.getIncomes(id)));
    }

    @PostMapping("/{id}/incomes")
    public ResponseEntity<ApiResponse<CustomerIncomeResponse>> addIncome(@PathVariable Long id,
                                                                           @Valid @RequestBody CustomerIncomeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Income added", customerService.addIncome(id, request)));
    }

    @PutMapping("/{id}/incomes/{incomeId}")
    public ResponseEntity<ApiResponse<CustomerIncomeResponse>> updateIncome(@PathVariable Long id,
                                                                              @PathVariable Long incomeId,
                                                                              @Valid @RequestBody CustomerIncomeRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Income updated", customerService.updateIncome(id, incomeId, request)));
    }

    @DeleteMapping("/{id}/incomes/{incomeId}")
    public ResponseEntity<ApiResponse<Void>> deleteIncome(@PathVariable Long id, @PathVariable Long incomeId) {
        customerService.deleteIncome(id, incomeId);
        return ResponseEntity.ok(ApiResponse.success("Income deleted", null));
    }

    @GetMapping("/{id}/relationships")
    public ResponseEntity<ApiResponse<List<CustomerRelationshipResponse>>> getRelationships(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(customerService.getRelationships(id)));
    }

    @PostMapping("/{id}/relationships")
    public ResponseEntity<ApiResponse<CustomerRelationshipResponse>> addRelationship(@PathVariable Long id,
                                                                                       @Valid @RequestBody CustomerRelationshipRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Relationship added", customerService.addRelationship(id, request)));
    }

    @PutMapping("/{id}/relationships/{relationshipId}")
    public ResponseEntity<ApiResponse<CustomerRelationshipResponse>> updateRelationship(@PathVariable Long id,
                                                                                          @PathVariable Long relationshipId,
                                                                                          @Valid @RequestBody CustomerRelationshipRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Relationship updated", customerService.updateRelationship(id, relationshipId, request)));
    }

    @DeleteMapping("/{id}/relationships/{relationshipId}")
    public ResponseEntity<ApiResponse<Void>> deleteRelationship(@PathVariable Long id, @PathVariable Long relationshipId) {
        customerService.deleteRelationship(id, relationshipId);
        return ResponseEntity.ok(ApiResponse.success("Relationship deleted", null));
    }

    // Singleton — no {itemId} in the path. GET returns data: null rather than
    // 404 when the customer has no risk profile yet; PUT upserts.
    @GetMapping("/{id}/risk-profile")
    public ResponseEntity<ApiResponse<CustomerRiskProfileResponse>> getRiskProfile(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(customerService.getRiskProfile(id)));
    }

    @PutMapping("/{id}/risk-profile")
    public ResponseEntity<ApiResponse<CustomerRiskProfileResponse>> upsertRiskProfile(@PathVariable Long id,
                                                                                        @Valid @RequestBody CustomerRiskProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Risk profile saved", customerService.upsertRiskProfile(id, request)));
    }

    @DeleteMapping("/{id}/risk-profile")
    public ResponseEntity<ApiResponse<Void>> deleteRiskProfile(@PathVariable Long id) {
        customerService.deleteRiskProfile(id);
        return ResponseEntity.ok(ApiResponse.success("Risk profile deleted", null));
    }

    // Singleton — same upsert shape as risk-profile.
    @GetMapping("/{id}/preferences")
    public ResponseEntity<ApiResponse<CustomerPreferenceResponse>> getPreferences(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(customerService.getPreferences(id)));
    }

    @PutMapping("/{id}/preferences")
    public ResponseEntity<ApiResponse<CustomerPreferenceResponse>> upsertPreferences(@PathVariable Long id,
                                                                                       @Valid @RequestBody CustomerPreferenceRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Preferences saved", customerService.upsertPreferences(id, request)));
    }

    @DeleteMapping("/{id}/preferences")
    public ResponseEntity<ApiResponse<Void>> deletePreferences(@PathVariable Long id) {
        customerService.deletePreferences(id);
        return ResponseEntity.ok(ApiResponse.success("Preferences deleted", null));
    }

    // Read-only — no POST/PUT/DELETE. Nothing writes audit log rows yet
    // (see CustomerAuditLog); this exists so the read path is ready.
    @GetMapping("/{id}/audit-logs")
    public ResponseEntity<ApiResponse<List<CustomerAuditLogResponse>>> getAuditLogs(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(customerService.getAuditLogs(id)));
    }
}
