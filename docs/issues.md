# Backlog de issues do laboratório

Este repositório tem um escopo simples: aprender Java/Spring Boot e integrar
com a API PJ do Banco Inter. CQRS, Event Sourcing e Event-Driven ficam para
projetos separados.

## Java e Spring Boot

- [x] **LAB-001 — Criar setup do server com Java 21 e Spring Boot** — aplicação sobe e responde `/api/health`.
- [x] **LAB-004 — Documentar configuração local e segredos** — `.env.example`, `.gitignore` e instruções de execução.
- [x] **LAB-005 — Criar pipeline mínimo de build e testes do server** — build e testes Java executam no CI.
- [ ] **LAB-006 — Estudar records, sealed interfaces e imutabilidade** — aplicar esses recursos aos tipos do domínio.
- [ ] **LAB-007 — Criar value object Money** — impedir `double`, moeda incompatível e escala inválida.
- [ ] **LAB-008 — Modelar contas do ledger** — conta Inter, contrapartida e tipos contábeis.
- [ ] **LAB-009 — Implementar partidas dobradas** — toda transação exige débitos iguais a créditos.
- [ ] **LAB-010 — Criar aggregate de transação** — invariantes de estado, valor, moeda e referência externa.
- [ ] **LAB-011 — Adicionar testes de domínio** — casos válidos, inválidos e regras financeiras.

## Integração com o Inter PJ

- [ ] **LAB-029 — Implementar cliente OAuth/mTLS do Inter em sandbox** — credenciais fora do Git e token reutilizável.
- [ ] **LAB-030 — Normalizar extrato, webhook e reconciliação** — entradas do Inter chegam ao mesmo caso de uso.

## Próximos estudos, em outros projetos

- CQRS
- Event Sourcing
- Event-Driven e Outbox

Esses temas não fazem parte do backlog deste repositório.
