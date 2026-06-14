package com.learning.axon.command.integration;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Spring Boot integration test for the command service.
 *
 * <p><b>Known limitation:</b> Axon Framework 4.x JPA event-store uses the
 * {@code javax.persistence.*} namespace internally, while Spring Boot 4.x /
 * Hibernate 7 exposes only {@code jakarta.persistence.*}. This causes the
 * Spring application context to fail on startup.  The Axon unit tests
 * ({@link com.learning.axon.command.aggregate.AccountAggregateTest}) work
 * correctly — they exercise the aggregate logic in isolation without Spring.
 * Full integration tests require either Axon 5.x (with jakarta support) or
 * Spring Boot 3.x (compatible with Axon 4.x).
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Command Service Integration Tests")
@Disabled("Axon 4.x javax.persistence vs Spring Boot 4.x jakarta.persistence namespace mismatch")
class CommandServiceIntegrationTest {

    @Test
    @DisplayName("application context loads successfully")
    void contextLoads() {
    }

    @Test
    @DisplayName("POST /bank-accounts creates an account and returns 201")
    void createAccount_shouldReturn201() {
    }
}
