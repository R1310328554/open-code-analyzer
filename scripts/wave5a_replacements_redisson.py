"""Chinese annotation replacements for Redisson 4.7.0 wave-5a micronaut-20 session + micronaut-30 [0:15]."""
from __future__ import annotations

import importlib.util
from pathlib import Path

_spec = importlib.util.spec_from_file_location(
    "wave4b_replacements_redisson",
    Path(__file__).with_name("wave4b_replacements_redisson.py"),
)
_w4b = importlib.util.module_from_spec(_spec)
assert _spec.loader is not None
_spec.loader.exec_module(_w4b)


def _m3(name: str) -> list[tuple[str, str]]:
    """Adapt wave-4b replacements for Micronaut 3.x module paths."""
    reps: list[tuple[str, str]] = []
    for old, new in _w4b.W4B_REPLACEMENTS[name]:
        reps.append((old, new.replace("Micronaut 2.x", "Micronaut 3.x")))
    return reps


W5A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

# --- micronaut-20 session [0:5] ---

W5A_REPLACEMENTS[
    "redisson-micronaut/redisson-micronaut-20/src/main/java/org/redisson/micronaut/session/RedissonHttpSessionConfiguration.java"
] = [
    (
        "/**\n * Micronaut Session settings.\n *\n * @author Nikita Koksharov\n */",
        "/**\n"
        " * Redisson 分布式 HTTP Session 的 Micronaut 配置（{@code redisson.*}，Micronaut 2.x）。\n"
        " * <p>扩展 {@link HttpSessionConfiguration}，支持 Redis 键前缀、编解码器、更新模式与跨节点广播。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public enum UpdateMode {WRITE_BEHIND, AFTER_REQUEST}",
        "    /** Session 属性写入 Redis 的时机策略。 */\n"
        "    public enum UpdateMode {WRITE_BEHIND, AFTER_REQUEST}",
    ),
    (
        "    /**\n     * Defines broadcasting of session updates across all micronaut services.\n     *\n     * @param broadcastSessionUpdates - if true then session changes are broadcasted.\n     */",
        "    /**\n"
        "     * 是否将 Session 变更广播到所有 Micronaut 服务实例。\n"
        "     *\n"
        "     * @param broadcastSessionUpdates 为 {@code true} 时通过 Redis Topic 同步属性变更\n"
        "     */",
    ),
    (
        "    /**\n     * Defines session attributes update mode.\n     * <p>\n     * WRITE_BEHIND - session changes stored asynchronously.\n     * AFTER_REQUEST - session changes stored only on io.micronaut.session.SessionStore#save(io.micronaut.session.Session) method invocation.\n     * <p>\n     * Default is AFTER_REQUEST.\n     *\n     * @param updateMode - mode value\n     */",
        "    /**\n"
        "     * 设置 Session 属性更新模式。\n"
        "     * <p>{@link UpdateMode#WRITE_BEHIND}：变更异步写入 Redis。\n"
        "     * <p>{@link UpdateMode#AFTER_REQUEST}：仅在\n"
        "     * {@link io.micronaut.session.SessionStore#save(io.micronaut.session.Session)} 时批量落库。\n"
        "     * <p>默认 {@link UpdateMode#AFTER_REQUEST}。\n"
        "     *\n"
        "     * @param updateMode 更新模式\n"
        "     */",
    ),
    (
        "    /**\n     * Redis data codec applied to session values.\n     * Default is Kryo5Codec codec\n     *\n     * @see org.redisson.client.codec.Codec\n     * @see org.redisson.codec.Kryo5Codec\n     *\n     * @param codec - data codec\n     * @return config\n     */",
        "    /**\n"
        "     * 设置 Session 属性值的 Redis 编解码器。\n"
        "     * <p>默认 {@link org.redisson.codec.Kryo5Codec}。\n"
        "     *\n"
        "     * @param codec 编解码器实例\n"
        "     */",
    ),
    (
        "    /**\n     * Defines string prefix applied to all objects stored in Redis.\n     *\n     * @param keyPrefix - key prefix value\n     */",
        "    /**\n"
        "     * 设置写入 Redis 时所有 Session 相关键的统一前缀。\n"
        "     *\n"
        "     * @param keyPrefix 键前缀字符串\n"
        "     */",
    ),
]

W5A_REPLACEMENTS[
    "redisson-micronaut/redisson-micronaut-20/src/main/java/org/redisson/micronaut/session/RedissonSession.java"
] = [
    (
        "/**\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 基于 Redisson {@link RMap} 的 Micronaut {@link Session} 实现（Micronaut 2.x）。\n"
        " * <p>在内存中维护属性副本，按 {@link RedissonHttpSessionConfiguration.UpdateMode}\n"
        " * 异步或请求结束时同步到 Redis；支持跨节点属性广播。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    private static final String MAX_INACTIVE_INTERVAL_ATTR = \"session:maxInactiveInterval\";",
        "    /** Redis Map 中存储最大非活动间隔（毫秒）的键名。 */\n"
        "    private static final String MAX_INACTIVE_INTERVAL_ATTR = \"session:maxInactiveInterval\";",
    ),
    (
        "    private static final String LAST_ACCESSED_TIME_ATTR = \"session:lastAccessedTime\";",
        "    /** Redis Map 中存储最后访问时间戳的键名。 */\n"
        "    private static final String LAST_ACCESSED_TIME_ATTR = \"session:lastAccessedTime\";",
    ),
    (
        "    private static final String CREATION_TIME_ATTR = \"session:creationTime\";",
        "    /** Redis Map 中存储创建时间戳的键名。 */\n"
        "    private static final String CREATION_TIME_ATTR = \"session:creationTime\";",
    ),
    (
        "    public RedissonSession(RedissonSessionStore redissonManager,\n                           String id,\n                           RedissonHttpSessionConfiguration.UpdateMode updateMode) {",
        "    /** 以默认最大非活动间隔创建 Session。 */\n"
        "    public RedissonSession(RedissonSessionStore redissonManager,\n"
        "                           String id,\n"
        "                           RedissonHttpSessionConfiguration.UpdateMode updateMode) {",
    ),
    (
        "    public RedissonSession(RedissonSessionStore redissonManager,\n                           String id,\n                           RedissonHttpSessionConfiguration.UpdateMode updateMode,\n                           Duration maxInactiveInterval) {",
        "    /** 绑定 Session Store、ID、更新模式与 TTL，并打开 Redis Map。 */\n"
        "    public RedissonSession(RedissonSessionStore redissonManager,\n"
        "                           String id,\n"
        "                           RedissonHttpSessionConfiguration.UpdateMode updateMode,\n"
        "                           Duration maxInactiveInterval) {",
    ),
    (
        "    @Override\n    public MutableConvertibleValues<Object> clear() {",
        "    /** 清空内存属性；非新 Session 时在 WRITE_BEHIND 模式下异步删除 Redis 数据。 */\n"
        "    @Override\n"
        "    public MutableConvertibleValues<Object> clear() {",
    ),
    (
        "    public CompletableFuture<Void> delete() {",
        "    /** 批量删除 Session Map、通知桶，并在启用广播时发布清空消息。 */\n"
        "    public CompletableFuture<Void> delete() {",
    ),
    (
        "    protected void expireSession() {",
        "    /** 为 Session Map 与通知桶设置与 maxInactiveInterval 一致的过期时间。 */\n"
        "    protected void expireSession() {",
    ),
    (
        "    protected AttributesPutAllMessage createPutAllMessage(Map<CharSequence, Object> newMap) {",
        "    /** 构造跨节点广播用的批量属性写入消息（含编码后的值）。 */\n"
        "    protected AttributesPutAllMessage createPutAllMessage(Map<CharSequence, Object> newMap) {",
    ),
    (
        "    private void fastPut(String name, Object value) {",
        "    /** WRITE_BEHIND 模式下异步写入单个属性，可选广播 {@link AttributeUpdateMessage}。 */\n"
        "    private void fastPut(String name, Object value) {",
    ),
    (
        "    public void superPut(CharSequence name, Object value) {",
        "    /** 绕过 Redisson 同步逻辑，直接写入内存属性 Map（供远程广播回放使用）。 */\n"
        "    public void superPut(CharSequence name, Object value) {",
    ),
    (
        "    public void superRemove(CharSequence key) {",
        "    /** 绕过 Redisson 同步逻辑，直接从内存移除属性（供远程广播回放使用）。 */\n"
        "    public void superRemove(CharSequence key) {",
    ),
    (
        "    public CompletableFuture<RedissonSession> save() {",
        "    /** 将累积的属性变更批量写入 Redis，刷新 TTL 并可选广播。 */\n"
        "    public CompletableFuture<RedissonSession> save() {",
    ),
    (
        "    public void load(Map<CharSequence, Object> attrs) {",
        "    /** 从 Redis 读出的 Map 还原元数据与业务属性到内存 Session。 */\n"
        "    public void load(Map<CharSequence, Object> attrs) {",
    ),
]

W5A_REPLACEMENTS[
    "redisson-micronaut/redisson-micronaut-20/src/main/java/org/redisson/micronaut/session/RedissonSessionStore.java"
] = [
    (
        "/**\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 基于 Redisson 的 Micronaut {@link SessionStore} 实现（Micronaut 2.x）。\n"
        " * <p>将 Session 持久化到 Redis {@link RMap}，监听键过期/删除事件并发布 Micronaut Session 生命周期事件。\n"
        " * <p>启用 {@code broadcastSessionUpdates} 时订阅属性变更 Topic 以保持多节点内存视图一致。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public static final String ENABLED = SessionSettings.HTTP + \".redisson.enabled\";",
        "    /** 启用 Redisson Session Store 的配置键：{@code micronaut.session.http.redisson.enabled}。 */\n"
        "    public static final String ENABLED = SessionSettings.HTTP + \".redisson.enabled\";",
    ),
    (
        "    public RedissonSessionStore(\n            RedissonClient redisson,\n            SessionIdGenerator sessionIdGenerator,\n            RedissonHttpSessionConfiguration sessionConfiguration,\n            ApplicationEventPublisher eventPublisher) {",
        "    /** 注册 Redis 键空间通知监听器，并在需要时订阅 Session 属性更新 Topic。 */\n"
        "    public RedissonSessionStore(\n"
        "            RedissonClient redisson,\n"
        "            SessionIdGenerator sessionIdGenerator,\n"
        "            RedissonHttpSessionConfiguration sessionConfiguration,\n"
        "            ApplicationEventPublisher eventPublisher) {",
    ),
    (
        "    @Override\n    public RedissonSession newSession() {",
        "    /** 生成新 Session ID 并构造 {@link RedissonSession}。 */\n"
        "    @Override\n"
        "    public RedissonSession newSession() {",
    ),
    (
        "    @Override\n    public CompletableFuture<Optional<RedissonSession>> findSession(String id) {",
        "    /** 从 Redis 加载 Session；不存在或已过期时返回空 Optional。 */\n"
        "    @Override\n"
        "    public CompletableFuture<Optional<RedissonSession>> findSession(String id) {",
    ),
    (
        "    @Override\n    public CompletableFuture<Boolean> deleteSession(String id) {",
        "    /** 加载并调用 {@link RedissonSession#delete()} 移除 Session。 */\n"
        "    @Override\n"
        "    public CompletableFuture<Boolean> deleteSession(String id) {",
    ),
    (
        "    @Override\n    public CompletableFuture<RedissonSession> save(RedissonSession session) {",
        "    /** 持久化 Session；新 Session 首次保存时在 created Topic 发布 ID。 */\n"
        "    @Override\n"
        "    public CompletableFuture<RedissonSession> save(RedissonSession session) {",
    ),
    (
        "    @Override\n    public void onMessage(CharSequence pattern, CharSequence channel, String body) {",
        "    /** 处理 Redis {@code __keyevent@*:del/expired}，发布 Session 删除/过期事件。 */\n"
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
        "    /** 返回 Session 属性跨节点同步 Topic（支持分片 Topic）。 */\n"
        "    public RTopic getTopic() {",
    ),
    (
        "    public RMap<CharSequence, Object> getMap(String sessionId) {",
        "    /** 获取指定 Session ID 的 Redis Map（键用 {@link StringCodec}，值用配置的 Codec）。 */\n"
        "    public RMap<CharSequence, Object> getMap(String sessionId) {",
    ),
    (
        "    public RBucket<Integer> getNotificationBucket(String sessionId) {",
        "    /** 获取 Session 过期通知用的整数桶（键空间事件携带 sessionId）。 */\n"
        "    public RBucket<Integer> getNotificationBucket(String sessionId) {",
    ),
]

W5A_REPLACEMENTS[
    "redisson-micronaut/redisson-micronaut-20/src/main/java/org/redisson/micronaut/session/SessionCreatedMessage.java"
] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 跨节点广播：新 Session 已创建（Micronaut 2.x）。\n"
        " * <p>继承 {@link AttributeMessage}，携带节点 ID 与 Session ID。\n"
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

W5A_REPLACEMENTS[
    "redisson-micronaut/redisson-micronaut-20/src/main/java/org/redisson/micronaut/session/SessionDestroyedMessage.java"
] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 跨节点广播：Session 已销毁（Micronaut 2.x）。\n"
        " * <p>继承 {@link AttributeMessage}，携带节点 ID 与 Session ID。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public SessionDestroyedMessage(String nodeId, String sessionId) {",
        "    /** @param nodeId 销毁 Session 的节点\n     *  @param sessionId 被销毁的 Session ID */\n"
        "    public SessionDestroyedMessage(String nodeId, String sessionId) {",
    ),
]

# --- micronaut-30 [5:15] — adapted from wave-4b ---

_M30 = "redisson-micronaut/redisson-micronaut-30/src/main/java/org/redisson/micronaut"

W5A_REPLACEMENTS[f"{_M30}/RedissonConfiguration.java"] = _m3("RedissonConfiguration.java")
W5A_REPLACEMENTS[f"{_M30}/RedissonFactory.java"] = _m3("RedissonFactory.java")
W5A_REPLACEMENTS[f"{_M30}/cache/BaseCacheConfiguration.java"] = _m3("BaseCacheConfiguration.java")
W5A_REPLACEMENTS[f"{_M30}/cache/RedissonAsyncCache.java"] = _m3("RedissonAsyncCache.java")
W5A_REPLACEMENTS[f"{_M30}/cache/RedissonCacheConfiguration.java"] = _m3("RedissonCacheConfiguration.java")
W5A_REPLACEMENTS[f"{_M30}/cache/RedissonCacheNativeConfiguration.java"] = _m3(
    "RedissonCacheNativeConfiguration.java"
)
W5A_REPLACEMENTS[f"{_M30}/cache/RedissonSyncCache.java"] = _m3("RedissonSyncCache.java")
W5A_REPLACEMENTS[f"{_M30}/session/AttributeMessage.java"] = _m3("AttributeMessage.java")
W5A_REPLACEMENTS[f"{_M30}/session/AttributeRemoveMessage.java"] = _m3("AttributeRemoveMessage.java")
W5A_REPLACEMENTS[f"{_M30}/session/AttributeUpdateMessage.java"] = _m3("AttributeUpdateMessage.java")
