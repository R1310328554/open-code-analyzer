#!/usr/bin/env python3
"""将 batch.json 中的文件标记为 done（精读完成后调用）。"""
from __future__ import annotations

import argparse
import json
import sys
import time
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "framework"))

from oca.classwork.queue import QueuePaths, _read_lines, _write_lines  # noqa: E402


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--project", default="springframework")
    ap.add_argument("--version", default="7.0.8")
    ap.add_argument("--note", default="agent-understood-batch")
    ap.add_argument("files", nargs="*")
    args = ap.parse_args()

    q = ROOT / args.project / args.version / "_reports" / "class-queue"
    qp = QueuePaths(q)
    files = list(args.files)
    if not files:
        batch = q / "batch.json"
        if batch.exists():
            files = json.loads(batch.read_text(encoding="utf-8")).get("files", [])
    done = _read_lines(qp.done)
    done_set = set(done)
    for rel in files:
        if rel not in done_set:
            done.append(rel)
            done_set.add(rel)
            with qp.log.open("a", encoding="utf-8") as f:
                f.write(f"{time.strftime('%Y-%m-%dT%H:%M:%SZ', time.gmtime())} DONE {rel} {args.note}\n")
    _write_lines(qp.done, done)
    # 从 pending 去除
    pending = [x for x in _read_lines(qp.pending) if x not in done_set]
    _write_lines(qp.pending, pending)
    if qp.current.exists():
        cur = json.loads(qp.current.read_text(encoding="utf-8"))
        if cur.get("file") in done_set:
            qp.current.unlink(missing_ok=True)
    print(json.dumps({"marked": len(files), "done_total": len(done), "pending": len(pending)}, ensure_ascii=False))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
