package com.eziocdl.api.controller;

import com.eziocdl.api.dto.request.CreateVmRequest;
import com.eziocdl.api.dto.response.VmStatusResponse;
import com.eziocdl.application.port.in.CreateProvisioningUseCasePort;
import com.eziocdl.domain.model.ProvisioningRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/provisioning")
public class ProvisioningController {

    private final CreateProvisioningUseCasePort useCase;

    public ProvisioningController(CreateProvisioningUseCasePort useCase) {
        this.useCase = useCase;
    }

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

