"""RocketMQ 5.5.0 wave49a mixed [0:15] Chinese annotation replacements."""

R: dict[str, list[tuple[str, str]]] = {}

R['container/src/main/java/org/apache/rocketmq/container/BrokerContainerStartup.java'] = [
    ('public class BrokerContainerStartup {', '/**\n * Broker 容器启动入口：解析命令行与配置文件，启动 {@link BrokerContainer}，\n * 并按配置路径批量创建、初始化并启动多个 {@link InnerBrokerController}。\n */\npublic class BrokerContainerStartup {'),
    ('    private static final String BROKER_CONTAINER_CONFIG_OPTION = "c";', '    /** 命令行选项：Broker 容器配置文件路径。 */\n    private static final String BROKER_CONTAINER_CONFIG_OPTION = "c";'),
    ('    private static final String BROKER_CONFIG_OPTION = "b";', '    /** 命令行选项：Broker 配置文件路径列表（冒号分隔）。 */\n    private static final String BROKER_CONFIG_OPTION = "b";'),
    ('    public static void main(String[] args) {', '    /** 主入口：解析配置并启动容器及其下属 Broker。 */\n    public static void main(String[] args) {'),
    ('    public static List<BrokerController> createAndStartBrokers(BrokerContainer brokerContainer) {', '    /** 按配置路径在容器内创建并启动全部 Broker。 */\n    public static List<BrokerController> createAndStartBrokers(BrokerContainer brokerContainer) {'),
    ('    public static String[] parseBrokerConfigPath() {', '    /** 解析 Broker 配置文件路径（支持 -b 或容器配置中的路径列表）。 */\n    public static String[] parseBrokerConfigPath() {'),
    ('    public static InnerBrokerController createAndInitializeBroker(BrokerContainer brokerContainer,', '    /** 加载 Broker 配置、校验副本参数并在容器中注册 InnerBrokerController。 */\n    public static InnerBrokerController createAndInitializeBroker(BrokerContainer brokerContainer,'),
    ('    public static BrokerContainer startBrokerContainer(BrokerContainer brokerContainer) {', '    /** 启动 Broker 容器并打印序列化类型与 NameServer 地址。 */\n    public static BrokerContainer startBrokerContainer(BrokerContainer brokerContainer) {'),
    ('    public static void startBrokerController(BrokerContainer brokerContainer,', '    /** 执行启动 Hook 并启动单个 InnerBrokerController。 */\n    public static void startBrokerController(BrokerContainer brokerContainer,'),
    ('    public static void shutdown(final BrokerContainer controller) {', '    /** 关闭 Broker 容器。 */\n    public static void shutdown(final BrokerContainer controller) {'),
    ('            //PackageConflictDetect.detectFastjson();', '            // 可选：检测 Fastjson 包冲突'),
    ('        // remember all configs to prevent discard', '        // 缓存全部配置项，避免被丢弃'),
]

R['controller/src/main/java/org/apache/rocketmq/controller/impl/DLedgerController.java'] = [
    ('/**\n * The implementation of controller, based on DLedger (raft).\n */', '/**\n * 基于 DLedger（Raft）的 Controller 实现：通过共识日志维护副本元数据，\n * 负责 Master 选举、SyncStateSet 变更及 Broker 生命周期管理。\n */'),
    ('    // use for checking whether the broker is alive', '    // 判定 Broker 是否存活的谓词'),
    ('    // use for elect a master', '    // Master 选举策略'),
    ('        // Register statemachine and role handler.', '        // 注册 Raft 状态机与角色变更处理器'),
    ('    /**\n     * Scan all broker-set in statemachine, find that the broker-set which\n     * its master has been timeout but still has at least one broker keep alive with controller,\n     * and we trigger an election to update its state.\n     */', '    /**\n     * 扫描状态机中 Master 已超时但仍有 Broker 存活的 broker-set，\n     * 触发重新选举以更新集群状态。\n     */'),
    ('            // Notify ControllerManager', '            // 通知 ControllerManager 处理 inactive Master'),
    ('    /**\n     * Append the request to DLedger, and wait for DLedger to commit the request.\n     */', '    /**\n     * 将请求追加到 DLedger 日志并等待提交完成。\n     */'),
    ('    // Only for test', '    // 仅供测试使用'),
    ('    /**\n     * Event handler that handle event\n     */', '    /** Controller 事件处理器接口。 */'),
    ('        /**\n         * Run the controller event\n         */', '        /** 执行 Controller 事件逻辑。 */'),
    ('        /**\n         * Return the completableFuture\n         */', '        /** 返回异步 Remoting 响应 Future。 */'),
    ('        /**\n         * Handle Exception.\n         */', '        /** 处理执行过程中的异常。 */'),
    ('    /**\n     * Event scheduler, schedule event handler from event queue\n     */', '    /** 事件调度器：从队列顺序调度 Controller 事件处理器。 */'),
    ('                // read event, or write event with empty events in response which also equals to read event', '                // 读事件，或响应中无事件的写事件（视为读）'),
    ('                    // Now the DLedger don\'t have the function of Read-Index or Lease-Read,', '                    // DLedger 暂不支持 Read-Index/Lease-Read，'),
    ('                    // So we still need to propose an empty request to DLedger.', '                    // 仍需 propose 空请求以同步状态'),
    ('                // write event', '                // 写事件：需追加 DLedger 日志'),
    ('                    // batch append events', '                    // 批量追加事件到 DLedger'),
]

R['controller/src/main/java/org/apache/rocketmq/controller/impl/manager/ReplicasInfoManager.java'] = [
    ('/**\n * The manager that manages the replicas info for all brokers. We can think of this class as the controller\'s memory\n * state machine. If the upper layer want to update the statemachine, it must sequentially call its methods.\n */', '/**\n * 管理全部 Broker 副本信息的内存状态机。\n * 上层更新状态须顺序调用本类方法，保证与 DLedger 日志一致。\n */'),
    ('        // Check whether the oldSyncStateSet is equal with newSyncStateSet', '        // 校验新 SyncStateSet 是否与旧集合相同'),
    ('        // Check master', '        // 校验请求中的 Master brokerId 是否与当前一致'),
    ('        // Check master epoch', '        // 校验 Master epoch 是否匹配（防脑裂）'),
    ('        // Check syncStateSet epoch', '        // 校验 SyncStateSet epoch'),
    ('        // Check newSyncStateSet correctness', '        // 校验新 SyncStateSet 成员合法性'),
    ('        // Generate event', '        // 生成 AlterSyncStateSet 事件'),
    ('            // this broker set hasn\'t been registered', '            // 该 broker-set 尚未注册'),
    ('            // If never have a master in this broker set, in other words, it is the first time to elect a master', '            // 首次选举：该 broker-set 尚无 Master'),
    ('            // elect it as the first master', '            // 将指定 Broker 选为首个 Master'),
    ('        // elect by policy', '        // 按选举策略选主'),
    ('            // old master still valid, change nothing', '            // 旧 Master 仍有效，无需变更'),
    ('        // a new master is elected', '        // 已选举出新 Master'),
    ('    /**\n     * Apply events to memory statemachine.\n     *\n     * @param event event message\n     */', '    /**\n     * 将已提交事件应用到内存状态机。\n     *\n     * @param event 事件消息\n     */'),
    ('            // First time to register in this broker set', '            // 首次在该 broker-set 注册'),
    ('            // Initialize the replicaInfo about this broker set', '            // 初始化该 broker-set 的副本元数据'),
    ('            // Initialize an empty syncStateInfo for this broker set', '            // 初始化空的 SyncStateSet 信息'),
    ('                // Record new master', '                // 记录新 Master'),
    ('    /**\n     * Is the broker existed in the memory metadata\n     *\n     * @return true if both existed in replicaInfoTable and inSyncReplicasInfoTable\n     */', '    /**\n     * 判断 brokerName 是否已存在于内存元数据。\n     *\n     * @return replicaInfoTable 与 syncStateSetInfoTable 均存在时返回 true\n     */'),
]

R['dev/merge_rocketmq_pr.py'] = [
    ('# This script is a modified version of the one created by the RocketMQ\n# project (https://github.com/apache/rocketmq/blob/master/dev/merge_rocketmq_pr.py).\n\n# Utility for creating well-formed pull request merges and pushing them to Apache.\n#   usage: ./merge_rocketmq_pr.py    (see config env vars below)\n#\n# This utility assumes you already have local a RocketMQ git folder and that you\n# have added remotes corresponding to both (i) the github apache RocketMQ\n# mirror and (ii) the apache git repo.',
     '# 本脚本改编自 RocketMQ 官方 dev/merge_rocketmq_pr.py。\n\n# 用于将 GitHub Pull Request 规范合并并推送到 Apache 官方仓库。\n#   用法: ./merge_rocketmq_pr.py（环境变量见下方配置）\n#\n# 前提：本地已有 RocketMQ git 仓库，并配置 (i) GitHub apache 镜像\n# 与 (ii) Apache git 远程。'),
    ('# Location of your RocketMQ git development area\nROCKETMQ_HOME = os.environ.get("ROCKETMQ_HOME", os.getcwd())\n# Remote name which points to the Gihub site\nPR_REMOTE_NAME = os.environ.get("PR_REMOTE_NAME", "apache-github")\n# Remote name which points to Apache git\nPUSH_REMOTE_NAME = os.environ.get("PUSH_REMOTE_NAME", "origin")\n# ASF JIRA username\nJIRA_USERNAME = os.environ.get("JIRA_USERNAME", "")\n# ASF JIRA password\nJIRA_PASSWORD = os.environ.get("JIRA_PASSWORD", "")',
     '# RocketMQ 本地开发目录\nROCKETMQ_HOME = os.environ.get("ROCKETMQ_HOME", os.getcwd())\n# 指向 GitHub 镜像的 remote 名称\nPR_REMOTE_NAME = os.environ.get("PR_REMOTE_NAME", "apache-github")\n# 指向 Apache 官方 git 的 remote 名称\nPUSH_REMOTE_NAME = os.environ.get("PUSH_REMOTE_NAME", "origin")\n# ASF JIRA 用户名\nJIRA_USERNAME = os.environ.get("JIRA_USERNAME", "")\n# ASF JIRA 密码\nJIRA_PASSWORD = os.environ.get("JIRA_PASSWORD", "")'),
    ('# OAuth key used for issuing requests against the GitHub API. If this is not defined, then requests\n# will be unauthenticated. You should only need to configure this if you find yourself regularly\n# exceeding your IP\'s unauthenticated request rate limit. You can create an OAuth key at\n# https://github.com/settings/tokens. This script only requires the "public_repo" scope.\nGITHUB_OAUTH_KEY = os.environ.get("GITHUB_OAUTH_KEY")',
     '# GitHub API OAuth Token；未配置则匿名请求（易触发限流）。\n# 在 https://github.com/settings/tokens 创建，仅需 public_repo 权限。\nGITHUB_OAUTH_KEY = os.environ.get("GITHUB_OAUTH_KEY")'),
    ('# Prefix added to temporary branches\nBRANCH_PREFIX = "PR_TOOL"\nDEVELOP_BRANCH = "develop"',
     '# 临时分支前缀\nBRANCH_PREFIX = "PR_TOOL"\nDEVELOP_BRANCH = "develop"'),
    ('def fail(msg):\n    print(msg)\n    clean_up()\n    sys.exit(-1)',
     'def fail(msg):\n    """打印错误、清理临时分支并退出。"""\n    print(msg)\n    clean_up()\n    sys.exit(-1)'),
    ('def run_cmd(cmd):\n    print(cmd)', 'def run_cmd(cmd):\n    """执行 shell/git 命令并返回输出。"""\n    print(cmd)'),
    ('def clean_up():\n    print("Restoring head pointer to %s" % original_head)', 'def clean_up():\n    """恢复 HEAD 并删除 PR_TOOL 临时分支。"""\n    print("Restoring head pointer to %s" % original_head)'),
    ('# merge the requested PR and return the merge hash\ndef merge_pr(pr_num, target_ref, title, body, pr_repo_desc):',
     '# 合并指定 PR 并返回 merge commit 哈希\ndef merge_pr(pr_num, target_ref, title, body, pr_repo_desc):'),
    ('    # The string "Closes #%s" string is required for GitHub to correctly close the PR',
     '    # GitHub 需 "Closes #%s" 才能自动关闭 PR'),
]

R['filter/src/main/java/org/apache/rocketmq/filter/expression/ComparisonExpression.java'] = [
    ('/**\n * A filter performing a comparison of two objects\n * <p>\n * This class was taken from ActiveMQ org.apache.activemq.filter.ComparisonExpression,\n * but:\n * 1. Remove LIKE expression, and related methods;\n * 2. Extract a new method __compare which has int return value;\n * 3. When create between expression, check whether left value is less or equal than right value;\n * 4. For string type value(can not convert to number), only equal or unequal comparison are supported.\n * </p>\n */',
     '/**\n * 二元比较表达式基类，用于 SQL92 风格消息过滤。\n * <p>\n * 源自 ActiveMQ ComparisonExpression，RocketMQ 改动：\n * 1. 移除 LIKE 及相关方法；\n * 2. 抽取 {@link #__compare} 返回 int 比较结果；\n * 3. BETWEEN 创建时校验左界 ≤ 右界；\n * 4. 不可转数字的字符串仅支持等于/不等于。\n * </p>\n */'),
    ('    /**\n     * @param left\n     * @param right\n     */', '    /**\n     * @param left 左操作数表达式\n     * @param right 右操作数表达式\n     */'),
    ('        // check', '        // 常量边界时校验 BETWEEN 左界 ≤ 右界'),
    ('                // If one of the values is null', '                // 任一操作数为 null 时比较结果为 false'),
    ('    /**\n     * Only Numeric expressions can be used in >, >=, < or <= expressions.s\n     */', '    /**\n     * 校验 >、>=、<、<= 的操作数必须为数值类型。\n     */'),
    ('            // Else it\'s boolean or a String..', '            // 布尔或字符串不可用于大小比较'),
    ('    /**\n     * Validates that the expression can be used in == or <> expression. Cannot\n     * not be NULL TRUE or FALSE litterals.\n     */', '    /**\n     * 校验 ==、!= 操作数合法性（禁止 null/true/false 字面量参与比较）。\n     */'),
    ('    /**\n     * @param left\n     * @param right\n     */', '    /**\n     * 校验等值比较左右操作数类型兼容。\n     *\n     * @param left 左表达式\n     * @param right 右表达式\n     */'),
    ('                // Compare String is illegal', '                // 字符串不可做大小比较'),
    ('                // first try to convert to double', '                // 先尝试转为 double 再比较'),
    ('        // If the the objects are not of the same type,', '        // 类型不一致时'),
    ('        // try to convert up to allow the comparison.', '        // 尝试向上转型以完成比较'),
]

R['filter/src/main/java/org/apache/rocketmq/filter/parser/SelectorParser.java'] = [
    ('/**\n * JMS Selector Parser generated by JavaCC\n * <p/>\n * Do not edit this .java file directly - it is autogenerated from SelectorParser.jj\n */', '/**\n * JMS 消息选择器解析器（JavaCC 生成）。\n * <p/>\n * 请勿直接编辑本 .java 文件，修改请编辑 SelectorParser.jj 后重新生成。\n */'),
    ('    // ----------------------------------------------------------------------------\n    // Grammar\n    // ----------------------------------------------------------------------------', '    // ----------------------------------------------------------------------------\n    // 语法规则（JMS Selector 表达式）\n    // ----------------------------------------------------------------------------'),
    ('        // Decode the sting value.', '        // 解码字符串字面量'),
    ('    /**\n     * Generated Token Manager.\n     */', '    /** JavaCC 生成的词法分析器。 */'),
    ('    /**\n     * Current token.\n     */', '    /** 当前 token。 */'),
    ('    /**\n     * Next token.\n     */', '    /** 下一个待读 token。 */'),
    ('    /**\n     * Constructor with InputStream.\n     */', '    /** 以 InputStream 构造解析器。 */'),
    ('    /**\n     * Constructor with InputStream and supplied encoding\n     */', '    /** 以 InputStream 与指定编码构造解析器。 */'),
    ('    /**\n     * Reinitialise.\n     */', '    /** 重新初始化输入流。 */'),
    ('    public static BooleanExpression parse(String sql) throws MQFilterException {', '    /** 解析 SQL92 风格过滤表达式，带 LRU 缓存。 */\n    public static BooleanExpression parse(String sql) throws MQFilterException {'),
    ('    public static void clearCache() {', '    /** 清空表达式解析缓存。 */\n    public static void clearCache() {'),
]

R['filter/src/main/java/org/apache/rocketmq/filter/parser/SelectorParserTokenManager.java'] = [
    ('/**\n * Token Manager.\n */', '/**\n * 消息选择器词法分析 Token 管理器（JavaCC 生成）。\n */'),
    ('    /**\n     * Debug output.\n     */', '    /** 调试输出流。 */'),
    ('    /**\n     * Set debug output.\n     */', '    /** 设置词法分析调试输出流。 */'),
    ('public class SelectorParserTokenManager implements SelectorParserConstants {', '/**\n * 将输入字符流切分为 Selector 语法 token（关键字、运算符、字面量等）。\n */\npublic class SelectorParserTokenManager implements SelectorParserConstants {'),
]

R['filter/src/main/java/org/apache/rocketmq/filter/parser/SimpleCharStream.java'] = [
    ('/**\n * An implementation of interface CharStream, where the stream is assumed to\n * contain only ASCII characters (without unicode processing).\n */', '/**\n * {@link CharStream} 实现：假定输入仅含 ASCII 字符（不做 Unicode 处理）。\n */'),
    ('    /**\n     * Whether parser is static.\n     */', '    /** 解析器是否为 static 模式。 */'),
    ('    /**\n     * Position in buffer.\n     */', '    /** 缓冲区当前读取位置。 */'),
    ('public class SimpleCharStream {', '/**\n * JavaCC 字符流缓冲：维护行号、列号，供 TokenManager 逐字符扫描。\n */\npublic class SimpleCharStream {'),
]

R['namesrv/src/main/java/org/apache/rocketmq/namesrv/processor/DefaultRequestProcessor.java'] = [
    ('public class DefaultRequestProcessor implements NettyRequestProcessor {', '/**\n * NameServer 默认请求处理器：路由 KV 配置、Broker 注册/心跳、\n * Topic 管理及 NameServer 配置查询等 Remoting 请求。\n */\npublic class DefaultRequestProcessor implements NettyRequestProcessor {'),
    ('    protected Set<String> configBlackList = new HashSet<>();', '    /** 禁止通过 API 修改的配置项黑名单。 */\n    protected Set<String> configBlackList = new HashSet<>();'),
    ('    private void initConfigBlackList() {', '    /** 初始化配置修改黑名单。 */\n    private void initConfigBlackList() {'),
    ('    public RemotingCommand putKVConfig(ChannelHandlerContext ctx,', '    /** 写入 KV 命名空间配置。 */\n    public RemotingCommand putKVConfig(ChannelHandlerContext ctx,'),
    ('    public RemotingCommand getKVConfig(ChannelHandlerContext ctx,', '    /** 读取 KV 命名空间配置。 */\n    public RemotingCommand getKVConfig(ChannelHandlerContext ctx,'),
    ('            // RegisterBrokerBody of old version only contains TopicConfig.', '            // 旧版 RegisterBrokerBody 仅含 TopicConfig'),
    ('            // Register single topic route info should be after the broker completes the first registration.', '            // 单 Topic 路由注册须在 Broker 首次注册完成后'),
    ('    @Override\n    public boolean rejectRequest() {', '    /** NameServer 不拒绝请求（流控由上层处理）。 */\n    @Override\n    public boolean rejectRequest() {'),
]

R['namesrv/src/main/java/org/apache/rocketmq/namesrv/routeinfo/RouteInfoManager.java'] = [
    ('public class RouteInfoManager {', '/**\n * NameServer 路由信息管理器：维护 Topic 队列、Broker 地址、\n * 集群映射、Broker 存活状态及 Filter Server 等路由元数据。\n */\npublic class RouteInfoManager {'),
    ('    private final Map<String/* topic */, Map<String, QueueData>> topicQueueTable;', '    /** topic → (brokerName → QueueData) 路由表。 */\n    private final Map<String/* topic */, Map<String, QueueData>> topicQueueTable;'),
    ('    private final Map<String/* brokerName */, BrokerData> brokerAddrTable;', '    /** brokerName → Broker 地址与角色信息。 */\n    private final Map<String/* brokerName */, BrokerData> brokerAddrTable;'),
    ('    private final Map<String/* clusterName */, Set<String/* brokerName */>> clusterAddrTable;', '    /** clusterName → brokerName 集合。 */\n    private final Map<String/* clusterName */, Set<String/* brokerName */>> clusterAddrTable;'),
    ('    private final Map<BrokerAddrInfo/* brokerAddr */, BrokerLiveInfo> brokerLiveTable;', '    /** Broker 地址 → 存活信息与 Channel。 */\n    private final Map<BrokerAddrInfo/* brokerAddr */, BrokerLiveInfo> brokerLiveTable;'),
    ('    // For test only\n    int blockedUnRegisterRequests() {', '    // 仅供测试：阻塞中的注销请求数\n    int blockedUnRegisterRequests() {'),
    ('                // check and construct queue data map', '                // 校验 Broker 合法性并构建队列映射'),
    ('            //get all the brokerNames fot the specified cluster', '            // 获取指定集群下全部 brokerName'),
    ('            //Switch slave to master: first remove <1, IP:PORT> in namesrv, then add <0, IP:PORT>', '            // Slave 升 Master：先删 slave 地址再注册 master 地址'),
    ('/**\n * broker address information\n */', '/**\n * Broker 地址标识（clusterName + brokerAddr），用作 brokerLiveTable 键。\n */'),
    ('                        // Master has been unregistered, wipe the write perm', '                        // Master 已注销，清除写权限'),
]

R['proxy/src/main/java/org/apache/rocketmq/proxy/config/ProxyConfig.java'] = [
    ('public class ProxyConfig implements ConfigFile {', '/**\n * RocketMQ Proxy 运行时配置：gRPC/Remoting 端口、线程池、\n * TLS、消息大小限制、路由缓存及事务心跳等参数。\n */\npublic class ProxyConfig implements ConfigFile {'),
    ('    /**\n     * configuration for ThreadPoolMonitor\n     */', '    /** 线程池监控：是否打印 JStack 及状态间隔。 */'),
    ('    /**\n     * TLS\n     */', '    /** TLS 证书与密钥路径配置。 */'),
    ('    /**\n     * gRPC\n     */', '    /** gRPC 服务端与客户端相关配置。 */'),
    ('    /**\n     * Maximum number of concurrent gRPC calls allowed per client connection.\n     * <p>\n     * A single client issuing excessively high concurrent requests may skew the validation load balancing\n     * and overload a single proxy instance (hotspot), potentially bringing it down. Limiting\n     * {@code grpcMaxConcurrentCallsPerConnection} helps mitigate this per-connection hotspot risk.\n     * <p>\n     * Note: Setting this limit too low may cause send/consume failures (e.g., backpressure or rejected calls).\n     */',
     '    /**\n     * 单客户端连接允许的最大并发 gRPC 调用数。\n     * <p>\n     * 过高并发可能导致单 Proxy 热点过载；限制 {@code grpcMaxConcurrentCallsPerConnection} 可缓解。\n     * <p>\n     * 注意：设置过低可能导致发送/消费失败（背压或拒绝）。\n     */'),
    ('    /**\n     * gRPC max message size\n     * 130M = 4M * 32 messages + 2M attributes\n     */', '    /**\n     * gRPC 入站消息最大尺寸（130M = 32×4M 消息体 + 2M 属性）。\n     */'),
    ('    /**\n     * max message body size, 0 or negative number means no limit for proxy\n     */', '    /** 消息体最大尺寸；0 或负数表示 Proxy 不限制。 */'),
    ('    /**\n     * if true, proxy will check message body size and reject msg if it\'s body is empty\n     */', '    /** 为 true 时校验消息体非空，否则拒绝。 */'),
    ('    /**\n     * max user property size, 0 or negative number means no limit for proxy\n     */', '    /** 用户属性总大小上限；0 或负数表示不限制。 */'),
    ('    /**\n     * max message group size, 0 or negative number means no limit for proxy\n     */', '    /** 消息组（顺序消息）最大尺寸；0 或负数表示不限制。 */'),
    ('    /**\n     * max lite topic size\n     */', '    /** Lite Topic 名称最大长度。 */'),
    ('    // syncLiteSubscription request rate limit per proxy', '    // 每个 Proxy 的 syncLiteSubscription 请求速率上限'),
    ('    /**\n     * When a message pops, the message is invisible by default\n     */', '    /** POP 消息默认不可见时长（毫秒）。 */'),
    ('    // Example address: 127.0.0.1:1234', '    // 示例地址：127.0.0.1:1234'),
    ('    // remoting', '    // Remoting 协议接入配置'),
    ('    // related to proxy\'s send strategy in cluster mode.', '    // 集群模式下 Proxy 发送策略相关配置'),
]

R['proxy/src/main/java/org/apache/rocketmq/proxy/grpc/v2/GrpcMessagingApplication.java'] = [
    ('public class GrpcMessagingApplication extends MessagingServiceGrpc.MessagingServiceImplBase implements StartAndShutdown {', '/**\n * gRPC v2 消息服务入口：实现 {@link MessagingServiceGrpc}，\n * 将 RPC 分发到路由/生产/消费/事务等线程池并串联鉴权 Pipeline。\n */\npublic class GrpcMessagingApplication extends MessagingServiceGrpc.MessagingServiceImplBase implements StartAndShutdown {'),
    ('        // add pipeline\n        // the last pipe add will execute at the first', '        // 组装 Pipeline；后添加的 Pipe 最先执行'),
    ('    protected Status flowLimitStatus() {', '    /** 构造流控拒绝时的 gRPC Status。 */\n    protected Status flowLimitStatus() {'),
    ('    protected Status convertExceptionToStatus(Throwable t) {', '    /** 将异常转换为 gRPC Status。 */\n    protected Status convertExceptionToStatus(Throwable t) {'),
    ('    protected ProxyContext createContext() {', '    /** 创建 Proxy 请求上下文。 */\n    protected ProxyContext createContext() {'),
    ('    protected void validateContext(ProxyContext context) {', '    /** 校验 clientId 等非空字段。 */\n    protected void validateContext(ProxyContext context) {'),
    ('    public void queryRoute(QueryRouteRequest request, StreamObserver<QueryRouteResponse> responseObserver) {', '    /** 查询 Topic 路由（gRPC QueryRoute）。 */\n    public void queryRoute(QueryRouteRequest request, StreamObserver<QueryRouteResponse> responseObserver) {'),
    ('    public void sendMessage(SendMessageRequest request, StreamObserver<SendMessageResponse> responseObserver) {', '    /** 发送消息（gRPC SendMessage）。 */\n    public void sendMessage(SendMessageRequest request, StreamObserver<SendMessageResponse> responseObserver) {'),
    ('    public void receiveMessage(ReceiveMessageRequest request, StreamObserver<ReceiveMessageResponse> responseObserver) {', '    /** 长轮询接收消息（gRPC ReceiveMessage）。 */\n    public void receiveMessage(ReceiveMessageRequest request, StreamObserver<ReceiveMessageResponse> responseObserver) {'),
    ('    public void ackMessage(AckMessageRequest request, StreamObserver<AckMessageResponse> responseObserver) {', '    /** 确认 POP 消息（gRPC AckMessage）。 */\n    public void ackMessage(AckMessageRequest request, StreamObserver<AckMessageResponse> responseObserver) {'),
    ('    public void endTransaction(EndTransactionRequest request, StreamObserver<EndTransactionResponse> responseObserver) {', '    /** 提交/回滚事务消息（gRPC EndTransaction）。 */\n    public void endTransaction(EndTransactionRequest request, StreamObserver<EndTransactionResponse> responseObserver) {'),
    ('    public StreamObserver<TelemetryCommand> telemetry(StreamObserver<TelemetryCommand> responseObserver) {', '    /** 双向 Telemetry 流：客户端设置与诊断信息。 */\n    public StreamObserver<TelemetryCommand> telemetry(StreamObserver<TelemetryCommand> responseObserver) {'),
]

R['proxy/src/main/java/org/apache/rocketmq/proxy/grpc/v2/client/ClientActivity.java'] = [
    ('public class ClientActivity extends AbstractMessagingActivity {', '/**\n * gRPC 客户端生命周期 Activity：处理心跳、终止通知、\n * Lite 订阅同步及 Producer/Consumer 注册。\n */\npublic class ClientActivity extends AbstractMessagingActivity {'),
    ('    protected void init() {', '    /** 注册 Producer/Consumer 变更监听器。 */\n    protected void init() {'),
    ('    public CompletableFuture<HeartbeatResponse> heartbeat(ProxyContext ctx, HeartbeatRequest request) {', '    /** 处理客户端心跳并注册 Producer/Consumer。 */\n    public CompletableFuture<HeartbeatResponse> heartbeat(ProxyContext ctx, HeartbeatRequest request) {'),
    ('    public CompletableFuture<NotifyClientTerminationResponse> notifyClientTermination(ProxyContext ctx,', '    /** 客户端断开时注销 Producer/Consumer 并清理 Channel。 */\n    public CompletableFuture<NotifyClientTerminationResponse> notifyClientTermination(ProxyContext ctx,'),
    ('    public CompletableFuture<SyncLiteSubscriptionResponse> syncLiteSubscription(ProxyContext ctx,', '    /** 同步 Lite Push 订阅关系。 */\n    public CompletableFuture<SyncLiteSubscriptionResponse> syncLiteSubscription(ProxyContext ctx,'),
    ('        // use topic name as producer group', '        // Lite 场景以 topic 名作为 producer group'),
    ('                    // save settings from channel sync from other proxy', '                    // 保存从其他 Proxy 同步的客户端 Settings'),
]

R['proxy/src/main/java/org/apache/rocketmq/proxy/grpc/v2/producer/SendMessageActivity.java'] = [
    ('public class SendMessageActivity extends AbstractMessagingActivity {', '/**\n * gRPC 发送消息 Activity：校验 Topic/消息体，转换 protobuf 消息\n * 并委托 {@link MessagingProcessor#sendMessage} 路由到 Broker。\n */\npublic class SendMessageActivity extends AbstractMessagingActivity {'),
    ('    public CompletableFuture<SendMessageResponse> sendMessage(ProxyContext ctx, SendMessageRequest request) {', '    /** 处理单条或批量 SendMessage 请求。 */\n    public CompletableFuture<SendMessageResponse> sendMessage(ProxyContext ctx, SendMessageRequest request) {'),
]

R['proxy/src/main/java/org/apache/rocketmq/proxy/processor/ConsumerProcessor.java'] = [
    ('public class ConsumerProcessor extends AbstractProcessor {', '/**\n * Proxy 消费侧处理器：封装 POP/Pull、ACK、位点管理、\n * 队列锁及不可见时间变更等 Broker RPC。\n */\npublic class ConsumerProcessor extends AbstractProcessor {'),
    ('    public CompletableFuture<PopResult> popMessage(\n        ProxyContext ctx,\n        QueueSelector queueSelector,', '    /** 按队列选择器 POP 消息（自动选队列）。 */\n    public CompletableFuture<PopResult> popMessage(\n        ProxyContext ctx,\n        QueueSelector queueSelector,'),
    ('    public CompletableFuture<PopResult> popLiteMessage(', '    /** POP Lite 消息（轻量消费模式）。 */\n    public CompletableFuture<PopResult> popLiteMessage('),
    ('    public CompletableFuture<AckResult> ackMessage(', '    /** 确认 POP 消息消费完成。 */\n    public CompletableFuture<AckResult> ackMessage('),
    ('    public CompletableFuture<List<BatchAckResult>> batchAckMessage(', '    /** 批量 ACK POP 消息。 */\n    public CompletableFuture<List<BatchAckResult>> batchAckMessage('),
    ('    public CompletableFuture<AckResult> changeInvisibleTime(ProxyContext ctx, ReceiptHandle handle, String messageId,', '    /** 修改 POP 消息不可见时间。 */\n    public CompletableFuture<AckResult> changeInvisibleTime(ProxyContext ctx, ReceiptHandle handle, String messageId,'),
    ('    public CompletableFuture<PullResult> pullMessage(ProxyContext ctx, MessageQueue messageQueue, String consumerGroup,', '    /** Pull 模式拉取消息。 */\n    public CompletableFuture<PullResult> pullMessage(ProxyContext ctx, MessageQueue messageQueue, String consumerGroup,'),
    ('    public CompletableFuture<Void> updateConsumerOffset(ProxyContext ctx, MessageQueue messageQueue,', '    /** 更新消费位点。 */\n    public CompletableFuture<Void> updateConsumerOffset(ProxyContext ctx, MessageQueue messageQueue,'),
    ('    public CompletableFuture<Long> queryConsumerOffset(ProxyContext ctx, MessageQueue messageQueue,', '    /** 查询消费位点。 */\n    public CompletableFuture<Long> queryConsumerOffset(ProxyContext ctx, MessageQueue messageQueue,'),
    ('    public CompletableFuture<Set<MessageQueue>> lockBatchMQ(ProxyContext ctx, Set<MessageQueue> mqSet,', '    /** 顺序消费：批量锁定 MessageQueue。 */\n    public CompletableFuture<Set<MessageQueue>> lockBatchMQ(ProxyContext ctx, Set<MessageQueue> mqSet,'),
    ('    public CompletableFuture<Void> unlockBatchMQ(ProxyContext ctx, Set<MessageQueue> mqSet,', '    /** 顺序消费：批量解锁 MessageQueue。 */\n    public CompletableFuture<Void> unlockBatchMQ(ProxyContext ctx, Set<MessageQueue> mqSet,'),
]
