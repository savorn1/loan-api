package com.example.customer.service;

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

import java.util.List;

public interface CustomerService {

    CustomerResponse create(CustomerRequest request);

    CustomerResponse getById(Long id);

    PageResponse<CustomerResponse> list(CustomerFilterRequest filter);

    CustomerResponse update(Long id, CustomerRequest request);

    void delete(Long id);

    List<CustomerIdentityResponse> getIdentities(Long customerId);

    CustomerIdentityResponse addIdentity(Long customerId, CustomerIdentityRequest request);

    CustomerIdentityResponse updateIdentity(Long customerId, Long identityId, CustomerIdentityRequest request);

    void deleteIdentity(Long customerId, Long identityId);

    List<CustomerAddressResponse> getAddresses(Long customerId);

    CustomerAddressResponse addAddress(Long customerId, CustomerAddressRequest request);

    CustomerAddressResponse updateAddress(Long customerId, Long addressId, CustomerAddressRequest request);

    void deleteAddress(Long customerId, Long addressId);

    List<CustomerContactResponse> getContacts(Long customerId);

    CustomerContactResponse addContact(Long customerId, CustomerContactRequest request);

    CustomerContactResponse updateContact(Long customerId, Long contactId, CustomerContactRequest request);

    void deleteContact(Long customerId, Long contactId);

    List<CustomerEmploymentResponse> getEmployments(Long customerId);

    CustomerEmploymentResponse addEmployment(Long customerId, CustomerEmploymentRequest request);

    CustomerEmploymentResponse updateEmployment(Long customerId, Long employmentId, CustomerEmploymentRequest request);

    void deleteEmployment(Long customerId, Long employmentId);

    List<CustomerIncomeResponse> getIncomes(Long customerId);

    CustomerIncomeResponse addIncome(Long customerId, CustomerIncomeRequest request);

    CustomerIncomeResponse updateIncome(Long customerId, Long incomeId, CustomerIncomeRequest request);

    void deleteIncome(Long customerId, Long incomeId);

    List<CustomerRelationshipResponse> getRelationships(Long customerId);

    CustomerRelationshipResponse addRelationship(Long customerId, CustomerRelationshipRequest request);

    CustomerRelationshipResponse updateRelationship(Long customerId, Long relationshipId, CustomerRelationshipRequest request);

    void deleteRelationship(Long customerId, Long relationshipId);

    // Singleton sub-resource — at most one per customer. Returns null (not a
    // 404) when the customer has no risk profile yet; upsertRiskProfile
    // creates it on first save and updates it thereafter.
    CustomerRiskProfileResponse getRiskProfile(Long customerId);

    CustomerRiskProfileResponse upsertRiskProfile(Long customerId, CustomerRiskProfileRequest request);

    void deleteRiskProfile(Long customerId);

    // Singleton sub-resource — at most one per customer, same upsert shape as
    // the risk profile.
    CustomerPreferenceResponse getPreferences(Long customerId);

    CustomerPreferenceResponse upsertPreferences(Long customerId, CustomerPreferenceRequest request);

    void deletePreferences(Long customerId);

    // Read-only — nothing currently writes rows here (see CustomerAuditLog).
    List<CustomerAuditLogResponse> getAuditLogs(Long customerId);
}
