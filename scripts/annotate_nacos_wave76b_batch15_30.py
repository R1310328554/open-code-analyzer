#!/usr/bin/env python3
"""Chinese-annotate Nacos 3.2.3 wave76b [15:30] default auth roles/token/users."""
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
    for ln in Path("/tmp/nc76b.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]
MARK_NOTE = "wave76b [15:30] default auth roles/token/users"
ANNOTATE_INDEX = Path("/tmp/nc76b_ann.index")
MARK_INDEX = Path("/tmp/nc76b_mark.index")

_spec = importlib.util.spec_from_file_location(
    "wave76b_replacements_nacos_auth_roles_token_users",
    SCRIPTS / "wave76b_replacements_nacos_auth_roles_token_users.py",
)
_mod = importlib.util.module_from_spec(_spec)
assert _spec.loader is not None
_spec.loader.exec_module(_mod)
FILE_EXTRAS: dict[str, list] = _mod.R

FALLBACKS: dict[str, str] = {
    "plugin-default-impl/nacos-default-auth-plugin/src/main/java/com/alibaba/nacos/plugin/auth/impl/roles/AbstractCachedRoleService.java": "Nacos 角色缓存抽象基类。",
    "plugin-default-impl/nacos-default-auth-plugin/src/main/java/com/alibaba/nacos/plugin/auth/impl/roles/AbstractCheckedRoleService.java": "Nacos 带权限校验的角色服务抽象类。",
    "plugin-default-impl/nacos-default-auth-plugin/src/main/java/com/alibaba/nacos/plugin/auth/impl/roles/NacosRoleService.java": "Nacos 角色服务接口。",
    "plugin-default-impl/nacos-default-auth-plugin/src/main/java/com/alibaba/nacos/plugin/auth/impl/roles/NacosRoleServiceDirectImpl.java": "直连数据库的角色服务实现。",
    "plugin-default-impl/nacos-default-auth-plugin/src/main/java/com/alibaba/nacos/plugin/auth/impl/roles/NacosRoleServiceRemoteImpl.java": "远程 HTTP 角色服务实现。",
    "plugin-default-impl/nacos-default-auth-plugin/src/main/java/com/alibaba/nacos/plugin/auth/impl/token/TokenManager.java": "JWT 令牌管理器接口。",
    "plugin-default-impl/nacos-default-auth-plugin/src/main/java/com/alibaba/nacos/plugin/auth/impl/token/TokenManagerDelegate.java": "TokenManager 委托包装。",
    "plugin-default-impl/nacos-default-auth-plugin/src/main/java/com/alibaba/nacos/plugin/auth/impl/token/impl/CachedJwtTokenManager.java": "带本地缓存的 JWT 令牌管理器。",
    "plugin-default-impl/nacos-default-auth-plugin/src/main/java/com/alibaba/nacos/plugin/auth/impl/token/impl/JwtTokenManager.java": "基于 NacosJwtParser 的 JWT 管理器。",
    "plugin-default-impl/nacos-default-auth-plugin/src/main/java/com/alibaba/nacos/plugin/auth/impl/users/AbstractCachedUserService.java": "Nacos 用户缓存抽象基类。",
    "plugin-default-impl/nacos-default-auth-plugin/src/main/java/com/alibaba/nacos/plugin/auth/impl/users/NacosUser.java": "鉴权上下文 Nacos 用户模型。",
    "plugin-default-impl/nacos-default-auth-plugin/src/main/java/com/alibaba/nacos/plugin/auth/impl/users/NacosUserDetails.java": "Spring Security UserDetails 适配。",
    "plugin-default-impl/nacos-default-auth-plugin/src/main/java/com/alibaba/nacos/plugin/auth/impl/users/NacosUserService.java": "Nacos 用户服务接口。",
    "plugin-default-impl/nacos-default-auth-plugin/src/main/java/com/alibaba/nacos/plugin/auth/impl/users/NacosUserServiceDirectImpl.java": "直连数据库的用户服务实现。",
    "plugin-default-impl/nacos-default-auth-plugin/src/main/java/com/alibaba/nacos/plugin/auth/impl/users/NacosUserServiceRemoteImpl.java": "远程 HTTP 用户服务实现。",
}


def has_chinese(text: str) -> bool:
    return bool(re.search(r"[\u4e00-\u9fff]", text))


def cjk_count(text: str) -> int:
    return len(re.findall(r"[\u4e00-\u9fff]", text))


def augment_javadocs(text: str, fallback: str = "Nacos 模块组件；详见上方说明。") -> str:
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
            count = -1
        if old not in text:
            raise SystemExit(f"MISSING pattern in {label}: {old[:120]!r}...")
        if count == -1:
            text = text.replace(old, new)
        else:
            text = text.replace(old, new, count)
    return text


def annotate_file(rel: str, min_cjk: int = 15) -> None:
    dst = ANALYZED / rel
    if not dst.exists():
        raise SystemExit(f"Missing analyzed: {rel}")
    text = dst.read_text(encoding="utf-8")
    text = apply_pairs(text, FILE_EXTRAS.get(rel, []), rel)
    text = augment_javadocs(text, FALLBACKS.get(rel, "Nacos 模块组件；详见上方说明。"))
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
            "-m", "docs(nacos): annotate wave76b [15:30] default auth roles/token/users",
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
            "-m", "queue: mark nacos wave76b [15:30] default auth roles/token/users done",
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

    subprocess.run(["git", "-C", str(ROOT), "fetch", "origin", "main"], check=True)
    for rel in BATCH_LIST:
        p = f"nacos/3.2.3/analyzed/{rel}"
        subprocess.run(
            ["git", "-C", str(ROOT), "checkout", "-f", "origin/main", "--", p],
            check=True,
        )

    per_file: dict[str, int] = {}
    for rel in BATCH_LIST:
        annotate_file(rel, min_cjk=15)
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

    done_count = len(
        subprocess.check_output(
            [
                "git", "-C", str(ROOT), "show",
                "origin/main:nacos/3.2.3/_reports/class-queue/done.txt",
            ],
            text=True,
        ).strip().splitlines()
    )
    pending_count = len(
        subprocess.check_output(
            [
                "git", "-C", str(ROOT), "show",
                "origin/main:nacos/3.2.3/_reports/class-queue/pending.txt",
            ],
            text=True,
        ).strip().splitlines()
    )

    result = {
        "annotate_sha": annotate_sha,
        "queue_sha": queue_sha,
        "tree_count": tree_count,
        "per-file CJK Han counts": per_file,
        "all_chinese": all_chinese,
        "diff_tree_count": 15,
        "done": done_count,
        "pending": pending_count,
    }
    print(json.dumps(result, ensure_ascii=False))
    return 0 if all_chinese else 1


if __name__ == "__main__":
    raise SystemExit(main())
