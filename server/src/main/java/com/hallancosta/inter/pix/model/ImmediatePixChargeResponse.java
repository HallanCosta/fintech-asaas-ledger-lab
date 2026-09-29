package com.hallancosta.inter.pix.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ImmediatePixChargeResponse(
        String txid,
        String status,
        @JsonProperty("pixCopiaECola") String pixCopyAndPaste,
        String location,
        PixValue valor) {
}
