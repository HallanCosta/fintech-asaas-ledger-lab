# Fintech Inter Ledger Lab

Laboratório de estudo em Java sobre integração bancária, ledger financeiro, partidas dobradas, CQRS, Event Sourcing e arquitetura orientada a eventos.

O repositório agora é organizado como um monorepo simples:

```text
server/  → Java 21 + Spring Boot
web/     → React + Vite
infra/   → reservado para scripts de infraestrutura
docs/    → arquitetura e backlog de estudo
```

## Primeiro experimento

O núcleo atual é Java 21 puro e não chama o Banco Inter ainda. Ele simula a entrada de um PIX normalizado e demonstra:

- `LedgerTransaction` balanceada em partidas dobradas;
- `PixReceived` e `LedgerTransactionPosted` como eventos imutáveis;
- Event Store append-only em memória;
- projeção de saldo via Event Bus;
- replay da projeção;
- deduplicação por identificador externo.

Esta primeira iteração aceita somente PIX recebido. O caminho de débito será implementado depois como outro caso de uso, com seus próprios eventos e regras.

Subir os serviços locais:

```bash
docker compose up -d
```

Iniciar o servidor Java:

```bash
cd server
mvn spring-boot:run
```

O endpoint inicial é `http://localhost:8080/api/health`.

Os testes de aprendizado podem ser executados com:

```bash
cd server
mvn test
```

Iniciar o frontend React:

```bash
cd web
npm install
npm run dev
```

## Próximos cortes

1. persistir o Event Store no PostgreSQL;
2. criar projections de extrato e reconciliação;
3. implementar o adapter OAuth/mTLS da API PJ do Inter;
4. receber webhook e fazer polling pelo mesmo caso de uso;
5. adicionar Outbox antes de experimentar Kafka ou NATS.

Veja [docs/architecture.md](docs/architecture.md), [docs/event-sourcing.md](docs/event-sourcing.md), [docs/stack.md](docs/stack.md) e [docs/issues.md](docs/issues.md).
