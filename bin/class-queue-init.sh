#!/usr/bin/env bash
# 初始化「逐类精读」队列
# 用法:
#   ./bin/class-queue-init.sh springframework 7.0.8
#   ./bin/class-queue-init.sh springframework 7.0.8 spring-beans,spring-context
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PROJECT="${1:?project}"
VERSION="${2:?version}"
MODULES="${3:-}"
export PYTHONPATH="${ROOT}/framework${PYTHONPATH:+:$PYTHONPATH}"

python3 - <<PY
from oca.classwork.queue import build_queue
from pathlib import Path
root = Path("$ROOT") / "$PROJECT" / "$VERSION"
src = root / "original"
out = root / "_reports" / "class-queue"
modules = [x.strip() for x in "$MODULES".split(",") if x.strip()] or None
n = build_queue(src, out, modules)
print(f"[class-queue] pending={n} -> {out}")
PY
