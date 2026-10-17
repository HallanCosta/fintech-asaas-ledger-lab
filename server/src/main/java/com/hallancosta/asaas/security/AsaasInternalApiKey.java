package com.hallancosta.asaas.security;

import com.hallancosta.asaas.config.AsaasProperties;
import org.springframework.core.env.Environment;

/** Impede a integração de subir sem um segredo interno distinto da chave do Asaas. */
public final class AsaasInternalApiKey {

    static final String MISSING_MESSAGE =
            "Configure a propriedade asaas.internal-api-key para habilitar a integração com o Asaas";
    static final String SAME_AS_PROVIDER_MESSAGE =
            "A propriedade asaas.internal-api-key deve ser diferente de asaas.api-key";

    private AsaasInternalApiKey() {
    }

    public static void requireWhenEnabled(Environment environment) {
        boolean enabled = Boolean.TRUE.equals(environment.getProperty("asaas.enabled", Boolean.class, false));
        requireWhenEnabled(
                enabled,
                environment.getProperty("asaas.internal-api-key"),
                environment.getProperty("asaas.api-key"));
    }

    public static void requireWhenEnabled(AsaasProperties properties) {
        requireWhenEnabled(properties.isEnabled(), properties.getInternalApiKey(), properties.getApiKey());
    }

    static void requireWhenEnabled(boolean enabled, String internalApiKey, String asaasApiKey) {
        if (!enabled) {
            return;
        }
        if (internalApiKey == null || internalApiKey.isBlank()) {
            throw new IllegalStateException(MISSING_MESSAGE);
        }
        if (asaasApiKey != null && internalApiKey.equals(asaasApiKey)) {
            throw new IllegalStateException(SAME_AS_PROVIDER_MESSAGE);
        }
    }
}
