package com.ai_customer_support.company.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ai_customer_support.company.dto.CompanyDto;
import com.ai_customer_support.company.service.CompanyService;


@RestController
@RequestMapping("/api/company")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompanyDto> get(@PathVariable(name = "id") String companyId) {
        return ResponseEntity.ok(companyService.getCompany(companyId));
    }

    @GetMapping("/by-authorized-person/{email}")
    public ResponseEntity<CompanyDto> getByAuthorizerEmail(@PathVariable String email) {
        return ResponseEntity.ok(companyService.getCompanyByAuthorizedPersonEmail(email));
    }

    @PatchMapping("/{id}/increase-knowledge-version")
    public ResponseEntity<CompanyDto> increaseKnowledgeVersion(@PathVariable(name="id") String companyId) {
        return ResponseEntity.ok(companyService.increaseAndUpdateKnowledgeVersion(companyId));
    }
}
