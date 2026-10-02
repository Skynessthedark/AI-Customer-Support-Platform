package com.ai_customer_support.client.model;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "company_documents")
public class CompanyDocument extends EntityItem {

    private String documentTitle;

    @ManyToOne
    @JoinColumn(name = "company_id",
                referencedColumnName = "id",
                nullable = false)
    private Company company;

    public String getDocumentTitle() {
        return documentTitle;
    }
    public void setDocumentTitle(String documentTitle) {
        this.documentTitle = documentTitle;
    }
    public Company getCompany() {
        return company;
    }
    public void setCompany(Company company) {
        this.company = company;
    }
}
