package com.example.loanproduct.repository;

import com.example.loanproduct.entity.RuleTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RuleTemplateRepository extends JpaRepository<RuleTemplate, UUID> {

    Optional<RuleTemplate> findByCode(String code);

    boolean existsByCode(String code);
}
