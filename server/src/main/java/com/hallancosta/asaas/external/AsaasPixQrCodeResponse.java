package com.hallancosta.asaas.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AsaasPixQrCodeResponse(
        String encodedImage,
        String payload,
        String expirationDate) {
}
