#!/usr/bin/env python3
"""Generate wave45b_replacements_redisson.py from originals with exact javadoc matching."""
from __future__ import annotations

import re
from pathlib import Path

ROOT = Path("/workspace")
ORIG = ROOT / "redisson/redisson-4.7.0/original"
OUT = ROOT / "scripts/wave45b_replacements_redisson.py"
_A = "redisson/src/main/java/org/redisson/api/"

FILES = [ln.strip() for ln in Path("/tmp/re45b.txt").read_text(encoding="utf-8").splitlines() if ln.strip()]

TRANSLATIONS: dict[str, str] = {}


def t(en: str, cn: str) -> None:
    TRANSLATIONS[en.strip()] = cn.strip()


CLASS_OVERRIDES: dict[str, list[tuple[str, str]]] = {
    f"{_A}RIdGeneratorAsync.java": [
        (
            "/**\n * Id generator of <code>Long</code> type numbers.\n * Returns unique numbers but not monotonically increased.\n *\n * @author Nikita Koksharov\n *\n */",
            "/**\n * {@code Long} 型分布式 ID 生成器异步 API。\n * <p>返回全局唯一 ID，但不保证严格单调递增。\n *\n * @author Nikita Koksharov\n */",
        ),
    ],
    f"{_A}RIdGeneratorReactive.java": [
        (
            "/**\n * Id generator of <code>Long</code> type numbers.\n * Returns unique numbers but not monotonically increased.\n *\n * @author Nikita Koksharov\n */",
            "/**\n * {@code Long} 型分布式 ID 生成器 Reactor API。\n * <p>返回全局唯一 ID，但不保证严格单调递增。\n *\n * @author Nikita Koksharov\n */",
        ),
    ],
    f"{_A}RIdGeneratorRx.java": [
        (
            "/**\n * Id generator of <code>Long</code> type numbers.\n * Returns unique numbers but not monotonically increased.\n *\n * @author Nikita Koksharov\n */",
            "/**\n * {@code Long} 型分布式 ID 生成器 RxJava API。\n * <p>返回全局唯一 ID，但不保证严格单调递增。\n *\n * @author Nikita Koksharov\n */",
        ),
    ],
    f"{_A}RKeysAsync.java": [
        (
            "/**\n * \n * @author Nikita Koksharov\n *\n */",
            "/**\n * Redis 键空间管理异步 API。\n * <p>各方法返回 {@link RFuture}。\n *\n * @author Nikita Koksharov\n */",
        ),
    ],
    f"{_A}RKeysReactive.java": [
        (
            "/**\n * \n * @author Nikita Koksharov\n *\n */",
            "/**\n * Redis 键空间管理 Reactor API。\n * <p>各方法返回 {@link Mono} 或 {@link reactor.core.publisher.Flux}。\n *\n * @author Nikita Koksharov\n */",
        ),
    ],
    f"{_A}RKeysRx.java": [
        (
            "/**\n * \n * @author Nikita Koksharov\n *\n */",
            "/**\n * Redis 键空间管理 RxJava API。\n * <p>各方法返回 {@link io.reactivex.rxjava3.core.Single} 或 {@link io.reactivex.rxjava3.core.Completable}。\n *\n * @author Nikita Koksharov\n */",
        ),
    ],
    f"{_A}RLeasedMap.java": [
        (
            "/**\n * Lease-based cache operations.\n * <p>\n * Lease token is an opaque string identifier generated on cache miss.\n *\n * @author nhancdt2602\n *\n * @param <K> key type\n * @param <V> value type\n */",
            "/**\n * 基于租约（Lease）的缓存 Map 同步 API。\n * <p>\n * 缓存未命中时生成不透明租约令牌（lease token），持有效令牌方可写入。\n *\n * @author nhancdt2602\n * @param <K> 键类型\n * @param <V> 值类型\n */",
        ),
    ],
    f"{_A}RLeasedMapAsync.java": [
        (
            "/**\n * Asynchronous API for lease-based cache operations.\n * <p>\n * Lease token is an opaque string identifier generated on cache miss.\n *\n * @author nhancdt2602\n *\n * @param <K> key type\n * @param <V> value type\n */",
            "/**\n * 基于租约（Lease）的缓存 Map 异步 API。\n * <p>\n * 缓存未命中时生成不透明租约令牌；各方法返回 {@link RFuture}。\n *\n * @author nhancdt2602\n * @param <K> 键类型\n * @param <V> 值类型\n */",
        ),
    ],
    f"{_A}RLeasedMapReactive.java": [
        (
            "/**\n * Reactive API for lease-based cache operations.\n * <p>\n * Lease token is an opaque string identifier generated on cache miss.\n *\n * @author nhancdt2602\n *\n * @param <K> key type\n * @param <V> value type\n */",
            "/**\n * 基于租约（Lease）的缓存 Map Reactor API。\n * <p>\n * 缓存未命中时生成不透明租约令牌；各方法返回 {@link Mono}。\n *\n * @author nhancdt2602\n * @param <K> 键类型\n * @param <V> 值类型\n */",
        ),
    ],
    f"{_A}RLeasedMapRx.java": [
        (
            "/**\n * RxJava API for lease-based cache operations.\n * <p>\n * Lease token is an opaque string identifier generated on cache miss.\n *\n * @author nhancdt2602\n *\n * @param <K> key type\n * @param <V> value type\n */",
            "/**\n * 基于租约（Lease）的缓存 Map RxJava API。\n * <p>\n * 缓存未命中时生成不透明租约令牌；各方法返回 {@link Single} 或 {@link Completable}。\n *\n * @author nhancdt2602\n * @param <K> 键类型\n * @param <V> 值类型\n */",
        ),
    ],
    f"{_A}RLexSortedSet.java": [
        (
            "/**\n * Sorted set contained values of String type\n * \n * @author Nikita Koksharov\n *\n */",
            "/**\n * 元素为 {@link String} 的字典序有序集合（lex sorted set）同步 API。\n * <p>基于 Redis {@code ZSET} 的 lex 范围命令（{@code ZRANGEBYLEX}、{@code ZLEXCOUNT} 等）。\n *\n * @author Nikita Koksharov\n */",
        ),
    ],
}

# --- RIdGenerator ---
t("Initializes Id generator params.", "初始化 ID 生成器参数（起始值与预分配步长）。")
t(
    "Returns next unique number but not monotonically increased",
    "返回下一个全局唯一 ID（不保证严格单调递增）。",
)

# --- RJsonBuckets ---
t(
    "Returns Redis json object mapped by key with default path",
    "按 Redis 键批量读取 JSON 对象（默认 JSON 路径）。",
)
t(
    "Returns Redis json object mapped by key with specific path",
    "按 Redis 键批量读取指定 JSON 路径下的值。",
)
t(
    "Saves json objects with default path mapped by Redis key.",
    "按 Redis 键批量写入 JSON 对象（默认 JSON 路径）。",
)
t(
    "Saves json objects with specific path mapped by Redis key.",
    "按 Redis 键批量写入指定 JSON 路径下的值。",
)

# --- RKeys family ---
t("Move object to another database", "将对象移动到指定 Redis 数据库。")
t(
    "Transfer object from source Redis instance to destination Redis instance",
    "将对象从源 Redis 实例迁移到目标实例。",
)
t(
    "Copy object from source Redis instance to destination Redis instance",
    "将对象从源 Redis 实例复制到目标实例。",
)
t("Use {@link #expire(Duration, String...)} instead.", "已废弃，请改用 {@link #expire(Duration, String...)}。")
t("Use {@link #expireAsync(Duration, String...)} instead.", "已废弃，请改用 {@link #expireAsync(Duration, String...)}。")
t("Use {@link #expireAt(Instant, String...)} instead.", "已废弃，请改用 {@link #expireAt(Instant, String...)}。")
t("Use {@link #expireAtAsync(Instant, String...)} instead.", "已废弃，请改用 {@link #expireAtAsync(Instant, String...)}。")
t(
    "Set a timeout for multiple objects. After the timeout has expired,",
    "为多个对象设置相对过期时长；到期后 Redis 键将自动删除。",
)
t("Clear an expire timeout or expire date for object.", "清除对象的过期时间或绝对过期时刻。")
t(
    "Rename object with <code>oldName</code> to <code>newName</code>",
    "仅当新键不存在时将 {@code oldName} 重命名为 {@code newName}。",
)
t("Rename current object key to <code>newName</code>", "将当前对象键重命名为 {@code newName}。")
t(
    "Remaining time to live of Redisson object that has a timeout",
    "返回带过期时间的 Redisson 对象剩余存活时间。",
)
t("Update the last access time of an object.", "更新对象的最后访问时间。")
t("Checks if provided keys exist", "检查给定键是否存在。")
t(
    "Get all keys using iterable. Keys traversing with SCAN operation.",
    "通过 SCAN 迭代获取所有键（异步 Iterable）。",
)
t(
    "Load keys in incrementally iterate mode. Keys traversed with SCAN operation.",
    "以增量迭代方式加载键（SCAN 遍历）。",
)
t("Use {@link #getKeys(KeysScanOptions)} instead.", "已废弃，请改用 {@link #getKeys(KeysScanOptions)}。")
t("Get Redis object type by key", "按键获取 Redis 对象类型。")
t("Get hash slot identifier for key.", "获取键在 Cluster 中的 hash slot（仅集群模式）。")
t("Get hash slot identifier for key in async mode.", "异步获取键在 Cluster 中的 hash slot（仅集群模式）。")
t("Get random key", "随机返回一个键。")
t("Get random key in async mode", "异步随机返回一个键。")
t("Delete multiple objects by a key pattern.", "按 glob 模式批量删除对象。")
t("Unlink multiple objects by a key pattern.", "按 glob 模式批量异步解除链接（UNLINK）。")
t("Delete multiple objects", "批量删除 Redisson 对象。")
t("Delete multiple objects by name", "按名称批量删除对象。")
t("Delete multiple objects by name.", "按名称批量删除对象。")
t(
    "Returns the number of keys in the currently-selected database",
    "返回当前选中数据库的键数量。",
)
t(
    "Returns the number of keys in the currently-selected database in async mode",
    "异步返回当前选中数据库的键数量。",
)
t("Swap two databases.", "交换两个 Redis 数据库的内容。")
t("Delete all keys of currently selected database", "清空当前选中数据库的所有键。")
t("Delete all keys of all existing databases", "清空所有数据库的所有键。")
t("Delete all the keys of the currently selected database", "清空当前选中数据库的所有键。")
t("Delete all the keys of all the existing databases", "清空所有数据库的所有键。")
t(
    "Adds global object event listener",
    "注册全局 Redisson 对象事件监听器。",
)
t("Removes global object event listener", "移除全局对象事件监听器。")

# --- RLeasedMap ---
t(
    "Returns the cached value mapped by defined {@code key} or {@code null} if value is absent.",
    "返回 {@code key} 对应的缓存值；未命中时返回 {@code null} 并尝试获取租约。",
)
t(
    "Invalidates the entry mapped by {@code key} and deletes current lease token (if any).",
    "使 {@code key} 对应条目失效并删除当前租约令牌（如有）。",
)
t(
    "Stores the specified {@code value} mapped by {@code key} only if the given {@code leaseToken} is still valid.",
    "仅当 {@code leaseToken} 仍有效时，将 {@code value} 写入 {@code key}。",
)

# --- RLexSortedSet ---
t(
    "Removes and returns the head element or {@code null} if this sorted set is empty.",
    "移除并返回字典序最小元素；集合为空时返回 {@code null}。",
)
t(
    "Removes and returns the tail element or {@code null} if this sorted set is empty.",
    "移除并返回字典序最大元素；集合为空时返回 {@code null}。",
)
t(
    "Returns rank of value, with the scores ordered from high to low.",
    "按分数从高到低返回元素的逆序排名。",
)
t(
    "Removes tail values range starting with <code>fromElement</code>.",
    "移除从 {@code fromElement} 开始的尾部 lex 区间元素。",
)
t(
    "Removes head values range ending with <code>toElement</code>.",
    "移除以 {@code toElement} 结束的头部 lex 区间元素。",
)
t(
    "Removes values range starting with <code>fromElement</code> and ending with <code>toElement</code>.",
    "移除 {@code fromElement} 到 {@code toElement} 之间的 lex 区间元素。",
)
t(
    "Returns the number of tail values starting with <code>fromElement</code>.",
    "统计从 {@code fromElement} 开始的尾部 lex 区间元素数量。",
)
t(
    "Returns the number of head values ending with <code>toElement</code>.",
    "统计以 {@code toElement} 结束的头部 lex 区间元素数量。",
)
t(
    "Returns tail values range starting with <code>fromElement</code>.",
    "返回从 {@code fromElement} 开始的尾部 lex 区间元素。",
)
t(
    "Returns head values range ending with <code>toElement</code>.",
    "返回以 {@code toElement} 结束的头部 lex 区间元素。",
)
t(
    "Returns values range starting with <code>fromElement</code> and ending with <code>toElement</code>.",
    "返回 {@code fromElement} 到 {@code toElement} 之间的 lex 区间元素。",
)
t(
    "Returns tail values range in reverse order starting with <code>fromElement</code>.",
    "逆序返回从 {@code fromElement} 开始的尾部 lex 区间元素。",
)
t(
    "Returns head values range in reverse order ending with <code>toElement</code>.",
    "逆序返回以 {@code toElement} 结束的头部 lex 区间元素。",
)
t(
    "Returns values range in reverse order starting with <code>fromElement</code> and ending with <code>toElement</code>.",
    "逆序返回 {@code fromElement} 到 {@code toElement} 之间的 lex 区间元素。",
)
t(
    "Returns the number of elements between <code>fromElement</code> and <code>toElement</code>.",
    "统计 {@code fromElement} 到 {@code toElement} 之间的 lex 区间元素数量。",
)
t("Returns rank of the element", "返回元素在字典序中的排名。")
t(
    "Returns values by rank range. Indexes are zero based.",
    "按排名区间返回元素（下标从 0 起）。",
)
t("Returns random element from this sorted set", "随机返回集合中的一个元素。")
t(
    "Returns random elements from this sorted set limited by <code>count</code>",
    "随机返回至多 {@code count} 个元素。",
)
t("Adds object event listener", "注册 lex 有序集对象事件监听器。")

CN_PARAM = {
    "value": "起始值",
    "allocationSize": "预分配步长",
    "keys": "Redis 键名",
    "codec": "JSON 编解码器",
    "path": "JSON 路径",
    "buckets": "JSON 桶映射",
    "name": "对象名称",
    "database": "目标数据库编号",
    "host": "目标主机",
    "port": "目标端口",
    "timeout": "通信最大空闲时间（毫秒）",
    "migrateArgs": "迁移参数",
    "timeToLive": "存活时间",
    "timeUnit": "时间单位",
    "duration": "过期时长",
    "names": "对象名称",
    "instant": "过期时刻",
    "timestamp": "过期时间戳",
    "oldName": "原对象名称",
    "newName": "新对象名称",
    "currentName": "当前对象名称",
    "pattern": "匹配模式",
    "objects": "Redisson 对象",
    "limit": "键数量上限",
    "options": "SCAN 选项",
    "key": "键名",
    "listener": "事件监听器",
    "listenerId": "监听器 ID",
    "db1": "第一个数据库编号",
    "db2": "第二个数据库编号",
    "fromElement": "起始元素",
    "fromInclusive": "起始边界是否包含",
    "toElement": "结束元素",
    "toInclusive": "结束边界是否包含",
    "offset": "结果集合偏移量",
    "count": "返回数量",
    "startIndex": "起始排名下标",
    "endIndex": "结束排名下标",
    "o": "待查元素",
    "leaseTimeToLive": "租约存活时间",
    "leaseToken": "租约令牌",
    "ttl": "条目 TTL",
    "maxIdleTime": "最大空闲时间",
}


def extract_javadocs(text: str) -> list[str]:
    return re.findall(r"/\*\*.*?\*/", text, re.DOTALL)


def main_desc(jdoc: str) -> str:
    for line in jdoc.splitlines():
        s = line.strip()
        if s.startswith("*") and not s.startswith("* @") and s not in ("*/", "/**", "*"):
            return s[1:].strip()
    return ""


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
        return f"{m.group(1)}{name} {rest[2:]}"
    if rest == "of object":
        return f"{m.group(1)}{name} 对象名称"
    if rest == "of key":
        return f"{m.group(1)}{name} 键名"
    if rest == "of keys":
        return f"{m.group(1)}{name} 键名"
    if rest == "of Redisson":
        return f"{m.group(1)}{name} Redisson 对象"
    if rest == "map key":
        return f"{m.group(1)}{name} Map 键"
    if rest == "map value":
        return f"{m.group(1)}{name} Map 值"
    if rest == "element to rank":
        return f"{m.group(1)}{name} 待查元素"
    if rest == "values amount to return":
        return f"{m.group(1)}{name} 返回数量"
    if rest == "object event listener":
        return f"{m.group(1)}{name} 事件监听器"
    if rest == "listener id":
        return f"{m.group(1)}{name} 监听器 ID"
    if rest == "match pattern":
        return f"{m.group(1)}{name} 匹配模式"
    if rest == "object names":
        return f"{m.group(1)}{name} 对象名称"
    if rest == "scan options":
        return f"{m.group(1)}{name} SCAN 选项"
    if rest == "name of key":
        return f"{m.group(1)}{name} 键名"
    return line


def cn_return_line(line: str) -> str:
    m = re.match(r"(\s*\* @return\s*)(.*)", line)
    if not m:
        return line
    en = m.group(2).strip()
    mapping = {
        "number": "下一个 ID",
        "Map with name as key and bucket as value": "键名到 JSON 值的映射",
        "random key": "随机键名",
        "slot": "hash slot 编号",
        "type of key": "键类型",
        "number of removed keys": "已删除键数量",
        "number of keys": "键数量",
        "random element": "随机元素",
        "random elements": "随机元素集合",
        "collection of elements": "元素集合",
        "number of elements": "元素数量",
        "rank or <code>null</code> if value does not exist": "排名；不存在时为 null",
        "rank or <code>null</code> if element does not exist": "排名；不存在时为 null",
        "listener id": "监听器 ID",
        "cached value or lease on miss": "缓存值或未命中时的租约信息",
        "time in milliseconds": "剩余毫秒数",
        "count of objects were touched": "已 touch 的对象数量",
        "amount of existing keys": "存在的键数量",
        "Asynchronous Iterable object": "异步 Iterable",
        "Iterable object": "Iterable",
        "Flux object": "Flux",
        "void": "无返回值",
    }
    if en in mapping:
        return f"{m.group(1)}{mapping[en]}"
    if "<code>true</code>" in en:
        return f"{m.group(1)}见方法说明"
    if "number of keys for which the timeout was set successfully" in en:
        return f"{m.group(1)}成功设置过期的键数量"
    return line


def translate_block(jdoc: str) -> str | None:
    desc = main_desc(jdoc)
    if desc.startswith("Copyright"):
        return None
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
        elif s.startswith("* @param"):
            out.append(cn_param_line(line))
        elif s.startswith("* @return"):
            out.append(cn_return_line(line))
        elif s.startswith("* Requires"):
            out.append(line.replace("Requires", "需要").replace("and higher", "及以上"))
        elif "Supported glob-style patterns:" in s:
            out.append(line.replace("Supported glob-style patterns:", "支持的 glob 模式示例："))
        elif s.startswith("* @deprecated"):
            out.append(line.replace("@deprecated", "@deprecated 已废弃，"))
        elif s.startswith("* Method executes"):
            out.append(line.replace("Method executes in <b>NON atomic way</b> in cluster mode due to lua script limitations.", "Cluster 模式下因 Lua 限制以<b>非原子</b>方式执行。"))
        elif s.startswith("* Actual removal"):
            out.append(line.replace("Actual removal will happen later asynchronously.", "实际删除将异步进行。"))
        elif s.startswith("* in background"):
            out.append(line.replace("in background without blocking server.", "在后台执行，不阻塞服务器。"))
        elif s.startswith("* Each SCAN"):
            out.append(line.replace("Each SCAN operation loads up to <code>10</code> keys per request.", "每次 SCAN 请求最多加载 10 个键。"))
        elif s.startswith("* Available for cluster"):
            out.append(line.replace("Available for cluster nodes only", "仅适用于 Cluster 节点"))
        elif s.startswith("* only if new key"):
            out.append(line.replace("only if new key is not exists", "仅当新键不存在时"))
        elif s.startswith("* If value is absent"):
            out.append(line.replace("If value is absent then tries to acquire a lease and returns it together with {@code null} value.", "未命中时尝试获取租约并与 {@code null} 一并返回。"))
        elif s.startswith("* Lease is automatically"):
            out.append(line.replace("Lease is automatically released after {@code leaseTimeToLive} timeout.", "租约在 {@code leaseTimeToLive} 超时后自动释放。"))
        elif s.startswith("* On miss,"):
            out.append(line.replace("On miss, {@link LeaseGetResult#getLeaseToken()} carries the lease identifier.", "未命中时 {@link LeaseGetResult#getLeaseToken()} 携带租约标识。"))
        elif s.startswith("* Returned collection limited"):
            out.append(line.replace("Returned collection limited by <code>count</code> and starts with <code>offset</code>.", "结果集合受 {@code count} 限制，从 {@code offset} 起返回。"))
        elif s.startswith("* <code>-1</code> means"):
            out.append(line.replace("<code>-1</code> means the highest score, <code>-2</code> means the second highest score.", "{@code -1} 表示最高分，{@code -2} 表示次高分。"))
        elif s.startswith("* The lease key"):
            pass  # keep English detail
        else:
            out.append(line)
    return "\n".join(out)


W45B: dict[str, list[tuple[str, str]]] = {}
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
        elif main_desc(jdoc) and main_desc(jdoc) not in TRANSLATIONS and not main_desc(jdoc).startswith("Copyright"):
            failures.append(f"{rel}: untranslated: {main_desc(jdoc)[:80]}")

    if not reps:
        failures.append(f"{rel}: no replacements")
    W45B[rel] = reps
    W45B[rel.split("/")[-1]] = reps

if failures:
    print("WARNINGS:")
    for f in failures:
        print(f)
    print(f"... total warnings: {len(failures)}")
    raise SystemExit(1)

lines = [
    '"""Chinese annotation replacements for Redisson 4.7.0 wave-45b api [15:30]."""',
    "from __future__ import annotations",
    "",
    f'_A = "{_A}"',
    "",
    "W45B_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {",
]
for rel in FILES:
    reps = W45B[rel]
    lines.append(f"    {rel!r}: [")
    for old, new in reps:
        lines.append(f"        ({old!r}, {new!r}),")
    lines.append("    ],")
    name = rel.split("/")[-1]
    lines.append(f"    {name!r}: [")
    for old, new in reps:
        lines.append(f"        ({old!r}, {new!r}),")
    lines.append("    ],")
lines.append("}")
OUT.write_text("\n".join(lines) + "\n", encoding="utf-8")
print(f"Wrote {OUT} — {sum(len(W45B[r]) for r in FILES)} replacement pairs across {len(FILES)} files")
