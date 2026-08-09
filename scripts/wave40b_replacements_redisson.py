"""Chinese annotation replacements for Redisson 4.7.0 wave-40b core [15:30]."""
from __future__ import annotations

_R = "redisson/src/main/java/org/redisson/"

_EMPTY_JDOC = "/**\n *\n * @author Nikita Koksharov\n *\n */"
_EMPTY_JDOC_RUI = "/**\n *\n * @author Rui Gu (https://github.com/jackygurui)\n * @author Nikita Koksharov\n */"

W40B_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

# --- RedissonReference ---

_reference = [
    (
        _EMPTY_JDOC_RUI,
        "/**\n"
        " * 可序列化的 Redisson 对象引用，保存类型名、Redis 键名与可选 {@link org.redisson.client.codec.Codec}。\n"
        " * <p>支持 {@link org.redisson.api.RObject}、Reactive/Rx 变体及 {@link org.redisson.api.annotation.REntity} 类型；\n"
        " * Reactive/Rx 接口会映射到对应的同步实现类名。\n"
        " *\n"
        " * @author Rui Gu (https://github.com/jackygurui)\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public RedissonReference() {",
        "    /** 无参构造，供序列化框架使用。 */\n"
        "    public RedissonReference() {",
    ),
    (
        "    public RedissonReference(Class<?> type, String keyName) {",
        "    /** @param type Redisson 对象接口或 LiveObject 类型\n"
        "     *  @param keyName Redis 键名 */\n"
        "    public RedissonReference(Class<?> type, String keyName) {",
    ),
    (
        "    public RedissonReference(Class<?> type, String keyName, Codec codec) {",
        "    /** @param codec 可选编解码器；非空时保存其类全名 */\n"
        "    public RedissonReference(Class<?> type, String keyName, Codec codec) {",
    ),
    (
        "    /**\n     * @return the type\n     * @throws java.lang.ClassNotFoundException - if the class cannot be located\n     */",
        "    /** @return 同步 {@link org.redisson.api.RObject} 实现类\n"
        "     *  @throws java.lang.ClassNotFoundException 类不在 classpath 时 */\n",
    ),
    (
        "    public Class<?> getRxJavaType() throws ClassNotFoundException {",
        "    /** 返回 {@code type + \"Rx\"} 对应的 RxJava 接口类；LiveObject 不支持。 */\n"
        "    public Class<?> getRxJavaType() throws ClassNotFoundException {",
    ),
    (
        "    /**\n     * @return the type\n     * @throws java.lang.ClassNotFoundException - if the class cannot be located\n     */",
        "    /** @return Reactive 变体类（{@code type + \"Reactive\"}）\n"
        "     *  @throws java.lang.ClassNotFoundException 无对应 Reactive 类型时 */\n",
    ),
    (
        "    /**\n     * @return type name in string\n     */",
        "    /** @return 已解析的同步实现类全名字符串 */\n",
    ),
    (
        "    /**\n     * @return the keyName\n     */",
        "    /** @return Redis 键名 */\n",
    ),
    (
        "    /**\n     * @param keyName the keyName to set\n     */",
        "    /** @param keyName 要设置的 Redis 键名 */\n",
    ),
    (
        "    /**\n     * @return the codec\n     * @throws java.lang.ClassNotFoundException - if the class cannot be located\n     */",
        "    /** @return 编解码器类；未指定时返回 {@code null}\n"
        "     *  @throws java.lang.ClassNotFoundException codec 类无法加载时 */\n",
    ),
    (
        "    private boolean isAvailable(String type) {",
        "    /** 探测给定类名是否可通过 {@link Class#forName} 加载。 */\n"
        "    private boolean isAvailable(String type) {",
    ),
]
W40B_REPLACEMENTS[f"{_R}RedissonReference.java"] = _reference
W40B_REPLACEMENTS["RedissonReference.java"] = _reference

# --- RedissonReliableTopic ---

_reliable_topic = [
    (
        _EMPTY_JDOC,
        "/**\n"
        " * {@link org.redisson.api.RReliableTopic} 的 Redis Stream 可靠主题实现。\n"
        " * <p>发布通过 {@code XADD}；订阅创建消费者组并轮询 pending/新消息，\n"
        " * 看门狗在 ZSet 中续期订阅者，超时订阅者会被清理。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    private static class Entry {",
        "    /** 监听器条目：消息类型与 {@link MessageListener} 实例。 */\n"
        "    private static class Entry {",
    ),
    (
        "    RedissonReliableTopic(Codec codec, CommandAsyncExecutor commandExecutor, String name) {",
        "    /** @param name Stream 键名；超时 ZSet 为 {@code name:timeout} */\n"
        "    RedissonReliableTopic(Codec codec, CommandAsyncExecutor commandExecutor, String name) {",
    ),
    (
        "    RedissonReliableTopic(CommandAsyncExecutor commandExecutor, String name) {",
        "    /** 使用全局默认 codec 构造。 */\n"
        "    RedissonReliableTopic(CommandAsyncExecutor commandExecutor, String name) {",
    ),
    (
        "    private String getTimeout(String name) {",
        "    /** 返回订阅者看门狗 ZSet 键名。 */\n"
        "    private String getTimeout(String name) {",
    ),
    (
        "    public RFuture<Long> publishAsync(Object message) {",
        "    /** 向 Stream 追加消息并返回当前消费者组数量。 */\n"
        "    public RFuture<Long> publishAsync(Object message) {",
    ),
    (
        "    public <M> RFuture<String> addListenerAsync(Class<M> type, MessageListener<M> listener) {",
        "    /** 注册监听器；首个订阅者创建消费者组并启动 {@link #poll} 循环。 */\n"
        "    public <M> RFuture<String> addListenerAsync(Class<M> type, MessageListener<M> listener) {",
    ),
    (
        "    private void poll(String id) {",
        "    /** 拉取 pending 或未投递消息，分派给匹配类型的监听器并 ACK。 */\n"
        "    private void poll(String id) {",
    ),
    (
        "    private RFuture<Void> removeSubscriber() {",
        "    /** 销毁消费者组、取消读/看门狗任务并从 ZSet 移除订阅者。 */\n"
        "    private RFuture<Void> removeSubscriber() {",
    ),
    (
        "    private void renewExpiration() {",
        "    /** 周期性续期看门狗 ZSet 中订阅者分数，防止被当作过期清理。 */\n"
        "    private void renewExpiration() {",
    ),
]
W40B_REPLACEMENTS[f"{_R}RedissonReliableTopic.java"] = _reliable_topic
W40B_REPLACEMENTS["RedissonReliableTopic.java"] = _reliable_topic

# --- RedissonRingBuffer ---

_ring_buffer = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n * @param <V> value type\n */",
        "/**\n"
        " * {@link org.redisson.api.RRingBuffer} 的固定容量环形队列实现。\n"
        " * <p>容量存于独立设置键；{@code RPUSH} 超出上限时 {@code LPOP} 丢弃最旧元素。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " * @param <V> 元素类型\n"
        " */",
    ),
    (
        "    public RedissonRingBuffer(CommandAsyncExecutor commandExecutor, String name, RedissonClient redisson) {",
        "    /** 使用默认 codec；设置键前缀 {@code redisson_rb:}。 */\n"
        "    public RedissonRingBuffer(CommandAsyncExecutor commandExecutor, String name, RedissonClient redisson) {",
    ),
    (
        "    public RedissonRingBuffer(Codec codec, CommandAsyncExecutor commandExecutor, String name, RedissonClient redisson) {",
        "    /** @param codec 元素编解码器 */\n"
        "    public RedissonRingBuffer(Codec codec, CommandAsyncExecutor commandExecutor, String name, RedissonClient redisson) {",
    ),
    (
        "    public RFuture<Boolean> trySetCapacityAsync(int capacity) {",
        "    /** 仅当设置键不存在时写入容量（{@code SETNX}）。 */\n"
        "    public RFuture<Boolean> trySetCapacityAsync(int capacity) {",
    ),
    (
        "    public RFuture<Void> setCapacityAsync(int capacity) {",
        "    /** 设置容量并在必要时 {@code LTRIM} 截断队列。 */\n"
        "    public RFuture<Void> setCapacityAsync(int capacity) {",
    ),
    (
        "    public RFuture<Boolean> addAsync(V e) {",
        "    /** 追加元素；超出容量时弹出队首。容量未定义时 Lua 断言失败。 */\n"
        "    public RFuture<Boolean> addAsync(V e) {",
    ),
    (
        "    public RFuture<Integer> remainingCapacityAsync() {",
        "    /** 返回 {@code max(0, capacity - llen)}。 */\n"
        "    public RFuture<Integer> remainingCapacityAsync() {",
    ),
    (
        "    public RFuture<List<V>> readNewestAsync(int count) {",
        "    /** 读取末尾 {@code count} 个元素（最新）。 */\n"
        "    public RFuture<List<V>> readNewestAsync(int count) {",
    ),
    (
        "    public RFuture<List<V>> readOldestAsync(int count) {",
        "    /** 读取队首 {@code count} 个元素（最旧）。 */\n"
        "    public RFuture<List<V>> readOldestAsync(int count) {",
    ),
]
W40B_REPLACEMENTS[f"{_R}RedissonRingBuffer.java"] = _ring_buffer
W40B_REPLACEMENTS["RedissonRingBuffer.java"] = _ring_buffer

# --- RedissonScript ---

_script = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * {@link org.redisson.api.RScript} 的 Lua 脚本执行门面。\n"
        " * <p>支持 {@code SCRIPT LOAD/EXISTS/FLUSH/KILL} 及 {@code EVAL}/{@code EVALSHA}；\n"
        " * 只读模式优先尝试 {@code EVALSHA_RO}，不支持时自动回退。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public RedissonScript(CommandAsyncExecutor commandExecutor) {",
        "    /** 使用全局默认 codec 构造。 */\n"
        "    public RedissonScript(CommandAsyncExecutor commandExecutor) {",
    ),
    (
        "    public RedissonScript(CommandAsyncExecutor commandExecutor, Codec codec) {",
        "    /** @param codec 脚本参数与返回值的编解码器 */\n"
        "    public RedissonScript(CommandAsyncExecutor commandExecutor, Codec codec) {",
    ),
    (
        "    public RFuture<String> scriptLoadAsync(String luaScript) {",
        "    /** 向所有 Redis 节点加载脚本，返回首个节点的 SHA1。 */\n"
        "    public RFuture<String> scriptLoadAsync(String luaScript) {",
    ),
    (
        "    public RFuture<String> scriptLoadAsync(String key, String luaScript) {",
        "    /** 向指定键所在 slot 的节点加载脚本。 */\n"
        "    public RFuture<String> scriptLoadAsync(String key, String luaScript) {",
    ),
    (
        "    private static String getKey(List<Object> keys) {",
        "    /** 从 KEYS 列表取首个键作为路由 slot 的键。 */\n"
        "    private static String getKey(List<Object> keys) {",
    ),
    (
        "    private List<Object> encode(Collection<?> values, Codec codec) {",
        "    /** 使用指定 codec 批量编码脚本 ARGV 参数。 */\n"
        "    private List<Object> encode(Collection<?> values, Codec codec) {",
    ),
    (
        "    public <R> RFuture<R> evalShaAsync(String key, Mode mode, String shaDigest, ReturnType returnType,",
        "    /** 按 {@link Mode} 在指定键 slot 执行 {@code EVALSHA}；只读且支持时走 {@code EVALSHA_RO}。 */\n"
        "    public <R> RFuture<R> evalShaAsync(String key, Mode mode, String shaDigest, ReturnType returnType,",
    ),
    (
        "    public <R> RFuture<R> evalAsync(String key, Mode mode, String luaScript, ReturnType returnType, List<Object> keys,",
        "    /** 在指定键 slot 执行 {@code EVAL}；{@link Mode#READ_ONLY} 时使用 evalRead。 */\n"
        "    public <R> RFuture<R> evalAsync(String key, Mode mode, String luaScript, ReturnType returnType, List<Object> keys,",
    ),
]
W40B_REPLACEMENTS[f"{_R}RedissonScript.java"] = _script
W40B_REPLACEMENTS["RedissonScript.java"] = _script

# --- RedissonSetMultimapCache ---

_set_multimap_cache = [
    (
        "/**\n * @author Nikita Koksharov\n *\n * @param <K> key\n * @param <V> value\n */",
        "/**\n"
        " * {@link org.redisson.api.RSetMultimapCache} 的带 TTL 集合多映射实现。\n"
        " * <p>键级过期时间存于 ZSet；{@link EvictionScheduler} 负责后台清理。\n"
        " * 各映射值集合为独立 Redis Set。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " * @param <K> 映射键类型\n"
        " * @param <V> 集合元素类型\n"
        " */",
    ),
    (
        "    public RedissonSetMultimapCache(EvictionScheduler evictionScheduler, CommandAsyncExecutor connectionManager, String name) {",
        "    /** 使用默认 codec 构造；{@code baseCache} 委托过期与删除。 */\n"
        "    public RedissonSetMultimapCache(EvictionScheduler evictionScheduler, CommandAsyncExecutor connectionManager, String name) {",
    ),
    (
        "    public RedissonSetMultimapCache(EvictionScheduler evictionScheduler, Codec codec, CommandAsyncExecutor connectionManager, String name) {",
        "    /** @param codec 键与元素编解码器 */\n"
        "    public RedissonSetMultimapCache(EvictionScheduler evictionScheduler, Codec codec, CommandAsyncExecutor connectionManager, String name) {",
    ),
    (
        "    String getTimeoutSetName() {",
        "    /** 返回键级过期 ZSet 名称（后缀 {@code redisson_set_multimap_ttl}）。 */\n"
        "    String getTimeoutSetName() {",
    ),
    (
        "    public RSet<V> get(K key) {",
        "    /** 返回指定键对应的 {@link RedissonSetMultimapValues} 视图（含 TTL 感知）。 */\n"
        "    public RSet<V> get(K key) {",
    ),
    (
        "    public RFuture<Boolean> expireKeyAsync(K key, long timeToLive, TimeUnit timeUnit) {",
        "    /** 为单个映射键设置存活时间，委托 {@link RedissonMultimapCache}。 */\n"
        "    public RFuture<Boolean> expireKeyAsync(K key, long timeToLive, TimeUnit timeUnit) {",
    ),
]
W40B_REPLACEMENTS[f"{_R}RedissonSetMultimapCache.java"] = _set_multimap_cache
W40B_REPLACEMENTS["RedissonSetMultimapCache.java"] = _set_multimap_cache

# --- RedissonSetMultimapCacheNative ---

_set_multimap_cache_native = [
    (
        "/**\n * @author Nikita Koksharov\n *\n * @param <K> key\n * @param <V> value\n */",
        "/**\n"
        " * {@link org.redisson.api.RSetMultimapCacheNative} 的原生 TTL 集合多映射实现。\n"
        " * <p>键级过期由 Redis 原生 {@code EXPIRE} 管理，无需 {@link EvictionScheduler}。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " * @param <K> 映射键类型\n"
        " * @param <V> 集合元素类型\n"
        " */",
    ),
    (
        "    public RedissonSetMultimapCacheNative(CommandAsyncExecutor connectionManager, String name) {",
        "    /** 使用默认 codec 构造。 */\n"
        "    public RedissonSetMultimapCacheNative(CommandAsyncExecutor connectionManager, String name) {",
    ),
    (
        "    public RedissonSetMultimapCacheNative(Codec codec, CommandAsyncExecutor connectionManager, String name) {",
        "    /** @param codec 键与元素编解码器 */\n"
        "    public RedissonSetMultimapCacheNative(Codec codec, CommandAsyncExecutor connectionManager, String name) {",
    ),
    (
        "    public RFuture<Boolean> expireKeyAsync(K key, long timeToLive, TimeUnit timeUnit) {",
        "    /** 为单个映射键设置原生 TTL，委托 {@link RedissonMultimapCacheNative}。 */\n"
        "    public RFuture<Boolean> expireKeyAsync(K key, long timeToLive, TimeUnit timeUnit) {",
    ),
]
W40B_REPLACEMENTS[f"{_R}RedissonSetMultimapCacheNative.java"] = _set_multimap_cache_native
W40B_REPLACEMENTS["RedissonSetMultimapCacheNative.java"] = _set_multimap_cache_native

# --- RedissonSetMultimapIterator ---

_set_multimap_iter = [
    (
        "public class RedissonSetMultimapIterator<K, V, M> extends RedissonMultiMapIterator<K, V, M> {",
        "/**\n"
        " * {@link RedissonSetMultimap} 的键-值集合迭代器。\n"
        " * <p>对每个映射键通过 {@link RedissonSet#iterator(int)} 遍历其 Set 值。\n"
        " *\n"
        " * @param <K> 映射键类型\n"
        " * @param <V> 集合元素类型\n"
        " * @param <M> 多映射实现类型\n"
        " */\n"
        "public class RedissonSetMultimapIterator<K, V, M> extends RedissonMultiMapIterator<K, V, M> {",
    ),
    (
        "    public RedissonSetMultimapIterator(RedissonMultimap<K, V> map, CommandAsyncExecutor commandExecutor, Codec codec) {",
        "    /** 使用默认 HSCAN 批次大小构造。 */\n"
        "    public RedissonSetMultimapIterator(RedissonMultimap<K, V> map, CommandAsyncExecutor commandExecutor, Codec codec) {",
    ),
    (
        "    public RedissonSetMultimapIterator(RedissonMultimap<K, V> map, CommandAsyncExecutor commandExecutor, Codec codec, int count) {",
        "    /** @param count 每次 SCAN 建议返回的键数量 */\n"
        "    public RedissonSetMultimapIterator(RedissonMultimap<K, V> map, CommandAsyncExecutor commandExecutor, Codec codec, int count) {",
    ),
    (
        "    protected Iterator<V> getIterator(String name, int count) {",
        "    /** 为给定值 Set 键创建 {@link RedissonSet} 迭代器。 */\n"
        "    protected Iterator<V> getIterator(String name, int count) {",
    ),
]
W40B_REPLACEMENTS[f"{_R}RedissonSetMultimapIterator.java"] = _set_multimap_iter
W40B_REPLACEMENTS["RedissonSetMultimapIterator.java"] = _set_multimap_iter

# --- RedissonShardedTopic ---

_sharded_topic = [
    (
        "/**\n * Sharded Topic for Redis Cluster. Messages are delivered to message listeners connected to the same Topic.\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Redis Cluster 分片主题 {@link org.redisson.api.RShardedTopic} 实现。\n"
        " * <p>使用 {@code SPUBLISH}/{@code SSUBSCRIBE}；消息仅投递到同一 slot 上的监听器。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public RedissonShardedTopic(CommandAsyncExecutor commandExecutor, String... names) {",
        "    /** 使用默认 codec 构造。 */\n"
        "    public RedissonShardedTopic(CommandAsyncExecutor commandExecutor, String... names) {",
    ),
    (
        "    public static RedissonTopic createRaw(Codec codec, CommandAsyncExecutor commandExecutor, String... names) {",
        "    /** 创建不经 {@link NameMapper} 映射的原始分片主题实例。 */\n"
        "    public static RedissonTopic createRaw(Codec codec, CommandAsyncExecutor commandExecutor, String... names) {",
    ),
    (
        "    public RFuture<Long> publishAsync(Object message) {",
        "    /** 通过 {@code SPUBLISH} 向分片频道发布消息。 */\n"
        "    public RFuture<Long> publishAsync(Object message) {",
    ),
    (
        "    protected RFuture<Integer> addListenerAsync(RedisPubSubListener<?> pubSubListener) {",
        "    /** 通过 {@code SSUBSCRIBE} 注册分片 Pub/Sub 监听器。 */\n"
        "    protected RFuture<Integer> addListenerAsync(RedisPubSubListener<?> pubSubListener) {",
    ),
]
W40B_REPLACEMENTS[f"{_R}RedissonShardedTopic.java"] = _sharded_topic
W40B_REPLACEMENTS["RedissonShardedTopic.java"] = _sharded_topic

# --- RedissonShutdownException ---

_shutdown_ex = [
    (
        "public class RedissonShutdownException extends RedisException {",
        "/**\n"
        " * Redisson 客户端关闭过程中抛出的异常。\n"
        " * <p>表示操作因 {@link org.redisson.connection.ConnectionManager} 正在关停而无法完成。\n"
        " */\n"
        "public class RedissonShutdownException extends RedisException {",
    ),
    (
        "    public RedissonShutdownException(String message) {",
        "    /** @param message 异常描述 */\n"
        "    public RedissonShutdownException(String message) {",
    ),
]
W40B_REPLACEMENTS[f"{_R}RedissonShutdownException.java"] = _shutdown_ex
W40B_REPLACEMENTS["RedissonShutdownException.java"] = _shutdown_ex

# --- RedissonSpinLock ---

_spin_lock = [
    (
        "/**\n * Distributed implementation of {@link java.util.concurrent.locks.Lock}\n * Implements reentrant lock.<br>\n * Lock will be removed automatically if client disconnects.\n * This lock implementation doesn't use pub/sub mechanism. It can be used in large Redis clusters despite current naive\n * pub/sub implementation.\n * <p>\n * Implements a <b>non-fair</b> locking so doesn't guarantees an acquire order.\n *\n * @author Danila Varatyntsev\n */",
        "/**\n"
        " * {@link java.util.concurrent.locks.Lock} 的分布式自旋锁实现（可重入）。\n"
        " * <p>不使用 Pub/Sub，通过退避策略轮询 {@code tryLock}，适合大规模集群。\n"
        " * 客户端断开时锁自动释放；<b>非公平</b>，不保证获取顺序。\n"
        " *\n"
        " * @author Danila Varatyntsev\n"
        " */",
    ),
    (
        "    RedissonSpinLock(CommandAsyncExecutor commandExecutor, String name,\n                            LockOptions.BackOff backOff) {",
        "    /** @param backOff 获取失败时的退避策略 */\n"
        "    RedissonSpinLock(CommandAsyncExecutor commandExecutor, String name,\n"
        "                            LockOptions.BackOff backOff) {",
    ),
    (
        "    public void lockInterruptibly(long leaseTime, TimeUnit unit) throws InterruptedException {",
        "    /** 自旋尝试获取锁直至成功；{@code leaseTime <= 0} 时使用看门狗续期。 */\n"
        "    public void lockInterruptibly(long leaseTime, TimeUnit unit) throws InterruptedException {",
    ),
    (
        "    <T> RFuture<T> tryLockInnerAsync(long leaseTime, TimeUnit unit, long threadId, RedisStrictCommand<T> command) {",
        "    /** Lua：不存在或当前线程已持有时 HINCRBY 并重置 TTL；否则返回剩余 TTL。 */\n"
        "    <T> RFuture<T> tryLockInnerAsync(long leaseTime, TimeUnit unit, long threadId, RedisStrictCommand<T> command) {",
    ),
    (
        "    protected RFuture<Boolean> unlockInnerAsync(long threadId, String requestId, long timeout) {",
        "    /** Lua 递减重入计数；计数归零时 DEL 键并通过 latch 通知等待者。 */\n"
        "    protected RFuture<Boolean> unlockInnerAsync(long threadId, String requestId, long timeout) {",
    ),
    (
        "    private void lockAsync(long leaseTime, TimeUnit unit, long currentThreadId, CompletableFuture<Void> result,",
        "    /** 异步自旋锁：失败时按 {@link LockOptions.BackOffPolicy} 调度重试。 */\n"
        "    private void lockAsync(long leaseTime, TimeUnit unit, long currentThreadId, CompletableFuture<Void> result,",
    ),
]
W40B_REPLACEMENTS[f"{_R}RedissonSpinLock.java"] = _spin_lock
W40B_REPLACEMENTS["RedissonSpinLock.java"] = _spin_lock

# --- RedissonSubSortedSet ---

_sub_sorted_set = [
    (
        "/**\n *\n * @author Nikita Koksharov\n *\n * @param <V>\n */",
        "/**\n"
        " * {@link RedissonSortedSet} 的有界子视图（{@link java.util.SortedSet}）。\n"
        " * <p>由 {@code headValue}/{@code tailValue} 界定 score 范围；\n"
        " * 大部分 mutating/查询方法当前未实现（抛出 {@link UnsupportedOperationException}）。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " * @param <V> 元素类型\n"
        " */",
    ),
    (
        "    RedissonSubSortedSet(RedissonSortedSet<V> redissonSortedSet, ConnectionManager connectionManager, V headValue, V tailValue) {",
        "    /** @param headValue 子集下界元素（含）\n"
        "     *  @param tailValue 子集上界元素（含） */\n"
        "    RedissonSubSortedSet(RedissonSortedSet<V> redissonSortedSet, ConnectionManager connectionManager, V headValue, V tailValue) {",
    ),
    (
        "    public Comparator<? super V> comparator() {",
        "    /** 委托底层 {@link RedissonSortedSet#comparator()}。 */\n"
        "    public Comparator<? super V> comparator() {",
    ),
    (
        "    public SortedSet<V> headSet(V toElement) {",
        "    /** 返回 {@code (null, toElement]} 子集视图。 */\n"
        "    public SortedSet<V> headSet(V toElement) {",
    ),
    (
        "    public SortedSet<V> tailSet(V fromElement) {",
        "    /** 返回 {@code [fromElement, null)} 子集视图。 */\n"
        "    public SortedSet<V> tailSet(V fromElement) {",
    ),
]
W40B_REPLACEMENTS[f"{_R}RedissonSubSortedSet.java"] = _sub_sorted_set
W40B_REPLACEMENTS["RedissonSubSortedSet.java"] = _sub_sorted_set

# --- RedissonTDigest ---

_tdigest = [
    (
        "/**\n * Distributed implementation of t-digest\n * based on Redis Bloom module {@code TDIGEST.*} commands.\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * {@link org.redisson.api.RTDigest} 的分布式 t-digest 实现。\n"
        " * <p>基于 RedisBloom 模块 {@code TDIGEST.*} 命令，用于流式分位数与 CDF 估算。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public RedissonTDigest(CommandAsyncExecutor commandExecutor, String name) {",
        "    /** @param name t-digest 结构 Redis 键名 */\n"
        "    public RedissonTDigest(CommandAsyncExecutor commandExecutor, String name) {",
    ),
    (
        "    public RFuture<Void> createAsync(int compression) {",
        "    /** 创建 t-digest 并指定压缩因子。 */\n"
        "    public RFuture<Void> createAsync(int compression) {",
    ),
    (
        "    public RFuture<Void> addAsync(double... values) {",
        "    /** 批量追加观测值（{@code TDIGEST.ADD}）。 */\n"
        "    public RFuture<Void> addAsync(double... values) {",
    ),
    (
        "    public RFuture<Void> mergeWithAsync(TDigestMergeArgs args) {",
        "    /** 合并其他 t-digest 键，支持 COMPRESSION/OVERRIDE 选项。 */\n"
        "    public RFuture<Void> mergeWithAsync(TDigestMergeArgs args) {",
    ),
    (
        "    public RFuture<List<Double>> quantileAsync(double... quantiles) {",
        "    /** 查询给定分位点对应的值（{@code TDIGEST.QUANTILE}）。 */\n"
        "    public RFuture<List<Double>> quantileAsync(double... quantiles) {",
    ),
    (
        "    private static String toStr(double value) {",
        "    /** 将 double 转为 Redis TDIGEST 可接受的字符串（含 nan/inf）。 */\n"
        "    private static String toStr(double value) {",
    ),
]
W40B_REPLACEMENTS[f"{_R}RedissonTDigest.java"] = _tdigest
W40B_REPLACEMENTS["RedissonTDigest.java"] = _tdigest

# --- RedissonTopK ---

_topk = [
    (
        "/**\n * Distributed implementation of Top-K\n * based on Redis Bloom module {@code TOPK.*} commands.\n *\n * @param <V> element type\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * {@link org.redisson.api.RTopK} 的分布式 Top-K 频率 sketch 实现。\n"
        " * <p>基于 RedisBloom 模块 {@code TOPK.*} 命令，近似追踪高频元素。\n"
        " *\n"
        " * @param <V> 元素类型\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public RedissonTopK(Codec codec,\n                        CommandAsyncExecutor commandExecutor,\n                        String name) {",
        "    /** @param codec 元素编解码器\n"
        "     *  @param name Top-K 结构 Redis 键名 */\n"
        "    public RedissonTopK(Codec codec,\n                        CommandAsyncExecutor commandExecutor,\n                        String name) {",
    ),
    (
        "    public RFuture<Void> initAsync(int topK) {",
        "    /** 以默认 width/depth/decay 初始化 Top-K（{@code TOPK.RESERVE}）。 */\n"
        "    public RFuture<Void> initAsync(int topK) {",
    ),
    (
        "    public RFuture<V> addAsync(V item) {",
        "    /** 追加元素；若被挤出 Top-K 则返回被替换项，否则返回 null。 */\n"
        "    public RFuture<V> addAsync(V item) {",
    ),
    (
        "    public RFuture<List<V>> listAsync() {",
        "    /** 返回当前 Top-K 元素列表（{@code TOPK.LIST}）。 */\n"
        "    public RFuture<List<V>> listAsync() {",
    ),
    (
        "    public RFuture<Map<V, Long>> listWithCountAsync() {",
        "    /** 返回 Top-K 元素及其近似计数（{@code TOPK.LIST WITHCOUNT}）。 */\n"
        "    public RFuture<Map<V, Long>> listWithCountAsync() {",
    ),
]
W40B_REPLACEMENTS[f"{_R}RedissonTopK.java"] = _topk
W40B_REPLACEMENTS["RedissonTopK.java"] = _topk

# --- RedissonTopic ---

_topic = [
    (
        "/**\n * Distributed topic implementation. Messages are delivered to all message listeners across Redis cluster.\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * {@link org.redisson.api.RTopic} 的分布式 Pub/Sub 主题实现。\n"
        " * <p>消息通过 {@code PUBLISH} 广播，集群内所有订阅同一频道的监听器均可收到。\n"
        " * 支持多频道名与 {@link NameMapper} 映射。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public RedissonTopic(CommandAsyncExecutor commandExecutor, String... names) {",
        "    /** 使用全局默认 codec 构造。 */\n"
        "    public RedissonTopic(CommandAsyncExecutor commandExecutor, String... names) {",
    ),
    (
        "    public static RedissonTopic createRaw(CommandAsyncExecutor commandExecutor, String... names) {",
        "    /** 创建不经 {@link NameMapper} 映射的原始主题实例。 */\n"
        "    public static RedissonTopic createRaw(CommandAsyncExecutor commandExecutor, String... names) {",
    ),
    (
        "    public RedissonTopic(Codec codec, CommandAsyncExecutor commandExecutor, NameMapper nameMapper, String... names) {",
        "    /** @param nameMapper 频道名映射器；各 name 经 map 后转为 {@link ChannelName} */\n"
        "    public RedissonTopic(Codec codec, CommandAsyncExecutor commandExecutor, NameMapper nameMapper, String... names) {",
    ),
    (
        "    public RFuture<Long> publishAsync(Object message) {",
        "    /** 向首个频道名 {@code PUBLISH} 编码后的消息。 */\n"
        "    public RFuture<Long> publishAsync(Object message) {",
    ),
    (
        "    protected RFuture<Integer> addListenerAsync(RedisPubSubListener<?> pubSubListener) {",
        "    /** 订阅所有 {@link #channelNames} 并返回监听器 id（identityHashCode）。 */\n"
        "    protected RFuture<Integer> addListenerAsync(RedisPubSubListener<?> pubSubListener) {",
    ),
    (
        "    public RFuture<Long> countSubscribersAsync() {",
        "    /** 查询各频道订阅者总数（{@code PUBSUB NUMSUB}）。 */\n"
        "    public RFuture<Long> countSubscribersAsync() {",
    ),
]
W40B_REPLACEMENTS[f"{_R}RedissonTopic.java"] = _topic
W40B_REPLACEMENTS["RedissonTopic.java"] = _topic

# --- RedissonWriteLock ---

_write_lock = [
    (
        "/**\n * Lock will be removed automatically if client disconnects.\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * {@link org.redisson.api.RLock} 读写锁中的写锁实现。\n"
        " * <p>Hash 字段 {@code mode=write} 标记写模式；可重入。\n"
        " * 客户端断开时锁自动释放；释放后若有读锁则切换为 {@code mode=read}。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    protected RedissonWriteLock(CommandAsyncExecutor commandExecutor, String name) {",
        "    /** @param name 读写锁共享 Redis Hash 键名 */\n"
        "    protected RedissonWriteLock(CommandAsyncExecutor commandExecutor, String name) {",
    ),
    (
        "    String getChannelName() {",
        "    /** 返回写锁 Pub/Sub 通知频道（前缀 {@code redisson_rwlock:}）。 */\n"
        "    String getChannelName() {",
    ),
    (
        "    protected String getLockName(long threadId) {",
        "    /** 写锁线程字段名为 {@code {uuid}:{threadId}:write}。 */\n"
        "    protected String getLockName(long threadId) {",
    ),
    (
        "    <T> RFuture<T> tryLockInnerAsync(long waitTime, long leaseTime, TimeUnit unit, long threadId, RedisStrictCommand<T> command) {",
        "    /** Lua：无 mode 或已有写锁且同线程时获取/重入；否则返回剩余 TTL。 */\n"
        "    <T> RFuture<T> tryLockInnerAsync(long waitTime, long leaseTime, TimeUnit unit, long threadId, RedisStrictCommand<T> command) {",
    ),
    (
        "    public Condition newCondition() {",
        "    /** 写锁不支持 {@link Condition}。 */\n"
        "    public Condition newCondition() {",
    ),
    (
        "    public boolean isLocked() {",
        "    /** 当 Hash 中 {@code mode} 字段为 {@code write} 时视为已加写锁。 */\n"
        "    public boolean isLocked() {",
    ),
]
W40B_REPLACEMENTS[f"{_R}RedissonWriteLock.java"] = _write_lock
W40B_REPLACEMENTS["RedissonWriteLock.java"] = _write_lock
