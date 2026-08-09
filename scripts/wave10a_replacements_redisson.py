"""Chinese annotation replacements for Redisson 4.7.0 wave-10a spring-data-16/17 [0:15]."""
from __future__ import annotations

W10A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

# --- spring-data-16: time convertor ---
W10A_REPLACEMENTS["SecondsConvertor.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 将 Redis 时间数值在 {@link TimeUnit} 间转换的协议层转换器。\n"
        " * <p>Spring Data Redis 命令参数常以秒为单位，需与目标 {@code TimeUnit} 对齐。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "    private final TimeUnit unit;\n    private final TimeUnit source;",
        "    /** 目标时间单位。 */\n"
        "    private final TimeUnit unit;\n"
        "    /** 源时间单位（Redis 返回值侧）。 */\n"
        "    private final TimeUnit source;",
    ),
    (
        "    public SecondsConvertor(TimeUnit unit, TimeUnit source) {",
        "    /** 指定源单位与目标单位的转换关系。 */\n"
        "    public SecondsConvertor(TimeUnit unit, TimeUnit source) {",
    ),
    (
        "    @Override\n    public Long convert(Object obj) {",
        "    /** 将 {@code Long} 时长从 {@code source} 换算为 {@code unit}。 */\n"
        "    @Override\n"
        "    public Long convert(Object obj) {",
    ),
]

# --- spring-data-16: set replay decoder ---
W10A_REPLACEMENTS["SetReplayDecoder.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 将 Redis 集合型批量响应解码为 {@link Set}{@code <T>}（{@link LinkedHashSet} 保序去重）。\n"
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
        "    /** 将已解码元素列表包装为 {@link LinkedHashSet}。 */\n"
        "    @Override\n"
        "    public Set<T> decode(List<Object> parts, State state) {",
    ),
]

# --- spring-data-17: protocol convertors ---
W10A_REPLACEMENTS["BinaryConvertor.java"] = [
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

W10A_REPLACEMENTS["DataTypeConvertor.java"] = [
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

W10A_REPLACEMENTS["DistanceConvertor.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 将 Redis GEO 距离数值（{@code double}）包装为 Spring {@link Distance}。\n"
        " * <p>构造时绑定 {@link Metric}（如 {@code Metrics.KILOMETERS}）。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "    private final Metric metric;",
        "    /** 距离度量单位。 */\n"
        "    private final Metric metric;",
    ),
    (
        "    public DistanceConvertor(Metric metric) {",
        "    /** 指定 {@link Distance} 使用的 {@link Metric}。 */\n"
        "    public DistanceConvertor(Metric metric) {",
    ),
    (
        "    @Override\n    public Distance convert(Object obj) {",
        "    /** 用 Redis 返回的浮点距离构造 {@link Distance}。 */\n"
        "    @Override\n"
        "    public Distance convert(Object obj) {",
    ),
]

# --- spring-data-17: replay decoders ---
W10A_REPLACEMENTS["ObjectListReplayDecoder2.java"] = [
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
        "    public ObjectListReplayDecoder2() {",
        "    /** 使用默认 Codec 解码嵌套元素。 */\n"
        "    public ObjectListReplayDecoder2() {",
    ),
    (
        "    public ObjectListReplayDecoder2(Decoder<Object> decoder) {",
        "    /** 指定嵌套元素解码器；{@code null} 时使用默认 Codec 解码。 */\n"
        "    public ObjectListReplayDecoder2(Decoder<Object> decoder) {",
    ),
    (
        "                if (((List) object).isEmpty()) {\n                    parts.set(i, null);",
        "                if (((List) object).isEmpty()) {\n"
        "                    // 空子列表视为 null，与 Spring Data 空值约定一致。\n"
        "                    parts.set(i, null);",
    ),
    (
        "    @Override\n    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size) {",
        "    /** 返回构造时注入的元素解码器。 */\n"
        "    @Override\n"
        "    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size) {",
    ),
]

W10A_REPLACEMENTS["PointDecoder.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 将 Redis GEO {@code GEOPOS} 等命令的经纬度对解码为 Spring {@link Point}。\n"
        " * <p>坐标分量使用 {@link DoubleCodec} 解析；空响应返回 {@code null}。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "    @Override\n    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size) {",
        "    /** 经纬度分量均按 {@code double} 解码。 */\n"
        "    @Override\n"
        "    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size) {",
    ),
    (
        "        if (parts.isEmpty()) {",
        "        // 无坐标数据时返回 null。\n"
        "        if (parts.isEmpty()) {",
    ),
    (
        "    @Override\n    public Point decode(List<Object> parts, State state) {",
        "    /** 从 [经度, 纬度] 两元素列表构造 {@link Point}。 */\n"
        "    @Override\n"
        "    public Point decode(List<Object> parts, State state) {",
    ),
]

W10A_REPLACEMENTS["PropertiesDecoder.java"] = [
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

W10A_REPLACEMENTS["RedisClusterNodeDecoder.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 解析 Redis {@code CLUSTER NODES} 文本响应为 {@link RedisClusterNode} 列表。\n"
        " * <p>提取 nodeId、地址、角色标志、master 关联、槽位范围与链路状态；\n"
        " * 空地址节点（{@link Flag#NOADDR}）经 {@link ServiceManager#toURI} 规范化。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "    private final ServiceManager serviceManager;",
        "    /** 用于将集群 bus 地址映射为客户端可连 URI。 */\n"
        "    private final ServiceManager serviceManager;",
    ),
    (
        "    public RedisClusterNodeDecoder(ServiceManager serviceManager) {",
        "    /** 注入 Redisson 连接管理器以解析节点地址。 */\n"
        "    public RedisClusterNodeDecoder(ServiceManager serviceManager) {",
    ),
    (
        "                if (name.isEmpty()) {\n                    // skip nodes with empty address",
        "                if (name.isEmpty()) {\n"
        "                    // 跳过无有效 host 的占位节点。\n"
        "                    // skip nodes with empty address",
    ),
    (
        "                    if (slots.indexOf(\"-<-\") != -1 || slots.indexOf(\"->-\") != -1) {",
        "                    // 忽略槽迁移中间态标记行。\n"
        "                    if (slots.indexOf(\"-<-\") != -1 || slots.indexOf(\"->-\") != -1) {",
    ),
    (
        "    @Override\n    public List<RedisClusterNode> decode(ByteBuf buf, State state) throws IOException {",
        "    /** 逐行解析 CLUSTER NODES 输出并组装 {@link RedisClusterNode}。 */\n"
        "    @Override\n"
        "    public List<RedisClusterNode> decode(ByteBuf buf, State state) throws IOException {",
    ),
]

# --- spring-data-17: connection factory (cluster-aware) ---
W10A_REPLACEMENTS["RedissonConnectionFactory.java"] = [
    (
        "/**\n * Redisson based connection factory\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 基于 Redisson 的 Spring Data Redis {@link RedisConnectionFactory}。\n"
        " * <p>集群模式返回 {@link RedissonClusterConnection}，单机返回 {@link RedissonConnection}；\n"
        "Sentinel 模式下 {@link #getSentinelConnection()} 探测可用节点；\n"
        "异常经 {@link RedissonExceptionConverter} 转为 Spring {@link DataAccessException}。\n"
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
        "    /**\n     * Creates factory with default Redisson configuration\n     */",
        "    /** 使用 {@link Redisson#create()} 默认配置创建工厂。 */\n"
        "    /**\n     * Creates factory with default Redisson configuration\n     */",
    ),
    (
        "    @Override\n    public void afterPropertiesSet() throws Exception {",
        "    /** 若注入了 {@link Config}，在此阶段创建 {@link RedissonClient}。 */\n"
        "    @Override\n"
        "    public void afterPropertiesSet() throws Exception {",
    ),
    (
        "    @Override\n    public RedisConnection getConnection() {",
        "    /** 按配置返回单机或集群 {@link RedisConnection}。 */\n"
        "    @Override\n"
        "    public RedisConnection getConnection() {",
    ),
    (
        "        if (redisson.getConfig().isClusterConfig()) {",
        "        // 集群配置时使用 RedissonClusterConnection。\n"
        "        if (redisson.getConfig().isClusterConfig()) {",
    ),
    (
        "    @Override\n    public RedisClusterConnection getClusterConnection() {",
        "    /** 非集群模式调用将抛出 {@link InvalidDataAccessResourceUsageException}。 */\n"
        "    @Override\n"
        "    public RedisClusterConnection getClusterConnection() {",
    ),
    (
        "        if (!redisson.getConfig().isClusterConfig()) {",
        "        // 非集群配置时拒绝创建集群连接。\n"
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

W10A_REPLACEMENTS["RedissonExceptionConverter.java"] = [
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

W10A_REPLACEMENTS["RedissonSentinelConnection.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Spring Data Redis {@link RedisSentinelConnection} 的 Redisson 实现。\n"
        " * <p>通过底层 {@link RedisConnection} 同步发送 Sentinel 管理命令\n"
        "（failover、monitor、masters/slaves 查询、remove 等）。\n"
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
        "    @Override\n    public void remove(NamedNode master) {",
        "    /** 从 Sentinel 监控列表移除指定 master（{@code SENTINEL REMOVE}）。 */\n"
        "    @Override\n"
        "    public void remove(NamedNode master) {",
    ),
    (
        "    @Override\n    public void monitor(RedisServer master) {",
        "    /** 向 Sentinel 注册新的 master 监控（host/port/quorum）。 */\n"
        "    @Override\n"
        "    public void monitor(RedisServer master) {",
    ),
]

W10A_REPLACEMENTS["RedissonSubscription.java"] = [
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

W10A_REPLACEMENTS["ScoredSortedListReplayDecoder.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 将 Redis 有序集合批量响应（member/score 交替）解码为 {@link List}{@code <}{@link Tuple}{@code >}。\n"
        " * <p>奇数参数位使用 {@link DoubleCodec} 解析 score。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "    @Override\n    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size) {",
        "    /** 奇数下标参数解码为 {@code double} score。 */\n"
        "    @Override\n"
        "    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size) {",
    ),
    (
        "    @Override\n    public List<Tuple> decode(List<Object> parts, State state) {",
        "    /** 每两个元素组装为一个 {@link DefaultTuple}（member 字节数组 + score）。 */\n"
        "    @Override\n"
        "    public List<Tuple> decode(List<Object> parts, State state) {",
    ),
]

W10A_REPLACEMENTS["ScoredSortedSetReplayDecoder.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 有序集合响应解码为 {@link Set}{@code <}{@link Tuple}{@code >}（保持 Redis 返回顺序的 {@link LinkedHashSet}）。\n"
        " * <p>奇数参数位使用 {@link DoubleCodec} 解析 score。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "    @Override\n    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size) {",
        "    /** 奇数下标参数解码为 {@code double} score。 */\n"
        "    @Override\n"
        "    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size) {",
    ),
    (
        "    @Override\n    public Set<Tuple> decode(List<Object> parts, State state) {",
        "    /** 成对解析 member/score 并加入 {@link LinkedHashSet}。 */\n"
        "    @Override\n"
        "    public Set<Tuple> decode(List<Object> parts, State state) {",
    ),
]
