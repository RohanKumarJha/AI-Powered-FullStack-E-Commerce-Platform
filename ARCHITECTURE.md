# Architecture --- Enterprise AI-Powered E-Commerce Platform

This document describes the intended architecture, service boundaries,
database ownership, security model, communication patterns, and major
business workflows.

> **Architecture status:** Target design. Update this document when
> implementation decisions change. Do not treat planned components as
> deployed components.

## 1. Architectural goals

-   Independently developable and deployable business capabilities
-   Clear service ownership and database boundaries
-   Secure APIs and least-privilege access
-   Synchronous communication for immediate responses
-   Asynchronous events for decoupled workflows
-   Resilient order, inventory, and payment processing
-   A React frontend using a single public API entry point
-   Observable, testable, containerized deployment

## 2. System context

``` mermaid
flowchart TB
    CUSTOMER[Customer]
    SELLER[Seller]
    ADMIN[Administrator]

    subgraph Client
        WEB[React + TypeScript Web App]
    end

    subgraph Edge
        GW[Spring Cloud API Gateway]
    end

    subgraph Platform
        EUREKA[Eureka Discovery Server]
        ID[Identity]
        CAT[Catalog]
        INV[Inventory]
        CART[Cart]
        ORD[Order]
        PAY[Payment]
        NOT[Notification]
        AI[AI]
        KAFKA[Apache Kafka]
    end

    CUSTOMER --> WEB
    SELLER --> WEB
    ADMIN --> WEB
    WEB --> GW

    GW --> ID
    GW --> CAT
    GW --> INV
    GW --> CART
    GW --> ORD
    GW --> PAY
    GW --> NOT
    GW --> AI

    EUREKA -. registration/discovery .- GW
    EUREKA -. registration/discovery .- ID
    EUREKA -. registration/discovery .- CAT
    EUREKA -. registration/discovery .- INV
    EUREKA -. registration/discovery .- CART
    EUREKA -. registration/discovery .- ORD
    EUREKA -. registration/discovery .- PAY
    EUREKA -. registration/discovery .- NOT
    EUREKA -. registration/discovery .- AI

    ORD <--> KAFKA
    INV <--> KAFKA
    PAY <--> KAFKA
    NOT <--> KAFKA
    CAT <--> KAFKA
```

The Gateway is the intended frontend entry point. Internal service calls
and event consumers remain service-to-service interactions and must be
secured separately.

## 3. Service boundaries and responsibilities

  -----------------------------------------------------------------------
  Component               Responsibilities        Owns
  ----------------------- ----------------------- -----------------------
  Eureka Server           Service registry and    Registry state
                          discovery               

  Identity                Users, roles, profiles, Identity data
                          addresses, login, token 
                          issuance                

  Catalog                 Products, categories,   Catalog data
                          brands, images,         
                          reviews, search         

  Inventory               Stock levels,           Inventory data
                          reservations, releases  

  Cart                    Cart items, quantities, Cart data
                          cart state              

  Order                   Order creation,         Order data
                          lifecycle, history      

  Payment                 Payment attempts,       Payment data
                          status, refunds         

  Notification            Notification requests,  Notification data
                          templates, delivery     
                          status                  

  AI                      AI requests, assistant  AI metadata
                          context, retrieval      
                          orchestration           

  API Gateway             Routing, edge filters,  No business database
                          request policies        
  -----------------------------------------------------------------------

### Service ownership rules

-   A service is the sole owner of its database.
-   Other services do not query or update its tables/collections
    directly.
-   Cross-service data is obtained through APIs or events.
-   Avoid synchronous chains that create unnecessary latency or tight
    coupling.
-   Use events for facts that other services need to react to.
-   Define ownership for every event and API contract.

## 4. Database architecture

### Target database allocation

  -----------------------------------------------------------------------
  Service                 Target database         Reason / use
  ----------------------- ----------------------- -----------------------
  Identity                MySQL                   Relational users,
                                                  roles, addresses

  Catalog                 MongoDB                 Product documents and
                                                  flexible catalog
                                                  attributes

  Inventory               PostgreSQL              Transactional stock and
                                                  reservation data

  Cart                    Redis                   Fast, short-lived cart
                                                  state

  Order                   PostgreSQL              Relational order and
                                                  lifecycle records

  Payment                 PostgreSQL              Payment and refund
                                                  records

  Notification            PostgreSQL              Notification records
                                                  and delivery status

  AI                      PostgreSQL + Qdrant     Relational metadata
                                                  plus vector retrieval
  -----------------------------------------------------------------------

### Development and testing strategy

  ------------------------------------------------------------------------
  Service                 Early local development Target
                                                  integration/production
                                                  testing
  ----------------------- ----------------------- ------------------------
  Identity                H2                      MySQL via Testcontainers

  Catalog                 MongoDB                 MongoDB via
                                                  Testcontainers

  Inventory               H2                      PostgreSQL via
                                                  Testcontainers

  Cart                    H2 prototype if useful  Redis container

  Order                   H2                      PostgreSQL via
                                                  Testcontainers

  Payment                 H2                      PostgreSQL via
                                                  Testcontainers

  Notification            H2                      PostgreSQL via
                                                  Testcontainers

  AI                      H2 for optional         PostgreSQL and Qdrant
                          relational prototype    containers as needed
  ------------------------------------------------------------------------

**Important distinctions** - Development and production must use
separate databases, credentials, volumes, and data. - Catalog can use
MongoDB in both development and production while still using separate
MongoDB databases/instances. - H2 does not reproduce every MySQL or
PostgreSQL behavior. Validate migrations, SQL, locking, indexes, and
transactions against the actual engine. - H2 is not a substitute for
Redis. Redis-specific expiration, atomic operations, and data structures
require Redis integration tests. - Use Flyway for relational schema
migrations. Use versioned scripts or an equivalent controlled process
for MongoDB indexes and initialization. - Do not share a database schema
between business services.

## 5. Service discovery and API routing

### Eureka

Eureka is created first. Each Spring Boot service becomes a Eureka
client and registers using a stable application name.

-   Local profile: Eureka URL points to the locally running server.
-   Docker profile: Eureka URL uses the Compose service name on the
    Docker network.
-   Health and registration behavior must be verified after container
    restarts.

### API Gateway

The Gateway is introduced after the core services and their API
requirements are understood.

Responsibilities: - Route public requests to services - Resolve service
instances through Eureka - Apply edge-level JWT validation - Configure
CORS and rate limits - Add correlation IDs and request logging -
Standardize edge error handling and API version routing

The Gateway is not a replacement for authorization inside each
microservice.

## 6. Security architecture

### Identity responsibilities

Identity is responsible for: - User registration and account
management - Password hashing using BCrypt - Login authentication - Role
and permission administration - JWT issuance and refresh-token policy

### Request security flow

``` mermaid
sequenceDiagram
    actor User
    participant UI as React Frontend
    participant GW as API Gateway
    participant ID as Identity Service
    participant SVC as Target Microservice

    User->>UI: Submit login credentials
    UI->>GW: Login request
    GW->>ID: Forward login request
    ID->>ID: Authenticate user and verify password
    ID-->>UI: Return access token (and refresh mechanism)
    User->>UI: Request protected resource
    UI->>GW: API request with access token
    GW->>GW: Validate token at edge
    GW->>SVC: Forward authenticated request
    SVC->>SVC: Validate token and enforce permissions
    SVC-->>UI: Protected response
```

### Security rules

-   Passwords are never stored in plaintext.
-   Tokens must be signed, time-limited, and validated for signature,
    issuer, audience where configured, and expiration.
-   Services validate JWTs locally using trusted signing-key
    configuration; they do not call Identity for every request.
-   Authorization is enforced at the service that owns the protected
    operation.
-   The Gateway provides an additional edge security layer.
-   Service-to-service calls and Kafka access require their own trust
    and authorization controls.
-   Secrets and signing keys must not be committed to Git.
-   Use TLS for production network traffic.
-   Refresh-token storage, rotation, revocation, and browser storage
    strategy must be explicitly designed.

Keycloak and OAuth2/OIDC are planned as a later identity-management
upgrade, not assumed to exist in the initial JWT implementation.

## 7. Communication architecture

Choose the communication type based on the business requirement.

### Synchronous communication

Use REST when the caller needs an immediate result, for example: -
Gateway to a requested business service - A service checking a required
piece of information from another service - A workflow step that cannot
proceed without a direct response

Potential clients: - RestClient - WebClient - HTTP Interfaces -
OpenFeign (optional)

Apply timeouts, controlled retries, error mapping, and circuit breakers.
Avoid long chains of synchronous calls.

### Asynchronous communication

Use Kafka when a service publishes a business event and other services
can react independently.

Candidate events: - `order-created` - `payment-success` -
`payment-failed` - `inventory-updated` - `product-created` -
`product-updated` - `notification-created`

For every event, document: - Owning producer - Consumers - Event schema
and version - Event key and ordering requirements - Retry and
dead-letter behavior - Idempotency strategy - Retention and replay
expectations

### Example event flow

``` mermaid
flowchart LR
    O[Order Service] -->|OrderCreated| K[(Kafka)]
    K --> I[Inventory Consumer]
    K --> P[Payment Consumer]
    P -->|PaymentSucceeded / PaymentFailed| K
    K --> N[Notification Consumer]
    K --> O2[Order Status Consumer]
```

This is an illustrative flow. The final event sequence and ownership
must be defined with the implemented business rules.

## 8. Order workflow and distributed consistency

An order crosses multiple service-owned data stores. A single relational
database transaction cannot atomically commit all of them.

Use a Saga workflow with explicit states and compensating actions.

``` mermaid
flowchart TD
    A[Customer places order] --> B[Create pending order]
    B --> C[Reserve inventory]
    C --> D{Stock reserved?}
    D -- No --> X[Reject or cancel order]
    D -- Yes --> E[Process payment]
    E --> F{Payment successful?}
    F -- Yes --> G[Confirm order]
    G --> H[Publish order confirmed event]
    H --> I[Send notification]
    F -- No --> J[Release reserved inventory]
    J --> K[Mark order failed or cancelled]
```

### Reliability mechanisms

-   Saga orchestration or choreography, selected per workflow
-   Transactional Outbox to publish events reliably with local database
    changes
-   Inbox or processed-message tracking to handle duplicates
-   Idempotency keys for payment and order operations
-   Retry with bounded attempts and backoff
-   Dead-letter handling and operational replay procedures
-   Explicit compensation for each completed step

A payment provider call must not be blindly repeated after an uncertain
timeout. Use provider idempotency keys and reconcile the payment status.

## 9. Frontend architecture

The React + TypeScript application is developed after the backend
services, security, communication, and Gateway are established.

### Main UI areas

-   Public pages: home, product search, product details
-   Authentication: registration, login, profile
-   Shopping: cart, checkout, order confirmation
-   Customer: order history, addresses, account settings
-   Seller: product and inventory management
-   Administrator: catalog and operational dashboards
-   AI: shopping assistant, semantic search, recommendations

### Frontend engineering

-   React, TypeScript, Vite
-   React Router
-   Axios API client and interceptors
-   Redux Toolkit or Zustand for client state
-   TanStack Query where server-state caching is useful
-   React Hook Form and Zod
-   Tailwind CSS or Material UI
-   Vitest, React Testing Library, Playwright

The frontend should call the Gateway rather than independently exposing
internal service endpoints to the browser.

## 10. AI architecture

The AI Service will evolve from mock endpoints to real AI-backed
functionality.

### Planned capabilities

-   Semantic product search
-   Product recommendations
-   Product and review summaries
-   Context-aware shopping assistant
-   RAG over catalog/product information

### Planned technology flow

``` mermaid
flowchart LR
    UI[React UI] --> GW[API Gateway]
    GW --> AI[AI Service]
    AI --> LLM[OpenAI / Gemini]
    AI --> EMB[Embedding Model]
    EMB --> Q[(Qdrant)]
    AI --> DB[(PostgreSQL Metadata)]
    AI --> CAT[Catalog API]
```

The AI service should retrieve authorized, relevant product data and
pass only necessary context to the model. Validate model output, manage
token costs, apply rate limits, and avoid exposing private customer data
in prompts or logs.

## 11. Deployment and runtime architecture

### Development progression

1.  Run services locally with local profiles.
2.  Run selected databases in Docker.
3.  Containerize each service and its required data store.
4.  Use Docker Compose for local multi-container integration.
5.  Build and test the React application.
6.  Add CI/CD and deploy to AWS.
7.  Introduce Kubernetes when orchestration and scaling requirements
    justify it.

### Planned infrastructure

-   Docker and Docker Compose
-   AWS VPC, IAM, ALB, EC2 or ECS/EKS
-   RDS, ElastiCache, S3
-   CloudWatch and Secrets Manager
-   Terraform
-   Kubernetes manifests, probes, resource limits, HPA
-   GitHub Actions CI/CD

ECS and EKS are alternative deployment choices. Select one based on
operational requirements rather than deploying both by default. Prefer
managed databases and managed messaging where they reduce operational
burden.

## 12. Observability

Every service should expose and propagate enough telemetry to understand
a request across boundaries.

-   Health endpoints through Spring Boot Actuator
-   Metrics through Micrometer and Prometheus
-   Dashboards through Grafana
-   Structured logs through SLF4J and Logback
-   Correlation IDs and distributed trace context
-   OpenTelemetry and Zipkin
-   ELK stack for centralized log search
-   Alerts for service health, error rates, latency, and consumer lag

Do not log passwords, access tokens, payment secrets, or sensitive
personal data.

## 13. Testing strategy

  -----------------------------------------------------------------------
  Test level              Purpose                 Tools
  ----------------------- ----------------------- -----------------------
  Unit                    Isolated business logic JUnit 5, Mockito,
                                                  Vitest

  Service integration     Service plus real       Spring Boot Test,
                          dependencies            Testcontainers

  API                     HTTP contract and       REST Assured, Postman,
                          behavior                WireMock

  Contract                Producer/consumer       Spring Cloud Contract
                          compatibility           or Pact

  Frontend component      UI behavior             React Testing Library

  End-to-end              Complete user workflows Playwright

  Performance             Load and latency        k6, JMeter, or Gatling

  Security                Authentication,         Automated scans and
                          authorization,          targeted security tests
                          dependency risks        
  -----------------------------------------------------------------------

Test against the actual database and infrastructure behavior for
critical paths. H2-only tests are not sufficient evidence that MySQL or
PostgreSQL integration is correct.

## 14. Architecture decisions to record

Maintain ADRs for decisions such as: - Why each service owns its
database - Why Catalog uses MongoDB - Which relational services use H2
during early development - Why Cart uses Redis in the target design -
Which calls are synchronous and which are events - Saga orchestration
versus choreography - JWT key management and token storage - Kafka event
schemas and versioning - ECS versus EKS - Managed versus self-hosted
data infrastructure

## 15. Non-goals and safeguards

-   Do not claim production readiness before production-like testing.
-   Do not add microservices solely to increase service count.
-   Do not share database tables across services.
-   Do not use the Gateway as the only security boundary.
-   Do not assume exactly-once business effects from Kafka alone; design
    idempotent processing.
-   Do not retry payment operations without idempotency and
    reconciliation.
-   Do not commit credentials, signing keys, or production data.
-   Do not list planned features as completed in the public README.
