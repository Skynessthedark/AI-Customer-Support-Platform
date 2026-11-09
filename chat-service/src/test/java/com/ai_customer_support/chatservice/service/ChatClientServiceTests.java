package com.ai_customer_support.chatservice.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;

import com.ai_customer_support.chatservice.dto.CompanyDto;

@ExtendWith(MockitoExtension.class)
public class ChatClientServiceTests {

    private static final String NO_RELEVANT_CONTEXT_MESSAGE = "No relevant context found for the provided message.";

    private ChatClient chatClient;
    private ChatClientService chatClientService;

    @Mock
    private RestService restService;

    @Mock
    private ChatModel chatModelMock;

    @BeforeEach
    void setUp() {
        chatClient = ChatClient.builder(chatModelMock).build();
        chatClientService = new ChatClientService(chatClient, restService);

        String mockPromptText = "Company: {companyName}, Docs: {documents}";
        Resource mockResource = new ByteArrayResource(mockPromptText.getBytes());

        ReflectionTestUtils.setField(
        chatClientService, 
        "companySystemPromptTemplate", 
        mockResource);
    }

	@DisplayName("Should return no relevant context message when no similar context found")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void shouldReturnNoRelevantContextMessageWhenSimilarContextIsNullOrEmpty(String similarContext) {
        // Given
        String companyId = "12345-12345";
        String username = "testUser";
        String message = "Test message";

        //When
        when(restService.getSimilarContextForCompany(companyId, message)).thenReturn(similarContext);

        String response = chatClientService.getAIResponseForCompanyCustomer(companyId, username, message);

        // Then
        Assertions.assertThat(response).isEqualTo(NO_RELEVANT_CONTEXT_MESSAGE);
        verify(restService, never()).getCompany(companyId);
        verify(chatModelMock, never()).call((any(Prompt.class)));
    }

    @Test
    void shouldReturnMockedAiResponseWithoutHittingRealModel() {
        //Given
        String message = "This is a mocked message for testing.";
        String response = "This is a mocked AI response.";
        String username = "testUser";

        AssistantMessage assistantMessage = new AssistantMessage(response);
        ChatResponse mockResponse = new ChatResponse(List.of(new Generation(assistantMessage)));

        
        CompanyDto sampleCompany = getSampleCompany();

        //when
        when(chatModelMock.getOptions()).thenReturn(OpenAiChatOptions.builder().build());
        when(restService.getSimilarContextForCompany(any(), any())).thenReturn("Sample context");
        when(restService.getCompany(any())).thenReturn(sampleCompany);
        when(chatModelMock.call(any(Prompt.class))).thenReturn(mockResponse);

        //then
        String result = chatClientService.getAIResponseForCompanyCustomer(sampleCompany.id(), username, message);

        Assertions.assertThat(result).isEqualTo(response);
        verify(restService, times(1)).getCompany(sampleCompany.id());
        verify(chatModelMock, times(1)).call((any(Prompt.class)));
    }


    @Test
    void shouldArgumentCaptorVerifyTheGivenPrompt() {
        // Given

        String message = "This is a mocked message for testing.";
        String response = "This is a mocked AI response.";
        String username = "testUser";

        AssistantMessage assistantMessage = new AssistantMessage(response);
        ChatResponse mockResponse = new ChatResponse(List.of(new Generation(assistantMessage)));

        CompanyDto sampleCompany = getSampleCompany();

        ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);

        // when
        when(chatModelMock.getOptions()).thenReturn(OpenAiChatOptions.builder().build());
        when(restService.getSimilarContextForCompany(any(), any())).thenReturn("Sample context");
        when(restService.getCompany(any())).thenReturn(sampleCompany);
        when(chatModelMock.call(any(Prompt.class))).thenReturn(mockResponse);

        // then
        chatClientService.getAIResponseForCompanyCustomer(sampleCompany.id(), username, message);

        verify(chatModelMock).call(promptCaptor.capture());
        Prompt capturedPrompt = promptCaptor.getValue();
        
        Assertions.assertThat(capturedPrompt).isNotNull();
        Assertions.assertThat(capturedPrompt.getSystemMessage().getText()).isEqualTo("Company: Sample Company, Docs: Sample context");
        Assertions.assertThat(capturedPrompt.getUserMessage().getText()).isEqualTo(message);
    }

    private CompanyDto getSampleCompany(){
        return new CompanyDto("12345-12345",
                "Sample Company", "auth@samplecorp.com",
                null, null, 1, LocalDateTime.now(), LocalDateTime.now());
    }
}
