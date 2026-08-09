from __future__ import annotations

import json
import re
from dataclasses import dataclass, field
from pathlib import Path

from ..static.complexity import TRIVIAL_NAME_RE, ComplexityHit
from ..util.fs import write_text


OCA_MARK_BEGIN = "/* ===== [OCA 中文解析] ====="
OCA_MARK_END = "===== [OCA 中文解析结束] ===== */"
OCA_LINE_MARK = "// [OCA]"


@dataclass
class AnnotationPlan:
    """外部/LLM 提供的注释计划，按文件相对路径索引。"""

    files: dict[str, dict] = field(default_factory=dict)
    # files[rel] = {
    #   "file_summary": "...",
    #   "types": {"ClassName": "说明"},
    #   "fields": {"ClassName.field": "说明"},
    #   "methods": {"ClassName.method": {"summary": "...", "inline": {lineNo: "..."}}},
    # }


def load_plan(path: Path | None) -> AnnotationPlan:
    if not path or not path.exists():
        return AnnotationPlan()
    data = json.loads(path.read_text(encoding="utf-8"))
    return AnnotationPlan(files=data.get("files", data))


def _already_annotated(text: str) -> bool:
    return OCA_MARK_BEGIN in text or OCA_LINE_MARK in text


def _java_doc_block(lines: list[str], indent: str = "") -> str:
    body = "\n".join(f"{indent} * {ln}" if ln else f"{indent} *" for ln in lines)
    return f"{indent}/**\n{body}\n{indent} */\n"


def _oca_block(title: str, paragraphs: list[str], indent: str = "") -> str:
    content = [title, ""] + paragraphs
    inner = "\n".join(content)
    return f"{indent}{OCA_MARK_BEGIN}\n{inner}\n{indent}{OCA_MARK_END}\n"


def _insert_before_line(lines: list[str], line_idx0: int, block: str) -> list[str]:
    """在指定行前插入注释；若上方已有 javadoc，则插在 javadoc 之前。"""
    i = line_idx0
    # 向上跳过空白
    j = i - 1
    while j >= 0 and lines[j].strip() == "":
        j -= 1
    # 若紧邻 javadoc 结束 */，继续上找到 /**
    if j >= 0 and lines[j].strip().endswith("*/"):
        k = j
        while k >= 0 and "/**" not in lines[k]:
            k -= 1
        if k >= 0 and "/**" in lines[k]:
            i = k
    # 避免重复插入
    window = "\n".join(lines[max(0, i - 8) : i])
    if OCA_MARK_BEGIN in window:
        return lines
    block_lines = block.splitlines(keepends=True)
    if block_lines and not block_lines[-1].endswith("\n"):
        block_lines[-1] += "\n"
    return lines[:i] + ["".join(block_lines)] + lines[i:]


# 仅匹配带可见性修饰符的「类成员字段」，避免误伤方法内局部变量
FIELD_RE = re.compile(
    r"^(?P<indent>\t| {4})(?P<mods>(?:public|protected|private)\s+(?:static\s+|final\s+|volatile\s+|transient\s+)*)"
    r"(?P<type>[\w.<>,\s\[\]]+?)\s+(?P<name>[A-Za-z_]\w*)\s*(?:=|;)",
    re.M,
)
# mods 只允许 Java 修饰符关键字，避免匹配注释里的英文 “subclass to ...”
TYPE_RE = re.compile(
    r"^(?P<indent>\s*)(?P<mods>(?:(?:public|protected|private|static|final|abstract|sealed|non-sealed|"
    r"strictfp|@\w+(?:\([^)]*\))?)\s+)*)"
    r"(?P<kind>class|interface|enum|record|@interface)\s+(?P<name>[A-Za-z_]\w*)\b",
    re.M,
)
# 要求至少有可见性修饰符，避免把 `return doGetBean(...)` 误识别为方法定义
METHOD_RE = re.compile(
    r"^(?P<indent>\s*)(?P<mods>(?:(?:public|protected|private)\s+)(?:(?:static|final|native|synchronized|"
    r"abstract|default|strictfp)\s+)*)"
    r"(?:<[^>\n]+>\s*)?"
    r"(?P<rtype>[\w.<>,\[\]?]+\s+)+"
    r"(?P<name>[A-Za-z_]\w*)\s*\((?P<params>[^;{}]*?)\)\s*(?:throws\s+[\w.,\s]+)?\{",
    re.M,
)
METHOD_NAME_BLOCKLIST = {
    "if",
    "for",
    "while",
    "switch",
    "catch",
    "return",
    "new",
    "throw",
    "assert",
    "class",
}


def _heuristic_type_summary(name: str, kind: str, file_rel: str) -> str:
    hints = []
    lname = name.lower()
    mapping = [
        ("beanfactory", "Bean 工厂：存在与获取 Bean 实例的核心入口"),
        ("applicationcontext", "应用上下文：在 BeanFactory 之上提供企业级容器能力"),
        ("beandefinition", "Bean 定义元数据：描述如何创建与装配一个 Bean"),
        ("autowired", "依赖注入相关：自动装配候选与注入点处理"),
        ("dispatcher", "前端控制器：请求分发与处理器映射"),
        ("handlermapping", "处理器映射：URL/条件 -> Handler"),
        ("proxy", "代理相关：AOP/事务等横切能力的载体"),
        ("advisor", "通知器：Pointcut + Advice 的组合"),
        ("transaction", "事务抽象：边界、同步与管理器"),
        ("expression", "表达式：解析与求值"),
        ("resolver", "解析器：名称/类型/占位符等到具体对象的转换"),
        ("registry", "注册表：名称到定义/实例的集中存放"),
        ("processor", "处理器：容器生命周期中的扩展钩子"),
        ("interceptor", "拦截器：调用链中的前置/后置逻辑"),
        ("factory", "工厂：封装复杂创建逻辑"),
    ]
    for key, text in mapping:
        if key in lname:
            hints.append(text)
            break
    if not hints:
        hints.append(f"{kind} `{name}`：请结合所属模块与调用方理解其在整体架构中的职责。")
    hints.append(f"源文件: `{file_rel}`")
    return "；".join(hints)


def _heuristic_method_summary(name: str, ccn: int, nloc: int) -> str:
    if TRIVIAL_NAME_RE.match(name) and ccn <= 2:
        return ""
    return (
        f"方法 `{name}` 复杂度较高（CCN≈{ccn}, NLOC≈{nloc}）。"
        f"阅读时建议先抓住主路径，再看分支/异常/缓存等旁路逻辑；"
        f"关注它在调用链中上下游的契约（入参约束、返回值语义、抛出的异常）。"
    )


def annotate_java_source(
    text: str,
    *,
    file_rel: str,
    plan_for_file: dict | None,
    complex_for_file: list[ComplexityHit],
    annotate_fields: bool = True,
) -> tuple[str, dict]:
    """返回 (新文本, 统计)."""
    stats = {"types": 0, "methods": 0, "fields": 0, "skipped_trivial": 0, "unchanged": False}
    if _already_annotated(text):
        stats["unchanged"] = True
        return text, stats

    plan_for_file = plan_for_file or {}
    lines = text.splitlines(keepends=True)
    # 用无 keepends 方便算行号匹配
    plain = text.splitlines()
    complex_by_name: dict[str, ComplexityHit] = {}
    for h in complex_for_file:
        short = h.name.split("::")[-1].split(".")[-1]
        complex_by_name[short] = h

    # 自下而上插入，避免行号漂移
    insertions: list[tuple[int, str, str]] = []  # (line0, kind, block)

    # types
    for m in TYPE_RE.finditer(text):
        name = m.group("name")
        kind = m.group("kind")
        indent = m.group("indent")
        line0 = text.count("\n", 0, m.start())
        summary = (plan_for_file.get("types") or {}).get(name) or _heuristic_type_summary(
            name, kind, file_rel
        )
        block = _oca_block(
            f"{kind} {name} — 意图说明",
            [summary, "", "（本注释由 open-code-analyzer 生成，置于原有文档注释之前）"],
            indent=indent,
        )
        insertions.append((line0, "type", block))

    # methods (only complex / planned)
    planned_methods = plan_for_file.get("methods") or {}
    for m in METHOD_RE.finditer(text):
        name = m.group("name")
        if name in METHOD_NAME_BLOCKLIST:
            continue
        indent = m.group("indent")
        line0 = text.count("\n", 0, m.start())
        # 跳过方法体内的嵌套匹配（类方法通常一级缩进）
        if indent.startswith("\t\t") or indent.startswith("        "):
            continue
        hit = complex_by_name.get(name)
        plan_m = planned_methods.get(name) or planned_methods.get(f"*.{name}")
        if plan_m is None and hit is None:
            if TRIVIAL_NAME_RE.match(name):
                stats["skipped_trivial"] += 1
            continue
        if plan_m is None and hit and TRIVIAL_NAME_RE.match(name) and hit.cyclomatic <= 2:
            stats["skipped_trivial"] += 1
            continue
        if isinstance(plan_m, str):
            summary = plan_m
        elif isinstance(plan_m, dict):
            summary = plan_m.get("summary") or ""
        else:
            summary = ""
        if not summary and hit:
            summary = _heuristic_method_summary(name, hit.cyclomatic, hit.nloc)
        if not summary:
            continue
        block = _oca_block(
            f"方法 {name} — 意图与阅读要点",
            [summary],
            indent=indent,
        )
        insertions.append((line0, "method", block))

    # fields（仅类成员；无计划且名字像 trivial 计数器时可给短说明）
    if annotate_fields:
        planned_fields = plan_for_file.get("fields") or {}
        for m in FIELD_RE.finditer(text):
            name = m.group("name")
            indent = m.group("indent")
            line0 = text.count("\n", 0, m.start())
            snippet = plain[line0] if line0 < len(plain) else ""
            # 跳过方法签名/注解参数等误匹配
            if "(" in snippet.split("=")[0]:
                continue
            # 跳过注释行
            stripped = snippet.lstrip()
            if stripped.startswith("//") or stripped.startswith("*") or stripped.startswith("/*"):
                continue
            key_hits = [k for k in planned_fields if k == name or k.endswith("." + name)]
            if key_hits:
                summary = planned_fields[key_hits[0]]
            else:
                # 无计划时给极短中文占位，避免空话套话
                summary = f"字段 `{name}`：类成员状态。"
            if OCA_LINE_MARK in snippet:
                continue
            block = f"{indent}{OCA_LINE_MARK} {summary}\n"
            insertions.append((line0, "field", block))

    insertions.sort(key=lambda x: x[0], reverse=True)
    for line0, kind, block in insertions:
        lines = _insert_before_line(lines, line0, block)
        stats[{"type": "types", "method": "methods", "field": "fields"}[kind]] += 1

    # file summary at top
    file_summary = plan_for_file.get("file_summary")
    if file_summary:
        header = _oca_block("文件意图总览", [file_summary])
        body = "".join(lines)
        if "package " in body:
            # 插在 package 之前
            pkg_idx = body.find("package ")
            body = body[:pkg_idx] + header + body[pkg_idx:]
        else:
            body = header + body
        return body, stats

    return "".join(lines), stats


def annotate_java_tree(
    analyzed_root: Path,
    *,
    plan: AnnotationPlan,
    complex_hits: list[ComplexityHit],
    annotate_fields: bool = True,
    only_files: set[str] | None = None,
    max_files: int | None = None,
) -> dict:
    by_file: dict[str, list[ComplexityHit]] = {}
    for h in complex_hits:
        by_file.setdefault(h.file, []).append(h)

    # 优先：计划内文件 + 高复杂度文件
    candidates: list[str] = []
    for rel in plan.files:
        candidates.append(rel.replace("\\", "/"))
    ranked = sorted(
        by_file.items(),
        key=lambda kv: -max(x.cyclomatic for x in kv[1]),
    )
    for rel, _ in ranked:
        if rel not in candidates:
            candidates.append(rel)

    if only_files:
        candidates = [c for c in candidates if c in only_files]

    stats_all = {"files_touched": 0, "types": 0, "methods": 0, "fields": 0, "skipped": 0}
    touched = 0
    for rel in candidates:
        if max_files is not None and touched >= max_files:
            break
        path = analyzed_root / rel
        if not path.exists() or path.suffix != ".java":
            continue
        text = path.read_text(encoding="utf-8", errors="replace")
        new_text, st = annotate_java_source(
            text,
            file_rel=rel,
            plan_for_file=plan.files.get(rel),
            complex_for_file=by_file.get(rel, []),
            annotate_fields=annotate_fields,
        )
        if st.get("unchanged"):
            stats_all["skipped"] += 1
            continue
        if new_text != text:
            write_text(path, new_text)
            stats_all["files_touched"] += 1
            stats_all["types"] += st["types"]
            stats_all["methods"] += st["methods"]
            stats_all["fields"] += st["fields"]
            touched += 1
    return stats_all
