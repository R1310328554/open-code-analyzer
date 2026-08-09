"""Chinese annotation replacements for Redisson 4.7.0 wave-38b core [15:30]."""
from __future__ import annotations

_R = "redisson/src/main/java/org/redisson/"

_EMPTY_JDOC = "/**\n *\n * @author Nikita Koksharov\n *\n */"

W38B_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

# --- IntegerSlotCallback / LongSlotCallback ---

_int_slot = [
    (
        _EMPTY_JDOC,
        "/**\n"
        " * Redis Cluster 多 slot 命令的 {@link Integer} 结果聚合回调。\n"
        " * <p>对各 slot 返回值求和；可选固定 {@link #createParams} 参数覆盖默认行为。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public IntegerSlotCallback() {",
        "    /** 使用默认参数策略构造。 */\n"
        "    public IntegerSlotCallback() {",
    ),
    (
        "    public IntegerSlotCallback(Object[] params) {",
        "    /** @param params 若非 null，{@link #createParams} 始终返回该固定参数数组 */\n"
        "    public IntegerSlotCallback(Object[] params) {",
    ),
    (
        "    @Override\n    public Integer onResult(Collection<Integer> result) {",
        "    /** 对各 slot 返回的整型值求和。 */\n"
        "    @Override\n"
        "    public Integer onResult(Collection<Integer> result) {",
    ),
    (
        "    @Override\n    public Object[] createParams(List<Object> params) {",
        "    /** 若构造时指定了固定参数则直接返回，否则委托 {@link SlotCallback} 默认实现。 */\n"
        "    @Override\n"
        "    public Object[] createParams(List<Object> params) {",
    ),
]
W38B_REPLACEMENTS[f"{_R}IntegerSlotCallback.java"] = _int_slot
W38B_REPLACEMENTS["IntegerSlotCallback.java"] = _int_slot

_long_slot = [
    (
        _EMPTY_JDOC,
        "/**\n"
        " * Redis Cluster 多 slot 命令的 {@link Long} 结果聚合回调。\n"
        " * <p>对各 slot 返回值求和；可选固定 {@link #createParams} 参数覆盖默认行为。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public LongSlotCallback() {",
        "    /** 使用默认参数策略构造。 */\n"
        "    public LongSlotCallback() {",
    ),
    (
        "    public LongSlotCallback(Object[] params) {",
        "    /** @param params 若非 null，{@link #createParams} 始终返回该固定参数数组 */\n"
        "    public LongSlotCallback(Object[] params) {",
    ),
    (
        "    @Override\n    public Long onResult(Collection<Long> result) {",
        "    /** 对各 slot 返回的长整型值求和。 */\n"
        "    @Override\n"
        "    public Long onResult(Collection<Long> result) {",
    ),
    (
        "    @Override\n    public Object[] createParams(List<Object> params) {",
        "    /** 若构造时指定了固定参数则直接返回，否则委托 {@link SlotCallback} 默认实现。 */\n"
        "    @Override\n"
        "    public Object[] createParams(List<Object> params) {",
    ),
]
W38B_REPLACEMENTS[f"{_R}LongSlotCallback.java"] = _long_slot
W38B_REPLACEMENTS["LongSlotCallback.java"] = _long_slot

# --- JndiRedissonFactory ---

_jndi = [
    (
        "/**\n * Redisson object factory used to register instance in JNDI registry. \n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * JNDI {@link ObjectFactory}：从 Reference 中的 YAML 配置路径创建 {@link RedissonClient}。\n"
        " * <p>适用于应用服务器将 Redisson 实例绑定到 JNDI 后供组件查找。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    @Override\n    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment)\n            throws Exception {",
        "    /** 解析 JNDI {@link Reference} 的 {@code configPath} 地址并构建客户端。 */\n"
        "    @Override\n"
        "    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment)\n"
        "            throws Exception {",
    ),
    (
        "    protected RedissonClient buildClient(String configPath) throws NamingException {",
        "    /** 从 YAML 文件加载 {@link Config} 并调用 {@link Redisson#create}。\n"
        "     *\n"
        "     * @param configPath Redisson YAML 配置文件路径\n"
        "     * @throws NamingException 配置解析或客户端创建失败\n"
        "     */\n"
        "    protected RedissonClient buildClient(String configPath) throws NamingException {",
    ),
]
W38B_REPLACEMENTS[f"{_R}JndiRedissonFactory.java"] = _jndi
W38B_REPLACEMENTS["JndiRedissonFactory.java"] = _jndi

# --- MapWriteBehindTask ---

_map_wb = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * {@link MapOptions} Write-Behind 异步落库调度器。\n"
        " * <p>将 {@link MapWriterTask} 写入 Redis 队列，按延迟与批量大小\n"
        " * 调用 {@link MapOptions#getWriter()} 或 {@link MapOptions#getWriterAsync()}。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public MapWriteBehindTask(String name, CommandAsyncExecutor commandExecutor, MapOptions<?, ?> options) {",
        "    /** @param name Map 名称；队列键为 {@code name:write-behind-queue}\n"
        "     *  @param commandExecutor 异步命令执行器\n"
        "     *  @param options 含 writer、延迟与批量大小等 Write-Behind 配置\n"
        "     */\n"
        "    public MapWriteBehindTask(String name, CommandAsyncExecutor commandExecutor, MapOptions<?, ?> options) {",
    ),
    (
        "    public void start() {",
        "    /** 启动后台轮询；重复调用仅生效一次。 */\n"
        "    public void start() {",
    ),
    (
        "    private void pollTask(Map<Object, Object> addedMap, List<Object> deletedKeys) {",
        "    /** 异步从队列取任务；队列空时 flush 累积批次并重新调度。 */\n"
        "    private void pollTask(Map<Object, Object> addedMap, List<Object> deletedKeys) {",
    ),
    (
        "    private void flushTasks(Map<Object, Object> addedMap, List<Object> deletedKeys) {",
        "    /** 将累积的删除键与新增条目一次性提交给 MapWriter。 */\n"
        "    private void flushTasks(Map<Object, Object> addedMap, List<Object> deletedKeys) {",
    ),
    (
        "    private void processTask(Map<Object, Object> addedMap, List<Object> deletedKeys, MapWriterTask task) {",
        "    /** 合并单条 {@link MapWriterTask} 到批次；达 {@link MapOptions#getWriteBehindBatchSize()} 时立即 flush。 */\n"
        "    private void processTask(Map<Object, Object> addedMap, List<Object> deletedKeys, MapWriterTask task) {",
    ),
    (
        "    private void enqueueTask() {",
        "    /** 在 {@link MapOptions#getWriteBehindDelay()} 后启动新一轮 poll。 */\n"
        "    private void enqueueTask() {",
    ),
    (
        "    public void addTask(MapWriterTask task) {",
        "    /** 将 Write-Behind 任务异步入队。 */\n"
        "    public void addTask(MapWriterTask task) {",
    ),
    (
        "    public void stop() {",
        "    /** 停止调度并 drain 队列中剩余任务后 flush。 */\n"
        "    public void stop() {",
    ),
]
W38B_REPLACEMENTS[f"{_R}MapWriteBehindTask.java"] = _map_wb
W38B_REPLACEMENTS["MapWriteBehindTask.java"] = _map_wb

# --- MapWriterTask ---

_map_wt = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Write-Behind 队列中的可序列化写入任务。\n"
        " * <p>{@link Remove} 表示批量删除键；{@link Add} 表示批量 put。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public static class Remove extends MapWriterTask {",
        "    /** 删除键任务：{@link #getKeys()} 为待删键集合。 */\n"
        "    public static class Remove extends MapWriterTask {",
    ),
    (
        "    public static class Add extends MapWriterTask {",
        "    /** 新增/更新任务：{@link #getMap()} 为待写入键值对。 */\n"
        "    public static class Add extends MapWriterTask {",
    ),
    (
        "    public MapWriterTask(Object key) {",
        "    /** 单键删除任务。 */\n"
        "    public MapWriterTask(Object key) {",
    ),
    (
        "    public MapWriterTask(Object key, Object value) {",
        "    /** 单键 put 任务。 */\n"
        "    public MapWriterTask(Object key, Object value) {",
    ),
    (
        "    public MapWriterTask(Map<?, ?> map) {",
        "    /** 批量 put 任务。 */\n"
        "    public MapWriterTask(Map<?, ?> map) {",
    ),
    (
        "    public MapWriterTask(Collection<?> keys) {",
        "    /** 批量删除任务。 */\n"
        "    public MapWriterTask(Collection<?> keys) {",
    ),
    (
        "    public <V> Collection<V> getKeys() {",
        "    /** 返回待删除键集合（Remove 任务）。 */\n"
        "    public <V> Collection<V> getKeys() {",
    ),
    (
        "    public <K, V> Map<K, V> getMap() {",
        "    /** 返回待写入键值映射（Add 任务）。 */\n"
        "    public <K, V> Map<K, V> getMap() {",
    ),
]
W38B_REPLACEMENTS[f"{_R}MapWriterTask.java"] = _map_wt
W38B_REPLACEMENTS["MapWriterTask.java"] = _map_wt

# --- PubSubEntry ---

_pubsub_entry = [
    (
        "/**\n *\n * Nikita Koksharov\n *\n */",
        "/**\n"
        " * Pub/Sub 订阅条目的引用计数与完成信号。\n"
        " * <p>{@link #acquire}/{@link #release} 管理并发订阅；{@link #getPromise} 在订阅就绪时完成。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    void acquire();",
        "    /** 增加一次引用（默认 1）。 */\n"
        "    void acquire();",
    ),
    (
        "    void acquire(int permits);",
        "    /** 增加指定数量的引用。 */\n"
        "    void acquire(int permits);",
    ),
    (
        "    int release();",
        "    /** 释放引用并返回剩余计数。 */\n"
        "    int release();",
    ),
    (
        "    CompletableFuture<E> getPromise();",
        "    /** 订阅完成或失败时完成的 Future。 */\n"
        "    CompletableFuture<E> getPromise();",
    ),
]
W38B_REPLACEMENTS[f"{_R}PubSubEntry.java"] = _pubsub_entry
W38B_REPLACEMENTS["PubSubEntry.java"] = _pubsub_entry

# --- PubSubMessageListener ---

_pubsub_msg = [
    (
        "/**\n *\n * @author Nikita Koksharov\n *\n * @param <V> value\n */",
        "/**\n"
        " * 将 Redis 频道/模式消息转发给 {@link MessageListener} 的适配器。\n"
        " * <p>仅当频道/模式在 {@code names} 中且消息类型匹配时才回调。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " * @param <V> 消息体类型\n"
        " */",
    ),
    (
        "    public PubSubMessageListener(Class<V> type, MessageListener<V> listener, Set<String> names) {",
        "    /** @param type 期望的消息类型\n"
        "     *  @param listener 用户消息监听器\n"
        "     *  @param names 监听的频道或模式名集合\n"
        "     */\n"
        "    public PubSubMessageListener(Class<V> type, MessageListener<V> listener, Set<String> names) {",
    ),
    (
        "    public PubSubMessageListener(Class<V> type, MessageListener<V> listener, Set<String> names, Runnable callback) {",
        "    /** 同上；消息匹配后额外执行 {@code callback}（如释放信号量）。 */\n"
        "    public PubSubMessageListener(Class<V> type, MessageListener<V> listener, Set<String> names, Runnable callback) {",
    ),
    (
        "    @Override\n    public void onMessage(CharSequence channel, Object message) {",
        "    /** SUBSCRIBE 模式：频道名在 names 中且类型匹配时回调。 */\n"
        "    @Override\n"
        "    public void onMessage(CharSequence channel, Object message) {",
    ),
    (
        "    @Override\n    public void onPatternMessage(CharSequence pattern, CharSequence channel, Object message) {",
        "    /** PSUBSCRIBE 模式：模式名在 names 中且类型匹配时回调。 */\n"
        "    @Override\n"
        "    public void onPatternMessage(CharSequence pattern, CharSequence channel, Object message) {",
    ),
]
W38B_REPLACEMENTS[f"{_R}PubSubMessageListener.java"] = _pubsub_msg
W38B_REPLACEMENTS["PubSubMessageListener.java"] = _pubsub_msg

# --- PubSubPatternMessageListener ---

_pubsub_pat_msg = [
    (
        "/**\n *\n * @author Nikita Koksharov\n *\n * @param <V> value\n */",
        "/**\n"
        " * PSUBSCRIBE 模式消息监听器：将匹配的模式消息交给 {@link PatternMessageListener}。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " * @param <V> 消息体类型\n"
        " */",
    ),
    (
        "    public PubSubPatternMessageListener(Class<V> type, PatternMessageListener<V> listener, String name) {",
        "    /** @param name 订阅的模式名（如 {@code user:*}） */\n"
        "    public PubSubPatternMessageListener(Class<V> type, PatternMessageListener<V> listener, String name) {",
    ),
    (
        "    @Override\n    public void onPatternMessage(CharSequence pattern, CharSequence channel, V message) {",
        "    /** 模式名匹配且消息类型正确时转发给 listener。 */\n"
        "    @Override\n"
        "    public void onPatternMessage(CharSequence pattern, CharSequence channel, V message) {",
    ),
]
W38B_REPLACEMENTS[f"{_R}PubSubPatternMessageListener.java"] = _pubsub_pat_msg
W38B_REPLACEMENTS["PubSubPatternMessageListener.java"] = _pubsub_pat_msg

# --- PubSubPatternStatusListener ---

_pubsub_pat_stat = [
    (
        _EMPTY_JDOC,
        "/**\n"
        " * PSUBSCRIBE/PUNSUBSCRIBE 状态事件适配器。\n"
        " * <p>将 Redis Pub/Sub 状态转为 {@link PatternStatusListener} 回调。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public PubSubPatternStatusListener(PatternStatusListener listener, String name) {",
        "    /** @param name 监听的模式名 */\n"
        "    public PubSubPatternStatusListener(PatternStatusListener listener, String name) {",
    ),
    (
        "    @Override\n    public void onStatus(PubSubType type, CharSequence channel) {",
        "    /** 频道名等于模式名时触发 onPSubscribe/onPUnsubscribe。 */\n"
        "    @Override\n"
        "    public void onStatus(PubSubType type, CharSequence channel) {",
    ),
]
W38B_REPLACEMENTS[f"{_R}PubSubPatternStatusListener.java"] = _pubsub_pat_stat
W38B_REPLACEMENTS["PubSubPatternStatusListener.java"] = _pubsub_pat_stat

# --- PubSubStatusListener ---

_pubsub_stat = [
    (
        _EMPTY_JDOC,
        "/**\n"
        " * SUBSCRIBE/UNSUBSCRIBE（及 sharded 变体）状态事件适配器。\n"
        " * <p>待所有 {@code names} 均收到状态确认后，才通知 {@link StatusListener} 一次。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public PubSubStatusListener(StatusListener listener, String... names) {",
        "    /** @param names 待确认订阅/取消订阅的频道名列表 */\n"
        "    public PubSubStatusListener(StatusListener listener, String... names) {",
    ),
    (
        "    @Override\n    public void onStatus(PubSubType type, CharSequence channel) {",
        "    /** 逐个消减 notified 集合；全部就绪后触发 onSubscribe/onUnsubscribe。 */\n"
        "    @Override\n"
        "    public void onStatus(PubSubType type, CharSequence channel) {",
    ),
]
W38B_REPLACEMENTS[f"{_R}PubSubStatusListener.java"] = _pubsub_stat
W38B_REPLACEMENTS["PubSubStatusListener.java"] = _pubsub_stat

# --- QueueTransferService ---

_qts = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 延迟队列/优先级队列的跨节点 {@link QueueTransferTask} 注册表。\n"
        " * <p>同名任务共享实例并通过引用计数复用；计数归零时停止任务。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public void schedule(String name, QueueTransferTask task) {",
        "    /** 注册或复用名为 {@code name} 的转移任务；首次调用时 {@link QueueTransferTask#start()}。 */\n"
        "    public void schedule(String name, QueueTransferTask task) {",
    ),
    (
        "    public void remove(String name) {",
        "    /** 递减引用计数；归零时 {@link QueueTransferTask#stop()} 并移除。 */\n"
        "    public void remove(String name) {",
    ),
]
W38B_REPLACEMENTS[f"{_R}QueueTransferService.java"] = _qts
W38B_REPLACEMENTS["QueueTransferService.java"] = _qts

# --- QueueTransferTask ---

_qtt = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 抽象队列转移任务：通过 {@link RTopic} 协调多节点上的 push/schedule。\n"
        " * <p>订阅调度主题后，按远端下发的 startTime 延迟执行 {@link #pushTaskAsync()}。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public static class TimeoutTask {",
        "    /** 记录 Netty 延迟任务及其计划开始时间。 */\n"
        "    public static class TimeoutTask {",
    ),
    (
        "    public QueueTransferTask(ServiceManager serviceManager) {",
        "    /** @param serviceManager 提供定时器与关闭检测 */\n"
        "    public QueueTransferTask(ServiceManager serviceManager) {",
    ),
    (
        "    public void incUsage() {",
        "    /** 增加引用计数（{@link QueueTransferService} 复用时调用）。 */\n"
        "    public void incUsage() {",
    ),
    (
        "    public int decUsage() {",
        "    /** 递减引用计数并返回当前值。 */\n"
        "    public int decUsage() {",
    ),
    (
        "    public void start() {",
        "    /** 订阅调度 Topic：就绪时 push，收到 startTime 时 schedule。 */\n"
        "    public void start() {",
    ),
    (
        "    public void stop() {",
        "    /** 移除监听器并取消未执行的延迟任务。 */\n"
        "    public void stop() {",
    ),
    (
        "    private void scheduleTask(final Long startTime) {",
        "    /** 按 startTime 与当前时间差安排 push；过近则立即执行。 */\n"
        "    private void scheduleTask(final Long startTime) {",
    ),
    (
        "    protected abstract RTopic getTopic();",
        "    /** 返回本队列使用的调度 {@link RTopic}。 */\n"
        "    protected abstract RTopic getTopic();",
    ),
    (
        "    protected abstract RFuture<Long> pushTaskAsync();",
        "    /** 执行一次队列转移并返回下次计划的 startTime（可为 null）。 */\n"
        "    protected abstract RFuture<Long> pushTaskAsync();",
    ),
    (
        "    private void pushTask() {",
        "    /** 异步 push；失败时 5 秒后重试。 */\n"
        "    private void pushTask() {",
    ),
]
W38B_REPLACEMENTS[f"{_R}QueueTransferTask.java"] = _qtt
W38B_REPLACEMENTS["QueueTransferTask.java"] = _qtt

# --- RedissonAtomicDouble ---

_rad = [
    (
        "/**\n * Distributed alternative to the {@link java.util.concurrent.atomic.AtomicLong}\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 基于 Redis {@code INCRBYFLOAT} 的分布式 {@link RAtomicDouble} 实现。\n"
        " * <p>提供线程安全的分布式浮点计数器，支持 CAS、条件删除与带界递增。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public RedissonAtomicDouble(CommandAsyncExecutor commandExecutor, String name) {",
        "    /** @param commandExecutor 异步 Redis 命令执行器\n"
        "     *  @param name Redis 键名\n"
        "     */\n"
        "    public RedissonAtomicDouble(CommandAsyncExecutor commandExecutor, String name) {",
    ),
    (
        "    @Override\n    public RFuture<Boolean> compareAndDeleteAsync(CompareAndDeleteArgs args) {",
        "    /** Lua：当前值满足比较条件时删除键。 */\n"
        "    @Override\n"
        "    public RFuture<Boolean> compareAndDeleteAsync(CompareAndDeleteArgs args) {",
    ),
    (
        "    @Override\n    public RFuture<Double> addAndGetAsync(double delta) {",
        "    /** {@code INCRBYFLOAT} 原子加 delta 并返回新值。 */\n"
        "    @Override\n"
        "    public RFuture<Double> addAndGetAsync(double delta) {",
    ),
    (
        "    @Override\n    public RFuture<Boolean> compareAndSetAsync(double expect, double update) {",
        "    /** Lua CAS：当前值等于 expect（或键不存在且 expect 为 0）时设为 update。 */\n"
        "    @Override\n"
        "    public RFuture<Boolean> compareAndSetAsync(double expect, double update) {",
    ),
    (
        "    @Override\n    public RFuture<Double> getAsync() {",
        "    /** 读取当前浮点值；键不存在时返回 0。 */\n"
        "    @Override\n"
        "    public RFuture<Double> getAsync() {",
    ),
    (
        "    @Override\n    public RFuture<Double> incrementAndGetAsync(DoubleIncrementArgs args) {",
        "    /** {@code INCREX} 带上下界、饱和与过期参数的浮点递增。 */\n"
        "    @Override\n"
        "    public RFuture<Double> incrementAndGetAsync(DoubleIncrementArgs args) {",
    ),
    (
        "    @Override\n    public RFuture<Void> setAsync(double newValue) {",
        "    /** 直接 SET 新值。 */\n"
        "    @Override\n"
        "    public RFuture<Void> setAsync(double newValue) {",
    ),
    (
        "    @Override\n    public RFuture<Boolean> setIfLessAsync(double less, double value) {",
        "    /** 当前值小于 {@code less} 时设为 {@code value}。 */\n"
        "    @Override\n"
        "    public RFuture<Boolean> setIfLessAsync(double less, double value) {",
    ),
    (
        "    @Override\n    public RFuture<Boolean> setIfGreaterAsync(double greater, double value) {",
        "    /** 当前值大于 {@code greater} 时设为 {@code value}。 */\n"
        "    @Override\n"
        "    public RFuture<Boolean> setIfGreaterAsync(double greater, double value) {",
    ),
    (
        "    @Override\n    public int addListener(ObjectListener listener) {",
        "    /** {@link IncrByListener} 订阅 {@code __keyevent@*:incrby} 键空间通知。 */\n"
        "    @Override\n"
        "    public int addListener(ObjectListener listener) {",
    ),
]
W38B_REPLACEMENTS[f"{_R}RedissonAtomicDouble.java"] = _rad
W38B_REPLACEMENTS["RedissonAtomicDouble.java"] = _rad

# --- RedissonAtomicLong ---

_ral = [
    (
        "/**\n * Distributed alternative to the {@link java.util.concurrent.atomic.AtomicLong}\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 基于 Redis {@code INCR/DECR/INCRBY} 的分布式 {@link RAtomicLong} 实现。\n"
        " * <p>对标 {@link java.util.concurrent.atomic.AtomicLong}，支持 CAS、条件删除与带界递增。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public RedissonAtomicLong(CommandAsyncExecutor commandExecutor, String name) {",
        "    /** @param commandExecutor 异步 Redis 命令执行器\n"
        "     *  @param name Redis 键名\n"
        "     */\n"
        "    public RedissonAtomicLong(CommandAsyncExecutor commandExecutor, String name) {",
    ),
    (
        "    @Override\n    public RFuture<Boolean> compareAndDeleteAsync(CompareAndDeleteArgs args) {",
        "    /** Lua：当前值满足比较条件时删除键。 */\n"
        "    @Override\n"
        "    public RFuture<Boolean> compareAndDeleteAsync(CompareAndDeleteArgs args) {",
    ),
    (
        "    @Override\n    public RFuture<Long> addAndGetAsync(long delta) {",
        "    /** {@code INCRBY} 原子加 delta 并返回新值。 */\n"
        "    @Override\n"
        "    public RFuture<Long> addAndGetAsync(long delta) {",
    ),
    (
        "    @Override\n    public RFuture<Boolean> compareAndSetAsync(long expect, long update) {",
        "    /** Lua CAS：当前值等于 expect（或键不存在且 expect 为 0）时设为 update。 */\n"
        "    @Override\n"
        "    public RFuture<Boolean> compareAndSetAsync(long expect, long update) {",
    ),
    (
        "    @Override\n    public RFuture<Long> getAsync() {",
        "    /** 读取当前长整型值；键不存在时返回 0。 */\n"
        "    @Override\n"
        "    public RFuture<Long> getAsync() {",
    ),
    (
        "    @Override\n    public RFuture<Long> incrementAndGetAsync(LongIncrementArgs args) {",
        "    /** {@code INCREX} 带上下界、饱和与过期参数的整型递增。 */\n"
        "    @Override\n"
        "    public RFuture<Long> incrementAndGetAsync(LongIncrementArgs args) {",
    ),
    (
        "    @Override\n    public RFuture<Void> setAsync(long newValue) {",
        "    /** 直接 SET 新值。 */\n"
        "    @Override\n"
        "    public RFuture<Void> setAsync(long newValue) {",
    ),
    (
        "    @Override\n    public RFuture<Boolean> setIfLessAsync(long less, long value) {",
        "    /** 当前值小于 {@code less} 时设为 {@code value}。 */\n"
        "    @Override\n"
        "    public RFuture<Boolean> setIfLessAsync(long less, long value) {",
    ),
    (
        "    @Override\n    public RFuture<Boolean> setIfGreaterAsync(long greater, long value) {",
        "    /** 当前值大于 {@code greater} 时设为 {@code value}。 */\n"
        "    @Override\n"
        "    public RFuture<Boolean> setIfGreaterAsync(long greater, long value) {",
    ),
    (
        "    @Override\n    public int addListener(ObjectListener listener) {",
        "    /** {@link IncrByListener} 订阅 {@code __keyevent@*:incrby} 键空间通知。 */\n"
        "    @Override\n"
        "    public int addListener(ObjectListener listener) {",
    ),
]
W38B_REPLACEMENTS[f"{_R}RedissonAtomicLong.java"] = _ral
W38B_REPLACEMENTS["RedissonAtomicLong.java"] = _ral

# --- RedissonBaseAdder ---

_rba = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * {@link org.redisson.api.RLongAdder}/{@link org.redisson.api.RDoubleAdder} 的抽象基类。\n"
        " * <p>通过 {@link RTopic} 广播 sum/reset 消息，各节点上报局部计数后由发起方汇总；\n"
        " * 使用 {@link RSemaphore} 同步多节点响应完成。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public RedissonBaseAdder(CommandAsyncExecutor commandExecutor, String name, RedissonClient redisson) {",
        "    /** 注册 Topic 监听器并递增 {@link AdderEntry} 引用计数。\n"
        "     *  @param redisson 用于获取 per-operation 信号量\n"
        "     */\n"
        "    public RedissonBaseAdder(CommandAsyncExecutor commandExecutor, String name, RedissonClient redisson) {",
    ),
    (
        "    private void release(String id, AdderEntry entry) {",
        "    /** 各节点处理完 sum/reset 后递减计数；全部完成则释放信号量。 */\n"
        "    private void release(String id, AdderEntry entry) {",
    ),
    (
        "    protected abstract void doReset();",
        "    /** 子类实现：将本地计数器清零。 */\n"
        "    protected abstract void doReset();",
    ),
    (
        "    public RFuture<T> sumAsync() {",
        "    /** 广播 SUM 消息，等待各节点上报后合并并删除临时计数键。 */\n"
        "    public RFuture<T> sumAsync() {",
    ),
    (
        "    public RFuture<T> sumAsync(long timeout, TimeUnit timeUnit) {",
        "    /** 带超时的 sum；超时抛出 {@link TimeoutException}。 */\n"
        "    public RFuture<T> sumAsync(long timeout, TimeUnit timeUnit) {",
    ),
    (
        "    public RFuture<Void> resetAsync() {",
        "    /** 广播 CLEAR 消息，各节点 reset 后释放信号量。 */\n"
        "    public RFuture<Void> resetAsync() {",
    ),
    (
        "    public void destroy() {",
        "    /** 移除 Topic 监听器并在引用计数归零时清理未完成操作。 */\n"
        "    public void destroy() {",
    ),
    (
        "    protected abstract RFuture<T> addAndGetAsync(String id);",
        "    /** 子类实现：将本地增量累加到 id 对应的临时键并返回。 */\n"
        "    protected abstract RFuture<T> addAndGetAsync(String id);",
    ),
    (
        "    protected abstract RFuture<T> getAndDeleteAsync(String id);",
        "    /** 子类实现：读取并删除 id 对应的临时汇总键。 */\n"
        "    protected abstract RFuture<T> getAndDeleteAsync(String id);",
    ),
]
W38B_REPLACEMENTS[f"{_R}RedissonBaseAdder.java"] = _rba
W38B_REPLACEMENTS["RedissonBaseAdder.java"] = _rba
