"""Chinese annotation replacements for Nacos 3.2.3 wave71b [15:30] persistence datasource/events/repo."""

R: dict[str, list[tuple[str, str]]] = {}

# --- ConditionStandaloneEmbedStorage ---

R["persistence/src/main/java/com/alibaba/nacos/persistence/configuration/condition/ConditionStandaloneEmbedStorage.java"] = [
    (
        "/**\n * Judge whether to user StandaloneEmbedStorage by condition.\n * When embeddedStorage==false.\n *\n * @author <a href=\"mailto:liaochuntao@live.com\">liaochuntao</a>\n */",
        "/**\n * 判断是否启用单机嵌入式存储的 Spring {@link Condition}。\n *\n * <p>当 {@link DatasourceConfiguration#isEmbeddedStorage()} 为 true 且处于"
        " {@link EnvUtil#getStandaloneMode()} 单机模式时匹配，用于条件化注册 Derby 相关 Bean。</p>\n *\n * @author <a href=\"mailto:liaochuntao@live.com\">liaochuntao</a>\n */",
    ),
    (
        "    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {",
        "    /** 嵌入式存储开启且为单机模式时返回 true。 */\n    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {",
    ),
]

# --- PersistenceConstant ---

R["persistence/src/main/java/com/alibaba/nacos/persistence/constants/PersistenceConstant.java"] = [
    (
        "/**\n * Persistence constant.\n *\n * @author xiweng.yy\n */",
        "/**\n * 持久化模块常量定义。\n *\n * <p>集中维护数据源平台属性名、Derby 目录、Raft 分组等持久层通用常量。</p>\n *\n * @author xiweng.yy\n */",
    ),
    (
        "    public static final String DEFAULT_ENCODE = \"UTF-8\";",
        "    /** 默认字符编码 UTF-8。 */\n    public static final String DEFAULT_ENCODE = \"UTF-8\";",
    ),
    (
        "    /**\n     * May be removed with the upgrade of springboot version.\n     */",
        "    /** 旧版 Spring Boot 数据源平台属性名，升级后可能移除。 */",
    ),
    (
        "    public static final String DATASOURCE_PLATFORM_PROPERTY = \"spring.sql.init.platform\";",
        "    /** 新版 Spring Boot SQL 初始化平台属性名。 */\n    public static final String DATASOURCE_PLATFORM_PROPERTY = \"spring.sql.init.platform\";",
    ),
    (
        "    public static final String MYSQL = \"mysql\";",
        "    /** MySQL 数据源平台标识。 */\n    public static final String MYSQL = \"mysql\";",
    ),
    (
        "    public static final String DERBY = \"derby\";",
        "    /** Derby 嵌入式数据库平台标识。 */\n    public static final String DERBY = \"derby\";",
    ),
    (
        "    public static final String EMBEDDED_STORAGE = \"embeddedStorage\";",
        "    /** 嵌入式存储配置项键名。 */\n    public static final String EMBEDDED_STORAGE = \"embeddedStorage\";",
    ),
    (
        "    /**\n     * The derby base dir.\n     */",
        "    /** Derby 数据文件根目录名。 */",
    ),
    (
        "    /**\n     * Specifies that reads wait without timeout.\n     */",
        "    /** Raft 读等待无超时时的占位标识字符串。 */",
    ),
    (
        "    public static final String CONFIG_MODEL_RAFT_GROUP = \"nacos_config\";",
        "    /** 配置模块 Raft 一致性分组名。 */\n    public static final String CONFIG_MODEL_RAFT_GROUP = \"nacos_config\";",
    ),
]

# --- DataSourcePoolProperties ---

R["persistence/src/main/java/com/alibaba/nacos/persistence/datasource/DataSourcePoolProperties.java"] = [
    (
        "/**\n * DataSource pool properties.\n *\n * <p>Nacos server use HikariCP as the datasource pool. So the basic pool properties will based on {@link\n * com.zaxxer.hikari.HikariDataSource}.\n *\n * @author xiweng.yy\n */",
        "/**\n * 数据源连接池属性封装。\n *\n * <p>Nacos 使用 HikariCP 作为连接池，本类基于 {@link com.zaxxer.hikari.HikariDataSource} "
        "提供默认超时、池大小等配置，并支持从 {@code db.pool.config} 绑定外部属性。</p>\n *\n * @author xiweng.yy\n */",
    ),
    (
        "    public static final long DEFAULT_CONNECTION_TIMEOUT = TimeUnit.SECONDS.toMillis(3L);",
        "    /** 默认连接超时：3 秒。 */\n    public static final long DEFAULT_CONNECTION_TIMEOUT = TimeUnit.SECONDS.toMillis(3L);",
    ),
    (
        "    public static final int DEFAULT_MAX_POOL_SIZE = 20;",
        "    /** 默认最大连接池大小。 */\n    public static final int DEFAULT_MAX_POOL_SIZE = 20;",
    ),
    (
        "    private final HikariDataSource dataSource;",
        "    /** 内部 HikariCP 数据源实例。 */\n    private final HikariDataSource dataSource;",
    ),
    (
        "    /**\n     * Build new Hikari config.\n     *\n     * @return new hikari config\n     */",
        "    /**\n     * 从 Spring {@link Environment} 构建 Hikari 连接池配置。\n     *\n     * @return 已绑定 {@code db.pool.config} 的池属性对象\n     */",
    ),
    (
        "    public static DataSourcePoolProperties build(Environment environment) {",
        "    /** 创建实例并绑定环境变量中的池参数。 */\n    public static DataSourcePoolProperties build(Environment environment) {",
    ),
    (
        "    public HikariDataSource getDataSource() {",
        "    /** 返回底层 HikariCP 数据源。 */\n    public HikariDataSource getDataSource() {",
    ),
]

# --- DataSourceService ---

R["persistence/src/main/java/com/alibaba/nacos/persistence/datasource/DataSourceService.java"] = [
    (
        "/**\n * Datasource interface.\n *\n * @author Nacos\n */",
        "/**\n * 数据源服务接口。\n *\n * <p>抽象 Nacos 持久层 JDBC 访问：初始化、重载、主库可写检测、"
        " {@link JdbcTemplate} 与 {@link TransactionTemplate} 获取及健康状态查询。</p>\n *\n * @author Nacos\n */",
    ),
    (
        "    /**\n     * Initialize the relevant resource information.\n     *\n     * @throws Exception exception.\n     */",
        "    /**\n     * 初始化数据源及相关 JDBC 资源。\n     *\n     * @throws Exception 初始化失败时抛出\n     */",
    ),
    (
        "    /**\n     * Reload.\n     *\n     * @throws IOException exception.\n     */",
        "    /**\n     * 重新加载数据源配置（如外部 MySQL 多数据源切换）。\n     *\n     * @throws IOException 重载失败时抛出\n     */",
    ),
    (
        "    /**\n     * Check master db.\n     *\n     * @return is master.\n     */",
        "    /**\n     * 检测当前主库是否可写。\n     *\n     * @return 主库可写返回 true\n     */",
    ),
    (
        "    /**\n     * Get jdbc template.\n     *\n     * @return JdbcTemplate.\n     */",
        "    /**\n     * 获取用于 SQL 操作的 {@link JdbcTemplate}。\n     *\n     * @return JDBC 模板\n     */",
    ),
    (
        "    /**\n     * Get transaction template.\n     *\n     * @return TransactionTemplate.\n     */",
        "    /**\n     * 获取事务模板，用于编程式事务。\n     *\n     * @return 事务模板\n     */",
    ),
    (
        "    /**\n     * Get current db url.\n     *\n     * @return database url\n     */",
        "    /**\n     * 返回当前活跃数据源的 JDBC URL。\n     *\n     * @return 数据库连接 URL\n     */",
    ),
    (
        "    /**\n     * Get heath information.\n     *\n     * @return heath info.\n     */",
        "    /**\n     * 返回数据源健康状态摘要（UP/DOWN/WARN）。\n     *\n     * @return 健康信息字符串\n     */",
    ),
    (
        "    /**\n     * Get current db type.\n     *\n     * @return\n     */",
        "    /**\n     * 返回当前数据源平台类型（如 mysql、derby）。\n     *\n     * @return 数据源类型\n     */",
    ),
]

# --- DynamicDataSource ---

R["persistence/src/main/java/com/alibaba/nacos/persistence/datasource/DynamicDataSource.java"] = [
    (
        "/**\n * Datasource adapter.\n *\n * @author Nacos\n */",
        "/**\n * 动态数据源适配器（单例）。\n *\n * <p>根据 {@link DatasourceConfiguration#isEmbeddedStorage()} 在"
        " {@link LocalDataSourceServiceImpl}（Derby 嵌入式）与 {@link ExternalDataSourceServiceImpl}（外部 MySQL 等）"
        " 之间切换，单机默认嵌入式、集群默认外部库。</p>\n *\n * @author Nacos\n */",
    ),
    (
        "    private DataSourceService localDataSourceService = null;",
        "    /** 本地 Derby 嵌入式数据源服务。 */\n    private DataSourceService localDataSourceService = null;",
    ),
    (
        "    private DataSourceService basicDataSourceService = null;",
        "    /** 外部数据库数据源服务。 */\n    private DataSourceService basicDataSourceService = null;",
    ),
    (
        "    private static final DynamicDataSource INSTANCE = new DynamicDataSource();",
        "    /** 单例实例。 */\n    private static final DynamicDataSource INSTANCE = new DynamicDataSource();",
    ),
    (
        "    public static DynamicDataSource getInstance() {",
        "    /** 获取动态数据源单例。 */\n    public static DynamicDataSource getInstance() {",
    ),
    (
        "            // Embedded storage is used by default in stand-alone mode\n            // In cluster mode, external databases are used by default",
        "            // 单机模式默认使用嵌入式存储\n            // 集群模式默认使用外部数据库",
    ),
    (
        "    public synchronized DataSourceService getDataSource() {",
        "    /** 懒加载并返回当前模式对应的数据源服务。 */\n    public synchronized DataSourceService getDataSource() {",
    ),
]

# --- ExternalDataSourceProperties ---

R["persistence/src/main/java/com/alibaba/nacos/persistence/datasource/ExternalDataSourceProperties.java"] = [
    (
        "/**\n * Properties of external DataSource.\n *\n * @author Nacos\n */",
        "/**\n * 外部数据源配置属性。\n *\n * <p>从 {@code db.*} 配置绑定 URL、用户名、密码列表，按 {@code db.num} "
        "批量构建 HikariCP 数据源，支持主从多库部署。</p>\n *\n * @author Nacos\n */",
    ),
    (
        "    private static final String JDBC_DRIVER_NAME = \"com.mysql.cj.jdbc.Driver\";",
        "    /** 默认 MySQL JDBC 驱动类名。 */\n    private static final String JDBC_DRIVER_NAME = \"com.mysql.cj.jdbc.Driver\";",
    ),
    (
        "    private static final String TEST_QUERY = \"SELECT 1\";",
        "    /** 连接健康检测 SQL。 */\n    private static final String TEST_QUERY = \"SELECT 1\";",
    ),
    (
        "    private Integer num;",
        "    /** 外部数据源实例数量（主从个数）。 */\n    private Integer num;",
    ),
    (
        "    /**\n     * Build serveral HikariDataSource.\n     *\n     * @param environment {@link Environment}\n     * @param callback    Callback function when constructing data source\n     * @return List of {@link HikariDataSource}\n     */",
        "    /**\n     * 按配置构建多个 HikariCP 数据源。\n     *\n     * @param environment Spring 环境，用于绑定 {@code db} 前缀属性\n     * @param callback 每个数据源创建后的回调（如连接校验）\n     * @return HikariCP 数据源列表\n     */",
    ),
    (
        "    List<HikariDataSource> build(Environment environment, Callback<HikariDataSource> callback) {",
        "    /** 校验配置完整性并逐索引创建数据源。 */\n    List<HikariDataSource> build(Environment environment, Callback<HikariDataSource> callback) {",
    ),
    (
        "    interface Callback<D> {",
        "    /** 数据源构建完成后的回调接口。 */\n    interface Callback<D> {",
    ),
    (
        "        /**\n         * Perform custom logic.\n         *\n         * @param datasource dataSource.\n         */",
        "        /**\n         * 对每个新建数据源执行自定义逻辑。\n         *\n         * @param datasource 已配置的数据源\n         */",
    ),
]

# --- ExternalDataSourceServiceImpl ---

R["persistence/src/main/java/com/alibaba/nacos/persistence/datasource/ExternalDataSourceServiceImpl.java"] = [
    (
        "/**\n * Base data source.\n *\n * @author Nacos\n */",
        "/**\n * 外部数据库数据源服务实现。\n *\n * <p>管理多 HikariCP 数据源的主从选举、健康巡检、PostgreSQL tenant_id 模式校验，"
        " 并提供 {@link JdbcTemplate} 与事务模板供持久层使用。</p>\n *\n * @author Nacos\n */",
    ),
    (
        "    /**\n     * JDBC execute timeout value, unit:second.\n     */",
        "    /** JDBC 查询超时时间，单位：秒。 */",
    ),
    (
        "    private List<HikariDataSource> dataSourceList = new ArrayList<>();",
        "    /** 已加载的外部 HikariCP 数据源列表。 */\n    private List<HikariDataSource> dataSourceList = new ArrayList<>();",
    ),
    (
        "    private volatile int masterIndex;",
        "    /** 当前主库在 dataSourceList 中的索引。 */\n    private volatile int masterIndex;",
    ),
    (
        "    public void init() {",
        "    /** 初始化 JDBC 模板、事务管理器并启动主库选举与健康检查定时任务。 */\n    public void init() {",
    ),
    (
        "        // Set the maximum number of records to prevent memory expansion",
        "        // 限制最大返回行数，防止内存膨胀",
    ),
    (
        "        // Prevent the login interface from being too long because the main library is not available",
        "        // 主库不可用时缩短超时，避免登录接口长时间阻塞",
    ),
    (
        "        //  Database health check",
        "        // 初始化各数据源健康检测用的 JdbcTemplate",
    ),
    (
        "        // Transaction timeout needs to be distinguished from ordinary operations.",
        "        // 事务超时需与普通查询超时区分设置",
    ),
    (
        "    public synchronized void reload() throws IOException {",
        "    /** 重建数据源列表、重选主库并关闭旧连接池。 */\n    public synchronized void reload() throws IOException {",
    ),
    (
        "                    //check datasource connection",
        "                    // 校验数据源连接可用性",
    ),
    (
        "            //close old datasource.",
        "            // 关闭旧数据源释放连接",
    ),
    (
        "    public boolean checkMasterWritable() {",
        "    /** 通过 {@code SELECT @@read_only} 判断主库是否可写。 */\n    public boolean checkMasterWritable() {",
    ),
    (
        "    public String getHealth() {",
        "    /** 汇总各库健康状态，主库异常返回 DOWN，从库异常返回 WARN。 */\n    public String getHealth() {",
    ),
    (
        "                    // The master is unhealthy.",
        "                    // 主库不健康",
    ),
    (
        "                    // The slave  is unhealthy.",
        "                    // 从库不健康",
    ),
    (
        "    void validatePostgresqlTenantSchema() {",
        "    /** PostgreSQL 平台下校验 config 表 tenant_id 列约束。 */\n    void validatePostgresqlTenantSchema() {",
    ),
    (
        "    class SelectMasterTask implements Runnable {",
        "    /** 定时任务：通过试写探测可写主库并切换 JdbcTemplate。 */\n    class SelectMasterTask implements Runnable {",
    ),
    (
        "                } catch (DataAccessException e) { // read only",
        "                } catch (DataAccessException e) { // 只读库写入失败，继续尝试下一个",
    ),
    (
        "    class CheckDbHealthTask implements Runnable {",
        "    /** 定时任务：对各数据源执行探活查询并更新健康标记。 */\n    class CheckDbHealthTask implements Runnable {",
    ),
    (
        "                        // do nothing.",
        "                        // 空结果视为健康，忽略",
    ),
]

# --- LocalDataSourceServiceImpl ---

R["persistence/src/main/java/com/alibaba/nacos/persistence/datasource/LocalDataSourceServiceImpl.java"] = [
    (
        "/**\n * local data source.\n *\n * @author Nacos\n */",
        "/**\n * 本地 Derby 嵌入式数据源服务实现。\n *\n * <p>单机模式下在 Nacos 工作目录创建 Derby 库，执行 schema 脚本初始化表结构，"
        " 支持清理重建与备份恢复等运维操作。</p>\n *\n * @author Nacos\n */",
    ),
    (
        "    private final String jdbcDriverName = \"org.apache.derby.jdbc.EmbeddedDriver\";",
        "    /** Derby 嵌入式 JDBC 驱动类名。 */\n    private final String jdbcDriverName = \"org.apache.derby.jdbc.EmbeddedDriver\";",
    ),
    (
        "    private final String derbyBaseDir =",
        "    /** Derby 数据目录相对 Nacos Home 的路径。 */\n    private final String derbyBaseDir =",
    ),
    (
        "    private volatile JdbcTemplate jt;",
        "    /** 本地 Derby JDBC 模板。 */\n    private volatile JdbcTemplate jt;",
    ),
    (
        "    private String dataSourceType = \"derby\";",
        "    /** 数据源类型标识：derby。 */\n    private String dataSourceType = \"derby\";",
    ),
    (
        "    public synchronized void init() throws Exception {",
        "    /** 非外部库模式下创建 Derby 库并执行 schema 初始化。 */\n    public synchronized void init() throws Exception {",
    ),
    (
        "    public synchronized void reload() {",
        "    /** 重新执行 derby-schema.sql 脚本。 */\n    public synchronized void reload() {",
    ),
    (
        "    /**\n     * Clean and reopen Derby.\n     *\n     * @throws Exception exception.\n     */",
        "    /**\n     * 关闭并删除 Derby 数据目录后重新创建库。\n     *\n     * @throws Exception 清理或重建失败时抛出\n     */",
    ),
    (
        "    public void cleanAndReopenDerby() throws Exception {",
        "    /** 清空本地 Derby 并重新初始化。 */\n    public void cleanAndReopenDerby() throws Exception {",
    ),
    (
        "    /**\n     * Restore derby.\n     *\n     * @param jdbcUrl  jdbcUrl string value.\n     * @param callable callable.\n     * @throws Exception exception.\n     */",
        "    /**\n     * 清理 Derby 后执行自定义恢复逻辑并重新连接指定 JDBC URL。\n     *\n     * @param jdbcUrl 恢复后的 JDBC 连接串\n     * @param callable 恢复中间步骤回调\n     * @throws Exception 恢复失败时抛出\n     */",
    ),
    (
        "            // An error is thrown when the Derby shutdown is executed, which should be ignored",
        "            // Derby shutdown 会抛出预期异常，非 shutdown 消息则继续抛出",
    ),
    (
        "    private synchronized void initialize(String jdbcUrl) {",
        "    /** 配置 HikariCP 连接 Derby 并初始化 JdbcTemplate 与事务模板。 */\n    private synchronized void initialize(String jdbcUrl) {",
    ),
    (
        "    /**\n     * Load sql.\n     *\n     * @param sqlFile sql.\n     * @return sqls.\n     * @throws Exception Exception.\n     */",
        "    /**\n     * 从 conf 目录或 classpath 加载 SQL 脚本并拆分为语句列表。\n     *\n     * @param sqlFile 脚本 classpath 路径\n     * @return SQL 语句列表\n     * @throws Exception 读取或解析失败时抛出\n     */",
    ),
    (
        "    /**\n     * Execute sql.\n     *\n     * @param conn    connect.\n     * @param sqlFile sql.\n     * @throws Exception Exception.\n     */",
        "    /**\n     * 在指定连接上逐条执行 SQL 脚本，单条失败仅记录警告。\n     *\n     * @param conn 数据库连接\n     * @param sqlFile 脚本路径\n     * @throws Exception 加载脚本失败时抛出\n     */",
    ),
]

# --- NJdbcException ---

R["persistence/src/main/java/com/alibaba/nacos/persistence/exception/NJdbcException.java"] = [
    (
        "/**\n * NJdbcException.\n *\n * @author <a href=\"mailto:liaochuntao@live.com\">liaochuntao</a>\n */",
        "/**\n * Nacos 持久层 JDBC 访问异常。\n *\n * <p>继承 {@link DataAccessException}，可携带原始异常类名便于上层识别与转换。</p>\n *\n * @author <a href=\"mailto:liaochuntao@live.com\">liaochuntao</a>\n */",
    ),
    (
        "    private String originExceptionName;",
        "    /** 原始异常类名，用于错误分类。 */\n    private String originExceptionName;",
    ),
    (
        "    public NJdbcException(String msg) {",
        "    /** 仅含消息的 JDBC 异常。 */\n    public NJdbcException(String msg) {",
    ),
    (
        "    public NJdbcException(String msg, String originExceptionName) {",
        "    /** 含消息与原始异常类名的 JDBC 异常。 */\n    public NJdbcException(String msg, String originExceptionName) {",
    ),
    (
        "    public String getOriginExceptionName() {",
        "    /** 返回原始异常类名。 */\n    public String getOriginExceptionName() {",
    ),
]

# --- DerbyImportEvent ---

R["persistence/src/main/java/com/alibaba/nacos/persistence/model/event/DerbyImportEvent.java"] = [
    (
        "/**\n * Data import event.\n *\n * @author <a href=\"mailto:liaochuntao@live.com\">liaochuntao</a>\n */",
        "/**\n * Derby 数据导入进度事件。\n *\n * <p>继承 {@link SlowEvent}，通过 {@link #finished} 标识导入是否已完成，"
        " 供监听器异步感知嵌入式库迁移状态。</p>\n *\n * @author <a href=\"mailto:liaochuntao@live.com\">liaochuntao</a>\n */",
    ),
    (
        "    private final boolean finished;",
        "    /** 导入是否已结束。 */\n    private final boolean finished;",
    ),
    (
        "    public DerbyImportEvent(boolean finished) {",
        "    /** 构造导入事件并设置完成标志。 */\n    public DerbyImportEvent(boolean finished) {",
    ),
    (
        "    public boolean isFinished() {",
        "    /** 返回导入是否已完成。 */\n    public boolean isFinished() {",
    ),
]

# --- DerbyLoadEvent ---

R["persistence/src/main/java/com/alibaba/nacos/persistence/model/event/DerbyLoadEvent.java"] = [
    (
        "/**\n * DerbyLoadEvent.\n *\n * @author <a href=\"mailto:liaochuntao@live.com\">liaochuntao</a>\n */",
        "/**\n * Derby 数据库加载完成事件。\n *\n * <p>单例 {@link SlowEvent}，在嵌入式 Derby 初始化或 schema 加载完成后发布，"
        " 通知依赖方可以开始访问本地库。</p>\n *\n * @author <a href=\"mailto:liaochuntao@live.com\">liaochuntao</a>\n */",
    ),
    (
        "    public static final DerbyLoadEvent INSTANCE = new DerbyLoadEvent();",
        "    /** 全局单例事件实例。 */\n    public static final DerbyLoadEvent INSTANCE = new DerbyLoadEvent();",
    ),
]

# --- RaftDbErrorEvent ---

R["persistence/src/main/java/com/alibaba/nacos/persistence/model/event/RaftDbErrorEvent.java"] = [
    (
        "/**\n * RaftDBErrorEvent.\n *\n * @author <a href=\"mailto:liaochunyhm@live.com\">liaochuntao</a>\n */",
        "/**\n * Raft 嵌入式数据库错误事件。\n *\n * <p>当 Raft 持久化存储发生异常时发布，携带 {@link #ex} 供监听器记录告警或触发降级。</p>\n *\n * @author <a href=\"mailto:liaochunyhm@live.com\">liaochuntao</a>\n */",
    ),
    (
        "    private Throwable ex;",
        "    /** 导致 Raft DB 错误的异常对象。 */\n    private Throwable ex;",
    ),
    (
        "    public RaftDbErrorEvent(Throwable ex) {",
        "    /** 构造事件并绑定异常。 */\n    public RaftDbErrorEvent(Throwable ex) {",
    ),
    (
        "    public Throwable getEx() {",
        "    /** 返回关联的异常。 */\n    public Throwable getEx() {",
    ),
]

# --- DatasourceMetrics ---

R["persistence/src/main/java/com/alibaba/nacos/persistence/monitor/DatasourceMetrics.java"] = [
    (
        "/**\n * Metrics for datasource.\n *\n * @author xiweng.yy\n */",
        "/**\n * 数据源异常监控指标。\n *\n * <p>通过 Micrometer 暴露 {@code nacos_exception} 计数器，"
        " 统计 config 模块数据库相关异常次数。</p>\n *\n * @author xiweng.yy\n */",
    ),
    (
        "    public static Counter getDbException() {",
        "    /** 获取数据库异常 Micrometer 计数器。 */\n    public static Counter getDbException() {",
    ),
    (
        "        // TODO: After {@code NacosMeterRegistryCenter} move to more basic module, the usage can be changed.",
        "        // TODO: NacosMeterRegistryCenter 迁移至更基础模块后可改用统一注册中心",
    ),
    (
        "        // TODO: Current {@code NacosMeterRegistryCenter} is in core module, but core module maybe depend persistence to save namespace.",
        "        // TODO: 当前 core 模块可能依赖 persistence 存 namespace，故暂用 Metrics 全局注册",
    ),
]

# --- PaginationHelper ---

R["persistence/src/main/java/com/alibaba/nacos/persistence/repository/PaginationHelper.java"] = [
    (
        "/**\n * Pagination Utils interface.\n *\n * @author <a href=\"mailto:liaochuntao@live.com\">liaochuntao</a>\n */",
        "/**\n * 数据库分页查询辅助接口。\n *\n * <p>封装 count + fetch 分页、LIMIT 分页及 {@link MapperResult} 插件化 SQL 等多种分页模式，"
        " 返回 {@link Page} 统一结果。</p>\n *\n * @author <a href=\"mailto:liaochuntao@live.com\">liaochuntao</a>\n */",
    ),
    (
        "    Page<E> fetchPage(final String sqlCountRows, final String sqlFetchRows, final Object[] args,\n        final int pageNo,\n        final int pageSize, final RowMapper<E> rowMapper);",
        "    /** 标准 count + offset 分页查询。 */\n    Page<E> fetchPage(final String sqlCountRows, final String sqlFetchRows, final Object[] args,\n        final int pageNo,\n        final int pageSize, final RowMapper<E> rowMapper);",
    ),
    (
        "    Page<E> fetchPageLimit(final String sqlCountRows, final String sqlFetchRows,\n        final Object[] args, final int pageNo,\n        final int pageSize, final RowMapper<E> rowMapper);",
        "    /** 使用 LIMIT 语法的分页查询（count 与 fetch 共用 args）。 */\n    Page<E> fetchPageLimit(final String sqlCountRows, final String sqlFetchRows,\n        final Object[] args, final int pageNo,\n        final int pageSize, final RowMapper<E> rowMapper);",
    ),
    (
        "    Page<E> fetchPageLimit(final MapperResult countMapperResult, final MapperResult mapperResult,\n        final int pageNo,\n        final int pageSize, final RowMapper<E> rowMapper);",
        "    /** 基于插件 {@link MapperResult} 的分页查询。 */\n    Page<E> fetchPageLimit(final MapperResult countMapperResult, final MapperResult mapperResult,\n        final int pageNo,\n        final int pageSize, final RowMapper<E> rowMapper);",
    ),
    (
        "    void updateLimit(final String sql, final Object[] args);",
        "    /** 带 LIMIT 约束的更新操作。 */\n    void updateLimit(final String sql, final Object[] args);",
    ),
]

# --- RowMapperManager ---

R["persistence/src/main/java/com/alibaba/nacos/persistence/repository/RowMapperManager.java"] = [
    (
        "/**\n * Manager RowMapper {@link RowMapper} for database object mapping.\n *\n * @author <a href=\"mailto:liaochuntao@live.com\">liaochuntao</a>\n */",
        "/**\n * {@link RowMapper} 注册与管理器。\n *\n * <p>维护类全名到 RowMapper 实例的映射，内置 {@link MapRowMapper} 将结果集转为 Map，"
        " 支持运行时注册自定义映射器。</p>\n *\n * @author <a href=\"mailto:liaochuntao@live.com\">liaochuntao</a>\n */",
    ),
    (
        "    public static final MapRowMapper MAP_ROW_MAPPER = new MapRowMapper();",
        "    /** 默认 Map 行映射器单例。 */\n    public static final MapRowMapper MAP_ROW_MAPPER = new MapRowMapper();",
    ),
    (
        "    public static Map<String, RowMapper> mapperMap = new HashMap<>(16);",
        "    /** 类全名到 RowMapper 的注册表。 */\n    public static Map<String, RowMapper> mapperMap = new HashMap<>(16);",
    ),
    (
        "        // MAP_ROW_MAPPER",
        "        // 注册内置 MAP_ROW_MAPPER",
    ),
    (
        "    public static <D> RowMapper<D> getRowMapper(String classFullName) {",
        "    /** 按类全名查找已注册的 RowMapper。 */\n    public static <D> RowMapper<D> getRowMapper(String classFullName) {",
    ),
    (
        "    /**\n     * Register custom row mapper to manager.\n     *\n     * @param classFullName full class name of row mapper handled.\n     * @param rowMapper     row mapper\n     * @param <D>           class of row mapper handled\n     */",
        "    /**\n     * 注册自定义 RowMapper 到管理器。\n     *\n     * @param classFullName 映射器处理的类全名\n     * @param rowMapper RowMapper 实例\n     * @param <D> 映射目标类型\n     */",
    ),
    (
        "    public static synchronized <D> void registerRowMapper(String classFullName,",
        "    /** 注册或覆盖 RowMapper，冲突时记录警告。 */\n    public static synchronized <D> void registerRowMapper(String classFullName,",
    ),
    (
        "    public static final class MapRowMapper implements RowMapper<Map<String, Object>> {",
        "    /** 将 ResultSet 每行转为 LinkedHashMap 的 RowMapper 实现。 */\n    public static final class MapRowMapper implements RowMapper<Map<String, Object>> {",
    ),
    (
        "        public Map<String, Object> mapRow(ResultSet resultSet, int rowNum) throws SQLException {",
        "        /** 按列标签填充 Map，保持列顺序。 */\n        public Map<String, Object> mapRow(ResultSet resultSet, int rowNum) throws SQLException {",
    ),
]
