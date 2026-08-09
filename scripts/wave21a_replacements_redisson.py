"""Chinese annotation replacements for Redisson 4.7.0 wave-21a spring-data-26 [0:15]."""
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


W15A = _load("wave15a_replacements_redisson.py", "W15A_REPLACEMENTS")

_EMPTY_JDOC = "/**\n * \n * @author Nikita Koksharov\n *\n */"

W21A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

# spring-data-26: sources identical to spring-data-22/24/25 for these files.
for _key in (
    "RedissonReactiveRedisClusterConnection.java",
    "RedissonReactiveRedisConnection.java",
    "RedissonReactiveScriptingCommands.java",
    "RedissonReactiveServerCommands.java",
    "RedissonReactiveSetCommands.java",
    "RedissonSentinelConnection.java",
    "ScoredSortedListReplayDecoder.java",
    "ScoredSortedSetReplayDecoder.java",
    "ScoredSortedSetReplayDecoderV2.java",
    "SecondsConvertor.java",
):
    W21A_REPLACEMENTS[_key] = list(W15A[_key])

# spring-data-26: TIME with TimeUnit conversion (added vs spring-data-24).
W21A_REPLACEMENTS["RedissonReactiveServerCommands.java"].append(
    (
        "    @Override\n    public Mono<Long> time(TimeUnit timeUnit) {",
        "    /** TIME：读取服务器时间并按 {@link TimeUnit} 转换（毫秒基准）。 */\n"
        "    @Override\n"
        "    public Mono<Long> time(TimeUnit timeUnit) {",
    )
)

# spring-data-26: blocking ZSET pop decoders (key + member + score triplets).
W21A_REPLACEMENTS["ScoredSortedSetBlockingReplayDecoder.java"] = [
    (
        _EMPTY_JDOC,
        "/**\n"
        " * 阻塞有序集合弹出响应解码器：将 BZPOPMIN/BZPOPMAX 等返回的 key/member/score 三元组\n"
        " * 解析为 {@link Set}{@code <}{@link RedisZSetCommands.Tuple}{@code >}。\n"
        " * <p>每 3 个元素为一组：下标 0 为 key（解码时跳过）、1 为 member、2 为 score；\n"
        " * {@code paramNum == 2} 时使用 {@link DoubleCodec} 解析 score。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "        if (paramNum == 2) {",
        "        // 三元组中第三项为 score，使用 DoubleCodec。\n"
        "        if (paramNum == 2) {",
    ),
    (
        "    @Override\n    public Set<RedisZSetCommands.Tuple> decode(List<Object> parts, State state) {",
        "    /** 按 key/member/score 步长 3 遍历，提取 member 与 score 构造 {@link DefaultTuple}。 */\n"
        "    @Override\n"
        "    public Set<RedisZSetCommands.Tuple> decode(List<Object> parts, State state) {",
    ),
    (
        "        for (int i = 0; i < parts.size(); i += 3) {",
        "        // 跳过每组首元素 key，取 i+1 member 与 i+2 score。\n"
        "        for (int i = 0; i < parts.size(); i += 3) {",
    ),
]

W21A_REPLACEMENTS["ScoredSortedSingleBlockingReplayDecoder.java"] = [
    (
        _EMPTY_JDOC,
        "/**\n"
        " * 单条阻塞有序集合弹出解码器：将 BZPOPMIN/BZPOPMAX 等单 key 响应\n"
        " * （key、member、score 三元素）解析为一条 {@link RedisZSetCommands.Tuple}。\n"
        " * <p>{@code paramNum == 2} 时以 {@link DoubleCodec} 解析 score。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "        if (paramNum == 2) {",
        "        // 响应第三项为 score，使用 DoubleCodec。\n"
        "        if (paramNum == 2) {",
    ),
    (
        "    @Override\n    public RedisZSetCommands.Tuple decode(List<Object> parts, State state) {",
        "    /** 从 parts[1] member 与 parts[2] score 构造 {@link DefaultTuple}（parts[0] 为 key）。 */\n"
        "    @Override\n"
        "    public RedisZSetCommands.Tuple decode(List<Object> parts, State state) {",
    ),
]

# spring-data-26: SubscriptionListener API (constructor + status callbacks).
W21A_REPLACEMENTS["RedissonReactiveSubscription.java"] = [
    (
        "/**\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Spring Data Redis 响应式 Pub/Sub 订阅实现。\n"
        " * <p>通过 {@link PublishSubscribeService} 管理频道与模式订阅；\n"
        " {@link #receive()} 以 Reactor {@link Flux} 推送 {@link ChannelMessage}/{@link PatternMessage}；\n"
        " 订阅状态变更经 {@link SubscriptionListener} 回调。\n"
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
        "    private final RedisPubSubListener subscriptionListener;",
        "    /** 内部 Pub/Sub 监听器，将 Redisson 状态事件转发给 {@link SubscriptionListener}。 */\n"
        "    private final RedisPubSubListener subscriptionListener;",
    ),
    (
        "    private final PublishSubscribeService subscribeService;",
        "    /** Redisson Pub/Sub 订阅服务。 */\n"
        "    private final PublishSubscribeService subscribeService;",
    ),
    (
        "    public RedissonReactiveSubscription(ConnectionManager connectionManager, SubscriptionListener subscriptionListener) {",
        "    /** 从 {@link ConnectionManager} 获取 {@link PublishSubscribeService}，并绑定 {@link SubscriptionListener}。 */\n"
        "    public RedissonReactiveSubscription(ConnectionManager connectionManager, SubscriptionListener subscriptionListener) {",
    ),
    (
        "                if (type == PubSubType.SUBSCRIBE) {",
        "                // 频道订阅成功：通知 Spring SubscriptionListener。\n"
        "                if (type == PubSubType.SUBSCRIBE) {",
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

W21A_REPLACEMENTS["RedissonSubscription.java"] = [
    (
        "/**\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Spring Data Redis Pub/Sub {@link AbstractSubscription} 的 Redisson 实现。\n"
        " * <p>通过 {@link PublishSubscribeService} 管理频道/模式订阅，\n"
        "将 Redisson 消息转为 {@link DefaultMessage} 回调 {@link MessageListener}；\n"
        "若监听器为 {@link SubscriptionListener}，则同步订阅/取消订阅状态事件。\n"
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
        "                    if (getListener() instanceof SubscriptionListener) {",
        "                    // SubscriptionListener：记录待通知的已订阅频道。\n"
        "                    if (getListener() instanceof SubscriptionListener) {",
    ),
    (
        "    @Override\n    protected void doUnsubscribe(boolean all, byte[]... channels) {",
        "    /** 取消指定频道订阅；{@link SubscriptionListener} 时回调 onChannelUnsubscribed。 */\n"
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
        "    /** PUNSUBSCRIBE：取消指定模式订阅；{@link SubscriptionListener} 时回调 onPatternUnsubscribed。 */\n"
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

W21A_REPLACEMENTS["ScoredSortedSingleReplayDecoder.java"] = [
    (
        _EMPTY_JDOC,
        "/**\n"
        " * 单条 member/score 对解码为 {@link Tuple}；空响应返回 {@code null}。\n"
        " * <p>奇数下标参数经 {@link DoubleCodec} 解析 score，适用于仅含一对元素的 ZSET 命令响应。\n"
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
        "    @Override\n    public Tuple decode(List<Object> parts, State state) {",
        "    /** 空列表返回 null，否则从 member/score 构造 {@link DefaultTuple}。 */\n"
        "    @Override\n"
        "    public Tuple decode(List<Object> parts, State state) {",
    ),
    (
        "        if (parts.isEmpty()) {",
        "        // 无元素时返回 null（如 ZPOPMIN 空集合）。\n"
        "        if (parts.isEmpty()) {",
    ),
]
