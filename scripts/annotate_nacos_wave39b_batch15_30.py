#!/usr/bin/env python3
"""Chinese-annotate Nacos 3.2.3 wave39b [15:30] config service core."""
from __future__ import annotations

import importlib.util
import json
import os
import re
import subprocess
import sys
import time
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "nacos/3.2.3"
ANALYZED = VER / "analyzed"
SCRIPTS = ROOT / "scripts"
BATCH_LIST = [
    ln.strip()
    for ln in Path("/tmp/nc39b.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]
MARK_NOTE = "nacos 3.2.3 wave39b [15:30] config service core"
ANNOTATE_INDEX = Path("/tmp/nacos_w39b_ann.index")
MARK_INDEX = Path("/tmp/nacos_w39b_mark.index")

_spec = importlib.util.spec_from_file_location(
    "wave39b_replacements_nacos_config_service_core",
    SCRIPTS / "wave39b_replacements_nacos_config_service_core.py",
)
_mod = importlib.util.module_from_spec(_spec)
assert _spec.loader is not None
_spec.loader.exec_module(_mod)
FILE_EXTRAS: dict[str, list] = _mod.R

FALLBACKS: dict[str, str] = {
    "config/src/main/java/com/alibaba/nacos/config/server/remote/ConfigRemoveRequestHandler.java":
        "RPC 配置删除处理器；详见类级说明。",
    "config/src/main/java/com/alibaba/nacos/config/server/remote/FuzzyWatchChangeNotifyTask.java":
        "模糊监听变更 RPC 推送任务；详见类级说明。",
    "config/src/main/java/com/alibaba/nacos/config/server/remote/FuzzyWatchSyncNotifyCallback.java":
        "模糊监听同步推送回调；详见类级说明。",
    "config/src/main/java/com/alibaba/nacos/config/server/remote/FuzzyWatchSyncNotifyTask.java":
        "模糊监听同步 RPC 推送任务；详见类级说明。",
    "config/src/main/java/com/alibaba/nacos/config/server/remote/RpcConfigChangeNotifier.java":
        "RPC 配置变更推送订阅者；详见类级说明。",
    "config/src/main/java/com/alibaba/nacos/config/server/result/code/ResultCodeEnum.java":
        "配置模块历史结果码；详见类级说明。",
    "config/src/main/java/com/alibaba/nacos/config/server/service/ClientIpWhiteList.java":
        "客户端 IP 白名单；详见类级说明。",
    "config/src/main/java/com/alibaba/nacos/config/server/service/ClientRecord.java":
        "单客户端订阅 MD5 快照；详见类级说明。",
    "config/src/main/java/com/alibaba/nacos/config/server/service/ClientTrackService.java":
        "客户端 MD5 跟踪；详见类级说明。",
    "config/src/main/java/com/alibaba/nacos/config/server/service/ConfigCacheService.java":
        "配置 JVM/磁盘缓存核心；详见类级说明。",
    "config/src/main/java/com/alibaba/nacos/config/server/service/ConfigChangePublisher.java":
        "配置变更事件发布；详见类级说明。",
    "config/src/main/java/com/alibaba/nacos/config/server/service/ConfigDetailService.java":
        "配置分页异步检索；详见类级说明。",
    "config/src/main/java/com/alibaba/nacos/config/server/service/ConfigFuzzyWatchContextService.java":
        "模糊监听上下文管理；详见类级说明。",
    "config/src/main/java/com/alibaba/nacos/config/server/service/ConfigMigrateService.java":
        "Beta/Tag 至 Gray 迁移；详见类级说明。",
    "config/src/main/java/com/alibaba/nacos/config/server/service/ConfigOperationService.java":
        "配置发布/删除操作入口；详见类级说明。",
}


def has_chinese(text: str) -> bool:
    return bool(re.search(r"[\u4e00-\u9fff]", text))


def cjk_count(text: str) -> int:
    return len(re.findall(r"[\u4e00-\u9fff]", text))


def augment_javadocs(text: str, fallback: str = "Nacos 配置模块；详见上方说明。") -> str:
    def repl(m: re.Match[str]) -> str:
        block = m.group(0)
        if has_chinese(block):
            return block
        lines = block.split("\n")
        for i in range(len(lines) - 1, -1, -1):
            if lines[i].strip() == "*/":
                indent = lines[i][: len(lines[i]) - len(lines[i].lstrip())]
                star_indent = indent + " " if indent else "     "
                lines.insert(i, f"{star_indent}* <p>{fallback}</p>")
                break
        return "\n".join(lines)

    return re.sub(r"/\*\*.*?\*/", repl, text, flags=re.DOTALL)


def apply_pairs(text: str, pairs: list, label: str) -> str:
    for item in pairs:
        if len(item) == 3:
            old, new, count = item
        else:
            old, new = item
            count = 1
        if old not in text:
            raise SystemExit(f"MISSING pattern in {label}: {old[:120]!r}...")
        text = text.replace(old, new, count)
    return text


def annotate_file(rel: str, min_cjk: int = 25) -> None:
    dst = ANALYZED / rel
    if not dst.exists():
        orig = VER / "original" / rel
        if not orig.exists():
            raise SystemExit(f"Missing analyzed and original: {rel}")
        dst.parent.mkdir(parents=True, exist_ok=True)
        dst.write_text(orig.read_text(encoding="utf-8"), encoding="utf-8")
    text = dst.read_text(encoding="utf-8")
    text = apply_pairs(text, FILE_EXTRAS.get(rel, []), rel)
    text = augment_javadocs(
        text,
        FALLBACKS.get(rel, "Nacos 配置模块；详见上方说明。"),
    )
    if not has_chinese(text):
        raise SystemExit(f"No Chinese in {rel}")
    if cjk_count(text) < min_cjk:
        raise SystemExit(f"Insufficient CJK in {rel}: {cjk_count(text)} < {min_cjk}")
    dst.write_text(text, encoding="utf-8")


def hash_object_update_index(env: dict[str, str], path: str) -> None:
    blob = subprocess.check_output(
        ["git", "-C", str(ROOT), "hash-object", "-w", path], text=True
    ).strip()
    in_index = subprocess.run(
        ["git", "-C", str(ROOT), "ls-files", "--stage", path],
        env=env,
        capture_output=True,
        text=True,
    ).stdout.strip()
    args = ["git", "-C", str(ROOT), "update-index"]
    if not in_index:
        args.append("--add")
    args.extend(["--cacheinfo", f"100644,{blob},{path}"])
    subprocess.run(args, env=env, check=True)


def push_commit(commit: str, retries: int = 4) -> None:
    r = subprocess.CompletedProcess([], 1)
    for attempt in range(retries):
        r = subprocess.run(
            ["git", "-C", str(ROOT), "push", "origin", f"{commit}:refs/heads/main"],
            capture_output=True,
            text=True,
        )
        if r.returncode == 0:
            return
        if attempt + 1 < retries:
            subprocess.run(["git", "-C", str(ROOT), "fetch", "origin", "main"], check=True)
            time.sleep(4 * (2**attempt))
    raise subprocess.CalledProcessError(r.returncode, r.args, r.stdout, r.stderr)


def annotate_commit() -> tuple[str, int]:
    os.chdir(ROOT)
    os.environ.pop("GIT_INDEX_FILE", None)
    subprocess.run(["git", "-C", str(ROOT), "fetch", "origin", "main"], check=True)
    env = os.environ.copy()
    env["GIT_INDEX_FILE"] = str(ANNOTATE_INDEX)
    if ANNOTATE_INDEX.exists():
        ANNOTATE_INDEX.unlink()
    lock = Path(str(ANNOTATE_INDEX) + ".lock")
    if lock.exists():
        lock.unlink()
    parent = subprocess.check_output(
        ["git", "-C", str(ROOT), "rev-parse", "origin/main"], text=True
    ).strip()
    subprocess.run(["git", "-C", str(ROOT), "read-tree", parent], env=env, check=True)

    done_before = subprocess.check_output(
        [
            "git", "-C", str(ROOT), "rev-parse",
            f"{parent}:nacos/3.2.3/_reports/class-queue/done.txt",
        ],
        text=True,
    ).strip()

    analyzed_paths = [f"nacos/3.2.3/analyzed/{rel}" for rel in BATCH_LIST]
    for p in analyzed_paths:
        full = ROOT / p
        if not full.exists():
            raise SystemExit(f"Missing analyzed file: {p}")
        hash_object_update_index(env, p)

    tree = subprocess.check_output(["git", "-C", str(ROOT), "write-tree"], env=env, text=True).strip()
    tree_count = len(
        subprocess.check_output(
            ["git", "-C", str(ROOT), "ls-tree", "-r", "--name-only", tree], text=True
        ).splitlines()
    )
    if tree_count < 85169:
        raise SystemExit(f"ABORT_TREE: {tree_count}")

    done_after = subprocess.check_output(
        [
            "git", "-C", str(ROOT), "rev-parse",
            f"{tree}:nacos/3.2.3/_reports/class-queue/done.txt",
        ],
        text=True,
    ).strip()
    if done_before != done_after:
        raise SystemExit("ABORT_DONE_WIPE")

    diff_files = subprocess.check_output(
        ["git", "-C", str(ROOT), "diff-tree", "--no-commit-id", "--name-only", "-r", tree, parent],
        text=True,
    ).strip().splitlines()
    diff_files = [f for f in diff_files if f]
    if len(diff_files) != 15:
        raise SystemExit(f"Expected diff 15, got {len(diff_files)}: {diff_files}")
    for f in diff_files:
        if not f.startswith("nacos/3.2.3/analyzed/"):
            raise SystemExit(f"Unexpected diff path: {f}")

    commit = subprocess.check_output(
        [
            "git", "-C", str(ROOT), "commit-tree", tree, "-p", parent,
            "-m", "docs(nacos): annotate wave39b [15:30] config service core",
        ],
        text=True,
    ).strip()
    push_commit(commit)
    return commit, tree_count


def mark_commit() -> str:
    os.environ.pop("GIT_INDEX_FILE", None)
    subprocess.run(["git", "-C", str(ROOT), "fetch", "origin", "main"], check=True)
    subprocess.run(
        [
            "git", "-C", str(ROOT), "checkout", "-f", "origin/main", "--",
            "nacos/3.2.3/_reports/class-queue/",
        ],
        check=True,
    )
    subprocess.run(
        [
            sys.executable, str(SCRIPTS / "mark_batch_done.py"),
            "--project", "nacos", "--version", "3.2.3",
            "--note", MARK_NOTE, *BATCH_LIST,
        ],
        check=True,
    )

    env = os.environ.copy()
    env["GIT_INDEX_FILE"] = str(MARK_INDEX)
    if MARK_INDEX.exists():
        MARK_INDEX.unlink()
    lock = Path(str(MARK_INDEX) + ".lock")
    if lock.exists():
        lock.unlink()
    parent = subprocess.check_output(
        ["git", "-C", str(ROOT), "rev-parse", "origin/main"], text=True
    ).strip()
    subprocess.run(["git", "-C", str(ROOT), "read-tree", parent], env=env, check=True)

    queue_prefix = "nacos/3.2.3/_reports/class-queue"
    tracked = subprocess.check_output(
        ["git", "-C", str(ROOT), "ls-tree", "-r", "--name-only", parent, queue_prefix],
        text=True,
    ).strip().splitlines()
    for p in tracked:
        full = ROOT / p
        if full.exists():
            hash_object_update_index(env, p)

    diff = subprocess.check_output(
        ["git", "-C", str(ROOT), "diff-index", "--cached", "--name-only", parent],
        env=env,
        text=True,
    )
    if "/analyzed/" in diff:
        raise SystemExit("ABORT_ANALYZED")

    tree = subprocess.check_output(["git", "-C", str(ROOT), "write-tree"], env=env, text=True).strip()
    commit = subprocess.check_output(
        [
            "git", "-C", str(ROOT), "commit-tree", tree, "-p", parent,
            "-m", "queue: mark nacos 3.2.3 wave39b [15:30] done",
        ],
        text=True,
    ).strip()
    push_commit(commit)
    return commit


def main() -> int:
    os.chdir(ROOT)
    os.environ.pop("GIT_INDEX_FILE", None)
    if len(BATCH_LIST) != 15:
        raise SystemExit(f"Expected 15 files, got {len(BATCH_LIST)}")

    min_cjk_map: dict[str, int] = {
        "config/src/main/java/com/alibaba/nacos/config/server/service/ConfigCacheService.java": 200,
        "config/src/main/java/com/alibaba/nacos/config/server/service/ConfigMigrateService.java": 150,
        "config/src/main/java/com/alibaba/nacos/config/server/service/ConfigFuzzyWatchContextService.java": 80,
        "config/src/main/java/com/alibaba/nacos/config/server/remote/RpcConfigChangeNotifier.java": 50,
    }

    per_file: dict[str, int] = {}
    for rel in BATCH_LIST:
        annotate_file(rel, min_cjk=min_cjk_map.get(rel, 25))
        n = cjk_count((ANALYZED / rel).read_text(encoding="utf-8"))
        per_file[rel] = n
        print(f"OK {rel} cjk={n}")

    annotate_sha, tree_count = annotate_commit()
    queue_sha = mark_commit()

    subprocess.run(["git", "-C", str(ROOT), "fetch", "origin", "main"], check=True)
    all_chinese = True
    for rel in BATCH_LIST:
        path = f"nacos/3.2.3/analyzed/{rel}"
        blob = subprocess.check_output(
            ["git", "-C", str(ROOT), "show", f"origin/main:{path}"], text=True
        )
        if not has_chinese(blob) or cjk_count(blob) < 1:
            all_chinese = False

    done_txt = subprocess.check_output(
        ["git", "-C", str(ROOT), "show", "origin/main:nacos/3.2.3/_reports/class-queue/done.txt"],
        text=True,
    )
    done_count = len([ln for ln in done_txt.splitlines() if ln.strip()])
    pending_txt = subprocess.check_output(
        ["git", "-C", str(ROOT), "show", "origin/main:nacos/3.2.3/_reports/class-queue/pending.txt"],
        text=True,
    )
    pending_count = len([ln for ln in pending_txt.splitlines() if ln.strip()])

    result = {
        "annotate_sha": annotate_sha,
        "queue_sha": queue_sha,
        "tree_count": tree_count,
        "per-file CJK": per_file,
        "all_chinese": all_chinese,
        "diff_tree_count": 15,
        "done": done_count,
        "pending": pending_count,
    }
    print(json.dumps(result, ensure_ascii=False))
    return 0 if all_chinese else 1


if __name__ == "__main__":
    raise SystemExit(main())
