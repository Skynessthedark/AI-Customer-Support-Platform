package com.ai_customer_support.chatservice.service;

import java.io.IOException;

import org.assertj.core.api.Assertions;
import static org.hamcrest.Matchers.startsWith;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import org.springframework.web.client.RestClient;

import com.ai_customer_support.chatservice.config.RestClientConfig;
import com.ai_customer_support.chatservice.config.property.CompanyClientConnectionProperties;
import com.ai_customer_support.chatservice.config.property.CompanyDocumentClientConnectionProperties;
import com.ai_customer_support.chatservice.config.property.CompanyDocumentServiceEndpoints;
import com.ai_customer_support.chatservice.config.property.CompanyServiceEndpoints;
import com.ai_customer_support.chatservice.dto.CompanyDto;
import com.ai_customer_support.chatservice.exception.CompanyDocumentServiceNetworkException;
import com.ai_customer_support.chatservice.exception.CompanyServiceNetworkException;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

@RestClientTest({ RestClientConfig.class,
    CompanyClientConnectionProperties.class, CompanyDocumentClientConnectionProperties.class})
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.cloud.config.enabled=false",
    "spring.config.import=optional:configserver:"
})
public class RestServiceTests {

    private RestService restService;

    private MockRestServiceServer companyMockServer;
    private MockRestServiceServer companyDocumentMockServer;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public CircuitBreakerRegistry circuitBreakerRegistry() {
            return CircuitBreakerRegistry.ofDefaults();
        }
    }

    @BeforeEach
    void setUp() {
        RestClient.Builder builderA = RestClient.builder();
        RestClient.Builder builderB = RestClient.builder();

        companyMockServer = MockRestServiceServer.bindTo(builderA).build();
        companyDocumentMockServer = MockRestServiceServer.bindTo(builderB).build();

        CompanyServiceEndpoints companyServiceEndpoints = new CompanyServiceEndpoints();
        companyServiceEndpoints.setCompanyById("https://company-service/api/company/{id}");
        companyServiceEndpoints.setKnowledgeVersionIncrement("https://company-service/api/company/{id}/increase-knowledge-version");

        CompanyDocumentServiceEndpoints companyDocumentServiceEndpoints = new CompanyDocumentServiceEndpoints();
        companyDocumentServiceEndpoints.setSimilarContext("https://company-document-service/api/company-document/similar-context");

        restService = new RestService(builderA.build(), 
                companyServiceEndpoints,
                builderB.build(), 
                companyDocumentServiceEndpoints);
    }

    /**
     * Test cases for getCompany method
     */

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void shouldReturnNullWhenGivenInvalidCompanyId(String companyId) {
        CompanyDto result = restService.getCompany(companyId);
        Assertions.assertThat(result).isNull();
    }

    @Test
    void shouldReturnCompanyDtoWhenGivenValidCompanyId() {
        //Given
        String companyId = "12345-12345";

        //when
        companyMockServer.expect(requestTo("https://company-service/api/company/12345-12345"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                            "id": "12345-12345",
                            "name": "Sample Company",
                            "authorizedPersonEmail": "auth@samplecorp.com",
                            "knowledgeVersion": 1,
                            "createdAt": "2024-06-01 12:00:00",
                            "updatedAt": "2024-06-01 12:00:00"
                        }
                        """, MediaType.APPLICATION_JSON));

        //then
        CompanyDto sampleCompany = restService.getCompany(companyId);

        companyMockServer.verify();
        Assertions.assertThat(sampleCompany.id()).isEqualTo("12345-12345");
    }

    @Test
    void shouldReturnNetworkExceptionWithExpectedMessageWhenServiceIsUnavailable() {
        // Given
        String companyId = "12345-12345";

        // when
        companyMockServer.expect(requestTo("https://company-service/api/company/12345-12345"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withException(new IOException("Company service gateway connection timed out.")));
                
        // then
        Assertions.assertThatThrownBy(() -> restService.getCompany(companyId))
                .isInstanceOf(CompanyServiceNetworkException.class)
                .hasMessage("Company service gateway connection timed out.");
        companyMockServer.verify();
    }

    /**
     * Test cases for increaseAndUpdateKnowledgeVersion method
     */

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { " " })
    void shouldReturnNullForIncreaseAndUpdateKnowledgeVersionWhenGivenInvalidCompanyId(String companyId) {
        CompanyDto result = restService.increaseAndUpdateKnowledgeVersion(companyId);
        Assertions.assertThat(result).isNull();
    }

    @Test
    void shouldReturnCompanyDtoWithUpdatedKnowledgeVersionWhenGivenValidCompanyId() {
        // Given
        String companyId = "12345-12345";

        // when
        companyMockServer.expect(requestTo("https://company-service/api/company/12345-12345/increase-knowledge-version"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                            "id": "12345-12345",
                            "name": "Sample Company",
                            "authorizedPersonEmail": "auth@samplecorp.com",
                            "knowledgeVersion": 2,
                            "createdAt": "2024-06-01 12:00:00",
                            "updatedAt": "2024-06-01 12:00:00"
                        }
                        """, MediaType.APPLICATION_JSON));

        // then
        CompanyDto sampleCompany = restService.increaseAndUpdateKnowledgeVersion(companyId);

        companyMockServer.verify();
        Assertions.assertThat(sampleCompany.id()).isEqualTo("12345-12345");
        Assertions.assertThat(sampleCompany.knowledgeVersion()).isEqualTo(2);
    }

    @Test
    void shouldReturnNetworkExceptionWithExpectedMessageWhenKnowledgeVersionServiceIsUnavailable() {
        // Given
        String companyId = "12345-12345";

        // when
        companyMockServer.expect(requestTo("https://company-service/api/company/12345-12345/increase-knowledge-version"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withException(new IOException("Company service gateway connection timed out.")));

        // then
        Assertions.assertThatThrownBy(() -> restService.increaseAndUpdateKnowledgeVersion(companyId))
                .isInstanceOf(CompanyServiceNetworkException.class)
                .hasMessage("Company service gateway connection timed out.");
        companyMockServer.verify();
    }

    /**
     * Test cases for getSimilarContextForCompany method
     */

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { " " })
    void shouldReturnNullForSimilarContextWhenGivenInvalidCompanyId(String companyId) {
        String result = restService.getSimilarContextForCompany(companyId, "Sample message");
        Assertions.assertThat(result).isNull();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { " " })
    void shouldReturnNullForSimilarContextWhenGivenInvalidMessage(String message) {
        String result = restService.getSimilarContextForCompany("123-123", message);
        Assertions.assertThat(result).isNull();
    }

    @Test
    void shouldReturnSimilarContextWhenGivenValidParameters() {
        // Given
        String companyId = "12345-12345";
        String message = "Sample";

        // when
        companyDocumentMockServer
                .expect(requestTo(startsWith("https:/company-document-service/api/company-document/similar-context")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("companyId", "12345-12345"))
                .andExpect(queryParam("message", "Sample"))
                .andRespond(
                    withSuccess(ResponseEntity.ok("Sample Similar Context.").getBody(),
                    MediaType.TEXT_PLAIN));

        // then
        String similarContext = restService.getSimilarContextForCompany(companyId, message);

        companyDocumentMockServer.verify();
        Assertions.assertThat(similarContext).isEqualTo("Sample Similar Context.");
    }

    @Test
    void shouldReturnNetworkExceptionWithExpectedMessageWhenSimilarContextServiceIsUnavailable() {
        // Given
        String companyId = "12345-12345";
        String message = "Sample";

        // when
        companyDocumentMockServer
                .expect(requestTo(startsWith("https:/company-document-service/api/company-document/similar-context")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withException(new IOException("Company document service gateway connection timed out.")));
        // then
        Assertions.assertThatThrownBy(() -> restService.getSimilarContextForCompany(companyId, message))
                .isInstanceOf(CompanyDocumentServiceNetworkException.class)
                .hasMessage("Company document service gateway connection timed out.");
        companyDocumentMockServer.verify();
    }
}
