#!/usr/bin/env python3
"""Generate wave57a_replacements_redisson.py for core map/lock/geo [0:15]."""
from __future__ import annotations

import re
from pathlib import Path

ROOT = Path("/workspace")
ORIG = ROOT / "redisson/redisson-4.7.0/original"
OUT = ROOT / "scripts/wave57a_replacements_redisson.py"
FILES = [
    ln.strip()
    for ln in Path("/tmp/re57a.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]

_EMPTY_JDOC = "/**\n * \n * @author Nikita Koksharov\n *\n */"
_EMPTY_JDOC2 = "/**\n *\n * @author Nikita Koksharov\n *\n */"
_EMPTY_JDOC3 = "/**\n * \n * @author Nikita Koksharov\n *\n * @param <V> value type\n */"

CLASS_JDOC: dict[str, str] = {
    "RedissonCircularBuffer": (
        "/**\n"
        " * Redis {@code ARRAY} 环形缓冲区 {@link RCircularBuffer} 实现（Redis 8+）。\n"
        " * <p>固定容量循环覆盖写入，支持 ARPUSH/ARLTRIM 等 ARRAY 命令。\n"
        " *\n"
        " * @param <V> 元素类型\n"
        " * @author Nikita Koksharov\n"
        " */"
    ),
    "RedissonDelayedQueue": (
        "/**\n"
        " * 延迟队列 {@link RDelayedQueue} 实现（已废弃）。\n"
        " * <p>通过 ZSET 调度到期元素并转移到目标队列；后台 {@link QueueTransferTask} 轮询迁移。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " * @param <V> 元素类型\n"
        " */"
    ),
    "RedissonExecutorService": (
        "/**\n"
        " * 分布式任务执行器 {@link RExecutorService} 实现。\n"
        " * <p>将 {@link Runnable}/{@link Callable} 序列化后提交到 Redis 队列，由远程 Worker 执行；\n"
        " * 支持任务取消、结果回调与 {@link org.redisson.api.annotation.RRemoteService} 远程调用。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */"
    ),
    "RedissonFasterMultiLock": (
        "/**\n"
        " * 高性能联锁 {@link RFasterMultiLock}：同一 Redis Hash 键下批量锁定多个 field。\n"
        " * <p>lock/unlock 仅当全部 field 均成功时才返回成功；相比 {@link RedissonMultiLock} 减少键数量与网络往返。\n"
        " * <p>group 参数应使用最小粒度键名（如 {@code class_100} 而非 {@code class}）。\n"
        " *\n"
        " * @author lyrric\n"
        " */"
    ),
    "RedissonGeo": (
        "/**\n"
        " * Redis 地理空间 {@link RGeo} 实现。\n"
        " * <p>封装 GEOADD/GEORADIUS/GEODIST 等命令，支持按坐标与半径查询成员。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " * @param <V> 成员标识类型\n"
        " */"
    ),
    "RedissonJsonBucket": (
        "/**\n"
        " * JSON 字符串桶 {@link RJsonBucket} 实现。\n"
        " * <p>基于 Redis JSON 模块读写结构化文档，支持路径查询与部分更新。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */"
    ),
    "RedissonKeys": (
        "/**\n"
        " * Redis 键空间管理 {@link RKeys} 实现。\n"
        " * <p>封装 KEYS/SCAN、UNLINK、DUMP/RESTORE、MIGRATE 及跨库 MOVE 等键级操作。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */"
    ),
    "RedissonListMultimap": (
        "/**\n"
        " * 列表型 Multimap {@link RListMultimap} 实现：一个键对应多个列表元素。\n"
        " * <p>底层为 Redis LIST，支持按 key 追加/移除/读取元素集合。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " * @param <K> 外层键类型\n"
        " * @param <V> 列表元素类型\n"
        " */"
    ),
    "RedissonListMultimapValues": (
        "/**\n"
        " * {@link RedissonListMultimap} 某 key 下元素列表的只读/可变视图。\n"
        " * <p>实现 {@link java.util.List}，变更会同步到底层 Multimap。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " * @param <V> 元素类型\n"
        " */"
    ),
    "RedissonLiveObjectService": (
        "/**\n"
        " * Live Object 服务 {@link RLiveObjectService} 实现。\n"
        " * <p>通过代理将标注 {@link org.redisson.api.annotation.REntity} 的 Java 对象\n"
        " * 映射为 Redis Hash 字段，支持分布式引用、索引与 CRUD。\n"
        " */"
    ),
    "RedissonLocalCachedMap": (
        "/**\n"
        " * 带本地缓存的分布式 Map {@link RLocalCachedMap} 实现。\n"
        " * <p>在 {@link RedissonMap} 之上维护 JVM 本地 LRU/LFU 缓存，并通过 Pub/Sub\n"
        " * 或 Redis 6+ Client Tracking 使多节点缓存失效保持一致。\n"
        " *\n"
        " * @param <K> 键类型\n"
        " * @param <V> 值类型\n"
        " */"
    ),
    "RedissonLock": (
        "/**\n"
        " * 分布式可重入锁 {@link RLock} 实现（非公平）。\n"
        " * <p>客户端断开连接时锁自动释放；基于 Lua 脚本与 Pub/Sub 唤醒等待线程。\n"
        " * <p>默认启用看门狗自动续期（{@link org.redisson.config.Config#getLockWatchdogTimeout()}）。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */"
    ),
    "RedissonMap": (
        "/**\n"
        " * 分布式并发 Map {@link RMap} 实现。\n"
        " * <p>封装 HGET/HSET、HMGET、HSCAN 等 Hash 命令；可选 Write-Behind、\n"
        " * MapReduce 与监听器；实现 {@link java.util.concurrent.ConcurrentMap}。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " * @param <K> 键类型\n"
        " * @param <V> 值类型\n"
        " */"
    ),
    "RedissonMapCache": (
        "/**\n"
        " * 带逐条目 TTL 的 Map 缓存 {@link RMapCache} 实现。\n"
        " * <p>通过 {@link #put(Object, Object, long, TimeUnit)} 等为每条记录设置过期；\n"
        " * 读操作触发惰性过期检查，{@link EvictionScheduler} 定期批量清理（每次最多 100 条）。\n"
        " * <p>若无需逐条 TTL，优先使用 {@link RedissonMap}。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " * @param <K> 键类型\n"
        " * @param <V> 值类型\n"
        " */"
    ),
    "RedissonMapCacheNative": (
        "/**\n"
        " * 基于 Redis 原生过期语义的 {@link RMapCacheNative} 实现。\n"
        " * <p>条目 TTL 由 Redis 服务器维护，无需 {@link EvictionScheduler} 定时扫描；\n"
        " * 部分 Lease/max-idle 接口在 Native 模式下不可用。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " * @param <K> 键类型\n"
        " * @param <V> 值类型\n"
        " */"
    ),
}

PREPEND_JDOC: dict[str, tuple[str, str]] = {
    "RedissonLiveObjectService": (
        "public class RedissonLiveObjectService implements RLiveObjectService {",
        "/**\n"
        " * Live Object 服务 {@link RLiveObjectService} 实现。\n"
        " * <p>通过代理将标注 {@link org.redisson.api.annotation.REntity} 的 Java 对象\n"
        " * 映射为 Redis Hash 字段，支持分布式引用、索引与 CRUD。\n"
        " */\n"
        "public class RedissonLiveObjectService implements RLiveObjectService {",
    ),
    "RedissonLocalCachedMap": (
        "public class RedissonLocalCachedMap<K, V> extends RedissonMap<K, V> implements RLocalCachedMap<K, V> {",
        "/**\n"
        " * 带本地缓存的分布式 Map {@link RLocalCachedMap} 实现。\n"
        " * <p>在 {@link RedissonMap} 之上维护 JVM 本地 LRU/LFU 缓存，并通过 Pub/Sub\n"
        " * 或 Redis 6+ Client Tracking 使多节点缓存失效保持一致。\n"
        " *\n"
        " * @param <K> 键类型\n"
        " * @param <V> 值类型\n"
        " */\n"
        "public class RedissonLocalCachedMap<K, V> extends RedissonMap<K, V> implements RLocalCachedMap<K, V> {",
    ),
}

JDOC_REPLACE: dict[str, tuple[str, str]] = {
    "Distributed implementation of {@link java.util.concurrent.locks.Lock}": (
        "/**\n"
        " * Distributed implementation of {@link java.util.concurrent.locks.Lock}\n"
        " * Implements reentrant lock.<br>\n"
        " * Lock will be removed automatically if client disconnects.\n"
        " * <p>\n"
        " * Implements a <b>non-fair</b> locking so doesn't guarantees an acquire order.\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
        CLASS_JDOC["RedissonLock"],
    ),
    "Distributed and concurrent implementation of {@link java.util.concurrent.ConcurrentMap}": (
        "/**\n"
        " * Distributed and concurrent implementation of {@link java.util.concurrent.ConcurrentMap}\n"
        " * and {@link java.util.Map}\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " * @param <K> key\n"
        " * @param <V> value\n"
        " */",
        CLASS_JDOC["RedissonMap"],
    ),
    "Map-based cache with ability to set TTL for each entry": (
        "/**\n"
        " * <p>Map-based cache with ability to set TTL for each entry via\n"
        " * {@link #put(Object, Object, long, TimeUnit)} or {@link #putIfAbsent(Object, Object, long, TimeUnit)} methods.\n"
        " * And therefore has an complex lua-scripts inside.</p>\n"
        " *\n"
        " * <p>Current redis implementation doesnt have map entry eviction functionality.\n"
        " * Thus entries are checked for TTL expiration during any key/value/entry read operation.\n"
        " * If key/value/entry expired then it doesn't returns and clean task runs asynchronous.\n"
        " * Clean task deletes removes 100 expired entries at once.\n"
        " * In addition there is {@link EvictionScheduler}. This scheduler\n"
        " * deletes expired entries in time interval between 5 seconds to 2 hours.</p>\n"
        " *\n"
        " * <p>If eviction is not required then it's better to use {@link RedissonMap} object.</p>\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " * @param <K> key\n"
        " * @param <V> value\n"
        " */",
        CLASS_JDOC["RedissonMapCache"],
    ),
    "Map-based cache with ability to set TTL per entry.": (
        "/**\n"
        " * Map-based cache with ability to set TTL per entry.\n"
        " * Uses Redis native commands for entry expiration and not a scheduled eviction task.\n"
        "\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " * @param <K> key\n"
        " * @param <V> value\n"
        " */",
        CLASS_JDOC["RedissonMapCacheNative"],
    ),
    "RedissonFasterMultiLock.": (
        "/**\n"
        " * RedissonFasterMultiLock.<br/>\n"
        " * All lock, unlock, lockAsync unlockAsync methods only success when all values locked succeed. <br/>\n"
        " * Example:  <br/>\n"
        " * there is a class, id is 100, and three students in class, Jack(id:001),Mary(id:002) <br/>\n"
        " * <ul>\n"
        " * <li>current thread id : 1\n"
        " * <li>ServiceManager id: 71b96ce8-2746......\n"
        " * <li>current time stamp: 1727422868000\n"
        " * </ul>\n"
        " * when {@code redissonBatchLock.lock(\"class_100\",Arrays.asList(\"Jack_001\",\"Mary_002\")} <br/>\n"
        " * It will be saved In redis like this:\n"
        " * <PRE>\n"
        " * -----------------------------------------------------------------------\n"
        " * | redis type: hash                                                    |\n"
        " * | redis Key: class_100                                                |\n"
        " * -----------------------------------------------------------------------\n"
        " * | field                                           | value             |\n"
        " * -----------------------------------------------------------------------\n"
        " * | Jack_001                                        | 71b96ce8-2746:1   |\n"
        " * | Mary_002                                        | 71b96ce8-2746:1   |\n"
        " * | Jack_001:71b96ce8-2746:1:expire_time            | 1,727,422,898,000 |\n"
        " * | Jack_001:71b96ce8-2746:1:lock_count             | 1                 |\n"
        " * | Mary_002:71b96ce8-2746:1:expire_time            | 1,727,422,898,000 |\n"
        " * | Mary_002:71b96ce8-2746:1:lock_count             | 1                 |\n"
        " * -----------------------------------------------------------------------\n"
        " * </PRE>\n"
        " * <strong>Attention: the value of <code>group</code> should be `smallest`, in our example above ,\n"
        " * <code>group</code> should be  'class_100' not just 'class' </strong><br/>\n"
        " * Of course the values `Jack_001`,`Mary_002` will be encoded and hashed.\n"
        " *\n"
        " * @author lyrric\n"
        " *\n"
        " */",
        CLASS_JDOC["RedissonFasterMultiLock"],
    ),
    "Geospatial items holder": (
        "/**\n"
        " * Geospatial items holder\n"
        " * \n"
        " * @author Nikita Koksharov\n"
        "\n"
        " * @param <V> value\n"
        " */",
        CLASS_JDOC["RedissonGeo"],
    ),
    "Json data holder": (
        "/**\n"
        " * Json data holder\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
        CLASS_JDOC["RedissonJsonBucket"],
    ),
    "List based Multimap Cache values holder": (
        "/**\n"
        " * List based Multimap Cache values holder\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " * @param <V> the type of elements held in this collection\n"
        " */",
        CLASS_JDOC["RedissonListMultimapValues"],
    ),
}

FIELD_CN: list[tuple[str, str]] = [
    (
        "    protected long internalLockLeaseTime;",
        "    /** 锁看门狗续期间隔（毫秒）。 */\n    protected long internalLockLeaseTime;",
    ),
    (
        "    protected final LockPubSub pubSub;",
        "    /** 锁释放 Pub/Sub 通道。 */\n    protected final LockPubSub pubSub;",
    ),
    (
        "    final CommandAsyncExecutor commandExecutor;",
        "    /** 异步命令执行器。 */\n    final CommandAsyncExecutor commandExecutor;",
    ),
    (
        "    private static final Logger LOGGER = LoggerFactory.getLogger(RedissonLock.class);",
        "    /** 日志记录器。 */\n    private static final Logger LOGGER = LoggerFactory.getLogger(RedissonLock.class);",
    ),
    (
        "    private final Logger log = LoggerFactory.getLogger(getClass());",
        "    /** 日志记录器。 */\n    private final Logger log = LoggerFactory.getLogger(getClass());",
    ),
    (
        "    final RedissonClient redisson;",
        "    /** 关联 Redisson 客户端。 */\n    final RedissonClient redisson;",
    ),
    (
        "    final MapOptions<K, V> options;",
        "    /** Map 读写/缓存选项。 */\n    final MapOptions<K, V> options;",
    ),
    (
        "    final WriteBehindService writeBehindService;",
        "    /** Write-Behind 异步写回服务。 */\n    final WriteBehindService writeBehindService;",
    ),
    (
        "    final MapWriteBehindTask writeBehindTask;",
        "    /** 当前 Map 的 Write-Behind 刷盘任务。 */\n    final MapWriteBehindTask writeBehindTask;",
    ),
    (
        "    private final String settingsName;",
        "    /** 环形缓冲区容量配置的 Redis 键。 */\n    private final String settingsName;",
    ),
    (
        "    private final String channelName;",
        "    /** 延迟队列到期通知 Pub/Sub 通道。 */\n    private final String channelName;",
    ),
    (
        "    private final String queueName;",
        "    /** 目标队列 Redis 键名。 */\n    private final String queueName;",
    ),
    (
        "    private final String timeoutSetName;",
        "    /** 延迟元素 ZSET 键名。 */\n    private final String timeoutSetName;",
    ),
    (
        "    protected RedissonClient redisson;",
        "    /** 底层 Redisson 客户端。 */\n    protected RedissonClient redisson;",
    ),
    (
        "    private final EvictionScheduler evictionScheduler;",
        "    /** MapCache 过期条目淘汰调度器。 */\n    private final EvictionScheduler evictionScheduler;",
    ),
    (
        "    private final LocalCachedMapOptions<K, V> options;",
        "    /** 本地缓存策略选项。 */\n    private final LocalCachedMapOptions<K, V> options;",
    ),
    (
        "    private final Cache<K, V> cache;",
        "    /** JVM 本地 LRU/LFU 缓存实例。 */\n    private final Cache<K, V> cache;",
    ),
    (
        "    private final CacheKeyMap cacheKeyMap;",
        "    /** 本地缓存键到 Redis 键的映射。 */\n    private final CacheKeyMap cacheKeyMap;",
    ),
    (
        "    private final ConcurrentMap<String, RFuture<?>> invalidationFuture;",
        "    /** 进行中的缓存失效异步操作。 */\n    private final ConcurrentMap<String, RFuture<?>> invalidationFuture;",
    ),
]

METHOD_CN: dict[str, str] = {
    "lock": "获取锁；阻塞直到成功（启用看门狗时自动续期）。",
    "lockAsync": "异步获取锁。",
    "lockInterruptibly": "可中断地获取锁。",
    "tryLock": "尝试获取锁，支持等待超时与租约时间。",
    "tryLockAsync": "异步尝试获取锁。",
    "unlock": "释放当前线程持有的锁。",
    "unlockAsync": "异步释放锁。",
    "forceUnlock": "强制释放锁（不校验持有者）。",
    "forceUnlockAsync": "异步强制释放锁。",
    "isLocked": "锁是否已被任意线程持有。",
    "isHeldByCurrentThread": "当前线程是否持有该锁。",
    "isHeldByThread": "指定线程是否持有该锁。",
    "getHoldCount": "当前线程重入持有次数。",
    "remainTimeToLive": "锁剩余存活时间（毫秒）。",
    "remainTimeToLiveAsync": "异步返回锁剩余存活时间。",
    "getChannelName": "返回锁 Pub/Sub 通道名。",
    "put": "写入键值。",
    "putAsync": "异步写入键值。",
    "putIfAbsent": "仅当键不存在时写入。",
    "putIfAbsentAsync": "异步 putIfAbsent。",
    "get": "读取键对应值。",
    "getAsync": "异步读取值。",
    "remove": "移除键或元素。",
    "removeAsync": "异步移除。",
    "containsKey": "是否包含指定键。",
    "containsValue": "是否包含指定值。",
    "size": "返回元素/条目数量。",
    "sizeAsync": "异步返回数量。",
    "isEmpty": "是否为空。",
    "clear": "清空全部条目。",
    "clearAsync": "异步清空。",
    "readAllMap": "一次性读取全部 Map 条目。",
    "readAllMapAsync": "异步一次性读取全部 Map 条目。",
    "readAllEntrySet": "一次性读取全部 Entry。",
    "readAllKeySet": "一次性读取全部键。",
    "readAllValues": "一次性读取全部值。",
    "keySet": "返回键集合视图。",
    "values": "返回值集合视图。",
    "entrySet": "返回条目集合视图。",
    "mapReduce": "创建 MapReduce 任务入口。",
    "addListener": "注册 Map 变更监听器。",
    "removeListener": "移除监听器。",
    "addPutListener": "注册 put 事件监听器。",
    "addRemoveListener": "注册 remove 事件监听器。",
    "addIncrListener": "注册自增事件监听器。",
    "fastPut": "快速写入（不返回旧值）。",
    "fastPutAsync": "异步 fastPut。",
    "fastRemove": "快速删除多个键。",
    "getAll": "批量读取多个键的值。",
    "getAllAsync": "异步批量读取。",
    "putAll": "批量写入键值对。",
    "putAllAsync": "异步批量写入。",
    "replace": "替换已有键的值。",
    "merge": "按 remapping 函数合并值。",
    "compute": "若存在则计算新值。",
    "computeIfAbsent": "键不存在时计算并写入。",
    "computeIfPresent": "键存在时计算新值。",
    "addAndGet": "原子自增并返回新值。",
    "addAndGetAsync": "异步原子自增。",
    "addGeo": "添加地理空间成员及坐标。",
    "addGeoAsync": "异步添加地理坐标。",
    "removeGeo": "移除地理空间成员。",
    "radius": "按圆心与半径查询成员。",
    "radiusAsync": "异步半径查询。",
    "radiusWithDistance": "半径查询并返回距离。",
    "radiusWithDistanceAsync": "异步半径查询（含距离）。",
    "geoSearch": "按 GEOSEARCH 条件查询。",
    "geoSearchAsync": "异步 GEOSEARCH 查询。",
    "geoSearchStore": "GEOSEARCH 并将结果写入目标键。",
    "geoSearchStoreAsync": "异步 GEOSEARCHSTORE。",
    "trySetCapacity": "尝试设置环形缓冲区容量（仅首次）。",
    "trySetCapacityAsync": "异步 trySetCapacity。",
    "setCapacity": "设置环形缓冲区容量。",
    "capacity": "返回当前容量。",
    "remainingCapacity": "返回剩余可写容量。",
    "add": "追加元素（满则覆盖最旧）。",
    "addAsync": "异步追加元素。",
    "offer": "尝试入队。",
    "poll": "出队队首元素。",
    "take": "阻塞直到可取到元素。",
    "offerAsync": "异步入队。",
    "pollAsync": "异步出队。",
    "takeAsync": "异步阻塞出队。",
    "offerAsync": "异步入队。",
    "delete": "删除 Redis 键。",
    "deleteAsync": "异步删除键。",
    "rename": "重命名键。",
    "countExists": "统计存在的键数量。",
    "getKeysByPattern": "按模式匹配返回键集合。",
    "getKeys": "返回全部键（慎用）。",
    "getKeysStream": "流式迭代键空间。",
    "getKeysStreamByPattern": "按模式流式迭代键。",
    "unlink": "异步删除键（UNLINK）。",
    "touch": "更新键的最后访问时间。",
    "dump": "序列化键值（DUMP）。",
    "restore": "从 DUMP 数据恢复键。",
    "migrate": "将键迁移到另一 Redis 实例。",
    "move": "将键移动到指定数据库。",
    "copy": "复制键到目标名称。",
    "scanIterator": "SCAN 迭代器。",
    "scanIteratorAsync": "异步 SCAN 迭代器。",
    "register": "注册 Live Object 实体类。",
    "unregisterClass": "取消注册实体类。",
    "get": "按 ID 获取 Live Object 代理。",
    "delete": "删除 Live Object 实例。",
    "attach": "将已有对象附加为 Live Object。",
    "detach": "分离 Live Object 代理。",
    "persist": "持久化 Live Object 变更。",
    "getLocalCachedMap": "获取带本地缓存的 Map 视图。",
    "preloadCache": "预加载本地缓存。",
    "clearLocalCache": "清空本地缓存。",
    "getCachedMap": "返回本地缓存 Map 视图。",
    "getCacheKeyMap": "返回缓存键映射。",
    "getOptions": "返回本地缓存选项。",
    "getEvictionScheduler": "返回淘汰调度器。",
    "execute": "提交分布式任务。",
    "executeAsync": "异步提交任务。",
    "submit": "提交 Callable 并返回 Future。",
    "submitAsync": "异步提交 Callable。",
    "registerWorkers": "注册远程 Worker 执行器。",
    "shutdown": "关闭执行器。",
    "isShutdown": "是否已关闭。",
    "getTaskCount": "返回队列中任务数量。",
    "cancelTask": "取消指定任务。",
    "getTaskId": "返回任务 ID。",
    "offer": "向延迟队列添加带延迟的元素。",
    "offerAsync": "异步添加延迟元素。",
    "remove": "从延迟队列移除元素。",
    "destroy": "销毁延迟队列后台任务。",
    "getQueueName": "返回目标队列名。",
    "getDelayedQueue": "返回关联的延迟队列。",
    "getAll": "返回 Multimap 全部键值对。",
    "getAllAsync": "异步返回全部键值对。",
    "containsKey": "Multimap 是否包含指定键。",
    "containsEntry": "是否包含指定键值对。",
    "containsValue": "是否包含指定值。",
    "replaceValues": "替换 key 下全部值列表。",
    "removeAll": "移除 key 下全部值。",
    "removeAllAsync": "异步 removeAll。",
    "putAll": "批量写入 Multimap 条目。",
    "keySize": "返回外层键数量。",
    "getValues": "返回指定 key 的值列表视图。",
    "getValuesAsync": "异步 getValues。",
    "set": "JSON 路径写入。",
    "setAsync": "异步 JSON 写入。",
    "get": "JSON 路径读取。",
    "getAsync": "异步 JSON 读取。",
    "delete": "删除 JSON 路径或键。",
    "deleteAsync": "异步 JSON 删除。",
    "arrayAppend": "JSON 数组追加元素。",
    "arrayInsert": "JSON 数组插入元素。",
    "strLen": "JSON 字符串长度。",
    "objLen": "JSON 对象字段数。",
    "objKeys": "JSON 对象全部键。",
    "toggle": "JSON 布尔取反。",
    "numIncrBy": "JSON 数值自增。",
    "clearExpire": "清除条目 TTL。",
    "clearExpireAsync": "异步清除 TTL。",
    "remainTimeToLive": "条目剩余 TTL（毫秒）。",
    "remainTimeToLiveAsync": "异步返回条目 TTL。",
    "updateEntryExpiration": "更新条目过期时间。",
    "updateEntryExpirationAsync": "异步更新条目过期。",
    "putWithLease": "写入并设置 Lease（Native 模式可能不支持）。",
    "getWithLeaseAsync": "异步 getWithLease。",
    "loadAll": "批量加载 Map 条目到本地缓存。",
    "loadAllAsync": "异步 loadAll。",
    "getCachedMap": "返回本地缓存 backing map。",
    "getLocalCacheStats": "返回本地缓存统计信息。",
    "getLock": "对指定 field 加锁（FasterMultiLock）。",
    "unlock": "释放指定 field 联锁。",
    "lockAll": "锁定全部 field。",
    "unlockAll": "释放全部 field。",
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
        if cls == "RedissonKeys":
            return f"键空间 {suffix} 操作。"
        if cls.startswith("RedissonMap"):
            return f"Map {suffix} 操作。"
        return f"获取 {suffix}。"
    if name.startswith("is") and len(name) > 2:
        return f"是否{ name[2:] }。"
    if name.startswith("set") and len(name) > 3:
        return f"设置{ name[3:] }。"
    if name.endswith("Async"):
        base = name[:-5]
        return f"异步执行 {base}。"
    if cls == "RedissonLock":
        return f"分布式锁 {name} 操作。"
    if cls == "RedissonFasterMultiLock":
        return f"联锁 {name} 操作。"
    if cls in ("RedissonMap", "RedissonMapCache", "RedissonMapCacheNative", "RedissonLocalCachedMap"):
        return f"Map/Cache {name} 操作。"
    if cls == "RedissonGeo":
        return f"地理空间 {name} 操作。"
    if cls == "RedissonJsonBucket":
        return f"JSON Bucket {name} 操作。"
    if cls == "RedissonKeys":
        return f"键管理 {name} 操作。"
    if cls == "RedissonCircularBuffer":
        return f"环形缓冲区 {name} 操作。"
    if cls == "RedissonDelayedQueue":
        return f"延迟队列 {name} 操作。"
    if cls == "RedissonExecutorService":
        return f"分布式执行器 {name} 操作。"
    if cls in ("RedissonListMultimap", "RedissonListMultimapValues"):
        return f"List Multimap {name} 操作。"
    if cls == "RedissonLiveObjectService":
        return f"Live Object {name} 操作。"
    return f"{name} 方法实现。"


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


def class_javadoc_replacements(text: str, cls: str) -> list[tuple[str, str]]:
    reps: list[tuple[str, str]] = []
    if cls in PREPEND_JDOC:
        old, new = PREPEND_JDOC[cls]
        if old in text:
            reps.append((old, new))
            return reps
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
            old = "/**\n * @author Nikita Koksharov\n *\n * @param <K> key\n * @param <V> value\n */"
            if old in text:
                reps.append((old, CLASS_JDOC[cls]))
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
        '"""Chinese annotation replacements for Redisson 4.7.0 wave-57a core map/lock [0:15]."""',
        "from __future__ import annotations",
        "",
        "W57A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {",
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
        cn = sum(len(re.findall(r"[\u4e00-\u9fff]", new)) for _, new in reps)
        print(f"  {rel}: {len(reps)} replacements, ~{cn} CJK chars in new text")


if __name__ == "__main__":
    emit()
