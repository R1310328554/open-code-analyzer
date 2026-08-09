"""Chinese JavaDoc replacements for springframework wave24a spring-jdbc core (part A)."""

CORE_A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "AggregatedBatchUpdateException.java": [
        (
            "/**\n * A {@link BatchUpdateException} that provides additional information about\n * batches that were successful prior to one failing.\n *\n * @author Stephane Nicoll\n * @since 6.2\n */",
            "/**\n * 提供失败前已成功批次额外信息的 {@link BatchUpdateException}。\n *\n * @author Stephane Nicoll\n * @since 6.2\n */",
        ),
        (
            "\t/**\n\t * Create an aggregated exception with the batches that have completed prior\n\t * to the given {@code cause}.\n\t * @param successfulUpdateCounts the counts of the batches that run successfully\n\t * @param original the exception this instance aggregates\n\t */",
            "\t/**\n\t * 创建聚合异常，包含给定 {@code cause} 之前已完成的批次。\n\t * @param successfulUpdateCounts 成功批次的更新计数\n\t * @param original 本实例聚合的原始异常\n\t */",
        ),
        (
            "\t/**\n\t * Return the batches that have completed successfully, prior to this exception.\n\t * <p>Information about the batch that failed is available via\n\t * {@link #getUpdateCounts()}.\n\t * @return an array containing for each batch another array containing the numbers of\n\t * rows affected by each update in the batch\n\t * @see #getUpdateCounts()\n\t */",
            "\t/**\n\t * 返回本异常之前已成功完成的批次。\n\t * <p>失败批次信息可通过 {@link #getUpdateCounts()} 获取。\n\t * @return 数组：每个批次对应一个子数组，表示该批次各更新受影响行数\n\t * @see #getUpdateCounts()\n\t */",
        ),
        (
            "\t/**\n\t * Return the original {@link BatchUpdateException} that this exception aggregates.\n\t * @return the original exception\n\t */",
            "\t/**\n\t * 返回本异常聚合的原始 {@link BatchUpdateException}。\n\t * @return 原始异常\n\t */",
        ),
    ],
    "ArgumentPreparedStatementSetter.java": [
        (
            "/**\n * Simple adapter for {@link PreparedStatementSetter} that applies a given array\n * of arguments.\n *\n * @author Juergen Hoeller\n * @since 3.2.3\n */",
            "/**\n * 将给定参数数组应用到 {@link PreparedStatementSetter} 的简单适配器。\n *\n * @author Juergen Hoeller\n * @since 3.2.3\n */",
        ),
        (
            "\t/**\n\t * Create a new {@code ArgumentPreparedStatementSetter} for the given arguments.\n\t * @param args the arguments to set\n\t */",
            "\t/**\n\t * 为给定参数创建 {@code ArgumentPreparedStatementSetter}。\n\t * @param args 要设置的参数\n\t */",
        ),
        (
            "\t/**\n\t * Set the value for the prepared statement's specified parameter position\n\t * using the supplied value.\n\t * <p>This method can be overridden by subclasses if needed.\n\t * @param ps the PreparedStatement\n\t * @param parameterPosition index of the parameter position\n\t * @param argValue the value to set\n\t * @throws SQLException if thrown by PreparedStatement methods\n\t */",
            "\t/**\n\t * 使用给定值为 PreparedStatement 指定参数位置设值。\n\t * <p>子类可按需覆盖。\n\t * @param ps PreparedStatement\n\t * @param parameterPosition 参数位置索引\n\t * @param argValue 要设置的值\n\t * @throws SQLException 若 PreparedStatement 方法抛出\n\t */",
        ),
    ],
    "ArgumentTypePreparedStatementSetter.java": [
        (
            "/**\n * Simple adapter for {@link PreparedStatementSetter} that applies the given\n * arrays of arguments and JDBC argument types.\n *\n * @author Juergen Hoeller\n * @since 3.2.3\n */",
            "/**\n * 将给定参数数组及 JDBC 参数类型应用到\n * {@link PreparedStatementSetter} 的简单适配器。\n *\n * @author Juergen Hoeller\n * @since 3.2.3\n */",
        ),
        (
            "\t/**\n\t * Create a new {@code ArgumentTypePreparedStatementSetter} for the given\n\t * arguments and types.\n\t * @param args the arguments to set\n\t * @param argTypes the corresponding SQL types of the arguments\n\t */",
            "\t/**\n\t * 为给定参数及类型创建 {@code ArgumentTypePreparedStatementSetter}。\n\t * @param args 要设置的参数\n\t * @param argTypes 参数对应的 SQL 类型\n\t */",
        ),
        (
            "\t/**\n\t * Set the value for the prepared statement's specified parameter position\n\t * using the supplied value and type.\n\t * <p>This method can be overridden by subclasses if needed.\n\t * @param ps the PreparedStatement\n\t * @param parameterPosition index of the parameter position\n\t * @param argType the argument type\n\t * @param argValue the argument value\n\t * @throws SQLException if thrown by PreparedStatement methods\n\t */",
            "\t/**\n\t * 使用给定值和类型为 PreparedStatement 指定参数位置设值。\n\t * <p>子类可按需覆盖。\n\t * @param ps PreparedStatement\n\t * @param parameterPosition 参数位置索引\n\t * @param argType 参数类型\n\t * @param argValue 参数值\n\t * @throws SQLException 若 PreparedStatement 方法抛出\n\t */",
        ),
    ],
    "BatchPreparedStatementSetter.java": [
        (
            "/**\n * Batch update callback interface used by the {@link JdbcTemplate} class.\n *\n * <p>This interface sets values on a {@link java.sql.PreparedStatement} provided\n * by the JdbcTemplate class, for each of a number of updates in a batch using the\n * same SQL. Implementations are responsible for setting any necessary parameters.\n * SQL with placeholders will already have been supplied.\n *\n * <p>Implementations <i>do not</i> need to concern themselves with SQLExceptions\n * that may be thrown from operations they attempt. The JdbcTemplate class will\n * catch and handle SQLExceptions appropriately.\n *\n * @author Rod Johnson\n * @since March 2, 2003\n * @see JdbcTemplate#batchUpdate(String, BatchPreparedStatementSetter)\n * @see InterruptibleBatchPreparedStatementSetter\n */",
            "/**\n * {@link JdbcTemplate} 使用的批量更新回调接口。\n *\n * <p>对同一 SQL 的批量更新中，为 JdbcTemplate 提供的\n * {@link java.sql.PreparedStatement} 逐条设值；实现类负责设置必要参数。\n * 带占位符的 SQL 已由框架提供。\n *\n * <p>实现类<i>无需</i>处理操作中可能抛出的 SQLException；\n * JdbcTemplate 会适当捕获并处理。\n *\n * @author Rod Johnson\n * @since March 2, 2003\n * @see JdbcTemplate#batchUpdate(String, BatchPreparedStatementSetter)\n * @see InterruptibleBatchPreparedStatementSetter\n */",
        ),
        (
            "\t/**\n\t * Set parameter values on the given PreparedStatement.\n\t * @param ps the PreparedStatement to invoke setter methods on\n\t * @param i index of the statement we're issuing in the batch, starting from 0\n\t * @throws SQLException if an SQLException is encountered\n\t * (i.e. there is no need to catch SQLException)\n\t */",
            "\t/**\n\t * 为给定 PreparedStatement 设置参数值。\n\t * @param ps 要调用 setter 的 PreparedStatement\n\t * @param i 批量中当前语句索引，从 0 起\n\t * @throws SQLException 若遇到 SQLException（无需自行捕获）\n\t */",
        ),
        (
            "\t/**\n\t * Return the size of the batch.\n\t * @return the number of statements in the batch\n\t */",
            "\t/**\n\t * 返回批量大小。\n\t * @return 批量中语句数量\n\t */",
        ),
    ],
    "CallableStatementCallback.java": [
        (
            "/**\n * Generic callback interface for code that operates on a CallableStatement.\n * Allows to execute any number of operations on a single CallableStatement,\n * for example a single execute call or repeated execute calls with varying\n * parameters.\n *\n * <p>Used internally by JdbcTemplate, but also useful for application code.\n * Note that the passed-in CallableStatement can have been created by the\n * framework or by a custom CallableStatementCreator. However, the latter is\n * hardly ever necessary, as most custom callback actions will perform updates\n * in which case a standard CallableStatement is fine. Custom actions will\n * always set parameter values themselves, so that CallableStatementCreator\n * capability is not needed either.\n *\n * @author Juergen Hoeller\n * @since 16.03.2004\n * @param <T> the result type\n * @see JdbcTemplate#execute(String, CallableStatementCallback)\n * @see JdbcTemplate#execute(CallableStatementCreator, CallableStatementCallback)\n */",
            "/**\n * 在 CallableStatement 上执行代码的通用回调接口。\n * 可在单个 CallableStatement 上执行任意次操作，\n * 例如单次 execute 或参数不同的多次 execute。\n *\n * <p>JdbcTemplate 内部使用，应用代码亦可用。\n * 传入的 CallableStatement 可由框架或自定义 CallableStatementCreator 创建；\n * 后者通常不必，因多数自定义回调只需标准 CallableStatement。\n * 自定义回调会自行设参，故亦无需 CallableStatementCreator。\n *\n * @author Juergen Hoeller\n * @since 16.03.2004\n * @param <T> 结果类型\n * @see JdbcTemplate#execute(String, CallableStatementCallback)\n * @see JdbcTemplate#execute(CallableStatementCreator, CallableStatementCallback)\n */",
        ),
        (
            "\t/**\n\t * Gets called by {@code JdbcTemplate.execute} with an active JDBC\n\t * CallableStatement. Does not need to care about closing the Statement\n\t * or the Connection, or about handling transactions: this will all be\n\t * handled by Spring's JdbcTemplate.\n\t *\n\t * <p><b>NOTE:</b> Any ResultSets opened should be closed in finally blocks\n\t * within the callback implementation. Spring will close the Statement\n\t * object after the callback returned, but this does not necessarily imply\n\t * that the ResultSet resources will be closed: the Statement objects might\n\t * get pooled by the connection pool, with {@code close} calls only\n\t * returning the object to the pool but not physically closing the resources.\n\t *\n\t * <p>If called without a thread-bound JDBC transaction (initiated by\n\t * DataSourceTransactionManager), the code will simply get executed on the\n\t * JDBC connection with its transactional semantics. If JdbcTemplate is\n\t * configured to use a JTA-aware DataSource, the JDBC connection and thus\n\t * the callback code will be transactional if a JTA transaction is active.\n\t *\n\t * <p>Allows for returning a result object created within the callback, i.e.\n\t * a domain object or a collection of domain objects. A thrown RuntimeException\n\t * is treated as application exception: it gets propagated to the caller of\n\t * the template.\n\t * @param cs active JDBC CallableStatement\n\t * @return a result object, or {@code null} if none\n\t * @throws SQLException if thrown by a JDBC method, to be auto-converted\n\t * into a DataAccessException by an SQLExceptionTranslator\n\t * @throws DataAccessException in case of custom exceptions\n\t */",
            "\t/**\n\t * 由 {@code JdbcTemplate.execute} 以活动 JDBC CallableStatement 调用。\n\t * 无需关心关闭 Statement 或 Connection，亦无需处理事务；\n\t * 均由 Spring JdbcTemplate 负责。\n\t *\n\t * <p><b>注意：</b>打开的 ResultSet 应在回调实现的 finally 块中关闭。\n\t * Spring 在回调返回后关闭 Statement，但不保证 ResultSet 资源已释放：\n\t * Statement 可能被连接池复用，{@code close} 仅归还池而非物理关闭。\n\t *\n\t * <p>若无线程绑定 JDBC 事务（由 DataSourceTransactionManager 启动），\n\t * 代码将按 JDBC 连接自身语义执行。若 JdbcTemplate 使用 JTA 感知 DataSource，\n\t * 且 JTA 事务活动，则 JDBC 连接及回调代码亦具事务性。\n\t *\n\t * <p>可返回回调内创建的结果对象，如领域对象或其集合。\n\t * 抛出的 RuntimeException 视为应用异常，传播给模板调用方。\n\t * @param cs 活动 JDBC CallableStatement\n\t * @return 结果对象，无则 {@code null}\n\t * @throws SQLException JDBC 方法抛出时，由 SQLExceptionTranslator 转为 DataAccessException\n\t * @throws DataAccessException 自定义异常时\n\t */",
        ),
    ],
    "CallableStatementCreator.java": [
        (
            "/**\n * One of the three central callback interfaces used by the JdbcTemplate class.\n * This interface creates a CallableStatement given a connection, provided\n * by the JdbcTemplate class. Implementations are responsible for providing\n * SQL and any necessary parameters.\n *\n * <p>Implementations <i>do not</i> need to concern themselves with\n * SQLExceptions that may be thrown from operations they attempt.\n * The JdbcTemplate class will catch and handle SQLExceptions appropriately.\n *\n * <p>A PreparedStatementCreator should also implement the SqlProvider interface\n * if it is able to provide the SQL it uses for PreparedStatement creation.\n * This allows for better contextual information in case of exceptions.\n *\n * @author Rod Johnson\n * @author Thomas Risberg\n * @see JdbcTemplate#execute(CallableStatementCreator, CallableStatementCallback)\n * @see JdbcTemplate#call\n * @see SqlProvider\n */",
            "/**\n * JdbcTemplate 使用的三个核心回调接口之一。\n * 由 JdbcTemplate 提供 Connection，本接口负责创建 CallableStatement；\n * 实现类需提供 SQL 及必要参数。\n *\n * <p>实现类<i>无需</i>处理操作中可能抛出的 SQLException；\n * JdbcTemplate 会适当捕获并处理。\n *\n * <p>若能为 CallableStatement 创建提供所用 SQL，\n * 还应实现 SqlProvider，以便异常时提供更完整上下文。\n *\n * @author Rod Johnson\n * @author Thomas Risberg\n * @see JdbcTemplate#execute(CallableStatementCreator, CallableStatementCallback)\n * @see JdbcTemplate#call\n * @see SqlProvider\n */",
        ),
        (
            "\t/**\n\t * Create a callable statement in this connection. Allows implementations to use\n\t * CallableStatements.\n\t * @param con the Connection to use to create statement\n\t * @return a callable statement\n\t * @throws SQLException there is no need to catch SQLExceptions\n\t * that may be thrown in the implementation of this method.\n\t * The JdbcTemplate class will handle them.\n\t */",
            "\t/**\n\t * 在此连接上创建 CallableStatement。\n\t * @param con 用于创建语句的 Connection\n\t * @return CallableStatement\n\t * @throws SQLException 实现中可能抛出，无需自行捕获；JdbcTemplate 会处理\n\t */",
        ),
    ],
}
