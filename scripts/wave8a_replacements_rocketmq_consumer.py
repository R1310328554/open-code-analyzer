"""RocketMQ 5.5.0 wave8a client consumer [0:15] replacements."""

R: dict[str, list[tuple[str, str]]] = {}

R['client/src/main/java/org/apache/rocketmq/client/consumer/AckCallback.java'] = [
    (
        'public interface AckCallback {',
        '/**\n * POP 消息确认（Ack）异步回调接口。\n */\npublic interface AckCallback {',
    ),
    (
        '    void onSuccess(final AckResult ackResult);',
        '    /** Ack 成功时回调。 */\n    void onSuccess(final AckResult ackResult);',
    ),
    (
        '    void onException(final Throwable e);',
        '    /** Ack 失败或发生异常时回调。 */\n    void onException(final Throwable e);',
    ),
]

R['client/src/main/java/org/apache/rocketmq/client/consumer/AckResult.java'] = [
    (
        'public class AckResult {',
        '/** POP 消息 Ack 操作的结果封装。 */\npublic class AckResult {',
    ),
    (
        '    private AckStatus status;',
        '    /** Ack 状态。 */\n    private AckStatus status;',
    ),
    (
        '    private String extraInfo;',
        '    /** 附加信息（如错误详情）。 */\n    private String extraInfo;',
    ),
    (
        '    private long popTime;',
        '    /** Pop 操作时间戳。 */\n    private long popTime;',
    ),
    (
        '    public void setPopTime(long popTime) {',
        '    /** 设置 Pop 时间戳。 */\n    public void setPopTime(long popTime) {',
    ),
    (
        '    public long getPopTime() {',
        '    /** 获取 Pop 时间戳。 */\n    public long getPopTime() {',
    ),
    (
        '    public AckStatus getStatus() {',
        '    /** 获取 Ack 状态。 */\n    public AckStatus getStatus() {',
    ),
    (
        '    public void setStatus(AckStatus status) {',
        '    /** 设置 Ack 状态。 */\n    public void setStatus(AckStatus status) {',
    ),
    (
        '    public void setExtraInfo(String extraInfo) {',
        '    /** 设置附加信息。 */\n    public void setExtraInfo(String extraInfo) {',
    ),
    (
        '    public String getExtraInfo() {',
        '    /** 获取附加信息。 */\n    public String getExtraInfo() {',
    ),
]

R['client/src/main/java/org/apache/rocketmq/client/consumer/AckStatus.java'] = [
    (
        'public enum AckStatus {',
        '/** POP 消息 Ack 结果状态枚举。 */\npublic enum AckStatus {',
    ),
    (
        '    /**\n     * ack success\n     */',
        '    /** 确认成功。 */',
    ),
    (
        '    /**\n     * msg not exist\n     */',
        '    /** 消息不存在。 */',
    ),
]

R['client/src/main/java/org/apache/rocketmq/client/consumer/AllocateMessageQueueStrategy.java'] = [
    (
        '/**\n * Strategy Algorithm for message allocating between consumers\n */',
        '/**\n * 消费者在消费组内分配消息队列的策略算法。\n */',
    ),
    (
        '    /**\n     * Allocating by consumer id\n     *\n     * @param consumerGroup current consumer group\n     * @param currentCID current consumer id\n     * @param mqAll message queue set in current topic\n     * @param cidAll consumer set in current consumer group\n     * @return The allocate result of given strategy\n     */',
        '    /**\n     * 按当前消费者 ID 分配消息队列。\n     *\n     * @param consumerGroup 当前消费组\n     * @param currentCID 当前消费者 ID\n     * @param mqAll 当前 Topic 下全部消息队列\n     * @param cidAll 当前消费组内全部消费者 ID\n     * @return 本策略的分配结果\n     */',
    ),
    (
        '    /**\n     * Algorithm name\n     *\n     * @return The strategy name\n     */',
        '    /**\n     * 策略算法名称。\n     *\n     * @return 策略名称\n     */',
    ),
]

R['client/src/main/java/org/apache/rocketmq/client/consumer/LitePullConsumer.java'] = [
    (
        'public interface LitePullConsumer {',
        '/**\n * 轻量级主动拉取消费者接口：支持 assign 模式手动分配队列、poll 拉取与偏移量管理。\n */\npublic interface LitePullConsumer {',
    ),
    (
        '    /**\n     * Start the consumer\n     */',
        '    /** 启动消费者。 */',
    ),
    (
        '    /**\n     * Shutdown the consumer\n     */',
        '    /** 关闭消费者。 */',
    ),
    (
        '    /**\n     * This consumer is still running\n     *\n     * @return true if consumer is still running\n     */',
        '    /**\n     * 判断消费者是否仍在运行。\n     *\n     * @return 仍在运行返回 true\n     */',
    ),
    (
        '    /**\n     * Subscribe some topic with all tags\n     * @throws MQClientException if there is any client error.\n     */',
        '    /**\n     * 订阅 Topic（全部 Tag）。\n     * @throws MQClientException 客户端错误时抛出\n     */',
    ),
    (
        '    /**\n     * Subscribe some topic with subExpression\n     *\n     * @param subExpression subscription expression.it only support or operation such as "tag1 || tag2 || tag3" <br> if\n     * null or * expression,meaning subscribe all\n     * @throws MQClientException if there is any client error.\n     */',
        '    /**\n     * 按 Tag 子表达式订阅 Topic。\n     *\n     * @param subExpression 订阅表达式，仅支持或运算如 "tag1 || tag2 || tag3"；null 或 * 表示订阅全部\n     * @throws MQClientException 客户端错误时抛出\n     */',
    ),
    (
        '    /**\n     * Subscribe some topic with subExpression and messageQueueListener\n     * @param topic\n     * @param subExpression\n     * @param messageQueueListener\n     */',
        '    /**\n     * 订阅 Topic 并注册队列变更监听器。\n     * @param topic Topic 名称\n     * @param subExpression Tag 子表达式\n     * @param messageQueueListener 队列变更监听器\n     */',
    ),
    (
        '    /**\n     * Subscribe some topic with selector.\n     *\n     * @param selector message selector({@link MessageSelector}), can be null.\n     * @throws MQClientException if there is any client error.\n     */',
        '    /**\n     * 使用 {@link MessageSelector} 订阅 Topic。\n     *\n     * @param selector 消息选择器（{@link MessageSelector}），可为 null\n     * @throws MQClientException 客户端错误时抛出\n     */',
    ),
    (
        '    /**\n     * Unsubscribe consumption some topic\n     *\n     * @param topic Message topic that needs to be unsubscribe.\n     */',
        '    /**\n     * 取消订阅指定 Topic。\n     *\n     * @param topic 待取消订阅的 Topic\n     */',
    ),
    (
        '    /**\n     * subscribe mode, get assigned MessageQueue\n     * @return\n     * @throws MQClientException\n     */',
        '    /**\n     * 订阅模式下获取已分配的消息队列集合。\n     * @return 已分配队列\n     * @throws MQClientException 客户端错误时抛出\n     */',
    ),
    (
        '    /**\n     * Manually assign a list of message queues to this consumer. This interface does not allow for incremental\n     * assignment and will replace the previous assignment (if there is one).\n     *\n     * @param messageQueues Message queues that needs to be assigned.\n     */',
        '    /**\n     * 手动分配消息队列（全量替换，不支持增量追加）。\n     *\n     * @param messageQueues 待分配的队列列表\n     */',
    ),
    (
        '    /**\n     * Set topic subExpression for assign mode. This interface does not allow be call after start(). Default value is * if not set.\n     * assignment and will replace the previous assignment (if there is one).\n     *\n     * @param subExpression subscription expression.it only support or operation such as "tag1 || tag2 || tag3" <br> if\n     *      * null or * expression,meaning subscribe all\n     */',
        '    /**\n     * 为 assign 模式设置 Topic 的 Tag 子表达式（start 后不可调用，默认 *）。\n     *\n     * @param subExpression Tag 子表达式，仅支持或运算；null 或 * 表示全部\n     */',
    ),
    (
        '    void buildSubscriptionsForHeartbeat(Map<String, MessageSelector> subExpressionMap) throws Exception;',
        '    /** 为心跳构建订阅信息。 */\n    void buildSubscriptionsForHeartbeat(Map<String, MessageSelector> subExpressionMap) throws Exception;',
    ),
    (
        '    /**\n     * Fetch data for the topics or partitions specified using assign API\n     *\n     * @return list of message, can be null.\n     */',
        '    /**\n     * 拉取 assign 模式下已分配队列的消息（非阻塞）。\n     *\n     * @return 消息列表，可能为 null\n     */',
    ),
    (
        '    /**\n     * Fetch data for the topics or partitions specified using assign API\n     *\n     * @param timeout The amount time, in milliseconds, spent waiting in poll if data is not available. Must not be\n     * negative\n     * @return list of message, can be null.\n     */',
        '    /**\n     * 带超时的 poll 拉取。\n     *\n     * @param timeout 无数据时等待毫秒数，不可为负\n     * @return 消息列表，可能为 null\n     */',
    ),
    (
        '    /**\n     * Overrides the fetch offsets that the consumer will use on the next poll. If this API is invoked for the same\n     * message queue more than once, the latest offset will be used on the next poll(). Note that you may lose data if\n     * this API is arbitrarily used in the middle of consumption.\n     *\n     * @param messageQueue\n     * @param offset\n     */',
        '    /**\n     * 设置下次 poll 使用的拉取偏移量；同一队列多次调用以最后一次为准。\n     * 消费中途随意 seek 可能导致数据丢失。\n     *\n     * @param messageQueue 目标队列\n     * @param offset 目标偏移量\n     */',
    ),
    (
        '    /**\n     * Suspend pulling from the requested message queues.\n     *\n     * Because of the implementation of pre-pull, fetch data in {@link #poll()} will not stop immediately until the\n     * messages of the requested message queues drain.\n     *\n     * Note that this method does not affect message queue subscription. In particular, it does not cause a group\n     * rebalance.\n     *\n     * @param messageQueues Message queues that needs to be paused.\n     */',
        '    /**\n     * 暂停指定队列的拉取；因预拉取机制，{@link #poll()} 可能直到缓冲耗尽才停止。\n     * 不影响订阅关系，不会触发 rebalance。\n     *\n     * @param messageQueues 待暂停的队列\n     */',
    ),
    (
        '    /**\n     * Resume specified message queues which have been paused with {@link #pause(Collection)}.\n     *\n     * @param messageQueues Message queues that needs to be resumed.\n     */',
        '    /**\n     * 恢复此前 {@link #pause(Collection)} 暂停的队列。\n     *\n     * @param messageQueues 待恢复的队列\n     */',
    ),
    (
        '    /**\n     * Whether to enable auto-commit consume offset.\n     *\n     * @return true if enable auto-commit, false if disable auto-commit.\n     */',
        '    /**\n     * 是否启用消费偏移量自动提交。\n     *\n     * @return 启用返回 true\n     */',
    ),
    (
        '    /**\n     * Set whether to enable auto-commit consume offset.\n     *\n     * @param autoCommit Whether to enable auto-commit.\n     */',
        '    /**\n     * 设置是否自动提交消费偏移量。\n     *\n     * @param autoCommit 是否自动提交\n     */',
    ),
    (
        '    /**\n     * Get metadata about the message queues for a given topic.\n     *\n     * @param topic The topic that need to get metadata.\n     * @return collection of message queues\n     * @throws MQClientException if there is any client error.\n     */',
        '    /**\n     * 获取指定 Topic 的消息队列元数据。\n     *\n     * @param topic Topic 名称\n     * @return 队列集合\n     * @throws MQClientException 客户端错误时抛出\n     */',
    ),
    (
        '    /**\n     * Look up the offsets for the given message queue by timestamp. The returned offset for each message queue is the\n     * earliest offset whose timestamp is greater than or equal to the given timestamp in the corresponding message\n     * queue.\n     *\n     * @param messageQueue Message queues that needs to get offset by timestamp.\n     * @param timestamp\n     * @return offset\n     * @throws MQClientException if there is any client error.\n     */',
        '    /**\n     * 按时间戳查找队列偏移量：返回不小于给定时间戳的最早偏移。\n     *\n     * @param messageQueue 目标队列\n     * @param timestamp 时间戳\n     * @return 偏移量\n     * @throws MQClientException 客户端错误时抛出\n     */',
    ),
    (
        '    @Deprecated\n    /**\n     * The method is deprecated because its name is ambiguous, this method relies on the background thread commit consumerOffset rather than the synchronous commit offset.\n     * The method is expected to be removed after version 5.1.0. It is recommended to use the {@link #commit()} method.\n     *\n     * Manually commit consume offset saved by the system.\n     */',
        '    @Deprecated\n    /**\n     * 已废弃：名称易误解，实际由后台线程提交偏移量而非同步提交。\n     * 5.1.0 后移除，请改用 {@link #commit()}。\n     *\n     * 手动提交系统保存的消费偏移量。\n     */',
    ),
    (
        '    @Deprecated\n    /**\n     * The method is deprecated because its name is ambiguous, this method relies on the background thread commit consumerOffset rather than the synchronous commit offset.\n     * The method is expected to be removed after version 5.1.0. It is recommended to use the {@link #commit(java.util.Map, boolean)} method.\n     *\n     * @param offsetMap Offset specified by batch commit\n     */',
        '    @Deprecated\n    /**\n     * 已废弃：名称易误解，实际由后台线程提交。\n     * 5.1.0 后移除，请改用 {@link #commit(java.util.Map, boolean)}。\n     *\n     * @param offsetMap 批量提交的偏移量映射\n     */',
    ),
    (
        '    /**\n     * Manually commit consume offset saved by the system. This is a non-blocking method.\n     */',
        '    /** 非阻塞方式手动提交系统保存的消费偏移量。 */',
    ),
    (
        '    /**\n     * Offset specified by batch commit\n     *\n     * @param offsetMap Offset specified by batch commit\n     * @param persist Whether to persist to the broker\n     */',
        '    /**\n     * 按指定偏移量映射批量提交。\n     *\n     * @param offsetMap 队列到偏移量的映射\n     * @param persist 是否持久化到 Broker\n     */',
    ),
    (
        '    /**\n     * Manually commit consume offset saved by the system.\n     *\n     * @param messageQueues Message queues that need to submit consumer offset\n     * @param persist hether to persist to the broker\n     */',
        '    /**\n     * 提交指定队列的消费偏移量。\n     *\n     * @param messageQueues 待提交偏移量的队列\n     * @param persist 是否持久化到 Broker\n     */',
    ),
    (
        '    /**\n     * Get the last committed offset for the given message queue.\n     *\n     * @param messageQueue\n     * @return offset, if offset equals -1 means no offset in broker.\n     * @throws MQClientException if there is any client error.\n     */',
        '    /**\n     * 获取队列上次已提交的偏移量。\n     *\n     * @param messageQueue 目标队列\n     * @return 偏移量；-1 表示 Broker 无记录\n     * @throws MQClientException 客户端错误时抛出\n     */',
    ),
    (
        '    /**\n     * Register a callback for sensing topic metadata changes.\n     *\n     * @param topic The topic that need to monitor.\n     * @param topicMessageQueueChangeListener Callback when topic metadata changes, refer {@link\n     * TopicMessageQueueChangeListener}\n     * @throws MQClientException if there is any client error.\n     */',
        '    /**\n     * 注册 Topic 元数据（队列集合）变更回调。\n     *\n     * @param topic 待监听的 Topic\n     * @param topicMessageQueueChangeListener 变更回调，参见 {@link TopicMessageQueueChangeListener}\n     * @throws MQClientException 客户端错误时抛出\n     */',
    ),
    (
        '    /**\n     * Update name server addresses.\n     */',
        '    /** 更新 NameServer 地址。 */',
    ),
    (
        '    /**\n     * Overrides the fetch offsets with the begin offset that the consumer will use on the next poll. If this API is\n     * invoked for the same message queue more than once, the latest offset will be used on the next poll(). Note that\n     * you may lose data if this API is arbitrarily used in the middle of consumption.\n     *\n     * @param messageQueue\n     */',
        '    /**\n     * 将下次 poll 偏移设为队列起始位置；多次调用以最后一次为准。\n     *\n     * @param messageQueue 目标队列\n     */',
    ),
    (
        '    /**\n     * Overrides the fetch offsets with the end offset that the consumer will use on the next poll. If this API is\n     * invoked for the same message queue more than once, the latest offset will be used on the next poll(). Note that\n     * you may lose data if this API is arbitrarily used in the middle of consumption.\n     *\n     * @param messageQueue\n     */',
        '    /**\n     * 将下次 poll 偏移设为队列末尾；多次调用以最后一次为准。\n     *\n     * @param messageQueue 目标队列\n     */',
    ),
]

R['client/src/main/java/org/apache/rocketmq/client/consumer/MQConsumer.java'] = [
    (
        '/**\n * Message queue consumer interface\n */',
        '/**\n * 消息队列消费者基础接口，继承 {@link org.apache.rocketmq.client.MQAdmin} 管理能力。\n */',
    ),
    (
        '    /**\n     * If consuming of messages failed, they will be sent back to the brokers for another delivery attempt after\n     * interval specified in delay level.\n     */',
        '    /**\n     * 消费失败时将消息发回 Broker，按 delayLevel 延迟后重投（已废弃）。\n     */',
    ),
    (
        '    /**\n     * If consuming of messages failed, they will be sent back to the brokers for another delivery attempt after\n     * interval specified in delay level.\n     */',
        '    /**\n     * 消费失败时将消息发回指定 Broker，按 delayLevel 延迟后重投。\n     */',
    ),
    (
        '    /**\n     * Fetch message queues from consumer cache pertaining to the given topic.\n     *\n     * @param topic message topic\n     * @return queue set\n     */',
        '    /**\n     * 从消费者缓存获取已订阅 Topic 的消息队列集合。\n     *\n     * @param topic 消息 Topic\n     * @return 队列集合\n     */',
    ),
]

R['client/src/main/java/org/apache/rocketmq/client/consumer/MQPullConsumer.java'] = [
    (
        '/**\n * Pulling consumer interface\n */',
        '/**\n * 主动拉取型消费者接口：支持同步/异步 pull 与偏移量管理。\n */',
    ),
    (
        '    /**\n     * Start the consumer\n     */',
        '    /** 启动消费者。 */',
    ),
    (
        '    /**\n     * Shutdown the consumer\n     */',
        '    /** 关闭消费者。 */',
    ),
    (
        '    /**\n     * Register the message queue listener\n     */',
        '    /** 注册消息队列变更监听器。 */',
    ),
    (
        '    /**\n     * Pulling the messages,not blocking\n     *\n     * @param mq from which message queue\n     * @param subExpression subscription expression.it only support or operation such as "tag1 || tag2 || tag3" <br> if\n     * null or * expression,meaning subscribe all\n     * @param offset from where to pull\n     * @param maxNums max pulling numbers\n     * @return The resulting {@code PullRequest}\n     */',
        '    /**\n     * 非阻塞拉取消息（Tag 表达式）。\n     *\n     * @param mq 目标消息队列\n     * @param subExpression Tag 子表达式，null 或 * 表示全部\n     * @param offset 起始偏移量\n     * @param maxNums 最大拉取条数\n     * @return {@code PullResult}\n     */',
    ),
    (
        '    /**\n     * Pulling the messages in the specified timeout\n     *\n     * @return The resulting {@code PullRequest}\n     */',
        '    /**\n     * 带超时的拉取（Tag 表达式）。\n     *\n     * @return {@code PullResult}\n     */',
    ),
    (
        '    /**\n     * Pulling the messages, not blocking\n     * <p>\n     * support other message selection, such as {@link org.apache.rocketmq.common.filter.ExpressionType#SQL92}\n     * </p>\n     *\n     * @param mq from which message queue\n     * @param selector message selector({@link MessageSelector}), can be null.\n     * @param offset from where to pull\n     * @param maxNums max pulling numbers\n     * @return The resulting {@code PullRequest}\n     */',
        '    /**\n     * 非阻塞拉取（支持 {@link MessageSelector}，含 SQL92 等）。\n     *\n     * @param mq 目标消息队列\n     * @param selector 消息选择器（{@link MessageSelector}），可为 null\n     * @param offset 起始偏移量\n     * @param maxNums 最大拉取条数\n     * @return {@code PullResult}\n     */',
    ),
    (
        '    /**\n     * Pulling the messages in the specified timeout\n     * <p>\n     * support other message selection, such as {@link org.apache.rocketmq.common.filter.ExpressionType#SQL92}\n     * </p>\n     *\n     * @param mq from which message queue\n     * @param selector message selector({@link MessageSelector}), can be null.\n     * @param offset from where to pull\n     * @param maxNums max pulling numbers\n     * @param timeout Pulling the messages in the specified timeout\n     * @return The resulting {@code PullRequest}\n     */',
        '    /**\n     * 带超时的拉取（支持 {@link MessageSelector}）。\n     *\n     * @param mq 目标消息队列\n     * @param selector 消息选择器\n     * @param offset 起始偏移量\n     * @param maxNums 最大拉取条数\n     * @param timeout 超时毫秒数\n     * @return {@code PullResult}\n     */',
    ),
    (
        '    /**\n     * Pulling the messages in a async. way\n     */',
        '    /** 异步拉取（Tag 表达式，无超时参数）。 */',
    ),
    (
        '    /**\n     * Pulling the messages in a async. way\n     */',
        '    /** 异步拉取（Tag 表达式，带超时）。 */',
    ),
    (
        '    /**\n     * Pulling the messages in a async. way\n     */',
        '    /** 异步拉取（Tag 表达式，带 maxSize 与超时）。 */',
    ),
    (
        '    /**\n     * Pulling the messages in a async way. Support message selection\n     */',
        '    /** 异步拉取（{@link MessageSelector}，无超时）。 */',
    ),
    (
        '    /**\n     * Pulling the messages in a async. way. Support message selection\n     */',
        '    /** 异步拉取（{@link MessageSelector}，带超时）。 */',
    ),
    (
        '    /**\n     * Pulling the messages,if no message arrival,blocking some time\n     *\n     * @return The resulting {@code PullRequest}\n     */',
        '    /**\n     * 无消息时阻塞等待的拉取（Tag 表达式）。\n     *\n     * @return {@code PullResult}\n     */',
    ),
    (
        '    /**\n     * Pulling the messages through callback function,if no message arrival,blocking.\n     */',
        '    /** 无消息时阻塞的异步拉取（Tag 表达式）。 */',
    ),
    (
        '    /**\n     * Pulling the messages through callback function,if no message arrival,blocking. Support message selection\n     */',
        '    /** 无消息时阻塞的异步拉取（{@link MessageSelector}）。 */',
    ),
    (
        '    /**\n     * Pulling the messages,if no message arrival,blocking some time. Support message selection\n     *\n     * @return The resulting {@code PullRequest}\n     */',
        '    /**\n     * 无消息时阻塞等待的拉取（{@link MessageSelector}）。\n     *\n     * @return {@code PullResult}\n     */',
    ),
    (
        '    /**\n     * Update the offset\n     */',
        '    /** 更新本地消费偏移量。 */',
    ),
    (
        '    /**\n     * Fetch the offset\n     *\n     * @return The fetched offset of given queue\n     */',
        '    /**\n     * 获取消费偏移量。\n     *\n     * @return 指定队列的偏移量\n     */',
    ),
    (
        '    /**\n     * Fetch the message queues according to the topic\n     *\n     * @param topic message topic\n     * @return message queue set\n     */',
        '    /**\n     * 获取负载均衡后分配给本消费者的 Topic 队列。\n     *\n     * @param topic 消息 Topic\n     * @return 队列集合\n     */',
    ),
    (
        '    /**\n     * If consuming failure,message will be send back to the broker,and delay consuming in some time later.<br>\n     * Mind! message can only be consumed in the same group.\n     */',
        '    /**\n     * 消费失败时将消息发回 Broker 并延迟重投；仅能在同一消费组内消费。\n     */',
    ),
]

R['client/src/main/java/org/apache/rocketmq/client/consumer/MQPullConsumerScheduleService.java'] = [
    (
        '/**\n * Schedule service for pull consumer.\n * This Consumer will be removed in 2022, and a better implementation {@link\n * DefaultLitePullConsumer} is recommend to use in the scenario of actively pulling messages.\n */',
        '/**\n * Pull 消费者定时调度服务（遗留实现，建议改用 {@link DefaultLitePullConsumer} 主动拉取）。\n */',
    ),
    (
        '    private final Logger log = LoggerFactory.getLogger(MQPullConsumerScheduleService.class);',
        '    /** 日志记录器。 */\n    private final Logger log = LoggerFactory.getLogger(MQPullConsumerScheduleService.class);',
    ),
    (
        '    private final MessageQueueListener messageQueueListener = new MessageQueueListenerImpl();',
        '    /** 内部队列变更监听器。 */\n    private final MessageQueueListener messageQueueListener = new MessageQueueListenerImpl();',
    ),
    (
        '    private final ConcurrentMap<MessageQueue, PullTaskImpl> taskTable =',
        '    /** 队列到拉取任务的映射表。 */\n    private final ConcurrentMap<MessageQueue, PullTaskImpl> taskTable =',
    ),
    (
        '    private DefaultMQPullConsumer defaultMQPullConsumer;',
        '    /** 底层 Pull 消费者实例。 */\n    private DefaultMQPullConsumer defaultMQPullConsumer;',
    ),
    (
        '    private int pullThreadNums = 20;',
        '    /** 拉取线程池大小，默认 20。 */\n    private int pullThreadNums = 20;',
    ),
    (
        '    private ConcurrentMap<String /* topic */, PullTaskCallback> callbackTable =',
        '    /** Topic 到拉取回调的映射。 */\n    private ConcurrentMap<String /* topic */, PullTaskCallback> callbackTable =',
    ),
    (
        '    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;',
        '    /** 调度拉取任务的线程池。 */\n    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;',
    ),
    (
        '    public MQPullConsumerScheduleService(final String consumerGroup) {',
        '    /** 按消费组创建调度服务（集群模式）。 */\n    public MQPullConsumerScheduleService(final String consumerGroup) {',
    ),
    (
        '    public MQPullConsumerScheduleService(final String consumerGroup, final RPCHook rpcHook) {',
        '    /** 带 RPC Hook 的构造器。 */\n    public MQPullConsumerScheduleService(final String consumerGroup, final RPCHook rpcHook) {',
    ),
    (
        '    public void putTask(String topic, Set<MessageQueue> mqNewSet) {',
        '    /** 同步 Topic 的拉取任务：移除已下线队列，为新队列创建任务。 */\n    public void putTask(String topic, Set<MessageQueue> mqNewSet) {',
    ),
    (
        '    public void start() throws MQClientException {',
        '    /** 启动调度线程池与底层 Pull 消费者。 */\n    public void start() throws MQClientException {',
    ),
    (
        '    public void registerPullTaskCallback(final String topic, final PullTaskCallback callback) {',
        '    /** 注册 Topic 的拉取任务回调。 */\n    public void registerPullTaskCallback(final String topic, final PullTaskCallback callback) {',
    ),
    (
        '    public void shutdown() {',
        '    /** 关闭线程池与消费者。 */\n    public void shutdown() {',
    ),
    (
        '    public ConcurrentMap<String, PullTaskCallback> getCallbackTable() {',
        '    /** 获取回调映射表。 */\n    public ConcurrentMap<String, PullTaskCallback> getCallbackTable() {',
    ),
    (
        '    public void setCallbackTable(ConcurrentHashMap<String, PullTaskCallback> callbackTable) {',
        '    /** 设置回调映射表。 */\n    public void setCallbackTable(ConcurrentHashMap<String, PullTaskCallback> callbackTable) {',
    ),
    (
        '    public int getPullThreadNums() {',
        '    /** 获取拉取线程数。 */\n    public int getPullThreadNums() {',
    ),
    (
        '    public void setPullThreadNums(int pullThreadNums) {',
        '    /** 设置拉取线程数。 */\n    public void setPullThreadNums(int pullThreadNums) {',
    ),
    (
        '    public DefaultMQPullConsumer getDefaultMQPullConsumer() {',
        '    /** 获取底层 Pull 消费者。 */\n    public DefaultMQPullConsumer getDefaultMQPullConsumer() {',
    ),
    (
        '    public void setDefaultMQPullConsumer(DefaultMQPullConsumer defaultMQPullConsumer) {',
        '    /** 设置底层 Pull 消费者。 */\n    public void setDefaultMQPullConsumer(DefaultMQPullConsumer defaultMQPullConsumer) {',
    ),
    (
        '    public MessageModel getMessageModel() {',
        '    /** 获取消息模式（广播/集群）。 */\n    public MessageModel getMessageModel() {',
    ),
    (
        '    public void setMessageModel(MessageModel messageModel) {',
        '    /** 设置消息模式。 */\n    public void setMessageModel(MessageModel messageModel) {',
    ),
    (
        '    class MessageQueueListenerImpl implements MessageQueueListener {',
        '    /** 队列变更时按消息模式更新拉取任务。 */\n    class MessageQueueListenerImpl implements MessageQueueListener {',
    ),
    (
        '        public void messageQueueChanged(String topic, Set<MessageQueue> mqAll, Set<MessageQueue> mqDivided) {',
        '        /** 广播使用全部队列，集群使用分配子集。 */\n        public void messageQueueChanged(String topic, Set<MessageQueue> mqAll, Set<MessageQueue> mqDivided) {',
    ),
    (
        '    public class PullTaskImpl implements Runnable {',
        '    /** 单队列周期性拉取任务。 */\n    public class PullTaskImpl implements Runnable {',
    ),
    (
        '        private final MessageQueue messageQueue;',
        '        /** 本任务绑定的队列。 */\n        private final MessageQueue messageQueue;',
    ),
    (
        '        private volatile boolean cancelled = false;',
        '        /** 是否已取消。 */\n        private volatile boolean cancelled = false;',
    ),
    (
        '        public PullTaskImpl(final MessageQueue messageQueue) {',
        '        /** 创建指定队列的拉取任务。 */\n        public PullTaskImpl(final MessageQueue messageQueue) {',
    ),
    (
        '        public void run() {',
        '        /** 执行拉取回调并按上下文延迟调度下一次。 */\n        public void run() {',
    ),
    (
        '        public boolean isCancelled() {',
        '        /** 是否已取消。 */\n        public boolean isCancelled() {',
    ),
    (
        '        public void setCancelled(boolean cancelled) {',
        '        /** 设置取消标志。 */\n        public void setCancelled(boolean cancelled) {',
    ),
    (
        '        public MessageQueue getMessageQueue() {',
        '        /** 获取绑定的消息队列。 */\n        public MessageQueue getMessageQueue() {',
    ),
]

R['client/src/main/java/org/apache/rocketmq/client/consumer/MQPushConsumer.java'] = [
    (
        '/**\n * Push consumer\n */',
        '/**\n * 服务端推送型消费者接口：注册监听器后由 Broker 长轮询推送消息。\n */',
    ),
    (
        '    /**\n     * Start the consumer\n     */',
        '    /** 启动消费者。 */',
    ),
    (
        '    /**\n     * Shutdown the consumer\n     */',
        '    /** 关闭消费者。 */',
    ),
    (
        '    /**\n     * Register the message listener\n     */',
        '    /** 注册消息监听器（已废弃的通用接口）。 */',
    ),
    (
        '    void registerMessageListener(final MessageListenerConcurrently messageListener);',
        '    /** 注册并发消费监听器。 */\n    void registerMessageListener(final MessageListenerConcurrently messageListener);',
    ),
    (
        '    void registerMessageListener(final MessageListenerOrderly messageListener);',
        '    /** 注册顺序消费监听器。 */\n    void registerMessageListener(final MessageListenerOrderly messageListener);',
    ),
    (
        '    /**\n     * Subscribe some topic\n     *\n     * @param subExpression subscription expression.it only support or operation such as "tag1 || tag2 || tag3" <br> if\n     * null or * expression,meaning subscribe\n     * all\n     */',
        '    /**\n     * 按 Tag 子表达式订阅 Topic。\n     *\n     * @param subExpression Tag 子表达式，null 或 * 表示全部\n     */',
    ),
    (
        '    /**\n     * This method will be removed in the version 5.0.0,because filterServer was removed,and method <code>subscribe(final String topic, final MessageSelector messageSelector)</code>\n     * is recommended.\n     *\n     * Subscribe some topic\n     *\n     * @param fullClassName full class name,must extend org.apache.rocketmq.common.filter. MessageFilter\n     * @param filterClassSource class source code,used UTF-8 file encoding,must be responsible for your code safety\n     */',
        '    /**\n     * 已废弃：FilterServer 移除后将在 5.0.0 删除，请改用 {@code subscribe(topic, MessageSelector)}。\n     *\n     * @param fullClassName 过滤器全类名，须继承 MessageFilter\n     * @param filterClassSource 过滤器源码（UTF-8），需自行保证安全\n     */',
    ),
    (
        '    /**\n     * Subscribe some topic with selector.\n     * <p>\n     * This interface also has the ability of {@link #subscribe(String, String)},\n     * and, support other message selection, such as {@link org.apache.rocketmq.common.filter.ExpressionType#SQL92}.\n     * </p>\n     * <p/>\n     * <p>\n     * Choose Tag: {@link MessageSelector#byTag(java.lang.String)}\n     * </p>\n     * <p/>\n     * <p>\n     * Choose SQL92: {@link MessageSelector#bySql(java.lang.String)}\n     * </p>\n     *\n     * @param selector message selector({@link MessageSelector}), can be null.\n     */',
        '    /**\n     * 使用 {@link MessageSelector} 订阅 Topic，支持 Tag 与 SQL92。\n     * <p>Tag：{@link MessageSelector#byTag(java.lang.String)}</p>\n     * <p>SQL92：{@link MessageSelector#bySql(java.lang.String)}</p>\n     *\n     * @param selector 消息选择器（{@link MessageSelector}），可为 null\n     */',
    ),
    (
        '    /**\n     * Unsubscribe consumption some topic\n     *\n     * @param topic message topic\n     */',
        '    /**\n     * 取消订阅指定 Topic。\n     *\n     * @param topic 消息 Topic\n     */',
    ),
    (
        '    /**\n     * Update the consumer thread pool size Dynamically\n     */',
        '    /** 动态调整消费线程池核心线程数。 */',
    ),
    (
        '    /**\n     * Suspend the consumption\n     */',
        '    /** 暂停消费。 */',
    ),
    (
        '    /**\n     * Resume the consumption\n     */',
        '    /** 恢复消费。 */',
    ),
]

R['client/src/main/java/org/apache/rocketmq/client/consumer/MessageQueueListener.java'] = [
    (
        '/**\n * A MessageQueueListener is implemented by the application and may be specified when a message queue changed\n */',
        '/**\n * 消息队列变更监听器，由应用在队列分配变化时实现回调。\n */',
    ),
    (
        '    /**\n     * @param topic message topic\n     * @param mqAll all queues in this message topic\n     * @param mqAssigned collection of queues, assigned to the current consumer\n     */',
        '    /**\n     * 队列集合变更时回调。\n     * @param topic 消息 Topic\n     * @param mqAll 该 Topic 下全部队列\n     * @param mqAssigned 分配给当前消费者的队列子集\n     */',
    ),
]

R['client/src/main/java/org/apache/rocketmq/client/consumer/MessageSelector.java'] = [
    (
        '/**\n * Message selector: select message at server.\n * <p>\n * Now, support:\n * <li>Tag: {@link org.apache.rocketmq.common.filter.ExpressionType#TAG}\n * </li>\n * <li>SQL92: {@link org.apache.rocketmq.common.filter.ExpressionType#SQL92}\n * </li>\n * </p>\n */',
        '/**\n * 服务端消息过滤选择器。\n * <p>支持：Tag（{@link org.apache.rocketmq.common.filter.ExpressionType#TAG}）、\n * SQL92（{@link org.apache.rocketmq.common.filter.ExpressionType#SQL92}）。</p>\n */',
    ),
    (
        '    /**\n     * @see org.apache.rocketmq.common.filter.ExpressionType\n     */',
        '    /** 表达式类型，参见 {@link org.apache.rocketmq.common.filter.ExpressionType}。 */',
    ),
    (
        '    /**\n     * expression content.\n     */',
        '    /** 表达式内容。 */',
    ),
    (
        '    /**\n     * Use SQL92 to select message.\n     *\n     * @param sql if null or empty, will be treated as select all message.\n     */',
        '    /**\n     * 按 SQL92 表达式过滤。\n     *\n     * @param sql 为 null 或空时表示不过滤\n     */',
    ),
    (
        '    /**\n     * Use tag to select message.\n     *\n     * @param tag if null or empty or "*", will be treated as select all message.\n     */',
        '    /**\n     * 按 Tag 过滤。\n     *\n     * @param tag 为 null、空或 "*" 时表示全部\n     */',
    ),
    (
        '    public String getExpressionType() {',
        '    /** 获取表达式类型。 */\n    public String getExpressionType() {',
    ),
    (
        '    public String getExpression() {',
        '    /** 获取表达式内容。 */\n    public String getExpression() {',
    ),
]

R['client/src/main/java/org/apache/rocketmq/client/consumer/NotifyResult.java'] = [
    (
        'public class NotifyResult {',
        '/** POP 长轮询通知结果。 */\npublic class NotifyResult {',
    ),
    (
        '    private boolean hasMsg;',
        '    /** 是否有新消息。 */\n    private boolean hasMsg;',
    ),
    (
        '    private boolean pollingFull;',
        '    /** 长轮询池是否已满。 */\n    private boolean pollingFull;',
    ),
    (
        '    public boolean isHasMsg() {',
        '    /** 是否有新消息。 */\n    public boolean isHasMsg() {',
    ),
    (
        '    public boolean isPollingFull() {',
        '    /** 长轮询池是否已满。 */\n    public boolean isPollingFull() {',
    ),
    (
        '    public void setHasMsg(boolean hasMsg) {',
        '    /** 设置是否有新消息。 */\n    public void setHasMsg(boolean hasMsg) {',
    ),
    (
        '    public void setPollingFull(boolean pollingFull) {',
        '    /** 设置长轮询池是否已满。 */\n    public void setPollingFull(boolean pollingFull) {',
    ),
]

R['client/src/main/java/org/apache/rocketmq/client/consumer/PopCallback.java'] = [
    (
        '/**\n * Async message pop interface\n */',
        '/**\n * POP 消息异步拉取回调接口。\n */',
    ),
    (
        '    void onSuccess(final PopResult popResult);',
        '    /** Pop 成功时回调。 */\n    void onSuccess(final PopResult popResult);',
    ),
    (
        '    void onException(final Throwable e);',
        '    /** Pop 失败或异常时回调。 */\n    void onException(final Throwable e);',
    ),
]

R['client/src/main/java/org/apache/rocketmq/client/consumer/PopResult.java'] = [
    (
        'public class PopResult {',
        '/** POP 拉取操作的结果封装。 */\npublic class PopResult {',
    ),
    (
        '    private List<MessageExt> msgFoundList;',
        '    /** 拉取到的消息列表。 */\n    private List<MessageExt> msgFoundList;',
    ),
    (
        '    private PopStatus popStatus;',
        '    /** Pop 状态。 */\n    private PopStatus popStatus;',
    ),
    (
        '    private long popTime;',
        '    /** Pop 操作时间戳。 */\n    private long popTime;',
    ),
    (
        '    private long invisibleTime;',
        '    /** 消息不可见时长（毫秒）。 */\n    private long invisibleTime;',
    ),
    (
        '    private long restNum;',
        '    /** 队列中剩余可 Pop 消息数。 */\n    private long restNum;',
    ),
    (
        '    public PopResult(PopStatus popStatus, List<MessageExt> msgFoundList) {',
        '    /** 构造 Pop 结果。 */\n    public PopResult(PopStatus popStatus, List<MessageExt> msgFoundList) {',
    ),
    (
        '    public long getPopTime() {',
        '    /** 获取 Pop 时间戳。 */\n    public long getPopTime() {',
    ),
    (
        '    public void setPopTime(long popTime) {',
        '    /** 设置 Pop 时间戳。 */\n    public void setPopTime(long popTime) {',
    ),
    (
        '    public long getRestNum() {',
        '    /** 获取剩余可 Pop 数量。 */\n    public long getRestNum() {',
    ),
    (
        '    public void setRestNum(long restNum) {',
        '    /** 设置剩余可 Pop 数量。 */\n    public void setRestNum(long restNum) {',
    ),
    (
        '    public long getInvisibleTime() {',
        '    /** 获取不可见时长。 */\n    public long getInvisibleTime() {',
    ),
    (
        '    public void setInvisibleTime(long invisibleTime) {',
        '    /** 设置不可见时长。 */\n    public void setInvisibleTime(long invisibleTime) {',
    ),
    (
        '    public void setPopStatus(PopStatus popStatus) {',
        '    /** 设置 Pop 状态。 */\n    public void setPopStatus(PopStatus popStatus) {',
    ),
    (
        '    public PopStatus getPopStatus() {',
        '    /** 获取 Pop 状态。 */\n    public PopStatus getPopStatus() {',
    ),
    (
        '    public List<MessageExt> getMsgFoundList() {',
        '    /** 获取消息列表。 */\n    public List<MessageExt> getMsgFoundList() {',
    ),
    (
        '    public void setMsgFoundList(List<MessageExt> msgFoundList) {',
        '    /** 设置消息列表。 */\n    public void setMsgFoundList(List<MessageExt> msgFoundList) {',
    ),
]

R['client/src/main/java/org/apache/rocketmq/client/consumer/PopStatus.java'] = [
    (
        'public enum PopStatus {',
        '/** POP 拉取结果状态枚举。 */\npublic enum PopStatus {',
    ),
    (
        '    /**\n     * Founded\n     */',
        '    /** 拉取到消息。 */',
    ),
    (
        '    /**\n     * No new message can be pull after polling time out\n     * delete after next release\n     */',
        '    /** 长轮询超时且无新消息（后续版本将删除）。 */',
    ),
    (
        '    /**\n     * polling pool is full, do not try again immediately.\n     */',
        '    /** 长轮询池已满，不宜立即重试。 */',
    ),
    (
        '    /**\n     * polling time out but no message find\n     */',
        '    /** 长轮询超时但未找到消息。 */',
    ),
]
