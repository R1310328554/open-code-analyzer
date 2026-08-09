"""Chinese JavaDoc replacements for springframework wave24b SQL parameter classes."""

SQLPARAM_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "ResultSetSupportingSqlParameter.java": [
        (
            "/**\n * Common base class for ResultSet-supporting SqlParameters like\n * {@link SqlOutParameter} and {@link SqlReturnResultSet}.\n *\n * @author Juergen Hoeller\n * @since 1.0.2\n */",
            "/**\n * 支持 ResultSet 的 SqlParameter（如 {@link SqlOutParameter}\n * 和 {@link SqlReturnResultSet}）的公共基类。\n *\n * @author Juergen Hoeller\n * @since 1.0.2\n */",
        ),
        (
            "\t/**\n\t * Create a new ResultSetSupportingSqlParameter.\n\t * @param name the name of the parameter, as used in input and output maps\n\t * @param sqlType the parameter SQL type according to {@code java.sql.Types}\n\t */",
            "\t/**\n\t * 创建新的 ResultSetSupportingSqlParameter。\n\t * @param name 参数名称，用于输入和输出 Map\n\t * @param sqlType 参数的 SQL 类型，对应 {@code java.sql.Types}\n\t */",
        ),
        (
            "\t/**\n\t * Create a new ResultSetSupportingSqlParameter.\n\t * @param name the name of the parameter, as used in input and output maps\n\t * @param sqlType the parameter SQL type according to {@code java.sql.Types}\n\t * @param scale the number of digits after the decimal point\n\t * (for DECIMAL and NUMERIC types)\n\t */",
            "\t/**\n\t * 创建新的 ResultSetSupportingSqlParameter。\n\t * @param name 参数名称，用于输入和输出 Map\n\t * @param sqlType 参数的 SQL 类型，对应 {@code java.sql.Types}\n\t * @param scale 小数点后的位数（用于 DECIMAL 和 NUMERIC 类型）\n\t */",
        ),
        (
            "\t/**\n\t * Create a new ResultSetSupportingSqlParameter.\n\t * @param name the name of the parameter, as used in input and output maps\n\t * @param sqlType the parameter SQL type according to {@code java.sql.Types}\n\t * @param typeName the type name of the parameter (optional)\n\t */",
            "\t/**\n\t * 创建新的 ResultSetSupportingSqlParameter。\n\t * @param name 参数名称，用于输入和输出 Map\n\t * @param sqlType 参数的 SQL 类型，对应 {@code java.sql.Types}\n\t * @param typeName 参数的类型名（可选）\n\t */",
        ),
        (
            "\t/**\n\t * Create a new ResultSetSupportingSqlParameter.\n\t * @param name the name of the parameter, as used in input and output maps\n\t * @param sqlType the parameter SQL type according to {@code java.sql.Types}\n\t * @param rse the {@link ResultSetExtractor} to use for parsing the {@link ResultSet}\n\t */",
            "\t/**\n\t * 创建新的 ResultSetSupportingSqlParameter。\n\t * @param name 参数名称，用于输入和输出 Map\n\t * @param sqlType 参数的 SQL 类型，对应 {@code java.sql.Types}\n\t * @param rse 用于解析 {@link ResultSet} 的 {@link ResultSetExtractor}\n\t */",
        ),
        (
            "\t/**\n\t * Create a new ResultSetSupportingSqlParameter.\n\t * @param name the name of the parameter, as used in input and output maps\n\t * @param sqlType the parameter SQL type according to {@code java.sql.Types}\n\t * @param rch the {@link RowCallbackHandler} to use for parsing the {@link ResultSet}\n\t */",
            "\t/**\n\t * 创建新的 ResultSetSupportingSqlParameter。\n\t * @param name 参数名称，用于输入和输出 Map\n\t * @param sqlType 参数的 SQL 类型，对应 {@code java.sql.Types}\n\t * @param rch 用于解析 {@link ResultSet} 的 {@link RowCallbackHandler}\n\t */",
        ),
        (
            "\t/**\n\t * Create a new ResultSetSupportingSqlParameter.\n\t * @param name the name of the parameter, as used in input and output maps\n\t * @param sqlType the parameter SQL type according to {@code java.sql.Types}\n\t * @param rm the {@link RowMapper} to use for parsing the {@link ResultSet}\n\t */",
            "\t/**\n\t * 创建新的 ResultSetSupportingSqlParameter。\n\t * @param name 参数名称，用于输入和输出 Map\n\t * @param sqlType 参数的 SQL 类型，对应 {@code java.sql.Types}\n\t * @param rm 用于解析 {@link ResultSet} 的 {@link RowMapper}\n\t */",
        ),
        (
            "\t/**\n\t * Does this parameter support a ResultSet, i.e. does it hold a\n\t * ResultSetExtractor, RowCallbackHandler or RowMapper?\n\t */",
            "\t/**\n\t * 此参数是否支持 ResultSet，即是否持有\n\t * ResultSetExtractor、RowCallbackHandler 或 RowMapper？\n\t */",
        ),
        (
            "\t/**\n\t * Return the ResultSetExtractor held by this parameter, if any.\n\t */",
            "\t/**\n\t * 返回此参数持有的 ResultSetExtractor（若有）。\n\t */",
        ),
        (
            "\t/**\n\t * Return the RowCallbackHandler held by this parameter, if any.\n\t */",
            "\t/**\n\t * 返回此参数持有的 RowCallbackHandler（若有）。\n\t */",
        ),
        (
            "\t/**\n\t * Return the RowMapper held by this parameter, if any.\n\t */",
            "\t/**\n\t * 返回此参数持有的 RowMapper（若有）。\n\t */",
        ),
        (
            "\t/**\n\t * This implementation always returns {@code false}.\n\t */",
            "\t/**\n\t * 本实现始终返回 {@code false}。\n\t */",
        ),
    ],
    "SqlInOutParameter.java": [
        (
            "/**\n * Subclass of {@link SqlOutParameter} to represent an INOUT parameter.\n * Will return {@code true} for SqlParameter's {@link #isInputValueProvided}\n * test, in contrast to a standard SqlOutParameter.\n *\n * <p>Output parameters - like all stored procedure parameters - must have names.\n *\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @since 2.0\n */",
            "/**\n * {@link SqlOutParameter} 的子类，表示 INOUT 参数。\n * 与标准 SqlOutParameter 不同，对 SqlParameter 的\n * {@link #isInputValueProvided} 测试将返回 {@code true}。\n *\n * <p>输出参数——与所有存储过程参数一样——必须有名称。\n *\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @since 2.0\n */",
        ),
        (
            "\t/**\n\t * Create a new SqlInOutParameter.\n\t * @param name the name of the parameter, as used in input and output maps\n\t * @param sqlType the parameter SQL type according to {@code java.sql.Types}\n\t */",
            "\t/**\n\t * 创建新的 SqlInOutParameter。\n\t * @param name 参数名称，用于输入和输出 Map\n\t * @param sqlType 参数的 SQL 类型，对应 {@code java.sql.Types}\n\t */",
        ),
        (
            "\t/**\n\t * Create a new SqlInOutParameter.\n\t * @param name the name of the parameter, as used in input and output maps\n\t * @param sqlType the parameter SQL type according to {@code java.sql.Types}\n\t * @param scale the number of digits after the decimal point\n\t * (for DECIMAL and NUMERIC types)\n\t */",
            "\t/**\n\t * 创建新的 SqlInOutParameter。\n\t * @param name 参数名称，用于输入和输出 Map\n\t * @param sqlType 参数的 SQL 类型，对应 {@code java.sql.Types}\n\t * @param scale 小数点后的位数（用于 DECIMAL 和 NUMERIC 类型）\n\t */",
        ),
        (
            "\t/**\n\t * Create a new SqlInOutParameter.\n\t * @param name the name of the parameter, as used in input and output maps\n\t * @param sqlType the parameter SQL type according to {@code java.sql.Types}\n\t * @param typeName the type name of the parameter (optional)\n\t */",
            "\t/**\n\t * 创建新的 SqlInOutParameter。\n\t * @param name 参数名称，用于输入和输出 Map\n\t * @param sqlType 参数的 SQL 类型，对应 {@code java.sql.Types}\n\t * @param typeName 参数的类型名（可选）\n\t */",
        ),
        (
            "\t/**\n\t * Create a new SqlInOutParameter.\n\t * @param name the name of the parameter, as used in input and output maps\n\t * @param sqlType the parameter SQL type according to {@code java.sql.Types}\n\t * @param typeName the type name of the parameter (optional)\n\t * @param sqlReturnType custom value handler for complex type (optional)\n\t */",
            "\t/**\n\t * 创建新的 SqlInOutParameter。\n\t * @param name 参数名称，用于输入和输出 Map\n\t * @param sqlType 参数的 SQL 类型，对应 {@code java.sql.Types}\n\t * @param typeName 参数的类型名（可选）\n\t * @param sqlReturnType 复杂类型的自定义值处理器（可选）\n\t */",
        ),
        (
            "\t/**\n\t * Create a new SqlInOutParameter.\n\t * @param name the name of the parameter, as used in input and output maps\n\t * @param sqlType the parameter SQL type according to {@code java.sql.Types}\n\t * @param rse the {@link ResultSetExtractor} to use for parsing the {@link ResultSet}\n\t */",
            "\t/**\n\t * 创建新的 SqlInOutParameter。\n\t * @param name 参数名称，用于输入和输出 Map\n\t * @param sqlType 参数的 SQL 类型，对应 {@code java.sql.Types}\n\t * @param rse 用于解析 {@link ResultSet} 的 {@link ResultSetExtractor}\n\t */",
        ),
        (
            "\t/**\n\t * Create a new SqlInOutParameter.\n\t * @param name the name of the parameter, as used in input and output maps\n\t * @param sqlType the parameter SQL type according to {@code java.sql.Types}\n\t * @param rch the {@link RowCallbackHandler} to use for parsing the {@link ResultSet}\n\t */",
            "\t/**\n\t * 创建新的 SqlInOutParameter。\n\t * @param name 参数名称，用于输入和输出 Map\n\t * @param sqlType 参数的 SQL 类型，对应 {@code java.sql.Types}\n\t * @param rch 用于解析 {@link ResultSet} 的 {@link RowCallbackHandler}\n\t */",
        ),
        (
            "\t/**\n\t * Create a new SqlInOutParameter.\n\t * @param name the name of the parameter, as used in input and output maps\n\t * @param sqlType the parameter SQL type according to {@code java.sql.Types}\n\t * @param rm the {@link RowMapper} to use for parsing the {@link ResultSet}\n\t */",
            "\t/**\n\t * 创建新的 SqlInOutParameter。\n\t * @param name 参数名称，用于输入和输出 Map\n\t * @param sqlType 参数的 SQL 类型，对应 {@code java.sql.Types}\n\t * @param rm 用于解析 {@link ResultSet} 的 {@link RowMapper}\n\t */",
        ),
        (
            "\t/**\n\t * This implementation always returns {@code true}.\n\t */",
            "\t/**\n\t * 本实现始终返回 {@code true}。\n\t */",
        ),
    ],
    "SqlOutParameter.java": [
        (
            "/**\n * Subclass of {@link SqlParameter} to represent an output parameter.\n * No additional properties: instanceof will be used to check for such types.\n *\n * <p>Output parameters - like all stored procedure parameters - must have names.\n *\n * @author Rod Johnson\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @see SqlReturnResultSet\n * @see SqlInOutParameter\n */",
            "/**\n * {@link SqlParameter} 的子类，表示输出参数。\n * 无额外属性：通过 instanceof 检查此类类型。\n *\n * <p>输出参数——与所有存储过程参数一样——必须有名称。\n *\n * @author Rod Johnson\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @see SqlReturnResultSet\n * @see SqlInOutParameter\n */",
        ),
        (
            "\t/**\n\t * Create a new SqlOutParameter.\n\t * @param name the name of the parameter, as used in input and output maps\n\t * @param sqlType the parameter SQL type according to {@code java.sql.Types}\n\t */",
            "\t/**\n\t * 创建新的 SqlOutParameter。\n\t * @param name 参数名称，用于输入和输出 Map\n\t * @param sqlType 参数的 SQL 类型，对应 {@code java.sql.Types}\n\t */",
        ),
        (
            "\t/**\n\t * Create a new SqlOutParameter.\n\t * @param name the name of the parameter, as used in input and output maps\n\t * @param sqlType the parameter SQL type according to {@code java.sql.Types}\n\t * @param scale the number of digits after the decimal point\n\t * (for DECIMAL and NUMERIC types)\n\t */",
            "\t/**\n\t * 创建新的 SqlOutParameter。\n\t * @param name 参数名称，用于输入和输出 Map\n\t * @param sqlType 参数的 SQL 类型，对应 {@code java.sql.Types}\n\t * @param scale 小数点后的位数（用于 DECIMAL 和 NUMERIC 类型）\n\t */",
        ),
        (
            "\t/**\n\t * Create a new SqlOutParameter.\n\t * @param name the name of the parameter, as used in input and output maps\n\t * @param sqlType the parameter SQL type according to {@code java.sql.Types}\n\t * @param typeName the type name of the parameter (optional)\n\t */",
            "\t/**\n\t * 创建新的 SqlOutParameter。\n\t * @param name 参数名称，用于输入和输出 Map\n\t * @param sqlType 参数的 SQL 类型，对应 {@code java.sql.Types}\n\t * @param typeName 参数的类型名（可选）\n\t */",
        ),
        (
            "\t/**\n\t * Create a new SqlOutParameter.\n\t * @param name the name of the parameter, as used in input and output maps\n\t * @param sqlType the parameter SQL type according to {@code java.sql.Types}\n\t * @param typeName the type name of the parameter (optional)\n\t * @param sqlReturnType custom value handler for complex type (optional)\n\t */",
            "\t/**\n\t * 创建新的 SqlOutParameter。\n\t * @param name 参数名称，用于输入和输出 Map\n\t * @param sqlType 参数的 SQL 类型，对应 {@code java.sql.Types}\n\t * @param typeName 参数的类型名（可选）\n\t * @param sqlReturnType 复杂类型的自定义值处理器（可选）\n\t */",
        ),
        (
            "\t/**\n\t * Create a new SqlOutParameter.\n\t * @param name the name of the parameter, as used in input and output maps\n\t * @param sqlType the parameter SQL type according to {@code java.sql.Types}\n\t * @param rse the {@link ResultSetExtractor} to use for parsing the {@link ResultSet}\n\t */",
            "\t/**\n\t * 创建新的 SqlOutParameter。\n\t * @param name 参数名称，用于输入和输出 Map\n\t * @param sqlType 参数的 SQL 类型，对应 {@code java.sql.Types}\n\t * @param rse 用于解析 {@link ResultSet} 的 {@link ResultSetExtractor}\n\t */",
        ),
        (
            "\t/**\n\t * Create a new SqlOutParameter.\n\t * @param name the name of the parameter, as used in input and output maps\n\t * @param sqlType the parameter SQL type according to {@code java.sql.Types}\n\t * @param rch the {@link RowCallbackHandler} to use for parsing the {@link ResultSet}\n\t */",
            "\t/**\n\t * 创建新的 SqlOutParameter。\n\t * @param name 参数名称，用于输入和输出 Map\n\t * @param sqlType 参数的 SQL 类型，对应 {@code java.sql.Types}\n\t * @param rch 用于解析 {@link ResultSet} 的 {@link RowCallbackHandler}\n\t */",
        ),
        (
            "\t/**\n\t * Create a new SqlOutParameter.\n\t * @param name the name of the parameter, as used in input and output maps\n\t * @param sqlType the parameter SQL type according to {@code java.sql.Types}\n\t * @param rm the {@link RowMapper} to use for parsing the {@link ResultSet}\n\t */",
            "\t/**\n\t * 创建新的 SqlOutParameter。\n\t * @param name 参数名称，用于输入和输出 Map\n\t * @param sqlType 参数的 SQL 类型，对应 {@code java.sql.Types}\n\t * @param rm 用于解析 {@link ResultSet} 的 {@link RowMapper}\n\t */",
        ),
        (
            "\t/**\n\t * Return the custom return type, if any.\n\t */",
            "\t/**\n\t * 返回自定义返回类型（若有）。\n\t */",
        ),
        (
            "\t/**\n\t * Return whether this parameter holds a custom return type.\n\t */",
            "\t/**\n\t * 返回此参数是否持有自定义返回类型。\n\t */",
        ),
    ],
    "SqlParameter.java": [
        (
            "/**\n * Object to represent an SQL parameter definition.\n *\n * <p>Parameters may be anonymous, in which case \"name\" is {@code null}.\n * However, all parameters must define an SQL type according to {@link java.sql.Types}.\n *\n * @author Rod Johnson\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @see java.sql.Types\n */",
            "/**\n * 表示 SQL 参数定义的对象。\n *\n * <p>参数可以是匿名的，此时 \"name\" 为 {@code null}。\n * 但所有参数必须根据 {@link java.sql.Types} 定义 SQL 类型。\n *\n * @author Rod Johnson\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @see java.sql.Types\n */",
        ),
        (
            "\t// The name of the parameter, if any",
            "\t// 参数名称（若有）",
        ),
        (
            "\t// SQL type constant from {@code java.sql.Types}",
            "\t// 来自 {@code java.sql.Types} 的 SQL 类型常量",
        ),
        (
            "\t// Used for types that are user-named like: STRUCT, DISTINCT, JAVA_OBJECT, named array types",
            "\t// 用于用户命名类型，如 STRUCT、DISTINCT、JAVA_OBJECT、命名数组类型",
        ),
        (
            "\t// The scale to apply in case of a NUMERIC or DECIMAL type, if any",
            "\t// NUMERIC 或 DECIMAL 类型的小数位数（若有）",
        ),
        (
            "\t/**\n\t * Create a new anonymous SqlParameter, supplying the SQL type.\n\t * @param sqlType the SQL type of the parameter according to {@code java.sql.Types}\n\t */",
            "\t/**\n\t * 创建新的匿名 SqlParameter，指定 SQL 类型。\n\t * @param sqlType 参数的 SQL 类型，对应 {@code java.sql.Types}\n\t */",
        ),
        (
            "\t/**\n\t * Create a new anonymous SqlParameter, supplying the SQL type.\n\t * @param sqlType the SQL type of the parameter according to {@code java.sql.Types}\n\t * @param typeName the type name of the parameter (optional)\n\t */",
            "\t/**\n\t * 创建新的匿名 SqlParameter，指定 SQL 类型。\n\t * @param sqlType 参数的 SQL 类型，对应 {@code java.sql.Types}\n\t * @param typeName 参数的类型名（可选）\n\t */",
        ),
        (
            "\t/**\n\t * Create a new anonymous SqlParameter, supplying the SQL type.\n\t * @param sqlType the SQL type of the parameter according to {@code java.sql.Types}\n\t * @param scale the number of digits after the decimal point\n\t * (for DECIMAL and NUMERIC types)\n\t */",
            "\t/**\n\t * 创建新的匿名 SqlParameter，指定 SQL 类型。\n\t * @param sqlType 参数的 SQL 类型，对应 {@code java.sql.Types}\n\t * @param scale 小数点后的位数（用于 DECIMAL 和 NUMERIC 类型）\n\t */",
        ),
        (
            "\t/**\n\t * Create a new SqlParameter, supplying name and SQL type.\n\t * @param name the name of the parameter, as used in input and output maps\n\t * @param sqlType the SQL type of the parameter according to {@code java.sql.Types}\n\t */",
            "\t/**\n\t * 创建新的 SqlParameter，指定名称和 SQL 类型。\n\t * @param name 参数名称，用于输入和输出 Map\n\t * @param sqlType 参数的 SQL 类型，对应 {@code java.sql.Types}\n\t */",
        ),
        (
            "\t/**\n\t * Create a new SqlParameter, supplying name and SQL type.\n\t * @param name the name of the parameter, as used in input and output maps\n\t * @param sqlType the SQL type of the parameter according to {@code java.sql.Types}\n\t * @param typeName the type name of the parameter (optional)\n\t */",
            "\t/**\n\t * 创建新的 SqlParameter，指定名称和 SQL 类型。\n\t * @param name 参数名称，用于输入和输出 Map\n\t * @param sqlType 参数的 SQL 类型，对应 {@code java.sql.Types}\n\t * @param typeName 参数的类型名（可选）\n\t */",
        ),
        (
            "\t/**\n\t * Create a new SqlParameter, supplying name and SQL type.\n\t * @param name the name of the parameter, as used in input and output maps\n\t * @param sqlType the SQL type of the parameter according to {@code java.sql.Types}\n\t * @param scale the number of digits after the decimal point\n\t * (for DECIMAL and NUMERIC types)\n\t */",
            "\t/**\n\t * 创建新的 SqlParameter，指定名称和 SQL 类型。\n\t * @param name 参数名称，用于输入和输出 Map\n\t * @param sqlType 参数的 SQL 类型，对应 {@code java.sql.Types}\n\t * @param scale 小数点后的位数（用于 DECIMAL 和 NUMERIC 类型）\n\t */",
        ),
        (
            "\t/**\n\t * Copy constructor.\n\t * @param otherParam the SqlParameter object to copy from\n\t */",
            "\t/**\n\t * 拷贝构造器。\n\t * @param otherParam 要复制的 SqlParameter 对象\n\t */",
        ),
        (
            "\t/**\n\t * Return the name of the parameter, or {@code null} if anonymous.\n\t */",
            "\t/**\n\t * 返回参数名称，匿名时返回 {@code null}。\n\t */",
        ),
        (
            "\t/**\n\t * Return the SQL type of the parameter.\n\t */",
            "\t/**\n\t * 返回参数的 SQL 类型。\n\t */",
        ),
        (
            "\t/**\n\t * Return the type name of the parameter, if any.\n\t */",
            "\t/**\n\t * 返回参数的类型名（若有）。\n\t */",
        ),
        (
            "\t/**\n\t * Return the scale of the parameter, if any.\n\t */",
            "\t/**\n\t * 返回参数的小数位数（若有）。\n\t */",
        ),
        (
            "\t/**\n\t * Return whether this parameter holds input values that should be set\n\t * before execution even if they are {@code null}.\n\t * <p>This implementation always returns {@code true}.\n\t */",
            "\t/**\n\t * 返回此参数是否持有应在执行前设置的输入值，\n\t * 即使值为 {@code null}。\n\t * <p>本实现始终返回 {@code true}。\n\t */",
        ),
        (
            "\t/**\n\t * Return whether this parameter is an implicit return parameter used during the\n\t * results processing of {@code CallableStatement.getMoreResults/getUpdateCount}.\n\t * <p>This implementation always returns {@code false}.\n\t */",
            "\t/**\n\t * 返回此参数是否为 {@code CallableStatement.getMoreResults/getUpdateCount}\n\t * 结果处理期间使用的隐式返回参数。\n\t * <p>本实现始终返回 {@code false}。\n\t */",
        ),
        (
            "\t/**\n\t * Convert a list of JDBC types, as defined in {@code java.sql.Types},\n\t * to a List of SqlParameter objects as used in this package.\n\t */",
            "\t/**\n\t * 将 {@code java.sql.Types} 中定义的 JDBC 类型列表\n\t * 转换为本包使用的 SqlParameter 对象 List。\n\t */",
        ),
    ],
    "SqlParameterValue.java": [
        (
            "/**\n * Object to represent an SQL parameter value, including parameter meta-data\n * such as the SQL type and the scale for numeric values.\n *\n * <p>Designed for use with {@link JdbcTemplate}'s operations that take an array of\n * argument values: Each such argument value may be a {@code SqlParameterValue},\n * indicating the SQL type (and optionally the scale) instead of letting the\n * template guess a default type. Note that this only applies to the operations with\n * a 'plain' argument array, not to the overloaded variants with an explicit type array.\n *\n * @author Juergen Hoeller\n * @since 2.0.5\n * @see java.sql.Types\n * @see JdbcTemplate#query(String, ResultSetExtractor, Object[])\n * @see JdbcTemplate#query(String, RowCallbackHandler, Object[])\n * @see JdbcTemplate#query(String, RowMapper, Object[])\n * @see JdbcTemplate#update(String, Object[])\n */",
            "/**\n * 表示 SQL 参数值的对象，包含参数元数据，\n * 如 SQL 类型和数值的小数位数。\n *\n * <p>设计用于 {@link JdbcTemplate} 接受参数值数组的操作：\n * 每个参数值可以是 {@code SqlParameterValue}，\n * 显式指定 SQL 类型（及可选的小数位数），\n * 而非让模板猜测默认类型。\n * 注意这仅适用于带普通参数数组的操作，\n * 不适用于带显式类型数组的重载变体。\n *\n * @author Juergen Hoeller\n * @since 2.0.5\n * @see java.sql.Types\n * @see JdbcTemplate#query(String, ResultSetExtractor, Object[])\n * @see JdbcTemplate#query(String, RowCallbackHandler, Object[])\n * @see JdbcTemplate#query(String, RowMapper, Object[])\n * @see JdbcTemplate#update(String, Object[])\n */",
        ),
        (
            "\t/**\n\t * Create a new SqlParameterValue, supplying the SQL type.\n\t * @param sqlType the SQL type of the parameter according to {@code java.sql.Types}\n\t * @param value the value object\n\t */",
            "\t/**\n\t * 创建新的 SqlParameterValue，指定 SQL 类型。\n\t * @param sqlType 参数的 SQL 类型，对应 {@code java.sql.Types}\n\t * @param value 值对象\n\t */",
        ),
        (
            "\t/**\n\t * Create a new SqlParameterValue, supplying the SQL type.\n\t * @param sqlType the SQL type of the parameter according to {@code java.sql.Types}\n\t * @param typeName the type name of the parameter (optional)\n\t * @param value the value object\n\t */",
            "\t/**\n\t * 创建新的 SqlParameterValue，指定 SQL 类型。\n\t * @param sqlType 参数的 SQL 类型，对应 {@code java.sql.Types}\n\t * @param typeName 参数的类型名（可选）\n\t * @param value 值对象\n\t */",
        ),
        (
            "\t/**\n\t * Create a new SqlParameterValue, supplying the SQL type.\n\t * @param sqlType the SQL type of the parameter according to {@code java.sql.Types}\n\t * @param scale the number of digits after the decimal point\n\t * (for DECIMAL and NUMERIC types)\n\t * @param value the value object\n\t */",
            "\t/**\n\t * 创建新的 SqlParameterValue，指定 SQL 类型。\n\t * @param sqlType 参数的 SQL 类型，对应 {@code java.sql.Types}\n\t * @param scale 小数点后的位数（用于 DECIMAL 和 NUMERIC 类型）\n\t * @param value 值对象\n\t */",
        ),
        (
            "\t/**\n\t * Create a new SqlParameterValue based on the given SqlParameter declaration.\n\t * @param declaredParam the declared SqlParameter to define a value for\n\t * @param value the value object\n\t */",
            "\t/**\n\t * 基于给定 SqlParameter 声明创建新的 SqlParameterValue。\n\t * @param declaredParam 要定义值的已声明 SqlParameter\n\t * @param value 值对象\n\t */",
        ),
        (
            "\t/**\n\t * Return the value object that this parameter value holds.\n\t */",
            "\t/**\n\t * 返回此参数值持有的值对象。\n\t */",
        ),
    ],
}
