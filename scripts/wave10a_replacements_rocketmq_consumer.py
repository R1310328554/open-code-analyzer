"""RocketMQ 5.5.0 wave10a client impl/consumer [0:15] replacements."""

R: dict[str, list[tuple[str, str]]] = {}

R["client/src/main/java/org/apache/rocketmq/client/impl/consumer/AssignedMessageQueue.java"] = [
    (
        "public class AssignedMessageQueue {",
        "/**\n * 已分配消息队列状态管理：维护 rebalance 后本消费者持有的队列及其\n * {@link ProcessQueue}、拉取/消费 offset、暂停与 seek 状态。\n */\npublic class AssignedMessageQueue {",
    ),
    (
        "    private final ConcurrentHashMap<MessageQueue, MessageQueueState> assignedMessageQueueState;",
        "    /** 消息队列 -> 队列状态映射。 */\n    private final ConcurrentHashMap<MessageQueue, MessageQueueState> assignedMessageQueueState;",
    ),
    (
        "    private RebalanceImpl rebalanceImpl;",
        "    /** 关联的 rebalance 实现，用于复用已有 ProcessQueue。 */\n    private RebalanceImpl rebalanceImpl;",
    ),
    (
        "    public AssignedMessageQueue() {",
        "    /** 构造空的已分配队列表。 */\n    public AssignedMessageQueue() {",
    ),
    (
        "    public void setRebalanceImpl(RebalanceImpl rebalanceImpl) {",
        "    /** 设置 rebalance 实现引用。 */\n    public void setRebalanceImpl(RebalanceImpl rebalanceImpl) {",
    ),
    (
        "    public boolean isPaused(MessageQueue messageQueue) {",
        "    /** 判断指定队列是否已暂停消费；未分配时视为已暂停。 */\n    public boolean isPaused(MessageQueue messageQueue) {",
    ),
    (
        "    public void pause(Collection<MessageQueue> messageQueues) {",
        "    /** 暂停指定队列集合的消费。 */\n    public void pause(Collection<MessageQueue> messageQueues) {",
    ),
    (
        "    public void resume(Collection<MessageQueue> messageQueueCollection) {",
        "    /** 恢复指定队列集合的消费。 */\n    public void resume(Collection<MessageQueue> messageQueueCollection) {",
    ),
    (
        "    public ProcessQueue getProcessQueue(MessageQueue messageQueue) {",
        "    /** 获取队列对应的 {@link ProcessQueue}。 */\n    public ProcessQueue getProcessQueue(MessageQueue messageQueue) {",
    ),
    (
        "    public long getPullOffset(MessageQueue messageQueue) {",
        "    /** 获取队列当前拉取 offset，未分配时返回 -1。 */\n    public long getPullOffset(MessageQueue messageQueue) {",
    ),
    (
        "    public void updatePullOffset(MessageQueue messageQueue, long offset, ProcessQueue processQueue) {",
        "    /** 更新拉取 offset；ProcessQueue 不匹配时不更新。 */\n    public void updatePullOffset(MessageQueue messageQueue, long offset, ProcessQueue processQueue) {",
    ),
    (
        "    public long getConsumerOffset(MessageQueue messageQueue) {",
        "    /** 获取消费 offset，未分配时返回 -1。 */\n    public long getConsumerOffset(MessageQueue messageQueue) {",
    ),
    (
        "    public void updateConsumeOffset(MessageQueue messageQueue, long offset) {",
        "    /** 更新消费 offset。 */\n    public void updateConsumeOffset(MessageQueue messageQueue, long offset) {",
    ),
    (
        "    public void setSeekOffset(MessageQueue messageQueue, long offset) {",
        "    /** 设置 seek 目标 offset。 */\n    public void setSeekOffset(MessageQueue messageQueue, long offset) {",
    ),
    (
        "    public long getSeekOffset(MessageQueue messageQueue) {",
        "    /** 获取 seek 目标 offset，未分配时返回 -1。 */\n    public long getSeekOffset(MessageQueue messageQueue) {",
    ),
    (
        "    public void updateAssignedMessageQueue(String topic, Collection<MessageQueue> assigned) {",
        "    /** 按 topic 更新已分配队列：移除不在 assigned 中的队列并标记 dropped。 */\n    public void updateAssignedMessageQueue(String topic, Collection<MessageQueue> assigned) {",
    ),
    (
        "    public void updateAssignedMessageQueue(Collection<MessageQueue> assigned) {",
        "    /** 全量更新已分配队列列表。 */\n    public void updateAssignedMessageQueue(Collection<MessageQueue> assigned) {",
    ),
    (
        "    public void removeAssignedMessageQueue(String topic) {",
        "    /** 移除指定 topic 下所有已分配队列。 */\n    public void removeAssignedMessageQueue(String topic) {",
    ),
    (
        "    public Set<MessageQueue> getAssignedMessageQueues() {",
        "    /** 返回当前已分配的消息队列集合。 */\n    public Set<MessageQueue> getAssignedMessageQueues() {",
    ),
    (
        "    private class MessageQueueState {",
        "    /** 单队列运行时状态：ProcessQueue、offset 与暂停标志。 */\n    private class MessageQueueState {",
    ),
    (
        "        private volatile boolean paused = false;",
        "        /** 是否暂停消费。 */\n        private volatile boolean paused = false;",
    ),
    (
        "        private volatile long pullOffset = -1;",
        "        /** 当前拉取 offset。 */\n        private volatile long pullOffset = -1;",
    ),
    (
        "        private volatile long consumeOffset = -1;",
        "        /** 当前消费 offset。 */\n        private volatile long consumeOffset = -1;",
    ),
    (
        "        private volatile long seekOffset = -1;",
        "        /** seek 目标 offset。 */\n        private volatile long seekOffset = -1;",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/impl/consumer/ConsumeMessageService.java"] = [
    (
        "public interface ConsumeMessageService {",
        "/**\n * 消息消费线程池服务接口：管理消费线程生命周期，提交 pull/pop 拉到的消息进行消费。\n */\npublic interface ConsumeMessageService {",
    ),
    (
        "    void start();",
        "    /** 启动消费线程池。 */\n    void start();",
    ),
    (
        "    void shutdown(long awaitTerminateMillis);",
        "    /** 关闭服务并等待线程终止。 */\n    void shutdown(long awaitTerminateMillis);",
    ),
    (
        "    void updateCorePoolSize(int corePoolSize);",
        "    /** 更新核心线程数。 */\n    void updateCorePoolSize(int corePoolSize);",
    ),
    (
        "    void incCorePoolSize();",
        "    /** 核心线程数加一。 */\n    void incCorePoolSize();",
    ),
    (
        "    void decCorePoolSize();",
        "    /** 核心线程数减一。 */\n    void decCorePoolSize();",
    ),
    (
        "    int getCorePoolSize();",
        "    /** 返回当前核心线程数。 */\n    int getCorePoolSize();",
    ),
    (
        "    ConsumeMessageDirectlyResult consumeMessageDirectly(final MessageExt msg, final String brokerName);",
        "    /** 直接消费单条消息（管理/调试用途）。 */\n    ConsumeMessageDirectlyResult consumeMessageDirectly(final MessageExt msg, final String brokerName);",
    ),
    (
        "    void submitConsumeRequest(\n        final List<MessageExt> msgs,\n        final ProcessQueue processQueue,\n        final MessageQueue messageQueue,\n        final boolean dispathToConsume);",
        "    /**\n     * 提交 pull 模式消费请求。\n     *\n     * @param msgs 待消费消息列表\n     * @param processQueue 对应 ProcessQueue\n     * @param messageQueue 消息队列\n     * @param dispathToConsume 是否立即分派到消费线程\n     */\n    void submitConsumeRequest(\n        final List<MessageExt> msgs,\n        final ProcessQueue processQueue,\n        final MessageQueue messageQueue,\n        final boolean dispathToConsume);",
    ),
    (
        "    void submitPopConsumeRequest(\n        final List<MessageExt> msgs,\n        final PopProcessQueue processQueue,\n        final MessageQueue messageQueue);",
        "    /**\n     * 提交 POP 模式消费请求。\n     *\n     * @param msgs 待消费消息列表\n     * @param processQueue 对应 PopProcessQueue\n     * @param messageQueue 消息队列\n     */\n    void submitPopConsumeRequest(\n        final List<MessageExt> msgs,\n        final PopProcessQueue processQueue,\n        final MessageQueue messageQueue);",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/impl/consumer/MQConsumerInner.java"] = [
    (
        "/**\n * Consumer inner interface\n */",
        "/**\n * 消费者内部接口：供 {@link MQClientInstance} 与 rebalance/pull 服务调用。\n */",
    ),
    (
        "    String groupName();",
        "    /** 消费组名。 */\n    String groupName();",
    ),
    (
        "    MessageModel messageModel();",
        "    /** 消息模式（集群/广播）。 */\n    MessageModel messageModel();",
    ),
    (
        "    ConsumeType consumeType();",
        "    /** 消费类型（主动 pull / 被动 push）。 */\n    ConsumeType consumeType();",
    ),
    (
        "    ConsumeFromWhere consumeFromWhere();",
        "    /** 首次消费起始位置策略。 */\n    ConsumeFromWhere consumeFromWhere();",
    ),
    (
        "    Set<SubscriptionData> subscriptions();",
        "    /** 当前订阅集合。 */\n    Set<SubscriptionData> subscriptions();",
    ),
    (
        "    void doRebalance();",
        "    /** 执行 rebalance。 */\n    void doRebalance();",
    ),
    (
        "    boolean tryRebalance();",
        "    /** 尝试 rebalance，返回是否已均衡。 */\n    boolean tryRebalance();",
    ),
    (
        "    void persistConsumerOffset();",
        "    /** 持久化消费位点。 */\n    void persistConsumerOffset();",
    ),
    (
        "    void updateTopicSubscribeInfo(final String topic, final Set<MessageQueue> info);",
        "    /** 更新 topic 的路由/队列订阅信息。 */\n    void updateTopicSubscribeInfo(final String topic, final Set<MessageQueue> info);",
    ),
    (
        "    boolean isSubscribeTopicNeedUpdate(final String topic);",
        "    /** 判断 topic 订阅信息是否需要更新。 */\n    boolean isSubscribeTopicNeedUpdate(final String topic);",
    ),
    (
        "    boolean isUnitMode();",
        "    /** 是否单元化模式。 */\n    boolean isUnitMode();",
    ),
    (
        "    ConsumerRunningInfo consumerRunningInfo();",
        "    /** 返回消费者运行时信息快照。 */\n    ConsumerRunningInfo consumerRunningInfo();",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/impl/consumer/MessageQueueLock.java"] = [
    (
        "/**\n * Message lock,strictly ensure the single queue only one thread at a time consuming\n */",
        "/**\n * 消息队列消费锁：严格保证同一队列同一时刻仅一个线程消费。\n * 支持按 shardingKey 索引细分锁对象。\n */",
    ),
    (
        "    private ConcurrentMap<MessageQueue, ConcurrentMap<Integer, Object>> mqLockTable =",
        "    /** 队列 -> (shardingKeyIndex -> 锁对象) 映射表。 */\n    private ConcurrentMap<MessageQueue, ConcurrentMap<Integer, Object>> mqLockTable =",
    ),
    (
        "    public Object fetchLockObject(final MessageQueue mq) {",
        "    /** 获取队列默认锁对象（shardingKeyIndex 为 -1）。 */\n    public Object fetchLockObject(final MessageQueue mq) {",
    ),
    (
        "    public Object fetchLockObject(final MessageQueue mq, final int shardingKeyIndex) {",
        "    /**\n     * 获取指定队列与 shardingKey 索引对应的锁对象。\n     *\n     * @param mq 消息队列\n     * @param shardingKeyIndex 分片键索引，-1 表示整队列锁\n     */\n    public Object fetchLockObject(final MessageQueue mq, final int shardingKeyIndex) {",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/impl/consumer/MessageRequest.java"] = [
    (
        "public interface MessageRequest {",
        "/**\n * 拉取/POP 请求统一接口，由 {@link PullMessageService} 队列调度。\n */\npublic interface MessageRequest {",
    ),
    (
        "    MessageRequestMode getMessageRequestMode();",
        "    /** 返回请求模式（PULL 或 POP）。 */\n    MessageRequestMode getMessageRequestMode();",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/impl/consumer/PopProcessQueue.java"] = [
    (
        "/**\n * Queue consumption snapshot\n */",
        "/**\n * POP 消费队列快照：跟踪待 Ack 消息数、最近 pop 时间与丢弃状态。\n */",
    ),
    (
        "    private final static long PULL_MAX_IDLE_TIME = Long.parseLong(System.getProperty(\"rocketmq.client.pull.pullMaxIdleTime\", \"120000\"));",
        "    /** POP 空闲超时阈值（毫秒），超时视为 pull 过期。 */\n    private final static long PULL_MAX_IDLE_TIME = Long.parseLong(System.getProperty(\"rocketmq.client.pull.pullMaxIdleTime\", \"120000\"));",
    ),
    (
        "    private long lastPopTimestamp = System.currentTimeMillis();",
        "    /** 最近一次 pop 时间戳。 */\n    private long lastPopTimestamp = System.currentTimeMillis();",
    ),
    (
        "    private AtomicInteger waitAckCounter = new AtomicInteger(0);",
        "    /** 待 Ack 消息计数。 */\n    private AtomicInteger waitAckCounter = new AtomicInteger(0);",
    ),
    (
        "    private volatile boolean dropped = false;",
        "    /** 队列是否已丢弃（rebalance 移除等）。 */\n    private volatile boolean dropped = false;",
    ),
    (
        "    public long getLastPopTimestamp() {",
        "    /** 返回最近 pop 时间戳。 */\n    public long getLastPopTimestamp() {",
    ),
    (
        "    public void setLastPopTimestamp(long lastPopTimestamp) {",
        "    /** 设置最近 pop 时间戳。 */\n    public void setLastPopTimestamp(long lastPopTimestamp) {",
    ),
    (
        "    public void incFoundMsg(int count) {",
        "    /** 增加待 Ack 计数（pop 到新消息时）。 */\n    public void incFoundMsg(int count) {",
    ),
    (
        "    /**\n     * @return the value before decrement.\n     */",
        "    /**\n     * 消息 Ack 后递减计数。\n     *\n     * @return 递减前的计数值\n     */",
    ),
    (
        "    public void decFoundMsg(int count) {",
        "    /** 减少待 Ack 计数。 */\n    public void decFoundMsg(int count) {",
    ),
    (
        "    public int getWaiAckMsgCount() {",
        "    /** 返回当前待 Ack 消息数。 */\n    public int getWaiAckMsgCount() {",
    ),
    (
        "    public boolean isDropped() {",
        "    /** 队列是否已丢弃。 */\n    public boolean isDropped() {",
    ),
    (
        "    public void setDropped(boolean dropped) {",
        "    /** 设置丢弃标志。 */\n    public void setDropped(boolean dropped) {",
    ),
    (
        "    public void fillPopProcessQueueInfo(final PopProcessQueueInfo info) {",
        "    /** 填充运行时监控信息结构体。 */\n    public void fillPopProcessQueueInfo(final PopProcessQueueInfo info) {",
    ),
    (
        "    public boolean isPullExpired() {",
        "    /** 判断是否超过 POP 空闲超时未 pop。 */\n    public boolean isPullExpired() {",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/impl/consumer/PopRequest.java"] = [
    (
        "public class PopRequest implements MessageRequest {",
        "/**\n * POP 拉取请求：携带 topic、消费组、队列及 {@link PopProcessQueue} 等上下文。\n */\npublic class PopRequest implements MessageRequest {",
    ),
    (
        "    private String topic;",
        "    /** Topic 名称。 */\n    private String topic;",
    ),
    (
        "    private String consumerGroup;",
        "    /** 消费组名。 */\n    private String consumerGroup;",
    ),
    (
        "    private MessageQueue messageQueue;",
        "    /** 目标消息队列。 */\n    private MessageQueue messageQueue;",
    ),
    (
        "    private PopProcessQueue popProcessQueue;",
        "    /** 关联的 POP 处理队列。 */\n    private PopProcessQueue popProcessQueue;",
    ),
    (
        "    private boolean lockedFirst = false;",
        "    /** 是否已优先加锁（顺序消费场景）。 */\n    private boolean lockedFirst = false;",
    ),
    (
        "    private int initMode = ConsumeInitMode.MAX;",
        "    /** POP 初始 offset 模式。 */\n    private int initMode = ConsumeInitMode.MAX;",
    ),
    (
        "    public boolean isLockedFirst() {",
        "    /** 是否已优先加锁。 */\n    public boolean isLockedFirst() {",
    ),
    (
        "    public void setLockedFirst(boolean lockedFirst) {",
        "    /** 设置优先加锁标志。 */\n    public void setLockedFirst(boolean lockedFirst) {",
    ),
    (
        "    public PopProcessQueue getPopProcessQueue() {",
        "    /** 返回 POP 处理队列。 */\n    public PopProcessQueue getPopProcessQueue() {",
    ),
    (
        "    public void setPopProcessQueue(PopProcessQueue popProcessQueue) {",
        "    /** 设置 POP 处理队列。 */\n    public void setPopProcessQueue(PopProcessQueue popProcessQueue) {",
    ),
    (
        "    public int getInitMode() {",
        "    /** 返回 POP 初始模式。 */\n    public int getInitMode() {",
    ),
    (
        "    public void setInitMode(int initMode) {",
        "    /** 设置 POP 初始模式。 */\n    public void setInitMode(int initMode) {",
    ),
    (
        "    public MessageRequestMode getMessageRequestMode() {",
        "    /** 返回 {@link MessageRequestMode#POP}。 */\n    public MessageRequestMode getMessageRequestMode() {",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/impl/consumer/PullRequest.java"] = [
    (
        "public class PullRequest implements MessageRequest {",
        "/**\n * Pull 拉取请求：描述消费组、目标队列、{@link ProcessQueue} 及下次拉取 offset。\n */\npublic class PullRequest implements MessageRequest {",
    ),
    (
        "    private String consumerGroup;",
        "    /** 消费组名。 */\n    private String consumerGroup;",
    ),
    (
        "    private MessageQueue messageQueue;",
        "    /** 目标消息队列。 */\n    private MessageQueue messageQueue;",
    ),
    (
        "    private ProcessQueue processQueue;",
        "    /** 关联的处理队列。 */\n    private ProcessQueue processQueue;",
    ),
    (
        "    private long nextOffset;",
        "    /** 下次 pull 起始 offset。 */\n    private long nextOffset;",
    ),
    (
        "    private boolean previouslyLocked = false;",
        "    /** 此前是否已成功加锁（顺序消费）。 */\n    private boolean previouslyLocked = false;",
    ),
    (
        "    public boolean isPreviouslyLocked() {",
        "    /** 返回此前加锁状态。 */\n    public boolean isPreviouslyLocked() {",
    ),
    (
        "    public void setPreviouslyLocked(boolean previouslyLocked) {",
        "    /** 设置此前加锁状态。 */\n    public void setPreviouslyLocked(boolean previouslyLocked) {",
    ),
    (
        "    public long getNextOffset() {",
        "    /** 返回下次 pull offset。 */\n    public long getNextOffset() {",
    ),
    (
        "    public void setNextOffset(long nextOffset) {",
        "    /** 设置下次 pull offset。 */\n    public void setNextOffset(long nextOffset) {",
    ),
    (
        "    public ProcessQueue getProcessQueue() {",
        "    /** 返回 ProcessQueue。 */\n    public ProcessQueue getProcessQueue() {",
    ),
    (
        "    public void setProcessQueue(ProcessQueue processQueue) {",
        "    /** 设置 ProcessQueue。 */\n    public void setProcessQueue(ProcessQueue processQueue) {",
    ),
    (
        "    public MessageRequestMode getMessageRequestMode() {",
        "    /** 返回 {@link MessageRequestMode#PULL}。 */\n    public MessageRequestMode getMessageRequestMode() {",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/impl/consumer/PullResultExt.java"] = [
    (
        "public class PullResultExt extends PullResult {",
        "/**\n * Pull 结果扩展：在 {@link PullResult} 基础上携带 Broker 建议节点、原始二进制及 offset 增量。\n */\npublic class PullResultExt extends PullResult {",
    ),
    (
        "    private final long suggestWhichBrokerId;",
        "    /** Broker 建议下次从哪个 brokerId 拉取。 */\n    private final long suggestWhichBrokerId;",
    ),
    (
        "    private byte[] messageBinary;",
        "    /** 原始消息二进制（解码前）。 */\n    private byte[] messageBinary;",
    ),
    (
        "    private final Long offsetDelta;",
        "    /** 队列 offset 增量，用于修正消息 queueOffset。 */\n    private final Long offsetDelta;",
    ),
    (
        "    public PullResultExt(PullStatus pullStatus, long nextBeginOffset, long minOffset, long maxOffset,\n        List<MessageExt> msgFoundList, final long suggestWhichBrokerId, final byte[] messageBinary) {",
        "    /** 构造扩展 pull 结果（offsetDelta 默认为 0）。 */\n    public PullResultExt(PullStatus pullStatus, long nextBeginOffset, long minOffset, long maxOffset,\n        List<MessageExt> msgFoundList, final long suggestWhichBrokerId, final byte[] messageBinary) {",
    ),
    (
        "    public PullResultExt(PullStatus pullStatus, long nextBeginOffset, long minOffset, long maxOffset,\n                         List<MessageExt> msgFoundList, final long suggestWhichBrokerId, final byte[] messageBinary, final Long offsetDelta) {",
        "    /** 构造扩展 pull 结果，含完整 offsetDelta。 */\n    public PullResultExt(PullStatus pullStatus, long nextBeginOffset, long minOffset, long maxOffset,\n                         List<MessageExt> msgFoundList, final long suggestWhichBrokerId, final byte[] messageBinary, final Long offsetDelta) {",
    ),
    (
        "    public Long getOffsetDelta() {",
        "    /** 返回 offset 增量。 */\n    public Long getOffsetDelta() {",
    ),
    (
        "    public byte[] getMessageBinary() {",
        "    /** 返回原始消息二进制。 */\n    public byte[] getMessageBinary() {",
    ),
    (
        "    public void setMessageBinary(byte[] messageBinary) {",
        "    /** 设置原始消息二进制。 */\n    public void setMessageBinary(byte[] messageBinary) {",
    ),
    (
        "    public long getSuggestWhichBrokerId() {",
        "    /** 返回建议拉取的 Broker ID。 */\n    public long getSuggestWhichBrokerId() {",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/impl/consumer/PullMessageService.java"] = [
    (
        "public class PullMessageService extends ServiceThread {",
        "/**\n * 拉取消息后台服务：从请求队列取出 {@link PullRequest}/{@link PopRequest} 并分派给对应 Push 消费者执行。\n */\npublic class PullMessageService extends ServiceThread {",
    ),
    (
        "    private final LinkedBlockingQueue<MessageRequest> messageRequestQueue = new LinkedBlockingQueue<>();",
        "    /** pull/pop 请求阻塞队列。 */\n    private final LinkedBlockingQueue<MessageRequest> messageRequestQueue = new LinkedBlockingQueue<>();",
    ),
    (
        "    private final MQClientInstance mQClientFactory;",
        "    /** 关联的客户端实例。 */\n    private final MQClientInstance mQClientFactory;",
    ),
    (
        "    public PullMessageService(MQClientInstance mQClientFactory) {",
        "    /** 构造拉取服务。 */\n    public PullMessageService(MQClientInstance mQClientFactory) {",
    ),
    (
        "    public void executePullRequestLater(final PullRequest pullRequest, final long timeDelay) {",
        "    /** 延迟提交 pull 请求。 */\n    public void executePullRequestLater(final PullRequest pullRequest, final long timeDelay) {",
    ),
    (
        "    public void executePullRequestImmediately(final PullRequest pullRequest) {",
        "    /** 立即提交 pull 请求到队列。 */\n    public void executePullRequestImmediately(final PullRequest pullRequest) {",
    ),
    (
        "    public void executePopPullRequestLater(final PopRequest popRequest, final long timeDelay) {",
        "    /** 延迟提交 POP 请求。 */\n    public void executePopPullRequestLater(final PopRequest popRequest, final long timeDelay) {",
    ),
    (
        "    public void executePopPullRequestImmediately(final PopRequest popRequest) {",
        "    /** 立即提交 POP 请求到队列。 */\n    public void executePopPullRequestImmediately(final PopRequest popRequest) {",
    ),
    (
        "    public void executeTaskLater(final Runnable r, final long timeDelay) {",
        "    /** 延迟执行调度任务。 */\n    public void executeTaskLater(final Runnable r, final long timeDelay) {",
    ),
    (
        "    public void executeTask(final Runnable r) {",
        "    /** 立即执行调度任务。 */\n    public void executeTask(final Runnable r) {",
    ),
    (
        "    public ScheduledExecutorService getScheduledExecutorService() {",
        "    /** 返回延迟调度线程池。 */\n    public ScheduledExecutorService getScheduledExecutorService() {",
    ),
    (
        "    private void pullMessage(final PullRequest pullRequest) {",
        "    /** 根据 pull 请求查找消费者并执行 pull。 */\n    private void pullMessage(final PullRequest pullRequest) {",
    ),
    (
        "    private void popMessage(final PopRequest popRequest) {",
        "    /** 根据 POP 请求查找消费者并执行 pop。 */\n    private void popMessage(final PopRequest popRequest) {",
    ),
    (
        "    public void run() {",
        "    /** 主循环：从队列取请求并按模式分派 pull 或 pop。 */\n    public void run() {",
    ),
    (
        "    public void shutdown(boolean interrupt) {",
        "    /** 关闭服务并优雅停止调度线程池。 */\n    public void shutdown(boolean interrupt) {",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/impl/consumer/RebalanceService.java"] = [
    (
        "public class RebalanceService extends ServiceThread {",
        "/**\n * Rebalance 后台服务：周期性触发 {@link MQClientInstance#doRebalance()}，\n * 协调消费组内队列分配与负载均衡。\n */\npublic class RebalanceService extends ServiceThread {",
    ),
    (
        "    private static long waitInterval =",
        "    /** rebalance 已均衡时的等待间隔（毫秒）。 */\n    private static long waitInterval =",
    ),
    (
        "    private static long minInterval =",
        "    /** rebalance 未均衡时的最小重试间隔（毫秒）。 */\n    private static long minInterval =",
    ),
    (
        "    private final MQClientInstance mqClientFactory;",
        "    /** 关联的客户端工厂。 */\n    private final MQClientInstance mqClientFactory;",
    ),
    (
        "    private long lastRebalanceTimestamp = System.currentTimeMillis();",
        "    /** 上次 rebalance 时间戳。 */\n    private long lastRebalanceTimestamp = System.currentTimeMillis();",
    ),
    (
        "    public RebalanceService(MQClientInstance mqClientFactory) {",
        "    /** 构造 rebalance 服务。 */\n    public RebalanceService(MQClientInstance mqClientFactory) {",
    ),
    (
        "    public void run() {",
        "    /** 主循环：按间隔触发 rebalance，未均衡时缩短等待。 */\n    public void run() {",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/impl/consumer/RebalancePullImpl.java"] = [
    (
        "public class RebalancePullImpl extends RebalanceImpl {",
        "/**\n * Pull 消费者 rebalance 实现：主动拉取模式，不派发 pull 请求。\n */\npublic class RebalancePullImpl extends RebalanceImpl {",
    ),
    (
        "    private final DefaultMQPullConsumerImpl defaultMQPullConsumerImpl;",
        "    /** 关联的 Pull 消费者实现。 */\n    private final DefaultMQPullConsumerImpl defaultMQPullConsumerImpl;",
    ),
    (
        "    public RebalancePullImpl(DefaultMQPullConsumerImpl defaultMQPullConsumerImpl) {",
        "    /** 以 Pull 消费者构造 rebalance 实现。 */\n    public RebalancePullImpl(DefaultMQPullConsumerImpl defaultMQPullConsumerImpl) {",
    ),
    (
        "    public void messageQueueChanged(String topic, Set<MessageQueue> mqAll, Set<MessageQueue> mqDivided) {",
        "    /** 队列变更时通知 {@link MessageQueueListener}。 */\n    public void messageQueueChanged(String topic, Set<MessageQueue> mqAll, Set<MessageQueue> mqDivided) {",
    ),
    (
        "    public boolean removeUnnecessaryMessageQueue(MessageQueue mq, ProcessQueue pq) {",
        "    /** 移除多余队列：持久化并删除 offset。 */\n    public boolean removeUnnecessaryMessageQueue(MessageQueue mq, ProcessQueue pq) {",
    ),
    (
        "    public ConsumeType consumeType() {",
        "    /** 返回主动消费类型 {@link ConsumeType#CONSUME_ACTIVELY}。 */\n    public ConsumeType consumeType() {",
    ),
    (
        "    public void removeDirtyOffset(final MessageQueue mq) {",
        "    /** 移除脏 offset 记录。 */\n    public void removeDirtyOffset(final MessageQueue mq) {",
    ),
    (
        "    public long computePullFromWhereWithException(MessageQueue mq) throws MQClientException {",
        "    /** Pull 模式不计算起始 offset，固定返回 0。 */\n    public long computePullFromWhereWithException(MessageQueue mq) throws MQClientException {",
    ),
    (
        "    public int getConsumeInitMode() {",
        "    /** Pull 模式不支持 initMode。 */\n    public int getConsumeInitMode() {",
    ),
    (
        "    public void dispatchPullRequest(final List<PullRequest> pullRequestList, final long delay) {",
        "    /** Pull 模式由用户主动拉取，不派发请求。 */\n    public void dispatchPullRequest(final List<PullRequest> pullRequestList, final long delay) {",
    ),
    (
        "    public ProcessQueue createProcessQueue() {",
        "    /** 创建标准 ProcessQueue。 */\n    public ProcessQueue createProcessQueue() {",
    ),
    (
        "    public PopProcessQueue createPopProcessQueue() {",
        "    /** Pull 模式不支持 POP 队列。 */\n    public PopProcessQueue createPopProcessQueue() {",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/impl/consumer/RebalanceLitePullImpl.java"] = [
    (
        "public class RebalanceLitePullImpl extends RebalanceImpl {",
        "/**\n * Lite Pull 消费者 rebalance 实现：支持按 {@link ConsumeFromWhere} 计算起始 offset。\n */\npublic class RebalanceLitePullImpl extends RebalanceImpl {",
    ),
    (
        "    private final DefaultLitePullConsumerImpl litePullConsumerImpl;",
        "    /** 关联的 Lite Pull 消费者实现。 */\n    private final DefaultLitePullConsumerImpl litePullConsumerImpl;",
    ),
    (
        "    public RebalanceLitePullImpl(DefaultLitePullConsumerImpl litePullConsumerImpl) {",
        "    /** 以 Lite Pull 消费者构造 rebalance 实现。 */\n    public RebalanceLitePullImpl(DefaultLitePullConsumerImpl litePullConsumerImpl) {",
    ),
    (
        "    public void messageQueueChanged(String topic, Set<MessageQueue> mqAll, Set<MessageQueue> mqDivided) {",
        "    /** 队列变更时通知 {@link MessageQueueListener}。 */\n    public void messageQueueChanged(String topic, Set<MessageQueue> mqAll, Set<MessageQueue> mqDivided) {",
    ),
    (
        "    public boolean removeUnnecessaryMessageQueue(MessageQueue mq, ProcessQueue pq) {",
        "    /** 移除多余队列：持久化并删除 offset。 */\n    public boolean removeUnnecessaryMessageQueue(MessageQueue mq, ProcessQueue pq) {",
    ),
    (
        "    public ConsumeType consumeType() {",
        "    /** 返回主动消费类型。 */\n    public ConsumeType consumeType() {",
    ),
    (
        "    public long computePullFromWhereWithException(MessageQueue mq) throws MQClientException {",
        "    /** 按 ConsumeFromWhere 策略计算首次/恢复 pull 起始 offset。 */\n    public long computePullFromWhereWithException(MessageQueue mq) throws MQClientException {",
    ),
    (
        "                    if (mq.getTopic().startsWith(MixAll.RETRY_GROUP_TOPIC_PREFIX)) { // First start, no offset",
        "                    if (mq.getTopic().startsWith(MixAll.RETRY_GROUP_TOPIC_PREFIX)) { // 首次启动，无 offset",
    ),
    (
        "    public int getConsumeInitMode() {",
        "    /** Lite Pull 模式不支持 initMode。 */\n    public int getConsumeInitMode() {",
    ),
    (
        "    public void dispatchPullRequest(final List<PullRequest> pullRequestList, final long delay) {",
        "    /** Lite Pull 由用户主动拉取，不派发请求。 */\n    public void dispatchPullRequest(final List<PullRequest> pullRequestList, final long delay) {",
    ),
    (
        "    public ProcessQueue createProcessQueue() {",
        "    /** 创建标准 ProcessQueue。 */\n    public ProcessQueue createProcessQueue() {",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/impl/consumer/RebalancePushImpl.java"] = [
    (
        "public class RebalancePushImpl extends RebalanceImpl {",
        "/**\n * Push 消费者 rebalance 实现：被动消费，负责计算 pull 起点、派发 pull/pop 请求及顺序消费解锁。\n */\npublic class RebalancePushImpl extends RebalanceImpl {",
    ),
    (
        "    private final static long UNLOCK_DELAY_TIME_MILLS = Long.parseLong(System.getProperty(\"rocketmq.client.unlockDelayTimeMills\", \"20000\"));",
        "    /** 顺序消费解锁延迟（毫秒），兼容旧行为。 */\n    private final static long UNLOCK_DELAY_TIME_MILLS = Long.parseLong(System.getProperty(\"rocketmq.client.unlockDelayTimeMills\", \"20000\"));",
    ),
    (
        "    private final DefaultMQPushConsumerImpl defaultMQPushConsumerImpl;",
        "    /** 关联的 Push 消费者实现。 */\n    private final DefaultMQPushConsumerImpl defaultMQPushConsumerImpl;",
    ),
    (
        "    public RebalancePushImpl(DefaultMQPushConsumerImpl defaultMQPushConsumerImpl) {",
        "    /** 以 Push 消费者构造 rebalance 实现。 */\n    public RebalancePushImpl(DefaultMQPushConsumerImpl defaultMQPushConsumerImpl) {",
    ),
    (
        "        /*\n         * When rebalance result changed, should update subscription's version to notify broker.\n         * Fix: inconsistency subscription may lead to consumer miss messages.\n         */",
        "        /*\n         * rebalance 结果变化时更新订阅 version 通知 Broker，\n         * 避免订阅不一致导致消费者漏消息。\n         */",
    ),
    (
        "        // notify broker",
        "        // 通知 Broker 心跳",
    ),
    (
        "    public boolean removeUnnecessaryMessageQueue(final MessageQueue mq, final ProcessQueue pq) {",
        "    /** 移除多余队列：顺序消费需解锁，否则持久化 offset 后删除。 */\n    public boolean removeUnnecessaryMessageQueue(final MessageQueue mq, final ProcessQueue pq) {",
    ),
    (
        "            // commit offset immediately",
        "            // 立即提交 offset",
    ),
    (
        "            // remove order message queue: unlock & remove",
        "            // 移除顺序消费队列：解锁并删除",
    ),
    (
        "    private boolean tryRemoveOrderMessageQueue(final MessageQueue mq, final ProcessQueue pq) {",
        "    /** 尝试移除顺序消费队列：无消费或超时后解锁并删除 offset。 */\n    private boolean tryRemoveOrderMessageQueue(final MessageQueue mq, final ProcessQueue pq) {",
    ),
    (
        "            // unlock & remove when no message is consuming or UNLOCK_DELAY_TIME_MILLS timeout (Backwards compatibility)",
        "            // 无消息消费中或超过 UNLOCK_DELAY_TIME_MILLS 时强制解锁并移除（向后兼容）",
    ),
    (
        "    public boolean clientRebalance(String topic) {",
        "    /** 判断是否由客户端执行 rebalance（客户端 rebalance/顺序/广播模式）。 */\n    public boolean clientRebalance(String topic) {",
    ),
    (
        "        // POPTODO order pop consume not implement yet",
        "        // POPTODO：顺序 POP 消费尚未实现",
    ),
    (
        "    public ConsumeType consumeType() {",
        "    /** 返回被动消费类型 {@link ConsumeType#CONSUME_PASSIVELY}。 */\n    public ConsumeType consumeType() {",
    ),
    (
        "    public long computePullFromWhereWithException(MessageQueue mq) throws MQClientException {",
        "    /** 按 ConsumeFromWhere 与 offsetStore 计算 push 模式 pull 起始 offset。 */\n    public long computePullFromWhereWithException(MessageQueue mq) throws MQClientException {",
    ),
    (
        "                // First start,no offset",
        "                // 首次启动，无 offset",
    ),
    (
        "                    //the offset will be fixed by the OFFSET_ILLEGAL process",
        "                    // offset 将由 OFFSET_ILLEGAL 流程修正",
    ),
    (
        "    public int getConsumeInitMode() {",
        "    /** 根据 ConsumeFromWhere 返回 POP initMode（MIN 或 MAX）。 */\n    public int getConsumeInitMode() {",
    ),
    (
        "    public void dispatchPullRequest(final List<PullRequest> pullRequestList, final long delay) {",
        "    /** 将 pull 请求立即或延迟提交到 {@link PullMessageService}。 */\n    public void dispatchPullRequest(final List<PullRequest> pullRequestList, final long delay) {",
    ),
    (
        "    public void dispatchPopPullRequest(final List<PopRequest> pullRequestList, final long delay) {",
        "    /** 将 POP 请求立即或延迟提交到 PullMessageService。 */\n    public void dispatchPopPullRequest(final List<PopRequest> pullRequestList, final long delay) {",
    ),
    (
        "    public ProcessQueue createProcessQueue() {",
        "    /** 创建标准 ProcessQueue。 */\n    public ProcessQueue createProcessQueue() {",
    ),
    (
        "    public PopProcessQueue createPopProcessQueue() {",
        "    /** 创建 PopProcessQueue。 */\n    public PopProcessQueue createPopProcessQueue() {",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/impl/consumer/PullAPIWrapper.java"] = [
    (
        "public class PullAPIWrapper {",
        "/**\n * Pull/POP API 封装：向 Broker 发起 pull/pop 请求，解码消息并执行过滤钩子。\n */\npublic class PullAPIWrapper {",
    ),
    (
        "    private final MQClientInstance mQClientFactory;",
        "    /** 客户端实例。 */\n    private final MQClientInstance mQClientFactory;",
    ),
    (
        "    private final String consumerGroup;",
        "    /** 消费组名。 */\n    private final String consumerGroup;",
    ),
    (
        "    private final boolean unitMode;",
        "    /** 是否单元化模式。 */\n    private final boolean unitMode;",
    ),
    (
        "    private ConcurrentMap<MessageQueue, AtomicLong/* brokerId */> pullFromWhichNodeTable =",
        "    /** 队列 -> 建议拉取 Broker 节点 ID 映射。 */\n    private ConcurrentMap<MessageQueue, AtomicLong/* brokerId */> pullFromWhichNodeTable =",
    ),
    (
        "    private volatile boolean connectBrokerByUser = false;",
        "    /** 是否由用户指定连接 Broker。 */\n    private volatile boolean connectBrokerByUser = false;",
    ),
    (
        "    private volatile long defaultBrokerId = MixAll.MASTER_ID;",
        "    /** 用户指定时的默认 Broker ID。 */\n    private volatile long defaultBrokerId = MixAll.MASTER_ID;",
    ),
    (
        "    private ArrayList<FilterMessageHook> filterMessageHookList = new ArrayList<>();",
        "    /** 消息过滤钩子列表。 */\n    private ArrayList<FilterMessageHook> filterMessageHookList = new ArrayList<>();",
    ),
    (
        "    public PullAPIWrapper(MQClientInstance mQClientFactory, String consumerGroup, boolean unitMode) {",
        "    /** 构造 Pull API 封装。 */\n    public PullAPIWrapper(MQClientInstance mQClientFactory, String consumerGroup, boolean unitMode) {",
    ),
    (
        "    public PullResult processPullResult(final MessageQueue mq, final PullResult pullResult,\n        final SubscriptionData subscriptionData) {",
        "    /**\n     * 处理 pull 结果：解码二进制、标签过滤、执行钩子并填充消息属性。\n     *\n     * @param mq 消息队列\n     * @param pullResult pull 结果（须为 {@link PullResultExt}）\n     * @param subscriptionData 订阅数据\n     */\n    public PullResult processPullResult(final MessageQueue mq, final PullResult pullResult,\n        final SubscriptionData subscriptionData) {",
    ),
    (
        "    public void updatePullFromWhichNode(final MessageQueue mq, final long brokerId) {",
        "    /** 更新队列建议拉取的 Broker 节点 ID。 */\n    public void updatePullFromWhichNode(final MessageQueue mq, final long brokerId) {",
    ),
    (
        "    public boolean hasHook() {",
        "    /** 是否注册了过滤钩子。 */\n    public boolean hasHook() {",
    ),
    (
        "    public void executeHook(final FilterMessageContext context) {",
        "    /** 依次执行所有 {@link FilterMessageHook}。 */\n    public void executeHook(final FilterMessageContext context) {",
    ),
    (
        "    public PullResult pullKernelImpl(",
        "    /**\n     * 向 Broker 内核发起 pull 请求（含 maxSizeInBytes）。\n     */\n    public PullResult pullKernelImpl(",
    ),
    (
        "                // check version",
        "                // 校验 Broker 版本是否支持当前表达式类型",
    ),
    (
        "    public long recalculatePullFromWhichNode(final MessageQueue mq) {",
        "    /** 重新计算应从哪个 Broker 节点拉取。 */\n    public long recalculatePullFromWhichNode(final MessageQueue mq) {",
    ),
    (
        "    private String computePullFromWhichFilterServer(final String topic, final String brokerAddr)",
        "    /** 计算类过滤模式下应连接的 Filter Server 地址。 */\n    private String computePullFromWhichFilterServer(final String topic, final String brokerAddr)",
    ),
    (
        "    public boolean isConnectBrokerByUser() {",
        "    /** 是否用户指定 Broker 连接。 */\n    public boolean isConnectBrokerByUser() {",
    ),
    (
        "    public void setConnectBrokerByUser(boolean connectBrokerByUser) {",
        "    /** 设置是否用户指定 Broker。 */\n    public void setConnectBrokerByUser(boolean connectBrokerByUser) {",
    ),
    (
        "    public void registerFilterMessageHook(ArrayList<FilterMessageHook> filterMessageHookList) {",
        "    /** 注册消息过滤钩子列表。 */\n    public void registerFilterMessageHook(ArrayList<FilterMessageHook> filterMessageHookList) {",
    ),
    (
        "    public long getDefaultBrokerId() {",
        "    /** 返回默认 Broker ID。 */\n    public long getDefaultBrokerId() {",
    ),
    (
        "    public void setDefaultBrokerId(long defaultBrokerId) {",
        "    /** 设置默认 Broker ID。 */\n    public void setDefaultBrokerId(long defaultBrokerId) {",
    ),
    (
        "    /**\n     *\n     * @param mq\n     * @param invisibleTime\n     * @param maxNums\n     * @param consumerGroup\n     * @param timeout\n     * @param popCallback\n     * @param poll\n     * @param initMode\n    //     * @param expressionType\n    //     * @param expression\n     * @param order\n     * @throws MQClientException\n     * @throws RemotingException\n     * @throws InterruptedException\n     */",
        "    /**\n     * 异步向 Broker 发起 POP 请求。\n     *\n     * @param mq 消息队列\n     * @param invisibleTime 消息不可见时长\n     * @param maxNums 最大消息数\n     * @param consumerGroup 消费组\n     * @param timeout 超时（毫秒）\n     * @param popCallback POP 回调\n     * @param poll 是否长轮询\n     * @param initMode 初始 offset 模式\n     * @param order 是否顺序消费\n     * @param expressionType 过滤表达式类型\n     * @param expression 过滤表达式\n     * @throws MQClientException\n     * @throws RemotingException\n     * @throws InterruptedException\n     */",
    ),
    (
        "            //give 1000 ms for server response",
        "            // 长轮询时为服务端响应预留时间",
    ),
    (
        "                // timeout + 10s, fix the too earlier timeout of client when long polling.",
        "                // 长轮询时 timeout 加 10s，避免客户端过早超时",
    ),
]
