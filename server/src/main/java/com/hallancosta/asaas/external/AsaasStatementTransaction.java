package com.hallancosta.asaas.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AsaasStatementTransaction(
        String id,
        String type,
        BigDecimal value,
        String date,
        String description) {
}
