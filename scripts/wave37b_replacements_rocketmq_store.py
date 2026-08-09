"""Chinese JavaDoc replacements for RocketMQ wave37b store [15:30]."""

R: dict[str, list[tuple[str, str]]] = {
    "store/src/main/java/org/apache/rocketmq/store/FileQueueSnapshot.java": [
        (
            "public class FileQueueSnapshot {",
            "/**\n * CommitLog 文件队列快照：记录首尾 MappedFile、当前写入位置及落后条数等元信息。\n */\npublic class FileQueueSnapshot {",
        ),
        (
            "    private MappedFile firstFile;",
            "    /** 队列中首个 MappedFile。 */\n    private MappedFile firstFile;",
        ),
        (
            "    private long firstFileIndex;",
            "    /** 首个文件在队列中的索引。 */\n    private long firstFileIndex;",
        ),
        (
            "    private MappedFile lastFile;",
            "    /** 队列中最后一个 MappedFile。 */\n    private MappedFile lastFile;",
        ),
        (
            "    private long lastFileIndex;",
            "    /** 末位文件在队列中的索引。 */\n    private long lastFileIndex;",
        ),
        (
            "    private long currentFile;",
            "    /** 当前正在写入的文件偏移或标识。 */\n    private long currentFile;",
        ),
        (
            "    private long currentFileIndex;",
            "    /** 当前写入文件在队列中的索引。 */\n    private long currentFileIndex;",
        ),
        (
            "    private long behindCount;",
            "    /** 相对消费进度落后的消息条数。 */\n    private long behindCount;",
        ),
        (
            "    private boolean exist;",
            "    /** 快照对应队列是否存在。 */\n    private boolean exist;",
        ),
        (
            "    public FileQueueSnapshot() {",
            "    /** 无参构造，用于序列化或占位。 */\n    public FileQueueSnapshot() {",
        ),
        (
            "    public FileQueueSnapshot(MappedFile firstFile, long firstFileIndex, MappedFile lastFile, long lastFileIndex, long currentFile, long currentFileIndex, long behindCount, boolean exist) {",
            "    /** 构造完整文件队列快照。 */\n    public FileQueueSnapshot(MappedFile firstFile, long firstFileIndex, MappedFile lastFile, long lastFileIndex, long currentFile, long currentFileIndex, long behindCount, boolean exist) {",
        ),
        (
            "    public MappedFile getFirstFile() {",
            "    /** 返回首个 MappedFile。 */\n    public MappedFile getFirstFile() {",
        ),
        (
            "    public long getFirstFileIndex() {",
            "    /** 返回首个文件索引。 */\n    public long getFirstFileIndex() {",
        ),
        (
            "    public MappedFile getLastFile() {",
            "    /** 返回末位 MappedFile。 */\n    public MappedFile getLastFile() {",
        ),
        (
            "    public long getLastFileIndex() {",
            "    /** 返回末位文件索引。 */\n    public long getLastFileIndex() {",
        ),
        (
            "    public long getCurrentFile() {",
            "    /** 返回当前写入文件标识。 */\n    public long getCurrentFile() {",
        ),
        (
            "    public long getCurrentFileIndex() {",
            "    /** 返回当前写入文件索引。 */\n    public long getCurrentFileIndex() {",
        ),
        (
            "    public long getBehindCount() {",
            "    /** 返回落后条数。 */\n    public long getBehindCount() {",
        ),
        (
            "    public boolean isExist() {",
            "    /** 队列是否存在。 */\n    public boolean isExist() {",
        ),
        (
            "    @Override\n    public String toString() {",
            "    /** 返回调试字符串。 */\n    @Override\n    public String toString() {",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/FlushDiskWatcher.java": [
        (
            "public class FlushDiskWatcher extends ServiceThread {",
            "/**\n * 刷盘超时监视线程：异步等待 GroupCommit 完成，超时则唤醒调用方。\n */\npublic class FlushDiskWatcher extends ServiceThread {",
        ),
        (
            "    private final LinkedBlockingQueue<GroupCommitRequest> commitRequests = new LinkedBlockingQueue<>();",
            "    /** 待监视的组提交请求队列。 */\n    private final LinkedBlockingQueue<GroupCommitRequest> commitRequests = new LinkedBlockingQueue<>();",
        ),
        (
            "    @Override\n    public String getServiceName() {",
            "    /** 返回服务线程名称。 */\n    @Override\n    public String getServiceName() {",
        ),
        (
            "    @Override\n    public void run() {",
            "    /** 循环取请求并轮询刷盘 Future，超时则返回 FLUSH_DISK_TIMEOUT。 */\n    @Override\n    public void run() {",
        ),
        (
            "                // To avoid frequent thread switching, replace future.get with sleep here,",
            "                // 避免频繁线程切换，此处用 sleep 替代 future.get 轮询，",
        ),
        (
            "    public void add(GroupCommitRequest request) {",
            "    /** 提交一条刷盘监视请求。 */\n    public void add(GroupCommitRequest request) {",
        ),
        (
            "    public int queueSize() {",
            "    /** 返回待处理请求数量。 */\n    public int queueSize() {",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/FlushManager.java": [
        (
            "public interface FlushManager {",
            "/**\n * 刷盘管理器接口：负责 CommitLog 落盘、唤醒刷盘/提交线程及异步刷盘回调。\n */\npublic interface FlushManager {",
        ),
        (
            "    void start();",
            "    /** 启动刷盘相关线程。 */\n    void start();",
        ),
        (
            "    void shutdown();",
            "    /** 关闭刷盘服务。 */\n    void shutdown();",
        ),
        (
            "    void wakeUpFlush();",
            "    /** 唤醒刷盘线程立即执行。 */\n    void wakeUpFlush();",
        ),
        (
            "    void wakeUpCommit();",
            "    /** 唤醒提交线程立即执行。 */\n    void wakeUpCommit();",
        ),
        (
            "    void handleDiskFlush(AppendMessageResult result, PutMessageResult putMessageResult, MessageExt messageExt);",
            "    /** 同步处理磁盘刷盘并更新 PutMessageResult。 */\n    void handleDiskFlush(AppendMessageResult result, PutMessageResult putMessageResult, MessageExt messageExt);",
        ),
        (
            "    CompletableFuture<PutMessageStatus> handleDiskFlush(AppendMessageResult result, MessageExt messageExt);",
            "    /** 异步处理磁盘刷盘，返回 Future 状态。 */\n    CompletableFuture<PutMessageStatus> handleDiskFlush(AppendMessageResult result, MessageExt messageExt);",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/GetMessageResult.java": [
        (
            "public class GetMessageResult {",
            "/**\n * 拉取消息结果：封装状态、偏移区间、消息缓冲区列表及商业计费统计等。\n */\npublic class GetMessageResult {",
        ),
        (
            "    private final List<SelectMappedBufferResult> messageMapedList;",
            "    /** 映射缓冲区结果列表（含物理偏移）。 */\n    private final List<SelectMappedBufferResult> messageMapedList;",
        ),
        (
            "    private final List<ByteBuffer> messageBufferList;",
            "    /** 消息 ByteBuffer 列表，供上层直接读取。 */\n    private final List<ByteBuffer> messageBufferList;",
        ),
        (
            "    private final List<Long> messageQueueOffset;",
            "    /** 各消息在 ConsumeQueue 中的逻辑 offset。 */\n    private final List<Long> messageQueueOffset;",
        ),
        (
            "    private GetMessageStatus status;",
            "    /** 拉取结果状态码。 */\n    private GetMessageStatus status;",
        ),
        (
            "    private long nextBeginOffset;",
            "    /** 建议下次拉起的起始 offset。 */\n    private long nextBeginOffset;",
        ),
        (
            "    private long minOffset;",
            "    /** 队列最小可读 offset。 */\n    private long minOffset;",
        ),
        (
            "    private long maxOffset;",
            "    /** 队列最大可读 offset。 */\n    private long maxOffset;",
        ),
        (
            "    private int bufferTotalSize = 0;",
            "    /** 所有消息缓冲区总字节数。 */\n    private int bufferTotalSize = 0;",
        ),
        (
            "    private int messageCount = 0;",
            "    /** 本次拉取的消息条数。 */\n    private int messageCount = 0;",
        ),
        (
            "    private boolean suggestPullingFromSlave = false;",
            "    /** 是否建议从 Slave 拉取（主从延迟场景）。 */\n    private boolean suggestPullingFromSlave = false;",
        ),
        (
            "    private int msgCount4Commercial = 0;",
            "    /** 商业版计费消息条数（按块折算）。 */\n    private int msgCount4Commercial = 0;",
        ),
        (
            "    private int commercialSizePerMsg = 4 * 1024;",
            "    /** 商业版单条消息计费块大小（默认 4KB）。 */\n    private int commercialSizePerMsg = 4 * 1024;",
        ),
        (
            "    private long coldDataSum = 0L;",
            "    /** 冷数据总字节数统计。 */\n    private long coldDataSum = 0L;",
        ),
        (
            "    private int filterMessageCount;",
            "    /** 被过滤掉的消息条数。 */\n    private int filterMessageCount;",
        ),
        (
            "    public static final GetMessageResult NO_MATCH_LOGIC_QUEUE =",
            "    /** 无匹配逻辑队列时的空结果常量。 */\n    public static final GetMessageResult NO_MATCH_LOGIC_QUEUE =",
        ),
        (
            "    public GetMessageResult() {",
            "    /** 默认构造，预分配容量 100。 */\n    public GetMessageResult() {",
        ),
        (
            "    public GetMessageResult(int resultSize) {",
            "    /** 指定预分配容量的构造。 */\n    public GetMessageResult(int resultSize) {",
        ),
        (
            "    public GetMessageStatus getStatus() {",
            "    /** 返回拉取状态。 */\n    public GetMessageStatus getStatus() {",
        ),
        (
            "    public void setStatus(GetMessageStatus status) {",
            "    /** 设置拉取状态。 */\n    public void setStatus(GetMessageStatus status) {",
        ),
        (
            "    public void addMessage(final SelectMappedBufferResult mapedBuffer) {",
            "    /** 追加一条消息（不含队列 offset）。 */\n    public void addMessage(final SelectMappedBufferResult mapedBuffer) {",
        ),
        (
            "    public void addMessage(final SelectMappedBufferResult mapedBuffer, final long queueOffset) {",
            "    /** 追加一条消息并记录 ConsumeQueue offset。 */\n    public void addMessage(final SelectMappedBufferResult mapedBuffer, final long queueOffset) {",
        ),
        (
            "    public void addMessage(final SelectMappedBufferResult mapedBuffer, final long queueOffset, final int batchNum) {",
            "    /** 追加消息并按 batchNum 调整 messageCount（批量消息）。 */\n    public void addMessage(final SelectMappedBufferResult mapedBuffer, final long queueOffset, final int batchNum) {",
        ),
        (
            "    public void release() {",
            "    /** 释放所有映射缓冲区引用。 */\n    public void release() {",
        ),
        (
            "    @Override\n    public String toString() {",
            "    /** 返回调试字符串。 */\n    @Override\n    public String toString() {",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/GetMessageStatus.java": [
        (
            "public enum GetMessageStatus {",
            "/**\n * 拉取消息状态枚举：描述 offset 合法性、队列匹配及消息是否存在等结果。\n */\npublic enum GetMessageStatus {",
        ),
        (
            "    FOUND,",
            "    /** 成功找到消息。 */\n    FOUND,",
        ),
        (
            "    NO_MATCHED_MESSAGE,",
            "    /** 无匹配消息（如 TAG 过滤未命中）。 */\n    NO_MATCHED_MESSAGE,",
        ),
        (
            "    MESSAGE_WAS_REMOVING,",
            "    /** 消息正在被删除。 */\n    MESSAGE_WAS_REMOVING,",
        ),
        (
            "    OFFSET_FOUND_NULL,",
            "    /** offset 指向空槽位。 */\n    OFFSET_FOUND_NULL,",
        ),
        (
            "    OFFSET_OVERFLOW_BADLY,",
            "    /** offset 严重越界。 */\n    OFFSET_OVERFLOW_BADLY,",
        ),
        (
            "    OFFSET_OVERFLOW_ONE,",
            "    /** offset 越界一条（通常可重试）。 */\n    OFFSET_OVERFLOW_ONE,",
        ),
        (
            "    OFFSET_TOO_SMALL,",
            "    /** offset 过小，低于最小可读位置。 */\n    OFFSET_TOO_SMALL,",
        ),
        (
            "    NO_MATCHED_LOGIC_QUEUE,",
            "    /** 无匹配的逻辑队列。 */\n    NO_MATCHED_LOGIC_QUEUE,",
        ),
        (
            "    NO_MESSAGE_IN_QUEUE,",
            "    /** 队列中暂无消息。 */\n    NO_MESSAGE_IN_QUEUE,",
        ),
        (
            "    OFFSET_RESET",
            "    /** offset 已被重置。 */\n    OFFSET_RESET",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/LmqDispatch.java": [
        (
            "public class LmqDispatch {",
            "/**\n * 轻量消息队列（LMQ）分发工具：写入多队列 offset 并在落盘后递增 LMQ offset。\n */\npublic class LmqDispatch {",
        ),
        (
            "    private static final short VALUE_OF_EACH_INCREMENT = 1;",
            "    /** LMQ offset 每次递增步长。 */\n    private static final short VALUE_OF_EACH_INCREMENT = 1;",
        ),
        (
            "    public static void wrapLmqDispatch(MessageStore messageStore, final MessageExtBrokerInner msg)",
            "    /** 为消息填充各 LMQ 队列当前 offset 到内部属性。 */\n    public static void wrapLmqDispatch(MessageStore messageStore, final MessageExtBrokerInner msg)",
        ),
        (
            "    public static void updateLmqOffsets(MessageStore messageStore, final MessageExtBrokerInner msgInner)",
            "    /** 消息落盘后递增各 LMQ 队列 offset。 */\n    public static void updateLmqOffsets(MessageStore messageStore, final MessageExtBrokerInner msgInner)",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/MessageArrivingListener.java": [
        (
            "    /**\n     * Notify that a new message arrives in a consume queue\n     * @param topic topic name\n     * @param queueId consume queue id\n     * @param logicOffset consume queue offset\n     * @param tagsCode message tags hash code\n     * @param msgStoreTime message store time\n     * @param filterBitMap message bloom filter\n     * @param properties message properties\n     */",
            "    /**\n     * 消费队列有新消息到达时的回调通知。\n     * @param topic Topic 名称\n     * @param queueId 消费队列 ID\n     * @param logicOffset 消费队列逻辑 offset\n     * @param tagsCode 消息 Tag 哈希码\n     * @param msgStoreTime 消息存储时间戳\n     * @param filterBitMap 消息布隆过滤器位图\n     * @param properties 消息属性\n     */",
        ),
        (
            "public interface MessageArrivingListener {",
            "/**\n * 消息到达监听器：ConsumeQueue 写入新索引时通知上层（如长轮询挂起服务）。\n */\npublic interface MessageArrivingListener {",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/MessageFilter.java": [
        (
            "public interface MessageFilter {",
            "/**\n * 消息过滤接口：支持基于 ConsumeQueue 扩展单元或 CommitLog 内容的匹配。\n */\npublic interface MessageFilter {",
        ),
        (
            "    /**\n     * match by tags code or filter bit map which is calculated when message received\n     * and stored in consume queue ext.\n     *\n     * @param tagsCode tagsCode\n     * @param cqExtUnit extend unit of consume queue\n     */",
            "    /**\n     * 基于 Tag 哈希码或 ConsumeQueue 扩展单元中的布隆位图匹配。\n     *\n     * @param tagsCode Tag 哈希码\n     * @param cqExtUnit ConsumeQueue 扩展单元\n     */",
        ),
        (
            "    /**\n     * match by message content which are stored in commit log.\n     * <br>{@code msgBuffer} and {@code properties} are not all null.If invoked in store,\n     * {@code properties} is null;If invoked in {@code PullRequestHoldService}, {@code msgBuffer} is null.\n     *\n     * @param msgBuffer message buffer in commit log, may be null if not invoked in store.\n     * @param properties message properties, should decode from buffer if null by yourself.\n     */",
            "    /**\n     * 基于 CommitLog 中消息体内容匹配（如 SQL92 表达式）。\n     * <br>{@code msgBuffer} 与 {@code properties} 不会同时非 null：Store 内调用时 properties 为 null；\n     * {@code PullRequestHoldService} 内调用时 msgBuffer 为 null。\n     *\n     * @param msgBuffer CommitLog 消息缓冲区，Store 外可能为 null\n     * @param properties 消息属性，为 null 时需自行从 buffer 解码\n     */",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/MessageStoreStateMachine.java": [
        (
            "public class MessageStoreStateMachine {",
            "/**\n * MessageStore 生命周期状态机：加载、恢复、运行与关闭各阶段单向流转。\n */\npublic class MessageStoreStateMachine {",
        ),
        (
            "    protected final Logger log;",
            "    /** 状态变更日志记录器。 */\n    protected final Logger log;",
        ),
        (
            "    private MessageStoreState currentState;",
            "    /** 当前状态。 */\n    private MessageStoreState currentState;",
        ),
        (
            "    private long lastStateChangeTimestamp;",
            "    /** 上次状态变更时间戳。 */\n    private long lastStateChangeTimestamp;",
        ),
        (
            "    private final long startTimestamp;",
            "    /** 状态机启动时间戳。 */\n    private final long startTimestamp;",
        ),
        (
            "    public enum MessageStoreState {",
            "    /** MessageStore 各生命周期阶段，order 数值越大表示越靠后。 */\n    public enum MessageStoreState {",
        ),
        (
            "        INIT(0),",
            "        /** 初始状态。 */\n        INIT(0),",
        ),
        (
            "        LOAD_BEGIN(10),",
            "        /** 开始加载存储文件。 */\n        LOAD_BEGIN(10),",
        ),
        (
            "        LOAD_COMMITLOG_OK(11),",
            "        /** CommitLog 加载完成。 */\n        LOAD_COMMITLOG_OK(11),",
        ),
        (
            "        LOAD_CONSUME_QUEUE_OK(12),",
            "        /** ConsumeQueue 加载完成。 */\n        LOAD_CONSUME_QUEUE_OK(12),",
        ),
        (
            "        LOAD_COMPACTION_OK(13),",
            "        /** 压缩索引加载完成。 */\n        LOAD_COMPACTION_OK(13),",
        ),
        (
            "        LOAD_INDEX_OK(14),",
            "        /** 索引文件加载完成。 */\n        LOAD_INDEX_OK(14),",
        ),
        (
            "        RECOVER_BEGIN(20),",
            "        /** 开始恢复。 */\n        RECOVER_BEGIN(20),",
        ),
        (
            "        RECOVER_CONSUME_QUEUE_OK(21),",
            "        /** ConsumeQueue 恢复完成。 */\n        RECOVER_CONSUME_QUEUE_OK(21),",
        ),
        (
            "        RECOVER_COMMITLOG_OK(22),",
            "        /** CommitLog 恢复完成。 */\n        RECOVER_COMMITLOG_OK(22),",
        ),
        (
            "        RECOVER_TOPIC_QUEUE_TABLE_OK(23),",
            "        /** Topic-Queue 映射表恢复完成。 */\n        RECOVER_TOPIC_QUEUE_TABLE_OK(23),",
        ),
        (
            "        RUNNING(30),",
            "        /** 正常运行，可读写。 */\n        RUNNING(30),",
        ),
        (
            "        SHUTDOWN_BEGIN(40),",
            "        /** 开始关闭。 */\n        SHUTDOWN_BEGIN(40),",
        ),
        (
            "        SHUTDOWN_OK(41);",
            "        /** 关闭完成。 */\n        SHUTDOWN_OK(41);",
        ),
        (
            "        public int getOrder() {",
            "        /** 返回状态顺序值。 */\n        public int getOrder() {",
        ),
        (
            "        public boolean isBefore(MessageStoreState storeState) {",
            "        /** 是否早于给定状态。 */\n        public boolean isBefore(MessageStoreState storeState) {",
        ),
        (
            "        public boolean isAfter(MessageStoreState storeState) {",
            "        /** 是否晚于给定状态。 */\n        public boolean isAfter(MessageStoreState storeState) {",
        ),
        (
            "    public MessageStoreStateMachine(Logger log) {",
            "    /** 构造状态机，初始为 INIT 并记录启动时间。 */\n    public MessageStoreStateMachine(Logger log) {",
        ),
        (
            "    public void transitTo(MessageStoreState newState) {",
            "    /** 迁移到新状态（默认成功）。 */\n    public void transitTo(MessageStoreState newState) {",
        ),
        (
            "    public void transitTo(MessageStoreState newState, boolean success) {",
            "    /** 迁移到新状态，success 为 false 时仅记日志不更新 currentState。 */\n    public void transitTo(MessageStoreState newState, boolean success) {",
        ),
        (
            "    public MessageStoreState getCurrentState() {",
            "    /** 返回当前状态。 */\n    public MessageStoreState getCurrentState() {",
        ),
        (
            "    public long getTotalRunningTimeMs() {",
            "    /** 自启动以来的总运行毫秒数。 */\n    public long getTotalRunningTimeMs() {",
        ),
        (
            "    public long getCurrentStateRunningTimeMs() {",
            "    /** 当前状态已持续毫秒数。 */\n    public long getCurrentStateRunningTimeMs() {",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/MultiPathMappedFileQueue.java": [
        (
            "public class MultiPathMappedFileQueue extends MappedFileQueue {",
            "/**\n * 多路径 MappedFile 队列：CommitLog 可分布在多个磁盘路径，创建与加载时轮询选取。\n */\npublic class MultiPathMappedFileQueue extends MappedFileQueue {",
        ),
        (
            "    private final MessageStoreConfig config;",
            "    /** 消息存储配置（含多路径与只读路径）。 */\n    private final MessageStoreConfig config;",
        ),
        (
            "    private final Supplier<Set<String>> fullStorePathsSupplier;",
            "    /** 磁盘空间已满路径集合供应器，创建文件时排除。 */\n    private final Supplier<Set<String>> fullStorePathsSupplier;",
        ),
        (
            "    public MultiPathMappedFileQueue(MessageStoreConfig messageStoreConfig, int mappedFileSize,\n        AllocateMappedFileService allocateMappedFileService,\n        Supplier<Set<String>> fullStorePathsSupplier) {",
            "    /** 构造多路径队列（无 RunningFlags）。 */\n    public MultiPathMappedFileQueue(MessageStoreConfig messageStoreConfig, int mappedFileSize,\n        AllocateMappedFileService allocateMappedFileService,\n        Supplier<Set<String>> fullStorePathsSupplier) {",
        ),
        (
            "    public MultiPathMappedFileQueue(MessageStoreConfig messageStoreConfig, int mappedFileSize,\n                                    AllocateMappedFileService allocateMappedFileService,\n                                    Supplier<Set<String>> fullStorePathsSupplier, RunningFlags runningFlags) {",
            "    /** 构造多路径队列并指定 RunningFlags。 */\n    public MultiPathMappedFileQueue(MessageStoreConfig messageStoreConfig, int mappedFileSize,\n                                    AllocateMappedFileService allocateMappedFileService,\n                                    Supplier<Set<String>> fullStorePathsSupplier, RunningFlags runningFlags) {",
        ),
        (
            "    private Set<String> getPaths() {",
            "    /** 解析可写 CommitLog 存储路径集合。 */\n    private Set<String> getPaths() {",
        ),
        (
            "    private Set<String> getReadonlyPaths() {",
            "    /** 解析只读 CommitLog 路径集合。 */\n    private Set<String> getReadonlyPaths() {",
        ),
        (
            "    @Override\n    public boolean load() {",
            "    /** 从所有可写与只读路径加载 MappedFile。 */\n    @Override\n    public boolean load() {",
        ),
        (
            "        //do not create file in readonly store path.",
            "        // 不在只读路径上创建新文件",
        ),
        (
            "        //do not create file is space is nearly full.",
            "        // 磁盘空间将满的路径不创建新文件",
        ),
        (
            "        //if no store path left, fall back to writable store path.",
            "        // 若无可用路径则回退到可写路径",
        ),
        (
            "    @Override\n    public MappedFile tryCreateMappedFile(long createOffset) {",
            "    /** 按 fileIdx 轮询选取路径并创建 MappedFile。 */\n    @Override\n    public MappedFile tryCreateMappedFile(long createOffset) {",
        ),
        (
            "    @Override\n    public void destroy() {",
            "    /** 销毁所有 MappedFile 并清空各路径目录。 */\n    @Override\n    public void destroy() {",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/PutMessageContext.java": [
        (
            "public class PutMessageContext {",
            "/**\n * 批量写消息上下文：关联 Topic-Queue 键、物理位置数组与批次大小。\n */\npublic class PutMessageContext {",
        ),
        (
            "    private String topicQueueTableKey;",
            "    /** Topic 与 Queue 组合键。 */\n    private String topicQueueTableKey;",
        ),
        (
            "    private long[] phyPos;",
            "    /** 各条消息在 CommitLog 中的物理偏移数组。 */\n    private long[] phyPos;",
        ),
        (
            "    private int batchSize;",
            "    /** 本批次消息条数。 */\n    private int batchSize;",
        ),
        (
            "    public PutMessageContext(String topicQueueTableKey) {",
            "    /** 指定 Topic-Queue 键构造上下文。 */\n    public PutMessageContext(String topicQueueTableKey) {",
        ),
        (
            "    public String getTopicQueueTableKey() {",
            "    /** 返回 Topic-Queue 键。 */\n    public String getTopicQueueTableKey() {",
        ),
        (
            "    public long[] getPhyPos() {",
            "    /** 返回物理偏移数组。 */\n    public long[] getPhyPos() {",
        ),
        (
            "    public void setPhyPos(long[] phyPos) {",
            "    /** 设置物理偏移数组。 */\n    public void setPhyPos(long[] phyPos) {",
        ),
        (
            "    public int getBatchSize() {",
            "    /** 返回批次大小。 */\n    public int getBatchSize() {",
        ),
        (
            "    public void setBatchSize(int batchSize) {",
            "    /** 设置批次大小。 */\n    public void setBatchSize(int batchSize) {",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/PutMessageLock.java": [
        (
            "/**\n * Used when trying to put message\n */",
            "/**\n * 写消息时使用的互斥锁抽象，由 ReentrantLock 或自旋锁实现。\n */",
        ),
        (
            "    void lock();",
            "    /** 获取写锁。 */\n    void lock();",
        ),
        (
            "    void unlock();",
            "    /** 释放写锁。 */\n    void unlock();",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/PutMessageReentrantLock.java": [
        (
            "/**\n * Exclusive lock implementation to put message\n */",
            "/**\n * 写消息可重入互斥锁实现，适用于竞争较激烈的场景。\n */",
        ),
        (
            "    private ReentrantLock putMessageNormalLock = new ReentrantLock(); // NonfairSync",
            "    /** 非公平可重入锁。 */\n    private ReentrantLock putMessageNormalLock = new ReentrantLock(); // NonfairSync",
        ),
        (
            "    @Override\n    public void lock() {",
            "    /** 阻塞获取锁。 */\n    @Override\n    public void lock() {",
        ),
        (
            "    @Override\n    public void unlock() {",
            "    /** 释放锁。 */\n    @Override\n    public void unlock() {",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/PutMessageResult.java": [
        (
            "public class PutMessageResult {",
            "/**\n * 写消息结果：封装 PutMessageStatus、AppendMessageResult 及是否远程写入标志。\n */\npublic class PutMessageResult {",
        ),
        (
            "    private PutMessageStatus putMessageStatus;",
            "    /** 写消息状态码。 */\n    private PutMessageStatus putMessageStatus;",
        ),
        (
            "    private AppendMessageResult appendMessageResult;",
            "    /** CommitLog 追加结果。 */\n    private AppendMessageResult appendMessageResult;",
        ),
        (
            "    private boolean remotePut = false;",
            "    /** 是否为远程（Proxy）写入路径。 */\n    private boolean remotePut = false;",
        ),
        (
            "    public PutMessageResult(PutMessageStatus putMessageStatus, AppendMessageResult appendMessageResult) {",
            "    /** 本地写入结果构造。 */\n    public PutMessageResult(PutMessageStatus putMessageStatus, AppendMessageResult appendMessageResult) {",
        ),
        (
            "    public PutMessageResult(PutMessageStatus putMessageStatus, AppendMessageResult appendMessageResult,\n        boolean remotePut) {",
            "    /** 指定是否远程写入的构造。 */\n    public PutMessageResult(PutMessageStatus putMessageStatus, AppendMessageResult appendMessageResult,\n        boolean remotePut) {",
        ),
        (
            "    public boolean isOk() {",
            "    /** 是否视为写入成功（含刷盘/同步超时等可接受状态）。 */\n    public boolean isOk() {",
        ),
        (
            "    public AppendMessageResult getAppendMessageResult() {",
            "    /** 返回追加结果。 */\n    public AppendMessageResult getAppendMessageResult() {",
        ),
        (
            "    public PutMessageStatus getPutMessageStatus() {",
            "    /** 返回写消息状态。 */\n    public PutMessageStatus getPutMessageStatus() {",
        ),
        (
            "    public boolean isRemotePut() {",
            "    /** 是否远程写入。 */\n    public boolean isRemotePut() {",
        ),
        (
            "    @Override\n    public String toString() {",
            "    /** 返回调试字符串。 */\n    @Override\n    public String toString() {",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/PutMessageSpinLock.java": [
        (
            "/**\n * Spin lock Implementation to put message, suggest using this with low race conditions\n */",
            "/**\n * 写消息自旋锁实现，适用于竞争较低的轻量场景。\n */",
        ),
        (
            "    //true: Can lock, false : in lock.",
            "    // true 表示可获取锁，false 表示已锁定",
        ),
        (
            "    private AtomicBoolean putMessageSpinLock = new AtomicBoolean(true);",
            "    /** 自旋锁状态位。 */\n    private AtomicBoolean putMessageSpinLock = new AtomicBoolean(true);",
        ),
        (
            "    @Override\n    public void lock() {",
            "    /** CAS 自旋直到获取锁。 */\n    @Override\n    public void lock() {",
        ),
        (
            "    @Override\n    public void unlock() {",
            "    /** CAS 释放锁。 */\n    @Override\n    public void unlock() {",
        ),
    ],
}
