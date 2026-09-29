package com.hallancosta.inter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuração da integração com a API PJ do Inter.
 *
 * <p>Os valores sensíveis chegam por variáveis de ambiente. A integração fica
 * desligada por padrão para que o projeto continue iniciando sem certificado
 * ou credenciais durante o estudo.</p>
 */
@ConfigurationProperties(prefix = "inter")
public class InterProperties {

    private boolean enabled;
    private String environment = "SANDBOX";
    private String baseUrl = "https://cdpj-sandbox.partners.uatinter.co";
    private String tokenPath = "/oauth/v2/token";
    private String clientId;
    private String clientSecret;
    private String certificatePath;
    private String certificatePassword;
    private String accountId;
    private String scope = "extrato.read";

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

    public String getTokenPath() {
        return tokenPath;
    }

    public void setTokenPath(String tokenPath) {
        this.tokenPath = tokenPath;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getCertificatePath() {
        return certificatePath;
    }

    public void setCertificatePath(String certificatePath) {
        this.certificatePath = certificatePath;
    }

    public String getCertificatePassword() {
        return certificatePassword;
    }

    public void setCertificatePassword(String certificatePassword) {
        this.certificatePassword = certificatePassword;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}
