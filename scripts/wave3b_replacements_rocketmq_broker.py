"""RocketMQ 5.5.0 wave3b broker client/coldctr/config [15:30] replacements."""

R: dict[str, list[tuple[str, str]]] = {}

R["broker/src/main/java/org/apache/rocketmq/broker/client/ProducerManager.java"] = [
    (
        "public class ProducerManager {",
        "/**\n * 生产者连接管理器：维护 producer group 到 Netty {@link Channel} 的映射，\n * 负责注册/注销、过期扫描、通道关闭事件及事务回查时的可用通道选取。\n */\npublic class ProducerManager {",
    ),
    (
        "    private static final long CHANNEL_EXPIRED_TIMEOUT = 1000 * 120;",
        "    /** 通道无心跳超过该毫秒数视为过期并清理。 */\n    private static final long CHANNEL_EXPIRED_TIMEOUT = 1000 * 120;",
    ),
    (
        "    private final ConcurrentMap<String /* group name */, ConcurrentMap<Channel, ClientChannelInfo>> groupChannelTable =",
        "    /** group 名 → (Channel → 客户端通道信息) 二级表。 */\n    private final ConcurrentMap<String /* group name */, ConcurrentMap<Channel, ClientChannelInfo>> groupChannelTable =",
    ),
    (
        "    public ProducerManager(final BrokerStatsManager brokerStatsManager, final BrokerConfig brokerConfig) {",
        "    /** 注入统计管理器与 Broker 配置。 */\n    public ProducerManager(final BrokerStatsManager brokerStatsManager, final BrokerConfig brokerConfig) {",
    ),
    (
        "    public boolean groupOnline(String group) {",
        "    /** 判断指定 producer group 是否仍有在线连接。 */\n    public boolean groupOnline(String group) {",
    ),
    (
        "    public ProducerTableInfo getProducerTable() {",
        "    /** 汇总所有 group 下在线生产者信息供管理接口查询。 */\n    public ProducerTableInfo getProducerTable() {",
    ),
    (
        "    public void scanNotActiveChannel() {",
        "    /** 定时扫描并移除超时未更新的通道，空 group 一并删除。 */\n    public void scanNotActiveChannel() {",
    ),
    (
        "    public boolean doChannelCloseEvent(final String remoteAddr, final Channel channel) {",
        "    /** Netty 通道关闭时从各 group 表移除对应连接并触发变更监听。 */\n    public boolean doChannelCloseEvent(final String remoteAddr, final Channel channel) {",
    ),
    (
        "    public void registerProducer(final String group, final ClientChannelInfo clientChannelInfo) {",
        "    /** 注册或刷新 producer 连接；可配置拒绝新 producer 注册（事务场景）。 */\n    public void registerProducer(final String group, final ClientChannelInfo clientChannelInfo) {",
    ),
    (
        "    public void unregisterProducer(final String group, final ClientChannelInfo clientChannelInfo) {",
        "    /** 显式注销 producer 连接，group 为空时移除整个 group。 */\n    public void unregisterProducer(final String group, final ClientChannelInfo clientChannelInfo) {",
    ),
    (
        "    public Channel getAvailableChannel(String groupId) {",
        "    /** 轮询选取 group 内 active 且可写的 Channel，供事务状态回查使用。 */\n    public Channel getAvailableChannel(String groupId) {",
    ),
    (
        "    public Channel findChannel(String clientId) {",
        "    /** 按 clientId 查找已注册通道。 */\n    public Channel findChannel(String clientId) {",
    ),
    (
        "    public void appendProducerChangeListener(ProducerChangeListener producerChangeListener) {",
        "    /** 注册 producer 上下线事件监听器。 */\n    public void appendProducerChangeListener(ProducerChangeListener producerChangeListener) {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/client/net/Broker2Client.java"] = [
    (
        "public class Broker2Client {",
        "/**\n * Broker 主动调用 Client 的 RPC 封装：重置消费位点、查询消费进度、\n * 事务状态回查及通知 consumerId 变更等下行 Remoting 请求。\n */\npublic class Broker2Client {",
    ),
    (
        "    public Broker2Client(BrokerController brokerController) {",
        "    /** 绑定 {@link BrokerController} 以获取 RemotingServer 与消费者管理器。 */\n    public Broker2Client(BrokerController brokerController) {",
    ),
    (
        "    public void notifyUnsubscribeLite(Channel channel, NotifyUnsubscribeLiteRequestHeader requestHeader) {",
        "    /** 向客户端发送轻量退订通知（oneway）。 */\n    public void notifyUnsubscribeLite(Channel channel, NotifyUnsubscribeLiteRequestHeader requestHeader) {",
    ),
    (
        "    public void checkProducerTransactionState(",
        "    /** 向 producer 通道发送事务状态回查请求，附带序列化后的消息体。 */\n    public void checkProducerTransactionState(",
    ),
    (
        "    public RemotingCommand callClient(final Channel channel,\n        final RemotingCommand request\n    ) throws RemotingSendRequestException, RemotingTimeoutException, InterruptedException {",
        "    /** 同步调用客户端并等待响应（默认 10s 超时）。 */\n    public RemotingCommand callClient(final Channel channel,\n        final RemotingCommand request\n    ) throws RemotingSendRequestException, RemotingTimeoutException, InterruptedException {",
    ),
    (
        "    public void notifyConsumerIdsChanged(",
        "    /** 通知 consumer 同组内 consumerId 列表已变更。 */\n    public void notifyConsumerIdsChanged(",
    ),
    (
        "    public RemotingCommand resetOffset(String topic, String group, long timeStamp, boolean isForce,\n        boolean isC) throws RemotingCommandException {",
        "    /** 按时间戳计算各队列目标位点并下发 RESET_CONSUMER_CLIENT_OFFSET 到在线 consumer。 */\n    public RemotingCommand resetOffset(String topic, String group, long timeStamp, boolean isForce,\n        boolean isC) throws RemotingCommandException {",
    ),
    (
        "    private List<MessageQueueForC> convertOffsetTable2OffsetList(Map<MessageQueue, Long> table) {",
        "    /** 将 Java MessageQueue 位点表转为 C++ 客户端使用的结构列表。 */\n    private List<MessageQueueForC> convertOffsetTable2OffsetList(Map<MessageQueue, Long> table) {",
    ),
    (
        "    public RemotingCommand getConsumeStatus(String topic, String group, String originClientId) {",
        "    /** 向在线 consumer 拉取各 MessageQueue 消费进度，可指定单个 clientId。 */\n    public RemotingCommand getConsumeStatus(String topic, String group, String originClientId) {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/client/rebalance/RebalanceLockManager.java"] = [
    (
        "public class RebalanceLockManager {",
        "/**\n * 广播消费模式下的队列 rebalance 锁管理器：\n * 保证同一 MessageQueue 在同一时刻仅被一个 consumer 实例持有。\n */\npublic class RebalanceLockManager {",
    ),
    (
        "    private final static long REBALANCE_LOCK_MAX_LIVE_TIME = Long.parseLong(System.getProperty(",
        "    /** 锁最大存活时间（毫秒），超时后可被其他 client 抢占。 */\n    private final static long REBALANCE_LOCK_MAX_LIVE_TIME = Long.parseLong(System.getProperty(",
    ),
    (
        "    public boolean isLockAllExpired(final String group) {",
        "    /** 判断 group 下所有队列锁是否均已过期（或 group 不存在）。 */\n    public boolean isLockAllExpired(final String group) {",
    ),
    (
        "    public boolean tryLock(final String group, final MessageQueue mq, final String clientId) {",
        "    /** 尝试为 client 锁定单个 MessageQueue；已持有则刷新时间戳。 */\n    public boolean tryLock(final String group, final MessageQueue mq, final String clientId) {",
    ),
    (
        "    public Set<MessageQueue> tryLockBatch(final String group, final Set<MessageQueue> mqs,\n        final String clientId) {",
        "    /** 批量尝试加锁，返回成功锁定的队列集合。 */\n    public Set<MessageQueue> tryLockBatch(final String group, final Set<MessageQueue> mqs,\n        final String clientId) {",
    ),
    (
        "    public void unlockBatch(final String group, final Set<MessageQueue> mqs, final String clientId) {",
        "    /** 批量释放 client 持有的队列锁。 */\n    public void unlockBatch(final String group, final Set<MessageQueue> mqs, final String clientId) {",
    ),
    (
        "    static class LockEntry {",
        "    /** 单队列锁条目：持有 clientId 与最后更新时间。 */\n    static class LockEntry {",
    ),
    (
        "        public boolean isLocked(final String clientId) {",
        "        /** 判断锁是否仍由指定 client 持有且未过期。 */\n        public boolean isLocked(final String clientId) {",
    ),
    (
        "        public boolean isExpired() {",
        "        /** 距上次更新超过 {@link #REBALANCE_LOCK_MAX_LIVE_TIME} 则视为过期。 */\n        public boolean isExpired() {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/coldctr/ColdCtrStrategy.java"] = [
    (
        "public interface ColdCtrStrategy {",
        "/**\n * 冷读流控策略 SPI：根据全局冷读量决策加速或减速各 consumer group 的冷读阈值。\n */\npublic interface ColdCtrStrategy {",
    ),
    (
        "    /**\n     * Calculate the determining factor about whether to accelerate or decelerate\n     * @return\n     */",
        "    /**\n     * 计算加/减速决策因子；正值倾向加速，负值倾向减速。\n     * @return 决策因子，简单策略可返回 null\n     */",
    ),
    (
        "    /**\n     * Promote the speed for consumerGroup to read cold data\n     * @param consumerGroup\n     * @param currentThreshold\n     */",
        "    /**\n     * 提高指定 consumer group 的冷读阈值（放宽限速）。\n     * @param consumerGroup 消费组名（自适应策略带 ||adaptive 后缀）\n     * @param currentThreshold 当前阈值\n     */",
    ),
    (
        "    /**\n     * Decelerate the speed for consumerGroup to read cold data\n     * @param consumerGroup\n     * @param currentThreshold\n     */",
        "    /**\n     * 降低指定 consumer group 的冷读阈值（收紧限速）。\n     * @param consumerGroup 消费组名\n     * @param currentThreshold 当前阈值\n     */",
    ),
    (
        "    /**\n     * Collect the total number of cold read data in the system\n     * @param globalAcc\n     */",
        "    /**\n     * 采集本周期全局冷读累计量，供策略更新内部状态。\n     * @param globalAcc 全局冷读字节累计\n     */",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/coldctr/ColdDataCgCtrService.java"] = [
    (
        "/**\n * store the cg cold read ctr table and acc the size of the cold\n * reading msg, timing to clear the table and set acc to zero\n */",
        "/**\n * 冷数据消费组流控服务：维护运行时/配置阈值表，累计冷读量，\n * 定时清零并按策略动态调节各 group 冷读限速。\n */",
    ),
    (
        "    private static final AtomicLong GLOBAL_ACC = new AtomicLong(0L);",
        "    /** 全局冷读字节累计，每周期清零。 */\n    private static final AtomicLong GLOBAL_ACC = new AtomicLong(0L);",
    ),
    (
        "    private final ConcurrentHashMap<String, AccAndTimeStamp> cgColdThresholdMapRuntime = new ConcurrentHashMap<>();",
        "    /** 运行时表：正在读冷数据的 consumer group → 累计量与时间戳。 */\n    private final ConcurrentHashMap<String, AccAndTimeStamp> cgColdThresholdMapRuntime = new ConcurrentHashMap<>();",
    ),
    (
        "    private final ConcurrentHashMap<String, Long> cgColdThresholdMapConfig = new ConcurrentHashMap<>();",
        "    /** 配置表：管理员或自适应策略写入的 per-group 冷读阈值。 */\n    private final ConcurrentHashMap<String, Long> cgColdThresholdMapConfig = new ConcurrentHashMap<>();",
    ),
    (
        "    public ColdDataCgCtrService(BrokerController brokerController) {",
        "    /** 按配置选择 PID 自适应或简单冷读流控策略。 */\n    public ColdDataCgCtrService(BrokerController brokerController) {",
    ),
    (
        "    @Override\n    public void run() {",
        "    /** 后台循环：定期清零累计、清理超时 group 并执行加减速策略。 */\n    @Override\n    public void run() {",
    ),
    (
        "    public String getColdDataFlowCtrInfo() {",
        "    /** 导出运行时表、配置表及全局累计的 JSON 快照。 */\n    public String getColdDataFlowCtrInfo() {",
    ),
    (
        "    private void clearDataAcc() {",
        "    /** 清理长期无冷读的 group、重置累计并按策略 promote/decelerate。 */\n    private void clearDataAcc() {",
    ),
    (
        "    public void coldAcc(String consumerGroup, long coldDataToAcc) {",
        "    /** 累加指定 group 与本周期的全局冷读字节数。 */\n    public void coldAcc(String consumerGroup, long coldDataToAcc) {",
    ),
    (
        "    public boolean isCgNeedColdDataFlowCtr(String consumerGroup) {",
        "    /** 判断该 group 是否应触发冷读流控（超 group 或全局阈值）。 */\n    public boolean isCgNeedColdDataFlowCtr(String consumerGroup) {",
    ),
    (
        "    public boolean isGlobalColdCtr() {",
        "    /** 全局冷读累计是否超过 {@link BrokerConfig#getGlobalColdReadThreshold()}。 */\n    public boolean isGlobalColdCtr() {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/coldctr/ColdDataPullRequestHoldService.java"] = [
    (
        "/**\n * just requests are type of pull have the qualification to be put into this hold queue.\n * if the pull request is reading cold data and that request will be cold at the first time,\n * then the pull request will be cold in this @code pullRequestLinkedBlockingQueue,\n * in @code coldTimeoutMillis later the pull request will be warm and marked holded\n */",
        "/**\n * 冷读 Pull 请求挂起服务：首次触发冷读流控的 Pull 请求入队，\n * 经过 {@link #coldHoldTimeoutMillis} 后唤醒并标记不再挂起。\n */",
    ),
    (
        "    public static final String NO_SUSPEND_KEY = \"_noSuspend_\";",
        "    /** 唤醒后写入请求扩展字段，表示后续不再挂起。 */\n    public static final String NO_SUSPEND_KEY = \"_noSuspend_\";",
    ),
    (
        "    public void suspendColdDataReadRequest(PullRequest pullRequest) {",
        "    /** 冷读流控开启时将 Pull 请求放入挂起队列。 */\n    public void suspendColdDataReadRequest(PullRequest pullRequest) {",
    ),
    (
        "    @Override\n    public void run() {",
        "    /** 后台循环检查挂起队列，超时请求唤醒并执行 pull 处理。 */\n    @Override\n    public void run() {",
    ),
    (
        "    private void checkColdDataPullRequest() {",
        "    /** 遍历挂起队列，超时条目添加 NO_SUSPEND 标记并触发 wakeup 处理。 */\n    private void checkColdDataPullRequest() {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/coldctr/PIDAdaptiveColdCtrStrategy.java"] = [
    (
        "public class PIDAdaptiveColdCtrStrategy implements ColdCtrStrategy {",
        "/**\n * 基于 PID 控制的自适应冷读流控：根据全局冷读误差历史动态调节 group 阈值。\n */\npublic class PIDAdaptiveColdCtrStrategy implements ColdCtrStrategy {",
    ),
    (
        "    private static final Double KP = 0.5, KI = 0.3, KD = 0.2;",
        "    /** PID 比例、积分、微分权重系数。 */\n    private static final Double KP = 0.5, KI = 0.3, KD = 0.2;",
    ),
    (
        "    public PIDAdaptiveColdCtrStrategy(ColdDataCgCtrService coldDataCgCtrService, Long expectGlobalVal) {",
        "    /** 绑定流控服务并设置期望全局冷读目标值。 */\n    public PIDAdaptiveColdCtrStrategy(ColdDataCgCtrService coldDataCgCtrService, Long expectGlobalVal) {",
    ),
    (
        "    @Override\n    public Double decisionFactor() {",
        "    /** 历史误差样本足够时计算 PID 决策因子。 */\n    @Override\n    public Double decisionFactor() {",
    ),
    (
        "    @Override\n    public void promote(String consumerGroup, Long currentThreshold) {",
        "    /** 决策因子为正时将阈值提高 1.5 倍。 */\n    @Override\n    public void promote(String consumerGroup, Long currentThreshold) {",
    ),
    (
        "    @Override\n    public void decelerate(String consumerGroup, Long currentThreshold) {",
        "    /** 决策因子为负时将阈值降至 0.8 倍（不低于默认 group 阈值）。 */\n    @Override\n    public void decelerate(String consumerGroup, Long currentThreshold) {",
    ),
    (
        "    @Override\n    public void collect(Long globalAcc) {",
        "    /** 记录期望与实际全局冷读之差，维护最近 {@link #MAX_STORE_NUMS} 条误差历史。 */\n    @Override\n    public void collect(Long globalAcc) {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/coldctr/SimpleColdCtrStrategy.java"] = [
    (
        "public class SimpleColdCtrStrategy implements ColdCtrStrategy {",
        "/**\n * 简单冷读流控策略：超阈值时固定倍率升阈，仅全局超限时才降阈。\n */\npublic class SimpleColdCtrStrategy implements ColdCtrStrategy {",
    ),
    (
        "    @Override\n    public void promote(String consumerGroup, Long currentThreshold) {",
        "    /** 无条件将 group 阈值提高 1.5 倍。 */\n    @Override\n    public void promote(String consumerGroup, Long currentThreshold) {",
    ),
    (
        "    @Override\n    public void decelerate(String consumerGroup, Long currentThreshold) {",
        "    /** 仅全局超限时将阈值降至 0.8 倍（不低于默认 group 阈值）。 */\n    @Override\n    public void decelerate(String consumerGroup, Long currentThreshold) {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/config/v1/RocksDBConfigManager.java"] = [
    (
        "public class RocksDBConfigManager {",
        "/**\n * Broker 元数据 RocksDB 封装：初始化存储、读写 KV、维护 {@link DataVersion} 及批量/WAL 刷盘。\n */\npublic class RocksDBConfigManager {",
    ),
    (
        "    public ConfigRocksDBStorage configRocksDBStorage = null;",
        "    /** 底层 {@link ConfigRocksDBStorage} 实例。 */\n    public ConfigRocksDBStorage configRocksDBStorage = null;",
    ),
    (
        "    public boolean init(boolean readOnly) {",
        "    /** 打开 RocksDB；readOnly 为 true 时只读打开（迁移场景）。 */\n    public boolean init(boolean readOnly) {",
    ),
    (
        "    public boolean loadDataVersion() {",
        "    /** 从 version 列族加载 KV 数据版本号。 */\n    public boolean loadDataVersion() {",
    ),
    (
        "    public boolean loadData(BiConsumer<byte[], byte[]> biConsumer) {",
        "    /** 迭代 default 列族全部 KV 并交给回调解码。 */\n    public boolean loadData(BiConsumer<byte[], byte[]> biConsumer) {",
    ),
    (
        "    public void flushWAL() {",
        "    /** 刷 WAL 并按间隔 flush MemTable。 */\n    public void flushWAL() {",
    ),
    (
        "    public void updateKvDataVersion() throws Exception {",
        "    /** 递增版本计数并持久化到 version 列族。 */\n    public void updateKvDataVersion() throws Exception {",
    ),
    (
        "    public void batchPutWithWal(final WriteBatch batch) throws Exception {",
        "    /** 批量写入并刷 WAL。 */\n    public void batchPutWithWal(final WriteBatch batch) throws Exception {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/config/v1/RocksDBConsumerOffsetManager.java"] = [
    (
        "public class RocksDBConsumerOffsetManager extends ConsumerOffsetManager {",
        "/**\n * 基于 RocksDB 的消费位点管理器：支持独立/统一 RocksDB 实例，\n * 启动时与 JSON 文件合并并按需从旧库迁移。\n */\npublic class RocksDBConsumerOffsetManager extends ConsumerOffsetManager {",
    ),
    (
        "    public RocksDBConsumerOffsetManager(BrokerController brokerController) {",
        "    /** 按 Broker 配置选择单库或多库 RocksDB 路径。 */\n    public RocksDBConsumerOffsetManager(BrokerController brokerController) {",
    ),
    (
        "    @Override\n    public boolean load() {",
        "    /** 初始化 RocksDB、加载位点并与 JSON 合并；统一库模式下尝试迁移。 */\n    @Override\n    public boolean load() {",
    ),
    (
        "    private boolean merge() {",
        "    /** JSON 版本更新时将 JSON 位点全量导入 RocksDB。 */\n    private boolean merge() {",
    ),
    (
        "    protected void decodeOffset(final byte[] key, final byte[] body) {",
        "    /** 解码 topic@group 键与 {@link RocksDBOffsetSerializeWrapper} 值到内存表。 */\n    protected void decodeOffset(final byte[] key, final byte[] body) {",
    ),
    (
        "    @Override\n    public synchronized void persist() {",
        "    /** 增量模式仅刷版本与 WAL；否则全量 batch 写入 RocksDB。 */\n    @Override\n    public synchronized void persist() {",
    ),
    (
        "    @Override\n    public void commitOffset(String clientHost, String group, String topic, int queueId, long offset) {",
        "    /** 更新内存位点并按配置增量或跳过持久化。 */\n    @Override\n    public void commitOffset(String clientHost, String group, String topic, int queueId, long offset) {",
    ),
    (
        "    private void migrateFromSeparateRocksDBs() {",
        "    /** 统一库模式下只读打开旧独立库，版本较新则导入位点数据。 */\n    private void migrateFromSeparateRocksDBs() {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/config/v1/RocksDBLmqSubscriptionGroupManager.java"] = [
    (
        "public class RocksDBLmqSubscriptionGroupManager extends RocksDBSubscriptionGroupManager {",
        "/**\n * LMQ 场景下的订阅组管理器：LMQ group 视为始终存在且使用默认配置，不落库。\n */\npublic class RocksDBLmqSubscriptionGroupManager extends RocksDBSubscriptionGroupManager {",
    ),
    (
        "    @Override\n    public SubscriptionGroupConfig findSubscriptionGroupConfig(final String group) {",
        "    /** LMQ group 返回仅含组名的默认配置，否则委托父类。 */\n    @Override\n    public SubscriptionGroupConfig findSubscriptionGroupConfig(final String group) {",
    ),
    (
        "    @Override\n    public void updateSubscriptionGroupConfig(final SubscriptionGroupConfig config) {",
        "    /** 忽略 LMQ group 的更新请求。 */\n    @Override\n    public void updateSubscriptionGroupConfig(final SubscriptionGroupConfig config) {",
    ),
    (
        "    @Override\n    public boolean containsSubscriptionGroup(String group) {",
        "    /** LMQ group 恒为 true，其余走父类逻辑。 */\n    @Override\n    public boolean containsSubscriptionGroup(String group) {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/config/v1/RocksDBLmqTopicConfigManager.java"] = [
    (
        "public class RocksDBLmqTopicConfigManager extends RocksDBTopicConfigManager {",
        "/**\n * LMQ 场景下的 Topic 配置管理器：LMQ topic 动态生成单队列读写配置，不持久化。\n */\npublic class RocksDBLmqTopicConfigManager extends RocksDBTopicConfigManager {",
    ),
    (
        "    @Override\n    public TopicConfig selectTopicConfig(final String topic) {",
        "    /** LMQ topic 返回 1 读 1 写队列的默认配置。 */\n    @Override\n    public TopicConfig selectTopicConfig(final String topic) {",
    ),
    (
        "    @Override\n    public void updateTopicConfig(final TopicConfig topicConfig) {",
        "    /** 忽略 LMQ topic 的更新请求。 */\n    @Override\n    public void updateTopicConfig(final TopicConfig topicConfig) {",
    ),
    (
        "    @Override\n    public boolean containsTopic(String topic) {",
        "    /** LMQ topic 恒为 true，其余走父类逻辑。 */\n    @Override\n    public boolean containsTopic(String topic) {",
    ),
    (
        "    private TopicConfig simpleLmqTopicConfig(String topic) {",
        "    /** 构造 LMQ topic 的单队列读写 {@link TopicConfig}。 */\n    private TopicConfig simpleLmqTopicConfig(String topic) {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/config/v1/RocksDBOffsetSerializeWrapper.java"] = [
    (
        "public class RocksDBOffsetSerializeWrapper extends RemotingSerializable {",
        "/**\n * RocksDB 消费位点值序列化包装：queueId → offset 映射表。\n */\npublic class RocksDBOffsetSerializeWrapper extends RemotingSerializable {",
    ),
    (
        "    private ConcurrentMap<Integer, Long> offsetTable = null;",
        "    /** 队列 ID 到消费位点的映射。 */\n    private ConcurrentMap<Integer, Long> offsetTable = null;",
    ),
    (
        "    public ConcurrentMap<Integer, Long> getOffsetTable() {",
        "    /** 返回位点映射表。 */\n    public ConcurrentMap<Integer, Long> getOffsetTable() {",
    ),
    (
        "    public void setOffsetTable(ConcurrentMap<Integer, Long> offsetTable) {",
        "    /** 设置位点映射表。 */\n    public void setOffsetTable(ConcurrentMap<Integer, Long> offsetTable) {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/config/v1/RocksDBTopicConfigManager.java"] = [
    (
        "public class RocksDBTopicConfigManager extends TopicConfigManager {",
        "/**\n * 基于 RocksDB 的 Topic 配置管理器：读写 topic 元数据，\n * 支持与 JSON 合并及从独立库迁移到统一 metadata 库。\n */\npublic class RocksDBTopicConfigManager extends TopicConfigManager {",
    ),
    (
        "    public RocksDBTopicConfigManager(BrokerController brokerController) {",
        "    /** 按 Broker 配置初始化 RocksDB 路径与列族。 */\n    public RocksDBTopicConfigManager(BrokerController brokerController) {",
    ),
    (
        "    @Override\n    public boolean load() {",
        "    /** 加载 RocksDB topic 配置、合并 JSON 并在统一库模式下迁移。 */\n    @Override\n    public boolean load() {",
    ),
    (
        "    protected void decodeTopicConfig(byte[] key, byte[] body) {",
        "    /** 将 RocksDB KV 解码为 {@link TopicConfig} 写入内存表。 */\n    protected void decodeTopicConfig(byte[] key, byte[] body) {",
    ),
    (
        "    @Override\n    public TopicConfig putTopicConfig(TopicConfig topicConfig) {",
        "    /** 更新内存表并同步写入 RocksDB。 */\n    @Override\n    public TopicConfig putTopicConfig(TopicConfig topicConfig) {",
    ),
    (
        "    @Override\n    public synchronized void persist() {",
        "    /** 实时持久化模式下 flush WAL。 */\n    @Override\n    public synchronized void persist() {",
    ),
    (
        "    private void migrateFromSeparateRocksDBs() {",
        "    /** 统一库模式下从旧 topics 独立库导入较新版本数据。 */\n    private void migrateFromSeparateRocksDBs() {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/config/v2/ConfigHelper.java"] = [
    (
        "public class ConfigHelper {",
        "/**\n * Broker v2 配置 RocksDB 键值编解码辅助：按表前缀/记录前缀构造\n * DataVersion 与配置条目的 Netty {@link ByteBuf} 键值。\n */\npublic class ConfigHelper {",
    ),
    (
        "    /**\n     * <p>\n     * Layout of data version key:\n     * [table-prefix, 1 byte][table-id, 2 byte][record-prefix, 1 byte][data-version-bytes]\n     * </p>\n     *\n     * <p>\n     * Layout of data version value:\n     * [state-machine-version, 8 bytes][timestamp, 8 bytes][sequence counter, 8 bytes]\n     * </p>\n     *\n     * @throws RocksDBException if RocksDB raises an error\n     */",
        "    /**\n     * 从 RocksDB 加载指定表的 DataVersion。\n     * <p>键布局：[table-prefix,1][table-id,2][record-prefix,1][data-version-bytes]</p>\n     * <p>值布局：[state-machine-version,8][timestamp,8][counter,8]</p>\n     *\n     * @throws RocksDBException RocksDB 读失败时抛出\n     */",
    ),
    (
        "    public static void stampDataVersion(WriteBatch writeBatch, TableId table, DataVersion dataVersion, long stateMachineVersion)",
        "    /** 递增 DataVersion 并将键值对写入 WriteBatch。 */\n    public static void stampDataVersion(WriteBatch writeBatch, TableId table, DataVersion dataVersion, long stateMachineVersion)",
    ),
    (
        "    public static void onDataVersionLoad(ByteBuf buf, DataVersion dataVersion) {",
        "    /** 从 ByteBuf 解析 stateVersion、timestamp、counter 填入 DataVersion。 */\n    public static void onDataVersionLoad(ByteBuf buf, DataVersion dataVersion) {",
    ),
    (
        "    public static ByteBuf keyBufOf(TableId tableId, final String name) {",
        "    /** 构造配置记录键：[table-prefix][table-id][data-prefix][name-len][name-bytes]。 */\n    public static ByteBuf keyBufOf(TableId tableId, final String name) {",
    ),
    (
        "    public static ByteBuf valueBufOf(final Object config, SerializationType serializationType) {",
        "    /** 按序列化类型（当前仅 JSON）构造配置值 ByteBuf。 */\n    public static ByteBuf valueBufOf(final Object config, SerializationType serializationType) {",
    ),
    (
        "    public static byte[] readBytes(final ByteBuf buf) {",
        "    /** 读取 ByteBuf 全部可读字节为数组。 */\n    public static byte[] readBytes(final ByteBuf buf) {",
    ),
]
