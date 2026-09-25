# Fintech / Ledger Lab com API PJ do Inter

## Objetivo

Criar um projeto de estudo separado do **Lisboon**, focado em
arquitetura financeira e sistemas orientados a eventos.

A ideia é construir:

> **Um ledger financeiro append-only que sincroniza movimentações de uma
> conta PJ do Inter, registra lançamentos em partidas dobradas, mantém
> projeções de saldo e consegue reconciliar seu estado com o banco.**

O projeto serve como laboratório para estudar:

-   Event Sourcing
-   Event-Driven Architecture
-   CQRS
-   Append-only storage
-   Double-entry accounting
-   Ledger financeiro
-   Idempotência
-   Concorrência
-   Webhooks
-   Integrações bancárias
-   Reconciliação
-   Sistemas distribuídos

------------------------------------------------------------------------

## Princípio importante

O **Banco Inter não será o ledger do sistema**.

O Inter será uma **fonte externa de informações e eventos financeiros**.
O sistema manterá seu próprio histórico append-only e seu próprio modelo
financeiro.

``` text
              API Banco Inter
                    │
                    ▼
            Inter Integration
                    │
        ┌───────────┴───────────┐
        │                       │
   Webhook recebido        Polling/Sync
        │                       │
        └───────────┬───────────┘
                    ▼
              Normalização
                    │
                    ▼
               Commands
                    │
                    ▼
             Domain / Ledger
                    │
                    ▼
              Event Store
             (append-only)
                    │
          ┌─────────┴─────────┐
          ▼                   ▼
     Projections          Event Bus
          │                   │
          ▼                   ├── Notification
      PostgreSQL              ├── Analytics
                              └── etc.
```

------------------------------------------------------------------------

## Exemplo: PIX recebido

Um PIX recebido pelo Inter pode entrar no sistema através de webhook ou
sincronização.

O fluxo conceitual poderia ser:

``` text
Inter webhook
    ↓
PixReceived
    ↓
ProcessIncomingPix
    ↓
LedgerTransactionCreated
    ↓
LedgerEntryPosted
```

O sistema não simplesmente atualiza um campo `balance`.

Ele registra fatos que aconteceram.

Por exemplo:

``` text
Event Stream: account:123

1 AccountCreated
2 ExternalBankAccountLinked
3 PixReceived
4 LedgerTransactionCreated
5 LedgerEntryPosted
6 PixReceived
7 LedgerTransactionCreated
8 LedgerEntryPosted
```

O histórico é preservado e novos acontecimentos são adicionados ao
final.

------------------------------------------------------------------------

## Problemas interessantes para estudar

O valor desse projeto aparece principalmente nos problemas reais de
integração financeira.

### Webhooks duplicados

O Inter pode eventualmente entregar novamente uma notificação.

O sistema precisa reconhecer que aquela operação já foi processada e
impedir que o mesmo dinheiro seja registrado duas vezes.

Isso leva ao estudo de:

-   idempotency keys
-   external transaction IDs
-   deduplicação
-   exactly-once effects

### Eventos fora de ordem

Um evento pode chegar depois de outro que logicamente deveria ter
ocorrido posteriormente.

Isso força o sistema a pensar em:

-   ordenação
-   timestamps
-   sequence numbers
-   estado intermediário
-   eventual consistency

### Reprocessamento

Como os eventos são preservados, deve ser possível reconstruir
determinadas projeções.

``` text
Event Store
    ↓
Replay
    ↓
Projection
    ↓
Account Balance
```

Isso permite, por exemplo, apagar uma projeção de saldo e reconstruí-la
a partir do histórico.

### Reconciliação

O sistema pode comparar seu próprio ledger com o extrato obtido do Banco
Inter.

``` text
Nosso Ledger
     │
     ├─────── Reconciliation ───────┐
     │                              │
     ▼                              ▼
Expected Transactions        Inter Statement
```

Diferenças podem gerar eventos ou alertas como:

``` text
ReconciliationStarted
TransactionMatched
TransactionMissing
UnexpectedExternalTransaction
ReconciliationCompleted
```

### Falhas parciais

Também é interessante simular situações como:

``` text
PIX recebido
    ↓
evento salvo
    ↓
processamento iniciado
    ↓
CRASH
```

Depois investigar como continuar o processamento sem duplicar operações.

### Reversões

Em um ledger append-only, uma operação incorreta não deve simplesmente
desaparecer.

Em vez de:

``` sql
DELETE FROM ledger_entries
WHERE id = ?;
```

a ideia é registrar uma operação compensatória/reversão.

``` text
Original Transaction
        ↓
Reversal Requested
        ↓
Reversal Transaction
        ↓
Compensating Entries
```

Assim, o histórico permanece auditável.

------------------------------------------------------------------------

## Começar como monólito modular

Não é necessário começar com microserviços, Kafka e vários bancos.

Uma primeira versão pode ser um **monólito modular**.

``` text
modules/
  banking/
  ledger/
  transactions/
  reconciliation/
  inter-integration/
  event-store/
```

Cada módulo possui responsabilidade clara, mas tudo inicialmente pode
executar dentro da mesma aplicação.

Isso permite estudar os conceitos sem adicionar complexidade operacional
cedo demais.

------------------------------------------------------------------------

## Stack inicial

Uma V1 pode usar:

``` text
Java
Spring Boot
PostgreSQL
Docker
API PJ Banco Inter
```

O próprio PostgreSQL pode inicialmente funcionar como:

-   banco operacional;
-   armazenamento append-only;
-   event store experimental;
-   armazenamento das projections;
-   armazenamento do ledger.

Mais tarde, conforme os problemas aparecerem, é possível experimentar
tecnologias especializadas.

``` text
PostgreSQL
    ↓
KurrentDB / EventStoreDB
    ↓
Kafka ou NATS
    ↓
TigerBeetle
```

A ideia não é substituir obrigatoriamente PostgreSQL, mas entender
**qual problema cada tecnologia resolve**.

------------------------------------------------------------------------

## Ledger com partidas dobradas

O projeto também deve servir para estudar **double-entry accounting**.

Em vez de pensar apenas:

``` text
balance += 100
```

uma movimentação financeira gera lançamentos correspondentes.

Conceitualmente:

``` text
Transaction
    │
    ├── Entry A
    │
    └── Entry B
```

E o sistema mantém uma propriedade contábil equivalente a:

``` text
débitos = créditos
```

Isso leva naturalmente a conceitos como:

``` text
Ledger
Journal
Accounts
Entries
Transactions
Transfers
Debit
Credit
Pending
Posted
Reversal
```

------------------------------------------------------------------------

## Event Store e Ledger não precisam ser a mesma coisa

Uma distinção importante durante o estudo é entender que:

``` text
Event Store ≠ Ledger
```

O Event Store registra acontecimentos do domínio.

``` text
PixReceived
TransferRequested
TransferApproved
ReconciliationCompleted
```

O Ledger registra os efeitos financeiros relevantes.

``` text
Transaction
    ├── Debit Entry
    └── Credit Entry
```

Alguns eventos podem gerar lançamentos no ledger, enquanto outros não
têm efeito financeiro.

Por exemplo:

``` text
ExternalBankAccountLinked
```

pode ser um evento importante do domínio sem representar movimentação de
dinheiro.

------------------------------------------------------------------------

## Evolução sugerida

### V1 --- Ledger básico

Construir:

-   contas
-   transactions
-   ledger entries
-   partidas dobradas
-   saldo derivado dos lançamentos
-   armazenamento append-only

Sem integração com banco.

### V2 --- Event Store

Adicionar eventos como:

``` text
AccountCreated
TransactionCreated
LedgerEntryPosted
TransactionReversed
```

Implementar replay e reconstrução de estado.

### V3 --- Projections / CQRS

Criar read models como:

``` text
account_balance
transaction_history
daily_cash_flow
```

O objetivo é conseguir destruir uma projection e reconstruí-la usando os
eventos.

### V4 --- Integração com Banco Inter

Criar o módulo:

``` text
inter-integration/
```

Responsável por:

-   autenticação
-   consulta de movimentações
-   recebimento de webhooks
-   normalização dos dados externos
-   deduplicação

### V5 --- Idempotência

Garantir que:

``` text
mesmo webhook recebido 10 vezes
```

produza:

``` text
1 movimentação financeira
```

### V6 --- Reconciliação

Comparar:

``` text
Ledger interno
      VS
Extrato Banco Inter
```

Detectar divergências e registrar os resultados.

### V7 --- Mensageria

Somente depois introduzir algo como:

``` text
Kafka
```

ou:

``` text
NATS
```

para experimentar processamento assíncrono.

### V8 --- Bancos especializados

Experimentar:

``` text
KurrentDB
```

para Event Sourcing e:

``` text
TigerBeetle
```

para ledger/transações financeiras.

Comparar essas soluções com a implementação original em PostgreSQL.

------------------------------------------------------------------------

## Estrutura possível do projeto

``` text
fintech-lab/
│
├── banking/
│   ├── account/
│   └── external-account/
│
├── ledger/
│   ├── account/
│   ├── transaction/
│   ├── entry/
│   └── balance/
│
├── transactions/
│   ├── pix/
│   └── transfer/
│
├── reconciliation/
│
├── integrations/
│   └── inter/
│
├── event-store/
│
├── projections/
│
└── infrastructure/
```

Os limites exatos devem evoluir durante o estudo. O objetivo não é
acertar a arquitetura definitiva antes de escrever código.

------------------------------------------------------------------------

## Filosofia do projeto

Esse projeto **não é o Lisboon**.

Ele deve ser tratado como laboratório.

Isso significa que é aceitável:

-   quebrar arquitetura;
-   reescrever módulos;
-   experimentar padrões;
-   implementar duas soluções para o mesmo problema;
-   trocar tecnologias;
-   gerar eventos artificiais;
-   simular falhas;
-   apagar projections;
-   fazer replay;
-   testar condições de corrida.

As decisões arquiteturais podem ser tomadas pelo **valor de
aprendizado**, e não apenas pelo caminho mais curto para colocar um
produto em produção.

------------------------------------------------------------------------

## Resultado esperado

Ao final, o sistema idealmente será capaz de:

``` text
Banco Inter
     ↓
Webhook / Sync
     ↓
Deduplicação
     ↓
Domain
     ↓
Event Store
     ↓
Ledger
     ↓
Double-entry entries
     ↓
Projections
     ↓
Saldo / Extrato
     ↓
Reconciliation
     ↓
Comparação com Banco Inter
```

Mais importante que o produto final será entender **por que cada
componente existe**.

O projeto deve permitir responder, na prática:

-   Por que usar append-only?
-   Qual a diferença entre Event Store e Ledger?
-   Quando CQRS é útil?
-   Como reconstruir estado?
-   Como lidar com webhook duplicado?
-   Como garantir idempotência?
-   Como representar dinheiro corretamente?
-   Como registrar reversões sem destruir histórico?
-   Como reconciliar um ledger interno com um banco externo?
-   Onde Kafka/NATS realmente entram?
-   Quando PostgreSQL é suficiente?
-   O que KurrentDB resolve?
-   O que TigerBeetle resolve?
-   Quais problemas aparecem quando o sistema passa a ser distribuído?

---

## Organização do repositório: web + server

Para este projeto, a ideia é manter **web e server no mesmo repositório**.

Como o `fintech-inter-ledger-lab` é um laboratório de estudo focado em ledger, Event Sourcing e integração com a API PJ do Banco Inter, separar web e server em repositórios diferentes adicionaria gerenciamento sem trazer muito benefício neste momento.

Um monorepo simples também facilita:

- subir todo o ambiente com Docker Compose;
- documentar o projeto em um único README;
- versionar mudanças de web e server juntas quando necessário;
- executar o laboratório localmente;
- manter exemplos e documentação próximos do código.

### Nome do repositório

```text
fintech-inter-ledger-lab
```

O nome deixa explícito o escopo do projeto:

```text
fintech  → contexto/domínio
inter    → integração específica
ledger   → principal objeto de estudo
lab      → projeto experimental/de estudo
```

### Estrutura inicial

```text
fintech-inter-ledger-lab/
│
├── server/
│   ├── src/
│   ├── pom.xml
│   └── Dockerfile
│
├── web/
│   ├── src/
│   ├── package.json
│   └── Dockerfile
│
├── docs/
│   ├── architecture.md
│   ├── ledger.md
│   └── event-sourcing.md
│
├── infra/
│   └── ...
│
├── docker-compose.yml
├── .env.example
├── .gitignore
└── README.md
```

Inicialmente, não é necessário introduzir ferramentas de monorepo como Nx ou Turborepo, nem transformar o server em um Maven multi-module sem necessidade.

O repositório pode simplesmente conter dois projetos independentes:

```text
/server    → Spring Boot
/web       → React / Vite
```

Cada aplicação mantém seu próprio processo de build.

### Papel do web

O web não precisa ser o foco principal nem tentar reproduzir a experiência visual de uma fintech completa.

Ele pode funcionar principalmente como uma **ferramenta de inspeção do laboratório**, permitindo visualizar o que acontece dentro da arquitetura.

Uma interface inicial pode apresentar:

```text
Dashboard
 ├── Saldo
 ├── Contas
 ├── Transações
 ├── Ledger Entries
 ├── Eventos
 └── Reconciliação
```

Isso permite acompanhar visualmente fluxos como:

```text
Transferência R$ 100
        │
        ▼
Transaction
        │
        ├── Debit Entry   -R$100
        └── Credit Entry  +R$100
        │
        ▼
Domain Events
        │
        ├── TransferCreated
        └── LedgerEntryPosted
        │
        ▼
Projection
        │
        ▼
Saldo atualizado
```

Dessa forma, o web ajuda a enxergar conceitos que normalmente ficariam escondidos no server, como:

- eventos gerados;
- lançamentos do ledger;
- partidas dobradas;
- projections;
- estado de uma transação;
- reconciliação com o Banco Inter;
- replay de eventos;
- divergências encontradas durante a reconciliação.

### Quando faria sentido separar os repositórios?

Web e server podem virar repositórios independentes no futuro caso apareçam necessidades concretas, como:

- ciclos de release independentes;
- equipes diferentes;
- permissões diferentes;
- pipelines de deploy completamente independentes;
- crescimento significativo de cada aplicação.

Até que exista uma dessas necessidades, manter tudo no mesmo repositório reduz complexidade e combina melhor com a proposta de laboratório do projeto.
