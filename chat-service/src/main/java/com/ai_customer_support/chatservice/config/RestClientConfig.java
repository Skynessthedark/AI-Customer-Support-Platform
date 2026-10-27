package com.ai_customer_support.chatservice.config;

import java.time.Duration;

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

import com.ai_customer_support.chatservice.config.property.CompanyClientConnectionProperties;
import com.ai_customer_support.chatservice.config.property.CompanyDocumentClientConnectionProperties;
import com.ai_customer_support.chatservice.exception.CompanyDocumentServiceException;
import com.ai_customer_support.chatservice.exception.CompanyDocumentServiceNetworkException;
import com.ai_customer_support.chatservice.exception.CompanyServiceException;
import com.ai_customer_support.chatservice.exception.CompanyServiceNetworkException;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;

@Configuration
public class RestClientConfig {

        private static final Logger LOGGER = LoggerFactory.getLogger(RestClientConfig.class);

        @Value("${services.company.url}")
        String companyUrl;

        @Value("${services.company-document.url}")
        String companyDocumentUrl;

        private final CompanyClientConnectionProperties companyProperties;
        private final CompanyDocumentClientConnectionProperties companyDocumentProperties;
        private final CircuitBreakerRegistry circuitBreakerRegistry;

        RestClientConfig(CompanyClientConnectionProperties companyProperties,
                        CompanyDocumentClientConnectionProperties companyDocumentProperties,
                        CircuitBreakerRegistry circuitBreakerRegistry) {
                this.companyProperties = companyProperties;
                this.companyDocumentProperties = companyDocumentProperties;
                this.circuitBreakerRegistry = circuitBreakerRegistry;
        }

        @Bean("companyRestClient")
        RestClient companyRestClient(RestClient.Builder builder) {
                ClientHttpRequestFactory requestFactory = ClientHttpRequestFactoryBuilder.detect()
                                .build(getHttpClientSettingsForCompany());
                return builder
                                .baseUrl(companyUrl)
                                .defaultHeader("Content-Type", "application/json")
                                .requestFactory(requestFactory)
                                .requestInterceptors(interceptors -> {
                                        interceptors.add(circuitBreakerInterceptor("companyServiceBreaker"));
                                        interceptors.add(companyServiceRetryInterceptor());
                                })
                                .defaultStatusHandler(
                                                HttpStatusCode::isError,
                                                (request, response) -> {
                                                        throw new CompanyServiceException(
                                                                        response.getStatusCode(),
                                                                        response.getStatusText(),
                                                                        response.getBody().readAllBytes());
                                                })
                                .build();
        }

        @Bean("companyDocumentRestClient")
        RestClient companyDocumentRestClient(RestClient.Builder builder) {
                ClientHttpRequestFactory requestFactory = ClientHttpRequestFactoryBuilder.detect()
                                .build(getHttpClientSettingsForCompanyDocument());
                return builder
                                .baseUrl(companyDocumentUrl)
                                .defaultHeader("Content-Type", "application/json")
                                .requestFactory(requestFactory)
                                .requestInterceptors(interceptors -> {
                                        interceptors.add(circuitBreakerInterceptor("companyDocumentServiceBreaker"));
                                        interceptors.add(companyDocServiceRetryInterceptor());
                                })
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

        private HttpClientSettings getHttpClientSettingsForCompany() {
                return HttpClientSettings.defaults()
                                .withConnectTimeout(Duration.ofSeconds(
                                                companyProperties.getConnectTimeout()))
                                .withReadTimeout(Duration.ofSeconds(
                                                companyProperties.getReadTimeout()));
        }

        private HttpClientSettings getHttpClientSettingsForCompanyDocument() {
                return HttpClientSettings.defaults()
                                .withConnectTimeout(Duration.ofSeconds(
                                                companyDocumentProperties.getConnectTimeout()))
                                .withReadTimeout(Duration.ofSeconds(
                                                companyDocumentProperties.getReadTimeout()));
        }

        private ClientHttpRequestInterceptor companyServiceRetryInterceptor() {
                RetryPolicy policy = RetryPolicy.builder()
                                .includes(ResourceAccessException.class, ConnectTimeoutException.class)
                                .maxRetries(companyProperties.getRetryMaxAttempts())
                                .maxDelay(Duration.ofMillis(companyProperties.getRetryBackoffDelayMs()))
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
                                throw new CompanyServiceNetworkException(
                                                "Company Service Connection - Retry attempts exhausted: " + throwable
                                                                .getMessage(),
                                                throwable);
                        }
                };
        }

        private ClientHttpRequestInterceptor companyDocServiceRetryInterceptor() {
                RetryPolicy policy = RetryPolicy.builder()
                                .includes(ResourceAccessException.class, ConnectTimeoutException.class)
                                .maxRetries(companyDocumentProperties.getRetryMaxAttempts())
                                .maxDelay(Duration.ofMillis(companyDocumentProperties.getRetryBackoffDelayMs()))
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
                                throw new CompanyDocumentServiceNetworkException(
                                                "Company Document Service Connection - Retry attempts exhausted: "
                                                                + throwable
                                                                                .getMessage(),
                                                throwable);
                        }
                };
        }

        private ClientHttpRequestInterceptor circuitBreakerInterceptor(String circuitBreakerName) {
                CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(circuitBreakerName);

                // Status change event
                circuitBreaker.getEventPublisher().onStateTransition(event -> {
                        var transition = event.getStateTransition();
                        LOGGER.warn("[Circuit Breaker: {}] Status is changed! {} -> {}",
                                        circuitBreakerName,
                                        transition.getFromState(),
                                        transition.getToState());
                });

                // OPEN status event
                circuitBreaker.getEventPublisher().onCallNotPermitted(event -> {
                        LOGGER.error("[Circuit Breaker: {}] Circuit Breaker is OPEN, so requests are rejected!",
                                        circuitBreakerName);
                });

                return (request, body, execution) -> {
                        try {
                                return circuitBreaker.executeCheckedSupplier(() -> execution.execute(request, body));
                        } catch (Throwable e) {
                                if (e instanceof CallNotPermittedException) {
                                        throw new RuntimeException("Circuit Breaker is opened: " + e.getMessage(), e);
                                }
                                throw new RuntimeException("Service error: " + e.getMessage(), e);
                        }
                };
        }

}
