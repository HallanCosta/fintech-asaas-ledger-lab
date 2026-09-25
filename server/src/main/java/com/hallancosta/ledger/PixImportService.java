package com.hallancosta.ledger;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Caso de uso de entrada. A API do Inter (webhook ou polling) deve chamar
 * este serviço somente depois de normalizar o payload externo.
 */
public final class PixImportService {
    private final EventStore eventStore;
    private final LocalEventBus eventBus;
    private final Map<ExternalTransactionId, LedgerTransactionId> processed = new ConcurrentHashMap<>();

    public PixImportService(EventStore eventStore, LocalEventBus eventBus) {
        this.eventStore = eventStore;
        this.eventBus = eventBus;
    }

    public synchronized ImportResult importTransaction(InterTransaction external) {
        if (external.direction() != ExternalTransactionDirection.CREDIT) {
            throw new UnsupportedOperationException(
                    "A primeira iteração trata somente PIX recebido; débito será um próximo caso de uso"
            );
        }

        var previousTransaction = processed.get(external.externalId());
        if (previousTransaction != null) {
            return new ImportResult(previousTransaction, true);
        }

        var streamId = "account:" + external.bankAccountId().value();
        var received = new PixReceived(
                UUID.randomUUID(),
                streamId,
                external.externalId(),
                external.amount(),
                external.occurredAt(),
                external.description()
        );
        appendAndPublish(streamId, received);

        var transactionId = LedgerTransactionId.newId();
        var bankSide = external.direction() == ExternalTransactionDirection.CREDIT
                ? EntrySide.DEBIT
                : EntrySide.CREDIT;
        var counterpartySide = bankSide == EntrySide.DEBIT ? EntrySide.CREDIT : EntrySide.DEBIT;
        var entries = List.of(
                new LedgerEntry(transactionId, external.bankAccountId(), bankSide, external.amount(), "Conta Inter"),
                new LedgerEntry(transactionId, external.counterpartyAccountId(), counterpartySide, external.amount(), "Contrapartida")
        );
        var ledgerTransaction = new LedgerTransaction(
                transactionId,
                Instant.now(),
                external.externalId().value(),
                entries
        );
        var posted = new LedgerTransactionPosted(UUID.randomUUID(), streamId, ledgerTransaction, Instant.now());
        appendAndPublish(streamId, posted);
        processed.put(external.externalId(), transactionId);

        return new ImportResult(transactionId, false);
    }

    private void appendAndPublish(String streamId, DomainEvent event) {
        var currentVersion = eventStore.load(streamId).size();
        eventStore.append(streamId, List.of(event), currentVersion);
        eventBus.publish(event);
    }
}
