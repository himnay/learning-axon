package com.learning.axon.saga.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Saga Service Integration Tests")
class SagaServiceIntegrationTest {

    @Test
    @DisplayName("application context loads successfully")
    void contextLoads() {
    }
}
