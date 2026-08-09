"""Targeted Chinese annotation refinements for Spring Framework 7.0.8 wave32a."""

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "NamedParameterJdbcTemplate.java": [
        (
            "\t/**\n\t */\n\tpublic static final int DEFAULT_CACHE_LIMIT = 256;",
            "\t/** 此模板 SQL 缓存的默认最大条目数：256。 */\n\tpublic static final int DEFAULT_CACHE_LIMIT = 256;",
        ),
        (
            "\t/**\n\t */\n\tprivate final JdbcOperations classicJdbcTemplate;",
            "\t/** 被包装的 {@link JdbcOperations} 委托。 */\n\tprivate final JdbcOperations classicJdbcTemplate;",
        ),
        (
            "\t/**\n\t */\n\tprivate volatile ConcurrentLruCache<String, ParsedSql> parsedSqlCache;",
            "\t/** 原始 SQL 字符串到 {@link ParsedSql} 表示的缓存。 */\n\tprivate volatile ConcurrentLruCache<String, ParsedSql> parsedSqlCache;",
        ),
    ],
    "DefaultJdbcClient.java": [
        (
            "\t/** 名称相关状态（`namedParamOps`）。 */\n\tprivate final NamedParameterJdbcOperations namedParamOps;",
            "\t/** 底层命名参数 JDBC 操作委托。 */\n\tprivate final NamedParameterJdbcOperations namedParamOps;",
        ),
        (
            "\t/** `conversionService`：该类的成员状态。 */\n\tprivate final ConversionService conversionService;",
            "\t/** 用于行映射与参数类型转换的 {@link ConversionService}。 */\n\tprivate final ConversionService conversionService;",
        ),
        (
            "\t/**\n\t * 创建 `DefaultJdbcClient` 的新实例。\n\t */\n\tpublic DefaultJdbcClient(DataSource dataSource) {",
            "\t/**\n\t * 基于给定 {@link DataSource} 创建实例（内部包装为 {@link JdbcTemplate}）。\n\t */\n\tpublic DefaultJdbcClient(DataSource dataSource) {",
        ),
        (
            "\t/**\n\t * 创建 `DefaultJdbcClient` 的新实例。\n\t */\n\tpublic DefaultJdbcClient(JdbcOperations jdbcTemplate) {",
            "\t/**\n\t * 基于给定 {@link JdbcOperations} 创建实例。\n\t */\n\tpublic DefaultJdbcClient(JdbcOperations jdbcTemplate) {",
        ),
        (
            "\t/**\n\t * 创建 `DefaultJdbcClient` 的新实例。\n\t */\n\tpublic DefaultJdbcClient(NamedParameterJdbcOperations jdbcTemplate, @Nullable ConversionService conversionService) {",
            "\t/**\n\t * 基于给定 {@link NamedParameterJdbcOperations} 创建实例，可选指定类型转换服务。\n\t */\n\tpublic DefaultJdbcClient(NamedParameterJdbcOperations jdbcTemplate, @Nullable ConversionService conversionService) {",
        ),
        (
            "\t/**\n\t * 方法 `sql`：完成本类中与「sql」相关的职责。\n\t */\n\t@Override\n\tpublic StatementSpec sql(String sql) {",
            "\t@Override\n\tpublic StatementSpec sql(String sql) {",
        ),
        (
            "\tprivate final Map<Class<?>, RowMapper<?>> rowMapperCache = new ConcurrentHashMap<>();\n",
            "\t/** 按目标类型缓存的 {@link RowMapper} 实例。 */\n\tprivate final Map<Class<?>, RowMapper<?>> rowMapperCache = new ConcurrentHashMap<>();\n",
        ),
    ],
    "NamedParameterUtils.java": [
        (
            "\t/**\n\t * 添加：Named Parameter（方法 `addNamedParameter`）。\n\t */\n\tprivate static int addNamedParameter(",
            "\t/** 将已解析的命名参数登记到参数列表并递增总计数。 */\n\tprivate static int addNamedParameter(",
        ),
        (
            "\t/**\n\t * 添加：New Named Parameter（方法 `addNewNamedParameter`）。\n\t */\n\tprivate static int addNewNamedParameter(",
            "\t/** 首次出现的命名参数加入集合并递增命名参数计数。 */\n\tprivate static int addNewNamedParameter(",
        ),
    ],
    "DataSourceTransactionManager.java": [
        (
            "\t/** 来源相关状态（`dataSource`）。 */\n\tprivate @Nullable DataSource dataSource;",
            "\t/** 此管理器所绑定事务的 JDBC {@link DataSource}。 */\n\tprivate @Nullable DataSource dataSource;",
        ),
        (
            "\t/** `false`：该类的成员状态。 */\n\tprivate boolean enforceReadOnly = false;",
            "\t/** 是否通过显式 SQL 语句强制只读事务（默认 {@code false}）。 */\n\tprivate boolean enforceReadOnly = false;",
        ),
        (
            "\t/** `defaultReadOnly`：该类的成员状态。 */\n\tprivate volatile @Nullable Boolean defaultReadOnly;",
            "\t/** 从数据源新连接上探测到的默认 {@link Connection#isReadOnly()} 标志。 */\n\tprivate volatile @Nullable Boolean defaultReadOnly;",
        ),
        (
            "\t/**\n\t * 在…之后回调：Properties Set（方法 `afterPropertiesSet`）。\n\t */\n\t@Override\n\tpublic void afterPropertiesSet() {",
            "\t/**\n\t * 校验已配置 {@link #setDataSource(DataSource) DataSource}。\n\t */\n\t@Override\n\tpublic void afterPropertiesSet() {",
        ),
        (
            "\t/**\n\t * 获取 Resource Factory（`ResourceFactory`）。\n\t */\n\t@Override\n\tpublic Object getResourceFactory() {",
            "\t/**\n\t * 返回作为资源工厂的数据源。\n\t */\n\t@Override\n\tpublic Object getResourceFactory() {",
        ),
        (
            "\t/**\n\t * 执行核心逻辑：Get Transaction（方法 `doGetTransaction`）。\n\t */\n\t@Override\n\tprotected Object doGetTransaction() {",
            "\t/**\n\t * 创建并初始化数据源事务对象，绑定当前线程上已有的连接持有者（若有）。\n\t */\n\t@Override\n\tprotected Object doGetTransaction() {",
        ),
        (
            "\t/**\n\t * 判断是否 Existing Transaction。\n\t */\n\t@Override\n\tprotected boolean isExistingTransaction(Object transaction) {",
            "\t/**\n\t * 判断给定事务对象是否对应已激活的 JDBC 事务。\n\t */\n\t@Override\n\tprotected boolean isExistingTransaction(Object transaction) {",
        ),
        (
            "\t/**\n\t * 执行核心逻辑：Begin（方法 `doBegin`）。\n\t */\n\t@Override\n\tprotected void doBegin(Object transaction, TransactionDefinition definition) {",
            "\t/**\n\t * 获取或创建连接、设置隔离级别与只读标志，并切换为手动提交以开启 JDBC 事务。\n\t */\n\t@Override\n\tprotected void doBegin(Object transaction, TransactionDefinition definition) {",
        ),
        (
            "\t/**\n\t * 执行核心逻辑：Suspend（方法 `doSuspend`）。\n\t */\n\t@Override\n\tprotected Object doSuspend(Object transaction) {",
            "\t/**\n\t * 挂起当前事务：解绑线程资源并清空连接持有者引用。\n\t */\n\t@Override\n\tprotected Object doSuspend(Object transaction) {",
        ),
        (
            "\t/**\n\t * 执行核心逻辑：Resume（方法 `doResume`）。\n\t */\n\t@Override\n\tprotected void doResume(@Nullable Object transaction, Object suspendedResources) {",
            "\t/**\n\t * 恢复先前挂起的事务资源到当前线程。\n\t */\n\t@Override\n\tprotected void doResume(@Nullable Object transaction, Object suspendedResources) {",
        ),
        (
            "\t/**\n\t * 执行核心逻辑：Commit（方法 `doCommit`）。\n\t */\n\t@Override\n\tprotected void doCommit(DefaultTransactionStatus status) {",
            "\t/**\n\t * 提交底层 JDBC 连接上的事务。\n\t */\n\t@Override\n\tprotected void doCommit(DefaultTransactionStatus status) {",
        ),
        (
            "\t/**\n\t * 执行核心逻辑：Rollback（方法 `doRollback`）。\n\t */\n\t@Override\n\tprotected void doRollback(DefaultTransactionStatus status) {",
            "\t/**\n\t * 回滚底层 JDBC 连接上的事务。\n\t */\n\t@Override\n\tprotected void doRollback(DefaultTransactionStatus status) {",
        ),
        (
            "\t/**\n\t * 执行核心逻辑：Set Rollback Only（方法 `doSetRollbackOnly`）。\n\t */\n\t@Override\n\tprotected void doSetRollbackOnly(DefaultTransactionStatus status) {",
            "\t/**\n\t * 将当前 JDBC 事务标记为仅回滚。\n\t */\n\t@Override\n\tprotected void doSetRollbackOnly(DefaultTransactionStatus status) {",
        ),
        (
            "\t/**\n\t * 执行核心逻辑：Cleanup After Completion（方法 `doCleanupAfterCompletion`）。\n\t */\n\t@Override\n\tprotected void doCleanupAfterCompletion(Object transaction) {",
            "\t/**\n\t * 事务完成后解绑连接、重置连接状态并释放新获取的连接。\n\t */\n\t@Override\n\tprotected void doCleanupAfterCompletion(Object transaction) {",
        ),
        (
            "\t * 指定是否通过事务连接上的显式语句强制事务的只读性质（如 {@link TransactionDefinition#isReadOnly()} 所示）：Oracle、MySQL \n"
            "\t * 和 Postgres 所理解的“SET TRANSACTION READ ONLY”。 <p> 的精确处理，包括在连接上执行的任何 SQL 语句，都可以通过 {@link #p\n"
            "\t * repareTransactionalConnection} 进行定制。 <p>这种只读处理模式超出了Spring默认应用的{@link Connection#setReadO\n"
            "\t * nly}提示。与标准 JDBC 提示相反，“SET TRANSACTION READ ONLY”强制执行类似隔离级别的连接模式，其中严格不允许数据操作语句。另外，在 Oracl\n"
            "\t * e 上，这种只读模式为整个事务提供了读一致性。 <p> 请注意，旧版 Oracle JDBC 驱动程序（9i、10g）用于强制执行此只读模式，即使对于 {@code Conne\n"
            "\t * ction.setReadOnly(true} 也是如此。然而，对于最近的驱动程序，需要明确应用这种强有力的强制执行，例如通过此标志。",
            "\t * 指定是否通过事务连接上的显式语句强制事务的只读性质（如 {@link TransactionDefinition#isReadOnly()} 所示），"
            "即 Oracle、MySQL 和 Postgres 所理解的 {@code SET TRANSACTION READ ONLY}。\n"
            "\t * <p>在连接上执行的具体 SQL 语句可通过 {@link #prepareTransactionalConnection} 定制。\n"
            "\t * <p>此只读处理模式超出 Spring 默认应用的 {@link Connection#setReadOnly} 提示；"
            "与标准 JDBC 提示不同，{@code SET TRANSACTION READ ONLY} 会强制类似隔离级别的连接模式，严格禁止数据修改语句。"
            "在 Oracle 上，该模式还为整个事务提供读一致性。\n"
            "\t * <p>旧版 Oracle JDBC 驱动（9i、10g）即使对 {@code Connection.setReadOnly(true)} 也会强制此只读模式；"
            "较新驱动则需通过本标志显式启用。",
        ),
    ],
}
