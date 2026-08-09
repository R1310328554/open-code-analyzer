#!/usr/bin/env python3
"""Chinese-annotate Spring Framework 7.0.8 wave-20b batch [20:40]."""
from __future__ import annotations

import importlib.util
import json
import re
import shutil
import subprocess
import sys
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "springframework/7.0.8"
ORIGINAL = VER / "original"
ANALYZED = VER / "analyzed"
SCRIPTS = ROOT / "scripts"

BATCH_FILES = [
    ln.strip()
    for ln in Path("/tmp/sf_w20b.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]

GUARD_FILES = [
    VER
    / "analyzed/spring-tx/src/main/java/org/springframework/transaction/TransactionDefinition.java",
    VER
    / "analyzed/spring-tx/src/main/java/org/springframework/transaction/interceptor/RollbackRuleAttribute.java",
    VER
    / "analyzed/spring-tx/src/main/java/org/springframework/transaction/support/TransactionTemplate.java",
    VER / "analyzed/spring-aop/src/main/java/org/springframework/aop/TargetSource.java",
]

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}


def _load(name: str, attr: str) -> dict:
    spec = importlib.util.spec_from_file_location(name.replace(".py", ""), SCRIPTS / name)
    mod = importlib.util.module_from_spec(spec)
    assert spec.loader is not None
    spec.loader.exec_module(mod)
    return getattr(mod, attr)


for mod_name, attr in [
    ("wave20b_replacements_annotation.py", "ANNOTATION_REPLACEMENTS"),
    ("wave20b_replacements_autoproxy.py", "AUTOPROXY_REPLACEMENTS"),
    ("wave20b_replacements_config.py", "CONFIG_REPLACEMENTS"),
]:
    FILE_REPLACEMENTS.update(_load(mod_name, attr))


def apply_replacements(text: str, replacements: list[tuple[str, str]]) -> str:
    for old, new in replacements:
        if old not in text:
            raise ValueError(f"Pattern not found:\n{old[:120]}...")
        text = text.replace(old, new, 1)
    return text


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
    tree_count = tree_guard() if not failures and ok == len(BATCH_FILES) else 0
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
