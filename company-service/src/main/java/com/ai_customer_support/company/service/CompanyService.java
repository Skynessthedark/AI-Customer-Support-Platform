package com.ai_customer_support.company.service;

import org.springframework.stereotype.Service;

import com.ai_customer_support.company.dto.CompanyDto;
import com.ai_customer_support.company.exception.CompanyNotFoundException;
import com.ai_customer_support.company.model.Company;
import com.ai_customer_support.company.repository.CompanyRepository;

@Service 
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public boolean isCompanyExists(String companyId) {
        return companyRepository.existsById(companyId);
    }

    public Company getCompanyModel(String companyId) {
        return companyRepository.findById(companyId)
            .orElseThrow(() -> new CompanyNotFoundException("Company with ID " + companyId + " does not exist."));
    }

    public CompanyDto getCompanyByAuthorizedPersonEmail(String email) {
        Company company = companyRepository.findByAuthorizedPersonEmail(email)
                .orElseThrow(() -> new CompanyNotFoundException(
                        "Company with authorized person email " + email + " does not exist."));
        return prepareDtoForCompany(company);
    }

    public CompanyDto increaseAndUpdateKnowledgeVersion(String companyId){
        Company company = getCompanyModel(companyId);
        company.increaseKnowledgeVersion();
        return prepareDtoForCompany(companyRepository.save(company));
    }

    public CompanyDto getCompany(String companyId){
        Company company = getCompanyModel(companyId);
        return prepareDtoForCompany(company);
    }

    private CompanyDto prepareDtoForCompany(Company company){
        return new CompanyDto(company.getId(),
                company.getName(),
                company.getAuthorizedPersonEmail(),
                company.getAuthorizedPersonName(),
                company.getAuthorizedPersonPhone(),
                company.getKnowledgeVersion(),
                company.getCreatedAt(),
                company.getUpdatedAt());
    }
}
