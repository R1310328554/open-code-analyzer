#!/usr/bin/env python3
"""Generate wave58a_replacements_redisson.py for set/stream/batch/bitset [0:15]."""
from __future__ import annotations

import re
from pathlib import Path

ROOT = Path("/workspace")
ORIG = ROOT / "redisson/redisson-4.7.0/original"
OUT = ROOT / "scripts/wave58a_replacements_redisson.py"
FILES = [
    ln.strip()
    for ln in Path("/tmp/re58a.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]

IMPL_CLASSES = {
    "RedissonSetMultimapValues",
    "RedissonSortedSet",
    "RedissonStream",
    "RedissonSubList",
    "RedissonTimeSeries",
    "RedissonTransferQueue",
    "RedissonVectorSet",
}

_EMPTY_JDOC = "/**\n * \n * @author Nikita Koksharov\n *\n */"
_EMPTY_JDOC2 = "/**\n *\n * @author Nikita Koksharov\n *\n */"
_EMPTY_JDOC3 = "/**\n * \n * @author Nikita Koksharov\n *\n * @param <V> value type\n */"

CLASS_JDOC: dict[str, str] = {
    "RedissonSetMultimapValues": (
        "/**\n"
        " * 集合型 Multimap Cache 值视图 {@link RSet} 实现。\n"
        " * <p>表示 {@link RedissonSetMultimapCache} 某 key 下的 Set 元素集合；\n"
        " * 读操作会校验过期 ZSET，写操作委托底层 {@link RedissonSet}。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " * @param <V> 元素类型\n"
        " */"
    ),
    "RedissonSortedSet": (
        "/**\n"
        " * 分布式有序集合 {@link RSortedSet} 实现。\n"
        " * <p>底层 Redis LIST 按 Comparator 有序插入；修改操作需获取分布式锁。\n"
        " * Comparator 序列化存储，支持 {@link Comparator#nullsFirst} 等无公参构造器的比较器。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " * @param <V> 元素类型\n"
        " */"
    ),
    "RedissonStream": (
        "/**\n"
        " * Redis Stream {@link RStream} 实现。\n"
        " * <p>封装 XADD/XREAD/XGROUP 等流命令，支持消费者组、ACK 与 Pending 消息管理。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " * @param <K> 流条目字段键类型\n"
        " * @param <V> 流条目字段值类型\n"
        " */"
    ),
    "RedissonSubList": (
        "/**\n"
        " * {@link RedissonList} 的子列表视图，对应 {@link java.util.List#subList} 语义。\n"
        " * <p>对子区间的增删改会映射到底层 Redis LIST 的相应索引范围。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " * @param <V> 元素类型\n"
        " */"
    ),
    "RedissonTimeSeries": (
        "/**\n"
        " * 时间序列 {@link RTimeSeries} 实现。\n"
        " * <p>基于 Redis ZSET 按时间戳存储带分数条目，支持范围查询、TTL 与惰性过期淘汰。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " * @param <V> 值类型\n"
        " * @param <L> 标签/元数据类型\n"
        " */"
    ),
    "RedissonTransferQueue": (
        "/**\n"
        " * 分布式转移队列 {@link RTransferQueue} 实现。\n"
        " * <p>基于 Redis LIST 与远程服务，支持 {@link java.util.concurrent.TransferQueue}\n"
        " * 零缓冲转移：生产者可直接将元素交给正在等待的消费者。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " * @param <V> 元素类型\n"
        " */"
    ),
    "RedissonVectorSet": (
        "/**\n"
        " * Redis 向量集合 {@link RVectorSet} 实现（Redis 8.0+）。\n"
        " * <p>支持 HNSW 向量索引、相似度检索、属性存储与字典序范围查询。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */"
    ),
}

JDOC_REPLACE: dict[str, tuple[str, str]] = {
    "Set based Multimap Cache values holder": (
        "/**\n"
        " * Set based Multimap Cache values holder\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " * @param <V> value\n"
        " */",
        CLASS_JDOC["RedissonSetMultimapValues"],
    ),
    "Distributed and concurrent implementation of {@link java.util.List}": (
        "/**\n"
        " * Distributed and concurrent implementation of {@link java.util.List}\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " * @param <V> the type of elements held in this collection\n"
        " */",
        CLASS_JDOC["RedissonSubList"],
    ),
}

FIELD_CN: list[tuple[str, str]] = [
    (
        "    private final RSet<V> set;",
        "    /** 底层 Set 委托对象。 */\n    private final RSet<V> set;",
    ),
    (
        "    private final Object key;",
        "    /** Multimap 外层键。 */\n    private final Object key;",
    ),
    (
        "    private final String timeoutSetName;",
        "    /** 过期时间 ZSET 键名。 */\n    private final String timeoutSetName;",
    ),
    (
        "    private Comparator comparator = Comparator.naturalOrder();",
        "    /** 元素比较器（默认自然序）。 */\n    private Comparator comparator = Comparator.naturalOrder();",
    ),
    (
        "    private RLock lock;",
        "    /** 修改有序集合时使用的分布式锁。 */\n    private RLock lock;",
    ),
    (
        "    private RedissonList<V> list;",
        "    /** 底层 Redis LIST 存储。 */\n    private RedissonList<V> list;",
    ),
    (
        "    final int fromIndex;",
        "    /** 子列表起始索引（含）。 */\n    final int fromIndex;",
    ),
    (
        "    AtomicInteger toIndex = new AtomicInteger();",
        "    /** 子列表结束索引（动态维护）。 */\n    AtomicInteger toIndex = new AtomicInteger();",
    ),
    (
        "    private final EvictionScheduler evictionScheduler;",
        "    /** 过期条目淘汰调度器。 */\n    private final EvictionScheduler evictionScheduler;",
    ),
]

METHOD_CN: dict[str, str] = {
    "size": "返回元素/条目数量。",
    "sizeAsync": "异步返回数量。",
    "isEmpty": "是否为空。",
    "contains": "是否包含指定元素。",
    "containsAsync": "异步检查是否包含。",
    "add": "添加元素。",
    "addAsync": "异步添加元素。",
    "remove": "移除元素。",
    "removeAsync": "异步移除元素。",
    "clear": "清空全部元素。",
    "delete": "删除 Redis 键。",
    "deleteAsync": "异步删除键。",
    "readAll": "一次性读取全部元素。",
    "readAllAsync": "异步一次性读取全部元素。",
    "iterator": "返回元素迭代器。",
    "addAll": "批量添加元素。",
    "addAllAsync": "异步批量添加。",
    "removeAll": "批量移除元素。",
    "removeAllAsync": "异步批量移除。",
    "retainAll": "仅保留指定集合中的元素。",
    "retainAllAsync": "异步 retainAll。",
    "containsAll": "是否包含指定集合的全部元素。",
    "containsAllAsync": "异步 containsAll。",
    "mapReduce": "创建 MapReduce 任务入口。",
    "tryAdd": "尝试添加元素（Set 语义）。",
    "tryAddAsync": "异步 tryAdd。",
    "union": "与指定集合求并集并写回。",
    "unionAsync": "异步求并集。",
    "intersection": "与指定集合求交集并写回。",
    "intersectionAsync": "异步求交集。",
    "diff": "与指定集合求差集并写回。",
    "diffAsync": "异步求差集。",
    "pollFirst": "弹出最小/首元素。",
    "pollFirstAsync": "异步 pollFirst。",
    "pollLast": "弹出最大/尾元素。",
    "pollLastAsync": "异步 pollLast。",
    "first": "返回最小/首元素。",
    "last": "返回最大/尾元素。",
    "comparator": "返回当前比较器。",
    "trySetComparator": "尝试设置比较器（仅空集合时成功）。",
    "binarySearch": "在有序列表中二分查找元素。",
    "loadComparator": "从 Redis 加载已持久化的比较器。",
    "wrapLockedAsync": "在分布式锁保护下执行异步命令。",
    "checkKey": "校验流条目键非空。",
    "checkValue": "校验流条目值非空。",
    "add": "向 Stream 追加条目。",
    "addAsync": "异步 XADD。",
    "read": "从 Stream 读取条目。",
    "readAsync": "异步 XREAD。",
    "createGroup": "创建消费者组。",
    "removeGroup": "删除消费者组。",
    "ack": "确认消息已处理。",
    "pending": "查询 Pending 消息。",
    "claim": "转移 Pending 消息所有权。",
    "trim": "裁剪 Stream 长度。",
    "tryTransfer": "尝试零缓冲转移元素给等待消费者。",
    "tryTransferAsync": "异步 tryTransfer。",
    "transfer": "阻塞转移元素给等待消费者。",
    "transferAsync": "异步 transfer。",
    "offer": "尝试入队。",
    "offerAsync": "异步入队。",
    "poll": "出队队首元素。",
    "pollAsync": "异步出队。",
    "take": "阻塞直到可取到元素。",
    "takeAsync": "异步阻塞出队。",
    "execute": "提交批处理并返回各命令结果。",
    "executeAsync": "异步提交批处理命令。",
    "discard": "丢弃批处理队列中的命令。",
    "getSigned": "读取指定位域的有符号整数。",
    "setSigned": "写入指定位域的有符号整数并返回旧值。",
    "getUnsigned": "读取指定位域的无符号整数。",
    "setUnsigned": "写入指定位域的无符号整数并返回旧值。",
    "get": "读取指定位。",
    "set": "设置指定位并返回旧值。",
    "setAsync": "异步设置位。",
    "cardinality": "返回置 1 的位数。",
    "length": "返回逻辑长度（最高置 1 位索引加一）。",
    "and": "对多个位集执行 AND。",
    "or": "对多个位集执行 OR。",
    "xor": "对多个位集执行 XOR。",
    "not": "对全部位取反。",
    "bitField": "执行 BITFIELD 多子命令。",
}

METHOD_RE = re.compile(
    r"(?P<prefix>\n    )(?P<override>@Override\n    )?"
    r"(?P<sig>(?:public|protected|private)\s+(?:static\s+)?[\w<>,\?\[\]\s]+\s+(\w+)\s*\([^)]*\)\s*(?:throws\s+[\w.\s,]+)?\{)"
)

TRANSLATIONS: dict[str, str] = {}


def t(en: str, cn: str) -> None:
    TRANSLATIONS[en.strip()] = cn.strip()


# --- class-level ---
t(
    "Use org.redisson.api.options.LocalCachedMapOptions instead",
    "已废弃：请改用 org.redisson.api.options.LocalCachedMapOptions",
)
t(
    "Use org.redisson.api.options.LocalCachedMapCacheOptions instead",
    "已废弃：请改用 org.redisson.api.options.LocalCachedMapCacheOptions",
)
t(
    "Configuration for LocalCachedMapCache object.",
    "{@link RLocalCachedMapCache} 本地缓存 Map 配置选项（含逐条 TTL）。",
)
t(
    "Configuration object for LocalCachedMap",
    "{@link RLocalCachedMap} 本地缓存 Map 配置选项。",
)
t(
    "Interface for using Redis pipeline feature.",
    "Redis 管道/批处理接口。\n     * <p>\n"
    "通过本接口获取的对象，其方法调用会进入批处理队列，"
    "稍后通过 {@code execute()} 或 {@code executeAsync()} 一次性提交。",
)
t(
    "Reactive interface for using Redis pipeline feature.",
    "Redis 管道/批处理 Reactor 响应式接口。",
)
t(
    "RxJava2 interface for using Redis pipeline feature.",
    "Redis 管道/批处理 RxJava3 接口。",
)
t(
    "Vector of bits that grows as needed.",
    "按需增长的 Redis 位图 {@link RBitSet} 同步 API；"
    "封装 GETBIT/SETBIT、BITCOUNT、BITOP 及位域读写。",
)
t(
    "Async interface for Vector of bits that grows as needed.",
    "Redis 位图 {@link RBitSet} 异步 API；各方法返回 {@link RFuture}。",
)
t(
    "Reactive interface for Vector of bits that grows as needed.",
    "Redis 位图 {@link RBitSet} Reactor 响应式 API。",
)

# --- LocalCachedMap options ---
t("Various strategies to avoid stale objects in local cache.", "避免本地缓存出现过期数据的策略。")
t(
    "Handle cases when map instance has been disconnected for a while.",
    "处理 Map 实例断开连接一段时间后的缓存一致性问题。",
)
t("No reconnect handling.", "不进行重连处理。")
t("Clear local cache if map instance disconnected.", "Map 实例断连后清空本地缓存。")
t(
    "Store invalidated entry hash in invalidation log for 10 minutes.",
    "将失效条目哈希写入失效日志并保留 10 分钟。",
)
t(
    "Cache keys for stored invalidated entry hashes will be removed",
    "若断连时间少于 10 分钟，则移除已记录失效哈希对应的缓存键；",
)
t(
    "if LocalCachedMap instance has been disconnected less than 10 minutes",
    "否则清空整个本地缓存。",
)
t("or whole local cache will be cleaned otherwise.", "（见上条）")
t(
    "Creates a new instance of LocalCachedMapOptions with default options.",
    "创建带默认选项的 LocalCachedMapOptions 实例。",
)
t("This is equivalent to:", "等价于：")
t("Defines Cache provider used as local cache store.", "指定本地缓存存储提供方。")
t("<p><code>REDISSON</code> - uses Redisson own implementation.", "<p>{@code REDISSON} — 使用 Redisson 内置实现。")
t("<p><code>CAFFEINE</code> - uses Caffeine implementation.", "<p>{@code CAFFEINE} — 使用 Caffeine 实现。")
t(
    "Defines how to listen expired event sent by Redis upon this instance deletion.",
    "定义如何监听本实例删除时 Redis 发送的过期事件。",
)
t("Defines local cache eviction policy.", "定义本地缓存淘汰策略。")
t(
    "<p><code>LRU</code> - uses local cache with LRU (least recently used) eviction policy.",
    "<p>{@code LRU} — 最近最少使用（LRU）淘汰。",
)
t(
    "<p><code>LFU</code> - uses local cache with LFU (least frequently used) eviction policy.",
    "<p>{@code LFU} — 最不经常使用（LFU）淘汰。",
)
t(
    "<p><code>SOFT</code> - uses local cache with soft references. The garbage collector will evict items from the local cache when the JVM is running out of memory.",
    "<p>{@code SOFT} — 软引用缓存；JVM 内存不足时 GC 回收条目。",
)
t(
    "<p><code>WEAK</code> - uses local cache with weak references. The garbage collector will evict items from the local cache when it became weakly reachable.",
    "<p>{@code WEAK} — 弱引用缓存；条目变为弱可达时 GC 回收。",
)
t(
    "<p><code>NONE</code> - doesn't use eviction policy, but timeToLive and maxIdleTime params are still working.",
    "<p>{@code NONE} — 不使用淘汰策略，但 timeToLive 与 maxIdleTime 仍生效。",
)
t("Defines local cache size.", "定义本地缓存容量。")
t("If size is <code>0</code> then local cache is unbounded.", "容量为 {@code 0} 表示无界缓存。")
t(
    "If size is <code>-1</code> then local cache is always empty and doesn't store data.",
    "容量为 {@code -1} 表示始终为空、不存储数据。",
)
t("Defines local cache synchronization strategy.", "定义本地缓存同步策略。")
t(
    "<p><code>INVALIDATE</code> - Default. Invalidate cache entry across all LocalCachedMap instances on map entry change",
    "<p>{@code INVALIDATE} — 默认；Map 条目变更时使所有节点本地缓存失效。",
)
t(
    "<p><code>UPDATE</code> - Insert/update cache entry across all LocalCachedMap instances on map entry change",
    "<p>{@code UPDATE} — Map 条目变更时在所有节点插入/更新缓存条目。",
)
t("<p><code>NONE</code> - No synchronizations on map changes", "<p>{@code NONE} — Map 变更时不做缓存同步。")
t(
    "Defines max idle time in milliseconds of each map entry in local cache.",
    "定义本地缓存每条目的最大空闲时间（毫秒）。",
)
t("Defines max idle time of each map entry in local cache.", "定义本地缓存每条目的最大空闲时间。")
t("If value equals to <code>0</code> then timeout is not applied", "值为 {@code 0} 表示不应用超时。")
t("Defines store mode of cache data.", "定义缓存数据的存储模式。")
t("<p><code>LOCALCACHE</code> - store data in local cache only.", "<p>{@code LOCALCACHE} — 仅存储在本地缓存。")
t(
    "<p><code>LOCALCACHE_REDIS</code> - store data in both Redis and local cache.",
    "<p>{@code LOCALCACHE_REDIS} — 同时存储在 Redis 与本地缓存。",
)
t(
    "Defines strategy for load missed local cache updates after Redis connection failure.",
    "定义 Redis 连接失败后加载遗漏本地缓存更新的策略。",
)
t(
    "<p><code>CLEAR</code> - clear local cache if map instance has been disconnected for a while.",
    "<p>{@code CLEAR} — 断连一段时间后清空本地缓存。",
)
t(
    "<p><code>LOAD</code> - store invalidated entry hash in invalidation log for 10 minutes. Cache keys for stored invalidated entry hashes will be removed if LocalCachedMap instance has been disconnected less than 10 minutes or whole cache will be cleaned otherwise",
    "<p>{@code LOAD} — 失效条目哈希写入 10 分钟失效日志；断连不足 10 分钟时移除对应缓存键，否则清空全部本地缓存。",
)
t("<p><code>NONE</code> - Default. No reconnection handling", "<p>{@code NONE} — 默认；不做重连处理。")
t(
    "Defines time to live in milliseconds of each map entry in local cache.",
    "定义本地缓存每条目的存活时间（毫秒）。",
)
t("Defines time to live of each map entry in local cache.", "定义本地缓存每条目的存活时间。")
t(
    "Defines whether to store CacheKey of an object key into the local cache. <br>",
    "是否将对象键的 CacheKey 存入本地缓存。<br>",
)
t("Defines whether to store a cache miss into the local cache.", "是否将缓存未命中（cache miss）结果存入本地缓存。")
t(
    "Defines whether to use a global topic pattern listener",
    "是否使用全局 Topic 模式监听器",
)
t("All references will be collected by GC", "所有引用最终由 GC 回收。")
t("Returns current options instance", "返回当前配置实例（链式调用）。")
t("Returns current options instance.", "返回当前配置实例。")

# --- RBatch ---
t(
    "All method invocations on objects got through this interface",
    "通过本接口获取的对象，其全部方法调用",
)
t(
    "are batched to separate queue and could be executed later",
    "会进入独立批处理队列，可稍后",
)
t("with <code>execute()</code> or <code>executeAsync()</code> methods.", "通过 {@code execute()} 或 {@code executeAsync()} 执行。")
t("Returns Array instance by <code>name</code>.", "按名称返回 {@link RArray} 批处理对象。")
t("Returns Array instance by <code>name</code>", "按名称返回 {@link RArray} 批处理对象")
t("using provided <code>codec</code> for values.", "并使用指定 {@code codec} 编解码值。")
t("Requires <b>Redis 8.8 or higher.</b>", "需要 <b>Redis 8.8 及以上</b>。")
t("Requires <b>Redis 7.4.0 and higher.</b> or <b>Valkey 9.0.0 and higher.</b>", "需要 <b>Redis 7.4.0+</b> 或 <b>Valkey 9.0.0+</b>。")
t("Covers BF.* commands.", "覆盖 BF.* 布隆过滤器命令。")
t("Returns bloom filter native instance by <code>name</code>.", "按名称返回原生布隆过滤器 {@link RBloomFilterNative} 批处理对象。")
t("Returns bloom filter native instance by <code>name</code>", "按名称返回原生布隆过滤器批处理对象")
t("using provided <code>codec</code> for objects.", "并使用指定 {@code codec} 编解码对象。")
t("Returns JSON data holder instance by name using provided codec.", "按名称与 codec 返回 JSON 数据持有者批处理对象。")
t("Returns HyperLogLog object", "返回 HyperLogLog 批处理对象")
t("Returns atomicDouble instance by name.", "按名称返回 {@link RAtomicDouble} 批处理对象。")
t("Returns atomicLong instance by name.", "按名称返回 {@link RAtomicLong} 批处理对象。")
t("Returns atomicLong instance by name", "按名称返回 {@link RAtomicLong} 批处理对象")
t("Returns Redis Sorted Set instance by name", "按名称返回 Redis 有序集合批处理对象")
t("Returns String based Redis Sorted Set instance by name", "按名称返回基于字符串的有序集合批处理对象")
t("Returns List based MultiMap instance by name.", "按名称返回 List Multimap 批处理对象。")
t("Returns List based MultiMap instance by name", "按名称返回 List Multimap 批处理对象")
t("Returns List based Multimap instance by name.", "按名称返回 List Multimap 批处理对象。")
t("Returns List based Multimap instance by name", "按名称返回 List Multimap 批处理对象")
t("Returns Set based MultiMap instance by name.", "按名称返回 Set Multimap 批处理对象。")
t("Returns Set based MultiMap instance by name", "按名称返回 Set Multimap 批处理对象")
t("Returns Set based Multimap instance by name.", "按名称返回 Set Multimap 批处理对象。")
t("Returns Set based Multimap instance by name", "按名称返回 Set Multimap 批处理对象")
t("Returns Sharded Topic instance by name.", "按名称返回分片 Topic 批处理对象。")
t("Returns Sharded Topic instance by name using provided codec for messages.", "按名称与消息 codec 返回分片 Topic 批处理对象。")
t("Returns Top-K sketch instance by <code>name</code>.", "按名称返回 Top-K 草图批处理对象。")
t("Returns Top-K sketch instance by <code>name</code>", "按名称返回 Top-K 草图批处理对象")
t("Returns API for RediSearch module", "返回 RediSearch 模块 API 批处理对象")
t("Returns API for RediSearch module using defined codec for attribute values.", "使用指定属性值 codec 返回 RediSearch API 批处理对象。")
t("Executes all operations accumulated during async methods invocations.", "执行批处理队列中累积的全部命令。")
t(
    "Executes all operations accumulated during async methods invocations asynchronously.",
    "异步执行批处理队列中累积的全部命令。",
)
t("Discard batched commands and release allocated buffers used for parameters encoding.", "丢弃批处理命令并释放参数编码缓冲区。")
t("If cluster configuration used then operations are grouped by slot ids", "集群模式下按 slot 分组执行。")
t("In cluster configurations operations grouped by slot ids", "集群配置下按 slot 分组。")
t("Each of Redis/Redisson object associated with own key", "每个 Redis/Redisson 对象对应独立键")
t("Messages are delivered to message listeners connected to the same Topic.", "消息投递给连接同一 Topic 的监听器。")
t("Doesn't allow duplications for values mapped to key.", "同一 key 下不允许重复值。")
t(
    "All elements are inserted with the same score during addition,",
    "添加时所有元素使用相同分数插入，",
)
t(
    "<p>If eviction is not required then it's better to use regular map {@link #getMap(String)}.</p>",
    "<p>若无需逐条 TTL，优先使用普通 Map {@link #getMap(String)}。</p>",
)
t(
    "<p>If eviction is not required then it's better to use regular map {@link #getMap(String, Codec)}.</p>",
    "<p>若无需逐条 TTL，优先使用普通 Map {@link #getMap(String, Codec)}。</p>",
)
t(
    "<p>If eviction is not required then it's better to use regular map {@link #getSet(String, Codec)}.</p>",
    "<p>若无需逐条 TTL，优先使用普通 Set {@link #getSet(String, Codec)}。</p>",
)
t(
    "<p>If eviction is not required then it's better to use regular map {@link #getSetMultimap(String)}.</p>",
    "<p>若无需逐条 TTL，优先使用普通 SetMultimap {@link #getSetMultimap(String)}。</p>",
)
t(
    "<p>If eviction is not required then it's better to use regular map {@link #getSetMultimap(String, Codec)}.</p>",
    "<p>若无需逐条 TTL，优先使用普通 SetMultimap {@link #getSetMultimap(String, Codec)}。</p>",
)

# --- RBitSet ---
t("Returns signed number at specified", "读取指定位域的有符号整数；")
t("<code>offset</code> and <code>size</code>", "参数为 {@code offset} 与 {@code size}。")
t(
    "Returns previous value of signed number and replaces it",
    "写入指定位域的有符号整数并返回旧值；",
)
t("with defined <code>value</code> at specified <code>offset</code>", "在指定 {@code offset} 处写入 {@code value}。")
t("Increments current signed value by", "将指定位域有符号整数加上")
t("defined <code>increment</code> value and <code>size</code>", "增量 {@code increment}（位宽 {@code size}）")
t("at specified <code>offset</code>", "位于 {@code offset}，并返回结果。")
t("and returns result.", "（见上条）")
t("Returns unsigned number at specified", "读取指定位域的无符号整数；")
t(
    "Returns previous value of unsigned number and replaces it",
    "写入指定位域的无符号整数并返回旧值；",
)
t("Increments current unsigned value by", "将指定位域无符号整数加上")
t("Returns byte number at specified <code>offset</code>", "读取 {@code offset} 处的字节值。")
t(
    "Returns previous value of byte number and replaces it",
    "写入 {@code offset} 处字节值并返回旧值；",
)
t("Returns short number at specified <code>offset</code>", "读取 {@code offset} 处的 short 值。")
t(
    "Returns previous value of short number and replaces it",
    "写入 {@code offset} 处 short 值并返回旧值；",
)
t("Returns integer number at specified <code>offset</code>", "读取 {@code offset} 处的 int 值。")
t(
    "Returns previous value of integer number and replaces it",
    "写入 {@code offset} 处 int 值并返回旧值；",
)
t("Returns long number at specified <code>offset</code>", "读取 {@code offset} 处的 long 值。")
t(
    "Returns previous value of long number and replaces it",
    "写入 {@code offset} 处 long 值并返回旧值；",
)
t(
    "Increments current byte value on defined <code>increment</code> value at specified <code>offset</code>",
    "在 {@code offset} 处将字节值加上 {@code increment} 并返回结果。",
)
t(
    "Increments current short value on defined <code>increment</code> value at specified <code>offset</code>",
    "在 {@code offset} 处将 short 值加上 {@code increment} 并返回结果。",
)
t(
    "Increments current integer value on defined <code>increment</code> value at specified <code>offset</code>",
    "在 {@code offset} 处将 int 值加上 {@code increment} 并返回结果。",
)
t(
    "Increments current long value on defined <code>increment</code> value at specified <code>offset</code>",
    "在 {@code offset} 处将 long 值加上 {@code increment} 并返回结果。",
)
t(
    "Returns <code>true</code> if bit set to one and <code>false</code> overwise.",
    "位为 1 返回 {@code true}，否则 {@code false}。",
)
t("Returns the number of bits set to one.", "返回置 1 的位数。")
t('Returns "logical size" = index of highest set bit plus one.', '返回"逻辑长度"= 最高置 1 位索引加一。')
t("Returns number of set bits.", "返回置 1 的位数。")
t("Returns zero if there are no any set bit.", "若无任何置 1 位则返回 0。")
t("Set all bits to zero", "将全部位清零。")
t("Set all bits to one", "将全部位置 1。")
t(
    "Set all bits to <code>value</code> from <code>fromIndex</code> (inclusive) to <code>toIndex</code> (exclusive)",
    "将 [{@code fromIndex}, {@code toIndex}) 范围内的位设为 {@code value}。",
)
t(
    "Set all bits to one from <code>fromIndex</code> (inclusive) to <code>toIndex</code> (exclusive)",
    "将 [{@code fromIndex}, {@code toIndex}) 范围内的位全部置 1。",
)
t("Set all bits to <code>value</code> which index in indexArray", "将 indexArray 中索引对应的位设为 {@code value}。")
t("Copy bits state of source BitSet object to this object", "将源 BitSet 的位状态复制到本对象。")
t("Executes AND operation over this object and specified bitsets.", "对本对象与指定位集执行 AND。")
t("Executes OR operation over this object and specified bitsets.", "对本对象与指定位集执行 OR。")
t("Executes XOR operation over this object and specified bitsets.", "对本对象与指定位集执行 XOR。")
t("Executes NOT operation over all bits", "对全部位执行 NOT。")
t("Executes bitwise ANDOR operation over this object and specified bitsets.", "对本对象与指定位集执行 ANDOR。")
t("Executes bitwise DIFF operation over this object and specified bitsets.", "对本对象与指定位集执行 DIFF。")
t("Executes bitwise DIFF1 operation over this object and specified bitsets.", "对本对象与指定位集执行 DIFF1。")
t("Executes bitwise ONE operation over this object and specified bitsets.", "对本对象与指定位集执行 ONE。")
t("Executes BITFIELD command with multiple subcommands", "执行 BITFIELD 多子命令。")
t(
    "Returns a boolean array where each element of the array corresponds to the query result of the input parameters.",
    "返回布尔数组，每个元素对应输入参数的位查询结果。",
)
t("<code>false</code> - if previous value was false", "{@code false} — 若旧值为 false")

PARAM_RETURN: dict[str, str] = {
    "@param name name of instance": "@param name 对象名称",
    "@param name - name of object": "@param name 对象名称",
    "@param name - name of instance": "@param name 实例名称",
    "@param codec codec for values": "@param codec 值编解码器",
    "@param codec - codec for values": "@param codec 值编解码器",
    "@param codec codec for objects": "@param codec 对象编解码器",
    "@param codec - codec for objects": "@param codec 对象编解码器",
    "@param <V> value type": "@param <V> 值类型",
    "@param <T> type of object": "@param <T> 对象类型",
    "@param <K> key type": "@param <K> 键类型",
    "@param cacheProvider": "@param cacheProvider 缓存提供方",
    "@param cacheSize size of cache": "@param cacheSize 缓存容量",
    "@param syncStrategy": "@param syncStrategy 同步策略",
    "@param evictionPolicy": "@param evictionPolicy 淘汰策略",
    "@param reconnectionStrategy": "@param reconnectionStrategy 重连策略",
    "@param storeMode": "@param storeMode 存储模式",
    "@param expirationEventPolicy expiration policy value": "@param expirationEventPolicy 过期事件策略",
    "@param timeToLiveInMillis - time to live in milliseconds": "@param timeToLiveInMillis 存活时间（毫秒）",
    "@param timeToLive - time to live": "@param timeToLive 存活时间",
    "@param maxIdleInMillis - time to live in milliseconds": "@param maxIdleInMillis 最大空闲时间（毫秒）",
    "@param maxIdle - max idle time": "@param maxIdle 最大空闲时间",
    "@param timeUnit - time unit": "@param timeUnit 时间单位",
    "@param size - size of signed number up to 64 bits": "@param size 有符号数位宽（最多 64 位）",
    "@param offset - offset of signed number": "@param offset 有符号数偏移",
    "@param value - value of signed number": "@param value 有符号数值",
    "@param increment - increment value": "@param increment 增量",
    "@param size - size of unsigned number up to 64 bits": "@param size 无符号数位宽（最多 64 位）",
    "@param offset - offset of unsigned number": "@param offset 无符号数偏移",
    "@param value - value of unsigned number": "@param value 无符号数值",
    "@param offset - offset of byte number": "@param offset 字节偏移",
    "@param value - value of byte number": "@param value 字节值",
    "@param offset - offset of short number": "@param offset short 偏移",
    "@param value - value of short number": "@param value short 值",
    "@param offset - offset of integer number": "@param offset int 偏移",
    "@param value - value of integer number": "@param value int 值",
    "@param offset - offset of long number": "@param offset long 偏移",
    "@param value - value of long number": "@param value long 值",
    "@param bs - BitSet source": "@param bs 源 BitSet",
    "@param bitsetNames - names of bitsets": "@param bitsetNames 位集名称数组",
    "@param index - bit index": "@param index 位索引",
    "@param value - value to set": "@param value 要设置的值",
    "@param fromIndex - from index": "@param fromIndex 起始索引（含）",
    "@param toIndex - to index": "@param toIndex 结束索引（不含）",
    "@param indexArray - indexes of bits": "@param indexArray 位索引数组",
    "@param args - bitfield arguments": "@param args BITFIELD 参数",
    "@return RArray object": "@return {@link RArray} 对象",
    "@return RBloomFilterNative object": "@return {@link RBloomFilterNative} 对象",
    "@return signed number": "@return 有符号整数",
    "@return previous value of signed number": "@return 旧有符号整数",
    "@return result value": "@return 结果值",
    "@return unsigned number": "@return 无符号整数",
    "@return previous value of unsigned number": "@return 旧无符号整数",
    "@return byte number": "@return 字节值",
    "@return previous value of byte number": "@return 旧字节值",
    "@return short number": "@return short 值",
    "@return previous value of short number": "@return 旧 short 值",
    "@return integer number": "@return int 值",
    "@return previous value of integer number": "@return 旧 int 值",
    "@return long number": "@return long 值",
    "@return previous value of long number": "@return 旧 long 值",
    "@return LocalCachedMapOptions instance": "@return LocalCachedMapOptions 实例",
    "@return LocalCachedMapCacheOptions instance": "@return LocalCachedMapCacheOptions 实例",
    "@return void": "@return 无返回值",
    "@return <code>true</code> if bit set to one and <code>false</code> overwise.": "@return 位为 1 则 {@code true}，否则 {@code false}",
}


def has_comment_before(text: str, pos: int) -> bool:
    window = text[max(0, pos - 120) : pos]
    return "/**" in window or "//" in window.split("\n")[-1]


def method_comment(name: str, cls: str) -> str:
    if name in METHOD_CN:
        return METHOD_CN[name]
    if name.endswith("Async"):
        base = name[:-5]
        return f"异步执行 {base}。"
    if name.startswith("get") and len(name) > 3:
        return f"获取 {name[3:]}。"
    if name.startswith("is") and len(name) > 2:
        return f"是否{ name[2:] }。"
    if name.startswith("set") and len(name) > 3:
        return f"设置{ name[3:] }。"
    if cls == "RedissonSetMultimapValues":
        return f"Set Multimap 值视图 {name} 操作。"
    if cls == "RedissonSortedSet":
        return f"有序集合 {name} 操作。"
    if cls == "RedissonStream":
        return f"Stream {name} 操作。"
    if cls == "RedissonSubList":
        return f"子列表 {name} 操作。"
    if cls == "RedissonTimeSeries":
        return f"时间序列 {name} 操作。"
    if cls == "RedissonTransferQueue":
        return f"转移队列 {name} 操作。"
    if cls == "RedissonVectorSet":
        return f"向量集合 {name} 操作。"
    return f"{name} 方法实现。"


def translate_javadoc(block: str) -> str | None:
    if not block.startswith("/**"):
        return None
    if "Copyright" in block and "Licensed under the Apache License" in block:
        return None
    result = block
    changed = False
    for en, cn in sorted(TRANSLATIONS.items(), key=lambda x: -len(x[0])):
        if en in result:
            result = result.replace(en, cn, 1)
            changed = True
    for en, cn in PARAM_RETURN.items():
        if en in result:
            result = result.replace(en, cn)
            changed = True
    if re.search(r"[\u4e00-\u9fff]", result):
        changed = True
    return result if changed and result != block else None


def class_javadoc_replacements(text: str, cls: str) -> list[tuple[str, str]]:
    reps: list[tuple[str, str]] = []
    for key, (old, new) in JDOC_REPLACE.items():
        if key in text and old in text:
            reps.append((old, new))
            return reps
    if cls in CLASS_JDOC:
        for old in (_EMPTY_JDOC, _EMPTY_JDOC2, _EMPTY_JDOC3):
            if old in text:
                reps.append((old, CLASS_JDOC[cls]))
                break
        if not reps:
            old = "/**\n *\n * @param <V> value type\n *\n * @author Nikita Koksharov\n *\n */"
            if old in text:
                reps.append((old, CLASS_JDOC[cls]))
        if not reps:
            old = "/**\n * \n * @author Nikita Koksharov\n *\n * @param <V> value type\n */"
            if old in text:
                reps.append((old, CLASS_JDOC[cls]))
        if not reps:
            old = "/**\n *\n * @author Nikita Koksharov\n *\n * @param <K> key type\n * @param <V> value type\n */"
            if old in text and cls == "RedissonStream":
                reps.append((old, CLASS_JDOC[cls]))
        if not reps:
            old = "/**\n *\n * @author Nikita Koksharov\n *\n */"
            if old in text and cls in ("RedissonTimeSeries", "RedissonTransferQueue", "RedissonVectorSet"):
                reps.append((old, CLASS_JDOC[cls]))
    return reps


def collect_auto_replacements(text: str, cls: str) -> list[tuple[str, str]]:
    reps: list[tuple[str, str]] = []
    for old, new in FIELD_CN:
        if old in text and new.split("\n", 1)[0] not in text:
            reps.append((old, new))
    for m in METHOD_RE.finditer(text):
        if has_comment_before(text, m.start()):
            continue
        name = m.group(4)
        cn = method_comment(name, cls)
        old = m.group(0)
        override = m.group("override")
        sig = m.group("sig")
        if override:
            new = f"\n    /** {cn} */\n    @Override\n    {sig}"
        else:
            new = f"\n    /** {cn} */\n    {sig}"
        reps.append((old, new))
    return reps


def collect_replacements(rel: str) -> list[tuple[str, str]]:
    path = ORIG / rel
    text = path.read_text(encoding="utf-8")
    cls = Path(rel).stem
    reps: list[tuple[str, str]] = []
    seen: set[str] = set()

    def add(old: str, new: str) -> None:
        if old not in text or old in seen:
            return
        reps.append((old, new))
        seen.add(old)

    if cls in IMPL_CLASSES:
        for old, new in class_javadoc_replacements(text, cls):
            add(old, new)
        for old, new in collect_auto_replacements(text, cls):
            add(old, new)
    else:
        for m in re.finditer(r"/\*\*.*?\*/", text, re.DOTALL):
            old = m.group(0)
            if "Copyright" in old and "Licensed under the Apache License" in old:
                continue
            new = translate_javadoc(old)
            if new and new != old:
                add(old, new)
    return reps


def emit() -> None:
    all_data: dict[str, list[tuple[str, str]]] = {}
    for rel in FILES:
        reps = collect_replacements(rel)
        if not reps:
            raise RuntimeError(f"No replacements for {rel}")
        all_data[rel] = reps

    lines = [
        '"""Chinese annotation replacements for Redisson 4.7.0 wave-58a set/stream/batch [0:15]."""',
        "from __future__ import annotations",
        "",
        "W58A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {",
    ]
    emitted_short: set[str] = set()
    for rel, reps in all_data.items():
        lines.append(f"    {rel!r}: [")
        for old, new in reps:
            lines.append(f"        ({old!r}, {new!r}),")
        lines.append("    ],")
        short = Path(rel).name
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
        cn = sum(len(re.findall(r"[\u4e00-\u9fff]", new)) for _, new in reps)
        print(f"  {rel}: {len(reps)} replacements, ~{cn} CJK in new text")


if __name__ == "__main__":
    emit()
