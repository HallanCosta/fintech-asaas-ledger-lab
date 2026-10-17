# Preparação para AWS EC2

Este documento prepara o server para uma futura execução em EC2. Nenhum recurso
AWS é criado por este projeto automaticamente.

## Princípios

- validar primeiro com `server/.env.staging` e o Sandbox do Asaas;
- manter API keys e tokens fora da imagem Docker e do Git;
- expor publicamente apenas HTTPS por um reverse proxy ou load balancer;
- manter a porta interna do server em `9090`;
- usar PostgreSQL gerenciado e Redis gerenciado quando o estudo de persistência
  começar, em vez dos serviços com credenciais de desenvolvimento do Compose;
- publicar logs sem API key, webhook token ou payload financeiro desnecessário.

## Imagem local

O server possui um Dockerfile multi-stage que compila com Java 21 e executa com
uma JRE:

```bash
docker build -t fintech-pix-lab-server:local ./server
docker run --rm --name fintech-pix-lab-server \
  --env-file server/.env.staging \
  -p 127.0.0.1:9090:9090 \
  fintech-pix-lab-server:local
```

Valide o health check:

```bash
curl http://127.0.0.1:9090/actuator/health
```

## Roteiro futuro para EC2

1. Criar uma EC2 privada ou protegida por Security Group.
2. Instalar Docker e configurar reinício automático do container.
3. Copiar somente o arquivo de ambiente do staging para a máquina, com
   permissões restritas.
4. Publicar a imagem em um registry e fixar uma versão por tag ou digest.
5. Colocar HTTPS na frente do container e limitar a exposição da porta `9090`.
6. Configurar health check, logs e backup antes de habilitar produção.
7. Trocar o arquivo de staging por secrets manager e API key de produção apenas
   após validar o fluxo completo no Sandbox.

Produção não deve ser o primeiro deploy. O primeiro objetivo é comprovar health,
saldo, extrato, cobrança Pix e webhooks usando Sandbox.
