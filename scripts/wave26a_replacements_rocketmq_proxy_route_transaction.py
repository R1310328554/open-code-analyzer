"""Chinese JavaDoc replacements for RocketMQ wave26a proxy route/sysmessage/transaction [0:15]."""

R: dict[str, list[tuple[str, str]]] = {
    "proxy/src/main/java/org/apache/rocketmq/proxy/service/route/MessageQueueSelector.java": [
        (
            "public class MessageQueueSelector {",
            "/**\n * 消息队列选择器：基于主题路由构建读/写队列列表，支持轮询、惩罚因子与优先级分组选择。\n */\npublic class MessageQueueSelector {",
        ),
        (
            "    private static final int BROKER_ACTING_QUEUE_ID = -1;",
            "    /** Broker 代理队列标识，queueId 为 -1 表示按 Broker 粒度路由。 */\n    private static final int BROKER_ACTING_QUEUE_ID = -1;",
        ),
        (
            "    // multiple queues for brokers with queueId : normal",
            "    // 普通队列：每个 Broker 下 queueId >= 0 的多个队列",
        ),
        (
            "    // one queue for brokers with queueId : -1",
            "    // Broker 代理队列：每个 Broker 仅一条 queueId 为 -1 的队列",
        ),
        (
            "    // ordered by priority asc (smaller => higher priority)",
            "    // 按优先级升序分组（数值越小优先级越高）",
        ),
        (
            "    public MessageQueueSelector(TopicRouteWrapper topicRouteWrapper, boolean read) {",
            "    /** 以主题路由与读/写模式构造选择器，使用默认优先级提供者。 */\n    public MessageQueueSelector(TopicRouteWrapper topicRouteWrapper, boolean read) {",
        ),
        (
            "    public MessageQueueSelector(TopicRouteWrapper topicRouteWrapper, boolean read,\n        MessageQueuePriorityProvider<AddressableMessageQueue> priorityProvider) {",
            "    /**\n     * 构造选择器并指定优先级分组策略。\n     *\n     * @param topicRouteWrapper 主题路由包装\n     * @param read 为 true 时构建可读队列，否则构建可写队列\n     * @param priorityProvider 队列优先级提供者，可为 null\n     */\n    public MessageQueueSelector(TopicRouteWrapper topicRouteWrapper, boolean read,\n        MessageQueuePriorityProvider<AddressableMessageQueue> priorityProvider) {",
        ),
        (
            "    private static List<AddressableMessageQueue> buildRead(TopicRouteWrapper topicRoute) {",
            "    /** 根据可读权限与主 Broker 地址构建读队列列表。 */\n    private static List<AddressableMessageQueue> buildRead(TopicRouteWrapper topicRoute) {",
        ),
        (
            "    private static List<AddressableMessageQueue> buildWrite(TopicRouteWrapper topicRoute) {",
            "    /** 根据顺序主题配置或可写权限构建写队列列表。 */\n    private static List<AddressableMessageQueue> buildWrite(TopicRouteWrapper topicRoute) {",
        ),
        (
            "        // order topic route.",
            "        // 顺序主题：按 orderTopicConf 解析 Broker 与队列数",
        ),
        (
            "    public AddressableMessageQueue getQueueByBrokerName(String brokerName) {",
            "    /** 按 Broker 名称返回对应的 Broker 代理队列。 */\n    public AddressableMessageQueue getQueueByBrokerName(String brokerName) {",
        ),
        (
            "    public AddressableMessageQueue selectOne(boolean onlyBroker) {",
            "    /** 轮询选择一条队列；onlyBroker 为 true 时仅从 Broker 代理队列中选择。 */\n    public AddressableMessageQueue selectOne(boolean onlyBroker) {",
        ),
        (
            "    public AddressableMessageQueue selectOneByPipeline(boolean onlyBroker) {",
            "    /** 优先按惩罚因子与优先级选择，无可用结果时回退到轮询。 */\n    public AddressableMessageQueue selectOneByPipeline(boolean onlyBroker) {",
        ),
        (
            "        // SendLatency is not enabled, or no queue is selected, then select by index.",
            "        // 未启用延迟惩罚或未选出队列时，按索引轮询",
        ),
        (
            "    public AddressableMessageQueue selectNextOne(AddressableMessageQueue last) {",
            "    /** 选择与 last 不同的下一条队列，最多尝试 count 次。 */\n    public AddressableMessageQueue selectNextOne(AddressableMessageQueue last) {",
        ),
        (
            "    public AddressableMessageQueue selectOneByIndex(int index, boolean onlyBroker) {",
            "    /** 按 index 取模选择队列。 */\n    public AddressableMessageQueue selectOneByIndex(int index, boolean onlyBroker) {",
        ),
        (
            "    public void addPenalizer(MessageQueuePenalizer<AddressableMessageQueue> penalizer) {",
            "    /** 注册队列惩罚器，用于延迟/故障容忍路由。 */\n    public void addPenalizer(MessageQueuePenalizer<AddressableMessageQueue> penalizer) {",
        ),
    ],
    "proxy/src/main/java/org/apache/rocketmq/proxy/service/route/MessageQueueView.java": [
        (
            "public class MessageQueueView {",
            "/**\n * 主题消息队列视图：封装读/写 {@link MessageQueueSelector} 与 {@link TopicRouteWrapper}。\n */\npublic class MessageQueueView {",
        ),
        (
            "    public static final MessageQueueView WRAPPED_EMPTY_QUEUE = new MessageQueueView(\"\", new TopicRouteData(), null);",
            "    /** 空路由占位视图，表示 NameServer 中不存在该主题。 */\n    public static final MessageQueueView WRAPPED_EMPTY_QUEUE = new MessageQueueView(\"\", new TopicRouteData(), null);",
        ),
        (
            "    public MessageQueueView(String topic, TopicRouteData topicRouteData, List<MessageQueuePenalizer<AddressableMessageQueue>> penalizer) {",
            "    /** 以主题名、路由数据与惩罚器列表构造视图。 */\n    public MessageQueueView(String topic, TopicRouteData topicRouteData, List<MessageQueuePenalizer<AddressableMessageQueue>> penalizer) {",
        ),
        (
            "    public MessageQueueView(String topic, TopicRouteData topicRouteData, List<MessageQueuePenalizer<AddressableMessageQueue>> penalizer,\n        MessageQueuePriorityProvider<AddressableMessageQueue> priorityProvider) {",
            "    /**\n     * 构造视图并指定队列优先级提供者。\n     *\n     * @param topic 主题名\n     * @param topicRouteData NameServer 返回的路由数据\n     * @param penalizer 惩罚器列表\n     * @param priorityProvider 优先级提供者\n     */\n    public MessageQueueView(String topic, TopicRouteData topicRouteData, List<MessageQueuePenalizer<AddressableMessageQueue>> penalizer,\n        MessageQueuePriorityProvider<AddressableMessageQueue> priorityProvider) {",
        ),
        (
            "    public boolean isEmptyCachedQueue() {",
            "    /** 判断是否为无路由的空占位视图。 */\n    public boolean isEmptyCachedQueue() {",
        ),
        (
            "    public MessageQueueSelector getReadSelector() {",
            "    /** 返回读路径队列选择器。 */\n    public MessageQueueSelector getReadSelector() {",
        ),
        (
            "    public MessageQueueSelector getWriteSelector() {",
            "    /** 返回写路径队列选择器。 */\n    public MessageQueueSelector getWriteSelector() {",
        ),
    ],
    "proxy/src/main/java/org/apache/rocketmq/proxy/service/route/ProxyTopicRouteData.java": [
        (
            "public class ProxyTopicRouteData {",
            "/**\n * Proxy 侧主题路由数据：将 Broker 地址转换为 {@link Address} 列表供客户端访问。\n */\npublic class ProxyTopicRouteData {",
        ),
        (
            "    public ProxyTopicRouteData(TopicRouteData topicRouteData) {",
            "    /** 从标准 {@link TopicRouteData} 转换，保留原始 Broker 主机与端口。 */\n    public ProxyTopicRouteData(TopicRouteData topicRouteData) {",
        ),
        (
            "    public ProxyTopicRouteData(TopicRouteData topicRouteData, int port) {",
            "    /** 转换路由并将所有 Broker 地址端口替换为指定 proxy 端口。 */\n    public ProxyTopicRouteData(TopicRouteData topicRouteData, int port) {",
        ),
        (
            "    public ProxyTopicRouteData(TopicRouteData topicRouteData, List<Address> requestHostAndPortList) {",
            "    /** 转换路由并将各 Broker 地址统一映射为请求侧地址列表。 */\n    public ProxyTopicRouteData(TopicRouteData topicRouteData, List<Address> requestHostAndPortList) {",
        ),
        (
            "    public static class ProxyBrokerData {",
            "    /** Proxy 视角的 Broker 路由条目，地址为 {@link Address} 列表。 */\n    public static class ProxyBrokerData {",
        ),
        (
            "        public BrokerData buildBrokerData() {",
            "        /** 还原为 Remoting 协议使用的 {@link BrokerData}。 */\n        public BrokerData buildBrokerData() {",
        ),
        (
            "    public TopicRouteData buildTopicRouteData() {",
            "    /** 组装并返回标准 {@link TopicRouteData}。 */\n    public TopicRouteData buildTopicRouteData() {",
        ),
    ],
    "proxy/src/main/java/org/apache/rocketmq/proxy/service/route/TopicRouteHelper.java": [
        (
            "public class TopicRouteHelper {",
            "/** 主题路由相关异常判定工具类。 */\npublic class TopicRouteHelper {",
        ),
        (
            "    public static boolean isTopicNotExistError(Throwable e) {",
            "    /** 判断异常是否表示主题在 NameServer/Broker 中不存在。 */\n    public static boolean isTopicNotExistError(Throwable e) {",
        ),
    ],
    "proxy/src/main/java/org/apache/rocketmq/proxy/service/route/TopicRouteService.java": [
        (
            "public abstract class TopicRouteService extends AbstractStartAndShutdown {",
            "/**\n * 主题路由服务抽象基类：维护 Caffeine 缓存、故障容忍策略与队列视图构建。\n */\npublic abstract class TopicRouteService extends AbstractStartAndShutdown {",
        ),
        (
            "    protected final LoadingCache<String /* topicName */, MessageQueueView> topicCache;",
            "    /** 主题名到 {@link MessageQueueView} 的本地缓存。 */\n    protected final LoadingCache<String /* topicName */, MessageQueueView> topicCache;",
        ),
        (
            "                        if (TopicRouteHelper.isTopicNotExistError(e)) {",
            "                        // 主题不存在时缓存空占位视图\n                        if (TopicRouteHelper.isTopicNotExistError(e)) {",
        ),
        (
            "    // pickup one topic in the topic cache",
            "    // 从缓存中任取一个主题用于 Broker 可达性探测",
        ),
        (
            "    public void updateFaultItem(final String brokerName, final long currentLatency, boolean isolation,\n                                boolean reachable) {",
            "    /** 更新 Broker 故障项：延迟、隔离与可达状态。 */\n    public void updateFaultItem(final String brokerName, final long currentLatency, boolean isolation,\n                                boolean reachable) {",
        ),
        (
            "    public MessageQueueView getAllMessageQueueView(ProxyContext ctx, String topicName) throws Exception {",
            "    /** 从缓存加载主题的全部读写队列视图。 */\n    public MessageQueueView getAllMessageQueueView(ProxyContext ctx, String topicName) throws Exception {",
        ),
        (
            "    public abstract MessageQueueView getCurrentMessageQueueView(ProxyContext ctx, String topicName) throws Exception;",
            "    /** 获取当前生效的主题队列视图（子类实现）。 */\n    public abstract MessageQueueView getCurrentMessageQueueView(ProxyContext ctx, String topicName) throws Exception;",
        ),
        (
            "    protected static MessageQueueView getCacheMessageQueueWrapper(LoadingCache<String, MessageQueueView> topicCache,\n        String key) throws Exception {",
            "    /** 从缓存取视图，空占位时抛出 TOPIC_NOT_EXIST。 */\n    protected static MessageQueueView getCacheMessageQueueWrapper(LoadingCache<String, MessageQueueView> topicCache,\n        String key) throws Exception {",
        ),
        (
            "    protected static boolean isTopicRouteValid(TopicRouteData routeData) {",
            "    /** 校验路由数据是否同时包含队列与 Broker 信息。 */\n    protected static boolean isTopicRouteValid(TopicRouteData routeData) {",
        ),
        (
            "    protected MessageQueueView buildMessageQueueView(String topic, TopicRouteData topicRouteData) {",
            "    /** 根据路由有效性构建 {@link MessageQueueView} 或空占位。 */\n    protected MessageQueueView buildMessageQueueView(String topic, TopicRouteData topicRouteData) {",
        ),
        (
            "    @VisibleForTesting\n    public static List<MessageQueuePenalizer<AddressableMessageQueue>> buildPenalizerByMQFaultStrategy(MQFaultStrategy mqFaultStrategy) {",
            "    @VisibleForTesting\n    /** 根据 {@link MQFaultStrategy} 构建可用性与可达性惩罚器。 */\n    public static List<MessageQueuePenalizer<AddressableMessageQueue>> buildPenalizerByMQFaultStrategy(MQFaultStrategy mqFaultStrategy) {",
        ),
    ],
    "proxy/src/main/java/org/apache/rocketmq/proxy/service/route/TopicRouteWrapper.java": [
        (
            "public class TopicRouteWrapper {",
            "/**\n * {@link TopicRouteData} 包装：按 brokerName 索引并便捷查询 Master 地址。\n */\npublic class TopicRouteWrapper {",
        ),
        (
            "    public TopicRouteWrapper(TopicRouteData topicRouteData, String topicName) {",
            "    /** @param topicRouteData 原始路由 @param topicName 主题名 */\n    public TopicRouteWrapper(TopicRouteData topicRouteData, String topicName) {",
        ),
        (
            "    public String getMasterAddr(String brokerName) {",
            "    /** 返回指定 Broker 的 Master 地址。 */\n    public String getMasterAddr(String brokerName) {",
        ),
        (
            "    public String getMasterAddrPrefer(String brokerName) {",
            "    /** 优先返回 Master 地址，无 Master 时取任意可用地址。 */\n    public String getMasterAddrPrefer(String brokerName) {",
        ),
        (
            "    public String getOrderTopicConf() {",
            "    /** 返回顺序主题配置字符串。 */\n    public String getOrderTopicConf() {",
        ),
    ],
    "proxy/src/main/java/org/apache/rocketmq/proxy/service/sysmessage/AbstractSystemMessageSyncer.java": [
        (
            "public abstract class AbstractSystemMessageSyncer implements StartAndShutdown, MessageListenerConcurrently {",
            "/**\n * 系统消息同步器基类：通过广播主题在 Proxy 实例间同步注册/注销等系统事件。\n */\npublic abstract class AbstractSystemMessageSyncer implements StartAndShutdown, MessageListenerConcurrently {",
        ),
        (
            "    protected String getSystemMessageProducerId() {",
            "    /** 系统消息生产者组 ID。 */\n    protected String getSystemMessageProducerId() {",
        ),
        (
            "    protected String getBroadcastTopicName() {",
            "    /** 广播主题名，取自 Proxy 配置。 */\n    protected String getBroadcastTopicName() {",
        ),
        (
            "    protected void sendSystemMessage(Object data) {",
            "    /** 将 data 序列化为 JSON 并异步发送到广播主题。 */\n    protected void sendSystemMessage(Object data) {",
        ),
        (
            "    protected SendMessageRequestHeader buildSendMessageRequestHeader(Message message,\n        String producerGroup, int queueId) {",
            "    /** 构造系统消息发送请求头。 */\n    protected SendMessageRequestHeader buildSendMessageRequestHeader(Message message,\n        String producerGroup, int queueId) {",
        ),
        (
            "    @Override\n    public void start() throws Exception {",
            "    @Override\n    /** 创建广播主题并启动 BROADCASTING 模式 PushConsumer。 */\n    public void start() throws Exception {",
        ),
        (
            "    protected void createSysTopic() {",
            "    /** 在指定集群上创建系统广播主题（若不存在）。 */\n    protected void createSysTopic() {",
        ),
        (
            "    @Override\n    public void shutdown() throws Exception {",
            "    @Override\n    /** 关闭 PushConsumer。 */\n    public void shutdown() throws Exception {",
        ),
    ],
    "proxy/src/main/java/org/apache/rocketmq/proxy/service/sysmessage/HeartbeatSyncer.java": [
        (
            "public class HeartbeatSyncer extends AbstractSystemMessageSyncer {",
            "/**\n * 消费者心跳同步器：将本地消费者注册/注销广播到其他 Proxy 实例。\n */\npublic class HeartbeatSyncer extends AbstractSystemMessageSyncer {",
        ),
        (
            "    protected final Map<String /* group @ channelId as longText */, RemoteChannel> remoteChannelMap = new ConcurrentHashMap<>();",
            "    /** 远程消费者通道映射：group@channelId -> {@link RemoteChannel}。 */\n    protected final Map<String /* group @ channelId as longText */, RemoteChannel> remoteChannelMap = new ConcurrentHashMap<>();",
        ),
        (
            "    public HeartbeatSyncer(TopicRouteService topicRouteService, AdminService adminService,\n                           ConsumerManager consumerManager, MQClientAPIFactory mqClientAPIFactory, RPCHook rpcHook) {",
            "    /** 构造心跳同步器并注册 ConsumerIds 变更监听。 */\n    public HeartbeatSyncer(TopicRouteService topicRouteService, AdminService adminService,\n                           ConsumerManager consumerManager, MQClientAPIFactory mqClientAPIFactory, RPCHook rpcHook) {",
        ),
        (
            "    protected void processConsumerGroupEvent(ConsumerGroupEvent event, String group, Object... args) {",
            "    /** 处理消费者组事件，注销时清理 remoteChannelMap。 */\n    protected void processConsumerGroupEvent(ConsumerGroupEvent event, String group, Object... args) {",
        ),
        (
            "    public void onConsumerRegister(String consumerGroup, ClientChannelInfo clientChannelInfo,",
            "    /** 本地消费者注册时异步广播 REGISTER 心跳。 */\n    public void onConsumerRegister(String consumerGroup, ClientChannelInfo clientChannelInfo,",
        ),
        (
            "    public void onConsumerUnRegister(String consumerGroup, ClientChannelInfo clientChannelInfo) {",
            "    /** 本地消费者注销时异步广播 UNREGISTER 心跳。 */\n    public void onConsumerUnRegister(String consumerGroup, ClientChannelInfo clientChannelInfo) {",
        ),
        (
            "    @Override\n    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {",
            "    @Override\n    /** 消费广播心跳并在远端 Proxy 上注册/注销消费者。 */\n    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {",
        ),
        (
            "        // use local address, remoting port and grpc port to build unique local proxy Id",
            "        // 使用本地地址、Remoting 端口与 gRPC 端口拼接唯一 Proxy 标识",
        ),
    ],
    "proxy/src/main/java/org/apache/rocketmq/proxy/service/sysmessage/HeartbeatSyncerData.java": [
        (
            "public class HeartbeatSyncerData {",
            "/** 心跳同步广播消息体：携带消费者注册/注销上下文与通道编码。 */\npublic class HeartbeatSyncerData {",
        ),
        (
            "    public HeartbeatSyncerData(HeartbeatType heartbeatType, String clientId,\n        LanguageCode language, int version, String group,\n        ConsumeType consumeType, MessageModel messageModel,\n        ConsumeFromWhere consumeFromWhere, String localProxyId,\n        String channelData) {",
            "    /**\n     * 构造心跳同步数据。\n     *\n     * @param heartbeatType REGISTER 或 UNREGISTER\n     * @param clientId 客户端标识\n     * @param localProxyId 发送方 Proxy 唯一 ID\n     * @param channelData 编码后的 {@link RemoteChannel} 数据\n     */\n    public HeartbeatSyncerData(HeartbeatType heartbeatType, String clientId,\n        LanguageCode language, int version, String group,\n        ConsumeType consumeType, MessageModel messageModel,\n        ConsumeFromWhere consumeFromWhere, String localProxyId,\n        String channelData) {",
        ),
        (
            "    public HeartbeatType getHeartbeatType() {",
            "    /** 返回心跳类型。 */\n    public HeartbeatType getHeartbeatType() {",
        ),
        (
            "    public String getLocalProxyId() {",
            "    /** 返回发送方 Proxy 标识。 */\n    public String getLocalProxyId() {",
        ),
        (
            "    public String getChannelData() {",
            "    /** 返回序列化后的远程通道数据。 */\n    public String getChannelData() {",
        ),
    ],
    "proxy/src/main/java/org/apache/rocketmq/proxy/service/sysmessage/HeartbeatType.java": [
        (
            "public enum HeartbeatType {",
            "/** 心跳同步类型：消费者注册或注销。 */\npublic enum HeartbeatType {",
        ),
        (
            "    REGISTER,",
            "    /** 消费者注册。 */\n    REGISTER,",
        ),
        (
            "    UNREGISTER;",
            "    /** 消费者注销。 */\n    UNREGISTER;",
        ),
    ],
    "proxy/src/main/java/org/apache/rocketmq/proxy/service/transaction/AbstractTransactionService.java": [
        (
            "public abstract class AbstractTransactionService implements TransactionService, StartAndShutdown {",
            "/**\n * 事务服务抽象基类：管理 {@link TransactionData} 并生成结束事务请求。\n */\npublic abstract class AbstractTransactionService implements TransactionService, StartAndShutdown {",
        ),
        (
            "    protected TransactionDataManager transactionDataManager = new TransactionDataManager();",
            "    /** 本地事务元数据管理器。 */\n    protected TransactionDataManager transactionDataManager = new TransactionDataManager();",
        ),
        (
            "    @Override\n    public TransactionData addTransactionDataByBrokerName(ProxyContext ctx, String brokerName, String topic, String producerGroup, long tranStateTableOffset, long commitLogOffset, String transactionId,\n        Message message) {",
            "    @Override\n    /** 按 brokerName 记录半消息事务元数据。 */\n    public TransactionData addTransactionDataByBrokerName(ProxyContext ctx, String brokerName, String topic, String producerGroup, long tranStateTableOffset, long commitLogOffset, String transactionId,\n        Message message) {",
        ),
        (
            "    @Override\n    public EndTransactionRequestData genEndTransactionRequestHeader(ProxyContext ctx, String topic, String producerGroup, Integer commitOrRollback,\n        boolean fromTransactionCheck, String msgId, String transactionId) {",
            "    @Override\n    /** 根据本地缓存的事务数据生成 {@link EndTransactionRequestData}。 */\n    public EndTransactionRequestData genEndTransactionRequestHeader(ProxyContext ctx, String topic, String producerGroup, Integer commitOrRollback,\n        boolean fromTransactionCheck, String msgId, String transactionId) {",
        ),
        (
            "    @Override\n    public void onSendCheckTransactionStateFailed(ProxyContext context, String producerGroup, TransactionData transactionData) {",
            "    @Override\n    /** 回查发送失败时移除本地事务记录。 */\n    public void onSendCheckTransactionStateFailed(ProxyContext context, String producerGroup, TransactionData transactionData) {",
        ),
        (
            "    protected abstract String getBrokerNameByAddr(String brokerAddr);",
            "    /** 将 Broker 地址解析为 brokerName（子类实现）。 */\n    protected abstract String getBrokerNameByAddr(String brokerAddr);",
        ),
    ],
    "proxy/src/main/java/org/apache/rocketmq/proxy/service/transaction/ClusterTransactionService.java": [
        (
            "public class ClusterTransactionService extends AbstractTransactionService {",
            "/**\n * 集群模式事务服务：维护生产者组与集群订阅关系，并周期性向 Broker 发送事务心跳。\n */\npublic class ClusterTransactionService extends AbstractTransactionService {",
        ),
        (
            "    private static final String TRANS_HEARTBEAT_CLIENT_ID = \"rmq-proxy-producer-client\";",
            "    /** 事务心跳使用的固定 clientId。 */\n    private static final String TRANS_HEARTBEAT_CLIENT_ID = \"rmq-proxy-producer-client\";",
        ),
        (
            "    @Override\n    public void addTransactionSubscription(ProxyContext ctx, String group, String topic) {",
            "    @Override\n    /** 将主题所属集群加入生产者组的事务订阅集合。 */\n    public void addTransactionSubscription(ProxyContext ctx, String group, String topic) {",
        ),
        (
            "    @Override\n    public void replaceTransactionSubscription(ProxyContext ctx, String group, List<String> topicList) {",
            "    @Override\n    /** 用新主题列表替换生产者组的集群订阅。 */\n    public void replaceTransactionSubscription(ProxyContext ctx, String group, List<String> topicList) {",
        ),
        (
            "    public void scanProducerHeartBeat() {",
            "    /** 扫描在线生产者组并向各集群 Broker 发送事务心跳。 */\n    public void scanProducerHeartBeat() {",
        ),
        (
            "    protected void sendHeartBeatToCluster(String clusterName, HeartbeatData heartbeatData, Map<String, String> brokerAddrNameMap) {",
            "    /** 向集群内各 Broker 异步发送单条事务心跳。 */\n    protected void sendHeartBeatToCluster(String clusterName, HeartbeatData heartbeatData, Map<String, String> brokerAddrNameMap) {",
        ),
        (
            "    class TxHeartbeatServiceThread extends ServiceThread {",
            "    /** 后台线程：按配置周期触发 scanProducerHeartBeat。 */\n    class TxHeartbeatServiceThread extends ServiceThread {",
        ),
        (
            "        @Override\n        protected void onWaitEnd() {",
            "        @Override\n        /** 等待周期结束后扫描并发送事务心跳。 */\n        protected void onWaitEnd() {",
        ),
    ],
    "proxy/src/main/java/org/apache/rocketmq/proxy/service/transaction/EndTransactionRequestData.java": [
        (
            "public class EndTransactionRequestData {",
            "/** 结束事务请求封装：目标 brokerName 与 {@link EndTransactionRequestHeader}。 */\npublic class EndTransactionRequestData {",
        ),
        (
            "    public EndTransactionRequestData(String brokerName, EndTransactionRequestHeader requestHeader) {",
            "    /** @param brokerName 目标 Broker @param requestHeader 结束事务请求头 */\n    public EndTransactionRequestData(String brokerName, EndTransactionRequestHeader requestHeader) {",
        ),
        (
            "    public String getBrokerName() {",
            "    /** 返回目标 Broker 名称。 */\n    public String getBrokerName() {",
        ),
        (
            "    public EndTransactionRequestHeader getRequestHeader() {",
            "    /** 返回结束事务请求头。 */\n    public EndTransactionRequestHeader getRequestHeader() {",
        ),
    ],
    "proxy/src/main/java/org/apache/rocketmq/proxy/service/transaction/LocalTransactionService.java": [
        (
            "/**\n * no need to implements, because the channel of producer will put into the broker's producerManager\n */",
            "/**\n * 本地（Broker 内嵌）事务服务：生产者通道已由 Broker producerManager 管理，无需额外心跳订阅。\n */",
        ),
        (
            "public class LocalTransactionService extends AbstractTransactionService {",
            "/** 内嵌 Broker 模式下的 {@link TransactionService} 实现。 */\npublic class LocalTransactionService extends AbstractTransactionService {",
        ),
        (
            "    public LocalTransactionService(BrokerConfig brokerConfig) {",
            "    /** @param brokerConfig 本地 Broker 配置 */\n    public LocalTransactionService(BrokerConfig brokerConfig) {",
        ),
        (
            "    @Override\n    public void addTransactionSubscription(ProxyContext ctx, String group, List<String> topicList) {",
            "    @Override\n    /** 本地模式无需维护事务主题订阅。 */\n    public void addTransactionSubscription(ProxyContext ctx, String group, List<String> topicList) {",
        ),
        (
            "    @Override\n    protected String getBrokerNameByAddr(String brokerAddr) {",
            "    @Override\n    /** 直接返回本地 Broker 名称。 */\n    protected String getBrokerNameByAddr(String brokerAddr) {",
        ),
    ],
    "proxy/src/main/java/org/apache/rocketmq/proxy/service/transaction/TransactionData.java": [
        (
            "public class TransactionData implements Comparable<TransactionData> {",
            "/**\n * 半消息事务元数据：记录 Broker、偏移量、事务 ID 与过期时间。\n */\npublic class TransactionData implements Comparable<TransactionData> {",
        ),
        (
            "    public TransactionData(String brokerName, String topic, long tranStateTableOffset, long commitLogOffset, String transactionId,\n        long checkTimestamp, long expireMs) {",
            "    /**\n     * @param brokerName 存储半消息的 Broker\n     * @param tranStateTableOffset 事务状态表偏移\n     * @param commitLogOffset CommitLog 偏移\n     * @param transactionId 全局事务 ID\n     * @param expireMs 过期毫秒数\n     */\n    public TransactionData(String brokerName, String topic, long tranStateTableOffset, long commitLogOffset, String transactionId,\n        long checkTimestamp, long expireMs) {",
        ),
        (
            "    public long getExpireTime() {",
            "    /** 返回 checkTimestamp + expireMs 的绝对过期时间。 */\n    public long getExpireTime() {",
        ),
        (
            "    @Override\n    public int compareTo(TransactionData o) {",
            "    @Override\n    /** 按过期时间、brokerName、偏移量与 transactionId 排序。 */\n    public int compareTo(TransactionData o) {",
        ),
    ],
}
