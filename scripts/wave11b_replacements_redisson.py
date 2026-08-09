"""Chinese annotation replacements for Redisson 4.7.0 wave-11b spring-data-20 reactive [15:30]."""
from __future__ import annotations

W11B_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

_EMPTY_JDOC = "/**\n * \n * @author Nikita Koksharov\n *\n */"

# --- spring-data-20: reactive base ---
W11B_REPLACEMENTS["RedissonBaseReactive.java"] = [
    (
        _EMPTY_JDOC,
        "/**\n"
        " * Spring Data Redis 响应式命令实现的公共基类。\n"
        " * <p>封装 {@link CommandReactiveExecutor} 上的读写调用、集群节点路由与\n"
        " {@link ByteBuffer} 工具方法，供各 {@code RedissonReactive*}Commands 复用。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "    final CommandReactiveExecutor executorService;",
        "    /** 响应式 Redis 命令执行器。 */\n"
        "    final CommandReactiveExecutor executorService;",
    ),
    (
        "    RedissonBaseReactive(CommandReactiveExecutor executorService) {",
        "    /** 注入 Redisson 响应式命令执行器。 */\n"
        "    RedissonBaseReactive(CommandReactiveExecutor executorService) {",
    ),
    (
        "    public static byte[] toByteArray(ByteBuffer buffer) {",
        "    /** 将 {@link ByteBuffer} 拷贝为 byte[]，不改变原 buffer 的 position。 */\n"
        "    public static byte[] toByteArray(ByteBuffer buffer) {",
    ),
    (
        "    RFuture<String> toStringFuture(RFuture<Void> f) {",
        "    /** 将 {@code Void} 型 Future 映射为固定 {@code \"OK\"} 字符串响应。 */\n"
        "    RFuture<String> toStringFuture(RFuture<Void> f) {",
    ),
    (
        "    <T> Mono<T> execute(RedisClusterNode node, RedisCommand<T> command, Object... params) {",
        "    /** 在指定集群节点上执行写命令并包装为 {@link Mono}。 */\n"
        "    <T> Mono<T> execute(RedisClusterNode node, RedisCommand<T> command, Object... params) {",
    ),
    (
        "    protected RedisClient getEntry(RedisClusterNode node) {",
        "    /** 根据 {@link RedisClusterNode} 地址解析底层 {@link RedisClient} 连接。 */\n"
        "    protected RedisClient getEntry(RedisClusterNode node) {",
    ),
    (
        "    <V, T> Flux<T> execute(Publisher<V> commands, Function<V, Publisher<T>> mapper) {",
        "    /** 将 Publisher 命令流按序 {@code concatMap} 展开为响应 {@link Flux}。 */\n"
        "    <V, T> Flux<T> execute(Publisher<V> commands, Function<V, Publisher<T>> mapper) {",
    ),
    (
        "    <T> Mono<T> write(byte[] key, Codec codec, RedisCommand<?> command, Object... params) {",
        "    /** 按 key 路由写命令；异常映射为 {@link RedisSystemException}。 */\n"
        "    <T> Mono<T> write(byte[] key, Codec codec, RedisCommand<?> command, Object... params) {",
    ),
    (
        "    <T> Mono<T> read(byte[] key, Codec codec, RedisCommand<?> command, Object... params) {",
        "    /** 按 key 路由读命令；异常映射为 {@link RedisSystemException}。 */\n"
        "    <T> Mono<T> read(byte[] key, Codec codec, RedisCommand<?> command, Object... params) {",
    ),
]

# --- spring-data-20: connection factory (reactive-aware) ---
W11B_REPLACEMENTS["RedissonConnectionFactory.java"] = [
    (
        "/**\n * Redisson based connection factory\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 基于 Redisson 的 Spring Data Redis 连接工厂。\n"
        " * <p>同时实现阻塞式 {@link RedisConnectionFactory} 与响应式\n"
        " {@link ReactiveRedisConnectionFactory}；集群/Sentinel 模式按配置返回对应连接类型；\n"
        "异常经 {@link RedissonExceptionConverter} 翻译为 Spring {@link DataAccessException}。\n"
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
        "    @Override\n    public RedisSentinelConnection getSentinelConnection() {",
        "    /** 遍历 Sentinel 节点 PING，返回首个可用的 {@link RedissonSentinelConnection}。 */\n"
        "    @Override\n"
        "    public RedisSentinelConnection getSentinelConnection() {",
    ),
    (
        "                if (\"pong\".equalsIgnoreCase(res)) {",
        "                // 首个响应 PONG 的 Sentinel 用于 Spring Data 管理命令。\n"
        "                if (\"pong\".equalsIgnoreCase(res)) {",
    ),
    (
        "    @Override\n    public ReactiveRedisConnection getReactiveConnection() {",
        "    /** 返回单机或集群响应式 Redis 连接。 */\n"
        "    @Override\n"
        "    public ReactiveRedisConnection getReactiveConnection() {",
    ),
    (
        "    @Override\n    public ReactiveRedisClusterConnection getReactiveClusterConnection() {",
        "    /** 非集群模式调用时抛出 {@link InvalidDataAccessResourceUsageException}。 */\n"
        "    @Override\n"
        "    public ReactiveRedisClusterConnection getReactiveClusterConnection() {",
    ),
]

W11B_REPLACEMENTS["RedissonExceptionConverter.java"] = [
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

# --- spring-data-20: cluster reactive command adapters ---
_CLUSTER_ADAPTER: dict[str, tuple[str, str, str, bool]] = {
    "RedissonReactiveClusterGeoCommands.java": (
        "ReactiveClusterGeoCommands",
        "RedissonReactiveGeoCommands",
        "GEO",
        False,
    ),
    "RedissonReactiveClusterHashCommands.java": (
        "ReactiveClusterHashCommands",
        "RedissonReactiveHashCommands",
        "Hash",
        False,
    ),
    "RedissonReactiveClusterHyperLogLogCommands.java": (
        "ReactiveClusterHyperLogLogCommands",
        "RedissonReactiveHyperLogLogCommands",
        "HyperLogLog",
        False,
    ),
    "RedissonReactiveClusterListCommands.java": (
        "ReactiveClusterListCommands",
        "RedissonReactiveListCommands",
        "List",
        False,
    ),
    "RedissonReactiveClusterNumberCommands.java": (
        "ReactiveClusterNumberCommands",
        "RedissonReactiveNumberCommands",
        "数值",
        True,
    ),
    "RedissonReactiveClusterSetCommands.java": (
        "ReactiveClusterSetCommands",
        "RedissonReactiveSetCommands",
        "Set",
        False,
    ),
    "RedissonReactiveClusterStringCommands.java": (
        "ReactiveClusterStringCommands",
        "RedissonReactiveStringCommands",
        "String",
        False,
    ),
    "RedissonReactiveClusterZSetCommands.java": (
        "ReactiveClusterZSetCommands",
        "RedissonReactiveZSetCommands",
        "ZSet",
        False,
    ),
}

for _fname, (_iface, _base, _kind, _public_ctor) in _CLUSTER_ADAPTER.items():
    _cls = _fname.replace(".java", "")
    _vis = "public " if _public_ctor else ""
    W11B_REPLACEMENTS[_fname] = [
        (
            _EMPTY_JDOC,
            "/**\n"
            " * 集群模式下 Spring Data Redis 响应式 " + _kind + " 命令适配器。\n"
            " * <p>继承 {@link " + _base + "} 并实现 {@link " + _iface + "}，\n"
            "在集群拓扑下复用单机响应式命令实现。\n"
            " *\n"
            " * @author Nikita Koksharov\n"
            " *\n"
            " */",
        ),
        (
            "    " + _vis + _cls + "(CommandReactiveExecutor executorService) {",
            "    /** 注入响应式命令执行器。 */\n"
            "    " + _vis + _cls + "(CommandReactiveExecutor executorService) {",
        ),
    ]

# --- spring-data-20: cluster key commands (cross-slot rename) ---
W11B_REPLACEMENTS["RedissonReactiveClusterKeyCommands.java"] = [
    (
        "/**\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 集群模式下 Spring Data Redis 响应式 Key 命令实现。\n"
        " * <p>继承 {@link RedissonReactiveKeyCommands} 并实现 {@link ReactiveClusterKeyCommands}；\n"
        "跨 slot 的 RENAME 通过 DUMP/RESTORE 与 TTL 迁移完成。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "    public RedissonReactiveClusterKeyCommands(CommandReactiveExecutor executorService) {",
        "    /** 注入响应式命令执行器。 */\n"
        "    public RedissonReactiveClusterKeyCommands(CommandReactiveExecutor executorService) {",
    ),
    (
        "    @Override\n    public Mono<List<ByteBuffer>> keys(RedisClusterNode node, ByteBuffer pattern) {",
        "    /** 在指定集群节点上执行 KEYS 并合并各 master 结果。 */\n"
        "    @Override\n"
        "    public Mono<List<ByteBuffer>> keys(RedisClusterNode node, ByteBuffer pattern) {",
    ),
    (
        "    @Override\n    public Mono<ByteBuffer> randomKey(RedisClusterNode node) {",
        "    /** 在指定节点上执行 RANDOMKEY。 */\n"
        "    @Override\n"
        "    public Mono<ByteBuffer> randomKey(RedisClusterNode node) {",
    ),
    (
        "    @Override\n    public Flux<BooleanResponse<RenameCommand>> rename(Publisher<RenameCommand> commands) {",
        "    /** 同 slot 走父类 RENAME；跨 slot 时 DUMP+RESTORE 后删除旧 key。 */\n"
        "    @Override\n"
        "    public Flux<BooleanResponse<RenameCommand>> rename(Publisher<RenameCommand> commands) {",
    ),
    (
        "            if (executorService.getConnectionManager().calcSlot(keyBuf) == executorService.getConnectionManager().calcSlot(newKeyBuf)) {",
        "            // 源 key 与目标 key 在同一 hash slot，可直接 RENAME。\n"
        "            if (executorService.getConnectionManager().calcSlot(keyBuf) == executorService.getConnectionManager().calcSlot(newKeyBuf)) {",
    ),
    (
        "    @Override\n    public Flux<ReactiveRedisConnection.BooleanResponse<RenameCommand>> renameNX(Publisher<RenameCommand> commands) {",
        "    /** 跨 slot 时仅当新 key 不存在且 DUMP 成功才 RESTORE 并删除旧 key。 */\n"
        "    @Override\n"
        "    public Flux<ReactiveRedisConnection.BooleanResponse<RenameCommand>> renameNX(Publisher<RenameCommand> commands) {",
    ),
]

# --- spring-data-20: cluster server commands ---
W11B_REPLACEMENTS["RedissonReactiveClusterServerCommands.java"] = [
    (
        _EMPTY_JDOC,
        "/**\n"
        " * 集群模式下 Spring Data Redis 响应式 Server 命令实现。\n"
        " * <p>继承 {@link RedissonReactiveServerCommands} 并实现 {@link ReactiveClusterServerCommands}；\n"
        "支持对单个 {@link RedisClusterNode} 执行 BGSAVE、INFO、CONFIG 等管理命令。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "    RedissonReactiveClusterServerCommands(CommandReactiveExecutor executorService) {",
        "    /** 注入响应式命令执行器。 */\n"
        "    RedissonReactiveClusterServerCommands(CommandReactiveExecutor executorService) {",
    ),
    (
        "    @Override\n    public Mono<String> bgReWriteAof(RedisClusterNode node) {",
        "    /** 在指定节点触发 BGREWRITEAOF。 */\n"
        "    @Override\n"
        "    public Mono<String> bgReWriteAof(RedisClusterNode node) {",
    ),
    (
        "    @Override\n    public Mono<String> flushDb(RedisClusterNode node) {",
        "    /** 在指定节点执行 FLUSHDB。 */\n"
        "    @Override\n"
        "    public Mono<String> flushDb(RedisClusterNode node) {",
    ),
    (
        "    @Override\n    public Mono<String> flushAll(RedisClusterNode node) {",
        "    /** 在指定节点执行 FLUSHALL。 */\n"
        "    @Override\n"
        "    public Mono<String> flushAll(RedisClusterNode node) {",
    ),
    (
        "    @Override\n    public Mono<Properties> info(RedisClusterNode node) {",
        "    /** 读取指定节点的 INFO 默认段。 */\n"
        "    @Override\n"
        "    public Mono<Properties> info(RedisClusterNode node) {",
    ),
    (
        "    @Override\n    public Flux<RedisClientInfo> getClientList(RedisClusterNode node) {",
        "    /** 获取指定节点的 CLIENT LIST 并转换为 {@link RedisClientInfo} 流。 */\n"
        "    @Override\n"
        "    public Flux<RedisClientInfo> getClientList(RedisClusterNode node) {",
    ),
]

# --- spring-data-20: reactive GEO commands ---
W11B_REPLACEMENTS["RedissonReactiveGeoCommands.java"] = [
    (
        _EMPTY_JDOC,
        "/**\n"
        " * Spring Data Redis 响应式 GEO 命令实现。\n"
        " * <p>封装 GEOADD、GEODIST、GEOHASH、GEOPOS、GEORADIUS 等命令，\n"
        "通过 {@link RedissonBaseReactive#write} / {@link RedissonBaseReactive#read} 路由。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "    RedissonReactiveGeoCommands(CommandReactiveExecutor executorService) {",
        "    /** 注入响应式命令执行器。 */\n"
        "    RedissonReactiveGeoCommands(CommandReactiveExecutor executorService) {",
    ),
    (
        "    @Override\n    public Flux<NumericResponse<GeoAddCommand, Long>> geoAdd(Publisher<GeoAddCommand> commands) {",
        "    /** 批量 GEOADD：经度、纬度与 member 依次写入命令参数。 */\n"
        "    @Override\n"
        "    public Flux<NumericResponse<GeoAddCommand, Long>> geoAdd(Publisher<GeoAddCommand> commands) {",
    ),
    (
        "    @Override\n    public Flux<CommandResponse<GeoDistCommand, Distance>> geoDist(Publisher<GeoDistCommand> commands) {",
        "    /** GEODIST：可选 {@link Metric} 经 {@link DistanceConvertor} 解码。 */\n"
        "    @Override\n"
        "    public Flux<CommandResponse<GeoDistCommand, Distance>> geoDist(Publisher<GeoDistCommand> commands) {",
    ),
    (
        "    @Override\n    public Flux<MultiValueResponse<GeoHashCommand, String>> geoHash(Publisher<GeoHashCommand> commands) {",
        "    /** GEOHASH：返回各 member 的 geohash 字符串列表。 */\n"
        "    @Override\n"
        "    public Flux<MultiValueResponse<GeoHashCommand, String>> geoHash(Publisher<GeoHashCommand> commands) {",
    ),
    (
        "    @Override\n    public Flux<MultiValueResponse<GeoPosCommand, Point>> geoPos(Publisher<GeoPosCommand> commands) {",
        "    /** GEOPOS：经 {@code geoDecoder} 解码经纬度 {@link Point}。 */\n"
        "    @Override\n"
        "    public Flux<MultiValueResponse<GeoPosCommand, Point>> geoPos(Publisher<GeoPosCommand> commands) {",
    ),
    (
        "            if (args.getFlags().contains(GeoRadiusCommandArgs.Flag.WITHCOORD)) {",
        "            // WITHCOORD：返回 member 与坐标。\n"
        "            if (args.getFlags().contains(GeoRadiusCommandArgs.Flag.WITHCOORD)) {",
    ),
    (
        "                cmd = new RedisCommand<GeoResults<GeoLocation<byte[]>>>(\"GEORADIUS_RO\", distanceDecoder);",
        "                // WITHDIST：返回 member 与距离。\n"
        "                cmd = new RedisCommand<GeoResults<GeoLocation<byte[]>>>(\"GEORADIUS_RO\", distanceDecoder);",
    ),
    (
        "    @Override\n    public Flux<CommandResponse<GeoRadiusByMemberCommand, Flux<GeoResult<GeoLocation<ByteBuffer>>>>> geoRadiusByMember(",
        "    /** GEORADIUSBYMEMBER_RO：以 member 为圆心按半径查询。 */\n"
        "    @Override\n"
        "    public Flux<CommandResponse<GeoRadiusByMemberCommand, Flux<GeoResult<GeoLocation<ByteBuffer>>>>> geoRadiusByMember(",
    ),
]

# --- spring-data-20: reactive Hash commands ---
W11B_REPLACEMENTS["RedissonReactiveHashCommands.java"] = [
    (
        _EMPTY_JDOC,
        "/**\n"
        " * Spring Data Redis 响应式 Hash 命令实现。\n"
        " * <p>封装 HSET/HMSET、HMGET、HEXISTS、HDEL、HLEN、HKEYS、HVALS、HGETALL 等命令。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "    RedissonReactiveHashCommands(CommandReactiveExecutor executorService) {",
        "    /** 注入响应式命令执行器。 */\n"
        "    RedissonReactiveHashCommands(CommandReactiveExecutor executorService) {",
    ),
    (
        "    @Override\n    public Flux<BooleanResponse<HSetCommand>> hSet(Publisher<HSetCommand> commands) {",
        "    /** 单字段走 HSET/HSETNX；多字段走 HMSET。 */\n"
        "    @Override\n"
        "    public Flux<BooleanResponse<HSetCommand>> hSet(Publisher<HSetCommand> commands) {",
    ),
    (
        "            if (command.getFieldValueMap().size() == 1) {",
        "            // 单 field-value 对使用 HSET 或 HSETNX。\n"
        "            if (command.getFieldValueMap().size() == 1) {",
    ),
    (
        "    @Override\n    public Flux<MultiValueResponse<HGetCommand, ByteBuffer>> hMGet(Publisher<HGetCommand> commands) {",
        "    /** HMGET：过滤 null 后返回与请求 fields 对齐的值列表。 */\n"
        "    @Override\n"
        "    public Flux<MultiValueResponse<HGetCommand, ByteBuffer>> hMGet(Publisher<HGetCommand> commands) {",
    ),
    (
        "    @Override\n    public Flux<BooleanResponse<HExistsCommand>> hExists(Publisher<HExistsCommand> commands) {",
        "    /** HEXISTS：判断 hash 字段是否存在。 */\n"
        "    @Override\n"
        "    public Flux<BooleanResponse<HExistsCommand>> hExists(Publisher<HExistsCommand> commands) {",
    ),
    (
        "    @Override\n    public Flux<NumericResponse<HDelCommand, Long>> hDel(Publisher<HDelCommand> commands) {",
        "    /** HDEL：删除一个或多个 hash 字段。 */\n"
        "    @Override\n"
        "    public Flux<NumericResponse<HDelCommand, Long>> hDel(Publisher<HDelCommand> commands) {",
    ),
    (
        "    @Override\n    public Flux<CommandResponse<KeyCommand, Flux<Entry<ByteBuffer, ByteBuffer>>>> hGetAll(",
        "    /** HGETALL：返回 hash 全部 field-value 条目流。 */\n"
        "    @Override\n"
        "    public Flux<CommandResponse<KeyCommand, Flux<Entry<ByteBuffer, ByteBuffer>>>> hGetAll(",
    ),
]
