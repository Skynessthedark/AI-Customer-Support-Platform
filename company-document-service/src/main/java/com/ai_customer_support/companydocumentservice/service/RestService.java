package com.ai_customer_support.companydocumentservice.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.ai_customer_support.companydocumentservice.config.property.CompanyServiceEndpoints;
import com.ai_customer_support.companydocumentservice.dto.CompanyDto;

@Service 
public class RestService {

    private final RestClient companyRestClient;
    private final CompanyServiceEndpoints companyServiceEndpoints;

    public RestService(@Qualifier("companyRestClient") RestClient companyRestClient, 
                            CompanyServiceEndpoints companyServiceEndpoints) {
        this.companyRestClient = companyRestClient;
        this.companyServiceEndpoints = companyServiceEndpoints;
    }

    public CompanyDto getCompany(String id) {
        if(id == null || id.isEmpty()) return null;

        return companyRestClient.get()
                .uri(generateCompanyUrl(id))
                .retrieve()
                .body(CompanyDto.class);
    }

    private String generateCompanyUrl(String companyId) {
        return UriComponentsBuilder.fromUriString(companyServiceEndpoints.getCompanyById())
                .buildAndExpand(companyId)
                .toUriString();
    }

    public CompanyDto increaseAndUpdateKnowledgeVersion(String id) {
        return companyRestClient.patch()
                .uri(generateKnowledgeVersionUrl(id))
                .retrieve()
                .body(CompanyDto.class);
    }

    private String generateKnowledgeVersionUrl(String companyId) {
        return UriComponentsBuilder.fromUriString(companyServiceEndpoints.getKnowledgeVersionIncrement())
                .buildAndExpand(companyId)
                .toUriString();
    }
}
