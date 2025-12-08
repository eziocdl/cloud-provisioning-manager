package com.eziocdl.application.port.out;

import com.eziocdl.domain.model.ProvisioningRequest;

import java.util.Optional;
import java.util.UUID;

public interface ProvisioningRepositoryPort {
    ProvisioningRequest save(ProvisioningRequest request);
    Optional<ProvisioningRequest> findById(UUID id);
}
