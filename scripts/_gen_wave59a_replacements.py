#!/usr/bin/env python3
"""Generate wave59a_replacements_redisson.py for map/mapcache/scoredsortedset [0:15]."""
from __future__ import annotations

import importlib.util
import re
from pathlib import Path

ROOT = Path("/workspace")
ORIG = ROOT / "redisson/redisson-4.7.0/original"
OUT = ROOT / "scripts/wave59a_replacements_redisson.py"
SCRIPTS = ROOT / "scripts"
_A = "redisson/src/main/java/org/redisson/api/"
FILES = [
    ln.strip()
    for ln in Path("/tmp/re59a.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]

# --- load wave58b phrase/param translations ---
_spec58b = importlib.util.spec_from_file_location(
    "w58b_gen", SCRIPTS / "_gen_wave58b_replacements.py"
)
_mod58b = importlib.util.module_from_spec(_spec58b)
assert _spec58b.loader is not None
_spec58b.loader.exec_module(_mod58b)

_spec58b_rep = importlib.util.spec_from_file_location(
    "w58b_rep", SCRIPTS / "wave58b_replacements_redisson.py"
)
_rep58b = importlib.util.module_from_spec(_spec58b_rep)
assert _spec58b_rep.loader is not None
_spec58b_rep.loader.exec_module(_rep58b)

TRANSLATIONS: dict[str, str] = dict(_mod58b.TRANSLATIONS)
PARAM_RETURN: dict[str, str] = dict(_mod58b.PARAM_RETURN)


def t(en: str, cn: str) -> None:
    TRANSLATIONS[en.strip()] = cn.strip()


# --- class-level overrides ---
MAP_CACHE_CLASS = (
    "/**\n * <p>Map-based cache with ability to set TTL for each entry via\n"
    " * {@link #put(Object, Object, long, TimeUnit)} or {@link #putIfAbsent(Object, Object, long, TimeUnit)}\n"
    " * And therefore has an complex lua-scripts inside.</p>\n *\n"
    " * <p>Current redis implementation doesnt have map entry eviction functionality.\n"
    " * Thus entries are checked for TTL expiration during any key/value/entry read operation.\n"
    " * If key/value/entry expired then it doesn't returns.\n"
    " * Expired tasks cleaned by {@link org.redisson.eviction.EvictionScheduler}. This scheduler\n"
    " * deletes expired entries in time interval between 5 seconds to 2 hours.</p>\n *\n"
    " * <p>If eviction is not required then it's better to use {@link org.redisson.RedissonMap}.</p>\n *\n"
    " * @author Nikita Koksharov\n *\n * @param <K> key\n * @param <V> value\n */",
    "/**\n * 带逐条目 TTL 的 Map 缓存 {@link RMapCache} API。\n"
    " * <p>通过 {@link #put(Object, Object, long, TimeUnit)} 或 {@link #putIfAbsent(Object, Object, long, TimeUnit)}\n"
    " * 设置条目过期；内部使用复杂 Lua 脚本。\n"
    " * <p>Redis 无原生逐条目淘汰，读操作时会检查 TTL；过期条目由 {@link org.redisson.eviction.EvictionScheduler}\n"
    " * 在 5 秒至 2 小时间隔内异步清理。\n"
    " * <p>若无需逐条目淘汰，建议使用 {@link org.redisson.RedissonMap}。</p>\n *\n"
    " * @author Nikita Koksharov\n * @param <K> 键类型\n * @param <V> 值类型\n */",
)

MAP_CACHE_ASYNC_CLASS = (
    "/**\n * <p>Map-based cache with ability to set TTL for each entry via\n"
    " * {@link RMapCache#put(Object, Object, long, TimeUnit)} or {@link RMapCache#putIfAbsent(Object, Object, long, TimeUnit)}\n"
    " * And therefore has an complex lua-scripts inside.</p>\n *\n"
    " * <p>Current redis implementation doesnt have map entry eviction functionality.\n"
    " * Thus entries are checked for TTL expiration during any key/value/entry read operation.\n"
    " * If key/value/entry expired then it doesn't returns.\n"
    " * Expired tasks cleaned by {@link org.redisson.eviction.EvictionScheduler}. This scheduler\n"
    " * deletes expired entries in time interval between 5 seconds to 2 hours.</p>\n *\n"
    " * <p>If eviction is not required then it's better to use {@link org.redisson.RedissonMap}.</p>\n *\n"
    " * @author Nikita Koksharov\n *\n * @param <K> key\n * @param <V> value\n */",
    "/**\n * {@link RMapCache} 异步 API；各方法返回 {@link RFuture}。\n"
    " * <p>支持逐条目 TTL、LRU 容量淘汰与 MapLoader/MapWriter。\n"
    " * <p>过期条目由 {@link org.redisson.eviction.EvictionScheduler} 异步清理。\n *\n"
    " * @author Nikita Koksharov\n * @param <K> 键类型\n * @param <V> 值类型\n */",
)

MAP_CACHE_REACTIVE_CLASS = (
    "/**\n * <p>Map-based cache with ability to set TTL for each entry via\n"
    " * {@link #put(Object, Object, long, TimeUnit)} or {@link #putIfAbsent(Object, Object, long, TimeUnit)} method.\n"
    " * And therefore has an complex lua-scripts inside.</p>\n *\n"
    " * <p>Current redis implementation doesnt have map entry eviction functionality.\n"
    " * Thus entries are checked for TTL expiration during any key/value/entry read operation.\n"
    " * If key/value/entry expired then it doesn't returns and clean task runs asynchronous.\n"
    " * Clean task deletes removes 100 expired entries at once.\n"
    " * In addition there is {@link org.redisson.eviction.EvictionScheduler}. This scheduler\n"
    " * deletes expired entries in time interval between 5 seconds to 2 hours.</p>\n *\n"
    " * <p>If eviction is not required then it's better to use {@link org.redisson.reactive.RedissonMapReactive}.</p>\n *\n"
    " * @author Nikita Koksharov\n *\n * @param <K> key\n * @param <V> value\n */",
    "/**\n * {@link RMapCache} Reactor 响应式 API；各方法返回 {@link Mono}。\n"
    " * <p>支持逐条目 TTL、LRU 容量淘汰与 MapLoader/MapWriter。\n"
    " * <p>过期条目由后台任务与 {@link org.redisson.eviction.EvictionScheduler} 清理。\n *\n"
    " * @author Nikita Koksharov\n * @param <K> 键类型\n * @param <V> 值类型\n */",
)

MAP_CACHE_RX_CLASS = (
    "/**\n * <p>Map-based cache with ability to set TTL for each entry via\n"
    " * {@link #put(Object, Object, long, TimeUnit)} or {@link #putIfAbsent(Object, Object, long, TimeUnit)} method.\n"
    " * And therefore has an complex lua-scripts inside.</p>\n *\n"
    " * <p>Current redis implementation doesnt have map entry eviction functionality.\n"
    " * Thus entries are checked for TTL expiration during any key/value/entry read operation.\n"
    " * If key/value/entry expired then it doesn't returns and clean task runs asynchronous.\n"
    " * Clean task deletes removes 100 expired entries at once.\n"
    " * In addition there is {@link org.redisson.eviction.EvictionScheduler}. This scheduler\n"
    " * deletes expired entries in time interval between 5 seconds to 2 hours.</p>\n *\n"
    " * <p>If eviction is not required then it's better to use {@link org.redisson.reactive.RedissonMapReactive}.</p>\n *\n"
    " * @author Nikita Koksharov\n *\n * @param <K> key\n * @param <V> value\n */",
    "/**\n * {@link RMapCache} RxJava3 API；各方法返回 {@link Single}/{@link Completable}。\n"
    " * <p>支持逐条目 TTL、LRU 容量淘汰与 MapLoader/MapWriter。\n"
    " * <p>过期条目由后台任务与 {@link org.redisson.eviction.EvictionScheduler} 清理。\n *\n"
    " * @author Nikita Koksharov\n * @param <K> 键类型\n * @param <V> 值类型\n */",
)

NATIVE_CLASS = (
    "/**\n * Map-based cache with ability to set TTL per entry.\n"
    " * Uses Redis native commands for entry expiration and not a scheduled eviction task.\n"
    " * <p>\n * Requires <b>Redis 7.4.0 and higher.</b>\n *\n * @author Nikita Koksharov\n *\n"
    " * @param <K> key\n * @param <V> value\n */",
    "/**\n * 基于 Redis 原生过期语义的 {@link RMapCacheNative} API。\n"
    " * <p>条目 TTL 由 Redis 服务器维护，无需定时扫描淘汰任务。\n"
    " * <p>需要 <b>Redis 7.4.0 及以上</b>。\n *\n * @author Nikita Koksharov\n"
    " * @param <K> 键类型\n * @param <V> 值类型\n */",
)

NATIVE_REACTIVE_CLASS = (
    "/**\n * Map-based cache with ability to set TTL per entry.\n"
    " * Uses Redis native commands for entry expiration and not a scheduled eviction task.\n"
    " * <p>\n * Requires <b>Redis 7.4.0 and higher.</b>\n *\n * @param <K> key\n * @param <V> value\n"
    " * @author Nikita Koksharov\n */",
    "/**\n * {@link RMapCacheNative} Reactor 响应式 API。\n"
    " * <p>条目 TTL 由 Redis 原生命令维护；需要 <b>Redis 7.4.0 及以上</b>。\n *\n"
    " * @param <K> 键类型\n * @param <V> 值类型\n * @author Nikita Koksharov\n */",
)

CLASS_OVERRIDES: dict[str, list[tuple[str, str]]] = {
    f"{_A}RMapAsync.java": [
        (
            "/**\n * Async interface for Redis based implementation\n"
            " * of {@link java.util.concurrent.ConcurrentMap} and {@link java.util.Map}\n"
            " * <p>\n * This map uses serialized state of key instead of hashCode or equals methods.\n"
            " * This map doesn't allow to store <code>null</code> as key or value.\n *\n"
            " * @author Nikita Koksharov\n *\n * @param <K> key\n * @param <V> value\n */",
            "/**\n * 基于 Redis 的 {@link java.util.concurrent.ConcurrentMap} 与 {@link java.util.Map} 异步 API。\n"
            " * <p>使用键序列化状态而非 hashCode/equals；不允许 {@code null} 键或值。\n"
            " * 各方法返回 {@link RFuture}；支持 MapLoader/MapWriter 与 per-key 分布式锁。\n *\n"
            " * @author Nikita Koksharov\n * @param <K> 键类型\n * @param <V> 值类型\n */",
        ),
    ],
    f"{_A}RMapReactive.java": [
        (
            "/**\n * Reactive interface for Redis based implementation\n"
            " * of {@link java.util.concurrent.ConcurrentMap} and {@link java.util.Map}\n"
            " * <p>\n * This map uses serialized state of key instead of hashCode or equals methods.\n"
            " * This map doesn't allow to store <code>null</code> as key or value.\n *\n"
            " * @author Nikita Koksharov\n *\n * @param <K> key\n * @param <V> value\n */",
            "/**\n * 基于 Redis 的 {@link java.util.concurrent.ConcurrentMap} 与 {@link java.util.Map} Reactor API。\n"
            " * <p>使用键序列化状态而非 hashCode/equals；不允许 {@code null} 键或值。\n"
            " * 各方法返回 {@link Mono}/{@link Flux}；支持 MapLoader/MapWriter 与 per-key 分布式锁。\n *\n"
            " * @author Nikita Koksharov\n * @param <K> 键类型\n * @param <V> 值类型\n */",
        ),
    ],
    f"{_A}RMapRx.java": [
        (
            "/**\n * RxJava2 interface for Redis based implementation\n"
            " * of {@link java.util.concurrent.ConcurrentMap} and {@link java.util.Map}\n"
            " * <p>\n * This map uses serialized state of key instead of hashCode or equals methods.\n"
            " * This map doesn't allow to store <code>null</code> as key or value.\n *\n"
            " * @author Nikita Koksharov\n *\n * @param <K> key\n * @param <V> value\n */",
            "/**\n * 基于 Redis 的 {@link java.util.concurrent.ConcurrentMap} 与 {@link java.util.Map} RxJava3 API。\n"
            " * <p>使用键序列化状态而非 hashCode/equals；不允许 {@code null} 键或值。\n"
            " * 各方法返回 {@link Single}/{@link Maybe}/{@link Completable}；支持 MapLoader/MapWriter。\n *\n"
            " * @author Nikita Koksharov\n * @param <K> 键类型\n * @param <V> 值类型\n */",
        ),
    ],
    f"{_A}RMapCache.java": [MAP_CACHE_CLASS],
    f"{_A}RMapCacheAsync.java": [MAP_CACHE_ASYNC_CLASS],
    f"{_A}RMapCacheReactive.java": [MAP_CACHE_REACTIVE_CLASS],
    f"{_A}RMapCacheRx.java": [MAP_CACHE_RX_CLASS],
    f"{_A}RMapCacheNative.java": [NATIVE_CLASS],
    f"{_A}RMapCacheNativeAsync.java": [NATIVE_CLASS],
    f"{_A}RMapCacheNativeReactive.java": [NATIVE_REACTIVE_CLASS],
    f"{_A}RMapCacheNativeRx.java": [NATIVE_CLASS],
    f"{_A}RPermitExpirableSemaphoreRx.java": [
        (
            "/**\n * RxJava2 interface for Semaphore object with lease time parameter support for each acquired permit.\n"
            " * \n * <p>Each permit identified by own id and could be released only using its id.\n"
            " * Permit id is a 128-bits unique random identifier generated each time during acquiring.\n"
            " *   \n * <p>Works in non-fair mode. Therefore order of acquiring is unpredictable.\n"
            " * \n * @author Nikita Koksharov\n *\n */",
            "/**\n * 可过期许可分布式信号量 {@link RPermitExpirableSemaphore} RxJava3 API。\n"
            " * <p>每次 acquire 返回带 TTL 的 permitId（128 位随机标识），仅能用该 id 释放。\n"
            " * <p>非公平模式，获取顺序不可预测。\n *\n * @author Nikita Koksharov\n */",
        ),
    ],
    f"{_A}RScoredSortedSet.java": [
        (
            "/**\n * Set containing elements sorted by score.\n * \n * @author Nikita Koksharov\n *\n"
            " * @param <V> object type\n */",
            "/**\n * 按分数排序的有序集合 {@link RScoredSortedSet}（Redis ZSET）同步 API。\n"
            " * <p>封装 ZADD/ZREM、ZRANGE/ZREVRANGE、ZSCAN、ZUNION/ZINTER\n"
            " * 及按分数/排名范围查询、MapReduce 等操作。\n *\n * @author Nikita Koksharov\n * @param <V> 成员类型\n */",
        ),
        (
            "        /**\n         * Score of each member equals the count of input sets containing it,\n"
            "         * optionally scaled by WEIGHTS. Scores from input sets are ignored.\n"
            "         * Requires <b>Redis 8.8.0 or higher.</b>\n         */",
            "        /**\n         * 成员分数等于包含该成员的输入集合数量（可按 WEIGHTS 缩放）；\n"
            "         * 忽略输入集合中的原始分数。需要 <b>Redis 8.8.0 及以上</b>。\n         */",
        ),
    ],
    f"{_A}RScoredSortedSetAsync.java": [
        (
            "/**\n * \n * @author Nikita Koksharov\n *\n * @param <V> value\n */",
            "/**\n * {@link RScoredSortedSet} 异步 API；各方法返回 {@link RFuture}。\n"
            " * <p>支持 ZADD/ZREM、范围查询、并集/交集/差集及 MapReduce。\n *\n"
            " * @author Nikita Koksharov\n * @param <V> 成员类型\n */",
        ),
    ],
    f"{_A}RScoredSortedSetReactive.java": [
        (
            "/**\n * Reactive interface for SortedSet object\n * \n * @author Nikita Koksharov\n *\n"
            " * @param <V> value type\n */",
            "/**\n * {@link RScoredSortedSet} Reactor 响应式 API；各方法返回 {@link Mono}/{@link Flux}。\n"
            " * <p>支持 ZADD/ZREM、范围查询、并集/交集/差集及 MapReduce。\n *\n"
            " * @author Nikita Koksharov\n * @param <V> 成员类型\n */",
        ),
    ],
}

# --- shared RMap replacements from wave58b ---
RMAP_SHARED: dict[str, str] = {}
for old, new in _rep58b.W58B_REPLACEMENTS.get(f"{_A}RMap.java", []):
    RMAP_SHARED[old] = new

# --- phrase translations ---
t(
    "Async interface for Redis based implementation\n     * of {@link java.util.concurrent.ConcurrentMap} and {@link java.util.Map}",
    "基于 Redis 的 {@link java.util.concurrent.ConcurrentMap} 与 {@link java.util.Map} 异步 API",
)
t(
    "Reactive interface for Redis based implementation\n     * of {@link java.util.concurrent.ConcurrentMap} and {@link java.util.Map}",
    "基于 Redis 的 {@link java.util.concurrent.ConcurrentMap} 与 {@link java.util.Map} Reactor API",
)
t(
    "RxJava2 interface for Redis based implementation\n     * of {@link java.util.concurrent.ConcurrentMap} and {@link java.util.Map}",
    "基于 Redis 的 {@link java.util.concurrent.ConcurrentMap} 与 {@link java.util.Map} RxJava3 API",
)
t(
    "Associates specified key with the given value if key isn't already associated with a value.",
    "若键尚无映射则关联指定值；否则用 remapping 函数合并并更新值。",
)
t(
    "Computes a new mapping for the specified key and its current mapped value.",
    "根据键及其当前映射值计算新映射。",
)
t(
    "Computes a mapping for the specified key if it's not mapped before.",
    "仅当键尚未映射时计算并存储新值。",
)
t(
    "Computes a mapping for the specified key only if it's already mapped.",
    "仅当键已有映射时计算并更新值。",
)
t(
    "If the specified key is not already associated with a value, associates it with the given value.",
    "若键尚无映射则关联给定值。",
)
t("Sets max size of the map and overrides current value.", "设置 Map 最大容量并覆盖当前配置。")
t(
    "Superfluous elements are evicted using LRU algorithm.",
    "超出容量时按 LRU 算法淘汰条目。",
)
t(
    "Superfluous elements are evicted using LRU algorithm by default.",
    "超出容量时默认按 LRU 算法淘汰条目。",
)
t(
    "Superfluous elements are evicted using defined algorithm.",
    "超出容量时按指定淘汰算法移除条目。",
)
t("Tries to set max size of the map.", "尝试设置 Map 最大容量。")
t(
    "Stores value mapped by key with specified time to live.",
    "存储键值对并设置 TTL。",
)
t(
    "Stores value mapped by key with specified time to live and max idle time.",
    "存储键值对并设置 TTL 与最大空闲时间。",
)
t(
    "Remaining time to live of map entry associated with a <code>key</code>.",
    "返回指定键对应条目的剩余 TTL。",
)
t(
    "Remaining time to live of map entries associated with <code>keys</code>.",
    "返回指定键集合对应条目的剩余 TTL 映射。",
)
t(
    "Sets time to live of specified entry by key.",
    "为指定键的条目设置 TTL。",
)
t(
    "Sets time to live of specified entries by keys.",
    "为指定键集合的条目设置 TTL。",
)
t(
    "Updates time to live of specified entry by key.",
    "更新指定键条目的 TTL。",
)
t(
    "Updates time to live of specified entries by keys.",
    "更新指定键集合条目的 TTL。",
)
t(
    "Sets time to live and max idle time of specified entry by key.",
    "为指定键条目设置 TTL 与最大空闲时间。",
)
t(
    "Sets time to live and max idle time of specified entries by keys.",
    "为指定键集合条目设置 TTL 与最大空闲时间。",
)
t(
    "Updates time to live and max idle time of specified entry by key.",
    "更新指定键条目的 TTL 与最大空闲时间。",
)
t(
    "Updates time to live and max idle time of specified entries by keys.",
    "更新指定键集合条目的 TTL 与最大空闲时间。",
)
t(
    "Sets time to live of specified entry by key only if it's greater than timeout set before.",
    "仅当新 TTL 大于已有 TTL 时为指定键条目设置过期时间。",
)
t(
    "Sets time to live of specified entry by key only if it's less than timeout set before.",
    "仅当新 TTL 小于已有 TTL 时为指定键条目设置过期时间。",
)
t(
    "Sets time to live of specified entries by keys only if it's greater than timeout set before.",
    "仅当新 TTL 大于已有 TTL 时为指定键集合条目设置过期时间。",
)
t(
    "Sets time to live of specified entries by keys only if it's less than timeout set before.",
    "仅当新 TTL 小于已有 TTL 时为指定键集合条目设置过期时间。",
)
t(
    "Clears an expiration timeout or date of specified entry by key.",
    "清除指定键条目的过期时间。",
)
t(
    "Clears an expiration timeout or date of specified entries by keys.",
    "清除指定键集合条目的过期时间。",
)
t(
    "Associates the specified <code>value</code> with the specified <code>key</code>",
    "将指定 {@code value} 关联到 {@code key}",
)
t(
    "Returns iterator over map entries collection.",
    "返回 Map 条目集合的迭代器。",
)
t(
    "Returns iterator over values collection of this map.",
    "返回本 Map 值集合的迭代器。",
)
t(
    "Returns iterator over key set of this map.",
    "返回本 Map 键集合的迭代器。",
)
t(
    "Returns values of this map using iterable.",
    "通过可迭代对象返回本 Map 的全部值。",
)
t(
    "Returns map entries using iterable.",
    "通过可迭代对象返回本 Map 的全部键值对。",
)
t("Returns size of this map", "返回本 Map 的元素数量。")
t(
    "Read all map as local instance at once",
    "一次性读取全部 Map 条目到本地实例。",
)
t(
    "Returns random keys from this map limited by <code>count</code>",
    "随机返回至多 {@code count} 个键。",
)
t(
    "Returns random map entries from this map limited by <code>count</code>",
    "随机返回至多 {@code count} 个键值对。",
)
t(
    "Returns map slice contained the mappings with defined <code>keys</code>.",
    "返回指定 {@code keys} 对应的 Map 切片。",
)
t("Returns the number of entries in cache.", "返回缓存中的条目数量。")
t(
    "Stores map entries specified in the {@code args} parameter.",
    "存储 {@code args} 中指定的 Map 条目。",
)
t(
    "Stores the specified entries only if all specified keys already exist.",
    "仅当全部指定键已存在时存储条目。",
)
t(
    "Stores the specified entries only if none of the specified keys exist.",
    "仅当全部指定键不存在时存储条目。",
)
t("Use {@link #putAll(PutArgs)} method instead.", "请改用 {@link #putAll(PutArgs)}。")
t(
    "Set containing elements sorted by score.",
    "按分数排序的有序集合（Redis ZSET）。",
)
t(
    "Returns <code>RMapReduce</code> object associated with this object",
    "返回与当前集合关联的 {@link RCollectionMapReduce} 实例。",
)
t(
    "Adds element to this set, overrides previous score if it has been already added.",
    "向集合添加元素；若已存在则覆盖原分数。",
)
t(
    "Removes and returns the head element or {@code null} if this sorted set is empty.",
    "移除并返回分数最小（队首）元素；空集合时返回 {@code null}。",
)
t(
    "Removes and returns the tail element or {@code null} if this sorted set is empty.",
    "移除并返回分数最大（队尾）元素；空集合时返回 {@code null}。",
)
t(
    "Removes and returns the head entries (value and its score).",
    "移除并返回分数最小的多个条目（成员与分数）。",
)
t(
    "Returns rank and score of specified <code>value</code>,",
    "返回指定成员的排名与分数；",
)
t(
    "Returns all entries (value and its score) between <code>startScore</code> and <code>endScore</code>.",
    "返回 {@code startScore} 到 {@code endScore} 区间内的全部条目。",
)
t(
    "Returns all entries (value and its score) between <code>startScore</code> and <code>endScore</code> ",
    "返回 {@code startScore} 到 {@code endScore} 区间内的全部条目 ",
)
t(
    "Returns an iterator over entries (value and its score) in this set.",
    "返回本集合条目（成员与分数）的迭代器。",
)
t(
    "Returns an iterator over elements in this set.",
    "返回本集合成员的迭代器。",
)
t("Returns stream of elements in this set.", "返回本集合成员的 Stream。")
t(
    "Union provided ScoredSortedSets mapped to weight multiplier",
    "对给定有序集合按权重求并集并写入当前集合。",
)
t("Intersect provided ScoredSortedSets", "对给定有序集合求交集并写入当前集合。")
t("Diff ScoredSortedSets specified by name", "对指定名称的有序集合求差集并写入当前集合。")
t(
    "Counts elements of set as a result of sets intersection with current set.",
    "统计当前集合与指定集合交集的元素数量。",
)
t("Use {@link #intersection(SetIntersectionArgs)} instead.", "请改用 {@link #intersection(SetIntersectionArgs)}。")
t("Use {@link #readIntersection(SetIntersectionArgs)} instead.", "请改用 {@link #readIntersection(SetIntersectionArgs)}。")
t("Use {@link #union(SetUnionArgs)} instead.", "请改用 {@link #union(SetUnionArgs)}。")
t("Use {@link #readUnion(SetUnionArgs)} instead.", "请改用 {@link #readUnion(SetUnionArgs)}。")
t(
    "Use {@link #intersectionAsync(SetIntersectionArgs)} instead.",
    "请改用 {@link #intersectionAsync(SetIntersectionArgs)}。",
)
t(
    "Use {@link #readIntersectionAsync(SetIntersectionArgs)} instead.",
    "请改用 {@link #readIntersectionAsync(SetIntersectionArgs)}。",
)
t("Use {@link #unionAsync(SetUnionArgs)} instead.", "请改用 {@link #unionAsync(SetUnionArgs)}。")
t("Use {@link #readUnionAsync(SetUnionArgs)} instead.", "请改用 {@link #readUnionAsync(SetUnionArgs)}。")
t(
    "Removes and returns first available head elements of <b>any</b> sorted set,",
    "从<b>任意</b>有序集合移除并返回最先可用的队首元素，",
)
t(
    "Removes and returns first available head elements",
    "移除并返回最先可用的队首元素",
)
t(
    "Removes and returns first available tail element of <b>any</b> sorted set,",
    "从<b>任意</b>有序集合移除并返回最先可用的队尾元素，",
)
t(
    "Removes and returns first available tail elements of <b>any</b> sorted set,",
    "从<b>任意</b>有序集合移除并返回最先可用的队尾元素，",
)
t(
    "Acquires a permit from this semaphore, blocking until one is",
    "阻塞获取一个许可，直到可用或线程被中断。",
)
t(
    "Acquires defined amount of <code>permits</code> from this semaphore, blocking until enough permits are",
    "阻塞获取指定数量的许可，直到足够或线程被中断。",
)
t(
    "Acquires a permit only if one is available at the time of invoking.",
    "非阻塞尝试获取一个许可；无可用许可时立即返回。",
)
t(
    "Acquires defined amount of <code>permits</code> only if all of them available at the time of invoking.",
    "非阻塞尝试获取指定数量许可；数量不足时立即返回。",
)
t(
    "Releases a permit with defined <code>permitId</code>",
    "使用指定 {@code permitId} 释放许可。",
)
t(
    "Updates a permit time to live by <code>permitId</code>",
    "按 {@code permitId} 更新许可的 TTL。",
)
t(
    "Returns amount of available permits",
    "返回当前可用许可数量。",
)
t(
    "Returns list of all acquired permits",
    "返回全部已获取许可的 ID 列表。",
)
t(
    "Tries to set new permits amount.",
    "尝试设置许可总数。",
)
t(
    "Adds new permits or reduces amount of available permits",
    "增加许可总数或减少可用许可数量。",
)

# extra param/return
PARAM_RETURN.update(
    {
        "@param <K> key": "@param <K> 键类型",
        "@param <V> value": "@param <V> 值类型",
        "@param <V> object type": "@param <V> 成员类型",
        "@param <V> value type": "@param <V> 成员类型",
        "@param key - map key": "@param key 映射键",
        "@param value - value to be merged with the existing value": "@param value 待合并的值",
        "@param remappingFunction - the function is invoked with the existing value to compute new value": "@param remappingFunction 合并函数",
        "@param mappingFunction - function to compute a value": "@param mappingFunction 映射函数",
        "@param maxSize - max size": "@param maxSize 最大容量",
        "@param mode - eviction mode": "@param mode 淘汰模式",
        "@param timeToLive - timeout before object will be deleted": "@param timeToLive 过期时长",
        "@param timeUnit - timeout time unit": "@param timeUnit 时间单位",
        "@param maxIdleTime - max idle timeout before object will be deleted": "@param maxIdleTime 最大空闲时长",
        "@param maxIdleUnit - max idle timeout time unit": "@param maxIdleUnit 最大空闲时间单位",
        "@param ttl - time to live duration": "@param ttl TTL 时长",
        "@param ttlDuration - time to live duration": "@param ttlDuration TTL 时长",
        "@param keys - map keys": "@param keys 键集合",
        "@param score - object score": "@param score 成员分数",
        "@param value - object": "@param value 成员",
        "@param permits - amount of permits": "@param permits 许可数量",
        "@param permitId - id of permit": "@param permitId 许可 ID",
        "@param timeout - the maximum time to wait": "@param timeout 最长等待时间",
        "@param unit - time unit": "@param unit 时间单位",
        "@return permit id": "@return 许可 ID",
        "@return amount of added permits": "@return 新增许可数量",
        "@return <code>true</code> if max size has been successfully set, otherwise <code>false</code>.": "@return 设置成功则为 true，否则 false",
        "@return void": "@return 无返回值",
        "@return previous score": "@return 先前的分数",
        "@return <code>true</code> if element added and <code>false</code> if element already added and score updated": "@return 新添加为 true，更新已有分数为 false",
    }
)


def translate_javadoc(block: str) -> str | None:
    if block in RMAP_SHARED:
        return RMAP_SHARED[block]
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
    if re.search(r"[\u4e00-\u9fff]", result) and result != block:
        return result
    return result if changed else None


def _overlaps(a: str, b: str) -> bool:
    return a in b or b in a


def collect_replacements(rel: str) -> list[tuple[str, str]]:
    text = (ORIG / rel).read_text(encoding="utf-8")
    reps: list[tuple[str, str]] = []
    seen: set[str] = set()
    override_olds = [o for o, _ in CLASS_OVERRIDES.get(rel, [])]

    for old, new in CLASS_OVERRIDES.get(rel, []):
        if old in text and old not in seen:
            reps.append((old, new))
            seen.add(old)

    for m in re.finditer(r"/\*\*.*?\*/", text, re.DOTALL):
        old = m.group(0)
        if old in seen:
            continue
        if any(_overlaps(old, o) for o in override_olds):
            continue
        if "Copyright" in old and "Licensed under the Apache License" in old:
            continue
        new = translate_javadoc(old)
        if new and new != old:
            reps.append((old, new))
            seen.add(old)

    return reps


def emit() -> None:
    all_data: dict[str, list[tuple[str, str]]] = {}
    for rel in FILES:
        reps = collect_replacements(rel)
        if not reps:
            raise RuntimeError(f"No replacements for {rel}")
        preview = (ORIG / rel).read_text(encoding="utf-8")
        for old, new in reps:
            if old not in preview:
                raise RuntimeError(f"Sequential apply miss for {rel}: {old[:80]!r}")
            preview = preview.replace(old, new, 1)
        cn = len(re.findall(r"[\u4e00-\u9fff]", preview))
        if cn < 10:
            raise RuntimeError(f"CJK preview {cn}<10 for {rel}")
        all_data[rel] = reps
        print(f"  {Path(rel).name}: {len(reps)} reps, cjk={cn}")

    lines = [
        '"""Chinese annotation replacements for Redisson 4.7.0 wave-59a api [0:15]."""',
        "from __future__ import annotations",
        "",
        "W59A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {",
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


if __name__ == "__main__":
    emit()
