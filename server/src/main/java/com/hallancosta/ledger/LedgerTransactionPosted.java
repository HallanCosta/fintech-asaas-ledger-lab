package com.hallancosta.ledger;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record LedgerTransactionPosted(
        UUID eventId,
        String aggregateId,
        LedgerTransaction transaction,
        Instant occurredAt
) implements DomainEvent {
    public LedgerTransactionPosted {
        Objects.requireNonNull(eventId, "eventId");
        Objects.requireNonNull(aggregateId, "aggregateId");
        Objects.requireNonNull(transaction, "transaction");
        Objects.requireNonNull(occurredAt, "occurredAt");
    }
}
