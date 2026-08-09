"""RocketMQ 5.5.0 wave12b common PopAck/Service/Topic/Action [15:30] replacements."""

R: dict[str, list[tuple[str, str]]] = {}

R["common/src/main/java/org/apache/rocketmq/common/PopAckConstants.java"] = [
    (
        "public class PopAckConstants {",
        "/**\n * Pop 消费 Ack/Checkpoint 相关常量：Ack 间隔、锁时长、Revive 主题/组名及 CK/ACK 消息 Tag。\n * 用于 Pop 模式下的可见性超时、重试与死信复活链路。\n */\npublic class PopAckConstants {",
    ),
    (
        "    public static long ackTimeInterval = 1000;",
        "    /** Ack 时间间隔（毫秒），默认 1 秒。 */\n    public static long ackTimeInterval = 1000;",
    ),
    (
        "    public static final long SECOND = 1000;",
        "    /** 一秒对应的毫秒数。 */\n    public static final long SECOND = 1000;",
    ),
    (
        "    public static long lockTime = 5000;",
        "    /** Pop 消息锁定时长（毫秒），默认 5 秒。 */\n    public static long lockTime = 5000;",
    ),
    (
        "    public static int retryQueueNum = 1;",
        "    /** Pop 重试队列数量。 */\n    public static int retryQueueNum = 1;",
    ),
    (
        "    public static final String REVIVE_GROUP = MixAll.CID_RMQ_SYS_PREFIX + \"REVIVE_GROUP\";",
        "    /** Revive 消费组名（系统前缀 + REVIVE_GROUP）。 */\n    public static final String REVIVE_GROUP = MixAll.CID_RMQ_SYS_PREFIX + \"REVIVE_GROUP\";",
    ),
    (
        "    public static final String LOCAL_HOST = \"127.0.0.1\";",
        "    /** 本地主机占位地址。 */\n    public static final String LOCAL_HOST = \"127.0.0.1\";",
    ),
    (
        "    public static final String REVIVE_TOPIC = TopicValidator.SYSTEM_TOPIC_PREFIX + \"REVIVE_LOG_\";",
        "    /** Revive 日志 Topic 前缀（系统 Topic 前缀 + REVIVE_LOG_）。 */\n    public static final String REVIVE_TOPIC = TopicValidator.SYSTEM_TOPIC_PREFIX + \"REVIVE_LOG_\";",
    ),
    (
        "    public static final String CK_TAG = \"ck\";",
        "    /** Checkpoint 消息 Tag。 */\n    public static final String CK_TAG = \"ck\";",
    ),
    (
        "    public static final String ACK_TAG = \"ack\";",
        "    /** 单条 Ack 消息 Tag。 */\n    public static final String ACK_TAG = \"ack\";",
    ),
    (
        "    public static final String BATCH_ACK_TAG = \"bAck\";",
        "    /** 批量 Ack 消息 Tag。 */\n    public static final String BATCH_ACK_TAG = \"bAck\";",
    ),
    (
        "    public static final String SPLIT = \"@\";",
        "    /** Revive/Ack 载荷字段分隔符。 */\n    public static final String SPLIT = \"@\";",
    ),
    (
        "    /**\n     * Build cluster revive topic\n     *\n     * @param clusterName cluster name\n     * @return revive topic\n     */",
        "    /**\n     * 构造集群级 Revive Topic 名称。\n     *\n     * @param clusterName 集群名\n     * @return Revive Topic 全名\n     */",
    ),
    (
        "    public static boolean isStartWithRevivePrefix(String topicName) {",
        "    /** 判断 Topic 是否以 Revive 前缀开头。 */\n    public static boolean isStartWithRevivePrefix(String topicName) {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/ServiceState.java"] = [
    (
        "public enum ServiceState {",
        "/**\n * RocketMQ 内部服务生命周期状态枚举。\n */\npublic enum ServiceState {",
    ),
    (
        "    /**\n     * Service just created,not start\n     */",
        "    /** 服务已创建但尚未启动。 */",
    ),
    (
        "    /**\n     * Service Running\n     */",
        "    /** 服务正在运行。 */",
    ),
    (
        "    /**\n     * Service shutdown\n     */",
        "    /** 服务已关闭。 */",
    ),
    (
        "    /**\n     * Service Start failure\n     */",
        "    /** 服务启动失败。 */",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/ServiceThread.java"] = [
    (
        "public abstract class ServiceThread implements Runnable {",
        "/**\n * 后台服务线程基类：封装启动/停止、可中断等待与唤醒机制。\n * 子类在 {@link #run()} 中循环调用 {@link #waitForRunning(long)} 实现定时任务。\n */\npublic abstract class ServiceThread implements Runnable {",
    ),
    (
        "    private static final long JOIN_TIME = 90 * 1000;",
        "    /** shutdown 时 join 线程的默认超时（毫秒）。 */\n    private static final long JOIN_TIME = 90 * 1000;",
    ),
    (
        "    protected Thread thread;",
        "    /** 实际执行 {@link Runnable} 的工作线程。 */\n    protected Thread thread;",
    ),
    (
        "    protected final CountDownLatch2 waitPoint = new CountDownLatch2(1);",
        "    /** 等待/唤醒同步点，配合 {@link #wakeup()} 提前结束 sleep。 */\n    protected final CountDownLatch2 waitPoint = new CountDownLatch2(1);",
    ),
    (
        "    protected volatile AtomicBoolean hasNotified = new AtomicBoolean(false);",
        "    /** 是否已被 {@link #wakeup()} 唤醒，避免重复 countDown。 */\n    protected volatile AtomicBoolean hasNotified = new AtomicBoolean(false);",
    ),
    (
        "    protected volatile boolean stopped = false;",
        "    /** 停止标志，子类循环中应检查此字段。 */\n    protected volatile boolean stopped = false;",
    ),
    (
        "    protected boolean isDaemon = false;",
        "    /** 工作线程是否为守护线程。 */\n    protected boolean isDaemon = false;",
    ),
    (
        "    //Make it able to restart the thread",
        "    // 允许 stop 后再次 start",
    ),
    (
        "    private final AtomicBoolean started = new AtomicBoolean(false);",
        "    /** 是否已启动，保证 start/shutdown 幂等。 */\n    private final AtomicBoolean started = new AtomicBoolean(false);",
    ),
    (
        "    public String getServiceName() {",
        "    /** 返回服务名（默认类简单名），用作线程名。 */\n    public String getServiceName() {",
    ),
    (
        "    public void start() {",
        "    /** 启动后台线程；重复调用会被忽略。 */\n    public void start() {",
    ),
    (
        "    public void shutdown() {",
        "    /** 优雅停止：不 interrupt，等待 join 超时。 */\n    public void shutdown() {",
    ),
    (
        "    public void shutdown(final boolean interrupt) {",
        "    /** 停止服务；{@code interrupt=true} 时中断阻塞中的线程。 */\n    public void shutdown(final boolean interrupt) {",
    ),
    (
        "        //if thead is waiting, wakeup it",
        "        // 若线程正在 waitForRunning，先唤醒以便尽快退出",
    ),
    (
        "    public long getJoinTime() {",
        "    /** shutdown 时 join 的超时毫秒数，子类可覆盖。 */\n    public long getJoinTime() {",
    ),
    (
        "    public void makeStop() {",
        "    /** 仅置 stopped 标志，不 join 线程（软停止）。 */\n    public void makeStop() {",
    ),
    (
        "    public void wakeup() {",
        "    /** 唤醒正在 {@link #waitForRunning(long)} 的线程。 */\n    public void wakeup() {",
    ),
    (
        "    protected void waitForRunning(long interval) {",
        "    /** 等待 {@code interval} 毫秒或被 wakeup；结束时调用 {@link #onWaitEnd()}。 */\n    protected void waitForRunning(long interval) {",
    ),
    (
        "        //entry to wait",
        "        // 进入等待前先 reset latch",
    ),
    (
        "    protected void onWaitEnd() {",
        "    /** 每次 waitForRunning 结束时的钩子，子类可覆写执行周期任务。 */\n    protected void onWaitEnd() {",
    ),
    (
        "    public boolean isStopped() {",
        "    /** 是否已请求停止。 */\n    public boolean isStopped() {",
    ),
    (
        "    public boolean isDaemon() {",
        "    /** 工作线程是否为守护线程。 */\n    public boolean isDaemon() {",
    ),
    (
        "    public void setDaemon(boolean daemon) {",
        "    /** 设置下次 start 时线程的 daemon 属性。 */\n    public void setDaemon(boolean daemon) {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/SubscriptionGroupAttributes.java"] = [
    (
        "public class SubscriptionGroupAttributes {",
        "/**\n * 消费组可配置属性定义：优先级因子、Lite 订阅模型/配额/通配符等。\n * 所有属性注册在 {@link #ALL} 供 Broker 校验与默认值查询。\n */\npublic class SubscriptionGroupAttributes {",
    ),
    (
        "    public static final Map<String, Attribute> ALL;",
        "    /** 属性名 → {@link Attribute} 定义的全局注册表。 */\n    public static final Map<String, Attribute> ALL;",
    ),
    (
        "    public static final LongRangeAttribute PRIORITY_FACTOR_ATTRIBUTE = new LongRangeAttribute(",
        "    /** 优先级因子：0 关闭优先级模式，1–100 启用并调节权重。 */\n    public static final LongRangeAttribute PRIORITY_FACTOR_ATTRIBUTE = new LongRangeAttribute(",
    ),
    (
        "        0, // disable priority mode",
        "        0, // 0 表示关闭优先级模式",
    ),
    (
        "        100, // enable priority mode",
        "        100, // 100 表示完全启用优先级模式",
    ),
    (
        "    public static final StringAttribute LITE_BIND_TOPIC_ATTRIBUTE = new StringAttribute(",
        "    /** Lite 订阅绑定的 Topic 名称。 */\n    public static final StringAttribute LITE_BIND_TOPIC_ATTRIBUTE = new StringAttribute(",
    ),
    (
        "    public static final EnumAttribute LITE_SUB_MODEL_ATTRIBUTE = new EnumAttribute(",
        "    /** Lite 订阅模型：Shared（共享）或 Exclusive（独占）。 */\n    public static final EnumAttribute LITE_SUB_MODEL_ATTRIBUTE = new EnumAttribute(",
    ),
    (
        "    public static final BooleanAttribute LITE_SUB_RESET_OFFSET_EXCLUSIVE_ATTRIBUTE = new BooleanAttribute(",
        "    /** 独占 Lite 订阅是否在 reset offset 时生效。 */\n    public static final BooleanAttribute LITE_SUB_RESET_OFFSET_EXCLUSIVE_ATTRIBUTE = new BooleanAttribute(",
    ),
    (
        "    public static final BooleanAttribute LITE_SUB_RESET_OFFSET_UNSUBSCRIBE_ATTRIBUTE = new BooleanAttribute(",
        "    /** 取消订阅时是否 reset Lite 订阅 offset。 */\n    public static final BooleanAttribute LITE_SUB_RESET_OFFSET_UNSUBSCRIBE_ATTRIBUTE = new BooleanAttribute(",
    ),
    (
        "    /**\n     * client-side lite subscription quota limit\n     */",
        "    /** 客户端 Lite 订阅配额上限（-1 表示不限制）。 */",
    ),
    (
        "    public static final LongRangeAttribute LITE_SUB_CLIENT_MAX_EVENT_COUNT_ATTRIBUTE = new LongRangeAttribute(",
        "    /** 客户端 Lite 订阅最大事件缓存条数。 */\n    public static final LongRangeAttribute LITE_SUB_CLIENT_MAX_EVENT_COUNT_ATTRIBUTE = new LongRangeAttribute(",
    ),
    (
        "    public static final StringAttribute LITE_SUB_WILDCARD_ATTRIBUTE = new StringAttribute(",
        "    /** Lite 订阅通配符表达式。 */\n    public static final StringAttribute LITE_SUB_WILDCARD_ATTRIBUTE = new StringAttribute(",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/SystemClock.java"] = [
    (
        "public class SystemClock {",
        "/**\n * 系统时钟抽象：便于测试注入 mock 时间。\n */\npublic class SystemClock {",
    ),
    (
        "    public long now() {",
        "    /** 返回当前毫秒时间戳（{@link System#currentTimeMillis()}）。 */\n    public long now() {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/ThreadFactoryImpl.java"] = [
    (
        "public class ThreadFactoryImpl implements ThreadFactory {",
        "/**\n * RocketMQ 通用线程工厂：按前缀递增编号命名线程，可选 daemon 与 Broker 容器标识前缀。\n * 为未捕获异常注册统一日志处理器。\n */\npublic class ThreadFactoryImpl implements ThreadFactory {",
    ),
    (
        "    private final AtomicLong threadIndex = new AtomicLong(0);",
        "    /** 线程序号，用于生成唯一线程名后缀。 */\n    private final AtomicLong threadIndex = new AtomicLong(0);",
    ),
    (
        "    private final String threadNamePrefix;",
        "    /** 线程名前缀。 */\n    private final String threadNamePrefix;",
    ),
    (
        "    private final boolean daemon;",
        "    /** 新建线程是否为守护线程。 */\n    private final boolean daemon;",
    ),
    (
        "    public ThreadFactoryImpl(final String threadNamePrefix) {",
        "    /** 非 daemon 线程工厂。 */\n    public ThreadFactoryImpl(final String threadNamePrefix) {",
    ),
    (
        "    public ThreadFactoryImpl(final String threadNamePrefix, boolean daemon) {",
        "    /** 指定 daemon 属性的线程工厂。 */\n    public ThreadFactoryImpl(final String threadNamePrefix, boolean daemon) {",
    ),
    (
        "    public ThreadFactoryImpl(final String threadNamePrefix, BrokerIdentity brokerIdentity) {",
        "    /** Broker 容器模式下自动附加 Broker 标识前缀。 */\n    public ThreadFactoryImpl(final String threadNamePrefix, BrokerIdentity brokerIdentity) {",
    ),
    (
        "    public ThreadFactoryImpl(final String threadNamePrefix, boolean daemon, BrokerIdentity brokerIdentity) {",
        "    /** 完整构造：前缀、daemon 与 Broker 标识。 */\n    public ThreadFactoryImpl(final String threadNamePrefix, boolean daemon, BrokerIdentity brokerIdentity) {",
    ),
    (
        "        // Log all uncaught exception",
        "        // 记录所有未捕获异常，便于排查后台线程 BUG",
    ),
    (
        "    public Thread newThread(Runnable r) {",
        "    /** 创建命名线程并设置 uncaughtExceptionHandler。 */\n    public Thread newThread(Runnable r) {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/TopicAttributes.java"] = [
    (
        "public class TopicAttributes {",
        "/**\n * Topic 级可配置属性：队列类型、清理策略、消息类型、保留/过期时间等。\n * 定义注册在 {@link #ALL} 供创建与更新 Topic 时校验。\n */\npublic class TopicAttributes {",
    ),
    (
        "    public static final EnumAttribute QUEUE_TYPE_ATTRIBUTE = new EnumAttribute(",
        "    /** 队列实现类型：BatchCQ 或 SimpleCQ。 */\n    public static final EnumAttribute QUEUE_TYPE_ATTRIBUTE = new EnumAttribute(",
    ),
    (
        "    public static final EnumAttribute CLEANUP_POLICY_ATTRIBUTE = new EnumAttribute(",
        "    /** 清理策略：DELETE（删除）或 COMPACTION（压缩）。 */\n    public static final EnumAttribute CLEANUP_POLICY_ATTRIBUTE = new EnumAttribute(",
    ),
    (
        "    public static final EnumAttribute TOPIC_MESSAGE_TYPE_ATTRIBUTE = new EnumAttribute(",
        "    /** Topic 消息类型（NORMAL、FIFO、DELAY、TRANSACTION、LITE 等）。 */\n    public static final EnumAttribute TOPIC_MESSAGE_TYPE_ATTRIBUTE = new EnumAttribute(",
    ),
    (
        "    public static final LongRangeAttribute TOPIC_RESERVE_TIME_ATTRIBUTE = new LongRangeAttribute(",
        "    /** Topic 消息保留时间（分钟，-1 表示使用 Broker 默认）。 */\n    public static final LongRangeAttribute TOPIC_RESERVE_TIME_ATTRIBUTE = new LongRangeAttribute(",
    ),
    (
        "    public static final LongRangeAttribute LITE_EXPIRATION_ATTRIBUTE = new LongRangeAttribute(",
        "    /** Lite Topic 过期时间（分钟，上限 30 天，-1 表示不限制）。 */\n    public static final LongRangeAttribute LITE_EXPIRATION_ATTRIBUTE = new LongRangeAttribute(",
    ),
    (
        "    public static final Map<String, Attribute> ALL;",
        "    /** 属性名 → {@link Attribute} 定义的全局注册表。 */\n    public static final Map<String, Attribute> ALL;",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/TopicConfig.java"] = [
    (
        "public class TopicConfig {",
        "/**\n * Topic 元数据配置：读写队列数、权限、过滤类型、顺序标志及扩展属性。\n * 支持空格分隔字符串编解码，属性 Map 以 JSON 附加在末尾字段。\n */\npublic class TopicConfig {",
    ),
    (
        "    private static final String SEPARATOR = \" \";",
        "    /** encode/decode 字段分隔符（属性 JSON 内不得含空格）。 */\n    private static final String SEPARATOR = \" \";",
    ),
    (
        "    public static int defaultReadQueueNums = 16;",
        "    /** 默认读队列数量。 */\n    public static int defaultReadQueueNums = 16;",
    ),
    (
        "    public static int defaultWriteQueueNums = 16;",
        "    /** 默认写队列数量。 */\n    public static int defaultWriteQueueNums = 16;",
    ),
    (
        "    private String topicName;",
        "    /** Topic 名称。 */\n    private String topicName;",
    ),
    (
        "    private int readQueueNums = defaultReadQueueNums;",
        "    /** 读队列数量。 */\n    private int readQueueNums = defaultReadQueueNums;",
    ),
    (
        "    private int writeQueueNums = defaultWriteQueueNums;",
        "    /** 写队列数量。 */\n    private int writeQueueNums = defaultWriteQueueNums;",
    ),
    (
        "    private int perm = PermName.PERM_READ | PermName.PERM_WRITE;",
        "    /** 权限位（读/写/继承等，见 {@link PermName}）。 */\n    private int perm = PermName.PERM_READ | PermName.PERM_WRITE;",
    ),
    (
        "    private TopicFilterType topicFilterType = TopicFilterType.SINGLE_TAG;",
        "    /** Tag 过滤类型（单 Tag / 多 Tag）。 */\n    private TopicFilterType topicFilterType = TopicFilterType.SINGLE_TAG;",
    ),
    (
        "    private int topicSysFlag = 0;",
        "    /** Topic 系统标志位。 */\n    private int topicSysFlag = 0;",
    ),
    (
        "    private boolean order = false;",
        "    /** 是否为顺序 Topic。 */\n    private boolean order = false;",
    ),
    (
        "    // Field attributes should not have ' ' char in key or value, otherwise will lead to decode failure.",
        "    // 属性键值不得含空格，否则 decode 时 split 会失败",
    ),
    (
        "    private Map<String, String> attributes = new HashMap<>();",
        "    /** 扩展属性（message.type、lite.topic.expiration 等）。 */\n    private Map<String, String> attributes = new HashMap<>();",
    ),
    (
        "    public String encode() {",
        "    /** 编码为「topic read write perm filterType [attributesJson]」空格分隔串。 */\n    public String encode() {",
    ),
    (
        "        //[0]",
        "        //[0] topicName",
    ),
    (
        "        //[1]",
        "        //[1] readQueueNums",
    ),
    (
        "        //[2]",
        "        //[2] writeQueueNums",
    ),
    (
        "        //[3]",
        "        //[3] perm",
    ),
    (
        "        //[4]",
        "        //[4] topicFilterType",
    ),
    (
        "        //[5]",
        "        //[5] attributes JSON（可选）",
    ),
    (
        "    public boolean decode(final String in) {",
        "    /** 从 encode 字符串解析；至少 5 段，第 6 段为 attributes JSON。 */\n    public boolean decode(final String in) {",
    ),
    (
        "                    // ignore exception when parse failed, cause map's key/value can have ' ' char.",
        "                    // 解析失败时忽略，因旧数据或键值可能含空格导致 JSON 段不完整",
    ),
    (
        "    @JSONField(serialize = false, deserialize = false)\n    public TopicMessageType getTopicMessageType() {",
        "    /** 从 attributes 读取 {@link TopicMessageType}，缺省为 NORMAL。 */\n    @JSONField(serialize = false, deserialize = false)\n    public TopicMessageType getTopicMessageType() {",
    ),
    (
        "    @JSONField(serialize = false, deserialize = false)\n    public void setTopicMessageType(TopicMessageType topicMessageType) {",
        "    /** 写入 message.type 属性。 */\n    @JSONField(serialize = false, deserialize = false)\n    public void setTopicMessageType(TopicMessageType topicMessageType) {",
    ),
    (
        "    @JSONField(serialize = false, deserialize = false)\n    public void setLiteTopicExpiration(int liteTopicExpiration) {",
        "    /** 仅 LITE 类型 Topic 设置 lite.topic.expiration（分钟）。 */\n    @JSONField(serialize = false, deserialize = false)\n    public void setLiteTopicExpiration(int liteTopicExpiration) {",
    ),
    (
        "    @JSONField(serialize = false, deserialize = false)\n    public int getLiteTopicExpiration() {",
        "    /** 读取 LITE Topic 过期分钟数，非 LITE 或缺失时返回 -1。 */\n    @JSONField(serialize = false, deserialize = false)\n    public int getLiteTopicExpiration() {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/TopicFilterType.java"] = [
    (
        "public enum TopicFilterType {",
        "/**\n * Topic 订阅 Tag 过滤模式。\n */\npublic enum TopicFilterType {",
    ),
    (
        "    SINGLE_TAG,",
        "    /** 单 Tag 过滤（SQL92 仅支持一个 Tag 表达式）。 */\n    SINGLE_TAG,",
    ),
    (
        "    MULTI_TAG",
        "    /** 多 Tag 过滤。 */\n    MULTI_TAG",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/TopicQueueId.java"] = [
    (
        "public class TopicQueueId {",
        "/**\n * Topic 与队列 ID 的不可变组合键，用于 HashMap 索引与相等比较。\n * hashCode 在构造时预计算以提升热点路径性能。\n */\npublic class TopicQueueId {",
    ),
    (
        "    private final String topic;",
        "    /** Topic 名称。 */\n    private final String topic;",
    ),
    (
        "    private final int queueId;",
        "    /** 队列 ID。 */\n    private final int queueId;",
    ),
    (
        "    private final int hash;",
        "    /** 预计算的 hash(topic, queueId)。 */\n    private final int hash;",
    ),
    (
        "    public TopicQueueId(String topic, int queueId) {",
        "    /** 构造 Topic+queueId 键并缓存 hashCode。 */\n    public TopicQueueId(String topic, int queueId) {",
    ),
    (
        "    public boolean equals(Object o) {",
        "    /** 按 topic 与 queueId 相等比较。 */\n    public boolean equals(Object o) {",
    ),
    (
        "    public int hashCode() {",
        "    /** 返回构造时缓存的 hash 值。 */\n    public int hashCode() {",
    ),
    (
        "    public String toString() {",
        "    /** 调试字符串（类名沿用 MessageQueueInBroker 历史命名）。 */\n    public String toString() {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/UnlockCallback.java"] = [
    (
        "public interface UnlockCallback {",
        "/**\n * 分布式锁解锁异步回调：成功或异常时通知调用方。\n */\npublic interface UnlockCallback {",
    ),
    (
        "    void onSuccess();",
        "    /** 解锁成功。 */\n    void onSuccess();",
    ),
    (
        "    void onException(final Throwable e);",
        "    /** 解锁失败并携带异常。 */\n    void onException(final Throwable e);",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/action/Action.java"] = [
    (
        "public enum Action {",
        "/**\n * ACL/鉴权动作枚举：与 {@link RocketMQAction} 配合描述对资源的操作类型。\n * 支持按名称忽略大小写解析。\n */\npublic enum Action {",
    ),
    (
        "    UNKNOWN((byte) 0, \"Unknown\"),",
        "    /** 未知动作。 */\n    UNKNOWN((byte) 0, \"Unknown\"),",
    ),
    (
        "    ALL((byte) 1, \"All\"),",
        "    /** 全部动作（通配）。 */\n    ALL((byte) 1, \"All\"),",
    ),
    (
        "    ANY((byte) 2, \"Any\"),",
        "    /** 任意动作。 */\n    ANY((byte) 2, \"Any\"),",
    ),
    (
        "    PUB((byte) 3, \"Pub\"),",
        "    /** 发布消息。 */\n    PUB((byte) 3, \"Pub\"),",
    ),
    (
        "    SUB((byte) 4, \"Sub\"),",
        "    /** 订阅/消费。 */\n    SUB((byte) 4, \"Sub\"),",
    ),
    (
        "    CREATE((byte) 5, \"Create\"),",
        "    /** 创建资源。 */\n    CREATE((byte) 5, \"Create\"),",
    ),
    (
        "    UPDATE((byte) 6, \"Update\"),",
        "    /** 更新资源。 */\n    UPDATE((byte) 6, \"Update\"),",
    ),
    (
        "    DELETE((byte) 7, \"Delete\"),",
        "    /** 删除资源。 */\n    DELETE((byte) 7, \"Delete\"),",
    ),
    (
        "    GET((byte) 8, \"Get\"),",
        "    /** 获取单个资源。 */\n    GET((byte) 8, \"Get\"),",
    ),
    (
        "    LIST((byte) 9, \"List\");",
        "    /** 列出资源。 */\n    LIST((byte) 9, \"List\");",
    ),
    (
        "    @JSONField(value = true)\n    private final byte code;",
        "    /** 动作编码，JSON 序列化主键。 */\n    @JSONField(value = true)\n    private final byte code;",
    ),
    (
        "    private final String name;",
        "    /** 动作英文名称。 */\n    private final String name;",
    ),
    (
        "    public static Action getByName(String name) {",
        "    /** 按名称忽略大小写查找，未找到返回 null。 */\n    public static Action getByName(String name) {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/action/RocketMQAction.java"] = [
    (
        "@Retention(RetentionPolicy.RUNTIME)\npublic @interface RocketMQAction {",
        "/**\n * 标注 RPC/管理接口所需的 ACL 动作与资源类型。\n * {@link #value()} 为请求码，{@link #action()} 列出允许的动作集合。\n */\n@Retention(RetentionPolicy.RUNTIME)\npublic @interface RocketMQAction {",
    ),
    (
        "    int value();",
        "    /** 关联的请求码（RequestCode）。 */\n    int value();",
    ),
    (
        "    ResourceType resource() default ResourceType.UNKNOWN;",
        "    /** 受控资源类型，默认 UNKNOWN。 */\n    ResourceType resource() default ResourceType.UNKNOWN;",
    ),
    (
        "    Action[] action();",
        "    /** 本接口允许的一个或多个 {@link Action}。 */\n    Action[] action();",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/annotation/ImportantField.java"] = [
    (
        "@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE})\npublic @interface ImportantField {",
        "/**\n * 标记重要字段/参数/局部变量：供配置持久化、Diff 或审计逻辑识别不可忽略项。\n */\n@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE})\npublic @interface ImportantField {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/attribute/Attribute.java"] = [
    (
        "public abstract class Attribute {",
        "/**\n * Topic/消费组等资源的命名属性抽象基类。\n * 子类实现 {@link #verify(String)} 校验取值，{@link #changeable} 控制是否允许运行时修改。\n */\npublic abstract class Attribute {",
    ),
    (
        "    protected String name;",
        "    /** 属性名（如 queue.type、message.type）。 */\n    protected String name;",
    ),
    (
        "    protected boolean changeable;",
        "    /** 创建后是否允许变更。 */\n    protected boolean changeable;",
    ),
    (
        "    public abstract void verify(String value);",
        "    /** 校验属性值是否合法，非法时抛出异常。 */\n    public abstract void verify(String value);",
    ),
    (
        "    public Attribute(String name, boolean changeable) {",
        "    /** 构造属性定义。 */\n    public Attribute(String name, boolean changeable) {",
    ),
    (
        "    public String getName() {",
        "    /** 属性名。 */\n    public String getName() {",
    ),
    (
        "    public void setName(String name) {",
        "    /** 设置属性名。 */\n    public void setName(String name) {",
    ),
    (
        "    public boolean isChangeable() {",
        "    /** 是否可在运行时修改。 */\n    public boolean isChangeable() {",
    ),
    (
        "    public void setChangeable(boolean changeable) {",
        "    /** 设置是否可变更。 */\n    public void setChangeable(boolean changeable) {",
    ),
]
