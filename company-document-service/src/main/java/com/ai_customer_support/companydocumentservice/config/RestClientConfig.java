package com.ai_customer_support.companydocumentservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.HttpClientSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import java.time.Duration;

import com.ai_customer_support.companydocumentservice.config.property.ClientConnectionProperties;
import com.ai_customer_support.companydocumentservice.exception.CompanyServiceException;

@Configuration
public class RestClientConfig {

    @Value("${services.company.url}")
    String companyUrl;

    private final ClientConnectionProperties clientConnectionProperties;

    RestClientConfig(ClientConnectionProperties clientConnectionProperties) {
        this.clientConnectionProperties = clientConnectionProperties;
    }
    
    @Bean("companyRestClient")
    RestClient companyRestClient(RestClient.Builder builder) {
        ClientHttpRequestFactory requestFactory = ClientHttpRequestFactoryBuilder.detect()
                .build(getHttpClientSettings());
        return builder
                .baseUrl(companyUrl)
                .defaultHeader("Content-Type", "application/json")
                .requestFactory(requestFactory)
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

    private HttpClientSettings getHttpClientSettings() {
        return HttpClientSettings.defaults()
                .withConnectTimeout(Duration.ofSeconds(
                        clientConnectionProperties.getConnectTimeout()))
                .withReadTimeout(Duration.ofSeconds(
                        clientConnectionProperties.getReadTimeout()));
    }

}
