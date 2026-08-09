"""RocketMQ 5.5.0 wave4a broker config/v2/dledger/failover/filter/latency [0:15] replacements."""

R: dict[str, list[tuple[str, str]]] = {}

R["broker/src/main/java/org/apache/rocketmq/broker/config/v2/ConfigStorage.java"] = [
    (
        "/**\n * https://book.tidb.io/session1/chapter3/tidb-kv-to-relation.html\n */",
        "/**\n * Broker 配置 RocksDB 存储层：以 KV 形式持久化 Topic、订阅组等元数据，\n * 并负责 WAL 刷盘与同步。键值布局参考 TiDB KV 映射模型。\n *\n * @see <a href=\"https://book.tidb.io/session1/chapter3/tidb-kv-to-relation.html\">Table, Key Value Mapping</a>\n */",
    ),
    (
        "    /**\n     * Number of write ops since previous flush.\n     */",
        "    /** 自上次 WAL 刷盘以来累计的写操作次数。 */",
    ),
    (
        "    public ConfigStorage(MessageStoreConfig messageStoreConfig) {",
        "    /** 在消息存储根目录下创建 {@code config/rdb} 子库并启动 WAL 刷盘后台线程。 */\n    public ConfigStorage(MessageStoreConfig messageStoreConfig) {",
    ),
    (
        "    private void statNettyMemory() {",
        "    /** 周期性输出 Netty 池化内存分配器指标。 */\n    private void statNettyMemory() {",
    ),
    (
        "    @Override\n    public synchronized boolean start() {",
        "    /** 启动 RocksDB 并调度统计任务与 {@link FlushSyncService}。 */\n    @Override\n    public synchronized boolean start() {",
    ),
    (
        "    @Override\n    protected boolean postLoad() {",
        "    /** 校验 Unsafe 可用性、创建目录并打开默认列族。 */\n    @Override\n    protected boolean postLoad() {",
    ),
    (
        "    @Override\n    protected void preShutdown() {",
        "    /** 关闭定时任务与 WAL 刷盘服务。 */\n    @Override\n    protected void preShutdown() {",
    ),
    (
        "    protected void initOptions() {",
        "    /** 使用 {@link ConfigHelper} 初始化配置库专用 DB 选项。 */\n    protected void initOptions() {",
    ),
    (
        "    @Override\n    protected void initAbleWalWriteOptions() {",
        "    /** 配置 WAL 写入选项：保留 WAL、不同步 fdatasync 以避免每次写入开销。 */\n    @Override\n    protected void initAbleWalWriteOptions() {",
    ),
    (
        "    public byte[] get(ByteBuffer key) throws RocksDBException {",
        "    /** 按 ByteBuffer 键读取默认列族中的配置值。 */\n    public byte[] get(ByteBuffer key) throws RocksDBException {",
    ),
    (
        "    public void write(WriteBatch writeBatch) throws RocksDBException {",
        "    /** 批量写入并累计写操作计数与 WAL 估算大小。 */\n    public void write(WriteBatch writeBatch) throws RocksDBException {",
    ),
    (
        "    private void accountWriteOps(long dataSize) {",
        "    /** 累加写次数与 WAL 数据量估算。 */\n    private void accountWriteOps(long dataSize) {",
    ),
    (
        "    public RocksIterator iterate(ByteBuffer beginKey, ByteBuffer endKey) {",
        "    /** 在 [beginKey, endKey) 范围内全序扫描配置键。 */\n    public RocksIterator iterate(ByteBuffer beginKey, ByteBuffer endKey) {",
    ),
    (
        "    /**\n     * RocksDB writes contain 3 stages: application memory buffer --> OS Page Cache --> Disk.\n     * Given that we are having DBOptions::manual_wal_flush, we need to manually call DB::FlushWAL and DB::SyncWAL\n     * Note: DB::FlushWAL(true) will internally call DB::SyncWAL.\n     * <p>\n     * See <a href=\"https://rocksdb.org/blog/2017/08/25/flushwal.html\">Flush And Sync WAL</a>\n     */",
        "    /**\n     * WAL 刷盘同步后台服务：RocksDB 写入经应用缓冲、页缓存到磁盘三阶段，\n     * 在 {@code manual_wal_flush} 模式下需手动调用 {@code FlushWAL}/{@code SyncWAL}。\n     * <p>\n     * 参见 <a href=\"https://rocksdb.org/blog/2017/08/25/flushwal.html\">Flush And Sync WAL</a>\n     */",
    ),
    (
        "        @Override\n        public String getServiceName() {",
        "        /** 返回服务线程名称。 */\n        @Override\n        public String getServiceName() {",
    ),
    (
        "        @Override\n        public void run() {",
        "        /** 周期性刷 WAL，退出前执行最终同步。 */\n        @Override\n        public void run() {",
    ),
    (
        "        private void flushAndSyncWAL(boolean onExit) throws RocksDBException {",
        "        /** 按写次数、时间间隔或 WAL 滚动阈值触发刷盘与同步。 */\n        private void flushAndSyncWAL(boolean onExit) throws RocksDBException {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/config/v2/RecordPrefix.java"] = [
    (
        "public enum RecordPrefix {",
        "/**\n * 配置记录类型前缀：标识 RocksDB 键值对中的记录语义（版本元数据或业务数据）。\n */\npublic enum RecordPrefix {",
    ),
    (
        "    UNSPECIFIED((byte)0),",
        "    /** 未指定类型。 */\n    UNSPECIFIED((byte)0),",
    ),
    (
        "    DATA_VERSION((byte)1),",
        "    /** 数据版本记录。 */\n    DATA_VERSION((byte)1),",
    ),
    (
        "    DATA((byte)2);",
        "    /** 业务配置数据记录。 */\n    DATA((byte)2);",
    ),
    (
        "    RecordPrefix(byte value) {",
        "    /** 绑定单字节编码值。 */\n    RecordPrefix(byte value) {",
    ),
    (
        "    public byte getValue() {",
        "    /** 返回记录前缀的字节值。 */\n    public byte getValue() {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/config/v2/SerializationType.java"] = [
    (
        "public enum SerializationType {",
        "/**\n * 配置值序列化格式：写入 RocksDB 时在值首部携带的类型标识。\n */\npublic enum SerializationType {",
    ),
    (
        "    UNSPECIFIED((byte) 0),",
        "    /** 未指定格式。 */\n    UNSPECIFIED((byte) 0),",
    ),
    (
        "    JSON((byte) 1),",
        "    /** JSON 文本序列化。 */\n    JSON((byte) 1),",
    ),
    (
        "    PROTOBUF((byte) 2),",
        "    /** Protocol Buffers 二进制序列化。 */\n    PROTOBUF((byte) 2),",
    ),
    (
        "    FLAT_BUFFERS((byte) 3);",
        "    /** FlatBuffers 二进制序列化。 */\n    FLAT_BUFFERS((byte) 3);",
    ),
    (
        "    SerializationType(byte value) {",
        "    /** 绑定单字节编码值。 */\n    SerializationType(byte value) {",
    ),
    (
        "    public byte getValue() {",
        "    /** 返回序列化类型的字节值。 */\n    public byte getValue() {",
    ),
    (
        "    public static SerializationType valueOf(byte value) {",
        "    /** 按字节值解析序列化类型，未知时返回 {@link #UNSPECIFIED}。 */\n    public static SerializationType valueOf(byte value) {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/config/v2/SubscriptionGroupManagerV2.java"] = [
    (
        "public class SubscriptionGroupManagerV2 extends SubscriptionGroupManager {",
        "/**\n * 基于 RocksDB {@link ConfigStorage} 的订阅组配置管理器：\n * 从 KV 存储加载/持久化 {@link SubscriptionGroupConfig}。\n */\npublic class SubscriptionGroupManagerV2 extends SubscriptionGroupManager {",
    ),
    (
        "    public SubscriptionGroupManagerV2(BrokerController brokerController, ConfigStorage configStorage) {",
        "    /** 绑定 broker 控制器与配置存储实例。 */\n    public SubscriptionGroupManagerV2(BrokerController brokerController, ConfigStorage configStorage) {",
    ),
    (
        "    @Override\n    public boolean load() {",
        "    /** 加载数据版本与全部订阅组配置。 */\n    @Override\n    public boolean load() {",
    ),
    (
        "    public boolean loadDataVersion() {",
        "    /** 从 {@link TableId#SUBSCRIPTION_GROUP} 表读取并应用数据版本。 */\n    public boolean loadDataVersion() {",
    ),
    (
        "    private boolean loadSubscriptions() {",
        "    /** 扫描订阅组表前缀范围内的全部 KV 并反序列化。 */\n    private boolean loadSubscriptions() {",
    ),
    (
        "    private SubscriptionGroupConfig parseSubscription(byte[] key, byte[] value) {",
        "    /** 解析 RocksDB 键值对为 {@link SubscriptionGroupConfig}（当前仅支持 JSON）。 */\n    private SubscriptionGroupConfig parseSubscription(byte[] key, byte[] value) {",
    ),
    (
        "    @Override\n    public synchronized void persist() {",
        "    /** 强制刷 WAL 到磁盘（核心元数据变更后调用）。 */\n    @Override\n    public synchronized void persist() {",
    ),
    (
        "    @Override\n    public SubscriptionGroupConfig findSubscriptionGroupConfig(final String group) {",
        "    /** 查询订阅组；LMQ 组名返回默认配置而不查库。 */\n    @Override\n    public SubscriptionGroupConfig findSubscriptionGroupConfig(final String group) {",
    ),
    (
        "    @Override\n    public void updateSubscriptionGroupConfig(final SubscriptionGroupConfig config) {",
        "    /** 更新内存并写入 RocksDB，同时戳记数据版本。 */\n    @Override\n    public void updateSubscriptionGroupConfig(final SubscriptionGroupConfig config) {",
    ),
    (
        "    @Override\n    public boolean containsSubscriptionGroup(String group) {",
        "    /** LMQ 组名恒视为存在。 */\n    @Override\n    public boolean containsSubscriptionGroup(String group) {",
    ),
    (
        "    @Override\n    protected SubscriptionGroupConfig removeSubscriptionGroupConfig(String groupName) {",
        "    /** 从 RocksDB 删除键并更新内存缓存。 */\n    @Override\n    protected SubscriptionGroupConfig removeSubscriptionGroupConfig(String groupName) {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/config/v2/TableId.java"] = [
    (
        "/**\n * See <a href=\"https://book.tidb.io/session1/chapter3/tidb-kv-to-relation.html\">Table, Key Value Mapping</a>\n */",
        "/**\n * 配置逻辑表标识：嵌入 RocksDB 键中的 2 字节表 ID。\n *\n * @see <a href=\"https://book.tidb.io/session1/chapter3/tidb-kv-to-relation.html\">Table, Key Value Mapping</a>\n */",
    ),
    (
        "    UNSPECIFIED((short) 0),",
        "    /** 未指定表。 */\n    UNSPECIFIED((short) 0),",
    ),
    (
        "    CONSUMER_OFFSET((short) 1),",
        "    /** 消费位点表。 */\n    CONSUMER_OFFSET((short) 1),",
    ),
    (
        "    PULL_OFFSET((short) 2),",
        "    /** Pull 位点表。 */\n    PULL_OFFSET((short) 2),",
    ),
    (
        "    TOPIC((short) 3),",
        "    /** Topic 配置表。 */\n    TOPIC((short) 3),",
    ),
    (
        "    SUBSCRIPTION_GROUP((short) 4);",
        "    /** 订阅组配置表。 */\n    SUBSCRIPTION_GROUP((short) 4);",
    ),
    (
        "    TableId(short value) {",
        "    /** 绑定 2 字节表 ID。 */\n    TableId(short value) {",
    ),
    (
        "    public short getValue() {",
        "    /** 返回表 ID 短整型值。 */\n    public short getValue() {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/config/v2/TablePrefix.java"] = [
    (
        "public enum TablePrefix {",
        "/**\n * 配置键表级前缀：标识该键属于逻辑表命名空间（区别于其他 KV 用途）。\n */\npublic enum TablePrefix {",
    ),
    (
        "    UNSPECIFIED((byte) 0),",
        "    /** 未指定前缀。 */\n    UNSPECIFIED((byte) 0),",
    ),
    (
        "    TABLE((byte) 1);",
        "    /** 标准配置表前缀。 */\n    TABLE((byte) 1);",
    ),
    (
        "    TablePrefix(byte value) {",
        "    /** 绑定单字节前缀值。 */\n    TablePrefix(byte value) {",
    ),
    (
        "    public byte getValue() {",
        "    /** 返回表前缀的字节值。 */\n    public byte getValue() {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/config/v2/TopicConfigManagerV2.java"] = [
    (
        "/**\n * Key layout: [table-prefix, 1 byte][table-id, 2 bytes][record-type-prefix, 1 byte][topic-len, 2 bytes][topic-bytes]\n * Value layout: [serialization-type, 1 byte][topic-config-bytes]\n */",
        "/**\n * 基于 RocksDB {@link ConfigStorage} 的 Topic 配置管理器。\n * <p>\n * 键布局：[table-prefix, 1 byte][table-id, 2 bytes][record-type-prefix, 1 byte][topic-len, 2 bytes][topic-bytes]\n * 值布局：[serialization-type, 1 byte][topic-config-bytes]\n */",
    ),
    (
        "    public TopicConfigManagerV2(BrokerController brokerController, ConfigStorage configStorage) {",
        "    /** 绑定 broker 控制器与配置存储。 */\n    public TopicConfigManagerV2(BrokerController brokerController, ConfigStorage configStorage) {",
    ),
    (
        "    @Override\n    public boolean load() {",
        "    /** 加载数据版本与全部 Topic 配置。 */\n    @Override\n    public boolean load() {",
    ),
    (
        "    public boolean loadDataVersion() {",
        "    /** 从 {@link TableId#TOPIC} 表读取并应用数据版本。 */\n    public boolean loadDataVersion() {",
    ),
    (
        "    private boolean loadTopicConfig() {",
        "    /** 扫描 Topic 表前缀范围内全部 KV 并加载到内存。 */\n    private boolean loadTopicConfig() {",
    ),
    (
        "    /**\n     * Key layout: [table-prefix, 1 byte][table-id, 2 bytes][record-type-prefix, 1 byte][topic-len, 2 bytes][topic-bytes]\n     * Value layout: [serialization-type, 1 byte][topic-config-bytes]\n     *\n     * @param key   Topic config key representation in RocksDB\n     * @param value Topic config value representation in RocksDB\n     * @return decoded topic config\n     */",
        "    /**\n     * 将 RocksDB 键值对解码为 {@link TopicConfig}。\n     * <p>\n     * 键布局：[table-prefix, 1 byte][table-id, 2 bytes][record-type-prefix, 1 byte][topic-len, 2 bytes][topic-bytes]\n     * 值布局：[serialization-type, 1 byte][topic-config-bytes]\n     *\n     * @param key   RocksDB 中的 Topic 配置键\n     * @param value RocksDB 中的 Topic 配置值\n     * @return 解码后的 Topic 配置，格式不支持时返回 null\n     */",
    ),
    (
        "    @Override\n    public synchronized void persist() {",
        "    /** 强制刷 WAL 到磁盘。 */\n    @Override\n    public synchronized void persist() {",
    ),
    (
        "    @Override\n    public TopicConfig selectTopicConfig(final String topic) {",
        "    /** 查询 Topic 配置；LMQ Topic 返回简化默认配置。 */\n    @Override\n    public TopicConfig selectTopicConfig(final String topic) {",
    ),
    (
        "    @Override\n    public void updateTopicConfig(final TopicConfig topicConfig) {",
        "    /** 更新内存并写入 RocksDB，同时戳记数据版本。 */\n    @Override\n    public void updateTopicConfig(final TopicConfig topicConfig) {",
    ),
    (
        "    @Override\n    protected TopicConfig removeTopicConfig(String topicName) {",
        "    /** 从 RocksDB 删除 Topic 键并更新内存。 */\n    @Override\n    protected TopicConfig removeTopicConfig(String topicName) {",
    ),
    (
        "    @Override\n    public boolean containsTopic(String topic) {",
        "    /** LMQ Topic 恒视为存在。 */\n    @Override\n    public boolean containsTopic(String topic) {",
    ),
    (
        "    private TopicConfig simpleLmqTopicConfig(String topic) {",
        "    /** 为 LMQ Topic 构造最小读写权限配置。 */\n    private TopicConfig simpleLmqTopicConfig(String topic) {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/dledger/DLedgerRoleChangeHandler.java"] = [
    (
        "public class DLedgerRoleChangeHandler implements DLedgerLeaderElector.RoleChangeHandler {",
        "/**\n * DLedger 角色变更处理器：在选举结果变化时将 broker 切换为主/从并协调同步任务。\n */\npublic class DLedgerRoleChangeHandler implements DLedgerLeaderElector.RoleChangeHandler {",
    ),
    (
        "    public DLedgerRoleChangeHandler(BrokerController brokerController, DefaultMessageStore messageStore) {",
        "    /** 绑定 broker 与消息存储，并创建单线程执行器处理角色切换。 */\n    public DLedgerRoleChangeHandler(BrokerController brokerController, DefaultMessageStore messageStore) {",
    ),
    (
        "    @Override\n    public void handle(long term, MemberState.Role role) {",
        "    /** 异步处理 CANDIDATE/FOLLOWER/LEADER 角色变更逻辑。 */\n    @Override\n    public void handle(long term, MemberState.Role role) {",
    ),
    (
        "    private void handleSlaveSynchronize(BrokerRole role) {",
        "    /** 从节点时启动定时全量/Checkpoint 同步；主节点时取消同步任务。 */\n    private void handleSlaveSynchronize(BrokerRole role) {",
    ),
    (
        "    public void changeToSlave(int brokerId) {",
        "    /** 切换为从 broker：更新 ID/角色、关闭特殊服务并重新注册。 */\n    public void changeToSlave(int brokerId) {",
    ),
    (
        "    public void changeToMaster(BrokerRole role) {",
        "    /** 切换为主 broker：恢复服务、设置 brokerId=0 并重新注册。 */\n    public void changeToMaster(BrokerRole role) {",
    ),
    (
        "    @Override\n    public void startup() {",
        "    /** 启动钩子（当前无额外逻辑）。 */\n    @Override\n    public void startup() {",
    ),
    (
        "    @Override\n    public void shutdown() {",
        "    /** 关闭角色变更执行器。 */\n    @Override\n    public void shutdown() {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/failover/EscapeBridge.java"] = [
    (
        "public class EscapeBridge {",
        "/**\n * 故障逃逸桥：主不可用时将写/读请求转发至远程 broker，\n * 支持从节点代主（slave acting master）与远程逃逸模式。\n */\npublic class EscapeBridge {",
    ),
    (
        "    public EscapeBridge(BrokerController brokerController) {",
        "    /** 初始化内部生产者/消费者组名前缀。 */\n    public EscapeBridge(BrokerController brokerController) {",
    ),
    (
        "    public void start() throws Exception {",
        "    /** 启用远程逃逸时创建异步发送线程池。 */\n    public void start() throws Exception {",
    ),
    (
        "    public void shutdown() {",
        "    /** 关闭异步发送线程池。 */\n    public void shutdown() {",
    ),
    (
        "    public PutMessageResult putMessage(MessageExtBrokerInner messageExt) {",
        "    /** 优先写本地主；否则远程逃逸发送并转换结果。 */\n    public PutMessageResult putMessage(MessageExtBrokerInner messageExt) {",
    ),
    (
        "    public SendResult putMessageToRemoteBroker(MessageExtBrokerInner messageExt, String brokerNameToSend) {",
        "    /** 按路由选择目标 broker 并同步发送消息（含半事务消息转换）。 */\n    public SendResult putMessageToRemoteBroker(MessageExtBrokerInner messageExt, String brokerNameToSend) {",
    ),
    (
        "    public CompletableFuture<PutMessageResult> asyncPutMessage(MessageExtBrokerInner messageExt) {",
        "    /** 异步写入：本地主可用则本地写，否则异步远程发送。 */\n    public CompletableFuture<PutMessageResult> asyncPutMessage(MessageExtBrokerInner messageExt) {",
    ),
    (
        "    private String getProducerGroup(MessageExtBrokerInner messageExt) {",
        "    /** 从消息属性读取生产者组，缺省使用内部组名。 */\n    private String getProducerGroup(MessageExtBrokerInner messageExt) {",
    ),
    (
        "    public PutMessageResult putMessageToSpecificQueue(MessageExtBrokerInner messageExt) {",
        "    /** 写入指定队列：本地主或阻塞等待远程异步结果。 */\n    public PutMessageResult putMessageToSpecificQueue(MessageExtBrokerInner messageExt) {",
    ),
    (
        "    public CompletableFuture<PutMessageResult> asyncPutMessageToSpecificQueue(MessageExtBrokerInner messageExt) {",
        "    /** 异步写入指定队列。 */\n    public CompletableFuture<PutMessageResult> asyncPutMessageToSpecificQueue(MessageExtBrokerInner messageExt) {",
    ),
    (
        "    public CompletableFuture<PutMessageResult> asyncRemotePutMessageToSpecificQueue(MessageExtBrokerInner messageExt) {",
        "    /** 按 Topic+StoreHost 哈希选队列并异步远程发送。 */\n    public CompletableFuture<PutMessageResult> asyncRemotePutMessageToSpecificQueue(MessageExtBrokerInner messageExt) {",
    ),
    (
        "    private PutMessageResult transformSendResult2PutResult(SendResult sendResult) {",
        "    /** 将客户端 {@link SendResult} 映射为存储层 {@link PutMessageResult}。 */\n    private PutMessageResult transformSendResult2PutResult(SendResult sendResult) {",
    ),
    (
        "    public Triple<MessageExt, String, Boolean> getMessage(String topic, long offset, int queueId, String brokerName,\n        boolean deCompressBody) {",
        "    /** 同步拉取单条消息（本地或远程）。 */\n    public Triple<MessageExt, String, Boolean> getMessage(String topic, long offset, int queueId, String brokerName,\n        boolean deCompressBody) {",
    ),
    (
        "    // Triple<MessageExt, info, needRetry>, check info and retry if and only if MessageExt is null",
        "    // Triple<MessageExt, 说明, needRetry>：仅当 MessageExt 为 null 时根据 needRetry 决定是否重试",
    ),
    (
        "    public CompletableFuture<Triple<MessageExt, String, Boolean>> getMessageAsync(String topic, long offset,\n        int queueId, String brokerName, boolean deCompressBody) {",
        "    /** 异步拉取：优先本地 {@link MessageStore}，否则远程 Pull。 */\n    public CompletableFuture<Triple<MessageExt, String, Boolean>> getMessageAsync(String topic, long offset,\n        int queueId, String brokerName, boolean deCompressBody) {",
    ),
    (
        "    protected List<MessageExt> decodeMsgList(GetMessageResult getMessageResult, boolean deCompressBody) {",
        "    /** 解码 {@link GetMessageResult} 缓冲区列表为 {@link MessageExt} 列表。 */\n    protected List<MessageExt> decodeMsgList(GetMessageResult getMessageResult, boolean deCompressBody) {",
    ),
    (
        "    protected Triple<MessageExt, String, Boolean> getMessageFromRemote(String topic, long offset, int queueId,\n        String brokerName) {",
        "    /** 同步从远程 broker Pull 单条消息。 */\n    protected Triple<MessageExt, String, Boolean> getMessageFromRemote(String topic, long offset, int queueId,\n        String brokerName) {",
    ),
    (
        "    protected CompletableFuture<Triple<MessageExt, String, Boolean>> getMessageFromRemoteAsync(String topic,\n        long offset, int queueId, String brokerName) {",
        "    /** 异步从远程 broker Pull；地址缺失时刷新路由后重试。 */\n    protected CompletableFuture<Triple<MessageExt, String, Boolean>> getMessageFromRemoteAsync(String topic,\n        long offset, int queueId, String brokerName) {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/filter/CommitLogDispatcherCalcBitMap.java"] = [
    (
        "/**\n * Calculate bit map of filter.\n */",
        "/**\n * CommitLog 分发器：在消息落盘时为各消费者订阅表达式预计算 Bloom 位图，\n * 供 Pull 阶段快速过滤。\n */",
    ),
    (
        "    public CommitLogDispatcherCalcBitMap(BrokerConfig brokerConfig, ConsumerFilterManager consumerFilterManager) {",
        "    /** 注入 broker 配置与消费者过滤器管理器。 */\n    public CommitLogDispatcherCalcBitMap(BrokerConfig brokerConfig, ConsumerFilterManager consumerFilterManager) {",
    ),
    (
        "    @Override\n    public void dispatch(DispatchRequest request) {",
        "    /** 遍历 Topic 下全部 {@link ConsumerFilterData}，求值表达式并写入位图。 */\n    @Override\n    public void dispatch(DispatchRequest request) {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/filter/ConsumerFilterData.java"] = [
    (
        "/**\n * Filter data of consumer.\n */",
        "/**\n * 消费者过滤元数据：保存订阅表达式、编译结果、Bloom 数据及生命周期时间戳。\n */",
    ),
    (
        "    public boolean isDead() {",
        "    /** 判断过滤器是否已失效（deadTime >= bornTime）。 */\n    public boolean isDead() {",
    ),
    (
        "    public long howLongAfterDeath() {",
        "    /** 返回失效后经过的毫秒数，仍有效时返回 -1。 */\n    public long howLongAfterDeath() {",
    ),
    (
        "    /**\n     * Check this filter data has been used to calculate bit map when msg was stored in server.\n     */",
        "    /** 判断消息存储时间是否晚于过滤器创建时间（即是否应参与位图计算）。 */",
    ),
    (
        "    public String getConsumerGroup() {",
        "    /** 返回消费者组名。 */\n    public String getConsumerGroup() {",
    ),
    (
        "    public void setConsumerGroup(final String consumerGroup) {",
        "    /** 设置消费者组名。 */\n    public void setConsumerGroup(final String consumerGroup) {",
    ),
    (
        "    public String getTopic() {",
        "    /** 返回订阅 Topic。 */\n    public String getTopic() {",
    ),
    (
        "    public void setTopic(final String topic) {",
        "    /** 设置订阅 Topic。 */\n    public void setTopic(final String topic) {",
    ),
    (
        "    public String getExpression() {",
        "    /** 返回原始过滤表达式字符串。 */\n    public String getExpression() {",
    ),
    (
        "    public void setExpression(final String expression) {",
        "    /** 设置过滤表达式。 */\n    public void setExpression(final String expression) {",
    ),
    (
        "    public String getExpressionType() {",
        "    /** 返回表达式类型（如 TAG、SQL92）。 */\n    public String getExpressionType() {",
    ),
    (
        "    public void setExpressionType(final String expressionType) {",
        "    /** 设置表达式类型。 */\n    public void setExpressionType(final String expressionType) {",
    ),
    (
        "    public Expression getCompiledExpression() {",
        "    /** 返回已编译的 {@link Expression}。 */\n    public Expression getCompiledExpression() {",
    ),
    (
        "    public void setCompiledExpression(final Expression compiledExpression) {",
        "    /** 设置编译后的表达式对象。 */\n    public void setCompiledExpression(final Expression compiledExpression) {",
    ),
    (
        "    public long getBornTime() {",
        "    /** 返回过滤器注册时间戳。 */\n    public long getBornTime() {",
    ),
    (
        "    public void setBornTime(final long bornTime) {",
        "    /** 设置注册时间戳。 */\n    public void setBornTime(final long bornTime) {",
    ),
    (
        "    public long getDeadTime() {",
        "    /** 返回失效时间戳（0 表示仍有效）。 */\n    public long getDeadTime() {",
    ),
    (
        "    public void setDeadTime(final long deadTime) {",
        "    /** 设置失效时间戳。 */\n    public void setDeadTime(final long deadTime) {",
    ),
    (
        "    public BloomFilterData getBloomFilterData() {",
        "    /** 返回关联的 Bloom 过滤器数据。 */\n    public BloomFilterData getBloomFilterData() {",
    ),
    (
        "    public void setBloomFilterData(final BloomFilterData bloomFilterData) {",
        "    /** 设置 Bloom 过滤器数据。 */\n    public void setBloomFilterData(final BloomFilterData bloomFilterData) {",
    ),
    (
        "    public long getClientVersion() {",
        "    /** 返回客户端版本号。 */\n    public long getClientVersion() {",
    ),
    (
        "    public void setClientVersion(long clientVersion) {",
        "    /** 设置客户端版本号。 */\n    public void setClientVersion(long clientVersion) {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/filter/ExpressionForRetryMessageFilter.java"] = [
    (
        "/**\n * Support filter to retry topic.\n * <br>It will decode properties first in order to get real topic.\n */",
        "/**\n * 重试 Topic 专用表达式过滤器：先解码消息属性以获取原始 Topic，\n * 再使用对应消费者组的 {@link ConsumerFilterData} 求值。\n */",
    ),
    (
        "    public ExpressionForRetryMessageFilter(SubscriptionData subscriptionData, ConsumerFilterData consumerFilterData,\n        ConsumerFilterManager consumerFilterManager) {",
        "    /** 构造重试消息过滤器，继承 {@link ExpressionMessageFilter} 行为。 */\n    public ExpressionForRetryMessageFilter(SubscriptionData subscriptionData, ConsumerFilterData consumerFilterData,\n        ConsumerFilterManager consumerFilterManager) {",
    ),
    (
        "    @Override\n    public boolean isMatchedByCommitLog(ByteBuffer msgBuffer, Map<String, String> properties) {",
        "    /** 对重试 Topic 解析真实 Topic 与消费组后再执行表达式匹配。 */\n    @Override\n    public boolean isMatchedByCommitLog(ByteBuffer msgBuffer, Map<String, String> properties) {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/filter/ExpressionMessageFilter.java"] = [
    (
        "public class ExpressionMessageFilter implements MessageFilter {",
        "/**\n * 基于 SQL92/属性表达式的消息过滤器：ConsumeQueue 阶段用 Bloom 位图，\n * CommitLog 阶段直接对消息属性求值。\n */\npublic class ExpressionMessageFilter implements MessageFilter {",
    ),
    (
        "    public ExpressionMessageFilter(SubscriptionData subscriptionData, ConsumerFilterData consumerFilterData,\n        ConsumerFilterManager consumerFilterManager) {",
        "    /** 绑定订阅数据、消费者过滤元数据与管理器，并校验 Bloom 数据有效性。 */\n    public ExpressionMessageFilter(SubscriptionData subscriptionData, ConsumerFilterData consumerFilterData,\n        ConsumerFilterManager consumerFilterManager) {",
    ),
    (
        "    @Override\n    public boolean isMatchedByConsumeQueue(Long tagsCode, ConsumeQueueExt.CqExtUnit cqExtUnit) {",
        "    /** TAG 模式按 tagsCode 匹配；表达式模式用预计算位图做 Bloom 命中判断。 */\n    @Override\n    public boolean isMatchedByConsumeQueue(Long tagsCode, ConsumeQueueExt.CqExtUnit cqExtUnit) {",
    ),
    (
        "    @Override\n    public boolean isMatchedByCommitLog(ByteBuffer msgBuffer, Map<String, String> properties) {",
        "    /** 解码消息属性并对编译表达式求值，非布尔或 null 视为不匹配。 */\n    @Override\n    public boolean isMatchedByCommitLog(ByteBuffer msgBuffer, Map<String, String> properties) {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/filter/MessageEvaluationContext.java"] = [
    (
        "/**\n * Evaluation context from message.\n */",
        "/**\n * 消息属性求值上下文：将用户属性 Map 暴露给 {@link org.apache.rocketmq.filter.expression.Expression} 引擎。\n */",
    ),
    (
        "    public MessageEvaluationContext(Map<String, String> properties) {",
        "    /** 使用消息用户属性构造上下文。 */\n    public MessageEvaluationContext(Map<String, String> properties) {",
    ),
    (
        "    @Override\n    public Object get(final String name) {",
        "    /** 按属性名读取值，无属性表时返回 null。 */\n    @Override\n    public Object get(final String name) {",
    ),
    (
        "    @Override\n    public Map<String, Object> keyValues() {",
        "    /** 返回属性键值副本供表达式遍历。 */\n    @Override\n    public Map<String, Object> keyValues() {",
    ),
]

R["broker/src/main/java/org/apache/rocketmq/broker/latency/BrokerFastFailure.java"] = [
    (
        "/**\n * BrokerFastFailure will cover {@link BrokerController#getSendThreadPoolQueue()} and {@link\n * BrokerController#getPullThreadPoolQueue()}\n */",
        "/**\n * Broker 快速失败：监控各 Remoting 线程池队列，在 OS 页缓存繁忙或\n * 排队超时时主动拒绝请求，覆盖 {@link BrokerController#getSendThreadPoolQueue()}、\n * {@link BrokerController#getPullThreadPoolQueue()} 等队列。\n */",
    ),
    (
        "    public BrokerFastFailure(final BrokerController brokerController) {",
        "    /** 注册待清理队列并创建定时调度器。 */\n    public BrokerFastFailure(final BrokerController brokerController) {",
    ),
    (
        "    private void initCleanExpiredRequestQueueList() {",
        "    /** 初始化 Send/Pull/LitePull/Heartbeat/事务/Ack/Admin 等队列及超时阈值。 */\n    private void initCleanExpiredRequestQueueList() {",
    ),
    (
        "    public static RequestTask castRunnable(final Runnable runnable) {",
        "    /** 从 {@link FutureTaskExt} 包装中提取 {@link RequestTask}。 */\n    public static RequestTask castRunnable(final Runnable runnable) {",
    ),
    (
        "    public void start() {",
        "    /** 每 10ms 检查是否启用快速失败并清理过期请求。 */\n    public void start() {",
    ),
    (
        "    private void cleanExpiredRequest() {",
        "    /** 页缓存繁忙时优先丢弃 Send 队列任务，再逐队列清理超时任务。 */\n    private void cleanExpiredRequest() {",
    ),
    (
        "    void cleanExpiredRequestInQueue(final BlockingQueue<Runnable> blockingQueue, final long maxWaitTimeMillsInQueue) {",
        "    /** 从队首移除等待超过阈值的 {@link RequestTask} 并返回 SYSTEM_BUSY。 */\n    void cleanExpiredRequestInQueue(final BlockingQueue<Runnable> blockingQueue, final long maxWaitTimeMillsInQueue) {",
    ),
    (
        "    public synchronized void addCleanExpiredRequestQueue(BlockingQueue<Runnable> cleanExpiredRequestQueue,\n        Supplier<Long> maxWaitTimeMillsInQueueSupplier) {",
        "    /** 动态注册额外待清理队列及超时供应函数。 */\n    public synchronized void addCleanExpiredRequestQueue(BlockingQueue<Runnable> cleanExpiredRequestQueue,\n        Supplier<Long> maxWaitTimeMillsInQueueSupplier) {",
    ),
    (
        "    public void shutdown() {",
        "    /** 关闭定时调度器。 */\n    public void shutdown() {",
    ),
]
