package com.ai_customer_support.chatservice.integration;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.cache.semantic.SemanticCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

import com.ai_customer_support.chatservice.dto.CompanyDto;
import com.ai_customer_support.chatservice.exception.CompanyDocumentServiceException;
import com.ai_customer_support.chatservice.exception.CompanyDocumentServiceNetworkException;
import com.ai_customer_support.chatservice.exception.CompanyServiceException;
import com.ai_customer_support.chatservice.exception.CompanyServiceNetworkException;
import com.ai_customer_support.chatservice.service.RestService;
import com.github.tomakehurst.wiremock.WireMockServer;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import redis.clients.jedis.RedisClient;

@SpringBootTest(properties = {
        "spring.ai.openai.api-key=test-dummy-key",
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "services.company.endpoints.company-by-id=/api/company/{id}",
        "services.company.endpoints.knowledge-version-increment=/api/company/{id}/increase-knowledge-version",
        "services.company-document.endpoints.similar-context=/api/company-document/similar-context"
})
@EnableWireMock({
    @ConfigureWireMock(
        name = "company-service",
        baseUrlProperties = "services.company.url"
    ),
    @ConfigureWireMock(
        name = "company-document-service",
        baseUrlProperties = "services.company-document.url"
    )
})
public class Service2ServiceTests {

    @Autowired
    private RestService restService;

    @InjectWireMock("company-service")
    private WireMockServer companyWireMockServer;

    @InjectWireMock("company-document-service")
    private WireMockServer companyDocumentWireMockServer;

    @MockitoBean
    private SemanticCache semanticCache;

    @MockitoBean
    private RedisClient redisClient;

    /**
     * Company-Service Integration Tests
     */

    @Test
    void shouldReturnCompanyDtoWhenGivenValidCompanyId() {
        //Given
        String companyId = "12345-12345";

        companyWireMockServer.stubFor(get(urlEqualTo("/api/company/" + companyId))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                    "id": "12345-12345",
                                    "name": "Sample Company",
                                    "authorizedPersonEmail": "auth@samplecorp.com",
                                    "knowledgeVersion": 1,
                                    "createdAt": "2024-06-01 12:00:00",
                                    "updatedAt": "2024-06-01 12:00:00"
                                }
                                """)));

        CompanyDto response = restService.getCompany(companyId);

        Assertions.assertThat(response.authorizedPersonEmail()).isEqualTo("auth@samplecorp.com");
    }

    @Test
    void shouldReturnExceptionWhenGetCompanyByIdThrowsA500Error() {
        // Given
        String companyId = "12345-12345";

        companyWireMockServer.stubFor(get(urlEqualTo("/api/company/" + companyId))
                .willReturn(aResponse()
                        .withStatus(500)
        ));

        Assertions.assertThatThrownBy(() -> restService.getCompany(companyId))
                .isInstanceOf(CompanyServiceException.class);

    }

    @Test
    void shouldReturnNetworkExceptionWhenGetCompanyByIdThrowsATimeoutError() {
        // Given
        String companyId = "12345-12345";

        companyWireMockServer.stubFor(get(urlEqualTo("/api/company/" + companyId))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withFixedDelay(3000)));

        Assertions.assertThatThrownBy(() -> restService.getCompany(companyId))
                .isInstanceOf(CompanyServiceNetworkException.class);

    }

    /**
     * Company-Document-Service Integration Tests
     */

    @Test
    void shouldReturnSimilarContextWhenGivenValidParameters() {
        // Given
        String companyId = "12345-12345";
        String message = "Sample";
        String expectedResponse = "Sample Similar Context.";

        companyDocumentWireMockServer.stubFor(get(urlEqualTo("/api/company-document/similar-context?companyId=" + companyId + "&message=" + message))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(expectedResponse)));

        String similarContext = restService.getSimilarContextForCompany(companyId, message);

        Assertions.assertThat(similarContext).isEqualTo(expectedResponse);
    }
    
    @Test
    void shouldReturnExceptionWhenSimilarContextServiceThrowsA500Error() {
        // Given
        String companyId = "12345-12345";
        String message = "Sample";

        companyDocumentWireMockServer.stubFor(
                get(urlEqualTo("/api/company-document/similar-context?companyId=" + companyId + "&message=" + message))
                        .willReturn(aResponse()
                                .withStatus(500)));

        Assertions.assertThatThrownBy(() -> restService.getSimilarContextForCompany(companyId, message))
                .isInstanceOf(CompanyDocumentServiceException.class);
        
    }

    @Test
    void shouldReturnNetworkExceptionWhenSimilarContextServiceThrowsATimeoutError() {
        // Given
        String companyId = "12345-12345";
        String message = "Sample";
        
        companyDocumentWireMockServer.stubFor(
                get(urlEqualTo("/api/company-document/similar-context?companyId=" + companyId + "&message=" + message))
                        .willReturn(aResponse()
                        .withStatus(200)
                        .withFixedDelay(3000)));

        Assertions.assertThatThrownBy(() -> restService.getSimilarContextForCompany(companyId, message))
                .isInstanceOf(CompanyDocumentServiceNetworkException.class);

    }
}
