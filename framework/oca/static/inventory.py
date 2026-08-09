from __future__ import annotations

import json
import re
from dataclasses import asdict, dataclass
from pathlib import Path

from ..util.fs import write_text
from .complexity import ComplexityHit


@dataclass
class TypeUnit:
    file: str
    package: str
    name: str
    kind: str  # class/interface/enum/record/@interface
    start_line: int
    modifiers: list[str]


JAVA_TYPE_RE = re.compile(
    r"(?P<mods>^[ \t]*(?:public|protected|private|static|final|abstract|sealed|non-sealed|strictfp)[ \t\w]*)"
    r"(?P<kind>class|interface|enum|record|@interface)\s+(?P<name>[A-Za-z_]\w*)",
    re.M,
)
PACKAGE_RE = re.compile(r"^\s*package\s+([\w.]+)\s*;", re.M)


def extract_java_types(root: Path) -> list[TypeUnit]:
    units: list[TypeUnit] = []
    for path in root.rglob("*.java"):
        try:
            text = path.read_text(encoding="utf-8", errors="replace")
        except OSError:
            continue
        pkg_m = PACKAGE_RE.search(text)
        package = pkg_m.group(1) if pkg_m else ""
        rel = str(path.relative_to(root)).replace("\\", "/")
        for m in JAVA_TYPE_RE.finditer(text):
            mods = [x for x in m.group("mods").split() if x not in {"", "\t"}]
            start = text.count("\n", 0, m.start()) + 1
            units.append(
                TypeUnit(
                    file=rel,
                    package=package,
                    name=m.group("name"),
                    kind=m.group("kind"),
                    start_line=start,
                    modifiers=mods,
                )
            )
    return units


def build_inventory(
    root: Path,
    complex_hits: list[ComplexityHit],
    out_json: Path,
    out_md: Path,
) -> dict:
    types = extract_java_types(root)
    by_pkg: dict[str, int] = {}
    for t in types:
        by_pkg[t.package or "(default)"] = by_pkg.get(t.package or "(default)", 0) + 1

    # 核心意图候选：高复杂度 + 常见框架关键字
    keywords = (
        "BeanFactory",
        "ApplicationContext",
        "BeanDefinition",
        "Autowired",
        "DispatcherServlet",
        "HandlerMapping",
        "ProxyFactory",
        "Transaction",
        "Resolver",
        "Factory",
        "Registry",
        "Processor",
        "Interceptor",
        "Advisor",
        "Expression",
    )
    intent_candidates = []
    for t in types:
        score = 0
        for kw in keywords:
            if kw.lower() in t.name.lower():
                score += 2
        if any(h.file == t.file and h.cyclomatic >= 10 for h in complex_hits):
            score += 3
        if score:
            intent_candidates.append({"type": asdict(t), "score": score})
    intent_candidates.sort(key=lambda x: -x["score"])

    payload = {
        "type_count": len(types),
        "packages": dict(sorted(by_pkg.items(), key=lambda kv: -kv[1])),
        "types": [asdict(t) for t in types[:5000]],
        "intent_candidates": intent_candidates[:300],
        "complex_method_count": len(complex_hits),
    }
    write_text(out_json, json.dumps(payload, ensure_ascii=False, indent=2))

    lines = [
        "# 代码清单与意图候选",
        "",
        f"- Java 类型数: **{len(types)}**",
        f"- 高复杂度方法数: **{len(complex_hits)}**",
        "",
        "## Top 包",
        "",
    ]
    for pkg, n in list(payload["packages"].items())[:40]:
        lines.append(f"- `{pkg}`: {n}")
    lines += ["", "## 意图架构候选类型（优先深入注释）", ""]
    for item in intent_candidates[:80]:
        t = item["type"]
        lines.append(
            f"- score={item['score']} `{t['package']}.{t['name']}` ({t['kind']}) — `{t['file']}:{t['start_line']}`"
        )
    write_text(out_md, "\n".join(lines) + "\n")
    return payload
