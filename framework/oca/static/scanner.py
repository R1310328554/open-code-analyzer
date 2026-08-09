from __future__ import annotations

import fnmatch
import json
from collections import Counter, defaultdict
from dataclasses import asdict, dataclass, field
from pathlib import Path

from ..util.fs import write_text


CODE_EXTS = {
    ".java": "java",
    ".kt": "kotlin",
    ".groovy": "groovy",
    ".py": "python",
    ".js": "javascript",
    ".ts": "typescript",
    ".tsx": "typescript",
    ".go": "go",
    ".rs": "rust",
    ".c": "c",
    ".cc": "cpp",
    ".cpp": "cpp",
    ".h": "c-header",
    ".hpp": "cpp",
    ".cs": "csharp",
    ".rb": "ruby",
    ".php": "php",
    ".scala": "scala",
    ".xml": "xml",
    ".gradle": "gradle",
    ".kts": "kotlin",
    ".md": "markdown",
    ".adoc": "asciidoc",
    ".yml": "yaml",
    ".yaml": "yaml",
    ".properties": "properties",
    ".sql": "sql",
}


@dataclass
class ModuleInfo:
    name: str
    path: str
    file_count: int = 0
    code_files: int = 0
    languages: dict[str, int] = field(default_factory=dict)
    description: str = ""


@dataclass
class ScanResult:
    root: str
    total_files: int
    code_files: int
    languages: dict[str, int]
    modules: list[ModuleInfo]
    top_dirs: dict[str, int]


def _ignored(rel: str, patterns: list[str]) -> bool:
    rel_posix = rel.replace("\\", "/")
    for pat in patterns:
        if fnmatch.fnmatch(rel_posix, pat) or fnmatch.fnmatch(Path(rel_posix).name, pat):
            return True
        # 目录级匹配
        parts = rel_posix.split("/")
        for i in range(len(parts)):
            prefix = "/".join(parts[: i + 1])
            if fnmatch.fnmatch(prefix, pat.rstrip("/**")):
                return True
    return False


def _guess_modules(root: Path, files: list[Path]) -> list[ModuleInfo]:
    """启发式识别模块：顶层目录含 build 文件或 src/ 视为模块。"""
    modules: dict[str, ModuleInfo] = {}
    build_markers = {
        "build.gradle",
        "build.gradle.kts",
        "pom.xml",
        "package.json",
        "Cargo.toml",
        "go.mod",
        "pyproject.toml",
        "setup.py",
    }
    top_candidates = []
    for child in sorted(root.iterdir()):
        if not child.is_dir() or child.name.startswith("."):
            continue
        names = {p.name for p in child.iterdir()} if child.exists() else set()
        if names & build_markers or "src" in names:
            top_candidates.append(child)

    if not top_candidates:
        # 单模块仓库
        return [
            ModuleInfo(
                name=root.name,
                path=".",
                file_count=len(files),
                code_files=sum(1 for f in files if f.suffix.lower() in CODE_EXTS),
            )
        ]

    for mod_dir in top_candidates:
        rel_files = [f for f in files if mod_dir in f.parents or f.parent == mod_dir]
        langs: Counter[str] = Counter()
        code_n = 0
        for f in rel_files:
            lang = CODE_EXTS.get(f.suffix.lower())
            if lang:
                langs[lang] += 1
                if lang not in {"markdown", "asciidoc", "yaml", "xml", "properties"}:
                    code_n += 1
        desc = ""
        readme = mod_dir / "README.md"
        if readme.exists():
            try:
                first = readme.read_text(encoding="utf-8", errors="replace").strip().splitlines()
                desc = next((ln.lstrip("# ").strip() for ln in first if ln.strip()), "")[:200]
            except OSError:
                pass
        modules[mod_dir.name] = ModuleInfo(
            name=mod_dir.name,
            path=str(mod_dir.relative_to(root)),
            file_count=len(rel_files),
            code_files=code_n,
            languages=dict(langs),
            description=desc,
        )
    return list(modules.values())


def scan_project(root: Path, ignore: list[str] | None = None) -> ScanResult:
    ignore = ignore or []
    files: list[Path] = []
    langs: Counter[str] = Counter()
    top_dirs: Counter[str] = Counter()

    for path in root.rglob("*"):
        if not path.is_file():
            continue
        rel = str(path.relative_to(root))
        if _ignored(rel, ignore):
            continue
        files.append(path)
        lang = CODE_EXTS.get(path.suffix.lower())
        if lang:
            langs[lang] += 1
        parts = Path(rel).parts
        if parts:
            top_dirs[parts[0]] += 1

    code_files = sum(
        1
        for f in files
        if CODE_EXTS.get(f.suffix.lower())
        not in {None, "markdown", "asciidoc", "yaml", "xml", "properties"}
        and CODE_EXTS.get(f.suffix.lower())
    )
    # 更稳妥的 code_files 计数
    code_like = {
        "java",
        "kotlin",
        "groovy",
        "python",
        "javascript",
        "typescript",
        "go",
        "rust",
        "c",
        "cpp",
        "csharp",
        "ruby",
        "php",
        "scala",
    }
    code_files = sum(1 for f in files if CODE_EXTS.get(f.suffix.lower()) in code_like)

    modules = _guess_modules(root, files)
    return ScanResult(
        root=str(root),
        total_files=len(files),
        code_files=code_files,
        languages=dict(langs.most_common()),
        modules=modules,
        top_dirs=dict(top_dirs.most_common()),
    )


def dump_scan(result: ScanResult, out_json: Path, out_md: Path) -> None:
    payload = {
        "root": result.root,
        "total_files": result.total_files,
        "code_files": result.code_files,
        "languages": result.languages,
        "top_dirs": result.top_dirs,
        "modules": [asdict(m) for m in result.modules],
    }
    write_text(out_json, json.dumps(payload, ensure_ascii=False, indent=2))

    lines = [
        "# 源码扫描报告",
        "",
        f"- 根目录: `{result.root}`",
        f"- 文件总数: **{result.total_files}**",
        f"- 代码文件: **{result.code_files}**",
        "",
        "## 语言分布",
        "",
    ]
    for lang, n in result.languages.items():
        lines.append(f"- {lang}: {n}")
    lines += ["", "## 模块一览", ""]
    for m in sorted(result.modules, key=lambda x: (-x.code_files, x.name)):
        lines.append(
            f"- **{m.name}** (`{m.path}`): 文件 {m.file_count}, 代码 {m.code_files}"
            + (f" — {m.description}" if m.description else "")
        )
    lines += ["", "## 顶层目录文件数", ""]
    for d, n in result.top_dirs.items():
        lines.append(f"- {d}: {n}")
    write_text(out_md, "\n".join(lines) + "\n")
