package com.example.loanproduct.repository;

import com.example.loanproduct.entity.DocumentTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DocumentTemplateRepository extends JpaRepository<DocumentTemplate, UUID> {

    Optional<DocumentTemplate> findByCode(String code);

    boolean existsByCode(String code);
}
