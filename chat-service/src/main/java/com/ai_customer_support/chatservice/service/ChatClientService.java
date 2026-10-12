package com.ai_customer_support.chatservice.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.ai_customer_support.chatservice.dto.CompanyDto;

@Service
public class ChatClientService {

    private static final String DOCUMENTS = "documents";
    private static final String CACHE_COMPANY_ID = "company_id";
    private static final String COMPANY_NAME = "companyName";
    private static final String NO_RELEVANT_CONTEXT_MESSAGE = "No relevant context found for the provided message.";
    private static final String CACHE_KNOWLEDGE = "knowledge";

    private final ChatClient chatClient;
    private final RestService restService;

    @Value("classpath:/prompttemplates/companySystemPromptTemplate.st")
    Resource companySystemPromptTemplate;

    public ChatClientService(ChatClient chatClient, RestService restService) {
        this.chatClient = chatClient;
        this.restService = restService;
    }

    public String getAIResponseForCompanyCustomer(String companyId, String username, String message) {
        String similarContext = restService.getSimilarContextForCompany(companyId, message);

        if (similarContext == null || similarContext.isEmpty()) {
            return NO_RELEVANT_CONTEXT_MESSAGE;
        }

        CompanyDto company = restService.getCompany(companyId);

        return chatClient.prompt()
                .system(promptSystemSpec -> promptSystemSpec.text(companySystemPromptTemplate)
                        .param(COMPANY_NAME, company.name())
                        .param(DOCUMENTS, similarContext))
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, username)
                        .param(CACHE_COMPANY_ID, companyId)
                        .param(CACHE_KNOWLEDGE, company.knowledgeVersion()))
                .user(message)
                .call()
                .content();
    }
}
