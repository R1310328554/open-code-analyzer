#!/usr/bin/env bash
# 精读改写完成后标记当前类 done，并清除 CURRENT
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PROJECT="${1:?project}"
VERSION="${2:?version}"
NOTE="${3:-agent-understood}"
export PYTHONPATH="${ROOT}/framework${PYTHONPATH:+:$PYTHONPATH}"
python3 - <<PY
from pathlib import Path
import json
from oca.classwork.queue import mark_done, QueuePaths
q = Path("$ROOT/$PROJECT/$VERSION/_reports/class-queue")
qp = QueuePaths(q)
if not qp.current.exists():
    raise SystemExit("no CURRENT")
rel = json.loads(qp.current.read_text())["file"]
mark_done(q, rel, note="$NOTE")
print(f"[mark-done] {rel}")
PY
