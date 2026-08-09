#!/usr/bin/env python3
"""Chinese-annotate Spring Boot 4.1.0 wave-16a slice [0:6] (properties/bind/convert/json/logging)."""
from __future__ import annotations

import importlib.util
import json
import re
import shutil
import sys
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "springboot/4.1.0"
ORIGINAL = VER / "original"
ANALYZED = VER / "analyzed"
SCRIPTS = ROOT / "scripts"
BATCH_FILES = Path("/tmp/springboot_w16a.txt").read_text(encoding="utf-8").strip().splitlines()


def _load(name: str, attr: str) -> list[tuple[str, str]]:
    path = SCRIPTS / name
    spec = importlib.util.spec_from_file_location(name.replace(".py", ""), path)
    mod = importlib.util.module_from_spec(spec)
    assert spec.loader is not None
    spec.loader.exec_module(mod)
    return getattr(mod, attr)


FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "PropertyMapper.java": _load("wave16a_replacements_property_mapper.py", "PROPERTY_MAPPER_REPLACEMENTS"),
    "Binder.java": _load("wave16a_replacements_binder.py", "BINDER_REPLACEMENTS"),
    "ApplicationConversionService.java": _load(
        "wave16a_replacements_convert_json.py", "APPLICATION_CONVERSION_SERVICE_REPLACEMENTS"
    ),
    "JsonValueWriter.java": _load("wave16a_replacements_convert_json.py", "JSON_VALUE_WRITER_REPLACEMENTS"),
    "StandardStackTracePrinter.java": _load(
        "wave16a_replacements_logging.py", "STANDARD_STACK_TRACE_PRINTER_REPLACEMENTS"
    ),
    "Log4J2LoggingSystem.java": _load("wave16a_replacements_logging.py", "LOG4J2_LOGGING_SYSTEM_REPLACEMENTS"),
}


def apply_replacements(text: str, replacements: list[tuple[str, str]]) -> str:
    for old, new in replacements:
        if old not in text:
            raise ValueError(f"Pattern not found:\n{old[:160]}...")
        text = text.replace(old, new, 1)
    return text


def has_chinese(text: str) -> bool:
    return bool(re.search(r"[\u4e00-\u9fff]", text))


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
    print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False, indent=2))
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
