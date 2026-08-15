package com.example.loan.repository;

import com.example.loan.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GroupRepository extends JpaRepository<Group, Long>, JpaSpecificationExecutor<Group> {

    @Query("SELECT g FROM Group g WHERE g.code IS NULL OR TRIM(g.code) = ''")
    List<Group> findWithBlankCode();
}
