# Cloud Provisioning Manager (CPM)

![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0-green?style=flat-square&logo=springboot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?style=flat-square&logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-Ready-blue?style=flat-square&logo=docker)
![Kubernetes](https://img.shields.io/badge/Kubernetes-Ready-blue?style=flat-square&logo=kubernetes)
![License](https://img.shields.io/badge/License-MIT-green?style=flat-square)

[![CI](https://github.com/eziocdl/cloud-provisioning-manager/actions/workflows/ci.yml/badge.svg)](https://github.com/eziocdl/cloud-provisioning-manager/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/eziocdl/cloud-provisioning-manager/branch/main/graph/badge.svg)](https://codecov.io/gh/eziocdl/cloud-provisioning-manager)

> API de Governanca e Orquestracao para provisionamento de infraestrutura em nuvem.

---

## Highlights

| Feature | Descricao |
|---------|-----------|
| **Clean Architecture** | Separacao clara de responsabilidades com Ports & Adapters |
| **Domain-Driven Design** | Aggregates, Value Objects, Domain Events, Domain Services |
| **Circuit Breaker** | Resiliencia com fallback automatico via Resilience4j |
| **Distributed Tracing** | Observabilidade end-to-end com Zipkin |
| **LDAP Auth** | Autenticacao corporativa com roles (TRAINEE, DEV, ADMIN) |
| **Kubernetes Ready** | Deployment, Service, ConfigMap, Secrets |
| **Architecture Tests** | Validacao automatica com ArchUnit |
| **Code Coverage** | Relatorios JaCoCo integrados ao CI |

---

## O Problema

Grandes empresas sofrem com **Shadow IT** e descontrole financeiro na nuvem. Desenvolvedores criam VMs sem padronizacao, sem etiquetas de custo e sem aprovacao, gerando faturas milionarias e brechas de seguranca.

## A Solucao

O CPM atua como um **Gateway Inteligente** entre o desenvolvedor e a infraestrutura (OpenStack):

1. **Blindagem de Seguranca**: Acesso via LDAP corporativo
2. **Governanca Ativa**: Regras de negocio por cargo (ex: "Trainee so cria VM pequena")
3. **Resiliencia**: Circuit Breaker para tolerancia a falhas
4. **Observabilidade**: Distributed Tracing com Zipkin

---

## Arquitetura C4

### Context Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                         CLOUD PROVISIONING MANAGER                   │
│                                                                      │
│    ┌──────────┐         ┌─────────────────┐         ┌──────────┐   │
│    │          │         │                 │         │          │   │
│    │ Developer├────────►│   CPM API       ├────────►│ OpenStack│   │
│    │          │  REST   │   (Gateway)     │  HTTP   │  Cloud   │   │
│    └──────────┘         └────────┬────────┘         └──────────┘   │
│                                  │                                  │
│                         ┌────────▼────────┐                        │
│                         │                 │                        │
│                         │   PostgreSQL    │                        │
│                         │   (Audit Log)   │                        │
│                         │                 │                        │
│                         └─────────────────┘                        │
│                                                                     │
│    ┌──────────┐         ┌─────────────────┐                        │
│    │          │         │                 │                        │
│    │  LDAP    │◄────────│   Security      │                        │
│    │ Server   │  Auth   │   Layer         │                        │
│    │          │         │                 │                        │
│    └──────────┘         └─────────────────┘                        │
└─────────────────────────────────────────────────────────────────────┘
```

### Container Diagram

```
┌──────────────────────────────────────────────────────────────────────────┐
│                              CPM API Container                            │
│                                                                           │
│  ┌─────────────┐    ┌─────────────────┐    ┌────────────────────────┐   │
│  │             │    │                 │    │                        │   │
│  │ REST API    │───►│ Policy          │───►│ Use Cases              │   │
│  │ Controller  │    │ Enforcement     │    │ (Application Layer)    │   │
│  │             │    │                 │    │                        │   │
│  └─────────────┘    └─────────────────┘    └───────────┬────────────┘   │
│                                                         │                │
│                     ┌───────────────────────────────────┼────────────┐   │
│                     │                                   │            │   │
│              ┌──────▼──────┐    ┌──────────────┐   ┌───▼────────┐   │   │
│              │             │    │              │   │            │   │   │
│              │ Domain      │    │ JPA          │   │ OpenStack  │   │   │
│              │ Model       │    │ Repository   │   │ Adapter    │   │   │
│              │             │    │              │   │            │   │   │
│              └─────────────┘    └──────┬───────┘   └─────┬──────┘   │   │
│                                        │                 │          │   │
│                                        │    Circuit      │          │   │
│                                        │    Breaker      │          │   │
└────────────────────────────────────────┼─────────────────┼──────────────┘
                                         │                 │
                                    ┌────▼────┐       ┌────▼────┐
                                    │PostgreSQL│       │OpenStack│
                                    └─────────┘       └─────────┘
```

### Component Diagram (Clean Architecture)

```
┌────────────────────────────────────────────────────────────────────────────┐
│                                API Layer                                    │
│  ┌──────────────────────┐  ┌───────────────┐  ┌─────────────────────────┐ │
│  │ProvisioningController│  │CreateVmRequest│  │VmStatusResponse        │ │
│  │      (REST)          │  │    (DTO)      │  │     (DTO)              │ │
│  └──────────┬───────────┘  └───────────────┘  └─────────────────────────┘ │
└─────────────┼───────────────────────────────────────────────────────────────┘
              │
              ▼
┌────────────────────────────────────────────────────────────────────────────┐
│                            Application Layer                                │
│  ┌────────────────────────┐  ┌─────────────────────────────────────────┐  │
│  │CreateProvisioningUseCase│  │ProvisioningAsyncListener               │  │
│  │    (Orchestrator)      │  │   (@Async @TransactionalEventListener) │  │
│  └────────────┬───────────┘  └─────────────────────────────────────────┘  │
│               │                                                            │
│  ┌────────────▼───────────────────────────────────────────────────────┐   │
│  │                         Ports (Interfaces)                          │   │
│  │  ┌─────────────────┐  ┌──────────────────┐  ┌───────────────────┐  │   │
│  │  │RepositoryPort   │  │ CloudProviderPort│  │ AuthenticationPort│  │   │
│  │  └─────────────────┘  └──────────────────┘  └───────────────────┘  │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
└────────────────────────────────────────────────────────────────────────────┘
              │
              ▼
┌────────────────────────────────────────────────────────────────────────────┐
│                              Domain Layer                                   │
│  ┌─────────────────────────┐  ┌──────────────────┐  ┌──────────────────┐  │
│  │   ProvisioningRequest   │  │ProvisioningStatus│  │ ResourceQuota    │  │
│  │      (Aggregate)        │  │     (Enum)       │  │ (Value Object)   │  │
│  └─────────────────────────┘  └──────────────────┘  └──────────────────┘  │
│                                                                            │
│  ┌─────────────────────────┐  ┌──────────────────────────────────────┐    │
│  │PolicyEnforcementService │  │PolicyViolationException              │    │
│  │    (Domain Service)     │  │   (Domain Exception)                 │    │
│  └─────────────────────────┘  └──────────────────────────────────────┘    │
└────────────────────────────────────────────────────────────────────────────┘
              │
              ▼
┌────────────────────────────────────────────────────────────────────────────┐
│                           Infrastructure Layer                              │
│  ┌───────────────────┐  ┌─────────────────────┐  ┌─────────────────────┐  │
│  │JpaProvisionRepo   │  │OpenStackAdapter     │  │LdapAuthAdapter      │  │
│  │  (Persistence)    │  │  (Cloud Provider)   │  │  (Security)         │  │
│  │                   │  │  + Resilience4j     │  │                     │  │
│  └───────────────────┘  └─────────────────────┘  └─────────────────────┘  │
│                                                                            │
│  ┌───────────────────┐  ┌─────────────────────┐  ┌─────────────────────┐  │
│  │SecurityConfig     │  │TracingConfig        │  │AsyncConfig          │  │
│  └───────────────────┘  └─────────────────────┘  └─────────────────────┘  │
└────────────────────────────────────────────────────────────────────────────┘
```

---

## Stack Tecnologica

| Categoria | Tecnologia |
|-----------|------------|
| Runtime | Java 21 + Spring Boot 4.0 |
| Persistencia | PostgreSQL + Spring Data JPA + Flyway |
| Seguranca | Spring Security + LDAP |
| Resiliencia | Resilience4j (Circuit Breaker) |
| Observabilidade | Micrometer + Zipkin (Distributed Tracing) |
| Containerizacao | Docker + Kubernetes |
| Documentacao | OpenAPI 3.0 (Swagger) |
| Testes | JUnit 5 + Testcontainers |

---

## Como Executar

### Pre-requisitos

- Java 21+
- Docker & Docker Compose
- Maven 3.9+

### 1. Subir infraestrutura

```bash
docker-compose up -d
```

Isso inicia:
- PostgreSQL (porta 5432)
- OpenLDAP (porta 389)
- Zipkin (porta 9411)
- WireMock - Mock do OpenStack (porta 8081)

### 2. Executar a aplicacao

```bash
./mvnw spring-boot:run
```

### 3. Acessar

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Zipkin**: http://localhost:9411

---

## Endpoints da API

### POST /api/v1/provisioning

Cria uma solicitacao de provisionamento de VM.

**Request:**
```json
{
  "username": "devuser",
  "ram": "16GB",
  "cpu": "4vCPU"
}
```

**Response (201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "status": "PENDING_APPROVAL",
  "trackingId": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Response (403 Forbidden - Policy Violation):**
```json
{
  "type": "https://cpm.org/errors/policy-violation",
  "title": "Policy Violation",
  "status": 403,
  "detail": "Policy violation: User with role 'TRAINEE' cannot request RAM=64GB. Maximum allowed: 8GB",
  "userRole": "TRAINEE",
  "violatedResource": "RAM",
  "requestedValue": "64GB",
  "maxAllowed": "8GB"
}
```

---

## Politicas de Governanca

| Cargo | RAM Maximo | CPU Maximo |
|-------|------------|------------|
| TRAINEE | 8GB | 4 vCPU |
| DEV | 32GB | 8 vCPU |
| ADMIN | Ilimitado | Ilimitado |

---

## Usuarios de Teste (LDAP)

| Usuario | Senha | Cargo |
|---------|-------|-------|
| admin | senhaadmin123 | ADMIN |
| devuser | senhadev123 | DEV |
| trainee | senhatrainee123 | TRAINEE |

---

## Testes

```bash
# Todos os testes
./mvnw test

# Apenas testes unitarios
./mvnw test -Dtest=*Test

# Testes de integracao (requer Docker)
./mvnw test -Dtest=*IntegrationTest
```

---

## Deploy Kubernetes

```bash
# Aplicar manifestos
kubectl apply -f k8s/

# Verificar pods
kubectl get pods -l app=cpm
```

---

## Estrutura do Projeto

```
src/
├── main/java/com/eziocdl/
│   ├── api/                    # Controllers, DTOs, Exception Handlers
│   ├── application/            # Use Cases, Ports, Event Listeners
│   ├── domain/                 # Entities, Value Objects, Domain Services
│   └── infrastructure/         # Adapters, Configs, External Integrations
└── test/java/com/eziocdl/
    ├── architecture/           # ArchUnit Tests (Clean Architecture validation)
    ├── domain/                 # Unit Tests
    └── integration/            # Integration Tests (Testcontainers)
```

---

## Documentacao

| Documento | Descricao |
|-----------|-----------|
| [C4 Diagrams](docs/architecture/C4-DIAGRAMS.md) | Diagramas de arquitetura (Context, Container, Component) |
| [Code Review](docs/CODE-REVIEW.md) | Analise tecnica e pontos de melhoria |
| [ADR-0001](docs/adr/0001-use-clean-architecture.md) | Por que Clean Architecture? |
| [ADR-0002](docs/adr/0002-use-ldap-authentication.md) | Por que LDAP? |
| [ADR-0003](docs/adr/0003-use-circuit-breaker.md) | Por que Circuit Breaker? |
| [ADR-0004](docs/adr/0004-use-event-driven-async.md) | Por que processamento assincrono? |
| [ADR-0005](docs/adr/0005-use-distributed-tracing.md) | Por que Distributed Tracing? |

---

## CI/CD

O projeto inclui um pipeline GitHub Actions completo:

```yaml
# .github/workflows/ci.yml
- Unit Tests + Coverage (JaCoCo)
- Architecture Tests (ArchUnit)
- Integration Tests (Testcontainers)
- Build & Package
- Docker Build & Push
- Security Scan (OWASP)
- SonarCloud Analysis
```

Para ativar, configure os secrets no GitHub:
- `CODECOV_TOKEN` - Para upload de cobertura
- `DOCKER_USERNAME` / `DOCKER_PASSWORD` - Para push de imagens
- `SONAR_TOKEN` - Para analise de qualidade

---

## Licenca

MIT License - Ezio Lima

---

## Screenshots

### Swagger UI
![Swagger](docs/swagger-screenshot.png)

### Zipkin Tracing
![Zipkin](docs/zipkin-screenshot.png)
