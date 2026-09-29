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
visível para estudo: token OAuth2, saldo e extrato. Pix ficará para uma etapa
posterior, depois que o acesso básico ao sandbox estiver funcionando.

Subir os serviços locais:

```bash
docker compose up -d
```

Iniciar o servidor Java usando Docker (não é necessário instalar Maven na máquina):

```bash
docker run --rm -it --network host \
  --user "$(id -u):$(id -g)" \
  -e MAVEN_CONFIG=/tmp/maven \
  --env-file /home/hallan/github/hallancosta/fintech-inter-ledger-lab/server/.env \
  -v /home/hallan/github/hallancosta/fintech-inter-ledger-lab/server:/workspace \
  -v /home/hallan/github/hallancosta/fintech-inter-ledger-lab/secrets/inter-api:/secrets/inter-api:ro \
  -w /workspace \
  maven:3.9-eclipse-temurin-21 \
  mvn spring-boot:run
```

Mantenha esse comando rodando no terminal. O servidor estará funcionando quando
aparecerem no log mensagens semelhantes a:

```text
Tomcat started on port 9090 (http)
Started ServerApplication
```

Em outro terminal, teste os endpoints:

```bash
curl http://localhost:9090/api/health
curl http://localhost:9090/actuator/health
```

As respostas esperadas são semelhantes a:

```json
{"status":"UP","service":"fintech-inter-ledger-server","timestamp":"..."}
{"groups":["liveness","readiness"],"status":"UP"}
```

Para parar o servidor, pressione `Ctrl+C` no terminal em que ele está rodando.

Os testes de aprendizado podem ser executados com:

```bash
docker run --rm -it \
  --user "$(id -u):$(id -g)" \
  -e MAVEN_CONFIG=/tmp/maven \
  -v /home/hallan/github/hallancosta/fintech-inter-ledger-lab/server:/workspace \
  -w /workspace \
  maven:3.9-eclipse-temurin-21 \
  mvn test
```

## Preparar a integração com o Inter

No Internet Banking PJ, crie uma integração para o sandbox e baixe o
certificado. O projeto espera o certificado no formato PKCS12 (`.p12` ou
`.pfx`), como usado pelo SDK Java oficial. Não faça commit do arquivo: ele já
está ignorado pelo `.gitignore`.

O ZIP `Inter_API-Chave_e_Certificado.zip` já foi extraído localmente e convertido
para `secrets/inter-api/inter-api-client.p12`. O arquivo `server/.env` já contém
o caminho do certificado, a senha local do PKCS12 e as credenciais OAuth. Esse
arquivo é local e ignorado pelo Git.
O `INTER_ACCOUNT_ID` pode ficar vazio quando a integração estiver ligada a uma
única conta corrente; ele serve para selecionar uma conta quando houver mais de
uma associada à integração.

As credenciais devem ficar somente no arquivo local `server/.env`:

```dotenv
INTER_ENABLED=true
INTER_CLIENT_ID=seu-client-id
INTER_CLIENT_SECRET=seu-client-secret
INTER_ACCOUNT_ID=
```

O comando Docker acima carrega esse arquivo e monta o certificado em modo
somente leitura.

O escopo inicial já está configurado como `extrato.read` no `server/.env`.

Com o server rodando e a integração habilitada, as rotas de laboratório são:

```bash
curl http://localhost:9090/api/inter/balance
curl 'http://localhost:9090/api/inter/statement?from=2026-09-29&to=2026-09-29'
```

Validação realizada no Sandbox:

- server iniciado na porta `9090`;
- `/api/health` respondeu `HTTP 200`;
- `/api/inter/balance` respondeu `HTTP 200`;
- `/api/inter/statement` respondeu `HTTP 200`;
- testes automatizados: 6 testes passando.

As consultas acima foram somente de leitura; nenhuma transação Pix foi criada.

Depois que esse fluxo básico estiver funcionando, adicionaremos a primeira
operação Pix com uma issue separada.

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
