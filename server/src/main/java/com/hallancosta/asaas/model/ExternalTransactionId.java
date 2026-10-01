package com.hallancosta.asaas.model;

import java.util.Objects;

public record ExternalTransactionId(String value) {
    public ExternalTransactionId {
        Objects.requireNonNull(value, "value");
        if (value.isBlank()) {
            throw new IllegalArgumentException("O identificador externo não pode ser vazio");
        }
    }
}
