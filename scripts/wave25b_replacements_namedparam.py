"""Chinese JavaDoc replacements for springframework wave25b named-parameter classes."""

NAMEDPARAM_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "AbstractSqlParameterSource.java": [
        (
            "/**\n * Abstract base class for {@link SqlParameterSource} implementations.\n * Provides registration of SQL types per parameter and a friendly\n * {@link #toString() toString} representation enumerating all parameters for\n * a {@code SqlParameterSource} implementing {@link #getParameterNames()}.\n * Concrete subclasses must implement {@link #hasValue} and {@link #getValue}.\n *\n * @author Juergen Hoeller\n * @author Jens Schauder\n * @since 2.0\n * @see #hasValue(String)\n * @see #getValue(String)\n * @see #getParameterNames()\n */",
            "/**\n * {@link SqlParameterSource} 实现的抽象基类。\n * 提供按参数注册 SQL 类型，并为实现了 {@link #getParameterNames()} 的\n * {@code SqlParameterSource} 提供友好的 {@link #toString() toString} 表示，\n * 枚举所有参数。具体子类必须实现 {@link #hasValue} 和 {@link #getValue}。\n *\n * @author Juergen Hoeller\n * @author Jens Schauder\n * @since 2.0\n * @see #hasValue(String)\n * @see #getValue(String)\n * @see #getParameterNames()\n */",
        ),
        (
            "\t/**\n\t * Register an SQL type for the given parameter.\n\t * @param paramName the name of the parameter\n\t * @param sqlType the SQL type of the parameter\n\t */",
            "\t/**\n\t * 为给定参数注册 SQL 类型。\n\t * @param paramName 参数名称\n\t * @param sqlType 参数的 SQL 类型\n\t */",
        ),
        (
            "\t/**\n\t * Register an SQL type for the given parameter.\n\t * @param paramName the name of the parameter\n\t * @param typeName the type name of the parameter\n\t */",
            "\t/**\n\t * 为给定参数注册类型名称。\n\t * @param paramName 参数名称\n\t * @param typeName 参数的类型名称\n\t */",
        ),
        (
            "\t/**\n\t * Return the SQL type for the given parameter, if registered.\n\t * @param paramName the name of the parameter\n\t * @return the SQL type of the parameter,\n\t * or {@code TYPE_UNKNOWN} if not registered\n\t */",
            "\t/**\n\t * 返回给定参数已注册的 SQL 类型。\n\t * @param paramName 参数名称\n\t * @return 参数的 SQL 类型，未注册时返回 {@code TYPE_UNKNOWN}\n\t */",
        ),
        (
            "\t/**\n\t * Return the type name for the given parameter, if registered.\n\t * @param paramName the name of the parameter\n\t * @return the type name of the parameter,\n\t * or {@code null} if not registered\n\t */",
            "\t/**\n\t * 返回给定参数已注册的类型名称。\n\t * @param paramName 参数名称\n\t * @return 参数的类型名称，未注册时返回 {@code null}\n\t */",
        ),
        (
            "\t/**\n\t * Enumerate the parameter names and values with their corresponding SQL type if available,\n\t * or just return the simple {@code SqlParameterSource} implementation class name otherwise.\n\t * @since 5.2\n\t * @see #getParameterNames()\n\t */",
            "\t/**\n\t * 枚举参数名称和值及其对应的 SQL 类型（若有），\n\t * 否则仅返回 {@code SqlParameterSource} 实现类的简单类名。\n\t * @since 5.2\n\t * @see #getParameterNames()\n\t */",
        ),
    ],
    "BeanPropertySqlParameterSource.java": [
        (
            "/**\n * {@link SqlParameterSource} implementation that obtains parameter values\n * from bean properties of a given JavaBean object. The names of the bean\n * properties have to match the parameter names. Supports components of\n * record classes as well, with accessor methods matching parameter names.\n *\n * <p>Uses a Spring {@link BeanWrapper} for bean property access underneath.\n *\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @since 2.0\n * @see NamedParameterJdbcTemplate\n * @see SimplePropertySqlParameterSource\n */",
            "/**\n * 从给定 JavaBean 对象的 bean 属性获取参数值的 {@link SqlParameterSource} 实现。\n * bean 属性名称须与参数名称匹配。也支持 record 类的组件，\n * 其访问器方法须与参数名称匹配。\n *\n * <p>底层使用 Spring {@link BeanWrapper} 访问 bean 属性。\n *\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @since 2.0\n * @see NamedParameterJdbcTemplate\n * @see SimplePropertySqlParameterSource\n */",
        ),
        (
            "\t/**\n\t * Create a new BeanPropertySqlParameterSource for the given bean.\n\t * @param object the bean instance to wrap\n\t */",
            "\t/**\n\t * 为给定 bean 创建新的 BeanPropertySqlParameterSource。\n\t * @param object 要包装的 bean 实例\n\t */",
        ),
        (
            "\t/**\n\t * Derives a default SQL type from the corresponding property type.\n\t * @see org.springframework.jdbc.core.StatementCreatorUtils#javaTypeToSqlParameterType\n\t */",
            "\t/**\n\t * 从对应属性类型推导默认 SQL 类型。\n\t * @see org.springframework.jdbc.core.StatementCreatorUtils#javaTypeToSqlParameterType\n\t */",
        ),
        (
            "\t/**\n\t * Provide access to the property names of the wrapped bean.\n\t * Uses support provided in the {@link PropertyAccessor} interface.\n\t * @return an array containing all the known property names\n\t */",
            "\t/**\n\t * 提供对包装 bean 属性名称的访问。\n\t * 使用 {@link PropertyAccessor} 接口提供的支持。\n\t * @return 包含所有已知属性名称的数组\n\t */",
        ),
    ],
    "EmptySqlParameterSource.java": [
        (
            "/**\n * A simple empty implementation of the {@link SqlParameterSource} interface.\n *\n * @author Juergen Hoeller\n * @since 3.2.2\n */",
            "/**\n * {@link SqlParameterSource} 接口的简单空实现。\n *\n * @author Juergen Hoeller\n * @since 3.2.2\n */",
        ),
        (
            "\t/**\n\t * A shared instance of {@link EmptySqlParameterSource}.\n\t */",
            "\t/**\n\t * {@link EmptySqlParameterSource} 的共享实例。\n\t */",
        ),
    ],
    "MapSqlParameterSource.java": [
        (
            "/**\n * {@link SqlParameterSource} implementation that holds a given Map of parameters.\n *\n * <p>This class is intended for passing in a simple Map of parameter values\n * to the methods of the {@link NamedParameterJdbcTemplate} class.\n *\n * <p>The {@code addValue} methods on this class will make adding several values\n * easier. The methods return a reference to the {@link MapSqlParameterSource}\n * itself, so you can chain several method calls together within a single statement.\n *\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @since 2.0\n * @see #addValue(String, Object)\n * @see #addValue(String, Object, int)\n * @see #registerSqlType\n * @see NamedParameterJdbcTemplate\n */",
            "/**\n * 持有给定参数 Map 的 {@link SqlParameterSource} 实现。\n *\n * <p>本类用于向 {@link NamedParameterJdbcTemplate} 类的方法\n * 传入简单的参数值 Map。\n *\n * <p>本类的 {@code addValue} 方法便于添加多个值。\n * 方法返回 {@link MapSqlParameterSource} 自身引用，\n * 可在单条语句中链式调用多个方法。\n *\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @since 2.0\n * @see #addValue(String, Object)\n * @see #addValue(String, Object, int)\n * @see #registerSqlType\n * @see NamedParameterJdbcTemplate\n */",
        ),
        (
            "\t/**\n\t * Create an empty MapSqlParameterSource,\n\t * with values to be added via {@code addValue}.\n\t * @see #addValue(String, Object)\n\t */",
            "\t/**\n\t * 创建空的 MapSqlParameterSource，\n\t * 值通过 {@code addValue} 添加。\n\t * @see #addValue(String, Object)\n\t */",
        ),
        (
            "\t/**\n\t * Create a new MapSqlParameterSource, with one value\n\t * comprised of the supplied arguments.\n\t * @param paramName the name of the parameter\n\t * @param value the value of the parameter\n\t * @see #addValue(String, Object)\n\t */",
            "\t/**\n\t * 创建新的 MapSqlParameterSource，包含一个由给定参数组成的值。\n\t * @param paramName 参数名称\n\t * @param value 参数值\n\t * @see #addValue(String, Object)\n\t */",
        ),
        (
            "\t/**\n\t * Create a new MapSqlParameterSource based on a Map.\n\t * @param values a Map holding existing parameter values (can be {@code null})\n\t */",
            "\t/**\n\t * 基于 Map 创建新的 MapSqlParameterSource。\n\t * @param values 持有现有参数值的 Map（可为 {@code null}）\n\t */",
        ),
        (
            "\t/**\n\t * Add a parameter to this parameter source.\n\t * @param paramName the name of the parameter\n\t * @param value the value of the parameter\n\t * @return a reference to this parameter source,\n\t * so it's possible to chain several calls together\n\t */",
            "\t/**\n\t * 向此参数源添加参数。\n\t * @param paramName 参数名称\n\t * @param value 参数值\n\t * @return 此参数源的引用，便于链式调用\n\t */",
        ),
        (
            "\t/**\n\t * Add a parameter to this parameter source.\n\t * @param paramName the name of the parameter\n\t * @param value the value of the parameter\n\t * @param sqlType the SQL type of the parameter\n\t * @return a reference to this parameter source,\n\t * so it's possible to chain several calls together\n\t */",
            "\t/**\n\t * 向此参数源添加参数。\n\t * @param paramName 参数名称\n\t * @param value 参数值\n\t * @param sqlType 参数的 SQL 类型\n\t * @return 此参数源的引用，便于链式调用\n\t */",
        ),
        (
            "\t/**\n\t * Add a parameter to this parameter source.\n\t * @param paramName the name of the parameter\n\t * @param value the value of the parameter\n\t * @param sqlType the SQL type of the parameter\n\t * @param typeName the type name of the parameter\n\t * @return a reference to this parameter source,\n\t * so it's possible to chain several calls together\n\t */",
            "\t/**\n\t * 向此参数源添加参数。\n\t * @param paramName 参数名称\n\t * @param value 参数值\n\t * @param sqlType 参数的 SQL 类型\n\t * @param typeName 参数的类型名称\n\t * @return 此参数源的引用，便于链式调用\n\t */",
        ),
        (
            "\t/**\n\t * Add a Map of parameters to this parameter source.\n\t * @param values a Map holding existing parameter values (can be {@code null})\n\t * @return a reference to this parameter source,\n\t * so it's possible to chain several calls together\n\t */",
            "\t/**\n\t * 向此参数源添加参数 Map。\n\t * @param values 持有现有参数值的 Map（可为 {@code null}）\n\t * @return 此参数源的引用，便于链式调用\n\t */",
        ),
        (
            "\t/**\n\t * Return whether this parameter source has been configured with any values.\n\t * @since 6.1\n\t */",
            "\t/**\n\t * 返回此参数源是否已配置任何值。\n\t * @since 6.1\n\t */",
        ),
        (
            "\t/**\n\t * Expose the current parameter values as read-only Map.\n\t */",
            "\t/**\n\t * 以只读 Map 形式暴露当前参数值。\n\t */",
        ),
    ],
    "NamedParameterJdbcDaoSupport.java": [
        (
            "/**\n * Extension of JdbcDaoSupport that exposes a NamedParameterJdbcTemplate as well.\n *\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @since 2.0\n * @see NamedParameterJdbcTemplate\n * @deprecated as of 7.0, in favor of direct injection of {@link NamedParameterJdbcTemplate}\n * or {@link org.springframework.jdbc.core.simple.JdbcClient}\n */",
            "/**\n * JdbcDaoSupport 的扩展，同时暴露 NamedParameterJdbcTemplate。\n *\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @since 2.0\n * @see NamedParameterJdbcTemplate\n * @deprecated 自 7.0 起弃用，建议直接注入 {@link NamedParameterJdbcTemplate}\n * 或 {@link org.springframework.jdbc.core.simple.JdbcClient}\n */",
        ),
        (
            "\t/**\n\t * Create a NamedParameterJdbcTemplate based on the configured JdbcTemplate.\n\t */",
            "\t/**\n\t * 基于已配置的 JdbcTemplate 创建 NamedParameterJdbcTemplate。\n\t */",
        ),
        (
            "\t/**\n\t * Return a NamedParameterJdbcTemplate wrapping the configured JdbcTemplate.\n\t */",
            "\t/**\n\t * 返回包装已配置 JdbcTemplate 的 NamedParameterJdbcTemplate。\n\t */",
        ),
    ],
    "ParsedSql.java": [
        (
            "/**\n * Holds information about a parsed SQL statement.\n *\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @since 2.0\n */",
            "/**\n * 保存已解析 SQL 语句的信息。\n *\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @since 2.0\n */",
        ),
        (
            "\t/**\n\t * Create a new instance of the {@link ParsedSql} class.\n\t * @param originalSql the SQL statement that is being (or is to be) parsed\n\t */",
            "\t/**\n\t * 创建 {@link ParsedSql} 类的新实例。\n\t * @param originalSql 正在（或将要）解析的 SQL 语句\n\t */",
        ),
        (
            "\t/**\n\t * Return the SQL statement that is being parsed.\n\t */",
            "\t/**\n\t * 返回正在解析的 SQL 语句。\n\t */",
        ),
        (
            "\t/**\n\t * Add a named parameter parsed from this SQL statement.\n\t * @param parameterName the name of the parameter\n\t * @param startIndex the start index in the original SQL String\n\t * @param endIndex the end index in the original SQL String\n\t */",
            "\t/**\n\t * 添加从此 SQL 语句解析出的命名参数。\n\t * @param parameterName 参数名称\n\t * @param startIndex 原始 SQL 字符串中的起始索引\n\t * @param endIndex 原始 SQL 字符串中的结束索引\n\t */",
        ),
        (
            "\t/**\n\t * Return all the parameters (bind variables) in the parsed SQL statement.\n\t * Repeated occurrences of the same parameter name are included here.\n\t */",
            "\t/**\n\t * 返回已解析 SQL 语句中的所有参数（绑定变量）。\n\t * 同一参数名的重复出现也包含在内。\n\t */",
        ),
        (
            "\t/**\n\t * Return the parameter indexes for the specified parameter.\n\t * @param parameterPosition the position of the parameter\n\t * (as index in the parameter names List)\n\t * @return the start index and end index, combined into\n\t * an int array of length 2\n\t */",
            "\t/**\n\t * 返回指定参数的索引。\n\t * @param parameterPosition 参数位置（参数名称列表中的索引）\n\t * @return 起始索引和结束索引，合并为长度为 2 的 int 数组\n\t */",
        ),
        (
            "\t/**\n\t * Set the count of named parameters in the SQL statement.\n\t * Each parameter name counts once; repeated occurrences do not count here.\n\t */",
            "\t/**\n\t * 设置 SQL 语句中命名参数的数量。\n\t * 每个参数名只计一次，重复出现不计入。\n\t */",
        ),
        (
            "\t/**\n\t * Return the count of named parameters in the SQL statement.\n\t * Each parameter name counts once; repeated occurrences do not count here.\n\t */",
            "\t/**\n\t * 返回 SQL 语句中命名参数的数量。\n\t * 每个参数名只计一次，重复出现不计入。\n\t */",
        ),
        (
            "\t/**\n\t * Set the count of all the unnamed parameters in the SQL statement.\n\t */",
            "\t/**\n\t * 设置 SQL 语句中所有未命名参数的数量。\n\t */",
        ),
        (
            "\t/**\n\t * Return the count of all the unnamed parameters in the SQL statement.\n\t */",
            "\t/**\n\t * 返回 SQL 语句中所有未命名参数的数量。\n\t */",
        ),
        (
            "\t/**\n\t * Set the total count of all the parameters in the SQL statement.\n\t * Repeated occurrences of the same parameter name do count here.\n\t */",
            "\t/**\n\t * 设置 SQL 语句中所有参数的总数。\n\t * 同一参数名的重复出现也计入。\n\t */",
        ),
        (
            "\t/**\n\t * Return the total count of all the parameters in the SQL statement.\n\t * Repeated occurrences of the same parameter name do count here.\n\t */",
            "\t/**\n\t * 返回 SQL 语句中所有参数的总数。\n\t * 同一参数名的重复出现也计入。\n\t */",
        ),
        (
            "\t/**\n\t * Exposes the original SQL String.\n\t */",
            "\t/**\n\t * 暴露原始 SQL 字符串。\n\t */",
        ),
    ],
    "SimplePropertySqlParameterSource.java": [
        (
            "/**\n * {@link SqlParameterSource} implementation that obtains parameter values\n * from bean properties of a given JavaBean object, from component accessors\n * of a record class, or from raw field access.\n *\n * <p>This is a more flexible variant of {@link BeanPropertySqlParameterSource},\n * with the limitation that it is not able to enumerate its\n * {@link #getParameterNames() parameter names}.\n *\n * <p>In terms of its fallback property discovery algorithm, this class is\n * similar to {@link org.springframework.validation.SimpleErrors} which is\n * also just used for property retrieval purposes (rather than binding).\n *\n * @author Juergen Hoeller\n * @since 6.1\n * @see NamedParameterJdbcTemplate\n * @see BeanPropertySqlParameterSource\n * @see org.springframework.jdbc.core.simple.JdbcClient.StatementSpec#paramSource(Object)\n * @see org.springframework.jdbc.core.SimplePropertyRowMapper\n */",
            "/**\n * 从给定 JavaBean 的 bean 属性、record 类的组件访问器\n * 或原始字段访问获取参数值的 {@link SqlParameterSource} 实现。\n *\n * <p>这是 {@link BeanPropertySqlParameterSource} 更灵活的变体，\n * 限制在于无法枚举其 {@link #getParameterNames() 参数名称}。\n *\n * <p>在回退属性发现算法方面，本类与\n * {@link org.springframework.validation.SimpleErrors} 类似，\n * 同样仅用于属性检索（而非绑定）。\n *\n * @author Juergen Hoeller\n * @since 6.1\n * @see NamedParameterJdbcTemplate\n * @see BeanPropertySqlParameterSource\n * @see org.springframework.jdbc.core.simple.JdbcClient.StatementSpec#paramSource(Object)\n * @see org.springframework.jdbc.core.SimplePropertyRowMapper\n */",
        ),
        (
            "\t/**\n\t * Create a new SqlParameterSource for the given bean, record or field holder.\n\t * @param paramObject the bean, record or field holder instance to wrap\n\t */",
            "\t/**\n\t * 为给定 bean、record 或字段持有者创建新的 SqlParameterSource。\n\t * @param paramObject 要包装的 bean、record 或字段持有者实例\n\t */",
        ),
        (
            "\t/**\n\t * Derives a default SQL type from the corresponding property type.\n\t * @see StatementCreatorUtils#javaTypeToSqlParameterType\n\t */",
            "\t/**\n\t * 从对应属性类型推导默认 SQL 类型。\n\t * @see StatementCreatorUtils#javaTypeToSqlParameterType\n\t */",
        ),
    ],
    "SqlParameterSource.java": [
        (
            "/**\n * Interface that defines common functionality for objects that can\n * offer parameter values for named SQL parameters, serving as argument\n * for {@link NamedParameterJdbcTemplate} operations.\n *\n * <p>This interface allows for the specification of SQL type in addition\n * to parameter values. All parameter values and types are identified by\n * specifying the name of the parameter.\n *\n * <p>Intended to wrap various implementations like a Map or a JavaBean\n * with a consistent interface.\n *\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @since 2.0\n * @see NamedParameterJdbcOperations\n * @see NamedParameterJdbcTemplate\n * @see MapSqlParameterSource\n * @see BeanPropertySqlParameterSource\n */",
            "/**\n * 定义可为命名 SQL 参数提供参数值的对象的通用功能接口，\n * 作为 {@link NamedParameterJdbcTemplate} 操作的参数。\n *\n * <p>本接口除参数值外还允许指定 SQL 类型。\n * 所有参数值和类型均通过参数名称标识。\n *\n * <p>旨在用一致接口包装 Map、JavaBean 等多种实现。\n *\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @since 2.0\n * @see NamedParameterJdbcOperations\n * @see NamedParameterJdbcTemplate\n * @see MapSqlParameterSource\n * @see BeanPropertySqlParameterSource\n */",
        ),
        (
            "\t/**\n\t * Constant that indicates an unknown (or unspecified) SQL type.\n\t * To be returned from {@code getType} when no specific SQL type known.\n\t * @see #getSqlType\n\t * @see java.sql.Types\n\t */",
            "\t/**\n\t * 表示未知（或未指定）SQL 类型的常量。\n\t * 当无特定 SQL 类型时由 {@code getType} 返回。\n\t * @see #getSqlType\n\t * @see java.sql.Types\n\t */",
        ),
        (
            "\t/**\n\t * Determine whether there is a value for the specified named parameter.\n\t * @param paramName the name of the parameter\n\t * @return whether there is a value defined\n\t */",
            "\t/**\n\t * 判断指定命名参数是否有值。\n\t * @param paramName 参数名称\n\t * @return 是否已定义值\n\t */",
        ),
        (
            "\t/**\n\t * Return the parameter value for the requested named parameter.\n\t * @param paramName the name of the parameter\n\t * @return the value of the specified parameter\n\t * @throws IllegalArgumentException if there is no value for the requested parameter\n\t */",
            "\t/**\n\t * 返回请求的命名参数的值。\n\t * @param paramName 参数名称\n\t * @return 指定参数的值\n\t * @throws IllegalArgumentException 若请求的参数无值\n\t */",
        ),
        (
            "\t/**\n\t * Determine the SQL type for the specified named parameter.\n\t * @param paramName the name of the parameter\n\t * @return the SQL type of the specified parameter,\n\t * or {@code TYPE_UNKNOWN} if not known\n\t * @see #TYPE_UNKNOWN\n\t */",
            "\t/**\n\t * 确定指定命名参数的 SQL 类型。\n\t * @param paramName 参数名称\n\t * @return 指定参数的 SQL 类型，未知时返回 {@code TYPE_UNKNOWN}\n\t * @see #TYPE_UNKNOWN\n\t */",
        ),
        (
            "\t/**\n\t * Determine the type name for the specified named parameter.\n\t * @param paramName the name of the parameter\n\t * @return the type name of the specified parameter,\n\t * or {@code null} if not known\n\t */",
            "\t/**\n\t * 确定指定命名参数的类型名称。\n\t * @param paramName 参数名称\n\t * @return 指定参数的类型名称，未知时返回 {@code null}\n\t */",
        ),
        (
            "\t/**\n\t * Enumerate all available parameter names if possible.\n\t * <p>This is an optional operation, primarily for use with\n\t * {@link org.springframework.jdbc.core.simple.SimpleJdbcInsert}\n\t * and {@link org.springframework.jdbc.core.simple.SimpleJdbcCall}.\n\t * @return the array of parameter names, or {@code null} if not determinable\n\t * @since 5.0.3\n\t * @see SqlParameterSourceUtils#extractCaseInsensitiveParameterNames\n\t */",
            "\t/**\n\t * 若可能，枚举所有可用参数名称。\n\t * <p>此为可选操作，主要用于\n\t * {@link org.springframework.jdbc.core.simple.SimpleJdbcInsert}\n\t * 和 {@link org.springframework.jdbc.core.simple.SimpleJdbcCall}。\n\t * @return 参数名称数组，无法确定时返回 {@code null}\n\t * @since 5.0.3\n\t * @see SqlParameterSourceUtils#extractCaseInsensitiveParameterNames\n\t */",
        ),
    ],
    "SqlParameterSourceUtils.java": [
        (
            "/**\n * Class that provides helper methods for the use of {@link SqlParameterSource},\n * in particular with {@link NamedParameterJdbcTemplate}.\n *\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @since 2.5\n */",
            "/**\n * 为 {@link SqlParameterSource} 的使用提供辅助方法的类，\n * 尤其配合 {@link NamedParameterJdbcTemplate} 使用。\n *\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @since 2.5\n */",
        ),
        (
            "\t/**\n\t * Create an array of {@link SqlParameterSource} objects populated with data\n\t * from the values passed in (either a {@link Map} or a bean object).\n\t * This will define what is included in a batch operation.\n\t * @param candidates object array of objects containing the values to be used\n\t * @return an array of {@link SqlParameterSource}\n\t * @see MapSqlParameterSource\n\t * @see BeanPropertySqlParameterSource\n\t * @see NamedParameterJdbcTemplate#batchUpdate(String, SqlParameterSource[])\n\t */",
            "\t/**\n\t * 创建 {@link SqlParameterSource} 对象数组，用传入值（{@link Map} 或 bean 对象）填充。\n\t * 这将定义批操作中包含的内容。\n\t * @param candidates 包含待使用值的对象数组\n\t * @return {@link SqlParameterSource} 数组\n\t * @see MapSqlParameterSource\n\t * @see BeanPropertySqlParameterSource\n\t * @see NamedParameterJdbcTemplate#batchUpdate(String, SqlParameterSource[])\n\t */",
        ),
        (
            "\t/**\n\t * Create an array of {@link SqlParameterSource} objects populated with data\n\t * from the values passed in (either a {@link Map} or a bean object).\n\t * This will define what is included in a batch operation.\n\t * @param candidates collection of objects containing the values to be used\n\t * @return an array of {@link SqlParameterSource}\n\t * @since 5.0.2\n\t * @see MapSqlParameterSource\n\t * @see BeanPropertySqlParameterSource\n\t * @see NamedParameterJdbcTemplate#batchUpdate(String, SqlParameterSource[])\n\t */",
            "\t/**\n\t * 创建 {@link SqlParameterSource} 对象数组，用传入值（{@link Map} 或 bean 对象）填充。\n\t * 这将定义批操作中包含的内容。\n\t * @param candidates 包含待使用值的集合\n\t * @return {@link SqlParameterSource} 数组\n\t * @since 5.0.2\n\t * @see MapSqlParameterSource\n\t * @see BeanPropertySqlParameterSource\n\t * @see NamedParameterJdbcTemplate#batchUpdate(String, SqlParameterSource[])\n\t */",
        ),
        (
            "\t/**\n\t * Create an array of {@link MapSqlParameterSource} objects populated with data from\n\t * the values passed in. This will define what is included in a batch operation.\n\t * @param valueMaps array of {@link Map} instances containing the values to be used\n\t * @return an array of {@link SqlParameterSource}\n\t * @see MapSqlParameterSource\n\t * @see NamedParameterJdbcTemplate#batchUpdate(String, Map[])\n\t */",
            "\t/**\n\t * 创建 {@link MapSqlParameterSource} 对象数组，用传入值填充。\n\t * 这将定义批操作中包含的内容。\n\t * @param valueMaps 包含待使用值的 {@link Map} 实例数组\n\t * @return {@link SqlParameterSource} 数组\n\t * @see MapSqlParameterSource\n\t * @see NamedParameterJdbcTemplate#batchUpdate(String, Map[])\n\t */",
        ),
        (
            "\t/**\n\t * Create a wrapped value if parameter has type information, plain object if not.\n\t * @param source the source of parameter values and type information\n\t * @param parameterName the name of the parameter\n\t * @return the value object\n\t * @see SqlParameterValue\n\t */",
            "\t/**\n\t * 若参数有类型信息则创建包装值，否则返回普通对象。\n\t * @param source 参数值和类型信息的来源\n\t * @param parameterName 参数名称\n\t * @return 值对象\n\t * @see SqlParameterValue\n\t */",
        ),
        (
            "\t/**\n\t * Create a Map of case-insensitive parameter names together with the original name.\n\t * @param parameterSource the source of parameter names\n\t * @return the Map that can be used for case-insensitive matching of parameter names\n\t */",
            "\t/**\n\t * 创建不区分大小写的参数名称与原始名称的 Map。\n\t * @param parameterSource 参数名称来源\n\t * @return 可用于不区分大小写匹配参数名称的 Map\n\t */",
        ),
    ],
}
