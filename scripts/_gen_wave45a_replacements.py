#!/usr/bin/env python3
"""Generate wave45a_replacements_redisson.py from originals with exact javadoc matching."""
from __future__ import annotations

import re
from pathlib import Path

ROOT = Path("/workspace")
ORIG = ROOT / "redisson/redisson-4.7.0/original"
OUT = ROOT / "scripts/wave45a_replacements_redisson.py"
_A = "redisson/src/main/java/org/redisson/api/"

FILES = [
    ln.strip()
    for ln in Path("/tmp/re45a.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]

TRANSLATIONS: dict[str, str] = {}


def t(en: str, cn: str) -> None:
    TRANSLATIONS[en.strip()] = cn.strip()


def extract_javadocs(text: str) -> list[str]:
    return re.findall(r"/\*\*.*?\*/", text, re.DOTALL)


def main_desc(jdoc: str) -> str:
    for line in jdoc.splitlines():
        s = line.strip()
        if s.startswith("*") and not s.startswith("* @") and s not in ("*/", "/**", "*"):
            return s[1:].strip()
    return ""


CONTINUATION: dict[str, str] = {
    "which are within the borders of specified search conditions.": (
        "在指定搜索条件范围内查找成员。"
    ),
    "using the rate configuration set through": "使用已通过以下方法设置的速率配置：",
    "{@link #trySetRate(long, long, Duration)} or {@link #setRate(long, long, Duration)} method.": (
        "{@link #trySetRate(long, long, Duration)} 或 {@link #setRate(long, long, Duration)}。"
    ),
    "{@link #trySetRateAsync(long, long, Duration)} or {@link #setRateAsync(long, long, Duration)} method.": (
        "{@link #trySetRateAsync(long, long, Duration)} 或 {@link #setRateAsync(long, long, Duration)}。"
    ),
    "not including elements already existing for which": "不含已存在且仅更新分数的元素；",
    "the score was updated": "已存在成员的分数被更新时不计入新增数。",
    "Stores result to <code>destName</code>.": "将结果写入 {@code destName} 目标键。",
    "Stores result to <code>destName</code> sorted by distance.": (
        "将结果按距离排序后写入 {@code destName} 目标键。"
    ),
    "added into this instances and other instances defined through <code>otherLogNames</code>.": (
        "本实例与 {@code otherLogNames} 指定其他实例合并后的近似唯一元素数。"
    ),
    "Returns unique numbers but not monotonically increased.": (
        "返回唯一 Long 型 ID，但不保证严格单调递增。"
    ),
    "Applied only to functions which don't modify data.": (
        "仅适用于不修改数据的函数。"
    ),
    "Restored libraries are appended to the existing libraries and throws error in case of collision.": (
        "恢复的库追加到现有库；名称冲突时抛出错误。"
    ),
    "Restored libraries are appended to the existing libraries.": (
        "恢复的库追加到现有库。"
    ),
    "Deletes all existing libraries before restoring.": (
        "恢复前先删除所有现有库。"
    ),
    "Redis function and available execution engines.": (
        "当前运行的 Redis Function 及可用执行引擎。"
    ),
    "Usage examples:": "用法示例：",
}

CN_PARAM = {
    "libraryName": "函数库名称",
    "code": "函数库代码",
    "namePattern": "名称匹配模式",
    "payload": "序列化状态",
    "key": "路由键（Cluster 定位节点）",
    "mode": "执行模式",
    "name": "函数名称",
    "returnType": "返回值类型",
    "keys": "脚本 KEYS 参数",
    "values": "脚本 ARGV 参数",
    "maxBurst": "最大突发容量",
    "tokensPerPeriod": "每周期补充令牌数",
    "period": "补充周期",
    "tokens": "请求令牌数",
    "longitude": "经度",
    "latitude": "纬度",
    "member": "成员对象",
    "entries": "地理空间条目",
    "firstMember": "第一个成员",
    "secondMember": "第二个成员",
    "geoUnit": "距离单位",
    "members": "成员集合",
    "args": "搜索条件",
    "destName": "目标键名",
    "obj": "待添加元素",
    "objects": "待添加元素集合",
    "otherLogNames": "其他 HyperLogLog 键名",
    "value": "初始值",
    "allocationSize": "预分配区间大小",
}

CN_RETURN = {
    "void": None,
    "serialized state": "序列化状态",
    "list of libraries": "函数库列表",
    "function information": "函数运行信息",
    "result object": "执行结果",
    "rate configuration or {@code null} if the rate wasn't set": "速率配置；未设置时返回 {@code null}",
    "GCRA result": "GCRA 限流结果",
    "distance": "距离",
    "hash mapped by object": "成员到 Geohash 的映射",
    "geo position mapped by object": "成员到坐标的映射",
    "list of memebers": "成员列表",
    "distance mapped by object": "成员到距离的映射",
    "position mapped by object": "成员到坐标的映射",
    "length of result": "结果集长度",
    "number": "唯一元素近似基数",
    "approximated number of unique elements added into this structure": "已添加唯一元素的近似基数",
}

CLASS_OVERRIDES: dict[str, list[tuple[str, str]]] = {
    f"{_A}RFunctionRx.java": [
        (
            "/**\n * Interface for Redis Function feature\n *\n * @author Nikita Koksharov\n *\n */",
            "/**\n * Redis Function RxJava3 风格 API。\n"
            " * <p>各方法返回 {@link Completable}、{@link Single} 或 {@link Maybe}。\n"
            " *\n * @author Nikita Koksharov\n */",
        ),
    ],
    f"{_A}RFuture.java": [
        (
            "/**\n * Represents the result of an asynchronous computation\n * \n * @author Nikita Koksharov\n *\n * @param <V> type of value\n */",
            "/**\n * 表示异步计算结果的接口。\n"
            " * <p>扩展 {@link java.util.concurrent.Future} 与 {@link java.util.concurrent.CompletionStage}，"
            "Redisson 各异步 API 均返回此类型。\n"
            " *\n * @author Nikita Koksharov\n * @param <V> 结果值类型\n */",
        ),
    ],
    f"{_A}RGcra.java": [
        (
            "/**\n * Redis based GCRA object.\n * <p>\n * Requires <b>Redis 8.8.0 or higher.</b>\n *\n * @author Su Ko\n *\n */",
            "/**\n * 基于 Redis 的 GCRA（Generic Cell Rate Algorithm）限流对象。\n"
            " * <p>需要 <b>Redis 8.8.0 及以上</b>。\n"
            " *\n * @author Su Ko\n */",
        ),
    ],
    f"{_A}RGcraAsync.java": [
        (
            "/**\n * Async interface for Redis based GCRA object.\n * <p>\n * Requires <b>Redis 8.8.0 or higher.</b>\n *\n * @author Su Ko\n *\n */",
            "/**\n * {@link RGcra} 异步 API。\n"
            " * <p>各方法返回 {@link RFuture}；需要 <b>Redis 8.8.0 及以上</b>。\n"
            " *\n * @author Su Ko\n */",
        ),
    ],
    f"{_A}RGcraReactive.java": [
        (
            "/**\n * Reactive interface for Redis based GCRA object.\n * <p>\n * Requires <b>Redis 8.8.0 or higher.</b>\n *\n * @author Su Ko\n *\n */",
            "/**\n * {@link RGcra} Reactor 风格 API。\n"
            " * <p>各方法返回 {@link Mono}；需要 <b>Redis 8.8.0 及以上</b>。\n"
            " *\n * @author Su Ko\n */",
        ),
    ],
    f"{_A}RGcraRx.java": [
        (
            "/**\n * Rx interface for Redis based GCRA object.\n * <p>\n * Requires <b>Redis 8.8.0 or higher.</b>\n *\n * @author Su Ko\n *\n */",
            "/**\n * {@link RGcra} RxJava3 风格 API。\n"
            " * <p>各方法返回 {@link Single}；需要 <b>Redis 8.8.0 及以上</b>。\n"
            " *\n * @author Su Ko\n */",
        ),
    ],
    f"{_A}RGeo.java": [
        (
            "/**\n * Geospatial items holder. \n * \n * @author Nikita Koksharov\n *\n * @param <V> type of value\n */",
            "/**\n * Redis 地理空间（Geo）容器，存储带经纬度的成员。\n"
            " * <p>基于有序集合实现 GEOADD、GEODIST、GEORADIUS 等命令。\n"
            " *\n * @author Nikita Koksharov\n * @param <V> 成员类型\n */",
        ),
    ],
    f"{_A}RGeoAsync.java": [
        (
            "/**\n * Geospatial items holder. Asynchronous interface.\n * \n * @author Nikita Koksharov\n *\n * @param <V> type of value\n */",
            "/**\n * {@link RGeo} 异步 API。\n"
            " * <p>各方法返回 {@link RFuture}。\n"
            " *\n * @author Nikita Koksharov\n * @param <V> 成员类型\n */",
        ),
    ],
    f"{_A}RGeoReactive.java": [
        (
            "/**\n * Geospatial items holder. Reactive interface.\n * \n * @author Nikita Koksharov\n *\n * @param <V> type of value\n */",
            "/**\n * {@link RGeo} Reactor 风格 API。\n"
            " * <p>各方法返回 {@link Mono}。\n"
            " *\n * @author Nikita Koksharov\n * @param <V> 成员类型\n */",
        ),
    ],
    f"{_A}RGeoRx.java": [
        (
            "/**\n * Geospatial items holder. Reactive interface.\n * \n * @author Nikita Koksharov\n *\n * @param <V> type of value\n */",
            "/**\n * {@link RGeo} RxJava3 风格 API。\n"
            " * <p>各方法返回 {@link Single} 或 {@link Maybe}。\n"
            " *\n * @author Nikita Koksharov\n * @param <V> 成员类型\n */",
        ),
    ],
    f"{_A}RHyperLogLog.java": [
        (
            "/**\n * Probabilistic data structure that lets you maintain counts of millions of items with extreme space efficiency.\n * \n * @author Nikita Koksharov\n *\n * @param <V> type of stored values \n */",
            "/**\n * HyperLogLog 基数估算接口，以极小空间统计海量唯一元素。\n"
            " * <p>封装 {@code PFADD}、{@code PFCOUNT}、{@code PFMERGE} 等命令。\n"
            " *\n * @author Nikita Koksharov\n * @param <V> 元素类型\n */",
        ),
    ],
    f"{_A}RHyperLogLogAsync.java": [
        (
            "/**\n * Probabilistic data structure that lets you maintain counts of millions of items with extreme space efficiency.\n * Asynchronous interface.\n * \n * @author Nikita Koksharov\n *\n * @param <V> type of stored values\n */",
            "/**\n * {@link RHyperLogLog} 异步 API。\n"
            " * <p>各方法返回 {@link RFuture}。\n"
            " *\n * @author Nikita Koksharov\n * @param <V> 元素类型\n */",
        ),
    ],
    f"{_A}RHyperLogLogReactive.java": [
        (
            "/**\n * Probabilistic data structure that lets you maintain counts of millions of items with extreme space efficiency.\n * Reactive interface.\n * \n * @author Nikita Koksharov\n *\n * @param <V> type of stored values\n */",
            "/**\n * {@link RHyperLogLog} Reactor 风格 API。\n"
            " * <p>各方法返回 {@link Mono}。\n"
            " *\n * @author Nikita Koksharov\n * @param <V> 元素类型\n */",
        ),
    ],
    f"{_A}RHyperLogLogRx.java": [
        (
            "/**\n * Probabilistic data structure that lets you maintain counts of millions of items with extreme space efficiency.\n * RxJava2 interface.\n * \n * @author Nikita Koksharov\n *\n * @param <V> type of stored values\n */",
            "/**\n * {@link RHyperLogLog} RxJava3 风格 API。\n"
            " * <p>各方法返回 {@link Single} 或 {@link Completable}。\n"
            " *\n * @author Nikita Koksharov\n * @param <V> 元素类型\n */",
        ),
    ],
    f"{_A}RIdGenerator.java": [
        (
            "/**\n * Id generator of <code>Long</code> type numbers.\n * Returns unique numbers but not monotonically increased.\n *\n * @author Nikita Koksharov\n *\n */",
            "/**\n * Long 型分布式 ID 生成器。\n"
            " * <p>返回唯一 Long 型 ID，但不保证严格单调递增；"
            "本地预分配区间以减少 Redis 访问。\n"
            " *\n * @author Nikita Koksharov\n */",
        ),
    ],
}

# --- RFunctionRx / shared Function method translations (from wave44b) ---
t("Deletes library. Error is thrown if library doesn't exist.", "删除函数库；库不存在时抛出错误。")
t("Returns serialized state of all libraries.", "返回所有函数库的序列化状态。")
t("Deletes all libraries.", "删除所有已加载的函数库。")
t(
    "Kills currently executed functions.",
    "终止当前正在执行的函数（仅适用于不修改数据的函数）。",
)
t(
    "Returns information about libraries and functions per each.",
    "返回各函数库及其包含函数的信息列表。",
)
t(
    "Returns information about libraries and functions per each by name pattern.",
    "按名称模式（glob）返回匹配的函数库及函数信息。",
)
t("Loads a library. Error is thrown if library already exists.", "加载函数库；库已存在时抛出错误。")
t("Loads a library and overwrites existing library.", "加载函数库并覆盖同名已有库。")
t(
    "Restores libraries using their state returned by {@link #dump()} method.",
    "使用 {@link #dump()} 返回的状态恢复函数库。",
)
t(
    "Returns information about currently running",
    "返回当前正在运行的 Redis Function 及可用执行引擎信息。",
)
t("Executes function", "执行 Redis Function。")

# --- RGcra ---
t(
    "Sets the rate configuration only if it hasn't been set before.",
    "仅尚未配置时设置速率参数。",
)
t(
    "Sets the rate configuration overwriting the previous value and resetting the consumed tokens.",
    "覆盖设置速率参数并重置已消耗令牌。",
)
t(
    "Returns the rate configuration set through",
    "返回通过 trySetRate/setRate 设置的速率配置。",
)
t(
    "Applies the GCRA algorithm with a single token request",
    "以单令牌请求执行 GCRA 限流。",
)
t(
    "Applies the GCRA algorithm with a custom token request size",
    "以指定令牌数执行 GCRA 限流。",
)
t(
    "Applies the GCRA algorithm with a single token request.",
    "以单令牌请求执行 GCRA 限流（一次性参数，已废弃）。",
)
t(
    "Applies the GCRA algorithm with a custom token request size.",
    "以指定令牌数执行 GCRA 限流（一次性参数，已废弃）。",
)

# --- RGeo ---
t("Adds geospatial member.", "添加带经纬度的地理空间成员。")
t("Adds geospatial members.", "批量添加地理空间成员。")
t(
    "Adds geospatial member only if it's already exists.",
    "仅当成员已存在时更新坐标（GEOADD XX）。",
)
t(
    "Adds geospatial members only if it's already exists.",
    "批量：仅当成员已存在时更新坐标。",
)
t(
    "Adds geospatial member only if has not been added before.",
    "仅当成员不存在时添加（GEOADD NX）。",
)
t(
    "Adds geospatial members only if has not been added before.",
    "批量：仅当成员不存在时添加。",
)
t(
    "Returns distance between members in <code>GeoUnit</code> units.",
    "返回两成员间距离（指定 {@link GeoUnit} 单位）。",
)
t(
    "Returns 11 characters long Geohash string mapped by defined member.",
    "返回成员对应的 11 位 Geohash 字符串映射。",
)
t(
    "Returns 11 characters Geohash string mapped by defined member.",
    "返回成员对应的 11 位 Geohash 字符串映射。",
)
t(
    "Returns geo-position mapped by defined member.",
    "返回成员经纬度坐标映射。",
)
t(
    "Returns the members of a sorted set, which are within the",
    "按搜索条件返回范围内的成员列表。",
)
t(
    "Returns the distance mapped by member of a sorted set,",
    "按搜索条件返回成员及距离的映射。",
)
t(
    "Returns the position mapped by member of a sorted set,",
    "按搜索条件返回成员及坐标的映射。",
)
t(
    "Finds the members of a sorted set,",
    "按搜索条件查找成员并写入目标键。",
)

# --- RHyperLogLog ---
t("Adds element into this structure.", "向 HyperLogLog 添加元素。")
t(
    "Adds all elements contained in <code>objects</code> collection into this structure",
    "批量向 HyperLogLog 添加元素。",
)
t(
    "Returns approximated number of unique elements added into this structure.",
    "返回已添加唯一元素的近似基数。",
)
t(
    "Returns approximated number of unique elements",
    "返回本实例与其他指定实例合并后的近似唯一元素数。",
)
t("Merges multiple instances into this instance.", "将多个 HyperLogLog 实例合并到本实例。")

# --- RIdGenerator ---
t("Initializes Id generator params.", "初始化 ID 生成器参数。")
t(
    "Returns next unique number but not monotonically increased",
    "返回下一个唯一 ID（非严格单调递增）。",
)


def cn_param_line(line: str) -> str:
    m = re.match(r"(\s*\* @param(?:\s+<[^>]+>)?\s+)(\S+)(\s*)(.*)", line)
    if not m:
        return line
    name = m.group(2)
    rest = m.group(4).strip()
    cn = CN_PARAM.get(name)
    if cn:
        return f"{m.group(1)}{name} {cn}"
    if rest.startswith("- "):
        tail = rest[2:]
        tail_map = {
            "longitude of object": "对象经度",
            "latitude of object": "对象纬度",
            "object itself": "成员对象",
            "objects": "地理空间条目",
            "first object": "第一个成员",
            "second object": "第二个成员",
            "geo unit": "距离单位",
            "search conditions object": "搜索条件",
            "element to add": "待添加元素",
            "elements to add": "待添加元素",
            "name of instances": "其他 HyperLogLog 键名",
            "initial value": "初始值",
            "values range allocation size": "预分配区间大小",
            "type of result": "结果类型",
            "used to locate Redis node in Cluster": "路由键（Cluster 定位节点）",
            "execution mode": "执行模式",
            "function name": "函数名称",
            "return type": "返回值类型",
            "keys available through KEYS param in script": "脚本 KEYS 参数",
            "values available through VALUES param in script": "脚本 ARGV 参数",
        }
        for k, v in tail_map.items():
            if k in tail:
                return f"{m.group(1)}{name} {v}"
        return f"{m.group(1)}{name} {tail}"
    return line


def cn_return_line(line: str) -> str:
    m = re.match(r"(\s*\* @return\s*)(.*)", line)
    if not m:
        return line
    en = m.group(2).strip()
    if en in CN_RETURN:
        mapped = CN_RETURN[en]
        if mapped is None:
            return line
        return f"{m.group(1)}{mapped}"
    if "<code>true</code>" in en:
        return f"{m.group(1)}见方法说明"
    if "number of elements added" in en:
        return f"{m.group(1)}新增元素数量"
    if "set of elements" in en:
        return f"{m.group(1)}元素集合"
    return line


def cn_throws_line(line: str) -> str:
    if "InterruptedException" in line and "interrupted" in line:
        return re.sub(
            r"if the current thread was interrupted",
            "当前线程被中断",
            line,
        )
    return line


def translate_block(jdoc: str) -> str | None:
    if "Copyright" in jdoc:
        return None
    desc = main_desc(jdoc)
    if desc not in TRANSLATIONS:
        return None
    cn = TRANSLATIONS[desc]
    out = []
    first = True
    for line in jdoc.splitlines():
        s = line.strip()
        if first and s.startswith("*") and not s.startswith("* @") and s not in ("*/", "/**", "*"):
            indent = line[: len(line) - len(line.lstrip())]
            out.append(f"{indent}* {cn}")
            first = False
        elif not s.startswith("* @") and s not in ("*/", "/**", "*") and s.startswith("*"):
            body = s[1:].strip()
            if body in CONTINUATION:
                indent = line[: len(line) - len(line.lstrip())]
                out.append(f"{indent}* {CONTINUATION[body]}")
            else:
                out.append(line)
        elif s.startswith("* @param"):
            out.append(cn_param_line(line))
        elif s.startswith("* @return"):
            out.append(cn_return_line(line))
        elif s.startswith("* @throws"):
            out.append(cn_throws_line(line))
        elif s.startswith("* Requires"):
            out.append(
                line.replace("Requires", "需要")
                .replace("and higher", "及以上")
                .replace("or higher", "及以上")
            )
        elif s.startswith("* Supported glob-style patterns:"):
            out.append(line.replace("Supported glob-style patterns:", "支持的 glob 模式示例："))
        else:
            out.append(line)
    return "\n".join(out)


W45A: dict[str, list[tuple[str, str]]] = {}
failures: list[str] = []

for rel in FILES:
    src = (ORIG / rel).read_text(encoding="utf-8")
    reps: list[tuple[str, str]] = []
    used: set[str] = set()

    for old, new in CLASS_OVERRIDES.get(rel, []):
        if old in src:
            reps.append((old, new))
            used.add(old)

    for jdoc in extract_javadocs(src):
        if jdoc in used:
            continue
        new = translate_block(jdoc)
        if new and new != jdoc:
            reps.append((jdoc, new))
            used.add(jdoc)
        elif main_desc(jdoc) and main_desc(jdoc) not in TRANSLATIONS and "Copyright" not in jdoc:
            failures.append(f"{rel}: untranslated: {main_desc(jdoc)[:100]}")

    if not reps:
        failures.append(f"{rel}: no replacements")
    W45A[rel] = reps
    W45A[rel.split("/")[-1]] = reps

if failures:
    print("WARNINGS:")
    for f in failures:
        print(f)

lines = [
    '"""Chinese annotation replacements for Redisson 4.7.0 wave-45a api [0:15]."""',
    "from __future__ import annotations",
    "",
    f'_A = "{_A}"',
    "",
    "W45A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {",
]
for rel in FILES:
    reps = W45A[rel]
    lines.append(f"    {rel!r}: [")
    for old, new in reps:
        lines.append(f"        ({old!r}, {new!r}),")
    lines.append("    ],")
    name = rel.split("/")[-1]
    if name != rel:
        lines.append(f"    {name!r}: [")
        for old, new in reps:
            lines.append(f"        ({old!r}, {new!r}),")
        lines.append("    ],")
lines.append("}")
OUT.write_text("\n".join(lines) + "\n", encoding="utf-8")
print(f"Wrote {OUT} — {sum(len(W45A[r]) for r in FILES)} pairs across {len(FILES)} files")
