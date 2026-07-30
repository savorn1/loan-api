package com.example.auth.repository;

import com.example.auth.entity.RbacRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RbacRoleRepository extends JpaRepository<RbacRole, Long> {

    Optional<RbacRole> findByName(String name);

    boolean existsByName(String name);

    boolean existsByCode(String code);

    List<RbacRole> findByPermissions_Id(Long permissionId);
}
