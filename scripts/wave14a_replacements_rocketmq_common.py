"""RocketMQ 5.5.0 wave14a common constant/consumer/entity/filter [0:15] replacements."""

R: dict[str, list[tuple[str, str]]] = {}

R["common/src/main/java/org/apache/rocketmq/common/constant/GrpcConstants.java"] = [
    (
        "public class GrpcConstants {",
        "/**\n * gRPC 调用上下文与 Metadata 键名常量：承载 RPC 元数据、鉴权及客户端标识等。\n */\npublic class GrpcConstants {",
    ),
    (
        "    public static final Context.Key<Metadata> METADATA = Context.key(\"rpc-metadata\");",
        "    /** gRPC {@link Context} 中存放 RPC Metadata 的键。 */\n    public static final Context.Key<Metadata> METADATA = Context.key(\"rpc-metadata\");",
    ),
    (
        "    /**\n     * Remote address key in attributes of call\n     */",
        "    /** 调用属性中的远端地址 Metadata 键。 */",
    ),
    (
        "    /**\n     * Local address key in attributes of call\n     */",
        "    /** 调用属性中的本地地址 Metadata 键。 */",
    ),
    (
        "    public static final Metadata.Key<String> AUTHORIZATION",
        "    /** HTTP Authorization 头对应的 Metadata 键。 */\n    public static final Metadata.Key<String> AUTHORIZATION",
    ),
    (
        "    public static final Metadata.Key<String> NAMESPACE_ID",
        "    /** 命名空间 ID（{@code x-mq-namespace}）。 */\n    public static final Metadata.Key<String> NAMESPACE_ID",
    ),
    (
        "    public static final Metadata.Key<String> DATE_TIME",
        "    /** 请求日期时间（{@code x-mq-date-time}）。 */\n    public static final Metadata.Key<String> DATE_TIME",
    ),
    (
        "    public static final Metadata.Key<String> REQUEST_ID",
        "    /** 请求唯一标识（{@code x-mq-request-id}）。 */\n    public static final Metadata.Key<String> REQUEST_ID",
    ),
    (
        "    public static final Metadata.Key<String> LANGUAGE",
        "    /** 客户端语言标识（{@code x-mq-language}）。 */\n    public static final Metadata.Key<String> LANGUAGE",
    ),
    (
        "    public static final Metadata.Key<String> CLIENT_VERSION",
        "    /** 客户端版本号（{@code x-mq-client-version}）。 */\n    public static final Metadata.Key<String> CLIENT_VERSION",
    ),
    (
        "    public static final Metadata.Key<String> PROTOCOL_VERSION",
        "    /** 协议版本（{@code x-mq-protocol}）。 */\n    public static final Metadata.Key<String> PROTOCOL_VERSION",
    ),
    (
        "    public static final Metadata.Key<String> RPC_NAME",
        "    /** RPC 方法全名（{@code x-mq-rpc-name}）。 */\n    public static final Metadata.Key<String> RPC_NAME",
    ),
    (
        "    public static final Metadata.Key<String> SIMPLE_RPC_NAME",
        "    /** RPC 方法简名（{@code x-mq-simple-rpc-name}）。 */\n    public static final Metadata.Key<String> SIMPLE_RPC_NAME",
    ),
    (
        "    public static final Metadata.Key<String> SESSION_TOKEN",
        "    /** 会话令牌（{@code x-mq-session-token}）。 */\n    public static final Metadata.Key<String> SESSION_TOKEN",
    ),
    (
        "    public static final Metadata.Key<String> CLIENT_ID",
        "    /** 客户端 ID（{@code x-mq-client-id}）。 */\n    public static final Metadata.Key<String> CLIENT_ID",
    ),
    (
        "    public static final Metadata.Key<String> AUTHORIZATION_AK",
        "    /** 鉴权 AccessKey（{@code x-mq-authorization-ak}）。 */\n    public static final Metadata.Key<String> AUTHORIZATION_AK",
    ),
    (
        "    public static final Metadata.Key<String> CHANNEL_ID",
        "    /** 连接通道 ID（{@code x-mq-channel-id}）。 */\n    public static final Metadata.Key<String> CHANNEL_ID",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/constant/HAProxyConstants.java"] = [
    (
        "public class HAProxyConstants {",
        "/**\n * HAProxy PROXY Protocol 相关属性键名常量。\n */\npublic class HAProxyConstants {",
    ),
    (
        "    public static final String CHANNEL_ID = \"channel_id\";",
        "    /** Netty Channel 属性中的通道 ID 键。 */\n    public static final String CHANNEL_ID = \"channel_id\";",
    ),
    (
        "    public static final String PROXY_PROTOCOL_PREFIX = \"proxy_protocol_\";",
        "    /** PROXY Protocol 属性键前缀。 */\n    public static final String PROXY_PROTOCOL_PREFIX = \"proxy_protocol_\";",
    ),
    (
        "    public static final String PROXY_PROTOCOL_ADDR = PROXY_PROTOCOL_PREFIX + \"addr\";",
        "    /** 客户端源地址（PROXY Protocol 解析结果）。 */\n    public static final String PROXY_PROTOCOL_ADDR = PROXY_PROTOCOL_PREFIX + \"addr\";",
    ),
    (
        "    public static final String PROXY_PROTOCOL_PORT = PROXY_PROTOCOL_PREFIX + \"port\";",
        "    /** 客户端源端口。 */\n    public static final String PROXY_PROTOCOL_PORT = PROXY_PROTOCOL_PREFIX + \"port\";",
    ),
    (
        "    public static final String PROXY_PROTOCOL_SERVER_ADDR = PROXY_PROTOCOL_PREFIX + \"server_addr\";",
        "    /** 服务端本地地址。 */\n    public static final String PROXY_PROTOCOL_SERVER_ADDR = PROXY_PROTOCOL_PREFIX + \"server_addr\";",
    ),
    (
        "    public static final String PROXY_PROTOCOL_SERVER_PORT = PROXY_PROTOCOL_PREFIX + \"server_port\";",
        "    /** 服务端本地端口。 */\n    public static final String PROXY_PROTOCOL_SERVER_PORT = PROXY_PROTOCOL_PREFIX + \"server_port\";",
    ),
    (
        "    public static final String PROXY_PROTOCOL_TLV_PREFIX = PROXY_PROTOCOL_PREFIX + \"tlv_0x\";",
        "    /** PROXY Protocol TLV 扩展字段键前缀（十六进制类型码）。 */\n    public static final String PROXY_PROTOCOL_TLV_PREFIX = PROXY_PROTOCOL_PREFIX + \"tlv_0x\";",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/constant/LoggerName.java"] = [
    (
        "public class LoggerName {",
        "/**\n * RocketMQ 各模块 SLF4J Logger 名称常量。\n */\npublic class LoggerName {",
    ),
    (
        "    public static final String FILTERSRV_LOGGER_NAME = \"RocketmqFiltersrv\";",
        "    /** Filter Server 日志名。 */\n    public static final String FILTERSRV_LOGGER_NAME = \"RocketmqFiltersrv\";",
    ),
    (
        "    public static final String NAMESRV_LOGGER_NAME = \"RocketmqNamesrv\";",
        "    /** NameServer 日志名。 */\n    public static final String NAMESRV_LOGGER_NAME = \"RocketmqNamesrv\";",
    ),
    (
        "    public static final String NAMESRV_CONSOLE_LOGGER_NAME = \"RocketmqNamesrvConsole\";",
        "    /** NameServer 控制台日志名。 */\n    public static final String NAMESRV_CONSOLE_LOGGER_NAME = \"RocketmqNamesrvConsole\";",
    ),
    (
        "    public static final String CONTROLLER_LOGGER_NAME = \"RocketmqController\";",
        "    /** Controller 日志名。 */\n    public static final String CONTROLLER_LOGGER_NAME = \"RocketmqController\";",
    ),
    (
        "    public static final String CONTROLLER_CONSOLE_NAME = \"RocketmqControllerConsole\";",
        "    /** Controller 控制台日志名。 */\n    public static final String CONTROLLER_CONSOLE_NAME = \"RocketmqControllerConsole\";",
    ),
    (
        "    public static final String NAMESRV_WATER_MARK_LOGGER_NAME = \"RocketmqNamesrvWaterMark\";",
        "    /** NameServer 水位监控日志名。 */\n    public static final String NAMESRV_WATER_MARK_LOGGER_NAME = \"RocketmqNamesrvWaterMark\";",
    ),
    (
        "    public static final String BROKER_LOGGER_NAME = \"RocketmqBroker\";",
        "    /** Broker 日志名。 */\n    public static final String BROKER_LOGGER_NAME = \"RocketmqBroker\";",
    ),
    (
        "    public static final String BROKER_CONSOLE_NAME = \"RocketmqConsole\";",
        "    /** Broker 控制台日志名。 */\n    public static final String BROKER_CONSOLE_NAME = \"RocketmqConsole\";",
    ),
    (
        "    public static final String CLIENT_LOGGER_NAME = \"RocketmqClient\";",
        "    /** Client 日志名。 */\n    public static final String CLIENT_LOGGER_NAME = \"RocketmqClient\";",
    ),
    (
        "    public static final String ROCKETMQ_TRAFFIC_NAME = \"RocketmqTraffic\";",
        "    /** 流量统计日志名。 */\n    public static final String ROCKETMQ_TRAFFIC_NAME = \"RocketmqTraffic\";",
    ),
    (
        "    public static final String ROCKETMQ_REMOTING_NAME = \"RocketmqRemoting\";",
        "    /** Remoting 网络层日志名。 */\n    public static final String ROCKETMQ_REMOTING_NAME = \"RocketmqRemoting\";",
    ),
    (
        "    public static final String TOOLS_LOGGER_NAME = \"RocketmqTools\";",
        "    /** 命令行工具日志名。 */\n    public static final String TOOLS_LOGGER_NAME = \"RocketmqTools\";",
    ),
    (
        "    public static final String COMMON_LOGGER_NAME = \"RocketmqCommon\";",
        "    /** Common 公共模块日志名。 */\n    public static final String COMMON_LOGGER_NAME = \"RocketmqCommon\";",
    ),
    (
        "    public static final String STORE_LOGGER_NAME = \"RocketmqStore\";",
        "    /** Store 存储层日志名。 */\n    public static final String STORE_LOGGER_NAME = \"RocketmqStore\";",
    ),
    (
        "    public static final String STORE_ERROR_LOGGER_NAME = \"RocketmqStoreError\";",
        "    /** Store 错误日志名。 */\n    public static final String STORE_ERROR_LOGGER_NAME = \"RocketmqStoreError\";",
    ),
    (
        "    public static final String TRANSACTION_LOGGER_NAME = \"RocketmqTransaction\";",
        "    /** 事务消息日志名。 */\n    public static final String TRANSACTION_LOGGER_NAME = \"RocketmqTransaction\";",
    ),
    (
        "    public static final String REBALANCE_LOCK_LOGGER_NAME = \"RocketmqRebalanceLock\";",
        "    /** Rebalance 锁日志名。 */\n    public static final String REBALANCE_LOCK_LOGGER_NAME = \"RocketmqRebalanceLock\";",
    ),
    (
        "    public static final String ROCKETMQ_STATS_LOGGER_NAME = \"RocketmqStats\";",
        "    /** 统计指标日志名。 */\n    public static final String ROCKETMQ_STATS_LOGGER_NAME = \"RocketmqStats\";",
    ),
    (
        "    public static final String DLQ_STATS_LOGGER_NAME = \"RocketmqDLQStats\";",
        "    /** 死信队列统计日志名。 */\n    public static final String DLQ_STATS_LOGGER_NAME = \"RocketmqDLQStats\";",
    ),
    (
        "    public static final String DLQ_LOGGER_NAME = \"RocketmqDLQ\";",
        "    /** 死信队列日志名。 */\n    public static final String DLQ_LOGGER_NAME = \"RocketmqDLQ\";",
    ),
    (
        "    public static final String CONSUMER_STATS_LOGGER_NAME = \"RocketmqConsumerStats\";",
        "    /** 消费者统计日志名。 */\n    public static final String CONSUMER_STATS_LOGGER_NAME = \"RocketmqConsumerStats\";",
    ),
    (
        "    public static final String COMMERCIAL_LOGGER_NAME = \"RocketmqCommercial\";",
        "    /** 商业化/计费日志名。 */\n    public static final String COMMERCIAL_LOGGER_NAME = \"RocketmqCommercial\";",
    ),
    (
        "    public static final String ACCOUNT_LOGGER_NAME = \"RocketmqAccount\";",
        "    /** 账户相关日志名。 */\n    public static final String ACCOUNT_LOGGER_NAME = \"RocketmqAccount\";",
    ),
    (
        "    public static final String FLOW_CONTROL_LOGGER_NAME = \"RocketmqFlowControl\";",
        "    /** 流控日志名。 */\n    public static final String FLOW_CONTROL_LOGGER_NAME = \"RocketmqFlowControl\";",
    ),
    (
        "    public static final String ROCKETMQ_AUTHORIZE_LOGGER_NAME = \"RocketmqAuthorize\";",
        "    /** 鉴权日志名。 */\n    public static final String ROCKETMQ_AUTHORIZE_LOGGER_NAME = \"RocketmqAuthorize\";",
    ),
    (
        "    public static final String DUPLICATION_LOGGER_NAME = \"RocketmqDuplication\";",
        "    /** 消息去重日志名。 */\n    public static final String DUPLICATION_LOGGER_NAME = \"RocketmqDuplication\";",
    ),
    (
        "    public static final String PROTECTION_LOGGER_NAME = \"RocketmqProtection\";",
        "    /** 保护/限流日志名。 */\n    public static final String PROTECTION_LOGGER_NAME = \"RocketmqProtection\";",
    ),
    (
        "    public static final String WATER_MARK_LOGGER_NAME = \"RocketmqWaterMark\";",
        "    /** 水位监控日志名。 */\n    public static final String WATER_MARK_LOGGER_NAME = \"RocketmqWaterMark\";",
    ),
    (
        "    public static final String FILTER_LOGGER_NAME = \"RocketmqFilter\";",
        "    /** 消息过滤日志名。 */\n    public static final String FILTER_LOGGER_NAME = \"RocketmqFilter\";",
    ),
    (
        "    public static final String ROCKETMQ_POP_LOGGER_NAME = \"RocketmqPop\";",
        "    /** POP 消费模式日志名。 */\n    public static final String ROCKETMQ_POP_LOGGER_NAME = \"RocketmqPop\";",
    ),
    (
        "    public static final String ROCKETMQ_POP_LITE_LOGGER_NAME = \"RocketmqPopLite\";",
        "    /** POP Lite 消费模式日志名。 */\n    public static final String ROCKETMQ_POP_LITE_LOGGER_NAME = \"RocketmqPopLite\";",
    ),
    (
        "    public static final String FAILOVER_LOGGER_NAME = \"RocketmqFailover\";",
        "    /** 故障转移日志名。 */\n    public static final String FAILOVER_LOGGER_NAME = \"RocketmqFailover\";",
    ),
    (
        "    public static final String STDOUT_LOGGER_NAME = \"STDOUT\";",
        "    /** 标准输出 Logger 名。 */\n    public static final String STDOUT_LOGGER_NAME = \"STDOUT\";",
    ),
    (
        "    public static final String PROXY_LOGGER_NAME = \"RocketmqProxy\";",
        "    /** Proxy 代理日志名。 */\n    public static final String PROXY_LOGGER_NAME = \"RocketmqProxy\";",
    ),
    (
        "    public static final String PROXY_WATER_MARK_LOGGER_NAME = \"RocketmqProxyWatermark\";",
        "    /** Proxy 水位监控日志名。 */\n    public static final String PROXY_WATER_MARK_LOGGER_NAME = \"RocketmqProxyWatermark\";",
    ),
    (
        "    public static final String ROCKETMQ_COLDCTR_LOGGER_NAME = \"RocketmqColdCtr\";",
        "    /** 冷读控制日志名。 */\n    public static final String ROCKETMQ_COLDCTR_LOGGER_NAME = \"RocketmqColdCtr\";",
    ),
    (
        "    public static final String ROCKSDB_LOGGER_NAME = \"RocketmqRocksDB\";",
        "    /** RocksDB 存储日志名。 */\n    public static final String ROCKSDB_LOGGER_NAME = \"RocketmqRocksDB\";",
    ),
    (
        "    public static final String ROCKETMQ_AUTH_AUDIT_LOGGER_NAME = \"RocketmqAuthAudit\";",
        "    /** 鉴权审计日志名。 */\n    public static final String ROCKETMQ_AUTH_AUDIT_LOGGER_NAME = \"RocketmqAuthAudit\";",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/constant/PermName.java"] = [
    (
        "public class PermName {",
        "/**\n * Topic/Group 权限位掩码：读、写、继承、优先等，以位标志组合表示。\n */\npublic class PermName {",
    ),
    (
        "    public static final int INDEX_PERM_PRIORITY = 3;",
        "    /** 优先权限位索引。 */\n    public static final int INDEX_PERM_PRIORITY = 3;",
    ),
    (
        "    public static final int INDEX_PERM_READ = 2;",
        "    /** 读权限位索引。 */\n    public static final int INDEX_PERM_READ = 2;",
    ),
    (
        "    public static final int INDEX_PERM_WRITE = 1;",
        "    /** 写权限位索引。 */\n    public static final int INDEX_PERM_WRITE = 1;",
    ),
    (
        "    public static final int INDEX_PERM_INHERIT = 0;",
        "    /** 继承权限位索引。 */\n    public static final int INDEX_PERM_INHERIT = 0;",
    ),
    (
        "    public static final int PERM_PRIORITY = 0x1 << INDEX_PERM_PRIORITY;",
        "    /** 优先权限标志位。 */\n    public static final int PERM_PRIORITY = 0x1 << INDEX_PERM_PRIORITY;",
    ),
    (
        "    public static final int PERM_READ = 0x1 << INDEX_PERM_READ;",
        "    /** 读权限标志位。 */\n    public static final int PERM_READ = 0x1 << INDEX_PERM_READ;",
    ),
    (
        "    public static final int PERM_WRITE = 0x1 << INDEX_PERM_WRITE;",
        "    /** 写权限标志位。 */\n    public static final int PERM_WRITE = 0x1 << INDEX_PERM_WRITE;",
    ),
    (
        "    public static final int PERM_INHERIT = 0x1 << INDEX_PERM_INHERIT;",
        "    /** 继承权限标志位。 */\n    public static final int PERM_INHERIT = 0x1 << INDEX_PERM_INHERIT;",
    ),
    (
        "    public static String perm2String(final int perm) {",
        "    /**\n     * 将权限掩码转为 {@code RWX} 可读字符串（{@code ---} 表示无权限）。\n     *\n     * @param perm 权限掩码\n     * @return 三位权限字符串\n     */\n    public static String perm2String(final int perm) {",
    ),
    (
        "    public static boolean isReadable(final int perm) {",
        "    /** 判断是否具备读权限。 */\n    public static boolean isReadable(final int perm) {",
    ),
    (
        "    public static boolean isWriteable(final int perm) {",
        "    /** 判断是否具备写权限。 */\n    public static boolean isWriteable(final int perm) {",
    ),
    (
        "    public static boolean isInherited(final int perm) {",
        "    /** 判断是否具备继承权限。 */\n    public static boolean isInherited(final int perm) {",
    ),
    (
        "    public static boolean isValid(final String perm) {",
        "    /** 判断字符串形式的权限值是否合法。 */\n    public static boolean isValid(final String perm) {",
    ),
    (
        "    public static boolean isValid(final int perm) {",
        "    /** 判断整型权限值是否在合法范围内。 */\n    public static boolean isValid(final int perm) {",
    ),
    (
        "    public static boolean isPriority(final int perm) {",
        "    /** 判断是否具备优先权限。 */\n    public static boolean isPriority(final int perm) {",
    ),
    (
        "    public static boolean isAccessible(final int perm) {",
        "    /** 判断是否可读或可写（至少具备其一）。 */\n    public static boolean isAccessible(final int perm) {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/consumer/ConsumeFromWhere.java"] = [
    (
        "public enum ConsumeFromWhere {",
        "/**\n * 消费者首次启动或位点丢失时的起始消费位置策略。\n */\npublic enum ConsumeFromWhere {",
    ),
    (
        "    CONSUME_FROM_LAST_OFFSET,",
        "    /** 从队列最新位点（last offset）开始消费。 */\n    CONSUME_FROM_LAST_OFFSET,",
    ),
    (
        "    @Deprecated\n    CONSUME_FROM_LAST_OFFSET_AND_FROM_MIN_WHEN_BOOT_FIRST,",
        "    /** @deprecated 首次启动从最小位点开始，后续从最新位点开始。 */\n    @Deprecated\n    CONSUME_FROM_LAST_OFFSET_AND_FROM_MIN_WHEN_BOOT_FIRST,",
    ),
    (
        "    @Deprecated\n    CONSUME_FROM_MIN_OFFSET,",
        "    /** @deprecated 从队列最小位点开始消费。 */\n    @Deprecated\n    CONSUME_FROM_MIN_OFFSET,",
    ),
    (
        "    @Deprecated\n    CONSUME_FROM_MAX_OFFSET,",
        "    /** @deprecated 从队列最大位点开始消费。 */\n    @Deprecated\n    CONSUME_FROM_MAX_OFFSET,",
    ),
    (
        "    CONSUME_FROM_FIRST_OFFSET,",
        "    /** 从队列最早位点（first offset）开始消费。 */\n    CONSUME_FROM_FIRST_OFFSET,",
    ),
    (
        "    CONSUME_FROM_TIMESTAMP,",
        "    /** 按指定时间戳回溯消费。 */\n    CONSUME_FROM_TIMESTAMP,",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/consumer/ReceiptHandle.java"] = [
    (
        "public class ReceiptHandle {",
        "/**\n * POP 消费回执句柄：编码/解码消息拉取凭证，含不可见时间、Broker 位点及重试 Topic 类型等信息。\n */\npublic class ReceiptHandle {",
    ),
    (
        "    private static final String SEPARATOR = MessageConst.KEY_SEPARATOR;",
        "    /** 回执串字段分隔符。 */\n    private static final String SEPARATOR = MessageConst.KEY_SEPARATOR;",
    ),
    (
        "    public static final String NORMAL_TOPIC = \"0\";",
        "    /** 普通 Topic 类型标识。 */\n    public static final String NORMAL_TOPIC = \"0\";",
    ),
    (
        "    public static final String RETRY_TOPIC = \"1\";",
        "    /** 重试 Topic V1 类型标识。 */\n    public static final String RETRY_TOPIC = \"1\";",
    ),
    (
        "    public static final String RETRY_TOPIC_V2 = \"2\";",
        "    /** 重试 Topic V2 类型标识。 */\n    public static final String RETRY_TOPIC_V2 = \"2\";",
    ),
    (
        "    private final long startOffset;",
        "    /** POP 起始消费位点。 */\n    private final long startOffset;",
    ),
    (
        "    private final long retrieveTime;",
        "    /** 消息拉取时间戳（毫秒）。 */\n    private final long retrieveTime;",
    ),
    (
        "    private final long invisibleTime;",
        "    /** 不可见时长（毫秒）。 */\n    private final long invisibleTime;",
    ),
    (
        "    private final long nextVisibleTime;",
        "    /** 下次可见时间戳（retrieveTime + invisibleTime）。 */\n    private final long nextVisibleTime;",
    ),
    (
        "    private final int reviveQueueId;",
        "    /** 复活队列 ID（超时重投用）。 */\n    private final int reviveQueueId;",
    ),
    (
        "    private final String topicType;",
        "    /** Topic 类型（普通/重试 V1/V2）。 */\n    private final String topicType;",
    ),
    (
        "    private final String brokerName;",
        "    /** 所属 Broker 名称。 */\n    private final String brokerName;",
    ),
    (
        "    private final int queueId;",
        "    /** 消息队列 ID。 */\n    private final int queueId;",
    ),
    (
        "    private final long offset;",
        "    /** 消费队列 offset。 */\n    private final long offset;",
    ),
    (
        "    private final long commitLogOffset;",
        "    /** CommitLog 物理 offset。 */\n    private final long commitLogOffset;",
    ),
    (
        "    private final String receiptHandle;",
        "    /** 原始回执字符串。 */\n    private final String receiptHandle;",
    ),
    (
        "    public String encode() {",
        "    /** 将回执字段编码为分隔符拼接的字符串（不含原始 receiptHandle 字段）。 */\n    public String encode() {",
    ),
    (
        "    public boolean isExpired() {",
        "    /** 判断不可见期是否已过期（当前时间 >= nextVisibleTime）。 */\n    public boolean isExpired() {",
    ),
    (
        "    public static ReceiptHandle decode(String receiptHandle) {",
        "    /**\n     * 从回执字符串解析 {@link ReceiptHandle}。\n     *\n     * @param receiptHandle 编码后的回执串\n     * @return 解析得到的回执对象\n     * @throws IllegalArgumentException 字段数不足时抛出\n     */\n    public static ReceiptHandle decode(String receiptHandle) {",
    ),
    (
        "        long commitLogOffset = -1L;",
        "        // 兼容旧版回执（无 commitLogOffset 字段）\n        long commitLogOffset = -1L;",
    ),
    (
        "    ReceiptHandle(final long startOffset, final long retrieveTime, final long invisibleTime, final long nextVisibleTime,",
        "    /** 包内构造：由 {@link ReceiptHandleBuilder} 调用。 */\n    ReceiptHandle(final long startOffset, final long retrieveTime, final long invisibleTime, final long nextVisibleTime,",
    ),
    (
        "    public static class ReceiptHandleBuilder {",
        "    /** {@link ReceiptHandle} 建造者。 */\n    public static class ReceiptHandleBuilder {",
    ),
    (
        "        ReceiptHandleBuilder() {",
        "        /** 包内可见的无参构造。 */\n        ReceiptHandleBuilder() {",
    ),
    (
        "        public ReceiptHandle.ReceiptHandleBuilder startOffset(final long startOffset) {",
        "        /** 设置 POP 起始位点。 */\n        public ReceiptHandle.ReceiptHandleBuilder startOffset(final long startOffset) {",
    ),
    (
        "        public ReceiptHandle.ReceiptHandleBuilder retrieveTime(final long retrieveTime) {",
        "        /** 设置拉取时间戳。 */\n        public ReceiptHandle.ReceiptHandleBuilder retrieveTime(final long retrieveTime) {",
    ),
    (
        "        public ReceiptHandle.ReceiptHandleBuilder invisibleTime(final long invisibleTime) {",
        "        /** 设置不可见时长。 */\n        public ReceiptHandle.ReceiptHandleBuilder invisibleTime(final long invisibleTime) {",
    ),
    (
        "        public ReceiptHandle.ReceiptHandleBuilder reviveQueueId(final int reviveQueueId) {",
        "        /** 设置复活队列 ID。 */\n        public ReceiptHandle.ReceiptHandleBuilder reviveQueueId(final int reviveQueueId) {",
    ),
    (
        "        public ReceiptHandle.ReceiptHandleBuilder topicType(final String topicType) {",
        "        /** 设置 Topic 类型标识。 */\n        public ReceiptHandle.ReceiptHandleBuilder topicType(final String topicType) {",
    ),
    (
        "        public ReceiptHandle.ReceiptHandleBuilder brokerName(final String brokerName) {",
        "        /** 设置 Broker 名称。 */\n        public ReceiptHandle.ReceiptHandleBuilder brokerName(final String brokerName) {",
    ),
    (
        "        public ReceiptHandle.ReceiptHandleBuilder queueId(final int queueId) {",
        "        /** 设置队列 ID。 */\n        public ReceiptHandle.ReceiptHandleBuilder queueId(final int queueId) {",
    ),
    (
        "        public ReceiptHandle.ReceiptHandleBuilder offset(final long offset) {",
        "        /** 设置消费队列 offset。 */\n        public ReceiptHandle.ReceiptHandleBuilder offset(final long offset) {",
    ),
    (
        "        public ReceiptHandle.ReceiptHandleBuilder commitLogOffset(final long commitLogOffset) {",
        "        /** 设置 CommitLog 物理 offset。 */\n        public ReceiptHandle.ReceiptHandleBuilder commitLogOffset(final long commitLogOffset) {",
    ),
    (
        "        public ReceiptHandle.ReceiptHandleBuilder receiptHandle(final String receiptHandle) {",
        "        /** 设置原始回执字符串。 */\n        public ReceiptHandle.ReceiptHandleBuilder receiptHandle(final String receiptHandle) {",
    ),
    (
        "        public ReceiptHandle build() {",
        "        /** 构建不可变 {@link ReceiptHandle} 实例。 */\n        public ReceiptHandle build() {",
    ),
    (
        "    public static ReceiptHandle.ReceiptHandleBuilder builder() {",
        "    /** 创建 {@link ReceiptHandleBuilder}。 */\n    public static ReceiptHandle.ReceiptHandleBuilder builder() {",
    ),
    (
        "    public long getStartOffset() {",
        "    /** 返回 POP 起始位点。 */\n    public long getStartOffset() {",
    ),
    (
        "    public long getRetrieveTime() {",
        "    /** 返回拉取时间戳。 */\n    public long getRetrieveTime() {",
    ),
    (
        "    public long getInvisibleTime() {",
        "    /** 返回不可见时长。 */\n    public long getInvisibleTime() {",
    ),
    (
        "    public long getNextVisibleTime() {",
        "    /** 返回下次可见时间戳。 */\n    public long getNextVisibleTime() {",
    ),
    (
        "    public int getReviveQueueId() {",
        "    /** 返回复活队列 ID。 */\n    public int getReviveQueueId() {",
    ),
    (
        "    public String getTopicType() {",
        "    /** 返回 Topic 类型标识。 */\n    public String getTopicType() {",
    ),
    (
        "    public String getBrokerName() {",
        "    /** 返回 Broker 名称。 */\n    public String getBrokerName() {",
    ),
    (
        "    public int getQueueId() {",
        "    /** 返回队列 ID。 */\n    public int getQueueId() {",
    ),
    (
        "    public long getOffset() {",
        "    /** 返回消费队列 offset。 */\n    public long getOffset() {",
    ),
    (
        "    public long getCommitLogOffset() {",
        "    /** 返回 CommitLog 物理 offset。 */\n    public long getCommitLogOffset() {",
    ),
    (
        "    public String getReceiptHandle() {",
        "    /** 返回原始回执字符串。 */\n    public String getReceiptHandle() {",
    ),
    (
        "    public boolean isRetryTopic() {",
        "    /** 判断是否为重试 Topic（V1 或 V2）。 */\n    public boolean isRetryTopic() {",
    ),
    (
        "    public String getRealTopic(String topic, String groupName) {",
        "    /**\n     * 根据 Topic 类型解析实际消费 Topic（重试 Topic 需经 {@link KeyBuilder} 构建）。\n     *\n     * @param topic 原始 Topic\n     * @param groupName 消费组名\n     * @return 实际 Topic 名\n     */\n    public String getRealTopic(String topic, String groupName) {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/entity/ClientGroup.java"] = [
    (
        "public class ClientGroup {",
        "/**\n * 客户端 ID 与消费组的复合键，用于 Map/Set 去重与哈希索引。\n */\npublic class ClientGroup {",
    ),
    (
        "    public final String clientId;",
        "    /** 客户端实例 ID。 */\n    public final String clientId;",
    ),
    (
        "    public final String group;",
        "    /** 消费组名。 */\n    public final String group;",
    ),
    (
        "    /**\n     * Cache the hash code for the object\n     */",
        "    /** 缓存的 hashCode（懒计算，0 表示未计算）。 */",
    ),
    (
        "    private int hash; // Default to 0",
        "    private int hash; // 默认为 0",
    ),
    (
        "    public ClientGroup(String clientId, String group) {",
        "    /**\n     * @param clientId 客户端 ID\n     * @param group 消费组名\n     */\n    public ClientGroup(String clientId, String group) {",
    ),
    (
        "    @Override\n    public boolean equals(Object o) {",
        "    /** 按 clientId 与 group 判等。 */\n    @Override\n    public boolean equals(Object o) {",
    ),
    (
        "    @Override\n    public int hashCode() {",
        "    /** 懒计算并缓存 hashCode。 */\n    @Override\n    public int hashCode() {",
    ),
    (
        "    @Override\n    public String toString() {",
        "    /** 返回调试字符串。 */\n    @Override\n    public String toString() {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/entity/TopicGroup.java"] = [
    (
        "public class TopicGroup {",
        "/**\n * Topic 与消费组的复合键，用于 Map/Set 去重与哈希索引。\n */\npublic class TopicGroup {",
    ),
    (
        "    public final String topic;",
        "    /** Topic 名称。 */\n    public final String topic;",
    ),
    (
        "    public final String group;",
        "    /** 消费组名。 */\n    public final String group;",
    ),
    (
        "    /**\n     * Cache the hash code for the object\n     */",
        "    /** 缓存的 hashCode（懒计算，0 表示未计算）。 */",
    ),
    (
        "    private int hash; // Default to 0",
        "    private int hash; // 默认为 0",
    ),
    (
        "    public TopicGroup(String topic, String group) {",
        "    /**\n     * @param topic Topic 名称\n     * @param group 消费组名\n     */\n    public TopicGroup(String topic, String group) {",
    ),
    (
        "    @Override\n    public boolean equals(Object o) {",
        "    /** 按 topic 与 group 判等。 */\n    @Override\n    public boolean equals(Object o) {",
    ),
    (
        "    @Override\n    public int hashCode() {",
        "    /** 懒计算并缓存 hashCode。 */\n    @Override\n    public int hashCode() {",
    ),
    (
        "    @Override\n    public String toString() {",
        "    /** 返回调试字符串。 */\n    @Override\n    public String toString() {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/fastjson/GenericMapSuperclassDeserializer.java"] = [
    (
        "/**\n * workaround https://github.com/alibaba/fastjson/issues/3730\n */",
        "/**\n * 泛型 Map 子类反序列化器：从父类 {@link ParameterizedType} 读取 K/V 类型并逐键解析。\n * 用于规避 fastjson issue #3730。\n */",
    ),
    (
        "    public static final GenericMapSuperclassDeserializer INSTANCE = new GenericMapSuperclassDeserializer();",
        "    /** 单例实例。 */\n    public static final GenericMapSuperclassDeserializer INSTANCE = new GenericMapSuperclassDeserializer();",
    ),
    (
        "    @SuppressWarnings({\"unchecked\", \"rawtypes\"})\n    @Override\n    public Object readObject(JSONReader reader, Type type, Object fieldName, long features) {",
        "    /**\n     * 反序列化继承自 Map 的自定义类型：无参构造实例化后按泛型参数解析键值对。\n     *\n     * @param reader JSON 读取器\n     * @param type 目标类型\n     * @param fieldName 字段名\n     * @param features 解析特性位\n     * @return 填充后的 Map 实例\n     */\n    @SuppressWarnings({\"unchecked\", \"rawtypes\"})\n    @Override\n    public Object readObject(JSONReader reader, Type type, Object fieldName, long features) {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/filter/ExpressionType.java"] = [
    (
        "public class ExpressionType {",
        "/**\n * 消息过滤表达式类型常量：SQL92 与 TAG 两种订阅过滤语法。\n */\npublic class ExpressionType {",
    ),
    (
        "    /**\n     * <ul>\n     * Keywords:\n     * <li>{@code AND, OR, NOT, BETWEEN, IN, TRUE, FALSE, IS, NULL}</li>\n     * </ul>\n     * <p/>\n     * <ul>\n     * Data type:\n     * <li>Boolean, like: TRUE, FALSE</li>\n     * <li>String, like: 'abc'</li>\n     * <li>Decimal, like: 123</li>\n     * <li>Float number, like: 3.1415</li>\n     * </ul>\n     * <p/>\n     * <ul>\n     * Grammar:\n     * <li>{@code AND, OR}</li>\n     * <li>{@code >, >=, <, <=, =}</li>\n     * <li>{@code BETWEEN A AND B}, equals to {@code >=A AND <=B}</li>\n     * <li>{@code NOT BETWEEN A AND B}, equals to {@code >B OR <A}</li>\n     * <li>{@code IN ('a', 'b')}, equals to {@code ='a' OR ='b'}, this operation only support String type.</li>\n     * <li>{@code IS NULL}, {@code IS NOT NULL}, check parameter whether is null, or not.</li>\n     * <li>{@code =TRUE}, {@code =FALSE}, check parameter whether is true, or false.</li>\n     * </ul>\n     * <p/>\n     * <p>\n     * Example:\n     * (a > 10 AND a < 100) OR (b IS NOT NULL AND b=TRUE)\n     * </p>\n     */",
        "    /**\n     * SQL92 风格过滤表达式。\n     * <ul>\n     * 关键字：\n     * <li>{@code AND, OR, NOT, BETWEEN, IN, TRUE, FALSE, IS, NULL}</li>\n     * </ul>\n     * <p/>\n     * <ul>\n     * 数据类型：\n     * <li>布尔：TRUE、FALSE</li>\n     * <li>字符串：如 {@code 'abc'}</li>\n     * <li>整数：如 123</li>\n     * <li>浮点：如 3.1415</li>\n     * </ul>\n     * <p/>\n     * <ul>\n     * 语法：\n     * <li>{@code AND, OR}</li>\n     * <li>{@code >, >=, <, <=, =}</li>\n     * <li>{@code BETWEEN A AND B} 等价于 {@code >=A AND <=B}</li>\n     * <li>{@code NOT BETWEEN A AND B} 等价于 {@code >B OR <A}</li>\n     * <li>{@code IN ('a', 'b')} 等价于 {@code ='a' OR ='b'}，仅支持字符串</li>\n     * <li>{@code IS NULL}、{@code IS NOT NULL} 判空</li>\n     * <li>{@code =TRUE}、{@code =FALSE} 判布尔</li>\n     * </ul>\n     * <p/>\n     * <p>\n     * 示例：{@code (a > 10 AND a < 100) OR (b IS NOT NULL AND b=TRUE)}\n     * </p>\n     */",
    ),
    (
        "    /**\n     * Only support or operation such as\n     * \"tag1 || tag2 || tag3\", <br>\n     * If null or * expression,meaning subscribe all.\n     */",
        "    /**\n     * TAG 过滤：仅支持 {@code ||} 或运算，如 {@code tag1 || tag2 || tag3}；\n     * null 或 {@code *} 表示订阅全部。\n     */",
    ),
    (
        "    public static boolean isTagType(String type) {",
        "    /**\n     * 判断表达式类型是否为 TAG（null、空串或 {@link #TAG} 均视为 TAG）。\n     *\n     * @param type 表达式类型字符串\n     * @return 是否为 TAG 类型\n     */\n    public static boolean isTagType(String type) {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/filter/FilterContext.java"] = [
    (
        "public class FilterContext {",
        "/**\n * 消息过滤上下文：向 {@link MessageFilter} 传递消费侧附加信息。\n */\npublic class FilterContext {",
    ),
    (
        "    private String consumerGroup;",
        "    /** 当前消费组名。 */\n    private String consumerGroup;",
    ),
    (
        "    public String getConsumerGroup() {",
        "    /** 返回消费组名。 */\n    public String getConsumerGroup() {",
    ),
    (
        "    public void setConsumerGroup(String consumerGroup) {",
        "    /** 设置消费组名。 */\n    public void setConsumerGroup(String consumerGroup) {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/filter/MessageFilter.java"] = [
    (
        "public interface MessageFilter {",
        "/**\n * 消息过滤 SPI：Broker/Consumer 按订阅表达式判定消息是否匹配。\n */\npublic interface MessageFilter {",
    ),
    (
        "    boolean match(final MessageExt msg, final FilterContext context);",
        "    /**\n     * 判断消息是否通过过滤。\n     *\n     * @param msg 待检消息\n     * @param context 过滤上下文（含消费组等）\n     * @return true 表示匹配订阅条件\n     */\n    boolean match(final MessageExt msg, final FilterContext context);",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/filter/impl/Op.java"] = [
    (
        "public abstract class Op {",
        "/**\n * 过滤表达式 AST 节点基类：操作数与运算符均继承此类。\n */\npublic abstract class Op {",
    ),
    (
        "    private String symbol;",
        "    /** 符号字面量（如 {@code &&}、{@code tag1}）。 */\n    private String symbol;",
    ),
    (
        "    protected Op(String symbol) {",
        "    /** @param symbol 符号字符串 */\n    protected Op(String symbol) {",
    ),
    (
        "    public String getSymbol() {",
        "    /** 返回符号字面量。 */\n    public String getSymbol() {",
    ),
    (
        "    public String toString() {",
        "    /** 返回符号字符串表示。 */\n    public String toString() {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/filter/impl/Operand.java"] = [
    (
        "public class Operand extends Op {",
        "/**\n * 过滤表达式操作数（如 Tag 名、字面量）。\n */\npublic class Operand extends Op {",
    ),
    (
        "    public Operand(String symbol) {",
        "    /** @param symbol 操作数符号 */\n    public Operand(String symbol) {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/filter/impl/Operator.java"] = [
    (
        "public class Operator extends Op {",
        "/**\n * TAG 过滤表达式运算符：括号、逻辑与/或，含优先级与可比较性。\n */\npublic class Operator extends Op {",
    ),
    (
        "    public static final Operator LEFTPARENTHESIS = new Operator(\"(\", 30, false);",
        "    /** 左括号。 */\n    public static final Operator LEFTPARENTHESIS = new Operator(\"(\", 30, false);",
    ),
    (
        "    public static final Operator RIGHTPARENTHESIS = new Operator(\")\", 30, false);",
        "    /** 右括号。 */\n    public static final Operator RIGHTPARENTHESIS = new Operator(\")\", 30, false);",
    ),
    (
        "    public static final Operator AND = new Operator(\"&&\", 20, true);",
        "    /** 逻辑与 {@code &&}。 */\n    public static final Operator AND = new Operator(\"&&\", 20, true);",
    ),
    (
        "    public static final Operator OR = new Operator(\"||\", 15, true);",
        "    /** 逻辑或 {@code ||}。 */\n    public static final Operator OR = new Operator(\"||\", 15, true);",
    ),
    (
        "    private int priority;",
        "    /** 运算符优先级（数值越大优先级越高）。 */\n    private int priority;",
    ),
    (
        "    private boolean compareable;",
        "    /** 是否参与优先级比较（括号不参与）。 */\n    private boolean compareable;",
    ),
    (
        "    private Operator(String symbol, int priority, boolean compareable) {",
        "    /** @param symbol 符号 @param priority 优先级 @param compareable 是否可比较 */\n    private Operator(String symbol, int priority, boolean compareable) {",
    ),
    (
        "    public static Operator createOperator(String operator) {",
        "    /**\n     * 按符号字符串创建预定义运算符实例。\n     *\n     * @param operator 运算符符号\n     * @return 对应 {@link Operator}\n     * @throws IllegalArgumentException 不支持的运算符\n     */\n    public static Operator createOperator(String operator) {",
    ),
    (
        "    public int getPriority() {",
        "    /** 返回运算符优先级。 */\n    public int getPriority() {",
    ),
    (
        "    public boolean isCompareable() {",
        "    /** 返回是否参与优先级比较。 */\n    public boolean isCompareable() {",
    ),
    (
        "    public int compare(Operator operator) {",
        "    /**\n     * 与另一运算符比较优先级。\n     *\n     * @param operator 待比较运算符\n     * @return 正数表示本运算符优先级更高\n     */\n    public int compare(Operator operator) {",
    ),
    (
        "    public boolean isSpecifiedOp(String operator) {",
        "    /** 判断本运算符符号是否与给定字符串相同。 */\n    public boolean isSpecifiedOp(String operator) {",
    ),
]
