# Backlog de issues do laboratório

Este backlog foi simplificado. O projeto estuda Java, Spring Boot e integração
com a API Pix do Asaas, usando Sandbox antes de qualquer ambiente real.

CQRS, Event Sourcing e Event-Driven foram removidos deste projeto e devem ser
tratados em repositórios próprios.

## Java e Spring Boot

- [x] **LAB-001 — Criar setup do server com Java 21 e Spring Boot** — aplicação sobe e responde `/api/health` na porta `9090`.
- [x] **LAB-004 — Documentar configuração local e segredos** — `.env.example`, `.gitignore` e script Docker.
- [x] **LAB-005 — Criar pipeline mínimo de build e testes do server** — build e testes Java executam sem Maven instalado na máquina.
- [ ] **LAB-006 — Estudar records e imutabilidade** — entender `record`, construtor compacto e validação.
- [ ] **LAB-007 — Criar value object Money** — impedir `double`, moeda incompatível e escala inválida.
- [x] **LAB-008 — Modelar transação normalizada** — separar o payload do Asaas do modelo da aplicação.
- [ ] **LAB-009 — Criar gateway fake do Asaas** — executar casos de uso sem credencial ou rede.
- [ ] **LAB-010 — Criar caso de uso de importação** — receber movimentações do extrato e preparar a persistência.
- [ ] **LAB-011 — Adicionar testes de integração** — validar controller, HTTP, banco e tratamento de erros.

## Integração com Asaas

- [x] **LAB-029 — Implementar cliente autenticado do Asaas em Sandbox** — usar `RestClient`, header `access_token` e URL configurável.
- [ ] **LAB-030 — Normalizar extrato, cobranças, transferências e webhooks do Asaas** — manter o provedor fora do modelo interno e evitar duplicidade.
- [x] **LAB-031 — Consultar saldo da conta** — integrar `GET /finance/balance` e expor `GET /api/asaas/balance`.
- [x] **LAB-032 — Consultar extrato financeiro** — integrar `GET /financialTransactions` e normalizar lançamentos.
- [x] **LAB-033 — Criar cobrança Pix** — enviar `POST /payments` com `billingType=PIX`.
- [x] **LAB-034 — Consultar QR Code da cobrança** — expor payload Pix e imagem Base64 da cobrança.
- [x] **LAB-035 — Criar transferência Pix** — enviar `POST /transfers` para uma chave de homologação.
- [ ] **LAB-036 — Criar conta e API key do Asaas Sandbox** — configurar `server/.env.local` sem versionar a chave.
- [ ] **LAB-037 — Testar geração de saldo no Sandbox** — criar cliente fictício, cobrança Pix e confirmar recebimento pela interface.
- [ ] **LAB-038 — Mapear erros e idempotência** — estudar `400`, `401`, `403`, `429` e `externalReference`.
- [ ] **LAB-039 — Receber webhook de cobrança** — validar `authToken`, persistir evento e evitar duplicidade.
- [ ] **LAB-040 — Receber webhook de transferência** — acompanhar `PENDING`, `DONE` e `CANCELLED`.
- [ ] **LAB-041 — Persistir extrato no PostgreSQL** — criar migration, tabela e sincronização incremental.
- [ ] **LAB-042 — Usar Redis para idempotência** — impedir processamento duplicado durante a janela de estudo.
- [ ] **LAB-043 — Trocar Sandbox por produção com segurança** — validar `.env.production`, URL e API key separada.
- [ ] **LAB-044 — Cobrir integração com testes HTTP** — simular respostas de sucesso, erro e timeout.

## Frontend

- [x] **LAB-050 — Exibir saldo Asaas no React** — conectar o dashboard ao endpoint interno do server.
- [ ] **LAB-051 — Exibir extrato real** — substituir os dados mockados pelo endpoint de statement.
- [ ] **LAB-052 — Criar tela de cobrança Pix** — informar cliente, valor e vencimento e mostrar QR Code.
- [ ] **LAB-053 — Mostrar status da operação** — diferenciar criada, pendente, recebida, concluída e falha.

## Como uma issue deve ser estudada

Cada issue de integração deve conter:

1. objetivo em uma frase;
2. endpoint oficial do Asaas envolvido;
3. variáveis de ambiente necessárias;
4. teste automatizado sem credencial real;
5. comando `curl` para reproduzir no Sandbox;
6. observação sobre o que é simulação e o que seria produção.

Uma cobrança criada não significa que o pagamento já foi confirmado. Uma
transferência criada também pode permanecer pendente. O status final deve ser
consultado ou recebido por webhook.
