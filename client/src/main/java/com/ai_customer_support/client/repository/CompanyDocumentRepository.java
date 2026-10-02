package com.ai_customer_support.client.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ai_customer_support.client.model.CompanyDocument;

public interface CompanyDocumentRepository extends JpaRepository<CompanyDocument, String> {

    public List<CompanyDocument> findByCompanyId(String companyId);

}
