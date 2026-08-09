#!/usr/bin/env python3
"""Refine Chinese annotations for Spring Framework 7.0.8 wave32b batch [9:18]."""
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
SCRIPT_NAME = "annotate_springframework_wave32b_batch9_18.py"
MARK_NOTE = "wave32b [9:18]"
INDEX_FILE = Path("/tmp/sf32b.index")

BATCH_FILES = [
    ln.strip()
    for ln in Path("/tmp/sf_w32b.txt").read_text(encoding="utf-8").splitlines()
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


FILE_REPLACEMENTS.update(_load("wave32b_replacements_mega.py", "FILE_REPLACEMENTS"))


def apply_replacements(text: str, replacements: list[tuple[str, str]]) -> str:
    for old, new in replacements:
        if old not in text:
            raise ValueError(f"Pattern not found:\n{old[:120]}...")
        text = text.replace(old, new, 1)
    return text


def has_chinese(text: str) -> bool:
    return bool(re.search(r"[\u4e00-\u9fff]", text))


def refine_placeholders(text: str) -> str:
    text = re.sub(
        r"\t \* 方法 `(\w+)`：完成本类中与「[^」]+」相关的职责。",
        r"\t * 执行 \1 相关逻辑。",
        text,
    )
    text = re.sub(
        r"\t \* 执行核心逻辑：[^（]+（方法 `(\w+)`）。",
        r"\t * 执行 \1 核心逻辑。",
        text,
    )
    text = text.replace("/** 结果相关状态（`resultSet`）。 */", "/** 被包装的 ResultSet。 */")
    text = text.replace("/** 来源相关状态（`readOnlyDataSource`）。 */", "/** 只读事务 DataSource。 */")
    text = text.replace("/** 名称相关状态（`generatedKeysColumnNames`）。 */", "/** 生成键列名。 */")
    text = text.replace("/** `false`：该类的成员状态。 */", "/** 布尔配置标志。 */")
    text = text.replace("/** `sql`：该类的成员状态。 */", "/** SQL 语句。 */")
    text = text.replace("/** `sqlErrorCodes`：该类的成员状态。 */", "/** SQL 错误码配置。 */")
    text = text.replace(
        "/** `userProvidedErrorCodesFilePresent`：该类的成员状态。 */",
        "/** 是否提供用户错误码文件。 */",
    )
    return text


def extract_interface_method_javadocs(text: str) -> dict[str, str]:
    """Map method name -> javadoc block from interface source."""
    result: dict[str, str] = {}
    pattern = re.compile(
        r"(/\*\*.*?\*/)\s*\n\s*"
        r"(?:@\w+(?:\([^)]*\))?\s+\n\s*)*"
        r"(?:public\s+)?(?:<[^>]+>\s+)?(?:@\w+(?:\([^)]*\))?\s+)*"
        r"[\w<>,\?\[\]\s]+\s+(\w+)\s*\(",
        re.DOTALL,
    )
    for m in pattern.finditer(text):
        javadoc, name = m.group(1), m.group(2)
        if has_chinese(javadoc):
            result[name] = javadoc
    return result


def sync_rowset_impl_javadocs(impl_text: str, iface_javadocs: dict[str, str]) -> str:
    """Copy Chinese javadocs from SqlRowSet interface onto @see-only impl methods."""
    pattern = re.compile(
        r"/\*\*\n\t \* @see [^\n]+\n\t \*/\n\t@Override\n\t"
        r"(?:public\s+)?(?:final\s+)?"
        r"(?:<[^>]+>\s+)?"
        r"(?:@\w+(?:\([^)]*\))?\s+)*"
        r"[\w<>,\?\[\]\s]+\s+(\w+)\s*\("
    )

    def repl(match: re.Match[str]) -> str:
        name = match.group(1)
        if name not in iface_javadocs:
            return match.group(0)
        tail = match.group(0).split("@Override\n\t", 1)[1]
        return iface_javadocs[name] + "\n\t@Override\n\t" + tail

    return pattern.sub(repl, impl_text)


def pick_source(rel: str) -> Path:
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
    iface_javadocs = extract_interface_method_javadocs(
        (ANALYZED / "spring-jdbc/src/main/java/org/springframework/jdbc/support/rowset/SqlRowSet.java").read_text(
            encoding="utf-8"
        )
    )
    for rel in BATCH_FILES:
        name = Path(rel).name
        dst = ANALYZED / rel
        reps = FILE_REPLACEMENTS.get(name, [])
        try:
            src = pick_source(rel)
        except FileNotFoundError as e:
            failures.append(str(e))
            continue
        dst.parent.mkdir(parents=True, exist_ok=True)
        if src.resolve() != dst.resolve():
            shutil.copy2(src, dst)
        try:
            text = dst.read_text(encoding="utf-8")
            if reps:
                text = apply_replacements(text, reps)
            text = refine_placeholders(text)
            if name == "ResultSetWrappingSqlRowSet.java":
                text = sync_rowset_impl_javadocs(text, iface_javadocs)
            cn = len(re.findall(r"[\u4e00-\u9fff]", text))
            lic = "Licensed under the Apache License" in text
            eng_blocks = len(
                [
                    b
                    for b in re.findall(r"/\*\*.*?\*/", text, re.DOTALL)
                    if "Licensed under the Apache" not in b
                    and not re.search(r"[\u4e00-\u9fff]", b)
                    and re.search(r"[A-Za-z]{4,}", b)
                ]
            )
            if cn < 50 or not lic or not has_chinese(text) or eng_blocks > 0:
                failures.append(
                    f"VALIDATION cn={cn} lic={lic} eng_blocks={eng_blocks}: {rel}"
                )
                continue
            dst.write_text(text, encoding="utf-8")
            ok += 1
            print(f"OK cn={cn} eng_blocks={eng_blocks} {rel} (base={src.parent.name})")
        except Exception as e:
            failures.append(f"{rel}: {e}")
            print(f"FAIL {rel}: {e}")
    return ok, failures


def confirm_chinese() -> dict[str, bool]:
    return {rel: has_chinese((ANALYZED / rel).read_text(encoding="utf-8")) for rel in BATCH_FILES}


def main() -> int:
    if len(BATCH_FILES) != 9:
        print(json.dumps({"error": f"expected 9 files, got {len(BATCH_FILES)}"}, ensure_ascii=False))
        return 1

    ok, failures = annotate_batch()
    if failures or ok != len(BATCH_FILES):
        print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False, indent=2))
        return 1

    replacement_modules = [
        "scripts/wave32b_replacements_mega.py",
    ]
    analyzed_paths = [f"springframework/7.0.8/analyzed/{rel}" for rel in BATCH_FILES]
    script_paths = [f"scripts/{SCRIPT_NAME}", *replacement_modules]
    sha, tree_count = isolated_index_commit(
        "zh-annotate springframework 7.0.8 wave32b [9:18]",
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
        "queue: mark springframework 7.0.8 wave32b [9:18] done",
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
                "chinese_confirmed": chinese,
                "all_chinese": all(chinese.values()),
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
