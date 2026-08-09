#!/usr/bin/env bash
# 解析最新 Release → 浅克隆 → sync analyzed → 初始化 class-queue
# 用法:
#   ./scripts/bootstrap_project.sh <url-or-alias> [modules-csv] [local-name]
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"
TARGET="${1:?url or alias}"
MODULES="${2:-}"
LOCAL_NAME="${3:-}"

export PYTHONPATH="${ROOT}/framework${PYTHONPATH:+:$PYTHONPATH}"

META_JSON="$(python3 - <<PY
import json, sys
from oca.resolve import normalize_repo_url, resolve_version
from oca.util.paths import slugify_project_name

url = normalize_repo_url("$TARGET")
rt = resolve_version(url, "latest")
name = "$LOCAL_NAME".strip() or (
    "springframework" if rt.repo == "spring-framework"
    else "springboot" if rt.repo == "spring-boot"
    else slugify_project_name(rt.repo)
)
print(json.dumps({
    "project": name,
    "repo_url": rt.repo_url,
    "clone_url": rt.clone_url,
    "version": rt.version,
    "git_ref": rt.git_ref,
    "source": rt.source,
}, ensure_ascii=False))
PY
)"

echo "$META_JSON" | tee /tmp/oca_bootstrap_meta.json
PROJ=$(python3 -c "import json;print(json.load(open('/tmp/oca_bootstrap_meta.json'))['project'])")
VER=$(python3 -c "import json;print(json.load(open('/tmp/oca_bootstrap_meta.json'))['version'])")

echo "[bootstrap] fetch $PROJ@$VER"
ARGS=(fetch "$TARGET" latest)
if [[ -n "$LOCAL_NAME" ]]; then
  ARGS+=(--name "$LOCAL_NAME")
fi
./bin/oca "${ARGS[@]}"

# 确保 analyzed 存在
if [[ ! -d "$PROJ/$VER/analyzed/.git" ]] && [[ ! -f "$PROJ/$VER/analyzed/README.md" ]] && [[ -z "$(ls -A "$PROJ/$VER/analyzed" 2>/dev/null || true)" ]]; then
  echo "[bootstrap] sync analyzed <- original"
  mkdir -p "$PROJ/$VER/analyzed"
  if command -v rsync >/dev/null; then
    rsync -a --exclude '.git' "$PROJ/$VER/original/" "$PROJ/$VER/analyzed/"
  else
    cp -a "$PROJ/$VER/original/." "$PROJ/$VER/analyzed/"
  fi
fi

echo "[bootstrap] class-queue-init"
if [[ -n "$MODULES" ]]; then
  ./bin/class-queue-init.sh "$PROJ" "$VER" "$MODULES"
else
  ./bin/class-queue-init.sh "$PROJ" "$VER"
fi

echo "[bootstrap] DONE $PROJ/$VER pending=$(wc -l < "$PROJ/$VER/_reports/class-queue/pending.txt")"
