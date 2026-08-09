"""Chinese JavaDoc replacements for RocketMQ wave33a remoting protocol header [0:15]."""

R: dict[str, list[tuple[str, str]]] = {
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/NotifyBrokerRoleChangedRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.NOTIFY_BROKER_ROLE_CHANGED, resource = ResourceType.CLUSTER, action = Action.UPDATE)\npublic class NotifyBrokerRoleChangedRequestHeader implements CommandCustomHeader {",
            "/**\n * Broker 角色变更通知请求头：集群内同步新 Master 地址、Epoch 及 BrokerId。\n */\n@RocketMQAction(value = RequestCode.NOTIFY_BROKER_ROLE_CHANGED, resource = ResourceType.CLUSTER, action = Action.UPDATE)\npublic class NotifyBrokerRoleChangedRequestHeader implements CommandCustomHeader {",
        ),
        (
            "    private String masterAddress;",
            "    /** 新 Master Broker 地址。 */\n    private String masterAddress;",
        ),
        (
            "    private Integer masterEpoch;",
            "    /** Master Epoch 版本号。 */\n    private Integer masterEpoch;",
        ),
        (
            "    private Integer syncStateSetEpoch;",
            "    /** 同步状态集 Epoch 版本号。 */\n    private Integer syncStateSetEpoch;",
        ),
        (
            "    private Long masterBrokerId;",
            "    /** 新 Master Broker ID。 */\n    private Long masterBrokerId;",
        ),
        (
            "    public NotifyBrokerRoleChangedRequestHeader() {",
            "    /** 无参构造。 */\n    public NotifyBrokerRoleChangedRequestHeader() {",
        ),
        (
            "    public NotifyBrokerRoleChangedRequestHeader(String masterAddress, Long masterBrokerId, Integer masterEpoch, Integer syncStateSetEpoch) {",
            "    /** 按 Master 地址、BrokerId 及 Epoch 构造。 */\n    public NotifyBrokerRoleChangedRequestHeader(String masterAddress, Long masterBrokerId, Integer masterEpoch, Integer syncStateSetEpoch) {",
        ),
        (
            "    public String getMasterAddress() {",
            "    /** 返回 Master 地址。 */\n    public String getMasterAddress() {",
        ),
        (
            "    public void setMasterAddress(String masterAddress) {",
            "    /** 设置 Master 地址。 */\n    public void setMasterAddress(String masterAddress) {",
        ),
        (
            "    public Integer getMasterEpoch() {",
            "    /** 返回 Master Epoch。 */\n    public Integer getMasterEpoch() {",
        ),
        (
            "    public void setMasterEpoch(Integer masterEpoch) {",
            "    /** 设置 Master Epoch。 */\n    public void setMasterEpoch(Integer masterEpoch) {",
        ),
        (
            "    public Integer getSyncStateSetEpoch() {",
            "    /** 返回同步状态集 Epoch。 */\n    public Integer getSyncStateSetEpoch() {",
        ),
        (
            "    public void setSyncStateSetEpoch(Integer syncStateSetEpoch) {",
            "    /** 设置同步状态集 Epoch。 */\n    public void setSyncStateSetEpoch(Integer syncStateSetEpoch) {",
        ),
        (
            "    public Long getMasterBrokerId() {",
            "    /** 返回 Master Broker ID。 */\n    public Long getMasterBrokerId() {",
        ),
        (
            "    public void setMasterBrokerId(Long masterBrokerId) {",
            "    /** 设置 Master Broker ID。 */\n    public void setMasterBrokerId(Long masterBrokerId) {",
        ),
        (
            "    @Override\n    public String toString() {",
            "    /** 返回含 Master 信息的调试字符串。 */\n    @Override\n    public String toString() {",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n\n    }",
            "    /** 校验请求头字段（空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n\n    }",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/NotifyConsumerIdsChangedRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.NOTIFY_CONSUMER_IDS_CHANGED, action = Action.SUB)\npublic class NotifyConsumerIdsChangedRequestHeader extends RpcRequestHeader {",
            "/**\n * 消费组客户端 ID 变更通知请求头：Broker 通知 Consumer 刷新同组在线成员。\n */\n@RocketMQAction(value = RequestCode.NOTIFY_CONSUMER_IDS_CHANGED, action = Action.SUB)\npublic class NotifyConsumerIdsChangedRequestHeader extends RpcRequestHeader {",
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
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/NotifyMinBrokerIdChangeRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.NOTIFY_MIN_BROKER_ID_CHANGE, resource = ResourceType.CLUSTER, action = Action.UPDATE)\npublic class NotifyMinBrokerIdChangeRequestHeader implements CommandCustomHeader {",
            "/**\n * 最小 BrokerId 变更通知请求头：集群内同步 BrokerId 重分配及 HA 地址信息。\n */\n@RocketMQAction(value = RequestCode.NOTIFY_MIN_BROKER_ID_CHANGE, resource = ResourceType.CLUSTER, action = Action.UPDATE)\npublic class NotifyMinBrokerIdChangeRequestHeader implements CommandCustomHeader {",
        ),
        (
            "    @CFNullable\n    private Long minBrokerId;",
            "    /** 新的最小 BrokerId，可为空。 */\n    @CFNullable\n    private Long minBrokerId;",
        ),
        (
            "    @CFNullable\n    private String brokerName;",
            "    /** Broker 名称，可为空。 */\n    @CFNullable\n    private String brokerName;",
        ),
        (
            "    @CFNullable\n    private String minBrokerAddr;",
            "    /** 持有最小 BrokerId 的 Broker 地址，可为空。 */\n    @CFNullable\n    private String minBrokerAddr;",
        ),
        (
            "    @CFNullable\n    private String offlineBrokerAddr;",
            "    /** 下线 Broker 地址，可为空。 */\n    @CFNullable\n    private String offlineBrokerAddr;",
        ),
        (
            "    @CFNullable\n    private String haBrokerAddr;",
            "    /** HA 同步目标 Broker 地址，可为空。 */\n    @CFNullable\n    private String haBrokerAddr;",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n\n    }",
            "    /** 校验请求头字段（空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n\n    }",
        ),
        (
            "    public Long getMinBrokerId() {",
            "    /** 返回最小 BrokerId。 */\n    public Long getMinBrokerId() {",
        ),
        (
            "    public void setMinBrokerId(Long minBrokerId) {",
            "    /** 设置最小 BrokerId。 */\n    public void setMinBrokerId(Long minBrokerId) {",
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
            "    public String getMinBrokerAddr() {",
            "    /** 返回最小 BrokerId 持有者地址。 */\n    public String getMinBrokerAddr() {",
        ),
        (
            "    public void setMinBrokerAddr(String minBrokerAddr) {",
            "    /** 设置最小 BrokerId 持有者地址。 */\n    public void setMinBrokerAddr(String minBrokerAddr) {",
        ),
        (
            "    public String getOfflineBrokerAddr() {",
            "    /** 返回下线 Broker 地址。 */\n    public String getOfflineBrokerAddr() {",
        ),
        (
            "    public void setOfflineBrokerAddr(String offlineBrokerAddr) {",
            "    /** 设置下线 Broker 地址。 */\n    public void setOfflineBrokerAddr(String offlineBrokerAddr) {",
        ),
        (
            "    public String getHaBrokerAddr() {",
            "    /** 返回 HA Broker 地址。 */\n    public String getHaBrokerAddr() {",
        ),
        (
            "    public void setHaBrokerAddr(String haBrokerAddr) {",
            "    /** 设置 HA Broker 地址。 */\n    public void setHaBrokerAddr(String haBrokerAddr) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/NotifyUnsubscribeLiteRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.NOTIFY_UNSUBSCRIBE_LITE, action = Action.SUB)\npublic class NotifyUnsubscribeLiteRequestHeader extends RpcRequestHeader {",
            "/**\n * Lite Topic 取消订阅通知请求头：Broker 通知 Consumer 退出 Lite 订阅。\n */\n@RocketMQAction(value = RequestCode.NOTIFY_UNSUBSCRIBE_LITE, action = Action.SUB)\npublic class NotifyUnsubscribeLiteRequestHeader extends RpcRequestHeader {",
        ),
        (
            "    @CFNotNull\n    private String liteTopic;",
            "    /** Lite Topic 名称。 */\n    @CFNotNull\n    private String liteTopic;",
        ),
        (
            "    @RocketMQResource(ResourceType.GROUP)\n    @CFNotNull\n    private String consumerGroup;",
            "    /** 目标消费组名称。 */\n    @RocketMQResource(ResourceType.GROUP)\n    @CFNotNull\n    private String consumerGroup;",
        ),
        (
            "    @CFNotNull\n    private String clientId;",
            "    /** 发起取消订阅的 Consumer 客户端 ID。 */\n    @CFNotNull\n    private String clientId;",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
            "    /** 校验请求头字段（空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
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
            "    @Override\n    public String toString() {",
            "    /** 返回含 Lite Topic、消费组与客户端 ID 的调试字符串。 */\n    @Override\n    public String toString() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/PeekMessageRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.PEEK_MESSAGE, action = Action.SUB)\npublic class PeekMessageRequestHeader extends TopicQueueRequestHeader {",
            "/**\n * 消息窥视（Peek）请求头：只读查看队列消息而不消费偏移量。\n */\n@RocketMQAction(value = RequestCode.PEEK_MESSAGE, action = Action.SUB)\npublic class PeekMessageRequestHeader extends TopicQueueRequestHeader {",
        ),
        (
            "    @CFNotNull\n    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
            "    /** 目标 Topic 名称。 */\n    @CFNotNull\n    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
        ),
        (
            "    @CFNotNull\n    private int queueId;",
            "    /** 目标队列 ID。 */\n    @CFNotNull\n    private int queueId;",
        ),
        (
            "    @CFNotNull\n    private int maxMsgNums;",
            "    /** 单次窥视的最大消息条数。 */\n    @CFNotNull\n    private int maxMsgNums;",
        ),
        (
            "    @CFNotNull\n    @RocketMQResource(ResourceType.GROUP)\n    private String consumerGroup;",
            "    /** 消费组名称。 */\n    @CFNotNull\n    @RocketMQResource(ResourceType.GROUP)\n    private String consumerGroup;",
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
            "    public String getTopic() {",
            "    /** 返回 Topic 名称。 */\n    public String getTopic() {",
        ),
        (
            "    public void setTopic(String topic) {",
            "    /** 设置 Topic 名称。 */\n    public void setTopic(String topic) {",
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
            "    public int getMaxMsgNums() {",
            "    /** 返回最大消息条数。 */\n    public int getMaxMsgNums() {",
        ),
        (
            "    public void setMaxMsgNums(int maxMsgNums) {",
            "    /** 设置最大消息条数。 */\n    public void setMaxMsgNums(int maxMsgNums) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/PollingInfoRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.POLLING_INFO, action = Action.GET)\npublic class PollingInfoRequestHeader extends TopicQueueRequestHeader {",
            "/**\n * Pop 长轮询信息查询请求头：查询消费组在指定 Topic 队列上的轮询状态。\n */\n@RocketMQAction(value = RequestCode.POLLING_INFO, action = Action.GET)\npublic class PollingInfoRequestHeader extends TopicQueueRequestHeader {",
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
            "    @CFNotNull\n    private int queueId;",
            "    /** 目标队列 ID，负值表示不限定队列。 */\n    @CFNotNull\n    private int queueId;",
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
            "    public String getTopic() {",
            "    /** 返回 Topic 名称。 */\n    public String getTopic() {",
        ),
        (
            "    public void setTopic(String topic) {",
            "    /** 设置 Topic 名称。 */\n    public void setTopic(String topic) {",
        ),
        (
            "    public Integer getQueueId() {\n        if (queueId < 0) {\n            return -1;\n        }\n        return queueId;\n    }",
            "    /** 返回队列 ID，负值归一化为 -1 表示不限定。 */\n    public Integer getQueueId() {\n        if (queueId < 0) {\n            return -1;\n        }\n        return queueId;\n    }",
        ),
        (
            "    public void setQueueId(Integer queueId) {",
            "    /** 设置队列 ID。 */\n    public void setQueueId(Integer queueId) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/PollingInfoResponseHeader.java": [
        (
            "public class PollingInfoResponseHeader implements CommandCustomHeader {",
            "/**\n * Pop 长轮询信息查询响应头：返回当前轮询中的 Consumer 数量。\n */\npublic class PollingInfoResponseHeader implements CommandCustomHeader {",
        ),
        (
            "    @CFNotNull\n    private int pollingNum;",
            "    /** 当前处于长轮询等待中的 Consumer 数量。 */\n    @CFNotNull\n    private int pollingNum;",
        ),
        (
            "    public int getPollingNum() {",
            "    /** 返回轮询 Consumer 数量。 */\n    public int getPollingNum() {",
        ),
        (
            "    public void setPollingNum(int pollingNum) {",
            "    /** 设置轮询 Consumer 数量。 */\n    public void setPollingNum(int pollingNum) {",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
            "    /** 校验响应头字段（空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/PopLiteMessageRequestHeader.java": [
        (
            "public class PopLiteMessageRequestHeader extends RpcRequestHeader {",
            "/**\n * Lite Pop 消息请求头：轻量级 Pop 消费，指定 Topic 与不可见时间等参数。\n */\npublic class PopLiteMessageRequestHeader extends RpcRequestHeader {",
        ),
        (
            "    @CFNotNull\n    private String clientId;",
            "    /** Consumer 客户端 ID。 */\n    @CFNotNull\n    private String clientId;",
        ),
        (
            "    @CFNotNull\n    @RocketMQResource(ResourceType.GROUP)\n    private String consumerGroup;",
            "    /** 消费组名称。 */\n    @CFNotNull\n    @RocketMQResource(ResourceType.GROUP)\n    private String consumerGroup;",
        ),
        (
            "    @CFNotNull\n    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
            "    /** 目标 Topic 名称。 */\n    @CFNotNull\n    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
        ),
        (
            "    @CFNotNull\n    private int maxMsgNum;",
            "    /** 单次 Pop 的最大消息条数。 */\n    @CFNotNull\n    private int maxMsgNum;",
        ),
        (
            "    @CFNotNull\n    private long invisibleTime;",
            "    /** 消息不可见时长（毫秒）。 */\n    @CFNotNull\n    private long invisibleTime;",
        ),
        (
            "    @CFNotNull\n    private long pollTime;",
            "    /** 长轮询等待时长（毫秒）。 */\n    @CFNotNull\n    private long pollTime;",
        ),
        (
            "    @CFNotNull\n    private long bornTime;",
            "    /** 请求创建时间戳（毫秒）。 */\n    @CFNotNull\n    private long bornTime;",
        ),
        (
            "    private String attemptId;",
            "    /** Pop 尝试 ID，用于幂等与重试追踪。 */\n    private String attemptId;",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n\n    }",
            "    /** 校验请求头字段（空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n\n    }",
        ),
        (
            "    public boolean isTimeoutTooMuch() {\n        return System.currentTimeMillis() - bornTime - pollTime > 500;\n    }",
            "    /** 判断长轮询是否已超时过多（超过 500ms 余量）。 */\n    public boolean isTimeoutTooMuch() {\n        return System.currentTimeMillis() - bornTime - pollTime > 500;\n    }",
        ),
        (
            "    @Override\n    public String toString() {",
            "    /** 返回含 Pop 参数的调试字符串。 */\n    @Override\n    public String toString() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/PopLiteMessageResponseHeader.java": [
        (
            "public class PopLiteMessageResponseHeader implements CommandCustomHeader {",
            "/**\n * Lite Pop 消息响应头：返回 Pop 时间、不可见时长及偏移信息。\n */\npublic class PopLiteMessageResponseHeader implements CommandCustomHeader {",
        ),
        (
            "    @CFNotNull\n    private long popTime;",
            "    /** Pop 操作时间戳（毫秒）。 */\n    @CFNotNull\n    private long popTime;",
        ),
        (
            "    @CFNotNull\n    private long invisibleTime;",
            "    /** 消息不可见时长（毫秒）。 */\n    @CFNotNull\n    private long invisibleTime;",
        ),
        (
            "    @CFNotNull\n    private int reviveQid; // reuse current ack implementation",
            "    /** 复活队列 ID，复用现有 ACK 实现。 */\n    @CFNotNull\n    private int reviveQid;",
        ),
        (
            "    private String startOffsetInfo;",
            "    /** 起始偏移量信息（序列化字符串）。 */\n    private String startOffsetInfo;",
        ),
        (
            "    private String msgOffsetInfo;",
            "    /** 消息偏移量信息（序列化字符串）。 */\n    private String msgOffsetInfo;",
        ),
        (
            "    private String orderCountInfo;",
            "    /** 顺序消费计数信息（序列化字符串）。 */\n    private String orderCountInfo;",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
            "    /** 校验响应头字段（空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/PopMessageRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.POP_MESSAGE, action = Action.SUB)\npublic class PopMessageRequestHeader extends TopicQueueRequestHeader {",
            "/**\n * Pop 消息请求头：长轮询 Pop 消费，支持顺序消费与过滤表达式。\n */\n@RocketMQAction(value = RequestCode.POP_MESSAGE, action = Action.SUB)\npublic class PopMessageRequestHeader extends TopicQueueRequestHeader {",
        ),
        (
            "    @CFNotNull\n    @RocketMQResource(ResourceType.GROUP)\n    private String consumerGroup;",
            "    /** 消费组名称。 */\n    @CFNotNull\n    @RocketMQResource(ResourceType.GROUP)\n    private String consumerGroup;",
        ),
        (
            "    @CFNotNull\n    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
            "    /** 目标 Topic 名称。 */\n    @CFNotNull\n    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
        ),
        (
            "    @CFNotNull\n    private int queueId;",
            "    /** 目标队列 ID。 */\n    @CFNotNull\n    private int queueId;",
        ),
        (
            "    @CFNotNull\n    private int maxMsgNums;",
            "    /** 单次 Pop 的最大消息条数。 */\n    @CFNotNull\n    private int maxMsgNums;",
        ),
        (
            "    @CFNotNull\n    private long invisibleTime;",
            "    /** 消息不可见时长（毫秒）。 */\n    @CFNotNull\n    private long invisibleTime;",
        ),
        (
            "    @CFNotNull\n    private long pollTime;",
            "    /** 长轮询等待时长（毫秒）。 */\n    @CFNotNull\n    private long pollTime;",
        ),
        (
            "    @CFNotNull\n    private long bornTime;",
            "    /** 请求创建时间戳（毫秒）。 */\n    @CFNotNull\n    private long bornTime;",
        ),
        (
            "    @CFNotNull\n    private int initMode;",
            "    /** Pop 初始化模式。 */\n    @CFNotNull\n    private int initMode;",
        ),
        (
            "    private String expType;",
            "    /** 过滤表达式类型。 */\n    private String expType;",
        ),
        (
            "    private String exp;",
            "    /** 过滤表达式内容。 */\n    private String exp;",
        ),
        (
            "    /**\n     * marked as order consume, if true\n     * 1. not commit offset\n     * 2. not pop retry, because no retry\n     * 3. not append check point, because no retry\n     */\n    private Boolean order = Boolean.FALSE;",
            "    /**\n     * 标记为顺序消费；为 true 时：\n     * 1. 不提交消费偏移量\n     * 2. 不进行 Pop 重试（无重试机制）\n     * 3. 不追加检查点（无重试机制）\n     */\n    private Boolean order = Boolean.FALSE;",
        ),
        (
            "    private String attemptId;",
            "    /** Pop 尝试 ID，用于幂等与重试追踪。 */\n    private String attemptId;",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
            "    /** 校验请求头字段（空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
        ),
        (
            "    public Integer getQueueId() {\n        if (queueId < 0) {\n            return -1;\n        }\n        return queueId;\n    }",
            "    /** 返回队列 ID，负值归一化为 -1 表示不限定。 */\n    public Integer getQueueId() {\n        if (queueId < 0) {\n            return -1;\n        }\n        return queueId;\n    }",
        ),
        (
            "    public boolean isTimeoutTooMuch() {\n        return System.currentTimeMillis() - bornTime - pollTime > 500;\n    }",
            "    /** 判断长轮询是否已超时过多（超过 500ms 余量）。 */\n    public boolean isTimeoutTooMuch() {\n        return System.currentTimeMillis() - bornTime - pollTime > 500;\n    }",
        ),
        (
            "    public boolean isOrder() {\n        return this.order != null && this.order.booleanValue();\n    }",
            "    /** 返回是否为顺序消费模式。 */\n    public boolean isOrder() {\n        return this.order != null && this.order.booleanValue();\n    }",
        ),
        (
            "    @Override\n    public String toString() {",
            "    /** 返回含 Pop 参数的调试字符串。 */\n    @Override\n    public String toString() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/PopMessageResponseHeader.java": [
        (
            "public class PopMessageResponseHeader implements CommandCustomHeader {",
            "/**\n * Pop 消息响应头：返回 Pop 时间、不可见时长、队列剩余消息数及偏移信息。\n */\npublic class PopMessageResponseHeader implements CommandCustomHeader {",
        ),
        (
            "    @CFNotNull\n    private long popTime;",
            "    /** Pop 操作时间戳（毫秒）。 */\n    @CFNotNull\n    private long popTime;",
        ),
        (
            "    @CFNotNull\n    private long invisibleTime;",
            "    /** 消息不可见时长（毫秒）。 */\n    @CFNotNull\n    private long invisibleTime;",
        ),
        (
            "    @CFNotNull\n    private int reviveQid;",
            "    /** 复活队列 ID。 */\n    @CFNotNull\n    private int reviveQid;",
        ),
        (
            "    /**\n     * the rest num in queue\n     */\n    @CFNotNull\n    private long restNum;",
            "    /** 队列中剩余可 Pop 消息数。 */\n    @CFNotNull\n    private long restNum;",
        ),
        (
            "    private String startOffsetInfo;",
            "    /** 起始偏移量信息（序列化字符串）。 */\n    private String startOffsetInfo;",
        ),
        (
            "    private String msgOffsetInfo;",
            "    /** 消息偏移量信息（序列化字符串）。 */\n    private String msgOffsetInfo;",
        ),
        (
            "    private String orderCountInfo;",
            "    /** 顺序消费计数信息（序列化字符串）。 */\n    private String orderCountInfo;",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
            "    /** 校验响应头字段（空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/PullMessageRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.PULL_MESSAGE, action = Action.SUB)\npublic class PullMessageRequestHeader extends TopicQueueRequestHeader implements FastCodesHeader {",
            "/**\n * 拉取消息请求头：指定 Topic、队列偏移、过滤条件及长轮询参数。\n */\n@RocketMQAction(value = RequestCode.PULL_MESSAGE, action = Action.SUB)\npublic class PullMessageRequestHeader extends TopicQueueRequestHeader implements FastCodesHeader {",
        ),
        (
            "    @CFNotNull\n    @RocketMQResource(ResourceType.GROUP)\n    private String consumerGroup;",
            "    /** 消费组名称。 */\n    @CFNotNull\n    @RocketMQResource(ResourceType.GROUP)\n    private String consumerGroup;",
        ),
        (
            "    @CFNotNull\n    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
            "    /** 目标 Topic 名称。 */\n    @CFNotNull\n    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
        ),
        (
            "    private String liteTopic;",
            "    /** Lite Topic 名称，可选。 */\n    private String liteTopic;",
        ),
        (
            "    @CFNotNull\n    private Integer queueId;",
            "    /** 目标队列 ID。 */\n    @CFNotNull\n    private Integer queueId;",
        ),
        (
            "    @CFNotNull\n    private Long queueOffset;",
            "    /** 拉取起始队列偏移量。 */\n    @CFNotNull\n    private Long queueOffset;",
        ),
        (
            "    @CFNotNull\n    private Integer maxMsgNums;",
            "    /** 单次拉取的最大消息条数。 */\n    @CFNotNull\n    private Integer maxMsgNums;",
        ),
        (
            "    @CFNotNull\n    private Integer sysFlag;",
            "    /** 系统标志位（压缩、事务等）。 */\n    @CFNotNull\n    private Integer sysFlag;",
        ),
        (
            "    @CFNotNull\n    private Long commitOffset;",
            "    /** 已提交的消费偏移量。 */\n    @CFNotNull\n    private Long commitOffset;",
        ),
        (
            "    @CFNotNull\n    private Long suspendTimeoutMillis;",
            "    /** 长轮询挂起超时（毫秒）。 */\n    @CFNotNull\n    private Long suspendTimeoutMillis;",
        ),
        (
            "    @CFNullable\n    private String subscription;",
            "    /** 订阅过滤表达式，可为空。 */\n    @CFNullable\n    private String subscription;",
        ),
        (
            "    @CFNotNull\n    private Long subVersion;",
            "    /** 订阅版本号。 */\n    @CFNotNull\n    private Long subVersion;",
        ),
        (
            "    private String expressionType;",
            "    /** 过滤表达式类型（如 TAG、SQL92）。 */\n    private String expressionType;",
        ),
        (
            "    @CFNullable\n    private Integer maxMsgBytes;",
            "    /** 单条消息最大字节数限制，可为空。 */\n    @CFNullable\n    private Integer maxMsgBytes;",
        ),
        (
            "    /**\n     * mark the source of this pull request\n     */\n    private Integer requestSource;",
            "    /** 标识本次拉取请求的来源。 */\n    private Integer requestSource;",
        ),
        (
            "    /**\n     * the real clientId when request from proxy\n     */\n    private String proxyFrowardClientId;",
            "    /** 经 Proxy 转发时的真实客户端 ID。 */\n    private String proxyFrowardClientId;",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
            "    /** 校验请求头字段（空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
        ),
        (
            "    @Override\n    public void encode(ByteBuf out) {",
            "    /** 将请求头字段编码写入 ByteBuf。 */\n    @Override\n    public void encode(ByteBuf out) {",
        ),
        (
            "    @Override\n    public void decode(HashMap<String, String> fields) throws RemotingCommandException {",
            "    /** 从字段映射解码并填充请求头。 */\n    @Override\n    public void decode(HashMap<String, String> fields) throws RemotingCommandException {",
        ),
        (
            "    @Override\n    public String toString() {",
            "    /** 返回含拉取参数的调试字符串。 */\n    @Override\n    public String toString() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/PullMessageResponseHeader.java": [
        (
            "public class PullMessageResponseHeader implements CommandCustomHeader, FastCodesHeader {",
            "/**\n * 拉取消息响应头：返回下次拉取偏移、队列边界及 Broker 建议信息。\n */\npublic class PullMessageResponseHeader implements CommandCustomHeader, FastCodesHeader {",
        ),
        (
            "    @CFNotNull\n    private Long suggestWhichBrokerId;",
            "    /** 建议下次拉取的 Broker ID。 */\n    @CFNotNull\n    private Long suggestWhichBrokerId;",
        ),
        (
            "    @CFNotNull\n    private Long nextBeginOffset;",
            "    /** 下次拉取起始偏移量。 */\n    @CFNotNull\n    private Long nextBeginOffset;",
        ),
        (
            "    @CFNotNull\n    private Long minOffset;",
            "    /** 队列最小偏移量。 */\n    @CFNotNull\n    private Long minOffset;",
        ),
        (
            "    @CFNotNull\n    private Long maxOffset;",
            "    /** 队列最大偏移量。 */\n    @CFNotNull\n    private Long maxOffset;",
        ),
        (
            "    @CFNullable\n    private Long offsetDelta;",
            "    /** 偏移量增量，可为空。 */\n    @CFNullable\n    private Long offsetDelta;",
        ),
        (
            "    @CFNullable\n    private Integer topicSysFlag;",
            "    /** Topic 系统标志位，可为空。 */\n    @CFNullable\n    private Integer topicSysFlag;",
        ),
        (
            "    @CFNullable\n    private Integer groupSysFlag;",
            "    /** 消费组系统标志位，可为空。 */\n    @CFNullable\n    private Integer groupSysFlag;",
        ),
        (
            "    @CFNullable\n    private Integer forbiddenType;",
            "    /** 禁止拉取类型，可为空。 */\n    @CFNullable\n    private Integer forbiddenType;",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
            "    /** 校验响应头字段（空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
        ),
        (
            "    @Override\n    public void encode(ByteBuf out) {",
            "    /** 将响应头字段编码写入 ByteBuf。 */\n    @Override\n    public void encode(ByteBuf out) {",
        ),
        (
            "    @Override\n    public void decode(HashMap<String, String> fields) throws RemotingCommandException {",
            "    /** 从字段映射解码并填充响应头。 */\n    @Override\n    public void decode(HashMap<String, String> fields) throws RemotingCommandException {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/QueryConsumeQueueRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.QUERY_CONSUME_QUEUE, action = Action.GET)\npublic class QueryConsumeQueueRequestHeader extends TopicQueueRequestHeader {",
            "/**\n * 查询 ConsumeQueue 索引的请求头：按 Topic、队列及起始索引分页读取。\n */\n@RocketMQAction(value = RequestCode.QUERY_CONSUME_QUEUE, action = Action.GET)\npublic class QueryConsumeQueueRequestHeader extends TopicQueueRequestHeader {",
        ),
        (
            "    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
            "    /** 目标 Topic 名称。 */\n    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
        ),
        (
            "    private int queueId;",
            "    /** 目标队列 ID。 */\n    private int queueId;",
        ),
        (
            "    private long index;",
            "    /** ConsumeQueue 起始索引位置。 */\n    private long index;",
        ),
        (
            "    private int count;",
            "    /** 读取条目数量。 */\n    private int count;",
        ),
        (
            "    @RocketMQResource(ResourceType.GROUP)\n    private String consumerGroup;",
            "    /** 消费组名称。 */\n    @RocketMQResource(ResourceType.GROUP)\n    private String consumerGroup;",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n\n    }",
            "    /** 校验请求头字段（空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n\n    }",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/QueryConsumeTimeSpanRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.QUERY_CONSUME_TIME_SPAN, action = Action.GET)\npublic class QueryConsumeTimeSpanRequestHeader extends TopicRequestHeader {",
            "/**\n * 查询消费时间跨度请求头：按 Topic 与消费组获取消费进度时间范围。\n */\n@RocketMQAction(value = RequestCode.QUERY_CONSUME_TIME_SPAN, action = Action.GET)\npublic class QueryConsumeTimeSpanRequestHeader extends TopicRequestHeader {",
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
    ],
}
