#!/usr/bin/env python3
"""Generate wave59b_replacements_redisson.py for set/stream/timeseries/clients [15:30]."""
from __future__ import annotations

import importlib.util
import re
from pathlib import Path

ROOT = Path("/workspace")
ORIG = ROOT / "redisson/redisson-4.7.0/original"
OUT = ROOT / "scripts/wave59b_replacements_redisson.py"
SCRIPTS = ROOT / "scripts"
_A = "redisson/src/main/java/org/redisson/api/"
FILES = [
    ln.strip()
    for ln in Path("/tmp/re59b.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]

TRANSLATIONS: dict[str, str] = {}


def t(en: str, cn: str) -> None:
    TRANSLATIONS[en.strip()] = cn.strip()


def load_external_translations() -> None:
    for name in ("_gen_wave43b_replacements", "_gen_wave49b_replacements", "_gen_wave58b_replacements"):
        path = SCRIPTS / f"{name}.py"
        if not path.exists():
            continue
        spec = importlib.util.spec_from_file_location(name, path)
        mod = importlib.util.module_from_spec(spec)
        assert spec.loader is not None
        spec.loader.exec_module(mod)
        if hasattr(mod, "TRANSLATIONS"):
            TRANSLATIONS.update(mod.TRANSLATIONS)


CLASS_OVERRIDES: dict[str, list[tuple[str, str]]] = {
    f"{_A}RScoredSortedSetRx.java": [
        (
            "/**\n * RxJava2 interface for scored sorted set data structure.\n * \n"
            " * @author Nikita Koksharov\n *\n * @param <V> value type\n */",
            "/**\n * 有序集合 {@link RScoredSortedSet} 的 RxJava3 API。\n"
            " * <p>封装 ZADD/ZREM、ZRANGE、ZUNION/ZINTER、阻塞 poll 及 MapReduce 等操作。\n"
            " *\n * @author Nikita Koksharov\n * @param <V> 成员类型\n */",
        ),
    ],
    f"{_A}RSet.java": [
        (
            "/**\n * Redis based implementation of {@link java.util.Set}\n *\n"
            " * @author Nikita Koksharov\n *\n * @param <V> type of value\n */",
            "/**\n * 基于 Redis 的 {@link java.util.Set} 同步 API {@link RSet}。\n"
            " * <p>封装 SADD/SREM、SMEMBERS、SINTER/SUNION/SDIFF、SSCAN 及 per-member 锁等操作。\n"
            " *\n * @author Nikita Koksharov\n * @param <V> 元素类型\n */",
        ),
    ],
    f"{_A}RStream.java": [
        (
            "/**\n * Interface for Redis Stream object.\n * <p>\n"
            " * Requires <b>Redis 5.0.0 and higher.</b>\n * \n"
            " * @author Nikita Koksharov\n *\n * @param <K> key type\n * @param <V> value type\n */",
            "/**\n * Redis Stream {@link RStream} 同步 API。\n"
            " * <p>需要 <b>Redis 5.0.0 及以上</b>；支持 XADD/XREAD、消费者组、ACK 与 Pending 管理。\n"
            " *\n * @author Nikita Koksharov\n * @param <K> 流条目字段键类型\n * @param <V> 流条目字段值类型\n */",
        ),
    ],
    f"{_A}RStreamAsync.java": [
        (
            "/**\n * Async interface for Redis Stream object.\n * <p>\n"
            " * Requires <b>Redis 5.0.0 and higher.</b>\n * \n"
            " * @author Nikita Koksharov\n *\n * @param <K> key type\n * @param <V> value type\n */",
            "/**\n * {@link RStream} 异步 API；各方法返回 {@link RFuture}。\n"
            " * <p>需要 <b>Redis 5.0.0 及以上</b>。\n"
            " *\n * @author Nikita Koksharov\n * @param <K> 流条目字段键类型\n * @param <V> 流条目字段值类型\n */",
        ),
    ],
    f"{_A}RStreamReactive.java": [
        (
            "/**\n * Reactive interface for Redis Stream object.\n * <p>\n"
            " * Requires <b>Redis 5.0.0 and higher.</b>\n * \n"
            " * @author Nikita Koksharov\n *\n * @param <K> key type\n * @param <V> value type\n */",
            "/**\n * {@link RStream} Reactor 响应式 API。\n"
            " * <p>需要 <b>Redis 5.0.0 及以上</b>；各方法返回 {@link Mono} 或 {@link Flux}。\n"
            " *\n * @author Nikita Koksharov\n * @param <K> 流条目字段键类型\n * @param <V> 流条目字段值类型\n */",
        ),
    ],
    f"{_A}RStreamRx.java": [
        (
            "/**\n * RxJava2 interface for Redis Stream object.\n * <p>\n"
            " * Requires <b>Redis 5.0.0 and higher.</b>\n * \n"
            " * @author Nikita Koksharov\n *\n * @param <K> key type\n * @param <V> value type\n */",
            "/**\n * {@link RStream} RxJava3 API。\n"
            " * <p>需要 <b>Redis 5.0.0 及以上</b>；各方法返回 {@link Single}、{@link Maybe} 或 {@link Flowable}。\n"
            " *\n * @author Nikita Koksharov\n * @param <K> 流条目字段键类型\n * @param <V> 流条目字段值类型\n */",
        ),
    ],
    f"{_A}RTimeSeries.java": [
        (
            "/**\n * Redis based time-series collection.\n *\n"
            " * @author Nikita Koksharov\n *\n * @param <V> value type\n * @param <L> label type\n */",
            "/**\n * 时间序列 {@link RTimeSeries} 同步 API。\n"
            " * <p>基于 Redis ZSET 按时间戳存储条目，支持范围查询、TTL 与惰性过期淘汰。\n"
            " *\n * @author Nikita Koksharov\n * @param <V> 值类型\n * @param <L> 标签/元数据类型\n */",
        ),
    ],
    f"{_A}RTimeSeriesAsync.java": [
        (
            "/**\n * Async interface for Redis based time-series collection.\n *\n"
            " * @author Nikita Koksharov\n *\n * @param <V> value type\n * @param <L> label type\n */",
            "/**\n * {@link RTimeSeries} 异步 API；各方法返回 {@link RFuture}。\n"
            " *\n * @author Nikita Koksharov\n * @param <V> 值类型\n * @param <L> 标签/元数据类型\n */",
        ),
    ],
    f"{_A}RTimeSeriesReactive.java": [
        (
            "/**\n * Reactive interface for Redis based time-series collection.\n *\n"
            " * @author Nikita Koksharov\n *\n * @param <V> value type\n * @param <L> label type\n */",
            "/**\n * {@link RTimeSeries} Reactor 响应式 API。\n"
            " *\n * @author Nikita Koksharov\n * @param <V> 值类型\n * @param <L> 标签/元数据类型\n */",
        ),
    ],
    f"{_A}RTimeSeriesRx.java": [
        (
            "/**\n * RxJava2 interface for Redis based time-series collection.\n *\n"
            " * @author Nikita Koksharov\n *\n * @param <V> value type\n * @param <L> label type\n */",
            "/**\n * {@link RTimeSeries} RxJava3 API。\n"
            " *\n * @author Nikita Koksharov\n * @param <V> 值类型\n * @param <L> 标签/元数据类型\n */",
        ),
    ],
    f"{_A}RedissonClient.java": [
        (
            "/**\n * Main Redisson interface for access\n"
            " * to all redisson objects with sync/async interface.\n * \n"
            " * @see RedissonReactiveClient\n * @see RedissonRxClient\n *\n"
            " * @author Nikita Koksharov\n *\n */",
            "/**\n * Redisson 主客户端 {@link RedissonClient}。\n"
            " * <p>提供全部分布式对象的同步/异步 factory 方法；\n"
            " * 参见 {@link RedissonReactiveClient} 与 {@link RedissonRxClient}。\n"
            " *\n * @author Nikita Koksharov\n */",
        ),
    ],
    f"{_A}RedissonReactiveClient.java": [
        (
            "/**\n * Main Redisson interface for access\n"
            " * to all redisson objects with Reactive interface.\n *\n"
            " * @see RedissonRxClient\n * @see RedissonClient\n *\n"
            " * @author Nikita Koksharov\n *\n */",
            "/**\n * Reactor 风格 Redisson 客户端 {@link RedissonReactiveClient}。\n"
            " * <p>提供全部响应式分布式对象 factory 方法；\n"
            " * 参见 {@link RedissonClient} 与 {@link RedissonRxClient}。\n"
            " *\n * @author Nikita Koksharov\n */",
        ),
    ],
    f"{_A}RedissonRxClient.java": [
        (
            "/**\n * Main Redisson interface for access\n"
            " * to all redisson objects with RxJava2 interface.\n *\n"
            " * @see RedissonReactiveClient\n * @see RedissonClient\n *\n *\n"
            " * @author Nikita Koksharov\n *\n */",
            "/**\n * RxJava3 风格 Redisson 客户端 {@link RedissonRxClient}。\n"
            " * <p>提供全部 Rx 分布式对象 factory 方法；\n"
            " * 参见 {@link RedissonClient} 与 {@link RedissonReactiveClient}。\n"
            " *\n * @author Nikita Koksharov\n */",
        ),
    ],
    "redisson/src/main/java/org/redisson/api/bitset/BitOffset.java": [
        (
            "/**\n * BITFIELD offset wrapper for bit or index-based offsets.\n *\n"
            " * @author Su Ko\n *\n */",
            "/**\n * BITFIELD 位偏移包装类，支持按位或按索引（{@code #} 前缀）偏移。\n"
            " *\n * @author Su Ko\n */",
        ),
        (
            "    public long getLongValue() {",
            "    /** 返回原始 long 偏移值。 */\n    public long getLongValue() {",
        ),
        (
            "    public String getValue() {",
            "    /** 返回 Redis 命令使用的偏移字符串（索引型带 {@code #} 前缀）。 */\n    public String getValue() {",
        ),
        (
            "    public boolean isIndexed() {",
            "    /** 是否为索引型偏移（{@code #} 前缀）。 */\n    public boolean isIndexed() {",
        ),
    ],
    "redisson/src/main/java/org/redisson/api/bitvector/MatchArgs.java": [
        (
            "/**\n * Argument builder for the\n"
            " * {@link RBitVectorStore#matchAll(MatchArgs) matchAll},\n"
            " * {@link RBitVectorStore#matchAny(MatchArgs) matchAny}, and\n"
            " * {@link RBitVectorStore#matchNone(MatchArgs) matchNone} queries.\n"
            " * <p>\n"
            " * Carries the required bitmask plus optional iteration-tuning parameters that\n"
            " * control server-side batching during result iteration.\n"
            " * <p>\n"
            " * Construct with {@link #mask(long)} and chain further configuration:\n"
            " * <pre>{@code\n"
            " *   MatchArgs args = MatchArgs.mask(0b101001L)\n"
            " *                             .chunkSize(2048)\n"
            " *                             .chunkFetchTTL(Duration.ofMinutes(2));\n"
            " * }</pre>\n"
            " *\n * @see MatchExactArgs\n *\n * @author Nikita Koksharov\n *\n */",
            "/**\n * {@link RBitVectorStore#matchAll}、{@link RBitVectorStore#matchAny}、\n"
            " * {@link RBitVectorStore#matchNone} 查询的参数构建器。\n"
            " * <p>携带必需位掩码及可选迭代调优参数（服务端分批拉取）。\n"
            " * <p>通过 {@link #mask(long)} 创建并链式配置 chunkSize/chunkFetchTTL 等。\n"
            " *\n * @see MatchExactArgs\n * @author Nikita Koksharov\n */",
        ),
    ],
}

PARAM_RETURN: dict[str, str] = {
    "@param <V> value type": "@param <V> 值类型",
    "@param <V> type of value": "@param <V> 元素类型",
    "@param <K> key type": "@param <K> 键类型",
    "@param <L> label type": "@param <L> 标签类型",
    "@param name name of instance": "@param name 实例名称",
    "@param name name of object": "@param name 对象名称",
    "@param codec codec for values": "@param codec 值编解码器",
    "@param options instance options": "@param options 实例选项",
    "@param args method arguments object": "@param args 方法参数对象",
    "@param groupName - name of group": "@param groupName 消费者组名称",
    "@param consumerName - name of consumer": "@param consumerName 消费者名称",
    "@param id - Stream Message ID": "@param id Stream 消息 ID",
    "@param timestamp object timestamp": "@param timestamp 时间戳",
    "@param object object itself": "@param object 对象本身",
    "@param label object label": "@param label 对象标签",
    "@param c - collection of elements to add": "@param c 待添加元素集合",
    "@param value - set value": "@param value Set 成员值",
    "@param queueNames name of queues": "@param queueNames 有序集合/队列名称",
    "@param timeout how long to wait before giving up, in units of": "@param timeout 等待超时",
    "@param unit a {@code TimeUnit} determining how to interpret the": "@param unit 时间单位",
    "@param duration how long to wait before giving up": "@param duration 等待时长",
    "@param count elements amount": "@param count 元素数量",
    "@param count entries amount": "@param count 条目数量",
    "@param listener - object event listener": "@param listener 对象事件监听器",
    "@param listener object event listener": "@param listener 对象事件监听器",
    "@return listener id": "@return 监听器 ID",
    "@return void": "@return 无返回值",
    "@param value the bitmask": "@param value 位掩码",
    "@param value the batch size; must be positive": "@param value 批次大小（须为正数）",
    "@param value the TTL applied to server-side iteration state": "@param value 服务端迭代状态 TTL",
    "@param offset zero-based bit offset": "@param offset 从 0 开始的位偏移",
    "@param index index of the integer element": "@param index 整型元素索引",
    "@return offset wrapper": "@return 偏移包装对象",
    "@param timeToLive - time to live interval": "@param timeToLive 存活时间",
    "@param timeUnit - unit of time to live interval": "@param timeUnit 时间单位",
    "@param score - object score": "@param score 成员分数",
    "@param member - object to add": "@param member 待添加成员",
    "@param startIndex - start index": "@param startIndex 起始索引",
    "@param endIndex - end index": "@param endIndex 结束索引",
    "@param startScore - start score": "@param startScore 起始分数",
    "@param endScore - end score": "@param endScore 结束分数",
    "@param startTimestamp - start timestamp": "@param startTimestamp 起始时间戳",
    "@param endTimestamp - end timestamp": "@param endTimestamp 结束时间戳",
}

PHRASE_CN: dict[str, str] = {
    "Requires <b>Redis 5.0.0 and higher.</b>": "需要 <b>Redis 5.0.0 及以上</b>。",
    "Requires <b>Redis 6.2.0 and higher.</b>": "需要 <b>Redis 6.2.0 及以上</b>。",
    "Requires <b>Redis 7.0.0 and higher.</b>": "需要 <b>Redis 7.0.0 及以上</b>。",
    "Requires <b>Redis 8.8 or higher.</b>": "需要 <b>Redis 8.8 及以上</b>。",
    "Requires <b>Redis 8.8.0 or higher.</b>": "需要 <b>Redis 8.8.0 及以上</b>。",
    "Use {@link #": "请改用 {@link #",
    " instead.": "。",
    " instead": "。",
    "Adds object event listener": "注册对象事件监听器。",
    "Stores result into this object.": "结果写回本对象。",
    "Returns set instance by name.": "按名称获取 {@link RSet} 实例。",
    "Returns time-series instance by <code>name</code>": "按名称获取 {@link RTimeSeries} 实例。",
    "Returns stream instance by <code>name</code>": "按名称获取 {@link RStream} 实例。",
    "Returns Redis Sorted Set instance by name.": "按名称获取 {@link RScoredSortedSet} 实例（按 score 排序）。",
    "Creates consumer group.": "创建消费者组。",
    "Removes group by name.": "按名称移除消费者组。",
    "Creates consumer of the group by name.": "在指定组下创建消费者。",
    "Removes consumer of the group by name.": "移除指定组下的消费者。",
    "Updates next message id delivered to consumers.": "更新投递给消费者的下一条消息 ID。",
    "Appends a new entry/entries and returns generated Stream Message ID": "追加流条目并返回生成的 Stream 消息 ID。",
    "Appends a new entry/entries by specified Stream Message ID": "按指定 Stream 消息 ID 追加流条目。",
    "Acknowledges and conditionally deletes one or multiple entries (messages)": "确认并条件删除一条或多条流消息。",
    "Async interface for Redis based time-series collection.": "时间序列异步 API。",
    "Allows to get configuration provided": "返回创建客户端时使用的 {@link Config}。",
    "Shutdown Redisson instance and all associated threads": "关闭 Redisson 实例及全部关联线程。",
    "Returns reactive api instance": "返回 {@link RedissonReactiveClient} 响应式 API 实例。",
    "Returns RxJava api instance": "返回 {@link RedissonRxClient} RxJava API 实例。",
}


def main_desc(jdoc: str) -> str:
    for line in jdoc.splitlines():
        s = line.strip()
        if s.startswith("*") and not s.startswith("* @") and s not in ("*/", "/**", "*"):
            return s[1:].strip()
    return ""


def heuristic_cn(desc: str) -> str:
    if desc in TRANSLATIONS:
        return TRANSLATIONS[desc]
    s = desc
    for en, cn in sorted(PHRASE_CN.items(), key=lambda x: -len(x[0])):
        if en in s:
            s = s.replace(en, cn)
    rules: list[tuple[str, str]] = [
        (r"^Returns (.+)$", r"返回\1。"),
        (r"^Returns (.+) by (.+)$", r"按\2返回\1。"),
        (r"^Adds (.+)$", r"添加\1。"),
        (r"^Removes (.+)$", r"移除\1。"),
        (r"^Removes and returns (.+)$", r"移除并返回\1。"),
        (r"^Creates (.+)$", r"创建\1。"),
        (r"^Updates (.+)$", r"更新\1。"),
        (r"^Sets (.+)$", r"设置\1。"),
        (r"^Checks (.+)$", r"检查\1。"),
        (r"^Check (.+)$", r"检查\1。"),
        (r"^Counts (.+)$", r"统计\1。"),
        (r"^Reads (.+)$", r"读取\1。"),
        (r"^Read (.+)$", r"读取\1。"),
        (r"^Executes (.+)$", r"执行\1。"),
        (r"^Allows (.+)$", r"允许\1。"),
        (r"^Shutdown (.+)$", r"关闭\1。"),
        (r"^Use (.+) instead\.?$", r"请改用\1。"),
        (r"^Async (.+)$", r"\1 的异步 API。"),
        (r"^Reactive (.+)$", r"\1 的 Reactor 响应式 API。"),
        (r"^RxJava2 interface for (.+)$", r"\1 的 RxJava3 API。"),
    ]
    for pat, repl in rules:
        m = re.match(pat, s, re.I)
        if m:
            return re.sub(pat, repl, s, flags=re.I)
    if "time-series" in s.lower():
        return s.replace("time-series", "时间序列").replace("collection", "集合") + "。"
    if "sorted set" in s.lower():
        return "有序集合（ZSET）相关操作：" + s + "。"
    if "stream" in s.lower():
        return "Redis Stream 相关操作：" + s + "。"
    if "set" in s.lower() and "offset" not in s.lower():
        return "Set 相关操作：" + s + "。"
    return s + "（Redisson API）。"


def translate_javadoc(block: str) -> str | None:
    if "Copyright" in block and "Licensed under the Apache License" in block:
        return None
    desc = main_desc(block)
    cn_desc = TRANSLATIONS.get(desc) or heuristic_cn(desc)
    if not cn_desc or not re.search(r"[\u4e00-\u9fff]", cn_desc):
        return None
    out: list[str] = []
    replaced_first = False
    for line in block.splitlines():
        s = line.strip()
        if (
            not replaced_first
            and s.startswith("*")
            and not s.startswith("* @")
            and s not in ("*/", "/**", "*")
        ):
            indent = line[: len(line) - len(line.lstrip())]
            out.append(f"{indent}* {cn_desc}")
            replaced_first = True
        else:
            new_line = line
            for en, cn in sorted(PHRASE_CN.items(), key=lambda x: -len(x[0])):
                if en in new_line:
                    new_line = new_line.replace(en, cn)
            for en, cn in PARAM_RETURN.items():
                if en in new_line:
                    new_line = new_line.replace(en, cn)
            out.append(new_line)
    result = "\n".join(out)
    if result == block or not re.search(r"[\u4e00-\u9fff]", result):
        return None
    return result


def collect_replacements(rel: str) -> list[tuple[str, str]]:
    text = (ORIG / rel).read_text(encoding="utf-8")
    reps: list[tuple[str, str]] = []
    seen: set[str] = set()
    override_olds = {o for o, _ in CLASS_OVERRIDES.get(rel, [])}

    for old, new in CLASS_OVERRIDES.get(rel, []):
        if old in text and old not in seen:
            reps.append((old, new))
            seen.add(old)

    for m in re.finditer(r"/\*\*.*?\*/", text, re.DOTALL):
        old = m.group(0)
        if old in seen or old in override_olds:
            continue
        if "Copyright" in old and "Licensed under the Apache License" in old:
            continue
        new = translate_javadoc(old)
        if new and new != old:
            reps.append((old, new))
            seen.add(old)
    return reps


def emit() -> None:
    load_external_translations()
    all_data: dict[str, list[tuple[str, str]]] = {}
    for rel in FILES:
        reps = collect_replacements(rel)
        if not reps:
            raise RuntimeError(f"No replacements for {rel}")
        preview = (ORIG / rel).read_text(encoding="utf-8")
        for old, new in reps:
            preview = preview.replace(old, new, 1)
        cn = len(re.findall(r"[\u4e00-\u9fff]", preview))
        if cn < 10:
            raise RuntimeError(f"CJK preview {cn}<10 for {rel}")
        all_data[rel] = reps

    lines = [
        '"""Chinese annotation replacements for Redisson 4.7.0 wave-59b api [15:30]."""',
        "from __future__ import annotations",
        "",
        "W59B_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {",
    ]
    emitted_short: set[str] = set()
    for rel, reps in all_data.items():
        short = Path(rel).name
        lines.append(f"    {rel!r}: [")
        for old, new in reps:
            lines.append(f"        ({old!r}, {new!r}),")
        lines.append("    ],")
        if short not in emitted_short:
            same = all(
                all_data[r] == reps for r in all_data if Path(r).name == short and r != rel
            )
            if same:
                lines.append(f"    {short!r}: [")
                for old, new in reps:
                    lines.append(f"        ({old!r}, {new!r}),")
                lines.append("    ],")
                emitted_short.add(short)
    lines.append("}")
    OUT.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(f"Wrote {OUT} ({len(all_data)} files)")
    for rel, reps in all_data.items():
        preview = (ORIG / rel).read_text(encoding="utf-8")
        for old, new in reps:
            preview = preview.replace(old, new, 1)
        cn = len(re.findall(r"[\u4e00-\u9fff]", preview))
        print(f"  {rel}: {len(reps)} reps, cn={cn}")


if __name__ == "__main__":
    emit()
