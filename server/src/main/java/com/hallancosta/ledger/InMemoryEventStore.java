package com.hallancosta.ledger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Event Store didático. A versão PostgreSQL deverá trocar apenas esta porta.
 */
public final class InMemoryEventStore implements EventStore {
    private final Map<String, List<DomainEvent>> streams = new HashMap<>();
    private final List<DomainEvent> globalOrder = new ArrayList<>();

    @Override
    public synchronized void append(String streamId, List<DomainEvent> events, long expectedVersion) {
        var stream = streams.computeIfAbsent(streamId, ignored -> new ArrayList<>());
        if (stream.size() != expectedVersion) {
            throw new OptimisticConcurrencyException(streamId, expectedVersion, stream.size());
        }
        stream.addAll(events);
        globalOrder.addAll(events);
    }

    @Override
    public synchronized List<DomainEvent> load(String streamId) {
        return List.copyOf(streams.getOrDefault(streamId, List.of()));
    }

    @Override
    public synchronized List<DomainEvent> loadAll() {
        return List.copyOf(globalOrder);
    }

    public static final class OptimisticConcurrencyException extends RuntimeException {
        public OptimisticConcurrencyException(String streamId, long expected, long actual) {
            super("Versão inesperada no stream " + streamId + ": esperada=" + expected + ", atual=" + actual);
        }
    }
}
