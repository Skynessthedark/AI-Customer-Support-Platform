package com.ai_customer_support.client.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAIChatClientConfig {

    private static final String DEFAULT_USER_MESSAGE = "What is your purpose and how can you help me?";
    private static final String MODEL = "gpt-5-mini";

    @Bean(name="openaiChatClient")
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder) {
        var options = OpenAiChatOptions.builder()
                .model(MODEL);

        return chatClientBuilder
            .defaultOptions(options)
            .defaultUser(DEFAULT_USER_MESSAGE)
            .build();
    }
}
