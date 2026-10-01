package com.ai_customer_support.client.service;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ai_customer_support.client.dto.DocumentInfo;

@Service
public class DocumentService {

    private final VectorStore vectorStore;

    public DocumentService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public void uploadDocument(MultipartFile document, DocumentInfo documentInfo) {
        TikaDocumentReader reader = new TikaDocumentReader(document.getResource());
        List<Document> documents = reader.get();
        TextSplitter splitter = TokenTextSplitter.builder()
                .withChunkSize(200)
                .withMaxNumChunks(600)
                .build();
        vectorStore.add(splitter.split(documents));
    } 
}

