package com.example.loanproduct.repository;

import com.example.loanproduct.entity.InterestScheme;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InterestSchemeRepository extends JpaRepository<InterestScheme, UUID> {

    Optional<InterestScheme> findByCode(String code);

    boolean existsByCode(String code);
}
