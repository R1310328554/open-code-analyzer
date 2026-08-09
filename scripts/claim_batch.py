#!/usr/bin/env python3
"""从 class-queue 领取一批待精读文件，供并行 agent 处理。"""
from __future__ import annotations

import argparse
import json
import shutil
import time
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "framework"))

from oca.classwork.queue import QueuePaths, _read_lines, _write_lines  # noqa: E402


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--project", default="springframework")
    ap.add_argument("--version", default="7.0.8")
    ap.add_argument("--n", type=int, default=8)
    ap.add_argument("--max-lines", type=int, default=0, help="若>0，优先领取不超过该行数的文件")
    args = ap.parse_args()

    version_root = ROOT / args.project / args.version
    q = version_root / "_reports" / "class-queue"
    qp = QueuePaths(q)
    pending = _read_lines(qp.pending)
    done = set(_read_lines(qp.done))
    failed = set(_read_lines(qp.failed))

    # 清理卡住的 CURRENT
    if qp.current.exists():
        cur = json.loads(qp.current.read_text(encoding="utf-8"))
        # 若 processing 超过 30 分钟，放回 pending 头部
        claimed = cur.get("claimed_at") or cur.get("processing_at")
        rel = cur.get("file")
        if rel and rel not in done:
            pending = [rel] + [x for x in pending if x != rel]
        qp.current.unlink(missing_ok=True)

    batch: list[str] = []
    rest: list[str] = []
    for rel in pending:
        if rel in done or rel in failed:
            continue
        if len(batch) >= args.n:
            rest.append(rel)
            continue
        src = version_root / "original" / rel
        if not src.exists():
            failed.add(rel)
            continue
        if args.max_lines > 0:
            try:
                nlines = sum(1 for _ in src.open("r", encoding="utf-8", errors="ignore"))
            except OSError:
                rest.append(rel)
                continue
            if nlines > args.max_lines:
                rest.append(rel)
                continue
        batch.append(rel)
        # 从 original 覆盖 analyzed，保证精读基线干净
        dst = version_root / "analyzed" / rel
        dst.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(src, dst)

    # 未入本批的保持顺序
    for rel in pending:
        if rel not in batch and rel not in done and rel not in failed and rel not in rest:
            rest.append(rel)

    _write_lines(qp.pending, rest)
    if failed:
        old_failed = set(_read_lines(qp.failed))
        _write_lines(qp.failed, sorted(old_failed | failed))

    out = {
        "claimed_at": time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime()),
        "files": batch,
        "remaining_pending": len(rest),
        "done": len(done),
    }
    batch_path = q / "batch.json"
    batch_path.write_text(json.dumps(out, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps(out, ensure_ascii=False, indent=2))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
