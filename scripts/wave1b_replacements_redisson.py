"""Chinese annotation replacements for Redisson 4.7.0 wave-1b [15:30]."""

W1B_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

W1B_REPLACEMENTS["BaseRegionAccessStrategy.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Hibernate 缓存区域访问策略的抽象基类。\n"
        " * <p>封装 {@link GeneralDataRegion} 与 {@link Settings}，提供区域锁、逐出\n"
        " * 以及带最小写入开关的 {@link #putFromLoad} 等默认实现。\n"
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
        "    @Override\n    public boolean putFromLoad(Object key, Object value, long txTimestamp, Object version) throws CacheException {\n        return putFromLoad( key, value, txTimestamp, version, settings.isMinimalPutsEnabled() );\n    }",
        "    /** 按全局最小写入开关委托至五参数 {@link #putFromLoad}。 */\n"
        "    @Override\n"
        "    public boolean putFromLoad(Object key, Object value, long txTimestamp, Object version) throws CacheException {\n"
        "        return putFromLoad( key, value, txTimestamp, version, settings.isMinimalPutsEnabled() );\n"
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
        "    @Override\n    public void remove(Object key) throws CacheException {\n    }",
        "    /** 移除键：基类为空实现，由子类按需覆盖。 */\n"
        "    @Override\n"
        "    public void remove(Object key) throws CacheException {\n"
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

W1B_REPLACEMENTS["NonStrictReadWriteCollectionRegionAccessStrategy.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 集合并发策略 {@code nonstrict-read-write} 的 Redisson 区域访问实现。\n"
        " * <p>不保证读写一致性，允许脏读；项级锁为空操作，解锁或移除时逐出键。\n"
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
        "    @Override\n    public Object get(Object key, long txTimestamp) throws CacheException {",
        "    /** 直接读取缓存，不校验事务时间戳。 */\n"
        "    @Override\n"
        "    public Object get(Object key, long txTimestamp) throws CacheException {",
    ),
    (
        "        if (minimalPutOverride && region.contains(key)) {\n            return false;\n        }",
        "        // 最小写入模式下键已存在则跳过写入。\n"
        "        if (minimalPutOverride && region.contains(key)) {\n"
        "            return false;\n        }",
    ),
    (
        "    @Override\n    public SoftLock lockItem(Object key, Object version) throws CacheException {\n        return null;\n    }",
        "    /** 非严格读写不提供项级软锁。 */\n"
        "    @Override\n"
        "    public SoftLock lockItem(Object key, Object version) throws CacheException {\n"
        "        return null;\n"
        "    }",
    ),
    (
        "    @Override\n    public void unlockItem(Object key, SoftLock lock) throws CacheException {\n        evict(key);\n    }",
        "    /** 解锁时逐出键，使后续读取重新加载。 */\n"
        "    @Override\n"
        "    public void unlockItem(Object key, SoftLock lock) throws CacheException {\n"
        "        evict(key);\n"
        "    }",
    ),
    (
        "    @Override\n    public void remove(Object key) throws CacheException {\n        evict(key);\n    }",
        "    /** 移除键等同于逐出缓存条目。 */\n"
        "    @Override\n"
        "    public void remove(Object key) throws CacheException {\n"
        "        evict(key);\n"
        "    }",
    ),
]

W1B_REPLACEMENTS["NonStrictReadWriteEntityRegionAccessStrategy.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 实体并发策略 {@code nonstrict-read-write} 的 Redisson 区域访问实现。\n"
        " * <p>允许脏读；更新时先移除键，插入/更新后不主动写回缓存。\n"
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
        "    @Override\n    public Object get(Object key, long txTimestamp) throws CacheException {",
        "    /** 直接读取缓存，不校验事务时间戳。 */\n"
        "    @Override\n"
        "    public Object get(Object key, long txTimestamp) throws CacheException {",
    ),
    (
        "        if (minimalPutOverride && region.contains(key)) {\n            return false;\n        }",
        "        // 最小写入模式下键已存在则跳过写入。\n"
        "        if (minimalPutOverride && region.contains(key)) {\n"
        "            return false;\n        }",
    ),
    (
        "    @Override\n    public SoftLock lockItem(Object key, Object version) throws CacheException {\n        return null;\n    }",
        "    /** 非严格读写不提供项级软锁。 */\n"
        "    @Override\n"
        "    public SoftLock lockItem(Object key, Object version) throws CacheException {\n"
        "        return null;\n"
        "    }",
    ),
    (
        "    @Override\n    public void unlockItem(Object key, SoftLock lock) throws CacheException {\n        evict(key);\n    }",
        "    /** 解锁时逐出键。 */\n"
        "    @Override\n"
        "    public void unlockItem(Object key, SoftLock lock) throws CacheException {\n"
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
        "    @Override\n    public boolean insert(Object key, Object value, Object version) throws CacheException {\n        return false;\n    }",
        "    /** 插入阶段不写缓存，延迟至 afterInsert。 */\n"
        "    @Override\n"
        "    public boolean insert(Object key, Object value, Object version) throws CacheException {\n"
        "        return false;\n"
        "    }",
    ),
    (
        "    @Override\n    public boolean afterInsert(Object key, Object value, Object version) throws CacheException {\n        return false;\n    }",
        "    /** 插入完成后也不写回缓存。 */\n"
        "    @Override\n"
        "    public boolean afterInsert(Object key, Object value, Object version) throws CacheException {\n"
        "        return false;\n"
        "    }",
    ),
    (
        "    @Override\n    public boolean update(Object key, Object value, Object currentVersion, Object previousVersion)\n            throws CacheException {\n        remove(key);\n        return false;\n    }",
        "    /** 更新时移除旧缓存条目，不立即写入新值。 */\n"
        "    @Override\n"
        "    public boolean update(Object key, Object value, Object currentVersion, Object previousVersion)\n"
        "            throws CacheException {\n"
        "        remove(key);\n"
        "        return false;\n"
        "    }",
    ),
    (
        "    @Override\n    public boolean afterUpdate(Object key, Object value, Object currentVersion, Object previousVersion, SoftLock lock)\n            throws CacheException {\n        unlockItem(key, lock);\n        return false;\n    }",
        "    /** 更新完成后解锁并逐出，不写回新值。 */\n"
        "    @Override\n"
        "    public boolean afterUpdate(Object key, Object value, Object currentVersion, Object previousVersion, SoftLock lock)\n"
        "            throws CacheException {\n"
        "        unlockItem(key, lock);\n"
        "        return false;\n"
        "    }",
    ),
    (
        "    @Override\n    public void remove(Object key) throws CacheException {\n        evict(key);\n    }",
        "    /** 移除键等同于逐出缓存条目。 */\n"
        "    @Override\n"
        "    public void remove(Object key) throws CacheException {\n"
        "        evict(key);\n"
        "    }",
    ),
]

W1B_REPLACEMENTS["NonStrictReadWriteNaturalIdRegionAccessStrategy.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 自然 ID 并发策略 {@code nonstrict-read-write} 的 Redisson 区域访问实现。\n"
        " * <p>允许脏读；更新时移除键，插入/更新后不主动写回缓存。\n"
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
        "    @Override\n    public Object get(Object key, long txTimestamp) throws CacheException {",
        "    /** 直接读取缓存，不校验事务时间戳。 */\n"
        "    @Override\n"
        "    public Object get(Object key, long txTimestamp) throws CacheException {",
    ),
    (
        "        if (minimalPutOverride && region.contains(key)) {\n            return false;\n        }",
        "        // 最小写入模式下键已存在则跳过写入。\n"
        "        if (minimalPutOverride && region.contains(key)) {\n"
        "            return false;\n        }",
    ),
    (
        "    @Override\n    public SoftLock lockItem(Object key, Object version) throws CacheException {\n        return null;\n    }",
        "    /** 非严格读写不提供项级软锁。 */\n"
        "    @Override\n"
        "    public SoftLock lockItem(Object key, Object version) throws CacheException {\n"
        "        return null;\n"
        "    }",
    ),
    (
        "    @Override\n    public void unlockItem(Object key, SoftLock lock) throws CacheException {\n        evict(key);\n    }",
        "    /** 解锁时逐出键。 */\n"
        "    @Override\n"
        "    public void unlockItem(Object key, SoftLock lock) throws CacheException {\n"
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
        "    @Override\n    public boolean insert(Object key, Object value) throws CacheException {\n        return false;\n    }",
        "    /** 插入阶段不写缓存。 */\n"
        "    @Override\n"
        "    public boolean insert(Object key, Object value) throws CacheException {\n"
        "        return false;\n"
        "    }",
    ),
    (
        "    @Override\n    public boolean afterInsert(Object key, Object value) throws CacheException {\n        return false;\n    }",
        "    /** 插入完成后也不写回缓存。 */\n"
        "    @Override\n"
        "    public boolean afterInsert(Object key, Object value) throws CacheException {\n"
        "        return false;\n"
        "    }",
    ),
    (
        "    @Override\n    public boolean update(Object key, Object value) throws CacheException {\n        remove(key);\n        return false;\n    }",
        "    /** 更新时移除旧缓存条目。 */\n"
        "    @Override\n"
        "    public boolean update(Object key, Object value) throws CacheException {\n"
        "        remove(key);\n"
        "        return false;\n"
        "    }",
    ),
    (
        "    @Override\n    public boolean afterUpdate(Object key, Object value, SoftLock lock) throws CacheException {\n        unlockItem(key, lock);\n        return false;\n    }",
        "    /** 更新完成后解锁并逐出。 */\n"
        "    @Override\n"
        "    public boolean afterUpdate(Object key, Object value, SoftLock lock) throws CacheException {\n"
        "        unlockItem(key, lock);\n"
        "        return false;\n"
        "    }",
    ),
    (
        "    @Override\n    public void remove(Object key) throws CacheException {\n        region.evict(key);\n    }",
        "    /** 移除指定自然 ID 对应的缓存条目。 */\n"
        "    @Override\n"
        "    public void remove(Object key) throws CacheException {\n"
        "        region.evict(key);\n"
        "    }",
    ),
]

W1B_REPLACEMENTS["ReadOnlyCollectionRegionAccessStrategy.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 集合并发策略 {@code read-only} 的 Redisson 区域访问实现。\n"
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
        "    @Override\n    public Object get(Object key, long txTimestamp) throws CacheException {",
        "    /** 直接读取缓存条目。 */\n"
        "    @Override\n"
        "    public Object get(Object key, long txTimestamp) throws CacheException {",
    ),
    (
        "        if (minimalPutOverride && region.contains(key)) {\n            return false;\n        }",
        "        // 最小写入模式下键已存在则跳过写入。\n"
        "        if (minimalPutOverride && region.contains(key)) {\n"
        "            return false;\n        }",
    ),
    (
        "    @Override\n    public SoftLock lockItem(Object key, Object version) throws CacheException {\n        return null;\n    }",
        "    /** 只读策略不提供项级软锁。 */\n"
        "    @Override\n"
        "    public SoftLock lockItem(Object key, Object version) throws CacheException {\n"
        "        return null;\n"
        "    }",
    ),
    (
        "    @Override\n    public void unlockItem(Object key, SoftLock lock) throws CacheException {\n    }",
        "    /** 只读解锁为空操作，不修改缓存。 */\n"
        "    @Override\n"
        "    public void unlockItem(Object key, SoftLock lock) throws CacheException {\n"
        "    }",
    ),
]

W1B_REPLACEMENTS["ReadOnlyEntityRegionAccessStrategy.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 实体并发策略 {@code read-only} 的 Redisson 区域访问实现。\n"
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
        "    @Override\n    public Object get(Object key, long txTimestamp) throws CacheException {",
        "    /** 直接读取缓存条目。 */\n"
        "    @Override\n"
        "    public Object get(Object key, long txTimestamp) throws CacheException {",
    ),
    (
        "        if (minimalPutOverride && region.contains(key)) {\n            return false;\n        }",
        "        // 最小写入模式下键已存在则跳过写入。\n"
        "        if (minimalPutOverride && region.contains(key)) {\n"
        "            return false;\n        }",
    ),
    (
        "    @Override\n    public SoftLock lockItem(Object key, Object version) throws CacheException {\n        return null;\n    }",
        "    /** 只读策略不提供项级软锁。 */\n"
        "    @Override\n"
        "    public SoftLock lockItem(Object key, Object version) throws CacheException {\n"
        "        return null;\n"
        "    }",
    ),
    (
        "    @Override\n    public void unlockItem(Object key, SoftLock lock) throws CacheException {\n        evict(key);\n    }",
        "    /** 解锁时逐出键（与 Hibernate 只读语义一致）。 */\n"
        "    @Override\n"
        "    public void unlockItem(Object key, SoftLock lock) throws CacheException {\n"
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
        "    @Override\n    public boolean insert(Object key, Object value, Object version) throws CacheException {\n        return false;\n    }",
        "    /** 插入阶段不写缓存，延迟至 afterInsert。 */\n"
        "    @Override\n"
        "    public boolean insert(Object key, Object value, Object version) throws CacheException {\n"
        "        return false;\n"
        "    }",
    ),
    (
        "    @Override\n    public boolean afterInsert(Object key, Object value, Object version) throws CacheException {\n        region.put(key, value);\n        return true;\n    }",
        "    /** 插入完成后将实体写入缓存。 */\n"
        "    @Override\n"
        "    public boolean afterInsert(Object key, Object value, Object version) throws CacheException {\n"
        "        region.put(key, value);\n"
        "        return true;\n"
        "    }",
    ),
    (
        "    @Override\n    public boolean update(Object key, Object value, Object currentVersion, Object previousVersion)\n            throws CacheException {\n        throw new UnsupportedOperationException(\"Unable to update read-only object\");\n    }",
        "    /** 只读实体禁止更新。 */\n"
        "    @Override\n"
        "    public boolean update(Object key, Object value, Object currentVersion, Object previousVersion)\n"
        "            throws CacheException {\n"
        "        throw new UnsupportedOperationException(\"Unable to update read-only object\");\n"
        "    }",
    ),
    (
        "    @Override\n    public boolean afterUpdate(Object key, Object value, Object currentVersion, Object previousVersion, SoftLock lock)\n            throws CacheException {\n        throw new UnsupportedOperationException(\"Unable to update read-only object\");\n    }",
        "    /** 只读实体禁止 afterUpdate。 */\n"
        "    @Override\n"
        "    public boolean afterUpdate(Object key, Object value, Object currentVersion, Object previousVersion, SoftLock lock)\n"
        "            throws CacheException {\n"
        "        throw new UnsupportedOperationException(\"Unable to update read-only object\");\n"
        "    }",
    ),
]

W1B_REPLACEMENTS["ReadOnlyNaturalIdRegionAccessStrategy.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 自然 ID 并发策略 {@code read-only} 的 Redisson 区域访问实现。\n"
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
        "    @Override\n    public Object get(Object key, long txTimestamp) throws CacheException {",
        "    /** 直接读取缓存条目。 */\n"
        "    @Override\n"
        "    public Object get(Object key, long txTimestamp) throws CacheException {",
    ),
    (
        "        if (minimalPutOverride && region.contains(key)) {\n            return false;\n        }",
        "        // 最小写入模式下键已存在则跳过写入。\n"
        "        if (minimalPutOverride && region.contains(key)) {\n"
        "            return false;\n        }",
    ),
    (
        "    @Override\n    public SoftLock lockItem(Object key, Object version) throws CacheException {\n        return null;\n    }",
        "    /** 只读策略不提供项级软锁。 */\n"
        "    @Override\n"
        "    public SoftLock lockItem(Object key, Object version) throws CacheException {\n"
        "        return null;\n"
        "    }",
    ),
    (
        "    @Override\n    public void unlockItem(Object key, SoftLock lock) throws CacheException {\n        evict(key);\n    }",
        "    /** 解锁时逐出键。 */\n"
        "    @Override\n"
        "    public void unlockItem(Object key, SoftLock lock) throws CacheException {\n"
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
        "    @Override\n    public boolean insert(Object key, Object value) throws CacheException {\n        return false;\n    }",
        "    /** 插入阶段不写缓存，延迟至 afterInsert。 */\n"
        "    @Override\n"
        "    public boolean insert(Object key, Object value) throws CacheException {\n"
        "        return false;\n"
        "    }",
    ),
    (
        "    @Override\n    public boolean afterInsert(Object key, Object value) throws CacheException {\n        region.put(key, value);\n        return true;\n    }",
        "    /** 插入完成后将自然 ID 映射写入缓存。 */\n"
        "    @Override\n"
        "    public boolean afterInsert(Object key, Object value) throws CacheException {\n"
        "        region.put(key, value);\n"
        "        return true;\n"
        "    }",
    ),
    (
        "    @Override\n    public boolean update(Object key, Object value) throws CacheException {\n        throw new UnsupportedOperationException(\"Unable to update read-only object\");\n    }",
        "    /** 只读自然 ID 禁止更新。 */\n"
        "    @Override\n"
        "    public boolean update(Object key, Object value) throws CacheException {\n"
        "        throw new UnsupportedOperationException(\"Unable to update read-only object\");\n"
        "    }",
    ),
    (
        "    @Override\n    public boolean afterUpdate(Object key, Object value, SoftLock lock) throws CacheException {\n        throw new UnsupportedOperationException(\"Unable to update read-only object\");\n    }",
        "    /** 只读自然 ID 禁止 afterUpdate。 */\n"
        "    @Override\n"
        "    public boolean afterUpdate(Object key, Object value, SoftLock lock) throws CacheException {\n"
        "        throw new UnsupportedOperationException(\"Unable to update read-only object\");\n"
        "    }",
    ),
]

W1B_REPLACEMENTS["ReadWriteCollectionRegionAccessStrategy.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 集合并发策略 {@code read-write} 的 Redisson 区域访问实现。\n"
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
]

W1B_REPLACEMENTS["ReadWriteEntityRegionAccessStrategy.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 实体并发策略 {@code read-write} 的 Redisson 区域访问实现。\n"
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
        "    @Override\n    public boolean insert(Object key, Object value, Object version) throws CacheException {\n        return false;\n    }",
        "    /** 插入阶段不写缓存，延迟至 afterInsert。 */\n"
        "    @Override\n"
        "    public boolean insert(Object key, Object value, Object version) throws CacheException {\n"
        "        return false;\n"
        "    }",
    ),
    (
        "    @Override\n    public boolean afterInsert(Object key, Object value, Object version) throws CacheException {\n        region.put(key, value);\n        return true;\n    }",
        "    /** 插入完成后将实体写入缓存。 */\n"
        "    @Override\n"
        "    public boolean afterInsert(Object key, Object value, Object version) throws CacheException {\n"
        "        region.put(key, value);\n"
        "        return true;\n"
        "    }",
    ),
    (
        "    @Override\n    public boolean update(Object key, Object value, Object currentVersion, Object previousVersion)\n            throws CacheException {\n        return false;\n    }",
        "    /** 更新阶段不立即写入，等待 afterUpdate。 */\n"
        "    @Override\n"
        "    public boolean update(Object key, Object value, Object currentVersion, Object previousVersion)\n"
        "            throws CacheException {\n"
        "        return false;\n"
        "    }",
    ),
    (
        "    @Override\n    public boolean afterUpdate(Object key, Object value, Object currentVersion, Object previousVersion, SoftLock lock)\n            throws CacheException {\n        region.put(key, value);\n        return true;\n    }",
        "    /** 更新完成后将新实体版本写入缓存。 */\n"
        "    @Override\n"
        "    public boolean afterUpdate(Object key, Object value, Object currentVersion, Object previousVersion, SoftLock lock)\n"
        "            throws CacheException {\n"
        "        region.put(key, value);\n"
        "        return true;\n"
        "    }",
    ),
]

W1B_REPLACEMENTS["ReadWriteNaturalIdRegionAccessStrategy.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 自然 ID 并发策略 {@code read-write} 的 Redisson 区域访问实现。\n"
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
        "    @Override\n    public boolean insert(Object key, Object value) throws CacheException {\n        return false;\n    }",
        "    /** 插入阶段不写缓存，延迟至 afterInsert。 */\n"
        "    @Override\n"
        "    public boolean insert(Object key, Object value) throws CacheException {\n"
        "        return false;\n"
        "    }",
    ),
    (
        "    @Override\n    public boolean afterInsert(Object key, Object value) throws CacheException {\n        region.put(key, value);\n        return true;\n    }",
        "    /** 插入完成后将自然 ID 映射写入缓存。 */\n"
        "    @Override\n"
        "    public boolean afterInsert(Object key, Object value) throws CacheException {\n"
        "        region.put(key, value);\n"
        "        return true;\n"
        "    }",
    ),
    (
        "    @Override\n    public boolean update(Object key, Object value) throws CacheException {\n        return false;\n    }",
        "    /** 更新阶段不立即写入，等待 afterUpdate。 */\n"
        "    @Override\n"
        "    public boolean update(Object key, Object value) throws CacheException {\n"
        "        return false;\n"
        "    }",
    ),
    (
        "    @Override\n    public boolean afterUpdate(Object key, Object value, SoftLock lock) throws CacheException {\n        region.put(key, value);\n        return true;\n    }",
        "    /** 更新完成后将新映射写入缓存。 */\n"
        "    @Override\n"
        "    public boolean afterUpdate(Object key, Object value, SoftLock lock) throws CacheException {\n"
        "        region.put(key, value);\n"
        "        return true;\n"
        "    }",
    ),
]

W1B_REPLACEMENTS["TransactionalCollectionRegionAccessStrategy.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 集合并发策略 {@code transactional} 的 Redisson 区域访问实现。\n"
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
        "    @Override\n    public Object get(Object key, long txTimestamp) throws CacheException {",
        "    /** 直接读取缓存条目。 */\n"
        "    @Override\n"
        "    public Object get(Object key, long txTimestamp) throws CacheException {",
    ),
    (
        "        if (minimalPutOverride && region.contains(key)) {\n            return false;\n        }",
        "        // 最小写入模式下键已存在则跳过写入。\n"
        "        if (minimalPutOverride && region.contains(key)) {\n"
        "            return false;\n        }",
    ),
    (
        "    @Override\n    public SoftLock lockItem(Object key, Object version) throws CacheException {\n        return null;\n    }",
        "    /** 事务策略不提供项级软锁。 */\n"
        "    @Override\n"
        "    public SoftLock lockItem(Object key, Object version) throws CacheException {\n"
        "        return null;\n"
        "    }",
    ),
    (
        "    @Override\n    public void unlockItem(Object key, SoftLock lock) throws CacheException {\n    }",
        "    /** 解锁为空操作，事务回滚由 Hibernate 协调。 */\n"
        "    @Override\n"
        "    public void unlockItem(Object key, SoftLock lock) throws CacheException {\n"
        "    }",
    ),
    (
        "    @Override\n    public void remove(Object key) throws CacheException {\n        region.evict(key);\n    }",
        "    /** 移除指定键对应的缓存条目。 */\n"
        "    @Override\n"
        "    public void remove(Object key) throws CacheException {\n"
        "        region.evict(key);\n"
        "    }",
    ),
]

W1B_REPLACEMENTS["TransactionalEntityRegionAccessStrategy.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 实体并发策略 {@code transactional} 的 Redisson 区域访问实现。\n"
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
        "    @Override\n    public Object get(Object key, long txTimestamp) throws CacheException {",
        "    /** 直接读取缓存条目。 */\n"
        "    @Override\n"
        "    public Object get(Object key, long txTimestamp) throws CacheException {",
    ),
    (
        "        if (minimalPutOverride && region.contains(key)) {\n            return false;\n        }",
        "        // 最小写入模式下键已存在则跳过写入。\n"
        "        if (minimalPutOverride && region.contains(key)) {\n"
        "            return false;\n        }",
    ),
    (
        "    @Override\n    public SoftLock lockItem(Object key, Object version) throws CacheException {\n        return null;\n    }",
        "    /** 事务策略不提供项级软锁。 */\n"
        "    @Override\n"
        "    public SoftLock lockItem(Object key, Object version) throws CacheException {\n"
        "        return null;\n"
        "    }",
    ),
    (
        "    @Override\n    public void unlockItem(Object key, SoftLock lock) throws CacheException {\n    }",
        "    /** 解锁为空操作。 */\n"
        "    @Override\n"
        "    public void unlockItem(Object key, SoftLock lock) throws CacheException {\n"
        "    }",
    ),
    (
        "    @Override\n    public EntityRegion getRegion() {",
        "    /** 返回强类型 {@link EntityRegion} 视图。 */\n"
        "    @Override\n"
        "    public EntityRegion getRegion() {",
    ),
    (
        "    @Override\n    public boolean insert(Object key, Object value, Object version) throws CacheException {\n        region.put(key, value);\n        return true;\n    }",
        "    /** 插入阶段立即写入缓存。 */\n"
        "    @Override\n"
        "    public boolean insert(Object key, Object value, Object version) throws CacheException {\n"
        "        region.put(key, value);\n"
        "        return true;\n"
        "    }",
    ),
    (
        "    @Override\n    public boolean afterInsert(Object key, Object value, Object version) throws CacheException {\n        return false;\n    }",
        "    /** 插入后阶段不再重复写入。 */\n"
        "    @Override\n"
        "    public boolean afterInsert(Object key, Object value, Object version) throws CacheException {\n"
        "        return false;\n"
        "    }",
    ),
    (
        "    @Override\n    public void remove(Object key) throws CacheException {\n        region.evict(key);\n    }",
        "    /** 移除指定键对应的缓存条目。 */\n"
        "    @Override\n"
        "    public void remove(Object key) throws CacheException {\n"
        "        region.evict(key);\n"
        "    }",
    ),
    (
        "    @Override\n    public boolean update(Object key, Object value, Object currentVersion, Object previousVersion)\n            throws CacheException {\n        return insert(key, value, currentVersion);\n    }",
        "    /** 更新阶段复用 insert 逻辑立即写入。 */\n"
        "    @Override\n"
        "    public boolean update(Object key, Object value, Object currentVersion, Object previousVersion)\n"
        "            throws CacheException {\n"
        "        return insert(key, value, currentVersion);\n"
        "    }",
    ),
    (
        "    @Override\n    public boolean afterUpdate(Object key, Object value, Object currentVersion, Object previousVersion, SoftLock lock)\n            throws CacheException {\n        return false;\n    }",
        "    /** 更新后阶段不再重复写入。 */\n"
        "    @Override\n"
        "    public boolean afterUpdate(Object key, Object value, Object currentVersion, Object previousVersion, SoftLock lock)\n"
        "            throws CacheException {\n"
        "        return false;\n"
        "    }",
    ),
]

W1B_REPLACEMENTS["TransactionalNaturalIdRegionAccessStrategy.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 自然 ID 并发策略 {@code transactional} 的 Redisson 区域访问实现。\n"
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
        "    @Override\n    public Object get(Object key, long txTimestamp) throws CacheException {",
        "    /** 直接读取缓存条目。 */\n"
        "    @Override\n"
        "    public Object get(Object key, long txTimestamp) throws CacheException {",
    ),
    (
        "        if (minimalPutOverride && region.contains(key)) {\n            return false;\n        }",
        "        // 最小写入模式下键已存在则跳过写入。\n"
        "        if (minimalPutOverride && region.contains(key)) {\n"
        "            return false;\n        }",
    ),
    (
        "    @Override\n    public SoftLock lockItem(Object key, Object version) throws CacheException {\n        return null;\n    }",
        "    /** 事务策略不提供项级软锁。 */\n"
        "    @Override\n"
        "    public SoftLock lockItem(Object key, Object version) throws CacheException {\n"
        "        return null;\n"
        "    }",
    ),
    (
        "    @Override\n    public void unlockItem(Object key, SoftLock lock) throws CacheException {\n    }",
        "    /** 解锁为空操作。 */\n"
        "    @Override\n"
        "    public void unlockItem(Object key, SoftLock lock) throws CacheException {\n"
        "    }",
    ),
    (
        "    @Override\n    public NaturalIdRegion getRegion() {",
        "    /** 返回强类型 {@link NaturalIdRegion} 视图。 */\n"
        "    @Override\n"
        "    public NaturalIdRegion getRegion() {",
    ),
    (
        "    @Override\n    public void remove(Object key) throws CacheException {\n        region.evict(key);\n    }",
        "    /** 移除指定自然 ID 对应的缓存条目。 */\n"
        "    @Override\n"
        "    public void remove(Object key) throws CacheException {\n"
        "        region.evict(key);\n"
        "    }",
    ),
    (
        "    @Override\n    public boolean insert(Object key, Object value) throws CacheException {\n        region.put(key, value);\n        return true;\n    }",
        "    /** 插入阶段立即写入缓存。 */\n"
        "    @Override\n"
        "    public boolean insert(Object key, Object value) throws CacheException {\n"
        "        region.put(key, value);\n"
        "        return true;\n"
        "    }",
    ),
    (
        "    @Override\n    public boolean afterInsert(Object key, Object value) throws CacheException {\n        return false;\n    }",
        "    /** 插入后阶段不再重复写入。 */\n"
        "    @Override\n"
        "    public boolean afterInsert(Object key, Object value) throws CacheException {\n"
        "        return false;\n"
        "    }",
    ),
    (
        "    @Override\n    public boolean update(Object key, Object value) throws CacheException {\n        return insert(key, value);\n    }",
        "    /** 更新阶段复用 insert 逻辑立即写入。 */\n"
        "    @Override\n"
        "    public boolean update(Object key, Object value) throws CacheException {\n"
        "        return insert(key, value);\n"
        "    }",
    ),
    (
        "    @Override\n    public boolean afterUpdate(Object key, Object value, SoftLock lock) throws CacheException {\n        return false;\n    }",
        "    /** 更新后阶段不再重复写入。 */\n"
        "    @Override\n"
        "    public boolean afterUpdate(Object key, Object value, SoftLock lock) throws CacheException {\n"
        "        return false;\n"
        "    }",
    ),
]

W1B_REPLACEMENTS["JndiRedissonRegionFactory.java"] = [
    (
        "/**\n * Hibernate Cache region factory based on Redisson. \n * Uses Redisson instance located in JNDI.\n * \n * @author Nikita Koksharov \n *\n */",
        "/**\n"
        " * 基于 Redisson 的 Hibernate 缓存区域工厂（Hibernate 5）。\n"
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

W1B_REPLACEMENTS["JndiRedissonRegionNativeFactory.java"] = [
    (
        "/**\n * Hibernate Cache region factory based on Redisson. \n * Uses Redisson instance located in JNDI.\n * \n * @author Nikita Koksharov \n *\n */",
        "/**\n"
        " * 基于 Redisson Native 编解码的 Hibernate 缓存区域工厂（Hibernate 5）。\n"
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
