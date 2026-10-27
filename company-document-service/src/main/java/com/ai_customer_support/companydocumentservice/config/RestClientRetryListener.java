package com.ai_customer_support.companydocumentservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.retry.RetryException;
import org.springframework.core.retry.RetryListener;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryState;
import org.springframework.core.retry.Retryable;

public class RestClientRetryListener implements RetryListener{

    private static final Logger LOGGER = LoggerFactory.getLogger(RestClientRetryListener.class);

    //Every try
    @Override
    public void onRetryableExecution(RetryPolicy retryPolicy, Retryable<?> retryable, RetryState retryState) {
        int attempt = retryState.getRetryCount() + 1;
        if (retryState.isSuccessful()) {
            LOGGER.info("[Retry Successful] Request attempt {} is successful.", attempt);
        } else {
            Throwable lastError = retryState.getLastException();
            LOGGER.warn("[Retry Failure] Request attempt #{} failed. Error: {}", 
                    attempt, lastError != null ? lastError.getMessage() : "UNKNOWN");
        }
    }

    // Every failure
    @Override
    public void onRetryFailure(RetryPolicy retryPolicy, Retryable<?> retryable, Throwable throwable) {
        LOGGER.warn("[Retry Failure] Request attempt failed. Error: {}", throwable.getMessage());
    }

    //After first failure, before retry
    @Override
    public void beforeRetry(RetryPolicy retryPolicy, Retryable<?> retryable, RetryState retryState) {
        int nextAttempt = retryState.getRetryCount() + 1;
        LOGGER.info("Preparing for Request attempt #{} ...", nextAttempt);
    }

    // after max attempts exhausted
    @Override
    public void onRetryPolicyExhaustion(RetryPolicy retryPolicy, Retryable<?> retryable, RetryException exception) {
        LOGGER.error("[Retry Exhausted] Maximum retry attempts exhausted or timeout exceed! Total retry: {}. Last Error: {}", 
                exception.getRetryCount(), 
                exception.getCause() != null ? exception.getCause().getMessage() : exception.getMessage());
    }

    //Timeout
    @Override
    public void onRetryPolicyTimeout(RetryPolicy retryPolicy, Retryable<?> retryable, RetryException exception) {
        LOGGER.error("⏱️ [Retry Timeout] Last Error: {}", 
                exception.getCause() != null ? exception.getCause().getMessage() : exception.getMessage());
    }
}
