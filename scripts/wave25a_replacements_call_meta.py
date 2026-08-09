"""Chinese JavaDoc replacements for springframework wave25a call metadata interfaces."""

CALL_META_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "CallMetaDataProvider.java": [
        (
            "/**\n * Interface specifying the API to be implemented by a class providing call meta-data.\n *\n * <p>This is intended for internal use by Spring's\n * {@link org.springframework.jdbc.core.simple.SimpleJdbcCall}.\n *\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @author Giuseppe Milicia\n * @since 2.5\n */",
            "/**\n * 提供调用元数据的类须实现的 API 接口。\n *\n * <p>供 Spring 的\n * {@link org.springframework.jdbc.core.simple.SimpleJdbcCall} 内部使用。\n *\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @author Giuseppe Milicia\n * @since 2.5\n */",
        ),
        (
            "\t/**\n\t * Initialize using the provided DatabaseMetData.\n\t * @param databaseMetaData used to retrieve database specific information\n\t * @throws SQLException in case of initialization failure\n\t */",
            "\t/**\n\t * 使用提供的 DatabaseMetaData 初始化。\n\t * @param databaseMetaData 用于获取数据库特定信息\n\t * @throws SQLException 初始化失败时\n\t */",
        ),
        (
            "\t/**\n\t * Initialize the database specific management of procedure column meta-data.\n\t * <p>This is only called for databases that are supported. This initialization\n\t * can be turned off by specifying that column meta-data should not be used.\n\t * @param databaseMetaData used to retrieve database specific information\n\t * @param catalogName name of catalog to use (or {@code null} if none)\n\t * @param schemaName name of schema name to use (or {@code null} if none)\n\t * @param procedureName name of the stored procedure\n\t * @throws SQLException in case of initialization failure\n\t * @see\torg.springframework.jdbc.core.simple.SimpleJdbcCall#withoutProcedureColumnMetaDataAccess()\n\t */",
            "\t/**\n\t * 初始化数据库特定的存储过程列元数据管理。\n\t * <p>仅对受支持的数据库调用；可通过指定不使用列元数据关闭。\n\t * @param databaseMetaData 用于获取数据库特定信息\n\t * @param catalogName 要使用的 catalog 名（无则 {@code null}）\n\t * @param schemaName 要使用的 schema 名（无则 {@code null}）\n\t * @param procedureName 存储过程名\n\t * @throws SQLException 初始化失败时\n\t * @see\torg.springframework.jdbc.core.simple.SimpleJdbcCall#withoutProcedureColumnMetaDataAccess()\n\t */",
        ),
        (
            "\t/**\n\t * Get the call parameter meta-data that is currently used.\n\t * @return a List of {@link CallParameterMetaData}\n\t */",
            "\t/**\n\t * 获取当前使用的调用参数元数据。\n\t * @return {@link CallParameterMetaData} 列表\n\t */",
        ),
        (
            "\t/**\n\t * Provide any modification of the procedure name passed in to match the meta-data currently used.\n\t * <p>This could include altering the case.\n\t */",
            "\t/**\n\t * 对传入的过程名做必要修改以匹配当前元数据。\n\t * <p>可能包括调整大小写。\n\t */",
        ),
        (
            "\t/**\n\t * Provide any modification of the catalog name passed in to match the meta-data currently used.\n\t * <p>This could include altering the case.\n\t */",
            "\t/**\n\t * 对传入的 catalog 名做必要修改以匹配当前元数据。\n\t * <p>可能包括调整大小写。\n\t */",
        ),
        (
            "\t/**\n\t * Provide any modification of the schema name passed in to match the meta-data currently used.\n\t * <p>This could include altering the case.\n\t */",
            "\t/**\n\t * 对传入的 schema 名做必要修改以匹配当前元数据。\n\t * <p>可能包括调整大小写。\n\t */",
        ),
        (
            "\t/**\n\t * Provide any modification of the catalog name passed in to match the meta-data currently used.\n\t * <p>The returned value will be used for meta-data lookups. This could include altering the case\n\t * used or providing a base catalog if none is provided.\n\t */",
            "\t/**\n\t * 对传入的 catalog 名做必要修改以匹配当前元数据。\n\t * <p>返回值用于元数据查找；可能调整大小写或在未提供时使用默认 catalog。\n\t */",
        ),
        (
            "\t/**\n\t * Provide any modification of the schema name passed in to match the meta-data currently used.\n\t * <p>The returned value will be used for meta-data lookups. This could include altering the case\n\t * used or providing a base schema if none is provided.\n\t */",
            "\t/**\n\t * 对传入的 schema 名做必要修改以匹配当前元数据。\n\t * <p>返回值用于元数据查找；可能调整大小写或在未提供时使用默认 schema。\n\t */",
        ),
        (
            "\t/**\n\t * Provide any modification of the column name passed in to match the meta-data currently used.\n\t * <p>This could include altering the case.\n\t * @param parameterName name of the parameter of column\n\t */",
            "\t/**\n\t * 对传入的列名做必要修改以匹配当前元数据。\n\t * <p>可能包括调整大小写。\n\t * @param parameterName 参数或列名\n\t */",
        ),
        (
            "\t/**\n\t * Return the name of the named parameter to use for binding the given parameter name.\n\t * @param parameterName the name of the parameter to bind\n\t * @return the name of the named parameter to use for binding the given parameter name\n\t * @since 6.1.2\n\t */",
            "\t/**\n\t * 返回用于绑定给定参数名的命名参数名。\n\t * @param parameterName 待绑定参数名\n\t * @return 用于绑定的命名参数名\n\t * @since 6.1.2\n\t */",
        ),
        (
            "\t/**\n\t * Create a default out parameter based on the provided meta-data.\n\t * <p>This is used when no explicit parameter declaration has been made.\n\t * @param parameterName the name of the parameter\n\t * @param meta meta-data used for this call\n\t * @return the configured SqlOutParameter\n\t */",
            "\t/**\n\t * 根据提供的元数据创建默认 OUT 参数。\n\t * <p>未显式声明参数时使用。\n\t * @param parameterName 参数名\n\t * @param meta 本次调用的元数据\n\t * @return 配置好的 SqlOutParameter\n\t */",
        ),
        (
            "\t/**\n\t * Create a default in/out parameter based on the provided meta-data.\n\t * <p>This is used when no explicit parameter declaration has been made.\n\t * @param parameterName the name of the parameter\n\t * @param meta meta-data used for this call\n\t * @return the configured SqlInOutParameter\n\t */",
            "\t/**\n\t * 根据提供的元数据创建默认 IN/OUT 参数。\n\t * <p>未显式声明参数时使用。\n\t * @param parameterName 参数名\n\t * @param meta 本次调用的元数据\n\t * @return 配置好的 SqlInOutParameter\n\t */",
        ),
        (
            "\t/**\n\t * Create a default in parameter based on the provided meta-data.\n\t * <p>This is used when no explicit parameter declaration has been made.\n\t * @param parameterName the name of the parameter\n\t * @param meta meta-data used for this call\n\t * @return the configured SqlParameter\n\t */",
            "\t/**\n\t * 根据提供的元数据创建默认 IN 参数。\n\t * <p>未显式声明参数时使用。\n\t * @param parameterName 参数名\n\t * @param meta 本次调用的元数据\n\t * @return 配置好的 SqlParameter\n\t */",
        ),
        (
            "\t/**\n\t * Get the name of the current user. Useful for meta-data lookups etc.\n\t * @return current user name from database connection\n\t */",
            "\t/**\n\t * 获取当前用户名，用于元数据查找等。\n\t * @return 数据库连接上的当前用户名\n\t */",
        ),
        (
            "\t/**\n\t * Are we using the meta-data for the procedure columns?\n\t */",
            "\t/**\n\t * 是否使用存储过程列元数据？\n\t */",
        ),
        (
            "\t/**\n\t * Does this database support returning ResultSets that should be retrieved with the JDBC call:\n\t * {@link java.sql.Statement#getResultSet()}?\n\t */",
            "\t/**\n\t * 本数据库是否支持通过 JDBC 调用\n\t * {@link java.sql.Statement#getResultSet()} 获取返回的 ResultSet？\n\t */",
        ),
        (
            "\t/**\n\t * Does this database support returning ResultSets as ref cursors to be retrieved with\n\t * {@link java.sql.CallableStatement#getObject(int)} for the specified column?\n\t */",
            "\t/**\n\t * 本数据库是否支持将 ResultSet 作为 ref cursor 返回，\n\t * 并通过 {@link java.sql.CallableStatement#getObject(int)} 按列读取？\n\t */",
        ),
        (
            "\t/**\n\t * Get the {@link java.sql.Types} type for columns that return ResultSets as ref cursors\n\t * if this feature is supported.\n\t */",
            "\t/**\n\t * 若支持 ref cursor，返回以 ResultSet 形式返回的列的\n\t * {@link java.sql.Types} 类型。\n\t */",
        ),
        (
            "\t/**\n\t * Should we bypass the return parameter with the specified name?\n\t * <p>This allows the database specific implementation to skip the processing\n\t * for specific results returned by the database call.\n\t */",
            "\t/**\n\t * 是否跳过指定名称的返回参数？\n\t * <p>允许数据库特定实现跳过对调用返回的特定结果的处理。\n\t */",
        ),
        (
            "\t/**\n\t * Does the database support the use of catalog name in procedure calls?\n\t */",
            "\t/**\n\t * 数据库是否支持在过程调用中使用 catalog 名？\n\t */",
        ),
        (
            "\t/**\n\t * Does the database support the use of schema name in procedure calls?\n\t */",
            "\t/**\n\t * 数据库是否支持在过程调用中使用 schema 名？\n\t */",
        ),
    ],
    "CallMetaDataProviderFactory.java": [
        (
            "/**\n * Factory used to create a {@link CallMetaDataProvider} implementation\n * based on the type of database being used.\n *\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @author Sam Brannen\n * @since 2.5\n */",
            "/**\n * 根据所用数据库类型创建 {@link CallMetaDataProvider} 实现的工厂。\n *\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @author Sam Brannen\n * @since 2.5\n */",
        ),
        (
            "\t/** List of supported database products for procedure calls. */",
            "\t/** 支持存储过程调用的数据库产品列表。 */",
        ),
        (
            "\t/** List of supported database products for function calls. */",
            "\t/** 支持函数调用的数据库产品列表。 */",
        ),
        (
            "\t/**\n\t * Create a {@link CallMetaDataProvider} based on the database meta-data.\n\t * @param dataSource the JDBC DataSource to use for retrieving meta-data\n\t * @param context the class that holds configuration and meta-data\n\t * @return instance of the CallMetaDataProvider implementation to be used\n\t */",
            "\t/**\n\t * 根据数据库元数据创建 {@link CallMetaDataProvider}。\n\t * @param dataSource 用于获取元数据的 JDBC DataSource\n\t * @param context 持有配置与元数据的上下文类\n\t * @return 要使用的 CallMetaDataProvider 实现实例\n\t */",
        ),
    ],
    "CallParameterMetaData.java": [
        (
            "/**\n * Holder of meta-data for a specific parameter that is used for call processing.\n *\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @since 2.5\n * @see GenericCallMetaDataProvider\n */",
            "/**\n * 用于调用处理的特定参数元数据持有者。\n *\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @since 2.5\n * @see GenericCallMetaDataProvider\n */",
        ),
        (
            "\t/**\n\t * Constructor taking all the properties including the function marker.\n\t * @since 5.2.9\n\t */",
            "\t/**\n\t * 接收全部属性（含函数标记）的构造函数。\n\t * @since 5.2.9\n\t */",
        ),
        (
            "\t/**\n\t * Return whether this parameter is declared in a function.\n\t * @since 5.2.9\n\t */",
            "\t/**\n\t * 返回本参数是否声明于函数中。\n\t * @since 5.2.9\n\t */",
        ),
        (
            "\t/**\n\t * Return the parameter name.\n\t */",
            "\t/**\n\t * 返回参数名。\n\t */",
        ),
        (
            "\t/**\n\t * Return the parameter type.\n\t */",
            "\t/**\n\t * 返回参数类型。\n\t */",
        ),
        (
            "\t/**\n\t * Determine whether the declared parameter qualifies as a 'return' parameter\n\t * for our purposes: type {@link DatabaseMetaData#procedureColumnReturn} or\n\t * {@link DatabaseMetaData#procedureColumnResult}, or in case of a function,\n\t * {@link DatabaseMetaData#functionReturn}.\n\t * @since 4.3.15\n\t */",
            "\t/**\n\t * 判断声明的参数是否视为「返回」参数：\n\t * 类型为 {@link DatabaseMetaData#procedureColumnReturn} 或\n\t * {@link DatabaseMetaData#procedureColumnResult}；函数则为\n\t * {@link DatabaseMetaData#functionReturn}。\n\t * @since 4.3.15\n\t */",
        ),
        (
            "\t/**\n\t * Determine whether the declared parameter qualifies as an 'out' parameter\n\t * for our purposes: type {@link DatabaseMetaData#procedureColumnOut},\n\t * or in case of a function, {@link DatabaseMetaData#functionColumnOut}.\n\t * @since 5.3.31\n\t */",
            "\t/**\n\t * 判断声明的参数是否视为 OUT 参数：\n\t * 类型为 {@link DatabaseMetaData#procedureColumnOut}；\n\t * 函数则为 {@link DatabaseMetaData#functionColumnOut}。\n\t * @since 5.3.31\n\t */",
        ),
        (
            "\t/**\n\t * Determine whether the declared parameter qualifies as an 'in-out' parameter\n\t * for our purposes: type {@link DatabaseMetaData#procedureColumnInOut},\n\t * or in case of a function, {@link DatabaseMetaData#functionColumnInOut}.\n\t * @since 5.3.31\n\t */",
            "\t/**\n\t * 判断声明的参数是否视为 IN/OUT 参数：\n\t * 类型为 {@link DatabaseMetaData#procedureColumnInOut}；\n\t * 函数则为 {@link DatabaseMetaData#functionColumnInOut}。\n\t * @since 5.3.31\n\t */",
        ),
        (
            "\t/**\n\t * Return the parameter SQL type.\n\t */",
            "\t/**\n\t * 返回参数的 SQL 类型。\n\t */",
        ),
        (
            "\t/**\n\t * Return the parameter type name.\n\t */",
            "\t/**\n\t * 返回参数类型名。\n\t */",
        ),
        (
            "\t/**\n\t * Return whether the parameter is nullable.\n\t */",
            "\t/**\n\t * 返回参数是否可为 null。\n\t */",
        ),
    ],
}
