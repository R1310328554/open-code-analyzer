"""Chinese annotation replacements for Nacos 3.2.3 wave42b [15:30] config sql/utils."""

R: dict[str, list[tuple[str, str]]] = {
    "config/src/main/java/com/alibaba/nacos/config/server/service/repository/extrnal/ExternalHistoryConfigInfoPersistServiceImpl.java": [
        (
            "/**\n * ExternalHistoryConfigInfoPersistServiceImpl.\n *\n * @author lixiaoshuang\n */",
            "/**\n * 外部存储模式下配置历史记录的持久化实现：基于 JDBC 与插件化 Mapper 完成\n"
            " * {@link com.alibaba.nacos.config.server.service.repository.HistoryConfigInfoPersistService} 定义的增删查操作。\n"
            " * ExternalHistoryConfigInfoPersistServiceImpl.\n *\n * @author lixiaoshuang\n */",
        ),
        (
            "    private DataSourceService dataSourceService;",
            "    /** 动态数据源服务，提供 JDBC 与事务模板 */\n    private DataSourceService dataSourceService;",
        ),
        (
            "    protected JdbcTemplate jt;",
            "    /** Spring JDBC 模板，执行 SQL */\n    protected JdbcTemplate jt;",
        ),
        (
            "    protected TransactionTemplate tjt;",
            "    /** 事务模板，用于需要原子性的写操作 */\n    protected TransactionTemplate tjt;",
        ),
        (
            "    private MapperManager mapperManager;",
            "    /** 多数据库方言 Mapper 管理器 */\n    private MapperManager mapperManager;",
        ),
        (
            "    @Override\n    public <E> PaginationHelper<E> createPaginationHelper() {",
            "    /** 创建外存分页助手，供历史列表查询使用 */\n    @Override\n    public <E> PaginationHelper<E> createPaginationHelper() {",
        ),
        (
            "    @Override\n    public void insertConfigHistoryAtomic(long id, ConfigInfo configInfo, String srcIp,",
            "    /** 原子写入一条配置变更历史（含 MD5、发布类型、灰度名与扩展信息） */\n"
            "    @Override\n    public void insertConfigHistoryAtomic(long id, ConfigInfo configInfo, String srcIp,",
        ),
        (
            "        } catch (DataAccessException e) {\n            LogUtil.FATAL_LOG.error(\"[db-error] \" + e, e);\n            throw e;\n        }\n    }\n    \n    @Override\n    public void removeConfigHistory",
            "        } catch (DataAccessException e) {\n            // 数据库异常记录致命日志并向上抛出\n            LogUtil.FATAL_LOG.error(\"[db-error] \" + e, e);\n            throw e;\n        }\n    }\n    \n    /** 按起始时间与条数上限批量清理过期历史记录 */\n    @Override\n    public void removeConfigHistory",
        ),
        (
            "    @Override\n    public List<ConfigInfoStateWrapper> findDeletedConfig(final Timestamp startTime, long startId,",
            "    /** 分页查询指定时间之后被删除的配置快照，用于增量同步 */\n"
            "    @Override\n    public List<ConfigInfoStateWrapper> findDeletedConfig(final Timestamp startTime, long startId,",
        ),
        (
            "    @Override\n    public Page<ConfigHistoryInfo> findConfigHistory(String dataId, String group, String tenant,",
            "    /** 按 dataId/group/tenant 分页检索配置变更历史列表 */\n"
            "    @Override\n    public Page<ConfigHistoryInfo> findConfigHistory(String dataId, String group, String tenant,",
        ),
        (
            "    @Override\n    public ConfigHistoryInfo detailConfigHistory(Long nid) {",
            "    /** 按历史主键 nid 查询单条历史详情，不存在时返回 null */\n    @Override\n    public ConfigHistoryInfo detailConfigHistory(Long nid) {",
        ),
        (
            "    @Override\n    public ConfigHistoryInfo detailPreviousConfigHistory(Long id) {",
            "    /** 查询指定配置 id 的上一条历史记录，用于版本对比 */\n    @Override\n    public ConfigHistoryInfo detailPreviousConfigHistory(Long id) {",
        ),
        (
            "    @Override\n    public int findConfigHistoryCountByTime(final Timestamp startTime) {",
            "    /** 统计指定时间之后的历史记录总数，供清理任务估算进度 */\n    @Override\n    public int findConfigHistoryCountByTime(final Timestamp startTime) {",
        ),
        (
            "    @Override\n    public ConfigHistoryInfo getNextHistoryInfo(String dataId, String group, String tenant,",
            "    /** 按 nid 游标获取下一条匹配的历史记录，支持灰度与发布类型过滤 */\n"
            "    @Override\n    public ConfigHistoryInfo getNextHistoryInfo(String dataId, String group, String tenant,",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/service/sql/EmbeddedStorageContextUtils.java": [
        (
            "/**\n * Temporarily saves all insert, update, and delete statements under a transaction in the order in which they occur.\n *\n * @author <a href=\"mailto:liaochuntao@live.com\">liaochuntao</a>\n */",
            "/**\n * 内嵌存储（Raft）事务上下文工具：在写库前将 {@link com.alibaba.nacos.config.server.model.event.ConfigDumpEvent}\n"
            " * 序列化写入 {@link com.alibaba.nacos.persistence.repository.embedded.EmbeddedStorageContextHolder} 扩展信息，"
            "供状态机异步横向通知。\n"
            " * Temporarily saves all insert, update, and delete statements under a transaction in the order in which they occur.\n *\n * @author <a href=\"mailto:liaochuntao@live.com\">liaochuntao</a>\n */",
        ),
        (
            "    public static void onModifyConfigInfo(ConfigInfo configInfo, String srcIp, Timestamp time) {",
            "    /** 正式配置变更：非单机模式下附加 ConfigDumpEvent 到 Raft 扩展上下文 */\n"
            "    public static void onModifyConfigInfo(ConfigInfo configInfo, String srcIp, Timestamp time) {",
        ),
        (
            "    public static void onModifyConfigBetaInfo(ConfigInfo configInfo, String betaIps, String srcIp,",
            "    /** Beta 灰度配置变更：携带 betaIps 写入 Dump 事件 */\n"
            "    public static void onModifyConfigBetaInfo(ConfigInfo configInfo, String betaIps, String srcIp,",
        ),
        (
            "    public static void onModifyConfigTagInfo(ConfigInfo configInfo, String tag, String srcIp,",
            "    /** Tag 维度配置变更：携带 tag 标识写入 Dump 事件 */\n"
            "    public static void onModifyConfigTagInfo(ConfigInfo configInfo, String tag, String srcIp,",
        ),
        (
            "    public static void onModifyConfigGrayInfo(ConfigInfo configInfo, String grayName,",
            "    /** 灰度规则配置变更：携带 grayName 与 grayRule 写入 Dump 事件 */\n"
            "    public static void onModifyConfigGrayInfo(ConfigInfo configInfo, String grayName,",
        ),
        (
            "    public static void onDeleteConfigInfo(String namespaceId, String group, String dataId,",
            "    /** 删除正式配置：构造 remove=true 的 Dump 事件 */\n"
            "    public static void onDeleteConfigInfo(String namespaceId, String group, String dataId,",
        ),
        (
            "    public static void onBatchDeleteConfigInfo(List<ConfigAllInfo> configInfos) {",
            "    /** 批量删除配置：聚合多条 remove 事件后一次性写入扩展上下文 */\n"
            "    public static void onBatchDeleteConfigInfo(List<ConfigAllInfo> configInfos) {",
        ),
        (
            "    public static void onDeleteConfigBetaInfo(String namespaceId, String group, String dataId,",
            "    /** 删除 Beta 配置：标记 isBeta=true 的 remove 事件 */\n"
            "    public static void onDeleteConfigBetaInfo(String namespaceId, String group, String dataId,",
        ),
        (
            "    public static void onDeleteConfigTagInfo(String namespaceId, String group, String dataId,",
            "    /** 删除 Tag 配置：携带 tag 与操作者 IP 的 remove 事件 */\n"
            "    public static void onDeleteConfigTagInfo(String namespaceId, String group, String dataId,",
        ),
        (
            "    public static void onDeleteConfigGrayInfo(String namespaceId, String group, String dataId,",
            "    /** 删除灰度配置：携带 grayName 的 remove 事件 */\n"
            "    public static void onDeleteConfigGrayInfo(String namespaceId, String group, String dataId,",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/service/sql/ExternalStorageUtils.java": [
        (
            "/**\n * external storage utils.\n * @author shiyiyue\n */",
            "/**\n * 外部存储（MySQL 等）辅助工具：封装 Spring JDBC 主键回填所需的 {@link org.springframework.jdbc.support.KeyHolder}。\n"
            " * external storage utils.\n * @author shiyiyue\n */",
        ),
        (
            "    public static KeyHolder createKeyHolder() {",
            "    /** 创建 {@link org.springframework.jdbc.support.GeneratedKeyHolder}，供 insert 后读取自增主键 */\n"
            "    public static KeyHolder createKeyHolder() {",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/service/trace/ConfigTraceService.java": [
        (
            "/**\n * Config trace.\n *\n * @author Nacos\n */",
            "/**\n * 配置全链路追踪服务：将持久化、通知、Dump、Pull 等阶段事件以管道分隔格式写入 trace 日志，"
            "并驱动 {@link com.alibaba.nacos.config.server.monitor.MetricsMonitor} 计时指标。\n"
            " * Config trace.\n *\n * @author Nacos\n */",
        ),
        (
            "    /**\n     * persist event.\n     */",
            "    /**\n     * 持久化阶段事件标识。\n     * persist event.\n     */",
        ),
        (
            "    public static final String PERSISTENCE_EVENT_BETA = \"persist-beta\";",
            "    /** Beta 配置持久化事件 */\n    public static final String PERSISTENCE_EVENT_BETA = \"persist-beta\";",
        ),
        (
            "    public static final String PERSISTENCE_EVENT_TAG = \"persist-tag\";",
            "    /** Tag 配置持久化事件 */\n    public static final String PERSISTENCE_EVENT_TAG = \"persist-tag\";",
        ),
        (
            "    public static final String PERSISTENCE_EVENT_METADATA = \"persist-metadata\";",
            "    /** 元数据持久化事件 */\n    public static final String PERSISTENCE_EVENT_METADATA = \"persist-metadata\";",
        ),
        (
            "    /**\n     * persist type.\n     */",
            "    /**\n     * 持久化操作类型常量。\n     * persist type.\n     */",
        ),
        (
            "    public static final String PERSISTENCE_TYPE_PUB = \"pub\";",
            "    /** 发布/写入 */\n    public static final String PERSISTENCE_TYPE_PUB = \"pub\";",
        ),
        (
            "    public static final String PERSISTENCE_TYPE_REMOVE = \"remove\";",
            "    /** 删除 */\n    public static final String PERSISTENCE_TYPE_REMOVE = \"remove\";",
        ),
        (
            "    public static final String PERSISTENCE_TYPE_MERGE = \"merge\";",
            "    /** 合并 */\n    public static final String PERSISTENCE_TYPE_MERGE = \"merge\";",
        ),
        (
            "    /**\n     * notify event.\n     */",
            "    /**\n     * 长轮询/推送通知阶段事件标识。\n     * notify event.\n     */",
        ),
        (
            "    /**\n     * notify type.\n     */",
            "    /**\n     * 通知结果类型常量。\n     * notify type.\n     */",
        ),
        (
            "    public static final String NOTIFY_TYPE_OK = \"ok\";",
            "    /** 通知成功 */\n    public static final String NOTIFY_TYPE_OK = \"ok\";",
        ),
        (
            "    public static final String NOTIFY_TYPE_ERROR = \"error\";",
            "    /** 通知失败 */\n    public static final String NOTIFY_TYPE_ERROR = \"error\";",
        ),
        (
            "    public static final String NOTIFY_TYPE_UNHEALTH = \"unhealth\";",
            "    /** 客户端不健康 */\n    public static final String NOTIFY_TYPE_UNHEALTH = \"unhealth\";",
        ),
        (
            "    public static final String NOTIFY_TYPE_EXCEPTION = \"exception\";",
            "    /** 通知过程异常 */\n    public static final String NOTIFY_TYPE_EXCEPTION = \"exception\";",
        ),
        (
            "    /**\n     * dump event.\n     */",
            "    /**\n     * 本地缓存 Dump 阶段事件标识。\n     * dump event.\n     */",
        ),
        (
            "    /**\n     * dump type.\n     */",
            "    /**\n     * Dump 结果类型常量。\n     * dump type.\n     */",
        ),
        (
            "    public static final String DUMP_TYPE_OK = \"ok\";",
            "    /** Dump 成功 */\n    public static final String DUMP_TYPE_OK = \"ok\";",
        ),
        (
            "    public static final String DUMP_TYPE_REMOVE_OK = \"remove-ok\";",
            "    /** 删除 Dump 成功 */\n    public static final String DUMP_TYPE_REMOVE_OK = \"remove-ok\";",
        ),
        (
            "    public static final String DUMP_TYPE_ERROR = \"error\";",
            "    /** Dump 失败 */\n    public static final String DUMP_TYPE_ERROR = \"error\";",
        ),
        (
            "    /**\n     * pull event.\n     */",
            "    /**\n     * 客户端拉取配置阶段事件标识。\n     * pull event.\n     */",
        ),
        (
            "    /**\n     * pull type.\n     */",
            "    /**\n     * 拉取结果类型常量。\n     * pull type.\n     */",
        ),
        (
            "    public static void logPersistenceEvent(String dataId, String group, String tenant,",
            "    /** 记录持久化 trace：末尾 ext 字段为内容 MD5 */\n"
            "    public static void logPersistenceEvent(String dataId, String group, String tenant,",
        ),
        (
            "        // Convenient tlog segmentation.\n        if (StringUtils.isBlank(tenant)) {",
            "        // 空 tenant 写 null 便于 tlog 分段解析\n        if (StringUtils.isBlank(tenant)) {",
        ),
        (
            "    public static void logNotifyEvent(String dataId, String group, String tenant,",
            "    /** 记录通知 trace 并上报 notify 耗时到 MetricsMonitor */\n"
            "    public static void logNotifyEvent(String dataId, String group, String tenant,",
        ),
        (
            "    public static void logDumpEvent(String dataId, String group, String tenant,",
            "    /** 记录标准 Dump trace，event 固定为 {@link #DUMP_EVENT} */\n"
            "    public static void logDumpEvent(String dataId, String group, String tenant,",
        ),
        (
            "    public static void logDumpGrayNameEvent(String dataId, String group, String tenant,",
            "    /** 记录带灰度名的 Dump trace，event 为 dump-{grayName} */\n"
            "    public static void logDumpGrayNameEvent(String dataId, String group, String tenant,",
        ),
        (
            "    public static void logDumpAllEvent(String dataId, String group, String tenant,",
            "    /** 记录全量 Dump trace，event 固定为 dump-all */\n"
            "    public static void logDumpAllEvent(String dataId, String group, String tenant,",
        ),
        (
            "    public static void logPullEvent(String dataId, String group, String tenant,",
            "    /** 记录客户端拉取 trace，含 delayed、clientIp、isNotify 与协议 model */\n"
            "    public static void logPullEvent(String dataId, String group, String tenant,",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/utils/AccumulateStatCount.java": [
        (
            "/**\n * Accumulate Stat Count.\n *\n * @author Nacos\n */",
            "/**\n * 累积计数统计器：基于 {@link java.util.concurrent.atomic.AtomicLong} 累加总量，"
            " {@link #stat()} 返回自上次统计以来的增量。\n"
            " * Accumulate Stat Count.\n *\n * @author Nacos\n */",
        ),
        (
            "    final AtomicLong total = new AtomicLong(0);",
            "    /** 全局累计计数 */\n    final AtomicLong total = new AtomicLong(0);",
        ),
        (
            "    long lastStatValue = 0;",
            "    /** 上次 stat() 时的累计快照，用于计算增量 */\n    long lastStatValue = 0;",
        ),
        (
            "    public long increase() {",
            "    /** 原子递增并返回新值 */\n    public long increase() {",
        ),
        (
            "    /**\n     * accumulate stat.\n     *\n     * @return stat.\n     */",
            "    /**\n     * 返回自上次调用以来的增量并更新快照。\n     * accumulate stat.\n     *\n     * @return stat.\n     */",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/utils/AppNameUtils.java": [
        (
            "/**\n * appName util.\n *\n * @author Nacos\n */",
            "/**\n * 应用名推断工具：依次尝试 {@code project.name} 系统属性与常见应用服务器 home 路径，"
            "解析出 Nacos 客户端侧上报的应用标识。\n"
            " * appName util.\n *\n * @author Nacos\n */",
        ),
        (
            "    private static final String PARAM_MARKING_PROJECT = \"project.name\";",
            "    /** JVM 启动参数 project.name */\n    private static final String PARAM_MARKING_PROJECT = \"project.name\";",
        ),
        (
            "    private static final String LINUX_ADMIN_HOME = \"/home/admin/\";",
            "    /** 阿里云标准部署根路径前缀 */\n    private static final String LINUX_ADMIN_HOME = \"/home/admin/\";",
        ),
        (
            "    private static final String DEFAULT_APP_NAME = \"unknown\";",
            "    /** 无法识别时的默认应用名 */\n    private static final String DEFAULT_APP_NAME = \"unknown\";",
        ),
        (
            "    public static String getAppName() {",
            "    /** 获取当前进程应用名：优先 project.name，其次从 server home 路径解析 */\n    public static String getAppName() {",
        ),
        (
            "    private static String getAppNameByProjectName() {",
            "    /** 从 project.name 系统属性读取应用名 */\n    private static String getAppNameByProjectName() {",
        ),
        (
            "    private static String getAppNameByServerHome() {",
            "    /** 从 JBoss/Jetty/Tomcat home 路径 /home/admin/{app}/ 段解析应用名 */\n    private static String getAppNameByServerHome() {",
        ),
        (
            "    private static String getServerType() {",
            "    /** 根据 JVM 属性判断当前运行的应用服务器类型 */\n    private static String getServerType() {",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/utils/ConfigExecutor.java": [
        (
            "/**\n * Config executor.\n *\n * @author <a href=\"mailto:liaochuntao@live.com\">liaochuntao</a>\n */",
            "/**\n * 配置模块统一线程池门面：集中管理定时任务、异步通知、长轮询、容量校正与插件回调等"
            " {@link java.util.concurrent.ScheduledExecutorService} 实例。\n"
            " * Config executor.\n *\n * @author <a href=\"mailto:liaochuntao@live.com\">liaochuntao</a>\n */",
        ),
        (
            "    private static final ScheduledExecutorService TIMER_EXECUTOR =",
            "    /** 通用配置定时任务线程池（8 线程） */\n    private static final ScheduledExecutorService TIMER_EXECUTOR =",
        ),
        (
            "    private static final ScheduledExecutorService CAPACITY_MANAGEMENT_EXECUTOR =",
            "    /** 配置容量校正单线程池 */\n    private static final ScheduledExecutorService CAPACITY_MANAGEMENT_EXECUTOR =",
        ),
        (
            "    private static final ScheduledExecutorService ASYNC_NOTIFY_EXECUTOR =",
            "    /** 异步长轮询/推送通知线程池（100 线程） */\n    private static final ScheduledExecutorService ASYNC_NOTIFY_EXECUTOR =",
        ),
        (
            "    private static final ScheduledExecutorService ASYNC_CONFIG_CHANGE_PLUGIN_EXECUTOR =",
            "    /** 配置变更插件异步执行线程池 */\n    private static final ScheduledExecutorService ASYNC_CONFIG_CHANGE_PLUGIN_EXECUTOR =",
        ),
        (
            "    private static final ScheduledExecutorService CONFIG_SUB_SERVICE_EXECUTOR =",
            "    /** 配置订阅服务线程池 */\n    private static final ScheduledExecutorService CONFIG_SUB_SERVICE_EXECUTOR =",
        ),
        (
            "    private static final ScheduledExecutorService LONG_POLLING_EXECUTOR =",
            "    /** 长轮询调度单线程池 */\n    private static final ScheduledExecutorService LONG_POLLING_EXECUTOR =",
        ),
        (
            "    private static final ScheduledExecutorService ASYNC_CONFIG_CHANGE_NOTIFY_EXECUTOR =",
            "    /** 远程配置变更通知线程池（gRPC 等） */\n    private static final ScheduledExecutorService ASYNC_CONFIG_CHANGE_NOTIFY_EXECUTOR =",
        ),
        (
            "    public static void scheduleConfigTask(Runnable command, long initialDelay, long delay,",
            "    /** 以固定延迟周期调度通用配置定时任务 */\n    public static void scheduleConfigTask(Runnable command, long initialDelay, long delay,",
        ),
        (
            "    public static void scheduleConfigChangeTask(Runnable command, long delay, TimeUnit unit) {",
            "    /** 一次性延迟执行配置变更相关任务 */\n    public static void scheduleConfigChangeTask(Runnable command, long delay, TimeUnit unit) {",
        ),
        (
            "    public static void scheduleCorrectUsageTask(Runnable runnable, long initialDelay, long delay,",
            "    /** 周期性调度容量使用量校正任务 */\n    public static void scheduleCorrectUsageTask(Runnable runnable, long initialDelay, long delay,",
        ),
        (
            "    public static void executeAsyncNotify(Runnable runnable) {",
            "    /** 立即提交异步通知任务 */\n    public static void executeAsyncNotify(Runnable runnable) {",
        ),
        (
            "    public static void scheduleAsyncNotify(Runnable command, long delay, TimeUnit unit) {",
            "    /** 延迟调度异步通知任务 */\n    public static void scheduleAsyncNotify(Runnable command, long delay, TimeUnit unit) {",
        ),
        (
            "    public static void executeAsyncConfigChangePluginTask(Runnable runnable) {",
            "    /** 提交配置变更插件异步任务 */\n    public static void executeAsyncConfigChangePluginTask(Runnable runnable) {",
        ),
        (
            "    public static int asyncNotifyQueueSize() {",
            "    /** 返回异步通知线程池当前排队任务数 */\n    public static int asyncNotifyQueueSize() {",
        ),
        (
            "    public static int asyncConfigChangeClientNotifyQueueSize() {",
            "    /** 返回远程配置变更通知线程池排队任务数 */\n    public static int asyncConfigChangeClientNotifyQueueSize() {",
        ),
        (
            "    public static ScheduledExecutorService getConfigSubServiceExecutor() {",
            "    /** 获取配置订阅服务线程池 */\n    public static ScheduledExecutorService getConfigSubServiceExecutor() {",
        ),
        (
            "    public static ScheduledExecutorService getClientConfigNotifierServiceExecutor() {",
            "    /** 获取客户端配置变更通知线程池 */\n    public static ScheduledExecutorService getClientConfigNotifierServiceExecutor() {",
        ),
        (
            "    public static ScheduledFuture<?> scheduleClientConfigNotifier(Runnable runnable, long delay,",
            "    /** 延迟调度客户端配置变更通知并返回 ScheduledFuture */\n    public static ScheduledFuture<?> scheduleClientConfigNotifier(Runnable runnable, long delay,",
        ),
        (
            "    public static void scheduleLongPolling(Runnable runnable, long initialDelay, long delay,",
            "    /** 以固定延迟周期调度长轮询任务 */\n    public static void scheduleLongPolling(Runnable runnable, long initialDelay, long delay,",
        ),
        (
            "    public static ScheduledFuture<?> scheduleLongPolling(Runnable runnable, long delay,",
            "    /** 一次性延迟调度长轮询任务 */\n    public static ScheduledFuture<?> scheduleLongPolling(Runnable runnable, long delay,",
        ),
        (
            "    public static void executeLongPolling(Runnable runnable) {",
            "    /** 立即提交长轮询执行任务 */\n    public static void executeLongPolling(Runnable runnable) {",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/utils/ConfigExtInfoUtil.java": [
        (
            "/**\n * Extra info util.\n *\n * @author Nacos\n */",
            "/**\n * 配置扩展信息（ext_info）JSON 构建工具：将高级发布参数、全量配置元数据或灰度规则"
            "序列化为持久化层 ext_info 字段。\n"
            " * Extra info util.\n *\n * @author Nacos\n */",
        ),
        (
            "    private static final Map<String, String> EXTRA_INFO_KEYS_MAPPING = new HashMap<>();",
            "    /** 高级发布参数字段到 ext_info JSON 键的映射表 */\n    private static final Map<String, String> EXTRA_INFO_KEYS_MAPPING = new HashMap<>();",
        ),
        (
            "    /**\n     * Extract the extInfo from advance config info.\n     */",
            "    /**\n     * 从高级发布参数 Map 提取 ext_info JSON 字符串。\n     * Extract the extInfo from advance config info.\n     */",
        ),
        (
            "    /**\n     * Extract the extInfo from all config info.\n     */",
            "    /**\n     * 从 {@link com.alibaba.nacos.config.server.model.ConfigAllInfo} 组装 ext_info JSON。\n     * Extract the extInfo from all config info.\n     */",
        ),
        (
            "    /**\n     * Extract the extInfo from gray config info.\n     */",
            "    /**\n     * 从灰度名与灰度规则 JSON 构建 ext_info（含 gray_name、gray_rule 等）。\n     * Extract the extInfo from gray config info.\n     */",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/utils/ConfigPersistContext.java": [
        (
            "/**\n * Config persistence context for current thread.\n *\n * <p>Used to control some persistence behaviors (e.g. whether to write history records)\n * for internal batch operations such as data migration or skill upload.</p>\n */",
            "/**\n * 当前线程配置持久化上下文：通过 ThreadLocal 控制是否跳过历史记录写入，"
            "适用于数据迁移、批量导入等内部场景。\n"
            " * Config persistence context for current thread.\n *\n * <p>Used to control some persistence behaviors (e.g. whether to write history records)\n * for internal batch operations such as data migration or skill upload.</p>\n */",
        ),
        (
            "    private static final ThreadLocal<Boolean> SKIP_HISTORY =",
            "    /** 线程级“跳过历史写入”标志，默认 false */\n    private static final ThreadLocal<Boolean> SKIP_HISTORY =",
        ),
        (
            "    /**\n     * Whether current thread should skip writing config history.\n     */",
            "    /**\n     * 当前线程是否应跳过配置历史记录写入。\n     * Whether current thread should skip writing config history.\n     */",
        ),
        (
            "    /**\n     * Set whether to skip history for current thread.\n     *\n     * <p>Callers should use {@link #withSkipHistory()} whenever possible to ensure cleanup.</p>\n     */",
            "    /**\n     * 设置当前线程是否跳过历史写入；false 时清除上下文。\n     * Set whether to skip history for current thread.\n     *\n     * <p>Callers should use {@link #withSkipHistory()} whenever possible to ensure cleanup.</p>\n     */",
        ),
        (
            "    /**\n     * Clear thread local context.\n     */",
            "    /**\n     * 清除当前线程的持久化上下文。\n     * Clear thread local context.\n     */",
        ),
        (
            "    /**\n     * Enable skip-history in try-with-resources style.\n     */",
            "    /**\n     * 以 try-with-resources 方式启用跳过历史写入，close 时自动恢复。\n     * Enable skip-history in try-with-resources style.\n     */",
        ),
        (
            "    /**\n     * A guard which restores previous value when closed.\n     */",
            "    /**\n     * 自动关闭守卫：close 时恢复进入前的 skipHistory 状态。\n     * A guard which restores previous value when closed.\n     */",
        ),
        (
            "        private final Boolean previous;",
            "        /** 进入 Guard 前线程原有的 skipHistory 值 */\n        private final Boolean previous;",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/utils/ConfigTagUtil.java": [
        (
            "/**\n * Config Tag util.\n *\n * @author PoisonGravity\n */",
            "/**\n * 配置 Tag 工具：识别 Istio 相关标签（virtual-service、destination-rule），"
            "供配置发布与路由策略分支判断。\n"
            " * Config Tag util.\n *\n * @author PoisonGravity\n */",
        ),
        (
            "    public static final String VIRTUAL_SERVICE = \"virtual-service\";",
            "    /** Istio VirtualService 配置标签 */\n    public static final String VIRTUAL_SERVICE = \"virtual-service\";",
        ),
        (
            "    public static final String DESTINATION_RULE = \"destination-rule\";",
            "    /** Istio DestinationRule 配置标签 */\n    public static final String DESTINATION_RULE = \"destination-rule\";",
        ),
        (
            "    private static final String TAGS_DELIMITER = \",\";",
            "    /** 多 Tag 逗号分隔符 */\n    private static final String TAGS_DELIMITER = \",\";",
        ),
        (
            "    /**\n     * <p>Checks if config tags contains \"virtual-service\" or \"destination-rule\".</p>\n     * @param configTags the tags to check\n     * @return {@code true} if the config tags contains \"virtual-service\" or \"destination-rule\".\n     */",
            "    /**\n     * 判断 configTags 是否包含 Istio VirtualService 或 DestinationRule 标签（忽略连字符与大小写）。\n"
            "     * <p>Checks if config tags contains \"virtual-service\" or \"destination-rule\".</p>\n     * @param configTags the tags to check\n     * @return {@code true} if the config tags contains \"virtual-service\" or \"destination-rule\".\n     */",
        ),
        (
            "    /**\n     * <p>Gets the type of Istio from the config tags.</p>\n     * @param configTags the tags to check\n     * @return the type of Istio if it is found, {@code null} otherwise.\n     * @throws IllegalArgumentException if configTags is null.\n     */",
            "    /**\n     * 从 configTags 中提取首个匹配的 Istio 类型标签，未匹配时返回 null。\n"
            "     * <p>Gets the type of Istio from the config tags.</p>\n     * @param configTags the tags to check\n     * @return the type of Istio if it is found, {@code null} otherwise.\n     * @throws IllegalArgumentException if configTags is null.\n     */",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/utils/ContentUtils.java": [
        (
            "/**\n * Content utils.\n *\n * @author Nacos\n */",
            "/**\n * 配置内容工具：校验增量发布内容格式、解析 dataId 与正文分隔符、截断过长内容用于日志展示。\n"
            " * Content utils.\n *\n * @author Nacos\n */",
        ),
        (
            "    /**\n     * verify the pub config content.\n     *\n     * @param content content\n     */",
            "    /**\n     * 校验增量发布/删除内容：禁止空串、换行及 WORD_SEPARATOR 控制字符。\n     * verify the pub config content.\n     *\n     * @param content content\n     */",
        ),
        (
            "    public static String getContentIdentity(String content) {",
            "    /** 从 WORD_SEPARATOR 分隔的内容中提取 dataId（分隔符前段） */\n    public static String getContentIdentity(String content) {",
        ),
        (
            "    public static String getContent(String content) {",
            "    /** 从 WORD_SEPARATOR 分隔的内容中提取正文（分隔符后段） */\n    public static String getContent(String content) {",
        ),
        (
            "    /**\n     * Truncate the content.\n     *\n     * @param content content\n     * @return content after truncate.\n     */",
            "    /**\n     * 截断过长内容至 100 字符并追加省略号，供日志安全输出。\n     * Truncate the content.\n     *\n     * @param content content\n     * @return content after truncate.\n     */",
        ),
        (
            "    private static final int LIMIT_CONTENT_SIZE = 100;",
            "    /** 内容截断最大长度 */\n    private static final int LIMIT_CONTENT_SIZE = 100;",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/utils/GroupKey.java": [
        (
            "/**\n * Synthesize dataId+groupId form. Escape reserved characters in dataId and groupId.\n *\n * @author jiuRen\n */",
            "/**\n * 配置分组键（GroupKey）编解码：将 dataId、group、tenant 用 {@code +} 连接，"
            "并对 {@code +}/{@code %} 做 URL 风格转义，供缓存索引与长轮询协议使用。\n"
            " * Synthesize dataId+groupId form. Escape reserved characters in dataId and groupId.\n *\n * @author jiuRen\n */",
        ),
        (
            "    public static String getKey(String dataId, String group) {",
            "    /** 生成 dataId+group 二元组键 */\n    public static String getKey(String dataId, String group) {",
        ),
        (
            "    public static String getKey(String dataId, String group, String datumStr) {",
            "    /** 生成 dataId+group+datum 三元组键 */\n    public static String getKey(String dataId, String group, String datumStr) {",
        ),
        (
            "    public static String getKeyTenant(String dataId, String group, String tenant) {",
            "    /** 生成含 tenant 命名空间的三元组键 */\n    public static String getKeyTenant(String dataId, String group, String tenant) {",
        ),
        (
            "    /**\n     * Parse the group key.\n     */",
            "    /**\n     * 解析 GroupKey 字符串为 [dataId, group, tenant] 数组。\n     * Parse the group key.\n     */",
        ),
        (
            "    /**\n     * + -> %2B % -> %25.\n     */",
            "    /**\n     * URL 风格转义：{@code +} → {@code %2B}，{@code %} → {@code %25}。\n     * + -> %2B % -> %25.\n     */",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/utils/GroupKey2.java": [
        (
            "/**\n * Group key util.\n *\n * @author Nacos\n */",
            "/**\n * GroupKey 编解码第二版：与 {@link GroupKey} 类似，tenant 为空时不追加第三段，"
            "为长轮询与 MD5 比对协议提供键格式。\n"
            " * Group key util.\n *\n * @author Nacos\n */",
        ),
        (
            "    public static String getKey(String dataId, String group) {",
            "    /** 生成 dataId+group 二元组键 */\n    public static String getKey(String dataId, String group) {",
        ),
        (
            "    public static String getKey(String dataId, String group, String tenant) {",
            "    /** 生成 dataId+group+tenant 键，tenant 非空时才追加第三段 */\n    public static String getKey(String dataId, String group, String tenant) {",
        ),
        (
            "    /**\n     * Parse the group key.\n     */",
            "    /**\n     * 解析 GroupKey 为 [dataId, group, tenant]，非法格式抛 IllegalArgumentException。\n     * Parse the group key.\n     */",
        ),
        (
            "    /**\n     * + -> %2B % -> %25.\n     */",
            "    /**\n     * URL 风格转义：{@code +} → {@code %2B}，{@code %} → {@code %25}。\n     * + -> %2B % -> %25.\n     */",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/utils/LogUtil.java": [
        (
            "/**\n * Log util.\n *\n * @author Nacos\n */",
            "/**\n * 配置模块日志门面：集中暴露启动、致命、拉取、Dump、追踪、通知等专用 Logger，"
            "并提供运行时动态调整 Logback 级别的方法。\n"
            " * Log util.\n *\n * @author Nacos\n */",
        ),
        (
            "    /**\n     * Default log.\n     */",
            "    /**\n     * 配置服务启动与常规日志。\n     * Default log.\n     */",
        ),
        (
            "    /**\n     * Fatal error log, require alarm.\n     */",
            "    /**\n     * 致命错误日志，需触发告警。\n     * Fatal error log, require alarm.\n     */",
        ),
        (
            "    /**\n     * Http client log.\n     */",
            "    /**\n     * 客户端 HTTP 拉取日志。\n     * Http client log.\n     */",
        ),
        (
            "    public static final Logger PULL_CHECK_LOG =",
            "    /** 拉取校验专用日志 */\n    public static final Logger PULL_CHECK_LOG =",
        ),
        (
            "    /**\n     * Dump log.\n     */",
            "    /**\n     * 本地缓存 Dump 日志。\n     * Dump log.\n     */",
        ),
        (
            "    public static final Logger MEMORY_LOG =",
            "    /** 内存与监控指标日志 */\n    public static final Logger MEMORY_LOG =",
        ),
        (
            "    public static final Logger CLIENT_LOG =",
            "    /** 客户端请求日志 */\n    public static final Logger CLIENT_LOG =",
        ),
        (
            "    public static final Logger TRACE_LOG =",
            "    /** 全链路 trace 日志，供 {@link com.alibaba.nacos.config.server.service.trace.ConfigTraceService} 写入 */\n    public static final Logger TRACE_LOG =",
        ),
        (
            "    public static final Logger NOTIFY_LOG =",
            "    /** 长轮询/推送通知日志 */\n    public static final Logger NOTIFY_LOG =",
        ),
        (
            "    public static void setLogLevel(String logName, String level) {",
            "    /** 按 logName 别名动态设置对应 Logger 的 Logback 级别 */\n    public static void setLogLevel(String logName, String level) {",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/utils/MD5Util.java": [
        (
            "/**\n * MD5 util.\n *\n * @author Nacos\n */",
            "/**\n * 长轮询 MD5 比对与监听协议工具：解析客户端上报的 configKeys、比较 MD5 变更、"
            "编码变更列表供 HTTP 响应返回。\n"
            " * MD5 util.\n *\n * @author Nacos\n */",
        ),
        (
            "    /**\n     * Compare Md5.\n     */",
            "    /**\n     * 委托 {@link com.alibaba.nacos.config.server.utils.Md5ComparatorDelegate} 执行 MD5 比对。\n     * Compare Md5.\n     */",
        ),
        (
            "    /**\n     * Compare old Md5.\n     */",
            "    /**\n     * 将变更 GroupKey 编码为旧版 {@code dataId:group;} 分号分隔格式。\n     * Compare old Md5.\n     */",
        ),
        (
            "    /**\n     * Join and encode changedGroupKeys string.\n     */",
            "    /**\n     * 将变更键列表用 WORD/LINE 分隔符拼接后 URL 编码，供新版长轮询响应。\n     * Join and encode changedGroupKeys string.\n     */",
        ),
        (
            "    /**\n     * Parse the transport protocol, which has two formats (W for field delimiter, L for each data delimiter) old: D w G\n     * w MD5 l new: D w G w MD5 w T l.\n     *\n     * @param configKeysString protocol\n     * @return protocol message\n     */",
            "    /**\n     * 解析客户端监听协议字符串为 groupKey→{@link com.alibaba.nacos.config.server.model.ConfigListenState} 映射；"
            "兼容旧版（D+G+MD5）与新版（D+G+MD5+T）格式。\n"
            "     * Parse the transport protocol, which has two formats (W for field delimiter, L for each data delimiter) old: D w G\n     * w MD5 l new: D w G w MD5 w T l.\n     *\n     * @param configKeysString protocol\n     * @return protocol message\n     */",
        ),
        (
            "    public static String toString(InputStream input, String encoding) throws IOException {",
            "    /** 按指定编码将 InputStream 读为字符串，encoding 为 null 时使用 Constants.ENCODE */\n    public static String toString(InputStream input, String encoding) throws IOException {",
        ),
        (
            "    /**\n     * Reader to String.\n     */",
            "    /**\n     * 将 Reader 内容读入字符串。\n     * Reader to String.\n     */",
        ),
        (
            "    /**\n     * Copy data to buffer.\n     */",
            "    /**\n     * 从 Reader 复制字符到 Writer，返回复制的字符总数。\n     * Copy data to buffer.\n     */",
        ),
        (
            "    static final char WORD_SEPARATOR_CHAR = (char) 2;",
            "    /** 协议字段分隔符 ASCII 2 */\n    static final char WORD_SEPARATOR_CHAR = (char) 2;",
        ),
        (
            "    static final char LINE_SEPARATOR_CHAR = (char) 1;",
            "    /** 协议条目分隔符 ASCII 1 */\n    static final char LINE_SEPARATOR_CHAR = (char) 1;",
        ),
    ],
}
