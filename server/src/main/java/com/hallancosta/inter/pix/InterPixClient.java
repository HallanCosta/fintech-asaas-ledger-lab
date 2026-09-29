package com.hallancosta.inter.pix;

import com.hallancosta.inter.auth.InterOAuthClient;
import com.hallancosta.inter.config.InterProperties;
import com.hallancosta.inter.pix.model.ImmediatePixChargeRequest;
import com.hallancosta.inter.pix.model.ImmediatePixChargeResponse;
import java.util.Objects;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

/** Cliente pequeno para estudar a API Pix sem esconder o HTTP atrás de um SDK. */
public class InterPixClient {

    private static final String IMMEDIATE_BILLING_PATH = "/pix/v2/cob";

    private final RestClient restClient;
    private final InterOAuthClient oauthClient;
    private final InterProperties properties;

    public InterPixClient(
            RestClient restClient,
            InterOAuthClient oauthClient,
            InterProperties properties) {
        this.restClient = Objects.requireNonNull(restClient);
        this.oauthClient = Objects.requireNonNull(oauthClient);
        this.properties = Objects.requireNonNull(properties);
    }

    public ImmediatePixChargeResponse createImmediateCharge(ImmediatePixChargeRequest request) {
        Objects.requireNonNull(request, "request");
        String path = request.txid() == null || request.txid().isBlank()
                ? IMMEDIATE_BILLING_PATH
                : IMMEDIATE_BILLING_PATH + "/" + request.txid();

        return restClient.method(request.txid() == null || request.txid().isBlank()
                        ? org.springframework.http.HttpMethod.POST
                        : org.springframework.http.HttpMethod.PUT)
                .uri(path)
                .headers(headers -> {
                    headers.setBearerAuth(oauthClient.accessToken());
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    if (properties.getAccountId() != null && !properties.getAccountId().isBlank()) {
                        headers.set("x-conta-corrente", properties.getAccountId());
                    }
                })
                .body(request)
                .retrieve()
                .body(ImmediatePixChargeResponse.class);
    }

    public ImmediatePixChargeResponse getImmediateCharge(String txid) {
        if (txid == null || txid.isBlank()) {
            throw new IllegalArgumentException("txid é obrigatório");
        }
        return restClient.get()
                .uri(IMMEDIATE_BILLING_PATH + "/" + txid)
                .headers(headers -> {
                    headers.setBearerAuth(oauthClient.accessToken());
                    if (properties.getAccountId() != null && !properties.getAccountId().isBlank()) {
                        headers.set("x-conta-corrente", properties.getAccountId());
                    }
                })
                .retrieve()
                .body(ImmediatePixChargeResponse.class);
    }
}
