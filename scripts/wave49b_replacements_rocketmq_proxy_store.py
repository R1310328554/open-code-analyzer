"""RocketMQ 5.5.0 wave49b proxy/remoting/store [15:30] Chinese annotation replacements."""

R: dict[str, list[tuple[str, str]]] = {}

# --- DefaultMessagingProcessor ---
R["proxy/src/main/java/org/apache/rocketmq/proxy/processor/DefaultMessagingProcessor.java"] = [
    (
        "public class DefaultMessagingProcessor extends AbstractStartAndShutdown implements MessagingProcessor {",
        "/**\n * Proxy 消息处理默认实现：聚合 {@link ProducerProcessor}、{@link ConsumerProcessor}、\n * {@link TransactionProcessor} 等子处理器，对外提供统一的生产/消费/POP/事务 API。\n * 支持 LOCAL 模式（嵌入 Broker）与 CLUSTER 模式（远程转发）。\n */\npublic class DefaultMessagingProcessor extends AbstractStartAndShutdown implements MessagingProcessor {",
    ),
    (
        "    protected ServiceManager serviceManager;",
        "    /** 底层服务管理器，封装路由、元数据与消息服务。 */\n    protected ServiceManager serviceManager;",
    ),
    (
        "    protected ProducerProcessor producerProcessor;",
        "    /** 生产侧请求处理器。 */\n    protected ProducerProcessor producerProcessor;",
    ),
    (
        "    protected ConsumerProcessor consumerProcessor;",
        "    /** 消费侧请求处理器（Pull/POP/ACK/offset）。 */\n    protected ConsumerProcessor consumerProcessor;",
    ),
    (
        "    protected ReceiptHandleProcessor receiptHandleProcessor;",
        "    /** POP 回执句柄生命周期管理（续期、清理）。 */\n    protected ReceiptHandleProcessor receiptHandleProcessor;",
    ),
    (
        "    public static DefaultMessagingProcessor createForLocalMode(BrokerController brokerController) {",
        "    /** LOCAL 模式工厂：Proxy 与 Broker 同进程，直接调用本地 {@link ServiceManager}。 */\n    public static DefaultMessagingProcessor createForLocalMode(BrokerController brokerController) {",
    ),
    (
        "    public static DefaultMessagingProcessor createForClusterMode() {",
        "    /** CLUSTER 模式工厂：按 ACL 配置构造 RPCHook 并创建远程服务管理器。 */\n    public static DefaultMessagingProcessor createForClusterMode() {",
    ),
    (
        "    protected void init() {",
        "    /** 注册 ServiceManager、ReceiptHandleProcessor 及线程池到生命周期管理。 */\n    protected void init() {",
    ),
    (
        "    public CompletableFuture<List<SendResult>> sendMessage(ProxyContext ctx, QueueSelector queueSelector,",
        "    /** 委托 {@link ProducerProcessor} 发送消息。 */\n    public CompletableFuture<List<SendResult>> sendMessage(ProxyContext ctx, QueueSelector queueSelector,",
    ),
    (
        "    public CompletableFuture<PopResult> popMessage(",
        "    /** 委托 {@link ConsumerProcessor} 执行 POP 长轮询消费。 */\n    public CompletableFuture<PopResult> popMessage(",
    ),
    (
        "    public CompletableFuture<AckResult> ackMessage(ProxyContext ctx, ReceiptHandle handle, String messageId,",
        "    /** 委托 {@link ConsumerProcessor} 确认 POP 消息。 */\n    public CompletableFuture<AckResult> ackMessage(ProxyContext ctx, ReceiptHandle handle, String messageId,",
    ),
    (
        "    public void registerConsumer(ProxyContext ctx, String consumerGroup, ClientChannelInfo clientChannelInfo,",
        "    /** 注册消费者连接并同步订阅关系。 */\n    public void registerConsumer(ProxyContext ctx, String consumerGroup, ClientChannelInfo clientChannelInfo,",
    ),
    (
        "    public void doChannelCloseEvent(String remoteAddr, Channel channel) {",
        "    /** 通道关闭时清理生产/消费注册信息及回执句柄。 */\n    public void doChannelCloseEvent(String remoteAddr, Channel channel) {",
    ),
    (
        "    public void addReceiptHandle(ProxyContext ctx, Channel channel, String group, String msgID,",
        "    /** 缓存 POP 回执句柄供续期与 ACK 校验。 */\n    public void addReceiptHandle(ProxyContext ctx, Channel channel, String group, String msgID,",
    ),
]

# --- MessagingProcessor ---
R["proxy/src/main/java/org/apache/rocketmq/proxy/processor/MessagingProcessor.java"] = [
    (
        "public interface MessagingProcessor extends StartAndShutdown {",
        "/**\n * Proxy 消息处理门面接口：定义生产、消费、POP、事务、路由及客户端注册等异步 API。\n * gRPC/Remoting 层 Activity 均通过此接口与 Broker 或集群后端交互。\n */\npublic interface MessagingProcessor extends StartAndShutdown {",
    ),
    (
        "    long DEFAULT_TIMEOUT_MILLS = Duration.ofSeconds(2).toMillis();",
        "    /** 默认 RPC 超时（毫秒）。 */\n    long DEFAULT_TIMEOUT_MILLS = Duration.ofSeconds(2).toMillis();",
    ),
    (
        "    long INVISIBLE_TIME_MS = Duration.ofSeconds(1).toMillis();",
        "    /** POP 消息默认不可见时间下限（毫秒）。 */\n    long INVISIBLE_TIME_MS = Duration.ofSeconds(1).toMillis();",
    ),
    (
        "    SubscriptionGroupConfig getSubscriptionGroupConfig(",
        "    /** 查询消费组订阅配置。 */\n    SubscriptionGroupConfig getSubscriptionGroupConfig(",
    ),
    (
        "    ProxyTopicRouteData getTopicRouteDataForProxy(",
        "    /** 为 Proxy 客户端返回 Topic 路由（含 Broker 地址与队列分布）。 */\n    ProxyTopicRouteData getTopicRouteDataForProxy(",
    ),
    (
        "    CompletableFuture<List<SendResult>> sendMessage(",
        "    /** 异步发送消息到选定队列。 */\n    CompletableFuture<List<SendResult>> sendMessage(",
    ),
    (
        "    CompletableFuture<RemotingCommand> forwardMessageToDeadLetterQueue(",
        "    /** 将 POP/Pull 失败消息转发到死信队列（DLQ）。 */\n    CompletableFuture<RemotingCommand> forwardMessageToDeadLetterQueue(",
    ),
    (
        "    CompletableFuture<Void> endTransaction(",
        "    /** 提交或回滚半事务消息。 */\n    CompletableFuture<Void> endTransaction(",
    ),
    (
        "    CompletableFuture<PopResult> popMessage(",
        "    /** POP 模式拉取消息（支持长轮询与 FIFO）。 */\n    CompletableFuture<PopResult> popMessage(",
    ),
    (
        "    CompletableFuture<PullResult> pullMessage(",
        "    /** Pull 模式按 offset 拉取消息。 */\n    CompletableFuture<PullResult> pullMessage(",
    ),
    (
        "    CompletableFuture<Void> updateConsumerOffset(",
        "    /** 同步更新消费位点。 */\n    CompletableFuture<Void> updateConsumerOffset(",
    ),
    (
        "    CompletableFuture<Set<MessageQueue>> lockBatchMQ(",
        "    /** 批量锁定 MessageQueue（顺序消费 rebalance 用）。 */\n    CompletableFuture<Set<MessageQueue>> lockBatchMQ(",
    ),
    (
        "    CompletableFuture<String> recallMessage(",
        "    /** 按 recallHandle 撤回已发送消息。 */\n    CompletableFuture<String> recallMessage(",
    ),
    (
        "    void registerProducer(",
        "    /** 注册生产者客户端连接。 */\n    void registerProducer(",
    ),
    (
        "    int getUnackedMessageCount(ProxyContext ctx, Channel channel, String group);",
        "    /** 返回通道上指定消费组未 ACK 的 POP 消息数。 */\n    int getUnackedMessageCount(ProxyContext ctx, Channel channel, String group);",
    ),
]

# --- RemotingProtocolServer ---
R["proxy/src/main/java/org/apache/rocketmq/proxy/remoting/RemotingProtocolServer.java"] = [
    (
        "public class RemotingProtocolServer implements StartAndShutdown, RemotingProxyOutClient {",
        "/**\n * Proxy Remoting 协议服务端：基于 {@link NettyRemotingServer} 暴露经典 Remoting API，\n * 将 RequestCode 映射到各 Activity（Send/Pull/POP/ACK/事务等），\n * 并串联认证/授权/上下文初始化 Pipeline。\n */\npublic class RemotingProtocolServer implements StartAndShutdown, RemotingProxyOutClient {",
    ),
    (
        "    protected final MessagingProcessor messagingProcessor;",
        "    /** 消息处理门面，Activity 通过它访问 Broker 能力。 */\n    protected final MessagingProcessor messagingProcessor;",
    ),
    (
        "    protected final RemotingChannelManager remotingChannelManager;",
        "    /** Remoting 通道管理，维护客户端连接与转发上下文。 */\n    protected final RemotingChannelManager remotingChannelManager;",
    ),
    (
        "    protected final SendMessageActivity sendMessageActivity;",
        "    /** 发送/重试/DLQ 消息 Activity。 */\n    protected final SendMessageActivity sendMessageActivity;",
    ),
    (
        "    protected final PullMessageActivity pullMessageActivity;",
        "    /** Pull/POP/Lite Pull 消息 Activity。 */\n    protected final PullMessageActivity pullMessageActivity;",
    ),
    (
        "    protected final ThreadPoolExecutor sendMessageExecutor;",
        "    /** 发送类请求专用线程池。 */\n    protected final ThreadPoolExecutor sendMessageExecutor;",
    ),
    (
        "    protected final ThreadPoolExecutor pullMessageExecutor;",
        "    /** Pull/POP 类请求专用线程池。 */\n    protected final ThreadPoolExecutor pullMessageExecutor;",
    ),
    (
        "    protected void registerRemotingServer(RemotingServer remotingServer) {",
        "    /** 将各 RequestCode 注册到对应 Activity 与线程池。 */\n    protected void registerRemotingServer(RemotingServer remotingServer) {",
    ),
    (
        "        remotingServer.registerProcessor(RequestCode.SEND_MESSAGE, sendMessageActivity, this.sendMessageExecutor);",
        "        // 发送类：单条/批量/重试回 Broker\n        remotingServer.registerProcessor(RequestCode.SEND_MESSAGE, sendMessageActivity, this.sendMessageExecutor);",
    ),
    (
        "        remotingServer.registerProcessor(RequestCode.PULL_MESSAGE, pullMessageActivity, this.pullMessageExecutor);",
        "        // 拉取类：Pull / Lite Pull / POP\n        remotingServer.registerProcessor(RequestCode.PULL_MESSAGE, pullMessageActivity, this.pullMessageExecutor);",
    ),
    (
        "        remotingServer.registerProcessor(RequestCode.ACK_MESSAGE, consumerManagerActivity, this.updateOffsetExecutor);",
        "        // 消费位点与 ACK 走 updateOffset 线程池\n        remotingServer.registerProcessor(RequestCode.ACK_MESSAGE, consumerManagerActivity, this.updateOffsetExecutor);",
    ),
    (
        "    protected RequestPipeline createRequestPipeline(MessagingProcessor messagingProcessor) {",
        "    /** 构建 ContextInit → Authentication → Authorization 请求管道。 */\n    protected RequestPipeline createRequestPipeline(MessagingProcessor messagingProcessor) {",
    ),
    (
        "    protected void cleanExpireRequest() {",
        "    /** 定时清理超时未完成的异步 Remoting 请求。 */\n    protected void cleanExpireRequest() {",
    ),
]

# --- LocalMessageService ---
R["proxy/src/main/java/org/apache/rocketmq/proxy/service/message/LocalMessageService.java"] = [
    (
        "public class LocalMessageService implements MessageService {",
        "/**\n * LOCAL 模式消息服务：Proxy 与 Broker 同进程时，直接调用 Broker Processor\n *（如 {@link SendMessageProcessor}）处理发送/Pull/POP/ACK，无需网络转发。\n */\npublic class LocalMessageService implements MessageService {",
    ),
    (
        "    private final BrokerController brokerController;",
        "    /** 本地 Broker 控制器，提供各 NettyRequestProcessor。 */\n    private final BrokerController brokerController;",
    ),
    (
        "    private final ChannelManager channelManager;",
        "    /** 构造模拟 Channel 以适配 Broker Processor 接口。 */\n    private final ChannelManager channelManager;",
    ),
    (
        "    public CompletableFuture<List<SendResult>> sendMessage(ProxyContext ctx, AddressableMessageQueue messageQueue,",
        "    /** 本地调用 SendMessageProcessor 发送单条或批量消息。 */\n    public CompletableFuture<List<SendResult>> sendMessage(ProxyContext ctx, AddressableMessageQueue messageQueue,",
    ),
    (
        "    public CompletableFuture<RemotingCommand> sendMessageBack(ProxyContext ctx, ReceiptHandle handle, String messageId,",
        "    /** 本地执行消费失败重试（SendBack）请求。 */\n    public CompletableFuture<RemotingCommand> sendMessageBack(ProxyContext ctx, ReceiptHandle handle, String messageId,",
    ),
    (
        "    public CompletableFuture<Void> endTransactionOneway(ProxyContext ctx, String brokerName,",
        "    /** 本地单向提交/回滚事务消息。 */\n    public CompletableFuture<Void> endTransactionOneway(ProxyContext ctx, String brokerName,",
    ),
    (
        "    public CompletableFuture<AckResult> ackMessage(ProxyContext ctx, ReceiptHandle handle, String messageId,",
        "    /** 本地调用 AckMessageProcessor 确认 POP 消息。 */\n    public CompletableFuture<AckResult> ackMessage(ProxyContext ctx, ReceiptHandle handle, String messageId,",
    ),
    (
        "    public CompletableFuture<PopResult> popMessage(ProxyContext ctx, AddressableMessageQueue messageQueue,",
        "    /** 本地调用 PopMessageProcessor 执行 POP 消费。 */\n    public CompletableFuture<PopResult> popMessage(ProxyContext ctx, AddressableMessageQueue messageQueue,",
    ),
    (
        "        throw new NotImplementedException(\"pullMessage is not implemented in LocalMessageService\");",
        "        // LOCAL 模式 Pull 走 Pop/Push 路径，此处未实现\n        throw new NotImplementedException(\"pullMessage is not implemented in LocalMessageService\");",
    ),
    (
        "        throw new NotImplementedException(\"queryConsumerOffset is not implemented in LocalMessageService\");",
        "        // LOCAL 模式 offset 查询未实现（由 Broker 内部管理）\n        throw new NotImplementedException(\"queryConsumerOffset is not implemented in LocalMessageService\");",
    ),
    (
        "    public CompletableFuture<String> recallMessage(ProxyContext ctx, String brokerName,",
        "    /** 本地调用 RecallMessageProcessor 撤回已发送消息。 */\n    public CompletableFuture<String> recallMessage(ProxyContext ctx, String brokerName,",
    ),
    (
        "                    // Value of POP_CK is used to determine whether it is a pop retry,",
        "                    // POP_CK 属性标识是否为 POP 重试消息（topic 可能被 Broker 改写）",
    ),
]

# --- NettyRemotingAbstract ---
R["remoting/src/main/java/org/apache/rocketmq/remoting/netty/NettyRemotingAbstract.java"] = [
    (
        "public abstract class NettyRemotingAbstract {",
        "/**\n * Netty Remoting 抽象基类：管理请求/响应表、信号量流控、Processor 路由\n * 及请求分发/响应写回逻辑，{@link NettyRemotingClient} 与 {@link NettyRemotingServer} 共用。\n */\npublic abstract class NettyRemotingAbstract {",
    ),
    (
        "    /**\n     * Remoting logger instance.\n     */",
        "    /** Remoting 模块日志实例。 */",
    ),
    (
        "    /**\n     * Semaphore to limit maximum number of on-going one-way requests, which protects system memory footprint.\n     */",
        "    /** 限制并发 Oneway 请求数，防止内存膨胀。 */",
    ),
    (
        "    /**\n     * Semaphore to limit maximum number of on-going asynchronous requests, which protects system memory footprint.\n     */",
        "    /** 限制并发异步请求数，防止内存膨胀。 */",
    ),
    (
        "    /**\n     * This map caches all on-going requests.\n     */",
        "    /** opaque → 进行中的 {@link ResponseFuture} 映射。 */",
    ),
    (
        "    /**\n     * This container holds all processors per request code, aka, for each incoming request, we may look up the\n     * responding processor in this map to handle the request.\n     */",
        "    /** requestCode → (Processor, Executor) 路由表。 */",
    ),
    (
        "    /**\n     * Constructor, specifying capacity of one-way and asynchronous semaphores.\n     *\n     * @param permitsOneway Number of permits for one-way requests.\n     * @param permitsAsync  Number of permits for asynchronous requests.\n     */",
        "    /**\n     * 构造 Remoting 抽象层并初始化 Oneway/Async 信号量。\n     *\n     * @param permitsOneway Oneway 请求许可数\n     * @param permitsAsync  异步请求许可数\n     */",
    ),
    (
        "    /**\n     * Entry of incoming command processing.\n     *\n     * <p>\n     * <strong>Note:</strong>\n     * The incoming remoting command may be\n     * <ul>\n     * <li>An inquiry request from a remote peer component;</li>\n     * <li>A response to a previous request issued by this very participant.</li>\n     * </ul>\n     * </p>\n     *\n     * @param ctx Channel handler context.\n     * @param msg incoming remoting command.\n     */",
        "    /**\n     * 入站 Remoting 命令入口：区分请求与响应。\n     * <ul>\n     * <li>远端发起的请求 → {@link #processRequestCommand}；</li>\n     * <li>对本端先前请求的响应 → {@link #processResponseCommand}。</li>\n     * </ul>\n     *\n     * @param ctx 通道上下文\n     * @param msg 入站 Remoting 命令\n     */",
    ),
    (
        "    /**\n     * Process incoming request command issued by remote peer.\n     *\n     * @param ctx channel handler context.\n     * @param cmd request command.\n     */",
        "    /**\n     * 处理远端请求：查找 Processor、提交线程池或返回 BUSY/GO_AWAY。\n     *\n     * @param ctx 通道上下文\n     * @param cmd 请求命令\n     */",
    ),
    (
        "            //async execute task, current thread return directly",
        "            // 异步提交到业务线程池，Netty IO 线程立即返回",
    ),
    (
        "    /**\n     * Process response from remote peer to the previous issued requests.\n     *\n     * @param ctx channel handler context.\n     * @param cmd response command instance.\n     */",
        "    /**\n     * 处理远端响应：匹配 opaque 并完成 ResponseFuture 或触发回调。\n     *\n     * @param ctx 通道上下文\n     * @param cmd 响应命令\n     */",
    ),
    (
        "    /**\n     * Execute callback in callback executor. If callback executor is null, run directly in current thread\n     */",
        "    /** 在 callback 线程池执行 InvokeCallback；无池则在当前线程执行。 */",
    ),
    (
        "    /**\n     * <p>\n     * This method is periodically invoked to scan and expire deprecated request.\n     * </p>\n     */",
        "    /** 定时扫描 responseTable，清理超时请求并触发超时回调。 */",
    ),
    (
        "    public RemotingCommand invokeSyncImpl(final Channel channel, final RemotingCommand request,",
        "    /** 同步 RPC 实现：invokeImpl 后阻塞 get 直到超时。 */\n    public RemotingCommand invokeSyncImpl(final Channel channel, final RemotingCommand request,",
    ),
]

# --- NettyRemotingClient ---
R["remoting/src/main/java/org/apache/rocketmq/remoting/netty/NettyRemotingClient.java"] = [
    (
        "public class NettyRemotingClient extends NettyRemotingAbstract implements RemotingClient {",
        "/**\n * Netty Remoting 客户端：维护 addr → Channel 连接池、NameServer 轮询、\n * 同步/异步/Oneway 调用及 TLS/Socks5 代理支持。\n */\npublic class NettyRemotingClient extends NettyRemotingAbstract implements RemotingClient {",
    ),
    (
        "    private final ConcurrentMap<String /* addr */, ChannelWrapper> channelTables = new ConcurrentHashMap<>();",
        "    /** Broker/NameServer 地址 → 连接包装（含锁与创建时间）。 */\n    private final ConcurrentMap<String /* addr */, ChannelWrapper> channelTables = new ConcurrentHashMap<>();",
    ),
    (
        "    private final AtomicReference<List<String>> namesrvAddrList = new AtomicReference<>();",
        "    /** NameServer 地址列表（可热更新）。 */\n    private final AtomicReference<List<String>> namesrvAddrList = new AtomicReference<>();",
    ),
    (
        "    /**\n     * Invoke the callback methods in this executor when process response.\n     */",
        "    /** 异步响应回调执行线程池。 */",
    ),
    (
        "    public void start() {",
        "    /** 启动 Bootstrap、扫描线程与 NameServer 心跳。 */\n    public void start() {",
    ),
    (
        "    public void shutdown() {",
        "    /** 关闭所有 Channel、线程池与定时器。 */\n    public void shutdown() {",
    ),
    (
        "    public void updateNameServerAddressList(List<String> addrs) {",
        "    /** 更新 NameServer 地址并触发不可用地址清理。 */\n    public void updateNameServerAddressList(List<String> addrs) {",
    ),
    (
        "                    // Netty Socks5 Proxy",
        "                    // 为匹配 CIDR 的地址注入 Netty Socks5 代理 Handler",
    ),
    (
        "        // only affected by sync or async request, oneway is not included.",
        "        // 仅同步/异步请求占用 semaphoreAsync，Oneway 不计入",
    ),
]

# --- NettyRemotingServer ---
R["remoting/src/main/java/org/apache/rocketmq/remoting/netty/NettyRemotingServer.java"] = [
    (
        "public class NettyRemotingServer extends NettyRemotingAbstract implements RemotingServer {",
        "/**\n * Netty Remoting 服务端：绑定监听端口、装配 Pipeline（TLS/编解码/分发），\n * 支持多 SubRemotingServer 与 Epoll/NIO 双模式。\n */\npublic class NettyRemotingServer extends NettyRemotingAbstract implements RemotingServer {",
    ),
    (
        "    /**\n     * NettyRemotingServer may hold multiple SubRemotingServer, each server will be stored in this container with a\n     * ListenPort key.\n     */",
        "    /** 监听端口 → SubRemotingServer 映射（多端口场景）。 */",
    ),
    (
        "    public static final String HANDSHAKE_HANDLER_NAME = \"handshakeHandler\";",
        "    /** TLS 握手 Handler 名称。 */\n    public static final String HANDSHAKE_HANDLER_NAME = \"handshakeHandler\";",
    ),
    (
        "    protected final NettyServerHandler serverHandler = new NettyServerHandler();",
        "    /** 入站 RemotingCommand 解码后的业务 Handler。 */\n    protected final NettyServerHandler serverHandler = new NettyServerHandler();",
    ),
    (
        "    public void start() {",
        "    /** 绑定 listenPort 并启动 Boss/Selector 线程组。 */\n    public void start() {",
    ),
    (
        "    public void shutdown() {",
        "    /** 优雅关闭 ServerBootstrap 与线程池。 */\n    public void shutdown() {",
    ),
    (
        "    public void registerProcessor(final int requestCode, final NettyRequestProcessor processor,",
        "    /** 注册 requestCode 对应的 Processor 与业务线程池。 */\n    public void registerProcessor(final int requestCode, final NettyRequestProcessor processor,",
    ),
    (
        "    public void loadSslContext() {",
        "    /** 按 TlsMode 加载或刷新 SSL 上下文。 */\n    public void loadSslContext() {",
    ),
    (
        "            ChannelFuture sync = serverBootstrap.bind().sync();",
        "            // 绑定 listenPort 并注册到 remotingServerTable\n            ChannelFuture sync = serverBootstrap.bind().sync();",
    ),
    (
        "    // sharable handlers",
        "    // 可共享的 Pipeline Handler 实例",
    ),
    (
        "        TimerTask timerScanResponseTable = new TimerTask() {",
        "        // 定时扫描超时 ResponseFuture\n        TimerTask timerScanResponseTable = new TimerTask() {",
    ),
]

# --- RemotingCommand ---
R["remoting/src/main/java/org/apache/rocketmq/remoting/protocol/RemotingCommand.java"] = [
    (
        "public class RemotingCommand {",
        "/**\n * RocketMQ Remoting 协议命令载体：封装 request/response code、opaque、flag、\n * extFields 与 body，支持 JSON/RocketMQ 两种序列化及自定义 Header 反射编解码。\n */\npublic class RemotingCommand {",
    ),
    (
        "    public static final String SERIALIZE_TYPE_PROPERTY = \"rocketmq.serialize.type\";",
        "    /** JVM 属性：指定序列化类型（JSON/ROCKETMQ）。 */\n    public static final String SERIALIZE_TYPE_PROPERTY = \"rocketmq.serialize.type\";",
    ),
    (
        "    private static final int RPC_TYPE = 0; // 0, REQUEST_COMMAND",
        "    private static final int RPC_TYPE = 0; // 0=REQUEST_COMMAND",
    ),
    (
        "    private int opaque = requestId.getAndIncrement();",
        "    /** 请求/响应关联 ID，客户端生成递增。 */\n    private int opaque = requestId.getAndIncrement();",
    ),
    (
        "    public static RemotingCommand createRequestCommand(int code, CommandCustomHeader customHeader) {",
        "    /** 构造请求命令并绑定自定义 Header。 */\n    public static RemotingCommand createRequestCommand(int code, CommandCustomHeader customHeader) {",
    ),
    (
        "    public static RemotingCommand createResponseCommand(int code, String remark) {",
        "    /** 构造带 remark 的响应命令。 */\n    public static RemotingCommand createResponseCommand(int code, String remark) {",
    ),
    (
        "    public static RemotingCommand decode(final ByteBuffer byteBuffer) throws RemotingCommandException {",
        "    /** 从 ByteBuffer 解码 RemotingCommand（自动识别序列化类型）。 */\n    public static RemotingCommand decode(final ByteBuffer byteBuffer) throws RemotingCommandException {",
    ),
    (
        "    public void markResponseType() {",
        "    /** 标记为响应类型（RPC_TYPE 位置 1）。 */\n    public void markResponseType() {",
    ),
    (
        "    public void markOnewayRPC() {",
        "    /** 标记为 Oneway RPC（不期望响应）。 */\n    public void markOnewayRPC() {",
    ),
    (
        "    public ByteBuffer encode() {",
        "    /** 按当前 RPC 序列化类型编码为 ByteBuffer。 */\n    public ByteBuffer encode() {",
    ),
    (
        "    public CommandCustomHeader readCustomHeader() {",
        "    /** 将 extFields 反序列化到 customHeader 并缓存。 */\n    public CommandCustomHeader readCustomHeader() {",
    ),
    (
        "    public void makeCustomHeaderToNet() {",
        "    /** 将 customHeader 字段反射写入 extFields 供网络传输。 */\n    public void makeCustomHeaderToNet() {",
    ),
]

# --- TopicQueueMappingUtils ---
R["remoting/src/main/java/org/apache/rocketmq/remoting/protocol/statictopic/TopicQueueMappingUtils.java"] = [
    (
        "public class TopicQueueMappingUtils {",
        "/**\n * 静态 Topic 队列映射工具：管理逻辑队列 ↔ 物理 Broker 队列的 epoch/offset 映射，\n * 支持 Remapping、Leader 选举校验及 blockSeq 对齐。\n */\npublic class TopicQueueMappingUtils {",
    ),
    (
        "    public static final int DEFAULT_BLOCK_SEQ_SIZE = 10000;",
        "    /** 静态 Topic block 序列默认步长。 */\n    public static final int DEFAULT_BLOCK_SEQ_SIZE = 10000;",
    ),
    (
        "    public static class MappingAllocator {",
        "    /** 映射分配器：按 Broker 负载均衡分配逻辑队列到物理 Broker。 */\n    public static class MappingAllocator {",
    ),
    (
        "        //used for remapping",
        "        // Remapping 时保留旧 Broker 计数用于优先回迁",
    ),
    (
        "            //reduce the remapping",
        "            // Remapping 场景：优先选择旧映射中计数较少的 Broker",
    ),
    (
        "                //reduce the imbalance",
        "                // 非 Remapping：随机打散负载最低的 Broker 列表",
    ),
    (
        "    public static MappingAllocator buildMappingAllocator(Map<Integer, String> idToBroker, Map<String, Integer> brokerNumMap, Map<String, Integer> brokerNumMapBeforeRemapping) {",
        "    /** 构建 MappingAllocator 并刷新负载状态。 */\n    public static MappingAllocator buildMappingAllocator(Map<Integer, String> idToBroker, Map<String, Integer> brokerNumMap, Map<String, Integer> brokerNumMapBeforeRemapping) {",
    ),
    (
        "    public static Map.Entry<Long, Integer> findMaxEpochAndQueueNum(List<TopicQueueMappingDetail> mappingDetailList) {",
        "    /** 从映射详情列表中取最大 epoch 与对应 queueNum。 */\n    public static Map.Entry<Long, Integer> findMaxEpochAndQueueNum(List<TopicQueueMappingDetail> mappingDetailList) {",
    ),
    (
        "    public static String getLeaderBroker(List<LogicQueueMappingItem> items) {",
        "    /** 返回逻辑队列映射项中的 Leader Broker 名称。 */\n    public static String getLeaderBroker(List<LogicQueueMappingItem> items) {",
    ),
    (
        "    public static long blockSeqRoundUp(long offset, long blockSeqSize) {",
        "    /** 将逻辑 offset 向上对齐到 blockSeq 边界。 */\n    public static long blockSeqRoundUp(long offset, long blockSeqSize) {",
    ),
    (
        "    public static TopicRemappingDetailWrapper remappingStaticTopic(String topic, Map<String, TopicConfigAndQueueMapping> brokerConfigMap, Set<String> targetBrokers) {",
        "    /** 对静态 Topic 执行 Remapping，生成新 epoch 的队列分配方案。 */\n    public static TopicRemappingDetailWrapper remappingStaticTopic(String topic, Map<String, TopicConfigAndQueueMapping> brokerConfigMap, Set<String> targetBrokers) {",
    ),
    (
        "    public static LogicQueueMappingItem findLogicQueueMappingItem(List<LogicQueueMappingItem> mappingItems, long logicOffset, boolean ignoreNegative) {",
        "    /** 按逻辑 offset 查找覆盖该区间的 {@link LogicQueueMappingItem}。 */\n    public static LogicQueueMappingItem findLogicQueueMappingItem(List<LogicQueueMappingItem> mappingItems, long logicOffset, boolean ignoreNegative) {",
    ),
]

# --- CommitLog ---
R["store/src/main/java/org/apache/rocketmq/store/CommitLog.java"] = [
    (
        "/**\n * Store all metadata downtime for recovery, data protection reliability\n */\npublic class CommitLog implements Swappable {",
        "/**\n * Broker 消息顺序写日志（CommitLog）：所有 Topic 消息追加到 mmap 文件，\n * 是存储层核心；负责 putMessage、刷盘、HA 同步、异常/正常恢复及 CQ 分发。\n * 实现 {@link Swappable} 支持冷热数据换出。\n */\npublic class CommitLog implements Swappable {",
    ),
    (
        "    // Message's MAGIC CODE daa320a7",
        "    // 正常消息 MAGIC CODE（0xdaa320a7）",
    ),
    (
        "    // End of file empty MAGIC CODE cbd43194",
        "    // 文件末尾空白记录 MAGIC CODE（0xcbd43194）",
    ),
    (
        "    /**\n     * CRC32 Format: [PROPERTY_CRC32 + NAME_VALUE_SEPARATOR + 10-digit fixed-length string + PROPERTY_SEPARATOR]\n     */",
        "    /** 属性 CRC32 预留长度（PROPERTY_CRC32=xxx 格式）。 */",
    ),
    (
        "    protected volatile long confirmOffset = -1L;",
        "    /** 已确认可对外服务的最大物理 offset（HA/Controller 场景）。 */\n    protected volatile long confirmOffset = -1L;",
    ),
    (
        "    protected final PutMessageLock putMessageLock;",
        "    /** 写 CommitLog 全局锁（自旋/可重入/ABS 自适应）。 */\n    protected final PutMessageLock putMessageLock;",
    ),
    (
        "    public boolean load() {",
        "    /** 加载 CommitLog 目录下全部 MappedFile 并校验。 */\n    public boolean load() {",
    ),
    (
        "    /**\n     * Read CommitLog data, use data replication\n     */",
        "    /** 按物理 offset 读取 CommitLog 数据（HA 复制场景）。 */",
    ),
    (
        "    /**\n     * When the normal exit, data recovery, all memory data have been flush\n     *\n     * @throws RocksDBException only in rocksdb mode\n     */",
        "    /**\n     * 正常关机恢复：从尾部 MappedFile 回放并重建 CQ/Index。\n     *\n     * @throws RocksDBException 仅 RocksDB 模式\n     */",
    ),
    (
        "                    // It's safe to recover from this mapped file",
        "                    // 该文件末条消息 storeTimestamp 满足恢复条件",
    ),
    (
        "                // Normal data",
        "                // 合法消息：推进 offset 并 dispatch 到 CQ/Index",
    ),
    (
        "                // Come the end of the file, switch to the next file Since the",
        "                // 文件末尾空白记录：切换下一个 MappedFile",
    ),
    (
        "    /**\n     * check the message and returns the message size\n     *\n     * @return 0 Come the end of the file // >0 Normal messages // -1 Message checksum failure\n     */",
        "    /**\n     * 校验并解析单条 CommitLog 记录。\n     *\n     * @return 0 文件结束空白；>0 消息字节数；-1 校验失败\n     */",
    ),
    (
        "    /**\n     * @throws RocksDBException only in rocksdb mode\n     */\n    public void recoverAbnormally(long dispatchFromPhyOffset) throws RocksDBException {",
        "    /**\n     * 异常关机恢复：从最小 storeTimestamp 的 MappedFile 开始扫描重建。\n     *\n     * @throws RocksDBException 仅 RocksDB 模式\n     */\n    public void recoverAbnormally(long dispatchFromPhyOffset) throws RocksDBException {",
    ),
    (
        "        // recover by the minimum time stamp",
        "        // 按最小 storeTimestamp 定位起始恢复文件",
    ),
    (
        "    public CompletableFuture<PutMessageResult> asyncPutMessage(final MessageExtBrokerInner msg) {",
        "    /** 异步写入单条消息：加锁 → append → flush/HA → 返回 Future。 */\n    public CompletableFuture<PutMessageResult> asyncPutMessage(final MessageExtBrokerInner msg) {",
    ),
    (
        "    public CompletableFuture<PutMessageResult> asyncPutMessages(final MessageExtBatch messageExtBatch) {",
        "    /** 异步批量写入消息。 */\n    public CompletableFuture<PutMessageResult> asyncPutMessages(final MessageExtBatch messageExtBatch) {",
    ),
    (
        "    /**\n     * According to receive certain message or offset storage time if an error occurs, it returns -1\n     */",
        "    /** 读取指定 offset 处消息的 storeTimestamp；失败返回 -1。 */",
    ),
    (
        "    /**\n     * GroupCommit Service\n     */",
        "    /** 同步刷盘 GroupCommit 服务：批量等待 flush 完成后唤醒生产者。 */",
    ),
    (
        "                            // When transientStorePoolEnable is true, the messages in writeBuffer may not be committed",
        "                            // TransientPool 模式下 writeBuffer 落盘可能延迟，短暂 sleep 等待",
    ),
]

# --- ConsumeQueue ---
R["store/src/main/java/org/apache/rocketmq/store/ConsumeQueue.java"] = [
    (
        "public class ConsumeQueue implements ConsumeQueueInterface {",
        "/**\n * 经典 ConsumeQueue 实现：每个 Topic-QueueId 对应独立 CQ 文件，\n * 每条 20 字节索引指向 CommitLog 物理 offset，供 Pull/GetMessage 按逻辑 offset 定位消息。\n */\npublic class ConsumeQueue implements ConsumeQueueInterface {",
    ),
    (
        "    /**\n     * ConsumeQueue's store unit. Format:\n     * <pre>\n     * ┌───────────────────────────────┬───────────────────┬───────────────────────────────┐\n     * │    CommitLog Physical Offset  │      Body Size    │            Tag HashCode       │\n     * │          (8 Bytes)            │      (4 Bytes)    │             (8 Bytes)         │\n     * ├───────────────────────────────┴───────────────────┴───────────────────────────────┤\n     * │                                     Store Unit                                    │\n     * │                                                                                   │\n     * </pre>\n     * ConsumeQueue's store unit. Size: CommitLog Physical Offset(8) + Body Size(4) + Tag HashCode(8) = 20 Bytes\n     */",
        "    /**\n     * CQ 存储单元格式（共 20 字节）：\n     * CommitLog 物理 offset(8) + Body 大小(4) + Tag HashCode(8)。\n     */",
    ),
    (
        "    /**\n     * Minimum offset of the consume file queue that points to valid commit log record.\n     */",
        "    /** 指向有效 CommitLog 记录的最小逻辑 offset。 */",
    ),
    (
        "    private ConsumeQueueExt consumeQueueExt = null;",
        "    /** 可选扩展 CQ，存储过滤位图等非关键数据。 */\n    private ConsumeQueueExt consumeQueueExt = null;",
    ),
    (
        "    public long getOffsetInQueueByTime(final long timestamp) {",
        "    /** 按 storeTimestamp 二分查找最接近的逻辑 offset。 */\n    public long getOffsetInQueueByTime(final long timestamp) {",
    ),
    (
        "    public void putMessagePositionInfoWrapper(DispatchRequest request) {",
        "    /** 写入一条 CQ 索引（CommitLog offset + size + tagsCode）。 */\n    public void putMessagePositionInfoWrapper(DispatchRequest request) {",
    ),
    (
        "    /**\n     * Update minLogicOffset such that entries after it would point to valid commit log address.\n     *\n     * @param minCommitLogOffset Minimum commit log offset\n     */",
        "    /**\n     * 修正 minLogicOffset，使后续 CQ 条目指向有效 CommitLog 记录。\n     *\n     * @param minCommitLogOffset 最小有效 CommitLog 物理 offset\n     */",
    ),
    (
        "        // Check if the consume queue is the state of deprecation.",
        "        // CQ 已全部过期则跳过修正",
    ),
    (
        "            // Search from previous min logical offset. Typically, a consume queue file segment contains 300,000 entries",
        "            // 从上一次 minLogicOffset 附近搜索，减少全量扫描",
    ),
    (
        "    public CqUnit get(long offset) {",
        "    /** 读取逻辑 offset 对应的 CQ 单元（含物理 offset 与 tagsCode）。 */\n    public CqUnit get(long offset) {",
    ),
    (
        "    public ReferredIterator<CqUnit> iterateFrom(long startOffset) {",
        "    /** 从指定逻辑 offset 起迭代 CQ 条目。 */\n    public ReferredIterator<CqUnit> iterateFrom(long startOffset) {",
    ),
    (
        "    public int deleteExpiredFile(long offset) {",
        "    /** 删除逻辑 offset 之前的过期 CQ 文件并修正 minOffset。 */\n    public int deleteExpiredFile(long offset) {",
    ),
]

# --- ConsumeQueueExt ---
R["store/src/main/java/org/apache/rocketmq/store/ConsumeQueueExt.java"] = [
    (
        "/**\n * Extend of consume queue, to store something not important,\n * such as message store time, filter bit map and etc.\n * <p/>\n * <li>1. This class is used only by {@link ConsumeQueue}</li>\n * <li>2. And is weakly reliable.</li>\n * <li>3. Be careful, address returned is always less than 0.</li>\n * <li>4. Pls keep this file small.</li>\n */",
        "/**\n * ConsumeQueue 扩展文件：存储非关键辅助数据（storeTime、过滤位图等）。\n * <ul>\n * <li>仅由 {@link ConsumeQueue} 使用；</li>\n * <li>弱可靠，丢失不影响主链路；</li>\n * <li>返回地址恒为负值（与主 CQ 区分）；</li>\n * <li>宜保持文件小巧。</li>\n * </ul>\n */",
    ),
    (
        "    /**\n     * Addr can not exceed this value.For compatible.\n     */",
        "    /** 扩展地址上限（兼容旧版本，地址为负）。 */",
    ),
    (
        "    /**\n     * Constructor.\n     *\n     * @param topic topic\n     * @param queueId id of queue\n     * @param storePath root dir of files to store.\n     * @param mappedFileSize file size\n     * @param bitMapLength bit map length.\n     */",
        "    /**\n     * 构造扩展 CQ 并初始化 MappedFileQueue。\n     *\n     * @param topic          Topic 名\n     * @param queueId        队列 ID\n     * @param storePath      存储根目录\n     * @param mappedFileSize 单文件大小\n     * @param bitMapLength   过滤位图长度\n     */",
    ),
    (
        "    public static final int END_BLANK_DATA_LENGTH = 4;",
        "    /** 文件末尾空白标记长度（字节）。 */\n    public static final int END_BLANK_DATA_LENGTH = 4;",
    ),
    (
        "    public long put(final CqExtUnit cqExtUnit) {",
        "    /** 写入扩展单元（storeTime/过滤位图），返回 decorate 后的负地址。 */\n    public long put(final CqExtUnit cqExtUnit) {",
    ),
    (
        "    /**\n     * Check whether {@code address} point to extend file.\n     * <p>\n     * Just test {@code address} is less than 0.\n     * </p>\n     */",
        "    /** 判断 address 是否指向扩展 CQ（值为负）。 */",
    ),
    (
        "    /**\n     * Decorate {@code offset} from mapped file, in order to distinguish with tagsCode(saved in cq originally).\n     * <p>\n     * if {@code offset} is greater than or equal to 0, then return {@code offset} + {@link java.lang.Long#MIN_VALUE};\n     * else, just return {@code offset}\n     * </p>\n     *\n     * @return ext address(value is less than 0)\n     */",
        "    /**\n     * 将 MappedFile 内 offset 装饰为负地址，与主 CQ 的 tagsCode 区分。\n     *\n     * @return 扩展地址（小于 0）\n     */",
    ),
    (
        "    public void truncateByMinAddress(final long minAddress) {",
        "    /** 截断 minAddress 之前的扩展数据。 */\n    public void truncateByMinAddress(final long minAddress) {",
    ),
    (
        "    public boolean load() {",
        "    /** 加载扩展 CQ 目录下 MappedFile。 */\n    public boolean load() {",
    ),
    (
        "    public void recover() {",
        "    /** 恢复扩展 CQ：扫描并重置 maxAddress。 */\n    public void recover() {",
    ),
]

# --- DefaultMessageStore ---
R["store/src/main/java/org/apache/rocketmq/store/DefaultMessageStore.java"] = [
    (
        "public class DefaultMessageStore implements MessageStore {",
        "/**\n * Broker 默认消息存储实现：编排 CommitLog、ConsumeQueue、Index、HA、\n * Reput、Compaction、Timer/Trans RocksDB 等子系统，是存储层总入口。\n */\npublic class DefaultMessageStore implements MessageStore {",
    ),
    (
        "    // CommitLog",
        "    // 顺序写消息主体",
    ),
    (
        "    /**\n     * List of stores that require commitlog dispatch and recovery. Each store registers itself when loading.\n     */",
        "    /** 需在 CommitLog 恢复时分发的存储组件列表（CQ/Index/RocksDB 等）。 */",
    ),
    (
        "    // Max pull msg size",
        "    // Pull 单次最大消息体累计大小（128MB）",
    ),
    (
        "    // Refer the MessageStore of MasterBroker in the same process.",
        "    // 同进程 Slave 引用 Master 的 MessageStore（跨 BrokerGroup）",
    ),
    (
        "    private final LinkedList<CommitLogDispatcher> dispatcherList = new LinkedList<>();",
        "    /** CommitLog 分发链：BuildCQ → BuildIndex → BuildTransIndex → Compaction。 */\n    private final LinkedList<CommitLogDispatcher> dispatcherList = new LinkedList<>();",
    ),
    (
        "    /**\n     * @throws Exception\n     */\n    @Override\n    public void start() throws Exception {",
        "    /**\n     * 启动存储：加文件锁、启动 Reput/CommitLog/CQ/HA/定时任务。\n     *\n     * @throws Exception 加锁或子组件启动失败\n     */\n    @Override\n    public void start() throws Exception {",
    ),
    (
        "        // Checking is not necessary, as long as the dLedger's implementation exactly follows the definition of Recover,",
        "        // DLedger 模式下 recover 定义一致时可跳过 CQ offset 复检",
    ),
    (
        "    /**\n     * @throws IOException\n     */\n    @Override\n    public boolean load() {",
        "    /**\n     * 加载存储：CommitLog → CQ → Index → recover → 注册 dispatch store。\n     *\n     * @throws IOException 文件 IO 异常\n     */\n    @Override\n    public boolean load() {",
    ),
    (
        "            // load Commit Log",
        "            // 1. 加载 CommitLog",
    ),
    (
        "            // load Consume Queue",
        "            // 2. 加载 ConsumeQueue",
    ),
    (
        "    public PutMessageResult putMessage(MessageExtBrokerInner msg) {",
        "    /** 同步写入单条消息（阻塞等待 asyncPutMessage）。 */\n    public PutMessageResult putMessage(MessageExtBrokerInner msg) {",
    ),
    (
        "    public GetMessageResult getMessage(final String group, final String topic, final int queueId, final long offset,",
        "    /** Pull 读消息：经 CQ 索引定位 CommitLog 并应用 MessageFilter。 */\n    public GetMessageResult getMessage(final String group, final String topic, final int queueId, final long offset,",
    ),
    (
        "        //check request topic flag",
        "        // Compaction Topic 走 CompactionStore 读路径",
    ),
    (
        "    /**\n     * Register a store that requires commitlog dispatch and recovery. Each store should register itself when loading.\n     *\n     * @param store the store to register\n     */",
        "    /**\n     * 注册需在 CommitLog 恢复/分发阶段参与的存储组件。\n     *\n     * @param store 待注册组件\n     */",
    ),
    (
        "    public long getMaxPhyOffset() {",
        "    /** 返回 CommitLog 当前最大物理 offset。 */\n    public long getMaxPhyOffset() {",
    ),
    (
        "    public DispatchRequest checkMessageAndReturnSize(final ByteBuffer byteBuffer, final boolean checkCRC,",
        "    /** 委托 CommitLog 校验并解析消息（Reput/Recover 用）。 */\n    public DispatchRequest checkMessageAndReturnSize(final ByteBuffer byteBuffer, final boolean checkCRC,",
    ),
]

# --- MappedFileQueue ---
R["store/src/main/java/org/apache/rocketmq/store/MappedFileQueue.java"] = [
    (
        "public class MappedFileQueue implements Swappable {",
        "/**\n * MappedFile 队列：管理同一存储路径下按 offset 排序的 mmap 文件，\n * 提供 append、flush、commit、过期删除及按 offset 查找能力。\n * CommitLog 与 ConsumeQueue 均依赖此组件。\n */\npublic class MappedFileQueue implements Swappable {",
    ),
    (
        "    protected final CopyOnWriteArrayList<MappedFile> mappedFiles = new CopyOnWriteArrayList<>();",
        "    /** 按起始 offset 排序的 MappedFile 列表。 */\n    protected final CopyOnWriteArrayList<MappedFile> mappedFiles = new CopyOnWriteArrayList<>();",
    ),
    (
        "    protected long flushedWhere = 0;",
        "    /** 已刷盘（fsync）的最大物理 offset。 */\n    protected long flushedWhere = 0;",
    ),
    (
        "    protected long committedWhere = 0;",
        "    /** 已 commit 到 PageCache 的最大物理 offset。 */\n    protected long committedWhere = 0;",
    ),
    (
        "    /**\n     * Configuration flag to use RandomAccessFile instead of MappedByteBuffer for writing\n     */",
        "    /** 为 true 时用 RandomAccessFile 代替 mmap 写入。 */",
    ),
    (
        "    public boolean load() {",
        "    /** 扫描 storePath 加载全部 MappedFile 并排序。 */\n    public boolean load() {",
    ),
    (
        "    public MappedFile getLastMappedFile(final long startOffset) {",
        "    /** 获取可写入 startOffset 的最后一个 MappedFile，不足则预分配。 */\n    public MappedFile getLastMappedFile(final long startOffset) {",
    ),
    (
        "    public boolean flush(final int flushLeastPages) {",
        "    /** 刷盘：至少 flushLeastPages 页或全部脏页。 */\n    public boolean flush(final int flushLeastPages) {",
    ),
    (
        "    public synchronized boolean commit(final int commitLeastPages) {",
        "    /** commit：将 writeBuffer 提交到 FileChannel/PageCache。 */\n    public synchronized boolean commit(final int commitLeastPages) {",
    ),
    (
        "    /**\n     * Finds a mapped file by offset.\n     *\n     * @param offset Offset.\n     * @param returnFirstOnNotFound If the mapped file is not found, then return the first one.\n     * @return Mapped file or null (when not found and returnFirstOnNotFound is <code>false</code>).\n     */",
        "    /**\n     * 按物理 offset 定位 MappedFile。\n     *\n     * @param offset                目标 offset\n     * @param returnFirstOnNotFound 未命中时是否返回第一个文件\n     * @return MappedFile 或 null\n     */",
    ),
    (
        "    public int deleteExpiredFileByTime(",
        "    /** 按 storeTimestamp 删除过期 MappedFile。 */\n    public int deleteExpiredFileByTime(",
    ),
    (
        "    public void truncateDirtyFiles(long offset) {",
        "    /** 截断 offset 之后的脏数据并移除不完整文件。 */\n    public void truncateDirtyFiles(long offset) {",
    ),
]

# --- MessageExtEncoder ---
R["store/src/main/java/org/apache/rocketmq/store/MessageExtEncoder.java"] = [
    (
        "public class MessageExtEncoder {",
        "/**\n * CommitLog 消息编码器：将 {@link MessageExtBrokerInner} 序列化为\n * V1/V2 二进制格式写入 Netty ByteBuf，供 appendMessage 零拷贝落盘。\n */\npublic class MessageExtEncoder {",
    ),
    (
        "    // The maximum length of the message body.",
        "    // 消息体最大长度",
    ),
    (
        "    // The maximum length of the full message.",
        "    // 完整消息（含头+属性）最大长度",
    ),
    (
        "        //Reserve 64kb for encoding buffer outside body",
        "        // 为 header/properties 预留 64KB 编码缓冲",
    ),
    (
        "    public static int calMsgLength(MessageVersion messageVersion,",
        "    /** 计算含 properties 的完整消息字节长度。 */\n    public static int calMsgLength(MessageVersion messageVersion,",
    ),
    (
        "        return 4 //TOTALSIZE",
        "        return 4 // TOTALSIZE",
    ),
    (
        "            + 4 //MAGICCODE",
        "            + 4 // MAGICCODE",
    ),
    (
        "            + 8 //Prepared Transaction Offset",
        "            + 8 // 预提交事务 offset",
    ),
    (
        "    public PutMessageResult encode(MessageExtBrokerInner msgInner) {",
        "    /** 编码单条消息（含 properties）到内部 ByteBuf。 */\n    public PutMessageResult encode(MessageExtBrokerInner msgInner) {",
    ),
    (
        "    public PutMessageResult encodeWithoutProperties(MessageExtBrokerInner msgInner) {",
        "    /** 编码消息但不写入 properties（properties 单独 append）。 */\n    public PutMessageResult encodeWithoutProperties(MessageExtBrokerInner msgInner) {",
    ),
    (
        "    public ByteBuffer encode(final MessageExtBatch messageExtBatch, PutMessageContext putMessageContext) {",
        "    /** 编码批量消息到 ByteBuffer。 */\n    public ByteBuffer encode(final MessageExtBatch messageExtBatch, PutMessageContext putMessageContext) {",
    ),
    (
        "    public void updateEncoderBufferCapacity(int newMaxMessageBodySize) {",
        "    /** 动态扩容编码缓冲以适配更大的 maxMessageSize。 */\n    public void updateEncoderBufferCapacity(int newMaxMessageBodySize) {",
    ),
    (
        "        public MessageExtEncoder getEncoder() {",
        "        /** 返回线程绑定的编码器实例。 */\n        public MessageExtEncoder getEncoder() {",
    ),
]
