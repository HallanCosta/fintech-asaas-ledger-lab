package com.hallancosta.inter.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record InterBankStatementResponse(
        @JsonProperty("transacoes") List<InterStatementTransaction> transactions) {
}
