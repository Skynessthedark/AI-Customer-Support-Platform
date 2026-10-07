package com.ai_customer_support.client.config.advisor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.cache.semantic.SemanticCache;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Component;

@Component 
public class CompanySemanticCacheAdvisor implements CallAdvisor{

    private static final Logger LOGGER = LoggerFactory.getLogger(CompanySemanticCacheAdvisor.class);
    
    private static final String CACHE_COMPANY_ID = "company_id";
    private static final String CACHE_KNOWLEDGE = "knowledge";
    private static final String ALGORTIHM_TYPE = "SHA-256";

    private final SemanticCache semanticCache;

    public CompanySemanticCacheAdvisor(SemanticCache semanticCache) {
        this.semanticCache = semanticCache;
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        String companyId = (String) chatClientRequest.context().get(CACHE_COMPANY_ID);
        String knowledgeVersion = String.valueOf(chatClientRequest.context().get(CACHE_KNOWLEDGE));
        String conversationId = String.valueOf(chatClientRequest.context().get(ChatMemory.CONVERSATION_ID));

        String contextHash = createContextHash(companyId, knowledgeVersion, conversationId);

        String query = chatClientRequest.prompt().getUserMessage().getText();

        var chatResponse = semanticCache.get(query, contextHash);
        
        if (chatResponse.isPresent()) {
            return ChatClientResponse.builder()
                    .chatResponse(chatResponse.get())
                    .context(chatClientRequest.context())
                    .build();
        }

        ChatClientResponse response = callAdvisorChain.nextCall(chatClientRequest);

        if (response.chatResponse() != null
                && response.chatResponse().getResult() != null
                && response.chatResponse().getResult().getOutput() != null) {
            semanticCache.set(query, response.chatResponse(), contextHash);
        }

        return response;
    }

    private String createContextHash(String companyId, String knowledgeVersion, String conversationId) {
        String context = companyId + ":" + knowledgeVersion + ":" + conversationId;

        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORTIHM_TYPE);

            return HexFormat.of().formatHex(digest
                        .digest(context.getBytes(StandardCharsets.UTF_8)));

        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "Unable to create cache context hash", e);
        }
    }

    @Override
    public String getName() {
        return "CompanySemanticCacheAdvisor";
    }

    @Override
    public int getOrder() {
        return 1;
    }

}
