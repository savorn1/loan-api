package com.example.loanproduct.repository;

import com.example.loanproduct.entity.FeeSchemeDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FeeSchemeDetailRepository extends JpaRepository<FeeSchemeDetail, UUID> {

    List<FeeSchemeDetail> findByFeeSchemeId(UUID feeSchemeId);

    Optional<FeeSchemeDetail> findByIdAndFeeSchemeId(UUID id, UUID feeSchemeId);
}
