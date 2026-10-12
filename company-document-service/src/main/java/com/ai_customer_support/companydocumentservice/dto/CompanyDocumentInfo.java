package com.ai_customer_support.companydocumentservice.dto;

import jakarta.validation.constraints.NotBlank;

public record CompanyDocumentInfo(
                @NotBlank(message = "CompanyId is required") String companyId,
                @NotBlank(message = "Document title is required") String documentTitle,
                @NotBlank(message = "Company authorized person email is required") String companyAuthorizedPersonEmail) {
}
