"""Chinese annotation replacements for Redisson 4.7.0 wave-2b [15:30]."""

W2B_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

W2B_REPLACEMENTS["ReadOnlyCollectionRegionAccessStrategy.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 集合并发策略 {@code read-only} 的 Redisson 区域访问实现（Hibernate 5）。\n"
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
        "    @Override\n    public Object get(SessionImplementor session, Object key, long txTimestamp) throws CacheException {",
        "    /** 在当前会话上下文中读取缓存条目。 */\n"
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
        "    /** 只读策略不提供项级软锁。 */\n"
        "    @Override\n"
        "    public SoftLock lockItem(SessionImplementor session, Object key, Object version) throws CacheException {\n"
        "        return null;\n"
        "    }",
    ),
    (
        "    @Override\n    public void unlockItem(SessionImplementor session, Object key, SoftLock lock) throws CacheException {\n    }",
        "    /** 只读解锁为空操作，不修改缓存。 */\n"
        "    @Override\n"
        "    public void unlockItem(SessionImplementor session, Object key, SoftLock lock) throws CacheException {\n"
        "    }",
    ),
]

W2B_REPLACEMENTS["ReadOnlyEntityRegionAccessStrategy.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 实体并发策略 {@code read-only} 的 Redisson 区域访问实现（Hibernate 5）。\n"
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
        "    @Override\n    public Object get(SessionImplementor session, Object key, long txTimestamp) throws CacheException {",
        "    /** 在当前会话上下文中读取缓存条目。 */\n"
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
        "    /** 只读策略不提供项级软锁。 */\n"
        "    @Override\n"
        "    public SoftLock lockItem(SessionImplementor session, Object key, Object version) throws CacheException {\n"
        "        return null;\n"
        "    }",
    ),
    (
        "    @Override\n    public void unlockItem(SessionImplementor session, Object key, SoftLock lock) throws CacheException {\n        evict(key);\n    }",
        "    /** 解锁时逐出键（与 Hibernate 只读语义一致）。 */\n"
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
        "    @Override\n    public boolean afterInsert(SessionImplementor session, Object key, Object value, Object version) throws CacheException {\n        region.put(session, key, value);\n        return true;\n    }",
        "    /** 插入完成后将实体写入缓存。 */\n"
        "    @Override\n"
        "    public boolean afterInsert(SessionImplementor session, Object key, Object value, Object version) throws CacheException {\n"
        "        region.put(session, key, value);\n"
        "        return true;\n"
        "    }",
    ),
    (
        "    @Override\n    public boolean update(SessionImplementor session, Object key, Object value, Object currentVersion, Object previousVersion)\n            throws CacheException {\n        throw new UnsupportedOperationException(\"Unable to update read-only object\");\n    }",
        "    /** 只读实体禁止更新。 */\n"
        "    @Override\n"
        "    public boolean update(SessionImplementor session, Object key, Object value, Object currentVersion, Object previousVersion)\n"
        "            throws CacheException {\n"
        "        throw new UnsupportedOperationException(\"Unable to update read-only object\");\n"
        "    }",
    ),
    (
        "    @Override\n    public boolean afterUpdate(SessionImplementor session, Object key, Object value, Object currentVersion, Object previousVersion, SoftLock lock)\n            throws CacheException {\n        throw new UnsupportedOperationException(\"Unable to update read-only object\");\n    }",
        "    /** 只读实体禁止 afterUpdate。 */\n"
        "    @Override\n"
        "    public boolean afterUpdate(SessionImplementor session, Object key, Object value, Object currentVersion, Object previousVersion, SoftLock lock)\n"
        "            throws CacheException {\n"
        "        throw new UnsupportedOperationException(\"Unable to update read-only object\");\n"
        "    }",
    ),
]

W2B_REPLACEMENTS["ReadOnlyNaturalIdRegionAccessStrategy.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 自然 ID 并发策略 {@code read-only} 的 Redisson 区域访问实现（Hibernate 5）。\n"
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
        "    @Override\n    public Object get(SessionImplementor session, Object key, long txTimestamp) throws CacheException {",
        "    /** 在当前会话上下文中读取缓存条目。 */\n"
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
        "    /** 只读策略不提供项级软锁。 */\n"
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
        "    /** 插入阶段不写缓存，延迟至 afterInsert。 */\n"
        "    @Override\n"
        "    public boolean insert(SessionImplementor session, Object key, Object value) throws CacheException {\n"
        "        return false;\n"
        "    }",
    ),
    (
        "    @Override\n    public boolean afterInsert(SessionImplementor session, Object key, Object value) throws CacheException {\n        region.put(session, key, value);\n        return true;\n    }",
        "    /** 插入完成后将自然 ID 映射写入缓存。 */\n"
        "    @Override\n"
        "    public boolean afterInsert(SessionImplementor session, Object key, Object value) throws CacheException {\n"
        "        region.put(session, key, value);\n"
        "        return true;\n"
        "    }",
    ),
    (
        "    @Override\n    public boolean update(SessionImplementor session, Object key, Object value) throws CacheException {\n        throw new UnsupportedOperationException(\"Unable to update read-only object\");\n    }",
        "    /** 只读自然 ID 禁止更新。 */\n"
        "    @Override\n"
        "    public boolean update(SessionImplementor session, Object key, Object value) throws CacheException {\n"
        "        throw new UnsupportedOperationException(\"Unable to update read-only object\");\n"
        "    }",
    ),
    (
        "    @Override\n    public boolean afterUpdate(SessionImplementor session, Object key, Object value, SoftLock lock) throws CacheException {\n        throw new UnsupportedOperationException(\"Unable to update read-only object\");\n    }",
        "    /** 只读自然 ID 禁止 afterUpdate。 */\n"
        "    @Override\n"
        "    public boolean afterUpdate(SessionImplementor session, Object key, Object value, SoftLock lock) throws CacheException {\n"
        "        throw new UnsupportedOperationException(\"Unable to update read-only object\");\n"
        "    }",
    ),
]

W2B_REPLACEMENTS["ReadWriteCollectionRegionAccessStrategy.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 集合并发策略 {@code read-write} 的 Redisson 区域访问实现（Hibernate 5）。\n"
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

W2B_REPLACEMENTS["ReadWriteEntityRegionAccessStrategy.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 实体并发策略 {@code read-write} 的 Redisson 区域访问实现（Hibernate 5）。\n"
        " * <p>插入/更新完成后写回缓存；更新阶段不立即写入。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public ReadWriteEntityRegionAccessStrategy(Settings settings, GeneralDataRegion region, RMapCache<Object, Object> mapCache) {",
        "    /** @param settings Hibernate 缓存配置\n"
        "     * @param region 实体缓存区域\n"
        "     * @param mapCache 底层 Redisson 带 TTL 的 Map 缓存\n"
        "     */\n"
        "    public ReadWriteEntityRegionAccessStrategy(Settings settings, GeneralDataRegion region, RMapCache<Object, Object> mapCache) {",
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
        "    @Override\n    public boolean afterInsert(SessionImplementor session, Object key, Object value, Object version) throws CacheException {\n        region.put(session, key, value);\n        return true;\n    }",
        "    /** 插入完成后将实体写入缓存。 */\n"
        "    @Override\n"
        "    public boolean afterInsert(SessionImplementor session, Object key, Object value, Object version) throws CacheException {\n"
        "        region.put(session, key, value);\n"
        "        return true;\n"
        "    }",
    ),
    (
        "    @Override\n    public boolean update(SessionImplementor session, Object key, Object value, Object currentVersion, Object previousVersion)\n            throws CacheException {\n        return false;\n    }",
        "    /** 更新阶段不立即写入，等待 afterUpdate。 */\n"
        "    @Override\n"
        "    public boolean update(SessionImplementor session, Object key, Object value, Object currentVersion, Object previousVersion)\n"
        "            throws CacheException {\n"
        "        return false;\n"
        "    }",
    ),
    (
        "    @Override\n    public boolean afterUpdate(SessionImplementor session, Object key, Object value, Object currentVersion, Object previousVersion, SoftLock lock)\n            throws CacheException {\n        region.put(session, key, value);\n        return true;\n    }",
        "    /** 更新完成后将新实体版本写入缓存。 */\n"
        "    @Override\n"
        "    public boolean afterUpdate(SessionImplementor session, Object key, Object value, Object currentVersion, Object previousVersion, SoftLock lock)\n"
        "            throws CacheException {\n"
        "        region.put(session, key, value);\n"
        "        return true;\n"
        "    }",
    ),
]

W2B_REPLACEMENTS["ReadWriteNaturalIdRegionAccessStrategy.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 自然 ID 并发策略 {@code read-write} 的 Redisson 区域访问实现（Hibernate 5）。\n"
        " * <p>插入/更新完成后写回缓存；更新阶段不立即写入。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public ReadWriteNaturalIdRegionAccessStrategy(Settings settings, GeneralDataRegion region,\n            RMapCache<Object, Object> mapCache) {",
        "    /** @param settings Hibernate 缓存配置\n"
        "     * @param region 自然 ID 缓存区域\n"
        "     * @param mapCache 底层 Redisson 带 TTL 的 Map 缓存\n"
        "     */\n"
        "    public ReadWriteNaturalIdRegionAccessStrategy(Settings settings, GeneralDataRegion region,\n"
        "            RMapCache<Object, Object> mapCache) {",
    ),
    (
        "    @Override\n    public NaturalIdRegion getRegion() {",
        "    /** 返回强类型 {@link NaturalIdRegion} 视图。 */\n"
        "    @Override\n"
        "    public NaturalIdRegion getRegion() {",
    ),
    (
        "    @Override\n    public boolean insert(SessionImplementor session, Object key, Object value) throws CacheException {\n        return false;\n    }",
        "    /** 插入阶段不写缓存，延迟至 afterInsert。 */\n"
        "    @Override\n"
        "    public boolean insert(SessionImplementor session, Object key, Object value) throws CacheException {\n"
        "        return false;\n"
        "    }",
    ),
    (
        "    @Override\n    public boolean afterInsert(SessionImplementor session, Object key, Object value) throws CacheException {\n        region.put(session, key, value);\n        return true;\n    }",
        "    /** 插入完成后将自然 ID 映射写入缓存。 */\n"
        "    @Override\n"
        "    public boolean afterInsert(SessionImplementor session, Object key, Object value) throws CacheException {\n"
        "        region.put(session, key, value);\n"
        "        return true;\n"
        "    }",
    ),
    (
        "    @Override\n    public boolean update(SessionImplementor session, Object key, Object value) throws CacheException {\n        return false;\n    }",
        "    /** 更新阶段不立即写入，等待 afterUpdate。 */\n"
        "    @Override\n"
        "    public boolean update(SessionImplementor session, Object key, Object value) throws CacheException {\n"
        "        return false;\n"
        "    }",
    ),
    (
        "    @Override\n    public boolean afterUpdate(SessionImplementor session, Object key, Object value, SoftLock lock) throws CacheException {\n        region.put(session, key, value);\n        return true;\n    }",
        "    /** 更新完成后将新映射写入缓存。 */\n"
        "    @Override\n"
        "    public boolean afterUpdate(SessionImplementor session, Object key, Object value, SoftLock lock) throws CacheException {\n"
        "        region.put(session, key, value);\n"
        "        return true;\n"
        "    }",
    ),
]

W2B_REPLACEMENTS["TransactionalCollectionRegionAccessStrategy.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 集合并发策略 {@code transactional} 的 Redisson 区域访问实现（Hibernate 5）。\n"
        " * <p>缓存写入与 JTA 事务边界对齐；解锁为空操作。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public TransactionalCollectionRegionAccessStrategy(Settings settings, GeneralDataRegion region) {",
        "    /** @param settings Hibernate 缓存配置\n     * @param region 集合缓存区域\n     */\n"
        "    public TransactionalCollectionRegionAccessStrategy(Settings settings, GeneralDataRegion region) {",
    ),
    (
        "    @Override\n    public CollectionRegion getRegion() {",
        "    /** 返回强类型 {@link CollectionRegion} 视图。 */\n"
        "    @Override\n"
        "    public CollectionRegion getRegion() {",
    ),
    (
        "    @Override\n    public Object get(SessionImplementor session, Object key, long txTimestamp) throws CacheException {",
        "    /** 在当前会话上下文中读取缓存条目。 */\n"
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
        "    /** 事务策略不提供项级软锁。 */\n"
        "    @Override\n"
        "    public SoftLock lockItem(SessionImplementor session, Object key, Object version) throws CacheException {\n"
        "        return null;\n"
        "    }",
    ),
    (
        "    @Override\n    public void unlockItem(SessionImplementor session, Object key, SoftLock lock) throws CacheException {\n    }",
        "    /** 解锁为空操作，事务回滚由 Hibernate 协调。 */\n"
        "    @Override\n"
        "    public void unlockItem(SessionImplementor session, Object key, SoftLock lock) throws CacheException {\n"
        "    }",
    ),
    (
        "    @Override\n    public void remove(SessionImplementor session, Object key) throws CacheException {\n        region.evict(key);\n    }",
        "    /** 移除指定键对应的缓存条目。 */\n"
        "    @Override\n"
        "    public void remove(SessionImplementor session, Object key) throws CacheException {\n"
        "        region.evict(key);\n"
        "    }",
    ),
]

W2B_REPLACEMENTS["TransactionalEntityRegionAccessStrategy.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 实体并发策略 {@code transactional} 的 Redisson 区域访问实现（Hibernate 5）。\n"
        " * <p>插入/更新在事务阶段立即写入缓存，after 阶段返回 {@code false}。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public TransactionalEntityRegionAccessStrategy(Settings settings, GeneralDataRegion region) {",
        "    /** @param settings Hibernate 缓存配置\n     * @param region 实体缓存区域\n     */\n"
        "    public TransactionalEntityRegionAccessStrategy(Settings settings, GeneralDataRegion region) {",
    ),
    (
        "    @Override\n    public Object get(SessionImplementor session, Object key, long txTimestamp) throws CacheException {",
        "    /** 在当前会话上下文中读取缓存条目。 */\n"
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
        "    /** 事务策略不提供项级软锁。 */\n"
        "    @Override\n"
        "    public SoftLock lockItem(SessionImplementor session, Object key, Object version) throws CacheException {\n"
        "        return null;\n"
        "    }",
    ),
    (
        "    @Override\n    public void unlockItem(SessionImplementor session, Object key, SoftLock lock) throws CacheException {\n    }",
        "    /** 解锁为空操作。 */\n"
        "    @Override\n"
        "    public void unlockItem(SessionImplementor session, Object key, SoftLock lock) throws CacheException {\n"
        "    }",
    ),
    (
        "    @Override\n    public EntityRegion getRegion() {",
        "    /** 返回强类型 {@link EntityRegion} 视图。 */\n"
        "    @Override\n"
        "    public EntityRegion getRegion() {",
    ),
    (
        "    @Override\n    public boolean insert(SessionImplementor session, Object key, Object value, Object version) throws CacheException {\n        region.put(session, key, value);\n        return true;\n    }",
        "    /** 插入阶段立即写入缓存。 */\n"
        "    @Override\n"
        "    public boolean insert(SessionImplementor session, Object key, Object value, Object version) throws CacheException {\n"
        "        region.put(session, key, value);\n"
        "        return true;\n"
        "    }",
    ),
    (
        "    @Override\n    public boolean afterInsert(SessionImplementor session, Object key, Object value, Object version) throws CacheException {\n        return false;\n    }",
        "    /** 插入后阶段不再重复写入。 */\n"
        "    @Override\n"
        "    public boolean afterInsert(SessionImplementor session, Object key, Object value, Object version) throws CacheException {\n"
        "        return false;\n"
        "    }",
    ),
    (
        "    @Override\n    public void remove(SessionImplementor session, Object key) throws CacheException {\n        region.evict(key);\n    }",
        "    /** 移除指定键对应的缓存条目。 */\n"
        "    @Override\n"
        "    public void remove(SessionImplementor session, Object key) throws CacheException {\n"
        "        region.evict(key);\n"
        "    }",
    ),
    (
        "    @Override\n    public boolean update(SessionImplementor session, Object key, Object value, Object currentVersion, Object previousVersion)\n            throws CacheException {\n        return insert(session, key, value, currentVersion);\n    }",
        "    /** 更新阶段复用 insert 逻辑立即写入。 */\n"
        "    @Override\n"
        "    public boolean update(SessionImplementor session, Object key, Object value, Object currentVersion, Object previousVersion)\n"
        "            throws CacheException {\n"
        "        return insert(session, key, value, currentVersion);\n"
        "    }",
    ),
    (
        "    @Override\n    public boolean afterUpdate(SessionImplementor session, Object key, Object value, Object currentVersion, Object previousVersion, SoftLock lock)\n            throws CacheException {\n        return false;\n    }",
        "    /** 更新后阶段不再重复写入。 */\n"
        "    @Override\n"
        "    public boolean afterUpdate(SessionImplementor session, Object key, Object value, Object currentVersion, Object previousVersion, SoftLock lock)\n"
        "            throws CacheException {\n"
        "        return false;\n"
        "    }",
    ),
]

W2B_REPLACEMENTS["TransactionalNaturalIdRegionAccessStrategy.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 自然 ID 并发策略 {@code transactional} 的 Redisson 区域访问实现（Hibernate 5）。\n"
        " * <p>插入/更新在事务阶段立即写入缓存，after 阶段返回 {@code false}。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public TransactionalNaturalIdRegionAccessStrategy(Settings settings, GeneralDataRegion region) {",
        "    /** @param settings Hibernate 缓存配置\n     * @param region 自然 ID 缓存区域\n     */\n"
        "    public TransactionalNaturalIdRegionAccessStrategy(Settings settings, GeneralDataRegion region) {",
    ),
    (
        "    @Override\n    public Object get(SessionImplementor session, Object key, long txTimestamp) throws CacheException {",
        "    /** 在当前会话上下文中读取缓存条目。 */\n"
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
        "    /** 事务策略不提供项级软锁。 */\n"
        "    @Override\n"
        "    public SoftLock lockItem(SessionImplementor session, Object key, Object version) throws CacheException {\n"
        "        return null;\n"
        "    }",
    ),
    (
        "    @Override\n    public void unlockItem(SessionImplementor session, Object key, SoftLock lock) throws CacheException {\n    }",
        "    /** 解锁为空操作。 */\n"
        "    @Override\n"
        "    public void unlockItem(SessionImplementor session, Object key, SoftLock lock) throws CacheException {\n"
        "    }",
    ),
    (
        "    @Override\n    public NaturalIdRegion getRegion() {",
        "    /** 返回强类型 {@link NaturalIdRegion} 视图。 */\n"
        "    @Override\n"
        "    public NaturalIdRegion getRegion() {",
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
        "    @Override\n    public boolean insert(SessionImplementor session, Object key, Object value) throws CacheException {\n        region.put(session, key, value);\n        return true;\n    }",
        "    /** 插入阶段立即写入缓存。 */\n"
        "    @Override\n"
        "    public boolean insert(SessionImplementor session, Object key, Object value) throws CacheException {\n"
        "        region.put(session, key, value);\n"
        "        return true;\n"
        "    }",
    ),
    (
        "    @Override\n    public boolean afterInsert(SessionImplementor session, Object key, Object value) throws CacheException {\n        return false;\n    }",
        "    /** 插入后阶段不再重复写入。 */\n"
        "    @Override\n"
        "    public boolean afterInsert(SessionImplementor session, Object key, Object value) throws CacheException {\n"
        "        return false;\n"
        "    }",
    ),
    (
        "    @Override\n    public boolean update(SessionImplementor session, Object key, Object value) throws CacheException {\n        return insert(session, key, value);\n    }",
        "    /** 更新阶段复用 insert 逻辑立即写入。 */\n"
        "    @Override\n"
        "    public boolean update(SessionImplementor session, Object key, Object value) throws CacheException {\n"
        "        return insert(session, key, value);\n"
        "    }",
    ),
    (
        "    @Override\n    public boolean afterUpdate(SessionImplementor session, Object key, Object value, SoftLock lock) throws CacheException {\n        return false;\n    }",
        "    /** 更新后阶段不再重复写入。 */\n"
        "    @Override\n"
        "    public boolean afterUpdate(SessionImplementor session, Object key, Object value, SoftLock lock) throws CacheException {\n"
        "        return false;\n"
        "    }",
    ),
]

W2B_REPLACEMENTS["JndiRedissonRegionFactory.java"] = [
    (
        "/**\n * Hibernate Cache region factory based on Redisson. \n * Uses Redisson instance located in JNDI.\n * \n * @author Nikita Koksharov \n *\n */",
        "/**\n"
        " * 基于 Redisson 的 Hibernate 缓存区域工厂（Hibernate 5.2）。\n"
        " * <p>从 JNDI 查找已部署的 {@link RedissonClient}，而非自行创建实例。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public static final String JNDI_NAME = CONFIG_PREFIX + \"jndi_name\";",
        "    /** JNDI 查找 Redisson 客户端所用的配置键（{@code hibernate.cache.redisson.jndi_name}）。 */\n"
        "    public static final String JNDI_NAME = CONFIG_PREFIX + \"jndi_name\";",
    ),
    (
        "    @Override\n    protected RedissonClient createRedissonClient(Properties properties) {",
        "    /** 从 JNDI 按配置名查找 {@link RedissonClient}。\n"
        "     *\n"
        "     * @param properties Hibernate 缓存属性\n"
        "     * @return 已绑定的 Redisson 客户端\n"
        "     * @throws CacheException JNDI 名未配置或查找/关闭上下文失败\n"
        "     */\n"
        "    @Override\n"
        "    protected RedissonClient createRedissonClient(Properties properties) {",
    ),
    (
        "        if (jndiName == null) {\n            throw new CacheException(JNDI_NAME + \" property not set\");\n        }",
        "        // 未配置 JNDI 名则无法查找客户端。\n"
        "        if (jndiName == null) {\n"
        "            throw new CacheException(JNDI_NAME + \" property not set\");\n"
        "        }",
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
    (
        "    @Override\n    public void stop() {\n    }",
        "    /** JNDI 模式下不销毁外部管理的 Redisson 实例。 */\n"
        "    @Override\n"
        "    public void stop() {\n"
        "    }",
    ),
]

W2B_REPLACEMENTS["JndiRedissonRegionNativeFactory.java"] = [
    (
        "/**\n * Hibernate Cache region factory based on Redisson. \n * Uses Redisson instance located in JNDI.\n * \n * @author Nikita Koksharov \n *\n */",
        "/**\n"
        " * 基于 Redisson Native 编解码的 Hibernate 缓存区域工厂（Hibernate 5.2）。\n"
        " * <p>从 JNDI 查找已部署的 {@link RedissonClient}，配合 {@link RedissonRegionNativeFactory} 使用。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public static final String JNDI_NAME = CONFIG_PREFIX + \"jndi_name\";",
        "    /** JNDI 查找 Redisson 客户端所用的配置键（{@code hibernate.cache.redisson.jndi_name}）。 */\n"
        "    public static final String JNDI_NAME = CONFIG_PREFIX + \"jndi_name\";",
    ),
    (
        "    @Override\n    protected RedissonClient createRedissonClient(Properties properties) {",
        "    /** 从 JNDI 按配置名查找 {@link RedissonClient}。\n"
        "     *\n"
        "     * @param properties Hibernate 缓存属性\n"
        "     * @return 已绑定的 Redisson 客户端\n"
        "     * @throws CacheException JNDI 名未配置或查找/关闭上下文失败\n"
        "     */\n"
        "    @Override\n"
        "    protected RedissonClient createRedissonClient(Properties properties) {",
    ),
    (
        "        if (jndiName == null) {\n            throw new CacheException(JNDI_NAME + \" property not set\");\n        }",
        "        // 未配置 JNDI 名则无法查找客户端。\n"
        "        if (jndiName == null) {\n"
        "            throw new CacheException(JNDI_NAME + \" property not set\");\n"
        "        }",
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
    (
        "    @Override\n    public void stop() {\n    }",
        "    /** JNDI 模式下不销毁外部管理的 Redisson 实例。 */\n"
        "    @Override\n"
        "    public void stop() {\n"
        "    }",
    ),
]

W2B_REPLACEMENTS["RedissonCacheKeysFactory.java"] = [
    (
        "/**\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Redisson 专用的 Hibernate 缓存键工厂。\n"
        " * <p>生成集合缓存键前临时清空关联字段，使 Redisson 编解码器与 Hibernate 默认键格式对齐。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    private final Codec codec;",
        "    /** 用于序列化/反序列化 Map 键的 Redisson 编解码器。 */\n"
        "    private final Codec codec;",
    ),
    (
        "    public RedissonCacheKeysFactory(Codec codec) {",
        "    /** @param codec Redisson 编解码器，用于键的编码与解码\n     */\n"
        "    public RedissonCacheKeysFactory(Codec codec) {",
    ),
    (
        "    @Override\n    public Object createCollectionKey(Object id, CollectionPersister persister, SessionFactoryImplementor factory, String tenantIdentifier) {",
        "    /** 生成集合缓存键：先按编解码器规范化 id，再委托父类默认实现。\n"
        "     * 若反射找不到关联字段则回退至默认键生成。\n"
        "     */\n"
        "    @Override\n"
        "    public Object createCollectionKey(Object id, CollectionPersister persister, SessionFactoryImplementor factory, String tenantIdentifier) {",
    ),
    (
        "            Object prev = f.get(id);\n            f.set(id, null);",
        "            // 临时清空关联引用，避免编解码器序列化多余字段。\n"
        "            Object prev = f.get(id);\n"
        "            f.set(id, null);",
    ),
    (
        "            return super.createCollectionKey(newId, persister, factory, tenantIdentifier);",
        "            // 使用规范化后的 id 调用 Hibernate 默认键工厂。\n"
        "            return super.createCollectionKey(newId, persister, factory, tenantIdentifier);",
    ),
]

W2B_REPLACEMENTS["RedissonRegionFactory.java"] = [
    (
        "/**\n * Hibernate Cache region factory based on Redisson. \n * Creates own Redisson instance during region start.\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 基于 Redisson 的 Hibernate 二级缓存 {@link RegionFactory} 实现（Hibernate 5.2）。\n"
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
        "    @Override\n    public void start(SessionFactoryOptions settings, Properties properties) throws CacheException {",
        "    /** 加载 Redisson 配置、初始化客户端并解析 fallback 与缓存键工厂。 */\n"
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
        "    /** 关闭 Redisson 客户端并释放连接。 */\n    @Override\n    public void stop() {",
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
        "    /** 通过 Redis Lua 脚本生成全局递增时间戳；失败且启用 fallback 时使用本地 CAS 递增。 */\n"
        "    @Override\n    public long nextTimestamp() {",
    ),
    (
        "    @Override\n    public EntityRegion buildEntityRegion(String regionName, Properties properties, CacheDataDescription metadata)",
        "    /** 构建实体二级缓存 Region。 */\n"
        "    @Override\n"
        "    public EntityRegion buildEntityRegion(String regionName, Properties properties, CacheDataDescription metadata)",
    ),
    (
        "    protected RMapCache<Object, Object> getCache(String regionName, Properties properties, String defaultKey) {",
        "    /** 获取指定 Region 名称对应的 {@link RMapCache} 实例。 */\n"
        "    protected RMapCache<Object, Object> getCache(String regionName, Properties properties, String defaultKey) {",
    ),
]

W2B_REPLACEMENTS["RedissonRegionNativeFactory.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 使用 Redisson 原生 Map 缓存（{@link RMapCacheNative}）的 Region 工厂（Hibernate 5.2）。\n"
        " * <p>启动前校验 eviction 与 max_idle 配置必须为 0。\n"
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
        "    @Override\n    protected RMapCache<Object, Object> getCache(String regionName, Properties properties, String defaultKey) {",
        "    /** 返回包装后的 {@link RMapCacheNative} 实例作为 Region 底层存储。 */\n"
        "    @Override\n"
        "    protected RMapCache<Object, Object> getCache(String regionName, Properties properties, String defaultKey) {",
    ),
]

W2B_REPLACEMENTS["RedissonStrategyRegistrationProvider.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Hibernate 启动时注册 Redisson {@link RegionFactory} 策略的 SPI 提供者（Hibernate 5.2）。\n"
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
