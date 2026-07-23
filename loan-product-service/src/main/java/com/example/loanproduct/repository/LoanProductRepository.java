package com.example.loanproduct.repository;

import com.example.loanproduct.entity.LoanProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LoanProductRepository extends JpaRepository<LoanProduct, UUID> {
}
