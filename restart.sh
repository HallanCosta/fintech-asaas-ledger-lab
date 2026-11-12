#!/usr/bin/env bash
# Derruba e sobe de novo server e web, recriando as imagens.
set -euo pipefail

cd "$(dirname "$0")"

./down.sh
./up.sh
