#!/usr/bin/env bash
# Derruba server e web de produção (Docker Compose).
set -euo pipefail

cd "$(dirname "$0")"

ENV_FILE="server/.env.production"
if [ ! -f "$ENV_FILE" ]; then
  echo "Arquivo $ENV_FILE não encontrado." >&2
  exit 1
fi

docker compose -f docker-compose.prod.yml --env-file "$ENV_FILE" down "$@"
