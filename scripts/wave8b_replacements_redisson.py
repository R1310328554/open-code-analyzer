"""Chinese annotation replacements for Redisson 4.7.0 wave-8b quarkus-33 [15:30]."""
from __future__ import annotations

import importlib.util
from pathlib import Path

_spec7b = importlib.util.spec_from_file_location(
    "wave7b_replacements_redisson",
    Path(__file__).with_name("wave7b_replacements_redisson.py"),
)
_w7b = importlib.util.module_from_spec(_spec7b)
assert _spec7b.loader is not None
_spec7b.loader.exec_module(_w7b)

_spec6b = importlib.util.spec_from_file_location(
    "wave6b_replacements_redisson",
    Path(__file__).with_name("wave6b_replacements_redisson.py"),
)
_w6b = importlib.util.module_from_spec(_spec6b)
assert _spec6b.loader is not None
_spec6b.loader.exec_module(_w6b)

W8B_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

# --- cache integration-tests REST (full path avoids CDI同名冲突) ---
CACHE_IT_RESOURCE = (
    "redisson-quarkus/redisson-quarkus-33/cache/integration-tests/src/main/java/"
    "org/redisson/quarkus/client/it/QuarkusRedissonClientResource.java"
)
W8B_REPLACEMENTS[CACHE_IT_RESOURCE] = _w7b.W7B_REPLACEMENTS[
    "QuarkusRedissonClientResource.java"
]

# --- cache runtime (reuse wave-7b) ---
for _name in (
    "CacheImplementation.java",
    "RedissonCache.java",
    "RedissonCacheBuildRecorder.java",
    "RedissonCacheImpl.java",
    "RedissonCacheInfo.java",
    "RedissonCacheInfoBuilder.java",
):
    W8B_REPLACEMENTS[_name] = _w7b.W7B_REPLACEMENTS[_name]

# --- cache runtime config ---
W8B_REPLACEMENTS["RedissonCachesConfig.java"] = [
    (
        "/**\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Quarkus {@code cache.redisson.*} 运行时配置根映射。\n"
        " * <p>{@link #defaultConfig()} 为全局默认；{@link #cachesConfig()} 按缓存名覆盖。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    /**\n     * Default configuration applied to all Redis caches (lowest precedence)\n     */",
        "    /** 应用于所有 Redisson 缓存的默认配置（优先级最低）。 */\n"
        "    /**\n     * Default configuration applied to all Redis caches (lowest precedence)\n     */",
    ),
    (
        "    /**\n     * Additional configuration applied to a specific Redis cache (highest precedence)\n     */",
        "    /** 按缓存名附加的配置（优先级最高，覆盖 default）。 */\n"
        "    /**\n     * Additional configuration applied to a specific Redis cache (highest precedence)\n     */",
    ),
    (
        "        /**\n         * Specifies maximum size of this cache.\n         * Superfluous elements are evicted using LRU algorithm.\n         * If <code>0</code> the cache is unbounded (default).\n         */",
        "        /** 缓存最大条目数；超出时 LRU 淘汰；{@code 0} 表示无界（默认）。 */\n"
        "        /**\n         * Specifies maximum size of this cache.\n         * Superfluous elements are evicted using LRU algorithm.\n         * If <code>0</code> the cache is unbounded (default).\n         */",
    ),
    (
        "        /**\n         * Specifies that each entry should be automatically removed from the cache once a fixed duration has elapsed after\n         * the entry's creation, or the most recent replacement of its value.\n         */",
        "        /** 写入后过期时间（对应 {@link RedissonCacheInfo#expireAfterWrite}）。 */\n"
        "        /**\n         * Specifies that each entry should be automatically removed from the cache once a fixed duration has elapsed after\n         * the entry's creation, or the most recent replacement of its value.\n         */",
    ),
    (
        "        /**\n         * Specifies that each entry should be automatically removed from the cache once a fixed duration has elapsed after\n         * the last access of its value.\n         */",
        "        /** 访问后过期时间（max-idle，对应 {@link RedissonCacheInfo#expireAfterAccess}）。 */\n"
        "        /**\n         * Specifies that each entry should be automatically removed from the cache once a fixed duration has elapsed after\n         * the last access of its value.\n         */",
    ),
    (
        "        /**\n         * Specifies the cache implementation.\n         */",
        "        /** 底层 Redis 结构实现（{@link CacheImplementation}）。 */\n"
        "        /**\n         * Specifies the cache implementation.\n         */",
    ),
]

# --- cdi deployment (adapt wave-6b for Quarkus 3.3) ---
W8B_REPLACEMENTS["QuarkusRedissonClientProcessor.java"] = [
    (old, new.replace("Quarkus 1.6", "Quarkus 3.3"))
    for old, new in _w6b.W6B_REPLACEMENTS["QuarkusRedissonClientProcessor.java"]
]

W8B_REPLACEMENTS["RedissonClientItemBuild.java"] = _w6b.W6B_REPLACEMENTS[
    "RedissonClientItemBuild.java"
]

# --- cdi integration-tests ---
CDI_IT_RESOURCE = (
    "redisson-quarkus/redisson-quarkus-33/cdi/integration-tests/src/main/java/"
    "org/redisson/quarkus/client/it/QuarkusRedissonClientResource.java"
)
W8B_REPLACEMENTS[CDI_IT_RESOURCE] = [
    (
        "@Path(\"/quarkus-redisson-client\")\npublic class QuarkusRedissonClientResource {",
        "/**\n"
        " * Quarkus 3.3 Redisson CDI 集成测试 REST 资源。\n"
        " * <p>覆盖 {@link RMap}、{@link RRemoteService}、{@link RScheduledExecutorService}\n"
        " * 与响应式 {@link RBucketReactive} 等典型 API 的 HTTP 冒烟端点。\n"
        " */\n"
        "@Path(\"/quarkus-redisson-client\")\n"
        "public class QuarkusRedissonClientResource {",
    ),
    (
        "    @Inject\n    RedissonClient redisson;",
        "    /** 由 {@link RedissonClientProducer} 提供的 CDI {@link RedissonClient}。 */\n"
        "    @Inject\n"
        "    RedissonClient redisson;",
    ),
    (
        "    @GET\n    @Path(\"/map\")\n    public String map() {",
        "    /** 测试 {@link RMap} 基本读写。 */\n"
        "    @GET\n"
        "    @Path(\"/map\")\n"
        "    public String map() {",
    ),
    (
        "    @GET\n    @Path(\"/remoteService\")\n    public String remoteService() {",
        "    /** 注册 {@link RemService} 实现并通过 {@link RRemoteService} 远程调用。 */\n"
        "    @GET\n"
        "    @Path(\"/remoteService\")\n"
        "    public String remoteService() {",
    ),
    (
        "    @GET\n    @Path(\"/pingAll\")\n    public String pingAll() {",
        "    /** 对单机 Redis 节点执行 {@code pingAll} 连通性检测。 */\n"
        "    @GET\n"
        "    @Path(\"/pingAll\")\n"
        "    public String pingAll() {",
    ),
    (
        "    @GET\n    @Path(\"/executeTask\")\n    public String executeTask() throws ExecutionException, InterruptedException {",
        "    /** 向 {@link RScheduledExecutorService} 提交 {@link Task} 并同步等待结果。 */\n"
        "    @GET\n"
        "    @Path(\"/executeTask\")\n"
        "    public String executeTask() throws ExecutionException, InterruptedException {",
    ),
    (
        "    @GET\n    @Path(\"/bucket\")\n    public Uni<String> getBucket(){",
        "    /** 响应式写入并读取 {@link RBucketReactive}（StringCodec）。 */\n"
        "    @GET\n"
        "    @Path(\"/bucket\")\n"
        "    public Uni<String> getBucket(){",
    ),
    (
        "    @GET\n    @Path(\"/delBucket\")\n    public Uni<Boolean> deleteBucket(){",
        "    /** 写入后删除响应式 Bucket，返回删除是否成功。 */\n"
        "    @GET\n"
        "    @Path(\"/delBucket\")\n"
        "    public Uni<Boolean> deleteBucket(){",
    ),
]

W8B_REPLACEMENTS["RemService.java"] = [
    (
        "/**\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * {@link RRemoteService} 集成测试用远程服务接口。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    String executeMe();",
        "    /** 远程调用入口，返回执行结果字符串。 */\n"
        "    String executeMe();",
    ),
]

W8B_REPLACEMENTS["RemoteServiceImpl.java"] = [
    (
        "/**\n * @author Nikita Koksharov\n */",
        "/**\n"
        " * {@link RemService} 本地实现，注册到 {@link RRemoteService} 供远程代理调用。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    @Override\n    public String executeMe() {",
        "    /** 返回固定字符串 {@code \"executed\"} 表示调用成功。 */\n"
        "    @Override\n"
        "    public String executeMe() {",
    ),
]

W8B_REPLACEMENTS["Task.java"] = [
    (
        "/**\n * @author Nikita Koksharov\n */",
        "/**\n"
        " * 可序列化 {@link java.util.concurrent.Callable}，提交到分布式 Worker 执行。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    @Override\n    public String call() throws Exception {",
        "    /** Worker 节点执行后返回 {@code \"hello\"}。 */\n"
        "    @Override\n"
        "    public String call() throws Exception {",
    ),
]

# --- cdi runtime producer (Quarkus 3.3: isBlank + shutdown timeout inject) ---
W8B_REPLACEMENTS["RedissonClientProducer.java"] = [
    (
        "/**\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Quarkus 3.3 CDI 生产者：从 {@code redisson.yaml} 或 {@code quarkus.redisson.*} 构建 {@link RedissonClient}。\n"
        " * <p>应用关闭时按 {@code quarkus.shutdown.timeout} 分阶段优雅停机。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    @Produces\n    @Singleton\n    @DefaultBean\n    public RedissonClient create() throws IOException {",
        "    /** 加载配置并创建单例 {@link RedissonClient}；配置缺失时抛出 {@link IllegalStateException}。 */\n"
        "    @Produces\n"
        "    @Singleton\n"
        "    @DefaultBean\n"
        "    public RedissonClient create() throws IOException {",
    ),
    (
        "        Optional<String> configFile = ConfigProvider.getConfig().getOptionalValue(\"quarkus.redisson.file\", String.class);",
        "        // 优先读取 quarkus.redisson.file，否则默认 classpath 上的 redisson.yaml。\n"
        "        Optional<String> configFile = ConfigProvider.getConfig().getOptionalValue(\"quarkus.redisson.file\", String.class);",
    ),
    (
        "        if (config == null) {",
        "        // 无 YAML 文件时，将 quarkus.redisson.* 属性聚合为 YAML。\n"
        "        if (config == null) {",
    ),
    (
        "        if (config.isBlank()) {",
        "        // 配置为空则拒绝启动，避免静默连接失败。\n"
        "        if (config.isBlank()) {",
    ),
    (
        "    @PreDestroy\n    public void close() {",
        "    /** 容器销毁时关闭 Redisson；若注入 shutdown timeout 则分阶段等待。 */\n"
        "    @PreDestroy\n"
        "    public void close() {",
    ),
]
