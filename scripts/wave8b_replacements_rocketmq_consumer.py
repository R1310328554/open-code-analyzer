"""RocketMQ 5.5.0 wave8b client consumer pull/listener/rebalance [15:30] replacements."""

R: dict[str, list[tuple[str, str]]] = {}

R["client/src/main/java/org/apache/rocketmq/client/consumer/PullCallback.java"] = [
    (
        "/**\n * Async message pulling interface\n */",
        "/**\n * 异步拉取消息回调：{@link MQPullConsumer} 发起 pull 后通过本接口通知结果或异常。\n */",
    ),
    (
        "    void onSuccess(final PullResult pullResult);",
        "    /** 拉取成功时回调，携带 {@link PullResult}。 */\n    void onSuccess(final PullResult pullResult);",
    ),
    (
        "    void onException(final Throwable e);",
        "    /** 拉取过程发生异常时回调。 */\n    void onException(final Throwable e);",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/consumer/PullResult.java"] = [
    (
        "public class PullResult {",
        "/**\n * 单次 pull 请求的返回体：包含拉取状态、队列 offset 边界及本次拉到的消息列表。\n */\npublic class PullResult {",
    ),
    (
        "    private final PullStatus pullStatus;",
        "    /** 拉取结果状态。 */\n    private final PullStatus pullStatus;",
    ),
    (
        "    private final long nextBeginOffset;",
        "    /** 下次 pull 建议起始 offset。 */\n    private final long nextBeginOffset;",
    ),
    (
        "    private final long minOffset;",
        "    /** 队列最小可用 offset。 */\n    private final long minOffset;",
    ),
    (
        "    private final long maxOffset;",
        "    /** 队列最大可用 offset。 */\n    private final long maxOffset;",
    ),
    (
        "    private List<MessageExt> msgFoundList;",
        "    /** 本次拉取到的消息列表（可为空）。 */\n    private List<MessageExt> msgFoundList;",
    ),
    (
        "    public PullResult(PullStatus pullStatus, long nextBeginOffset, long minOffset, long maxOffset,\n        List<MessageExt> msgFoundList) {",
        "    /** 构造 pull 结果。 */\n    public PullResult(PullStatus pullStatus, long nextBeginOffset, long minOffset, long maxOffset,\n        List<MessageExt> msgFoundList) {",
    ),
    (
        "    public PullStatus getPullStatus() {",
        "    /** 返回拉取状态。 */\n    public PullStatus getPullStatus() {",
    ),
    (
        "    public long getNextBeginOffset() {",
        "    /** 返回下次 pull 起始 offset。 */\n    public long getNextBeginOffset() {",
    ),
    (
        "    public long getMinOffset() {",
        "    /** 返回队列最小 offset。 */\n    public long getMinOffset() {",
    ),
    (
        "    public long getMaxOffset() {",
        "    /** 返回队列最大 offset。 */\n    public long getMaxOffset() {",
    ),
    (
        "    public List<MessageExt> getMsgFoundList() {",
        "    /** 返回本次拉取到的消息。 */\n    public List<MessageExt> getMsgFoundList() {",
    ),
    (
        "    public void setMsgFoundList(List<MessageExt> msgFoundList) {",
        "    /** 设置消息列表（内部或测试使用）。 */\n    public void setMsgFoundList(List<MessageExt> msgFoundList) {",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/consumer/PullStatus.java"] = [
    (
        "public enum PullStatus {",
        "/**\n * pull 操作结果状态枚举。\n */\npublic enum PullStatus {",
    ),
    (
        "    /**\n     * Founded\n     */",
        "    /** 拉取成功并找到消息。 */",
    ),
    (
        "    /**\n     * No new message can be pull\n     */",
        "    /** 当前无新消息可拉取。 */",
    ),
    (
        "    /**\n     * Filtering results can not match\n     */",
        "    /** 过滤后无匹配消息。 */",
    ),
    (
        "    /**\n     * Illegal offset,may be too big or too small\n     */",
        "    /** offset 非法（过大或过小）。 */",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/consumer/PullTaskCallback.java"] = [
    (
        "public interface PullTaskCallback {",
        "/**\n * Pull 模式消费循环任务回调：对每个已分配 {@link MessageQueue} 执行一次 pull 调度。\n */\npublic interface PullTaskCallback {",
    ),
    (
        "    void doPullTask(final MessageQueue mq, final PullTaskContext context);",
        "    /** 对指定队列执行一次 pull 任务；可通过 {@link PullTaskContext} 调节下次延迟。 */\n    void doPullTask(final MessageQueue mq, final PullTaskContext context);",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/consumer/PullTaskContext.java"] = [
    (
        "public class PullTaskContext {",
        "/**\n * Pull 任务执行上下文：控制下次 pull 延迟并持有当前 {@link MQPullConsumer} 引用。\n */\npublic class PullTaskContext {",
    ),
    (
        "    private int pullNextDelayTimeMillis = 200;",
        "    /** 下次 pull 延迟毫秒数，默认 200ms。 */\n    private int pullNextDelayTimeMillis = 200;",
    ),
    (
        "    private MQPullConsumer pullConsumer;",
        "    /** 关联的 Pull 消费者实例。 */\n    private MQPullConsumer pullConsumer;",
    ),
    (
        "    public int getPullNextDelayTimeMillis() {",
        "    /** 返回下次 pull 延迟。 */\n    public int getPullNextDelayTimeMillis() {",
    ),
    (
        "    public void setPullNextDelayTimeMillis(int pullNextDelayTimeMillis) {",
        "    /** 设置下次 pull 延迟。 */\n    public void setPullNextDelayTimeMillis(int pullNextDelayTimeMillis) {",
    ),
    (
        "    public MQPullConsumer getPullConsumer() {",
        "    /** 返回 Pull 消费者。 */\n    public MQPullConsumer getPullConsumer() {",
    ),
    (
        "    public void setPullConsumer(MQPullConsumer pullConsumer) {",
        "    /** 绑定 Pull 消费者。 */\n    public void setPullConsumer(MQPullConsumer pullConsumer) {",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/consumer/TopicMessageQueueChangeListener.java"] = [
    (
        "public interface TopicMessageQueueChangeListener {",
        "/**\n * Topic 队列数变更监听器：Topic 扩缩容导致 {@link MessageQueue} 集合变化时触发。\n */\npublic interface TopicMessageQueueChangeListener {",
    ),
    (
        "    /**\n     * This method will be invoked in the condition of queue numbers changed, These scenarios occur when the topic is\n     * expanded or shrunk.\n     *\n     * @param messageQueues\n     */",
        "    /**\n     * Topic 队列数量变更时回调（扩缩容场景）。\n     *\n     * @param topic           发生变更的 Topic\n     * @param messageQueues   变更后的队列集合\n     */",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/consumer/listener/ConsumeConcurrentlyContext.java"] = [
    (
        "/**\n * Consumer concurrent consumption context\n */",
        "/**\n * 并发消费上下文：携带当前队列、重试策略及批量 ack 游标。\n */",
    ),
    (
        "    private final MessageQueue messageQueue;",
        "    /** 当前消费所在消息队列。 */\n    private final MessageQueue messageQueue;",
    ),
    (
        "    /**\n     * Message consume retry strategy<br>\n     * -1,no retry,put into DLQ directly<br>\n     * 0,broker control retry frequency<br>\n     * >0,client control retry frequency\n     */",
        "    /**\n     * 消费失败后的重试策略：<br>\n     * -1 — 不重试，直接进入死信队列；<br>\n     * 0 — 由 Broker 控制重试间隔；<br>\n     * &gt;0 — 由客户端指定延迟级别。\n     */",
    ),
    (
        "    private int delayLevelWhenNextConsume = 0;",
        "    /** 下次消费延迟级别，默认 0（Broker 控制）。 */\n    private int delayLevelWhenNextConsume = 0;",
    ),
    (
        "    private int ackIndex = Integer.MAX_VALUE;",
        "    /** 批量消费时已确认的消息下标，默认全部成功。 */\n    private int ackIndex = Integer.MAX_VALUE;",
    ),
    (
        "    public ConsumeConcurrentlyContext(MessageQueue messageQueue) {",
        "    /** 绑定当前消费队列。 */\n    public ConsumeConcurrentlyContext(MessageQueue messageQueue) {",
    ),
    (
        "    public int getDelayLevelWhenNextConsume() {",
        "    /** 返回下次消费延迟级别。 */\n    public int getDelayLevelWhenNextConsume() {",
    ),
    (
        "    public void setDelayLevelWhenNextConsume(int delayLevelWhenNextConsume) {",
        "    /** 设置下次消费延迟级别。 */\n    public void setDelayLevelWhenNextConsume(int delayLevelWhenNextConsume) {",
    ),
    (
        "    public MessageQueue getMessageQueue() {",
        "    /** 返回当前消息队列。 */\n    public MessageQueue getMessageQueue() {",
    ),
    (
        "    public int getAckIndex() {",
        "    /** 返回批量 ack 下标。 */\n    public int getAckIndex() {",
    ),
    (
        "    public void setAckIndex(int ackIndex) {",
        "    /** 设置批量 ack 下标（部分成功场景）。 */\n    public void setAckIndex(int ackIndex) {",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/consumer/listener/ConsumeConcurrentlyStatus.java"] = [
    (
        "public enum ConsumeConcurrentlyStatus {",
        "/**\n * 并发消费 listener 返回状态。\n */\npublic enum ConsumeConcurrentlyStatus {",
    ),
    (
        "    /**\n     * Success consumption\n     */",
        "    /** 消费成功。 */",
    ),
    (
        "    /**\n     * Failure consumption,later try to consume\n     */",
        "    /** 消费失败，稍后重试。 */",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/consumer/listener/ConsumeOrderlyContext.java"] = [
    (
        "/**\n * Consumer Orderly consumption context\n */",
        "/**\n * 顺序消费上下文：控制 offset 自动提交与队列暂停时长。\n */",
    ),
    (
        "    private final MessageQueue messageQueue;",
        "    /** 当前顺序消费所在队列。 */\n    private final MessageQueue messageQueue;",
    ),
    (
        "    private boolean autoCommit = true;",
        "    /** 消费成功后是否自动提交 offset，默认 true。 */\n    private boolean autoCommit = true;",
    ),
    (
        "    private long suspendCurrentQueueTimeMillis = -1;",
        "    /** 暂停当前队列的毫秒数，-1 表示不暂停。 */\n    private long suspendCurrentQueueTimeMillis = -1;",
    ),
    (
        "    public ConsumeOrderlyContext(MessageQueue messageQueue) {",
        "    /** 绑定当前消费队列。 */\n    public ConsumeOrderlyContext(MessageQueue messageQueue) {",
    ),
    (
        "    public boolean isAutoCommit() {",
        "    /** 是否自动提交 offset。 */\n    public boolean isAutoCommit() {",
    ),
    (
        "    public void setAutoCommit(boolean autoCommit) {",
        "    /** 设置是否自动提交 offset。 */\n    public void setAutoCommit(boolean autoCommit) {",
    ),
    (
        "    public MessageQueue getMessageQueue() {",
        "    /** 返回当前消息队列。 */\n    public MessageQueue getMessageQueue() {",
    ),
    (
        "    public long getSuspendCurrentQueueTimeMillis() {",
        "    /** 返回队列暂停时长（毫秒）。 */\n    public long getSuspendCurrentQueueTimeMillis() {",
    ),
    (
        "    public void setSuspendCurrentQueueTimeMillis(long suspendCurrentQueueTimeMillis) {",
        "    /** 设置队列暂停时长（毫秒）。 */\n    public void setSuspendCurrentQueueTimeMillis(long suspendCurrentQueueTimeMillis) {",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/consumer/listener/ConsumeOrderlyStatus.java"] = [
    (
        "public enum ConsumeOrderlyStatus {",
        "/**\n * 顺序消费 listener 返回状态。\n */\npublic enum ConsumeOrderlyStatus {",
    ),
    (
        "    /**\n     * Success consumption\n     */",
        "    /** 消费成功。 */",
    ),
    (
        "    /**\n     * Rollback consumption(only for binlog consumption)\n     */",
        "    /** 回滚消费（仅 binlog 场景，已废弃）。 */",
    ),
    (
        "    /**\n     * Commit offset(only for binlog consumption)\n     */",
        "    /** 提交 offset（仅 binlog 场景，已废弃）。 */",
    ),
    (
        "    /**\n     * Suspend current queue a moment\n     */",
        "    /** 暂停当前队列一段时间后继续消费。 */",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/consumer/listener/ConsumeReturnType.java"] = [
    (
        "public enum ConsumeReturnType {",
        "/**\n * 消费结果类型：用于统计与监控区分成功、超时、异常等场景。\n */\npublic enum ConsumeReturnType {",
    ),
    (
        "    /**\n     * consume return success\n     */",
        "    /** 正常消费成功。 */",
    ),
    (
        "    /**\n     * consume timeout ,even if success\n     */",
        "    /** 消费超时（即使业务逻辑已成功）。 */",
    ),
    (
        "    /**\n     * consume throw exception\n     */",
        "    /** 消费过程抛出异常。 */",
    ),
    (
        "    /**\n     * consume return null\n     */",
        "    /** listener 返回 null。 */",
    ),
    (
        "    /**\n     * consume return failed\n     */",
        "    /** 消费明确失败。 */",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/consumer/listener/MessageListener.java"] = [
    (
        "/**\n * A MessageListener object is used to receive asynchronously delivered messages.\n */",
        "/**\n * 消息监听器标记接口：Push 消费者异步投递消息时的回调根类型。\n */",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/consumer/listener/MessageListenerConcurrently.java"] = [
    (
        "/**\n * A MessageListenerConcurrently object is used to receive asynchronously delivered messages concurrently\n */",
        "/**\n * 并发消息监听器：同一消费组内多线程并行处理不同队列的消息。\n */",
    ),
    (
        "    /**\n     * It is not recommend to throw exception,rather than returning ConsumeConcurrentlyStatus.RECONSUME_LATER if\n     * consumption failure\n     *\n     * @param msgs msgs.size() >= 1<br> DefaultMQPushConsumer.consumeMessageBatchMaxSize=1,you can modify here\n     * @return The consume status\n     */",
        "    /**\n     * 处理一批消息；不建议抛异常，失败时应返回 {@link ConsumeConcurrentlyStatus#RECONSUME_LATER}。\n     *\n     * @param msgs    消息列表，size &gt;= 1；批量大小由 consumeMessageBatchMaxSize 控制\n     * @param context 并发消费上下文\n     * @return 消费状态\n     */",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/consumer/listener/MessageListenerOrderly.java"] = [
    (
        "/**\n * A MessageListenerOrderly object is used to receive messages orderly. One queue by one thread\n */",
        "/**\n * 顺序消息监听器：同一队列由单线程顺序消费，保证分区内有序。\n */",
    ),
    (
        "    /**\n     * It is not recommend to throw exception,rather than returning ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT\n     * if consumption failure\n     *\n     * @param msgs msgs.size() >= 1<br> DefaultMQPushConsumer.consumeMessageBatchMaxSize=1,you can modify here\n     * @return The consume status\n     */",
        "    /**\n     * 顺序处理一批消息；不建议抛异常，失败时可返回\n     * {@link ConsumeOrderlyStatus#SUSPEND_CURRENT_QUEUE_A_MOMENT} 暂停当前队列。\n     *\n     * @param msgs    消息列表，size &gt;= 1；批量大小由 consumeMessageBatchMaxSize 控制\n     * @param context 顺序消费上下文\n     * @return 消费状态\n     */",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/consumer/rebalance/AbstractAllocateMessageQueueStrategy.java"] = [
    (
        "public abstract class AbstractAllocateMessageQueueStrategy implements AllocateMessageQueueStrategy {",
        "/**\n * 消息队列分配策略抽象基类：提供 consumerId 与队列列表的前置校验。\n */\npublic abstract class AbstractAllocateMessageQueueStrategy implements AllocateMessageQueueStrategy {",
    ),
    (
        "    public boolean check(String consumerGroup, String currentCID, List<MessageQueue> mqAll,\n        List<String> cidAll) {",
        "    /**\n     * 校验分配参数：currentCID 非空、队列与消费者列表非空，且 currentCID 在 cidAll 中。\n     *\n     * @return 参数合法且 currentCID 在列表中返回 true，否则 false\n     */\n    public boolean check(String consumerGroup, String currentCID, List<MessageQueue> mqAll,\n        List<String> cidAll) {",
    ),
    (
        '            log.info("[BUG] ConsumerGroup: {} The consumerId: {} not in cidAll: {}",',
        '            // 当前 consumerId 不在在线列表中，可能是 rebalance 时序问题\n            log.info("[BUG] ConsumerGroup: {} The consumerId: {} not in cidAll: {}",',
    ),
]
