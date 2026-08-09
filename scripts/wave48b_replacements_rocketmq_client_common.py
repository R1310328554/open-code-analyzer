"""RocketMQ 5.5.0 wave48b client/common/container [15:30] Chinese annotation replacements."""

R: dict[str, list[tuple[str, str]]] = {}

# --- ProcessQueue ---
R["client/src/main/java/org/apache/rocketmq/client/impl/consumer/ProcessQueue.java"] = [
    (
        "/**\n * Queue consumption snapshot\n */\npublic class ProcessQueue {",
        "/**\n * 队列消费快照：维护单个 {@link MessageQueue} 上已拉取但未提交 offset 的消息缓冲。\n * 并发消费使用 {@link #msgTreeMap}；顺序消费额外使用 {@link #consumingMsgOrderlyTreeMap}\n * 配合 {@link #takeMessages}/{@link #commit}/{@link #rollback} 实现两阶段提交。\n */\npublic class ProcessQueue {",
    ),
    (
        "    public final static long REBALANCE_LOCK_MAX_LIVE_TIME =",
        "    /** 顺序消费 Broker 端队列锁的最大存活时间（毫秒），超时需续锁。 */\n    public final static long REBALANCE_LOCK_MAX_LIVE_TIME =",
    ),
    (
        "    private final static long PULL_MAX_IDLE_TIME = Long.parseLong(System.getProperty(\"rocketmq.client.pull.pullMaxIdleTime\", \"120000\"));",
        "    /** Pull 空闲超过该毫秒数视为过期，Rebalance 时可回收 ProcessQueue。 */\n    private final static long PULL_MAX_IDLE_TIME = Long.parseLong(System.getProperty(\"rocketmq.client.pull.pullMaxIdleTime\", \"120000\"));",
    ),
    (
        "    private final TreeMap<Long, MessageExt> msgTreeMap = new TreeMap<>();",
        "    /** queueOffset → 待消费消息（并发/顺序共用主缓冲）。 */\n    private final TreeMap<Long, MessageExt> msgTreeMap = new TreeMap<>();",
    ),
    (
        "    /**\n     * A subset of msgTreeMap, will only be used when orderly consume\n     */",
        "    /** 顺序消费专用：已从 msgTreeMap 取出、正在消费中的消息子集。 */",
    ),
    (
        "    public boolean isLockExpired() {",
        "    /** 顺序消费队列锁是否已过期（超过 REBALANCE_LOCK_MAX_LIVE_TIME）。 */\n    public boolean isLockExpired() {",
    ),
    (
        "    /**\n     * @param pushConsumer\n     */",
        "    /**\n     * 清理超时未消费消息：并发模式下将队首超时消息 SendBack（重试级别 3）。\n     * 顺序消费不执行此逻辑。\n     *\n     * @param pushConsumer Push 消费者实例\n     */",
    ),
    (
        "    public boolean putMessage(final List<MessageExt> msgs) {",
        "    /** 写入 Pull 到的消息；若此前未在消费中则返回 true 触发 ConsumeMessageService 调度。 */\n    public boolean putMessage(final List<MessageExt> msgs) {",
    ),
    (
        "    public long removeMessage(final List<MessageExt> msgs) {",
        "    /** 消费成功后移除消息并返回下一待提交 offset（并发模式）。 */\n    public long removeMessage(final List<MessageExt> msgs) {",
    ),
    (
        "    public void rollback() {",
        "    /** 顺序消费失败：将 consumingMsgOrderlyTreeMap 中的消息归还 msgTreeMap。 */\n    public void rollback() {",
    ),
    (
        "    public long commit() {",
        "    /** 顺序消费成功：清空 consumingMsgOrderlyTreeMap 并返回下一 offset。 */\n    public long commit() {",
    ),
    (
        "    public List<MessageExt> takeMessages(final int batchSize) {",
        "    /** 顺序消费：从 msgTreeMap 取出最多 batchSize 条消息移入 consuming 子集。 */\n    public List<MessageExt> takeMessages(final int batchSize) {",
    ),
    (
        "    /**\n     * Return the result that whether current message is exist in the process queue or not.\n     */",
        "    /** 判断指定消息是否仍存在于 ProcessQueue 缓冲中。 */",
    ),
    (
        "            // should never reach here.",
        "            // 正常不应传入 null",
    ),
]

# --- RebalanceImpl ---
R["client/src/main/java/org/apache/rocketmq/client/impl/consumer/RebalanceImpl.java"] = [
    (
        "public abstract class RebalanceImpl {",
        "/**\n * 消费端 Rebalance 抽象基类：维护 MessageQueue ↔ ProcessQueue 映射，\n * 执行队列分配/回收、Broker 端 lock/unlock，并协调 Pull/POP 请求下发。\n * 子类 {@link RebalancePushImpl}、{@link RebalancePullImpl} 等实现具体策略。\n */\npublic abstract class RebalanceImpl {",
    ),
    (
        "    protected final ConcurrentMap<MessageQueue, ProcessQueue> processQueueTable = new ConcurrentHashMap<>(64);",
        "    /** Push/Pull 模式下 MessageQueue → ProcessQueue 映射。 */\n    protected final ConcurrentMap<MessageQueue, ProcessQueue> processQueueTable = new ConcurrentHashMap<>(64);",
    ),
    (
        "    protected final ConcurrentMap<MessageQueue, PopProcessQueue> popProcessQueueTable = new ConcurrentHashMap<>(64);",
        "    /** POP 模式下 MessageQueue → PopProcessQueue 映射。 */\n    protected final ConcurrentMap<MessageQueue, PopProcessQueue> popProcessQueueTable = new ConcurrentHashMap<>(64);",
    ),
    (
        "    private static final int QUERY_ASSIGNMENT_TIMEOUT = 3000;",
        "    /** 向 Broker 查询 MessageQueueAssignment 的超时（毫秒）。 */\n    private static final int QUERY_ASSIGNMENT_TIMEOUT = 3000;",
    ),
    (
        "    public void unlock(final MessageQueue mq, final boolean oneway) {",
        "    /** 向 Broker 解锁单个 MessageQueue（顺序消费释放队列锁）。 */\n    public void unlock(final MessageQueue mq, final boolean oneway) {",
    ),
    (
        "    public boolean lock(final MessageQueue mq) {",
        "    /** 向 Broker 申请锁定 MessageQueue，顺序消费 Pull 前必须成功。 */\n    public boolean lock(final MessageQueue mq) {",
    ),
    (
        "    public boolean doRebalance(final boolean isOrder) {",
        "    /** Rebalance 主入口：遍历订阅 topic 并执行 rebalanceByTopic。 */\n    public boolean doRebalance(final boolean isOrder) {",
    ),
    (
        "        // null means invalid result, we should skip the update logic",
        "        // null 表示 Broker 返回无效，跳过本次 ProcessQueue 更新",
    ),
    (
        "        // drop process queues no longer belong me",
        "        // 标记不再归属本消费者的 ProcessQueue 为 dropped",
    ),
    (
        "        // remove message queues no longer belong me",
        "        // 移除不再归属本消费者的 MessageQueue 条目",
    ),
    (
        "        // add new message queue",
        "        // 为新分配的 MessageQueue 创建 ProcessQueue 并触发 offset 计算",
    ),
    (
        "                //pop switch to push",
        "                // POP 切换为 Push：订阅 POP 重试 topic",
    ),
    (
        "                //push switch to pop",
        "                // Push 切换为 POP：取消 POP 重试 topic 订阅",
    ),
    (
        "                        //the queue is no longer your assignment",
        "                        // 该队列已不在 Broker 分配结果中",
    ),
    (
        "    /**\n     * When the network is unstable, using this interface may return wrong offset.\n     * It is recommended to use computePullFromWhereWithException instead.\n     * @param mq\n     * @return offset\n     */",
        "    /**\n     * 计算 Pull 起始 offset；网络不稳定时可能返回错误值。\n     * 推荐使用 {@link #computePullFromWhereWithException}。\n     *\n     * @param mq 目标 MessageQueue\n     * @return Pull 起始 offset\n     */",
    ),
    (
        "    public void removeProcessQueue(final MessageQueue mq) {",
        "    /** 修正 offset 时移除多余 ProcessQueue 并标记 dropped。 */\n    public void removeProcessQueue(final MessageQueue mq) {",
    ),
]

# --- MQClientInstance ---
R["client/src/main/java/org/apache/rocketmq/client/impl/factory/MQClientInstance.java"] = [
    (
        "public class MQClientInstance {",
        "/**\n * RocketMQ 客户端实例：同一 clientId 下共享 Netty 连接、路由表、心跳与 Rebalance 服务。\n * 管理 producerTable/consumerTable/adminExtTable，是客户端侧的核心调度中心。\n */\npublic class MQClientInstance {",
    ),
    (
        "    /**\n     * The container of the producer in the current client. The key is the name of producerGroup.\n     */",
        "    /** 当前客户端内的 Producer 容器，key 为 producerGroup。 */",
    ),
    (
        "    /**\n     * The container of the consumer in the current client. The key is the name of consumerGroup.\n     */",
        "    /** 当前客户端内的 Consumer 容器，key 为 consumerGroup。 */",
    ),
    (
        "    /**\n     * The container of the adminExt in the current client. The key is the name of adminExtGroup.\n     */",
        "    /** 当前客户端内的 AdminExt 容器，key 为 adminExtGroup。 */",
    ),
    (
        "    /**\n     * The container which stores the brokerClusterInfo. The key of the map is the broker name.\n     * And the value is the broker instance list that belongs to the broker cluster.\n     * For the sub map, the key is the id of single broker instance, and the value is the address.\n     */",
        "    /**\n     * Broker 集群地址表：brokerName → (brokerId → address)。\n     * 由 NameServer 路由更新与心跳维护。\n     */",
    ),
    (
        "    private final ConcurrentMap<String/* Topic */, TopicRouteData> topicRouteTable = new ConcurrentHashMap<>();",
        "    /** topic → 路由信息（Broker 列表、Queue 读写权限等）。 */\n    private final ConcurrentMap<String/* Topic */, TopicRouteData> topicRouteTable = new ConcurrentHashMap<>();",
    ),
    (
        "    private ServiceState serviceState = ServiceState.CREATE_JUST;",
        "    /** 客户端生命周期状态（CREATE_JUST/RUNNING/SHUTDOWN_ALREADY 等）。 */\n    private ServiceState serviceState = ServiceState.CREATE_JUST;",
    ),
    (
        "        // TO DO should check the usage of raw route, it is better to remove such field",
        "        // TODO：评估 raw route 字段用途，后续版本考虑移除",
    ),
    (
        "        // If not specified,looking address from name server",
        "        // 未显式指定 namesrvAddr 时从 NameServer 拉取地址",
    ),
    (
        "        // Start request-response channel",
        "        // 启动 Netty 请求-响应通道",
    ),
    (
        "        // Start various schedule tasks",
        "        // 启动定时任务（路由更新、心跳、持久化 offset 等）",
    ),
    (
        "        // Start pull service",
        "        // 启动 PullMessageService 后台线程",
    ),
    (
        "        // Start rebalance service",
        "        // 启动 RebalanceService 后台线程",
    ),
    (
        "    /**\n     * Remove offline broker\n     */",
        "    /** 移除已下线的 Broker 地址并清理相关路由缓存。 */",
    ),
    (
        "        // assume that the configs of every broker in cluster are the same.",
        "        // 假设同一集群内各 Broker 配置一致",
    ),
    (
        "        // wait all tasks finish",
        "        // 等待异步任务全部完成后再返回",
    ),
]

# --- MQClientAPIExt ---
R["client/src/main/java/org/apache/rocketmq/client/impl/mqclient/MQClientAPIExt.java"] = [
    (
        "public class MQClientAPIExt extends MQClientAPIImpl {",
        "/**\n * 客户端 Remoting API 扩展：在 {@link MQClientAPIImpl} 基础上封装\n * Proxy/Lite/POP 等新协议请求，并提供 {@link MqClientAdminImpl} 管理接口。\n * 供 {@link MQClientInstance} 调用 Broker/Proxy。\n */\npublic class MQClientAPIExt extends MQClientAPIImpl {",
    ),
    (
        "    private final MqClientAdminImpl mqClientAdmin;",
        "    /** 客户端管理 API 实现（topic/订阅/offset 等运维操作）。 */\n    private final MqClientAdminImpl mqClientAdmin;",
    ),
    (
        "    public boolean updateNameServerAddressList() {",
        "    /** 更新 NameServer 地址列表；优先使用 clientConfig 中显式配置的 namesrvAddr。 */\n    public boolean updateNameServerAddressList() {",
    ),
    (
        "    public CompletableFuture<Void> sendHeartbeatOneway(",
        "    /** 向 Broker 发送 Oneway 心跳（不等待响应）。 */\n    public CompletableFuture<Void> sendHeartbeatOneway(",
    ),
    (
        "    public CompletableFuture<Integer> sendHeartbeatAsync(",
        "    /** 异步发送心跳并返回 Broker 协议版本号。 */\n    public CompletableFuture<Integer> sendHeartbeatAsync(",
    ),
    (
        "    public CompletableFuture<SendResult> sendMessageAsync(",
        "    /** 异步发送消息到 Broker。 */\n    public CompletableFuture<SendResult> sendMessageAsync(",
    ),
    (
        "    public CompletableFuture<PopResult> popMessageAsync(",
        "    /** 异步 POP 消息。 */\n    public CompletableFuture<PopResult> popMessageAsync(",
    ),
    (
        "    public CompletableFuture<PullResult> pullMessageAsync(",
        "    /** 异步 Pull 消息。 */\n    public CompletableFuture<PullResult> pullMessageAsync(",
    ),
    (
        "    public MqClientAdminImpl getMqClientAdmin() {",
        "    /** 返回客户端 Admin API 实例。 */\n    public MqClientAdminImpl getMqClientAdmin() {",
    ),
]

# --- DefaultMQProducerImpl ---
R["client/src/main/java/org/apache/rocketmq/client/impl/producer/DefaultMQProducerImpl.java"] = [
    (
        "public class DefaultMQProducerImpl implements MQProducerInner {",
        "/**\n * 默认 Producer 内部实现：路由选择、同步/异步/Oneway 发送、\n * 事务消息二阶段、延迟/顺序消息及 SendMessageHook 回调。\n * 由 {@link DefaultMQProducer} 委托调用。\n */\npublic class DefaultMQProducerImpl implements MQProducerInner {",
    ),
    (
        "    /**\n     * DEFAULT ASYNC -------------------------------------------------------\n     */",
        "    /** 默认异步发送（由客户端选择队列） ------------------------------------------------------- */",
    ),
    (
        "    /**\n     * DEFAULT ONEWAY -------------------------------------------------------\n     */",
        "    /** 默认 Oneway 发送（不等待 Broker 响应） ------------------------------------------------------- */",
    ),
    (
        "    /**\n     * KERNEL SYNC -------------------------------------------------------\n     */",
        "    /** 内核同步发送（指定 MessageQueue） ------------------------------------------------------- */",
    ),
    (
        "    /**\n     * KERNEL ASYNC -------------------------------------------------------\n     */",
        "    /** 内核异步发送（指定 MessageQueue） ------------------------------------------------------- */",
    ),
    (
        "    /**\n     * KERNEL ONEWAY -------------------------------------------------------\n     */",
        "    /** 内核 Oneway 发送（指定 MessageQueue） ------------------------------------------------------- */",
    ),
    (
        "    // backpressure related",
        "    // 背压相关：限制异步发送并发数与在途消息大小",
    ),
    (
        "            //Reset topic with namespace during resend.",
        "            // 重发时按 namespace 重置 topic",
    ),
    (
        "        // Get the maximum timeout allowed per request",
        "        // 获取单次请求允许的最大超时",
    ),
    (
        "        // Determine if retries are still possible",
        "        // 判断是否仍可重试",
    ),
    (
        "            // Set this broker unreachable when detecting schedule task is running for RemotingException.",
        "            // 定时任务场景下 RemotingException 时将 Broker 标记为不可达",
    ),
    (
        "            //for MessageBatch,ID has been set in the generating process",
        "            // MessageBatch 的 msgId 已在组批阶段生成",
    ),
    (
        "            //If msg body was compressed, msgbody should be reset using prevBody.",
        "            // 若消息体已压缩，需用 prevBody 还原后再重试",
    ),
    (
        "            //batch does not support compressing right now",
        "            // 批量发送暂不支持压缩",
    ),
    (
        "            // find another address to support multi proxy endpoints,",
        "            // 多 Proxy 端点场景下尝试备用地址",
    ),
]

# --- DefaultMQProducer ---
R["client/src/main/java/org/apache/rocketmq/client/producer/DefaultMQProducer.java"] = [
    (
        "/**\n * This class is the entry point for applications intending to send messages. </p>\n * <p>\n * It's fine to tune fields which exposes getter/setter methods, but keep in mind, all of them should work well out of\n * box for most scenarios. </p>\n * <p>\n * This class aggregates various <code>send</code> methods to deliver messages to broker(s). Each of them has pros and\n * cons; you'd better understand strengths and weakness of them before actually coding. </p>\n *\n * <p> <strong>Thread Safety:</strong> After configuring and starting process, this class can be regarded as thread-safe\n * and used among multiple threads context. </p>\n */",
        "/**\n * 应用发送消息的入口类。\n * <p>\n * 可通过 getter/setter 调整配置，但多数场景下默认参数即可满足需求。\n * 本类聚合多种 {@code send} 方法向 Broker 投递消息，各有适用场景，使用前建议理解差异。\n * </p>\n * <p>\n * <strong>线程安全：</strong>配置并 start 后可在多线程环境下使用。\n * </p>\n */",
    ),
    (
        "    /**\n     * Wrapping internal implementations for virtually all methods presented in this class.\n     */",
        "    /** 封装本类几乎全部方法的内部实现 {@link DefaultMQProducerImpl}。 */",
    ),
    (
        "    /**\n     * Producer group conceptually aggregates all producer instances of exactly same role, which is particularly\n     * important when transactional messages are involved. </p>\n     * <p>\n     * For non-transactional messages, it does not matter as long as it's unique per process. </p>\n     * <p>\n     * See <a href=\"https://rocketmq.apache.org/docs/introduction/02concepts\">core concepts</a> for more discussion.\n     */",
        "    /**\n     * Producer 组：聚合职责相同的 Producer 实例，事务消息场景下尤为重要。\n     * <p>非事务消息在同一进程内保持唯一即可。</p>\n     * <p>详见 <a href=\"https://rocketmq.apache.org/docs/introduction/02concepts\">核心概念</a>。</p>\n     */",
    ),
    (
        "    /**\n     * Topics that need to be initialized for transaction producer\n     */",
        "    /** 事务 Producer 需要预先初始化的 topic 集合。 */",
    ),
    (
        "    /**\n     * Just for testing or demo program\n     */",
        "    /** 仅用于测试或 Demo 程序。 */",
    ),
    (
        "    /**\n     * Number of queues to create per default topic.\n     */",
        "    /** 自动创建 topic 时的默认队列数。 */",
    ),
    (
        "    /**\n     * Timeout for sending messages.\n     */",
        "    /** 发送消息默认超时（毫秒）。 */",
    ),
    (
        "    /**\n     * Max timeout for sending messages per request.\n     */",
        "    /** 单次发送请求允许的最大超时（毫秒）。 */",
    ),
    (
        "        //if client open the message trace feature",
        "        // 客户端开启消息轨迹功能时注册 Trace Hook",
    ),
    (
        "        // produceAccumulator is full",
        "        // ProduceAccumulator 缓冲已满，降级为直接发送",
    ),
    (
        "        // delay message do not support batch processing",
        "        // 延迟消息不支持批量聚合",
    ),
    (
        "        // retry message do not support batch processing",
        "        // 重试消息不支持批量聚合",
    ),
    (
        "        // send in sync mode",
        "        // 以同步模式发送",
    ),
]

# --- ProduceAccumulator ---
R["client/src/main/java/org/apache/rocketmq/client/producer/ProduceAccumulator.java"] = [
    (
        "public class ProduceAccumulator {",
        "/**\n * 发送消息聚合器：将同 topic+MessageQueue 的小消息在内存中攒批，\n * 达到 holdSize/holdMs 阈值后合并为 {@link MessageBatch} 发送，降低网络开销。\n * 由 {@link MQClientManager} 按 clientId 复用。\n */\npublic class ProduceAccumulator {",
    ),
    (
        "    // totalHoldSize normal value",
        "    // 全局缓冲上限默认值 32MB",
    ),
    (
        "    // holdSize normal value",
        "    // 单批聚合大小阈值默认值 32KB",
    ),
    (
        "    // holdMs normal value",
        "    // 单批最大等待时间默认值 10ms",
    ),
    (
        "    private final Map<AggregateKey, MessageAccumulation> syncSendBatchs = new ConcurrentHashMap<AggregateKey, MessageAccumulation>();",
        "    /** 同步发送聚合缓冲：AggregateKey → 待发送批次。 */\n    private final Map<AggregateKey, MessageAccumulation> syncSendBatchs = new ConcurrentHashMap<AggregateKey, MessageAccumulation>();",
    ),
    (
        "    private final Map<AggregateKey, MessageAccumulation> asyncSendBatchs = new ConcurrentHashMap<AggregateKey, MessageAccumulation>();",
        "    /** 异步发送聚合缓冲。 */\n    private final Map<AggregateKey, MessageAccumulation> asyncSendBatchs = new ConcurrentHashMap<AggregateKey, MessageAccumulation>();",
    ),
    (
        "    void start() {",
        "    /** 启动同步/异步 Guard 守护线程，定时 flush 超时批次。 */\n    void start() {",
    ),
    (
        "    void shutdown() {",
        "    /** 关闭 Guard 线程并 flush 剩余批次。 */\n    void shutdown() {",
    ),
    (
        "    SendResult send(Message msg,\n        DefaultMQProducer defaultMQProducer)",
        "    /** 同步发送：尝试将消息追加到聚合批次，满批则立即发送。 */\n    SendResult send(Message msg,\n        DefaultMQProducer defaultMQProducer)",
    ),
    (
        "    private class GuardForSyncSendService extends ServiceThread {",
        "    /** 同步发送 Guard：周期性检查 syncSendBatchs 并 flush 超时批次。 */\n    private class GuardForSyncSendService extends ServiceThread {",
    ),
    (
        "    private class GuardForAsyncSendService extends ServiceThread {",
        "    /** 异步发送 Guard：周期性检查 asyncSendBatchs 并 flush 超时批次。 */\n    private class GuardForAsyncSendService extends ServiceThread {",
    ),
]

# --- AsyncTraceDispatcher ---
R["client/src/main/java/org/apache/rocketmq/client/trace/AsyncTraceDispatcher.java"] = [
    (
        "public class AsyncTraceDispatcher implements TraceDispatcher {",
        "/**\n * 异步消息轨迹分发器：将 Send/Consume 轨迹上下文写入内存队列，\n * 后台线程批量序列化后发送到 RMQ_SYS_TRACE_TOPIC。\n * 实现 {@link TraceDispatcher}，供 Producer/Consumer 注册 Trace Hook。\n */\npublic class AsyncTraceDispatcher implements TraceDispatcher {",
    ),
    (
        "    private final ArrayBlockingQueue<TraceContext> traceContextQueue;",
        "    /** 待上报的轨迹上下文队列（容量 2048）。 */\n    private final ArrayBlockingQueue<TraceContext> traceContextQueue;",
    ),
    (
        "    private DefaultMQProducerImpl hostProducer;",
        "    /** 宿主 Producer 实现（Producer 侧轨迹）。 */\n    private DefaultMQProducerImpl hostProducer;",
    ),
    (
        "    private DefaultMQPushConsumerImpl hostConsumer;",
        "    /** 宿主 Push Consumer 实现（Consumer 侧轨迹）。 */\n    private DefaultMQPushConsumerImpl hostConsumer;",
    ),
    (
        "        this.batchNum = Math.min(batchNum, 20);/* max value 20*/",
        "        this.batchNum = Math.min(batchNum, 20);/* 单批最多 20 条轨迹 */",
    ),
    (
        "        // The max size of message is 128K",
        "        // 单条轨迹消息最大 128KB",
    ),
    (
        "                // ignore - VM is already shutting down",
        "                // 忽略：JVM 已在关闭",
    ),
    (
        "        // To prevent an infinite loop, add a wait time between each two task executions",
        "        // 防止无限循环，两次任务执行间加入等待",
    ),
    (
        "            // Keyset of message trace includes msgId of or original message",
        "            // 轨迹 keySet 包含原始消息的 msgId",
    ),
    (
        "            // No cross set",
        "            // 禁止跨 set 发送轨迹",
    ),
    (
        "        /**\n         * Send message trace data\n         *\n         * @param keySet     the keyset in this batch(including msgId in original message not offsetMsgId)\n         * @param data       the message trace data in this batch\n         * @param traceTopic the topic which message trace data will send to\n         */",
        "        /**\n         * 通过 MQ 发送一批消息轨迹数据。\n         *\n         * @param keySet 本批 key 集合（含原始 msgId，非 offsetMsgId）\n         * @param data 本批轨迹 JSON 数据\n         * @param traceTopic 轨迹数据目标 topic\n         */",
    ),
]

# --- BrokerConfig ---
R["common/src/main/java/org/apache/rocketmq/common/BrokerConfig.java"] = [
    (
        "public class BrokerConfig extends BrokerIdentity {",
        "/**\n * Broker 运行时配置：监听端口、线程池规模、NameServer 交互间隔、\n * POP/Lite/事务/轨迹/ACL 等特性开关。继承 {@link BrokerIdentity} 的集群与 brokerId 信息。\n * 标注 {@link ImportantField} 的字段变更需持久化并同步。\n */\npublic class BrokerConfig extends BrokerIdentity {",
    ),
    (
        "    /**\n     * Listen port for single broker\n     */",
        "    /** 单 Broker 监听端口（默认 6888）。 */",
    ),
    (
        "    /**\n     * thread numbers for send message thread pool.\n     */",
        "    /** SendMessageProcessor 线程池大小。 */",
    ),
    (
        "    /**\n     * Thread numbers for EndTransactionProcessor\n     */",
        "    /** EndTransactionProcessor 线程池大小（事务消息二阶段）。 */",
    ),
    (
        "    /**\n     * This configurable item defines interval of topics registration of broker to name server. Allowing values are\n     * between 10,000 and 60,000 milliseconds.\n     */",
        "    /** Broker 向 NameServer 注册 topic 的间隔（毫秒，允许 10000~60000）。 */",
    ),
    (
        "    /**\n     * This configurable item defines interval of update name server address. Default: 120 * 1000 milliseconds\n     */",
        "    /** 从 NameServer 拉取地址列表的间隔（默认 120 秒）。 */",
    ),
    (
        "    /**\n     * the interval to send heartbeat to name server for liveness detection.\n     */",
        "    /** 向 NameServer 发送心跳的间隔（存活检测）。 */",
    ),
    (
        "    /**\n     * How long the broker will be considered as inactive by nameserver since last heartbeat. Effective only if\n     * enableSlaveActingMaster is true\n     */",
        "    /** 自上次心跳起超过该毫秒数，NameServer 视 Broker 为非活跃（enableSlaveActingMaster 时生效）。 */",
    ),
    (
        "    /**\n     * the interval of pulling topic information from the named server\n     */",
        "    /** 从 NameServer 拉取 topic 路由信息的间隔。 */",
    ),
    (
        "    // Switch of filter bit map calculation.",
        "    // 消费队列 Filter 位图计算开关",
    ),
    (
        "    //Reject the pull consumer instance to pull messages from broker.",
        "    // 拒绝 Pull 消费者从本 Broker 拉取消息",
    ),
    (
        "    // Error rate of bloom filter, 1~100.",
        "    // Bloom 过滤器误判率，取值 1~100",
    ),
    (
        "    // read message from pop retry topic v1, for the compatibility, will be removed in the future version",
        "    // 兼容读取 POP 重试 topic v1，后续版本将移除",
    ),
    (
        "    // each message queue will have a corresponding retry queue",
        "    // 每个 MessageQueue 对应独立 POP 重试队列",
    ),
    (
        "    // make sense for rocksdb store",
        "    // 仅 RocksDB 存储模式下有意义",
    ),
]

# --- MQVersion ---
R["common/src/main/java/org/apache/rocketmq/common/MQVersion.java"] = [
    (
        "public class MQVersion {",
        "/**\n * RocketMQ 协议版本号管理：{@link Version} 枚举按 ordinal 递增，\n * 用于 Broker/Client 心跳与特性协商。{@link #CURRENT_VERSION} 对应当前发行版。\n */\npublic class MQVersion {",
    ),
    (
        "    public static final int CURRENT_VERSION = Version.V5_5_0.ordinal();",
        "    /** 当前代码库对应的协议版本 ordinal（V5_5_0）。 */\n    public static final int CURRENT_VERSION = Version.V5_5_0.ordinal();",
    ),
    (
        "    public static String getVersionDesc(int value) {",
        "    /** 将版本 ordinal 转为枚举名称字符串；超出范围返回最高已知版本。 */\n    public static String getVersionDesc(int value) {",
    ),
    (
        "    public static Version value2Version(int value) {",
        "    /** 将版本 ordinal 转为 {@link Version} 枚举；超出范围返回最高已知版本。 */\n    public static Version value2Version(int value) {",
    ),
    (
        "    public enum Version {",
        "    /** 历史版本枚举：从 V3_0_0 到 V5_9_9，末尾 HIGHER_VERSION 表示未知更高版本。 */\n    public enum Version {",
    ),
    (
        "        HIGHER_VERSION",
        "        /** 高于当前代码库已知版本的占位符。 */\n        HIGHER_VERSION",
    ),
]

# --- MixAll ---
R["common/src/main/java/org/apache/rocketmq/common/MixAll.java"] = [
    (
        "public class MixAll {",
        "/**\n * RocketMQ 全局常量与工具方法：环境变量、默认 Group/Topic 前缀、\n * 重试/DLQ topic 命名、本机地址解析及配置文件读写等。\n */\npublic class MixAll {",
    ),
    (
        "    /**\n     * unify the home dir\n     */",
        "    /** 统一的 RocketMQ 安装目录（系统属性优先于环境变量）。 */",
    ),
    (
        "    public static final String DEFAULT_PRODUCER_GROUP = \"DEFAULT_PRODUCER\";",
        "    /** 默认 Producer 组名。 */\n    public static final String DEFAULT_PRODUCER_GROUP = \"DEFAULT_PRODUCER\";",
    ),
    (
        "    public static final String RETRY_GROUP_TOPIC_PREFIX = \"%RETRY%\";",
        "    /** 消费重试 topic 前缀：%RETRY%{consumerGroup}。 */\n    public static final String RETRY_GROUP_TOPIC_PREFIX = \"%RETRY%\";",
    ),
    (
        "    public static final String DLQ_GROUP_TOPIC_PREFIX = \"%DLQ%\";",
        "    /** 死信队列 topic 前缀：%DLQ%{consumerGroup}。 */\n    public static final String DLQ_GROUP_TOPIC_PREFIX = \"%DLQ%\";",
    ),
    (
        "    public static final long MASTER_ID = 0L;",
        "    /** Master Broker 的 brokerId。 */\n    public static final long MASTER_ID = 0L;",
    ),
    (
        "    public static String getRetryTopic(final String consumerGroup) {",
        "    /** 根据消费组生成重试 topic 名。 */\n    public static String getRetryTopic(final String consumerGroup) {",
    ),
    (
        "    public static String getDLQTopic(final String consumerGroup) {",
        "    /** 根据消费组生成死信 topic 名。 */\n    public static String getDLQTopic(final String consumerGroup) {",
    ),
    (
        "    //Reverse logic comparing to RemotingUtil method, consider refactor in RocketMQ 5.0",
        "    // 与 RemotingUtil 逻辑相反，RocketMQ 5.0 考虑重构",
    ),
    (
        "            // Workaround for docker0 bridge",
        "            // docker0 网桥场景的 workaround",
    ),
    (
        "            //ip4 higher priority",
        "            // IPv4 地址优先于 IPv6",
    ),
    (
        "        // Fallback to loopback",
        "        // 兜底使用回环地址",
    ),
]

# --- UtilAll ---
R["common/src/main/java/org/apache/rocketmq/common/UtilAll.java"] = [
    (
        "public class UtilAll {",
        "/**\n * 通用工具类：时间格式化、CRC32、堆外内存释放、\n * IP 校验、文件读写及消息压缩/解压（旧 API，推荐 {@link org.apache.rocketmq.common.compression.Compressor}）。\n */\npublic class UtilAll {",
    ),
    (
        "    /**\n     * use {@link org.apache.rocketmq.common.compression.Compressor#decompress(byte[])} instead.\n     */",
        "    /** 已废弃：请使用 {@link org.apache.rocketmq.common.compression.Compressor#decompress(byte[])}。 */",
    ),
    (
        "    /**\n     * use {@link org.apache.rocketmq.common.compression.Compressor#compress(byte[], int)} instead.\n     */",
        "    /** 已废弃：请使用 {@link org.apache.rocketmq.common.compression.Compressor#compress(byte[], int)}。 */",
    ),
    (
        "    /**\n     * Free direct-buffer's memory actively.\n     * @param buffer Direct buffer to free.\n     */",
        "    /**\n     * 主动释放 DirectBuffer 堆外内存。\n     * @param buffer 待释放的 DirectBuffer\n     */",
    ),
    (
        "        // format: \"pid@hostname\"",
        "        // 格式：\"pid@hostname\"",
    ),
    (
        "        //10.0.0.0~10.255.255.255",
        "        // A 类内网 10.0.0.0~10.255.255.255",
    ),
    (
        "        //172.16.0.0~172.31.255.255",
        "        // B 类内网 172.16.0.0~172.31.255.255",
    ),
    (
        "        //192.168.0.0~192.168.255.255",
        "        // C 类内网 192.168.0.0~192.168.255.255",
    ),
    (
        "        //127.0.0.0~127.255.255.255",
        "        // 回环地址 127.0.0.0~127.255.255.255",
    ),
    (
        "    public static boolean isBlank(String str) {",
        "    /** 判断字符串是否为 null 或仅含空白字符。 */\n    public static boolean isBlank(String str) {",
    ),
    (
        "    public static long computeElapsedTimeMilliseconds(final long beginTime) {",
        "    /** 计算自 beginTime 起经过的毫秒数。 */\n    public static long computeElapsedTimeMilliseconds(final long beginTime) {",
    ),
]

# --- AbstractRocksDBStorage ---
R["common/src/main/java/org/apache/rocketmq/common/config/AbstractRocksDBStorage.java"] = [
    (
        "public abstract class AbstractRocksDBStorage {",
        "/**\n * RocksDB 存储抽象基类：封装 DB 打开/关闭、ColumnFamily 管理、\n * Atomic Flush 写入选项及 Jemalloc 堆外内存分配。\n * 供 Broker/Controller 等模块的 RocksDB 持久化实现继承。\n */\npublic abstract class AbstractRocksDBStorage {",
    ),
    (
        "    /**\n     * Direct Jemalloc allocator\n     */",
        "    /** 基于 Jemalloc 的 Direct 内存分配器。 */",
    ),
    (
        "    /**\n     * Write options for <a href=\"https://github.com/facebook/rocksdb/wiki/Atomic-flush\">Atomic Flush</a>\n     */",
        "    /** Atomic Flush 写入选项（多 ColumnFamily 原子落盘）。 */",
    ),
    (
        "    /**\n     * Close column family handles except the default column family\n     */",
        "    /** 关闭除 default 外的全部 ColumnFamily 句柄。 */",
    ),
    (
        "        // https://github.com/facebook/rocksdb/wiki/Write-Stalls",
        "        // 参见 RocksDB Write-Stalls 文档",
    ),
    (
        "        //The close order matters.",
        "        // 关闭顺序很重要",
    ),
    (
        "        //1. close column family handles",
        "        // 1. 关闭 ColumnFamily 句柄",
    ),
    (
        "        //2. close column family options.",
        "        // 2. 关闭 ColumnFamily 选项",
    ),
    (
        "        //3. close options",
        "        // 3. 关闭 DBOptions",
    ),
    (
        "        //4. close db.",
        "        // 4. 关闭 DB 实例",
    ),
    (
        "        // For atomic-flush, we have to explicitly specify column family handles",
        "        // Atomic Flush 需显式指定全部 ColumnFamily 句柄",
    ),
    (
        "        // try to reload rocksdb next time",
        "        // 下次启动时尝试重新加载 RocksDB",
    ),
]

# --- MessageDecoder ---
R["common/src/main/java/org/apache/rocketmq/common/message/MessageDecoder.java"] = [
    (
        "public class MessageDecoder {",
        "/**\n * CommitLog 消息编解码器：定义 V1/V2 二进制格式，\n * 负责 MessageExt 与 ByteBuffer 互转、msgId 生成及属性解析。\n * magic code 区分 {@link #MESSAGE_MAGIC_CODE} 与 {@link #MESSAGE_MAGIC_CODE_V2}。\n */\npublic class MessageDecoder {",
    ),
    (
        "    // Set message magic code v2 if topic length > 127",
        "    // topic 长度 >127 时使用 V2 magic code",
    ),
    (
        "    // End of file empty MAGIC CODE cbd43194",
        "    // 文件末尾空白记录 magic code",
    ),
    (
        "    public static String createMessageId(final ByteBuffer input, final ByteBuffer addr, final long offset) {",
        "    /** 根据 storeHost 与物理 offset 生成 msgId（IP+port+offset）。 */\n    public static String createMessageId(final ByteBuffer input, final ByteBuffer addr, final long offset) {",
    ),
    (
        "    /**\n     * Just decode properties from msg buffer.\n     *\n     * @param byteBuffer msg commit log buffer.\n     */",
        "    /**\n     * 仅从 CommitLog buffer 解析消息属性（properties 段）。\n     *\n     * @param byteBuffer CommitLog 消息 buffer\n     */",
    ),
    (
        "    /**\n     * Encode without store timestamp and store host, skip blank msg.\n     *\n     * @param messageExt   msg\n     * @param needCompress need compress or not\n     * @return byte array\n     * @throws IOException when compress failed\n     */",
        "    /**\n     * 编码消息（不含 store 时间戳与 storeHost），跳过空白消息。\n     *\n     * @param messageExt 待编码消息\n     * @param needCompress 是否压缩消息体\n     * @return 编码后的字节数组\n     * @throws IOException 压缩失败时抛出\n     */",
    ),
    (
        "        // address(ip+port)",
        "        // 地址段：IP + port",
    ),
    (
        "        // offset",
        "        // 物理 offset",
    ),
    (
        "        // 1 TOTALSIZE",
        "        // 1 总长度 TOTALSIZE",
    ),
    (
        "        // 2 MAGICCODE",
        "        // 2 魔数 MAGICCODE",
    ),
    (
        "        //crc body",
        "        // 校验消息体 CRC",
    ),
    (
        "        // inflate body",
        "        // 解压消息体",
    ),
    (
        "        //only need flag, body, properties",
        "        // 仅编码 flag、body、properties",
    ),
    (
        "        //TO DO refactor, accumulate in one buffer, avoid copies",
        "        // TODO：重构为单 buffer 累积，减少拷贝",
    ),
]

# --- BrokerContainer ---
R["container/src/main/java/org/apache/rocketmq/container/BrokerContainer.java"] = [
    (
        "public class BrokerContainer implements IBrokerContainer {",
        "/**\n * Broker 容器：在单 JVM 内托管多个 Master/Slave/dLedger Broker 实例，\n * 共享 Netty RemotingServer 与 OuterAPI，通过 {@link BrokerContainerProcessor} 路由请求。\n * 适用于容器化/多租户 Broker 部署场景。\n */\npublic class BrokerContainer implements IBrokerContainer {",
    ),
    (
        "    protected final ConcurrentMap<BrokerIdentity, InnerSalveBrokerController> slaveBrokerControllers = new ConcurrentHashMap<>();",
        "    /** 已注册的 Slave Broker 控制器映射。 */\n    protected final ConcurrentMap<BrokerIdentity, InnerSalveBrokerController> slaveBrokerControllers = new ConcurrentHashMap<>();",
    ),
    (
        "    protected final ConcurrentMap<BrokerIdentity, InnerBrokerController> masterBrokerControllers = new ConcurrentHashMap<>();",
        "    /** 已注册的 Master Broker 控制器映射。 */\n    protected final ConcurrentMap<BrokerIdentity, InnerBrokerController> masterBrokerControllers = new ConcurrentHashMap<>();",
    ),
    (
        "    protected final ConcurrentMap<BrokerIdentity, InnerBrokerController> dLedgerBrokerControllers = new ConcurrentHashMap<>();",
        "    /** 已注册的 dLedger Broker 控制器映射。 */\n    protected final ConcurrentMap<BrokerIdentity, InnerBrokerController> dLedgerBrokerControllers = new ConcurrentHashMap<>();",
    ),
    (
        "    /**\n     * This function will create a slave broker along with the main broker, and start it with a different port.\n     *\n     * @param slaveBrokerConfig the specific slave broker config\n     * @throws Exception is thrown if an error occurs\n     */",
        "    /**\n     * 随主 Broker 一并创建 Slave Broker，使用不同端口启动。\n     *\n     * @param slaveBrokerConfig Slave Broker 配置\n     * @throws Exception 启动失败时抛出\n     */",
    ),
    (
        "        // also auto update namesrv if specify",
        "        // 若配置了 namesrvAddr 则自动更新 NameServer 地址",
    ),
    (
        "        // Shutdown slave brokers",
        "        // 先关闭全部 Slave Broker",
    ),
    (
        "        // Shutdown master brokers",
        "        // 再关闭全部 Master Broker",
    ),
    (
        "        // Shutdown dLedger brokers",
        "        // 关闭 dLedger Broker",
    ),
    (
        "        // Shutdown the remoting server with a high priority to avoid further traffic",
        "        // 高优先级关闭 RemotingServer，阻止新流量进入",
    ),
    (
        "        // New dLedger broker added, start it",
        "        // 新增 dLedger Broker，启动之",
    ),
    (
        "        // New master broker added, start it",
        "        // 新增 Master Broker，启动之",
    ),
    (
        "        // New slave broker added, start it",
        "        // 新增 Slave Broker，启动之",
    ),
]
