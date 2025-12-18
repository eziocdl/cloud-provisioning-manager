package com.eziocdl.integration;

import com.eziocdl.TestcontainersConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Base64;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@DisplayName("Provisioning API Integration Tests")
class ProvisioningIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    private static final String API_URL = "/api/v1/provisioning";

    // ==================== AUTHENTICATION TEST ====================

    @Test
    @DisplayName("Should return 401 for unauthenticated request")
    void shouldReturn401ForUnauthenticated() {
        String request = """
            {
                "username": "hacker",
                "ram": "4GB",
                "cpu": "2vCPU"
            }
            """;

        webTestClient.post()
                .uri(API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ==================== AUTHENTICATED TESTS ====================

    @Test
    @DisplayName("TRAINEE: Should CREATE small VM within quota (4GB, 2vCPU)")
    void trainee_shouldCreateSmallVm() {
        String request = """
            {
                "username": "trainee",
                "ram": "4GB",
                "cpu": "2vCPU"
            }
            """;

        webTestClient.post()
                .uri(API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", basicAuth("trainee", "senhatrainee123"))
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").exists()
                .jsonPath("$.status").isEqualTo("PENDING_APPROVAL");
    }

    @Test
    @DisplayName("DEV: Should CREATE medium VM within quota (16GB, 4vCPU)")
    void dev_shouldCreateMediumVm() {
        String request = """
            {
                "username": "devuser",
                "ram": "16GB",
                "cpu": "4vCPU"
            }
            """;

        webTestClient.post()
                .uri(API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", basicAuth("devuser", "senhadev123"))
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").exists();
    }

    @Test
    @DisplayName("ADMIN: Should CREATE any size VM (unlimited quota)")
    void admin_shouldCreateAnyVm() {
        String request = """
            {
                "username": "admin",
                "ram": "256GB",
                "cpu": "64vCPU"
            }
            """;

        webTestClient.post()
                .uri(API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", basicAuth("admin", "senhaadmin123"))
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").exists();
    }

    // ==================== POLICY VIOLATION TESTS ====================

    @Test
    @DisplayName("TRAINEE: Should be DENIED for large VM (64GB) - Policy Violation")
    void trainee_shouldBeDeniedForLargeVm() {
        String request = """
            {
                "username": "trainee",
                "ram": "64GB",
                "cpu": "12vCPU"
            }
            """;

        webTestClient.post()
                .uri(API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", basicAuth("trainee", "senhatrainee123"))
                .bodyValue(request)
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Policy Violation")
                .jsonPath("$.userRole").isEqualTo("TRAINEE");
    }

    @Test
    @DisplayName("DEV: Should be DENIED for production-size VM (64GB, 16vCPU)")
    void dev_shouldBeDeniedForProductionVm() {
        String request = """
            {
                "username": "devuser",
                "ram": "64GB",
                "cpu": "16vCPU"
            }
            """;

        webTestClient.post()
                .uri(API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", basicAuth("devuser", "senhadev123"))
                .bodyValue(request)
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("$.userRole").isEqualTo("DEV");
    }

    // ==================== HELPER METHOD ====================

    private String basicAuth(String username, String password) {
        String credentials = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
    }
}
