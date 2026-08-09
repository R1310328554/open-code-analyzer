#!/usr/bin/env python3
"""Refine Chinese annotations for Spring Framework 7.0.8 wave30a mega batch [0:10]."""
from __future__ import annotations

import importlib.util
import json
import os
import re
import shutil
import subprocess
import sys
import time
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "springframework/7.0.8"
ORIGINAL = VER / "original"
ANALYZED = VER / "analyzed"
SCRIPTS = ROOT / "scripts"
SCRIPT_NAME = "annotate_springframework_wave30a_batch0_10.py"
MARK_NOTE = "wave30a mega [0:10]"

BATCH_FILES = [
    ln.strip()
    for ln in Path("/tmp/sf_w30a.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]

GUARD_FILES = [
    VER
    / "analyzed/spring-tx/src/main/java/org/springframework/transaction/TransactionDefinition.java",
    VER
    / "analyzed/spring-tx/src/main/java/org/springframework/transaction/interceptor/RollbackRuleAttribute.java",
    ROOT
    / "rxjava/4.0.0-alpha-21/analyzed/src/main/java/io/reactivex/rxjava4/internal/operators/flowable/FlowableSamplePublisher.java",
]

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}


def _load(name: str, attr: str) -> dict:
    spec = importlib.util.spec_from_file_location(name.replace(".py", ""), SCRIPTS / name)
    mod = importlib.util.module_from_spec(spec)
    assert spec.loader is not None
    spec.loader.exec_module(mod)
    return getattr(mod, attr)


FILE_REPLACEMENTS.update(_load("wave30a_replacements_mega.py", "MEGA_REPLACEMENTS"))


def apply_replacements(text: str, replacements: list[tuple[str, str]]) -> str:
    for old, new in replacements:
        if old not in text:
            raise ValueError(f"Pattern not found:\n{old[:120]}...")
        text = text.replace(old, new, 1)
    return text


def has_chinese(text: str) -> bool:
    return bool(re.search(r"[\u4e00-\u9fff]", text))


def pick_source(rel: str) -> Path:
    """Prefer existing analyzed content when it already carries Chinese annotations."""
    analyzed = ANALYZED / rel
    if analyzed.exists() and has_chinese(analyzed.read_text(encoding="utf-8")):
        return analyzed
    original = ORIGINAL / rel
    if not original.exists():
        raise FileNotFoundError(f"MISSING original: {rel}")
    return original


def tree_guard(env: dict[str, str] | None = None) -> int:
    if env is None:
        tracked = len(subprocess.check_output(["git", "-C", str(ROOT), "ls-files"]).splitlines())
    else:
        tracked = len(
            subprocess.check_output(["git", "-C", str(ROOT), "ls-files"], env=env).splitlines()
        )
    if tracked < 50000:
        raise RuntimeError(f"tree guard failed: tracked={tracked} (expected >=50000)")
    for path in GUARD_FILES:
        if env is None:
            if not path.exists():
                raise RuntimeError(f"guard file missing: {path}")
            blob = path.read_text(encoding="utf-8")
        else:
            rel = path.relative_to(ROOT)
            blob = subprocess.check_output(
                ["git", "-C", str(ROOT), "show", f":{rel}"], env=env, text=True
            )
        if not has_chinese(blob):
            raise RuntimeError(f"guard file lacks Chinese: {path}")
    return tracked


def isolated_index_commit(message: str, paths: list[str], base_ref: str = "origin/main") -> tuple[str, int]:
    index_file = Path("/tmp/git-index-sf-w30a")
    env = os.environ.copy()
    env["GIT_INDEX_FILE"] = str(index_file)
    base = subprocess.check_output(
        ["git", "-C", str(ROOT), "rev-parse", base_ref], text=True
    ).strip()
    subprocess.run(["git", "-C", str(ROOT), "read-tree", base], env=env, check=True)
    tree_before = subprocess.check_output(
        ["git", "-C", str(ROOT), "write-tree"], env=env, text=True
    ).strip()
    tree_count = len(
        subprocess.check_output(
            ["git", "-C", str(ROOT), "ls-tree", "-r", "--name-only", tree_before],
            env=env,
            text=True,
        ).splitlines()
    )
    if tree_count < 50000:
        raise RuntimeError(f"read-tree guard failed: tree_count={tree_count} (expected >=50000)")
    subprocess.run(["git", "-C", str(ROOT), "add", "--", *paths], env=env, check=True)
    tree_guard(env)
    tree = subprocess.check_output(["git", "-C", str(ROOT), "write-tree"], env=env, text=True).strip()
    commit = subprocess.check_output(
        ["git", "-C", str(ROOT), "commit-tree", tree, "-p", base, "-m", message],
        text=True,
    ).strip()
    subprocess.run(["git", "-C", str(ROOT), "update-ref", "refs/heads/main", commit], check=True)
    index_file.unlink(missing_ok=True)
    return commit, tree_count


def push_main_with_retry(max_attempts: int = 5) -> None:
    for attempt in range(max_attempts):
        result = subprocess.run(
            ["git", "-C", str(ROOT), "push", "-u", "origin", "main"],
            capture_output=True,
            text=True,
        )
        if result.returncode == 0:
            return
        combined = result.stdout + result.stderr
        if "non-fast-forward" in combined or "fetch first" in combined:
            wait = 4 * (2**attempt)
            subprocess.run(["git", "-C", str(ROOT), "fetch", "origin", "main"], check=True)
            local = subprocess.check_output(
                ["git", "-C", str(ROOT), "rev-parse", "main"], text=True
            ).strip()
            remote = subprocess.check_output(
                ["git", "-C", str(ROOT), "rev-parse", "origin/main"], text=True
            ).strip()
            if local != remote:
                subprocess.run(
                    ["git", "-C", str(ROOT), "rebase", "origin/main"],
                    check=True,
                )
            time.sleep(wait)
            continue
        raise RuntimeError(f"git push failed: {combined}")
    raise RuntimeError("git push failed after retries")


def annotate_batch() -> tuple[int, list[str]]:
    failures: list[str] = []
    ok = 0
    for rel in BATCH_FILES:
        name = Path(rel).name
        dst = ANALYZED / rel
        reps = FILE_REPLACEMENTS.get(name, [])
        if not reps:
            failures.append(f"NO_REPLACEMENTS: {rel}")
            continue
        try:
            src = pick_source(rel)
        except FileNotFoundError as e:
            failures.append(str(e))
            continue
        dst.parent.mkdir(parents=True, exist_ok=True)
        if src.resolve() != dst.resolve():
            shutil.copy2(src, dst)
        try:
            text = apply_replacements(dst.read_text(encoding="utf-8"), reps)
            cn = len(re.findall(r"[\u4e00-\u9fff]", text))
            lic = "Licensed under the Apache License" in text
            if cn < 50 or not lic:
                failures.append(f"VALIDATION cn={cn} lic={lic}: {rel}")
                continue
            dst.write_text(text, encoding="utf-8")
            ok += 1
            print(f"OK cn={cn} {rel} (base={src.parent.name})")
        except Exception as e:
            failures.append(f"{rel}: {e}")
            print(f"FAIL {rel}: {e}")
    return ok, failures


def confirm_chinese() -> dict[str, int]:
    out: dict[str, int] = {}
    for rel in BATCH_FILES:
        text = (ANALYZED / rel).read_text(encoding="utf-8")
        out[rel] = len(re.findall(r"[\u4e00-\u9fff]", text))
    return out


def main() -> int:
    if len(BATCH_FILES) != 10:
        print(json.dumps({"error": f"expected 10 files, got {len(BATCH_FILES)}"}, indent=2))
        return 1

    ok, failures = annotate_batch()
    if failures or ok != len(BATCH_FILES):
        print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False, indent=2))
        return 1

    replacement_modules = [
        "scripts/wave30a_replacements_mega.py",
    ]
    analyzed_paths = [f"springframework/7.0.8/analyzed/{rel}" for rel in BATCH_FILES]
    script_paths = [f"scripts/{SCRIPT_NAME}", *replacement_modules]
    sha, tree_count = isolated_index_commit(
        "zh-annotate springframework 7.0.8 wave30a mega [0:10]",
        [*analyzed_paths, *script_paths],
    )
    push_main_with_retry()

    subprocess.run(
        [
            sys.executable,
            str(SCRIPTS / "mark_batch_done.py"),
            "--project",
            "springframework",
            "--version",
            "7.0.8",
            "--note",
            MARK_NOTE,
            *BATCH_FILES,
        ],
        check=True,
    )
    queue_paths = [
        "springframework/7.0.8/_reports/class-queue/done.txt",
        "springframework/7.0.8/_reports/class-queue/pending.txt",
        "springframework/7.0.8/_reports/class-queue/batch.json",
        "springframework/7.0.8/_reports/class-queue/worker.log",
    ]
    queue_sha, _ = isolated_index_commit(
        "queue: mark springframework 7.0.8 wave30a mega [0:10] done",
        queue_paths,
        base_ref="HEAD",
    )
    push_main_with_retry()

    chinese = confirm_chinese()
    print(
        json.dumps(
            {
                "sha": sha,
                "queue_sha": queue_sha,
                "tree_count": tree_count,
                "chinese_counts": chinese,
                "all_strong_cjk": all(v >= 50 for v in chinese.values()),
                "ok": ok,
                "total": len(BATCH_FILES),
            },
            ensure_ascii=False,
            indent=2,
        )
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
