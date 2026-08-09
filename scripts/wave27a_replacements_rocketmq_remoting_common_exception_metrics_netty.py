"""Chinese JavaDoc replacements for RocketMQ wave27a remoting common/exception/metrics/netty [0:15]."""

R: dict[str, list[tuple[str, str]]] = {
    "remoting/src/main/java/org/apache/rocketmq/remoting/common/SemaphoreReleaseOnlyOnce.java": [
        (
            "public class SemaphoreReleaseOnlyOnce {",
            "/**\n * 信号量一次性释放包装：保证 {@link java.util.concurrent.Semaphore#release()} 至多执行一次，\n * 避免异步回调重复归还许可导致计数膨胀。\n */\npublic class SemaphoreReleaseOnlyOnce {",
        ),
        (
            "    private final AtomicBoolean released = new AtomicBoolean(false);",
            "    /** 是否已释放过许可的原子标记。 */\n    private final AtomicBoolean released = new AtomicBoolean(false);",
        ),
        (
            "    public SemaphoreReleaseOnlyOnce(Semaphore semaphore) {",
            "    /** 绑定待释放的目标信号量。 */\n    public SemaphoreReleaseOnlyOnce(Semaphore semaphore) {",
        ),
        (
            "    public void release() {",
            "    /** 首次调用时释放一次许可，后续调用忽略。 */\n    public void release() {",
        ),
        (
            "    public Semaphore getSemaphore() {",
            "    /** 返回内部持有的信号量实例。 */\n    public Semaphore getSemaphore() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/common/ServiceThread.java": [
        (
            "/**\n * Base class for background thread\n */",
            "/**\n * 后台服务线程基类：封装启动、优雅关闭与 join 等待逻辑。\n */",
        ),
        (
            "    private static final long JOIN_TIME = 90 * 1000;",
            "    /** shutdown 时等待线程结束的最大毫秒数。 */\n    private static final long JOIN_TIME = 90 * 1000;",
        ),
        (
            "    public ServiceThread() {",
            "    /** 以 {@link #getServiceName()} 命名并创建内部线程。 */\n    public ServiceThread() {",
        ),
        (
            "    public abstract String getServiceName();",
            "    /** 返回线程名称，供日志与监控标识。 */\n    public abstract String getServiceName();",
        ),
        (
            "    public void start() {",
            "    /** 启动后台线程。 */\n    public void start() {",
        ),
        (
            "    public void shutdown() {",
            "    /** 非中断方式请求停止并 join 线程。 */\n    public void shutdown() {",
        ),
        (
            "    public void shutdown(final boolean interrupt) {",
            "    /**\n     * 请求停止服务线程。\n     *\n     * @param interrupt 为 true 时对线程调用 {@link Thread#interrupt()}\n     */\n    public void shutdown(final boolean interrupt) {",
        ),
        (
            "    public long getJointime() {",
            "    /** 返回 shutdown 时 join 的超时毫秒数。 */\n    public long getJointime() {",
        ),
        (
            "    public boolean isStopped() {",
            "    /** 是否已收到停止信号。 */\n    public boolean isStopped() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/common/TlsMode.java": [
        (
            "/**\n * For server, three SSL modes are supported: disabled, permissive and enforcing.\n * <ol>\n *     <li><strong>disabled:</strong> SSL is not supported; any incoming SSL handshake will be rejected, causing connection closed.</li>\n *     <li><strong>permissive:</strong> SSL is optional, aka, server in this mode can serve client connections with or without SSL;</li>\n *     <li><strong>enforcing:</strong> SSL is required, aka, non SSL connection will be rejected.</li>\n * </ol>\n */",
            "/**\n * 服务端 TLS/SSL 工作模式枚举，支持三种策略：\n * <ol>\n *     <li><strong>disabled：</strong> 禁用 SSL；任何 SSL 握手将被拒绝并关闭连接。</li>\n *     <li><strong>permissive：</strong> 可选 SSL；服务端同时接受明文与 TLS 连接。</li>\n *     <li><strong>enforcing：</strong> 强制 SSL；非 TLS 连接将被拒绝。</li>\n * </ol>\n */",
        ),
        (
            "    DISABLED(\"disabled\"),",
            "    /** 禁用 TLS。 */\n    DISABLED(\"disabled\"),",
        ),
        (
            "    PERMISSIVE(\"permissive\"),",
            "    /** 可选 TLS（兼容明文）。 */\n    PERMISSIVE(\"permissive\"),",
        ),
        (
            "    ENFORCING(\"enforcing\");",
            "    /** 强制 TLS。 */\n    ENFORCING(\"enforcing\");",
        ),
        (
            "    public static TlsMode parse(String mode) {",
            "    /** 按配置字符串解析模式，无法识别时默认 {@link #PERMISSIVE}。 */\n    public static TlsMode parse(String mode) {",
        ),
        (
            "    public String getName() {",
            "    /** 返回模式对应的配置名称字符串。 */\n    public String getName() {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/exception/RemotingCommandException.java": [
        (
            "public class RemotingCommandException extends RemotingException {",
            "/**\n * Remoting 命令异常：序列化/反序列化或扩展头字段校验失败时抛出。\n */\npublic class RemotingCommandException extends RemotingException {",
        ),
        (
            "    public RemotingCommandException(String message) {",
            "    /** 以描述信息构造异常。 */\n    public RemotingCommandException(String message) {",
        ),
        (
            "    public RemotingCommandException(String message, Throwable cause) {",
            "    /** 以描述信息与根因构造异常。 */\n    public RemotingCommandException(String message, Throwable cause) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/exception/RemotingConnectException.java": [
        (
            "public class RemotingConnectException extends RemotingException {",
            "/**\n * Remoting 连接异常：无法与远端地址建立 TCP 连接时抛出。\n */\npublic class RemotingConnectException extends RemotingException {",
        ),
        (
            "    public RemotingConnectException(String addr) {",
            "    /** 连接 {@code addr} 失败。 */\n    public RemotingConnectException(String addr) {",
        ),
        (
            "    public RemotingConnectException(String addr, Throwable cause) {",
            "    /** 连接 {@code addr} 失败，并携带底层 I/O 异常。 */\n    public RemotingConnectException(String addr, Throwable cause) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/exception/RemotingException.java": [
        (
            "public class RemotingException extends Exception {",
            "/**\n * Remoting 层通用受检异常基类，涵盖连接、发送、超时与命令处理等错误。\n */\npublic class RemotingException extends Exception {",
        ),
        (
            "    public RemotingException(String message) {",
            "    /** 以描述信息构造异常。 */\n    public RemotingException(String message) {",
        ),
        (
            "    public RemotingException(String message, Throwable cause) {",
            "    /** 以描述信息与根因构造异常。 */\n    public RemotingException(String message, Throwable cause) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/exception/RemotingSendRequestException.java": [
        (
            "public class RemotingSendRequestException extends RemotingException {",
            "/**\n * Remoting 发送请求异常：向远端写入请求数据失败时抛出。\n */\npublic class RemotingSendRequestException extends RemotingException {",
        ),
        (
            "    public RemotingSendRequestException(String addr) {",
            "    /** 向 {@code addr} 发送请求失败。 */\n    public RemotingSendRequestException(String addr) {",
        ),
        (
            "    public RemotingSendRequestException(String addr, Throwable cause) {",
            "    /** 向 {@code addr} 发送请求失败，并携带底层异常。 */\n    public RemotingSendRequestException(String addr, Throwable cause) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/exception/RemotingTimeoutException.java": [
        (
            "public class RemotingTimeoutException extends RemotingException {",
            "/**\n * Remoting 超时异常：同步/异步调用在指定时间内未收到响应时抛出。\n */\npublic class RemotingTimeoutException extends RemotingException {",
        ),
        (
            "    public RemotingTimeoutException(String message) {",
            "    /** 以自定义超时描述构造异常。 */\n    public RemotingTimeoutException(String message) {",
        ),
        (
            "    public RemotingTimeoutException(String addr, long timeoutMillis) {",
            "    /** 等待 {@code addr} 通道响应超过 {@code timeoutMillis} 毫秒。 */\n    public RemotingTimeoutException(String addr, long timeoutMillis) {",
        ),
        (
            "    public RemotingTimeoutException(String addr, long timeoutMillis, Throwable cause) {",
            "    /** 等待 {@code addr} 响应超时，并携带底层异常。 */\n    public RemotingTimeoutException(String addr, long timeoutMillis, Throwable cause) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/exception/RemotingTooMuchRequestException.java": [
        (
            "public class RemotingTooMuchRequestException extends RemotingException {",
            "/**\n * Remoting 并发限流异常：异步/oneway 请求超过信号量许可上限时抛出。\n */\npublic class RemotingTooMuchRequestException extends RemotingException {",
        ),
        (
            "    public RemotingTooMuchRequestException(String message) {",
            "    /** 以限流说明信息构造异常。 */\n    public RemotingTooMuchRequestException(String message) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/metrics/RemotingMetricsConstant.java": [
        (
            "public class RemotingMetricsConstant {",
            "/** Remoting 层 OpenTelemetry 指标名与标签常量定义。 */\npublic class RemotingMetricsConstant {",
        ),
        (
            "    public static final String HISTOGRAM_RPC_LATENCY = \"rocketmq_rpc_latency\";",
            "    /** RPC 延迟直方图指标名。 */\n    public static final String HISTOGRAM_RPC_LATENCY = \"rocketmq_rpc_latency\";",
        ),
        (
            "    public static final String LABEL_PROTOCOL_TYPE = \"protocol_type\";",
            "    /** 协议类型标签键。 */\n    public static final String LABEL_PROTOCOL_TYPE = \"protocol_type\";",
        ),
        (
            "    public static final String LABEL_REQUEST_CODE = \"request_code\";",
            "    /** Remoting 请求码标签键。 */\n    public static final String LABEL_REQUEST_CODE = \"request_code\";",
        ),
        (
            "    public static final String LABEL_RESPONSE_CODE = \"response_code\";",
            "    /** Remoting 响应码标签键。 */\n    public static final String LABEL_RESPONSE_CODE = \"response_code\";",
        ),
        (
            "    public static final String LABEL_IS_LONG_POLLING = \"is_long_polling\";",
            "    /** 是否为长轮询请求的标签键。 */\n    public static final String LABEL_IS_LONG_POLLING = \"is_long_polling\";",
        ),
        (
            "    public static final String LABEL_RESULT = \"result\";",
            "    /** RPC 结果标签键（成功/取消/写失败等）。 */\n    public static final String LABEL_RESULT = \"result\";",
        ),
        (
            "    public static final String PROTOCOL_TYPE_REMOTING = \"remoting\";",
            "    /** 协议类型标签值：Remoting 协议。 */\n    public static final String PROTOCOL_TYPE_REMOTING = \"remoting\";",
        ),
        (
            "    public static final String RESULT_ONEWAY = \"oneway\";",
            "    /** 结果标签值：oneway 调用。 */\n    public static final String RESULT_ONEWAY = \"oneway\";",
        ),
        (
            "    public static final String RESULT_SUCCESS = \"success\";",
            "    /** 结果标签值：成功。 */\n    public static final String RESULT_SUCCESS = \"success\";",
        ),
        (
            "    public static final String RESULT_CANCELED = \"cancelled\";",
            "    /** 结果标签值：写通道任务被取消。 */\n    public static final String RESULT_CANCELED = \"cancelled\";",
        ),
        (
            "    public static final String RESULT_PROCESS_REQUEST_FAILED = \"process_request_failed\";",
            "    /** 结果标签值：服务端处理请求失败。 */\n    public static final String RESULT_PROCESS_REQUEST_FAILED = \"process_request_failed\";",
        ),
        (
            "    public static final String RESULT_WRITE_CHANNEL_FAILED = \"write_channel_failed\";",
            "    /** 结果标签值：向 Netty 通道写数据失败。 */\n    public static final String RESULT_WRITE_CHANNEL_FAILED = \"write_channel_failed\";",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/metrics/RemotingMetricsManager.java": [
        (
            "public class RemotingMetricsManager {",
            "/**\n * Remoting 指标管理器：注册 RPC 延迟直方图并统一构建 OpenTelemetry 属性。\n */\npublic class RemotingMetricsManager {",
        ),
        (
            "    private LongHistogram rpcLatency = new NopLongHistogram();",
            "    /** RPC 延迟直方图，未初始化时为无操作实现。 */\n    private LongHistogram rpcLatency = new NopLongHistogram();",
        ),
        (
            "    public AttributesBuilder newAttributesBuilder() {",
            "    /** 创建带默认 protocol_type=remoting 标签的属性构建器。 */\n    public AttributesBuilder newAttributesBuilder() {",
        ),
        (
            "    public void initMetrics(Meter meter, Supplier<AttributesBuilder> attributesBuilderSupplier) {",
            "    /**\n     * 注册 RPC 延迟直方图并绑定公共属性提供者。\n     *\n     * @param meter OpenTelemetry Meter\n     * @param attributesBuilderSupplier 公共标签构建器工厂\n     */\n    public void initMetrics(Meter meter, Supplier<AttributesBuilder> attributesBuilderSupplier) {",
        ),
        (
            "            .setDescription(\"Rpc latency\")",
            "            .setDescription(\"RPC 调用延迟\")",
        ),
        (
            "    public List<Pair<InstrumentSelector, ViewBuilder>> getMetricsView() {",
            "    /** 返回 RPC 延迟直方图的桶边界视图配置。 */\n    public List<Pair<InstrumentSelector, ViewBuilder>> getMetricsView() {",
        ),
        (
            "    public String getWriteAndFlushResult(Future<?> future) {",
            "    /** 根据 Netty writeAndFlush 的 {@link Future} 状态映射为 result 标签值。 */\n    public String getWriteAndFlushResult(Future<?> future) {",
        ),
        (
            "    // Getter methods for external access",
            "    // 供外部读取已注册指标的 getter",
        ),
        (
            "    // Setter methods for testing",
            "    // 供单元测试注入 mock 依赖的 setter",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/netty/AttributeKeys.java": [
        (
            "public class AttributeKeys {",
            "/**\n * Netty {@link AttributeKey} 常量：在 Channel 上缓存远端地址、客户端 ID、版本与 HAProxy 信息。\n */\npublic class AttributeKeys {",
        ),
        (
            "    public static final AttributeKey<String> REMOTE_ADDR_KEY = AttributeKey.valueOf(\"RemoteAddr\");",
            "    /** 对端 Remoting 地址字符串。 */\n    public static final AttributeKey<String> REMOTE_ADDR_KEY = AttributeKey.valueOf(\"RemoteAddr\");",
        ),
        (
            "    public static final AttributeKey<String> CLIENT_ID_KEY = AttributeKey.valueOf(\"ClientId\");",
            "    /** 客户端实例标识。 */\n    public static final AttributeKey<String> CLIENT_ID_KEY = AttributeKey.valueOf(\"ClientId\");",
        ),
        (
            "    public static final AttributeKey<Integer> VERSION_KEY = AttributeKey.valueOf(\"Version\");",
            "    /** Remoting 协议版本号。 */\n    public static final AttributeKey<Integer> VERSION_KEY = AttributeKey.valueOf(\"Version\");",
        ),
        (
            "    public static final AttributeKey<LanguageCode> LANGUAGE_CODE_KEY = AttributeKey.valueOf(\"LanguageCode\");",
            "    /** 客户端语言/实现类型。 */\n    public static final AttributeKey<LanguageCode> LANGUAGE_CODE_KEY = AttributeKey.valueOf(\"LanguageCode\");",
        ),
        (
            "    public static final AttributeKey<String> PROXY_PROTOCOL_ADDR =",
            "    /** HAProxy PROXY 协议解析出的客户端地址。 */\n    public static final AttributeKey<String> PROXY_PROTOCOL_ADDR =",
        ),
        (
            "    public static final AttributeKey<String> PROXY_PROTOCOL_PORT =",
            "    /** HAProxy PROXY 协议解析出的客户端端口。 */\n    public static final AttributeKey<String> PROXY_PROTOCOL_PORT =",
        ),
        (
            "    public static final AttributeKey<String> PROXY_PROTOCOL_SERVER_ADDR =",
            "    /** HAProxy PROXY 协议解析出的服务端地址。 */\n    public static final AttributeKey<String> PROXY_PROTOCOL_SERVER_ADDR =",
        ),
        (
            "    public static final AttributeKey<String> PROXY_PROTOCOL_SERVER_PORT =",
            "    /** HAProxy PROXY 协议解析出的服务端端口。 */\n    public static final AttributeKey<String> PROXY_PROTOCOL_SERVER_PORT =",
        ),
        (
            "    public static AttributeKey<String> valueOf(String name) {",
            "    /** 按名称获取或创建并缓存字符串型 {@link AttributeKey}。 */\n    public static AttributeKey<String> valueOf(String name) {",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/netty/FileRegionEncoder.java": [
        (
            "/**\n * <p>\n *     By default, file region are directly transferred to socket channel which is known as zero copy. In case we need\n *     to encrypt transmission, data being sent should go through the {@link SslHandler}. This encoder ensures this\n *     process.\n * </p>\n */",
            "/**\n * <p>\n *     默认情况下 {@link FileRegion} 经零拷贝直接写入 Socket；启用 TLS 时需经 {@link SslHandler}\n *     加密，本编码器将文件区域读入 {@link ByteBuf} 以走 SSL 管道。\n * </p>\n */",
        ),
        (
            "    /**\n     * Encode a message into a {@link io.netty.buffer.ByteBuf}. This method will be called for each written message that\n     * can be handled by this encoder.\n     *\n     * @param ctx the {@link io.netty.channel.ChannelHandlerContext} which this {@link\n     * io.netty.handler.codec.MessageToByteEncoder} belongs to\n     * @param msg the message to encode\n     * @param out the {@link io.netty.buffer.ByteBuf} into which the encoded message will be written\n     * @throws Exception is thrown if an error occurs\n     */",
            "    /**\n     * 将 {@link FileRegion} 分块读入 {@code out}，供后续 SSL 加密发送。\n     *\n     * @param ctx 当前 {@link ChannelHandlerContext}\n     * @param msg 待编码的文件区域\n     * @param out 目标 {@link ByteBuf}\n     * @throws Exception 传输过程中发生 I/O 错误时抛出\n     */",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/netty/NettyClientConfig.java": [
        (
            "public class NettyClientConfig {",
            "/**\n * Netty Remoting 客户端配置：线程池、信号量、超时、TLS 与写缓冲水位等参数。\n */\npublic class NettyClientConfig {",
        ),
        (
            "    /**\n     * Worker thread number\n     */",
            "    /** Netty 客户端 Worker 线程数。 */",
        ),
        (
            "    /**\n     * IdleStateEvent will be triggered when neither read nor write was performed for\n     * the specified period of this time. Specify {@code 0} to disable\n     */",
            "    /**\n     * 通道读写均空闲超过该秒数时触发 IdleStateEvent；{@code 0} 表示禁用。\n     */",
        ),
        (
            "    private int clientCallbackExecutorThreads = Runtime.getRuntime().availableProcessors();",
            "    /** 异步回调线程池大小，默认为 CPU 核数。 */\n    private int clientCallbackExecutorThreads = Runtime.getRuntime().availableProcessors();",
        ),
        (
            "    private int clientOnewaySemaphoreValue = NettySystemConfig.CLIENT_ONEWAY_SEMAPHORE_VALUE;",
            "    /** oneway 并发请求信号量上限。 */\n    private int clientOnewaySemaphoreValue = NettySystemConfig.CLIENT_ONEWAY_SEMAPHORE_VALUE;",
        ),
        (
            "    private int clientAsyncSemaphoreValue = NettySystemConfig.CLIENT_ASYNC_SEMAPHORE_VALUE;",
            "    /** 异步 RPC 并发请求信号量上限。 */\n    private int clientAsyncSemaphoreValue = NettySystemConfig.CLIENT_ASYNC_SEMAPHORE_VALUE;",
        ),
        (
            "    private int connectTimeoutMillis = NettySystemConfig.connectTimeoutMillis;",
            "    /** TCP 连接超时毫秒数。 */\n    private int connectTimeoutMillis = NettySystemConfig.connectTimeoutMillis;",
        ),
        (
            "    private long channelNotActiveInterval = 1000 * 60;",
            "    /** 通道非活跃状态判定间隔（毫秒）。 */\n    private long channelNotActiveInterval = 1000 * 60;",
        ),
        (
            "    private boolean isScanAvailableNameSrv = true;",
            "    /** 是否扫描并连接可用的 NameServer 地址。 */\n    private boolean isScanAvailableNameSrv = true;",
        ),
        (
            "    private boolean useTLS = Boolean.parseBoolean(System.getProperty(TLS_ENABLE,",
            "    /** 是否启用 TLS，可由系统属性 {@link TlsSystemConfig#TLS_ENABLE} 覆盖。 */\n    private boolean useTLS = Boolean.parseBoolean(System.getProperty(TLS_ENABLE,",
        ),
        (
            "    private boolean disableCallbackExecutor = false;",
            "    /** 为 true 时禁用独立回调线程池。 */\n    private boolean disableCallbackExecutor = false;",
        ),
        (
            "    private boolean disableNettyWorkerGroup = false;",
            "    /** 为 true 时禁用 Netty Worker EventLoopGroup。 */\n    private boolean disableNettyWorkerGroup = false;",
        ),
        (
            "    private long maxReconnectIntervalTimeSeconds = 60;",
            "    /** 断线重连的最大间隔秒数。 */\n    private long maxReconnectIntervalTimeSeconds = 60;",
        ),
        (
            "    private boolean enableReconnectForGoAway = true;",
            "    /** 收到 HTTP/2 GOAWAY 等信号时是否自动重连。 */\n    private boolean enableReconnectForGoAway = true;",
        ),
    ],
    "remoting/src/main/java/org/apache/rocketmq/remoting/netty/NettyDecoder.java": [
        (
            "public class NettyDecoder extends LengthFieldBasedFrameDecoder {",
            "/**\n * Remoting 帧解码器：按 4 字节长度字段拆包并反序列化为 {@link RemotingCommand}。\n */\npublic class NettyDecoder extends LengthFieldBasedFrameDecoder {",
        ),
        (
            "    private static final int FRAME_MAX_LENGTH =",
            "    /** 单帧最大字节数，可通过系统属性 com.rocketmq.remoting.frameMaxLength 配置。 */\n    private static final int FRAME_MAX_LENGTH =",
        ),
        (
            "    public NettyDecoder() {",
            "    /** 使用默认最大帧长与长度字段偏移构造解码器。 */\n    public NettyDecoder() {",
        ),
        (
            "    public Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {",
            "    /**\n     * 解码一帧 Remoting 报文；失败时记录日志并关闭通道。\n     *\n     * @param ctx Netty 通道上下文\n     * @param in  输入字节缓冲\n     */\n    public Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {",
        ),
    ],
}
