"""Chinese JavaDoc replacements for springframework wave24b PreparedStatementCreatorFactory."""

FACTORY_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "PreparedStatementCreatorFactory.java": [
        (
            "/**\n * Helper class that efficiently creates multiple {@link PreparedStatementCreator}\n * objects with different parameters based on an SQL statement and a single\n * set of parameter declarations.\n *\n * @author Rod Johnson\n * @author Thomas Risberg\n * @author Juergen Hoeller\n */",
            "/**\n * 辅助类，基于一条 SQL 语句和一组参数声明，\n * 高效创建带不同参数的多个 {@link PreparedStatementCreator} 对象。\n *\n * @author Rod Johnson\n * @author Thomas Risberg\n * @author Juergen Hoeller\n */",
        ),
        (
            "\t/** The SQL, which won't change when the parameters change. */",
            "\t/** SQL 语句，参数变化时不会改变。 */",
        ),
        (
            "\t/** List of SqlParameter objects (may be {@code null}). */",
            "\t/** SqlParameter 对象列表（可能为 {@code null}）。 */",
        ),
        (
            "\t/**\n\t * Create a new factory. Will need to add parameters via the\n\t * {@link #addParameter} method or have no parameters.\n\t * @param sql the SQL statement to execute\n\t */",
            "\t/**\n\t * 创建新工厂。需通过 {@link #addParameter} 方法添加参数，或不设参数。\n\t * @param sql 要执行的 SQL 语句\n\t */",
        ),
        (
            "\t/**\n\t * Create a new factory with the given SQL and JDBC types.\n\t * @param sql the SQL statement to execute\n\t * @param types int array of JDBC types\n\t */",
            "\t/**\n\t * 使用给定 SQL 和 JDBC 类型创建新工厂。\n\t * @param sql 要执行的 SQL 语句\n\t * @param types JDBC 类型的 int 数组\n\t */",
        ),
        (
            "\t/**\n\t * Create a new factory with the given SQL and parameters.\n\t * @param sql the SQL statement to execute\n\t * @param declaredParameters list of {@link SqlParameter} objects\n\t */",
            "\t/**\n\t * 使用给定 SQL 和参数创建新工厂。\n\t * @param sql 要执行的 SQL 语句\n\t * @param declaredParameters {@link SqlParameter} 对象列表\n\t */",
        ),
        (
            "\t/**\n\t * Return the SQL statement to execute.\n\t * @since 5.1.3\n\t */",
            "\t/**\n\t * 返回要执行的 SQL 语句。\n\t * @since 5.1.3\n\t */",
        ),
        (
            "\t/**\n\t * Add a new declared parameter.\n\t * <p>Order of parameter addition is significant.\n\t * @param param the parameter to add to the list of declared parameters\n\t */",
            "\t/**\n\t * 添加新的声明参数。\n\t * <p>参数添加顺序有意义。\n\t * @param param 要添加到声明参数列表的参数\n\t */",
        ),
        (
            "\t/**\n\t * Set whether to use prepared statements that return a specific type of ResultSet.\n\t * @param resultSetType the ResultSet type\n\t * @see java.sql.ResultSet#TYPE_FORWARD_ONLY\n\t * @see java.sql.ResultSet#TYPE_SCROLL_INSENSITIVE\n\t * @see java.sql.ResultSet#TYPE_SCROLL_SENSITIVE\n\t */",
            "\t/**\n\t * 设置是否使用返回特定类型 ResultSet 的 PreparedStatement。\n\t * @param resultSetType ResultSet 类型\n\t * @see java.sql.ResultSet#TYPE_FORWARD_ONLY\n\t * @see java.sql.ResultSet#TYPE_SCROLL_INSENSITIVE\n\t * @see java.sql.ResultSet#TYPE_SCROLL_SENSITIVE\n\t */",
        ),
        (
            "\t/**\n\t * Set whether to use prepared statements capable of returning updatable ResultSets.\n\t */",
            "\t/**\n\t * 设置是否使用能返回可更新 ResultSet 的 PreparedStatement。\n\t */",
        ),
        (
            "\t/**\n\t * Set whether prepared statements should be capable of returning auto-generated keys.\n\t */",
            "\t/**\n\t * 设置 PreparedStatement 是否应能返回自动生成的主键。\n\t */",
        ),
        (
            "\t/**\n\t * Set the column names of the auto-generated keys.\n\t */",
            "\t/**\n\t * 设置自动生成主键的列名。\n\t */",
        ),
        (
            "\t/**\n\t * Return a new PreparedStatementSetter for the given parameters.\n\t * @param params list of parameters (may be {@code null})\n\t */",
            "\t/**\n\t * 为给定参数返回新的 PreparedStatementSetter。\n\t * @param params 参数列表（可能为 {@code null}）\n\t */",
        ),
        (
            "\t/**\n\t * Return a new PreparedStatementSetter for the given parameters.\n\t * @param params the parameter array (may be {@code null})\n\t */",
            "\t/**\n\t * 为给定参数返回新的 PreparedStatementSetter。\n\t * @param params 参数数组（可能为 {@code null}）\n\t */",
        ),
        (
            "\t/**\n\t * Return a new PreparedStatementCreator for the given parameters.\n\t * @param params list of parameters (may be {@code null})\n\t */",
            "\t/**\n\t * 为给定参数返回新的 PreparedStatementCreator。\n\t * @param params 参数列表（可能为 {@code null}）\n\t */",
        ),
        (
            "\t/**\n\t * Return a new PreparedStatementCreator for the given parameters.\n\t * @param params the parameter array (may be {@code null})\n\t */",
            "\t/**\n\t * 为给定参数返回新的 PreparedStatementCreator。\n\t * @param params 参数数组（可能为 {@code null}）\n\t */",
        ),
        (
            "\t/**\n\t * Return a new PreparedStatementCreator for the given parameters.\n\t * @param sqlToUse the actual SQL statement to use (if different from\n\t * the factory's, for example because of named parameter expanding)\n\t * @param params the parameter array (may be {@code null})\n\t */",
            "\t/**\n\t * 为给定参数返回新的 PreparedStatementCreator。\n\t * @param sqlToUse 实际使用的 SQL 语句（若与工厂的不同，\n\t * 例如因命名参数展开）\n\t * @param params 参数数组（可能为 {@code null}）\n\t */",
        ),
        (
            "\t/**\n\t * PreparedStatementCreator implementation returned by this class.\n\t */",
            "\t/**\n\t * 本类返回的 PreparedStatementCreator 实现。\n\t */",
        ),
        (
            "\t\t\t\t// Account for named parameters being used multiple times",
            "\t\t\t\t// 考虑命名参数被多次使用的情况",
        ),
        (
            "\t\t\t// Set arguments: Does nothing if there are no parameters.",
            "\t\t\t// 设置参数：若无参数则不执行任何操作。",
        ),
        (
            "\t\t\t\t// SqlParameterValue overrides declared parameter meta-data, in particular for",
            "\t\t\t\t// SqlParameterValue 覆盖声明的参数元数据，特别是",
        ),
        (
            "\t\t\t\t// independence from the declared parameter position in case of named parameters.",
            "\t\t\t\t// 命名参数场景下不依赖声明的参数位置。",
        ),
    ],
}
