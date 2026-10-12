CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS company_documents (
    id VARCHAR(255) PRIMARY KEY,
    document_title VARCHAR(255),
    company_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,

    CONSTRAINT fk_company_documents_company
        FOREIGN KEY (company_id)
        REFERENCES companies(id)
);
