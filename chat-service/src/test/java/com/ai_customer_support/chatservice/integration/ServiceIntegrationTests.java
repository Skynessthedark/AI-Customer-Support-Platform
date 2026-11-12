package com.ai_customer_support.chatservice.integration;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.ai.chat.cache.semantic.SemanticCache;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ai_customer_support.chatservice.config.advisor.CompanySemanticCacheAdvisor;
import com.ai_customer_support.chatservice.dto.CompanyDto;
import com.ai_customer_support.chatservice.service.RestService;

import redis.clients.jedis.RedisClient;

@SpringBootTest
@AutoConfigureMockMvc
public class ServiceIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RestService restService;

    @MockitoBean
    private CompanySemanticCacheAdvisor companySemanticCacheAdvisor;

    @MockitoBean
    private VectorStore vectorStore;

    @MockitoBean
    private SemanticCache semanticCache;

    @MockitoBean
    private RedisClient redisClient;

    @MockitoBean
    private ChatModel chatModel;

    @Test
    void shouldReturnValidResponseWhenAIIsCalled() throws Exception {
        // Given
        String companyId = "12345-12345";
        String username = "testuser";
        String message = "Sample message";

        String similarContext = "Sample context";
        String aiResponse = "Sample AI response";
        AssistantMessage assistantMessage = new AssistantMessage(aiResponse);
        ChatResponse mockResponse = new ChatResponse(List.of(new Generation(assistantMessage)));

        // when
        when(chatModel.getOptions()).thenReturn(OpenAiChatOptions.builder().build());
        when(companySemanticCacheAdvisor.getName()).thenReturn("CompanySemanticCacheAdvisor");

        when(companySemanticCacheAdvisor.adviseCall(any(ChatClientRequest.class), any(CallAdvisorChain.class)))
                .thenAnswer(invocation -> {
                    ChatClientRequest request = invocation.getArgument(0);
                    CallAdvisorChain chain = invocation.getArgument(1);
                    return chain.nextCall(request);
                });

        when(restService.getSimilarContextForCompany(companyId, message)).thenReturn(similarContext);
        when(restService.getCompany(companyId)).thenReturn(getSampleCompany());
        when(chatModel.call(any(Prompt.class))).thenReturn(mockResponse);

        mockMvc.perform(get("/api/chat")
                .header("companyId", companyId)
                .header("username", username)
                .param("message", message))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(aiResponse));

        verify(restService, times(1)).getSimilarContextForCompany(companyId, message);
        verify(restService, times(1)).getCompany(companyId);
        verify(chatModel, times(1)).call(any(Prompt.class));
    }

    private CompanyDto getSampleCompany() {
        return new CompanyDto("12345-12345",
                "Sample Company", "auth@samplecorp.com",
                null, null, 1, LocalDateTime.now(), LocalDateTime.now());
    }
}
