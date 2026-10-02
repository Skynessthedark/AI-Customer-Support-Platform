package com.ai_customer_support.client.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ai_customer_support.client.annotation.ValidFile;
import com.ai_customer_support.client.dto.CompanyDocumentInfo;
import com.ai_customer_support.client.service.CompanyDocumentService;

import jakarta.validation.Valid;

@RestController 
@RequestMapping("/api/company/document")
public class CompanyDocumentController {

    private final CompanyDocumentService documentService;

    public CompanyDocumentController(CompanyDocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping(value="/upload", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadDocument(@RequestPart("document") @ValidFile MultipartFile document,
                                                @RequestPart("documentInfo") @Valid CompanyDocumentInfo documentInfo){
        if(documentService.uploadDocument(document, documentInfo)){
            return ResponseEntity.ok("Document uploaded successfully.");
        } else {
            return ResponseEntity.status(500).body("An error occurred while uploading the document.");
        }
    }
}
