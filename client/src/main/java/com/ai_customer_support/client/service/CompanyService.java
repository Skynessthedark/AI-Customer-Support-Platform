package com.ai_customer_support.client.service;

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

import com.ai_customer_support.client.config.property.SimilaritySearchProperties;
import com.ai_customer_support.client.exception.CompanyNotFoundException;
import com.ai_customer_support.client.model.Company;
import com.ai_customer_support.client.repository.CompanyRepository;
@Service 
public class CompanyService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompanyService.class);
    private static final String COMPANY_ID = "companyId";

    private final CompanyRepository companyRepository;
    private final VectorStore vectorStore;
    private final SimilaritySearchProperties similaritySearchProperties;

    public CompanyService(CompanyRepository companyRepository, VectorStore vectorStore, SimilaritySearchProperties similaritySearchProperties) {
        this.companyRepository = companyRepository;
        this.vectorStore = vectorStore;
        this.similaritySearchProperties = similaritySearchProperties;
    }

    public boolean isCompanyExists(String companyId) {
        return companyRepository.existsById(companyId);
    }

    public Company getCompany(String companyId) {
        return companyRepository.findById(companyId)
            .orElseThrow(() -> new CompanyNotFoundException("Company with ID " + companyId + " does not exist."));
    }

    public Company getCompanyByAuthorizedPersonEmail(String email) {
        return companyRepository.findByAuthorizedPersonEmail(email)
            .orElseThrow(() -> new CompanyNotFoundException("Company with authorized person email " + email + " does not exist."));
    }

    @SuppressWarnings("null")
    public String getSimilarContext(String companyId, String message) {
        if (!isCompanyExists(companyId)) {
            throw new CompanyNotFoundException("Company with ID " + companyId + " does not exist.");
        }

        SearchRequest req = SearchRequest.builder()
                .query(message)
                .topK(similaritySearchProperties.getTopK())
                .similarityThreshold(similaritySearchProperties.getThreshold())
                .filterExpression(getFilterExpressionForQuery(companyId))
                .build();
        List<Document> similarDocs = vectorStore.similaritySearch(req);

        if(similarDocs.isEmpty()) {
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

    public String getCompanyName(String companyId) {
        return getCompany(companyId).getName();
    }
}
