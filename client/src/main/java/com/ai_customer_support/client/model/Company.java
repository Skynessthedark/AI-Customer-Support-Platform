package com.ai_customer_support.client.model;

import java.beans.Transient;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity 
@Table(name = "companies")
public class Company extends EntityItem {
    
    private String name;

    @Column (unique = true, nullable = false)
    private String authorizedPersonEmail;

    private String authorizedPersonName;
    private String authorizedPersonPhone;
    private int knowledgeVersion = 0;

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

}
