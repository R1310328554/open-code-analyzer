"""Chinese annotation replacements for Redisson 4.7.0 wave-8a quarkus-30/33 [0:15]."""
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


def _adapt_q30(reps: list[tuple[str, str]]) -> list[tuple[str, str]]:
    return [
        (
            old,
            new.replace("Quarkus 1.6", "Quarkus 3.0")
            .replace("Quarkus 2.0", "Quarkus 3.0")
            .replace("ShutdownConfig", "quarkus.shutdown.timeout"),
        )
        for old, new in reps
    ]


W8A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

# --- quarkus-30 cache runtime config ---
W8A_REPLACEMENTS["RedissonCachesConfig.java"] = [
    (
        "/**\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Quarkus Cache 扩展的 Redisson 缓存运行时配置（{@code cache.redisson.*}）。\n"
        " * <p>{@link #defaultConfig()} 为全局默认；{@link #cachesConfig()} 按缓存名覆盖（优先级更高）。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    /**\n     * Default configuration applied to all Redis caches (lowest precedence)\n     */",
        "    /** 应用于所有 Redis 缓存的默认配置（优先级最低）。 */\n"
        "    /**\n     * Default configuration applied to all Redis caches (lowest precedence)\n     */",
    ),
    (
        "    /**\n     * Additional configuration applied to a specific Redis cache (highest precedence)\n     */",
        "    /** 按缓存名附加的配置（优先级最高，覆盖 default）。 */\n"
        "    /**\n     * Additional configuration applied to a specific Redis cache (highest precedence)\n     */",
    ),
    (
        "        /**\n         * Specifies maximum size of this cache.\n"
        "         * Superfluous elements are evicted using LRU algorithm.\n"
        "         * If <code>0</code> the cache is unbounded (default).\n         */",
        "        /** 缓存最大条目数；超出时按 LRU 淘汰。{@code 0} 表示无上限（默认）。 */\n"
        "        /**\n         * Specifies maximum size of this cache.\n"
        "         * Superfluous elements are evicted using LRU algorithm.\n"
        "         * If <code>0</code> the cache is unbounded (default).\n         */",
    ),
    (
        "        /**\n         * Specifies that each entry should be automatically removed from the cache once a fixed duration has elapsed after\n"
        "         * the entry's creation, or the most recent replacement of its value.\n         */",
        "        /** 写入后固定时长过期（TTL，对应 expireAfterWrite）。 */\n"
        "        /**\n         * Specifies that each entry should be automatically removed from the cache once a fixed duration has elapsed after\n"
        "         * the entry's creation, or the most recent replacement of its value.\n         */",
    ),
    (
        "        /**\n         * Specifies that each entry should be automatically removed from the cache once a fixed duration has elapsed after\n"
        "         * the last access of its value.\n         */",
        "        /** 最后一次访问后固定时长过期（max-idle，对应 expireAfterAccess）。 */\n"
        "        /**\n         * Specifies that each entry should be automatically removed from the cache once a fixed duration has elapsed after\n"
        "         * the last access of its value.\n         */",
    ),
    (
        "        /**\n         * Specifies the cache implementation.\n         */",
        "        /** 缓存底层实现类型（STANDARD MapCache 或 NATIVE MapCacheNative）。 */\n"
        "        /**\n         * Specifies the cache implementation.\n         */",
    ),
]

# --- quarkus-30 CDI deployment (adapt wave-6b processor) ---
W8A_REPLACEMENTS["QuarkusRedissonClientProcessor.java"] = _adapt_q30(
    _w6b.W6B_REPLACEMENTS["QuarkusRedissonClientProcessor.java"]
)
W8A_REPLACEMENTS["RedissonClientItemBuild.java"] = _w6b.W6B_REPLACEMENTS[
    "RedissonClientItemBuild.java"
]

# --- quarkus-30 integration tests (CDI client resource + helpers) ---
W8A_REPLACEMENTS["QuarkusRedissonClientResource.java"] = [
    (
        "@Path(\"/quarkus-redisson-client\")\npublic class QuarkusRedissonClientResource {",
        "/**\n"
        " * Quarkus 3.0 Redisson 客户端集成测试 REST 资源。\n"
        " * <p>覆盖 {@link RMap}、{@link RRemoteService}、{@link RScheduledExecutorService}、\n"
        " * 响应式 {@link RBucketReactive} 等典型用法的 HTTP 端点。\n"
        " */\n"
        "@Path(\"/quarkus-redisson-client\")\n"
        "public class QuarkusRedissonClientResource {",
    ),
    (
        "    @Inject\n    RedissonClient redisson;",
        "    /** 注入的 Redisson 客户端（由扩展 CDI 生产者提供）。 */\n"
        "    @Inject\n"
        "    RedissonClient redisson;",
    ),
    (
        "    @GET\n    @Path(\"/map\")\n    public String map() {",
        "    /** 测试 {@link RMap} 读写：写入键 {@code \"1\"} 并返回其值。 */\n"
        "    @GET\n"
        "    @Path(\"/map\")\n"
        "    public String map() {",
    ),
    (
        "    @GET\n    @Path(\"/remoteService\")\n    public String remoteService() {",
        "    /** 测试 {@link RRemoteService}：注册 {@link RemService} 实现并远程调用。 */\n"
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
        "    /** 测试 {@link RScheduledExecutorService}：提交 {@link Task} 并同步等待结果。 */\n"
        "    @GET\n"
        "    @Path(\"/executeTask\")\n"
        "    public String executeTask() throws ExecutionException, InterruptedException {",
    ),
    (
        "    @GET\n    @Path(\"/bucket\")\n    public Uni<String> getBucket(){",
        "    /** 测试响应式 {@link RBucketReactive}：写入 {@code \"world\"} 后读取值。 */\n"
        "    @GET\n"
        "    @Path(\"/bucket\")\n"
        "    public Uni<String> getBucket(){",
    ),
    (
        "    @GET\n    @Path(\"/delBucket\")\n    public Uni<Boolean> deleteBucket(){",
        "    /** 测试响应式 Bucket 删除：写入后调用 {@code delete()} 并返回结果。 */\n"
        "    @GET\n"
        "    @Path(\"/delBucket\")\n"
        "    public Uni<Boolean> deleteBucket(){",
    ),
]

W8A_REPLACEMENTS["RemService.java"] = _w7a.W7A_REPLACEMENTS["RemService.java"]
W8A_REPLACEMENTS["RemoteServiceImpl.java"] = _w7a.W7A_REPLACEMENTS["RemoteServiceImpl.java"]
W8A_REPLACEMENTS["Task.java"] = _w7a.W7A_REPLACEMENTS["Task.java"]

# --- quarkus-30 CDI runtime ---
W8A_REPLACEMENTS["RedissonClientProducer.java"] = [
    (
        "/**\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Quarkus 3.0 CDI 生产者：从配置或 {@code redisson.yaml} 创建 {@link RedissonClient}。\n"
        " * <p>支持 MicroProfile Config 属性前缀 {@code quarkus.redisson.} 与 classpath 配置文件；\n"
        " * 应用关闭时按 {@code quarkus.shutdown.timeout} 优雅停止客户端。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    @Inject\n    @ConfigProperty(name = \"quarkus.shutdown.timeout\")\n    Optional<Duration> shutdownTimeout;",
        "    /** Quarkus 优雅关闭超时；存在时分阶段 shutdown Redisson。 */\n"
        "    @Inject\n"
        "    @ConfigProperty(name = \"quarkus.shutdown.timeout\")\n"
        "    Optional<Duration> shutdownTimeout;",
    ),
    (
        "    @Produces\n    @Singleton\n    @DefaultBean\n    public RedissonClient create() throws IOException {",
        "    /** 加载 Redisson 配置并创建单例 {@link RedissonClient}。 */\n"
        "    @Produces\n"
        "    @Singleton\n"
        "    @DefaultBean\n"
        "    public RedissonClient create() throws IOException {",
    ),
    (
        "        Optional<String> configFile = ConfigProvider.getConfig().getOptionalValue(\"quarkus.redisson.file\", String.class);",
        "        // 优先从 quarkus.redisson.file 指定路径或默认 redisson.yaml 加载。\n"
        "        Optional<String> configFile = ConfigProvider.getConfig().getOptionalValue(\"quarkus.redisson.file\", String.class);",
    ),
    (
        "        if (config == null) {",
        "        // 无 YAML 文件时，将 quarkus.redisson.* 属性聚合为 YAML 字符串。\n"
        "        if (config == null) {",
    ),
    (
        "        if (config.isBlank()) {",
        "        // 配置为空时拒绝启动，避免静默连接失败。\n"
        "        if (config.isBlank()) {",
    ),
    (
        "    @PreDestroy\n    public void close() {",
        "    /** 容器销毁时关闭 Redisson 客户端；若配置了 shutdown timeout 则分阶段优雅退出。 */\n"
        "    @PreDestroy\n"
        "    public void close() {",
    ),
]

W8A_REPLACEMENTS["RedissonClientRecorder.java"] = _w7a.W7A_REPLACEMENTS[
    "RedissonClientRecorder.java"
]
W8A_REPLACEMENTS["RedissonConfig.java"] = _w7a.W7A_REPLACEMENTS["RedissonConfig.java"]
W8A_REPLACEMENTS["ByteBuddySubstitutions.java"] = _w7a.W7A_REPLACEMENTS[
    "ByteBuddySubstitutions.java"
]
W8A_REPLACEMENTS["CodecsSubstitutions.java"] = _w7a.W7A_REPLACEMENTS[
    "CodecsSubstitutions.java"
]

# --- quarkus-33 cache deployment & integration tests (reuse wave-7b) ---
W8A_REPLACEMENTS["RedissonCacheProcessor.java"] = _w7b.W7B_REPLACEMENTS[
    "RedissonCacheProcessor.java"
]
W8A_REPLACEMENTS["CachedService.java"] = _w7b.W7B_REPLACEMENTS["CachedService.java"]
W8A_REPLACEMENTS["MyCredentialsResolver.java"] = _w7b.W7B_REPLACEMENTS[
    "MyCredentialsResolver.java"
]
