#!/usr/bin/env python3
"""Chinese-annotate Spring Framework 7.0.8 wave-14a batch [0:20]."""
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
    "spring-context/src/main/java/org/springframework/validation/AbstractErrors.java",
    "spring-context/src/main/java/org/springframework/validation/AbstractPropertyBindingResult.java",
    "spring-context/src/main/java/org/springframework/validation/BeanPropertyBindingResult.java",
    "spring-context/src/main/java/org/springframework/validation/BindException.java",
    "spring-context/src/main/java/org/springframework/validation/BindingErrorProcessor.java",
    "spring-context/src/main/java/org/springframework/validation/BindingResult.java",
    "spring-context/src/main/java/org/springframework/validation/BindingResultUtils.java",
    "spring-context/src/main/java/org/springframework/validation/DefaultBindingErrorProcessor.java",
    "spring-context/src/main/java/org/springframework/validation/DefaultMessageCodesResolver.java",
    "spring-context/src/main/java/org/springframework/validation/DirectFieldBindingResult.java",
    "spring-context/src/main/java/org/springframework/validation/Errors.java",
    "spring-context/src/main/java/org/springframework/validation/FieldError.java",
    "spring-context/src/main/java/org/springframework/validation/MapBindingResult.java",
    "spring-context/src/main/java/org/springframework/validation/MessageCodeFormatter.java",
    "spring-context/src/main/java/org/springframework/validation/MessageCodesResolver.java",
    "spring-context/src/main/java/org/springframework/validation/ObjectError.java",
    "spring-context/src/main/java/org/springframework/validation/SimpleErrors.java",
    "spring-context/src/main/java/org/springframework/validation/SmartValidator.java",
    "spring-context/src/main/java/org/springframework/validation/TypedValidator.java",
    "spring-context/src/main/java/org/springframework/validation/ValidationUtils.java",
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
    ("wave14a_validation_binding.py", "VALIDATION_BINDING_REPLACEMENTS"),
    ("wave14a_validation_errors.py", "VALIDATION_ERRORS_REPLACEMENTS"),
    ("wave14a_validation_rest_part1.py", "VALIDATION_REST_REPLACEMENTS"),
    ("wave14a_validation_rest_part2.py", "VALIDATION_REST_PART2_REPLACEMENTS"),
]:
    FILE_REPLACEMENTS.update(_load_module(mod_name, attr))


def apply_replacements(text: str, replacements: list[tuple[str, str]]) -> str:
    for old, new in replacements:
        if old not in text:
            raise ValueError(f"Pattern not found:\n{old[:120]}...")
        text = text.replace(old, new, 1)
    return text


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
    print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False, indent=2))
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
