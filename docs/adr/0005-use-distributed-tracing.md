# ADR-0005: Distributed Tracing com Zipkin

## Status

**Accepted**

## Context

Em sistemas distribuidos, rastrear o fluxo de uma requisicao e essencial para:

1. **Debugging**: Identificar onde ocorreu um erro
2. **Performance**: Encontrar gargalos em chamadas externas
3. **Observabilidade**: Entender o comportamento do sistema em producao

O CPM faz chamadas externas para:
- LDAP (autenticacao)
- OpenStack (provisionamento)
- PostgreSQL (persistencia)

Alem disso, usa processamento assincrono (`@Async`), onde o contexto de trace pode se perder.

### Alternativas

1. **Logging correlacionado**: Simples, mas dificil de visualizar fluxo
2. **Jaeger**: Robusto, mas requer mais infraestrutura
3. **Zipkin**: Leve, facil de rodar localmente, integracao nativa com Spring
4. **OpenTelemetry**: Padrao emergente, mas mais complexo de configurar

## Decision

Adotar **Zipkin** com **Brave** via Spring Boot Micrometer Tracing.

### Configuracao

```yaml
management:
  tracing:
    sampling:
      probability: 1.0  # 100% sampling em dev
  zipkin:
    tracing:
      endpoint: http://localhost:9411/api/v2/spans
```

### Propagacao de Contexto

**HTTP (WebClient):**
```java
@Bean
public WebClient.Builder webClientBuilder(Tracing tracing) {
    return WebClient.builder()
        .filter(new TracingExchangeFilterFunction(tracing));
}
```

**Async (@Async):**
```java
@Bean
public TaskExecutor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setTaskDecorator(new ContextPropagatingTaskDecorator());
    return executor;
}
```

### Headers Propagados

```
X-B3-TraceId: 80f198ee56343ba864fe8b2a57d3eff7
X-B3-SpanId: e457b5a2e4d86bd1
X-B3-Sampled: 1
```

## Consequences

### Positivas

- **Visibilidade end-to-end**: Ver todo o fluxo de uma requisicao
- **Correlacao de logs**: TraceId/SpanId nos logs facilitam debugging
- **Latency analysis**: Identificar chamadas lentas
- **Context propagation**: Trace continua em threads assincronas

### Negativas

- **Overhead**: Pequeno impacto de performance (mitigado com sampling)
- **Infraestrutura**: Requer Zipkin rodando
- **Dados sensiveis**: Traces podem conter informacoes que precisam ser protegidas

### Metricas Disponiveis

```
GET /actuator/prometheus

# Latencia por endpoint
http_server_requests_seconds_bucket{uri="/api/v1/provisioning",...}

# Spans enviados
zipkin_reporter_spans_total
```

### Visualizacao no Zipkin

```
http://localhost:9411

[Trace View]
├── POST /api/v1/provisioning (150ms)
│   ├── LDAP bind (20ms)
│   ├── PolicyEnforcementService.enforce (1ms)
│   ├── JPA save (30ms)
│   └── [ASYNC] OpenStackAdapter.provisionInstance (100ms)
│       └── POST /servers (95ms)
```

## References

- [Spring Boot Tracing](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html#actuator.micrometer-tracing)
- [Zipkin](https://zipkin.io/)
- [Brave](https://github.com/openzipkin/brave)
- [B3 Propagation](https://github.com/openzipkin/b3-propagation)
