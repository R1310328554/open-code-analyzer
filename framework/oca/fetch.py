from __future__ import annotations

import shutil
import subprocess
from pathlib import Path

from .resolve import ResolvedTarget
from .util.fs import ensure_dir
from .util.paths import ProjectLayout


def fetch_source(target: ResolvedTarget, layout: ProjectLayout, *, depth: int = 1) -> Path:
    """浅克隆指定 ref 到 layout.original（不含 .git）。"""
    layout.ensure()
    original = layout.original
    if original.exists() and any(original.iterdir()):
        print(f"[oca] original 已存在，跳过拉取: {original}")
        return original

    tmp = layout.version_dir / "_fetch_tmp"
    if tmp.exists():
        shutil.rmtree(tmp)
    ensure_dir(tmp.parent)

    print(f"[oca] 浅克隆 {target.clone_url} @ {target.git_ref} (depth={depth})")
    cmd = [
        "git",
        "clone",
        f"--depth={depth}",
        "--branch",
        target.git_ref,
        target.clone_url,
        str(tmp),
    ]
    try:
        subprocess.check_call(cmd)
    except subprocess.CalledProcessError:
        # 某些 tag 可能需要先 clone 再 checkout；或 branch 名不匹配时去掉 --branch
        print("[oca] --branch 失败，回退为 clone + fetch tag/commit")
        if tmp.exists():
            shutil.rmtree(tmp)
        subprocess.check_call(
            ["git", "clone", f"--depth={depth}", target.clone_url, str(tmp)]
        )
        subprocess.check_call(
            ["git", "-C", str(tmp), "fetch", "--depth", str(depth), "origin", target.git_ref]
        )
        subprocess.check_call(["git", "-C", str(tmp), "checkout", "FETCH_HEAD"])

    # 移除 .git，保持 original 为只读源码快照
    git_dir = tmp / ".git"
    if git_dir.exists():
        shutil.rmtree(git_dir)

    if original.exists():
        shutil.rmtree(original)
    tmp.rename(original)
    print(f"[oca] 源码已就绪: {original}")
    return original


def sync_analyzed_from_original(layout: ProjectLayout, *, force: bool = False) -> Path:
    """将 original 同步到 analyzed（分析前的基线拷贝）。"""
    from .util.fs import copy_tree

    layout.ensure()
    if layout.analyzed.exists() and any(layout.analyzed.iterdir()) and not force:
        print(f"[oca] analyzed 已存在，跳过同步（可用 --force-sync 覆盖）: {layout.analyzed}")
        return layout.analyzed
    print(f"[oca] 同步 original -> analyzed")
    copy_tree(layout.original, layout.analyzed, ignore_git=True)
    return layout.analyzed
