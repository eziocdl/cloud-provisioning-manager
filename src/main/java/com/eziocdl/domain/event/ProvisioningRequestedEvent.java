package com.eziocdl.domain.event;

import java.util.UUID;

public record ProvisioningRequestedEvent(UUID provisioningId) {
}