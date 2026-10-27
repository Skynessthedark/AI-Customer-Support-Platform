package com.ai_customer_support.companydocumentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.resilience.annotation.EnableResilientMethods;

@SpringBootApplication
@EnableResilientMethods
public class CompanyDocumentServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CompanyDocumentServiceApplication.class, args);
	}

}
