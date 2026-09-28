# Fintech Inter Ledger Lab

Laboratório de estudo em Java 21 e Spring Boot para aprender fundamentos de Java e integrar com a API PJ do Banco Inter.

O repositório agora é organizado como um monorepo simples:

```text
server/  → Java 21 + Spring Boot
docs/    → integração, arquitetura simples e backlog de estudo
web/     → protótipo visual mantido fora do escopo atual
```

## Primeiro experimento

O ponto de partida é um server Java 21 com Spring Boot e um modelo pequeno para estudar:

- `InterTransaction` como representação normalizada do movimento;
- `Money` para valores monetários sem `double`;
- contrato de gateway para a integração com o Inter;
- testes de validação do modelo.

O próximo passo é implementar o cliente autenticado da API PJ do Inter em sandbox.

Subir os serviços locais:

```bash
docker compose up -d
```

Iniciar o servidor Java usando Docker (não é necessário instalar Maven na máquina):

```bash
docker run --rm -it --network host \
  --user "$(id -u):$(id -g)" \
  -e MAVEN_CONFIG=/tmp/maven \
  -v /home/hallan/github/hallancosta/fintech-inter-ledger-lab/server:/workspace \
  -w /workspace \
  maven:3.9-eclipse-temurin-21 \
  mvn spring-boot:run
```

Mantenha esse comando rodando no terminal. O servidor estará funcionando quando
aparecerem no log mensagens semelhantes a:

```text
Tomcat started on port 8080 (http)
Started ServerApplication
```

Em outro terminal, teste os endpoints:

```bash
curl http://localhost:8080/api/health
curl http://localhost:8080/actuator/health
```

As respostas esperadas são semelhantes a:

```json
{"status":"UP","service":"fintech-inter-ledger-server","timestamp":"..."}
{"groups":["liveness","readiness"],"status":"UP"}
```

Para parar o servidor, pressione `Ctrl+C` no terminal em que ele está rodando.

Se o Maven já estiver instalado localmente, também é possível iniciar o servidor
diretamente:

```bash
cd server
mvn spring-boot:run
```

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
