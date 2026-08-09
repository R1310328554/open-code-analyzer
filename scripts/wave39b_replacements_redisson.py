"""Chinese annotation replacements for Redisson 4.7.0 wave-39b core [15:30]."""
from __future__ import annotations

_R = "redisson/src/main/java/org/redisson/"

_EMPTY_JDOC = "/**\n *\n * @author Nikita Koksharov\n *\n */"
_EMPTY_JDOC_SU = "/**\n *\n * @author Su Ko\n *\n */"

W39B_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

# --- RedissonFuction ---

_fuction = [
    (
        _EMPTY_JDOC,
        "/**\n"
        " * {@link org.redisson.api.RFunction} 的 Redis 实现。\n"
        " * <p>封装 Redis 7+ Functions 管理（加载、删除、转储/恢复）及 {@code FCALL}/{@code FCALL_RO} 调用。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public RedissonFuction(CommandAsyncExecutor commandExecutor) {",
        "    /** 使用全局默认 {@link org.redisson.client.codec.Codec} 构造。 */\n"
        "    public RedissonFuction(CommandAsyncExecutor commandExecutor) {",
    ),
    (
        "    public RedissonFuction(CommandAsyncExecutor commandExecutor, Codec codec) {",
        "    /** @param codec 函数调用参数与返回值的编解码器 */\n"
        "    public RedissonFuction(CommandAsyncExecutor commandExecutor, Codec codec) {",
    ),
    (
        "    private List<Object> encode(Collection<?> values, Codec codec) {",
        "    /** 使用指定 codec 批量编码函数调用参数。 */\n"
        "    private List<Object> encode(Collection<?> values, Codec codec) {",
    ),
    (
        "    public <R> RFuture<R> callAsync(String key, FunctionMode mode, String name, FunctionResult returnType, List<Object> keys, Object... values) {",
        "    /** 向指定键所在 slot 执行函数；{@link FunctionMode#READ} 时使用 {@code FCALL_RO}。 */\n"
        "    public <R> RFuture<R> callAsync(String key, FunctionMode mode, String name, FunctionResult returnType, List<Object> keys, Object... values) {",
    ),
]
W39B_REPLACEMENTS[f"{_R}RedissonFuction.java"] = _fuction
W39B_REPLACEMENTS["RedissonFuction.java"] = _fuction

# --- RedissonGcra ---

_gcra = [
    (
        _EMPTY_JDOC_SU,
        "/**\n"
        " * {@link org.redisson.api.RGcra} 的 GCRA（Generic Cell Rate Algorithm）限流实现。\n"
        " * <p>速率配置存于独立 Hash 键；{@code tryAcquire} 通过 Lua 调用 Redis {@code GCRA} 命令。\n"
        " *\n"
        " * @author Su Ko\n"
        " */",
    ),
    (
        "    public RedissonGcra(CommandAsyncExecutor commandExecutor, String name) {",
        "    /** @param name 限流器 Redis 键名 */\n"
        "    public RedissonGcra(CommandAsyncExecutor commandExecutor, String name) {",
    ),
    (
        "    String getConfigName() {",
        "    /** 返回存储 maxBurst/rate/period 的配置 Hash 键名。 */\n"
        "    String getConfigName() {",
    ),
    (
        "    static void validate(long maxBurst, long tokensPerPeriod, Duration period) {",
        "    /** 校验 GCRA 速率参数合法性。 */\n"
        "    static void validate(long maxBurst, long tokensPerPeriod, Duration period) {",
    ),
    (
        "    static void validateTokens(long tokens) {",
        "    /** 校验单次申请令牌数必须为正。 */\n"
        "    static void validateTokens(long tokens) {",
    ),
    (
        "    static String toSeconds(Duration period) {",
        "    /** 将 {@link Duration} 转为 Redis GCRA 所需的秒数字符串（含小数）。 */\n"
        "    static String toSeconds(Duration period) {",
    ),
    (
        "    static Duration fromSeconds(String seconds) {",
        "    /** 从 Redis 配置 Hash 中的 period 字符串还原 {@link Duration}。 */\n"
        "    static Duration fromSeconds(String seconds) {",
    ),
]
W39B_REPLACEMENTS[f"{_R}RedissonGcra.java"] = _gcra
W39B_REPLACEMENTS["RedissonGcra.java"] = _gcra

# --- RedissonHyperLogLog ---

_hll = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n * @param <V> value\n */",
        "/**\n"
        " * {@link org.redisson.api.RHyperLogLog} 的 Redis HyperLogLog 实现。\n"
        " * <p>封装 {@code PFADD}、{@code PFCOUNT}、{@code PFMERGE} 等基数估算命令。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " * @param <V> 待统计元素类型\n"
        " */",
    ),
    (
        "    public RedissonHyperLogLog(CommandAsyncExecutor commandExecutor, String name) {",
        "    /** 使用默认 codec 构造。 */\n"
        "    public RedissonHyperLogLog(CommandAsyncExecutor commandExecutor, String name) {",
    ),
    (
        "    public RedissonHyperLogLog(Codec codec, CommandAsyncExecutor commandExecutor, String name) {",
        "    /** @param codec 元素编解码器 */\n"
        "    public RedissonHyperLogLog(Codec codec, CommandAsyncExecutor commandExecutor, String name) {",
    ),
]
W39B_REPLACEMENTS[f"{_R}RedissonHyperLogLog.java"] = _hll
W39B_REPLACEMENTS["RedissonHyperLogLog.java"] = _hll

# --- RedissonIdGenerator ---

_idgen = [
    (
        _EMPTY_JDOC,
        "/**\n"
        " * {@link org.redisson.api.RIdGenerator} 的分布式自增 ID 生成器。\n"
        " * <p>本地预分配一段 ID（默认 allocationSize=5000），用完后通过 Lua 原子递增 Redis 计数器。\n"
        " * 请求排队由后台 worker 串行处理，避免并发击穿。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    RedissonIdGenerator(CommandAsyncExecutor connectionManager, String name) {",
        "    /** @param name 主计数器 Redis 键；allocation 键为 {@code name:allocation} */\n"
        "    RedissonIdGenerator(CommandAsyncExecutor connectionManager, String name) {",
    ),
    (
        "    private String getAllocationSizeName(String name) {",
        "    /** 返回预分配步长键名。 */\n"
        "    private String getAllocationSizeName(String name) {",
    ),
    (
        "    private void startIdRequestsHandle() {",
        "    /** 若 worker 未运行则启动 {@link #handleIdRequests} 处理队列。 */\n"
        "    private void startIdRequestsHandle() {",
    ),
    (
        "    private void handleIdRequests() {",
        "    /** 从本地计数器发放 ID；耗尽时 Lua 批量申请新段并重试队列。 */\n"
        "    private void handleIdRequests() {",
    ),
]
W39B_REPLACEMENTS[f"{_R}RedissonIdGenerator.java"] = _idgen
W39B_REPLACEMENTS["RedissonIdGenerator.java"] = _idgen

# --- RedissonJsonBuckets ---

_json_buckets = [
    (
        "public final class RedissonJsonBuckets implements RJsonBuckets {",
        "/**\n"
        " * {@link org.redisson.api.RJsonBuckets} 的 RedisJSON 批量读写实现。\n"
        " * <p>通过 {@code JSON.MGET}/{@code JSON.MSET} 按 slot 分批访问多个 JSON 键。\n"
        " */\n"
        "public final class RedissonJsonBuckets implements RJsonBuckets {",
    ),
    (
        "    RedissonJsonBuckets(JsonCodec codec, CommandAsyncExecutor commandExecutor) {",
        "    /** @param codec JSON 编解码器\n"
        "     *  @param commandExecutor 异步命令执行器\n"
        "     */\n"
        "    RedissonJsonBuckets(JsonCodec codec, CommandAsyncExecutor commandExecutor) {",
    ),
    (
        "    public <V> RFuture<Map<String, V>> getAsync(JsonCodec codec, String path, String... keys) {",
        "    /** 批量读取多个 JSON 键在 {@code path} 下的值，按 slot 合并结果。 */\n"
        "    public <V> RFuture<Map<String, V>> getAsync(JsonCodec codec, String path, String... keys) {",
    ),
    (
        "    public RFuture<Void> setAsync(JsonCodec codec, String path, Map<String, ?> buckets) {",
        "    /** 批量写入 JSON 键；空 map 直接返回已完成 future。 */\n"
        "    public RFuture<Void> setAsync(JsonCodec codec, String path, Map<String, ?> buckets) {",
    ),
]
W39B_REPLACEMENTS[f"{_R}RedissonJsonBuckets.java"] = _json_buckets
W39B_REPLACEMENTS["RedissonJsonBuckets.java"] = _json_buckets

# --- RedissonLexSortedSet ---

_lex = [
    (
        "/**\n * Sorted set contained values of String type\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 元素为 {@link String} 的字典序有序集合（{@link org.redisson.api.RLexSortedSet}）。\n"
        " * <p>基于 Redis {@code ZSET} 的 lex 范围命令（{@code ZRANGEBYLEX}、{@code ZLEXCOUNT} 等）。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public RedissonLexSortedSet(CommandAsyncExecutor commandExecutor, String name, RedissonClient redisson) {",
        "    /** 使用 {@link StringCodec} 构造 lex 有序集。 */\n"
        "    public RedissonLexSortedSet(CommandAsyncExecutor commandExecutor, String name, RedissonClient redisson) {",
    ),
    (
        "    private String value(String fromElement, boolean fromInclusive) {",
        "    /** 将边界元素格式化为 Redis lex 区间前缀 {@code [}（含）或 {@code (}（不含）。 */\n"
        "    private String value(String fromElement, boolean fromInclusive) {",
    ),
]
W39B_REPLACEMENTS[f"{_R}RedissonLexSortedSet.java"] = _lex
W39B_REPLACEMENTS["RedissonLexSortedSet.java"] = _lex

# --- RedissonList ---

_list = [
    (
        "/**\n * Distributed and concurrent implementation of {@link java.util.List}\n *\n * @author Nikita Koksharov\n *\n * @param <V> the type of elements held in this collection\n */",
        "/**\n"
        " * {@link org.redisson.api.RList} 的分布式并发 List 实现。\n"
        " * <p>底层 Redis {@code LIST}；具体读写逻辑由 {@link BaseRedissonList} 提供。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " * @param <V> 列表元素类型\n"
        " */",
    ),
    (
        "    public RedissonList(CommandAsyncExecutor commandExecutor, String name, RedissonClient redisson) {",
        "    /** 使用默认 codec 构造。 */\n"
        "    public RedissonList(CommandAsyncExecutor commandExecutor, String name, RedissonClient redisson) {",
    ),
    (
        "    public RedissonList(Codec codec, CommandAsyncExecutor commandExecutor, String name, RedissonClient redisson) {",
        "    /** @param codec 元素编解码器 */\n"
        "    public RedissonList(Codec codec, CommandAsyncExecutor commandExecutor, String name, RedissonClient redisson) {",
    ),
]
W39B_REPLACEMENTS[f"{_R}RedissonList.java"] = _list
W39B_REPLACEMENTS["RedissonList.java"] = _list

# --- RedissonListMultimapCache ---

_lmmc = [
    (
        "/**\n * @author Nikita Koksharov\n *\n * @param <K> key\n * @param <V> value\n */",
        "/**\n"
        " * 带 per-key TTL 的 {@link org.redisson.api.RListMultimapCache} 实现。\n"
        " * <p>过期时间存于 {@code name:redisson_list_multimap_ttl} 有序集；\n"
        " * 查询时 Lua 脚本过滤已过期键对应的 List。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " * @param <K> 映射键类型\n"
        " * @param <V> 列表元素类型\n"
        " */",
    ),
    (
        "    public RedissonListMultimapCache(EvictionScheduler evictionScheduler, CommandAsyncExecutor connectionManager, String name) {",
        "    /** 使用默认 codec 并注册 EvictionScheduler 清理过期键。 */\n"
        "    public RedissonListMultimapCache(EvictionScheduler evictionScheduler, CommandAsyncExecutor connectionManager, String name) {",
    ),
    (
        "    public RedissonListMultimapCache(EvictionScheduler evictionScheduler, Codec codec, CommandAsyncExecutor connectionManager, String name) {",
        "    /** @param codec 键与值的编解码器 */\n"
        "    public RedissonListMultimapCache(EvictionScheduler evictionScheduler, Codec codec, CommandAsyncExecutor connectionManager, String name) {",
    ),
    (
        "    String getTimeoutSetName() {",
        "    /** 返回存储各映射键过期时间戳的 ZSET 键名。 */\n"
        "    String getTimeoutSetName() {",
    ),
    (
        "    public RList<V> get(K key) {",
        "    /** 返回带 TTL 感知的 List 视图（{@link RedissonListMultimapValues}）。 */\n"
        "    public RList<V> get(K key) {",
    ),
]
W39B_REPLACEMENTS[f"{_R}RedissonListMultimapCache.java"] = _lmmc
W39B_REPLACEMENTS["RedissonListMultimapCache.java"] = _lmmc

# --- RedissonListMultimapCacheNative ---

_lmmcn = [
    (
        "/**\n * @author Nikita Koksharov\n *\n * @param <K> key\n * @param <V> value\n */",
        "/**\n"
        " * 使用 Redis 原生键 TTL 的 {@link org.redisson.api.RListMultimapCacheNative} 实现。\n"
        " * <p>与 {@link RedissonListMultimapCache} 不同，过期由 Redis {@code EXPIRE} 直接作用于 List 键。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " * @param <K> 映射键类型\n"
        " * @param <V> 列表元素类型\n"
        " */",
    ),
    (
        "    public RedissonListMultimapCacheNative(CommandAsyncExecutor connectionManager, String name) {",
        "    /** 使用默认 codec 构造。 */\n"
        "    public RedissonListMultimapCacheNative(CommandAsyncExecutor connectionManager, String name) {",
    ),
    (
        "    public RedissonListMultimapCacheNative(Codec codec, CommandAsyncExecutor connectionManager, String name) {",
        "    /** @param codec 键与值的编解码器 */\n"
        "    public RedissonListMultimapCacheNative(Codec codec, CommandAsyncExecutor connectionManager, String name) {",
    ),
]
W39B_REPLACEMENTS[f"{_R}RedissonListMultimapCacheNative.java"] = _lmmcn
W39B_REPLACEMENTS["RedissonListMultimapCacheNative.java"] = _lmmcn

# --- RedissonListMultimapIterator ---

_lmmi = [
    (
        "public class RedissonListMultimapIterator<K, V, M> extends RedissonMultiMapIterator<K, V, M> {",
        "/**\n"
        " * {@link RedissonListMultimap} 的键迭代器：对每个映射键遍历其 List 元素。\n"
        " *\n"
        " * @param <K> 映射键类型\n"
        " * @param <V> 列表元素类型\n"
        " * @param <M> Multimap 实现类型\n"
        " */\n"
        "public class RedissonListMultimapIterator<K, V, M> extends RedissonMultiMapIterator<K, V, M> {",
    ),
    (
        "    public RedissonListMultimapIterator(RedissonMultimap<K, V> map, CommandAsyncExecutor commandExecutor, Codec codec) {",
        "    /** 使用默认 scan 批次大小构造。 */\n"
        "    public RedissonListMultimapIterator(RedissonMultimap<K, V> map, CommandAsyncExecutor commandExecutor, Codec codec) {",
    ),
    (
        "    public RedissonListMultimapIterator(RedissonMultimap<K, V> map, CommandAsyncExecutor commandExecutor, Codec codec, int count) {",
        "    /** @param count HSCAN 每批返回的键数量 */\n"
        "    public RedissonListMultimapIterator(RedissonMultimap<K, V> map, CommandAsyncExecutor commandExecutor, Codec codec, int count) {",
    ),
    (
        "    protected Iterator<V> getIterator(String name, int count) {",
        "    /** 为指定映射键创建 {@link RedissonList} 本地迭代器。 */\n"
        "    protected Iterator<V> getIterator(String name, int count) {",
    ),
]
W39B_REPLACEMENTS[f"{_R}RedissonListMultimapIterator.java"] = _lmmi
W39B_REPLACEMENTS["RedissonListMultimapIterator.java"] = _lmmi

# --- RedissonLockEntry ---

_lock_entry = [
    (
        _EMPTY_JDOC,
        "/**\n"
        " * 分布式锁 Pub/Sub 订阅条目（{@link PubSubEntry} 实现）。\n"
        " * <p>维护重入计数 {@code counter}、{@link Semaphore} 唤醒 latch 及\n"
        " * 锁释放/续期等 {@link Runnable} 监听器队列。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public RedissonLockEntry(CompletableFuture<RedissonLockEntry> promise) {",
        "    /** @param promise 订阅成功时完成的 future */\n"
        "    public RedissonLockEntry(CompletableFuture<RedissonLockEntry> promise) {",
    ),
    (
        "    public int acquired() {",
        "    /** 返回当前重入持有计数。 */\n"
        "    public int acquired() {",
    ),
    (
        "    public void acquire() {",
        "    /** 重入计数加一。 */\n"
        "    public void acquire() {",
    ),
    (
        "    public int release() {",
        "    /** 重入计数减一并返回新值。 */\n"
        "    public int release() {",
    ),
    (
        "    public void addListener(Runnable listener) {",
        "    /** 注册监听器；若 latch 已释放则立即执行。 */\n"
        "    public void addListener(Runnable listener) {",
    ),
    (
        "    public void tryRunListener() {",
        "    /** 从队列取出一个监听器并执行。 */\n"
        "    public void tryRunListener() {",
    ),
]
W39B_REPLACEMENTS[f"{_R}RedissonLockEntry.java"] = _lock_entry
W39B_REPLACEMENTS["RedissonLockEntry.java"] = _lock_entry

# --- RedissonLongAdder ---

_long_adder = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * {@link org.redisson.api.RLongAdder} 的分布式长整型累加器。\n"
        " * <p>本地 {@link java.util.concurrent.atomic.LongAdder} 缓冲增量，\n"
        " * {@link #sum()} 时通过 {@link RedissonBaseAdder} Topic 协议汇总各节点计数。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public RedissonLongAdder(CommandAsyncExecutor connectionManager, String name, RedissonClient redisson) {",
        "    /** @param name 累加器 Redis 键名前缀 */\n"
        "    public RedissonLongAdder(CommandAsyncExecutor connectionManager, String name, RedissonClient redisson) {",
    ),
    (
        "    protected void doReset() {",
        "    /** 重置本地 {@link LongAdder}。 */\n"
        "    protected void doReset() {",
    ),
    (
        "    public void add(long x) {",
        "    /** 本地累加 {@code x}，不立即写 Redis。 */\n"
        "    public void add(long x) {",
    ),
    (
        "    public long sum() {",
        "    /** 汇总各节点局部计数并返回总和（默认超时 60 秒）。 */\n"
        "    public long sum() {",
    ),
]
W39B_REPLACEMENTS[f"{_R}RedissonLongAdder.java"] = _long_adder
W39B_REPLACEMENTS["RedissonLongAdder.java"] = _long_adder

# --- RedissonMapEntry ---

_map_entry = [
    (
        "public class RedissonMapEntry<K, V> implements Entry<K, V> {",
        "/**\n"
        " * 不可变 {@link java.util.Map.Entry} 实现，用于 Redisson Map 扫描/迭代结果。\n"
        " * <p>{@link #setValue} 不支持修改。\n"
        " *\n"
        " * @param <K> 键类型\n"
        " * @param <V> 值类型\n"
        " */\n"
        "public class RedissonMapEntry<K, V> implements Entry<K, V> {",
    ),
    (
        "    public RedissonMapEntry(K key, V value) {",
        "    /** @param key 映射键\n     *  @param value 映射值 */\n"
        "    public RedissonMapEntry(K key, V value) {",
    ),
    (
        "    public V setValue(V value) {",
        "    /** 不支持修改；始终抛出 {@link UnsupportedOperationException}。 */\n"
        "    public V setValue(V value) {",
    ),
]
W39B_REPLACEMENTS[f"{_R}RedissonMapEntry.java"] = _map_entry
W39B_REPLACEMENTS["RedissonMapEntry.java"] = _map_entry

# --- RedissonMaps ---

_maps = [
    (
        "/**\n *\n * @author Nikita Koksharov\n *\n * @param <K> field type\n * @param <V> value type\n */",
        "/**\n"
        " * {@link org.redisson.api.RMaps} 的多 Map 批量写入门面。\n"
        " * <p>按字段集合分组后通过 {@link RedissonMapsImport} 批量导入，\n"
        " * 优先使用 Redis {@code HIMPORT}，不支持时回退 {@code HSET}。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " * @param <K> Hash 字段类型\n"
        " * @param <V> Hash 值类型\n"
        " */",
    ),
    (
        "    private static final int DEFAULT_BATCH_SIZE = 500;",
        "    /** 未指定时的默认批量 flush 大小。 */\n"
        "    private static final int DEFAULT_BATCH_SIZE = 500;",
    ),
    (
        "    public RedissonMaps(CommandAsyncExecutor commandExecutor) {",
        "    /** 使用全局默认 codec 构造。 */\n"
        "    public RedissonMaps(CommandAsyncExecutor commandExecutor) {",
    ),
    (
        "    public RedissonMaps(Codec codec, CommandAsyncExecutor commandExecutor) {",
        "    /** @param codec 字段与值的编解码器 */\n"
        "    public RedissonMaps(Codec codec, CommandAsyncExecutor commandExecutor) {",
    ),
    (
        "    public RFuture<Void> setAsync(Map<String, Map<K, V>> maps, int batchSize) {",
        "    /** 按字段签名分组后分批写入多个 Redis Hash。 */\n"
        "    public RFuture<Void> setAsync(Map<String, Map<K, V>> maps, int batchSize) {",
    ),
    (
        "    public RMapsImport<K, V> createImport(MapsImportArgs<K> args) {",
        "    /** 根据 {@link MapsImportParams} 创建 {@link RedissonMapsImport} 实例。 */\n"
        "    public RMapsImport<K, V> createImport(MapsImportArgs<K> args) {",
    ),
    (
        "    private Map<HashValue, Group> groupByFields(Map<String, Map<K, V>> maps) {",
        "    /** 将具有相同字段集合（及顺序）的 Map 归为一组以便 HIMPORT。 */\n"
        "    private Map<HashValue, Group> groupByFields(Map<String, Map<K, V>> maps) {",
    ),
]
W39B_REPLACEMENTS[f"{_R}RedissonMaps.java"] = _maps
W39B_REPLACEMENTS["RedissonMaps.java"] = _maps

# --- RedissonMapsImport ---

_maps_import = [
    (
        "/**\n *\n * @author Nikita Koksharov\n *\n * @param <K> field type\n * @param <V> value type\n */",
        "/**\n"
        " * {@link org.redisson.api.RMapsImport} 的批量 Hash 导入实现。\n"
        " * <p>缓冲多行后 flush；优先 {@code HIMPORT PREPARE/SET}，\n"
        " * 遇 unknown command 时禁用 HIMPORT 并回退 {@code DEL}+{@code HSET}。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " * @param <K> Hash 字段类型\n"
        " * @param <V> Hash 值类型\n"
        " */",
    ),
    (
        "    RedissonMapsImport(CommandAsyncExecutor commandExecutor, Codec codec, List<byte[]> fields, int batchSize) {",
        "    /** @param fields 各 Map 共用的字段名（已编码字节序）\n"
        "     *  @param batchSize 缓冲达到该行数时自动 flush\n"
        "     */\n"
        "    RedissonMapsImport(CommandAsyncExecutor commandExecutor, Codec codec, List<byte[]> fields, int batchSize) {",
    ),
    (
        "    public RFuture<Void> addAsync(String name, List<V> values) {",
        "    /** 追加一行；缓冲满 {@code batchSize} 时触发 {@link #flushAsync}。 */\n"
        "    public RFuture<Void> addAsync(String name, List<V> values) {",
    ),
    (
        "    public RFuture<Void> flushAsync() {",
        "    /** 循环 drain 缓冲并写入 Redis，直至队列为空。 */\n"
        "    public RFuture<Void> flushAsync() {",
    ),
    (
        "    void addEncoded(String name, List<byte[]> encodedValues) {",
        "    /** 内部：追加已编码行到缓冲队列。 */\n"
        "    void addEncoded(String name, List<byte[]> encodedValues) {",
    ),
    (
        "    private CompletionStage<Void> writeAsync(List<Row> rows) {",
        "    /** 按配置选择 HIMPORT 或 HSET 路径写入。 */\n"
        "    private CompletionStage<Void> writeAsync(List<Row> rows) {",
    ),
    (
        "    static String fieldsetName(List<byte[]> fields) {",
        "    /** 由字段集合哈希生成 HIMPORT fieldset 名称（{@code rsXXXXXXXX}）。 */\n"
        "    static String fieldsetName(List<byte[]> fields) {",
    ),
    (
        "    static void validateFields(List<byte[]> fields) {",
        "    /** 校验字段非空且无重复。 */\n"
        "    static void validateFields(List<byte[]> fields) {",
    ),
]
W39B_REPLACEMENTS[f"{_R}RedissonMapsImport.java"] = _maps_import
W39B_REPLACEMENTS["RedissonMapsImport.java"] = _maps_import
