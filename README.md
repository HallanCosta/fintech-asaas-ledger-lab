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

O cliente didático da API PJ já está preparado em `com.hallancosta.inter`.
Ele fica desabilitado por padrão e não tenta acessar o Inter sem credenciais.
Quando habilitado, o fluxo estudado é:

```text
OAuth2 client_credentials + certificado mTLS
                    ↓
        cliente de banking / Pix
                    ↓
       modelo interno do laboratório
```

O código usa os mesmos caminhos que aparecem no SDK oficial, mas deixa o HTTP
visível para estudo: token OAuth2, saldo, extrato e criação de cobrança Pix.

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

## Preparar a integração com o Inter

No Internet Banking PJ, crie uma integração para o sandbox e baixe o
certificado. O projeto espera o certificado no formato PKCS12 (`.p12` ou
`.pfx`), como usado pelo SDK Java oficial. Não faça commit do arquivo: ele já
está ignorado pelo `.gitignore`.

O primeiro teste real será somente depois de você ter o `client id`, o
`client secret`, o certificado e a senha do certificado. Deixe esses valores
apenas no terminal ou em um arquivo local ignorado:

```bash
export INTER_ENABLED=true
export INTER_ENVIRONMENT=SANDBOX
export INTER_BASE_URL=https://cdpj-sandbox.partners.uatinter.co
export INTER_CLIENT_ID='seu-client-id'
export INTER_CLIENT_SECRET='seu-client-secret'
export INTER_CERTIFICATE_PATH="$PWD/../secrets/inter-sandbox.pfx"
export INTER_CERTIFICATE_PASSWORD='senha-do-certificado'
export INTER_ACCOUNT_ID='sua-conta-corrente-se-houver'
export INTER_SCOPE='extrato.read'
```

Com o escopo de leitura, suba o server e consulte saldo/extrato pelos clientes
Java. Para estudar cobrança Pix, solicite também a permissão `cob.write`:

```bash
export INTER_SCOPE='extrato.read cob.read cob.write'
```

Com o server rodando e a integração habilitada, as rotas de laboratório são:

```bash
curl http://localhost:8080/api/inter/balance
curl 'http://localhost:8080/api/inter/statement?from=2026-09-29&to=2026-09-29'

curl -X POST http://localhost:8080/api/inter/pix/charges \
  -H 'Content-Type: application/json' \
  -d '{
    "calendario": {"expiration": 3600},
    "valor": {"original": "10.00"},
    "chave": "sua-chave-pix",
    "solicitacaoPagador": "Cobrança de estudo Java"
  }'
```

Essa última chamada cria uma cobrança Pix imediata; ela não envia um Pix para
outra pessoa. Por isso, ainda vamos executar a primeira operação real com
cuidado depois que as permissões do sandbox estiverem ativas.

Ainda não coloque credenciais reais no repositório. O passo que depende de você
amanhã é apenas criar/ativar a integração no portal, baixar o certificado e
preencher as variáveis localmente.

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

1. testar o OAuth/mTLS com o sandbox;
2. expor endpoints internos para saldo, extrato e cobrança Pix;
3. normalizar extrato e criar a primeira transação de estudo;
4. adicionar testes de integração contra o sandbox.

Veja [docs/architecture.md](docs/architecture.md), [docs/inter-integration.md](docs/inter-integration.md), [docs/stack.md](docs/stack.md) e [docs/issues.md](docs/issues.md).
