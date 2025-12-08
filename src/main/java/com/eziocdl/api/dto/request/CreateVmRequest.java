package com.eziocdl.api.dto.request;

public record CreateVmRequest(

        String username,
        String ram,
        String cpu
) {

    public CreateVmRequest {

        if(username == null || username.isBlank()) throw new IllegalArgumentException("Username required");
        if(ram == null || ram.isBlank()) throw new IllegalArgumentException("RAM required");
        if(cpu == null || cpu.isBlank()) throw new IllegalArgumentException("CPU required");
    }

}
