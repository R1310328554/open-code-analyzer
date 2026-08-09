"""RocketMQ 5.5.0 wave6b broker processor/schedule/slave/subscription/topic/transaction [15:30] replacements."""

R: dict[str, list[tuple[str, str]]] = {}

R["broker/src/main/java/org/apache/rocketmq/broker/processor/QueryAssignmentProcessor.java"] = [
    (
        "public class QueryAssignmentProcessor implements NettyRequestProcessor {",
        "/**\n * 队列分配查询处理器：响应 QUERY_ASSIGNMENT / SET_MESSAGE_REQUEST_MODE，\n * 按消费模式与负载均衡策略为客户端计算 MessageQueue 分配结果。\n */\npublic class QueryAssignmentProcessor implements NettyRequestProcessor {",
    ),
    (
        "    private final ConcurrentHashMap<String, AllocateMessageQueueStrategy> name2LoadStrategy = new ConcurrentHashMap<>();",
        "    /** 负载均衡策略名 → 策略实例（平均分配、环形平均等）。 */\n    private final ConcurrentHashMap<String, AllocateMessageQueueStrategy> name2LoadStrategy = new ConcurrentHashMap<>();",
    ),
    (
        "        //register strategy\n        //NOTE: init with broker's log instead of init with ClientLogger.getLog();",
        "        // 注册内置负载均衡策略（使用 Broker 日志而非 ClientLogger）",
    ),
    (
        "            case RequestCode.QUERY_ASSIGNMENT:",
        "            case RequestCode.QUERY_ASSIGNMENT:  // 查询客户端队列分配",
    ),
    (
        "            case RequestCode.SET_MESSAGE_REQUEST_MODE:",
        "            case RequestCode.SET_MESSAGE_REQUEST_MODE:  // 设置 Pull/Pop 请求模式",
    ),
    (
        "                // retry topic must be pull mode",
        "                // 重试 Topic 强制 Pull 模式",
    ),
    (
        "     * Returns empty set means the client should clear all load assigned to it before, null means invalid result and the\n     * client should skip the update logic",
        "     * 返回空集表示客户端应清空此前分配；null 表示无效结果，客户端应跳过更新。",
    ),
    (
        "            //each client pop all messagequeue",
        "            // popShareQueueNum 无效时，每个 client Pop 全部逻辑队列",
    ),
    (
        "                //consumer working in pop mode could share the MessageQueues assigned to the N (N = popWorkGroupSize) consumer following it in the cid list",
        "                // Pop 模式下可共享后续 N 个 consumer 的队列分配",
    ),
    (
        "                //make sure each cid is assigned",
        "                // consumer 数多于队列数时，保证每个 cid 至少分到一条队列",
    ),
    (
        "    public List<MessageQueue> allocate4Pop(AllocateMessageQueueStrategy allocateMessageQueueStrategy,",
        "    /** Pop 模式下的队列分配：支持共享队列与 queueId=-1 的全队列 Pop。 */\n    public List<MessageQueue> allocate4Pop(AllocateMessageQueueStrategy allocateMessageQueueStrategy,",
    ),
    (
        "                //must create new MessageQueue in case of change cache in AssignmentManager",
        "                // 必须新建 MessageQueue（queueId=-1），避免 AssignmentManager 缓存被修改",
    ),
    (
        "            response.setRemark(\"retry topic is not allowed to set mode\");",
        "            response.setRemark(\"retry topic is not allowed to set mode\");  // 重试 Topic 禁止切换请求模式",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/processor/QueryMessageProcessor.java"] = [
    (
        "public class QueryMessageProcessor implements NettyRequestProcessor {",
        "/**\n * 消息查询处理器：按 key/时间范围索引查询，或按物理 offset 查看单条消息；\n * 命中结果经 PageCache 零拷贝（QueryMessageTransfer / OneMessageTransfer）回传。\n */\npublic class QueryMessageProcessor implements NettyRequestProcessor {",
    ),
    (
        "            case RequestCode.QUERY_MESSAGE:",
        "            case RequestCode.QUERY_MESSAGE:  // 按索引查询消息",
    ),
    (
        "            case RequestCode.VIEW_MESSAGE_BY_ID:",
        "            case RequestCode.VIEW_MESSAGE_BY_ID:  // 按 commitLog offset 查看单条",
    ),
    (
        "    public RemotingCommand queryMessage(ChannelHandlerContext ctx, RemotingCommand request)",
        "    /** 解析索引类型（key/unique），调用 MessageStore 查询并通过 FileRegion 流式返回。 */\n    public RemotingCommand queryMessage(ChannelHandlerContext ctx, RemotingCommand request)",
    ),
    (
        "        response.setRemark(\"can not find message, maybe time range not correct\");",
        "        response.setRemark(\"can not find message, maybe time range not correct\");  // 索引未命中或时间范围有误",
    ),
    (
        "    public RemotingCommand viewMessageById(ChannelHandlerContext ctx, RemotingCommand request)",
        "    /** 按物理 offset 读取单条消息并零拷贝传输。 */\n    public RemotingCommand viewMessageById(ChannelHandlerContext ctx, RemotingCommand request)",
    ),
    (
        "            response.setRemark(\"can not find message by the offset, \" + requestHeader.getOffset());",
        "            response.setRemark(\"can not find message by the offset, \" + requestHeader.getOffset());  // offset 无效",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/processor/RecallMessageProcessor.java"] = [
    (
        "public class RecallMessageProcessor implements NettyRequestProcessor {",
        "/**\n * 定时消息撤回处理器：校验 recallHandle 后将删除指令写入 Timer 系统 Topic，\n * 由 TimerMessageStore 在目标时刻撤销原定时消息。\n */\npublic class RecallMessageProcessor implements NettyRequestProcessor {",
    ),
    (
        "    private static final String RECALL_MESSAGE_TAG = \"_RECALL_TAG_\";",
        "    /** 撤回指令内部消息 Tag。 */\n    private static final String RECALL_MESSAGE_TAG = \"_RECALL_TAG_\";",
    ),
    (
        "            response.setRemark(\"recall failed, operation is forbidden\");",
        "            response.setRemark(\"recall failed, operation is forbidden\");  // 未开启 recallMessageEnable",
    ),
    (
        "            response.setRemark(\"recall failed, broker service not available\");",
        "            response.setRemark(\"recall failed, broker service not available\");  // Slave 或 Broker 尚未就绪",
    ),
    (
        "            response.setRemark(\"recall failed, timestamp invalid\");",
        "            response.setRemark(\"recall failed, timestamp invalid\");  // 撤回时间戳超出 Timer 允许范围",
    ),
    (
        "    public MessageExtBrokerInner buildMessage(ChannelHandlerContext ctx, RecallMessageRequestHeader requestHeader,",
        "    /** 构造 Timer 删除指令消息：携带 uniqKey、deliverMs 与原 messageId。 */\n    public MessageExtBrokerInner buildMessage(ChannelHandlerContext ctx, RecallMessageRequestHeader requestHeader,",
    ),
    (
        "    public void handlePutMessageResult(PutMessageResult putMessageResult, RemotingCommand request,",
        "    /** 处理落盘结果：成功时更新统计并返回撤回指令 msgId。 */\n    public void handlePutMessageResult(PutMessageResult putMessageResult, RemotingCommand request,",
    ),
    (
        "                    message.getTopic(), putMessageResult.getAppendMessageResult().getMsgNum(), 1); // system timer topic",
        "                    message.getTopic(), putMessageResult.getAppendMessageResult().getMsgNum(), 1); // 系统 Timer Topic 统计",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/processor/ReplyMessageProcessor.java"] = [
    (
        "public class ReplyMessageProcessor extends AbstractSendMessageProcessor {",
        "/**\n * 请求-回复消息处理器：接收 SEND_REPLY_MESSAGE 请求，\n * 按 PROPERTY_MESSAGE_REPLY_TO_CLIENT 将回复推回原始 Producer 通道，可选持久化。\n */\npublic class ReplyMessageProcessor extends AbstractSendMessageProcessor {",
    ),
    (
        "            case RequestCode.SEND_REPLY_MESSAGE_V2:",
        "            case RequestCode.SEND_REPLY_MESSAGE_V2:  // V2 协议回复发送",
    ),
    (
        "            case RequestCode.SEND_REPLY_MESSAGE:",
        "            case RequestCode.SEND_REPLY_MESSAGE:  // V1 协议回复发送",
    ),
    (
        "        PushReplyResult pushReplyResult = this.pushReplyMessage(ctx, requestHeader, msgInner);",
        "        // 先向原 Producer 通道推送回复，再可选落盘",
    ),
    (
        "    private PushReplyResult pushReplyMessage(final ChannelHandlerContext ctx,",
        "    /** 根据 replyToClient 属性查找 Producer 通道并同步推送回复。 */\n    private PushReplyResult pushReplyMessage(final ChannelHandlerContext ctx,",
    ),
    (
        "            //set to zero to avoid client decoding exception",
        "            // msgId/offset 置零，避免客户端解码异常",
    ),
    (
        "    class PushReplyResult {",
        "    /** 回复推送结果：是否成功及失败原因。 */\n    class PushReplyResult {",
    ),
    (
        "        if (this.brokerController.getBrokerConfig().isStoreReplyMessageEnable()) {",
        "        // storeReplyMessageEnable 为 true 时额外持久化回复消息",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/processor/SendMessageCallback.java"] = [
    (
        "public interface SendMessageCallback {",
        "/**\n * 发送消息异步回调：在 Broker 完成发送处理后通知调用方。\n */\npublic interface SendMessageCallback {",
    ),
    (
        "     * On send complete.",
        "     * 发送完成时回调。",
    ),
    (
        "     * @param ctx send context",
        "     * @param ctx 发送追踪上下文",
    ),
    (
        "     * @param response send response",
        "     * @param response 发送响应命令",
    ),
    (
        "    void onComplete(SendMessageContext ctx, RemotingCommand response);",
        "    /** 发送处理结束（成功或失败）时触发。 */\n    void onComplete(SendMessageContext ctx, RemotingCommand response);",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/schedule/DelayOffsetSerializeWrapper.java"] = [
    (
        "public class DelayOffsetSerializeWrapper extends RemotingSerializable {",
        "/**\n * 延迟消息各级别消费位点序列化包装：level → offset 表及 DataVersion。\n */\npublic class DelayOffsetSerializeWrapper extends RemotingSerializable {",
    ),
    (
        "    private ConcurrentMap<Integer /* level */, Long/* offset */> offsetTable =",
        "    /** 延迟级别 → 已消费物理 offset。 */\n    private ConcurrentMap<Integer /* level */, Long/* offset */> offsetTable =",
    ),
    (
        "    private DataVersion dataVersion;",
        "    /** 位点表数据版本，用于主从同步与持久化。 */\n    private DataVersion dataVersion;",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/slave/SlaveSynchronize.java"] = [
    (
        "public class SlaveSynchronize {",
        "/**\n * Slave Broker 元数据同步器：定期从 Master 拉取 Topic、消费位点、延迟位点、\n * 订阅组、MessageRequestMode 及 Timer 相关配置并本地持久化。\n */\npublic class SlaveSynchronize {",
    ),
    (
        "    private volatile String masterAddr = null;",
        "    /** 当前 Master Broker 地址。 */\n    private volatile String masterAddr = null;",
    ),
    (
        "    public void syncAll() {",
        "    /** 依次同步 Topic、消费位点、延迟位点、订阅组、请求模式及 Timer 指标。 */\n    public void syncAll() {",
    ),
    (
        "                    //delete",
        "                    // 删除 Master 已移除的 Topic",
    ),
    (
        "                    //update",
        "                    // 合并 Master 最新 Topic 配置",
    ),
    (
        "    public void syncTimerCheckPoint() {",
        "    /** 同步 Timer 检查点（lastReadTime、masterTimerQueueOffset）。 */\n    public void syncTimerCheckPoint() {",
    ),
    (
        "    private void syncTimerMetrics() {",
        "    /** 同步 Timer 计量指标 timingCount。 */\n    private void syncTimerMetrics() {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/subscription/LmqSubscriptionGroupManager.java"] = [
    (
        "public class LmqSubscriptionGroupManager extends SubscriptionGroupManager {",
        "/**\n * Lite/LMQ 订阅组管理器：LMQ group 动态生成默认配置，禁止持久化变更。\n */\npublic class LmqSubscriptionGroupManager extends SubscriptionGroupManager {",
    ),
    (
        "    public SubscriptionGroupConfig findSubscriptionGroupConfig(final String group) {",
        "    /** LMQ group 返回内存默认配置，否则委托父类查询。 */\n    public SubscriptionGroupConfig findSubscriptionGroupConfig(final String group) {",
    ),
    (
        "    public void updateSubscriptionGroupConfig(final SubscriptionGroupConfig config) {",
        "    /** LMQ group 跳过更新，避免写入持久化表。 */\n    public void updateSubscriptionGroupConfig(final SubscriptionGroupConfig config) {",
    ),
    (
        "    public boolean containsSubscriptionGroup(String group) {",
        "    /** LMQ group 恒视为存在。 */\n    public boolean containsSubscriptionGroup(String group) {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/topic/LmqTopicConfigManager.java"] = [
    (
        "public class LmqTopicConfigManager extends TopicConfigManager {",
        "/**\n * Lite/LMQ Topic 配置管理器：LMQ topic 按需生成单队列读写配置，不参与持久化更新。\n */\npublic class LmqTopicConfigManager extends TopicConfigManager {",
    ),
    (
        "    public TopicConfig selectTopicConfig(final String topic) {",
        "    /** LMQ topic 返回单队列默认配置，否则走常规 Topic 表。 */\n    public TopicConfig selectTopicConfig(final String topic) {",
    ),
    (
        "    private TopicConfig simpleLmqTopicConfig(String topic) {",
        "    /** 构造 LMQ 单队列 TopicConfig（1 读 1 写，读写权限）。 */\n    private TopicConfig simpleLmqTopicConfig(String topic) {",
    ),
    (
        "    public boolean containsTopic(String topic) {",
        "    /** LMQ topic 恒视为存在。 */\n    public boolean containsTopic(String topic) {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/topic/TopicQueueMappingCleanService.java"] = [
    (
        "public class TopicQueueMappingCleanService extends ServiceThread {",
        "/**\n * 静态 Topic 逻辑队列映射清理服务：定时移除已消费完毕的历史映射项，\n * 以及 leader 已迁移到其他 Broker 的冗余映射链。\n */\npublic class TopicQueueMappingCleanService extends ServiceThread {",
    ),
    (
        "        log.info(\"Start topic queue mapping clean service thread!\");",
        "        log.info(\"Start topic queue mapping clean service thread!\");  // 每 5 分钟执行一轮清理",
    ),
    (
        "    public void cleanItemExpired() {",
        "    /** 清理 maxOffset==minOffset 或 maxOffset==0 的最早逻辑队列映射项。 */\n    public void cleanItemExpired() {",
    ),
    (
        "                            //this may should not happen",
        "                            // 正常情况下不应出现 topicOffset 为空",
    ),
    (
        "                        //ignore the maxOffset < 0, which may in case of some error",
        "                        // 忽略 maxOffset<0 的异常统计",
    ),
    (
        "    public void cleanItemListMoreThanSecondGen() {",
        "    /** 当真实 leader 已不在本 Broker 时，删除本地多余的二代以上映射链。 */\n    public void cleanItemListMoreThanSecondGen() {",
    ),
    (
        "                    //find the topic route",
        "                    // 从 NameServer 获取 Topic 路由以定位真实 leader",
    ),
    (
        "                    //fine the real leader",
        "                    // 解析各逻辑队列当前真实 leader Broker",
    ),
    (
        "                        //all the check is ok",
        "                        // 校验通过后，移除 leader 已外迁的本地映射项",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/topic/TopicQueueMappingManager.java"] = [
    (
        "public class TopicQueueMappingManager extends ConfigManager {",
        "/**\n * 静态 Topic 逻辑队列映射管理器：维护 topic → TopicQueueMappingDetail，\n * 支持 epoch 校验、映射合并及 RPC 请求重写（全局 queueId → 物理 queueId）。\n */\npublic class TopicQueueMappingManager extends ConfigManager {",
    ),
    (
        "    //this data version should be equal to the TopicConfigManager",
        "    // 映射 dataVersion 应与 TopicConfigManager 保持一致",
    ),
    (
        "    private final ConcurrentMap<String, TopicQueueMappingDetail> topicQueueMappingTable = new ConcurrentHashMap<>();",
        "    /** topic → 逻辑队列映射详情。 */\n    private final ConcurrentMap<String, TopicQueueMappingDetail> topicQueueMappingTable = new ConcurrentHashMap<>();",
    ),
    (
        "    public void updateTopicQueueMapping(TopicQueueMappingDetail newDetail, boolean force, boolean isClean, boolean flush) throws Exception {",
        "    /** 更新映射：force 合并旧项，isClean 允许清理，flush 控制是否立即持久化。 */\n    public void updateTopicQueueMapping(TopicQueueMappingDetail newDetail, boolean force, boolean isClean, boolean flush) throws Exception {",
    ),
    (
        "                //bakeup the old items",
        "                // force 模式下保留旧队列映射项",
    ),
    (
        "            //do more check",
        "            // 非 force 时校验 epoch、scope 及映射链不可变性",
    ),
    (
        "    //Do not return a null context",
        "    // 永不返回 null，非静态 Topic 时 mappingDetail 为 null",
    ),
    (
        "        // if lo is set to false explicitly, it maybe the forwarded request",
        "        // lo=false 表示转发请求，不做静态 Topic 重写",
    ),
    (
        "            //it is not static topic",
        "            // 非静态 Topic，返回空映射上下文",
    ),
    (
        "        //If not find mappingItem, it encounters some errors",
        "        // 找不到 mappingItem 时返回仅含 globalId 的上下文",
    ),
    (
        "    public  RemotingCommand rewriteRequestForStaticTopic(TopicQueueRequestHeader requestHeader, TopicQueueMappingContext mappingContext) {",
        "    /** 静态 Topic 请求重写：将全局 queueId 替换为 leader 物理 queueId。 */\n    public  RemotingCommand rewriteRequestForStaticTopic(TopicQueueRequestHeader requestHeader, TopicQueueMappingContext mappingContext) {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/topic/TopicRouteInfoManager.java"] = [
    (
        "public class TopicRouteInfoManager {",
        "/**\n * Broker 侧 Topic 路由缓存：定时从 NameServer 拉取路由，维护发布/订阅 MessageQueue 视图，\n * 供 Pop 分配、Escape Bridge 等场景使用。\n */\npublic class TopicRouteInfoManager {",
    ),
    (
        "    private final ConcurrentMap<String/* Topic */, TopicRouteData> topicRouteTable = new ConcurrentHashMap<>();",
        "    /** Topic → 完整路由数据（含 queue mapping）。 */\n    private final ConcurrentMap<String/* Topic */, TopicRouteData> topicRouteTable = new ConcurrentHashMap<>();",
    ),
    (
        "    private final ConcurrentMap<String/* Broker Name */, HashMap<Long/* brokerId */, String/* address */>> brokerAddrTable =",
        "    /** Broker 名 → (brokerId → 地址) 映射。 */\n    private final ConcurrentMap<String/* Broker Name */, HashMap<Long/* brokerId */, String/* address */>> brokerAddrTable =",
    ),
    (
        "    private final ConcurrentHashMap<String, Set<MessageQueue>> topicSubscribeInfoTable = new ConcurrentHashMap<>();",
        "    /** Topic → 订阅侧 MessageQueue 集合（Pop 分配用）。 */\n    private final ConcurrentHashMap<String, Set<MessageQueue>> topicSubscribeInfoTable = new ConcurrentHashMap<>();",
    ),
    (
        "    public void start() {",
        "    /** 启动定时任务，按配置间隔从 NameServer 刷新路由。 */\n    public void start() {",
    ),
    (
        "                        // clean no used topic",
        "                        // Topic 不存在时清理本地订阅缓存",
    ),
    (
        "    public TopicPublishInfo tryToFindTopicPublishInfo(final String topic) {",
        "    /** 查找发布路由，本地无效时触发 NameServer 拉取。 */\n    public TopicPublishInfo tryToFindTopicPublishInfo(final String topic) {",
    ),
    (
        "    public Set<MessageQueue> getTopicSubscribeInfo(String topic) {",
        "    /** 获取订阅 MessageQueue 集合，缺失时主动刷新路由。 */\n    public Set<MessageQueue> getTopicSubscribeInfo(String topic) {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/transaction/AbstractTransactionalMessageCheckListener.java"] = [
    (
        "public abstract class AbstractTransactionalMessageCheckListener {",
        "/**\n * 事务消息回查监听器抽象基类：异步向 Producer 发送 CHECK_TRANSACTION_STATE 请求，\n * 子类实现 resolveDiscardMsg 处理超限回查消息。\n */\npublic abstract class AbstractTransactionalMessageCheckListener {",
    ),
    (
        "    //queue nums of topic TRANS_CHECK_MAX_TIME_TOPIC",
        "    // TRANS_CHECK_MAX_TIME_TOPIC 队列数",
    ),
    (
        "    public void sendCheckMessage(MessageExt msgExt) throws Exception {",
        "    /** 构造回查请求头，恢复真实 topic/queueId 并向 Producer 通道发送。 */\n    public void sendCheckMessage(MessageExt msgExt) throws Exception {",
    ),
    (
        "    public void resolveHalfMsg(final MessageExt msgExt) {",
        "    /** 在线程池中异步执行半消息回查。 */\n    public void resolveHalfMsg(final MessageExt msgExt) {",
    ),
    (
        "     * Inject brokerController for this listener",
        "     * 注入 BrokerController 并初始化回查线程池",
    ),
    (
        "     * In order to avoid check back unlimited, we will discard the message that have been checked more than a certain\n     * number of times.",
        "     * 避免无限回查：超过最大回查次数时丢弃半消息（由子类实现）。",
    ),
    (
        "     * @param msgExt Message to be discarded.",
        "     * @param msgExt 待丢弃的半消息",
    ),
    (
        "    public abstract void resolveDiscardMsg(MessageExt msgExt);",
        "    /** 回查次数超限时丢弃半消息的具体实现。 */\n    public abstract void resolveDiscardMsg(MessageExt msgExt);",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/transaction/OperationResult.java"] = [
    (
        "public class OperationResult {",
        "/**\n * 事务操作结果：携带半消息、响应码与备注，供 EndTransaction 等流程使用。\n */\npublic class OperationResult {",
    ),
    (
        "    private MessageExt prepareMessage;",
        "    /** 关联的半事务（prepare）消息。 */\n    private MessageExt prepareMessage;",
    ),
    (
        "    private int responseCode;",
        "    /** Remoting 响应码。 */\n    private int responseCode;",
    ),
    (
        "    private String responseRemark;",
        "    /** 响应备注或错误描述。 */\n    private String responseRemark;",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/transaction/TransactionMetrics.java"] = [
    (
        "public class TransactionMetrics extends ConfigManager {",
        "/**\n * 事务消息计量：按 Topic 累计半消息/事务消息数量及时间戳，持久化到独立配置文件。\n */\npublic class TransactionMetrics extends ConfigManager {",
    ),
    (
        "    private ConcurrentMap<String, Metric> transactionCounts =",
        "    /** Topic → 事务消息计数与时间戳。 */\n    private ConcurrentMap<String, Metric> transactionCounts =",
    ),
    (
        "    public long addAndGet(String topic, int value) {",
        "    /** 递增指定 Topic 事务计数并刷新 dataVersion。 */\n    public long addAndGet(String topic, int value) {",
    ),
    (
        "    public void cleanMetrics(Set<String> topics) {",
        "    /** 清理指定 Topic 集合中的事务计量（跳过系统 Topic）。 */\n    public void cleanMetrics(Set<String> topics) {",
    ),
    (
        "            // in the input topics set, then remove it.",
        "            // 命中输入 Topic 集合时移除对应计量项",
    ),
    (
        "    public static class TransactionMetricsSerializeWrapper extends RemotingSerializable {",
        "    /** 事务计量持久化包装：transactionCount 表 + dataVersion。 */\n    public static class TransactionMetricsSerializeWrapper extends RemotingSerializable {",
    ),
    (
        "            // bak metrics file",
        "            // 先原子备份旧 metrics 文件",
    ),
    (
        "                // sync the directory, ensure that the bak file is visible",
        "                // fsync 目录，确保备份文件可见",
    ),
    (
        "            // persist metrics file",
        "            // 写入新 metrics 并 force 刷盘",
    ),
    (
        "    public static class Metric {",
        "    /** 单 Topic 事务计数（AtomicLong）及最后更新时间戳。 */\n    public static class Metric {",
    ),
]
