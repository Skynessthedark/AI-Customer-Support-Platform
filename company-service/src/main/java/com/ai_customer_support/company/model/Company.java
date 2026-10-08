package com.ai_customer_support.company.model;

import java.beans.Transient;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity 
@Table(name = "companies")
public class Company{
    
    @Id 
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;

    @Column (unique = true, nullable = false)
    private String authorizedPersonEmail;

    private String authorizedPersonName;
    private String authorizedPersonPhone;
    private int knowledgeVersion = 0;

    @CreationTimestamp 
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthorizedPersonName() {
        return authorizedPersonName;
    }

    public void setAuthorizedPersonName(String authorizedPersonName) {
        this.authorizedPersonName = authorizedPersonName;
    }

    public String getAuthorizedPersonPhone() {
        return authorizedPersonPhone;
    }

    public void setAuthorizedPersonPhone(String authorizedPersonPhone) {
        this.authorizedPersonPhone = authorizedPersonPhone;
    }

    public String getAuthorizedPersonEmail() {
        return authorizedPersonEmail;
    }

    public void setAuthorizedPersonEmail(String authorizedPersonEmail) {
        this.authorizedPersonEmail = authorizedPersonEmail;
    }

    public void increaseKnowledgeVersion(){
        this.knowledgeVersion++;
    }

    public int getKnowledgeVersion() {
        return knowledgeVersion;
    }

    @Transient 
    public void setKnowledgeVersion(int knowledgeVersion) {
        this.knowledgeVersion = knowledgeVersion;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
