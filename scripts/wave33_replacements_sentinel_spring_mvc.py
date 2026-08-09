"""Chinese JavaDoc replacements for Sentinel 1.8.10 wave33 final spring-mvc transport batch [0:5]."""

SPRING_MVC_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "SentinelApiHandlerMapping.java": [
        (
            "/**\n * @author shenbaoyong\n */",
            "/**\n * Spring MVC 命令 API 路由映射：将请求 URI 映射为 {@link SentinelApiHandler}。\n * 监听 Spring Boot {@code WebServerInitializedEvent} 自动回填运行时端口。\n *\n * @author shenbaoyong\n */",
        ),
        (
            "    private static final String SPRING_BOOT_WEB_SERVER_INITIALIZED_EVENT_CLASS = \"org.springframework.boot.web.context.WebServerInitializedEvent\";",
            "    /** Spring Boot Web 容器就绪事件类名，用于运行时探测。 */\n    private static final String SPRING_BOOT_WEB_SERVER_INITIALIZED_EVENT_CLASS = \"org.springframework.boot.web.context.WebServerInitializedEvent\";",
        ),
        (
            "    private static Class webServerInitializedEventClass;",
            "    /** 探测到的 WebServerInitializedEvent 类，非 Spring Boot 环境为 null。 */\n    private static Class webServerInitializedEventClass;",
        ),
        (
            "    final static Map<String, CommandHandler> handlerMap = new ConcurrentHashMap<>();",
            "    /** 命令名到 {@link CommandHandler} 的全局注册表。 */\n    final static Map<String, CommandHandler> handlerMap = new ConcurrentHashMap<>();",
        ),
        (
            "    private boolean ignoreInterceptor = true;",
            "    /** 为 true 时不挂载拦截器，仅返回 handler 链。 */\n    private boolean ignoreInterceptor = true;",
        ),
        (
            "    public SentinelApiHandlerMapping() {",
            "    /** 设置较低优先级，避免覆盖业务 HandlerMapping。 */\n    public SentinelApiHandlerMapping() {",
        ),
        (
            "    protected Object getHandlerInternal(HttpServletRequest request) throws Exception {",
            "    /** 按 URI（去掉前导 /）查找已注册命令，命中则返回 {@link SentinelApiHandler}。 */\n    protected Object getHandlerInternal(HttpServletRequest request) throws Exception {",
        ),
        (
            "    protected HandlerExecutionChain getHandlerExecutionChain(Object handler, HttpServletRequest request) {",
            "    /** ignoreInterceptor 为 true 时跳过拦截器，否则走父类默认链。 */\n    protected HandlerExecutionChain getHandlerExecutionChain(Object handler, HttpServletRequest request) {",
        ),
        (
            "    public void setIgnoreInterceptor(boolean ignoreInterceptor) {",
            "    /** 设置是否在 Handler 链中忽略拦截器。 */\n    public void setIgnoreInterceptor(boolean ignoreInterceptor) {",
        ),
        (
            "    public static void registerCommand(String commandName, CommandHandler handler) {",
            "    /** 注册单个命令处理器，重复命令名会被忽略并打 warn 日志。 */\n    public static void registerCommand(String commandName, CommandHandler handler) {",
        ),
        (
            "    public static void registerCommands(Map<String, CommandHandler> handlerMap) {",
            "    /** 批量注册命令处理器。 */\n    public static void registerCommands(Map<String, CommandHandler> handlerMap) {",
        ),
        (
            "    public void onApplicationEvent(ApplicationEvent applicationEvent) {",
            "    /** Spring Boot 启动完成后从事件中解析 Web 端口并写入 {@link TransportConfig}。 */\n    public void onApplicationEvent(ApplicationEvent applicationEvent) {",
        ),
    ],
    "SpringMvcHttpCommandCenter.java": [
        (
            "/**\n * @author shenbaoyong\n */",
            "/**\n * Spring MVC 命令中心 SPI 实现：复用宿主 Web 容器，无需独立监听端口。\n * 启动前将 SPI 加载的命令注册到 {@link SentinelApiHandlerMapping}。\n *\n * @author shenbaoyong\n */",
        ),
        (
            "    public void start() throws Exception {",
            "    /** 端口由 Spring MVC 容器提供，此处无需额外启动逻辑。 */\n    public void start() throws Exception {",
        ),
        (
            "    public void stop() throws Exception {",
            "    /** 无独立资源需释放。 */\n    public void stop() throws Exception {",
        ),
        (
            "        // Register handlers",
            "        // 注册 SPI 命令处理器",
        ),
        (
            "    public void beforeStart() throws Exception {",
            "    /** 注册 SPI 加载的全部命令处理器到 HandlerMapping。 */\n    public void beforeStart() throws Exception {",
        ),
    ],
    "StatusCode.java": [
        (
            "/**\n * @author Jason Joo\n */",
            "/**\n * Spring MVC 命令 API 使用的 HTTP 状态码枚举。\n *\n * @author Jason Joo\n */",
        ),
        (
            "    /**\n     * 200 OK.\n     */",
            "    /** 200 成功。 */",
        ),
        (
            "    BAD_REQUEST(400, \"Bad Request\"),",
            "    /** 400 请求格式或参数错误。 */\n    BAD_REQUEST(400, \"Bad Request\"),",
        ),
        (
            "    REQUEST_TIMEOUT(408, \"Request Timeout\"),",
            "    /** 408 请求超时。 */\n    REQUEST_TIMEOUT(408, \"Request Timeout\"),",
        ),
        (
            "    LENGTH_REQUIRED(411, \"Length Required\"),",
            "    /** 411 缺少 Content-Length。 */\n    LENGTH_REQUIRED(411, \"Length Required\"),",
        ),
        (
            "    UNSUPPORTED_MEDIA_TYPE(415, \"Unsupported Media Type\"),",
            "    /** 415 不支持的 Content-Type。 */\n    UNSUPPORTED_MEDIA_TYPE(415, \"Unsupported Media Type\"),",
        ),
        (
            "    INTERNAL_SERVER_ERROR(500, \"Internal Server Error\");",
            "    /** 500 服务端内部错误。 */\n    INTERNAL_SERVER_ERROR(500, \"Internal Server Error\");",
        ),
        (
            "    private int code;",
            "    /** HTTP 数字状态码。 */\n    private int code;",
        ),
        (
            "    private String desc;",
            "    /** 状态描述短语。 */\n    private String desc;",
        ),
        (
            "    private String representation;",
            "    /** 形如 \"200 OK\" 的完整状态行片段。 */\n    private String representation;",
        ),
        (
            "    public int getCode() {",
            "    /** @return HTTP 数字状态码。 */\n    public int getCode() {",
        ),
        (
            "    public String getDesc() {",
            "    /** @return 状态描述。 */\n    public String getDesc() {",
        ),
    ],
    "SpringMvcHttpHeartbeatSender.java": [
        (
            "/**\n * @author Eric Zhao\n * @author Carpenter Lee\n * @author Leo Li\n */",
            "/**\n * Spring MVC 环境下基于 Apache HttpClient 的心跳发送器：向 Dashboard 发起 GET 注册本机信息。\n * SPI 优先级 {@code ORDER_LOWEST - 100}，与 Netty 实现互斥加载。\n *\n * @author Eric Zhao\n * @author Carpenter Lee\n * @author Leo Li\n */",
        ),
        (
            "    private final CloseableHttpClient client;",
            "    /** Apache HttpClient 实例，按 Dashboard 协议（HTTP/HTTPS）创建。 */\n    private final CloseableHttpClient client;",
        ),
        (
            "    private static final int OK_STATUS = 200;",
            "    /** HTTP 200 视为心跳成功。 */\n    private static final int OK_STATUS = 200;",
        ),
        (
            "    private final int timeoutMs = 3000;",
            "    /** 连接与读超时（毫秒）。 */\n    private final int timeoutMs = 3000;",
        ),
        (
            "    private final Protocol consoleProtocol;",
            "    /** 首个 Dashboard 端点的通信协议。 */\n    private final Protocol consoleProtocol;",
        ),
        (
            "    private final String consoleHost;",
            "    /** Dashboard 主机名或 IP。 */\n    private final String consoleHost;",
        ),
        (
            "    private final int consolePort;",
            "    /** Dashboard 端口。 */\n    private final int consolePort;",
        ),
        (
            "        // Send heartbeat request.",
            "        // 发送心跳 GET 请求",
        ),
        (
            "    public long intervalMs() {",
            "    /** @return 默认心跳间隔 5000 毫秒。 */\n    public long intervalMs() {",
        ),
        (
            "    private boolean clientErrorCode(int code) {",
            "    /** 判断是否为 4xx 客户端错误状态码。 */\n    private boolean clientErrorCode(int code) {",
        ),
        (
            "    private boolean serverErrorCode(int code) {",
            "    /** 判断是否为 5xx 服务端错误状态码。 */\n    private boolean serverErrorCode(int code) {",
        ),
    ],
    "HttpClientsFactory.java": [
        (
            "/**\n * @author Leo Li\n */",
            "/**\n * Apache HttpClient 工厂：按协议创建普通 HTTP 或带 SSL 的 HTTPS 客户端。\n *\n * @author Leo Li\n */",
        ),
        (
            "    private static class SslConnectionSocketFactoryInstance {",
            "    /** 懒加载 SSL 套接字工厂的静态内部类。 */\n    private static class SslConnectionSocketFactoryInstance {",
        ),
        (
            "    public static CloseableHttpClient getHttpClientsByProtocol(Protocol protocol) {",
            "    /** 按协议返回 HttpClient：HTTP 用默认实现，HTTPS 挂载 {@link SslFactory} 证书。 */\n    public static CloseableHttpClient getHttpClientsByProtocol(Protocol protocol) {",
        ),
    ],
}
