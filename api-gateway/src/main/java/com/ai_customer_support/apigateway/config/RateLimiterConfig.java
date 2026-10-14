package com.ai_customer_support.apigateway.config;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import reactor.core.publisher.Mono;

@Configuration 
public class RateLimiterConfig {

    private static final Pattern COMPANY_ID_PATH_PATTERN = Pattern.compile("/api/(?:company/)?([^/]+)");
    
    @Bean
    @Primary 
    KeyResolver companyKeyResolver() {

        return exchange -> {

            //Header
            String companyId = exchange.getRequest().getHeaders().getFirst("X-Company-Id");

            // Req Param
            if (companyId == null || companyId.isEmpty()) {
                companyId = exchange.getRequest().getQueryParams().getFirst("companyId");
            }

            // Path
            if (companyId == null || companyId.isEmpty()) {
                String path = exchange.getRequest().getURI().getPath();
                Matcher matcher = COMPANY_ID_PATH_PATTERN.matcher(path);

                if (matcher.find())
                    companyId = matcher.group(1); 
            }

            //None
            if (companyId == null || companyId.isEmpty()) {
                companyId = "anonymous-company";
            }

            return Mono.just(companyId);
        };
    }

    @Bean
    KeyResolver userAndCompanyKeyResolver() {
        return exchange -> {
            String username = exchange.getRequest().getHeaders().getFirst("X-Username");
            username = (username != null) ? username : "anonymous-user";

            String companyId = exchange.getRequest().getHeaders().getFirst("X-Company-Id");
            companyId = (companyId != null) ? companyId : "anonymous-company";

            return Mono.just(username + "_user:" + companyId + "_company");
        };
    }
}
