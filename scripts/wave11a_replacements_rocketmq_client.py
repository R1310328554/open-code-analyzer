"""RocketMQ 5.5.0 wave11a client producer/request/transaction/trace [0:15] replacements."""

R: dict[str, list[tuple[str, str]]] = {}

R["client/src/main/java/org/apache/rocketmq/client/producer/RequestResponseFuture.java"] = [
    (
        "public class RequestResponseFuture {",
        "/**\n * Request-Reply 模式的单次请求上下文：持有 correlationId、超时与 CountDownLatch，\n * 用于同步等待或异步回调响应消息。\n */\npublic class RequestResponseFuture {",
    ),
    (
        "    private final String correlationId;",
        "    /** 请求关联 ID，与 Reply 消息中的 correlationId 对应。 */\n    private final String correlationId;",
    ),
    (
        "    private final RequestCallback requestCallback;",
        "    /** 异步回调；为 null 时仅支持同步 wait。 */\n    private final RequestCallback requestCallback;",
    ),
    (
        "    private final long beginTimestamp = System.currentTimeMillis();",
        "    /** 请求创建时间戳，用于判断是否超时。 */\n    private final long beginTimestamp = System.currentTimeMillis();",
    ),
    (
        "    private final Message requestMsg = null;",
        "    /** 预留的请求消息引用（当前实现恒为 null）。 */\n    private final Message requestMsg = null;",
    ),
    (
        "    private long timeoutMillis;",
        "    /** 超时阈值（毫秒）。 */\n    private long timeoutMillis;",
    ),
    (
        "    private CountDownLatch countDownLatch = new CountDownLatch(1);",
        "    /** 响应到达时 countDown，唤醒等待线程。 */\n    private CountDownLatch countDownLatch = new CountDownLatch(1);",
    ),
    (
        "    private volatile Message responseMsg = null;",
        "    /** 服务端返回的响应消息。 */\n    private volatile Message responseMsg = null;",
    ),
    (
        "    private volatile boolean sendRequestOk = true;",
        "    /** 请求是否成功发出（网络层）。 */\n    private volatile boolean sendRequestOk = true;",
    ),
    (
        "    private volatile Throwable cause = null;",
        "    /** 失败时的异常原因。 */\n    private volatile Throwable cause = null;",
    ),
    (
        "    public RequestResponseFuture(String correlationId, long timeoutMillis, RequestCallback requestCallback) {",
        "    /** 构造 Request-Reply Future。 */\n    public RequestResponseFuture(String correlationId, long timeoutMillis, RequestCallback requestCallback) {",
    ),
    (
        "    public void executeRequestCallback() {",
        "    /** 根据发送结果调用成功或异常回调。 */\n    public void executeRequestCallback() {",
    ),
    (
        "    public boolean isTimeout() {",
        "    /** 判断是否已超过 timeoutMillis。 */\n    public boolean isTimeout() {",
    ),
    (
        "    public Message waitResponseMessage(final long timeout) throws InterruptedException {",
        "    /** 阻塞等待响应，超时后返回当前 responseMsg（可能为 null）。 */\n    public Message waitResponseMessage(final long timeout) throws InterruptedException {",
    ),
    (
        "    public void putResponseMessage(final Message responseMsg) {",
        "    /** 写入响应并唤醒等待线程。 */\n    public void putResponseMessage(final Message responseMsg) {",
    ),
    (
        "    public String getCorrelationId() {",
        "    /** 返回 correlationId。 */\n    public String getCorrelationId() {",
    ),
    (
        "    public long getTimeoutMillis() {",
        "    /** 返回超时毫秒数。 */\n    public long getTimeoutMillis() {",
    ),
    (
        "    public void setTimeoutMillis(long timeoutMillis) {",
        "    /** 设置超时毫秒数。 */\n    public void setTimeoutMillis(long timeoutMillis) {",
    ),
    (
        "    public RequestCallback getRequestCallback() {",
        "    /** 返回异步回调。 */\n    public RequestCallback getRequestCallback() {",
    ),
    (
        "    public long getBeginTimestamp() {",
        "    /** 返回请求开始时间戳。 */\n    public long getBeginTimestamp() {",
    ),
    (
        "    public CountDownLatch getCountDownLatch() {",
        "    /** 返回同步等待用的 CountDownLatch。 */\n    public CountDownLatch getCountDownLatch() {",
    ),
    (
        "    public void setCountDownLatch(CountDownLatch countDownLatch) {",
        "    /** 替换 CountDownLatch（测试或特殊场景）。 */\n    public void setCountDownLatch(CountDownLatch countDownLatch) {",
    ),
    (
        "    public Message getResponseMsg() {",
        "    /** 返回响应消息。 */\n    public Message getResponseMsg() {",
    ),
    (
        "    public void setResponseMsg(Message responseMsg) {",
        "    /** 设置响应消息。 */\n    public void setResponseMsg(Message responseMsg) {",
    ),
    (
        "    public boolean isSendRequestOk() {",
        "    /** 请求是否成功发出。 */\n    public boolean isSendRequestOk() {",
    ),
    (
        "    public void setSendRequestOk(boolean sendRequestOk) {",
        "    /** 标记请求发送是否成功。 */\n    public void setSendRequestOk(boolean sendRequestOk) {",
    ),
    (
        "    public Message getRequestMsg() {",
        "    /** 返回请求消息引用。 */\n    public Message getRequestMsg() {",
    ),
    (
        "    public Throwable getCause() {",
        "    /** 返回失败异常。 */\n    public Throwable getCause() {",
    ),
    (
        "    public void setCause(Throwable cause) {",
        "    /** 设置失败异常。 */\n    public void setCause(Throwable cause) {",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/producer/SendCallback.java"] = [
    (
        "public interface SendCallback {",
        "/**\n * 异步发送结果回调：发送成功或失败时由客户端线程池调用。\n */\npublic interface SendCallback {",
    ),
    (
        "    void onSuccess(final SendResult sendResult);",
        "    /** 发送成功，返回 {@link SendResult}。 */\n    void onSuccess(final SendResult sendResult);",
    ),
    (
        "    void onException(final Throwable e);",
        "    /** 发送失败，携带异常信息。 */\n    void onException(final Throwable e);",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/producer/SendResult.java"] = [
    (
        "public class SendResult {",
        "/**\n * 消息发送结果：包含发送状态、msgId、目标队列、偏移量及事务/追踪等扩展字段。\n */\npublic class SendResult {",
    ),
    (
        "    private SendStatus sendStatus;",
        "    /** 发送状态（OK、刷盘超时等）。 */\n    private SendStatus sendStatus;",
    ),
    (
        "    private String msgId;",
        "    /** Broker 生成的全局消息 ID。 */\n    private String msgId;",
    ),
    (
        "    private MessageQueue messageQueue;",
        "    /** 消息写入的目标队列。 */\n    private MessageQueue messageQueue;",
    ),
    (
        "    private long queueOffset;",
        "    /** 消息在队列中的逻辑偏移量。 */\n    private long queueOffset;",
    ),
    (
        "    private String transactionId;",
        "    /** 事务消息 ID（非事务消息可为 null）。 */\n    private String transactionId;",
    ),
    (
        "    private String offsetMsgId;",
        "    /** 基于 host+offset 的物理消息 ID。 */\n    private String offsetMsgId;",
    ),
    (
        "    private String regionId;",
        "    /** 消息所属区域 ID。 */\n    private String regionId;",
    ),
    (
        "    private boolean traceOn = true;",
        "    /** 是否开启消息轨迹追踪。 */\n    private boolean traceOn = true;",
    ),
    (
        "    private byte[] rawRespBody;",
        "    /** Broker 原始响应体（扩展场景）。 */\n    private byte[] rawRespBody;",
    ),
    (
        "    private String recallHandle;",
        "    /** 消息撤回句柄。 */\n    private String recallHandle;",
    ),
    (
        "    public SendResult() {",
        "    /** 无参构造，供 JSON 反序列化等使用。 */\n    public SendResult() {",
    ),
    (
        "    public static String encoderSendResultToJson(final Object obj) {",
        "    /** 将 SendResult 序列化为 JSON 字符串。 */\n    public static String encoderSendResultToJson(final Object obj) {",
    ),
    (
        "    public static SendResult decoderSendResultFromJson(String json) {",
        "    /** 从 JSON 字符串反序列化为 SendResult。 */\n    public static SendResult decoderSendResultFromJson(String json) {",
    ),
    (
        "    public boolean isTraceOn() {",
        "    /** 是否开启轨迹追踪。 */\n    public boolean isTraceOn() {",
    ),
    (
        "    public void setTraceOn(final boolean traceOn) {",
        "    /** 设置是否开启轨迹追踪。 */\n    public void setTraceOn(final boolean traceOn) {",
    ),
    (
        "    public String getRegionId() {",
        "    /** 返回区域 ID。 */\n    public String getRegionId() {",
    ),
    (
        "    public void setRegionId(final String regionId) {",
        "    /** 设置区域 ID。 */\n    public void setRegionId(final String regionId) {",
    ),
    (
        "    public String getMsgId() {",
        "    /** 返回全局 msgId。 */\n    public String getMsgId() {",
    ),
    (
        "    public void setMsgId(String msgId) {",
        "    /** 设置全局 msgId。 */\n    public void setMsgId(String msgId) {",
    ),
    (
        "    public SendStatus getSendStatus() {",
        "    /** 返回发送状态。 */\n    public SendStatus getSendStatus() {",
    ),
    (
        "    public void setSendStatus(SendStatus sendStatus) {",
        "    /** 设置发送状态。 */\n    public void setSendStatus(SendStatus sendStatus) {",
    ),
    (
        "    public MessageQueue getMessageQueue() {",
        "    /** 返回目标 MessageQueue。 */\n    public MessageQueue getMessageQueue() {",
    ),
    (
        "    public void setMessageQueue(MessageQueue messageQueue) {",
        "    /** 设置目标 MessageQueue。 */\n    public void setMessageQueue(MessageQueue messageQueue) {",
    ),
    (
        "    public long getQueueOffset() {",
        "    /** 返回队列偏移量。 */\n    public long getQueueOffset() {",
    ),
    (
        "    public void setQueueOffset(long queueOffset) {",
        "    /** 设置队列偏移量。 */\n    public void setQueueOffset(long queueOffset) {",
    ),
    (
        "    public String getTransactionId() {",
        "    /** 返回事务 ID。 */\n    public String getTransactionId() {",
    ),
    (
        "    public void setTransactionId(String transactionId) {",
        "    /** 设置事务 ID。 */\n    public void setTransactionId(String transactionId) {",
    ),
    (
        "    public String getOffsetMsgId() {",
        "    /** 返回物理 offsetMsgId。 */\n    public String getOffsetMsgId() {",
    ),
    (
        "    public void setOffsetMsgId(String offsetMsgId) {",
        "    /** 设置物理 offsetMsgId。 */\n    public void setOffsetMsgId(String offsetMsgId) {",
    ),
    (
        "    public String getRecallHandle() {",
        "    /** 返回撤回句柄。 */\n    public String getRecallHandle() {",
    ),
    (
        "    public void setRecallHandle(String recallHandle) {",
        "    /** 设置撤回句柄。 */\n    public void setRecallHandle(String recallHandle) {",
    ),
    (
        "    public void setRawRespBody(byte[] body) {",
        "    /** 设置 Broker 原始响应体。 */\n    public void setRawRespBody(byte[] body) {",
    ),
    (
        "    public byte[] getRawRespBody() {",
        "    /** 返回 Broker 原始响应体。 */\n    public byte[] getRawRespBody() {",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/producer/SendStatus.java"] = [
    (
        "public enum SendStatus {",
        "/**\n * 同步发送返回的状态码：表示消息是否成功写入及副本/刷盘是否满足 SLA。\n */\npublic enum SendStatus {",
    ),
    (
        "    SEND_OK,",
        "    /** 发送成功，刷盘与副本均正常。 */\n    SEND_OK,",
    ),
    (
        "    FLUSH_DISK_TIMEOUT,",
        "    /** 消息已写入但同步刷盘超时。 */\n    FLUSH_DISK_TIMEOUT,",
    ),
    (
        "    FLUSH_SLAVE_TIMEOUT,",
        "    /** 消息已写入主节点但同步到从节点超时。 */\n    FLUSH_SLAVE_TIMEOUT,",
    ),
    (
        "    SLAVE_NOT_AVAILABLE,",
        "    /** 从节点不可用，无法完成同步复制。 */\n    SLAVE_NOT_AVAILABLE,",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/producer/TransactionCheckListener.java"] = [
    (
        "/**\n * @deprecated This interface will be removed in the version 5.0.0, interface {@link TransactionListener} is recommended.\n */",
        "/**\n * @deprecated 5.0.0 起移除，请改用 {@link TransactionListener}。\n */",
    ),
    (
        "public interface TransactionCheckListener {",
        "/**\n * 旧版事务回查监听器：Broker 回查半消息时查询本地事务状态。\n * @deprecated 请使用 {@link TransactionListener#checkLocalTransaction}\n */\npublic interface TransactionCheckListener {",
    ),
    (
        "    LocalTransactionState checkLocalTransactionState(final MessageExt msg);",
        "    /** 根据半消息回查本地事务状态。 */\n    LocalTransactionState checkLocalTransactionState(final MessageExt msg);",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/producer/TransactionListener.java"] = [
    (
        "public interface TransactionListener {",
        "/**\n * 事务消息监听器：半消息发送成功后执行本地事务，\n * 并在 Broker 回查时返回本地事务状态。\n */\npublic interface TransactionListener {",
    ),
    (
        "    /**\n     * When send transactional prepare(half) message succeed, this method will be invoked to execute local transaction.\n     *\n     * @param msg Half(prepare) message\n     * @param arg Custom business parameter\n     * @return Transaction state\n     */",
        "    /**\n     * 半消息发送成功后执行本地事务。\n     *\n     * @param msg 半消息（Prepare）\n     * @param arg 业务自定义参数\n     * @return 本地事务状态\n     */",
    ),
    (
        "    /**\n     * When no response to prepare(half) message. broker will send check message to check the transaction status, and this\n     * method will be invoked to get local transaction status.\n     *\n     * @param msg Check message\n     * @return Transaction state\n     */",
        "    /**\n     * Broker 未收到提交/回滚时发起回查，此方法返回本地事务状态。\n     *\n     * @param msg 回查消息\n     * @return 本地事务状态\n     */",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/producer/TransactionMQProducer.java"] = [
    (
        "public class TransactionMQProducer extends DefaultMQProducer {",
        "/**\n * 事务消息 Producer：发送半消息后由 {@link TransactionListener} 执行本地事务，\n * 并管理回查线程池与事务环境生命周期。\n */\npublic class TransactionMQProducer extends DefaultMQProducer {",
    ),
    (
        "    private TransactionCheckListener transactionCheckListener;",
        "    /** 旧版回查监听器（已废弃）。 */\n    private TransactionCheckListener transactionCheckListener;",
    ),
    (
        "    private int checkThreadPoolMinSize = 1;",
        "    /** 回查线程池最小线程数（已废弃，建议自定义 ExecutorService）。 */\n    private int checkThreadPoolMinSize = 1;",
    ),
    (
        "    private int checkThreadPoolMaxSize = 1;",
        "    /** 回查线程池最大线程数（已废弃）。 */\n    private int checkThreadPoolMaxSize = 1;",
    ),
    (
        "    private int checkRequestHoldMax = 2000;",
        "    /** 回查请求最大排队数（已废弃）。 */\n    private int checkRequestHoldMax = 2000;",
    ),
    (
        "    private ExecutorService executorService;",
        "    /** 执行本地事务与回查的自定义线程池。 */\n    private ExecutorService executorService;",
    ),
    (
        "    private TransactionListener transactionListener;",
        "    /** 新版事务监听器，发送前必须设置。 */\n    private TransactionListener transactionListener;",
    ),
    (
        "    public TransactionMQProducer() {",
        "    /** 默认构造。 */\n    public TransactionMQProducer() {",
    ),
    (
        "    @Override\n    public void start() throws MQClientException {",
        "    /** 初始化事务环境后启动 Producer。 */\n    @Override\n    public void start() throws MQClientException {",
    ),
    (
        "    @Override\n    public void shutdown() {",
        "    /** 关闭 Producer 并销毁事务环境。 */\n    @Override\n    public void shutdown() {",
    ),
    (
        "    @Override\n    public TransactionSendResult sendMessageInTransaction(final Message msg,\n        final Object arg) throws MQClientException {",
        "    /** 发送事务半消息并触发本地事务；Topic 会自动加上 namespace 前缀。 */\n    @Override\n    public TransactionSendResult sendMessageInTransaction(final Message msg,\n        final Object arg) throws MQClientException {",
    ),
    (
        "    public TransactionCheckListener getTransactionCheckListener() {",
        "    /** 返回旧版回查监听器。 */\n    public TransactionCheckListener getTransactionCheckListener() {",
    ),
    (
        "    /**\n     * This method will be removed in the version 5.0.0 and set a custom thread pool is recommended.\n     */\n    @Deprecated\n    public void setTransactionCheckListener(TransactionCheckListener transactionCheckListener) {",
        "    /** @deprecated 5.0.0 起移除，请改用 {@link TransactionListener}。 */\n    @Deprecated\n    public void setTransactionCheckListener(TransactionCheckListener transactionCheckListener) {",
    ),
    (
        "    /**\n     * This method will be removed in the version 5.0.0 and set a custom thread pool is recommended.\n     */\n    @Deprecated\n    public void setCheckThreadPoolMinSize(int checkThreadPoolMinSize) {",
        "    /** @deprecated 5.0.0 起移除，请改用 {@link #setExecutorService} 自定义线程池。 */\n    @Deprecated\n    public void setCheckThreadPoolMinSize(int checkThreadPoolMinSize) {",
    ),
    (
        "    /**\n     * This method will be removed in the version 5.0.0 and set a custom thread pool is recommended.\n     */\n    @Deprecated\n    public void setCheckThreadPoolMaxSize(int checkThreadPoolMaxSize) {",
        "    /** @deprecated 5.0.0 起移除，请改用 {@link #setExecutorService} 自定义线程池。 */\n    @Deprecated\n    public void setCheckThreadPoolMaxSize(int checkThreadPoolMaxSize) {",
    ),
    (
        "    /**\n     * This method will be removed in the version 5.0.0 and set a custom thread pool is recommended.\n     */\n    @Deprecated\n    public void setCheckRequestHoldMax(int checkRequestHoldMax) {",
        "    /** @deprecated 5.0.0 起移除，请改用 {@link #setExecutorService} 自定义线程池。 */\n    @Deprecated\n    public void setCheckRequestHoldMax(int checkRequestHoldMax) {",
    ),
    (
        "    public int getCheckThreadPoolMinSize() {",
        "    /** 返回回查线程池最小线程数。 */\n    public int getCheckThreadPoolMinSize() {",
    ),
    (
        "    public int getCheckThreadPoolMaxSize() {",
        "    /** 返回回查线程池最大线程数。 */\n    public int getCheckThreadPoolMaxSize() {",
    ),
    (
        "    public int getCheckRequestHoldMax() {",
        "    /** 返回回查请求最大排队数。 */\n    public int getCheckRequestHoldMax() {",
    ),
    (
        "    public ExecutorService getExecutorService() {",
        "    /** 返回事务执行线程池。 */\n    public ExecutorService getExecutorService() {",
    ),
    (
        "    public void setExecutorService(ExecutorService executorService) {",
        "    /** 设置执行本地事务与回查的线程池。 */\n    public void setExecutorService(ExecutorService executorService) {",
    ),
    (
        "    public TransactionListener getTransactionListener() {",
        "    /** 返回事务监听器。 */\n    public TransactionListener getTransactionListener() {",
    ),
    (
        "    public void setTransactionListener(TransactionListener transactionListener) {",
        "    /** 设置事务监听器（发送事务消息前必填）。 */\n    public void setTransactionListener(TransactionListener transactionListener) {",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/producer/TransactionSendResult.java"] = [
    (
        "public class TransactionSendResult extends SendResult {",
        "/** 事务消息发送结果：在 {@link SendResult} 基础上附加本地事务状态。 */\npublic class TransactionSendResult extends SendResult {",
    ),
    (
        "    private LocalTransactionState localTransactionState;",
        "    /** 本地事务执行结果（COMMIT/ROLLBACK/UNKNOWN）。 */\n    private LocalTransactionState localTransactionState;",
    ),
    (
        "    public TransactionSendResult() {",
        "    /** 无参构造。 */\n    public TransactionSendResult() {",
    ),
    (
        "    public LocalTransactionState getLocalTransactionState() {",
        "    /** 返回本地事务状态。 */\n    public LocalTransactionState getLocalTransactionState() {",
    ),
    (
        "    public void setLocalTransactionState(LocalTransactionState localTransactionState) {",
        "    /** 设置本地事务状态。 */\n    public void setLocalTransactionState(LocalTransactionState localTransactionState) {",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/producer/selector/SelectMessageQueueByHash.java"] = [
    (
        "public class SelectMessageQueueByHash implements MessageQueueSelector {",
        "/**\n * 按 arg 的 hashCode 取模选择队列，保证相同路由键的消息进入同一队列。\n */\npublic class SelectMessageQueueByHash implements MessageQueueSelector {",
    ),
    (
        "    public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {",
        "    /** 使用 arg.hashCode() % mqs.size() 选取队列。 */\n    public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/producer/selector/SelectMessageQueueByMachineRoom.java"] = [
    (
        "public class SelectMessageQueueByMachineRoom implements MessageQueueSelector {",
        "/**\n * 按机房（IDC）选择 MessageQueue 的策略占位实现；\n * 当前 {@link #select} 恒返回 null，需业务侧自行扩展。\n */\npublic class SelectMessageQueueByMachineRoom implements MessageQueueSelector {",
    ),
    (
        "    private Set<String> consumeridcs;",
        "    /** 消费者所在机房 ID 集合。 */\n    private Set<String> consumeridcs;",
    ),
    (
        "    public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {",
        "    /** 按机房筛选队列（当前未实现，返回 null）。 */\n    public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {",
    ),
    (
        "    public Set<String> getConsumeridcs() {",
        "    /** 返回消费者机房集合。 */\n    public Set<String> getConsumeridcs() {",
    ),
    (
        "    public void setConsumeridcs(Set<String> consumeridcs) {",
        "    /** 设置消费者机房集合。 */\n    public void setConsumeridcs(Set<String> consumeridcs) {",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/producer/selector/SelectMessageQueueByRandom.java"] = [
    (
        "public class SelectMessageQueueByRandom implements MessageQueueSelector {",
        "/**\n * 随机选择 MessageQueue，用于无顺序要求的负载均衡发送。\n */\npublic class SelectMessageQueueByRandom implements MessageQueueSelector {",
    ),
    (
        "    private Random random = new Random(System.currentTimeMillis());",
        "    /** 随机数生成器，种子为当前时间。 */\n    private Random random = new Random(System.currentTimeMillis());",
    ),
    (
        "    public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {",
        "    /** 在可用队列中均匀随机选取一条。 */\n    public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/rpchook/NamespaceRpcHook.java"] = [
    (
        "public class NamespaceRpcHook implements RPCHook {",
        "/**\n * RPC 钩子：在出站 Remoting 请求头中注入 namespace 信息，\n * 供多租户/逻辑隔离场景下的 Broker 路由识别。\n */\npublic class NamespaceRpcHook implements RPCHook {",
    ),
    (
        "    private final ClientConfig clientConfig;",
        "    /** 客户端配置，读取 namespaceV2。 */\n    private final ClientConfig clientConfig;",
    ),
    (
        "    public NamespaceRpcHook(ClientConfig clientConfig) {",
        "    /** 绑定客户端配置。 */\n    public NamespaceRpcHook(ClientConfig clientConfig) {",
    ),
    (
        "    @Override\n    public void doBeforeRequest(String remoteAddr, RemotingCommand request) {",
        "    /** 若配置了 namespaceV2，在请求扩展字段中标记并携带 namespace。 */\n    @Override\n    public void doBeforeRequest(String remoteAddr, RemotingCommand request) {",
    ),
    (
        "    @Override\n    public void doAfterResponse(String remoteAddr, RemotingCommand request,\n        RemotingCommand response) {",
        "    /** 响应后无额外处理（占位实现）。 */\n    @Override\n    public void doAfterResponse(String remoteAddr, RemotingCommand request,\n        RemotingCommand response) {",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/stat/ConsumerStatsManager.java"] = [
    (
        "public class ConsumerStatsManager {",
        "/**\n * 消费者运行时统计：按 topic@group 维度累计拉取/消费 TPS、RT 及失败次数，\n * 供管理接口 {@link #consumeStatus} 聚合快照。\n */\npublic class ConsumerStatsManager {",
    ),
    (
        "    private static final String TOPIC_AND_GROUP_CONSUME_OK_TPS = \"CONSUME_OK_TPS\";",
        "    /** 消费成功 TPS 指标名。 */\n    private static final String TOPIC_AND_GROUP_CONSUME_OK_TPS = \"CONSUME_OK_TPS\";",
    ),
    (
        "    private static final String TOPIC_AND_GROUP_CONSUME_FAILED_TPS = \"CONSUME_FAILED_TPS\";",
        "    /** 消费失败 TPS 指标名。 */\n    private static final String TOPIC_AND_GROUP_CONSUME_FAILED_TPS = \"CONSUME_FAILED_TPS\";",
    ),
    (
        "    private static final String TOPIC_AND_GROUP_CONSUME_RT = \"CONSUME_RT\";",
        "    /** 消费耗时 RT 指标名。 */\n    private static final String TOPIC_AND_GROUP_CONSUME_RT = \"CONSUME_RT\";",
    ),
    (
        "    private static final String TOPIC_AND_GROUP_PULL_TPS = \"PULL_TPS\";",
        "    /** 拉取 TPS 指标名。 */\n    private static final String TOPIC_AND_GROUP_PULL_TPS = \"PULL_TPS\";",
    ),
    (
        "    private static final String TOPIC_AND_GROUP_PULL_RT = \"PULL_RT\";",
        "    /** 拉取 RT 指标名。 */\n    private static final String TOPIC_AND_GROUP_PULL_RT = \"PULL_RT\";",
    ),
    (
        "    public ConsumerStatsManager(final ScheduledExecutorService scheduledExecutorService) {",
        "    /** 注册各 StatsItemSet，由定时线程池驱动分钟/小时级聚合。 */\n    public ConsumerStatsManager(final ScheduledExecutorService scheduledExecutorService) {",
    ),
    (
        "    public void start() {",
        "    /** 启动钩子（当前无操作）。 */\n    public void start() {",
    ),
    (
        "    public void shutdown() {",
        "    /** 关闭钩子（当前无操作）。 */\n    public void shutdown() {",
    ),
    (
        "    public void incPullRT(final String group, final String topic, final long rt) {",
        "    /** 累加一次拉取耗时。 */\n    public void incPullRT(final String group, final String topic, final long rt) {",
    ),
    (
        "    public void incPullTPS(final String group, final String topic, final long msgs) {",
        "    /** 累加拉取消息条数。 */\n    public void incPullTPS(final String group, final String topic, final long msgs) {",
    ),
    (
        "    public void incConsumeRT(final String group, final String topic, final long rt) {",
        "    /** 累加一次消费耗时。 */\n    public void incConsumeRT(final String group, final String topic, final long rt) {",
    ),
    (
        "    public void incConsumeOKTPS(final String group, final String topic, final long msgs) {",
        "    /** 累加消费成功条数。 */\n    public void incConsumeOKTPS(final String group, final String topic, final long msgs) {",
    ),
    (
        "    public void incConsumeFailedTPS(final String group, final String topic, final long msgs) {",
        "    /** 累加消费失败条数。 */\n    public void incConsumeFailedTPS(final String group, final String topic, final long msgs) {",
    ),
    (
        "    public ConsumeStatus consumeStatus(final String group, final String topic) {",
        "    /** 聚合 topic@group 的拉取/消费 TPS、RT 及失败消息总数。 */\n    public ConsumeStatus consumeStatus(final String group, final String topic) {",
    ),
    (
        "    private StatsSnapshot getPullRT(final String group, final String topic) {",
        "    /** 读取分钟级拉取 RT 快照。 */\n    private StatsSnapshot getPullRT(final String group, final String topic) {",
    ),
    (
        "    private StatsSnapshot getPullTPS(final String group, final String topic) {",
        "    /** 读取分钟级拉取 TPS 快照。 */\n    private StatsSnapshot getPullTPS(final String group, final String topic) {",
    ),
    (
        "    private StatsSnapshot getConsumeRT(final String group, final String topic) {",
        "    /** 读取消费 RT；分钟级无数据时回退到小时级。 */\n    private StatsSnapshot getConsumeRT(final String group, final String topic) {",
    ),
    (
        "    private StatsSnapshot getConsumeOKTPS(final String group, final String topic) {",
        "    /** 读取分钟级消费成功 TPS 快照。 */\n    private StatsSnapshot getConsumeOKTPS(final String group, final String topic) {",
    ),
    (
        "    private StatsSnapshot getConsumeFailedTPS(final String group, final String topic) {",
        "    /** 读取分钟级消费失败 TPS 快照。 */\n    private StatsSnapshot getConsumeFailedTPS(final String group, final String topic) {",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/trace/TraceBean.java"] = [
    (
        "public class TraceBean {",
        "/**\n * 消息轨迹单条记录的数据载体：封装 topic、msgId、主机、事务状态等\n * 写入轨迹 Topic 所需的字段。\n */\npublic class TraceBean {",
    ),
    (
        "    private static final String LOCAL_ADDRESS;",
        "    /** 本机 IP 字符串（IPv4 或 IPv6）。 */\n    private static final String LOCAL_ADDRESS;",
    ),
    (
        "    private String topic = \"\";",
        "    /** 消息 Topic。 */\n    private String topic = \"\";",
    ),
    (
        "    private String msgId = \"\";",
        "    /** 全局消息 ID。 */\n    private String msgId = \"\";",
    ),
    (
        "    private String offsetMsgId = \"\";",
        "    /** 物理 offset 消息 ID。 */\n    private String offsetMsgId = \"\";",
    ),
    (
        "    private String tags = \"\";",
        "    /** 消息 Tag。 */\n    private String tags = \"\";",
    ),
    (
        "    private String keys = \"\";",
        "    /** 消息 Keys。 */\n    private String keys = \"\";",
    ),
    (
        "    private String storeHost = LOCAL_ADDRESS;",
        "    /** 消息存储 Broker 主机地址。 */\n    private String storeHost = LOCAL_ADDRESS;",
    ),
    (
        "    private String clientHost = LOCAL_ADDRESS;",
        "    /** 客户端主机地址。 */\n    private String clientHost = LOCAL_ADDRESS;",
    ),
    (
        "    private long storeTime;",
        "    /** 消息存储时间戳。 */\n    private long storeTime;",
    ),
    (
        "    private int retryTimes;",
        "    /** 消费重试次数。 */\n    private int retryTimes;",
    ),
    (
        "    private int bodyLength;",
        "    /** 消息体字节长度。 */\n    private int bodyLength;",
    ),
    (
        "    private MessageType msgType;",
        "    /** 消息类型（普通/事务/延迟等）。 */\n    private MessageType msgType;",
    ),
    (
        "    private LocalTransactionState transactionState;",
        "    /** 本地事务状态。 */\n    private LocalTransactionState transactionState;",
    ),
    (
        "    private String transactionId;",
        "    /** 事务消息 ID。 */\n    private String transactionId;",
    ),
    (
        "    private boolean fromTransactionCheck;",
        "    /** 是否来自 Broker 事务回查。 */\n    private boolean fromTransactionCheck;",
    ),
    (
        "    static {",
        "    /** 静态初始化本机 IP 地址字符串。 */\n    static {",
    ),
    (
        "    public MessageType getMsgType() {",
        "    /** 返回消息类型。 */\n    public MessageType getMsgType() {",
    ),
    (
        "    public void setMsgType(final MessageType msgType) {",
        "    /** 设置消息类型。 */\n    public void setMsgType(final MessageType msgType) {",
    ),
    (
        "    public String getOffsetMsgId() {",
        "    /** 返回 offsetMsgId。 */\n    public String getOffsetMsgId() {",
    ),
    (
        "    public void setOffsetMsgId(final String offsetMsgId) {",
        "    /** 设置 offsetMsgId。 */\n    public void setOffsetMsgId(final String offsetMsgId) {",
    ),
    (
        "    public String getTopic() {",
        "    /** 返回 Topic。 */\n    public String getTopic() {",
    ),
    (
        "    public void setTopic(String topic) {",
        "    /** 设置 Topic。 */\n    public void setTopic(String topic) {",
    ),
    (
        "    public String getMsgId() {",
        "    /** 返回 msgId。 */\n    public String getMsgId() {",
    ),
    (
        "    public void setMsgId(String msgId) {",
        "    /** 设置 msgId。 */\n    public void setMsgId(String msgId) {",
    ),
    (
        "    public String getTags() {",
        "    /** 返回 Tag。 */\n    public String getTags() {",
    ),
    (
        "    public void setTags(String tags) {",
        "    /** 设置 Tag。 */\n    public void setTags(String tags) {",
    ),
    (
        "    public String getKeys() {",
        "    /** 返回 Keys。 */\n    public String getKeys() {",
    ),
    (
        "    public void setKeys(String keys) {",
        "    /** 设置 Keys。 */\n    public void setKeys(String keys) {",
    ),
    (
        "    public String getStoreHost() {",
        "    /** 返回存储主机。 */\n    public String getStoreHost() {",
    ),
    (
        "    public void setStoreHost(String storeHost) {",
        "    /** 设置存储主机。 */\n    public void setStoreHost(String storeHost) {",
    ),
    (
        "    public String getClientHost() {",
        "    /** 返回客户端主机。 */\n    public String getClientHost() {",
    ),
    (
        "    public void setClientHost(String clientHost) {",
        "    /** 设置客户端主机。 */\n    public void setClientHost(String clientHost) {",
    ),
    (
        "    public long getStoreTime() {",
        "    /** 返回存储时间。 */\n    public long getStoreTime() {",
    ),
    (
        "    public void setStoreTime(long storeTime) {",
        "    /** 设置存储时间。 */\n    public void setStoreTime(long storeTime) {",
    ),
    (
        "    public int getRetryTimes() {",
        "    /** 返回重试次数。 */\n    public int getRetryTimes() {",
    ),
    (
        "    public void setRetryTimes(int retryTimes) {",
        "    /** 设置重试次数。 */\n    public void setRetryTimes(int retryTimes) {",
    ),
    (
        "    public int getBodyLength() {",
        "    /** 返回消息体长度。 */\n    public int getBodyLength() {",
    ),
    (
        "    public void setBodyLength(int bodyLength) {",
        "    /** 设置消息体长度。 */\n    public void setBodyLength(int bodyLength) {",
    ),
    (
        "    public LocalTransactionState getTransactionState() {",
        "    /** 返回事务状态。 */\n    public LocalTransactionState getTransactionState() {",
    ),
    (
        "    public void setTransactionState(LocalTransactionState transactionState) {",
        "    /** 设置事务状态。 */\n    public void setTransactionState(LocalTransactionState transactionState) {",
    ),
    (
        "    public String getTransactionId() {",
        "    /** 返回事务 ID。 */\n    public String getTransactionId() {",
    ),
    (
        "    public void setTransactionId(String transactionId) {",
        "    /** 设置事务 ID。 */\n    public void setTransactionId(String transactionId) {",
    ),
    (
        "    public boolean isFromTransactionCheck() {",
        "    /** 是否来自事务回查。 */\n    public boolean isFromTransactionCheck() {",
    ),
    (
        "    public void setFromTransactionCheck(boolean fromTransactionCheck) {",
        "    /** 标记是否来自事务回查。 */\n    public void setFromTransactionCheck(boolean fromTransactionCheck) {",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/trace/TraceConstants.java"] = [
    (
        "public class TraceConstants {",
        "/**\n * 消息轨迹模块常量：内部 Producer 组名、轨迹 Topic 前缀及\n * OpenTracing 风格的属性键名。\n */\npublic class TraceConstants {",
    ),
    (
        "    public static final String GROUP_NAME_PREFIX = \"_INNER_TRACE_PRODUCER\";",
        "    /** 轨迹内部 Producer 组名前缀。 */\n    public static final String GROUP_NAME_PREFIX = \"_INNER_TRACE_PRODUCER\";",
    ),
    (
        "    public static final char CONTENT_SPLITOR = (char) 1;",
        "    /** 轨迹消息内容字段分隔符。 */\n    public static final char CONTENT_SPLITOR = (char) 1;",
    ),
    (
        "    public static final char FIELD_SPLITOR = (char) 2;",
        "    /** 轨迹消息属性字段分隔符。 */\n    public static final char FIELD_SPLITOR = (char) 2;",
    ),
    (
        "    public static final String TRACE_INSTANCE_NAME = \"PID_CLIENT_INNER_TRACE_PRODUCER\";",
        "    /** 轨迹内部 Producer 实例名。 */\n    public static final String TRACE_INSTANCE_NAME = \"PID_CLIENT_INNER_TRACE_PRODUCER\";",
    ),
    (
        "    public static final String TRACE_TOPIC_PREFIX = TopicValidator.SYSTEM_TOPIC_PREFIX + \"TRACE_DATA_\";",
        "    /** 轨迹数据 Topic 前缀（系统 Topic）。 */\n    public static final String TRACE_TOPIC_PREFIX = TopicValidator.SYSTEM_TOPIC_PREFIX + \"TRACE_DATA_\";",
    ),
    (
        "    public static final String TO_PREFIX = \"To_\";",
        "    /** Span 目标端前缀。 */\n    public static final String TO_PREFIX = \"To_\";",
    ),
    (
        "    public static final String FROM_PREFIX = \"From_\";",
        "    /** Span 来源端前缀。 */\n    public static final String FROM_PREFIX = \"From_\";",
    ),
    (
        "    public static final String END_TRANSACTION = \"EndTransaction\";",
        "    /** 事务结束 Span 操作名。 */\n    public static final String END_TRANSACTION = \"EndTransaction\";",
    ),
    (
        "    public static final String ROCKETMQ_SERVICE = \"rocketmq\";",
        "    /** OpenTracing 服务名。 */\n    public static final String ROCKETMQ_SERVICE = \"rocketmq\";",
    ),
    (
        "    public static final String ROCKETMQ_SUCCESS = \"rocketmq.success\";",
        "    /** 轨迹属性：是否成功。 */\n    public static final String ROCKETMQ_SUCCESS = \"rocketmq.success\";",
    ),
    (
        "    public static final String ROCKETMQ_TAGS = \"rocketmq.tags\";",
        "    /** 轨迹属性：消息 Tag。 */\n    public static final String ROCKETMQ_TAGS = \"rocketmq.tags\";",
    ),
    (
        "    public static final String ROCKETMQ_KEYS = \"rocketmq.keys\";",
        "    /** 轨迹属性：消息 Keys。 */\n    public static final String ROCKETMQ_KEYS = \"rocketmq.keys\";",
    ),
    (
        "    public static final String ROCKETMQ_STORE_HOST = \"rocketmq.store_host\";",
        "    /** 轨迹属性：存储主机。 */\n    public static final String ROCKETMQ_STORE_HOST = \"rocketmq.store_host\";",
    ),
    (
        "    public static final String ROCKETMQ_BODY_LENGTH = \"rocketmq.body_length\";",
        "    /** 轨迹属性：消息体长度。 */\n    public static final String ROCKETMQ_BODY_LENGTH = \"rocketmq.body_length\";",
    ),
    (
        "    public static final String ROCKETMQ_MSG_ID = \"rocketmq.mgs_id\";",
        "    /** 轨迹属性：msgId（历史拼写 mgs_id）。 */\n    public static final String ROCKETMQ_MSG_ID = \"rocketmq.mgs_id\";",
    ),
    (
        "    public static final String ROCKETMQ_MSG_TYPE = \"rocketmq.mgs_type\";",
        "    /** 轨迹属性：消息类型。 */\n    public static final String ROCKETMQ_MSG_TYPE = \"rocketmq.mgs_type\";",
    ),
    (
        "    public static final String ROCKETMQ_REGION_ID = \"rocketmq.region_id\";",
        "    /** 轨迹属性：区域 ID。 */\n    public static final String ROCKETMQ_REGION_ID = \"rocketmq.region_id\";",
    ),
    (
        "    public static final String ROCKETMQ_TRANSACTION_ID = \"rocketmq.transaction_id\";",
        "    /** 轨迹属性：事务 ID。 */\n    public static final String ROCKETMQ_TRANSACTION_ID = \"rocketmq.transaction_id\";",
    ),
    (
        "    public static final String ROCKETMQ_TRANSACTION_STATE = \"rocketmq.transaction_state\";",
        "    /** 轨迹属性：事务状态。 */\n    public static final String ROCKETMQ_TRANSACTION_STATE = \"rocketmq.transaction_state\";",
    ),
    (
        "    public static final String ROCKETMQ_IS_FROM_TRANSACTION_CHECK = \"rocketmq.is_from_transaction_check\";",
        "    /** 轨迹属性：是否来自事务回查。 */\n    public static final String ROCKETMQ_IS_FROM_TRANSACTION_CHECK = \"rocketmq.is_from_transaction_check\";",
    ),
    (
        "    public static final String ROCKETMQ_RETRY_TIMERS = \"rocketmq.retry_times\";",
        "    /** 轨迹属性：重试次数。 */\n    public static final String ROCKETMQ_RETRY_TIMERS = \"rocketmq.retry_times\";",
    ),
]
