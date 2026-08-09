"""Chinese JavaDoc replacements for Sentinel 1.8.10 wave32b transport batch [15:30]."""

TRANSPORT_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "HttpHeartbeatSender.java": [
        (
            "/**\n * @author Eric Zhao\n * @author Carpenter Lee\n * @author Leo Li\n */",
            "/**\n * 基于 Apache HttpClient 的心跳发送器：向 Dashboard 发起 GET 注册本机信息。\n * SPI 优先级 {@code ORDER_LOWEST - 100}，与 Netty 实现互斥加载。\n *\n * @author Eric Zhao\n * @author Carpenter Lee\n * @author Leo Li\n */",
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
            "            RecordLog.info(\"[NettyHttpHeartbeatSender] No dashboard server available\");",
            "            RecordLog.info(\"[NettyHttpHeartbeatSender] 未配置可用的 Dashboard 地址\");",
        ),
        (
            "            RecordLog.info(\"[NettyHttpHeartbeatSender] Dashboard address parsed: <{}:{}>\", consoleHost, consolePort);",
            "            RecordLog.info(\"[NettyHttpHeartbeatSender] 已解析 Dashboard 地址: <{}:{}>\", consoleHost, consolePort);",
        ),
        (
            "        // Send heartbeat request.",
            "        // 发送心跳 GET 请求",
        ),
        (
            "            RecordLog.warn(\"[HttpHeartbeatSender] Failed to send heartbeat to \"",
            "            RecordLog.warn(\"[HttpHeartbeatSender] 心跳发送失败，目标 \"",
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
            "package com.alibaba.csp.sentinel.transport.heartbeat.client;",
            "/*\n * Copyright 1999-2018 Alibaba Group Holding Ltd.\n *\n * Licensed under the Apache License, Version 2.0 (the \"License\");\n * you may not use this file except in compliance with the License.\n * You may obtain a copy of the License at\n *\n *      http://www.apache.org/licenses/LICENSE-2.0\n *\n * Unless required by applicable law or agreed to in writing, software\n * distributed under the License is distributed on an \"AS IS\" BASIS,\n * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n * See the License for the specific language governing permissions and\n * limitations under the License.\n */\npackage com.alibaba.csp.sentinel.transport.heartbeat.client;",
        ),
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
    "SimpleHttpCommandCenter.java": [
        (
            "/***\n * The simple command center provides service to exchange information.\n *\n * @author youji.zj\n */",
            "/***\n * 简易 HTTP 命令中心：基于 {@link ServerSocket} 接受 Dashboard 下发的控制命令。\n * 在独立线程中绑定端口，业务请求由线程池异步处理 {@link HttpEventTask}。\n *\n * @author youji.zj\n */",
        ),
        (
            "    private static final int PORT_UNINITIALIZED = -1;",
            "    /** 端口未初始化时的占位值。 */\n    private static final int PORT_UNINITIALIZED = -1;",
        ),
        (
            "    private static final int DEFAULT_SERVER_SO_TIMEOUT = 3000;",
            "    /** 接受连接后 Socket 读超时（毫秒）。 */\n    private static final int DEFAULT_SERVER_SO_TIMEOUT = 3000;",
        ),
        (
            "    private static final int DEFAULT_PORT = 8719;",
            "    /** 未配置端口时的默认监听端口。 */\n    private static final int DEFAULT_PORT = 8719;",
        ),
        (
            "    private static final Map<String, CommandHandler> handlerMap = new ConcurrentHashMap<String, CommandHandler>();",
            "    /** 命令名到 {@link CommandHandler} 的全局注册表。 */\n    private static final Map<String, CommandHandler> handlerMap = new ConcurrentHashMap<String, CommandHandler>();",
        ),
        (
            "    private ExecutorService executor = Executors.newSingleThreadExecutor(",
            "    /** 负责端口绑定与 accept 循环的单线程池。 */\n    private ExecutorService executor = Executors.newSingleThreadExecutor(",
        ),
        (
            "    private ExecutorService bizExecutor;",
            "    /** 处理 {@link HttpEventTask} 的业务线程池。 */\n    private ExecutorService bizExecutor;",
        ),
        (
            "    private ServerSocket socketReference;",
            "    /** 当前监听的 ServerSocket，stop 时关闭。 */\n    private ServerSocket socketReference;",
        ),
        (
            "        // Register handlers",
            "        // 注册 SPI 加载的全部命令处理器",
        ),
        (
            "                    CommandCenterLog.info(\"EventTask rejected\");",
            "                    CommandCenterLog.info(\"命令任务被拒绝，线程池已满\");",
        ),
        (
            "                    CommandCenterLog.info(\"[CommandCenter] Begin listening at port \" + serverSocket.getLocalPort());",
            "                    CommandCenterLog.info(\"[CommandCenter] 开始在端口 \" + serverSocket.getLocalPort() + \" 监听\");",
        ),
        (
            "                    CommandCenterLog.info(\"[CommandCenter] chooses port fail, http command center will not work\");",
            "                    CommandCenterLog.info(\"[CommandCenter] 端口绑定失败，HTTP 命令中心不可用\");",
        ),
        (
            "     * Get a server socket from an available port from a base port.<br>\n     * Increasing on port number will occur when the port has already been used.\n     *\n     * @param basePort base port to start\n     * @return new socket with available port",
            "     * 从 basePort 起递增尝试绑定可用端口（每 3 次失败 port+1）。<br>\n     * 端口被占用时自动递增重试。\n     *\n     * @param basePort 起始端口\n     * @return 绑定成功的 ServerSocket，全部失败时返回 null",
        ),
        (
            "     * Get the name set of all registered commands.",
            "     * 获取已注册命令名称集合。",
        ),
        (
            "    class ServerThread extends Thread {",
            "    /** accept 循环线程：接收连接并提交 {@link HttpEventTask}。 */\n    class ServerThread extends Thread {",
        ),
        (
            "                        // In case of infinite log.",
            "                        // 避免异常时日志刷屏",
        ),
        (
            "                        // Indicates the task should stop.",
            "                        // 中断 accept 循环",
        ),
        (
            "            CommandCenterLog.warn(\"Register failed (duplicate command): \" + commandName);",
            "            CommandCenterLog.warn(\"注册失败（命令重复）: \" + commandName);",
        ),
        (
            "     * Avoid server thread hang, 3 seconds timeout by default.",
            "     * 设置 Socket 读超时，避免 accept 后线程永久阻塞（默认 3 秒）。",
        ),
    ],
    "RequestException.java": [
        (
            "/**\n * Represent exception with status code processing a request\n * \n * @author jason\n *\n */",
            "/**\n * HTTP 命令请求处理异常，携带 {@link StatusCode} 供 {@link HttpEventTask} 写回响应。\n *\n * @author jason\n */",
        ),
        (
            "    private StatusCode statusCode = StatusCode.BAD_REQUEST;",
            "    /** 默认 400 Bad Request。 */\n    private StatusCode statusCode = StatusCode.BAD_REQUEST;",
        ),
        (
            "    public RequestException(StatusCode statusCode, String msg) {",
            "    /** @param statusCode HTTP 状态码\n     * @param msg 错误消息 */\n    public RequestException(StatusCode statusCode, String msg) {",
        ),
        (
            "    public StatusCode getStatusCode() {",
            "    /** @return 关联的 HTTP 状态码枚举。 */\n    public StatusCode getStatusCode() {",
        ),
    ],
    "HttpEventTask.java": [
        (
            "/**\n * The task handles incoming command request in HTTP protocol.\n *\n * @author youji.zj\n * @author Eric Zhao\n * @author Jason Joo\n */",
            "/**\n * HTTP 命令事件任务：解析 Socket 上的简易 HTTP 请求并分派给 {@link CommandHandler}。\n * 支持 GET 查询串与 POST form-urlencoded 请求体。\n *\n * @author youji.zj\n * @author Eric Zhao\n * @author Jason Joo\n */",
        ),
        (
            "    public static final String SERVER_ERROR_MESSAGE = \"Command server error\";",
            "    /** 服务端内部错误时的默认响应正文。 */\n    public static final String SERVER_ERROR_MESSAGE = \"Command server error\";",
        ),
        (
            "    public static final String INVALID_COMMAND_MESSAGE = \"Invalid command\";",
            "    /** 缺少目标命令名时的响应正文。 */\n    public static final String INVALID_COMMAND_MESSAGE = \"Invalid command\";",
        ),
        (
            "    private final Socket socket;",
            "    /** 客户端连接 Socket。 */\n    private final Socket socket;",
        ),
        (
            "    private boolean writtenHead = false;",
            "    /** 是否已写入 HTTP 响应头（异常路径需区分）。 */\n    private boolean writtenHead = false;",
        ),
        (
            "                // Deal with post method",
            "                // POST 请求需额外解析请求头与 body",
        ),
        (
            "            // Validate the target command.",
            "            // 校验目标命令名",
        ),
        (
            "            // Find the matching command handler.",
            "            // 查找匹配的 CommandHandler",
        ),
        (
            "                // No matching command handler.",
            "                // 未找到对应命令处理器",
        ),
        (
            "     * Try to process the body of POST request additionally.\n     *\n     * @param in\n     * @param request\n     * @throws RequestException\n     * @throws IOException",
            "     * 解析 POST 请求头与 form-urlencoded 请求体，参数写入 {@link CommandRequest}。\n     *\n     * @param in 输入流\n     * @param request 命令请求\n     * @throws RequestException 格式或 Content-Type 不合法\n     * @throws IOException IO 错误",
        ),
        (
            "            // illegal request",
            "            // 非法请求（无法解析头）",
        ),
        (
            "            // not supported Content-type",
            "            // 不支持的 Content-Type",
        ),
        (
            "            // illegal request without Content-length header",
            "            // 缺少或非法 Content-Length",
        ),
        (
            "     * Process header line in request\n     *\n     * @param in\n     * @return return headers in a Map, null for illegal request\n     * @throws IOException",
            "     * 逐行读取 POST 请求头直至空行。\n     *\n     * @param in 输入流\n     * @return 头字段 Map（键小写），非法请求返回 null\n     * @throws IOException IO 错误",
        ),
        (
            "                // empty line",
            "                // 空行表示头部结束",
        ),
        (
            "                // empty value, abandon",
            "                // 无冒号或空值行跳过",
        ),
        (
            "            // Not supported request type",
            "            // 仅支持 application/x-www-form-urlencoded",
        ),
        (
            "            // Now simple-http only support form-encoded post request.",
            "            // simple-http 仅支持表单编码 POST",
        ),
        (
            "        // Only allow partial",
            "        // 允许部分读取 body",
        ),
        (
            "     * Consume all the body submitted and parse params into {@link CommandRequest}\n     *\n     * @param queryString\n     * @param request",
            "     * 解析 a=1&b=2 查询串，将键值对写入 {@link CommandRequest}。\n     *\n     * @param queryString 查询串\n     * @param request 命令请求",
        ),
        (
            "        // check anchor",
            "        // 去除 # 锚点",
        ),
        (
            "                // empty",
            "                // 跳过空参数段",
        ),
        (
            "                // reach the end",
            "                // 已解析完所有参数",
        ),
        (
            "            // Here we directly use `toString` to encode the result to plain text.",
            "            // 成功结果直接 toString 作为纯文本响应",
        ),
        (
            "     * Parse raw HTTP request line to a {@link CommandRequest}.\n     *\n     * @param line HTTP request line\n     * @return parsed command request",
            "     * 解析 HTTP 请求行（如 GET /cmd?a=1 HTTP/1.0）为 {@link CommandRequest}。\n     *\n     * @param line HTTP 请求行\n     * @return 解析后的命令请求",
        ),
        (
            "     * Truncate query from \"a=1&b=2#mark\" to \"a=1&b=2\"\n     *\n     * @param str\n     * @return",
            "     * 去掉 URL 中的 # 锚点片段。\n     *\n     * @param str 原始串\n     * @return 去掉锚点后的串",
        ),
        (
            "            // empty key/val or nothing found",
            "            // 键或值为空则跳过",
        ),
    ],
    "StatusCode.java": [
        (
            "/**\n * @author Jason Joo\n */",
            "/**\n * 简易 HTTP 命令中心使用的状态码枚举。\n *\n * @author Jason Joo\n */",
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
    "HeartbeatMessage.java": [
        (
            "/**\n * Heart beat message entity.\n * The message consists of key-value pair parameters.\n *\n * @author leyou\n */",
            "/**\n * 心跳消息实体：维护向 Dashboard 上报的键值对参数。\n * 构造时填充 hostname、ip、app 等静态字段；发送前调用 {@link #generateCurrentMessage()} 刷新版本与时间戳。\n *\n * @author leyou\n */",
        ),
        (
            "    private final Map<String, String> message = new HashMap<String, String>();",
            "    /** 心跳参数字典。 */\n    private final Map<String, String> message = new HashMap<String, String>();",
        ),
        (
            "        // Put application type (since 1.6.0).",
            "        // 应用类型（1.6.0 起）",
        ),
        (
            "    public HeartbeatMessage registerInformation(String key, String value) {",
            "    /** 链式注册额外心跳字段。 */\n    public HeartbeatMessage registerInformation(String key, String value) {",
        ),
        (
            "        // Version of Sentinel.",
            "        // Sentinel 版本号",
        ),
        (
            "        // Actually timestamp.",
            "        // 当前时间戳，用于 Dashboard 判活",
        ),
        (
            "    public Map<String, String> generateCurrentMessage() {",
            "    /** 刷新动态字段并返回完整心跳参数 Map。 */\n    public Map<String, String> generateCurrentMessage() {",
        ),
    ],
    "SimpleHttpHeartbeatSender.java": [
        (
            "/**\n * The heartbeat sender provides basic API for sending heartbeat request to provided target.\n * This implementation is based on a trivial HTTP client.\n *\n * @author Eric Zhao\n * @author Carpenter Lee\n * @author Leo Li\n */",
            "/**\n * 基于 {@link SimpleHttpClient} 的心跳发送器：向 Dashboard 列表轮询 POST 注册信息。\n * 命令端口未初始化时不发送心跳。\n *\n * @author Eric Zhao\n * @author Carpenter Lee\n * @author Leo Li\n */",
        ),
        (
            "    private static final int OK_STATUS = 200;",
            "    /** HTTP 200 视为成功。 */\n    private static final int OK_STATUS = 200;",
        ),
        (
            "    private static final long DEFAULT_INTERVAL = 1000 * 10;",
            "    /** 默认心跳间隔 10 秒。 */\n    private static final long DEFAULT_INTERVAL = 1000 * 10;",
        ),
        (
            "    private final HeartbeatMessage heartBeat = new HeartbeatMessage();",
            "    /** 心跳消息构建器。 */\n    private final HeartbeatMessage heartBeat = new HeartbeatMessage();",
        ),
        (
            "    private final SimpleHttpClient httpClient = new SimpleHttpClient();",
            "    /** 简易 HTTP 客户端。 */\n    private final SimpleHttpClient httpClient = new SimpleHttpClient();",
        ),
        (
            "    private final List<Endpoint> addressList;",
            "    /** Dashboard 地址列表。 */\n    private final List<Endpoint> addressList;",
        ),
        (
            "    private int currentAddressIdx = 0;",
            "    /** 当前轮询的 Dashboard 索引。 */\n    private int currentAddressIdx = 0;",
        ),
        (
            "        // Retrieve the list of default addresses.",
            "        // 从 TransportConfig 读取 Dashboard 地址列表",
        ),
        (
            "            RecordLog.warn(\"[SimpleHttpHeartbeatSender] Dashboard server address not configured or not available\");",
            "            RecordLog.warn(\"[SimpleHttpHeartbeatSender] Dashboard 地址未配置或不可用\");",
        ),
        (
            "            RecordLog.info(\"[SimpleHttpHeartbeatSender] Command server port not initialized, won't send heartbeat\");",
            "            RecordLog.info(\"[SimpleHttpHeartbeatSender] 命令端口未初始化，跳过心跳\");",
        ),
        (
            "                RecordLog.warn(\"[SimpleHttpHeartbeatSender] Failed to send heartbeat to \" + addrInfo",
            "                RecordLog.warn(\"[SimpleHttpHeartbeatSender] 心跳发送失败，目标 \" + addrInfo",
        ),
        (
            "    private Endpoint getAvailableAddress() {",
            "    /** 按 currentAddressIdx 轮询返回可用 Dashboard 端点。 */\n    private Endpoint getAvailableAddress() {",
        ),
    ],
    "SimpleHttpClient.java": [
        (
            " * A very simple HTTP client that only supports GET/POST method and plain text request body.\n * The Content-Type header is always set as <pre>application/x-www-form-urlencoded</pre>.\n * All parameters in the request will be encoded using {@link URLEncoder#encode(String, String)}.\n * </p>\n * <p>\n * The result of a HTTP invocation will be wrapped as a {@link SimpleHttpResponse}. Content in response body\n * will be automatically decoded to string with provided charset.\n * </p>\n * <p>\n * This is a blocking and synchronous client, so an invocation will await the response until timeout exceed.\n * </p>\n * <p>\n * Note that this is a very NAIVE client, {@code Content-Length} must be specified in the\n * HTTP response header, otherwise, the response body will be dropped. All other body type such as\n * {@code Transfer-Encoding: chunked}, {@code Transfer-Encoding: deflate} are not supported.\n * </p>",
            " * 极简阻塞式 HTTP 客户端，仅支持 GET/POST 与 form-urlencoded 正文。\n * Content-Type 固定为 <pre>application/x-www-form-urlencoded</pre>，参数经 {@link URLEncoder} 编码。\n * </p>\n * <p>\n * 响应封装为 {@link SimpleHttpResponse}，正文按 charset 解码为字符串。\n * </p>\n * <p>\n * 同步阻塞：调用线程等待响应或超时。\n * </p>\n * <p>\n * 实现较朴素：响应必须带 {@code Content-Length}，否则丢弃 body；不支持 chunked/deflate 等编码。\n * </p>",
        ),
        (
            "     * Execute a GET HTTP request.\n     *\n     * @param request HTTP request\n     * @return the response if the request is successful\n     * @throws IOException when connection cannot be established or the connection is interrupted",
            "     * 执行 GET 请求，查询参数拼接到 URL。\n     *\n     * @param request HTTP 请求\n     * @return 响应实体，request 为 null 时返回 null\n     * @throws IOException 连接失败或中断",
        ),
        (
            "     * Execute a POST HTTP request.\n     *\n     * @param request HTTP request\n     * @return the response if the request is successful\n     * @throws IOException when connection cannot be established or the connection is interrupted",
            "     * 执行 POST 请求，参数放在请求体。\n     *\n     * @param request HTTP 请求\n     * @return 响应实体\n     * @throws IOException 连接失败或中断",
        ),
        (
            "                // POST method.",
            "                // POST：先写 Content-Length 再写 body",
        ),
        (
            "     * Encode and get the URL request parameters.\n     *\n     * @param paramsMap pair of parameters\n     * @param charset   charset\n     * @return encoded request parameters, or empty string (\"\") if no parameters are provided",
            "     * 将参数 Map 编码为 application/x-www-form-urlencoded 查询串。\n     *\n     * @param paramsMap 键值对\n     * @param charset 字符集\n     * @return 编码后的参数字符串，无参数时返回空串",
        ),
        (
            "                // Remove the last '&'.",
            "                // 去掉末尾 &",
        ),
        (
            "    private enum RequestMethod {",
            "    /** HTTP 方法枚举。 */\n    private enum RequestMethod {",
        ),
    ],
    "SimpleHttpRequest.java": [
        (
            "/**\n * Simple HTTP request representation.\n *\n * @author leyou\n * @author Leo Li\n */",
            "/**\n * 简易 HTTP 请求描述：目标端点、路径、超时、字符集与参数 Map。\n * 支持链式 setter 与 {@link #addParam}。\n *\n * @author leyou\n * @author Leo Li\n */",
        ),
        (
            "    private Endpoint endpoint;",
            "    /** 目标 Dashboard 端点。 */\n    private Endpoint endpoint;",
        ),
        (
            "    private String requestPath = \"\";",
            "    /** API 路径（如心跳注册路径）。 */\n    private String requestPath = \"\";",
        ),
        (
            "    private int soTimeout = 3000;",
            "    /** Socket 读写超时（毫秒）。 */\n    private int soTimeout = 3000;",
        ),
        (
            "    private Map<String, String> params;",
            "    /** 请求参数（GET 拼 URL，POST 写 body）。 */\n    private Map<String, String> params;",
        ),
        (
            "    private Charset charset = Charset.forName(SentinelConfig.charset());",
            "    /** 请求编码字符集，默认 Sentinel 全局 charset。 */\n    private Charset charset = Charset.forName(SentinelConfig.charset());",
        ),
        (
            "    public SimpleHttpRequest(Endpoint endpoint, String requestPath) {",
            "    /** @param endpoint 目标端点\n     * @param requestPath API 路径 */\n    public SimpleHttpRequest(Endpoint endpoint, String requestPath) {",
        ),
        (
            "            throw new IllegalArgumentException(\"Parameter key cannot be empty\");",
            "            throw new IllegalArgumentException(\"参数键不能为空\");",
        ),
        (
            "    public SimpleHttpRequest addParam(String key, String value) {",
            "    /** 链式添加单个请求参数。 */\n    public SimpleHttpRequest addParam(String key, String value) {",
        ),
    ],
    "SimpleHttpResponse.java": [
        (
            "/**\n * Simple HTTP response representation.\n *\n * @author leyou\n */",
            "/**\n * 简易 HTTP 响应：状态行、响应头与 body 字节。\n * 通过 {@link #getBodyAsString()} 按 Content-Type charset 解码正文。\n *\n * @author leyou\n */",
        ),
        (
            "    private Charset charset = Charset.forName(SentinelConfig.charset());",
            "    /** 正文解码字符集，可从 Content-Type 解析。 */\n    private Charset charset = Charset.forName(SentinelConfig.charset());",
        ),
        (
            "    private String statusLine;",
            "    /** HTTP 状态行（如 HTTP/1.1 200 OK）。 */\n    private String statusLine;",
        ),
        (
            "    private int statusCode;",
            "    /** 数字状态码，懒解析自 statusLine。 */\n    private int statusCode;",
        ),
        (
            "    private Map<String, String> headers;",
            "    /** 响应头 Map。 */\n    private Map<String, String> headers;",
        ),
        (
            "    private byte[] body;",
            "    /** 响应体原始字节。 */\n    private byte[] body;",
        ),
        (
            "     * Get header of the key ignoring case.\n     *\n     * @param key header key\n     * @return header value",
            "     * 按 key 获取响应头，忽略大小写。\n     *\n     * @param key 头名称\n     * @return 头值，不存在时 null",
        ),
        (
            "    public String getBodyAsString() {",
            "    /** 按 charset 将 body 解码为字符串。 */\n    public String getBodyAsString() {",
        ),
    ],
    "SimpleHttpResponseParser.java": [
        (
            " * The parser provides functionality to parse raw bytes HTTP response to a {@link SimpleHttpResponse}.\n * </p>\n * <p>\n * Note that this is a very NAIVE parser, {@code Content-Length} must be specified in the\n * HTTP response header, otherwise, the body will be dropped. All other body type such as\n * {@code Transfer-Encoding: chunked}, {@code Transfer-Encoding: deflate} are not supported.\n * </p>",
            " * 将 Socket 输入流中的原始 HTTP 响应解析为 {@link SimpleHttpResponse}。\n * </p>\n * <p>\n * 朴素实现：必须存在 {@code Content-Length}，否则丢弃 body；不支持 chunked/deflate。\n * </p>",
        ),
        (
            "    private static final int MAX_BODY_SIZE = 1024 * 1024 * 4;",
            "    /** 允许的最大响应体 4MB。 */\n    private static final int MAX_BODY_SIZE = 1024 * 1024 * 4;",
        ),
        (
            "    private byte[] buf;",
            "    /** 读缓冲，按 maxSize 分配。 */\n    private byte[] buf;",
        ),
        (
            "     * Parse bytes from an input stream to a {@link SimpleHttpResponse}.\n     *\n     * @param in input stream\n     * @return parsed HTTP response entity\n     * @throws IOException when an IO error occurs",
            "     * 从输入流增量读取并解析 HTTP 响应。\n     *\n     * @param in 输入流\n     * @return 解析完成的响应，流提前结束时可能为 null\n     * @throws IOException IO 错误",
        ),
        (
            "                            //When the `Content-Length` is absent, parse the rest of the bytes as body directly.",
            "                            // 无 Content-Length 时曾考虑读剩余字节（已注释）",
        ),
        (
            "                            // Parse HTTP body.",
            "                            // 解析 HTTP 正文",
        ),
        (
            "                            // When the `Content-Length` is absent, drop the body, return directly.",
            "                            // 无 Content-Length 则丢弃 body 直接返回",
        ),
        (
            "                            // `Content-Length` is not equal to exact length.",
            "                            // Content-Length 与已读长度不一致",
        ),
        (
            "                            // Parse HTTP header.",
            "                            // 解析单行响应头",
        ),
        (
            "                // Move remaining bytes to the beginning.",
            "                // 未消费字节前移，继续读下一行",
        ),
        (
            "     * Get the index of CRLF separator.\n     *\n     * @param bg begin offset\n     * @param ed end offset\n     * @return the index, or {@code -1} if no CRLF is found",
            "     * 在 buf[bg..ed) 中查找 \\r\\n 分隔符位置。\n     *\n     * @param bg 起始偏移\n     * @param ed 结束偏移\n     * @return CRLF 起始索引，未找到返回 -1",
        ),
    ],
    "SocketFactory.java": [
        (
            "package com.alibaba.csp.sentinel.transport.heartbeat.client;",
            "/*\n * Copyright 1999-2018 Alibaba Group Holding Ltd.\n *\n * Licensed under the Apache License, Version 2.0 (the \"License\");\n * you may not use this file except in compliance with the License.\n * You may obtain a copy of the License at\n *\n *      http://www.apache.org/licenses/LICENSE-2.0\n *\n * Unless required by applicable law or agreed to in writing, software\n * distributed under the License is distributed on an \"AS IS\" BASIS,\n * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n * See the License for the specific language governing permissions and\n * limitations under the License.\n */\npackage com.alibaba.csp.sentinel.transport.heartbeat.client;",
        ),
        (
            "/**\n * @author Leo Li\n */",
            "/**\n * Socket 工厂：按协议创建普通 TCP 或 SSL {@link Socket}。\n *\n * @author Leo Li\n */",
        ),
        (
            "    private static class SSLSocketFactoryInstance {",
            "    /** 懒加载 SSLSocketFactory 的静态内部类。 */\n    private static class SSLSocketFactoryInstance {",
        ),
        (
            "    public static Socket getSocket(Protocol protocol) throws IOException {",
            "    /** HTTP 返回普通 Socket，HTTPS 返回 {@link SslFactory} 创建的 SSL Socket。 */\n    public static Socket getSocket(Protocol protocol) throws IOException {",
        ),
    ],
    "SentinelApiHandler.java": [
        (
            "/**\n * @author shenbaoyong\n */",
            "/**\n * Spring MVC 环境下的 Sentinel 命令 API 处理器：将 HTTP 参数转为 {@link CommandRequest} 并调用 {@link CommandHandler}。\n *\n * @author shenbaoyong\n */",
        ),
        (
            "    public static final String SERVER_ERROR_MESSAGE = \"Command server error\";",
            "    /** 内部错误时的默认响应消息。 */\n    public static final String SERVER_ERROR_MESSAGE = \"Command server error\";",
        ),
        (
            "    private CommandHandler commandHandler;",
            "    /** 绑定的命令处理器。 */\n    private CommandHandler commandHandler;",
        ),
        (
            "    public SentinelApiHandler(CommandHandler commandHandler) {",
            "    /** @param commandHandler 处理具体命令的 Handler */\n    public SentinelApiHandler(CommandHandler commandHandler) {",
        ),
        (
            "            // Here we directly use `toString` to encode the result to plain text.",
            "            // 成功结果 toString 后按 Sentinel charset 编码输出",
        ),
    ],
    "SentinelApiHandlerAdapter.java": [
        (
            "/**\n * @author shenbaoyong\n */",
            "/**\n * {@link HandlerAdapter} 适配器：识别 {@link SentinelApiHandler} 并委托其处理 Spring MVC 请求。\n * 默认 {@link Ordered#LOWEST_PRECEDENCE}，可通过 setOrder 调整。\n *\n * @author shenbaoyong\n */",
        ),
        (
            "    private int order = Ordered.LOWEST_PRECEDENCE;",
            "    /** Handler 排序，数值越小优先级越高。 */\n    private int order = Ordered.LOWEST_PRECEDENCE;",
        ),
        (
            "    public void setOrder(int order) {",
            "    /** 设置适配器在 HandlerAdapter 链中的顺序。 */\n    public void setOrder(int order) {",
        ),
        (
            "    public boolean supports(Object handler) {",
            "    /** 仅支持 {@link SentinelApiHandler} 类型 handler。 */\n    public boolean supports(Object handler) {",
        ),
        (
            "    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {",
            "    /** 委托 {@link SentinelApiHandler#handle} 处理请求，不返回视图。 */\n    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {",
        ),
        (
            "    public long getLastModified(HttpServletRequest request, Object handler) {",
            "    /** 命令 API 不做缓存，固定返回 -1。 */\n    public long getLastModified(HttpServletRequest request, Object handler) {",
        ),
    ],
}
