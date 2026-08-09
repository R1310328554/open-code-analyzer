"""Chinese annotation replacements for Redisson 4.7.0 wave-7b quarkus-20/30 [15:30]."""
from __future__ import annotations

W7B_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

W7B_REPLACEMENTS["RedissonClientProducer.java"] = [
    (
        "/**\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Quarkus 2.x 运行时 CDI 生产者：从配置或 {@code redisson.yaml} 构建 {@link RedissonClient}。\n"
        " * <p>优先读取 {@code quarkus.redisson.file} 指定资源；否则将 {@code quarkus.redisson.*}\n"
        " * 属性转换为 YAML 后解析。应用关闭时按 {@link ShutdownConfig} 优雅停机。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    @Produces\n    @Singleton\n    @DefaultBean\n    public RedissonClient create() throws IOException {",
        "    /** 创建默认 {@link RedissonClient} Bean；配置缺失时抛出 {@link IllegalStateException}。 */\n"
        "    @Produces\n"
        "    @Singleton\n"
        "    @DefaultBean\n"
        "    public RedissonClient create() throws IOException {",
    ),
    (
        "        if (config.trim().isEmpty()) {\n            throw new IllegalStateException(\"Redisson settings aren't defined.\");",
        "        if (config.trim().isEmpty()) {\n"
        "            // 未找到 YAML 文件且 quarkus.redisson.* 属性为空。\n"
        "            throw new IllegalStateException(\"Redisson settings aren't defined.\");",
    ),
    (
        "    @PreDestroy\n    public void close() {",
        "    /** 容器销毁时关闭 Redisson；若配置了 shutdown timeout 则分阶段等待。 */\n"
        "    @PreDestroy\n"
        "    public void close() {",
    ),
]

W7B_REPLACEMENTS["RedissonClientRecorder.java"] = [
    (
        "/**\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Quarkus 运行时 Recorder：在 RUNTIME_INIT 阶段触发 {@link RedissonClientProducer} 实例化。\n"
        " * <p>确保 CDI 容器在应用启动时完成 Redisson 客户端的 eagerly 初始化。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public void createProducer() {",
        "    /** 通过 Arc 容器获取并初始化 {@link RedissonClientProducer}。 */\n"
        "    public void createProducer() {",
    ),
]

W7B_REPLACEMENTS["RedissonConfig.java"] = [
    (
        "/**\n * Redisson config\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Quarkus {@code quarkus.redisson.*} 运行时配置映射。\n"
        " * <p>各方法返回扁平化属性 Map，供 {@link PropertiesConvertor} 转为 Redisson YAML。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    /**\n     * Common params\n     *\n     * @return params\n     */",
        "    /** 通用 Redisson 参数（{@code quarkus.redisson.*}）。 */\n"
        "    /**\n     * Common params\n     *\n     * @return params\n     */",
    ),
    (
        "    /**\n     * Single server params\n     *\n     * @return params\n     */",
        "    /** 单节点模式配置（{@code quarkus.redisson.single-server-config.*}）。 */\n"
        "    /**\n     * Single server params\n     *\n     * @return params\n     */",
    ),
    (
        "    /**\n     * Cluster servers params\n     *\n     * @return params\n     */",
        "    /** 集群模式配置（{@code quarkus.redisson.cluster-servers-config.*}）。 */\n"
        "    /**\n     * Cluster servers params\n     *\n     * @return params\n     */",
    ),
    (
        "    /**\n     * Sentinel servers params\n     *\n     * @return params\n     */",
        "    /** 哨兵模式配置（{@code quarkus.redisson.sentinel-servers-config.*}）。 */\n"
        "    /**\n     * Sentinel servers params\n     *\n     * @return params\n     */",
    ),
    (
        "    /**\n     * Replicated servers params\n     *\n     * @return params\n     */",
        "    /** 复制模式配置（{@code quarkus.redisson.replicated-servers-config.*}）。 */\n"
        "    /**\n     * Replicated servers params\n     *\n     * @return params\n     */",
    ),
    (
        "    /**\n     * Master and slave servers params\n     *\n     * @return params\n     */",
        "    /** 主从模式配置（{@code quarkus.redisson.master-slave-servers-config.*}）。 */\n"
        "    /**\n     * Master and slave servers params\n     *\n     * @return params\n     */",
    ),
]

W7B_REPLACEMENTS["ByteBuddySubstitutions.java"] = [
    (
        "@TargetClass(className = \"net.bytebuddy.description.type.TypeDescription$Generic$AnnotationReader$ForTypeVariableBoundType$OfFormalTypeVariable\")\nfinal class OfFormalTypeVariableSubstitute {",
        "/** GraalVM Native Image 下替换 ByteBuddy 形式类型变量注解解析（返回 null 避免反射失败）。 */\n"
        "@TargetClass(className = \"net.bytebuddy.description.type.TypeDescription$Generic$AnnotationReader$ForTypeVariableBoundType$OfFormalTypeVariable\")\n"
        "final class OfFormalTypeVariableSubstitute {",
    ),
    (
        "@TargetClass(className = \"net.bytebuddy.description.type.TypeDescription$Generic$AnnotationReader$ForTypeVariableBoundType\")\nfinal class ForTypeVariableBoundTypeSubstitute {",
        "/** 替换类型变量上界注解读取器，Native 构建时跳过 ByteBuddy 内部反射。 */\n"
        "@TargetClass(className = \"net.bytebuddy.description.type.TypeDescription$Generic$AnnotationReader$ForTypeVariableBoundType\")\n"
        "final class ForTypeVariableBoundTypeSubstitute {",
    ),
]

W7B_REPLACEMENTS["CodecsSubstitutions.java"] = [
    (
        "@TargetClass(className = \"org.redisson.connection.ServiceManager\")\nfinal class ServiceManagerSubstitute {",
        "/** Native Image 中禁用 IOUring EventLoop，避免与 GraalVM 不兼容。 */\n"
        "@TargetClass(className = \"org.redisson.connection.ServiceManager\")\n"
        "final class ServiceManagerSubstitute {",
    ),
    (
        "    @Substitute\n    private static EventLoopGroup createIOUringGroup(Config cfg) {",
        "    /** 替换 IOUring 组创建逻辑，Native 模式下直接抛出异常。 */\n"
        "    @Substitute\n"
        "    private static EventLoopGroup createIOUringGroup(Config cfg) {",
    ),
    (
        "@TargetClass(className = \"org.redisson.codec.JsonJacksonCodec\")\nfinal class JsonJacksonCodecSubstitute {",
        "/** 跳过 JsonJacksonCodec 启动预热，缩短 Native Image 启动时间。 */\n"
        "@TargetClass(className = \"org.redisson.codec.JsonJacksonCodec\")\n"
        "final class JsonJacksonCodecSubstitute {",
    ),
    (
        "    @Substitute\n    private void warmup() {",
        "    /** 空实现替代 Jackson 编解码器 warmup。 */\n"
        "    @Substitute\n"
        "    private void warmup() {",
    ),
]

W7B_REPLACEMENTS["RedissonCacheProcessor.java"] = [
    (
        "public class RedissonCacheProcessor {",
        "/**\n"
        " * Quarkus Cache 扩展部署处理器：注册 Redisson 缓存管理器与 Native 反射。\n"
        " * <p>构建阶段通过 {@link RedissonCacheBuildRecorder} 提供 {@link CacheManagerInfo}，\n"
        " * 并确保 {@link RedissonClient} 为不可移除 Bean。\n"
        " */\n"
        "public class RedissonCacheProcessor {",
    ),
    (
        "    @BuildStep\n    @Record(RUNTIME_INIT)\n    CacheManagerInfoBuildItem cacheManagerInfo(RedissonCacheBuildRecorder recorder) {",
        "    /** 运行时初始化：向 Quarkus Cache SPI 注册 Redisson 缓存管理器供应商。 */\n"
        "    @BuildStep\n"
        "    @Record(RUNTIME_INIT)\n"
        "    CacheManagerInfoBuildItem cacheManagerInfo(RedissonCacheBuildRecorder recorder) {",
    ),
    (
        "    @BuildStep\n    UnremovableBeanBuildItem redissonClientUnremoveable() {",
        "    /** 标记 {@link RedissonClient} 为不可移除 Bean，避免 Arc 优化剔除依赖。 */\n"
        "    @BuildStep\n"
        "    UnremovableBeanBuildItem redissonClientUnremoveable() {",
    ),
    (
        "    @BuildStep\n    void nativeImage(BuildProducer<ReflectiveClassBuildItem> producer) {",
        "    /** 为 {@link CompositeCacheKey} 注册 Native Image 方法反射。 */\n"
        "    @BuildStep\n"
        "    void nativeImage(BuildProducer<ReflectiveClassBuildItem> producer) {",
    ),
]

W7B_REPLACEMENTS["CachedService.java"] = [
    (
        "@ApplicationScoped\npublic class CachedService {",
        "/**\n"
        " * 集成测试用缓存服务：演示 {@link io.quarkus.cache.CacheResult} 与 Redisson 缓存联动。\n"
        " * <p>{@link #cache1} 与 {@link #cache2} 分别映射不同缓存命名空间。\n"
        " */\n"
        "@ApplicationScoped\n"
        "public class CachedService {",
    ),
    (
        "    @CacheResult(cacheName = CACHE1)\n    public String cache1(String key) {",
        "    /** 按字符串键缓存随机 UUID；相同键应命中 Redis 缓存。 */\n"
        "    @CacheResult(cacheName = CACHE1)\n"
        "    public String cache1(String key) {",
    ),
    (
        "    @CacheResult(cacheName = CACHE2)\n    public Long cache2(Long val) {",
        "    /** 按 Long 键缓存随机长整型；用于验证多缓存实例隔离。 */\n"
        "    @CacheResult(cacheName = CACHE2)\n"
        "    public Long cache2(Long val) {",
    ),
]

W7B_REPLACEMENTS["MyCredentialsResolver.java"] = [
    (
        "/**\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 集成测试用 {@link CredentialsResolver}：始终返回空 {@link Credentials}。\n"
        " * <p>用于验证 Quarkus Redisson 扩展在无认证 Redis 下的连接流程。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    @Override\n    public CompletionStage<Credentials> resolve(InetSocketAddress address) {",
        "    /** 忽略目标地址，返回预构造的空凭证 Future。 */\n"
        "    @Override\n"
        "    public CompletionStage<Credentials> resolve(InetSocketAddress address) {",
    ),
]

W7B_REPLACEMENTS["QuarkusRedissonClientResource.java"] = [
    (
        "@Path(\"/quarkus-redisson-client\")\npublic class QuarkusRedissonClientResource {",
        "/**\n"
        " * Quarkus Redisson 客户端集成测试 REST 资源。\n"
        " * <p>{@link #cacheResult} 验证 {@link CacheResult} 注解缓存命中后 Redis 中存在对应键。\n"
        " */\n"
        "@Path(\"/quarkus-redisson-client\")\n"
        "public class QuarkusRedissonClientResource {",
    ),
    (
        "    @GET\n    @Path(\"/cacheResult\")\n    public Boolean cacheResult() {",
        "    /** 调用 {@link CachedService} 两次相同参数，断言缓存命中且 Redis 键数量增加。 */\n"
        "    @GET\n"
        "    @Path(\"/cacheResult\")\n"
        "    public Boolean cacheResult() {",
    ),
    (
        "        assert redisson.getKeys().count() == 0;",
        "        // 测试开始前 Redis 中应无缓存键。\n"
        "        assert redisson.getKeys().count() == 0;",
    ),
    (
        "        assert redisson.getKeys().count() >= 2;",
        "        // 两次 @CacheResult 调用后至少写入两个缓存条目。\n"
        "        assert redisson.getKeys().count() >= 2;",
    ),
]

W7B_REPLACEMENTS["CacheImplementation.java"] = [
    (
        "public enum CacheImplementation {",
        "/**\n"
        " * Quarkus Redisson 缓存底层 Redis 结构实现类型。\n"
        " * <p>STANDARD/NATIVE 为开源版可用；V2、LOCALCACHE、CLUSTERED 等需 PRO 版本。\n"
        " */\n"
        "public enum CacheImplementation {",
    ),
    (
        "    STANDARD,",
        "    /** 标准 {@link RMap}/{@link RMapCache} 实现。 */\n"
        "    STANDARD,",
    ),
    (
        "    NATIVE,",
        "    /** Native Map 实现（{@link RMapCacheNative}），不支持 max-idle 与 LRU。 */\n"
        "    NATIVE,",
    ),
    (
        "    V2,",
        "    /** PRO 版 V2 结构（需联系 sales@redisson.pro）。 */\n"
        "    V2,",
    ),
    (
        "    LOCALCACHE,",
        "    /** PRO 版本地缓存层。 */\n"
        "    LOCALCACHE,",
    ),
]

W7B_REPLACEMENTS["RedissonCache.java"] = [
    (
        "/**\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 扩展 Quarkus {@link Cache} 的 Redisson 专用异步 API。\n"
        " * <p>所有方法返回 {@link Uni}，底层映射 Redisson {@code *Async} 操作。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    <K, V> Uni<V> put(K key, V value);",
        "    /** 写入键值；若配置了 TTL/max-idle 则附带过期策略。 */\n"
        "    <K, V> Uni<V> put(K key, V value);",
    ),
    (
        "    <K, V> Uni<V> putIfAbsent(K key, V value);",
        "    /** 仅当键不存在时写入并返回旧值（或 null）。 */\n"
        "    <K, V> Uni<V> putIfAbsent(K key, V value);",
    ),
    (
        "    <K, V> Uni<V> getOrDefault(K key, V defaultValue);",
        "    /** 异步读取；键缺失时返回 {@code defaultValue}。 */\n"
        "    <K, V> Uni<V> getOrDefault(K key, V defaultValue);",
    ),
]

W7B_REPLACEMENTS["RedissonCacheBuildRecorder.java"] = [
    (
        "/**\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Quarkus 构建 Recorder：根据构建期缓存名与运行时配置组装 {@link CacheManager}。\n"
        " * <p>仅在 {@code quarkus.cache.type=redisson} 且缓存启用时激活。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public CacheManagerInfo getCacheManagerSupplier() {",
        "    /** 返回 Redisson 缓存管理器供应商，供 {@link RedissonCacheProcessor} 注册。 */\n"
        "    public CacheManagerInfo getCacheManagerSupplier() {",
    ),
    (
        "            public boolean supports(Context context) {",
        "            /** 缓存已启用且类型为 redisson 时返回 true。 */\n"
        "            public boolean supports(Context context) {",
    ),
    (
        "                        // The number of caches is known at build time so we can use fixed initialCapacity and loadFactor for the caches map.",
        "                        // 构建期已知缓存数量，使用固定 initialCapacity 与 loadFactor 优化 HashMap。",
    ),
]

W7B_REPLACEMENTS["RedissonCacheImpl.java"] = [
    (
        "/**\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 基于 Redisson {@link RMap}/{@link RMapCache}/{@link RMapCacheNative} 的 Quarkus 缓存实现。\n"
        " * <p>按 {@link RedissonCacheInfo} 选择底层结构；TTL、max-idle 与 maxSize 在构造时绑定。\n"
        " * PRO 版实现（V2、LOCALCACHE 等）在未授权时抛出 {@link IllegalArgumentException}。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public RedissonCacheImpl(RedissonCacheInfo cacheInfo) {",
        "    /** 从 Arc 获取 {@link RedissonClient}，按配置选择 Map/MapCache/Native 结构。 */\n"
        "    public RedissonCacheImpl(RedissonCacheInfo cacheInfo) {",
    ),
    (
        "                if (cacheInfo.expireAfterAccess.isPresent()) {\n                    throw new IllegalArgumentException(\"expireAfterAccess isn't supported by NATIVE implementation\");",
        "                if (cacheInfo.expireAfterAccess.isPresent()) {\n"
        "                    // NATIVE 实现不支持访问后过期。\n"
        "                    throw new IllegalArgumentException(\"expireAfterAccess isn't supported by NATIVE implementation\");",
    ),
    (
        "    @Override\n    public <K, V> Uni<V> put(K key, V value) {",
        "    /** 异步写入；存在 TTL 或 max-idle 时使用 MapCache 带过期参数的 putAsync。 */\n"
        "    @Override\n"
        "    public <K, V> Uni<V> put(K key, V value) {",
    ),
    (
        "    @Override\n    public <K, V> Uni<V> get(K key, Function<K, V> valueLoader) {",
        "    /** 缓存未命中时通过 {@link RMap#computeIfAbsentAsync} 加载值。 */\n"
        "    @Override\n"
        "    public <K, V> Uni<V> get(K key, Function<K, V> valueLoader) {",
    ),
    (
        "    @Override\n    public Uni<Void> invalidateIf(Predicate<Object> predicate) {",
        "    /** 扫描全部键并按谓词批量删除匹配条目。 */\n"
        "    @Override\n"
        "    public Uni<Void> invalidateIf(Predicate<Object> predicate) {",
    ),
]

W7B_REPLACEMENTS["RedissonCacheInfo.java"] = [
    (
        "/**\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 单个 Quarkus Redisson 缓存的运行时元数据（由构建期名称 + 配置合并而成）。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    /**\n     * The maximum size of this cache.\n     * Superfluous elements are evicted using LRU algorithm.\n     */",
        "    /** 缓存最大条目数；超出时按 LRU 淘汰（仅 STANDARD MapCache 支持）。 */\n"
        "    /**\n     * The maximum size of this cache.\n     * Superfluous elements are evicted using LRU algorithm.\n     */",
    ),
    (
        "    /**\n     * The cache name\n     */",
        "    /** Quarkus 缓存逻辑名，同时作为 Redis Map 名称。 */\n"
        "    /**\n     * The cache name\n     */",
    ),
    (
        "    /**\n     * The default time to live of the item stored in the cache\n     */",
        "    /** 条目访问后过期时间（max-idle，对应 expireAfterAccess）。 */\n"
        "    /**\n     * The default time to live of the item stored in the cache\n     */",
    ),
    (
        "    /**\n     * The default time to live to add to the item once read\n     */",
        "    /** 条目写入后过期时间（TTL，对应 expireAfterWrite）。 */\n"
        "    /**\n     * The default time to live to add to the item once read\n     */",
    ),
]

W7B_REPLACEMENTS["RedissonCacheInfoBuilder.java"] = [
    (
        "/**\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 将构建期缓存名集合与 {@link RedissonCachesConfig} 合并为 {@link RedissonCacheInfo} 集合。\n"
        " * <p>命名缓存配置优先于 default 配置；未指定字段保持 {@link Optional#empty()}。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public static Set<RedissonCacheInfo> build(Set<String> cacheNames,\n                                               RedissonCachesConfig runtimeConfig) {",
        "    /** 为每个构建期注册的缓存名生成运行时 {@link RedissonCacheInfo}。 */\n"
        "    public static Set<RedissonCacheInfo> build(Set<String> cacheNames,\n"
        "                                               RedissonCachesConfig runtimeConfig) {",
    ),
    (
        "            if (namedRuntimeConfig != null && namedRuntimeConfig.implementation().isPresent()) {",
        "            // 命名缓存的 implementation 优先于全局 default。\n"
        "            if (namedRuntimeConfig != null && namedRuntimeConfig.implementation().isPresent()) {",
    ),
]
