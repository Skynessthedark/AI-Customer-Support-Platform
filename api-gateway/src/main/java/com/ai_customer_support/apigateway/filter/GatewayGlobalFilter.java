package com.ai_customer_support.apigateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component 
public class GatewayGlobalFilter implements GlobalFilter, Ordered{

    private static final Logger LOGGER = LoggerFactory.getLogger(GatewayGlobalFilter.class);
    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        LOGGER.info("Global Pre-Filter: Request Address -> {}", exchange.getRequest().getURI().getPath());

        return chain.filter(exchange)
                .then(Mono.fromRunnable(() -> {
                    LOGGER.info("Global Post-Filter: Response Status -> {}", exchange.getResponse().getStatusCode());
                }));
    }

}
