"""Chinese annotation replacements for Redisson 4.7.0 wave-9b spring-cache/data-16 [15:30]."""
from __future__ import annotations

W9B_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

# --- spring-cache: Actuator metrics auto-configuration (Boot 2.x) ---
W9B_REPLACEMENTS["RedissonCacheStatisticsAutoConfiguration.java"] = [
    (
        "/**\n *\n * @author Craig Andrews\n * @author Nikita Koksharov\n *\n"
        " * {@link EnableAutoConfiguration Auto-configuration} for {@link RedissonCacheMeterBinderProvider}\n *\n */",
        "/**\n"
        " * Spring Boot Actuator 缓存指标自动配置（Boot 2.x {@code CacheMeterBinderProvider}）。\n"
        " * <p>在存在 {@link CacheManager} 且 classpath 含 {@link RedissonCache} 时注册\n"
        " * {@link RedissonCacheMeterBinderProvider}，将 Redisson 缓存命中率等指标暴露给 Micrometer。\n"
        " *\n"
        " * @author Craig Andrews\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " * {@link EnableAutoConfiguration Auto-configuration} for {@link RedissonCacheMeterBinderProvider}\n"
        " *\n"
        " */",
    ),
    (
        "    @Bean\n    public RedissonCacheMeterBinderProvider redissonCacheMeterBinderProvider(){",
        "    /** 注册 Redisson 缓存 Micrometer 绑定器。 */\n"
        "    @Bean\n"
        "    public RedissonCacheMeterBinderProvider redissonCacheMeterBinderProvider(){",
    ),
]

# --- spring-cache: Actuator metrics auto-configuration (Boot 4.x) ---
W9B_REPLACEMENTS["RedissonCacheStatisticsAutoConfigurationV4.java"] = [
    (
        "/**\n *\n * @author Craig Andrews\n * @author Nikita Koksharov\n *\n"
        " * {@link EnableAutoConfiguration Auto-configuration} for {@link RedissonCacheMeterBinderProvider}\n *\n */",
        "/**\n"
        " * Spring Boot 4.x 缓存指标自动配置（{@code org.springframework.boot.cache.metrics.CacheMeterBinderProvider}）。\n"
        " * <p>条件与 {@link RedissonCacheStatisticsAutoConfiguration} 相同，绑定器实现为\n"
        " * {@link RedissonCacheMeterBinderProviderV4}。\n"
        " *\n"
        " * @author Craig Andrews\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " * {@link EnableAutoConfiguration Auto-configuration} for {@link RedissonCacheMeterBinderProvider}\n"
        " *\n"
        " */",
    ),
    (
        "    @Bean\n    public RedissonCacheMeterBinderProviderV4 redissonCacheMeterBinderProvider(){",
        "    /** 注册 Boot 4.x 版 Redisson 缓存 Micrometer 绑定器。 */\n"
        "    @Bean\n"
        "    public RedissonCacheMeterBinderProviderV4 redissonCacheMeterBinderProvider(){",
    ),
]

# --- spring-cache: CacheManager backed by RMap / RMapCache ---
W9B_REPLACEMENTS["RedissonSpringCacheManager.java"] = [
    (
        "/**\n * A {@link org.springframework.cache.CacheManager} implementation\n"
        " * backed by Redisson instance.\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 基于 Redisson {@link RMap}/{@link RMapCache} 的 Spring {@link org.springframework.cache.CacheManager}。\n"
        " * <p>支持动态创建缓存、YAML/Map 配置、Codec 共享、事务感知装饰及空值策略。\n"
        " * TTL/maxIdle/maxSize 全为 0 时使用 {@link RMap}，否则使用 {@link RMapCache}。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "    ResourceLoader resourceLoader;\n\n    private boolean dynamic = true;",
        "    /** Spring 资源加载器，用于读取 YAML 缓存配置。 */\n"
        "    ResourceLoader resourceLoader;\n\n"
        "    /** {@code true} 时按名称动态创建缓存；{@code false} 时仅使用预定义名称。 */\n"
        "    private boolean dynamic = true;",
    ),
    (
        "    private boolean allowNullValues = true;\n\n    private boolean transactionAware = false;",
        "    /** 是否允许缓存 {@code null} 值（默认 {@code true}）。 */\n"
        "    private boolean allowNullValues = true;\n\n"
        "    /** 是否包装 {@link TransactionAwareCacheDecorator} 以参与 Spring 事务。 */\n"
        "    private boolean transactionAware = false;",
    ),
    (
        "    /**\n     * Creates CacheManager supplied by Redisson instance\n     *\n     * @param redisson object\n     */",
        "    /** 使用 Redisson 客户端与默认配置创建 CacheManager。 */\n"
        "    /**\n     * Creates CacheManager supplied by Redisson instance\n     *\n     * @param redisson object\n     */",
    ),
    (
        "    /**\n     * Defines possibility of storing {@code null} values.\n     * <p>\n"
        "     * Default is <code>true</code>\n     * \n     * @param allowNullValues stores if <code>true</code>\n     */",
        "    /** 设置是否允许缓存空值；关闭时 {@code null} 不会写入 Redis。 */\n"
        "    /**\n     * Defines possibility of storing {@code null} values.\n     * <p>\n"
        "     * Default is <code>true</code>\n     * \n     * @param allowNullValues stores if <code>true</code>\n     */",
    ),
    (
        "    /**\n     * Defines if cache aware of Spring-managed transactions.\n"
        "     * If {@code true} put/evict operations are executed only for successful transaction in after-commit phase.\n"
        "     * <p>\n     * Default is <code>false</code>\n     *\n"
        "     * @param transactionAware cache is transaction aware if <code>true</code>\n     */",
        "    /** 启用后 put/evict 仅在事务成功提交后的 after-commit 阶段执行。 */\n"
        "    /**\n     * Defines if cache aware of Spring-managed transactions.\n"
        "     * If {@code true} put/evict operations are executed only for successful transaction in after-commit phase.\n"
        "     * <p>\n     * Default is <code>false</code>\n     *\n"
        "     * @param transactionAware cache is transaction aware if <code>true</code>\n     */",
    ),
    (
        "    /**\n     * Defines 'fixed' cache names. \n"
        "     * A new cache instance will not be created in dynamic for non-defined names.\n"
        "     * <p>\n     * `null` parameter setups dynamic mode \n     * \n     * @param names of caches\n     */",
        "    /** 预注册固定缓存名；传入非 {@code null} 集合后关闭动态模式。 */\n"
        "    /**\n     * Defines 'fixed' cache names. \n"
        "     * A new cache instance will not be created in dynamic for non-defined names.\n"
        "     * <p>\n     * `null` parameter setups dynamic mode \n     * \n     * @param names of caches\n     */",
    ),
    (
        "    @Override\n    public Cache getCache(String name) {",
        "    /** 按名称获取或（动态模式下）创建 {@link RedissonCache} 实例。 */\n"
        "    @Override\n"
        "    public Cache getCache(String name) {",
    ),
    (
        "        if (config.getMaxIdleTime() == 0 && config.getTTL() == 0 && config.getMaxSize() == 0) {",
        "        // 无过期与容量限制时使用普通 RMap，否则使用 RMapCache。\n"
        "        if (config.getMaxIdleTime() == 0 && config.getTTL() == 0 && config.getMaxSize() == 0) {",
    ),
    (
        "    private Cache createMap(String name, CacheConfig config) {",
        "    /** 基于 {@link RMap} 创建无 TTL 的 {@link RedissonCache}。 */\n"
        "    private Cache createMap(String name, CacheConfig config) {",
    ),
    (
        "    private Cache createMapCache(String name, CacheConfig config) {",
        "    /** 基于 {@link RMapCache} 创建带 TTL/淘汰策略的 {@link RedissonCache}。 */\n"
        "    private Cache createMapCache(String name, CacheConfig config) {",
    ),
    (
        "            map.setMaxSize(config.getMaxSize(), config.getEvictionMode());",
        "            // 首次创建时应用 maxSize 与 MapEntryListener。\n"
        "            map.setMaxSize(config.getMaxSize(), config.getEvictionMode());",
    ),
    (
        "    @Override\n    public void afterPropertiesSet() throws Exception {",
        "    /** 若配置了 {@code configLocation}，从 classpath 加载 YAML 缓存配置。 */\n"
        "    @Override\n"
        "    public void afterPropertiesSet() throws Exception {",
    ),
]

# --- spring-cache: Native MapCacheNative backend ---
W9B_REPLACEMENTS["RedissonSpringCacheNativeManager.java"] = [
    (
        "/**\n * A {@link CacheManager} implementation\n * backed by Redisson instance.\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 使用 Redisson {@link RMapCacheNative} 后端的 Spring {@link CacheManager}。\n"
        " * <p>继承 {@link RedissonSpringCacheManager}，底层 map 一律为 Native 实现；\n"
        " * 不支持 {@code maxIdleTime} 与 {@code maxSize}（启动时校验）。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "    private void validateProps() {",
        "    /** 校验各缓存配置：Native 模式不支持 maxIdleTime 与 maxSize。 */\n"
        "    private void validateProps() {",
    ),
    (
        "            if (value.getMaxIdleTime() > 0) {\n                throw new UnsupportedOperationException(\"maxIdleTime isn't supported\");",
        "            if (value.getMaxIdleTime() > 0) {\n"
        "                // MapCacheNative 无独立 max-idle 语义。\n"
        "                throw new UnsupportedOperationException(\"maxIdleTime isn't supported\");",
    ),
    (
        "    @Override\n    protected RMap<Object, Object> getMap(String name, CacheConfig config) {",
        "    /** 始终返回 {@link RMapCacheNative}（可选 Codec）。 */\n"
        "    @Override\n"
        "    protected RMap<Object, Object> getMap(String name, CacheConfig config) {",
    ),
    (
        "    @Override\n    protected RMapCache<Object, Object> getMapCache(String name, CacheConfig config) {",
        "    /** 将 Native map 包装为 {@link MapCacheNativeWrapper} 以适配 {@link RMapCache} API。 */\n"
        "    @Override\n"
        "    protected RMapCache<Object, Object> getMapCache(String name, CacheConfig config) {",
    ),
]

# --- spring-data-16: protocol convertors ---
W9B_REPLACEMENTS["BinaryConvertor.java"] = [
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

W9B_REPLACEMENTS["DataTypeConvertor.java"] = [
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

# --- spring-data-16: replay decoders ---
W9B_REPLACEMENTS["ObjectListReplayDecoder2.java"] = [
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

W9B_REPLACEMENTS["PropertiesDecoder.java"] = [
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

# --- spring-data-16: connection factory ---
W9B_REPLACEMENTS["RedissonConnectionFactory.java"] = [
    (
        "/**\n * Redisson based connection factory\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 基于 Redisson 的 Spring Data Redis {@link RedisConnectionFactory}。\n"
        " * <p>提供 {@link RedissonConnection} 与 Sentinel 模式下的 {@link RedissonSentinelConnection}；\n"
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
        "    /** 返回包装当前 {@link RedissonClient} 的 {@link RedissonConnection}。 */\n"
        "    @Override\n"
        "    public RedisConnection getConnection() {",
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

W9B_REPLACEMENTS["RedissonExceptionConverter.java"] = [
    (
        "/**\n * Converts Redisson exceptions to Spring compatible\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 将 Redisson 客户端异常映射为 Spring Data Redis {@link DataAccessException}。\n"
        " * <p>连接失败、超时与通用 Redis 错误分别对应不同 Spring 异常类型。\n"
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

W9B_REPLACEMENTS["RedissonSentinelConnection.java"] = [
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

W9B_REPLACEMENTS["RedissonSubscription.java"] = [
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

W9B_REPLACEMENTS["ScoredSortedListReplayDecoder.java"] = [
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

W9B_REPLACEMENTS["ScoredSortedSetReplayDecoder.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 有序集合响应解码为 {@link Set}{@code <}{@link Tuple}{@code >}（保持 Redis 返回顺序的 {@link LinkedHashSet}）。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "    @Override\n    public Set<Tuple> decode(List<Object> parts, State state) {",
        "    /** 成对解析 member/score 并加入 {@link LinkedHashSet}。 */\n"
        "    @Override\n"
        "    public Set<Tuple> decode(List<Object> parts, State state) {",
    ),
]

W9B_REPLACEMENTS["ScoredSortedSetReplayDecoderV2.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 单条 member/score 对解码为单个 {@link RedisZSetCommands.Tuple}（V2 接口）。\n"
        " * <p>用于仅返回一个元素的 ZSET 命令响应。\n"
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
