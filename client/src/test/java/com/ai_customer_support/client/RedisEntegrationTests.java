package com.ai_customer_support.client;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.ai_customer_support.client.model.Company;
import com.ai_customer_support.client.service.ChatClientService;
import com.ai_customer_support.client.service.CompanyService;

@Testcontainers
@SpringBootTest
@TestPropertySource(properties = {
        "spring.ai.open-ai.api-key=${OPENAI_API_KEY}",
        "logging.level.org.springframework.ai=DEBUG" })
public class RedisEntegrationTests {

    @Container
    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("pgvector/pgvector:pg16")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Container
    static GenericContainer<?> redisContainer = new GenericContainer<>(
            DockerImageName.parse("redis/redis-stack:latest"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", () -> redisContainer.getMappedPort(6379));

        registry.add("spring.datasource.url",
                postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username",
                postgresContainer::getUsername);
        registry.add("spring.datasource.password",
                postgresContainer::getPassword);
    }

    @Autowired
    private ChatClientService chatClientService;

    @MockitoBean
    private ChatModel chatModel;

    @MockitoBean
    private CompanyService companyService;

    @Value("classpath:prompttemplates/sampleCompanyPolicyTestTemplate.st")
    Resource sampleCompanyPolicyTestTemplate;

    @Test
    void testSimilaritySearch_AndExpectCacheMissOnce() throws IOException {

        String message = "What is the refund policy?";

        String companyId = "1111";
        Company sampleCompany = new Company();
        sampleCompany.setId(companyId);
        sampleCompany.setName("Sample Company");
        sampleCompany.setAuthorizedPersonEmail("auth@samplecorp.com");

        String username = "testUser";

        // when
        when(companyService.getCompany(companyId)).thenReturn(sampleCompany);

        String retrievedContext = sampleCompanyPolicyTestTemplate.getContentAsString(Charset.defaultCharset());
        when(companyService.getSimilarContext(companyId, message)).thenReturn(retrievedContext);

        when(companyService.getCompanyName(companyId)).thenReturn(sampleCompany.getName());

        when(chatModel.getOptions()).thenReturn(ChatOptions.builder().build());

        ChatResponse chatResponse = new ChatResponse(
            List.of(new Generation(
                new AssistantMessage("Your refund policy is 30 days."))));
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        //then

        String firstResponse = chatClientService.getAIResponseForCompanyCustomer(companyId, username, message);

        String secondResponse = chatClientService.getAIResponseForCompanyCustomer(companyId, username, message);

        Assertions.assertThat(firstResponse).isEqualTo(secondResponse);

        verify(chatModel, times(1))
                .call(any(Prompt.class));
    }
}
