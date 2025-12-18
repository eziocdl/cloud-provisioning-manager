package com.eziocdl.domain.model;

/**
 * Value Object representing resource quotas for a user role.
 * Immutable by design - enforces governance limits.
 */
public record ResourceQuota(
        String role,
        int maxRamGb,
        int maxCpuCores
) {
    // Predefined quotas per role
    public static final ResourceQuota TRAINEE = new ResourceQuota("TRAINEE", 8, 4);
    public static final ResourceQuota DEV = new ResourceQuota("DEV", 32, 8);
    public static final ResourceQuota ADMIN = new ResourceQuota("ADMIN", Integer.MAX_VALUE, Integer.MAX_VALUE);

    public static ResourceQuota forRole(String role) {
        return switch (role.toUpperCase()) {
            case "TRAINEE" -> TRAINEE;
            case "DEV", "DEVELOPER" -> DEV;
            case "ADMIN", "ADMINISTRATOR" -> ADMIN;
            default -> TRAINEE; // Least privilege principle
        };
    }

    public boolean allowsRam(int requestedGb) {
        return requestedGb <= maxRamGb;
    }

    public boolean allowsCpu(int requestedCores) {
        return requestedCores <= maxCpuCores;
    }

    public String maxRamFormatted() {
        return maxRamGb == Integer.MAX_VALUE ? "unlimited" : maxRamGb + "GB";
    }

    public String maxCpuFormatted() {
        return maxCpuCores == Integer.MAX_VALUE ? "unlimited" : maxCpuCores + "vCPU";
    }
}
