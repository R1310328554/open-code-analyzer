"""Chinese JavaDoc replacements for RocketMQ wave30a remoting protocol body [0:15]."""

R: dict[str, list[tuple[str, str]]] = {
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/body/ProcessQueueInfo.java": [
        (
            "public class ProcessQueueInfo {",
            "/**\n * Push/Pull 消费 ProcessQueue 运行态快照：缓存消息范围、事务消息、顺序锁及拉取/消费时间戳。\n */\npublic class ProcessQueueInfo {",
        ),
        (
            "    private long commitOffset;",
            "    /** 已提交消费位点。 */\n    private long commitOffset;",
        ),
        (
            "    private long cachedMsgMinOffset;",
            "    /** 本地缓存消息最小逻辑位点。 */\n    private long cachedMsgMinOffset;",
        ),
        (
            "    private long cachedMsgMaxOffset;",
            "    /** 本地缓存消息最大逻辑位点。 */\n    private long cachedMsgMaxOffset;",
        ),
        (
            "    private int cachedMsgCount;",
            "    /** 本地缓存消息条数。 */\n    private int cachedMsgCount;",
        ),
        (
            "    private int cachedMsgSizeInMiB;",
            "    /** 本地缓存消息占用内存（MiB）。 */\n    private int cachedMsgSizeInMiB;",
        ),
        (
            "    private long transactionMsgMinOffset;",
            "    /** 事务消息最小位点。 */\n    private long transactionMsgMinOffset;",
        ),
        (
            "    private long transactionMsgMaxOffset;",
            "    /** 事务消息最大位点。 */\n    private long transactionMsgMaxOffset;",
        ),
        (
            "    private int transactionMsgCount;",
            "    /** 待处理事务消息条数。 */\n    private int transactionMsgCount;",
        ),
        (
            "    private boolean locked;",
            "    /** 顺序消费队列是否已加锁。 */\n    private boolean locked;",
        ),
        (
            "    private long tryUnlockTimes;",
            "    /** 尝试解锁次数。 */\n    private long tryUnlockTimes;",
        ),
        (
            "    private long lastLockTimestamp;",
            "    /** 最近一次加锁时间戳。 */\n    private long lastLockTimestamp;",
        ),
        (
            "    private boolean droped;",
            "    /** 队列是否已被丢弃（Rebalance 回收）。 */\n    private boolean droped;",
        ),
        (
            "    private long lastPullTimestamp;",
            "    /** 最近一次拉取时间戳。 */\n    private long lastPullTimestamp;",
        ),
        (
            "    private long lastConsumeTimestamp;",
            "    /** 最近一次消费时间戳。 */\n    private long lastConsumeTimestamp;",
        ),
        (
            "    public long getCommitOffset() {",
            "    /** 返回已提交位点。 */\n    public long getCommitOffset() {",
        ),
        (
            "    public boolean isLocked() {",
            "    /** 返回是否已加锁。 */\n    public boolean isLocked() {",
        ),
        (
            "    public boolean isDroped() {",
            "    /** 返回队列是否已丢弃。 */\n    public boolean isDroped() {",
        ),
        (
            "    @Override\n    public String toString() {",
            "    /** 返回便于诊断的可读字符串。 */\n    @Override\n    public String toString() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/body/ProducerConnection.java": [
        (
            "public class ProducerConnection extends RemotingSerializable {",
            "/**\n * 生产者组在线连接快照：当前已注册的生产者客户端集合。\n */\npublic class ProducerConnection extends RemotingSerializable {",
        ),
        (
            "    private HashSet<Connection> connectionSet = new HashSet<>();",
            "    /** 在线生产者连接集合。 */\n    private HashSet<Connection> connectionSet = new HashSet<>();",
        ),
        (
            "    public HashSet<Connection> getConnectionSet() {",
            "    /** 返回连接集合。 */\n    public HashSet<Connection> getConnectionSet() {",
        ),
        (
            "    public void setConnectionSet(HashSet<Connection> connectionSet) {",
            "    /** 设置连接集合。 */\n    public void setConnectionSet(HashSet<Connection> connectionSet) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/body/ProducerInfo.java": [
        (
            "public class ProducerInfo extends RemotingSerializable {",
            "/**\n * 单个生产者客户端元信息：clientId、网络地址、SDK 语言与协议版本。\n */\npublic class ProducerInfo extends RemotingSerializable {",
        ),
        (
            "    private String clientId;",
            "    /** 客户端唯一标识。 */\n    private String clientId;",
        ),
        (
            "    private String remoteIP;",
            "    /** 客户端远程 IP 地址。 */\n    private String remoteIP;",
        ),
        (
            "    private LanguageCode language;",
            "    /** SDK 语言类型。 */\n    private LanguageCode language;",
        ),
        (
            "    private int version;",
            "    /** Remoting 协议版本号。 */\n    private int version;",
        ),
        (
            "    private long lastUpdateTimestamp;",
            "    /** 最近一次心跳/注册更新时间戳。 */\n    private long lastUpdateTimestamp;",
        ),
        (
            "    public ProducerInfo(String clientId, String remoteIP, LanguageCode language, int version, long lastUpdateTimestamp) {",
            "    /** 全字段构造。 */\n    public ProducerInfo(String clientId, String remoteIP, LanguageCode language, int version, long lastUpdateTimestamp) {",
        ),
        (
            "    public String getClientId() {",
            "    /** 返回客户端 ID。 */\n    public String getClientId() {",
        ),
        (
            "    public String getRemoteIP() {",
            "    /** 返回远程 IP。 */\n    public String getRemoteIP() {",
        ),
        (
            "    public LanguageCode getLanguage() {",
            "    /** 返回 SDK 语言。 */\n    public LanguageCode getLanguage() {",
        ),
        (
            "    public int getVersion() {",
            "    /** 返回协议版本。 */\n    public int getVersion() {",
        ),
        (
            "    public long getLastUpdateTimestamp() {",
            "    /** 返回最近更新时间戳。 */\n    public long getLastUpdateTimestamp() {",
        ),
        (
            "    @Override\n    public String toString() {",
            "    /** 返回调试字符串。 */\n    @Override\n    public String toString() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/body/ProducerTableInfo.java": [
        (
            "public class ProducerTableInfo extends RemotingSerializable {",
            "/**\n * 生产者组 → 生产者实例列表映射表，用于 Admin 查询在线生产者。\n */\npublic class ProducerTableInfo extends RemotingSerializable {",
        ),
        (
            "    public ProducerTableInfo(Map<String, List<ProducerInfo>> data) {",
            "    /** 以映射表初始化。 */\n    public ProducerTableInfo(Map<String, List<ProducerInfo>> data) {",
        ),
        (
            "    private Map<String, List<ProducerInfo>> data;",
            "    /** groupName → {@link ProducerInfo} 列表。 */\n    private Map<String, List<ProducerInfo>> data;",
        ),
        (
            "    public Map<String, List<ProducerInfo>> getData() {",
            "    /** 返回生产者映射表。 */\n    public Map<String, List<ProducerInfo>> getData() {",
        ),
        (
            "    public void setData(Map<String, List<ProducerInfo>> data) {",
            "    /** 设置生产者映射表。 */\n    public void setData(Map<String, List<ProducerInfo>> data) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/body/QueryAssignmentRequestBody.java": [
        (
            "public class QueryAssignmentRequestBody extends RemotingSerializable {",
            "/**\n * 查询消息队列分配（Rebalance 结果）请求体：Topic、消费组、客户端及分配策略。\n */\npublic class QueryAssignmentRequestBody extends RemotingSerializable {",
        ),
        (
            "    private String topic;",
            "    /** 目标 Topic。 */\n    private String topic;",
        ),
        (
            "    private String consumerGroup;",
            "    /** 消费者 Group。 */\n    private String consumerGroup;",
        ),
        (
            "    private String clientId;",
            "    /** 发起查询的客户端 ID。 */\n    private String clientId;",
        ),
        (
            "    private String strategyName;",
            "    /** 队列分配策略名称。 */\n    private String strategyName;",
        ),
        (
            "    private MessageModel messageModel;",
            "    /** 消息模型（集群/广播）。 */\n    private MessageModel messageModel;",
        ),
        (
            "    public String getTopic() {",
            "    /** 返回 Topic。 */\n    public String getTopic() {",
        ),
        (
            "    public String getConsumerGroup() {",
            "    /** 返回消费组。 */\n    public String getConsumerGroup() {",
        ),
        (
            "    public String getClientId() {",
            "    /** 返回客户端 ID。 */\n    public String getClientId() {",
        ),
        (
            "    public String getStrategyName() {",
            "    /** 返回分配策略名。 */\n    public String getStrategyName() {",
        ),
        (
            "    public MessageModel getMessageModel() {",
            "    /** 返回消息模型。 */\n    public MessageModel getMessageModel() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/body/QueryAssignmentResponseBody.java": [
        (
            "public class QueryAssignmentResponseBody extends RemotingSerializable {",
            "/**\n * 队列分配查询响应：返回 {@link MessageQueueAssignment} 集合。\n */\npublic class QueryAssignmentResponseBody extends RemotingSerializable {",
        ),
        (
            "    private Set<MessageQueueAssignment> messageQueueAssignments;",
            "    /** 分配给当前客户端的队列集合。 */\n    private Set<MessageQueueAssignment> messageQueueAssignments;",
        ),
        (
            "    public Set<MessageQueueAssignment> getMessageQueueAssignments() {",
            "    /** 返回队列分配集合。 */\n    public Set<MessageQueueAssignment> getMessageQueueAssignments() {",
        ),
        (
            "    public void setMessageQueueAssignments(",
            "    /** 设置队列分配集合。 */\n    public void setMessageQueueAssignments(",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/body/QueryConsumeQueueResponseBody.java": [
        (
            "public class QueryConsumeQueueResponseBody extends RemotingSerializable {",
            "/**\n * 查询 ConsumeQueue 索引响应：订阅信息、过滤数据及队列索引条目列表。\n */\npublic class QueryConsumeQueueResponseBody extends RemotingSerializable {",
        ),
        (
            "    private SubscriptionData subscriptionData;",
            "    /** 关联订阅数据。 */\n    private SubscriptionData subscriptionData;",
        ),
        (
            "    private String filterData;",
            "    /** 过滤表达式序列化数据。 */\n    private String filterData;",
        ),
        (
            "    private List<ConsumeQueueData> queueData;",
            "    /** ConsumeQueue 索引条目列表。 */\n    private List<ConsumeQueueData> queueData;",
        ),
        (
            "    private long maxQueueIndex;",
            "    /** 返回结果中最大队列逻辑索引。 */\n    private long maxQueueIndex;",
        ),
        (
            "    private long minQueueIndex;",
            "    /** 返回结果中最小队列逻辑索引。 */\n    private long minQueueIndex;",
        ),
        (
            "    public SubscriptionData getSubscriptionData() {",
            "    /** 返回订阅数据。 */\n    public SubscriptionData getSubscriptionData() {",
        ),
        (
            "    public List<ConsumeQueueData> getQueueData() {",
            "    /** 返回队列索引列表。 */\n    public List<ConsumeQueueData> getQueueData() {",
        ),
        (
            "    public long getMaxQueueIndex() {",
            "    /** 返回最大队列索引。 */\n    public long getMaxQueueIndex() {",
        ),
        (
            "    public long getMinQueueIndex() {",
            "    /** 返回最小队列索引。 */\n    public long getMinQueueIndex() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/body/QueryConsumeTimeSpanBody.java": [
        (
            "public class QueryConsumeTimeSpanBody extends RemotingSerializable {",
            "/**\n * 消费时间跨度查询响应：各队列最早/最晚消息时间与消费延迟。\n */\npublic class QueryConsumeTimeSpanBody extends RemotingSerializable {",
        ),
        (
            "    List<QueueTimeSpan> consumeTimeSpanSet = new ArrayList<>();",
            "    /** 各队列时间跨度条目列表。 */\n    List<QueueTimeSpan> consumeTimeSpanSet = new ArrayList<>();",
        ),
        (
            "    public List<QueueTimeSpan> getConsumeTimeSpanSet() {",
            "    /** 返回时间跨度列表。 */\n    public List<QueueTimeSpan> getConsumeTimeSpanSet() {",
        ),
        (
            "    public void setConsumeTimeSpanSet(List<QueueTimeSpan> consumeTimeSpanSet) {",
            "    /** 设置时间跨度列表。 */\n    public void setConsumeTimeSpanSet(List<QueueTimeSpan> consumeTimeSpanSet) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/body/QueryCorrectionOffsetBody.java": [
        (
            "public class QueryCorrectionOffsetBody extends RemotingSerializable {",
            "/**\n * 消费位点校正查询响应：queueId → 校正后位点映射。\n */\npublic class QueryCorrectionOffsetBody extends RemotingSerializable {",
        ),
        (
            "    private Map<Integer, Long> correctionOffsets = new HashMap<>();",
            "    /** queueId → 校正位点。 */\n    private Map<Integer, Long> correctionOffsets = new HashMap<>();",
        ),
        (
            "    public Map<Integer, Long> getCorrectionOffsets() {",
            "    /** 返回校正位点映射。 */\n    public Map<Integer, Long> getCorrectionOffsets() {",
        ),
        (
            "    public void setCorrectionOffsets(Map<Integer, Long> correctionOffsets) {",
            "    /** 设置校正位点映射。 */\n    public void setCorrectionOffsets(Map<Integer, Long> correctionOffsets) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/body/QuerySubscriptionResponseBody.java": [
        (
            "public class QuerySubscriptionResponseBody extends RemotingSerializable {",
            "/**\n * 订阅关系查询响应：消费组、Topic 及 {@link SubscriptionData} 详情。\n */\npublic class QuerySubscriptionResponseBody extends RemotingSerializable {",
        ),
        (
            "    private SubscriptionData subscriptionData;",
            "    /** 订阅数据详情。 */\n    private SubscriptionData subscriptionData;",
        ),
        (
            "    private String group;",
            "    /** 消费者 Group。 */\n    private String group;",
        ),
        (
            "    private String topic;",
            "    /** 目标 Topic。 */\n    private String topic;",
        ),
        (
            "    public SubscriptionData getSubscriptionData() {",
            "    /** 返回订阅数据。 */\n    public SubscriptionData getSubscriptionData() {",
        ),
        (
            "    public String getGroup() {",
            "    /** 返回消费组。 */\n    public String getGroup() {",
        ),
        (
            "    public String getTopic() {",
            "    /** 返回 Topic。 */\n    public String getTopic() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/body/QueueTimeSpan.java": [
        (
            "public class QueueTimeSpan {",
            "/**\n * 单队列消费时间跨度：最早/最晚消息时间、消费时间及延迟。\n */\npublic class QueueTimeSpan {",
        ),
        (
            "    private MessageQueue messageQueue;",
            "    /** 目标 MessageQueue。 */\n    private MessageQueue messageQueue;",
        ),
        (
            "    private long minTimeStamp;",
            "    /** 队列中最早消息存储时间戳。 */\n    private long minTimeStamp;",
        ),
        (
            "    private long maxTimeStamp;",
            "    /** 队列中最晚消息存储时间戳。 */\n    private long maxTimeStamp;",
        ),
        (
            "    private long consumeTimeStamp;",
            "    /** 当前消费进度对应时间戳。 */\n    private long consumeTimeStamp;",
        ),
        (
            "    private long delayTime;",
            "    /** 消费延迟（毫秒）。 */\n    private long delayTime;",
        ),
        (
            "    public MessageQueue getMessageQueue() {",
            "    /** 返回 MessageQueue。 */\n    public MessageQueue getMessageQueue() {",
        ),
        (
            "    public String getMinTimeStampStr() {",
            "    /** 返回最早消息时间的可读字符串。 */\n    public String getMinTimeStampStr() {",
        ),
        (
            "    public String getMaxTimeStampStr() {",
            "    /** 返回最晚消息时间的可读字符串。 */\n    public String getMaxTimeStampStr() {",
        ),
        (
            "    public long getDelayTime() {",
            "    /** 返回消费延迟毫秒数。 */\n    public long getDelayTime() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/body/RegisterBrokerBody.java": [
        (
            "public class RegisterBrokerBody extends RemotingSerializable {",
            "/**\n * Broker 向 NameServer 注册时的请求体：Topic 配置、FilterServer 列表及队列映射。\n * 支持 Deflate 压缩编解码以减小大 Topic 表传输体积。\n */\npublic class RegisterBrokerBody extends RemotingSerializable {",
        ),
        (
            "    private TopicConfigAndMappingSerializeWrapper topicConfigSerializeWrapper = new TopicConfigAndMappingSerializeWrapper();",
            "    /** Topic 配置与队列映射序列化包装。 */\n    private TopicConfigAndMappingSerializeWrapper topicConfigSerializeWrapper = new TopicConfigAndMappingSerializeWrapper();",
        ),
        (
            "    private List<String> filterServerList = new ArrayList<>();",
            "    /** 关联 FilterServer 地址列表。 */\n    private List<String> filterServerList = new ArrayList<>();",
        ),
        (
            "    public byte[] encode(boolean compress) {",
            "    /** 序列化注册体；compress 为 true 时使用 Deflate 压缩。 */\n    public byte[] encode(boolean compress) {",
        ),
        (
            "            // write data version",
            "            // 写入 DataVersion",
        ),
        (
            "            // write number of topic configs",
            "            // 写入 Topic 配置数量",
        ),
        (
            "            // write topic config entry one by one.",
            "            // 逐条写入 Topic 配置",
        ),
        (
            "            // write filter server list json length",
            "            // 写入 FilterServer 列表 JSON 长度",
        ),
        (
            "            // write filter server list json",
            "            // 写入 FilterServer 列表 JSON",
        ),
        (
            "            //write the topic queue mapping",
            "            // 写入 Topic 队列映射",
        ),
        (
            "                //as the placeholder",
            "                // 占位空映射",
        ),
        (
            "    public static RegisterBrokerBody decode(byte[] data, boolean compressed, MQVersion.Version brokerVersion) throws IOException {",
            "    /** 反序列化注册体；compressed 为 true 时先解压；5.0+ 解析队列映射。 */\n    public static RegisterBrokerBody decode(byte[] data, boolean compressed, MQVersion.Version brokerVersion) throws IOException {",
        ),
        (
            "    public TopicConfigAndMappingSerializeWrapper getTopicConfigSerializeWrapper() {",
            "    /** 返回 Topic 配置包装。 */\n    public TopicConfigAndMappingSerializeWrapper getTopicConfigSerializeWrapper() {",
        ),
        (
            "    public List<String> getFilterServerList() {",
            "    /** 返回 FilterServer 列表。 */\n    public List<String> getFilterServerList() {",
        ),
        (
            "    private ConcurrentMap<String, TopicConfig> cloneTopicConfigTable(",
            "    /** 浅拷贝 Topic 配置表，避免编码时并发修改。 */\n    private ConcurrentMap<String, TopicConfig> cloneTopicConfigTable(",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/body/ResetOffsetBody.java": [
        (
            "public class ResetOffsetBody extends RemotingSerializable {",
            "/**\n * 管理端重置消费位点请求体：MessageQueue → 目标位点映射。\n */\npublic class ResetOffsetBody extends RemotingSerializable {",
        ),
        (
            "    private Map<MessageQueue, Long> offsetTable;",
            "    /** 待重置的队列与目标位点。 */\n    private Map<MessageQueue, Long> offsetTable;",
        ),
        (
            "    public ResetOffsetBody() {",
            "    /** 默认构造，初始化空位点表。 */\n    public ResetOffsetBody() {",
        ),
        (
            "    public Map<MessageQueue, Long> getOffsetTable() {",
            "    /** 返回位点映射表。 */\n    public Map<MessageQueue, Long> getOffsetTable() {",
        ),
        (
            "    public void setOffsetTable(Map<MessageQueue, Long> offsetTable) {",
            "    /** 设置位点映射表。 */\n    public void setOffsetTable(Map<MessageQueue, Long> offsetTable) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/body/ResetOffsetBodyForC.java": [
        (
            "public class ResetOffsetBodyForC extends RemotingSerializable {",
            "/**\n * C 语言客户端重置消费位点请求体：{@link MessageQueueForC} 列表。\n */\npublic class ResetOffsetBodyForC extends RemotingSerializable {",
        ),
        (
            "    private List<MessageQueueForC> offsetTable;",
            "    /** 待重置的 C 风格队列位点列表。 */\n    private List<MessageQueueForC> offsetTable;",
        ),
        (
            "    public List<MessageQueueForC> getOffsetTable() {",
            "    /** 返回位点列表。 */\n    public List<MessageQueueForC> getOffsetTable() {",
        ),
        (
            "    public void setOffsetTable(List<MessageQueueForC> offsetTable) {",
            "    /** 设置位点列表。 */\n    public void setOffsetTable(List<MessageQueueForC> offsetTable) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/body/RoleChangeNotifyEntry.java": [
        (
            "public class RoleChangeNotifyEntry {",
            "/**\n * Controller 模式下 Master 角色变更通知条目：成员组、Master 地址/Epoch 及 syncStateSet。\n */\npublic class RoleChangeNotifyEntry {",
        ),
        (
            "    private final BrokerMemberGroup brokerMemberGroup;",
            "    /** Broker 成员组信息。 */\n    private final BrokerMemberGroup brokerMemberGroup;",
        ),
        (
            "    private final String masterAddress;",
            "    /** 新 Master 服务地址。 */\n    private final String masterAddress;",
        ),
        (
            "    private final Long masterBrokerId;",
            "    /** 新 Master 的 brokerId。 */\n    private final Long masterBrokerId;",
        ),
        (
            "    private final int masterEpoch;",
            "    /** Master Epoch 版本号。 */\n    private final int masterEpoch;",
        ),
        (
            "    private final int syncStateSetEpoch;",
            "    /** syncStateSet Epoch 版本号。 */\n    private final int syncStateSetEpoch;",
        ),
        (
            "    private final Set<Long> syncStateSet;",
            "    /** 同步副本 brokerId 集合。 */\n    private final Set<Long> syncStateSet;",
        ),
        (
            "    public RoleChangeNotifyEntry(BrokerMemberGroup brokerMemberGroup, String masterAddress, Long masterBrokerId, int masterEpoch, int syncStateSetEpoch, Set<Long> syncStateSet) {",
            "    /** 全字段构造。 */\n    public RoleChangeNotifyEntry(BrokerMemberGroup brokerMemberGroup, String masterAddress, Long masterBrokerId, int masterEpoch, int syncStateSetEpoch, Set<Long> syncStateSet) {",
        ),
        (
            "    public static RoleChangeNotifyEntry convert(RemotingCommand electMasterResponse) {",
            "    /** 从选举 Master 响应命令解析角色变更条目。 */\n    public static RoleChangeNotifyEntry convert(RemotingCommand electMasterResponse) {",
        ),
        (
            "    public BrokerMemberGroup getBrokerMemberGroup() {",
            "    /** 返回成员组。 */\n    public BrokerMemberGroup getBrokerMemberGroup() {",
        ),
        (
            "    public String getMasterAddress() {",
            "    /** 返回 Master 地址。 */\n    public String getMasterAddress() {",
        ),
        (
            "    public Set<Long> getSyncStateSet() {",
            "    /** 返回 syncStateSet。 */\n    public Set<Long> getSyncStateSet() {",
        ),
    ],
}
