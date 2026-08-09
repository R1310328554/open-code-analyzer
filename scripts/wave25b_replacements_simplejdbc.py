"""Chinese JavaDoc replacements for springframework wave25b SimpleJdbc classes."""

SIMPLEJDBC_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "SimpleJdbcCall.java": [
        (
            "/**\n * A SimpleJdbcCall is a multithreaded, reusable object representing a call\n * to a stored procedure or a stored function. It provides meta-data processing\n * to simplify the code needed to access basic stored procedures/functions.\n * All you need to provide is the name of the procedure/function and a Map\n * containing the parameters when you execute the call. The names of the\n * supplied parameters will be matched up with in and out parameters declared\n * when the stored procedure was created.\n *\n * <p>The meta-data processing is based on the DatabaseMetaData provided by\n * the JDBC driver. Since we rely on the JDBC driver, this \"auto-detection\"\n * can only be used for databases that are known to provide accurate meta-data.\n * These currently include Derby, MySQL, Microsoft SQL Server, Oracle, DB2,\n * Sybase and PostgreSQL. For any other databases you are required to declare\n * all parameters explicitly. You can of course declare all parameters\n * explicitly even if the database provides the necessary meta-data. In that\n * case your declared parameters will take precedence. You can also turn off\n * any meta-data processing if you want to use parameter names that do not\n * match what is declared during the stored procedure compilation.\n *\n * <p>The actual call is being handled using Spring's {@link JdbcTemplate}.\n *\n * <p>Many of the configuration methods return the current instance of the\n * SimpleJdbcCall in order to provide the ability to chain multiple ones\n * together in a \"fluent\" interface style.\n *\n * @author Thomas Risberg\n * @author Stephane Nicoll\n * @since 2.5\n * @see java.sql.DatabaseMetaData\n * @see org.springframework.jdbc.core.JdbcTemplate\n */",
            "/**\n * SimpleJdbcCall 是表示对存储过程或存储函数调用的多线程、可复用对象。\n * 它提供元数据处理，简化访问基本存储过程/函数所需的代码。\n * 执行调用时只需提供过程/函数名称和包含参数的 Map。\n * 所供参数名称将与创建存储过程时声明的入参和出参匹配。\n *\n * <p>元数据处理基于 JDBC 驱动提供的 DatabaseMetaData。\n * 由于依赖 JDBC 驱动，此\"自动检测\"仅适用于已知提供准确元数据的数据库。\n * 目前包括 Derby、MySQL、Microsoft SQL Server、Oracle、DB2、\n * Sybase 和 PostgreSQL。其他数据库须显式声明所有参数。\n * 即使数据库提供必要元数据，也可显式声明所有参数，\n * 此时声明的参数优先。若需使用与存储过程编译时声明不匹配的参数名，\n * 也可关闭所有元数据处理。\n *\n * <p>实际调用通过 Spring 的 {@link JdbcTemplate} 处理。\n *\n * <p>许多配置方法返回 SimpleJdbcCall 当前实例，\n * 以便以\"流式\"接口风格链式调用。\n *\n * @author Thomas Risberg\n * @author Stephane Nicoll\n * @since 2.5\n * @see java.sql.DatabaseMetaData\n * @see org.springframework.jdbc.core.JdbcTemplate\n */",
        ),
        (
            "\t/**\n\t * Constructor that takes one parameter with the JDBC DataSource to use when\n\t * creating the underlying JdbcTemplate.\n\t * @param dataSource the {@code DataSource} to use\n\t * @see org.springframework.jdbc.core.JdbcTemplate#setDataSource\n\t */",
            "\t/**\n\t * 接受 JDBC DataSource 参数的构造函数，用于创建底层 JdbcTemplate。\n\t * @param dataSource 要使用的 {@code DataSource}\n\t * @see org.springframework.jdbc.core.JdbcTemplate#setDataSource\n\t */",
        ),
        (
            "\t/**\n\t * Alternative Constructor that takes one parameter with the JdbcTemplate to be used.\n\t * @param jdbcTemplate the {@code JdbcTemplate} to use\n\t * @see org.springframework.jdbc.core.JdbcTemplate#setDataSource\n\t */",
            "\t/**\n\t * 接受 JdbcTemplate 参数的替代构造函数。\n\t * @param jdbcTemplate 要使用的 {@code JdbcTemplate}\n\t * @see org.springframework.jdbc.core.JdbcTemplate#setDataSource\n\t */",
        ),
    ],
    "SimpleJdbcCallOperations.java": [
        (
            "/**\n * Interface specifying the API for a Simple JDBC Call implemented by {@link SimpleJdbcCall}.\n * This interface is not often used directly, but provides the option to enhance testability,\n * as it can easily be mocked or stubbed.\n *\n * @author Thomas Risberg\n * @author Stephane Nicoll\n * @since 2.5\n */",
            "/**\n * 定义 {@link SimpleJdbcCall} 实现的 Simple JDBC Call API 接口。\n * 本接口不常直接使用，但可增强可测试性，\n * 因其易于 mock 或 stub。\n *\n * @author Thomas Risberg\n * @author Stephane Nicoll\n * @since 2.5\n */",
        ),
        (
            "\t/**\n\t * Specify the procedure name to be used - this implies that we will be calling a stored procedure.\n\t * @param procedureName the name of the stored procedure\n\t * @return the instance of this SimpleJdbcCall\n\t */",
            "\t/**\n\t * 指定要使用的存储过程名称——表示将调用存储过程。\n\t * @param procedureName 存储过程名称\n\t * @return 此 SimpleJdbcCall 实例\n\t */",
        ),
        (
            "\t/**\n\t * Specify the procedure name to be used - this implies that we will be calling a stored function.\n\t * @param functionName the name of the stored function\n\t * @return the instance of this SimpleJdbcCall\n\t */",
            "\t/**\n\t * 指定要使用的函数名称——表示将调用存储函数。\n\t * @param functionName 存储函数名称\n\t * @return 此 SimpleJdbcCall 实例\n\t */",
        ),
        (
            "\t/**\n\t * Optionally, specify the name of the schema that contains the stored procedure.\n\t * @param schemaName the name of the schema\n\t * @return the instance of this SimpleJdbcCall\n\t */",
            "\t/**\n\t * 可选地指定包含存储过程的 schema 名称。\n\t * @param schemaName schema 名称\n\t * @return 此 SimpleJdbcCall 实例\n\t */",
        ),
        (
            "\t/**\n\t * Optionally, specify the name of the catalog that contains the stored procedure.\n\t * <p>To provide consistency with the Oracle DatabaseMetaData, this is used to specify the\n\t * package name if the procedure is declared as part of a package.\n\t * @param catalogName the catalog or package name\n\t * @return the instance of this SimpleJdbcCall\n\t */",
            "\t/**\n\t * 可选地指定包含存储过程的 catalog 名称。\n\t * <p>为与 Oracle DatabaseMetaData 保持一致，\n\t * 若过程作为包的一部分声明，则用于指定包名。\n\t * @param catalogName catalog 或包名\n\t * @return 此 SimpleJdbcCall 实例\n\t */",
        ),
        (
            "\t/**\n\t * Indicates the procedure's return value should be included in the results returned.\n\t * @return the instance of this SimpleJdbcCall\n\t */",
            "\t/**\n\t * 指示过程的返回值应包含在返回结果中。\n\t * @return 此 SimpleJdbcCall 实例\n\t */",
        ),
        (
            "\t/**\n\t * Specify one or more parameters if desired. These parameters will be supplemented with\n\t * any parameter information retrieved from the database meta-data.\n\t * <p>Note that only parameters declared as {@code SqlParameter} and {@code SqlInOutParameter}\n\t * will be used to provide input values. This is different from the {@code StoredProcedure}\n\t * class which - for backwards compatibility reasons - allows input values to be provided\n\t * for parameters declared as {@code SqlOutParameter}.\n\t * @param sqlParameters the parameters to use\n\t * @return the instance of this SimpleJdbcCall\n\t */",
            "\t/**\n\t * 按需指定一个或多个参数。这些参数将补充从数据库元数据获取的参数信息。\n\t * <p>注意：仅声明为 {@code SqlParameter} 和 {@code SqlInOutParameter} 的参数\n\t * 用于提供输入值。这与 {@code StoredProcedure} 类不同——\n\t * 后者出于向后兼容允许为声明为 {@code SqlOutParameter} 的参数提供输入值。\n\t * @param sqlParameters 要使用的参数\n\t * @return 此 SimpleJdbcCall 实例\n\t */",
        ),
        (
            "\t/** Not used yet. */",
            "\t/** 尚未使用。 */",
        ),
        (
            "\t/**\n\t * Used to specify when a ResultSet is returned by the stored procedure and you want it\n\t * mapped by a {@link RowMapper}. The results will be returned using the parameter name\n\t * specified. Multiple ResultSets must be declared in the correct order.\n\t * <p>If the database you are using uses ref cursors then the name specified must match\n\t * the name of the parameter declared for the procedure in the database.\n\t * @param parameterName the name of the returned results and/or the name of the ref cursor parameter\n\t * @param rowMapper the RowMapper implementation that will map the data returned for each row\n\t * */",
            "\t/**\n\t * 用于指定存储过程返回 ResultSet 且需由 {@link RowMapper} 映射时。\n\t * 结果将使用指定的参数名称返回。多个 ResultSet 须按正确顺序声明。\n\t * <p>若所用数据库使用 ref cursor，则指定名称须与\n\t * 数据库中为过程声明的参数名称匹配。\n\t * @param parameterName 返回结果的名称和/或 ref cursor 参数名称\n\t * @param rowMapper 映射每行返回数据的 RowMapper 实现\n\t * */",
        ),
        (
            "\t/**\n\t * Turn off any processing of parameter meta-data information obtained via JDBC.\n\t * @return the instance of this SimpleJdbcCall\n\t */",
            "\t/**\n\t * 关闭通过 JDBC 获取的参数元数据信息的任何处理。\n\t * @return 此 SimpleJdbcCall 实例\n\t */",
        ),
        (
            "\t/**\n\t * Indicates that parameters should be bound by name.\n\t * @return the instance of this SimpleJdbcCall\n\t * @since 4.2\n\t */",
            "\t/**\n\t * 指示参数应按名称绑定。\n\t * @return 此 SimpleJdbcCall 实例\n\t * @since 4.2\n\t */",
        ),
        (
            "\t/**\n\t * Execute the stored function and return the results obtained as an Object of the\n\t * specified return type.\n\t * @param returnType the type of the value to return\n\t * @param args optional array containing the in parameter values to be used in the call.\n\t * Parameter values must be provided in the same order as the parameters are defined\n\t * for the stored procedure.\n\t */",
            "\t/**\n\t * 执行存储函数并以指定返回类型的 Object 返回结果。\n\t * @param returnType 要返回值的类型\n\t * @param args 可选数组，包含调用中使用的入参值。\n\t * 参数值须与存储过程定义的参数顺序一致。\n\t */",
        ),
        (
            "\t/**\n\t * Execute the stored function and return the results obtained as an Object of the\n\t * specified return type.\n\t * @param returnType the type of the value to return\n\t * @param args a Map containing the parameter values to be used in the call\n\t */",
            "\t/**\n\t * 执行存储函数并以指定返回类型的 Object 返回结果。\n\t * @param returnType 要返回值的类型\n\t * @param args 包含调用中使用的参数值的 Map\n\t */",
        ),
        (
            "\t/**\n\t * Execute the stored function and return the results obtained as an Object of the\n\t * specified return type.\n\t * @param returnType the type of the value to return\n\t * @param args the MapSqlParameterSource containing the parameter values to be used in the call\n\t */",
            "\t/**\n\t * 执行存储函数并以指定返回类型的 Object 返回结果。\n\t * @param returnType 要返回值的类型\n\t * @param args 包含调用中使用的参数值的 MapSqlParameterSource\n\t */",
        ),
        (
            "\t/**\n\t * Execute the stored procedure and return the single out parameter as an Object\n\t * of the specified return type. In the case where there are multiple out parameters,\n\t * the first one is returned and additional out parameters are ignored.\n\t * @param returnType the type of the value to return\n\t * @param args optional array containing the in parameter values to be used in the call.\n\t * Parameter values must be provided in the same order as the parameters are defined for\n\t * the stored procedure.\n\t */",
            "\t/**\n\t * 执行存储过程并以指定返回类型的 Object 返回单个出参。\n\t * 若有多个出参，返回第一个，其余忽略。\n\t * @param returnType 要返回值的类型\n\t * @param args 可选数组，包含调用中使用的入参值。\n\t * 参数值须与存储过程定义的参数顺序一致。\n\t */",
        ),
        (
            "\t/**\n\t * Execute the stored procedure and return the single out parameter as an Object\n\t * of the specified return type. In the case where there are multiple out parameters,\n\t * the first one is returned and additional out parameters are ignored.\n\t * @param returnType the type of the value to return\n\t * @param args a Map containing the parameter values to be used in the call\n\t */",
            "\t/**\n\t * 执行存储过程并以指定返回类型的 Object 返回单个出参。\n\t * 若有多个出参，返回第一个，其余忽略。\n\t * @param returnType 要返回值的类型\n\t * @param args 包含调用中使用的参数值的 Map\n\t */",
        ),
        (
            "\t/**\n\t * Execute the stored procedure and return the single out parameter as an Object\n\t * of the specified return type. In the case where there are multiple out parameters,\n\t * the first one is returned and additional out parameters are ignored.\n\t * @param returnType the type of the value to return\n\t * @param args the MapSqlParameterSource containing the parameter values to be used in the call\n\t */",
            "\t/**\n\t * 执行存储过程并以指定返回类型的 Object 返回单个出参。\n\t * 若有多个出参，返回第一个，其余忽略。\n\t * @param returnType 要返回值的类型\n\t * @param args 包含调用中使用的参数值的 MapSqlParameterSource\n\t */",
        ),
        (
            "\t/**\n\t * Execute the stored procedure and return a map of output params, keyed by name\n\t * as in parameter declarations.\n\t * @param args optional array containing the in parameter values to be used in the call.\n\t * Parameter values must be provided in the same order as the parameters are defined for\n\t * the stored procedure.\n\t * @return a Map of output params\n\t */",
            "\t/**\n\t * 执行存储过程并返回出参 Map，键为参数声明中的名称。\n\t * @param args 可选数组，包含调用中使用的入参值。\n\t * 参数值须与存储过程定义的参数顺序一致。\n\t * @return 出参 Map\n\t */",
        ),
        (
            "\t/**\n\t * Execute the stored procedure and return a map of output params, keyed by name\n\t * as in parameter declarations.\n\t * @param args a Map containing the parameter values to be used in the call\n\t * @return a Map of output params\n\t */",
            "\t/**\n\t * 执行存储过程并返回出参 Map，键为参数声明中的名称。\n\t * @param args 包含调用中使用的参数值的 Map\n\t * @return 出参 Map\n\t */",
        ),
        (
            "\t/**\n\t * Execute the stored procedure and return a map of output params, keyed by name\n\t * as in parameter declarations.\n\t * @param args the SqlParameterSource containing the parameter values to be used in the call\n\t * @return a Map of output params\n\t */",
            "\t/**\n\t * 执行存储过程并返回出参 Map，键为参数声明中的名称。\n\t * @param args 包含调用中使用的参数值的 SqlParameterSource\n\t * @return 出参 Map\n\t */",
        ),
    ],
    "SimpleJdbcInsert.java": [
        (
            "/**\n * A {@code SimpleJdbcInsert} is a multi-threaded, reusable object providing easy\n * (batch) insert capabilities for a table. It provides meta-data processing to\n * simplify the code needed to construct a basic insert statement. All you need\n * to provide is the name of the table and a {@code Map} containing the column\n * names and the column values.\n *\n * <p>The meta-data processing is based on the {@code DatabaseMetaData} provided\n * by the JDBC driver. As long as the JDBC driver can provide the names of the columns\n * for a specified table then we can rely on this auto-detection feature. If that\n * is not the case, then the column names must be specified explicitly.\n *\n * <p>The actual (batch) insert is handled using Spring's {@link JdbcTemplate}.\n *\n * <p>Many of the configuration methods return the current instance of the\n * {@code SimpleJdbcInsert} to provide the ability to chain multiple ones together\n * in a \"fluent\" API style.\n *\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @author Sam Brannen\n * @since 2.5\n * @see java.sql.DatabaseMetaData\n * @see org.springframework.jdbc.core.JdbcTemplate\n */",
            "/**\n * {@code SimpleJdbcInsert} 是为表提供简便（批量）插入能力的多线程、可复用对象。\n * 它提供元数据处理，简化构建基本 insert 语句所需的代码。\n * 只需提供表名和包含列名与列值的 {@code Map}。\n *\n * <p>元数据处理基于 JDBC 驱动提供的 {@code DatabaseMetaData}。\n * 只要 JDBC 驱动能为指定表提供列名，即可依赖此自动检测特性。\n * 否则须显式指定列名。\n *\n * <p>实际（批量）插入通过 Spring 的 {@link JdbcTemplate} 处理。\n *\n * <p>许多配置方法返回 {@code SimpleJdbcInsert} 当前实例，\n * 以便以\"流式\" API 风格链式调用。\n *\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @author Sam Brannen\n * @since 2.5\n * @see java.sql.DatabaseMetaData\n * @see org.springframework.jdbc.core.JdbcTemplate\n */",
        ),
        (
            "\t/**\n\t * Constructor that accepts the JDBC {@link DataSource} to use when creating\n\t * the {@link JdbcTemplate}.\n\t * @param dataSource the {@code DataSource} to use\n\t * @see org.springframework.jdbc.core.JdbcTemplate#setDataSource\n\t */",
            "\t/**\n\t * 接受 JDBC {@link DataSource} 的构造函数，用于创建 {@link JdbcTemplate}。\n\t * @param dataSource 要使用的 {@code DataSource}\n\t * @see org.springframework.jdbc.core.JdbcTemplate#setDataSource\n\t */",
        ),
        (
            "\t/**\n\t * Alternative constructor that accepts the {@link JdbcTemplate} to be used.\n\t * @param jdbcTemplate the {@code JdbcTemplate} to use\n\t * @see org.springframework.jdbc.core.JdbcTemplate#setDataSource\n\t */",
            "\t/**\n\t * 接受 {@link JdbcTemplate} 的替代构造函数。\n\t * @param jdbcTemplate 要使用的 {@code JdbcTemplate}\n\t * @see org.springframework.jdbc.core.JdbcTemplate#setDataSource\n\t */",
        ),
    ],
    "SimpleJdbcInsertOperations.java": [
        (
            "/**\n * Interface specifying the API for a Simple JDBC Insert implemented by {@link SimpleJdbcInsert}.\n *\n * <p>This interface is not often used directly, but provides the option to enhance testability,\n * as it can easily be mocked or stubbed.\n *\n * @author Thomas Risberg\n * @author Sam Brannen\n * @since 2.5\n */",
            "/**\n * 定义 {@link SimpleJdbcInsert} 实现的 Simple JDBC Insert API 接口。\n *\n * <p>本接口不常直接使用，但可增强可测试性，\n * 因其易于 mock 或 stub。\n *\n * @author Thomas Risberg\n * @author Sam Brannen\n * @since 2.5\n */",
        ),
        (
            "\t/**\n\t * Specify the table name to be used for the insert.\n\t * @param tableName the name of the stored table\n\t * @return this {@code SimpleJdbcInsert} (for method chaining)\n\t */",
            "\t/**\n\t * 指定 insert 使用的表名。\n\t * @param tableName 存储表名称\n\t * @return 此 {@code SimpleJdbcInsert}（用于方法链式调用）\n\t */",
        ),
        (
            "\t/**\n\t * Specify the schema name, if any, to be used for the insert.\n\t * @param schemaName the name of the schema\n\t * @return this {@code SimpleJdbcInsert} (for method chaining)\n\t */",
            "\t/**\n\t * 指定 insert 使用的 schema 名称（若有）。\n\t * @param schemaName schema 名称\n\t * @return 此 {@code SimpleJdbcInsert}（用于方法链式调用）\n\t */",
        ),
        (
            "\t/**\n\t * Specify the catalog name, if any, to be used for the insert.\n\t * @param catalogName the name of the catalog\n\t * @return this {@code SimpleJdbcInsert} (for method chaining)\n\t */",
            "\t/**\n\t * 指定 insert 使用的 catalog 名称（若有）。\n\t * @param catalogName catalog 名称\n\t * @return 此 {@code SimpleJdbcInsert}（用于方法链式调用）\n\t */",
        ),
        (
            "\t/**\n\t * Specify the column names that the insert statement should be limited to use.\n\t * @param columnNames one or more column names\n\t * @return this {@code SimpleJdbcInsert} (for method chaining)\n\t */",
            "\t/**\n\t * 指定 insert 语句应限制使用的列名。\n\t * @param columnNames 一个或多个列名\n\t * @return 此 {@code SimpleJdbcInsert}（用于方法链式调用）\n\t */",
        ),
        (
            "\t/**\n\t * Specify the names of any columns that have auto-generated keys.\n\t * @param columnNames one or more column names\n\t * @return this {@code SimpleJdbcInsert} (for method chaining)\n\t */",
            "\t/**\n\t * 指定具有自动生成键的列名。\n\t * @param columnNames 一个或多个列名\n\t * @return 此 {@code SimpleJdbcInsert}（用于方法链式调用）\n\t */",
        ),
        (
            "\t/**\n\t * Specify that SQL identifiers should be quoted.\n\t * <p>If this method is invoked, the identifier quote string for the underlying\n\t * database will be used to quote SQL identifiers in generated SQL statements.\n\t * In this context, SQL identifiers refer to schema, table, and column names.\n\t * <p>When identifiers are quoted, explicit column names must be supplied via\n\t * {@link #usingColumns(String...)}. Furthermore, all identifiers for the\n\t * schema name, table name, and column names must match the corresponding\n\t * identifiers in the database's metadata regarding casing (mixed case,\n\t * uppercase, or lowercase).\n\t * @return this {@code SimpleJdbcInsert} (for method chaining)\n\t * @since 6.1\n\t * @see #withSchemaName(String)\n\t * @see #withTableName(String)\n\t * @see #usingColumns(String...)\n\t * @see java.sql.DatabaseMetaData#getIdentifierQuoteString()\n\t * @see java.sql.DatabaseMetaData#storesMixedCaseIdentifiers()\n\t * @see java.sql.DatabaseMetaData#storesMixedCaseQuotedIdentifiers()\n\t * @see java.sql.DatabaseMetaData#storesUpperCaseIdentifiers()\n\t * @see java.sql.DatabaseMetaData#storesUpperCaseQuotedIdentifiers()\n\t * @see java.sql.DatabaseMetaData#storesLowerCaseIdentifiers()\n\t * @see java.sql.DatabaseMetaData#storesLowerCaseQuotedIdentifiers()\n\t */",
            "\t/**\n\t * 指定应对 SQL 标识符加引号。\n\t * <p>调用此方法后，将使用底层数据库的标识符引用字符串\n\t * 为生成 SQL 语句中的 SQL 标识符加引号。\n\t * 此处 SQL 标识符指 schema、表和列名。\n\t * <p>标识符加引号时，须通过 {@link #usingColumns(String...)} 显式提供列名。\n\t * 此外，schema 名、表名和列名的所有标识符\n\t * 须与数据库元数据中对应标识符的大小写（混合、大写或小写）一致。\n\t * @return 此 {@code SimpleJdbcInsert}（用于方法链式调用）\n\t * @since 6.1\n\t * @see #withSchemaName(String)\n\t * @see #withTableName(String)\n\t * @see #usingColumns(String...)\n\t * @see java.sql.DatabaseMetaData#getIdentifierQuoteString()\n\t * @see java.sql.DatabaseMetaData#storesMixedCaseIdentifiers()\n\t * @see java.sql.DatabaseMetaData#storesMixedCaseQuotedIdentifiers()\n\t * @see java.sql.DatabaseMetaData#storesUpperCaseIdentifiers()\n\t * @see java.sql.DatabaseMetaData#storesUpperCaseQuotedIdentifiers()\n\t * @see java.sql.DatabaseMetaData#storesLowerCaseIdentifiers()\n\t * @see java.sql.DatabaseMetaData#storesLowerCaseQuotedIdentifiers()\n\t */",
        ),
        (
            "\t/**\n\t * Turn off any processing of column meta-data information obtained via JDBC.\n\t * @return this {@code SimpleJdbcInsert} (for method chaining)\n\t */",
            "\t/**\n\t * 关闭通过 JDBC 获取的列元数据信息的任何处理。\n\t * @return 此 {@code SimpleJdbcInsert}（用于方法链式调用）\n\t */",
        ),
        (
            "\t/**\n\t * Include synonyms for the column meta-data lookups via JDBC.\n\t * <p>Note: This is only necessary to include for Oracle since other databases\n\t * supporting synonyms seem to include the synonyms automatically.\n\t * @return this {@code SimpleJdbcInsert} (for method chaining)\n\t */",
            "\t/**\n\t * 在通过 JDBC 查找列元数据时包含同义词。\n\t * <p>注意：仅 Oracle 需要显式包含，其他支持同义词的数据库似乎会自动包含。\n\t * @return 此 {@code SimpleJdbcInsert}（用于方法链式调用）\n\t */",
        ),
        (
            "\t/**\n\t * Execute the insert using the values passed in.\n\t * @param args a Map containing column names and corresponding value\n\t * @return the number of rows affected as returned by the JDBC driver\n\t */",
            "\t/**\n\t * 使用传入的值执行 insert。\n\t * @param args 包含列名和对应值的 Map\n\t * @return JDBC 驱动返回的影响行数\n\t */",
        ),
        (
            "\t/**\n\t * Execute the insert using the values passed in.\n\t * @param parameterSource the SqlParameterSource containing values to use for insert\n\t * @return the number of rows affected as returned by the JDBC driver\n\t */",
            "\t/**\n\t * 使用传入的值执行 insert。\n\t * @param parameterSource 包含 insert 所用值的 SqlParameterSource\n\t * @return JDBC 驱动返回的影响行数\n\t */",
        ),
        (
            "\t/**\n\t * Execute the insert using the values passed in and return the generated key.\n\t * <p>This requires that the name of the columns with auto generated keys have been specified.\n\t * This method will always return a KeyHolder but the caller must verify that it actually\n\t * contains the generated keys.\n\t * @param args a Map containing column names and corresponding value\n\t * @return the generated key value\n\t */",
            "\t/**\n\t * 使用传入的值执行 insert 并返回生成的键。\n\t * <p>须已指定具有自动生成键的列名。\n\t * 本方法始终返回 KeyHolder，但调用方须验证其确实包含生成的键。\n\t * @param args 包含列名和对应值的 Map\n\t * @return 生成的键值\n\t */",
        ),
        (
            "\t/**\n\t * Execute the insert using the values passed in and return the generated key.\n\t * <p>This requires that the name of the columns with auto generated keys have been specified.\n\t * This method will always return a KeyHolder but the caller must verify that it actually\n\t * contains the generated keys.\n\t * @param parameterSource the SqlParameterSource containing values to use for insert\n\t * @return the generated key value.\n\t */",
            "\t/**\n\t * 使用传入的值执行 insert 并返回生成的键。\n\t * <p>须已指定具有自动生成键的列名。\n\t * 本方法始终返回 KeyHolder，但调用方须验证其确实包含生成的键。\n\t * @param parameterSource 包含 insert 所用值的 SqlParameterSource\n\t * @return 生成的键值\n\t */",
        ),
        (
            "\t/**\n\t * Execute the insert using the values passed in and return the generated keys.\n\t * <p>This requires that the name of the columns with auto generated keys have been specified.\n\t * This method will always return a KeyHolder but the caller must verify that it actually\n\t * contains the generated keys.\n\t * @param args a Map containing column names and corresponding value\n\t * @return the KeyHolder containing all generated keys\n\t */",
            "\t/**\n\t * 使用传入的值执行 insert 并返回生成的键。\n\t * <p>须已指定具有自动生成键的列名。\n\t * 本方法始终返回 KeyHolder，但调用方须验证其确实包含生成的键。\n\t * @param args 包含列名和对应值的 Map\n\t * @return 包含所有生成键的 KeyHolder\n\t */",
        ),
        (
            "\t/**\n\t * Execute the insert using the values passed in and return the generated keys.\n\t * <p>This requires that the name of the columns with auto generated keys have been specified.\n\t * This method will always return a KeyHolder but the caller must verify that it actually\n\t * contains the generated keys.\n\t * @param parameterSource the SqlParameterSource containing values to use for insert\n\t * @return the KeyHolder containing all generated keys\n\t */",
            "\t/**\n\t * 使用传入的值执行 insert 并返回生成的键。\n\t * <p>须已指定具有自动生成键的列名。\n\t * 本方法始终返回 KeyHolder，但调用方须验证其确实包含生成的键。\n\t * @param parameterSource 包含 insert 所用值的 SqlParameterSource\n\t * @return 包含所有生成键的 KeyHolder\n\t */",
        ),
        (
            "\t/**\n\t * Execute a batch insert using the batch of values passed in.\n\t * @param batch an array of Maps containing a batch of column names and corresponding value\n\t * @return the array of number of rows affected as returned by the JDBC driver\n\t */",
            "\t/**\n\t * 使用传入的批量值执行批量 insert。\n\t * @param batch 包含批量列名和对应值的 Map 数组\n\t * @return JDBC 驱动返回的影响行数数组\n\t */",
        ),
        (
            "\t/**\n\t * Execute a batch insert using the batch of values passed in.\n\t * @param batch an array of SqlParameterSource containing values for the batch\n\t * @return the array of number of rows affected as returned by the JDBC driver\n\t */",
            "\t/**\n\t * 使用传入的批量值执行批量 insert。\n\t * @param batch 包含批量值的 SqlParameterSource 数组\n\t * @return JDBC 驱动返回的影响行数数组\n\t */",
        ),
    ],
}
