package com.ai_customer_support.companydocumentservice.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ai_customer_support.companydocumentservice.annotation.ValidFile;
import com.ai_customer_support.companydocumentservice.dto.CompanyDocumentInfo;
import com.ai_customer_support.companydocumentservice.service.CompanyDocumentService;
import com.ai_customer_support.companydocumentservice.service.DocumentRetrievalService;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/company-document")
public class CompanyDocumentController {

    private final CompanyDocumentService documentService;
    private final DocumentRetrievalService documentRetrievalService;

    public CompanyDocumentController(CompanyDocumentService documentService, com.ai_customer_support.companydocumentservice.service.DocumentRetrievalService documentRetrievalService) {
        this.documentService = documentService;
        this.documentRetrievalService = documentRetrievalService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadDocument(@RequestPart("document") @ValidFile MultipartFile document,
            @RequestPart("documentInfo") @Valid CompanyDocumentInfo documentInfo) {
        if (documentService.uploadDocument(document, documentInfo)) {
            return ResponseEntity.ok("Document uploaded successfully.");
        } else {
            return ResponseEntity.status(500).body("An error occurred while uploading the document.");
        }
    }

    @GetMapping(value="/similar-context")
    public ResponseEntity<String> getSimilarContextForCompany(@RequestParam String companyId, 
                        @RequestParam String message) {
        return ResponseEntity.ok(documentRetrievalService.getSimilarContext(companyId, message));
    }
    
}
