"""Chinese JavaDoc replacements for RocketMQ wave31a remoting protocol header [0:15]."""

R: dict[str, list[tuple[str, str]]] = {
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/CheckRocksdbCqWriteProgressRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.CHECK_ROCKSDB_CQ_WRITE_PROGRESS, action = Action.GET)\npublic class CheckRocksdbCqWriteProgressRequestHeader implements CommandCustomHeader {",
            "/**\n * 检查 RocksDB 消费队列（CQ）写入进度请求头：指定 Topic 与校验时间点。\n */\n@RocketMQAction(value = RequestCode.CHECK_ROCKSDB_CQ_WRITE_PROGRESS, action = Action.GET)\npublic class CheckRocksdbCqWriteProgressRequestHeader implements CommandCustomHeader {",
        ),
        (
            "    @CFNotNull\n    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
            "    /** 待检查的 Topic 名称。 */\n    @CFNotNull\n    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
        ),
        (
            "    private long checkStoreTime;",
            "    /** 校验用的存储时间戳（毫秒）。 */\n    private long checkStoreTime;",
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
            "    public long getCheckStoreTime() {",
            "    /** 返回校验存储时间戳。 */\n    public long getCheckStoreTime() {",
        ),
        (
            "    public void setCheckStoreTime(long checkStoreTime) {",
            "    /** 设置校验存储时间戳。 */\n    public void setCheckStoreTime(long checkStoreTime) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/CheckTransactionStateRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.CHECK_TRANSACTION_STATE, action = Action.PUB)\npublic class CheckTransactionStateRequestHeader extends RpcRequestHeader {",
            "/**\n * 事务状态回查请求头：Broker 向 Producer 发起半消息提交/回滚状态确认。\n */\n@RocketMQAction(value = RequestCode.CHECK_TRANSACTION_STATE, action = Action.PUB)\npublic class CheckTransactionStateRequestHeader extends RpcRequestHeader {",
        ),
        (
            "    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
            "    /** 半消息所属 Topic。 */\n    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
        ),
        (
            "    @CFNotNull\n    private Long tranStateTableOffset;",
            "    /** 事务状态表中的逻辑位点。 */\n    @CFNotNull\n    private Long tranStateTableOffset;",
        ),
        (
            "    @CFNotNull\n    private Long commitLogOffset;",
            "    /** CommitLog 中的物理位点。 */\n    @CFNotNull\n    private Long commitLogOffset;",
        ),
        (
            "    private String msgId;",
            "    /** 消息唯一标识。 */\n    private String msgId;",
        ),
        (
            "    private String transactionId;",
            "    /** 事务 ID（Producer 端生成）。 */\n    private String transactionId;",
        ),
        (
            "    private String offsetMsgId;",
            "    /** 基于位点生成的 MsgId。 */\n    private String offsetMsgId;",
        ),
        (
            "    public Long getTranStateTableOffset() {",
            "    /** 返回事务状态表位点。 */\n    public Long getTranStateTableOffset() {",
        ),
        (
            "    public Long getCommitLogOffset() {",
            "    /** 返回 CommitLog 位点。 */\n    public Long getCommitLogOffset() {",
        ),
        (
            "    public String getMsgId() {",
            "    /** 返回消息 ID。 */\n    public String getMsgId() {",
        ),
        (
            "    public String getTransactionId() {",
            "    /** 返回事务 ID。 */\n    public String getTransactionId() {",
        ),
        (
            "    public String getOffsetMsgId() {",
            "    /** 返回位点 MsgId。 */\n    public String getOffsetMsgId() {",
        ),
        (
            "    @Override\n    public String toString() {",
            "    /** 返回便于诊断的可读字符串。 */\n    @Override\n    public String toString() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/CheckTransactionStateResponseHeader.java": [
        (
            "public class CheckTransactionStateResponseHeader implements CommandCustomHeader {",
            "/**\n * 事务状态回查响应头：Producer 告知 Broker 半消息提交或回滚决策。\n */\npublic class CheckTransactionStateResponseHeader implements CommandCustomHeader {",
        ),
        (
            "    @CFNotNull\n    private String producerGroup;",
            "    /** 生产者组名称。 */\n    @CFNotNull\n    private String producerGroup;",
        ),
        (
            "    @CFNotNull\n    private Long tranStateTableOffset;",
            "    /** 事务状态表位点。 */\n    @CFNotNull\n    private Long tranStateTableOffset;",
        ),
        (
            "    @CFNotNull\n    private Long commitLogOffset;",
            "    /** CommitLog 物理位点。 */\n    @CFNotNull\n    private Long commitLogOffset;",
        ),
        (
            "    @CFNotNull\n    private Integer commitOrRollback; // TRANSACTION_COMMIT_TYPE",
            "    /** 提交或回滚标志（TRANSACTION_COMMIT_TYPE / TRANSACTION_ROLLBACK_TYPE）。 */\n    @CFNotNull\n    private Integer commitOrRollback; // TRANSACTION_COMMIT_TYPE",
        ),
        (
            "    public void checkFields() throws RemotingCommandException {",
            "    /** 校验 commitOrRollback 必须为合法事务类型。 */\n    public void checkFields() throws RemotingCommandException {",
        ),
        (
            "    public String getProducerGroup() {",
            "    /** 返回生产者组。 */\n    public String getProducerGroup() {",
        ),
        (
            "    public Integer getCommitOrRollback() {",
            "    /** 返回提交/回滚标志。 */\n    public Integer getCommitOrRollback() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/CloneGroupOffsetRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.CLONE_GROUP_OFFSET, action = Action.UPDATE)\npublic class CloneGroupOffsetRequestHeader extends RpcRequestHeader {",
            "/**\n * 克隆消费组位点请求头：将源组在某 Topic 上的消费进度复制到目标组。\n */\n@RocketMQAction(value = RequestCode.CLONE_GROUP_OFFSET, action = Action.UPDATE)\npublic class CloneGroupOffsetRequestHeader extends RpcRequestHeader {",
        ),
        (
            "    @CFNotNull\n    private String srcGroup;",
            "    /** 源消费组名称。 */\n    @CFNotNull\n    private String srcGroup;",
        ),
        (
            "    @CFNotNull\n    @RocketMQResource(ResourceType.GROUP)\n    private String destGroup;",
            "    /** 目标消费组名称。 */\n    @CFNotNull\n    @RocketMQResource(ResourceType.GROUP)\n    private String destGroup;",
        ),
        (
            "    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
            "    /** 待克隆位点的 Topic。 */\n    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
        ),
        (
            "    private boolean offline;",
            "    /** 是否离线克隆（不通知在线消费者）。 */\n    private boolean offline;",
        ),
        (
            "    public String getDestGroup() {",
            "    /** 返回目标消费组。 */\n    public String getDestGroup() {",
        ),
        (
            "    public String getSrcGroup() {",
            "    /** 返回源消费组。 */\n    public String getSrcGroup() {",
        ),
        (
            "    public boolean isOffline() {",
            "    /** 返回是否离线克隆。 */\n    public boolean isOffline() {",
        ),
        (
            "    @Override\n    public String toString() {",
            "    /** 返回便于诊断的可读字符串。 */\n    @Override\n    public String toString() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/ConsumeMessageDirectlyResultRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.CONSUME_MESSAGE_DIRECTLY, action = Action.SUB)\npublic class ConsumeMessageDirectlyResultRequestHeader extends TopicRequestHeader {",
            "/**\n * 直接消费消息结果查询请求头：按消费组、客户端与 MsgId 定位消费结果。\n */\n@RocketMQAction(value = RequestCode.CONSUME_MESSAGE_DIRECTLY, action = Action.SUB)\npublic class ConsumeMessageDirectlyResultRequestHeader extends TopicRequestHeader {",
        ),
        (
            "    @CFNotNull\n    @RocketMQResource(ResourceType.GROUP)\n    private String consumerGroup;",
            "    /** 消费组名称。 */\n    @CFNotNull\n    @RocketMQResource(ResourceType.GROUP)\n    private String consumerGroup;",
        ),
        (
            "    @CFNullable\n    private String clientId;",
            "    /** 消费者客户端 ID（可选）。 */\n    @CFNullable\n    private String clientId;",
        ),
        (
            "    @CFNullable\n    private String msgId;",
            "    /** 目标消息 ID（可选）。 */\n    @CFNullable\n    private String msgId;",
        ),
        (
            "    @CFNullable\n    private String brokerName;",
            "    /** 承载消息的 Broker 名称（可选）。 */\n    @CFNullable\n    private String brokerName;",
        ),
        (
            "    @CFNullable\n    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
            "    /** 消息所属 Topic（可选）。 */\n    @CFNullable\n    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
        ),
        (
            "    @CFNullable\n    private Integer topicSysFlag;",
            "    /** Topic 系统标志位（可选）。 */\n    @CFNullable\n    private Integer topicSysFlag;",
        ),
        (
            "    @CFNullable\n    private Integer groupSysFlag;",
            "    /** 消费组系统标志位（可选）。 */\n    @CFNullable\n    private Integer groupSysFlag;",
        ),
        (
            "    public String getConsumerGroup() {",
            "    /** 返回消费组名称。 */\n    public String getConsumerGroup() {",
        ),
        (
            "    public String getBrokerName() {",
            "    /** 返回 Broker 名称。 */\n    public String getBrokerName() {",
        ),
        (
            "    public String getMsgId() {",
            "    /** 返回消息 ID。 */\n    public String getMsgId() {",
        ),
        (
            "    @Override\n    public String toString() {",
            "    /** 返回便于诊断的可读字符串。 */\n    @Override\n    public String toString() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/ConsumerSendMsgBackRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.CONSUMER_SEND_MSG_BACK, action = Action.SUB)\npublic class ConsumerSendMsgBackRequestHeader extends RpcRequestHeader {",
            "/**\n * 消费者消息回退请求头：将消费失败的消息重新投递到重试队列。\n */\n@RocketMQAction(value = RequestCode.CONSUMER_SEND_MSG_BACK, action = Action.SUB)\npublic class ConsumerSendMsgBackRequestHeader extends RpcRequestHeader {",
        ),
        (
            "    @CFNotNull\n    private Long offset;",
            "    /** 消息在 CommitLog 中的位点。 */\n    @CFNotNull\n    private Long offset;",
        ),
        (
            "    @CFNotNull\n    @RocketMQResource(ResourceType.GROUP)\n    private String group;",
            "    /** 消费组名称。 */\n    @CFNotNull\n    @RocketMQResource(ResourceType.GROUP)\n    private String group;",
        ),
        (
            "    @CFNotNull\n    private Integer delayLevel;",
            "    /** 延迟级别（决定重投间隔）。 */\n    @CFNotNull\n    private Integer delayLevel;",
        ),
        (
            "    private String originMsgId;",
            "    /** 原始消息 ID。 */\n    private String originMsgId;",
        ),
        (
            "    @RocketMQResource(ResourceType.TOPIC)\n    private String originTopic;",
            "    /** 原始 Topic 名称。 */\n    @RocketMQResource(ResourceType.TOPIC)\n    private String originTopic;",
        ),
        (
            "    @CFNullable\n    private boolean unitMode = false;",
            "    /** 是否单元化模式。 */\n    @CFNullable\n    private boolean unitMode = false;",
        ),
        (
            "    private Integer maxReconsumeTimes;",
            "    /** 最大重消费次数上限。 */\n    private Integer maxReconsumeTimes;",
        ),
        (
            "    public Long getOffset() {",
            "    /** 返回消息位点。 */\n    public Long getOffset() {",
        ),
        (
            "    public Integer getDelayLevel() {",
            "    /** 返回延迟级别。 */\n    public Integer getDelayLevel() {",
        ),
        (
            "    public boolean isUnitMode() {",
            "    /** 返回是否单元化模式。 */\n    public boolean isUnitMode() {",
        ),
        (
            "    public Integer getMaxReconsumeTimes() {",
            "    /** 返回最大重消费次数。 */\n    public Integer getMaxReconsumeTimes() {",
        ),
        (
            "    @Override\n    public String toString() {",
            "    /** 返回便于诊断的可读字符串。 */\n    @Override\n    public String toString() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/CreateAclRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.AUTH_CREATE_ACL, resource = ResourceType.CLUSTER, action = Action.UPDATE)\npublic class CreateAclRequestHeader implements CommandCustomHeader {",
            "/**\n * 创建 ACL 权限规则请求头：指定授权主体（Subject）。\n */\n@RocketMQAction(value = RequestCode.AUTH_CREATE_ACL, resource = ResourceType.CLUSTER, action = Action.UPDATE)\npublic class CreateAclRequestHeader implements CommandCustomHeader {",
        ),
        (
            "    private String subject;",
            "    /** ACL 授权主体（用户或角色）。 */\n    private String subject;",
        ),
        (
            "    public CreateAclRequestHeader() {",
            "    /** 默认构造器。 */\n    public CreateAclRequestHeader() {",
        ),
        (
            "    public CreateAclRequestHeader(String subject) {",
            "    /** 以指定主体构造请求头。 */\n    public CreateAclRequestHeader(String subject) {",
        ),
        (
            "    public String getSubject() {",
            "    /** 返回授权主体。 */\n    public String getSubject() {",
        ),
        (
            "    public void setSubject(String subject) {",
            "    /** 设置授权主体。 */\n    public void setSubject(String subject) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/CreateTopicListRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.UPDATE_AND_CREATE_TOPIC_LIST, action = Action.CREATE)\npublic class CreateTopicListRequestHeader extends RpcRequestHeader {",
            "/**\n * 批量创建 Topic 请求头：配合 RPC 请求体一次性创建多个 Topic。\n */\n@RocketMQAction(value = RequestCode.UPDATE_AND_CREATE_TOPIC_LIST, action = Action.CREATE)\npublic class CreateTopicListRequestHeader extends RpcRequestHeader {",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {",
            "    /** 字段校验（当前无额外约束）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/CreateTopicRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.UPDATE_AND_CREATE_TOPIC, action = Action.CREATE)\npublic class CreateTopicRequestHeader extends TopicRequestHeader {",
            "/**\n * 创建或更新 Topic 请求头：配置读写队列数、权限、过滤类型及顺序属性。\n */\n@RocketMQAction(value = RequestCode.UPDATE_AND_CREATE_TOPIC, action = Action.CREATE)\npublic class CreateTopicRequestHeader extends TopicRequestHeader {",
        ),
        (
            "    @CFNotNull\n    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
            "    /** 目标 Topic 名称。 */\n    @CFNotNull\n    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
        ),
        (
            "    @CFNotNull\n    private String defaultTopic;",
            "    /** 默认 Topic（用于路由占位）。 */\n    @CFNotNull\n    private String defaultTopic;",
        ),
        (
            "    @CFNotNull\n    private Integer readQueueNums;",
            "    /** 读队列数量。 */\n    @CFNotNull\n    private Integer readQueueNums;",
        ),
        (
            "    @CFNotNull\n    private Integer writeQueueNums;",
            "    /** 写队列数量。 */\n    @CFNotNull\n    private Integer writeQueueNums;",
        ),
        (
            "    @CFNotNull\n    private Integer perm;",
            "    /** Topic 权限位（读/写/继承）。 */\n    @CFNotNull\n    private Integer perm;",
        ),
        (
            "    @CFNotNull\n    private String topicFilterType;",
            "    /** 消息过滤类型（SINGLE_TAG / MULTI_TAG）。 */\n    @CFNotNull\n    private String topicFilterType;",
        ),
        (
            "    private Integer topicSysFlag;",
            "    /** Topic 系统标志位。 */\n    private Integer topicSysFlag;",
        ),
        (
            "    @CFNotNull\n    private Boolean order = false;",
            "    /** 是否顺序 Topic。 */\n    @CFNotNull\n    private Boolean order = false;",
        ),
        (
            "    private String attributes;",
            "    /** Topic 扩展属性（JSON 字符串）。 */\n    private String attributes;",
        ),
        (
            "    @CFNullable\n    private Boolean force = false;",
            "    /** 是否强制覆盖已有配置。 */\n    @CFNullable\n    private Boolean force = false;",
        ),
        (
            "    public void checkFields() throws RemotingCommandException {",
            "    /** 校验 topicFilterType 是否为合法枚举值。 */\n    public void checkFields() throws RemotingCommandException {",
        ),
        (
            "    public TopicFilterType getTopicFilterTypeEnum() {",
            "    /** 返回过滤类型枚举。 */\n    public TopicFilterType getTopicFilterTypeEnum() {",
        ),
        (
            "    public Integer getReadQueueNums() {",
            "    /** 返回读队列数。 */\n    public Integer getReadQueueNums() {",
        ),
        (
            "    public Boolean getOrder() {",
            "    /** 返回是否顺序 Topic。 */\n    public Boolean getOrder() {",
        ),
        (
            "    public Boolean getForce() {",
            "    /** 返回是否强制覆盖。 */\n    public Boolean getForce() {",
        ),
        (
            "    @Override\n    public String toString() {",
            "    /** 返回便于诊断的可读字符串。 */\n    @Override\n    public String toString() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/CreateUserRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.AUTH_CREATE_USER, resource = ResourceType.CLUSTER, action = Action.UPDATE)\npublic class CreateUserRequestHeader implements CommandCustomHeader {",
            "/**\n * 创建集群用户请求头：指定新用户名。\n */\n@RocketMQAction(value = RequestCode.AUTH_CREATE_USER, resource = ResourceType.CLUSTER, action = Action.UPDATE)\npublic class CreateUserRequestHeader implements CommandCustomHeader {",
        ),
        (
            "    private String username;",
            "    /** 待创建的用户名。 */\n    private String username;",
        ),
        (
            "    public CreateUserRequestHeader() {",
            "    /** 默认构造器。 */\n    public CreateUserRequestHeader() {",
        ),
        (
            "    public CreateUserRequestHeader(String username) {",
            "    /** 以指定用户名构造请求头。 */\n    public CreateUserRequestHeader(String username) {",
        ),
        (
            "    public String getUsername() {",
            "    /** 返回用户名。 */\n    public String getUsername() {",
        ),
        (
            "    public void setUsername(String username) {",
            "    /** 设置用户名。 */\n    public void setUsername(String username) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/DeleteAclRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.AUTH_DELETE_ACL, resource = ResourceType.CLUSTER, action = Action.UPDATE)\npublic class DeleteAclRequestHeader implements CommandCustomHeader {",
            "/**\n * 删除 ACL 权限规则请求头：按主体、策略类型与资源定位待删规则。\n */\n@RocketMQAction(value = RequestCode.AUTH_DELETE_ACL, resource = ResourceType.CLUSTER, action = Action.UPDATE)\npublic class DeleteAclRequestHeader implements CommandCustomHeader {",
        ),
        (
            "    private String subject;",
            "    /** ACL 授权主体。 */\n    private String subject;",
        ),
        (
            "    private String policyType;",
            "    /** 策略类型（如 Custom / Default）。 */\n    private String policyType;",
        ),
        (
            "    private String resource;",
            "    /** 受控资源标识（Topic/Group/Cluster 等）。 */\n    private String resource;",
        ),
        (
            "    public DeleteAclRequestHeader() {",
            "    /** 默认构造器。 */\n    public DeleteAclRequestHeader() {",
        ),
        (
            "    public DeleteAclRequestHeader(String subject, String resource) {",
            "    /** 以主体与资源构造请求头。 */\n    public DeleteAclRequestHeader(String subject, String resource) {",
        ),
        (
            "    public String getSubject() {",
            "    /** 返回授权主体。 */\n    public String getSubject() {",
        ),
        (
            "    public String getPolicyType() {",
            "    /** 返回策略类型。 */\n    public String getPolicyType() {",
        ),
        (
            "    public String getResource() {",
            "    /** 返回受控资源。 */\n    public String getResource() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/DeleteSubscriptionGroupRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.DELETE_SUBSCRIPTIONGROUP, action = Action.DELETE)\npublic class DeleteSubscriptionGroupRequestHeader extends RpcRequestHeader {",
            "/**\n * 删除订阅组请求头：移除消费组并可选择清理其消费位点。\n */\n@RocketMQAction(value = RequestCode.DELETE_SUBSCRIPTIONGROUP, action = Action.DELETE)\npublic class DeleteSubscriptionGroupRequestHeader extends RpcRequestHeader {",
        ),
        (
            "    @CFNotNull\n    @RocketMQResource(ResourceType.GROUP)\n    private String groupName;",
            "    /** 待删除的消费组名称。 */\n    @CFNotNull\n    @RocketMQResource(ResourceType.GROUP)\n    private String groupName;",
        ),
        (
            "    private boolean cleanOffset = false;",
            "    /** 是否同步清理该组的消费位点。 */\n    private boolean cleanOffset = false;",
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
            "    public boolean isCleanOffset() {",
            "    /** 返回是否清理位点。 */\n    public boolean isCleanOffset() {",
        ),
        (
            "    public void setCleanOffset(boolean cleanOffset) {",
            "    /** 设置是否清理位点。 */\n    public void setCleanOffset(boolean cleanOffset) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/DeleteTopicRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.DELETE_TOPIC_IN_BROKER, action = Action.DELETE)\npublic class DeleteTopicRequestHeader extends TopicRequestHeader {",
            "/**\n * 在 Broker 上删除 Topic 请求头：指定待移除的 Topic 名称。\n */\n@RocketMQAction(value = RequestCode.DELETE_TOPIC_IN_BROKER, action = Action.DELETE)\npublic class DeleteTopicRequestHeader extends TopicRequestHeader {",
        ),
        (
            "    @CFNotNull\n    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
            "    /** 待删除的 Topic 名称。 */\n    @CFNotNull\n    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
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
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/DeleteUserRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.AUTH_DELETE_USER, resource = ResourceType.CLUSTER, action = Action.UPDATE)\npublic class DeleteUserRequestHeader implements CommandCustomHeader {",
            "/**\n * 删除集群用户请求头：指定待移除的用户名。\n */\n@RocketMQAction(value = RequestCode.AUTH_DELETE_USER, resource = ResourceType.CLUSTER, action = Action.UPDATE)\npublic class DeleteUserRequestHeader implements CommandCustomHeader {",
        ),
        (
            "    private String username;",
            "    /** 待删除的用户名。 */\n    private String username;",
        ),
        (
            "    public DeleteUserRequestHeader() {",
            "    /** 默认构造器。 */\n    public DeleteUserRequestHeader() {",
        ),
        (
            "    public DeleteUserRequestHeader(String username) {",
            "    /** 以指定用户名构造请求头。 */\n    public DeleteUserRequestHeader(String username) {",
        ),
        (
            "    public String getUsername() {",
            "    /** 返回用户名。 */\n    public String getUsername() {",
        ),
        (
            "    public void setUsername(String username) {",
            "    /** 设置用户名。 */\n    public void setUsername(String username) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/EndTransactionRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.END_TRANSACTION, action = Action.PUB)\npublic class EndTransactionRequestHeader extends RpcRequestHeader {",
            "/**\n * 结束事务请求头：Producer 提交或回滚半消息，完成两阶段事务。\n */\n@RocketMQAction(value = RequestCode.END_TRANSACTION, action = Action.PUB)\npublic class EndTransactionRequestHeader extends RpcRequestHeader {",
        ),
        (
            "    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
            "    /** 半消息所属 Topic。 */\n    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
        ),
        (
            "    @CFNotNull\n    private String producerGroup;",
            "    /** 生产者组名称。 */\n    @CFNotNull\n    private String producerGroup;",
        ),
        (
            "    @CFNotNull\n    private Long tranStateTableOffset;",
            "    /** 事务状态表位点。 */\n    @CFNotNull\n    private Long tranStateTableOffset;",
        ),
        (
            "    @CFNotNull\n    private Long commitLogOffset;",
            "    /** CommitLog 物理位点。 */\n    @CFNotNull\n    private Long commitLogOffset;",
        ),
        (
            "    @CFNotNull\n    private Integer commitOrRollback; // TRANSACTION_COMMIT_TYPE",
            "    /** 提交/回滚/未知标志（TRANSACTION_COMMIT_TYPE / ROLLBACK / NOT）。 */\n    @CFNotNull\n    private Integer commitOrRollback; // TRANSACTION_COMMIT_TYPE",
        ),
        (
            "    @CFNullable\n    private Boolean fromTransactionCheck = false;",
            "    /** 是否由 Broker 事务回查触发。 */\n    @CFNullable\n    private Boolean fromTransactionCheck = false;",
        ),
        (
            "    @CFNotNull\n    private String msgId;",
            "    /** 半消息 ID。 */\n    @CFNotNull\n    private String msgId;",
        ),
        (
            "    private String transactionId;",
            "    /** 事务 ID。 */\n    private String transactionId;",
        ),
        (
            "    public void checkFields() throws RemotingCommandException {",
            "    /** 校验 commitOrRollback 必须为合法事务类型。 */\n    public void checkFields() throws RemotingCommandException {",
        ),
        (
            "    public String getProducerGroup() {",
            "    /** 返回生产者组。 */\n    public String getProducerGroup() {",
        ),
        (
            "    public Integer getCommitOrRollback() {",
            "    /** 返回提交/回滚标志。 */\n    public Integer getCommitOrRollback() {",
        ),
        (
            "    public Boolean getFromTransactionCheck() {",
            "    /** 返回是否由回查触发。 */\n    public Boolean getFromTransactionCheck() {",
        ),
        (
            "    public String getMsgId() {",
            "    /** 返回消息 ID。 */\n    public String getMsgId() {",
        ),
        (
            "    public String getTransactionId() {",
            "    /** 返回事务 ID。 */\n    public String getTransactionId() {",
        ),
        (
            "    @Override\n    public String toString() {",
            "    /** 返回便于诊断的可读字符串。 */\n    @Override\n    public String toString() {",
        ),
    ],
}
