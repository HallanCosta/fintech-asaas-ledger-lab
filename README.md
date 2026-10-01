# Fintech Pix Lab

Laboratório de estudo em Java 21, Spring Boot e integração com a API Pix do
Asaas. O foco é aprender HTTP, testes, configuração e integrações financeiras.

CQRS, Event Sourcing e Event-Driven ficam fora deste projeto e serão estudados
em repositórios separados.

## Estrutura

```text
server/  Java 21 + Spring Boot + cliente Asaas
web/     dashboard React
docs/    arquitetura, integração e backlog
```

## Executar

O Maven roda dentro do Docker:

```bash
./scripts/run-server.sh
```

O comando usa `server/.env.local` e inicia o server na porta `9090`. Para usar
produção explicitamente:

```bash
./scripts/run-server.sh production
```

Verifique se o server está funcionando:

```bash
curl http://localhost:9090/api/health
curl http://localhost:9090/actuator/health
```

## Configuração do Asaas

Crie uma API key no [Sandbox do Asaas](https://sandbox.asaas.com/) e configure
`server/.env.local`:

```dotenv
SERVER_PORT=9090
ASAAS_ENABLED=true
ASAAS_ENVIRONMENT=SANDBOX
ASAAS_BASE_URL=https://api-sandbox.asaas.com/v3
ASAAS_API_KEY=sua-chave
```

A chave é enviada no header `access_token`. Nunca versione arquivos `.env`.
Sandbox e produção usam contas e chaves separadas.

## Endpoints

Com `ASAAS_ENABLED=true`:

```text
GET  /api/asaas/balance
GET  /api/asaas/statement?from=2026-09-29&to=2026-09-29
POST /api/asaas/payments/pix
GET  /api/asaas/payments/{paymentId}/pix-qrcode
POST /api/asaas/transfers/pix
```

Exemplo de cobrança Pix:

```bash
curl -X POST http://localhost:9090/api/asaas/payments/pix \
  -H 'Content-Type: application/json' \
  -d '{
    "customer": "cus_exemplo",
    "value": 10.00,
    "dueDate": "2026-10-01",
    "description": "Cobrança de estudo"
  }'
```

## Testes

```bash
docker run --rm -it \
  --user "$(id -u):$(id -g)" \
  -e MAVEN_CONFIG=/tmp/maven \
  -v "$PWD/server:/workspace" \
  -w /workspace \
  maven:3.9-eclipse-temurin-21 \
  mvn verify
```

Os testes não usam credenciais nem fazem chamadas reais ao Asaas.

## Documentação

- [Integração com Asaas](docs/asaas-integration.md)
- [Arquitetura](docs/architecture.md)
- [Stack](docs/stack.md)
- [Backlog de estudo](docs/issues.md)
- [Documentação oficial do Asaas](https://docs.asaas.com/docs/visao-geral)
