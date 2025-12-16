package com.eziocdl.infrastructure.adapter.cloud;

import com.eziocdl.application.port.out.CloudProviderPort;
import com.eziocdl.domain.model.ProvisioningRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Component
public class OpenStackAdapter implements CloudProviderPort {

    private final WebClient webClient;

    public OpenStackAdapter(WebClient.Builder builder) {
        this.webClient = builder
                .baseUrl("http://localhost:8081")
                .build();
    }

    @Override
    @CircuitBreaker(name = "openstack", fallbackMethod = "fallbackProvision")
    public String provisionInstance(ProvisioningRequest request) {
        System.out.println("☁️ [OpenStackAdapter] Tentando chamar a nuvem...");

        var payload = """
            { "server": { "name": "vm-%s", "imageRef": "ubuntu-22.04" } }
            """.formatted(request.getId());


        String response = webClient.post()
                .uri("/servers")
                .header("Content-Type", "application/json")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(3)) // Timeout técnico
                .block();

        System.out.println("[OpenStackAdapter] Sucesso!");
        return "i-007-instance-uuid";
    }

    public String fallbackProvision(ProvisioningRequest request, Throwable t) {
        System.err.println("[Resilience] Circuito Aberto ou Erro! Causa: " + t.getMessage());

        return "QUEUE-WAITING-ALLOCATION";
    }
}