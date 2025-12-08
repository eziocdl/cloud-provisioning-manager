package com.eziocdl.application.port.in;

import com.eziocdl.domain.model.ProvisioningRequest;

public interface CreateProvisioningUseCasePort {
    ProvisioningRequest create(String username, String ram, String cpu);
}
