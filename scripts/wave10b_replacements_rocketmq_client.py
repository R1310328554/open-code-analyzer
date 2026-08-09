"""RocketMQ 5.5.0 wave10b client mqclient/producer/latency/lock [15:30] replacements."""

R: dict[str, list[tuple[str, str]]] = {}

R["client/src/main/java/org/apache/rocketmq/client/impl/mqclient/DoNothingClientRemotingProcessor.java"] = [
    (
        "public class DoNothingClientRemotingProcessor extends ClientRemotingProcessor {",
        "/**\n * 空实现的客户端 Remoting 处理器：收到服务端请求时直接返回 null，不做任何业务处理。\n * 用于仅需出站通信、无需处理入站指令的轻量客户端场景。\n */\npublic class DoNothingClientRemotingProcessor extends ClientRemotingProcessor {",
    ),
    (
        "    public DoNothingClientRemotingProcessor(MQClientInstance mqClientFactory) {",
        "    /** 以 MQClient 实例构造，委托父类注册 Remoting 回调。 */\n    public DoNothingClientRemotingProcessor(MQClientInstance mqClientFactory) {",
    ),
    (
        "    public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request) {",
        "    /** 忽略入站请求，始终返回 null 表示不响应。 */\n    public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request) {",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/impl/mqclient/MQClientAPIFactory.java"] = [
    (
        "public class MQClientAPIFactory implements StartAndShutdown {",
        "/**\n * MQClientAPI 工厂：按配置创建并管理多个 {@link MQClientAPIExt} 实例，\n * 负责 NameServer 地址初始化、客户端启动/关闭及负载均衡选取。\n */\npublic class MQClientAPIFactory implements StartAndShutdown {",
    ),
    (
        "    private MQClientAPIExt[] clients;",
        "    /** 已创建的客户端 API 实例数组。 */\n    private MQClientAPIExt[] clients;",
    ),
    (
        "    private final String namePrefix;",
        "    /** 客户端实例名前缀。 */\n    private final String namePrefix;",
    ),
    (
        "    private final int clientNum;",
        "    /** 并行客户端数量，大于 1 时 getClient 随机选取。 */\n    private final int clientNum;",
    ),
    (
        "    private final ClientRemotingProcessor clientRemotingProcessor;",
        "    /** 入站 Remoting 请求处理器。 */\n    private final ClientRemotingProcessor clientRemotingProcessor;",
    ),
    (
        "    private final RPCHook rpcHook;",
        "    /** RPC 钩子，用于鉴权等扩展。 */\n    private final RPCHook rpcHook;",
    ),
    (
        "    private final ScheduledExecutorService scheduledExecutorService;",
        "    /** 定时任务线程池，用于域名模式下周期性拉取 NameServer 地址。 */\n    private final ScheduledExecutorService scheduledExecutorService;",
    ),
    (
        "    private final NameserverAccessConfig nameserverAccessConfig;",
        "    /** NameServer 访问配置（地址或域名模式）。 */\n    private final NameserverAccessConfig nameserverAccessConfig;",
    ),
    (
        "    private final ObjectCreator<RemotingClient> remotingClientCreator;",
        "    /** 可选的 RemotingClient 创建器，便于测试或自定义实现。 */\n    private final ObjectCreator<RemotingClient> remotingClientCreator;",
    ),
    (
        "    protected void init() {",
        "    /** 初始化系统属性：关闭 VIP 通道，并设置 NameServer 地址或域名。 */\n    protected void init() {",
    ),
    (
        "    public MQClientAPIExt getClient() {",
        "    /** 返回一个客户端实例；多实例时随机负载均衡。 */\n    public MQClientAPIExt getClient() {",
    ),
    (
        "    public void start() throws Exception {",
        "    /** 创建并启动 clientNum 个 MQClientAPIExt 实例。 */\n    public void start() throws Exception {",
    ),
    (
        "    public void shutdown() throws Exception {",
        "    /** 并行关闭所有客户端实例。 */\n    public void shutdown() throws Exception {",
    ),
    (
        "    protected MQClientAPIExt createAndStart(String instanceName) {",
        "    /** 创建单个 MQClientAPIExt：配置 Netty、注册 NameServer 并启动。 */\n    protected MQClientAPIExt createAndStart(String instanceName) {",
    ),
    (
        "    public void onNameServerAddressChange(String namesrvAddress) {",
        "    /** NameServer 地址变更时通知所有客户端更新。 */\n    public void onNameServerAddressChange(String namesrvAddress) {",
    ),
    (
        "    public MQClientAPIExt[] getClients() {",
        "    /** 返回全部客户端实例数组。 */\n    public MQClientAPIExt[] getClients() {",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/impl/producer/MQProducerInner.java"] = [
    (
        "public interface MQProducerInner {",
        "/**\n * Producer 内部接口：供 MQClientInstance 回调，管理 Topic 路由、\n * 事务状态回查监听及单元化模式等内部协作能力。\n */\npublic interface MQProducerInner {",
    ),
    (
        "    Set<String> getPublishTopicList();",
        "    /** 返回当前 Producer 负责发布的 Topic 集合。 */\n    Set<String> getPublishTopicList();",
    ),
    (
        "    boolean isPublishTopicNeedUpdate(final String topic);",
        "    /** 判断指定 Topic 的路由信息是否需要刷新。 */\n    boolean isPublishTopicNeedUpdate(final String topic);",
    ),
    (
        "    TransactionCheckListener checkListener();",
        "    /** 返回旧版事务回查监听器（已废弃接口）。 */\n    TransactionCheckListener checkListener();",
    ),
    (
        "    TransactionListener getCheckListener();",
        "    /** 返回新版事务回查监听器。 */\n    TransactionListener getCheckListener();",
    ),
    (
        "    void checkTransactionState(",
        "    /** Broker 发起事务状态回查时的回调入口。 */\n    void checkTransactionState(",
    ),
    (
        "    void updateTopicPublishInfo(final String topic, final TopicPublishInfo info);",
        "    /** 更新 Topic 的发布路由信息。 */\n    void updateTopicPublishInfo(final String topic, final TopicPublishInfo info);",
    ),
    (
        "    boolean isUnitMode();",
        "    /** 是否处于单元化部署模式。 */\n    boolean isUnitMode();",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/impl/producer/TopicPublishInfo.java"] = [
    (
        "public class TopicPublishInfo {",
        "/**\n * Topic 发布路由信息：维护可写队列列表、顺序 Topic 标志及轮询选队列索引，\n * 供 Producer 发送时选择目标 MessageQueue。\n */\npublic class TopicPublishInfo {",
    ),
    (
        "    private boolean orderTopic = false;",
        "    /** 是否为顺序 Topic（顺序 Topic 仅使用单个队列）。 */\n    private boolean orderTopic = false;",
    ),
    (
        "    private boolean haveTopicRouterInfo = false;",
        "    /** 是否已持有完整 Topic 路由数据。 */\n    private boolean haveTopicRouterInfo = false;",
    ),
    (
        "    private List<MessageQueue> messageQueueList = new ArrayList<>();",
        "    /** 当前 Topic 可写消息队列列表。 */\n    private List<MessageQueue> messageQueueList = new ArrayList<>();",
    ),
    (
        "    private volatile ThreadLocalIndex sendWhichQueue = new ThreadLocalIndex();",
        "    /** 线程本地轮询索引，用于均匀选取队列。 */\n    private volatile ThreadLocalIndex sendWhichQueue = new ThreadLocalIndex();",
    ),
    (
        "    private TopicRouteData topicRouteData;",
        "    /** 原始 Topic 路由数据（含各 Broker 队列数等）。 */\n    private TopicRouteData topicRouteData;",
    ),
    (
        "    public interface QueueFilter {",
        "    /** 队列过滤器：选队列时按条件排除不符合的 MessageQueue。 */\n    public interface QueueFilter {",
    ),
    (
        "        boolean filter(MessageQueue mq);",
        "        /** 返回 true 表示该队列可用。 */\n        boolean filter(MessageQueue mq);",
    ),
    (
        "    public boolean ok() {",
        "    /** 路由是否有效（队列列表非空）。 */\n    public boolean ok() {",
    ),
    (
        "    public MessageQueue selectOneMessageQueue(QueueFilter ...filter) {",
        "    /** 按轮询选取一个队列，可选附加过滤器链。 */\n    public MessageQueue selectOneMessageQueue(QueueFilter ...filter) {",
    ),
    (
        "    private MessageQueue selectOneMessageQueue(List<MessageQueue> messageQueueList, ThreadLocalIndex sendQueue, QueueFilter ...filter) {",
        "    /** 内部选队列逻辑：遍历轮询并应用过滤器，全部不匹配时返回 null。 */\n    private MessageQueue selectOneMessageQueue(List<MessageQueue> messageQueueList, ThreadLocalIndex sendQueue, QueueFilter ...filter) {",
    ),
    (
        "    public void resetIndex() {",
        "    /** 重置轮询索引，用于故障切换后重新从头发送。 */\n    public void resetIndex() {",
    ),
    (
        "    public MessageQueue selectOneMessageQueue(final String lastBrokerName) {",
        "    /** 选取队列并尽量避开上次失败的 Broker。 */\n    public MessageQueue selectOneMessageQueue(final String lastBrokerName) {",
    ),
    (
        "    public MessageQueue selectOneMessageQueue() {",
        "    /** 简单轮询选取一个队列。 */\n    public MessageQueue selectOneMessageQueue() {",
    ),
    (
        "    public int getWriteQueueNumsByBroker(final String brokerName) {",
        "    /** 查询指定 Broker 的可写队列数量，未找到返回 -1。 */\n    public int getWriteQueueNumsByBroker(final String brokerName) {",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/latency/LatencyFaultTolerance.java"] = [
    (
        "public interface LatencyFaultTolerance<T> {",
        "/**\n * 延迟故障容错接口：根据发送延迟与可达性动态隔离 Broker，\n * 并在恢复后重新纳入选路范围。\n *\n * @param <T> 故障项标识类型，通常为 Broker 名称。\n */\npublic interface LatencyFaultTolerance<T> {",
    ),
    (
        "    /**\n     * Update brokers' states, to decide if they are good or not.\n     *\n     * @param name Broker's name.\n     * @param currentLatency Current message sending process's latency.\n     * @param notAvailableDuration Corresponding not available time, ms. The broker will be not available until it\n     * spends such time.\n     * @param reachable To decide if this broker is reachable or not.\n     */",
        "    /**\n     * 更新 Broker 故障项：记录当前延迟、不可用时长及可达性。\n     *\n     * @param name Broker 名称\n     * @param currentLatency 本次发送耗时（毫秒）\n     * @param notAvailableDuration 隔离时长（毫秒），到期前视为不可用\n     * @param reachable 当前是否网络可达\n     */",
    ),
    (
        "    /**\n     * To check if this broker is available.\n     *\n     * @param name Broker's name.\n     * @return boolean variable, if this is true, then the broker is available.\n     */",
        "    /**\n     * 判断 Broker 是否可用（隔离期已过）。\n     *\n     * @param name Broker 名称\n     * @return true 表示可用\n     */",
    ),
    (
        "    /**\n     * To check if this broker is reachable.\n     *\n     * @param name Broker's name.\n     * @return boolean variable, if this is true, then the broker is reachable.\n     */",
        "    /**\n     * 判断 Broker 是否可达（网络探测正常）。\n     *\n     * @param name Broker 名称\n     * @return true 表示可达\n     */",
    ),
    (
        "    /**\n     * Remove the broker in this fault item table.\n     *\n     * @param name broker's name.\n     */",
        "    /**\n     * 从故障表中移除指定 Broker。\n     *\n     * @param name Broker 名称\n     */",
    ),
    (
        "    /**\n     * The worst situation, no broker can be available. Then choose random one.\n     *\n     * @return A random mq will be returned.\n     */",
        "    /**\n     * 兜底策略：无可用 Broker 时随机选取一个可达项。\n     *\n     * @return 随机 Broker 名称，无则 null\n     */",
    ),
    (
        "    /**\n     * Start a new thread, to detect the broker's reachable tag.\n     */",
        "    /** 启动后台探测线程，周期性检测 Broker 可达性。 */",
    ),
    (
        "    /**\n     * Shutdown threads that started by LatencyFaultTolerance.\n     */",
        "    /** 关闭探测线程池。 */",
    ),
    (
        "    /**\n     * A function reserved, just detect by once, won't create a new thread.\n     */",
        "    /** 执行一轮可达性探测，不创建新线程。 */",
    ),
    (
        "    /**\n     * Use it to set the detect timeout bound.\n     *\n     * @param detectTimeout timeout bound\n     */",
        "    /**\n     * 设置单次探测超时（毫秒）。\n     *\n     * @param detectTimeout 超时上限\n     */",
    ),
    (
        "    /**\n     * Use it to set the detector's detector interval for each broker (each broker will be detected once during this\n     * time)\n     *\n     * @param detectInterval each broker's detecting interval\n     */",
        "    /**\n     * 设置每个 Broker 的探测间隔（毫秒）。\n     *\n     * @param detectInterval 探测周期\n     */",
    ),
    (
        "    /**\n     * Use it to set the detector work or not.\n     *\n     * @param startDetectorEnable set the detector's work status\n     */",
        "    /**\n     * 启用或禁用后台探测器。\n     *\n     * @param startDetectorEnable 是否启动探测\n     */",
    ),
    (
        "    /**\n     * Use it to judge if the detector enabled.\n     *\n     * @return is the detector should be started.\n     */",
        "    /**\n     * 探测器是否已启用。\n     *\n     * @return true 表示应启动探测\n     */",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/latency/LatencyFaultToleranceImpl.java"] = [
    (
        "public class LatencyFaultToleranceImpl implements LatencyFaultTolerance<String> {",
        "/**\n * 延迟故障容错默认实现：维护 Broker 故障项表，按延迟隔离并在后台探测恢复。\n */\npublic class LatencyFaultToleranceImpl implements LatencyFaultTolerance<String> {",
    ),
    (
        "    private final ConcurrentHashMap<String, FaultItem> faultItemTable = new ConcurrentHashMap<String, FaultItem>(16);",
        "    /** Broker 名称 → 故障项映射表。 */\n    private final ConcurrentHashMap<String, FaultItem> faultItemTable = new ConcurrentHashMap<String, FaultItem>(16);",
    ),
    (
        "    private int detectTimeout = 200;",
        "    /** 单次探测超时（毫秒）。 */\n    private int detectTimeout = 200;",
    ),
    (
        "    private int detectInterval = 2000;",
        "    /** 每个 Broker 的探测间隔（毫秒）。 */\n    private int detectInterval = 2000;",
    ),
    (
        "    private volatile boolean startDetectorEnable = false;",
        "    /** 是否启用后台探测器。 */\n    private volatile boolean startDetectorEnable = false;",
    ),
    (
        "    private final Resolver resolver;",
        "    /** Broker 名称解析器，用于获取探测地址。 */\n    private final Resolver resolver;",
    ),
    (
        "    private final ServiceDetector serviceDetector;",
        "    /** 远程服务可达性探测器。 */\n    private final ServiceDetector serviceDetector;",
    ),
    (
        "    public void detectByOneRound() {",
        "    /** 遍历故障表，对到期项执行一次可达性探测。 */\n    public void detectByOneRound() {",
    ),
    (
        "    public void startDetector() {",
        "    /** 启动定时任务，每 3 秒执行一轮探测（需 startDetectorEnable 为 true）。 */\n    public void startDetector() {",
    ),
    (
        "    public void shutdown() {",
        "    /** 关闭探测线程池。 */\n    public void shutdown() {",
    ),
    (
        "    public void updateFaultItem(final String name, final long currentLatency, final long notAvailableDuration,\n                                final boolean reachable) {",
        "    /** 更新或创建故障项，记录延迟、隔离时长与可达性。 */\n    public void updateFaultItem(final String name, final long currentLatency, final long notAvailableDuration,\n                                final boolean reachable) {",
    ),
    (
        "    public boolean isAvailable(final String name) {",
        "    /** 判断 Broker 隔离期是否已过；无记录视为可用。 */\n    public boolean isAvailable(final String name) {",
    ),
    (
        "    public boolean isReachable(final String name) {",
        "    /** 判断 Broker 是否可达；无记录视为可达。 */\n    public boolean isReachable(final String name) {",
    ),
    (
        "    public String pickOneAtLeast() {",
        "    /** 随机打乱后返回第一个可达 Broker，兜底选路用。 */\n    public String pickOneAtLeast() {",
    ),
    (
        "    public class FaultItem implements Comparable<FaultItem> {",
        "    /** 单个 Broker 的故障状态：延迟、隔离截止时间、可达标志。 */\n    public class FaultItem implements Comparable<FaultItem> {",
    ),
    (
        "        public void updateNotAvailableDuration(long notAvailableDuration) {",
        "        /** 延长隔离截止时间（仅当新截止时间更晚时生效）。 */\n        public void updateNotAvailableDuration(long notAvailableDuration) {",
    ),
    (
        "        public int compareTo(final FaultItem other) {",
        "        /** 比较优先级：可用性优先，其次延迟低、隔离期短者优先。 */\n        public int compareTo(final FaultItem other) {",
    ),
    (
        "        public boolean isAvailable() {",
        "        /** 当前时间是否已过隔离截止时间。 */\n        public boolean isAvailable() {",
    ),
    (
        "        public boolean isReachable() {",
        "        /** 返回可达标志。 */\n        public boolean isReachable() {",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/latency/MQFaultStrategy.java"] = [
    (
        "public class MQFaultStrategy implements StartAndShutdown {",
        "/**\n * 发送端故障策略：结合延迟隔离与 Broker 可达性，在选队列时优先避开故障 Broker。\n */\npublic class MQFaultStrategy implements StartAndShutdown {",
    ),
    (
        "    private LatencyFaultTolerance<String> latencyFaultTolerance;",
        "    /** 底层延迟故障容错实现。 */\n    private LatencyFaultTolerance<String> latencyFaultTolerance;",
    ),
    (
        "    private volatile boolean sendLatencyFaultEnable;",
        "    /** 是否启用发送延迟故障隔离。 */\n    private volatile boolean sendLatencyFaultEnable;",
    ),
    (
        "    private long[] latencyMax = {50L, 100L, 550L, 1800L, 3000L, 5000L, 15000L};",
        "    /** 延迟分级阈值（毫秒），用于映射隔离时长。 */\n    private long[] latencyMax = {50L, 100L, 550L, 1800L, 3000L, 5000L, 15000L};",
    ),
    (
        "    private long[] notAvailableDuration = {0L, 0L, 2000L, 5000L, 6000L, 10000L, 30000L};",
        "    /** 与 latencyMax 对应的隔离时长（毫秒）。 */\n    private long[] notAvailableDuration = {0L, 0L, 2000L, 5000L, 6000L, 10000L, 30000L};",
    ),
    (
        "    public static class BrokerFilter implements QueueFilter {",
        "    /** 过滤上次发送失败的 Broker，避免连续重试同一节点。 */\n    public static class BrokerFilter implements QueueFilter {",
    ),
    (
        "    private QueueFilter reachableFilter = new QueueFilter() {",
        "    /** 仅保留可达 Broker 的队列。 */\n    private QueueFilter reachableFilter = new QueueFilter() {",
    ),
    (
        "    private QueueFilter availableFilter = new QueueFilter() {",
        "    /** 仅保留可用（隔离期已过）Broker 的队列。 */\n    private QueueFilter availableFilter = new QueueFilter() {",
    ),
    (
        "    public MQFaultStrategy(ClientConfig cc, Resolver fetcher, ServiceDetector serviceDetector) {",
        "    /** 根据 ClientConfig 创建默认 LatencyFaultToleranceImpl。 */\n    public MQFaultStrategy(ClientConfig cc, Resolver fetcher, ServiceDetector serviceDetector) {",
    ),
    (
        "    // For unit test.",
        "    /** 单元测试用：注入自定义 LatencyFaultTolerance。 */",
    ),
    (
        "    public MessageQueue selectOneMessageQueue(final TopicPublishInfo tpInfo, final String lastBrokerName, final boolean resetIndex) {",
        "    /** 按故障策略选取队列：先可用、再可达、最后兜底轮询。 */\n    public MessageQueue selectOneMessageQueue(final TopicPublishInfo tpInfo, final String lastBrokerName, final boolean resetIndex) {",
    ),
    (
        "    public void updateFaultItem(final String brokerName, final long currentLatency, boolean isolation,\n                                final boolean reachable) {",
        "    /** 发送完成后更新 Broker 故障项；isolation 为 true 时使用固定 10s 隔离。 */\n    public void updateFaultItem(final String brokerName, final long currentLatency, boolean isolation,\n                                final boolean reachable) {",
    ),
    (
        "    private long computeNotAvailableDuration(final long currentLatency) {",
        "    /** 按延迟分级查表得到对应隔离时长。 */\n    private long computeNotAvailableDuration(final long currentLatency) {",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/latency/Resolver.java"] = [
    (
        "public interface Resolver {",
        "/**\n * 名称解析器：将 Broker 名称解析为可探测的网络地址。\n */\npublic interface Resolver {",
    ),
    (
        "    String resolve(String name);",
        "    /** 解析 Broker 名称，返回地址；无法解析时返回 null。 */\n    String resolve(String name);",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/latency/ServiceDetector.java"] = [
    (
        "/**\n * Detect whether the remote service state is normal.\n */",
        "/**\n * 远程服务探测器：检测 Broker 等远端节点是否恢复正常。\n */",
    ),
    (
        "    /**\n     * Check if the remote service is normal.\n     * @param endpoint Service endpoint to check against\n     * @return true if the service is back to normal; false otherwise.\n     */",
        "    /**\n     * 探测远端服务是否可用。\n     * @param endpoint 待检测的服务端点地址\n     * @param timeoutMillis 超时毫秒数\n     * @return true 表示服务已恢复\n     */",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/lock/ReadWriteCASLock.java"] = [
    (
        "public class ReadWriteCASLock {",
        "/**\n * 基于 CAS 的读写锁：写锁独占，读锁共享；写锁获取前需等待所有读锁释放。\n */\npublic class ReadWriteCASLock {",
    ),
    (
        "    //true : can lock ; false : not lock",
        "    // true 表示可获取写锁；false 表示写锁已被占用",
    ),
    (
        "    private final AtomicBoolean writeLock = new AtomicBoolean(true);",
        "    /** 写锁标志：true 可写，false 写锁被持有。 */\n    private final AtomicBoolean writeLock = new AtomicBoolean(true);",
    ),
    (
        "    private final AtomicInteger readLock = new AtomicInteger(0);",
        "    /** 当前读锁持有计数。 */\n    private final AtomicInteger readLock = new AtomicInteger(0);",
    ),
    (
        "    public void acquireWriteLock() {",
        "    /** 自旋获取写锁，并等待所有读锁释放。 */\n    public void acquireWriteLock() {",
    ),
    (
        "    public void releaseWriteLock() {",
        "    /** 释放写锁。 */\n    public void releaseWriteLock() {",
    ),
    (
        "    public void acquireReadLock() {",
        "    /** 等待写锁可用后递增读锁计数。 */\n    public void acquireReadLock() {",
    ),
    (
        "    public void releaseReadLock() {",
        "    /** 递减读锁计数。 */\n    public void releaseReadLock() {",
    ),
    (
        "    public boolean getWriteLock() {",
        "    /** 是否可获取写锁（无读锁且写标志为 true）。 */\n    public boolean getWriteLock() {",
    ),
    (
        "    public boolean getReadLock() {",
        "    /** 是否可获取读锁（写标志为 true）。 */\n    public boolean getReadLock() {",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/producer/LocalTransactionState.java"] = [
    (
        "public enum LocalTransactionState {",
        "/**\n * 本地事务执行结果：Producer 回查时告知 Broker 提交、回滚或未知。\n */\npublic enum LocalTransactionState {",
    ),
    (
        "    COMMIT_MESSAGE,",
        "    /** 提交事务消息，对消费者可见。 */\n    COMMIT_MESSAGE,",
    ),
    (
        "    ROLLBACK_MESSAGE,",
        "    /** 回滚事务消息，丢弃该消息。 */\n    ROLLBACK_MESSAGE,",
    ),
    (
        "    UNKNOW,",
        "    /** 事务状态未知，等待 Broker 再次回查。 */\n    UNKNOW,",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/producer/MQProducer.java"] = [
    (
        "public interface MQProducer extends MQAdmin {",
        "/**\n * 消息生产者接口：提供同步/异步/单向发送、顺序/批量/事务消息及 Request-Reply 模式。\n * 继承 {@link MQAdmin} 的管理能力。\n */\npublic interface MQProducer extends MQAdmin {",
    ),
    (
        "    void start() throws MQClientException;",
        "    /** 启动 Producer，注册路由并初始化内部组件。 */\n    void start() throws MQClientException;",
    ),
    (
        "    void shutdown();",
        "    /** 关闭 Producer，释放资源。 */\n    void shutdown();",
    ),
    (
        "    List<MessageQueue> fetchPublishMessageQueues(final String topic) throws MQClientException;",
        "    /** 获取 Topic 的可发布队列列表。 */\n    List<MessageQueue> fetchPublishMessageQueues(final String topic) throws MQClientException;",
    ),
    (
        "    SendResult send(final Message msg) throws MQClientException, RemotingException, MQBrokerException,\n        InterruptedException;",
        "    /** 同步发送单条消息（默认超时）。 */\n    SendResult send(final Message msg) throws MQClientException, RemotingException, MQBrokerException,\n        InterruptedException;",
    ),
    (
        "    void send(final Message msg, final SendCallback sendCallback) throws MQClientException,\n        RemotingException, InterruptedException, MQBrokerException;",
        "    /** 异步发送单条消息，结果通过 SendCallback 回调。 */\n    void send(final Message msg, final SendCallback sendCallback) throws MQClientException,\n        RemotingException, InterruptedException, MQBrokerException;",
    ),
    (
        "    void sendOneway(final Message msg) throws MQClientException, RemotingException,\n        InterruptedException;",
        "    /** 单向发送，不等待 Broker 响应。 */\n    void sendOneway(final Message msg) throws MQClientException, RemotingException,\n        InterruptedException;",
    ),
    (
        "    SendResult send(final Message msg, final MessageQueue mq) throws MQClientException,\n        RemotingException, MQBrokerException, InterruptedException;",
        "    /** 同步发送到指定队列。 */\n    SendResult send(final Message msg, final MessageQueue mq) throws MQClientException,\n        RemotingException, MQBrokerException, InterruptedException;",
    ),
    (
        "    SendResult send(final Message msg, final MessageQueueSelector selector, final Object arg)\n        throws MQClientException, RemotingException, MQBrokerException, InterruptedException;",
        "    /** 通过自定义选择器选取队列并同步发送。 */\n    SendResult send(final Message msg, final MessageQueueSelector selector, final Object arg)\n        throws MQClientException, RemotingException, MQBrokerException, InterruptedException;",
    ),
    (
        "    TransactionSendResult sendMessageInTransaction(final Message msg,\n        final Object arg) throws MQClientException;",
        "    /** 发送事务消息，本地事务由 TransactionListener 执行。 */\n    TransactionSendResult sendMessageInTransaction(final Message msg,\n        final Object arg) throws MQClientException;",
    ),
    (
        "    //for batch",
        "    /** 批量发送相关 API */",
    ),
    (
        "    SendResult send(final Collection<Message> msgs) throws MQClientException, RemotingException, MQBrokerException,\n        InterruptedException;",
        "    /** 同步批量发送多条消息。 */\n    SendResult send(final Collection<Message> msgs) throws MQClientException, RemotingException, MQBrokerException,\n        InterruptedException;",
    ),
    (
        "    String recallMessage(String topic, String recallHandle)",
        "    /** 撤回已发送消息（需 Broker 支持）。 */\n    String recallMessage(String topic, String recallHandle)",
    ),
    (
        "    //for rpc",
        "    /** Request-Reply 模式相关 API */",
    ),
    (
        "    Message request(final Message msg, final long timeout) throws RequestTimeoutException, MQClientException,\n        RemotingException, MQBrokerException, InterruptedException;",
        "    /** 同步 Request-Reply：发送请求并阻塞等待响应消息。 */\n    Message request(final Message msg, final long timeout) throws RequestTimeoutException, MQClientException,\n        RemotingException, MQBrokerException, InterruptedException;",
    ),
    (
        "    void request(final Message msg, final RequestCallback requestCallback, final long timeout)\n        throws MQClientException, RemotingException, InterruptedException, MQBrokerException;",
        "    /** 异步 Request-Reply：响应通过 RequestCallback 回调。 */\n    void request(final Message msg, final RequestCallback requestCallback, final long timeout)\n        throws MQClientException, RemotingException, InterruptedException, MQBrokerException;",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/producer/MessageQueueSelector.java"] = [
    (
        "public interface MessageQueueSelector {",
        "/**\n * 消息队列选择器：Producer 发送时根据消息内容与参数自定义目标队列。\n */\npublic interface MessageQueueSelector {",
    ),
    (
        "    MessageQueue select(final List<MessageQueue> mqs, final Message msg, final Object arg);",
        "    /** 从候选队列中选取一个目标 MessageQueue。 */\n    MessageQueue select(final List<MessageQueue> mqs, final Message msg, final Object arg);",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/producer/RequestCallback.java"] = [
    (
        "public interface RequestCallback {",
        "/**\n * Request-Reply 异步回调：接收响应消息或异常。\n */\npublic interface RequestCallback {",
    ),
    (
        "    void onSuccess(final Message message);",
        "    /** 收到 Broker 响应消息时调用。 */\n    void onSuccess(final Message message);",
    ),
    (
        "    void onException(final Throwable e);",
        "    /** 请求失败或超时时调用。 */\n    void onException(final Throwable e);",
    ),
]

R["client/src/main/java/org/apache/rocketmq/client/producer/RequestFutureHolder.java"] = [
    (
        "public class RequestFutureHolder {",
        "/**\n * Request-Reply 全局单例：维护 correlationId → RequestResponseFuture 映射，\n * 并定时扫描超时请求触发回调。\n */\npublic class RequestFutureHolder {",
    ),
    (
        "    private static final RequestFutureHolder INSTANCE = new RequestFutureHolder();",
        "    /** 单例实例。 */\n    private static final RequestFutureHolder INSTANCE = new RequestFutureHolder();",
    ),
    (
        "    private ConcurrentHashMap<String, RequestResponseFuture> requestFutureTable = new ConcurrentHashMap<>();",
        "    /** 请求 ID → 异步 Future 映射表。 */\n    private ConcurrentHashMap<String, RequestResponseFuture> requestFutureTable = new ConcurrentHashMap<>();",
    ),
    (
        "    private final Set<DefaultMQProducerImpl> producerSet = new HashSet<>();",
        "    /** 引用此 Holder 的 Producer 集合，用于引用计数式启停扫描任务。 */\n    private final Set<DefaultMQProducerImpl> producerSet = new HashSet<>();",
    ),
    (
        "    private void scanExpiredRequest() {",
        "    /** 扫描并移除超时请求，触发 onException 回调。 */\n    private void scanExpiredRequest() {",
    ),
    (
        "    public synchronized void startScheduledTask(DefaultMQProducerImpl producer) {",
        "    /** Producer 启动时注册并懒启动超时扫描定时任务（3s 后首次，每 1s 执行）。 */\n    public synchronized void startScheduledTask(DefaultMQProducerImpl producer) {",
    ),
    (
        "    public synchronized void shutdown(DefaultMQProducerImpl producer) {",
        "    /** Producer 关闭时注销；无引用后关闭扫描线程池。 */\n    public synchronized void shutdown(DefaultMQProducerImpl producer) {",
    ),
    (
        "    public static RequestFutureHolder getInstance() {",
        "    /** 返回全局单例。 */\n    public static RequestFutureHolder getInstance() {",
    ),
]
