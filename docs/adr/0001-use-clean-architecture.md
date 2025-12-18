# ADR-0001: Adotar Clean Architecture

## Status

**Accepted**

## Context

O projeto Cloud Provisioning Manager (CPM) e uma API de governanca que precisa:

1. Ser facilmente testavel (regras de negocio independentes de frameworks)
2. Permitir troca de tecnologias (banco de dados, provedor de nuvem) sem afetar o core
3. Facilitar manutencao por times diferentes (frontend, backend, infra)
4. Demonstrar maturidade arquitetural para portfolio profissional

As alternativas consideradas foram:

- **MVC tradicional**: Simples, mas acopla regras de negocio ao framework
- **Hexagonal Architecture**: Bom isolamento, mas nomenclatura menos conhecida
- **Clean Architecture**: Combina os beneficios de Hexagonal com nomenclatura padronizada

## Decision

Adotar **Clean Architecture** com as seguintes camadas:

```
src/main/java/com/eziocdl/
├── api/                    # Camada de Interface (Controllers, DTOs)
├── application/            # Camada de Aplicacao (Use Cases, Ports)
├── domain/                 # Camada de Dominio (Entities, Services, Events)
└── infrastructure/         # Camada de Infraestrutura (Adapters, Configs)
```

### Regras de Dependencia

1. **Domain** nao depende de nenhuma outra camada
2. **Application** depende apenas de Domain
3. **API** depende de Application e Domain
4. **Infrastructure** implementa os Ports definidos em Application

### Ports & Adapters

- **Input Ports**: Interfaces em `application/port/in/` implementadas pelos Use Cases
- **Output Ports**: Interfaces em `application/port/out/` implementadas pelos Adapters

## Consequences

### Positivas

- **Testabilidade**: Domain e Application podem ser testados sem Spring ou banco de dados
- **Flexibilidade**: Trocar PostgreSQL por MongoDB requer apenas novo Adapter
- **Manutenibilidade**: Cada camada tem responsabilidade clara
- **Documentacao viva**: Estrutura de pastas documenta a arquitetura
- **Portfolio**: Demonstra conhecimento de padroes enterprise

### Negativas

- **Complexidade inicial**: Mais pastas e interfaces do que MVC tradicional
- **Curva de aprendizado**: Novos desenvolvedores precisam entender o padrao
- **Boilerplate**: Mapeamento entre entidades de dominio e entidades JPA

## Validacao

A arquitetura e validada automaticamente por testes ArchUnit em:
`src/test/java/com/eziocdl/architecture/CleanArchitectureTest.java`

## References

- [Clean Architecture - Robert C. Martin](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Hexagonal Architecture - Alistair Cockburn](https://alistair.cockburn.us/hexagonal-architecture/)
- [ArchUnit Documentation](https://www.archunit.org/userguide/html/000_Index.html)
