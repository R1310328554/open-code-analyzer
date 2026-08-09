"""Chinese JavaDoc replacements for springframework wave25a spring-jdbc core."""

CORE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "SqlProvider.java": [
        (
            "/**\n * Interface to be implemented by objects that can provide SQL strings.\n *\n * <p>Typically implemented by PreparedStatementCreators, CallableStatementCreators\n * and StatementCallbacks that want to expose the SQL they use to create their\n * statements, to allow for better contextual information in case of exceptions.\n *\n * @author Juergen Hoeller\n * @since 16.03.2004\n * @see PreparedStatementCreator\n * @see CallableStatementCreator\n * @see StatementCallback\n */",
            "/**\n * 由能提供 SQL 字符串的对象实现的接口。\n *\n * <p>通常由 PreparedStatementCreator、CallableStatementCreator\n * 及 StatementCallback 实现，用于暴露创建语句所用的 SQL，\n * 以便异常时提供更完整的上下文信息。\n *\n * @author Juergen Hoeller\n * @since 16.03.2004\n * @see PreparedStatementCreator\n * @see CallableStatementCreator\n * @see StatementCallback\n */",
        ),
        (
            "\t/**\n\t * Return the SQL string for this object, i.e.\n\t * typically the SQL used for creating statements.\n\t * @return the SQL string, or {@code null} if not available\n\t */",
            "\t/**\n\t * 返回本对象的 SQL 字符串，\n\t * 通常为创建语句所用的 SQL。\n\t * @return SQL 字符串，不可用则 {@code null}\n\t */",
        ),
    ],
    "SqlReturnResultSet.java": [
        (
            "/**\n * Represents a returned {@link java.sql.ResultSet} from a stored procedure call.\n *\n * <p>A {@link ResultSetExtractor}, {@link RowCallbackHandler} or {@link RowMapper}\n * must be provided to handle any returned rows.\n *\n * <p>Returned {@link java.sql.ResultSet ResultSets} - like all stored procedure\n * parameters - must have names.\n *\n * @author Thomas Risberg\n * @author Juergen Hoeller\n */",
            "/**\n * 表示存储过程调用返回的 {@link java.sql.ResultSet}。\n *\n * <p>必须提供 {@link ResultSetExtractor}、{@link RowCallbackHandler}\n * 或 {@link RowMapper} 以处理返回行。\n *\n * <p>返回的 {@link java.sql.ResultSet ResultSets} 与所有存储过程参数一样\n * 必须具名。\n *\n * @author Thomas Risberg\n * @author Juergen Hoeller\n */",
        ),
        (
            "\t/**\n\t * Create a new instance of the {@link SqlReturnResultSet} class.\n\t * @param name the name of the parameter, as used in input and output maps\n\t * @param extractor the {@link ResultSetExtractor} to use for parsing the {@link java.sql.ResultSet}\n\t */",
            "\t/**\n\t * 创建 {@link SqlReturnResultSet} 新实例。\n\t * @param name 参数名，用于输入/输出映射\n\t * @param extractor 解析 {@link java.sql.ResultSet} 的 {@link ResultSetExtractor}\n\t */",
        ),
        (
            "\t/**\n\t * Create a new instance of the {@link SqlReturnResultSet} class.\n\t * @param name the name of the parameter, as used in input and output maps\n\t * @param handler the {@link RowCallbackHandler} to use for parsing the {@link java.sql.ResultSet}\n\t */",
            "\t/**\n\t * 创建 {@link SqlReturnResultSet} 新实例。\n\t * @param name 参数名，用于输入/输出映射\n\t * @param handler 解析 {@link java.sql.ResultSet} 的 {@link RowCallbackHandler}\n\t */",
        ),
        (
            "\t/**\n\t * Create a new instance of the {@link SqlReturnResultSet} class.\n\t * @param name the name of the parameter, as used in input and output maps\n\t * @param mapper the {@link RowMapper} to use for parsing the {@link java.sql.ResultSet}\n\t */",
            "\t/**\n\t * 创建 {@link SqlReturnResultSet} 新实例。\n\t * @param name 参数名，用于输入/输出映射\n\t * @param mapper 解析 {@link java.sql.ResultSet} 的 {@link RowMapper}\n\t */",
        ),
        (
            "\t/**\n\t * This implementation always returns {@code true}.\n\t */",
            "\t/**\n\t * 本实现始终返回 {@code true}。\n\t */",
        ),
    ],
    "SqlReturnType.java": [
        (
            "/**\n * Interface to be implemented for retrieving values for more complex database-specific\n * types not supported by the standard {@code CallableStatement.getObject} method.\n *\n * <p>Implementations perform the actual work of getting the actual values. They must\n * implement the callback method {@code getTypeValue} which can throw SQLExceptions\n * that will be caught and translated by the calling code. This callback method has\n * access to the underlying Connection via the given CallableStatement object, if that\n * should be needed to create any database-specific objects.\n *\n * @author Thomas Risberg\n * @since 1.1\n * @see java.sql.Types\n * @see java.sql.CallableStatement#getObject\n * @see org.springframework.jdbc.object.StoredProcedure#execute(java.util.Map)\n */",
            "/**\n * 用于读取标准 {@code CallableStatement.getObject} 不支持的\n * 复杂数据库特定类型值的接口。\n *\n * <p>实现类负责实际取值，须实现回调方法 {@code getTypeValue}；\n * 该方法可能抛出 SQLException，由调用方捕获并翻译。\n * 若需创建数据库特定对象，可通过给定 CallableStatement 访问底层 Connection。\n *\n * @author Thomas Risberg\n * @since 1.1\n * @see java.sql.Types\n * @see java.sql.CallableStatement#getObject\n * @see org.springframework.jdbc.object.StoredProcedure#execute(java.util.Map)\n */",
        ),
        (
            "\t/**\n\t * Constant that indicates an unknown (or unspecified) SQL type.\n\t * Passed into setTypeValue if the original operation method does\n\t * not specify an SQL type.\n\t * @see java.sql.Types\n\t * @see JdbcOperations#update(String, Object[])\n\t */",
            "\t/**\n\t * 表示未知（或未指定）SQL 类型的常量。\n\t * 原始操作方法未指定 SQL 类型时传入 setTypeValue。\n\t * @see java.sql.Types\n\t * @see JdbcOperations#update(String, Object[])\n\t */",
        ),
        (
            "\t/**\n\t * Get the type value from the specific object.\n\t * @param cs the CallableStatement to operate on\n\t * @param paramIndex the index of the parameter for which we need to set the value\n\t * @param sqlType the SQL type of the parameter we are setting\n\t * @param typeName the type name of the parameter (optional)\n\t * @return the target value\n\t * @throws SQLException if an SQLException is encountered setting parameter values\n\t * (that is, there's no need to catch SQLException)\n\t * @see java.sql.Types\n\t * @see java.sql.CallableStatement#getObject\n\t */",
            "\t/**\n\t * 从特定对象获取类型值。\n\t * @param cs 要操作的 CallableStatement\n\t * @param paramIndex 待设值参数的索引\n\t * @param sqlType 参数的 SQL 类型\n\t * @param typeName 参数类型名（可选）\n\t * @return 目标值\n\t * @throws SQLException 设参时遇到 SQLException（无需自行捕获）\n\t * @see java.sql.Types\n\t * @see java.sql.CallableStatement#getObject\n\t */",
        ),
    ],
    "SqlReturnUpdateCount.java": [
        (
            "/**\n * Represents a returned update count from a stored procedure call.\n *\n * <p>Returned update counts - like all stored procedure\n * parameters - <b>must</b> have names.\n *\n * @author Thomas Risberg\n */",
            "/**\n * 表示存储过程调用返回的更新计数。\n *\n * <p>返回的更新计数与所有存储过程参数一样<b>必须</b>具名。\n *\n * @author Thomas Risberg\n */",
        ),
        (
            "\t/**\n\t * Create a new SqlReturnUpdateCount.\n\t * @param name the name of the parameter, as used in input and output maps\n\t */",
            "\t/**\n\t * 创建新的 SqlReturnUpdateCount。\n\t * @param name 参数名，用于输入/输出映射\n\t */",
        ),
        (
            "\t/**\n\t * This implementation always returns {@code false}.\n\t */",
            "\t/**\n\t * 本实现始终返回 {@code false}。\n\t */",
        ),
        (
            "\t/**\n\t * This implementation always returns {@code true}.\n\t */",
            "\t/**\n\t * 本实现始终返回 {@code true}。\n\t */",
        ),
    ],
    "SqlRowSetResultSetExtractor.java": [
        (
            "/**\n * {@link ResultSetExtractor} implementation that returns a Spring {@link SqlRowSet}\n * representation for each given {@link ResultSet}.\n *\n * <p>The default implementation uses a standard JDBC CachedRowSet underneath.\n *\n * @author Juergen Hoeller\n * @since 1.2\n * @see #newCachedRowSet\n * @see org.springframework.jdbc.support.rowset.SqlRowSet\n * @see JdbcTemplate#queryForRowSet(String)\n * @see javax.sql.rowset.CachedRowSet\n */",
            "/**\n * 为每个给定 {@link ResultSet} 返回 Spring {@link SqlRowSet} 表示的\n * {@link ResultSetExtractor} 实现。\n *\n * <p>默认实现底层使用标准 JDBC CachedRowSet。\n *\n * @author Juergen Hoeller\n * @since 1.2\n * @see #newCachedRowSet\n * @see org.springframework.jdbc.support.rowset.SqlRowSet\n * @see JdbcTemplate#queryForRowSet(String)\n * @see javax.sql.rowset.CachedRowSet\n */",
        ),
        (
            "\t/**\n\t * Create a {@link SqlRowSet} that wraps the given {@link ResultSet},\n\t * representing its data in a disconnected fashion.\n\t * <p>This implementation creates a Spring {@link ResultSetWrappingSqlRowSet}\n\t * instance that wraps a standard JDBC {@link CachedRowSet} instance.\n\t * Can be overridden to use a different implementation.\n\t * @param rs the original ResultSet (connected)\n\t * @return the disconnected SqlRowSet\n\t * @throws SQLException if thrown by JDBC methods\n\t * @see #newCachedRowSet()\n\t * @see org.springframework.jdbc.support.rowset.ResultSetWrappingSqlRowSet\n\t */",
            "\t/**\n\t * 创建包装给定 {@link ResultSet} 的 {@link SqlRowSet}，\n\t * 以断开连接方式表示其数据。\n\t * <p>本实现创建 Spring {@link ResultSetWrappingSqlRowSet}，\n\t * 包装标准 JDBC {@link CachedRowSet}；可覆盖以使用其他实现。\n\t * @param rs 原始 ResultSet（已连接）\n\t * @return 断开连接的 SqlRowSet\n\t * @throws SQLException JDBC 方法抛出时\n\t * @see #newCachedRowSet()\n\t * @see org.springframework.jdbc.support.rowset.ResultSetWrappingSqlRowSet\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@link CachedRowSet} instance, to be populated by\n\t * the {@code createSqlRowSet} implementation.\n\t * <p>The default implementation uses JDBC's {@link RowSetFactory}.\n\t * @return a new CachedRowSet instance\n\t * @throws SQLException if thrown by JDBC methods\n\t * @see #createSqlRowSet\n\t * @see RowSetProvider#newFactory()\n\t * @see RowSetFactory#createCachedRowSet()\n\t */",
            "\t/**\n\t * 创建新的 {@link CachedRowSet} 实例，供 {@code createSqlRowSet} 填充。\n\t * <p>默认实现使用 JDBC 的 {@link RowSetFactory}。\n\t * @return 新的 CachedRowSet 实例\n\t * @throws SQLException JDBC 方法抛出时\n\t * @see #createSqlRowSet\n\t * @see RowSetProvider#newFactory()\n\t * @see RowSetFactory#createCachedRowSet()\n\t */",
        ),
    ],
    "SqlTypeValue.java": [
        (
            "/**\n * Interface to be implemented for setting values for more complex database-specific\n * types not supported by the standard {@code setObject} method. This is\n * effectively an extended variant of {@link org.springframework.jdbc.support.SqlValue}.\n *\n * <p>Implementations perform the actual work of setting the actual values. They must\n * implement the callback method {@code setTypeValue} which can throw SQLExceptions\n * that will be caught and translated by the calling code. This callback method has\n * access to the underlying Connection via the given PreparedStatement object, if that\n * should be needed to create any database-specific objects.\n *\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @since 1.1\n * @see java.sql.Types\n * @see java.sql.PreparedStatement#setObject\n * @see JdbcOperations#update(String, Object[], int[])\n * @see org.springframework.jdbc.support.SqlValue\n */",
            "/**\n * 用于设置标准 {@code setObject} 不支持的复杂数据库特定类型值的接口。\n * 可视为 {@link org.springframework.jdbc.support.SqlValue} 的扩展变体。\n *\n * <p>实现类负责实际设值，须实现回调方法 {@code setTypeValue}；\n * 该方法可能抛出 SQLException，由调用方捕获并翻译。\n * 若需创建数据库特定对象，可通过给定 PreparedStatement 访问底层 Connection。\n *\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @since 1.1\n * @see java.sql.Types\n * @see java.sql.PreparedStatement#setObject\n * @see JdbcOperations#update(String, Object[], int[])\n * @see org.springframework.jdbc.support.SqlValue\n */",
        ),
        (
            "\t/**\n\t * Constant that indicates an unknown (or unspecified) SQL type.\n\t * Passed into {@code setTypeValue} if the original operation method\n\t * does not specify an SQL type.\n\t * @see java.sql.Types\n\t * @see JdbcOperations#update(String, Object[])\n\t */",
            "\t/**\n\t * 表示未知（或未指定）SQL 类型的常量。\n\t * 原始操作方法未指定 SQL 类型时传入 {@code setTypeValue}。\n\t * @see java.sql.Types\n\t * @see JdbcOperations#update(String, Object[])\n\t */",
        ),
        (
            "\t/**\n\t * Set the type value on the given PreparedStatement.\n\t * @param ps the PreparedStatement to work on\n\t * @param paramIndex the index of the parameter for which we need to set the value\n\t * @param sqlType the SQL type of the parameter we are setting\n\t * @param typeName the type name of the parameter (optional)\n\t * @throws SQLException if an SQLException is encountered while setting parameter values\n\t * @see java.sql.Types\n\t * @see java.sql.PreparedStatement#setObject\n\t */",
            "\t/**\n\t * 在给定 PreparedStatement 上设置类型值。\n\t * @param ps 要操作的 PreparedStatement\n\t * @param paramIndex 待设值参数的索引\n\t * @param sqlType 参数的 SQL 类型\n\t * @param typeName 参数类型名（可选）\n\t * @throws SQLException 设参时遇到 SQLException\n\t * @see java.sql.Types\n\t * @see java.sql.PreparedStatement#setObject\n\t */",
        ),
    ],
    "StatementCallback.java": [
        (
            "/**\n * Generic callback interface for code that operates on a JDBC Statement.\n * Allows to execute any number of operations on a single Statement,\n * for example a single {@code executeUpdate} call or repeated\n * {@code executeUpdate} calls with varying SQL.\n *\n * <p>Used internally by JdbcTemplate, but also useful for application code.\n *\n * @author Juergen Hoeller\n * @since 16.03.2004\n * @param <T> the result type\n * @see JdbcTemplate#execute(StatementCallback)\n */",
            "/**\n * 在 JDBC Statement 上执行代码的通用回调接口。\n * 可在单个 Statement 上执行任意次操作，\n * 例如单次 {@code executeUpdate} 或 SQL 不同的多次 {@code executeUpdate}。\n *\n * <p>JdbcTemplate 内部使用，应用代码亦可用。\n *\n * @author Juergen Hoeller\n * @since 16.03.2004\n * @param <T> 结果类型\n * @see JdbcTemplate#execute(StatementCallback)\n */",
        ),
        (
            "\t/**\n\t * Gets called by {@code JdbcTemplate.execute} with an active JDBC\n\t * Statement. Does not need to care about closing the Statement or the\n\t * Connection, or about handling transactions: this will all be handled\n\t * by Spring's JdbcTemplate.\n\t * <p><b>NOTE:</b> Any ResultSets opened should be closed in finally blocks\n\t * within the callback implementation. Spring will close the Statement\n\t * object after the callback returned, but this does not necessarily imply\n\t * that the ResultSet resources will be closed: the Statement objects might\n\t * get pooled by the connection pool, with {@code close} calls only\n\t * returning the object to the pool but not physically closing the resources.\n\t * <p>If called without a thread-bound JDBC transaction (initiated by\n\t * DataSourceTransactionManager), the code will simply get executed on the\n\t * JDBC connection with its transactional semantics. If JdbcTemplate is\n\t * configured to use a JTA-aware DataSource, the JDBC connection and thus\n\t * the callback code will be transactional if a JTA transaction is active.\n\t * <p>Allows for returning a result object created within the callback, i.e.\n\t * a domain object or a collection of domain objects. Note that there's\n\t * special support for single step actions: see JdbcTemplate.queryForObject etc.\n\t * A thrown RuntimeException is treated as application exception, it gets\n\t * propagated to the caller of the template.\n\t * @param stmt active JDBC Statement\n\t * @return a result object, or {@code null} if none\n\t * @throws SQLException if thrown by a JDBC method, to be auto-converted\n\t * to a DataAccessException by an SQLExceptionTranslator\n\t * @throws DataAccessException in case of custom exceptions\n\t * @see JdbcTemplate#queryForObject(String, Class)\n\t * @see JdbcTemplate#queryForRowSet(String)\n\t */",
            "\t/**\n\t * 由 {@code JdbcTemplate.execute} 以活动 JDBC Statement 调用。\n\t * 无需关心关闭 Statement 或 Connection，亦无需处理事务；\n\t * 均由 Spring JdbcTemplate 负责。\n\t * <p><b>注意：</b>打开的 ResultSet 应在回调实现的 finally 块中关闭。\n\t * Spring 在回调返回后关闭 Statement，但不保证 ResultSet 资源已释放：\n\t * Statement 可能被连接池复用，{@code close} 仅归还池而非物理关闭。\n\t * <p>若无线程绑定 JDBC 事务（由 DataSourceTransactionManager 启动），\n\t * 代码将按 JDBC 连接自身语义执行。若 JdbcTemplate 使用 JTA 感知 DataSource，\n\t * 且 JTA 事务活动，则 JDBC 连接及回调代码亦具事务性。\n\t * <p>可返回回调内创建的结果对象，如领域对象或其集合。\n\t * 单步操作有专门支持：见 JdbcTemplate.queryForObject 等。\n\t * 抛出的 RuntimeException 视为应用异常，传播给模板调用方。\n\t * @param stmt 活动 JDBC Statement\n\t * @return 结果对象，无则 {@code null}\n\t * @throws SQLException JDBC 方法抛出时，由 SQLExceptionTranslator 转为 DataAccessException\n\t * @throws DataAccessException 自定义异常时\n\t * @see JdbcTemplate#queryForObject(String, Class)\n\t * @see JdbcTemplate#queryForRowSet(String)\n\t */",
        ),
    ],
}
