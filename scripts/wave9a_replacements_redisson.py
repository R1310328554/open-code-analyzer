"""Chinese annotation replacements for Redisson 4.7.0 wave-9a quarkus-33/spring [0:15]."""
from __future__ import annotations

import importlib.util
from pathlib import Path

_spec7a = importlib.util.spec_from_file_location(
    "wave7a_replacements_redisson",
    Path(__file__).with_name("wave7a_replacements_redisson.py"),
)
_w7a = importlib.util.module_from_spec(_spec7a)
assert _spec7a.loader is not None
_spec7a.loader.exec_module(_w7a)

W9A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

# --- quarkus-33 cdi runtime (reuse wave-7a) ---
for _name in (
    "RedissonClientRecorder.java",
    "RedissonConfig.java",
    "ByteBuddySubstitutions.java",
    "CodecsSubstitutions.java",
):
    W9A_REPLACEMENTS[_name] = _w7a.W7A_REPLACEMENTS[_name]

# --- spring-boot-starter ---
W9A_REPLACEMENTS["RedissonAutoConfigurationCustomizer.java"] = [
    (
        "/**\n * Callback interface that can be implemented by beans wishing to customize\n"
        " * the {@link org.redisson.api.RedissonClient} auto configuration\n *\n"
        " * @author Nikos Kakavas (https://github.com/nikakis)\n */",
        "/**\n"
        " * 回调接口：允许 Bean 在自动配置阶段定制 {@link org.redisson.api.RedissonClient}。\n"
        " * <p>实现类注册为 Spring Bean 后，会在 {@code RedissonClient} 创建前依次调用 {@link #customize(Config)}。\n"
        " *\n"
        " * @author Nikos Kakavas (https://github.com/nikakis)\n"
        " */",
    ),
    (
        "    /**\n     * Customize the RedissonClient configuration.\n"
        "     * @param configuration the {@link Config} to customize\n     */",
        "    /**\n"
        "     * 定制 Redisson 客户端配置。\n"
        "     * @param configuration 待修改的 {@link Config}\n"
        "     */",
    ),
]

W9A_REPLACEMENTS["RedissonAutoConfigurationV2.java"] = [
    (
        "/**\n * Spring configuration used with Spring Boot 2.7 - 3.5\n *\n"
        " * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Spring Boot 2.7–3.5 使用的 Redisson 自动配置入口。\n"
        " * <p>继承 {@link RedissonAutoConfiguration} 的全部 Bean 定义；\n"
        " * 通过 {@link org.springframework.boot.autoconfigure.AutoConfiguration} 参与 Boot 3 自动配置发现。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
]

W9A_REPLACEMENTS["RedissonAutoConfigurationV4.java"] = [
    (
        "/**\n * Spring configuration used with Spring Boot 4.0+\n *\n"
        " * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Spring Boot 4.0+ 使用的 Redisson 自动配置。\n"
        " * <p>基于 {@link org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration}\n"
        " * 的连接详情与 SSL 捆绑包构建 {@link RedissonClient}，并注册 Spring Data Redis 兼容 Bean。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public static final String[] EMPTY = {};",
        "    /** 空节点数组占位符，哨兵/集群未配置时使用。 */\n"
        "    public static final String[] EMPTY = {};",
    ),
    (
        "    @Bean\n    @ConditionalOnMissingBean(name = \"redisTemplate\")\n"
        "    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {",
        "    /** 注册默认 {@link RedisTemplate}（若应用未自定义）。 */\n"
        "    @Bean\n"
        "    @ConditionalOnMissingBean(name = \"redisTemplate\")\n"
        "    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {",
    ),
    (
        "    @Bean\n    @ConditionalOnMissingBean(RedisConnectionFactory.class)\n"
        "    public RedissonConnectionFactory redissonConnectionFactory(RedissonClient redisson) {",
        "    /** 将 {@link RedissonClient} 包装为 Spring Data {@link RedisConnectionFactory}。 */\n"
        "    @Bean\n"
        "    @ConditionalOnMissingBean(RedisConnectionFactory.class)\n"
        "    public RedissonConnectionFactory redissonConnectionFactory(RedissonClient redisson) {",
    ),
    (
        "    @Bean(destroyMethod = \"shutdown\")\n"
        "    @ConditionalOnMissingBean(RedissonClient.class)\n"
        "    public RedissonClient redisson() throws IOException {",
        "    /**\n"
        "     * 创建 {@link RedissonClient}：优先 YAML 配置，否则从 Spring Data Redis 属性推导。\n"
        "     * <p>支持单机、哨兵、集群；创建前应用全部 {@link RedissonAutoConfigurationCustomizer}。\n"
        "     */\n"
        "    @Bean(destroyMethod = \"shutdown\")\n"
        "    @ConditionalOnMissingBean(RedissonClient.class)\n"
        "    public RedissonClient redisson() throws IOException {",
    ),
    (
        "    private Config buildSentinelConfig(String prefix, String username, String password, int database,\n"
        "                                       String clientName, DataRedisConnectionDetails connectionDetails) {",
        "    /** 从 {@link DataRedisConnectionDetails} 或 {@link DataRedisProperties} 构建哨兵模式配置。 */\n"
        "    private Config buildSentinelConfig(String prefix, String username, String password, int database,\n"
        "                                       String clientName, DataRedisConnectionDetails connectionDetails) {",
    ),
    (
        "    private Config buildClusterConfig(String prefix, String username, String password,\n"
        "                                      String clientName, DataRedisConnectionDetails connectionDetails) {",
        "    /** 构建 Redis 集群模式 {@link Config}。 */\n"
        "    private Config buildClusterConfig(String prefix, String username, String password,\n"
        "                                      String clientName, DataRedisConnectionDetails connectionDetails) {",
    ),
    (
        "    private Config buildSingleServerConfig(String prefix, String username, String password, int database,\n"
        "                                           String clientName, DataRedisConnectionDetails connectionDetails) {",
        "    /** 构建单机模式 {@link Config}。 */\n"
        "    private Config buildSingleServerConfig(String prefix, String username, String password, int database,\n"
        "                                           String clientName, DataRedisConnectionDetails connectionDetails) {",
    ),
    (
        "    private void setTimeouts(BaseConfig c) {",
        "    /** 将 Spring Data 连接/命令超时映射到 Redisson {@link BaseConfig}。 */\n"
        "    private void setTimeouts(BaseConfig c) {",
    ),
    (
        "    private void initSSL(Config config) {",
        "    /** 若配置了 SSL bundle，则注入信任库与密钥库到 {@link Config}。 */\n"
        "    private void initSSL(Config config) {",
    ),
    (
        "    @SuppressWarnings(\"IllegalCatch\")\n"
        "    private String[] convertNodes(String prefix, List<?> nodesObject) {",
        "    /** 通过 MethodHandle 读取 host/port（兼容 JDK 8 record 编译产物）。 */\n"
        "    @SuppressWarnings(\"IllegalCatch\")\n"
        "    private String[] convertNodes(String prefix, List<?> nodesObject) {",
    ),
    (
        "            // fixes JDK 8 record type compilation error",
        "            // 兼容 JDK 8 record 类型编译：MethodHandle 反射 host/port。",
    ),
]

W9A_REPLACEMENTS["RedissonProperties.java"] = [
    (
        "/**\n *\n * @author Nikita Koksharov\n"
        " * @author AnJia (https://anjia0532.github.io/)\n *\n */",
        "/**\n"
        " * {@code spring.redis.redisson.*} 扩展属性：内联 YAML 或外部配置文件路径。\n"
        " * <p>与 Spring Boot 标准 {@code spring.redis.*} 并存；二者同时存在时 YAML/文件优先。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " * @author AnJia (https://anjia0532.github.io/)\n"
        " */",
    ),
    (
        "    private String config;",
        "    /** 内联 Redisson YAML 配置字符串。 */\n"
        "    private String config;",
    ),
    (
        "    private String file;",
        "    /** Redisson 配置文件路径（Spring {@link org.springframework.core.io.Resource} 格式）。 */\n"
        "    private String file;",
    ),
]

# --- spring-cache ---
W9A_REPLACEMENTS["CacheConfig.java"] = [
    (
        "/**\n * Cache config object used for Spring cache configuration.\n *\n"
        " * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Spring Cache 集成用的 Redisson 缓存配置对象。\n"
        " * <p>支持 TTL、max-idle、容量上限与 {@link org.redisson.api.map.event.MapEntryListener}。\n"
        " * 可通过静态 {@link #fromYAML} 方法从 YAML 批量加载。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    /**\n     * Creates config object with\n"
        "     * <code>ttl = 0</code> and <code>maxIdleTime = 0</code>.\n     *\n     */",
        "    /** 默认构造：{@code ttl=0}、{@code maxIdleTime=0}（条目永不过期）。 */\n"
        "    /**\n     * Creates config object with\n"
        "     * <code>ttl = 0</code> and <code>maxIdleTime = 0</code>.\n     *\n     */",
    ),
    (
        "    /**\n     * Creates config object.\n     *\n"
        "     * @param ttl - time to live for key\\value entry in milliseconds.\n"
        "     *              If <code>0</code> then time to live doesn't affect entry expiration.\n"
        "     * @param maxIdleTime - max idle time for key\\value entry in milliseconds.\n"
        "     * <p>\n"
        "     * if <code>maxIdleTime</code> and <code>ttl</code> params are equal to <code>0</code>\n"
        "     * then entry stores infinitely.\n     */",
        "    /**\n"
        "     * 指定 TTL 与 max-idle 构造配置。\n"
        "     * @param ttl 条目存活时间（毫秒）；{@code 0} 表示不按 TTL 过期\n"
        "     * @param maxIdleTime 最大空闲时间（毫秒）；与 ttl 均为 {@code 0} 时条目永久保留\n"
        "     */",
    ),
    (
        "    /**\n     * Set time to live for key\\value entry in milliseconds.\n     *\n"
        "     * @param ttl - time to live for key\\value entry in milliseconds.\n"
        "     *              If <code>0</code> then time to live doesn't affect entry expiration.\n     */",
        "    /**\n"
        "     * 设置条目 TTL（毫秒）。\n"
        "     * @param ttl 存活时间；{@code 0} 表示 TTL 不参与过期\n"
        "     */",
    ),
    (
        "    /**\n     * Set max size of map. Superfluous elements are evicted using LRU algorithm.\n     *\n"
        "     * @param maxSize - max size\n"
        "     *                  If <code>0</code> the cache is unbounded (default).\n     */",
        "    /**\n"
        "     * 设置 Map 最大容量；超出时按 LRU 淘汰。\n"
        "     * @param maxSize 上限；{@code 0} 表示无界（默认）\n"
        "     */",
    ),
    (
        "    /**\n     * Set the eviction mode of the map. Superfluous elements are evicted using LRU or LFU algorithm.\n     *\n"
        "     * @param evictionMode - eviction mode (LRU, LFU)\n     * @return\n     */",
        "    /**\n"
        "     * 设置淘汰算法（{@link org.redisson.api.EvictionMode#LRU} 或 LFU）。\n"
        "     * @param evictionMode 淘汰模式\n"
        "     * @return 当前实例（链式调用）\n"
        "     */",
    ),
    (
        "    /**\n     * listener will invoke if one of the ttl,maxIdleTime,maxSize is set\n"
        "     * listener Is one of the following implementations:\n"
        "     * EntryCreatedListener\n     * EntryExpiredListener\n     * EntryRemovedListener\n"
        "     * EntryUpdatedListener\n     *\n     * @param listener listener\n     */",
        "    /**\n"
        "     * 注册 Map 事件监听器（ttl/maxIdleTime/maxSize 任一非零时生效）。\n"
        "     * <p>listener 可为 EntryCreated/Expired/Removed/Updated 等实现。\n"
        "     * @param listener 监听器实例\n"
        "     */",
    ),
    (
        "    /**\n     * Read config objects stored in YAML format from <code>String</code>\n     *\n"
        "     * @param content of config\n     * @return config\n     * @throws IOException error\n     */",
        "    /**\n"
        "     * 从 YAML 字符串解析缓存名 → {@link CacheConfig} 映射。\n"
        "     * @param content YAML 文本\n"
        "     * @return 配置映射\n"
        "     * @throws IOException 解析失败\n"
        "     */",
    ),
    (
        "    /**\n     * Convert current configuration to YAML format\n     *\n     * @param config map\n"
        "     * @return yaml string\n     * @throws IOException error\n     */",
        "    /**\n"
        "     * 将配置映射序列化为 YAML 字符串。\n"
        "     * @param config 缓存名 → 配置\n"
        "     * @return YAML 文本\n"
        "     * @throws IOException 序列化失败\n"
        "     */",
    ),
]

W9A_REPLACEMENTS["CacheConfigSupport.java"] = [
    (
        "/**\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * {@link CacheConfig} 的 YAML 读写支持：SnakeYAML 解析、Jodd Bean 属性绑定与监听器实例化。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "        yamlLoaderOptions.setTagInspector(tag -> true); // Allow all tags",
        "        yamlLoaderOptions.setTagInspector(tag -> true); // 允许所有 YAML 标签（含自定义类名）",
    ),
    (
        "    private static class CustomConstructor extends Constructor {",
        "    /** 扩展 SnakeYAML 构造器：从 tag 解析完整 Java 类名。 */\n"
        "    private static class CustomConstructor extends Constructor {",
    ),
    (
        "    public static <T> Map<String, T> fromMap(Map<String, Map<String, Object>> configMap, Class<T> clazz) {",
        "    /** 将嵌套 Map 反射实例化为指定类型并填充属性（含 listeners 特殊处理）。 */\n"
        "    public static <T> Map<String, T> fromMap(Map<String, Map<String, Object>> configMap, Class<T> clazz) {",
    ),
    (
        "    public static Object createListener(String className) {",
        "    /** 按类名无参构造实例化 {@link org.redisson.api.map.event.MapEntryListener}。 */\n"
        "    public static Object createListener(String className) {",
    ),
    (
        "    public String toYAML(Map<String, ? extends CacheConfig> configs) {",
        "    /** 将 {@link CacheConfig} 映射导出为 YAML（listeners 字段省略）。 */\n"
        "    public String toYAML(Map<String, ? extends CacheConfig> configs) {",
    ),
]

W9A_REPLACEMENTS["NullValue.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 表示缓存中的 {@code null} 值：Spring Cache 不允许直接存 null，\n"
        " * 使用 {@link #INSTANCE} 单例占位并在读取时还原为 {@code null}。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public static final NullValue INSTANCE = new NullValue();",
        "    /** 全局单例占位符。 */\n"
        "    public static final NullValue INSTANCE = new NullValue();",
    ),
]

W9A_REPLACEMENTS["RedissonCache.java"] = [
    (
        "/**\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 基于 Redisson {@link RMap}/{@link RMapCache} 的 Spring {@link org.springframework.cache.Cache} 实现。\n"
        " * <p>支持 TTL、max-idle、null 值占位、同步/异步 {@code retrieve} 及命中/未命中统计。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public RedissonCache(RMapCache<Object, Object> mapCache, CacheConfig config, boolean allowNullValues) {",
        "    /** 带过期策略的 MapCache 构造（绑定 {@link CacheConfig}）。 */\n"
        "    public RedissonCache(RMapCache<Object, Object> mapCache, CacheConfig config, boolean allowNullValues) {",
    ),
    (
        "    public RedissonCache(RMap<Object, Object> map, boolean allowNullValues) {",
        "    /** 无 TTL 的普通 {@link RMap} 构造。 */\n"
        "    public RedissonCache(RMap<Object, Object> map, boolean allowNullValues) {",
    ),
    (
        "    @Override\n    public ValueWrapper get(Object key) {",
        "    /** 读取缓存；统计 hit/miss 并包装 {@link NullValue}。 */\n"
        "    @Override\n"
        "    public ValueWrapper get(Object key) {",
    ),
    (
        "    @Override\n    public void put(Object key, Object value) {",
        "    /** 写入条目；不允许 null 时转为删除；MapCache 附带 TTL/max-idle。 */\n"
        "    @Override\n"
        "    public void put(Object key, Object value) {",
    ),
    (
        "    public ValueWrapper putIfAbsent(Object key, Object value) {",
        "    /** 键不存在时写入并返回先前值（包装后）。 */\n"
        "    public ValueWrapper putIfAbsent(Object key, Object value) {",
    ),
    (
        "    public CompletableFuture<?> retrieve(Object key) {",
        "    /** 异步读取（无 loader）；未命中返回 completed null。 */\n"
        "    public CompletableFuture<?> retrieve(Object key) {",
    ),
    (
        "    public <T> CompletableFuture<T> retrieve(Object key, Supplier<CompletableFuture<T>> valueLoader) {",
        "    /** 异步读取；未命中时加锁加载并写入缓存（防击穿）。 */\n"
        "    public <T> CompletableFuture<T> retrieve(Object key, Supplier<CompletableFuture<T>> valueLoader) {",
    ),
    (
        "    public <T> T get(Object key, Callable<T> valueLoader) {",
        "    /** 同步读取；未命中时加锁调用 {@code valueLoader} 并回填。 */\n"
        "    public <T> T get(Object key, Callable<T> valueLoader) {",
    ),
    (
        "    private <V> V get(RFuture<V> future) {",
        "    /** 在 Netty 线程外同步等待 {@link RFuture}；Netty 线程内禁止调用。 */\n"
        "    private <V> V get(RFuture<V> future) {",
    ),
    (
        "    /** The number of get requests that were satisfied by the cache.\n"
        "     * @return the number of hits\n     */",
        "    /** 缓存命中次数。\n"
        "     * @return 命中计数\n"
        "     */",
    ),
    (
        "    /** A miss is a get request that is not satisfied.\n"
        "     * @return the number of misses\n     */",
        "    /** 缓存未命中次数。\n"
        "     * @return 未命中计数\n"
        "     */",
    ),
]

W9A_REPLACEMENTS["RedissonCacheMeterBinderProvider.java"] = [
    (
        "/**\n * \n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Spring Boot Actuator 缓存指标绑定提供者（Boot 2.x/3.x {@code actuate.metrics.cache} 包）。\n"
        " * <p>为 {@link RedissonCache} 实例创建 {@link RedissonCacheMetrics}。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    @Override\n    public MeterBinder getMeterBinder(RedissonCache cache, Iterable<Tag> tags) {",
        "    /** 返回绑定 hit/miss/put/eviction 指标的 {@link RedissonCacheMetrics}。 */\n"
        "    @Override\n"
        "    public MeterBinder getMeterBinder(RedissonCache cache, Iterable<Tag> tags) {",
    ),
]

W9A_REPLACEMENTS["RedissonCacheMeterBinderProviderV4.java"] = [
    (
        "/**\n * \n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Spring Boot 4.0+ 缓存指标绑定提供者（{@code org.springframework.boot.cache.metrics} 包）。\n"
        " * <p>接口签名与 Actuator 版相同，适配 Boot 4 模块化包结构。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    @Override\n    public MeterBinder getMeterBinder(RedissonCache cache, Iterable<Tag> tags) {",
        "    /** 创建 {@link RedissonCacheMetrics} 以暴露 Micrometer 缓存指标。 */\n"
        "    @Override\n"
        "    public MeterBinder getMeterBinder(RedissonCache cache, Iterable<Tag> tags) {",
    ),
]

W9A_REPLACEMENTS["RedissonCacheMetrics.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * {@link RedissonCache} 的 Micrometer {@link io.micrometer.core.instrument.binder.cache.CacheMeterBinder}。\n"
        " * <p>暴露 size、hit、miss、put、eviction 等标准缓存指标。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    /**\n     * Record metrics on a Redisson cache.\n     *\n"
        "     * @param registry - registry to bind metrics to\n"
        "     * @param cache - cache to instrument\n"
        "     * @param tags - tags to apply to all recorded metrics\n"
        "     * @return cache\n     */",
        "    /**\n"
        "     * 便捷方法：创建绑定器并注册到 {@link MeterRegistry}。\n"
        "     * @param registry 指标注册表\n"
        "     * @param cache 待监控的 {@link RedissonCache}\n"
        "     * @param tags 附加标签\n"
        "     * @return 同一 cache 实例（便于链式使用）\n"
        "     */",
    ),
    (
        "    @Override\n    protected Long size() {",
        "    /** 当前缓存条目数（底层 {@link RMap#size()}）。 */\n"
        "    @Override\n"
        "    protected Long size() {",
    ),
    (
        "    @Override\n    protected long hitCount() {",
        "    /** 命中次数，来自 {@link RedissonCache#getCacheHits()}。 */\n"
        "    @Override\n"
        "    protected long hitCount() {",
    ),
]
