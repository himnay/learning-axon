package com.learning.axon.saga.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Boots the full service against the test profile (H2 JPA event/token store, RabbitMQ
 * excluded) to prove the Axon + Spring Boot 4 wiring is valid.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Saga Service Integration Tests")
class SagaServiceIntegrationTest {

    @Test
    @DisplayName("application context loads successfully")
    void contextLoads() {
    }
}
