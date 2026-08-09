"""Chinese annotation replacements for Redisson 4.7.0 wave-3b [15:30]."""
from __future__ import annotations

import importlib.util
from pathlib import Path

_spec = importlib.util.spec_from_file_location(
    "wave2b_replacements_redisson",
    Path(__file__).with_name("wave2b_replacements_redisson.py"),
)
_mod = importlib.util.module_from_spec(_spec)
assert _spec.loader is not None
_spec.loader.exec_module(_mod)
_W2B = _mod.W2B_REPLACEMENTS


def _h52(name: str) -> list[tuple[str, str]]:
    reps: list[tuple[str, str]] = []
    for old, new in _W2B[name]:
        reps.append(
            (
                old.replace("SessionImplementor", "SharedSessionContractImplementor"),
                new.replace("SessionImplementor", "SharedSessionContractImplementor").replace(
                    "Hibernate 5", "Hibernate 5.2"
                ),
            )
        )
    return reps


W3B_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

W3B_REPLACEMENTS["ReadWriteEntityRegionAccessStrategy.java"] = _h52(
    "ReadWriteEntityRegionAccessStrategy.java"
) + [
    (
        "    @Override\n    public Object generateCacheKey(Object id, EntityPersister persister, SessionFactoryImplementor factory, String tenantIdentifier) {",
        "    /** 通过 {@link RedissonEntityRegion} 的键工厂生成实体缓存键。 */\n"
        "    @Override\n"
        "    public Object generateCacheKey(Object id, EntityPersister persister, SessionFactoryImplementor factory, String tenantIdentifier) {",
    ),
    (
        "    @Override\n    public Object getCacheKeyId(Object cacheKey) {",
        "    /** 从缓存键反解实体标识符。 */\n    @Override\n    public Object getCacheKeyId(Object cacheKey) {",
    ),
]

W3B_REPLACEMENTS["ReadWriteNaturalIdRegionAccessStrategy.java"] = _h52(
    "ReadWriteNaturalIdRegionAccessStrategy.java"
) + [
    (
        "    @Override\n    public Object generateCacheKey(Object[] naturalIdValues, EntityPersister persister, SharedSessionContractImplementor session) {",
        "    /** 通过 {@link RedissonNaturalIdRegion} 的键工厂生成自然 ID 缓存键。 */\n"
        "    @Override\n"
        "    public Object generateCacheKey(Object[] naturalIdValues, EntityPersister persister, SharedSessionContractImplementor session) {",
    ),
    (
        "    @Override\n    public Object[] getNaturalIdValues(Object cacheKey) {",
        "    /** 从缓存键反解自然 ID 值数组。 */\n    @Override\n    public Object[] getNaturalIdValues(Object cacheKey) {",
    ),
]

W3B_REPLACEMENTS["TransactionalCollectionRegionAccessStrategy.java"] = _h52(
    "TransactionalCollectionRegionAccessStrategy.java"
) + [
    (
        "    @Override\n    public Object generateCacheKey(Object id, CollectionPersister persister, SessionFactoryImplementor factory, String tenantIdentifier) {",
        "    /** 通过 {@link RedissonCollectionRegion} 的键工厂生成集合缓存键。 */\n"
        "    @Override\n"
        "    public Object generateCacheKey(Object id, CollectionPersister persister, SessionFactoryImplementor factory, String tenantIdentifier) {",
    ),
    (
        "    @Override\n    public Object getCacheKeyId(Object cacheKey) {",
        "    /** 从缓存键反解集合标识符。 */\n    @Override\n    public Object getCacheKeyId(Object cacheKey) {",
    ),
]

W3B_REPLACEMENTS["TransactionalEntityRegionAccessStrategy.java"] = _h52(
    "TransactionalEntityRegionAccessStrategy.java"
) + [
    (
        "    @Override\n    public Object generateCacheKey(Object id, EntityPersister persister, SessionFactoryImplementor factory, String tenantIdentifier) {",
        "    /** 通过 {@link RedissonEntityRegion} 的键工厂生成实体缓存键。 */\n"
        "    @Override\n"
        "    public Object generateCacheKey(Object id, EntityPersister persister, SessionFactoryImplementor factory, String tenantIdentifier) {",
    ),
    (
        "    @Override\n    public Object getCacheKeyId(Object cacheKey) {",
        "    /** 从缓存键反解实体标识符。 */\n    @Override\n    public Object getCacheKeyId(Object cacheKey) {",
    ),
]

W3B_REPLACEMENTS["TransactionalNaturalIdRegionAccessStrategy.java"] = _h52(
    "TransactionalNaturalIdRegionAccessStrategy.java"
) + [
    (
        "    @Override\n    public Object generateCacheKey(Object[] naturalIdValues, EntityPersister persister, SharedSessionContractImplementor session) {",
        "    /** 通过 {@link RedissonNaturalIdRegion} 的键工厂生成自然 ID 缓存键。 */\n"
        "    @Override\n"
        "    public Object generateCacheKey(Object[] naturalIdValues, EntityPersister persister, SharedSessionContractImplementor session) {",
    ),
    (
        "    @Override\n    public Object[] getNaturalIdValues(Object cacheKey) {",
        "    /** 从缓存键反解自然 ID 值数组。 */\n    @Override\n    public Object[] getNaturalIdValues(Object cacheKey) {",
    ),
]

W3B_REPLACEMENTS["RedissonCacheKeysFactory.java"] = list(_W2B["RedissonCacheKeysFactory.java"])

W3B_REPLACEMENTS["RedissonStrategyRegistrationProvider.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Hibernate 启动时注册 Redisson {@link RegionFactory} 策略的 SPI 提供者（Hibernate 5.3）。\n"
        " * <p>允许在配置中使用 {@code redisson} 短名或完整类名。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    @Override\n    public Iterable<StrategyRegistration> getStrategyRegistrations() {",
        "    /** 向 Hibernate 注册 {@link RedissonRegionFactory} 作为 {@link RegionFactory} 实现。 */\n"
        "    @Override\n"
        "    public Iterable<StrategyRegistration> getStrategyRegistrations() {",
    ),
]

_JNDI_CLASS = (
    "/**\n * Hibernate Cache region factory based on Redisson. \n * Uses Redisson instance located in JNDI.\n * \n * @author Nikita Koksharov \n *\n */",
    "/**\n"
    " * 基于 Redisson 的 Hibernate 缓存区域工厂。\n"
    " * <p>从 JNDI 查找已部署的 {@link RedissonClient}，而非自行创建实例。\n"
    " *\n"
    " * @author Nikita Koksharov\n"
    " */",
)

_JNDI_NATIVE_CLASS = (
    "/**\n * Hibernate Cache region factory based on Redisson. \n * Uses Redisson instance located in JNDI.\n * \n * @author Nikita Koksharov \n *\n */",
    "/**\n"
    " * 基于 Redisson Native 编解码的 Hibernate 缓存区域工厂。\n"
    " * <p>从 JNDI 查找已部署的 {@link RedissonClient}，配合 {@link RedissonRegionNativeFactory} 使用。\n"
    " *\n"
    " * @author Nikita Koksharov\n"
    " */",
)

_JNDI_COMMON = [
    (
        "    public static final String JNDI_NAME = CONFIG_PREFIX + \"jndi_name\";",
        "    /** JNDI 查找 Redisson 客户端所用的配置键（{@code hibernate.cache.redisson.jndi_name}）。 */\n"
        "    public static final String JNDI_NAME = CONFIG_PREFIX + \"jndi_name\";",
    ),
    (
        "        if (jndiName == null) {\n            throw new CacheException(JNDI_NAME + \" property not set\");\n        }",
        "        // 未配置 JNDI 名则无法查找客户端。\n"
        "        if (jndiName == null) {\n"
        "            throw new CacheException(JNDI_NAME + \" property not set\");\n"
        "        }",
    ),
    (
        "    @Override\n    protected void releaseFromUse() {\n    }",
        "    /** JNDI 模式下不销毁外部管理的 Redisson 实例。 */\n"
        "    @Override\n"
        "    protected void releaseFromUse() {\n"
        "    }",
    ),
]

_H53_JNDI = [
    _JNDI_CLASS,
    *_JNDI_COMMON,
    (
        "    @Override\n    protected RedissonClient createRedissonClient(Map properties) {",
        "    /** 从 JNDI 按配置名查找 {@link RedissonClient}（Hibernate 5.3）。\n"
        "     *\n"
        "     * @param properties Hibernate 缓存属性\n"
        "     * @return 已绑定的 Redisson 客户端\n"
        "     * @throws CacheException JNDI 名未配置或查找/关闭上下文失败\n"
        "     */\n"
        "    @Override\n"
        "    protected RedissonClient createRedissonClient(Map properties) {",
    ),
    (
        "        try {\n            context = new InitialContext(jndiProperties);\n            return (RedissonClient) context.lookup(jndiName);",
        "        try {\n"
        "            context = new InitialContext(jndiProperties);\n"
        "            // 按 JNDI 名查找 Redisson 客户端。\n"
        "            return (RedissonClient) context.lookup(jndiName);",
    ),
    (
        "        } finally {\n            if (context != null) {\n                try {\n                    context.close();",
        "        } finally {\n"
        "            // 查找完成后关闭 JNDI 上下文。\n"
        "            if (context != null) {\n"
        "                try {\n"
        "                    context.close();",
    ),
]

_H53_JNDI_NATIVE = [
    _JNDI_NATIVE_CLASS,
    *_JNDI_COMMON,
    (
        "    @Override\n    protected RedissonClient createRedissonClient(Map properties) {",
        "    /** 从 JNDI 按配置名查找 {@link RedissonClient}（Hibernate 5.3）。\n"
        "     *\n"
        "     * @param properties Hibernate 缓存属性\n"
        "     * @return 已绑定的 Redisson 客户端\n"
        "     * @throws CacheException JNDI 名未配置或查找/关闭上下文失败\n"
        "     */\n"
        "    @Override\n"
        "    protected RedissonClient createRedissonClient(Map properties) {",
    ),
    (
        "        try {\n            context = new InitialContext(jndiProperties);\n            return (RedissonClient) context.lookup(jndiName);",
        "        try {\n"
        "            context = new InitialContext(jndiProperties);\n"
        "            // 按 JNDI 名查找 Redisson 客户端。\n"
        "            return (RedissonClient) context.lookup(jndiName);",
    ),
    (
        "        } finally {\n            if (context != null) {\n                try {\n                    context.close();",
        "        } finally {\n"
        "            // 查找完成后关闭 JNDI 上下文。\n"
        "            if (context != null) {\n"
        "                try {\n"
        "                    context.close();",
    ),
]

_H6_JNDI = [
    _JNDI_CLASS,
    *_JNDI_COMMON,
    (
        "    @Override\n    protected RedissonClient createRedissonClient(StandardServiceRegistry registry, Map properties) {",
        "    /** 通过 {@link JndiService} 按配置名查找 {@link RedissonClient}（Hibernate 6）。\n"
        "     *\n"
        "     * @param registry Hibernate 服务注册表\n"
        "     * @param properties Hibernate 缓存属性\n"
        "     * @return 已绑定的 Redisson 客户端\n"
        "     * @throws CacheException JNDI 名未配置或查找失败\n"
        "     */\n"
        "    @Override\n"
        "    protected RedissonClient createRedissonClient(StandardServiceRegistry registry, Map properties) {",
    ),
    (
        "        try {\n            return (RedissonClient) registry.getService(JndiService.class).locate(jndiName);",
        "        try {\n"
        "            // 通过 Hibernate JndiService 按名定位 Redisson 客户端。\n"
        "            return (RedissonClient) registry.getService(JndiService.class).locate(jndiName);",
    ),
]

_H6_JNDI_NATIVE = [
    _JNDI_NATIVE_CLASS,
    *_JNDI_COMMON,
    (
        "    @Override\n    protected RedissonClient createRedissonClient(StandardServiceRegistry registry, Map properties) {",
        "    /** 通过 {@link JndiService} 按配置名查找 {@link RedissonClient}（Hibernate 6）。\n"
        "     *\n"
        "     * @param registry Hibernate 服务注册表\n"
        "     * @param properties Hibernate 缓存属性\n"
        "     * @return 已绑定的 Redisson 客户端\n"
        "     * @throws CacheException JNDI 名未配置或查找失败\n"
        "     */\n"
        "    @Override\n"
        "    protected RedissonClient createRedissonClient(StandardServiceRegistry registry, Map properties) {",
    ),
    (
        "        try {\n            return (RedissonClient) registry.getService(JndiService.class).locate(jndiName);",
        "        try {\n"
        "            // 通过 Hibernate JndiService 按名定位 Redisson 客户端。\n"
        "            return (RedissonClient) registry.getService(JndiService.class).locate(jndiName);",
    ),
]

W3B_REPLACEMENTS[
    "redisson-hibernate/redisson-hibernate-53/src/main/java/org/redisson/hibernate/JndiRedissonRegionFactory.java"
] = _H53_JNDI
W3B_REPLACEMENTS[
    "redisson-hibernate/redisson-hibernate-53/src/main/java/org/redisson/hibernate/JndiRedissonRegionNativeFactory.java"
] = _H53_JNDI_NATIVE
W3B_REPLACEMENTS[
    "redisson-hibernate/redisson-hibernate-6/src/main/java/org/redisson/hibernate/JndiRedissonRegionFactory.java"
] = _H6_JNDI
W3B_REPLACEMENTS[
    "redisson-hibernate/redisson-hibernate-6/src/main/java/org/redisson/hibernate/JndiRedissonRegionNativeFactory.java"
] = _H6_JNDI_NATIVE

W3B_REPLACEMENTS["RedissonRegionFactory.java"] = [
    (
        "/**\n * Hibernate Cache region factory based on Redisson. \n * Creates own Redisson instance during region start.\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 基于 Redisson 的 Hibernate 二级缓存 {@link RegionFactory} 实现（Hibernate 5.3+）。\n"
        " * <p>在 Region 启动时创建并持有独立的 {@link RedissonClient} 实例。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public static final String QUERY_DEF = \"query\";",
        "    /** 查询结果缓存区域的默认配置键后缀。 */\n    public static final String QUERY_DEF = \"query\";",
    ),
    (
        "    public static final String COLLECTION_DEF = \"collection\";",
        "    /** 集合缓存区域的默认配置键后缀。 */\n    public static final String COLLECTION_DEF = \"collection\";",
    ),
    (
        "    public static final String ENTITY_DEF = \"entity\";",
        "    /** 实体缓存区域的默认配置键后缀。 */\n    public static final String ENTITY_DEF = \"entity\";",
    ),
    (
        "    public static final String NATURAL_ID_DEF = \"naturalid\";",
        "    /** 自然 ID 缓存区域的默认配置键后缀。 */\n    public static final String NATURAL_ID_DEF = \"naturalid\";",
    ),
    (
        "    public static final String TIMESTAMPS_DEF = \"timestamps\";",
        "    /** 时间戳缓存区域的默认配置键后缀。 */\n    public static final String TIMESTAMPS_DEF = \"timestamps\";",
    ),
    (
        "    public static final String MAX_ENTRIES_SUFFIX = \".eviction.max_entries\";",
        "    /** Region 最大条目数配置键后缀。 */\n    public static final String MAX_ENTRIES_SUFFIX = \".eviction.max_entries\";",
    ),
    (
        "    public static final String TTL_SUFFIX = \".expiration.time_to_live\";",
        "    /** Region TTL 配置键后缀。 */\n    public static final String TTL_SUFFIX = \".expiration.time_to_live\";",
    ),
    (
        "    public static final String MAX_IDLE_SUFFIX = \".expiration.max_idle_time\";",
        "    /** Region 最大空闲时间配置键后缀。 */\n    public static final String MAX_IDLE_SUFFIX = \".expiration.max_idle_time\";",
    ),
    (
        "    public static final String CONFIG_PREFIX = \"hibernate.cache.redisson.\";",
        "    /** Hibernate 属性中 Redisson 相关配置的前缀。 */\n    public static final String CONFIG_PREFIX = \"hibernate.cache.redisson.\";",
    ),
    (
        "    public static final String REDISSON_CONFIG_PATH = CONFIG_PREFIX + \"config\";",
        "    /** Redisson 配置文件路径对应的 Hibernate 属性键。 */\n    public static final String REDISSON_CONFIG_PATH = CONFIG_PREFIX + \"config\";",
    ),
    (
        "    public static final String FALLBACK = CONFIG_PREFIX + \"fallback\";",
        "    /** 是否在 Redis 不可用时启用本地降级模式的属性键。 */\n    public static final String FALLBACK = CONFIG_PREFIX + \"fallback\";",
    ),
    (
        "    @Override\n    protected CacheKeysFactory getImplicitCacheKeysFactory() {",
        "    /** 返回启动时解析的缓存键工厂。 */\n    @Override\n    protected CacheKeysFactory getImplicitCacheKeysFactory() {",
    ),
    (
        "    @Override\n    protected void prepareForUse(SessionFactoryOptions settings, @SuppressWarnings(\"rawtypes\") Map properties) throws CacheException {",
        "    /** 加载 Redisson 配置、初始化客户端并解析 fallback 与缓存键工厂。 */\n"
        "    @Override\n"
        "    protected void prepareForUse(SessionFactoryOptions settings, @SuppressWarnings(\"rawtypes\") Map properties) throws CacheException {",
    ),
    (
        "    protected RedissonClient createRedissonClient(Map properties) {",
        "    /** 从类路径或指定路径加载 YAML/JSON 配置并创建 {@link RedissonClient}。 */\n"
        "    protected RedissonClient createRedissonClient(Map properties) {",
    ),
    (
        "    @Override\n    protected void releaseFromUse() {",
        "    /** 关闭 Redisson 客户端并释放连接。 */\n    @Override\n    protected void releaseFromUse() {",
    ),
    (
        "    @Override\n    public boolean isMinimalPutsEnabledByDefault() {",
        "    /** 默认启用最小化 put 策略以减少缓存写入。 */\n    @Override\n    public boolean isMinimalPutsEnabledByDefault() {",
    ),
    (
        "    @Override\n    public AccessType getDefaultAccessType() {",
        "    /** 默认缓存并发访问策略为 {@link AccessType#TRANSACTIONAL}。 */\n    @Override\n    public AccessType getDefaultAccessType() {",
    ),
    (
        "    @Override\n    public long nextTimestamp() {",
        "    /** 通过 Redis Lua 脚本生成全局递增时间戳；失败且启用 fallback 时使用父类本地递增。 */\n"
        "    @Override\n"
        "    public long nextTimestamp() {",
    ),
    (
        "    @Override\n    public DomainDataRegion buildDomainDataRegion(",
        "    /** 构建域数据（实体/集合/自然 ID）二级缓存 Region。 */\n    @Override\n    public DomainDataRegion buildDomainDataRegion(",
    ),
    (
        "    @Override\n    protected DomainDataStorageAccess createDomainDataStorageAccess(DomainDataRegionConfig regionConfig,",
        "    /** 根据 Region 配置创建 {@link RedissonStorage} 作为底层存储访问层。 */\n"
        "    @Override\n"
        "    protected DomainDataStorageAccess createDomainDataStorageAccess(DomainDataRegionConfig regionConfig,",
    ),
    (
        "            throw new IllegalArgumentException(\"Unable to determine entity cache type!\");",
        "            // 无法从 Region 配置推断缓存类型。\n"
        "            throw new IllegalArgumentException(\"Unable to determine entity cache type!\");",
    ),
    (
        "    @Override\n    protected StorageAccess createQueryResultsRegionStorageAccess(String regionName,",
        "    /** 为查询结果 Region 创建 {@link RedissonStorage} 存储访问。 */\n"
        "    @Override\n"
        "    protected StorageAccess createQueryResultsRegionStorageAccess(String regionName,",
    ),
    (
        "    @Override\n    protected StorageAccess createTimestampsRegionStorageAccess(String regionName,",
        "    /** 为时间戳 Region 创建 {@link RedissonStorage} 存储访问。 */\n"
        "    @Override\n"
        "    protected StorageAccess createTimestampsRegionStorageAccess(String regionName,",
    ),
    (
        "    protected RMapCache<Object, Object> getCache(String cacheName, Map properties, String defaultKey) {",
        "    /** 获取指定 Region 名称对应的 {@link RMapCache} 实例。 */\n"
        "    protected RMapCache<Object, Object> getCache(String cacheName, Map properties, String defaultKey) {",
    ),
]

W3B_REPLACEMENTS["RedissonRegionNativeFactory.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 使用 Redisson 原生 Map 缓存（{@link RMapCacheNative}）的 Region 工厂（Hibernate 5.3）。\n"
        " * <p>启动前校验 eviction 与 max_idle 配置必须为 0。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    @Override\n    protected void prepareForUse(SessionFactoryOptions settings, Map properties) throws CacheException {",
        "    /** 校验原生模式下不允许非零的 max_entries 与 max_idle_time，再调用父类启动逻辑。 */\n"
        "    @Override\n"
        "    protected void prepareForUse(SessionFactoryOptions settings, Map properties) throws CacheException {",
    ),
    (
        "                    throw new IllegalArgumentException(\".eviction.max_entries setting can't be non-zero\");",
        "                    // 原生 Map 缓存不支持客户端侧 max_entries 逐出。\n"
        "                    throw new IllegalArgumentException(\".eviction.max_entries setting can't be non-zero\");",
    ),
    (
        "                    throw new IllegalArgumentException(\".expiration.max_idle_time setting can't be non-zero\");",
        "                    // 原生 Map 缓存不支持客户端侧 max_idle 过期。\n"
        "                    throw new IllegalArgumentException(\".expiration.max_idle_time setting can't be non-zero\");",
    ),
    (
        "    @Override\n    protected RMapCache<Object, Object> getCache(String regionName, Map properties, String defaultKey) {",
        "    /** 返回包装后的 {@link RMapCacheNative} 实例作为 Region 底层存储。 */\n"
        "    @Override\n"
        "    protected RMapCache<Object, Object> getCache(String regionName, Map properties, String defaultKey) {",
    ),
]

W3B_REPLACEMENTS["RedissonStorage.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Redisson {@link RMapCache} 上的 Hibernate {@link DomainDataStorageAccess} 实现（Hibernate 5.3）。\n"
        " * <p>支持 TTL、max_idle、max_entries 配置及 Redis 不可用时的 fallback 降级。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public RedissonStorage(String regionName, RMapCache<Object, Object> mapCache, ServiceManager serviceManager, Map<String, Object> properties, String defaultKey) {",
        "    /** 从 Hibernate 属性解析 TTL、max_idle、max_entries 与 fallback 并应用到 {@link RMapCache}。\n"
        "     *\n"
        "     * @param regionName Region 逻辑名\n"
        "     * @param mapCache 底层 Redisson Map 缓存\n"
        "     * @param serviceManager Redisson 服务管理器（用于 fallback 心跳）\n"
        "     * @param properties Hibernate 缓存属性\n"
        "     * @param defaultKey 默认配置键后缀（entity/collection 等）\n"
        "     */\n"
        "    public RedissonStorage(String regionName, RMapCache<Object, Object> mapCache, ServiceManager serviceManager, Map<String, Object> properties, String defaultKey) {",
    ),
    (
        "    private String getProperty(Map<String, Object> properties, String name, String regionName, String defaultKey, String suffix) {",
        "    /** 按 map 名、Region 名、默认键后缀的优先级查找配置属性。 */\n"
        "    private String getProperty(Map<String, Object> properties, String name, String regionName, String defaultKey, String suffix) {",
    ),
    (
        "    private void ping() {",
        "    /** 进入 fallback 模式后周期性探测 Redis 是否恢复。 */\n    private void ping() {",
    ),
    (
        "    @Override\n    public Object getFromCache(Object key, SharedSessionContractImplementor session) {",
        "    /** 从缓存读取条目；fallback 模式下返回 null。 */\n"
        "    @Override\n"
        "    public Object getFromCache(Object key, SharedSessionContractImplementor session) {",
    ),
    (
        "        if (fallbackMode) {\n            return null;\n        }\n        try {\n            if (maxIdle == 0 && size == 0) {",
        "        // fallback 模式下跳过远程读取。\n"
        "        if (fallbackMode) {\n"
        "            return null;\n"
        "        }\n"
        "        try {\n"
        "            // 未配置 max_idle 与 max_entries 时使用仅 TTL 的读取路径。\n"
        "            if (maxIdle == 0 && size == 0) {",
    ),
    (
        "    @Override\n    public void putIntoCache(Object key, Object value, SharedSessionContractImplementor session) {",
        "    /** 写入缓存条目；fallback 模式下跳过写入。 */\n"
        "    @Override\n"
        "    public void putIntoCache(Object key, Object value, SharedSessionContractImplementor session) {",
    ),
    (
        "    @Override\n    public boolean contains(Object key) {",
        "    /** 判断键是否存在；fallback 模式下恒为 false。 */\n    @Override\n    public boolean contains(Object key) {",
    ),
    (
        "    @Override\n    public void evictData() {",
        "    /** 清空整个 Region 缓存；fallback 模式下跳过。 */\n    @Override\n    public void evictData() {",
    ),
    (
        "    @Override\n    public void evictData(Object key) {",
        "    /** 逐出指定键；fallback 模式下跳过。 */\n    @Override\n    public void evictData(Object key) {",
    ),
    (
        "    @Override\n    public void release() {",
        "    /** 销毁底层 {@link RMapCache} 并释放 Redis 资源。 */\n    @Override\n    public void release() {",
    ),
]
