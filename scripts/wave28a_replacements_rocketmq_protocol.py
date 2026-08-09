"""Chinese JavaDoc replacements for RocketMQ wave28a remoting protocol [0:15]."""

R: dict[str, list[tuple[str, str]]] = {
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/BrokerSyncInfo.java": [
        (
            "public class BrokerSyncInfo extends RemotingSerializable {",
            "/**\n * Broker 主从同步信息：Slave 上线前从 Master 获取 HA 地址、刷盘位点与服务地址。\n */\npublic class BrokerSyncInfo extends RemotingSerializable {",
        ),
        (
            "    /**\n     * For slave online sync, retrieve HA address before register\n     */",
            "    /** Slave 上线同步前获取的 Master HA 传输地址。 */",
        ),
        (
            "    private String masterHaAddress;",
            "    /** Master HA 监听地址。 */\n    private String masterHaAddress;",
        ),
        (
            "    private long masterFlushOffset;",
            "    /** Master 已刷盘 CommitLog 位点。 */\n    private long masterFlushOffset;",
        ),
        (
            "    private String masterAddress;",
            "    /** Master Broker 服务地址。 */\n    private String masterAddress;",
        ),
        (
            "    public BrokerSyncInfo(String masterHaAddress, long masterFlushOffset, String masterAddress) {",
            "    /** 构造主从同步三元组。 */\n    public BrokerSyncInfo(String masterHaAddress, long masterFlushOffset, String masterAddress) {",
        ),
        (
            "    public String getMasterHaAddress() {",
            "    /** 返回 Master HA 地址。 */\n    public String getMasterHaAddress() {",
        ),
        (
            "    public void setMasterHaAddress(String masterHaAddress) {",
            "    /** 设置 Master HA 地址。 */\n    public void setMasterHaAddress(String masterHaAddress) {",
        ),
        (
            "    public long getMasterFlushOffset() {",
            "    /** 返回 Master 刷盘位点。 */\n    public long getMasterFlushOffset() {",
        ),
        (
            "    public void setMasterFlushOffset(long masterFlushOffset) {",
            "    /** 设置 Master 刷盘位点。 */\n    public void setMasterFlushOffset(long masterFlushOffset) {",
        ),
        (
            "    public String getMasterAddress() {",
            "    /** 返回 Master 服务地址。 */\n    public String getMasterAddress() {",
        ),
        (
            "    public void setMasterAddress(String masterAddress) {",
            "    /** 设置 Master 服务地址。 */\n    public void setMasterAddress(String masterAddress) {",
        ),
        (
            "    @Override\n    public String toString() {",
            "    /** 返回便于日志排查的字符串表示。 */\n    @Override\n    public String toString() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/DataVersion.java": [
        (
            "public class DataVersion extends RemotingSerializable {",
            "/**\n * 元数据版本号：由状态版本、时间戳与递增计数器组成，用于检测配置变更。\n */\npublic class DataVersion extends RemotingSerializable {",
        ),
        (
            "    private long stateVersion = 0L;",
            "    /** 业务状态版本，通常对应 Controller 选举轮次等。 */\n    private long stateVersion = 0L;",
        ),
        (
            "    private long timestamp = System.currentTimeMillis();",
            "    /** 版本更新时间戳（毫秒）。 */\n    private long timestamp = System.currentTimeMillis();",
        ),
        (
            "    private AtomicLong counter = new AtomicLong(0);",
            "    /** 同 stateVersion 下的单调递增计数器。 */\n    private AtomicLong counter = new AtomicLong(0);",
        ),
        (
            "    public void assignNewOne(final DataVersion dataVersion) {",
            "    /** 从另一个 {@link DataVersion} 拷贝全部字段。 */\n    public void assignNewOne(final DataVersion dataVersion) {",
        ),
        (
            "    public void nextVersion() {",
            "    /** 递增版本，stateVersion 保持不变。 */\n    public void nextVersion() {",
        ),
        (
            "    public void nextVersion(long stateVersion) {",
            "    /** 指定新的 stateVersion 并递增计数器、刷新时间戳。 */\n    public void nextVersion(long stateVersion) {",
        ),
        (
            "    public long getStateVersion() {",
            "    /** 返回状态版本。 */\n    public long getStateVersion() {",
        ),
        (
            "    public void setStateVersion(long stateVersion) {",
            "    /** 设置状态版本。 */\n    public void setStateVersion(long stateVersion) {",
        ),
        (
            "    public long getTimestamp() {",
            "    /** 返回时间戳。 */\n    public long getTimestamp() {",
        ),
        (
            "    public void setTimestamp(long timestamp) {",
            "    /** 设置时间戳。 */\n    public void setTimestamp(long timestamp) {",
        ),
        (
            "    public AtomicLong getCounter() {",
            "    /** 返回计数器引用。 */\n    public AtomicLong getCounter() {",
        ),
        (
            "    public void setCounter(AtomicLong counter) {",
            "    /** 替换计数器实例。 */\n    public void setCounter(AtomicLong counter) {",
        ),
        (
            "    @Override\n    public boolean equals(final Object o) {",
            "    /** 按 stateVersion、timestamp 与 counter 值比较相等性。 */\n    @Override\n    public boolean equals(final Object o) {",
        ),
        (
            "    @Override\n    public int hashCode() {",
            "    /** 基于三字段计算哈希码。 */\n    @Override\n    public int hashCode() {",
        ),
        (
            "    @Override\n    public String toString() {",
            "    /** 返回 timestamp 与 counter 的摘要字符串。 */\n    @Override\n    public String toString() {",
        ),
        (
            "    public int compare(DataVersion dataVersion) {",
            "    /** 先比 stateVersion，再比 counter，最后比 timestamp；大者返回 1。 */\n    public int compare(DataVersion dataVersion) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/EpochEntry.java": [
        (
            "public class EpochEntry extends RemotingSerializable {",
            "/**\n * Controller 选举纪元条目：记录某一 epoch 在 CommitLog 上的起止偏移。\n */\npublic class EpochEntry extends RemotingSerializable {",
        ),
        (
            "    public static final long LAST_EPOCH_END_OFFSET = Long.MAX_VALUE;",
            "    /** 当前活跃 epoch 的 endOffset 哨兵值（表示尚未结束）。 */\n    public static final long LAST_EPOCH_END_OFFSET = Long.MAX_VALUE;",
        ),
        (
            "    private int epoch;",
            "    /** 选举纪元编号。 */\n    private int epoch;",
        ),
        (
            "    private long startOffset;",
            "    /** 该 epoch 覆盖的 CommitLog 起始偏移。 */\n    private long startOffset;",
        ),
        (
            "    private long endOffset = LAST_EPOCH_END_OFFSET;",
            "    /** 该 epoch 覆盖的 CommitLog 结束偏移，默认未结束。 */\n    private long endOffset = LAST_EPOCH_END_OFFSET;",
        ),
        (
            "    public EpochEntry(EpochEntry entry) {",
            "    /** 拷贝构造。 */\n    public EpochEntry(EpochEntry entry) {",
        ),
        (
            "    public EpochEntry(int epoch, long startOffset) {",
            "    /** 创建未指定结束偏移的 epoch 条目。 */\n    public EpochEntry(int epoch, long startOffset) {",
        ),
        (
            "    public EpochEntry(int epoch, long startOffset, long endOffset) {",
            "    /** 创建完整起止偏移的 epoch 条目。 */\n    public EpochEntry(int epoch, long startOffset, long endOffset) {",
        ),
        (
            "    public int getEpoch() {",
            "    /** 返回 epoch 编号。 */\n    public int getEpoch() {",
        ),
        (
            "    public void setEpoch(int epoch) {",
            "    /** 设置 epoch 编号。 */\n    public void setEpoch(int epoch) {",
        ),
        (
            "    public long getStartOffset() {",
            "    /** 返回起始偏移。 */\n    public long getStartOffset() {",
        ),
        (
            "    public void setStartOffset(long startOffset) {",
            "    /** 设置起始偏移。 */\n    public void setStartOffset(long startOffset) {",
        ),
        (
            "    public long getEndOffset() {",
            "    /** 返回结束偏移。 */\n    public long getEndOffset() {",
        ),
        (
            "    public void setEndOffset(long endOffset) {",
            "    /** 设置结束偏移。 */\n    public void setEndOffset(long endOffset) {",
        ),
        (
            "    @Override\n    public String toString() {",
            "    /** 返回 epoch 与偏移范围的字符串。 */\n    @Override\n    public String toString() {",
        ),
        (
            "    @Override\n    public boolean equals(Object o) {",
            "    /** 三字段值相等则视为同一 epoch 条目。 */\n    @Override\n    public boolean equals(Object o) {",
        ),
        (
            "    @Override\n    public int hashCode() {",
            "    /** 基于 epoch 与起止偏移计算哈希。 */\n    @Override\n    public int hashCode() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/FastCodesHeader.java": [
        (
            "public interface FastCodesHeader {",
            "/**\n * 高性能 Remoting 请求头：直接对 {@link ByteBuf} 编解码，绕过反射字段映射。\n */\npublic interface FastCodesHeader {",
        ),
        (
            "    default String getAndCheckNotNull(HashMap<String, String> fields, String field) {",
            "    /** 从字段 Map 取值；缺失时记错误日志但不抛异常，保持与旧解码兼容。 */\n    default String getAndCheckNotNull(HashMap<String, String> fields, String field) {",
        ),
        (
            "            // no exception throws, keep compatible with RemotingCommand.decodeCommandCustomHeader",
            "            // 不抛异常，与 RemotingCommand.decodeCommandCustomHeader 行为一致",
        ),
        (
            "    default void writeIfNotNull(ByteBuf out, String key, Object value) {",
            "    /** value 非空时按 RocketMQ 字符串格式写入键值对。 */\n    default void writeIfNotNull(ByteBuf out, String key, Object value) {",
        ),
        (
            "    void encode(ByteBuf out);",
            "    /** 将请求头字段序列化到 ByteBuf。 */\n    void encode(ByteBuf out);",
        ),
        (
            "    void decode(HashMap<String, String> fields) throws RemotingCommandException;",
            "    /** 从 extFields 解析并填充请求头。 */\n    void decode(HashMap<String, String> fields) throws RemotingCommandException;",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/ForbiddenType.java": [
        (
            "/**\n *\n * gives the reason for a no permission messaging pulling.\n *\n */",
            "/**\n * 消息拉取被禁止的原因码常量。\n */",
        ),
        (
            "    /**\n     * 1=forbidden by broker\n     */",
            "    /** 1=Broker 全局禁止拉取。 */",
        ),
        (
            "    int BROKER_FORBIDDEN               = 1;",
            "    /** Broker 级禁止。 */\n    int BROKER_FORBIDDEN               = 1;",
        ),
        (
            "    /**\n     * 2=forbidden by groupId\n     */",
            "    /** 2=消费者组被禁止。 */",
        ),
        (
            "    int GROUP_FORBIDDEN                = 2;",
            "    /** 消费组级禁止。 */\n    int GROUP_FORBIDDEN                = 2;",
        ),
        (
            "    /**\n     * 3=forbidden by topic\n     */",
            "    /** 3=Topic 被禁止。 */",
        ),
        (
            "    int TOPIC_FORBIDDEN                = 3;",
            "    /** Topic 级禁止。 */\n    int TOPIC_FORBIDDEN                = 3;",
        ),
        (
            "    /**\n     * 4=forbidden by broadcasting mode\n     */",
            "    /** 4=广播模式被禁止。 */",
        ),
        (
            "    int BROADCASTING_DISABLE_FORBIDDEN = 4;",
            "    /** 广播消费禁止。 */\n    int BROADCASTING_DISABLE_FORBIDDEN = 4;",
        ),
        (
            "    /**\n     * 5=forbidden for a subscription(group with a topic)\n     */",
            "    /** 5=特定订阅（组+Topic）被禁止。 */",
        ),
        (
            "    int SUBSCRIPTION_FORBIDDEN         = 5;",
            "    /** 订阅级禁止。 */\n    int SUBSCRIPTION_FORBIDDEN         = 5;",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/LanguageCode.java": [
        (
            "public enum LanguageCode {",
            "/**\n * 客户端/SDK 语言标识，随 Remoting 握手上报。\n */\npublic enum LanguageCode {",
        ),
        (
            "    JAVA((byte) 0),",
            "    /** Java 客户端。 */\n    JAVA((byte) 0),",
        ),
        (
            "    CPP((byte) 1),",
            "    /** C++ 客户端。 */\n    CPP((byte) 1),",
        ),
        (
            "    DOTNET((byte) 2),",
            "    /** .NET 客户端。 */\n    DOTNET((byte) 2),",
        ),
        (
            "    PYTHON((byte) 3),",
            "    /** Python 客户端。 */\n    PYTHON((byte) 3),",
        ),
        (
            "    GO((byte) 9),",
            "    /** Go 客户端。 */\n    GO((byte) 9),",
        ),
        (
            "    RUST((byte) 12),",
            "    /** Rust 客户端。 */\n    RUST((byte) 12),",
        ),
        (
            "    NODE_JS((byte) 13);",
            "    /** Node.js 客户端。 */\n    NODE_JS((byte) 13);",
        ),
        (
            "    private byte code;",
            "    /** 协议层单字节语言码。 */\n    private byte code;",
        ),
        (
            "    LanguageCode(byte code) {",
            "    /** 绑定协议码。 */\n    LanguageCode(byte code) {",
        ),
        (
            "    public static LanguageCode valueOf(byte code) {",
            "    /** 按字节码查找枚举，未命中返回 null。 */\n    public static LanguageCode valueOf(byte code) {",
        ),
        (
            "    public byte getCode() {",
            "    /** 返回协议语言码。 */\n    public byte getCode() {",
        ),
        (
            "    public static LanguageCode getCode(String language) {",
            "    /** 按枚举名（如 JAVA）查找，未命中返回 null。 */\n    public static LanguageCode getCode(String language) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/MQProtosHelper.java": [
        (
            "public class MQProtosHelper {",
            "/**\n * Remoting 协议辅助工具：封装向 NameServer 注册 Broker 的同步调用。\n */\npublic class MQProtosHelper {",
        ),
        (
            "    public static boolean registerBrokerToNameServer(final String nsaddr, final String brokerAddr,\n        final long timeoutMillis) {",
            "    /**\n     * 向指定 NameServer 同步注册 Broker。\n     *\n     * @param nsaddr NameServer 地址\n     * @param brokerAddr Broker 地址\n     * @param timeoutMillis 调用超时（毫秒）\n     * @return 响应码为 SUCCESS 时返回 true\n     */\n    public static boolean registerBrokerToNameServer(final String nsaddr, final String brokerAddr,\n        final long timeoutMillis) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/NamespaceUtil.java": [
        (
            "public class NamespaceUtil {",
            "/**\n * 多租户命名空间工具：在 Topic/Group 资源名与 {@code NS%Resource} 形式间转换。\n */\npublic class NamespaceUtil {",
        ),
        (
            "    public static final char NAMESPACE_SEPARATOR = '%';",
            "    /** 命名空间与资源名分隔符。 */\n    public static final char NAMESPACE_SEPARATOR = '%';",
        ),
        (
            "    public static final String STRING_BLANK = \"\";",
            "    /** 空字符串常量。 */\n    public static final String STRING_BLANK = \"\";",
        ),
        (
            "    public static final int RETRY_PREFIX_LENGTH = MixAll.RETRY_GROUP_TOPIC_PREFIX.length();",
            "    /** %RETRY% 前缀长度，用于剥离重试 Topic 前缀。 */\n    public static final int RETRY_PREFIX_LENGTH = MixAll.RETRY_GROUP_TOPIC_PREFIX.length();",
        ),
        (
            "    public static final int DLQ_PREFIX_LENGTH = MixAll.DLQ_GROUP_TOPIC_PREFIX.length();",
            "    /** %DLQ% 前缀长度，用于剥离死信 Topic 前缀。 */\n    public static final int DLQ_PREFIX_LENGTH = MixAll.DLQ_GROUP_TOPIC_PREFIX.length();",
        ),
        (
            "    /**\n     * Unpack namespace from resource, just like:\n     * (1) MQ_INST_XX%Topic_XXX --> Topic_XXX\n     * (2) %RETRY%MQ_INST_XX%GID_XXX --> %RETRY%GID_XXX\n     *\n     * @param resourceWithNamespace, topic/groupId with namespace.\n     * @return topic/groupId without namespace.\n     */",
            "    /**\n     * 从资源名剥离命名空间，例如：\n     * (1) MQ_INST_XX%Topic_XXX --> Topic_XXX\n     * (2) %RETRY%MQ_INST_XX%GID_XXX --> %RETRY%GID_XXX\n     *\n     * @param resourceWithNamespace 带命名空间的 topic/groupId\n     * @return 不含命名空间的 topic/groupId\n     */",
        ),
        (
            "    /**\n     * If resource contains the namespace, unpack namespace from resource, just like:\n     * (1) (MQ_INST_XX1%Topic_XXX1, MQ_INST_XX1) --> Topic_XXX1\n     * (2) (MQ_INST_XX2%Topic_XXX2, NULL) --> MQ_INST_XX2%Topic_XXX2\n     * (3) (%RETRY%MQ_INST_XX1%GID_XXX1, MQ_INST_XX1) --> %RETRY%GID_XXX1\n     * (4) (%RETRY%MQ_INST_XX2%GID_XXX2, MQ_INST_XX3) --> %RETRY%MQ_INST_XX2%GID_XXX2\n     *\n     * @param resourceWithNamespace, topic/groupId with namespace.\n     * @param namespace, namespace to be unpacked.\n     * @return topic/groupId without namespace.\n     */",
            "    /**\n     * 仅当资源属于指定 namespace 时才剥离，例如：\n     * (1) (MQ_INST_XX1%Topic_XXX1, MQ_INST_XX1) --> Topic_XXX1\n     * (2) (MQ_INST_XX2%Topic_XXX2, NULL) --> MQ_INST_XX2%Topic_XXX2\n     * (3) (%RETRY%MQ_INST_XX1%GID_XXX1, MQ_INST_XX1) --> %RETRY%GID_XXX1\n     * (4) (%RETRY%MQ_INST_XX2%GID_XXX2, MQ_INST_XX3) --> %RETRY%MQ_INST_XX2%GID_XXX2\n     *\n     * @param resourceWithNamespace 带命名空间的 topic/groupId\n     * @param namespace 待剥离的命名空间\n     * @return 不含命名空间的 topic/groupId\n     */",
        ),
        (
            "    public static String wrapNamespace(String namespace, String resourceWithOutNamespace) {",
            "    /** 为普通资源名添加 namespace 前缀，系统 Topic 或已包装则原样返回。 */\n    public static String wrapNamespace(String namespace, String resourceWithOutNamespace) {",
        ),
        (
            "    public static boolean isAlreadyWithNamespace(String resource, String namespace) {",
            "    /** 判断资源是否已包含指定 namespace（忽略 %RETRY%/%DLQ% 前缀）。 */\n    public static boolean isAlreadyWithNamespace(String resource, String namespace) {",
        ),
        (
            "    public static String wrapNamespaceAndRetry(String namespace, String consumerGroup) {",
            "    /** 生成 %RETRY% + namespace%consumerGroup 形式的重试 Topic 名。 */\n    public static String wrapNamespaceAndRetry(String namespace, String consumerGroup) {",
        ),
        (
            "    public static String getNamespaceFromResource(String resource) {",
            "    /** 从资源名解析 namespace 段；无分隔符或系统资源返回空串。 */\n    public static String getNamespaceFromResource(String resource) {",
        ),
        (
            "    public static String withOutRetryAndDLQ(String originalResource) {",
            "    /** 去掉 %RETRY% 或 %DLQ% 前缀，保留 namespace 与主体名。 */\n    public static String withOutRetryAndDLQ(String originalResource) {",
        ),
        (
            "    private static boolean isSystemResource(String resource) {",
            "    /** 系统 Topic 或系统消费组视为不可加 namespace 的资源。 */\n    private static boolean isSystemResource(String resource) {",
        ),
        (
            "    public static boolean isRetryTopic(String resource) {",
            "    /** 是否以 %RETRY% 开头的重试 Topic。 */\n    public static boolean isRetryTopic(String resource) {",
        ),
        (
            "    public static boolean isDLQTopic(String resource) {",
            "    /** 是否以 %DLQ% 开头的死信 Topic。 */\n    public static boolean isDLQTopic(String resource) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/RemotingCommandType.java": [
        (
            "public enum RemotingCommandType {",
            "/**\n * Remoting 帧类型：区分请求与响应命令。\n */\npublic enum RemotingCommandType {",
        ),
        (
            "    REQUEST_COMMAND,",
            "    /** 客户端/服务端发出的请求帧。 */\n    REQUEST_COMMAND,",
        ),
        (
            "    RESPONSE_COMMAND;",
            "    /** 对请求的响应帧。 */\n    RESPONSE_COMMAND;",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/RemotingSerializable.java": [
        (
            "public abstract class RemotingSerializable {",
            "/**\n * Remoting 可序列化基类：基于 Fastjson2 提供 JSON/字节数组编解码。\n */\npublic abstract class RemotingSerializable {",
        ),
        (
            "    private final static Charset CHARSET_UTF8 = StandardCharsets.UTF_8;",
            "    /** JSON 序列化统一使用 UTF-8。 */\n    private final static Charset CHARSET_UTF8 = StandardCharsets.UTF_8;",
        ),
        (
            "    public static byte[] encode(final Object obj) {",
            "    /** 将任意对象编码为 UTF-8 JSON 字节数组。 */\n    public static byte[] encode(final Object obj) {",
        ),
        (
            "    public static String toJson(final Object obj, boolean prettyFormat) {",
            "    /** 将对象转为 JSON 字符串，可选 PrettyFormat。 */\n    public static String toJson(final Object obj, boolean prettyFormat) {",
        ),
        (
            "    public static <T> T decode(final byte[] data, Class<T> classOfT) {",
            "    /** 从 JSON 字节数组反序列化为指定类型。 */\n    public static <T> T decode(final byte[] data, Class<T> classOfT) {",
        ),
        (
            "    public static <T> List<T> decodeList(final byte[] data, Class<T> classOfT) {",
            "    /** 解析 JSON 数组为 List&lt;T&gt;。 */\n    public static <T> List<T> decodeList(final byte[] data, Class<T> classOfT) {",
        ),
        (
            "    public static <T> T fromJson(String json, Class<T> classOfT) {",
            "    /** 从 JSON 字符串反序列化。 */\n    public static <T> T fromJson(String json, Class<T> classOfT) {",
        ),
        (
            "    public byte[] encode() {",
            "    /** 将当前实例编码为 JSON 字节数组。 */\n    public byte[] encode() {",
        ),
        (
            "    /**\n     * Allow call-site to apply specific features according to their requirements.\n     *\n     * @param features Features to apply\n     * @return serialized data.\n     */",
            "    /**\n     * 允许调用方传入 Fastjson2 {@link JSONWriter.Feature} 控制序列化行为。\n     *\n     * @param features 要启用的特性\n     * @return 序列化后的字节数组\n     */",
        ),
        (
            "    public String toJson() {",
            "    /** 紧凑 JSON 字符串。 */\n    public String toJson() {",
        ),
        (
            "    public String toJson(final boolean prettyFormat) {",
            "    /** 可选格式化输出的 JSON 字符串。 */\n    public String toJson(final boolean prettyFormat) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/RemotingSysResponseCode.java": [
        (
            "public class RemotingSysResponseCode {",
            "/**\n * Remoting 层系统级响应码，与业务 {@link ResponseCode} 区分。\n */\npublic class RemotingSysResponseCode {",
        ),
        (
            "    public static final int SUCCESS = 0;",
            "    /** 处理成功。 */\n    public static final int SUCCESS = 0;",
        ),
        (
            "    public static final int SYSTEM_ERROR = 1;",
            "    /** 服务端未捕获异常。 */\n    public static final int SYSTEM_ERROR = 1;",
        ),
        (
            "    public static final int SYSTEM_BUSY = 2;",
            "    /** 服务端流控或线程池饱和。 */\n    public static final int SYSTEM_BUSY = 2;",
        ),
        (
            "    public static final int REQUEST_CODE_NOT_SUPPORTED = 3;",
            "    /** 未知或未注册的 RequestCode。 */\n    public static final int REQUEST_CODE_NOT_SUPPORTED = 3;",
        ),
        (
            "    public static final int TRANSACTION_FAILED = 4;",
            "    /** 事务消息处理失败。 */\n    public static final int TRANSACTION_FAILED = 4;",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/RequestCode.java": [
        (
            "public class RequestCode {",
            "/**\n * Remoting 请求码常量表：Broker、NameServer、Proxy 与 Controller 各 RPC 的唯一整数标识。\n */\npublic class RequestCode {",
        ),
        (
            "    public static final int SEND_MESSAGE = 10;",
            "    /** 单条消息发送。 */\n    public static final int SEND_MESSAGE = 10;",
        ),
        (
            "    public static final int PULL_MESSAGE = 11;",
            "    /** 拉模式消费。 */\n    public static final int PULL_MESSAGE = 11;",
        ),
        (
            "    public static final int HEART_BEAT = 34;",
            "    /** 客户端心跳注册。 */\n    public static final int HEART_BEAT = 34;",
        ),
        (
            "    public static final int END_TRANSACTION = 37;",
            "    /** 提交或回滚半事务消息。 */\n    public static final int END_TRANSACTION = 37;",
        ),
        (
            "    public static final int POP_MESSAGE = 200050;",
            "    /** Pop 消费拉取消息。 */\n    public static final int POP_MESSAGE = 200050;",
        ),
        (
            "    public static final int ACK_MESSAGE = 200051;",
            "    /** Pop 消费确认。 */\n    public static final int ACK_MESSAGE = 200051;",
        ),
        (
            "    // lite admin api",
            "    // Lite 管理面 API",
        ),
        (
            "    public static final int REGISTER_BROKER = 103;",
            "    /** Broker 向 NameServer 注册。 */\n    public static final int REGISTER_BROKER = 103;",
        ),
        (
            "    public static final int GET_ROUTEINFO_BY_TOPIC = 105;",
            "    /** 按 Topic 查询路由。 */\n    public static final int GET_ROUTEINFO_BY_TOPIC = 105;",
        ),
        (
            "    /**\n     * update the config of name server\n     */",
            "    /** 更新 NameServer 配置。 */",
        ),
        (
            "    /**\n     * get config from name server\n     */",
            "    /** 读取 NameServer 配置。 */",
        ),
        (
            "    /**\n     * resume logic of checking half messages that have been put in TRANS_CHECK_MAXTIME_TOPIC before\n     */",
            "    /** 恢复此前写入 TRANS_CHECK_MAXTIME_TOPIC 的半事务消息回查逻辑。 */",
        ),
        (
            "    /**\n     * Controller code\n     */",
            "    /** Controller 模块请求码段。 */",
        ),
        (
            "    public static final int CONTROLLER_ELECT_MASTER = 1002;",
            "    /** Controller 选举 Master。 */\n    public static final int CONTROLLER_ELECT_MASTER = 1002;",
        ),
        (
            "    /**\n     * update the config of controller\n     */",
            "    /** 更新 Controller 配置。 */",
        ),
        (
            "    /**\n     * get config from controller\n     */",
            "    /** 读取 Controller 配置。 */",
        ),
        (
            "    /**\n     * clean broker data\n     */",
            "    /** 清理 Broker 元数据。 */",
        ),
        (
            "    public static final int AUTH_CREATE_USER = 3001;",
            "    /** ACL：创建用户。 */\n    public static final int AUTH_CREATE_USER = 3001;",
        ),
        (
            "    public static final int SWITCH_TIMER_ENGINE = 5001;",
            "    /** 切换定时消息引擎。 */\n    public static final int SWITCH_TIMER_ENGINE = 5001;",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/RequestHeaderRegistry.java": [
        (
            "public class RequestHeaderRegistry {",
            "/**\n * 请求头类型注册表：扫描 {@link CommandCustomHeader} 子类并按 {@link RequestCode} 建立映射。\n */\npublic class RequestHeaderRegistry {",
        ),
        (
            "    private static final String PACKAGE_NAME = \"org.apache.rocketmq.remoting.protocol.header\";",
            "    /** 请求头类所在包，供 Reflections 扫描。 */\n    private static final String PACKAGE_NAME = \"org.apache.rocketmq.remoting.protocol.header\";",
        ),
        (
            "    private final Map<Integer, Class<? extends CommandCustomHeader>> requestHeaderMap = new HashMap<>();",
            "    /** RequestCode → 请求头 Class 映射。 */\n    private final Map<Integer, Class<? extends CommandCustomHeader>> requestHeaderMap = new HashMap<>();",
        ),
        (
            "    public static RequestHeaderRegistry getInstance() {",
            "    /** 返回单例注册表。 */\n    public static RequestHeaderRegistry getInstance() {",
        ),
        (
            "    public void initialize() {",
            "    /** 扫描 header 包并注册带 {@link RocketMQAction} 注解的请求头类。 */\n    public void initialize() {",
        ),
        (
            "    public Class<? extends CommandCustomHeader> getRequestHeader(int requestCode) {",
            "    /** 按 RequestCode 查找对应请求头类型，未注册返回 null。 */\n    public Class<? extends CommandCustomHeader> getRequestHeader(int requestCode) {",
        ),
        (
            "    private void registerHeader(Class<? extends CommandCustomHeader> clazz) {",
            "    /** 读取 {@link RocketMQAction#value()} 并写入映射表。 */\n    private void registerHeader(Class<? extends CommandCustomHeader> clazz) {",
        ),
        (
            "    private static class RequestHeaderRegistryHolder {",
            "    /** 静态内部类持有单例，延迟加载。 */\n    private static class RequestHeaderRegistryHolder {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/RequestSource.java": [
        (
            "public enum RequestSource {",
            "/**\n * 请求来源标识：区分 SDK 直连与 Proxy 各类转发模式。\n */\npublic enum RequestSource {",
        ),
        (
            "    SDK(-1),",
            "    /** 原生 SDK 直连。 */\n    SDK(-1),",
        ),
        (
            "    PROXY_FOR_ORDER(0),",
            "    /** Proxy 顺序消息转发。 */\n    PROXY_FOR_ORDER(0),",
        ),
        (
            "    PROXY_FOR_BROADCAST(1),",
            "    /** Proxy 广播消费转发。 */\n    PROXY_FOR_BROADCAST(1),",
        ),
        (
            "    PROXY_FOR_STREAM(2);",
            "    /** Proxy 流式消费转发。 */\n    PROXY_FOR_STREAM(2);",
        ),
        (
            "    public static final String SYSTEM_PROPERTY_KEY = \"rocketmq.requestSource\";",
            "    /** JVM 系统属性键，用于覆盖默认请求来源。 */\n    public static final String SYSTEM_PROPERTY_KEY = \"rocketmq.requestSource\";",
        ),
        (
            "    private final int value;",
            "    /** 协议层整型来源码。 */\n    private final int value;",
        ),
        (
            "    RequestSource(int value) {",
            "    /** 绑定来源码。 */\n    RequestSource(int value) {",
        ),
        (
            "    public int getValue() {",
            "    /** 返回来源码。 */\n    public int getValue() {",
        ),
        (
            "    public static boolean isValid(Integer value) {",
            "    /** 判断整型值是否在已知来源范围内。 */\n    public static boolean isValid(Integer value) {",
        ),
        (
            "    public static RequestSource parseInteger(Integer value) {",
            "    /** 解析整型来源码，非法时回退为 {@link #SDK}。 */\n    public static RequestSource parseInteger(Integer value) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/RequestType.java": [
        (
            "public enum RequestType {",
            "/**\n * Remoting 请求子类型，目前仅定义流式请求。\n */\npublic enum RequestType {",
        ),
        (
            "    STREAM((byte) 0);",
            "    /** 流式 RPC 请求。 */\n    STREAM((byte) 0);",
        ),
        (
            "    private final byte code;",
            "    /** 协议单字节类型码。 */\n    private final byte code;",
        ),
        (
            "    RequestType(byte code) {",
            "    /** 绑定类型码。 */\n    RequestType(byte code) {",
        ),
        (
            "    public static RequestType valueOf(byte code) {",
            "    /** 按字节码查找，未命中返回 null。 */\n    public static RequestType valueOf(byte code) {",
        ),
        (
            "    public byte getCode() {",
            "    /** 返回类型码。 */\n    public byte getCode() {",
        ),
    ],
}
