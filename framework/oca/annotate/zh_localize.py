from __future__ import annotations

import re
from dataclasses import dataclass, field
from pathlib import Path

from ..static.complexity import TRIVIAL_NAME_RE
from .translator import ZhTranslator, _mostly_chinese

COPYRIGHT_HINT = "Licensed under the Apache License"


@dataclass
class LocalizeStats:
    files: int = 0
    javadocs: int = 0
    line_comments: int = 0
    fields_added: int = 0
    methods_added: int = 0
    skipped: int = 0
    errors: list[str] = field(default_factory=list)


# 捕获前导缩进，避免翻译后丢失 tab/空格
JAVADOC_RE = re.compile(r"(?P<indent>[ \t]*)/\*\*.*?\*/", re.S)
LINE_COMMENT_RE = re.compile(r"(?P<indent>^[ \t]*)//(?P<body>.*)$", re.M)

# 更稳的占位符（避免被翻译器吃掉）
_TOKEN = "OCAJAVA{0}DOC"


def _protect_inline(text: str) -> tuple[str, list[str]]:
    items: list[str] = []

    def repl(m: re.Match[str]) -> str:
        items.append(m.group(0))
        return _TOKEN.format(len(items) - 1)

    # 保护 {@...}、`...`、常见 HTML 标签（不要把泛型 <T> 当成标签）
    protected = re.sub(
        r"\{@[^{}]+\}|`[^`]+`|</?(?:p|em|strong|code|pre|ul|ol|li|b|i|br|a)(?:\s[^>]*)?>",
        repl,
        text,
        flags=re.I,
    )
    return protected, items


def _restore_inline(text: str, items: list[str]) -> str:
    out = text
    for i, raw in enumerate(items):
        for cand in (
            _TOKEN.format(i),
            _TOKEN.format(i).lower(),
            f"ocajava{i}doc",
            f"OCAJAVA{i}DOC",
        ):
            out = out.replace(cand, raw)
    return out


def translate_text(tr: ZhTranslator, text: str) -> str:
    text = text.strip()
    if not text or _mostly_chinese(text):
        return text
    protected, items = _protect_inline(text)
    protected = re.sub(r"[ \t]+", " ", protected)
    zh = tr.translate(protected)
    zh = _restore_inline(zh, items)
    # 保护失败残留时，至少保证 {@link} 等原文还在 items 里被替换；
    # 对 @param 描述这类短句若仍是英文，再裸译一次
    if not _mostly_chinese(zh) and re.search(r"[A-Za-z]{3,}", zh) and "OCAJAVA" not in protected:
        zh2 = tr.translate(re.sub(r"\{@[^{}]+\}|`[^`]+`|</?[a-zA-Z][^>]*>", " ", text))
        if _mostly_chinese(zh2):
            # 把链接补回末尾语义较弱，直接用带恢复的 zh；短描述用 zh2
            if len(text) < 80:
                return zh2
    return zh


def _wrap_javadoc(text: str, indent: str, width: int = 88) -> list[str]:
    prefix = f"{indent} * "
    if not text:
        return [f"{indent} *"]
    if _mostly_chinese(text) or " " not in text.strip()[:20]:
        s = text.replace("\n", "")
        lines = []
        buf = ""
        for ch in s:
            buf += ch
            if len(buf) >= width:
                lines.append(prefix + buf)
                buf = ""
        if buf:
            lines.append(prefix + buf)
        return lines
    words = text.split()
    lines: list[str] = []
    buf: list[str] = []
    n = 0
    for w in words:
        if n + len(w) + 1 > width and buf:
            lines.append(prefix + " ".join(buf))
            buf = [w]
            n = len(w)
        else:
            buf.append(w)
            n += len(w) + 1
    if buf:
        lines.append(prefix + " ".join(buf))
    return lines


def translate_javadoc_block(block: str, tr: ZhTranslator, indent: str = "") -> str:
    if _mostly_chinese(block):
        # 仍确保带上调用方缩进（block 可能已被去掉前导空白）
        if indent and not block.startswith(indent):
            return "\n".join(indent + ln.lstrip(" \t") if ln.strip() else ln for ln in block.splitlines())
        return block
    first = block.splitlines()[0]
    m_ind = re.match(r"^([ \t]*)/\*\*", first)
    # 仅当 block 自身带缩进时覆盖；否则保留调用方传入的 indent
    if m_ind and m_ind.group(1):
        indent = m_ind.group(1)

    lines = block.splitlines()
    out = [f"{indent}/**"]
    desc_buf: list[str] = []
    tag_lines: list[str] = []

    body = lines[1:-1] if len(lines) >= 2 else []
    for ln in body:
        m = re.match(r"^\s*\*\s?(.*)$", ln)
        content = m.group(1) if m else ln.strip()
        if content.startswith("@"):
            if desc_buf:
                out.extend(_emit_desc(desc_buf, indent, tr))
                desc_buf = []
            tag_lines.append(content)
        else:
            if tag_lines:
                tag_lines.append(content)
            else:
                desc_buf.append(content)

    if desc_buf:
        out.extend(_emit_desc(desc_buf, indent, tr))

    i = 0
    while i < len(tag_lines):
        cont = [tag_lines[i]]
        j = i + 1
        while j < len(tag_lines) and not tag_lines[j].startswith("@"):
            cont.append(tag_lines[j])
            j += 1
        out.extend(_emit_tag(cont, indent, tr))
        i = j

    out.append(f"{indent} */")
    return "\n".join(out)


def _emit_desc(parts: list[str], indent: str, tr: ZhTranslator) -> list[str]:
    paragraphs: list[list[str]] = [[]]
    for p in parts:
        if not p.strip():
            paragraphs.append([])
        else:
            paragraphs[-1].append(p.strip())
    out: list[str] = []
    for para in paragraphs:
        if not para:
            out.append(f"{indent} *")
            continue
        text = " ".join(para)
        zh = translate_text(tr, re.sub(r"\s+", " ", text))
        out.extend(_wrap_javadoc(zh, indent))
    while out and out[-1] == f"{indent} *":
        out.pop()
    return out or [f"{indent} *"]


def _emit_tag(cont: list[str], indent: str, tr: ZhTranslator) -> list[str]:
    first = cont[0].strip()
    rest = " ".join(x.strip() for x in cont[1:] if x.strip())
    m = re.match(
        r"@(param|return|throws|exception|deprecated|implNote|apiNote|since|author|see|version|serial)\b(\s+\S+)?\s*(.*)$",
        first,
    )
    if not m:
        zh = translate_text(tr, (first + " " + rest).strip())
        return _wrap_javadoc(zh, indent)

    tag, name, desc = m.group(1), (m.group(2) or "").strip(), (m.group(3) or "")
    desc_full = (desc + (" " + rest if rest else "")).strip()

    if tag in {"author", "since", "version", "see", "serial"}:
        line = f"{indent} * @{tag}"
        if name:
            line += f" {name}"
        if desc_full:
            line += f" {desc_full}"
        return [line.rstrip()]

    zh_desc = translate_text(tr, desc_full) if desc_full else ""
    if tag == "param":
        return [f"{indent} * @param {name} {zh_desc}".rstrip()]
    if tag == "return":
        return [f"{indent} * @return {zh_desc}".rstrip()]
    if tag in {"throws", "exception"}:
        return [f"{indent} * @{tag} {name} {zh_desc}".rstrip()]
    if tag == "deprecated":
        return [f"{indent} * @deprecated {zh_desc}".rstrip()]
    line = f"{indent} * @{tag}"
    if name:
        line += f" {name}"
    if zh_desc:
        line += f" {zh_desc}"
    return [line.rstrip()]


def translate_line_comments(text: str, tr: ZhTranslator) -> tuple[str, int]:
    count = 0

    def repl(m: re.Match[str]) -> str:
        nonlocal count
        indent, body = m.group("indent"), m.group("body")
        raw = body.strip()
        if not raw or raw.startswith("[OCA") or raw.startswith("http") or "://" in raw:
            return m.group(0)
        if _mostly_chinese(raw) or not re.search(r"[A-Za-z]{3,}", raw):
            return m.group(0)
        if len(raw) > 180:
            return m.group(0)
        zh = translate_text(tr, raw)
        count += 1
        return f"{indent}// {zh}"

    return LINE_COMMENT_RE.sub(repl, text), count


def _camel_to_words(name: str) -> str:
    s = re.sub(r"([a-z0-9])([A-Z])", r"\1 \2", name)
    s = re.sub(r"([A-Z]+)([A-Z][a-z])", r"\1 \2", s)
    return s.replace("_", " ").strip()


def _zh_field_comment(name: str) -> str:
    # 常见完整字段名优先
    exact = {
        "mappedConstructor": "用于实例化映射目标的可解析构造器。",
        "constructorParameterNames": "构造器参数名列表，对应要绑定的列名。",
        "constructorParameterTypes": "构造器各参数的类型描述，用于 JDBC 值转换。",
        "logger": "日志记录器。",
        "beanFactory": "底层 BeanFactory 引用。",
        "parentBeanFactory": "父 BeanFactory，用于层次化查找。",
        "applicationContext": "所属 ApplicationContext。",
    }
    if name in exact:
        return exact[name]

    # 领域友好的短中文，不依赖机翻套话
    words = _camel_to_words(name)
    mapping_hint = {
        "constructor": "构造器",
        "parameter": "参数",
        "type": "类型",
        "name": "名称",
        "names": "名称列表",
        "types": "类型列表",
        "value": "值",
        "values": "值集合",
        "factory": "工厂",
        "resolver": "解析器",
        "registry": "注册表",
        "cache": "缓存",
        "config": "配置",
        "context": "上下文",
        "handler": "处理器",
        "interceptor": "拦截器",
        "mapper": "映射器",
        "template": "模板",
        "datasource": "数据源",
        "connection": "连接",
        "statement": "语句",
        "result": "结果",
        "exception": "异常",
        "logger": "日志记录器",
        "lock": "锁",
        "monitor": "监视器",
        "listener": "监听器",
        "executor": "执行器",
        "converter": "转换器",
        "editor": "编辑器",
        "bean": "Bean",
        "class": "类",
        "method": "方法",
        "field": "字段",
        "property": "属性",
        "properties": "属性集",
        "source": "来源",
        "target": "目标",
        "parent": "父级",
        "child": "子级",
        "singleton": "单例",
        "prototype": "原型",
        "scope": "作用域",
        "attribute": "属性",
        "message": "消息",
        "transaction": "事务",
        "proxy": "代理",
        "advisor": "通知器",
        "advice": "通知",
        "pointcut": "切点",
    }
    low = words.lower()
    for en, zh in mapping_hint.items():
        if en in low:
            return f"{zh}相关状态（`{name}`）。"
    return f"`{name}`：该类的成员状态。"


def _zh_method_comment(name: str, *, ctor: bool = False, type_name: str = "") -> str:
    if ctor:
        return f"创建 `{type_name or '该类'}` 的新实例。"
    if TRIVIAL_NAME_RE.match(name):
        if name.startswith("get") and len(name) > 3:
            return f"获取 {_camel_to_words(name[3:])}（`{name[3:]}`）。"
        if name.startswith("set") and len(name) > 3:
            return f"设置 {_camel_to_words(name[3:])}（`{name[3:]}`）。"
        if name.startswith("is") and len(name) > 2:
            return f"判断是否 {_camel_to_words(name[2:])}。"
        if name.startswith("has") and len(name) > 3:
            return f"判断是否包含/具备 {_camel_to_words(name[3:])}。"
        if name in {"equals", "hashCode", "toString"}:
            return {"equals": "比较是否相等。", "hashCode": "计算哈希值。", "toString": "返回字符串表示。"}[name]
    # 常见动词
    verbs = [
        ("initialize", "初始化"),
        ("destroy", "销毁"),
        ("create", "创建"),
        ("build", "构建"),
        ("resolve", "解析"),
        ("register", "注册"),
        ("unregister", "注销"),
        ("add", "添加"),
        ("remove", "移除"),
        ("clear", "清空"),
        ("reset", "重置"),
        ("update", "更新"),
        ("convert", "转换"),
        ("translate", "转译"),
        ("execute", "执行"),
        ("invoke", "调用"),
        ("apply", "应用"),
        ("handle", "处理"),
        ("process", "处理"),
        ("parse", "解析"),
        ("validate", "校验"),
        ("check", "检查"),
        ("find", "查找"),
        ("lookup", "查找"),
        ("load", "加载"),
        ("save", "保存"),
        ("flush", "刷新"),
        ("refresh", "刷新"),
        ("close", "关闭"),
        ("open", "打开"),
        ("start", "启动"),
        ("stop", "停止"),
        ("bind", "绑定"),
        ("map", "映射"),
        ("match", "匹配"),
        ("extract", "提取"),
        ("construct", "构造"),
        ("instantiate", "实例化"),
        ("populate", "填充"),
        ("prepare", "准备"),
        ("after", "在…之后回调"),
        ("before", "在…之前回调"),
        ("do", "执行核心逻辑"),
    ]
    low = name.lower()
    for en, zh in verbs:
        if low.startswith(en):
            rest = _camel_to_words(name[len(en) :])
            if rest:
                return f"{zh}：{rest}（方法 `{name}`）。"
            return f"{zh}（方法 `{name}`）。"
    return f"方法 `{name}`：完成本类中与「{_camel_to_words(name)}」相关的职责。"


def _line_starts(text: str) -> list[int]:
    starts = [0]
    for i, ch in enumerate(text):
        if ch == "\n":
            starts.append(i + 1)
    return starts


def _line_no(starts: list[int], pos: int) -> int:
    # 1-based
    lo, hi = 0, len(starts) - 1
    while lo <= hi:
        mid = (lo + hi) // 2
        if starts[mid] <= pos:
            lo = mid + 1
        else:
            hi = mid - 1
    return hi + 1


def _prev_nonempty_line(lines: list[str], idx0: int) -> tuple[int, str]:
    i = idx0 - 1
    while i >= 0:
        if lines[i].strip():
            return i, lines[i]
        i -= 1
    return -1, ""


def _annotation_block_start(lines: list[str], decl_line0: int) -> int:
    """从声明行向上吞掉注解，返回应插入注释的行号。"""
    i = decl_line0
    j = i - 1
    while j >= 0:
        s = lines[j].strip()
        if s.startswith("@"):
            i = j
            j -= 1
            continue
        break
    return i


def _has_javadoc_above(lines: list[str], insert_at: int) -> bool:
    j = insert_at - 1
    while j >= 0 and lines[j].strip() == "":
        j -= 1
    if j < 0:
        return False
    # 向上看是否是 javadoc 结束
    if lines[j].strip().endswith("*/"):
        # 确认是 /**
        k = j
        while k >= 0 and "/**" not in lines[k]:
            if lines[k].strip() and not lines[k].lstrip().startswith("*") and "*/" not in lines[k]:
                break
            k -= 1
        return k >= 0 and "/**" in lines[k]
    return False


def add_missing_member_docs(text: str, stats: LocalizeStats) -> str:
    lines = text.splitlines(keepends=True)
    plain = [ln.rstrip("\n\r") for ln in text.splitlines()]

    # 找当前顶层类型名（用于构造器）
    type_name = ""
    for ln in plain:
        m = re.search(r"\b(class|interface|enum|record)\s+([A-Za-z_]\w*)", ln)
        if m:
            type_name = m.group(2)
            break

    insertions: list[tuple[int, str]] = []  # insert_at_line0, block

    field_re = re.compile(
        r"^(?P<indent>\t| {4})(?P<vis>public|protected|private)\s+"
        r"(?P<rest>.+?)\s+(?P<name>[A-Za-z_]\w*)\s*(?:=|;)\s*$"
    )
    method_re = re.compile(
        r"^(?P<indent>\t| {4})(?P<vis>public|protected|private)\s+"
        r"(?P<rest>[^{;]*?)\s*(?P<tail>\{|;)\s*$"
    )
    method_name_re = re.compile(
        r"(?:^|\s)([A-Za-z_]\w*)\s*\((.*)\)\s*(?:throws\s+[\w.,\s]+)?$"
    )

    i = 0
    while i < len(plain):
        ln = plain[i]
        # 跳过注释块
        if "/**" in ln or ln.strip().startswith("/*"):
            if "*/" not in ln:
                while i < len(plain) and "*/" not in plain[i]:
                    i += 1
            i += 1
            continue

        fm = field_re.match(ln)
        if fm and "(" not in fm.group("rest").split("=")[0]:
            # 排除方法
            insert_at = _annotation_block_start(plain, i)
            if not _has_javadoc_above(plain, insert_at):
                indent = fm.group("indent")
                name = fm.group("name")
                comment = f"{indent}/** {_zh_field_comment(name)} */\n"
                insertions.append((insert_at, comment))
                stats.fields_added += 1
            i += 1
            continue

        # 方法：允许签名跨多行，从可见性行开始收集直到 { 或 ;
        vm = re.match(r"^(?P<indent>\t| {4})(?P<vis>public|protected|private)\b(?P<body>.*)$", ln)
        if vm and "(" in ln or (
            vm and i + 1 < len(plain) and "(" in "".join(plain[i : min(i + 6, len(plain))])
        ):
            if not vm:
                i += 1
                continue
            # 不像字段
            if field_re.match(ln) and "(" not in ln:
                i += 1
                continue
            indent = vm.group("indent")
            chunk = ln
            j = i
            while j < len(plain) and "{" not in chunk and not chunk.rstrip().endswith(";"):
                j += 1
                if j < len(plain):
                    chunk += " " + plain[j].strip()
            # 提取方法名
            # 去掉 throws 后内容便于匹配
            sig = chunk
            sig = re.sub(r"\s+", " ", sig).strip()
            # 去掉前缀修饰与泛型方法
            sig2 = re.sub(
                r"^(public|protected|private|static|final|native|synchronized|abstract|default|strictfp|@\w+(?:\([^)]*\))?)\s+",
                "",
                sig,
            )
            while re.match(
                r"^(public|protected|private|static|final|native|synchronized|abstract|default|strictfp|@\w+(?:\([^)]*\))?|<[^>]+>)\s+",
                sig2,
            ):
                sig2 = re.sub(
                    r"^(public|protected|private|static|final|native|synchronized|abstract|default|strictfp|@\w+(?:\([^)]*\))?|<[^>]+>)\s+",
                    "",
                    sig2,
                )
            mn = re.search(r"([A-Za-z_]\w*)\s*\(", sig2)
            if not mn:
                i = j + 1
                continue
            name = mn.group(1)
            # 过滤非方法：if/for 等
            if name in {"if", "for", "while", "switch", "catch", "return", "new"}:
                i = j + 1
                continue
            # 构造器
            is_ctor = name == type_name
            # 必须是方法形态
            if "(" not in sig2:
                i = j + 1
                continue
            insert_at = _annotation_block_start(plain, i)
            if not _has_javadoc_above(plain, insert_at):
                zh = _zh_method_comment(name, ctor=is_ctor, type_name=type_name)
                comment = f"{indent}/**\n{indent} * {zh}\n{indent} */\n"
                insertions.append((insert_at, comment))
                stats.methods_added += 1
            i = j + 1
            continue

        i += 1

    if not insertions:
        return text
    # 按行号倒序插入
    insertions.sort(key=lambda x: x[0], reverse=True)
    for line0, block in insertions:
        lines[line0:line0] = [block]
    return "".join(lines)


def localize_java_source(text: str, tr: ZhTranslator, stats: LocalizeStats) -> str:
    # 1) 翻译已有 JavaDoc
    def javadoc_repl(m: re.Match[str]) -> str:
        full = m.group(0)
        indent = m.group("indent") or ""
        # 去掉前导缩进再解析，translate 时重新套上
        block = full[len(indent) :]
        if COPYRIGHT_HINT in block:
            return full
        new_block = translate_javadoc_block(block, tr, indent=indent)
        if new_block != full and new_block != block:
            stats.javadocs += 1
        # translate_javadoc_block 已带 indent
        return new_block

    text = JAVADOC_RE.sub(javadoc_repl, text)

    # 2) 翻译 // 行注释
    text, n = translate_line_comments(text, tr)
    stats.line_comments += n

    # 3) 补齐缺失的字段/方法中文注释
    text = add_missing_member_docs(text, stats)
    return text


def localize_tree(
    root: Path,
    tr: ZhTranslator,
    *,
    only_globs: list[str] | None = None,
    max_files: int | None = None,
    modules: list[str] | None = None,
) -> LocalizeStats:
    stats = LocalizeStats()
    files = sorted(root.rglob("*.java"))
    selected: list[Path] = []
    for p in files:
        rel = str(p.relative_to(root)).replace("\\", "/")
        if "/src/main/java/" not in rel:
            continue
        if modules and not any(rel.startswith(m.rstrip("/") + "/") or rel == m for m in modules):
            continue
        if only_globs:
            import fnmatch

            if not any(fnmatch.fnmatch(rel, g) for g in only_globs):
                continue
        selected.append(p)

    for p in selected:
        if max_files is not None and stats.files >= max_files:
            break
        rel = str(p.relative_to(root)).replace("\\", "/")
        try:
            original = p.read_text(encoding="utf-8", errors="replace")
            # 已基本中文化的文件跳过（避免重复机翻破坏）
            if original.count("\u4e00") > 80 and original.count("/**") > 0:
                # 仍可能缺字段注释；仅当中文密度很高时跳过
                cn = sum(1 for ch in original if "\u4e00" <= ch <= "\u9fff")
                en = sum(1 for ch in original if ch.isascii() and ch.isalpha())
                if cn > 200 and cn * 2 > en:
                    stats.skipped += 1
                    continue
            updated = localize_java_source(original, tr, stats)
            if updated != original:
                p.write_text(updated, encoding="utf-8")
                stats.files += 1
                print(f"[oca-zh] localized {rel}", flush=True)
            else:
                stats.skipped += 1
            if stats.files % 5 == 0:
                tr.flush()
        except Exception as ex:
            stats.errors.append(f"{rel}: {ex}")
            print(f"[oca-zh] error {rel}: {ex}", flush=True)
    tr.flush()
    return stats
