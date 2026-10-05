package com.ai_customer_support.client.validation;

import org.springframework.web.multipart.MultipartFile;

import com.ai_customer_support.client.annotation.ValidFile;
import com.ai_customer_support.client.config.property.DocumentLoaderProperties;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class FileValidator implements ConstraintValidator<ValidFile, MultipartFile> {

    private final DocumentLoaderProperties documentLoaderProperties;

    public FileValidator(DocumentLoaderProperties documentLoaderProperties) {
        this.documentLoaderProperties = documentLoaderProperties;
    }
    
    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        if (file == null || file.isEmpty()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("File cannot be empty.")
                   .addConstraintViolation();
            return false;
        }

        String contentType = file.getContentType();
        if (contentType == null || !documentLoaderProperties.getFileAllowedContentTypes().contains(contentType)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Only PNG, JPEG, or PDF files are allowed.")
                   .addConstraintViolation();
            return false;
        }

        return true;
    }

}
