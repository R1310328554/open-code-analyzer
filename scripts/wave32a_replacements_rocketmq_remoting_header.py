"""Chinese JavaDoc replacements for RocketMQ wave32a remoting protocol header [0:15]."""

R: dict[str, list[tuple[str, str]]] = {
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/GetConsumerConnectionListRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.GET_CONSUMER_CONNECTION_LIST, action = Action.GET)\npublic class GetConsumerConnectionListRequestHeader extends RpcRequestHeader {",
            "/**\n * 查询消费组在线连接列表的请求头：指定消费组以获取当前连接的 Consumer 客户端。\n */\n@RocketMQAction(value = RequestCode.GET_CONSUMER_CONNECTION_LIST, action = Action.GET)\npublic class GetConsumerConnectionListRequestHeader extends RpcRequestHeader {",
        ),
        (
            "    @CFNotNull\n    @RocketMQResource(ResourceType.GROUP)\n    private String consumerGroup;",
            "    /** 目标消费组名称。 */\n    @CFNotNull\n    @RocketMQResource(ResourceType.GROUP)\n    private String consumerGroup;",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n        // To change body of implemented methods use File | Settings | File\n        // Templates.\n    }",
            "    /** 校验请求头字段（本类无额外校验逻辑）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
        ),
        (
            "    public String getConsumerGroup() {",
            "    /** 返回消费组名称。 */\n    public String getConsumerGroup() {",
        ),
        (
            "    public void setConsumerGroup(String consumerGroup) {",
            "    /** 设置消费组名称。 */\n    public void setConsumerGroup(String consumerGroup) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/GetConsumerListByGroupRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.GET_CONSUMER_LIST_BY_GROUP, action = Action.SUB)\npublic class GetConsumerListByGroupRequestHeader extends RpcRequestHeader {",
            "/**\n * 按消费组查询 Consumer 客户端 ID 列表的请求头。\n */\n@RocketMQAction(value = RequestCode.GET_CONSUMER_LIST_BY_GROUP, action = Action.SUB)\npublic class GetConsumerListByGroupRequestHeader extends RpcRequestHeader {",
        ),
        (
            "    @CFNotNull\n    @RocketMQResource(ResourceType.GROUP)\n    private String consumerGroup;",
            "    /** 目标消费组名称。 */\n    @CFNotNull\n    @RocketMQResource(ResourceType.GROUP)\n    private String consumerGroup;",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
            "    /** 校验请求头字段（空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
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
            "    @Override\n    public String toString() {",
            "    /** 返回含消费组的调试字符串。 */\n    @Override\n    public String toString() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/GetConsumerListByGroupResponseBody.java": [
        (
            "public class GetConsumerListByGroupResponseBody extends RemotingSerializable {",
            "/**\n * 按消费组查询 Consumer 列表的响应体：返回客户端 ID 列表。\n */\npublic class GetConsumerListByGroupResponseBody extends RemotingSerializable {",
        ),
        (
            "    private List<String> consumerIdList;",
            "    /** 消费组下在线 Consumer 客户端 ID 列表。 */\n    private List<String> consumerIdList;",
        ),
        (
            "    public List<String> getConsumerIdList() {",
            "    /** 返回 Consumer 客户端 ID 列表。 */\n    public List<String> getConsumerIdList() {",
        ),
        (
            "    public void setConsumerIdList(List<String> consumerIdList) {",
            "    /** 设置 Consumer 客户端 ID 列表。 */\n    public void setConsumerIdList(List<String> consumerIdList) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/GetConsumerListByGroupResponseHeader.java": [
        (
            "public class GetConsumerListByGroupResponseHeader implements CommandCustomHeader {",
            "/**\n * 按消费组查询 Consumer 列表的响应头：无附加字段，实际数据在响应体中。\n */\npublic class GetConsumerListByGroupResponseHeader implements CommandCustomHeader {",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
            "    /** 校验响应头字段（空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/GetConsumerRunningInfoRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.GET_CONSUMER_RUNNING_INFO, action = Action.GET)\npublic class GetConsumerRunningInfoRequestHeader extends RpcRequestHeader {",
            "/**\n * 获取 Consumer 运行时信息的请求头：指定消费组、客户端 ID 及是否采集 JStack。\n */\n@RocketMQAction(value = RequestCode.GET_CONSUMER_RUNNING_INFO, action = Action.GET)\npublic class GetConsumerRunningInfoRequestHeader extends RpcRequestHeader {",
        ),
        (
            "    @CFNotNull\n    @RocketMQResource(ResourceType.GROUP)\n    private String consumerGroup;",
            "    /** 目标消费组名称。 */\n    @CFNotNull\n    @RocketMQResource(ResourceType.GROUP)\n    private String consumerGroup;",
        ),
        (
            "    @CFNotNull\n    private String clientId;",
            "    /** 目标 Consumer 客户端 ID。 */\n    @CFNotNull\n    private String clientId;",
        ),
        (
            "    @CFNullable\n    private boolean jstackEnable;",
            "    /** 是否采集 JStack 线程栈，可为空。 */\n    @CFNullable\n    private boolean jstackEnable;",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
            "    /** 校验请求头字段（空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
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
            "    public String getClientId() {",
            "    /** 返回客户端 ID。 */\n    public String getClientId() {",
        ),
        (
            "    public void setClientId(String clientId) {",
            "    /** 设置客户端 ID。 */\n    public void setClientId(String clientId) {",
        ),
        (
            "    public boolean isJstackEnable() {",
            "    /** 返回是否启用 JStack 采集。 */\n    public boolean isJstackEnable() {",
        ),
        (
            "    public void setJstackEnable(boolean jstackEnable) {",
            "    /** 设置是否启用 JStack 采集。 */\n    public void setJstackEnable(boolean jstackEnable) {",
        ),
        (
            "    @Override\n    public String toString() {",
            "    /** 返回含消费组、客户端 ID 与 JStack 开关的调试字符串。 */\n    @Override\n    public String toString() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/GetConsumerStatusRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.INVOKE_BROKER_TO_GET_CONSUMER_STATUS, action = Action.GET)\npublic class GetConsumerStatusRequestHeader extends TopicRequestHeader {",
            "/**\n * 查询消费者消费进度状态的请求头：指定 Topic、消费组及可选客户端地址。\n */\n@RocketMQAction(value = RequestCode.INVOKE_BROKER_TO_GET_CONSUMER_STATUS, action = Action.GET)\npublic class GetConsumerStatusRequestHeader extends TopicRequestHeader {",
        ),
        (
            "    @CFNotNull\n    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
            "    /** 目标 Topic 名称。 */\n    @CFNotNull\n    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
        ),
        (
            "    @CFNotNull\n    @RocketMQResource(ResourceType.GROUP)\n    private String group;",
            "    /** 目标消费组名称。 */\n    @CFNotNull\n    @RocketMQResource(ResourceType.GROUP)\n    private String group;",
        ),
        (
            "    @CFNullable\n    private String clientAddr;",
            "    /** 指定 Consumer 客户端地址，可为空表示查询全组。 */\n    @CFNullable\n    private String clientAddr;",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
            "    /** 校验请求头字段（空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
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
            "    public String getClientAddr() {",
            "    /** 返回客户端地址。 */\n    public String getClientAddr() {",
        ),
        (
            "    public void setClientAddr(String clientAddr) {",
            "    /** 设置客户端地址。 */\n    public void setClientAddr(String clientAddr) {",
        ),
        (
            "    @Override\n    public String toString() {",
            "    /** 返回含 Topic、消费组与客户端地址的调试字符串。 */\n    @Override\n    public String toString() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/GetEarliestMsgStoretimeRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.GET_EARLIEST_MSG_STORETIME, action = Action.GET)\npublic class GetEarliestMsgStoretimeRequestHeader extends TopicQueueRequestHeader {",
            "/**\n * 查询队列最早消息存储时间的请求头：指定 Topic 与队列 ID。\n */\n@RocketMQAction(value = RequestCode.GET_EARLIEST_MSG_STORETIME, action = Action.GET)\npublic class GetEarliestMsgStoretimeRequestHeader extends TopicQueueRequestHeader {",
        ),
        (
            "    @CFNotNull\n    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
            "    /** 目标 Topic 名称。 */\n    @CFNotNull\n    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
        ),
        (
            "    @CFNotNull\n    private Integer queueId;",
            "    /** 目标队列 ID。 */\n    @CFNotNull\n    private Integer queueId;",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
            "    /** 校验请求头字段（空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
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
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/GetEarliestMsgStoretimeResponseHeader.java": [
        (
            "public class GetEarliestMsgStoretimeResponseHeader implements CommandCustomHeader {",
            "/**\n * 查询队列最早消息存储时间的响应头：返回最早消息的时间戳（毫秒）。\n */\npublic class GetEarliestMsgStoretimeResponseHeader implements CommandCustomHeader {",
        ),
        (
            "    @CFNotNull\n    private Long timestamp;",
            "    /** 最早消息的存储时间戳（毫秒）。 */\n    @CFNotNull\n    private Long timestamp;",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
            "    /** 校验响应头字段（空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
        ),
        (
            "    public Long getTimestamp() {",
            "    /** 返回最早消息存储时间戳。 */\n    public Long getTimestamp() {",
        ),
        (
            "    public void setTimestamp(Long timestamp) {",
            "    /** 设置最早消息存储时间戳。 */\n    public void setTimestamp(Long timestamp) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/GetLiteClientInfoRequestHeader.java": [
        (
            "public class GetLiteClientInfoRequestHeader implements CommandCustomHeader {",
            "/**\n * 查询 Lite 消费客户端信息的请求头：指定父 Topic、消费组、客户端 ID 及返回条数上限。\n */\npublic class GetLiteClientInfoRequestHeader implements CommandCustomHeader {",
        ),
        (
            "    private String parentTopic;",
            "    /** 父 Topic 名称。 */\n    private String parentTopic;",
        ),
        (
            "    private String group;",
            "    /** 消费组名称。 */\n    private String group;",
        ),
        (
            "    private String clientId;",
            "    /** Consumer 客户端 ID。 */\n    private String clientId;",
        ),
        (
            "    private int maxCount = 1000;",
            "    /** 最大返回条数，默认 1000。 */\n    private int maxCount = 1000;",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n        if (maxCount <= 0) {\n            throw new RemotingCommandException(\"[maxCount] field invalid\");\n        }\n    }",
            "    /** 校验 maxCount 必须大于 0。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n        if (maxCount <= 0) {\n            throw new RemotingCommandException(\"[maxCount] field invalid\");\n        }\n    }",
        ),
        (
            "    public String getParentTopic() {",
            "    /** 返回父 Topic 名称。 */\n    public String getParentTopic() {",
        ),
        (
            "    public void setParentTopic(String parentTopic) {",
            "    /** 设置父 Topic 名称。 */\n    public void setParentTopic(String parentTopic) {",
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
            "    public String getClientId() {",
            "    /** 返回客户端 ID。 */\n    public String getClientId() {",
        ),
        (
            "    public void setClientId(String clientId) {",
            "    /** 设置客户端 ID。 */\n    public void setClientId(String clientId) {",
        ),
        (
            "    public int getMaxCount() {",
            "    /** 返回最大返回条数。 */\n    public int getMaxCount() {",
        ),
        (
            "    public void setMaxCount(int maxCount) {",
            "    /** 设置最大返回条数。 */\n    public void setMaxCount(int maxCount) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/GetLiteGroupInfoRequestHeader.java": [
        (
            "public class GetLiteGroupInfoRequestHeader implements CommandCustomHeader {",
            "/**\n * 查询 Lite 消费组信息的请求头：指定消费组、Lite Topic 及 TopK 条数。\n */\npublic class GetLiteGroupInfoRequestHeader implements CommandCustomHeader {",
        ),
        (
            "    @CFNotNull\n    @RocketMQResource(ResourceType.GROUP)\n    private String group;",
            "    /** 目标消费组名称。 */\n    @CFNotNull\n    @RocketMQResource(ResourceType.GROUP)\n    private String group;",
        ),
        (
            "    private String liteTopic;",
            "    /** Lite Topic 名称，可选过滤条件。 */\n    private String liteTopic;",
        ),
        (
            "    private int topK;",
            "    /** 返回 TopK 条记录数。 */\n    private int topK;",
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
            "    public String getLiteTopic() {",
            "    /** 返回 Lite Topic 名称。 */\n    public String getLiteTopic() {",
        ),
        (
            "    public void setLiteTopic(String liteTopic) {",
            "    /** 设置 Lite Topic 名称。 */\n    public void setLiteTopic(String liteTopic) {",
        ),
        (
            "    public int getTopK() {",
            "    /** 返回 TopK 条数。 */\n    public int getTopK() {",
        ),
        (
            "    public void setTopK(int topK) {",
            "    /** 设置 TopK 条数。 */\n    public void setTopK(int topK) {",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
            "    /** 校验请求头字段（空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/GetLiteTopicInfoRequestHeader.java": [
        (
            "public class GetLiteTopicInfoRequestHeader implements CommandCustomHeader {",
            "/**\n * 查询 Lite Topic 信息的请求头：指定父 Topic 与 Lite Topic。\n */\npublic class GetLiteTopicInfoRequestHeader implements CommandCustomHeader {",
        ),
        (
            "    private String parentTopic;",
            "    /** 父 Topic 名称。 */\n    private String parentTopic;",
        ),
        (
            "    private String liteTopic;",
            "    /** Lite Topic 名称。 */\n    private String liteTopic;",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n\n    }",
            "    /** 校验请求头字段（空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
        ),
        (
            "    public String getParentTopic() {",
            "    /** 返回父 Topic 名称。 */\n    public String getParentTopic() {",
        ),
        (
            "    public void setParentTopic(String parentTopic) {",
            "    /** 设置父 Topic 名称。 */\n    public void setParentTopic(String parentTopic) {",
        ),
        (
            "    public String getLiteTopic() {",
            "    /** 返回 Lite Topic 名称。 */\n    public String getLiteTopic() {",
        ),
        (
            "    public void setLiteTopic(String liteTopic) {",
            "    /** 设置 Lite Topic 名称。 */\n    public void setLiteTopic(String liteTopic) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/GetMaxOffsetRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.GET_MAX_OFFSET, action = Action.GET)\npublic class GetMaxOffsetRequestHeader extends TopicQueueRequestHeader {",
            "/**\n * 查询队列最大消费位点的请求头：指定 Topic、队列 ID 及是否取已提交位点。\n */\n@RocketMQAction(value = RequestCode.GET_MAX_OFFSET, action = Action.GET)\npublic class GetMaxOffsetRequestHeader extends TopicQueueRequestHeader {",
        ),
        (
            "    @CFNotNull\n    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
            "    /** 目标 Topic 名称。 */\n    @CFNotNull\n    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
        ),
        (
            "    @CFNotNull\n    private Integer queueId;",
            "    /** 目标队列 ID。 */\n    @CFNotNull\n    private Integer queueId;",
        ),
        (
            "    /**\n     * A message at committed offset has been dispatched from Topic to MessageQueue, so it can be consumed immediately,\n     * while a message at inflight offset is not visible for a consumer temporarily.\n     * Set this flag true if the max committed offset is needed, or false if the max inflight offset is preferred.\n     * The default value is true.\n     */\n    @CFNullable\n    private boolean committed = true;",
            "    /**\n     * 是否查询已提交（committed）位点的最大值。\n     * 已提交位点表示消息已从 Topic 分发到 MessageQueue 可立即消费；\n     * 未提交（inflight）位点对 Consumer 暂时不可见。默认为 true。\n     */\n    @CFNullable\n    private boolean committed = true;",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
            "    /** 校验请求头字段（空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
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
            "    public boolean isCommitted() {",
            "    /** 返回是否查询已提交位点。 */\n    public boolean isCommitted() {",
        ),
        (
            "    public void setCommitted(final boolean committed) {",
            "    /** 设置是否查询已提交位点。 */\n    public void setCommitted(final boolean committed) {",
        ),
        (
            "    @Override\n    public String toString() {",
            "    /** 返回含 Topic、队列 ID 与 committed 标志的调试字符串。 */\n    @Override\n    public String toString() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/GetMaxOffsetResponseHeader.java": [
        (
            "public class GetMaxOffsetResponseHeader implements CommandCustomHeader {",
            "/**\n * 查询队列最大消费位点的响应头：返回最大位点 offset。\n */\npublic class GetMaxOffsetResponseHeader implements CommandCustomHeader {",
        ),
        (
            "    @CFNotNull\n    private Long offset;",
            "    /** 队列最大消费位点。 */\n    @CFNotNull\n    private Long offset;",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
            "    /** 校验响应头字段（空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
        ),
        (
            "    public Long getOffset() {",
            "    /** 返回最大消费位点。 */\n    public Long getOffset() {",
        ),
        (
            "    public void setOffset(Long offset) {",
            "    /** 设置最大消费位点。 */\n    public void setOffset(Long offset) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/GetMinOffsetRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.GET_MIN_OFFSET, action = Action.GET)\npublic class GetMinOffsetRequestHeader extends TopicQueueRequestHeader {",
            "/**\n * 查询队列最小消费位点的请求头：指定 Topic 与队列 ID。\n */\n@RocketMQAction(value = RequestCode.GET_MIN_OFFSET, action = Action.GET)\npublic class GetMinOffsetRequestHeader extends TopicQueueRequestHeader {",
        ),
        (
            "    @CFNotNull\n    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
            "    /** 目标 Topic 名称。 */\n    @CFNotNull\n    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
        ),
        (
            "    @CFNotNull\n    private Integer queueId;",
            "    /** 目标队列 ID。 */\n    @CFNotNull\n    private Integer queueId;",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
            "    /** 校验请求头字段（空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
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
            "    @Override\n    public String toString() {",
            "    /** 返回含 Topic 与队列 ID 的调试字符串。 */\n    @Override\n    public String toString() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/GetMinOffsetResponseHeader.java": [
        (
            "public class GetMinOffsetResponseHeader implements CommandCustomHeader {",
            "/**\n * 查询队列最小消费位点的响应头：返回最小位点 offset。\n */\npublic class GetMinOffsetResponseHeader implements CommandCustomHeader {",
        ),
        (
            "    @CFNotNull\n    private Long offset;",
            "    /** 队列最小消费位点。 */\n    @CFNotNull\n    private Long offset;",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
            "    /** 校验响应头字段（空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
        ),
        (
            "    public Long getOffset() {",
            "    /** 返回最小消费位点。 */\n    public Long getOffset() {",
        ),
        (
            "    public void setOffset(Long offset) {",
            "    /** 设置最小消费位点。 */\n    public void setOffset(Long offset) {",
        ),
    ],
}
