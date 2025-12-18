# ADR-0004: Processamento Assincrono com Domain Events

## Status

**Accepted**

## Context

O fluxo de provisionamento tem duas partes:

1. **Sincrona**: Validar politicas, salvar pedido no banco
2. **Assincrona**: Chamar OpenStack para criar a VM

Se a chamada ao OpenStack fosse sincrona, o usuario esperaria varios segundos (ou minutos) por uma resposta. Alem disso, falhas no OpenStack nao deveriam impedir o registro do pedido.

### Alternativas

1. **Sincrono**: Simples, mas bloqueia o usuario
2. **@Async no UseCase**: Funciona, mas mistura responsabilidades
3. **Message Queue (RabbitMQ/Kafka)**: Robusto, mas adiciona complexidade operacional
4. **Spring Events + @TransactionalEventListener**: Desacoplado, sem infraestrutura extra

## Decision

Usar **Spring Application Events** com `@TransactionalEventListener(phase = AFTER_COMMIT)`.

### Fluxo

```
1. Controller recebe request
2. UseCase:
   - Valida politicas
   - Salva pedido no banco
   - Publica ProvisioningRequestedEvent
3. Retorna 201 CREATED imediatamente
4. [APOS COMMIT] AsyncListener:
   - Busca pedido no banco
   - Chama OpenStack
   - Atualiza status (ACTIVE ou FAILED)
```

### Implementacao

**Domain Event:**
```java
public record ProvisioningRequestedEvent(UUID provisioningId) {}
```

**UseCase:**
```java
@Transactional
public ProvisioningRequest create(...) {
    // ... validacao e persistencia ...
    eventPublisher.publishEvent(new ProvisioningRequestedEvent(savedRequest.getId()));
    return savedRequest;
}
```

**Listener:**
```java
@Async
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void handleProvisioningRequested(ProvisioningRequestedEvent event) {
    // Processa em thread separada APOS commit do banco
}
```

### Por que AFTER_COMMIT?

Se o evento fosse processado BEFORE_COMMIT e a transacao falhasse, o OpenStack teria criado uma VM orfao. Com AFTER_COMMIT, so processamos se o pedido realmente foi salvo.

## Consequences

### Positivas

- **Resposta rapida**: Usuario recebe 201 em milissegundos
- **Consistencia**: Evento so dispara apos commit bem-sucedido
- **Desacoplamento**: UseCase nao conhece detalhes de provisionamento
- **Simplicidade**: Nao requer infraestrutura de mensageria

### Negativas

- **Eventos perdidos**: Se aplicacao cair apos commit mas antes do listener, evento se perde
- **Sem replay**: Nao ha historico de eventos para reprocessamento
- **Escalabilidade limitada**: Thread pool local, nao distribui entre instancias

### Mitigacoes

1. **Job de reconciliacao**: Cronjob que busca pedidos em `PENDING_APPROVAL` ha muito tempo
2. **Outbox Pattern**: Salvar evento em tabela e processar com poll (se necessario)
3. **Idempotencia**: Listener deve ser seguro para reprocessamento

### Melhorias Futuras

Para cenarios de alta escala, considerar migracao para:
- Spring Cloud Stream + Kafka
- Outbox Pattern com Debezium

## References

- [Spring Application Events](https://docs.spring.io/spring-framework/reference/core/beans/context-introduction.html#context-functionality-events)
- [TransactionalEventListener](https://docs.spring.io/spring-framework/reference/data-access/transaction/event.html)
- [Outbox Pattern](https://microservices.io/patterns/data/transactional-outbox.html)
