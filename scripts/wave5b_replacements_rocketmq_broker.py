"""RocketMQ 5.5.0 wave5b broker mqtrace/offset/pagecache/plugin/pop [15:30] replacements."""

R: dict[str, list[tuple[str, str]]] = {}

R["broker/src/main/java/org/apache/rocketmq/broker/mqtrace/ConsumeMessageContext.java"] = [
    (
        "public class ConsumeMessageContext {",
        "/**\n * 消费消息追踪上下文：承载一次 Pull/Pop 消费链路中的 group、topic、消息 ID、\n * 账号统计与商业化计量等元数据，供 {@link ConsumeMessageHook} 前后置回调使用。\n */\npublic class ConsumeMessageContext {",
    ),
    (
        "    private String consumerGroup;",
        "    /** 消费组名（不含 namespace）。 */\n    private String consumerGroup;",
    ),
    (
        "    private Map<String, Long> messageIds;",
        "    /** 消息 ID 与 store 时间戳映射。 */\n    private Map<String, Long> messageIds;",
    ),
    (
        "    private Object mqTraceContext;",
        "    /** 扩展追踪上下文，由 trace 组件注入。 */\n    private Object mqTraceContext;",
    ),
    (
        "    private BrokerStatsManager.StatsType rcvStat;",
        "    /** 账号维度接收统计类型。 */\n    private BrokerStatsManager.StatsType rcvStat;",
    ),
    (
        "    private int filterMessageCount;",
        "    /** SQL/Tag 过滤后实际投递的消息条数。 */\n    private int filterMessageCount;",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/mqtrace/ConsumeMessageHook.java"] = [
    (
        "public interface ConsumeMessageHook {",
        "/**\n * 消费消息钩子：在 Broker 向客户端返回消息前后插入自定义逻辑（追踪、审计、限流等）。\n */\npublic interface ConsumeMessageHook {",
    ),
    (
        "    String hookName();",
        "    /** 钩子唯一名称，用于注册与去重。 */\n    String hookName();",
    ),
    (
        "    void consumeMessageBefore(final ConsumeMessageContext context);",
        "    /** 消费响应发出前回调。 */\n    void consumeMessageBefore(final ConsumeMessageContext context);",
    ),
    (
        "    void consumeMessageAfter(final ConsumeMessageContext context);",
        "    /** 消费响应完成后回调（含成功/失败状态）。 */\n    void consumeMessageAfter(final ConsumeMessageContext context);",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/mqtrace/SendMessageContext.java"] = [
    (
        "public class SendMessageContext {",
        "/**\n * 发送消息追踪上下文：记录 Producer 发送请求的 topic、msgId、队列位点、\n * 响应码及账号/商业化统计字段，供 {@link SendMessageHook} 使用。\n */\npublic class SendMessageContext {",
    ),
    (
        "    /** namespace */",
        "    /** 租户 namespace。 */",
    ),
    (
        "    /** producer group without namespace. */",
        "    /** 生产者组名（不含 namespace）。 */",
    ),
    (
        "    /** topic without namespace. */",
        "    /** Topic 名（不含 namespace）。 */",
    ),
    (
        "    private Object mqTraceContext;",
        "    /** 扩展追踪上下文。 */\n    private Object mqTraceContext;",
    ),
    (
        "    /**\n     * Account Statistics\n     */",
        "    /** 账号维度发送统计字段。 */",
    ),
    (
        "    /**\n     * For Commercial\n     */",
        "    /** 商业化计量字段。 */",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/mqtrace/SendMessageHook.java"] = [
    (
        "public interface SendMessageHook {",
        "/**\n * 发送消息钩子：在 Broker 处理 Producer 发送请求前后插入自定义逻辑。\n */\npublic interface SendMessageHook {",
    ),
    (
        "    String hookName();",
        "    /** 钩子唯一名称。 */\n    String hookName();",
    ),
    (
        "    void sendMessageBefore(final SendMessageContext context);",
        "    /** 消息落盘/转发前回调。 */\n    void sendMessageBefore(final SendMessageContext context);",
    ),
    (
        "    void sendMessageAfter(final SendMessageContext context);",
        "    /** 发送处理完成后回调。 */\n    void sendMessageAfter(final SendMessageContext context);",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/offset/BroadcastOffsetManager.java"] = [
    (
        "/**\n * manage the offset of broadcast.\n * now, use this to support switch remoting client between proxy and broker\n */",
        "/**\n * 广播消费位点管理器：按 clientId 维护各队列拉取位点，并汇总提交 group@broadcast 位点。\n * 同时支持 Proxy 与 Broker 直连切换时的初始位点协商。\n */",
    ),
    (
        "    /**\n     * k: topic@groupId\n     * v: the pull offset of all client of all queue\n     */",
        "    /** key 为 topic@groupId，value 为该组下所有 client 各队列拉取位点。 */",
    ),
    (
        "    public void updateOffset(String topic, String group, int queueId, long offset, String clientId, boolean fromProxy) {",
        "    /** 更新指定 client 在某队列上的广播拉取位点；fromProxy 标记请求是否经 Proxy 转发。 */\n    public void updateOffset(String topic, String group, int queueId, long offset, String clientId, boolean fromProxy) {",
    ),
    (
        "    /**\n     * the time need init offset\n     * 1. client connect to proxy -> client connect to broker\n     * 2. client connect to broker -> client connect to proxy\n     * 3. client connect to proxy at the first time\n     *\n     * @return -1 means no init offset, use the queueOffset in pullRequestHeader\n     */",
        "    /**\n     * 查询是否需要初始化拉取位点，典型场景：\n     * 1. Proxy 切 Broker；2. Broker 切 Proxy；3. 首次经 Proxy 拉取。\n     *\n     * @return -1 表示无需初始化，沿用 Pull 请求头中的 queueOffset\n     */",
    ),
    (
        "    private long getOffset(BroadcastTimedOffsetStore offsetStore, String topic, String groupId, int queueId)",
        "    /** 依次从本地缓存、ConsumerOffsetManager、MessageStore 解析可用起始位点。 */\n    private long getOffset(BroadcastTimedOffsetStore offsetStore, String topic, String groupId, int queueId)",
    ),
    (
        "    /**\n     * 1. scan expire offset\n     * 2. calculate the min offset of all client of one topic@group,\n     * and then commit consumer offset by group@broadcast\n     */",
        "    /**\n     * 定时扫描：1) 清理过期 client 位点；2) 取各队列最小位点并以 group@broadcast 提交。\n     */",
    ),
    (
        "    /**\n     * @param group group of users\n     * @return the groupId used to commit offset\n     */",
        "    /**\n     * @param group 用户消费组\n     * @return 用于提交位点的 broadcast 专用 groupId（group@broadcast）\n     */",
    ),
    (
        "        /**\n         * the timeStamp of last update occurred\n         */",
        "        /** 该 client 位点最后一次更新时间戳。 */",
    ),
    (
        "        /**\n         * mark the offset of this client is updated by proxy or not\n         */",
        "        /** 标记该 client 位点是否由 Proxy 侧更新。 */",
    ),
    (
        "        /**\n         * the pulled offset of each queue\n         */",
        "        /** 各队列已拉取位点存储。 */",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/offset/BroadcastOffsetStore.java"] = [
    (
        "public class BroadcastOffsetStore {",
        "/**\n * 广播消费单 client 的队列位点表：queueId → AtomicLong offset。\n */\npublic class BroadcastOffsetStore {",
    ),
    (
        "    public void updateOffset(int queueId, long offset, boolean increaseOnly) {",
        "    /** 更新队列位点；increaseOnly 为 true 时仅允许单调递增。 */\n    public void updateOffset(int queueId, long offset, boolean increaseOnly) {",
    ),
    (
        "    public long readOffset(int queueId) {",
        "    /** 读取队列位点，不存在返回 -1。 */\n    public long readOffset(int queueId) {",
    ),
    (
        "    public Set<Integer> queueList() {",
        "    /** 返回已有位点记录的 queueId 集合。 */\n    public Set<Integer> queueList() {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/offset/LmqConsumerOffsetManager.java"] = [
    (
        "public class LmqConsumerOffsetManager extends ConsumerOffsetManager {",
        "/**\n * Lite/LMQ 消费位点管理器：LMQ group 使用 topic@group 单键存储位点，\n * 与普通多队列位点表分离持久化。\n */\npublic class LmqConsumerOffsetManager extends ConsumerOffsetManager {",
    ),
    (
        "    private ConcurrentHashMap<String, Long> lmqOffsetTable = new ConcurrentHashMap<>(512);",
        "    /** LMQ 位点表：key 为 topic@group，value 为队列 0 的 committed offset。 */\n    private ConcurrentHashMap<String, Long> lmqOffsetTable = new ConcurrentHashMap<>(512);",
    ),
    (
        "        // topic@group",
        "        // LMQ 位点 key：topic@group",
    ),
    (
        "    public void removeOffset(String group) {",
        "    /** 删除指定 LMQ group 的全部 topic@group 位点条目。 */\n    public void removeOffset(String group) {",
    ),
    (
        "    public void assignResetOffset(String topic, String group, int queueId, long offset) {",
        "    /** 为 LMQ topic/group 分配重置位点，同步更新 lmqOffsetTable 与 resetOffsetTable。 */\n    public void assignResetOffset(String topic, String group, int queueId, long offset) {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/offset/MemoryConsumerOrderInfoManager.java"] = [
    (
        "/**\n * Memory-based Consumer Order Information Manager for Lite Topics\n * Trade-off considerations::\n * 1. Lite Topics are primarily used for lightweight consumption where\n *    strict ordering requirements are relatively low\n * 2. Considering compatibility with traditional PushConsumer,\n *    a certain degree of ordering control failure is acceptable\n * 3. Avoiding I/O overhead from persistence operations\n * <p>\n * We may make structural adjustments and optimizations to reduce overhead and memory footprint.\n */",
        "/**\n * 基于内存的 Lite Topic 顺序消费信息管理器。\n * 设计取舍：Lite 场景对严格顺序要求较低；兼容 PushConsumer 时可容忍部分顺序控制失效；\n * 避免持久化 I/O 开销。后续可能进一步优化结构与内存占用。\n */",
    ),
    (
        "            // use max lock free time to prevent unexpected blocking",
        "            // 取最大 lock-free 时间戳，避免意外长时间阻塞",
    ),
    (
        "    public void suspendQueue(String topic, String group, int queueId, long popTime, long visibilityTimeout) {",
        "    /** Pop 挂起队列：校验 popTime 后递减 offset 消费计数并刷新不可见时间与 lock-free 戳。 */\n    public void suspendQueue(String topic, String group, int queueId, long popTime, long visibilityTimeout) {",
    ),
    (
        "        // MemoryConsumerOrderInfoManager persist, do nothing.",
        "        // 纯内存实现，persist 为空操作。",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/pagecache/ManyMessageTransfer.java"] = [
    (
        "public class ManyMessageTransfer extends AbstractReferenceCounted implements FileRegion {",
        "/**\n * 批量消息零拷贝传输：Netty FileRegion 封装响应头与 {@link GetMessageResult} 多段 ByteBuffer。\n */\npublic class ManyMessageTransfer extends AbstractReferenceCounted implements FileRegion {",
    ),
    (
        "    /**\n     * Bytes which were transferred already.\n     */",
        "    /** 已累计写入 channel 的字节数。 */",
    ),
    (
        "    public long transferTo(WritableByteChannel target, long position) throws IOException {",
        "    /** 优先写完响应头，再逐段写出消息体 buffer；返回累计 transferred。 */\n    public long transferTo(WritableByteChannel target, long position) throws IOException {",
    ),
    (
        "    protected void deallocate() {",
        "    /** 释放 GetMessageResult 引用计数。 */\n    protected void deallocate() {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/pagecache/OneMessageTransfer.java"] = [
    (
        "public class OneMessageTransfer extends AbstractReferenceCounted implements FileRegion {",
        "/**\n * 单条消息零拷贝传输：响应头 + {@link SelectMappedBufferResult} 映射缓冲区。\n */\npublic class OneMessageTransfer extends AbstractReferenceCounted implements FileRegion {",
    ),
    (
        "    /**\n     * Bytes which were transferred already.\n     */",
        "    /** 已累计写入 channel 的字节数。 */",
    ),
    (
        "    public long transferTo(WritableByteChannel target, long position) throws IOException {",
        "    /** 先写响应头，再写 mapped buffer 剩余内容。 */\n    public long transferTo(WritableByteChannel target, long position) throws IOException {",
    ),
    (
        "    protected void deallocate() {",
        "    /** 释放 SelectMappedBufferResult 引用。 */\n    protected void deallocate() {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/pagecache/QueryMessageTransfer.java"] = [
    (
        "public class QueryMessageTransfer extends AbstractReferenceCounted implements FileRegion {",
        "/**\n * 按 key/时间查询消息的零拷贝传输：响应头 + {@link QueryMessageResult} 多段 buffer。\n */\npublic class QueryMessageTransfer extends AbstractReferenceCounted implements FileRegion {",
    ),
    (
        "    /**\n     * Bytes which were transferred already.\n     */",
        "    /** 已累计写入 channel 的字节数。 */",
    ),
    (
        "    public long transferTo(WritableByteChannel target, long position) throws IOException {",
        "    /** 分段写出查询结果 buffer 列表。 */\n    public long transferTo(WritableByteChannel target, long position) throws IOException {",
    ),
    (
        "    protected void deallocate() {",
        "    /** 释放 QueryMessageResult 引用。 */\n    protected void deallocate() {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/plugin/BrokerAttachedPlugin.java"] = [
    (
        "public interface BrokerAttachedPlugin {",
        "/**\n * Broker 附属插件生命周期接口：加载、启停、元数据同步及运行时信息扩展。\n */\npublic interface BrokerAttachedPlugin {",
    ),
    (
        "    /**\n     * Get plugin name\n     *\n     * @return plugin name\n     */",
        "    /**\n     * 插件名称。\n     */",
    ),
    (
        "    /**\n     * Load broker attached plugin.\n     *\n     * @return load success or failed\n     */",
        "    /**\n     * 加载插件资源。\n     *\n     * @return 是否加载成功\n     */",
    ),
    (
        "    /**\n     * Start broker attached plugin.\n     */",
        "    /** 启动插件后台任务。 */",
    ),
    (
        "    /**\n     * Shutdown broker attached plugin.\n     */",
        "    /** 关闭插件并释放资源。 */",
    ),
    (
        "    /**\n     * Sync metadata from master.\n     */",
        "    /** 从 Master Broker 同步元数据。 */",
    ),
    (
        "    /**\n     * Sync metadata reverse from slave\n     *\n     * @param brokerAddr\n     */",
        "    /**\n     * 反向从 Slave 拉取元数据（主从角色切换场景）。\n     *\n     * @param brokerAddr 目标 Broker 地址\n     */",
    ),
    (
        "    /**\n     * Some plugin need build runningInfo when prepare runtime info.\n     *\n     * @param runtimeInfo\n     */",
        "    /**\n     * 向 Broker 运行时信息 Map 注入插件状态字段。\n     */",
    ),
    (
        "    /**\n     * Some plugin need do something when status changed. For example, brokerRole change to master or slave.\n     *\n     * @param shouldStart\n     */",
        "    /**\n     * Broker 角色/状态变更回调（如升主、降从）。\n     *\n     * @param shouldStart 是否应处于运行态\n     */",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/plugin/PullMessageResultHandler.java"] = [
    (
        "public interface PullMessageResultHandler {",
        "/**\n * Pull 消息结果处理器插件：在 Store 返回 GetMessageResult 后定制响应组装逻辑。\n */\npublic interface PullMessageResultHandler {",
    ),
    (
        "    /**\n     * Handle result of get message from store.\n     *\n     * @param getMessageResult store result\n     * @param request request\n     * @param requestHeader request header\n     * @param channel channel\n     * @param subscriptionData sub data\n     * @param subscriptionGroupConfig sub config\n     * @param brokerAllowSuspend brokerAllowSuspend\n     * @param messageFilter store message filter\n     * @param response response\n     * @return response or null\n     */",
        "    /**\n     * 处理 Store 拉取结果并构造/改写 Remoting 响应。\n     *\n     * @return 最终响应；返回 null 表示沿用默认处理\n     */",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/pop/PopConsumerCache.java"] = [
    (
        "public class PopConsumerCache extends ServiceThread {",
        "/**\n * Pop 消费内存缓冲：在 CK（Checkpoint）落盘前缓存 in-flight PopConsumerRecord，\n * 定时清理超时记录、触发 revive 并提交最小可推进位点。\n */\npublic class PopConsumerCache extends ServiceThread {",
    ),
    (
        "    private static final long OFFSET_NOT_EXIST = -1L;",
        "    /** 位点不存在时的哨兵值。 */\n    private static final long OFFSET_NOT_EXIST = -1L;",
    ),
    (
        "    public boolean isCacheFull() {",
        "    /** 估算缓存条数是否超过 popCkMaxBufferSize 上限。 */\n    public boolean isCacheFull() {",
    ),
    (
        "    public void writeRecords(List<PopConsumerRecord> consumerRecordList) {",
        "    /** 将 Pop 拉取记录写入按 group@topic@queueId 分片的内存表。 */\n    public void writeRecords(List<PopConsumerRecord> consumerRecordList) {",
    ),
    (
        "    /**\n     * Remove the record from the input list then return the content that has not been deleted\n     */",
        "    /**\n     * 批量 ACK：从缓存删除已确认记录，返回未能删除的剩余列表。\n     */",
    ),
    (
        "    public int cleanupRecords(Consumer<PopConsumerRecord> consumer) {",
        "    /** 扫描全部 shard：过期记录 revive 或落 CK，并提交 buffer 最小 offset。 */\n    public int cleanupRecords(Consumer<PopConsumerRecord> consumer) {",
    ),
    (
        "            // revive or write record to store",
        "            // 消费者离线超时：revive 或写入 CK store",
    ),
    (
        "            // write to store and handle it later",
        "            // 未到期记录写入 store 延后处理",
    ),
    (
        "            // commit min offset in buffer to offset store",
        "            // 将 buffer 内最小 offset 提交到 ConsumerOffsetManager",
    ),
    (
        "            // refer: org.apache.rocketmq.broker.processor.PopBufferMergeService.scan",
        "            // 过期判定逻辑参考 PopBufferMergeService.scan",
    ),
    (
        "    protected static class ConsumerRecords {",
        "    /** 单个 group@topic@queueId 下的 in-flight Pop 记录双跳表结构。 */\n    protected static class ConsumerRecords {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/pop/PopConsumerContext.java"] = [
    (
        "public class PopConsumerContext {",
        "/**\n * 单次 Pop 拉取会话上下文：聚合多队列 GetMessageResult、生成 PopConsumerRecord，\n * 并拼接 startOffset/msgOffset/orderCount 等 ExtraInfo 字符串。\n */\npublic class PopConsumerContext {",
    ),
    (
        "    public boolean isFound() {",
        "    /** 是否至少有一个队列拉取到消息。 */\n    public boolean isFound() {",
    ),
    (
        "    // offset is consumer last request offset",
        "    // offset 为 consumer 本次请求的起始拉取位点",
    ),
    (
        "    public void addGetMessageResult(GetMessageResult result,",
        "    /** 合并单队列拉取结果：构建 PopConsumerRecord 并累加 restCount。 */\n    public void addGetMessageResult(GetMessageResult result,",
    ),
    (
        "    public void addRestCount(long delta) {",
        "    /** 累加队列剩余可 Pop 消息估算数。 */\n    public void addRestCount(long delta) {",
    ),
    (
        "    public int getMessageCount() {",
        "    /** 汇总所有 GetMessageResult 的消息条数。 */\n    public int getMessageCount() {",
    ),
]
