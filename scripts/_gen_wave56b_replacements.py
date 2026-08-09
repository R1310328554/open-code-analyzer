#!/usr/bin/env python3
"""Generate wave56b_replacements_redisson.py for core + tomcat session [15:30]."""
from __future__ import annotations

import re
from pathlib import Path

ROOT = Path("/workspace")
ORIG = ROOT / "redisson/redisson-4.7.0/original"
OUT = ROOT / "scripts/wave56b_replacements_redisson.py"
FILES = [
    ln.strip()
    for ln in Path("/tmp/re56b.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]

_EMPTY_JDOC = "/**\n * \n * @author Nikita Koksharov\n *\n */"
_EMPTY_JDOC2 = "/**\n *\n * @author Nikita Koksharov\n *\n */"

CLASS_JDOC: dict[str, str] = {
    "RedissonSession": (
        "/**\n"
        " * 基于 Redis {@link RMap} 的 Apache Tomcat {@link org.apache.catalina.Session} 实现。\n"
        " * <p>支持 {@link RedissonSessionManager.ReadMode#REDIS} 按需从 Redis 读取属性\n"
        " * 与 {@link RedissonSessionManager.UpdateMode#AFTER_REQUEST} 请求末批量写回两种模式；\n"
        " * 通过 {@link RTopic} 在集群节点间同步 Session 变更。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */"
    ),
    "RedissonSessionManager": (
        "/**\n"
        " * Apache Tomcat {@link org.apache.catalina.Manager}：以 Redis/Valkey 持久化 HTTP Session。\n"
        " * <p>支持 YAML 配置、ReadMode（REDIS/MEMORY）、UpdateMode（DEFAULT/AFTER_REQUEST）、\n"
        " * Session 事件广播与跨节点属性同步 Topic。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */"
    ),
    "BaseRedissonList": (
        "/**\n"
        " * Redis {@code LIST} 的抽象基类，为 {@link RedissonList} 等提供通用实现。\n"
        " * <p>封装 LPUSH/RPUSH、LRANGE、LREM、BLPOP 等列表命令及监听器、MapReduce 入口。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " * @param <V> 元素类型\n"
        " */"
    ),
    "MapCacheNativeWrapper": (
        "/**\n"
        " * 将 {@link RMapCacheNative} 适配为标准 {@link RMapCache} API 的包装器。\n"
        " * <p>Lease 相关方法与部分 max-idle/容量接口在 Native 模式下不支持，调用时抛出\n"
        " * {@link UnsupportedOperationException}。\n"
        " */"
    ),
    "Redisson": (
        "/**\n"
        " * Redisson 主入口：在 Redis/Valkey 之上创建并访问全部分布式对象。\n"
        " * <p>管理连接池、命令执行器、淘汰调度、Write-Behind 与 Live Object 等基础设施；\n"
        " * 实现 {@link RedissonClient} 的全部 factory 方法。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */"
    ),
    "RedissonArray": (
        "/**\n"
        " * Redis {@code ARRAY} 类型（Redis 8+）的 {@link RArray} 实现。\n"
        " * <p>支持按索引读写、范围查询、追加与批量操作。\n"
        " *\n"
        " * @param <V> 元素类型\n"
        " * @author lamnt2008\n"
        " * @author Nikita Koksharov\n"
        " */"
    ),
    "RedissonBatch": (
        "/**\n"
        " * {@link RBatch} 实现：将多条 Redis 命令聚合为单次网络往返（管道/批处理）。\n"
        " * <p>通过 {@link CommandBatchService} 延迟执行并在 {@link #execute()} 时一次性提交。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */"
    ),
    "RedissonBitSet": (
        "/**\n"
        " * 基于 Redis 字符串位操作的 {@link RBitSet} 实现。\n"
        " * <p>封装 GETBIT/SETBIT、BITCOUNT、BITOP 及有符号/无符号位域读写。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */"
    ),
    "RedissonBloomFilter": (
        "/**\n"
        " * 基于 Highway 128 位哈希的 {@link RBloomFilter} 布隆过滤器实现。\n"
        " * <p>支持预期元素数与误判率配置，底层使用 Redis 位图存储。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " * @param <T> 元素类型\n"
        " */"
    ),
    "RedissonBoundedBlockingQueue": (
        "/**\n"
        " * 有界分布式阻塞队列 {@link RBoundedBlockingQueue}（已废弃）。\n"
        " * <p>通过 {@link RedissonQueueSemaphore} 限制容量，委托 {@link RedissonBlockingQueue}\n"
        " * 实现阻塞入队/出队。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */"
    ),
    "RedissonBucket": (
        "/**\n"
        " * Redis 字符串键 {@link RBucket} 实现：单值读写、CAS、过期与对象追踪。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " * @param <V> 值类型\n"
        " */"
    ),
}

JDOC_REPLACE: dict[str, tuple[str, str]] = {
    "Main infrastructure class allows to get access": (
        "/**\n"
        " * Main infrastructure class allows to get access\n"
        " * to all Redisson objects on top of Redis server.\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
        CLASS_JDOC["Redisson"],
    ),
    "Redisson Session object for Apache Tomcat": (
        "/**\n"
        " * Redisson Session object for Apache Tomcat\n"
        " * \n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
        CLASS_JDOC["RedissonSession"],
    ),
    "Redisson Session Manager for Apache Tomcat": (
        "/**\n"
        " * Redisson Session Manager for Apache Tomcat\n"
        " * \n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
        CLASS_JDOC["RedissonSessionManager"],
    ),
    "Base list implementation": (
        "/**\n"
        " * Base list implementation\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " * @param <V> the type of elements held in this collection\n"
        " */",
        CLASS_JDOC["BaseRedissonList"],
    ),
    "Array object implementation.": (
        "/**\n"
        " * Array object implementation.\n"
        " *\n"
        " * @param <V> value type\n"
        " *\n"
        " * @author lamnt2008\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
        CLASS_JDOC["RedissonArray"],
    ),
    "Bloom filter based on Highway 128-bit hash.": (
        "/**\n"
        " * Bloom filter based on Highway 128-bit hash.\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " * @param <T> type of object\n"
        " */",
        CLASS_JDOC["RedissonBloomFilter"],
    ),
    "Distributed and concurrent implementation of bounded": (
        "/**\n"
        " * <p>Distributed and concurrent implementation of bounded {@link java.util.concurrent.BlockingQueue}.\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
        CLASS_JDOC["RedissonBoundedBlockingQueue"],
    ),
    "Create sync/async Redisson instance with default config": (
        "    /**\n"
        "     * Create sync/async Redisson instance with default config\n"
        "     *\n"
        "     * @return Redisson instance\n"
        "     */",
        "    /**\n"
        "     * 使用默认单机配置（127.0.0.1:6379）创建同步/异步 Redisson 客户端。\n"
        "     *\n"
        "     * @return Redisson 客户端实例\n"
        "     */",
    ),
    "Create sync/async Redisson instance with provided config": (
        "    /**\n"
        "     * Create sync/async Redisson instance with provided config\n"
        "     *\n"
        "     * @param config for Redisson\n"
        "     * @return Redisson instance\n"
        "     */",
        "    /**\n"
        "     * 根据给定 {@link Config} 创建同步/异步 Redisson 客户端。\n"
        "     *\n"
        "     * @param config Redisson 配置\n"
        "     * @return Redisson 客户端实例\n"
        "     */",
    ),
}

FIELD_CN: list[tuple[str, str]] = [
    (
        "public class MapCacheNativeWrapper",
        "/**\n * 将 {@link RMapCacheNative} 适配为标准 {@link RMapCache} API 的包装器。\n */\npublic class MapCacheNativeWrapper",
    ),
    (
        "    private final RMapCacheNative<K, V> cache;",
        "    /** 底层 Native MapCache 实例。 */\n    private final RMapCacheNative<K, V> cache;",
    ),
    (
        "    public enum ReadMode {REDIS, MEMORY}",
        "    /** Session 属性读取模式：REDIS 按需加载，MEMORY 本地缓存。 */\n    public enum ReadMode {REDIS, MEMORY}",
    ),
    (
        "    public enum UpdateMode {DEFAULT, AFTER_REQUEST}",
        "    /** Session 写回模式：DEFAULT 即时写入，AFTER_REQUEST 请求结束批量写回。 */\n    public enum UpdateMode {DEFAULT, AFTER_REQUEST}",
    ),
    (
        "    protected RedissonClient redisson;",
        "    /** 底层 Redisson 客户端。 */\n    protected RedissonClient redisson;",
    ),
    (
        "    private ReadMode readMode = ReadMode.REDIS;",
        "    /** 当前 Session 读取模式。 */\n    private ReadMode readMode = ReadMode.REDIS;",
    ),
    (
        "    private UpdateMode updateMode = UpdateMode.DEFAULT;",
        "    /** 当前 Session 写回模式。 */\n    private UpdateMode updateMode = UpdateMode.DEFAULT;",
    ),
    (
        "    private final String nodeId = UUID.randomUUID().toString();",
        "    /** 本 Tomcat 节点唯一标识，用于集群 Topic 消息来源。 */\n    private final String nodeId = UUID.randomUUID().toString();",
    ),
    (
        "    private final RedissonSessionManager redissonManager;",
        "    /** 所属 Session 管理器。 */\n    private final RedissonSessionManager redissonManager;",
    ),
    (
        "    private RMap<String, Object> map;",
        "    /** 持久化 Session 属性的 Redis Map。 */\n    private RMap<String, Object> map;",
    ),
    (
        "    private final RTopic topic;",
        "    /** 集群 Session 变更广播 Topic。 */\n    private final RTopic topic;",
    ),
    (
        "    private final ReadMode readMode;",
        "    /** Session 属性读取模式。 */\n    private final ReadMode readMode;",
    ),
    (
        "    private final UpdateMode updateMode;",
        "    /** Session 属性写回模式。 */\n    private final UpdateMode updateMode;",
    ),
    (
        "    private final AtomicInteger usages = new AtomicInteger();",
        "    /** 当前请求对 Session 的并发使用计数。 */\n    private final AtomicInteger usages = new AtomicInteger();",
    ),
    (
        "    volatile long size;",
        "    /** 布隆过滤器预期元素容量。 */\n    volatile long size;",
    ),
    (
        "    volatile int hashIterations;",
        "    /** 布隆过滤器哈希迭代次数。 */\n    volatile int hashIterations;",
    ),
    (
        "    String configName;",
        "    /** 存储布隆过滤器配置的 Redis 键后缀。 */\n    String configName;",
    ),
    (
        "    private RedissonClient redisson;",
        "    /** 关联 Redisson 客户端（MapReduce 等扩展用）。 */\n    private RedissonClient redisson;",
    ),
    (
        "    private final RedissonBlockingQueue<V> blockingQueue;",
        "    /** 底层阻塞队列实现。 */\n    private final RedissonBlockingQueue<V> blockingQueue;",
    ),
    (
        "    private final RedissonQueueSemaphore semaphore;",
        "    /** 控制队列容量的分布式信号量。 */\n    private final RedissonQueueSemaphore semaphore;",
    ),
    (
        "    private final EvictionScheduler evictionScheduler;",
        "    /** 淘汰调度器（MapCache 等对象使用）。 */\n    private final EvictionScheduler evictionScheduler;",
    ),
    (
        "    private final CommandBatchService executorService;",
        "    /** 批处理命令执行服务。 */\n    private final CommandBatchService executorService;",
    ),
]

METHOD_CN: dict[str, str] = {
    "create": "使用配置创建 Redisson 客户端实例。",
    "createEmptySession": "创建无 ID 的空 Session（由 Tomcat 分配 ID）。",
    "createSession": "创建或加载指定 ID 的 Session。",
    "findSession": "按 ID 查找 Session；不存在时返回 null。",
    "remove": "从管理器移除 Session 并清理 Redis 数据。",
    "add": "注册 Session 到本地管理器。",
    "load": "Tomcat 生命周期：加载 Session 管理器。",
    "unload": "Tomcat 生命周期：卸载 Session 管理器。",
    "startInternal": "启动管理器：创建 Redisson 客户端并订阅集群 Topic。",
    "stopInternal": "停止管理器：取消订阅并关闭 Redisson 客户端。",
    "buildClient": "根据 configPath 或 config 构建 {@link RedissonClient}。",
    "shutdownRedisson": "关闭 Redisson 客户端并释放连接。",
    "store": "将 Session 变更持久化到 Redis 并广播更新。",
    "getMap": "获取 Session 属性 Map 或通用 Redis Map。",
    "getTopic": "返回 Session 集群同步 Topic。",
    "getTomcatSessionKeyName": "构造 Session 在 Redis 中的完整键名。",
    "getNotifiedNodes": "返回已处理 Session 销毁通知的节点集合。",
    "getRedisson": "返回底层 Redisson 客户端。",
    "getNodeId": "返回本 Tomcat 节点 ID。",
    "getAttribute": "读取 Session 属性；REDIS 模式下按需从 Redis 加载。",
    "getAttributeNames": "返回全部属性名枚举。",
    "getValueNames": "返回用户属性名数组（不含内部元数据键）。",
    "setAttribute": "设置 Session 属性并标记变更。",
    "delete": "从 Redis 删除 Session 并广播销毁事件。",
    "access": "更新 Session 最后访问时间。",
    "endAccess": "结束访问并触发过期检查或写回。",
    "startUsage": "递增使用计数，防止并发覆盖。",
    "endUsage": "递减使用计数。",
    "expireSession": "触发 Session 过期逻辑。",
    "setMaxInactiveInterval": "设置 Session 最大非活动间隔（秒）。",
    "setPrincipal": "设置认证主体并持久化。",
    "setAuthType": "设置认证类型并持久化。",
    "setValid": "设置 Session 有效标志。",
    "setNew": "设置 Session 是否为新创建。",
    "setCreationTime": "设置 Session 创建时间戳。",
    "loadFromMap": "从 Redis Map 加载 Session 元数据与属性。",
    "saveToMap": "将 Session 状态写入 Redis Map。",
    "fastPut": "快速写入属性到 Redis Map。",
    "createPutAllMessage": "构造批量属性更新集群消息。",
    "superAccess": "委托 {@link StandardSession#access()}。",
    "superEndAccess": "委托 {@link StandardSession#endAccess()}。",
    "superRemoveAttributeInternal": "委托父类移除属性逻辑。",
    "superRemove": "委托父类 remove Session。",
    "superAdd": "委托父类 add Session。",
    "getIdleTimeInternal": "返回自上次访问以来的空闲毫秒数。",
    "isValidInternal": "检查 Session 内部有效状态。",
    "execute": "提交批处理并返回各命令结果。",
    "executeAsync": "异步提交批处理命令。",
    "discard": "丢弃批处理队列中的命令。",
    "size": "返回列表/集合/过滤器当前元素数量。",
    "sizeAsync": "异步返回元素数量。",
    "isEmpty": "是否为空。",
    "contains": "是否包含指定元素。",
    "add": "添加元素。",
    "remove": "移除元素。",
    "clear": "清空全部元素。",
    "get": "按索引或键读取元素/值。",
    "set": "按索引或键写入元素/值。",
    "compareAndSet": "CAS 原子更新值。",
    "getAndSet": "设置新值并返回旧值。",
    "trySet": "仅当键不存在时写入。",
    "delete": "删除键或 Session。",
    "tryInit": "初始化布隆过滤器容量与误判率。",
    "contains": "布隆过滤器是否可能包含元素。",
    "count": "统计可能包含的元素数量。",
    "add": "向布隆过滤器添加元素。",
    "addAll": "批量添加元素。",
    "getExpectedInsertions": "返回预期插入元素数。",
    "getFalseProbability": "返回目标误判率。",
    "get": "返回底层 Native MapCache 实例。",
    "mapReduce": "创建 MapReduce 任务入口。",
    "offer": "尝试入队（有界队列可能阻塞或失败）。",
    "poll": "出队队首元素。",
    "take": "阻塞直到可取到元素。",
    "put": "写入键值（Map/Cache）。",
    "putIfAbsent": "仅当键不存在时写入。",
    "remove": "移除键或元素。",
    "containsKey": "是否包含指定键。",
    "containsValue": "是否包含指定值。",
    "keySet": "返回键集合视图。",
    "values": "返回值集合视图。",
    "entrySet": "返回条目集合视图。",
    "readAllMap": "一次性读取全部 Map 条目。",
    "readAllEntrySet": "一次性读取全部条目。",
    "readAllKeySet": "一次性读取全部键。",
    "readAllValues": "一次性读取全部值。",
    "getEvictionScheduler": "返回淘汰调度器。",
    "getCommandExecutor": "返回命令异步执行器。",
    "getServiceManager": "返回连接服务管理器。",
    "shutdown": "关闭 Redisson 客户端。",
    "isShutdown": "客户端是否已关闭。",
    "isShuttingDown": "客户端是否正在关闭。",
    "rxJava": "返回 RxJava3 风格客户端。",
    "reactive": "返回 Reactor 风格客户端。",
    "getNodesGroup": "返回 Redis 节点管理 API。",
    "getLock": "获取分布式锁。",
    "getMultiLock": "获取联锁（MultiLock）。",
    "getRedLock": "获取红锁（RedLock）。",
    "getFairLock": "获取公平锁。",
    "getReadWriteLock": "获取读写锁。",
    "createTransaction": "创建 Redis 事务。",
    "createBatch": "创建命令批处理。",
    "getId": "返回对象名称/ID。",
    "rename": "重命名 Redis 键。",
    "countExists": "统计存在的键数量。",
    "touch": "更新键的最后访问时间。",
    "unlink": "异步删除键。",
    "copy": "复制键到目标名称。",
    "move": "将键移动到指定数据库。",
    "migrate": "将键迁移到另一 Redis 实例。",
    "dump": "序列化键值。",
    "restore": "从 DUMP 数据恢复键。",
    "getKeysByPattern": "按模式匹配返回键集合。",
    "getKeys": "返回全部键（慎用）。",
    "getKeysStream": "流式迭代键空间。",
    "getKeysStreamByPattern": "按模式流式迭代键。",
    "getLiveObjectService": "返回 Live Object 服务。",
    "getRemoteService": "返回远程服务执行器。",
    "getExecutorService": "返回分布式任务执行器。",
    "getScheduledExecutorService": "返回分布式定时任务执行器。",
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
            return f"异步获取 {base} 对象或执行 {base} 操作。"
        if cls == "Redisson":
            return f"获取 {{@link R{suffix}}} 分布式对象。"
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
    if "WithLease" in name or "Lease" in name:
        return f"{name}：Native Map 不支持 Lease 语义，调用抛出异常。"
    if cls == "MapCacheNativeWrapper":
        return f"委托底层 Native MapCache 执行 {name}。"
    if cls == "RedissonBatch":
        return f"批处理模式下获取 {name[3:] if name.startswith('get') else name} 异步对象。"
    if cls in ("BaseRedissonList", "RedissonArray"):
        return f"列表/数组 {name} 操作。"
    if cls == "RedissonBitSet":
        return f"位图 {name} 操作。"
    if cls == "RedissonBucket":
        return f"Bucket {name} 操作。"
    if cls == "RedissonBloomFilter":
        return f"布隆过滤器 {name} 操作。"
    if cls == "RedissonBoundedBlockingQueue":
        return f"有界阻塞队列 {name} 操作。"
    if cls in ("RedissonSession", "RedissonSessionManager"):
        return f"Tomcat Session {name} 操作。"
    if cls == "Redisson":
        return f"Redisson 客户端 {name} 方法。"
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
    for key, (old, new) in JDOC_REPLACE.items():
        if key in text and old in text:
            reps.append((old, new))
            return reps
    if cls in CLASS_JDOC:
        for old in (_EMPTY_JDOC, _EMPTY_JDOC2):
            if old in text:
                reps.append((old, CLASS_JDOC[cls]))
                break
        if not reps and cls == "RedissonBitSet":
            old = "/**\n * \n * @author Nikita Koksharov\n *\n */"
            if old in text:
                reps.append((old, CLASS_JDOC[cls]))
        if not reps and cls == "RedissonBatch":
            old = "/**\n *\n *\n * @author Nikita Koksharov\n *\n */"
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
        '"""Chinese annotation replacements for Redisson 4.7.0 wave-56b core+tomcat [15:30]."""',
        "from __future__ import annotations",
        "",
        "W56B_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {",
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
