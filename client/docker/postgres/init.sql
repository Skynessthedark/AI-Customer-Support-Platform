CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS companies (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255),
    authorized_person_email VARCHAR(255) NOT NULL UNIQUE,
    authorized_person_name VARCHAR(255),
    authorized_person_phone VARCHAR(255),
    knowledge_version INTEGER DEFAULT 0,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

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

INSERT INTO companies (
    id,
    name,
    authorized_person_email,
    authorized_person_name,
    authorized_person_phone,
	knowledge_version,
	created_at,
	updated_at
)
VALUES
(
    '11111111-1111-1111-1111-111111111111',
    'Acme Technologies',
    'john.doe@acme.com',
    'John Doe',
    '+1-555-0100',
	0,
	CURRENT_TIMESTAMP,
	CURRENT_TIMESTAMP
),
(
    '22222222-2222-2222-2222-222222222222',
    'TechCorp Solutions',
    'jane.smith@techcorp.com',
    'Jane Smith',
    '+1-555-0200',
	0,
	CURRENT_TIMESTAMP,
	CURRENT_TIMESTAMP
),
(
    '22222223-2223-2223-2223-222222222223',
    'Goksen Technologies',
    'geralt.rivia@goksentech.com',
    'Geralt of Rivia',
    '+1-555-0300',
	0,
	CURRENT_TIMESTAMP,
	CURRENT_TIMESTAMP
);