package com.ai_customer_support.company.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import jakarta.validation.constraints.NotBlank;

public record CompanyDto(
    @NotBlank(message = "CompanyId is required") String id,
    @NotBlank(message = "Company name is required") String name,
    @NotBlank(message = "Authorized person email is required") String authorizedPersonEmail,
    String authorizedPersonName,
    String authorizedPersonPhone,
    Integer knowledgeVersion,
    String createdAt,
    String updatedAt) {

    public CompanyDto(String id,
         String name, 
         String authorizedPersonEmail, 
         String authorizedPersonName, 
         String authorizedPersonPhone,
         Integer knowledgeVersion, 
         LocalDateTime createdAt, 
         LocalDateTime updatedAt){
            
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this(id, name, authorizedPersonEmail, authorizedPersonName, authorizedPersonPhone, knowledgeVersion, 
            createdAt.format(formatter), updatedAt.format(formatter));
    }

}
