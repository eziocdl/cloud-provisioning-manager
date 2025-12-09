package com.eziocdl.api.controller;

import com.eziocdl.api.dto.request.CreateVmRequest;
import com.eziocdl.api.dto.response.VmStatusResponse;
import com.eziocdl.application.port.in.CreateProvisioningUseCasePort;
import com.eziocdl.domain.model.ProvisioningRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/provisioning")
@Tag(name = "Provisioning API", description = "Endpoints para gerenciamento do ciclo de vida de infraestrutura")
public class ProvisioningController {

    private final CreateProvisioningUseCasePort useCase;

    public ProvisioningController(CreateProvisioningUseCasePort useCase) {
        this.useCase = useCase;
    }

    @Operation(
            summary = "Provisionar Recurso",
            description = "Inicia o workflow de provisionamento de uma nova Máquina Virtual. A solicitação é persistida e submetida às regras de governança antes da execução no provedor de nuvem."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Solicitação aceita e persistida."),
            @ApiResponse(responseCode = "400", description = "Falha na validação do contrato de entrada."),
            @ApiResponse(responseCode = "500", description = "Erro interno no processamento da solicitação.")
    })
    @PostMapping
    public ResponseEntity<VmStatusResponse> create(@RequestBody CreateVmRequest request) {

        ProvisioningRequest domainObject = useCase.create(
                request.username(),
                request.ram(),
                request.cpu()
        );

        VmStatusResponse response = new VmStatusResponse(
                domainObject.getId(),
                domainObject.getStatus().name(),
                domainObject.getId().toString()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}