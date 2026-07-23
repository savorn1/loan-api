package com.example.loan.repository;

import com.example.loan.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findByCustomerId(Long customerId);
}
