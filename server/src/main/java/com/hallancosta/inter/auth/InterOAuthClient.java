package com.hallancosta.inter.auth;

import com.hallancosta.inter.config.InterProperties;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

/** Cliente OAuth2 client-credentials utilizado pelas APIs do Inter. */
public class InterOAuthClient {

    private final RestClient restClient;
    private final InterProperties properties;
    private final Clock clock;
    private CachedToken cachedToken;

    public InterOAuthClient(RestClient restClient, InterProperties properties, Clock clock) {
        this.restClient = Objects.requireNonNull(restClient);
        this.properties = Objects.requireNonNull(properties);
        this.clock = Objects.requireNonNull(clock);
    }

    public synchronized String accessToken() {
        Instant now = Instant.now(clock);
        if (cachedToken != null && now.isBefore(cachedToken.expiresAt())) {
            return cachedToken.value();
        }

        String clientId = required(properties.getClientId(), "inter.client-id");
        String clientSecret = required(properties.getClientSecret(), "inter.client-secret");

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        if (properties.getScope() != null && !properties.getScope().isBlank()) {
            form.add("scope", properties.getScope());
        }

        InterAccessToken token = restClient.post()
                .uri(properties.getTokenPath())
                .headers(headers -> headers.setBasicAuth(clientId, clientSecret))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(InterAccessToken.class);

        if (token == null || token.accessToken() == null || token.accessToken().isBlank()) {
            throw new IllegalStateException("O Inter retornou um token OAuth2 vazio");
        }

        long lifetime = token.expiresIn() > 0 ? token.expiresIn() : 3600;
        // Renova 30 segundos antes da expiração para evitar chamadas no limite.
        Instant expiresAt = now.plusSeconds(Math.max(1, lifetime - 30));
        cachedToken = new CachedToken(token.accessToken(), expiresAt);
        return token.accessToken();
    }

    private static String required(String value, String propertyName) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Configure a propriedade " + propertyName);
        }
        return value;
    }

    private record CachedToken(String value, Instant expiresAt) {
    }
}
