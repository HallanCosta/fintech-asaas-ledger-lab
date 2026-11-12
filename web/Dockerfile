# syntax=docker/dockerfile:1

# ---- build: gera o bundle estático com Vite ----
FROM node:22-alpine AS build

WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci --no-audit --no-fund

COPY . .

# Rótulo opcional exibido no dashboard. A chave interna NÃO entra no build:
# o Nginx injeta o header X-Api-Key no proxy em tempo de execução.
ARG VITE_ASAAS_ENVIRONMENT=""
ENV VITE_ASAAS_ENVIRONMENT=${VITE_ASAAS_ENVIRONMENT}
RUN npm run build

# ---- runtime: Nginx servindo dist/ e fazendo proxy de /api ----
FROM nginx:1.29-alpine

# O entrypoint oficial do Nginx roda envsubst em /etc/nginx/templates/*.template
# e grava o resultado em /etc/nginx/conf.d/. Só variáveis definidas no
# ambiente são substituídas, então $host, $uri etc. continuam intactos.
COPY nginx/default.conf.template /etc/nginx/templates/default.conf.template
COPY --from=build /app/dist /usr/share/nginx/html

# NGINX_ENTRYPOINT_LOCAL_RESOLVERS faz o entrypoint exportar NGINX_LOCAL_RESOLVERS
# (DNS interno do Docker), usado pelo "resolver" do template.
ENV NGINX_ENTRYPOINT_LOCAL_RESOLVERS=1 \
    API_UPSTREAM=server:9090 \
    ASAAS_INTERNAL_API_KEY=""

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD wget -qO- http://127.0.0.1/healthz >/dev/null || exit 1
