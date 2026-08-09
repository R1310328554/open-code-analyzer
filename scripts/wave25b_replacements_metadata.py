"""Chinese JavaDoc replacements for springframework wave25b metadata classes."""

METADATA_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "SqlServerCallMetaDataProvider.java": [
        (
            "/**\n * SQL Server specific implementation for the {@link CallMetaDataProvider} interface.\n * This class is intended for internal use by the Simple JDBC classes.\n *\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @since 2.5\n */",
            "/**\n * {@link CallMetaDataProvider} 接口的 SQL Server 专用实现。\n * 本类供 Simple JDBC 类内部使用。\n *\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @since 2.5\n */",
        ),
    ],
    "SybaseCallMetaDataProvider.java": [
        (
            "/**\n * Sybase specific implementation for the {@link CallMetaDataProvider} interface.\n * This class is intended for internal use by the Simple JDBC classes.\n *\n * @author Thomas Risberg\n * @author Giuseppe Milicia\n * @since 2.5\n */",
            "/**\n * {@link CallMetaDataProvider} 接口的 Sybase 专用实现。\n * 本类供 Simple JDBC 类内部使用。\n *\n * @author Thomas Risberg\n * @author Giuseppe Milicia\n * @since 2.5\n */",
        ),
    ],
    "TableMetaDataProvider.java": [
        (
            "/**\n * Interface specifying the API to be implemented by a class providing table meta-data.\n *\n * <p>This is intended for internal use by the Simple JDBC classes.\n *\n * @author Thomas Risberg\n * @author Sam Brannen\n * @since 2.5\n */",
            "/**\n * 定义提供表元数据的类应实现的 API 接口。\n *\n * <p>供 Simple JDBC 类内部使用。\n *\n * @author Thomas Risberg\n * @author Sam Brannen\n * @since 2.5\n */",
        ),
        (
            "\t/**\n\t * Initialize using the database meta-data provided.\n\t * @param databaseMetaData used to retrieve database specific information\n\t * @throws SQLException in case of initialization failure\n\t */",
            "\t/**\n\t * 使用提供的数据库元数据进行初始化。\n\t * @param databaseMetaData 用于获取数据库特定信息\n\t * @throws SQLException 初始化失败时抛出\n\t */",
        ),
        (
            "\t/**\n\t * Initialize using provided database meta-data, table and column information.\n\t * <p>This initialization can be turned off by specifying that column meta-data\n\t * should not be used.\n\t * @param databaseMetaData used to retrieve database specific information\n\t * @param catalogName name of catalog to use (or {@code null} if none)\n\t * @param schemaName name of schema name to use (or {@code null} if none)\n\t * @param tableName name of the table\n\t * @throws SQLException in case of initialization failure\n\t */",
            "\t/**\n\t * 使用提供的数据库元数据、表和列信息进行初始化。\n\t * <p>可通过指定不使用列元数据来关闭此初始化。\n\t * @param databaseMetaData 用于获取数据库特定信息\n\t * @param catalogName 要使用的 catalog 名称（无则为 {@code null}）\n\t * @param schemaName 要使用的 schema 名称（无则为 {@code null}）\n\t * @param tableName 表名\n\t * @throws SQLException 初始化失败时抛出\n\t */",
        ),
        (
            "\t/**\n\t * Get the table parameter meta-data that is currently used.\n\t * @return a List of {@link TableParameterMetaData}\n\t */",
            "\t/**\n\t * 获取当前使用的表参数元数据。\n\t * @return {@link TableParameterMetaData} 列表\n\t */",
        ),
        (
            "\t/**\n\t * Get the table name formatted based on meta-data information.\n\t * <p>This could include altering the case.\n\t */",
            "\t/**\n\t * 根据元数据信息获取格式化后的表名。\n\t * <p>可能包括大小写转换。\n\t */",
        ),
        (
            "\t/**\n\t * Get the column name formatted based on meta-data information.\n\t * <p>This could include altering the case.\n\t * @since 6.1\n\t */",
            "\t/**\n\t * 根据元数据信息获取格式化后的列名。\n\t * <p>可能包括大小写转换。\n\t * @since 6.1\n\t */",
        ),
        (
            "\t/**\n\t * Get the catalog name formatted based on meta-data information.\n\t * <p>This could include altering the case.\n\t */",
            "\t/**\n\t * 根据元数据信息获取格式化后的 catalog 名称。\n\t * <p>可能包括大小写转换。\n\t */",
        ),
        (
            "\t/**\n\t * Get the schema name formatted based on meta-data information.\n\t * <p>This could include altering the case.\n\t */",
            "\t/**\n\t * 根据元数据信息获取格式化后的 schema 名称。\n\t * <p>可能包括大小写转换。\n\t */",
        ),
        (
            "\t/**\n\t * Provide any modification of the catalog name passed in to match the meta-data\n\t * currently used.\n\t * <p>The returned value will be used for meta-data lookups.\n\t * <p>This could include altering the case used or providing a base catalog\n\t * if none is provided.\n\t */",
            "\t/**\n\t * 对传入的 catalog 名称进行必要修改，以匹配当前使用的元数据。\n\t * <p>返回值将用于元数据查找。\n\t * <p>可能包括调整大小写，或在未提供时给出默认 catalog。\n\t */",
        ),
        (
            "\t/**\n\t * Provide any modification of the schema name passed in to match the meta-data\n\t * currently used.\n\t * <p>The returned value will be used for meta-data lookups.\n\t * <p>This could include altering the case used or providing a base schema\n\t * if none is provided.\n\t */",
            "\t/**\n\t * 对传入的 schema 名称进行必要修改，以匹配当前使用的元数据。\n\t * <p>返回值将用于元数据查找。\n\t * <p>可能包括调整大小写，或在未提供时给出默认 schema。\n\t */",
        ),
        (
            "\t/**\n\t * Are we using the meta-data for the table columns?\n\t */",
            "\t/**\n\t * 是否正在使用表列的元数据？\n\t */",
        ),
        (
            "\t/**\n\t * Does this database support the JDBC feature for retrieving generated keys?\n\t * @see java.sql.DatabaseMetaData#supportsGetGeneratedKeys()\n\t */",
            "\t/**\n\t * 此数据库是否支持 JDBC 获取生成键的特性？\n\t * @see java.sql.DatabaseMetaData#supportsGetGeneratedKeys()\n\t */",
        ),
        (
            "\t/**\n\t * Does this database support a simple query to retrieve generated keys when\n\t * the JDBC feature for retrieving generated keys is not supported?\n\t * @see #isGetGeneratedKeysSupported()\n\t * @see #getSimpleQueryForGetGeneratedKey(String, String)\n\t */",
            "\t/**\n\t * 当 JDBC 不支持获取生成键时，此数据库是否支持通过简单查询获取生成键？\n\t * @see #isGetGeneratedKeysSupported()\n\t * @see #getSimpleQueryForGetGeneratedKey(String, String)\n\t */",
        ),
        (
            "\t/**\n\t * Get the simple query to retrieve generated keys when the JDBC feature for\n\t * retrieving generated keys is not supported.\n\t * @see #isGetGeneratedKeysSimulated()\n\t */",
            "\t/**\n\t * 获取在 JDBC 不支持获取生成键时用于检索生成键的简单查询。\n\t * @see #isGetGeneratedKeysSimulated()\n\t */",
        ),
        (
            "\t/**\n\t * Does this database support a column name String array for retrieving generated keys?\n\t * @see java.sql.Connection#createStruct(String, Object[])\n\t */",
            "\t/**\n\t * 此数据库是否支持通过列名字符串数组获取生成键？\n\t * @see java.sql.Connection#createStruct(String, Object[])\n\t */",
        ),
        (
            "\t/**\n\t * Get the string used to quote SQL identifiers.\n\t * <p>This method returns a space ({@code \" \"}) if identifier quoting is not supported.\n\t * @return database identifier quote string\n\t * @since 6.1\n\t * @see DatabaseMetaData#getIdentifierQuoteString()\n\t */",
            "\t/**\n\t * 获取用于引用 SQL 标识符的字符串。\n\t * <p>若不支持标识符引用，则返回空格（{@code \" \"}）。\n\t * @return 数据库标识符引用字符串\n\t * @since 6.1\n\t * @see DatabaseMetaData#getIdentifierQuoteString()\n\t */",
        ),
    ],
    "TableMetaDataProviderFactory.java": [
        (
            "/**\n * Factory used to create a {@link TableMetaDataProvider} implementation\n * based on the type of database being used.\n *\n * @author Thomas Risberg\n * @since 2.5\n */",
            "/**\n * 根据所用数据库类型创建 {@link TableMetaDataProvider} 实现的工厂。\n *\n * @author Thomas Risberg\n * @since 2.5\n */",
        ),
        (
            "\t/**\n\t * Create a {@link TableMetaDataProvider} based on the database meta-data.\n\t * @param dataSource used to retrieve meta-data\n\t * @param context the class that holds configuration and meta-data\n\t * @return instance of the TableMetaDataProvider implementation to be used\n\t */",
            "\t/**\n\t * 根据数据库元数据创建 {@link TableMetaDataProvider}。\n\t * @param dataSource 用于获取元数据\n\t * @param context 持有配置和元数据的上下文类\n\t * @return 要使用的 TableMetaDataProvider 实现实例\n\t */",
        ),
    ],
    "TableParameterMetaData.java": [
        (
            "/**\n * Holder of meta-data for a specific parameter that is used for table processing.\n *\n * @author Thomas Risberg\n * @since 2.5\n * @see GenericTableMetaDataProvider\n */",
            "/**\n * 用于表处理的特定参数的元数据持有者。\n *\n * @author Thomas Risberg\n * @since 2.5\n * @see GenericTableMetaDataProvider\n */",
        ),
        (
            "\t/**\n\t * Constructor taking all the properties.\n\t */",
            "\t/**\n\t * 接受所有属性的构造函数。\n\t */",
        ),
        (
            "\t/**\n\t * Get the parameter name.\n\t */",
            "\t/**\n\t * 获取参数名称。\n\t */",
        ),
        (
            "\t/**\n\t * Get the parameter SQL type.\n\t */",
            "\t/**\n\t * 获取参数的 SQL 类型。\n\t */",
        ),
        (
            "\t/**\n\t * Get whether the parameter/column is nullable.\n\t */",
            "\t/**\n\t * 获取参数/列是否可为 null。\n\t */",
        ),
    ],
}
