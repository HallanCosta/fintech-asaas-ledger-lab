package com.hallancosta.ledger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Barramento síncrono para visualizar Event-Driven sem introduzir Kafka ainda.
 */
public final class LocalEventBus {
    private final List<Consumer<DomainEvent>> subscribers = new ArrayList<>();

    public void subscribe(Consumer<DomainEvent> subscriber) {
        subscribers.add(subscriber);
    }

    public void publish(DomainEvent event) {
        subscribers.forEach(subscriber -> subscriber.accept(event));
    }
}
