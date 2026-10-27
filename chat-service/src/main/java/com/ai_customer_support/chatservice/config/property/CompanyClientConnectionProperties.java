package com.ai_customer_support.chatservice.config.property;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "client.company.connection")
public class CompanyClientConnectionProperties {
    private Long readTimeout = 3L;
    
    private Long connectTimeout = 3L;

    private Long retryMaxAttempts = 3L;
    private Long retryBackoffDelayMs = 500L;

    public Long getRetryMaxAttempts() {
        return retryMaxAttempts;
    }

    public void setRetryMaxAttempts(Long retryMaxAttempts) {
        this.retryMaxAttempts = retryMaxAttempts;
    }

    public Long getRetryBackoffDelayMs() {
        return retryBackoffDelayMs;
    }

    public void setRetryBackoffDelayMs(Long retryBackoffDelayMs) {
        this.retryBackoffDelayMs = retryBackoffDelayMs;
    }

    public Long getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Long connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Long getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Long readTimeout) {
        this.readTimeout = readTimeout;
    }
    
}
