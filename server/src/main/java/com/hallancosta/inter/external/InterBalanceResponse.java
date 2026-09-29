package com.hallancosta.inter.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record InterBalanceResponse(
        @JsonProperty("disponivel") BigDecimal available,
        @JsonProperty("bloqueadoCheque") BigDecimal checkBlocked,
        @JsonProperty("bloqueadoJudicialmente") BigDecimal judiciallyBlocked,
        @JsonProperty("bloqueadoAdministrativo") BigDecimal administrativelyBlocked,
        @JsonProperty("limite") BigDecimal limit) {
}
