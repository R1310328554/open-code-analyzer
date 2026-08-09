"""Chinese annotation replacements for Redisson 4.7.0 wave-12a spring-data-20 reactive [0:15]."""
from __future__ import annotations

W12A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

_EMPTY_JDOC = "/**\n * \n * @author Nikita Koksharov\n *\n */"

# --- spring-data-20: reactive HyperLogLog ---
W12A_REPLACEMENTS["RedissonReactiveHyperLogLogCommands.java"] = [
    (
        _EMPTY_JDOC,
        "/**\n"
        " * Spring Data Redis 响应式 HyperLogLog 命令实现。\n"
        " * <p>封装 PFADD、PFCOUNT、PFMERGE，通过 {@link RedissonBaseReactive#write} 路由。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "    RedissonReactiveHyperLogLogCommands(CommandReactiveExecutor executorService) {",
        "    /** 注入响应式命令执行器。 */\n"
        "    RedissonReactiveHyperLogLogCommands(CommandReactiveExecutor executorService) {",
    ),
    (
        "    @Override\n    public Flux<NumericResponse<PfAddCommand, Long>> pfAdd(Publisher<PfAddCommand> commands) {",
        "    /** PFADD：向 HyperLogLog 追加一个或多个元素，返回内部寄存器变更数。 */\n"
        "    @Override\n"
        "    public Flux<NumericResponse<PfAddCommand, Long>> pfAdd(Publisher<PfAddCommand> commands) {",
    ),
    (
        "    @Override\n    public Flux<NumericResponse<PfCountCommand, Long>> pfCount(Publisher<PfCountCommand> commands) {",
        "    /** PFCOUNT：估算一个或多个 HLL key 的基数。 */\n"
        "    @Override\n"
        "    public Flux<NumericResponse<PfCountCommand, Long>> pfCount(Publisher<PfCountCommand> commands) {",
    ),
    (
        "    @Override\n    public Flux<BooleanResponse<PfMergeCommand>> pfMerge(Publisher<PfMergeCommand> commands) {",
        "    /** PFMERGE：将多个源 HLL 合并到目标 key。 */\n"
        "    @Override\n"
        "    public Flux<BooleanResponse<PfMergeCommand>> pfMerge(Publisher<PfMergeCommand> commands) {",
    ),
]

# --- spring-data-20: reactive Key commands ---
W12A_REPLACEMENTS["RedissonReactiveKeyCommands.java"] = [
    (
        _EMPTY_JDOC,
        "/**\n"
        " * Spring Data Redis 响应式 Key 命令实现。\n"
        " * <p>封装 EXISTS、TYPE、KEYS、RENAME、DEL、EXPIRE、TTL、MOVE 等通用 key 操作。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "    public RedissonReactiveKeyCommands(CommandReactiveExecutor executorService) {",
        "    /** 注入响应式命令执行器。 */\n"
        "    public RedissonReactiveKeyCommands(CommandReactiveExecutor executorService) {",
    ),
    (
        "    @Override\n    public Flux<BooleanResponse<KeyCommand>> exists(Publisher<KeyCommand> keys) {",
        "    /** EXISTS：判断 key 是否存在。 */\n"
        "    @Override\n"
        "    public Flux<BooleanResponse<KeyCommand>> exists(Publisher<KeyCommand> keys) {",
    ),
    (
        "    @Override\n    public Flux<CommandResponse<KeyCommand, DataType>> type(Publisher<KeyCommand> keys) {",
        "    /** TYPE：返回 key 的 {@link DataType}。 */\n"
        "    @Override\n"
        "    public Flux<CommandResponse<KeyCommand, DataType>> type(Publisher<KeyCommand> keys) {",
    ),
    (
        "    @Override\n    public Flux<MultiValueResponse<ByteBuffer, ByteBuffer>> keys(Publisher<ByteBuffer> patterns) {",
        "    /** KEYS：按模式匹配返回 key 列表（生产环境慎用）。 */\n"
        "    @Override\n"
        "    public Flux<MultiValueResponse<ByteBuffer, ByteBuffer>> keys(Publisher<ByteBuffer> patterns) {",
    ),
    (
        "    @Override\n    public Mono<ByteBuffer> randomKey() {",
        "    /** RANDOMKEY：随机返回一个 key。 */\n"
        "    @Override\n"
        "    public Mono<ByteBuffer> randomKey() {",
    ),
    (
        "    @Override\n    public Flux<BooleanResponse<RenameCommand>> rename(Publisher<RenameCommand> commands) {",
        "    /** RENAME：重命名 key。 */\n"
        "    @Override\n"
        "    public Flux<BooleanResponse<RenameCommand>> rename(Publisher<RenameCommand> commands) {",
    ),
    (
        "    @Override\n    public Flux<NumericResponse<KeyCommand, Long>> del(Publisher<KeyCommand> keys) {",
        "    /** DEL：删除单个 key 并返回删除数量。 */\n"
        "    @Override\n"
        "    public Flux<NumericResponse<KeyCommand, Long>> del(Publisher<KeyCommand> keys) {",
    ),
    (
        "    @Override\n    public Flux<NumericResponse<List<ByteBuffer>, Long>> mDel(Publisher<List<ByteBuffer>> keys) {",
        "    /** 批量 DEL：一次删除多个 key。 */\n"
        "    @Override\n"
        "    public Flux<NumericResponse<List<ByteBuffer>, Long>> mDel(Publisher<List<ByteBuffer>> keys) {",
    ),
    (
        "    @Override\n    public Flux<BooleanResponse<ExpireCommand>> expire(Publisher<ExpireCommand> commands) {",
        "    /** EXPIRE：以秒为单位设置 key 过期时间。 */\n"
        "    @Override\n"
        "    public Flux<BooleanResponse<ExpireCommand>> expire(Publisher<ExpireCommand> commands) {",
    ),
    (
        "    @Override\n    public Flux<NumericResponse<KeyCommand, Long>> ttl(Publisher<KeyCommand> commands) {",
        "    /** TTL：返回 key 剩余存活秒数。 */\n"
        "    @Override\n"
        "    public Flux<NumericResponse<KeyCommand, Long>> ttl(Publisher<KeyCommand> commands) {",
    ),
    (
        "    @Override\n    public Flux<BooleanResponse<MoveCommand>> move(Publisher<MoveCommand> commands) {",
        "    /** MOVE：将 key 迁移到指定数据库编号。 */\n"
        "    @Override\n"
        "    public Flux<BooleanResponse<MoveCommand>> move(Publisher<MoveCommand> commands) {",
    ),
]

# --- spring-data-20: reactive List commands ---
W12A_REPLACEMENTS["RedissonReactiveListCommands.java"] = [
    (
        _EMPTY_JDOC,
        "/**\n"
        " * Spring Data Redis 响应式 List 命令实现。\n"
        " * <p>封装 LPUSH/RPUSH、LRANGE、LTRIM、LINSERT、LPOP/RPOP、BLPOP/BRPOP 等列表操作。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "    RedissonReactiveListCommands(CommandReactiveExecutor executorService) {",
        "    /** 注入响应式命令执行器。 */\n"
        "    RedissonReactiveListCommands(CommandReactiveExecutor executorService) {",
    ),
    (
        "    @Override\n    public Flux<NumericResponse<PushCommand, Long>> push(Publisher<PushCommand> commands) {",
        "    /** LPUSH/RPUSH 或 LPUSHX/RPUSHX：按方向与 upsert 标志选择命令。 */\n"
        "    @Override\n"
        "    public Flux<NumericResponse<PushCommand, Long>> push(Publisher<PushCommand> commands) {",
    ),
    (
        "            if (!command.getUpsert() && command.getValues().size() > 1) {",
        "            // PUSHX 仅允许单个 value。\n"
        "            if (!command.getUpsert() && command.getValues().size() > 1) {",
    ),
    (
        "    @Override\n    public Flux<NumericResponse<KeyCommand, Long>> lLen(Publisher<KeyCommand> commands) {",
        "    /** LLEN：返回列表长度。 */\n"
        "    @Override\n"
        "    public Flux<NumericResponse<KeyCommand, Long>> lLen(Publisher<KeyCommand> commands) {",
    ),
    (
        "    @Override\n    public Flux<CommandResponse<RangeCommand, Flux<ByteBuffer>>> lRange(Publisher<RangeCommand> commands) {",
        "    /** LRANGE：按闭区间下标返回列表片段。 */\n"
        "    @Override\n"
        "    public Flux<CommandResponse<RangeCommand, Flux<ByteBuffer>>> lRange(Publisher<RangeCommand> commands) {",
    ),
    (
        "    @Override\n    public Flux<ByteBufferResponse<PopCommand>> pop(Publisher<PopCommand> commands) {",
        "    /** LPOP/RPOP：按 {@link Direction} 弹出列表头或尾元素。 */\n"
        "    @Override\n"
        "    public Flux<ByteBufferResponse<PopCommand>> pop(Publisher<PopCommand> commands) {",
    ),
    (
        "    @Override\n    public Flux<PopResponse> bPop(Publisher<BPopCommand> commands) {",
        "    /** BLPOP/BRPOP：阻塞式弹出，超时以秒计。 */\n"
        "    @Override\n"
        "    public Flux<PopResponse> bPop(Publisher<BPopCommand> commands) {",
    ),
    (
        "    @Override\n    public Flux<ByteBufferResponse<RPopLPushCommand>> rPopLPush(Publisher<RPopLPushCommand> commands) {",
        "    /** RPOPLPUSH：从源列表弹出并推入目标列表。 */\n"
        "    @Override\n"
        "    public Flux<ByteBufferResponse<RPopLPushCommand>> rPopLPush(Publisher<RPopLPushCommand> commands) {",
    ),
]

# --- spring-data-20: reactive Number commands ---
W12A_REPLACEMENTS["RedissonReactiveNumberCommands.java"] = [
    (
        _EMPTY_JDOC,
        "/**\n"
        " * Spring Data Redis 响应式数值命令实现。\n"
        " * <p>封装 INCR/DECR、INCRBYFLOAT 及 hash 字段 HINCRBYFLOAT。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "    public RedissonReactiveNumberCommands(CommandReactiveExecutor executorService) {",
        "    /** 注入响应式命令执行器。 */\n"
        "    public RedissonReactiveNumberCommands(CommandReactiveExecutor executorService) {",
    ),
    (
        "    @Override\n    public Flux<NumericResponse<KeyCommand, Long>> incr(Publisher<KeyCommand> keys) {",
        "    /** INCR：字符串 key 原子加一。 */\n"
        "    @Override\n"
        "    public Flux<NumericResponse<KeyCommand, Long>> incr(Publisher<KeyCommand> keys) {",
    ),
    (
        "    @Override\n    public <T extends Number> Flux<NumericResponse<IncrByCommand<T>, T>> incrBy(Publisher<IncrByCommand<T>> commands) {",
        "    /** INCRBYFLOAT：按浮点增量递增。 */\n"
        "    @Override\n"
        "    public <T extends Number> Flux<NumericResponse<IncrByCommand<T>, T>> incrBy(Publisher<IncrByCommand<T>> commands) {",
    ),
    (
        "    @Override\n    public Flux<NumericResponse<KeyCommand, Long>> decr(Publisher<KeyCommand> keys) {",
        "    /** DECR：字符串 key 原子减一。 */\n"
        "    @Override\n"
        "    public Flux<NumericResponse<KeyCommand, Long>> decr(Publisher<KeyCommand> keys) {",
    ),
    (
        "    @Override\n    public <T extends Number> Flux<NumericResponse<DecrByCommand<T>, T>> decrBy(Publisher<DecrByCommand<T>> commands) {",
        "    /** DECRBY：通过 INCRBYFLOAT 负增量实现递减。 */\n"
        "    @Override\n"
        "    public <T extends Number> Flux<NumericResponse<DecrByCommand<T>, T>> decrBy(Publisher<DecrByCommand<T>> commands) {",
    ),
    (
        "    @Override\n    public <T extends Number> Flux<NumericResponse<HIncrByCommand<T>, T>> hIncrBy(",
        "    /** HINCRBYFLOAT：hash 字段按浮点增量递增。 */\n"
        "    @Override\n"
        "    public <T extends Number> Flux<NumericResponse<HIncrByCommand<T>, T>> hIncrBy(",
    ),
]

# --- spring-data-20: reactive cluster connection ---
W12A_REPLACEMENTS["RedissonReactiveRedisClusterConnection.java"] = [
    (
        _EMPTY_JDOC,
        "/**\n"
        " * Spring Data Redis 集群模式响应式连接门面。\n"
        " * <p>继承 {@link RedissonReactiveRedisConnection} 并实现 {@link ReactiveRedisClusterConnection}；\n"
        "各 {@code *Commands()} 返回集群专用命令适配器。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "    public RedissonReactiveRedisClusterConnection(CommandReactiveExecutor executorService) {",
        "    /** 注入响应式命令执行器。 */\n"
        "    public RedissonReactiveRedisClusterConnection(CommandReactiveExecutor executorService) {",
    ),
    (
        "    @Override\n    public ReactiveClusterKeyCommands keyCommands() {",
        "    /** 返回集群 Key 命令适配器。 */\n"
        "    @Override\n"
        "    public ReactiveClusterKeyCommands keyCommands() {",
    ),
    (
        "    @Override\n    public ReactiveClusterServerCommands serverCommands() {",
        "    /** 返回集群 Server 命令适配器。 */\n"
        "    @Override\n"
        "    public ReactiveClusterServerCommands serverCommands() {",
    ),
    (
        "    @Override\n    public Mono<String> ping(RedisClusterNode node) {",
        "    /** 对指定集群节点执行 PING。 */\n"
        "    @Override\n"
        "    public Mono<String> ping(RedisClusterNode node) {",
    ),
]

# --- spring-data-20: reactive connection ---
W12A_REPLACEMENTS["RedissonReactiveRedisConnection.java"] = [
    (
        _EMPTY_JDOC,
        "/**\n"
        " * Spring Data Redis 单机模式响应式连接门面。\n"
        " * <p>实现 {@link ReactiveRedisConnection}，按数据类型委托各 {@code RedissonReactive*Commands}。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "    public RedissonReactiveRedisConnection(CommandReactiveExecutor executorService) {",
        "    /** 注入响应式命令执行器。 */\n"
        "    public RedissonReactiveRedisConnection(CommandReactiveExecutor executorService) {",
    ),
    (
        "    @Override\n    public ReactiveKeyCommands keyCommands() {",
        "    /** 返回 Key 命令实现。 */\n"
        "    @Override\n"
        "    public ReactiveKeyCommands keyCommands() {",
    ),
    (
        "    @Override\n    public ReactiveStringCommands stringCommands() {",
        "    /** 返回 String 命令实现。 */\n"
        "    @Override\n"
        "    public ReactiveStringCommands stringCommands() {",
    ),
    (
        "    @Override\n    public ReactiveScriptingCommands scriptingCommands() {",
        "    /** 返回 Lua 脚本命令实现。 */\n"
        "    @Override\n"
        "    public ReactiveScriptingCommands scriptingCommands() {",
    ),
    (
        "    @Override\n    public Mono<String> ping() {",
        "    /** PING：检测连接可用性。 */\n"
        "    @Override\n"
        "    public Mono<String> ping() {",
    ),
    (
        "    @Override\n    public void close() {",
        "    /** 响应式连接由工厂统一管理生命周期，此处为空实现。 */\n"
        "    @Override\n"
        "    public void close() {",
    ),
]

# --- spring-data-20: reactive Scripting commands ---
W12A_REPLACEMENTS["RedissonReactiveScriptingCommands.java"] = [
    (
        _EMPTY_JDOC,
        "/**\n"
        " * Spring Data Redis 响应式 Lua 脚本命令实现。\n"
        " * <p>封装 SCRIPT FLUSH/LOAD/EXISTS 及 EVAL/EVALSHA；\n"
        " {@link ReturnType} 映射为对应 {@link RedisCommand} 与解码器。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "    RedissonReactiveScriptingCommands(CommandReactiveExecutor executorService) {",
        "    /** 注入响应式命令执行器。 */\n"
        "    RedissonReactiveScriptingCommands(CommandReactiveExecutor executorService) {",
    ),
    (
        "    @Override\n    public Mono<String> scriptFlush() {",
        "    /** SCRIPT FLUSH：清空所有节点脚本缓存。 */\n"
        "    @Override\n"
        "    public Mono<String> scriptFlush() {",
    ),
    (
        "    @Override\n    public Mono<String> scriptKill() {",
        "    /** SCRIPT KILL：当前未实现。 */\n"
        "    @Override\n"
        "    public Mono<String> scriptKill() {",
    ),
    (
        "    @Override\n    public Mono<String> scriptLoad(ByteBuffer script) {",
        "    /** SCRIPT LOAD：向所有节点加载脚本并返回 SHA1。 */\n"
        "    @Override\n"
        "    public Mono<String> scriptLoad(ByteBuffer script) {",
    ),
    (
        "    protected RedisCommand<?> toCommand(ReturnType returnType, String name) {",
        "    /** 将 Spring {@link ReturnType} 映射为 Redisson {@link RedisCommand}。 */\n"
        "    protected RedisCommand<?> toCommand(ReturnType returnType, String name) {",
    ),
    (
        "        } else if (returnType == ReturnType.MULTI) {",
        "        // MULTI 返回列表，MULTI/VALUE 使用 BinaryConvertor 解码。\n"
        "        } else if (returnType == ReturnType.MULTI) {",
    ),
    (
        "    @Override\n    public <T> Flux<T> eval(ByteBuffer script, ReturnType returnType, int numKeys, ByteBuffer... keysAndArgs) {",
        "    /** EVAL：执行脚本并将 byte[]/List 转为 {@link ByteBuffer}。 */\n"
        "    @Override\n"
        "    public <T> Flux<T> eval(ByteBuffer script, ReturnType returnType, int numKeys, ByteBuffer... keysAndArgs) {",
    ),
    (
        "    protected <T> Flux<T> convert(Mono<T> m) {",
        "    /** 将脚本返回值中的 byte[] 与嵌套列表元素包装为 {@link ByteBuffer}。 */\n"
        "    protected <T> Flux<T> convert(Mono<T> m) {",
    ),
    (
        "    @Override\n    public <T> Flux<T> evalSha(String scriptSha, ReturnType returnType, int numKeys, ByteBuffer... keysAndArgs) {",
        "    /** EVALSHA：按 SHA1 执行已加载脚本。 */\n"
        "    @Override\n"
        "    public <T> Flux<T> evalSha(String scriptSha, ReturnType returnType, int numKeys, ByteBuffer... keysAndArgs) {",
    ),
]

# --- spring-data-20: reactive Server commands ---
W12A_REPLACEMENTS["RedissonReactiveServerCommands.java"] = [
    (
        _EMPTY_JDOC,
        "/**\n"
        " * Spring Data Redis 响应式 Server 命令实现。\n"
        " * <p>封装 BGSAVE、SAVE、FLUSHDB/FLUSHALL、INFO、CONFIG、TIME、CLIENT LIST 等管理命令。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "    RedissonReactiveServerCommands(CommandReactiveExecutor executorService) {",
        "    /** 注入响应式命令执行器。 */\n"
        "    RedissonReactiveServerCommands(CommandReactiveExecutor executorService) {",
    ),
    (
        "    @Override\n    public Mono<String> bgReWriteAof() {",
        "    /** BGREWRITEAOF：异步重写 AOF 文件。 */\n"
        "    @Override\n"
        "    public Mono<String> bgReWriteAof() {",
    ),
    (
        "    @Override\n    public Mono<String> bgSave() {",
        "    /** BGSAVE：后台触发 RDB 快照。 */\n"
        "    @Override\n"
        "    public Mono<String> bgSave() {",
    ),
    (
        "    @Override\n    public Mono<Long> dbSize() {",
        "    /** DBSIZE：汇总所有 master 节点的 key 数量。 */\n"
        "    @Override\n"
        "    public Mono<Long> dbSize() {",
    ),
    (
        "    @Override\n    public Mono<String> flushDb() {",
        "    /** FLUSHDB：清空所有节点当前数据库。 */\n"
        "    @Override\n"
        "    public Mono<String> flushDb() {",
    ),
    (
        "    @Override\n    public Mono<Properties> info() {",
        "    /** INFO DEFAULT：读取默认段服务器信息。 */\n"
        "    @Override\n"
        "    public Mono<Properties> info() {",
    ),
    (
        "    @Override\n    public Mono<Properties> getConfig(String pattern) {",
        "    /** CONFIG GET：按模式读取运行时配置。 */\n"
        "    @Override\n"
        "    public Mono<Properties> getConfig(String pattern) {",
    ),
    (
        "    @Override\n    public Mono<String> setClientName(String name) {",
        "    /** 客户端名称应通过 Redisson {@link Config} 配置，此处不支持。 */\n"
        "    @Override\n"
        "    public Mono<String> setClientName(String name) {",
    ),
    (
        "    @Override\n    public Flux<RedisClientInfo> getClientList() {",
        "    /** CLIENT LIST：解析为 {@link RedisClientInfo} 流。 */\n"
        "    @Override\n"
        "    public Flux<RedisClientInfo> getClientList() {",
    ),
]

# --- spring-data-20: reactive Set commands ---
W12A_REPLACEMENTS["RedissonReactiveSetCommands.java"] = [
    (
        _EMPTY_JDOC,
        "/**\n"
        " * Spring Data Redis 响应式 Set 命令实现。\n"
        " * <p>封装 SADD/SREM、SPOP、SINTER/SUNION/SDIFF 及 STORE 变体、SMEMBERS 等集合操作。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "    RedissonReactiveSetCommands(CommandReactiveExecutor executorService) {",
        "    /** 注入响应式命令执行器。 */\n"
        "    RedissonReactiveSetCommands(CommandReactiveExecutor executorService) {",
    ),
    (
        "    @Override\n    public Flux<NumericResponse<SAddCommand, Long>> sAdd(Publisher<SAddCommand> commands) {",
        "    /** SADD：向集合添加一个或多个 member。 */\n"
        "    @Override\n"
        "    public Flux<NumericResponse<SAddCommand, Long>> sAdd(Publisher<SAddCommand> commands) {",
    ),
    (
        "    @Override\n    public Flux<NumericResponse<SRemCommand, Long>> sRem(Publisher<SRemCommand> commands) {",
        "    /** SREM：从集合移除 member。 */\n"
        "    @Override\n"
        "    public Flux<NumericResponse<SRemCommand, Long>> sRem(Publisher<SRemCommand> commands) {",
    ),
    (
        "    @Override\n    public Flux<ByteBuffer> sPop(SPopCommand command) {",
        "    /** SPOP count：随机弹出多个 member。 */\n"
        "    @Override\n"
        "    public Flux<ByteBuffer> sPop(SPopCommand command) {",
    ),
    (
        "    @Override\n    public Flux<BooleanResponse<SMoveCommand>> sMove(Publisher<SMoveCommand> commands) {",
        "    /** SMOVE：将 member 从源集合移动到目标集合。 */\n"
        "    @Override\n"
        "    public Flux<BooleanResponse<SMoveCommand>> sMove(Publisher<SMoveCommand> commands) {",
    ),
    (
        "    @Override\n    public Flux<CommandResponse<SInterCommand, Flux<ByteBuffer>>> sInter(Publisher<SInterCommand> commands) {",
        "    /** SINTER：返回多个集合的交集 member 流。 */\n"
        "    @Override\n"
        "    public Flux<CommandResponse<SInterCommand, Flux<ByteBuffer>>> sInter(Publisher<SInterCommand> commands) {",
    ),
    (
        "    @Override\n    public Flux<CommandResponse<SUnionCommand, Flux<ByteBuffer>>> sUnion(Publisher<SUnionCommand> commands) {",
        "    /** SUNION：返回多个集合的并集 member 流。 */\n"
        "    @Override\n"
        "    public Flux<CommandResponse<SUnionCommand, Flux<ByteBuffer>>> sUnion(Publisher<SUnionCommand> commands) {",
    ),
    (
        "    @Override\n    public Flux<CommandResponse<SDiffCommand, Flux<ByteBuffer>>> sDiff(Publisher<SDiffCommand> commands) {",
        "    /** SDIFF：返回集合差集 member 流。 */\n"
        "    @Override\n"
        "    public Flux<CommandResponse<SDiffCommand, Flux<ByteBuffer>>> sDiff(Publisher<SDiffCommand> commands) {",
    ),
    (
        "    @Override\n    public Flux<CommandResponse<KeyCommand, Flux<ByteBuffer>>> sMembers(Publisher<KeyCommand> commands) {",
        "    /** SMEMBERS：返回集合全部 member。 */\n"
        "    @Override\n"
        "    public Flux<CommandResponse<KeyCommand, Flux<ByteBuffer>>> sMembers(Publisher<KeyCommand> commands) {",
    ),
]

# --- spring-data-20: reactive String commands ---
W12A_REPLACEMENTS["RedissonReactiveStringCommands.java"] = [
    (
        _EMPTY_JDOC,
        "/**\n"
        " * Spring Data Redis 响应式 String 命令实现。\n"
        " * <p>封装 SET/GET、MGET/MSET、APPEND、GETRANGE、BITCOUNT/BITOP 等字符串与位图操作。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "    RedissonReactiveStringCommands(CommandReactiveExecutor executorService) {",
        "    /** 注入响应式命令执行器。 */\n"
        "    RedissonReactiveStringCommands(CommandReactiveExecutor executorService) {",
    ),
    (
        "    @Override\n    public Flux<BooleanResponse<SetCommand>> set(Publisher<SetCommand> commands) {",
        "    /** SET：支持 PX/NX/XX 过期与条件选项组合。 */\n"
        "    @Override\n"
        "    public Flux<BooleanResponse<SetCommand>> set(Publisher<SetCommand> commands) {",
    ),
    (
        "    @Override\n    public Flux<ByteBufferResponse<KeyCommand>> get(Publisher<KeyCommand> keys) {",
        "    /** GET：读取字符串值，缺失时返回 {@link AbsentByteBufferResponse}。 */\n"
        "    @Override\n"
        "    public Flux<ByteBufferResponse<KeyCommand>> get(Publisher<KeyCommand> keys) {",
    ),
    (
        "    @Override\n    public Flux<MultiValueResponse<List<ByteBuffer>, ByteBuffer>> mGet(Publisher<List<ByteBuffer>> keysets) {",
        "    /** MGET：批量读取，null 槽位映射为空 {@link ByteBuffer}。 */\n"
        "    @Override\n"
        "    public Flux<MultiValueResponse<List<ByteBuffer>, ByteBuffer>> mGet(Publisher<List<ByteBuffer>> keysets) {",
    ),
    (
        "    @Override\n    public Flux<BooleanResponse<MSetCommand>> mSet(Publisher<MSetCommand> commands) {",
        "    /** MSET：批量写入 key-value 对。 */\n"
        "    @Override\n"
        "    public Flux<BooleanResponse<MSetCommand>> mSet(Publisher<MSetCommand> commands) {",
    ),
    (
        "    protected List<byte[]> convert(MSetCommand command) {",
        "    /** 将 MSetCommand 的键值对展开为 Redis 参数数组。 */\n"
        "    protected List<byte[]> convert(MSetCommand command) {",
    ),
    (
        "    @Override\n    public Flux<NumericResponse<AppendCommand, Long>> append(Publisher<AppendCommand> commands) {",
        "    /** APPEND：在字符串末尾追加内容。 */\n"
        "    @Override\n"
        "    public Flux<NumericResponse<AppendCommand, Long>> append(Publisher<AppendCommand> commands) {",
    ),
    (
        "    @Override\n    public Flux<BooleanResponse<SetBitCommand>> setBit(Publisher<SetBitCommand> commands) {",
        "    /** SETBIT：设置指定偏移处的位值。 */\n"
        "    @Override\n"
        "    public Flux<BooleanResponse<SetBitCommand>> setBit(Publisher<SetBitCommand> commands) {",
    ),
    (
        "    @Override\n    public Flux<NumericResponse<BitOpCommand, Long>> bitOp(Publisher<BitOpCommand> commands) {",
        "    /** BITOP：对多个字符串 key 执行 AND/OR/XOR/NOT 位运算。 */\n"
        "    @Override\n"
        "    public Flux<NumericResponse<BitOpCommand, Long>> bitOp(Publisher<BitOpCommand> commands) {",
    ),
    (
        "    @Override\n    public Flux<NumericResponse<KeyCommand, Long>> strLen(Publisher<KeyCommand> keys) {",
        "    /** STRLEN：返回字符串字节长度。 */\n"
        "    @Override\n"
        "    public Flux<NumericResponse<KeyCommand, Long>> strLen(Publisher<KeyCommand> keys) {",
    ),
]

# --- spring-data-20: Sentinel connection ---
W12A_REPLACEMENTS["RedissonSentinelConnection.java"] = [
    (
        _EMPTY_JDOC,
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
        "    private final RedisConnection connection;",
        "    /** 底层 Sentinel Redis 连接。 */\n"
        "    private final RedisConnection connection;",
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
        "    private static List<RedisServer> toRedisServersList(List<Map<String, String>> source) {",
        "    /** 将 Sentinel 返回的 map 列表转为 {@link RedisServer} 集合。 */\n"
        "    private static List<RedisServer> toRedisServersList(List<Map<String, String>> source) {",
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
        "    /** SENTINEL REMOVE：取消对 master 的监控。 */\n"
        "    @Override\n"
        "    public void remove(NamedNode master) {",
    ),
    (
        "    @Override\n    public void monitor(RedisServer master) {",
        "    /** 向 Sentinel 注册新的 master 监控（host/port/quorum）。 */\n"
        "    @Override\n"
        "    public void monitor(RedisServer master) {",
    ),
    (
        "    @Override\n    public void close() throws IOException {",
        "    /** 异步关闭底层连接。 */\n"
        "    @Override\n"
        "    public void close() throws IOException {",
    ),
]

# --- spring-data-20: Pub/Sub subscription ---
W12A_REPLACEMENTS["RedissonSubscription.java"] = [
    (
        _EMPTY_JDOC,
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
        "    private final CommandAsyncExecutor commandExecutor;",
        "    /** 异步 Redis 命令执行器。 */\n"
        "    private final CommandAsyncExecutor commandExecutor;",
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
        "    @Override\n    protected void doUnsubscribe(boolean all, byte[]... channels) {",
        "    /** 取消指定频道订阅。 */\n"
        "    @Override\n"
        "    protected void doUnsubscribe(boolean all, byte[]... channels) {",
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

# --- spring-data-20: ZSET replay decoders ---
W12A_REPLACEMENTS["ScoredSortedListReplayDecoder.java"] = [
    (
        _EMPTY_JDOC,
        "/**\n"
        " * 有序集合批量响应解码器：member/score 交替排列，产出 {@link List}{@code <}{@link Tuple}{@code >}。\n"
        " * <p>奇数下标参数经 {@link DoubleCodec} 解析为 score。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "        if (paramNum % 2 != 0) {",
        "        // 奇数下标为 score，使用 DoubleCodec。\n"
        "        if (paramNum % 2 != 0) {",
    ),
    (
        "    @Override\n    public List<Tuple> decode(List<Object> parts, State state) {",
        "    /** 每两个元素组装为一个 {@link DefaultTuple}（member 字节数组 + score）。 */\n"
        "    @Override\n"
        "    public List<Tuple> decode(List<Object> parts, State state) {",
    ),
]

W12A_REPLACEMENTS["ScoredSortedSetReplayDecoder.java"] = [
    (
        _EMPTY_JDOC,
        "/**\n"
        " * 有序集合响应解码为 {@link Set}{@code <}{@link Tuple}{@code >}，以 {@link LinkedHashSet} 保留 Redis 返回顺序。\n"
        " * <p>奇数下标参数经 {@link DoubleCodec} 解析 score。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "        if (paramNum % 2 != 0) {",
        "        // 奇数下标为 score，使用 DoubleCodec。\n"
        "        if (paramNum % 2 != 0) {",
    ),
    (
        "    @Override\n    public Set<Tuple> decode(List<Object> parts, State state) {",
        "    /** 成对解析 member/score 并加入 {@link LinkedHashSet}。 */\n"
        "    @Override\n"
        "    public Set<Tuple> decode(List<Object> parts, State state) {",
    ),
]

W12A_REPLACEMENTS["ScoredSortedSetReplayDecoderV2.java"] = [
    (
        _EMPTY_JDOC,
        "/**\n"
        " * 单条 member/score 对解码为 {@link RedisZSetCommands.Tuple}（V2 接口）。\n"
        " * <p>适用于仅含一对元素的 ZSET 命令响应；奇数下标以 {@link DoubleCodec} 解析 score。\n"
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
