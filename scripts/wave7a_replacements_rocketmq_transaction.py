"""RocketMQ 5.5.0 wave7a broker transaction/acl [0:15] replacements."""

R: dict[str, list[tuple[str, str]]] = {}

R["broker/src/main/java/org/apache/rocketmq/broker/transaction/TransactionMetricsFlushService.java"] = [
    (
        "public class TransactionMetricsFlushService extends ServiceThread {",
        "/**\n * 事务指标持久化后台线程：按 {@link BrokerController} 配置的间隔\n * 调用 {@link TransactionalMessageService#getTransactionMetrics()} 落盘统计。\n */\npublic class TransactionMetricsFlushService extends ServiceThread {",
    ),
    (
        "    public TransactionMetricsFlushService(BrokerController brokerController) {",
        "    /** @param brokerController 所属 Broker 控制器 */\n    public TransactionMetricsFlushService(BrokerController brokerController) {",
    ),
    (
        "    @Override\n    public String getServiceName() {",
        "    /** 返回线程服务名 {@code TransactionFlushService}。 */\n    @Override\n    public String getServiceName() {",
    ),
    (
        "    @Override\n    public void run() {",
        "    /** 循环等待 flush 间隔到期后持久化事务指标。 */\n    @Override\n    public void run() {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/transaction/TransactionalMessageCheckService.java"] = [
    (
        "public class TransactionalMessageCheckService extends ServiceThread {",
        "/**\n * 事务半消息回查调度线程：按 {@code transactionCheckInterval} 唤醒，\n * 在 {@link #onWaitEnd()} 中触发 {@link TransactionalMessageService#check}。\n */\npublic class TransactionalMessageCheckService extends ServiceThread {",
    ),
    (
        "    public TransactionalMessageCheckService(BrokerController brokerController) {",
        "    /** @param brokerController 所属 Broker 控制器 */\n    public TransactionalMessageCheckService(BrokerController brokerController) {",
    ),
    (
        "    @Override\n    public String getServiceName() {",
        "    /** 容器模式下返回 broker 标识前缀 + 类名。 */\n    @Override\n    public String getServiceName() {",
    ),
    (
        "    @Override\n    public void run() {",
        "    /** 按配置间隔 {@link #waitForRunning}，等待结束时执行回查。 */\n    @Override\n    public void run() {",
    ),
    (
        "    @Override\n    protected void onWaitEnd() {",
        "    /** 读取超时与最大回查次数，调用事务服务扫描半消息。 */\n    @Override\n    protected void onWaitEnd() {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/transaction/TransactionalMessageService.java"] = [
    (
        "public interface TransactionalMessageService {",
        "/**\n * Broker 端事务消息服务抽象：半消息写入、提交/回滚、回查与指标管理。\n * 队列模式见 {@link org.apache.rocketmq.broker.transaction.queue.TransactionalMessageServiceImpl}，\n * RocksDB 模式见 {@link org.apache.rocketmq.broker.transaction.rocksdb.TransactionalMessageRocksDBService}。\n */\npublic interface TransactionalMessageService {",
    ),
    (
        "    /**\n     * Process prepare message, in common, we should put this message to storage service.\n     *\n     * @param messageInner Prepare(Half) message.\n     * @return Prepare message storage result.\n     */",
        "    /**\n     * 同步写入半消息（Prepare）到存储。\n     *\n     * @param messageInner 半消息体\n     * @return 写入结果\n     */",
    ),
    (
        "    /**\n     * Process prepare message in async manner, we should put this message to storage service\n     *\n     * @param messageInner Prepare(Half) message.\n     * @return CompletableFuture of put result, will be completed at put success(flush and replica done)\n     */",
        "    /**\n     * 异步写入半消息；Future 在刷盘与副本同步完成后完成。\n     *\n     * @param messageInner 半消息体\n     * @return 异步写入结果\n     */",
    ),
    (
        "    /**\n     * Delete prepare message when this message has been committed or rolled back.\n     *\n     * @param messageExt\n     */",
        "    /**\n     * 提交或回滚后删除半消息。\n     *\n     * @param messageExt 待删除的半消息\n     * @return 是否删除成功\n     */",
    ),
    (
        "    /**\n     * Invoked to process commit prepare message.\n     *\n     * @param requestHeader Commit message request header.\n     * @return Operate result contains prepare message and relative error code.\n     */",
        "    /**\n     * 处理事务提交：将半消息转为正式消息。\n     *\n     * @param requestHeader 结束事务请求头\n     * @return 操作结果（含半消息与错误码）\n     */",
    ),
    (
        "    /**\n     * Invoked to roll back prepare message.\n     *\n     * @param requestHeader Prepare message request header.\n     * @return Operate result contains prepare message and relative error code.\n     */",
        "    /**\n     * 处理事务回滚：丢弃半消息。\n     *\n     * @param requestHeader 结束事务请求头\n     * @return 操作结果（含半消息与错误码）\n     */",
    ),
    (
        "    /**\n     * Traverse uncommitted/unroll back half message and send check back request to producer to obtain transaction\n     * status.\n     *\n     * @param transactionTimeout The minimum time of the transactional message to be checked firstly, one message only\n     * exceed this time interval that can be checked.\n     * @param transactionCheckMax The maximum number of times the message was checked, if exceed this value, this\n     * message will be discarded.\n     * @param listener When the message is considered to be checked or discarded, the relative method of this class will\n     * be invoked.\n     */",
        "    /**\n     * 扫描未决半消息并向 Producer 发送回查请求。\n     *\n     * @param transactionTimeout 首次回查最小等待时间（毫秒）\n     * @param transactionCheckMax 最大回查次数，超限则丢弃\n     * @param listener 回查或丢弃时的回调\n     */",
    ),
    (
        "    /**\n     * Open transaction service.\n     *\n     * @return If open success, return true.\n     */",
        "    /**\n     * 启动事务服务。\n     *\n     * @return 启动成功返回 true\n     */",
    ),
    (
        "    /**\n     * Close transaction service.\n     */",
        "    /** 关闭事务服务。 */",
    ),
]

R[
    "broker/src/main/java/org/apache/rocketmq/broker/transaction/queue/DefaultTransactionalMessageCheckListener.java"
] = [
    (
        "public class DefaultTransactionalMessageCheckListener extends AbstractTransactionalMessageCheckListener {",
        "/**\n * 默认事务回查监听器：回查次数超限时将半消息迁至\n * {@code TRANS_CHECK_MAXTIME_TOPIC} 并更新事务指标。\n */\npublic class DefaultTransactionalMessageCheckListener extends AbstractTransactionalMessageCheckListener {",
    ),
    (
        "    public DefaultTransactionalMessageCheckListener() {",
        "    /** 使用默认 Broker 控制器绑定。 */\n    public DefaultTransactionalMessageCheckListener() {",
    ),
    (
        "    @Override\n    public void resolveDiscardMsg(MessageExt msgExt) {",
        "    /** 将超限半消息写入系统 topic 并递减对应真实 topic 的半消息计数。 */\n    @Override\n    public void resolveDiscardMsg(MessageExt msgExt) {",
    ),
    (
        "                // discarded, then the num of half-messages minus 1",
        "                // 丢弃成功后，真实 topic 半消息计数减 1",
    ),
    (
        "    private MessageExtBrokerInner toMessageExtBrokerInner(MessageExt msgExt) {",
        "    /** 将半消息转换为写入 {@code TRANS_CHECK_MAXTIME_TOPIC} 的内部消息。 */\n    private MessageExtBrokerInner toMessageExtBrokerInner(MessageExt msgExt) {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/transaction/queue/GetResult.java"] = [
    (
        "public class GetResult {",
        "/** 事务队列拉取结果封装：关联单条 {@link MessageExt} 与 {@link PullResult}。 */\npublic class GetResult {",
    ),
    (
        "    public MessageExt getMsg() {",
        "    /** 返回拉取到的单条消息。 */\n    public MessageExt getMsg() {",
    ),
    (
        "    public void setMsg(MessageExt msg) {",
        "    /** 设置拉取到的单条消息。 */\n    public void setMsg(MessageExt msg) {",
    ),
    (
        "    public PullResult getPullResult() {",
        "    /** 返回底层 Pull 结果（含 offset 与状态）。 */\n    public PullResult getPullResult() {",
    ),
    (
        "    public void setPullResult(PullResult pullResult) {",
        "    /** 设置底层 Pull 结果。 */\n    public void setPullResult(PullResult pullResult) {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/transaction/queue/MessageQueueOpContext.java"] = [
    (
        "public class MessageQueueOpContext {",
        "/**\n * 事务 Op 消息批量上下文：维护待发送 Op 队列、累计条数与最近写入时间。\n * 供 {@link TransactionalOpBatchService} 批量刷写 Op 消息。\n */\npublic class MessageQueueOpContext {",
    ),
    (
        "    public MessageQueueOpContext(long timestamp, int queueLength) {",
        "    /** @param timestamp 初始最后写入时间戳\n     *  @param queueLength Op 上下文队列容量 */\n    public MessageQueueOpContext(long timestamp, int queueLength) {",
    ),
    (
        "    public LinkedBlockingQueue<String> getContextQueue() {",
        "    /** 返回待批量发送的 Op 上下文队列。 */\n    public LinkedBlockingQueue<String> getContextQueue() {",
    ),
    (
        "    public AtomicInteger getTotalSize() {",
        "    /** 返回累计 Op 条数计数器。 */\n    public AtomicInteger getTotalSize() {",
    ),
    (
        "    public long getLastWriteTimestamp() {",
        "    /** 返回最近一次写入 Op 的时间戳。 */\n    public long getLastWriteTimestamp() {",
    ),
    (
        "    public void setLastWriteTimestamp(long lastWriteTimestamp) {",
        "    /** 更新最近一次写入 Op 的时间戳。 */\n    public void setLastWriteTimestamp(long lastWriteTimestamp) {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/transaction/queue/TransactionalMessageBridge.java"] = [
    (
        "public class TransactionalMessageBridge {",
        "/**\n * 事务消息与 CommitLog 之间的桥接层：半消息/Op 消息的读写、\n * offset 管理与回查所需的消息重建。\n */\npublic class TransactionalMessageBridge {",
    ),
    (
        "    public TransactionalMessageBridge(BrokerController brokerController, MessageStore store) {",
        "    /** @param brokerController Broker 控制器\n     *  @param store 底层消息存储 */\n    public TransactionalMessageBridge(BrokerController brokerController, MessageStore store) {",
    ),
    (
        "    public long fetchConsumeOffset(MessageQueue mq) {",
        "    /** 查询事务系统消费组在指定队列上的消费位点；无记录时取最小 offset。 */\n    public long fetchConsumeOffset(MessageQueue mq) {",
    ),
    (
        "    public Set<MessageQueue> fetchMessageQueues(String topic) {",
        "    /** 按 topic 配置构建可读队列集合。 */\n    public Set<MessageQueue> fetchMessageQueues(String topic) {",
    ),
    (
        "    public void updateConsumeOffset(MessageQueue mq, long offset) {",
        "    /** 提交事务系统消费组在指定队列上的消费位点。 */\n    public void updateConsumeOffset(MessageQueue mq, long offset) {",
    ),
    (
        "    public PullResult getHalfMessage(int queueId, long offset, int nums) {",
        "    /** 从半消息 topic 拉取消息。 */\n    public PullResult getHalfMessage(int queueId, long offset, int nums) {",
    ),
    (
        "    public PullResult getOpMessage(int queueId, long offset, int nums) {",
        "    /** 从 Op 消息 topic 拉取消息。 */\n    public PullResult getOpMessage(int queueId, long offset, int nums) {",
    ),
    (
        "    public PutMessageResult putHalfMessage(MessageExtBrokerInner messageInner) {",
        "    /** 将业务消息改写为半消息并写入 CommitLog。 */\n    public PutMessageResult putHalfMessage(MessageExtBrokerInner messageInner) {",
    ),
    (
        "    public CompletableFuture<PutMessageResult> asyncPutHalfMessage(MessageExtBrokerInner messageInner) {",
        "    /** 异步写入半消息。 */\n    public CompletableFuture<PutMessageResult> asyncPutHalfMessage(MessageExtBrokerInner messageInner) {",
    ),
    (
        "    public PutMessageResult putMessageReturnResult(MessageExtBrokerInner messageInner) {",
        "    /** 写入消息并更新 Broker 写入统计。 */\n    public PutMessageResult putMessageReturnResult(MessageExtBrokerInner messageInner) {",
    ),
    (
        "    public boolean putMessage(MessageExtBrokerInner messageInner) {",
        "    /** 写入消息，成功返回 true。 */\n    public boolean putMessage(MessageExtBrokerInner messageInner) {",
    ),
    (
        "    public MessageExtBrokerInner renewImmunityHalfMessageInner(MessageExt msgExt) {",
        "    /** 重建半消息并保留免疫期 queueOffset 属性。 */\n    public MessageExtBrokerInner renewImmunityHalfMessageInner(MessageExt msgExt) {",
    ),
    (
        "    public MessageExtBrokerInner renewHalfMessageInner(MessageExt msgExt) {",
        "    /** 从 {@link MessageExt} 重建 {@link MessageExtBrokerInner}（不修改 topic）。 */\n    public MessageExtBrokerInner renewHalfMessageInner(MessageExt msgExt) {",
    ),
    (
        "    public boolean writeOp(Integer queueId,Message message) {",
        "    /** 向对应 queueId 的 Op topic 写入操作消息。 */\n    public boolean writeOp(Integer queueId,Message message) {",
    ),
    (
        "    public MessageExt lookMessageByOffset(final long commitLogOffset) {",
        "    /** 按 CommitLog 物理 offset 查找消息。 */\n    public MessageExt lookMessageByOffset(final long commitLogOffset) {",
    ),
    (
        "    public BrokerController getBrokerController() {",
        "    /** 返回关联的 Broker 控制器。 */\n    public BrokerController getBrokerController() {",
    ),
    (
        "    public boolean escapeMessage(MessageExtBrokerInner messageInner) {",
        "    /** 通过 EscapeBridge 将消息写入远端 Broker（主从切换场景）。 */\n    public boolean escapeMessage(MessageExtBrokerInner messageInner) {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/transaction/queue/TransactionalMessageUtil.java"] = [
    (
        "public class TransactionalMessageUtil {",
        "/** 事务消息 topic/消费组命名与半消息重建、免疫期计算的静态工具。 */\npublic class TransactionalMessageUtil {",
    ),
    (
        "    public static final String REMOVE_TAG = \"d\";",
        "    /** Op 消息删除标记 tag。 */\n    public static final String REMOVE_TAG = \"d\";",
    ),
    (
        "    public static final String TRANSACTION_ID = \"__transactionId__\";",
        "    /** 半消息中记录客户端事务 ID 的用户属性键。 */\n    public static final String TRANSACTION_ID = \"__transactionId__\";",
    ),
    (
        "    public static String buildOpTopic() {",
        "    /** 返回队列模式 Op 消息系统 topic。 */\n    public static String buildOpTopic() {",
    ),
    (
        "    public static String buildOpTopicForRocksDB() {",
        "    /** 返回 RocksDB 模式 Op 消息系统 topic。 */\n    public static String buildOpTopicForRocksDB() {",
    ),
    (
        "    public static String buildHalfTopic() {",
        "    /** 返回队列模式半消息系统 topic。 */\n    public static String buildHalfTopic() {",
    ),
    (
        "    public static String buildHalfTopicForRocksDB() {",
        "    /** 返回 RocksDB 模式半消息系统 topic。 */\n    public static String buildHalfTopicForRocksDB() {",
    ),
    (
        "    public static String buildConsumerGroup() {",
        "    /** 返回 Broker 内部事务扫描用的系统消费组。 */\n    public static String buildConsumerGroup() {",
    ),
    (
        "    public static MessageExtBrokerInner buildTransactionalMessageFromHalfMessage(MessageExt msgExt) {",
        "    /** 从半消息还原真实 topic/queue 并打上 TRANSACTION_PREPARED 标志。 */\n    public static MessageExtBrokerInner buildTransactionalMessageFromHalfMessage(MessageExt msgExt) {",
    ),
    (
        "        //If a custom first check time is set, the minimum check time;",
        "        // 若配置了自定义首次回查免疫期，不得低于 transactionTimeout",
    ),
    (
        "        //The default check protection period is transactionTimeout",
        "        // 默认免疫期等于 transactionTimeout",
    ),
    (
        "    public static long getImmunityTime(String checkImmunityTimeStr, long transactionTimeout) {",
        "    /** 解析免疫期秒数字符串，返回不低于 transactionTimeout 的毫秒值。 */\n    public static long getImmunityTime(String checkImmunityTimeStr, long transactionTimeout) {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/transaction/queue/TransactionalOpBatchService.java"] = [
    (
        "public class TransactionalOpBatchService extends ServiceThread {",
        "/**\n * 事务 Op 消息批量发送线程：按 {@code transactionOpBatchInterval}\n * 唤醒 {@link TransactionalMessageServiceImpl#batchSendOpMessage()}。\n */\npublic class TransactionalOpBatchService extends ServiceThread {",
    ),
    (
        "    public TransactionalOpBatchService(BrokerController brokerController,\n                                       TransactionalMessageServiceImpl transactionalMessageService) {",
        "    /** @param brokerController Broker 控制器\n     *  @param transactionalMessageService 队列模式事务服务实现 */\n    public TransactionalOpBatchService(BrokerController brokerController,\n                                       TransactionalMessageServiceImpl transactionalMessageService) {",
    ),
    (
        "    @Override\n    public String getServiceName() {",
        "    /** 返回本服务线程类名。 */\n    @Override\n    public String getServiceName() {",
    ),
    (
        "    @Override\n    public void run() {",
        "    /** 按 batch 间隔休眠，到期或提前唤醒时批量发送 Op。 */\n    @Override\n    public void run() {",
    ),
    (
        "    @Override\n    protected void onWaitEnd() {",
        "    /** 调用事务服务批量刷写 Op 消息并更新下次唤醒时间。 */\n    @Override\n    protected void onWaitEnd() {",
    ),
]

R[
    "broker/src/main/java/org/apache/rocketmq/broker/transaction/rocksdb/TransactionalMessageRocksDBService.java"
] = [
    (
        "public class TransactionalMessageRocksDBService {",
        "/**\n * 基于 RocksDB 的事务半消息回查服务：扫描 {@link TransMessageRocksDBStore}\n * 中的半消息记录，向 Producer 发送 {@link CheckTransactionStateRequestHeader}。\n */\npublic class TransactionalMessageRocksDBService {",
    ),
    (
        "    public TransactionalMessageRocksDBService(final MessageStore messageStore, final BrokerController brokerController) {",
        "    /** @param messageStore 消息存储（含 RocksDB 事务列族）\n     *  @param brokerController Broker 控制器 */\n    public TransactionalMessageRocksDBService(final MessageStore messageStore, final BrokerController brokerController) {",
    ),
    (
        "    public void start() {",
        "    /** 初始化线程池并启动 {@link TransStatusCheckService} 扫描线程。 */\n    public void start() {",
    ),
    (
        "    public void shutdown() {",
        "    /** 停止扫描线程并关闭回查任务线程池。 */\n    public void shutdown() {",
    ),
    (
        "    private void checkTransStatus() {",
        "    /** 分页扫描 RocksDB 事务列族并逐批回查。 */\n    private void checkTransStatus() {",
    ),
    (
        "    private void checkTransRecordsStatus(List<TransRocksDBRecord> trs) {",
        "    /** 对一批记录判断免疫期、回查次数并更新或删除。 */\n    private void checkTransRecordsStatus(List<TransRocksDBRecord> trs) {",
    ),
    (
        "    private boolean isImmunityTimeExpired(MessageExt msgExt) {",
        "    /** 判断半消息是否已过免疫期（可发起回查）。 */\n    private boolean isImmunityTimeExpired(MessageExt msgExt) {",
    ),
    (
        "    private void resolveHalfMsg(final MessageExt msgExt) {",
        "    /** 在线程池中异步发送事务状态回查请求。 */\n    private void resolveHalfMsg(final MessageExt msgExt) {",
    ),
    (
        "    private void sendCheckMessage(MessageExt msgExt) {",
        "    /** 构造回查头并通过 Producer 通道发送 CheckTransactionState 请求。 */\n    private void sendCheckMessage(MessageExt msgExt) {",
    ),
    (
        "    private class TransStatusCheckService extends ServiceThread {",
        "    /** 周期性调用 {@link #checkTransStatus()} 的后台扫描线程。 */\n    private class TransStatusCheckService extends ServiceThread {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/util/HookUtils.java"] = [
    (
        "public class HookUtils {",
        "/**\n * Broker 入队前校验与消息变换工具：topic/存储状态检查、\n * 定时/延迟/LMQ 配额处理及消息回退发送。\n */\npublic class HookUtils {",
    ),
    (
        "    /**\n     * On Linux: The maximum length for a file name is 255 bytes.\n     * The maximum combined length of both the file name and path name is 4096 bytes.\n     * This length matches the PATH_MAX that is supported by the operating system.\n     * The Unicode representation of a character can occupy several bytes,\n     * so the maximum number of characters that comprises a path and file name can vary.\n     * The actual limitation is the number of bytes in the path and file components,\n     * which might correspond to an equal number of characters.\n     */",
        "    /** Linux 下单个文件名最大 255 字节；topic 名长度上限与此对齐。 */",
    ),
    (
        "    public static PutMessageResult checkBeforePutMessage(BrokerController brokerController, final MessageExt msg) {",
        "    /** 入队前校验：存储可用性、Broker 角色、topic/body 合法性及页缓存压力。 */\n    public static PutMessageResult checkBeforePutMessage(BrokerController brokerController, final MessageExt msg) {",
    ),
    (
        "    public static PutMessageResult checkInnerBatch(BrokerController brokerController, final MessageExt msg) {",
        "    /** 校验内部批量消息标志与 ConsumeQueue 类型是否一致。 */\n    public static PutMessageResult checkInnerBatch(BrokerController brokerController, final MessageExt msg) {",
    ),
    (
        "    public static PutMessageResult handleScheduleMessage(BrokerController brokerController,\n        final MessageExtBrokerInner msg) {",
        "    /** 非事务消息处理定时轮与 delayLevel 延迟投递变换。 */\n    public static PutMessageResult handleScheduleMessage(BrokerController brokerController,\n        final MessageExtBrokerInner msg) {",
    ),
    (
        "                        //wheel timer is not enabled, reject the message",
        "                        // 未启用定时轮时拒绝定时消息",
    ),
    (
        "            // Delay Delivery",
        "            // 延迟级别投递：改写 topic 为系统 schedule topic",
    ),
    (
        "    public static PutMessageResult handleLmqQuota(BrokerController brokerController, final MessageExtBrokerInner msg) {",
        "    /** 校验 LMQ 多路分发目标数量是否超过配额。 */\n    public static PutMessageResult handleLmqQuota(BrokerController brokerController, final MessageExtBrokerInner msg) {",
    ),
    (
        "        //double check",
        "        // 二次确认：排除 delayLevel 与 timer topic 冲突",
    ),
    (
        "        //do transform",
        "        // 计算 deliverMs 并改写为 timer topic 消息",
    ),
    (
        "    public static void transformDelayLevelMessage(BrokerController brokerController, MessageExtBrokerInner msg) {",
        "    /** 将 delayLevel 消息路由到 {@link TopicValidator#RMQ_SYS_SCHEDULE_TOPIC} 对应队列。 */\n    public static void transformDelayLevelMessage(BrokerController brokerController, MessageExtBrokerInner msg) {",
    ),
    (
        "        // Backup real topic, queueId",
        "        // 备份真实 topic 与 queueId 到用户属性",
    ),
    (
        "    public static boolean sendMessageBack(BrokerController brokerController, List<MessageExt> msgList,\n        String brokerName, String brokerAddr) {",
        "    /** 将消息列表逐条回发到指定 Broker 地址。 */\n    public static boolean sendMessageBack(BrokerController brokerController, List<MessageExt> msgList,\n        String brokerName, String brokerAddr) {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/util/PositiveAtomicCounter.java"] = [
    (
        "public class PositiveAtomicCounter {",
        "/** 始终返回非负值的 {@link AtomicInteger} 包装，高位溢出后按 {@link #MASK} 截断。 */\npublic class PositiveAtomicCounter {",
    ),
    (
        "    public PositiveAtomicCounter() {",
        "    /** 以 0 初始化内部计数器。 */\n    public PositiveAtomicCounter() {",
    ),
    (
        "    public final int incrementAndGet() {",
        "    /** 自增并返回截断后的非负值。 */\n    public final int incrementAndGet() {",
    ),
    (
        "    public int intValue() {",
        "    /** 返回当前原始 int 值（可能为负）。 */\n    public int intValue() {",
    ),
]

R["client/src/main/java/org/apache/rocketmq/acl/common/AclClientRPCHook.java"] = [
    (
        "public class AclClientRPCHook implements RPCHook {",
        "/**\n * 客户端 ACL RPC 钩子：在请求发出前注入 AccessKey/SecurityToken\n * 并计算 {@link SessionCredentials#SIGNATURE} 签名。\n */\npublic class AclClientRPCHook implements RPCHook {",
    ),
    (
        "    public AclClientRPCHook(SessionCredentials sessionCredentials) {",
        "    /** @param sessionCredentials ACL 会话凭证（accessKey/secretKey 等） */\n    public AclClientRPCHook(SessionCredentials sessionCredentials) {",
    ),
    (
        "    @Override\n    public void doBeforeRequest(String remoteAddr, RemotingCommand request) {",
        "    /** 请求前写入凭证字段并附加 HMAC 签名。 */\n    @Override\n    public void doBeforeRequest(String remoteAddr, RemotingCommand request) {",
    ),
    (
        "        // Add AccessKey and SecurityToken into signature calculating.",
        "        // 将 AccessKey 与可选 SecurityToken 纳入签名字段",
    ),
    (
        "        // The SecurityToken value is unnecessary,user can choose this one.",
        "        // SecurityToken 可选，临时凭证场景才需要",
    ),
    (
        "    @Override\n    public void doAfterResponse(String remoteAddr, RemotingCommand request, RemotingCommand response) {",
        "    /** 响应后钩子（当前无额外处理）。 */\n    @Override\n    public void doAfterResponse(String remoteAddr, RemotingCommand request, RemotingCommand response) {",
    ),
    (
        "    protected SortedMap<String, String> parseRequestContent(RemotingCommand request) {",
        "    /** 将扩展字段排序为 TreeMap，供签名计算使用。 */\n    protected SortedMap<String, String> parseRequestContent(RemotingCommand request) {",
    ),
    (
        "        // Sort property",
        "        // 扩展字段按字典序排序以保证签名一致",
    ),
    (
        "    public SessionCredentials getSessionCredentials() {",
        "    /** 返回绑定的会话凭证。 */\n    public SessionCredentials getSessionCredentials() {",
    ),
]

R["client/src/main/java/org/apache/rocketmq/acl/common/AclConstants.java"] = [
    (
        "public class AclConstants {",
        "/** ACL 配置键与权限类型常量（PUB/SUB/DENY 等）。 */\npublic class AclConstants {",
    ),
    (
        "    public static final String CONFIG_ACCESS_KEY = \"accessKey\";",
        "    /** 配置文件中的 accessKey 键名。 */\n    public static final String CONFIG_ACCESS_KEY = \"accessKey\";",
    ),
    (
        "    public static final String CONFIG_SECRET_KEY = \"secretKey\";",
        "    /** 配置文件中的 secretKey 键名。 */\n    public static final String CONFIG_SECRET_KEY = \"secretKey\";",
    ),
    (
        "    public static final String PUB = \"PUB\";",
        "    /** 仅发布权限。 */\n    public static final String PUB = \"PUB\";",
    ),
    (
        "    public static final String SUB = \"SUB\";",
        "    /** 仅订阅权限。 */\n    public static final String SUB = \"SUB\";",
    ),
    (
        "    public static final String DENY = \"DENY\";",
        "    /** 拒绝访问。 */\n    public static final String DENY = \"DENY\";",
    ),
    (
        "    public static final String PUB_SUB = \"PUB|SUB\";",
        "    /** 发布与订阅权限（PUB 在前）。 */\n    public static final String PUB_SUB = \"PUB|SUB\";",
    ),
    (
        "    public static final String SUB_PUB = \"SUB|PUB\";",
        "    /** 发布与订阅权限（SUB 在前）。 */\n    public static final String SUB_PUB = \"SUB|PUB\";",
    ),
]

R["client/src/main/java/org/apache/rocketmq/acl/common/AclException.java"] = [
    (
        "public class AclException extends RuntimeException {",
        "/** ACL 校验失败时抛出的运行时异常，携带 status 与 code。 */\npublic class AclException extends RuntimeException {",
    ),
    (
        "    public AclException(String status, int code) {",
        "    /** @param status 错误状态标识\n     *  @param code 错误码 */\n    public AclException(String status, int code) {",
    ),
    (
        "    public AclException(String status, int code, String message) {",
        "    /** @param status 错误状态标识\n     *  @param code 错误码\n     *  @param message 错误描述 */\n    public AclException(String status, int code, String message) {",
    ),
    (
        "    public AclException(String message) {",
        "    /** @param message 错误描述 */\n    public AclException(String message) {",
    ),
    (
        "    public AclException(String message, Throwable throwable) {",
        "    /** @param message 错误描述\n     *  @param throwable 根因 */\n    public AclException(String message, Throwable throwable) {",
    ),
    (
        "    public AclException(String status, int code, String message, Throwable throwable) {",
        "    /** @param status 错误状态标识\n     *  @param code 错误码\n     *  @param message 错误描述\n     *  @param throwable 根因 */\n    public AclException(String status, int code, String message, Throwable throwable) {",
    ),
    (
        "    public String getStatus() {",
        "    /** 返回 ACL 错误状态标识。 */\n    public String getStatus() {",
    ),
    (
        "    public void setStatus(String status) {",
        "    /** 设置 ACL 错误状态标识。 */\n    public void setStatus(String status) {",
    ),
    (
        "    public int getCode() {",
        "    /** 返回 ACL 错误码。 */\n    public int getCode() {",
    ),
    (
        "    public void setCode(int code) {",
        "    /** 设置 ACL 错误码。 */\n    public void setCode(int code) {",
    ),
]
