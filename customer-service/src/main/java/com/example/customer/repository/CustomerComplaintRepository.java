package com.example.customer.repository;

import com.example.customer.entity.CustomerComplaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface CustomerComplaintRepository
        extends JpaRepository<CustomerComplaint, Long>, JpaSpecificationExecutor<CustomerComplaint> {

    List<CustomerComplaint> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
}
