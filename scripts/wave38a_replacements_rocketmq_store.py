"""Chinese JavaDoc replacements for RocketMQ wave38a store [0:15]."""

R: dict[str, list[tuple[str, str]]] = {
    "store/src/main/java/org/apache/rocketmq/store/PutMessageStatus.java": [
        (
            "public enum PutMessageStatus {",
            "/**\n * 写消息结果状态枚举：CommitLog 写入、刷盘、同步及校验各阶段的返回码。\n */\npublic enum PutMessageStatus {",
        ),
        (
            "    PUT_OK,",
            "    /** 写入成功。 */\n    PUT_OK,",
        ),
        (
            "    FLUSH_DISK_TIMEOUT,",
            "    /** 刷盘超时。 */\n    FLUSH_DISK_TIMEOUT,",
        ),
        (
            "    FLUSH_SLAVE_TIMEOUT,",
            "    /** 同步从节点刷盘超时。 */\n    FLUSH_SLAVE_TIMEOUT,",
        ),
        (
            "    SLAVE_NOT_AVAILABLE,",
            "    /** 从节点不可用。 */\n    SLAVE_NOT_AVAILABLE,",
        ),
        (
            "    SERVICE_NOT_AVAILABLE,",
            "    /** 存储服务不可用。 */\n    SERVICE_NOT_AVAILABLE,",
        ),
        (
            "    CREATE_MAPPED_FILE_FAILED,",
            "    /** 创建 MappedFile 失败。 */\n    CREATE_MAPPED_FILE_FAILED,",
        ),
        (
            "    MESSAGE_ILLEGAL,",
            "    /** 消息内容或格式非法。 */\n    MESSAGE_ILLEGAL,",
        ),
        (
            "    PROPERTIES_SIZE_EXCEEDED,",
            "    /** 消息属性大小超限。 */\n    PROPERTIES_SIZE_EXCEEDED,",
        ),
        (
            "    OS_PAGE_CACHE_BUSY,",
            "    /** 操作系统页缓存繁忙。 */\n    OS_PAGE_CACHE_BUSY,",
        ),
        (
            "    UNKNOWN_ERROR,",
            "    /** 未知错误。 */\n    UNKNOWN_ERROR,",
        ),
        (
            "    IN_SYNC_REPLICAS_NOT_ENOUGH,",
            "    /** 同步副本数量不足。 */\n    IN_SYNC_REPLICAS_NOT_ENOUGH,",
        ),
        (
            "    PUT_TO_REMOTE_BROKER_FAIL,",
            "    /** 写入远程 Broker 失败。 */\n    PUT_TO_REMOTE_BROKER_FAIL,",
        ),
        (
            "    LMQ_CONSUME_QUEUE_NUM_EXCEEDED,",
            "    /** LMQ 消费队列数量超限。 */\n    LMQ_CONSUME_QUEUE_NUM_EXCEEDED,",
        ),
        (
            "    WHEEL_TIMER_FLOW_CONTROL,",
            "    /** 时间轮流控拒绝写入。 */\n    WHEEL_TIMER_FLOW_CONTROL,",
        ),
        (
            "    WHEEL_TIMER_MSG_ILLEGAL,",
            "    /** 时间轮消息非法。 */\n    WHEEL_TIMER_MSG_ILLEGAL,",
        ),
        (
            "    WHEEL_TIMER_NOT_ENABLE",
            "    /** 时间轮功能未启用。 */\n    WHEEL_TIMER_NOT_ENABLE",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/QueryMessageResult.java": [
        (
            "public class QueryMessageResult {",
            "/**\n * 按索引查询消息的结果容器：聚合多条 SelectMappedBufferResult 及索引元数据。\n */\npublic class QueryMessageResult {",
        ),
        (
            "    private final List<SelectMappedBufferResult> messageMapedList =\n        new ArrayList<>(100);",
            "    /** 查询命中的 Mapped 缓冲区结果列表。 */\n    private final List<SelectMappedBufferResult> messageMapedList =\n        new ArrayList<>(100);",
        ),
        (
            "    private final List<ByteBuffer> messageBufferList = new ArrayList<>(100);",
            "    /** 对应消息内容的 ByteBuffer 列表。 */\n    private final List<ByteBuffer> messageBufferList = new ArrayList<>(100);",
        ),
        (
            "    private long indexLastUpdateTimestamp;",
            "    /** 索引最后更新时间戳。 */\n    private long indexLastUpdateTimestamp;",
        ),
        (
            "    private long indexLastUpdatePhyoffset;",
            "    /** 索引最后更新时的物理偏移量。 */\n    private long indexLastUpdatePhyoffset;",
        ),
        (
            "    private int bufferTotalSize = 0;",
            "    /** 所有消息缓冲区的总字节数。 */\n    private int bufferTotalSize = 0;",
        ),
        (
            "    public void addMessage(final SelectMappedBufferResult mapedBuffer) {",
            "    /** 追加一条查询结果并累加总大小。 */\n    public void addMessage(final SelectMappedBufferResult mapedBuffer) {",
        ),
        (
            "    public void release() {",
            "    /** 释放所有 Mapped 缓冲区引用。 */\n    public void release() {",
        ),
        (
            "    public long getIndexLastUpdateTimestamp() {",
            "    /** 返回索引最后更新时间戳。 */\n    public long getIndexLastUpdateTimestamp() {",
        ),
        (
            "    public void setIndexLastUpdateTimestamp(long indexLastUpdateTimestamp) {",
            "    /** 设置索引最后更新时间戳。 */\n    public void setIndexLastUpdateTimestamp(long indexLastUpdateTimestamp) {",
        ),
        (
            "    public long getIndexLastUpdatePhyoffset() {",
            "    /** 返回索引最后更新的物理偏移量。 */\n    public long getIndexLastUpdatePhyoffset() {",
        ),
        (
            "    public void setIndexLastUpdatePhyoffset(long indexLastUpdatePhyoffset) {",
            "    /** 设置索引最后更新的物理偏移量。 */\n    public void setIndexLastUpdatePhyoffset(long indexLastUpdatePhyoffset) {",
        ),
        (
            "    public List<ByteBuffer> getMessageBufferList() {",
            "    /** 返回消息 ByteBuffer 列表。 */\n    public List<ByteBuffer> getMessageBufferList() {",
        ),
        (
            "    public int getBufferTotalSize() {",
            "    /** 返回缓冲区总字节数。 */\n    public int getBufferTotalSize() {",
        ),
        (
            "    public List<SelectMappedBufferResult> getMessageMapedList() {",
            "    /** 返回 Mapped 缓冲区结果列表。 */\n    public List<SelectMappedBufferResult> getMessageMapedList() {",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/ReferenceResource.java": [
        (
            "public abstract class ReferenceResource {",
            "/**\n * 引用计数资源基类：通过 hold/release 管理 MappedFile 等资源的共享与回收。\n */\npublic abstract class ReferenceResource {",
        ),
        (
            "    protected final AtomicLong refCount = new AtomicLong(1);",
            "    /** 当前引用计数，初始为 1。 */\n    protected final AtomicLong refCount = new AtomicLong(1);",
        ),
        (
            "    protected volatile boolean available = true;",
            "    /** 资源是否仍可用（未 shutdown）。 */\n    protected volatile boolean available = true;",
        ),
        (
            "    protected volatile boolean cleanupOver = false;",
            "    /** 底层 cleanup 是否已完成。 */\n    protected volatile boolean cleanupOver = false;",
        ),
        (
            "    private volatile long firstShutdownTimestamp = 0;",
            "    /** 首次 shutdown 的时间戳，用于强制回收。 */\n    private volatile long firstShutdownTimestamp = 0;",
        ),
        (
            "    public synchronized boolean hold() {",
            "    /** 增加引用计数；资源不可用或计数异常时返回 false。 */\n    public synchronized boolean hold() {",
        ),
        (
            "    public boolean isAvailable() {",
            "    /** 资源是否可用。 */\n    public boolean isAvailable() {",
        ),
        (
            "    public void shutdown(final long intervalForcibly) {",
            "    /** 标记不可用并尝试释放；超时后强制将引用计数置负以触发 cleanup。 */\n    public void shutdown(final long intervalForcibly) {",
        ),
        (
            "    public void release() {",
            "    /** 递减引用计数，归零时调用 cleanup。 */\n    public void release() {",
        ),
        (
            "    public long getRefCount() {",
            "    /** 返回当前引用计数。 */\n    public long getRefCount() {",
        ),
        (
            "    public abstract boolean cleanup(final long currentRef);",
            "    /** 引用计数归零时的资源清理逻辑，由子类实现。 */\n    public abstract boolean cleanup(final long currentRef);",
        ),
        (
            "    public boolean isCleanupOver() {",
            "    /** 引用已归零且 cleanup 是否完成。 */\n    public boolean isCleanupOver() {",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/RocksDBMessageStore.java": [
        (
            "public class RocksDBMessageStore extends DefaultMessageStore {",
            "/**\n * 基于 RocksDB 的消息存储实现：消费队列持久化到 RocksDB 而非本地文件。\n */\npublic class RocksDBMessageStore extends DefaultMessageStore {",
        ),
        (
            "    public RocksDBMessageStore(final MessageStoreConfig messageStoreConfig, final BrokerStatsManager brokerStatsManager,\n        final MessageArrivingListener messageArrivingListener, final BrokerConfig brokerConfig, final ConcurrentMap<String, TopicConfig> topicConfigTable) throws\n        IOException {",
            "    /** 构造 RocksDB 版 MessageStore，委托父类完成 CommitLog 等初始化。 */\n    public RocksDBMessageStore(final MessageStoreConfig messageStoreConfig, final BrokerStatsManager brokerStatsManager,\n        final MessageArrivingListener messageArrivingListener, final BrokerConfig brokerConfig, final ConcurrentMap<String, TopicConfig> topicConfigTable) throws\n        IOException {",
        ),
        (
            "    @Override\n    public ConsumeQueueStoreInterface createConsumeQueueStore() {",
            "    /** 创建 RocksDB 消费队列存储实例。 */\n    @Override\n    public ConsumeQueueStoreInterface createConsumeQueueStore() {",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/RunningFlags.java": [
        (
            "public class RunningFlags {",
            "/**\n * Broker 存储运行标志位：以位掩码表示可读、可写、磁盘满、围栏及索引错误等状态。\n */\npublic class RunningFlags {",
        ),
        (
            "    private static final int NOT_READABLE_BIT = 1;",
            "    /** 不可读标志位。 */\n    private static final int NOT_READABLE_BIT = 1;",
        ),
        (
            "    private static final int NOT_WRITEABLE_BIT = 1 << 1;",
            "    /** 不可写标志位。 */\n    private static final int NOT_WRITEABLE_BIT = 1 << 1;",
        ),
        (
            "    private static final int WRITE_LOGICS_QUEUE_ERROR_BIT = 1 << 2;",
            "    /** 逻辑队列写入错误标志位。 */\n    private static final int WRITE_LOGICS_QUEUE_ERROR_BIT = 1 << 2;",
        ),
        (
            "    private static final int WRITE_INDEX_FILE_ERROR_BIT = 1 << 3;",
            "    /** 索引文件写入错误标志位。 */\n    private static final int WRITE_INDEX_FILE_ERROR_BIT = 1 << 3;",
        ),
        (
            "    private static final int DISK_FULL_BIT = 1 << 4;",
            "    /** 物理磁盘已满标志位。 */\n    private static final int DISK_FULL_BIT = 1 << 4;",
        ),
        (
            "    private static final int FENCED_BIT = 1 << 5;",
            "    /** 围栏（fenced）标志位，禁止写入。 */\n    private static final int FENCED_BIT = 1 << 5;",
        ),
        (
            "    private static final int LOGIC_DISK_FULL_BIT = 1 << 6;",
            "    /** 逻辑磁盘已满标志位。 */\n    private static final int LOGIC_DISK_FULL_BIT = 1 << 6;",
        ),
        (
            "    private volatile int flagBits = 0;",
            "    /** 当前组合标志位。 */\n    private volatile int flagBits = 0;",
        ),
        (
            "    public RunningFlags() {",
            "    /** 默认构造，所有标志位清零。 */\n    public RunningFlags() {",
        ),
        (
            "    public int getFlagBits() {",
            "    /** 返回原始标志位整型值。 */\n    public int getFlagBits() {",
        ),
        (
            "    public boolean getAndMakeReadable() {",
            "    /** 若当前不可读则清除不可读位，并返回变更前的可读状态。 */\n    public boolean getAndMakeReadable() {",
        ),
        (
            "    public boolean isReadable() {",
            "    /** 是否可读（未置 NOT_READABLE 位）。 */\n    public boolean isReadable() {",
        ),
        (
            "    public boolean isFenced() {",
            "    /** 是否处于围栏状态。 */\n    public boolean isFenced() {",
        ),
        (
            "    public boolean getAndMakeNotReadable() {",
            "    /** 若当前可读则置不可读位，并返回变更前的可读状态。 */\n    public boolean getAndMakeNotReadable() {",
        ),
        (
            "    public void clearLogicsQueueError() {",
            "    /** 清除逻辑队列写入错误标志。 */\n    public void clearLogicsQueueError() {",
        ),
        (
            "    public boolean getAndMakeWriteable() {",
            "    /** 若当前不可写则尝试清除不可写位，返回变更前是否可写。 */\n    public boolean getAndMakeWriteable() {",
        ),
        (
            "    public boolean isWriteable() {",
            "    /** 综合判断是否可写（无不可写、队列错误、磁盘满、索引错误、围栏等）。 */\n    public boolean isWriteable() {",
        ),
        (
            "    public boolean isStoreWriteable() {",
            "    /** 仅检查 NOT_WRITEABLE 位是否未置位。 */\n    public boolean isStoreWriteable() {",
        ),
        (
            "    //for consume queue, just ignore the DISK_FULL_BIT\n    public boolean isCQWriteable() {",
            "    /** 消费队列是否可写（忽略物理磁盘满标志）。 */\n    //for consume queue, just ignore the DISK_FULL_BIT\n    public boolean isCQWriteable() {",
        ),
        (
            "    public boolean getAndMakeStoreNotWriteable() {",
            "    /** 若当前可写则置不可写位，返回变更前是否可写。 */\n    public boolean getAndMakeStoreNotWriteable() {",
        ),
        (
            "    public void makeLogicsQueueError() {",
            "    /** 标记逻辑队列写入错误。 */\n    public void makeLogicsQueueError() {",
        ),
        (
            "    public void makeFenced(boolean fenced) {",
            "    /** 设置或清除围栏标志。 */\n    public void makeFenced(boolean fenced) {",
        ),
        (
            "    public boolean isLogicsQueueError() {",
            "    /** 逻辑队列是否写入错误。 */\n    public boolean isLogicsQueueError() {",
        ),
        (
            "    public void makeIndexFileError() {",
            "    /** 标记索引文件写入错误。 */\n    public void makeIndexFileError() {",
        ),
        (
            "    public boolean isIndexFileError() {",
            "    /** 索引文件是否写入错误。 */\n    public boolean isIndexFileError() {",
        ),
        (
            "    public boolean getAndMakeDiskFull() {",
            "    /** 置物理磁盘满标志，返回变更前是否未满。 */\n    public boolean getAndMakeDiskFull() {",
        ),
        (
            "    public boolean getAndMakeDiskOK() {",
            "    /** 清除物理磁盘满标志，返回变更前是否未满。 */\n    public boolean getAndMakeDiskOK() {",
        ),
        (
            "    public boolean getAndMakeLogicDiskFull() {",
            "    /** 置逻辑磁盘满标志，返回变更前是否未满。 */\n    public boolean getAndMakeLogicDiskFull() {",
        ),
        (
            "    public boolean getAndMakeLogicDiskOK() {",
            "    /** 清除逻辑磁盘满标志，返回变更前是否未满。 */\n    public boolean getAndMakeLogicDiskOK() {",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/SelectMappedBufferResult.java": [
        (
            "public class SelectMappedBufferResult {",
            "/**\n * Mapped 文件切片查询结果：封装起始偏移、ByteBuffer 视图及所属 MappedFile。\n */\npublic class SelectMappedBufferResult {",
        ),
        (
            "    private final long startOffset;",
            "    /** 切片在 CommitLog 中的起始物理偏移。 */\n    private final long startOffset;",
        ),
        (
            "    private final ByteBuffer byteBuffer;",
            "    /** 消息内容的只读 ByteBuffer 视图。 */\n    private final ByteBuffer byteBuffer;",
        ),
        (
            "    private int size;",
            "    /** 有效数据字节长度。 */\n    private int size;",
        ),
        (
            "    protected MappedFile mappedFile;",
            "    /** 数据来源 MappedFile，release 后置 null。 */\n    protected MappedFile mappedFile;",
        ),
        (
            "    private boolean isInCache = true;",
            "    /** 数据是否仍在页缓存中（影响消费延迟统计）。 */\n    private boolean isInCache = true;",
        ),
        (
            "    public SelectMappedBufferResult(long startOffset, ByteBuffer byteBuffer, int size, MappedFile mappedFile) {",
            "    /** 构造指定偏移与缓冲区的查询结果。 */\n    public SelectMappedBufferResult(long startOffset, ByteBuffer byteBuffer, int size, MappedFile mappedFile) {",
        ),
        (
            "    public ByteBuffer getByteBuffer() {",
            "    /** 返回消息 ByteBuffer。 */\n    public ByteBuffer getByteBuffer() {",
        ),
        (
            "    public int getSize() {",
            "    /** 返回有效数据长度。 */\n    public int getSize() {",
        ),
        (
            "    public void setSize(final int s) {",
            "    /** 设置有效长度并调整 buffer limit。 */\n    public void setSize(final int s) {",
        ),
        (
            "    public MappedFile getMappedFile() {",
            "    /** 返回所属 MappedFile。 */\n    public MappedFile getMappedFile() {",
        ),
        (
            "    public synchronized void release() {",
            "    /** 释放 MappedFile 引用。 */\n    public synchronized void release() {",
        ),
        (
            "    public synchronized boolean hasReleased() {",
            "    /** 是否已 release（mappedFile 为 null）。 */\n    public synchronized boolean hasReleased() {",
        ),
        (
            "    public long getStartOffset() {",
            "    /** 返回起始物理偏移。 */\n    public long getStartOffset() {",
        ),
        (
            "    public boolean isInMem() {",
            "    /** 对应区间是否已加载到内存（mmap 预热）。 */\n    public boolean isInMem() {",
        ),
        (
            "    public boolean isInCache() {",
            "    /** 是否在页缓存中。 */\n    public boolean isInCache() {",
        ),
        (
            "    public void setInCache(boolean inCache) {",
            "    /** 设置页缓存标志。 */\n    public void setInCache(boolean inCache) {",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/SelectMappedFileResult.java": [
        (
            "public class SelectMappedFileResult {",
            "/**\n * MappedFile 选择结果：仅包含文件引用与有效写入大小，不含 ByteBuffer 视图。\n */\npublic class SelectMappedFileResult {",
        ),
        (
            "    protected int size;",
            "    /** 当前 MappedFile 内有效数据大小。 */\n    protected int size;",
        ),
        (
            "    protected MappedFile mappedFile;",
            "    /** 选中的 MappedFile 实例。 */\n    protected MappedFile mappedFile;",
        ),
        (
            "    public SelectMappedFileResult(int size, MappedFile mappedFile) {",
            "    /** 构造指定大小与文件的查询结果。 */\n    public SelectMappedFileResult(int size, MappedFile mappedFile) {",
        ),
        (
            "    public int getSize() {",
            "    /** 返回有效数据大小。 */\n    public int getSize() {",
        ),
        (
            "    public void setSize(int size) {",
            "    /** 设置有效数据大小。 */\n    public void setSize(int size) {",
        ),
        (
            "    public MappedFile getMappedFile() {",
            "    /** 返回 MappedFile 引用。 */\n    public MappedFile getMappedFile() {",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/StoreCheckpoint.java": [
        (
            "public class StoreCheckpoint {",
            "/**\n * 存储检查点文件：mmap 持久化物理/逻辑消息时间戳、索引时间及刷盘偏移等元数据。\n */\npublic class StoreCheckpoint {",
        ),
        (
            "    private volatile long tmpLogicsMsgTimestamp = 0;",
            "    /** 逻辑消息时间戳临时值（刷盘前）。 */\n    private volatile long tmpLogicsMsgTimestamp = 0;",
        ),
        (
            "    private volatile long physicMsgTimestamp = 0;",
            "    /** 已持久化的物理消息最大时间戳。 */\n    private volatile long physicMsgTimestamp = 0;",
        ),
        (
            "    private volatile long logicsMsgTimestamp = 0;",
            "    /** 已持久化的逻辑消息最大时间戳。 */\n    private volatile long logicsMsgTimestamp = 0;",
        ),
        (
            "    private volatile long tmpLogicsPhysicalOffset = 0;",
            "    /** 逻辑队列物理偏移临时值。 */\n    private volatile long tmpLogicsPhysicalOffset = 0;",
        ),
        (
            "    private volatile long logicsPhysicalOffset = 0;",
            "    /** 已持久化的逻辑队列物理偏移。 */\n    private volatile long logicsPhysicalOffset = 0;",
        ),
        (
            "    private volatile long indexMsgTimestamp = 0;",
            "    /** 索引文件最大消息时间戳。 */\n    private volatile long indexMsgTimestamp = 0;",
        ),
        (
            "    private volatile long masterFlushedOffset = 0;",
            "    /** 主节点已刷盘确认的物理偏移。 */\n    private volatile long masterFlushedOffset = 0;",
        ),
        (
            "    private volatile long confirmPhyOffset = 0;",
            "    /** 已确认的最小物理偏移（用于过期删除）。 */\n    private volatile long confirmPhyOffset = 0;",
        ),
        (
            "    public StoreCheckpoint(final String scpPath) throws IOException {",
            "    /** 打开或创建检查点文件并 mmap 一页；若已存在则加载各字段。 */\n    public StoreCheckpoint(final String scpPath) throws IOException {",
        ),
        (
            "    public void shutdown() {",
            "    /** 刷盘后解除 mmap 并关闭文件通道。 */\n    public void shutdown() {",
        ),
        (
            "        // unmap mappedByteBuffer",
            "        // 解除 mmap 映射",
        ),
        (
            "    public void flush() {",
            "    /** 将各时间戳与偏移写入 mmap 并 force 到磁盘。 */\n    public void flush() {",
        ),
        (
            "    public long getPhysicMsgTimestamp() {",
            "    /** 返回物理消息时间戳。 */\n    public long getPhysicMsgTimestamp() {",
        ),
        (
            "    public void setPhysicMsgTimestamp(long physicMsgTimestamp) {",
            "    /** 设置物理消息时间戳。 */\n    public void setPhysicMsgTimestamp(long physicMsgTimestamp) {",
        ),
        (
            "    public long getLogicsMsgTimestamp() {",
            "    /** 返回逻辑消息时间戳。 */\n    public long getLogicsMsgTimestamp() {",
        ),
        (
            "    public void setLogicsMsgTimestamp(long logicsMsgTimestamp) {",
            "    /** 设置逻辑消息时间戳。 */\n    public void setLogicsMsgTimestamp(long logicsMsgTimestamp) {",
        ),
        (
            "    public long getMinTimestampIndex() {",
            "    /** 返回物理/逻辑/索引时间戳中的最小值（减 3 秒缓冲）。 */\n    public long getMinTimestampIndex() {",
        ),
        (
            "    public long getMinTimestamp() {",
            "    /** 返回物理与逻辑时间戳的较小值减 3 秒。 */\n    public long getMinTimestamp() {",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/StoreType.java": [
        (
            "public enum StoreType {",
            "/**\n * 消息存储后端类型：默认文件存储或 RocksDB 存储。\n */\npublic enum StoreType {",
        ),
        (
            '    DEFAULT("default"),',
            '    /** 默认 CommitLog + 文件消费队列。 */\n    DEFAULT("default"),',
        ),
        (
            '    DEFAULT_ROCKSDB("defaultRocksDB");',
            '    /** 默认 CommitLog + RocksDB 消费队列。 */\n    DEFAULT_ROCKSDB("defaultRocksDB");',
        ),
        (
            "    private String storeType;",
            "    /** 配置字符串标识。 */\n    private String storeType;",
        ),
        (
            "    StoreType(String storeType) {",
            "    /** 按字符串标识构造枚举常量。 */\n    StoreType(String storeType) {",
        ),
        (
            "    public String getStoreType() {",
            "    /** 返回存储类型字符串。 */\n    public String getStoreType() {",
        ),
        (
            "     * convert string to set of StoreType\n     *\n     * @param str example \"default;defaultRocksDB\"\n     * @return set of StoreType",
            "     * 将分号分隔的配置字符串解析为 StoreType 集合。\n     *\n     * @param str 示例 \"default;defaultRocksDB\"\n     * @return StoreType 集合",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/StoreUtil.java": [
        (
            "public class StoreUtil {",
            "/**\n * 存储层工具类：物理内存探测、MappedFile 追加及消息解码等辅助方法。\n */\npublic class StoreUtil {",
        ),
        (
            "    public static final long TOTAL_PHYSICAL_MEMORY_SIZE = getTotalPhysicalMemorySize();",
            "    /** JVM 可见的物理内存总量（字节）。 */\n    public static final long TOTAL_PHYSICAL_MEMORY_SIZE = getTotalPhysicalMemorySize();",
        ),
        (
            "    public static long getTotalPhysicalMemorySize() {",
            "    /** 通过 OperatingSystemMXBean 获取物理内存，不可用时默认 24GB。 */\n    public static long getTotalPhysicalMemorySize() {",
        ),
        (
            "    public static void fileAppend(MappedFile file, ByteBuffer data) {",
            "    /** 向 MappedFile 追加数据，失败时抛出 RuntimeException。 */\n    public static void fileAppend(MappedFile file, ByteBuffer data) {",
        ),
        (
            "    public static FileQueueSnapshot getFileQueueSnapshot(MappedFileQueue mappedFileQueue) {",
            "    /** 以末文件起始偏移为当前位置获取文件队列快照。 */\n    public static FileQueueSnapshot getFileQueueSnapshot(MappedFileQueue mappedFileQueue) {",
        ),
        (
            "    public static FileQueueSnapshot getFileQueueSnapshot(MappedFileQueue mappedFileQueue, final long currentFile) {",
            "    /** 按指定 currentFile 偏移计算首尾文件索引与落后条数。 */\n    public static FileQueueSnapshot getFileQueueSnapshot(MappedFileQueue mappedFileQueue, final long currentFile) {",
        ),
        (
            "            log.error(\"[BUG] get file queue snapshot failed. fileQueue: {}, currentFile: {}\", mappedFileQueue, currentFile, e);",
            "            log.error(\"[BUG] 获取文件队列快照失败. fileQueue: {}, currentFile: {}\", mappedFileQueue, currentFile, e);",
        ),
        (
            "    public static MessageExt getMessage(long offsetPy, int sizePy, MessageStore messageStore, ByteBuffer byteBuffer) {",
            "    /** 按物理偏移与大小从 MessageStore 读取并解码单条消息。 */\n    public static MessageExt getMessage(long offsetPy, int sizePy, MessageStore messageStore, ByteBuffer byteBuffer) {",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/Swappable.java": [
        (
            "/**\n * Clean up page-table on super large disk\n */\npublic interface Swappable {",
            "/**\n * 超大磁盘场景下可交换映射：定期 swap/clean MappedFile 以降低页表占用。\n */\npublic interface Swappable {",
        ),
        (
            "    void swapMap(int reserveNum, long forceSwapIntervalMs, long normalSwapIntervalMs);",
            "    /** 按保留数量与间隔将冷 MappedFile 换出内存。 */\n    void swapMap(int reserveNum, long forceSwapIntervalMs, long normalSwapIntervalMs);",
        ),
        (
            "    void cleanSwappedMap(long forceCleanSwapIntervalMs);",
            "    /** 清理已换出且超过强制间隔的 MappedFile 映射。 */\n    void cleanSwappedMap(long forceCleanSwapIntervalMs);",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/TopicQueueLock.java": [
        (
            "public class TopicQueueLock {",
            "/**\n * Topic-Queue 分段锁：按 topicQueueKey 哈希到固定数量 ReentrantLock，降低锁竞争。\n */\npublic class TopicQueueLock {",
        ),
        (
            "    private final int size;",
            "    /** 锁分段数量。 */\n    private final int size;",
        ),
        (
            "    private final List<Lock> lockList;",
            "    /** 分段锁列表。 */\n    private final List<Lock> lockList;",
        ),
        (
            "    public TopicQueueLock() {",
            "    /** 默认 32 个分段锁。 */\n    public TopicQueueLock() {",
        ),
        (
            "    public TopicQueueLock(int size) {",
            "    /** 指定分段数量构造。 */\n    public TopicQueueLock(int size) {",
        ),
        (
            "    public void lock(String topicQueueKey) {",
            "    /** 对 topicQueueKey 对应分段加锁。 */\n    public void lock(String topicQueueKey) {",
        ),
        (
            "    public void unlock(String topicQueueKey) {",
            "    /** 释放 topicQueueKey 对应分段锁。 */\n    public void unlock(String topicQueueKey) {",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/TransientStorePool.java": [
        (
            "public class TransientStorePool {",
            "/**\n *  transient 写入缓冲池：预分配 direct ByteBuffer 并 mlock，供 CommitLog 双写路径借用。\n */\npublic class TransientStorePool {",
        ),
        (
            "    private final int poolSize;",
            "    /** 缓冲池容量（块数）。 */\n    private final int poolSize;",
        ),
        (
            "    private final int fileSize;",
            "    /** 每块 direct buffer 大小（通常等于 mappedFileSize）。 */\n    private final int fileSize;",
        ),
        (
            "    private final Deque<ByteBuffer> availableBuffers;",
            "    /** 可用 direct buffer 双端队列。 */\n    private final Deque<ByteBuffer> availableBuffers;",
        ),
        (
            "    private volatile boolean isRealCommit = true;",
            "    /** 是否执行真实 commit（否则仅写 transient 缓冲）。 */\n    private volatile boolean isRealCommit = true;",
        ),
        (
            "    public TransientStorePool(final int poolSize, final int fileSize) {",
            "    /** 构造指定容量与块大小的缓冲池。 */\n    public TransientStorePool(final int poolSize, final int fileSize) {",
        ),
        (
            "     * It's a heavy init method.",
            "     * 重量级初始化：分配 direct buffer 并 mlock 锁定物理页。",
        ),
        (
            "    public void init() {",
            "    /** 预分配并锁定全部 direct buffer。 */\n    public void init() {",
        ),
        (
            "    public void destroy() {",
            "    /** 释放 mlock 并销毁缓冲池。 */\n    public void destroy() {",
        ),
        (
            "    public void returnBuffer(ByteBuffer byteBuffer) {",
            "    /** 归还借出的 buffer 到池首。 */\n    public void returnBuffer(ByteBuffer byteBuffer) {",
        ),
        (
            "    public ByteBuffer borrowBuffer() {",
            "    /** 从池首借出一块 buffer；余量不足 40% 时打 warn 日志。 */\n    public ByteBuffer borrowBuffer() {",
        ),
        (
            "            log.warn(\"TransientStorePool only remain {} sheets.\", availableBuffers.size());",
            "            log.warn(\"TransientStorePool 剩余缓冲仅 {} 块.\", availableBuffers.size());",
        ),
        (
            "    public int availableBufferNums() {",
            "    /** 返回当前可用 buffer 数量。 */\n    public int availableBufferNums() {",
        ),
        (
            "    public boolean isRealCommit() {",
            "    /** 是否真实 commit 到 MappedFile。 */\n    public boolean isRealCommit() {",
        ),
        (
            "    public void setRealCommit(boolean realCommit) {",
            "    /** 设置是否真实 commit。 */\n    public void setRealCommit(boolean realCommit) {",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/config/BrokerRole.java": [
        (
            "public enum BrokerRole {",
            "/**\n * Broker 角色枚举：异步主、同步主或从节点，影响刷盘与复制策略。\n */\npublic enum BrokerRole {",
        ),
        (
            "    ASYNC_MASTER,",
            "    /** 异步复制主节点。 */\n    ASYNC_MASTER,",
        ),
        (
            "    SYNC_MASTER,",
            "    /** 同步复制主节点。 */\n    SYNC_MASTER,",
        ),
        (
            "    SLAVE;",
            "    /** 从节点，只读复制。 */\n    SLAVE;",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/config/FlushDiskType.java": [
        (
            "public enum FlushDiskType {",
            "/**\n * 刷盘策略：同步刷盘保证持久化，异步刷盘提升吞吐。\n */\npublic enum FlushDiskType {",
        ),
        (
            "    SYNC_FLUSH,",
            "    /** 同步刷盘，写入线程等待 fsync 完成。 */\n    SYNC_FLUSH,",
        ),
        (
            "    ASYNC_FLUSH",
            "    /** 异步刷盘，由后台线程批量落盘。 */\n    ASYNC_FLUSH",
        ),
    ],
}
