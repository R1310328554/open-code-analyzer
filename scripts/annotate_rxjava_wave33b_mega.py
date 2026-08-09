#!/usr/bin/env python3
"""Chinese-annotate RxJava 4.0.0-alpha-21 wave33b mega batch [7:14]."""
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
SCRIPTS = ROOT / "scripts"
SCRIPT_NAME = "annotate_rxjava_wave33b_mega.py"
MARK_NOTE = "wave33b mega [7:14]"
INDEX_FILE = Path("/var/tmp/rx33b.index")

BATCH_FILES = [
    ln.strip()
    for ln in Path("/tmp/rxjava_w33b.txt").read_text(encoding="utf-8").splitlines()
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


FILE_REPLACEMENTS.update(_load("wave33b_replacements_rxjava_mega.py", "FILE_REPLACEMENTS"))


def apply_replacements(text: str, replacements: list[tuple[str, str]]) -> str:
    for old, new in replacements:
        if old not in text:
            raise ValueError(f"Pattern not found:\n{old[:120]}...")
        text = text.replace(old, new, 1)
    return text


def refine_placeholders(text: str) -> str:
    """Polish auto-generated placeholder Chinese without wiping existing annotations."""
    text = re.sub(
        r"\t \* 方法 `(\w+)`：完成本类中与「[^」]+」相关的职责。",
        r"\t * 执行 \1 方法的核心逻辑。",
        text,
    )
    text = re.sub(
        r"\t \* 执行（方法 `(\w+)`）。",
        r"\t * 执行 \1 回调并处理 Reactive Streams 生命周期。",
        text,
    )
    text = re.sub(
        r"\t \* 内部实现类 (\w+)。",
        r"\t * 内部 \1 实现：协调上游/下游信号与背压。",
        text,
    )
    text = text.replace(
        "/** 订阅核心逻辑：组装内部 Subscriber 并连接上游。 */",
        "/** 组装内部 Subscriber/Observer 并订阅上游。 */",
    )
    text = text.replace(
        "/** 内部实现类 ",
        "/** 内部 ",
    )
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
    env = os.environ.copy()
    env["GIT_INDEX_FILE"] = str(INDEX_FILE)
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
    INDEX_FILE.unlink(missing_ok=True)
    subprocess.run(["git", "-C", str(ROOT), "reset", "--hard", "HEAD"], check=True)
    return commit, tree_count


def push_main_with_retry(max_attempts: int = 5) -> None:
    for attempt in range(max_attempts):
        subprocess.run(["git", "-C", str(ROOT), "fetch", "origin", "main"], check=True)
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
                    subprocess.run(
                        ["git", "-C", str(ROOT), "rebase", "origin/main"],
                        check=True,
                    )
            time.sleep(wait)
            continue
        raise RuntimeError(f"git push failed: {combined}")
    raise RuntimeError("git push failed after retries")


def annotate_batch() -> tuple[int, list[str], dict[str, int]]:
    failures: list[str] = []
    cjk_counts: dict[str, int] = {}
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
            text = refine_placeholders(text)
            cn = len(re.findall(r"[\u4e00-\u9fff]", text))
            lic = "Licensed under the Apache License" in text
            oca = "OCA 中文解析" in text
            if cn < 100 or not lic or not oca or not has_chinese(text):
                failures.append(f"VALIDATION cn={cn} lic={lic} oca={oca}: {rel}")
                continue
            dst.write_text(text, encoding="utf-8")
            cjk_counts[rel] = cn
            ok += 1
            print(f"OK cn={cn} {rel} (base={src.parent.name})")
        except Exception as e:
            failures.append(f"{rel}: {e}")
            print(f"FAIL {rel}: {e}")
    return ok, failures, cjk_counts


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
    if len(BATCH_FILES) != 7:
        print(json.dumps({"error": f"expected 7 files, got {len(BATCH_FILES)}"}, ensure_ascii=False))
        return 1

    subprocess.run(["git", "-C", str(ROOT), "fetch", "origin", "main"], check=True)
    subprocess.run(["git", "-C", str(ROOT), "checkout", "-f", "main"], check=True)
    subprocess.run(["git", "-C", str(ROOT), "reset", "--hard", "origin/main"], check=True)

    ok, failures, cjk_counts = annotate_batch()
    if failures or ok != len(BATCH_FILES):
        print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False, indent=2))
        return 1

    replacement_modules = [
        "scripts/wave33b_replacements_rxjava_mega.py",
        "scripts/gen_wave33b_rxjava_replacements.py",
    ]
    analyzed_paths = [f"rxjava/4.0.0-alpha-21/analyzed/{rel}" for rel in BATCH_FILES]
    script_paths = [f"scripts/{SCRIPT_NAME}", *replacement_modules]
    sha, tree_count = isolated_index_commit(
        "rxjava 4.0.0-alpha-21: Chinese-annotate wave33b mega [7:14]",
        [*analyzed_paths, *script_paths],
    )
    push_main_with_retry()

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
        "queue: mark rxjava 4.0.0-alpha-21 wave33b mega [7:14] done",
        queue_paths,
        base_ref="HEAD",
    )
    push_main_with_retry()

    chinese = confirm_chinese_on_origin()
    verified = sum(chinese.values())
    print(
        json.dumps(
            {
                "sha": sha,
                "queue_sha": queue_sha,
                "tree_count": tree_count,
                "cjk_per_file": cjk_counts,
                "chinese_confirmed": chinese,
                "all_chinese": all(chinese.values()),
                "ok": ok,
                "total": len(BATCH_FILES),
                "verified": f"{verified}/7",
            },
            ensure_ascii=False,
            indent=2,
        )
    )
    return 0 if verified == 7 else 1


if __name__ == "__main__":
    raise SystemExit(main())
