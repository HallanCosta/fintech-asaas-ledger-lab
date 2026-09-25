package com.hallancosta.ledger;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record PixReceived(
        UUID eventId,
        String aggregateId,
        ExternalTransactionId externalTransactionId,
        Money amount,
        Instant occurredAt,
        String description
) implements DomainEvent {
    public PixReceived {
        Objects.requireNonNull(eventId, "eventId");
        Objects.requireNonNull(aggregateId, "aggregateId");
        Objects.requireNonNull(externalTransactionId, "externalTransactionId");
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(occurredAt, "occurredAt");
        Objects.requireNonNull(description, "description");
        if (!amount.isPositive()) {
            throw new IllegalArgumentException("Pix recebido precisa ter valor positivo");
        }
    }
}
