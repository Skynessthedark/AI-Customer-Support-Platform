package com.ai_customer_support.chatservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

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
                .build();
    }

    @Bean("companyDocumentRestClient")
    RestClient companyDocumentRestClient(RestClient.Builder builder) {
        return builder
                .baseUrl(companyDocumentUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

}
