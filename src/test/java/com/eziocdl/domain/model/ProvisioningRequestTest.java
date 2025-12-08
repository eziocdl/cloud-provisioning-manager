package com.eziocdl.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProvisioningRequestTest {

    // --- 1. Testes de Criação (Happy Path & Validation) ---

    @Test
    @DisplayName("Deve nascer com status PENDING_APPROVAL e ID gerado")
    void shouldInitializeCorrectly() {
        var request = new ProvisioningRequest("ezio", "16GB", "4vCPU");

        assertThat(request.getId()).isNotNull();
        assertThat(request.getStatus()).isEqualTo(ProvisioningStatus.PENDING_APPROVAL);
        assertThat(request.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Não deve criar request sem usuário (Validação)")
    void shouldNotCreateWithoutUser() {
        assertThatThrownBy(() -> new ProvisioningRequest(null, "16GB", "4vCPU"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username is required");
    }

    // --- 2. Testes de Transição de Estado (Máquina de Estados) ---

    @Test
    @DisplayName("Deve aprovar um pedido pendente")
    void shouldApprovePendingRequest() {
        var request = new ProvisioningRequest("ezio", "8GB", "2vCPU");

        request.approve();

        assertThat(request.getStatus()).isEqualTo(ProvisioningStatus.APPROVED);
        assertThat(request.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Deve rejeitar um pedido pendente")
    void shouldRejectPendingRequest() {
        var request = new ProvisioningRequest("ezio", "8GB", "2vCPU");

        request.reject();

        assertThat(request.getStatus()).isEqualTo(ProvisioningStatus.REJECTED); // Isso validará sua correção!
    }

    @Test
    @DisplayName("Deve marcar como provisionando apenas se estiver aprovado")
    void shouldMarkProvisioning() {
        var request = new ProvisioningRequest("ezio", "8GB", "2vCPU");
        request.approve(); // Precisa aprovar antes

        request.markProvisioning();

        assertThat(request.getStatus()).isEqualTo(ProvisioningStatus.PROVISIONING);
    }

    @Test
    @DisplayName("Deve completar o fluxo com sucesso (Active)")
    void shouldCompleteProvisioning() {
        var request = new ProvisioningRequest("ezio", "8GB", "2vCPU");
        // Simulando o ciclo de vida completo
        request.approve();
        request.markProvisioning();

        request.complete();

        assertThat(request.getStatus()).isEqualTo(ProvisioningStatus.ACTIVE);
    }

    // --- 3. Testes de Regras de Negócio (Bloqueios) ---

    @Test
    @DisplayName("Erro: Não pode provisionar direto sem aprovação")
    void shouldFailToProvisionPendingRequest() {
        var request = new ProvisioningRequest("ezio", "8GB", "2vCPU");

        assertThatThrownBy(request::markProvisioning)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("precisa ser aprovado");
    }

    @Test
    @DisplayName("Erro: Não pode aprovar um pedido já rejeitado")
    void shouldFailToApproveRejectedRequest() {
        var request = new ProvisioningRequest("ezio", "8GB", "2vCPU");
        request.reject();

        assertThatThrownBy(request::approve)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Apenas os pedidos pendentes podem ser aprovados");
    }
}