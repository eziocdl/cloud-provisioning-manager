# Cloud Provisioning Manager - C4 Architecture Diagrams

> Diagramas seguindo o modelo C4 (Context, Container, Component, Code) para documentar a arquitetura do sistema.

---

## Level 1: System Context Diagram

Visao de alto nivel mostrando o CPM e suas interacoes com sistemas externos.

```mermaid
C4Context
    title System Context Diagram - Cloud Provisioning Manager

    Person(dev, "Developer", "Desenvolvedor que precisa provisionar recursos na nuvem")
    Person(admin, "Platform Admin", "Administrador que gerencia politicas e cotas")

    System(cpm, "Cloud Provisioning Manager", "API de Governanca que controla o acesso a recursos de nuvem, aplicando politicas de quota por cargo")

    System_Ext(ldap, "Corporate LDAP", "Diretorio de usuarios com roles (TRAINEE, DEV, ADMIN)")
    System_Ext(openstack, "OpenStack Cloud", "Provedor de infraestrutura (VMs, Storage, Network)")
    System_Ext(zipkin, "Zipkin", "Distributed Tracing para observabilidade")

    Rel(dev, cpm, "Solicita VMs via REST API", "HTTPS + Basic Auth")
    Rel(admin, cpm, "Monitora e audita", "HTTPS")
    Rel(cpm, ldap, "Autentica e extrai roles", "LDAP")
    Rel(cpm, openstack, "Provisiona recursos", "HTTP/REST")
    Rel(cpm, zipkin, "Envia spans de tracing", "HTTP")
```

### Diagrama Alternativo (Compativel com GitHub)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              SYSTEM CONTEXT                                  │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│    ┌──────────────┐                                    ┌──────────────┐     │
│    │  Developer   │                                    │Platform Admin│     │
│    │              │                                    │              │     │
│    └──────┬───────┘                                    └──────┬───────┘     │
│           │ REST API                                          │             │
│           │ (HTTPS + Basic Auth)                    Monitoring│             │
│           ▼                                                   ▼             │
│    ┌─────────────────────────────────────────────────────────────────┐     │
│    │                                                                  │     │
│    │              CLOUD PROVISIONING MANAGER (CPM)                    │     │
│    │                                                                  │     │
│    │  "API de Governanca que controla acesso a recursos de nuvem,    │     │
│    │   aplicando politicas de quota baseadas em cargo corporativo"   │     │
│    │                                                                  │     │
│    └───────────┬─────────────────┬─────────────────┬─────────────────┘     │
│                │                 │                 │                        │
│                │ LDAP            │ HTTP/REST       │ HTTP                   │
│                ▼                 ▼                 ▼                        │
│    ┌───────────────┐   ┌─────────────────┐   ┌───────────────┐             │
│    │ Corporate     │   │   OpenStack     │   │    Zipkin     │             │
│    │ LDAP Server   │   │   Cloud         │   │   (Tracing)   │             │
│    │               │   │                 │   │               │             │
│    │ - Usuarios    │   │ - VMs           │   │ - Spans       │             │
│    │ - Roles       │   │ - Storage       │   │ - Traces      │             │
│    │ - Grupos      │   │ - Network       │   │ - Latency     │             │
│    └───────────────┘   └─────────────────┘   └───────────────┘             │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘

LEGENDA:
[Person]     = Usuario/Ator
[System]     = Sistema Principal (CPM)
[System_Ext] = Sistema Externo
```

---

## Level 2: Container Diagram

Zoom no sistema CPM mostrando os containers que o compoem.

```mermaid
C4Container
    title Container Diagram - Cloud Provisioning Manager

    Person(dev, "Developer", "Solicita provisionamento de VMs")

    System_Boundary(cpm, "Cloud Provisioning Manager") {
        Container(api, "REST API", "Spring Boot 4.0, Java 21", "Expoe endpoints REST, valida requests, aplica seguranca")
        Container(domain, "Domain Core", "Pure Java", "Regras de negocio, politicas de quota, state machine")
        ContainerDb(db, "PostgreSQL", "PostgreSQL 16", "Persiste solicitacoes de provisionamento e audit logs")
    }

    System_Ext(ldap, "Corporate LDAP", "Autenticacao e roles")
    System_Ext(openstack, "OpenStack", "Provedor de nuvem")
    System_Ext(zipkin, "Zipkin", "Tracing distribuido")

    Rel(dev, api, "POST /api/v1/provisioning", "HTTPS + Basic Auth")
    Rel(api, domain, "Invoca use cases")
    Rel(api, ldap, "Autentica usuario", "LDAP")
    Rel(domain, db, "Persiste requests", "JDBC")
    Rel(api, openstack, "Provisiona VMs (async)", "HTTP + Circuit Breaker")
    Rel(api, zipkin, "Reporta spans", "HTTP")
```

### Diagrama Alternativo (ASCII)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              CONTAINER DIAGRAM                               │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│                            ┌──────────────┐                                  │
│                            │  Developer   │                                  │
│                            └──────┬───────┘                                  │
│                                   │                                          │
│                                   │ POST /api/v1/provisioning                │
│                                   │ (HTTPS + Basic Auth)                     │
│                                   ▼                                          │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │           CLOUD PROVISIONING MANAGER [System Boundary]               │    │
│  │                                                                      │    │
│  │   ┌──────────────────────────────────────────────────────────────┐  │    │
│  │   │                      REST API                                 │  │    │
│  │   │                 [Spring Boot 4.0, Java 21]                   │  │    │
│  │   │                                                               │  │    │
│  │   │  • ProvisioningController    • SecurityConfig                │  │    │
│  │   │  • GlobalExceptionHandler    • OpenAPI/Swagger               │  │    │
│  │   │  • TracingFilter             • Circuit Breaker               │  │    │
│  │   └───────────────────────────┬──────────────────────────────────┘  │    │
│  │                               │                                      │    │
│  │                               │ Invoca Use Cases                     │    │
│  │                               ▼                                      │    │
│  │   ┌──────────────────────────────────────────────────────────────┐  │    │
│  │   │                    DOMAIN CORE                                │  │    │
│  │   │                    [Pure Java]                                │  │    │
│  │   │                                                               │  │    │
│  │   │  • ProvisioningRequest (Entity)   • PolicyEnforcementService │  │    │
│  │   │  • ResourceQuota (Value Object)   • State Machine            │  │    │
│  │   │  • CreateProvisioningUseCase      • Domain Events            │  │    │
│  │   └───────────────────────────┬──────────────────────────────────┘  │    │
│  │                               │                                      │    │
│  │                               │ Persiste via JPA                     │    │
│  │                               ▼                                      │    │
│  │   ┌──────────────────────────────────────────────────────────────┐  │    │
│  │   │                    PostgreSQL 16                              │  │    │
│  │   │            [provisioning_requests table]                      │  │    │
│  │   │                                                               │  │    │
│  │   │  id | requester_username | ram | cpu | status | created_at   │  │    │
│  │   └──────────────────────────────────────────────────────────────┘  │    │
│  │                                                                      │    │
│  └──────────────────────────────────────────────────────────────────────┘    │
│         │                    │                         │                     │
│         │ LDAP               │ HTTP (async)            │ HTTP                │
│         ▼                    ▼                         ▼                     │
│  ┌────────────┐      ┌────────────────┐        ┌────────────────┐           │
│  │  Corporate │      │   OpenStack    │        │     Zipkin     │           │
│  │    LDAP    │      │     Cloud      │        │    Tracing     │           │
│  │            │      │                │        │                │           │
│  │ [External] │      │   [External]   │        │   [External]   │           │
│  └────────────┘      └────────────────┘        └────────────────┘           │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Level 3: Component Diagram

Zoom no container da API mostrando os componentes internos organizados por Clean Architecture.

```mermaid
C4Component
    title Component Diagram - CPM API (Clean Architecture Layers)

    Container_Boundary(api, "API Layer") {
        Component(controller, "ProvisioningController", "REST Controller", "Recebe HTTP requests, valida DTOs")
        Component(dto, "DTOs", "Records", "CreateVmRequest, VmStatusResponse")
        Component(exHandler, "GlobalExceptionHandler", "ControllerAdvice", "Trata excecoes, retorna ProblemDetail")
    }

    Container_Boundary(app, "Application Layer") {
        Component(usecase, "CreateProvisioningUseCase", "Service", "Orquestra fluxo de criacao")
        Component(listener, "ProvisioningAsyncListener", "EventListener", "Processa eventos de forma assincrona")
        Component(ports, "Ports", "Interfaces", "CloudProviderPort, RepositoryPort, AuthPort")
    }

    Container_Boundary(domain, "Domain Layer") {
        Component(entity, "ProvisioningRequest", "Entity", "Agregado com state machine")
        Component(vo, "ResourceQuota", "Value Object", "Quotas imutaveis por role")
        Component(policy, "PolicyEnforcementService", "Domain Service", "Aplica regras de governanca")
        Component(event, "ProvisioningRequestedEvent", "Domain Event", "Evento de dominio")
    }

    Container_Boundary(infra, "Infrastructure Layer") {
        Component(repo, "JpaProvisionRepository", "Adapter", "Implementa RepositoryPort")
        Component(cloud, "OpenStackAdapter", "Adapter", "Implementa CloudProviderPort + Circuit Breaker")
        Component(ldap, "LdapAuthAdapter", "Adapter", "Implementa AuthPort")
        Component(security, "SecurityConfig", "Config", "Spring Security + LDAP")
        Component(tracing, "TracingConfig", "Config", "Brave/Zipkin integration")
    }

    Rel(controller, usecase, "Invoca")
    Rel(usecase, policy, "Valida politicas")
    Rel(usecase, entity, "Cria entidade")
    Rel(usecase, ports, "Usa ports")
    Rel(listener, cloud, "Provisiona async")
    Rel(repo, entity, "Persiste")
```

### Diagrama Alternativo (ASCII - Clean Architecture Layers)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    COMPONENT DIAGRAM (Clean Architecture)                    │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌────────────────────────────────────────────────────────────────────────┐ │
│  │                           API LAYER                                     │ │
│  │                      (Framework Dependent)                              │ │
│  │  ┌─────────────────────┐ ┌─────────────────┐ ┌─────────────────────┐   │ │
│  │  │ProvisioningController│ │   DTOs          │ │GlobalExceptionHandler│  │ │
│  │  │                      │ │                 │ │                      │  │ │
│  │  │ @RestController      │ │ CreateVmRequest │ │ @RestControllerAdvice│  │ │
│  │  │ @PostMapping         │ │ VmStatusResponse│ │ ProblemDetail (RFC)  │  │ │
│  │  │ @Operation (OpenAPI) │ │ @Schema         │ │                      │  │ │
│  │  └──────────┬───────────┘ └─────────────────┘ └──────────────────────┘  │ │
│  └─────────────┼───────────────────────────────────────────────────────────┘ │
│                │                                                              │
│                │ Invoca Port                                                  │
│                ▼                                                              │
│  ┌────────────────────────────────────────────────────────────────────────┐ │
│  │                        APPLICATION LAYER                                │ │
│  │                         (Use Cases)                                     │ │
│  │  ┌─────────────────────────────────────────────────────────────────┐   │ │
│  │  │                  CreateProvisioningUseCase                       │   │ │
│  │  │                                                                  │   │ │
│  │  │  1. Extract user role from SecurityContext                       │   │ │
│  │  │  2. Invoke PolicyEnforcementService.enforce()                    │   │ │
│  │  │  3. Create ProvisioningRequest entity                            │   │ │
│  │  │  4. Save via RepositoryPort                                      │   │ │
│  │  │  5. Publish ProvisioningRequestedEvent                           │   │ │
│  │  └─────────────────────────────────────────────────────────────────┘   │ │
│  │                                                                         │ │
│  │  ┌─────────────────────────────────────────────────────────────────┐   │ │
│  │  │                 ProvisioningAsyncListener                        │   │ │
│  │  │                                                                  │   │ │
│  │  │  @Async + @TransactionalEventListener(AFTER_COMMIT)              │   │ │
│  │  │  - Handles ProvisioningRequestedEvent                            │   │ │
│  │  │  - Calls CloudProviderPort.provisionInstance()                   │   │ │
│  │  │  - Updates request status (ACTIVE or FAILED)                     │   │ │
│  │  └─────────────────────────────────────────────────────────────────┘   │ │
│  │                                                                         │ │
│  │  ╔═══════════════════════════════════════════════════════════════════╗ │ │
│  │  ║                    PORTS (Interfaces)                              ║ │ │
│  │  ║  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────────┐  ║ │ │
│  │  ║  │CloudProviderPort│ │RepositoryPort   │ │AuthenticationPort   │  ║ │ │
│  │  ║  │                 │ │                 │ │                     │  ║ │ │
│  │  ║  │provisionInstance│ │save(request)    │ │isAuthenticated()    │  ║ │ │
│  │  ║  │                 │ │findById(id)     │ │                     │  ║ │ │
│  │  ║  └─────────────────┘ └─────────────────┘ └─────────────────────┘  ║ │ │
│  │  ╚═══════════════════════════════════════════════════════════════════╝ │ │
│  └────────────────────────────────────────────────────────────────────────┘ │
│                │                                                              │
│                │ Depende do Domain                                            │
│                ▼                                                              │
│  ┌────────────────────────────────────────────────────────────────────────┐ │
│  │                          DOMAIN LAYER                                   │ │
│  │                    (Pure Business Logic)                                │ │
│  │                                                                         │ │
│  │  ┌─────────────────────────────────────────────────────────────────┐   │ │
│  │  │              ProvisioningRequest [Aggregate Root]                │   │ │
│  │  │                                                                  │   │ │
│  │  │  State Machine:                                                  │   │ │
│  │  │  PENDING_APPROVAL → APPROVED → PROVISIONING → ACTIVE            │   │ │
│  │  │        │                                         ↓               │   │ │
│  │  │        └─→ REJECTED                           FAILED             │   │ │
│  │  │                                                                  │   │ │
│  │  │  Methods: approve(), reject(), markProvisioning(), complete()    │   │ │
│  │  └─────────────────────────────────────────────────────────────────┘   │ │
│  │                                                                         │ │
│  │  ┌─────────────────────┐  ┌──────────────────────────────────────────┐ │ │
│  │  │ ResourceQuota [VO]  │  │    PolicyEnforcementService              │ │ │
│  │  │                     │  │                                          │ │ │
│  │  │ TRAINEE: 8GB/4vCPU  │  │  enforce(role, ram, cpu)                 │ │ │
│  │  │ DEV: 32GB/8vCPU     │  │  - Validates against ResourceQuota       │ │ │
│  │  │ ADMIN: unlimited    │  │  - Throws PolicyViolationException       │ │ │
│  │  └─────────────────────┘  └──────────────────────────────────────────┘ │ │
│  └────────────────────────────────────────────────────────────────────────┘ │
│                ▲                                                              │
│                │ Implementa Ports                                             │
│                │                                                              │
│  ┌────────────────────────────────────────────────────────────────────────┐ │
│  │                       INFRASTRUCTURE LAYER                              │ │
│  │                         (Adapters)                                      │ │
│  │                                                                         │ │
│  │  ┌─────────────────────────────────────────────────────────────────┐   │ │
│  │  │                         ADAPTERS                                 │   │ │
│  │  │  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────────┐│   │ │
│  │  │  │JpaProvisionRepo │ │OpenStackAdapter │ │LdapAuthAdapter      ││   │ │
│  │  │  │                 │ │                 │ │                     ││   │ │
│  │  │  │implements       │ │implements       │ │implements           ││   │ │
│  │  │  │RepositoryPort   │ │CloudProviderPort│ │AuthenticationPort   ││   │ │
│  │  │  │                 │ │                 │ │                     ││   │ │
│  │  │  │Uses Spring Data │ │@CircuitBreaker  │ │BindAuthenticator    ││   │ │
│  │  │  │JPA              │ │WebClient        │ │                     ││   │ │
│  │  │  └─────────────────┘ └─────────────────┘ └─────────────────────┘│   │ │
│  │  └─────────────────────────────────────────────────────────────────┘   │ │
│  │                                                                         │ │
│  │  ┌─────────────────────────────────────────────────────────────────┐   │ │
│  │  │                      CONFIGURATIONS                              │   │ │
│  │  │  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌─────────┐ │   │ │
│  │  │  │SecurityConfig│ │ TracingConfig│ │  AsyncConfig │ │WebClient│ │   │ │
│  │  │  │              │ │              │ │              │ │ Config  │ │   │ │
│  │  │  │LDAP + Basic  │ │Brave/Zipkin  │ │ThreadPool +  │ │         │ │   │ │
│  │  │  │Auth          │ │B3 Propagation│ │Context Prop. │ │         │ │   │ │
│  │  │  └──────────────┘ └──────────────┘ └──────────────┘ └─────────┘ │   │ │
│  │  └─────────────────────────────────────────────────────────────────┘   │ │
│  └────────────────────────────────────────────────────────────────────────┘ │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘

DEPENDENCY RULE: Dependencies point INWARD (Infrastructure → Application → Domain)
```

---

## Level 4: Code Diagram (Sequence)

Fluxo detalhado de uma requisicao de provisionamento.

```mermaid
sequenceDiagram
    autonumber
    participant Dev as Developer
    participant API as ProvisioningController
    participant Sec as SecurityConfig
    participant LDAP as LDAP Server
    participant UC as CreateProvisioningUseCase
    participant Policy as PolicyEnforcementService
    participant Entity as ProvisioningRequest
    participant Repo as JpaRepository
    participant DB as PostgreSQL
    participant Event as EventPublisher
    participant Listener as AsyncListener
    participant Cloud as OpenStackAdapter
    participant OS as OpenStack

    Dev->>API: POST /api/v1/provisioning
    API->>Sec: Validate Basic Auth
    Sec->>LDAP: Bind + Extract Role
    LDAP-->>Sec: Role: DEV
    Sec-->>API: Authenticated

    API->>UC: create(username, ram, cpu)
    UC->>UC: extractUserRole() → DEV
    UC->>Policy: enforce(DEV, 16GB, 4vCPU)
    Policy->>Policy: Check ResourceQuota.DEV
    Policy-->>UC: OK (within quota)

    UC->>Entity: new ProvisioningRequest()
    Entity-->>UC: status=PENDING_APPROVAL
    UC->>Repo: save(request)
    Repo->>DB: INSERT INTO provisioning_requests
    DB-->>Repo: Saved

    UC->>Event: publish(ProvisioningRequestedEvent)
    UC-->>API: Return ProvisioningRequest
    API-->>Dev: 201 Created {id, status, trackingId}

    Note over Listener: Async (After Commit)
    Event->>Listener: handle(event)
    Listener->>Repo: findById(id)
    Listener->>Cloud: provisionInstance(request)

    alt Success
        Cloud->>OS: POST /servers
        OS-->>Cloud: 200 OK
        Cloud-->>Listener: instance-id
        Listener->>Entity: complete()
        Listener->>Repo: save(request)
    else Circuit Open/Timeout
        Cloud-->>Listener: QUEUE-WAITING-ALLOCATION (fallback)
        Listener->>Entity: fail()
        Listener->>Repo: save(request)
    end
```

### Diagrama Alternativo (ASCII Sequence)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         REQUEST FLOW SEQUENCE                                │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  Developer          API           LDAP        UseCase      Policy     DB    │
│      │               │              │            │           │         │    │
│      │───────────────────────────────────────────────────────────────────   │
│      │  POST /api/v1/provisioning                                           │
│      │  Authorization: Basic ZGV2dXNlcjpzZW5oYWRldjEyMw==                   │
│      │───────────────────────────────────────────────────────────────────   │
│      │               │              │            │           │         │    │
│      │               │──────────────>            │           │         │    │
│      │               │ LDAP Bind    │            │           │         │    │
│      │               │<─────────────│            │           │         │    │
│      │               │ Role: DEV    │            │           │         │    │
│      │               │              │            │           │         │    │
│      │               │──────────────────────────>│           │         │    │
│      │               │ create(devuser, 16GB, 4vCPU)          │         │    │
│      │               │              │            │           │         │    │
│      │               │              │            │──────────>│         │    │
│      │               │              │            │ enforce() │         │    │
│      │               │              │            │<──────────│         │    │
│      │               │              │            │ OK        │         │    │
│      │               │              │            │           │         │    │
│      │               │              │            │─────────────────────>    │
│      │               │              │            │         INSERT           │
│      │               │              │            │<─────────────────────    │
│      │               │              │            │         Saved            │
│      │               │              │            │           │         │    │
│      │               │<─────────────────────────│           │         │    │
│      │               │ ProvisioningRequest      │           │         │    │
│      │<──────────────│              │            │           │         │    │
│      │ 201 Created   │              │            │           │         │    │
│      │ {id, status}  │              │            │           │         │    │
│      │               │              │            │           │         │    │
│                                                                              │
│  ═══════════════════════════════════════════════════════════════════════    │
│  ASYNC (After Transaction Commit)                                            │
│  ═══════════════════════════════════════════════════════════════════════    │
│                                                                              │
│  AsyncListener      Repo         OpenStackAdapter    OpenStack              │
│      │               │                  │               │                   │
│      │──────────────>│                  │               │                   │
│      │ findById(id)  │                  │               │                   │
│      │<──────────────│                  │               │                   │
│      │               │                  │               │                   │
│      │──────────────────────────────────>               │                   │
│      │      provisionInstance(request)  │               │                   │
│      │               │                  │               │                   │
│      │               │                  │──────────────>│                   │
│      │               │                  │ POST /servers │                   │
│      │               │                  │<──────────────│                   │
│      │               │                  │  200 OK       │                   │
│      │               │                  │               │                   │
│      │<─────────────────────────────────│               │                   │
│      │  instance-id  │                  │               │                   │
│      │               │                  │               │                   │
│      │──────────────>│                  │               │                   │
│      │  save(request.complete())        │               │                   │
│      │               │                  │               │                   │
│                                                                              │
│  ═══════════════════════════════════════════════════════════════════════    │
│  FALLBACK SCENARIO (Circuit Breaker Open or Timeout)                        │
│  ═══════════════════════════════════════════════════════════════════════    │
│                                                                              │
│      │──────────────────────────────────>               │                   │
│      │      provisionInstance(request)  │               │                   │
│      │               │                  │──────X (timeout/error)            │
│      │               │                  │               │                   │
│      │<─────────────────────────────────│               │                   │
│      │  "QUEUE-WAITING-ALLOCATION"      │               │                   │
│      │  (fallback response)             │               │                   │
│      │               │                  │               │                   │
│      │──────────────>│                  │               │                   │
│      │  save(request.fail())            │               │                   │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Deployment Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          KUBERNETES DEPLOYMENT                               │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │                        Kubernetes Cluster                            │    │
│  │                                                                      │    │
│  │   ┌────────────────────────────────────────────────────────────┐    │    │
│  │   │                    CPM Namespace                            │    │    │
│  │   │                                                             │    │    │
│  │   │  ┌─────────────────┐      ┌─────────────────┐              │    │    │
│  │   │  │   CPM Pod #1    │      │   CPM Pod #2    │  ← Replicas  │    │    │
│  │   │  │                 │      │                 │              │    │    │
│  │   │  │ ┌─────────────┐ │      │ ┌─────────────┐ │              │    │    │
│  │   │  │ │  Container  │ │      │ │  Container  │ │              │    │    │
│  │   │  │ │  Java 21    │ │      │ │  Java 21    │ │              │    │    │
│  │   │  │ │  Spring 4.0 │ │      │ │  Spring 4.0 │ │              │    │    │
│  │   │  │ └─────────────┘ │      │ └─────────────┘ │              │    │    │
│  │   │  │                 │      │                 │              │    │    │
│  │   │  │ Resources:      │      │ Resources:      │              │    │    │
│  │   │  │  256Mi-512Mi    │      │  256Mi-512Mi    │              │    │    │
│  │   │  │  250m-500m CPU  │      │  250m-500m CPU  │              │    │    │
│  │   │  │                 │      │                 │              │    │    │
│  │   │  │ Probes:         │      │ Probes:         │              │    │    │
│  │   │  │  /health/live   │      │  /health/live   │              │    │    │
│  │   │  │  /health/ready  │      │  /health/ready  │              │    │    │
│  │   │  └────────┬────────┘      └────────┬────────┘              │    │    │
│  │   │           │                        │                        │    │    │
│  │   │           └──────────┬─────────────┘                        │    │    │
│  │   │                      │                                      │    │    │
│  │   │           ┌──────────▼──────────┐                          │    │    │
│  │   │           │   CPM Service       │                          │    │    │
│  │   │           │   (LoadBalancer)    │                          │    │    │
│  │   │           │   Port: 80 → 8080   │                          │    │    │
│  │   │           └──────────┬──────────┘                          │    │    │
│  │   │                      │                                      │    │    │
│  │   │   ┌──────────────────┴───────────────────┐                 │    │    │
│  │   │   │                                      │                 │    │    │
│  │   │   ▼                                      ▼                 │    │    │
│  │   │ ┌─────────────────┐           ┌─────────────────┐          │    │    │
│  │   │ │   ConfigMap     │           │     Secret      │          │    │    │
│  │   │ │   (cpm-config)  │           │   (cpm-secrets) │          │    │    │
│  │   │ │                 │           │                 │          │    │    │
│  │   │ │ - DB_URL        │           │ - DB_PASSWORD   │          │    │    │
│  │   │ │ - LDAP_URL      │           │ - LDAP_PASSWORD │          │    │    │
│  │   │ │ - ZIPKIN_URL    │           │                 │          │    │    │
│  │   │ └─────────────────┘           └─────────────────┘          │    │    │
│  │   │                                                             │    │    │
│  │   └─────────────────────────────────────────────────────────────┘    │    │
│  │                                                                      │    │
│  └──────────────────────────────────────────────────────────────────────┘    │
│                    │                    │                    │               │
│                    ▼                    ▼                    ▼               │
│  ┌──────────────────────┐  ┌─────────────────┐  ┌──────────────────────┐    │
│  │    PostgreSQL        │  │    OpenLDAP     │  │      Zipkin          │    │
│  │    (Managed/PVC)     │  │   (StatefulSet) │  │    (Deployment)      │    │
│  └──────────────────────┘  └─────────────────┘  └──────────────────────┘    │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Technology Stack Summary

| Layer | Technology | Purpose |
|-------|------------|---------|
| **Runtime** | Java 21 LTS | Latest LTS with virtual threads support |
| **Framework** | Spring Boot 4.0 | Enterprise-grade framework |
| **API** | Spring WebMVC + OpenAPI 3.0 | REST API with auto-documentation |
| **Security** | Spring Security + LDAP | Corporate authentication |
| **Persistence** | Spring Data JPA + PostgreSQL | Reliable data storage |
| **Migration** | Flyway | Database versioning |
| **Resilience** | Resilience4j | Circuit Breaker, Fallback |
| **Observability** | Micrometer + Brave + Zipkin | Distributed Tracing |
| **Testing** | JUnit 5 + Testcontainers | Unit & Integration tests |
| **Container** | Docker (Multi-stage) | Optimized images |
| **Orchestration** | Kubernetes | Production deployment |

---

## References

- [C4 Model](https://c4model.com/)
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)
