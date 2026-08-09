"""RocketMQ 5.5.0 wave15a common message/metrics [0:15] replacements."""

R: dict[str, list[tuple[str, str]]] = {}

R["common/src/main/java/org/apache/rocketmq/common/message/MessageBatch.java"] = [
    (
        "public class MessageBatch extends Message implements Iterable<Message> {",
        "/**\n * 批量消息容器：将多条 {@link Message} 合并为一次发送，\n * 要求同 Topic、同 waitStoreMsgOK，且不支持延迟与重试 Topic。\n */\npublic class MessageBatch extends Message implements Iterable<Message> {",
    ),
    (
        "    private final List<Message> messages;",
        "    /** 批量内各条消息列表。 */\n    private final List<Message> messages;",
    ),
    (
        "    public MessageBatch(List<Message> messages) {",
        "    /** 以消息列表构造批量对象。 */\n    public MessageBatch(List<Message> messages) {",
    ),
    (
        "    public byte[] encode() {",
        "    /** 将批量消息编码为字节数组。 */\n    public byte[] encode() {",
    ),
    (
        "    public Iterator<Message> iterator() {",
        "    /** 遍历批量内各条消息。 */\n    public Iterator<Message> iterator() {",
    ),
    (
        "    public static MessageBatch generateFromList(Collection<? extends Message> messages) {",
        "    /**\n     * 从消息集合生成 {@link MessageBatch}，校验 Topic、延迟与重试约束。\n     *\n     * @throws UnsupportedOperationException 含延迟/重试 Topic 或 Topic 不一致\n     */",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/message/MessageClientExt.java"] = [
    (
        "public class MessageClientExt extends MessageExt {",
        "/**\n * 客户端侧 {@link MessageExt}：{@link #getMsgId()} 优先返回客户端唯一 ID（UNIQ_KEY），\n * 无 UNIQ_KEY 时回退 Broker 偏移 msgId。\n */\npublic class MessageClientExt extends MessageExt {",
    ),
    (
        "    public String getOffsetMsgId() {",
        "    /** 返回 Broker 侧基于 CommitLog 偏移的 msgId。 */\n    public String getOffsetMsgId() {",
    ),
    (
        "    public void setOffsetMsgId(String offsetMsgId) {",
        "    /** 设置 Broker 偏移 msgId。 */\n    public void setOffsetMsgId(String offsetMsgId) {",
    ),
    (
        "    @Override\n    public String getMsgId() {",
        "    /** 优先返回 {@link MessageClientIDSetter} 生成的 UNIQ_KEY，否则为 offsetMsgId。 */\n    @Override\n    public String getMsgId() {",
    ),
    (
        "    public void setMsgId(String msgId) {",
        "    /** 客户端 msgId 由 UNIQ_KEY 决定，此方法 intentionally 空实现。 */\n    public void setMsgId(String msgId) {",
    ),
    (
        "        //DO NOTHING",
        "        // 客户端 msgId 由 UNIQ_KEY 属性维护，此处不写入",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/message/MessageClientIDSetter.java"] = [
    (
        "public class MessageClientIDSetter {",
        "/**\n * 客户端消息唯一 ID（UNIQ_KEY）生成与解析：\n * 编码 IP、PID、ClassLoader、月初时间差与递增计数器。\n */\npublic class MessageClientIDSetter {",
    ),
    (
        "    private static final int LEN;",
        "    /** UNIQ_KEY 字符长度（IP + PID + hash + 时间差 + 计数）。 */\n    private static final int LEN;",
    ),
    (
        "    private static final char[] FIX_STRING;",
        "    /** 固定前缀（IP、PID、ClassLoader hash 编码）。 */\n    private static final char[] FIX_STRING;",
    ),
    (
        "    private static final AtomicInteger COUNTER;",
        "    /** 同进程内递增序号，防碰撞。 */\n    private static final AtomicInteger COUNTER;",
    ),
    (
        "    private static long startTime;",
        "    /** 当前月起始毫秒时间戳。 */\n    private static long startTime;",
    ),
    (
        "    private static long nextStartTime;",
        "    /** 下月起始时间，用于跨月重置 startTime。 */\n    private static long nextStartTime;",
    ),
    (
        "    private synchronized static void setStartTime(long millis) {",
        "    /** 将 startTime 对齐到 millis 所在月 1 日 0 点，并计算 nextStartTime。 */\n    private synchronized static void setStartTime(long millis) {",
    ),
    (
        "    public static Date getNearlyTimeFromID(String msgID) {",
        "    /** 从 msgID 中解析近似生成时间（月初 + 编码时间差）。 */\n    public static Date getNearlyTimeFromID(String msgID) {",
    ),
    (
        "    public static String getIPStrFromID(String msgID) {",
        "    /** 从 msgID 解析发送端 IP 字符串（IPv4/IPv6）。 */\n    public static String getIPStrFromID(String msgID) {",
    ),
    (
        "    public static byte[] getIPFromID(String msgID) {",
        "    /** 从 msgID 提取 IP 原始字节。 */\n    public static byte[] getIPFromID(String msgID) {",
    ),
    (
        "    public static int getPidFromID(String msgID) {",
        "    /** 从 msgID 解析发送进程 PID。 */\n    public static int getPidFromID(String msgID) {",
    ),
    (
        "    public static String createUniqID() {",
        "    /** 生成新的客户端唯一 ID 字符串。 */\n    public static String createUniqID() {",
    ),
    (
        "            // may cause by NTP",
        "            // NTP 回拨可能导致 diff 为负，归零处理",
    ),
    (
        "    public static void setUniqID(final Message msg) {",
        "    /** 若消息尚无 UNIQ_KEY 属性，则写入 {@link #createUniqID()}。 */\n    public static void setUniqID(final Message msg) {",
    ),
    (
        "    public static String getUniqID(final Message msg) {",
        "    /** 读取消息 UNIQ_KEY 属性值。 */\n    public static String getUniqID(final Message msg) {",
    ),
    (
        "    public static byte[] createFakeIP() {",
        "    /** 无法获取真实 IP 时，用当前时间戳生成 4 字节占位 IP。 */\n    public static byte[] createFakeIP() {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/message/MessageConst.java"] = [
    (
        "public class MessageConst {",
        "/**\n * 消息系统属性键名常量：KEYS、TAGS、延迟、事务、POP、定时、DLQ 等。\n * 以 {@link #PROPERTY_TRANSIENT_PREFIX} 开头的属性不落盘。\n */\npublic class MessageConst {",
    ),
    (
        "    public static final String PROPERTY_KEYS = \"KEYS\";",
        "    /** 消息业务键（索引/过滤）。 */\n    public static final String PROPERTY_KEYS = \"KEYS\";",
    ),
    (
        "    public static final String PROPERTY_TAGS = \"TAGS\";",
        "    /** 消息标签。 */\n    public static final String PROPERTY_TAGS = \"TAGS\";",
    ),
    (
        "    public static final String PROPERTY_WAIT_STORE_MSG_OK = \"WAIT\";",
        "    /** 是否等待 Broker 存储确认。 */\n    public static final String PROPERTY_WAIT_STORE_MSG_OK = \"WAIT\";",
    ),
    (
        "    public static final String PROPERTY_DELAY_TIME_LEVEL = \"DELAY\";",
        "    /** 延迟级别（旧版延迟消息）。 */\n    public static final String PROPERTY_DELAY_TIME_LEVEL = \"DELAY\";",
    ),
    (
        "    public static final String PROPERTY_RETRY_TOPIC = \"RETRY_TOPIC\";",
        "    /** 重试 Topic 名。 */\n    public static final String PROPERTY_RETRY_TOPIC = \"RETRY_TOPIC\";",
    ),
    (
        "    public static final String PROPERTY_UNIQ_CLIENT_MESSAGE_ID_KEYIDX = \"UNIQ_KEY\";",
        "    /** 客户端唯一消息 ID。 */\n    public static final String PROPERTY_UNIQ_CLIENT_MESSAGE_ID_KEYIDX = \"UNIQ_KEY\";",
    ),
    (
        "    public static final String PROPERTY_TRANSACTION_PREPARED = \"TRAN_MSG\";",
        "    /** 半事务消息标记。 */\n    public static final String PROPERTY_TRANSACTION_PREPARED = \"TRAN_MSG\";",
    ),
    (
        "    public static final String PROPERTY_TRANSACTION_ID = \"__transactionId__\";",
        "    /** 事务 ID。 */\n    public static final String PROPERTY_TRANSACTION_ID = \"__transactionId__\";",
    ),
    (
        "    public static final String PROPERTY_RECONSUME_TIME = \"RECONSUME_TIME\";",
        "    /** 当前重试消费次数。 */\n    public static final String PROPERTY_RECONSUME_TIME = \"RECONSUME_TIME\";",
    ),
    (
        "    public static final String PROPERTY_TRACE_SWITCH = \"TRACE_ON\";",
        "    /** 消息轨迹开关。 */\n    public static final String PROPERTY_TRACE_SWITCH = \"TRACE_ON\";",
    ),
    (
        "    public static final String PROPERTY_POP_CK = \"POP_CK\";",
        "    /** POP 模式 checkpoint。 */\n    public static final String PROPERTY_POP_CK = \"POP_CK\";",
    ),
    (
        "    public static final String PROPERTY_SHARDING_KEY = \"__SHARDINGKEY\";",
        "    /** 顺序/分片路由键。 */\n    public static final String PROPERTY_SHARDING_KEY = \"__SHARDINGKEY\";",
    ),
    (
        "    public static final String PROPERTY_TIMER_DELAY_SEC = \"TIMER_DELAY_SEC\";",
        "    /** 定时消息延迟秒数。 */\n    public static final String PROPERTY_TIMER_DELAY_SEC = \"TIMER_DELAY_SEC\";",
    ),
    (
        "    public static final String PROPERTY_TIMER_DELIVER_MS = \"TIMER_DELIVER_MS\";",
        "    /** 定时消息绝对投递毫秒时间戳。 */\n    public static final String PROPERTY_TIMER_DELIVER_MS = \"TIMER_DELIVER_MS\";",
    ),
    (
        "    /**\n     * property which name starts with \"__RMQ.TRANSIENT.\" is called transient one that will not stored in broker disks.\n     */",
        "    /** 以此前缀开头的属性为瞬态属性，Broker 不落盘。 */",
    ),
    (
        "    public static final String PROPERTY_TRANSIENT_PREFIX = \"__RMQ.TRANSIENT.\";",
        "    /** 瞬态属性键前缀。 */\n    public static final String PROPERTY_TRANSIENT_PREFIX = \"__RMQ.TRANSIENT.\";",
    ),
    (
        "    /**\n     * the transient property key of topicSysFlag (set by client when pulling messages)\n     */",
        "    /** 拉取时客户端写入的 Topic 系统标志瞬态键。 */",
    ),
    (
        "    public static final String PROPERTY_TRANSIENT_TOPIC_CONFIG = PROPERTY_TRANSIENT_PREFIX + \"TOPIC_SYS_FLAG\";",
        "    /** Topic sysFlag 瞬态属性键。 */\n    public static final String PROPERTY_TRANSIENT_TOPIC_CONFIG = PROPERTY_TRANSIENT_PREFIX + \"TOPIC_SYS_FLAG\";",
    ),
    (
        "    /**\n     * the transient property key of groupSysFlag (set by client when pulling messages)\n     */",
        "    /** 拉取时客户端写入的消费组系统标志瞬态键。 */",
    ),
    (
        "    public static final String PROPERTY_TRANSIENT_GROUP_CONFIG = PROPERTY_TRANSIENT_PREFIX + \"GROUP_SYS_FLAG\";",
        "    /** 消费组 sysFlag 瞬态属性键。 */\n    public static final String PROPERTY_TRANSIENT_GROUP_CONFIG = PROPERTY_TRANSIENT_PREFIX + \"GROUP_SYS_FLAG\";",
    ),
    (
        "    public static final HashSet<String> STRING_HASH_SET = new HashSet<>(64);",
        "    /** 已知系统属性键集合，用于校验与索引。 */\n    public static final HashSet<String> STRING_HASH_SET = new HashSet<>(64);",
    ),
    (
        "    /**\n     * properties for DLQ\n     */",
        "    /** 死信队列（DLQ）相关属性。 */",
    ),
    (
        "    public static final String PROPERTY_DLQ_ORIGIN_TOPIC = \"DLQ_ORIGIN_TOPIC\";",
        "    /** DLQ 消息原始 Topic。 */\n    public static final String PROPERTY_DLQ_ORIGIN_TOPIC = \"DLQ_ORIGIN_TOPIC\";",
    ),
    (
        "    public static final String PROPERTY_DLQ_ORIGIN_MESSAGE_ID = \"DLQ_ORIGIN_MESSAGE_ID\";",
        "    /** DLQ 消息原始 msgId。 */\n    public static final String PROPERTY_DLQ_ORIGIN_MESSAGE_ID = \"DLQ_ORIGIN_MESSAGE_ID\";",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/message/MessageExt.java"] = [
    (
        "public class MessageExt extends Message {",
        "/**\n * Broker 存储侧扩展消息：队列、Broker 名、Born/Store 主机与时间、\n * CommitLog 偏移、重试次数及事务 prepared 偏移等。\n */\npublic class MessageExt extends Message {",
    ),
    (
        "    private String brokerName;",
        "    /** 消息所在 Broker 名称。 */\n    private String brokerName;",
    ),
    (
        "    private int queueId;",
        "    /** 队列 ID。 */\n    private int queueId;",
    ),
    (
        "    private int storeSize;",
        "    /** CommitLog 中序列化后的存储大小。 */\n    private int storeSize;",
    ),
    (
        "    private long queueOffset;",
        "    /** 队列逻辑偏移。 */\n    private long queueOffset;",
    ),
    (
        "    private int sysFlag;",
        "    /** 消息系统标志（IPv6、多 Tag 等）。 */\n    private int sysFlag;",
    ),
    (
        "    private long bornTimestamp;",
        "    /** 消息产生时间戳。 */\n    private long bornTimestamp;",
    ),
    (
        "    private SocketAddress bornHost;",
        "    /** 消息产生主机地址。 */\n    private SocketAddress bornHost;",
    ),
    (
        "    private long storeTimestamp;",
        "    /** Broker 存储时间戳。 */\n    private long storeTimestamp;",
    ),
    (
        "    private SocketAddress storeHost;",
        "    /** 存储 Broker 主机地址。 */\n    private SocketAddress storeHost;",
    ),
    (
        "    private String msgId;",
        "    /** Broker 生成的 msgId（IP + 端口 + 偏移）。 */\n    private String msgId;",
    ),
    (
        "    private long commitLogOffset;",
        "    /** CommitLog 物理偏移。 */\n    private long commitLogOffset;",
    ),
    (
        "    private int bodyCRC;",
        "    /** 消息体 CRC 校验值。 */\n    private int bodyCRC;",
    ),
    (
        "    private int reconsumeTimes;",
        "    /** 重试消费次数。 */\n    private int reconsumeTimes;",
    ),
    (
        "    private long preparedTransactionOffset;",
        "    /** 半事务消息 prepared 偏移。 */\n    private long preparedTransactionOffset;",
    ),
    (
        "    public static TopicFilterType parseTopicFilterType(final int sysFlag) {",
        "    /** 根据 sysFlag 解析 Topic 过滤类型（单 Tag / 多 Tag）。 */\n    public static TopicFilterType parseTopicFilterType(final int sysFlag) {",
    ),
    (
        "    public static ByteBuffer socketAddress2ByteBuffer(final SocketAddress socketAddress, final ByteBuffer byteBuffer) {",
        "    /** 将 SocketAddress 写入已有 ByteBuffer（IPv4 4 字节 / IPv6 16 字节 + 端口）。 */\n    public static ByteBuffer socketAddress2ByteBuffer(final SocketAddress socketAddress, final ByteBuffer byteBuffer) {",
    ),
    (
        "    public static ByteBuffer socketAddress2ByteBuffer(SocketAddress socketAddress) {",
        "    /** 分配合适容量并将 SocketAddress 编码为 ByteBuffer。 */\n    public static ByteBuffer socketAddress2ByteBuffer(SocketAddress socketAddress) {",
    ),
    (
        "    public String getBornHostString() {",
        "    /** 返回 bornHost 的 IP 字符串。 */\n    public String getBornHostString() {",
    ),
    (
        "                // without reverse dns lookup",
        "                // 使用 getHostString 避免反向 DNS 查询",
    ),
    (
        "    public void setStoreHostAddressV6Flag() { this.sysFlag = this.sysFlag | MessageSysFlag.STOREHOSTADDRESS_V6_FLAG; }",
        "    /** 标记 storeHost 为 IPv6 地址。 */\n    public void setStoreHostAddressV6Flag() { this.sysFlag = this.sysFlag | MessageSysFlag.STOREHOSTADDRESS_V6_FLAG; }",
    ),
    (
        "    public void setBornHostV6Flag() { this.sysFlag = this.sysFlag | MessageSysFlag.BORNHOST_V6_FLAG; }",
        "    /** 标记 bornHost 为 IPv6 地址。 */\n    public void setBornHostV6Flag() { this.sysFlag = this.sysFlag | MessageSysFlag.BORNHOST_V6_FLAG; }",
    ),
    (
        "    /**\n     *\n     * achieves topicSysFlag value from transient properties\n     *\n     * @return\n     */",
        "    /** 从瞬态属性读取 topicSysFlag。 */",
    ),
    (
        "    public Integer getTopicSysFlag() {",
        "    /** 获取 Topic 系统标志，未设置时返回 null。 */\n    public Integer getTopicSysFlag() {",
    ),
    (
        "    /**\n     * set topicSysFlag to transient properties, or clear it\n     *\n     * @param topicSysFlag\n     */",
        "    /** 写入或清除 topicSysFlag 瞬态属性。 */",
    ),
    (
        "    /**\n     *\n     * achieves groupSysFlag value from transient properties\n     *\n     * @return\n     */",
        "    /** 从瞬态属性读取 groupSysFlag。 */",
    ),
    (
        "    public Integer getGroupSysFlag() {",
        "    /** 获取消费组系统标志，未设置时返回 null。 */\n    public Integer getGroupSysFlag() {",
    ),
    (
        "    /**\n     *\n     * set groupSysFlag to transient properties, or clear it\n     *\n     * @param groupSysFlag\n     */",
        "    /** 写入或清除 groupSysFlag 瞬态属性。 */",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/message/MessageExtBatch.java"] = [
    (
        "public class MessageExtBatch extends MessageExtBrokerInner {",
        "/**\n * Broker 侧批量扩展消息：body 为多条子消息编码，\n * 支持 inner batch（无需 Broker 再拆包）。\n */\npublic class MessageExtBatch extends MessageExtBrokerInner {",
    ),
    (
        "    /**\n     * Inner batch means the batch does not need to be unwrapped\n     */",
        "    /** true 表示 inner batch，Broker 无需再拆包。 */",
    ),
    (
        "    private boolean isInnerBatch = false;",
        "    /** 是否为 inner batch 模式。 */\n    private boolean isInnerBatch = false;",
    ),
    (
        "    public ByteBuffer wrap() {",
        "    /** 将消息体 body 包装为 ByteBuffer 视图。 */\n    public ByteBuffer wrap() {",
    ),
    (
        "    public boolean isInnerBatch() {",
        "    /** 是否 inner batch。 */\n    public boolean isInnerBatch() {",
    ),
    (
        "    public void setInnerBatch(boolean innerBatch) {",
        "    /** 设置 inner batch 标志。 */\n    public void setInnerBatch(boolean innerBatch) {",
    ),
    (
        "    private ByteBuffer encodedBuff;",
        "    /** 预编码后的 ByteBuffer 缓存。 */\n    private ByteBuffer encodedBuff;",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/message/MessageExtBrokerInner.java"] = [
    (
        "public class MessageExtBrokerInner extends MessageExt {",
        "/**\n * Broker 内部写入 CommitLog 前的消息：含 properties 字符串、tagsCode、\n * 编码缓存及 {@link MessageVersion} 等 Broker 专用字段。\n */\npublic class MessageExtBrokerInner extends MessageExt {",
    ),
    (
        "    private String propertiesString;",
        "    /** 属性键值对序列化字符串（落盘格式）。 */\n    private String propertiesString;",
    ),
    (
        "    private long tagsCode;",
        "    /** 标签 hashCode，用于索引过滤。 */\n    private long tagsCode;",
    ),
    (
        "    private ByteBuffer encodedBuff;",
        "    /** 编码后的 ByteBuffer 缓存。 */\n    private ByteBuffer encodedBuff;",
    ),
    (
        "    private volatile boolean encodeCompleted;",
        "    /** 是否已完成编码。 */\n    private volatile boolean encodeCompleted;",
    ),
    (
        "    private MessageVersion version = MessageVersion.MESSAGE_VERSION_V1;",
        "    /** 消息协议版本，默认 V1。 */\n    private MessageVersion version = MessageVersion.MESSAGE_VERSION_V1;",
    ),
    (
        "    public static long tagsString2tagsCode(final TopicFilterType filter, final String tags) {",
        "    /** 将 tags 字符串转为 tagsCode（空串为 0）。 */\n    public static long tagsString2tagsCode(final TopicFilterType filter, final String tags) {",
    ),
    (
        "    public void deleteProperty(String name) {",
        "    /** 删除属性并同步更新 propertiesString。 */\n    public void deleteProperty(String name) {",
    ),
    (
        "    public void removeWaitStorePropertyString() {",
        "    /** 从 propertiesString 中移除 WAIT 以节省存储，properties Map 仍保留 WAIT。 */\n    public void removeWaitStorePropertyString() {",
    ),
    (
        "            // There is no need to store \"WAIT=true\", remove it from propertiesString to save 9 bytes for each message.",
        "            // WAIT=true 无需落盘，从 propertiesString 移除以省 9 字节",
    ),
    (
        "            // Reput to properties, since msgInner.isWaitStoreMsgOK() will be invoked later",
        "            // 仍写回 properties，后续 isWaitStoreMsgOK() 会读取",
    ),
    (
        "    public boolean needDispatchLMQ() {",
        "    /** 是否需向 LMQ（逻辑多队列）分发。 */\n    public boolean needDispatchLMQ() {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/message/MessageId.java"] = [
    (
        "public class MessageId {",
        "/**\n * CommitLog 物理地址标识：Broker 主机 + CommitLog 偏移。\n */\npublic class MessageId {",
    ),
    (
        "    private SocketAddress address;",
        "    /** Broker 主机地址。 */\n    private SocketAddress address;",
    ),
    (
        "    private long offset;",
        "    /** CommitLog 物理偏移。 */\n    private long offset;",
    ),
    (
        "    public MessageId(SocketAddress address, long offset) {",
        "    /** 构造物理 msgId 元组。 */\n    public MessageId(SocketAddress address, long offset) {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/message/MessageQueue.java"] = [
    (
        "public class MessageQueue implements Comparable<MessageQueue>, Serializable {",
        "/**\n * 逻辑队列三元组：Topic + Broker 名 + queueId，\n * 用于路由、消费分配与 offset 管理。\n */\npublic class MessageQueue implements Comparable<MessageQueue>, Serializable {",
    ),
    (
        "    private String topic;",
        "    /** Topic 名。 */\n    private String topic;",
    ),
    (
        "    private String brokerName;",
        "    /** Broker 名称。 */\n    private String brokerName;",
    ),
    (
        "    private int queueId;",
        "    /** 队列编号。 */\n    private int queueId;",
    ),
    (
        "    public MessageQueue(MessageQueue other) {",
        "    /** 拷贝构造。 */\n    public MessageQueue(MessageQueue other) {",
    ),
    (
        "    public MessageQueue(String topic, String brokerName, int queueId) {",
        "    /** 指定 Topic、Broker 与 queueId 构造。 */\n    public MessageQueue(String topic, String brokerName, int queueId) {",
    ),
    (
        "    @Override\n    public int compareTo(MessageQueue o) {",
        "    /** 按 topic、brokerName、queueId 字典序比较。 */\n    @Override\n    public int compareTo(MessageQueue o) {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/message/MessageQueueAssignment.java"] = [
    (
        "public class MessageQueueAssignment implements Serializable {",
        "/**\n * 队列分配结果：绑定 {@link MessageQueue}、请求模式（PULL/POP）及扩展附件。\n */\npublic class MessageQueueAssignment implements Serializable {",
    ),
    (
        "    private MessageQueue messageQueue;",
        "    /** 分配的队列。 */\n    private MessageQueue messageQueue;",
    ),
    (
        "    private MessageRequestMode mode = MessageRequestMode.PULL;",
        "    /** 消费请求模式，默认 PULL。 */\n    private MessageRequestMode mode = MessageRequestMode.PULL;",
    ),
    (
        "    private Map<String, String> attachments;",
        "    /** 扩展附件键值对。 */\n    private Map<String, String> attachments;",
    ),
    (
        "    @Override\n    public boolean equals(Object obj) {",
        "    /** 相等性仅比较 messageQueue。 */\n    @Override\n    public boolean equals(Object obj) {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/message/MessageQueueForC.java"] = [
    (
        "public class MessageQueueForC implements Comparable<MessageQueueForC>, Serializable {",
        "/**\n * 带消费偏移的队列描述，供 C 端客户端或跨语言协议使用。\n */\npublic class MessageQueueForC implements Comparable<MessageQueueForC>, Serializable {",
    ),
    (
        "    private String topic;",
        "    /** Topic 名。 */\n    private String topic;",
    ),
    (
        "    private String brokerName;",
        "    /** Broker 名称。 */\n    private String brokerName;",
    ),
    (
        "    private int queueId;",
        "    /** 队列编号。 */\n    private int queueId;",
    ),
    (
        "    private long offset;",
        "    /** 当前消费偏移。 */\n    private long offset;",
    ),
    (
        "    public MessageQueueForC(String topic, String brokerName, int queueId, long offset) {",
        "    /** 构造带偏移的队列描述。 */\n    public MessageQueueForC(String topic, String brokerName, int queueId, long offset) {",
    ),
    (
        "    @Override\n    public int compareTo(MessageQueueForC o) {",
        "    /** 按 topic、brokerName、queueId、offset 比较。 */\n    @Override\n    public int compareTo(MessageQueueForC o) {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/message/MessageRequestMode.java"] = [
    (
        "/**\n * Message Request Mode\n */",
        "/**\n * 消息拉取请求模式：PULL 传统拉取，POP 共享队列的 Pop 消费。\n */",
    ),
    (
        "    /**\n     * pull\n     */",
        "    /** 传统 Pull 拉取模式。 */",
    ),
    (
        "    PULL(\"PULL\"),",
        "    /** Pull 拉取。 */\n    PULL(\"PULL\"),",
    ),
    (
        "    /**\n     * pop, consumer working in pop mode could share MessageQueue\n     */",
        "    /** Pop 模式：多消费者可共享同一 MessageQueue。 */",
    ),
    (
        "    POP(\"POP\");",
        "    /** Pop 消费。 */\n    POP(\"POP\");",
    ),
    (
        "    private String name;",
        "    /** 模式名称字符串。 */\n    private String name;",
    ),
    (
        "    public String getName() {",
        "    /** 返回模式名。 */\n    public String getName() {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/message/MessageType.java"] = [
    (
        "public enum MessageType {",
        "/**\n * 消息类型枚举：普通、事务半消息/提交、延迟、顺序等。\n */\npublic enum MessageType {",
    ),
    (
        "    Normal_Msg(\"Normal\"),",
        "    /** 普通消息。 */\n    Normal_Msg(\"Normal\"),",
    ),
    (
        "    Trans_Msg_Half(\"Trans\"),",
        "    /** 事务半消息。 */\n    Trans_Msg_Half(\"Trans\"),",
    ),
    (
        "    Trans_msg_Commit(\"TransCommit\"),",
        "    /** 事务提交消息。 */\n    Trans_msg_Commit(\"TransCommit\"),",
    ),
    (
        "    Delay_Msg(\"Delay\"),",
        "    /** 延迟/定时消息。 */\n    Delay_Msg(\"Delay\"),",
    ),
    (
        "    Order_Msg(\"Order\");",
        "    /** 顺序消息。 */\n    Order_Msg(\"Order\");",
    ),
    (
        "    private final String shortName;",
        "    /** 短名称字符串。 */\n    private final String shortName;",
    ),
    (
        "    public static MessageType getByShortName(String shortName) {",
        "    /** 按 shortName 查找类型，未匹配时返回 {@link #Normal_Msg}。 */\n    public static MessageType getByShortName(String shortName) {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/message/MessageVersion.java"] = [
    (
        "public enum MessageVersion {",
        "/**\n * CommitLog 消息编码版本：V1 topic 长度 1 字节，V2 为 2 字节 short。\n */\npublic enum MessageVersion {",
    ),
    (
        "    MESSAGE_VERSION_V1(MessageDecoder.MESSAGE_MAGIC_CODE) {",
        "    /** V1：magic {@link MessageDecoder#MESSAGE_MAGIC_CODE}，topic 长度 1 字节。 */\n    MESSAGE_VERSION_V1(MessageDecoder.MESSAGE_MAGIC_CODE) {",
    ),
    (
        "        public int getTopicLengthSize() {",
        "        /** V1 topic 长度字段占 1 字节。 */\n        public int getTopicLengthSize() {",
    ),
    (
        "    MESSAGE_VERSION_V2(MessageDecoder.MESSAGE_MAGIC_CODE_V2) {",
        "    /** V2：magic {@link MessageDecoder#MESSAGE_MAGIC_CODE_V2}，topic 长度 2 字节。 */\n    MESSAGE_VERSION_V2(MessageDecoder.MESSAGE_MAGIC_CODE_V2) {",
    ),
    (
        "        public int getTopicLengthSize() {",
        "        /** V2 topic 长度字段占 2 字节。 */\n        public int getTopicLengthSize() {",
    ),
    (
        "    private final int magicCode;",
        "    /** 协议 magic 码。 */\n    private final int magicCode;",
    ),
    (
        "    public static MessageVersion valueOfMagicCode(int magicCode) {",
        "    /** 按 magic 码解析版本，无效时抛 {@link IllegalArgumentException}。 */\n    public static MessageVersion valueOfMagicCode(int magicCode) {",
    ),
    (
        "    public abstract int getTopicLengthSize();",
        "    /** topic 长度字段字节数。 */\n    public abstract int getTopicLengthSize();",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/metrics/MetricsExporterType.java"] = [
    (
        "public enum MetricsExporterType {",
        "/**\n * 指标导出器类型：DISABLE 关闭，OTLP gRPC、Prometheus、日志等。\n */\npublic enum MetricsExporterType {",
    ),
    (
        "    DISABLE(0),",
        "    /** 关闭指标导出。 */\n    DISABLE(0),",
    ),
    (
        "    OTLP_GRPC(1),",
        "    /** OpenTelemetry OTLP gRPC 导出。 */\n    OTLP_GRPC(1),",
    ),
    (
        "    PROM(2),",
        "    /** Prometheus 格式导出。 */\n    PROM(2),",
    ),
    (
        "    LOG(3);",
        "    /** 日志输出指标。 */\n    LOG(3);",
    ),
    (
        "    private final int value;",
        "    /** 配置整型值。 */\n    private final int value;",
    ),
    (
        "    public static MetricsExporterType valueOf(int value) {",
        "    /** 按整型值解析导出器类型，未知值返回 DISABLE。 */\n    public static MetricsExporterType valueOf(int value) {",
    ),
    (
        "    public boolean isEnable() {",
        "    /** 是否启用导出（value > 0）。 */\n    public boolean isEnable() {",
    ),
]
