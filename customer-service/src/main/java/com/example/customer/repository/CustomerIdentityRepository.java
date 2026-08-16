package com.example.customer.repository;

import com.example.customer.entity.CustomerIdentity;
import com.example.customer.entity.IdentityStatus;
import com.example.customer.entity.IdentityType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface CustomerIdentityRepository extends JpaRepository<CustomerIdentity, Long> {

    List<CustomerIdentity> findByCustomerIdOrderByCreatedAtAsc(Long customerId);

    boolean existsByCustomerIdAndIdentityTypeAndIdentityNumber(Long customerId, IdentityType identityType, String identityNumber);

    // Used by IdentityExpiryScheduler — identities are only ever flagged EXPIRED
    // once, so this naturally excludes anything already flagged on a prior run.
    List<CustomerIdentity> findByStatusAndExpiryDateBefore(IdentityStatus status, LocalDate date);
}
