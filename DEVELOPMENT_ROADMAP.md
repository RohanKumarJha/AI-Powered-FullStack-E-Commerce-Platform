# Development Roadmap

This document describes the planned implementation sequence for the
Enterprise AI-Powered E-Commerce Microservices Platform.

> **Status convention:** Mark a task complete only after implementation
> and verification. The roadmap is a plan, not a claim that every listed
> feature already exists.

## Development principles

-   Build one service at a time.
-   Complete core business logic before adding security and integrations
    to that service.
-   Give every service ownership of its data.
-   Register each service with Eureka as it is introduced.
-   Add inter-service communication only when a real business use case
    requires it.
-   Use local and Docker profiles with separate configuration and data.
-   Test with the actual database engine for important integration
    behavior.
-   Keep the API Gateway as the frontend entry point, while retaining
    security inside services.

------------------------------------------------------------------------

## Phase 0 --- Foundations

**Learn and prepare** - Java 21, OOP, collections, streams, exceptions,
concurrency - Spring Boot, Spring MVC, Spring Data JPA, Hibernate - SQL,
REST APIs, DTOs, validation - Git, GitHub, Maven, Linux, IntelliJ IDEA,
Postman - Docker fundamentals

**Deliverables:** foundational practice applications, Git repository,
development environment.

## Phase 1 --- System Design

**Design before implementation** - Functional and non-functional
requirements - Capacity estimation and quality attributes - HLD, LLD,
service boundaries, database-per-service - UML class and sequence
diagrams - ER diagrams and API contracts - OpenAPI / Swagger -
Architecture Decision Records (ADRs)

**Deliverables:** architecture diagrams, service responsibilities,
database designs, API contracts, design decisions.

## Phase 2 --- Eureka Discovery Server

**Build first** - Spring Cloud Netflix Eureka Server - Eureka dashboard
and configuration - Client registration and discovery - Heartbeats,
health status, instance lifecycle - Application names and service ports

**Sequence:** create and verify Eureka; register Identity next; register
each later service as it is built.

**Deliverables:** running Eureka Server and verified service
registration.

## Phase 3 --- Identity Service: Business Logic

**Implement without inter-service dependencies initially** - User
registration, profile updates, retrieval, and deletion - Roles: ADMIN,
CUSTOMER, SELLER - Role assignment and validation - Address management -
DTOs, mappers, factories, business validation - Global exception
handling, logging, OpenAPI - Unit and integration tests

**Initial database:** H2 for local relational development.

**Deliverables:** tested Identity business APIs and data model.

## Phase 4 --- Identity Service: Security

-   Spring Security and SecurityFilterChain
-   AuthenticationManager and UserDetailsService
-   BCrypt PasswordEncoder
-   Login authentication and JWT generation
-   JWT claims, signature and expiration validation
-   Access and refresh token strategy
-   Role-based and method-level authorization
-   Authentication and authorization error handling

**Important:** Identity issues tokens. Other services will validate JWTs
locally when their security is implemented; they should not call
Identity on every request.

**Deliverables:** secured Identity endpoints and tested authentication
flows.

## Phase 5 --- Docker and Database Environments

**Containerize Identity first** - Dockerfile and multi-stage builds -
Docker Compose, networks, volumes, health checks - Environment variables
and secret handling - Local and Docker Spring profiles - Identity MySQL
container and Flyway migrations - Persistent data and startup
dependencies

### Database strategy

  -----------------------------------------------------------------------
  Service                 Initial local           Docker / production
                          development             target
  ----------------------- ----------------------- -----------------------
  Identity                H2                      MySQL

  Catalog                 MongoDB                 MongoDB

  Inventory               H2                      PostgreSQL

  Cart                    H2 prototype, if useful Redis

  Order                   H2                      PostgreSQL

  Payment                 H2                      PostgreSQL

  Notification            H2                      PostgreSQL

  AI                      H2 for relational       PostgreSQL + Qdrant
                          prototype data, if      
                          needed                  
  -----------------------------------------------------------------------

**Testing guidance** - Catalog uses MongoDB in both development and
production, but with separate databases/instances and test data. - H2 is
convenient for early relational development, but it is not behaviorally
identical to MySQL or PostgreSQL. - Use Testcontainers with the actual
target database engine for important repository and integration tests. -
H2 is not a substitute for Redis. If Cart depends on Redis-specific
behavior, add Redis integration tests using a real Redis container. -
Use separate environment variables, credentials, databases, and volumes.
Never share development data with production.

**Deliverables:** Dockerized Identity and MySQL, repeatable local/Docker
configuration.

## Phase 6 --- Catalog Service

**Database:** MongoDB in both development and production.

**Business capabilities** - Products, categories, brands - Product
images and reviews - Product status and seller ownership - Search,
filtering, sorting, pagination

**Engineering** - Spring Data MongoDB and document modeling - DTOs,
mapping, validation, exception handling - Builder and appropriate
query/specification approach - Indexes and query optimization - JWT
validation and role-based access - Unit tests, MongoDB integration
tests, Testcontainers - Dockerfile, MongoDB container, persistent volume

**Communication:** identify real use cases with Identity or other
services. Do not add calls merely because another service exists.

**Deliverables:** secured, tested, containerized Catalog service.

## Phase 7 --- Remaining Business Microservices

Build each service independently using the same lifecycle: requirements
→ business logic → database → tests → security → Docker → Eureka
registration → communication assessment.

### Inventory Service

-   Stock levels, available/reserved/sold quantities
-   Reserve, release, validation, low-stock thresholds
-   Initial H2; PostgreSQL target

### Cart Service

-   Add/remove items, update quantities, totals
-   Cart persistence and validation
-   H2 only for an early relational prototype; Redis target
-   Validate Redis behavior with Redis integration tests

### Order Service

-   Place/cancel orders, history, status lifecycle
-   Order state transitions and validation
-   State pattern where appropriate
-   Initial H2; PostgreSQL target

### Payment Service

-   Mock payment processing, payment history, refunds
-   Payment status and validation
-   Strategy and Adapter patterns where appropriate
-   Initial H2; PostgreSQL target

### Notification Service

-   Email, SMS, push notifications
-   Templates, delivery status, notification history
-   Observer/event-driven design where appropriate
-   Initial H2; PostgreSQL target

### AI Service --- initial mock

-   Mock recommendations, search, assistant, product summaries
-   Define AI APIs and service boundaries
-   PostgreSQL + Qdrant target for later AI capabilities

**Deliverables:** independently tested and containerized business
services.

## Phase 8 --- Inter-Service Communication and API Gateway

### Synchronous communication

-   RestClient, WebClient, HTTP Interfaces
-   OpenFeign optional
-   Eureka-based service resolution and load balancing
-   Timeouts, error handling, secure service calls

### Asynchronous communication

-   Apache Kafka producers and consumers
-   Topics, partitions, consumer groups, offsets
-   Event serialization and message contracts
-   Retry topics and dead-letter queues
-   Event versioning and schema evolution
-   Idempotent consumers and duplicate handling

**Candidate events** - `order-created` - `payment-success` -
`payment-failed` - `inventory-updated` - `product-created` -
`product-updated` - `notification-created`

Choose event names and ownership based on the final domain model. Define
event producers, consumers, payloads, and failure behavior explicitly.

### API Gateway

-   Spring Cloud Gateway and Eureka-based routing
-   JWT validation at the edge
-   CORS, rate limiting, global filters
-   Correlation IDs and request logging
-   API versioning and consistent error responses

**Security boundary:** Gateway validation is an additional edge layer,
not a replacement for service-level authorization. Services must
validate credentials and permissions for their own protected operations.

**Deliverables:** documented service interactions, Kafka workflows,
Gateway routes, end-to-end communication tests.

## Phase 9 --- React Frontend

**Foundation** - React, TypeScript, Vite - Component architecture and
custom hooks - React Router and protected routes - Tailwind CSS or
Material UI - Responsive design and accessibility

**Integration** - Axios API client and interceptors - API Gateway
integration - Authentication state and token handling - Loading, empty,
and error states - Pagination, sorting, filtering, search

**State and forms** - Context API and Redux Toolkit or Zustand -
TanStack Query optional - React Hook Form and Zod

**Application modules** - Registration, login, profile, addresses - Home
page and product discovery - Product details, reviews, search - Cart and
checkout - Order history and tracking - Seller product management -
Admin dashboards - AI assistant and recommendations

**Quality and delivery** - Vitest and React Testing Library - Playwright
end-to-end tests - Error boundaries and performance optimization -
Production build, Nginx hosting, Docker

**Deliverables:** responsive React application integrated through the
Gateway.

## Phase 10 --- Distributed Transactions

-   Saga choreography and orchestration
-   Compensation actions
-   Transactional Outbox and Inbox
-   Idempotency and duplicate event protection
-   Failure recovery and workflow observability

**Example:** order request → inventory reservation → payment → order
confirmation → notification. Define compensation behavior for each
failure point.

## Phase 11 --- AI Integration

-   LangChain4j and OpenAI/Gemini APIs
-   Embeddings and vector search
-   Qdrant integration
-   Retrieval-Augmented Generation (RAG)
-   Product recommendations and semantic search
-   Shopping assistant and conversation context
-   Product/review summaries
-   Prompt and response validation
-   Token usage, rate limits, privacy, and failure handling

**Deliverables:** real AI features replacing the initial mock
implementations.

## Phase 12 --- Caching

-   Redis and Spring Cache
-   Cache-Aside, Write-Through, Write-Behind (where justified)
-   TTL, eviction and invalidation
-   Cache consistency and stampede prevention
-   Measure cache hit rate and latency

## Phase 13 --- Resilience Engineering

-   Resilience4j
-   Circuit breaker, retry, rate limiter
-   Time limiter and bulkhead
-   Timeouts and fallbacks
-   Graceful degradation and failure isolation

Avoid retrying non-idempotent operations unless they have safe
idempotency handling.

## Phase 14 --- Configuration Management

-   Spring Cloud Config Server and Client
-   Externalized configuration and environment profiles
-   Development, QA, staging, production
-   Secure secret storage using Vault or a cloud secret manager
-   Configuration refresh where appropriate

## Phase 15 --- Observability and Monitoring

-   Spring Boot Actuator and Micrometer
-   Structured logs with Logback
-   Correlation IDs and trace context
-   Prometheus and Grafana
-   OpenTelemetry and Zipkin
-   ELK: Elasticsearch, Logstash, Kibana
-   Alertmanager and operational dashboards

**Deliverables:** logs, metrics, traces, dashboards, and actionable
alerts.

## Phase 16 --- API, Contract, and End-to-End Testing

**Backend** - JUnit 5, Mockito, Spring Boot Test - MockMvc, REST
Assured, WireMock - Testcontainers for MySQL, PostgreSQL, MongoDB,
Redis, Kafka as needed

**Contracts and frontend** - Spring Cloud Contract or Pact - Vitest and
React Testing Library - Playwright end-to-end tests - Consumer-driven
contract tests - Complete business workflow tests

**Deliverables:** automated test suites and repeatable test execution in
CI.

## Phase 17 --- AWS Cloud Deployment

-   VPC, subnets, security groups, IAM
-   EC2 or ECS; EKS if Kubernetes is selected
-   RDS, ElastiCache, S3
-   Application Load Balancer, Route 53
-   CloudWatch and Secrets Manager
-   Terraform infrastructure as code

Choose the AWS runtime deliberately. ECS and EKS are alternative
container orchestration paths; do not deploy every option without a
need.

## Phase 18 --- CI/CD

**GitHub Actions pipeline** 1. Checkout 2. Backend and frontend builds
3. Unit tests 4. Integration and contract tests 5. Static analysis 6.
Dependency and image security scans 7. Docker image build and publish 8.
Deployment 9. Post-deployment health verification

Optional: Jenkins and Argo CD.

## Phase 19 --- Kubernetes

-   Pods, Deployments, ReplicaSets, StatefulSets
-   Services, Ingress, ConfigMaps, Secrets
-   Persistent Volumes and Claims
-   Requests, limits, and HPA
-   Liveness and readiness probes
-   Rolling updates and rollbacks
-   Namespaces and resource management

Deploy application services and only self-host stateful infrastructure
when operationally justified. Managed cloud databases may be preferable.

## Phase 20 --- Enterprise Security Upgrade

-   Keycloak
-   OAuth2 and OpenID Connect
-   SSO and identity providers
-   Google/GitHub login
-   Realm and client configuration
-   Service accounts and service-to-service identity
-   Fine-grained authorization
-   Token lifecycle and key rotation

## Phase 21 --- Performance Engineering

**Tools:** JMeter, k6, or Gatling.

**Measure** - Throughput and response time - P95 and P99 latency - Error
rate - CPU and memory - Database and cache performance

**Optimize** - SQL queries and indexes - Cache strategy - JVM, GC,
thread and connection pools - API and frontend bundle performance

**Deliverables:** baseline measurements, bottleneck analysis,
before/after results.

## Phase 22 --- Production Readiness

-   Health, readiness, and liveness checks
-   Graceful shutdown
-   API versioning and feature flags
-   Blue-green or canary deployment where justified
-   Rollback strategy
-   Backup and restore
-   Disaster recovery
-   Multi-environment configuration
-   Secret rotation
-   Operational runbooks and incident procedures

**Deliverables:** production checklist, recovery documentation,
deployment and operations guides.

------------------------------------------------------------------------

## Cross-cutting design patterns

Use patterns when they solve a real problem; avoid adding patterns only
to increase the technology list.

  Pattern           Potential use
  ----------------- ----------------------------------------------
  Factory           Controlled object creation
  Strategy          Payment methods and interchangeable policies
  Builder           Complex object construction
  Specification     Dynamic query composition where supported
  State             Order lifecycle
  Adapter           External provider integration
  Observer          Notification/event reactions
  Repository        Persistence abstraction
  Mapper            DTO/entity conversion
  Saga              Distributed business transaction
  Outbox / Inbox    Reliable event publication and consumption
  Cache-Aside       Read caching
  Circuit Breaker   Remote-call fault isolation
  CQRS              Optional read/write separation
  Event Sourcing    Optional event-based state history

## Completion criteria

A phase is complete when: - Its agreed features are implemented. -
Relevant unit and integration tests pass. - API and configuration
documentation is updated. - Security and failure behavior are verified
where applicable. - Docker and environment configuration work where
applicable. - The README and architecture decisions reflect the actual
implementation.
