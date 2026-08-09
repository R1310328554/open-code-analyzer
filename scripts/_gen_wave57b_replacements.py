#!/usr/bin/env python3
"""Generate wave57b_replacements_redisson.py for core Redisson [15:30]."""
from __future__ import annotations

import re
from pathlib import Path

ROOT = Path("/workspace")
ORIG = ROOT / "redisson/redisson-4.7.0/original"
OUT = ROOT / "scripts/wave57b_replacements_redisson.py"
FILES = [
    ln.strip()
    for ln in Path("/tmp/re57b.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]

_EMPTY_JDOC = "/**\n * \n * @author Nikita Koksharov\n *\n */"
_EMPTY_JDOC2 = "/**\n *\n * @author Nikita Koksharov\n *\n */"
_EMPTY_JDOC3 = "/**\n *\n * @author Nikita Koksharov\n */"

CLASS_JDOC: dict[str, str] = {
    "RedissonMultiLock": (
        "/**\n"
        " * 联锁（MultiLock）：将多个独立 {@link RLock} 组合为一把逻辑锁统一加锁/解锁。\n"
        " * <p>按固定顺序尝试获取全部子锁；任一失败则释放已持有的锁。\n"
        " * 支持可重入、租约续期与 {@link java.util.concurrent.locks.Condition}。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */"
    ),
    "RedissonMultimap": (
        "/**\n"
        " * {@link RMultimap} 抽象基类：每个键映射到 Redis Set 子键。\n"
        " * <p>通过 {@code prefix} 后缀命名子集合，封装 getAll/putAll/removeAll 等通用逻辑。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " * @param <K> 键类型\n"
        " * @param <V> 值类型\n"
        " */"
    ),
    "RedissonObject": (
        "/**\n"
        " * Redisson 分布式对象基类，实现 {@link RObject} 通用能力。\n"
        " * <p>管理命令执行器、编解码器、键名/hash-tag、Pub/Sub 监听器\n"
        " * 与对象追踪（OBJECT ENCODING）等基础设施。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */"
    ),
    "RedissonPermitExpirableSemaphore": (
        "/**\n"
        " * 可过期许可的分布式信号量 {@link RPermitExpirableSemaphore}。\n"
        " * <p>每次 acquire 返回带 TTL 的 permitId；到期自动释放，\n"
        " * 也可通过 permitId 显式 release 或续期。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */"
    ),
    "RedissonPriorityQueue": (
        "/**\n"
        " * 基于 Redis List 的二分有序 {@link RPriorityQueue} 实现。\n"
        " * <p>按 Comparator 维护升序，支持 offer/poll/peek 及阻塞变体。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " * @param <V> 元素类型\n"
        " */"
    ),
    "RedissonRateLimiter": (
        "/**\n"
        " * 分布式令牌桶限流器 {@link RRateLimiter}。\n"
        " * <p>基于 Redis + Lua 脚本实现 OVERALL/PER_CLIENT 速率配置，\n"
        " * 支持 tryAcquire 与异步/响应式变体。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */"
    ),
    "RedissonReactive": (
        "/**\n"
        " * Reactor 风格 Redisson 客户端 {@link RedissonReactiveClient}。\n"
        " * <p>在 Redis/Valkey 之上提供全部响应式分布式对象 factory 方法；\n"
        " * 委托 {@link CommandReactiveExecutor} 执行非阻塞命令。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */"
    ),
    "RedissonRemoteService": (
        "/**\n"
        " * 分布式远程服务 {@link RRemoteService} 实现。\n"
        " * <p>基于 Redis 队列在 JVM 间调用接口方法；支持多 worker、\n"
        " * 异步回调与 RxJava/Reactor 代理。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */"
    ),
    "RedissonRx": (
        "/**\n"
        " * RxJava3 风格 Redisson 客户端 {@link RedissonRxClient}。\n"
        " * <p>在 Redis/Valkey 之上提供全部 Rx 分布式对象 factory 方法；\n"
        " * 委托 {@link CommandRxExecutor} 执行异步命令。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */"
    ),
    "RedissonScoredSortedSet": (
        "/**\n"
        " * Redis 有序集合 {@link RScoredSortedSet}（ZSET）实现。\n"
        " * <p>封装 ZADD/ZREM、ZRANGE/ZREVRANGE、ZSCAN、ZUNION/ZINTER\n"
        " * 及按分数/排名范围查询、MapReduce 等操作。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " * @param <V> 成员类型\n"
        " */"
    ),
    "RedissonSearch": (
        "/**\n"
        " * RediSearch 模块 {@link RSearch} 客户端实现。\n"
        " * <p>封装索引创建/删除、文档增删改、FT.SEARCH/FT.AGGREGATE\n"
        " * 及拼写建议等全文检索命令。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */"
    ),
    "RedissonSemaphore": (
        "/**\n"
        " * 分布式信号量 {@link RSemaphore}，对应 {@link java.util.concurrent.Semaphore}。\n"
        " * <p>非公平模式，获取顺序不可预测；基于 Redis + Pub/Sub 唤醒等待线程。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */"
    ),
    "RedissonSet": (
        "/**\n"
        " * 分布式 Set {@link RSet}，对应 {@link java.util.Set}。\n"
        " * <p>封装 SADD/SREM、SMEMBERS、SINTER/SUNION/SDIFF、SSCAN\n"
        " * 及随机成员、MapReduce 等操作。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " * @param <V> 元素类型\n"
        " */"
    ),
    "RedissonSetCache": (
        "/**\n"
        " * 带逐元素 TTL 的 Set 缓存 {@link RSetCache}。\n"
        " * <p>通过 {@link RSetCache#add(Object, long, TimeUnit)} 为每个成员设置过期；\n"
        " * 读操作触发惰性过期检查，{@link org.redisson.eviction.EvictionScheduler}\n"
        " * 定期异步清理。若无需逐元素 TTL 建议使用 {@link org.redisson.api.RSet}。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " * @param <V> 元素类型\n"
        " */"
    ),
    "RedissonSetMultimap": (
        "/**\n"
        " * Set 型多值映射 {@link RSetMultimap}：每个键对应一个 {@link RSet}。\n"
        " * <p>底层 Redis Set 子键由 {@link RedissonMultimap} 前缀规则命名。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " * @param <K> 键类型\n"
        " * @param <V> 值类型\n"
        " */"
    ),
}

JDOC_REPLACE: dict[str, tuple[str, str]] = {
    "Groups multiple independent locks": (
        "/**\n"
        " * Groups multiple independent locks and manages them as one lock.\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
        CLASS_JDOC["RedissonMultiLock"],
    ),
    "Base Redisson object": (
        "/**\n"
        " * Base Redisson object\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
        CLASS_JDOC["RedissonObject"],
    ),
    "Main infrastructure class allows to get access": (
        "/**\n"
        " * Main infrastructure class allows to get access\n"
        " * to all Redisson objects on top of Redis server.\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
        "",  # filled per-class below
    ),
    "Distributed and concurrent implementation of {@link java.util.Set}": (
        "/**\n"
        " * Distributed and concurrent implementation of {@link java.util.Set}\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " * @param <V> value\n"
        " */",
        CLASS_JDOC["RedissonSet"],
    ),
    "Distributed and concurrent implementation of {@link java.util.concurrent.Semaphore}": (
        "/**\n"
        " * Distributed and concurrent implementation of {@link java.util.concurrent.Semaphore}.\n"
        " * <p>\n"
        " * Works in non-fair mode. Therefore order of acquiring is unpredictable.\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
        CLASS_JDOC["RedissonSemaphore"],
    ),
    "Set-based cache with ability to set TTL": (
        "/**\n"
        " * <p>Set-based cache with ability to set TTL for each entry via\n"
        " * {@link RSetCache#add(Object, long, TimeUnit)} method.\n"
        " * </p>\n"
        " *\n"
        " * <p>Current Redis implementation doesn't have set entry eviction functionality.\n"
        " * Thus values are checked for TTL expiration during any value read operation.\n"
        " * If entry expired then it doesn't returns and clean task runs asynchronous.\n"
        " * Clean task deletes removes 100 expired entries at once.\n"
        " * In addition there is {@link org.redisson.eviction.EvictionScheduler}. This scheduler\n"
        " * deletes expired entries in time interval between 5 seconds to 2 hours.</p>\n"
        " *\n"
        " * <p>If eviction is not required then it's better to use {@link org.redisson.api.RSet}.</p>\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " * @param <V> value\n"
        " */",
        CLASS_JDOC["RedissonSetCache"],
    ),
}

FIELD_CN: list[tuple[str, str]] = [
    (
        "    protected CommandAsyncExecutor commandExecutor;",
        "    /** 异步 Redis 命令执行器。 */\n    protected CommandAsyncExecutor commandExecutor;",
    ),
    (
        "    protected String name;",
        "    /** Redis 键名（含 hash-tag）。 */\n    protected String name;",
    ),
    (
        "    protected final Codec codec;",
        "    /** 值序列化编解码器。 */\n    protected final Codec codec;",
    ),
    (
        "    final String prefix;",
        "    /** 多值映射子键前缀。 */\n    final String prefix;",
    ),
    (
        "    RedissonClient redisson;",
        "    /** 关联 Redisson 客户端（MapReduce 等）。 */\n    RedissonClient redisson;",
    ),
    (
        "    final RedissonClient redisson;",
        "    /** 关联 Redisson 客户端（MapReduce 等）。 */\n    final RedissonClient redisson;",
    ),
    (
        "    final EvictionScheduler evictionScheduler;",
        "    /** 过期条目异步淘汰调度器。 */\n    final EvictionScheduler evictionScheduler;",
    ),
    (
        "    final String publishCommand;",
        "    /** 过期事件 Pub/Sub 发布命令名。 */\n    final String publishCommand;",
    ),
    (
        "    private final SemaphorePubSub semaphorePubSub;",
        "    /** 信号量等待/唤醒 Pub/Sub 通道。 */\n    private final SemaphorePubSub semaphorePubSub;",
    ),
    (
        "    private final String channelName;",
        "    /** 信号量 Pub/Sub 通道名。 */\n    private final String channelName;",
    ),
    (
        "    private final String timeoutName;",
        "    /** 存储 permit 过期时间的 Redis 键后缀。 */\n    private final String timeoutName;",
    ),
    (
        "    private final WriteBehindService writeBehindService;",
        "    /** Write-Behind 批写服务。 */\n    private final WriteBehindService writeBehindService;",
    ),
    (
        "    private final EvictionScheduler evictionScheduler;",
        "    /** 淘汰调度器。 */\n    private final EvictionScheduler evictionScheduler;",
    ),
    (
        "    private final CommandReactiveExecutor commandExecutor;",
        "    /** 响应式命令执行器。 */\n    private final CommandReactiveExecutor commandExecutor;",
    ),
    (
        "    private final CommandRxExecutor commandExecutor;",
        "    /** RxJava 命令执行器。 */\n    private final CommandRxExecutor commandExecutor;",
    ),
    (
        "    private final ConnectionManager connectionManager;",
        "    /** Redis 连接管理器。 */\n    private final ConnectionManager connectionManager;",
    ),
    (
        "    private final List<RLock> locks = new ArrayList<>();",
        "    /** 参与联锁的子锁列表。 */\n    private final List<RLock> locks = new ArrayList<>();",
    ),
    (
        "        private final long newLeaseTime;",
        "        /** 续期后的新租约时长。 */\n        private final long newLeaseTime;",
    ),
    (
        "        private final List<RLock> acquiredLocks;",
        "        /** 已成功获取的子锁列表。 */\n        private final List<RLock> acquiredLocks;",
    ),
    (
        "        private int failedLocksLimit;",
        "        /** 允许连续获取失败的最大子锁次数。 */\n        private int failedLocksLimit;",
    ),
    (
        "        private V value;",
        "        /** 二分查找命中的元素值。 */\n        private V value;",
    ),
    (
        "        private int index = -1;",
        "        /** 二分查找命中的列表索引，-1 表示未找到。 */\n        private int index = -1;",
    ),
    (
        "        RFuture<String> future;",
        "        /** 远程方法调用结果 Future。 */\n        RFuture<String> future;",
    ),
    (
        "        final AtomicInteger freeWorkers;",
        "        /** 当前可用 worker 计数。 */\n        final AtomicInteger freeWorkers;",
    ),
]

METHOD_CN: dict[str, str] = {
    "lock": "阻塞获取锁直至成功。",
    "lockAsync": "异步阻塞获取锁。",
    "lockInterruptibly": "可中断地阻塞获取锁。",
    "tryLock": "尝试获取锁，支持等待超时与租约。",
    "tryLockAsync": "异步尝试获取锁。",
    "unlock": "释放当前线程持有的锁。",
    "unlockAsync": "异步释放锁。",
    "forceUnlock": "强制释放锁（不校验持有者）。",
    "forceUnlockAsync": "异步强制释放锁。",
    "isLocked": "锁是否已被任意线程持有。",
    "isHeldByCurrentThread": "当前线程是否持有该锁。",
    "isHeldByThread": "指定线程是否持有该锁。",
    "getHoldCount": "当前线程重入持有次数。",
    "remainTimeToLive": "返回锁剩余租约毫秒数。",
    "newCondition": "创建与锁绑定的 Condition。",
    "acquire": "获取一个信号量许可（阻塞）。",
    "acquireAsync": "异步获取信号量许可。",
    "acquirePermit": "获取可过期 permit 并返回 permitId。",
    "releasePermit": "通过 permitId 释放许可。",
    "updatePermits": "更新 permit 过期时间。",
    "tryAcquire": "尝试获取许可（限流/信号量）。",
    "tryAcquireAsync": "异步尝试获取许可。",
    "release": "释放信号量/限流许可。",
    "releaseAsync": "异步释放许可。",
    "drainPermits": "一次性获取全部可用许可。",
    "availablePermits": "返回当前可用许可数。",
    "setPermits": "设置信号量总许可数。",
    "trySetRate": "配置令牌桶速率（仅首次生效）。",
    "trySetRateAsync": "异步配置令牌桶速率。",
    "getConfig": "返回限流器当前 Rate 配置。",
    "getCommandExecutor": "返回命令执行器。",
    "getServiceManager": "返回连接服务管理器。",
    "prefixName": "构造带 hash-tag 的键前缀。",
    "suffixName": "为键名追加后缀段。",
    "getRawName": "返回未经处理的 Redis 键名。",
    "getName": "返回对象名称。",
    "setName": "设置对象 Redis 键名。",
    "rename": "重命名 Redis 键。",
    "delete": "删除 Redis 键。",
    "unlink": "异步删除 Redis 键。",
    "touch": "更新键最后访问时间。",
    "countExists": "统计键是否存在。",
    "isExists": "键是否存在。",
    "addListener": "注册对象变更监听器。",
    "removeListener": "移除监听器。",
    "getListeners": "返回已注册监听器 ID 集合。",
    "getObjectEncoding": "查询 Redis 内部编码类型。",
    "getObjectSize": "查询键占用内存字节数。",
    "getObjectIdleTime": "查询键空闲时间。",
    "getObjectRefcount": "查询键引用计数。",
    "compareAndSet": "CAS 原子更新值。",
    "getAndSet": "设置新值并返回旧值。",
    "trySet": "仅当键不存在时写入。",
    "add": "添加元素/成员。",
    "addAll": "批量添加元素。",
    "remove": "移除元素。",
    "removeAll": "批量移除元素。",
    "removeRandom": "随机移除并返回一个成员。",
    "removeRandomAsync": "异步随机移除成员。",
    "random": "随机返回一个成员（不移除）。",
    "randomAsync": "异步随机返回成员。",
    "contains": "是否包含指定元素。",
    "containsAll": "是否包含全部给定元素。",
    "retainAll": "仅保留与给定集合的交集。",
    "readAll": "一次性读取全部成员。",
    "readAllAsync": "异步读取全部成员。",
    "iterator": "返回成员迭代器。",
    "iteratorAsync": "返回异步成员迭代器。",
    "scanIterator": "SSCAN 增量扫描迭代器。",
    "scanIteratorAsync": "异步 SSCAN 迭代器。",
    "size": "返回元素数量。",
    "sizeAsync": "异步返回元素数量。",
    "isEmpty": "集合是否为空。",
    "clear": "清空全部元素。",
    "union": "计算并存储集合并集。",
    "intersection": "计算并存储集合交集。",
    "difference": "计算并存储集合差集。",
    "move": "将成员移动到另一 Set。",
    "copy": "复制 Set 到目标键。",
    "mapReduce": "创建 MapReduce 任务入口。",
    "offer": "按优先级插入元素。",
    "poll": "移除并返回队首/最高优先级元素。",
    "peek": "查看但不移除最高优先级元素。",
    "comparator": "返回元素比较器。",
    "addScore": "为 ZSet 成员设置分数。",
    "addScoreAsync": "异步设置成员分数。",
    "addAsync": "异步添加 ZSet 成员。",
    "removeAsync": "异步移除 ZSet 成员。",
    "rank": "返回成员升序排名（0 起）。",
    "revRank": "返回成员降序排名。",
    "score": "返回成员分数。",
    "valueRange": "按排名范围返回成员。",
    "valueRangeReversed": "按排名降序范围返回成员。",
    "entryRange": "按排名范围返回成员与分数。",
    "pollFirst": "移除并返回分数最小的成员。",
    "pollLast": "移除并返回分数最大的成员。",
    "count": "统计分数区间内成员数。",
    "removeRangeByRank": "按排名范围删除成员。",
    "removeRangeByScore": "按分数范围删除成员。",
    "unionAsync": "异步计算 ZSet 并集。",
    "intersectionAsync": "异步计算 ZSet 交集。",
    "get": "按键获取 Set 视图或值。",
    "getAll": "返回全部键及其 Set 值。",
    "put": "向指定键的 Set 添加值。",
    "putAll": "批量写入多键多值。",
    "replaceValues": "替换指定键的全部值。",
    "removeAllValues": "移除指定键的全部值。",
    "containsKey": "是否包含指定键。",
    "containsValue": "是否包含指定值。",
    "containsEntry": "是否包含键值对。",
    "keySet": "返回键集合视图。",
    "values": "返回值集合视图。",
    "entries": "返回键值对集合视图。",
    "sizeAll": "返回全部子 Set 元素总数。",
    "getKeys": "返回全部映射键。",
    "expireKey": "为指定键的子 Set 设置过期。",
    "register": "注册远程服务接口实现。",
    "registerAsync": "异步注册远程服务。",
    "deregister": "注销远程服务。",
    "getRemoteProxy": "获取远程服务调用代理。",
    "createIndex": "创建 RediSearch 索引。",
    "dropIndex": "删除索引。",
    "search": "执行 FT.SEARCH 查询。",
    "aggregate": "执行 FT.AGGREGATE 聚合。",
    "addDocument": "向索引添加文档。",
    "updateDocument": "更新索引文档。",
    "deleteDocument": "从索引删除文档。",
    "info": "返回索引/模块信息。",
    "getDict": "获取拼写校正词典。",
    "setDict": "设置拼写校正词典。",
    "getSynonym": "获取同义词组。",
    "addSynonym": "添加同义词。",
    "shutdown": "关闭客户端。",
    "isShutdown": "客户端是否已关闭。",
    "createTransaction": "创建 Redis 事务。",
    "createBatch": "创建命令批处理。",
    "getLock": "获取分布式锁。",
    "getMultiLock": "获取联锁。",
    "getFairLock": "获取公平锁。",
    "getReadWriteLock": "获取读写锁。",
    "getSemaphore": "获取分布式信号量。",
    "getPermitExpirableSemaphore": "获取可过期许可信号量。",
    "getRateLimiter": "获取限流器。",
    "getSet": "获取 Set 对象。",
    "getSetCache": "获取带 TTL 的 Set 缓存。",
    "getScoredSortedSet": "获取有序集合。",
    "getPriorityQueue": "获取优先级队列。",
    "getSetMultimap": "获取 Set 多值映射。",
    "getSearch": "获取 RediSearch 客户端。",
    "getRemoteService": "获取远程服务执行器。",
    "reactive": "返回 Reactor 风格客户端。",
    "rxJava": "返回 RxJava3 风格客户端。",
}

METHOD_RE = re.compile(
    r"(?P<prefix>\n    )(?P<override>@Override\n    )?"
    r"(?P<sig>(?:public|protected|private)\s+(?:static\s+)?[\w<>,\?\[\]\s]+\s+(\w+)\s*\([^)]*\)\s*(?:throws\s+[\w.\s,]+)?\{)"
)


def has_comment_before(text: str, pos: int) -> bool:
    window = text[max(0, pos - 120) : pos]
    return "/**" in window or "//" in window.split("\n")[-1]


def method_comment(name: str, cls: str) -> str:
    if name in METHOD_CN:
        return METHOD_CN[name]
    if name.startswith("get") and len(name) > 3:
        suffix = name[3:]
        if suffix.endswith("Async"):
            base = suffix[:-5]
            return f"异步获取 {base} 或执行 {base} 操作。"
        if cls in ("RedissonReactive", "RedissonRx"):
            if suffix.startswith("Stream"):
                return f"获取响应式 Stream 对象 {name[3:]}。"
            return f"获取 {{@link R{suffix}}} 响应式分布式对象。"
        if cls == "RedissonRemoteService":
            return f"远程服务 {suffix} 相关操作。"
        return f"获取 {suffix}。"
    if name.startswith("is") and len(name) > 2:
        return f"是否{ name[2:] }。"
    if name.startswith("set") and len(name) > 3:
        return f"设置{ name[3:] }。"
    if name.endswith("Async"):
        base = name[:-5]
        return f"异步执行 {base}。"
    if name.startswith("add"):
        return f"{name}：添加操作。"
    if name.startswith("remove"):
        return f"{name}：移除操作。"
    if cls == "RedissonMultiLock":
        return f"联锁 {name} 操作。"
    if cls == "RedissonObject":
        return f"Redisson 对象基类 {name} 方法。"
    if cls == "RedissonMultimap":
        return f"Multimap {name} 操作。"
    if cls == "RedissonSetMultimap":
        return f"SetMultimap {name} 操作。"
    if cls == "RedissonSet":
        return f"Set {name} 操作。"
    if cls == "RedissonSetCache":
        return f"SetCache {name} 操作。"
    if cls == "RedissonScoredSortedSet":
        return f"ZSet {name} 操作。"
    if cls == "RedissonSemaphore":
        return f"信号量 {name} 操作。"
    if cls == "RedissonPermitExpirableSemaphore":
        return f"可过期信号量 {name} 操作。"
    if cls == "RedissonRateLimiter":
        return f"限流器 {name} 操作。"
    if cls == "RedissonPriorityQueue":
        return f"优先级队列 {name} 操作。"
    if cls == "RedissonSearch":
        return f"RediSearch {name} 操作。"
    if cls == "RedissonRemoteService":
        return f"远程服务 {name} 操作。"
    if cls in ("RedissonReactive", "RedissonRx"):
        return f"响应式客户端 {name} 方法。"
    return f"{name} 方法实现。"


def class_javadoc_replacements(text: str, cls: str) -> list[tuple[str, str]]:
    reps: list[tuple[str, str]] = []
    for key, (old, new) in JDOC_REPLACE.items():
        if key in text and old in text:
            if key == "Main infrastructure class allows to get access":
                new = CLASS_JDOC.get(cls, CLASS_JDOC["RedissonReactive"])
            reps.append((old, new))
            return reps
    if cls in CLASS_JDOC:
        for old in (_EMPTY_JDOC, _EMPTY_JDOC2, _EMPTY_JDOC3):
            if old in text:
                reps.append((old, CLASS_JDOC[cls]))
                break
        if not reps and cls == "RedissonPriorityQueue":
            old = "/**\n *\n * @author Nikita Koksharov\n *\n * @param <V> value type\n */"
            if old in text:
                reps.append((old, CLASS_JDOC[cls]))
        if not reps and cls == "RedissonScoredSortedSet":
            old = "/**\n * \n * @author Nikita Koksharov\n *\n * @param <V> value type\n */"
            if old in text:
                reps.append((old, CLASS_JDOC[cls]))
        if not reps and cls in ("RedissonMultimap", "RedissonSetMultimap"):
            old = "/**\n * @author Nikita Koksharov\n *\n * @param <K> key\n * @param <V> value\n */"
            if old in text:
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

    for old, new in class_javadoc_replacements(text, cls):
        add(old, new)
    for old, new in collect_auto_replacements(text, cls):
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
        '"""Chinese annotation replacements for Redisson 4.7.0 wave-57b core [15:30]."""',
        "from __future__ import annotations",
        "",
        "W57B_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {",
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
        print(f"  {rel}: {len(reps)} replacements")


if __name__ == "__main__":
    emit()
