# AI Customer Support Platform

An AI-powered customer support platform that enables companies to provide AI-assisted support to their customers.

The platform processes company-specific documents and uses them as a knowledge source for generating relevant answers to customer questions.

## Current Status

### Phase 1 — Spring AI Integration ✅

The first phase implements the core AI chat functionality using Spring Boot and Spring AI.

```text
Client
  ↓
ChatController
  ↓
ChatService
  ↓
Spring AI ChatClient
  ↓
LLM
  ↓
AI Response
```

Implemented:

* Spring Boot application
* Spring AI integration
* `ChatClient` based LLM communication
* Chat API
* System prompt configuration
* User messages
* Request/response DTOs
* Exception handling
* Application configuration

### Phase 2 — RAG + PGVector ✅

The second phase introduces Retrieval-Augmented Generation (RAG), allowing the AI assistant to use company-specific documents when answering customer questions.

```text
Document
   ↓
Text Extraction
   ↓
Text Splitting
   ↓
Embeddings
   ↓
PGVector
   ↓
Similarity Search
   ↓
Context + Question
   ↓
LLM
   ↓
AI Response
```

Implemented:

* Company document management
* Document upload
* Document metadata
* Text extraction and splitting
* Embedding generation
* PGVector integration
* Vector similarity search
* Retrieval-Augmented Generation
* Company-specific document retrieval

### Phase 3 — Redis ✅

Redis is integrated to improve response performance and maintain conversation context.

```text
                    ┌──────────────┐
Customer Question → │ Redis Cache? │
                    └──────┬───────┘
                       Hit  │  Miss
                        ↓   ↓
                     Answer  LLM
                              ↓
                         Redis Cache
```

Implemented:

* Semantic response caching
* Cache hit / miss handling
* Cache TTL
* Cache invalidation
* Conversation memory
* User-specific conversation context
* Redis integration
* Redis integration testing with Testcontainers

## API

### Chat

```text
GET /api/chat
Headers:
  companyId: <company-id>
  username: <username>

Query:
  message=<question>
```

### Document Upload

```text
POST /api/company/document/upload
Content-Type: multipart/form-data

Parts:
  document: <file>
  documentInfo: <metadata>
```

## Technology Stack

* Java 25
* Spring Boot
* Spring AI
* PostgreSQL
* PGVector
* Redis
* LLM
* Embedding Model
* Testcontainers

## Roadmap

* [x] Phase 1 — Spring Boot + Spring AI
* [x] Phase 2 — RAG + PGVector
* [x] Phase 3 — Redis
* [ ] Phase 4 — Microservices
* [ ] Phase 5 — Kafka
* [ ] Phase 6 — Security + Multi-Tenancy
* [ ] Phase 7 — Observability + AI Evaluation
* [ ] Phase 8 — Docker + Deployment

## Architecture

```text
                         Company Document
                                ↓
                       Document Processing
                                ↓
                         Text Splitting
                                ↓
                            Embeddings
                                ↓
                             PGVector
                                ↑
                                │
                         Similarity Search
                                │
Customer Question ──────────────┘
           ↓
      Relevant Context
           ↓
     Spring AI ChatClient
           ↓
           LLM
           ↓
      AI Response
           ↓
      Redis Cache
```

Conversation memory is also managed through Redis to maintain context across chat interactions.

## Project Structure

```text
Controller
    ↓
Service
    ↓
Repository / Spring AI
    ↓
PostgreSQL / PGVector
    ↓
Redis
```

The platform will evolve with additional capabilities including microservices, Kafka, security, multi-tenancy, observability, AI evaluation, and deployment infrastructure.
