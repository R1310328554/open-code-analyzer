"""Chinese JavaDoc replacements for RocketMQ wave36b remoting subscription/topic/rpc [15:30]."""

R: dict[str, list[tuple[str, str]]] = {
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/subscription/SimpleSubscriptionData.java": [
        (
            "public class SimpleSubscriptionData {",
            "/**\n * 精简订阅数据：描述单个 Topic 的订阅表达式、类型与版本号。\n */\npublic class SimpleSubscriptionData {",
        ),
        (
            "    private String topic;",
            "    /** 订阅 Topic 名称。 */\n    private String topic;",
        ),
        (
            "    private String expressionType;",
            "    /** 过滤表达式类型（如 TAG、SQL92）。 */\n    private String expressionType;",
        ),
        (
            "    private String expression;",
            "    /** 订阅过滤表达式内容。 */\n    private String expression;",
        ),
        (
            "    private long version;",
            "    /** 订阅数据版本号，用于增量同步。 */\n    private long version;",
        ),
        (
            "    public SimpleSubscriptionData(String topic, String expressionType, String expression, long version) {",
            "    /** 构造精简订阅数据。 */\n    public SimpleSubscriptionData(String topic, String expressionType, String expression, long version) {",
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
            "    public String getExpressionType() {",
            "    /** 返回表达式类型。 */\n    public String getExpressionType() {",
        ),
        (
            "    public void setExpressionType(String expressionType) {",
            "    /** 设置表达式类型。 */\n    public void setExpressionType(String expressionType) {",
        ),
        (
            "    public String getExpression() {",
            "    /** 返回过滤表达式。 */\n    public String getExpression() {",
        ),
        (
            "    public void setExpression(String expression) {",
            "    /** 设置过滤表达式。 */\n    public void setExpression(String expression) {",
        ),
        (
            "    public long getVersion() {",
            "    /** 返回订阅版本号。 */\n    public long getVersion() {",
        ),
        (
            "    public void setVersion(long version) {",
            "    /** 设置订阅版本号。 */\n    public void setVersion(long version) {",
        ),
        (
            "    @Override\n    public boolean equals(Object o) {",
            "    /** 按 topic、表达式类型与表达式比较相等性（不含 version）。 */\n    @Override\n    public boolean equals(Object o) {",
        ),
        (
            "    @Override\n    public int hashCode() {",
            "    /** 计算哈希（不含 version）。 */\n    @Override\n    public int hashCode() {",
        ),
        (
            "    @Override public String toString() {",
            "    /** 返回调试字符串。 */\n    @Override public String toString() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/subscription/SubscriptionGroupConfig.java": [
        (
            "public class SubscriptionGroupConfig {",
            "/**\n * 订阅组（消费组）配置：控制消费开关、重试、广播/顺序及 Lite 订阅等 Broker 侧策略。\n */\npublic class SubscriptionGroupConfig {",
        ),
        (
            "    private String groupName;",
            "    /** 消费组名称。 */\n    private String groupName;",
        ),
        (
            "    private boolean consumeEnable = true;",
            "    /** 是否允许该组消费。 */\n    private boolean consumeEnable = true;",
        ),
        (
            "    private boolean consumeFromMinEnable = true;",
            "    /** 是否从最小 offset 开始消费。 */\n    private boolean consumeFromMinEnable = true;",
        ),
        (
            "    private boolean consumeBroadcastEnable = true;",
            "    /** 是否允许广播消费。 */\n    private boolean consumeBroadcastEnable = true;",
        ),
        (
            "    private boolean consumeMessageOrderly = false;",
            "    /** 是否顺序消费。 */\n    private boolean consumeMessageOrderly = false;",
        ),
        (
            "    private int retryQueueNums = 1;",
            "    /** 重试 Topic 队列数量。 */\n    private int retryQueueNums = 1;",
        ),
        (
            "    private int retryMaxTimes = 16;",
            "    /** 最大重试次数。 */\n    private int retryMaxTimes = 16;",
        ),
        (
            "    private GroupRetryPolicy groupRetryPolicy = new GroupRetryPolicy();",
            "    /** 组级重试策略。 */\n    private GroupRetryPolicy groupRetryPolicy = new GroupRetryPolicy();",
        ),
        (
            "    private long brokerId = MixAll.MASTER_ID;",
            "    /** 关联 Broker 实例 ID（默认 Master）。 */\n    private long brokerId = MixAll.MASTER_ID;",
        ),
        (
            "    private long whichBrokerWhenConsumeSlowly = 1;",
            "    /** 消费过慢时路由到的 Broker ID。 */\n    private long whichBrokerWhenConsumeSlowly = 1;",
        ),
        (
            "    private boolean notifyConsumerIdsChangedEnable = true;",
            "    /** 是否在 Consumer 实例变更时通知客户端。 */\n    private boolean notifyConsumerIdsChangedEnable = true;",
        ),
        (
            "    private int groupSysFlag = 0;",
            "    /** 订阅组系统标志位。 */\n    private int groupSysFlag = 0;",
        ),
        (
            "    // Only valid for push consumer\n    private int consumeTimeoutMinute = 15;",
            "    /** 消费超时分钟数（仅 Push 消费者有效）。 */\n    private int consumeTimeoutMinute = 15;",
        ),
        (
            "    private Set<SimpleSubscriptionData> subscriptionDataSet;",
            "    /** 组内各 Topic 订阅数据集合。 */\n    private Set<SimpleSubscriptionData> subscriptionDataSet;",
        ),
        (
            "    private Map<String, String> attributes = new HashMap<>();",
            "    /** 扩展属性键值（Lite 订阅、优先级等）。 */\n    private Map<String, String> attributes = new HashMap<>();",
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
            "    public boolean isConsumeEnable() {",
            "    /** 是否启用消费。 */\n    public boolean isConsumeEnable() {",
        ),
        (
            "    public void setConsumeEnable(boolean consumeEnable) {",
            "    /** 设置是否启用消费。 */\n    public void setConsumeEnable(boolean consumeEnable) {",
        ),
        (
            "    public int getRetryMaxTimes() {",
            "    /** 返回最大重试次数。 */\n    public int getRetryMaxTimes() {",
        ),
        (
            "    public void setRetryMaxTimes(int retryMaxTimes) {",
            "    /** 设置最大重试次数。 */\n    public void setRetryMaxTimes(int retryMaxTimes) {",
        ),
        (
            "    public GroupRetryPolicy getGroupRetryPolicy() {",
            "    /** 返回组重试策略。 */\n    public GroupRetryPolicy getGroupRetryPolicy() {",
        ),
        (
            "    public void setGroupRetryPolicy(GroupRetryPolicy groupRetryPolicy) {",
            "    /** 设置组重试策略。 */\n    public void setGroupRetryPolicy(GroupRetryPolicy groupRetryPolicy) {",
        ),
        (
            "    @JSONField(serialize = false, deserialize = false)\n    public long getPriorityFactor() {",
            "    /** 从扩展属性读取优先级因子。 */\n    @JSONField(serialize = false, deserialize = false)\n    public long getPriorityFactor() {",
        ),
        (
            "    @JSONField(serialize = false, deserialize = false)\n    public void setLiteBindTopic(String liteBindTopic) {",
            "    /** 设置 Lite 订阅绑定的 Topic。 */\n    @JSONField(serialize = false, deserialize = false)\n    public void setLiteBindTopic(String liteBindTopic) {",
        ),
        (
            "    @JSONField(serialize = false, deserialize = false)\n    public String getLiteBindTopic() {",
            "    /** 返回 Lite 订阅绑定的 Topic。 */\n    @JSONField(serialize = false, deserialize = false)\n    public String getLiteBindTopic() {",
        ),
        (
            "    @JSONField(serialize = false, deserialize = false)\n    public int getLiteSubClientQuota() {",
            "    /** 返回 Lite 订阅客户端配额。 */\n    @JSONField(serialize = false, deserialize = false)\n    public int getLiteSubClientQuota() {",
        ),
        (
            "    @JSONField(serialize = false, deserialize = false)\n    public void setLiteSubExclusive(boolean liteSubExclusive) {",
            "    /** 设置 Lite 订阅为独占模式。 */\n    @JSONField(serialize = false, deserialize = false)\n    public void setLiteSubExclusive(boolean liteSubExclusive) {",
        ),
        (
            "    @JSONField(serialize = false, deserialize = false)\n    public boolean isLiteSubExclusive() {",
            "    /** 是否为 Lite 独占订阅。 */\n    @JSONField(serialize = false, deserialize = false)\n    public boolean isLiteSubExclusive() {",
        ),
        (
            "    /**\n     * Whether to reset offset in exclusive mode\n     */",
            "    /** 独占模式下是否在特定场景重置 offset。 */",
        ),
        (
            "    @JSONField(serialize = false, deserialize = false)\n    public boolean isResetOffsetInExclusiveMode() {",
            "    /** 独占模式是否重置 offset。 */\n    @JSONField(serialize = false, deserialize = false)\n    public boolean isResetOffsetInExclusiveMode() {",
        ),
        (
            "    @JSONField(serialize = false, deserialize = false)\n    public boolean isResetOffsetOnUnsubscribe() {",
            "    /** 取消订阅时是否重置 offset。 */\n    @JSONField(serialize = false, deserialize = false)\n    public boolean isResetOffsetOnUnsubscribe() {",
        ),
        (
            "    @JSONField(serialize = false, deserialize = false)\n    public int getMaxClientEventCount() {",
            "    /** 返回 Lite 客户端最大事件数（-1 表示未设置）。 */\n    @JSONField(serialize = false, deserialize = false)\n    public int getMaxClientEventCount() {",
        ),
        (
            "    @JSONField(serialize = false, deserialize = false)\n    public void setWildcardLiteGroup(boolean wildcard) {",
            "    /** 标记为通配 Lite 消费组。 */\n    @JSONField(serialize = false, deserialize = false)\n    public void setWildcardLiteGroup(boolean wildcard) {",
        ),
        (
            "    @JSONField(serialize = false, deserialize = false)\n    public boolean isWildcardLiteGroup() {",
            "    /** 是否为通配 Lite 消费组。 */\n    @JSONField(serialize = false, deserialize = false)\n    public boolean isWildcardLiteGroup() {",
        ),
        (
            "    @Override\n    public int hashCode() {",
            "    /** 计算配置哈希。 */\n    @Override\n    public int hashCode() {",
        ),
        (
            "    @Override\n    public boolean equals(Object obj) {",
            "    /** 比较两组配置是否等价。 */\n    @Override\n    public boolean equals(Object obj) {",
        ),
        (
            "    @Override\n    public String toString() {",
            "    /** 返回调试字符串。 */\n    @Override\n    public String toString() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/protocol/topic/OffsetMovedEvent.java": [
        (
            "public class OffsetMovedEvent extends RemotingSerializable {",
            "/**\n * Offset 被 Broker 强制迁移事件：记录消费组、队列及请求/新 offset。\n */\npublic class OffsetMovedEvent extends RemotingSerializable {",
        ),
        (
            "    private String consumerGroup;",
            "    /** 发生 offset 迁移的消费组。 */\n    private String consumerGroup;",
        ),
        (
            "    private MessageQueue messageQueue;",
            "    /** 受影响的 MessageQueue。 */\n    private MessageQueue messageQueue;",
        ),
        (
            "    private long offsetRequest;",
            "    /** 客户端请求的 offset。 */\n    private long offsetRequest;",
        ),
        (
            "    private long offsetNew;",
            "    /** Broker 指定的新 offset。 */\n    private long offsetNew;",
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
            "    public MessageQueue getMessageQueue() {",
            "    /** 返回消息队列。 */\n    public MessageQueue getMessageQueue() {",
        ),
        (
            "    public void setMessageQueue(MessageQueue messageQueue) {",
            "    /** 设置消息队列。 */\n    public void setMessageQueue(MessageQueue messageQueue) {",
        ),
        (
            "    public long getOffsetRequest() {",
            "    /** 返回请求的 offset。 */\n    public long getOffsetRequest() {",
        ),
        (
            "    public void setOffsetRequest(long offsetRequest) {",
            "    /** 设置请求的 offset。 */\n    public void setOffsetRequest(long offsetRequest) {",
        ),
        (
            "    public long getOffsetNew() {",
            "    /** 返回新 offset。 */\n    public long getOffsetNew() {",
        ),
        (
            "    public void setOffsetNew(long offsetNew) {",
            "    /** 设置新 offset。 */\n    public void setOffsetNew(long offsetNew) {",
        ),
        (
            "    @Override\n    public String toString() {",
            "    /** 返回事件摘要字符串。 */\n    @Override\n    public String toString() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/proxy/SocksProxyConfig.java": [
        (
            "public class SocksProxyConfig {",
            "/**\n * SOCKS 代理连接配置：地址与可选认证凭据。\n */\npublic class SocksProxyConfig {",
        ),
        (
            "    private String addr;",
            "    /** 代理服务器地址（host:port）。 */\n    private String addr;",
        ),
        (
            "    private String username;",
            "    /** 代理认证用户名（可为空）。 */\n    private String username;",
        ),
        (
            "    private String password;",
            "    /** 代理认证密码（可为空）。 */\n    private String password;",
        ),
        (
            "    public SocksProxyConfig() {",
            "    /** 默认无参构造。 */\n    public SocksProxyConfig() {",
        ),
        (
            "    public SocksProxyConfig(String addr) {",
            "    /** 仅指定代理地址。 */\n    public SocksProxyConfig(String addr) {",
        ),
        (
            "    public SocksProxyConfig(String addr, String username, String password) {",
            "    /** 指定地址与认证信息。 */\n    public SocksProxyConfig(String addr, String username, String password) {",
        ),
        (
            "    public String getAddr() {",
            "    /** 返回代理地址。 */\n    public String getAddr() {",
        ),
        (
            "    public void setAddr(String addr) {",
            "    /** 设置代理地址。 */\n    public void setAddr(String addr) {",
        ),
        (
            "    public String getUsername() {",
            "    /** 返回用户名。 */\n    public String getUsername() {",
        ),
        (
            "    public void setUsername(String username) {",
            "    /** 设置用户名。 */\n    public void setUsername(String username) {",
        ),
        (
            "    public String getPassword() {",
            "    /** 返回密码。 */\n    public String getPassword() {",
        ),
        (
            "    public void setPassword(String password) {",
            "    /** 设置密码。 */\n    public void setPassword(String password) {",
        ),
        (
            "    @Override\n    public String toString() {",
            "    /** 返回配置摘要（含密码字段，慎用日志）。 */\n    @Override\n    public String toString() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/rpc/ClientMetadata.java": [
        (
            "public class ClientMetadata {",
            "/**\n * RPC 客户端元数据缓存：维护 Topic 路由、Broker 地址表及静态 Topic 队列映射。\n */\npublic class ClientMetadata {",
        ),
        (
            "    private final ConcurrentMap<String/* Topic */, TopicRouteData> topicRouteTable = new ConcurrentHashMap<>();",
            "    /** Topic → 路由数据。 */\n    private final ConcurrentMap<String/* Topic */, TopicRouteData> topicRouteTable = new ConcurrentHashMap<>();",
        ),
        (
            "    private final ConcurrentMap<String/* Topic */, ConcurrentMap<MessageQueue, String/*brokerName*/>> topicEndPointsTable = new ConcurrentHashMap<>();",
            "    /** Topic → (MessageQueue → 物理 Broker 名) 静态 Topic 端点表。 */\n    private final ConcurrentMap<String/* Topic */, ConcurrentMap<MessageQueue, String/*brokerName*/>> topicEndPointsTable = new ConcurrentHashMap<>();",
        ),
        (
            "    private final ConcurrentMap<String/* Broker Name */, HashMap<Long/* brokerId */, String/* address */>> brokerAddrTable =",
            "    /** Broker 名 → (brokerId → 地址)。 */\n    private final ConcurrentMap<String/* Broker Name */, HashMap<Long/* brokerId */, String/* address */>> brokerAddrTable =",
        ),
        (
            "    private final ConcurrentMap<String/* Broker Name */, HashMap<String/* address */, Integer>> brokerVersionTable =",
            "    /** Broker 名 → (地址 → 版本号)。 */\n    private final ConcurrentMap<String/* Broker Name */, HashMap<String/* address */, Integer>> brokerVersionTable =",
        ),
        (
            "    public void freshTopicRoute(String topic, TopicRouteData topicRouteData) {",
            "    /** 刷新 Topic 路由：更新 Broker 地址表与静态队列端点。 */\n    public void freshTopicRoute(String topic, TopicRouteData topicRouteData) {",
        ),
        (
            "    public String getBrokerNameFromMessageQueue(final MessageQueue mq) {",
            "    /** 从 MessageQueue 解析物理 Broker 名（静态 Topic 走端点表）。 */\n    public String getBrokerNameFromMessageQueue(final MessageQueue mq) {",
        ),
        (
            "    public void refreshClusterInfo(ClusterInfo clusterInfo) {",
            "    /** 用集群信息批量刷新 Broker 地址表。 */\n    public void refreshClusterInfo(ClusterInfo clusterInfo) {",
        ),
        (
            "    public String findMasterBrokerAddr(String brokerName) {",
            "    /** 查找指定 Broker 组的 Master 地址。 */\n    public String findMasterBrokerAddr(String brokerName) {",
        ),
        (
            "    public ConcurrentMap<String, HashMap<Long, String>> getBrokerAddrTable() {",
            "    /** 返回 Broker 地址表。 */\n    public ConcurrentMap<String, HashMap<Long, String>> getBrokerAddrTable() {",
        ),
        (
            "    public static ConcurrentMap<MessageQueue, String> topicRouteData2EndpointsForStaticTopic(final String topic, final TopicRouteData route) {",
            "    /** 将静态 Topic 路由转换为 MessageQueue → 物理 Broker 名映射。 */\n    public static ConcurrentMap<MessageQueue, String> topicRouteData2EndpointsForStaticTopic(final String topic, final TopicRouteData route) {",
        ),
        (
            "            //accomplish the static logic queues",
            "            // 补齐静态逻辑队列到物理 Broker 的映射",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/rpc/RequestBuilder.java": [
        (
            "public class RequestBuilder {",
            "/**\n * RPC 请求头构建器：按 RequestCode 实例化对应 Header 并填充 Broker/队列字段。\n */\npublic class RequestBuilder {",
        ),
        (
            "    private static Map<Integer, Class> requestCodeMap = new HashMap<>();",
            "    /** RequestCode → 请求头 Class 映射。 */\n    private static Map<Integer, Class> requestCodeMap = new HashMap<>();",
        ),
        (
            "    public static RpcRequestHeader buildCommonRpcHeader(int requestCode, String destBrokerName) {",
            "    /** 构建通用 RPC 请求头（目标 Broker 名）。 */\n    public static RpcRequestHeader buildCommonRpcHeader(int requestCode, String destBrokerName) {",
        ),
        (
            "    public static RpcRequestHeader buildCommonRpcHeader(int requestCode, Boolean oneway, String destBrokerName) {",
            "    /** 构建通用 RPC 请求头（含 oneway 标志）。 */\n    public static RpcRequestHeader buildCommonRpcHeader(int requestCode, Boolean oneway, String destBrokerName) {",
        ),
        (
            "    public static TopicQueueRequestHeader buildTopicQueueRequestHeader(int requestCode, MessageQueue mq) {",
            "    /** 从 MessageQueue 构建 Topic/队列请求头。 */\n    public static TopicQueueRequestHeader buildTopicQueueRequestHeader(int requestCode, MessageQueue mq) {",
        ),
        (
            "    public static TopicQueueRequestHeader buildTopicQueueRequestHeader(int requestCode, MessageQueue mq, Boolean logic) {",
            "    /** 从 MessageQueue 构建请求头并指定是否逻辑队列。 */\n    public static TopicQueueRequestHeader buildTopicQueueRequestHeader(int requestCode, MessageQueue mq, Boolean logic) {",
        ),
        (
            "    public static TopicQueueRequestHeader buildTopicQueueRequestHeader(int requestCode, Boolean oneway, MessageQueue mq, Boolean logic) {",
            "    /** 从 MessageQueue 构建请求头（含 oneway 与逻辑队列标志）。 */\n    public static TopicQueueRequestHeader buildTopicQueueRequestHeader(int requestCode, Boolean oneway, MessageQueue mq, Boolean logic) {",
        ),
        (
            "    public static TopicQueueRequestHeader buildTopicQueueRequestHeader(int requestCode,  Boolean oneway, String destBrokerName, String topic, int queueId, Boolean logic) {",
            "    /** 按 Broker、Topic、queueId 构建 Topic 队列请求头。 */\n    public static TopicQueueRequestHeader buildTopicQueueRequestHeader(int requestCode,  Boolean oneway, String destBrokerName, String topic, int queueId, Boolean logic) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/rpc/RpcClient.java": [
        (
            "public interface RpcClient {",
            "/**\n * 面向 Broker 的异步 RPC 客户端接口：支持通用请求与 MessageQueue 维度调用。\n */\npublic interface RpcClient {",
        ),
        (
            "    //common invoke paradigm, the logic remote addr is defined in \"bname\" field of request\n    //For oneway request, the sign is labeled in request, and do not need an another method named \"invokeOneway\"\n    //For one\n    Future<RpcResponse>  invoke(RpcRequest request, long timeoutMs) throws RpcException;",
            "    /**\n     * 通用调用：目标 Broker 由请求头 bname 指定；oneway 标志写在请求内，无需单独方法。\n     */\n    Future<RpcResponse>  invoke(RpcRequest request, long timeoutMs) throws RpcException;",
        ),
        (
            "    //For rocketmq, most requests are corresponded to MessageQueue\n    //And for LogicQueue, the broker name is mocked, the physical addr could only be defined by MessageQueue\n    Future<RpcResponse>  invoke(MessageQueue mq, RpcRequest request, long timeoutMs) throws RpcException;",
            "    /**\n     * 按 MessageQueue 调用：逻辑队列使用 mock Broker 名，物理地址由 mq 解析。\n     */\n    Future<RpcResponse>  invoke(MessageQueue mq, RpcRequest request, long timeoutMs) throws RpcException;",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/rpc/RpcClientHook.java": [
        (
            "public abstract class RpcClientHook {",
            "/**\n * RPC 客户端钩子：在请求发出前或响应返回后可短路返回自定义结果。\n */\npublic abstract class RpcClientHook {",
        ),
        (
            "    //if the return is not null, return it\n    public abstract RpcResponse beforeRequest(RpcRequest rpcRequest) throws RpcException;",
            "    /** 请求前拦截；非 null 则直接作为响应返回。 */\n    public abstract RpcResponse beforeRequest(RpcRequest rpcRequest) throws RpcException;",
        ),
        (
            "    //if the return is not null, return it\n    public abstract RpcResponse afterResponse(RpcResponse rpcResponse) throws RpcException;",
            "    /** 响应后处理；非 null 则替换原响应返回。 */\n    public abstract RpcResponse afterResponse(RpcResponse rpcResponse) throws RpcException;",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/rpc/RpcClientImpl.java": [
        (
            "public class RpcClientImpl implements RpcClient {",
            "/**\n * {@link RpcClient} 默认实现：经 {@link RemotingClient} 向 Broker Master 发起各类 RPC。\n */\npublic class RpcClientImpl implements RpcClient {",
        ),
        (
            "    private ClientMetadata clientMetadata;",
            "    /** 客户端路由与 Broker 地址元数据。 */\n    private ClientMetadata clientMetadata;",
        ),
        (
            "    private RemotingClient remotingClient;",
            "    /** 底层 Remoting 客户端。 */\n    private RemotingClient remotingClient;",
        ),
        (
            "    private List<RpcClientHook> clientHookList = new ArrayList<>();",
            "    /** 已注册的 RPC 钩子链。 */\n    private List<RpcClientHook> clientHookList = new ArrayList<>();",
        ),
        (
            "    public RpcClientImpl(ClientMetadata clientMetadata, RemotingClient remotingClient) {",
            "    /** 注入元数据与 Remoting 客户端。 */\n    public RpcClientImpl(ClientMetadata clientMetadata, RemotingClient remotingClient) {",
        ),
        (
            "    public void registerHook(RpcClientHook hook) {",
            "    /** 注册 RPC 钩子。 */\n    public void registerHook(RpcClientHook hook) {",
        ),
        (
            "    @Override\n    public Future<RpcResponse>  invoke(MessageQueue mq, RpcRequest request, long timeoutMs) throws RpcException {",
            "    /** 解析 mq 对应 Broker 名后委托通用 invoke。 */\n    @Override\n    public Future<RpcResponse>  invoke(MessageQueue mq, RpcRequest request, long timeoutMs) throws RpcException {",
        ),
        (
            "    public Promise<RpcResponse> createResponseFuture()  {",
            "    /** 创建立即完成的 Netty Promise 用于包装 RpcResponse。 */\n    public Promise<RpcResponse> createResponseFuture()  {",
        ),
        (
            "    @Override\n    public Future<RpcResponse>  invoke(RpcRequest request, long timeoutMs) throws RpcException {",
            "    /** 执行钩子、解析 Master 地址并按 RequestCode 分发处理。 */\n    @Override\n    public Future<RpcResponse>  invoke(RpcRequest request, long timeoutMs) throws RpcException {",
        ),
        (
            "                    //For 1.6, there is not easy-to-use future impl",
            "                    // 兼容旧版：无便捷 Future 实现时直接 Promise 返回",
        ),
        (
            "    private String getBrokerAddrByNameOrException(String bname) throws RpcException {",
            "    /** 按 Broker 名查找 Master 地址，找不到则抛 RpcException。 */\n    private String getBrokerAddrByNameOrException(String bname) throws RpcException {",
        ),
        (
            "    private void processFailedResponse(String addr, RemotingCommand requestCommand,  ResponseFuture responseFuture, Promise<RpcResponse> rpcResponsePromise) {",
            "    /** 将 Remoting 层失败（发送失败/超时等）封装为 RpcResponse。 */\n    private void processFailedResponse(String addr, RemotingCommand requestCommand,  ResponseFuture responseFuture, Promise<RpcResponse> rpcResponsePromise) {",
        ),
        (
            "            //this should not happen",
            "            // 不应出现：有响应命令却走失败分支",
        ),
        (
            "    public Promise<RpcResponse> handlePullMessage(final String addr, RpcRequest rpcRequest, long timeoutMillis)  throws Exception {",
            "    /** 异步拉取消息并解析 Pull 响应头。 */\n    public Promise<RpcResponse> handlePullMessage(final String addr, RpcRequest rpcRequest, long timeoutMillis)  throws Exception {",
        ),
        (
            "    public Promise<RpcResponse> handleSearchOffset(String addr, RpcRequest rpcRequest, long timeoutMillis) throws Exception {",
            "    /** 同步按时间戳搜索 offset。 */\n    public Promise<RpcResponse> handleSearchOffset(String addr, RpcRequest rpcRequest, long timeoutMillis) throws Exception {",
        ),
        (
            "    public Promise<RpcResponse> handleQueryConsumerOffset(String addr, RpcRequest rpcRequest, long timeoutMillis) throws Exception {",
            "    /** 同步查询消费组 offset。 */\n    public Promise<RpcResponse> handleQueryConsumerOffset(String addr, RpcRequest rpcRequest, long timeoutMillis) throws Exception {",
        ),
        (
            "    public Promise<RpcResponse> handleUpdateConsumerOffset(String addr, RpcRequest rpcRequest, long timeoutMillis) throws Exception {",
            "    /** 同步更新消费组 offset。 */\n    public Promise<RpcResponse> handleUpdateConsumerOffset(String addr, RpcRequest rpcRequest, long timeoutMillis) throws Exception {",
        ),
        (
            "    public Promise<RpcResponse> handleCommonBodyRequest(final String addr, RpcRequest rpcRequest, long timeoutMillis, Class bodyClass) throws Exception {",
            "    /** 同步请求并解码固定 Body 类型（如 Topic 统计、Topic 配置）。 */\n    public Promise<RpcResponse> handleCommonBodyRequest(final String addr, RpcRequest rpcRequest, long timeoutMillis, Class bodyClass) throws Exception {",
        ),
        (
            "    public Promise<RpcResponse> handleGetMinOffset(String addr, RpcRequest rpcRequest, long timeoutMillis) throws Exception {",
            "    /** 同步获取队列最小 offset。 */\n    public Promise<RpcResponse> handleGetMinOffset(String addr, RpcRequest rpcRequest, long timeoutMillis) throws Exception {",
        ),
        (
            "    public Promise<RpcResponse> handleGetMaxOffset(String addr, RpcRequest rpcRequest, long timeoutMillis) throws Exception {",
            "    /** 同步获取队列最大 offset。 */\n    public Promise<RpcResponse> handleGetMaxOffset(String addr, RpcRequest rpcRequest, long timeoutMillis) throws Exception {",
        ),
        (
            "    public Promise<RpcResponse> handleGetEarliestMsgStoretime(String addr, RpcRequest rpcRequest, long timeoutMillis) throws Exception {",
            "    /** 同步获取队列最早消息存储时间。 */\n    public Promise<RpcResponse> handleGetEarliestMsgStoretime(String addr, RpcRequest rpcRequest, long timeoutMillis) throws Exception {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/rpc/RpcClientUtils.java": [
        (
            "public class RpcClientUtils {",
            "/**\n * RPC 与 RemotingCommand 互转工具：封装请求/响应命令及 Body 编码。\n */\npublic class RpcClientUtils {",
        ),
        (
            "    public static RemotingCommand createCommandForRpcRequest(RpcRequest rpcRequest) {",
            "    /** 由 RpcRequest 构造 Remoting 请求命令。 */\n    public static RemotingCommand createCommandForRpcRequest(RpcRequest rpcRequest) {",
        ),
        (
            "    public static RemotingCommand createCommandForRpcResponse(RpcResponse rpcResponse) {",
            "    /** 由 RpcResponse 构造 Remoting 响应命令。 */\n    public static RemotingCommand createCommandForRpcResponse(RpcResponse rpcResponse) {",
        ),
        (
            "    public static byte[] encodeBody(Object body) {",
            "    /** 将 Body 编码为字节数组（支持 byte[]、RemotingSerializable、ByteBuffer）。 */\n    public static byte[] encodeBody(Object body) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/rpc/RpcException.java": [
        (
            "public class RpcException extends RemotingException {",
            "/**\n * RPC 层异常：携带 Remoting 响应码与错误描述。\n */\npublic class RpcException extends RemotingException {",
        ),
        (
            "    private int errorCode;",
            "    /** Remoting 错误码。 */\n    private int errorCode;",
        ),
        (
            "    public RpcException(int errorCode, String message) {",
            "    /** 构造带错误码的 RPC 异常。 */\n    public RpcException(int errorCode, String message) {",
        ),
        (
            "    public RpcException(int errorCode, String message, Throwable cause) {",
            "    /** 构造带原因链的 RPC 异常。 */\n    public RpcException(int errorCode, String message, Throwable cause) {",
        ),
        (
            "    public int getErrorCode() {",
            "    /** 返回错误码。 */\n    public int getErrorCode() {",
        ),
        (
            "    public void setErrorCode(int errorCode) {",
            "    /** 设置错误码。 */\n    public void setErrorCode(int errorCode) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/rpc/RpcRequest.java": [
        (
            "public class RpcRequest {",
            "/**\n * RPC 请求封装：RequestCode、自定义 Header 与可选 Body。\n */\npublic class RpcRequest {",
        ),
        (
            "    int code;",
            "    /** Remoting RequestCode。 */\n    int code;",
        ),
        (
            "    private RpcRequestHeader header;",
            "    /** 请求自定义头。 */\n    private RpcRequestHeader header;",
        ),
        (
            "    private Object body;",
            "    /** 请求体（可为 null）。 */\n    private Object body;",
        ),
        (
            "    public RpcRequest(int code, RpcRequestHeader header, Object body) {",
            "    /** 构造 RPC 请求。 */\n    public RpcRequest(int code, RpcRequestHeader header, Object body) {",
        ),
        (
            "    public RpcRequestHeader getHeader() {",
            "    /** 返回请求头。 */\n    public RpcRequestHeader getHeader() {",
        ),
        (
            "    public Object getBody() {",
            "    /** 返回请求体。 */\n    public Object getBody() {",
        ),
        (
            "    public int getCode() {",
            "    /** 返回 RequestCode。 */\n    public int getCode() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/rpc/RpcRequestHeader.java": [
        (
            "public abstract class RpcRequestHeader implements CommandCustomHeader {",
            "/**\n * RPC 请求头基类：命名空间、目标 Broker 名与 oneway 标志。\n */\npublic abstract class RpcRequestHeader implements CommandCustomHeader {",
        ),
        (
            "    //the namespace name\n    protected String ns;",
            "    /** 命名空间名称。 */\n    protected String ns;",
        ),
        (
            "    //if the data has been namespaced\n    protected Boolean nsd;",
            "    /** 数据是否已按命名空间隔离。 */\n    protected Boolean nsd;",
        ),
        (
            "    //the abstract remote addr name, usually the physical broker name\n    protected String bname;",
            "    /** 目标 Broker 抽象名（通常为物理 Broker 组名）。 */\n    protected String bname;",
        ),
        (
            "    //oneway\n    protected Boolean oway;",
            "    /** 是否为 oneway 请求（无需响应）。 */\n    protected Boolean oway;",
        ),
        (
            "    @Deprecated\n    public String getBname() {",
            "    /** 已废弃：请使用 {@link #getBrokerName()}。 */\n    @Deprecated\n    public String getBname() {",
        ),
        (
            "    @Deprecated\n    public void setBname(String brokerName) {",
            "    /** 已废弃：请使用 {@link #setBrokerName(String)}。 */\n    @Deprecated\n    public void setBname(String brokerName) {",
        ),
        (
            "    public String getBrokerName() {",
            "    /** 返回目标 Broker 名。 */\n    public String getBrokerName() {",
        ),
        (
            "    public void setBrokerName(String brokerName) {",
            "    /** 设置目标 Broker 名。 */\n    public void setBrokerName(String brokerName) {",
        ),
        (
            "    public String getNamespace() {",
            "    /** 返回命名空间。 */\n    public String getNamespace() {",
        ),
        (
            "    public void setNamespace(String namespace) {",
            "    /** 设置命名空间。 */\n    public void setNamespace(String namespace) {",
        ),
        (
            "    public Boolean getNamespaced() {",
            "    /** 是否已命名空间化。 */\n    public Boolean getNamespaced() {",
        ),
        (
            "    public void setNamespaced(Boolean namespaced) {",
            "    /** 设置命名空间化标志。 */\n    public void setNamespaced(Boolean namespaced) {",
        ),
        (
            "    public Boolean getOneway() {",
            "    /** 是否 oneway 请求。 */\n    public Boolean getOneway() {",
        ),
        (
            "    public void setOneway(Boolean oneway) {",
            "    /** 设置 oneway 标志。 */\n    public void setOneway(Boolean oneway) {",
        ),
        (
            "    @Override\n    public boolean equals(Object o) {",
            "    /** 比较请求头字段相等性。 */\n    @Override\n    public boolean equals(Object o) {",
        ),
        (
            "    @Override\n    public int hashCode() {",
            "    /** 计算请求头哈希。 */\n    @Override\n    public int hashCode() {",
        ),
        (
            "    @Override\n    public String toString() {",
            "    /** 返回调试字符串。 */\n    @Override\n    public String toString() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/rpc/RpcResponse.java": [
        (
            "public class RpcResponse   {",
            "/**\n * RPC 响应封装：响应码、Header、Body 或内嵌 RpcException。\n */\npublic class RpcResponse   {",
        ),
        (
            "    private int code;",
            "    /** Remoting 响应码。 */\n    private int code;",
        ),
        (
            "    private CommandCustomHeader header;",
            "    /** 响应自定义头。 */\n    private CommandCustomHeader header;",
        ),
        (
            "    private Object body;",
            "    /** 响应体。 */\n    private Object body;",
        ),
        (
            "    public RpcException exception;",
            "    /** 失败时的 RPC 异常（成功时为 null）。 */\n    public RpcException exception;",
        ),
        (
            "    public RpcResponse() {",
            "    /** 默认构造。 */\n    public RpcResponse() {",
        ),
        (
            "    public RpcResponse(int code, CommandCustomHeader header, Object body) {",
            "    /** 构造成功响应。 */\n    public RpcResponse(int code, CommandCustomHeader header, Object body) {",
        ),
        (
            "    public RpcResponse(RpcException rpcException) {",
            "    /** 由异常构造失败响应。 */\n    public RpcResponse(RpcException rpcException) {",
        ),
        (
            "    public int getCode() {",
            "    /** 返回响应码。 */\n    public int getCode() {",
        ),
        (
            "    public CommandCustomHeader getHeader() {",
            "    /** 返回响应头。 */\n    public CommandCustomHeader getHeader() {",
        ),
        (
            "    public void setHeader(CommandCustomHeader header) {",
            "    /** 设置响应头。 */\n    public void setHeader(CommandCustomHeader header) {",
        ),
        (
            "    public Object getBody() {",
            "    /** 返回响应体。 */\n    public Object getBody() {",
        ),
        (
            "    public void setBody(Object body) {",
            "    /** 设置响应体。 */\n    public void setBody(Object body) {",
        ),
        (
            "    public RpcException getException() {",
            "    /** 返回内嵌异常。 */\n    public RpcException getException() {",
        ),
        (
            "    public void setException(RpcException exception) {",
            "    /** 设置内嵌异常。 */\n    public void setException(RpcException exception) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/rpc/TopicQueueRequestHeader.java": [
        (
            "public abstract class TopicQueueRequestHeader extends TopicRequestHeader {",
            "/**\n * 带 Topic 与队列 ID 的 RPC 请求头抽象基类。\n */\npublic abstract class TopicQueueRequestHeader extends TopicRequestHeader {",
        ),
        (
            "    public abstract Integer getQueueId();",
            "    /** 返回队列 ID。 */\n    public abstract Integer getQueueId();",
        ),
        (
            "    public abstract void setQueueId(Integer queueId);",
            "    /** 设置队列 ID。 */\n    public abstract void setQueueId(Integer queueId);",
        ),
    ],
}
