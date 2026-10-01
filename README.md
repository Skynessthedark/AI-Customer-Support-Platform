# AI Customer Support Platform

An AI-powered customer support platform that enables companies to provide AI-assisted support to their customers.

The platform is designed to process company-specific information and provide relevant answers to customer questions through an AI assistant.

## Current Status

### Spring AI Integration ✅

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

### API

#### Chat

```http
POST /api/chat
Content-Type: application/json
```

**Request**

```json
{
  "message": "What is Spring AI?"
}
```

**Response**

```json
{
  "response": "Spring AI is a framework that provides abstractions for integrating AI models and AI capabilities into Spring applications."
}
```

The endpoint receives a user message and sends it to the configured LLM through Spring AI's `ChatClient`.

## Technology Stack

* Java 25
* Spring Boot
* Spring AI
* LLM

## Roadmap

* [x] Phase 1 — Spring Boot + Spring AI
* [ ] Phase 2 — RAG + PGVector
* [ ] Phase 3 — Redis
* [ ] Phase 4 — Microservices
* [ ] Phase 5 — Kafka
* [ ] Phase 6 — Security + Multi-Tenancy
* [ ] Phase 7 — Observability + AI Evaluation
* [ ] Phase 8 — Docker + Deployment

## Planned Architecture

The platform will evolve toward a document-aware AI customer support system.

```text
Company Documents
       ↓
Document Processing
       ↓
Embeddings
       ↓
Vector Database
       ↓
     RAG
       ↓
Customer Question
       ↓
AI Assistant
       ↓
Customer Answer
```

Additional platform capabilities will include event-driven processing, distributed services, security, multi-tenancy, observability, and AI evaluation.

## Project Structure

The application follows a layered architecture for the current phase:

```text
Controller
    ↓
Service
    ↓
Spring AI
    ↓
LLM
```