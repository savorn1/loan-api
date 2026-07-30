package com.example.auth.repository;

import com.example.auth.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByModuleAndAction(String module, String action);

    boolean existsByModuleAndAction(String module, String action);
}
