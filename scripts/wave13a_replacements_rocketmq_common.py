"""RocketMQ 5.5.0 wave13a common attribute/chain/compression [0:15] replacements."""

R: dict[str, list[tuple[str, str]]] = {}

R["common/src/main/java/org/apache/rocketmq/common/attribute/AttributeParser.java"] = [
    (
        "public class AttributeParser {",
        "/**\n * Topic 属性修改串解析器：支持 {@code +key=value} 增改、{@code -key} 删除，\n * 键值对以逗号分隔，例如 {@code +key1=value1,+key2=value2,-key3}。\n */\npublic class AttributeParser {",
    ),
    (
        "    public static final String ATTR_ARRAY_SEPARATOR_COMMA = \",\";",
        "    /** 属性键值对之间的分隔符。 */\n    public static final String ATTR_ARRAY_SEPARATOR_COMMA = \",\";",
    ),
    (
        "    public static final String ATTR_KEY_VALUE_EQUAL_SIGN = \"=\";",
        "    /** 键与值之间的等号分隔符。 */\n    public static final String ATTR_KEY_VALUE_EQUAL_SIGN = \"=\";",
    ),
    (
        "    public static final String ATTR_ADD_PLUS_SIGN = \"+\";",
        "    /** 新增或修改属性时的前缀符号。 */\n    public static final String ATTR_ADD_PLUS_SIGN = \"+\";",
    ),
    (
        "    private static final String ATTR_DELETE_MINUS_SIGN = \"-\";",
        "    /** 删除属性时的前缀符号。 */\n    private static final String ATTR_DELETE_MINUS_SIGN = \"-\";",
    ),
    (
        "    public static Map<String, String> parseToMap(String attributesModification) {",
        "    /**\n     * 将属性修改串解析为键值 Map。\n     *\n     * @param attributesModification 属性修改串，空串返回空 Map\n     * @return 解析后的属性 Map\n     */\n    public static Map<String, String> parseToMap(String attributesModification) {",
    ),
    (
        "        // format: +key1=value1,+key2=value2,-key3,+key4=value4",
        "        // 格式：+key1=value1,+key2=value2,-key3,+key4=value4",
    ),
    (
        "    public static String parseToString(Map<String, String> attributes) {",
        "    /**\n     * 将属性 Map 序列化为修改串。\n     *\n     * @param attributes 属性 Map，null 或空 Map 返回空串\n     * @return 逗号分隔的属性修改串\n     */\n    public static String parseToString(Map<String, String> attributes) {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/attribute/AttributeUtil.java"] = [
    (
        "public class AttributeUtil {",
        "/**\n * Topic 属性变更工具：解析 {@code +}/{@code -} 前缀的增量修改，\n * 校验属性定义、可变性及取值合法性，合并得到最终属性 Map。\n */\npublic class AttributeUtil {",
    ),
    (
        "    private static final Logger log = LoggerFactory.getLogger(LoggerName.COMMON_LOGGER_NAME);",
        "    /** 公共模块日志。 */\n    private static final Logger log = LoggerFactory.getLogger(LoggerName.COMMON_LOGGER_NAME);",
    ),
    (
        "    public static Map<String, String> alterCurrentAttributes(boolean create, Map<String, Attribute> all,\n        ImmutableMap<String, String> currentAttributes, ImmutableMap<String, String> newAttributes) {",
        "    /**\n     * 根据增量修改计算最终属性 Map。\n     *\n     * @param create 是否为创建 Topic（仅允许 {@code +key}）\n     * @param all 全部已注册属性定义\n     * @param currentAttributes 当前属性快照\n     * @param newAttributes 待应用的增量修改（键含 {@code +}/{@code -} 前缀）\n     * @return 合并后的新属性 Map\n     */\n    public static Map<String, String> alterCurrentAttributes(boolean create, Map<String, Attribute> all,\n        ImmutableMap<String, String> currentAttributes, ImmutableMap<String, String> newAttributes) {",
    ),
    (
        "    private static void duplicationCheck(Set<String> keys, String key) {",
        "    /** 同一批次修改中禁止重复键。 */\n    private static void duplicationCheck(Set<String> keys, String key) {",
    ),
    (
        "    private static void validate(String kvAttribute) {",
        "    /** 校验去掉前缀后的真实键名非空且不含 {@code +}/{@code -}。 */\n    private static void validate(String kvAttribute) {",
    ),
    (
        "    private static void validateAlter(Map<String, Attribute> all, Map<String, String> alter, boolean init, boolean delete) {",
        "    /** 校验变更项：键须已注册、非 init 时须可变更、非删除时须通过 {@link Attribute#verify}。 */\n    private static void validateAlter(Map<String, Attribute> all, Map<String, String> alter, boolean init, boolean delete) {",
    ),
    (
        "    private static String realKey(String key) {",
        "    /** 去掉 {@code +}/{@code -} 前缀得到真实属性键。 */\n    private static String realKey(String key) {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/attribute/BooleanAttribute.java"] = [
    (
        "public class BooleanAttribute extends Attribute {",
        "/**\n * 布尔型 Topic 属性：取值须为 {@code true} 或 {@code false}（忽略大小写）。\n */\npublic class BooleanAttribute extends Attribute {",
    ),
    (
        "    private final boolean defaultValue;",
        "    /** 属性默认值。 */\n    private final boolean defaultValue;",
    ),
    (
        "    public BooleanAttribute(String name, boolean changeable, boolean defaultValue) {",
        "    /**\n     * @param name 属性名\n     * @param changeable 创建后是否可修改\n     * @param defaultValue 默认值\n     */\n    public BooleanAttribute(String name, boolean changeable, boolean defaultValue) {",
    ),
    (
        "    @Override\n    public void verify(String value) {",
        "    /** 校验取值是否为 true/false。 */\n    @Override\n    public void verify(String value) {",
    ),
    (
        "    public boolean getDefaultValue() {",
        "    /** 返回默认值。 */\n    public boolean getDefaultValue() {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/attribute/CQType.java"] = [
    (
        "public enum CQType {",
        "/**\n * ConsumeQueue 实现类型。\n */\npublic enum CQType {",
    ),
    (
        "    SimpleCQ,",
        "    /** 简单 ConsumeQueue。 */\n    SimpleCQ,",
    ),
    (
        "    BatchCQ,",
        "    /** 批量 ConsumeQueue。 */\n    BatchCQ,",
    ),
    (
        "    RocksDBCQ",
        "    /** 基于 RocksDB 的 ConsumeQueue。 */\n    RocksDBCQ",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/attribute/CleanupPolicy.java"] = [
    (
        "public enum CleanupPolicy {",
        "/**\n * Topic 消息清理策略。\n */\npublic enum CleanupPolicy {",
    ),
    (
        "    DELETE,",
        "    /** 按保留策略直接删除过期消息。 */\n    DELETE,",
    ),
    (
        "    COMPACTION",
        "    /** 日志压缩（Compaction）策略。 */\n    COMPACTION",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/attribute/EnumAttribute.java"] = [
    (
        "public class EnumAttribute extends Attribute {",
        "/**\n * 枚举型 Topic 属性：取值须落在预定义的 {@link #universe} 集合内。\n */\npublic class EnumAttribute extends Attribute {",
    ),
    (
        "    private final Set<String> universe;",
        "    /** 合法取值集合。 */\n    private final Set<String> universe;",
    ),
    (
        "    private final String defaultValue;",
        "    /** 属性默认值。 */\n    private final String defaultValue;",
    ),
    (
        "    public EnumAttribute(String name, boolean changeable, Set<String> universe, String defaultValue) {",
        "    /**\n     * @param name 属性名\n     * @param changeable 创建后是否可修改\n     * @param universe 合法取值集合\n     * @param defaultValue 默认值\n     */\n    public EnumAttribute(String name, boolean changeable, Set<String> universe, String defaultValue) {",
    ),
    (
        "    @Override\n    public void verify(String value) {",
        "    /** 校验取值是否在 universe 集合内。 */\n    @Override\n    public void verify(String value) {",
    ),
    (
        "    public String getDefaultValue() {",
        "    /** 返回默认值。 */\n    public String getDefaultValue() {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/attribute/LiteSubModel.java"] = [
    (
        "public enum LiteSubModel {",
        "/**\n * Lite 订阅模型：共享或独占消费。\n */\npublic enum LiteSubModel {",
    ),
    (
        "    Shared,",
        "    /** 共享订阅：多消费者分摊消息。 */\n    Shared,",
    ),
    (
        "    Exclusive",
        "    /** 独占订阅：单消费者独占队列。 */\n    Exclusive",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/attribute/LongRangeAttribute.java"] = [
    (
        "public class LongRangeAttribute extends Attribute {",
        "/**\n * 长整型范围 Topic 属性：取值须在 {@code [min, max]} 闭区间内。\n */\npublic class LongRangeAttribute extends Attribute {",
    ),
    (
        "    private final long min;",
        "    /** 允许的最小值（含）。 */\n    private final long min;",
    ),
    (
        "    private final long max;",
        "    /** 允许的最大值（含）。 */\n    private final long max;",
    ),
    (
        "    private final long defaultValue;",
        "    /** 属性默认值。 */\n    private final long defaultValue;",
    ),
    (
        "    public LongRangeAttribute(String name, boolean changeable, long min, long max, long defaultValue) {",
        "    /**\n     * @param name 属性名\n     * @param changeable 创建后是否可修改\n     * @param min 最小值\n     * @param max 最大值\n     * @param defaultValue 默认值\n     */\n    public LongRangeAttribute(String name, boolean changeable, long min, long max, long defaultValue) {",
    ),
    (
        "    @Override\n    public void verify(String value) {",
        "    /** 解析为 long 并校验是否在 [min, max] 范围内。 */\n    @Override\n    public void verify(String value) {",
    ),
    (
        "    public long getDefaultValue() {",
        "    /** 返回默认值。 */\n    public long getDefaultValue() {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/attribute/StringAttribute.java"] = [
    (
        "public class StringAttribute extends Attribute {",
        "/**\n * 字符串型 Topic 属性：仅要求非 null。\n */\npublic class StringAttribute extends Attribute {",
    ),
    (
        "    public StringAttribute(String name, boolean changeable) {",
        "    /**\n     * @param name 属性名\n     * @param changeable 创建后是否可修改\n     */\n    public StringAttribute(String name, boolean changeable) {",
    ),
    (
        "    @Override\n    public void verify(String value) {",
        "    /** 校验取值非 null。 */\n    @Override\n    public void verify(String value) {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/attribute/TopicMessageType.java"] = [
    (
        "public enum TopicMessageType {",
        "/**\n * Topic 消息类型：用于区分普通、顺序、延迟、事务、优先级、Lite 等语义。\n */\npublic enum TopicMessageType {",
    ),
    (
        "    UNSPECIFIED(\"UNSPECIFIED\"),",
        "    /** 未指定类型。 */\n    UNSPECIFIED(\"UNSPECIFIED\"),",
    ),
    (
        "    NORMAL(\"NORMAL\"),",
        "    /** 普通消息。 */\n    NORMAL(\"NORMAL\"),",
    ),
    (
        "    FIFO(\"FIFO\"),",
        "    /** 顺序（FIFO）消息。 */\n    FIFO(\"FIFO\"),",
    ),
    (
        "    DELAY(\"DELAY\"),",
        "    /** 延迟/定时消息。 */\n    DELAY(\"DELAY\"),",
    ),
    (
        "    TRANSACTION(\"TRANSACTION\"),",
        "    /** 事务消息。 */\n    TRANSACTION(\"TRANSACTION\"),",
    ),
    (
        "    PRIORITY(\"PRIORITY\"),",
        "    /** 优先级消息。 */\n    PRIORITY(\"PRIORITY\"),",
    ),
    (
        "    LITE(\"LITE\"),",
        "    /** Lite Topic 消息。 */\n    LITE(\"LITE\"),",
    ),
    (
        "    MIXED(\"MIXED\");",
        "    /** 混合类型 Topic。 */\n    MIXED(\"MIXED\");",
    ),
    (
        "    private final String value;",
        "    /** 属性/协议中使用的字符串值。 */\n    private final String value;",
    ),
    (
        "    public static Set<String> topicMessageTypeSet() {",
        "    /** 返回全部消息类型的字符串值集合。 */\n    public static Set<String> topicMessageTypeSet() {",
    ),
    (
        "    public String getValue() {",
        "    /** 返回类型字符串值。 */\n    public String getValue() {",
    ),
    (
        "    public static TopicMessageType parseFromMessageProperty(Map<String, String> messageProperty) {",
        "    /**\n     * 根据消息 UserProperty 推断消息类型；解析顺序保证各类型互斥。\n     *\n     * @param messageProperty 消息属性 Map\n     * @return 推断出的 {@link TopicMessageType}\n     */\n    public static TopicMessageType parseFromMessageProperty(Map<String, String> messageProperty) {",
    ),
    (
        "        // the parse order keeps message types mutually exclusive",
        "        // 解析顺序保证各消息类型互斥",
    ),
    (
        "    public String getMetricsValue() {",
        "    /** 返回用于指标上报的小写类型名。 */\n    public String getMetricsValue() {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/chain/Handler.java"] = [
    (
        "public interface Handler<T, R> {",
        "/**\n * 责任链中的单个处理器。\n *\n * @param <T> 请求/上下文类型\n * @param <R> 处理结果类型\n */\npublic interface Handler<T, R> {",
    ),
    (
        "    R handle(T t, HandlerChain<T, R> chain);",
        "    /**\n     * 处理当前节点逻辑，并可选择调用 {@link HandlerChain#handle} 传递至下一节点。\n     *\n     * @param t 输入上下文\n     * @param chain 责任链实例\n     * @return 处理结果\n     */\n    R handle(T t, HandlerChain<T, R> chain);",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/chain/HandlerChain.java"] = [
    (
        "public class HandlerChain<T, R> {",
        "/**\n * 泛型责任链：按注册顺序依次调用 {@link Handler}，支持处理器内继续传递。\n *\n * @param <T> 请求/上下文类型\n * @param <R> 处理结果类型\n */\npublic class HandlerChain<T, R> {",
    ),
    (
        "    private List<Handler<T, R>> handlers;",
        "    /** 已注册的处理器列表。 */\n    private List<Handler<T, R>> handlers;",
    ),
    (
        "    private Iterator<Handler<T, R>> iterator;",
        "    /** 当前链路的迭代器（单次 handle 调用内复用）。 */\n    private Iterator<Handler<T, R>> iterator;",
    ),
    (
        "    public static <T, R> HandlerChain<T, R> create() {",
        "    /** 创建空责任链。 */\n    public static <T, R> HandlerChain<T, R> create() {",
    ),
    (
        "    public HandlerChain<T, R> addNext(Handler<T, R> handler) {",
        "    /** 追加处理器并返回自身（链式调用）。 */\n    public HandlerChain<T, R> addNext(Handler<T, R> handler) {",
    ),
    (
        "    public R handle(T t) {",
        "    /**\n     * 驱动责任链：取下一个处理器执行，无剩余处理器时返回 null。\n     *\n     * @param t 输入上下文\n     * @return 处理结果，链耗尽时为 null\n     */\n    public R handle(T t) {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/coldctr/AccAndTimeStamp.java"] = [
    (
        "public class AccAndTimeStamp {",
        "/**\n * 冷读控制计数与时间戳：记录冷数据累计访问量及最近冷读、创建时间。\n */\npublic class AccAndTimeStamp {",
    ),
    (
        "    public AtomicLong coldAcc = new AtomicLong(0L);",
        "    /** 冷读累计计数（原子累加）。 */\n    public AtomicLong coldAcc = new AtomicLong(0L);",
    ),
    (
        "    public Long lastColdReadTimeMills = System.currentTimeMillis();",
        "    /** 最近一次冷读时间戳（毫秒）。 */\n    public Long lastColdReadTimeMills = System.currentTimeMillis();",
    ),
    (
        "    public Long createTimeMills = System.currentTimeMillis();",
        "    /** 记录创建时间戳（毫秒）。 */\n    public Long createTimeMills = System.currentTimeMillis();",
    ),
    (
        "    public AccAndTimeStamp(AtomicLong coldAcc) {",
        "    /** 使用外部传入的冷读计数器构造。 */\n    public AccAndTimeStamp(AtomicLong coldAcc) {",
    ),
    (
        "    public AtomicLong getColdAcc() {",
        "    /** 获取冷读累计计数。 */\n    public AtomicLong getColdAcc() {",
    ),
    (
        "    public void setColdAcc(AtomicLong coldAcc) {",
        "    /** 设置冷读累计计数。 */\n    public void setColdAcc(AtomicLong coldAcc) {",
    ),
    (
        "    public Long getLastColdReadTimeMills() {",
        "    /** 获取最近冷读时间戳。 */\n    public Long getLastColdReadTimeMills() {",
    ),
    (
        "    public void setLastColdReadTimeMills(Long lastColdReadTimeMills) {",
        "    /** 设置最近冷读时间戳。 */\n    public void setLastColdReadTimeMills(Long lastColdReadTimeMills) {",
    ),
    (
        "    public Long getCreateTimeMills() {",
        "    /** 获取创建时间戳。 */\n    public Long getCreateTimeMills() {",
    ),
    (
        "    public void setCreateTimeMills(Long createTimeMills) {",
        "    /** 设置创建时间戳。 */\n    public void setCreateTimeMills(Long createTimeMills) {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/compression/CompressionType.java"] = [
    (
        "public enum CompressionType {",
        "/**\n * 消息体压缩算法类型，编码值写入 {@link MessageSysFlag}。\n */\npublic enum CompressionType {",
    ),
    (
        "    /**\n     *    Compression types number can be extended to seven {@link MessageSysFlag}\n     *\n     *    Benchmarks from https://github.com/facebook/zstd\n     *\n     *    |   Compressor   |  Ratio  | Compression | Decompress |\n     *    |----------------|---------|-------------|------------|\n     *    |   zstd 1.5.1   |  2.887  |   530 MB/s  |  1700 MB/s |\n     *    |  zlib 1.2.11   |  2.743  |    95 MB/s  |   400 MB/s |\n     *    |    lz4 1.9.3   |  2.101  |   740 MB/s  |  4500 MB/s |\n     *\n     */",
        "    /**\n     * 压缩类型编号最多可扩展至 7 种（见 {@link MessageSysFlag}）。\n     *\n     * 基准测试参考 https://github.com/facebook/zstd\n     *\n     *    |   Compressor   |  Ratio  | Compression | Decompress |\n     *    |----------------|---------|-------------|------------|\n     *    |   zstd 1.5.1   |  2.887  |   530 MB/s  |  1700 MB/s |\n     *    |  zlib 1.2.11   |  2.743  |    95 MB/s  |   400 MB/s |\n     *    |    lz4 1.9.3   |  2.101  |   740 MB/s  |  4500 MB/s |\n     */",
    ),
    (
        "    LZ4(1),",
        "    /** LZ4 压缩（值 1）。 */\n    LZ4(1),",
    ),
    (
        "    ZSTD(2),",
        "    /** Zstandard 压缩（值 2）。 */\n    ZSTD(2),",
    ),
    (
        "    ZLIB(3);",
        "    /** Zlib 压缩（值 3）。 */\n    ZLIB(3);",
    ),
    (
        "    private final int value;",
        "    /** 协议/存储中使用的整型编码。 */\n    private final int value;",
    ),
    (
        "    public int getValue() {",
        "    /** 返回整型编码值。 */\n    public int getValue() {",
    ),
    (
        "    public static CompressionType of(String name) {",
        "    /**\n     * 按名称解析压缩类型（忽略大小写与首尾空白）。\n     *\n     * @param name 算法名称（LZ4/ZSTD/ZLIB）\n     * @return 对应 {@link CompressionType}\n     */\n    public static CompressionType of(String name) {",
    ),
    (
        "    public static CompressionType findByValue(int value) {",
        "    /**\n     * 按整型编码查找压缩类型。\n     *\n     * @param value 编码值（0 兼容旧版无类型，视为 ZLIB）\n     * @return 对应 {@link CompressionType}\n     */\n    public static CompressionType findByValue(int value) {",
    ),
    (
        "            case 0: // To be compatible for older versions without compression type",
        "            case 0: // 兼容旧版未携带压缩类型的消息",
    ),
    (
        "    public int getCompressionFlag() {",
        "    /** 返回写入 {@link MessageSysFlag} 的压缩类型标志位。 */\n    public int getCompressionFlag() {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/compression/Compressor.java"] = [
    (
        "public interface Compressor {",
        "/**\n * 消息体压缩/解压 SPI：各算法实现本接口供 Broker/Client 调用。\n */\npublic interface Compressor {",
    ),
    (
        "    /**\n     * Compress message by different compressor.\n     *\n     * @param src bytes ready to compress\n     * @param level compression level used to balance compression rate and time consumption\n     * @return compressed byte data\n     * @throws IOException\n     */",
        "    /**\n     * 压缩消息体。\n     *\n     * @param src 待压缩字节数组\n     * @param level 压缩级别，用于平衡压缩率与耗时\n     * @return 压缩后的字节数组\n     * @throws IOException 压缩 IO 异常\n     */",
    ),
    (
        "    /**\n     * Decompress message by different compressor.\n     *\n     * @param src bytes ready to decompress\n     * @return decompressed byte data\n     * @throws IOException\n     */",
        "    /**\n     * 解压消息体。\n     *\n     * @param src 待解压字节数组\n     * @return 解压后的字节数组\n     * @throws IOException 解压 IO 异常\n     */",
    ),
]
