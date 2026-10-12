package com.ai_customer_support.companydocumentservice.service;

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

import com.ai_customer_support.companydocumentservice.config.property.DocumentLoaderProperties;
import com.ai_customer_support.companydocumentservice.dto.CompanyDocumentInfo;
import com.ai_customer_support.companydocumentservice.dto.CompanyDto;
import com.ai_customer_support.companydocumentservice.exception.InvalidDocumentInfoException;
import com.ai_customer_support.companydocumentservice.model.CompanyDocument;
import com.ai_customer_support.companydocumentservice.repository.CompanyDocumentRepository;

@Service
public class CompanyDocumentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompanyDocumentService.class);

    private static final String COMPANY_ID = "companyId";
    private static final String DOCUMENT_TITLE = "documentTitle";
    private static final String COMPANY_AUTHORIZED_PERSON_EMAIL = "companyAuthorizedPersonEmail";
    private static final String COMPANY_DOCUMENT_ID = "companyDocumentId";

    private final VectorStore vectorStore;
    private final DocumentLoaderProperties documentLoaderProperties;
    private final RestService restService;
    private final CompanyDocumentRepository companyDocumentRepository;

    public CompanyDocumentService(VectorStore vectorStore,
            DocumentLoaderProperties documentLoaderProperties,
            CompanyDocumentRepository companyDocumentRepository,
            RestService restService) {
        this.vectorStore = vectorStore;
        this.documentLoaderProperties = documentLoaderProperties;
        this.companyDocumentRepository = companyDocumentRepository;
        this.restService = restService;
    }

    public boolean uploadDocument(MultipartFile document, CompanyDocumentInfo documentInfo) {
        try {
            List<Document> documents = prepareDocuments(document, documentInfo);

            TextSplitter splitter = getTextSplitter();
            vectorStore.add(splitter.split(documents));

            return true;
        } catch (InvalidDocumentInfoException e) {
            LOGGER.error("Invalid document info: ", e);
        } catch (Exception e) {
            LOGGER.error("Error occurred while uploading document: ", e);
        }
        return false;
    }

    private List<Document> prepareDocuments(MultipartFile document, CompanyDocumentInfo documentInfo)
            throws InvalidDocumentInfoException {

        CompanyDto company = restService.getCompany(documentInfo.companyId());

        validateDocumentInfo(company, documentInfo);

        CompanyDocument savedDocument = saveAndReturnCompanyDocument(company, documentInfo);

        TikaDocumentReader reader = new TikaDocumentReader(document.getResource());
        List<Document> documents = reader.get();
        setMetaDataForDocument(documents, documentInfo, savedDocument.getId());
        return documents;
    }

    private CompanyDocument saveAndReturnCompanyDocument(CompanyDto company, CompanyDocumentInfo documentInfo) {
        CompanyDocument companyDocument = new CompanyDocument();
        companyDocument.setCompanyId(company.id());
        companyDocument.setDocumentTitle(documentInfo.documentTitle());

        restService.increaseAndUpdateKnowledgeVersion(company.id());
        return companyDocumentRepository.save(companyDocument);
    }

    private void validateDocumentInfo(CompanyDto company, CompanyDocumentInfo documentInfo)
            throws InvalidDocumentInfoException {
        if (company == null || company.id() == null) {
            throw new InvalidDocumentInfoException("Company with ID " + documentInfo.companyId() + " does not exist.");
        }

        if (!company.authorizedPersonEmail().equals(documentInfo.companyAuthorizedPersonEmail())) {
            throw new InvalidDocumentInfoException(
                    "The provided email does not match the authorized person for the specified company.");
        }
    }

    private TextSplitter getTextSplitter() {
        return TokenTextSplitter.builder()
                .withChunkSize(documentLoaderProperties.getChunkSize())
                .withMinChunkSizeChars(documentLoaderProperties.getMinChunkSizeChars())
                .withMaxNumChunks(documentLoaderProperties.getChunkMaxNum())
                .build();
    }

    private void setMetaDataForDocument(List<Document> documents, CompanyDocumentInfo documentInfo,
            String companyDocumentId) {
        for (Document doc : documents) {
            doc.getMetadata().put(COMPANY_ID, documentInfo.companyId());
            doc.getMetadata().put(COMPANY_DOCUMENT_ID, companyDocumentId);
            doc.getMetadata().put(DOCUMENT_TITLE, documentInfo.documentTitle());
            doc.getMetadata().put(COMPANY_AUTHORIZED_PERSON_EMAIL, documentInfo.companyAuthorizedPersonEmail());
        }
    }
}
