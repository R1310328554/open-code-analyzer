"""Chinese annotation replacements for Nacos 3.2.3 wave72a [0:15] persistence embedded repo/sql."""

R: dict[str, list[tuple[str, str]]] = {}

# --- EmbeddedPaginationHelperImpl ---

R["persistence/src/main/java/com/alibaba/nacos/persistence/repository/embedded/EmbeddedPaginationHelperImpl.java"] = [
    (
        "/**\n * Pagination Utils For Apache Derby.\n *\n * @param <E> Generic class\n * @author boyan\n * @date 2010-5-6\n */",
        "/**\n * 嵌入式 Derby 存储的分页查询辅助实现。\n *\n"
        " * <p>通过 {@link DatabaseOperate} 执行 COUNT 与数据查询，"
        "封装 {@link PaginationHelper} 接口供配置/命名等模块复用。</p>\n *\n"
        " * @param <E> Generic class\n * @author boyan\n * @date 2010-5-6\n */",
    ),
    (
        "    /**\n     * Take paging.\n     *\n     * @param sqlCountRows Query total SQL\n"
        "     * @param sqlFetchRows Query data sql\n     * @param args         query args\n"
        "     * @param pageNo       page number\n     * @param pageSize     page size\n"
        "     * @param rowMapper    Entity mapping\n     * @return Paging data\n     */",
        "    /**\n     * 标准分页查询：先统计总数再拉取当前页数据。\n     *\n"
        "     * @param sqlCountRows Query total SQL\n     * @param sqlFetchRows Query data sql\n"
        "     * @param args         query args\n     * @param pageNo       page number\n"
        "     * @param pageSize     page size\n     * @param rowMapper    Entity mapping\n"
        "     * @return Paging data\n     */",
    ),
    (
        "        // Create Page object",
        "        // 构造分页结果对象",
    ),
    (
        "        // Query the total number of current records",
        "        // 查询符合条件的记录总数",
    ),
    (
        "        // Count pages",
        "        // 根据总数与 pageSize 计算总页数",
    ),
]

# --- EmbeddedStorageContextHolder ---

R["persistence/src/main/java/com/alibaba/nacos/persistence/repository/embedded/EmbeddedStorageContextHolder.java"] = [
    (
        "/**\n * Embedded storae context holder.\n *\n * @author xiweng.yy\n */",
        "/**\n * 嵌入式存储线程上下文持有者。\n *\n"
        " * <p>使用 {@link ThreadLocal} 暂存待批量执行的 {@link ModifyRequest} 列表"
        "及扩展信息，供 {@link DatabaseOperate#blockUpdate()} 在同一事务中提交。</p>\n *\n"
        " * @author xiweng.yy\n */",
    ),
    (
        "    /**\n     * Add sql context.\n     *\n     * @param sql  sql\n     * @param args argument list\n     */\n    public static void addSqlContext(String sql, Object... args) {",
        "    /**\n     * 向当前线程追加一条待执行的修改 SQL。\n     *\n"
        "     * @param sql  sql\n     * @param args argument list\n     */\n"
        "    public static void addSqlContext(String sql, Object... args) {",
    ),
    (
        "    /**\n     * Add sql context.\n     *\n     * @param rollbackOnUpdateFail  roll back when update fail\n"
        "     * @param sql  sql\n     * @param args argument list\n     */\n"
        "    public static void addSqlContext(boolean rollbackOnUpdateFail, String sql, Object... args) {",
        "    /**\n     * 追加修改 SQL，并指定更新影响行数为 0 时是否回滚事务。\n     *\n"
        "     * @param rollbackOnUpdateFail  roll back when update fail\n"
        "     * @param sql  sql\n     * @param args argument list\n     */\n"
        "    public static void addSqlContext(boolean rollbackOnUpdateFail, String sql, Object... args) {",
    ),
    (
        "    /**\n     * Put extend info.\n     *\n     * @param key   key\n     * @param value value\n     */",
        "    /** 写入单条扩展上下文信息（如业务追踪键）。 */",
    ),
    (
        "    /**\n     * Put all extend info.\n     *\n     * @param map all extend info\n     */",
        "    /** 批量合并扩展上下文信息。 */",
    ),
    (
        "    /**\n     * Determine if key is included.\n     *\n     * @param key key\n     * @return {@code true} if contains key\n     */",
        "    /** 判断扩展上下文中是否包含指定键。 */",
    ),
    (
        "    public static List<ModifyRequest> getCurrentSqlContext() {",
        "    /** 获取当前线程累积的 SQL 修改请求列表。 */\n"
        "    public static List<ModifyRequest> getCurrentSqlContext() {",
    ),
    (
        "    public static Map<String, String> getCurrentExtendInfo() {",
        "    /** 获取当前线程的扩展信息映射。 */\n"
        "    public static Map<String, String> getCurrentExtendInfo() {",
    ),
    (
        "    public static void cleanAllContext() {",
        "    /** 清理当前线程全部 SQL 与扩展上下文，防止 ThreadLocal 泄漏。 */\n"
        "    public static void cleanAllContext() {",
    ),
]

# --- EmbeddedApplyHook ---

R["persistence/src/main/java/com/alibaba/nacos/persistence/repository/embedded/hook/EmbeddedApplyHook.java"] = [
    (
        "/**\n * Embedded storage apply hook.\n *\n * <p>Async Hook after embedded storage apply raft log.</p>\n *\n * @author xiweng.yy\n */",
        "/**\n * 嵌入式存储 Raft 日志 Apply 完成后的钩子基类。\n *\n"
        " * <p>子类构造时自动注册到 {@link EmbeddedApplyHookHolder}，"
        "在共识层持久化 SQL 后可异步触发下游刷新或通知。</p>\n *\n"
        " * @author xiweng.yy\n */",
    ),
    (
        "    /**\n     * Called after apply finished.\n     *\n     * @param log raft log\n     */",
        "    /**\n     * Raft 日志 Apply 成功后的回调。\n     *\n"
        "     * @param log raft log\n     */",
    ),
]

# --- EmbeddedApplyHookHolder ---

R["persistence/src/main/java/com/alibaba/nacos/persistence/repository/embedded/hook/EmbeddedApplyHookHolder.java"] = [
    (
        "/**\n * Holder for Embedded apply hook.\n *\n * @author xiweng.yy\n */",
        "/**\n * 嵌入式 Apply 钩子注册中心（单例）。\n *\n"
        " * <p>集中管理所有 {@link EmbeddedApplyHook} 实例，"
        "供存储层在日志 Apply 后统一遍历触发。</p>\n *\n"
        " * @author xiweng.yy\n */",
    ),
    (
        "    public static EmbeddedApplyHookHolder getInstance() {",
        "    /** 获取全局单例持有者。 */\n"
        "    public static EmbeddedApplyHookHolder getInstance() {",
    ),
    (
        "    public void register(EmbeddedApplyHook hook) {",
        "    /** 注册 Apply 完成钩子。 */\n"
        "    public void register(EmbeddedApplyHook hook) {",
    ),
    (
        "    public Set<EmbeddedApplyHook> getAllHooks() {",
        "    /** 返回已注册的全部钩子集合。 */\n"
        "    public Set<EmbeddedApplyHook> getAllHooks() {",
    ),
]

# --- BaseDatabaseOperate ---

R["persistence/src/main/java/com/alibaba/nacos/persistence/repository/embedded/operate/BaseDatabaseOperate.java"] = [
    (
        "/**\n * The Derby database basic operation.\n *\n * @author <a href=\"mailto:liaochuntao@live.com\">liaochuntao</a>\n */",
        "/**\n * Derby 嵌入式数据库通用操作接口。\n *\n"
        " * <p>封装 {@link JdbcTemplate} 查询/更新、事务批量提交及数据导入等默认实现，"
        "供集群与单机 {@link DatabaseOperate} 实现类复用。</p>\n *\n"
        " * @author <a href=\"mailto:liaochuntao@live.com\">liaochuntao</a>\n */",
    ),
    (
        "    /**\n     * query one result by sql then convert result to target type.\n     *\n"
        "     * @param jdbcTemplate {@link JdbcTemplate}\n     * @param sql          sql\n"
        "     * @param cls          target type\n     * @param <R>          target type\n     * @return R\n     */",
        "    /**\n     * 无参数单条查询，结果映射为目标类型。\n     *\n"
        "     * <p>无匹配行时返回 null 而非抛异常。</p>\n     *\n"
        "     * @param jdbcTemplate {@link JdbcTemplate}\n     * @param sql          sql\n"
        "     * @param cls          target type\n     * @param <R>          target type\n     * @return R\n     */",
    ),
    (
        "    /**\n     * query one result by sql and args then convert result to target type.\n     *\n"
        "     * @param jdbcTemplate {@link JdbcTemplate}\n     * @param sql          sql\n"
        "     * @param args         args\n     * @param cls          target type\n     * @param <R>          target type\n     * @return R\n     */",
        "    /** 带占位符参数的单条查询。 */",
    ),
    (
        "    /**\n     * query one result by sql and args then convert result to target type through {@link RowMapper}.\n     *\n"
        "     * @param jdbcTemplate {@link JdbcTemplate}\n     * @param sql          sql\n"
        "     * @param args         args\n     * @param mapper       {@link RowMapper}\n     * @param <R>          target type\n     * @return R\n     */",
        "    /** 使用 {@link RowMapper} 映射单条查询结果。 */",
    ),
    (
        "    /**\n     * query many result by sql and args then convert result to target type through {@link RowMapper}.\n     *\n"
        "     * @param jdbcTemplate {@link JdbcTemplate}\n     * @param sql          sql\n"
        "     * @param args         args\n     * @param mapper       {@link RowMapper}\n     * @param <R>          target type\n     * @return result list\n     */",
        "    /** 使用 RowMapper 查询多条记录。 */",
    ),
    (
        "    /**\n     * query many result by sql and args then convert result to target type.\n     *\n"
        "     * @param jdbcTemplate {@link JdbcTemplate}\n     * @param sql          sql\n"
        "     * @param args         args\n     * @param rClass       target type class\n     * @param <R>          target type\n     * @return result list\n     */",
        "    /** 按目标 Class 查询列表（如 Integer、String）。 */",
    ),
    (
        "    /**\n     * query many result by sql and args then convert result to List&lt;Map&lt;String, Object&gt;&gt;.\n     *\n"
        "     * @param jdbcTemplate {@link JdbcTemplate}\n     * @param sql          sql\n"
        "     * @param args         args\n     * @return List&lt;Map&lt;String, Object&gt;&gt;\n     */",
        "    /** 查询多行并返回列名到值的 Map 列表。 */",
    ),
    (
        "    /**\n     * execute update operation.\n     *\n"
        "     * @param transactionTemplate {@link TransactionTemplate}\n     * @param jdbcTemplate        {@link JdbcTemplate}\n"
        "     * @param contexts            {@link List} ModifyRequest list\n     * @return {@link Boolean}\n     */",
        "    /**\n     * 在事务中顺序执行多条 {@link ModifyRequest}。\n     *\n"
        "     * @param transactionTemplate {@link TransactionTemplate}\n     * @param jdbcTemplate        {@link JdbcTemplate}\n"
        "     * @param contexts            {@link List} ModifyRequest list\n     * @return {@link Boolean}\n     */",
    ),
    (
        "    /**\n     * execute update operation, to fix #3617.\n     *\n"
        "     * @param transactionTemplate {@link TransactionTemplate}\n     * @param jdbcTemplate        {@link JdbcTemplate}\n"
        "     * @param contexts            {@link List} ModifyRequest list\n     * @return {@link Boolean}\n     */",
        "    /**\n     * 带成功/失败回调的事务更新（修复 #3617）。\n     *\n"
        "     * <p>单条 SQL 更新影响行数为 0 且标记回滚时，整笔事务回滚。</p>\n     *\n"
        "     * @param transactionTemplate {@link TransactionTemplate}\n     * @param jdbcTemplate        {@link JdbcTemplate}\n"
        "     * @param contexts            {@link List} ModifyRequest list\n     * @return {@link Boolean}\n     */",
    ),
    (
        "                        LoggerUtils.printIfDebugEnabled(LOGGER, \"current sql : {}\", errSql[0]);",
        "                        // 调试模式下打印当前执行的 SQL 与参数\n"
        "                        LoggerUtils.printIfDebugEnabled(LOGGER, \"current sql : {}\", errSql[0]);",
    ),
    (
        "    /**\n     * Perform data import.\n     *\n"
        "     * @param template {@link JdbcTemplate}\n     * @param requests {@link List} ModifyRequest list\n     * @return {@link Boolean}\n     */",
        "    /**\n     * 批量导入外部 SQL 到 Derby。\n     *\n"
        "     * <p>对 INSERT 语句做 Derby 方言修正后使用 batchUpdate 提交。</p>\n     *\n"
        "     * @param template {@link JdbcTemplate}\n     * @param requests {@link List} ModifyRequest list\n     * @return {@link Boolean}\n     */",
    ),
]

# --- DatabaseOperate ---

R["persistence/src/main/java/com/alibaba/nacos/persistence/repository/embedded/operate/DatabaseOperate.java"] = [
    (
        "/**\n * Derby database operation.\n *\n * @author <a href=\"mailto:liaochuntao@live.com\">liaochuntao</a>\n */",
        "/**\n * 嵌入式 Derby 数据库操作门面接口。\n *\n"
        " * <p>对外暴露查询、批量更新、数据导入及基于 {@link EmbeddedStorageContextHolder} "
        "的 blockUpdate 能力，屏蔽单机/集群实现差异。</p>\n *\n"
        " * @author <a href=\"mailto:liaochuntao@live.com\">liaochuntao</a>\n */",
    ),
    (
        "    /**\n     * Data query transaction.\n     *\n     * @param sql sqk text\n     * @param cls target type\n     * @param <R> return type\n     * @return query result\n     */\n    <R> R queryOne(String sql, Class<R> cls);",
        "    /** 无参数单条查询。 */\n    <R> R queryOne(String sql, Class<R> cls);",
    ),
    (
        "    /**\n     * Data query transaction.\n     *\n     * @param sql  sqk text\n     * @param args sql parameters\n     * @param cls  target type\n     * @param <R>  return type\n     * @return query result\n     */\n    <R> R queryOne(String sql, Object[] args, Class<R> cls);",
        "    /** 带参数单条查询。 */\n    <R> R queryOne(String sql, Object[] args, Class<R> cls);",
    ),
    (
        "    /**\n     * Data query transaction.\n     *\n     * @param sql    sqk text\n     * @param args   sql parameters\n     * @param mapper Database query result converter\n     * @param <R>    return type\n     * @return query result\n     */\n    <R> R queryOne(String sql, Object[] args, RowMapper<R> mapper);",
        "    /** 使用 RowMapper 的单条查询。 */\n    <R> R queryOne(String sql, Object[] args, RowMapper<R> mapper);",
    ),
    (
        "    /**\n     * Data query transaction.\n     *\n     * @param sql    sqk text\n     * @param args   sql parameters\n     * @param mapper Database query result converter\n     * @param <R>    return type\n     * @return query result\n     */\n    <R> List<R> queryMany(String sql, Object[] args, RowMapper<R> mapper);",
        "    /** 使用 RowMapper 的多条查询。 */\n    <R> List<R> queryMany(String sql, Object[] args, RowMapper<R> mapper);",
    ),
    (
        "    /**\n     * Data query transaction.\n     *\n     * @param sql    sqk text\n     * @param args   sql parameters\n     * @param rClass target type\n     * @param <R>    return type\n     * @return query result\n     */\n    <R> List<R> queryMany(String sql, Object[] args, Class<R> rClass);",
        "    /** 按 Class 类型查询列表。 */\n    <R> List<R> queryMany(String sql, Object[] args, Class<R> rClass);",
    ),
    (
        "    /**\n     * Data query transaction.\n     *\n     * @param sql  sqk text\n     * @param args sql parameters\n     * @return query result\n     */\n    List<Map<String, Object>> queryMany(String sql, Object[] args);",
        "    /** 查询多行 Map 结果。 */\n    List<Map<String, Object>> queryMany(String sql, Object[] args);",
    ),
    (
        "    /**\n     * data modify transaction.\n     *\n     * @param modifyRequests {@link List}\n     * @param consumer       {@link BiConsumer}\n     * @return is success\n     */\n    Boolean update(List<ModifyRequest> modifyRequests, BiConsumer<Boolean, Throwable> consumer);",
        "    /** 批量修改数据，支持结果回调。 */\n"
        "    Boolean update(List<ModifyRequest> modifyRequests, BiConsumer<Boolean, Throwable> consumer);",
    ),
    (
        "    /**\n     * data modify transaction.\n     *\n     * @param modifyRequests {@link List}\n     * @return is success\n     */\n    default Boolean update(List<ModifyRequest> modifyRequests) {",
        "    /** 批量修改数据（无回调）。 */\n    default Boolean update(List<ModifyRequest> modifyRequests) {",
    ),
    (
        "    /**\n     * data importing, This method is suitable for importing data from external data sources into embedded data\n     * sources.\n     *\n     * @param file {@link File}\n     * @return {@link CompletableFuture}\n     */",
        "    /**\n     * 从外部 SQL 文件异步导入嵌入式 Derby。\n     *\n"
        "     * @param file {@link File}\n     * @return {@link CompletableFuture}\n     */",
    ),
    (
        "    /**\n     * data modify transaction The SqlContext to be executed in the current thread will be executed and automatically\n     * cleared.\n     *\n     * @return is success\n     */",
        "    /** 提交并清空当前线程 {@link EmbeddedStorageContextHolder} 中的 SQL 上下文。 */",
    ),
    (
        "    /**\n     * data modify transaction The SqlContext to be executed in the current thread will be executed and automatically\n     * cleared.\n     * @author klw(213539@qq.com)\n     * 2020/8/24 18:16\n     * @param consumer the consumer\n     * @return java.lang.Boolean\n     */",
        "    /**\n     * 提交当前线程 SQL 上下文，并在 finally 中清理 ThreadLocal。\n     *\n"
        "     * @author klw(213539@qq.com)\n     * 2020/8/24 18:16\n     * @param consumer the consumer\n     * @return java.lang.Boolean\n     */",
    ),
]

# --- StandaloneDatabaseOperateImpl ---

R["persistence/src/main/java/com/alibaba/nacos/persistence/repository/embedded/operate/StandaloneDatabaseOperateImpl.java"] = [
    (
        "/**\n * Derby operation in stand-alone mode.\n *\n * @author <a href=\"mailto:liaochuntao@live.com\">liaochuntao</a>\n */",
        "/**\n * 单机模式下 Derby 数据库操作实现。\n *\n"
        " * <p>在 {@link ConditionStandaloneEmbedStorage} 条件下装配，"
        "直接使用本地 {@link JdbcTemplate} 与 {@link SqlTypeLimiter} 执行 SQL。</p>\n *\n"
        " * @author <a href=\"mailto:liaochuntao@live.com\">liaochuntao</a>\n */",
    ),
    (
        "    @PostConstruct\n    protected void init() {",
        "    /** 初始化时从 {@link DynamicDataSource} 获取 JdbcTemplate 与事务模板。 */\n"
        "    @PostConstruct\n    protected void init() {",
    ),
    (
        "                int batchSize = 1000;",
        "                // 每批最多 1000 条 SQL，异步并行导入",
    ),
    (
        "                        sqlLimiter.doLimit(sql);",
        "                        // 导入前校验 SQL 类型是否在白名单内",
    ),
]

# --- ModifyRequest ---

R["persistence/src/main/java/com/alibaba/nacos/persistence/repository/embedded/sql/ModifyRequest.java"] = [
    (
        "/**\n * Represents a database UPDATE or INSERT or DELETE statement.\n *\n * @author <a href=\"mailto:liaochuntao@live.com\">liaochuntao</a>\n */",
        "/**\n * 封装一条数据库写操作（INSERT/UPDATE/DELETE）。\n *\n"
        " * <p>携带执行序号、SQL 文本、占位符参数及“更新失败是否回滚”标志，"
        "供嵌入式存储批量事务提交使用。</p>\n *\n"
        " * @author <a href=\"mailto:liaochuntao@live.com\">liaochuntao</a>\n */",
    ),
    (
        "    private int executeNo;",
        "    /** 在同一批次中的执行顺序号。 */\n    private int executeNo;",
    ),
    (
        "    private String sql;",
        "    /** 待执行的 SQL 语句。 */\n    private String sql;",
    ),
    (
        "    private boolean rollBackOnUpdateFail = Boolean.FALSE;",
        "    /** 更新影响行数小于 1 时是否触发事务回滚。 */\n    private boolean rollBackOnUpdateFail = Boolean.FALSE;",
    ),
    (
        "    private Object[] args;",
        "    /** SQL 占位符参数数组。 */\n    private Object[] args;",
    ),
]

# --- QueryType ---

R["persistence/src/main/java/com/alibaba/nacos/persistence/repository/embedded/sql/QueryType.java"] = [
    (
        "/**\n * Associated with the method correspondence of the {@link org.springframework.jdbc.core.JdbcTemplate}.\n *\n * @author <a href=\"mailto:liaochuntao@live.com\">liaochuntao</a>\n */",
        "/**\n * 查询类型常量，对应 {@link org.springframework.jdbc.core.JdbcTemplate} 的不同查询方法。\n *\n"
        " * <p>供 {@link SelectRequest} 在 Raft 复制查询时选择正确的 JdbcTemplate 调用路径。</p>\n *\n"
        " * @author <a href=\"mailto:liaochuntao@live.com\">liaochuntao</a>\n */",
    ),
    (
        "    /**\n     * {@link org.springframework.jdbc.core.JdbcTemplate#queryForObject(String, RowMapper)}.\n     */",
        "    /** 带 RowMapper 与参数的单条查询。 */",
    ),
    (
        "    /**\n     * {@link org.springframework.jdbc.core.JdbcTemplate#queryForObject(String, Class)}.\n     */",
        "    /** 无参数、按 Class 映射的单条查询。 */",
    ),
    (
        "    /**\n     * {@link org.springframework.jdbc.core.JdbcTemplate#queryForObject(String, Object[], Class)}.\n     */",
        "    /** 带参数、按 Class 映射的单条查询。 */",
    ),
    (
        "    /**\n     * {@link org.springframework.jdbc.core.JdbcTemplate#query(String, Object[], RowMapper)}.\n     */",
        "    /** 带 RowMapper 的多条查询。 */",
    ),
    (
        "    /**\n     * {@link org.springframework.jdbc.core.JdbcTemplate#queryForList(String, Object...)}.\n     */",
        "    /** 返回 List&lt;Map&gt; 的多条查询。 */",
    ),
    (
        "    /**\n     * {@link org.springframework.jdbc.core.JdbcTemplate#queryForList(String, Object[], Class)}.\n     */",
        "    /** 按 Class 映射的多条列表查询。 */",
    ),
]

# --- SelectRequest ---

R["persistence/src/main/java/com/alibaba/nacos/persistence/repository/embedded/sql/SelectRequest.java"] = [
    (
        "/**\n * Represents a database SELECT statement.\n *\n * @author <a href=\"mailto:liaochuntao@live.com\">liaochuntao</a>\n */",
        "/**\n * 封装一条 SELECT 查询请求。\n *\n"
        " * <p>包含 {@link QueryType}、SQL、参数及结果类型名，"
        "用于嵌入式存储在集群间复制只读查询。</p>\n *\n"
        " * @author <a href=\"mailto:liaochuntao@live.com\">liaochuntao</a>\n */",
    ),
    (
        "    private byte queryType;",
        "    /** 查询类型，见 {@link QueryType} 常量。 */\n    private byte queryType;",
    ),
    (
        "    private String sql;",
        "    /** SELECT SQL 文本。 */\n    private String sql;",
    ),
    (
        "    private Object[] args;",
        "    /** 查询占位符参数。 */\n    private Object[] args;",
    ),
    (
        "    private String className;",
        "    /** 结果映射类型的全限定类名。 */\n    private String className;",
    ),
    (
        "    public static SelectRequestBuilder builder() {",
        "    /** 创建 {@link SelectRequestBuilder} 构建查询请求。 */\n"
        "    public static SelectRequestBuilder builder() {",
    ),
    (
        "        /**\n         * build select request.\n         *\n         * @return {@link SelectRequest}\n         */",
        "        /** 构建不可变的 {@link SelectRequest} 实例。 */",
    ),
]

# --- SqlLimiter ---

R["persistence/src/main/java/com/alibaba/nacos/persistence/repository/embedded/sql/limiter/SqlLimiter.java"] = [
    (
        "/**\n * SQL limiter.\n *\n * @author xiweng.yy\n */",
        "/**\n * SQL 类型白名单限制器接口。\n *\n"
        " * <p>在嵌入式 Derby 场景下拦截非法 DML/DDL，"
        "防止导入或共识复制执行危险语句。</p>\n *\n"
        " * @author xiweng.yy\n */",
    ),
    (
        "    /**\n     * Do SQL limit for modify request.\n     *\n     * @param modifyRequest modify request\n     * @throws SQLException when SQL match the limit rule.\n     */\n    void doLimitForModifyRequest(ModifyRequest modifyRequest) throws SQLException;",
        "    /** 校验单条 {@link ModifyRequest} 的 SQL 类型。 */\n"
        "    void doLimitForModifyRequest(ModifyRequest modifyRequest) throws SQLException;",
    ),
    (
        "    /**\n     * Do SQL limit for modify request.\n     *\n     * @param modifyRequests modify request\n     * @throws SQLException when SQL match the limit rule.\n     */\n    void doLimitForModifyRequest(List<ModifyRequest> modifyRequests) throws SQLException;",
        "    /** 批量校验修改请求列表。 */\n"
        "    void doLimitForModifyRequest(List<ModifyRequest> modifyRequests) throws SQLException;",
    ),
    (
        "    /**\n     * Do SQL limit for select request.\n     *\n     * @param selectRequest select request\n     * @throws SQLException when SQL match the limit rule.\n     */\n    void doLimitForSelectRequest(SelectRequest selectRequest) throws SQLException;",
        "    /** 校验单条 {@link SelectRequest}。 */\n"
        "    void doLimitForSelectRequest(SelectRequest selectRequest) throws SQLException;",
    ),
    (
        "    /**\n     * Do SQL limit for select request.\n     *\n     * @param selectRequests select request\n     * @throws SQLException when SQL match the limit rule.\n     */\n    void doLimitForSelectRequest(List<SelectRequest> selectRequests) throws SQLException;",
        "    /** 批量校验查询请求列表。 */\n"
        "    void doLimitForSelectRequest(List<SelectRequest> selectRequests) throws SQLException;",
    ),
    (
        "    /**\n     * Do SQL limit for sql.\n     *\n     * @param sql SQL\n     * @throws SQLException when SQL match the limit rule.\n     */\n    void doLimit(String sql) throws SQLException;",
        "    /** 校验原始 SQL 字符串。 */\n    void doLimit(String sql) throws SQLException;",
    ),
    (
        "    /**\n     * Do SQL limit for sql.\n     *\n     * @param sql SQL\n     * @throws SQLException when SQL match the limit rule.\n     */\n    void doLimit(List<String> sql) throws SQLException;",
        "    /** 批量校验 SQL 字符串列表。 */\n    void doLimit(List<String> sql) throws SQLException;",
    ),
]

# --- SqlTypeLimiter ---

R["persistence/src/main/java/com/alibaba/nacos/persistence/repository/embedded/sql/limiter/SqlTypeLimiter.java"] = [
    (
        "/**\n * SQL Type Limiter, Nacos only allow `INSERT`, `UPDATE`, `DELETE`, `SELECT`, `CREATE SCHEMA`, `CREATE TABLE`, `CREATE\n * INDEX` and `ALTER TABLE`.\n *\n * @author xiweng.yy\n */",
        "/**\n * 基于 SQL 首词/次词的白名单类型限制器。\n *\n"
        " * <p>Nacos 仅允许 INSERT、UPDATE、DELETE、SELECT 及有限的 CREATE/ALTER DDL；"
        "可通过 {@code nacos.persistence.sql.derby.limit.enabled} 关闭。</p>\n *\n"
        " * @author xiweng.yy\n */",
    ),
    (
        "    private static final String ENABLED_SQL_LIMIT = \"nacos.persistence.sql.derby.limit.enabled\";",
        "    /** 是否启用 Derby SQL 类型限制的开关配置键。 */\n"
        "    private static final String ENABLED_SQL_LIMIT = \"nacos.persistence.sql.derby.limit.enabled\";",
    ),
    (
        "        String firstToken = trimmedSql.substring(0, firstTokenIndex).toUpperCase();",
        "        // 取 SQL 第一个关键字判断 DML 类型",
    ),
    (
        "    private void throwException(String sql) throws SQLException {",
        "    /** 抛出“不支持的 SQL 类型”异常。 */\n"
        "    private void throwException(String sql) throws SQLException {",
    ),
    (
        "    private void checkSqlForSecondToken(int firstTokenIndex, String trimmedSql)",
        "    /** 对 CREATE/ALTER 语句校验第二个关键字（SCHEMA/TABLE/INDEX）。 */\n"
        "    private void checkSqlForSecondToken(int firstTokenIndex, String trimmedSql)",
    ),
]

# --- ExternalStoragePaginationHelperImpl ---

R["persistence/src/main/java/com/alibaba/nacos/persistence/repository/extrnal/ExternalStoragePaginationHelperImpl.java"] = [
    (
        "/**\n * External Storage Pagination utils.\n *\n * @author <a href=\"mailto:liaochuntao@live.com\">liaochuntao</a>\n */",
        "/**\n * 外部关系型存储（MySQL 等）的分页查询辅助实现。\n *\n"
        " * <p>直接使用 {@link JdbcTemplate} 执行 COUNT 与分页查询，"
        "与嵌入式 {@link EmbeddedPaginationHelperImpl} 行为对齐。</p>\n *\n"
        " * @author <a href=\"mailto:liaochuntao@live.com\">liaochuntao</a>\n */",
    ),
    (
        "    /**\n     * Take paging.\n     *\n     * @param sqlCountRows query total SQL\n     * @param sqlFetchRows query data sql\n"
        "     * @param args         query parameters\n     * @param pageNo       page number\n     * @param pageSize     page size\n"
        "     * @param rowMapper    {@link RowMapper}\n     * @return Paginated data {@code <E>}\n     */",
        "    /** 标准分页：统计总数后查询当前页数据。 */",
    ),
    (
        "        // Create Page object",
        "        // 构造分页结果 Page 对象",
    ),
    (
        "        // Query the total number of current records",
        "        // 执行 COUNT 查询获取记录总数",
    ),
    (
        "        // Compute pages count",
        "        // 计算可用总页数",
    ),
]

# --- ConnectionCheckUtil ---

R["persistence/src/main/java/com/alibaba/nacos/persistence/utils/ConnectionCheckUtil.java"] = [
    (
        "/**\n * DataSource Connection CheckUtil.\n *\n * @author Long Yu\n */",
        "/**\n * HikariCP 数据源连通性校验工具。\n *\n"
        " * <p>启动或切换数据源时主动获取连接并探测，"
        "避免控制台显示 [no datasource set] 等误导信息。</p>\n *\n"
        " * @author Long Yu\n */",
    ),
    (
        "    /**\n     * check HikariDataSource connection ,avoid [no datasource set] text.\n     *\n     * @param ds HikariDataSource object\n     */",
        "    /**\n     * 校验 Hikari 数据源能否正常建立连接。\n     *\n"
        "     * <p>连接失败时包装为 {@link RuntimeException} 向上抛出。</p>\n     *\n"
        "     * @param ds HikariDataSource object\n     */",
    ),
    (
        "        try (java.sql.Connection connection = ds.getConnection()) {",
        "        // 借连接后立即探测 isClosed，确保池配置有效",
    ),
]

# --- DatasourcePlatformUtil ---

R["persistence/src/main/java/com/alibaba/nacos/persistence/utils/DatasourcePlatformUtil.java"] = [
    (
        "/**\n * get datasource platform util.\n *\n * @author lixiaoshuang\n */",
        "/**\n * 读取持久化层数据源平台（derby/mysql 等）的工具类。\n *\n"
        " * <p>优先读取新配置项，兼容旧版 property 名称。</p>\n *\n"
        " * @author lixiaoshuang\n */",
    ),
    (
        "    /**\n     * get datasource platform.\n     *\n     * @param defaultPlatform default platform.\n     * @return\n     */",
        "    /**\n     * 从环境变量/配置中获取数据源平台标识。\n     *\n"
        "     * <p>新键 {@link PersistenceConstant#DATASOURCE_PLATFORM_PROPERTY} 为空时"
        "回退到 {@link PersistenceConstant#DATASOURCE_PLATFORM_PROPERTY_OLD}。</p>\n     *\n"
        "     * @param defaultPlatform default platform.\n     * @return\n     */",
    ),
    (
        "        if (StringUtils.isBlank(platform)) {",
        "        // 新配置项未设置时使用旧版 spring.datasource.platform",
    ),
]
