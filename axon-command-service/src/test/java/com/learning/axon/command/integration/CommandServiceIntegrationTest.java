package com.learning.axon.command.integration;

import com.learning.axon.shared.models.AccountCreateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test for the command service context loading and REST endpoint.
 * Uses H2 in-memory database — no Docker required.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Command Service Integration Tests")
class CommandServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("application context loads successfully")
    void contextLoads() {
    }

    @Test
    @DisplayName("POST /bank-accounts creates an account and returns 201")
    void createAccount_shouldReturn201() throws Exception {
        String body = """
                {"startingBalance": 500.0, "currency": "USD"}
                """;

        mockMvc.perform(post("/bank-accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }
}
