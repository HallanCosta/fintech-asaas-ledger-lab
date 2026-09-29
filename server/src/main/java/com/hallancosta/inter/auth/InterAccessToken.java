package com.hallancosta.inter.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

public record InterAccessToken(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("token_type") String tokenType,
        @JsonProperty("expires_in") long expiresIn) {
}
