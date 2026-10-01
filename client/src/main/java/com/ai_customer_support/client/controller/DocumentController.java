package com.ai_customer_support.client.controller;

import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ai_customer_support.client.dto.DocumentInfo;
import com.ai_customer_support.client.exception.InvalidDocumentException;
import com.ai_customer_support.client.service.DocumentService;


@RestController 
@RequestMapping("/api/document")
public class DocumentController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentController.class);

    private final DocumentService documentService;

    public DocumentController(com.ai_customer_support.client.service.DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping(value="/upload", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadDocument(@RequestPart("document") MultipartFile document,
                                                @RequestPart("documentInfo") DocumentInfo documentInfo){
        try {
             validateDocument(document, documentInfo);
             documentService.uploadDocument(document, documentInfo);
        }catch (InvalidDocumentException invalidDocEx) {
            LOGGER.error(invalidDocEx.getMessage());
            return ResponseEntity.badRequest().body(invalidDocEx.getMessage());
        }
         catch (Exception e) {
            LOGGER.error("Error occurred while uploading document: ", e);
            return ResponseEntity.status(500).body("An error occurred while uploading the document.");
        }
       
        return ResponseEntity.ok("Document uploaded successfully.");
    }

    private void validateDocument(MultipartFile document, DocumentInfo documentInfo) throws InvalidDocumentException {
        if(document == null || document.isEmpty()) {
            throw new InvalidDocumentException("Document content cannot be null or empty.");
        }
        if(documentInfo == null 
            || Strings.isBlank(documentInfo.companyName())
            || Strings.isBlank(documentInfo.documentTitle())) {
            throw new InvalidDocumentException("Document info cannot be null.");
        }
    }
    
}
