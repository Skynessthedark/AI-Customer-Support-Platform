package com.ai_customer_support.client.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.VectorStoreChatMemoryAdvisor;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ai_customer_support.client.config.advisor.CompanySemanticCacheAdvisor;

@Configuration
public class ChatClientConfig {

    private static final String DEFAULT_USER_MESSAGE = "What is your purpose and how can you help me?";
    private static final String DEFAULT_SYSTEM_MESSAGE = """
        You are an AI customer support assistant.\s
        Answer user questions clearly, accurately, and professionally.\s
        If you are unsure about an answer, state that you do not have enough information instead of making up facts.\s
        """;
    private static final String MODEL = "gpt-5-mini";
    private static final String MODEL_VERBOSITY = "low";

    @Bean
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder,
                                 CompanySemanticCacheAdvisor companySemanticCacheAdvisor,
                                 QuestionAnswerAdvisor companyVectorStoreAdvisor,
                                 VectorStoreChatMemoryAdvisor chatHistoryVectorStoreAdvisor) {
        var options = OpenAiChatOptions.builder()
                .model(MODEL).verbosity(MODEL_VERBOSITY);

        return chatClientBuilder
            .defaultAdvisors(companyVectorStoreAdvisor,
                                            chatHistoryVectorStoreAdvisor,
                                            companySemanticCacheAdvisor)
            .defaultSystem(DEFAULT_SYSTEM_MESSAGE)
            .defaultOptions(options)
            .defaultUser(DEFAULT_USER_MESSAGE)
            .build();
    }
}
