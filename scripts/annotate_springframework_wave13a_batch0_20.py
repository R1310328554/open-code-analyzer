#!/usr/bin/env python3
"""Chinese-annotate Spring Framework 7.0.8 wave-13a batch [0:20]."""
from __future__ import annotations

import importlib.util
import json
import re
import shutil
import sys
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "springframework/7.0.8"
ORIGINAL = VER / "original"
ANALYZED = VER / "analyzed"
QUEUE = VER / "_reports/class-queue"
SCRIPTS = ROOT / "scripts"
# Wave-12 slice [0:20] — fixed list (batch.json may advance to later waves).
BATCH_FILES = [
    "spring-context/src/main/java/org/springframework/scheduling/support/NoOpTaskScheduler.java",
    "spring-context/src/main/java/org/springframework/scheduling/support/PeriodicTrigger.java",
    "spring-context/src/main/java/org/springframework/scheduling/support/QuartzCronField.java",
    "spring-context/src/main/java/org/springframework/scheduling/support/ScheduledMethodRunnable.java",
    "spring-context/src/main/java/org/springframework/scheduling/support/ScheduledTaskObservationContext.java",
    "spring-context/src/main/java/org/springframework/scheduling/support/ScheduledTaskObservationConvention.java",
    "spring-context/src/main/java/org/springframework/scheduling/support/ScheduledTaskObservationDocumentation.java",
    "spring-context/src/main/java/org/springframework/scheduling/support/SimpleTriggerContext.java",
    "spring-context/src/main/java/org/springframework/scheduling/support/TaskUtils.java",
    "spring-context/src/main/java/org/springframework/scripting/ScriptCompilationException.java",
    "spring-context/src/main/java/org/springframework/scripting/ScriptEvaluator.java",
    "spring-context/src/main/java/org/springframework/scripting/ScriptFactory.java",
    "spring-context/src/main/java/org/springframework/scripting/ScriptSource.java",
    "spring-context/src/main/java/org/springframework/scripting/bsh/BshScriptEvaluator.java",
    "spring-context/src/main/java/org/springframework/scripting/bsh/BshScriptFactory.java",
    "spring-context/src/main/java/org/springframework/scripting/bsh/BshScriptUtils.java",
    "spring-context/src/main/java/org/springframework/scripting/config/LangNamespaceHandler.java",
    "spring-context/src/main/java/org/springframework/scripting/config/LangNamespaceUtils.java",
    "spring-context/src/main/java/org/springframework/scripting/config/ScriptBeanDefinitionParser.java",
    "spring-context/src/main/java/org/springframework/scripting/config/ScriptingDefaultsParser.java",
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
    ("wave13a_support_scheduling.py", "SUPPORT_SCHEDULING_REPLACEMENTS"),
    ("wave13a_quartz_cron.py", "QUARTZ_CRON_FIELD_REPLACEMENTS"),
    ("wave13a_scripting_core.py", "SCRIPTING_CORE_REPLACEMENTS"),
    ("wave13a_scripting_bsh_config.py", "SCRIPTING_BSH_CONFIG_REPLACEMENTS"),
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
    print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False, indent=2))
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
