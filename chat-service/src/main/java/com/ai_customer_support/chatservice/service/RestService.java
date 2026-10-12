package com.ai_customer_support.chatservice.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.ai_customer_support.chatservice.config.property.CompanyDocumentServiceEndpoints;
import com.ai_customer_support.chatservice.config.property.CompanyServiceEndpoints;
import com.ai_customer_support.chatservice.dto.CompanyDto;
import com.ai_customer_support.chatservice.exception.CompanyServiceNetworkException;
import com.ai_customer_support.chatservice.exception.CompanyDocumentServiceNetworkException;

@Service 
public class RestService {

    private static final String COMPANY_TIMEOUT_MSG = "Company service gateway connection timed out.";
    private static final String COMPANY_DOCUMENT_TIMEOUT_MSG = "Company document service gateway connection timed out.";

    private final RestClient companyRestClient;
    private final RestClient companyDocumentRestClient;
    private final CompanyServiceEndpoints companyServiceEndpoints;
    private final CompanyDocumentServiceEndpoints companyDocumentServiceEndpoints;

    public RestService(@Qualifier(value = "companyRestClient")
            RestClient companyRestClient, CompanyServiceEndpoints companyServiceEndpoints, 
            @Qualifier(value = "companyDocumentRestClient") RestClient companyDocumentRestClient, 
            CompanyDocumentServiceEndpoints companyDocumentServiceEndpoints) {
        this.companyRestClient = companyRestClient;
        this.companyServiceEndpoints = companyServiceEndpoints;
        this.companyDocumentRestClient = companyDocumentRestClient;
        this.companyDocumentServiceEndpoints = companyDocumentServiceEndpoints;
    }

    public CompanyDto getCompany(String id) {
        if(isCompanyIdInvalid(id)) return null;

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
        if (isCompanyIdInvalid(id)) return null;

        try {
            return companyRestClient.get()
                    .uri(generateKnowledgeVersionUrl(id))
                    .retrieve()
                    .body(CompanyDto.class);
        } catch (ResourceAccessException ex) {
            throw new CompanyServiceNetworkException(COMPANY_TIMEOUT_MSG, ex);
        }
        
    }

    public String getSimilarContextForCompany(String companyId, String message) {
        if (isCompanyIdInvalid(companyId) || message.isEmpty()) return null;

        try {
            return companyDocumentRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(companyDocumentServiceEndpoints.getSimilarContext())
                            .queryParam("companyId", companyId)
                            .queryParam("message", message)
                            .build())
                    .retrieve()
                    .body(String.class);
        } catch (ResourceAccessException ex) {
            throw new CompanyDocumentServiceNetworkException(COMPANY_DOCUMENT_TIMEOUT_MSG, ex);
        }
    }


    private boolean isCompanyIdInvalid(String id){
        return id == null || id.isEmpty();
    }

    private String generateCompanyUrl(String companyId) {
        return UriComponentsBuilder.fromUriString(companyServiceEndpoints.getCompanyById())
                .buildAndExpand(companyId)
                .toUriString();
    }

    private String generateKnowledgeVersionUrl(String companyId) {
        return UriComponentsBuilder.fromUriString(companyServiceEndpoints.getKnowledgeVersionIncrement())
                .buildAndExpand(companyId)
                .toUriString();
    }
}
