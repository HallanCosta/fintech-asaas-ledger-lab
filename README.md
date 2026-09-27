# Fintech Inter Ledger Lab

Laboratório de estudo em Java 21 e Spring Boot para aprender fundamentos de Java e integrar com a API PJ do Banco Inter.

O repositório agora é organizado como um monorepo simples:

```text
server/  → Java 21 + Spring Boot
docs/    → integração, arquitetura simples e backlog de estudo
web/     → protótipo visual mantido fora do escopo atual
```

## Primeiro experimento

O ponto de partida é um server Java 21 com Spring Boot e um domínio financeiro pequeno para estudar:

- `Money` como value object;
- `LedgerTransaction` com partidas dobradas;
- normalização de transações externas;
- contrato de gateway para a integração com o Inter;
- testes de regras financeiras.

O próximo passo é implementar o cliente autenticado da API PJ do Inter em sandbox.

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

## Validação local

O pipeline do GitHub Actions executa os comandos essenciais do server:

```bash
docker compose config --quiet

cd server
mvn --batch-mode verify
```

O CI usa Java 21 e valida a configuração do Docker Compose. O runner hospedado
do GitHub já fornece Maven; localmente,
instale o Maven 3.9+ ou execute o comando dentro de uma imagem Maven compatível.

## Próximos cortes

1. estudar os recursos modernos de Java no domínio;
2. implementar o cliente OAuth/mTLS da API PJ do Inter;
3. normalizar extrato, webhook e reconciliação;
4. adicionar testes de integração contra o sandbox.

Veja [docs/architecture.md](docs/architecture.md), [docs/inter-integration.md](docs/inter-integration.md), [docs/stack.md](docs/stack.md) e [docs/issues.md](docs/issues.md).
