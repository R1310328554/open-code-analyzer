"""Chinese annotation replacements for Redisson 4.7.0 wave-4a hibernate-6/7/72 [0:15]."""
from __future__ import annotations

import importlib.util
from pathlib import Path

_spec = importlib.util.spec_from_file_location(
    "wave3b_replacements_redisson",
    Path(__file__).with_name("wave3b_replacements_redisson.py"),
)
_w3b = importlib.util.module_from_spec(_spec)
assert _spec.loader is not None
_spec.loader.exec_module(_w3b)

W4A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

W4A_REPLACEMENTS["RedissonCacheKeysFactory.java"] = list(
    _w3b.W3B_REPLACEMENTS["RedissonCacheKeysFactory.java"]
)

W4A_REPLACEMENTS["RedissonStorage.java"] = [
    (
        old.replace("（Hibernate 5.3）", "（Hibernate 6+）"),
        new.replace("（Hibernate 5.3）", "（Hibernate 6+）"),
    )
    for old, new in _w3b.W3B_REPLACEMENTS["RedissonStorage.java"]
]

W4A_REPLACEMENTS["RedissonRegionNativeFactory.java"] = [
    (
        old.replace("（Hibernate 5.3）", "（Hibernate 6+）"),
        new.replace("（Hibernate 5.3）", "（Hibernate 6+）"),
    )
    for old, new in _w3b.W3B_REPLACEMENTS["RedissonRegionNativeFactory.java"]
]

W4A_REPLACEMENTS["RedissonStrategyRegistrationProvider.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Hibernate 启动时注册 Redisson {@link RegionFactory} 策略的 SPI 提供者（Hibernate 6/7）。\n"
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

_H72_STRATEGY = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Hibernate 启动时注册 Redisson {@link RegionFactory} 策略的 SPI 提供者（Hibernate 7.2+）。\n"
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

_REGION_CLASS = (
    "/**\n * Hibernate Cache region factory based on Redisson. \n * Creates own Redisson instance during region start.\n * \n * @author Nikita Koksharov\n *\n */",
    "/**\n"
    " * 基于 Redisson 的 Hibernate 二级缓存 {@link RegionFactory} 实现（Hibernate 6+）。\n"
    " * <p>在 Region 启动时创建并持有独立的 {@link RedissonClient} 实例。\n"
    " *\n"
    " * @author Nikita Koksharov\n"
    " */",
)

_REGION_CONSTANTS: list[tuple[str, str]] = [
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
]

_REGION_METHODS: list[tuple[str, str]] = [
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
        "    protected RedissonClient createRedissonClient(StandardServiceRegistry registry, Map properties) {",
        "    /** 从类路径或指定路径加载 YAML/JSON 配置并创建 {@link RedissonClient}。 */\n"
        "    protected RedissonClient createRedissonClient(StandardServiceRegistry registry, Map properties) {",
    ),
    (
        "        if (config == null) {\n            throw new CacheException(\"Unable to locate Redisson configuration\");",
        "        // 未找到任何 Redisson 配置文件。\n"
        "        if (config == null) {\n"
        "            throw new CacheException(\"Unable to locate Redisson configuration\");",
    ),
    (
        "    private Config loadConfig(ClassLoader classLoader, String fileName) {",
        "    /** 从类路径资源加载 Redisson YAML 配置。 */\n    private Config loadConfig(ClassLoader classLoader, String fileName) {",
    ),
    (
        "    @Override\n    protected void releaseFromUse() {",
        "    /** 关闭 Redisson 客户端并释放连接。 */\n    @Override\n    protected void releaseFromUse() {",
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

_REGION_MINIMAL_TRUE = (
    "    @Override\n    public boolean isMinimalPutsEnabledByDefault() {\n        return true;\n    }",
    "    /** 默认启用最小化 put 策略以减少缓存写入。 */\n"
    "    @Override\n"
    "    public boolean isMinimalPutsEnabledByDefault() {\n"
    "        return true;\n"
    "    }",
)

_REGION_MINIMAL_FALSE = (
    "    @Override\n    public boolean isMinimalPutsEnabledByDefault() {\n"
    "        // Must stay false. Since Hibernate 7.4 AbstractReadWriteAccess#putFromLoad honors the\n"
    "        // minimalPutOverride flag and skips the put when (minimalPutOverride && version == null\n"
    "        // && item != null).\n"
    "        return false;\n"
    "    }",
    "    /** 必须返回 false：Hibernate 7.4 起 putFromLoad 会尊重 minimalPutOverride 并可能跳过写入。 */\n"
    "    @Override\n"
    "    public boolean isMinimalPutsEnabledByDefault() {\n"
    "        // Hibernate 7.4 起 AbstractReadWriteAccess#putFromLoad 在 minimalPutOverride 为 true\n"
    "        // 且 version 为 null、item 非 null 时会跳过 put，因此此处不能返回 true。\n"
    "        return false;\n"
    "    }",
)

_LOAD_CONFIG_IO = (
    "    private Config loadConfig(String configPath) {\n        try {\n            return Config.fromYAML(new File(configPath));\n        } catch (IOException e) {\n            throw new CacheException(\"Can't parse default config\", e);\n        }\n    }",
    "    /** 从文件系统路径加载 Redisson YAML 配置。 */\n"
    "    private Config loadConfig(String configPath) {\n"
    "        try {\n            return Config.fromYAML(new File(configPath));\n"
    "        } catch (IOException e) {\n"
    "            throw new CacheException(\"Can't parse default config\", e);\n"
    "        }\n"
    "    }",
)

_LOAD_CONFIG_EX = (
    "    private Config loadConfig(String configPath) {\n        try {\n            return Config.fromYAML(new File(configPath));\n        } catch (Exception e) {\n            throw new CacheException(\"Can't parse default config\", e);\n        }\n    }",
    "    /** 从文件系统路径加载 Redisson YAML 配置。 */\n"
    "    private Config loadConfig(String configPath) {\n"
    "        try {\n            return Config.fromYAML(new File(configPath));\n"
    "        } catch (Exception e) {\n"
    "            throw new CacheException(\"Can't parse default config\", e);\n"
    "        }\n"
    "    }",
)

_H6_REGION = [
    _REGION_CLASS,
    *_REGION_CONSTANTS,
    *_REGION_METHODS,
    _REGION_MINIMAL_TRUE,
    _LOAD_CONFIG_IO,
]

_H7_REGION = [
    _REGION_CLASS,
    *_REGION_CONSTANTS,
    *_REGION_METHODS,
    _REGION_MINIMAL_TRUE,
    _LOAD_CONFIG_EX,
]

_H72_REGION = [
    _REGION_CLASS,
    *_REGION_CONSTANTS,
    *_REGION_METHODS,
    _REGION_MINIMAL_FALSE,
    _LOAD_CONFIG_EX,
]

for rel, reps in (
    (
        "redisson-hibernate/redisson-hibernate-6/src/main/java/org/redisson/hibernate/RedissonRegionFactory.java",
        _H6_REGION,
    ),
    (
        "redisson-hibernate/redisson-hibernate-7/src/main/java/org/redisson/hibernate/RedissonRegionFactory.java",
        _H7_REGION,
    ),
    (
        "redisson-hibernate/redisson-hibernate-72/src/main/java/org/redisson/hibernate/RedissonRegionFactory.java",
        _H72_REGION,
    ),
):
    W4A_REPLACEMENTS[rel] = reps

for rel in (
    "redisson-hibernate/redisson-hibernate-7/src/main/java/org/redisson/hibernate/JndiRedissonRegionFactory.java",
    "redisson-hibernate/redisson-hibernate-72/src/main/java/org/redisson/hibernate/JndiRedissonRegionFactory.java",
):
    W4A_REPLACEMENTS[rel] = list(
        _w3b.W3B_REPLACEMENTS[
            "redisson-hibernate/redisson-hibernate-6/src/main/java/org/redisson/hibernate/JndiRedissonRegionFactory.java"
        ]
    )

for rel in (
    "redisson-hibernate/redisson-hibernate-7/src/main/java/org/redisson/hibernate/JndiRedissonRegionNativeFactory.java",
    "redisson-hibernate/redisson-hibernate-72/src/main/java/org/redisson/hibernate/JndiRedissonRegionNativeFactory.java",
):
    W4A_REPLACEMENTS[rel] = list(
        _w3b.W3B_REPLACEMENTS[
            "redisson-hibernate/redisson-hibernate-6/src/main/java/org/redisson/hibernate/JndiRedissonRegionNativeFactory.java"
        ]
    )

W4A_REPLACEMENTS[
    "redisson-hibernate/redisson-hibernate-72/src/main/java/org/redisson/hibernate/RedissonStrategyRegistrationProvider.java"
] = _H72_STRATEGY
