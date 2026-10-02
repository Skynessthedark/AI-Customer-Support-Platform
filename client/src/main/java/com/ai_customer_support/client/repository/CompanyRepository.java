package com.ai_customer_support.client.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ai_customer_support.client.model.Company;

public interface CompanyRepository extends JpaRepository<Company, String> {
    public Optional<Company> findByAuthorizedPersonEmail(String email);
}
