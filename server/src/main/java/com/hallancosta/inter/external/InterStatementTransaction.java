package com.hallancosta.inter.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record InterStatementTransaction(
        @JsonProperty("dataEntrada") String entryDate,
        @JsonProperty("tipoTransacao") String transactionType,
        @JsonProperty("tipoOperacao") String operationType,
        @JsonProperty("valor") String value,
        @JsonProperty("titulo") String title,
        @JsonProperty("descricao") String description) {
}
