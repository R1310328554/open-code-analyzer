"""Chinese annotation replacements for Redisson 4.7.0 wave-22b spring-data-27 reactive [15:30]."""
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


W20B = _load("wave20b_replacements_redisson.py", "W20B_REPLACEMENTS")
W21A = _load("wave21a_replacements_redisson.py", "W21A_REPLACEMENTS")

W22B_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

# spring-data-27: identical to spring-data-26 for these reactive command adapters.
for _key in (
    "RedissonReactiveHyperLogLogCommands.java",
    "RedissonReactiveKeyCommands.java",
    "RedissonReactiveListCommands.java",
    "RedissonReactiveNumberCommands.java",
    "RedissonReactivePubSubCommands.java",
):
    W22B_REPLACEMENTS[_key] = W20B[_key]

for _key in (
    "RedissonReactiveRedisClusterConnection.java",
    "RedissonReactiveRedisConnection.java",
    "RedissonReactiveScriptingCommands.java",
    "RedissonReactiveServerCommands.java",
    "RedissonReactiveSetCommands.java",
    "RedissonReactiveSubscription.java",
    "RedissonSentinelConnection.java",
    "ScoredSortedListReplayDecoder.java",
    "ScoredSortedSetBlockingReplayDecoder.java",
):
    W22B_REPLACEMENTS[_key] = W21A[_key]

_EMPTY_JDOC = "/**\n * \n * @author Nikita Koksharov\n *\n */"

# spring-data-27: RedissonSubscription refactored with CompletableFuture tracking and latch sync.
W22B_REPLACEMENTS["RedissonSubscription.java"] = [
    (
        "/**\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Spring Data Redis Pub/Sub {@link AbstractSubscription} 的 Redisson 实现。\n"
        " * <p>通过 {@link PublishSubscribeService} 管理频道/模式订阅，\n"
        "将 Redisson 消息转为 {@link DefaultMessage} 回调 {@link MessageListener}；\n"
        "若监听器为 {@link SubscriptionListener}，则同步订阅/取消订阅状态事件。\n"
        " * <p>spring-data-27 起以 {@link CompletableFuture} 追踪各频道/模式订阅状态，\n"
        "并对 {@code SynchronizingMessageListener} 使用 {@link CountDownLatch} 等待首次取消订阅。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "    private static final CompletableFuture<Void> COMPLETED = new CompletableFuture<>();",
        "    /** 已完成的占位 Future，用于已订阅频道的回调合并。 */\n"
        "    private static final CompletableFuture<Void> COMPLETED = new CompletableFuture<>();",
    ),
    (
        "    private final Map<ChannelName, CompletableFuture<Void>> subscribed = new ConcurrentHashMap<>();",
        "    /** 频道名 → 订阅完成 Future，供重复订阅与回调去重。 */\n"
        "    private final Map<ChannelName, CompletableFuture<Void>> subscribed = new ConcurrentHashMap<>();",
    ),
    (
        "    private final Map<ChannelName, CompletableFuture<Void>> psubscribed = new ConcurrentHashMap<>();",
        "    /** 模式名 → PSUBSCRIBE 完成 Future。 */\n"
        "    private final Map<ChannelName, CompletableFuture<Void>> psubscribed = new ConcurrentHashMap<>();",
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
        "    /** SUBSCRIBE：仅订阅尚未注册的频道，并可选等待 SynchronizingMessageListener 同步点。 */\n"
        "    @Override\n"
        "    protected void doSubscribe(byte[]... channels) {",
    ),
    (
        "        boolean hasSubscriptionsBefore = !(subscribed.isEmpty() & psubscribed.isEmpty());",
        "        // 是否已有活跃订阅（决定是否需要 latch 等待）。\n"
        "        boolean hasSubscriptionsBefore = !(subscribed.isEmpty() & psubscribed.isEmpty());",
    ),
    (
        "        Map<ChannelName, CompletableFuture<Void>> tosubscribe = getNonSubscribed(channels, subscribed, (l, ch) -> {",
        "        // 过滤已订阅频道，收集本次需新订阅的条目。\n"
        "        Map<ChannelName, CompletableFuture<Void>> tosubscribe = getNonSubscribed(channels, subscribed, (l, ch) -> {",
    ),
    (
        "        CountDownLatch latch = new CountDownLatch(1);",
        "        // 首次全量取消订阅时释放，供 SynchronizingMessageListener 阻塞等待。\n"
        "        CountDownLatch latch = new CountDownLatch(1);",
    ),
    (
        "                    if (!Arrays.equals(((ChannelName) ch).getName(), channel.getName())) {",
        "                    // 忽略非目标频道的回调（连接复用时可能收到其他频道消息）。\n"
        "                    if (!Arrays.equals(((ChannelName) ch).getName(), channel.getName())) {",
    ),
    (
        "                    if (getListener() instanceof SubscriptionListener\n"
        "                            && type == PubSubType.SUBSCRIBE) {",
        "                    // SUBSCRIBE 确认：完成对应频道的 CompletableFuture。\n"
        "                    if (getListener() instanceof SubscriptionListener\n"
        "                            && type == PubSubType.SUBSCRIBE) {",
    ),
    (
        "                    if (type == PubSubType.UNSUBSCRIBE) {",
        "                    // 全部频道取消后释放 latch。\n"
        "                    if (type == PubSubType.UNSUBSCRIBE) {",
    ),
    (
        "        // fix for RedisMessageListenerContainer",
        "        // RedisMessageListenerContainer 同步修复：等待首次 UNSUBSCRIBE。\n"
        "        // fix for RedisMessageListenerContainer",
    ),
    (
        "    private Map<ChannelName, CompletableFuture<Void>> getNonSubscribed(byte[][] channels,",
        "    /** 返回尚未订阅的频道/模式；已订阅则异步回调 {@link SubscriptionListener}。 */\n"
        "    private Map<ChannelName, CompletableFuture<Void>> getNonSubscribed(byte[][] channels,",
    ),
    (
        "            CompletableFuture<Void> cf = subscribed.putIfAbsent(n, f);",
        "            // putIfAbsent 成功表示新订阅，否则合并到已有 Future。\n"
        "            CompletableFuture<Void> cf = subscribed.putIfAbsent(n, f);",
    ),
    (
        "    @Override\n    protected void doUnsubscribe(boolean all, byte[]... channels) {",
        "    /** UNSUBSCRIBE：取消指定频道；{@link SubscriptionListener} 时回调 onChannelUnsubscribed。 */\n"
        "    @Override\n"
        "    protected void doUnsubscribe(boolean all, byte[]... channels) {",
    ),
    (
        "    @Override\n    protected void doPsubscribe(byte[]... patterns) {",
        "    /** PSUBSCRIBE：按模式订阅，逻辑同 {@link #doSubscribe}。 */\n"
        "    @Override\n"
        "    protected void doPsubscribe(byte[]... patterns) {",
    ),
    (
        "                    if (!Arrays.equals(((ChannelName) pattern).getName(), channel.getName())) {",
        "                    // 忽略非目标 pattern 的回调。\n"
        "                    if (!Arrays.equals(((ChannelName) pattern).getName(), channel.getName())) {",
    ),
    (
        "                    if (getListener() instanceof SubscriptionListener\n"
        "                            && type == PubSubType.PSUBSCRIBE) {",
        "                    // PSUBSCRIBE 确认：完成对应模式的 CompletableFuture。\n"
        "                    if (getListener() instanceof SubscriptionListener\n"
        "                            && type == PubSubType.PSUBSCRIBE) {",
    ),
    (
        "                    if (type == PubSubType.PUNSUBSCRIBE) {",
        "                    // 模式取消后释放 latch。\n"
        "                    if (type == PubSubType.PUNSUBSCRIBE) {",
    ),
    (
        "    private byte[] toBytes(Object message) {",
        "    /** 将 String 或 byte[] 载荷统一为字节数组。 */\n"
        "    private byte[] toBytes(Object message) {",
    ),
    (
        "    @Override\n    protected void doPUnsubscribe(boolean all, byte[]... patterns) {",
        "    /** PUNSUBSCRIBE：取消指定模式；{@link SubscriptionListener} 时回调 onPatternUnsubscribed。 */\n"
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
