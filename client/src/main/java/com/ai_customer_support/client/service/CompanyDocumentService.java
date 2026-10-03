package com.ai_customer_support.client.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ai_customer_support.client.config.DocumentLoaderProperties;
import com.ai_customer_support.client.dto.CompanyDocumentInfo;
import com.ai_customer_support.client.exception.InvalidDocumentInfoException;
import com.ai_customer_support.client.model.Company;
import com.ai_customer_support.client.model.CompanyDocument;
import com.ai_customer_support.client.repository.CompanyDocumentRepository;

@Service
public class CompanyDocumentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompanyDocumentService.class);

    private static final String COMPANY_ID = "companyId";
    private static final String DOCUMENT_TITLE = "documentTitle";
    private static final String COMPANY_AUTHORIZED_PERSON_EMAIL = "companyAuthorizedPersonEmail";

    private final VectorStore vectorStore;
    private final DocumentLoaderProperties documentLoaderProperties;
    private final CompanyService companyService;
    private final CompanyDocumentRepository companyDocumentRepository;

    public CompanyDocumentService(VectorStore vectorStore, DocumentLoaderProperties documentLoaderProperties, com.ai_customer_support.client.repository.CompanyDocumentRepository companyDocumentRepository, com.ai_customer_support.client.service.CompanyService companyService) {
        this.vectorStore = vectorStore;
        this.documentLoaderProperties = documentLoaderProperties;
        this.companyDocumentRepository = companyDocumentRepository;
        this.companyService = companyService;
    }

    public boolean uploadDocument(MultipartFile document, CompanyDocumentInfo documentInfo) {
        try{
            Company company = companyService.getCompany(documentInfo.companyId());
            List<Document> documents = prepareDocuments(company,document, documentInfo);

            TextSplitter splitter = getTextSplitter();
            vectorStore.add(splitter.split(documents));

            saveCompanyDocument(company, documentInfo);
            
            return true;
        }catch(InvalidDocumentInfoException e){
            LOGGER.error("Invalid document info: ", e);
        }catch(Exception e){
            LOGGER.error("Error occurred while uploading document: ", e);
        }
        return false;
    } 

    private List<Document> prepareDocuments(Company company,MultipartFile document, CompanyDocumentInfo documentInfo) throws InvalidDocumentInfoException {
        validateDocumentInfo(company, documentInfo);

        TikaDocumentReader reader = new TikaDocumentReader(document.getResource());
        List<Document> documents = reader.get();
        setMetaDataForDocument(documents, documentInfo);
        return documents;
    }

    private void saveCompanyDocument(Company company, CompanyDocumentInfo documentInfo) {
        CompanyDocument companyDocument = new CompanyDocument();
        companyDocument.setCompany(company);
        companyDocument.setDocumentTitle(documentInfo.documentTitle());
        companyDocumentRepository.save(companyDocument);
    }

    private void validateDocumentInfo(Company company, CompanyDocumentInfo documentInfo) throws InvalidDocumentInfoException {
        if (company == null) {
            throw new InvalidDocumentInfoException("Company with ID " + documentInfo.companyId() + " does not exist.");
        }

        if(!company.getAuthorizedPersonEmail().equals(documentInfo.companyAuthorizedPersonEmail())) {
            throw new InvalidDocumentInfoException("The provided email does not match the authorized person for the specified company.");
        }
    }

    private TextSplitter getTextSplitter() {
        return TokenTextSplitter.builder()
                .withChunkSize(documentLoaderProperties.getChunkSize())
                .withChunkOverlap(documentLoaderProperties.getChunkOverlap())
                .withMaxNumChunks(documentLoaderProperties.getChunkMaxNum())
                .build();
    }

    private void setMetaDataForDocument(List<Document> documents, CompanyDocumentInfo documentInfo) {
        for (Document doc : documents) {
            doc.getMetadata().put(COMPANY_ID, documentInfo.companyId());
            doc.getMetadata().put(DOCUMENT_TITLE, documentInfo.documentTitle());
            doc.getMetadata().put(COMPANY_AUTHORIZED_PERSON_EMAIL, documentInfo.companyAuthorizedPersonEmail());
        }
    }
}

