package com.hallancosta.asaas.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConstantTimeSecretTest {

    @Test
    void aceitaOMesmoSegredo() {
        assertTrue(ConstantTimeSecret.matches("internal-test-key", "internal-test-key"));
        assertTrue(ConstantTimeSecret.matches("ação", "ação"));
    }

    @Test
    void rejeitaSegredoAusenteDiferenteOuDeOutroTamanho() {
        assertFalse(ConstantTimeSecret.matches("internal-test-key", "internal-test-kez"));
        assertFalse(ConstantTimeSecret.matches("internal-test-key", "internal-test-key "));
        assertFalse(ConstantTimeSecret.matches("internal-test-key", "internal-test-key-extra"));
        assertFalse(ConstantTimeSecret.matches("internal-test-key", ""));
        assertFalse(ConstantTimeSecret.matches("internal-test-key", null));
        assertFalse(ConstantTimeSecret.matches(null, "internal-test-key"));
        assertFalse(ConstantTimeSecret.matches(null, null));
        assertFalse(ConstantTimeSecret.matches("", ""));
    }
}
