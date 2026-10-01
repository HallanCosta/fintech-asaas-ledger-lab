#!/usr/bin/env bash

set -Eeuo pipefail

PROJECT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
SERVER_DIR="$PROJECT_DIR/server"
RUNTIME_ENV="$SERVER_DIR/.env"
LOCAL_ENV="$SERVER_DIR/.env.local"
PRODUCTION_ENV="$SERVER_DIR/.env.production"

usage() {
  cat <<'EOF'
Uso:
  ./scripts/run-server.sh              Inicia com server/.env.local
  ./scripts/run-server.sh production   Inicia com server/.env.production
EOF
}

environment="${1:-local}"
case "$environment" in
  local)
    selected_env="$LOCAL_ENV"
    ;;
  production)
    selected_env="$PRODUCTION_ENV"
    ;;
  -h|--help)
    usage
    exit 0
    ;;
  *)
    echo "Ambiente inválido: $environment" >&2
    usage >&2
    exit 2
    ;;
esac

if [[ ! -f "$selected_env" ]]; then
  echo "Arquivo de ambiente não encontrado: $selected_env" >&2
  exit 1
fi

if ! command -v docker >/dev/null 2>&1; then
  echo "Docker não foi encontrado no PATH." >&2
  exit 1
fi

if [[ "$environment" == "production" ]]; then
  for required_key in ASAAS_API_KEY ASAAS_BASE_URL; do
    if ! awk -F= -v key="$required_key" '$1 == key && $2 != "" { found = 1 } END { exit !found }' "$selected_env"; then
      echo "Preencha $required_key em $selected_env antes de iniciar produção." >&2
      exit 1
    fi
  done
fi

temporary_backup=""
if [[ -e "$RUNTIME_ENV" ]]; then
  temporary_backup="$(mktemp)"
  cp "$RUNTIME_ENV" "$temporary_backup"
fi

restore_runtime_env() {
  local exit_status=$?
  if [[ -n "$temporary_backup" ]]; then
    cp "$temporary_backup" "$RUNTIME_ENV"
    rm -f "$temporary_backup"
  else
    rm -f "$RUNTIME_ENV"
  fi
  exit "$exit_status"
}

trap restore_runtime_env EXIT
cp "$selected_env" "$RUNTIME_ENV"

echo "Ambiente: $environment"
echo "Configuração aplicada temporariamente: $selected_env"
echo "Servidor: http://localhost:$(awk -F= '$1 == "SERVER_PORT" { print $2 }' "$RUNTIME_ENV" | tail -n 1)"

docker run --rm -it \
  --network host \
  --user "$(id -u):$(id -g)" \
  -e MAVEN_CONFIG=/tmp/maven \
  --env-file "$RUNTIME_ENV" \
  -v "$SERVER_DIR:/workspace" \
  -w /workspace \
  --name fintech-pix-lab-server \
  maven:3.9-eclipse-temurin-21 \
  mvn spring-boot:run
