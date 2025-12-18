# ADR-0003: Circuit Breaker para Integracao com Cloud Provider

## Status

**Accepted**

## Context

O CPM se integra com OpenStack para provisionamento de VMs. Essa integracao pode falhar por diversos motivos:

1. **Timeout**: OpenStack demorando para responder
2. **Indisponibilidade**: OpenStack fora do ar
3. **Rate Limiting**: Muitas requisicoes simultaneas
4. **Erros de rede**: Problemas de conectividade

Sem tratamento adequado, falhas no OpenStack podem:
- Travar threads esperando timeout
- Causar efeito cascata (todas requisicoes falham)
- Sobrecarregar o OpenStack com retries agressivos

### Alternativas

1. **Timeout simples**: Rapido de implementar, mas nao previne sobrecarga
2. **Retry com backoff**: Bom para falhas transitorias, ruim para indisponibilidade
3. **Circuit Breaker**: Falha rapido quando sistema esta instavel, permite recuperacao
4. **Bulkhead**: Isola falhas em pools separados

## Decision

Adotar **Circuit Breaker** com Resilience4j no `OpenStackAdapter`.

### Configuracao

```properties
resilience4j.circuitbreaker.instances.openstack.failure-rate-threshold=50
resilience4j.circuitbreaker.instances.openstack.wait-duration-in-open-state=5s
resilience4j.circuitbreaker.instances.openstack.sliding-window-size=5
resilience4j.circuitbreaker.instances.openstack.slow-call-duration-threshold=2s
```

### Estados do Circuit Breaker

```
CLOSED ──(50% falhas)──> OPEN ──(5s)──> HALF_OPEN ──(sucesso)──> CLOSED
                                              │
                                              └──(falha)──> OPEN
```

### Fallback Strategy

Quando o circuito abre, o fallback retorna `"QUEUE-WAITING-ALLOCATION"`, permitindo:
- Requisicao nao travar
- Usuario saber que pedido foi aceito mas esta em fila
- Processamento posterior quando OpenStack voltar

### Implementacao

```java
@CircuitBreaker(name = "openstack", fallbackMethod = "fallbackProvision")
public String provisionInstance(ProvisioningRequest request) {
    return webClient.post()
        .uri("/servers")
        .retrieve()
        .bodyToMono(String.class)
        .timeout(Duration.ofSeconds(3))
        .block();
}

public String fallbackProvision(ProvisioningRequest request, Throwable t) {
    log.warn("OpenStack unavailable, queuing request: {}", request.getId());
    return "QUEUE-WAITING-ALLOCATION";
}
```

## Consequences

### Positivas

- **Fail-fast**: Requisicoes nao ficam travadas esperando timeout
- **Recuperacao automatica**: Sistema tenta reconectar apos periodo de espera
- **Visibilidade**: Estado do circuito pode ser monitorado via Actuator
- **Graceful degradation**: Usuario recebe resposta mesmo quando OpenStack esta fora

### Negativas

- **Complexidade**: Mais um conceito para entender e configurar
- **Calibracao**: Thresholds precisam ser ajustados para cada ambiente
- **Estado persistido em memoria**: Reinicio da aplicacao reseta o circuito

### Metricas Expostas

```
GET /actuator/health
{
  "circuitBreakers": {
    "openstack": {
      "state": "CLOSED",
      "failureRate": "0.0%"
    }
  }
}
```

## References

- [Resilience4j Documentation](https://resilience4j.readme.io/docs/circuitbreaker)
- [Circuit Breaker Pattern - Martin Fowler](https://martinfowler.com/bliki/CircuitBreaker.html)
- [Release It! - Michael Nygard](https://pragprog.com/titles/mnee2/release-it-second-edition/)
