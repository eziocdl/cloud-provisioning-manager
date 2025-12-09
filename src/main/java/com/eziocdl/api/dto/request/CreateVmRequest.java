package com.eziocdl.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Especificação técnica para solicitação de provisionamento de VM.")
public record CreateVmRequest(

        @Schema(description = "Identificador do usuário no diretório corporativo (LDAP).", example = "ezio_auditore", requiredMode = Schema.RequiredMode.REQUIRED)
        String username,

        @Schema(description = "Alocação de memória RAM desejada.", example = "16GB", requiredMode = Schema.RequiredMode.REQUIRED)
        String ram,

        @Schema(description = "Alocação de vCPUs.", example = "8vCPU", requiredMode = Schema.RequiredMode.REQUIRED)
        String cpu
) {
    public CreateVmRequest {
        if (username == null || username.isBlank()) throw new IllegalArgumentException("Username required");
        if (ram == null || ram.isBlank()) throw new IllegalArgumentException("RAM required");
        if (cpu == null || cpu.isBlank()) throw new IllegalArgumentException("CPU required");
    }
}