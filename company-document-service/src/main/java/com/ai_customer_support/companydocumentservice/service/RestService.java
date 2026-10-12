package com.ai_customer_support.companydocumentservice.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.ai_customer_support.companydocumentservice.config.property.CompanyServiceEndpoints;
import com.ai_customer_support.companydocumentservice.dto.CompanyDto;
import com.ai_customer_support.companydocumentservice.exception.CompanyServiceNetworkException;

@Service 
public class RestService {

    private static final String COMPANY_TIMEOUT_MSG = "Company service gateway connection timed out.";

    private final RestClient companyRestClient;
    private final CompanyServiceEndpoints companyServiceEndpoints;

    public RestService(@Qualifier("companyRestClient") RestClient companyRestClient, 
                            CompanyServiceEndpoints companyServiceEndpoints) {
        this.companyRestClient = companyRestClient;
        this.companyServiceEndpoints = companyServiceEndpoints;
    }

    public CompanyDto getCompany(String id) {
        if(id == null || id.isEmpty()) return null;

        try {
            return companyRestClient.get()
                    .uri(generateCompanyUrl(id))
                    .retrieve()
                    .body(CompanyDto.class);
        } catch (ResourceAccessException ex) {
            throw new CompanyServiceNetworkException(COMPANY_TIMEOUT_MSG, ex);
        }
    }

    public CompanyDto increaseAndUpdateKnowledgeVersion(String id) {
        try {
            return companyRestClient.patch()
                    .uri(generateKnowledgeVersionUrl(id))
                    .retrieve()
                    .body(CompanyDto.class);
        } catch (ResourceAccessException ex) {
            throw new CompanyServiceNetworkException(COMPANY_TIMEOUT_MSG, ex);
        }  
    }


    private String generateKnowledgeVersionUrl(String companyId) {
        return UriComponentsBuilder.fromUriString(companyServiceEndpoints.getKnowledgeVersionIncrement())
                .buildAndExpand(companyId)
                .toUriString();
    }

    private String generateCompanyUrl(String companyId) {
        return UriComponentsBuilder.fromUriString(companyServiceEndpoints.getCompanyById())
                .buildAndExpand(companyId)
                .toUriString();
    }
}
