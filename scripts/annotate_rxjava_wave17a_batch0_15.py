#!/usr/bin/env python3
"""Chinese-annotate RxJava 4.0.0-alpha-21 wave-17a observable operators [0:15]."""
from __future__ import annotations

import importlib.util
import json
import re
import shutil
import subprocess
import sys
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "rxjava/4.0.0-alpha-21"
ORIGINAL = VER / "original"
ANALYZED = VER / "analyzed"
QUEUE = VER / "_reports/class-queue"
SCRIPTS = ROOT / "scripts"
WAVE17A_FILE = Path("/tmp/rxjava_w17a.txt")
BATCH_FILES = [
    ln.strip()
    for ln in WAVE17A_FILE.read_text(encoding="utf-8").splitlines()
    if ln.strip()
]

GUARD_FILES = [
    VER
    / "analyzed/src/main/java/io/reactivex/rxjava4/internal/operators/flowable/FlowableSamplePublisher.java",
    VER
    / "analyzed/src/main/java/io/reactivex/rxjava4/internal/operators/mixed/FlowableConcatMapCompletable.java",
    VER
    / "analyzed/src/main/java/io/reactivex/rxjava4/internal/operators/mixed/ObservableConcatMapMaybe.java",
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/TransactionDefinition.java",
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/interceptor/RollbackRuleAttribute.java",
]

_spec = importlib.util.spec_from_file_location(
    "wave17a_replacements_observable",
    SCRIPTS / "wave17a_replacements_observable.py",
)
_mod = importlib.util.module_from_spec(_spec)
assert _spec.loader is not None
_spec.loader.exec_module(_mod)
FILE_REPLACEMENTS = _mod.OBSERVABLE_W17A_REPLACEMENTS


def has_chinese(text: str) -> bool:
    return bool(re.search(r"[\u4e00-\u9fff]", text))


def tree_guard() -> int:
    tracked = len(subprocess.check_output(["git", "-C", str(ROOT), "ls-files"]).splitlines())
    if tracked < 50000:
        raise RuntimeError(f"tree guard failed: tracked={tracked} (expected >=50000)")
    for path in GUARD_FILES:
        if not path.exists():
            raise RuntimeError(f"guard file missing: {path}")
        if not has_chinese(path.read_text(encoding="utf-8")):
            raise RuntimeError(f"guard file lacks Chinese: {path}")
    return tracked


def apply_replacements(text: str, replacements: list[tuple[str, str]]) -> str:
    for old, new in replacements:
        if old not in text:
            raise ValueError(f"Pattern not found:\n{old[:120]}...")
        text = text.replace(old, new, 1)
    return text


def mark_queue_done(files: list[str]) -> None:
    subprocess.run(
        [
            sys.executable,
            str(SCRIPTS / "mark_batch_done.py"),
            "--project",
            "rxjava",
            "--version",
            "4.0.0-alpha-21",
            "--note",
            "wave17a",
            *files,
        ],
        check=True,
    )
    batch_path = QUEUE / "batch.json"
    batch = json.loads(batch_path.read_text(encoding="utf-8"))
    all_files = batch.get("files", [])
    batch["files"] = all_files[15:] if len(all_files) > 15 else all_files
    batch["done"] = len(
        [ln for ln in (QUEUE / "done.txt").read_text(encoding="utf-8").splitlines() if ln.strip()]
    )
    batch["remaining_pending"] = len(
        [ln for ln in (QUEUE / "pending.txt").read_text(encoding="utf-8").splitlines() if ln.strip()]
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
        reps = FILE_REPLACEMENTS.get(name, [])
        if not reps:
            failures.append(f"NO_REPLACEMENTS: {rel}")
            continue
        dst.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(src, dst)
        try:
            text = apply_replacements(dst.read_text(encoding="utf-8"), reps)
            cn = len(re.findall(r"[\u4e00-\u9fff]", text))
            lic = "Licensed under the Apache License" in text
            if cn < 10 or not lic:
                failures.append(f"VALIDATION cn={cn} lic={lic}: {rel}")
                continue
            dst.write_text(text, encoding="utf-8")
            ok += 1
            print(f"OK cn={cn} {rel}")
        except Exception as e:
            failures.append(f"{rel}: {e}")
            print(f"FAIL {rel}: {e}")
    tree_count = tree_guard() if ok == len(BATCH_FILES) and not failures else 0
    if "--mark-queue" in sys.argv and ok == len(BATCH_FILES) and not failures:
        mark_queue_done(BATCH_FILES)
        print(f"Marked {ok} files done in queue (note=wave17a)")
    print(
        json.dumps(
            {"ok": ok, "failures": failures, "tree_count": tree_count},
            ensure_ascii=False,
            indent=2,
        )
    )
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
