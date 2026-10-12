package com.ai_customer_support.companydocumentservice.exception;

public class CompanyServiceNetworkException extends RuntimeException{
    public CompanyServiceNetworkException(String message, Throwable cause) {
        super(message, cause);
    }
}
