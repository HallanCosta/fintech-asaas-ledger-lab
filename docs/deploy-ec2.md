# Deploy em AWS EC2 com Docker

Este guia sobe o **server** (Spring Boot) e o **web** (React servido por Nginx)
numa EC2 pequena do free tier (t2.micro/t3.micro, 1 GB de RAM) usando
`docker-compose.prod.yml`. Nenhum recurso AWS é criado automaticamente.

## Como fica rodando

```text
browser ──:80──▶ web (Nginx)
                  ├─ /        → arquivos do build do Vite (SPA)
                  └─ /api/... → server:9090 (rede interna do Docker)
                                 + header X-Api-Key injetado pelo Nginx
```

- **server**: imagem multi-stage (Maven + JDK 21 só no build; runtime
  `eclipse-temurin:21-jre-alpine`), usuário não-root, limite de 512 MB e
  `-XX:MaxRAMPercentage=75 -XX:+UseSerialGC`. A porta `9090` **não** é
  publicada no host; só o Nginx alcança o server.
- **web**: build com Node, runtime `nginx:alpine` (limite de 64 MB). O Nginx
  serve o `dist/`, faz fallback de SPA para `index.html` e repassa `/api/` ao
  server.
- **Chave interna**: o Nginx lê `ASAAS_INTERNAL_API_KEY` em tempo de execução e
  envia `X-Api-Key` em toda chamada a `/api/`. A chave **não** vai para o
  bundle JS e qualquer `X-Api-Key` vindo do browser é sobrescrito.
- `docker-compose.yml` (Postgres/Redis) é só para desenvolvimento e não é
  usado na EC2; o app ainda não usa esses serviços.

> Atenção: como o Nginx injeta a chave, **quem abrir o site consegue usar a
> API** (saldo, extrato, criar cobrança Pix). A chave só impede chamadas
> diretas ao server. Restrinja a porta 80 ao seu IP no Security Group (ou
> coloque autenticação na frente) enquanto o laboratório estiver no ar.

## 1. Criar a EC2

1. AMI **Ubuntu Server 24.04 LTS** (mais simples de instalar Docker), tipo
   marcado como *Free tier eligible* no console (t2.micro ou t3.micro; se
   escolher t4g, a máquina é ARM, veja a observação na opção B).
2. Disco de 20 GB gp3 (as imagens ocupam ~450 MB, mais o cache do build).
3. Security Group:
   - SSH (22): só o seu IP;
   - HTTP (80): só o seu IP (recomendado) ou `0.0.0.0/0` se aceitar o risco
     acima.
4. Baixe a chave `.pem` e conecte:

```bash
chmod 400 minha-chave.pem
ssh -i minha-chave.pem ubuntu@IP_PUBLICO_DA_EC2
```

## 2. Instalar Docker na EC2

```bash
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker "$USER"
exit   # saia e entre de novo no SSH para o grupo valer
docker compose version
```

O script oficial instala Docker Engine, Buildx e o plugin Compose. (No Amazon
Linux 2023 o pacote `docker` do `dnf` não traz o plugin Compose; por isso o
guia usa Ubuntu.)

Crie swap de 2 GB. Ajuda muito numa máquina de 1 GB, principalmente se o build
do Maven rodar nela:

```bash
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
```

## 3. Código e arquivo de ambiente

```bash
git clone https://github.com/HallanCosta/fintech-asaas-ledger-lab.git
cd fintech-asaas-ledger-lab
```

Do seu computador, copie o arquivo de ambiente (nunca versionado):

```bash
scp -i minha-chave.pem server/.env.production \
  ubuntu@IP_PUBLICO_DA_EC2:~/fintech-asaas-ledger-lab/server/.env.production
```

Na EC2: `chmod 600 server/.env.production`. Conteúdo mínimo:

```dotenv
SERVER_PORT=9090
ASAAS_ENABLED=true
ASAAS_ENVIRONMENT=SANDBOX
ASAAS_BASE_URL=https://api-sandbox.asaas.com/v3
ASAAS_API_KEY=sua-chave-do-sandbox
ASAAS_INTERNAL_API_KEY=gere-com-openssl-rand-hex-32
```

Para começar, use a chave do **Sandbox** (pode copiar o `server/.env.staging`
como `server/.env.production`). Gere o segredo interno com
`openssl rand -hex 32` (só letras e números; evite aspas e `$`).

O server lê o arquivo inteiro (`env_file`). O web só precisa de
`ASAAS_INTERNAL_API_KEY`, que o Compose pega via `--env-file`; assim a chave do
Asaas não entra no container do Nginx.

## 4. Subir

### Opção A: build na própria EC2 (mais simples)

```bash
docker compose -f docker-compose.prod.yml --env-file server/.env.production up -d --build
```

O build do Maven na micro é lento (alguns minutos) e pode travar por falta de
memória; com o swap do passo 2 costuma passar. Se travar, use a opção B.

### Opção B: build no seu computador e enviar as imagens (recomendado)

No seu computador, na raiz do repo:

```bash
docker compose -f docker-compose.prod.yml --env-file server/.env.production build
docker save fintech-asaas-ledger-lab-server:latest fintech-asaas-ledger-lab-web:latest \
  | gzip | ssh -i minha-chave.pem ubuntu@IP_PUBLICO_DA_EC2 'gunzip | docker load'
```

Na EC2:

```bash
cd fintech-asaas-ledger-lab
docker compose -f docker-compose.prod.yml --env-file server/.env.production up -d --no-build
```

Se a EC2 for ARM (t4g) e seu computador x86, gere as imagens para ARM:
`DOCKER_DEFAULT_PLATFORM=linux/arm64 docker compose -f docker-compose.prod.yml --env-file server/.env.production build`.

(Mais adiante dá para publicar as imagens num registry, GHCR ou ECR, e só
fazer `docker compose pull` na EC2.)

## 5. Verificar

```bash
docker compose -f docker-compose.prod.yml --env-file server/.env.production ps
curl http://localhost/api/health
curl "http://localhost/api/asaas/statement?from=2026-11-01&to=2026-11-12"
docker compose -f docker-compose.prod.yml --env-file server/.env.production logs -f server
```

No browser: `http://IP_PUBLICO_DA_EC2/`.

- `401` em `/api/asaas/...`: `ASAAS_INTERNAL_API_KEY` vazio ou diferente entre
  server e Nginx.
- `502` com `upstreamStatus: 401`: o Asaas rejeitou a `ASAAS_API_KEY`.
- `502 Bad Gateway` do Nginx: o server está fora do ar; veja os logs.

## Operação

```bash
# atualizar depois de um git pull (opção A)
docker compose -f docker-compose.prod.yml --env-file server/.env.production up -d --build

# parar tudo
docker compose -f docker-compose.prod.yml --env-file server/.env.production down

# limpar imagens antigas
docker image prune -f
```

Os containers usam `restart: unless-stopped` e voltam sozinhos após reboot da
EC2. Os logs são rotacionados (3 arquivos de 10 MB por container).

Para não ser cobrado depois do período gratuito, pare ou encerre a instância
quando terminar o estudo.

## Ainda fora deste guia

- HTTPS (domínio + Certbot ou um Load Balancer com certificado ACM);
- autenticação de usuário no dashboard;
- secrets no AWS Secrets Manager/SSM em vez de arquivo `.env`;
- Postgres/Redis gerenciados quando houver persistência.
