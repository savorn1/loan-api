package com.example.payment.repository;

import com.example.payment.entity.CollectionPromise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CollectionPromiseRepository extends JpaRepository<CollectionPromise, Long> {

    List<CollectionPromise> findByCollectionCaseIdOrderByCreatedAtDesc(Long collectionCaseId);

    Optional<CollectionPromise> findByIdAndCollectionCaseId(Long id, Long collectionCaseId);
}
