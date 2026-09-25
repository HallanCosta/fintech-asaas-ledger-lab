package com.hallancosta.ledger;

import java.util.UUID;

public record LedgerTransactionId(UUID value) {
    public static LedgerTransactionId newId() {
        return new LedgerTransactionId(UUID.randomUUID());
    }
}
