package com.ai_customer_support.companydocumentservice.config;

import org.apache.hc.client5.http.ConnectTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.HttpClientSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import java.time.Duration;

import com.ai_customer_support.companydocumentservice.config.property.ClientConnectionProperties;
import com.ai_customer_support.companydocumentservice.exception.CompanyServiceException;
import com.ai_customer_support.companydocumentservice.exception.CompanyServiceNetworkException;

@Configuration
public class RestClientConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestClientConfig.class);

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
                .requestInterceptor(retryInterceptor())
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

    private ClientHttpRequestInterceptor retryInterceptor() {
        RetryPolicy policy = RetryPolicy.builder()
                .includes(ResourceAccessException.class, ConnectTimeoutException.class)
                .maxRetries(clientConnectionProperties.getRetryMaxAttempts())
                .maxDelay(Duration.ofMillis(clientConnectionProperties.getRetryBackoffDelayMs()))
                .build();
        RetryTemplate retryTemplate = new RetryTemplate(policy);

        retryTemplate.setRetryListener(new RestClientRetryListener());

        return (request, body, execution) -> {
            try {
                return retryTemplate.execute(() -> execution.execute(request, body));
            } catch (Throwable throwable) {
                if (throwable instanceof RuntimeException) {
                    throw (RuntimeException) throwable;
                }
                throw new CompanyServiceNetworkException("Company Service Connection - Retry attempts exhausted: " + throwable
                        .getMessage(), throwable);
            }
        };
    }

}
