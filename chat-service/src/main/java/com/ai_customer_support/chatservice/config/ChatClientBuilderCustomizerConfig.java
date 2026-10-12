package com.ai_customer_support.chatservice.config;

import org.springframework.ai.chat.client.ChatClientBuilderCustomizer;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ai_customer_support.chatservice.config.advisor.TokenUsageAuditAdvisor;

@Configuration
public class ChatClientBuilderCustomizerConfig {

    @Bean
    ChatClientBuilderCustomizer loggerCustomizer() {
        return builder -> builder.defaultAdvisors(new SimpleLoggerAdvisor());
    }

    @Bean
    @ConditionalOnProperty(name = "audit.token-usage.enabled", havingValue = "true")
    ChatClientBuilderCustomizer auditCustomizer() {
        return builder -> builder.defaultAdvisors(new TokenUsageAuditAdvisor());
    }

}
