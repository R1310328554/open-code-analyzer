"""Chinese JavaDoc replacements for springframework wave27a init/embedded classes (part A)."""

INIT_A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "SimpleDriverDataSourceFactory.java": [
        (
            "/**\n * Creates a {@link SimpleDriverDataSource}.\n *\n * @author Keith Donald\n * @author Juergen Hoeller\n * @since 3.0\n */",
            "/**\n * 创建 {@link SimpleDriverDataSource}。\n *\n * @author Keith Donald\n * @author Juergen Hoeller\n * @since 3.0\n */",
        ),
        (
            "\tprivate final SimpleDriverDataSource dataSource = new SimpleDriverDataSource();\n",
            "\t/** 内部持有的 SimpleDriverDataSource 实例。 */\n\tprivate final SimpleDriverDataSource dataSource = new SimpleDriverDataSource();\n",
        ),
        (
            "\t@Override\n\tpublic ConnectionProperties getConnectionProperties() {\n\t\treturn new ConnectionProperties() {",
            "\t/** 返回用于配置驱动、URL、用户名与密码的连接属性回调。 */\n\t@Override\n\tpublic ConnectionProperties getConnectionProperties() {\n\t\treturn new ConnectionProperties() {",
        ),
        (
            "\t@Override\n\tpublic DataSource getDataSource() {\n\t\treturn this.dataSource;\n\t}",
            "\t/** 返回已配置的 {@link DataSource} 实例。 */\n\t@Override\n\tpublic DataSource getDataSource() {\n\t\treturn this.dataSource;\n\t}",
        ),
    ],
    "CannotReadScriptException.java": [
        (
            "/**\n * Thrown by {@link ScriptUtils} if an SQL script cannot be read.\n *\n * @author Keith Donald\n * @author Sam Brannen\n * @since 3.0\n */",
            "/**\n * 当 SQL 脚本无法读取时，由 {@link ScriptUtils} 抛出。\n *\n * @author Keith Donald\n * @author Sam Brannen\n * @since 3.0\n */",
        ),
        (
            "\t/**\n\t * Create a new {@code CannotReadScriptException}.\n\t * @param resource the resource that cannot be read from\n\t * @param cause the underlying cause of the resource access failure\n\t */",
            "\t/**\n\t * 创建新的 {@code CannotReadScriptException}。\n\t * @param resource 无法读取的资源\n\t * @param cause 资源访问失败的根本原因\n\t */",
        ),
    ],
    "ScriptException.java": [
        (
            "/**\n * Root of the hierarchy of data access exceptions that are related to processing\n * of SQL scripts.\n *\n * @author Sam Brannen\n * @since 4.0.3\n */",
            "/**\n * 与 SQL 脚本处理相关的数据访问异常层次结构的根类。\n *\n * @author Sam Brannen\n * @since 4.0.3\n */",
        ),
        (
            "\t/**\n\t * Create a new {@code ScriptException}.\n\t * @param message the detail message\n\t */",
            "\t/**\n\t * 创建新的 {@code ScriptException}。\n\t * @param message 详细消息\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@code ScriptException}.\n\t * @param message the detail message\n\t * @param cause the root cause\n\t */",
            "\t/**\n\t * 创建新的 {@code ScriptException}。\n\t * @param message 详细消息\n\t * @param cause 根本原因\n\t */",
        ),
    ],
    "ScriptParseException.java": [
        (
            "/**\n * Thrown by {@link ScriptUtils} if an SQL script cannot be properly parsed.\n *\n * @author Sam Brannen\n * @since 4.0.3\n */",
            "/**\n * 当 SQL 脚本无法正确解析时，由 {@link ScriptUtils} 抛出。\n *\n * @author Sam Brannen\n * @since 4.0.3\n */",
        ),
        (
            "\t/**\n\t * Create a new {@code ScriptParseException}.\n\t * @param message detailed message\n\t * @param resource the resource from which the SQL script was read\n\t */",
            "\t/**\n\t * 创建新的 {@code ScriptParseException}。\n\t * @param message 详细消息\n\t * @param resource 读取 SQL 脚本的资源\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@code ScriptParseException}.\n\t * @param message detailed message\n\t * @param resource the resource from which the SQL script was read\n\t * @param cause the underlying cause of the failure\n\t */",
            "\t/**\n\t * 创建新的 {@code ScriptParseException}。\n\t * @param message 详细消息\n\t * @param resource 读取 SQL 脚本的资源\n\t * @param cause 失败的根本原因\n\t */",
        ),
    ],
    "ScriptStatementFailedException.java": [
        (
            "/**\n * Thrown by {@link ScriptUtils} if a statement in an SQL script failed when\n * executing it against the target database.\n *\n * @author Juergen Hoeller\n * @author Sam Brannen\n * @since 3.0.5\n */",
            "/**\n * 当 SQL 脚本中的某条语句在目标数据库上执行失败时，由 {@link ScriptUtils} 抛出。\n *\n * @author Juergen Hoeller\n * @author Sam Brannen\n * @since 3.0.5\n */",
        ),
        (
            "\t/**\n\t * Construct a new {@code ScriptStatementFailedException}.\n\t * @param stmt the actual SQL statement that failed\n\t * @param stmtNumber the statement number in the SQL script (i.e.,\n\t * the n<sup>th</sup> statement present in the resource)\n\t * @param encodedResource the resource from which the SQL statement was read\n\t * @param cause the underlying cause of the failure\n\t */",
            "\t/**\n\t * 构造新的 {@code ScriptStatementFailedException}。\n\t * @param stmt 执行失败的实际 SQL 语句\n\t * @param stmtNumber SQL 脚本中的语句序号（即资源中第 n 条语句）\n\t * @param encodedResource 读取 SQL 语句的资源\n\t * @param cause 失败的根本原因\n\t */",
        ),
        (
            "\t/**\n\t * Build an error message for an SQL script execution failure,\n\t * based on the supplied arguments.\n\t * @param stmt the actual SQL statement that failed\n\t * @param stmtNumber the statement number in the SQL script (i.e.,\n\t * the n<sup>th</sup> statement present in the resource)\n\t * @param encodedResource the resource from which the SQL statement was read\n\t * @return an error message suitable for an exception's <em>detail message</em>\n\t * or logging\n\t * @since 4.2\n\t */",
            "\t/**\n\t * 根据给定参数构建 SQL 脚本执行失败的错误消息。\n\t * @param stmt 执行失败的实际 SQL 语句\n\t * @param stmtNumber SQL 脚本中的语句序号（即资源中第 n 条语句）\n\t * @param encodedResource 读取 SQL 语句的资源\n\t * @return 适用于异常<em>详细消息</em>或日志记录的错误消息\n\t * @since 4.2\n\t */",
        ),
    ],
    "UncategorizedScriptException.java": [
        (
            "/**\n * Thrown when we cannot determine anything more specific than \"something went\n * wrong while processing an SQL script\": for example, a {@link java.sql.SQLException}\n * from JDBC that we cannot pinpoint more precisely.\n *\n * @author Sam Brannen\n * @since 4.0.3\n */",
            "/**\n * 当无法确定比“处理 SQL 脚本时出错”更具体的异常原因时抛出；\n * 例如无法精确定位的 JDBC {@link java.sql.SQLException}。\n *\n * @author Sam Brannen\n * @since 4.0.3\n */",
        ),
        (
            "\t/**\n\t * Create a new {@code UncategorizedScriptException}.\n\t * @param message detailed message\n\t */",
            "\t/**\n\t * 创建新的 {@code UncategorizedScriptException}。\n\t * @param message 详细消息\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@code UncategorizedScriptException}.\n\t * @param message detailed message\n\t * @param cause the root cause\n\t */",
            "\t/**\n\t * 创建新的 {@code UncategorizedScriptException}。\n\t * @param message 详细消息\n\t * @param cause 根本原因\n\t */",
        ),
    ],
    "DatabasePopulator.java": [
        (
            "/**\n * Strategy used to populate, initialize, or clean up a database.\n *\n * @author Keith Donald\n * @author Sam Brannen\n * @since 3.0\n * @see ResourceDatabasePopulator\n * @see DatabasePopulatorUtils\n * @see DataSourceInitializer\n */",
            "/**\n * 用于填充、初始化或清理数据库的策略接口。\n *\n * @author Keith Donald\n * @author Sam Brannen\n * @since 3.0\n * @see ResourceDatabasePopulator\n * @see DatabasePopulatorUtils\n * @see DataSourceInitializer\n */",
        ),
        (
            "\t/**\n\t * Populate, initialize, or clean up the database using the provided JDBC\n\t * connection.\n\t * <p><strong>Warning</strong>: Concrete implementations should not close\n\t * the provided {@link Connection}.\n\t * <p>Concrete implementations <em>may</em> throw an {@link SQLException} if\n\t * an error is encountered but are <em>strongly encouraged</em> to throw a\n\t * specific {@link ScriptException} instead. For example, Spring's\n\t * {@link ResourceDatabasePopulator} and {@link DatabasePopulatorUtils} wrap\n\t * all {@code SQLExceptions} in {@code ScriptExceptions}.\n\t * @param connection the JDBC connection to use; already configured and\n\t * ready to use; never {@code null}\n\t * @throws SQLException if an unrecoverable data access exception occurs\n\t * while interacting with the database\n\t * @throws ScriptException in all other error cases\n\t * @see DatabasePopulatorUtils#execute\n\t */",
            "\t/**\n\t * 使用提供的 JDBC 连接填充、初始化或清理数据库。\n\t * <p><strong>警告</strong>：具体实现不应关闭提供的 {@link Connection}。\n\t * <p>具体实现<em>可以</em>在遇到错误时抛出 {@link SQLException}，\n\t * 但<em>强烈建议</em>改为抛出具体的 {@link ScriptException}。\n\t * 例如 Spring 的 {@link ResourceDatabasePopulator} 与 {@link DatabasePopulatorUtils}\n\t * 会将所有 {@code SQLExceptions} 包装为 {@code ScriptExceptions}。\n\t * @param connection 要使用的 JDBC 连接；已配置且可直接使用；永不为 {@code null}\n\t * @throws SQLException 与数据库交互时发生不可恢复的数据访问异常\n\t * @throws ScriptException 其他所有错误情况\n\t * @see DatabasePopulatorUtils#execute\n\t */",
        ),
    ],
    "DatabasePopulatorUtils.java": [
        (
            "/**\n * Utility methods for executing a {@link DatabasePopulator}.\n *\n * @author Juergen Hoeller\n * @author Oliver Gierke\n * @author Sam Brannen\n * @since 3.1\n */",
            "/**\n * 执行 {@link DatabasePopulator} 的工具方法。\n *\n * @author Juergen Hoeller\n * @author Oliver Gierke\n * @author Sam Brannen\n * @since 3.1\n */",
        ),
        (
            "\t/**\n\t * Execute the given {@link DatabasePopulator} against the given {@link DataSource}.\n\t * <p>The {@link Connection} for the supplied {@code DataSource} will be\n\t * {@linkplain Connection#commit() committed} if it is not configured for\n\t * {@link Connection#getAutoCommit() auto-commit} and is not\n\t * {@linkplain DataSourceUtils#isConnectionTransactional transactional}.\n\t * @param populator the {@code DatabasePopulator} to execute\n\t * @param dataSource the {@code DataSource} to execute against\n\t * @throws DataAccessException if an error occurs, specifically a {@link ScriptException}\n\t * @see DataSourceUtils#isConnectionTransactional(Connection, DataSource)\n\t */",
            "\t/**\n\t * 针对给定 {@link DataSource} 执行指定的 {@link DatabasePopulator}。\n\t * <p>若所供 {@code DataSource} 的 {@link Connection} 未配置\n\t * {@link Connection#getAutoCommit() 自动提交}，且\n\t * {@linkplain DataSourceUtils#isConnectionTransactional 非事务性}，\n\t * 则会 {@linkplain Connection#commit() 提交}。\n\t * @param populator 要执行的 {@code DatabasePopulator}\n\t * @param dataSource 要执行的目标 {@code DataSource}\n\t * @throws DataAccessException 发生错误时，通常为 {@link ScriptException}\n\t * @see DataSourceUtils#isConnectionTransactional(Connection, DataSource)\n\t */",
        ),
    ],
}
