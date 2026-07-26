package com.example.customer.repository;

import com.example.customer.entity.CustomerIdentity;
import com.example.customer.entity.IdentityType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerIdentityRepository extends JpaRepository<CustomerIdentity, Long> {

    List<CustomerIdentity> findByCustomerIdOrderByCreatedAtAsc(Long customerId);

    boolean existsByCustomerIdAndIdentityTypeAndIdentityNumber(Long customerId, IdentityType identityType, String identityNumber);
}
