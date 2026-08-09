from __future__ import annotations

import json
import os
import shutil
import sys
import time
from pathlib import Path

from .queue import QueuePaths, mark_done, mark_failed, stats


def _version_root() -> Path:
    env = os.environ.get("OCA_CLASSWORK_ROOT")
    if not env:
        raise SystemExit("OCA_CLASSWORK_ROOT 未设置")
    return Path(env)


def main() -> int:
    root = _version_root()
    qdir = root / "_reports" / "class-queue"
    qp = QueuePaths(qdir)
    if not qp.current.exists():
        print("[process-one] 无 CURRENT，跳过")
        return 0

    cur = json.loads(qp.current.read_text(encoding="utf-8"))
    rel = cur["file"]
    original = root / "original" / rel
    analyzed = root / "analyzed" / rel
    if not original.exists():
        mark_failed(qdir, rel, reason="original missing")
        return 1

    cur["status"] = "processing"
    cur["processing_at"] = time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime())
    qp.current.write_text(json.dumps(cur, ensure_ascii=False, indent=2), encoding="utf-8")

    # 工作包：给精读 agent 使用（禁止机翻管线）
    packet_dir = qdir / "work"
    packet_dir.mkdir(parents=True, exist_ok=True)
    packet = {
        "file": rel,
        "original": str(original),
        "analyzed": str(analyzed),
        "rules": [
            "完整阅读 original 源码，先理解类职责、字段含义、方法主路径与弯绕点",
            "以理解为基础，改写 analyzed：英文注释译为通顺中文；为字段与方法补充中文说明",
            "复杂方法内部关键步骤补充中文行内注释",
            "保留 License 头英文；保留 {@link}/{@code}/@author/@since 等结构",
            "禁止 Google/批量查找替换式机翻；禁止只贴空泛套话",
        ],
    }
    (packet_dir / "packet.json").write_text(json.dumps(packet, ensure_ascii=False, indent=2), encoding="utf-8")

    rewriter = os.environ.get("OCA_EXTERNAL_REWRITER", "").strip()
    if rewriter:
        import subprocess

        rc = subprocess.call([rewriter, str(packet_dir / "packet.json")])
        if rc != 0:
            mark_failed(qdir, rel, reason=f"rewriter exit {rc}")
            return rc
        mark_done(qdir, rel, note="external-rewriter")
        print(f"[process-one] DONE via rewriter: {rel}")
        return 0

    # 默认：确保 analyzed 有一份可改基线（从 original 拷贝），然后交给 agent 精读改写
    analyzed.parent.mkdir(parents=True, exist_ok=True)
    if not analyzed.exists():
        shutil.copy2(original, analyzed)

    # 若标记了自动完成（仅用于测试），否则保持 processing，等待 agent 写入后调用 mark-done
    if os.environ.get("OCA_CLASSWORK_AUTO_STUB") == "1":
        # 明确拒绝 stub 作为生产路径
        mark_failed(qdir, rel, reason="stub mode disabled for production")
        return 2

    print(f"[process-one] READY_FOR_AGENT file={rel}")
    print(f"[process-one] original={original}")
    print(f"[process-one] analyzed={analyzed}")
    print(f"[process-one] packet={packet_dir / 'packet.json'}")
    st = stats(qdir)
    print(f"[process-one] queue pending={st['pending']} done={st['done']}")
    # 不在此处 mark_done：必须等精读改写完成
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
