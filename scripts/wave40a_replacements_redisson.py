"""Chinese annotation replacements for Redisson 4.7.0 wave-40a core [0:15]."""
from __future__ import annotations

_R = "redisson/src/main/java/org/redisson/"
_EMPTY_JDOC = "/**\n * \n * @author Nikita Koksharov\n *\n */"

W40A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

# --- RedissonMultiMapIterator ---

_multi_map_iter = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n * @param <K> key type\n * @param <V> value type\n * @param <M> map type\n */",
        "/**\n"
        " * {@link RedissonMultimap} 的条目迭代器抽象基类。\n"
        " * <p>先 {@code HSCAN} 遍历 multimap 的外层键，再对每个键对应的集合执行内层扫描；\n"
        " * {@link #next()} 返回 {@code Map.Entry<K,V>} 形式的键值对。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " * @param <K> 键类型\n"
        " * @param <V> 值类型\n"
        " * @param <M> 迭代返回的映射条目类型\n"
        " */",
    ),
]
W40A_REPLACEMENTS[f"{_R}RedissonMultiMapIterator.java"] = _multi_map_iter
W40A_REPLACEMENTS["RedissonMultiMapIterator.java"] = _multi_map_iter

# --- RedissonMultimapCache ---

_multimap_cache = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n * @param <K> key type\n */",
        "/**\n"
        " * 带过期能力的 {@link RedissonMultimap} 缓存辅助类。\n"
        " * <p>通过 ZSET 记录各键的 TTL 截止时间，配合 {@link EvictionScheduler} 定期清理；\n"
        " * 支持单键过期、整体过期及清除过期设置。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " * @param <K> 键类型\n"
        " */",
    ),
]
W40A_REPLACEMENTS[f"{_R}RedissonMultimapCache.java"] = _multimap_cache
W40A_REPLACEMENTS["RedissonMultimapCache.java"] = _multimap_cache

# --- RedissonMultimapCacheNative ---

_multimap_cache_native = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n * @param <K> key type\n */",
        "/**\n"
        " * 基于 Redis 7+ {@code HPEXPIRE} 的原生 multimap 过期辅助类。\n"
        " * <p>对 hash 字段与对应 value 集合分别设置 TTL，无需额外 ZSET 调度。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " * @param <K> 键类型\n"
        " */",
    ),
]
W40A_REPLACEMENTS[f"{_R}RedissonMultimapCacheNative.java"] = _multimap_cache_native
W40A_REPLACEMENTS["RedissonMultimapCacheNative.java"] = _multimap_cache_native

# --- RedissonNode ---

_redisson_node = [
    (
        _EMPTY_JDOC,
        "/**\n"
        " * Redisson 分布式 Worker 节点。\n"
        " * <p>可独立启动或通过 YAML 配置运行，注册 MapReduce 与自定义\n"
        " * {@link org.redisson.api.RExecutorService} Worker 以执行远程任务。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    /**\n     * Shutdown Redisson node instance\n     * \n     */",
        "    /**\n     * 关闭 Redisson 节点实例。\n     */",
    ),
    (
        "                // skip",
        "                // 忽略异常",
    ),
    (
        "                // skip",
        "                // 忽略异常",
    ),
    (
        "                // skip",
        "                // 忽略异常",
    ),
    (
        "    /**\n     * Start Redisson node instance\n     */",
        "    /**\n     * 启动 Redisson 节点实例。\n     */",
    ),
    (
        "    /**\n     * Create Redisson node instance with provided config\n     *\n     * @param config of RedissonNode\n     * @return RedissonNode instance\n     */",
        "    /**\n"
        "     * 根据配置创建 Redisson 节点实例。\n"
        "     *\n"
        "     * @param config RedissonNode 配置\n"
        "     * @return RedissonNode 实例\n"
        "     */",
    ),
    (
        "    /**\n     * Create Redisson node instance with provided config\n     *\n     * @param config of RedissonNode\n     * @return RedissonNode instance\n     */\n    public static RedissonNode create(RedissonNodeFileConfig config) {",
        "    /**\n"
        "     * 根据 YAML 文件配置创建 Redisson 节点实例。\n"
        "     *\n"
        "     * @param config RedissonNode 文件配置\n"
        "     * @return RedissonNode 实例\n"
        "     */\n"
        "    public static RedissonNode create(RedissonNodeFileConfig config) {",
    ),
    (
        "    /**\n     * Create Redisson node instance with provided config and Redisson instance\n     *\n     * @param config of RedissonNode\n     * @param redisson instance\n     * @return RedissonNode instance\n     */",
        "    /**\n"
        "     * 根据配置与已有 {@link RedissonClient} 创建 Redisson 节点实例。\n"
        "     *\n"
        "     * @param config RedissonNode 配置\n"
        "     * @param redisson 已存在的 Redisson 客户端；为 {@code null} 时由节点自行创建\n"
        "     * @return RedissonNode 实例\n"
        "     */",
    ),
]
W40A_REPLACEMENTS[f"{_R}RedissonNode.java"] = _redisson_node
W40A_REPLACEMENTS["RedissonNode.java"] = _redisson_node

# --- RedissonNonReentrantFairLock ---

_non_reentrant_fair_lock = [
    (
        "/**\n * Distributed implementation of {@link java.util.concurrent.locks.Lock} that is\n * <b>fair</b> and <b>non-reentrant</b>. Acquisition order is FIFO across all\n * Redisson instances; a thread that already holds the lock and attempts to\n * acquire it again throws {@link IllegalMonitorStateException}, both for\n * {@code lock()} and {@code tryLock()}. Other threads contend for the lock\n * via the fair queue as usual.\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * {@link java.util.concurrent.locks.Lock} 的分布式<b>公平</b><b>不可重入</b>锁实现。\n"
        " * <p>所有 Redisson 实例间按 FIFO 公平排队；已持有锁的线程再次加锁时，\n"
        " * 无论 {@code lock()} 还是 {@code tryLock()} 均抛出 {@link IllegalMonitorStateException}；\n"
        " * 其他线程通过公平等待队列正常竞争。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "                    // remove stale waiters",
        "                    // 移除超时的等待者",
    ),
    (
        "                    // remove stale waiters",
        "                    // 移除超时的等待者",
    ),
    (
        "                    // first acquire path",
        "                    // 首次获取锁路径",
    ),
    (
        "                    // first acquire path",
        "                    // 首次获取锁路径",
    ),
    (
        "                        // decrease timeouts for all waiting in the queue",
        "                        // 为队列中所有等待者缩短超时时间",
    ),
    (
        "                        // decrease timeouts for all waiting in the queue",
        "                        // 为队列中所有等待者缩短超时时间",
    ),
    (
        "                    // non-reentrant: same-thread reacquire is rejected",
        "                    // 不可重入：同线程再次获取被拒绝",
    ),
    (
        "                    // non-reentrant: same-thread reacquire is rejected",
        "                    // 不可重入：同线程再次获取被拒绝",
    ),
    (
        "                    // contention path: already in queue?",
        "                    // 竞争路径：是否已在等待队列中？",
    ),
    (
        "                    // enqueue",
        "                    // 入队",
    ),
]
W40A_REPLACEMENTS[f"{_R}RedissonNonReentrantFairLock.java"] = _non_reentrant_fair_lock
W40A_REPLACEMENTS["RedissonNonReentrantFairLock.java"] = _non_reentrant_fair_lock

# --- RedissonNonReentrantLock ---

_non_reentrant_lock = [
    (
        "/**\n * Distributed implementation of {@link java.util.concurrent.locks.Lock} that is\n * <b>non-reentrant</b>. A thread that already holds the lock and attempts to\n * acquire it again throws {@link IllegalMonitorStateException}, both for\n * {@code lock()} and {@code tryLock()}. Other threads see the lock held and\n * contend for it as usual.\n * <p>\n * Lock will be removed automatically if client disconnects.\n * <p>\n * Implements a <b>non-fair</b> locking so doesn't guarantee an acquire order.\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * {@link java.util.concurrent.locks.Lock} 的分布式<b>不可重入</b>锁实现。\n"
        " * <p>已持有锁的线程再次加锁时，无论 {@code lock()} 还是 {@code tryLock()}\n"
        " * 均抛出 {@link IllegalMonitorStateException}；其他线程正常竞争。\n"
        " * <p>客户端断开连接后锁会自动释放。\n"
        " * <p>采用<b>非公平</b>加锁，不保证获取顺序。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    /**\n     * The async path inside the future propagates {@code IllegalMonitorStateException}\n     * via {@code CompletionException}. The synchronous {@code get(future)} path,\n     * however, routes through {@link org.redisson.command.CommandAsyncService#convertException}\n     * which wraps any non-{@code RedisException} cause in a generic\n     * {@code RedisException}. Mirror the unwrap that {@link RedissonBaseLock#unlock()}\n     * already performs so callers of the sync {@code lock}/{@code tryLock} methods\n     * see {@code IllegalMonitorStateException} directly.\n     */",
        "    /**\n"
        "     * 异步路径通过 {@code CompletionException} 传播 {@code IllegalMonitorStateException}；\n"
        "     * 同步 {@code get(future)} 路径经 {@link org.redisson.command.CommandAsyncService#convertException}\n"
        "     * 将非 {@code RedisException} 原因包装为通用 {@code RedisException}。\n"
        "     * 与 {@link RedissonBaseLock#unlock()} 的解包逻辑一致，使同步 {@code lock}/{@code tryLock}\n"
        "     * 调用方直接收到 {@code IllegalMonitorStateException}。\n"
        "     */",
    ),
]
W40A_REPLACEMENTS[f"{_R}RedissonNonReentrantLock.java"] = _non_reentrant_lock
W40A_REPLACEMENTS["RedissonNonReentrantLock.java"] = _non_reentrant_lock

# --- RedissonPatternTopic ---

_pattern_topic = [
    (
        "/**\n * Distributed topic implementation. Messages are delivered to all message listeners across Redis cluster.\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 分布式模式主题（Pattern Topic）实现。\n"
        " * <p>基于 Redis {@code PSUBSCRIBE/PUNSUBSCRIBE}，消息会投递给\n"
        " * 集群中所有匹配该模式的监听器。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
]
W40A_REPLACEMENTS[f"{_R}RedissonPatternTopic.java"] = _pattern_topic
W40A_REPLACEMENTS["RedissonPatternTopic.java"] = _pattern_topic

# --- RedissonPriorityBlockingDeque ---

_priority_blocking_deque = [
    (
        "/**\n * <p>Distributed and concurrent implementation of priority blocking deque.\n *\n * <p>Queue size limited by Redis server memory amount. This is why {@link #remainingCapacity()} always\n * returns <code>Integer.MAX_VALUE</code>\n *\n * @author Nikita Koksharov\n */",
        "/**\n"
        " * 优先级阻塞双端队列的分布式并发实现。\n"
        " * <p>容量受 Redis 服务器内存限制，因此 {@link #remainingCapacity()} 始终返回\n"
        " * {@code Integer.MAX_VALUE}。\n"
        " * <p>阻塞操作委托内部 {@link RedissonPriorityBlockingQueue} 完成。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
]
W40A_REPLACEMENTS[f"{_R}RedissonPriorityBlockingDeque.java"] = _priority_blocking_deque
W40A_REPLACEMENTS["RedissonPriorityBlockingDeque.java"] = _priority_blocking_deque

# --- RedissonPriorityBlockingQueue ---

_priority_blocking_queue = [
    (
        "/**\n * <p>Distributed and concurrent implementation of {@link java.util.concurrent.PriorityBlockingQueue}.\n *\n * <p>Queue size limited by Redis server memory amount. This is why {@link #remainingCapacity()} always\n * returns <code>Integer.MAX_VALUE</code>\n *\n * @author Nikita Koksharov\n */",
        "/**\n"
        " * {@link java.util.concurrent.PriorityBlockingQueue} 的分布式并发实现。\n"
        " * <p>基于 Redis Sorted Set 维护优先级顺序，通过轮询 {@code LPOP} 实现阻塞取元素；\n"
        " * 容量受 Redis 内存限制，{@link #remainingCapacity()} 恒为 {@code Integer.MAX_VALUE}。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
]
W40A_REPLACEMENTS[f"{_R}RedissonPriorityBlockingQueue.java"] = _priority_blocking_queue
W40A_REPLACEMENTS["RedissonPriorityBlockingQueue.java"] = _priority_blocking_queue

# --- RedissonPriorityDeque ---

_priority_deque = [
    (
        "/**\n * Distributed and concurrent implementation of {@link java.util.Queue}\n *\n * @author Nikita Koksharov\n *\n * @param <V> the type of elements held in this collection\n */",
        "/**\n"
        " * 优先级双端队列的分布式并发实现（Redis Sorted Set）。\n"
        " * <p>支持队首/队尾 peek、poll 及降序迭代；增删操作需通过 {@code add}/{@code put} 完成。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " * @param <V> 元素类型\n"
        " */",
    ),
]
W40A_REPLACEMENTS[f"{_R}RedissonPriorityDeque.java"] = _priority_deque
W40A_REPLACEMENTS["RedissonPriorityDeque.java"] = _priority_deque

# --- RedissonQueue ---

_queue = [
    (
        "/**\n * Distributed and concurrent implementation of {@link java.util.Queue}\n *\n * @author Nikita Koksharov\n *\n * @param <V> the type of elements held in this collection\n */",
        "/**\n"
        " * {@link java.util.Queue} 的分布式并发实现（Redis List）。\n"
        " * <p>提供 {@code offer/poll/peek} 等 FIFO 语义及批量移动元素能力。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " * @param <V> 元素类型\n"
        " */",
    ),
]
W40A_REPLACEMENTS[f"{_R}RedissonQueue.java"] = _queue
W40A_REPLACEMENTS["RedissonQueue.java"] = _queue

# --- RedissonQueueSemaphore ---

_queue_semaphore = [
    (
        _EMPTY_JDOC,
        "/**\n"
        " * 与队列绑定的分布式信号量辅助类。\n"
        " * <p>在 {@link RedissonSemaphore} 计数允许时，将元素 {@code RPUSH} 到指定队列；\n"
        " * 用于限流生产或背压场景。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
]
W40A_REPLACEMENTS[f"{_R}RedissonQueueSemaphore.java"] = _queue_semaphore
W40A_REPLACEMENTS["RedissonQueueSemaphore.java"] = _queue_semaphore

# --- RedissonReadLock ---

_read_lock = [
    (
        "/**\n * Lock will be removed automatically if client disconnects.\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * {@link RedissonReadWriteLock} 的读锁实现。\n"
        " * <p>允许多个读线程同时持有；写锁独占时读锁不可获取。\n"
        " * 客户端断开连接后锁会自动释放。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
]
W40A_REPLACEMENTS[f"{_R}RedissonReadLock.java"] = _read_lock
W40A_REPLACEMENTS["RedissonReadLock.java"] = _read_lock

# --- RedissonReadWriteLock ---

_read_write_lock = [
    (
        "/**\n * A {@code ReadWriteLock} maintains a pair of associated {@link\n * Lock locks}, one for read-only operations and one for writing.\n * The {@link #readLock read lock} may be held simultaneously by\n * multiple reader threads, so long as there are no writers.  The\n * {@link #writeLock write lock} is exclusive.\n *\n * Works in non-fair mode. Therefore order of read and write\n * locking is unspecified.\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * {@link java.util.concurrent.locks.ReadWriteLock} 的分布式实现。\n"
        " * <p>维护一对关联的 {@link Lock}：读锁供只读操作，写锁供写入操作。\n"
        " * 无写锁时多个读线程可同时持有读锁；写锁互斥。\n"
        " * <p>采用非公平模式，读写加锁顺序不作保证。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
]
W40A_REPLACEMENTS[f"{_R}RedissonReadWriteLock.java"] = _read_write_lock
W40A_REPLACEMENTS["RedissonReadWriteLock.java"] = _read_write_lock

# --- RedissonRedLock ---

_red_lock = [
    (
        "/**\n * RedLock locking algorithm implementation for multiple locks. \n * It manages all locks as one.\n * \n * @see <a href=\"http://redis.io/topics/distlock\">http://redis.io/topics/distlock</a>\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 多锁 RedLock 加锁算法实现，将多个 {@link RLock} 作为整体管理。\n"
        " * <p>需在多数实例（N/2+1）上成功加锁才算获取成功。\n"
        " *\n"
        " * @see <a href=\"http://redis.io/topics/distlock\">http://redis.io/topics/distlock</a>\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    /**\n     * Creates instance with multiple {@link RLock} objects.\n     * Each RLock object could be created by own Redisson instance.\n     *\n     * @param locks - array of locks\n     */",
        "    /**\n"
        "     * 使用多个 {@link RLock} 创建 RedLock 实例。\n"
        "     * <p>每个 RLock 可由独立的 Redisson 实例创建。\n"
        "     *\n"
        "     * @param locks 锁数组\n"
        "     */",
    ),
]
W40A_REPLACEMENTS[f"{_R}RedissonRedLock.java"] = _red_lock
W40A_REPLACEMENTS["RedissonRedLock.java"] = _red_lock
