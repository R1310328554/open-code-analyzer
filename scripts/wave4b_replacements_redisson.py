"""Chinese annotation replacements for Redisson 4.7.0 wave-4b [15:30]."""
from __future__ import annotations

import importlib.util
from pathlib import Path

_spec = importlib.util.spec_from_file_location(
    "wave3b_replacements_redisson",
    Path(__file__).with_name("wave3b_replacements_redisson.py"),
)
_mod = importlib.util.module_from_spec(_spec)
assert _spec.loader is not None
_spec.loader.exec_module(_mod)
_W3B = _mod.W3B_REPLACEMENTS


def _h72(name: str) -> list[tuple[str, str]]:
    reps: list[tuple[str, str]] = []
    for old, new in _W3B[name]:
        reps.append((old, new.replace("Hibernate 5.3", "Hibernate 7.2")))
    return reps


W4B_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

W4B_REPLACEMENTS["RedissonRegionNativeFactory.java"] = _h72("RedissonRegionNativeFactory.java")

W4B_REPLACEMENTS["RedissonStorage.java"] = _h72("RedissonStorage.java")

W4B_REPLACEMENTS["RedissonStrategyRegistrationProvider.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Hibernate 启动时注册 Redisson {@link RegionFactory} 策略的 SPI 提供者（Hibernate 7.2）。\n"
        " * <p>允许在配置中使用 {@code redisson} 短名或完整类名。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    @Override\n    public Iterable<StrategyRegistration<?>> getStrategyRegistrations() {",
        "    /** 向 Hibernate 注册 {@link RedissonRegionFactory} 作为 {@link RegionFactory} 实现。 */\n"
        "    @Override\n"
        "    public Iterable<StrategyRegistration<?>> getStrategyRegistrations() {",
    ),
]

W4B_REPLACEMENTS["RedissonConfiguration.java"] = [
    (
        "/**\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Micronaut {@link ConfigurationProperties} 绑定的 Redisson {@link Config} 扩展（Micronaut 2.x）。\n"
        " * <p>首次访问各服务器模式配置时按需懒初始化对应子配置块。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public RedissonConfiguration() {\n    }",
        "    /** 默认构造；子配置块在首次 getter 调用时创建。 */\n    public RedissonConfiguration() {\n    }",
    ),
    (
        "    @Override\n    public SingleServerConfig getSingleServerConfig() {",
        "    /** 获取单机模式配置；未指定任何模式时自动调用 {@link #useSingleServer()}。 */\n"
        "    @Override\n"
        "    public SingleServerConfig getSingleServerConfig() {",
    ),
    (
        "    @Override\n    @ConfigurationBuilder(\"singleServerConfig\")\n    protected void setSingleServerConfig(SingleServerConfig singleConnectionConfig) {",
        "    /** Micronaut 配置绑定入口：{@code redisson.single-server-config.*}。 */\n"
        "    @Override\n"
        "    @ConfigurationBuilder(\"singleServerConfig\")\n"
        "    protected void setSingleServerConfig(SingleServerConfig singleConnectionConfig) {",
    ),
    (
        "    @Override\n    public ClusterServersConfig getClusterServersConfig() {",
        "    /** 获取集群模式配置；未指定任何模式时自动调用 {@link #useClusterServers()}。 */\n"
        "    @Override\n"
        "    public ClusterServersConfig getClusterServersConfig() {",
    ),
    (
        "    @Override\n    @ConfigurationBuilder(value = \"clusterServersConfig\", includes = {\"nodeAddresses\"})",
        "    /** Micronaut 配置绑定入口：{@code redisson.cluster-servers-config.*}。 */\n"
        "    @Override\n"
        "    @ConfigurationBuilder(value = \"clusterServersConfig\", includes = {\"nodeAddresses\"})",
    ),
    (
        "    private boolean isNotDefined() {",
        "    /** 判断是否尚未选择任何 Redis 部署模式（五种子配置均为 null）。 */\n    private boolean isNotDefined() {",
    ),
    (
        "    @Override\n    public ReplicatedServersConfig getReplicatedServersConfig() {",
        "    /** 获取复制模式配置；未指定任何模式时自动调用 {@link #useReplicatedServers()}。 */\n"
        "    @Override\n"
        "    public ReplicatedServersConfig getReplicatedServersConfig() {",
    ),
    (
        "    @Override\n    public SentinelServersConfig getSentinelServersConfig() {",
        "    /** 获取哨兵模式配置；未指定任何模式时自动调用 {@link #useSentinelServers()}。 */\n"
        "    @Override\n"
        "    public SentinelServersConfig getSentinelServersConfig() {",
    ),
    (
        "    @Override\n    public MasterSlaveServersConfig getMasterSlaveServersConfig() {",
        "    /** 获取主从模式配置；未指定任何模式时自动调用 {@link #useMasterSlaveServers()}。 */\n"
        "    @Override\n"
        "    public MasterSlaveServersConfig getMasterSlaveServersConfig() {",
    ),
    (
        "    public Config setCodec(String className) {",
        "    /** 通过反射按类名实例化 {@link Codec} 并设置到配置。 */\n    public Config setCodec(String className) {",
    ),
    (
        "    public Config setNettyHook(String className) {",
        "    /** 通过反射按类名实例化 {@link NettyHook} 并设置到配置。 */\n    public Config setNettyHook(String className) {",
    ),
    (
        "    public Config setAddressResolverGroupFactory(String className) {",
        "    /** 通过反射按类名实例化 {@link AddressResolverGroupFactory} 并设置到配置。 */\n"
        "    public Config setAddressResolverGroupFactory(String className) {",
    ),
    (
        "    public Config setConnectionListener(String className) {",
        "    /** 通过反射按类名实例化 {@link ConnectionListener} 并设置到配置。 */\n"
        "    public Config setConnectionListener(String className) {",
    ),
]

W4B_REPLACEMENTS["RedissonFactory.java"] = [
    (
        "/**\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Micronaut {@link Factory}，注册 Redisson 客户端与缓存 Bean（Micronaut 2.x）。\n"
        " * <p>根据 {@link RedissonCacheConfiguration} 或 {@link RedissonCacheNativeConfiguration}\n"
        " * 为每个命名缓存创建 {@link RedissonSyncCache} 实例。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    @Requires(beans = Config.class)\n    @Singleton\n    @Bean(preDestroy = \"shutdown\")\n    public RedissonClient redisson(Config config) {",
        "    /** 从 {@link Config} 创建 {@link RedissonClient}；容器销毁时调用 {@code shutdown}。 */\n"
        "    @Requires(beans = Config.class)\n"
        "    @Singleton\n"
        "    @Bean(preDestroy = \"shutdown\")\n"
        "    public RedissonClient redisson(Config config) {",
    ),
    (
        "    @EachBean(RedissonCacheConfiguration.class)\n    public RedissonSyncCache cache(@Parameter RedissonCacheConfiguration configuration,",
        "    /** 为每个 {@code redisson.caches.*} 配置创建同步缓存；有 TTL/容量时使用 {@link RMapCache}。 */\n"
        "    @EachBean(RedissonCacheConfiguration.class)\n"
        "    public RedissonSyncCache cache(@Parameter RedissonCacheConfiguration configuration,",
    ),
    (
        "    @EachBean(RedissonCacheNativeConfiguration.class)\n    public RedissonSyncCache cache(@Parameter RedissonCacheNativeConfiguration configuration,",
        "    /** 为每个 {@code redisson.caches-native.*} 配置创建 Native Map 缓存。 */\n"
        "    @EachBean(RedissonCacheNativeConfiguration.class)\n"
        "    public RedissonSyncCache cache(@Parameter RedissonCacheNativeConfiguration configuration,",
    ),
]

W4B_REPLACEMENTS["BaseCacheConfiguration.java"] = [
    (
        "/**\n * Micronaut Cache settings.\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Micronaut 缓存通用配置基类，封装 {@link MapParams} 与过期/容量策略。\n"
        " * <p>子类通过 {@link io.micronaut.context.annotation.EachProperty} 绑定命名缓存实例。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public BaseCacheConfiguration(String name) {",
        "    /** 以缓存名称初始化 {@link MapParams} 选项。 */\n    public BaseCacheConfiguration(String name) {",
    ),
    (
        "    /**\n     * Redis data codec applied to cache entries.\n     * Default is Kryo5Codec codec\n     *\n     * @see Codec\n     * @see org.redisson.codec.Kryo5Codec\n     *\n     * @param className codec class name\n     * @return config\n     */",
        "    /**\n"
        "     * 设置缓存条目的 Redis 编解码器。\n"
        "     * <p>默认使用 {@link org.redisson.codec.Kryo5Codec}。\n"
        "     *\n"
        "     * @param className 编解码器全限定类名\n"
        "     */",
    ),
    (
        "    /**\n     * Cache entry time to live duration applied after each write operation.\n     *\n     * @param expireAfterWrite - time to live duration\n     */",
        "    /**\n     * 写入后条目的存活时间（TTL）。\n     *\n     * @param expireAfterWrite 写入后过期时长\n     */",
    ),
    (
        "    /**\n     * Cache entry time to live duration applied after each read operation.\n     *\n     * @param expireAfterAccess - time to live duration\n     */",
        "    /**\n     * 访问后条目的空闲过期时间（max idle）。\n     *\n     * @param expireAfterAccess 访问后过期时长\n     */",
    ),
    (
        "    /**\n     * Max size of this cache. Superfluous elements are evicted using LRU algorithm.\n     *\n     * @param maxSize - max size\n     *                  If <code>0</code> the cache is unbounded (default).\n     */",
        "    /**\n     * 缓存最大条目数；超出时按 LRU 逐出。\n"
        "     * <p>{@code 0} 表示不限制容量（默认）。\n"
        "     *\n     * @param maxSize 最大条目数\n     */",
    ),
    (
        "    /**\n     * Sets write behind tasks batch size.\n     * During MapWriter methods execution all updates accumulated into a batch of specified size.\n     * <p>\n     * Default is <code>50</code>\n     *\n     * @param writeBehindBatchSize - size of batch\n     */",
        "    /**\n"
        "     * 设置 Write-Behind 批量写入大小。\n"
        "     * <p>MapWriter 执行期间累积的更新达到该批量大小时一并提交。\n"
        "     * <p>默认 {@code 50}。\n"
        "     *\n     * @param writeBehindBatchSize 批量大小\n     */",
    ),
    (
        "    /**\n     * Sets write behind tasks execution delay. All updates would be applied with lag not more than specified delay.\n     * <p>\n     * Default is <code>1000</code> milliseconds\n     *\n     * @param writeBehindDelay - delay in milliseconds\n     */",
        "    /**\n"
        "     * 设置 Write-Behind 任务执行延迟（毫秒）。\n"
        "     * <p>所有更新最晚在该延迟内异步落库。\n"
        "     * <p>默认 {@code 1000} 毫秒。\n"
        "     *\n     * @param writeBehindDelay 延迟毫秒数\n     */",
    ),
    (
        "    /**\n     * Sets {@link MapWriter} object used for write-through operations.\n     *\n     * @param className writer object class name\n     */",
        "    /**\n     * 设置 Write-Through/Write-Behind 使用的 {@link MapWriter}（反射实例化）。\n"
        "     *\n     * @param className MapWriter 全限定类名\n     */",
    ),
    (
        "    /**\n     * Sets write mode.\n     * <p>\n     * Default is <code>{@link WriteMode#WRITE_THROUGH}</code>\n     *\n     * @param writeMode - write mode\n     */",
        "    /**\n     * 设置写入模式。\n"
        "     * <p>默认 {@link WriteMode#WRITE_THROUGH}。\n"
        "     *\n     * @param writeMode 写入模式\n     */",
    ),
    (
        "    /**\n     * Sets {@link MapLoader} object used to load entries during read-operations execution.\n     *\n     * @param className loader object class name\n     */",
        "    /**\n     * 设置读穿透时使用的 {@link MapLoader}（反射实例化）。\n"
        "     *\n     * @param className MapLoader 全限定类名\n     */",
    ),
    (
        "    public <K, V> org.redisson.api.options.MapOptions<K, V> getMapOptions() {",
        "    /** 构建普通 {@link RMap} 选项，合并 loader/writer/codec 等配置。 */\n"
        "    public <K, V> org.redisson.api.options.MapOptions<K, V> getMapOptions() {",
    ),
    (
        "    public <K, V> org.redisson.api.options.MapCacheOptions<K, V> getMapCacheOptions() {",
        "    /** 构建带 TTL/逐出能力的 {@link RMapCache} 选项。 */\n"
        "    public <K, V> org.redisson.api.options.MapCacheOptions<K, V> getMapCacheOptions() {",
    ),
]

W4B_REPLACEMENTS["RedissonAsyncCache.java"] = [
    (
        "/**\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 基于 Redisson {@link RMap}/{@link RMapCache} 的 Micronaut {@link AsyncCache} 实现。\n"
        " * <p>读操作走 Redis 异步 API；缺失键时通过 {@link ExecutorService} 加载并回填。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public RedissonAsyncCache(RMapCache<Object, Object> mapCache,",
        "    /** @param mapCache 带 TTL 的 Map 缓存；无过期策略时为 null\n"
        "     *  @param map 底层 {@link RMap}（始终非 null）\n"
        "     *  @param executorService 缓存未命中时执行 supplier 的线程池\n"
        "     *  @param configuration 过期与编解码配置\n"
        "     */\n"
        "    public RedissonAsyncCache(RMapCache<Object, Object> mapCache,",
    ),
    (
        "    @Override\n    public <T> CompletableFuture<Optional<T>> get(Object key, Argument<T> requiredType) {",
        "    /** 异步读取并转换为 {@code requiredType}；不存在时返回空 Optional。 */\n"
        "    @Override\n"
        "    public <T> CompletableFuture<Optional<T>> get(Object key, Argument<T> requiredType) {",
    ),
    (
        "    @Override\n    public <T> CompletableFuture<T> get(Object key, Argument<T> requiredType, Supplier<T> supplier) {",
        "    /** 缓存未命中时在 IO 线程池执行 supplier 并写入缓存。 */\n"
        "    @Override\n"
        "    public <T> CompletableFuture<T> get(Object key, Argument<T> requiredType, Supplier<T> supplier) {",
    ),
    (
        "    @Override\n    public <T> CompletableFuture<Optional<T>> putIfAbsent(Object key, T value) {",
        "    /** 键不存在时写入；返回先前值（Optional）。 */\n"
        "    @Override\n"
        "    public <T> CompletableFuture<Optional<T>> putIfAbsent(Object key, T value) {",
    ),
    (
        "    @Override\n    public CompletableFuture<Boolean> put(Object key, Object value) {",
        "    /** 写入或覆盖条目；有 {@link RMapCache} 时附带 TTL/max-idle。 */\n"
        "    @Override\n"
        "    public CompletableFuture<Boolean> put(Object key, Object value) {",
    ),
    (
        "    @Override\n    public CompletableFuture<Boolean> invalidate(Object key) {",
        "    /** 异步移除单个缓存键。 */\n    @Override\n    public CompletableFuture<Boolean> invalidate(Object key) {",
    ),
    (
        "    @Override\n    public CompletableFuture<Boolean> invalidateAll() {",
        "    /** 异步删除整个 Redis Map/MapCache。 */\n    @Override\n    public CompletableFuture<Boolean> invalidateAll() {",
    ),
]

W4B_REPLACEMENTS["RedissonCacheConfiguration.java"] = [
    (
        "/**\n * Micronaut Cache settings.\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * {@code redisson.caches.<name>} 命名缓存的 Micronaut 配置绑定。\n"
        " * <p>每个属性块对应一个 {@link RedissonSyncCache} Bean。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public RedissonCacheConfiguration(@Parameter String name) {",
        "    /** @param name 配置键后缀，即缓存逻辑名称 */\n"
        "    public RedissonCacheConfiguration(@Parameter String name) {",
    ),
]

W4B_REPLACEMENTS["RedissonCacheNativeConfiguration.java"] = [
    (
        "/**\n * Micronaut Cache settings.\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * {@code redisson.caches-native.<name>} Native Map 缓存的 Micronaut 配置绑定。\n"
        " * <p>使用 {@link RMapCacheNative}，适合服务端原生过期语义。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public RedissonCacheNativeConfiguration(@Parameter String name) {",
        "    /** @param name 配置键后缀，即 Native 缓存逻辑名称 */\n"
        "    public RedissonCacheNativeConfiguration(@Parameter String name) {",
    ),
]

W4B_REPLACEMENTS["RedissonSyncCache.java"] = [
    (
        "/**\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 基于 Redisson 的 Micronaut 同步缓存，继承 {@link AbstractMapBasedSyncCache}。\n"
        " * <p>{@link #mapCache} 非 null 时启用 TTL/max-idle；否则使用普通 {@link RMap}。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public RedissonSyncCache(ConversionService<?> conversionService,",
        "    /** @param mapCache 带过期策略的 MapCache；纯 Map 模式为 null\n"
        "     *  @param map 底层 Redis Map\n"
        "     *  @param configuration 容量与过期配置\n"
        "     */\n"
        "    public RedissonSyncCache(ConversionService<?> conversionService,",
    ),
    (
        "        if (configuration.getMaxSize() != 0) {\n            mapCache.setMaxSize(configuration.getMaxSize());",
        "        // 配置了 maxSize 时设置 MapCache LRU 上限。\n"
        "        if (configuration.getMaxSize() != 0) {\n"
        "            mapCache.setMaxSize(configuration.getMaxSize());",
    ),
    (
        "    @NonNull\n    @Override\n    public <T> Optional<T> putIfAbsent(@NonNull Object key, @NonNull T value) {",
        "    /** 键不存在时写入并返回先前值（Optional）。 */\n"
        "    @NonNull\n"
        "    @Override\n"
        "    public <T> Optional<T> putIfAbsent(@NonNull Object key, @NonNull T value) {",
    ),
    (
        "    @NonNull\n    @Override\n    public <T> T putIfAbsent(@NonNull Object key, @NonNull Supplier<T> value) {",
        "    /** 键不存在时调用 supplier 获取值并写入；返回最终缓存值。 */\n"
        "    @NonNull\n"
        "    @Override\n"
        "    public <T> T putIfAbsent(@NonNull Object key, @NonNull Supplier<T> value) {",
    ),
    (
        "    @Override\n    public void put(@NonNull Object key, @NonNull Object value) {",
        "    /** 写入或覆盖条目；MapCache 模式下附带 TTL/max-idle。 */\n"
        "    @Override\n"
        "    public void put(@NonNull Object key, @NonNull Object value) {",
    ),
    (
        "    @NonNull\n    @Override\n    public AsyncCache<RMap<Object, Object>> async() {",
        "    /** 返回共享同一 Redis 结构的 {@link RedissonAsyncCache} 视图。 */\n"
        "    @NonNull\n"
        "    @Override\n"
        "    public AsyncCache<RMap<Object, Object>> async() {",
    ),
]

W4B_REPLACEMENTS["AttributeMessage.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 集群 HTTP Session 属性同步消息的基类（Micronaut 2.x）。\n"
        " * <p>携带发起节点 ID 与目标 Session ID，并提供编解码辅助方法。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public AttributeMessage(String nodeId, String sessionId) {",
        "    /** @param nodeId 发起变更的 Micronaut 节点标识\n"
        "     *  @param sessionId 目标 HTTP Session ID\n"
        "     */\n"
        "    public AttributeMessage(String nodeId, String sessionId) {",
    ),
    (
        "\tprotected byte[] toByteArray(Encoder encoder, Object value) throws IOException {",
        "    /** 使用 Redisson {@link Encoder} 将属性值序列化为字节数组。 */\n"
        "\tprotected byte[] toByteArray(Encoder encoder, Object value) throws IOException {",
    ),
    (
        "\tprotected Object toObject(Decoder<?> decoder, byte[] value) throws IOException, ClassNotFoundException {",
        "    /** 使用 Redisson {@link Decoder} 从字节数组反序列化属性值。 */\n"
        "\tprotected Object toObject(Decoder<?> decoder, byte[] value) throws IOException, ClassNotFoundException {",
    ),
]

W4B_REPLACEMENTS["AttributeRemoveMessage.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 跨节点广播：从指定 Session 移除一组属性名。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public AttributeRemoveMessage(String nodeId, String sessionId, Set<CharSequence> names) {",
        "    /** @param names 待移除的属性名集合 */\n"
        "    public AttributeRemoveMessage(String nodeId, String sessionId, Set<CharSequence> names) {",
    ),
]

W4B_REPLACEMENTS["AttributeUpdateMessage.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 跨节点广播：更新 Session 中单个属性的值。\n"
        " * <p>构造时将值编码为 {@code byte[]} 以便 Redis 发布/订阅传输。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public AttributeUpdateMessage(String nodeId, String sessionId, String name, Object value, Encoder encoder) throws IOException {",
        "    /** @param name 属性名\n"
        "     *  @param value 新属性值\n"
        "     *  @param encoder Redisson 编码器\n"
        "     */\n"
        "    public AttributeUpdateMessage(String nodeId, String sessionId, String name, Object value, Encoder encoder) throws IOException {",
    ),
    (
        "    public Object getValue(Decoder<?> decoder) throws IOException, ClassNotFoundException {",
        "    /** 使用给定解码器还原属性值。 */\n"
        "    public Object getValue(Decoder<?> decoder) throws IOException, ClassNotFoundException {",
    ),
]

W4B_REPLACEMENTS["AttributesClearMessage.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 跨节点广播：清空指定 Session 的全部属性。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public AttributesClearMessage(String nodeId, String sessionId) {",
        "    /** @param nodeId 发起清空的节点\n     *  @param sessionId 目标 Session */\n"
        "    public AttributesClearMessage(String nodeId, String sessionId) {",
    ),
]

W4B_REPLACEMENTS["AttributesPutAllMessage.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 跨节点广播：批量写入 Session 属性。\n"
        " * <p>构造时将每个值编码为字节数组以支持集群消息传递。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public AttributesPutAllMessage(String nodeId, String sessionId, Map<CharSequence, Object> attrs, Encoder encoder) throws IOException {",
        "    /** @param attrs 待写入的属性名→值映射\n"
        "     *  @param encoder Redisson 编码器\n"
        "     */\n"
        "    public AttributesPutAllMessage(String nodeId, String sessionId, Map<CharSequence, Object> attrs, Encoder encoder) throws IOException {",
    ),
    (
        "    public Map<CharSequence, Object> getAttrs(Decoder<?> decoder) throws IOException, ClassNotFoundException {",
        "    /** 解码全部属性并返回名→值映射；原始 attrs 为 null 时返回 null。 */\n"
        "    public Map<CharSequence, Object> getAttrs(Decoder<?> decoder) throws IOException, ClassNotFoundException {",
    ),
]
