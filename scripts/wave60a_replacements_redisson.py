"""Chinese annotation replacements for Redisson 4.7.0 wave-60a bitvector/bloomfilter [0:15]."""
from __future__ import annotations

_BV = "redisson/src/main/java/org/redisson/api/bitvector/"
_BF = "redisson/src/main/java/org/redisson/api/bloomfilter/"

W60A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    f"{_BV}MatchExactArgs.java": [
        (
            "/**\n * Argument builder for the\n * {@link org.redisson.api.RBitVectorStore#matchExact(MatchExactArgs) matchExact} query.\n * <p>\n * Carries the required mask and target plus optional iteration-tuning parameters\n * that control server-side batching during result iteration. The exact-match\n * predicate is {@code (vector & mask) == target}: bits outside the mask are\n * ignored, and bits inside the mask must equal the corresponding bits of\n * {@code target}.\n * <pre>{@code\n *   MatchExactArgs args = MatchExactArgs.mask(0b101001L)\n *                                       .target(0b100001L)\n *                                       .chunkSize(2048)\n *                                       .chunkFetchTTL(Duration.ofMinutes(2));\n * }</pre>\n * <p>\n * If {@code target} has any bits set outside of {@code mask}, the predicate is\n * unsatisfiable and the query will produce an empty result.\n *\n * @see MatchArgs\n * @see MatchTargetArgs\n *\n * @author Nikita Koksharov\n *\n */",
            "/**\n * {@link org.redisson.api.RBitVectorStore#matchExact(MatchExactArgs) matchExact} 精确匹配查询的参数构建器。\n * <p>\n * 携带必需的掩码（mask）与目标值（target），以及控制服务端分批迭代结果的可选调优参数。\n * 精确匹配谓词为 {@code (vector & mask) == target}：掩码外比特被忽略，掩码内比特须与 {@code target} 对应位相等。\n * <pre>{@code\n *   MatchExactArgs args = MatchExactArgs.mask(0b101001L)\n *                                       .target(0b100001L)\n *                                       .chunkSize(2048)\n *                                       .chunkFetchTTL(Duration.ofMinutes(2));\n * }</pre>\n * <p>\n * 若 {@code target} 在 {@code mask} 外有置位比特，则谓词不可满足，查询结果为空。\n *\n * @see MatchArgs\n * @see MatchTargetArgs\n * @author Nikita Koksharov\n */",
        ),
        (
            "    /**\n     * Begins construction by setting the bitmask. The mask selects which bit\n     * positions participate in the equality check; bits outside the mask are\n     * ignored during matching.\n     * <p>\n     * Returns a {@link MatchTargetArgs} stage which must be completed by calling\n     * {@link MatchTargetArgs#target(long)} to obtain a usable {@code MatchExactArgs}.\n     *\n     * @param value the bitmask\n     * @return the next builder stage, awaiting a target value\n     */",
            "    /**\n     * 以位掩码开始构建；掩码选定参与相等比较的比特位，掩码外比特在匹配时被忽略。\n     * <p>\n     * 返回 {@link MatchTargetArgs} 阶段，须调用 {@link MatchTargetArgs#target(long)} 得到可用的 {@code MatchExactArgs}。\n     *\n     * @param value 位掩码\n     * @return 等待目标值的下一构建阶段\n     */",
        ),
        (
            "    /**\n     * Sets the number of keys fetched per server round-trip during result iteration.\n     *\n     * @param value the batch size; must be positive\n     * @return this builder, for chaining\n     */",
            "    /**\n     * 设置结果迭代时每次服务端往返拉取的键数量（批次大小）。\n     *\n     * @param value 批次大小，须为正数\n     * @return 当前构建器，支持链式调用\n     */",
        ),
        (
            "    /**\n     * Sets the time-to-live applied to the server-side iteration state created by\n     * the query. This is a safety net: if the caller abandons the iterator without\n     * consuming it fully (or the JVM dies mid-iteration), the server-side state\n     * will be reclaimed automatically once the TTL expires.\n     *\n     * @param value the TTL applied to server-side iteration state\n     * @return this builder, for chaining\n     */",
            "    /**\n     * 设置查询创建的服务端迭代状态的生存时间（TTL）。\n     * 若调用方未完整消费迭代器或 JVM 中途退出，TTL 到期后服务端状态将被自动回收。\n     *\n     * @param value 应用于服务端迭代状态的 TTL\n     * @return 当前构建器，支持链式调用\n     */",
        ),
    ],
    "MatchExactArgs.java": [
        (
            "/**\n * Argument builder for the\n * {@link org.redisson.api.RBitVectorStore#matchExact(MatchExactArgs) matchExact} query.\n * <p>\n * Carries the required mask and target plus optional iteration-tuning parameters\n * that control server-side batching during result iteration. The exact-match\n * predicate is {@code (vector & mask) == target}: bits outside the mask are\n * ignored, and bits inside the mask must equal the corresponding bits of\n * {@code target}.\n * <pre>{@code\n *   MatchExactArgs args = MatchExactArgs.mask(0b101001L)\n *                                       .target(0b100001L)\n *                                       .chunkSize(2048)\n *                                       .chunkFetchTTL(Duration.ofMinutes(2));\n * }</pre>\n * <p>\n * If {@code target} has any bits set outside of {@code mask}, the predicate is\n * unsatisfiable and the query will produce an empty result.\n *\n * @see MatchArgs\n * @see MatchTargetArgs\n *\n * @author Nikita Koksharov\n *\n */",
            "/**\n * {@link org.redisson.api.RBitVectorStore#matchExact(MatchExactArgs) matchExact} 精确匹配查询的参数构建器。\n * <p>\n * 携带必需的掩码（mask）与目标值（target），以及控制服务端分批迭代结果的可选调优参数。\n * 精确匹配谓词为 {@code (vector & mask) == target}：掩码外比特被忽略，掩码内比特须与 {@code target} 对应位相等。\n * <pre>{@code\n *   MatchExactArgs args = MatchExactArgs.mask(0b101001L)\n *                                       .target(0b100001L)\n *                                       .chunkSize(2048)\n *                                       .chunkFetchTTL(Duration.ofMinutes(2));\n * }</pre>\n * <p>\n * 若 {@code target} 在 {@code mask} 外有置位比特，则谓词不可满足，查询结果为空。\n *\n * @see MatchArgs\n * @see MatchTargetArgs\n * @author Nikita Koksharov\n */",
        ),
        (
            "    /**\n     * Begins construction by setting the bitmask. The mask selects which bit\n     * positions participate in the equality check; bits outside the mask are\n     * ignored during matching.\n     * <p>\n     * Returns a {@link MatchTargetArgs} stage which must be completed by calling\n     * {@link MatchTargetArgs#target(long)} to obtain a usable {@code MatchExactArgs}.\n     *\n     * @param value the bitmask\n     * @return the next builder stage, awaiting a target value\n     */",
            "    /**\n     * 以位掩码开始构建；掩码选定参与相等比较的比特位，掩码外比特在匹配时被忽略。\n     * <p>\n     * 返回 {@link MatchTargetArgs} 阶段，须调用 {@link MatchTargetArgs#target(long)} 得到可用的 {@code MatchExactArgs}。\n     *\n     * @param value 位掩码\n     * @return 等待目标值的下一构建阶段\n     */",
        ),
        (
            "    /**\n     * Sets the number of keys fetched per server round-trip during result iteration.\n     *\n     * @param value the batch size; must be positive\n     * @return this builder, for chaining\n     */",
            "    /**\n     * 设置结果迭代时每次服务端往返拉取的键数量（批次大小）。\n     *\n     * @param value 批次大小，须为正数\n     * @return 当前构建器，支持链式调用\n     */",
        ),
        (
            "    /**\n     * Sets the time-to-live applied to the server-side iteration state created by\n     * the query. This is a safety net: if the caller abandons the iterator without\n     * consuming it fully (or the JVM dies mid-iteration), the server-side state\n     * will be reclaimed automatically once the TTL expires.\n     *\n     * @param value the TTL applied to server-side iteration state\n     * @return this builder, for chaining\n     */",
            "    /**\n     * 设置查询创建的服务端迭代状态的生存时间（TTL）。\n     * 若调用方未完整消费迭代器或 JVM 中途退出，TTL 到期后服务端状态将被自动回收。\n     *\n     * @param value 应用于服务端迭代状态的 TTL\n     * @return 当前构建器，支持链式调用\n     */",
        ),
    ],
    f"{_BV}MatchExactParams.java": [
        (
            "public final class MatchExactParams implements MatchExactArgs, MatchTargetArgs {",
            "/**\n * {@link MatchExactArgs} 与 {@link MatchTargetArgs} 的默认实现，封装精确匹配查询的掩码、目标值及迭代调优参数。\n *\n * @author Nikita Koksharov\n */\npublic final class MatchExactParams implements MatchExactArgs, MatchTargetArgs {",
        ),
        (
            "    long mask;\n    long target;\n    int chunkSize = 10;\n    Duration chunkFetchTTL = Duration.ofMinutes(5);",
            "    /** 参与精确匹配的位掩码。 */\n    long mask;\n    /** 掩码范围内须匹配的目标比特模式。 */\n    long target;\n    /** 每次服务端往返拉取的键数量，默认 10。 */\n    int chunkSize = 10;\n    /** 服务端迭代状态 TTL，默认 5 分钟。 */\n    Duration chunkFetchTTL = Duration.ofMinutes(5);",
        ),
        (
            "    MatchExactParams(long mask) {",
            "    /** 以给定掩码创建参数对象。 */\n    MatchExactParams(long mask) {",
        ),
        (
            "    public long getMask() {",
            "    /** 返回位掩码。 */\n    public long getMask() {",
        ),
        (
            "    public long getTarget() {",
            "    /** 返回目标比特模式。 */\n    public long getTarget() {",
        ),
        (
            "    public int getChunkSize() {",
            "    /** 返回迭代批次大小。 */\n    public int getChunkSize() {",
        ),
        (
            "    public Duration getChunkFetchTTL() {",
            "    /** 返回服务端迭代状态 TTL。 */\n    public Duration getChunkFetchTTL() {",
        ),
    ],
    "MatchExactParams.java": [
        (
            "public final class MatchExactParams implements MatchExactArgs, MatchTargetArgs {",
            "/**\n * {@link MatchExactArgs} 与 {@link MatchTargetArgs} 的默认实现，封装精确匹配查询的掩码、目标值及迭代调优参数。\n *\n * @author Nikita Koksharov\n */\npublic final class MatchExactParams implements MatchExactArgs, MatchTargetArgs {",
        ),
        (
            "    long mask;\n    long target;\n    int chunkSize = 10;\n    Duration chunkFetchTTL = Duration.ofMinutes(5);",
            "    /** 参与精确匹配的位掩码。 */\n    long mask;\n    /** 掩码范围内须匹配的目标比特模式。 */\n    long target;\n    /** 每次服务端往返拉取的键数量，默认 10。 */\n    int chunkSize = 10;\n    /** 服务端迭代状态 TTL，默认 5 分钟。 */\n    Duration chunkFetchTTL = Duration.ofMinutes(5);",
        ),
        (
            "    MatchExactParams(long mask) {",
            "    /** 以给定掩码创建参数对象。 */\n    MatchExactParams(long mask) {",
        ),
        (
            "    public long getMask() {",
            "    /** 返回位掩码。 */\n    public long getMask() {",
        ),
        (
            "    public long getTarget() {",
            "    /** 返回目标比特模式。 */\n    public long getTarget() {",
        ),
        (
            "    public int getChunkSize() {",
            "    /** 返回迭代批次大小。 */\n    public int getChunkSize() {",
        ),
        (
            "    public Duration getChunkFetchTTL() {",
            "    /** 返回服务端迭代状态 TTL。 */\n    public Duration getChunkFetchTTL() {",
        ),
    ],
    f"{_BV}MatchParams.java": [
        (
            "public final class MatchParams implements MatchArgs {",
            "/**\n * {@link MatchArgs} 的默认实现，封装 matchAll/matchAny/matchNone 查询的掩码及迭代调优参数。\n *\n * @author Nikita Koksharov\n */\npublic final class MatchParams implements MatchArgs {",
        ),
        (
            "    long mask;\n    int chunkSize = 10;\n    Duration chunkFetchTTL = Duration.ofMinutes(5);",
            "    /** 参与查询谓词的位掩码。 */\n    long mask;\n    /** 每次服务端往返拉取的键数量，默认 10。 */\n    int chunkSize = 10;\n    /** 服务端迭代状态 TTL，默认 5 分钟。 */\n    Duration chunkFetchTTL = Duration.ofMinutes(5);",
        ),
        (
            "    MatchParams(long mask) {",
            "    /** 以给定掩码创建参数对象。 */\n    MatchParams(long mask) {",
        ),
        (
            "    public long getMask() {",
            "    /** 返回位掩码。 */\n    public long getMask() {",
        ),
        (
            "    public int getChunkSize() {",
            "    /** 返回迭代批次大小。 */\n    public int getChunkSize() {",
        ),
        (
            "    public Duration getChunkFetchTTL() {",
            "    /** 返回服务端迭代状态 TTL。 */\n    public Duration getChunkFetchTTL() {",
        ),
    ],
    "MatchParams.java": [
        (
            "public final class MatchParams implements MatchArgs {",
            "/**\n * {@link MatchArgs} 的默认实现，封装 matchAll/matchAny/matchNone 查询的掩码及迭代调优参数。\n *\n * @author Nikita Koksharov\n */\npublic final class MatchParams implements MatchArgs {",
        ),
        (
            "    long mask;\n    int chunkSize = 10;\n    Duration chunkFetchTTL = Duration.ofMinutes(5);",
            "    /** 参与查询谓词的位掩码。 */\n    long mask;\n    /** 每次服务端往返拉取的键数量，默认 10。 */\n    int chunkSize = 10;\n    /** 服务端迭代状态 TTL，默认 5 分钟。 */\n    Duration chunkFetchTTL = Duration.ofMinutes(5);",
        ),
        (
            "    MatchParams(long mask) {",
            "    /** 以给定掩码创建参数对象。 */\n    MatchParams(long mask) {",
        ),
        (
            "    public long getMask() {",
            "    /** 返回位掩码。 */\n    public long getMask() {",
        ),
        (
            "    public int getChunkSize() {",
            "    /** 返回迭代批次大小。 */\n    public int getChunkSize() {",
        ),
        (
            "    public Duration getChunkFetchTTL() {",
            "    /** 返回服务端迭代状态 TTL。 */\n    public Duration getChunkFetchTTL() {",
        ),
    ],
    f"{_BV}MatchTargetArgs.java": [
        (
            "/**\n * Intermediate stage in the construction of {@link MatchExactArgs}. Produced by\n * {@link MatchExactArgs#mask(long)} and consumed by {@link #target(long)}, which\n * yields a fully-constructed {@link MatchExactArgs} ready for use with\n * {@link org.redisson.api.RBitVectorStore#matchExact(MatchExactArgs)} or further\n * tuning.\n * <p>\n * This staged construction enforces at compile time that both a mask and a target\n * are supplied for an exact-match query.\n *\n * @see MatchExactArgs\n *\n * @author Nikita Koksharov\n *\n */",
            "/**\n * 构建 {@link MatchExactArgs} 的中间阶段；由 {@link MatchExactArgs#mask(long)} 产生，\n * 经 {@link #target(long)} 完成并得到可用于 {@link org.redisson.api.RBitVectorStore#matchExact(MatchExactArgs)} 的完整参数。\n * <p>\n * 分阶段构建在编译期强制精确匹配查询必须同时提供掩码与目标值。\n *\n * @see MatchExactArgs\n * @author Nikita Koksharov\n */",
        ),
        (
            "    /**\n     * Sets the target bit pattern to match against the bits selected by the mask.\n     * The completed predicate is {@code (vector & mask) == target}.\n     * <p>\n     * If {@code value} has any bits set outside of the previously-supplied mask,\n     * the predicate is unsatisfiable and a subsequent query will produce an empty\n     * result.\n     *\n     * @param value the target bit pattern within the masked positions\n     * @return a fully-constructed {@link MatchExactArgs} ready to use or further configure\n     */",
            "    /**\n     * 设置掩码选定比特位上须匹配的目标比特模式；完整谓词为 {@code (vector & mask) == target}。\n     * <p>\n     * 若 {@code value} 在先前提供的掩码外有置位比特，则谓词不可满足，后续查询结果为空。\n     *\n     * @param value 掩码范围内的目标比特模式\n     * @return 可立即使用或继续配置的完整 {@link MatchExactArgs}\n     */",
        ),
    ],
    "MatchTargetArgs.java": [
        (
            "/**\n * Intermediate stage in the construction of {@link MatchExactArgs}. Produced by\n * {@link MatchExactArgs#mask(long)} and consumed by {@link #target(long)}, which\n * yields a fully-constructed {@link MatchExactArgs} ready for use with\n * {@link org.redisson.api.RBitVectorStore#matchExact(MatchExactArgs)} or further\n * tuning.\n * <p>\n * This staged construction enforces at compile time that both a mask and a target\n * are supplied for an exact-match query.\n *\n * @see MatchExactArgs\n *\n * @author Nikita Koksharov\n *\n */",
            "/**\n * 构建 {@link MatchExactArgs} 的中间阶段；由 {@link MatchExactArgs#mask(long)} 产生，\n * 经 {@link #target(long)} 完成并得到可用于 {@link org.redisson.api.RBitVectorStore#matchExact(MatchExactArgs)} 的完整参数。\n * <p>\n * 分阶段构建在编译期强制精确匹配查询必须同时提供掩码与目标值。\n *\n * @see MatchExactArgs\n * @author Nikita Koksharov\n */",
        ),
        (
            "    /**\n     * Sets the target bit pattern to match against the bits selected by the mask.\n     * The completed predicate is {@code (vector & mask) == target}.\n     * <p>\n     * If {@code value} has any bits set outside of the previously-supplied mask,\n     * the predicate is unsatisfiable and a subsequent query will produce an empty\n     * result.\n     *\n     * @param value the target bit pattern within the masked positions\n     * @return a fully-constructed {@link MatchExactArgs} ready to use or further configure\n     */",
            "    /**\n     * 设置掩码选定比特位上须匹配的目标比特模式；完整谓词为 {@code (vector & mask) == target}。\n     * <p>\n     * 若 {@code value} 在先前提供的掩码外有置位比特，则谓词不可满足，后续查询结果为空。\n     *\n     * @param value 掩码范围内的目标比特模式\n     * @return 可立即使用或继续配置的完整 {@link MatchExactArgs}\n     */",
        ),
    ],
    f"{_BF}BloomFilterInfo.java": [
        (
            "/**\n * BloomFilter info for BF.INFO command\n *\n * @author Su Ko\n *\n */",
            "/**\n * 布隆过滤器信息值对象，对应 Redis {@code BF.INFO} 命令的返回字段。\n * 包含容量、位数组大小、子过滤器数量、已插入元素数及扩展倍率等元数据。\n *\n * @author Su Ko\n */",
        ),
        (
            "    public long getExpansionRate() {",
            "    /** 返回扩展倍率（创建新子过滤器时上一子过滤器大小的乘数）。 */\n    public long getExpansionRate() {",
        ),
        (
            "    public long getItemCount() {",
            "    /** 返回已插入元素数量。 */\n    public long getItemCount() {",
        ),
        (
            "    public long getSubFilterCount() {",
            "    /** 返回子过滤器（sub-filter）数量。 */\n    public long getSubFilterCount() {",
        ),
        (
            "    public long getSize() {",
            "    /** 返回位数组总大小（比特数）。 */\n    public long getSize() {",
        ),
        (
            "    public long getCapacity() {",
            "    /** 返回设计容量（预期可插入元素数）。 */\n    public long getCapacity() {",
        ),
    ],
    "BloomFilterInfo.java": [
        (
            "/**\n * BloomFilter info for BF.INFO command\n *\n * @author Su Ko\n *\n */",
            "/**\n * 布隆过滤器信息值对象，对应 Redis {@code BF.INFO} 命令的返回字段。\n * 包含容量、位数组大小、子过滤器数量、已插入元素数及扩展倍率等元数据。\n *\n * @author Su Ko\n */",
        ),
        (
            "    public long getExpansionRate() {",
            "    /** 返回扩展倍率（创建新子过滤器时上一子过滤器大小的乘数）。 */\n    public long getExpansionRate() {",
        ),
        (
            "    public long getItemCount() {",
            "    /** 返回已插入元素数量。 */\n    public long getItemCount() {",
        ),
        (
            "    public long getSubFilterCount() {",
            "    /** 返回子过滤器（sub-filter）数量。 */\n    public long getSubFilterCount() {",
        ),
        (
            "    public long getSize() {",
            "    /** 返回位数组总大小（比特数）。 */\n    public long getSize() {",
        ),
        (
            "    public long getCapacity() {",
            "    /** 返回设计容量（预期可插入元素数）。 */\n    public long getCapacity() {",
        ),
    ],
    f"{_BF}BloomFilterInfoOption.java": [
        (
            "/**\n * BloomFilter option for info command\n *\n * @author Su Ko\n *\n */",
            "/**\n * {@code BF.INFO} 命令可选返回字段枚举；{@link #getOptionString()} 为 Redis 协议字段名。\n *\n * @author Su Ko\n */",
        ),
        (
            "    CAPACITY(\"CAPACITY\"),\n    SIZE(\"SIZE\"),\n    FILTERS(\"FILTERS\"),\n    ITEMS(\"ITEMS\"),\n    EXPANSION(\"EXPANSION\");",
            "    /** 设计容量字段。 */\n    CAPACITY(\"CAPACITY\"),\n    /** 位数组大小字段。 */\n    SIZE(\"SIZE\"),\n    /** 子过滤器数量字段。 */\n    FILTERS(\"FILTERS\"),\n    /** 已插入元素数字段。 */\n    ITEMS(\"ITEMS\"),\n    /** 扩展倍率字段。 */\n    EXPANSION(\"EXPANSION\");",
        ),
        (
            "    public String getOptionString() {",
            "    /** 返回 Redis 协议中的选项字符串。 */\n    public String getOptionString() {",
        ),
    ],
    "BloomFilterInfoOption.java": [
        (
            "/**\n * BloomFilter option for info command\n *\n * @author Su Ko\n *\n */",
            "/**\n * {@code BF.INFO} 命令可选返回字段枚举；{@link #getOptionString()} 为 Redis 协议字段名。\n *\n * @author Su Ko\n */",
        ),
        (
            "    CAPACITY(\"CAPACITY\"),\n    SIZE(\"SIZE\"),\n    FILTERS(\"FILTERS\"),\n    ITEMS(\"ITEMS\"),\n    EXPANSION(\"EXPANSION\");",
            "    /** 设计容量字段。 */\n    CAPACITY(\"CAPACITY\"),\n    /** 位数组大小字段。 */\n    SIZE(\"SIZE\"),\n    /** 子过滤器数量字段。 */\n    FILTERS(\"FILTERS\"),\n    /** 已插入元素数字段。 */\n    ITEMS(\"ITEMS\"),\n    /** 扩展倍率字段。 */\n    EXPANSION(\"EXPANSION\");",
        ),
        (
            "    public String getOptionString() {",
            "    /** 返回 Redis 协议中的选项字符串。 */\n    public String getOptionString() {",
        ),
    ],
    f"{_BF}BloomFilterInitArgs.java": [
        (
            "/**\n * BloomFilterInitArgs for BF.RESERVE command\n *\n * @author Su Ko\n */",
            "/**\n * {@code BF.RESERVE} 命令初始化参数构建器入口；通过 {@link #create()} 开始链式配置。\n *\n * @author Su Ko\n */",
        ),
        (
            "    /**\n     * Defines BloomFilter to BF.RESERVE command\n     *\n     * @return ErrorRateBloomFilterInitArgs\n     */",
            "    /**\n     * 创建 {@code BF.RESERVE} 参数构建流程，下一步设置期望误判率。\n     *\n     * @return 误判率配置阶段\n     */",
        ),
    ],
    "BloomFilterInitArgs.java": [
        (
            "/**\n * BloomFilterInitArgs for BF.RESERVE command\n *\n * @author Su Ko\n */",
            "/**\n * {@code BF.RESERVE} 命令初始化参数构建器入口；通过 {@link #create()} 开始链式配置。\n *\n * @author Su Ko\n */",
        ),
        (
            "    /**\n     * Defines BloomFilter to BF.RESERVE command\n     *\n     * @return ErrorRateBloomFilterInitArgs\n     */",
            "    /**\n     * 创建 {@code BF.RESERVE} 参数构建流程，下一步设置期望误判率。\n     *\n     * @return 误判率配置阶段\n     */",
        ),
    ],
    f"{_BF}BloomFilterInitParams.java": [
        (
            "/**\n * BloomFilterInitParams for BF.RESERVE command\n *\n * @author Su Ko\n *\n */",
            "/**\n * {@code BF.RESERVE} 命令参数的默认实现，实现误判率、容量及可选扩展/非缩放配置的链式构建。\n *\n * @author Su Ko\n */",
        ),
        (
            "    private double errorRate;\n    private long capacity;\n    private Long expansionRate;\n    private Boolean nonScaling;",
            "    /** 期望误判率（0 到 1 之间）。 */\n    private double errorRate;\n    /** 设计容量（预期插入元素数）。 */\n    private long capacity;\n    /** 扩展倍率，与 nonScaling 互斥。 */\n    private Long expansionRate;\n    /** 是否禁止在达到容量时创建新子过滤器。 */\n    private Boolean nonScaling;",
        ),
        (
            "    public double getErrorRate() {",
            "    /** 返回期望误判率。 */\n    public double getErrorRate() {",
        ),
        (
            "    public long getCapacity() {",
            "    /** 返回设计容量。 */\n    public long getCapacity() {",
        ),
        (
            "    public Long getExpansionRate() {",
            "    /** 返回扩展倍率，未设置时为 null。 */\n    public Long getExpansionRate() {",
        ),
        (
            "    public Boolean isNonScaling() {",
            "    /** 返回是否启用非缩放模式。 */\n    public Boolean isNonScaling() {",
        ),
    ],
    "BloomFilterInitParams.java": [
        (
            "/**\n * BloomFilterInitParams for BF.RESERVE command\n *\n * @author Su Ko\n *\n */",
            "/**\n * {@code BF.RESERVE} 命令参数的默认实现，实现误判率、容量及可选扩展/非缩放配置的链式构建。\n *\n * @author Su Ko\n */",
        ),
        (
            "    private double errorRate;\n    private long capacity;\n    private Long expansionRate;\n    private Boolean nonScaling;",
            "    /** 期望误判率（0 到 1 之间）。 */\n    private double errorRate;\n    /** 设计容量（预期插入元素数）。 */\n    private long capacity;\n    /** 扩展倍率，与 nonScaling 互斥。 */\n    private Long expansionRate;\n    /** 是否禁止在达到容量时创建新子过滤器。 */\n    private Boolean nonScaling;",
        ),
        (
            "    public double getErrorRate() {",
            "    /** 返回期望误判率。 */\n    public double getErrorRate() {",
        ),
        (
            "    public long getCapacity() {",
            "    /** 返回设计容量。 */\n    public long getCapacity() {",
        ),
        (
            "    public Long getExpansionRate() {",
            "    /** 返回扩展倍率，未设置时为 null。 */\n    public Long getExpansionRate() {",
        ),
        (
            "    public Boolean isNonScaling() {",
            "    /** 返回是否启用非缩放模式。 */\n    public Boolean isNonScaling() {",
        ),
    ],
    f"{_BF}BloomFilterInsertArgs.java": [
        (
            "/**\n * BloomFilterInsertArgs for BF.INSERT command\n *\n * @author Su Ko\n */",
            "/**\n * {@code BF.INSERT} 命令插入参数构建器入口；以待插入元素集合开始链式配置。\n *\n * @author Su Ko\n * @param <V> 元素类型\n */",
        ),
        (
            "    /**\n     * Defines BloomFilter to BF.INSERT command\n     *\n     * @return ErrorRateBloomFilterInitArgs\n     */",
            "    /**\n     * 以给定元素集合创建 {@code BF.INSERT} 参数构建器。\n     *\n     * @param elements 待插入元素集合\n     * @return 可继续配置容量、误判率等选项的构建器\n     */",
        ),
    ],
    "BloomFilterInsertArgs.java": [
        (
            "/**\n * BloomFilterInsertArgs for BF.INSERT command\n *\n * @author Su Ko\n */",
            "/**\n * {@code BF.INSERT} 命令插入参数构建器入口；以待插入元素集合开始链式配置。\n *\n * @author Su Ko\n * @param <V> 元素类型\n */",
        ),
        (
            "    /**\n     * Defines BloomFilter to BF.INSERT command\n     *\n     * @return ErrorRateBloomFilterInitArgs\n     */",
            "    /**\n     * 以给定元素集合创建 {@code BF.INSERT} 参数构建器。\n     *\n     * @param elements 待插入元素集合\n     * @return 可继续配置容量、误判率等选项的构建器\n     */",
        ),
    ],
    f"{_BF}BloomFilterInsertParams.java": [
        (
            "/**\n * BloomFilter Params for BF.INSERT command\n *\n * @author Su Ko\n *\n */",
            "/**\n * {@code BF.INSERT} 命令参数的默认实现，封装待插入元素及可选容量、误判率、扩展等配置。\n *\n * @author Su Ko\n * @param <V> 元素类型\n */",
        ),
        (
            "    private final Collection<V> elements;\n\n    private Double errorRate;\n    private Long capacity;\n    private Long expansionRate;\n    private Boolean nonScaling;\n    private Boolean noCreate;",
            "    /** 待插入的元素集合。 */\n    private final Collection<V> elements;\n\n    /** 期望误判率，可选。 */\n    private Double errorRate;\n    /** 设计容量，可选。 */\n    private Long capacity;\n    /** 扩展倍率，与 nonScaling 互斥。 */\n    private Long expansionRate;\n    /** 是否禁止创建新子过滤器。 */\n    private Boolean nonScaling;\n    /** 过滤器不存在时是否跳过创建。 */\n    private Boolean noCreate;",
        ),
        (
            "    public BloomFilterInsertParams(Collection<V> elements) {",
            "    /** 以给定元素集合构造插入参数。 */\n    public BloomFilterInsertParams(Collection<V> elements) {",
        ),
        (
            "    public Collection<V> getElements() {",
            "    /** 返回待插入元素集合。 */\n    public Collection<V> getElements() {",
        ),
        (
            "    public Double getErrorRate() {",
            "    /** 返回期望误判率。 */\n    public Double getErrorRate() {",
        ),
        (
            "    public Long getCapacity() {",
            "    /** 返回设计容量。 */\n    public Long getCapacity() {",
        ),
        (
            "    public Long getExpansionRate() {",
            "    /** 返回扩展倍率。 */\n    public Long getExpansionRate() {",
        ),
        (
            "    public Boolean isNoCreate() {",
            "    /** 返回 noCreate 选项。 */\n    public Boolean isNoCreate() {",
        ),
        (
            "    public Boolean isNonScaling() {",
            "    /** 返回 nonScaling 选项。 */\n    public Boolean isNonScaling() {",
        ),
    ],
    "BloomFilterInsertParams.java": [
        (
            "/**\n * BloomFilter Params for BF.INSERT command\n *\n * @author Su Ko\n *\n */",
            "/**\n * {@code BF.INSERT} 命令参数的默认实现，封装待插入元素及可选容量、误判率、扩展等配置。\n *\n * @author Su Ko\n * @param <V> 元素类型\n */",
        ),
        (
            "    private final Collection<V> elements;\n\n    private Double errorRate;\n    private Long capacity;\n    private Long expansionRate;\n    private Boolean nonScaling;\n    private Boolean noCreate;",
            "    /** 待插入的元素集合。 */\n    private final Collection<V> elements;\n\n    /** 期望误判率，可选。 */\n    private Double errorRate;\n    /** 设计容量，可选。 */\n    private Long capacity;\n    /** 扩展倍率，与 nonScaling 互斥。 */\n    private Long expansionRate;\n    /** 是否禁止创建新子过滤器。 */\n    private Boolean nonScaling;\n    /** 过滤器不存在时是否跳过创建。 */\n    private Boolean noCreate;",
        ),
        (
            "    public BloomFilterInsertParams(Collection<V> elements) {",
            "    /** 以给定元素集合构造插入参数。 */\n    public BloomFilterInsertParams(Collection<V> elements) {",
        ),
        (
            "    public Collection<V> getElements() {",
            "    /** 返回待插入元素集合。 */\n    public Collection<V> getElements() {",
        ),
        (
            "    public Double getErrorRate() {",
            "    /** 返回期望误判率。 */\n    public Double getErrorRate() {",
        ),
        (
            "    public Long getCapacity() {",
            "    /** 返回设计容量。 */\n    public Long getCapacity() {",
        ),
        (
            "    public Long getExpansionRate() {",
            "    /** 返回扩展倍率。 */\n    public Long getExpansionRate() {",
        ),
        (
            "    public Boolean isNoCreate() {",
            "    /** 返回 noCreate 选项。 */\n    public Boolean isNoCreate() {",
        ),
        (
            "    public Boolean isNonScaling() {",
            "    /** 返回 nonScaling 选项。 */\n    public Boolean isNonScaling() {",
        ),
    ],
    f"{_BF}BloomFilterScanDumpInfo.java": [
        (
            "/**\n * BloomFilter ScanDump Info for BF.SCANDUMP\n * if returned iterator is 0 and data is empty, iteration is complete\n *\n * @author Su Ko\n *\n */",
            "/**\n * {@code BF.SCANDUMP} 迭代导出的一页结果；包含游标与二进制数据块。\n * 当返回的 iterator 为 0 且 data 为空时表示迭代完成。\n *\n * @author Su Ko\n */",
        ),
        (
            "    public long getIterator() {",
            "    /** 返回下次 SCANDUMP 应使用的游标值。 */\n    public long getIterator() {",
        ),
        (
            "    public byte[] getData() {",
            "    /** 返回本页导出的二进制数据。 */\n    public byte[] getData() {",
        ),
    ],
    "BloomFilterScanDumpInfo.java": [
        (
            "/**\n * BloomFilter ScanDump Info for BF.SCANDUMP\n * if returned iterator is 0 and data is empty, iteration is complete\n *\n * @author Su Ko\n *\n */",
            "/**\n * {@code BF.SCANDUMP} 迭代导出的一页结果；包含游标与二进制数据块。\n * 当返回的 iterator 为 0 且 data 为空时表示迭代完成。\n *\n * @author Su Ko\n */",
        ),
        (
            "    public long getIterator() {",
            "    /** 返回下次 SCANDUMP 应使用的游标值。 */\n    public long getIterator() {",
        ),
        (
            "    public byte[] getData() {",
            "    /** 返回本页导出的二进制数据。 */\n    public byte[] getData() {",
        ),
    ],
    f"{_BF}CapacityBloomFilterInitArgs.java": [
        (
            "/**\n * CapacityBloomFilterInitArgs for BF.RESERVE command\n *\n * @author Su Ko\n */",
            "/**\n * {@code BF.RESERVE} 构建链中的容量配置阶段；设置预期插入元素数。\n *\n * @author Su Ko\n */",
        ),
        (
            "    /**\n     * Defines BloomFilter to BF.RESERVE command\n     *\n     * @param capacity is number of entries intended to be added to the filter. must be greater than 0\n     * When the number of entries reaches capacity, a new subfilter is created.\n     *\n     * @return OptionalBloomFilterInitArgs\n     */",
            "    /**\n     * 设置过滤器设计容量（预期插入元素数，须大于 0）。\n     * 达到容量后将创建新的子过滤器（除非启用 nonScaling）。\n     *\n     * @param capacity 预期插入元素数\n     * @return 可选扩展参数配置阶段\n     */",
        ),
    ],
    "CapacityBloomFilterInitArgs.java": [
        (
            "/**\n * CapacityBloomFilterInitArgs for BF.RESERVE command\n *\n * @author Su Ko\n */",
            "/**\n * {@code BF.RESERVE} 构建链中的容量配置阶段；设置预期插入元素数。\n *\n * @author Su Ko\n */",
        ),
        (
            "    /**\n     * Defines BloomFilter to BF.RESERVE command\n     *\n     * @param capacity is number of entries intended to be added to the filter. must be greater than 0\n     * When the number of entries reaches capacity, a new subfilter is created.\n     *\n     * @return OptionalBloomFilterInitArgs\n     */",
            "    /**\n     * 设置过滤器设计容量（预期插入元素数，须大于 0）。\n     * 达到容量后将创建新的子过滤器（除非启用 nonScaling）。\n     *\n     * @param capacity 预期插入元素数\n     * @return 可选扩展参数配置阶段\n     */",
        ),
    ],
    f"{_BF}ErrorRateBloomFilterInitArgs.java": [
        (
            "/**\n * ErrorRateBloomFilterInitArgs for BF.RESERVE command\n *\n * @author Su Ko\n */",
            "/**\n * {@code BF.RESERVE} 构建链中的误判率配置阶段；设置期望假阳性概率。\n *\n * @author Su Ko\n */",
        ),
        (
            "    /**\n     * Defines BloomFilter to BF.RESERVE command\n     *\n     * @param errorRate is desired probability for false positives. must be greater than 0 and less than 1\n     *\n     * @return CapacityBloomFilterInitArgs\n     */",
            "    /**\n     * 设置期望误判率（假阳性概率，须大于 0 且小于 1）。\n     *\n     * @param errorRate 期望误判率\n     * @return 容量配置阶段\n     */",
        ),
    ],
    "ErrorRateBloomFilterInitArgs.java": [
        (
            "/**\n * ErrorRateBloomFilterInitArgs for BF.RESERVE command\n *\n * @author Su Ko\n */",
            "/**\n * {@code BF.RESERVE} 构建链中的误判率配置阶段；设置期望假阳性概率。\n *\n * @author Su Ko\n */",
        ),
        (
            "    /**\n     * Defines BloomFilter to BF.RESERVE command\n     *\n     * @param errorRate is desired probability for false positives. must be greater than 0 and less than 1\n     *\n     * @return CapacityBloomFilterInitArgs\n     */",
            "    /**\n     * 设置期望误判率（假阳性概率，须大于 0 且小于 1）。\n     *\n     * @param errorRate 期望误判率\n     * @return 容量配置阶段\n     */",
        ),
    ],
    f"{_BF}OptionalBloomFilterInitArgs.java": [
        (
            "/**\n * OptionalBloomFilterInitArgs for BF.RESERVE command\n *\n * @author Su Ko\n */",
            "/**\n * {@code BF.RESERVE} 构建链中的可选参数阶段；可配置扩展倍率或非缩放模式。\n *\n * @author Su Ko\n */",
        ),
        (
            "    /**\n     * Defines BloomFilter to BF.RESERVE command\n     *\n     * @param expansionRate is the value that is multiplied by the size of the last subfilter when a new subfilter is created when capacity is reached.\n     * expansionRate and nonScaling are mutually exclusive\n     *\n     * @return OptionalBloomFilterInitArgs\n     */",
            "    /**\n     * 设置扩展倍率：达到容量创建新子过滤器时，新子过滤器大小为上一子过滤器大小乘以该值。\n     * 与 {@link #nonScaling(boolean)} 互斥。\n     *\n     * @param expansionRate 扩展倍率\n     * @return 当前构建器\n     */",
        ),
        (
            "    /**\n     * Defines BloomFilter to BF.RESERVE command\n     *\n     * @param nonScaling is option that prevents subfliters from being created even when fcapacity is reached.\n     * expansionRate and nonScaling are mutually exclusive\n     *\n     * @return OptionalBloomFilterInitArgs\n     */",
            "    /**\n     * 启用非缩放模式：达到容量时不创建新子过滤器。\n     * 与 {@link #expansionRate(long)} 互斥。\n     *\n     * @param nonScaling 是否禁止扩展\n     * @return 当前构建器\n     */",
        ),
    ],
    "OptionalBloomFilterInitArgs.java": [
        (
            "/**\n * OptionalBloomFilterInitArgs for BF.RESERVE command\n *\n * @author Su Ko\n */",
            "/**\n * {@code BF.RESERVE} 构建链中的可选参数阶段；可配置扩展倍率或非缩放模式。\n *\n * @author Su Ko\n */",
        ),
        (
            "    /**\n     * Defines BloomFilter to BF.RESERVE command\n     *\n     * @param expansionRate is the value that is multiplied by the size of the last subfilter when a new subfilter is created when capacity is reached.\n     * expansionRate and nonScaling are mutually exclusive\n     *\n     * @return OptionalBloomFilterInitArgs\n     */",
            "    /**\n     * 设置扩展倍率：达到容量创建新子过滤器时，新子过滤器大小为上一子过滤器大小乘以该值。\n     * 与 {@link #nonScaling(boolean)} 互斥。\n     *\n     * @param expansionRate 扩展倍率\n     * @return 当前构建器\n     */",
        ),
        (
            "    /**\n     * Defines BloomFilter to BF.RESERVE command\n     *\n     * @param nonScaling is option that prevents subfliters from being created even when fcapacity is reached.\n     * expansionRate and nonScaling are mutually exclusive\n     *\n     * @return OptionalBloomFilterInitArgs\n     */",
            "    /**\n     * 启用非缩放模式：达到容量时不创建新子过滤器。\n     * 与 {@link #expansionRate(long)} 互斥。\n     *\n     * @param nonScaling 是否禁止扩展\n     * @return 当前构建器\n     */",
        ),
    ],
    f"{_BF}OptionalBloomFilterInsertArgs.java": [
        (
            "/**\n * OptionalBloomFilterInsertArgs for BF.INSERT command\n *\n * @author Su Ko\n */",
            "/**\n * {@code BF.INSERT} 构建链中的可选参数阶段；可配置容量、误判率、扩展及 noCreate 等选项。\n *\n * @author Su Ko\n * @param <V> 元素类型\n */",
        ),
        (
            "    /**\n     * Defines BloomFilter to BF.INSERT command\n     *\n     * @param capacity is number of entries intended to be added to the filter. must be greater than 0\n     * When the number of entries reaches capacity, a new subfilter is created.\n     *\n     * @return OptionalBloomFilterInsertArgs\n     */",
            "    /**\n     * 设置设计容量（预期插入元素数，须大于 0）；达到容量时创建新子过滤器。\n     *\n     * @param capacity 预期插入元素数\n     * @return 当前构建器\n     */",
        ),
        (
            "    /**\n     * Defines BloomFilter to BF.INSERT command\n     *\n     * @param errorRate is desired probability for false positives. must be greater than 0 and less than 1\n     *\n     * @return OptionalBloomFilterInsertArgs\n     */",
            "    /**\n     * 设置期望误判率（须大于 0 且小于 1）。\n     *\n     * @param errorRate 期望误判率\n     * @return 当前构建器\n     */",
        ),
        (
            "    /**\n     * Defines BloomFilter to BF.INSERT command\n     *\n     * @param expansionRate is the value that is multiplied by the size of the last subfilter when a new subfilter is created when capacity is reached.\n     * expansionRate and nonScaling are mutually exclusive\n     *\n     * @return OptionalBloomFilterInsertArgs\n     */",
            "    /**\n     * 设置扩展倍率；与 {@link #nonScaling(boolean)} 互斥。\n     *\n     * @param expansionRate 扩展倍率\n     * @return 当前构建器\n     */",
        ),
        (
            "    /**\n     * Defines BloomFilter to BF.INSERT command\n     *\n     * @param nonScaling is option that prevents subfliters from being created even when fcapacity is reached.\n     * expansionRate and nonScaling are mutually exclusive\n     *\n     * @return OptionalBloomFilterInsertArgs\n     */",
            "    /**\n     * 启用非缩放模式；与 {@link #expansionRate(long)} 互斥。\n     *\n     * @param nonScaling 是否禁止扩展\n     * @return 当前构建器\n     */",
        ),
        (
            "    /**\n     * Defines BloomFilter to BF.INSERT command\n     *\n     * @param noCreate is option that prevents subfliters from being created even when fcapacity is reached.\n     * expansionRate and nonScaling are mutually exclusive\n     *\n     * @return OptionalBloomFilterInsertArgs\n     */",
            "    /**\n     * 设置 noCreate：过滤器不存在时不自动创建。\n     *\n     * @param noCreate 是否跳过自动创建\n     * @return 当前构建器\n     */",
        ),
    ],
    "OptionalBloomFilterInsertArgs.java": [
        (
            "/**\n * OptionalBloomFilterInsertArgs for BF.INSERT command\n *\n * @author Su Ko\n */",
            "/**\n * {@code BF.INSERT} 构建链中的可选参数阶段；可配置容量、误判率、扩展及 noCreate 等选项。\n *\n * @author Su Ko\n * @param <V> 元素类型\n */",
        ),
        (
            "    /**\n     * Defines BloomFilter to BF.INSERT command\n     *\n     * @param capacity is number of entries intended to be added to the filter. must be greater than 0\n     * When the number of entries reaches capacity, a new subfilter is created.\n     *\n     * @return OptionalBloomFilterInsertArgs\n     */",
            "    /**\n     * 设置设计容量（预期插入元素数，须大于 0）；达到容量时创建新子过滤器。\n     *\n     * @param capacity 预期插入元素数\n     * @return 当前构建器\n     */",
        ),
        (
            "    /**\n     * Defines BloomFilter to BF.INSERT command\n     *\n     * @param errorRate is desired probability for false positives. must be greater than 0 and less than 1\n     *\n     * @return OptionalBloomFilterInsertArgs\n     */",
            "    /**\n     * 设置期望误判率（须大于 0 且小于 1）。\n     *\n     * @param errorRate 期望误判率\n     * @return 当前构建器\n     */",
        ),
        (
            "    /**\n     * Defines BloomFilter to BF.INSERT command\n     *\n     * @param expansionRate is the value that is multiplied by the size of the last subfilter when a new subfilter is created when capacity is reached.\n     * expansionRate and nonScaling are mutually exclusive\n     *\n     * @return OptionalBloomFilterInsertArgs\n     */",
            "    /**\n     * 设置扩展倍率；与 {@link #nonScaling(boolean)} 互斥。\n     *\n     * @param expansionRate 扩展倍率\n     * @return 当前构建器\n     */",
        ),
        (
            "    /**\n     * Defines BloomFilter to BF.INSERT command\n     *\n     * @param nonScaling is option that prevents subfliters from being created even when fcapacity is reached.\n     * expansionRate and nonScaling are mutually exclusive\n     *\n     * @return OptionalBloomFilterInsertArgs\n     */",
            "    /**\n     * 启用非缩放模式；与 {@link #expansionRate(long)} 互斥。\n     *\n     * @param nonScaling 是否禁止扩展\n     * @return 当前构建器\n     */",
        ),
        (
            "    /**\n     * Defines BloomFilter to BF.INSERT command\n     *\n     * @param noCreate is option that prevents subfliters from being created even when fcapacity is reached.\n     * expansionRate and nonScaling are mutually exclusive\n     *\n     * @return OptionalBloomFilterInsertArgs\n     */",
            "    /**\n     * 设置 noCreate：过滤器不存在时不自动创建。\n     *\n     * @param noCreate 是否跳过自动创建\n     * @return 当前构建器\n     */",
        ),
    ],
}
