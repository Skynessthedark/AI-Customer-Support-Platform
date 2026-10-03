package com.ai_customer_support.client.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ai_customer_support.client.service.CompanyService;

@RestController 
@RequestMapping ("/api/chat")
public class ChatController {

    private static final String DOCUMENTS = "documents";
    private static final String COMPANY_NAME = "companyName";
    
    private final ChatClient chatClient;
    private final CompanyService companyService;

    @Value("classpath:/prompttemplates/companySystemPromptTemplate.st")
    Resource companySystemPromptTemplate;

    public ChatController(ChatClient chatClient, CompanyService companyService) {
        this.chatClient = chatClient;
        this.companyService = companyService;
    }

    @GetMapping
    public ResponseEntity<String> chat(@RequestHeader(name = "companyId") String companyId,
                                        @RequestHeader(name = "username") String username,
                                        @RequestParam(name = "message") String message) {
        String similarContext = companyService.getSimilarContext(companyId, message);
        
        if (similarContext == null || similarContext.isEmpty()) {
            return ResponseEntity.ok("No relevant context found for the provided message.");
        }

        String response = chatClient.prompt()
            .system(promptSystemSpec -> promptSystemSpec.text(companySystemPromptTemplate)
                        .param(COMPANY_NAME, companyService.getCompanyName(companyId))
                        .param(DOCUMENTS, similarContext))
            .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, username))
            .user(message)
            .call()
            .content();
        return ResponseEntity.ok(response);
    }
    
}
