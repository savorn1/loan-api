package com.example.loanproduct.repository;

import com.example.loanproduct.entity.TermTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TermTemplateRepository extends JpaRepository<TermTemplate, UUID> {

    Optional<TermTemplate> findByCode(String code);

    boolean existsByCode(String code);
}
