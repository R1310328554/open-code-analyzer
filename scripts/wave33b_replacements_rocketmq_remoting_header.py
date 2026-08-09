"""Chinese JavaDoc replacements for RocketMQ wave33b remoting protocol header [15:30]."""

R: dict[str, list[tuple[str, str]]] = {
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/QueryConsumerOffsetRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.QUERY_CONSUMER_OFFSET, action = Action.GET)\npublic class QueryConsumerOffsetRequestHeader extends TopicQueueRequestHeader {",
            "/**\n * 查询消费位点的请求头：按消费组、Topic 与队列 ID 获取当前消费进度。\n * 若未找到位点且 setZeroIfNotFound 为 true，则返回 0。\n */\n@RocketMQAction(value = RequestCode.QUERY_CONSUMER_OFFSET, action = Action.GET)\npublic class QueryConsumerOffsetRequestHeader extends TopicQueueRequestHeader {",
        ),
        (
            "    @CFNotNull\n    @RocketMQResource(ResourceType.GROUP)\n    private String consumerGroup;",
            "    /** 目标消费组名称。 */\n    @CFNotNull\n    @RocketMQResource(ResourceType.GROUP)\n    private String consumerGroup;",
        ),
        (
            "    @CFNotNull\n    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
            "    /** 目标 Topic 名称。 */\n    @CFNotNull\n    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
        ),
        (
            "    @CFNotNull\n    private Integer queueId;",
            "    /** 消息队列 ID。 */\n    @CFNotNull\n    private Integer queueId;",
        ),
        (
            "    private Boolean setZeroIfNotFound;",
            "    /** 未找到位点时是否返回 0，可为空。 */\n    private Boolean setZeroIfNotFound;",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
            "    /** 校验请求头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
        ),
        (
            "    public String getConsumerGroup() {",
            "    /** 返回消费组名称。 */\n    public String getConsumerGroup() {",
        ),
        (
            "    public void setConsumerGroup(String consumerGroup) {",
            "    /** 设置消费组名称。 */\n    public void setConsumerGroup(String consumerGroup) {",
        ),
        (
            "    @Override\n    public String getTopic() {",
            "    /** 返回 Topic 名称。 */\n    @Override\n    public String getTopic() {",
        ),
        (
            "    @Override\n    public void setTopic(String topic) {",
            "    /** 设置 Topic 名称。 */\n    @Override\n    public void setTopic(String topic) {",
        ),
        (
            "    @Override\n    public Integer getQueueId() {",
            "    /** 返回队列 ID。 */\n    @Override\n    public Integer getQueueId() {",
        ),
        (
            "    @Override\n    public void setQueueId(Integer queueId) {",
            "    /** 设置队列 ID。 */\n    @Override\n    public void setQueueId(Integer queueId) {",
        ),
        (
            "    public Boolean getSetZeroIfNotFound() {",
            "    /** 返回未找到位点时是否归零。 */\n    public Boolean getSetZeroIfNotFound() {",
        ),
        (
            "    public void setSetZeroIfNotFound(Boolean setZeroIfNotFound) {",
            "    /** 设置未找到位点时是否归零。 */\n    public void setSetZeroIfNotFound(Boolean setZeroIfNotFound) {",
        ),
        (
            "    @Override\n    public String toString() {",
            "    /** 返回含消费组、Topic、队列及归零标志的调试字符串。 */\n    @Override\n    public String toString() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/QueryConsumerOffsetResponseHeader.java": [
        (
            "public class QueryConsumerOffsetResponseHeader implements CommandCustomHeader {",
            "/**\n * 查询消费位点的响应头：返回指定队列的消费进度 offset。\n */\npublic class QueryConsumerOffsetResponseHeader implements CommandCustomHeader {",
        ),
        (
            "    @CFNotNull\n    private Long offset;",
            "    /** 消费位点（逻辑 offset）。 */\n    @CFNotNull\n    private Long offset;",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
            "    /** 校验响应头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
        ),
        (
            "    public Long getOffset() {",
            "    /** 返回消费位点。 */\n    public Long getOffset() {",
        ),
        (
            "    public void setOffset(Long offset) {",
            "    /** 设置消费位点。 */\n    public void setOffset(Long offset) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/QueryCorrectionOffsetHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.QUERY_CORRECTION_OFFSET, action = Action.GET)\npublic class QueryCorrectionOffsetHeader extends TopicRequestHeader {",
            "/**\n * 查询消费位点校正信息的请求头：对比基准消费组与过滤组在 Topic 上的位点差异。\n * filterGroups 支持逗号分隔的多个消费组。\n */\n@RocketMQAction(value = RequestCode.QUERY_CORRECTION_OFFSET, action = Action.GET)\npublic class QueryCorrectionOffsetHeader extends TopicRequestHeader {",
        ),
        (
            "    @RocketMQResource(value = ResourceType.GROUP, splitter = \",\")\n    private String filterGroups;",
            "    /** 待对比的消费组列表，逗号分隔，可为空。 */\n    @RocketMQResource(value = ResourceType.GROUP, splitter = \",\")\n    private String filterGroups;",
        ),
        (
            "    @CFNotNull\n    @RocketMQResource(ResourceType.GROUP)\n    private String compareGroup;",
            "    /** 基准消费组名称。 */\n    @CFNotNull\n    @RocketMQResource(ResourceType.GROUP)\n    private String compareGroup;",
        ),
        (
            "    @CFNotNull\n    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
            "    /** 目标 Topic 名称。 */\n    @CFNotNull\n    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n\n    }",
            "    /** 校验请求头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n\n    }",
        ),
        (
            "    public String getFilterGroups() {",
            "    /** 返回过滤消费组列表。 */\n    public String getFilterGroups() {",
        ),
        (
            "    public void setFilterGroups(String filterGroups) {",
            "    /** 设置过滤消费组列表。 */\n    public void setFilterGroups(String filterGroups) {",
        ),
        (
            "    public String getCompareGroup() {",
            "    /** 返回基准消费组名称。 */\n    public String getCompareGroup() {",
        ),
        (
            "    public void setCompareGroup(String compareGroup) {",
            "    /** 设置基准消费组名称。 */\n    public void setCompareGroup(String compareGroup) {",
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
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/QueryMessageRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.QUERY_MESSAGE, action = {Action.SUB, Action.GET})\npublic class QueryMessageRequestHeader extends TopicRequestHeader {",
            "/**\n * 按索引查询消息的请求头：支持按 Key、时间范围及索引类型检索 Topic 中的消息。\n * 用于运维排查与消息追踪场景。\n */\n@RocketMQAction(value = RequestCode.QUERY_MESSAGE, action = {Action.SUB, Action.GET})\npublic class QueryMessageRequestHeader extends TopicRequestHeader {",
        ),
        (
            "    @CFNotNull\n    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
            "    /** 目标 Topic 名称。 */\n    @CFNotNull\n    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
        ),
        (
            "    @CFNotNull\n    private String key;",
            "    /** 消息索引 Key（业务键或消息 ID）。 */\n    @CFNotNull\n    private String key;",
        ),
        (
            "    @CFNotNull\n    private Integer maxNum;",
            "    /** 单次查询返回的最大消息条数。 */\n    @CFNotNull\n    private Integer maxNum;",
        ),
        (
            "    @CFNotNull\n    private Long beginTimestamp;",
            "    /** 查询起始时间戳（毫秒）。 */\n    @CFNotNull\n    private Long beginTimestamp;",
        ),
        (
            "    @CFNotNull\n    private Long endTimestamp;",
            "    /** 查询结束时间戳（毫秒）。 */\n    @CFNotNull\n    private Long endTimestamp;",
        ),
        (
            "    private String indexType;",
            "    /** 索引类型（如 UNIQ_KEY、TIME），可为空。 */\n    private String indexType;",
        ),
        (
            "    private String lastKey;",
            "    /** 分页游标：上一批最后一条消息的 Key，可为空。 */\n    private String lastKey;",
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
            "    public String getKey() {",
            "    /** 返回索引 Key。 */\n    public String getKey() {",
        ),
        (
            "    public void setKey(String key) {",
            "    /** 设置索引 Key。 */\n    public void setKey(String key) {",
        ),
        (
            "    public Integer getMaxNum() {",
            "    /** 返回最大返回条数。 */\n    public Integer getMaxNum() {",
        ),
        (
            "    public void setMaxNum(Integer maxNum) {",
            "    /** 设置最大返回条数。 */\n    public void setMaxNum(Integer maxNum) {",
        ),
        (
            "    public Long getBeginTimestamp() {",
            "    /** 返回起始时间戳。 */\n    public Long getBeginTimestamp() {",
        ),
        (
            "    public void setBeginTimestamp(Long beginTimestamp) {",
            "    /** 设置起始时间戳。 */\n    public void setBeginTimestamp(Long beginTimestamp) {",
        ),
        (
            "    public Long getEndTimestamp() {",
            "    /** 返回结束时间戳。 */\n    public Long getEndTimestamp() {",
        ),
        (
            "    public void setEndTimestamp(Long endTimestamp) {",
            "    /** 设置结束时间戳。 */\n    public void setEndTimestamp(Long endTimestamp) {",
        ),
        (
            "    public String getIndexType() {",
            "    /** 返回索引类型。 */\n    public String getIndexType() {",
        ),
        (
            "    public void setIndexType(String indexType) {",
            "    /** 设置索引类型。 */\n    public void setIndexType(String indexType) {",
        ),
        (
            "    public String getLastKey() {",
            "    /** 返回分页游标 Key。 */\n    public String getLastKey() {",
        ),
        (
            "    public void setLastKey(String lastKey) {",
            "    /** 设置分页游标 Key。 */\n    public void setLastKey(String lastKey) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/QueryMessageResponseHeader.java": [
        (
            "public class QueryMessageResponseHeader implements CommandCustomHeader {",
            "/**\n * 按索引查询消息的响应头：返回索引文件最后更新的时间与物理 offset。\n * 用于分页查询时判断索引是否已更新。\n */\npublic class QueryMessageResponseHeader implements CommandCustomHeader {",
        ),
        (
            "    @CFNotNull\n    private Long indexLastUpdateTimestamp;",
            "    /** 索引文件最后更新时间戳（毫秒）。 */\n    @CFNotNull\n    private Long indexLastUpdateTimestamp;",
        ),
        (
            "    @CFNotNull\n    private Long indexLastUpdatePhyoffset;",
            "    /** 索引文件最后更新对应的物理 offset。 */\n    @CFNotNull\n    private Long indexLastUpdatePhyoffset;",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
            "    /** 校验响应头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
        ),
        (
            "    public Long getIndexLastUpdateTimestamp() {",
            "    /** 返回索引最后更新时间戳。 */\n    public Long getIndexLastUpdateTimestamp() {",
        ),
        (
            "    public void setIndexLastUpdateTimestamp(Long indexLastUpdateTimestamp) {",
            "    /** 设置索引最后更新时间戳。 */\n    public void setIndexLastUpdateTimestamp(Long indexLastUpdateTimestamp) {",
        ),
        (
            "    public Long getIndexLastUpdatePhyoffset() {",
            "    /** 返回索引最后更新物理 offset。 */\n    public Long getIndexLastUpdatePhyoffset() {",
        ),
        (
            "    public void setIndexLastUpdatePhyoffset(Long indexLastUpdatePhyoffset) {",
            "    /** 设置索引最后更新物理 offset。 */\n    public void setIndexLastUpdatePhyoffset(Long indexLastUpdatePhyoffset) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/QuerySubscriptionByConsumerRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.QUERY_SUBSCRIPTION_BY_CONSUMER, action = Action.GET)\npublic class QuerySubscriptionByConsumerRequestHeader extends TopicRequestHeader {",
            "/**\n * 按消费组查询订阅关系的请求头：获取消费组对 Topic 的订阅配置。\n * topic 可为空，表示查询该消费组全部订阅。\n */\n@RocketMQAction(value = RequestCode.QUERY_SUBSCRIPTION_BY_CONSUMER, action = Action.GET)\npublic class QuerySubscriptionByConsumerRequestHeader extends TopicRequestHeader {",
        ),
        (
            "    @CFNotNull\n    @RocketMQResource(ResourceType.GROUP)\n    private String group;",
            "    /** 目标消费组名称。 */\n    @CFNotNull\n    @RocketMQResource(ResourceType.GROUP)\n    private String group;",
        ),
        (
            "    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
            "    /** 可选 Topic 名称，为空则返回全部订阅。 */\n    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n\n    }",
            "    /** 校验请求头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n\n    }",
        ),
        (
            "    public String getGroup() {",
            "    /** 返回消费组名称。 */\n    public String getGroup() {",
        ),
        (
            "    public void setGroup(String group) {",
            "    /** 设置消费组名称。 */\n    public void setGroup(String group) {",
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
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/QueryTopicConsumeByWhoRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.QUERY_TOPIC_CONSUME_BY_WHO, action = Action.GET)\npublic class QueryTopicConsumeByWhoRequestHeader extends TopicRequestHeader {",
            "/**\n * 查询 Topic 被哪些消费组消费的请求头：返回订阅该 Topic 的所有消费组列表。\n */\n@RocketMQAction(value = RequestCode.QUERY_TOPIC_CONSUME_BY_WHO, action = Action.GET)\npublic class QueryTopicConsumeByWhoRequestHeader extends TopicRequestHeader {",
        ),
        (
            "    @CFNotNull\n    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
            "    /** 目标 Topic 名称。 */\n    @CFNotNull\n    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
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
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/QueryTopicsByConsumerRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.QUERY_TOPICS_BY_CONSUMER, action = Action.GET)\npublic class QueryTopicsByConsumerRequestHeader extends RpcRequestHeader {",
            "/**\n * 按消费组查询订阅 Topic 列表的请求头：返回该消费组当前订阅的所有 Topic。\n */\n@RocketMQAction(value = RequestCode.QUERY_TOPICS_BY_CONSUMER, action = Action.GET)\npublic class QueryTopicsByConsumerRequestHeader extends RpcRequestHeader {",
        ),
        (
            "    @CFNotNull\n    @RocketMQResource(ResourceType.GROUP)\n    private String group;",
            "    /** 目标消费组名称。 */\n    @CFNotNull\n    @RocketMQResource(ResourceType.GROUP)\n    private String group;",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n\n    }",
            "    /** 校验请求头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n\n    }",
        ),
        (
            "    public String getGroup() {",
            "    /** 返回消费组名称。 */\n    public String getGroup() {",
        ),
        (
            "    public void setGroup(String group) {",
            "    /** 设置消费组名称。 */\n    public void setGroup(String group) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/RecallMessageRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.RECALL_MESSAGE, action = Action.PUB)\npublic class RecallMessageRequestHeader extends TopicRequestHeader {",
            "/**\n * 撤回已发送消息的请求头：Producer 通过 recallHandle 标识撤回指定 Topic 下的消息。\n * producerGroup 可为空，由 Broker 从连接上下文推断。\n */\n@RocketMQAction(value = RequestCode.RECALL_MESSAGE, action = Action.PUB)\npublic class RecallMessageRequestHeader extends TopicRequestHeader {",
        ),
        (
            "    @CFNullable\n    private String producerGroup;",
            "    /** 生产者组名称，可为空。 */\n    @CFNullable\n    private String producerGroup;",
        ),
        (
            "    @CFNotNull\n    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
            "    /** 目标 Topic 名称。 */\n    @CFNotNull\n    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
        ),
        (
            "    @CFNotNull\n    private String recallHandle;",
            "    /** 消息撤回句柄，由发送响应返回。 */\n    @CFNotNull\n    private String recallHandle;",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
            "    /** 校验请求头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
        ),
        (
            "    public String getProducerGroup() {",
            "    /** 返回生产者组名称。 */\n    public String getProducerGroup() {",
        ),
        (
            "    public void setProducerGroup(String producerGroup) {",
            "    /** 设置生产者组名称。 */\n    public void setProducerGroup(String producerGroup) {",
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
            "    public String getRecallHandle() {",
            "    /** 返回消息撤回句柄。 */\n    public String getRecallHandle() {",
        ),
        (
            "    public void setRecallHandle(String recallHandle) {",
            "    /** 设置消息撤回句柄。 */\n    public void setRecallHandle(String recallHandle) {",
        ),
        (
            "    @Override\n    public String toString() {",
            "    /** 返回含生产者组、Topic 与撤回句柄的调试字符串。 */\n    @Override\n    public String toString() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/RecallMessageResponseHeader.java": [
        (
            "public class RecallMessageResponseHeader implements CommandCustomHeader {",
            "/**\n * 撤回消息的响应头：返回被撤回消息的消息 ID。\n */\npublic class RecallMessageResponseHeader implements CommandCustomHeader {",
        ),
        (
            "    @CFNotNull\n    private String msgId;",
            "    /** 被撤回消息的唯一 ID。 */\n    @CFNotNull\n    private String msgId;",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
            "    /** 校验响应头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
        ),
        (
            "    public String getMsgId() {",
            "    /** 返回被撤回消息 ID。 */\n    public String getMsgId() {",
        ),
        (
            "    public void setMsgId(String msgId) {",
            "    /** 设置被撤回消息 ID。 */\n    public void setMsgId(String msgId) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/RemoveBrokerRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.REMOVE_BROKER, resource = ResourceType.CLUSTER,action = Action.UPDATE)\npublic class RemoveBrokerRequestHeader implements CommandCustomHeader {",
            "/**\n * 从集群移除 Broker 的请求头：指定 Broker 名称、集群名与 Broker ID。\n * 由 NameServer 或 Controller 发起，用于 Broker 下线或故障摘除。\n */\n@RocketMQAction(value = RequestCode.REMOVE_BROKER, resource = ResourceType.CLUSTER,action = Action.UPDATE)\npublic class RemoveBrokerRequestHeader implements CommandCustomHeader {",
        ),
        (
            "    @CFNotNull\n    private String brokerName;",
            "    /** Broker 实例名称。 */\n    @CFNotNull\n    private String brokerName;",
        ),
        (
            "    @CFNotNull\n    @RocketMQResource(ResourceType.CLUSTER)\n    private String brokerClusterName;",
            "    /** Broker 所属集群名称。 */\n    @CFNotNull\n    @RocketMQResource(ResourceType.CLUSTER)\n    private String brokerClusterName;",
        ),
        (
            "    @CFNotNull\n    private Long brokerId;",
            "    /** Broker 角色 ID（0 为 Master，非 0 为 Slave）。 */\n    @CFNotNull\n    private Long brokerId;",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n\n    }",
            "    /** 校验请求头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n\n    }",
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
            "    public String getBrokerClusterName() {",
            "    /** 返回集群名称。 */\n    public String getBrokerClusterName() {",
        ),
        (
            "    public void setBrokerClusterName(String brokerClusterName) {",
            "    /** 设置集群名称。 */\n    public void setBrokerClusterName(String brokerClusterName) {",
        ),
        (
            "    public Long getBrokerId() {",
            "    /** 返回 Broker ID。 */\n    public Long getBrokerId() {",
        ),
        (
            "    public void setBrokerId(Long brokerId) {",
            "    /** 设置 Broker ID。 */\n    public void setBrokerId(Long brokerId) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/ReplyMessageRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.PUSH_REPLY_MESSAGE_TO_CLIENT, action = Action.SUB)\npublic class ReplyMessageRequestHeader extends TopicQueueRequestHeader {",
            "/**\n * 推送应答消息到客户端的请求头：Request-Reply 模式下 Broker 将应答消息推送给 Producer。\n * 携带完整消息元数据（Topic、队列、时间戳、属性等）。\n */\n@RocketMQAction(value = RequestCode.PUSH_REPLY_MESSAGE_TO_CLIENT, action = Action.SUB)\npublic class ReplyMessageRequestHeader extends TopicQueueRequestHeader {",
        ),
        (
            "    @CFNotNull\n    @RocketMQResource(ResourceType.GROUP)\n    private String producerGroup;",
            "    /** 生产者组名称。 */\n    @CFNotNull\n    @RocketMQResource(ResourceType.GROUP)\n    private String producerGroup;",
        ),
        (
            "    @CFNotNull\n    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
            "    /** 应答消息 Topic 名称。 */\n    @CFNotNull\n    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
        ),
        (
            "    @CFNotNull\n    private String defaultTopic;",
            "    /** 默认 Topic 名称（路由占位）。 */\n    @CFNotNull\n    private String defaultTopic;",
        ),
        (
            "    @CFNotNull\n    private Integer defaultTopicQueueNums;",
            "    /** 默认 Topic 队列数量。 */\n    @CFNotNull\n    private Integer defaultTopicQueueNums;",
        ),
        (
            "    @CFNotNull\n    private Integer queueId;",
            "    /** 消息队列 ID。 */\n    @CFNotNull\n    private Integer queueId;",
        ),
        (
            "    @CFNotNull\n    private Integer sysFlag;",
            "    /** 消息系统标志位。 */\n    @CFNotNull\n    private Integer sysFlag;",
        ),
        (
            "    @CFNotNull\n    private Long bornTimestamp;",
            "    /** 消息创建时间戳（毫秒）。 */\n    @CFNotNull\n    private Long bornTimestamp;",
        ),
        (
            "    @CFNotNull\n    private Integer flag;",
            "    /** 消息类型标志。 */\n    @CFNotNull\n    private Integer flag;",
        ),
        (
            "    @CFNullable\n    private String properties;",
            "    /** 消息用户属性（键值对字符串），可为空。 */\n    @CFNullable\n    private String properties;",
        ),
        (
            "    @CFNullable\n    private Integer reconsumeTimes;",
            "    /** 重试消费次数，可为空。 */\n    @CFNullable\n    private Integer reconsumeTimes;",
        ),
        (
            "    @CFNullable\n    private boolean unitMode = false;",
            "    /** 是否单元化模式，默认 false。 */\n    @CFNullable\n    private boolean unitMode = false;",
        ),
        (
            "    @CFNotNull\n    private String bornHost;",
            "    /** 消息创建主机地址。 */\n    @CFNotNull\n    private String bornHost;",
        ),
        (
            "    @CFNotNull\n    private String storeHost;",
            "    /** 消息存储主机地址。 */\n    @CFNotNull\n    private String storeHost;",
        ),
        (
            "    @CFNotNull\n    private long storeTimestamp;",
            "    /** 消息存储时间戳（毫秒）。 */\n    @CFNotNull\n    private long storeTimestamp;",
        ),
        (
            "    public void checkFields() throws RemotingCommandException {\n    }",
            "    /** 校验请求头字段（本类无额外约束，空实现）。 */\n    public void checkFields() throws RemotingCommandException {\n    }",
        ),
        (
            "    public String getProducerGroup() {",
            "    /** 返回生产者组名称。 */\n    public String getProducerGroup() {",
        ),
        (
            "    public void setProducerGroup(String producerGroup) {",
            "    /** 设置生产者组名称。 */\n    public void setProducerGroup(String producerGroup) {",
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
            "    public String getDefaultTopic() {",
            "    /** 返回默认 Topic 名称。 */\n    public String getDefaultTopic() {",
        ),
        (
            "    public void setDefaultTopic(String defaultTopic) {",
            "    /** 设置默认 Topic 名称。 */\n    public void setDefaultTopic(String defaultTopic) {",
        ),
        (
            "    public Integer getDefaultTopicQueueNums() {",
            "    /** 返回默认 Topic 队列数。 */\n    public Integer getDefaultTopicQueueNums() {",
        ),
        (
            "    public void setDefaultTopicQueueNums(Integer defaultTopicQueueNums) {",
            "    /** 设置默认 Topic 队列数。 */\n    public void setDefaultTopicQueueNums(Integer defaultTopicQueueNums) {",
        ),
        (
            "    public Integer getQueueId() {",
            "    /** 返回队列 ID。 */\n    public Integer getQueueId() {",
        ),
        (
            "    public void setQueueId(Integer queueId) {",
            "    /** 设置队列 ID。 */\n    public void setQueueId(Integer queueId) {",
        ),
        (
            "    public Integer getSysFlag() {",
            "    /** 返回系统标志位。 */\n    public Integer getSysFlag() {",
        ),
        (
            "    public void setSysFlag(Integer sysFlag) {",
            "    /** 设置系统标志位。 */\n    public void setSysFlag(Integer sysFlag) {",
        ),
        (
            "    public Long getBornTimestamp() {",
            "    /** 返回消息创建时间戳。 */\n    public Long getBornTimestamp() {",
        ),
        (
            "    public void setBornTimestamp(Long bornTimestamp) {",
            "    /** 设置消息创建时间戳。 */\n    public void setBornTimestamp(Long bornTimestamp) {",
        ),
        (
            "    public Integer getFlag() {",
            "    /** 返回消息类型标志。 */\n    public Integer getFlag() {",
        ),
        (
            "    public void setFlag(Integer flag) {",
            "    /** 设置消息类型标志。 */\n    public void setFlag(Integer flag) {",
        ),
        (
            "    public String getProperties() {",
            "    /** 返回消息用户属性。 */\n    public String getProperties() {",
        ),
        (
            "    public void setProperties(String properties) {",
            "    /** 设置消息用户属性。 */\n    public void setProperties(String properties) {",
        ),
        (
            "    public Integer getReconsumeTimes() {",
            "    /** 返回重试消费次数。 */\n    public Integer getReconsumeTimes() {",
        ),
        (
            "    public void setReconsumeTimes(Integer reconsumeTimes) {",
            "    /** 设置重试消费次数。 */\n    public void setReconsumeTimes(Integer reconsumeTimes) {",
        ),
        (
            "    public boolean isUnitMode() {",
            "    /** 返回是否单元化模式。 */\n    public boolean isUnitMode() {",
        ),
        (
            "    public void setUnitMode(boolean unitMode) {",
            "    /** 设置是否单元化模式。 */\n    public void setUnitMode(boolean unitMode) {",
        ),
        (
            "    public String getBornHost() {",
            "    /** 返回消息创建主机。 */\n    public String getBornHost() {",
        ),
        (
            "    public void setBornHost(String bornHost) {",
            "    /** 设置消息创建主机。 */\n    public void setBornHost(String bornHost) {",
        ),
        (
            "    public String getStoreHost() {",
            "    /** 返回消息存储主机。 */\n    public String getStoreHost() {",
        ),
        (
            "    public void setStoreHost(String storeHost) {",
            "    /** 设置消息存储主机。 */\n    public void setStoreHost(String storeHost) {",
        ),
        (
            "    public long getStoreTimestamp() {",
            "    /** 返回消息存储时间戳。 */\n    public long getStoreTimestamp() {",
        ),
        (
            "    public void setStoreTimestamp(long storeTimestamp) {",
            "    /** 设置消息存储时间戳。 */\n    public void setStoreTimestamp(long storeTimestamp) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/ResetMasterFlushOffsetHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.RESET_MASTER_FLUSH_OFFSET, resource = ResourceType.CLUSTER, action = Action.UPDATE)\npublic class ResetMasterFlushOffsetHeader implements CommandCustomHeader {",
            "/**\n * 重置 Master 刷盘位点的请求头：用于 HA 同步场景下校正 Master 的 flush offset。\n */\n@RocketMQAction(value = RequestCode.RESET_MASTER_FLUSH_OFFSET, resource = ResourceType.CLUSTER, action = Action.UPDATE)\npublic class ResetMasterFlushOffsetHeader implements CommandCustomHeader {",
        ),
        (
            "    @CFNotNull\n    private Long masterFlushOffset;",
            "    /** 新的 Master 刷盘物理 offset。 */\n    @CFNotNull\n    private Long masterFlushOffset;",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n\n    }",
            "    /** 校验请求头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n\n    }",
        ),
        (
            "    public Long getMasterFlushOffset() {",
            "    /** 返回 Master 刷盘 offset。 */\n    public Long getMasterFlushOffset() {",
        ),
        (
            "    public void setMasterFlushOffset(Long masterFlushOffset) {",
            "    /** 设置 Master 刷盘 offset。 */\n    public void setMasterFlushOffset(Long masterFlushOffset) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/ResetOffsetRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.INVOKE_BROKER_TO_RESET_OFFSET, action = Action.UPDATE)\npublic class ResetOffsetRequestHeader extends TopicQueueRequestHeader {",
            "/**\n * 重置消费位点的请求头：按时间戳或指定 offset 将消费组进度回拨。\n * queueId 为 -1 表示重置该 Topic 下所有队列；isForce 强制跳过消费端确认。\n */\n@RocketMQAction(value = RequestCode.INVOKE_BROKER_TO_RESET_OFFSET, action = Action.UPDATE)\npublic class ResetOffsetRequestHeader extends TopicQueueRequestHeader {",
        ),
        (
            "    @CFNotNull\n    @RocketMQResource(ResourceType.GROUP)\n    private String group;",
            "    /** 目标消费组名称。 */\n    @CFNotNull\n    @RocketMQResource(ResourceType.GROUP)\n    private String group;",
        ),
        (
            "    @CFNotNull\n    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
            "    /** 目标 Topic 名称。 */\n    @CFNotNull\n    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
        ),
        (
            "    private int queueId = -1;",
            "    /** 队列 ID，-1 表示全部队列。 */\n    private int queueId = -1;",
        ),
        (
            "    private Long offset;",
            "    /** 目标消费位点，可为空（按 timestamp 定位）。 */\n    private Long offset;",
        ),
        (
            "    @CFNotNull\n    private long timestamp;",
            "    /** 重置基准时间戳（毫秒）。 */\n    @CFNotNull\n    private long timestamp;",
        ),
        (
            "    @CFNotNull\n    private boolean isForce;",
            "    /** 是否强制重置（跳过消费端确认）。 */\n    @CFNotNull\n    private boolean isForce;",
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
            "    public String getGroup() {",
            "    /** 返回消费组名称。 */\n    public String getGroup() {",
        ),
        (
            "    public void setGroup(String group) {",
            "    /** 设置消费组名称。 */\n    public void setGroup(String group) {",
        ),
        (
            "    public long getTimestamp() {",
            "    /** 返回重置基准时间戳。 */\n    public long getTimestamp() {",
        ),
        (
            "    public void setTimestamp(long timestamp) {",
            "    /** 设置重置基准时间戳。 */\n    public void setTimestamp(long timestamp) {",
        ),
        (
            "    public boolean isForce() {",
            "    /** 返回是否强制重置。 */\n    public boolean isForce() {",
        ),
        (
            "    public void setForce(boolean isForce) {",
            "    /** 设置是否强制重置。 */\n    public void setForce(boolean isForce) {",
        ),
        (
            "    public Integer getQueueId() {",
            "    /** 返回队列 ID。 */\n    public Integer getQueueId() {",
        ),
        (
            "    public void setQueueId(Integer queueId) {",
            "    /** 设置队列 ID。 */\n    public void setQueueId(Integer queueId) {",
        ),
        (
            "    public Long getOffset() {",
            "    /** 返回目标消费位点。 */\n    public Long getOffset() {",
        ),
        (
            "    public void setOffset(Long offset) {",
            "    /** 设置目标消费位点。 */\n    public void setOffset(Long offset) {",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n\n    }",
            "    /** 校验请求头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n\n    }",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/ResumeCheckHalfMessageRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.RESUME_CHECK_HALF_MESSAGE, action = Action.UPDATE)\npublic class ResumeCheckHalfMessageRequestHeader implements CommandCustomHeader {",
            "/**\n * 恢复半消息回查的请求头：事务消息二阶段提交/回滚后，通知 Broker 恢复对该半消息的回查。\n */\n@RocketMQAction(value = RequestCode.RESUME_CHECK_HALF_MESSAGE, action = Action.UPDATE)\npublic class ResumeCheckHalfMessageRequestHeader implements CommandCustomHeader {",
        ),
        (
            "    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
            "    /** 半消息所在 Topic 名称。 */\n    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
        ),
        (
            "    @CFNullable\n    private String msgId;",
            "    /** 半消息 ID，可为空。 */\n    @CFNullable\n    private String msgId;",
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
            "    public String getMsgId() {",
            "    /** 返回半消息 ID。 */\n    public String getMsgId() {",
        ),
        (
            "    public void setMsgId(String msgId) {",
            "    /** 设置半消息 ID。 */\n    public void setMsgId(String msgId) {",
        ),
        (
            "    @Override\n    public String toString() {",
            "    /** 返回含消息 ID 的调试字符串。 */\n    @Override\n    public String toString() {",
        ),
    ],
}
