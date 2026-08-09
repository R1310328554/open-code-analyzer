"""Chinese annotation replacements for Redisson 4.7.0 wave-5b [15:30]."""
from __future__ import annotations

import importlib.util
from pathlib import Path

_spec = importlib.util.spec_from_file_location(
    "wave4b_replacements_redisson",
    Path(__file__).with_name("wave4b_replacements_redisson.py"),
)
_mod = importlib.util.module_from_spec(_spec)
assert _spec.loader is not None
_spec.loader.exec_module(_mod)
_W4B = _mod.W4B_REPLACEMENTS


def _m4(name: str) -> list[tuple[str, str]]:
    """Reuse wave-4b cache/config replacements with Micronaut 4.x wording."""
    reps: list[tuple[str, str]] = []
    for old, new in _W4B[name]:
        reps.append((old, new.replace("Micronaut 2.x", "Micronaut 4.x")))
    return reps


W5B_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

W5B_REPLACEMENTS["AttributesClearMessage.java"] = _W4B["AttributesClearMessage.java"]
W5B_REPLACEMENTS["AttributesPutAllMessage.java"] = _W4B["AttributesPutAllMessage.java"]

W5B_REPLACEMENTS["SessionCreatedMessage.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 跨节点广播：通知其他 Micronaut 实例有新 Session 创建。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public SessionCreatedMessage(String nodeId, String sessionId) {",
        "    /** @param nodeId 创建 Session 的节点\n     *  @param sessionId 新 Session ID */\n"
        "    public SessionCreatedMessage(String nodeId, String sessionId) {",
    ),
]

W5B_REPLACEMENTS["SessionDestroyedMessage.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 跨节点广播：通知其他 Micronaut 实例 Session 已销毁。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public SessionDestroyedMessage(String nodeId, String sessionId) {",
        "    /** @param nodeId 销毁 Session 的节点\n     *  @param sessionId 已销毁 Session ID */\n"
        "    public SessionDestroyedMessage(String nodeId, String sessionId) {",
    ),
]

W5B_REPLACEMENTS["RedissonHttpSessionConfiguration.java"] = [
    (
        "/**\n * Micronaut Session settings.\n *\n * @author Nikita Koksharov\n */",
        "/**\n"
        " * Redisson 分布式 HTTP Session 的 Micronaut 配置（Micronaut 3.x）。\n"
        " * <p>绑定 {@code redisson.*} 前缀下的键前缀、编解码器、更新模式与集群广播开关。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public enum UpdateMode {WRITE_BEHIND, AFTER_REQUEST}",
        "    /** Session 属性持久化策略：异步写回或请求结束时批量保存。 */\n"
        "    public enum UpdateMode {WRITE_BEHIND, AFTER_REQUEST}",
    ),
    (
        "    /**\n     * Defines broadcasting of session updates across all micronaut services.\n     *\n     * @param broadcastSessionUpdates - if true then session changes are broadcasted.\n     */",
        "    /**\n"
        "     * 是否将 Session 变更广播到所有 Micronaut 服务实例。\n"
        "     * <p>启用后通过 Redis Topic 同步属性增删改。\n"
        "     *\n"
        "     * @param broadcastSessionUpdates {@code true} 时广播变更\n"
        "     */",
    ),
    (
        "    /**\n     * Defines session attributes update mode.\n     * <p>\n     * WRITE_BEHIND - session changes stored asynchronously.\n     * AFTER_REQUEST - session changes stored only on io.micronaut.session.SessionStore#save(io.micronaut.session.Session) method invocation.\n     * <p>\n     * Default is AFTER_REQUEST.\n     *\n     * @param updateMode - mode value\n     */",
        "    /**\n"
        "     * Session 属性更新模式。\n"
        "     * <p>{@link UpdateMode#WRITE_BEHIND} — 变更立即异步写入 Redis。\n"
        "     * <p>{@link UpdateMode#AFTER_REQUEST} — 仅在\n"
        "     * {@link io.micronaut.session.SessionStore#save(io.micronaut.session.Session)} 时批量持久化。\n"
        "     * <p>默认 {@link UpdateMode#AFTER_REQUEST}。\n"
        "     *\n"
        "     * @param updateMode 更新模式\n"
        "     */",
    ),
    (
        "    /**\n     * Redis data codec applied to session values.\n     * Default is Kryo5Codec codec\n     *\n     * @see org.redisson.client.codec.Codec\n     * @see org.redisson.codec.Kryo5Codec\n     *\n     * @param codec - data codec\n     * @return config\n     */",
        "    /**\n"
        "     * Session 属性值的 Redis 编解码器。\n"
        "     * <p>默认 {@link org.redisson.codec.Kryo5Codec}。\n"
        "     *\n"
        "     * @param codec 编解码器实例\n"
        "     */",
    ),
    (
        "    /**\n     * Defines string prefix applied to all objects stored in Redis.\n     *\n     * @param keyPrefix - key prefix value\n     */",
        "    /**\n"
        "     * 所有 Session 相关 Redis 键的统一前缀。\n"
        "     *\n"
        "     * @param keyPrefix 键前缀字符串\n"
        "     */",
    ),
]

W5B_REPLACEMENTS["RedissonSession.java"] = [
    (
        "/**\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 基于 Redis {@link RMap} 的 Micronaut {@link Session} 实现（Micronaut 3.x）。\n"
        " * <p>支持 {@link RedissonHttpSessionConfiguration.UpdateMode#WRITE_BEHIND} 即时异步写入\n"
        " * 与 {@link RedissonHttpSessionConfiguration.UpdateMode#AFTER_REQUEST} 请求末批量保存两种模式。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public RedissonSession(RedissonSessionStore redissonManager,\n                           String id,\n                           RedissonHttpSessionConfiguration.UpdateMode updateMode) {",
        "    /** 以零超时创建新 Session（调用 {@link #RedissonSession(RedissonSessionStore, String, UpdateMode, Duration)}）。 */\n"
        "    public RedissonSession(RedissonSessionStore redissonManager,\n"
        "                           String id,\n"
        "                           RedissonHttpSessionConfiguration.UpdateMode updateMode) {",
    ),
    (
        "    public RedissonSession(RedissonSessionStore redissonManager,\n                           String id,\n                           RedissonHttpSessionConfiguration.UpdateMode updateMode,\n                           Duration maxInactiveInterval) {",
        "    /** 绑定 SessionStore、ID、更新模式与最大非活动间隔；AFTER_REQUEST 模式下初始化变更追踪集合。 */\n"
        "    public RedissonSession(RedissonSessionStore redissonManager,\n"
        "                           String id,\n"
        "                           RedissonHttpSessionConfiguration.UpdateMode updateMode,\n"
        "                           Duration maxInactiveInterval) {",
    ),
    (
        "    @NonNull\n    @Override\n    public Instant getCreationTime() {",
        "    /** 返回 Session 创建时间（可能从 Redis 加载后覆盖内存值）。 */\n"
        "    @NonNull\n"
        "    @Override\n"
        "    public Instant getCreationTime() {",
    ),
    (
        "    @Override\n    public MutableConvertibleValues<Object> clear() {",
        "    /** 清空全部属性；非新 Session 时记录待删除并可能触发 Redis 删除。 */\n"
        "    @Override\n"
        "    public MutableConvertibleValues<Object> clear() {",
    ),
    (
        "    public CompletableFuture<Void> delete() {",
        "    /** 异步删除 Redis 中的 Session Map 与通知桶，可选广播 {@link AttributesClearMessage}。 */\n"
        "    public CompletableFuture<Void> delete() {",
    ),
    (
        "    protected void expireSession() {",
        "    /** 刷新 Session Map 与通知桶的 TTL（基于 {@link #getMaxInactiveInterval()}）。 */\n"
        "    protected void expireSession() {",
    ),
    (
        "    protected AttributesPutAllMessage createPutAllMessage(Map<CharSequence, Object> newMap) {",
        "    /** 构造批量属性写入广播消息（编码为字节数组供 Topic 传输）。 */\n"
        "    protected AttributesPutAllMessage createPutAllMessage(Map<CharSequence, Object> newMap) {",
    ),
    (
        "    @Override\n    public Session setMaxInactiveInterval(Duration duration) {",
        "    /** 设置最大非活动间隔并按更新模式写入 Redis 或暂存待保存。 */\n"
        "    @Override\n"
        "    public Session setMaxInactiveInterval(Duration duration) {",
    ),
    (
        "    private void fastPut(String name, Object value) {",
        "    /** WRITE_BEHIND 模式下异步写入单个属性并可选广播 {@link AttributeUpdateMessage}。 */\n"
        "    private void fastPut(String name, Object value) {",
    ),
    (
        "    @Override\n    public Session setLastAccessedTime(Instant instant) {",
        "    /** 更新最后访问时间并按模式持久化或暂存。 */\n"
        "    @Override\n"
        "    public Session setLastAccessedTime(Instant instant) {",
    ),
    (
        "    public void superPut(CharSequence name, Object value) {",
        "    /** 绕过变更追踪，直接写入内存属性 Map（集群同步回调使用）。 */\n"
        "    public void superPut(CharSequence name, Object value) {",
    ),
    (
        "    @Override\n    public MutableConvertibleValues<Object> put(CharSequence key, Object value) {",
        "    /** 写入属性；{@code null} 值等效于 {@link #remove(CharSequence)}。 */\n"
        "    @Override\n"
        "    public MutableConvertibleValues<Object> put(CharSequence key, Object value) {",
    ),
    (
        "    public void superRemove(CharSequence key) {",
        "    /** 绕过变更追踪，直接从内存属性 Map 移除（集群同步回调使用）。 */\n"
        "    public void superRemove(CharSequence key) {",
    ),
    (
        "    @Override\n    public MutableConvertibleValues<Object> remove(CharSequence key) {",
        "    /** 移除属性并按模式异步删除 Redis 键或记录待删除集合。 */\n"
        "    @Override\n"
        "    public MutableConvertibleValues<Object> remove(CharSequence key) {",
    ),
    (
        "    public CompletableFuture<RedissonSession> save() {",
        "    /** 将内存变更批量写入 Redis；新 Session 或 WRITE_BEHIND 模式写入全量属性。 */\n"
        "    public CompletableFuture<RedissonSession> save() {",
    ),
    (
        "    public void load(Map<CharSequence, Object> attrs) {",
        "    /** 从 Redis 读取的数据填充 Session（剥离元数据键后加载业务属性）。 */\n"
        "    public void load(Map<CharSequence, Object> attrs) {",
    ),
]

W5B_REPLACEMENTS["RedissonSessionStore.java"] = [
    (
        "/**\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 基于 Redisson 的 Micronaut {@link SessionStore} 实现（Micronaut 3.x）。\n"
        " * <p>Session 数据存于 Redis {@link RMap}；监听键过期/删除与创建 Topic，\n"
        " * 可选跨节点广播属性变更。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public static final String ENABLED = SessionSettings.HTTP + \".redisson.enabled\";",
        "    /** 启用 Redisson Session 的配置键：{@code micronaut.session.http.redisson.enabled}。 */\n"
        "    public static final String ENABLED = SessionSettings.HTTP + \".redisson.enabled\";",
    ),
    (
        "    public RedissonSessionStore(\n            RedissonClient redisson,\n            SessionIdGenerator sessionIdGenerator,\n            RedissonHttpSessionConfiguration sessionConfiguration,\n            ApplicationEventPublisher eventPublisher) {",
        "    /** 注册 Redis 键事件与 Session 创建监听器；启用广播时订阅属性更新 Topic。 */\n"
        "    public RedissonSessionStore(\n"
        "            RedissonClient redisson,\n"
        "            SessionIdGenerator sessionIdGenerator,\n"
        "            RedissonHttpSessionConfiguration sessionConfiguration,\n"
        "            ApplicationEventPublisher eventPublisher) {",
    ),
    (
        "    String getEventsChannelPrefix() {",
        "    /** Session 创建事件 Topic 的 Redis 通道名前缀。 */\n    String getEventsChannelPrefix() {",
    ),
    (
        "    String getExpiredKeyPrefix() {",
        "    /** Session 过期通知键前缀（预留扩展）。 */\n    String getExpiredKeyPrefix() {",
    ),
    (
        "    @Override\n    public RedissonSession newSession() {",
        "    /** 生成新 ID 并构造 {@link RedissonSession}。 */\n"
        "    @Override\n"
        "    public RedissonSession newSession() {",
    ),
    (
        "    @Override\n    public CompletableFuture<Optional<RedissonSession>> findSession(String id) {",
        "    /** 从 Redis 加载 Session；不存在或已过期返回空 Optional。 */\n"
        "    @Override\n"
        "    public CompletableFuture<Optional<RedissonSession>> findSession(String id) {",
    ),
    (
        "    @Override\n    public CompletableFuture<Boolean> deleteSession(String id) {",
        "    /** 加载并调用 {@link RedissonSession#delete()}；Session 不存在返回 {@code false}。 */\n"
        "    @Override\n"
        "    public CompletableFuture<Boolean> deleteSession(String id) {",
    ),
    (
        "    @Override\n    public CompletableFuture<RedissonSession> save(RedissonSession session) {",
        "    /** 持久化 Session；新 Session 额外发布创建 Topic 消息。 */\n"
        "    @Override\n"
        "    public CompletableFuture<RedissonSession> save(RedissonSession session) {",
    ),
    (
        "    @Override\n    public void onMessage(CharSequence pattern, CharSequence channel, String body) {",
        "    /** 处理 Redis {@code __keyevent@*:del/expired} 通知，发布 Session 删除/过期事件。 */\n"
        "    @Override\n"
        "    public void onMessage(CharSequence pattern, CharSequence channel, String body) {",
    ),
    (
        "    private CompletableFuture<Optional<RedissonSession>> loadSession(String id, boolean useExpired) {",
        "    /** 异步读取 Redis Map 并构造 Session；{@code useExpired} 为 true 时不校验过期。 */\n"
        "    private CompletableFuture<Optional<RedissonSession>> loadSession(String id, boolean useExpired) {",
    ),
    (
        "    @Override\n    public void onMessage(CharSequence channel, String id) {",
        "    /** 处理 Session 创建 Topic 消息，发布 {@link SessionCreatedEvent}。 */\n"
        "    @Override\n"
        "    public void onMessage(CharSequence channel, String id) {",
    ),
    (
        "    public RTopic getTopic() {",
        "    /** 获取 Session 属性变更广播 Topic（支持分片 Topic）。 */\n    public RTopic getTopic() {",
    ),
    (
        "    private RTopic getTopic(String name, Codec codec) {",
        "    /** 按名称创建 Topic；Redis 支持分片时使用 {@link RShardedTopic}。 */\n"
        "    private RTopic getTopic(String name, Codec codec) {",
    ),
    (
        "    public String getNodeId() {",
        "    /** 本 Micronaut 实例的唯一节点 ID（过滤自身广播消息）。 */\n    public String getNodeId() {",
    ),
    (
        "    public RBatch createBatch() {",
        "    /** 创建 Redisson 批处理以合并 Session 写操作。 */\n    public RBatch createBatch() {",
    ),
    (
        "    private Codec getCodec() {",
        "    /** 返回 Session 配置编解码器，未指定时使用 Redisson 全局 Codec。 */\n    private Codec getCodec() {",
    ),
    (
        "    public RMap<CharSequence, Object> getMap(String sessionId) {",
        "    /** 获取 Session 属性 Redis Map（键名含前缀与 Session ID）。 */\n"
        "    public RMap<CharSequence, Object> getMap(String sessionId) {",
    ),
    (
        "    public RBucket<Integer> getNotificationBucket(String sessionId) {",
        "    /** 获取 Session 生命周期通知桶（配合 Redis 键事件触发 Micronaut 事件）。 */\n"
        "    public RBucket<Integer> getNotificationBucket(String sessionId) {",
    ),
]

_attr_msg = []
for old, new in _W4B["AttributeMessage.java"]:
    _attr_msg.append((old, new.replace("Micronaut 2.x", "Micronaut 4.x")))
W5B_REPLACEMENTS["AttributeMessage.java"] = _attr_msg

W5B_REPLACEMENTS["RedissonConfiguration.java"] = [
    (
        "/**\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Micronaut {@link ConfigurationProperties} 绑定的 Redisson {@link Config}（Micronaut 4.x）。\n"
        " * <p>构造时扫描 {@code redisson.*} 属性自动选择服务器模式并绑定子配置。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    @Inject\n    public RedissonConfiguration(PropertySourcePropertyResolver propertyResolver) {",
        "    /** 根据 application 配置初始化部署模式与 codec/nettyHook 等扩展组件。 */\n"
        "    @Inject\n"
        "    public RedissonConfiguration(PropertySourcePropertyResolver propertyResolver) {",
    ),
    (
        "    @Override\n    @ConfigurationBuilder(\"singleServerConfig\")\n    public SingleServerConfig getSingleServerConfig() {",
        "    /** Micronaut 绑定入口：{@code redisson.single-server-config.*}。 */\n"
        "    @Override\n"
        "    @ConfigurationBuilder(\"singleServerConfig\")\n"
        "    public SingleServerConfig getSingleServerConfig() {",
    ),
    (
        "    @Override\n    @ConfigurationBuilder(value = \"clusterServersConfig\")\n    public ClusterServersConfig getClusterServersConfig() {",
        "    /** Micronaut 绑定入口：{@code redisson.cluster-servers-config.*}。 */\n"
        "    @Override\n"
        "    @ConfigurationBuilder(value = \"clusterServersConfig\")\n"
        "    public ClusterServersConfig getClusterServersConfig() {",
    ),
    (
        "    @Override\n    @ConfigurationBuilder(value = \"replicatedServersConfig\")\n    public ReplicatedServersConfig getReplicatedServersConfig() {",
        "    /** Micronaut 绑定入口：{@code redisson.replicated-servers-config.*}。 */\n"
        "    @Override\n"
        "    @ConfigurationBuilder(value = \"replicatedServersConfig\")\n"
        "    public ReplicatedServersConfig getReplicatedServersConfig() {",
    ),
    (
        "    @Override\n    @ConfigurationBuilder(value = \"sentinelServersConfig\")\n    public SentinelServersConfig getSentinelServersConfig() {",
        "    /** Micronaut 绑定入口：{@code redisson.sentinel-servers-config.*}。 */\n"
        "    @Override\n"
        "    @ConfigurationBuilder(value = \"sentinelServersConfig\")\n"
        "    public SentinelServersConfig getSentinelServersConfig() {",
    ),
    (
        "    @Override\n    @ConfigurationBuilder(value = \"masterSlaveServersConfig\")\n    public MasterSlaveServersConfig getMasterSlaveServersConfig() {",
        "    /** Micronaut 绑定入口：{@code redisson.master-slave-servers-config.*}。 */\n"
        "    @Override\n"
        "    @ConfigurationBuilder(value = \"masterSlaveServersConfig\")\n"
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

W5B_REPLACEMENTS["RedissonFactory.java"] = _m4("RedissonFactory.java")
W5B_REPLACEMENTS["BaseCacheConfiguration.java"] = _W4B["BaseCacheConfiguration.java"]
W5B_REPLACEMENTS["RedissonAsyncCache.java"] = _W4B["RedissonAsyncCache.java"]
W5B_REPLACEMENTS["RedissonCacheConfiguration.java"] = _W4B["RedissonCacheConfiguration.java"]
W5B_REPLACEMENTS["RedissonCacheNativeConfiguration.java"] = _W4B["RedissonCacheNativeConfiguration.java"]

_sync = []
for old, new in _W4B["RedissonSyncCache.java"]:
    _sync.append((old.replace("ConversionService<?>", "ConversionService"), new))
W5B_REPLACEMENTS["RedissonSyncCache.java"] = _sync
