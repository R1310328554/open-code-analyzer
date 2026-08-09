"""Chinese annotation replacements for Redisson 4.7.0 wave-3a hibernate-52 [0:15]."""

W3A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

W3A_REPLACEMENTS["BaseRegion.java"] = [
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
        "    @Override\n    public Object get(SharedSessionContractImplementor session, Object key) throws CacheException {",
        "    /** 从缓存读取条目；fallback 模式下返回 null 而不访问 Redis。 */\n"
        "    @Override\n"
        "    public Object get(SharedSessionContractImplementor session, Object key) throws CacheException {",
    ),
    (
        "            if (maxIdle == 0 && size == 0) {\n                return mapCache.getWithTTLOnly(key);",
        "            // 无 maxIdle 与 maxSize 限制时使用仅 TTL 的读取路径。\n"
        "            if (maxIdle == 0 && size == 0) {\n"
        "                return mapCache.getWithTTLOnly(key);",
    ),
    (
        "    @Override\n    public void put(SharedSessionContractImplementor session, Object key, Object value) throws CacheException {",
        "    /** 写入缓存条目，应用 Region 配置的 TTL 与 maxIdle。 */\n"
        "    @Override\n"
        "    public void put(SharedSessionContractImplementor session, Object key, Object value) throws CacheException {",
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

W3A_REPLACEMENTS["RedissonCollectionRegion.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Hibernate 5.2 集合（Collection）二级缓存 Region，基于 Redisson {@link RMapCache}。\n"
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

W3A_REPLACEMENTS["RedissonEntityRegion.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Hibernate 5.2 实体二级缓存 Region，基于 Redisson {@link RMapCache}。\n"
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

W3A_REPLACEMENTS["RedissonNaturalIdRegion.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Hibernate 5.2 自然 ID（NaturalId）二级缓存 Region，基于 Redisson {@link RMapCache}。\n"
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

W3A_REPLACEMENTS["RedissonQueryRegion.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Hibernate 5.2 查询结果二级缓存 Region，基于 Redisson {@link RMapCache}。\n"
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

W3A_REPLACEMENTS["RedissonTimestampsRegion.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Hibernate 5.2 查询失效时间戳 Region，基于 Redisson {@link RMapCache}。\n"
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

W3A_REPLACEMENTS["AbstractReadWriteAccessStrategy.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 读写（READ_WRITE）缓存并发访问策略的抽象基类（Hibernate 5.2）。\n"
        " * <p>委托 {@link GeneralDataRegion} 完成 get/put，解锁时驱逐条目。</p>\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public AbstractReadWriteAccessStrategy(Settings settings, GeneralDataRegion region, RMapCache<Object, Object> mapCache) {",
        "    /** @param settings Hibernate 缓存配置\n"
        "     * @param region 通用数据区域\n"
        "     * @param mapCache 底层 Redisson Map 缓存（保留参数以兼容子类构造签名）\n"
        "     */\n"
        "    public AbstractReadWriteAccessStrategy(Settings settings, GeneralDataRegion region, RMapCache<Object, Object> mapCache) {",
    ),
    (
        "    @Override\n    public Object get(SharedSessionContractImplementor session, Object key, long txTimestamp) throws CacheException {",
        "    /** 从 Region 读取缓存条目（不校验版本）。 */\n"
        "    @Override\n"
        "    public Object get(SharedSessionContractImplementor session, Object key, long txTimestamp) throws CacheException {",
    ),
    (
        "    @Override\n    public boolean putFromLoad(SharedSessionContractImplementor session, Object key, Object value, long txTimestamp, Object version, boolean minimalPutOverride)",
        "    /** 加载后写入缓存并始终返回 true。 */\n"
        "    @Override\n"
        "    public boolean putFromLoad(SharedSessionContractImplementor session, Object key, Object value, long txTimestamp, Object version, boolean minimalPutOverride)",
    ),
    (
        "    @Override\n    public SoftLock lockItem(SharedSessionContractImplementor session, Object key, Object version) throws CacheException {",
        "    /** 当前实现不使用软锁，直接返回 null。 */\n"
        "    @Override\n"
        "    public SoftLock lockItem(SharedSessionContractImplementor session, Object key, Object version) throws CacheException {",
    ),
    (
        "    @Override\n    public void unlockItem(SharedSessionContractImplementor session, Object key, SoftLock lock) throws CacheException {",
        "    /** 解锁时驱逐对应缓存条目。 */\n"
        "    @Override\n"
        "    public void unlockItem(SharedSessionContractImplementor session, Object key, SoftLock lock) throws CacheException {",
    ),
]

W3A_REPLACEMENTS["BaseRegionAccessStrategy.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Hibernate 5.2 缓存区域访问策略的抽象基类。\n"
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
        "    @Override\n    public boolean putFromLoad(SharedSessionContractImplementor session, Object key, Object value, long txTimestamp, Object version) throws CacheException {\n        return putFromLoad(session, key, value, txTimestamp, version, settings.isMinimalPutsEnabled() );\n    }",
        "    /** 按全局最小写入开关委托至六参数 {@link #putFromLoad}。 */\n"
        "    @Override\n"
        "    public boolean putFromLoad(SharedSessionContractImplementor session, Object key, Object value, long txTimestamp, Object version) throws CacheException {\n"
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
        "    @Override\n    public void remove(SharedSessionContractImplementor session, Object key) throws CacheException {\n    }",
        "    /** 移除键：基类为空实现，由子类按需覆盖。 */\n"
        "    @Override\n"
        "    public void remove(SharedSessionContractImplementor session, Object key) throws CacheException {\n"
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

W3A_REPLACEMENTS["NonStrictReadWriteCollectionRegionAccessStrategy.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 集合并发策略 {@code nonstrict-read-write} 的 Redisson 区域访问实现（Hibernate 5.2）。\n"
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
        "    @Override\n    public Object get(SharedSessionContractImplementor session, Object key, long txTimestamp) throws CacheException {",
        "    /** 直接读取缓存，不校验事务时间戳。 */\n"
        "    @Override\n"
        "    public Object get(SharedSessionContractImplementor session, Object key, long txTimestamp) throws CacheException {",
    ),
    (
        "        if (minimalPutOverride && region.contains(key)) {\n            return false;\n        }",
        "        // 最小写入模式下键已存在则跳过写入。\n"
        "        if (minimalPutOverride && region.contains(key)) {\n"
        "            return false;\n        }",
    ),
    (
        "    @Override\n    public SoftLock lockItem(SharedSessionContractImplementor session, Object key, Object version) throws CacheException {\n        return null;\n    }",
        "    /** 非严格读写不提供项级软锁。 */\n"
        "    @Override\n"
        "    public SoftLock lockItem(SharedSessionContractImplementor session, Object key, Object version) throws CacheException {\n"
        "        return null;\n"
        "    }",
    ),
    (
        "    @Override\n    public void unlockItem(SharedSessionContractImplementor session, Object key, SoftLock lock) throws CacheException {\n        evict(key);\n    }",
        "    /** 解锁时逐出键，使后续读取重新加载。 */\n"
        "    @Override\n"
        "    public void unlockItem(SharedSessionContractImplementor session, Object key, SoftLock lock) throws CacheException {\n"
        "        evict(key);\n"
        "    }",
    ),
    (
        "    @Override\n    public void remove(SharedSessionContractImplementor session, Object key) throws CacheException {\n        evict(key);\n    }",
        "    /** 移除键等同于逐出缓存条目。 */\n"
        "    @Override\n"
        "    public void remove(SharedSessionContractImplementor session, Object key) throws CacheException {\n"
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

W3A_REPLACEMENTS["NonStrictReadWriteEntityRegionAccessStrategy.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 实体并发策略 {@code nonstrict-read-write} 的 Redisson 区域访问实现（Hibernate 5.2）。\n"
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
        "    @Override\n    public Object get(SharedSessionContractImplementor session, Object key, long txTimestamp) throws CacheException {",
        "    /** 直接读取缓存，不校验事务时间戳。 */\n"
        "    @Override\n"
        "    public Object get(SharedSessionContractImplementor session, Object key, long txTimestamp) throws CacheException {",
    ),
    (
        "        if (minimalPutOverride && region.contains(key)) {\n            return false;\n        }",
        "        // 最小写入模式下键已存在则跳过写入。\n"
        "        if (minimalPutOverride && region.contains(key)) {\n"
        "            return false;\n        }",
    ),
    (
        "    @Override\n    public SoftLock lockItem(SharedSessionContractImplementor session, Object key, Object version) throws CacheException {\n        return null;\n    }",
        "    /** 非严格读写不提供项级软锁。 */\n"
        "    @Override\n"
        "    public SoftLock lockItem(SharedSessionContractImplementor session, Object key, Object version) throws CacheException {\n"
        "        return null;\n"
        "    }",
    ),
    (
        "    @Override\n    public void unlockItem(SharedSessionContractImplementor session, Object key, SoftLock lock) throws CacheException {\n        evict(key);\n    }",
        "    /** 解锁时逐出键。 */\n"
        "    @Override\n"
        "    public void unlockItem(SharedSessionContractImplementor session, Object key, SoftLock lock) throws CacheException {\n"
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
        "    @Override\n    public boolean insert(SharedSessionContractImplementor session, Object key, Object value, Object version) throws CacheException {\n        return false;\n    }",
        "    /** 插入阶段不写缓存，延迟至 afterInsert。 */\n"
        "    @Override\n"
        "    public boolean insert(SharedSessionContractImplementor session, Object key, Object value, Object version) throws CacheException {\n"
        "        return false;\n"
        "    }",
    ),
    (
        "    @Override\n    public boolean afterInsert(SharedSessionContractImplementor session, Object key, Object value, Object version) throws CacheException {\n        return false;\n    }",
        "    /** 插入完成后也不写回缓存。 */\n"
        "    @Override\n"
        "    public boolean afterInsert(SharedSessionContractImplementor session, Object key, Object value, Object version) throws CacheException {\n"
        "        return false;\n"
        "    }",
    ),
    (
        "    @Override\n    public boolean update(SharedSessionContractImplementor session, Object key, Object value, Object currentVersion, Object previousVersion)\n            throws CacheException {\n        remove(session, key);\n        return false;\n    }",
        "    /** 更新时移除旧缓存条目，不立即写入新值。 */\n"
        "    @Override\n"
        "    public boolean update(SharedSessionContractImplementor session, Object key, Object value, Object currentVersion, Object previousVersion)\n"
        "            throws CacheException {\n"
        "        remove(session, key);\n"
        "        return false;\n"
        "    }",
    ),
    (
        "    @Override\n    public boolean afterUpdate(SharedSessionContractImplementor session, Object key, Object value, Object currentVersion, Object previousVersion, SoftLock lock)\n            throws CacheException {\n        unlockItem(session, key, lock);\n        return false;\n    }",
        "    /** 更新完成后解锁并逐出，不写回新值。 */\n"
        "    @Override\n"
        "    public boolean afterUpdate(SharedSessionContractImplementor session, Object key, Object value, Object currentVersion, Object previousVersion, SoftLock lock)\n"
        "            throws CacheException {\n"
        "        unlockItem(session, key, lock);\n"
        "        return false;\n"
        "    }",
    ),
    (
        "    @Override\n    public void remove(SharedSessionContractImplementor session, Object key) throws CacheException {\n        evict(key);\n    }",
        "    /** 移除键等同于逐出缓存条目。 */\n"
        "    @Override\n"
        "    public void remove(SharedSessionContractImplementor session, Object key) throws CacheException {\n"
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

W3A_REPLACEMENTS["NonStrictReadWriteNaturalIdRegionAccessStrategy.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 自然 ID 并发策略 {@code nonstrict-read-write} 的 Redisson 区域访问实现（Hibernate 5.2）。\n"
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
        "    @Override\n    public Object get(SharedSessionContractImplementor session, Object key, long txTimestamp) throws CacheException {",
        "    /** 直接读取缓存，不校验事务时间戳。 */\n"
        "    @Override\n"
        "    public Object get(SharedSessionContractImplementor session, Object key, long txTimestamp) throws CacheException {",
    ),
    (
        "        if (minimalPutOverride && region.contains(key)) {\n            return false;\n        }",
        "        // 最小写入模式下键已存在则跳过写入。\n"
        "        if (minimalPutOverride && region.contains(key)) {\n"
        "            return false;\n        }",
    ),
    (
        "    @Override\n    public SoftLock lockItem(SharedSessionContractImplementor session, Object key, Object version) throws CacheException {\n        return null;\n    }",
        "    /** 非严格读写不提供项级软锁。 */\n"
        "    @Override\n"
        "    public SoftLock lockItem(SharedSessionContractImplementor session, Object key, Object version) throws CacheException {\n"
        "        return null;\n"
        "    }",
    ),
    (
        "    @Override\n    public void unlockItem(SharedSessionContractImplementor session, Object key, SoftLock lock) throws CacheException {\n        evict(key);\n    }",
        "    /** 解锁时逐出键。 */\n"
        "    @Override\n"
        "    public void unlockItem(SharedSessionContractImplementor session, Object key, SoftLock lock) throws CacheException {\n"
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
        "    @Override\n    public boolean insert(SharedSessionContractImplementor session, Object key, Object value) throws CacheException {\n        return false;\n    }",
        "    /** 插入阶段不写缓存。 */\n"
        "    @Override\n"
        "    public boolean insert(SharedSessionContractImplementor session, Object key, Object value) throws CacheException {\n"
        "        return false;\n"
        "    }",
    ),
    (
        "    @Override\n    public boolean afterInsert(SharedSessionContractImplementor session, Object key, Object value) throws CacheException {\n        return false;\n    }",
        "    /** 插入完成后也不写回缓存。 */\n"
        "    @Override\n"
        "    public boolean afterInsert(SharedSessionContractImplementor session, Object key, Object value) throws CacheException {\n"
        "        return false;\n"
        "    }",
    ),
    (
        "    @Override\n    public boolean update(SharedSessionContractImplementor session, Object key, Object value) throws CacheException {\n        remove(session, key);\n        return false;\n    }",
        "    /** 更新时移除旧缓存条目。 */\n"
        "    @Override\n"
        "    public boolean update(SharedSessionContractImplementor session, Object key, Object value) throws CacheException {\n"
        "        remove(session, key);\n"
        "        return false;\n"
        "    }",
    ),
    (
        "    @Override\n    public boolean afterUpdate(SharedSessionContractImplementor session, Object key, Object value, SoftLock lock) throws CacheException {\n        unlockItem(session, key, lock);\n        return false;\n    }",
        "    /** 更新完成后解锁并逐出。 */\n"
        "    @Override\n"
        "    public boolean afterUpdate(SharedSessionContractImplementor session, Object key, Object value, SoftLock lock) throws CacheException {\n"
        "        unlockItem(session, key, lock);\n"
        "        return false;\n"
        "    }",
    ),
    (
        "    @Override\n    public void remove(SharedSessionContractImplementor session, Object key) throws CacheException {\n        region.evict(key);\n    }",
        "    /** 移除指定自然 ID 对应的缓存条目。 */\n"
        "    @Override\n"
        "    public void remove(SharedSessionContractImplementor session, Object key) throws CacheException {\n"
        "        region.evict(key);\n"
        "    }",
    ),
    (
        "    @Override\n    public Object generateCacheKey(Object[] naturalIdValues, EntityPersister persister, SharedSessionContractImplementor session) {",
        "    /** 委托 Region 的 {@link CacheKeysFactory} 生成自然 ID 缓存键。 */\n"
        "    @Override\n"
        "    public Object generateCacheKey(Object[] naturalIdValues, EntityPersister persister, SharedSessionContractImplementor session) {",
    ),
    (
        "    @Override\n    public Object[] getNaturalIdValues(Object cacheKey) {",
        "    /** 从缓存键解析自然 ID 值数组。 */\n"
        "    @Override\n"
        "    public Object[] getNaturalIdValues(Object cacheKey) {",
    ),
]

W3A_REPLACEMENTS["ReadOnlyCollectionRegionAccessStrategy.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 集合并发策略 {@code read-only} 的 Redisson 区域访问实现（Hibernate 5.2）。\n"
        " * <p>缓存内容不可变；解锁为空操作，不逐出条目。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public ReadOnlyCollectionRegionAccessStrategy(Settings settings, GeneralDataRegion region) {",
        "    /** @param settings Hibernate 缓存配置\n     * @param region 集合缓存区域\n     */\n"
        "    public ReadOnlyCollectionRegionAccessStrategy(Settings settings, GeneralDataRegion region) {",
    ),
    (
        "    @Override\n    public CollectionRegion getRegion() {",
        "    /** 返回强类型 {@link CollectionRegion} 视图。 */\n"
        "    @Override\n"
        "    public CollectionRegion getRegion() {",
    ),
    (
        "    @Override\n    public Object get(SharedSessionContractImplementor session, Object key, long txTimestamp) throws CacheException {",
        "    /** 在当前会话上下文中读取缓存条目。 */\n"
        "    @Override\n"
        "    public Object get(SharedSessionContractImplementor session, Object key, long txTimestamp) throws CacheException {",
    ),
    (
        "        if (minimalPutOverride && region.contains(key)) {\n            return false;\n        }",
        "        // 最小写入模式下键已存在则跳过写入。\n"
        "        if (minimalPutOverride && region.contains(key)) {\n"
        "            return false;\n        }",
    ),
    (
        "    @Override\n    public SoftLock lockItem(SharedSessionContractImplementor session, Object key, Object version) throws CacheException {\n        return null;\n    }",
        "    /** 只读策略不提供项级软锁。 */\n"
        "    @Override\n"
        "    public SoftLock lockItem(SharedSessionContractImplementor session, Object key, Object version) throws CacheException {\n"
        "        return null;\n"
        "    }",
    ),
    (
        "    @Override\n    public void unlockItem(SharedSessionContractImplementor session, Object key, SoftLock lock) throws CacheException {\n    }",
        "    /** 只读解锁为空操作，不修改缓存。 */\n"
        "    @Override\n"
        "    public void unlockItem(SharedSessionContractImplementor session, Object key, SoftLock lock) throws CacheException {\n"
        "    }",
    ),
    (
        "    @Override\n    public Object generateCacheKey(Object id, CollectionPersister persister, SessionFactoryImplementor factory, String tenantIdentifier) {",
        "    /** 通过 {@link RedissonCollectionRegion} 的键工厂生成集合缓存键。 */\n"
        "    @Override\n"
        "    public Object generateCacheKey(Object id, CollectionPersister persister, SessionFactoryImplementor factory, String tenantIdentifier) {",
    ),
    (
        "    @Override\n    public Object getCacheKeyId(Object cacheKey) {",
        "    /** 从缓存键反解集合标识符。 */\n"
        "    @Override\n"
        "    public Object getCacheKeyId(Object cacheKey) {",
    ),
]

W3A_REPLACEMENTS["ReadOnlyEntityRegionAccessStrategy.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 实体并发策略 {@code read-only} 的 Redisson 区域访问实现（Hibernate 5.2）。\n"
        " * <p>插入后写入缓存；后续更新抛出 {@link UnsupportedOperationException}。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public ReadOnlyEntityRegionAccessStrategy(Settings settings, GeneralDataRegion region) {",
        "    /** @param settings Hibernate 缓存配置\n     * @param region 实体缓存区域\n     */\n"
        "    public ReadOnlyEntityRegionAccessStrategy(Settings settings, GeneralDataRegion region) {",
    ),
    (
        "    @Override\n    public Object get(SharedSessionContractImplementor session, Object key, long txTimestamp) throws CacheException {",
        "    /** 在当前会话上下文中读取缓存条目。 */\n"
        "    @Override\n"
        "    public Object get(SharedSessionContractImplementor session, Object key, long txTimestamp) throws CacheException {",
    ),
    (
        "        if (minimalPutOverride && region.contains(key)) {\n            return false;\n        }",
        "        // 最小写入模式下键已存在则跳过写入。\n"
        "        if (minimalPutOverride && region.contains(key)) {\n"
        "            return false;\n        }",
    ),
    (
        "    @Override\n    public SoftLock lockItem(SharedSessionContractImplementor session, Object key, Object version) throws CacheException {\n        return null;\n    }",
        "    /** 只读策略不提供项级软锁。 */\n"
        "    @Override\n"
        "    public SoftLock lockItem(SharedSessionContractImplementor session, Object key, Object version) throws CacheException {\n"
        "        return null;\n"
        "    }",
    ),
    (
        "    @Override\n    public void unlockItem(SharedSessionContractImplementor session, Object key, SoftLock lock) throws CacheException {\n        evict(key);\n    }",
        "    /** 解锁时逐出键（与 Hibernate 只读语义一致）。 */\n"
        "    @Override\n"
        "    public void unlockItem(SharedSessionContractImplementor session, Object key, SoftLock lock) throws CacheException {\n"
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
        "    @Override\n    public boolean insert(SharedSessionContractImplementor session, Object key, Object value, Object version) throws CacheException {\n        return false;\n    }",
        "    /** 插入阶段不写缓存，延迟至 afterInsert。 */\n"
        "    @Override\n"
        "    public boolean insert(SharedSessionContractImplementor session, Object key, Object value, Object version) throws CacheException {\n"
        "        return false;\n"
        "    }",
    ),
    (
        "    @Override\n    public boolean afterInsert(SharedSessionContractImplementor session, Object key, Object value, Object version) throws CacheException {\n        region.put(session, key, value);\n        return true;\n    }",
        "    /** 插入完成后将实体写入缓存。 */\n"
        "    @Override\n"
        "    public boolean afterInsert(SharedSessionContractImplementor session, Object key, Object value, Object version) throws CacheException {\n"
        "        region.put(session, key, value);\n"
        "        return true;\n"
        "    }",
    ),
    (
        "    @Override\n    public boolean update(SharedSessionContractImplementor session, Object key, Object value, Object currentVersion, Object previousVersion)\n            throws CacheException {\n        throw new UnsupportedOperationException(\"Unable to update read-only object\");\n    }",
        "    /** 只读实体禁止更新。 */\n"
        "    @Override\n"
        "    public boolean update(SharedSessionContractImplementor session, Object key, Object value, Object currentVersion, Object previousVersion)\n"
        "            throws CacheException {\n"
        "        throw new UnsupportedOperationException(\"Unable to update read-only object\");\n"
        "    }",
    ),
    (
        "    @Override\n    public boolean afterUpdate(SharedSessionContractImplementor session, Object key, Object value, Object currentVersion, Object previousVersion, SoftLock lock)\n            throws CacheException {\n        throw new UnsupportedOperationException(\"Unable to update read-only object\");\n    }",
        "    /** 只读实体禁止 afterUpdate。 */\n"
        "    @Override\n"
        "    public boolean afterUpdate(SharedSessionContractImplementor session, Object key, Object value, Object currentVersion, Object previousVersion, SoftLock lock)\n"
        "            throws CacheException {\n"
        "        throw new UnsupportedOperationException(\"Unable to update read-only object\");\n"
        "    }",
    ),
    (
        "    @Override\n    public Object generateCacheKey(Object id, EntityPersister persister, SessionFactoryImplementor factory, String tenantIdentifier) {",
        "    /** 通过 {@link RedissonEntityRegion} 的键工厂生成实体缓存键。 */\n"
        "    @Override\n"
        "    public Object generateCacheKey(Object id, EntityPersister persister, SessionFactoryImplementor factory, String tenantIdentifier) {",
    ),
    (
        "    @Override\n    public Object getCacheKeyId(Object cacheKey) {",
        "    /** 从缓存键反解实体标识符。 */\n"
        "    @Override\n"
        "    public Object getCacheKeyId(Object cacheKey) {",
    ),
]

W3A_REPLACEMENTS["ReadOnlyNaturalIdRegionAccessStrategy.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 自然 ID 并发策略 {@code read-only} 的 Redisson 区域访问实现（Hibernate 5.2）。\n"
        " * <p>插入后写入缓存；后续更新抛出 {@link UnsupportedOperationException}。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public ReadOnlyNaturalIdRegionAccessStrategy(Settings settings, GeneralDataRegion region) {",
        "    /** @param settings Hibernate 缓存配置\n     * @param region 自然 ID 缓存区域\n     */\n"
        "    public ReadOnlyNaturalIdRegionAccessStrategy(Settings settings, GeneralDataRegion region) {",
    ),
    (
        "    @Override\n    public Object get(SharedSessionContractImplementor session, Object key, long txTimestamp) throws CacheException {",
        "    /** 在当前会话上下文中读取缓存条目。 */\n"
        "    @Override\n"
        "    public Object get(SharedSessionContractImplementor session, Object key, long txTimestamp) throws CacheException {",
    ),
    (
        "        if (minimalPutOverride && region.contains(key)) {\n            return false;\n        }",
        "        // 最小写入模式下键已存在则跳过写入。\n"
        "        if (minimalPutOverride && region.contains(key)) {\n"
        "            return false;\n        }",
    ),
    (
        "    @Override\n    public SoftLock lockItem(SharedSessionContractImplementor session, Object key, Object version) throws CacheException {\n        return null;\n    }",
        "    /** 只读策略不提供项级软锁。 */\n"
        "    @Override\n"
        "    public SoftLock lockItem(SharedSessionContractImplementor session, Object key, Object version) throws CacheException {\n"
        "        return null;\n"
        "    }",
    ),
    (
        "    @Override\n    public void unlockItem(SharedSessionContractImplementor session, Object key, SoftLock lock) throws CacheException {\n        evict(key);\n    }",
        "    /** 解锁时逐出键。 */\n"
        "    @Override\n"
        "    public void unlockItem(SharedSessionContractImplementor session, Object key, SoftLock lock) throws CacheException {\n"
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
        "    @Override\n    public boolean insert(SharedSessionContractImplementor session, Object key, Object value) throws CacheException {\n        return false;\n    }",
        "    /** 插入阶段不写缓存，延迟至 afterInsert。 */\n"
        "    @Override\n"
        "    public boolean insert(SharedSessionContractImplementor session, Object key, Object value) throws CacheException {\n"
        "        return false;\n"
        "    }",
    ),
    (
        "    @Override\n    public boolean afterInsert(SharedSessionContractImplementor session, Object key, Object value) throws CacheException {\n        region.put(session, key, value);\n        return true;\n    }",
        "    /** 插入完成后将自然 ID 映射写入缓存。 */\n"
        "    @Override\n"
        "    public boolean afterInsert(SharedSessionContractImplementor session, Object key, Object value) throws CacheException {\n"
        "        region.put(session, key, value);\n"
        "        return true;\n"
        "    }",
    ),
    (
        "    @Override\n    public boolean update(SharedSessionContractImplementor session, Object key, Object value) throws CacheException {\n        throw new UnsupportedOperationException(\"Unable to update read-only object\");\n    }",
        "    /** 只读自然 ID 禁止更新。 */\n"
        "    @Override\n"
        "    public boolean update(SharedSessionContractImplementor session, Object key, Object value) throws CacheException {\n"
        "        throw new UnsupportedOperationException(\"Unable to update read-only object\");\n"
        "    }",
    ),
    (
        "    @Override\n    public boolean afterUpdate(SharedSessionContractImplementor session, Object key, Object value, SoftLock lock) throws CacheException {\n        throw new UnsupportedOperationException(\"Unable to update read-only object\");\n    }",
        "    /** 只读自然 ID 禁止 afterUpdate。 */\n"
        "    @Override\n"
        "    public boolean afterUpdate(SharedSessionContractImplementor session, Object key, Object value, SoftLock lock) throws CacheException {\n"
        "        throw new UnsupportedOperationException(\"Unable to update read-only object\");\n"
        "    }",
    ),
    (
        "    @Override\n    public Object generateCacheKey(Object[] naturalIdValues, EntityPersister persister, SharedSessionContractImplementor session) {",
        "    /** 通过 {@link RedissonNaturalIdRegion} 的键工厂生成自然 ID 缓存键。 */\n"
        "    @Override\n"
        "    public Object generateCacheKey(Object[] naturalIdValues, EntityPersister persister, SharedSessionContractImplementor session) {",
    ),
    (
        "    @Override\n    public Object[] getNaturalIdValues(Object cacheKey) {",
        "    /** 从缓存键解析自然 ID 值数组。 */\n"
        "    @Override\n"
        "    public Object[] getNaturalIdValues(Object cacheKey) {",
    ),
]

W3A_REPLACEMENTS["ReadWriteCollectionRegionAccessStrategy.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 集合并发策略 {@code read-write} 的 Redisson 区域访问实现（Hibernate 5.2）。\n"
        " * <p>基于 {@link AbstractReadWriteAccessStrategy}，利用 {@link RMapCache} 提供读写一致性。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public ReadWriteCollectionRegionAccessStrategy(Settings settings, GeneralDataRegion region,\n            RMapCache<Object, Object> mapCache) {",
        "    /** @param settings Hibernate 缓存配置\n"
        "     * @param region 集合缓存区域\n"
        "     * @param mapCache 底层 Redisson 带 TTL 的 Map 缓存\n"
        "     */\n"
        "    public ReadWriteCollectionRegionAccessStrategy(Settings settings, GeneralDataRegion region,\n"
        "            RMapCache<Object, Object> mapCache) {",
    ),
    (
        "    @Override\n    public CollectionRegion getRegion() {",
        "    /** 返回强类型 {@link CollectionRegion} 视图。 */\n"
        "    @Override\n"
        "    public CollectionRegion getRegion() {",
    ),
    (
        "    @Override\n    public Object generateCacheKey(Object id, CollectionPersister persister, SessionFactoryImplementor factory, String tenantIdentifier) {",
        "    /** 通过 {@link RedissonCollectionRegion} 的键工厂生成集合缓存键。 */\n"
        "    @Override\n"
        "    public Object generateCacheKey(Object id, CollectionPersister persister, SessionFactoryImplementor factory, String tenantIdentifier) {",
    ),
    (
        "    @Override\n    public Object getCacheKeyId(Object cacheKey) {",
        "    /** 从缓存键反解集合标识符。 */\n"
        "    @Override\n"
        "    public Object getCacheKeyId(Object cacheKey) {",
    ),
]
