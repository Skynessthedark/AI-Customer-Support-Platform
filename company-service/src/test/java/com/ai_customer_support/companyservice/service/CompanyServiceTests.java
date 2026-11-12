package com.ai_customer_support.companyservice.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ai_customer_support.companyservice.dto.CompanyDto;
import com.ai_customer_support.companyservice.exception.CompanyNotFoundException;
import com.ai_customer_support.companyservice.model.Company;
import com.ai_customer_support.companyservice.repository.CompanyRepository;

@ExtendWith(MockitoExtension.class)
public class CompanyServiceTests {

    @InjectMocks 
    private CompanyService companyService;

    @Mock 
    private CompanyRepository companyRepository;

    @Test 
    public void shouldReturnCompanyDtoWhenGivenValidCompanyId() {
        //Given
        String companyId = "12345-12345";

        //When
        when(companyRepository.findById(companyId))
            .thenReturn(Optional.of(getSampleCompanyModel(1)));

        //Then
        CompanyDto companyDto = companyService.getCompany(companyId);

        Assertions.assertThat(companyDto).isNotNull();
        Assertions.assertThat(companyDto.id()).isEqualTo(companyId);

        verify(companyRepository).findById(companyId);
    }

    @Test
    public void shouldThrowExceptionWhenGivenInvalidCompanyId() {
        // Given
        String companyId = "invalid-id";

        // When
        when(companyRepository.findById(companyId))
                .thenReturn(Optional.empty());

        // Then
        verify(companyRepository).findById(companyId);

        Assertions.assertThatThrownBy(() -> companyService.getCompany(companyId))
                .isInstanceOf(CompanyNotFoundException.class);
    }

    @Test
    public void shouldReturnCompanyDtoWithUpdatedKnowledgeVersionWhenGivenValidCompanyId() {
        // Given
        String companyId = "12345-12345";
        int initialKnowledgeVersion = 1;

        // when
        when(companyRepository.findById(companyId))
                .thenReturn(Optional.of(getSampleCompanyModel(initialKnowledgeVersion)));
        
        when(companyRepository.save(any(Company.class)))
                .thenReturn(getSampleCompanyModel(initialKnowledgeVersion + 1));

        // then
        CompanyDto companyDto = companyService.increaseAndUpdateKnowledgeVersion(companyId);

        Assertions.assertThat(companyDto).isNotNull();
        Assertions.assertThat(companyDto.knowledgeVersion()).isEqualTo(initialKnowledgeVersion + 1);
    }

    private Company getSampleCompanyModel(int knowledgeVersion) {
        Company company = new Company();
        company.setId("12345-12345");
        company.setName("Sample Company");
        company.setAuthorizedPersonEmail("auth@samplecorp.com");
        company.setKnowledgeVersion(knowledgeVersion);
        company.setCreatedAt(LocalDateTime.now());
        company.setUpdatedAt(LocalDateTime.now());
        return company;
    }
}
