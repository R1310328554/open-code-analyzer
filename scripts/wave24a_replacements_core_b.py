"""Chinese JavaDoc replacements for springframework wave24a spring-jdbc core (part B)."""

CORE_B_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "CallableStatementCreatorFactory.java": [
        (
            "/**\n * Helper class that efficiently creates multiple {@link CallableStatementCreator}\n * objects with different parameters based on an SQL statement and a single\n * set of parameter declarations.\n *\n * @author Rod Johnson\n * @author Thomas Risberg\n * @author Juergen Hoeller\n */",
            "/**\n * 基于 SQL 语句与一组参数声明，高效创建带不同参数的\n * 多个 {@link CallableStatementCreator} 的辅助类。\n *\n * @author Rod Johnson\n * @author Thomas Risberg\n * @author Juergen Hoeller\n */",
        ),
        (
            "\t/** The SQL call string, which won't change when the parameters change. */",
            "\t/** SQL 调用字符串；参数变化时不变。 */",
        ),
        (
            "\t/** List of SqlParameter objects. May not be {@code null}. */",
            "\t/** SqlParameter 列表；不可为 {@code null}。 */",
        ),
        (
            "\t/**\n\t * Create a new factory. Will need to add parameters via the\n\t * {@link #addParameter} method or have no parameters.\n\t * @param callString the SQL call string\n\t */",
            "\t/**\n\t * 创建新工厂。需通过 {@link #addParameter} 添加参数，或无参数。\n\t * @param callString SQL 调用字符串\n\t */",
        ),
        (
            "\t/**\n\t * Create a new factory with the given SQL and the given parameters.\n\t * @param callString the SQL call string\n\t * @param declaredParameters list of {@link SqlParameter} objects\n\t */",
            "\t/**\n\t * 用给定 SQL 与参数创建新工厂。\n\t * @param callString SQL 调用字符串\n\t * @param declaredParameters {@link SqlParameter} 列表\n\t */",
        ),
        (
            "\t/**\n\t * Return the SQL call string.\n\t * @since 5.1.3\n\t */",
            "\t/**\n\t * 返回 SQL 调用字符串。\n\t * @since 5.1.3\n\t */",
        ),
        (
            "\t/**\n\t * Add a new declared parameter.\n\t * <p>Order of parameter addition is significant.\n\t * @param param the parameter to add to the list of declared parameters\n\t */",
            "\t/**\n\t * 添加新的声明参数。\n\t * <p>添加顺序有意义。\n\t * @param param 要加入声明参数列表的参数\n\t */",
        ),
        (
            "\t/**\n\t * Set whether to use prepared statements that return a specific type of ResultSet.\n\t * specific type of ResultSet.\n\t * @param resultSetType the ResultSet type\n\t * @see java.sql.ResultSet#TYPE_FORWARD_ONLY\n\t * @see java.sql.ResultSet#TYPE_SCROLL_INSENSITIVE\n\t * @see java.sql.ResultSet#TYPE_SCROLL_SENSITIVE\n\t */",
            "\t/**\n\t * 设置是否使用返回特定 ResultSet 类型的 PreparedStatement。\n\t * @param resultSetType ResultSet 类型\n\t * @see java.sql.ResultSet#TYPE_FORWARD_ONLY\n\t * @see java.sql.ResultSet#TYPE_SCROLL_INSENSITIVE\n\t * @see java.sql.ResultSet#TYPE_SCROLL_SENSITIVE\n\t */",
        ),
        (
            "\t/**\n\t * Set whether to use prepared statements capable of returning updatable ResultSets.\n\t */",
            "\t/**\n\t * 设置是否使用可返回可更新 ResultSet 的 PreparedStatement。\n\t */",
        ),
        (
            "\t/**\n\t * Return a new CallableStatementCreator instance given these parameters.\n\t * @param params list of parameters (may be {@code null})\n\t */",
            "\t/**\n\t * 根据给定参数返回新的 CallableStatementCreator 实例。\n\t * @param params 参数列表（可为 {@code null}）\n\t */",
        ),
        (
            "\t/**\n\t * Return a new CallableStatementCreator instance given this parameter mapper.\n\t * @param inParamMapper the ParameterMapper implementation that will return a Map of parameters\n\t */",
            "\t/**\n\t * 根据给定 ParameterMapper 返回新的 CallableStatementCreator 实例。\n\t * @param inParamMapper 返回参数 Map 的 ParameterMapper 实现\n\t */",
        ),
        (
            "\t/**\n\t * CallableStatementCreator implementation returned by this class.\n\t */",
            "\t/**\n\t * 本类返回的 CallableStatementCreator 实现。\n\t */",
        ),
        (
            "\t\t/**\n\t\t * Create a new CallableStatementCreatorImpl.\n\t\t * @param inParamMapper the ParameterMapper implementation for mapping input parameters\n\t\t */",
            "\t\t/**\n\t\t * 创建 CallableStatementCreatorImpl。\n\t\t * @param inParamMapper 映射输入参数的 ParameterMapper 实现\n\t\t */",
        ),
        (
            "\t\t/**\n\t\t * Create a new CallableStatementCreatorImpl.\n\t\t * @param inParams list of SqlParameter objects\n\t\t */",
            "\t\t/**\n\t\t * 创建 CallableStatementCreatorImpl。\n\t\t * @param inParams SqlParameter 对象列表\n\t\t */",
        ),
    ],
    "ColumnMapRowMapper.java": [
        (
            "/**\n * {@link RowMapper} implementation that creates a {@code java.util.Map}\n * for each row, representing all columns as key-value pairs: one\n * entry for each column, with the column name as key.\n *\n * <p>The Map implementation to use and the key to use for each column\n * in the column Map can be customized by overriding {@link #createColumnMap}\n * and {@link #getColumnKey}, respectively.\n *\n * <p><b>Note:</b> By default, {@code ColumnMapRowMapper} will try to build a linked Map\n * with case-insensitive keys, to preserve column order as well as allow any\n * casing to be used for column names.\n *\n * @author Juergen Hoeller\n * @since 1.2\n * @see JdbcTemplate#queryForList(String)\n * @see JdbcTemplate#queryForMap(String)\n */",
            "/**\n * 为每行创建 {@code java.util.Map} 的 {@link RowMapper} 实现，\n * 以列名为键、列值为值表示所有列。\n *\n * <p>可通过覆盖 {@link #createColumnMap} 与 {@link #getColumnKey}\n * 分别自定义 Map 实现及列键。\n *\n * <p><b>注意：</b>默认 {@code ColumnMapRowMapper} 构建键不区分大小写的\n * 链接 Map，既保留列顺序又允许任意大小写列名。\n *\n * @author Juergen Hoeller\n * @since 1.2\n * @see JdbcTemplate#queryForList(String)\n * @see JdbcTemplate#queryForMap(String)\n */",
        ),
        (
            "\t/**\n\t * Create a Map instance to be used as column map.\n\t * <p>By default, a linked case-insensitive Map will be created.\n\t * @param columnCount the column count, to be used as initial\n\t * capacity for the Map\n\t * @return the new Map instance\n\t * @see org.springframework.util.LinkedCaseInsensitiveMap\n\t */",
            "\t/**\n\t * 创建用作列 Map 的 Map 实例。\n\t * <p>默认创建链接且不区分大小写的 Map。\n\t * @param columnCount 列数，用作 Map 初始容量\n\t * @return 新 Map 实例\n\t * @see org.springframework.util.LinkedCaseInsensitiveMap\n\t */",
        ),
        (
            "\t/**\n\t * Determine the key to use for the given column in the column Map.\n\t * <p>By default, the supplied column name will be returned unmodified.\n\t * @param columnName the column name as returned by the ResultSet\n\t * @return the column key to use\n\t * @see java.sql.ResultSetMetaData#getColumnName\n\t */",
            "\t/**\n\t * 确定列 Map 中给定列使用的键。\n\t * <p>默认原样返回列名。\n\t * @param columnName ResultSet 返回的列名\n\t * @return 使用的列键\n\t * @see java.sql.ResultSetMetaData#getColumnName\n\t */",
        ),
        (
            "\t/**\n\t * Retrieve a JDBC object value for the specified column.\n\t * <p>The default implementation uses the {@code getObject} method.\n\t * Additionally, this implementation includes a \"hack\" to get around Oracle\n\t * returning a non-standard object for their TIMESTAMP data type.\n\t * @param rs the ResultSet holding the data\n\t * @param index the column index\n\t * @return the Object returned\n\t * @see org.springframework.jdbc.support.JdbcUtils#getResultSetValue\n\t */",
            "\t/**\n\t * 获取指定列的 JDBC 对象值。\n\t * <p>默认使用 {@code getObject}；另含针对 Oracle TIMESTAMP\n\t * 返回非标准对象的变通处理。\n\t * @param rs 持有数据的 ResultSet\n\t * @param index 列索引\n\t * @return 返回值对象\n\t * @see org.springframework.jdbc.support.JdbcUtils#getResultSetValue\n\t */",
        ),
    ],
    "ConnectionCallback.java": [
        (
            "/**\n * Generic callback interface for code that operates on a JDBC Connection.\n * Allows to execute any number of operations on a single Connection,\n * using any type and number of Statements.\n *\n * <p>This is particularly useful for delegating to existing data access code\n * that expects a Connection to work on and throws SQLException. For newly\n * written code, it is strongly recommended to use JdbcTemplate's more specific\n * operations, for example a {@code query} or {@code update} variant.\n *\n * @author Juergen Hoeller\n * @since 1.1.3\n * @param <T> the result type\n * @see JdbcTemplate#execute(ConnectionCallback)\n * @see JdbcTemplate#query\n * @see JdbcTemplate#update\n */",
            "/**\n * 在 JDBC Connection 上执行代码的通用回调接口。\n * 可在单个 Connection 上用任意类型与数量的 Statement 执行任意次操作。\n *\n * <p>特别适用于委托给期望 Connection 并抛出 SQLException 的既有数据访问代码。\n * 新代码强烈建议使用 JdbcTemplate 更具体的操作，如 {@code query} 或 {@code update} 变体。\n *\n * @author Juergen Hoeller\n * @since 1.1.3\n * @param <T> 结果类型\n * @see JdbcTemplate#execute(ConnectionCallback)\n * @see JdbcTemplate#query\n * @see JdbcTemplate#update\n */",
        ),
        (
            "\t/**\n\t * Gets called by {@code JdbcTemplate.execute} with an active JDBC\n\t * Connection. Does not need to care about activating or closing the\n\t * Connection, or handling transactions.\n\t * <p>If called without a thread-bound JDBC transaction (initiated by\n\t * DataSourceTransactionManager), the code will simply get executed on the\n\t * JDBC connection with its transactional semantics. If JdbcTemplate is\n\t * configured to use a JTA-aware DataSource, the JDBC Connection and thus\n\t * the callback code will be transactional if a JTA transaction is active.\n\t * <p>Allows for returning a result object created within the callback, i.e.\n\t * a domain object or a collection of domain objects. Note that there's special\n\t * support for single step actions: see {@code JdbcTemplate.queryForObject}\n\t * etc. A thrown RuntimeException is treated as application exception:\n\t * it gets propagated to the caller of the template.\n\t * @param con active JDBC Connection\n\t * @return a result object, or {@code null} if none\n\t * @throws SQLException if thrown by a JDBC method, to be auto-converted\n\t * to a DataAccessException by an SQLExceptionTranslator\n\t * @throws DataAccessException in case of custom exceptions\n\t * @see JdbcTemplate#queryForObject(String, Class)\n\t * @see JdbcTemplate#queryForRowSet(String)\n\t */",
            "\t/**\n\t * 由 {@code JdbcTemplate.execute} 以活动 JDBC Connection 调用。\n\t * 无需关心激活或关闭 Connection，亦无需处理事务。\n\t * <p>若无线程绑定 JDBC 事务（由 DataSourceTransactionManager 启动），\n\t * 代码将按 JDBC 连接自身语义执行。若 JdbcTemplate 使用 JTA 感知 DataSource，\n\t * 且 JTA 事务活动，则 JDBC Connection 及回调代码亦具事务性。\n\t * <p>可返回回调内创建的结果对象，如领域对象或其集合。\n\t * 单步操作有专门支持：见 {@code JdbcTemplate.queryForObject} 等。\n\t * 抛出的 RuntimeException 视为应用异常，传播给模板调用方。\n\t * @param con 活动 JDBC Connection\n\t * @return 结果对象，无则 {@code null}\n\t * @throws SQLException JDBC 方法抛出时，由 SQLExceptionTranslator 转为 DataAccessException\n\t * @throws DataAccessException 自定义异常时\n\t * @see JdbcTemplate#queryForObject(String, Class)\n\t * @see JdbcTemplate#queryForRowSet(String)\n\t */",
        ),
    ],
    "DataClassRowMapper.java": [
        (
            "/**\n * {@link RowMapper} implementation that converts a row into a new instance\n * of the specified mapped target class. The mapped target class must be a\n * top-level class or {@code static} nested class, and it may expose either a\n * <em>data class</em> constructor with named parameters corresponding to column\n * names or classic bean property setter methods with property names corresponding\n * to column names (or even a combination of both).\n *\n * <p>The term \"data class\" applies to Java <em>records</em>, Kotlin <em>data\n * classes</em>, and any class which has a constructor with named parameters\n * that are intended to be mapped to corresponding column names.\n *\n * <p>When combining a data class constructor with setter methods, any property\n * mapped successfully via a constructor argument will not be mapped additionally\n * via a corresponding setter method. This means that constructor arguments take\n * precedence over property setter methods.\n *\n * <p>Note that this class extends {@link BeanPropertyRowMapper} and can\n * therefore serve as a common choice for any mapped target class, flexibly\n * adapting to constructor style versus setter methods in the mapped class.\n *\n * <p>Please note that this class is designed to provide convenience rather than\n * high performance. For best performance, consider using a custom {@code RowMapper}\n * implementation.\n *\n * @author Juergen Hoeller\n * @author Sam Brannen\n * @since 5.3\n * @param <T> the result type\n * @see SimplePropertyRowMapper\n */",
            "/**\n * 将行映射为指定目标类新实例的 {@link RowMapper} 实现。\n * 目标类须为顶层类或 {@code static} 嵌套类，可提供与列名对应的\n * <em>数据类</em> 命名参数构造器，或属性名对应列名的经典 setter（或两者兼有）。\n *\n * <p>「数据类」指 Java <em>record</em>、Kotlin <em>data class</em>，\n * 以及具命名参数构造器且参数拟映射到列名的任意类。\n *\n * <p>构造器与 setter 并用时，已通过构造器参数映射的属性\n * 不再经对应 setter 映射；构造器参数优先于 setter。\n *\n * <p>本类继承 {@link BeanPropertyRowMapper}，可作为通用映射选择，\n * 灵活适配构造器风格或 setter 方法。\n *\n * <p>本类侧重便利而非高性能；追求性能请考虑自定义 {@code RowMapper}。\n *\n * @author Juergen Hoeller\n * @author Sam Brannen\n * @since 5.3\n * @param <T> 结果类型\n * @see SimplePropertyRowMapper\n */",
        ),
        (
            "\t/**\n\t * Create a new {@code DataClassRowMapper} for bean-style configuration.\n\t * @see #setMappedClass\n\t * @see #setConversionService\n\t */",
            "\t/**\n\t * 创建用于 bean 风格配置的 {@code DataClassRowMapper}。\n\t * @see #setMappedClass\n\t * @see #setConversionService\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@code DataClassRowMapper}.\n\t * @param mappedClass the class that each row should be mapped to\n\t */",
            "\t/**\n\t * 创建 {@code DataClassRowMapper}。\n\t * @param mappedClass 每行映射到的类\n\t */",
        ),
        (
            "\t/**\n\t * Static factory method to create a new {@code DataClassRowMapper}.\n\t * @param mappedClass the class that each row should be mapped to\n\t * @see #newInstance(Class, ConversionService)\n\t */",
            "\t/**\n\t * 创建 {@code DataClassRowMapper} 的静态工厂方法。\n\t * @param mappedClass 每行映射到的类\n\t * @see #newInstance(Class, ConversionService)\n\t */",
        ),
        (
            "\t/**\n\t * Static factory method to create a new {@code DataClassRowMapper}.\n\t * @param mappedClass the class that each row should be mapped to\n\t * @param conversionService the {@link ConversionService} for binding\n\t * JDBC values to bean properties, or {@code null} for none\n\t * @see #newInstance(Class)\n\t * @see #setConversionService\n\t */",
            "\t/**\n\t * 创建 {@code DataClassRowMapper} 的静态工厂方法。\n\t * @param mappedClass 每行映射到的类\n\t * @param conversionService 将 JDBC 值绑定到 bean 属性的 {@link ConversionService}，无则 {@code null}\n\t * @see #newInstance(Class)\n\t * @see #setConversionService\n\t */",
        ),
    ],
    "DisposableSqlTypeValue.java": [
        (
            "/**\n * Subinterface of {@link SqlTypeValue} that adds a cleanup callback,\n * to be invoked after the value has been set and the corresponding\n * statement has been executed.\n *\n * @author Juergen Hoeller\n * @since 1.1\n * @see org.springframework.jdbc.core.support.SqlLobValue\n */",
            "/**\n * {@link SqlTypeValue} 的子接口，增加清理回调；\n * 在设值且对应语句执行后调用。\n *\n * @author Juergen Hoeller\n * @since 1.1\n * @see org.springframework.jdbc.core.support.SqlLobValue\n */",
        ),
        (
            "\t/**\n\t * Clean up resources held by this type value,\n\t * for example the LobCreator in case of an SqlLobValue.\n\t * @see org.springframework.jdbc.core.support.SqlLobValue#cleanup()\n\t * @see org.springframework.jdbc.support.SqlValue#cleanup()\n\t */",
            "\t/**\n\t * 清理本类型值持有的资源，例如 SqlLobValue 中的 LobCreator。\n\t * @see org.springframework.jdbc.core.support.SqlLobValue#cleanup()\n\t * @see org.springframework.jdbc.support.SqlValue#cleanup()\n\t */",
        ),
    ],
}
