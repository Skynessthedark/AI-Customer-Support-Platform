package com.ai_customer_support.client.config;

import org.springframework.ai.chat.cache.semantic.SemanticCache;
import org.springframework.ai.chat.cache.semantic.SemanticCacheAdvisor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.redis.cache.semantic.DefaultSemanticCache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import redis.clients.jedis.RedisClient;

@Configuration
public class RedisConfig {

    @Bean
    RedisClient redisClient(
            @Value("${spring.data.redis.host}") String host,
            @Value("${spring.data.redis.port}") int port) {
        return RedisClient.builder().hostAndPort(host, port).build();
    }

    @Bean
    public SemanticCache semanticCache(RedisClient redisClient,
            EmbeddingModel embeddingModel,
            @Value("${spring.ai.vectorstore.redis.index-name}") String indexName,
            @Value("${spring.ai.vectorstore.redis.prefix}") String prefix,
            @Value("${spring.ai.vectorstore-redis-semantic-cache.threshold}") Double threshold) {
        return DefaultSemanticCache.builder()
                .jedisClient(redisClient)
                .embeddingModel(embeddingModel)
                .similarityThreshold(threshold)
                .indexName(indexName)
                .prefix(prefix)
                .build();
    }

    @Bean
    public SemanticCacheAdvisor semanticCacheAdvisor(SemanticCache semanticCache) {
        return SemanticCacheAdvisor.builder().cache(semanticCache).build();
    }
}
