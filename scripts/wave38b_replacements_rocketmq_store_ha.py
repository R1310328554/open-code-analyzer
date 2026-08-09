"""Chinese JavaDoc replacements for RocketMQ wave38b store/ha [15:30]."""

R: dict[str, list[tuple[str, str]]] = {
    "store/src/main/java/org/apache/rocketmq/store/config/StorePathConfigHelper.java": [
        (
            "public class StorePathConfigHelper {",
            "/**\n * 存储路径配置辅助类：根据根目录拼接消费队列、索引、事务等子路径。\n */\npublic class StorePathConfigHelper {",
        ),
        (
            "    public static String getStorePathConsumeQueue(final String rootDir) {",
            "    /** 返回消费队列（ConsumeQueue）存储目录。 */\n    public static String getStorePathConsumeQueue(final String rootDir) {",
        ),
        (
            "    public static String getStorePathConsumeQueueExt(final String rootDir) {",
            "    /** 返回扩展消费队列目录路径。 */\n    public static String getStorePathConsumeQueueExt(final String rootDir) {",
        ),
        (
            "    public static String getStorePathBatchConsumeQueue(final String rootDir) {",
            "    /** 返回批量消费队列目录路径。 */\n    public static String getStorePathBatchConsumeQueue(final String rootDir) {",
        ),
        (
            "    public static String getStorePathRocksDBConsumeQueue(final String rootDir) {",
            "    /** 返回基于 RocksDB 的消费队列目录路径。 */\n    public static String getStorePathRocksDBConsumeQueue(final String rootDir) {",
        ),
        (
            "    public static String getStorePathIndex(final String rootDir) {",
            "    /** 返回消息索引文件目录路径。 */\n    public static String getStorePathIndex(final String rootDir) {",
        ),
        (
            "    public static String getStoreCheckpoint(final String rootDir) {",
            "    /** 返回存储检查点（checkpoint）目录路径。 */\n    public static String getStoreCheckpoint(final String rootDir) {",
        ),
        (
            "    public static String getAbortFile(final String rootDir) {",
            "    /** 返回异常退出标记文件路径。 */\n    public static String getAbortFile(final String rootDir) {",
        ),
        (
            "    public static String getLockFile(final String rootDir) {",
            "    /** 返回存储实例锁文件路径。 */\n    public static String getLockFile(final String rootDir) {",
        ),
        (
            "    public static String getDelayOffsetStorePath(final String rootDir) {",
            "    /** 返回延迟消息偏移量持久化文件路径。 */\n    public static String getDelayOffsetStorePath(final String rootDir) {",
        ),
        (
            "    public static String getTranStateTableStorePath(final String rootDir) {",
            "    /** 返回事务状态表存储目录路径。 */\n    public static String getTranStateTableStorePath(final String rootDir) {",
        ),
        (
            "    public static String getTranRedoLogStorePath(final String rootDir) {",
            "    /** 返回事务重做日志（redo log）存储目录路径。 */\n    public static String getTranRedoLogStorePath(final String rootDir) {",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/exception/ConsumeQueueException.java": [
        (
            "public class ConsumeQueueException extends StoreException {",
            "/**\n * 消费队列相关异常：封装 ConsumeQueue 读写或索引操作失败。\n */\npublic class ConsumeQueueException extends StoreException {",
        ),
        (
            "    public ConsumeQueueException() {",
            "    /** 无参构造。 */\n    public ConsumeQueueException() {",
        ),
        (
            "    public ConsumeQueueException(String message) {",
            "    /** 以消息构造异常。 */\n    public ConsumeQueueException(String message) {",
        ),
        (
            "    public ConsumeQueueException(String message, Throwable cause) {",
            "    /** 以消息与原因构造异常。 */\n    public ConsumeQueueException(String message, Throwable cause) {",
        ),
        (
            "    public ConsumeQueueException(Throwable cause) {",
            "    /** 以原因构造异常。 */\n    public ConsumeQueueException(Throwable cause) {",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/exception/StoreException.java": [
        (
            "public class StoreException extends Exception {",
            "/**\n * 消息存储模块通用异常基类。\n */\npublic class StoreException extends Exception {",
        ),
        (
            "    public StoreException() {",
            "    /** 无参构造。 */\n    public StoreException() {",
        ),
        (
            "    public StoreException(String message) {",
            "    /** 以消息构造异常。 */\n    public StoreException(String message) {",
        ),
        (
            "    public StoreException(String message, Throwable cause) {",
            "    /** 以消息与原因构造异常。 */\n    public StoreException(String message, Throwable cause) {",
        ),
        (
            "    public StoreException(Throwable cause) {",
            "    /** 以原因构造异常。 */\n    public StoreException(Throwable cause) {",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/ha/DefaultHAService.java": [
        (
            "public class DefaultHAService implements HAService {",
            "/**\n * 默认主从同步（HA）服务实现：管理 Accept 监听、GroupTransfer 与 HAClient。\n */\npublic class DefaultHAService implements HAService {",
        ),
        (
            "    protected final AtomicInteger connectionCount = new AtomicInteger(0);",
            "    /** 当前 HA 连接数量。 */\n    protected final AtomicInteger connectionCount = new AtomicInteger(0);",
        ),
        (
            "    protected final List<HAConnection> connectionList = new LinkedList<>();",
            "    /** 活跃 HA 连接列表。 */\n    protected final List<HAConnection> connectionList = new LinkedList<>();",
        ),
        (
            "    protected AcceptSocketService acceptSocketService;",
            "    /** 接受从节点连接的 Socket 服务。 */\n    protected AcceptSocketService acceptSocketService;",
        ),
        (
            "    protected DefaultMessageStore defaultMessageStore;",
            "    /** 所属 MessageStore 实例。 */\n    protected DefaultMessageStore defaultMessageStore;",
        ),
        (
            "    protected WaitNotifyObject waitNotifyObject = new WaitNotifyObject();",
            "    /** 组提交等待/唤醒对象。 */\n    protected WaitNotifyObject waitNotifyObject = new WaitNotifyObject();",
        ),
        (
            "    protected AtomicLong push2SlaveMaxOffset = new AtomicLong(0);",
            "    /** 已推送到从节点的最大 CommitLog 偏移。 */\n    protected AtomicLong push2SlaveMaxOffset = new AtomicLong(0);",
        ),
        (
            "    protected GroupTransferService groupTransferService;",
            "    /** 组传输服务，等待从节点 ACK。 */\n    protected GroupTransferService groupTransferService;",
        ),
        (
            "    protected HAClient haClient;",
            "    /** 从节点侧 HA 客户端（主节点为 null）。 */\n    protected HAClient haClient;",
        ),
        (
            "    protected HAConnectionStateNotificationService haConnectionStateNotificationService;",
            "    /** HA 连接状态变更通知服务。 */\n    protected HAConnectionStateNotificationService haConnectionStateNotificationService;",
        ),
        (
            "    public DefaultHAService() {",
            "    /** 默认构造。 */\n    public DefaultHAService() {",
        ),
        (
            "    @Override\n    public void init(final DefaultMessageStore defaultMessageStore) throws IOException {",
            "    /** 初始化 HA 组件，须在其它方法之前调用。 */\n    @Override\n    public void init(final DefaultMessageStore defaultMessageStore) throws IOException {",
        ),
        (
            "    @Override\n    public void updateMasterAddress(final String newAddr) {",
            "    /** 更新主节点业务地址。 */\n    @Override\n    public void updateMasterAddress(final String newAddr) {",
        ),
        (
            "    @Override\n    public void updateHaMasterAddress(String newAddr) {",
            "    /** 更新主节点 HA 专用地址。 */\n    @Override\n    public void updateHaMasterAddress(String newAddr) {",
        ),
        (
            "    @Override\n    public void putRequest(final CommitLog.GroupCommitRequest request) {",
            "    /** 提交组提交请求至 GroupTransferService。 */\n    @Override\n    public void putRequest(final CommitLog.GroupCommitRequest request) {",
        ),
        (
            "    @Override\n    public boolean isSlaveOK(final long masterPutWhere) {",
            "    /** 判断从节点是否跟得上主节点写入进度。 */\n    @Override\n    public boolean isSlaveOK(final long masterPutWhere) {",
        ),
        (
            "    public void notifyTransferSome(final long offset) {",
            "    /** 更新 push2SlaveMaxOffset 并唤醒传输等待。 */\n    public void notifyTransferSome(final long offset) {",
        ),
        (
            "    @Override\n    public AtomicInteger getConnectionCount() {",
            "    /** 返回当前 HA 连接数。 */\n    @Override\n    public AtomicInteger getConnectionCount() {",
        ),
        (
            "    @Override\n    public void start() throws Exception {",
            "    /** 启动 Accept、GroupTransfer、状态通知及 HAClient。 */\n    @Override\n    public void start() throws Exception {",
        ),
        (
            "    public void addConnection(final HAConnection conn) {",
            "    /** 将新 HA 连接加入列表。 */\n    public void addConnection(final HAConnection conn) {",
        ),
        (
            "    public void removeConnection(final HAConnection conn) {",
            "    /** 移除连接并触发状态通知检查。 */\n    public void removeConnection(final HAConnection conn) {",
        ),
        (
            "    @Override\n    public void shutdown() {",
            "    /** 关闭 HA 全部子服务与连接。 */\n    @Override\n    public void shutdown() {",
        ),
        (
            "    public void destroyConnections() {",
            "    /** 关闭并清空所有 HA 连接。 */\n    public void destroyConnections() {",
        ),
        (
            "    public DefaultMessageStore getDefaultMessageStore() {",
            "    /** 返回关联的 MessageStore。 */\n    public DefaultMessageStore getDefaultMessageStore() {",
        ),
        (
            "    @Override\n    public WaitNotifyObject getWaitNotifyObject() {",
            "    /** 返回等待/唤醒对象。 */\n    @Override\n    public WaitNotifyObject getWaitNotifyObject() {",
        ),
        (
            "    @Override\n    public AtomicLong getPush2SlaveMaxOffset() {",
            "    /** 返回已推送至从节点的最大偏移。 */\n    @Override\n    public AtomicLong getPush2SlaveMaxOffset() {",
        ),
        (
            "    @Override\n    public int inSyncReplicasNums(final long masterPutWhere) {",
            "    /** 统计与主节点同步的副本数量（含主）。 */\n    @Override\n    public int inSyncReplicasNums(final long masterPutWhere) {",
        ),
        (
            "    protected boolean isInSyncSlave(final long masterPutWhere, HAConnection conn) {",
            "    /** 判断单个从连接是否在同步阈值内。 */\n    protected boolean isInSyncSlave(final long masterPutWhere, HAConnection conn) {",
        ),
        (
            "    @Override\n    public void putGroupConnectionStateRequest(HAConnectionStateNotificationRequest request) {",
            "    /** 注册 HA 连接状态等待请求。 */\n    @Override\n    public void putGroupConnectionStateRequest(HAConnectionStateNotificationRequest request) {",
        ),
        (
            "    @Override\n    public List<HAConnection> getConnectionList() {",
            "    /** 返回 HA 连接列表。 */\n    @Override\n    public List<HAConnection> getConnectionList() {",
        ),
        (
            "    @Override\n    public HAClient getHAClient() {",
            "    /** 返回 HA 客户端（从节点）。 */\n    @Override\n    public HAClient getHAClient() {",
        ),
        (
            "    @Override\n    public HARuntimeInfo getRuntimeInfo(long masterPutWhere) {",
            "    /** 收集主/从 HA 运行时指标。 */\n    @Override\n    public HARuntimeInfo getRuntimeInfo(long masterPutWhere) {",
        ),
        (
            "    class DefaultAcceptSocketService extends AcceptSocketService {",
            "    /** 默认 Accept 实现，创建 DefaultHAConnection。 */\n    class DefaultAcceptSocketService extends AcceptSocketService {",
        ),
        (
            "        @Override\n        protected HAConnection createConnection(SocketChannel sc) throws IOException {",
            "        /** 为新 Socket 创建 DefaultHAConnection。 */\n        @Override\n        protected HAConnection createConnection(SocketChannel sc) throws IOException {",
        ),
        (
            "    /**\n     * Listens to slave connections to create {@link HAConnection}.\n     */",
            "    /**\n     * 监听从节点连接并创建 {@link HAConnection}。\n     */",
        ),
        (
            "        private final SocketAddress socketAddressListen;",
            "        /** 监听绑定地址。 */\n        private final SocketAddress socketAddressListen;",
        ),
        (
            "        private ServerSocketChannel serverSocketChannel;",
            "        /** 服务端 Socket 通道。 */\n        private ServerSocketChannel serverSocketChannel;",
        ),
        (
            "        private Selector selector;",
            "        /** NIO 选择器。 */\n        private Selector selector;",
        ),
        (
            "        /**\n         * Starts listening to slave connections.\n         *\n         * @throws Exception If fails.\n         */",
            "        /**\n         * 开始在 HA 端口监听从节点连接。\n         *\n         * @throws Exception 绑定或注册失败时抛出\n         */",
        ),
        (
            "                log.info(\"OS picked up {} to listen for HA\", messageStoreConfig.getHaListenPort());",
            "                log.info(\"OS picked up {} to listen for HA\", messageStoreConfig.getHaListenPort());  // 操作系统自动分配 HA 监听端口",
        ),
        (
            "        /**\n         * Create ha connection\n         */",
            "        /**\n         * 为接受的 Socket 创建 HA 连接实例\n         */",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/ha/FlowMonitor.java": [
        (
            "public class FlowMonitor extends ServiceThread {",
            "/**\n * HA 传输流量监视器：每秒统计传输字节并支持流控配额计算。\n */\npublic class FlowMonitor extends ServiceThread {",
        ),
        (
            "    private final AtomicLong transferredByte = new AtomicLong(0L);",
            "    /** 当前秒内累计传输字节数。 */\n    private final AtomicLong transferredByte = new AtomicLong(0L);",
        ),
        (
            "    private volatile long transferredByteInSecond;",
            "    /** 上一秒完成的传输字节数快照。 */\n    private volatile long transferredByteInSecond;",
        ),
        (
            "    protected MessageStoreConfig messageStoreConfig;",
            "    /** 消息存储配置，含 HA 流控开关与上限。 */\n    protected MessageStoreConfig messageStoreConfig;",
        ),
        (
            "    public FlowMonitor(MessageStoreConfig messageStoreConfig) {",
            "    /** 注入 MessageStoreConfig。 */\n    public FlowMonitor(MessageStoreConfig messageStoreConfig) {",
        ),
        (
            "    @Override\n    public void run() {",
            "    /** 每秒重置计数并计算传输速率。 */\n    @Override\n    public void run() {",
        ),
        (
            "    public void calculateSpeed() {",
            "    /** 将累计值写入快照并清零计数器。 */\n    public void calculateSpeed() {",
        ),
        (
            "        // Flow control is not started at present",
            "        // 当前若启用流控则按配额计算本周期可传字节数",
        ),
        (
            "    public int canTransferMaxByteNum() {",
            "    /** 返回本周期尚可传输的最大字节数。 */\n    public int canTransferMaxByteNum() {",
        ),
        (
            "    public void addByteCountTransferred(long count) {",
            "    /** 累加已传输字节数。 */\n    public void addByteCountTransferred(long count) {",
        ),
        (
            "    public long getTransferredByteInSecond() {",
            "    /** 返回上一秒传输字节数。 */\n    public long getTransferredByteInSecond() {",
        ),
        (
            "    @Override\n    public String getServiceName() {",
            "    /** 返回服务线程名称。 */\n    @Override\n    public String getServiceName() {",
        ),
        (
            "    protected boolean isFlowControlEnable() {",
            "    /** 是否启用 HA 流控。 */\n    protected boolean isFlowControlEnable() {",
        ),
        (
            "    public long maxTransferByteInSecond() {",
            "    /** 返回每秒最大可传输字节配置值。 */\n    public long maxTransferByteInSecond() {",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/ha/GroupTransferService.java": [
        (
            "/**\n * GroupTransferService Service\n */",
            "/**\n * 组传输服务：等待从节点 ACK 指定偏移后唤醒组提交请求。\n */",
        ),
        (
            "    private final WaitNotifyObject notifyTransferObject = new WaitNotifyObject();",
            "    /** 传输进度通知用的等待对象。 */\n    private final WaitNotifyObject notifyTransferObject = new WaitNotifyObject();",
        ),
        (
            "    private final PutMessageSpinLock lock = new PutMessageSpinLock();",
            "    /** 保护请求双缓冲交换的自旋锁。 */\n    private final PutMessageSpinLock lock = new PutMessageSpinLock();",
        ),
        (
            "    private final DefaultMessageStore defaultMessageStore;",
            "    /** 所属 MessageStore。 */\n    private final DefaultMessageStore defaultMessageStore;",
        ),
        (
            "    private final HAService haService;",
            "    /** 关联的 HA 服务。 */\n    private final HAService haService;",
        ),
        (
            "    private volatile List<CommitLog.GroupCommitRequest> requestsWrite = new LinkedList<>();",
            "    /** 写入侧待处理组提交请求列表。 */\n    private volatile List<CommitLog.GroupCommitRequest> requestsWrite = new LinkedList<>();",
        ),
        (
            "    private volatile List<CommitLog.GroupCommitRequest> requestsRead = new LinkedList<>();",
            "    /** 读取侧正在处理的请求列表。 */\n    private volatile List<CommitLog.GroupCommitRequest> requestsRead = new LinkedList<>();",
        ),
        (
            "    public GroupTransferService(final HAService haService, final DefaultMessageStore defaultMessageStore) {",
            "    /** 构造并绑定 HA 与 MessageStore。 */\n    public GroupTransferService(final HAService haService, final DefaultMessageStore defaultMessageStore) {",
        ),
        (
            "    public void putRequest(final CommitLog.GroupCommitRequest request) {",
            "    /** 追加组提交请求并唤醒服务线程。 */\n    public void putRequest(final CommitLog.GroupCommitRequest request) {",
        ),
        (
            "    public void notifyTransferSome() {",
            "    /** 通知有新的传输进度，唤醒等待。 */\n    public void notifyTransferSome() {",
        ),
        (
            "    private void swapRequests() {",
            "    /** 交换读写请求缓冲区。 */\n    private void swapRequests() {",
        ),
        (
            "    private void doWaitTransfer() {",
            "    /** 轮询等待从节点 ACK 直至超时或满足 ack 数。 */\n    private void doWaitTransfer() {",
        ),
        (
            "                        // In this mode, we must wait for all replicas that in SyncStateSet.",
            "                        // 此模式下须等待 SyncStateSet 内全部副本 ACK，",
        ),
        (
            "                            // Only master",
            "                            // 仅主节点时直接成功",
        ),
        (
            "                        // Include master",
            "                        // 计数含主节点本身",
        ),
        (
            "                            // TODO: We must ensure every HAConnection represents a different slave",
            "                            // TODO: 须确保每条 HAConnection 对应不同从节点",
        ),
        (
            "                            // Solution: Consider assign a unique and fixed IP:ADDR for each different slave",
            "                            // 方案：为每个从节点分配唯一固定 IP:PORT",
        ),
        (
            "    @Override\n    public void run() {",
            "    /** 主循环：周期性处理传输等待。 */\n    @Override\n    public void run() {",
        ),
        (
            "    @Override\n    protected void onWaitEnd() {",
            "    /** 等待结束时交换请求缓冲区。 */\n    @Override\n    protected void onWaitEnd() {",
        ),
        (
            "    @Override\n    public String getServiceName() {",
            "    /** 返回服务名称（容器模式下带 Broker 标识）。 */\n    @Override\n    public String getServiceName() {",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/ha/HAClient.java": [
        (
            "public interface HAClient {",
            "/**\n * HA 客户端接口：从节点侧连接主节点并同步 CommitLog。\n */\npublic interface HAClient {",
        ),
        (
            "    /**\n     * Start HAClient\n     */",
            "    /** 启动 HA 客户端线程。 */",
        ),
        (
            "    /**\n     * Shutdown HAClient\n     */",
            "    /** 关闭 HA 客户端。 */",
        ),
        (
            "    /**\n     * Wakeup HAClient\n     */",
            "    /** 唤醒阻塞中的 HA 客户端。 */",
        ),
        (
            "    /**\n     * Update master address\n     *\n     * @param newAddress\n     */",
            "    /**\n     * 更新主节点业务地址。\n     *\n     * @param newAddress 新主节点地址\n     */",
        ),
        (
            "    /**\n     * Update master ha address\n     *\n     * @param newAddress\n     */",
            "    /**\n     * 更新主节点 HA 专用地址。\n     *\n     * @param newAddress 新 HA 地址\n     */",
        ),
        (
            "    /**\n     * Get master address\n     *\n     * @return master address\n     */",
            "    /**\n     * 获取主节点业务地址。\n     *\n     * @return 主节点地址\n     */",
        ),
        (
            "    /**\n     * Get master ha address\n     *\n     * @return master ha address\n     */",
            "    /**\n     * 获取主节点 HA 地址。\n     *\n     * @return HA 地址\n     */",
        ),
        (
            "    /**\n     * Get HAClient last read timestamp\n     *\n     * @return last read timestamp\n     */",
            "    /**\n     * 获取最近一次读主节点数据的时间戳。\n     *\n     * @return 最后读时间戳\n     */",
        ),
        (
            "    /**\n     * Get HAClient last write timestamp\n     *\n     * @return last write timestamp\n     */",
            "    /**\n     * 获取最近一次向主节点写入的时间戳。\n     *\n     * @return 最后写时间戳\n     */",
        ),
        (
            "    /**\n     * Get current state for ha connection\n     *\n     * @return HAConnectionState\n     */",
            "    /**\n     * 获取当前 HA 连接状态。\n     *\n     * @return HAConnectionState\n     */",
        ),
        (
            "    /**\n     * Change the current state for ha connection for testing\n     *\n     * @param haConnectionState\n     */",
            "    /**\n     * 测试用：强制修改连接状态。\n     *\n     * @param haConnectionState 目标状态\n     */",
        ),
        (
            "    /**\n     * Disconnecting from the master for testing\n     */",
            "    /** 测试用：断开与主节点的连接。 */",
        ),
        (
            "    /**\n     * Get the transfer rate per second\n     *\n     *  @return transfer bytes in second\n     */",
            "    /**\n     * 获取每秒传输字节数。\n     *\n     * @return 每秒传输字节\n     */",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/ha/HAConnection.java": [
        (
            "public interface HAConnection {",
            "/**\n * HA 连接接口：主节点侧与单个从节点的同步通道抽象。\n */\npublic interface HAConnection {",
        ),
        (
            "    /**\n     * Start HA Connection\n     */",
            "    /** 启动 HA 连接读写线程。 */",
        ),
        (
            "    /**\n     * Shutdown HA Connection\n     */",
            "    /** 关闭 HA 连接及相关资源。 */",
        ),
        (
            "    /**\n     * Close HA Connection\n     */",
            "    /** 关闭底层 Socket 通道。 */",
        ),
        (
            "    /**\n     * Get socket channel\n     */",
            "    /** 返回底层 SocketChannel。 */",
        ),
        (
            "    /**\n     * Get current state for ha connection\n     *\n     * @return HAConnectionState\n     */",
            "    /**\n     * 获取连接当前状态。\n     *\n     * @return HAConnectionState\n     */",
        ),
        (
            "    /**\n     * Get client address for ha connection\n     *\n     * @return client ip address\n     */",
            "    /**\n     * 获取从节点客户端 IP 地址。\n     *\n     * @return 客户端 IP\n     */",
        ),
        (
            "    /**\n     * Get the transfer rate per second\n     *\n     *  @return transfer bytes in second\n     */",
            "    /**\n     * 获取每秒向该从节点传输的字节数。\n     *\n     * @return 每秒传输字节\n     */",
        ),
        (
            "    /**\n     * Get the current transfer offset to the slave\n     *\n     * @return the current transfer offset to the slave\n     */",
            "    /**\n     * 获取向从节点传输的起始偏移（transferFromWhere）。\n     *\n     * @return 当前传输起始偏移\n     */",
        ),
        (
            "    /**\n     * Get slave ack offset\n     *\n     * @return slave ack offset\n     */",
            "    /**\n     * 获取从节点已 ACK 的 CommitLog 偏移。\n     *\n     * @return 从节点 ACK 偏移\n     */",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/ha/HAConnectionState.java": [
        (
            "public enum HAConnectionState {",
            "/**\n * HA 连接生命周期状态枚举。\n */\npublic enum HAConnectionState {",
        ),
        (
            "    /**\n     * Ready to start connection.\n     */",
            "    /** 就绪，尚未开始握手。 */",
        ),
        (
            "    /**\n     * CommitLog consistency checking.\n     */",
            "    /** 握手中，校验 CommitLog 一致性。 */",
        ),
        (
            "    /**\n     * Synchronizing data.\n     */",
            "    /** 数据传输中，同步 CommitLog。 */",
        ),
        (
            "    /**\n     * Temporarily stop transferring.\n     */",
            "    /** 暂停传输。 */",
        ),
        (
            "    /**\n     * Connection shutdown.\n     */",
            "    /** 连接已关闭。 */",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/ha/HAConnectionStateNotificationRequest.java": [
        (
            "public class HAConnectionStateNotificationRequest {",
            "/**\n * HA 连接状态通知请求：等待指定远程地址达到期望状态。\n */\npublic class HAConnectionStateNotificationRequest {",
        ),
        (
            "    private final CompletableFuture<Boolean> requestFuture = new CompletableFuture<>();",
            "    /** 异步完成 Future，true 表示达到期望状态。 */\n    private final CompletableFuture<Boolean> requestFuture = new CompletableFuture<>();",
        ),
        (
            "    private final HAConnectionState expectState;",
            "    /** 期望达到的连接状态。 */\n    private final HAConnectionState expectState;",
        ),
        (
            "    private final String remoteAddr;",
            "    /** 目标远程地址（IP）。 */\n    private final String remoteAddr;",
        ),
        (
            "    private final boolean notifyWhenShutdown;",
            "    /** 连接关闭时是否以 false 完成 Future。 */\n    private final boolean notifyWhenShutdown;",
        ),
        (
            "    public HAConnectionStateNotificationRequest(HAConnectionState expectState, String remoteAddr, boolean notifyWhenShutdown) {",
            "    /** 构造状态等待请求。 */\n    public HAConnectionStateNotificationRequest(HAConnectionState expectState, String remoteAddr, boolean notifyWhenShutdown) {",
        ),
        (
            "    public CompletableFuture<Boolean> getRequestFuture() {",
            "    /** 返回完成 Future。 */\n    public CompletableFuture<Boolean> getRequestFuture() {",
        ),
        (
            "    public String getRemoteAddr() {",
            "    /** 返回远程地址。 */\n    public String getRemoteAddr() {",
        ),
        (
            "    public boolean isNotifyWhenShutdown() {",
            "    /** 是否在 SHUTDOWN 时通知。 */\n    public boolean isNotifyWhenShutdown() {",
        ),
        (
            "    public HAConnectionState getExpectState() {",
            "    /** 返回期望状态。 */\n    public HAConnectionState getExpectState() {",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/ha/HAConnectionStateNotificationService.java": [
        (
            "/**\n * Service to periodically check and notify for certain connection state.\n */",
            "/**\n * HA 连接状态通知服务：周期性检查并回调等待中的 Future。\n */",
        ),
        (
            "    private static final long CONNECTION_ESTABLISH_TIMEOUT = 10 * 1000;",
            "    /** 连接建立超时毫秒数（10 秒）。 */\n    private static final long CONNECTION_ESTABLISH_TIMEOUT = 10 * 1000;",
        ),
        (
            "    private volatile HAConnectionStateNotificationRequest request;",
            "    /** 当前待处理的通知请求。 */\n    private volatile HAConnectionStateNotificationRequest request;",
        ),
        (
            "    private volatile long lastCheckTimeStamp = -1;",
            "    /** 上次检测到匹配连接的时间戳。 */\n    private volatile long lastCheckTimeStamp = -1;",
        ),
        (
            "    private HAService haService;",
            "    /** 关联 HA 服务。 */\n    private HAService haService;",
        ),
        (
            "    private DefaultMessageStore defaultMessageStore;",
            "    /** 所属 MessageStore。 */\n    private DefaultMessageStore defaultMessageStore;",
        ),
        (
            "    public HAConnectionStateNotificationService(HAService haService, DefaultMessageStore defaultMessageStore) {",
            "    /** 构造并注入依赖。 */\n    public HAConnectionStateNotificationService(HAService haService, DefaultMessageStore defaultMessageStore) {",
        ),
        (
            "    @Override\n    public String getServiceName() {",
            "    /** 返回服务名称。 */\n    @Override\n    public String getServiceName() {",
        ),
        (
            "    public synchronized void setRequest(HAConnectionStateNotificationRequest request) {",
            "    /** 设置新请求并取消未完成的前序请求。 */\n    public synchronized void setRequest(HAConnectionStateNotificationRequest request) {",
        ),
        (
            "    private synchronized void doWaitConnectionState() {",
            "    /** 检查从/主侧连接是否达到期望状态或超时。 */\n    private synchronized void doWaitConnectionState() {",
        ),
        (
            "    /**\n     * Check if connection matched and notify request.\n     *\n     * @param connection connection to check.\n     * @return if connection remote address match request.\n     */",
            "    /**\n     * 检查连接地址与状态并通知请求。\n     *\n     * @param connection 待检查连接\n     * @return 远程地址是否匹配请求\n     */",
        ),
        (
            "    @Override\n    public void run() {",
            "    /** 主循环：每秒检查连接状态。 */\n    @Override\n    public void run() {",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/ha/HAService.java": [
        (
            "public interface HAService {",
            "/**\n * 主从同步（HA）服务接口：管理 CommitLog 复制、连接与角色切换。\n */\npublic interface HAService {",
        ),
        (
            "    /**\n     * Init HAService, must be called before other methods.\n     *\n     * @param defaultMessageStore\n     * @throws IOException\n     */",
            "    /**\n     * 初始化 HA 服务，须在其它方法之前调用。\n     *\n     * @param defaultMessageStore MessageStore 实例\n     * @throws IOException 初始化 IO 异常\n     */",
        ),
        (
            "    /**\n     * Start HA Service\n     *\n     * @throws Exception\n     */",
            "    /**\n     * 启动 HA 服务。\n     *\n     * @throws Exception 启动失败\n     */",
        ),
        (
            "    /**\n     * Shutdown HA Service\n     */",
            "    /** 关闭 HA 服务。 */",
        ),
        (
            "    /**\n     * Change to master state\n     *\n     * @param masterEpoch the new masterEpoch\n     */",
            "    /**\n     * 切换为主节点。\n     *\n     * @param masterEpoch 新主 epoch\n     */",
        ),
        (
            "    /**\n     * Change to slave state\n     *\n     * @param newMasterAddr new master addr\n     * @param newMasterEpoch new masterEpoch\n     */",
            "    /**\n     * 切换为从节点。\n     *\n     * @param newMasterAddr 新主地址\n     * @param newMasterEpoch 新主 epoch\n     */",
        ),
        (
            "    /**\n     * Update master address\n     *\n     * @param newAddr\n     */",
            "    /**\n     * 更新主节点业务地址。\n     *\n     * @param newAddr 新地址\n     */",
        ),
        (
            "    /**\n     * Update ha master address\n     *\n     * @param newAddr\n     */",
            "    /**\n     * 更新 HA 专用主地址。\n     *\n     * @param newAddr 新 HA 地址\n     */",
        ),
        (
            "    /**\n     * Returns the number of replicas those commit log are not far behind the master. It includes master itself. Returns\n     * syncStateSet size if HAService instanceof AutoSwitchService\n     *\n     * @return the number of slaves\n     * @see MessageStoreConfig#getHaMaxGapNotInSync()\n     */",
            "    /**\n     * 返回 CommitLog 未明显落后的副本数（含主）。AutoSwitch 模式下等价于 syncStateSet 大小。\n     *\n     * @return 同步副本数量\n     * @see MessageStoreConfig#getHaMaxGapNotInSync()\n     */",
        ),
        (
            "    /**\n     * Get connection count\n     *\n     * @return the number of connection\n     */",
            "    /**\n     * 获取 HA 连接数。\n     *\n     * @return 连接数量\n     */",
        ),
        (
            "    /**\n     * Put request to handle HA\n     *\n     * @param request\n     */",
            "    /**\n     * 提交组提交请求由 HA 处理。\n     *\n     * @param request 组提交请求\n     */",
        ),
        (
            "    /**\n     * Put GroupConnectionStateRequest for preOnline\n     *\n     * @param request\n     */",
            "    /**\n     * 注册 preOnline 用的连接状态等待请求。\n     *\n     * @param request 状态通知请求\n     */",
        ),
        (
            "    /**\n     * Get ha connection list\n     *\n     * @return List<HAConnection>\n     */",
            "    /**\n     * 获取 HA 连接列表。\n     *\n     * @return HAConnection 列表\n     */",
        ),
        (
            "    /**\n     * Get HAClient\n     *\n     * @return HAClient\n     */",
            "    /**\n     * 获取 HA 客户端（从节点）。\n     *\n     * @return HAClient\n     */",
        ),
        (
            "    /**\n     * Get the max offset in all slaves\n     */",
            "    /** 获取所有从节点中的最大已推送偏移。 */",
        ),
        (
            "    /**\n     * Get HA runtime info\n     */",
            "    /** 收集 HA 运行时信息。 */",
        ),
        (
            "    /**\n     * Get WaitNotifyObject\n     */",
            "    /** 返回等待/唤醒对象。 */",
        ),
        (
            "    /**\n     * Judge whether the slave keeps up according to the masterPutWhere, If the offset gap exceeds haSlaveFallBehindMax,\n     * then slave is not OK\n     */",
            "    /**\n     * 根据 masterPutWhere 判断从节点是否跟上；偏移差超过阈值则返回 false。\n     */",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/ha/WaitNotifyObject.java": [
        (
            "public class WaitNotifyObject {",
            "/**\n * 等待/唤醒工具：支持单线程 waitForRunning 与多线程 allWaitForRunning。\n */\npublic class WaitNotifyObject {",
        ),
        (
            "    protected final ConcurrentHashMap<Long/* thread id */, AtomicBoolean/* notified */> waitingThreadTable =",
            "    /** 多线程等待表：线程 ID -> 是否已被唤醒。 */\n    protected final ConcurrentHashMap<Long/* thread id */, AtomicBoolean/* notified */> waitingThreadTable =",
        ),
        (
            "    protected AtomicBoolean hasNotified = new AtomicBoolean(false);",
            "    /** 单线程模式下的唤醒标志。 */\n    protected AtomicBoolean hasNotified = new AtomicBoolean(false);",
        ),
        (
            "    public void wakeup() {",
            "    /** 唤醒单个等待线程。 */\n    public void wakeup() {",
        ),
        (
            "    protected void waitForRunning(long interval) {",
            "    /** 带超时的 wait，被唤醒或超时后调用 onWaitEnd。 */\n    protected void waitForRunning(long interval) {",
        ),
        (
            "    protected void onWaitEnd() {",
            "    /** 等待结束钩子，子类可覆盖。 */\n    protected void onWaitEnd() {",
        ),
        (
            "    public void wakeupAll() {",
            "    /** 唤醒 waitingThreadTable 中所有等待线程。 */\n    public void wakeupAll() {",
        ),
        (
            "    public void allWaitForRunning(long interval) {",
            "    /** 当前线程注册到等待表并 wait。 */\n    public void allWaitForRunning(long interval) {",
        ),
        (
            "    public void removeFromWaitingThreadTable() {",
            "    /** 从等待表移除当前线程。 */\n    public void removeFromWaitingThreadTable() {",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/ha/autoswitch/BrokerMetadata.java": [
        (
            "public class BrokerMetadata extends MetadataFile {",
            "/**\n * Broker 元数据文件：持久化 clusterName、brokerName、brokerId。\n */\npublic class BrokerMetadata extends MetadataFile {",
        ),
        (
            "    protected String clusterName;",
            "    /** 集群名称。 */\n    protected String clusterName;",
        ),
        (
            "    protected String brokerName;",
            "    /** Broker 名称。 */\n    protected String brokerName;",
        ),
        (
            "    protected Long brokerId;",
            "    /** Broker ID（0 为主）。 */\n    protected Long brokerId;",
        ),
        (
            "    public BrokerMetadata(String filePath) {",
            "    /** 指定元数据文件路径。 */\n    public BrokerMetadata(String filePath) {",
        ),
        (
            "    public void updateAndPersist(String clusterName, String brokerName, Long brokerId) throws Exception {",
            "    /** 更新内存字段并写盘。 */\n    public void updateAndPersist(String clusterName, String brokerName, Long brokerId) throws Exception {",
        ),
        (
            "    @Override\n    public String encodeToStr() {",
            "    /** 序列化为 cluster#broker#id 字符串。 */\n    @Override\n    public String encodeToStr() {",
        ),
        (
            "    @Override\n    public void decodeFromStr(String dataStr) {",
            "    /** 从 # 分隔字符串反序列化。 */\n    @Override\n    public void decodeFromStr(String dataStr) {",
        ),
        (
            "    @Override\n    public boolean isLoaded() {",
            "    /** 三个字段均已加载则返回 true。 */\n    @Override\n    public boolean isLoaded() {",
        ),
        (
            "    @Override\n    public void clearInMem() {",
            "    /** 清空内存中的元数据。 */\n    @Override\n    public void clearInMem() {",
        ),
        (
            "    public String getBrokerName() {",
            "    /** 返回 brokerName。 */\n    public String getBrokerName() {",
        ),
        (
            "    public Long getBrokerId() {",
            "    /** 返回 brokerId。 */\n    public Long getBrokerId() {",
        ),
        (
            "    public String getClusterName() {",
            "    /** 返回 clusterName。 */\n    public String getClusterName() {",
        ),
        (
            "    @Override\n    public boolean equals(Object o) {",
            "    /** 按 cluster、broker、id 比较相等。 */\n    @Override\n    public boolean equals(Object o) {",
        ),
        (
            "    @Override\n    public int hashCode() {",
            "    /** 计算哈希码。 */\n    @Override\n    public int hashCode() {",
        ),
        (
            "    @Override\n    public String toString() {",
            "    /** 返回调试字符串。 */\n    @Override\n    public String toString() {",
        ),
    ],
    "store/src/main/java/org/apache/rocketmq/store/ha/autoswitch/EpochFileCache.java": [
        (
            "/**\n * Cache for epochFile. Mapping (Epoch -> StartOffset)\n */",
            "/**\n * Epoch 文件缓存：维护 Epoch 到 StartOffset 的有序映射。\n */",
        ),
        (
            "    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();",
            "    /** 保护 epochMap 的读写锁。 */\n    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();",
        ),
        (
            "    private final TreeMap<Integer, EpochEntry> epochMap;",
            "    /** Epoch -> EpochEntry 有序表。 */\n    private final TreeMap<Integer, EpochEntry> epochMap;",
        ),
        (
            "    private CheckpointFile<EpochEntry> checkpoint;",
            "    /** 持久化 checkpoint 文件句柄。 */\n    private CheckpointFile<EpochEntry> checkpoint;",
        ),
        (
            "    public EpochFileCache() {",
            "    /** 内存模式，不写盘。 */\n    public EpochFileCache() {",
        ),
        (
            "    public EpochFileCache(final String path) {",
            "    /** 指定 checkpoint 文件路径。 */\n    public EpochFileCache(final String path) {",
        ),
        (
            "    public boolean initCacheFromFile() {",
            "    /** 从磁盘加载 epoch 条目到缓存。 */\n    public boolean initCacheFromFile() {",
        ),
        (
            "    public void initCacheFromEntries(final List<EpochEntry> entries) {",
            "    /** 用给定条目初始化并刷盘。 */\n    public void initCacheFromEntries(final List<EpochEntry> entries) {",
        ),
        (
            "    private void initEntries(final List<EpochEntry> entries) {",
            "    /** 重建 epochMap 并链接相邻 entry 的 endOffset。 */\n    private void initEntries(final List<EpochEntry> entries) {",
        ),
        (
            "    public int getEntrySize() {",
            "    /** 返回 epoch 条目数量。 */\n    public int getEntrySize() {",
        ),
        (
            "    public boolean appendEntry(final EpochEntry entry) {",
            "    /** 追加新 epoch 条目并刷盘。 */\n    public boolean appendEntry(final EpochEntry entry) {",
        ),
        (
            "    /**\n     * Set endOffset for lastEpochEntry.\n     */",
            "    /** 设置最后一个 epoch 条目的 endOffset。 */",
        ),
        (
            "    public EpochEntry firstEntry() {",
            "    /** 返回首个 epoch 条目的副本。 */\n    public EpochEntry firstEntry() {",
        ),
        (
            "    public EpochEntry lastEntry() {",
            "    /** 返回最后一个 epoch 条目的副本。 */\n    public EpochEntry lastEntry() {",
        ),
        (
            "    public int lastEpoch() {",
            "    /** 返回最大 epoch 值，空则 -1。 */\n    public int lastEpoch() {",
        ),
        (
            "    public EpochEntry getEntry(final int epoch) {",
            "    /** 按 epoch 查询条目副本。 */\n    public EpochEntry getEntry(final int epoch) {",
        ),
        (
            "    public EpochEntry findEpochEntryByOffset(final long offset) {",
            "    /** 按 CommitLog 偏移查找所属 epoch 条目。 */\n    public EpochEntry findEpochEntryByOffset(final long offset) {",
        ),
        (
            "    public EpochEntry nextEntry(final int epoch) {",
            "    /** 返回严格大于给定 epoch 的下一条目。 */\n    public EpochEntry nextEntry(final int epoch) {",
        ),
        (
            "    public List<EpochEntry> getAllEntries() {",
            "    /** 返回全部 epoch 条目副本列表。 */\n    public List<EpochEntry> getAllEntries() {",
        ),
        (
            "    /**\n     * Find the consistentPoint between compareCache and local.\n     *\n     * @return the consistent offset\n     */",
            "    /**\n     * 与 compareCache 比对，找到一致点偏移。\n     *\n     * @return 一致偏移，无则 -1\n     */",
        ),
        (
            "    /**\n     * Remove epochEntries with epoch >= truncateEpoch.\n     */",
            "    /** 截断 epoch >= truncateEpoch 的后缀条目。 */",
        ),
        (
            "    /**\n     * Remove epochEntries with startOffset >= truncateOffset.\n     */",
            "    /** 截断 startOffset >= truncateOffset 的后缀条目。 */",
        ),
        (
            "    private void doTruncateSuffix(Predicate<EpochEntry> predict) {",
            "    /** 按谓词删除后缀并重置末条 endOffset 为 MAX。 */\n    private void doTruncateSuffix(Predicate<EpochEntry> predict) {",
        ),
        (
            "    /**\n     * Remove epochEntries with endOffset <= truncateOffset.\n     */",
            "    /** 截断 endOffset <= truncateOffset 的前缀条目。 */",
        ),
        (
            "    private void flush() {",
            "    /** 将 epochMap 写入 checkpoint 文件。 */\n    private void flush() {",
        ),
        (
            "    static class EpochEntrySerializer implements CheckpointFile.CheckpointSerializer<EpochEntry> {",
            "    /** EpochEntry 与 checkpoint 行格式互转。 */\n    static class EpochEntrySerializer implements CheckpointFile.CheckpointSerializer<EpochEntry> {",
        ),
        (
            "        @Override\n        public String toLine(EpochEntry entry) {",
            "        /** 格式化为 epoch-startOffset 行。 */\n        @Override\n        public String toLine(EpochEntry entry) {",
        ),
        (
            "        @Override\n        public EpochEntry fromLine(String line) {",
            "        /** 从 checkpoint 行解析 EpochEntry。 */\n        @Override\n        public EpochEntry fromLine(String line) {",
        ),
    ],
}
