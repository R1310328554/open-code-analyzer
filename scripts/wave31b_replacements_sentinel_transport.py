"""Chinese JavaDoc replacements for Sentinel 1.8.10 wave31b transport batch [15:30]."""

TRANSPORT_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "TransportConfig.java": [
        (
            "/**\n * @author Carpenter Lee\n * @author Jason Joo\n * @author Leo Li\n */",
            "/**\n * Sentinel 传输层配置：解析 Dashboard 地址、心跳间隔、本地 API 端口等。\n * 配置项通过 {@link SentinelConfig} 读取，支持逗号分隔的多 Dashboard 地址。\n *\n * @author Carpenter Lee\n * @author Jason Joo\n * @author Leo Li\n */",
        ),
        (
            "    public static final String CONSOLE_SERVER = \"csp.sentinel.dashboard.server\";",
            "    /** Dashboard 控制台地址配置键（支持逗号分隔多个 endpoint）。 */\n    public static final String CONSOLE_SERVER = \"csp.sentinel.dashboard.server\";",
        ),
        (
            "    public static final String SERVER_PORT = \"csp.sentinel.api.port\";",
            "    /** 本机命令/API 服务端口配置键。 */\n    public static final String SERVER_PORT = \"csp.sentinel.api.port\";",
        ),
        (
            "    public static final String HEARTBEAT_INTERVAL_MS = \"csp.sentinel.heartbeat.interval.ms\";",
            "    /** 心跳上报间隔（毫秒）配置键。 */\n    public static final String HEARTBEAT_INTERVAL_MS = \"csp.sentinel.heartbeat.interval.ms\";",
        ),
        (
            "    public static final String HEARTBEAT_CLIENT_IP = \"csp.sentinel.heartbeat.client.ip\";",
            "    /** 心跳上报使用的客户端 IP 配置键。 */\n    public static final String HEARTBEAT_CLIENT_IP = \"csp.sentinel.heartbeat.client.ip\";",
        ),
        (
            "    public static final String HEARTBEAT_API_PATH = \"csp.sentinel.heartbeat.api.path\";",
            "    /** 机器注册心跳 API 路径配置键。 */\n    public static final String HEARTBEAT_API_PATH = \"csp.sentinel.heartbeat.api.path\";",
        ),
        (
            "    public static final String HEARTBEAT_DEFAULT_PATH = \"/registry/machine\";",
            "    /** 默认机器注册心跳路径。 */\n    public static final String HEARTBEAT_DEFAULT_PATH = \"/registry/machine\";",
        ),
        (
            "    private static int runtimePort = -1;",
            "    /** 运行时实际绑定的 API 端口（由传输层启动后回填）。 */\n    private static int runtimePort = -1;",
        ),
        (
            "     * Get heartbeat interval in milliseconds.\n     *\n     * @return heartbeat interval in milliseconds if exists, or null if not configured or invalid config",
            "     * 获取心跳间隔（毫秒）。\n     *\n     * @return 已配置且解析成功时返回间隔毫秒数，未配置或非法时返回 null",
        ),
        (
            "     * Get a list of Endpoint(protocol, ip/domain, port) indicating Sentinel Dashboard's address.<br>\n     * NOTE: only support <b>HTTP</b> and <b>HTTPS</b> protocol\n     *\n     * @return list of Endpoint(protocol, ip/domain, port). <br>\n     *         <b>May not be null</b>. <br>\n     *         An empty list returned when not configured.",
            "     * 解析 Dashboard 控制台地址列表，每项为 {@link Endpoint}（协议、主机、端口）。<br>\n     * 仅支持 <b>HTTP</b> 与 <b>HTTPS</b> 协议前缀。\n     *\n     * @return Endpoint 列表，<b>永不为 null</b>；未配置时返回空列表",
        ),
        (
            "                // for single segment, pos move to the end",
            "                // 单段地址时将 pos 移到末尾",
        ),
        (
            "            // parsing",
            "            // 解析 host:port 或带协议前缀的地址",
        ),
        (
            "                // skip",
            "                // 格式非法则跳过",
        ),
        (
            "     * Get Server port of this HTTP server.\n     *\n     * @return the port, maybe null if not configured.",
            "     * 获取本机 HTTP 命令/API 服务端口。\n     *\n     * @return 端口号字符串；未配置且未设置 runtimePort 时可能为 null",
        ),
        (
            "     * Set real port this HTTP server uses.\n     *\n     * @param port real port.",
            "     * 设置传输层实际监听的端口（启动成功后调用）。\n     *\n     * @param port 实际端口",
        ),
        (
            "     * Get heartbeat client local ip.\n     * If the client ip not configured,it will be the address of local host\n     *\n     * @return the local ip.",
            "     * 获取心跳上报使用的本机 IP。\n     * 未配置时回退为 {@link HostNameUtil#getIp()}。\n     *\n     * @return 客户端 IP",
        ),
        (
            "     * Get the heartbeat api path. If the machine registry path of the dashboard\n     * is modified, then the API path should also be consistent with the API path of the dashboard.\n     *\n     * @return the heartbeat api path\n     * @since 1.7.1",
            "     * 获取心跳注册 API 路径；须与 Dashboard 侧机器注册路径一致。\n     * 未配置时使用 {@link #HEARTBEAT_DEFAULT_PATH}。\n     *\n     * @return 以 / 开头的 API 路径\n     * @since 1.7.1",
        ),
    ],
    "Endpoint.java": [
        (
            "package com.alibaba.csp.sentinel.transport.endpoint;",
            "/*\n * Copyright 1999-2018 Alibaba Group Holding Ltd.\n *\n * Licensed under the Apache License, Version 2.0 (the \"License\");\n * you may not use this file except in compliance with the License.\n * You may obtain a copy of the License at\n *\n *      http://www.apache.org/licenses/LICENSE-2.0\n *\n * Unless required by applicable law or agreed to in writing, software\n * distributed under the License is distributed on an \"AS IS\" BASIS,\n * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n * See the License for the specific language governing permissions and\n * limitations under the License.\n */\npackage com.alibaba.csp.sentinel.transport.endpoint;",
        ),
        (
            "/**\n * @author Leo Li\n */",
            "/**\n * 传输层端点：封装协议、主机名与端口，用于 Dashboard 地址解析。\n *\n * @author Leo Li\n */",
        ),
        (
            "    private Protocol protocol;",
            "    /** 通信协议（HTTP/HTTPS）。 */\n    private Protocol protocol;",
        ),
        (
            "    private String host;",
            "    /** 主机名或 IP。 */\n    private String host;",
        ),
        (
            "    private int port;",
            "    /** 端口号。 */\n    private int port;",
        ),
        (
            "    public Endpoint(Protocol protocol, String host, int port) {",
            "    /** @param protocol 协议\n     * @param host 主机\n     * @param port 端口 */\n    public Endpoint(Protocol protocol, String host, int port) {",
        ),
        (
            "    public Protocol getProtocol() {",
            "    /** @return 通信协议。 */\n    public Protocol getProtocol() {",
        ),
        (
            "    public String getHost() {",
            "    /** @return 主机名或 IP。 */\n    public String getHost() {",
        ),
        (
            "    public int getPort() {",
            "    /** @return 端口号。 */\n    public int getPort() {",
        ),
    ],
    "Protocol.java": [
        (
            "package com.alibaba.csp.sentinel.transport.endpoint;",
            "/*\n * Copyright 1999-2018 Alibaba Group Holding Ltd.\n *\n * Licensed under the Apache License, Version 2.0 (the \"License\");\n * you may not use this file except in compliance with the License.\n * You may obtain a copy of the License at\n *\n *      http://www.apache.org/licenses/LICENSE-2.0\n *\n * Unless required by applicable law or agreed to in writing, software\n * distributed under the License is distributed on an \"AS IS\" BASIS,\n * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n * See the License for the specific language governing permissions and\n * limitations under the License.\n */\npackage com.alibaba.csp.sentinel.transport.endpoint;",
        ),
        (
            "/**\n * @author Leo Li\n * @author Yanming Zhou\n */",
            "/**\n * Dashboard 通信协议枚举，当前支持 HTTP 与 HTTPS。\n *\n * @author Leo Li\n * @author Yanming Zhou\n */",
        ),
        (
            "    HTTP,\n    HTTPS;",
            "    /** 明文 HTTP。 */\n    HTTP,\n    /** TLS HTTPS。 */\n    HTTPS;",
        ),
        (
            "    public String getProtocol() {",
            "    /** @return 小写协议名（http/https）。 */\n    public String getProtocol() {",
        ),
    ],
    "CommandCenterInitFunc.java": [
        (
            "/**\n * @author Eric Zhao\n */",
            "/**\n * 命令中心初始化函数：在 Sentinel 启动时解析并启动 {@link CommandCenter} 实现。\n * 通过 {@link CommandCenterProvider} SPI 加载具体传输实现（如 Netty HTTP）。\n *\n * @author Eric Zhao\n */",
        ),
        (
            "        if (commandCenter == null) {",
            "        // 未找到 CommandCenter SPI 实现\n        if (commandCenter == null) {",
        ),
        (
            "        commandCenter.beforeStart();",
            "        // 注册命令处理器后再启动监听\n        commandCenter.beforeStart();",
        ),
    ],
    "HeartbeatSenderInitFunc.java": [
        (
            "/**\n * Global init function for heartbeat sender.\n *\n * @author Eric Zhao\n */",
            "/**\n * 心跳发送器全局初始化：加载 {@link HeartbeatSender} SPI 并按固定间隔向 Dashboard 注册本机。\n * 间隔优先读取 {@link TransportConfig#HEARTBEAT_INTERVAL_MS}，否则使用发送器默认值。\n *\n * @author Eric Zhao\n */",
        ),
        (
            "    private ScheduledExecutorService pool = null;",
            "    /** 定时发送心跳的调度线程池。 */\n    private ScheduledExecutorService pool = null;",
        ),
        (
            "        if (sender == null) {",
            "        // 未加载 HeartbeatSender 实现则跳过\n        if (sender == null) {",
        ),
        (
            "    long retrieveInterval(/*@NonNull*/ HeartbeatSender sender) {",
            "    /** 解析有效心跳间隔：配置优先，否则使用 sender 默认值。 */\n    long retrieveInterval(/*@NonNull*/ HeartbeatSender sender) {",
        ),
        (
            "    private void scheduleHeartbeatTask(/*@NonNull*/ final HeartbeatSender sender, /*@Valid*/ long interval) {",
            "    /** 延迟 5 秒后首次发送，之后按 interval 固定频率调度。 */\n    private void scheduleHeartbeatTask(/*@NonNull*/ final HeartbeatSender sender, /*@Valid*/ long interval) {",
        ),
    ],
    "CommandCenterLog.java": [
        (
            "/**\n * Logger for command center.\n *\n * @author Eric Zhao\n */",
            "/**\n * 命令中心专用日志门面：优先加载 SPI 自定义 Logger，否则使用 JUL 适配器。\n * 默认日志文件名为 {@link #DEFAULT_LOG_FILENAME}。\n *\n * @author Eric Zhao\n */",
        ),
        (
            "    public static final String LOGGER_NAME = \"sentinelCommandCenterLogger\";",
            "    /** JUL/SPI Logger 名称。 */\n    public static final String LOGGER_NAME = \"sentinelCommandCenterLogger\";",
        ),
        (
            "    public static final String DEFAULT_LOG_FILENAME = \"command-center.log\";",
            "    /** 默认日志文件名。 */\n    public static final String DEFAULT_LOG_FILENAME = \"command-center.log\";",
        ),
        (
            "            // Load user-defined logger implementation first.",
            "            // 优先加载用户自定义 Logger SPI",
        ),
        (
            "                // If no customized loggers are provided, we use the default logger based on JUL.",
            "                // 无 SPI 实现时使用基于 JUL 的默认适配器",
        ),
    ],
    "SslFactory.java": [
        (
            "package com.alibaba.csp.sentinel.transport.ssl;",
            "/*\n * Copyright 1999-2018 Alibaba Group Holding Ltd.\n *\n * Licensed under the Apache License, Version 2.0 (the \"License\");\n * you may not use this file except in compliance with the License.\n * You may obtain a copy of the License at\n *\n *      http://www.apache.org/licenses/LICENSE-2.0\n *\n * Unless required by applicable law or agreed to in writing, software\n * distributed under the License is distributed on an \"AS IS\" BASIS,\n * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n * See the License for the specific language governing permissions and\n * limitations under the License.\n */\npackage com.alibaba.csp.sentinel.transport.ssl;",
        ),
        (
            "/**\n * @author Leo Li\n */",
            "/**\n * HTTPS 传输用 SSL 上下文工厂：提供信任所有证书的 {@link SSLContext}（内网 Dashboard 场景）。\n * 使用静态内部类实现懒加载单例。\n *\n * @author Leo Li\n */",
        ),
        (
            "    private static class SslContextInstance {",
            "    /** 持有单例 SSLContext 的静态内部类。 */\n    private static class SslContextInstance {",
        ),
        (
            "    private static SSLContext initSslContext() {",
            "    /** 初始化 TLS 上下文并注册信任所有证书的 TrustManager。 */\n    private static SSLContext initSslContext() {",
        ),
        (
            "    public static SSLContext getSslConnectionSocketFactory() {",
            "    /** @return 全局 SSLContext 单例。 */\n    public static SSLContext getSslConnectionSocketFactory() {",
        ),
    ],
    "HttpCommandUtils.java": [
        (
            "/**\n * Util class for HTTP command center.\n *\n * @author Eric Zhao\n */",
            "/**\n * HTTP 命令中心工具类：从 {@link CommandRequest} 元数据提取路由目标命令名。\n *\n * @author Eric Zhao\n */",
        ),
        (
            "    public static final String REQUEST_TARGET = \"command-target\";",
            "    /** 元数据键：目标命令名称。 */\n    public static final String REQUEST_TARGET = \"command-target\";",
        ),
        (
            "    public static String getTarget(CommandRequest request) {",
            "    /** 从请求元数据读取 command-target，即待执行的命令名。 */\n    public static String getTarget(CommandRequest request) {",
        ),
        (
            '            throw new IllegalArgumentException("Request cannot be null");',
            '            throw new IllegalArgumentException("请求不能为 null");',
        ),
    ],
    "WritableDataSourceRegistry.java": [
        (
            "package com.alibaba.csp.sentinel.transport.util;",
            "/*\n * Copyright 1999-2018 Alibaba Group Holding Ltd.\n *\n * Licensed under the Apache License, Version 2.0 (the \"License\");\n * you may not use this file except in compliance with the License.\n * You may obtain a copy of the License at\n *\n *      http://www.apache.org/licenses/LICENSE-2.0\n *\n * Unless required by applicable law or agreed to in writing, software\n * distributed under the License is distributed on an \"AS IS\" BASIS,\n * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n * See the License for the specific language governing permissions and\n * limitations under the License.\n */\npackage com.alibaba.csp.sentinel.transport.util;",
        ),
        (
            "/**\n * Writable data source registry for modifying rules via HTTP API.\n *\n * @author Eric Zhao\n */",
            "/**\n * 可写数据源注册表：供 HTTP 命令修改流控/降级/授权/系统规则时持久化。\n * 各规则类型对应一个 {@link WritableDataSource}，由应用启动时注册。\n *\n * @author Eric Zhao\n */",
        ),
        (
            "    private static WritableDataSource<List<FlowRule>> flowDataSource = null;",
            "    /** 流控规则可写数据源。 */\n    private static WritableDataSource<List<FlowRule>> flowDataSource = null;",
        ),
        (
            "    public static synchronized void registerFlowDataSource(WritableDataSource<List<FlowRule>> datasource) {",
            "    /** 注册流控规则可写数据源。 */\n    public static synchronized void registerFlowDataSource(WritableDataSource<List<FlowRule>> datasource) {",
        ),
        (
            "    public static WritableDataSource<List<FlowRule>> getFlowDataSource() {",
            "    /** @return 已注册的流控规则数据源，可能为 null。 */\n    public static WritableDataSource<List<FlowRule>> getFlowDataSource() {",
        ),
        (
            "    public static WritableDataSource<List<SystemRule>> getSystemSource() {",
            "    /** @return 已注册的系统规则数据源，可能为 null。 */\n    public static WritableDataSource<List<SystemRule>> getSystemSource() {",
        ),
    ],
    "NettyHttpCommandCenter.java": [
        (
            "/**\n * Implementation of {@link CommandCenter} based on Netty HTTP library.\n *\n * @author Eric Zhao\n */",
            "/**\n * 基于 Netty HTTP 的 {@link CommandCenter} 实现：在独立线程中启动 {@link HttpServer} 监听命令请求。\n * SPI 优先级 {@code ORDER_LOWEST - 100}，作为默认传输实现之一。\n *\n * @author Eric Zhao\n */",
        ),
        (
            "    private final HttpServer server = new HttpServer();",
            "    /** Netty HTTP 命令服务实例。 */\n    private final HttpServer server = new HttpServer();",
        ),
        (
            "        // Register handlers",
            "        // 将 SPI 加载的全部 CommandHandler 注册到 HTTP 路由",
        ),
    ],
    "CodecRegistry.java": [
        (
            "/**\n * @author Eric Zhao\n */",
            "/**\n * 命令编解码器注册表：维护 {@link Encoder} 与 {@link Decoder} 列表，构造时注册默认字符串编解码器。\n *\n * @author Eric Zhao\n */",
        ),
        (
            "        // Register default codecs.",
            "        // 注册默认字符串编解码器",
        ),
        (
            "    public void registerEncoder(Encoder<?> encoder) {",
            "    /** 注册响应体编码器。 */\n    public void registerEncoder(Encoder<?> encoder) {",
        ),
        (
            "    public void registerDecoder(Decoder<?> decoder) {",
            "    /** 注册请求体解码器。 */\n    public void registerDecoder(Decoder<?> decoder) {",
        ),
        (
            "    public void reset() {",
            "    /** 清空已注册的编解码器列表。 */\n    public void reset() {",
        ),
    ],
    "Decoder.java": [
        (
            "/**\n * The decoder decodes bytes into an object of type {@code <R>}.\n *\n * @param <R> target type\n * @author Eric Zhao\n */",
            "/**\n * 命令请求体解码器：将字节数组解码为类型 {@code R} 的对象。\n * 通过 {@link #canDecode} 判断是否支持目标类型。\n *\n * @param <R> 目标类型\n * @author Eric Zhao\n */",
        ),
        (
            "     * Check whether the decoder supports the given target type.\n     *\n     * @param clazz type of the class\n     * @return {@code true} if supported, {@code false} otherwise",
            "     * 判断是否支持解码为指定类型。\n     *\n     * @param clazz 目标类\n     * @return 支持时 true，否则 false",
        ),
        (
            "     * Decode the given byte array into an object of type {@code R} with the default charset.\n     *\n     * @param bytes raw byte buffer\n     * @return the decoded target object\n     * @throws Exception error occurs when decoding the object (e.g. IO fails)",
            "     * 使用默认字符集将字节数组解码为目标对象。\n     *\n     * @param bytes 原始字节\n     * @return 解码结果\n     * @throws Exception 解码失败时抛出",
        ),
    ],
    "DefaultCodecs.java": [
        (
            "/**\n * Caches default encoders and decoders.\n *\n * @author Eric Zhao\n */",
            "/**\n * 默认编解码器常量：字符串类型的 {@link StringEncoder} 与 {@link StringDecoder}。\n *\n * @author Eric Zhao\n */",
        ),
        (
            "    public static final Encoder<String> STRING_ENCODER = new StringEncoder();",
            "    /** 默认字符串编码器。 */\n    public static final Encoder<String> STRING_ENCODER = new StringEncoder();",
        ),
        (
            "    public static final Decoder<String> STRING_DECODER = new StringDecoder();",
            "    /** 默认字符串解码器。 */\n    public static final Decoder<String> STRING_DECODER = new StringDecoder();",
        ),
    ],
    "Encoder.java": [
        (
            "/**\n * The encoder encodes an object of type {@code <R>} into byte array.\n *\n * @param <R> source type\n * @author Eric Zhao\n */",
            "/**\n * 命令响应体编码器：将类型 {@code R} 的对象编码为字节数组。\n * 通过 {@link #canEncode} 判断是否支持源类型。\n *\n * @param <R> 源类型\n * @author Eric Zhao\n */",
        ),
        (
            "     * Check whether the encoder supports the given source type.\n     *\n     * @param clazz type of the class\n     * @return {@code true} if supported, {@code false} otherwise",
            "     * 判断是否支持编码指定类型的对象。\n     *\n     * @param clazz 源类型\n     * @return 支持时 true，否则 false",
        ),
        (
            "     * Encode the given object into a byte array with the given charset.\n     *\n     * @param r the object to encode\n     * @param charset the charset\n     * @return the encoded byte buffer\n     * @throws Exception error occurs when encoding the object (e.g. IO fails)",
            "     * 使用指定字符集将对象编码为字节数组。\n     *\n     * @param r 待编码对象\n     * @param charset 字符集\n     * @return 编码后的字节\n     * @throws Exception 编码失败时抛出",
        ),
    ],
    "StringDecoder.java": [
        (
            "/**\n * Decodes from a byte array to string.\n *\n * @author Eric Zhao\n */",
            "/**\n * 字符串解码器：将请求体字节按 {@link SentinelConfig#charset()} 或指定 Charset 转为 String。\n *\n * @author Eric Zhao\n */",
        ),
        (
            "    public boolean canDecode(Class<?> clazz) {",
            "    /** 仅支持 {@link String} 及其子类。 */\n    public boolean canDecode(Class<?> clazz) {",
        ),
        (
            '            throw new IllegalArgumentException("Bad byte array");',
            '            throw new IllegalArgumentException("无效的字节数组");',
        ),
    ],
}
