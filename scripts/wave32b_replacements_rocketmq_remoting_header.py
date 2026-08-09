"""Chinese JavaDoc replacements for RocketMQ wave32b remoting protocol header [15:30]."""

R: dict[str, list[tuple[str, str]]] = {
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/GetParentTopicInfoRequestHeader.java": [
        (
            "public class GetParentTopicInfoRequestHeader implements CommandCustomHeader {",
            "/**\n * 查询父 Topic 信息的请求头：按 Topic 名称获取 Lite Topic 对应的父 Topic 元数据。\n */\npublic class GetParentTopicInfoRequestHeader implements CommandCustomHeader {",
        ),
        (
            "    @CFNotNull\n    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
            "    /** 目标 Topic 名称。 */\n    @CFNotNull\n    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {",
            "    /** 校验请求头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {",
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
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/GetProducerConnectionListRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.GET_PRODUCER_CONNECTION_LIST, resource = ResourceType.CLUSTER, action = Action.GET)\npublic class GetProducerConnectionListRequestHeader extends RpcRequestHeader {",
            "/**\n * 查询生产者连接列表的请求头：按生产者组名获取当前在线 Producer 客户端连接。\n */\n@RocketMQAction(value = RequestCode.GET_PRODUCER_CONNECTION_LIST, resource = ResourceType.CLUSTER, action = Action.GET)\npublic class GetProducerConnectionListRequestHeader extends RpcRequestHeader {",
        ),
        (
            "    @CFNotNull\n    private String producerGroup;",
            "    /** 生产者组名称。 */\n    @CFNotNull\n    private String producerGroup;",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n        // To change body of implemented methods use File | Settings | File\n        // Templates.\n    }",
            "    /** 校验请求头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n        // To change body of implemented methods use File | Settings | File\n        // Templates.\n    }",
        ),
        (
            "    public String getProducerGroup() {",
            "    /** 返回生产者组名称。 */\n    public String getProducerGroup() {",
        ),
        (
            "    public void setProducerGroup(String producerGroup) {",
            "    /** 设置生产者组名称。 */\n    public void setProducerGroup(String producerGroup) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/GetSubscriptionGroupConfigRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.GET_SUBSCRIPTIONGROUP_CONFIG, action = Action.GET)\npublic class GetSubscriptionGroupConfigRequestHeader extends RpcRequestHeader {",
            "/**\n * 获取订阅组配置的请求头：按消费组名拉取 Broker 上持久化的订阅组配置。\n */\n@RocketMQAction(value = RequestCode.GET_SUBSCRIPTIONGROUP_CONFIG, action = Action.GET)\npublic class GetSubscriptionGroupConfigRequestHeader extends RpcRequestHeader {",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
            "    /** 校验请求头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
        ),
        (
            "    @CFNotNull\n    @RocketMQResource(ResourceType.GROUP)\n    private String group;",
            "    /** 订阅组（消费组）名称。 */\n    @CFNotNull\n    @RocketMQResource(ResourceType.GROUP)\n    private String group;",
        ),
        (
            "    /**\n     * @return the group\n     */\n    public String getGroup() {",
            "    /** 返回订阅组名称。 */\n    public String getGroup() {",
        ),
        (
            "    /**\n     * @param group the group to set\n     */\n    public void setGroup(String group) {",
            "    /** 设置订阅组名称。 */\n    public void setGroup(String group) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/GetTopicConfigRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.GET_TOPIC_CONFIG, action = Action.GET)\npublic class GetTopicConfigRequestHeader extends TopicRequestHeader {",
            "/**\n * 获取 Topic 配置的请求头：按 Topic 名称查询 Broker 上 Topic 路由与属性配置。\n */\n@RocketMQAction(value = RequestCode.GET_TOPIC_CONFIG, action = Action.GET)\npublic class GetTopicConfigRequestHeader extends TopicRequestHeader {",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
            "    /** 校验请求头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
        ),
        (
            "    @CFNotNull\n    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
            "    /** 目标 Topic 名称。 */\n    @CFNotNull\n    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
        ),
        (
            "    /**\n     * @return the topic\n     */\n    public String getTopic() {",
            "    /** 返回 Topic 名称。 */\n    public String getTopic() {",
        ),
        (
            "    /**\n     * @param topic the topic to set\n     */\n    public void setTopic(String topic) {",
            "    /** 设置 Topic 名称。 */\n    public void setTopic(String topic) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/GetTopicStatsInfoRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.GET_TOPIC_STATS_INFO, action = Action.GET)\npublic class GetTopicStatsInfoRequestHeader extends TopicRequestHeader {",
            "/**\n * 获取 Topic 统计信息的请求头：查询指定 Topic 的消息生产与消费 TPS 等运行时指标。\n */\n@RocketMQAction(value = RequestCode.GET_TOPIC_STATS_INFO, action = Action.GET)\npublic class GetTopicStatsInfoRequestHeader extends TopicRequestHeader {",
        ),
        (
            "    @CFNotNull\n    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
            "    /** 目标 Topic 名称。 */\n    @CFNotNull\n    @RocketMQResource(ResourceType.TOPIC)\n    private String topic;",
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
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/GetTopicsByClusterRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.GET_TOPICS_BY_CLUSTER, resource = ResourceType.TOPIC, action = Action.LIST)\npublic class GetTopicsByClusterRequestHeader implements CommandCustomHeader {",
            "/**\n * 按集群列举 Topic 的请求头：指定集群名称，返回该集群下所有 Topic 列表。\n */\n@RocketMQAction(value = RequestCode.GET_TOPICS_BY_CLUSTER, resource = ResourceType.TOPIC, action = Action.LIST)\npublic class GetTopicsByClusterRequestHeader implements CommandCustomHeader {",
        ),
        (
            "    @CFNotNull\n    private String cluster;",
            "    /** 集群名称。 */\n    @CFNotNull\n    private String cluster;",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
            "    /** 校验请求头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {\n    }",
        ),
        (
            "    public String getCluster() {",
            "    /** 返回集群名称。 */\n    public String getCluster() {",
        ),
        (
            "    public void setCluster(String cluster) {",
            "    /** 设置集群名称。 */\n    public void setCluster(String cluster) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/GetUserRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.AUTH_GET_USER, resource = ResourceType.CLUSTER, action = Action.GET)\npublic class GetUserRequestHeader implements CommandCustomHeader {",
            "/**\n * 查询 ACL 用户的请求头：按用户名获取 RocketMQ 访问控制用户详情。\n */\n@RocketMQAction(value = RequestCode.AUTH_GET_USER, resource = ResourceType.CLUSTER, action = Action.GET)\npublic class GetUserRequestHeader implements CommandCustomHeader {",
        ),
        (
            "    private String username;",
            "    /** ACL 用户名。 */\n    private String username;",
        ),
        (
            "    public GetUserRequestHeader() {",
            "    /** 无参构造。 */\n    public GetUserRequestHeader() {",
        ),
        (
            "    public GetUserRequestHeader(String username) {",
            "    /** 以用户名构造请求头。 */\n    public GetUserRequestHeader(String username) {",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {",
            "    /** 校验请求头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {",
        ),
        (
            "    public String getUsername() {",
            "    /** 返回 ACL 用户名。 */\n    public String getUsername() {",
        ),
        (
            "    public void setUsername(String username) {",
            "    /** 设置 ACL 用户名。 */\n    public void setUsername(String username) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/HeartbeatRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.HEART_BEAT, resource = ResourceType.GROUP, action = {Action.PUB, Action.SUB})\npublic class HeartbeatRequestHeader extends RpcRequestHeader {",
            "/**\n * 客户端心跳请求头：Producer/Consumer 定期上报存活状态，维持与 Broker 的连接注册。\n * 字段由 {@link RpcRequestHeader} 基类承载（含命名空间等）。\n */\n@RocketMQAction(value = RequestCode.HEART_BEAT, resource = ResourceType.GROUP, action = {Action.PUB, Action.SUB})\npublic class HeartbeatRequestHeader extends RpcRequestHeader {",
        ),
        (
            "    // for namespace\n    @Override\n    public void checkFields() throws RemotingCommandException {",
            "    /** 校验请求头字段（心跳无附加字段，空实现）。 */\n    // for namespace\n    @Override\n    public void checkFields() throws RemotingCommandException {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/InitConsumerOffsetRequestHeader.java": [
        (
            "public class InitConsumerOffsetRequestHeader extends TopicRequestHeader {",
            "/**\n * 初始化消费位点的请求头：为指定 Topic 设置消费组起始消费进度。\n * initMode 取值参见 {@code ConsumeInitMode}。\n */\npublic class InitConsumerOffsetRequestHeader extends TopicRequestHeader {",
        ),
        (
            "    private String topic;",
            "    /** 目标 Topic 名称。 */\n    private String topic;",
        ),
        (
            "    // @see ConsumeInitMode\n    private int initMode;",
            "    /** 消费位点初始化模式，参见 {@code ConsumeInitMode}。 */\n    private int initMode;",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {",
            "    /** 校验请求头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {",
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
            "    public int getInitMode() {",
            "    /** 返回消费位点初始化模式。 */\n    public int getInitMode() {",
        ),
        (
            "    public void setInitMode(int initMode) {",
            "    /** 设置消费位点初始化模式。 */\n    public void setInitMode(int initMode) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/ListAclsRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.AUTH_LIST_ACL, resource = ResourceType.CLUSTER, action = Action.GET)\npublic class ListAclsRequestHeader implements CommandCustomHeader {",
            "/**\n * 列举 ACL 规则的请求头：支持按主体（用户/角色）与资源类型过滤。\n */\n@RocketMQAction(value = RequestCode.AUTH_LIST_ACL, resource = ResourceType.CLUSTER, action = Action.GET)\npublic class ListAclsRequestHeader implements CommandCustomHeader {",
        ),
        (
            "    private String subjectFilter;",
            "    /** 主体过滤条件（用户名或角色），可为空表示不过滤。 */\n    private String subjectFilter;",
        ),
        (
            "    private String resourceFilter;",
            "    /** 资源过滤条件（Topic/Group/Cluster 等），可为空表示不过滤。 */\n    private String resourceFilter;",
        ),
        (
            "    public ListAclsRequestHeader() {",
            "    /** 无参构造。 */\n    public ListAclsRequestHeader() {",
        ),
        (
            "    public ListAclsRequestHeader(String subjectFilter, String resourceFilter) {",
            "    /** 以主体与资源过滤条件构造请求头。 */\n    public ListAclsRequestHeader(String subjectFilter, String resourceFilter) {",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {",
            "    /** 校验请求头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {",
        ),
        (
            "    public String getSubjectFilter() {",
            "    /** 返回主体过滤条件。 */\n    public String getSubjectFilter() {",
        ),
        (
            "    public void setSubjectFilter(String subjectFilter) {",
            "    /** 设置主体过滤条件。 */\n    public void setSubjectFilter(String subjectFilter) {",
        ),
        (
            "    public String getResourceFilter() {",
            "    /** 返回资源过滤条件。 */\n    public String getResourceFilter() {",
        ),
        (
            "    public void setResourceFilter(String resourceFilter) {",
            "    /** 设置资源过滤条件。 */\n    public void setResourceFilter(String resourceFilter) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/ListUsersRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.AUTH_LIST_USER, resource = ResourceType.CLUSTER, action = Action.GET)\npublic class ListUsersRequestHeader implements CommandCustomHeader {",
            "/**\n * 列举 ACL 用户的请求头：支持按用户名前缀或模式过滤。\n */\n@RocketMQAction(value = RequestCode.AUTH_LIST_USER, resource = ResourceType.CLUSTER, action = Action.GET)\npublic class ListUsersRequestHeader implements CommandCustomHeader {",
        ),
        (
            "    private String filter;",
            "    /** 用户名过滤条件，可为空表示列举全部用户。 */\n    private String filter;",
        ),
        (
            "    public ListUsersRequestHeader() {",
            "    /** 无参构造。 */\n    public ListUsersRequestHeader() {",
        ),
        (
            "    public ListUsersRequestHeader(String filter) {",
            "    /** 以过滤条件构造请求头。 */\n    public ListUsersRequestHeader(String filter) {",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {",
            "    /** 校验请求头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {",
        ),
        (
            "    public String getFilter() {",
            "    /** 返回用户名过滤条件。 */\n    public String getFilter() {",
        ),
        (
            "    public void setFilter(String filter) {",
            "    /** 设置用户名过滤条件。 */\n    public void setFilter(String filter) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/LiteSubscriptionCtlRequestHeader.java": [
        (
            "public class LiteSubscriptionCtlRequestHeader extends RpcRequestHeader {",
            "/**\n * Lite 订阅控制请求头：管理 Pop 模式下 Lite 消费组的订阅关系。\n * 具体参数由 RPC 请求体承载，本头类无附加字段。\n */\npublic class LiteSubscriptionCtlRequestHeader extends RpcRequestHeader {",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {",
            "    /** 校验请求头字段（本类无附加字段，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/LockBatchMqRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.LOCK_BATCH_MQ, action = Action.SUB)\npublic class LockBatchMqRequestHeader extends RpcRequestHeader {",
            "/**\n * 批量锁定消息队列的请求头：Pop 消费模式下 Consumer 批量申请队列消息锁。\n * 锁定详情由 RPC 请求体携带，本头类无附加字段。\n */\n@RocketMQAction(value = RequestCode.LOCK_BATCH_MQ, action = Action.SUB)\npublic class LockBatchMqRequestHeader extends RpcRequestHeader {",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {",
            "    /** 校验请求头字段（本类无附加字段，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/NotificationRequestHeader.java": [
        (
            "@RocketMQAction(value = RequestCode.NOTIFICATION, action = Action.SUB)\npublic class NotificationRequestHeader extends TopicQueueRequestHeader {",
            "/**\n * Pop 长轮询通知请求头：Consumer 向 Broker 注册对指定 Topic/Queue 的消息到达通知。\n * Broker 在有新消息或超时时通过 {@link NotificationResponseHeader} 响应。\n */\n@RocketMQAction(value = RequestCode.NOTIFICATION, action = Action.SUB)\npublic class NotificationRequestHeader extends TopicQueueRequestHeader {",
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
            "    /** 消息队列 ID。 */\n    @CFNotNull\n    private int queueId;",
        ),
        (
            "    @CFNotNull\n    private long pollTime;",
            "    /** 长轮询超时时间（毫秒）。 */\n    @CFNotNull\n    private long pollTime;",
        ),
        (
            "    @CFNotNull\n    private long bornTime;",
            "    /** 请求创建时间戳（毫秒）。 */\n    @CFNotNull\n    private long bornTime;",
        ),
        (
            "    private Boolean order = Boolean.FALSE;",
            "    /** 是否顺序消费，默认 false。 */\n    private Boolean order = Boolean.FALSE;",
        ),
        (
            "    private String attemptId;",
            "    /** Pop 消费尝试 ID，用于幂等与追踪。 */\n    private String attemptId;",
        ),
        (
            "    private String expType;",
            "    /** 消息过滤表达式类型（如 TAG、SQL92）。 */\n    private String expType;",
        ),
        (
            "    private String exp;",
            "    /** 消息过滤表达式内容。 */\n    private String exp;",
        ),
        (
            "    @CFNotNull\n    @Override\n    public void checkFields() throws RemotingCommandException {",
            "    /** 校验请求头必填字段（由框架注解驱动，空实现）。 */\n    @CFNotNull\n    @Override\n    public void checkFields() throws RemotingCommandException {",
        ),
        (
            "    public long getPollTime() {",
            "    /** 返回长轮询超时时间。 */\n    public long getPollTime() {",
        ),
        (
            "    public void setPollTime(long pollTime) {",
            "    /** 设置长轮询超时时间。 */\n    public void setPollTime(long pollTime) {",
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
            "    public long getBornTime() {",
            "    /** 返回请求创建时间戳。 */\n    public long getBornTime() {",
        ),
        (
            "    public void setBornTime(long bornTime) {",
            "    /** 设置请求创建时间戳。 */\n    public void setBornTime(long bornTime) {",
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
            "    /** 返回队列 ID；负值归一化为 -1。 */\n    public Integer getQueueId() {",
        ),
        (
            "    public void setQueueId(Integer queueId) {",
            "    /** 设置队列 ID。 */\n    public void setQueueId(Integer queueId) {",
        ),
        (
            "    public Boolean getOrder() {",
            "    /** 返回是否顺序消费。 */\n    public Boolean getOrder() {",
        ),
        (
            "    public void setOrder(Boolean order) {",
            "    /** 设置是否顺序消费。 */\n    public void setOrder(Boolean order) {",
        ),
        (
            "    public String getAttemptId() {",
            "    /** 返回 Pop 尝试 ID。 */\n    public String getAttemptId() {",
        ),
        (
            "    public void setAttemptId(String attemptId) {",
            "    /** 设置 Pop 尝试 ID。 */\n    public void setAttemptId(String attemptId) {",
        ),
        (
            "    public String getExpType() {",
            "    /** 返回过滤表达式类型。 */\n    public String getExpType() {",
        ),
        (
            "    public void setExpType(String expType) {",
            "    /** 设置过滤表达式类型。 */\n    public void setExpType(String expType) {",
        ),
        (
            "    public String getExp() {",
            "    /** 返回过滤表达式内容。 */\n    public String getExp() {",
        ),
        (
            "    public void setExp(String exp) {",
            "    /** 设置过滤表达式内容。 */\n    public void setExp(String exp) {",
        ),
        (
            "    @Override\n    public String toString() {",
            "    /** 返回含消费组、Topic、队列及轮询参数的调试字符串。 */\n    @Override\n    public String toString() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/header/NotificationResponseHeader.java": [
        (
            "public class NotificationResponseHeader implements CommandCustomHeader {",
            "/**\n * Pop 长轮询通知响应头：告知 Consumer 队列是否有新消息及轮询队列是否已满。\n */\npublic class NotificationResponseHeader implements CommandCustomHeader {",
        ),
        (
            "    @CFNotNull\n    private boolean hasMsg = false;",
            "    /** 队列是否有新消息可拉取。 */\n    @CFNotNull\n    private boolean hasMsg = false;",
        ),
        (
            "    private boolean pollingFull = false;",
            "    /** 长轮询等待队列是否已满（Broker 侧背压信号）。 */\n    private boolean pollingFull = false;",
        ),
        (
            "    public boolean isHasMsg() {",
            "    /** 返回是否有新消息。 */\n    public boolean isHasMsg() {",
        ),
        (
            "    public boolean isPollingFull() {",
            "    /** 返回轮询队列是否已满。 */\n    public boolean isPollingFull() {",
        ),
        (
            "    public void setPollingFull(boolean pollingFull) {",
            "    /** 设置轮询队列是否已满。 */\n    public void setPollingFull(boolean pollingFull) {",
        ),
        (
            "    public void setHasMsg(boolean hasMsg) {",
            "    /** 设置是否有新消息。 */\n    public void setHasMsg(boolean hasMsg) {",
        ),
        (
            "    @Override\n    public void checkFields() throws RemotingCommandException {",
            "    /** 校验响应头字段（本类无额外约束，空实现）。 */\n    @Override\n    public void checkFields() throws RemotingCommandException {",
        ),
    ],
}
