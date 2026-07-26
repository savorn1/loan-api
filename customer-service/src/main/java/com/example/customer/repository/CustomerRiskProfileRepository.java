package com.example.customer.repository;

import com.example.customer.entity.CustomerRiskProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRiskProfileRepository extends JpaRepository<CustomerRiskProfile, Long> {

    Optional<CustomerRiskProfile> findByCustomerId(Long customerId);
}
