package com.ai_customer_support.client.config;

import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.VectorStoreChatMemoryAdvisor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration 
public class VectorStoreConfig {

    private static final int OPENAI_DIMENSION = 1536;

    @Bean("companyVectorStore")
    public VectorStore companyVectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel) {
        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
                .vectorTableName("company_document_chunks")
                .dimensions(OPENAI_DIMENSION)
                .initializeSchema(true)
                .build();
    }

    @Bean(name="chatHistoryVectorStore")
    public VectorStore chatHistoryVectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel) {
        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
                .vectorTableName("customer_chat_history")
                .dimensions(OPENAI_DIMENSION)
                .initializeSchema(true)
                .build();
    }

    @Bean
    public QuestionAnswerAdvisor companyVectorStoreAdvisor(@Qualifier("companyVectorStore") VectorStore vectorStore) {
        SearchRequest searchRequest = SearchRequest.builder()
                                            .topK(3)
                                            .build();
        return QuestionAnswerAdvisor.builder(vectorStore)
                                    .searchRequest(searchRequest)
                                    .build();
    }

    @Bean
    public VectorStoreChatMemoryAdvisor chatHistoryVectorStoreAdvisor(@Qualifier("chatHistoryVectorStore") VectorStore vectorStore) {
        return VectorStoreChatMemoryAdvisor.builder(vectorStore)
                .defaultTopK(10)
                .build();
    }
}
