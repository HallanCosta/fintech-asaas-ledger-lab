package com.hallancosta.ledger;

import java.util.Objects;

public record LedgerAccountId(String value) {
    public LedgerAccountId {
        Objects.requireNonNull(value, "value");
        if (value.isBlank()) {
            throw new IllegalArgumentException("A conta do ledger não pode ser vazia");
        }
    }
}
