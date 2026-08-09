#!/usr/bin/env python3
"""Chinese-annotate Spring Boot 4.1.0 wave17a mega file ConfigurationPropertyName."""
from __future__ import annotations

import importlib.util
import json
import os
import re
import shutil
import subprocess
import sys
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "springboot/4.1.0"
ORIGINAL = VER / "original"
ANALYZED = VER / "analyzed"
SCRIPTS = ROOT / "scripts"
QUEUE = VER / "_reports/class-queue"

REL = "core/spring-boot/src/main/java/org/springframework/boot/context/properties/source/ConfigurationPropertyName.java"
SCRIPT_NAME = "annotate_springboot_wave17a_ConfigurationPropertyName.py"
REPLACEMENTS_MODULE = "wave17a_replacements_configuration_property_name.py"

GUARD_FILES = [
    VER / "analyzed/core/spring-boot/src/main/java/org/springframework/boot/context/properties/PropertyMapper.java",
    VER / "analyzed/core/spring-boot/src/main/java/org/springframework/boot/util/LambdaSafe.java",
    VER / "analyzed/core/spring-boot/src/main/java/org/springframework/boot/web/error/ErrorPage.java",
    VER
    / "analyzed/core/spring-boot/src/main/java/org/springframework/boot/web/servlet/support/SpringBootServletInitializer.java",
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/TransactionDefinition.java",
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/interceptor/RollbackRuleAttribute.java",
]


def _load_replacements() -> list[tuple[str, str]]:
    path = SCRIPTS / REPLACEMENTS_MODULE
    spec = importlib.util.spec_from_file_location("w17a_cpn", path)
    mod = importlib.util.module_from_spec(spec)
    assert spec.loader is not None
    spec.loader.exec_module(mod)
    return mod.CONFIGURATION_PROPERTY_NAME_REPLACEMENTS


def has_chinese(text: str) -> bool:
    return bool(re.search(r"[\u4e00-\u9fff]", text))


def apply_replacements(text: str, replacements: list[tuple[str, str]]) -> str:
    for old, new in replacements:
        if old not in text:
            raise ValueError(f"Pattern not found:\n{old[:160]}...")
        text = text.replace(old, new, 1)
    return text


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


def isolated_index_commit(message: str, paths: list[str], base_ref: str = "origin/main") -> tuple[str, int]:
    index_file = Path("/tmp/git-index-springboot-w17a")
    env = os.environ.copy()
    env["GIT_INDEX_FILE"] = str(index_file)
    base = subprocess.check_output(
        ["git", "-C", str(ROOT), "rev-parse", base_ref], text=True
    ).strip()
    subprocess.run(["git", "-C", str(ROOT), "read-tree", base], env=env, check=True)
    subprocess.run(["git", "-C", str(ROOT), "add", "--", *paths], env=env, check=True)
    tree_count = len(
        subprocess.check_output(["git", "-C", str(ROOT), "ls-files"], env=env).splitlines()
    )
    if tree_count < 50000:
        raise RuntimeError(f"isolated tree guard failed: tracked={tree_count} (expected >=50000)")
    for gf in GUARD_FILES:
        rel = gf.relative_to(ROOT)
        blob = subprocess.check_output(
            ["git", "-C", str(ROOT), "show", f":{rel}"], env=env, text=True
        )
        if not has_chinese(blob):
            raise RuntimeError(f"isolated guard lacks Chinese: {rel}")
    tree = subprocess.check_output(["git", "-C", str(ROOT), "write-tree"], env=env, text=True).strip()
    commit = subprocess.check_output(
        ["git", "-C", str(ROOT), "commit-tree", tree, "-p", base, "-m", message],
        text=True,
    ).strip()
    subprocess.run(["git", "-C", str(ROOT), "update-ref", "refs/heads/main", commit], check=True)
    index_file.unlink(missing_ok=True)
    return commit, tree_count


def annotate() -> int:
    src = ORIGINAL / REL
    dst = ANALYZED / REL
    if not src.exists():
        raise FileNotFoundError(f"missing original: {REL}")
    dst.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(src, dst)
    reps = _load_replacements()
    text = apply_replacements(dst.read_text(encoding="utf-8"), reps)
    cn = len(re.findall(r"[\u4e00-\u9fff]", text))
    lic = "Licensed under the Apache License" in text
    if cn < 100 or not lic or not has_chinese(text):
        raise ValueError(f"validation failed cn={cn} lic={lic}")
    dst.write_text(text, encoding="utf-8")
    print(f"OK cn={cn} {REL}")
    return cn


def update_batch_counts() -> None:
    batch_path = QUEUE / "batch.json"
    if not batch_path.exists():
        return
    batch = json.loads(batch_path.read_text(encoding="utf-8"))
    done_path = QUEUE / "done.txt"
    pending_path = QUEUE / "pending.txt"
    batch["done"] = len([ln for ln in done_path.read_text(encoding="utf-8").splitlines() if ln.strip()])
    if pending_path.exists():
        batch["remaining_pending"] = len(
            [ln for ln in pending_path.read_text(encoding="utf-8").splitlines() if ln.strip()]
        )
    batch_path.write_text(json.dumps(batch, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def main() -> int:
    cn = annotate()
    tree_count = tree_guard()
    analyzed_rel = f"springboot/4.1.0/analyzed/{REL}"
    script_rel = f"scripts/{SCRIPT_NAME}"
    repl_rel = f"scripts/{REPLACEMENTS_MODULE}"
    msg = "zh-annotate springboot 4.1.0 wave17a ConfigurationPropertyName"
    sha, tree_count = isolated_index_commit(msg, [analyzed_rel, script_rel, repl_rel])
    subprocess.run(
        ["git", "-C", str(ROOT), "push", "-u", "origin", "main"],
        check=True,
    )
    subprocess.run(
        [
            sys.executable,
            str(SCRIPTS / "mark_batch_done.py"),
            "--project",
            "springboot",
            "--version",
            "4.1.0",
            "--note",
            "wave17a",
            REL,
        ],
        check=True,
    )
    update_batch_counts()
    queue_paths = [
        "springboot/4.1.0/_reports/class-queue/done.txt",
        "springboot/4.1.0/_reports/class-queue/batch.json",
        "springboot/4.1.0/_reports/class-queue/worker.log",
    ]
    queue_sha, _ = isolated_index_commit(
        "queue: mark springboot 4.1.0 wave17a ConfigurationPropertyName done",
        queue_paths,
        base_ref="HEAD",
    )
    subprocess.run(
        ["git", "-C", str(ROOT), "push", "origin", "main"],
        check=True,
    )
    print(
        json.dumps(
            {
                "sha": sha,
                "queue_sha": queue_sha,
                "tree_count": tree_count,
                "chinese_char_count": cn,
                "file": REL,
            },
            ensure_ascii=False,
            indent=2,
        )
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
