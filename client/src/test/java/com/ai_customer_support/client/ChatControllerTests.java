package com.ai_customer_support.client;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.Timeout;
import static org.mockito.Mockito.when;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.evaluation.FactCheckingEvaluator;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.ai_customer_support.client.controller.ChatController;
import com.ai_customer_support.client.dto.CompanyDocumentInfo;
import com.ai_customer_support.client.model.Company;
import com.ai_customer_support.client.service.CompanyService;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(properties = {
	"spring.ai.open-ai.api-key=${OPENAI_API_KEY}",
	"logging.level.org.springframework.ai=DEBUG"})
public class ChatControllerTests {

    @Autowired 
    private ChatController chatController;

    @Autowired
    private ChatModel chatModel;

    private CompanyDocumentInfo companyDocumentInfo;
    private ChatClient chatClient;
	private FactCheckingEvaluator factCheckingEvaluator;

    @Value("classpath:prompttemplates/sampleCompanyPolicyTestTemplate.st")
    Resource sampleCompanyPolicyTestTemplate;

    @Value("classpath:prompttemplates/factCheckingSystemPromptTemplate.st")
    Resource factCheckingSystemPromptTemplate;

    @MockitoBean
    private CompanyService companyService; 

    @BeforeEach
	void setup() throws IOException{
		ChatClient.Builder chatClientBuilder = ChatClient.builder(chatModel)
				.defaultAdvisors(new SimpleLoggerAdvisor());
		this.chatClient = chatClientBuilder.build();
		this.factCheckingEvaluator = FactCheckingEvaluator.builder(chatClientBuilder)
                .evaluationPrompt(factCheckingSystemPromptTemplate.getContentAsString(Charset.defaultCharset()))
                .build();
        this.companyDocumentInfo = new CompanyDocumentInfo(null, null, null);
	}

    @Test
	@DisplayName("Should correctly evaluate factual response based on a company policy context(RAG scenario)")
	@Timeout(30)
	void evaluateChatControllerResponseFactually() throws IOException {
		// Given
        String question = "When is Customer Support Available?";
        String username = "testUser";
        String companyId = "12345-12345";

        Company sampleCompany = new Company();
        sampleCompany.setId(companyId);
        sampleCompany.setName("Sample Company");
        sampleCompany.setAuthorizedPersonEmail("auth@samplecorp.com");
        String retrievedContext = sampleCompanyPolicyTestTemplate.getContentAsString(Charset.defaultCharset());

        when(companyService.getCompany(companyId)).thenReturn(sampleCompany);
        when(companyService.getSimilarContext(companyId, question)).thenReturn(retrievedContext);
        when(companyService.getCompanyName(companyId)).thenReturn(sampleCompany.getName());

		// When
		String aiResponse = chatController.chat(companyId, username, question).getBody();
        
        EvaluationRequest evaluationRequest = new EvaluationRequest(
                question, List.of(new Document(retrievedContext)),
                aiResponse);
        EvaluationResponse evaluationResponse = factCheckingEvaluator.evaluate(evaluationRequest);

        // Then
		Assertions.assertThat(retrievedContext).isNotBlank();
		Assertions.assertThat(evaluationResponse.isPass())
                        .withFailMessage("""
                        ========================================
                        The response was not considered factually accurate.
                        Question: %s
                        Response: %s
                        Context: %s
                        ========================================
                        """, question, aiResponse, retrievedContext)
                        .isTrue();
	}

}
