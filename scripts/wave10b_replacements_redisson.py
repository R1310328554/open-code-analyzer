"""Chinese annotation replacements for Redisson 4.7.0 wave-10b spring-data-17/18 [15:30]."""
from __future__ import annotations

W10B_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

# --- spring-data-17: ZSET tuple decoder V2 ---
W10B_REPLACEMENTS["ScoredSortedSetReplayDecoderV2.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 单条 member/score 对解码为单个 {@link RedisZSetCommands.Tuple}（V2 接口）。\n"
        " * <p>用于仅返回一个元素的 ZSET 命令响应；奇数下标参数以 {@link DoubleCodec} 解析 score。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "        return MultiDecoder.super.getDecoder(codec, paramNum, state, size);",
        "        // 偶数下标 member 走默认 Codec 解码。\n"
        "        return MultiDecoder.super.getDecoder(codec, paramNum, state, size);",
    ),
    (
        "    @Override\n    public RedisZSetCommands.Tuple decode(List<Object> parts, State state) {",
        "    /** 从两元素列表构造 {@link DefaultTuple}。 */\n"
        "    @Override\n"
        "    public RedisZSetCommands.Tuple decode(List<Object> parts, State state) {",
    ),
]

# --- spring-data-17: time unit convertor ---
W10B_REPLACEMENTS["SecondsConvertor.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 时间单位转换器：将 Redis 协议层数值从 {@code source} 单位换算为 {@code unit}。\n"
        " * <p>常用于 EXPIRE/PEXPIRE 等命令在秒与毫秒间转换。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "    private final TimeUnit unit;\n    private final TimeUnit source;",
        "    /** 目标时间单位。 */\n"
        "    private final TimeUnit unit;\n"
        "    /** 源时间单位（Redis 返回值语义）。 */\n"
        "    private final TimeUnit source;",
    ),
    (
        "    public SecondsConvertor(TimeUnit unit, TimeUnit source) {",
        "    /** 指定目标与源 {@link TimeUnit}。 */\n"
        "    public SecondsConvertor(TimeUnit unit, TimeUnit source) {",
    ),
    (
        "    @Override\n    public Long convert(Object obj) {",
        "    /** 将 {@link Long} 数值从 {@code source} 换算为 {@code unit}。 */\n"
        "    @Override\n"
        "    public Long convert(Object obj) {",
    ),
]

# --- spring-data-17: set replay decoder ---
W10B_REPLACEMENTS["SetReplayDecoder.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 集合型 Redis 响应解码器：将元素列表转为 {@link LinkedHashSet} 以保持顺序。\n"
        " * <p>元素解码由构造时注入的 {@link Decoder} 负责。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "    public SetReplayDecoder(Decoder<Object> decoder) {",
        "    /** 指定集合元素解码器。 */\n"
        "    public SetReplayDecoder(Decoder<Object> decoder) {",
    ),
    (
        "    @Override\n    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size) {",
        "    /** 返回构造时注入的元素解码器。 */\n"
        "    @Override\n"
        "    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size) {",
    ),
    (
        "    @Override\n    public Set<T> decode(List<Object> parts, State state) {",
        "    /** 将响应元素列表包装为 {@link LinkedHashSet}。 */\n"
        "    @Override\n"
        "    public Set<T> decode(List<Object> parts, State state) {",
    ),
]

# --- spring-data-18: protocol convertors ---
W10B_REPLACEMENTS["BinaryConvertor.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Redis 协议层字符串到字节数组转换器。\n"
        " * <p>供 Spring Data Redis 命令参数在 UTF-8 二进制与 String 间转换。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "    @Override\n    public Object convert(Object obj) {",
        "    /** {@link String} 转为 UTF-8 字节数组；其他类型原样返回。 */\n"
        "    @Override\n"
        "    public Object convert(Object obj) {",
    ),
]

W10B_REPLACEMENTS["DataTypeConvertor.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Redis {@code TYPE} 命令返回值到 Spring {@link DataType} 的转换器。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "    @Override\n    public DataType convert(Object obj) {",
        "    /** 将 Redis 类型码字符串解析为 {@link DataType} 枚举。 */\n"
        "    @Override\n"
        "    public DataType convert(Object obj) {",
    ),
]

W10B_REPLACEMENTS["DistanceConvertor.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 将 Redis GEO 距离数值包装为 Spring {@link Distance}。\n"
        " * <p>构造时指定 {@link Metric}（如千米、英里），供 {@code GEODIST} 等命令使用。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "    public DistanceConvertor(Metric metric) {",
        "    /** 绑定距离度量单位。 */\n"
        "    public DistanceConvertor(Metric metric) {",
    ),
    (
        "    @Override\n    public Distance convert(Object obj) {",
        "    /** 将 {@link Double} 数值与 {@code metric} 组装为 {@link Distance}。 */\n"
        "    @Override\n"
        "    public Distance convert(Object obj) {",
    ),
]

# --- spring-data-18: GEO decoders ---
W10B_REPLACEMENTS["GeoResultsDecoder.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Redis GEO 批量响应解码器，产出 {@link GeoResults}{@code <}{@link GeoLocation}{@code <byte[]>}{@code >}。\n"
        " * <p>支持带距离（{@code metric} 非空）或带坐标（{@code Point}）两种嵌套列表格式；\n"
        "单元素条目视为仅 member 无附加信息。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "    public GeoResultsDecoder() {",
        "    /** 默认构造：嵌套列表第二项解析为 {@link Point}。 */\n"
        "    public GeoResultsDecoder() {",
    ),
    (
        "    public GeoResultsDecoder(Metric metric) {",
        "    /** 指定距离度量；非空时第二项解析为带 {@code metric} 的 {@link Distance}。 */\n"
        "    public GeoResultsDecoder(Metric metric) {",
    ),
    (
        "                if (metric != null) {",
        "                // GEORADIUS 等带距离字段的响应。\n"
        "                if (metric != null) {",
    ),
    (
        "                } else {",
        "                // GEOPOS 等返回经纬度 Point 的响应。\n"
        "                } else {",
    ),
    (
        "            } else {",
        "            // 单字节数组 member，无距离/坐标附加项。\n"
        "            } else {",
    ),
]

W10B_REPLACEMENTS["PointDecoder.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 将 Redis GEO 坐标对（经度、纬度）解码为 Spring {@link Point}。\n"
        " * <p>空列表返回 {@code null}；各坐标分量以 {@link DoubleCodec} 解析。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "    @Override\n    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size) {",
        "    /** 经纬度分量均按 double 解码。 */\n"
        "    @Override\n"
        "    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size) {",
    ),
    (
        "        if (parts.isEmpty()) {",
        "        // Redis 返回空列表表示该 member 无坐标。\n"
        "        if (parts.isEmpty()) {",
    ),
    (
        "    @Override\n    public Point decode(List<Object> parts, State state) {",
        "    /** 从前两个 double 元素构造 {@link Point}(longitude, latitude)。 */\n"
        "    @Override\n"
        "    public Point decode(List<Object> parts, State state) {",
    ),
]

# --- spring-data-18: replay / property decoders ---
W10B_REPLACEMENTS["ObjectListReplayDecoder2.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n * @param <T> type\n */",
        "/**\n"
        " * 列表型 Redis 响应解码器（第二版）：将空嵌套列表规范为 {@code null}。\n"
        " * <p>用于 Spring Data Redis 批量读取时对齐空集合语义。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " * @param <T> type\n"
        " */",
    ),
    (
        "    public ObjectListReplayDecoder2(Decoder<Object> decoder) {",
        "    /** 指定嵌套元素解码器；{@code null} 时使用默认 Codec 解码。 */\n"
        "    public ObjectListReplayDecoder2(Decoder<Object> decoder) {",
    ),
    (
        "                if (((List) object).isEmpty()) {",
        "                if (((List) object).isEmpty()) {\n"
        "                    // 空子列表视为 null，与 Spring Data 空值约定一致。\n",
    ),
    (
        "    @Override\n    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size) {",
        "    /** 返回构造时注入的元素解码器。 */\n"
        "    @Override\n"
        "    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size) {",
    ),
]

W10B_REPLACEMENTS["PropertiesDecoder.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 将 Redis INFO/CONFIG 类 colon 分隔文本解码为 {@link Properties}。\n"
        " * <p>按行拆分 {@code key:value}，忽略格式非法行。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "    @Override\n    public Properties decode(ByteBuf buf, State state) {",
        "    /** 从 Netty {@link ByteBuf} 读取 UTF-8 文本并解析为属性表。 */\n"
        "    @Override\n"
        "    public Properties decode(ByteBuf buf, State state) {",
    ),
    (
        "            if (second.charAt(second.length() - 1) == '\\r') {",
        "            // 去除 Windows 换行残留 \\r。\n"
        "            if (second.charAt(second.length() - 1) == '\\r') {",
    ),
]

W10B_REPLACEMENTS["RedisClusterNodeDecoder.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 将 Redis {@code CLUSTER NODES} 文本响应解码为 {@link RedisClusterNode} 列表。\n"
        " * <p>解析 node id、地址、flags、master、link state 与 slot 区间；\n"
        "经 {@link ServiceManager#toURI} 规范化 host/port。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "    public RedisClusterNodeDecoder(ServiceManager serviceManager) {",
        "    /** 注入连接管理器，用于地址解析与 URI 规范化。 */\n"
        "    public RedisClusterNodeDecoder(ServiceManager serviceManager) {",
    ),
    (
        "    @Override\n    public List<RedisClusterNode> decode(ByteBuf buf, State state) throws IOException {",
        "    /** 按行解析 CLUSTER NODES 输出并构建 {@link RedisClusterNode} 集合。 */\n"
        "    @Override\n"
        "    public List<RedisClusterNode> decode(ByteBuf buf, State state) throws IOException {",
    ),
    (
        "                if (name.isEmpty()) {\n                    // skip nodes with empty address\n                    continue;",
        "                if (name.isEmpty()) {\n"
        "                    // 跳过无有效地址的节点（NOADDR 等）。\n"
        "                    continue;",
    ),
    (
        "            if (\"-\".equals(masterId)) {",
        "            // \"-\" 表示当前节点为 master，无 upstream master。\n"
        "            if (\"-\".equals(masterId)) {",
    ),
    (
        "                    if (slots.indexOf(\"-<-\") != -1 || slots.indexOf(\"->-\") != -1) {",
        "                    // 忽略 slot 迁移中的箭头标记行。\n"
        "                    if (slots.indexOf(\"-<-\") != -1 || slots.indexOf(\"->-\") != -1) {",
    ),
    (
        "                    if(parts.length == 1) {",
        "                    // 单个 slot 号。\n"
        "                    if(parts.length == 1) {",
    ),
    (
        "                    } else if(parts.length == 2) {",
        "                    // slot 区间 start-end 展开为连续编号。\n"
        "                    } else if(parts.length == 2) {",
    ),
]

# --- spring-data-18: connection factory (cluster-aware) ---
W10B_REPLACEMENTS["RedissonConnectionFactory.java"] = [
    (
        "/**\n * Redisson based connection factory\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 基于 Redisson 的 Spring Data Redis {@link RedisConnectionFactory}。\n"
        " * <p>集群模式下返回 {@link RedissonClusterConnection}，单机返回 {@link RedissonConnection}；\n"
        "Sentinel 管理经 {@link RedissonSentinelConnection}；异常由 {@link RedissonExceptionConverter} 翻译。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "    public static final ExceptionTranslationStrategy EXCEPTION_TRANSLATION = \n"
        "                                new PassThroughExceptionTranslationStrategy(new RedissonExceptionConverter());",
        "    /** 全局异常翻译策略，供 {@link #translateExceptionIfPossible} 使用。 */\n"
        "    public static final ExceptionTranslationStrategy EXCEPTION_TRANSLATION = \n"
        "                                new PassThroughExceptionTranslationStrategy(new RedissonExceptionConverter());",
    ),
    (
        "    private boolean hasOwnRedisson;",
        "    /** {@code true} 表示工厂内部创建了 {@link RedissonClient}，销毁时需 shutdown。 */\n"
        "    private boolean hasOwnRedisson;",
    ),
    (
        "    /**\n     * Creates factory with default Redisson configuration\n     */",
        "    /** 使用 {@link Redisson#create()} 默认配置创建工厂。 */\n"
        "    /**\n     * Creates factory with default Redisson configuration\n     */",
    ),
    (
        "    @Override\n    public void destroy() throws Exception {",
        "    /** 若持有自建客户端则关闭 Redisson 实例。 */\n"
        "    @Override\n"
        "    public void destroy() throws Exception {",
    ),
    (
        "    @Override\n    public void afterPropertiesSet() throws Exception {",
        "    /** 若注入了 {@link Config}，在此阶段创建 {@link RedissonClient}。 */\n"
        "    @Override\n"
        "    public void afterPropertiesSet() throws Exception {",
    ),
    (
        "    @Override\n    public RedisConnection getConnection() {",
        "    /** 按配置返回 {@link RedissonClusterConnection} 或 {@link RedissonConnection}。 */\n"
        "    @Override\n"
        "    public RedisConnection getConnection() {",
    ),
    (
        "        if (redisson.getConfig().isClusterConfig()) {",
        "        // 集群配置时使用 Cluster 连接实现。\n"
        "        if (redisson.getConfig().isClusterConfig()) {",
    ),
    (
        "    @Override\n    public RedisClusterConnection getClusterConnection() {",
        "    /** 非集群模式调用时抛出 {@link InvalidDataAccessResourceUsageException}。 */\n"
        "    @Override\n"
        "    public RedisClusterConnection getClusterConnection() {",
    ),
    (
        "        if (!redisson.getConfig().isClusterConfig()) {",
        "        // 非 Cluster 配置时拒绝创建 Cluster 连接。\n"
        "        if (!redisson.getConfig().isClusterConfig()) {",
    ),
    (
        "    @Override\n    public RedisSentinelConnection getSentinelConnection() {",
        "    /** 遍历 Sentinel 节点 PING，返回首个可用的 {@link RedissonSentinelConnection}。 */\n"
        "    @Override\n"
        "    public RedisSentinelConnection getSentinelConnection() {",
    ),
    (
        "        if (!redisson.getConfig().isSentinelConfig()) {",
        "        // 非 Sentinel 配置时拒绝创建 Sentinel 连接。\n"
        "        if (!redisson.getConfig().isSentinelConfig()) {",
    ),
    (
        "                if (\"pong\".equalsIgnoreCase(res)) {",
        "                // 首个响应 PONG 的 Sentinel 用于 Spring Data 管理命令。\n"
        "                if (\"pong\".equalsIgnoreCase(res)) {",
    ),
]

W10B_REPLACEMENTS["RedissonExceptionConverter.java"] = [
    (
        "/**\n * Converts Redisson exceptions to Spring compatible\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 将 Redisson 客户端异常映射为 Spring Data Redis {@link DataAccessException}。\n"
        " * <p>连接失败、集群重定向、超时与通用 Redis 错误分别对应不同 Spring 异常类型。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "        if (source instanceof RedisConnectionException) {",
        "        // 连接层错误 -> RedisConnectionFailureException。\n"
        "        if (source instanceof RedisConnectionException) {",
    ),
    (
        "        if (source instanceof RedisRedirectException) {",
        "        // 集群 MOVED/ASK 重定向 -> ClusterRedirectException。\n"
        "        if (source instanceof RedisRedirectException) {",
    ),
    (
        "        if (source instanceof RedisTimeoutException) {",
        "        // 命令超时 -> QueryTimeoutException。\n"
        "        if (source instanceof RedisTimeoutException) {",
    ),
    (
        "        if (source instanceof RedisException) {",
        "        // 其他 Redis 协议错误 -> InvalidDataAccessApiUsageException。\n"
        "        if (source instanceof RedisException) {",
    ),
]

W10B_REPLACEMENTS["RedissonSentinelConnection.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Spring Data Redis {@link RedisSentinelConnection} 的 Redisson 实现。\n"
        " * <p>通过底层 {@link RedisConnection} 同步发送 Sentinel 管理命令\n"
        "（failover、monitor、masters/slaves 查询等）。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "    public RedissonSentinelConnection(RedisConnection connection) {",
        "    /** 绑定已连通的 Sentinel {@link RedisConnection}。 */\n"
        "    public RedissonSentinelConnection(RedisConnection connection) {",
    ),
    (
        "    @Override\n    public void failover(NamedNode master) {",
        "    /** 对指定 master 执行 {@code SENTINEL FAILOVER}。 */\n"
        "    @Override\n"
        "    public void failover(NamedNode master) {",
    ),
    (
        "    @Override\n    public Collection<RedisServer> masters() {",
        "    /** 查询所有被监控的 master 并转为 {@link RedisServer} 列表。 */\n"
        "    @Override\n"
        "    public Collection<RedisServer> masters() {",
    ),
    (
        "    @Override\n    public Collection<RedisServer> slaves(NamedNode master) {",
        "    /** 查询指定 master 下的 replica 节点。 */\n"
        "    @Override\n"
        "    public Collection<RedisServer> slaves(NamedNode master) {",
    ),
    (
        "    @Override\n    public void monitor(RedisServer master) {",
        "    /** 向 Sentinel 注册新的 master 监控（host/port/quorum）。 */\n"
        "    @Override\n"
        "    public void monitor(RedisServer master) {",
    ),
]

W10B_REPLACEMENTS["RedissonSubscription.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Spring Data Redis Pub/Sub {@link AbstractSubscription} 的 Redisson 实现。\n"
        " * <p>通过 {@link PublishSubscribeService} 管理频道/模式订阅，\n"
        "将 Redisson 消息转为 {@link DefaultMessage} 回调 {@link MessageListener}。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "    public RedissonSubscription(CommandAsyncExecutor commandExecutor, MessageListener listener) {",
        "    /** 绑定异步命令执行器与 Spring 消息监听器。 */\n"
        "    public RedissonSubscription(CommandAsyncExecutor commandExecutor, MessageListener listener) {",
    ),
    (
        "    @Override\n    protected void doSubscribe(byte[]... channels) {",
        "    /** 对每个频道注册 {@link BaseRedisPubSubListener} 并阻塞等待订阅完成。 */\n"
        "    @Override\n"
        "    protected void doSubscribe(byte[]... channels) {",
    ),
    (
        "                    if (!Arrays.equals(((ChannelName) ch).getName(), channel)) {",
        "                    // 忽略非目标频道的回调（连接复用时可能收到其他频道消息）。\n"
        "                    if (!Arrays.equals(((ChannelName) ch).getName(), channel)) {",
    ),
    (
        "    @Override\n    protected void doPsubscribe(byte[]... patterns) {",
        "    /** 按模式订阅（PSUBSCRIBE），回调携带 pattern 与 channel。 */\n"
        "    @Override\n"
        "    protected void doPsubscribe(byte[]... patterns) {",
    ),
    (
        "    private byte[] toBytes(Object message) {",
        "    /** 将 String 或 byte[] 载荷统一为字节数组。 */\n"
        "    private byte[] toBytes(Object message) {",
    ),
    (
        "    @Override\n    protected void doClose() {",
        "    /** 关闭时取消所有频道与模式订阅。 */\n"
        "    @Override\n"
        "    protected void doClose() {",
    ),
]
