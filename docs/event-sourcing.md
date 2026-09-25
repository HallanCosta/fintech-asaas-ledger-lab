# Event Sourcing em linguagem simples

Event Sourcing não é simplesmente usar uma tabela com nome `events`. A ideia é:

1. o histórico imutável de fatos é a fonte de verdade;
2. o estado atual é derivado desses fatos;
3. uma nova leitura pode ser reconstruída fazendo replay;
4. correções são novos fatos, não `UPDATE` ou `DELETE` do passado.

Neste laboratório:

```text
InterTransaction
  -> PixReceived
  -> LedgerTransactionPosted
  -> BalanceProjection
```

O primeiro evento registra que uma movimentação externa foi observada. O segundo registra o efeito contábil. A projeção de saldo interpreta os lançamentos: para uma conta de ativo, débito aumenta o saldo e crédito reduz.

## O que não devemos misturar

O Event Store responde: “quais fatos aconteceram?”.

O Ledger responde: “quais lançamentos contábeis representam os efeitos financeiros?”.

Uma reconciliação concluída pode ser um evento sem lançamento financeiro. Da mesma forma, um `LedgerTransactionPosted` é um evento de domínio que contém uma transação contábil, mas as tabelas de ledger devem manter suas próprias constraints e índices.

## Por que começar em memória?

Porque o objetivo inicial é enxergar:

- append-only;
- versão do stream e concorrência otimista;
- replay;
- projeção;
- idempotência;
- diferença entre comando, evento e consulta.

Depois persistimos o mesmo contrato em PostgreSQL. Só então vale experimentar EventStoreDB/KurrentDB ou um broker.
