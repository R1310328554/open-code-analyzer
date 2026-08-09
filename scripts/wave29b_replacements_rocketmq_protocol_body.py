"""Chinese JavaDoc replacements for RocketMQ wave29b protocol/body [15:30]."""

R: dict[str, list[tuple[str, str]]] = {
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/body/GetBrokerLiteInfoResponseBody.java": [
        (
            "public class GetBrokerLiteInfoResponseBody extends RemotingSerializable {",
            "/**\n * 查询 Broker 轻量消息（LMQ/Lite）运行态的响应体：存储类型、配额用量及 Topic/Group 元数据。\n */\npublic class GetBrokerLiteInfoResponseBody extends RemotingSerializable {",
        ),
        (
            "    private String storeType;",
            "    /** 消息存储类型（如本地文件、云存储等）。 */\n    private String storeType;",
        ),
        (
            "    private int maxLmqNum;",
            "    /** LMQ 配额上限。 */\n    private int maxLmqNum;",
        ),
        (
            "    private int currentLmqNum;",
            "    /** 当前已用 LMQ 数量。 */\n    private int currentLmqNum;",
        ),
        (
            "    private int liteSubscriptionCount;",
            "    /** Lite 订阅关系总数。 */\n    private int liteSubscriptionCount;",
        ),
        (
            "    private int orderInfoCount;",
            "    /** 顺序消息元信息条目数。 */\n    private int orderInfoCount;",
        ),
        (
            "    private int cqTableSize;",
            "    /** ConsumeQueue 表规模。 */\n    private int cqTableSize;",
        ),
        (
            "    private int offsetTableSize;",
            "    /** 消费位点表规模。 */\n    private int offsetTableSize;",
        ),
        (
            "    private int eventMapSize;",
            "    /** 事件映射表规模。 */\n    private int eventMapSize;",
        ),
        (
            "    private Map<String, Integer> topicMeta;",
            "    /** Topic 名 → 元数据计数。 */\n    private Map<String, Integer> topicMeta;",
        ),
        (
            "    private Map<String, Set<String>> groupMeta;",
            "    /** Group 名 → 关联 Lite Topic 集合。 */\n    private Map<String, Set<String>> groupMeta;",
        ),
        (
            "    public String getStoreType() {",
            "    /** 返回存储类型。 */\n    public String getStoreType() {",
        ),
        (
            "    public int getMaxLmqNum() {",
            "    /** 返回 LMQ 配额上限。 */\n    public int getMaxLmqNum() {",
        ),
        (
            "    public int getCurrentLmqNum() {",
            "    /** 返回当前 LMQ 用量。 */\n    public int getCurrentLmqNum() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/body/GetBrokerMemberGroupResponseBody.java": [
        (
            "public class GetBrokerMemberGroupResponseBody extends RemotingSerializable {",
            "/**\n * 查询 Broker 成员组的 Remoting 响应体，封装同 brokerName 下各副本地址。\n */\npublic class GetBrokerMemberGroupResponseBody extends RemotingSerializable {",
        ),
        (
            "    // Contains the broker member info of the same broker group",
            "    // 同一 Broker 组内各 brokerId 与地址映射",
        ),
        (
            "    private BrokerMemberGroup brokerMemberGroup;",
            "    /** 成员组详情。 */\n    private BrokerMemberGroup brokerMemberGroup;",
        ),
        (
            "    public BrokerMemberGroup getBrokerMemberGroup() {",
            "    /** 返回 Broker 成员组。 */\n    public BrokerMemberGroup getBrokerMemberGroup() {",
        ),
        (
            "    public void setBrokerMemberGroup(final BrokerMemberGroup brokerMemberGroup) {",
            "    /** 设置 Broker 成员组。 */\n    public void setBrokerMemberGroup(final BrokerMemberGroup brokerMemberGroup) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/body/GetConsumerStatusBody.java": [
        (
            "@Deprecated\npublic class GetConsumerStatusBody extends RemotingSerializable {",
            "/**\n * 消费者消费进度快照（已废弃）：全局队列位点与各消费者实例位点。\n *\n * @deprecated 请改用新版消费状态查询接口。\n */\n@Deprecated\npublic class GetConsumerStatusBody extends RemotingSerializable {",
        ),
        (
            "    private Map<MessageQueue, Long> messageQueueTable = new HashMap<>();",
            "    /** 全局 MessageQueue → 消费位点。 */\n    private Map<MessageQueue, Long> messageQueueTable = new HashMap<>();",
        ),
        (
            "    private Map<String, Map<MessageQueue, Long>> consumerTable =",
            "    /** clientId →（MessageQueue → 位点）映射。 */\n    private Map<String, Map<MessageQueue, Long>> consumerTable =",
        ),
        (
            "    public Map<MessageQueue, Long> getMessageQueueTable() {",
            "    /** 返回全局队列位点表。 */\n    public Map<MessageQueue, Long> getMessageQueueTable() {",
        ),
        (
            "    public Map<String, Map<MessageQueue, Long>> getConsumerTable() {",
            "    /** 返回各消费者实例位点表。 */\n    public Map<String, Map<MessageQueue, Long>> getConsumerTable() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/body/GetLiteClientInfoResponseBody.java": [
        (
            "public class GetLiteClientInfoResponseBody extends RemotingSerializable {",
            "/**\n * Lite 消费客户端运行态响应：父 Topic、订阅 Group、最近访问/消费时间及 Lite Topic 集合。\n */\npublic class GetLiteClientInfoResponseBody extends RemotingSerializable {",
        ),
        (
            "    private String parentTopic;",
            "    /** 父 Topic 名。 */\n    private String parentTopic;",
        ),
        (
            "    private String group;",
            "    /** 消费者 Group。 */\n    private String group;",
        ),
        (
            "    private String clientId;",
            "    /** 客户端实例 ID。 */\n    private String clientId;",
        ),
        (
            "    private long lastAccessTime;",
            "    /** 最近一次与 Broker 通信时间戳。 */\n    private long lastAccessTime;",
        ),
        (
            "    private long lastConsumeTime;",
            "    /** 最近一次成功消费时间戳。 */\n    private long lastConsumeTime;",
        ),
        (
            "    private int liteTopicCount;",
            "    /** 已订阅 Lite Topic 数量。 */\n    private int liteTopicCount;",
        ),
        (
            "    private Set<String> liteTopicSet;",
            "    /** 已订阅 Lite Topic 名称集合。 */\n    private Set<String> liteTopicSet;",
        ),
        (
            "    public String getParentTopic() {",
            "    /** 返回父 Topic。 */\n    public String getParentTopic() {",
        ),
        (
            "    public Set<String> getLiteTopicSet() {",
            "    /** 返回 Lite Topic 集合。 */\n    public Set<String> getLiteTopicSet() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/body/GetLiteGroupInfoResponseBody.java": [
        (
            "public class GetLiteGroupInfoResponseBody extends RemotingSerializable {",
            "/**\n * Lite 消费组积压与位点信息响应：汇总 lag、TopK 明细及可选 Lite Topic 位点。\n */\npublic class GetLiteGroupInfoResponseBody extends RemotingSerializable {",
        ),
        (
            "    private String group;",
            "    /** 消费者 Group。 */\n    private String group;",
        ),
        (
            "    private String parentTopic;",
            "    /** 父 Topic 名。 */\n    private String parentTopic;",
        ),
        (
            "    private String liteTopic;",
            "    /** 指定查询的 Lite Topic（可为空表示全组汇总）。 */\n    private String liteTopic;",
        ),
        (
            "    // total log info",
            "    // 全组汇总 lag 信息",
        ),
        (
            "    private long earliestUnconsumedTimestamp = -1;",
            "    /** 最早未消费消息时间戳，-1 表示无积压。 */\n    private long earliestUnconsumedTimestamp = -1;",
        ),
        (
            "    private long totalLagCount;",
            "    /** 全组消息积压条数。 */\n    private long totalLagCount;",
        ),
        (
            "    // lite topic detail info",
            "    // 单 Lite Topic 位点明细（指定 liteTopic 时填充）",
        ),
        (
            "    private OffsetWrapper liteTopicOffsetWrapper; // if lite topic specified",
            "    /** 指定 Lite Topic 的位点包装（min/max/consumer 位点）。 */\n    private OffsetWrapper liteTopicOffsetWrapper; // if lite topic specified",
        ),
        (
            "    // topK info",
            "    // 积压 TopK 排行",
        ),
        (
            "    private List<LiteLagInfo> lagCountTopK;",
            "    /** 按积压条数降序的 TopK Lite Topic。 */\n    private List<LiteLagInfo> lagCountTopK;",
        ),
        (
            "    private List<LiteLagInfo> lagTimestampTopK;",
            "    /** 按最早未消费时间排序的 TopK Lite Topic。 */\n    private List<LiteLagInfo> lagTimestampTopK;",
        ),
        (
            "    public long getTotalLagCount() {",
            "    /** 返回全组积压条数。 */\n    public long getTotalLagCount() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/body/GetLiteTopicInfoResponseBody.java": [
        (
            "public class GetLiteTopicInfoResponseBody extends RemotingSerializable {",
            "/**\n * Lite Topic 元信息响应：订阅者、Topic 位点及是否分片到 Broker。\n */\npublic class GetLiteTopicInfoResponseBody extends RemotingSerializable {",
        ),
        (
            "    private String parentTopic;",
            "    /** 父 Topic 名。 */\n    private String parentTopic;",
        ),
        (
            "    private String liteTopic;",
            "    /** Lite Topic 名。 */\n    private String liteTopic;",
        ),
        (
            "    private Set<ClientGroup> subscriber;",
            "    /** 订阅该 Lite Topic 的客户端组集合。 */\n    private Set<ClientGroup> subscriber;",
        ),
        (
            "    private TopicOffset topicOffset;",
            "    /** Topic 各队列 min/max 位点。 */\n    private TopicOffset topicOffset;",
        ),
        (
            "    private boolean shardingToBroker;",
            "    /** 是否已分片路由到 Broker。 */\n    private boolean shardingToBroker;",
        ),
        (
            "    public Set<ClientGroup> getSubscriber() {",
            "    /** 返回订阅者集合。 */\n    public Set<ClientGroup> getSubscriber() {",
        ),
        (
            "    public boolean isShardingToBroker() {",
            "    /** 返回是否已分片到 Broker。 */\n    public boolean isShardingToBroker() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/body/GetParentTopicInfoResponseBody.java": [
        (
            "public class GetParentTopicInfoResponseBody extends RemotingSerializable {",
            "/**\n * 父 Topic 元信息响应：TTL、关联 Group、LMQ 配额及 Lite Topic 数量。\n */\npublic class GetParentTopicInfoResponseBody extends RemotingSerializable {",
        ),
        (
            "    private String topic;",
            "    /** 父 Topic 名。 */\n    private String topic;",
        ),
        (
            "    private int ttl;",
            "    /** 消息存活时间（秒）。 */\n    private int ttl;",
        ),
        (
            "    private Set<String> groups;",
            "    /** 已订阅该 Topic 的 Group 集合。 */\n    private Set<String> groups;",
        ),
        (
            "    private int lmqNum;",
            "    /** 已分配 LMQ 数量。 */\n    private int lmqNum;",
        ),
        (
            "    private int liteTopicCount;",
            "    /** 下属 Lite Topic 数量。 */\n    private int liteTopicCount;",
        ),
        (
            "    public int getTtl() {",
            "    /** 返回消息 TTL。 */\n    public int getTtl() {",
        ),
        (
            "    public int getLiteTopicCount() {",
            "    /** 返回 Lite Topic 数量。 */\n    public int getLiteTopicCount() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/body/GroupList.java": [
        (
            "public class GroupList extends RemotingSerializable {",
            "/**\n * 消费者 Group 名称列表，用于 Remoting 批量查询或管理接口。\n */\npublic class GroupList extends RemotingSerializable {",
        ),
        (
            "    private HashSet<String> groupList = new HashSet<>();",
            "    /** Group 名集合。 */\n    private HashSet<String> groupList = new HashSet<>();",
        ),
        (
            "    public HashSet<String> getGroupList() {",
            "    /** 返回 Group 集合。 */\n    public HashSet<String> getGroupList() {",
        ),
        (
            "    public void setGroupList(HashSet<String> groupList) {",
            "    /** 设置 Group 集合。 */\n    public void setGroupList(HashSet<String> groupList) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/body/HARuntimeInfo.java": [
        (
            "public class HARuntimeInfo extends RemotingSerializable {",
            "/**\n * Broker 主从 HA 运行时快照：Master 身份、同步从节点数及各连接/客户端传输状态。\n */\npublic class HARuntimeInfo extends RemotingSerializable {",
        ),
        (
            "    private boolean master;",
            "    /** 当前节点是否为 Master。 */\n    private boolean master;",
        ),
        (
            "    private long masterCommitLogMaxOffset;",
            "    /** Master CommitLog 最大物理偏移。 */\n    private long masterCommitLogMaxOffset;",
        ),
        (
            "    private int inSyncSlaveNums;",
            "    /** 处于同步状态的从节点数量。 */\n    private int inSyncSlaveNums;",
        ),
        (
            "    private List<HAConnectionRuntimeInfo> haConnectionInfo = new ArrayList<>();",
            "    /** Master 侧各从节点 HA 连接详情。 */\n    private List<HAConnectionRuntimeInfo> haConnectionInfo = new ArrayList<>();",
        ),
        (
            "    private HAClientRuntimeInfo haClientRuntimeInfo = new HAClientRuntimeInfo();",
            "    /** 从节点 HA 客户端运行态（Master 侧为空对象）。 */\n    private HAClientRuntimeInfo haClientRuntimeInfo = new HAClientRuntimeInfo();",
        ),
        (
            "    public boolean isMaster() {",
            "    /** 返回是否为 Master。 */\n    public boolean isMaster() {",
        ),
        (
            "    public static class HAConnectionRuntimeInfo extends RemotingSerializable {",
            "    /** Master 视角下单条 HA 连接：从节点地址、复制进度与同步状态。 */\n    public static class HAConnectionRuntimeInfo extends RemotingSerializable {",
        ),
        (
            "        private String addr;",
            "        /** 从节点地址。 */\n        private String addr;",
        ),
        (
            "        private long slaveAckOffset;",
            "        /** 从节点已 ACK 的 CommitLog 偏移。 */\n        private long slaveAckOffset;",
        ),
        (
            "        private long diff;",
            "        /** 与 Master 最大偏移的差值。 */\n        private long diff;",
        ),
        (
            "        private boolean inSync;",
            "        /** 是否判定为同步副本。 */\n        private boolean inSync;",
        ),
        (
            "        private long transferredByteInSecond;",
            "        /** 每秒传输字节数。 */\n        private long transferredByteInSecond;",
        ),
        (
            "        private long transferFromWhere;",
            "        /** 本次 HA 传输起始偏移。 */\n        private long transferFromWhere;",
        ),
        (
            "    public static class HAClientRuntimeInfo extends RemotingSerializable {",
            "    /** 从节点 HA 客户端：Master 地址、读写时间戳及复制进度。 */\n    public static class HAClientRuntimeInfo extends RemotingSerializable {",
        ),
        (
            "        private String masterAddr;",
            "        /** 所连 Master 地址。 */\n        private String masterAddr;",
        ),
        (
            "        private long maxOffset;",
            "        /** 本地已同步的最大偏移。 */\n        private long maxOffset;",
        ),
        (
            "        private long lastReadTimestamp;",
            "        /** 最近一次从 Master 读取时间戳。 */\n        private long lastReadTimestamp;",
        ),
        (
            "        private long lastWriteTimestamp;",
            "        /** 最近一次写入本地 CommitLog 时间戳。 */\n        private long lastWriteTimestamp;",
        ),
        (
            "        private long masterFlushOffset;",
            "        /** Master 已刷盘偏移（从节点观测值）。 */\n        private long masterFlushOffset;",
        ),
        (
            "        private boolean isActivated = false;",
            "        /** HA 客户端是否已激活连接。 */\n        private boolean isActivated = false;",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/body/KVTable.java": [
        (
            "public class KVTable extends RemotingSerializable {",
            "/**\n * 通用键值表 Remoting 体，用于 Admin 接口传递字符串配置或统计项。\n */\npublic class KVTable extends RemotingSerializable {",
        ),
        (
            "    private HashMap<String, String> table = new HashMap<>();",
            "    /** 键值映射表。 */\n    private HashMap<String, String> table = new HashMap<>();",
        ),
        (
            "    public HashMap<String, String> getTable() {",
            "    /** 返回键值表。 */\n    public HashMap<String, String> getTable() {",
        ),
        (
            "    public void setTable(HashMap<String, String> table) {",
            "    /** 设置键值表。 */\n    public void setTable(HashMap<String, String> table) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/body/LiteSubscriptionCtlRequestBody.java": [
        (
            "public class LiteSubscriptionCtlRequestBody extends RemotingSerializable {",
            "/**\n * Lite 订阅关系批量变更请求体，携带待增删的 {@link LiteSubscriptionDTO} 集合。\n */\npublic class LiteSubscriptionCtlRequestBody extends RemotingSerializable {",
        ),
        (
            "    private Set<LiteSubscriptionDTO> subscriptionSet;",
            "    /** 待处理的 Lite 订阅项集合。 */\n    private Set<LiteSubscriptionDTO> subscriptionSet;",
        ),
        (
            "    public void setSubscriptionSet(Set<LiteSubscriptionDTO> subscriptionSet) {",
            "    /** 设置订阅项集合。 */\n    public void setSubscriptionSet(Set<LiteSubscriptionDTO> subscriptionSet) {",
        ),
        (
            "    public Set<LiteSubscriptionDTO> getSubscriptionSet() {",
            "    /** 返回订阅项集合。 */\n    public Set<LiteSubscriptionDTO> getSubscriptionSet() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/body/LockBatchRequestBody.java": [
        (
            "public class LockBatchRequestBody extends RemotingSerializable {",
            "/**\n * 顺序消费批量加锁请求：指定 Group、客户端及待锁定的 MessageQueue 集合。\n */\npublic class LockBatchRequestBody extends RemotingSerializable {",
        ),
        (
            "    private String consumerGroup;",
            "    /** 消费者 Group。 */\n    private String consumerGroup;",
        ),
        (
            "    private String clientId;",
            "    /** 发起加锁的客户端 ID。 */\n    private String clientId;",
        ),
        (
            "    private boolean onlyThisBroker = false;",
            "    /** 为 true 时仅在本 Broker 上加锁。 */\n    private boolean onlyThisBroker = false;",
        ),
        (
            "    private Set<MessageQueue> mqSet = new HashSet<>();",
            "    /** 待加锁的 MessageQueue 集合。 */\n    private Set<MessageQueue> mqSet = new HashSet<>();",
        ),
        (
            "    public boolean isOnlyThisBroker() {",
            "    /** 返回是否仅锁当前 Broker。 */\n    public boolean isOnlyThisBroker() {",
        ),
        (
            "    public Set<MessageQueue> getMqSet() {",
            "    /** 返回待锁队列集合。 */\n    public Set<MessageQueue> getMqSet() {",
        ),
        (
            "    @Override\n    public String toString() {",
            "    /** 返回加锁请求可读字符串。 */\n    @Override\n    public String toString() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/body/LockBatchResponseBody.java": [
        (
            "public class LockBatchResponseBody extends RemotingSerializable {",
            "/**\n * 顺序消费批量加锁响应：返回 Broker 成功锁定的 MessageQueue 集合。\n */\npublic class LockBatchResponseBody extends RemotingSerializable {",
        ),
        (
            "    private Set<MessageQueue> lockOKMQSet = new HashSet<>();",
            "    /** 加锁成功的 MessageQueue 集合。 */\n    private Set<MessageQueue> lockOKMQSet = new HashSet<>();",
        ),
        (
            "    public Set<MessageQueue> getLockOKMQSet() {",
            "    /** 返回加锁成功队列。 */\n    public Set<MessageQueue> getLockOKMQSet() {",
        ),
        (
            "    public void setLockOKMQSet(Set<MessageQueue> lockOKMQSet) {",
            "    /** 设置加锁成功队列。 */\n    public void setLockOKMQSet(Set<MessageQueue> lockOKMQSet) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/body/MessageRequestModeSerializeWrapper.java": [
        (
            "public class MessageRequestModeSerializeWrapper extends RemotingSerializable {",
            "/**\n * 消息拉取/推送模式配置序列化包装：Topic → Group → 模式请求体 三级映射。\n */\npublic class MessageRequestModeSerializeWrapper extends RemotingSerializable {",
        ),
        (
            "    private ConcurrentHashMap<String/* Topic */, ConcurrentHashMap<String/* Group */, SetMessageRequestModeRequestBody>>\n            messageRequestModeMap = new ConcurrentHashMap<>();",
            "    /** Topic →（Group → 拉取模式配置）并发映射。 */\n    private ConcurrentHashMap<String/* Topic */, ConcurrentHashMap<String/* Group */, SetMessageRequestModeRequestBody>>\n            messageRequestModeMap = new ConcurrentHashMap<>();",
        ),
        (
            "    public ConcurrentHashMap<String, ConcurrentHashMap<String, SetMessageRequestModeRequestBody>> getMessageRequestModeMap() {",
            "    /** 返回消息请求模式映射表。 */\n    public ConcurrentHashMap<String, ConcurrentHashMap<String, SetMessageRequestModeRequestBody>> getMessageRequestModeMap() {",
        ),
        (
            "    public void setMessageRequestModeMap(ConcurrentHashMap<String, ConcurrentHashMap<String, SetMessageRequestModeRequestBody>> messageRequestModeMap) {",
            "    /** 设置消息请求模式映射表。 */\n    public void setMessageRequestModeMap(ConcurrentHashMap<String, ConcurrentHashMap<String, SetMessageRequestModeRequestBody>> messageRequestModeMap) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/body/PopProcessQueueInfo.java": [
        (
            "public class PopProcessQueueInfo {",
            "/**\n * Pop 消费模式下 ProcessQueue 运行态：待 ACK 数、是否丢弃及最近 Pop 时间。\n */\npublic class PopProcessQueueInfo {",
        ),
        (
            "    private int waitAckCount;",
            "    /** 等待 ACK 的消息条数。 */\n    private int waitAckCount;",
        ),
        (
            "    private boolean droped;",
            "    /** 队列是否已被丢弃（如 rebalance 回收）。 */\n    private boolean droped;",
        ),
        (
            "    private long lastPopTimestamp;",
            "    /** 最近一次 Pop 拉取时间戳。 */\n    private long lastPopTimestamp;",
        ),
        (
            "    public int getWaitAckCount() {",
            "    /** 返回待 ACK 条数。 */\n    public int getWaitAckCount() {",
        ),
        (
            "    public boolean isDroped() {",
            "    /** 返回队列是否已丢弃。 */\n    public boolean isDroped() {",
        ),
        (
            "    public long getLastPopTimestamp() {",
            "    /** 返回最近 Pop 时间戳。 */\n    public long getLastPopTimestamp() {",
        ),
        (
            "    @Override\n    public String toString() {",
            "    /** 返回 Pop 队列运行态可读字符串。 */\n    @Override\n    public String toString() {",
        ),
    ],
}
