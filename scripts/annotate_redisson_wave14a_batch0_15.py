#!/usr/bin/env python3
"""Chinese-annotate Redisson 4.7.0 wave-14a spring-data-22 [0:15]."""
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
VER = ROOT / "redisson/redisson-4.7.0"
ORIGINAL = VER / "original"
ANALYZED = VER / "analyzed"
QUEUE = VER / "_reports/class-queue"
SCRIPTS = ROOT / "scripts"
WAVE14A_FILE = Path("/tmp/redisson_w14a.txt")
SCRIPT_NAME = "annotate_redisson_wave14a_batch0_15.py"
MARK_NOTE = "wave14a [0:15]"
BATCH_FILES = [
    ln.strip()
    for ln in WAVE14A_FILE.read_text(encoding="utf-8").splitlines()
    if ln.strip()
]

GUARD_FILES = [
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/TransactionDefinition.java",
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/interceptor/RollbackRuleAttribute.java",
    ROOT
    / "redisson/redisson-4.7.0/analyzed/redisson-hibernate/redisson-hibernate-4/src/main/java/org/redisson/hibernate/strategy/BaseRegionAccessStrategy.java",
    ROOT
    / "rxjava/4.0.0-alpha-21/analyzed/src/main/java/io/reactivex/rxjava4/internal/operators/flowable/FlowableSamplePublisher.java",
]

_spec = importlib.util.spec_from_file_location(
    "wave14a_replacements_redisson",
    SCRIPTS / "wave14a_replacements_redisson.py",
)
_mod = importlib.util.module_from_spec(_spec)
assert _spec.loader is not None
_spec.loader.exec_module(_mod)
FILE_REPLACEMENTS = _mod.W14A_REPLACEMENTS


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


def annotate_files() -> tuple[int, list[str], dict[str, int]]:
    failures: list[str] = []
    cjk_counts: dict[str, int] = {}
    ok = 0
    for rel in BATCH_FILES:
        name = Path(rel).name
        src = ORIGINAL / rel
        dst = ANALYZED / rel
        if not src.exists():
            failures.append(f"MISSING original: {rel}")
            continue
        reps = FILE_REPLACEMENTS.get(rel, FILE_REPLACEMENTS.get(name, []))
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
            cjk_counts[rel] = cn
            ok += 1
            print(f"OK cn={cn} {rel}")
        except Exception as e:
            failures.append(f"{rel}: {e}")
            print(f"FAIL {rel}: {e}")
    return ok, failures, cjk_counts


def isolated_index_commit(
    message: str, paths: list[str], base_ref: str = "origin/main"
) -> tuple[str, int]:
    index_file = Path("/tmp/re14a.index")
    env = os.environ.copy()
    env["GIT_INDEX_FILE"] = str(index_file)
    base = subprocess.check_output(
        ["git", "-C", str(ROOT), "rev-parse", base_ref], text=True
    ).strip()
    subprocess.run(["git", "-C", str(ROOT), "read-tree", base], env=env, check=True)
    normal = [p for p in paths if not p.endswith("worker.log")]
    forced = [p for p in paths if p.endswith("worker.log")]
    if normal:
        subprocess.run(["git", "-C", str(ROOT), "add", "--", *normal], env=env, check=True)
    if forced:
        subprocess.run(["git", "-C", str(ROOT), "add", "-f", "--", *forced], env=env, check=True)
    tree_count = tree_guard(env)
    tree = subprocess.check_output(
        ["git", "-C", str(ROOT), "write-tree"], env=env, text=True
    ).strip()
    commit = subprocess.check_output(
        ["git", "-C", str(ROOT), "commit-tree", tree, "-p", base, "-m", message],
        text=True,
    ).strip()
    subprocess.run(["git", "-C", str(ROOT), "update-ref", "refs/heads/main", commit], check=True)
    index_file.unlink(missing_ok=True)
    return commit, tree_count


def push_main(retries: int = 4) -> None:
    for attempt in range(retries):
        r = subprocess.run(
            ["git", "-C", str(ROOT), "push", "-u", "origin", "main"],
            capture_output=True,
            text=True,
        )
        if r.returncode == 0:
            return
        if attempt + 1 < retries:
            subprocess.run(["git", "-C", str(ROOT), "fetch", "origin", "main"], check=True)
            subprocess.run(["git", "-C", str(ROOT), "reset", "--hard", "origin/main"], check=True)
            time.sleep(4 * (2**attempt))
    raise subprocess.CalledProcessError(r.returncode, r.args, r.stdout, r.stderr)


def confirm_chinese() -> dict[str, bool]:
    return {
        rel: has_chinese((ANALYZED / rel).read_text(encoding="utf-8")) for rel in BATCH_FILES
    }


def verify_origin_main() -> dict[str, bool]:
    result: dict[str, bool] = {}
    for rel in BATCH_FILES:
        path = f"redisson/redisson-4.7.0/analyzed/{rel}"
        blob = subprocess.check_output(
            ["git", "-C", str(ROOT), "show", f"origin/main:{path}"],
            text=True,
        )
        result[rel] = has_chinese(blob)
    return result


def main() -> int:
    if len(BATCH_FILES) != 15:
        print(json.dumps({"error": f"expected 15 files, got {len(BATCH_FILES)}"}, indent=2))
        return 1

    subprocess.run(["git", "-C", str(ROOT), "fetch", "origin", "main"], check=True)
    ok, failures, cjk_counts = annotate_files()
    if failures or ok != len(BATCH_FILES):
        print(
            json.dumps(
                {
                    "ok": ok,
                    "expected": len(BATCH_FILES),
                    "failures": failures,
                    "cjk_counts": cjk_counts,
                    "tree_count": 0,
                },
                ensure_ascii=False,
                indent=2,
            )
        )
        return 1

    analyzed_paths = [f"redisson/redisson-4.7.0/analyzed/{rel}" for rel in BATCH_FILES]
    script_paths = [
        f"scripts/{SCRIPT_NAME}",
        "scripts/wave14a_replacements_redisson.py",
    ]
    sha, tree_count = isolated_index_commit(
        "redisson redisson-4.7.0: Chinese-annotate wave 14a [0:15]",
        [*analyzed_paths, *script_paths],
    )
    push_main()

    subprocess.run(
        [
            sys.executable,
            str(SCRIPTS / "mark_batch_done.py"),
            "--project",
            "redisson",
            "--version",
            "redisson-4.7.0",
            "--note",
            MARK_NOTE,
            *BATCH_FILES,
        ],
        check=True,
    )
    queue_paths = [
        "redisson/redisson-4.7.0/_reports/class-queue/done.txt",
        "redisson/redisson-4.7.0/_reports/class-queue/pending.txt",
        "redisson/redisson-4.7.0/_reports/class-queue/batch.json",
        "redisson/redisson-4.7.0/_reports/class-queue/worker.log",
    ]
    queue_sha, _ = isolated_index_commit(
        f"queue: mark redisson redisson-4.7.0 {MARK_NOTE} done",
        queue_paths,
        base_ref="HEAD",
    )
    push_main()

    subprocess.run(["git", "-C", str(ROOT), "fetch", "origin", "main"], check=True)
    origin_chinese = verify_origin_main()
    done_total = len(
        [ln for ln in (QUEUE / "done.txt").read_text(encoding="utf-8").splitlines() if ln.strip()]
    )
    pending_total = len(
        [ln for ln in (QUEUE / "pending.txt").read_text(encoding="utf-8").splitlines() if ln.strip()]
    )
    chinese = confirm_chinese()
    print(
        json.dumps(
            {
                "sha": sha,
                "queue_sha": queue_sha,
                "tree_count": tree_count,
                "cjk_counts": cjk_counts,
                "done": done_total,
                "pending": pending_total,
                "annotated": ok,
                "expected": len(BATCH_FILES),
                "chinese_confirmed": chinese,
                "origin_main_chinese": origin_chinese,
                "all_chinese": all(origin_chinese.values()),
            },
            ensure_ascii=False,
            indent=2,
        )
    )
    return 0 if all(origin_chinese.values()) and ok == 15 else 1


if __name__ == "__main__":
    raise SystemExit(main())
