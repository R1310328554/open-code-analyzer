from __future__ import annotations

import shutil
from pathlib import Path


def ensure_dir(path: str | Path) -> Path:
    p = Path(path)
    p.mkdir(parents=True, exist_ok=True)
    return p


def copy_tree(src: str | Path, dst: str | Path, *, ignore_git: bool = True) -> None:
    src_p, dst_p = Path(src), Path(dst)
    if dst_p.exists():
        shutil.rmtree(dst_p)

    def _ignore(directory: str, names: list[str]) -> set[str]:
        ignored: set[str] = set()
        if ignore_git and ".git" in names:
            ignored.add(".git")
        return ignored

    shutil.copytree(src_p, dst_p, ignore=_ignore)


def write_text(path: str | Path, content: str) -> None:
    p = Path(path)
    ensure_dir(p.parent)
    p.write_text(content, encoding="utf-8")


def read_text(path: str | Path) -> str:
    return Path(path).read_text(encoding="utf-8", errors="replace")
