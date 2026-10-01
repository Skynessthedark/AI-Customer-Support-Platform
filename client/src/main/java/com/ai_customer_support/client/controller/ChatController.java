package com.ai_customer_support.client.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController 
@RequestMapping ("/api/chat")
public class ChatController {

    private final ChatClient chatClient;

    public ChatController(@Qualifier("openaiChatClient") ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @GetMapping
    public String chat(@RequestParam(name = "message") String message) {
        return chatClient
            .prompt(message)
            .call()
            .content();
    }
}
