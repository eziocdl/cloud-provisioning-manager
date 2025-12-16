package com.eziocdl.application.port.out;

import com.eziocdl.domain.model.ProvisioningRequest;

public interface CloudProviderPort {
    String provisionInstance(ProvisioningRequest request);
}
