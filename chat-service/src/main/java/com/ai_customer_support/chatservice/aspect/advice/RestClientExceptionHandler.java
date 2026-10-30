package com.ai_customer_support.chatservice.aspect.advice;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.ai_customer_support.chatservice.exception.CompanyServiceException;
import com.ai_customer_support.chatservice.exception.CompanyServiceNetworkException;
import com.ai_customer_support.chatservice.exception.CompanyDocumentServiceException;
import com.ai_customer_support.chatservice.exception.CompanyDocumentServiceNetworkException;

@RestControllerAdvice
public class RestClientExceptionHandler {

    @ExceptionHandler(CompanyServiceException.class)
    public ProblemDetail handleCompanyServiceException(CompanyServiceException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            ex.getStatusCode(),
            "Company Service returned an error" + ex.getStatusText()
        );
        problemDetail.setTitle("Company Service Failure");
        problemDetail.setProperty("service", "COMPANY-SERVICE");
        problemDetail.setProperty("rawError", ex.getResponseBodyAsString());
        
        return problemDetail;
    }

    @ExceptionHandler(CompanyDocumentServiceException.class)
    public ProblemDetail handleCompanyDocumentServiceException(CompanyDocumentServiceException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            ex.getStatusCode(),
            "Company Document Service returned an error" + ex.getStatusText()
        );
        problemDetail.setTitle("Company Document Service Failure");
        problemDetail.setProperty("service", "COMPANY-DOCUMENT-SERVICE");
        problemDetail.setProperty("rawError", ex.getResponseBodyAsString());
        
        return problemDetail;
    }

    @ExceptionHandler(CompanyServiceNetworkException.class)
    public ProblemDetail handleCompanyServiceNetworkException(CompanyServiceNetworkException ex) {

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatusCode.valueOf(504),
                "The company information service is temporarily unavailable. Please try again later."
        );
        problemDetail.setTitle("Service Temporarily Unavailable");
        problemDetail.setProperty("service", "COMPANY-SERVICE");
        problemDetail.setProperty("error_type", "DEPENDENCY_UNAVAILABLE");
        return problemDetail;
    }

    @ExceptionHandler(CompanyDocumentServiceNetworkException.class)
    public ProblemDetail handleCompanyDocumentServiceNetworkException(CompanyDocumentServiceNetworkException ex) {

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatusCode.valueOf(504),
                "The company document information service is temporarily unavailable. Please try again later.");
        problemDetail.setTitle("Service Temporarily Unavailable");
        problemDetail.setProperty("service", "COMPANY-DOCUMENT-SERVICE");
        problemDetail.setProperty("error_type", "DEPENDENCY_UNAVAILABLE");
        return problemDetail;
    }
}
