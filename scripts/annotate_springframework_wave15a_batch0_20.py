#!/usr/bin/env python3
"""Chinese-annotate Spring Framework 7.0.8 wave-15a batch [0:20]."""
from __future__ import annotations

import importlib.util
import json
import re
import shutil
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "springframework/7.0.8"
ORIGINAL = VER / "original"
ANALYZED = VER / "analyzed"
QUEUE = VER / "_reports/class-queue"
SCRIPTS = ROOT / "scripts"

BATCH_FILES = [
    "spring-context/src/main/java/org/springframework/validation/support/BindingAwareConcurrentModel.java",
    "spring-context/src/main/java/org/springframework/validation/support/BindingAwareModelMap.java",
    "spring-tx/src/main/java/org/springframework/dao/CannotAcquireLockException.java",
    "spring-tx/src/main/java/org/springframework/dao/CannotSerializeTransactionException.java",
    "spring-tx/src/main/java/org/springframework/dao/CleanupFailureDataAccessException.java",
    "spring-tx/src/main/java/org/springframework/dao/ConcurrencyFailureException.java",
    "spring-tx/src/main/java/org/springframework/dao/DataAccessException.java",
    "spring-tx/src/main/java/org/springframework/dao/DataAccessResourceFailureException.java",
    "spring-tx/src/main/java/org/springframework/dao/DataIntegrityViolationException.java",
    "spring-tx/src/main/java/org/springframework/dao/DataRetrievalFailureException.java",
    "spring-tx/src/main/java/org/springframework/dao/DeadlockLoserDataAccessException.java",
    "spring-tx/src/main/java/org/springframework/dao/DuplicateKeyException.java",
    "spring-tx/src/main/java/org/springframework/dao/EmptyResultDataAccessException.java",
    "spring-tx/src/main/java/org/springframework/dao/IncorrectResultSizeDataAccessException.java",
    "spring-tx/src/main/java/org/springframework/dao/IncorrectUpdateSemanticsDataAccessException.java",
    "spring-tx/src/main/java/org/springframework/dao/InvalidDataAccessApiUsageException.java",
    "spring-tx/src/main/java/org/springframework/dao/InvalidDataAccessResourceUsageException.java",
    "spring-tx/src/main/java/org/springframework/dao/NonTransientDataAccessException.java",
    "spring-tx/src/main/java/org/springframework/dao/NonTransientDataAccessResourceException.java",
    "spring-tx/src/main/java/org/springframework/dao/OptimisticLockingFailureException.java",
]

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}


def _load_module(name: str, attr: str) -> dict:
    path = SCRIPTS / name
    spec = importlib.util.spec_from_file_location(name.replace(".py", ""), path)
    mod = importlib.util.module_from_spec(spec)
    assert spec.loader is not None
    spec.loader.exec_module(mod)
    return getattr(mod, attr)


for mod_name, attr in [
    ("wave15a_binding_support.py", "BINDING_SUPPORT_REPLACEMENTS"),
    ("wave15a_dao_exceptions.py", "DAO_EXCEPTIONS_REPLACEMENTS"),
]:
    FILE_REPLACEMENTS.update(_load_module(mod_name, attr))


def apply_replacements(text: str, replacements: list[tuple[str, str]]) -> str:
    for old, new in replacements:
        if old not in text:
            raise ValueError(f"Pattern not found:\n{old[:120]}...")
        text = text.replace(old, new, 1)
    return text


def mark_batch_done(batch: list[str]) -> None:
    done_path = QUEUE / "done.txt"
    done = [ln.strip() for ln in done_path.read_text(encoding="utf-8").splitlines() if ln.strip()]
    done_set = set(done)
    for rel in batch:
        if rel not in done_set:
            done.append(rel)
            done_set.add(rel)
    done_path.write_text("\n".join(done) + "\n", encoding="utf-8")


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
            if cn < 10 or not lic:
                failures.append(f"VALIDATION cn={cn} lic={lic}: {rel}")
                continue
            dst.write_text(text, encoding="utf-8")
            ok += 1
            print(f"OK cn={cn} {rel}")
        except Exception as e:
            failures.append(f"{rel}: {e}")
            print(f"FAIL {rel}: {e}")
    if ok == len(BATCH_FILES) and not failures:
        mark_batch_done(BATCH_FILES)
        print("Marked batch done in done.txt")
    print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False, indent=2))
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())