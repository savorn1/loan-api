package com.example.accounting.repository;

import com.example.accounting.entity.GlAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GlAccountRepository extends JpaRepository<GlAccount, Long> {

    boolean existsByAccountNo(String accountNo);

    Optional<GlAccount> findByAccountNo(String accountNo);

    List<GlAccount> findByParentId(Long parentId);
}
