package com.example.customer.repository;

import com.example.customer.entity.CustomerEmployment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerEmploymentRepository extends JpaRepository<CustomerEmployment, Long> {

    List<CustomerEmployment> findByCustomerIdOrderByCreatedAtAsc(Long customerId);
}
