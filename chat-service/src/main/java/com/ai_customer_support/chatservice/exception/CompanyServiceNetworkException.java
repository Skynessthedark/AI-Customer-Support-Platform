package com.ai_customer_support.chatservice.exception;

public class CompanyServiceNetworkException extends RuntimeException{
    public CompanyServiceNetworkException(String message, Throwable cause) {
        super(message, cause);
    }
}
