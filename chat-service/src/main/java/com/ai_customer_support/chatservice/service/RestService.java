package com.ai_customer_support.chatservice.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.ai_customer_support.chatservice.config.property.CompanyDocumentServiceEndpoints;
import com.ai_customer_support.chatservice.config.property.CompanyServiceEndpoints;
import com.ai_customer_support.chatservice.dto.CompanyDto;

@Service 
public class RestService {

    private final RestClient companyRestClient;
    private final RestClient companyDocumentRestClient;
    private final CompanyServiceEndpoints companyServiceEndpoints;
    private final CompanyDocumentServiceEndpoints companyDocumentServiceEndpoints;

    public RestService(@Qualifier(value = "companyRestClient")
            RestClient companyRestClient, CompanyServiceEndpoints companyServiceEndpoints, @Qualifier(value = "companyDocumentRestClient")
                    RestClient companyDocumentRestClient, 
                    CompanyDocumentServiceEndpoints companyDocumentServiceEndpoints) {
        this.companyRestClient = companyRestClient;
        this.companyServiceEndpoints = companyServiceEndpoints;
        this.companyDocumentRestClient = companyDocumentRestClient;
        this.companyDocumentServiceEndpoints = companyDocumentServiceEndpoints;
    }

    public CompanyDto getCompany(String id) {
        if(isCompanyIdInvalid(id)) return null;

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
        if (isCompanyIdInvalid(id))
            return null;
        return companyRestClient.get()
                .uri(generateKnowledgeVersionUrl(id))
                .retrieve()
                .body(CompanyDto.class);
    }

    public String getSimilarContextForCompany(String companyId, String message) {
        if (isCompanyIdInvalid(companyId) || message.isEmpty()) return null;

        return companyDocumentRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(companyDocumentServiceEndpoints.getSimilarContext())
                        .queryParam("companyId", companyId)
                        .queryParam("message", message)
                        .build())
                .retrieve()
                .body(String.class);
    }

    private String generateKnowledgeVersionUrl(String companyId) {
        return UriComponentsBuilder.fromUriString(companyServiceEndpoints.getKnowledgeVersionIncrement())
                .buildAndExpand(companyId)
                .toUriString();
    }


    private boolean isCompanyIdInvalid(String id){
        return id == null || id.isEmpty();
    }
}
