package com.learning.axon.saga.integration;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * @see com.learning.axon.command.integration.CommandServiceIntegrationTest for the
 * rationale behind @Disabled — Axon 4.x / Spring Boot 4.x javax vs jakarta incompatibility.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Saga Service Integration Tests")
@Disabled("Axon 4.x javax.persistence vs Spring Boot 4.x jakarta.persistence namespace mismatch")
class SagaServiceIntegrationTest {

    @Test
    @DisplayName("application context loads successfully")
    void contextLoads() {
    }
}
