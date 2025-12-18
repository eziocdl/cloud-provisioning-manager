package com.eziocdl.domain.service;

import com.eziocdl.domain.exception.PolicyViolationException;
import com.eziocdl.domain.model.ResourceQuota;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Domain Service responsible for enforcing resource governance policies.
 *
 * Business Rules:
 * - TRAINEE: Max 8GB RAM, 4 vCPU (learning environment)
 * - DEV: Max 32GB RAM, 8 vCPU (development workloads)
 * - ADMIN: Unlimited (production/emergency access)
 *
 * This prevents Shadow IT and ensures cost control.
 */
@Service
public class PolicyEnforcementService {

    private static final Pattern RAM_PATTERN = Pattern.compile("(\\d+)\\s*[Gg][Bb]?");
    private static final Pattern CPU_PATTERN = Pattern.compile("(\\d+)\\s*[vV]?[Cc][Pp][Uu]?");

    /**
     * Validates if the requested resources comply with the user's role quota.
     *
     * @param userRole The role from LDAP (TRAINEE, DEV, ADMIN)
     * @param ram Requested RAM (e.g., "16GB", "32gb")
     * @param cpu Requested CPU (e.g., "4vCPU", "8vcpu")
     * @throws PolicyViolationException if quota is exceeded
     */
    public void enforce(String userRole, String ram, String cpu) {
        ResourceQuota quota = ResourceQuota.forRole(userRole);

        int requestedRam = parseRam(ram);
        int requestedCpu = parseCpu(cpu);

        System.out.println("🛡️ [Policy] Checking quota for role=" + userRole +
                           " | Requested: RAM=" + requestedRam + "GB, CPU=" + requestedCpu + "vCPU" +
                           " | Allowed: RAM=" + quota.maxRamFormatted() + ", CPU=" + quota.maxCpuFormatted());

        if (!quota.allowsRam(requestedRam)) {
            throw new PolicyViolationException(
                    userRole, "RAM", ram, quota.maxRamFormatted()
            );
        }

        if (!quota.allowsCpu(requestedCpu)) {
            throw new PolicyViolationException(
                    userRole, "CPU", cpu, quota.maxCpuFormatted()
            );
        }

        System.out.println("✅ [Policy] Request APPROVED for role=" + userRole);
    }

    private int parseRam(String ram) {
        Matcher matcher = RAM_PATTERN.matcher(ram);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        throw new IllegalArgumentException("Invalid RAM format: " + ram + ". Expected format: '16GB' or '32gb'");
    }

    private int parseCpu(String cpu) {
        Matcher matcher = CPU_PATTERN.matcher(cpu);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        throw new IllegalArgumentException("Invalid CPU format: " + cpu + ". Expected format: '4vCPU' or '8vcpu'");
    }
}
