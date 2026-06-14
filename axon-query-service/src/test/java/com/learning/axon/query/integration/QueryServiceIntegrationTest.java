package com.learning.axon.query.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Query Service Integration Tests")
class QueryServiceIntegrationTest {

    @Test
    @DisplayName("application context loads successfully")
    void contextLoads() {
    }
}
