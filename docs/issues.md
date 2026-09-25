# Backlog de issues do laboratório

Não precisamos abrir tudo de uma vez. A recomendação é abrir as issues dos dois primeiros marcos e deixar as demais como backlog. São **30 issues** para cobrir bem Java, Spring, Event Sourcing, Event-Driven, CQRS, infraestrutura e integração.

## Marco 0 — Fundamentos e ambiente

- [ ] **LAB-001 — Criar setup do server com Java 21 e Spring Boot** — aplicação sobe e responde `/api/health`.
- [ ] **LAB-002 — Criar setup do web com React e Vite** — dashboard dark inicia em `localhost:5173`.
- [ ] **LAB-003 — Adicionar PostgreSQL e Redis com Docker Compose** — containers têm health checks e volumes locais.
- [ ] **LAB-004 — Documentar configuração local e segredos** — `.env.example`, `.gitignore` e instruções de execução.
- [ ] **LAB-005 — Criar pipeline mínimo de build e testes** — server e web têm comandos reproduzíveis.

## Marco 1 — Java e domínio financeiro

- [ ] **LAB-006 — Estudar records, sealed interfaces e imutabilidade** — aplicar esses recursos aos eventos e IDs.
- [ ] **LAB-007 — Criar value object Money** — impedir `double`, moeda incompatível e escala inválida.
- [ ] **LAB-008 — Modelar contas do ledger** — conta bancária, contrapartida e tipos contábeis.
- [ ] **LAB-009 — Implementar partidas dobradas** — toda transação exige débitos iguais a créditos.
- [ ] **LAB-010 — Criar aggregate de transação** — invariantes de estado, valor, moeda e referência externa.
- [ ] **LAB-011 — Adicionar testes de domínio** — casos válidos, inválidos, reversão e concorrência.

## Marco 2 — Event Sourcing

- [ ] **LAB-012 — Definir contrato de DomainEvent** — tipo, ID, aggregate, versão e timestamp.
- [ ] **LAB-013 — Persistir Event Store no PostgreSQL** — stream append-only com payload JSONB.
- [ ] **LAB-014 — Implementar optimistic concurrency** — rejeitar gravação com versão desatualizada.
- [ ] **LAB-015 — Implementar snapshot opcional** — comparar replay completo com replay a partir de snapshot.
- [ ] **LAB-016 — Criar replay de projection** — apagar saldo e reconstruí-lo exclusivamente dos eventos.
- [ ] **LAB-017 — Modelar correções e reversões** — nunca apagar evento ou lançamento financeiro.

## Marco 3 — CQRS

- [ ] **LAB-018 — Separar command side e query side** — comandos não leem saldo projetado como fonte de verdade.
- [ ] **LAB-019 — Criar projection de saldo** — atualizar saldo a partir de `LedgerTransactionPosted`.
- [ ] **LAB-020 — Criar projection de extrato** — consulta paginada e ordenada para o web.
- [ ] **LAB-021 — Criar endpoints de leitura** — saldo, transações, eventos e detalhe de uma operação.
- [ ] **LAB-022 — Criar endpoints de comando** — importar transação, reprocessar e solicitar reconciliação.
- [ ] **LAB-023 — Medir consistência eventual** — exibir no web quando projection está atrasada.

## Marco 4 — Event-Driven e confiabilidade

- [ ] **LAB-024 — Substituir Event Bus local por Outbox** — evento e registro de publicação na mesma transação.
- [ ] **LAB-025 — Criar worker assíncrono** — publicar Outbox com retry e backoff.
- [ ] **LAB-026 — Garantir idempotência durável** — constraint única para ID externo e efeitos financeiros.
- [ ] **LAB-027 — Simular crash e reentrega** — provar que o efeito final não duplica dinheiro.
- [ ] **LAB-028 — Comparar Redis Streams e Kafka** — documentar quando cada tecnologia resolve um problema real.

## Marco 5 — Integração com o Inter

- [ ] **LAB-029 — Implementar cliente OAuth/mTLS do Inter em sandbox** — credenciais fora do Git e token reutilizável.
- [ ] **LAB-030 — Normalizar extrato, webhook e reconciliação** — três entradas diferentes chegam ao mesmo caso de uso.

## Issues futuras para aprofundar

Depois das 30 primeiras, ainda podemos abrir issues para:

- rate limit e circuit breaker;
- webhook fora de ordem;
- reconciliação por janela e tolerância;
- múltiplas contas PJ;
- autorização por usuário;
- auditoria e trilha de acesso;
- observabilidade com métricas e tracing;
- Testcontainers para Postgres e Redis;
- property-based testing do ledger;
- contract tests do adapter do Inter;
- dead-letter queue;
- particionamento do Event Store;
- retenção e arquivamento;
- comparação com KurrentDB/EventStoreDB;
- comparação com TigerBeetle;
- containerizar server e web;
- CI/CD e análise de dependências;
- threat modeling e rotação de certificados;
- teste de carga das projections;
- documentação OpenAPI;
- acessibilidade e navegação por teclado no web.
