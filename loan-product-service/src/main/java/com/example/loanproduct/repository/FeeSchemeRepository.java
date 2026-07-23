package com.example.loanproduct.repository;

import com.example.loanproduct.entity.FeeScheme;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FeeSchemeRepository extends JpaRepository<FeeScheme, UUID> {

    Optional<FeeScheme> findByCode(String code);

    boolean existsByCode(String code);
}
