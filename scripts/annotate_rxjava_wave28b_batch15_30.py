#!/usr/bin/env python3
"""Chinese-annotate RxJava 4.0.0-alpha-21 wave-28b subscribers/module/jmh [15:30]."""
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
VER = ROOT / "rxjava/4.0.0-alpha-21"
ORIGINAL = VER / "original"
ANALYZED = VER / "analyzed"
QUEUE = VER / "_reports/class-queue"
SCRIPTS = ROOT / "scripts"
WAVE28B_FILE = Path("/tmp/rxjava_w28b.txt")
SCRIPT_NAME = "annotate_rxjava_wave28b_batch15_30.py"
MARK_NOTE = "wave28b [15:30]"
BATCH_FILES = [
    ln.strip()
    for ln in WAVE28B_FILE.read_text(encoding="utf-8").splitlines()
    if ln.strip()
]

GUARD_FILES = [
    VER
    / "analyzed/src/main/java/io/reactivex/rxjava4/internal/operators/flowable/FlowableSamplePublisher.java",
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/TransactionDefinition.java",
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/interceptor/RollbackRuleAttribute.java",
]

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}


def _load(name: str, attr: str) -> dict:
    spec = importlib.util.spec_from_file_location(name.replace(".py", ""), SCRIPTS / name)
    mod = importlib.util.module_from_spec(spec)
    assert spec.loader is not None
    spec.loader.exec_module(mod)
    return getattr(mod, attr)


for mod_name, attr in [
    ("wave28b_replacements_subscribers.py", "SUBSCRIBERS_W28B_REPLACEMENTS"),
    ("wave28b_replacements_module_jmh.py", "MODULE_JMH_W28B_REPLACEMENTS"),
]:
    FILE_REPLACEMENTS.update(_load(mod_name, attr))


def has_chinese(text: str) -> bool:
    return bool(re.search(r"[\u4e00-\u9fff]", text))


def tree_guard(env: dict[str, str] | None = None) -> int:
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


def apply_replacements(text: str, replacements: list[tuple[str, str]]) -> str:
    for old, new in replacements:
        if old not in text:
            raise ValueError(f"Pattern not found:\n{old[:120]}...")
        text = text.replace(old, new, 1)
    return text


def annotate_file(rel: str) -> None:
    name = Path(rel).name
    src = ORIGINAL / rel
    dst = ANALYZED / rel
    reps = FILE_REPLACEMENTS.get(rel) or FILE_REPLACEMENTS.get(name, [])
    if not reps:
        raise ValueError(f"NO_REPLACEMENTS: {rel}")
    dst.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(src, dst)
    text = apply_replacements(dst.read_text(encoding="utf-8"), reps)
    cn = len(re.findall(r"[\u4e00-\u9fff]", text))
    if cn < 10 or "Licensed under the Apache License" not in text:
        raise ValueError(f"VALIDATION cn={cn}: {rel}")
    dst.write_text(text, encoding="utf-8")


def isolated_index_commit(
    message: str, paths: list[str], base_ref: str = "origin/main"
) -> tuple[str, int]:
    index_file = Path("/tmp/git-index-rxjava-w28b")
    env = os.environ.copy()
    env["GIT_INDEX_FILE"] = str(index_file)
    base = subprocess.check_output(
        ["git", "-C", str(ROOT), "rev-parse", base_ref], text=True, env=env
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
    tree = subprocess.check_output(
        ["git", "-C", str(ROOT), "write-tree"], env=env, text=True
    ).strip()
    commit = subprocess.check_output(
        ["git", "-C", str(ROOT), "commit-tree", tree, "-p", base, "-m", message],
        text=True,
        env=env,
    ).strip()
    subprocess.run(
        ["git", "-C", str(ROOT), "update-ref", "refs/heads/main", commit], check=True
    )
    index_file.unlink(missing_ok=True)
    subprocess.run(["git", "-C", str(ROOT), "reset", "--hard", "HEAD"], check=True)
    return commit, tree_count


def push_main(retries: int = 4) -> None:
    r = subprocess.CompletedProcess([], 1)
    for attempt in range(retries):
        subprocess.run(["git", "-C", str(ROOT), "fetch", "origin", "main"], check=True)
        local = subprocess.check_output(
            ["git", "-C", str(ROOT), "rev-parse", "main"], text=True
        ).strip()
        remote = subprocess.check_output(
            ["git", "-C", str(ROOT), "rev-parse", "origin/main"], text=True
        ).strip()
        if local != remote:
            merge_base = subprocess.check_output(
                ["git", "-C", str(ROOT), "merge-base", local, remote], text=True
            ).strip()
            if merge_base != remote:
                raise RuntimeError(
                    f"main diverged from origin/main (local={local[:8]} remote={remote[:8]})"
                )
        r = subprocess.run(
            ["git", "-C", str(ROOT), "push", "-u", "origin", "main"],
            capture_output=True,
            text=True,
        )
        if r.returncode == 0:
            return
        if attempt + 1 < retries:
            time.sleep(4 * (2**attempt))
    raise subprocess.CalledProcessError(r.returncode, r.args, r.stdout, r.stderr)


def confirm_chinese_on_origin() -> dict[str, bool]:
    subprocess.run(["git", "-C", str(ROOT), "fetch", "origin", "main"], check=True)
    result: dict[str, bool] = {}
    for rel in BATCH_FILES:
        blob = subprocess.check_output(
            [
                "git",
                "-C",
                str(ROOT),
                "show",
                f"origin/main:rxjava/4.0.0-alpha-21/analyzed/{rel}",
            ],
            text=True,
        )
        result[rel] = has_chinese(blob)
    return result


def main() -> int:
    if len(BATCH_FILES) != 15:
        raise RuntimeError(f"batch guard failed: expected 15 files, got {len(BATCH_FILES)}")

    subprocess.run(["git", "-C", str(ROOT), "fetch", "origin", "main"], check=True)
    subprocess.run(["git", "-C", str(ROOT), "checkout", "-f", "main"], check=True)
    subprocess.run(["git", "-C", str(ROOT), "reset", "--hard", "origin/main"], check=True)

    failures: list[str] = []
    for rel in BATCH_FILES:
        try:
            annotate_file(rel)
            print(f"OK {rel}")
        except Exception as e:
            failures.append(f"{rel}: {e}")
            print(f"FAIL {rel}: {e}")
    if failures:
        print(json.dumps({"ok": 0, "failures": failures}, ensure_ascii=False, indent=2))
        return 1

    analyzed_paths = [f"rxjava/4.0.0-alpha-21/analyzed/{rel}" for rel in BATCH_FILES]
    script_paths = [
        f"scripts/{SCRIPT_NAME}",
        "scripts/wave28b_replacements_subscribers.py",
        "scripts/wave28b_replacements_module_jmh.py",
    ]
    sha, tree_count = isolated_index_commit(
        "rxjava 4.0.0-alpha-21: Chinese-annotate wave 28b [15:30]",
        [*analyzed_paths, *script_paths],
    )
    push_main()

    subprocess.run(
        [
            sys.executable,
            str(SCRIPTS / "mark_batch_done.py"),
            "--project",
            "rxjava",
            "--version",
            "4.0.0-alpha-21",
            "--note",
            MARK_NOTE,
            *BATCH_FILES,
        ],
        check=True,
    )
    queue_paths = [
        "rxjava/4.0.0-alpha-21/_reports/class-queue/done.txt",
        "rxjava/4.0.0-alpha-21/_reports/class-queue/pending.txt",
        "rxjava/4.0.0-alpha-21/_reports/class-queue/batch.json",
        "rxjava/4.0.0-alpha-21/_reports/class-queue/worker.log",
    ]
    queue_sha, _ = isolated_index_commit(
        "queue: mark rxjava 4.0.0-alpha-21 wave28b done",
        queue_paths,
        base_ref="HEAD",
    )
    push_main()

    done_total = len(
        [ln for ln in (QUEUE / "done.txt").read_text(encoding="utf-8").splitlines() if ln.strip()]
    )
    pending_total = len(
        [
            ln
            for ln in (QUEUE / "pending.txt").read_text(encoding="utf-8").splitlines()
            if ln.strip()
        ]
    )
    chinese = confirm_chinese_on_origin()
    verified = sum(chinese.values())
    print(
        json.dumps(
            {
                "sha": sha,
                "queue_sha": queue_sha,
                "tree_count": tree_count,
                "done": done_total,
                "pending": pending_total,
                "chinese_confirmed": chinese,
                "all_chinese": all(chinese.values()),
                "verified": f"{verified}/15",
            },
            ensure_ascii=False,
            indent=2,
        )
    )
    return 0 if all(chinese.values()) and verified == 15 else 1


if __name__ == "__main__":
    raise SystemExit(main())
