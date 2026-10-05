package com.ai_customer_support.client.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ai_customer_support.client.service.ChatClientService;

@RestController 
@RequestMapping ("/api/chat")
public class ChatController {

    private final ChatClientService chatClientService;

    public ChatController(ChatClientService chatClientService) {
        this.chatClientService = chatClientService;
    }

    @GetMapping
    public ResponseEntity<String> chat(@RequestHeader(name = "companyId") String companyId,
                                        @RequestHeader(name = "username") String username,
                                        @RequestParam(name = "message") String message) {
        return ResponseEntity.ok(chatClientService.getAIResponseForCompanyCustomer(companyId, username, message));
    }
    
}
