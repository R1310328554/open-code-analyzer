"""RocketMQ 5.5.0 wave5a broker longpolling/metrics [0:15] replacements."""

R: dict[str, list[tuple[str, str]]] = {}

R["broker/src/main/java/org/apache/rocketmq/broker/longpolling/PollingHeader.java"] = [
    (
        "public class PollingHeader {",
        "/**\n * 长轮询请求头快照：从 POP 或通知请求中提取 consumerGroup、topic、queueId 及轮询时间参数。\n */\npublic class PollingHeader {",
    ),
    (
        "    public PollingHeader(PopMessageRequestHeader requestHeader) {",
        "    /** 从 {@link PopMessageRequestHeader} 构造轮询头。 */\n    public PollingHeader(PopMessageRequestHeader requestHeader) {",
    ),
    (
        "    public PollingHeader(NotificationRequestHeader requestHeader) {",
        "    /** 从 {@link NotificationRequestHeader} 构造轮询头。 */\n    public PollingHeader(NotificationRequestHeader requestHeader) {",
    ),
    (
        "    public String getConsumerGroup() {",
        "    /** 返回消费组名。 */\n    public String getConsumerGroup() {",
    ),
    (
        "    public String getTopic() {",
        "    /** 返回 topic 名。 */\n    public String getTopic() {",
    ),
    (
        "    public int getQueueId() {",
        "    /** 返回队列 ID。 */\n    public int getQueueId() {",
    ),
    (
        "    public long getBornTime() {",
        "    /** 返回请求创建时间戳（毫秒）。 */\n    public long getBornTime() {",
    ),
    (
        "    public long getPollTime() {",
        "    /** 返回长轮询最长等待时长（毫秒）。 */\n    public long getPollTime() {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/longpolling/PollingResult.java"] = [
    (
        "public enum PollingResult {",
        "/**\n * POP 长轮询挂起结果：标识请求是否成功进入等待队列或遭拒绝/超时。\n */\npublic enum PollingResult {",
    ),
    (
        "    POLLING_SUC,",
        "    /** 成功挂起，等待新消息到达后唤醒。 */\n    POLLING_SUC,",
    ),
    (
        "    POLLING_FULL,",
        "    /** 轮询队列已满，拒绝挂起。 */\n    POLLING_FULL,",
    ),
    (
        "    POLLING_TIMEOUT,",
        "    /** 挂起前已超时，直接返回。 */\n    POLLING_TIMEOUT,",
    ),
    (
        "    NOT_POLLING;",
        "    /** 未进入长轮询（pollTime 无效或服务已停止）。 */\n    NOT_POLLING;",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/longpolling/PopCommandCallback.java"] = [
    (
        "public class PopCommandCallback implements CommandCallback {",
        "/**\n * POP 命令回调：在 Remoting 异步响应完成后触发消费滞后计算。\n */\npublic class PopCommandCallback implements CommandCallback {",
    ),
    (
        "    public PopCommandCallback(",
        "    /** 绑定滞后计算回调、进程组上下文与结果 Future。 */\n    public PopCommandCallback(",
    ),
    (
        "    @Override\n    public void accept() {",
        "    /** 执行滞后计算并将结果写入 Future。 */\n    @Override\n    public void accept() {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/longpolling/PopLiteLongPollingService.java"] = [
    (
        "/**\n * Long polling service specifically designed for lite consumption.\n * Stores pending requests in memory using clientId as the key instead of topic@cid@qid.\n * Notification and resource checking mechanisms are identical to those in PopLongPollingService.\n */",
        "/**\n * Lite 消费专用长轮询服务：以 clientId 为键在内存中挂起 POP 请求（而非 topic@cid@qid）。\n * 消息到达通知与资源清理机制与 {@link PopLongPollingService} 一致。\n */",
    ),
    (
        "    public PopLiteLongPollingService(BrokerController brokerController, NettyRequestProcessor processor, boolean notifyLast) {",
        "    /** 初始化 lite 轮询映射，{@code notifyLast} 控制唤醒时取队首还是队尾请求。 */\n    public PopLiteLongPollingService(BrokerController brokerController, NettyRequestProcessor processor, boolean notifyLast) {",
    ),
    (
        "    @Override\n    public String getServiceName() {",
        "    /** 容器模式下附加 broker 标识前缀。 */\n    @Override\n    public String getServiceName() {",
    ),
    (
        "    @Override\n    public void run() {",
        "    /** 周期性扫描超时挂起请求、统计队列深度并清理空桶。 */\n    @Override\n    public void run() {",
    ),
    (
        "    public boolean notifyMessageArriving(final String clientId, boolean force, long msgStoreTime, String group) {",
        "    /** 新消息到达时按 clientId 唤醒对应挂起 POP 请求。 */\n    public boolean notifyMessageArriving(final String clientId, boolean force, long msgStoreTime, String group) {",
    ),
    (
        "    public boolean wakeUp(final PopRequest request) {",
        "    /** 完成挂起请求并在 pull 线程池中重新执行 POP 处理逻辑。 */\n    public boolean wakeUp(final PopRequest request) {",
    ),
    (
        "    public PollingResult polling(final ChannelHandlerContext ctx, RemotingCommand remotingCommand,\n        long bornTime, long pollTime, String clientId, String group) {",
        "    /** 尝试将 POP 请求挂入 lite 轮询队列，返回挂起结果。 */\n    public PollingResult polling(final ChannelHandlerContext ctx, RemotingCommand remotingCommand,\n        long bornTime, long pollTime, String clientId, String group) {",
    ),
    (
        "    private void cleanUnusedResource() {",
        "    /** 每 3 分钟移除空的轮询桶以释放内存。 */\n    private void cleanUnusedResource() {",
    ),
    (
        "    private PopRequest pollRemotingCommands(ConcurrentSkipListSet<PopRequest> remotingCommands) {",
        "    /** 从挂起集合中取出首个活跃连接对应的请求。 */\n    private PopRequest pollRemotingCommands(ConcurrentSkipListSet<PopRequest> remotingCommands) {",
    ),
    (
        "    // Assume that clientId is unique, so we use it as the key for now.\n    private String getPollingKey(String clientId, String group) {",
        "    /** lite 模式下假定 clientId 全局唯一，直接作为轮询桶键。 */\n    private String getPollingKey(String clientId, String group) {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/longpolling/PopRequest.java"] = [
    (
        "public class PopRequest {",
        "/**\n * POP 长轮询挂起请求：封装 Remoting 命令、Netty 通道、过期时间及可选订阅过滤条件。\n */\npublic class PopRequest {",
    ),
    (
        "    public PopRequest(RemotingCommand remotingCommand, ChannelHandlerContext ctx,\n        long expired, SubscriptionData subscriptionData, MessageFilter messageFilter) {",
        "    /** 构造挂起请求，{@code expired} 为绝对超时时间戳（毫秒）。 */\n    public PopRequest(RemotingCommand remotingCommand, ChannelHandlerContext ctx,\n        long expired, SubscriptionData subscriptionData, MessageFilter messageFilter) {",
    ),
    (
        "    public Channel getChannel() {",
        "    /** 返回客户端 Netty 通道。 */\n    public Channel getChannel() {",
    ),
    (
        "    public boolean isTimeout() {",
        "    /** 距过期时间不足 50ms 即视为超时。 */\n    public boolean isTimeout() {",
    ),
    (
        "    public boolean complete() {",
        "    /** CAS 标记请求已完成，防止重复唤醒。 */\n    public boolean complete() {",
    ),
    (
        "    public static final Comparator<PopRequest> COMPARATOR = (o1, o2) -> {",
        "    /** 按过期时间升序、操作序号升序排序，用于 {@link ConcurrentSkipListSet}。 */\n    public static final Comparator<PopRequest> COMPARATOR = (o1, o2) -> {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/longpolling/PullRequest.java"] = [
    (
        "public class PullRequest {",
        "/**\n * Pull 长轮询挂起请求：记录客户端通道、超时参数、起始 offset 及消息过滤条件。\n */\npublic class PullRequest {",
    ),
    (
        "    public PullRequest(RemotingCommand requestCommand, Channel clientChannel, long timeoutMillis, long suspendTimestamp,\n        long pullFromThisOffset, SubscriptionData subscriptionData,\n        MessageFilter messageFilter) {",
        "    /** 构造 pull 挂起上下文，{@code suspendTimestamp} 为挂起起始时刻。 */\n    public PullRequest(RemotingCommand requestCommand, Channel clientChannel, long timeoutMillis, long suspendTimestamp,\n        long pullFromThisOffset, SubscriptionData subscriptionData,\n        MessageFilter messageFilter) {",
    ),
    (
        "    public RemotingCommand getRequestCommand() {",
        "    /** 返回原始 pull 请求命令。 */\n    public RemotingCommand getRequestCommand() {",
    ),
    (
        "    public long getPullFromThisOffset() {",
        "    /** 返回消费者期望拉取的起始 offset。 */\n    public long getPullFromThisOffset() {",
    ),
    (
        "    public MessageFilter getMessageFilter() {",
        "    /** 返回 tag/属性过滤器，用于唤醒时二次匹配。 */\n    public MessageFilter getMessageFilter() {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/longpolling/PullRequestHoldService.java"] = [
    (
        "public class PullRequestHoldService extends ServiceThread {",
        "/**\n * Pull 长轮询挂起服务：按 topic@queueId 索引挂起请求，定期扫描新消息或超时并唤醒客户端。\n */\npublic class PullRequestHoldService extends ServiceThread {",
    ),
    (
        "    public PullRequestHoldService(final BrokerController brokerController) {",
        "    /** 绑定 Broker 控制器。 */\n    public PullRequestHoldService(final BrokerController brokerController) {",
    ),
    (
        "    public void suspendPullRequest(final String topic, final int queueId, final PullRequest pullRequest) {",
        "    /** 将 pull 请求标记为 suspended 并加入对应 topic@queueId 挂起表。 */\n    public void suspendPullRequest(final String topic, final int queueId, final PullRequest pullRequest) {",
    ),
    (
        "    @Override\n    public void run() {",
        "    /** 按长/短轮询配置周期扫描挂起表并检查是否有新消息。 */\n    @Override\n    public void run() {",
    ),
    (
        "    protected void checkHoldRequest() {",
        "    /** 遍历挂起表，读取各队列 maxOffset 并触发到达通知。 */\n    protected void checkHoldRequest() {",
    ),
    (
        "    public void notifyMessageArriving(final String topic, final int queueId, final long maxOffset) {",
        "    /** 新消息写入队列时唤醒匹配的挂起 pull 请求（无 tag/属性过滤）。 */\n    public void notifyMessageArriving(final String topic, final int queueId, final long maxOffset) {",
    ),
    (
        "    public void notifyMessageArriving(final String topic, final int queueId, final long maxOffset, final Long tagsCode,\n        long msgStoreTime, byte[] filterBitMap, Map<String, String> properties) {",
        "    /** 带 tag/位图/属性过滤的消息到达通知：匹配则唤醒，否则重新挂起或超时唤醒。 */\n    public void notifyMessageArriving(final String topic, final int queueId, final long maxOffset, final Long tagsCode,\n        long msgStoreTime, byte[] filterBitMap, Map<String, String> properties) {",
    ),
    (
        "    public void notifyMasterOnline() {",
        "    /** Master 上线时唤醒全部挂起 pull 请求，避免 slave 切换后客户端长时间阻塞。 */\n    public void notifyMasterOnline() {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/metrics/BrokerMetricsConstant.java"] = [
    (
        "public class BrokerMetricsConstant {",
        "/**\n * Broker OpenTelemetry 指标名与标签常量定义。\n */\npublic class BrokerMetricsConstant {",
    ),
    (
        "    public static final String OPEN_TELEMETRY_METER_NAME = \"broker-meter\";",
        "    /** OpenTelemetry Meter 名称。 */\n    public static final String OPEN_TELEMETRY_METER_NAME = \"broker-meter\";",
    ),
    (
        "    public static final String GAUGE_PROCESSOR_WATERMARK = \"rocketmq_processor_watermark\";",
        "    /** Gauge：各处理器队列水位。 */\n    public static final String GAUGE_PROCESSOR_WATERMARK = \"rocketmq_processor_watermark\";",
    ),
    (
        "    public static final String COUNTER_MESSAGES_IN_TOTAL = \"rocketmq_messages_in_total\";",
        "    /** Counter：入站消息总数。 */\n    public static final String COUNTER_MESSAGES_IN_TOTAL = \"rocketmq_messages_in_total\";",
    ),
    (
        "    public static final String COUNTER_MESSAGES_OUT_TOTAL = \"rocketmq_messages_out_total\";",
        "    /** Counter：出站消息总数。 */\n    public static final String COUNTER_MESSAGES_OUT_TOTAL = \"rocketmq_messages_out_total\";",
    ),
    (
        "    public static final String GAUGE_CONSUMER_LAG_MESSAGES = \"rocketmq_consumer_lag_messages\";",
        "    /** Gauge：消费滞后消息数。 */\n    public static final String GAUGE_CONSUMER_LAG_MESSAGES = \"rocketmq_consumer_lag_messages\";",
    ),
    (
        "    public static final String GAUGE_CONSUMER_LAG_LATENCY = \"rocketmq_consumer_lag_latency\";",
        "    /** Gauge：消费滞后时间（毫秒）。 */\n    public static final String GAUGE_CONSUMER_LAG_LATENCY = \"rocketmq_consumer_lag_latency\";",
    ),
    (
        "    public static final String COUNTER_COMMIT_MESSAGES_TOTAL = \"rocketmq_commit_messages_total\";",
        "    /** Counter：事务提交消息数。 */\n    public static final String COUNTER_COMMIT_MESSAGES_TOTAL = \"rocketmq_commit_messages_total\";",
    ),
    (
        "    public static final String LABEL_CLUSTER_NAME = \"cluster\";",
        "    /** 标签：集群名。 */\n    public static final String LABEL_CLUSTER_NAME = \"cluster\";",
    ),
    (
        "    public static final String LABEL_TOPIC = \"topic\";",
        "    /** 标签：topic 名。 */\n    public static final String LABEL_TOPIC = \"topic\";",
    ),
    (
        "    public static final String LABEL_CONSUMER_GROUP = \"consumer_group\";",
        "    /** 标签：消费组名。 */\n    public static final String LABEL_CONSUMER_GROUP = \"consumer_group\";",
    ),
    (
        "    public static final String LABEL_CONSUME_MODE = \"consume_mode\";",
        "    /** 标签：消费模式（push/pull/pop 等）。 */\n    public static final String LABEL_CONSUME_MODE = \"consume_mode\";",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/metrics/ConsumerAttr.java"] = [
    (
        "public class ConsumerAttr {",
        "/**\n * 消费者连接属性：用于指标标签去重的 group、语言、版本与消费模式组合键。\n */\npublic class ConsumerAttr {",
    ),
    (
        "    public ConsumerAttr(String group, LanguageCode language, int version, ConsumeType consumeMode) {",
        "    /** 构造消费者属性快照。 */\n    public ConsumerAttr(String group, LanguageCode language, int version, ConsumeType consumeMode) {",
    ),
    (
        "    @Override\n    public boolean equals(Object o) {",
        "    /** 按 group、language、version、consumeMode 判等。 */\n    @Override\n    public boolean equals(Object o) {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/metrics/InvocationStatus.java"] = [
    (
        "public enum InvocationStatus {",
        "/**\n * RPC 调用结果状态，用作指标标签 {@code invocation_status} 的取值。\n */\npublic enum InvocationStatus {",
    ),
    (
        "    SUCCESS(\"success\"),",
        "    /** 调用成功。 */\n    SUCCESS(\"success\"),",
    ),
    (
        "    FAILURE(\"failure\");",
        "    /** 调用失败。 */\n    FAILURE(\"failure\");",
    ),
    (
        "    public String getName() {",
        "    /** 返回指标标签字符串值。 */\n    public String getName() {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/metrics/LiteConsumerLagCalculator.java"] = [
    (
        "public class LiteConsumerLagCalculator {",
        "/**\n * Lite 消费滞后计算器：按 LMQ 维度统计消息条数滞后与最早未消费时间戳。\n */\npublic class LiteConsumerLagCalculator {",
    ),
    (
        "    protected static final long INIT_CONSUME_TIMESTAMP = -1L;",
        "    /** 尚无滞后数据时的占位时间戳。 */\n    protected static final long INIT_CONSUME_TIMESTAMP = -1L;",
    ),
    (
        "    public LiteConsumerLagCalculator(BrokerController brokerController) {",
        "    /** 绑定 Broker 控制器以访问 offset 表与 MessageStore。 */\n    public LiteConsumerLagCalculator(BrokerController brokerController) {",
    ),
    (
        "    public void removeLagInfo(String group, String bindTopic, String lmqName) {",
        "    /** 移除指定 LMQ 的滞后时间堆条目。 */\n    public void removeLagInfo(String group, String bindTopic, String lmqName) {",
    ),
    (
        "    public void updateLagInfo(String group, String bindTopic, String lmqName, long storeTimestamp) {",
        "    /** 更新 LMQ 最早未消费时间戳，堆大小受 {@code liteLagLatencyTopK} 限制。 */\n    public void updateLagInfo(String group, String bindTopic, String lmqName, long storeTimestamp) {",
    ),
    (
        "    public void calculateLiteLagCount(Consumer<ConsumerLagCalculator.CalculateLagResult> lagRecorder) {",
        "    /** 遍历 lite offset 表，按父 topic 聚合消息条数滞后并回调记录。 */\n    public void calculateLiteLagCount(Consumer<ConsumerLagCalculator.CalculateLagResult> lagRecorder) {",
    ),
    (
        "    public void calculateLiteLagLatency(Consumer<ConsumerLagCalculator.CalculateLagResult> lagRecorder) {",
        "    /** 从滞后时间堆取最小 storeTimestamp 作为 topic 组滞后延迟指标。 */\n    public void calculateLiteLagLatency(Consumer<ConsumerLagCalculator.CalculateLagResult> lagRecorder) {",
    ),
    (
        "    /**\n     * Get top K LiteLagInfo entries with the smallest lag timestamps for a topic group.\n     *\n     * @param group       consumer group name\n     * @param parentTopic parent topic name\n     * @param topK        max number of entries to retrieve\n     * @return Pair containing:\n     * - Left: list of at most topK LiteLagInfo entries sorted by timestamp\n     * - Right: minimum lag timestamp (or initial consume timestamp if no data)\n     */",
        "    /**\n     * 获取指定 topic 组滞后时间最小的 topK {@link LiteLagInfo} 条目。\n     *\n     * @param group       consumer group name\n     * @param parentTopic parent topic name\n     * @param topK        max number of entries to retrieve\n     * @return Pair containing:\n     * - Left: list of at most topK LiteLagInfo entries sorted by timestamp\n     * - Right: minimum lag timestamp (or initial consume timestamp if no data)\n     */",
    ),
    (
        "    /**\n     * Get top K LiteLagInfo entries with the largest lag counts for a topic group.\n     *\n     * @param group consumer group name\n     * @param topK  max number of entries to retrieve\n     * @return Pair containing:\n     * - Left: list of at most topK LiteLagInfo entries sorted by lag count\n     * - Right: total lag count\n     */",
        "    /**\n     * 获取指定消费组滞后条数最大的 topK {@link LiteLagInfo} 条目。\n     *\n     * @param group consumer group name\n     * @param topK  max number of entries to retrieve\n     * @return Pair containing:\n     * - Left: list of at most topK LiteLagInfo entries sorted by lag count\n     * - Right: total lag count\n     */",
    ),
    (
        "    /**\n     * Filters the lite group offset by the specified group and processes each entry via BiConsumer.\n     *\n     * @param group    The specified consumer group. If null, all offset information is processed.\n     * @param consumer The BiConsumer used to process each entry.\n     */",
        "    /**\n     * 遍历 lite 消费 offset 表，{@code group} 为 null 时处理全部组。\n     *\n     * @param group    The specified consumer group. If null, all offset information is processed.\n     * @param consumer The BiConsumer used to process each entry.\n     */",
    ),
    (
        "    protected static class LagTimeInfo {",
        "    /** LMQ 最早未消费消息的时间戳条目，按 lmqName 去重。 */\n    protected static class LagTimeInfo {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/metrics/PopMetricsConstant.java"] = [
    (
        "public class PopMetricsConstant {",
        "/**\n * POP 消费模式专用 OpenTelemetry 指标名与标签常量。\n */\npublic class PopMetricsConstant {",
    ),
    (
        "    public static final String HISTOGRAM_POP_BUFFER_SCAN_TIME_CONSUME = \"rocketmq_pop_buffer_scan_time_consume\";",
        "    /** Histogram：POP buffer 扫描耗时（毫秒）。 */\n    public static final String HISTOGRAM_POP_BUFFER_SCAN_TIME_CONSUME = \"rocketmq_pop_buffer_scan_time_consume\";",
    ),
    (
        "    public static final String COUNTER_POP_REVIVE_IN_MESSAGE_TOTAL = \"rocketmq_pop_revive_in_message_total\";",
        "    /** Counter：写入 revive topic 的消息总数。 */\n    public static final String COUNTER_POP_REVIVE_IN_MESSAGE_TOTAL = \"rocketmq_pop_revive_in_message_total\";",
    ),
    (
        "    public static final String COUNTER_POP_REVIVE_OUT_MESSAGE_TOTAL = \"rocketmq_pop_revive_out_message_total\";",
        "    /** Counter：从 revive topic 读出的消息总数。 */\n    public static final String COUNTER_POP_REVIVE_OUT_MESSAGE_TOTAL = \"rocketmq_pop_revive_out_message_total\";",
    ),
    (
        "    public static final String GAUGE_POP_REVIVE_LAG = \"rocketmq_pop_revive_lag\";",
        "    /** Gauge：revive topic 处理滞后消息数。 */\n    public static final String GAUGE_POP_REVIVE_LAG = \"rocketmq_pop_revive_lag\";",
    ),
    (
        "    public static final String GAUGE_POP_REVIVE_LATENCY = \"rocketmq_pop_revive_latency\";",
        "    /** Gauge：revive topic 处理滞后时间（毫秒）。 */\n    public static final String GAUGE_POP_REVIVE_LATENCY = \"rocketmq_pop_revive_latency\";",
    ),
    (
        "    public static final String LABEL_REVIVE_MESSAGE_TYPE = \"revive_message_type\";",
        "    /** 标签：revive 消息类型（CK/ACK）。 */\n    public static final String LABEL_REVIVE_MESSAGE_TYPE = \"revive_message_type\";",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/metrics/PopMetricsManager.java"] = [
    (
        "public class PopMetricsManager {",
        "/**\n * POP 指标管理器：注册 buffer 扫描、revive 进出站及重试等 OpenTelemetry 仪器。\n */\npublic class PopMetricsManager {",
    ),
    (
        "    public List<Pair<InstrumentSelector, ViewBuilder>> getMetricsView() {",
        "    /** 返回 POP buffer 扫描耗时的直方图分桶视图配置。 */\n    public List<Pair<InstrumentSelector, ViewBuilder>> getMetricsView() {",
    ),
    (
        "    public void initMetrics(Meter meter, BrokerController brokerController,\n        Supplier<AttributesBuilder> attributesBuilderSupplier) {",
        "    /** 注册 POP 相关 Counter、Histogram 与 Gauge 回调。 */\n    public void initMetrics(Meter meter, BrokerController brokerController,\n        Supplier<AttributesBuilder> attributesBuilderSupplier) {",
    ),
    (
        "    public void incPopReviveAckPutCount(AckMsg ackMsg, PutMessageStatus status) {",
        "    /** ACK 消息写入 revive topic 时递增计数。 */\n    public void incPopReviveAckPutCount(AckMsg ackMsg, PutMessageStatus status) {",
    ),
    (
        "    public void incPopReviveCkPutCount(PopCheckPoint checkPoint, PutMessageStatus status) {",
        "    /** Checkpoint 写入 revive topic 时递增计数。 */\n    public void incPopReviveCkPutCount(PopCheckPoint checkPoint, PutMessageStatus status) {",
    ),
    (
        "    public void incPopRevivePutCount(String group, String topic, PopReviveMessageType messageType,\n        PutMessageStatus status, int num) {",
        "    /** 按 group、topic、消息类型与写入状态递增 revive 入站计数。 */\n    public void incPopRevivePutCount(String group, String topic, PopReviveMessageType messageType,\n        PutMessageStatus status, int num) {",
    ),
    (
        "    public void incPopReviveGetCount(String group, String topic, PopReviveMessageType messageType, int queueId,\n        int num) {",
        "    /** 从 revive topic 读出消息时递增出站计数。 */\n    public void incPopReviveGetCount(String group, String topic, PopReviveMessageType messageType, int queueId,\n        int num) {",
    ),
    (
        "    public void incPopReviveRetryMessageCount(PopCheckPoint checkPoint, PutMessageStatus status) {",
        "    /** Checkpoint 触发 POP 重试 topic 写入时递增计数。 */\n    public void incPopReviveRetryMessageCount(PopCheckPoint checkPoint, PutMessageStatus status) {",
    ),
    (
        "    public void recordPopBufferScanTimeConsume(long time) {",
        "    /** 记录 POP buffer 单次扫描耗时（毫秒）。 */\n    public void recordPopBufferScanTimeConsume(long time) {",
    ),
    (
        "    public AttributesBuilder newAttributesBuilder() {",
        "    /** 获取带 broker 公共标签的 Attributes 构建器。 */\n    public AttributesBuilder newAttributesBuilder() {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/metrics/PopReviveMessageType.java"] = [
    (
        "public enum PopReviveMessageType {",
        "/**\n * POP revive topic 消息类型，用于指标标签区分 Checkpoint 与 ACK。\n */\npublic enum PopReviveMessageType {",
    ),
    (
        "    CK,",
        "    /** Checkpoint 消息。 */\n    CK,",
    ),
    (
        "    ACK",
        "    /** 消费确认（ACK）消息。 */\n    ACK",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/metrics/ProducerAttr.java"] = [
    (
        "public class ProducerAttr {",
        "/**\n * 生产者连接属性：语言与客户端版本组合，用作指标标签去重键。\n */\npublic class ProducerAttr {",
    ),
    (
        "    public ProducerAttr(LanguageCode language, int version) {",
        "    /** 构造生产者属性快照。 */\n    public ProducerAttr(LanguageCode language, int version) {",
    ),
    (
        "    @Override\n    public boolean equals(Object o) {",
        "    /** 按 language 与 version 判等。 */\n    @Override\n    public boolean equals(Object o) {",
    ),
]
