package com.ai_customer_support.companydocumentservice.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter.Expression;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;

import com.ai_customer_support.companydocumentservice.config.property.SimilaritySearchProperties;
import com.ai_customer_support.companydocumentservice.dto.CompanyDto;
import com.ai_customer_support.companydocumentservice.exception.CompanyServiceException;
import com.ai_customer_support.companydocumentservice.exception.CompanyServiceNetworkException;

@Service 
public class DocumentRetrievalService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentRetrievalService.class);

    private static final String COMPANY_ID = "companyId";

    private final RestService restService;
    private final SimilaritySearchProperties similaritySearchProperties;
    private final VectorStore vectorStore;

    public DocumentRetrievalService(RestService restService, SimilaritySearchProperties similaritySearchProperties, 
        VectorStore vectorStore){
        this.restService = restService;
        this.similaritySearchProperties = similaritySearchProperties;
        this.vectorStore = vectorStore;
    }


    @SuppressWarnings("null")
    public String getSimilarContext(String companyId, String message) {
        CompanyDto company = null;

        try{
            company = restService.getCompany(companyId);
        } catch (CompanyServiceNetworkException | CompanyServiceException e) {
            LOGGER.error("An error occurred about Company Service. ");
            return null;
        }

        if (company == null || company.id() == null) {
            LOGGER.info("No company found by id: {}", companyId);
            return null;
        }

        SearchRequest req = SearchRequest.builder()
                .query(message)
                .topK(similaritySearchProperties.getTopK())
                .similarityThreshold(similaritySearchProperties.getThreshold())
                .filterExpression(getFilterExpressionForQuery(companyId))
                .build();
        List<Document> similarDocs = vectorStore.similaritySearch(req);

        if (similarDocs.isEmpty()) {
            LOGGER.info("No similar context found for companyId: {} and message: {}", companyId, message);
            return null;
        }

        return similarDocs.stream()
                .map(Document::getText)
                .collect(Collectors.joining(System.lineSeparator()));
    }

    private Expression getFilterExpressionForQuery(String companyId) {
        FilterExpressionBuilder feb = new FilterExpressionBuilder();
        return feb.eq(COMPANY_ID, companyId).build();
    }
}
