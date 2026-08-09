"""Chinese annotation replacements for Redisson 4.7.0 wave-39a core [0:15]."""
from __future__ import annotations

_R = "redisson/src/main/java/org/redisson/"
_EMPTY_JDOC = "/**\n * \n * @author Nikita Koksharov\n *\n */"

W39A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

# --- RedissonBaseLock ---

_base_lock = [
    (
        "/**\n * Base class for implementing distributed locks\n *\n * @author Danila Varatyntsev\n * @author Nikita Koksharov\n */",
        "/**\n"
        " * 分布式锁实现的抽象基类。\n"
        " * <p>封装租约续期、按线程 ID 加解锁、持有计数与 {@link Condition} 等通用逻辑；\n"
        " * 具体加锁算法由 {@link RedissonLock}、{@link RedissonFairLock} 等子类实现。\n"
        " *\n"
        " * @author Danila Varatyntsev\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "        // TODO implement",
        "        // TODO 待实现",
    ),
]
W39A_REPLACEMENTS[f"{_R}RedissonBaseLock.java"] = _base_lock
W39A_REPLACEMENTS["RedissonBaseLock.java"] = _base_lock

# --- RedissonBinaryStream ---

_binary_stream = [
    (
        _EMPTY_JDOC,
        "/**\n"
        " * 基于 Redis 字符串 {@code APPEND/GETRANGE/SETRANGE} 的二进制流实现。\n"
        " * <p>提供 {@link java.io.InputStream}/{@link java.io.OutputStream}、\n"
        " * {@link java.nio.channels.SeekableByteChannel} 与 {@link java.nio.channels.AsynchronousByteChannel}，\n"
        " * 将 Redis 键当作可随机读写的字节序列。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
]
W39A_REPLACEMENTS[f"{_R}RedissonBinaryStream.java"] = _binary_stream
W39A_REPLACEMENTS["RedissonBinaryStream.java"] = _binary_stream

# --- RedissonBlockingDeque ---

_blocking_deque = [
    (
        "/**\n * <p>Distributed and concurrent implementation of {@link java.util.concurrent.BlockingDeque}.\n *\n * <p>Queue size limited by Redis server memory amount. This is why {@link #remainingCapacity()} always\n * returns <code>Integer.MAX_VALUE</code>\n *\n * @author Nikita Koksharov\n */",
        "/**\n"
        " * {@link java.util.concurrent.BlockingDeque} 的分布式并发实现。\n"
        " * <p>队列容量受 Redis 服务器内存限制，因此 {@link #remainingCapacity()} 始终返回\n"
        " * {@code Integer.MAX_VALUE}。\n"
        " * <p>阻塞取元素等操作委托内部 {@link RedissonBlockingQueue} 完成。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
]
W39A_REPLACEMENTS[f"{_R}RedissonBlockingDeque.java"] = _blocking_deque
W39A_REPLACEMENTS["RedissonBlockingDeque.java"] = _blocking_deque

# --- RedissonBlockingQueue ---

_blocking_queue = [
    (
        "/**\n * <p>Distributed and concurrent implementation of {@link java.util.concurrent.BlockingQueue}.\n *\n * <p>Queue size limited by Redis server memory amount. This is why {@link #remainingCapacity()} always\n * returns <code>Integer.MAX_VALUE</code>\n *\n * @author pdeschen@gmail.com\n * @author Nikita Koksharov\n */",
        "/**\n"
        " * {@link java.util.concurrent.BlockingQueue} 的分布式并发实现。\n"
        " * <p>基于 Redis {@code BLPOP/BLMPOP/BRPOPLPUSH} 等命令实现阻塞与批量转移；\n"
        " * 容量受 Redis 内存限制，{@link #remainingCapacity()} 恒为 {@code Integer.MAX_VALUE}。\n"
        " *\n"
        " * @author pdeschen@gmail.com\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
]
W39A_REPLACEMENTS[f"{_R}RedissonBlockingQueue.java"] = _blocking_queue
W39A_REPLACEMENTS["RedissonBlockingQueue.java"] = _blocking_queue

# --- RedissonBloomFilterNative ---

_bloom_native = [
    (
        "/**\n * Bloom filter based on BF.* commands\n *\n * @author Su Ko\n *\n * @param <T> type of object\n */",
        "/**\n"
        " * 基于 Redis {@code BF.*} 命令的原生布隆过滤器实现。\n"
        " * <p>支持 {@code BF.RESERVE/BF.ADD/BF.INSERT/BF.MEXISTS} 及扫描导出等操作。\n"
        " *\n"
        " * @author Su Ko\n"
        " * @param <T> 元素类型\n"
        " */",
    ),
]
W39A_REPLACEMENTS[f"{_R}RedissonBloomFilterNative.java"] = _bloom_native
W39A_REPLACEMENTS["RedissonBloomFilterNative.java"] = _bloom_native

# --- RedissonBuckets ---

_buckets = [
    (
        _EMPTY_JDOC,
        "/**\n"
        " * 批量 {@link org.redisson.api.RBucket} 操作的 {@link org.redisson.api.RBuckets} 实现。\n"
        " * <p>通过 {@code MGET/MSET/MSETNX/MSETEX} 等命令在集群上分槽批量读写；\n"
        " * 键名经 {@link org.redisson.api.NameMapper} 映射。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
]
W39A_REPLACEMENTS[f"{_R}RedissonBuckets.java"] = _buckets
W39A_REPLACEMENTS["RedissonBuckets.java"] = _buckets

# --- RedissonClientSideCaching ---

_csc = [
    (
        "/**\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 客户端侧读缓存（Client-side caching）的 {@link org.redisson.api.RClientSideCaching} 实现。\n"
        " * <p>对带 {@code read} 前缀的异步读命令做本地缓存；订阅 Redis 失效通知后按键清理。\n"
        " * <p>支持 NONE/LRU/LFU/SOFT/WEAK 等驱逐策略。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public <T> T create(Object instance, Class<T> clazz) {",
        "    /** 为 {@link CommandAsyncExecutor} 创建动态代理：拦截 read 方法并命中本地缓存。 */\n"
        "    public <T> T create(Object instance, Class<T> clazz) {",
    ),
    (
        "    public void clearCache(String name) {",
        "    /** Redis 推送键失效时，移除该逻辑名下所有本地缓存条目。 */\n"
        "    public void clearCache(String name) {",
    ),
    (
        "    @Override\n    public void destroy() {",
        "    /** 取消失效订阅并从 {@link org.redisson.connection.ServiceManager} 注销。 */\n"
        "    @Override\n"
        "    public void destroy() {",
    ),
]
W39A_REPLACEMENTS[f"{_R}RedissonClientSideCaching.java"] = _csc
W39A_REPLACEMENTS["RedissonClientSideCaching.java"] = _csc

# --- RedissonCountDownLatch ---

_cdl = [
    (
        "/**\n * Distributed alternative to the {@link java.util.concurrent.CountDownLatch}\n *\n * It has a advantage over {@link java.util.concurrent.CountDownLatch} --\n * count can be reset via {@link #trySetCount}.\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * {@link java.util.concurrent.CountDownLatch} 的分布式替代实现。\n"
        " * <p>相较 JDK 版本，可通过 {@link #trySetCount} 重置计数；\n"
        " * 计数归零时通过 Pub/Sub 唤醒等待线程。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "                // waiting for open state",
        "                // 等待 latch 打开（计数归零通知）",
    ),
    (
        "                // waiting for open state",
        "                // 等待 latch 打开（计数归零通知）",
    ),
]
W39A_REPLACEMENTS[f"{_R}RedissonCountDownLatch.java"] = _cdl
W39A_REPLACEMENTS["RedissonCountDownLatch.java"] = _cdl

# --- RedissonCountDownLatchEntry ---

_cdl_entry = [
    (
        "import java.util.concurrent.ConcurrentLinkedQueue;\n\npublic class RedissonCountDownLatchEntry implements PubSubEntry<RedissonCountDownLatchEntry> {",
        "import java.util.concurrent.ConcurrentLinkedQueue;\n\n"
        "/**\n"
        " * {@link RedissonCountDownLatch} 的 Pub/Sub 订阅条目。\n"
        " * <p>维护引用计数、{@link org.redisson.misc.ReclosableLatch} 唤醒信号\n"
        " * 及计数变化时的监听器队列。\n"
        " */\n"
        "public class RedissonCountDownLatchEntry implements PubSubEntry<RedissonCountDownLatchEntry> {",
    ),
    (
        "    public void acquire() {",
        "    /** 增加一次订阅引用。 */\n"
        "    public void acquire() {",
    ),
    (
        "    public void addListener(Runnable listener) {",
        "    /** 注册计数变化或 latch 打开时的回调。 */\n"
        "    public void addListener(Runnable listener) {",
    ),
    (
        "    public ReclosableLatch getLatch() {",
        "    /** 返回用于阻塞/唤醒等待线程的可重闭 latch。 */\n"
        "    public ReclosableLatch getLatch() {",
    ),
]
W39A_REPLACEMENTS[f"{_R}RedissonCountDownLatchEntry.java"] = _cdl_entry
W39A_REPLACEMENTS["RedissonCountDownLatchEntry.java"] = _cdl_entry

# --- RedissonCuckooFilter ---

_cuckoo = [
    (
        "/**\n * Distributed implementation of cuckoo filter\n * based on Redis Bloom module {@code CF.*} commands.\n *\n * @param <V> element type\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 基于 Redis Bloom 模块 {@code CF.*} 命令的分布式布谷鸟过滤器实现。\n"
        " * <p>支持 {@code CF.RESERVE/CF.ADD/CF.INSERT/CF.MEXISTS/CF.DEL} 等操作。\n"
        " *\n"
        " * @param <V> 元素类型\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    private List<Object> buildInsertParams(CuckooFilterAddArgsImpl<V> a,\n                                            List<V> itemList) {",
        "    /** 组装 {@code CF.INSERT/CF.INSERTNX} 的参数列表（含 CAPACITY/NOCREATE/ITEMS）。 */\n"
        "    private List<Object> buildInsertParams(CuckooFilterAddArgsImpl<V> a,\n"
        "                                            List<V> itemList) {",
    ),
]
W39A_REPLACEMENTS[f"{_R}RedissonCuckooFilter.java"] = _cuckoo
W39A_REPLACEMENTS["RedissonCuckooFilter.java"] = _cuckoo

# --- RedissonDeque ---

_deque = [
    (
        "/**\n * Distributed and concurrent implementation of {@link java.util.Queue}\n *\n * @author Nikita Koksharov\n *\n * @param <V> the type of elements held in this collection\n */",
        "/**\n"
        " * {@link java.util.Deque} 的分布式并发实现（Redis List）。\n"
        " * <p>支持双端 push/pop、{@code LMOVE/BLMOVE} 转移及键空间事件监听。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " * @param <V> 集合元素类型\n"
        " */",
    ),
]
W39A_REPLACEMENTS[f"{_R}RedissonDeque.java"] = _deque
W39A_REPLACEMENTS["RedissonDeque.java"] = _deque

# --- RedissonDoubleAdder ---

_double_adder = [
    (
        _EMPTY_JDOC,
        "/**\n"
        " * {@link org.redisson.api.RDoubleAdder} 实现：跨节点汇总 {@link java.util.concurrent.atomic.DoubleAdder} 局部增量。\n"
        " * <p>继承 {@link RedissonBaseAdder}，通过 Topic 广播 sum/reset 并写入临时 {@link RAtomicDouble} 键。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
]
W39A_REPLACEMENTS[f"{_R}RedissonDoubleAdder.java"] = _double_adder
W39A_REPLACEMENTS["RedissonDoubleAdder.java"] = _double_adder

# --- RedissonExpirable ---

_expirable = [
    (
        _EMPTY_JDOC,
        "/**\n"
        " * 带过期能力的 Redisson 对象的抽象基类。\n"
        " * <p>封装 {@code PEXPIRE/PEXPIREAT/PERSIST/PTTL/PEXPIRETIME} 及\n"
        " * NX/XX/GT/LT 等条件过期变体；多键 Lua 脚本保证批量键任一成功即返回 true。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    protected RFuture<Boolean> expireAsync(long timeToLive, TimeUnit timeUnit, String param, String... keys) {",
        "    /** 对多个键设置相对过期时间；{@code param} 为 Redis 条件子命令（NX/XX/GT/LT 等）。 */\n"
        "    protected RFuture<Boolean> expireAsync(long timeToLive, TimeUnit timeUnit, String param, String... keys) {",
    ),
    (
        "    protected RFuture<Boolean> expireAtAsync(long timestamp, String param, String... keys) {",
        "    /** 对多个键设置绝对过期时间戳（毫秒）。 */\n"
        "    protected RFuture<Boolean> expireAtAsync(long timestamp, String param, String... keys) {",
    ),
    (
        "    protected RFuture<Boolean> clearExpireAsync(String... keys) {",
        "    /** 对多个键执行 {@code PERSIST}，清除过期时间。 */\n"
        "    protected RFuture<Boolean> clearExpireAsync(String... keys) {",
    ),
]
W39A_REPLACEMENTS[f"{_R}RedissonExpirable.java"] = _expirable
W39A_REPLACEMENTS["RedissonExpirable.java"] = _expirable

# --- RedissonFairLock ---

_fair_lock = [
    (
        "/**\n * Distributed implementation of {@link java.util.concurrent.locks.Lock}\n * Implements reentrant lock.<br>\n * Lock will be removed automatically if client disconnects.\n * <p>\n * Implements a <b>fair</b> locking so it guarantees an acquire order by threads.\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * {@link java.util.concurrent.locks.Lock} 的分布式<b>公平</b>可重入锁实现。\n"
        " * <p>客户端断开连接后锁会自动释放；通过等待队列与超时有序集合保证 FIFO 获取顺序。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "                // get the existing timeout for the thread to remove",
        "                // 查找待移除线程在队列中的超时记录",
    ),
    (
        "                        // find the location in the queue where the thread is",
        "                        // 定位该线程在公平队列中的位置",
    ),
    (
        "                        // go to the next index which will exist after the current thread is removed",
        "                        // 从移除后的下一位置起调整后续等待者",
    ),
    (
        "                        // decrement the timeout for the rest of the queue after the thread being removed",
        "                        // 为队列中后续线程递减超时时间",
    ),
    (
        "                        // remove the thread from the queue and timeouts set",
        "                        // 从队列与超时集合中移除该线程",
    ),
    (
        "                    // remove stale threads",
        "                    // 清理已超时的过期等待线程",
    ),
    (
        "                    // remove stale threads",
        "                    // 清理已超时的过期等待线程",
    ),
    (
        "                            // remove the item from the queue and timeout set",
        "                            // 从队列与超时集合中移除队首过期项",
    ),
    (
        "                            // NOTE we do not alter any other timeout",
        "                            // 注意：不修改其余等待者的超时",
    ),
    (
        "                        // decrease timeouts for all waiting in the queue",
        "                        // 为队列中所有等待者递减超时",
    ),
    (
        "                            // remove the item from the queue and timeout set",
        "                            // 从队列与超时集合中移除队首过期项",
    ),
    (
        "                            // NOTE we do not alter any other timeout",
        "                            // 注意：不修改其余等待者的超时",
    ),
    (
        "                        // decrease timeouts for all waiting in the queue",
        "                        // 为队列中所有等待者递减超时",
    ),
    (
        "                    // check if the lock can be acquired now",
        "                    // 判断当前是否可立即获锁",
    ),
    (
        "                        // remove this thread from the queue and timeout set",
        "                        // 将本线程从队列与超时集合中移除",
    ),
    (
        "                        // acquire the lock and set the TTL for the lease",
        "                        // 获锁并设置租约 TTL",
    ),
    (
        "                    // check if the lock is already held, and this is a re-entry",
        "                    // 已持有锁：可重入递增计数",
    ),
    (
        "                    // the lock cannot be acquired",
        "                    // 当前无法获锁",
    ),
    (
        "                    // check if the thread is already in the queue",
        "                    // 检查线程是否已在等待队列中",
    ),
    (
        "                        // the real timeout is the timeout of the prior thread",
        "                        // 真实超时应为前驱线程的超时",
    ),
    (
        "                        // in the queue, but this is approximately correct, and",
        "                        // 此处为近似值，但可避免遍历整个队列",
    ),
    (
        "                        // avoids having to traverse the queue",
        "                        // （完整计算见下方注释掉的 Lua 表达式）",
    ),
    (
        "                    // add the thread to the queue at the end, and set its timeout in the timeout set to the timeout of",
        "                    // 将线程入队到末尾，并在超时集合中设置其超时为",
    ),
    (
        "                    // the prior thread in the queue (or the timeout of the lock if the queue is empty) plus the",
        "                    // 前驱线程超时（队列为空则用锁 TTL）加上",
    ),
    (
        "                    // threadWaitTime",
        "                    // threadWaitTime",
    ),
    (
        "                // remove stale threads",
        "                // 清理已超时的过期等待线程",
    ),
    (
        "                // remove stale threads",
        "                // 清理已超时的过期等待线程",
    ),
]
W39A_REPLACEMENTS[f"{_R}RedissonFairLock.java"] = _fair_lock
W39A_REPLACEMENTS["RedissonFairLock.java"] = _fair_lock

# --- RedissonFencedLock ---

_fenced_lock = [
    (
        "/**\n * Redis based implementation of Fenced Lock with reentrancy support.\n * <p>\n * Each lock acquisition increases fencing token. It should be\n * checked if it's greater or equal with the previous one by\n * the service guarded by this lock and reject operation if condition is false.\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 支持可重入的 Redis 栅栏锁（Fenced Lock）实现。\n"
        " * <p>每次成功加锁递增 fencing token；受保护服务应校验新 token 不小于上次值，\n"
        " * 否则拒绝操作，以防过期锁持有者写入。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "            // lock acquired",
        "            // 已成功获锁",
    ),
    (
        "            // lock acquired",
        "            // 已成功获锁",
    ),
    (
        "            // lock acquired",
        "            // 已成功获锁",
    ),
    (
        "            // waiting for message",
        "            // 等待 Pub/Sub 解锁通知",
    ),
]
W39A_REPLACEMENTS[f"{_R}RedissonFencedLock.java"] = _fenced_lock
W39A_REPLACEMENTS["RedissonFencedLock.java"] = _fenced_lock
