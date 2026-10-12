package com.ai_customer_support.chatservice.exception;

import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.HttpStatusCodeException;

public class CompanyServiceException extends HttpStatusCodeException{
    public CompanyServiceException(HttpStatusCode statusCode, String statusText, byte[] responseBody) {
        super(statusCode, statusText, null, responseBody, null);
    }
}
