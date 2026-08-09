"""RocketMQ 5.5.0 wave16b common stats/sysflag/thread [15:30] replacements."""

R: dict[str, list[tuple[str, str]]] = {}

R["common/src/main/java/org/apache/rocketmq/common/stats/MomentStatsItem.java"] = [
    (
        "public class MomentStatsItem {",
        "/**\n * 瞬时统计项：维护单个 key 的当前累计值，每 5 分钟打印并重置。\n */\npublic class MomentStatsItem {",
    ),
    (
        "    private final AtomicLong value = new AtomicLong(0);",
        "    /** 当前统计周期内的累计值。 */\n    private final AtomicLong value = new AtomicLong(0);",
    ),
    (
        "    private final String statsName;",
        "    /** 统计类别名称（如 TOPIC_PUT_NUMS）。 */\n    private final String statsName;",
    ),
    (
        "    private final String statsKey;",
        "    /** 统计维度 key（如 topic 或 group 名）。 */\n    private final String statsKey;",
    ),
    (
        "    private final ScheduledExecutorService scheduledExecutorService;",
        "    /** 驱动定时打印与清零的调度线程池。 */\n    private final ScheduledExecutorService scheduledExecutorService;",
    ),
    (
        "    private final Logger log;",
        "    /** 输出统计日志的 Logger。 */\n    private final Logger log;",
    ),
    (
        "    private long lastUpdateTimestamp = System.currentTimeMillis();",
        "    /** 最近一次写入统计值的时间戳，用于空闲清理。 */\n    private long lastUpdateTimestamp = System.currentTimeMillis();",
    ),
    (
        "    public MomentStatsItem(String statsName, String statsKey,",
        "    /** 构造瞬时统计项并绑定调度器与日志。 */\n    public MomentStatsItem(String statsName, String statsKey,",
    ),
    (
        "    public void init() {",
        "    /** 注册每 5 分钟打印并重置 value 的定时任务。 */\n    public void init() {",
    ),
    (
        "    public void printAtMinutes() {",
        "    /** 打印当前 5 分钟窗口内的统计值。 */\n    public void printAtMinutes() {",
    ),
    (
        "    public AtomicLong getValue() {",
        "    /** 返回累计值原子计数器。 */\n    public AtomicLong getValue() {",
    ),
    (
        "    public String getStatsKey() {",
        "    /** 返回统计 key。 */\n    public String getStatsKey() {",
    ),
    (
        "    public String getStatsName() {",
        "    /** 返回统计类别名。 */\n    public String getStatsName() {",
    ),
    (
        "    public long getLastUpdateTimestamp() {",
        "    /** 返回最后更新时间戳。 */\n    public long getLastUpdateTimestamp() {",
    ),
    (
        "    public void setLastUpdateTimestamp(long lastUpdateTimestamp) {",
        "    /** 更新最后写入时间戳。 */\n    public void setLastUpdateTimestamp(long lastUpdateTimestamp) {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/stats/MomentStatsItemSet.java"] = [
    (
        "public class MomentStatsItemSet {",
        "/**\n * 瞬时统计项集合：按 statsKey 管理多个 {@link MomentStatsItem}，统一调度打印与清理。\n */\npublic class MomentStatsItemSet {",
    ),
    (
        "    private final ConcurrentMap<String/* key */, MomentStatsItem> statsItemTable =",
        "    /** statsKey → 瞬时统计项映射表。 */\n    private final ConcurrentMap<String/* key */, MomentStatsItem> statsItemTable =",
    ),
    (
        "    public MomentStatsItemSet(String statsName, ScheduledExecutorService scheduledExecutorService, Logger log) {",
        "    /** 构造集合并立即初始化定时打印任务。 */\n    public MomentStatsItemSet(String statsName, ScheduledExecutorService scheduledExecutorService, Logger log) {",
    ),
    (
        "    public ConcurrentMap<String, MomentStatsItem> getStatsItemTable() {",
        "    /** 返回底层统计项表（只读用途）。 */\n    public ConcurrentMap<String, MomentStatsItem> getStatsItemTable() {",
    ),
    (
        "    public String getStatsName() {",
        "    /** 返回统计类别名。 */\n    public String getStatsName() {",
    ),
    (
        "    public void init() {",
        "    /** 注册每 5 分钟遍历打印全部子项的定时任务。 */\n    public void init() {",
    ),
    (
        "    private void printAtMinutes() {",
        "    /** 遍历所有子项执行分钟级打印。 */\n    private void printAtMinutes() {",
    ),
    (
        "    public void setValue(final String statsKey, final int value) {",
        "    /** 设置指定 key 的 int 统计值并刷新时间戳。 */\n    public void setValue(final String statsKey, final int value) {",
    ),
    (
        "    public void setValue(final String statsKey, final long value) {",
        "    /** 设置指定 key 的 long 统计值并刷新时间戳。 */\n    public void setValue(final String statsKey, final long value) {",
    ),
    (
        "    public void delValueByInfixKey(final String statsKey, String separator) {",
        "    /** 删除 key 中包含 separator+statsKey+separator 的统计项。 */\n    public void delValueByInfixKey(final String statsKey, String separator) {",
    ),
    (
        "    public void delValueBySuffixKey(final String statsKey, String separator) {",
        "    /** 删除 key 以 separator+statsKey 结尾的统计项。 */\n    public void delValueBySuffixKey(final String statsKey, String separator) {",
    ),
    (
        "    public MomentStatsItem getAndCreateStatsItem(final String statsKey) {",
        "    /** 获取或懒创建指定 key 的 {@link MomentStatsItem}。 */\n    public MomentStatsItem getAndCreateStatsItem(final String statsKey) {",
    ),
    (
        "    public void cleanResource(int maxStatsIdleTimeInMinutes) {",
        "    /** 移除超过 maxStatsIdleTimeInMinutes 未更新的空闲统计项。 */\n    public void cleanResource(int maxStatsIdleTimeInMinutes) {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/stats/RTStatsItem.java"] = [
    (
        "/**\n * A StatItem for response time, the only difference between from StatsItem is it has a different log output.\n */\npublic class RTStatsItem extends StatsItem {",
        "/**\n * 响应时间（RT）统计项：继承 {@link StatsItem}，日志输出使用 AVGRT 而非 TPS/SUM。\n */\npublic class RTStatsItem extends StatsItem {",
    ),
    (
        "    public RTStatsItem(String statsName, String statsKey, ScheduledExecutorService scheduledExecutorService,",
        "    /** 构造 RT 统计项。 */\n    public RTStatsItem(String statsName, String statsKey, ScheduledExecutorService scheduledExecutorService,",
    ),
    (
        "    /**\n     *   For Response Time stat Item, the print detail should be a little different, TPS and SUM makes no sense.\n     *   And we give a name \"AVGRT\" rather than AVGPT for value getAvgpt()\n      */",
        "    /** RT 统计打印 TIMES 与 AVGRT，不含 SUM/TPS。 */",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/stats/Stats.java"] = [
    (
        "public class Stats {",
        "/**\n * Broker/客户端运行时统计项名称常量，供 {@link StatsItemSet} 与监控上报引用。\n */\npublic class Stats {",
    ),
    (
        "    public static final String QUEUE_PUT_NUMS = \"QUEUE_PUT_NUMS\";",
        "    /** 队列写入消息条数。 */\n    public static final String QUEUE_PUT_NUMS = \"QUEUE_PUT_NUMS\";",
    ),
    (
        "    public static final String QUEUE_PUT_SIZE = \"QUEUE_PUT_SIZE\";",
        "    /** 队列写入消息字节数。 */\n    public static final String QUEUE_PUT_SIZE = \"QUEUE_PUT_SIZE\";",
    ),
    (
        "    public static final String QUEUE_GET_NUMS = \"QUEUE_GET_NUMS\";",
        "    /** 队列读取消息条数。 */\n    public static final String QUEUE_GET_NUMS = \"QUEUE_GET_NUMS\";",
    ),
    (
        "    public static final String QUEUE_GET_SIZE = \"QUEUE_GET_SIZE\";",
        "    /** 队列读取消息字节数。 */\n    public static final String QUEUE_GET_SIZE = \"QUEUE_GET_SIZE\";",
    ),
    (
        "    public static final String TOPIC_PUT_NUMS = \"TOPIC_PUT_NUMS\";",
        "    /** Topic 写入消息条数。 */\n    public static final String TOPIC_PUT_NUMS = \"TOPIC_PUT_NUMS\";",
    ),
    (
        "    public static final String TOPIC_PUT_SIZE = \"TOPIC_PUT_SIZE\";",
        "    /** Topic 写入消息字节数。 */\n    public static final String TOPIC_PUT_SIZE = \"TOPIC_PUT_SIZE\";",
    ),
    (
        "    public static final String GROUP_GET_NUMS = \"GROUP_GET_NUMS\";",
        "    /** 消费组拉取消息条数。 */\n    public static final String GROUP_GET_NUMS = \"GROUP_GET_NUMS\";",
    ),
    (
        "    public static final String GROUP_GET_SIZE = \"GROUP_GET_SIZE\";",
        "    /** 消费组拉取消息字节数。 */\n    public static final String GROUP_GET_SIZE = \"GROUP_GET_SIZE\";",
    ),
    (
        "    public static final String SNDBCK_PUT_NUMS = \"SNDBCK_PUT_NUMS\";",
        "    /** 回退（sendback）写入条数。 */\n    public static final String SNDBCK_PUT_NUMS = \"SNDBCK_PUT_NUMS\";",
    ),
    (
        "    public static final String BROKER_PUT_NUMS = \"BROKER_PUT_NUMS\";",
        "    /** Broker 总写入条数。 */\n    public static final String BROKER_PUT_NUMS = \"BROKER_PUT_NUMS\";",
    ),
    (
        "    public static final String BROKER_GET_NUMS = \"BROKER_GET_NUMS\";",
        "    /** Broker 总读取条数。 */\n    public static final String BROKER_GET_NUMS = \"BROKER_GET_NUMS\";",
    ),
    (
        "    public static final String GROUP_GET_FROM_DISK_NUMS = \"GROUP_GET_FROM_DISK_NUMS\";",
        "    /** 消费组从磁盘读取条数。 */\n    public static final String GROUP_GET_FROM_DISK_NUMS = \"GROUP_GET_FROM_DISK_NUMS\";",
    ),
    (
        "    public static final String GROUP_GET_FROM_DISK_SIZE = \"GROUP_GET_FROM_DISK_SIZE\";",
        "    /** 消费组从磁盘读取字节数。 */\n    public static final String GROUP_GET_FROM_DISK_SIZE = \"GROUP_GET_FROM_DISK_SIZE\";",
    ),
    (
        "    public static final String BROKER_GET_FROM_DISK_NUMS = \"BROKER_GET_FROM_DISK_NUMS\";",
        "    /** Broker 从磁盘读取条数。 */\n    public static final String BROKER_GET_FROM_DISK_NUMS = \"BROKER_GET_FROM_DISK_NUMS\";",
    ),
    (
        "    public static final String BROKER_GET_FROM_DISK_SIZE = \"BROKER_GET_FROM_DISK_SIZE\";",
        "    /** Broker 从磁盘读取字节数。 */\n    public static final String BROKER_GET_FROM_DISK_SIZE = \"BROKER_GET_FROM_DISK_SIZE\";",
    ),
    (
        "    public static final String COMMERCIAL_SEND_TIMES = \"COMMERCIAL_SEND_TIMES\";",
        "    /** 商业版发送次数。 */\n    public static final String COMMERCIAL_SEND_TIMES = \"COMMERCIAL_SEND_TIMES\";",
    ),
    (
        "    public static final String COMMERCIAL_SNDBCK_TIMES = \"COMMERCIAL_SNDBCK_TIMES\";",
        "    /** 商业版回退次数。 */\n    public static final String COMMERCIAL_SNDBCK_TIMES = \"COMMERCIAL_SNDBCK_TIMES\";",
    ),
    (
        "    public static final String COMMERCIAL_RCV_TIMES = \"COMMERCIAL_RCV_TIMES\";",
        "    /** 商业版接收次数。 */\n    public static final String COMMERCIAL_RCV_TIMES = \"COMMERCIAL_RCV_TIMES\";",
    ),
    (
        "    public static final String COMMERCIAL_RCV_EPOLLS = \"COMMERCIAL_RCV_EPOLLS\";",
        "    /** 商业版 epoll 接收轮次。 */\n    public static final String COMMERCIAL_RCV_EPOLLS = \"COMMERCIAL_RCV_EPOLLS\";",
    ),
    (
        "    public static final String COMMERCIAL_SEND_SIZE = \"COMMERCIAL_SEND_SIZE\";",
        "    /** 商业版发送字节数。 */\n    public static final String COMMERCIAL_SEND_SIZE = \"COMMERCIAL_SEND_SIZE\";",
    ),
    (
        "    public static final String COMMERCIAL_RCV_SIZE = \"COMMERCIAL_RCV_SIZE\";",
        "    /** 商业版接收字节数。 */\n    public static final String COMMERCIAL_RCV_SIZE = \"COMMERCIAL_RCV_SIZE\";",
    ),
    (
        "    public static final String COMMERCIAL_PERM_FAILURES = \"COMMERCIAL_PERM_FAILURES\";",
        "    /** 商业版权限校验失败次数。 */\n    public static final String COMMERCIAL_PERM_FAILURES = \"COMMERCIAL_PERM_FAILURES\";",
    ),
    (
        "    public static final String GROUP_GET_FALL_SIZE = \"GROUP_GET_FALL_SIZE\";",
        "    /** 消费组降级拉取字节数。 */\n    public static final String GROUP_GET_FALL_SIZE = \"GROUP_GET_FALL_SIZE\";",
    ),
    (
        "    public static final String GROUP_GET_FALL_TIME = \"GROUP_GET_FALL_TIME\";",
        "    /** 消费组降级拉取耗时。 */\n    public static final String GROUP_GET_FALL_TIME = \"GROUP_GET_FALL_TIME\";",
    ),
    (
        "    public static final String GROUP_GET_LATENCY = \"GROUP_GET_LATENCY\";",
        "    /** 消费组拉取延迟。 */\n    public static final String GROUP_GET_LATENCY = \"GROUP_GET_LATENCY\";",
    ),
    (
        "    public static final String TOPIC_PUT_LATENCY = \"TOPIC_PUT_LATENCY\";",
        "    /** Topic 写入延迟。 */\n    public static final String TOPIC_PUT_LATENCY = \"TOPIC_PUT_LATENCY\";",
    ),
    (
        "    public static final String GROUP_ACK_NUMS = \"GROUP_ACK_NUMS\";",
        "    /** 消费组 ACK 条数。 */\n    public static final String GROUP_ACK_NUMS = \"GROUP_ACK_NUMS\";",
    ),
    (
        "    public static final String GROUP_CK_NUMS = \"GROUP_CK_NUMS\";",
        "    /** 消费组 Checkpoint 条数。 */\n    public static final String GROUP_CK_NUMS = \"GROUP_CK_NUMS\";",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/stats/StatsItem.java"] = [
    (
        "public class StatsItem {",
        "/**\n * 滑动窗口统计项：秒/分/时/日多级采样，计算 TPS、SUM 与平均耗时。\n */\npublic class StatsItem {",
    ),
    (
        "    private final LongAdder value = new LongAdder();",
        "    /** 累计统计值（如字节数或耗时总和）。 */\n    private final LongAdder value = new LongAdder();",
    ),
    (
        "    private final LongAdder times = new LongAdder();",
        "    /** 累计调用次数。 */\n    private final LongAdder times = new LongAdder();",
    ),
    (
        "    private final LinkedList<CallSnapshot> csListMinute = new LinkedList<>();",
        "    /** 分钟级采样快照链表（约 70 秒窗口）。 */\n    private final LinkedList<CallSnapshot> csListMinute = new LinkedList<>();",
    ),
    (
        "    private final LinkedList<CallSnapshot> csListHour = new LinkedList<>();",
        "    /** 小时级采样快照链表。 */\n    private final LinkedList<CallSnapshot> csListHour = new LinkedList<>();",
    ),
    (
        "    private final LinkedList<CallSnapshot> csListDay = new LinkedList<>();",
        "    /** 日级采样快照链表。 */\n    private final LinkedList<CallSnapshot> csListDay = new LinkedList<>();",
    ),
    (
        "    public StatsItem(String statsName, String statsKey, ScheduledExecutorService scheduledExecutorService, Logger logger) {",
        "    /** 构造统计项并绑定调度器与日志。 */\n    public StatsItem(String statsName, String statsKey, ScheduledExecutorService scheduledExecutorService, Logger logger) {",
    ),
    (
        "    private static StatsSnapshot computeStatsData(final LinkedList<CallSnapshot> csList) {",
        "    /** 根据快照链表首尾差值计算 SUM、TPS、AVGPT 与 TIMES。 */\n    private static StatsSnapshot computeStatsData(final LinkedList<CallSnapshot> csList) {",
    ),
    (
        "    public StatsSnapshot getStatsDataInMinute() {",
        "    /** 返回分钟窗口聚合快照。 */\n    public StatsSnapshot getStatsDataInMinute() {",
    ),
    (
        "    public StatsSnapshot getStatsDataInHour() {",
        "    /** 返回小时窗口聚合快照。 */\n    public StatsSnapshot getStatsDataInHour() {",
    ),
    (
        "    public StatsSnapshot getStatsDataInDay() {",
        "    /** 返回日窗口聚合快照。 */\n    public StatsSnapshot getStatsDataInDay() {",
    ),
    (
        "    public void init() {",
        "    /** 注册秒/分/时采样及分钟/小时/日打印的定时任务。 */\n    public void init() {",
    ),
    (
        "    public void samplingInSeconds() {",
        "    /** 每 10 秒向分钟链表追加一次快照。 */\n    public void samplingInSeconds() {",
    ),
    (
        "    public void samplingInMinutes() {",
        "    /** 每 10 分钟向小时链表追加一次快照。 */\n    public void samplingInMinutes() {",
    ),
    (
        "    public void samplingInHour() {",
        "    /** 每小时向日链表追加一次快照。 */\n    public void samplingInHour() {",
    ),
    (
        "    public void printAtMinutes() {",
        "    /** 打印分钟级统计日志。 */\n    public void printAtMinutes() {",
    ),
    (
        "    public void printAtHour() {",
        "    /** 打印小时级统计日志。 */\n    public void printAtHour() {",
    ),
    (
        "    public void printAtDay() {",
        "    /** 打印日级统计日志。 */\n    public void printAtDay() {",
    ),
    (
        "    protected String statPrintDetail(StatsSnapshot ss) {",
        "    /** 格式化 SUM/TPS/AVGPT 明细，子类可覆盖。 */\n    protected String statPrintDetail(StatsSnapshot ss) {",
    ),
    (
        "class CallSnapshot {",
        "/** 某一时刻的 times 与 value 快照，用于滑动窗口差分计算。 */\nclass CallSnapshot {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/stats/StatsItemSet.java"] = [
    (
        "public class StatsItemSet {",
        "/**\n * 统计项集合：按 statsKey 管理 {@link StatsItem}/{@link RTStatsItem}，统一采样与打印调度。\n */\npublic class StatsItemSet {",
    ),
    (
        "    private final ConcurrentMap<String/* key */, StatsItem> statsItemTable =",
        "    /** statsKey → 统计项映射表。 */\n    private final ConcurrentMap<String/* key */, StatsItem> statsItemTable =",
    ),
    (
        "    public StatsItemSet(String statsName, ScheduledExecutorService scheduledExecutorService, Logger logger) {",
        "    /** 构造集合并立即注册采样与打印定时任务。 */\n    public StatsItemSet(String statsName, ScheduledExecutorService scheduledExecutorService, Logger logger) {",
    ),
    (
        "    public void init() {",
        "    /** 注册秒/分/时采样及分钟/小时/日打印的集合级定时任务。 */\n    public void init() {",
    ),
    (
        "    public void addValue(final String statsKey, final int incValue, final int incTimes) {",
        "    /** 对普通统计项累加 value 与 times。 */\n    public void addValue(final String statsKey, final int incValue, final int incTimes) {",
    ),
    (
        "    public void addRTValue(final String statsKey, final int incValue, final int incTimes) {",
        "    /** 对 RT 统计项累加 value 与 times。 */\n    public void addRTValue(final String statsKey, final int incValue, final int incTimes) {",
    ),
    (
        "    public void delValue(final String statsKey) {",
        "    /** 删除指定 key 的统计项。 */\n    public void delValue(final String statsKey) {",
    ),
    (
        "    public void delValueByPrefixKey(final String statsKey, String separator) {",
        "    /** 删除 key 以 statsKey+separator 为前缀的统计项。 */\n    public void delValueByPrefixKey(final String statsKey, String separator) {",
    ),
    (
        "    public StatsItem getAndCreateStatsItem(final String statsKey) {",
        "    /** 获取或创建普通 {@link StatsItem}。 */\n    public StatsItem getAndCreateStatsItem(final String statsKey) {",
    ),
    (
        "    public StatsItem getAndCreateRTStatsItem(final String statsKey) {",
        "    /** 获取或创建 {@link RTStatsItem}。 */\n    public StatsItem getAndCreateRTStatsItem(final String statsKey) {",
    ),
    (
        "    public StatsItem getAndCreateItem(final String statsKey, boolean rtItem) {",
        "    /** 按 rtItem 标志懒创建 StatsItem 或 RTStatsItem。 */\n    public StatsItem getAndCreateItem(final String statsKey, boolean rtItem) {",
    ),
    (
        "    public StatsSnapshot getStatsDataInMinute(final String statsKey) {",
        "    /** 读取指定 key 的分钟级快照，不存在则返回空快照。 */\n    public StatsSnapshot getStatsDataInMinute(final String statsKey) {",
    ),
    (
        "    public StatsSnapshot getStatsDataInHour(final String statsKey) {",
        "    /** 读取指定 key 的小时级快照。 */\n    public StatsSnapshot getStatsDataInHour(final String statsKey) {",
    ),
    (
        "    public StatsSnapshot getStatsDataInDay(final String statsKey) {",
        "    /** 读取指定 key 的日级快照。 */\n    public StatsSnapshot getStatsDataInDay(final String statsKey) {",
    ),
    (
        "    public StatsItem getStatsItem(final String statsKey) {",
        "    /** 直接查找统计项，不存在返回 null。 */\n    public StatsItem getStatsItem(final String statsKey) {",
    ),
    (
        "    public void cleanResource(int maxStatsIdleTimeInMinutes) {",
        "    /** 清理超过 maxStatsIdleTimeInMinutes 未更新的空闲统计项。 */\n    public void cleanResource(int maxStatsIdleTimeInMinutes) {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/stats/StatsSnapshot.java"] = [
    (
        "public class StatsSnapshot {",
        "/**\n * 统计快照：保存某一时间窗口内的 SUM、TPS、调用次数与平均耗时。\n */\npublic class StatsSnapshot {",
    ),
    (
        "    private long sum;",
        "    /** 窗口内累计值。 */\n    private long sum;",
    ),
    (
        "    private double tps;",
        "    /** 每秒吞吐量。 */\n    private double tps;",
    ),
    (
        "    private long times;",
        "    /** 窗口内调用次数。 */\n    private long times;",
    ),
    (
        "    private double avgpt;",
        "    /** 平均单次耗时（per time）。 */\n    private double avgpt;",
    ),
    (
        "    public long getSum() {",
        "    /** 返回累计值。 */\n    public long getSum() {",
    ),
    (
        "    public void setSum(long sum) {",
        "    /** 设置累计值。 */\n    public void setSum(long sum) {",
    ),
    (
        "    public double getTps() {",
        "    /** 返回 TPS。 */\n    public double getTps() {",
    ),
    (
        "    public void setTps(double tps) {",
        "    /** 设置 TPS。 */\n    public void setTps(double tps) {",
    ),
    (
        "    public double getAvgpt() {",
        "    /** 返回平均耗时。 */\n    public double getAvgpt() {",
    ),
    (
        "    public void setAvgpt(double avgpt) {",
        "    /** 设置平均耗时。 */\n    public void setAvgpt(double avgpt) {",
    ),
    (
        "    public long getTimes() {",
        "    /** 返回调用次数。 */\n    public long getTimes() {",
    ),
    (
        "    public void setTimes(long times) {",
        "    /** 设置调用次数。 */\n    public void setTimes(long times) {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/sysflag/MessageSysFlag.java"] = [
    (
        "public class MessageSysFlag {",
        "/**\n * 消息系统标志位：压缩、事务、多 Tag、IPv6 地址及批处理等属性编码在 int 标志中。\n */\npublic class MessageSysFlag {",
    ),
    (
        "    /**\n     * Meaning of each bit in the system flag\n     *\n     * | bit    | 7 | 6 | 5         | 4        | 3           | 2                | 1                | 0                |\n     * |--------|---|---|-----------|----------|-------------|------------------|------------------|------------------|\n     * | byte 1 |   |   | STOREHOST | BORNHOST | TRANSACTION | TRANSACTION      | MULTI_TAGS       | COMPRESSED       |\n     * | byte 2 |   |   |           |          |             | COMPRESSION_TYPE | COMPRESSION_TYPE | COMPRESSION_TYPE |\n     * | byte 3 |   |   |           |          |             |                  |                  |                  |\n     * | byte 4 |   |   |           |          |             |                  |                  |                  |\n     */",
        "    /**\n     * 系统标志各位含义：\n     *\n     * | bit    | 7 | 6 | 5         | 4        | 3           | 2                | 1                | 0                |\n     * |--------|---|---|-----------|----------|-------------|------------------|------------------|------------------|\n     * | byte 1 |   |   | STOREHOST | BORNHOST | TRANSACTION | TRANSACTION      | MULTI_TAGS       | COMPRESSED       |\n     * | byte 2 |   |   |           |          |             | COMPRESSION_TYPE | COMPRESSION_TYPE | COMPRESSION_TYPE |\n     * | byte 3 |   |   |           |          |             |                  |                  |                  |\n     * | byte 4 |   |   |           |          |             |                  |                  |                  |\n     */",
    ),
    (
        "    public final static int COMPRESSED_FLAG = 0x1;",
        "    /** 消息体已压缩。 */\n    public final static int COMPRESSED_FLAG = 0x1;",
    ),
    (
        "    public final static int MULTI_TAGS_FLAG = 0x1 << 1;",
        "    /** 消息含多个 Tag。 */\n    public final static int MULTI_TAGS_FLAG = 0x1 << 1;",
    ),
    (
        "    public final static int TRANSACTION_NOT_TYPE = 0;",
        "    /** 非事务消息。 */\n    public final static int TRANSACTION_NOT_TYPE = 0;",
    ),
    (
        "    public final static int TRANSACTION_PREPARED_TYPE = 0x1 << 2;",
        "    /** 事务半消息（Prepared）。 */\n    public final static int TRANSACTION_PREPARED_TYPE = 0x1 << 2;",
    ),
    (
        "    public final static int TRANSACTION_COMMIT_TYPE = 0x2 << 2;",
        "    /** 事务已提交。 */\n    public final static int TRANSACTION_COMMIT_TYPE = 0x2 << 2;",
    ),
    (
        "    public final static int TRANSACTION_ROLLBACK_TYPE = 0x3 << 2;",
        "    /** 事务已回滚。 */\n    public final static int TRANSACTION_ROLLBACK_TYPE = 0x3 << 2;",
    ),
    (
        "    public final static int BORNHOST_V6_FLAG = 0x1 << 4;",
        "    /** bornHost 为 IPv6 地址。 */\n    public final static int BORNHOST_V6_FLAG = 0x1 << 4;",
    ),
    (
        "    public final static int STOREHOSTADDRESS_V6_FLAG = 0x1 << 5;",
        "    /** storeHost 为 IPv6 地址。 */\n    public final static int STOREHOSTADDRESS_V6_FLAG = 0x1 << 5;",
    ),
    (
        "    //Mark the flag for batch to avoid conflict",
        "    // 批处理标志，避免与其他位冲突",
    ),
    (
        "    public final static int NEED_UNWRAP_FLAG = 0x1 << 6;",
        "    /** 批消息需要解包。 */\n    public final static int NEED_UNWRAP_FLAG = 0x1 << 6;",
    ),
    (
        "    public final static int INNER_BATCH_FLAG = 0x1 << 7;",
        "    /** 内部批消息标志。 */\n    public final static int INNER_BATCH_FLAG = 0x1 << 7;",
    ),
    (
        "    // COMPRESSION_TYPE",
        "    // 压缩算法类型（占 bit 8-10）",
    ),
    (
        "    public final static int COMPRESSION_LZ4_TYPE = 0x1 << 8;",
        "    /** LZ4 压缩。 */\n    public final static int COMPRESSION_LZ4_TYPE = 0x1 << 8;",
    ),
    (
        "    public final static int COMPRESSION_ZSTD_TYPE = 0x2 << 8;",
        "    /** Zstd 压缩。 */\n    public final static int COMPRESSION_ZSTD_TYPE = 0x2 << 8;",
    ),
    (
        "    public final static int COMPRESSION_ZLIB_TYPE = 0x3 << 8;",
        "    /** Zlib 压缩。 */\n    public final static int COMPRESSION_ZLIB_TYPE = 0x3 << 8;",
    ),
    (
        "    public final static int COMPRESSION_TYPE_COMPARATOR = 0x7 << 8;",
        "    /** 压缩类型位掩码。 */\n    public final static int COMPRESSION_TYPE_COMPARATOR = 0x7 << 8;",
    ),
    (
        "    public static int getTransactionValue(final int flag) {",
        "    /** 提取事务状态位（Prepared/Commit/Rollback）。 */\n    public static int getTransactionValue(final int flag) {",
    ),
    (
        "    public static int resetTransactionValue(final int flag, final int type) {",
        "    /** 重置事务状态位为指定 type。 */\n    public static int resetTransactionValue(final int flag, final int type) {",
    ),
    (
        "    public static int clearCompressedFlag(final int flag) {",
        "    /** 清除压缩标志位。 */\n    public static int clearCompressedFlag(final int flag) {",
    ),
    (
        "    // To match the compression type",
        "    // 从标志位解析压缩算法",
    ),
    (
        "    public static CompressionType getCompressionType(final int flag) {",
        "    /** 根据标志位返回 {@link CompressionType}。 */\n    public static CompressionType getCompressionType(final int flag) {",
    ),
    (
        "    public static boolean check(int flag, int expectedFlag) {",
        "    /** 检测 flag 是否包含 expectedFlag 指定位。 */\n    public static boolean check(int flag, int expectedFlag) {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/sysflag/PullSysFlag.java"] = [
    (
        "public class PullSysFlag {",
        "/**\n * Pull 请求系统标志：提交 offset、挂起、订阅表达式、类过滤与 Lite Pull 等选项。\n */\npublic class PullSysFlag {",
    ),
    (
        "    private final static int FLAG_COMMIT_OFFSET = 0x1;",
        "    /** 请求携带 commit offset。 */\n    private final static int FLAG_COMMIT_OFFSET = 0x1;",
    ),
    (
        "    private final static int FLAG_SUSPEND = 0x1 << 1;",
        "    /** 长轮询挂起标志。 */\n    private final static int FLAG_SUSPEND = 0x1 << 1;",
    ),
    (
        "    private final static int FLAG_SUBSCRIPTION = 0x1 << 2;",
        "    /** 携带订阅表达式。 */\n    private final static int FLAG_SUBSCRIPTION = 0x1 << 2;",
    ),
    (
        "    private final static int FLAG_CLASS_FILTER = 0x1 << 3;",
        "    /** 启用类过滤。 */\n    private final static int FLAG_CLASS_FILTER = 0x1 << 3;",
    ),
    (
        "    private final static int FLAG_LITE_PULL_MESSAGE = 0x1 << 4;",
        "    /** Lite Pull 消费模式。 */\n    private final static int FLAG_LITE_PULL_MESSAGE = 0x1 << 4;",
    ),
    (
        "    public static int buildSysFlag(final boolean commitOffset, final boolean suspend,",
        "    /** 组装 Pull 系统标志（不含 Lite Pull）。 */\n    public static int buildSysFlag(final boolean commitOffset, final boolean suspend,",
    ),
    (
        "    public static int buildSysFlag(final boolean commitOffset, final boolean suspend,\n        final boolean subscription, final boolean classFilter, final boolean litePull) {",
        "    /** 组装 Pull 系统标志（含 Lite Pull 选项）。 */\n    public static int buildSysFlag(final boolean commitOffset, final boolean suspend,\n        final boolean subscription, final boolean classFilter, final boolean litePull) {",
    ),
    (
        "    public static int clearCommitOffsetFlag(final int sysFlag) {",
        "    /** 清除 commit offset 标志位。 */\n    public static int clearCommitOffsetFlag(final int sysFlag) {",
    ),
    (
        "    public static boolean hasCommitOffsetFlag(final int sysFlag) {",
        "    /** 是否设置了 commit offset 标志。 */\n    public static boolean hasCommitOffsetFlag(final int sysFlag) {",
    ),
    (
        "    public static boolean hasSuspendFlag(final int sysFlag) {",
        "    /** 是否设置了挂起标志。 */\n    public static boolean hasSuspendFlag(final int sysFlag) {",
    ),
    (
        "    public static int clearSuspendFlag(final int sysFlag) {",
        "    /** 清除挂起标志位。 */\n    public static int clearSuspendFlag(final int sysFlag) {",
    ),
    (
        "    public static boolean hasSubscriptionFlag(final int sysFlag) {",
        "    /** 是否携带订阅表达式。 */\n    public static boolean hasSubscriptionFlag(final int sysFlag) {",
    ),
    (
        "    public static int buildSysFlagWithSubscription(final int sysFlag) {",
        "    /** 在现有标志上追加订阅标志。 */\n    public static int buildSysFlagWithSubscription(final int sysFlag) {",
    ),
    (
        "    public static boolean hasClassFilterFlag(final int sysFlag) {",
        "    /** 是否启用类过滤。 */\n    public static boolean hasClassFilterFlag(final int sysFlag) {",
    ),
    (
        "    public static boolean hasLitePullFlag(final int sysFlag) {",
        "    /** 是否为 Lite Pull 请求。 */\n    public static boolean hasLitePullFlag(final int sysFlag) {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/sysflag/SubscriptionSysFlag.java"] = [
    (
        "public class SubscriptionSysFlag {",
        "/**\n * 订阅系统标志：标记消费组是否属于单元化（Unit）部署。\n */\npublic class SubscriptionSysFlag {",
    ),
    (
        "    private final static int FLAG_UNIT = 0x1 << 0;",
        "    /** 单元化消费组标志。 */\n    private final static int FLAG_UNIT = 0x1 << 0;",
    ),
    (
        "    public static int buildSysFlag(final boolean unit) {",
        "    /** 根据 unit 参数构建订阅系统标志。 */\n    public static int buildSysFlag(final boolean unit) {",
    ),
    (
        "    public static int setUnitFlag(final int sysFlag) {",
        "    /** 设置单元化标志位。 */\n    public static int setUnitFlag(final int sysFlag) {",
    ),
    (
        "    public static int clearUnitFlag(final int sysFlag) {",
        "    /** 清除单元化标志位。 */\n    public static int clearUnitFlag(final int sysFlag) {",
    ),
    (
        "    public static boolean hasUnitFlag(final int sysFlag) {",
        "    /** 是否包含单元化标志。 */\n    public static boolean hasUnitFlag(final int sysFlag) {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/sysflag/TopicSysFlag.java"] = [
    (
        "public class TopicSysFlag {",
        "/**\n * Topic 系统标志：标记 Topic 是否为单元化 Topic 及是否含单元订阅。\n */\npublic class TopicSysFlag {",
    ),
    (
        "    private final static int FLAG_UNIT = 0x1 << 0;",
        "    /** 单元化 Topic 标志。 */\n    private final static int FLAG_UNIT = 0x1 << 0;",
    ),
    (
        "    private final static int FLAG_UNIT_SUB = 0x1 << 1;",
        "    /** Topic 含单元订阅标志。 */\n    private final static int FLAG_UNIT_SUB = 0x1 << 1;",
    ),
    (
        "    public static int buildSysFlag(final boolean unit, final boolean hasUnitSub) {",
        "    /** 根据 unit 与 hasUnitSub 构建 Topic 系统标志。 */\n    public static int buildSysFlag(final boolean unit, final boolean hasUnitSub) {",
    ),
    (
        "    public static int setUnitFlag(final int sysFlag) {",
        "    /** 设置单元化 Topic 标志。 */\n    public static int setUnitFlag(final int sysFlag) {",
    ),
    (
        "    public static int clearUnitFlag(final int sysFlag) {",
        "    /** 清除单元化 Topic 标志。 */\n    public static int clearUnitFlag(final int sysFlag) {",
    ),
    (
        "    public static boolean hasUnitFlag(final int sysFlag) {",
        "    /** 是否为单元化 Topic。 */\n    public static boolean hasUnitFlag(final int sysFlag) {",
    ),
    (
        "    public static int setUnitSubFlag(final int sysFlag) {",
        "    /** 设置单元订阅标志。 */\n    public static int setUnitSubFlag(final int sysFlag) {",
    ),
    (
        "    public static int clearUnitSubFlag(final int sysFlag) {",
        "    /** 清除单元订阅标志。 */\n    public static int clearUnitSubFlag(final int sysFlag) {",
    ),
    (
        "    public static boolean hasUnitSubFlag(final int sysFlag) {",
        "    /** 是否含单元订阅。 */\n    public static boolean hasUnitSubFlag(final int sysFlag) {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/thread/FutureTaskExtThreadPoolExecutor.java"] = [
    (
        "public class FutureTaskExtThreadPoolExecutor extends ThreadPoolExecutor {",
        "/**\n * 扩展线程池：提交 Runnable 时包装为 {@link FutureTaskExt}，支持任务级超时与取消追踪。\n */\npublic class FutureTaskExtThreadPoolExecutor extends ThreadPoolExecutor {",
    ),
    (
        "    public FutureTaskExtThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime,",
        "    /** 构造扩展线程池，参数语义同 {@link ThreadPoolExecutor}。 */\n    public FutureTaskExtThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime,",
    ),
    (
        "    @Override\n    protected <T> RunnableFuture<T> newTaskFor(final Runnable runnable, final T value) {",
        "    /** 将 Runnable 包装为 {@link FutureTaskExt} 以便扩展行为。 */\n    @Override\n    protected <T> RunnableFuture<T> newTaskFor(final Runnable runnable, final T value) {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/thread/ThreadPoolMonitor.java"] = [
    (
        "public class ThreadPoolMonitor {",
        "/**\n * 线程池监控中心：创建受监控的线程池并周期性输出队列水位，必要时打印 jstack。\n */\npublic class ThreadPoolMonitor {",
    ),
    (
        "    private static final List<ThreadPoolWrapper> MONITOR_EXECUTOR = new CopyOnWriteArrayList<>();",
        "    /** 已注册监控的全部线程池包装器。 */\n    private static final List<ThreadPoolWrapper> MONITOR_EXECUTOR = new CopyOnWriteArrayList<>();",
    ),
    (
        "    public static void config(Logger jstackLoggerConfig, Logger waterMarkLoggerConfig,",
        "    /** 配置 jstack/水位日志 Logger 及打印间隔。 */\n    public static void config(Logger jstackLoggerConfig, Logger waterMarkLoggerConfig,",
    ),
    (
        "    public static ThreadPoolExecutor createAndMonitor(int corePoolSize,",
        "    /** 创建带默认 DiscardOldest 拒绝策略的受监控线程池。 */\n    public static ThreadPoolExecutor createAndMonitor(int corePoolSize,",
    ),
    (
        "    public static void logThreadPoolStatus() {",
        "    /** 遍历所有注册线程池，输出各监控指标并在超阈值时打印 jstack。 */\n    public static void logThreadPoolStatus() {",
    ),
    (
        "    public static void init() {",
        "    /** 启动周期性线程池状态采集任务。 */\n    public static void init() {",
    ),
    (
        "    public static void shutdown() {",
        "    /** 关闭监控调度线程。 */\n    public static void shutdown() {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/thread/ThreadPoolQueueSizeMonitor.java"] = [
    (
        "public class ThreadPoolQueueSizeMonitor implements ThreadPoolStatusMonitor {",
        "/**\n * 线程池队列长度监控：队列超过容量 85% 时触发 jstack 打印。\n */\npublic class ThreadPoolQueueSizeMonitor implements ThreadPoolStatusMonitor {",
    ),
    (
        "    private final int maxQueueCapacity;",
        "    /** 队列最大容量，用于计算 85% 告警阈值。 */\n    private final int maxQueueCapacity;",
    ),
    (
        "    public ThreadPoolQueueSizeMonitor(int maxQueueCapacity) {",
        "    /** 指定队列容量上限构造监控器。 */\n    public ThreadPoolQueueSizeMonitor(int maxQueueCapacity) {",
    ),
    (
        "    @Override\n    public String describe() {",
        "    /** 监控指标名称：queueSize。 */\n    @Override\n    public String describe() {",
    ),
    (
        "    @Override\n    public double value(ThreadPoolExecutor executor) {",
        "    /** 返回当前队列待执行任务数。 */\n    @Override\n    public double value(ThreadPoolExecutor executor) {",
    ),
    (
        "    @Override\n    public boolean needPrintJstack(ThreadPoolExecutor executor, double value) {",
        "    /** 队列长度超过容量 85% 时需要打印 jstack。 */\n    @Override\n    public boolean needPrintJstack(ThreadPoolExecutor executor, double value) {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/thread/ThreadPoolStatusMonitor.java"] = [
    (
        "public interface ThreadPoolStatusMonitor {",
        "/**\n * 线程池状态监控接口：定义指标名称、当前值及 jstack 触发条件。\n */\npublic interface ThreadPoolStatusMonitor {",
    ),
    (
        "    String describe();",
        "    /** 返回监控指标描述名。 */\n    String describe();",
    ),
    (
        "    double value(ThreadPoolExecutor executor);",
        "    /** 读取线程池当前指标值。 */\n    double value(ThreadPoolExecutor executor);",
    ),
    (
        "    boolean needPrintJstack(ThreadPoolExecutor executor, double value);",
        "    /** 根据当前指标值判断是否需要打印 jstack。 */\n    boolean needPrintJstack(ThreadPoolExecutor executor, double value);",
    ),
]
