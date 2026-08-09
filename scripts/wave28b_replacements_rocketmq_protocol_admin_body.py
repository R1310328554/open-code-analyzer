"""Chinese JavaDoc replacements for RocketMQ wave28b protocol/admin/body [15:30]."""

R: dict[str, list[tuple[str, str]]] = {
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/ResponseCode.java": [
        (
            "public class ResponseCode extends RemotingSysResponseCode {",
            "/**\n * Remoting 业务响应码：在 {@link RemotingSysResponseCode} 系统码之上扩展 Broker/Consumer/Controller 等场景。\n */\npublic class ResponseCode extends RemotingSysResponseCode {",
        ),
        (
            "    public static final int FLUSH_DISK_TIMEOUT = 10;",
            "    /** 刷盘超时。 */\n    public static final int FLUSH_DISK_TIMEOUT = 10;",
        ),
        (
            "    public static final int SLAVE_NOT_AVAILABLE = 11;",
            "    /** 从节点不可用。 */\n    public static final int SLAVE_NOT_AVAILABLE = 11;",
        ),
        (
            "    public static final int MESSAGE_ILLEGAL = 13;",
            "    /** 消息非法（格式或属性校验失败）。 */\n    public static final int MESSAGE_ILLEGAL = 13;",
        ),
        (
            "    public static final int TOPIC_NOT_EXIST = 17;",
            "    /** Topic 不存在。 */\n    public static final int TOPIC_NOT_EXIST = 17;",
        ),
        (
            "    public static final int PULL_NOT_FOUND = 19;",
            "    /** Pull 未找到可消费消息。 */\n    public static final int PULL_NOT_FOUND = 19;",
        ),
        (
            "    public static final int INVALID_PARAMETER = 29;",
            "    /** 请求参数无效。 */\n    public static final int INVALID_PARAMETER = 29;",
        ),
        (
            "    public static final int TRANSACTION_SHOULD_COMMIT = 200;",
            "    /** 半消息事务应提交。 */\n    public static final int TRANSACTION_SHOULD_COMMIT = 200;",
        ),
        (
            "    public static final int TRANSACTION_SHOULD_ROLLBACK = 201;",
            "    /** 半消息事务应回滚。 */\n    public static final int TRANSACTION_SHOULD_ROLLBACK = 201;",
        ),
        (
            "    public static final int CONSUMER_NOT_ONLINE = 206;",
            "    /** 消费者不在线。 */\n    public static final int CONSUMER_NOT_ONLINE = 206;",
        ),
        (
            "    public static final int FLOW_CONTROL = 215;",
            "    /** 服务端流控拒绝。 */\n    public static final int FLOW_CONTROL = 215;",
        ),
        (
            "    public static final int NOT_LEADER_FOR_QUEUE = 501;",
            "    /** 当前 Broker 非该队列 Leader。 */\n    public static final int NOT_LEADER_FOR_QUEUE = 501;",
        ),
        (
            "    public static final int RPC_UNKNOWN = -1000;",
            "    /** RPC 未知错误。 */\n    public static final int RPC_UNKNOWN = -1000;",
        ),
        (
            "    public static final int GO_AWAY = 1500;",
            "    /** 服务端要求客户端断开并重连（如优雅下线）。 */\n    public static final int GO_AWAY = 1500;",
        ),
        (
            "    /**\n     * Controller response code\n     */",
            "    /** Controller 模块专用响应码起始段。 */",
        ),
        (
            "    public static final int CONTROLLER_FENCED_MASTER_EPOCH = 2000;",
            "    /** Controller：Master epoch 已被 fencing。 */\n    public static final int CONTROLLER_FENCED_MASTER_EPOCH = 2000;",
        ),
        (
            "    public static final int CONTROLLER_NOT_LEADER = 2007;",
            "    /** Controller：当前节点非 Leader。 */\n    public static final int CONTROLLER_NOT_LEADER = 2007;",
        ),
        (
            "    public static final int LMQ_QUOTA_EXCEEDED = 2017;",
            "    /** 轻量消息队列（LMQ）配额超限。 */\n    public static final int LMQ_QUOTA_EXCEEDED = 2017;",
        ),
        (
            "    public static final int USER_NOT_EXIST = 3001;",
            "    /** ACL 用户不存在。 */\n    public static final int USER_NOT_EXIST = 3001;",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/RocketMQSerializable.java": [
        (
            "public class RocketMQSerializable {",
            "/**\n * RocketMQ 原生二进制 Remoting 头序列化/反序列化工具。\n * 支持 {@link ByteBuf} 零拷贝编码与 {@link ByteBuffer} 数组编码两种路径。\n */\npublic class RocketMQSerializable {",
        ),
        (
            "    private static final Charset CHARSET_UTF8 = StandardCharsets.UTF_8;",
            "    /** Remoting 字符串字段统一 UTF-8 编码。 */\n    private static final Charset CHARSET_UTF8 = StandardCharsets.UTF_8;",
        ),
        (
            "    public static void writeStr(ByteBuf buf, boolean useShortLength, String str) {",
            "    /** 写入长度前缀 UTF-8 字符串；useShortLength 为 true 时长度占 2 字节，否则 4 字节。 */\n    public static void writeStr(ByteBuf buf, boolean useShortLength, String str) {",
        ),
        (
            "    private static String readStr(ByteBuf buf, boolean useShortLength, int limit) throws RemotingCommandException {",
            "    /** 读取长度前缀字符串，超长时抛出 {@link RemotingCommandException}。 */\n    private static String readStr(ByteBuf buf, boolean useShortLength, int limit) throws RemotingCommandException {",
        ),
        (
            "    public static int rocketMQProtocolEncode(RemotingCommand cmd, ByteBuf out) {",
            "    /** 将 Remoting 命令头编码到 ByteBuf，返回写入字节数。 */\n    public static int rocketMQProtocolEncode(RemotingCommand cmd, ByteBuf out) {",
        ),
        (
            "        // int code(~32767)",
            "        // 请求/响应码（short）",
        ),
        (
            "        if (cmd.readCustomHeader() instanceof FastCodesHeader) {",
            "        // FastCodesHeader 走快速二进制编码路径\n        if (cmd.readCustomHeader() instanceof FastCodesHeader) {",
        ),
        (
            "    public static byte[] rocketMQProtocolEncode(RemotingCommand cmd) {",
            "    /** 将 Remoting 命令头编码为 byte[]（非 Netty 路径）。 */\n    public static byte[] rocketMQProtocolEncode(RemotingCommand cmd) {",
        ),
        (
            "    public static byte[] mapSerialize(HashMap<String, String> map) {",
            "    /** 将扩展字段 HashMap 序列化为 keySize+key+valSize+val 二进制块。 */\n    public static byte[] mapSerialize(HashMap<String, String> map) {",
        ),
        (
            "    private static int calTotalLen(int remark, int ext) {",
            "    /** 计算 Remoting 头固定字段 + remark + extFields 的总长度。 */\n    private static int calTotalLen(int remark, int ext) {",
        ),
        (
            "    public static RemotingCommand rocketMQProtocolDecode(final ByteBuf headerBuffer,\n        int headerLen) throws RemotingCommandException {",
            "    /** 从 ByteBuf 解码 Remoting 命令头；headerLen 用于校验 extFields 边界。 */\n    public static RemotingCommand rocketMQProtocolDecode(final ByteBuf headerBuffer,\n        int headerLen) throws RemotingCommandException {",
        ),
        (
            "    public static HashMap<String, String> mapDeserialize(ByteBuf byteBuffer, int len) throws RemotingCommandException {",
            "    /** 反序列化扩展字段 Map，len 为 extFields 段字节长度。 */\n    public static HashMap<String, String> mapDeserialize(ByteBuf byteBuffer, int len) throws RemotingCommandException {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/SerializeType.java": [
        (
            "public enum SerializeType {",
            "/**\n * Remoting 命令头/体的序列化方式标识。\n */\npublic enum SerializeType {",
        ),
        (
            "    JSON((byte) 0),",
            "    /** JSON 文本序列化。 */\n    JSON((byte) 0),",
        ),
        (
            "    ROCKETMQ((byte) 1);",
            "    /** RocketMQ 原生二进制协议。 */\n    ROCKETMQ((byte) 1);",
        ),
        (
            "    private byte code;",
            "    /** 协议层单字节类型码。 */\n    private byte code;",
        ),
        (
            "    SerializeType(byte code) {",
            "    /** 绑定序列化类型码。 */\n    SerializeType(byte code) {",
        ),
        (
            "    public static SerializeType valueOf(byte code) {",
            "    /** 按类型码查找枚举，未知码返回 null。 */\n    public static SerializeType valueOf(byte code) {",
        ),
        (
            "    public byte getCode() {",
            "    /** 返回协议类型码。 */\n    public byte getCode() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/admin/ConsumeStats.java": [
        (
            "public class ConsumeStats extends RemotingSerializable {",
            "/**\n * 消费组消费统计：各 {@link MessageQueue} 的偏移量快照与消费 TPS。\n */\npublic class ConsumeStats extends RemotingSerializable {",
        ),
        (
            "    private Map<MessageQueue, OffsetWrapper> offsetTable = new ConcurrentHashMap<>();",
            "    /** 队列 → 偏移量包装（Broker/Consumer/Pull 进度）。 */\n    private Map<MessageQueue, OffsetWrapper> offsetTable = new ConcurrentHashMap<>();",
        ),
        (
            "    private double consumeTps = 0;",
            "    /** 消费 TPS（条/秒）。 */\n    private double consumeTps = 0;",
        ),
        (
            "    public long computeTotalDiff() {",
            "    /** 汇总 brokerOffset − consumerOffset，衡量消息堆积量。 */\n    public long computeTotalDiff() {",
        ),
        (
            "    public long computeInflightTotalDiff() {",
            "    /** 汇总 pullOffset − consumerOffset，衡量在途未 ack 消息量。 */\n    public long computeInflightTotalDiff() {",
        ),
        (
            "    public Map<MessageQueue, OffsetWrapper> getOffsetTable() {",
            "    /** 返回偏移量表。 */\n    public Map<MessageQueue, OffsetWrapper> getOffsetTable() {",
        ),
        (
            "    public void setOffsetTable(Map<MessageQueue, OffsetWrapper> offsetTable) {",
            "    /** 设置偏移量表。 */\n    public void setOffsetTable(Map<MessageQueue, OffsetWrapper> offsetTable) {",
        ),
        (
            "    public double getConsumeTps() {",
            "    /** 返回消费 TPS。 */\n    public double getConsumeTps() {",
        ),
        (
            "    public void setConsumeTps(double consumeTps) {",
            "    /** 设置消费 TPS。 */\n    public void setConsumeTps(double consumeTps) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/admin/OffsetWrapper.java": [
        (
            "public class OffsetWrapper {",
            "/**\n * 单队列偏移量快照：Broker 最大位点、消费者已提交位点、Pull 位点及更新时间。\n */\npublic class OffsetWrapper {",
        ),
        (
            "    private long brokerOffset;",
            "    /** Broker 端该队列最大逻辑偏移。 */\n    private long brokerOffset;",
        ),
        (
            "    private long consumerOffset;",
            "    /** 消费组已提交消费位点。 */\n    private long consumerOffset;",
        ),
        (
            "    private long pullOffset;",
            "    /** 最近一次 Pull 请求的位点（在途消息上界）。 */\n    private long pullOffset;",
        ),
        (
            "    private long lastTimestamp;",
            "    /** 偏移量最后更新时间戳（毫秒）。 */\n    private long lastTimestamp;",
        ),
        (
            "    public long getBrokerOffset() {",
            "    /** 返回 Broker 偏移。 */\n    public long getBrokerOffset() {",
        ),
        (
            "    public void setBrokerOffset(long brokerOffset) {",
            "    /** 设置 Broker 偏移。 */\n    public void setBrokerOffset(long brokerOffset) {",
        ),
        (
            "    public long getConsumerOffset() {",
            "    /** 返回消费者偏移。 */\n    public long getConsumerOffset() {",
        ),
        (
            "    public void setConsumerOffset(long consumerOffset) {",
            "    /** 设置消费者偏移。 */\n    public void setConsumerOffset(long consumerOffset) {",
        ),
        (
            "    public long getPullOffset() {",
            "    /** 返回 Pull 偏移。 */\n    public long getPullOffset() {",
        ),
        (
            "    public void setPullOffset(long pullOffset) {",
            "    /** 设置 Pull 偏移。 */\n    public void setPullOffset(long pullOffset) {",
        ),
        (
            "    public long getLastTimestamp() {",
            "    /** 返回最后更新时间。 */\n    public long getLastTimestamp() {",
        ),
        (
            "    public void setLastTimestamp(long lastTimestamp) {",
            "    /** 设置最后更新时间。 */\n    public void setLastTimestamp(long lastTimestamp) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/admin/RollbackStats.java": [
        (
            "public class RollbackStats {",
            "/**\n * 按时间戳回退消费位点的统计项：记录 Broker/Consumer 偏移与目标回退位点。\n */\npublic class RollbackStats {",
        ),
        (
            "    private String brokerName;",
            "    /** Broker 名称。 */\n    private String brokerName;",
        ),
        (
            "    private long queueId;",
            "    /** 队列 ID。 */\n    private long queueId;",
        ),
        (
            "    private long brokerOffset;",
            "    /** 回退前 Broker 最大偏移。 */\n    private long brokerOffset;",
        ),
        (
            "    private long consumerOffset;",
            "    /** 回退前消费组偏移。 */\n    private long consumerOffset;",
        ),
        (
            "    private long timestampOffset;",
            "    /** 按时间戳匹配到的目标偏移。 */\n    private long timestampOffset;",
        ),
        (
            "    private long rollbackOffset;",
            "    /** 实际回退到的偏移。 */\n    private long rollbackOffset;",
        ),
        (
            "    public String getBrokerName() {",
            "    /** 返回 Broker 名称。 */\n    public String getBrokerName() {",
        ),
        (
            "    public void setBrokerName(String brokerName) {",
            "    /** 设置 Broker 名称。 */\n    public void setBrokerName(String brokerName) {",
        ),
        (
            "    public long getQueueId() {",
            "    /** 返回队列 ID。 */\n    public long getQueueId() {",
        ),
        (
            "    public void setQueueId(long queueId) {",
            "    /** 设置队列 ID。 */\n    public void setQueueId(long queueId) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/admin/TopicOffset.java": [
        (
            "public class TopicOffset {",
            "/**\n * Topic 在单队列上的偏移范围：最小/最大逻辑偏移及最后更新时间。\n */\npublic class TopicOffset {",
        ),
        (
            "    private long minOffset;",
            "    /** 队列最小可消费偏移。 */\n    private long minOffset;",
        ),
        (
            "    private long maxOffset;",
            "    /** 队列最大已写入偏移（不含）。 */\n    private long maxOffset;",
        ),
        (
            "    private long lastUpdateTimestamp;",
            "    /** 偏移统计最后更新时间（毫秒）。 */\n    private long lastUpdateTimestamp;",
        ),
        (
            "    public long getMinOffset() {",
            "    /** 返回最小偏移。 */\n    public long getMinOffset() {",
        ),
        (
            "    public void setMinOffset(long minOffset) {",
            "    /** 设置最小偏移。 */\n    public void setMinOffset(long minOffset) {",
        ),
        (
            "    public long getMaxOffset() {",
            "    /** 返回最大偏移。 */\n    public long getMaxOffset() {",
        ),
        (
            "    public void setMaxOffset(long maxOffset) {",
            "    /** 设置最大偏移。 */\n    public void setMaxOffset(long maxOffset) {",
        ),
        (
            "    public long getLastUpdateTimestamp() {",
            "    /** 返回最后更新时间。 */\n    public long getLastUpdateTimestamp() {",
        ),
        (
            "    public void setLastUpdateTimestamp(long lastUpdateTimestamp) {",
            "    /** 设置最后更新时间。 */\n    public void setLastUpdateTimestamp(long lastUpdateTimestamp) {",
        ),
        (
            "    @Override\n    public String toString() {",
            "    /** 返回偏移范围的可读字符串。 */\n    @Override\n    public String toString() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/admin/TopicStatsTable.java": [
        (
            "public class TopicStatsTable extends RemotingSerializable {",
            "/**\n * Topic 写入统计表：各队列 {@link TopicOffset} 与 Topic 级写入 TPS。\n */\npublic class TopicStatsTable extends RemotingSerializable {",
        ),
        (
            "    private double topicPutTps;",
            "    /** Topic 写入 TPS（条/秒）。 */\n    private double topicPutTps;",
        ),
        (
            "    private Map<MessageQueue, TopicOffset> offsetTable = new ConcurrentHashMap<>();",
            "    /** 队列 → 偏移范围快照。 */\n    private Map<MessageQueue, TopicOffset> offsetTable = new ConcurrentHashMap<>();",
        ),
        (
            "    public Map<MessageQueue, TopicOffset> getOffsetTable() {",
            "    /** 返回偏移表。 */\n    public Map<MessageQueue, TopicOffset> getOffsetTable() {",
        ),
        (
            "    public void setOffsetTable(Map<MessageQueue, TopicOffset> offsetTable) {",
            "    /** 设置偏移表。 */\n    public void setOffsetTable(Map<MessageQueue, TopicOffset> offsetTable) {",
        ),
        (
            "    public double getTopicPutTps() {",
            "    /** 返回写入 TPS。 */\n    public double getTopicPutTps() {",
        ),
        (
            "    public void setTopicPutTps(double topicPutTps) {",
            "    /** 设置写入 TPS。 */\n    public void setTopicPutTps(double topicPutTps) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/body/AclInfo.java": [
        (
            "public class AclInfo {",
            "/**\n * ACL 访问控制信息：主体（Subject）及其策略列表，用于 Plain ACL 管理接口。\n */\npublic class AclInfo {",
        ),
        (
            "    private String subject;",
            "    /** ACL 主体（用户名或角色）。 */\n    private String subject;",
        ),
        (
            "    private List<PolicyInfo> policies;",
            "    /** 绑定的策略集合。 */\n    private List<PolicyInfo> policies;",
        ),
        (
            "    public static AclInfo of(String subject, List<String> resources, List<String> actions,\n        List<String> sourceIps,\n        String decision) {",
            "    /** 便捷构造：单策略、多资源条目、统一决策（Allow/Deny）。 */\n    public static AclInfo of(String subject, List<String> resources, List<String> actions,\n        List<String> sourceIps,\n        String decision) {",
        ),
        (
            "    public static class PolicyInfo {",
            "    /** 单条 ACL 策略：类型 + 多条资源条目。 */\n    public static class PolicyInfo {",
        ),
        (
            "        private String policyType;",
            "        /** 策略类型标识。 */\n        private String policyType;",
        ),
        (
            "        private List<PolicyEntryInfo> entries;",
            "        /** 资源级策略条目列表。 */\n        private List<PolicyEntryInfo> entries;",
        ),
        (
            "        public static PolicyInfo of(List<String> resources, List<String> actions,\n            List<String> sourceIps, String decision) {",
            "        /** 按资源列表展开为多条 {@link PolicyEntryInfo}。 */\n        public static PolicyInfo of(List<String> resources, List<String> actions,\n            List<String> sourceIps, String decision) {",
        ),
        (
            "    public static class PolicyEntryInfo {",
            "    /** 单资源 ACL 条目：资源、操作、来源 IP 与 Allow/Deny 决策。 */\n    public static class PolicyEntryInfo {",
        ),
        (
            "        private String resource;",
            "        /** 资源名（Topic/Group/Cluster 等）。 */\n        private String resource;",
        ),
        (
            "        private List<String> actions;",
            "        /** 允许的操作列表（Pub/Sub 等）。 */\n        private List<String> actions;",
        ),
        (
            "        private List<String> sourceIps;",
            "        /** 来源 IP 白名单。 */\n        private List<String> sourceIps;",
        ),
        (
            "        private String decision;",
            "        /** 决策：Allow 或 Deny。 */\n        private String decision;",
        ),
        (
            "        public static PolicyEntryInfo of(String resource, List<String> actions, List<String> sourceIps,\n            String decision) {",
            "        /** 构造单条策略条目。 */\n        public static PolicyEntryInfo of(String resource, List<String> actions, List<String> sourceIps,\n            String decision) {",
        ),
        (
            "    public String getSubject() {",
            "    /** 返回 ACL 主体。 */\n    public String getSubject() {",
        ),
        (
            "    public void setSubject(String subject) {",
            "    /** 设置 ACL 主体。 */\n    public void setSubject(String subject) {",
        ),
        (
            "    public List<PolicyInfo> getPolicies() {",
            "    /** 返回策略列表。 */\n    public List<PolicyInfo> getPolicies() {",
        ),
        (
            "    public void setPolicies(List<PolicyInfo> policies) {",
            "    /** 设置策略列表。 */\n    public void setPolicies(List<PolicyInfo> policies) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/body/BatchAck.java": [
        (
            "public class BatchAck implements Serializable {",
            "/**\n * Pop 消费批量 Ack 单元：以 BitSet 标记相对 startOffset 的已确认消息。\n */\npublic class BatchAck implements Serializable {",
        ),
        (
            "    @JSONField(name = \"c\", alternateNames = {\"consumerGroup\"})\n    private String consumerGroup;",
            "    /** 消费组名。 */\n    @JSONField(name = \"c\", alternateNames = {\"consumerGroup\"})\n    private String consumerGroup;",
        ),
        (
            "    @JSONField(name = \"t\", alternateNames = {\"topic\"})\n    private String topic;",
            "    /** Topic 名称。 */\n    @JSONField(name = \"t\", alternateNames = {\"topic\"})\n    private String topic;",
        ),
        (
            "    private String retry; // \"1\" if is retry topic",
            "    /** 是否为重试 Topic（\"1\" 表示是）。 */\n    private String retry; // \"1\" if is retry topic",
        ),
        (
            "    @JSONField(name = \"so\", alternateNames = {\"startOffset\"})\n    private long startOffset;",
            "    /** BitSet 基准起始偏移。 */\n    @JSONField(name = \"so\", alternateNames = {\"startOffset\"})\n    private long startOffset;",
        ),
        (
            "    @JSONField(name = \"q\", alternateNames = {\"queueId\"})\n    private int queueId;",
            "    /** 消息队列 ID。 */\n    @JSONField(name = \"q\", alternateNames = {\"queueId\"})\n    private int queueId;",
        ),
        (
            "    @JSONField(name = \"rq\", alternateNames = {\"reviveQueueId\"})\n    private int reviveQueueId;",
            "    /** 复活队列 ID（Pop 超时重投）。 */\n    @JSONField(name = \"rq\", alternateNames = {\"reviveQueueId\"})\n    private int reviveQueueId;",
        ),
        (
            "    @JSONField(name = \"pt\", alternateNames = {\"popTime\"})\n    private long popTime;",
            "    /** Pop 请求时间戳。 */\n    @JSONField(name = \"pt\", alternateNames = {\"popTime\"})\n    private long popTime;",
        ),
        (
            "    @JSONField(name = \"it\", alternateNames = {\"invisibleTime\"})\n    private long invisibleTime;",
            "    /** 消息不可见时长（毫秒）。 */\n    @JSONField(name = \"it\", alternateNames = {\"invisibleTime\"})\n    private long invisibleTime;",
        ),
        (
            "    private BitSet bitSet; // ack offsets bitSet",
            "    /** 相对 startOffset 的已 Ack 偏移位图。 */\n    private BitSet bitSet; // ack offsets bitSet",
        ),
        (
            "    public String getConsumerGroup() {",
            "    /** 返回消费组。 */\n    public String getConsumerGroup() {",
        ),
        (
            "    public BitSet getBitSet() {",
            "    /** 返回 Ack 位图。 */\n    public BitSet getBitSet() {",
        ),
        (
            "    @Override\n    public String toString() {",
            "    /** 返回 BatchAck 调试字符串。 */\n    @Override\n    public String toString() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/body/BatchAckMessageRequestBody.java": [
        (
            "public class BatchAckMessageRequestBody extends RemotingSerializable {",
            "/**\n * 批量 Ack Pop 消息的请求体：指定 Broker 及 {@link BatchAck} 列表。\n */\npublic class BatchAckMessageRequestBody extends RemotingSerializable {",
        ),
        (
            "    private String brokerName;",
            "    /** 目标 Broker 名称。 */\n    private String brokerName;",
        ),
        (
            "    private List<BatchAck> acks;",
            "    /** 待提交的批量 Ack 条目。 */\n    private List<BatchAck> acks;",
        ),
        (
            "    public String getBrokerName() {",
            "    /** 返回 Broker 名称。 */\n    public String getBrokerName() {",
        ),
        (
            "    public void setBrokerName(String brokerName) {",
            "    /** 设置 Broker 名称。 */\n    public void setBrokerName(String brokerName) {",
        ),
        (
            "    public List<BatchAck> getAcks() {",
            "    /** 返回 Ack 列表。 */\n    public List<BatchAck> getAcks() {",
        ),
        (
            "    public void setAcks(List<BatchAck> acks) {",
            "    /** 设置 Ack 列表。 */\n    public void setAcks(List<BatchAck> acks) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/body/BrokerMemberGroup.java": [
        (
            "public class BrokerMemberGroup extends RemotingSerializable {",
            "/**\n * Broker 成员组：同一 brokerName 下各 brokerId 与地址映射，用于路由与 HA 选举。\n */\npublic class BrokerMemberGroup extends RemotingSerializable {",
        ),
        (
            "    private String cluster;",
            "    /** 所属集群名。 */\n    private String cluster;",
        ),
        (
            "    private String brokerName;",
            "    /** Broker 逻辑名称。 */\n    private String brokerName;",
        ),
        (
            "    private Map<Long/* brokerId */, String/* broker address */> brokerAddrs;",
            "    /** brokerId → 访问地址。 */\n    private Map<Long/* brokerId */, String/* broker address */> brokerAddrs;",
        ),
        (
            "    // Provide default constructor for serializer",
            "    // 供 JSON/Remoting 反序列化使用的无参构造",
        ),
        (
            "    public BrokerMemberGroup() {",
            "    /** 默认构造，初始化空地址表。 */\n    public BrokerMemberGroup() {",
        ),
        (
            "    public BrokerMemberGroup(final String cluster, final String brokerName) {",
            "    /** 指定集群与 Broker 名构造成员组。 */\n    public BrokerMemberGroup(final String cluster, final String brokerName) {",
        ),
        (
            "    public long minimumBrokerId() {",
            "    /** 返回当前成员中最小 brokerId，空表时返回 0。 */\n    public long minimumBrokerId() {",
        ),
        (
            "    public String getCluster() {",
            "    /** 返回集群名。 */\n    public String getCluster() {",
        ),
        (
            "    public Map<Long, String> getBrokerAddrs() {",
            "    /** 返回 brokerId 地址映射。 */\n    public Map<Long, String> getBrokerAddrs() {",
        ),
        (
            "    @Override\n    public boolean equals(Object o) {",
            "    /** 按 cluster、brokerName、brokerAddrs 判等。 */\n    @Override\n    public boolean equals(Object o) {",
        ),
        (
            "    @Override\n    public String toString() {",
            "    /** 返回成员组可读字符串。 */\n    @Override\n    public String toString() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/body/BrokerReplicasInfo.java": [
        (
            "public class BrokerReplicasInfo extends RemotingSerializable  {",
            "/**\n * 集群 Broker 副本拓扑：各 brokerName 的 Master/ISR 与滞后副本信息。\n */\npublic class BrokerReplicasInfo extends RemotingSerializable  {",
        ),
        (
            "    private Map<String/*brokerName*/, ReplicasInfo> replicasInfoTable;",
            "    /** brokerName → 副本详情。 */\n    private Map<String/*brokerName*/, ReplicasInfo> replicasInfoTable;",
        ),
        (
            "    public BrokerReplicasInfo() {",
            "    /** 默认构造，初始化空副本表。 */\n    public BrokerReplicasInfo() {",
        ),
        (
            "    public void addReplicaInfo(final String brokerName, final ReplicasInfo replicasInfo) {",
            "    /** 注册或覆盖某 Broker 的副本信息。 */\n    public void addReplicaInfo(final String brokerName, final ReplicasInfo replicasInfo) {",
        ),
        (
            "    public Map<String, ReplicasInfo> getReplicasInfoTable() {",
            "    /** 返回副本信息表。 */\n    public Map<String, ReplicasInfo> getReplicasInfoTable() {",
        ),
        (
            "    public static class ReplicasInfo extends RemotingSerializable {",
            "    /** 单 Broker 副本集：Master 身份、epoch 及同步/非同步副本列表。 */\n    public static class ReplicasInfo extends RemotingSerializable {",
        ),
        (
            "        private Long masterBrokerId;",
            "        /** 当前 Master 的 brokerId。 */\n        private Long masterBrokerId;",
        ),
        (
            "        private String masterAddress;",
            "        /** Master 访问地址。 */\n        private String masterAddress;",
        ),
        (
            "        private Integer masterEpoch;",
            "        /** Master epoch（Controller  fencing 用）。 */\n        private Integer masterEpoch;",
        ),
        (
            "        private Integer syncStateSetEpoch;",
            "        /** 同步副本集 epoch。 */\n        private Integer syncStateSetEpoch;",
        ),
        (
            "        private List<ReplicaIdentity> inSyncReplicas;",
            "        /** 在同步副本集（ISR）内的副本。 */\n        private List<ReplicaIdentity> inSyncReplicas;",
        ),
        (
            "        private List<ReplicaIdentity> notInSyncReplicas;",
            "        /** 滞后或未入 ISR 的副本。 */\n        private List<ReplicaIdentity> notInSyncReplicas;",
        ),
        (
            "        public boolean isExistInSync(String brokerName, Long brokerId, String brokerAddress) {",
            "        /** 判断指定副本是否在 ISR 列表中。 */\n        public boolean isExistInSync(String brokerName, Long brokerId, String brokerAddress) {",
        ),
        (
            "        public boolean isExistInNotSync(String brokerName, Long brokerId, String brokerAddress) {",
            "        /** 判断指定副本是否在非同步列表中。 */\n        public boolean isExistInNotSync(String brokerName, Long brokerId, String brokerAddress) {",
        ),
        (
            "        public boolean isExistInAllReplicas(String brokerName, Long brokerId, String brokerAddress) {",
            "        /** 判断副本是否存在于 ISR 或非同步列表任一之中。 */\n        public boolean isExistInAllReplicas(String brokerName, Long brokerId, String brokerAddress) {",
        ),
        (
            "    public static class ReplicaIdentity extends RemotingSerializable {",
            "    /** 副本身份：brokerName、brokerId、地址及存活标记。 */\n    public static class ReplicaIdentity extends RemotingSerializable {",
        ),
        (
            "        private String brokerName;",
            "        /** Broker 逻辑名。 */\n        private String brokerName;",
        ),
        (
            "        private Long brokerId;",
            "        /** 副本 brokerId。 */\n        private Long brokerId;",
        ),
        (
            "        private String brokerAddress;",
            "        /** 副本访问地址。 */\n        private String brokerAddress;",
        ),
        (
            "        private Boolean alive;",
            "        /** 副本是否存活（心跳探测结果）。 */\n        private Boolean alive;",
        ),
        (
            "        public ReplicaIdentity(String brokerName, Long brokerId, String brokerAddress) {",
            "        /** 构造副本身份，alive 默认 false。 */\n        public ReplicaIdentity(String brokerName, Long brokerId, String brokerAddress) {",
        ),
        (
            "        @Override\n        public boolean equals(Object o) {",
            "        /** 按 brokerName、brokerId、brokerAddress 判等。 */\n        @Override\n        public boolean equals(Object o) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/body/BrokerStatsData.java": [
        (
            "public class BrokerStatsData extends RemotingSerializable {",
            "/**\n * Broker 运行统计聚合：分钟/小时/天三个时间粒度的 {@link BrokerStatsItem}。\n */\npublic class BrokerStatsData extends RemotingSerializable {",
        ),
        (
            "    private BrokerStatsItem statsMinute;",
            "    /** 分钟级统计。 */\n    private BrokerStatsItem statsMinute;",
        ),
        (
            "    private BrokerStatsItem statsHour;",
            "    /** 小时级统计。 */\n    private BrokerStatsItem statsHour;",
        ),
        (
            "    private BrokerStatsItem statsDay;",
            "    /** 天级统计。 */\n    private BrokerStatsItem statsDay;",
        ),
        (
            "    public BrokerStatsItem getStatsMinute() {",
            "    /** 返回分钟统计。 */\n    public BrokerStatsItem getStatsMinute() {",
        ),
        (
            "    public void setStatsMinute(BrokerStatsItem statsMinute) {",
            "    /** 设置分钟统计。 */\n    public void setStatsMinute(BrokerStatsItem statsMinute) {",
        ),
        (
            "    public BrokerStatsItem getStatsHour() {",
            "    /** 返回小时统计。 */\n    public BrokerStatsItem getStatsHour() {",
        ),
        (
            "    public BrokerStatsItem getStatsDay() {",
            "    /** 返回天级统计。 */\n    public BrokerStatsItem getStatsDay() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/body/BrokerStatsItem.java": [
        (
            "public class BrokerStatsItem {",
            "/**\n * Broker 单时间窗口统计项：累计量、TPS 与平均处理耗时。\n */\npublic class BrokerStatsItem {",
        ),
        (
            "    private long sum;",
            "    /** 窗口内累计计数（如消息条数）。 */\n    private long sum;",
        ),
        (
            "    private double tps;",
            "    /** 吞吐量（条/秒）。 */\n    private double tps;",
        ),
        (
            "    private double avgpt;",
            "    /** 平均处理耗时（毫秒/条）。 */\n    private double avgpt;",
        ),
        (
            "    public long getSum() {",
            "    /** 返回累计量。 */\n    public long getSum() {",
        ),
        (
            "    public void setSum(long sum) {",
            "    /** 设置累计量。 */\n    public void setSum(long sum) {",
        ),
        (
            "    public double getTps() {",
            "    /** 返回 TPS。 */\n    public double getTps() {",
        ),
        (
            "    public void setTps(double tps) {",
            "    /** 设置 TPS。 */\n    public void setTps(double tps) {",
        ),
        (
            "    public double getAvgpt() {",
            "    /** 返回平均耗时。 */\n    public double getAvgpt() {",
        ),
        (
            "    public void setAvgpt(double avgpt) {",
            "    /** 设置平均耗时。 */\n    public void setAvgpt(double avgpt) {",
        ),
    ],
}
