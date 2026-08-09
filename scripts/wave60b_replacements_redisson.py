"""Chinese annotation replacements for Redisson 4.7.0 wave-60b api [15:30]."""
from __future__ import annotations

_B = "redisson/src/main/java/org/redisson/api/bucket"
_C = "redisson/src/main/java/org/redisson/api/condition"
_F = "redisson/src/main/java/org/redisson/api/cuckoofilter"
_E = "redisson/src/main/java/org/redisson/api/executor"

W60B_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    f"{_B}/CompareAndDeleteArgs.java": [
        (
            "/**\n * Arguments for {@link org.redisson.api.RBucket#compareAndDelete(CompareAndDeleteArgs)} method.\n * Defines conditions for conditional deletion of bucket value.\n *\n * @author Nikita Koksharov\n *\n * @param <V> value type\n */",
            "/**\n * {@link org.redisson.api.RBucket#compareAndDelete(CompareAndDeleteArgs)} 的参数对象；\n * 定义按当前值比较条件删除 Bucket 的规则。\n * <p>\n * 支持值相等/不等及摘要（digest）比较等多种模式。\n *\n * @author Nikita Koksharov\n *\n * @param <V> 值类型\n */",
        ),
        (
            "/**\n     * Deletes bucket if stored value does not equal specified object.\n     * Compatible with any Valkey or Redis version.\n     *\n     * @param object value to compare\n     * @param <V> value type\n     * @return arguments object\n     */",
            "/**\n     * 当存储值与指定对象不相等时删除 Bucket。\n     * 兼容任意 Valkey 或 Redis 版本。\n     *\n     * @param object 待比较的值\n     * @param <V> 值类型\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Deletes bucket if stored value equals specified object.\n     * Compatible with any Valkey or Redis version.\n     *\n     * @param object value to compare\n     * @param <V> value type\n     * @return arguments object\n     */",
            "/**\n     * 当存储值与指定对象相等时删除 Bucket。\n     * 兼容任意 Valkey 或 Redis 版本。\n     *\n     * @param object 待比较的值\n     * @param <V> 值类型\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Deletes bucket if stored value's digest equals specified digest.\n     * Uses DELEX IFDEQ command. Requires Valkey 8+ or Redis 8.4+.\n     *\n     * @param value digest value (hexadecimal string from DIGEST command)\n     * @param <V> value type\n     * @return arguments object\n     */",
            "/**\n     * 当存储值的摘要与指定摘要相等时删除 Bucket。\n     * 使用 DELEX IFDEQ 命令，需 Valkey 8+ 或 Redis 8.4+。\n     *\n     * @param value 摘要值（DIGEST 命令返回的十六进制字符串）\n     * @param <V> 值类型\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Deletes bucket if stored value's digest does not equal specified digest.\n     * Uses DELEX IFDNE command. Requires Valkey 8+ or Redis 8.4+.\n     *\n     * @param value digest value (hexadecimal string from DIGEST command)\n     * @param <V> value type\n     * @return arguments object\n     */",
            "/**\n     * 当存储值的摘要与指定摘要不同时删除 Bucket。\n     * 使用 DELEX IFDNE 命令，需 Valkey 8+ 或 Redis 8.4+。\n     *\n     * @param value 摘要值（DIGEST 命令返回的十六进制字符串）\n     * @param <V> 值类型\n     * @return 参数对象\n     */",
        ),
    ],
    f"{_B}/CompareAndDeleteParams.java": [
        (
            "/**\n * Implementation of {@link CompareAndDeleteArgs}.\n *\n * @author Nikita Koksharov\n *\n * @param <V> value type\n */",
            "/**\n * {@link CompareAndDeleteArgs} 的默认实现，保存比较条件类型与比较值/摘要。\n * <p>\n * 由静态工厂方法创建，供 {@link org.redisson.api.RBucket} 内部解析。\n *\n * @author Nikita Koksharov\n *\n * @param <V> 值类型\n */",
        ),
        (
            "    CompareAndDeleteParams(ConditionType conditionType, V object) {",
            "    /** 按值比较条件构造参数对象。 */\n    CompareAndDeleteParams(ConditionType conditionType, V object) {",
        ),
        (
            "    CompareAndDeleteParams(ConditionType conditionType, String digest) {",
            "    /** 按摘要比较条件构造参数对象。 */\n    CompareAndDeleteParams(ConditionType conditionType, String digest) {",
        ),
        (
            "    public ConditionType getConditionType() {",
            "    /** 返回比较条件类型。 */\n    public ConditionType getConditionType() {",
        ),
        (
            "    public V getValue() {",
            "    /** 返回待比较的对象值。 */\n    public V getValue() {",
        ),
        (
            "    public String getDigest() {",
            "    /** 返回待比较的摘要值。 */\n    public String getDigest() {",
        ),
    ],
    f"{_B}/CompareAndSetArgs.java": [
        (
            "/**\n * Arguments for compare-and-set operation on RBucket.\n * <p>\n * Use one of the static factory methods to create a condition, then call {@code set()} to specify\n * the new value, and optionally configure TTL or expiration time.\n * <p>\n * Supports multiple comparison modes:\n * <ul>\n *   <li>{@link #expected(Object)} - Set if current value equals expected value (compatible with any Redis/Valkey version)</li>\n *   <li>{@link #unexpected(Object)} - Set if current value does not equal unexpected value (compatible with any Redis/Valkey version)</li>\n *   <li>{@link #expectedDigest(String)} - Set if current value's hash digest equals expected digest (requires Redis 8.4+, uses SET IFDEQ)</li>\n *   <li>{@link #unexpectedDigest(String)} - Set if current value's hash digest does not equal unexpected digest (requires Redis 8.4+, uses SET IFDNE)</li>\n * </ul>\n * <p>\n *\n * @author Nikita Koksharov\n *\n * @param <V> value type\n */",
            "/**\n * RBucket 比较并设置（compare-and-set）操作的参数接口。\n * <p>\n * 先通过静态工厂方法创建比较条件，再调用 {@code set()} 指定新值，\n * 并可选择配置 TTL 或绝对过期时间。\n * <p>\n * 支持的比较模式：\n * <ul>\n *   <li>{@link #expected(Object)} — 当前值等于期望值时设置（任意 Redis/Valkey 版本）</li>\n *   <li>{@link #unexpected(Object)} — 当前值不等于指定值时设置（任意 Redis/Valkey 版本）</li>\n *   <li>{@link #expectedDigest(String)} — 摘要相等时设置（需 Redis 8.4+，SET IFDEQ）</li>\n *   <li>{@link #unexpectedDigest(String)} — 摘要不同时设置（需 Redis 8.4+，SET IFDNE）</li>\n * </ul>\n *\n * @author Nikita Koksharov\n *\n * @param <V> 值类型\n */",
        ),
        (
            "/**\n     * Creates a condition that succeeds if the current value equals the expected value.\n     * <p>\n     * This mode is compatible with any Valkey or Redis version.\n     *\n     * @param <V> value type\n     * @param object expected current value (can be null to check for non-existence)\n     * @return condition builder requiring {@code set()} to be called\n     */",
            "/**\n     * 创建「当前值等于期望值」时成功的比较条件。\n     * <p>\n     * 兼容任意 Valkey 或 Redis 版本。\n     *\n     * @param <V> 值类型\n     * @param object 期望的当前值（可为 null 表示键不存在）\n     * @return 需继续调用 {@code set()} 的条件构建器\n     */",
        ),
        (
            "/**\n     * Creates a condition that succeeds if the current value does not equal the unexpected value.\n     * <p>\n     * This mode is compatible with any Valkey or Redis version.\n     *\n     * @param <V> value type\n     * @param object unexpected current value\n     * @return condition builder requiring {@code set()} to be called\n     */",
            "/**\n     * 创建「当前值不等于指定值」时成功的比较条件。\n     * <p>\n     * 兼容任意 Valkey 或 Redis 版本。\n     *\n     * @param <V> 值类型\n     * @param object 不期望的当前值\n     * @return 需继续调用 {@code set()} 的条件构建器\n     */",
        ),
        (
            "/**\n     * Creates a condition that succeeds if the hash digest of the current value equals the expected digest.\n     * <p>\n     * This mode uses the SET IFDEQ command and requires Redis 8.4+ or compatible Valkey version.\n     * The digest can be obtained using the DIGEST command.\n     *\n     * @param <V> value type\n     * @param value expected hash digest value (hexadecimal string from DIGEST command)\n     * @return condition builder requiring {@code set()} to be called\n     */",
            "/**\n     * 创建「当前值摘要等于期望摘要」时成功的比较条件。\n     * <p>\n     * 使用 SET IFDEQ 命令，需 Redis 8.4+ 或兼容的 Valkey 版本；\n     * 摘要可通过 DIGEST 命令获取。\n     *\n     * @param <V> 值类型\n     * @param value 期望的哈希摘要（DIGEST 命令返回的十六进制字符串）\n     * @return 需继续调用 {@code set()} 的条件构建器\n     */",
        ),
        (
            "/**\n     * Creates a condition that succeeds if the hash digest of the current value does not equal the unexpected digest.\n     * <p>\n     * This mode uses the SET IFDNE command and requires Redis 8.4+ or compatible Valkey version.\n     * The digest can be obtained using the DIGEST command.\n     *\n     * @param <V> value type\n     * @param value unexpected hash digest value (hexadecimal string from DIGEST command)\n     * @return condition builder requiring {@code set()} to be called\n     */",
            "/**\n     * 创建「当前值摘要不等于指定摘要」时成功的比较条件。\n     * <p>\n     * 使用 SET IFDNE 命令，需 Redis 8.4+ 或兼容的 Valkey 版本；\n     * 摘要可通过 DIGEST 命令获取。\n     *\n     * @param <V> 值类型\n     * @param value 不期望的哈希摘要（DIGEST 命令返回的十六进制字符串）\n     * @return 需继续调用 {@code set()} 的条件构建器\n     */",
        ),
        (
            "/**\n     * Sets the time-to-live duration for the key.\n     * This is optional and can be combined with the set operation.\n     *\n     * @param duration time-to-live duration\n     * @return this instance for method chaining\n     */",
            "/**\n     * 设置键的生存时间（TTL）。\n     * 可选配置，可与 set 操作组合使用。\n     *\n     * @param duration 生存时长\n     * @return 当前实例，支持链式调用\n     */",
        ),
        (
            "/**\n     * Sets the expiration time as an absolute instant.\n     * This is optional and can be combined with the set operation.\n     *\n     * @param time expiration instant\n     * @return this instance for method chaining\n     */",
            "/**\n     * 设置键的绝对过期时间点。\n     * 可选配置，可与 set 操作组合使用。\n     *\n     * @param time 过期时刻\n     * @return 当前实例，支持链式调用\n     */",
        ),
    ],
    f"{_B}/CompareAndSetParams.java": [
        (
            "/**\n * Implementation of {@link CompareAndSetStep} and {@link CompareAndSetArgs}.\n *\n * @author Nikita Koksharov\n *\n * @param <V> value type\n */",
            "/**\n * {@link CompareAndSetStep} 与 {@link CompareAndSetArgs} 的默认实现。\n * <p>\n * 保存比较条件、新值以及 TTL/过期时间等可选参数，供 RBucket 内部执行 CAS 操作。\n *\n * @author Nikita Koksharov\n *\n * @param <V> 值类型\n */",
        ),
        (
            "    @Override\n    public CompareAndSetArgs<V> set(V value) {",
            "    /** 设置条件满足时要写入的新值。 */\n    @Override\n    public CompareAndSetArgs<V> set(V value) {",
        ),
        (
            "    @Override\n    public CompareAndSetArgs<V> timeToLive(Duration duration) {",
            "    /** 设置写入后的生存时间。 */\n    @Override\n    public CompareAndSetArgs<V> timeToLive(Duration duration) {",
        ),
        (
            "    @Override\n    public CompareAndSetArgs<V> expireAt(Instant time) {",
            "    /** 设置写入后的绝对过期时间。 */\n    @Override\n    public CompareAndSetArgs<V> expireAt(Instant time) {",
        ),
        (
            "    public ConditionType getConditionType() {",
            "    /** 返回比较条件类型。 */\n    public ConditionType getConditionType() {",
        ),
        (
            "    public V getNewValue() {",
            "    /** 返回待写入的新值。 */\n    public V getNewValue() {",
        ),
    ],
    f"{_B}/CompareAndSetStep.java": [
        (
            "/**\n * Intermediate builder interface for compare-and-set operations.\n * Returned by condition factory methods and requires {@link #set(Object)} to be called.\n *\n * @author Nikita Koksharov\n *\n * @param <V> value type\n */",
            "/**\n * compare-and-set 操作的中间构建器接口。\n * <p>\n * 由条件工厂方法返回，必须继续调用 {@link #set(Object)} 指定新值。\n *\n * @author Nikita Koksharov\n *\n * @param <V> 值类型\n */",
        ),
        (
            "/**\n     * Sets the new value to be stored if the condition is met.\n     * This method is required.\n     *\n     * @param value new value to set\n     * @return CompareAndSetArgs for optional configuration (timeToLive, expireAt)\n     */",
            "/**\n     * 设置条件满足时要存储的新值（必填）。\n     *\n     * @param value 新值\n     * @return {@link CompareAndSetArgs}，可继续配置 TTL 或过期时间\n     */",
        ),
    ],
    f"{_B}/ConditionType.java": [
        (
            "/**\n *\n * @author Nikita Koksharov\n *\n */",
            "/**\n * Bucket 比较并设置/删除操作使用的条件类型枚举。\n * <p>\n * 区分值相等/不等及摘要（digest）相等/不等四种模式。\n *\n * @author Nikita Koksharov\n *\n */",
        ),
        (
            "    EXPECTED,\n    UNEXPECTED,\n    EXPECTED_DIGEST,\n    UNEXPECTED_DIGEST",
            "    /** 当前值等于期望值。 */\n    EXPECTED,\n    /** 当前值不等于指定值。 */\n    UNEXPECTED,\n    /** 当前值摘要等于期望摘要。 */\n    EXPECTED_DIGEST,\n    /** 当前值摘要不等于指定摘要。 */\n    UNEXPECTED_DIGEST",
        ),
    ],
    f"{_B}/SetArgs.java": [
        (
            "/**\n * Arguments object.\n *\n * @author seakider\n * @author Nikita Koksharov\n *\n */",
            "/**\n * 批量设置 Bucket 键值对的参数接口。\n * <p>\n * 支持指定条目映射，并可配置 TTL、保留原 TTL 或绝对过期时间。\n *\n * @author seakider\n * @author Nikita Koksharov\n *\n */",
        ),
        (
            "/**\n     * Defines entries to set\n     *\n     * @param values entries map to set\n     * @return arguments object\n     */",
            "/**\n     * 创建包含待设置条目映射的参数对象。\n     *\n     * @param values 键值映射\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Defines retain the time to live associated with the keys\n     *\n     * @return SetArgs object\n     */",
            "/**\n     * 保留各键原有的 TTL（不重置过期时间）。\n     *\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Defines the specified expiration time.\n     *\n     * @param ttl time to live duration\n     * @return SetArgs object\n     */",
            "/**\n     * 为写入的键设置生存时间。\n     *\n     * @param ttl 生存时长\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Defines the specified Unix time at which the key(s) will expire.\n     *\n     * @param time expire date\n     * @return SetArgs object\n     */",
            "/**\n     * 为写入的键设置绝对过期时间点（Unix 时间）。\n     *\n     * @param time 过期时刻\n     * @return 参数对象\n     */",
        ),
    ],
    f"{_B}/SetParams.java": [
        (
            "/**\n *\n * @author seakider\n *\n */",
            "/**\n * {@link SetArgs} 的默认实现，保存待写入条目及 TTL 相关选项。\n * <p>\n * 由 {@link SetArgs#entries(java.util.Map)} 工厂方法创建。\n *\n * @author seakider\n *\n */",
        ),
        (
            "    @Override\n    public SetArgs keepTTL() {",
            "    /** 启用保留原 TTL 选项。 */\n    @Override\n    public SetArgs keepTTL() {",
        ),
        (
            "    @Override\n    public SetArgs timeToLive(Duration ttl) {",
            "    /** 设置生存时间。 */\n    @Override\n    public SetArgs timeToLive(Duration ttl) {",
        ),
        (
            "    @Override\n    public SetArgs expireAt(Instant time) {",
            "    /** 设置绝对过期时间。 */\n    @Override\n    public SetArgs expireAt(Instant time) {",
        ),
        (
            "    public Map<String, ?> getEntries() {",
            "    /** 返回待写入的键值映射。 */\n    public Map<String, ?> getEntries() {",
        ),
    ],
    f"{_C}/Condition.java": [
        (
            "/**\n * Interface for conditional object\n * \n * @author Nikita Koksharov\n *\n */",
            "/**\n * Live Object 查询条件的标记接口。\n * <p>\n * 具体条件由 {@link Conditions} 工厂创建，\n * 用于按字段过滤 Live Object 集合。\n *\n * @author Nikita Koksharov\n *\n */",
        ),
    ],
    f"{_C}/Conditions.java": [
        (
            "/**\n * Conditions factory to search for Live Objects by fields.\n * \n * @author Nikita Koksharov\n *\n */",
            "/**\n * 按字段构建 Live Object 查询条件的工厂类。\n * <p>\n * 提供等于、范围、IN、AND/OR 等组合条件的静态方法。\n *\n * @author Nikita Koksharov\n *\n */",
        ),
        (
            "/**\n     * Returns \"IN\" condition for property by <code>name</code> and allowed set of <code>values</code> \n     * \n     * @param name - name of property\n     * @param values - array of allowed values \n     * @return condition\n     */",
            "/**\n     * 返回属性 <code>name</code> 取值属于给定集合之一的 IN 条件。\n     *\n     * @param name 属性名\n     * @param values 允许的值数组\n     * @return 查询条件\n     */",
        ),
        (
            "/**\n     * Returns \"IN\" condition for property by <code>name</code> and allowed set of <code>values</code>\n     * \n     * @param name - name of property\n     * @param values - collection of allowed values \n     * @return condition\n     */",
            "/**\n     * 返回属性 <code>name</code> 取值属于给定集合之一的 IN 条件。\n     *\n     * @param name 属性名\n     * @param values 允许的值集合\n     * @return 查询条件\n     */",
        ),
        (
            "/**\n     * Returns \"EQUALS\" condition which restricts property by <code>name</code> to defined <code>value</code>\n     * \n     * @param name - name of property\n     * @param value - defined value\n     * @return condition\n     */",
            "/**\n     * 返回属性 <code>name</code> 等于 <code>value</code> 的 EQUALS 条件。\n     *\n     * @param name 属性名\n     * @param value 期望值\n     * @return 查询条件\n     */",
        ),
        (
            "/**\n     * Returns \"OR\" condition for collection of nested <code>conditions</code>\n     * \n     * @param conditions - nested condition objects\n     * @return condition\n     */",
            "/**\n     * 返回多个嵌套条件的 OR 组合条件。\n     *\n     * @param conditions 嵌套条件数组\n     * @return 查询条件\n     */",
        ),
        (
            "/**\n     * Returns \"AND\" condition for collection of nested <code>conditions</code>\n     * \n     * @param conditions - nested condition objects\n     * @return condition\n     */",
            "/**\n     * 返回多个嵌套条件的 AND 组合条件。\n     *\n     * @param conditions 嵌套条件数组\n     * @return 查询条件\n     */",
        ),
        (
            "/**\n     * Returns \"GREATER THAN\" condition which restricts property by <code>name</code> to defined <code>value</code>\n     * \n     * @param name - name of property\n     * @param value - defined value\n     * @return condition\n     */",
            "/**\n     * 返回属性 <code>name</code> 大于 <code>value</code> 的 GT 条件。\n     *\n     * @param name 属性名\n     * @param value 比较值\n     * @return 查询条件\n     */",
        ),
        (
            "/**\n     * Returns \"LESS THAN\" condition which restricts property by <code>name</code> to defined <code>value</code>\n     * \n     * @param name - name of property\n     * @param value - defined value\n     * @return condition\n     */",
            "/**\n     * 返回属性 <code>name</code> 小于 <code>value</code> 的 LT 条件。\n     *\n     * @param name 属性名\n     * @param value 比较值\n     * @return 查询条件\n     */",
        ),
        (
            "/**\n     * Returns \"GREATER THAN ON EQUAL\" condition which restricts property by <code>name</code> to defined <code>value</code>\n     * \n     * @param name - name of property\n     * @param value - defined value\n     * @return condition\n     */",
            "/**\n     * 返回属性 <code>name</code> 大于等于 <code>value</code> 的 GE 条件。\n     *\n     * @param name 属性名\n     * @param value 比较值\n     * @return 查询条件\n     */",
        ),
        (
            "/**\n     * Returns \"LESS THAN ON EQUAL\" condition which restricts property by <code>name</code> to defined <code>value</code>\n     * \n     * @param name - name of property\n     * @param value - defined value\n     * @return condition\n     */",
            "/**\n     * 返回属性 <code>name</code> 小于等于 <code>value</code> 的 LE 条件。\n     *\n     * @param name 属性名\n     * @param value 比较值\n     * @return 查询条件\n     */",
        ),
    ],
    f"{_F}/CuckooFilterAddArgs.java": [
        (
            "/**\n * Arguments for cuckoo filter bulk add operations.\n *\n * <p>Usage example:\n * <pre>\n *     Set&lt;String&gt; added = filter.add(\n *         CuckooFilterAddArgs.&lt;String&gt;items(List.of(\"a\", \"b\", \"c\"))\n *                 .capacity(50000)\n *                 .noCreate());\n * </pre>\n *\n * @param <V> element type\n *\n * @author Nikita Koksharov\n *\n */",
            "/**\n * 布谷鸟过滤器（Cuckoo Filter）批量添加元素的参数接口。\n *\n * <p>用法示例：\n * <pre>\n *     Set&lt;String&gt; added = filter.add(\n *         CuckooFilterAddArgs.&lt;String&gt;items(List.of(\"a\", \"b\", \"c\"))\n *                 .capacity(50000)\n *                 .noCreate());\n * </pre>\n *\n * @param <V> 元素类型\n *\n * @author Nikita Koksharov\n *\n */",
        ),
        (
            "/**\n     * Creates arguments with the specified items to insert.\n     *\n     * @param items elements to insert\n     * @param <V>   element type\n     * @return arguments instance\n     */",
            "/**\n     * 创建包含待插入元素集合的参数对象。\n     *\n     * @param items 待插入元素\n     * @param <V> 元素类型\n     * @return 参数实例\n     */",
        ),
        (
            "/**\n     * Defines the desired capacity if the filter\n     * is auto-created by this command.\n     *\n     * @param capacity filter capacity for auto-creation\n     * @return arguments instance\n     */",
            "/**\n     * 设置命令自动创建过滤器时的期望容量。\n     *\n     * @param capacity 自动创建时的过滤器容量\n     * @return 参数实例\n     */",
        ),
        (
            "/**\n     * Prevents auto-creation of the filter.\n     * The command will fail if the filter does not already exist.\n     *\n     * @return arguments instance\n     */",
            "/**\n     * 禁止自动创建过滤器；若过滤器不存在则命令失败。\n     *\n     * @return 参数实例\n     */",
        ),
    ],
    f"{_F}/CuckooFilterAddArgsImpl.java": [
        (
            "/**\n * @param <V> element type\n *\n * @author Nikita Koksharov\n *\n */",
            "/**\n * {@link CuckooFilterAddArgs} 的默认实现。\n * <p>\n * 保存待插入元素、自动创建容量及 noCreate 标志。\n *\n * @param <V> 元素类型\n *\n * @author Nikita Koksharov\n *\n */",
        ),
        (
            "    @Override\n    public CuckooFilterAddArgs<V> capacity(long capacity) {",
            "    /** 设置自动创建时的容量。 */\n    @Override\n    public CuckooFilterAddArgs<V> capacity(long capacity) {",
        ),
        (
            "    @Override\n    public CuckooFilterAddArgs<V> noCreate() {",
            "    /** 标记禁止自动创建过滤器。 */\n    @Override\n    public CuckooFilterAddArgs<V> noCreate() {",
        ),
        (
            "    public Collection<V> getItems() {",
            "    /** 返回待插入的元素集合。 */\n    public Collection<V> getItems() {",
        ),
        (
            "    public boolean isNoCreate() {",
            "    /** 返回是否禁止自动创建。 */\n    public boolean isNoCreate() {",
        ),
    ],
    f"{_F}/CuckooFilterInitArgs.java": [
        (
            "/**\n * Arguments for cuckoo filter initialization.\n *\n * <p>Usage example:\n * <pre>\n *     filter.init(CuckooFilterInitArgs.capacity(100000)\n *                     .bucketSize(4)\n *                     .maxIterations(500)\n *                     .expansion(2));\n * </pre>\n *\n * @author Nikita Koksharov\n *\n */",
            "/**\n * 布谷鸟过滤器初始化参数接口。\n *\n * <p>用法示例：\n * <pre>\n *     filter.init(CuckooFilterInitArgs.capacity(100000)\n *                     .bucketSize(4)\n *                     .maxIterations(500)\n *                     .expansion(2));\n * </pre>\n *\n * @author Nikita Koksharov\n *\n */",
        ),
        (
            "/**\n     * Creates arguments with the specified capacity.\n     *\n     * @param capacity expected number of items to store in the filter\n     * @return arguments instance\n     */",
            "/**\n     * 创建指定容量的初始化参数。\n     *\n     * @param capacity 过滤器预期存储的元素数量\n     * @return 参数实例\n     */",
        ),
        (
            "/**\n     * Defines the number of items per bucket.\n     * <p>\n     * Default value is 2.\n     * A higher bucket size improves fill rate but increases error rate.\n     *\n     * @param bucketSize number of items per bucket\n     * @return arguments instance\n     */",
            "/**\n     * 设置每个桶可容纳的元素数量。\n     * <p>\n     * 默认值为 2；桶越大填充率越高，但误判率也可能上升。\n     *\n     * @param bucketSize 每桶元素数\n     * @return 参数实例\n     */",
        ),
        (
            "/**\n     * Defines the maximum number of attempts to swap items\n     * between buckets before declaring the filter full.\n     * <p>\n     * Default value is 20.\n     *\n     * @param maxIterations max number of swap attempts\n     * @return arguments instance\n     */",
            "/**\n     * 设置判定过滤器已满前，桶间交换元素的最大尝试次数。\n     * <p>\n     * 默认值为 20。\n     *\n     * @param maxIterations 最大交换尝试次数\n     * @return 参数实例\n     */",
        ),
        (
            "/**\n     * Defines the expansion rate when the filter becomes full.\n     * <p>\n     * Default value is 1.\n     *\n     * @param expansion expansion rate\n     * @return arguments instance\n     */",
            "/**\n     * 设置过滤器满时的扩容倍率。\n     * <p>\n     * 默认值为 1。\n     *\n     * @param expansion 扩容倍率\n     * @return 参数实例\n     */",
        ),
    ],
    f"{_F}/CuckooFilterInitArgsImpl.java": [
        (
            "/**\n *\n * @author Nikita Koksharov\n *\n */",
            "/**\n * {@link CuckooFilterInitArgs} 的默认实现。\n * <p>\n * 保存容量、桶大小、最大迭代次数及扩容倍率等初始化参数。\n *\n * @author Nikita Koksharov\n *\n */",
        ),
        (
            "    @Override\n    public CuckooFilterInitArgs bucketSize(long bucketSize) {",
            "    /** 设置每桶元素数。 */\n    @Override\n    public CuckooFilterInitArgs bucketSize(long bucketSize) {",
        ),
        (
            "    @Override\n    public CuckooFilterInitArgs maxIterations(long maxIterations) {",
            "    /** 设置最大交换迭代次数。 */\n    @Override\n    public CuckooFilterInitArgs maxIterations(long maxIterations) {",
        ),
        (
            "    @Override\n    public CuckooFilterInitArgs expansion(long expansion) {",
            "    /** 设置扩容倍率。 */\n    @Override\n    public CuckooFilterInitArgs expansion(long expansion) {",
        ),
        (
            "    public long getCapacity() {",
            "    /** 返回过滤器容量。 */\n    public long getCapacity() {",
        ),
    ],
    f"{_E}/TaskFailureListener.java": [
        (
            "/**\n * Task listener invoked when task was failed during execution\n *\n * @author Nikita Koksharov\n *\n */",
            "/**\n * 分布式执行器任务失败时的回调监听器。\n * <p>\n * 当远程任务执行抛出异常时触发 {@link #onFailed(String, Throwable)}。\n *\n * @author Nikita Koksharov\n *\n */",
        ),
        (
            "/**\n     * Invoked when task was failed during execution\n     *\n     * @param taskId - id of task\n     * @param exception - exception during task execution\n     */",
            "/**\n     * 任务执行失败时调用。\n     *\n     * @param taskId 任务 ID\n     * @param exception 执行期间抛出的异常\n     */",
        ),
    ],
}
