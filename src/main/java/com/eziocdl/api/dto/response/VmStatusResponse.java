package com.eziocdl.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Objeto de resposta contendo metadados do processamento.")
public record VmStatusResponse(

        @Schema(description = "Identificador único (UUID) do recurso no sistema.", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,

        @Schema(description = "Estado atual do ciclo de vida da solicitação.", example = "PENDING_APPROVAL")
        String status,

        @Schema(description = "Identificador de correlação para rastreamento de logs (Trace ID).", example = "a1b2c3d4e5f6...")
        String trackingId
) {}