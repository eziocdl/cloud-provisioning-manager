package com.eziocdl.domain.service;

import com.eziocdl.domain.exception.PolicyViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PolicyEnforcementServiceTest {

    private PolicyEnforcementService policyService;

    @BeforeEach
    void setUp() {
        policyService = new PolicyEnforcementService();
    }

    @Nested
    @DisplayName("TRAINEE Quota Tests (Max: 8GB RAM, 4vCPU)")
    class TraineeQuotaTests {

        @Test
        @DisplayName("Should ALLOW trainee to create small VM (4GB, 2vCPU)")
        void shouldAllowSmallVm() {
            assertThatCode(() -> policyService.enforce("TRAINEE", "4GB", "2vCPU"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should ALLOW trainee to create VM at quota limit (8GB, 4vCPU)")
        void shouldAllowAtLimit() {
            assertThatCode(() -> policyService.enforce("TRAINEE", "8GB", "4vCPU"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should DENY trainee exceeding RAM quota (16GB)")
        void shouldDenyExcessiveRam() {
            assertThatThrownBy(() -> policyService.enforce("TRAINEE", "16GB", "2vCPU"))
                    .isInstanceOf(PolicyViolationException.class)
                    .hasMessageContaining("TRAINEE")
                    .hasMessageContaining("RAM");
        }

        @Test
        @DisplayName("Should DENY trainee exceeding CPU quota (8vCPU)")
        void shouldDenyExcessiveCpu() {
            assertThatThrownBy(() -> policyService.enforce("TRAINEE", "4GB", "8vCPU"))
                    .isInstanceOf(PolicyViolationException.class)
                    .hasMessageContaining("TRAINEE")
                    .hasMessageContaining("CPU");
        }
    }

    @Nested
    @DisplayName("DEV Quota Tests (Max: 32GB RAM, 8vCPU)")
    class DevQuotaTests {

        @Test
        @DisplayName("Should ALLOW dev to create medium VM (16GB, 4vCPU)")
        void shouldAllowMediumVm() {
            assertThatCode(() -> policyService.enforce("DEV", "16GB", "4vCPU"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should ALLOW dev to create VM at quota limit (32GB, 8vCPU)")
        void shouldAllowAtLimit() {
            assertThatCode(() -> policyService.enforce("DEV", "32GB", "8vCPU"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should DENY dev exceeding RAM quota (64GB)")
        void shouldDenyExcessiveRam() {
            assertThatThrownBy(() -> policyService.enforce("DEV", "64GB", "4vCPU"))
                    .isInstanceOf(PolicyViolationException.class)
                    .hasMessageContaining("DEV")
                    .hasMessageContaining("RAM");
        }

        @Test
        @DisplayName("Should DENY dev exceeding CPU quota (16vCPU)")
        void shouldDenyExcessiveCpu() {
            assertThatThrownBy(() -> policyService.enforce("DEV", "16GB", "16vCPU"))
                    .isInstanceOf(PolicyViolationException.class)
                    .hasMessageContaining("DEV")
                    .hasMessageContaining("CPU");
        }
    }

    @Nested
    @DisplayName("ADMIN Quota Tests (Unlimited)")
    class AdminQuotaTests {

        @Test
        @DisplayName("Should ALLOW admin to create any size VM")
        void shouldAllowAnySize() {
            assertThatCode(() -> policyService.enforce("ADMIN", "256GB", "64vCPU"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should ALLOW admin to create production-grade VM")
        void shouldAllowProductionVm() {
            assertThatCode(() -> policyService.enforce("ADMIN", "512GB", "128vCPU"))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Unknown Role Tests")
    class UnknownRoleTests {

        @Test
        @DisplayName("Should default to TRAINEE quota for unknown role (Least Privilege)")
        void shouldDefaultToTrainee() {
            // Unknown role should get TRAINEE limits (8GB, 4vCPU)
            assertThatThrownBy(() -> policyService.enforce("UNKNOWN_ROLE", "16GB", "8vCPU"))
                    .isInstanceOf(PolicyViolationException.class);
        }
    }

    @Nested
    @DisplayName("Input Format Tests")
    class InputFormatTests {

        @Test
        @DisplayName("Should accept lowercase format (16gb, 4vcpu)")
        void shouldAcceptLowercase() {
            assertThatCode(() -> policyService.enforce("DEV", "16gb", "4vcpu"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should accept format with spaces (16 GB, 4 vCPU)")
        void shouldAcceptSpaces() {
            assertThatCode(() -> policyService.enforce("DEV", "16 GB", "4 vCPU"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should reject invalid RAM format")
        void shouldRejectInvalidRam() {
            assertThatThrownBy(() -> policyService.enforce("DEV", "sixteen-gigs", "4vCPU"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid RAM format");
        }

        @Test
        @DisplayName("Should reject invalid CPU format")
        void shouldRejectInvalidCpu() {
            assertThatThrownBy(() -> policyService.enforce("DEV", "16GB", "four-cores"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid CPU format");
        }
    }
}
