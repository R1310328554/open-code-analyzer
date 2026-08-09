"""Chinese annotation replacements for Redisson 4.7.0 wave-2a hibernate-5 [0:15]."""

W2A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

W2A_REPLACEMENTS["RedissonCacheKeysFactory.java"] = [
    (
        "/**\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Redisson 自定义 {@link DefaultCacheKeysFactory}，用于 Hibernate 5 集合缓存键生成。\n"
        " * <p>在生成集合键前临时清空嵌入集合字段，经 Redisson {@link Codec} 编解码后再\n"
        " * 恢复字段值，避免集合引用影响键的稳定性。</p>\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    private final Codec codec;",
        "    /** Redisson 编解码器，用于集合 ID 的序列化与反序列化。 */\n"
        "    private final Codec codec;",
    ),
    (
        "    public RedissonCacheKeysFactory(Codec codec) {",
        "    /** @param codec Redisson 客户端使用的 {@link Codec} */\n"
        "    public RedissonCacheKeysFactory(Codec codec) {",
    ),
    (
        "    @Override\n    public Object createCollectionKey(Object id, CollectionPersister persister, SessionFactoryImplementor factory, String tenantIdentifier) {",
        "    /** 生成集合缓存键：临时剥离嵌入集合字段后经 Codec 规范化 ID，再委托父类生成键。\n"
        "     *\n"
        "     * @param id 实体标识\n"
        "     * @param persister 集合持久化器\n"
        "     * @param factory SessionFactory 实现\n"
        "     * @param tenantIdentifier 租户标识\n"
        "     * @return Hibernate 集合缓存键\n"
        "     */\n"
        "    @Override\n"
        "    public Object createCollectionKey(Object id, CollectionPersister persister, SessionFactoryImplementor factory, String tenantIdentifier) {",
    ),
    (
        "            Object prev = f.get(id);\n            f.set(id, null);",
        "            // 临时清空嵌入集合字段，使 ID 可稳定编码。\n"
        "            Object prev = f.get(id);\n"
        "            f.set(id, null);",
    ),
    (
        "            f.set(id, prev);\n            return super.createCollectionKey(newId, persister, factory, tenantIdentifier);",
        "            // 恢复字段后使用规范化 ID 生成缓存键。\n"
        "            f.set(id, prev);\n"
        "            return super.createCollectionKey(newId, persister, factory, tenantIdentifier);",
    ),
    (
        "        } catch (PropertyNotFoundException e) {\n            return super.createCollectionKey(id, persister, factory, tenantIdentifier);",
        "        // 找不到嵌入字段时回退至默认键生成逻辑。\n"
        "        } catch (PropertyNotFoundException e) {\n"
        "            return super.createCollectionKey(id, persister, factory, tenantIdentifier);",
    ),
]

W2A_REPLACEMENTS["RedissonRegionFactory.java"] = [
    (
        "/**\n * Hibernate Cache region factory based on Redisson. \n"
        " * Creates own Redisson instance during region start.\n * \n * @author Nikita Koksharov\n *\n */public class RedissonRegionFactory implements RegionFactory {",
        "/**\n"
        " * 基于 Redisson 的 Hibernate 5 二级缓存 {@link RegionFactory} 实现。\n"
        " * <p>在 Region 启动时创建并持有独立的 {@link RedissonClient} 实例，\n"
        " * 并解析 {@link CacheKeysFactory} 与 fallback 降级配置。</p>\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */public class RedissonRegionFactory implements RegionFactory {",
    ),
    (
        "    public static final String QUERY_DEF = \"query\";",
        "    /** 查询结果缓存区域的默认配置键后缀。 */\n"
        "    public static final String QUERY_DEF = \"query\";",
    ),
    (
        "    public static final String COLLECTION_DEF = \"collection\";",
        "    /** 集合缓存区域的默认配置键后缀。 */\n"
        "    public static final String COLLECTION_DEF = \"collection\";",
    ),
    (
        "    public static final String ENTITY_DEF = \"entity\";",
        "    /** 实体缓存区域的默认配置键后缀。 */\n"
        "    public static final String ENTITY_DEF = \"entity\";",
    ),
    (
        "    public static final String NATURAL_ID_DEF = \"naturalid\";",
        "    /** 自然 ID 缓存区域的默认配置键后缀。 */\n"
        "    public static final String NATURAL_ID_DEF = \"naturalid\";",
    ),
    (
        "    public static final String TIMESTAMPS_DEF = \"timestamps\";",
        "    /** 时间戳缓存区域的默认配置键后缀。 */\n"
        "    public static final String TIMESTAMPS_DEF = \"timestamps\";",
    ),
    (
        "    public static final String MAX_ENTRIES_SUFFIX = \".eviction.max_entries\";",
        "    /** Region 最大条目数配置键后缀。 */\n"
        "    public static final String MAX_ENTRIES_SUFFIX = \".eviction.max_entries\";",
    ),
    (
        "    public static final String TTL_SUFFIX = \".expiration.time_to_live\";",
        "    /** Region TTL 配置键后缀。 */\n"
        "    public static final String TTL_SUFFIX = \".expiration.time_to_live\";",
    ),
    (
        "    public static final String MAX_IDLE_SUFFIX = \".expiration.max_idle_time\";",
        "    /** Region 最大空闲时间配置键后缀。 */\n"
        "    public static final String MAX_IDLE_SUFFIX = \".expiration.max_idle_time\";",
    ),
    (
        "    public static final String CONFIG_PREFIX = \"hibernate.cache.redisson.\";",
        "    /** Hibernate 属性中 Redisson 相关配置的前缀。 */\n"
        "    public static final String CONFIG_PREFIX = \"hibernate.cache.redisson.\";",
    ),
    (
        "    public static final String REDISSON_CONFIG_PATH = CONFIG_PREFIX + \"config\";",
        "    /** Redisson 配置文件路径对应的 Hibernate 属性键。 */\n"
        "    public static final String REDISSON_CONFIG_PATH = CONFIG_PREFIX + \"config\";",
    ),
    (
        "    public static final String FALLBACK = CONFIG_PREFIX + \"fallback\";",
        "    /** 是否在 Redis 不可用时启用本地降级模式的属性键。 */\n"
        "    public static final String FALLBACK = CONFIG_PREFIX + \"fallback\";",
    ),
    (
        "    @Override\n    public void start(SessionFactoryOptions settings, Properties properties) throws CacheException {",
        "    /** 加载 Redisson 配置、初始化客户端，并解析 {@link CacheKeysFactory} 与 fallback 开关。 */\n"
        "    @Override\n"
        "    public void start(SessionFactoryOptions settings, Properties properties) throws CacheException {",
    ),
    (
        "    protected RedissonClient createRedissonClient(Properties properties) {",
        "    /** 从类路径或指定路径加载 YAML/JSON 配置并创建 {@link RedissonClient}。 */\n"
        "    protected RedissonClient createRedissonClient(Properties properties) {",
    ),
    (
        "    @Override\n    public void stop() {",
        "    /** 关闭 Redisson 客户端并释放连接。 */\n"
        "    @Override\n"
        "    public void stop() {",
    ),
    (
        "    @Override\n    public boolean isMinimalPutsEnabledByDefault() {",
        "    /** 默认启用最小化 put 策略以减少缓存写入。 */\n"
        "    @Override\n"
        "    public boolean isMinimalPutsEnabledByDefault() {",
    ),
    (
        "    @Override\n    public AccessType getDefaultAccessType() {",
        "    /** 默认缓存并发访问策略为 {@link AccessType#TRANSACTIONAL}。 */\n"
        "    @Override\n"
        "    public AccessType getDefaultAccessType() {",
    ),
    (
        "    @Override\n    public long nextTimestamp() {",
        "    /** 通过 Redis Lua 脚本生成全局递增时间戳；失败且启用 fallback 时使用本地 CAS 递增。 */\n"
        "    @Override\n"
        "    public long nextTimestamp() {",
    ),
    (
        "    @Override\n    public EntityRegion buildEntityRegion(String regionName, Properties properties, CacheDataDescription metadata)",
        "    /** 构建实体二级缓存 Region。 */\n"
        "    @Override\n"
        "    public EntityRegion buildEntityRegion(String regionName, Properties properties, CacheDataDescription metadata)",
    ),
    (
        "    @Override\n    public NaturalIdRegion buildNaturalIdRegion(String regionName, Properties properties, CacheDataDescription metadata)",
        "    /** 构建自然 ID 二级缓存 Region。 */\n"
        "    @Override\n"
        "    public NaturalIdRegion buildNaturalIdRegion(String regionName, Properties properties, CacheDataDescription metadata)",
    ),
    (
        "    @Override\n    public CollectionRegion buildCollectionRegion(String regionName, Properties properties,\n            CacheDataDescription metadata) throws CacheException {",
        "    /** 构建集合二级缓存 Region。 */\n"
        "    @Override\n"
        "    public CollectionRegion buildCollectionRegion(String regionName, Properties properties,\n"
        "            CacheDataDescription metadata) throws CacheException {",
    ),
    (
        "    @Override\n    public QueryResultsRegion buildQueryResultsRegion(String regionName, Properties properties) throws CacheException {",
        "    /** 构建查询结果缓存 Region。 */\n"
        "    @Override\n"
        "    public QueryResultsRegion buildQueryResultsRegion(String regionName, Properties properties) throws CacheException {",
    ),
    (
        "    @Override\n    public TimestampsRegion buildTimestampsRegion(String regionName, Properties properties) throws CacheException {",
        "    /** 构建查询失效时间戳 Region。 */\n"
        "    @Override\n"
        "    public TimestampsRegion buildTimestampsRegion(String regionName, Properties properties) throws CacheException {",
    ),
    (
        "    protected RMapCache<Object, Object> getCache(String regionName, Properties properties, String defaultKey) {",
        "    /** 获取指定 Region 名称对应的 {@link RMapCache} 实例。 */\n"
        "    protected RMapCache<Object, Object> getCache(String regionName, Properties properties, String defaultKey) {",
    ),
]

W2A_REPLACEMENTS["RedissonRegionNativeFactory.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 使用 Redisson 原生 Map 缓存（{@link RMapCacheNative}）的 Hibernate 5 Region 工厂。\n"
        " * <p>启动前校验 eviction 与 max_idle 配置必须为 0。</p>\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    @Override\n    public void start(SessionFactoryOptions settings, Properties properties) throws CacheException {",
        "    /** 校验原生模式下不允许非零的 max_entries 与 max_idle_time，再调用父类启动逻辑。 */\n"
        "    @Override\n"
        "    public void start(SessionFactoryOptions settings, Properties properties) throws CacheException {",
    ),
    (
        "                if (value > 0) {\n                    throw new IllegalArgumentException(\".eviction.max_entries setting can't be non-zero\");",
        "                // 原生 Map 模式不支持非零 max_entries。\n"
        "                if (value > 0) {\n"
        "                    throw new IllegalArgumentException(\".eviction.max_entries setting can't be non-zero\");",
    ),
    (
        "                if (value > 0) {\n                    throw new IllegalArgumentException(\".expiration.max_idle_time setting can't be non-zero\");",
        "                // 原生 Map 模式不支持非零 max_idle_time。\n"
        "                if (value > 0) {\n"
        "                    throw new IllegalArgumentException(\".expiration.max_idle_time setting can't be non-zero\");",
    ),
    (
        "    @Override\n    protected RMapCache<Object, Object> getCache(String regionName, Properties properties, String defaultKey) {",
        "    /** 返回包装后的 {@link RMapCacheNative} 实例作为 Region 底层存储。 */\n"
        "    @Override\n"
        "    protected RMapCache<Object, Object> getCache(String regionName, Properties properties, String defaultKey) {",
    ),
]

W2A_REPLACEMENTS["RedissonStrategyRegistrationProvider.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Hibernate 5 启动时注册 Redisson {@link RegionFactory} 策略的 SPI 提供者。\n"
        " * <p>允许在配置中使用 {@code redisson} 短名或完整类名。</p>\n"
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

W2A_REPLACEMENTS["BaseRegion.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Redisson 缓存 Region 的抽象基类，实现 {@link TransactionalDataRegion} 与 {@link GeneralDataRegion}。\n"
        " * <p>封装 {@link RMapCache} 的读写、驱逐、TTL 及 Redis 不可用时的 fallback 降级逻辑。</p>\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public BaseRegion(RMapCache<Object, Object> mapCache, ServiceManager serviceManager, RegionFactory regionFactory, CacheDataDescription metadata, Properties properties, String defaultKey) {",
        "    /** 根据 Hibernate 属性初始化 TTL、maxIdle、maxSize 及 fallback 模式。 */\n"
        "    public BaseRegion(RMapCache<Object, Object> mapCache, ServiceManager serviceManager, RegionFactory regionFactory, CacheDataDescription metadata, Properties properties, String defaultKey) {",
    ),
    (
        "    private String getProperty(Properties properties, String name, String defaultKey, String suffix) {",
        "    /** 按 Region 名或默认键后缀查找 Hibernate 缓存配置属性。 */\n"
        "    private String getProperty(Properties properties, String name, String defaultKey, String suffix) {",
    ),
    (
        "    private void ping() {",
        "    /** 异步探测 Redis 连通性；恢复后退出 fallback 模式，否则继续定时重试。 */\n"
        "    private void ping() {",
    ),
    (
        "        // TODO Auto-generated method stub",
        "        // 当前实现不参与 Hibernate 事务同步",
    ),
    (
        "        // 60 seconds (normalized value)",
        "        // 60 秒（Hibernate 规范化后的超时值）",
    ),
    (
        "    @Override\n    public Object get(SessionImplementor session, Object key) throws CacheException {",
        "    /** 从缓存读取条目；fallback 模式下返回 null 而不访问 Redis。 */\n"
        "    @Override\n"
        "    public Object get(SessionImplementor session, Object key) throws CacheException {",
    ),
    (
        "            if (maxIdle == 0 && size == 0) {\n                return mapCache.getWithTTLOnly(key);",
        "            // 无 maxIdle 与 maxSize 限制时使用仅 TTL 的读取路径。\n"
        "            if (maxIdle == 0 && size == 0) {\n"
        "                return mapCache.getWithTTLOnly(key);",
    ),
    (
        "    @Override\n    public void put(SessionImplementor session, Object key, Object value) throws CacheException {",
        "    /** 写入缓存条目，应用 Region 配置的 TTL 与 maxIdle。 */\n"
        "    @Override\n"
        "    public void put(SessionImplementor session, Object key, Object value) throws CacheException {",
    ),
    (
        "    @Override\n    public void evict(Object key) throws CacheException {",
        "    /** 移除指定键的缓存条目。 */\n"
        "    @Override\n"
        "    public void evict(Object key) throws CacheException {",
    ),
    (
        "    @Override\n    public void evictAll() throws CacheException {",
        "    /** 清空整个 Region 缓存。 */\n"
        "    @Override\n"
        "    public void evictAll() throws CacheException {",
    ),
]

W2A_REPLACEMENTS["RedissonCollectionRegion.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Hibernate 5 集合（Collection）二级缓存 Region，基于 Redisson {@link RMapCache}。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public RedissonCollectionRegion(RMapCache<Object, Object> mapCache, ServiceManager serviceManager, RegionFactory regionFactory,\n"
        "                                    CacheDataDescription metadata, Settings settings, Properties properties, String defaultKey, CacheKeysFactory cacheKeysFactory) {",
        "    /** @param cacheKeysFactory 缓存键工厂，用于集合键的生成与解析 */\n"
        "    public RedissonCollectionRegion(RMapCache<Object, Object> mapCache, ServiceManager serviceManager, RegionFactory regionFactory,\n"
        "                                    CacheDataDescription metadata, Settings settings, Properties properties, String defaultKey, CacheKeysFactory cacheKeysFactory) {",
    ),
    (
        "    public CacheKeysFactory getCacheKeysFactory() {",
        "    /** 返回本 Region 使用的 {@link CacheKeysFactory}。 */\n"
        "    public CacheKeysFactory getCacheKeysFactory() {",
    ),
    (
        "    @Override\n    public CollectionRegionAccessStrategy buildAccessStrategy(AccessType accessType) throws CacheException {",
        "    /** 按 {@link AccessType} 构建集合 Region 的并发访问策略。 */\n"
        "    @Override\n"
        "    public CollectionRegionAccessStrategy buildAccessStrategy(AccessType accessType) throws CacheException {",
    ),
]

W2A_REPLACEMENTS["RedissonEntityRegion.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Hibernate 5 实体二级缓存 Region，基于 Redisson {@link RMapCache}。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public RedissonEntityRegion(RMapCache<Object, Object> mapCache, ServiceManager serviceManager, RegionFactory regionFactory,\n"
        "                                CacheDataDescription metadata, Settings settings, Properties properties, String defaultKey, CacheKeysFactory cacheKeysFactory) {",
        "    /** @param cacheKeysFactory 缓存键工厂，用于实体键的生成与解析 */\n"
        "    public RedissonEntityRegion(RMapCache<Object, Object> mapCache, ServiceManager serviceManager, RegionFactory regionFactory,\n"
        "                                CacheDataDescription metadata, Settings settings, Properties properties, String defaultKey, CacheKeysFactory cacheKeysFactory) {",
    ),
    (
        "    public CacheKeysFactory getCacheKeysFactory() {",
        "    /** 返回本 Region 使用的 {@link CacheKeysFactory}。 */\n"
        "    public CacheKeysFactory getCacheKeysFactory() {",
    ),
    (
        "    @Override\n    public EntityRegionAccessStrategy buildAccessStrategy(AccessType accessType) throws CacheException {",
        "    /** 按 {@link AccessType} 构建实体 Region 的并发访问策略。 */\n"
        "    @Override\n"
        "    public EntityRegionAccessStrategy buildAccessStrategy(AccessType accessType) throws CacheException {",
    ),
]

W2A_REPLACEMENTS["RedissonNaturalIdRegion.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Hibernate 5 自然 ID（NaturalId）二级缓存 Region，基于 Redisson {@link RMapCache}。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public RedissonNaturalIdRegion(RMapCache<Object, Object> mapCache, ServiceManager serviceManager, RegionFactory regionFactory,\n"
        "                                   CacheDataDescription metadata, Settings settings, Properties properties, String defaultKey, CacheKeysFactory cacheKeysFactory) {",
        "    /** @param cacheKeysFactory 缓存键工厂，用于自然 ID 键的生成与解析 */\n"
        "    public RedissonNaturalIdRegion(RMapCache<Object, Object> mapCache, ServiceManager serviceManager, RegionFactory regionFactory,\n"
        "                                   CacheDataDescription metadata, Settings settings, Properties properties, String defaultKey, CacheKeysFactory cacheKeysFactory) {",
    ),
    (
        "    public CacheKeysFactory getCacheKeysFactory() {",
        "    /** 返回本 Region 使用的 {@link CacheKeysFactory}。 */\n"
        "    public CacheKeysFactory getCacheKeysFactory() {",
    ),
    (
        "    @Override\n    public NaturalIdRegionAccessStrategy buildAccessStrategy(AccessType accessType) throws CacheException {",
        "    /** 按 {@link AccessType} 构建自然 ID Region 的并发访问策略。 */\n"
        "    @Override\n"
        "    public NaturalIdRegionAccessStrategy buildAccessStrategy(AccessType accessType) throws CacheException {",
    ),
]

W2A_REPLACEMENTS["RedissonQueryRegion.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Hibernate 5 查询结果二级缓存 Region，基于 Redisson {@link RMapCache}。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public RedissonQueryRegion(RMapCache<Object, Object> mapCache, ServiceManager serviceManager,\n"
        "            RegionFactory regionFactory, Properties properties, String defaultKey) {",
        "    /** 构造查询结果 Region，metadata 为 null（非实体类缓存）。 */\n"
        "    public RedissonQueryRegion(RMapCache<Object, Object> mapCache, ServiceManager serviceManager,\n"
        "            RegionFactory regionFactory, Properties properties, String defaultKey) {",
    ),
]

W2A_REPLACEMENTS["RedissonTimestampsRegion.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Hibernate 5 查询失效时间戳 Region，基于 Redisson {@link RMapCache}。\n"
        " * <p>用于跟踪表/空间更新时间以支持查询缓存失效。</p>\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public RedissonTimestampsRegion(RMapCache<Object, Object> mapCache, ServiceManager serviceManager,\n"
        "            RegionFactory regionFactory, Properties properties, String defaultKey) {",
        "    /** 构造时间戳 Region，metadata 为 null（非实体类缓存）。 */\n"
        "    public RedissonTimestampsRegion(RMapCache<Object, Object> mapCache, ServiceManager serviceManager,\n"
        "            RegionFactory regionFactory, Properties properties, String defaultKey) {",
    ),
]

W2A_REPLACEMENTS["AbstractReadWriteAccessStrategy.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 读写（READ_WRITE）缓存并发访问策略的抽象基类（Hibernate 5）。\n"
        " * <p>委托 {@link GeneralDataRegion} 完成 get/put，解锁时驱逐条目。</p>\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    final RMapCache<Object, Object> mapCache;",
        "    /** 底层 Redisson 带 TTL 的 Map 缓存。 */\n"
        "    final RMapCache<Object, Object> mapCache;",
    ),
    (
        "    public AbstractReadWriteAccessStrategy(Settings settings, GeneralDataRegion region, RMapCache<Object, Object> mapCache) {",
        "    /** @param settings Hibernate 缓存配置\n"
        "     * @param region 通用数据区域\n"
        "     * @param mapCache 底层 Redisson Map 缓存\n"
        "     */\n"
        "    public AbstractReadWriteAccessStrategy(Settings settings, GeneralDataRegion region, RMapCache<Object, Object> mapCache) {",
    ),
    (
        "    @Override\n    public Object get(SessionImplementor session, Object key, long txTimestamp) throws CacheException {",
        "    /** 从 Region 读取缓存条目（不校验版本）。 */\n"
        "    @Override\n"
        "    public Object get(SessionImplementor session, Object key, long txTimestamp) throws CacheException {",
    ),
    (
        "    @Override\n    public boolean putFromLoad(SessionImplementor session, Object key, Object value, long txTimestamp, Object version, boolean minimalPutOverride)",
        "    /** 加载后写入缓存并始终返回 true。 */\n"
        "    @Override\n"
        "    public boolean putFromLoad(SessionImplementor session, Object key, Object value, long txTimestamp, Object version, boolean minimalPutOverride)",
    ),
    (
        "    @Override\n    public SoftLock lockItem(SessionImplementor session, Object key, Object version) throws CacheException {",
        "    /** 当前实现不使用软锁，直接返回 null。 */\n"
        "    @Override\n"
        "    public SoftLock lockItem(SessionImplementor session, Object key, Object version) throws CacheException {",
    ),
    (
        "    @Override\n    public void unlockItem(SessionImplementor session, Object key, SoftLock lock) throws CacheException {",
        "    /** 解锁时驱逐对应缓存条目。 */\n"
        "    @Override\n"
        "    public void unlockItem(SessionImplementor session, Object key, SoftLock lock) throws CacheException {",
    ),
]

W2A_REPLACEMENTS["BaseRegionAccessStrategy.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Hibernate 5 缓存区域访问策略的抽象基类。\n"
        " * <p>封装 {@link GeneralDataRegion} 与 {@link Settings}，提供区域锁、逐出\n"
        " * 以及带最小写入开关的 {@link #putFromLoad} 等默认实现。</p>\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "abstract class BaseRegionAccessStrategy implements RegionAccessStrategy {\n\n    final GeneralDataRegion region;\n    final Settings settings;",
        "abstract class BaseRegionAccessStrategy implements RegionAccessStrategy {\n\n"
        "    /** 关联的通用数据区域。 */\n"
        "    final GeneralDataRegion region;\n"
        "    /** Hibernate 缓存相关配置。 */\n"
        "    final Settings settings;",
    ),
    (
        "    BaseRegionAccessStrategy(Settings settings, GeneralDataRegion region) {",
        "    /** 绑定 Hibernate 设置与底层缓存区域。\n"
        "     *\n"
        "     * @param settings Hibernate 缓存配置\n"
        "     * @param region 通用数据区域实例\n"
        "     */\n"
        "    BaseRegionAccessStrategy(Settings settings, GeneralDataRegion region) {",
    ),
    (
        "    @Override\n    public boolean putFromLoad(SessionImplementor session, Object key, Object value, long txTimestamp, Object version) throws CacheException {\n        return putFromLoad(session, key, value, txTimestamp, version, settings.isMinimalPutsEnabled() );\n    }",
        "    /** 按全局最小写入开关委托至六参数 {@link #putFromLoad}。 */\n"
        "    @Override\n"
        "    public boolean putFromLoad(SessionImplementor session, Object key, Object value, long txTimestamp, Object version) throws CacheException {\n"
        "        return putFromLoad(session, key, value, txTimestamp, version, settings.isMinimalPutsEnabled() );\n"
        "    }",
    ),
    (
        "    @Override\n    public SoftLock lockRegion() throws CacheException {\n        return null;\n    }",
        "    /** 区域级锁：本实现不支持，始终返回 {@code null}。 */\n"
        "    @Override\n"
        "    public SoftLock lockRegion() throws CacheException {\n"
        "        return null;\n"
        "    }",
    ),
    (
        "    @Override\n    public void unlockRegion(SoftLock lock) throws CacheException {\n        region.evictAll();\n    }",
        "    /** 解锁区域时清空整个缓存区域。 */\n"
        "    @Override\n"
        "    public void unlockRegion(SoftLock lock) throws CacheException {\n"
        "        region.evictAll();\n"
        "    }",
    ),
    (
        "    @Override\n    public void remove(SessionImplementor session, Object key) throws CacheException {\n    }",
        "    /** 移除键：基类为空实现，由子类按需覆盖。 */\n"
        "    @Override\n"
        "    public void remove(SessionImplementor session, Object key) throws CacheException {\n"
        "    }",
    ),
    (
        "    @Override\n    public void removeAll() throws CacheException {\n        region.evictAll();\n    }",
        "    /** 移除全部条目，委托区域 {@link GeneralDataRegion#evictAll()}。 */\n"
        "    @Override\n"
        "    public void removeAll() throws CacheException {\n"
        "        region.evictAll();\n"
        "    }",
    ),
    (
        "    @Override\n    public void evict(Object key) throws CacheException {\n        region.evict(key);\n    }",
        "    /** 逐出指定键对应的缓存条目。 */\n"
        "    @Override\n"
        "    public void evict(Object key) throws CacheException {\n"
        "        region.evict(key);\n"
        "    }",
    ),
    (
        "    @Override\n    public void evictAll() throws CacheException {\n        region.evictAll();\n    }",
        "    /** 逐出区域内全部条目。 */\n"
        "    @Override\n"
        "    public void evictAll() throws CacheException {\n"
        "        region.evictAll();\n"
        "    }",
    ),
]

W2A_REPLACEMENTS["NonStrictReadWriteCollectionRegionAccessStrategy.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 集合并发策略 {@code nonstrict-read-write} 的 Redisson 区域访问实现（Hibernate 5）。\n"
        " * <p>不保证读写一致性，允许脏读；项级锁为空操作，解锁或移除时逐出键。</p>\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public NonStrictReadWriteCollectionRegionAccessStrategy(Settings settings, GeneralDataRegion region) {",
        "    /** @param settings Hibernate 缓存配置\n     * @param region 集合缓存区域\n     */\n"
        "    public NonStrictReadWriteCollectionRegionAccessStrategy(Settings settings, GeneralDataRegion region) {",
    ),
    (
        "    @Override\n    public CollectionRegion getRegion() {",
        "    /** 返回强类型 {@link CollectionRegion} 视图。 */\n"
        "    @Override\n"
        "    public CollectionRegion getRegion() {",
    ),
    (
        "    @Override\n    public Object get(SessionImplementor session, Object key, long txTimestamp) throws CacheException {",
        "    /** 直接读取缓存，不校验事务时间戳。 */\n"
        "    @Override\n"
        "    public Object get(SessionImplementor session, Object key, long txTimestamp) throws CacheException {",
    ),
    (
        "        if (minimalPutOverride && region.contains(key)) {\n            return false;\n        }",
        "        // 最小写入模式下键已存在则跳过写入。\n"
        "        if (minimalPutOverride && region.contains(key)) {\n"
        "            return false;\n        }",
    ),
    (
        "    @Override\n    public SoftLock lockItem(SessionImplementor session, Object key, Object version) throws CacheException {\n        return null;\n    }",
        "    /** 非严格读写不提供项级软锁。 */\n"
        "    @Override\n"
        "    public SoftLock lockItem(SessionImplementor session, Object key, Object version) throws CacheException {\n"
        "        return null;\n"
        "    }",
    ),
    (
        "    @Override\n    public void unlockItem(SessionImplementor session, Object key, SoftLock lock) throws CacheException {\n        evict(key);\n    }",
        "    /** 解锁时逐出键，使后续读取重新加载。 */\n"
        "    @Override\n"
        "    public void unlockItem(SessionImplementor session, Object key, SoftLock lock) throws CacheException {\n"
        "        evict(key);\n"
        "    }",
    ),
    (
        "    @Override\n    public void remove(SessionImplementor session, Object key) throws CacheException {\n        evict(key);\n    }",
        "    /** 移除键等同于逐出缓存条目。 */\n"
        "    @Override\n"
        "    public void remove(SessionImplementor session, Object key) throws CacheException {\n"
        "        evict(key);\n"
        "    }",
    ),
    (
        "    @Override\n    public Object generateCacheKey(Object id, CollectionPersister persister, SessionFactoryImplementor factory,\n            String tenantIdentifier) {",
        "    /** 委托 Region 的 {@link CacheKeysFactory} 生成集合缓存键。 */\n"
        "    @Override\n"
        "    public Object generateCacheKey(Object id, CollectionPersister persister, SessionFactoryImplementor factory,\n"
        "            String tenantIdentifier) {",
    ),
    (
        "    @Override\n    public Object getCacheKeyId(Object cacheKey) {",
        "    /** 从缓存键解析集合 ID。 */\n"
        "    @Override\n"
        "    public Object getCacheKeyId(Object cacheKey) {",
    ),
]

W2A_REPLACEMENTS["NonStrictReadWriteEntityRegionAccessStrategy.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 实体并发策略 {@code nonstrict-read-write} 的 Redisson 区域访问实现（Hibernate 5）。\n"
        " * <p>允许脏读；更新时先移除键，插入/更新后不主动写回缓存。</p>\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public NonStrictReadWriteEntityRegionAccessStrategy(Settings settings, GeneralDataRegion region) {",
        "    /** @param settings Hibernate 缓存配置\n     * @param region 实体缓存区域\n     */\n"
        "    public NonStrictReadWriteEntityRegionAccessStrategy(Settings settings, GeneralDataRegion region) {",
    ),
    (
        "    @Override\n    public Object get(SessionImplementor session, Object key, long txTimestamp) throws CacheException {",
        "    /** 直接读取缓存，不校验事务时间戳。 */\n"
        "    @Override\n"
        "    public Object get(SessionImplementor session, Object key, long txTimestamp) throws CacheException {",
    ),
    (
        "        if (minimalPutOverride && region.contains(key)) {\n            return false;\n        }",
        "        // 最小写入模式下键已存在则跳过写入。\n"
        "        if (minimalPutOverride && region.contains(key)) {\n"
        "            return false;\n        }",
    ),
    (
        "    @Override\n    public SoftLock lockItem(SessionImplementor session, Object key, Object version) throws CacheException {\n        return null;\n    }",
        "    /** 非严格读写不提供项级软锁。 */\n"
        "    @Override\n"
        "    public SoftLock lockItem(SessionImplementor session, Object key, Object version) throws CacheException {\n"
        "        return null;\n"
        "    }",
    ),
    (
        "    @Override\n    public void unlockItem(SessionImplementor session, Object key, SoftLock lock) throws CacheException {\n        evict(key);\n    }",
        "    /** 解锁时逐出键。 */\n"
        "    @Override\n"
        "    public void unlockItem(SessionImplementor session, Object key, SoftLock lock) throws CacheException {\n"
        "        evict(key);\n"
        "    }",
    ),
    (
        "    @Override\n    public EntityRegion getRegion() {",
        "    /** 返回强类型 {@link EntityRegion} 视图。 */\n"
        "    @Override\n"
        "    public EntityRegion getRegion() {",
    ),
    (
        "    @Override\n    public boolean insert(SessionImplementor session, Object key, Object value, Object version) throws CacheException {\n        return false;\n    }",
        "    /** 插入阶段不写缓存，延迟至 afterInsert。 */\n"
        "    @Override\n"
        "    public boolean insert(SessionImplementor session, Object key, Object value, Object version) throws CacheException {\n"
        "        return false;\n"
        "    }",
    ),
    (
        "    @Override\n    public boolean afterInsert(SessionImplementor session, Object key, Object value, Object version) throws CacheException {\n        return false;\n    }",
        "    /** 插入完成后也不写回缓存。 */\n"
        "    @Override\n"
        "    public boolean afterInsert(SessionImplementor session, Object key, Object value, Object version) throws CacheException {\n"
        "        return false;\n"
        "    }",
    ),
    (
        "    @Override\n    public boolean update(SessionImplementor session, Object key, Object value, Object currentVersion, Object previousVersion)\n            throws CacheException {\n        remove(session, key);\n        return false;\n    }",
        "    /** 更新时移除旧缓存条目，不立即写入新值。 */\n"
        "    @Override\n"
        "    public boolean update(SessionImplementor session, Object key, Object value, Object currentVersion, Object previousVersion)\n"
        "            throws CacheException {\n"
        "        remove(session, key);\n"
        "        return false;\n"
        "    }",
    ),
    (
        "    @Override\n    public boolean afterUpdate(SessionImplementor session, Object key, Object value, Object currentVersion, Object previousVersion, SoftLock lock)\n            throws CacheException {\n        unlockItem(session, key, lock);\n        return false;\n    }",
        "    /** 更新完成后解锁并逐出，不写回新值。 */\n"
        "    @Override\n"
        "    public boolean afterUpdate(SessionImplementor session, Object key, Object value, Object currentVersion, Object previousVersion, SoftLock lock)\n"
        "            throws CacheException {\n"
        "        unlockItem(session, key, lock);\n"
        "        return false;\n"
        "    }",
    ),
    (
        "    @Override\n    public void remove(SessionImplementor session, Object key) throws CacheException {\n        evict(key);\n    }",
        "    /** 移除键等同于逐出缓存条目。 */\n"
        "    @Override\n"
        "    public void remove(SessionImplementor session, Object key) throws CacheException {\n"
        "        evict(key);\n"
        "    }",
    ),
    (
        "    @Override\n    public Object generateCacheKey(Object id, EntityPersister persister, SessionFactoryImplementor factory, String tenantIdentifier) {",
        "    /** 委托 Region 的 {@link CacheKeysFactory} 生成实体缓存键。 */\n"
        "    @Override\n"
        "    public Object generateCacheKey(Object id, EntityPersister persister, SessionFactoryImplementor factory, String tenantIdentifier) {",
    ),
    (
        "    @Override\n    public Object getCacheKeyId(Object cacheKey) {",
        "    /** 从缓存键解析实体 ID。 */\n"
        "    @Override\n"
        "    public Object getCacheKeyId(Object cacheKey) {",
    ),
]

W2A_REPLACEMENTS["NonStrictReadWriteNaturalIdRegionAccessStrategy.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 自然 ID 并发策略 {@code nonstrict-read-write} 的 Redisson 区域访问实现（Hibernate 5）。\n"
        " * <p>允许脏读；更新时移除键，插入/更新后不主动写回缓存。</p>\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public NonStrictReadWriteNaturalIdRegionAccessStrategy(Settings settings, GeneralDataRegion region) {",
        "    /** @param settings Hibernate 缓存配置\n     * @param region 自然 ID 缓存区域\n     */\n"
        "    public NonStrictReadWriteNaturalIdRegionAccessStrategy(Settings settings, GeneralDataRegion region) {",
    ),
    (
        "    @Override\n    public Object get(SessionImplementor session, Object key, long txTimestamp) throws CacheException {",
        "    /** 直接读取缓存，不校验事务时间戳。 */\n"
        "    @Override\n"
        "    public Object get(SessionImplementor session, Object key, long txTimestamp) throws CacheException {",
    ),
    (
        "        if (minimalPutOverride && region.contains(key)) {\n            return false;\n        }",
        "        // 最小写入模式下键已存在则跳过写入。\n"
        "        if (minimalPutOverride && region.contains(key)) {\n"
        "            return false;\n        }",
    ),
    (
        "    @Override\n    public SoftLock lockItem(SessionImplementor session, Object key, Object version) throws CacheException {\n        return null;\n    }",
        "    /** 非严格读写不提供项级软锁。 */\n"
        "    @Override\n"
        "    public SoftLock lockItem(SessionImplementor session, Object key, Object version) throws CacheException {\n"
        "        return null;\n"
        "    }",
    ),
    (
        "    @Override\n    public void unlockItem(SessionImplementor session, Object key, SoftLock lock) throws CacheException {\n        evict(key);\n    }",
        "    /** 解锁时逐出键。 */\n"
        "    @Override\n"
        "    public void unlockItem(SessionImplementor session, Object key, SoftLock lock) throws CacheException {\n"
        "        evict(key);\n"
        "    }",
    ),
    (
        "    @Override\n    public NaturalIdRegion getRegion() {",
        "    /** 返回强类型 {@link NaturalIdRegion} 视图。 */\n"
        "    @Override\n"
        "    public NaturalIdRegion getRegion() {",
    ),
    (
        "    @Override\n    public boolean insert(SessionImplementor session, Object key, Object value) throws CacheException {\n        return false;\n    }",
        "    /** 插入阶段不写缓存。 */\n"
        "    @Override\n"
        "    public boolean insert(SessionImplementor session, Object key, Object value) throws CacheException {\n"
        "        return false;\n"
        "    }",
    ),
    (
        "    @Override\n    public boolean afterInsert(SessionImplementor session, Object key, Object value) throws CacheException {\n        return false;\n    }",
        "    /** 插入完成后也不写回缓存。 */\n"
        "    @Override\n"
        "    public boolean afterInsert(SessionImplementor session, Object key, Object value) throws CacheException {\n"
        "        return false;\n"
        "    }",
    ),
    (
        "    @Override\n    public boolean update(SessionImplementor session, Object key, Object value) throws CacheException {\n        remove(session, key);\n        return false;\n    }",
        "    /** 更新时移除旧缓存条目。 */\n"
        "    @Override\n"
        "    public boolean update(SessionImplementor session, Object key, Object value) throws CacheException {\n"
        "        remove(session, key);\n"
        "        return false;\n"
        "    }",
    ),
    (
        "    @Override\n    public boolean afterUpdate(SessionImplementor session, Object key, Object value, SoftLock lock) throws CacheException {\n        unlockItem(session, key, lock);\n        return false;\n    }",
        "    /** 更新完成后解锁并逐出。 */\n"
        "    @Override\n"
        "    public boolean afterUpdate(SessionImplementor session, Object key, Object value, SoftLock lock) throws CacheException {\n"
        "        unlockItem(session, key, lock);\n"
        "        return false;\n"
        "    }",
    ),
    (
        "    @Override\n    public void remove(SessionImplementor session, Object key) throws CacheException {\n        region.evict(key);\n    }",
        "    /** 移除指定自然 ID 对应的缓存条目。 */\n"
        "    @Override\n"
        "    public void remove(SessionImplementor session, Object key) throws CacheException {\n"
        "        region.evict(key);\n"
        "    }",
    ),
    (
        "    @Override\n    public Object generateCacheKey(Object[] naturalIdValues, EntityPersister persister, SessionImplementor session) {",
        "    /** 委托 Region 的 {@link CacheKeysFactory} 生成自然 ID 缓存键。 */\n"
        "    @Override\n"
        "    public Object generateCacheKey(Object[] naturalIdValues, EntityPersister persister, SessionImplementor session) {",
    ),
    (
        "    @Override\n    public Object[] getNaturalIdValues(Object cacheKey) {",
        "    /** 从缓存键解析自然 ID 值数组。 */\n"
        "    @Override\n"
        "    public Object[] getNaturalIdValues(Object cacheKey) {",
    ),
]
