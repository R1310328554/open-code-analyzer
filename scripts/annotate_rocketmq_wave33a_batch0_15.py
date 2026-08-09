#!/usr/bin/env python3
"""Chinese-annotate RocketMQ rocketmq-all-5.5.0 wave33a remoting protocol header [0:15]."""
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
VER = ROOT / "rocketmq/rocketmq-all-5.5.0"
ORIGINAL = VER / "original"
ANALYZED = VER / "analyzed"
SCRIPTS = ROOT / "scripts"
BATCH_LIST = [
    ln.strip()
    for ln in Path("/tmp/rmq33a.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]
REPLACEMENTS_NAME = "wave33a_replacements_rocketmq_remoting_header.py"
MARK_NOTE = "wave33a [0:15]"
INDEX_FILE = Path("/var/tmp/rmq33a.index")

GUARD_FILES = [
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/TransactionDefinition.java",
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/interceptor/RollbackRuleAttribute.java",
    ROOT
    / "rxjava/4.0.0-alpha-21/analyzed/src/main/java/io/reactivex/rxjava4/internal/operators/flowable/FlowableSamplePublisher.java",
]

_spec = importlib.util.spec_from_file_location(
    "wave33a_replacements_rocketmq_remoting_header",
    SCRIPTS / REPLACEMENTS_NAME,
)
_mod = importlib.util.module_from_spec(_spec)
assert _spec and _spec.loader
_spec.loader.exec_module(_mod)
R: dict[str, list[tuple[str, str]]] = _mod.R


def has_chinese(text: str) -> bool:
    return bool(re.search(r"[\u4e00-\u9fff]", text))


def cjk_count(text: str) -> int:
    return len(re.findall(r"[\u4e00-\u9fff]", text))


def apply_replacements(rel: str) -> None:
    src = ORIGINAL / rel
    dst = ANALYZED / rel
    if not src.exists():
        raise SystemExit(f"Missing original: {rel}")
    dst.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(src, dst)
    text = dst.read_text(encoding="utf-8")
    src_text = src.read_text(encoding="utf-8")
    for old, new in R.get(rel, []):
        if old not in text:
            raise SystemExit(f"MISSING pattern in {rel}: {old[:80]!r}...")
        text = text.replace(old, new, 1)
    if not has_chinese(text):
        raise SystemExit(f"No Chinese in {rel} after annotation")
    if cjk_count(text) < 10:
        raise SystemExit(f"Insufficient CJK in {rel}: {cjk_count(text)}")
    if "Licensed to the Apache Software Foundation" in src_text:
        if "Licensed to the Apache Software Foundation" not in text:
            raise SystemExit(f"License missing in {rel}")
    dst.write_text(text, encoding="utf-8")


def tree_guard(env: dict[str, str] | None = None) -> int:
    tracked = len(subprocess.check_output(["git", "-C", str(ROOT), "ls-files"], env=env).splitlines())
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
    env = os.environ.copy()
    env["GIT_INDEX_FILE"] = str(INDEX_FILE)
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
    tree = subprocess.check_output(["git", "-C", str(ROOT), "write-tree"], env=env, text=True).strip()
    commit = subprocess.check_output(
        ["git", "-C", str(ROOT), "commit-tree", tree, "-p", base, "-m", message],
        text=True,
    ).strip()
    subprocess.run(["git", "-C", str(ROOT), "update-ref", "refs/heads/main", commit], check=True)
    INDEX_FILE.unlink(missing_ok=True)
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


def verify_origin_main() -> dict[str, bool]:
    result: dict[str, bool] = {}
    for rel in BATCH_LIST:
        path = f"rocketmq/rocketmq-all-5.5.0/analyzed/{rel}"
        blob = subprocess.check_output(
            ["git", "-C", str(ROOT), "show", f"origin/main:{path}"],
            text=True,
        )
        result[rel] = has_chinese(blob)
    return result


def main() -> int:
    subprocess.run(["git", "-C", str(ROOT), "fetch", "origin", "main"], check=True)
    subprocess.run(["git", "-C", str(ROOT), "reset", "--hard", "origin/main"], check=True)
    if len(BATCH_LIST) != 15:
        raise SystemExit(f"Expected 15 batch files, got {len(BATCH_LIST)}")
    failures: list[str] = []
    for rel in BATCH_LIST:
        try:
            apply_replacements(rel)
            print(f"OK {rel} cjk={cjk_count((ANALYZED / rel).read_text(encoding='utf-8'))}")
        except Exception as e:
            failures.append(f"{rel}: {e}")
            print(f"FAIL {rel}: {e}")
    if failures:
        return 1

    analyzed_paths = [f"rocketmq/rocketmq-all-5.5.0/analyzed/{rel}" for rel in BATCH_LIST]
    script_paths = [
        f"scripts/{REPLACEMENTS_NAME}",
        "scripts/annotate_rocketmq_wave33a_batch0_15.py",
    ]
    sha, tree_count = isolated_index_commit(
        "Annotate: rocketmq rocketmq-all-5.5.0: Chinese-annotate wave 33a [0:15]",
        analyzed_paths + script_paths,
    )
    push_main()

    subprocess.run(
        [
            sys.executable,
            str(SCRIPTS / "mark_batch_done.py"),
            "--project",
            "rocketmq",
            "--version",
            "rocketmq-all-5.5.0",
            "--note",
            MARK_NOTE,
            *BATCH_LIST,
        ],
        check=True,
    )
    queue_paths = [
        "rocketmq/rocketmq-all-5.5.0/_reports/class-queue/done.txt",
        "rocketmq/rocketmq-all-5.5.0/_reports/class-queue/pending.txt",
        "rocketmq/rocketmq-all-5.5.0/_reports/class-queue/batch.json",
        "rocketmq/rocketmq-all-5.5.0/_reports/class-queue/worker.log",
    ]
    queue_sha, _ = isolated_index_commit(
        "queue: mark rocketmq rocketmq-all-5.5.0 wave33a [0:15] done",
        queue_paths,
        base_ref="HEAD",
    )
    push_main()

    subprocess.run(["git", "-C", str(ROOT), "fetch", "origin", "main"], check=True)
    origin_chinese = verify_origin_main()
    counts = {rel: cjk_count((ANALYZED / rel).read_text(encoding="utf-8")) for rel in BATCH_LIST}
    print(
        json.dumps(
            {
                "sha": sha,
                "queue_sha": queue_sha,
                "tree_count": tree_count,
                "cjk_counts": counts,
                "cjk_total": sum(counts.values()),
                "origin_main_chinese": origin_chinese,
                "all_chinese": all(origin_chinese.values()),
                "batch_count": len(BATCH_LIST),
            },
            ensure_ascii=False,
            indent=2,
        )
    )
    return 0 if all(origin_chinese.values()) and len(BATCH_LIST) == 15 else 1


if __name__ == "__main__":
    raise SystemExit(main())
