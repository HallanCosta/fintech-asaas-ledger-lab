package com.hallancosta.asaas.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AsaasApiKeyFilterTest {

    @Test
    void protegeSomenteAsRotasFinanceiras() {
        assertTrue(AsaasApiKeyFilter.protects("/api/asaas"));
        assertTrue(AsaasApiKeyFilter.protects("/api/asaas/"));
        assertTrue(AsaasApiKeyFilter.protects("/api/asaas/balance"));
        assertTrue(AsaasApiKeyFilter.protects("/api/asaas/statement"));
        assertTrue(AsaasApiKeyFilter.protects("/api/asaas/payments/pix"));
        assertTrue(AsaasApiKeyFilter.protects("/api/asaas/payments/pay_1/pix-qrcode"));
        assertTrue(AsaasApiKeyFilter.protects("/api/asaas/transfers/pix"));
        assertFalse(AsaasApiKeyFilter.protects("/api/health"));
        assertFalse(AsaasApiKeyFilter.protects("/actuator/health"));
        assertFalse(AsaasApiKeyFilter.protects("/api/asaas-extra"));
        assertFalse(AsaasApiKeyFilter.protects(null));
        assertFalse(AsaasApiKeyFilter.protects(""));
    }

    @Test
    void leOCaminhoPublicoDoHealth() {
        var request = new MockHttpServletRequest("GET", "/api/health");

        assertEquals("/api/health", AsaasApiKeyFilter.requestPath(request));
        assertFalse(AsaasApiKeyFilter.protects(AsaasApiKeyFilter.requestPath(request)));
    }
}
