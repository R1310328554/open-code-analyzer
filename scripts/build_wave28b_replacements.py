#!/usr/bin/env python3
"""Generate and validate wave28b Chinese replacement modules."""
from __future__ import annotations

import importlib.util
import re
from pathlib import Path

ROOT = Path("/workspace")
ORIGINAL = ROOT / "springframework/7.0.8/original"
ANALYZED = ROOT / "springframework/7.0.8/analyzed"
SCRIPTS = ROOT / "scripts"

BATCH_FILES = [
    ln.strip()
    for ln in Path("/tmp/sf_w28b.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]


def _load(name: str, attr: str) -> dict:
    spec = importlib.util.spec_from_file_location(name.replace(".py", ""), SCRIPTS / name)
    mod = importlib.util.module_from_spec(spec)
    assert spec.loader is not None
    spec.loader.exec_module(mod)
    return getattr(mod, attr)


def all_replacements() -> dict[str, list[tuple[str, str]]]:
    merged: dict[str, list[tuple[str, str]]] = {}
    for name, attr in [
        ("wave28b_replacements_incrementer.py", "INCREMENTER_REPLACEMENTS"),
        ("wave28b_replacements_incrementer_part2.py", "INCREMENTER_PART2"),
        ("wave28b_replacements_support.py", "SUPPORT_REPLACEMENTS"),
        ("wave28b_replacements_support_part2.py", "SUPPORT_PART2"),
        ("wave28b_replacements_lob_handler.py", "LOB_HANDLER_REPLACEMENTS"),
    ]:
        data = _load(name, attr)
        for key, pairs in data.items():
            merged.setdefault(key, []).extend(pairs)
    return merged


def apply_replacements(text: str, replacements: list[tuple[str, str]]) -> str:
    for old, new in replacements:
        if old not in text:
            raise ValueError(f"Pattern not found:\n{old[:120]}...")
        text = text.replace(old, new, 1)
    return text


def validate_all() -> tuple[int, list[str]]:
    all_reps = all_replacements()
    failures: list[str] = []
    ok = 0
    for rel in BATCH_FILES:
        name = Path(rel).name
        src = ORIGINAL / rel
        reps = all_reps.get(name, [])
        if not reps:
            failures.append(f"NO_REPLACEMENTS: {rel}")
            continue
        try:
            text = apply_replacements(src.read_text(encoding="utf-8"), reps)
            cn = len(re.findall(r"[\u4e00-\u9fff]", text))
            if cn < 10:
                failures.append(f"LOW_CN={cn}: {rel}")
                continue
            if "Licensed under the Apache License" not in text:
                failures.append(f"NO_LICENSE: {rel}")
                continue
            ok += 1
            print(f"OK cn={cn} {rel}")
        except Exception as e:
            failures.append(f"{rel}: {e}")
    missing = set(Path(rel).name for rel in BATCH_FILES) - set(all_reps)
    for name in sorted(missing):
        failures.append(f"NO_MODULE: {name}")
    print(f"Modules cover {len(all_reps)} files, batch has {len(BATCH_FILES)} files")
    return ok, failures


if __name__ == "__main__":
    ok, failures = validate_all()
    if failures or ok != len(BATCH_FILES):
        print("FAILURES:", failures)
        raise SystemExit(1)
    print(f"All {ok}/{len(BATCH_FILES)} files validated.")
