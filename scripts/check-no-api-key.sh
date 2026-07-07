#!/usr/bin/env bash
# Bloqueia o commit se um valor de TMDB_API_KEY em texto puro estiver no diff staged.
# Usado pelo hook PreToolUse (git commit) em .claude/settings.json.
set -euo pipefail

# Nunca falhar por causa deste script fora de um repo git
if ! git rev-parse --is-inside-work-tree >/dev/null 2>&1; then
  exit 0
fi

DIFF="$(git diff --cached -- . ':(exclude)scripts/check-no-api-key.sh')"

# local.properties nunca deve ser commitado
if git diff --cached --name-only | grep -q '^local\.properties$'; then
  echo "ERRO: local.properties está staged. Esse arquivo contém a TMDB_API_KEY e nunca deve ser commitado." >&2
  exit 2
fi

# Padrão comum de chave TMDB v3 (32 hex chars) hardcoded em código/config
if echo "$DIFF" | grep -qE 'TMDB_API_KEY\s*[:=]\s*"?[0-9a-fA-F]{32}"?'; then
  echo "ERRO: parece haver uma TMDB_API_KEY em texto puro no diff. Use local.properties + BuildConfig, nunca hardcode." >&2
  exit 2
fi

exit 0
