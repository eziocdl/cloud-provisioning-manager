package com.eziocdl.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "provisioning_requests")
@Getter
@NoArgsConstructor
public class ProvisioningRequest {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String requesterUsername;

    @Column(nullable = false)
    private String ram;

    @Column(nullable = false)
    private String cpu;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProvisioningStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Factory

    public ProvisioningRequest(String requesterUsername, String ram, String cpu) {
        if (requesterUsername == null || requesterUsername.isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (ram == null || ram.isBlank()) {
            throw new IllegalArgumentException("RAM is required");
        }
        if (cpu == null || cpu.isBlank()) {
            throw new IllegalArgumentException("CPU is required");
        }

        this.id = UUID.randomUUID();
        this.requesterUsername = requesterUsername;
        this.ram = ram;
        this.cpu = cpu;
        this.status = ProvisioningStatus.PENDING_APPROVAL;
        this.createdAt = LocalDateTime.now();
    }



    public void approve() {
        if (this.status != ProvisioningStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Apenas os pedidos pendentes podem ser aprovados");
        }
        this.status = ProvisioningStatus.APPROVED;
        this.updatedAt = LocalDateTime.now();
    }

    public void reject() {
        if (this.status != ProvisioningStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Apenas os pedidos pendentes podem ser rejeitados");
        }

        this.status = ProvisioningStatus.REJECTED;
        this.updatedAt = LocalDateTime.now();
    }

    public void markProvisioning() {
        if (this.status != ProvisioningStatus.APPROVED) {
            throw new IllegalStateException("O pedido precisa ser aprovado antes de provisionar");
        }
        this.status = ProvisioningStatus.PROVISIONING;
        this.updatedAt = LocalDateTime.now();
    }

    public void complete() {

        this.status = ProvisioningStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    public void fail() {
        this.status = ProvisioningStatus.FAILED;
        this.updatedAt = LocalDateTime.now();
    }
}