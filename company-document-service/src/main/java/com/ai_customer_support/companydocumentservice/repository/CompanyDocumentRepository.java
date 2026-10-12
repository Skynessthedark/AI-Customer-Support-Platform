package com.ai_customer_support.companydocumentservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ai_customer_support.companydocumentservice.model.CompanyDocument;

public interface CompanyDocumentRepository extends JpaRepository<CompanyDocument, String> {

    public List<CompanyDocument> findByCompanyId(String companyId);

}
