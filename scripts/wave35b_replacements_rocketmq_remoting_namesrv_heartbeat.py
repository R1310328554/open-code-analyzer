"""Chinese JavaDoc replacements for RocketMQ wave35b remoting namesrv/heartbeat/route [15:30]."""

R: dict[str, list[tuple[str, str]]] = {
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/namesrv/RegisterBrokerResponseHeader.java": [
        (
            "public class RegisterBrokerResponseHeader implements CommandCustomHeader {",
            "/**\n * Broker 注册 NameServer 的响应头：返回 HA 服务地址与 Master 地址供副本同步。\n */\npublic class RegisterBrokerResponseHeader implements CommandCustomHeader {",
        ),
        (
            "    @CFNullable\n    private String haServerAddr;",
            "    /** HA 高可用服务地址（可为空）。 */\n    @CFNullable\n    private String haServerAddr;",
        ),
        (
            "    @CFNullable\n    private String masterAddr;",
            "    /** 当前 Master Broker 地址（可为空）。 */\n    @CFNullable\n    private String masterAddr;",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
            "    /** 校验响应头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
        ),
        (
            "    public String getHaServerAddr() {",
            "    /** 返回 HA 服务地址。 */\n    public String getHaServerAddr() {",
        ),
        (
            "    public void setHaServerAddr(String haServerAddr) {",
            "    /** 设置 HA 服务地址。 */\n    public void setHaServerAddr(String haServerAddr) {",
        ),
        (
            "    public String getMasterAddr() {",
            "    /** 返回 Master 地址。 */\n    public String getMasterAddr() {",
        ),
        (
            "    public void setMasterAddr(String masterAddr) {",
            "    /** 设置 Master 地址。 */\n    public void setMasterAddr(String masterAddr) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/namesrv/RegisterOrderTopicRequestHeader.java": [
        (
            "public class RegisterOrderTopicRequestHeader implements CommandCustomHeader {",
            "/**\n * 向 NameServer 注册顺序 Topic 的请求头：携带 Topic 名与顺序 Topic 配置串。\n */\npublic class RegisterOrderTopicRequestHeader implements CommandCustomHeader {",
        ),
        (
            "    @CFNotNull\n    private String topic;",
            "    /** 顺序 Topic 名称。 */\n    @CFNotNull\n    private String topic;",
        ),
        (
            "    @CFNotNull\n    private String orderTopicString;",
            "    /** 顺序 Topic 配置串（队列与 Broker 映射描述）。 */\n    @CFNotNull\n    private String orderTopicString;",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n\n    }",
            "    /** 校验请求头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n\n    }",
        ),
        (
            "    public String getTopic() {",
            "    /** 返回 Topic 名称。 */\n    public String getTopic() {",
        ),
        (
            "    public void setTopic(String topic) {",
            "    /** 设置 Topic 名称。 */\n    public void setTopic(String topic) {",
        ),
        (
            "    public String getOrderTopicString() {",
            "    /** 返回顺序 Topic 配置串。 */\n    public String getOrderTopicString() {",
        ),
        (
            "    public void setOrderTopicString(String orderTopicString) {",
            "    /** 设置顺序 Topic 配置串。 */\n    public void setOrderTopicString(String orderTopicString) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/namesrv/RegisterTopicRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.REGISTER_TOPIC_IN_NAMESRV, resource = ResourceType.CLUSTER, action = Action.UPDATE)\npublic class RegisterTopicRequestHeader extends TopicRequestHeader {",
            "/**\n * 向 NameServer 注册 Topic 路由的请求头：指定待注册的 Topic 名称。\n */\n@RocketMQAction(value = RequestCode.REGISTER_TOPIC_IN_NAMESRV, resource = ResourceType.CLUSTER, action = Action.UPDATE)\npublic class RegisterTopicRequestHeader extends TopicRequestHeader {",
        ),
        (
            "    @CFNotNull\n    private String topic;",
            "    /** 待注册 Topic 名称。 */\n    @CFNotNull\n    private String topic;",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
            "    /** 校验请求头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
        ),
        (
            "    public String getTopic() {",
            "    /** 返回 Topic 名称。 */\n    public String getTopic() {",
        ),
        (
            "    public void setTopic(String topic) {",
            "    /** 设置 Topic 名称。 */\n    public void setTopic(String topic) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/namesrv/UnRegisterBrokerRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.UNREGISTER_BROKER, resource = ResourceType.CLUSTER, action = Action.UPDATE)\npublic class UnRegisterBrokerRequestHeader implements CommandCustomHeader {",
            "/**\n * 从 NameServer 注销 Broker 的请求头：携带集群名、Broker 组名、地址与 brokerId。\n */\n@RocketMQAction(value = RequestCode.UNREGISTER_BROKER, resource = ResourceType.CLUSTER, action = Action.UPDATE)\npublic class UnRegisterBrokerRequestHeader implements CommandCustomHeader {",
        ),
        (
            "    @CFNotNull\n    private String brokerName;",
            "    /** Broker 组名称。 */\n    @CFNotNull\n    private String brokerName;",
        ),
        (
            "    @CFNotNull\n    private String brokerAddr;",
            "    /** 待注销 Broker 实例地址。 */\n    @CFNotNull\n    private String brokerAddr;",
        ),
        (
            "    @CFNotNull\n    @RocketMQResource(ResourceType.CLUSTER)\n    private String clusterName;",
            "    /** 所属集群名称。 */\n    @CFNotNull\n    @RocketMQResource(ResourceType.CLUSTER)\n    private String clusterName;",
        ),
        (
            "    @CFNotNull\n    private Long brokerId;",
            "    /** Broker 实例 ID（0 为 Master）。 */\n    @CFNotNull\n    private Long brokerId;",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
            "    /** 校验请求头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
        ),
        (
            "    public String getBrokerName() {",
            "    /** 返回 Broker 组名称。 */\n    public String getBrokerName() {",
        ),
        (
            "    public void setBrokerName(String brokerName) {",
            "    /** 设置 Broker 组名称。 */\n    public void setBrokerName(String brokerName) {",
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
            "    public String getClusterName() {",
            "    /** 返回集群名称。 */\n    public String getClusterName() {",
        ),
        (
            "    public void setClusterName(String clusterName) {",
            "    /** 设置集群名称。 */\n    public void setClusterName(String clusterName) {",
        ),
        (
            "    public Long getBrokerId() {",
            "    /** 返回 brokerId。 */\n    public Long getBrokerId() {",
        ),
        (
            "    public void setBrokerId(Long brokerId) {",
            "    /** 设置 brokerId。 */\n    public void setBrokerId(Long brokerId) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/namesrv/WipeWritePermOfBrokerRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.WIPE_WRITE_PERM_OF_BROKER, resource = ResourceType.CLUSTER, action = Action.UPDATE)\npublic class WipeWritePermOfBrokerRequestHeader implements CommandCustomHeader {",
            "/**\n * 清除 Broker 写权限的请求头：指定 Broker 组名，NameServer 将撤销其 Topic 写权限。\n */\n@RocketMQAction(value = RequestCode.WIPE_WRITE_PERM_OF_BROKER, resource = ResourceType.CLUSTER, action = Action.UPDATE)\npublic class WipeWritePermOfBrokerRequestHeader implements CommandCustomHeader {",
        ),
        (
            "    @CFNotNull\n    private String brokerName;",
            "    /** 目标 Broker 组名称。 */\n    @CFNotNull\n    private String brokerName;",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n\n    }",
            "    /** 校验请求头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n\n    }",
        ),
        (
            "    public String getBrokerName() {",
            "    /** 返回 Broker 组名称。 */\n    public String getBrokerName() {",
        ),
        (
            "    public void setBrokerName(String brokerName) {",
            "    /** 设置 Broker 组名称。 */\n    public void setBrokerName(String brokerName) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/namesrv/WipeWritePermOfBrokerResponseHeader.java": [
        (
            "public class WipeWritePermOfBrokerResponseHeader implements CommandCustomHeader {",
            "/**\n * 清除 Broker 写权限的响应头：返回受影响的 Topic 数量。\n */\npublic class WipeWritePermOfBrokerResponseHeader implements CommandCustomHeader {",
        ),
        (
            "    @CFNotNull\n    private Integer wipeTopicCount;",
            "    /** 被撤销写权限的 Topic 数量。 */\n    @CFNotNull\n    private Integer wipeTopicCount;",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
            "    /** 校验响应头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
        ),
        (
            "    public Integer getWipeTopicCount() {",
            "    /** 返回受影响的 Topic 数量。 */\n    public Integer getWipeTopicCount() {",
        ),
        (
            "    public void setWipeTopicCount(Integer wipeTopicCount) {",
            "    /** 设置受影响的 Topic 数量。 */\n    public void setWipeTopicCount(Integer wipeTopicCount) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/heartbeat/ConsumeType.java": [
        (
            "public enum ConsumeType {",
            "/**\n * 客户端消费类型枚举：区分主动 Pull、被动 Push 与 Pop 消费模式。\n */\npublic enum ConsumeType {",
        ),
        (
            "    CONSUME_ACTIVELY(\"PULL\"),",
            "    /** 主动消费（Pull）。 */\n    CONSUME_ACTIVELY(\"PULL\"),",
        ),
        (
            "    CONSUME_PASSIVELY(\"PUSH\"),",
            "    /** 被动消费（Push）。 */\n    CONSUME_PASSIVELY(\"PUSH\"),",
        ),
        (
            "    CONSUME_POP(\"POP\");",
            "    /** Pop 消费模式。 */\n    CONSUME_POP(\"POP\");",
        ),
        (
            "    private String typeCN;",
            "    /** 类型中文标识串。 */\n    private String typeCN;",
        ),
        (
            "    ConsumeType(String typeCN) {",
            "    /** 指定类型标识串。 */\n    ConsumeType(String typeCN) {",
        ),
        (
            "    public String getTypeCN() {",
            "    /** 返回类型标识串。 */\n    public String getTypeCN() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/heartbeat/ConsumerData.java": [
        (
            "public class ConsumerData {",
            "/**\n * 心跳中的消费者数据：描述消费组、消费类型、消息模型、订阅集合与单元化标志。\n */\npublic class ConsumerData {",
        ),
        (
            "    private String groupName;",
            "    /** 消费组名称。 */\n    private String groupName;",
        ),
        (
            "    private ConsumeType consumeType;",
            "    /** 消费类型（Push/Pull/Pop）。 */\n    private ConsumeType consumeType;",
        ),
        (
            "    private MessageModel messageModel;",
            "    /** 消息模型（集群/广播）。 */\n    private MessageModel messageModel;",
        ),
        (
            "    private ConsumeFromWhere consumeFromWhere;",
            "    /** 初次启动时的消费位点策略。 */\n    private ConsumeFromWhere consumeFromWhere;",
        ),
        (
            "    private Set<SubscriptionData> subscriptionDataSet = new HashSet<>();",
            "    /** 当前订阅 Topic 集合。 */\n    private Set<SubscriptionData> subscriptionDataSet = new HashSet<>();",
        ),
        (
            "    private boolean unitMode;",
            "    /** 是否启用单元化模式。 */\n    private boolean unitMode;",
        ),
        (
            "    public String getGroupName() {",
            "    /** 返回消费组名称。 */\n    public String getGroupName() {",
        ),
        (
            "    public void setGroupName(String groupName) {",
            "    /** 设置消费组名称。 */\n    public void setGroupName(String groupName) {",
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
            "    /** 返回消费位点策略。 */\n    public ConsumeFromWhere getConsumeFromWhere() {",
        ),
        (
            "    public void setConsumeFromWhere(ConsumeFromWhere consumeFromWhere) {",
            "    /** 设置消费位点策略。 */\n    public void setConsumeFromWhere(ConsumeFromWhere consumeFromWhere) {",
        ),
        (
            "    public Set<SubscriptionData> getSubscriptionDataSet() {",
            "    /** 返回订阅数据集合。 */\n    public Set<SubscriptionData> getSubscriptionDataSet() {",
        ),
        (
            "    public void setSubscriptionDataSet(Set<SubscriptionData> subscriptionDataSet) {",
            "    /** 设置订阅数据集合。 */\n    public void setSubscriptionDataSet(Set<SubscriptionData> subscriptionDataSet) {",
        ),
        (
            "    public boolean isUnitMode() {",
            "    /** 返回是否单元化模式。 */\n    public boolean isUnitMode() {",
        ),
        (
            "    public void setUnitMode(boolean isUnitMode) {",
            "    /** 设置单元化模式标志。 */\n    public void setUnitMode(boolean isUnitMode) {",
        ),
        (
            "    @Override\n    public String toString() {",
            "    /** 返回含消费组、类型与订阅集合的调试字符串。 */\n    @Override\n    public String toString() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/heartbeat/HeartbeatData.java": [
        (
            "public class HeartbeatData extends RemotingSerializable {",
            "/**\n * 客户端心跳数据：汇总生产者/消费者注册信息，支持 fingerprint 检测订阅变更。\n */\npublic class HeartbeatData extends RemotingSerializable {",
        ),
        (
            "    private String clientID;",
            "    /** 客户端唯一标识。 */\n    private String clientID;",
        ),
        (
            "    private Set<ProducerData> producerDataSet = new HashSet<>();",
            "    /** 已注册生产者集合。 */\n    private Set<ProducerData> producerDataSet = new HashSet<>();",
        ),
        (
            "    private Set<ConsumerData> consumerDataSet = new HashSet<>();",
            "    /** 已注册消费者集合。 */\n    private Set<ConsumerData> consumerDataSet = new HashSet<>();",
        ),
        (
            "    private int heartbeatFingerprint = 0;",
            "    /** 心跳指纹（用于检测订阅是否变更）。 */\n    private int heartbeatFingerprint = 0;",
        ),
        (
            "    private boolean isWithoutSub = false;",
            "    /** 是否为无订阅信息的轻量心跳。 */\n    private boolean isWithoutSub = false;",
        ),
        (
            "    public String getClientID() {",
            "    /** 返回客户端 ID。 */\n    public String getClientID() {",
        ),
        (
            "    public void setClientID(String clientID) {",
            "    /** 设置客户端 ID。 */\n    public void setClientID(String clientID) {",
        ),
        (
            "    public Set<ProducerData> getProducerDataSet() {",
            "    /** 返回生产者数据集合。 */\n    public Set<ProducerData> getProducerDataSet() {",
        ),
        (
            "    public void setProducerDataSet(Set<ProducerData> producerDataSet) {",
            "    /** 设置生产者数据集合。 */\n    public void setProducerDataSet(Set<ProducerData> producerDataSet) {",
        ),
        (
            "    public Set<ConsumerData> getConsumerDataSet() {",
            "    /** 返回消费者数据集合。 */\n    public Set<ConsumerData> getConsumerDataSet() {",
        ),
        (
            "    public void setConsumerDataSet(Set<ConsumerData> consumerDataSet) {",
            "    /** 设置消费者数据集合。 */\n    public void setConsumerDataSet(Set<ConsumerData> consumerDataSet) {",
        ),
        (
            "    public int getHeartbeatFingerprint() {",
            "    /** 返回心跳指纹。 */\n    public int getHeartbeatFingerprint() {",
        ),
        (
            "    public void setHeartbeatFingerprint(int heartbeatFingerprint) {",
            "    /** 设置心跳指纹。 */\n    public void setHeartbeatFingerprint(int heartbeatFingerprint) {",
        ),
        (
            "    public boolean isWithoutSub() {",
            "    /** 返回是否为无订阅心跳。 */\n    public boolean isWithoutSub() {",
        ),
        (
            "    public void setWithoutSub(boolean withoutSub) {",
            "    /** 设置无订阅心跳标志。 */\n    public void setWithoutSub(boolean withoutSub) {",
        ),
        (
            "    @Override\n    public String toString() {",
            "    /** 返回含 clientID 与生产/消费集合的调试字符串。 */\n    @Override\n    public String toString() {",
        ),
        (
            "    public int computeHeartbeatFingerprint() {",
            "    /** 计算心跳指纹：忽略 subVersion、clientID 等易变字段后取 JSON hashCode。 */\n    public int computeHeartbeatFingerprint() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/heartbeat/MessageModel.java": [
        (
            "/**\n * Message model\n */\npublic enum MessageModel {",
            "/**\n * 消息模型枚举：广播、集群与 Lite 选择性消费。\n */\npublic enum MessageModel {",
        ),
        (
            "    /**\n     * broadcast\n     */\n    BROADCASTING(\"BROADCASTING\"),",
            "    /** 广播模式：每条消息投递到组内所有消费者。 */\n    BROADCASTING(\"BROADCASTING\"),",
        ),
        (
            "    /**\n     * clustering\n     */\n    CLUSTERING(\"CLUSTERING\"),",
            "    /** 集群模式：同组内消息负载均衡消费。 */\n    CLUSTERING(\"CLUSTERING\"),",
        ),
        (
            "    /**\n     * for lite consumer\n     */\n    LITE_SELECTIVE(\"LITE_SELECTIVE\");",
            "    /** Lite 消费者的选择性消费模式。 */\n    LITE_SELECTIVE(\"LITE_SELECTIVE\");",
        ),
        (
            "    private String modeCN;",
            "    /** 模式中文标识串。 */\n    private String modeCN;",
        ),
        (
            "    MessageModel(String modeCN) {",
            "    /** 指定模式标识串。 */\n    MessageModel(String modeCN) {",
        ),
        (
            "    public String getModeCN() {",
            "    /** 返回模式标识串。 */\n    public String getModeCN() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/heartbeat/ProducerData.java": [
        (
            "public class ProducerData {",
            "/**\n * 心跳中的生产者数据：仅包含生产者组名称。\n */\npublic class ProducerData {",
        ),
        (
            "    private String groupName;",
            "    /** 生产者组名称。 */\n    private String groupName;",
        ),
        (
            "    public String getGroupName() {",
            "    /** 返回生产者组名称。 */\n    public String getGroupName() {",
        ),
        (
            "    public void setGroupName(String groupName) {",
            "    /** 设置生产者组名称。 */\n    public void setGroupName(String groupName) {",
        ),
        (
            "    @Override\n    public String toString() {",
            "    /** 返回含 groupName 的调试字符串。 */\n    @Override\n    public String toString() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/heartbeat/SubscriptionData.java": [
        (
            "public class SubscriptionData implements Comparable<SubscriptionData> {",
            "/**\n * 订阅数据：描述 Topic、过滤表达式、Tag/SQL 集合及订阅版本。\n */\npublic class SubscriptionData implements Comparable<SubscriptionData> {",
        ),
        (
            "    public final static String SUB_ALL = \"*\";",
            "    /** 订阅全部 Tag 的通配符常量。 */\n    public final static String SUB_ALL = \"*\";",
        ),
        (
            "    private boolean classFilterMode = false;",
            "    /** 是否启用类过滤模式。 */\n    private boolean classFilterMode = false;",
        ),
        (
            "    private String topic;",
            "    /** 订阅 Topic 名称。 */\n    private String topic;",
        ),
        (
            "    private String subString;",
            "    /** 订阅表达式串（Tag 或 SQL92）。 */\n    private String subString;",
        ),
        (
            "    private Set<String> tagsSet = new HashSet<>();",
            "    /** 解析后的 Tag 集合。 */\n    private Set<String> tagsSet = new HashSet<>();",
        ),
        (
            "    private Set<Integer> codeSet = new HashSet<>();",
            "    /** Tag 哈希码集合（加速匹配）。 */\n    private Set<Integer> codeSet = new HashSet<>();",
        ),
        (
            "    private long subVersion = System.currentTimeMillis();",
            "    /** 订阅版本号（变更时递增）。 */\n    private long subVersion = System.currentTimeMillis();",
        ),
        (
            "    private String expressionType = ExpressionType.TAG;",
            "    /** 表达式类型（Tag/SQL92 等）。 */\n    private String expressionType = ExpressionType.TAG;",
        ),
        (
            "    @JSONField(serialize = false)\n    private String filterClassSource;",
            "    /** 类过滤源码（不参与 JSON 序列化）。 */\n    @JSONField(serialize = false)\n    private String filterClassSource;",
        ),
        (
            "    public SubscriptionData() {",
            "    /** 默认构造。 */\n    public SubscriptionData() {",
        ),
        (
            "    public SubscriptionData(String topic, String subString) {",
            "    /** 指定 Topic 与订阅串的构造。 */\n    public SubscriptionData(String topic, String subString) {",
        ),
        (
            "    public String getFilterClassSource() {",
            "    /** 返回类过滤源码。 */\n    public String getFilterClassSource() {",
        ),
        (
            "    public void setFilterClassSource(String filterClassSource) {",
            "    /** 设置类过滤源码。 */\n    public void setFilterClassSource(String filterClassSource) {",
        ),
        (
            "    public String getTopic() {",
            "    /** 返回 Topic 名称。 */\n    public String getTopic() {",
        ),
        (
            "    public void setTopic(String topic) {",
            "    /** 设置 Topic 名称。 */\n    public void setTopic(String topic) {",
        ),
        (
            "    public String getSubString() {",
            "    /** 返回订阅表达式串。 */\n    public String getSubString() {",
        ),
        (
            "    public void setSubString(String subString) {",
            "    /** 设置订阅表达式串。 */\n    public void setSubString(String subString) {",
        ),
        (
            "    public Set<String> getTagsSet() {",
            "    /** 返回 Tag 集合。 */\n    public Set<String> getTagsSet() {",
        ),
        (
            "    public void setTagsSet(Set<String> tagsSet) {",
            "    /** 设置 Tag 集合。 */\n    public void setTagsSet(Set<String> tagsSet) {",
        ),
        (
            "    public long getSubVersion() {",
            "    /** 返回订阅版本号。 */\n    public long getSubVersion() {",
        ),
        (
            "    public void setSubVersion(long subVersion) {",
            "    /** 设置订阅版本号。 */\n    public void setSubVersion(long subVersion) {",
        ),
        (
            "    public Set<Integer> getCodeSet() {",
            "    /** 返回 Tag 哈希码集合。 */\n    public Set<Integer> getCodeSet() {",
        ),
        (
            "    public void setCodeSet(Set<Integer> codeSet) {",
            "    /** 设置 Tag 哈希码集合。 */\n    public void setCodeSet(Set<Integer> codeSet) {",
        ),
        (
            "    public boolean isClassFilterMode() {",
            "    /** 返回是否类过滤模式。 */\n    public boolean isClassFilterMode() {",
        ),
        (
            "    public void setClassFilterMode(boolean classFilterMode) {",
            "    /** 设置类过滤模式标志。 */\n    public void setClassFilterMode(boolean classFilterMode) {",
        ),
        (
            "    public String getExpressionType() {",
            "    /** 返回表达式类型。 */\n    public String getExpressionType() {",
        ),
        (
            "    public void setExpressionType(String expressionType) {",
            "    /** 设置表达式类型。 */\n    public void setExpressionType(String expressionType) {",
        ),
        (
            "    @Override\n    public int hashCode() {",
            "    /** 基于 Topic、表达式与 Tag 集合计算哈希码。 */\n    @Override\n    public int hashCode() {",
        ),
        (
            "    @Override\n    public boolean equals(Object obj) {",
            "    /** 比较订阅 Topic、表达式、版本与 Tag 集合是否相等。 */\n    @Override\n    public boolean equals(Object obj) {",
        ),
        (
            "    @Override\n    public String toString() {",
            "    /** 返回含 Topic、表达式与 Tag 集合的调试字符串。 */\n    @Override\n    public String toString() {",
        ),
        (
            "    @Override\n    public int compareTo(SubscriptionData other) {",
            "    /** 按 topic@subString 字典序比较。 */\n    @Override\n    public int compareTo(SubscriptionData other) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/namesrv/RegisterBrokerResult.java": [
        (
            "public class RegisterBrokerResult {",
            "/**\n * Broker 注册 NameServer 的结果体：HA 地址、Master 地址与 KV 配置表。\n */\npublic class RegisterBrokerResult {",
        ),
        (
            "    private String haServerAddr;",
            "    /** HA 高可用服务地址。 */\n    private String haServerAddr;",
        ),
        (
            "    private String masterAddr;",
            "    /** Master Broker 地址。 */\n    private String masterAddr;",
        ),
        (
            "    private KVTable kvTable;",
            "    /** NameServer 返回的 KV 配置表。 */\n    private KVTable kvTable;",
        ),
        (
            "    public String getHaServerAddr() {",
            "    /** 返回 HA 服务地址。 */\n    public String getHaServerAddr() {",
        ),
        (
            "    public void setHaServerAddr(String haServerAddr) {",
            "    /** 设置 HA 服务地址。 */\n    public void setHaServerAddr(String haServerAddr) {",
        ),
        (
            "    public String getMasterAddr() {",
            "    /** 返回 Master 地址。 */\n    public String getMasterAddr() {",
        ),
        (
            "    public void setMasterAddr(String masterAddr) {",
            "    /** 设置 Master 地址。 */\n    public void setMasterAddr(String masterAddr) {",
        ),
        (
            "    public KVTable getKvTable() {",
            "    /** 返回 KV 配置表。 */\n    public KVTable getKvTable() {",
        ),
        (
            "    public void setKvTable(KVTable kvTable) {",
            "    /** 设置 KV 配置表。 */\n    public void setKvTable(KVTable kvTable) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/route/BrokerData.java": [
        (
            "/**\n * The class describes that a typical broker cluster's (in replication) details: the cluster (in sharding) name\n * that it belongs to, and all the single instance information for this cluster.\n */\npublic class BrokerData implements Comparable<BrokerData> {",
            "/**\n * Broker 路由数据：描述 Broker 组所属集群、各 brokerId 实例地址及可用区信息。\n */\npublic class BrokerData implements Comparable<BrokerData> {",
        ),
        (
            "    private String cluster;",
            "    /** 所属集群（分片）名称。 */\n    private String cluster;",
        ),
        (
            "    private String brokerName;",
            "    /** Broker 组名称。 */\n    private String brokerName;",
        ),
        (
            "    /**\n     * The container that store the all single instances for the current broker replication cluster.\n     * The key is the brokerId, and the value is the address of the single broker instance.\n     */\n    private HashMap<Long, String> brokerAddrs;",
            "    /** brokerId → 实例地址映射（含 Master 与 Slave）。 */\n    private HashMap<Long, String> brokerAddrs;",
        ),
        (
            "    private String zoneName;",
            "    /** 可用区名称。 */\n    private String zoneName;",
        ),
        (
            "    private final Random random = new Random();",
            "    /** 随机数生成器（Master 不可用时选 Slave）。 */\n    private final Random random = new Random();",
        ),
        (
            "    /**\n     * Enable acting master or not, used for old version HA adaption,\n     */\n    private boolean enableActingMaster = false;",
            "    /** 是否允许 Acting Master（兼容旧版 HA）。 */\n    private boolean enableActingMaster = false;",
        ),
        (
            "    public BrokerData() {",
            "    /** 默认构造。 */\n    public BrokerData() {",
        ),
        (
            "    public BrokerData(BrokerData brokerData) {",
            "    /** 拷贝构造。 */\n    public BrokerData(BrokerData brokerData) {",
        ),
        (
            "    public BrokerData(String cluster, String brokerName, HashMap<Long, String> brokerAddrs) {",
            "    /** 指定集群、Broker 组与地址映射的构造。 */\n    public BrokerData(String cluster, String brokerName, HashMap<Long, String> brokerAddrs) {",
        ),
        (
            "    public BrokerData(String cluster, String brokerName, HashMap<Long, String> brokerAddrs,\n        boolean enableActingMaster) {",
            "    /** 指定集群、Broker 组、地址映射与 Acting Master 标志的构造。 */\n    public BrokerData(String cluster, String brokerName, HashMap<Long, String> brokerAddrs,\n        boolean enableActingMaster) {",
        ),
        (
            "    public BrokerData(String cluster, String brokerName, HashMap<Long, String> brokerAddrs, boolean enableActingMaster,\n        String zoneName) {",
            "    /** 指定集群、Broker 组、地址映射、Acting Master 与可用区的构造。 */\n    public BrokerData(String cluster, String brokerName, HashMap<Long, String> brokerAddrs, boolean enableActingMaster,\n        String zoneName) {",
        ),
        (
            "    /**\n     * Selects a (preferably master) broker address from the registered list. If the master's address cannot be found, a\n     * slave broker address is selected in a random manner.\n     *\n     * @return Broker address.\n     */\n    public String selectBrokerAddr() {",
            "    /**\n     * 从已注册地址中选取 Broker 地址：优先 Master，不可用时随机选 Slave。\n     *\n     * @return Broker 地址\n     */\n    public String selectBrokerAddr() {",
        ),
        (
            "    public HashMap<Long, String> getBrokerAddrs() {",
            "    /** 返回 brokerId 地址映射。 */\n    public HashMap<Long, String> getBrokerAddrs() {",
        ),
        (
            "    public void setBrokerAddrs(HashMap<Long, String> brokerAddrs) {",
            "    /** 设置 brokerId 地址映射。 */\n    public void setBrokerAddrs(HashMap<Long, String> brokerAddrs) {",
        ),
        (
            "    public String getCluster() {",
            "    /** 返回集群名称。 */\n    public String getCluster() {",
        ),
        (
            "    public void setCluster(String cluster) {",
            "    /** 设置集群名称。 */\n    public void setCluster(String cluster) {",
        ),
        (
            "    public boolean isEnableActingMaster() {",
            "    /** 返回是否启用 Acting Master。 */\n    public boolean isEnableActingMaster() {",
        ),
        (
            "    public void setEnableActingMaster(boolean enableActingMaster) {",
            "    /** 设置 Acting Master 标志。 */\n    public void setEnableActingMaster(boolean enableActingMaster) {",
        ),
        (
            "    public String getZoneName() {",
            "    /** 返回可用区名称。 */\n    public String getZoneName() {",
        ),
        (
            "    public void setZoneName(String zoneName) {",
            "    /** 设置可用区名称。 */\n    public void setZoneName(String zoneName) {",
        ),
        (
            "    @Override\n    public int hashCode() {",
            "    /** 基于 brokerName 与地址映射计算哈希码。 */\n    @Override\n    public int hashCode() {",
        ),
        (
            "    @Override\n    public boolean equals(Object obj) {",
            "    /** 比较 brokerName 与地址映射是否相等。 */\n    @Override\n    public boolean equals(Object obj) {",
        ),
        (
            "    @Override\n    public String toString() {",
            "    /** 返回含 brokerName 与地址映射的调试字符串。 */\n    @Override\n    public String toString() {",
        ),
        (
            "    @Override\n    public int compareTo(BrokerData o) {",
            "    /** 按 brokerName 字典序比较。 */\n    @Override\n    public int compareTo(BrokerData o) {",
        ),
        (
            "    public String getBrokerName() {",
            "    /** 返回 Broker 组名称。 */\n    public String getBrokerName() {",
        ),
        (
            "    public void setBrokerName(String brokerName) {",
            "    /** 设置 Broker 组名称。 */\n    public void setBrokerName(String brokerName) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/route/MessageQueueRouteState.java": [
        (
            "public enum MessageQueueRouteState {",
            "/**\n * 消息队列路由状态枚举：ordinal 顺序固定，勿随意调整。\n */\npublic enum MessageQueueRouteState {",
        ),
        (
            "    // do not change below order, since ordinal() is used\n    Expired,",
            "    /** 路由已过期。 */\n    // do not change below order, since ordinal() is used\n    Expired,",
        ),
        (
            "    ReadOnly,",
            "    /** 只读状态。 */\n    ReadOnly,",
        ),
        (
            "    Normal,",
            "    /** 正常可读可写。 */\n    Normal,",
        ),
        (
            "    WriteOnly,",
            "    /** 只写状态。 */\n    WriteOnly,",
        ),
    ],
}
