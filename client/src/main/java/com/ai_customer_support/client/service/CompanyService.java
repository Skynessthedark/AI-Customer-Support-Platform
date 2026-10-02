package com.ai_customer_support.client.service;

import org.springframework.stereotype.Service;

import com.ai_customer_support.client.model.Company;
import com.ai_customer_support.client.repository.CompanyRepository;

@Service 
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public boolean isCompanyExists(String companyId) {
        return companyRepository.existsById(companyId);
    }

    public Company getCompany(String companyId) {
        return companyRepository.findById(companyId)
            .orElseThrow(() -> new IllegalArgumentException("Company with ID " + companyId + " does not exist."));
    }

    public Company getCompanyByAuthorizedPersonEmail(String email) {
        return companyRepository.findByAuthorizedPersonEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Company with authorized person email " + email + " does not exist."));
    }
}
