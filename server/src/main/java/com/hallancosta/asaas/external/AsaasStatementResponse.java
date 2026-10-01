package com.hallancosta.asaas.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AsaasStatementResponse(
        @JsonProperty("data") List<AsaasStatementTransaction> transactions) {
}
