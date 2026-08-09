"""RocketMQ 5.5.0 wave6a broker pop/orderly/processor [0:15] replacements."""

R: dict[str, list[tuple[str, str]]] = {}

R["broker/src/main/java/org/apache/rocketmq/broker/pop/PopConsumerKVStore.java"] = [
    (
        "public interface PopConsumerKVStore {",
        "/**\n * POP 消费状态 KV 存储抽象：负责 POP 投递记录的持久化、删除与过期扫描。\n * 典型实现为 {@link PopConsumerRocksdbStore}。\n */\npublic interface PopConsumerKVStore {",
    ),
    (
        "    /**\n     * Starts the storage service.\n     */",
        "    /** 启动存储服务。 */",
    ),
    (
        "    /**\n     * Shutdown the storage service.\n     */",
        "    /** 关闭存储服务。 */",
    ),
    (
        "    /**\n     * Gets the file path of the storage.\n     * @return The file path of the storage.\n     */",
        "    /**\n     * 返回存储目录路径。\n     * @return 存储文件路径\n     */",
    ),
    (
        "    /**\n     * Writes a list of consumer records to the storage.\n     * @param consumerRecordList The list of consumer records to be written.\n     */",
        "    /**\n     * 批量写入 POP 消费记录。\n     * @param consumerRecordList 待写入的记录列表\n     */",
    ),
    (
        "    /**\n     * Deletes a list of consumer records from the storage.\n     * @param consumerRecordList The list of consumer records to be deleted.\n     */",
        "    /**\n     * 批量删除 POP 消费记录。\n     * @param consumerRecordList 待删除的记录列表\n     */",
    ),
    (
        "    /**\n     * Scans and returns a list of expired consumer records within the specified time range.\n     * @param lowerTime The start time (inclusive) of the time range to search, in milliseconds.\n     * @param upperTime The end time (exclusive) of the time range to search, in milliseconds.\n     * @param maxCount The maximum number of records to return.\n     *                 Even if more records match the criteria, only this many will be returned.\n     * @return A list of expired consumer records within the specified time range.\n     *         If no matching records are found, an empty list is returned.\n     */",
        "    /**\n     * 扫描可见性超时时间落在 [lowerTime, upperTime) 区间内的过期记录。\n     * @param lowerTime 扫描下界（含），毫秒时间戳\n     * @param upperTime 扫描上界（不含），毫秒时间戳\n     * @param maxCount 最多返回条数\n     * @return 过期记录列表；无匹配时返回空列表\n     */",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/pop/PopConsumerLockService.java"] = [
    (
        "public class PopConsumerLockService {",
        "/**\n * POP 消费组-Topic 粒度互斥锁：防止同一 group@topic 并发 POP 导致状态错乱。\n * 锁超时后由 {@link #removeTimeout()} 清理。\n */\npublic class PopConsumerLockService {",
    ),
    (
        "    public PopConsumerLockService(long timeout) {",
        "    /** @param timeout 锁超时时间（毫秒），超时视为可重入 POP */\n    public PopConsumerLockService(long timeout) {",
    ),
    (
        "    public boolean tryLock(String key) {",
        "    /** 按 groupId@topicId 复合键尝试加锁。 */\n    public boolean tryLock(String key) {",
    ),
    (
        "    public boolean tryLock(String groupId, String topicId) {",
        "    /** 按消费组与 topic 尝试加锁。 */\n    public boolean tryLock(String groupId, String topicId) {",
    ),
    (
        "    public void unlock(String key) {",
        "    /** 释放指定复合键上的锁。 */\n    public void unlock(String key) {",
    ),
    (
        "    public void unlock(String groupId, String topicId) {",
        "    /** 释放指定消费组与 topic 上的锁。 */\n    public void unlock(String groupId, String topicId) {",
    ),
    (
        "    // For retry topics, should lock origin group and topic",
        "    // 重试 topic 需解析为原始 group/topic 再判断锁是否超时",
    ),
    (
        "    public boolean isLockTimeout(String groupId, String topicId) {",
        "    /** 判断锁是否已超时（不存在或持锁时间超过 timeout）。 */\n    public boolean isLockTimeout(String groupId, String topicId) {",
    ),
    (
        "    public void removeTimeout() {",
        "    /** 扫描 lockTable，移除已超时的锁条目。 */\n    public void removeTimeout() {",
    ),
    (
        "    static class TimedLock {",
        "    /** 带时间戳的可重入互斥锁，记录最近一次成功加锁时刻。 */\n    static class TimedLock {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/pop/PopConsumerRecord.java"] = [
    (
        "public class PopConsumerRecord {",
        "/**\n * POP 投递记录：序列化后写入 {@link PopConsumerKVStore}，用于可见性超时检查与重试调度。\n * Key 由可见性超时时间戳 + groupId + topicId + queueId + offset 组成。\n */\npublic class PopConsumerRecord {",
    ),
    (
        "    public enum RetryType {",
        "    /** POP 重试 topic 版本标识。 */\n    public enum RetryType {",
    ),
    (
        "        NORMAL_TOPIC(0),",
        "        /** 普通 topic，非重试。 */\n        NORMAL_TOPIC(0),",
    ),
    (
        "        RETRY_TOPIC_V1(1),",
        "        /** 重试 topic V1 格式。 */\n        RETRY_TOPIC_V1(1),",
    ),
    (
        "        RETRY_TOPIC_V2(2);",
        "        /** 重试 topic V2 格式。 */\n        RETRY_TOPIC_V2(2);",
    ),
    (
        "    // used for test and fastjson",
        "    // 供测试与 fastjson 反序列化使用",
    ),
    (
        "    /**\n     * Key: timestamp(8) + groupId + topicId + queueId + offset\n     */",
        "    /** Key 布局：可见性超时时间戳(8B) + groupId + topicId + queueId + offset */",
    ),
    (
        "    public byte[] getKeyBytes() {",
        "    /** 生成 RocksDB 存储键：以可见性超时时间为前缀便于范围扫描。 */\n    public byte[] getKeyBytes() {",
    ),
    (
        "    public boolean isRetry() {",
        "    /** 是否为重试 topic 上的 POP 记录。 */\n    public boolean isRetry() {",
    ),
    (
        "    public byte[] getValueBytes() {",
        "    /** 将记录序列化为 JSON 字节数组作为 value。 */\n    public byte[] getValueBytes() {",
    ),
    (
        "    public static PopConsumerRecord decode(byte[] body) {",
        "    /** 从 JSON 字节反序列化 POP 记录。 */\n    public static PopConsumerRecord decode(byte[] body) {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/pop/PopConsumerRocksdbStore.java"] = [
    (
        "public class PopConsumerRocksdbStore extends AbstractRocksDBStorage implements PopConsumerKVStore {",
        "/**\n * 基于 RocksDB 的 POP 消费状态存储：使用 popState 列族持久化 {@link PopConsumerRecord}。\n * Key 前缀为可见性超时时间戳，支持按时间范围扫描过期记录。\n */\npublic class PopConsumerRocksdbStore extends AbstractRocksDBStorage implements PopConsumerKVStore {",
    ),
    (
        "    public PopConsumerRocksdbStore(String filePath) {",
        "    /** @param filePath RocksDB 数据目录路径 */\n    public PopConsumerRocksdbStore(String filePath) {",
    ),
    (
        "    protected void initOptions() {",
        "    /** 初始化 DB/Write/Compact 选项，写路径开启 sync 保证 POP 状态可恢复。 */\n    protected void initOptions() {",
    ),
    (
        "    protected boolean postLoad() {",
        "    /** 创建 popState 列族并打开数据库。 */\n    protected boolean postLoad() {",
    ),
    (
        "    public String getFilePath() {",
        "    /** 返回 RocksDB 数据目录。 */\n    public String getFilePath() {",
    ),
    (
        "    public void writeRecords(List<PopConsumerRecord> consumerRecordList) {",
        "    /** 批量 Put POP 记录到 popState 列族。 */\n    public void writeRecords(List<PopConsumerRecord> consumerRecordList) {",
    ),
    (
        "    public void deleteRecords(List<PopConsumerRecord> consumerRecordList) {",
        "    /** 批量 Delete POP 记录（ACK 或超时清理时调用）。 */\n    public void deleteRecords(List<PopConsumerRecord> consumerRecordList) {",
    ),
    (
        "    public List<PopConsumerRecord> scanExpiredRecords(long lower, long upper, int maxCount) {",
        "    /** 按可见性超时时间范围扫描过期记录，利用 key 前缀索引加速迭代。 */\n    public List<PopConsumerRecord> scanExpiredRecords(long lower, long upper, int maxCount) {",
    ),
    (
        "    protected void preShutdown() {",
        "    /** 关闭 WriteOptions 与列族句柄。 */\n    protected void preShutdown() {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/pop/orderly/ConsumerOrderInfoManager.java"] = [
    (
        "/**\n *\n * Ordered Consumption Controller Interface\n * This is the top-level interface that encapsulates complete ordered consumption management functionality,\n * supporting different concurrency strategy implementations\n * <p>\n * Design Goals:\n * 1. Support queue-level ordered consumption (existing implementation)\n * 2. Support message group-level ordered consumption (improve concurrency)\n * 3. Support custom ordered consumption strategies\n * </p>\n */",
        "/**\n * 顺序消费控制器顶层接口：封装 POP 顺序消费的完整生命周期管理，\n * 支持不同并发策略实现。\n * <p>\n * 设计目标：\n * 1. 队列级顺序消费（现有实现）\n * 2. 消息组级顺序消费（提升并发度）\n * 3. 可插拔的自定义顺序消费策略\n * </p>\n */",
    ),
    (
        "    /**\n     * Update the reception status of message list\n     * Called by handleGetMessageResult when consumer POPs messages, used to record message status and build consumption information\n     *\n     * @param attemptId          Distinguish different pop requests\n     * @param isRetry            Whether it is a retry topic\n     * @param topic              Topic name\n     * @param group              Consumer group name\n     * @param queueId            Queue ID\n     * @param popTime            Time when messages are popped\n     * @param invisibleTime      Message invisible time\n     * @param msgQueueOffsetList List of message queue offsets\n     * @param orderInfoBuilder   String builder for constructing order information\n     * @param getMessageResult   Return new result\n     */",
        "    /**\n     * POP 成功后更新消息接收状态，记录各 offset 的可见性信息并构建 orderInfo。\n     *\n     * @param attemptId          区分不同 POP 请求\n     * @param isRetry            是否为重试 topic\n     * @param topic              Topic 名\n     * @param group              消费组名\n     * @param queueId            队列 ID\n     * @param popTime            POP 时刻\n     * @param invisibleTime      消息不可见时长\n     * @param msgQueueOffsetList 消息队列 offset 列表\n     * @param orderInfoBuilder   构建 orderInfo 的 StringBuilder\n     * @param getMessageResult   返回给客户端的消息结果\n     */",
    ),
    (
        "    /**\n     * Check whether the current POP request needs to be blocked\n     * Used to ensure ordered consumption of ordered messages\n     * Called when consumer POPs messages\n     *\n     * @param attemptId     Attempt ID\n     * @param topic         Topic name\n     * @param group         Consumer group name\n     * @param queueId       Queue ID\n     * @param invisibleTime Invisible time\n     * @return true indicates blocking is needed, false indicates can proceed\n     */",
        "    /**\n     * 检查当前 POP 是否应被阻塞，以保证顺序 topic 的严格有序消费。\n     *\n     * @param attemptId     请求 attemptId\n     * @param topic         Topic 名\n     * @param group         消费组名\n     * @param queueId       队列 ID\n     * @param invisibleTime 不可见时长\n     * @return true 表示需阻塞，false 表示可继续 POP\n     */",
    ),
    (
        "    /**\n     * Remove the specified topic and group\n     * Usually called during topic deletion\n     *\n     * @param topic Topic name\n     * @param group Consumer group name\n     */",
        "    /**\n     * 删除指定 topic@group 的顺序消费状态（topic 删除时调用）。\n     *\n     * @param topic Topic 名\n     * @param group 消费组名\n     */",
    ),
    (
        "    /**\n     * Get order info count\n     */",
        "    /** 返回当前维护的 orderInfo 条目数。 */",
    ),
    (
        "    /**\n     * Commit message and calculate next consumption offset\n     * Called when consumer ACKs messages\n     *\n     * @param topic       Topic name\n     * @param group       Consumer group name\n     * @param queueId     Queue ID\n     * @param queueOffset Message queue offset\n     * @param popTime     Pop time, used for validation\n     * @return -1: invalid, -2: no need to commit, >=0: offset that needs to be committed (indicates messages below this offset have been consumed)\n     */",
        "    /**\n     * ACK 时提交消费进度并计算下一可消费 offset。\n     *\n     * @param topic       Topic 名\n     * @param group       消费组名\n     * @param queueId     队列 ID\n     * @param queueOffset 消息队列 offset\n     * @param popTime     POP 时刻，用于校验\n     * @return -1 无效；-2 无需提交；>=0 应提交的 offset\n     */",
    ),
    (
        "    /**\n     * Update the next visible time of message\n     * Used for delayed message re-consumption\n     *\n     * @param topic           Topic name\n     * @param group           Consumer group name\n     * @param queueId         Queue ID\n     * @param queueOffset     Message offset\n     * @param popTime         Pop time, used for validation\n     * @param nextVisibleTime Next visible time\n     */",
        "    /**\n     * 更新消息下次可见时间（延迟重消费场景）。\n     *\n     * @param topic           Topic 名\n     * @param group           消费组名\n     * @param queueId         队列 ID\n     * @param queueOffset     消息 offset\n     * @param popTime         POP 时刻\n     * @param nextVisibleTime 下次可见时间戳\n     */",
    ),
    (
        "    /**\n     * Clear the blocking status of specified queue\n     * Usually called during consumer rebalancing or queue reassignment\n     *\n     * @param topic   Topic name\n     * @param group   Consumer group name\n     * @param queueId Queue ID\n     */",
        "    /**\n     * 清除指定队列的阻塞状态（重平衡或队列迁移时调用）。\n     *\n     * @param topic   Topic 名\n     * @param group   消费组名\n     * @param queueId 队列 ID\n     */",
    ),
    (
        "    /**\n     * Get ordered consumption level\n     * Used to distinguish different implementation strategies\n     *\n     * @return Ordered consumption level, such as: QUEUE, MESSAGE_GROUP, etc.\n     */",
        "    /**\n     * 返回顺序消费粒度（QUEUE、MESSAGE_GROUP 等）。\n     *\n     * @return 顺序消费级别枚举\n     */",
    ),
    (
        "    /**\n     * Start the controller\n     * Initialize necessary resources, such as timers, thread pools, etc.\n     */",
        "    /** 启动控制器，初始化定时器、线程池等资源。 */",
    ),
    (
        "    /**\n     * Shutdown the controller\n     * Release resources, clean up scheduled tasks, etc.\n     */",
        "    /** 关闭控制器并释放资源。 */",
    ),
    (
        "    /**\n     * Persist the controller\n     * Persist the controller's data\n     */",
        "    /** 持久化顺序消费状态到磁盘。 */",
    ),
    (
        "    /**\n     * Get available message result\n     * Used to retrieve messages from cache\n     */",
        "    /** 从缓存中获取可立即 POP 的消息结果。 */",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/pop/orderly/QueueLevelConsumerOrderInfoLockManager.java"] = [
    (
        "public class QueueLevelConsumerOrderInfoLockManager {",
        "/**\n * 队列级顺序消费锁释放通知管理器：在 lockFreeTimestamp 到达时\n * 唤醒长轮询或 Lite 事件分发，避免顺序 POP 长时间空等。\n */\npublic class QueueLevelConsumerOrderInfoLockManager {",
    ),
    (
        "    /**\n     * when QueueLevelConsumerManager load from disk, recover data\n     */",
        "    /** 从磁盘恢复 orderInfo 后，为尚未到期的 lockFreeTimestamp 重建定时任务。 */",
    ),
    (
        "    public void recover(Map<String/* topic@group*/, ConcurrentHashMap<Integer/*queueId*/, QueueLevelConsumerManager.OrderInfo>> table) {",
        "    /** 遍历持久化表，为每个未到期的 lockFreeTimestamp 注册 HashedWheel 定时器。 */\n    public void recover(Map<String/* topic@group*/, ConcurrentHashMap<Integer/*queueId*/, QueueLevelConsumerManager.OrderInfo>> table) {",
    ),
    (
        "    public void updateLockFreeTimestamp(String topic, String group, int queueId, QueueLevelConsumerManager.OrderInfo orderInfo) {",
        "    /** 从 OrderInfo 提取 lockFreeTimestamp 并更新定时任务。 */\n    public void updateLockFreeTimestamp(String topic, String group, int queueId, QueueLevelConsumerManager.OrderInfo orderInfo) {",
    ),
    (
        "    public void updateLockFreeTimestamp(String topic, String group, int queueId, Long lockFreeTimestamp) {",
        "    /** 注册/刷新 lockFreeTimestamp 到期后的通知定时器，新任务会取消旧任务。 */\n    public void updateLockFreeTimestamp(String topic, String group, int queueId, Long lockFreeTimestamp) {",
    ),
    (
        "    protected void notifyLockIsFree(Key key) {",
        "    /** 锁释放到期：Lite topic 走事件分发，普通 topic 唤醒 POP 长轮询。 */\n    protected void notifyLockIsFree(Key key) {",
    ),
    (
        "    public void shutdown() {",
        "    /** 停止 HashedWheelTimer。 */\n    public void shutdown() {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/processor/ClientManageProcessor.java"] = [
    (
        "public class ClientManageProcessor implements NettyRequestProcessor {",
        "/**\n * 客户端管理处理器：处理心跳注册、注销与订阅配置校验。\n * 维护 Producer/Consumer 与 Broker 之间的连接与会话状态。\n */\npublic class ClientManageProcessor implements NettyRequestProcessor {",
    ),
    (
        "    public ClientManageProcessor(final BrokerController brokerController) {",
        "    /** @param brokerController Broker 控制器 */\n    public ClientManageProcessor(final BrokerController brokerController) {",
    ),
    (
        "    public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request)",
        "    /** 分发 HEART_BEAT / UNREGISTER_CLIENT / CHECK_CLIENT_CONFIG 请求。 */\n    public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request)",
    ),
    (
        "    public RemotingCommand heartBeat(ChannelHandlerContext ctx, RemotingCommand request) {",
        "    /** 处理 V1 心跳：注册 Consumer/Producer 并自动创建重试 topic。 */\n    public RemotingCommand heartBeat(ChannelHandlerContext ctx, RemotingCommand request) {",
    ),
    (
        "    private RemotingCommand heartBeatV2(ChannelHandlerContext ctx, HeartbeatData heartbeatData, ClientChannelInfo clientChannelInfo, RemotingCommand response) {",
        "    /** 处理 V2 心跳：支持 fingerprint 检测订阅变更与 withoutSub 轻量注册。 */\n    private RemotingCommand heartBeatV2(ChannelHandlerContext ctx, HeartbeatData heartbeatData, ClientChannelInfo clientChannelInfo, RemotingCommand response) {",
    ),
    (
        "    public RemotingCommand unregisterClient(ChannelHandlerContext ctx, RemotingCommand request)",
        "    /** 注销指定 Producer/Consumer 客户端连接。 */\n    public RemotingCommand unregisterClient(ChannelHandlerContext ctx, RemotingCommand request)",
    ),
    (
        "    public RemotingCommand checkClientConfig(ChannelHandlerContext ctx, RemotingCommand request)",
        "    /** 校验客户端订阅表达式能否在 Broker 侧编译通过。 */\n    public RemotingCommand checkClientConfig(ChannelHandlerContext ctx, RemotingCommand request)",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/processor/ConsumerManageProcessor.java"] = [
    (
        "public class ConsumerManageProcessor implements NettyRequestProcessor {",
        "/**\n * 消费管理处理器：查询消费组在线 client、更新/查询消费位点。\n * 静态 topic 场景下负责逻辑队列到物理队列的位点映射与 RPC 转发。\n */\npublic class ConsumerManageProcessor implements NettyRequestProcessor {",
    ),
    (
        "    public ConsumerManageProcessor(final BrokerController brokerController) {",
        "    /** @param brokerController Broker 控制器 */\n    public ConsumerManageProcessor(final BrokerController brokerController) {",
    ),
    (
        "    public RemotingCommand getConsumerListByGroup(ChannelHandlerContext ctx, RemotingCommand request)",
        "    /** 返回指定消费组下所有在线 clientId 列表。 */\n    public RemotingCommand getConsumerListByGroup(ChannelHandlerContext ctx, RemotingCommand request)",
    ),
    (
        "    public RemotingCommand rewriteRequestForStaticTopic(final UpdateConsumerOffsetRequestHeader requestHeader,\n        final TopicQueueMappingContext mappingContext) {",
        "    /** 静态 topic 更新位点：将逻辑 offset 映射为物理 offset，必要时转发至目标 Broker。 */\n    public RemotingCommand rewriteRequestForStaticTopic(final UpdateConsumerOffsetRequestHeader requestHeader,\n        final TopicQueueMappingContext mappingContext) {",
    ),
    (
        "    private RemotingCommand updateConsumerOffset(ChannelHandlerContext ctx, RemotingCommand request)",
        "    /** 提交消费位点；若 Broker 已执行 server-side reset 则忽略客户端更新。 */\n    private RemotingCommand updateConsumerOffset(ChannelHandlerContext ctx, RemotingCommand request)",
    ),
    (
        "    public RemotingCommand rewriteRequestForStaticTopic(QueryConsumerOffsetRequestHeader requestHeader,\n        TopicQueueMappingContext mappingContext) {",
        "    /** 静态 topic 查询位点：双读 leader/副本 Broker 并合并逻辑 offset。 */\n    public RemotingCommand rewriteRequestForStaticTopic(QueryConsumerOffsetRequestHeader requestHeader,\n        TopicQueueMappingContext mappingContext) {",
    ),
    (
        "    public RemotingCommand rewriteResponseForStaticTopic(final QueryConsumerOffsetRequestHeader requestHeader,\n        final QueryConsumerOffsetResponseHeader responseHeader,\n        final TopicQueueMappingContext mappingContext, final int code) {",
        "    /** 将物理 offset 反算为静态 topic 逻辑 offset 写入响应。 */\n    public RemotingCommand rewriteResponseForStaticTopic(final QueryConsumerOffsetRequestHeader requestHeader,\n        final QueryConsumerOffsetResponseHeader responseHeader,\n        final TopicQueueMappingContext mappingContext, final int code) {",
    ),
    (
        "    private RemotingCommand queryConsumerOffset(ChannelHandlerContext ctx, RemotingCommand request)",
        "    /** 查询消费位点；未找到时可按 setZeroIfNotFound 策略返回 0 或 NOT_FOUND。 */\n    private RemotingCommand queryConsumerOffset(ChannelHandlerContext ctx, RemotingCommand request)",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/processor/DefaultPullMessageResultHandler.java"] = [
    (
        "public class DefaultPullMessageResultHandler implements PullMessageResultHandler {",
        "/**\n * 默认 Pull 结果处理器：组装响应头、执行 Hook、更新广播位点，\n * 并按堆内存或 PageCache 零拷贝方式向客户端传输消息。\n */\npublic class DefaultPullMessageResultHandler implements PullMessageResultHandler {",
    ),
    (
        "    public DefaultPullMessageResultHandler(final BrokerController brokerController) {",
        "    /** @param brokerController Broker 控制器 */\n    public DefaultPullMessageResultHandler(final BrokerController brokerController) {",
    ),
    (
        "    public RemotingCommand handle(final GetMessageResult getMessageResult,",
        "    /** 处理 Pull 取消息结果：统计、长轮询挂起、offset 校正与消息传输。 */\n    public RemotingCommand handle(final GetMessageResult getMessageResult,",
    ),
    (
        "    private boolean channelIsWritable(Channel channel, PullMessageRequestHeader requestHeader) {",
        "    /** 网络流控开启时检查 channel 是否可写，不可写则丢弃本次 Pull 响应。 */\n    private boolean channelIsWritable(Channel channel, PullMessageRequestHeader requestHeader) {",
    ),
    (
        "    protected byte[] readGetMessageResult(final GetMessageResult getMessageResult, final String group,",
        "    /** 堆模式：合并消息 buffer 并记录磁盘落后时间。 */\n    protected byte[] readGetMessageResult(final GetMessageResult getMessageResult, final String group,",
    ),
    (
        "    protected void generateOffsetMovedEvent(final OffsetMovedEvent event) {",
        "    /** 将 offset 漂移事件写入系统 topic 供客户端感知。 */\n    protected void generateOffsetMovedEvent(final OffsetMovedEvent event) {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/processor/EndTransactionProcessor.java"] = [
    (
        "/**\n * EndTransaction processor: process commit and rollback message\n */",
        "/**\n * 事务结束处理器：处理 Producer 提交/回滚半消息，\n * 校验 prepare 消息后将最终消息写入 CommitLog 或删除半消息。\n */",
    ),
    (
        "    public EndTransactionProcessor(final BrokerController brokerController) {",
        "    /** @param brokerController Broker 控制器 */\n    public EndTransactionProcessor(final BrokerController brokerController) {",
    ),
    (
        "    public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request) throws",
        "    /** 处理 END_TRANSACTION 请求：slave 拒绝；区分事务回查与 Producer 主动结束。 */\n    public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request) throws",
    ),
    (
        "    private void deletePrepareMessage(OperationResult result) {",
        "    /** 根据半消息 topic 类型从 CommitLog 或 RocksDB 删除 prepare 消息。 */\n    private void deletePrepareMessage(OperationResult result) {",
    ),
    (
        "    /**\n     * If you specify a custom first check time CheckImmunityTimeInSeconds,\n     * And the commit/rollback request whose validity period exceeds CheckImmunityTimeInSeconds and is not checked back will be processed and failed\n     * returns ILLEGAL_OPERATION 604 error\n     * @param requestHeader\n     * @param messageExt\n     * @return\n     */",
        "    /**\n     * 若消息设置了 CheckImmunityTimeInSeconds 且 Producer 主动 commit/rollback\n     * 超出免疫期且尚未回查，则拒绝并返回 ILLEGAL_OPERATION。\n     * @param requestHeader 事务结束请求头\n     * @param messageExt prepare 消息\n     * @return true 表示应拒绝本次操作\n     */",
    ),
    (
        "    private RemotingCommand checkPrepareMessage(MessageExt msgExt, EndTransactionRequestHeader requestHeader) {",
        "    /** 校验 prepare 消息的 producerGroup、tranStateTableOffset 与 commitLogOffset。 */\n    private RemotingCommand checkPrepareMessage(MessageExt msgExt, EndTransactionRequestHeader requestHeader) {",
    ),
    (
        "    private MessageExtBrokerInner endMessageTransaction(MessageExt msgExt) {",
        "    /** 将半消息还原为最终消息（恢复真实 topic/queueId 并清理临时属性）。 */\n    private MessageExtBrokerInner endMessageTransaction(MessageExt msgExt) {",
    ),
    (
        "    private RemotingCommand sendFinalMessage(MessageExtBrokerInner msgInner) {",
        "    /** 将最终消息写入 CommitLog 并映射 PutMessageStatus 到响应码。 */\n    private RemotingCommand sendFinalMessage(MessageExtBrokerInner msgInner) {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/processor/LiteSubscriptionCtlProcessor.java"] = [
    (
        "public class LiteSubscriptionCtlProcessor implements NettyRequestProcessor {",
        "/**\n * Lite 订阅控制处理器：处理客户端增量/全量订阅注册与注销，\n * 维护 Lite topic 到 LMQ 的映射关系。\n */\npublic class LiteSubscriptionCtlProcessor implements NettyRequestProcessor {",
    ),
    (
        "    public LiteSubscriptionCtlProcessor(BrokerController brokerController, LiteSubscriptionRegistry liteSubscriptionRegistry) {",
        "    /** @param brokerController Broker 控制器；@param liteSubscriptionRegistry Lite 订阅注册表 */\n    public LiteSubscriptionCtlProcessor(BrokerController brokerController, LiteSubscriptionRegistry liteSubscriptionRegistry) {",
    ),
    (
        "    public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request) throws Exception {",
        "    /** 按 action 分发 PARTIAL/COMPLETE 的 ADD/REMOVE 订阅操作。 */\n    public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request) throws Exception {",
    ),
    (
        "    private void checkConsumeEnable(String group) {",
        "    /** 校验消费组是否允许消费，否则抛出 IllegalStateException。 */\n    private void checkConsumeEnable(String group) {",
    ),
    (
        "    private Set<String> toLmqNameSet(LiteSubscriptionDTO liteSubscriptionDTO) {",
        "    /** 将 Lite topic 集合转换为 LMQ 全名集合。 */\n    private Set<String> toLmqNameSet(LiteSubscriptionDTO liteSubscriptionDTO) {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/processor/NotificationProcessor.java"] = [
    (
        "public class NotificationProcessor implements NettyRequestProcessor {",
        "/**\n * POP 通知处理器：客户端查询 topic/queue 是否有新消息，\n * 无消息时进入长轮询；CommitLog 写入时唤醒挂起请求。\n */\npublic class NotificationProcessor implements NettyRequestProcessor {",
    ),
    (
        "    public NotificationProcessor(final BrokerController brokerController) {",
        "    /** @param brokerController Broker 控制器 */\n    public NotificationProcessor(final BrokerController brokerController) {",
    ),
    (
        "    public void shutdown() throws Exception {",
        "    /** 关闭 POP 长轮询服务。 */\n    public void shutdown() throws Exception {",
    ),
    (
        "    // When a new message is written to CommitLog, this method would be called.\n    // Suspended long polling will receive notification and be wakeup.",
        "    // CommitLog 写入新消息时调用，唤醒对应 topic@queue 上挂起的长轮询",
    ),
    (
        "    public void notifyMessageArriving(final String topic, final int queueId, long offset,",
        "    /** 带消息元数据的通知：支持 Tag/SQL 过滤位图匹配后唤醒长轮询。 */\n    public void notifyMessageArriving(final String topic, final int queueId, long offset,",
    ),
    (
        "    public void notifyMessageArriving(final String topic, final int queueId) {",
        "    /** 简化通知：仅按 topic@queueId 唤醒长轮询。 */\n    public void notifyMessageArriving(final String topic, final int queueId) {",
    ),
    (
        "    public RemotingCommand processRequest(final ChannelHandlerContext ctx,",
        "    /** 处理 Notification 请求：检查队列是否有消息，无则挂起长轮询。 */\n    public RemotingCommand processRequest(final ChannelHandlerContext ctx,",
    ),
    (
        "    private long getPopOffset(String topic, String cid, int queueId) {",
        "    /** 计算 POP 有效起始 offset：取 consumerOffset 与 bufferOffset 的较大值。 */\n    private long getPopOffset(String topic, String cid, int queueId) {",
    ),
    (
        "    public PopLongPollingService getPopLongPollingService() {",
        "    /** 返回 POP 长轮询服务实例。 */\n    public PopLongPollingService getPopLongPollingService() {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/processor/PeekMessageProcessor.java"] = [
    (
        "public class PeekMessageProcessor implements NettyRequestProcessor {",
        "/**\n * Peek 消息处理器：只读预览队列消息而不改变 POP 状态，\n * 用于客户端探测可用消息量；支持重试 topic 与 PageCache 零拷贝传输。\n */\npublic class PeekMessageProcessor implements NettyRequestProcessor {",
    ),
    (
        "    public PeekMessageProcessor(final BrokerController brokerController) {",
        "    /** @param brokerController Broker 控制器 */\n    public PeekMessageProcessor(final BrokerController brokerController) {",
    ),
    (
        "    private RemotingCommand processRequest(final Channel channel, RemotingCommand request, boolean brokerAllowSuspend)",
        "    /** 校验权限与订阅后，从主 topic 与重试 topic 读取消息并返回。 */\n    private RemotingCommand processRequest(final Channel channel, RemotingCommand request, boolean brokerAllowSuspend)",
    ),
    (
        "    private long peekMsgFromQueue(boolean isRetry, GetMessageResult getMessageResult,",
        "    /** 从指定队列 peek 消息并累加到结果集，同时统计剩余消息数 restNum。 */\n    private long peekMsgFromQueue(boolean isRetry, GetMessageResult getMessageResult,",
    ),
    (
        "    private long getPopOffset(String topic, String cid, int queueId) {",
        "    /** 计算 peek 起始 offset：consumerOffset 与 PopBuffer 最新 offset 取 max。 */\n    private long getPopOffset(String topic, String cid, int queueId) {",
    ),
    (
        "    private byte[] readGetMessageResult(final GetMessageResult getMessageResult, final String group, final String topic,",
        "    /** 堆模式合并 peek 结果并记录磁盘落后时间。 */\n    private byte[] readGetMessageResult(final GetMessageResult getMessageResult, final String group, final String topic,",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/processor/PollingInfoProcessor.java"] = [
    (
        "public class PollingInfoProcessor implements NettyRequestProcessor {",
        "/**\n * 长轮询信息查询处理器：返回指定 topic@group@queue 上\n * 当前挂起的 POP 长轮询请求数量。\n */\npublic class PollingInfoProcessor implements NettyRequestProcessor {",
    ),
    (
        "    public PollingInfoProcessor(final BrokerController brokerController) {",
        "    /** @param brokerController Broker 控制器 */\n    public PollingInfoProcessor(final BrokerController brokerController) {",
    ),
    (
        "    private RemotingCommand processRequest(final Channel channel, RemotingCommand request)",
        "    /** 校验权限后查询 pollingMap 中挂起请求数并写入响应。 */\n    private RemotingCommand processRequest(final Channel channel, RemotingCommand request)",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/processor/PopInflightMessageCounter.java"] = [
    (
        "public class PopInflightMessageCounter {",
        "/**\n * POP 在途消息计数器：按 topic@group → queueId 维护未 ACK 消息数，\n * 供流控与监控使用；Broker 重启前的 popTime 不计入。\n */\npublic class PopInflightMessageCounter {",
    ),
    (
        "    public PopInflightMessageCounter(BrokerController brokerController) {",
        "    /** @param brokerController Broker 控制器，用于获取 shouldStartTime */\n    public PopInflightMessageCounter(BrokerController brokerController) {",
    ),
    (
        "    public void incrementInFlightMessageNum(String topic, String group, int queueId, int num) {",
        "    /** POP 成功后增加在途消息计数。 */\n    public void incrementInFlightMessageNum(String topic, String group, int queueId, int num) {",
    ),
    (
        "    public void decrementInFlightMessageNum(String topic, String group, long popTime, int qId, int delta) {",
        "    /** ACK 或超时时减少在途计数；popTime 早于 shouldStartTime 则忽略。 */\n    public void decrementInFlightMessageNum(String topic, String group, long popTime, int qId, int delta) {",
    ),
    (
        "    public void decrementInFlightMessageNum(PopCheckPoint checkPoint) {",
        "    /** 按 PopCheckPoint 递减在途计数（通常为 1）。 */\n    public void decrementInFlightMessageNum(PopCheckPoint checkPoint) {",
    ),
    (
        "    public void clearInFlightMessageNumByGroupName(String group) {",
        "    /** 按消费组名清除所有 topic 的在途计数（组删除时调用）。 */\n    public void clearInFlightMessageNumByGroupName(String group) {",
    ),
    (
        "    public void clearInFlightMessageNumByTopicName(String topic) {",
        "    /** 按 topic 名清除所有消费组的在途计数（topic 删除时调用）。 */\n    public void clearInFlightMessageNumByTopicName(String topic) {",
    ),
    (
        "    public void clearInFlightMessageNum(String topic, String group, int queueId) {",
        "    /** 清除指定 topic@group@queueId 的在途计数。 */\n    public void clearInFlightMessageNum(String topic, String group, int queueId) {",
    ),
    (
        "    public long getGroupPopInFlightMessageNum(String topic, String group, int queueId) {",
        "    /** 查询指定队列当前在途 POP 消息数。 */\n    public long getGroupPopInFlightMessageNum(String topic, String group, int queueId) {",
    ),
]
