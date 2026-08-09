"""Chinese JavaDoc replacements for RocketMQ wave30b protocol body/filter/header [15:30]."""

R: dict[str, list[tuple[str, str]]] = {
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/body/SetMessageRequestModeRequestBody.java": [
        (
            "public class SetMessageRequestModeRequestBody extends RemotingSerializable {",
            "/**\n * 设置 Topic 消息请求模式（Pull/Pop）的请求体：指定消费组、模式及 Pop 队列共享数。\n */\npublic class SetMessageRequestModeRequestBody extends RemotingSerializable {",
        ),
        (
            "    private String topic;",
            "    /** 目标 Topic 名称。 */\n    private String topic;",
        ),
        (
            "    private String consumerGroup;",
            "    /** 消费组名称。 */\n    private String consumerGroup;",
        ),
        (
            "    private MessageRequestMode mode = MessageRequestMode.PULL;",
            "    /** 消息请求模式，默认 Pull。 */\n    private MessageRequestMode mode = MessageRequestMode.PULL;",
        ),
        (
            "    /*\n    consumer working in pop mode could share the MessageQueues assigned to the N (N = popShareQueueNum) consumers following it in the cid list\n     */",
            "    /** Pop 模式下，当前消费者可与 cid 列表中后续 N（N=popShareQueueNum）个消费者共享已分配的 MessageQueue。 */",
        ),
        (
            "    private int popShareQueueNum = 0;",
            "    /** Pop 队列共享数量，0 表示不共享。 */\n    private int popShareQueueNum = 0;",
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
            "    public MessageRequestMode getMode() {",
            "    /** 返回消息请求模式。 */\n    public MessageRequestMode getMode() {",
        ),
        (
            "    public int getPopShareQueueNum() {",
            "    /** 返回 Pop 队列共享数。 */\n    public int getPopShareQueueNum() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/body/SubscriptionGroupList.java": [
        (
            "public class SubscriptionGroupList extends RemotingSerializable {",
            "/**\n * 订阅组配置列表 Remoting 体：批量传输 {@link SubscriptionGroupConfig} 条目。\n */\npublic class SubscriptionGroupList extends RemotingSerializable {",
        ),
        (
            "    @CFNotNull\n    private List<SubscriptionGroupConfig> groupConfigList;",
            "    /** 订阅组配置列表，不可为空。 */\n    @CFNotNull\n    private List<SubscriptionGroupConfig> groupConfigList;",
        ),
        (
            "    public SubscriptionGroupList(List<SubscriptionGroupConfig> groupConfigList) {",
            "    /** 以给定配置列表构造。 */\n    public SubscriptionGroupList(List<SubscriptionGroupConfig> groupConfigList) {",
        ),
        (
            "    public List<SubscriptionGroupConfig> getGroupConfigList() {",
            "    /** 返回订阅组配置列表。 */\n    public List<SubscriptionGroupConfig> getGroupConfigList() {",
        ),
        (
            "    public void setGroupConfigList(List<SubscriptionGroupConfig> groupConfigList) {",
            "    /** 设置订阅组配置列表。 */\n    public void setGroupConfigList(List<SubscriptionGroupConfig> groupConfigList) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/body/SubscriptionGroupWrapper.java": [
        (
            "public class SubscriptionGroupWrapper extends RemotingSerializable {",
            "/**\n * 订阅组元数据序列化包装：含订阅组表、禁消费表及数据版本号。\n */\npublic class SubscriptionGroupWrapper extends RemotingSerializable {",
        ),
        (
            "    private ConcurrentMap<String, SubscriptionGroupConfig> subscriptionGroupTable =",
            "    /** groupName → 订阅组配置。 */\n    private ConcurrentMap<String, SubscriptionGroupConfig> subscriptionGroupTable =",
        ),
        (
            "    private ConcurrentMap<String, ConcurrentMap<String, Integer>> forbiddenTable =",
            "    /** groupName →（Topic → 禁消费标志）映射。 */\n    private ConcurrentMap<String, ConcurrentMap<String, Integer>> forbiddenTable =",
        ),
        (
            "    private DataVersion dataVersion = new DataVersion();",
            "    /** 订阅组数据版本，用于增量同步。 */\n    private DataVersion dataVersion = new DataVersion();",
        ),
        (
            "    public ConcurrentMap<String, SubscriptionGroupConfig> getSubscriptionGroupTable() {",
            "    /** 返回订阅组配置表。 */\n    public ConcurrentMap<String, SubscriptionGroupConfig> getSubscriptionGroupTable() {",
        ),
        (
            "    public ConcurrentMap<String, ConcurrentMap<String, Integer>> getForbiddenTable() {",
            "    /** 返回禁消费表。 */\n    public ConcurrentMap<String, ConcurrentMap<String, Integer>> getForbiddenTable() {",
        ),
        (
            "    public DataVersion getDataVersion() {",
            "    /** 返回数据版本。 */\n    public DataVersion getDataVersion() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/body/SyncStateSet.java": [
        (
            "public class SyncStateSet extends RemotingSerializable {",
            "/**\n * 同步状态集（SyncStateSet）：Controller 模式下参与同步复制的 BrokerId 集合及纪元。\n */\npublic class SyncStateSet extends RemotingSerializable {",
        ),
        (
            "    private Set<Long> syncStateSet;",
            "    /** 同步副本 BrokerId 集合。 */\n    private Set<Long> syncStateSet;",
        ),
        (
            "    private int syncStateSetEpoch;",
            "    /** 同步状态集纪元，变更时递增。 */\n    private int syncStateSetEpoch;",
        ),
        (
            "    public SyncStateSet(Set<Long> syncStateSet, int syncStateSetEpoch) {",
            "    /** 以副本集合与纪元构造。 */\n    public SyncStateSet(Set<Long> syncStateSet, int syncStateSetEpoch) {",
        ),
        (
            "    public Set<Long> getSyncStateSet() {",
            "    /** 返回同步副本集合的防御性拷贝。 */\n    public Set<Long> getSyncStateSet() {",
        ),
        (
            "    public int getSyncStateSetEpoch() {",
            "    /** 返回同步状态集纪元。 */\n    public int getSyncStateSetEpoch() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/body/TopicConfigAndMappingSerializeWrapper.java": [
        (
            "public class TopicConfigAndMappingSerializeWrapper extends TopicConfigSerializeWrapper {",
            "/**\n * Topic 配置与静态 Topic 队列映射的联合序列化包装，继承 {@link TopicConfigSerializeWrapper}。\n */\npublic class TopicConfigAndMappingSerializeWrapper extends TopicConfigSerializeWrapper {",
        ),
        (
            "    private Map<String/* topic */, TopicQueueMappingInfo> topicQueueMappingInfoMap = new ConcurrentHashMap<>();",
            "    /** Topic → 队列映射概要信息。 */\n    private Map<String/* topic */, TopicQueueMappingInfo> topicQueueMappingInfoMap = new ConcurrentHashMap<>();",
        ),
        (
            "    private Map<String/* topic */, TopicQueueMappingDetail> topicQueueMappingDetailMap = new ConcurrentHashMap<>();",
            "    /** Topic → 队列映射详情。 */\n    private Map<String/* topic */, TopicQueueMappingDetail> topicQueueMappingDetailMap = new ConcurrentHashMap<>();",
        ),
        (
            "    private DataVersion mappingDataVersion = new DataVersion();",
            "    /** 映射数据版本号。 */\n    private DataVersion mappingDataVersion = new DataVersion();",
        ),
        (
            "    public Map<String, TopicQueueMappingInfo> getTopicQueueMappingInfoMap() {",
            "    /** 返回 Topic 队列映射概要表。 */\n    public Map<String, TopicQueueMappingInfo> getTopicQueueMappingInfoMap() {",
        ),
        (
            "    public Map<String, TopicQueueMappingDetail> getTopicQueueMappingDetailMap() {",
            "    /** 返回 Topic 队列映射详情表。 */\n    public Map<String, TopicQueueMappingDetail> getTopicQueueMappingDetailMap() {",
        ),
        (
            "    public static TopicConfigAndMappingSerializeWrapper from(TopicConfigSerializeWrapper wrapper) {",
            "    /** 从 Topic 配置包装转换为带映射信息的包装。 */\n    public static TopicConfigAndMappingSerializeWrapper from(TopicConfigSerializeWrapper wrapper) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/body/TopicConfigSerializeWrapper.java": [
        (
            "public class TopicConfigSerializeWrapper extends RemotingSerializable {",
            "/**\n * Topic 配置序列化包装：Broker 端 Topic 配置表及数据版本，用于注册与同步。\n */\npublic class TopicConfigSerializeWrapper extends RemotingSerializable {",
        ),
        (
            "    private ConcurrentMap<String, TopicConfig> topicConfigTable =",
            "    /** Topic 名 → {@link TopicConfig} 配置。 */\n    private ConcurrentMap<String, TopicConfig> topicConfigTable =",
        ),
        (
            "    private DataVersion dataVersion = new DataVersion();",
            "    /** Topic 配置数据版本。 */\n    private DataVersion dataVersion = new DataVersion();",
        ),
        (
            "    public ConcurrentMap<String, TopicConfig> getTopicConfigTable() {",
            "    /** 返回 Topic 配置表。 */\n    public ConcurrentMap<String, TopicConfig> getTopicConfigTable() {",
        ),
        (
            "    public DataVersion getDataVersion() {",
            "    /** 返回数据版本。 */\n    public DataVersion getDataVersion() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/body/TopicList.java": [
        (
            "public class TopicList extends RemotingSerializable {",
            "/**\n * Broker 上 Topic 名称列表及所属 Broker 地址，用于路由查询响应。\n */\npublic class TopicList extends RemotingSerializable {",
        ),
        (
            "    private Set<String> topicList = ConcurrentHashMap.newKeySet();",
            "    /** Topic 名称集合。 */\n    private Set<String> topicList = ConcurrentHashMap.newKeySet();",
        ),
        (
            "    private String brokerAddr;",
            "    /** 承载上述 Topic 的 Broker 地址。 */\n    private String brokerAddr;",
        ),
        (
            "    public Set<String> getTopicList() {",
            "    /** 返回 Topic 名称集合。 */\n    public Set<String> getTopicList() {",
        ),
        (
            "    public String getBrokerAddr() {",
            "    /** 返回 Broker 地址。 */\n    public String getBrokerAddr() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/body/TopicQueueMappingSerializeWrapper.java": [
        (
            "public class TopicQueueMappingSerializeWrapper extends RemotingSerializable {",
            "/**\n * 静态 Topic 队列映射序列化包装：Topic → {@link TopicQueueMappingDetail} 及数据版本。\n */\npublic class TopicQueueMappingSerializeWrapper extends RemotingSerializable {",
        ),
        (
            "    private Map<String/* topic */, TopicQueueMappingDetail> topicQueueMappingInfoMap;",
            "    /** Topic → 队列映射详情。 */\n    private Map<String/* topic */, TopicQueueMappingDetail> topicQueueMappingInfoMap;",
        ),
        (
            "    private DataVersion dataVersion = new DataVersion();",
            "    /** 映射数据版本。 */\n    private DataVersion dataVersion = new DataVersion();",
        ),
        (
            "    public Map<String, TopicQueueMappingDetail> getTopicQueueMappingInfoMap() {",
            "    /** 返回 Topic 队列映射表。 */\n    public Map<String, TopicQueueMappingDetail> getTopicQueueMappingInfoMap() {",
        ),
        (
            "    public DataVersion getDataVersion() {",
            "    /** 返回数据版本。 */\n    public DataVersion getDataVersion() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/body/UnlockBatchRequestBody.java": [
        (
            "public class UnlockBatchRequestBody extends RemotingSerializable {",
            "/**\n * 批量解锁 MessageQueue 请求体：Pop 消费模式下释放已锁定的队列。\n */\npublic class UnlockBatchRequestBody extends RemotingSerializable {",
        ),
        (
            "    private String consumerGroup;",
            "    /** 消费组名称。 */\n    private String consumerGroup;",
        ),
        (
            "    private String clientId;",
            "    /** 发起解锁的客户端 ID。 */\n    private String clientId;",
        ),
        (
            "    private boolean onlyThisBroker = false;",
            "    /** 是否仅在本 Broker 解锁，默认 false。 */\n    private boolean onlyThisBroker = false;",
        ),
        (
            "    private Set<MessageQueue> mqSet = new HashSet<>();",
            "    /** 待解锁的 MessageQueue 集合。 */\n    private Set<MessageQueue> mqSet = new HashSet<>();",
        ),
        (
            "    public String getConsumerGroup() {",
            "    /** 返回消费组。 */\n    public String getConsumerGroup() {",
        ),
        (
            "    public Set<MessageQueue> getMqSet() {",
            "    /** 返回待解锁队列集合。 */\n    public Set<MessageQueue> getMqSet() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/body/UserInfo.java": [
        (
            "public class UserInfo {",
            "/**\n * ACL 用户信息：用户名、密码、类型及状态，用于权限认证与账户管理。\n */\npublic class UserInfo {",
        ),
        (
            "    private String username;",
            "    /** 用户名。 */\n    private String username;",
        ),
        (
            "    private String password;",
            "    /** 密码（传输或存储时通常已加密）。 */\n    private String password;",
        ),
        (
            "    private String userType;",
            "    /** 用户类型（如 SUPER、NORMAL）。 */\n    private String userType;",
        ),
        (
            "    private String userStatus;",
            "    /** 账户状态（启用/禁用等）。 */\n    private String userStatus;",
        ),
        (
            "    public static UserInfo of(String username, String password, String userType) {",
            "    /** 三字段工厂方法。 */\n    public static UserInfo of(String username, String password, String userType) {",
        ),
        (
            "    public static UserInfo of(String username, String password, String userType, String userStatus) {",
            "    /** 四字段工厂方法，含账户状态。 */\n    public static UserInfo of(String username, String password, String userType, String userStatus) {",
        ),
        (
            "    public String getUsername() {",
            "    /** 返回用户名。 */\n    public String getUsername() {",
        ),
        (
            "    public String getUserType() {",
            "    /** 返回用户类型。 */\n    public String getUserType() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/filter/FilterAPI.java": [
        (
            "public class FilterAPI {",
            "/**\n * 订阅过滤表达式构建工具：将 Topic 与订阅串解析为 {@link SubscriptionData}。\n * 支持 Tag（|| 分隔）与 SQL92 等表达式类型。\n */\npublic class FilterAPI {",
        ),
        (
            "    public static SubscriptionData buildSubscriptionData(String topic, String subString) throws Exception {",
            "    /** 按 Topic 与 Tag 订阅串构建订阅数据（默认 Tag 类型）。 */\n    public static SubscriptionData buildSubscriptionData(String topic, String subString) throws Exception {",
        ),
        (
            "        if (StringUtils.isEmpty(subString) || subString.equals(SubscriptionData.SUB_ALL)) {",
            "        // 空串或 SUB_ALL 表示订阅全部 Tag\n        if (StringUtils.isEmpty(subString) || subString.equals(SubscriptionData.SUB_ALL)) {",
        ),
        (
            "        String[] tags = subString.split(\"\\\\|\\\\|\");",
            "        // 以 || 分割多 Tag\n        String[] tags = subString.split(\"\\\\|\\\\|\");",
        ),
        (
            "            throw new Exception(\"subString split error\");",
            "            throw new Exception(\"订阅串分割失败\");",
        ),
        (
            "    public static SubscriptionData buildSubscriptionData(String topic, String subString, String expressionType) throws Exception {",
            "    /** 构建订阅数据并指定表达式类型（Tag/SQL92 等）。 */\n    public static SubscriptionData buildSubscriptionData(String topic, String subString, String expressionType) throws Exception {",
        ),
        (
            "    public static SubscriptionData build(final String topic, final String subString,\n        final String type) throws Exception {",
            "    /** 统一入口：按 type 选择 Tag 或通用表达式构建逻辑。 */\n    public static SubscriptionData build(final String topic, final String subString,\n        final String type) throws Exception {",
        ),
        (
            "            throw new IllegalArgumentException(\"Expression can't be null! \" + type);",
            "            throw new IllegalArgumentException(\"非 Tag 类型时表达式不可为空: \" + type);",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/AckMessageRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.ACK_MESSAGE, action = Action.SUB)\npublic class AckMessageRequestHeader extends TopicQueueRequestHeader {",
            "/**\n * Pop 消息确认（ACK）请求头：标识消费组、Topic、队列、位点及 Pop 附加信息。\n */\n@RocketMQAction(value = RequestCode.ACK_MESSAGE, action = Action.SUB)\npublic class AckMessageRequestHeader extends TopicQueueRequestHeader {",
        ),
        (
            "    @CFNotNull\n    @RocketMQResource(ResourceType.GROUP)\n    private String consumerGroup;",
            "    /** 消费组名称。 */\n    @CFNotNull\n    @RocketMQResource(ResourceType.GROUP)\n    private String consumerGroup;",
        ),
        (
            "    @CFNotNull\n    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
            "    /** 目标 Topic。 */\n    @CFNotNull\n    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
        ),
        (
            "    @CFNotNull\n    private Integer queueId;",
            "    /** 队列 ID。 */\n    @CFNotNull\n    private Integer queueId;",
        ),
        (
            "    @CFNotNull\n    private String extraInfo;",
            "    /** Pop 附加信息（含 startOffset、popTime 等）。 */\n    @CFNotNull\n    private String extraInfo;",
        ),
        (
            "    @CFNotNull\n    private Long offset;",
            "    /** 消息消费位点。 */\n    @CFNotNull\n    private Long offset;",
        ),
        (
            "    private String liteTopic;",
            "    /** Lite Topic 名（Lite 消费场景可选）。 */\n    private String liteTopic;",
        ),
        (
            "    public Long getOffset() {",
            "    /** 返回消费位点。 */\n    public Long getOffset() {",
        ),
        (
            "    public String getExtraInfo() {",
            "    /** 返回 Pop 附加信息。 */\n    public String getExtraInfo() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/AddBrokerRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.ADD_BROKER, resource = ResourceType.CLUSTER, action = Action.UPDATE)\npublic class AddBrokerRequestHeader implements CommandCustomHeader {",
            "/**\n * 向集群添加 Broker 的请求头：可选指定 Broker 配置文件路径。\n */\n@RocketMQAction(value = RequestCode.ADD_BROKER, resource = ResourceType.CLUSTER, action = Action.UPDATE)\npublic class AddBrokerRequestHeader implements CommandCustomHeader {",
        ),
        (
            "    @CFNullable\n    private String configPath;",
            "    /** Broker 配置文件路径，可为空。 */\n    @CFNullable\n    private String configPath;",
        ),
        (
            "    public String getConfigPath() {",
            "    /** 返回配置文件路径。 */\n    public String getConfigPath() {",
        ),
        (
            "    public void setConfigPath(String configPath) {",
            "    /** 设置配置文件路径。 */\n    public void setConfigPath(String configPath) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/ChangeInvisibleTimeRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.CHANGE_MESSAGE_INVISIBLETIME, action = Action.SUB)\npublic class ChangeInvisibleTimeRequestHeader extends TopicQueueRequestHeader {",
            "/**\n * 修改 Pop 消息不可见时间请求头：延长或缩短消息重投前的等待窗口。\n */\n@RocketMQAction(value = RequestCode.CHANGE_MESSAGE_INVISIBLETIME, action = Action.SUB)\npublic class ChangeInvisibleTimeRequestHeader extends TopicQueueRequestHeader {",
        ),
        (
            "    @CFNotNull\n    @RocketMQResource(ResourceType.GROUP)\n    private String consumerGroup;",
            "    /** 消费组名称。 */\n    @CFNotNull\n    @RocketMQResource(ResourceType.GROUP)\n    private String consumerGroup;",
        ),
        (
            "    @CFNotNull\n    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
            "    /** 目标 Topic。 */\n    @CFNotNull\n    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
        ),
        (
            "    /**\n     * startOffset popTime invisibleTime queueId\n     */",
            "    /** Pop 附加信息：startOffset、popTime、invisibleTime、queueId。 */",
        ),
        (
            "    @CFNotNull\n    private Long invisibleTime;",
            "    /** 新的不可见时长（毫秒）。 */\n    @CFNotNull\n    private Long invisibleTime;",
        ),
        (
            "    private String liteTopic;",
            "    /** Lite Topic 名（可选）。 */\n    private String liteTopic;",
        ),
        (
            "    private boolean suspend = false;",
            "    /** 是否挂起等待（长轮询场景）。 */\n    private boolean suspend = false;",
        ),
        (
            "    public Long getInvisibleTime() {",
            "    /** 返回不可见时长。 */\n    public Long getInvisibleTime() {",
        ),
        (
            "    /**\n     * startOffset popTime invisibleTime queueId\n     */\n    public String getExtraInfo() {",
            "    /** 返回 Pop 附加信息。 */\n    public String getExtraInfo() {",
        ),
        (
            "    public boolean isSuspend() {",
            "    /** 返回是否挂起等待。 */\n    public boolean isSuspend() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/ChangeInvisibleTimeResponseHeader.java": [
        (
            "public class ChangeInvisibleTimeResponseHeader implements CommandCustomHeader {",
            "/**\n * 修改不可见时间响应头：返回 Pop 时间、新不可见时长及 Revive 队列 ID。\n */\npublic class ChangeInvisibleTimeResponseHeader implements CommandCustomHeader {",
        ),
        (
            "    @CFNotNull\n    private long popTime;",
            "    /** 消息 Pop 时间戳。 */\n    @CFNotNull\n    private long popTime;",
        ),
        (
            "    @CFNotNull\n    private long invisibleTime;",
            "    /** 生效后的不可见时长（毫秒）。 */\n    @CFNotNull\n    private long invisibleTime;",
        ),
        (
            "    @CFNotNull\n    private int reviveQid;",
            "    /** Revive 主题队列 ID，用于超时重投。 */\n    @CFNotNull\n    private int reviveQid;",
        ),
        (
            "    public long getPopTime() {",
            "    /** 返回 Pop 时间戳。 */\n    public long getPopTime() {",
        ),
        (
            "    public long getInvisibleTime() {",
            "    /** 返回不可见时长。 */\n    public long getInvisibleTime() {",
        ),
        (
            "    public int getReviveQid() {",
            "    /** 返回 Revive 队列 ID。 */\n    public int getReviveQid() {",
        ),
    ],
}
