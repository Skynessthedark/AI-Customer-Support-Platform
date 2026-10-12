package com.ai_customer_support.companyservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ai_customer_support.companyservice.model.Company;

public interface CompanyRepository extends JpaRepository<Company, String> {
    public Optional<Company> findByAuthorizedPersonEmail(String email);
}
