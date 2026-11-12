package com.ai_customer_support.companyservice.repository;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import com.ai_customer_support.companyservice.model.Company;

@Testcontainers
@SpringBootTest
public class CompanyRepositoryTests {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:15-alpine");

    @Autowired
    private CompanyRepository companyRepository;

    @Test
    void shouldReturnCompanyWhenFindCompanyByIdGivenValidId() {
        //Given
        Company newCompany = new Company();
        newCompany.setName("Sample Company");
        newCompany.setAuthorizedPersonEmail("auth@test.com");

        Company savedCompany = companyRepository.save(newCompany);

        String companyId = savedCompany.getId();

        //Then
        Optional<Company> company = companyRepository.findById(companyId);

        Assertions.assertThat(company).isPresent();
        Assertions.assertThat(company.get().getId()).isEqualTo(companyId);
    }

    @Test
    void shouldReturnNullWhenFindCompanyByIdGivenInvalidId() {
        // Given
        String companyId = "invalid-id";

        // Then
        Optional<Company> company = companyRepository.findById(companyId);

        Assertions.assertThat(company).isNotPresent();
    }

    @Test
    void shouldReturnCompanyWhenFindCompanyByAuthorizedPersonEmailGivenValidEmail() {
        // Given
        String authorizedPersonEmail = "auth@test.com";
        Company newCompany = new Company();
        newCompany.setName("Sample Company");
        newCompany.setAuthorizedPersonEmail(authorizedPersonEmail);

        Company savedCompany = companyRepository.save(newCompany);

        // Then
        Optional<Company> company = companyRepository.findByAuthorizedPersonEmail(authorizedPersonEmail);

        Assertions.assertThat(company).isPresent();
        Assertions.assertThat(company.get().getId()).isEqualTo(savedCompany.getId());
        Assertions.assertThat(company.get().getAuthorizedPersonEmail()).isEqualTo(savedCompany.getAuthorizedPersonEmail());
    }

}
