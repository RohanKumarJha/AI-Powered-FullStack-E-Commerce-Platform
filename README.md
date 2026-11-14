# 🚀 Enterprise AI-Powered E-Commerce Microservices Platform

**A full-stack, AI-enabled e-commerce platform built with Java 21,
Spring Boot, React, and a cloud-native microservices architecture.**

> **Project status:** In Progress\
> This repository describes the target architecture and development
> plan. A feature should be considered complete only after it has been
> implemented, tested, and documented.

**Java 21 · Spring Boot · React · TypeScript · Spring Cloud · MongoDB ·
MySQL · PostgreSQL · Redis · Kafka · Docker · AWS**

------------------------------------------------------------------------

## Overview

This project aims to model a real-world e-commerce ecosystem using
independently developed business services. Each service owns a specific
business capability and its data. Services are discovered through
Eureka, exposed through an API Gateway, and communicate synchronously or
asynchronously according to business needs.

A React application provides the customer, seller, and administrator
experiences. AI capabilities will support product discovery,
recommendations, summaries, and a shopping assistant.

### What the platform will provide

-   User registration, login, profiles, addresses, and role-based access
-   Product, category, brand, image, and review management
-   Inventory tracking and stock reservation
-   Shopping cart, checkout, orders, payments, and refunds
-   Email, SMS, and push notifications
-   AI-powered recommendations, semantic search, and shopping assistance
-   Service discovery, API routing, event-driven communication, and
    distributed workflows
-   Containerization, automated testing, observability, and cloud
    deployment

## Architecture at a glance

``` mermaid
flowchart TB
    U[Customers / Sellers / Admins] --> FE[React + TypeScript Frontend]
    FE --> GW[Spring Cloud API Gateway]
    GW --> ID[Identity Service]
    GW --> CAT[Catalog Service]
    GW --> INV[Inventory Service]
    GW --> CART[Cart Service]
    GW --> ORD[Order Service]
    GW --> PAY[Payment Service]
    GW --> NOT[Notification Service]
    GW --> AI[AI Service]

    E[Eureka Discovery Server] --- GW
    E --- ID
    E --- CAT
    E --- INV
    E --- CART
    E --- ORD
    E --- PAY
    E --- NOT
    E --- AI

    ORD <--> K[Apache Kafka]
    INV <--> K
    PAY <--> K
    NOT <--> K
    CAT <--> K

    AI --> Q[Qdrant]
```

See [ARCHITECTURE.md](ARCHITECTURE.md) for service responsibilities,
database ownership, security boundaries, communication patterns, and
workflow diagrams.

## Technology stack

  -----------------------------------------------------------------------
  Area                                Technologies
  ----------------------------------- -----------------------------------
  Backend                             Java 21, Spring Boot, Spring MVC,
                                      Spring Data JPA, Hibernate

  Frontend                            React, TypeScript, HTML5, CSS3,
                                      Vite

  UI and forms                        Tailwind CSS or Material UI, React
                                      Hook Form, Zod

  Routing and state                   React Router, Redux Toolkit or
                                      Zustand

  API integration                     REST, Axios, OpenAPI / Swagger

  Service infrastructure              Spring Cloud, Netflix Eureka,
                                      Spring Cloud Gateway

  Databases                           MySQL, MongoDB, PostgreSQL

  Cache / data stores                 Redis, Qdrant

  Messaging                           Apache Kafka

  Security                            Spring Security, JWT, BCrypt,
                                      OAuth2 / OIDC, Keycloak (planned)

  AI                                  LangChain4j, OpenAI or Gemini APIs,
                                      embeddings, RAG

  Testing                             JUnit 5, Mockito, Testcontainers,
                                      REST Assured, WireMock, Vitest,
                                      React Testing Library, Playwright

  Containers and orchestration        Docker, Docker Compose, Kubernetes

  Cloud and automation                AWS, Terraform, GitHub Actions

  Observability                       Actuator, Micrometer, Prometheus,
                                      Grafana, OpenTelemetry, Zipkin, ELK
  -----------------------------------------------------------------------

## Microservices

  -----------------------------------------------------------------------
  Service                 Main responsibility     Primary data store
  ----------------------- ----------------------- -----------------------
  Eureka Server           Service registration    ---
                          and discovery           

  Identity                Users, roles, profiles, MySQL
                          addresses,              
                          authentication          

  Catalog                 Products, categories,   MongoDB
                          brands, images,         
                          reviews, search         

  Inventory               Stock, reservations,    PostgreSQL
                          releases                

  Cart                    Shopping cart and cart  Redis
                          items                   

  Order                   Order lifecycle and     PostgreSQL
                          history                 

  Payment                 Payment records and     PostgreSQL
                          refunds                 

  Notification            Notification records    PostgreSQL
                          and delivery status     

  AI                      AI conversations,       PostgreSQL + Qdrant
                          metadata,               
                          embeddings/retrieval    
                          support                 

  API Gateway             Routing and edge-level  ---
                          policies                
  -----------------------------------------------------------------------

Each business service owns its data. Services should not directly read
or write another service's database.

## Key engineering capabilities

### Backend and distributed systems

-   Database-per-Service and independently deployable services
-   JWT authentication and service-level authorization
-   Eureka-based service discovery and Gateway routing
-   REST-based synchronous communication
-   Kafka-based asynchronous events
-   Saga, transactional outbox/inbox, and idempotent processing
-   Resilience patterns such as timeouts, retries, and circuit breakers

### Frontend

-   Responsive customer shopping experience
-   Login, registration, and protected routes
-   Product discovery, filtering, sorting, and pagination
-   Cart, checkout, and order tracking
-   Seller and administrator dashboards
-   AI shopping assistant and recommendations

### AI

-   Semantic product search using embeddings and vector retrieval
-   Context-aware shopping assistant using RAG
-   Product and review summaries
-   Personalized recommendations

### Delivery and operations

-   Dockerized services and databases
-   Automated unit, integration, contract, and end-to-end tests
-   Centralized logs, metrics, and distributed traces
-   CI/CD, AWS deployment, Kubernetes, and performance testing

## Development approach

The project is built incrementally:

1.  Design the system and create the Eureka Server.
2.  Implement Identity business logic, then add its security.
3.  Containerize Identity and connect it to MySQL.
4.  Build Catalog using MongoDB in development and production.
5.  Build the remaining business services independently.
6.  Add required service-to-service communication and the API Gateway.
7.  Build the React frontend against the Gateway APIs.
8.  Add distributed transactions, AI, caching, resilience,
    observability, and production deployment capabilities.

The complete phase-by-phase plan is in
[DEVELOPMENT_ROADMAP.md](DEVELOPMENT_ROADMAP.md).

## Repository structure

``` text
AI-Powered-E-Commerce-Application/
├── Eureka-Server/
├── Ecommerce-Backend/
│   ├── Identity-Microservice/
│   ├── Catalog-Microservice/
│   ├── Inventory-Microservice/
│   ├── Cart-Microservice/
│   ├── Order-Microservice/
│   ├── Payment-Microservice/
│   ├── Notification-Microservice/
│   ├── AI-Microservice/
│   └── API-Gateway/
├── Ecommerce-Frontend/
├── docker/
├── docs/
│   ├── architecture/
│   ├── diagrams/
│   ├── database/
│   └── api-documentation/
├── README.md
├── DEVELOPMENT_ROADMAP.md
└── ARCHITECTURE.md
```

The folder names are the intended organization; adjust them to match the
actual repository as it evolves.

## Documentation

-   [Development Roadmap](DEVELOPMENT_ROADMAP.md) --- implementation
    phases, technologies, tasks, and deliverables.
-   [Architecture](ARCHITECTURE.md) --- service boundaries, database
    allocation, security, communication, and workflows.

## Project goals

The goal is to demonstrate practical engineering across Java backend
development, React full-stack development, microservices, system design,
distributed systems, AI integration, databases, cloud infrastructure,
testing, and DevOps.

**The focus is not just on using many technologies. It is on building a
working application, making sound design decisions, testing its
behavior, documenting trade-offs, and being able to explain the
implementation.**
