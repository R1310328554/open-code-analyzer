"""Chinese JavaDoc replacements for RocketMQ wave40a store/plugin/pop/queue [0:15]."""

R: dict[str, list[tuple[str, str]]] = {
    "store/src/main/java/org/apache/rocketmq/store/plugin/MessageStorePluginContext.java": [
        (
            "public class MessageStorePluginContext {",
            "/**\n * 消息存储插件上下文：向插件注入 Store 配置、统计、监听与 Broker 配置。\n */\npublic class MessageStorePluginContext {",
        ),
        (
            "    private MessageStoreConfig messageStoreConfig;",
            "    /** 消息存储配置。 */\n    private MessageStoreConfig messageStoreConfig;",
        ),
        (
            "    private BrokerStatsManager brokerStatsManager;",
            "    /** Broker 统计管理器。 */\n    private BrokerStatsManager brokerStatsManager;",
        ),
        (
            "    private MessageArrivingListener messageArrivingListener;",
            "    /** 消息到达监听器。 */\n    private MessageArrivingListener messageArrivingListener;",
        ),
        (
            "    private BrokerConfig brokerConfig;",
            "    /** Broker 运行时配置。 */\n    private BrokerConfig brokerConfig;",
        ),
        (
            "    private final Configuration configuration;",
            "    /** 远程配置中心句柄。 */\n    private final Configuration configuration;",
        ),
        (
            "    public MessageStoreConfig getMessageStoreConfig() {",
            "    /** 返回消息存储配置。 */\n    public MessageStoreConfig getMessageStoreConfig() {",
        ),
        (
            "    public BrokerStatsManager getBrokerStatsManager() {",
            "    /** 返回 Broker 统计管理器。 */\n    public BrokerStatsManager getBrokerStatsManager() {",
        ),
        (
            "    public MessageArrivingListener getMessageArrivingListener() {",
            "    /** 返回消息到达监听器。 */\n    public MessageArrivingListener getMessageArrivingListener() {",
        ),
        (
            "    public BrokerConfig getBrokerConfig() {",
            "    /** 返回 Broker 配置。 */\n    public BrokerConfig getBrokerConfig() {",
        ),
        (
            "    public void registerConfiguration(Object config) {",
            "    /** 将远程配置属性注入对象并注册到配置中心。 */\n    public void registerConfiguration(Object config) {",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/pop/AckMsg.java": [
        (
            "public class AckMsg {",
            "/**\n * Pop 消费确认消息：记录 ack 偏移、主题、队列及 pop 时间等元数据。\n */\npublic class AckMsg {",
        ),
        (
            "    @JSONField(name = \"ao\", alternateNames = {\"ackOffset\"})\n    private long ackOffset;",
            "    /** 已确认的队列逻辑偏移。 */\n    @JSONField(name = \"ao\", alternateNames = {\"ackOffset\"})\n    private long ackOffset;",
        ),
        (
            "    @JSONField(name = \"so\", alternateNames = {\"startOffset\"})\n    private long startOffset;",
            "    /** 本次 pop 批次的起始偏移。 */\n    @JSONField(name = \"so\", alternateNames = {\"startOffset\"})\n    private long startOffset;",
        ),
        (
            "    @JSONField(name = \"c\", alternateNames = {\"consumerGroup\"})\n    private String consumerGroup;",
            "    /** 消费者组名。 */\n    @JSONField(name = \"c\", alternateNames = {\"consumerGroup\"})\n    private String consumerGroup;",
        ),
        (
            "    @JSONField(name = \"t\", alternateNames = {\"topic\"})\n    private String topic;",
            "    /** 主题名。 */\n    @JSONField(name = \"t\", alternateNames = {\"topic\"})\n    private String topic;",
        ),
        (
            "    @JSONField(name = \"q\", alternateNames = {\"queueId\"})\n    private int queueId;",
            "    /** 队列 ID。 */\n    @JSONField(name = \"q\", alternateNames = {\"queueId\"})\n    private int queueId;",
        ),
        (
            "    @JSONField(name = \"pt\", alternateNames = {\"popTime\"})\n    private long popTime;",
            "    /** Pop 操作发生的时间戳。 */\n    @JSONField(name = \"pt\", alternateNames = {\"popTime\"})\n    private long popTime;",
        ),
        (
            "    @JSONField(name = \"bn\", alternateNames = {\"brokerName\"})\n    private String brokerName;",
            "    /** 处理 pop 的 Broker 名称。 */\n    @JSONField(name = \"bn\", alternateNames = {\"brokerName\"})\n    private String brokerName;",
        ),
        (
            "    public long getPopTime() {",
            "    /** 返回 pop 时间戳。 */\n    public long getPopTime() {",
        ),
        (
            "    public long getAckOffset() {",
            "    /** 返回 ack 偏移。 */\n    public long getAckOffset() {",
        ),
        (
            "    public String getConsumerGroup() {",
            "    /** 返回消费者组名。 */\n    public String getConsumerGroup() {",
        ),
        (
            "    public long getStartOffset() {",
            "    /** 返回起始偏移。 */\n    public long getStartOffset() {",
        ),
        (
            "    public String getBrokerName() {",
            "    /** 返回 Broker 名称。 */\n    public String getBrokerName() {",
        ),
        (
            "    @Override\n    public String toString() {",
            "    /** 返回包含各字段的可读字符串。 */\n    @Override\n    public String toString() {",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/pop/BatchAckMsg.java": [
        (
            "public class BatchAckMsg extends AckMsg {",
            "/**\n * 批量 Pop 确认消息：在 {@link AckMsg} 基础上携带多条 ack 偏移列表。\n */\npublic class BatchAckMsg extends AckMsg {",
        ),
        (
            "    @JSONField(name = \"aol\", alternateNames = {\"ackOffsetList\"})\n    private List<Long> ackOffsetList = new ArrayList(32);",
            "    /** 批量 ack 的队列偏移列表。 */\n    @JSONField(name = \"aol\", alternateNames = {\"ackOffsetList\"})\n    private List<Long> ackOffsetList = new ArrayList(32);",
        ),
        (
            "    public List<Long> getAckOffsetList() {",
            "    /** 返回 ack 偏移列表。 */\n    public List<Long> getAckOffsetList() {",
        ),
        (
            "    public void setAckOffsetList(List<Long> ackOffsetList) {",
            "    /** 设置 ack 偏移列表。 */\n    public void setAckOffsetList(List<Long> ackOffsetList) {",
        ),
        (
            "    @Override\n    public String toString() {",
            "    /** 返回包含批量 ack 字段的可读字符串。 */\n    @Override\n    public String toString() {",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/pop/PopCheckPoint.java": [
        (
            "public class PopCheckPoint implements Comparable<PopCheckPoint> {",
            "/**\n * Pop 消费检查点：跟踪 invisible 窗口、位图 ack 状态及 revive 偏移。\n */\npublic class PopCheckPoint implements Comparable<PopCheckPoint> {",
        ),
        (
            "    @JSONField(name = \"so\")\n    private long startOffset;",
            "    /** 检查点覆盖的起始队列偏移。 */\n    @JSONField(name = \"so\")\n    private long startOffset;",
        ),
        (
            "    @JSONField(name = \"pt\")\n    private long popTime;",
            "    /** Pop 操作时间戳。 */\n    @JSONField(name = \"pt\")\n    private long popTime;",
        ),
        (
            "    @JSONField(name = \"it\")\n    private long invisibleTime;",
            "    /** 消息不可见时长（毫秒）。 */\n    @JSONField(name = \"it\")\n    private long invisibleTime;",
        ),
        (
            "    @JSONField(name = \"bm\")\n    private int bitMap;",
            "    /** 位图：标记各偏移是否已 ack。 */\n    @JSONField(name = \"bm\")\n    private int bitMap;",
        ),
        (
            "    @JSONField(name = \"n\")\n    private byte num;",
            "    /** 本检查点覆盖的消息条数。 */\n    @JSONField(name = \"n\")\n    private byte num;",
        ),
        (
            "    @JSONField(name = \"q\")\n    private int queueId;",
            "    /** 队列 ID。 */\n    @JSONField(name = \"q\")\n    private int queueId;",
        ),
        (
            "    @JSONField(name = \"t\")\n    private String topic;",
            "    /** 主题名。 */\n    @JSONField(name = \"t\")\n    private String topic;",
        ),
        (
            "    private String cid;",
            "    /** 消费者标识（consumer id）。 */\n    private String cid;",
        ),
        (
            "    @JSONField(name = \"ro\")\n    private long reviveOffset;",
            "    /** Revive 队列中的偏移位置。 */\n    @JSONField(name = \"ro\")\n    private long reviveOffset;",
        ),
        (
            "    @JSONField(name = \"d\")\n    private List<Integer> queueOffsetDiff;",
            "    /** 新版检查点：相对 startOffset 的偏移差值列表。 */\n    @JSONField(name = \"d\")\n    private List<Integer> queueOffsetDiff;",
        ),
        (
            "    @JSONField(name = \"bn\")\n    String brokerName;",
            "    /** Broker 名称。 */\n    @JSONField(name = \"bn\")\n    String brokerName;",
        ),
        (
            "    @JSONField(name = \"rp\")\n    String rePutTimes; // ck rePut times",
            "    /** 检查点重新投递次数。 */\n    @JSONField(name = \"rp\")\n    String rePutTimes; // ck rePut times",
        ),
        (
            "    @JSONField(name = \"sp\")\n    private boolean suspend; // nack without inc reconsume times, false default.",
            "    /** 是否挂起（nack 时不增加重试次数，默认 false）。 */\n    @JSONField(name = \"sp\")\n    private boolean suspend; // nack without inc reconsume times, false default.",
        ),
        (
            "    public long getReviveTime() {",
            "    /** 返回 revive 触发时间（popTime + invisibleTime）。 */\n    public long getReviveTime() {",
        ),
        (
            "    public void addDiff(int diff) {",
            "    /** 追加一条相对 startOffset 的偏移差值。 */\n    public void addDiff(int diff) {",
        ),
        (
            "    public int indexOfAck(long ackOffset) {",
            "    /** 根据 ack 偏移查找在位图/差值列表中的索引，未找到返回 -1。 */\n    public int indexOfAck(long ackOffset) {",
        ),
        (
            "        // old version of checkpoint",
            "        // 旧版检查点：按连续偏移计算索引",
        ),
        (
            "        // new version of checkpoint",
            "        // 新版检查点：在 queueOffsetDiff 中查找",
        ),
        (
            "    public long ackOffsetByIndex(byte index) {",
            "    /** 根据索引反查对应的 ack 队列偏移。 */\n    public long ackOffsetByIndex(byte index) {",
        ),
        (
            "    public int parseRePutTimes() {",
            "    /** 解析 rePutTimes 字符串为整数，失败时返回 Byte.MAX_VALUE。 */\n    public int parseRePutTimes() {",
        ),
        (
            "    @Override\n    public int compareTo(PopCheckPoint o) {",
            "    /** 按 startOffset 升序比较两个检查点。 */\n    @Override\n    public int compareTo(PopCheckPoint o) {",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/queue/AbstractConsumeQueueStore.java": [
        (
            "public abstract class AbstractConsumeQueueStore implements ConsumeQueueStoreInterface {",
            "/**\n * 消费队列存储抽象基类：维护 topic-queue 表与队列偏移操作器。\n */\npublic abstract class AbstractConsumeQueueStore implements ConsumeQueueStoreInterface {",
        ),
        (
            "    protected final DefaultMessageStore messageStore;",
            "    /** 所属 DefaultMessageStore 实例。 */\n    protected final DefaultMessageStore messageStore;",
        ),
        (
            "    protected final MessageStoreConfig messageStoreConfig;",
            "    /** 消息存储配置。 */\n    protected final MessageStoreConfig messageStoreConfig;",
        ),
        (
            "    protected final QueueOffsetOperator queueOffsetOperator = new QueueOffsetOperator();",
            "    /** 队列逻辑偏移操作器。 */\n    protected final QueueOffsetOperator queueOffsetOperator = new QueueOffsetOperator();",
        ),
        (
            "    protected final ConcurrentMap<String/* topic */, ConcurrentMap<Integer/* queueId */, ConsumeQueueInterface>> consumeQueueTable;",
            "    /** topic → queueId → 消费队列 的二级映射表。 */\n    protected final ConcurrentMap<String/* topic */, ConcurrentMap<Integer/* queueId */, ConsumeQueueInterface>> consumeQueueTable;",
        ),
        (
            "    public AbstractConsumeQueueStore(DefaultMessageStore messageStore) {",
            "    /** 绑定 MessageStore，LMQ 模式下预分配更大哈希表。 */\n    public AbstractConsumeQueueStore(DefaultMessageStore messageStore) {",
        ),
        (
            "    public void putMessagePositionInfoWrapper(ConsumeQueueInterface consumeQueue, DispatchRequest request) {",
            "    /** 将 dispatch 请求写入指定消费队列。 */\n    public void putMessagePositionInfoWrapper(ConsumeQueueInterface consumeQueue, DispatchRequest request) {",
        ),
        (
            "    @Override\n    public Long getMaxOffset(String topic, int queueId) throws ConsumeQueueException {",
            "    /** 从偏移表查询 topic-queue 的当前最大逻辑偏移。 */\n    @Override\n    public Long getMaxOffset(String topic, int queueId) throws ConsumeQueueException {",
        ),
        (
            "    @Override\n    public void assignQueueOffset(MessageExtBrokerInner msg) throws RocksDBException {",
            "    /** 为消息分配队列逻辑偏移（find-or-create 消费队列）。 */\n    @Override\n    public void assignQueueOffset(MessageExtBrokerInner msg) throws RocksDBException {",
        ),
        (
            "    @Override\n    public void increaseQueueOffset(MessageExtBrokerInner msg, short messageNum) {",
            "    /** 按消息条数递增队列逻辑偏移。 */\n    @Override\n    public void increaseQueueOffset(MessageExtBrokerInner msg, short messageNum) {",
        ),
        (
            "    public long getStoreTime(CqUnit cqUnit) {",
            "    /** 根据 CqUnit 物理位置从 CommitLog 提取存储时间戳。 */\n    public long getStoreTime(CqUnit cqUnit) {",
        ),
        (
            "    /**\n     * get max physic offset in consumeQueue\n     *\n     * @return the max physic offset in consumeQueue\n     * @throws RocksDBException only in rocksdb mode\n     */",
            "    /**\n     * 获取消费队列中已 dispatch 的最大物理偏移。\n     *\n     * @return 消费队列最大物理偏移\n     * @throws RocksDBException 仅 RocksDB 模式可能抛出\n     */",
        ),
        (
            "    /**\n     * destroy the specific consumeQueue\n     *\n     * @param consumeQueue consumeQueue to be destroyed\n     * @throws RocksDBException only in rocksdb mode\n     */",
            "    /**\n     * 销毁指定消费队列并释放资源。\n     *\n     * @param consumeQueue 待销毁的消费队列\n     * @throws RocksDBException 仅 RocksDB 模式可能抛出\n     */",
        ),
        (
            "    @Override\n    public boolean deleteTopic(String topic) {",
            "    /** 删除主题下全部消费队列并清理偏移表项。 */\n    @Override\n    public boolean deleteTopic(String topic) {",
        ),
        (
            "        // remove topic from cq table",
            "        // 从消费队列表移除该 topic",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/queue/BatchOffsetIndex.java": [
        (
            "public class BatchOffsetIndex {",
            "/**\n * 批量消息偏移索引：映射 CommitLog 物理位置与批次大小。\n */\npublic class BatchOffsetIndex {",
        ),
        (
            "    private final MappedFile mappedFile;",
            "    /** 索引条目所在的映射文件。 */\n    private final MappedFile mappedFile;",
        ),
        (
            "    private final int indexPos;",
            "    /** 索引在映射文件内的字节偏移。 */\n    private final int indexPos;",
        ),
        (
            "    private final long msgOffset;",
            "    /** 消息在 CommitLog 中的物理偏移。 */\n    private final long msgOffset;",
        ),
        (
            "    private final short batchSize;",
            "    /** 本批次包含的消息条数。 */\n    private final short batchSize;",
        ),
        (
            "    private final long storeTimestamp;",
            "    /** 消息存储时间戳。 */\n    private final long storeTimestamp;",
        ),
        (
            "    public BatchOffsetIndex(MappedFile file, int pos, long msgOffset, short size, long storeTimestamp) {",
            "    /** 构造批量偏移索引条目。 */\n    public BatchOffsetIndex(MappedFile file, int pos, long msgOffset, short size, long storeTimestamp) {",
        ),
        (
            "    public MappedFile getMappedFile() {",
            "    /** 返回映射文件。 */\n    public MappedFile getMappedFile() {",
        ),
        (
            "    public int getIndexPos() {",
            "    /** 返回索引在文件内的位置。 */\n    public int getIndexPos() {",
        ),
        (
            "    public long getMsgOffset() {",
            "    /** 返回 CommitLog 物理偏移。 */\n    public long getMsgOffset() {",
        ),
        (
            "    public short getBatchSize() {",
            "    /** 返回批次大小。 */\n    public short getBatchSize() {",
        ),
        (
            "    public long getStoreTimestamp() {",
            "    /** 返回存储时间戳。 */\n    public long getStoreTimestamp() {",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/queue/ConsumeQueueInterface.java": [
        (
            "public interface ConsumeQueueInterface extends FileQueueLifeCycle {",
            "/**\n * 消费队列接口：定义索引遍历、偏移查询与 dispatch 写入契约。\n */\npublic interface ConsumeQueueInterface extends FileQueueLifeCycle {",
        ),
        (
            "    /**\n     * Get the topic name\n     * @return the topic this cq belongs to.\n     */",
            "    /** 返回本消费队列所属主题名。 */",
        ),
        (
            "    /**\n     * Get queue id\n     * @return the queue id this cq belongs to.\n     */",
            "    /** 返回本消费队列的 queueId。 */",
        ),
        (
            "    /**\n     * Get the units from the start offset.\n     *\n     * @param startIndex start index\n     * @return the unit iterateFrom\n     */",
            "    /**\n     * 从指定逻辑索引起迭代 CqUnit。\n     *\n     * @param startIndex 起始索引\n     * @return CqUnit 迭代器\n     */",
        ),
        (
            "    /**\n     * Get the units from the start offset.\n     *\n     * @param startIndex start index\n     * @param count the unit counts will be iterated\n     * @return the unit iterateFrom\n     * @throws RocksDBException only in rocksdb mode\n     */",
            "    /**\n     * 从指定索引起迭代至多 count 条 CqUnit。\n     *\n     * @param startIndex 起始索引\n     * @param count 最多迭代条数\n     * @return CqUnit 迭代器\n     * @throws RocksDBException 仅 RocksDB 模式可能抛出\n     */",
        ),
        (
            "    /**\n     * Get cq unit at specified index\n     * @param index index\n     * @return the cq unit at index\n     */",
            "    /** 按逻辑索引读取单条 CqUnit。 */",
        ),
        (
            "    /**\n     * Get earliest cq unit\n     * @return the cq unit and message storeTime at index\n     */",
            "    /** 返回指定索引处的 CqUnit 及对应存储时间。 */",
        ),
        (
            "    /**\n     * Get earliest cq unit\n     * @return earliest cq unit and message storeTime\n     */",
            "    /** 返回最早 CqUnit 及其存储时间。 */",
        ),
        (
            "    /**\n     * Get earliest cq unit\n     * @return earliest cq unit\n     */",
            "    /** 返回最早 CqUnit。 */",
        ),
        (
            "    /**\n     * Get last cq unit\n     * @return last cq unit\n     */",
            "    /** 返回最新 CqUnit。 */",
        ),
        (
            "    /**\n     * Get last commit log offset\n     * @return last commit log offset\n     */",
            "    /** 返回最后一条消息对应的 CommitLog 物理偏移。 */",
        ),
        (
            "    /**\n     * Get min offset(index) in queue\n     * @return the min offset(index) in queue\n     */",
            "    /** 返回队列最小逻辑偏移（索引）。 */",
        ),
        (
            "    /**\n     * Get max offset(index) in queue\n     * @return the max offset(index) in queue\n     */",
            "    /** 返回队列最大逻辑偏移（索引）。 */",
        ),
        (
            "    /**\n     * Get total message count\n     * @return total message count\n     */",
            "    /** 返回队列中消息总条数。 */",
        ),
        (
            "    /**\n     * Get the message whose timestamp is the smallest, greater than or equal to the given time.\n     * @param timestamp timestamp\n     * @return the offset(index)\n     */",
            "    /**\n     * 查找存储时间 ≥ 给定时间戳的最小消息对应的逻辑偏移。\n     *\n     * @param timestamp 目标时间戳\n     * @return 逻辑偏移（索引）\n     */",
        ),
        (
            "    /**\n     * Get the message whose timestamp is the smallest, greater than or equal to the given time and when there are more\n     * than one message satisfy the condition, decide which one to return based on boundaryType.\n     * @param timestamp    timestamp\n     * @param boundaryType Lower or Upper\n     * @return the offset(index)\n     */",
            "    /**\n     * 按时间戳查找逻辑偏移；多条满足时由 boundaryType 决定取 Lower 或 Upper。\n     *\n     * @param timestamp 目标时间戳\n     * @param boundaryType 边界类型（Lower/Upper）\n     * @return 逻辑偏移（索引）\n     */",
        ),
        (
            "    /**\n     * The max physical offset of commitlog has been dispatched to this queue.\n     * It should be exclusive.\n     *\n     * @return the max physical offset point to commitlog\n     */",
            "    /**\n     * 已 dispatch 到本队列的最大 CommitLog 物理偏移（不含该偏移）。\n     *\n     * @return 最大物理偏移\n     */",
        ),
        (
            "    /**\n     * Usually, the cq files are not exactly consistent with the commitlog, there maybe some redundant data in the first\n     * cq file.\n     *\n     * @return the minimal effective pos of the cq file.\n     */",
            "    /**\n     * 消费队列文件与 CommitLog 可能不完全对齐，首文件或有冗余数据。\n     *\n     * @return 消费队列文件中最小有效物理位置\n     */",
        ),
        (
            "    /**\n     * Get cq type\n     * @return cq type\n     */",
            "    /** 返回消费队列实现类型。 */",
        ),
        (
            "    /**\n     * Gets the occupied size of CQ file on disk\n     * @return total size\n     */",
            "    /** 返回消费队列在磁盘上占用的总字节数。 */",
        ),
        (
            "    /**\n     * Get the unit size of this CQ which is different in different CQ impl\n     * @return cq unit size\n     */",
            "    /** 返回单条 CqUnit 的字节大小（因实现而异）。 */",
        ),
        (
            "    /**\n     * Correct min offset by min commit log offset.\n     * @param minCommitLogOffset min commit log offset\n     */",
            "    /** 根据 CommitLog 最小物理偏移校正队列最小逻辑偏移。 */",
        ),
        (
            "    /**\n     * Do dispatch.\n     * @param request the request containing dispatch information.\n     */",
            "    /** 执行 dispatch，将消息位置信息写入消费队列。 */",
        ),
        (
            "    /**\n     * Assign queue offset.\n     * @param queueOffsetAssigner the delegated queue offset assigner\n     * @param msg message itself\n     * @throws RocksDBException only in rocksdb mode\n     */",
            "    /**\n     * 为消息分配队列逻辑偏移。\n     *\n     * @param queueOffsetAssigner 偏移分配器\n     * @param msg 待写入消息\n     * @throws RocksDBException 仅 RocksDB 模式可能抛出\n     */",
        ),
        (
            "    /**\n     * Increase queue offset.\n     * @param queueOffsetAssigner the delegated queue offset assigner\n     * @param msg message itself\n     * @param messageNum message number\n     */",
            "    /**\n     * 按消息条数递增队列逻辑偏移。\n     *\n     * @param queueOffsetAssigner 偏移分配器\n     * @param msg 消息体\n     * @param messageNum 消息条数\n     */",
        ),
        (
            "    /**\n     * Estimate number of records matching given filter.\n     *\n     * @param from Lower boundary, inclusive.\n     * @param to Upper boundary, inclusive.\n     * @param filter Specified filter criteria\n     * @return Number of matching records.\n     */",
            "    /**\n     * 估算指定区间内满足过滤条件的消息条数。\n     *\n     * @param from 起始索引（含）\n     * @param to 结束索引（含）\n     * @param filter 消息过滤器\n     * @return 匹配条数\n     */",
        ),
        (
            "    /**\n     * Initialize cq and set max offset and min offset to given offset\n     *\n     * @param offset       set max and min offset to given offset\n     * @param minPhyOffset min physical offset, used to correct min offset\n     */",
            "    /**\n     * 初始化消费队列，将最大/最小逻辑偏移设为给定值。\n     *\n     * @param offset 初始逻辑偏移\n     * @param minPhyOffset 最小物理偏移，用于校正 min offset\n     */",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/queue/ConsumeQueueStoreInterface.java": [
        (
            "public interface ConsumeQueueStoreInterface extends CommitLogDispatchStore {",
            "/**\n * 消费队列存储接口：管理全部 topic-queue 的生命周期与 dispatch。\n */\npublic interface ConsumeQueueStoreInterface extends CommitLogDispatchStore {",
        ),
        (
            "    /**\n     * Load from file.\n     *\n     * @return true if loaded successfully.\n     */",
            "    /** 从磁盘加载消费队列数据，成功返回 true。 */",
        ),
        (
            "    /**\n     * Recover from file.\n     * @param concurrently whether to recover concurrently\n     */",
            "    /**\n     * 从文件恢复消费队列索引。\n     *\n     * @param concurrently 是否并发恢复\n     */",
        ),
        (
            "    /**\n     * Start the consumeQueueStore\n     */",
            "    /** 启动消费队列存储服务。 */",
        ),
        (
            "    /**\n     * Shutdown the consumeQueueStore\n     * @return true if shutdown successfully.\n     */",
            "    /** 关闭消费队列存储，成功返回 true。 */",
        ),
        (
            "    /**\n     * destroy all consumeQueues\n     * @param loadAfterDestroy reload store after destroy, only used in RocksDB mode\n     */",
            "    /**\n     * 销毁全部消费队列。\n     *\n     * @param loadAfterDestroy 销毁后是否重新加载（仅 RocksDB 模式）\n     */",
        ),
        (
            "    /**\n     * delete topic\n     */",
            "    /** 删除指定主题的全部消费队列。 */",
        ),
        (
            "    /**\n     * Flush all nested consume queues to disk\n     *\n     * @throws StoreException if there is an error during flush\n     */",
            "    /**\n     * 将全部消费队列刷盘。\n     *\n     * @throws StoreException 刷盘失败时抛出\n     */",
        ),
        (
            "    /**\n     * clean expired data from minCommitLogOffset\n     * @param minCommitLogOffset Minimum commit log offset\n     */",
            "    /** 从 minCommitLogOffset 起清理过期消费队列数据。 */",
        ),
        (
            "    /**\n     * Check files.\n     */",
            "    /** 自检消费队列文件完整性。 */",
        ),
        (
            "    /**\n     * truncate dirty data\n     * @param offsetToTruncate\n     * @throws RocksDBException only in rocksdb mode\n     */",
            "    /**\n     * 截断脏数据。\n     *\n     * @param offsetToTruncate 截断边界偏移\n     * @throws RocksDBException 仅 RocksDB 模式可能抛出\n     */",
        ),
        (
            "    /**\n     * Apply the dispatched request. This function should be idempotent.\n     *\n     * @param request dispatch request\n     * @throws RocksDBException only in rocksdb mode will throw exception\n     */",
            "    /**\n     * 应用 dispatch 请求（幂等）。\n     *\n     * @param request dispatch 请求\n     * @throws RocksDBException 仅 RocksDB 模式可能抛出\n     */",
        ),
        (
            "    /**\n     * get consumeQueue table\n     * @return the consumeQueue table\n     */",
            "    /** 返回 topic → queueId → 消费队列 的映射表。 */",
        ),
        (
            "    /**\n     * Assign queue offset.\n     * @param msg message itself\n     * @throws RocksDBException only in rocksdb mode\n     */",
            "    /**\n     * 为消息分配队列逻辑偏移。\n     *\n     * @param msg 消息体\n     * @throws RocksDBException 仅 RocksDB 模式可能抛出\n     */",
        ),
        (
            "    /**\n     * Increase queue offset.\n     * @param msg message itself\n     * @param messageNum message number\n     */",
            "    /** 按 messageNum 递增队列逻辑偏移。 */",
        ),
        (
            "    /**\n     * Increase lmq offset\n     * @param topic Topic/Queue name\n     * @param queueId Queue ID\n     * @param delta amount to increase\n     */",
            "    /**\n     * 递增 LMQ 逻辑偏移。\n     *\n     * @param topic 主题或 LMQ 名\n     * @param queueId 队列 ID\n     * @param delta 递增量\n     */",
        ),
        (
            "    /**\n     * get lmq queue offset\n     * @param topic\n     * @param queueId\n     * @return\n     */",
            "    /**\n     * 查询 LMQ 当前逻辑偏移。\n     *\n     * @param topic 主题名\n     * @param queueId 队列 ID\n     * @return 当前偏移\n     */",
        ),
        (
            "    /**\n     * recover topicQueue table by minPhyOffset\n     * @param minPhyOffset\n     */",
            "    /** 根据 minPhyOffset 恢复 topicQueue 偏移表。 */",
        ),
        (
            "    /**\n     * get maxOffset of specific topic-queueId in topicQueue table\n     *\n     * @param topic Topic name\n     * @param queueId Queue identifier\n     * @return the max offset in QueueOffsetOperator\n     * @throws ConsumeQueueException if there is an error while retrieving max consume queue offset\n     */",
            "    /**\n     * 查询 topic-queue 在偏移表中的最大逻辑偏移。\n     *\n     * @param topic 主题名\n     * @param queueId 队列 ID\n     * @return QueueOffsetOperator 中的最大偏移\n     * @throws ConsumeQueueException 查询失败时抛出\n     */",
        ),
        (
            "    /**\n     * get min logic offset of specific topic-queueId in consumeQueue\n     * @param topic\n     * @param queueId\n     * @return the min logic offset of specific topic-queueId in consumeQueue\n     * @throws RocksDBException only in rocksdb mode\n     */",
            "    /**\n     * 查询指定 topic-queue 的最小逻辑偏移。\n     *\n     * @param topic 主题名\n     * @param queueId 队列 ID\n     * @return 最小逻辑偏移\n     * @throws RocksDBException 仅 RocksDB 模式可能抛出\n     */",
        ),
        (
            "    /**\n     * Get the message whose timestamp is the smallest, greater than or equal to the given time and when there are more\n     * than one message satisfy the condition, decide which one to return based on boundaryType.\n     * @param timestamp    timestamp\n     * @param boundaryType Lower or Upper\n     * @return the offset(index)\n     * @throws RocksDBException only in rocksdb mode\n     */",
            "    /**\n     * 按时间戳在指定 topic-queue 中查找逻辑偏移。\n     *\n     * @param timestamp 目标时间戳\n     * @param boundaryType 边界类型\n     * @return 逻辑偏移\n     * @throws RocksDBException 仅 RocksDB 模式可能抛出\n     */",
        ),
        (
            "    /**\n     * find or create the consumeQueue\n     * @param topic\n     * @param queueId\n     * @return the consumeQueue\n     */",
            "    /**\n     * 查找或创建消费队列。\n     *\n     * @param topic 主题名\n     * @param queueId 队列 ID\n     * @return 消费队列实例\n     */",
        ),
        (
            "    /**\n     * only find consumeQueue\n     *\n     * @param topic\n     * @param queueId\n     * @return the consumeQueue\n     */",
            "    /**\n     * 仅查找消费队列，不存在时不创建。\n     *\n     * @param topic 主题名\n     * @param queueId 队列 ID\n     * @return 消费队列实例或 null\n     */",
        ),
        (
            "    /**\n     * get the total size of all consumeQueue\n     * @return the total size of all consumeQueue\n     */",
            "    /** 返回全部消费队列占用的磁盘总字节数。 */",
        ),
        (
            "    /**\n     * get lmq consume queue count\n     * @return the count of lmq\n     */",
            "    /** 返回 LMQ 消费队列数量。 */",
        ),
        (
            "    /**\n     * Check if the LMQ exists, this is different from getConsumeQueue()\n     * @param lmqTopic\n     * @return exist or not\n     */",
            "    /**\n     * 判断 LMQ 是否存在（语义不同于 getConsumeQueue）。\n     *\n     * @param lmqTopic LMQ 主题名\n     * @return 是否存在\n     */",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/queue/CqUnit.java": [
        (
            "public class CqUnit {",
            "/**\n * 消费队列单元：映射逻辑偏移到 CommitLog 物理位置与标签码。\n */\npublic class CqUnit {",
        ),
        (
            "    private final long queueOffset;",
            "    /** 队列逻辑偏移。 */\n    private final long queueOffset;",
        ),
        (
            "    private final int size;",
            "    /** CommitLog 中消息体字节长度。 */\n    private final int size;",
        ),
        (
            "    private final long pos;",
            "    /** 消息在 CommitLog 中的物理偏移。 */\n    private final long pos;",
        ),
        (
            "    private final short batchNum;",
            "    /** 批次内消息条数。 */\n    private final short batchNum;",
        ),
        (
            "    /**\n     * Be careful, the tagsCode is reused as an address for extent file. To prevent accident mistake, we follow the\n     * rules: 1. If the cqExtUnit is not null, make tagsCode == cqExtUnit.getTagsCode() 2. If the cqExtUnit is null, and\n     * the tagsCode is smaller than 0, it is an invalid tagsCode, which means failed to get cqExtUnit by address\n     */",
            "    /**\n     * 注意：tagsCode 同时用作扩展文件地址。规则：1) cqExtUnit 非空时 tagsCode 须与其一致；\n     * 2) cqExtUnit 为空且 tagsCode &lt; 0 表示无效地址、扩展单元读取失败。\n     */",
        ),
        (
            "    private long tagsCode;",
            "    /** 标签哈希码或扩展文件地址。 */\n    private long tagsCode;",
        ),
        (
            "    private ConsumeQueueExt.CqExtUnit cqExtUnit;",
            "    /** 关联的扩展单元（大 tags 等场景）。 */\n    private ConsumeQueueExt.CqExtUnit cqExtUnit;",
        ),
        (
            "    private final ByteBuffer nativeBuffer;",
            "    /** 指向底层映射缓冲区的引用（用于就地修正）。 */\n    private final ByteBuffer nativeBuffer;",
        ),
        (
            "    private final int compactedOffset;",
            "    /** 压缩偏移在 nativeBuffer 中的位置。 */\n    private final int compactedOffset;",
        ),
        (
            "    public CqUnit(long queueOffset, long pos, int size, long tagsCode) {",
            "    /** 构造单条消息的 CqUnit（batchNum 默认为 1）。 */\n    public CqUnit(long queueOffset, long pos, int size, long tagsCode) {",
        ),
        (
            "    public Long getValidTagsCodeAsLong() {",
            "    /** 返回有效 tagsCode；扩展地址或无效值时返回 null。 */\n    public Long getValidTagsCodeAsLong() {",
        ),
        (
            "    public boolean isTagsCodeValid() {",
            "    /** 判断 tagsCode 是否为有效标签码（非扩展地址）。 */\n    public boolean isTagsCodeValid() {",
        ),
        (
            "    public void correctCompactOffset(int correctedOffset) {",
            "    /** 在 nativeBuffer 中修正压缩偏移字段。 */\n    public void correctCompactOffset(int correctedOffset) {",
        ),
        (
            "    @Override\n    public String toString() {",
            "    /** 返回包含各字段的可读字符串。 */\n    @Override\n    public String toString() {",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/queue/DispatchEntry.java": [
        (
            "/**\n * Use Record when Java 16 is available\n */\npublic class DispatchEntry {",
            "/**\n * Dispatch 条目：扁平化存储 dispatch 请求的关键字段（Java 16 后可改用 Record）。\n */\npublic class DispatchEntry {",
        ),
        (
            "    public byte[] topic;",
            "    /** 主题名 UTF-8 字节数组。 */\n    public byte[] topic;",
        ),
        (
            "    public int queueId;",
            "    /** 队列 ID。 */\n    public int queueId;",
        ),
        (
            "    public long queueOffset;",
            "    /** 队列逻辑偏移。 */\n    public long queueOffset;",
        ),
        (
            "    public long commitLogOffset;",
            "    /** CommitLog 物理偏移。 */\n    public long commitLogOffset;",
        ),
        (
            "    public int messageSize;",
            "    /** 消息体字节长度。 */\n    public int messageSize;",
        ),
        (
            "    public long tagCode;",
            "    /** 标签哈希码。 */\n    public long tagCode;",
        ),
        (
            "    public long storeTimestamp;",
            "    /** 消息存储时间戳。 */\n    public long storeTimestamp;",
        ),
        (
            "    public static DispatchEntry from(@Nonnull DispatchRequest request) {",
            "    /** 从 {@link DispatchRequest} 构建 DispatchEntry。 */\n    public static DispatchEntry from(@Nonnull DispatchRequest request) {",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/queue/FileQueueLifeCycle.java": [
        (
            "/**\n * FileQueueLifeCycle contains life cycle methods of ConsumerQueue that is directly implemented by FILE.\n */\npublic interface FileQueueLifeCycle extends Swappable {",
            "/**\n * 文件型消费队列生命周期接口：定义加载、恢复、刷盘与销毁等操作。\n */\npublic interface FileQueueLifeCycle extends Swappable {",
        ),
        (
            "    /**\n     * Load from file.\n     * @return true if loaded successfully.\n     */",
            "    /** 从磁盘加载队列文件，成功返回 true。 */",
        ),
        (
            "    /**\n     * Recover from file.\n     */",
            "    /** 从文件恢复队列索引与偏移。 */",
        ),
        (
            "    /**\n     * Check files.\n     */",
            "    /** 自检队列文件完整性。 */",
        ),
        (
            "    /**\n     * Flush cache to file.\n     * @param flushLeastPages  the minimum number of pages to be flushed\n     * @return true if any data has been flushed.\n     */",
            "    /**\n     * 将缓存刷入文件。\n     *\n     * @param flushLeastPages 至少刷盘的页数\n     * @return 是否有数据被刷盘\n     */",
        ),
        (
            "    /**\n     * Destroy files.\n     */",
            "    /** 销毁队列文件并释放资源。 */",
        ),
        (
            "    /**\n     * Truncate dirty logic files starting at max commit log position.\n     * @param maxCommitLogPos max commit log position\n     */",
            "    /** 从 maxCommitLogPos 起截断脏逻辑文件。 */",
        ),
        (
            "    /**\n     * Delete expired files ending at min commit log position.\n     * @param minCommitLogPos min commit log position\n     * @return deleted file numbers.\n     */",
            "    /**\n     * 删除 minCommitLogPos 之前的过期文件。\n     *\n     * @param minCommitLogPos CommitLog 最小物理位置\n     * @return 删除的文件数\n     */",
        ),
        (
            "    /**\n     * Roll to next file.\n     * @param nextBeginOffset next begin offset\n     * @return the beginning offset of the next file\n     */",
            "    /**\n     * 滚动到下一个队列文件。\n     *\n     * @param nextBeginOffset 下一文件起始逻辑偏移\n     * @return 下一文件的起始偏移\n     */",
        ),
        (
            "    /**\n     * Is the first file available?\n     * @return true if it's available\n     */",
            "    /** 首个队列文件是否可用。 */",
        ),
        (
            "    /**\n     * Does the first file exist?\n     *\n     * @return true if it exists\n     */",
            "    /** 首个队列文件是否存在。 */",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/queue/MultiDispatchUtils.java": [
        (
            "public class MultiDispatchUtils {",
            "/**\n * 多路 dispatch 工具：判断 LMQ/多队列场景下是否需要额外 dispatch。\n */\npublic class MultiDispatchUtils {",
        ),
        (
            "    public static String lmqQueueKey(String queueName) {",
            "    /** 构造 LMQ 队列键：queueName-0。 */\n    public static String lmqQueueKey(String queueName) {",
        ),
        (
            "    public static boolean isNeedHandleMultiDispatch(MessageStoreConfig messageStoreConfig, String topic) {",
            "    /** 判断是否需处理多路 dispatch（排除重试/系统/定时主题）。 */\n    public static boolean isNeedHandleMultiDispatch(MessageStoreConfig messageStoreConfig, String topic) {",
        ),
        (
            "    public static boolean checkMultiDispatchQueue(MessageStoreConfig messageStoreConfig, DispatchRequest dispatchRequest) {",
            "    /** 检查 dispatch 请求是否携带多队列属性且需写入文件型 CQ。 */\n    public static boolean checkMultiDispatchQueue(MessageStoreConfig messageStoreConfig, DispatchRequest dispatchRequest) {",
        ),
        (
            "            return false; // no need to dispatch file CQ here",
            "            return false; // 此处无需 dispatch 文件型 CQ",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/queue/OffsetInitializer.java": [
        (
            "public interface OffsetInitializer {",
            "/**\n * 偏移初始化器：在缓存未命中时从持久层加载 LMQ 最大偏移。\n */\npublic interface OffsetInitializer {",
        ),
        (
            "    long maxConsumeQueueOffset(String topic, int queueId) throws ConsumeQueueException;",
            "    /** 查询 topic-queue 的最大消费队列逻辑偏移。 */\n    long maxConsumeQueueOffset(String topic, int queueId) throws ConsumeQueueException;",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/queue/OffsetInitializerRocksDBImpl.java": [
        (
            "public class OffsetInitializerRocksDBImpl implements OffsetInitializer {",
            "/**\n * RocksDB 偏移初始化器：从 RocksDBConsumeQueueStore 查询 LMQ 最大偏移。\n */\npublic class OffsetInitializerRocksDBImpl implements OffsetInitializer {",
        ),
        (
            "    private final RocksDBConsumeQueueStore consumeQueueStore;",
            "    /** 底层 RocksDB 消费队列存储。 */\n    private final RocksDBConsumeQueueStore consumeQueueStore;",
        ),
        (
            "    public OffsetInitializerRocksDBImpl(RocksDBConsumeQueueStore consumeQueueStore) {",
            "    /** 绑定 RocksDB 消费队列存储实例。 */\n    public OffsetInitializerRocksDBImpl(RocksDBConsumeQueueStore consumeQueueStore) {",
        ),
        (
            "    @Override\n    public long maxConsumeQueueOffset(String topic, int queueId) throws ConsumeQueueException {",
            "    /** 从 RocksDB 查询 LMQ 最大逻辑偏移并记录日志。 */\n    @Override\n    public long maxConsumeQueueOffset(String topic, int queueId) throws ConsumeQueueException {",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/queue/QueueOffsetOperator.java": [
        (
            "/**\n * QueueOffsetOperator is a component for operating offsets for queues.\n */\npublic class QueueOffsetOperator {",
            "/**\n * 队列偏移操作器：维护普通/批量/LMQ 三类 topic-queue 逻辑偏移表。\n */\npublic class QueueOffsetOperator {",
        ),
        (
            "    private ConcurrentMap<String, Long> topicQueueTable = new ConcurrentHashMap<>(1024);",
            "    /** 普通 topic-queue 下一逻辑偏移表。 */\n    private ConcurrentMap<String, Long> topicQueueTable = new ConcurrentHashMap<>(1024);",
        ),
        (
            "    private ConcurrentMap<String, Long> batchTopicQueueTable = new ConcurrentHashMap<>(1024);",
            "    /** 批量 topic-queue 下一逻辑偏移表。 */\n    private ConcurrentMap<String, Long> batchTopicQueueTable = new ConcurrentHashMap<>(1024);",
        ),
        (
            "    /**\n     * {TOPIC}-{QUEUE_ID} --> NEXT Consume Queue Offset\n     */",
            "    /** {TOPIC}-{QUEUE_ID} → 下一消费队列逻辑偏移（LMQ 专用）。 */",
        ),
        (
            "    private ConcurrentMap<String/* topic-queue-id */, Long/* offset */> lmqTopicQueueTable = new ConcurrentHashMap<>(1024);",
            "    /** LMQ topic-queue 最大逻辑偏移缓存。 */\n    private ConcurrentMap<String/* topic-queue-id */, Long/* offset */> lmqTopicQueueTable = new ConcurrentHashMap<>(1024);",
        ),
        (
            "    public long getQueueOffset(String topicQueueKey) {",
            "    /** 获取或初始化 topic-queue 当前逻辑偏移（默认 0）。 */\n    public long getQueueOffset(String topicQueueKey) {",
        ),
        (
            "    public void increaseQueueOffset(String topicQueueKey, short messageNum) {",
            "    /** 按 messageNum 递增普通 topic-queue 偏移。 */\n    public void increaseQueueOffset(String topicQueueKey, short messageNum) {",
        ),
        (
            "    public void updateQueueOffset(String topicQueueKey, long offset) {",
            "    /** 直接设置 topic-queue 逻辑偏移。 */\n    public void updateQueueOffset(String topicQueueKey, long offset) {",
        ),
        (
            "    public long getBatchQueueOffset(String topicQueueKey) {",
            "    /** 获取或初始化批量 topic-queue 偏移。 */\n    public long getBatchQueueOffset(String topicQueueKey) {",
        ),
        (
            "    public void increaseBatchQueueOffset(String topicQueueKey, short messageNum) {",
            "    /** 递增批量 topic-queue 偏移。 */\n    public void increaseBatchQueueOffset(String topicQueueKey, short messageNum) {",
        ),
        (
            "    public long getLmqOffset(String topic, int queueId, OffsetInitializer callback) throws ConsumeQueueException {",
            "    /** 查询 LMQ 偏移；缓存未命中时通过 callback 从持久层加载。 */\n    public long getLmqOffset(String topic, int queueId, OffsetInitializer callback) throws ConsumeQueueException {",
        ),
        (
            "            // Load from RocksDB on cache miss.",
            "            // 缓存未命中时从 RocksDB 加载",
        ),
        (
            "    public void increaseLmqOffset(String topic, int queueId, short delta) throws ConsumeQueueException {",
            "    /** 递增 LMQ 最大逻辑偏移（须已存在于缓存）。 */\n    public void increaseLmqOffset(String topic, int queueId, short delta) throws ConsumeQueueException {",
        ),
        (
            "    public long currentQueueOffset(String topicQueueKey) {",
            "    /** 返回 topic-queue 当前逻辑偏移，不存在时返回 0。 */\n    public long currentQueueOffset(String topicQueueKey) {",
        ),
        (
            "    public synchronized void remove(String topic, Integer queueId) {",
            "    /** 从三张偏移表中移除指定 topic-queue 条目。 */\n    public synchronized void remove(String topic, Integer queueId) {",
        ),
        (
            "        // Beware of thread-safety",
            "        // 注意线程安全",
        ),
        (
            "    public void setLmqTopicQueueTable(ConcurrentMap<String, Long> lmqTopicQueueTable) {",
            "    /** 从 topicQueueTable 中提取 LMQ 条目填充 LMQ 偏移表。 */\n    public void setLmqTopicQueueTable(ConcurrentMap<String, Long> lmqTopicQueueTable) {",
        ),
    ],
}
