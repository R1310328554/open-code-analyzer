"""Chinese annotation replacements for Redisson 4.7.0 wave-13b spring-data-21 [15:30]."""
from __future__ import annotations

import importlib.util
from pathlib import Path

SCRIPTS = Path(__file__).resolve().parent


def _load(module_file: str, attr: str) -> dict[str, list[tuple[str, str]]]:
    spec = importlib.util.spec_from_file_location(module_file, SCRIPTS / module_file)
    mod = importlib.util.module_from_spec(spec)
    assert spec.loader is not None
    spec.loader.exec_module(mod)
    return getattr(mod, attr)


W11A = _load("wave11a_replacements_redisson.py", "W11A_REPLACEMENTS")
W12A = _load("wave12a_replacements_redisson.py", "W12A_REPLACEMENTS")

W13B_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

_EMPTY_JDOC = "/**\n * \n * @author Nikita Koksharov\n *\n */"

# --- reuse wave-12a / wave-11a (spring-data-21 identical to spring-data-20) ---
for _key in (
    "RedissonReactiveNumberCommands.java",
    "RedissonReactiveRedisClusterConnection.java",
    "RedissonReactiveScriptingCommands.java",
    "RedissonReactiveServerCommands.java",
    "RedissonSentinelConnection.java",
    "ScoredSortedListReplayDecoder.java",
    "ScoredSortedSetReplayDecoder.java",
    "ScoredSortedSetReplayDecoderV2.java",
):
    W13B_REPLACEMENTS[_key] = W12A[_key]

for _key in ("SecondsConvertor.java", "SetReplayDecoder.java"):
    W13B_REPLACEMENTS[_key] = W11A[_key]

# --- spring-data-21: reactive Pub/Sub commands ---
W13B_REPLACEMENTS["RedissonReactivePubSubCommands.java"] = [
    (
        _EMPTY_JDOC,
        "/**\n"
        " * Spring Data Redis 响应式 Pub/Sub 命令实现。\n"
        " * <p>通过 {@link #createSubscription()} 创建 {@link RedissonReactiveSubscription} 管理订阅；\n"
        " {@link #publish} 向频道发布消息。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "    RedissonReactivePubSubCommands(CommandReactiveExecutor executorService) {",
        "    /** 注入响应式命令执行器。 */\n"
        "    RedissonReactivePubSubCommands(CommandReactiveExecutor executorService) {",
    ),
    (
        "    @Override\n    public Mono<ReactiveSubscription> createSubscription() {",
        "    /** 创建响应式订阅对象，由 {@link RedissonReactiveSubscription} 承载频道/模式订阅。 */\n"
        "    @Override\n"
        "    public Mono<ReactiveSubscription> createSubscription() {",
    ),
    (
        "    @Override\n    public Flux<Long> publish(Publisher<ChannelMessage<ByteBuffer, ByteBuffer>> messageStream) {",
        "    /** PUBLISH：向指定频道发布消息，返回接收订阅者数量。 */\n"
        "    @Override\n"
        "    public Flux<Long> publish(Publisher<ChannelMessage<ByteBuffer, ByteBuffer>> messageStream) {",
    ),
    (
        "    @Override\n    public Mono<Void> subscribe(ByteBuffer... channels) {",
        "    /** 直接订阅未实现，须通过 {@link #createSubscription()} 获取订阅对象后操作。 */\n"
        "    @Override\n"
        "    public Mono<Void> subscribe(ByteBuffer... channels) {",
    ),
    (
        "    @Override\n    public Mono<Void> pSubscribe(ByteBuffer... patterns) {",
        "    /** 直接模式订阅未实现，须通过 {@link #createSubscription()} 获取订阅对象后操作。 */\n"
        "    @Override\n"
        "    public Mono<Void> pSubscribe(ByteBuffer... patterns) {",
    ),
]

# --- spring-data-21: reactive connection (closeLater + pubSubCommands) ---
W13B_REPLACEMENTS["RedissonReactiveRedisConnection.java"] = [
    (
        _EMPTY_JDOC,
        "/**\n"
        " * Spring Data Redis 单机模式响应式连接门面。\n"
        " * <p>实现 {@link ReactiveRedisConnection}，按数据类型委托各 {@code RedissonReactive*Commands}；\n"
        "生命周期由工厂统一管理，{@link #closeLater()} 为空操作。\n"
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
        "    @Override\n    public Mono<Void> closeLater() {",
        "    /** 延迟关闭为空实现，连接由 {@link RedissonConnectionFactory} 统一管理。 */\n"
        "    @Override\n"
        "    public Mono<Void> closeLater() {",
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
        "    @Override\n    public ReactiveNumberCommands numberCommands() {",
        "    /** 返回数值命令实现。 */\n"
        "    @Override\n"
        "    public ReactiveNumberCommands numberCommands() {",
    ),
    (
        "    @Override\n    public ReactiveSetCommands setCommands() {",
        "    /** 返回 Set 命令实现。 */\n"
        "    @Override\n"
        "    public ReactiveSetCommands setCommands() {",
    ),
    (
        "    @Override\n    public ReactivePubSubCommands pubSubCommands() {",
        "    /** 返回 Pub/Sub 命令实现。 */\n"
        "    @Override\n"
        "    public ReactivePubSubCommands pubSubCommands() {",
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
]

# --- spring-data-21: reactive Set commands (extended API) ---
W13B_REPLACEMENTS["RedissonReactiveSetCommands.java"] = [
    (
        _EMPTY_JDOC,
        "/**\n"
        " * Spring Data Redis 响应式 Set 命令实现。\n"
        " * <p>封装 SADD/SREM、SPOP、SCARD/SISMEMBER、SINTER/SUNION/SDIFF 及 STORE 变体、\n"
        "SSCAN、SRANDMEMBER、SMEMBERS 等集合操作。\n"
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
        "    @Override\n    public Flux<ByteBufferResponse<KeyCommand>> sPop(Publisher<KeyCommand> commands) {",
        "    /** SPOP：随机弹出一个 member。 */\n"
        "    @Override\n"
        "    public Flux<ByteBufferResponse<KeyCommand>> sPop(Publisher<KeyCommand> commands) {",
    ),
    (
        "    @Override\n    public Flux<BooleanResponse<SMoveCommand>> sMove(Publisher<SMoveCommand> commands) {",
        "    /** SMOVE：将 member 从源集合移动到目标集合。 */\n"
        "    @Override\n"
        "    public Flux<BooleanResponse<SMoveCommand>> sMove(Publisher<SMoveCommand> commands) {",
    ),
    (
        "    @Override\n    public Flux<NumericResponse<KeyCommand, Long>> sCard(Publisher<KeyCommand> commands) {",
        "    /** SCARD：返回集合元素个数。 */\n"
        "    @Override\n"
        "    public Flux<NumericResponse<KeyCommand, Long>> sCard(Publisher<KeyCommand> commands) {",
    ),
    (
        "    @Override\n    public Flux<BooleanResponse<SIsMemberCommand>> sIsMember(Publisher<SIsMemberCommand> commands) {",
        "    /** SISMEMBER：判断 member 是否在集合中。 */\n"
        "    @Override\n"
        "    public Flux<BooleanResponse<SIsMemberCommand>> sIsMember(Publisher<SIsMemberCommand> commands) {",
    ),
    (
        "    @Override\n    public Flux<CommandResponse<SInterCommand, Flux<ByteBuffer>>> sInter(Publisher<SInterCommand> commands) {",
        "    /** SINTER：返回多个集合的交集 member 流。 */\n"
        "    @Override\n"
        "    public Flux<CommandResponse<SInterCommand, Flux<ByteBuffer>>> sInter(Publisher<SInterCommand> commands) {",
    ),
    (
        "    @Override\n    public Flux<NumericResponse<SInterStoreCommand, Long>> sInterStore(Publisher<SInterStoreCommand> commands) {",
        "    /** SINTERSTORE：将交集写入目标 key 并返回元素个数。 */\n"
        "    @Override\n"
        "    public Flux<NumericResponse<SInterStoreCommand, Long>> sInterStore(Publisher<SInterStoreCommand> commands) {",
    ),
    (
        "    @Override\n    public Flux<CommandResponse<SUnionCommand, Flux<ByteBuffer>>> sUnion(Publisher<SUnionCommand> commands) {",
        "    /** SUNION：返回多个集合的并集 member 流。 */\n"
        "    @Override\n"
        "    public Flux<CommandResponse<SUnionCommand, Flux<ByteBuffer>>> sUnion(Publisher<SUnionCommand> commands) {",
    ),
    (
        "    @Override\n    public Flux<NumericResponse<SUnionStoreCommand, Long>> sUnionStore(Publisher<SUnionStoreCommand> commands) {",
        "    /** SUNIONSTORE：将并集写入目标 key 并返回元素个数。 */\n"
        "    @Override\n"
        "    public Flux<NumericResponse<SUnionStoreCommand, Long>> sUnionStore(Publisher<SUnionStoreCommand> commands) {",
    ),
    (
        "    @Override\n    public Flux<CommandResponse<SDiffCommand, Flux<ByteBuffer>>> sDiff(Publisher<SDiffCommand> commands) {",
        "    /** SDIFF：返回集合差集 member 流。 */\n"
        "    @Override\n"
        "    public Flux<CommandResponse<SDiffCommand, Flux<ByteBuffer>>> sDiff(Publisher<SDiffCommand> commands) {",
    ),
    (
        "    @Override\n    public Flux<NumericResponse<SDiffStoreCommand, Long>> sDiffStore(Publisher<SDiffStoreCommand> commands) {",
        "    /** SDIFFSTORE：将差集写入目标 key 并返回元素个数。 */\n"
        "    @Override\n"
        "    public Flux<NumericResponse<SDiffStoreCommand, Long>> sDiffStore(Publisher<SDiffStoreCommand> commands) {",
    ),
    (
        "    @Override\n    public Flux<CommandResponse<KeyCommand, Flux<ByteBuffer>>> sMembers(Publisher<KeyCommand> commands) {",
        "    /** SMEMBERS：返回集合全部 member。 */\n"
        "    @Override\n"
        "    public Flux<CommandResponse<KeyCommand, Flux<ByteBuffer>>> sMembers(Publisher<KeyCommand> commands) {",
    ),
    (
        "    @Override\n    public Flux<CommandResponse<KeyCommand, Flux<ByteBuffer>>> sScan(Publisher<KeyScanCommand> commands) {",
        "    /** SSCAN：增量迭代集合 member，支持 MATCH/COUNT 选项。 */\n"
        "    @Override\n"
        "    public Flux<CommandResponse<KeyCommand, Flux<ByteBuffer>>> sScan(Publisher<KeyScanCommand> commands) {",
    ),
    (
        "    @Override\n    public Flux<CommandResponse<SRandMembersCommand, Flux<ByteBuffer>>> sRandMember(",
        "    /** SRANDMEMBER：随机返回指定数量的 member。 */\n"
        "    @Override\n"
        "    public Flux<CommandResponse<SRandMembersCommand, Flux<ByteBuffer>>> sRandMember(",
    ),
]

# --- spring-data-21: reactive subscription ---
W13B_REPLACEMENTS["RedissonReactiveSubscription.java"] = [
    (
        "/**\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Spring Data Redis 响应式 Pub/Sub 订阅实现。\n"
        " * <p>通过 {@link PublishSubscribeService} 管理频道与模式订阅；\n"
        " {@link #receive()} 以 Reactor {@link Flux} 推送 {@link ChannelMessage}/{@link PatternMessage}。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "    public static class ListenableCounter {",
        "    /** 引用计数器：在并发 subscribe/unsubscribe 完成前延迟注册消息监听器。 */\n"
        "    public static class ListenableCounter {",
    ),
    (
        "        public void acquire() {",
        "        /** 递增进行中操作计数。 */\n"
        "        public void acquire() {",
    ),
    (
        "        public void release() {",
        "        /** 递减计数；归零时执行挂起的 {@link Runnable}。 */\n"
        "        public void release() {",
    ),
    (
        "        public void addListener(Runnable r) {",
        "        /** 无进行中操作时立即执行，否则在 {@link #release()} 归零时执行。 */\n"
        "        public void addListener(Runnable r) {",
    ),
    (
        "    private final PublishSubscribeService subscribeService;",
        "    /** Redisson Pub/Sub 订阅服务。 */\n"
        "    private final PublishSubscribeService subscribeService;",
    ),
    (
        "    public RedissonReactiveSubscription(ConnectionManager connectionManager) {",
        "    /** 从 {@link ConnectionManager} 获取 {@link PublishSubscribeService}。 */\n"
        "    public RedissonReactiveSubscription(ConnectionManager connectionManager) {",
    ),
    (
        "    @Override\n    public Mono<Void> subscribe(ByteBuffer... channels) {",
        "    /** SUBSCRIBE：订阅一个或多个频道并记录连接条目。 */\n"
        "    @Override\n"
        "    public Mono<Void> subscribe(ByteBuffer... channels) {",
    ),
    (
        "    protected ChannelName toChannelName(ByteBuffer channel) {",
        "    /** 将 {@link ByteBuffer} 频道名转为 {@link ChannelName}。 */\n"
        "    protected ChannelName toChannelName(ByteBuffer channel) {",
    ),
    (
        "    @Override\n    public Mono<Void> pSubscribe(ByteBuffer... patterns) {",
        "    /** PSUBSCRIBE：按模式订阅一个或多个 pattern。 */\n"
        "    @Override\n"
        "    public Mono<Void> pSubscribe(ByteBuffer... patterns) {",
    ),
    (
        "    @Override\n    public Mono<Void> unsubscribe() {",
        "    /** 取消当前全部频道订阅。 */\n"
        "    @Override\n"
        "    public Mono<Void> unsubscribe() {",
    ),
    (
        "    @Override\n    public Mono<Void> unsubscribe(ByteBuffer... channels) {",
        "    /** UNSUBSCRIBE：取消指定频道订阅并清理空连接条目。 */\n"
        "    @Override\n"
        "    public Mono<Void> unsubscribe(ByteBuffer... channels) {",
    ),
    (
        "    @Override\n    public Mono<Void> pUnsubscribe() {",
        "    /** 取消当前全部模式订阅。 */\n"
        "    @Override\n"
        "    public Mono<Void> pUnsubscribe() {",
    ),
    (
        "    @Override\n    public Mono<Void> pUnsubscribe(ByteBuffer... patterns) {",
        "    /** PUNSUBSCRIBE：取消指定模式订阅并清理空连接条目。 */\n"
        "    @Override\n"
        "    public Mono<Void> pUnsubscribe(ByteBuffer... patterns) {",
    ),
    (
        "    @Override\n    public Set<ByteBuffer> getChannels() {",
        "    /** 返回当前已订阅的频道集合。 */\n"
        "    @Override\n"
        "    public Set<ByteBuffer> getChannels() {",
    ),
    (
        "    @Override\n    public Set<ByteBuffer> getPatterns() {",
        "    /** 返回当前已订阅的模式集合。 */\n"
        "    @Override\n"
        "    public Set<ByteBuffer> getPatterns() {",
    ),
    (
        "    @Override\n    public Flux<Message<ByteBuffer, ByteBuffer>> receive() {",
        "    /** 创建消息流：等待 subscribe 完成后注册 {@link BaseRedisPubSubListener} 并推送消息。 */\n"
        "    @Override\n"
        "    public Flux<Message<ByteBuffer, ByteBuffer>> receive() {",
    ),
    (
        "                            if (!patterns.containsKey(new ChannelName(pattern.toString()))) {",
        "                            // 忽略未订阅 pattern 的回调。\n"
        "                            if (!patterns.containsKey(new ChannelName(pattern.toString()))) {",
    ),
    (
        "                            if (!channels.containsKey(new ChannelName(channel.toString()))) {",
        "                            // 忽略未订阅频道的回调。\n"
        "                            if (!channels.containsKey(new ChannelName(channel.toString()))) {",
    ),
    (
        "    @Override\n    public Mono<Void> cancel() {",
        "    /** 取消全部订阅并释放消息监听器。 */\n"
        "    @Override\n"
        "    public Mono<Void> cancel() {",
    ),
]

# --- spring-data-21: sync Pub/Sub subscription ---
W13B_REPLACEMENTS["RedissonSubscription.java"] = [
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
        "    private final PublishSubscribeService subscribeService;",
        "    /** Redisson Pub/Sub 订阅服务。 */\n"
        "    private final PublishSubscribeService subscribeService;",
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
        "                    if (!Arrays.equals(((ChannelName) pattern).getName(), channel)) {",
        "                    // 忽略非目标 pattern 的回调。\n"
        "                    if (!Arrays.equals(((ChannelName) pattern).getName(), channel)) {",
    ),
    (
        "    private byte[] toBytes(Object message) {",
        "    /** 将 String 或 byte[] 载荷统一为字节数组。 */\n"
        "    private byte[] toBytes(Object message) {",
    ),
    (
        "    @Override\n    protected void doPUnsubscribe(boolean all, byte[]... patterns) {",
        "    /** PUNSUBSCRIBE：取消指定模式订阅。 */\n"
        "    @Override\n"
        "    protected void doPUnsubscribe(boolean all, byte[]... patterns) {",
    ),
    (
        "    @Override\n    protected void doClose() {",
        "    /** 关闭时取消所有频道与模式订阅。 */\n"
        "    @Override\n"
        "    protected void doClose() {",
    ),
]
