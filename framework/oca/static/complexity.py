from __future__ import annotations

import csv
import io
import json
import re
import subprocess
from dataclasses import asdict, dataclass
from pathlib import Path

from ..util.fs import write_text


@dataclass
class ComplexityHit:
    file: str
    name: str
    start_line: int
    end_line: int
    cyclomatic: int
    nloc: int
    token_count: int
    kind: str = "function"


TRIVIAL_NAME_RE = re.compile(
    r"^(get|set|is|has)[A-Z_].*$|^equals$|^hashCode$|^toString$|^canEqual$|^clone$"
)


def _rel_path(root: Path, fpath: str) -> str:
    p = Path(fpath)
    try:
        if not p.is_absolute():
            # lizard 可能输出相对当前工作目录的路径
            p = (Path.cwd() / p).resolve()
        return str(p.relative_to(root.resolve())).replace("\\", "/")
    except Exception:
        # 尽量截掉 root 前缀
        text = str(fpath).replace("\\", "/")
        root_s = str(root.resolve()).replace("\\", "/")
        if root_s in text:
            return text.split(root_s, 1)[-1].lstrip("/")
        # 常见：.../original/spring-xxx/...
        for marker in ("/original/", "/analyzed/"):
            if marker in text:
                return text.split(marker, 1)[-1]
        return text.lstrip("./")


def _parse_lizard_csv(out: str, root: Path) -> list[ComplexityHit]:
    """
    兼容 lizard 新旧 CSV：
    新格式示例:
      NLOC,CCN,token,PARAM,length,"Name@start-end@file",file,name,long_name,start,end
    旧格式示例:
      NLOC,CCN,token,PARAM,length,Name@file:start-end
    """
    hits: list[ComplexityHit] = []
    reader = csv.reader(io.StringIO(out))
    for parts in reader:
        if not parts or parts[0] in {"NLOC", "nloc"}:
            continue
        if len(parts) < 6:
            continue
        try:
            nloc = int(parts[0])
            ccn = int(parts[1])
            token = int(parts[2])
        except ValueError:
            continue

        name = ""
        fpath = ""
        start = 0
        end = 0

        # 新格式：末尾带 start,end，并单独给 filepath / name
        if len(parts) >= 11:
            try:
                start = int(parts[-2])
                end = int(parts[-1])
                fpath = parts[6] if parts[6] else parts[5]
                name = parts[7] or parts[8] or parts[5]
            except ValueError:
                pass

        if not start:
            loc = parts[5].strip().strip('"')
            # Name@start-end@file
            m = re.match(r"^(?P<name>.+)@(?P<start>\d+)-(?P<end>\d+)@(?P<file>.+)$", loc)
            if m:
                name, start, end, fpath = (
                    m.group("name"),
                    int(m.group("start")),
                    int(m.group("end")),
                    m.group("file"),
                )
            else:
                # Name@file:start-end
                m = re.match(r"^(?P<name>.+)@(?P<file>.+):(?P<start>\d+)-(?P<end>\d+)$", loc)
                if not m:
                    continue
                name, fpath, start, end = (
                    m.group("name"),
                    m.group("file"),
                    int(m.group("start")),
                    int(m.group("end")),
                )

        hits.append(
            ComplexityHit(
                file=_rel_path(root, fpath),
                name=name,
                start_line=start,
                end_line=end,
                cyclomatic=ccn,
                nloc=nloc,
                token_count=token,
            )
        )
    return hits


def _run_lizard(root: Path, languages: list[str] | None = None) -> list[ComplexityHit]:
    langs = languages or [
        "java",
        "python",
        "javascript",
        "typescript",
        "cpp",
        "csharp",
        "go",
        "rust",
        "kotlin",
        "scala",
    ]
    cmd = ["lizard", "--csv"]
    for lang in langs:
        cmd.extend(["-l", lang])

    # 大型 Java 仓库优先扫 main 源码，避免 test 噪声与过长耗时
    targets: list[str] = []
    main_dirs = list(root.glob("**/src/main"))
    if main_dirs:
        targets = [str(p) for p in main_dirs]
    else:
        targets = [str(root)]
    cmd.extend(targets)

    try:
        out = subprocess.check_output(cmd, stderr=subprocess.DEVNULL, text=True)
    except (subprocess.CalledProcessError, FileNotFoundError):
        return _heuristic_java_complexity(root)

    hits = _parse_lizard_csv(out, root)
    if not hits:
        # 解析失败时回退启发式，但只扫 main 以免卡死
        return _heuristic_java_complexity(Path(targets[0]).parent.parent if main_dirs else root)
    return hits


def _heuristic_java_complexity(root: Path) -> list[ComplexityHit]:
    """无 lizard 时的退化：按分支关键字粗估 Java 方法复杂度。"""
    hits: list[ComplexityHit] = []
    method_re = re.compile(
        r"(?P<indent>^[ \t]*)(?:public|protected|private|static|final|native|synchronized|abstract|default|[\w<>\[\],.?@\s]+)+\s+"
        r"(?P<name>[A-Za-z_]\w*)\s*\([^;]*\)\s*(?:throws\s+[^{]+)?\{",
        re.M,
    )
    branch_re = re.compile(r"\b(if|for|while|case|catch|\&\&|\|\||\?)\b")

    for path in root.rglob("*.java"):
        try:
            text = path.read_text(encoding="utf-8", errors="replace")
        except OSError:
            continue
        lines = text.splitlines()
        # 简化：用大括号配对提取方法体
        for m in method_re.finditer(text):
            name = m.group("name")
            if name in {"if", "for", "while", "switch", "catch", "return", "new"}:
                continue
            start = text.count("\n", 0, m.start()) + 1
            # 从方法 { 开始配对
            brace_pos = text.find("{", m.end() - 1)
            if brace_pos < 0:
                continue
            depth = 0
            end_pos = brace_pos
            for i in range(brace_pos, len(text)):
                ch = text[i]
                if ch == "{":
                    depth += 1
                elif ch == "}":
                    depth -= 1
                    if depth == 0:
                        end_pos = i
                        break
            end_line = text.count("\n", 0, end_pos) + 1
            body = text[brace_pos : end_pos + 1]
            ccn = 1 + len(branch_re.findall(body))
            nloc = max(1, end_line - start + 1)
            rel = str(path.relative_to(root)).replace("\\", "/")
            hits.append(
                ComplexityHit(
                    file=rel,
                    name=name,
                    start_line=start,
                    end_line=end_line,
                    cyclomatic=ccn,
                    nloc=nloc,
                    token_count=len(body),
                )
            )
    return hits


def analyze_file_complexity(root: Path) -> list[ComplexityHit]:
    return _run_lizard(root)


def filter_complex(
    hits: list[ComplexityHit],
    *,
    cyclomatic_min: int = 8,
    loc_min: int = 40,
    skip_trivial_accessors: bool = True,
) -> list[ComplexityHit]:
    out: list[ComplexityHit] = []
    for h in hits:
        short_name = h.name.split("::")[-1].split(".")[-1]
        if skip_trivial_accessors and TRIVIAL_NAME_RE.match(short_name) and h.cyclomatic <= 2:
            continue
        if h.cyclomatic >= cyclomatic_min or h.nloc >= loc_min:
            out.append(h)
    out.sort(key=lambda x: (-x.cyclomatic, -x.nloc, x.file, x.start_line))
    return out


def dump_complexity(hits: list[ComplexityHit], out_json: Path, out_md: Path, limit: int = 200) -> None:
    write_text(out_json, json.dumps([asdict(h) for h in hits], ensure_ascii=False, indent=2))
    lines = [
        "# 复杂度热点",
        "",
        f"共 **{len(hits)}** 个高复杂度方法/函数（展示前 {min(limit, len(hits))}）。",
        "",
        "| CCN | NLOC | 方法 | 文件 | 行号 |",
        "| --- | --- | --- | --- | --- |",
    ]
    for h in hits[:limit]:
        lines.append(
            f"| {h.cyclomatic} | {h.nloc} | `{h.name}` | `{h.file}` | {h.start_line}-{h.end_line} |"
        )
    write_text(out_md, "\n".join(lines) + "\n")
