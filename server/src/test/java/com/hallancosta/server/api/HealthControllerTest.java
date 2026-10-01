package com.hallancosta.server.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class HealthControllerTest {

    @Test
    void healthReportaServicoDisponivel() {
        var response = new HealthController().health();

        assertEquals("UP", response.status());
        assertEquals("fintech-pix-lab-server", response.service());
        assertNotNull(response.timestamp());
    }
}
