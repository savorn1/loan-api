package com.example.customer.repository;

import com.example.customer.entity.CustomerRelationship;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerRelationshipRepository extends JpaRepository<CustomerRelationship, Long> {

    List<CustomerRelationship> findByCustomerIdOrderByCreatedAtAsc(Long customerId);
}
