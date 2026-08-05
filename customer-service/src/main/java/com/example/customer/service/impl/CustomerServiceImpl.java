package com.example.customer.service.impl;

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
import com.example.customer.entity.Customer;
import com.example.customer.entity.CustomerAddress;
import com.example.customer.entity.CustomerAuditLog;
import com.example.customer.entity.CustomerContact;
import com.example.customer.entity.CustomerEmployment;
import com.example.customer.entity.CustomerIdentity;
import com.example.customer.entity.CustomerIncome;
import com.example.customer.entity.CustomerPreference;
import com.example.customer.entity.CustomerRelationship;
import com.example.customer.entity.CustomerRiskProfile;
import com.example.customer.entity.CustomerStatus;
import com.example.customer.entity.CustomerType;
import com.example.customer.entity.EmploymentStatus;
import com.example.customer.entity.IdentityStatus;
import com.example.customer.exception.AppException;
import com.example.customer.exception.ResourceNotFoundException;
import com.example.customer.repository.CustomerAddressRepository;
import com.example.customer.repository.CustomerAuditLogRepository;
import com.example.customer.repository.CustomerContactRepository;
import com.example.customer.repository.CustomerEmploymentRepository;
import com.example.customer.repository.CustomerIdentityRepository;
import com.example.customer.repository.CustomerIncomeRepository;
import com.example.customer.repository.CustomerPreferenceRepository;
import com.example.customer.repository.CustomerRelationshipRepository;
import com.example.customer.repository.CustomerRepository;
import com.example.customer.repository.CustomerRiskProfileRepository;
import com.example.customer.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerIdentityRepository customerIdentityRepository;
    private final CustomerAddressRepository customerAddressRepository;
    private final CustomerContactRepository customerContactRepository;
    private final CustomerEmploymentRepository customerEmploymentRepository;
    private final CustomerIncomeRepository customerIncomeRepository;
    private final CustomerRelationshipRepository customerRelationshipRepository;
    private final CustomerRiskProfileRepository customerRiskProfileRepository;
    private final CustomerPreferenceRepository customerPreferenceRepository;
    private final CustomerAuditLogRepository customerAuditLogRepository;

    @Override
    public CustomerResponse create(CustomerRequest request) {
        validateByType(request);
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new AppException(HttpStatus.CONFLICT, "Email already in use: " + request.getEmail());
        }
        if (request.getNationalId() != null && customerRepository.existsByNationalId(request.getNationalId())) {
            throw new AppException(HttpStatus.CONFLICT, "National ID already in use: " + request.getNationalId());
        }

        Customer customer = Customer.builder()
                .customerType(request.getCustomerType())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .fullName(resolveFullName(request))
                .gender(request.getGender())
                .email(request.getEmail())
                .phone(request.getPhone())
                .nationalId(request.getNationalId())
                .address(request.getAddress())
                .dateOfBirth(request.getDateOfBirth())
                .nationality(request.getNationality())
                .maritalStatus(request.getMaritalStatus())
                .status(request.getStatus() != null ? request.getStatus() : CustomerStatus.ACTIVE)
                .branchId(request.getBranchId())
                .build();

        customer = customerRepository.save(customer);
        customer.setCustomerNo(generateCustomerNo(customer));
        customer = customerRepository.save(customer);

        return toResponse(customer);
    }

    @Override
    public CustomerResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    public PageResponse<CustomerResponse> list(CustomerFilterRequest filter) {
        List<Specification<Customer>> conditions = new ArrayList<>();

        if (filter.getSearch() != null && !filter.getSearch().isBlank()) {
            String like = "%" + filter.getSearch().toLowerCase() + "%";
            conditions.add((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("customerNo")), like),
                    cb.like(cb.lower(root.get("firstName")), like),
                    cb.like(cb.lower(root.get("lastName")), like),
                    cb.like(cb.lower(root.get("fullName")), like),
                    cb.like(cb.lower(root.get("email")), like)));
        }
        if (filter.getCustomerType() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("customerType"), filter.getCustomerType()));
        }
        if (filter.getStatus() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("status"), filter.getStatus()));
        }
        if (filter.getBranchId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("branchId"), filter.getBranchId()));
        }
        if (filter.getDateFrom() != null) {
            conditions.add((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("createdAt"), filter.getDateFrom().atStartOfDay()));
        }
        if (filter.getDateTo() != null) {
            conditions.add((root, query, cb) ->
                    cb.lessThan(root.get("createdAt"), filter.getDateTo().plusDays(1).atStartOfDay()));
        }

        Specification<Customer> spec = Specification.allOf(conditions);

        Sort sort = "asc".equalsIgnoreCase(filter.getSortOrder())
                ? Sort.by(filter.getSortBy()).ascending()
                : Sort.by(filter.getSortBy()).descending();
        Pageable pageable = PageRequest.of(Math.max(filter.getPage() - 1, 0), filter.getSize(), sort);

        Page<CustomerResponse> page = customerRepository.findAll(spec, pageable).map(this::toResponse);
        return PageResponse.of(page);
    }

    @Override
    public CustomerResponse update(Long id, CustomerRequest request) {
        validateByType(request);
        Customer customer = findOrThrow(id);

        if (!customer.getEmail().equals(request.getEmail()) && customerRepository.existsByEmail(request.getEmail())) {
            throw new AppException(HttpStatus.CONFLICT, "Email already in use: " + request.getEmail());
        }
        if (request.getNationalId() != null
                && !request.getNationalId().equals(customer.getNationalId())
                && customerRepository.existsByNationalId(request.getNationalId())) {
            throw new AppException(HttpStatus.CONFLICT, "National ID already in use: " + request.getNationalId());
        }

        customer.setCustomerType(request.getCustomerType());
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setFullName(resolveFullName(request));
        customer.setGender(request.getGender());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setNationalId(request.getNationalId());
        customer.setAddress(request.getAddress());
        customer.setDateOfBirth(request.getDateOfBirth());
        customer.setNationality(request.getNationality());
        customer.setMaritalStatus(request.getMaritalStatus());
        customer.setStatus(request.getStatus() != null ? request.getStatus() : customer.getStatus());
        customer.setBranchId(request.getBranchId());

        return toResponse(customerRepository.save(customer));
    }

    @Override
    public void delete(Long id) {
        customerRepository.delete(findOrThrow(id));
    }

    @Override
    public List<CustomerIdentityResponse> getIdentities(Long customerId) {
        findOrThrow(customerId);
        return customerIdentityRepository.findByCustomerIdOrderByCreatedAtAsc(customerId).stream()
                .map(this::toIdentityResponse)
                .toList();
    }

    @Override
    public CustomerIdentityResponse addIdentity(Long customerId, CustomerIdentityRequest request) {
        Customer customer = findOrThrow(customerId);
        if (customerIdentityRepository.existsByCustomerIdAndIdentityTypeAndIdentityNumber(
                customerId, request.getIdentityType(), request.getIdentityNumber())) {
            throw new AppException(HttpStatus.CONFLICT,
                    "This customer already has a " + request.getIdentityType() + " with that number");
        }

        CustomerIdentity identity = CustomerIdentity.builder()
                .customer(customer)
                .identityType(request.getIdentityType())
                .identityNumber(request.getIdentityNumber())
                .issueDate(request.getIssueDate())
                .expiryDate(request.getExpiryDate())
                .issuingCountry(request.getIssuingCountry())
                .status(request.getStatus() != null ? request.getStatus() : IdentityStatus.ACTIVE)
                .build();

        return toIdentityResponse(customerIdentityRepository.save(identity));
    }

    @Override
    public CustomerIdentityResponse updateIdentity(Long customerId, Long identityId, CustomerIdentityRequest request) {
        findOrThrow(customerId);
        CustomerIdentity identity = findIdentityOrThrow(customerId, identityId);

        if ((request.getIdentityType() != identity.getIdentityType()
                || !request.getIdentityNumber().equals(identity.getIdentityNumber()))
                && customerIdentityRepository.existsByCustomerIdAndIdentityTypeAndIdentityNumber(
                        customerId, request.getIdentityType(), request.getIdentityNumber())) {
            throw new AppException(HttpStatus.CONFLICT,
                    "This customer already has a " + request.getIdentityType() + " with that number");
        }

        identity.setIdentityType(request.getIdentityType());
        identity.setIdentityNumber(request.getIdentityNumber());
        identity.setIssueDate(request.getIssueDate());
        identity.setExpiryDate(request.getExpiryDate());
        identity.setIssuingCountry(request.getIssuingCountry());
        identity.setStatus(request.getStatus() != null ? request.getStatus() : identity.getStatus());

        return toIdentityResponse(customerIdentityRepository.save(identity));
    }

    @Override
    public void deleteIdentity(Long customerId, Long identityId) {
        findOrThrow(customerId);
        customerIdentityRepository.delete(findIdentityOrThrow(customerId, identityId));
    }

    @Override
    public List<CustomerAddressResponse> getAddresses(Long customerId) {
        findOrThrow(customerId);
        return customerAddressRepository.findByCustomerIdOrderByCreatedAtAsc(customerId).stream()
                .map(this::toAddressResponse)
                .toList();
    }

    @Override
    public CustomerAddressResponse addAddress(Long customerId, CustomerAddressRequest request) {
        Customer customer = findOrThrow(customerId);
        boolean primary = Boolean.TRUE.equals(request.getIsPrimary());
        if (primary) {
            clearOtherPrimaryAddresses(customerId, null);
        }

        CustomerAddress address = CustomerAddress.builder()
                .customer(customer)
                .addressType(request.getAddressType())
                .country(request.getCountry())
                .province(request.getProvince())
                .district(request.getDistrict())
                .commune(request.getCommune())
                .village(request.getVillage())
                .street(request.getStreet())
                .postalCode(request.getPostalCode())
                .isPrimary(primary)
                .build();

        return toAddressResponse(customerAddressRepository.save(address));
    }

    @Override
    public CustomerAddressResponse updateAddress(Long customerId, Long addressId, CustomerAddressRequest request) {
        findOrThrow(customerId);
        CustomerAddress address = findAddressOrThrow(customerId, addressId);

        boolean primary = Boolean.TRUE.equals(request.getIsPrimary());
        if (primary) {
            clearOtherPrimaryAddresses(customerId, addressId);
        }

        address.setAddressType(request.getAddressType());
        address.setCountry(request.getCountry());
        address.setProvince(request.getProvince());
        address.setDistrict(request.getDistrict());
        address.setCommune(request.getCommune());
        address.setVillage(request.getVillage());
        address.setStreet(request.getStreet());
        address.setPostalCode(request.getPostalCode());
        address.setPrimary(primary);

        return toAddressResponse(customerAddressRepository.save(address));
    }

    @Override
    public void deleteAddress(Long customerId, Long addressId) {
        findOrThrow(customerId);
        customerAddressRepository.delete(findAddressOrThrow(customerId, addressId));
    }

    // Only one address per customer may be primary — unsets the flag on every
    // other address of theirs before the caller saves the new primary one.
    // excludeAddressId skips the address being updated itself (already in
    // memory with the new value, no need to touch it here).
    private void clearOtherPrimaryAddresses(Long customerId, Long excludeAddressId) {
        List<CustomerAddress> addresses = customerAddressRepository.findByCustomerIdOrderByCreatedAtAsc(customerId);
        List<CustomerAddress> toClear = addresses.stream()
                .filter(a -> a.isPrimary() && !a.getId().equals(excludeAddressId))
                .peek(a -> a.setPrimary(false))
                .toList();
        customerAddressRepository.saveAll(toClear);
    }

    @Override
    public List<CustomerContactResponse> getContacts(Long customerId) {
        findOrThrow(customerId);
        return customerContactRepository.findByCustomerIdOrderByCreatedAtAsc(customerId).stream()
                .map(this::toContactResponse)
                .toList();
    }

    @Override
    public CustomerContactResponse addContact(Long customerId, CustomerContactRequest request) {
        Customer customer = findOrThrow(customerId);

        CustomerContact contact = CustomerContact.builder()
                .customer(customer)
                .name(request.getName())
                .relationship(request.getRelationship())
                .phone(request.getPhone())
                .email(request.getEmail())
                .address(request.getAddress())
                .build();

        return toContactResponse(customerContactRepository.save(contact));
    }

    @Override
    public CustomerContactResponse updateContact(Long customerId, Long contactId, CustomerContactRequest request) {
        findOrThrow(customerId);
        CustomerContact contact = findContactOrThrow(customerId, contactId);

        contact.setName(request.getName());
        contact.setRelationship(request.getRelationship());
        contact.setPhone(request.getPhone());
        contact.setEmail(request.getEmail());
        contact.setAddress(request.getAddress());

        return toContactResponse(customerContactRepository.save(contact));
    }

    @Override
    public void deleteContact(Long customerId, Long contactId) {
        findOrThrow(customerId);
        customerContactRepository.delete(findContactOrThrow(customerId, contactId));
    }

    @Override
    public List<CustomerEmploymentResponse> getEmployments(Long customerId) {
        findOrThrow(customerId);
        return customerEmploymentRepository.findByCustomerIdOrderByCreatedAtAsc(customerId).stream()
                .map(this::toEmploymentResponse)
                .toList();
    }

    @Override
    public CustomerEmploymentResponse addEmployment(Long customerId, CustomerEmploymentRequest request) {
        Customer customer = findOrThrow(customerId);

        CustomerEmployment employment = CustomerEmployment.builder()
                .customer(customer)
                .companyName(request.getCompanyName())
                .occupation(request.getOccupation())
                .employmentType(request.getEmploymentType())
                .salary(request.getSalary())
                .currency(request.getCurrency())
                .startDate(request.getStartDate())
                .status(request.getStatus() != null ? request.getStatus() : EmploymentStatus.ACTIVE)
                .build();

        return toEmploymentResponse(customerEmploymentRepository.save(employment));
    }

    @Override
    public CustomerEmploymentResponse updateEmployment(Long customerId, Long employmentId, CustomerEmploymentRequest request) {
        findOrThrow(customerId);
        CustomerEmployment employment = findEmploymentOrThrow(customerId, employmentId);

        employment.setCompanyName(request.getCompanyName());
        employment.setOccupation(request.getOccupation());
        employment.setEmploymentType(request.getEmploymentType());
        employment.setSalary(request.getSalary());
        employment.setCurrency(request.getCurrency());
        employment.setStartDate(request.getStartDate());
        employment.setStatus(request.getStatus() != null ? request.getStatus() : employment.getStatus());

        return toEmploymentResponse(customerEmploymentRepository.save(employment));
    }

    @Override
    public void deleteEmployment(Long customerId, Long employmentId) {
        findOrThrow(customerId);
        customerEmploymentRepository.delete(findEmploymentOrThrow(customerId, employmentId));
    }

    @Override
    public List<CustomerIncomeResponse> getIncomes(Long customerId) {
        findOrThrow(customerId);
        return customerIncomeRepository.findByCustomerIdOrderByCreatedAtAsc(customerId).stream()
                .map(this::toIncomeResponse)
                .toList();
    }

    @Override
    public CustomerIncomeResponse addIncome(Long customerId, CustomerIncomeRequest request) {
        Customer customer = findOrThrow(customerId);

        CustomerIncome income = CustomerIncome.builder()
                .customer(customer)
                .incomeType(request.getIncomeType())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .frequency(request.getFrequency())
                .build();

        return toIncomeResponse(customerIncomeRepository.save(income));
    }

    @Override
    public CustomerIncomeResponse updateIncome(Long customerId, Long incomeId, CustomerIncomeRequest request) {
        findOrThrow(customerId);
        CustomerIncome income = findIncomeOrThrow(customerId, incomeId);

        income.setIncomeType(request.getIncomeType());
        income.setAmount(request.getAmount());
        income.setCurrency(request.getCurrency());
        income.setFrequency(request.getFrequency());

        return toIncomeResponse(customerIncomeRepository.save(income));
    }

    @Override
    public void deleteIncome(Long customerId, Long incomeId) {
        findOrThrow(customerId);
        customerIncomeRepository.delete(findIncomeOrThrow(customerId, incomeId));
    }

    @Override
    public List<CustomerRelationshipResponse> getRelationships(Long customerId) {
        findOrThrow(customerId);
        return customerRelationshipRepository.findByCustomerIdOrderByCreatedAtAsc(customerId).stream()
                .map(this::toRelationshipResponse)
                .toList();
    }

    @Override
    public CustomerRelationshipResponse addRelationship(Long customerId, CustomerRelationshipRequest request) {
        Customer customer = findOrThrow(customerId);
        Customer relatedCustomer = validateRelationship(customerId, request, null);

        CustomerRelationship relationship = CustomerRelationship.builder()
                .customer(customer)
                .relatedCustomer(relatedCustomer)
                .relationshipType(request.getRelationshipType())
                .build();

        return toRelationshipResponse(customerRelationshipRepository.save(relationship));
    }

    @Override
    public CustomerRelationshipResponse updateRelationship(Long customerId, Long relationshipId, CustomerRelationshipRequest request) {
        findOrThrow(customerId);
        CustomerRelationship relationship = findRelationshipOrThrow(customerId, relationshipId);
        Customer relatedCustomer = validateRelationship(customerId, request, relationshipId);

        relationship.setRelatedCustomer(relatedCustomer);
        relationship.setRelationshipType(request.getRelationshipType());

        return toRelationshipResponse(customerRelationshipRepository.save(relationship));
    }

    @Override
    public void deleteRelationship(Long customerId, Long relationshipId) {
        findOrThrow(customerId);
        customerRelationshipRepository.delete(findRelationshipOrThrow(customerId, relationshipId));
    }

    @Override
    public CustomerRiskProfileResponse getRiskProfile(Long customerId) {
        findOrThrow(customerId);
        return customerRiskProfileRepository.findByCustomerId(customerId)
                .map(this::toRiskProfileResponse)
                .orElse(null);
    }

    @Override
    public CustomerRiskProfileResponse upsertRiskProfile(Long customerId, CustomerRiskProfileRequest request) {
        Customer customer = findOrThrow(customerId);
        CustomerRiskProfile profile = customerRiskProfileRepository.findByCustomerId(customerId)
                .orElseGet(() -> CustomerRiskProfile.builder().customer(customer).build());

        profile.setRiskLevel(request.getRiskLevel());
        profile.setPep(request.isPep());
        profile.setSanctionChecked(request.isSanctionChecked());
        profile.setAmlStatus(request.getAmlStatus());
        profile.setLastReviewDate(request.getLastReviewDate());

        return toRiskProfileResponse(customerRiskProfileRepository.save(profile));
    }

    @Override
    public void deleteRiskProfile(Long customerId) {
        findOrThrow(customerId);
        CustomerRiskProfile profile = customerRiskProfileRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerRiskProfile", customerId));
        customerRiskProfileRepository.delete(profile);
    }

    @Override
    public CustomerPreferenceResponse getPreferences(Long customerId) {
        findOrThrow(customerId);
        return customerPreferenceRepository.findByCustomerId(customerId)
                .map(this::toPreferenceResponse)
                .orElse(null);
    }

    @Override
    public CustomerPreferenceResponse upsertPreferences(Long customerId, CustomerPreferenceRequest request) {
        Customer customer = findOrThrow(customerId);
        CustomerPreference preference = customerPreferenceRepository.findByCustomerId(customerId)
                .orElseGet(() -> CustomerPreference.builder().customer(customer).build());

        preference.setLanguage(request.getLanguage());
        preference.setCurrency(request.getCurrency());
        preference.setNotificationMethod(request.getNotificationMethod());

        return toPreferenceResponse(customerPreferenceRepository.save(preference));
    }

    @Override
    public void deletePreferences(Long customerId) {
        findOrThrow(customerId);
        CustomerPreference preference = customerPreferenceRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerPreference", customerId));
        customerPreferenceRepository.delete(preference);
    }

    @Override
    public List<CustomerAuditLogResponse> getAuditLogs(Long customerId) {
        findOrThrow(customerId);
        return customerAuditLogRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
                .map(this::toAuditLogResponse)
                .toList();
    }

    // Shared by add/update: a customer can't be related to themselves, the
    // related customer must actually exist, and the same (relatedCustomer,
    // relationshipType) pair can't be added twice for one customer.
    // excludeRelationshipId skips the relationship being updated itself when
    // checking for duplicates (it's about to be overwritten with these same
    // values, so it shouldn't conflict with itself).
    private Customer validateRelationship(Long customerId, CustomerRelationshipRequest request, Long excludeRelationshipId) {
        if (request.getRelatedCustomerId().equals(customerId)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "A customer cannot have a relationship with themselves");
        }
        Customer relatedCustomer = findOrThrow(request.getRelatedCustomerId());

        boolean duplicate = customerRelationshipRepository
                .findByCustomerIdOrderByCreatedAtAsc(customerId).stream()
                .anyMatch(r -> !r.getId().equals(excludeRelationshipId)
                        && r.getRelatedCustomer().getId().equals(request.getRelatedCustomerId())
                        && r.getRelationshipType() == request.getRelationshipType());
        if (duplicate) {
            throw new AppException(HttpStatus.CONFLICT,
                    "This relationship already exists for that customer");
        }
        return relatedCustomer;
    }

    // INDIVIDUAL needs a first/last name (fullName is derived from them);
    // CORPORATE has no person name, so it must supply fullName (the company's
    // registered name) directly instead.
    private void validateByType(CustomerRequest request) {
        if (request.getCustomerType() == CustomerType.INDIVIDUAL) {
            if (isBlank(request.getFirstName()) || isBlank(request.getLastName())) {
                throw new AppException(HttpStatus.BAD_REQUEST,
                        "firstName and lastName are required for an INDIVIDUAL customer");
            }
        } else if (request.getCustomerType() == CustomerType.CORPORATE) {
            if (isBlank(request.getFullName())) {
                throw new AppException(HttpStatus.BAD_REQUEST,
                        "fullName is required for a CORPORATE customer");
            }
        }
    }

    private String resolveFullName(CustomerRequest request) {
        if (request.getCustomerType() == CustomerType.INDIVIDUAL) {
            return (request.getFirstName() + " " + request.getLastName()).trim();
        }
        return request.getFullName();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private Customer findOrThrow(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
    }

    // Verifies the identity belongs to customerId rather than just any customer,
    // so one customer can't read/modify another's identity by guessing its id.
    private CustomerIdentity findIdentityOrThrow(Long customerId, Long identityId) {
        CustomerIdentity identity = customerIdentityRepository.findById(identityId)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerIdentity", identityId));
        if (!identity.getCustomer().getId().equals(customerId)) {
            throw new ResourceNotFoundException("CustomerIdentity", identityId);
        }
        return identity;
    }

    // Verifies the address belongs to customerId rather than just any customer,
    // so one customer can't read/modify another's address by guessing its id.
    private CustomerAddress findAddressOrThrow(Long customerId, Long addressId) {
        CustomerAddress address = customerAddressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerAddress", addressId));
        if (!address.getCustomer().getId().equals(customerId)) {
            throw new ResourceNotFoundException("CustomerAddress", addressId);
        }
        return address;
    }

    // Verifies the contact belongs to customerId rather than just any customer,
    // so one customer can't read/modify another's contact by guessing its id.
    private CustomerContact findContactOrThrow(Long customerId, Long contactId) {
        CustomerContact contact = customerContactRepository.findById(contactId)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerContact", contactId));
        if (!contact.getCustomer().getId().equals(customerId)) {
            throw new ResourceNotFoundException("CustomerContact", contactId);
        }
        return contact;
    }

    // Verifies the employment record belongs to customerId rather than just any
    // customer, so one customer can't read/modify another's by guessing its id.
    private CustomerEmployment findEmploymentOrThrow(Long customerId, Long employmentId) {
        CustomerEmployment employment = customerEmploymentRepository.findById(employmentId)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerEmployment", employmentId));
        if (!employment.getCustomer().getId().equals(customerId)) {
            throw new ResourceNotFoundException("CustomerEmployment", employmentId);
        }
        return employment;
    }

    // Verifies the income record belongs to customerId rather than just any
    // customer, so one customer can't read/modify another's by guessing its id.
    private CustomerIncome findIncomeOrThrow(Long customerId, Long incomeId) {
        CustomerIncome income = customerIncomeRepository.findById(incomeId)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerIncome", incomeId));
        if (!income.getCustomer().getId().equals(customerId)) {
            throw new ResourceNotFoundException("CustomerIncome", incomeId);
        }
        return income;
    }

    // Verifies the relationship belongs to customerId rather than just any
    // customer, so one customer can't read/modify another's by guessing its id.
    private CustomerRelationship findRelationshipOrThrow(Long customerId, Long relationshipId) {
        CustomerRelationship relationship = customerRelationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerRelationship", relationshipId));
        if (!relationship.getCustomer().getId().equals(customerId)) {
            throw new ResourceNotFoundException("CustomerRelationship", relationshipId);
        }
        return relationship;
    }

    private String generateCustomerNo(Customer customer) {
        String datePart = customer.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "CUS-" + datePart + "-" + String.format("%06d", customer.getId());
    }

    private CustomerResponse toResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .customerNo(customer.getCustomerNo())
                .customerType(customer.getCustomerType())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .fullName(customer.getFullName())
                .gender(customer.getGender())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .nationalId(customer.getNationalId())
                .address(customer.getAddress())
                .dateOfBirth(customer.getDateOfBirth())
                .nationality(customer.getNationality())
                .maritalStatus(customer.getMaritalStatus())
                .status(customer.getStatus())
                .branchId(customer.getBranchId())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .deletedAt(customer.getDeletedAt())
                .build();
    }

    private CustomerIdentityResponse toIdentityResponse(CustomerIdentity identity) {
        return CustomerIdentityResponse.builder()
                .id(identity.getId())
                .customerId(identity.getCustomer().getId())
                .identityType(identity.getIdentityType())
                .identityNumber(identity.getIdentityNumber())
                .issueDate(identity.getIssueDate())
                .expiryDate(identity.getExpiryDate())
                .issuingCountry(identity.getIssuingCountry())
                .status(identity.getStatus())
                .createdAt(identity.getCreatedAt())
                .updatedAt(identity.getUpdatedAt())
                .build();
    }

    private CustomerAddressResponse toAddressResponse(CustomerAddress address) {
        return CustomerAddressResponse.builder()
                .id(address.getId())
                .customerId(address.getCustomer().getId())
                .addressType(address.getAddressType())
                .country(address.getCountry())
                .province(address.getProvince())
                .district(address.getDistrict())
                .commune(address.getCommune())
                .village(address.getVillage())
                .street(address.getStreet())
                .postalCode(address.getPostalCode())
                .isPrimary(address.isPrimary())
                .createdAt(address.getCreatedAt())
                .updatedAt(address.getUpdatedAt())
                .build();
    }

    private CustomerContactResponse toContactResponse(CustomerContact contact) {
        return CustomerContactResponse.builder()
                .id(contact.getId())
                .customerId(contact.getCustomer().getId())
                .name(contact.getName())
                .relationship(contact.getRelationship())
                .phone(contact.getPhone())
                .email(contact.getEmail())
                .address(contact.getAddress())
                .createdAt(contact.getCreatedAt())
                .updatedAt(contact.getUpdatedAt())
                .build();
    }

    private CustomerEmploymentResponse toEmploymentResponse(CustomerEmployment employment) {
        return CustomerEmploymentResponse.builder()
                .id(employment.getId())
                .customerId(employment.getCustomer().getId())
                .companyName(employment.getCompanyName())
                .occupation(employment.getOccupation())
                .employmentType(employment.getEmploymentType())
                .salary(employment.getSalary())
                .currency(employment.getCurrency())
                .startDate(employment.getStartDate())
                .status(employment.getStatus())
                .createdAt(employment.getCreatedAt())
                .updatedAt(employment.getUpdatedAt())
                .build();
    }

    private CustomerIncomeResponse toIncomeResponse(CustomerIncome income) {
        return CustomerIncomeResponse.builder()
                .id(income.getId())
                .customerId(income.getCustomer().getId())
                .incomeType(income.getIncomeType())
                .amount(income.getAmount())
                .currency(income.getCurrency())
                .frequency(income.getFrequency())
                .createdAt(income.getCreatedAt())
                .updatedAt(income.getUpdatedAt())
                .build();
    }

    private CustomerRelationshipResponse toRelationshipResponse(CustomerRelationship relationship) {
        return CustomerRelationshipResponse.builder()
                .id(relationship.getId())
                .customerId(relationship.getCustomer().getId())
                .relatedCustomerId(relationship.getRelatedCustomer().getId())
                .relatedCustomerName(relationship.getRelatedCustomer().getFullName())
                .relationshipType(relationship.getRelationshipType())
                .createdAt(relationship.getCreatedAt())
                .updatedAt(relationship.getUpdatedAt())
                .build();
    }

    private CustomerRiskProfileResponse toRiskProfileResponse(CustomerRiskProfile profile) {
        return CustomerRiskProfileResponse.builder()
                .id(profile.getId())
                .customerId(profile.getCustomer().getId())
                .riskLevel(profile.getRiskLevel())
                .pep(profile.isPep())
                .sanctionChecked(profile.isSanctionChecked())
                .amlStatus(profile.getAmlStatus())
                .lastReviewDate(profile.getLastReviewDate())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }

    private CustomerPreferenceResponse toPreferenceResponse(CustomerPreference preference) {
        return CustomerPreferenceResponse.builder()
                .id(preference.getId())
                .customerId(preference.getCustomer().getId())
                .language(preference.getLanguage())
                .currency(preference.getCurrency())
                .notificationMethod(preference.getNotificationMethod())
                .createdAt(preference.getCreatedAt())
                .updatedAt(preference.getUpdatedAt())
                .build();
    }

    private CustomerAuditLogResponse toAuditLogResponse(CustomerAuditLog log) {
        return CustomerAuditLogResponse.builder()
                .id(log.getId())
                .customerId(log.getCustomer().getId())
                .action(log.getAction())
                .userId(log.getUserId())
                .oldValue(log.getOldValue())
                .newValue(log.getNewValue())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
