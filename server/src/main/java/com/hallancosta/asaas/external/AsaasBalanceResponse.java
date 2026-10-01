package com.hallancosta.asaas.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AsaasBalanceResponse(BigDecimal balance) {
}
