package com.ai_customer_support.apigateway.filter;

import java.util.UUID;

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
    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String generatedCorrelationID = generateCorrelationID();
        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(r -> r.header(CORRELATION_ID_HEADER, generatedCorrelationID))
                .build();

        // CorrelationID
        LOGGER.info("Global Pre-Filter[{}]: Request Address -> {}", 
                getCorrelationIDFromRequest(mutatedExchange),
            mutatedExchange.getRequest().getURI().getPath());

        return chain.filter(mutatedExchange)
                .then(Mono.fromRunnable(() -> {
                    mutatedExchange.getResponse().getHeaders().set(CORRELATION_ID_HEADER, generatedCorrelationID);
                    LOGGER.info("Global Post-Filter[{}]: Response Status -> {}",
                        getCorrelationIDFromResponse(mutatedExchange),
                            mutatedExchange.getResponse().getStatusCode());
                }));
    }

    private String generateCorrelationID(){
        return UUID.randomUUID().toString();
    }

    private String getCorrelationIDFromRequest(ServerWebExchange exchange){
        return exchange.getRequest().getHeaders().get(CORRELATION_ID_HEADER).getFirst();
    }

    private String getCorrelationIDFromResponse(ServerWebExchange exchange) {
        return exchange.getResponse().getHeaders().get(CORRELATION_ID_HEADER).getFirst();
    }

}
