package com.ai_customer_support.client.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "document-loader")
public class DocumentLoaderProperties {

    private List<String> fileAllowedContentTypes;
    private int chunkSize;
    private int minChunkSizeChars;
    private int chunkMaxNum;

    public List<String> getFileAllowedContentTypes() {
        return fileAllowedContentTypes;
    }

    public void setFileAllowedContentTypes(List<String> fileAllowedContentTypes) {
        this.fileAllowedContentTypes = fileAllowedContentTypes;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    public int getChunkMaxNum() {
        return chunkMaxNum;
    }

    public void setChunkMaxNum(int chunkMaxNum) {
        this.chunkMaxNum = chunkMaxNum;
    }

    public int getMinChunkSizeChars() {
        return minChunkSizeChars;
    }

    public void setMinChunkSizeChars(int minChunkSizeChars) {
        this.minChunkSizeChars = minChunkSizeChars;
    }
}
