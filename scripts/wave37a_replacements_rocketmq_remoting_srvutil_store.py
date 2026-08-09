"""Chinese JavaDoc replacements for RocketMQ wave37a remoting/srvutil/store [0:15]."""

R: dict[str, list[tuple[str, str]]] = {
    "remoting/src/main/java/org/apache/rocketmq/remoting/rpc/TopicRequestHeader.java": [
        (
            "public abstract class TopicRequestHeader extends RpcRequestHeader {",
            "/**\n * 带 Topic 字段的 RPC 请求头抽象基类，支持逻辑标识 lo。\n */\npublic abstract class TopicRequestHeader extends RpcRequestHeader {",
        ),
        (
            "    //logical\n    protected Boolean lo;",
            "    /** 逻辑标识（logical），用于区分同名 Topic 的不同逻辑视图。 */\n    protected Boolean lo;",
        ),
        (
            "    public abstract String getTopic();",
            "    /** 返回 Topic 名称。 */\n    public abstract String getTopic();",
        ),
        (
            "    public abstract void setTopic(String topic);",
            "    /** 设置 Topic 名称。 */\n    public abstract void setTopic(String topic);",
        ),
        (
            "    public Boolean getLo() {",
            "    /** 返回逻辑标识 lo。 */\n    public Boolean getLo() {",
        ),
        (
            "    public void setLo(Boolean lo) {",
            "    /** 设置逻辑标识 lo。 */\n    public void setLo(Boolean lo) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/rpchook/DynamicalExtFieldRPCHook.java": [
        (
            "public class DynamicalExtFieldRPCHook implements RPCHook {",
            "/**\n * 动态扩展字段 RPC 钩子：从系统属性或环境变量读取 Zone 信息并注入请求。\n */\npublic class DynamicalExtFieldRPCHook implements RPCHook {",
        ),
        (
            "    @Override\n    public void doBeforeRequest(String remoteAddr, RemotingCommand request) {",
            "    /** 请求发出前：若配置了 Zone 名称/模式，则写入 RemotingCommand 扩展字段。 */\n    @Override\n    public void doBeforeRequest(String remoteAddr, RemotingCommand request) {",
        ),
        (
            "        String zoneName = System.getProperty(MixAll.ROCKETMQ_ZONE_PROPERTY, System.getenv(MixAll.ROCKETMQ_ZONE_ENV));",
            "        // 优先读 JVM 属性，否则读环境变量\n        String zoneName = System.getProperty(MixAll.ROCKETMQ_ZONE_PROPERTY, System.getenv(MixAll.ROCKETMQ_ZONE_ENV));",
        ),
        (
            "    @Override\n    public void doAfterResponse(String remoteAddr, RemotingCommand request, RemotingCommand response) {",
            "    /** 响应返回后：本钩子无需处理，留空实现。 */\n    @Override\n    public void doAfterResponse(String remoteAddr, RemotingCommand request, RemotingCommand response) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/rpchook/StreamTypeRPCHook.java": [
        (
            "public class StreamTypeRPCHook implements RPCHook {",
            "/**\n * 流式请求 RPC 钩子：在请求扩展字段中标记 RequestType.STREAM。\n */\npublic class StreamTypeRPCHook implements RPCHook {",
        ),
        (
            "    @Override\n    public void doBeforeRequest(String remoteAddr, RemotingCommand request) {",
            "    /** 请求发出前：写入 REQ_T 扩展字段，值为 STREAM 类型码。 */\n    @Override\n    public void doBeforeRequest(String remoteAddr, RemotingCommand request) {",
        ),
        (
            "    @Override\n    public void doAfterResponse(String remoteAddr, RemotingCommand request,\n        RemotingCommand response) {",
            "    /** 响应返回后：本钩子无需处理，留空实现。 */\n    @Override\n    public void doAfterResponse(String remoteAddr, RemotingCommand request,\n        RemotingCommand response) {",
        ),
    ],
    "srvutil/src/main/java/org/apache/rocketmq/srvutil/FileWatchService.java": [
        (
            "public class FileWatchService extends LifecycleAwareServiceThread {",
            "/**\n * 文件变更监听服务：周期性计算 MD5，哈希变化时回调 Listener。\n * 常用于证书/配置文件热更新场景。\n */\npublic class FileWatchService extends LifecycleAwareServiceThread {",
        ),
        (
            "    private static final int DEFAULT_WATCH_INTERVAL = 500;",
            "    /** 默认轮询间隔（毫秒）。 */\n    private static final int DEFAULT_WATCH_INTERVAL = 500;",
        ),
        (
            "    private final Map<String, String> currentHash = new HashMap<>();",
            "    /** 文件路径到上次 MD5 摘要的映射。 */\n    private final Map<String, String> currentHash = new HashMap<>();",
        ),
        (
            "    private final Listener listener;",
            "    /** 文件变更回调。 */\n    private final Listener listener;",
        ),
        (
            "    private final int watchInterval;",
            "    /** 轮询间隔（毫秒）。 */\n    private final int watchInterval;",
        ),
        (
            "    public FileWatchService(final String[] watchFiles, final Listener listener) throws Exception {",
            "    /** 使用默认轮询间隔构造监听服务。 */\n    public FileWatchService(final String[] watchFiles, final Listener listener) throws Exception {",
        ),
        (
            "    @Override\n    public String getServiceName() {",
            "    /** 返回服务线程名称。 */\n    @Override\n    public String getServiceName() {",
        ),
        (
            "    @Override\n    public void run0() {",
            "    /** 主循环：定时比对 MD5，变化时触发 onChanged。 */\n    @Override\n    public void run0() {",
        ),
        (
            "     * Note: we ignore DELETE event on purpose. This is useful when application renew CA file.\n     * When the operator delete/rename the old CA file and copy a new one, this ensures the old CA file is used during\n     * the operation.\n     * <p>\n     * As we know exactly what to do when file does not exist or when IO exception is raised, there is no need to\n     * propagate the exception up.\n     *\n     * @param filePath Absolute path of the file to calculate its MD5 digest.\n     * @return Hash of the file content if exists; empty string otherwise.",
            "     * 注意：故意忽略 DELETE 事件，便于证书轮换时仍沿用旧文件哈希。\n     * 文件不存在或 IO 异常时复用上次哈希，不向上抛异常。\n     *\n     * @param filePath 待计算 MD5 的文件绝对路径\n     * @return 文件内容哈希；不存在时返回空串",
        ),
        (
            "            // Reuse previous hash result\n            return currentHash.getOrDefault(filePath, \"\");",
            "            // 复用上次哈希，避免短暂缺失导致误报\n            return currentHash.getOrDefault(filePath, \"\");",
        ),
        (
            "    public interface Listener {",
            "    /** 文件变更回调接口。 */\n    public interface Listener {",
        ),
        (
            "         * Will be called when the target files are changed\n         *\n         * @param path the changed file path",
            "         * 目标文件内容变更时调用\n         *\n         * @param path 变更文件路径",
        ),
    ],
    "srvutil/src/main/java/org/apache/rocketmq/srvutil/ServerUtil.java": [
        (
            "public class ServerUtil {",
            "/**\n * 服务端命令行工具：构建通用选项、解析参数并转为 Properties。\n */\npublic class ServerUtil {",
        ),
        (
            "    public static Options buildCommandlineOptions(final Options options) {",
            "    /** 向 Options 追加 -h/--help 与 -n/--namesrvAddr 等通用选项。 */\n    public static Options buildCommandlineOptions(final Options options) {",
        ),
        (
            '        Option opt = new Option("h", "help", false, "Print help");',
            '        Option opt = new Option("h", "help", false, "Print help");  // 打印帮助',
        ),
        (
            "                \"Name server address list, eg: '192.168.0.1:9876;192.168.0.2:9876'\");",
            "                \"Name server address list, eg: '192.168.0.1:9876;192.168.0.2:9876'\");  // NameServer 地址列表",
        ),
        (
            "    public static CommandLine parseCmdLine(final String appName, String[] args, Options options,\n        CommandLineParser parser) {",
            "    /** 解析命令行；若含 -h 则打印帮助并 exit(0)，解析失败则 exit(1)。 */\n    public static CommandLine parseCmdLine(final String appName, String[] args, Options options,\n        CommandLineParser parser) {",
        ),
        (
            "    public static void printCommandLineHelp(final String appName, final Options options) {",
            "    /** 打印命令行帮助信息。 */\n    public static void printCommandLineHelp(final String appName, final Options options) {",
        ),
        (
            "    public static Properties commandLine2Properties(final CommandLine commandLine) {",
            "    /** 将已解析的命令行选项转为 Properties（longOpt 为键）。 */\n    public static Properties commandLine2Properties(final CommandLine commandLine) {",
        ),
    ],
    "srvutil/src/main/java/org/apache/rocketmq/srvutil/ShutdownHookThread.java": [
        (
            "/**\n * {@link ShutdownHookThread} is the standard hook for filtersrv and namesrv modules.\n * Through {@link Callable} interface, this hook can customization operations in anywhere.\n */",
            "/**\n * filtersrv 与 namesrv 模块的标准 JVM 关闭钩子线程。\n * 通过 {@link Callable} 回调可在任意位置定制关闭逻辑。\n */",
        ),
        (
            "    private volatile boolean hasShutdown = false;",
            "    /** 是否已执行过关闭逻辑（防重复）。 */\n    private volatile boolean hasShutdown = false;",
        ),
        (
            "    private AtomicInteger shutdownTimes = new AtomicInteger(0);",
            "    /** 钩子被调用次数计数。 */\n    private AtomicInteger shutdownTimes = new AtomicInteger(0);",
        ),
        (
            "    private final Logger log;",
            "    /** 日志实例。 */\n    private final Logger log;",
        ),
        (
            "    private final Callable callback;",
            "    /** 关闭时执行的回调。 */\n    private final Callable callback;",
        ),
        (
            "     * Create the standard hook thread, with a call back, by using {@link Callable} interface.\n     *\n     * @param log The log instance is used in hook thread.\n     * @param callback The call back function.",
            "     * 构造标准关闭钩子线程。\n     *\n     * @param log 钩子线程使用的日志\n     * @param callback 关闭回调函数",
        ),
        (
            "     * Thread run method.\n     * Invoke when the jvm shutdown.\n     * 1. count the invocation times.\n     * 2. execute the {@link ShutdownHookThread#callback}, and time it.",
            "     * JVM 关闭时执行：\n     * 1. 累计调用次数；\n     * 2. 执行 {@link ShutdownHookThread#callback} 并记录耗时。",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/AllocateMappedFileService.java": [
        (
            "/**\n * Create MappedFile in advance\n */",
            "/**\n * 预分配 MappedFile 的后台服务：异步创建 mmap 文件以降低写入延迟。\n */",
        ),
        (
            "    private static int waitTimeOut = 1000 * 5;",
            "    /** 等待预分配完成的超时时间（毫秒）。 */\n    private static int waitTimeOut = 1000 * 5;",
        ),
        (
            "    private ConcurrentMap<String, AllocateRequest> requestTable =\n        new ConcurrentHashMap<>();",
            "    /** 文件路径到分配请求的映射表。 */\n    private ConcurrentMap<String, AllocateRequest> requestTable =\n        new ConcurrentHashMap<>();",
        ),
        (
            "    private PriorityBlockingQueue<AllocateRequest> requestQueue =\n        new PriorityBlockingQueue<>();",
            "    /** 待处理的预分配请求优先队列。 */\n    private PriorityBlockingQueue<AllocateRequest> requestQueue =\n        new PriorityBlockingQueue<>();",
        ),
        (
            "    private volatile boolean hasException = false;",
            "    /** 服务是否发生过 IO 等异常。 */\n    private volatile boolean hasException = false;",
        ),
        (
            "    private DefaultMessageStore messageStore;",
            "    /** 所属 MessageStore。 */\n    private DefaultMessageStore messageStore;",
        ),
        (
            "    private PreprocessHandler preprocessHandler;",
            "    /** 可选的外部预处理器。 */\n    private PreprocessHandler preprocessHandler;",
        ),
        (
            "     * Set preprocess handler for external extension\n     *\n     * @param preprocessHandler the preprocess handler",
            "     * 设置外部扩展用的预处理器\n     *\n     * @param preprocessHandler 预处理器实例",
        ),
        (
            "    public MappedFile putRequestAndReturnMappedFile(String nextFilePath, String nextNextFilePath, int fileSize) {",
            "    /** 提交当前与下一个文件的预分配请求，阻塞等待当前文件 mmap 就绪后返回。 */\n    public MappedFile putRequestAndReturnMappedFile(String nextFilePath, String nextNextFilePath, int fileSize) {",
        ),
        (
            "        // Execute preprocess logic if handler is set",
            "        // 若配置了预处理器则先执行",
        ),
        (
            "                && BrokerRole.SLAVE != this.messageStore.getMessageStoreConfig().getBrokerRole()) { //if broker is slave, don't fast fail even no buffer in pool",
            "                && BrokerRole.SLAVE != this.messageStore.getMessageStoreConfig().getBrokerRole()) { // 从节点即使缓冲池不足也不快速失败",
        ),
        (
            "     * Only interrupted by the external thread, will return false",
            "     * 仅被外部线程中断时返回 false",
        ),
        (
            "                // pre write mappedFile",
            "                // 预热 mappedFile 页缓存",
        ),
        (
            "     * Preprocess handler interface for external extension",
            "     * 预分配前的外部扩展预处理接口",
        ),
        (
            "         * Preprocess before allocating mapped file\n         *\n         * @param nextFilePath the next file path\n         * @param nextNextFilePath the next next file path\n         * @param fileSize the file size",
            "         * 分配 mapped 文件前的预处理\n         *\n         * @param nextFilePath 即将使用的文件路径\n         * @param nextNextFilePath 再下一个文件路径\n         * @param fileSize 文件大小",
        ),
        (
            "        // Full file path\n        private String filePath;",
            "        /** 完整文件路径。 */\n        private String filePath;",
        ),
        (
            "        private int fileSize;",
            "        /** 预分配文件大小。 */\n        private int fileSize;",
        ),
        (
            "        private CountDownLatch countDownLatch = new CountDownLatch(1);",
            "        /** 分配完成信号量。 */\n        private CountDownLatch countDownLatch = new CountDownLatch(1);",
        ),
        (
            "        private volatile MappedFile mappedFile = null;",
            "        /** 分配完成的 MappedFile（volatile 保证可见性）。 */\n        private volatile MappedFile mappedFile = null;",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/AppendMessageCallback.java": [
        (
            "/**\n * Write messages callback interface\n */",
            "/**\n * 消息追加写入 CommitLog 的回调接口。\n */",
        ),
        (
            "     * After message serialization, write MappedByteBuffer\n     *\n     * @return How many bytes to write",
            "     * 单条消息序列化后写入 MappedByteBuffer\n     *\n     * @return 实际写入字节数等信息",
        ),
        (
            "     * After batched message serialization, write MappedByteBuffer\n     *\n     * @param messageExtBatch, backed up by a byte array\n     * @return How many bytes to write",
            "     * 批量消息序列化后写入 MappedByteBuffer\n     *\n     * @param messageExtBatch 由字节数组支撑的批量消息\n     * @return 实际写入字节数等信息",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/AppendMessageResult.java": [
        (
            "/**\n * When write a message to the commit log, returns results\n */",
            "/**\n * 向 CommitLog 追加消息后的写入结果封装。\n */",
        ),
        (
            "    // Return code\n    private AppendMessageStatus status;",
            "    /** 写入状态码。 */\n    private AppendMessageStatus status;",
        ),
        (
            "    // Where to start writing\n    private long wroteOffset;",
            "    /** 本次写入在文件中的起始物理偏移。 */\n    private long wroteOffset;",
        ),
        (
            "    // Write Bytes\n    private int wroteBytes;",
            "    /** 本次写入字节数。 */\n    private int wroteBytes;",
        ),
        (
            "    // Message ID\n    private String msgId;",
            "    /** 消息 ID（可能延迟由 supplier 生成）。 */\n    private String msgId;",
        ),
        (
            "    private Supplier<String> msgIdSupplier;",
            "    /** 延迟生成 msgId 的供应器。 */\n    private Supplier<String> msgIdSupplier;",
        ),
        (
            "    // Message storage timestamp\n    private long storeTimestamp;",
            "    /** 消息存储时间戳。 */\n    private long storeTimestamp;",
        ),
        (
            "    // Consume queue's offset(step by one)\n    private long logicsOffset;",
            "    /** 对应 ConsumeQueue 逻辑偏移（逐条递增）。 */\n    private long logicsOffset;",
        ),
        (
            "    private long pagecacheRT = 0;",
            "    /** 页缓存相关耗时（纳秒或毫秒，取决于调用方）。 */\n    private long pagecacheRT = 0;",
        ),
        (
            "    private int msgNum = 1;",
            "    /** 本次写入包含的消息条数（批量时为多条）。 */\n    private int msgNum = 1;",
        ),
        (
            "    public boolean isOk() {",
            "    /** 是否写入成功（status 为 PUT_OK）。 */\n    public boolean isOk() {",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/AppendMessageStatus.java": [
        (
            "/**\n * When write a message to the commit log, returns code\n */",
            "/**\n * 向 CommitLog 写入消息时返回的状态码枚举。\n */",
        ),
        (
            "    PUT_OK,",
            "    /** 写入成功。 */\n    PUT_OK,",
        ),
        (
            "    END_OF_FILE,",
            "    /** 当前文件剩余空间不足。 */\n    END_OF_FILE,",
        ),
        (
            "    MESSAGE_SIZE_EXCEEDED,",
            "    /** 消息体超过允许的最大尺寸。 */\n    MESSAGE_SIZE_EXCEEDED,",
        ),
        (
            "    PROPERTIES_SIZE_EXCEEDED,",
            "    /** 消息属性总大小超限。 */\n    PROPERTIES_SIZE_EXCEEDED,",
        ),
        (
            "    UNKNOWN_ERROR,",
            "    /** 未知错误。 */\n    UNKNOWN_ERROR,",
        ),
        (
            "    ROCKSDB_ERROR,",
            "    /** RocksDB 存储层错误。 */\n    ROCKSDB_ERROR,",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/CommitLogDispatchStore.java": [
        (
            "/**\n * Interface for stores that require commitlog dispatch and recovery. Each store implementing this interface should\n * register itself in the commitlog when loading. This abstraction allows the commitlog recovery process to\n * automatically consider all registered stores without needing to modify the recovery logic when adding a new store.\n */",
            "/**\n * 需要参与 CommitLog 分发与恢复的存储抽象。\n * 实现类在加载时向 CommitLog 注册，恢复流程可自动遍历所有已注册存储。\n */",
        ),
        (
            "     * Get the dispatch offset in the store. Messages whose phyOffset larger than this offset need to be dispatched. The\n     * dispatch offset is only used during recovery.\n     *\n     * @param recoverNormally true if broker exited normally last time (normal recovery), false for abnormal recovery\n     * @return the dispatch phyOffset, or null if the store is not enabled or has no valid offset\n     * @throws RocksDBException if there is an error accessing RocksDB storage",
            "     * 获取本存储的分发起始物理偏移；大于该偏移的消息需重新分发（仅恢复阶段使用）。\n     *\n     * @param recoverNormally 上次 Broker 是否正常退出\n     * @return 分发 phyOffset；未启用或无有效偏移时返回 null\n     * @throws RocksDBException 访问 RocksDB 失败",
        ),
        (
            "     * Used to determine whether to start doDispatch from this commitLog mappedFile.\n     *\n     * @param phyOffset the offset of the first message in this commitlog mappedFile\n     * @param storeTimestamp the timestamp of the first message in this commitlog mappedFile\n     * @param recoverNormally whether this is a normal recovery\n     * @return whether to start recovering from this MappedFile\n     * @throws RocksDBException if there is an error accessing RocksDB storage",
            "     * 判断是否应从该 CommitLog MappedFile 开始执行 doDispatch。\n     *\n     * @param phyOffset 该文件首条消息的物理偏移\n     * @param storeTimestamp 该文件首条消息的存储时间戳\n     * @param recoverNormally 是否为正常恢复\n     * @return 是否从该 MappedFile 开始恢复\n     * @throws RocksDBException 访问 RocksDB 失败",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/CommitLogDispatcher.java": [
        (
            "/**\n * Dispatcher of commit log.\n */",
            "/**\n * CommitLog 消息分发器：将已持久化消息派发到 ConsumeQueue、索引等结构。\n */",
        ),
        (
            "     *  Dispatch messages from store to build consume queues, indexes, and filter data\n     * @param request dispatch message request\n     * @throws RocksDBException only in rocksdb mode",
            "     * 根据 DispatchRequest 构建 ConsumeQueue、索引与过滤数据\n     * @param request 分发请求\n     * @throws RocksDBException 仅 RocksDB 模式下可能抛出",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/CompactionAppendMsgCallback.java": [
        (
            "public interface CompactionAppendMsgCallback {",
            "/**\n * 消息压缩场景下的追加写入回调：将源缓冲区内容写入目标 MappedByteBuffer。\n */\npublic interface CompactionAppendMsgCallback {",
        ),
        (
            "    AppendMessageResult doAppend(ByteBuffer bbDest, long fileFromOffset, int maxBlank, ByteBuffer bbSrc);",
            "    /** 将 bbSrc 内容追加到 bbDest，返回写入结果。 */\n    AppendMessageResult doAppend(ByteBuffer bbDest, long fileFromOffset, int maxBlank, ByteBuffer bbSrc);",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/DefaultMessageFilter.java": [
        (
            "public class DefaultMessageFilter implements MessageFilter {",
            "/**\n * 默认消息过滤器：基于 SubscriptionData 在 ConsumeQueue 层按 Tag 码过滤。\n * CommitLog 层默认全部匹配。\n */\npublic class DefaultMessageFilter implements MessageFilter {",
        ),
        (
            "    private SubscriptionData subscriptionData;",
            "    /** 订阅表达式与 Tag 集合。 */\n    private SubscriptionData subscriptionData;",
        ),
        (
            "    public DefaultMessageFilter(final SubscriptionData subscriptionData) {",
            "    /** 使用给定订阅数据构造过滤器。 */\n    public DefaultMessageFilter(final SubscriptionData subscriptionData) {",
        ),
        (
            "    @Override\n    public boolean isMatchedByConsumeQueue(Long tagsCode, ConsumeQueueExt.CqExtUnit cqExtUnit) {",
            "    /** 按 ConsumeQueue 中的 tagsCode 判断是否匹配订阅。 */\n    @Override\n    public boolean isMatchedByConsumeQueue(Long tagsCode, ConsumeQueueExt.CqExtUnit cqExtUnit) {",
        ),
        (
            "    @Override\n    public boolean isMatchedByCommitLog(ByteBuffer msgBuffer, Map<String, String> properties) {",
            "    /** CommitLog 层默认不过滤，恒为 true。 */\n    @Override\n    public boolean isMatchedByCommitLog(ByteBuffer msgBuffer, Map<String, String> properties) {",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/DispatchRequest.java": [
        (
            "public class DispatchRequest {",
            "/**\n * CommitLog 分发请求：携带单条（或批量）消息在存储层的元数据。\n * 供 ReputMessageService 与各 Dispatcher 构建 ConsumeQueue/索引。\n */\npublic class DispatchRequest {",
        ),
        (
            "    private final String topic;",
            "    /** Topic 名称。 */\n    private final String topic;",
        ),
        (
            "    private final int queueId;",
            "    /** 队列 ID。 */\n    private final int queueId;",
        ),
        (
            "    private final long commitLogOffset;",
            "    /** 消息在 CommitLog 中的物理偏移。 */\n    private final long commitLogOffset;",
        ),
        (
            "    private int msgSize;",
            "    /** 消息体大小（字节）。 */\n    private int msgSize;",
        ),
        (
            "    private final long tagsCode;",
            "    /** Tag 哈希码，用于 ConsumeQueue 过滤。 */\n    private final long tagsCode;",
        ),
        (
            "    private final long storeTimestamp;",
            "    /** 存储时间戳。 */\n    private final long storeTimestamp;",
        ),
        (
            "    private final long consumeQueueOffset;",
            "    /** 对应 ConsumeQueue 逻辑偏移。 */\n    private final long consumeQueueOffset;",
        ),
        (
            "    private final String keys;",
            "    /** 消息 Keys（可为空）。 */\n    private final String keys;",
        ),
        (
            "    private final boolean success;",
            "    /** 解析/构造是否成功。 */\n    private final boolean success;",
        ),
        (
            "    private final String uniqKey;",
            "    /** 消息唯一键（如 UNIQ_KEY 属性）。 */\n    private final String uniqKey;",
        ),
        (
            "    private final int sysFlag;",
            "    /** 系统标志位。 */\n    private final int sysFlag;",
        ),
        (
            "    private final long preparedTransactionOffset;",
            "    /** 事务消息 prepared 偏移（非事务为 0）。 */\n    private final long preparedTransactionOffset;",
        ),
        (
            "    private final Map<String, String> propertiesMap;",
            "    /** 消息用户属性映射。 */\n    private final Map<String, String> propertiesMap;",
        ),
        (
            "    private byte[] bitMap;",
            "    /** 可选位图（如 SQL92 过滤）。 */\n    private byte[] bitMap;",
        ),
        (
            "    private int bufferSize = -1;//the buffer size maybe larger than the msg size if the message is wrapped by something",
            "    /** 缓冲区大小（可能大于 msgSize，例如外层包装）。 */\n    private int bufferSize = -1;",
        ),
        (
            "    // for batch consume queue\n    private long  msgBaseOffset = -1;",
            "    /** 批量 ConsumeQueue 的起始逻辑偏移。 */\n    private long  msgBaseOffset = -1;",
        ),
        (
            "    private short batchSize = 1;",
            "    /** 批量消息条数。 */\n    private short batchSize = 1;",
        ),
        (
            "    private long nextReputFromOffset = -1;",
            "    /** 下次 Reput 起始物理偏移（-1 表示默认）。 */\n    private long nextReputFromOffset = -1;",
        ),
        (
            "    private String offsetId;",
            "    /** 偏移标识（扩展用途）。 */\n    private String offsetId;",
        ),
        (
            "    public boolean containsLMQ() {",
            "    /** 是否包含轻量级消息队列（LMQ）多路分发属性。 */\n    public boolean containsLMQ() {",
        ),
    ],
}
