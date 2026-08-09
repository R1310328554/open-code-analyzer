"""RocketMQ 5.5.0 wave16a common resource/running/state/statistics [0:15] replacements."""

R: dict[str, list[tuple[str, str]]] = {}

R["common/src/main/java/org/apache/rocketmq/common/resource/RocketMQResource.java"] = [
    (
        "@Retention(RetentionPolicy.RUNTIME)\npublic @interface RocketMQResource {",
        "/**\n * 标记 RocketMQ 资源字段或参数的类型注解，\n * 配合 {@link ResourceType} 用于资源解析与注入。\n */\n@Retention(RetentionPolicy.RUNTIME)\npublic @interface RocketMQResource {",
    ),
    (
        "    ResourceType value();",
        "    /** 资源类型。 */\n    ResourceType value();",
    ),
    (
        "    String splitter() default \"\";",
        "    /** 多值资源的分隔符，默认空串表示不分隔。 */\n    String splitter() default \"\";",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/running/RunningStats.java"] = [
    (
        "public enum RunningStats {",
        "/**\n * Broker 运行时监控指标键：CommitLog/ConsumeQueue 磁盘占用与偏移等。\n */\npublic enum RunningStats {",
    ),
    (
        "    commitLogMaxOffset,",
        "    /** CommitLog 最大物理偏移。 */\n    commitLogMaxOffset,",
    ),
    (
        "    commitLogMinOffset,",
        "    /** CommitLog 最小物理偏移。 */\n    commitLogMinOffset,",
    ),
    (
        "    commitLogDiskRatio,",
        "    /** CommitLog 磁盘使用率。 */\n    commitLogDiskRatio,",
    ),
    (
        "    consumeQueueDiskRatio,",
        "    /** ConsumeQueue 磁盘使用率。 */\n    consumeQueueDiskRatio,",
    ),
    (
        "    scheduleMessageOffset,",
        "    /** 定时消息队列偏移。 */\n    scheduleMessageOffset,",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/state/StateEventListener.java"] = [
    (
        "public interface StateEventListener<T> {",
        "/**\n * 状态事件监听器：接收并处理类型为 {@code T} 的状态变更事件。\n */\npublic interface StateEventListener<T> {",
    ),
    (
        "    void fireEvent(T event);",
        "    /** 触发并分发状态事件。 */\n    void fireEvent(T event);",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/statistics/FutureHolder.java"] = [
    (
        "public class FutureHolder<T> {",
        "/**\n * 按键分组持有 {@link Future} 任务，支持批量取消与移除。\n *\n * @param <T> 分组键类型\n */\npublic class FutureHolder<T> {",
    ),
    (
        "    private ConcurrentMap<T, BlockingQueue<Future>> futureMap = new ConcurrentHashMap<>(8);",
        "    /** 键到 Future 队列的映射。 */\n    private ConcurrentMap<T, BlockingQueue<Future>> futureMap = new ConcurrentHashMap<>(8);",
    ),
    (
        "    public void addFuture(T t, Future future) {",
        "    /** 将 Future 登记到键 {@code t} 对应的队列。 */\n    public void addFuture(T t, Future future) {",
    ),
    (
        "    public void removeAllFuture(T t) {",
        "    /** 取消键 {@code t} 下全部 Future 并移除映射。 */\n    public void removeAllFuture(T t) {",
    ),
    (
        "    private void cancelAll(T t, boolean mayInterruptIfRunning) {",
        "    /** 取消键 {@code t} 下全部 Future，可选是否中断运行中任务。 */\n    private void cancelAll(T t, boolean mayInterruptIfRunning) {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/statistics/Interceptor.java"] = [
    (
        "/**\n * interceptor\n */\npublic interface Interceptor {",
        "/**\n * 统计项拦截器：在 {@link StatisticsItem} 增量更新时同步采样或重置。\n */\npublic interface Interceptor {",
    ),
    (
        "    /**\n     * increase multiple values\n     *\n     * @param deltas\n     */",
        "    /**\n     * 按项递增多个增量值。\n     *\n     * @param deltas 各统计项增量\n     */",
    ),
    (
        "    void reset();",
        "    /** 重置拦截器内部采样状态。 */\n    void reset();",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/statistics/StatisticsBrief.java"] = [
    (
        "public class StatisticsBrief {",
        "/**\n * 分桶直方图统计摘要：维护 max/min/avg/total 及 TP 分位值。\n * 桶边界由 {@code topPercentileMeta} 二维数组定义。\n */\npublic class StatisticsBrief {",
    ),
    (
        "    public static final int META_RANGE_INDEX = 0;",
        "    /** {@code topPercentileMeta} 每行中区间上界索引。 */\n    public static final int META_RANGE_INDEX = 0;",
    ),
    (
        "    public static final int META_SLOT_NUM_INDEX = 1;",
        "    /** {@code topPercentileMeta} 每行中桶数量索引。 */\n    public static final int META_SLOT_NUM_INDEX = 1;",
    ),
    (
        "    // TopPercentile",
        "    // 分位直方图元数据与计数",
    ),
    (
        "    private long[][] topPercentileMeta;",
        "    /** 分位桶元数据：每行 [rangeMax, slotNum]。 */\n    private long[][] topPercentileMeta;",
    ),
    (
        "    private AtomicInteger[] counts;",
        "    /** 各桶采样计数。 */\n    private AtomicInteger[] counts;",
    ),
    (
        "    private AtomicLong totalCount;",
        "    /** 总采样次数。 */\n    private AtomicLong totalCount;",
    ),
    (
        "    // max min avg total",
        "    // 全局 max/min/total 聚合",
    ),
    (
        "    private long max;",
        "    /** 采样最大值。 */\n    private long max;",
    ),
    (
        "    private long min;",
        "    /** 采样最小值。 */\n    private long min;",
    ),
    (
        "    private long total;",
        "    /** 采样值累加和。 */\n    private long total;",
    ),
    (
        "    public StatisticsBrief(long[][] topPercentileMeta) {",
        "    /** 以分位桶元数据构造摘要对象。 */\n    public StatisticsBrief(long[][] topPercentileMeta) {",
    ),
    (
        "    public void reset() {",
        "    /** 清零各桶计数与 max/min/total。 */\n    public void reset() {",
    ),
    (
        "    private static boolean isLegalMeta(long[][] meta) {",
        "    /** 校验元数据非空且每行长度为 2。 */\n    private static boolean isLegalMeta(long[][] meta) {",
    ),
    (
        "    private static int slotNum(long[][] meta) {",
        "    /** 根据元数据计算总桶数。 */\n    private static int slotNum(long[][] meta) {",
    ),
    (
        "    public void sample(long value) {",
        "    /** 记录一次采样并更新桶计数与 max/min/total。 */\n    public void sample(long value) {",
    ),
    (
        "    public long tp999() {",
        "    /** 返回 TP99.9 分位估计值。 */\n    public long tp999() {",
    ),
    (
        "    public long getTPValue(float ratio) {",
        "    /** 按给定比例（0~1）估算 TP 分位值。 */\n    public long getTPValue(float ratio) {",
    ),
    (
        "    private long getSlotTPValue(int index) {",
        "    /** 由桶索引反推该桶代表的分位值。 */\n    private long getSlotTPValue(int index) {",
    ),
    (
        "        // MAX_VALUE: the last slot",
        "        // 末桶上界为 Integer.MAX_VALUE",
    ),
    (
        "    private int getSlotIndex(long num) {",
        "    /** 将采样值映射到桶索引。 */\n    private int getSlotIndex(long num) {",
    ),
    (
        "    /**\n     * Getters\n     *\n     * @return\n     */",
        "    /** 返回采样最大值。 */",
    ),
    (
        "    public long getMax() {",
        "    public long getMax() {",
    ),
    (
        "    public long getMin() {",
        "    /** 无采样时返回 0，否则返回最小值。 */\n    public long getMin() {",
    ),
    (
        "    public long getTotal() {",
        "    /** 返回采样值累加和。 */\n    public long getTotal() {",
    ),
    (
        "    public long getCnt() {",
        "    /** 返回总采样次数。 */\n    public long getCnt() {",
    ),
    (
        "    public double getAvg() {",
        "    /** 返回采样平均值，无采样时为 0。 */\n    public double getAvg() {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/statistics/StatisticsBriefInterceptor.java"] = [
    (
        "/**\n * interceptor to generate statistics brief\n */\npublic class StatisticsBriefInterceptor implements Interceptor {",
        "/**\n * {@link Interceptor} 实现：将 {@link StatisticsItem} 指定项写入 {@link StatisticsBrief} 分位摘要。\n */\npublic class StatisticsBriefInterceptor implements Interceptor {",
    ),
    (
        "    private int[] indexOfItems;",
        "    /** 各 brief 对应 StatisticsItem 项下标。 */\n    private int[] indexOfItems;",
    ),
    (
        "    private StatisticsBrief[] statisticsBriefs;",
        "    /** 与 briefMetas 一一对应的摘要实例。 */\n    private StatisticsBrief[] statisticsBriefs;",
    ),
    (
        "    public StatisticsBriefInterceptor(StatisticsItem item, Pair<String, long[][]>[] briefMetas) {",
        "    /** 绑定统计项与 brief 元数据，校验项名存在于 item。 */\n    public StatisticsBriefInterceptor(StatisticsItem item, Pair<String, long[][]>[] briefMetas) {",
    ),
    (
        "    @Override\n    public void inc(long... itemValues) {",
        "    /** 按映射下标将增量采样到对应 {@link StatisticsBrief}。 */\n    @Override\n    public void inc(long... itemValues) {",
    ),
    (
        "    @Override\n    public void reset() {",
        "    /** 重置全部 brief 采样状态。 */\n    @Override\n    public void reset() {",
    ),
    (
        "    public int[] getIndexOfItems() {",
        "    /** 返回项下标映射。 */\n    public int[] getIndexOfItems() {",
    ),
    (
        "    public void setIndexOfItems(int[] indexOfItems) {",
        "    /** 设置项下标映射。 */\n    public void setIndexOfItems(int[] indexOfItems) {",
    ),
    (
        "    public StatisticsBrief[] getStatisticsBriefs() {",
        "    /** 返回摘要数组。 */\n    public StatisticsBrief[] getStatisticsBriefs() {",
    ),
    (
        "    public void setStatisticsBriefs(StatisticsBrief[] statisticsBriefs) {",
        "    /** 设置摘要数组。 */\n    public void setStatisticsBriefs(StatisticsBrief[] statisticsBriefs) {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/statistics/StatisticsItem.java"] = [
    (
        "/**\n * Statistics Item\n */\npublic class StatisticsItem {",
        "/**\n * 单条统计项：按 kind/object 维度累加多项计数，支持拦截器与快照差分。\n */\npublic class StatisticsItem {",
    ),
    (
        "    private String statKind;",
        "    /** 统计类别（如 RPC、Topic）。 */\n    private String statKind;",
    ),
    (
        "    private String statObject;",
        "    /** 统计对象键（如方法名、Topic 名）。 */\n    private String statObject;",
    ),
    (
        "    private String[] itemNames;",
        "    /** 各子项名称。 */\n    private String[] itemNames;",
    ),
    (
        "    private AtomicLong[] itemAccumulates;",
        "    /** 各子项累加器。 */\n    private AtomicLong[] itemAccumulates;",
    ),
    (
        "    private AtomicLong invokeTimes;",
        "    /** 调用/更新次数。 */\n    private AtomicLong invokeTimes;",
    ),
    (
        "    private Interceptor interceptor;",
        "    /** 可选拦截器（如分位摘要）。 */\n    private Interceptor interceptor;",
    ),
    (
        "    /**\n     * last timestamp when the item was updated\n     */",
        "    /** 最近一次更新的时间戳（毫秒）。 */",
    ),
    (
        "    private AtomicLong lastTimeStamp;",
        "    private AtomicLong lastTimeStamp;",
    ),
    (
        "    public StatisticsItem(String statKind, String statObject, String... itemNames) {",
        "    /** 构造统计项并初始化各子项累加器。 */\n    public StatisticsItem(String statKind, String statObject, String... itemNames) {",
    ),
    (
        "    public void incItems(long... itemIncs) {",
        "    /** 递增各子项并刷新 invokeTimes、lastTimeStamp，通知拦截器。 */\n    public void incItems(long... itemIncs) {",
    ),
    (
        "    public boolean allZeros() {",
        "    /** 是否从未调用或各子项均为 0。 */\n    public boolean allZeros() {",
    ),
    (
        "    public String getStatKind() {",
        "    /** 返回统计类别。 */\n    public String getStatKind() {",
    ),
    (
        "    public String getStatObject() {",
        "    /** 返回统计对象键。 */\n    public String getStatObject() {",
    ),
    (
        "    public String[] getItemNames() {",
        "    /** 返回子项名称数组。 */\n    public String[] getItemNames() {",
    ),
    (
        "    public AtomicLong[] getItemAccumulates() {",
        "    /** 返回各子项累加器。 */\n    public AtomicLong[] getItemAccumulates() {",
    ),
    (
        "    public AtomicLong getInvokeTimes() {",
        "    /** 返回调用次数。 */\n    public AtomicLong getInvokeTimes() {",
    ),
    (
        "    public AtomicLong getLastTimeStamp() {",
        "    /** 返回最后更新时间戳。 */\n    public AtomicLong getLastTimeStamp() {",
    ),
    (
        "    public AtomicLong getItemAccumulate(String itemName) {",
        "    /** 按名称取子项累加器，未知项返回零值 AtomicLong。 */\n    public AtomicLong getItemAccumulate(String itemName) {",
    ),
    (
        "    /**\n     * get snapshot\n     * <p>\n     * Warning: no guarantee of itemAccumulates consistency\n     *\n     * @return\n     */",
        "    /**\n     * 获取当前快照（各累加器可能非原子一致）。\n     *\n     * @return 独立副本\n     */",
    ),
    (
        "    /**\n     * subtract another StatisticsItem\n     *\n     * @param item\n     * @return\n     */",
        "    /**\n     * 与另一统计项做差，得到区间增量。\n     *\n     * @param item 被减项（同 kind/object/itemNames）\n     * @return 差分后的新 StatisticsItem\n     */",
    ),
    (
        "    public Interceptor getInterceptor() {",
        "    /** 返回拦截器。 */\n    public Interceptor getInterceptor() {",
    ),
    (
        "    public void setInterceptor(Interceptor interceptor) {",
        "    /** 设置拦截器。 */\n    public void setInterceptor(Interceptor interceptor) {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/statistics/StatisticsItemFormatter.java"] = [
    (
        "public class StatisticsItemFormatter {",
        "/**\n * 将 {@link StatisticsItem} 格式化为管道分隔单行：kind|object|各项累加|invokeTimes。\n */\npublic class StatisticsItemFormatter {",
    ),
    (
        "    public String format(StatisticsItem statItem) {",
        "    /** 格式化统计项为日志字符串。 */\n    public String format(StatisticsItem statItem) {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/statistics/StatisticsItemPrinter.java"] = [
    (
        "public class StatisticsItemPrinter {",
        "/**\n * 通过 {@link Logger} 输出格式化后的 {@link StatisticsItem}。\n */\npublic class StatisticsItemPrinter {",
    ),
    (
        "    private Logger log;",
        "    /** 输出目标 Logger。 */\n    private Logger log;",
    ),
    (
        "    private StatisticsItemFormatter formatter;",
        "    /** 统计项格式化器。 */\n    private StatisticsItemFormatter formatter;",
    ),
    (
        "    public StatisticsItemPrinter(StatisticsItemFormatter formatter, Logger log) {",
        "    /** 指定格式化器与 Logger。 */\n    public StatisticsItemPrinter(StatisticsItemFormatter formatter, Logger log) {",
    ),
    (
        "    public void log(Logger log) {",
        "    /** 更换输出 Logger。 */\n    public void log(Logger log) {",
    ),
    (
        "    public void formatter(StatisticsItemFormatter formatter) {",
        "    /** 更换格式化器。 */\n    public void formatter(StatisticsItemFormatter formatter) {",
    ),
    (
        "    public void print(String prefix, StatisticsItem statItem, String... suffixs) {",
        "    /** 输出 prefix + 格式化统计项 + 后缀到 log.info。 */\n    public void print(String prefix, StatisticsItem statItem, String... suffixs) {",
    ),
    (
        "        // System.out.printf(\"%s %s%s%s\\n\", new Date().toString(), prefix, formatter.format(statItem), suffix.toString());",
        "        // 调试时可改用标准输出",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/statistics/StatisticsItemScheduledIncrementPrinter.java"] = [
    (
        "public class StatisticsItemScheduledIncrementPrinter extends StatisticsItemScheduledPrinter {",
        "/**\n * 定时打印 {@link StatisticsItem} 区间增量（非累计值），并采样 TPS 相关 brief。\n */\npublic class StatisticsItemScheduledIncrementPrinter extends StatisticsItemScheduledPrinter {",
    ),
    (
        "    private String[] tpsItemNames;",
        "    /** 参与 TPS 采样的子项名称。 */\n    private String[] tpsItemNames;",
    ),
    (
        "    public static final int TPS_INITIAL_DELAY = 0;",
        "    /** TPS 采样任务初始延迟（毫秒）。 */\n    public static final int TPS_INITIAL_DELAY = 0;",
    ),
    (
        "    public static final int TPS_INTREVAL = 1000;",
        "    /** TPS 采样间隔（毫秒）。 */\n    public static final int TPS_INTREVAL = 1000;",
    ),
    (
        "    public static final String SEPARATOR = \"|\";",
        "    /** brief 输出字段分隔符。 */\n    public static final String SEPARATOR = \"|\";",
    ),
    (
        "    /**\n     * last snapshots of all scheduled items\n     */",
        "    /** 各 kind/object 上次打印时的快照，用于计算增量。 */",
    ),
    (
        "    private final ConcurrentHashMap<String, ConcurrentHashMap<String, StatisticsItem>> lastItemSnapshots",
        "    private final ConcurrentHashMap<String, ConcurrentHashMap<String, StatisticsItem>> lastItemSnapshots",
    ),
    (
        "    private final ConcurrentHashMap<String, ConcurrentHashMap<String, StatisticsItemSampleBrief>> sampleBriefs",
        "    /** kind -> object -> TPS 采样 brief。 */\n    private final ConcurrentHashMap<String, ConcurrentHashMap<String, StatisticsItemSampleBrief>> sampleBriefs",
    ),
    (
        "    public StatisticsItemScheduledIncrementPrinter(String name, StatisticsItemPrinter printer,",
        "    /** 构造增量定时打印机，并指定 TPS 采样项名。 */\n    public StatisticsItemScheduledIncrementPrinter(String name, StatisticsItemPrinter printer,",
    ),
    (
        "    /**\n     * schedule a StatisticsItem to print the Increments periodically\n     */",
        "    /** 注册统计项：按 interval 打印增量，并按 TPS_INTREVAL 采样 brief。 */",
    ),
    (
        "        // print log every ${interval} milliseconds",
        "        // 每 interval 毫秒打印一次区间增量",
    ),
    (
        "        // sample every TPS_INTERVAL",
        "        // 每 TPS_INTREVAL 毫秒采样一次 TPS brief",
    ),
    (
        "    @Override\n    public void remove(StatisticsItem item) {",
        "    /** 取消定时任务并清理快照与 brief 缓存。 */\n    @Override\n    public void remove(StatisticsItem item) {",
    ),
    (
        "        // remove task",
        "        // 取消关联的 Future 任务",
    ),
    (
        "    private StatisticsItem getItemSnapshot(",
        "    /** 从快照表取指定 kind/key 的上次快照。 */\n    private StatisticsItem getItemSnapshot(",
    ),
    (
        "    private StatisticsItemSampleBrief getSampleBrief(String kind, String key) {",
        "    /** 取 TPS 采样 brief。 */\n    private StatisticsItemSampleBrief getSampleBrief(String kind, String key) {",
    ),
    (
        "    private void setItemSnapshot(ConcurrentHashMap<String, ConcurrentHashMap<String, StatisticsItem>> snapshots,",
        "    /** 更新 kind/key 的快照缓存。 */\n    private void setItemSnapshot(ConcurrentHashMap<String, ConcurrentHashMap<String, StatisticsItem>> snapshots,",
    ),
    (
        "    private void setItemSampleBrief(String kind, String key,",
        "    /** 注册 kind/key 的 TPS 采样 brief。 */\n    private void setItemSampleBrief(String kind, String key,",
    ),
    (
        "    private String formatInterceptor(Interceptor interceptor) {",
        "    /** 将 {@link StatisticsBriefInterceptor} 格式化为 max|avg|tp999 后缀串。 */\n    private String formatInterceptor(Interceptor interceptor) {",
    ),
    (
        "                //sb.append(SEPARATOR).append(brief.getTotal());",
        "                // 可选输出 total/min",
    ),
    (
        "                //sb.append(SEPARATOR).append(brief.getMin());",
        "                // 可选输出 min",
    ),
    (
        "    public static class StatisticsItemSampleBrief {",
        "    /** 对指定 TPS 子项在采样周期内的 max/avg 摘要。 */\n    public static class StatisticsItemSampleBrief {",
    ),
    (
        "        private StatisticsItem lastSnapshot;",
        "        /** 上次采样时的统计项快照。 */\n        private StatisticsItem lastSnapshot;",
    ),
    (
        "        public String[] itemNames;",
        "        /** TPS 子项名称。 */\n        public String[] itemNames;",
    ),
    (
        "        public ItemSampleBrief[] briefs;",
        "        /** 与各 itemNames 对应的周期 brief。 */\n        public ItemSampleBrief[] briefs;",
    ),
    (
        "        public StatisticsItemSampleBrief(StatisticsItem statItem, String[] itemNames) {",
        "        /** 初始化并建立与 statItem 子项的 brief 数组。 */\n        public StatisticsItemSampleBrief(StatisticsItem statItem, String[] itemNames) {",
    ),
    (
        "        public synchronized void reset() {",
        "        /** 重置各子项 brief。 */\n        public synchronized void reset() {",
    ),
    (
        "        public synchronized void sample(StatisticsItem snapshot) {",
        "        /** 根据与上次快照的差分采样各 TPS 子项增量。 */\n        public synchronized void sample(StatisticsItem snapshot) {",
    ),
    (
        "                //sb.append(SEPARATOR).append(brief.getMin());",
        "                // 可选输出 min",
    ),
    (
        "    /**\n     * sample brief of a item for a period of time\n     */",
        "    /** 单个子项在一段时间内的 max/min/avg 采样摘要。 */",
    ),
    (
        "    public static class ItemSampleBrief {",
        "    public static class ItemSampleBrief {",
    ),
    (
        "        private long max;",
        "        /** 周期内增量最大值。 */\n        private long max;",
    ),
    (
        "        private long min;",
        "        /** 周期内增量最小值。 */\n        private long min;",
    ),
    (
        "        private long total;",
        "        /** 增量累加和。 */\n        private long total;",
    ),
    (
        "        private long cnt;",
        "        /** 采样次数。 */\n        private long cnt;",
    ),
    (
        "        public ItemSampleBrief() {",
        "        /** 构造并重置计数。 */\n        public ItemSampleBrief() {",
    ),
    (
        "        public void sample(long value) {",
        "        /** 记录一次增量采样。 */\n        public void sample(long value) {",
    ),
    (
        "        public void reset() {",
        "        /** 清零 max/min/total/cnt。 */\n        public void reset() {",
    ),
    (
        "        /**\n         * Getters\n         *\n         * @return\n         */",
        "        /** 返回周期内增量最大值。 */",
    ),
    (
        "        public long getMax() {",
        "        public long getMax() {",
    ),
    (
        "        public long getMin() {",
        "        /** 无采样时返回 0，否则返回最小增量。 */\n        public long getMin() {",
    ),
    (
        "        public long getTotal() {",
        "        /** 返回增量累加和。 */\n        public long getTotal() {",
    ),
    (
        "        public long getCnt() {",
        "        /** 返回采样次数。 */\n        public long getCnt() {",
    ),
    (
        "        public double getAvg() {",
        "        /** 返回增量平均值。 */\n        public double getAvg() {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/statistics/StatisticsItemScheduledPrinter.java"] = [
    (
        "public class StatisticsItemScheduledPrinter extends FutureHolder {",
        "/**\n * 基类：在 {@link ScheduledExecutorService} 上定时打印 {@link StatisticsItem} 累计值。\n */\npublic class StatisticsItemScheduledPrinter extends FutureHolder {",
    ),
    (
        "    protected String name;",
        "    /** 日志前缀名称。 */\n    protected String name;",
    ),
    (
        "    protected StatisticsItemPrinter printer;",
        "    /** 统计项打印机。 */\n    protected StatisticsItemPrinter printer;",
    ),
    (
        "    protected ScheduledExecutorService executor;",
        "    /** 调度线程池。 */\n    protected ScheduledExecutorService executor;",
    ),
    (
        "    protected long interval;",
        "    /** 打印间隔（毫秒）。 */\n    protected long interval;",
    ),
    (
        "    protected InitialDelay initialDelay;",
        "    /** 首次打印延迟策略。 */\n    protected InitialDelay initialDelay;",
    ),
    (
        "    protected Valve valve;",
        "    /** 开关与零行打印策略。 */\n    protected Valve valve;",
    ),
    (
        "    public StatisticsItemScheduledPrinter(String name, StatisticsItemPrinter printer,",
        "    /** 构造定时打印机。 */\n    public StatisticsItemScheduledPrinter(String name, StatisticsItemPrinter printer,",
    ),
    (
        "    /**\n     * schedule a StatisticsItem to print all the values periodically\n     */",
        "    /** 注册统计项，按 interval 周期性打印当前累计值。 */",
    ),
    (
        "    public void remove(final StatisticsItem statisticsItem) {",
        "    /** 取消该统计项关联的全部定时任务。 */\n    public void remove(final StatisticsItem statisticsItem) {",
    ),
    (
        "    public interface InitialDelay {",
        "    /** 首次调度延迟（毫秒）。 */\n    public interface InitialDelay {",
    ),
    (
        "        /**\n         * Get initial delay value\n         * @return\n         */",
        "        /** 返回初始延迟毫秒数。 */",
    ),
    (
        "    public interface Valve {",
        "    /** 控制是否启用打印及是否输出全零行。 */\n    public interface Valve {",
    ),
    (
        "        /**\n         * whether enabled\n         * @return\n         */",
        "        /** 是否启用定时打印。 */",
    ),
    (
        "        /**\n         * whether print zero lines\n         * @return\n         */",
        "        /** 增量全为 0 时是否仍打印一行。 */",
    ),
    (
        "    protected long getInitialDelay() {",
        "    /** 解析初始延迟，无 InitialDelay 时为 0。 */\n    protected long getInitialDelay() {",
    ),
    (
        "    protected boolean enabled() {",
        "    /** 是否启用（依赖 Valve，默认 false）。 */\n    protected boolean enabled() {",
    ),
    (
        "    protected boolean printZeroLine() {",
        "    /** 是否打印全零增量行。 */\n    protected boolean printZeroLine() {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/statistics/StatisticsItemStateGetter.java"] = [
    (
        "public interface StatisticsItemStateGetter {",
        "/**\n * 判断统计项是否仍“在线”，用于空闲清理时保留活跃项。\n */\npublic interface StatisticsItemStateGetter {",
    ),
    (
        "    boolean online(StatisticsItem item);",
        "    /** 返回 {@code item} 是否仍应保留（如连接未断开）。 */\n    boolean online(StatisticsItem item);",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/statistics/StatisticsKindMeta.java"] = [
    (
        "/**\n * Statistics Kind Metadata\n */\npublic class StatisticsKindMeta {",
        "/**\n * 统计类别元数据：名称、子项列表及关联的定时打印机。\n */\npublic class StatisticsKindMeta {",
    ),
    (
        "    private String name;",
        "    /** 统计类别名。 */\n    private String name;",
    ),
    (
        "    private String[] itemNames;",
        "    /** 该类别下各子项名称。 */\n    private String[] itemNames;",
    ),
    (
        "    private StatisticsItemScheduledPrinter scheduledPrinter;",
        "    /** 该类别统计项注册时使用的定时打印机。 */\n    private StatisticsItemScheduledPrinter scheduledPrinter;",
    ),
    (
        "    public String getName() {",
        "    /** 返回类别名。 */\n    public String getName() {",
    ),
    (
        "    public void setName(String name) {",
        "    /** 设置类别名。 */\n    public void setName(String name) {",
    ),
    (
        "    public String[] getItemNames() {",
        "    /** 返回子项名称数组。 */\n    public String[] getItemNames() {",
    ),
    (
        "    public void setItemNames(String[] itemNames) {",
        "    /** 设置子项名称数组。 */\n    public void setItemNames(String[] itemNames) {",
    ),
    (
        "    public StatisticsItemScheduledPrinter getScheduledPrinter() {",
        "    /** 返回定时打印机。 */\n    public StatisticsItemScheduledPrinter getScheduledPrinter() {",
    ),
    (
        "    public void setScheduledPrinter(StatisticsItemScheduledPrinter scheduledPrinter) {",
        "    /** 设置定时打印机。 */\n    public void setScheduledPrinter(StatisticsItemScheduledPrinter scheduledPrinter) {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/statistics/StatisticsManager.java"] = [
    (
        "public class StatisticsManager {",
        "/**\n * 统计中心：按 kind/key 维护 {@link StatisticsItem}，自动调度打印与空闲清理。\n */\npublic class StatisticsManager {",
    ),
    (
        "    /**\n     * Set of Statistics Kind Metadata\n     */",
        "    /** 统计类别名 -> 元数据。 */",
    ),
    (
        "    private Map<String, StatisticsKindMeta> kindMetaMap;",
        "    private Map<String, StatisticsKindMeta> kindMetaMap;",
    ),
    (
        "    /**\n     * item names to calculate statistics brief\n     */",
        "    /** 分位 brief 配置：项名 -> topPercentileMeta。 */",
    ),
    (
        "    private Pair<String, long[][]>[] briefMetas;",
        "    private Pair<String, long[][]>[] briefMetas;",
    ),
    (
        "    /**\n     * Statistics\n     */",
        "    /** kind -> objectKey -> 统计项实例。 */",
    ),
    (
        "    private final ConcurrentHashMap<String, ConcurrentHashMap<String, StatisticsItem>> statsTable",
        "    private final ConcurrentHashMap<String, ConcurrentHashMap<String, StatisticsItem>> statsTable",
    ),
    (
        "    private static final int MAX_IDLE_TIME = 10 * 60 * 1000;",
        "    /** 统计项最大空闲毫秒数，超时且非 online 则移除。 */\n    private static final int MAX_IDLE_TIME = 10 * 60 * 1000;",
    ),
    (
        "    private final ScheduledExecutorService executor = ThreadUtils.newSingleThreadScheduledExecutor(",
        "    /** 后台清理空闲统计项的单线程调度器。 */\n    private final ScheduledExecutorService executor = ThreadUtils.newSingleThreadScheduledExecutor(",
    ),
    (
        "    private StatisticsItemStateGetter statisticsItemStateGetter;",
        "    /** 可选：判定统计项是否仍在线。 */\n    private StatisticsItemStateGetter statisticsItemStateGetter;",
    ),
    (
        "    public StatisticsManager() {",
        "    /** 空元数据表并启动清理任务。 */\n    public StatisticsManager() {",
    ),
    (
        "    public StatisticsManager(Map<String, StatisticsKindMeta> kindMeta) {",
        "    /** 以给定 kind 元数据构造并启动清理。 */\n    public StatisticsManager(Map<String, StatisticsKindMeta> kindMeta) {",
    ),
    (
        "    public void addStatisticsKindMeta(StatisticsKindMeta kindMeta) {",
        "    /** 注册统计类别并初始化 statsTable 槽位。 */\n    public void addStatisticsKindMeta(StatisticsKindMeta kindMeta) {",
    ),
    (
        "    public void setBriefMeta(Pair<String, long[][]>[] briefMetas) {",
        "    /** 设置分位 brief 元数据，供新建项挂载拦截器。 */\n    public void setBriefMeta(Pair<String, long[][]>[] briefMetas) {",
    ),
    (
        "    private void start() {",
        "    /** 启动周期性空闲项清理任务。 */\n    private void start() {",
    ),
    (
        "                        // remove when expired",
        "                        // 超时且非 online 则移除",
    ),
    (
        "    /**\n     * Increment a StatisticsItem\n     *\n     * @param kind\n     * @param key\n     * @param itemAccumulates\n     */",
        "    /**\n     * 递增指定 kind/key 的统计项；不存在则懒创建并调度打印。\n     *\n     * @param kind 统计类别\n     * @param key 统计对象键\n     * @param itemAccumulates 各子项增量\n     */",
    ),
    (
        "            // if not exist, create and schedule",
        "            // 不存在则创建并注册定时打印",
    ),
    (
        "            // do increment",
        "            // 执行累加",
    ),
    (
        "    private void scheduleStatisticsItem(StatisticsItem item) {",
        "    /** 将新统计项交给对应 kind 的 ScheduledPrinter。 */\n    private void scheduleStatisticsItem(StatisticsItem item) {",
    ),
    (
        "    public void remove(StatisticsItem item) {",
        "    /** 从 statsTable 移除并取消定时任务。 */\n    public void remove(StatisticsItem item) {",
    ),
    (
        "    public StatisticsItemStateGetter getStatisticsItemStateGetter() {",
        "    /** 返回在线状态判定器。 */\n    public StatisticsItemStateGetter getStatisticsItemStateGetter() {",
    ),
    (
        "    public void setStatisticsItemStateGetter(StatisticsItemStateGetter statisticsItemStateGetter) {",
        "    /** 设置在线状态判定器。 */\n    public void setStatisticsItemStateGetter(StatisticsItemStateGetter statisticsItemStateGetter) {",
    ),
    (
        "    public void shutdown() {",
        "    /** 关闭清理调度线程池。 */\n    public void shutdown() {",
    ),
]
