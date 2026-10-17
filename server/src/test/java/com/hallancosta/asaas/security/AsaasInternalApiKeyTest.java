package com.hallancosta.asaas.security;

import com.hallancosta.asaas.config.AsaasProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AsaasInternalApiKeyTest {

    @Test
    void exigeSegredoQuandoIntegracaoEstaLigada() {
        var exception = assertThrows(IllegalStateException.class,
                () -> AsaasInternalApiKey.requireWhenEnabled(true, "   ", "asaas-key"));

        assertEquals(AsaasInternalApiKey.MISSING_MESSAGE, exception.getMessage());
    }

    @Test
    void rejeitaSegredoIgualAChaveDoAsaas() {
        var exception = assertThrows(IllegalStateException.class,
                () -> AsaasInternalApiKey.requireWhenEnabled(true, "same-key", "same-key"));

        assertEquals(AsaasInternalApiKey.SAME_AS_PROVIDER_MESSAGE, exception.getMessage());
    }

    @Test
    void permiteIntegracaoDesligadaSemSegredo() {
        assertDoesNotThrow(() -> AsaasInternalApiKey.requireWhenEnabled(false, null, null));
    }

    @Test
    void aceitaSegredoDistintoDaChaveDoAsaas() {
        assertDoesNotThrow(() -> AsaasInternalApiKey.requireWhenEnabled(true, "internal-key", "asaas-key"));
    }

    @Test
    void leOSegredoDasPropriedades() {
        AsaasProperties properties = new AsaasProperties();
        properties.setEnabled(true);
        properties.setApiKey("asaas-key");

        var exception = assertThrows(IllegalStateException.class,
                () -> AsaasInternalApiKey.requireWhenEnabled(properties));

        assertEquals(AsaasInternalApiKey.MISSING_MESSAGE, exception.getMessage());
    }
}
