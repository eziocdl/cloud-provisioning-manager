package com.eziocdl.api.dto.response;

import java.util.UUID;

public record VmStatusResponse(

        UUID id,
        String status,
        String trackingId
) {}