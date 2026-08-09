#!/usr/bin/env bash
# 逐类精读定时器：默认每 10 秒领取/触发处理一个类，直到队列清空。
#
# 设计原则：
# - 一次只处理一个类
# - 处理逻辑是「读懂再改注释」，不是批量机翻
# - 本守护进程负责节奏；真正改文件由 process-one-class 钩子完成
#
# 用法:
#   ./bin/class-by-class-daemon.sh springframework 7.0.8
#   INTERVAL=10 ./bin/class-by-class-daemon.sh springframework 7.0.8
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PROJECT="${1:?project}"
VERSION="${2:?version}"
INTERVAL="${INTERVAL:-10}"
QUEUE_DIR="$ROOT/$PROJECT/$VERSION/_reports/class-queue"
export PYTHONPATH="${ROOT}/framework${PYTHONPATH:+:$PYTHONPATH}"

mkdir -p "$QUEUE_DIR"
if [[ ! -f "$QUEUE_DIR/pending.txt" ]]; then
  "$ROOT/bin/class-queue-init.sh" "$PROJECT" "$VERSION"
fi

echo "[daemon] queue=$QUEUE_DIR interval=${INTERVAL}s"
echo "[daemon] mode=class-by-class-understand (禁止批量机翻)"

while true; do
  # 若 CURRENT 仍在 processing，等待人工/agent 完成，不抢下一个
  if [[ -f "$QUEUE_DIR/CURRENT.json" ]]; then
    status="$(python3 - <<PY
import json
from pathlib import Path
p=Path("$QUEUE_DIR/CURRENT.json")
print(json.loads(p.read_text()).get("status",""))
PY
)"
    if [[ "$status" == "claimed" || "$status" == "processing" ]]; then
      # 尝试处理当前类
      "$ROOT/bin/process-one-class.sh" "$PROJECT" "$VERSION" || true
      sleep "$INTERVAL"
      continue
    fi
  fi

  next="$(python3 - <<PY
from oca.classwork.queue import claim_next, stats
from pathlib import Path
q=Path("$QUEUE_DIR")
rel=claim_next(q)
st=stats(q)
print(rel or "")
import sys
print(f"[daemon] stats pending={st['pending']} done={st['done']} failed={st['failed']} current={st['current']}", file=sys.stderr)
PY
)"

  if [[ -z "$next" ]]; then
    echo "[daemon] 队列已空，退出"
    break
  fi

  echo "[daemon] $(date -u +%Y-%m-%dT%H:%M:%SZ) claim $next"
  "$ROOT/bin/process-one-class.sh" "$PROJECT" "$VERSION" || true
  sleep "$INTERVAL"
done
