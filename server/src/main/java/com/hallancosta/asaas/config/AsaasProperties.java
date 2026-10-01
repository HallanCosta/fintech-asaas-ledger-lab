package com.hallancosta.asaas.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Configuração da API Asaas; a chave nunca deve ser versionada. */
@ConfigurationProperties(prefix = "asaas")
public class AsaasProperties {

    private boolean enabled;
    private String environment = "SANDBOX";
    private String baseUrl = "https://api-sandbox.asaas.com/v3";
    private String apiKey;
    private String accountId;
    private String webhookAuthToken;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getWebhookAuthToken() {
        return webhookAuthToken;
    }

    public void setWebhookAuthToken(String webhookAuthToken) {
        this.webhookAuthToken = webhookAuthToken;
    }
}
