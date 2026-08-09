"""RocketMQ 5.5.0 wave12a common core types [0:15] replacements."""

R: dict[str, list[tuple[str, str]]] = {}

R["common/src/main/java/org/apache/rocketmq/common/AbortProcessException.java"] = [
    (
        "/**\n *\n * This exception is used for broker hooks only : SendMessageHook, ConsumeMessageHook, RPCHook\n * This exception is not ignored while executing hooks and it means that\n * certain processor should return an immediate error response to the client. The\n * error response code is included in AbortProcessException.  it's naming might\n * be confusing, so feel free to refactor this class. Also when any class implements\n * the 3 hook interface mentioned above we should be careful if we want to throw\n * an AbortProcessException, because it will change the control flow of broker\n * and cause a RemotingCommand return error immediately. So be aware of the side\n * effect before throw AbortProcessException in your implementation.\n *\n */",
        "/**\n * Broker Hook 专用异常（SendMessageHook、ConsumeMessageHook、RPCHook）。\n * 执行 Hook 时不会被忽略，表示处理器应立即向客户端返回错误响应；\n * 错误码封装在本异常中。抛出后会改变 Broker 控制流，使 RemotingCommand 立即返回错误，使用前需了解副作用。\n */",
    ),
    (
        "public class AbortProcessException extends RuntimeException {",
        "public class AbortProcessException extends RuntimeException {",
    ),
    (
        "    private static final long serialVersionUID = -5728810933841185841L;",
        "    private static final long serialVersionUID = -5728810933841185841L;",
    ),
    (
        "    private int responseCode;",
        "    /** 返回给客户端的响应码。 */\n    private int responseCode;",
    ),
    (
        "    private String errorMessage;",
        "    /** 错误描述信息。 */\n    private String errorMessage;",
    ),
    (
        "    public AbortProcessException(String errorMessage, Throwable cause) {",
        "    /** 携带错误信息与根因构造异常（responseCode 默认为 -1）。 */\n    public AbortProcessException(String errorMessage, Throwable cause) {",
    ),
    (
        "    public AbortProcessException(int responseCode, String errorMessage) {",
        "    /** 按响应码与错误描述构造异常。 */\n    public AbortProcessException(int responseCode, String errorMessage) {",
    ),
    (
        "    public int getResponseCode() {",
        "    /** 获取响应码。 */\n    public int getResponseCode() {",
    ),
    (
        "    public AbortProcessException setResponseCode(final int responseCode) {",
        "    /** 设置响应码并返回自身（链式调用）。 */\n    public AbortProcessException setResponseCode(final int responseCode) {",
    ),
    (
        "    public String getErrorMessage() {",
        "    /** 获取错误描述。 */\n    public String getErrorMessage() {",
    ),
    (
        "    public void setErrorMessage(final String errorMessage) {",
        "    /** 设置错误描述。 */\n    public void setErrorMessage(final String errorMessage) {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/BoundaryType.java"] = [
    (
        "public enum BoundaryType {",
        "/** 边界类型：用于表示期望的下界或上界。 */\npublic enum BoundaryType {",
    ),
    (
        "    /**\n     * Indicate that lower boundary is expected.\n     */",
        "    /** 期望下界。 */",
    ),
    (
        "    /**\n     * Indicate that upper boundary is expected.\n     */",
        "    /** 期望上界。 */",
    ),
    (
        "    private String name;",
        "    /** 边界类型名称字符串。 */\n    private String name;",
    ),
    (
        "    BoundaryType(String name) {",
        "    BoundaryType(String name) {",
    ),
    (
        "    public String getName() {",
        "    /** 返回边界类型名称。 */\n    public String getName() {",
    ),
    (
        "    public static BoundaryType getType(String name) {",
        "    /** 按名称解析边界类型，无法识别时默认 LOWER。 */\n    public static BoundaryType getType(String name) {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/BrokerConfigSingleton.java"] = [
    (
        "public class BrokerConfigSingleton {",
        "/**\n * BrokerConfig 进程级单例持有者：全局仅允许初始化一次。\n */\npublic class BrokerConfigSingleton {",
    ),
    (
        "    private static AtomicBoolean isInit = new AtomicBoolean();",
        "    /** 是否已完成初始化。 */\n    private static AtomicBoolean isInit = new AtomicBoolean();",
    ),
    (
        "    private static BrokerConfig brokerConfig;",
        "    /** 全局 Broker 配置实例。 */\n    private static BrokerConfig brokerConfig;",
    ),
    (
        "    public static BrokerConfig getBrokerConfig() {",
        "    /** 获取 Broker 配置，未初始化时抛 IllegalArgumentException。 */\n    public static BrokerConfig getBrokerConfig() {",
    ),
    (
        "    public static void setBrokerConfig(BrokerConfig brokerConfig) {",
        "    /** 设置 Broker 配置，重复初始化抛 IllegalArgumentException。 */\n    public static void setBrokerConfig(BrokerConfig brokerConfig) {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/BrokerIdentity.java"] = [
    (
        "public class BrokerIdentity {",
        "/**\n * Broker 身份标识：集群名、Broker 名、BrokerId 及容器模式标志。\n */\npublic class BrokerIdentity {",
    ),
    (
        "    private static final String DEFAULT_CLUSTER_NAME = \"DefaultCluster\";",
        "    /** 默认集群名称。 */\n    private static final String DEFAULT_CLUSTER_NAME = \"DefaultCluster\";",
    ),
    (
        "    protected static final Logger LOGGER = LoggerFactory.getLogger(LoggerName.COMMON_LOGGER_NAME);",
        "    /** 公共模块日志器。 */\n    protected static final Logger LOGGER = LoggerFactory.getLogger(LoggerName.COMMON_LOGGER_NAME);",
    ),
    (
        "    private static String localHostName;",
        "    /** 本机主机名，静态块中初始化。 */\n    private static String localHostName;",
    ),
    (
        "    // load it after the localHostName is initialized\n    public static final BrokerIdentity BROKER_CONTAINER_IDENTITY = new BrokerIdentity(true);",
        "    /** Broker 容器模式的预置身份（在 localHostName 初始化后加载）。 */\n    public static final BrokerIdentity BROKER_CONTAINER_IDENTITY = new BrokerIdentity(true);",
    ),
    (
        "    @ImportantField\n    private String brokerName = defaultBrokerName();",
        "    /** Broker 名称（重要字段）。 */\n    @ImportantField\n    private String brokerName = defaultBrokerName();",
    ),
    (
        "    @ImportantField\n    private String brokerClusterName = DEFAULT_CLUSTER_NAME;",
        "    /** 所属集群名称（重要字段）。 */\n    @ImportantField\n    private String brokerClusterName = DEFAULT_CLUSTER_NAME;",
    ),
    (
        "    @ImportantField\n    private volatile long brokerId = MixAll.MASTER_ID;",
        "    /** Broker 角色 ID，默认 Master（重要字段）。 */\n    @ImportantField\n    private volatile long brokerId = MixAll.MASTER_ID;",
    ),
    (
        "    private boolean isBrokerContainer = false;",
        "    /** 是否为 Broker 容器实体本身。 */\n    private boolean isBrokerContainer = false;",
    ),
    (
        "    // Do not set it manually, it depends on the startup mode\n    // Broker start by BrokerStartup is false, start or add by BrokerContainer is true\n    private boolean isInBrokerContainer = false;",
        "    /** 是否运行在 BrokerContainer 内（由启动方式决定，勿手动设置）。 */\n    private boolean isInBrokerContainer = false;",
    ),
    (
        "    public BrokerIdentity() {",
        "    /** 默认构造，使用主机名等默认值。 */\n    public BrokerIdentity() {",
    ),
    (
        "    public BrokerIdentity(boolean isBrokerContainer) {",
        "    /** 指定是否为 Broker 容器身份。 */\n    public BrokerIdentity(boolean isBrokerContainer) {",
    ),
    (
        "    public BrokerIdentity(String brokerClusterName, String brokerName, long brokerId) {",
        "    /** 按集群、名称与 ID 构造身份。 */\n    public BrokerIdentity(String brokerClusterName, String brokerName, long brokerId) {",
    ),
    (
        "    public BrokerIdentity(String brokerClusterName, String brokerName, long brokerId, boolean isInBrokerContainer) {",
        "    /** 完整构造，含容器内运行标志。 */\n    public BrokerIdentity(String brokerClusterName, String brokerName, long brokerId, boolean isInBrokerContainer) {",
    ),
    (
        "    public String getBrokerName() {",
        "    /** 获取 Broker 名称。 */\n    public String getBrokerName() {",
    ),
    (
        "    public void setBrokerName(final String brokerName) {",
        "    /** 设置 Broker 名称。 */\n    public void setBrokerName(final String brokerName) {",
    ),
    (
        "    public String getBrokerClusterName() {",
        "    /** 获取集群名称。 */\n    public String getBrokerClusterName() {",
    ),
    (
        "    public void setBrokerClusterName(final String brokerClusterName) {",
        "    /** 设置集群名称。 */\n    public void setBrokerClusterName(final String brokerClusterName) {",
    ),
    (
        "    public long getBrokerId() {",
        "    /** 获取 Broker ID。 */\n    public long getBrokerId() {",
    ),
    (
        "    public void setBrokerId(final long brokerId) {",
        "    /** 设置 Broker ID。 */\n    public void setBrokerId(final long brokerId) {",
    ),
    (
        "    public boolean isInBrokerContainer() {",
        "    /** 是否在 BrokerContainer 内运行。 */\n    public boolean isInBrokerContainer() {",
    ),
    (
        "    public void setInBrokerContainer(boolean inBrokerContainer) {",
        "    /** 设置是否在 BrokerContainer 内运行。 */\n    public void setInBrokerContainer(boolean inBrokerContainer) {",
    ),
    (
        "    private String defaultBrokerName() {",
        "    /** 默认 Broker 名：主机名或 DEFAULT_BROKER。 */\n    private String defaultBrokerName() {",
    ),
    (
        "    public String getCanonicalName() {",
        "    /** 规范名称：容器为 BrokerContainer，否则 cluster_name_id。 */\n    public String getCanonicalName() {",
    ),
    (
        "    public String getIdentifier() {",
        "    /** 带 # 分隔符的标识字符串。 */\n    public String getIdentifier() {",
    ),
    (
        "    @Override\n    public boolean equals(final Object o) {",
        "    /** 按 cluster、name、id 比较相等性。 */\n    @Override\n    public boolean equals(final Object o) {",
    ),
    (
        "    @Override\n    public int hashCode() {",
        "    /** 基于 name、cluster、id 的哈希码。 */\n    @Override\n    public int hashCode() {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/CheckRocksdbCqWriteResult.java"] = [
    (
        "public class CheckRocksdbCqWriteResult {",
        "/**\n * RocksDB CommitLog 队列写入校验结果。\n */\npublic class CheckRocksdbCqWriteResult {",
    ),
    (
        "    String checkResult;",
        "    /** 校验结果描述文本。 */\n    String checkResult;",
    ),
    (
        "    int checkStatus;",
        "    /** 校验状态码，对应 {@link CheckStatus}。 */\n    int checkStatus;",
    ),
    (
        "    public enum CheckStatus {",
        "    /** RocksDB CQ 写入校验状态枚举。 */\n    public enum CheckStatus {",
    ),
    (
        "        CHECK_OK(0),",
        "        /** 校验通过。 */\n        CHECK_OK(0),",
    ),
    (
        "        CHECK_NOT_OK(1),",
        "        /** 校验未通过。 */\n        CHECK_NOT_OK(1),",
    ),
    (
        "        CHECK_IN_PROGRESS(2),",
        "        /** 校验进行中。 */\n        CHECK_IN_PROGRESS(2),",
    ),
    (
        "        CHECK_ERROR(3);",
        "        /** 校验过程出错。 */\n        CHECK_ERROR(3);",
    ),
    (
        "        private int value;",
        "        /** 状态整型值。 */\n        private int value;",
    ),
    (
        "        CheckStatus(int value) {",
        "        CheckStatus(int value) {",
    ),
    (
        "        public int getValue() {",
        "        /** 返回状态整型值。 */\n        public int getValue() {",
    ),
    (
        "    public String getCheckResult() {",
        "    /** 获取校验结果描述。 */\n    public String getCheckResult() {",
    ),
    (
        "    public void setCheckResult(String checkResult) {",
        "    /** 设置校验结果描述。 */\n    public void setCheckResult(String checkResult) {",
    ),
    (
        "    public int getCheckStatus() {",
        "    /** 获取校验状态码。 */\n    public int getCheckStatus() {",
    ),
    (
        "    public void setCheckStatus(int checkStatus) {",
        "    /** 设置校验状态码。 */\n    public void setCheckStatus(int checkStatus) {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/ConfigManager.java"] = [
    (
        "public abstract class ConfigManager {",
        "/**\n * 配置持久化抽象基类：JSON 编码/解码、主文件与 .bak 备份的原子读写。\n */\npublic abstract class ConfigManager {",
    ),
    (
        "    private static final Logger log = LoggerFactory.getLogger(LoggerName.COMMON_LOGGER_NAME);",
        "    /** 配置管理日志器。 */\n    private static final Logger log = LoggerFactory.getLogger(LoggerName.COMMON_LOGGER_NAME);",
    ),
    (
        "    public boolean load() {",
        "    /** 从主配置文件加载；失败或空文件时尝试 .bak 备份。 */\n    public boolean load() {",
    ),
    (
        "                // delete invalid file",
        "                // 删除无效主配置文件",
    ),
    (
        "    private boolean loadBak() {",
        "    /** 从 .bak 备份文件加载配置。 */\n    private boolean loadBak() {",
    ),
    (
        "    public synchronized <T> void persist(String topicName, T t) {",
        "    /** 按 Topic 持久化（当前为 stub，委托 persist()）。 */\n    public synchronized <T> void persist(String topicName, T t) {",
    ),
    (
        "        // stub for future",
        "        // 预留扩展",
    ),
    (
        "    public synchronized <T> void persist(Map<String, T> m) {",
        "    /** 批量持久化（当前为 stub，委托 persist()）。 */\n    public synchronized <T> void persist(Map<String, T> m) {",
    ),
    (
        "    public synchronized void persist() {",
        "    /** 将 encode 结果原子写入配置文件（先备份再写入并 fsync）。 */\n    public synchronized void persist() {",
    ),
    (
        "                // bak metrics file",
        "                // 备份现有配置文件",
    ),
    (
        "                    // atomic move",
        "                    // 原子移动为 .bak",
    ),
    (
        "                    // sync the directory, ensure that the bak file is visible",
        "                    // fsync 目录确保备份可见",
    ),
    (
        "                    // sync the directory, ensure that the config file is visible",
        "                    // fsync 目录确保新配置可见",
    ),
    (
        "    public boolean stop() {",
        "    /** 停止配置管理（默认可直接返回 true）。 */\n    public boolean stop() {",
    ),
    (
        "    public void shutdown() {",
        "    /** 关闭并调用 stop()。 */\n    public void shutdown() {",
    ),
    (
        "    public abstract String configFilePath();",
        "    /** 配置文件路径。 */\n    public abstract String configFilePath();",
    ),
    (
        "    public abstract String encode();",
        "    /** 编码为 JSON 字符串（默认格式）。 */\n    public abstract String encode();",
    ),
    (
        "    public abstract String encode(final boolean prettyFormat);",
        "    /** 编码为 JSON，可选美化格式。 */\n    public abstract String encode(final boolean prettyFormat);",
    ),
    (
        "    public abstract void decode(final String jsonString);",
        "    /** 从 JSON 字符串解码配置。 */\n    public abstract void decode(final String jsonString);",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/ControllerConfig.java"] = [
    (
        "public class ControllerConfig {",
        "/**\n * Controller 节点配置：DLedger/jRaft 类型、选举、线程池、指标导出等。\n */\npublic class ControllerConfig {",
    ),
    (
        "    private String rocketmqHome = MixAll.ROCKETMQ_HOME_DIR;",
        "    /** RocketMQ 安装根目录。 */\n    private String rocketmqHome = MixAll.ROCKETMQ_HOME_DIR;",
    ),
    (
        "    private String configStorePath = System.getProperty(\"user.home\") + File.separator + \"controller\" + File.separator + \"controller.properties\";",
        "    /** 配置文件存储路径。 */\n    private String configStorePath = System.getProperty(\"user.home\") + File.separator + \"controller\" + File.separator + \"controller.properties\";",
    ),
    (
        "    public static final String DLEDGER_CONTROLLER = \"DLedger\";",
        "    /** DLedger 型 Controller 类型标识。 */\n    public static final String DLEDGER_CONTROLLER = \"DLedger\";",
    ),
    (
        "    public static final String JRAFT_CONTROLLER = \"jRaft\";",
        "    /** jRaft 型 Controller 类型标识。 */\n    public static final String JRAFT_CONTROLLER = \"jRaft\";",
    ),
    (
        "    private JraftConfig jraftConfig = new JraftConfig();",
        "    /** jRaft 相关子配置。 */\n    private JraftConfig jraftConfig = new JraftConfig();",
    ),
    (
        "    private String controllerType = DLEDGER_CONTROLLER;",
        "    /** Controller 实现类型，默认 DLedger。 */\n    private String controllerType = DLEDGER_CONTROLLER;",
    ),
    (
        "    /**\n     * Interval of periodic scanning for non-active broker;\n     * Unit: millisecond\n     */",
        "    /** 周期性扫描非活跃 Broker 的间隔（毫秒）。 */",
    ),
    (
        "    /**\n     * Indicates the nums of thread to handle broker or operation requests, like REGISTER_BROKER.\n     */",
        "    /** 处理 Broker/操作请求（如 REGISTER_BROKER）的线程数。 */",
    ),
    (
        "    /**\n     * Indicates the capacity of queue to hold client requests.\n     */",
        "    /** 客户端请求队列容量。 */",
    ),
    (
        "    private String controllerDLegerGroup;",
        "    /** DLedger Controller 组名。 */\n    private String controllerDLegerGroup;",
    ),
    (
        "    private String controllerDLegerPeers;",
        "    /** DLedger 对等节点列表。 */\n    private String controllerDLegerPeers;",
    ),
    (
        "    private String controllerDLegerSelfId;",
        "    /** 本节点 DLedger SelfId。 */\n    private String controllerDLegerSelfId;",
    ),
    (
        "    private int mappedFileSize = 1024 * 1024 * 1024;",
        "    /** 映射文件大小（字节），默认 1GB。 */\n    private int mappedFileSize = 1024 * 1024 * 1024;",
    ),
    (
        "    private String controllerStorePath = \"\";",
        "    /** Controller 数据存储路径。 */\n    private String controllerStorePath = \"\";",
    ),
    (
        "    /**\n     * Max retry count for electing master when failed because of network or system error.\n     */",
        "    /** 选举 Master 失败时的最大重试次数。 */",
    ),
    (
        "    /**\n     * Whether the controller can elect a master which is not in the syncStateSet.\n     */",
        "    /** 是否允许选举不在 syncStateSet 中的 Master（非干净选举）。 */",
    ),
    (
        "    /**\n     * Whether process read event\n     */",
        "    /** 是否处理读事件。 */",
    ),
    (
        "    /**\n     * Whether notify broker when its role changed\n     */",
        "    /** Broker 角色变更时是否通知 Broker。 */",
    ),
    (
        "    /**\n     * Interval of periodic scanning for non-active master in each broker-set;\n     * Unit: millisecond\n     */",
        "    /** 每个 Broker-Set 内扫描非活跃 Master 的间隔（毫秒）。 */",
    ),
    (
        "    private MetricsExporterType metricsExporterType = MetricsExporterType.DISABLE;",
        "    /** 指标导出器类型。 */\n    private MetricsExporterType metricsExporterType = MetricsExporterType.DISABLE;",
    ),
    (
        "    private String metricsGrpcExporterTarget = \"\";",
        "    /** gRPC 指标导出目标地址。 */\n    private String metricsGrpcExporterTarget = \"\";",
    ),
    (
        "    private String metricsGrpcExporterHeader = \"\";",
        "    /** gRPC 指标导出请求头。 */\n    private String metricsGrpcExporterHeader = \"\";",
    ),
    (
        "    private long metricGrpcExporterTimeOutInMills = 3 * 1000;",
        "    /** gRPC 指标导出超时（毫秒）。 */\n    private long metricGrpcExporterTimeOutInMills = 3 * 1000;",
    ),
    (
        "    private long metricGrpcExporterIntervalInMills = 60 * 1000;",
        "    /** gRPC 指标导出间隔（毫秒）。 */\n    private long metricGrpcExporterIntervalInMills = 60 * 1000;",
    ),
    (
        "    private long metricLoggingExporterIntervalInMills = 10 * 1000;",
        "    /** 日志指标导出间隔（毫秒）。 */\n    private long metricLoggingExporterIntervalInMills = 10 * 1000;",
    ),
    (
        "    private int metricsPromExporterPort = 5557;",
        "    /** Prometheus 指标导出端口。 */\n    private int metricsPromExporterPort = 5557;",
    ),
    (
        "    private String metricsPromExporterHost = \"\";",
        "    /** Prometheus 指标导出监听地址。 */\n    private String metricsPromExporterHost = \"\";",
    ),
    (
        "    // Label pairs in CSV. Each label follows pattern of Key:Value. eg: instance_id:xxx,uid:xxx",
        "    // 指标标签 CSV，格式 Key:Value，如 instance_id:xxx,uid:xxx",
    ),
    (
        "    private String metricsLabel = \"\";",
        "    /** 指标标签 CSV 字符串。 */\n    private String metricsLabel = \"\";",
    ),
    (
        "    private boolean metricsInDelta = false;",
        "    /** 是否以增量方式导出指标。 */\n    private boolean metricsInDelta = false;",
    ),
    (
        "    /**\n     * Config in this black list will be not allowed to update by command.\n     * Try to update this config black list by restart process.\n     * Try to update configures in black list by restart process.\n     */",
        "    /** 命令行不可热更新的配置黑名单，需重启进程修改。 */",
    ),
    (
        "    public String getConfigBlackList() {",
        "    /** 获取配置黑名单。 */\n    public String getConfigBlackList() {",
    ),
    (
        "    public void setConfigBlackList(String configBlackList) {",
        "    /** 设置配置黑名单。 */\n    public void setConfigBlackList(String configBlackList) {",
    ),
    (
        "    public String getRocketmqHome() {",
        "    /** 获取 RocketMQ 安装目录。 */\n    public String getRocketmqHome() {",
    ),
    (
        "    public void setRocketmqHome(String rocketmqHome) {",
        "    /** 设置 RocketMQ 安装目录。 */\n    public void setRocketmqHome(String rocketmqHome) {",
    ),
    (
        "    public String getConfigStorePath() {",
        "    /** 获取配置文件路径。 */\n    public String getConfigStorePath() {",
    ),
    (
        "    public void setConfigStorePath(String configStorePath) {",
        "    /** 设置配置文件路径。 */\n    public void setConfigStorePath(String configStorePath) {",
    ),
    (
        "    public long getScanNotActiveBrokerInterval() {",
        "    /** 获取扫描非活跃 Broker 间隔。 */\n    public long getScanNotActiveBrokerInterval() {",
    ),
    (
        "    public void setScanNotActiveBrokerInterval(long scanNotActiveBrokerInterval) {",
        "    /** 设置扫描非活跃 Broker 间隔。 */\n    public void setScanNotActiveBrokerInterval(long scanNotActiveBrokerInterval) {",
    ),
    (
        "    public int getControllerThreadPoolNums() {",
        "    /** 获取 Controller 线程池大小。 */\n    public int getControllerThreadPoolNums() {",
    ),
    (
        "    public void setControllerThreadPoolNums(int controllerThreadPoolNums) {",
        "    /** 设置 Controller 线程池大小。 */\n    public void setControllerThreadPoolNums(int controllerThreadPoolNums) {",
    ),
    (
        "    public int getControllerRequestThreadPoolQueueCapacity() {",
        "    /** 获取请求队列容量。 */\n    public int getControllerRequestThreadPoolQueueCapacity() {",
    ),
    (
        "    public void setControllerRequestThreadPoolQueueCapacity(int controllerRequestThreadPoolQueueCapacity) {",
        "    /** 设置请求队列容量。 */\n    public void setControllerRequestThreadPoolQueueCapacity(int controllerRequestThreadPoolQueueCapacity) {",
    ),
    (
        "    public String getControllerDLegerGroup() {",
        "    /** 获取 DLedger 组名。 */\n    public String getControllerDLegerGroup() {",
    ),
    (
        "    public void setControllerDLegerGroup(String controllerDLegerGroup) {",
        "    /** 设置 DLedger 组名。 */\n    public void setControllerDLegerGroup(String controllerDLegerGroup) {",
    ),
    (
        "    public String getControllerDLegerPeers() {",
        "    /** 获取 DLedger 对等节点。 */\n    public String getControllerDLegerPeers() {",
    ),
    (
        "    public void setControllerDLegerPeers(String controllerDLegerPeers) {",
        "    /** 设置 DLedger 对等节点。 */\n    public void setControllerDLegerPeers(String controllerDLegerPeers) {",
    ),
    (
        "    public String getControllerDLegerSelfId() {",
        "    /** 获取本节点 DLedger SelfId。 */\n    public String getControllerDLegerSelfId() {",
    ),
    (
        "    public void setControllerDLegerSelfId(String controllerDLegerSelfId) {",
        "    /** 设置本节点 DLedger SelfId。 */\n    public void setControllerDLegerSelfId(String controllerDLegerSelfId) {",
    ),
    (
        "    public int getMappedFileSize() {",
        "    /** 获取映射文件大小。 */\n    public int getMappedFileSize() {",
    ),
    (
        "    public void setMappedFileSize(int mappedFileSize) {",
        "    /** 设置映射文件大小。 */\n    public void setMappedFileSize(int mappedFileSize) {",
    ),
    (
        "    public String getControllerStorePath() {",
        "    /** 获取存储路径，空时按 controllerType 生成默认路径。 */\n    public String getControllerStorePath() {",
    ),
    (
        "    public void setControllerStorePath(String controllerStorePath) {",
        "    /** 设置 Controller 存储路径。 */\n    public void setControllerStorePath(String controllerStorePath) {",
    ),
    (
        "    public boolean isEnableElectUncleanMaster() {",
        "    /** 是否允许非干净 Master 选举。 */\n    public boolean isEnableElectUncleanMaster() {",
    ),
    (
        "    public void setEnableElectUncleanMaster(boolean enableElectUncleanMaster) {",
        "    /** 设置是否允许非干净 Master 选举。 */\n    public void setEnableElectUncleanMaster(boolean enableElectUncleanMaster) {",
    ),
    (
        "    public boolean isProcessReadEvent() {",
        "    /** 是否处理读事件。 */\n    public boolean isProcessReadEvent() {",
    ),
    (
        "    public void setProcessReadEvent(boolean processReadEvent) {",
        "    /** 设置是否处理读事件。 */\n    public void setProcessReadEvent(boolean processReadEvent) {",
    ),
    (
        "    public boolean isNotifyBrokerRoleChanged() {",
        "    /** 角色变更时是否通知 Broker。 */\n    public boolean isNotifyBrokerRoleChanged() {",
    ),
    (
        "    public void setNotifyBrokerRoleChanged(boolean notifyBrokerRoleChanged) {",
        "    /** 设置角色变更通知开关。 */\n    public void setNotifyBrokerRoleChanged(boolean notifyBrokerRoleChanged) {",
    ),
    (
        "    public long getScanInactiveMasterInterval() {",
        "    /** 获取扫描非活跃 Master 间隔。 */\n    public long getScanInactiveMasterInterval() {",
    ),
    (
        "    public void setScanInactiveMasterInterval(long scanInactiveMasterInterval) {",
        "    /** 设置扫描非活跃 Master 间隔。 */\n    public void setScanInactiveMasterInterval(long scanInactiveMasterInterval) {",
    ),
    (
        "    public String getDLedgerAddress() {",
        "    /** 从 peers 解析本节点 DLedger 地址。 */\n    public String getDLedgerAddress() {",
    ),
    (
        "    public MetricsExporterType getMetricsExporterType() {",
        "    /** 获取指标导出器类型。 */\n    public MetricsExporterType getMetricsExporterType() {",
    ),
    (
        "    public void setMetricsExporterType(MetricsExporterType metricsExporterType) {",
        "    /** 设置指标导出器类型。 */\n    public void setMetricsExporterType(MetricsExporterType metricsExporterType) {",
    ),
    (
        "    public void setMetricsExporterType(int metricsExporterType) {",
        "    /** 按整型设置指标导出器类型。 */\n    public void setMetricsExporterType(int metricsExporterType) {",
    ),
    (
        "    public void setMetricsExporterType(String metricsExporterType) {",
        "    /** 按字符串设置指标导出器类型。 */\n    public void setMetricsExporterType(String metricsExporterType) {",
    ),
    (
        "    public String getMetricsGrpcExporterTarget() {",
        "    /** 获取 gRPC 导出目标。 */\n    public String getMetricsGrpcExporterTarget() {",
    ),
    (
        "    public void setMetricsGrpcExporterTarget(String metricsGrpcExporterTarget) {",
        "    /** 设置 gRPC 导出目标。 */\n    public void setMetricsGrpcExporterTarget(String metricsGrpcExporterTarget) {",
    ),
    (
        "    public String getMetricsGrpcExporterHeader() {",
        "    /** 获取 gRPC 导出请求头。 */\n    public String getMetricsGrpcExporterHeader() {",
    ),
    (
        "    public void setMetricsGrpcExporterHeader(String metricsGrpcExporterHeader) {",
        "    /** 设置 gRPC 导出请求头。 */\n    public void setMetricsGrpcExporterHeader(String metricsGrpcExporterHeader) {",
    ),
    (
        "    public long getMetricGrpcExporterTimeOutInMills() {",
        "    /** 获取 gRPC 导出超时。 */\n    public long getMetricGrpcExporterTimeOutInMills() {",
    ),
    (
        "    public void setMetricGrpcExporterTimeOutInMills(long metricGrpcExporterTimeOutInMills) {",
        "    /** 设置 gRPC 导出超时。 */\n    public void setMetricGrpcExporterTimeOutInMills(long metricGrpcExporterTimeOutInMills) {",
    ),
    (
        "    public long getMetricGrpcExporterIntervalInMills() {",
        "    /** 获取 gRPC 导出间隔。 */\n    public long getMetricGrpcExporterIntervalInMills() {",
    ),
    (
        "    public void setMetricGrpcExporterIntervalInMills(long metricGrpcExporterIntervalInMills) {",
        "    /** 设置 gRPC 导出间隔。 */\n    public void setMetricGrpcExporterIntervalInMills(long metricGrpcExporterIntervalInMills) {",
    ),
    (
        "    public long getMetricLoggingExporterIntervalInMills() {",
        "    /** 获取日志导出间隔。 */\n    public long getMetricLoggingExporterIntervalInMills() {",
    ),
    (
        "    public void setMetricLoggingExporterIntervalInMills(long metricLoggingExporterIntervalInMills) {",
        "    /** 设置日志导出间隔。 */\n    public void setMetricLoggingExporterIntervalInMills(long metricLoggingExporterIntervalInMills) {",
    ),
    (
        "    public int getMetricsPromExporterPort() {",
        "    /** 获取 Prometheus 导出端口。 */\n    public int getMetricsPromExporterPort() {",
    ),
    (
        "    public void setMetricsPromExporterPort(int metricsPromExporterPort) {",
        "    /** 设置 Prometheus 导出端口。 */\n    public void setMetricsPromExporterPort(int metricsPromExporterPort) {",
    ),
    (
        "    public String getMetricsPromExporterHost() {",
        "    /** 获取 Prometheus 导出主机。 */\n    public String getMetricsPromExporterHost() {",
    ),
    (
        "    public void setMetricsPromExporterHost(String metricsPromExporterHost) {",
        "    /** 设置 Prometheus 导出主机。 */\n    public void setMetricsPromExporterHost(String metricsPromExporterHost) {",
    ),
    (
        "    public String getMetricsLabel() {",
        "    /** 获取指标标签 CSV。 */\n    public String getMetricsLabel() {",
    ),
    (
        "    public void setMetricsLabel(String metricsLabel) {",
        "    /** 设置指标标签 CSV。 */\n    public void setMetricsLabel(String metricsLabel) {",
    ),
    (
        "    public boolean isMetricsInDelta() {",
        "    /** 是否增量导出指标。 */\n    public boolean isMetricsInDelta() {",
    ),
    (
        "    public void setMetricsInDelta(boolean metricsInDelta) {",
        "    /** 设置是否增量导出指标。 */\n    public void setMetricsInDelta(boolean metricsInDelta) {",
    ),
    (
        "    public String getControllerType() {",
        "    /** 获取 Controller 类型。 */\n    public String getControllerType() {",
    ),
    (
        "    public void setControllerType(String controllerType) {",
        "    /** 设置 Controller 类型。 */\n    public void setControllerType(String controllerType) {",
    ),
    (
        "    public JraftConfig getJraftConfig() {",
        "    /** 获取 jRaft 子配置。 */\n    public JraftConfig getJraftConfig() {",
    ),
    (
        "    public void setJraftConfig(JraftConfig jraftConfig) {",
        "    /** 设置 jRaft 子配置。 */\n    public void setJraftConfig(JraftConfig jraftConfig) {",
    ),
    (
        "    public int getElectMasterMaxRetryCount() {",
        "    /** 获取 Master 选举最大重试次数。 */\n    public int getElectMasterMaxRetryCount() {",
    ),
    (
        "    public void setElectMasterMaxRetryCount(int electMasterMaxRetryCount) {",
        "    /** 设置 Master 选举最大重试次数。 */\n    public void setElectMasterMaxRetryCount(int electMasterMaxRetryCount) {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/CountDownLatch2.java"] = [
    (
        "/**\n * Add reset feature for @see java.util.concurrent.CountDownLatch\n */",
        "/**\n * 在 {@link java.util.concurrent.CountDownLatch} 基础上增加 reset 能力。\n */",
    ),
    (
        "public class CountDownLatch2 {",
        "public class CountDownLatch2 {",
    ),
    (
        "    private final Sync sync;",
        "    /** AQS 同步器。 */\n    private final Sync sync;",
    ),
    (
        "    /**\n     * Constructs a {@code CountDownLatch2} initialized with the given count.\n     *\n     * @param count the number of times {@link #countDown} must be invoked before threads can pass through {@link\n     * #await}\n     * @throws IllegalArgumentException if {@code count} is negative\n     */",
        "    /**\n     * 以给定计数初始化 CountDownLatch2。\n     *\n     * @param count 需调用 {@link #countDown} 的次数，归零后等待线程可通过 {@link #await}\n     * @throws IllegalArgumentException count 为负时\n     */",
    ),
    (
        "    /**\n     * Causes the current thread to wait until the latch has counted down to\n     * zero, unless the thread is {@linkplain Thread#interrupt interrupted}.\n     *\n     * <p>If the current count is zero then this method returns immediately.\n     *\n     * <p>If the current count is greater than zero then the current\n     * thread becomes disabled for thread scheduling purposes and lies\n     * dormant until one of two things happen:\n     * <ul>\n     * <li>The count reaches zero due to invocations of the\n     * {@link #countDown} method; or\n     * <li>Some other thread {@linkplain Thread#interrupt interrupts}\n     * the current thread.\n     * </ul>\n     *\n     * <p>If the current thread:\n     * <ul>\n     * <li>has its interrupted status set on entry to this method; or\n     * <li>is {@linkplain Thread#interrupt interrupted} while waiting,\n     * </ul>\n     * then {@link InterruptedException} is thrown and the current thread's\n     * interrupted status is cleared.\n     *\n     * @throws InterruptedException if the current thread is interrupted while waiting\n     */",
        "    /**\n     * 阻塞直到计数归零，除非当前线程被中断。\n     * 计数已为 0 则立即返回；大于 0 则挂起直至 countDown 归零或被中断。\n     *\n     * @throws InterruptedException 等待过程中被中断\n     */",
    ),
    (
        "    /**\n     * Causes the current thread to wait until the latch has counted down to\n     * zero, unless the thread is {@linkplain Thread#interrupt interrupted},\n     * or the specified waiting time elapses.\n     *\n     * <p>If the current count is zero then this method returns immediately\n     * with the value {@code true}.\n     *\n     * <p>If the current count is greater than zero then the current\n     * thread becomes disabled for thread scheduling purposes and lies\n     * dormant until one of three things happen:\n     * <ul>\n     * <li>The count reaches zero due to invocations of the\n     * {@link #countDown} method; or\n     * <li>Some other thread {@linkplain Thread#interrupt interrupts}\n     * the current thread; or\n     * <li>The specified waiting time elapses.\n     * </ul>\n     *\n     * <p>If the count reaches zero then the method returns with the\n     * value {@code true}.\n     *\n     * <p>If the current thread:\n     * <ul>\n     * <li>has its interrupted status set on entry to this method; or\n     * <li>is {@linkplain Thread#interrupt interrupted} while waiting,\n     * </ul>\n     * then {@link InterruptedException} is thrown and the current thread's\n     * interrupted status is cleared.\n     *\n     * <p>If the specified waiting time elapses then the value {@code false}\n     * is returned.  If the time is less than or equal to zero, the method\n     * will not wait at all.\n     *\n     * @param timeout the maximum time to wait\n     * @param unit the time unit of the {@code timeout} argument\n     * @return {@code true} if the count reached zero and {@code false} if the waiting time elapsed before the count\n     * reached zero\n     * @throws InterruptedException if the current thread is interrupted while waiting\n     */",
        "    /**\n     * 限时等待计数归零。\n     *\n     * @param timeout 最长等待时间\n     * @param unit 时间单位\n     * @return 计数归零返回 true，超时返回 false\n     * @throws InterruptedException 等待过程中被中断\n     */",
    ),
    (
        "    /**\n     * Decrements the count of the latch, releasing all waiting threads if\n     * the count reaches zero.\n     *\n     * <p>If the current count is greater than zero then it is decremented.\n     * If the new count is zero then all waiting threads are re-enabled for\n     * thread scheduling purposes.\n     *\n     * <p>If the current count equals zero then nothing happens.\n     */",
        "    /** 计数减一，归零时唤醒所有等待线程。 */",
    ),
    (
        "    /**\n     * Returns the current count.\n     *\n     * <p>This method is typically used for debugging and testing purposes.\n     *\n     * @return the current count\n     */",
        "    /**\n     * 返回当前计数（常用于调试与测试）。\n     *\n     * @return 当前计数\n     */",
    ),
    (
        "    public void reset() {",
        "    /** 将计数重置为构造时的初始值。 */\n    public void reset() {",
    ),
    (
        "    /**\n     * Returns a string identifying this latch, as well as its state.\n     * The state, in brackets, includes the String {@code \"Count =\"}\n     * followed by the current count.\n     *\n     * @return a string identifying this latch, as well as its state\n     */",
        "    /**\n     * 返回含当前计数的字符串表示。\n     *\n     * @return 标识与状态字符串\n     */",
    ),
    (
        "    /**\n     * Synchronization control For CountDownLatch2.\n     * Uses AQS state to represent count.\n     */",
        "    /** CountDownLatch2 的 AQS 同步实现，用 state 表示计数。 */",
    ),
    (
        "        private final int startCount;",
        "        /** 构造时的初始计数，供 reset 使用。 */\n        private final int startCount;",
    ),
    (
        "        Sync(int count) {",
        "        Sync(int count) {",
    ),
    (
        "        int getCount() {",
        "        /** 读取当前 AQS state 作为计数。 */\n        int getCount() {",
    ),
    (
        "        @Override\n        protected int tryAcquireShared(int acquires) {",
        "        /** 共享获取：state 为 0 时成功。 */\n        @Override\n        protected int tryAcquireShared(int acquires) {",
    ),
    (
        "        @Override\n        protected boolean tryReleaseShared(int releases) {",
        "        /** 共享释放：CAS 递减 state，减至 0 时唤醒等待者。 */\n        @Override\n        protected boolean tryReleaseShared(int releases) {",
    ),
    (
        "            // Decrement count; signal when transition to zero",
        "            // 递减计数，归零时发信号",
    ),
    (
        "        protected void reset() {",
        "        /** 将 state 恢复为 startCount。 */\n        protected void reset() {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/JraftConfig.java"] = [
    (
        "public class JraftConfig {",
        "/**\n * jRaft Controller 的 Raft 相关配置项。\n */\npublic class JraftConfig {",
    ),
    (
        "    private int jRaftElectionTimeoutMs = 1000;",
        "    /** 选举超时（毫秒）。 */\n    private int jRaftElectionTimeoutMs = 1000;",
    ),
    (
        "    private int jRaftScanWaitTimeoutMs = 1000;",
        "    /** 扫描等待超时（毫秒）。 */\n    private int jRaftScanWaitTimeoutMs = 1000;",
    ),
    (
        "    private int jRaftSnapshotIntervalSecs = 3600;",
        "    /** 快照间隔（秒）。 */\n    private int jRaftSnapshotIntervalSecs = 3600;",
    ),
    (
        "    private String jRaftGroupId = \"jRaft-Controller\";",
        "    /** jRaft 组 ID。 */\n    private String jRaftGroupId = \"jRaft-Controller\";",
    ),
    (
        "    private String jRaftServerId = \"localhost:9880\";",
        "    /** 本节点 jRaft Server 地址。 */\n    private String jRaftServerId = \"localhost:9880\";",
    ),
    (
        "    private String jRaftInitConf = \"localhost:9880,localhost:9881,localhost:9882\";",
        "    /** 初始集群成员配置。 */\n    private String jRaftInitConf = \"localhost:9880,localhost:9881,localhost:9882\";",
    ),
    (
        "    private String jRaftControllerRPCAddr = \"localhost:9770,localhost:9771,localhost:9772\";",
        "    /** Controller RPC 地址列表。 */\n    private String jRaftControllerRPCAddr = \"localhost:9770,localhost:9771,localhost:9772\";",
    ),
    (
        "    public int getjRaftElectionTimeoutMs() {",
        "    /** 获取选举超时。 */\n    public int getjRaftElectionTimeoutMs() {",
    ),
    (
        "    public void setjRaftElectionTimeoutMs(int jRaftElectionTimeoutMs) {",
        "    /** 设置选举超时。 */\n    public void setjRaftElectionTimeoutMs(int jRaftElectionTimeoutMs) {",
    ),
    (
        "    public int getjRaftSnapshotIntervalSecs() {",
        "    /** 获取快照间隔。 */\n    public int getjRaftSnapshotIntervalSecs() {",
    ),
    (
        "    public void setjRaftSnapshotIntervalSecs(int jRaftSnapshotIntervalSecs) {",
        "    /** 设置快照间隔。 */\n    public void setjRaftSnapshotIntervalSecs(int jRaftSnapshotIntervalSecs) {",
    ),
    (
        "    public String getjRaftGroupId() {",
        "    /** 获取 jRaft 组 ID。 */\n    public String getjRaftGroupId() {",
    ),
    (
        "    public void setjRaftGroupId(String jRaftGroupId) {",
        "    /** 设置 jRaft 组 ID。 */\n    public void setjRaftGroupId(String jRaftGroupId) {",
    ),
    (
        "    public String getjRaftServerId() {",
        "    /** 获取本节点 Server ID。 */\n    public String getjRaftServerId() {",
    ),
    (
        "    public void setjRaftServerId(String jRaftServerId) {",
        "    /** 设置本节点 Server ID。 */\n    public void setjRaftServerId(String jRaftServerId) {",
    ),
    (
        "    public String getjRaftInitConf() {",
        "    /** 获取初始集群配置。 */\n    public String getjRaftInitConf() {",
    ),
    (
        "    public void setjRaftInitConf(String jRaftInitConf) {",
        "    /** 设置初始集群配置。 */\n    public void setjRaftInitConf(String jRaftInitConf) {",
    ),
    (
        "    public String getjRaftControllerRPCAddr() {",
        "    /** 获取 Controller RPC 地址。 */\n    public String getjRaftControllerRPCAddr() {",
    ),
    (
        "    public void setjRaftControllerRPCAddr(String jRaftControllerRPCAddr) {",
        "    /** 设置 Controller RPC 地址。 */\n    public void setjRaftControllerRPCAddr(String jRaftControllerRPCAddr) {",
    ),
    (
        "    public String getjRaftAddress() {",
        "    /** 返回本节点 jRaft 地址（同 ServerId）。 */\n    public String getjRaftAddress() {",
    ),
    (
        "    public int getjRaftScanWaitTimeoutMs() {",
        "    /** 获取扫描等待超时。 */\n    public int getjRaftScanWaitTimeoutMs() {",
    ),
    (
        "    public void setjRaftScanWaitTimeoutMs(int jRaftScanWaitTimeoutMs) {",
        "    /** 设置扫描等待超时。 */\n    public void setjRaftScanWaitTimeoutMs(int jRaftScanWaitTimeoutMs) {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/KeyBuilder.java"] = [
    (
        "public class KeyBuilder {",
        "/**\n * POP 消费相关 Topic/Key 构建与解析工具。\n */\npublic class KeyBuilder {",
    ),
    (
        "    public static final int POP_ORDER_REVIVE_QUEUE = 999;",
        "    /** POP 顺序消息复活队列 ID。 */\n    public static final int POP_ORDER_REVIVE_QUEUE = 999;",
    ),
    (
        "    private static final char POP_RETRY_SEPARATOR_V1 = '_';",
        "    /** V1 重试 Topic 分隔符。 */\n    private static final char POP_RETRY_SEPARATOR_V1 = '_';",
    ),
    (
        "    private static final char POP_RETRY_SEPARATOR_V2 = '+';",
        "    /** V2 重试 Topic 分隔符。 */\n    private static final char POP_RETRY_SEPARATOR_V2 = '+';",
    ),
    (
        "    private static final String POP_RETRY_REGEX_SEPARATOR_V2 = \"\\\\+\";",
        "    /** V2 分隔符的正则转义形式。 */\n    private static final String POP_RETRY_REGEX_SEPARATOR_V2 = \"\\\\+\";",
    ),
    (
        "    public static String buildPopRetryTopic(String topic, String cid, boolean enableRetryV2) {",
        "    /** 按版本开关构建 POP 重试 Topic。 */\n    public static String buildPopRetryTopic(String topic, String cid, boolean enableRetryV2) {",
    ),
    (
        "    public static String buildPopRetryTopic(String topic, String cid) {",
        "    /** 使用 V1 规则构建 POP 重试 Topic。 */\n    public static String buildPopRetryTopic(String topic, String cid) {",
    ),
    (
        "    public static String buildPopRetryTopicV2(String topic, String cid) {",
        "    /** 使用 + 分隔符构建 V2 重试 Topic。 */\n    public static String buildPopRetryTopicV2(String topic, String cid) {",
    ),
    (
        "    public static String buildPopRetryTopicV1(String topic, String cid) {",
        "    /** 使用 _ 分隔符构建 V1 重试 Topic。 */\n    public static String buildPopRetryTopicV1(String topic, String cid) {",
    ),
    (
        "    public static String parseNormalTopic(String topic, String cid) {",
        "    /** 从重试 Topic 解析原始 Topic（需消费组 cid）。 */\n    public static String parseNormalTopic(String topic, String cid) {",
    ),
    (
        "    public static String parseNormalTopic(String retryTopic) {",
        "    /** 从 V2 重试 Topic 解析原始 Topic。 */\n    public static String parseNormalTopic(String retryTopic) {",
    ),
    (
        "    public static String parseGroup(String retryTopic) {",
        "    /** 从重试 Topic 解析消费组名。 */\n    public static String parseGroup(String retryTopic) {",
    ),
    (
        "    public static String buildPollingKey(String topic, String cid, int queueId) {",
        "    /** 构建 POP 轮询键：topic@cid@queueId。 */\n    public static String buildPollingKey(String topic, String cid, int queueId) {",
    ),
    (
        "    public static boolean isPopRetryTopicV2(String retryTopic) {",
        "    /** 判断是否为 V2 格式 POP 重试 Topic。 */\n    public static boolean isPopRetryTopicV2(String retryTopic) {",
    ),
    (
        "    public static String buildPopLiteLockKey(String group, String lmqName) {",
        "    /** 构建 POP Lite 锁键。 */\n    public static String buildPopLiteLockKey(String group, String lmqName) {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/LifecycleAwareServiceThread.java"] = [
    (
        "public abstract class LifecycleAwareServiceThread extends ServiceThread {",
        "/**\n * 可感知启动完成的服务线程：run 开始时置 started 并通知 awaitStarted 等待者。\n */\npublic abstract class LifecycleAwareServiceThread extends ServiceThread {",
    ),
    (
        "    private final AtomicBoolean started = new AtomicBoolean(false);",
        "    /** 线程是否已进入 run 逻辑。 */\n    private final AtomicBoolean started = new AtomicBoolean(false);",
    ),
    (
        "    @Override\n    public void run() {",
        "    /** 标记已启动并唤醒等待者，再执行 run0。 */\n    @Override\n    public void run() {",
    ),
    (
        "    public abstract void run0();",
        "    /** 子类实现的实际运行逻辑。 */\n    public abstract void run0();",
    ),
    (
        "    /**\n     * Take spurious wakeup into account.\n     *\n     * @param timeout amount of time in milliseconds\n     * @throws InterruptedException if interrupted\n     */",
        "    /**\n     * 等待线程进入 run（考虑虚假唤醒）。\n     *\n     * @param timeout 最长等待毫秒数\n     * @throws InterruptedException 被中断时\n     */",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/LockCallback.java"] = [
    (
        "public interface LockCallback {",
        "/**\n * 消费端队列加锁结果回调。\n */\npublic interface LockCallback {",
    ),
    (
        "    void onSuccess(final Set<MessageQueue> lockOKMQSet);",
        "    /** 加锁成功，返回已锁定队列集合。 */\n    void onSuccess(final Set<MessageQueue> lockOKMQSet);",
    ),
    (
        "    void onException(final Throwable e);",
        "    /** 加锁失败或异常。 */\n    void onException(final Throwable e);",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/ObjectCreator.java"] = [
    (
        "public interface ObjectCreator<T> {",
        "/**\n * 通用对象工厂接口，按可变参数创建实例。\n */\npublic interface ObjectCreator<T> {",
    ),
    (
        "    T create(Object... args);",
        "    /** 根据参数创建 T 类型对象。 */\n    T create(Object... args);",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/OrderedConsumptionLevel.java"] = [
    (
        "public enum OrderedConsumptionLevel {",
        "/**\n * 顺序消费粒度：按队列或按 Sharding Key。\n */\npublic enum OrderedConsumptionLevel {",
    ),
    (
        "    QUEUE(0),",
        "    /** 按消息队列顺序消费。 */\n    QUEUE(0),",
    ),
    (
        "    SHARDING_KEY(1);",
        "    /** 按 Sharding Key 顺序消费。 */\n    SHARDING_KEY(1);",
    ),
    (
        "    private final int value;",
        "    /** 枚举整型值。 */\n    private final int value;",
    ),
    (
        "    OrderedConsumptionLevel(int value) {",
        "    OrderedConsumptionLevel(int value) {",
    ),
    (
        "    public int getValue() {",
        "    /** 返回整型值。 */\n    public int getValue() {",
    ),
    (
        "    public static OrderedConsumptionLevel valueOf(int value) {",
        "    /** 按整型解析，1 为 SHARDING_KEY，否则 QUEUE。 */\n    public static OrderedConsumptionLevel valueOf(int value) {",
    ),
]

R["common/src/main/java/org/apache/rocketmq/common/Pair.java"] = [
    (
        "public class Pair<T1, T2> implements Serializable {",
        "/**\n * 二元组容器，封装两个关联对象。\n */\npublic class Pair<T1, T2> implements Serializable {",
    ),
    (
        "    private T1 object1;",
        "    /** 第一个元素。 */\n    private T1 object1;",
    ),
    (
        "    private T2 object2;",
        "    /** 第二个元素。 */\n    private T2 object2;",
    ),
    (
        "    public Pair(T1 object1, T2 object2) {",
        "    /** 构造二元组。 */\n    public Pair(T1 object1, T2 object2) {",
    ),
    (
        "    public static <T1, T2> Pair<T1, T2> of(T1 object1, T2 object2) {",
        "    /** 静态工厂创建 Pair。 */\n    public static <T1, T2> Pair<T1, T2> of(T1 object1, T2 object2) {",
    ),
    (
        "    public T1 getObject1() {",
        "    /** 获取第一个元素。 */\n    public T1 getObject1() {",
    ),
    (
        "    public void setObject1(T1 object1) {",
        "    /** 设置第一个元素。 */\n    public void setObject1(T1 object1) {",
    ),
    (
        "    public T2 getObject2() {",
        "    /** 获取第二个元素。 */\n    public T2 getObject2() {",
    ),
    (
        "    public void setObject2(T2 object2) {",
        "    /** 设置第二个元素。 */\n    public void setObject2(T2 object2) {",
    ),
]
