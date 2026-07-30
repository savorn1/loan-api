package com.example.payment.repository;

import com.example.payment.entity.CollectionLetter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CollectionLetterRepository extends JpaRepository<CollectionLetter, Long> {

    List<CollectionLetter> findByCollectionCaseIdOrderByCreatedAtDesc(Long collectionCaseId);

    Optional<CollectionLetter> findByIdAndCollectionCaseId(Long id, Long collectionCaseId);
}
