package com.ai_customer_support.chatservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;

import com.ai_customer_support.chatservice.exception.CompanyDocumentServiceException;
import com.ai_customer_support.chatservice.exception.CompanyServiceException;

@Configuration
public class RestClientConfig {

    @Value("${services.company.url}")
    String companyUrl;

    @Value("${services.company-document.url}")
    String companyDocumentUrl;
    
    @Bean("companyRestClient")
    RestClient companyRestClient(RestClient.Builder builder) {
        return builder
                .baseUrl(companyUrl)
                .defaultHeader("Content-Type", "application/json")
                .defaultStatusHandler(
                    HttpStatusCode::isError,
                    (request, response) -> {
                        throw new CompanyServiceException(
                            response.getStatusCode(),
                            response.getStatusText(),
                            response.getBody().readAllBytes()
                        );
                })
                .build();
    }

    @Bean("companyDocumentRestClient")
    RestClient companyDocumentRestClient(RestClient.Builder builder) {
        return builder
                .baseUrl(companyDocumentUrl)
                .defaultHeader("Content-Type", "application/json")
                .defaultStatusHandler(
                        HttpStatusCode::isError,
                        (request, response) -> {
                            throw new CompanyDocumentServiceException(
                                    response.getStatusCode(),
                                    response.getStatusText(),
                                    response.getBody().readAllBytes());
                })
                .build();
    }

}
