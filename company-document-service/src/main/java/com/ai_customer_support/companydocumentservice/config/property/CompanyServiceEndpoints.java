package com.ai_customer_support.companydocumentservice.config.property;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "services.company.endpoints")
public class CompanyServiceEndpoints {

    private String companyById;
    private String knowledgeVersionIncrement;

    public String getCompanyById() {
        return companyById;
    }

    public void setCompanyById(String companyById) {
        this.companyById = companyById;
    }

    public String getKnowledgeVersionIncrement() {
        return knowledgeVersionIncrement;
    }

    public void setKnowledgeVersionIncrement(String knowledgeVersionIncrement) {
        this.knowledgeVersionIncrement = knowledgeVersionIncrement;
    }

}
