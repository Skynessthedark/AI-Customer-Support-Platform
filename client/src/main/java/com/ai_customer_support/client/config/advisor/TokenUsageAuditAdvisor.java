package com.ai_customer_support.client.config.advisor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;

public class TokenUsageAuditAdvisor implements CallAdvisor{

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenUsageAuditAdvisor.class);

    @Override
    public String getName() {
        return "TokenUsageAuditAdvisor";
    }

    @Override
    public int getOrder() {
        return 1;
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        ChatClientResponse response = callAdvisorChain.nextCall(chatClientRequest);
        ChatResponse chatResponse = response.chatResponse();

        if(chatResponse != null){
            Usage usage = chatResponse.getMetadata().getUsage();
            if(usage != null)
                LOGGER.info("Token Usage: {}", usage.toString());
        }
        return response;
    }

}
