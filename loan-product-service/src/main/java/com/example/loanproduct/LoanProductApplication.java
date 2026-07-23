package com.example.loanproduct;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class LoanProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoanProductApplication.class, args);
    }
}
