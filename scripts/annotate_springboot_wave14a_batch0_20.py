#!/usr/bin/env python3
"""Chinese-annotate Spring Boot 4.1.0 wave-14a slice [0:20] (pid/task/validation/reactive)."""
from __future__ import annotations

import json
import re
import shutil
import subprocess
import sys
from pathlib import Path

ROOT = Path("/workspace")
sys.path.insert(0, str(ROOT / "scripts"))

from wave14a_replacements_misc import FILE_REPLACEMENTS as MISC_REPLACEMENTS
from wave14a_replacements_system import FILE_REPLACEMENTS as SYSTEM_REPLACEMENTS
from wave14a_replacements_task import FILE_REPLACEMENTS as TASK_REPLACEMENTS

VER = ROOT / "springboot/4.1.0"
ORIGINAL = VER / "original"
ANALYZED = VER / "analyzed"
QUEUE = VER / "_reports/class-queue"
BATCH_FILES = Path("/tmp/springboot_w14a.txt").read_text(encoding="utf-8").strip().splitlines()

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}
FILE_REPLACEMENTS.update(SYSTEM_REPLACEMENTS)
FILE_REPLACEMENTS.update(TASK_REPLACEMENTS)
FILE_REPLACEMENTS.update(MISC_REPLACEMENTS)


def apply_replacements(text: str, replacements: list[tuple[str, str]]) -> str:
    for old, new in replacements:
        if old not in text:
            raise ValueError(f"Pattern not found:\n{old[:160]}...")
        text = text.replace(old, new, 1)
    return text


def has_chinese(text: str) -> bool:
    return bool(re.search(r"[\u4e00-\u9fff]", text))


def update_batch_counts() -> None:
    batch_path = QUEUE / "batch.json"
    batch = json.loads(batch_path.read_text(encoding="utf-8"))
    done_path = QUEUE / "done.txt"
    pending_path = QUEUE / "pending.txt"
    batch["done"] = len([ln for ln in done_path.read_text(encoding="utf-8").splitlines() if ln.strip()])
    batch["remaining_pending"] = len(
        [ln for ln in pending_path.read_text(encoding="utf-8").splitlines() if ln.strip()]
    )
    batch_path.write_text(json.dumps(batch, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def main() -> int:
    failures: list[str] = []
    ok = 0
    for rel in BATCH_FILES:
        name = Path(rel).name
        src = ORIGINAL / rel
        dst = ANALYZED / rel
        if not src.exists():
            failures.append(f"MISSING original: {rel}")
            continue
        dst.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(src, dst)
        reps = FILE_REPLACEMENTS.get(name, [])
        if not reps:
            failures.append(f"NO_REPLACEMENTS: {rel}")
            continue
        try:
            text = dst.read_text(encoding="utf-8")
            text = apply_replacements(text, reps)
            cn = len(re.findall(r"[\u4e00-\u9fff]", text))
            lic = "Licensed under the Apache License" in text
            if cn < 10 or not lic or not has_chinese(text):
                failures.append(f"VALIDATION cn={cn} lic={lic}: {rel}")
                continue
            dst.write_text(text, encoding="utf-8")
            ok += 1
            print(f"OK cn={cn} {rel}")
        except Exception as e:
            failures.append(f"{rel}: {e}")
            print(f"FAIL {rel}: {e}")
    if ok == len(BATCH_FILES) and not failures:
        subprocess.run(
            [
                sys.executable,
                str(ROOT / "scripts/mark_batch_done.py"),
                "--project",
                "springboot",
                "--version",
                "4.1.0",
                "--note",
                "wave14a pid/task/validation/reactive [0:20]",
                *BATCH_FILES,
            ],
            check=True,
        )
        update_batch_counts()
        print(f"Marked {ok} files done in queue")
    print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False, indent=2))
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
