package com.ai_customer_support.chatservice.config.property;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "services.company-document.endpoints")
public class CompanyDocumentServiceEndpoints {

    private String upload;
    private String similarContext;

    public String getUpload() {
        return upload;
    }

    public void setUpload(String upload) {
        this.upload = upload;
    }

    public String getSimilarContext() {
        return similarContext;
    }

    public void setSimilarContext(String similarContext) {
        this.similarContext = similarContext;
    }

    

}
