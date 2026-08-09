"""RocketMQ 5.5.0 wave15b common metrics/namesrv/producer/queue/resource [15:30] replacements."""

R: dict[str, list[tuple[str, str]]] = {}

R["common/src/main/java/org/apache/rocketmq/common/metrics/NopLongCounter.java"] = [
    (
        "public class NopLongCounter implements LongCounter {",
        "/**\n * OpenTelemetry {@link LongCounter} 的空实现：指标未启用时不记录任何数据。\n */\npublic class NopLongCounter implements LongCounter {",
    ),
    (
        "    @Override public void add(long l) {",
        "    /** 累加计数值（无属性、无上下文）。 */\n    @Override public void add(long l) {",
    ),
    (
        "    @Override public void add(long l, Attributes attributes) {",
        "    /** 带属性标签累加计数值。 */\n    @Override public void add(long l, Attributes attributes) {",
    ),
    (
        "    @Override public void add(long l, Attributes attributes, Context context) {",
        "    /** 带属性与上下文累加计数值。 */\n    @Override public void add(long l, Attributes attributes, Context context) {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/metrics/NopLongHistogram.java"] = [
    (
        "public class NopLongHistogram implements LongHistogram {",
        "/**\n * OpenTelemetry {@link LongHistogram} 的空实现：不记录直方图样本。\n */\npublic class NopLongHistogram implements LongHistogram {",
    ),
    (
        "    @Override public void record(long l) {",
        "    /** 记录一个 long 样本（无属性）。 */\n    @Override public void record(long l) {",
    ),
    (
        "    @Override public void record(long l, Attributes attributes) {",
        "    /** 带属性标签记录样本。 */\n    @Override public void record(long l, Attributes attributes) {",
    ),
    (
        "    @Override public void record(long l, Attributes attributes, Context context) {",
        "    /** 带属性与上下文记录样本。 */\n    @Override public void record(long l, Attributes attributes, Context context) {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/metrics/NopLongUpDownCounter.java"] = [
    (
        "public class NopLongUpDownCounter implements LongUpDownCounter {",
        "/**\n * OpenTelemetry {@link LongUpDownCounter} 的空实现：可增可减计数器的不记录占位。\n */\npublic class NopLongUpDownCounter implements LongUpDownCounter {",
    ),
    (
        "    @Override public void add(long l) {",
        "    /** 增减计数值（无属性）。 */\n    @Override public void add(long l) {",
    ),
    (
        "    @Override public void add(long l, Attributes attributes) {",
        "    /** 带属性标签增减计数值。 */\n    @Override public void add(long l, Attributes attributes) {",
    ),
    (
        "    @Override public void add(long l, Attributes attributes, Context context) {",
        "    /** 带属性与上下文增减计数值。 */\n    @Override public void add(long l, Attributes attributes, Context context) {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/metrics/NopObservableDoubleGauge.java"] = [
    (
        "public class NopObservableDoubleGauge implements ObservableDoubleGauge {",
        "/**\n * OpenTelemetry {@link ObservableDoubleGauge} 的空实现：不注册可观测 double 仪表回调。\n */\npublic class NopObservableDoubleGauge implements ObservableDoubleGauge {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/metrics/NopObservableLongGauge.java"] = [
    (
        "public class NopObservableLongGauge implements ObservableLongGauge {",
        "/**\n * OpenTelemetry {@link ObservableLongGauge} 的空实现：不注册可观测 long 仪表回调。\n */\npublic class NopObservableLongGauge implements ObservableLongGauge {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/namesrv/DefaultTopAddressing.java"] = [
    (
        "public class DefaultTopAddressing implements TopAddressing {",
        "/**\n * 默认 NameServer 地址解析：通过 HTTP 从 WS 域名拉取 NS 地址，并支持 SPI 自定义 {@link TopAddressing}。\n */\npublic class DefaultTopAddressing implements TopAddressing {",
    ),
    (
        "    private String nsAddr;",
        "    /** 缓存或外部设置的 NameServer 地址。 */\n    private String nsAddr;",
    ),
    (
        "    private String wsAddr;",
        "    /** Web 服务地址，用于 HTTP 获取 NameServer 列表。 */\n    private String wsAddr;",
    ),
    (
        "    private String unitName;",
        "    /** 单元化部署的单元名。 */\n    private String unitName;",
    ),
    (
        "    private Map<String, String> para;",
        "    /** 附加 HTTP 查询参数。 */\n    private Map<String, String> para;",
    ),
    (
        "    private List<TopAddressing> topAddressingList;",
        "    /** 通过 ServiceLoader 加载的自定义 TopAddressing 实现列表。 */\n    private List<TopAddressing> topAddressingList;",
    ),
    (
        "    public DefaultTopAddressing(final String wsAddr) {",
        "    /** 仅指定 WS 地址构造。 */\n    public DefaultTopAddressing(final String wsAddr) {",
    ),
    (
        "    public DefaultTopAddressing(final String wsAddr, final String unitName) {",
        "    /** 指定 WS 地址与单元名构造。 */\n    public DefaultTopAddressing(final String wsAddr, final String unitName) {",
    ),
    (
        "    public DefaultTopAddressing(final String unitName, final Map<String, String> para, final String wsAddr) {",
        "    /** 指定单元名、查询参数与 WS 地址构造。 */\n    public DefaultTopAddressing(final String unitName, final Map<String, String> para, final String wsAddr) {",
    ),
    (
        "    private static String clearNewLine(final String str) {",
        "    /** 去除响应字符串首尾空白及首行换行符。 */\n    private static String clearNewLine(final String str) {",
    ),
    (
        "    private List<TopAddressing> loadCustomTopAddressing() {",
        "    /** 通过 {@link ServiceLoader} 加载首个自定义 TopAddressing 实现。 */\n    private List<TopAddressing> loadCustomTopAddressing() {",
    ),
    (
        "        // Return result of default implementation",
        "        // 自定义实现均未返回地址时，走默认 HTTP 拉取",
    ),
    (
        "    public final String fetchNSAddr(boolean verbose, long timeoutMills) {",
        "    /**\n     * 通过 HTTP GET 从 wsAddr 拉取 NameServer 地址。\n     *\n     * @param verbose 失败时是否输出详细日志\n     * @param timeoutMills HTTP 超时毫秒数\n     * @return 成功返回 NS 地址字符串，失败返回 null\n     */",
    ),
    (
        "    public String getNsAddr() {",
        "    /** 返回当前 nsAddr 字段值。 */\n    public String getNsAddr() {",
    ),
    (
        "    public void setNsAddr(String nsAddr) {",
        "    /** 设置 nsAddr 缓存。 */\n    public void setNsAddr(String nsAddr) {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/namesrv/NameServerUpdateCallback.java"] = [
    (
        "public interface NameServerUpdateCallback {",
        "/**\n * NameServer 地址变更回调：客户端在 NS 列表更新时通知上层。\n */\npublic interface NameServerUpdateCallback {",
    ),
    (
        "    String onNameServerAddressChange(String namesrvAddress);",
        "    /**\n     * NameServer 地址发生变化时调用。\n     *\n     * @param namesrvAddress 新的 NameServer 地址\n     * @return 处理后的地址（可为 null）\n     */",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/namesrv/NamesrvConfig.java"] = [
    (
        "public class NamesrvConfig {",
        "/**\n * NameServer 进程配置项：线程池、路径、Topic 策略及 Controller 集成等。\n */\npublic class NamesrvConfig {",
    ),
    (
        "    private String rocketmqHome = MixAll.ROCKETMQ_HOME_DIR;",
        "    /** RocketMQ 安装根目录。 */\n    private String rocketmqHome = MixAll.ROCKETMQ_HOME_DIR;",
    ),
    (
        "    private String kvConfigPath = System.getProperty(\"user.home\") + File.separator + \"namesrv\" + File.separator + \"kvConfig.json\";",
        "    /** KV 配置持久化文件路径。 */\n    private String kvConfigPath = System.getProperty(\"user.home\") + File.separator + \"namesrv\" + File.separator + \"kvConfig.json\";",
    ),
    (
        "    private String configStorePath = System.getProperty(\"user.home\") + File.separator + \"namesrv\" + File.separator + \"namesrv.properties\";",
        "    /** NameServer 主配置文件存储路径。 */\n    private String configStorePath = System.getProperty(\"user.home\") + File.separator + \"namesrv\" + File.separator + \"namesrv.properties\";",
    ),
    (
        "    private String productEnvName = \"center\";",
        "    /** 生产环境标识名。 */\n    private String productEnvName = \"center\";",
    ),
    (
        "    private boolean clusterTest = false;",
        "    /** 是否为集群测试模式。 */\n    private boolean clusterTest = false;",
    ),
    (
        "    private boolean orderMessageEnable = false;",
        "    /** 是否启用顺序消息相关能力。 */\n    private boolean orderMessageEnable = false;",
    ),
    (
        "    private boolean returnOrderTopicConfigToBroker = true;",
        "    /** 是否向 Broker 返回顺序 Topic 配置。 */\n    private boolean returnOrderTopicConfigToBroker = true;",
    ),
    (
        "    /**\n     * Indicates the nums of thread to handle client requests, like GET_ROUTEINTO_BY_TOPIC.\n     */",
        "    /** 处理客户端请求（如 GET_ROUTEINTO_BY_TOPIC）的线程数。 */",
    ),
    (
        "    /**\n     * Indicates the nums of thread to handle broker or operation requests, like REGISTER_BROKER.\n     */",
        "    /** 处理 Broker/运维请求（如 REGISTER_BROKER）的线程数。 */",
    ),
    (
        "    /**\n     * Indicates the capacity of queue to hold client requests.\n     */",
        "    /** 客户端请求队列容量。 */",
    ),
    (
        "    /**\n     * Indicates the capacity of queue to hold broker or operation requests.\n     */",
        "    /** Broker/运维请求队列容量。 */",
    ),
    (
        "    /**\n     * Interval of periodic scanning for non-active broker;\n     */",
        "    /** 扫描非活跃 Broker 的周期间隔（毫秒）。 */",
    ),
    (
        "    private int unRegisterBrokerQueueCapacity = 3000;",
        "    /** 待注销 Broker 请求队列容量。 */\n    private int unRegisterBrokerQueueCapacity = 3000;",
    ),
    (
        "    /**\n     * Support acting master or not.\n     *\n     * The slave can be an acting master when master node is down to support following operations:\n     * 1. support lock/unlock message queue operation.\n     * 2. support searchOffset, query maxOffset/minOffset operation.\n     * 3. support query earliest msg store time.\n     */",
        "    /**\n     * 是否支持 Acting Master：主节点宕机时从节点可临时承担主职责，支持：\n     * 1. 消息队列 lock/unlock；\n     * 2. searchOffset、maxOffset/minOffset 查询；\n     * 3. 最早消息存储时间查询。\n     */",
    ),
    (
        "    private volatile boolean enableAllTopicList = true;",
        "    /** 是否启用全量 Topic 列表接口。 */\n    private volatile boolean enableAllTopicList = true;",
    ),
    (
        "    private volatile boolean enableTopicList = true;",
        "    /** 是否启用 Topic 列表相关能力。 */\n    private volatile boolean enableTopicList = true;",
    ),
    (
        "    private volatile boolean notifyMinBrokerIdChanged = false;",
        "    /** 最小 BrokerId 变更时是否通知客户端。 */\n    private volatile boolean notifyMinBrokerIdChanged = false;",
    ),
    (
        "    /**\n     * Is startup the controller in this name-srv\n     */",
        "    /** 是否在本 NameServer 进程中启动 Controller。 */",
    ),
    (
        "    private volatile boolean needWaitForService = false;",
        "    /** 启动时是否等待依赖服务就绪。 */\n    private volatile boolean needWaitForService = false;",
    ),
    (
        "    private int waitSecondsForService = 45;",
        "    /** 等待依赖服务的超时秒数。 */\n    private int waitSecondsForService = 45;",
    ),
    (
        "    /**\n     * If enable this flag, the topics that don't exist in broker registration payload will be deleted from name server.\n     *\n     * WARNING:\n     * 1. Enable this flag and \"enableSingleTopicRegister\" of broker config meanwhile to avoid losing topic route info unexpectedly.\n     * 2. This flag does not support static topic currently.\n     */",
        "    /**\n     * 启用后，Broker 注册载荷中不存在的 Topic 将从 NameServer 路由中删除。\n     *\n     * 注意：\n     * 1. 需与 Broker 的 enableSingleTopicRegister 同时启用，避免意外丢失路由；\n     * 2. 暂不支持静态 Topic。\n     */",
    ),
    (
        "    /**\n     * Config in this black list will be not allowed to update by command.\n     * Try to update this config black list by restart process.\n     * Try to update configures in black list by restart process.\n     */",
        "    /** 配置黑名单：名单内项不允许通过命令热更新，需重启进程修改。 */",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/namesrv/NamesrvUtil.java"] = [
    (
        "public class NamesrvUtil {",
        "/**\n * NameServer 相关常量与工具方法。\n */\npublic class NamesrvUtil {",
    ),
    (
        "    public static final String NAMESPACE_ORDER_TOPIC_CONFIG = \"ORDER_TOPIC_CONFIG\";",
        "    /** KV 命名空间：顺序 Topic 配置。 */\n    public static final String NAMESPACE_ORDER_TOPIC_CONFIG = \"ORDER_TOPIC_CONFIG\";",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/namesrv/TopAddressing.java"] = [
    (
        "public interface TopAddressing {",
        "/**\n * 顶层 NameServer 地址解析 SPI：从 WS/云配置等来源获取 NS 地址。\n */\npublic interface TopAddressing {",
    ),
    (
        "    String fetchNSAddr();",
        "    /** 获取当前 NameServer 地址字符串。 */\n    String fetchNSAddr();",
    ),
    (
        "    void registerChangeCallBack(NameServerUpdateCallback changeCallBack);",
        "    /** 注册 NameServer 地址变更回调。 */\n    void registerChangeCallBack(NameServerUpdateCallback changeCallBack);",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/producer/RecallMessageHandle.java"] = [
    (
        "/**\n * handle to recall a message, only support delay message for now\n * v1 pattern like this:\n * version topic brokerName timestamp messageId\n * use Base64 to encode it\n */",
        "/**\n * 延迟消息撤回句柄编解码：当前仅支持 v1 格式。\n * 明文格式：{@code version topic brokerName timestamp messageId}（空格分隔），再经 URL-safe Base64 编码。\n */",
    ),
    (
        "    public static class HandleV1 extends RecallMessageHandle {",
        "    /** v1 撤回句柄：携带 Topic、Broker、时间戳与消息 ID。 */\n    public static class HandleV1 extends RecallMessageHandle {",
    ),
    (
        "        private String messageId; // id of unique key",
        "        /** 消息唯一键 ID。 */\n        private String messageId; // id of unique key",
    ),
    (
        "        // no param check",
        "        // 不做参数校验",
    ),
    (
        "        public static String buildHandle(String topic, String brokerName, String timestampStr, String messageId) {",
        "        /** 构造 v1 撤回句柄 Base64 字符串。 */\n        public static String buildHandle(String topic, String brokerName, String timestampStr, String messageId) {",
    ),
    (
        "    public static RecallMessageHandle decodeHandle(String handle) throws DecoderException {",
        "    /**\n     * 解码撤回句柄字符串。\n     *\n     * @throws DecoderException 格式非法或版本不匹配\n     */",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/queue/ConcurrentTreeMap.java"] = [
    (
        "/**\n * thread safe\n */",
        "/**\n * 线程安全的 TreeMap 包装：结合 {@link RoundQueue} 限制可跟踪键的数量。\n *\n * @param <K> 键类型\n * @param <V> 值类型\n */",
    ),
    (
        "    private final ReentrantLock lock;",
        "    /** 保护 tree 与 roundQueue 的可重入锁（公平模式）。 */\n    private final ReentrantLock lock;",
    ),
    (
        "    private TreeMap<K, V> tree;",
        "    /** 有序键值存储。 */\n    private TreeMap<K, V> tree;",
    ),
    (
        "    private RoundQueue<K> roundQueue;",
        "    /** 最近访问键的环形队列，用于容量控制。 */\n    private RoundQueue<K> roundQueue;",
    ),
    (
        "    public ConcurrentTreeMap(int capacity, Comparator<? super K> comparator) {",
        "    /**\n     * @param capacity roundQueue 容量上限\n     * @param comparator 键比较器\n     */",
    ),
    (
        "    public Map.Entry<K, V> pollFirstEntry() {",
        "    /** 移除并返回最小键对应的 Map.Entry。 */\n    public Map.Entry<K, V> pollFirstEntry() {",
    ),
    (
        "    public V putIfAbsentAndRetExsit(K key, V value) {",
        "    /**\n     * 若 roundQueue 接受该键则尝试 putIfAbsent，并返回已存在或新写入的值；\n     * 键已在 roundQueue 中则仅返回 tree 中现有值。\n     */",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/queue/RoundQueue.java"] = [
    (
        "/**\n * not thread safe\n */",
        "/**\n * 固定容量的环形队列：新元素入队时若已满则淘汰最旧元素；非线程安全。\n *\n * @param <E> 元素类型\n */",
    ),
    (
        "    private Queue<E> queue;",
        "    /** 底层 FIFO 队列。 */\n    private Queue<E> queue;",
    ),
    (
        "    private int capacity;",
        "    /** 最大容量。 */\n    private int capacity;",
    ),
    (
        "    public RoundQueue(int capacity) {",
        "    /** 指定容量构造环形队列。 */\n    public RoundQueue(int capacity) {",
    ),
    (
        "    public boolean put(E e) {",
        "    /**\n     * 尝试加入元素：已存在则返回 false；满则 poll 队首后再 add。\n     *\n     * @return 实际新增元素返回 true，重复元素返回 false\n     */",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/resource/ResourcePattern.java"] = [
    (
        "public enum ResourcePattern {",
        "/**\n * ACL 资源匹配模式：任意、字面量或前缀匹配。\n */\npublic enum ResourcePattern {",
    ),
    (
        "    ANY((byte) 1, \"ANY\"),",
        "    /** 匹配任意资源。 */\n    ANY((byte) 1, \"ANY\"),",
    ),
    (
        "    LITERAL((byte) 2, \"LITERAL\"),",
        "    /** 字面量精确匹配。 */\n    LITERAL((byte) 2, \"LITERAL\"),",
    ),
    (
        "    PREFIXED((byte) 3, \"PREFIXED\");",
        "    /** 前缀匹配。 */\n    PREFIXED((byte) 3, \"PREFIXED\");",
    ),
    (
        "    @JSONField(value = true)\n    private final byte code;",
        "    /** 序列化/反序列化使用的模式编码。 */\n    @JSONField(value = true)\n    private final byte code;",
    ),
    (
        "    private final String name;",
        "    /** 模式名称字符串。 */\n    private final String name;",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/resource/ResourceType.java"] = [
    (
        "public enum ResourceType {",
        "/**\n * ACL 资源类型：集群、命名空间、Topic、消费组等。\n */\npublic enum ResourceType {",
    ),
    (
        "    UNKNOWN((byte) 0, \"Unknown\"),",
        "    /** 未知类型。 */\n    UNKNOWN((byte) 0, \"Unknown\"),",
    ),
    (
        "    ANY((byte) 1, \"Any\"),",
        "    /** 任意资源类型。 */\n    ANY((byte) 1, \"Any\"),",
    ),
    (
        "    CLUSTER((byte) 2, \"Cluster\"),",
        "    /** 集群级资源。 */\n    CLUSTER((byte) 2, \"Cluster\"),",
    ),
    (
        "    NAMESPACE((byte) 3, \"Namespace\"),",
        "    /** 命名空间级资源。 */\n    NAMESPACE((byte) 3, \"Namespace\"),",
    ),
    (
        "    TOPIC((byte) 4, \"Topic\"),",
        "    /** Topic 资源。 */\n    TOPIC((byte) 4, \"Topic\"),",
    ),
    (
        "    GROUP((byte) 5, \"Group\");",
        "    /** 消费组资源。 */\n    GROUP((byte) 5, \"Group\");",
    ),
    (
        "    @JSONField(value = true)\n    private final byte code;",
        "    /** JSON 序列化主键字段。 */\n    @JSONField(value = true)\n    private final byte code;",
    ),
    (
        "    private final String name;",
        "    /** 资源类型显示名。 */\n    private final String name;",
    ),
    (
        "    public static ResourceType getByName(String name) {",
        "    /** 按名称（忽略大小写）查找资源类型，未匹配返回 null。 */\n    public static ResourceType getByName(String name) {",
    ),
]
