package com.example.loanproduct.repository;

import com.example.loanproduct.entity.InterestSchemeDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InterestSchemeDetailRepository extends JpaRepository<InterestSchemeDetail, UUID> {

    List<InterestSchemeDetail> findByInterestSchemeId(UUID interestSchemeId);

    Optional<InterestSchemeDetail> findByIdAndInterestSchemeId(UUID id, UUID interestSchemeId);
}
