from __future__ import annotations

import re
from dataclasses import dataclass
from pathlib import Path
from urllib.parse import urlparse


def slugify_project_name(name_or_url: str) -> str:
    """从项目名或 GitHub URL 生成目录名。"""
    s = name_or_url.strip().rstrip("/")
    if "://" in s or s.startswith("git@"):
        if s.startswith("git@"):
            # git@github.com:org/repo.git
            s = s.split(":", 1)[-1]
        else:
            path = urlparse(s).path.lstrip("/")
            s = path
        if s.endswith(".git"):
            s = s[:-4]
        # org/repo -> repo（springframework 场景用 repo 名）
        if "/" in s:
            s = s.split("/")[-1]
    # spring-framework -> springframework（按 README 示例）
    s = s.replace("-", "").replace("_", "")
    s = re.sub(r"[^A-Za-z0-9\u4e00-\u9fff.+]", "", s)
    return s.lower() or "project"


@dataclass(frozen=True)
class ProjectLayout:
    root: Path
    project: str
    version: str
    original_dirname: str = "original"
    analyzed_dirname: str = "analyzed"
    arch_dirname: str = "架构说明"
    module_docs_dirname: str = "项目模块说明"
    reports_dirname: str = "_reports"

    @property
    def version_dir(self) -> Path:
        return self.root / self.project / self.version

    @property
    def original(self) -> Path:
        return self.version_dir / self.original_dirname

    @property
    def analyzed(self) -> Path:
        return self.version_dir / self.analyzed_dirname

    @property
    def arch(self) -> Path:
        return self.version_dir / self.arch_dirname

    @property
    def module_docs(self) -> Path:
        return self.arch / self.module_docs_dirname

    @property
    def reports(self) -> Path:
        return self.version_dir / self.reports_dirname

    def ensure(self) -> None:
        for p in (self.original, self.analyzed, self.module_docs, self.reports):
            p.mkdir(parents=True, exist_ok=True)
