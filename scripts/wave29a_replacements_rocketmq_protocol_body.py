"""Chinese JavaDoc replacements for RocketMQ wave29a remoting protocol body [0:15]."""

R: dict[str, list[tuple[str, str]]] = {
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/body/CMResult.java": [
        (
            "public enum CMResult {",
            "/**\n * 直接消费（ConsumeMessageDirectly）结果码：标识 Broker 端模拟消费时的处理结论。\n */\npublic enum CMResult {",
        ),
        (
            "    CR_SUCCESS,",
            "    /** 消费成功。 */\n    CR_SUCCESS,",
        ),
        (
            "    CR_LATER,",
            "    /** 稍后重试。 */\n    CR_LATER,",
        ),
        (
            "    CR_ROLLBACK,",
            "    /** 事务回滚。 */\n    CR_ROLLBACK,",
        ),
        (
            "    CR_COMMIT,",
            "    /** 事务提交。 */\n    CR_COMMIT,",
        ),
        (
            "    CR_THROW_EXCEPTION,",
            "    /** 消费抛出异常。 */\n    CR_THROW_EXCEPTION,",
        ),
        (
            "    CR_RETURN_NULL,",
            "    /** 监听器返回 null。 */\n    CR_RETURN_NULL,",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/body/CheckClientRequestBody.java": [
        (
            "public class CheckClientRequestBody extends RemotingSerializable {",
            "/**\n * 客户端订阅一致性校验请求体：携带 clientId、消费组与订阅信息供 Broker 比对。\n */\npublic class CheckClientRequestBody extends RemotingSerializable {",
        ),
        (
            "    private String clientId;",
            "    /** 客户端唯一标识。 */\n    private String clientId;",
        ),
        (
            "    private String group;",
            "    /** 消费组名称。 */\n    private String group;",
        ),
        (
            "    private SubscriptionData subscriptionData;",
            "    /** 待校验的订阅数据。 */\n    private SubscriptionData subscriptionData;",
        ),
        (
            "    private String namespace;",
            "    /** 命名空间（多租户隔离）。 */\n    private String namespace;",
        ),
        (
            "    public String getClientId() {",
            "    /** 返回客户端 ID。 */\n    public String getClientId() {",
        ),
        (
            "    public void setClientId(String clientId) {",
            "    /** 设置客户端 ID。 */\n    public void setClientId(String clientId) {",
        ),
        (
            "    public String getGroup() {",
            "    /** 返回消费组。 */\n    public String getGroup() {",
        ),
        (
            "    public void setGroup(String group) {",
            "    /** 设置消费组。 */\n    public void setGroup(String group) {",
        ),
        (
            "    public SubscriptionData getSubscriptionData() {",
            "    /** 返回订阅数据。 */\n    public SubscriptionData getSubscriptionData() {",
        ),
        (
            "    public void setSubscriptionData(SubscriptionData subscriptionData) {",
            "    /** 设置订阅数据。 */\n    public void setSubscriptionData(SubscriptionData subscriptionData) {",
        ),
        (
            "    public String getNamespace() {",
            "    /** 返回命名空间。 */\n    public String getNamespace() {",
        ),
        (
            "    public void setNamespace(String namespace) {",
            "    /** 设置命名空间。 */\n    public void setNamespace(String namespace) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/body/ClusterInfo.java": [
        (
            "public class ClusterInfo extends RemotingSerializable {",
            "/**\n * NameServer 集群路由快照：Broker 地址表与集群-Broker 归属关系。\n */\npublic class ClusterInfo extends RemotingSerializable {",
        ),
        (
            "    private Map<String/* brokerName */, BrokerData> brokerAddrTable;",
            "    /** brokerName → {@link BrokerData} 地址映射。 */\n    private Map<String/* brokerName */, BrokerData> brokerAddrTable;",
        ),
        (
            "    private Map<String/* clusterName */, Set<String/* brokerName */>> clusterAddrTable;",
            "    /** clusterName → brokerName 集合。 */\n    private Map<String/* clusterName */, Set<String/* brokerName */>> clusterAddrTable;",
        ),
        (
            "    public Map<String, BrokerData> getBrokerAddrTable() {",
            "    /** 返回 Broker 地址表。 */\n    public Map<String, BrokerData> getBrokerAddrTable() {",
        ),
        (
            "    public void setBrokerAddrTable(Map<String, BrokerData> brokerAddrTable) {",
            "    /** 设置 Broker 地址表。 */\n    public void setBrokerAddrTable(Map<String, BrokerData> brokerAddrTable) {",
        ),
        (
            "    public Map<String, Set<String>> getClusterAddrTable() {",
            "    /** 返回集群归属表。 */\n    public Map<String, Set<String>> getClusterAddrTable() {",
        ),
        (
            "    public void setClusterAddrTable(Map<String, Set<String>> clusterAddrTable) {",
            "    /** 设置集群归属表。 */\n    public void setClusterAddrTable(Map<String, Set<String>> clusterAddrTable) {",
        ),
        (
            "    public String[] retrieveAllAddrByCluster(String cluster) {",
            "    /** 收集指定集群下全部 Broker 服务地址。 */\n    public String[] retrieveAllAddrByCluster(String cluster) {",
        ),
        (
            "    public String[] retrieveAllClusterNames() {",
            "    /** 返回所有已注册集群名称。 */\n    public String[] retrieveAllClusterNames() {",
        ),
        (
            "    @Override\n    public boolean equals(Object o) {",
            "    /** 比较两张路由表是否一致。 */\n    @Override\n    public boolean equals(Object o) {",
        ),
        (
            "    @Override\n    public int hashCode() {",
            "    /** 基于两张路由表计算哈希。 */\n    @Override\n    public int hashCode() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/body/Connection.java": [
        (
            "public class Connection {",
            "/**\n * 消费者连接元数据：clientId、网络地址、语言与协议版本。\n */\npublic class Connection {",
        ),
        (
            "    private String clientId;",
            "    /** 客户端唯一标识。 */\n    private String clientId;",
        ),
        (
            "    private String clientAddr;",
            "    /** 客户端网络地址。 */\n    private String clientAddr;",
        ),
        (
            "    private LanguageCode language;",
            "    /** 客户端 SDK 语言类型。 */\n    private LanguageCode language;",
        ),
        (
            "    private int version;",
            "    /** Remoting 协议版本号。 */\n    private int version;",
        ),
        (
            "    public String getClientId() {",
            "    /** 返回客户端 ID。 */\n    public String getClientId() {",
        ),
        (
            "    public void setClientId(String clientId) {",
            "    /** 设置客户端 ID。 */\n    public void setClientId(String clientId) {",
        ),
        (
            "    public String getClientAddr() {",
            "    /** 返回客户端地址。 */\n    public String getClientAddr() {",
        ),
        (
            "    public void setClientAddr(String clientAddr) {",
            "    /** 设置客户端地址。 */\n    public void setClientAddr(String clientAddr) {",
        ),
        (
            "    public LanguageCode getLanguage() {",
            "    /** 返回 SDK 语言。 */\n    public LanguageCode getLanguage() {",
        ),
        (
            "    public void setLanguage(LanguageCode language) {",
            "    /** 设置 SDK 语言。 */\n    public void setLanguage(LanguageCode language) {",
        ),
        (
            "    public int getVersion() {",
            "    /** 返回协议版本。 */\n    public int getVersion() {",
        ),
        (
            "    public void setVersion(int version) {",
            "    /** 设置协议版本。 */\n    public void setVersion(int version) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/body/ConsumeByWho.java": [
        (
            "public class ConsumeByWho extends RemotingSerializable {",
            "/**\n * 消息被哪些消费组消费/未消费的诊断结果：按 Topic、队列与位点定位。\n */\npublic class ConsumeByWho extends RemotingSerializable {",
        ),
        (
            "    private HashSet<String> consumedGroup = new HashSet<>();",
            "    /** 已消费该消息的消费组集合。 */\n    private HashSet<String> consumedGroup = new HashSet<>();",
        ),
        (
            "    private HashSet<String> notConsumedGroup = new HashSet<>();",
            "    /** 尚未消费该消息的消费组集合。 */\n    private HashSet<String> notConsumedGroup = new HashSet<>();",
        ),
        (
            "    private String topic;",
            "    /** 目标 Topic。 */\n    private String topic;",
        ),
        (
            "    private int queueId;",
            "    /** 队列 ID。 */\n    private int queueId;",
        ),
        (
            "    private long offset;",
            "    /** CommitLog 逻辑位点。 */\n    private long offset;",
        ),
        (
            "    public HashSet<String> getConsumedGroup() {",
            "    /** 返回已消费组集合。 */\n    public HashSet<String> getConsumedGroup() {",
        ),
        (
            "    public void setConsumedGroup(HashSet<String> consumedGroup) {",
            "    /** 设置已消费组集合。 */\n    public void setConsumedGroup(HashSet<String> consumedGroup) {",
        ),
        (
            "    public HashSet<String> getNotConsumedGroup() {",
            "    /** 返回未消费组集合。 */\n    public HashSet<String> getNotConsumedGroup() {",
        ),
        (
            "    public void setNotConsumedGroup(HashSet<String> notConsumedGroup) {",
            "    /** 设置未消费组集合。 */\n    public void setNotConsumedGroup(HashSet<String> notConsumedGroup) {",
        ),
        (
            "    public String getTopic() {",
            "    /** 返回 Topic。 */\n    public String getTopic() {",
        ),
        (
            "    public void setTopic(String topic) {",
            "    /** 设置 Topic。 */\n    public void setTopic(String topic) {",
        ),
        (
            "    public int getQueueId() {",
            "    /** 返回队列 ID。 */\n    public int getQueueId() {",
        ),
        (
            "    public void setQueueId(int queueId) {",
            "    /** 设置队列 ID。 */\n    public void setQueueId(int queueId) {",
        ),
        (
            "    public long getOffset() {",
            "    /** 返回位点。 */\n    public long getOffset() {",
        ),
        (
            "    public void setOffset(long offset) {",
            "    /** 设置位点。 */\n    public void setOffset(long offset) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/body/ConsumeMessageDirectlyResult.java": [
        (
            "public class ConsumeMessageDirectlyResult extends RemotingSerializable {",
            "/**\n * 直接消费（管理端触发）结果：顺序/自动提交标志、消费结论与耗时。\n */\npublic class ConsumeMessageDirectlyResult extends RemotingSerializable {",
        ),
        (
            "    private boolean order = false;",
            "    /** 是否顺序消费模式。 */\n    private boolean order = false;",
        ),
        (
            "    private boolean autoCommit = true;",
            "    /** 是否自动提交消费位点。 */\n    private boolean autoCommit = true;",
        ),
        (
            "    private CMResult consumeResult;",
            "    /** 消费处理结果码。 */\n    private CMResult consumeResult;",
        ),
        (
            "    private String remark;",
            "    /** 附加说明或异常信息。 */\n    private String remark;",
        ),
        (
            "    private long spentTimeMills;",
            "    /** 消费耗时（毫秒）。 */\n    private long spentTimeMills;",
        ),
        (
            "    public boolean isOrder() {",
            "    /** 是否顺序消费。 */\n    public boolean isOrder() {",
        ),
        (
            "    public void setOrder(boolean order) {",
            "    /** 设置顺序消费标志。 */\n    public void setOrder(boolean order) {",
        ),
        (
            "    public boolean isAutoCommit() {",
            "    /** 是否自动提交。 */\n    public boolean isAutoCommit() {",
        ),
        (
            "    public void setAutoCommit(boolean autoCommit) {",
            "    /** 设置自动提交标志。 */\n    public void setAutoCommit(boolean autoCommit) {",
        ),
        (
            "    public String getRemark() {",
            "    /** 返回备注信息。 */\n    public String getRemark() {",
        ),
        (
            "    public void setRemark(String remark) {",
            "    /** 设置备注信息。 */\n    public void setRemark(String remark) {",
        ),
        (
            "    public CMResult getConsumeResult() {",
            "    /** 返回消费结果码。 */\n    public CMResult getConsumeResult() {",
        ),
        (
            "    public void setConsumeResult(CMResult consumeResult) {",
            "    /** 设置消费结果码。 */\n    public void setConsumeResult(CMResult consumeResult) {",
        ),
        (
            "    public long getSpentTimeMills() {",
            "    /** 返回耗时毫秒数。 */\n    public long getSpentTimeMills() {",
        ),
        (
            "    public void setSpentTimeMills(long spentTimeMills) {",
            "    /** 设置耗时毫秒数。 */\n    public void setSpentTimeMills(long spentTimeMills) {",
        ),
        (
            "    @Override\n    public String toString() {",
            "    /** 返回便于日志排查的字符串。 */\n    @Override\n    public String toString() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/body/ConsumeQueueData.java": [
        (
            "public class ConsumeQueueData {",
            "/**\n * ConsumeQueue 单条索引条目：物理偏移、大小、Tags 哈希及扩展 JSON。\n */\npublic class ConsumeQueueData {",
        ),
        (
            "    private long physicOffset;",
            "    /** CommitLog 物理起始偏移。 */\n    private long physicOffset;",
        ),
        (
            "    private int physicSize;",
            "    /** CommitLog 消息体字节长度。 */\n    private int physicSize;",
        ),
        (
            "    private long tagsCode;",
            "    /** Tags 字符串哈希码。 */\n    private long tagsCode;",
        ),
        (
            "    private String extendDataJson;",
            "    /** 扩展属性 JSON 字符串。 */\n    private String extendDataJson;",
        ),
        (
            "    private String bitMap;",
            "    /** 事务/过滤相关位图。 */\n    private String bitMap;",
        ),
        (
            "    private boolean eval;",
            "    /** 是否已执行表达式求值。 */\n    private boolean eval;",
        ),
        (
            "    private String msg;",
            "    /** 关联消息摘要或调试文本。 */\n    private String msg;",
        ),
        (
            "    public long getPhysicOffset() {",
            "    /** 返回物理偏移。 */\n    public long getPhysicOffset() {",
        ),
        (
            "    public void setPhysicOffset(long physicOffset) {",
            "    /** 设置物理偏移。 */\n    public void setPhysicOffset(long physicOffset) {",
        ),
        (
            "    public int getPhysicSize() {",
            "    /** 返回物理大小。 */\n    public int getPhysicSize() {",
        ),
        (
            "    public void setPhysicSize(int physicSize) {",
            "    /** 设置物理大小。 */\n    public void setPhysicSize(int physicSize) {",
        ),
        (
            "    public long getTagsCode() {",
            "    /** 返回 Tags 哈希码。 */\n    public long getTagsCode() {",
        ),
        (
            "    public void setTagsCode(long tagsCode) {",
            "    /** 设置 Tags 哈希码。 */\n    public void setTagsCode(long tagsCode) {",
        ),
        (
            "    public String getExtendDataJson() {",
            "    /** 返回扩展 JSON。 */\n    public String getExtendDataJson() {",
        ),
        (
            "    public void setExtendDataJson(String extendDataJson) {",
            "    /** 设置扩展 JSON。 */\n    public void setExtendDataJson(String extendDataJson) {",
        ),
        (
            "    public String getBitMap() {",
            "    /** 返回位图字符串。 */\n    public String getBitMap() {",
        ),
        (
            "    public void setBitMap(String bitMap) {",
            "    /** 设置位图字符串。 */\n    public void setBitMap(String bitMap) {",
        ),
        (
            "    public boolean isEval() {",
            "    /** 是否已求值。 */\n    public boolean isEval() {",
        ),
        (
            "    public void setEval(boolean eval) {",
            "    /** 设置求值标志。 */\n    public void setEval(boolean eval) {",
        ),
        (
            "    public String getMsg() {",
            "    /** 返回消息摘要。 */\n    public String getMsg() {",
        ),
        (
            "    public void setMsg(String msg) {",
            "    /** 设置消息摘要。 */\n    public void setMsg(String msg) {",
        ),
        (
            "    @Override\n    public String toString() {",
            "    /** 返回调试字符串。 */\n    @Override\n    public String toString() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/body/ConsumeStatsList.java": [
        (
            "public class ConsumeStatsList extends RemotingSerializable {",
            "/**\n * 多 Broker 消费统计列表：按订阅组聚合 {@link ConsumeStats} 及总堆积量。\n */\npublic class ConsumeStatsList extends RemotingSerializable {",
        ),
        (
            "    private List<Map<String/*subscriptionGroupName*/, List<ConsumeStats>>> consumeStatsList = new ArrayList<>();",
            "    /** 订阅组 → 消费统计列表。 */\n    private List<Map<String/*subscriptionGroupName*/, List<ConsumeStats>>> consumeStatsList = new ArrayList<>();",
        ),
        (
            "    private String brokerAddr;",
            "    /** 统计来源 Broker 地址。 */\n    private String brokerAddr;",
        ),
        (
            "    private long totalDiff;",
            "    /** 全部队列总堆积量。 */\n    private long totalDiff;",
        ),
        (
            "    private long totalInflightDiff;",
            "    /** 在途（已拉取未 Ack）总堆积量。 */\n    private long totalInflightDiff;",
        ),
        (
            "    public List<Map<String, List<ConsumeStats>>> getConsumeStatsList() {",
            "    /** 返回消费统计列表。 */\n    public List<Map<String, List<ConsumeStats>>> getConsumeStatsList() {",
        ),
        (
            "    public void setConsumeStatsList(List<Map<String, List<ConsumeStats>>> consumeStatsList) {",
            "    /** 设置消费统计列表。 */\n    public void setConsumeStatsList(List<Map<String, List<ConsumeStats>>> consumeStatsList) {",
        ),
        (
            "    public String getBrokerAddr() {",
            "    /** 返回 Broker 地址。 */\n    public String getBrokerAddr() {",
        ),
        (
            "    public void setBrokerAddr(String brokerAddr) {",
            "    /** 设置 Broker 地址。 */\n    public void setBrokerAddr(String brokerAddr) {",
        ),
        (
            "    public long getTotalDiff() {",
            "    /** 返回总堆积量。 */\n    public long getTotalDiff() {",
        ),
        (
            "    public void setTotalDiff(long totalDiff) {",
            "    /** 设置总堆积量。 */\n    public void setTotalDiff(long totalDiff) {",
        ),
        (
            "    public long getTotalInflightDiff() {",
            "    /** 返回在途堆积量。 */\n    public long getTotalInflightDiff() {",
        ),
        (
            "    public void setTotalInflightDiff(long totalInflightDiff) {",
            "    /** 设置在途堆积量。 */\n    public void setTotalInflightDiff(long totalInflightDiff) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/body/ConsumeStatus.java": [
        (
            "public class ConsumeStatus {",
            "/**\n * 单 Topic 消费性能指标：拉取/消费 RT、TPS 及失败计数。\n */\npublic class ConsumeStatus {",
        ),
        (
            "    private double pullRT;",
            "    /** 拉取平均响应时间（毫秒）。 */\n    private double pullRT;",
        ),
        (
            "    private double pullTPS;",
            "    /** 拉取吞吐量（条/秒）。 */\n    private double pullTPS;",
        ),
        (
            "    private double consumeRT;",
            "    /** 消费平均耗时（毫秒）。 */\n    private double consumeRT;",
        ),
        (
            "    private double consumeOKTPS;",
            "    /** 消费成功 TPS。 */\n    private double consumeOKTPS;",
        ),
        (
            "    private double consumeFailedTPS;",
            "    /** 消费失败 TPS。 */\n    private double consumeFailedTPS;",
        ),
        (
            "    private long consumeFailedMsgs;",
            "    /** 近一小时消费失败消息数。 */\n    private long consumeFailedMsgs;",
        ),
        (
            "    public double getPullRT() {",
            "    /** 返回拉取 RT。 */\n    public double getPullRT() {",
        ),
        (
            "    public void setPullRT(double pullRT) {",
            "    /** 设置拉取 RT。 */\n    public void setPullRT(double pullRT) {",
        ),
        (
            "    public double getPullTPS() {",
            "    /** 返回拉取 TPS。 */\n    public double getPullTPS() {",
        ),
        (
            "    public void setPullTPS(double pullTPS) {",
            "    /** 设置拉取 TPS。 */\n    public void setPullTPS(double pullTPS) {",
        ),
        (
            "    public double getConsumeRT() {",
            "    /** 返回消费 RT。 */\n    public double getConsumeRT() {",
        ),
        (
            "    public void setConsumeRT(double consumeRT) {",
            "    /** 设置消费 RT。 */\n    public void setConsumeRT(double consumeRT) {",
        ),
        (
            "    public double getConsumeOKTPS() {",
            "    /** 返回消费成功 TPS。 */\n    public double getConsumeOKTPS() {",
        ),
        (
            "    public void setConsumeOKTPS(double consumeOKTPS) {",
            "    /** 设置消费成功 TPS。 */\n    public void setConsumeOKTPS(double consumeOKTPS) {",
        ),
        (
            "    public double getConsumeFailedTPS() {",
            "    /** 返回消费失败 TPS。 */\n    public double getConsumeFailedTPS() {",
        ),
        (
            "    public void setConsumeFailedTPS(double consumeFailedTPS) {",
            "    /** 设置消费失败 TPS。 */\n    public void setConsumeFailedTPS(double consumeFailedTPS) {",
        ),
        (
            "    public long getConsumeFailedMsgs() {",
            "    /** 返回失败消息数。 */\n    public long getConsumeFailedMsgs() {",
        ),
        (
            "    public void setConsumeFailedMsgs(long consumeFailedMsgs) {",
            "    /** 设置失败消息数。 */\n    public void setConsumeFailedMsgs(long consumeFailedMsgs) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/body/ConsumerConnection.java": [
        (
            "public class ConsumerConnection extends RemotingSerializable {",
            "/**\n * 消费组连接快照：在线客户端、订阅表及消费模式/起始位点策略。\n */\npublic class ConsumerConnection extends RemotingSerializable {",
        ),
        (
            "    private HashSet<Connection> connectionSet = new HashSet<>();",
            "    /** 当前在线客户端连接集合。 */\n    private HashSet<Connection> connectionSet = new HashSet<>();",
        ),
        (
            "    private ConcurrentMap<String/* Topic */, SubscriptionData> subscriptionTable =\n        new ConcurrentHashMap<>();",
            "    /** Topic → 订阅数据并发映射。 */\n    private ConcurrentMap<String/* Topic */, SubscriptionData> subscriptionTable =\n        new ConcurrentHashMap<>();",
        ),
        (
            "    private ConsumeType consumeType;",
            "    /** 消费类型（Push/Pull/Pop 等）。 */\n    private ConsumeType consumeType;",
        ),
        (
            "    private MessageModel messageModel;",
            "    /** 消息模型（集群/广播）。 */\n    private MessageModel messageModel;",
        ),
        (
            "    private ConsumeFromWhere consumeFromWhere;",
            "    /** 首次启动消费起始策略。 */\n    private ConsumeFromWhere consumeFromWhere;",
        ),
        (
            "    public int computeMinVersion() {",
            "    /** 计算连接集合中最低的 Remoting 协议版本。 */\n    public int computeMinVersion() {",
        ),
        (
            "    public HashSet<Connection> getConnectionSet() {",
            "    /** 返回连接集合。 */\n    public HashSet<Connection> getConnectionSet() {",
        ),
        (
            "    public void setConnectionSet(HashSet<Connection> connectionSet) {",
            "    /** 设置连接集合。 */\n    public void setConnectionSet(HashSet<Connection> connectionSet) {",
        ),
        (
            "    public ConcurrentMap<String, SubscriptionData> getSubscriptionTable() {",
            "    /** 返回订阅表。 */\n    public ConcurrentMap<String, SubscriptionData> getSubscriptionTable() {",
        ),
        (
            "    public void setSubscriptionTable(ConcurrentHashMap<String, SubscriptionData> subscriptionTable) {",
            "    /** 设置订阅表。 */\n    public void setSubscriptionTable(ConcurrentHashMap<String, SubscriptionData> subscriptionTable) {",
        ),
        (
            "    public ConsumeType getConsumeType() {",
            "    /** 返回消费类型。 */\n    public ConsumeType getConsumeType() {",
        ),
        (
            "    public void setConsumeType(ConsumeType consumeType) {",
            "    /** 设置消费类型。 */\n    public void setConsumeType(ConsumeType consumeType) {",
        ),
        (
            "    public MessageModel getMessageModel() {",
            "    /** 返回消息模型。 */\n    public MessageModel getMessageModel() {",
        ),
        (
            "    public void setMessageModel(MessageModel messageModel) {",
            "    /** 设置消息模型。 */\n    public void setMessageModel(MessageModel messageModel) {",
        ),
        (
            "    public ConsumeFromWhere getConsumeFromWhere() {",
            "    /** 返回起始消费策略。 */\n    public ConsumeFromWhere getConsumeFromWhere() {",
        ),
        (
            "    public void setConsumeFromWhere(ConsumeFromWhere consumeFromWhere) {",
            "    /** 设置起始消费策略。 */\n    public void setConsumeFromWhere(ConsumeFromWhere consumeFromWhere) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/body/ConsumerOffsetSerializeWrapper.java": [
        (
            "public class ConsumerOffsetSerializeWrapper extends RemotingSerializable {",
            "/**\n * 消费位点持久化包装：topic@group → queueId → offset 及 {@link DataVersion}。\n */\npublic class ConsumerOffsetSerializeWrapper extends RemotingSerializable {",
        ),
        (
            "    private ConcurrentMap<String/* topic@group */, ConcurrentMap<Integer, Long>> offsetTable =\n        new ConcurrentHashMap<>(512);",
            "    /** topic@group → (queueId → 消费位点)。 */\n    private ConcurrentMap<String/* topic@group */, ConcurrentMap<Integer, Long>> offsetTable =\n        new ConcurrentHashMap<>(512);",
        ),
        (
            "    private DataVersion dataVersion;",
            "    /** 位点表版本号，用于增量同步。 */\n    private DataVersion dataVersion;",
        ),
        (
            "    public ConcurrentMap<String, ConcurrentMap<Integer, Long>> getOffsetTable() {",
            "    /** 返回位点表。 */\n    public ConcurrentMap<String, ConcurrentMap<Integer, Long>> getOffsetTable() {",
        ),
        (
            "    public void setOffsetTable(ConcurrentMap<String, ConcurrentMap<Integer, Long>> offsetTable) {",
            "    /** 设置位点表。 */\n    public void setOffsetTable(ConcurrentMap<String, ConcurrentMap<Integer, Long>> offsetTable) {",
        ),
        (
            "    public DataVersion getDataVersion() {",
            "    /** 返回数据版本。 */\n    public DataVersion getDataVersion() {",
        ),
        (
            "    public void setDataVersion(DataVersion dataVersion) {",
            "    /** 设置数据版本。 */\n    public void setDataVersion(DataVersion dataVersion) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/body/ConsumerRunningInfo.java": [
        (
            "public class ConsumerRunningInfo extends RemotingSerializable {",
            "/**\n * 消费者运行时诊断快照：属性、订阅、ProcessQueue、Pop 队列及性能指标。\n * 提供订阅一致性、Rebalance 与 ProcessQueue 阻塞分析工具方法。\n */\npublic class ConsumerRunningInfo extends RemotingSerializable {",
        ),
        (
            "    public static final String PROP_NAMESERVER_ADDR = \"PROP_NAMESERVER_ADDR\";",
            "    /** 属性键：NameServer 地址。 */\n    public static final String PROP_NAMESERVER_ADDR = \"PROP_NAMESERVER_ADDR\";",
        ),
        (
            "    public static final String PROP_THREADPOOL_CORE_SIZE = \"PROP_THREADPOOL_CORE_SIZE\";",
            "    /** 属性键：消费线程池核心数。 */\n    public static final String PROP_THREADPOOL_CORE_SIZE = \"PROP_THREADPOOL_CORE_SIZE\";",
        ),
        (
            "    public static final String PROP_CONSUME_ORDERLY = \"PROP_CONSUMEORDERLY\";",
            "    /** 属性键：是否顺序消费。 */\n    public static final String PROP_CONSUME_ORDERLY = \"PROP_CONSUMEORDERLY\";",
        ),
        (
            "    public static final String PROP_CONSUME_TYPE = \"PROP_CONSUME_TYPE\";",
            "    /** 属性键：消费类型。 */\n    public static final String PROP_CONSUME_TYPE = \"PROP_CONSUME_TYPE\";",
        ),
        (
            "    public static final String PROP_CLIENT_VERSION = \"PROP_CLIENT_VERSION\";",
            "    /** 属性键：客户端版本。 */\n    public static final String PROP_CLIENT_VERSION = \"PROP_CLIENT_VERSION\";",
        ),
        (
            "    public static final String PROP_CONSUMER_START_TIMESTAMP = \"PROP_CONSUMER_START_TIMESTAMP\";",
            "    /** 属性键：消费者启动时间戳。 */\n    public static final String PROP_CONSUMER_START_TIMESTAMP = \"PROP_CONSUMER_START_TIMESTAMP\";",
        ),
        (
            "    private Properties properties = new Properties();",
            "    /** 客户端运行时属性集合。 */\n    private Properties properties = new Properties();",
        ),
        (
            "    private TreeSet<SubscriptionData> subscriptionSet = new TreeSet<>();",
            "    /** 当前订阅集合（有序）。 */\n    private TreeSet<SubscriptionData> subscriptionSet = new TreeSet<>();",
        ),
        (
            "    private TreeMap<MessageQueue, ProcessQueueInfo> mqTable = new TreeMap<>();",
            "    /** Push/Pull 模式 ProcessQueue 表。 */\n    private TreeMap<MessageQueue, ProcessQueueInfo> mqTable = new TreeMap<>();",
        ),
        (
            "    private TreeMap<MessageQueue, PopProcessQueueInfo> mqPopTable = new TreeMap<>();",
            "    /** Pop 模式 ProcessQueue 表。 */\n    private TreeMap<MessageQueue, PopProcessQueueInfo> mqPopTable = new TreeMap<>();",
        ),
        (
            "    private TreeMap<String/* Topic */, ConsumeStatus> statusTable = new TreeMap<>();",
            "    /** Topic → 消费性能指标。 */\n    private TreeMap<String/* Topic */, ConsumeStatus> statusTable = new TreeMap<>();",
        ),
        (
            "    private TreeMap<String, String> userConsumerInfo = new TreeMap<>();",
            "    /** 用户自定义扩展信息。 */\n    private TreeMap<String, String> userConsumerInfo = new TreeMap<>();",
        ),
        (
            "    private String jstack;",
            "    /** 消费线程 jstack 快照。 */\n    private String jstack;",
        ),
        (
            "    public static boolean analyzeSubscription(final TreeMap<String/* clientId */, ConsumerRunningInfo> criTable) {",
            "    /** 校验同组各客户端订阅是否一致（Push 且启动超过 2 分钟时生效）。 */\n    public static boolean analyzeSubscription(final TreeMap<String/* clientId */, ConsumerRunningInfo> criTable) {",
        ),
        (
            "                        // Different subscription in the same group of consumer",
            "                        // 同组消费者订阅不一致",
        ),
        (
            "    public static boolean isPushType(ConsumerRunningInfo consumerRunningInfo) {",
            "    /** 判断是否为 Push（被动）消费类型。 */\n    public static boolean isPushType(ConsumerRunningInfo consumerRunningInfo) {",
        ),
        (
            "    public static boolean analyzeRebalance(final TreeMap<String/* clientId */, ConsumerRunningInfo> criTable) {",
            "    /** Rebalance 一致性分析（当前恒返回 true）。 */\n    public static boolean analyzeRebalance(final TreeMap<String/* clientId */, ConsumerRunningInfo> criTable) {",
        ),
        (
            "    public static String analyzeProcessQueue(final String clientId, ConsumerRunningInfo info) {",
            "    /** 分析 ProcessQueue 锁/消费阻塞并生成诊断文本。 */\n    public static String analyzeProcessQueue(final String clientId, ConsumerRunningInfo info) {",
        ),
        (
            "    public Properties getProperties() {",
            "    /** 返回运行时属性。 */\n    public Properties getProperties() {",
        ),
        (
            "    public void setProperties(Properties properties) {",
            "    /** 设置运行时属性。 */\n    public void setProperties(Properties properties) {",
        ),
        (
            "    public TreeSet<SubscriptionData> getSubscriptionSet() {",
            "    /** 返回订阅集合。 */\n    public TreeSet<SubscriptionData> getSubscriptionSet() {",
        ),
        (
            "    public void setSubscriptionSet(TreeSet<SubscriptionData> subscriptionSet) {",
            "    /** 设置订阅集合。 */\n    public void setSubscriptionSet(TreeSet<SubscriptionData> subscriptionSet) {",
        ),
        (
            "    public TreeMap<MessageQueue, ProcessQueueInfo> getMqTable() {",
            "    /** 返回 ProcessQueue 表。 */\n    public TreeMap<MessageQueue, ProcessQueueInfo> getMqTable() {",
        ),
        (
            "    public void setMqTable(TreeMap<MessageQueue, ProcessQueueInfo> mqTable) {",
            "    /** 设置 ProcessQueue 表。 */\n    public void setMqTable(TreeMap<MessageQueue, ProcessQueueInfo> mqTable) {",
        ),
        (
            "    public TreeMap<String, ConsumeStatus> getStatusTable() {",
            "    /** 返回性能指标表。 */\n    public TreeMap<String, ConsumeStatus> getStatusTable() {",
        ),
        (
            "    public void setStatusTable(TreeMap<String, ConsumeStatus> statusTable) {",
            "    /** 设置性能指标表。 */\n    public void setStatusTable(TreeMap<String, ConsumeStatus> statusTable) {",
        ),
        (
            "    public TreeMap<String, String> getUserConsumerInfo() {",
            "    /** 返回用户扩展信息。 */\n    public TreeMap<String, String> getUserConsumerInfo() {",
        ),
        (
            "    public String formatString() {",
            "    /** 格式化为管理端可读的完整诊断报告。 */\n    public String formatString() {",
        ),
        (
            "    public String getJstack() {",
            "    /** 返回 jstack 文本。 */\n    public String getJstack() {",
        ),
        (
            "    public void setJstack(String jstack) {",
            "    /** 设置 jstack 文本。 */\n    public void setJstack(String jstack) {",
        ),
        (
            "    public TreeMap<MessageQueue, PopProcessQueueInfo> getMqPopTable() {",
            "    /** 返回 Pop ProcessQueue 表。 */\n    public TreeMap<MessageQueue, PopProcessQueueInfo> getMqPopTable() {",
        ),
        (
            "    public void setMqPopTable(\n        TreeMap<MessageQueue, PopProcessQueueInfo> mqPopTable) {",
            "    /** 设置 Pop ProcessQueue 表。 */\n    public void setMqPopTable(\n        TreeMap<MessageQueue, PopProcessQueueInfo> mqPopTable) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/body/CreateTopicListRequestBody.java": [
        (
            "public class CreateTopicListRequestBody extends RemotingSerializable {",
            "/**\n * 批量创建 Topic 请求体：携带 {@link TopicConfig} 列表。\n */\npublic class CreateTopicListRequestBody extends RemotingSerializable {",
        ),
        (
            "    @CFNotNull\n    private List<TopicConfig> topicConfigList;",
            "    /** 待创建的 Topic 配置列表（非空）。 */\n    @CFNotNull\n    private List<TopicConfig> topicConfigList;",
        ),
        (
            "    public CreateTopicListRequestBody() {}",
            "    /** 默认构造，供反序列化使用。 */\n    public CreateTopicListRequestBody() {}",
        ),
        (
            "    public CreateTopicListRequestBody(List<TopicConfig> topicConfigList) {",
            "    /** 以配置列表初始化。 */\n    public CreateTopicListRequestBody(List<TopicConfig> topicConfigList) {",
        ),
        (
            "    public List<TopicConfig> getTopicConfigList() {",
            "    /** 返回 Topic 配置列表。 */\n    public List<TopicConfig> getTopicConfigList() {",
        ),
        (
            "    public void setTopicConfigList(List<TopicConfig> topicConfigList) {",
            "    /** 设置 Topic 配置列表。 */\n    public void setTopicConfigList(List<TopicConfig> topicConfigList) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/body/ElectMasterResponseBody.java": [
        (
            "public class ElectMasterResponseBody extends RemotingSerializable {",
            "/**\n * Controller 选举 Master 响应体：Broker 成员组与同步状态集（syncStateSet）。\n */\npublic class ElectMasterResponseBody extends RemotingSerializable {",
        ),
        (
            "    private BrokerMemberGroup brokerMemberGroup;",
            "    /** 选举后的 Broker 成员组信息。 */\n    private BrokerMemberGroup brokerMemberGroup;",
        ),
        (
            "    private Set<Long> syncStateSet;",
            "    /** 处于同步状态的 brokerId 集合。 */\n    private Set<Long> syncStateSet;",
        ),
        (
            "    // Provide default constructor for serializer",
            "    // 供序列化框架使用的默认构造",
        ),
        (
            "    public ElectMasterResponseBody() {",
            "    /** 默认构造，初始化空 syncStateSet。 */\n    public ElectMasterResponseBody() {",
        ),
        (
            "    public ElectMasterResponseBody(final Set<Long> syncStateSet) {",
            "    /** 仅指定 syncStateSet 的构造。 */\n    public ElectMasterResponseBody(final Set<Long> syncStateSet) {",
        ),
        (
            "    public ElectMasterResponseBody(final BrokerMemberGroup brokerMemberGroup, final Set<Long> syncStateSet) {",
            "    /** 指定成员组与 syncStateSet 的完整构造。 */\n    public ElectMasterResponseBody(final BrokerMemberGroup brokerMemberGroup, final Set<Long> syncStateSet) {",
        ),
        (
            "    @Override\n    public boolean equals(Object o) {",
            "    /** 比较成员组与 syncStateSet 是否相等。 */\n    @Override\n    public boolean equals(Object o) {",
        ),
        (
            "    @Override\n    public int hashCode() {",
            "    /** 基于成员组与 syncStateSet 计算哈希。 */\n    @Override\n    public int hashCode() {",
        ),
        (
            "    @Override\n    public String toString() {",
            "    /** 返回调试字符串。 */\n    @Override\n    public String toString() {",
        ),
        (
            "    public void setBrokerMemberGroup(BrokerMemberGroup brokerMemberGroup) {",
            "    /** 设置 Broker 成员组。 */\n    public void setBrokerMemberGroup(BrokerMemberGroup brokerMemberGroup) {",
        ),
        (
            "    public BrokerMemberGroup getBrokerMemberGroup() {",
            "    /** 返回 Broker 成员组。 */\n    public BrokerMemberGroup getBrokerMemberGroup() {",
        ),
        (
            "    public void setSyncStateSet(Set<Long> syncStateSet) {",
            "    /** 设置 syncStateSet。 */\n    public void setSyncStateSet(Set<Long> syncStateSet) {",
        ),
        (
            "    public Set<Long> getSyncStateSet() {",
            "    /** 返回 syncStateSet。 */\n    public Set<Long> getSyncStateSet() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/body/EpochEntryCache.java": [
        (
            "public class EpochEntryCache extends RemotingSerializable {",
            "/**\n * Broker Epoch 条目缓存：集群/Broker 标识、Epoch 列表及最大 CommitLog 位点。\n * 用于 Controller 模式下副本同步状态上报。\n */\npublic class EpochEntryCache extends RemotingSerializable {",
        ),
        (
            "    private String clusterName;",
            "    /** 所属集群名称。 */\n    private String clusterName;",
        ),
        (
            "    private String brokerName;",
            "    /** Broker 逻辑名称。 */\n    private String brokerName;",
        ),
        (
            "    private long brokerId;",
            "    /** Broker 副本 ID（0 通常为 Master）。 */\n    private long brokerId;",
        ),
        (
            "    private List<EpochEntry> epochList;",
            "    /** Epoch 条目列表（leader epoch 与起始位点）。 */\n    private List<EpochEntry> epochList;",
        ),
        (
            "    private long maxOffset;",
            "    /** 当前最大 CommitLog 位点。 */\n    private long maxOffset;",
        ),
        (
            "    public EpochEntryCache(String clusterName, String brokerName, long brokerId, List<EpochEntry> epochList, long maxOffset) {",
            "    /** 全字段构造。 */\n    public EpochEntryCache(String clusterName, String brokerName, long brokerId, List<EpochEntry> epochList, long maxOffset) {",
        ),
        (
            "    public String getClusterName() {",
            "    /** 返回集群名称。 */\n    public String getClusterName() {",
        ),
        (
            "    public void setClusterName(String clusterName) {",
            "    /** 设置集群名称。 */\n    public void setClusterName(String clusterName) {",
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
            "    public long getBrokerId() {",
            "    /** 返回 Broker ID。 */\n    public long getBrokerId() {",
        ),
        (
            "    public void setBrokerId(long brokerId) {",
            "    /** 设置 Broker ID。 */\n    public void setBrokerId(long brokerId) {",
        ),
        (
            "    public List<EpochEntry> getEpochList() {",
            "    /** 返回 Epoch 列表。 */\n    public List<EpochEntry> getEpochList() {",
        ),
        (
            "    public void setEpochList(List<EpochEntry> epochList) {",
            "    /** 设置 Epoch 列表。 */\n    public void setEpochList(List<EpochEntry> epochList) {",
        ),
        (
            "    public long getMaxOffset() {",
            "    /** 返回最大位点。 */\n    public long getMaxOffset() {",
        ),
        (
            "    public void setMaxOffset(long maxOffset) {",
            "    /** 设置最大位点。 */\n    public void setMaxOffset(long maxOffset) {",
        ),
        (
            "    @Override\n    public String toString() {",
            "    /** 返回调试字符串。 */\n    @Override\n    public String toString() {",
        ),
    ],
}
