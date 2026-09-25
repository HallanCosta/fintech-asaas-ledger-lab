package com.hallancosta.ledger;

import java.util.List;

public interface EventStore {
    void append(String streamId, List<DomainEvent> events, long expectedVersion);

    List<DomainEvent> load(String streamId);

    List<DomainEvent> loadAll();
}
