package com.hallancosta.ledger;

import java.time.Instant;
import java.util.UUID;

public sealed interface DomainEvent permits PixReceived, LedgerTransactionPosted {
    UUID eventId();

    String aggregateId();

    Instant occurredAt();

    default String type() {
        return getClass().getSimpleName();
    }
}
