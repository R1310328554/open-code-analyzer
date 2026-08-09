"""Chinese JavaDoc replacements for springframework wave26a jdbc/core/support classes."""

SUPPORT_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "AbstractLobStreamingResultSetExtractor.java": [
        (
            "/**\n * Abstract ResultSetExtractor implementation that assumes streaming of LOB data.\n * Typically used as inner class, with access to surrounding method arguments.\n *\n * <p>Delegates to the {@code streamData} template method for streaming LOB\n * content to some OutputStream, typically using a LobHandler. Converts an\n * IOException thrown during streaming to a LobRetrievalFailureException.\n *\n * <p>A usage example with JdbcTemplate:\n *\n * <pre class=\"code\">JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);  // reusable object\n * final LobHandler lobHandler = new DefaultLobHandler();  // reusable object\n *\n * jdbcTemplate.query(\n *\t   \"SELECT content FROM imagedb WHERE image_name=?\", new Object[] {name},\n *\t   new AbstractLobStreamingResultSetExtractor() {\n *\t     public void streamData(ResultSet rs) throws SQLException, IOException {\n *         FileCopyUtils.copy(lobHandler.getBlobAsBinaryStream(rs, 1), contentStream);\n *       }\n *     });\n * </pre>\n *\n * @author Juergen Hoeller\n * @since 1.0.2\n * @param <T> the result type\n * @see org.springframework.jdbc.support.lob.LobHandler\n * @see org.springframework.jdbc.LobRetrievalFailureException\n * @deprecated as of 6.2 along with {@link org.springframework.jdbc.support.lob.LobHandler},\n * in favor of {@link ResultSet#getBinaryStream}/{@link ResultSet#getCharacterStream} usage\n */",
            "/**\n * 假定以流式方式读取 LOB 数据的抽象 {@link ResultSetExtractor} 实现。\n * 通常作为内部类使用，可访问外围方法参数。\n *\n * <p>委托 {@code streamData} 模板方法将 LOB 内容流式写入 OutputStream，\n * 通常借助 LobHandler。流式读取期间抛出的 IOException 会转换为 LobRetrievalFailureException。\n *\n * <p>与 JdbcTemplate 配合使用的示例：\n *\n * <pre class=\"code\">JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);  // 可复用对象\n * final LobHandler lobHandler = new DefaultLobHandler();  // 可复用对象\n *\n * jdbcTemplate.query(\n *\t   \"SELECT content FROM imagedb WHERE image_name=?\", new Object[] {name},\n *\t   new AbstractLobStreamingResultSetExtractor() {\n *\t     public void streamData(ResultSet rs) throws SQLException, IOException {\n *         FileCopyUtils.copy(lobHandler.getBlobAsBinaryStream(rs, 1), contentStream);\n *       }\n *     });\n * </pre>\n *\n * @author Juergen Hoeller\n * @since 1.0.2\n * @param <T> 结果类型\n * @see org.springframework.jdbc.support.lob.LobHandler\n * @see org.springframework.jdbc.LobRetrievalFailureException\n * @deprecated 自 6.2 起与 {@link org.springframework.jdbc.support.lob.LobHandler} 一并弃用，\n * 建议使用 {@link ResultSet#getBinaryStream}/{@link ResultSet#getCharacterStream}\n */",
        ),
        (
            "\t/**\n\t * Delegates to handleNoRowFound, handleMultipleRowsFound and streamData,\n\t * according to the ResultSet state. Converts an IOException thrown by\n\t * streamData to a LobRetrievalFailureException.\n\t * @see #handleNoRowFound\n\t * @see #handleMultipleRowsFound\n\t * @see #streamData\n\t * @see org.springframework.jdbc.LobRetrievalFailureException\n\t */",
            "\t/**\n\t * 根据 ResultSet 状态分别委托 handleNoRowFound、handleMultipleRowsFound 和 streamData。\n\t * 将 streamData 抛出的 IOException 转换为 LobRetrievalFailureException。\n\t * @see #handleNoRowFound\n\t * @see #handleMultipleRowsFound\n\t * @see #streamData\n\t * @see org.springframework.jdbc.LobRetrievalFailureException\n\t */",
        ),
        (
            "\t/**\n\t * Handle the case where the ResultSet does not contain a row.\n\t * @throws DataAccessException a corresponding exception,\n\t * by default an EmptyResultDataAccessException\n\t * @see org.springframework.dao.EmptyResultDataAccessException\n\t */",
            "\t/**\n\t * 处理 ResultSet 不包含任何行的情况。\n\t * @throws DataAccessException 对应异常，默认抛出 EmptyResultDataAccessException\n\t * @see org.springframework.dao.EmptyResultDataAccessException\n\t */",
        ),
        (
            "\t/**\n\t * Handle the case where the ResultSet contains multiple rows.\n\t * @throws DataAccessException a corresponding exception,\n\t * by default an IncorrectResultSizeDataAccessException\n\t * @see org.springframework.dao.IncorrectResultSizeDataAccessException\n\t */",
            "\t/**\n\t * 处理 ResultSet 包含多行的情况。\n\t * @throws DataAccessException 对应异常，默认抛出 IncorrectResultSizeDataAccessException\n\t * @see org.springframework.dao.IncorrectResultSizeDataAccessException\n\t */",
        ),
        (
            "\t/**\n\t * Stream LOB content from the given ResultSet to some OutputStream.\n\t * <p>Typically used as inner class, with access to surrounding method arguments\n\t * and to a LobHandler instance variable of the surrounding class.\n\t * @param rs the ResultSet to take the LOB content from\n\t * @throws SQLException if thrown by JDBC methods\n\t * @throws IOException if thrown by stream access methods\n\t * @throws DataAccessException in case of custom exceptions\n\t * @see org.springframework.jdbc.support.lob.LobHandler#getBlobAsBinaryStream\n\t * @see org.springframework.util.FileCopyUtils\n\t */",
            "\t/**\n\t * 从给定 ResultSet 将 LOB 内容流式写入 OutputStream。\n\t * <p>通常作为内部类使用，可访问外围方法参数及外围类的 LobHandler 实例变量。\n\t * @param rs 读取 LOB 内容的 ResultSet\n\t * @throws SQLException JDBC 方法抛出时\n\t * @throws IOException 流访问方法抛出时\n\t * @throws DataAccessException 自定义异常时\n\t * @see org.springframework.jdbc.support.lob.LobHandler#getBlobAsBinaryStream\n\t * @see org.springframework.util.FileCopyUtils\n\t */",
        ),
    ],
    "AbstractSqlTypeValue.java": [
        (
            "/**\n * Abstract implementation of the SqlTypeValue interface, for convenient\n * creation of type values that are supposed to be passed into the\n * {@code PreparedStatement.setObject} method. The {@code createTypeValue}\n * callback method has access to the underlying Connection, if that should\n * be needed to create any database-specific objects.\n *\n * <p>A usage example from a StoredProcedure (compare this to the plain\n * SqlTypeValue version in the superclass javadoc):\n *\n * <pre class=\"code\">proc.declareParameter(new SqlParameter(\"myarray\", Types.ARRAY, \"NUMBERS\"));\n * ...\n *\n * Map&lt;String, Object&gt; in = new HashMap&lt;String, Object&gt;();\n * in.put(\"myarray\", new AbstractSqlTypeValue() {\n *   public Object createTypeValue(Connection con, int sqlType, String typeName) throws SQLException {\n *\t   oracle.sql.ArrayDescriptor desc = new oracle.sql.ArrayDescriptor(typeName, con);\n *\t   return new oracle.sql.ARRAY(desc, con, seats);\n *   }\n * });\n * Map out = execute(in);\n * </pre>\n *\n * @author Juergen Hoeller\n * @since 1.1\n * @see java.sql.PreparedStatement#setObject(int, Object, int)\n * @see org.springframework.jdbc.object.StoredProcedure\n */",
            "/**\n * {@link SqlTypeValue} 接口的抽象实现，便于创建应传入\n * {@code PreparedStatement.setObject} 方法的类型值。\n * {@code createTypeValue} 回调可访问底层 Connection，以便创建数据库特定对象。\n *\n * <p>StoredProcedure 中的使用示例（可与父类 javadoc 中的普通 SqlTypeValue 写法对比）：\n *\n * <pre class=\"code\">proc.declareParameter(new SqlParameter(\"myarray\", Types.ARRAY, \"NUMBERS\"));\n * ...\n *\n * Map&lt;String, Object&gt; in = new HashMap&lt;String, Object&gt;();\n * in.put(\"myarray\", new AbstractSqlTypeValue() {\n *   public Object createTypeValue(Connection con, int sqlType, String typeName) throws SQLException {\n *\t   oracle.sql.ArrayDescriptor desc = new oracle.sql.ArrayDescriptor(typeName, con);\n *\t   return new oracle.sql.ARRAY(desc, con, seats);\n *   }\n * });\n * Map out = execute(in);\n * </pre>\n *\n * @author Juergen Hoeller\n * @since 1.1\n * @see java.sql.PreparedStatement#setObject(int, Object, int)\n * @see org.springframework.jdbc.object.StoredProcedure\n */",
        ),
        (
            "\t/**\n\t * Create the type value to be passed into {@code PreparedStatement.setObject}.\n\t * @param con the JDBC Connection, if needed to create any database-specific objects\n\t * @param sqlType the SQL type of the parameter we are setting\n\t * @param typeName the type name of the parameter\n\t * @return the type value\n\t * @throws SQLException if an SQLException is encountered setting\n\t * parameter values (that is, there's no need to catch SQLException)\n\t * @see java.sql.PreparedStatement#setObject(int, Object, int)\n\t */",
            "\t/**\n\t * 创建要传入 {@code PreparedStatement.setObject} 的类型值。\n\t * @param con JDBC Connection，创建数据库特定对象时可能需要\n\t * @param sqlType 要设置的参数的 SQL 类型\n\t * @param typeName 参数的类型名称\n\t * @return 类型值\n\t * @throws SQLException 设置参数值时遇到 SQLException（无需捕获）\n\t * @see java.sql.PreparedStatement#setObject(int, Object, int)\n\t */",
        ),
    ],
    "JdbcBeanDefinitionReader.java": [
        (
            "/**\n * Bean definition reader that reads values from a database table,\n * based on a given SQL statement.\n *\n * <p>Expects columns for bean name, property name and value as String.\n * Formats for each are identical to the properties format recognized\n * by PropertiesBeanDefinitionReader.\n *\n * <p><b>NOTE:</b> This is mainly intended as an example for a custom\n * JDBC-based bean definition reader. It does not aim to offer\n * comprehensive functionality.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @see #loadBeanDefinitions\n * @deprecated in favor of Spring's common bean definition formats and/or\n * custom BeanDefinitionReader implementations\n */",
            "/**\n * 基于给定 SQL 语句从数据库表读取值的 Bean 定义读取器。\n *\n * <p>期望三列分别为 bean 名称、属性名和字符串形式的属性值。\n * 各列格式与 PropertiesBeanDefinitionReader 所识别的 properties 格式相同。\n *\n * <p><b>注意：</b> 此类主要作为自定义 JDBC Bean 定义读取器的示例，\n * 并不提供完整功能。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @see #loadBeanDefinitions\n * @deprecated 建议使用 Spring 通用 Bean 定义格式和/或自定义 BeanDefinitionReader 实现\n */",
        ),
        (
            "\t/**\n\t * Create a new JdbcBeanDefinitionReader for the given bean factory,\n\t * using a default PropertiesBeanDefinitionReader underneath.\n\t * <p>DataSource or JdbcTemplate still need to be set.\n\t * @see #setDataSource\n\t * @see #setJdbcTemplate\n\t */",
            "\t/**\n\t * 为给定 Bean 工厂创建新的 JdbcBeanDefinitionReader，\n\t * 底层使用默认 PropertiesBeanDefinitionReader。\n\t * <p>仍需设置 DataSource 或 JdbcTemplate。\n\t * @see #setDataSource\n\t * @see #setJdbcTemplate\n\t */",
        ),
        (
            "\t/**\n\t * Create a new JdbcBeanDefinitionReader that delegates to the\n\t * given PropertiesBeanDefinitionReader underneath.\n\t * <p>DataSource or JdbcTemplate still need to be set.\n\t * @see #setDataSource\n\t * @see #setJdbcTemplate\n\t */",
            "\t/**\n\t * 创建新的 JdbcBeanDefinitionReader，委托给给定 PropertiesBeanDefinitionReader。\n\t * <p>仍需设置 DataSource 或 JdbcTemplate。\n\t * @see #setDataSource\n\t * @see #setJdbcTemplate\n\t */",
        ),
        (
            "\t/**\n\t * Set the DataSource to use to obtain database connections.\n\t * Will implicitly create a new JdbcTemplate with the given DataSource.\n\t */",
            "\t/**\n\t * 设置用于获取数据库连接的 DataSource。\n\t * 将隐式使用给定 DataSource 创建新的 JdbcTemplate。\n\t */",
        ),
        (
            "\t/**\n\t * Set the JdbcTemplate to be used by this bean factory.\n\t * Contains settings for DataSource, SQLExceptionTranslator, etc.\n\t */",
            "\t/**\n\t * 设置本 Bean 工厂使用的 JdbcTemplate。\n\t * 其中包含 DataSource、SQLExceptionTranslator 等配置。\n\t */",
        ),
        (
            "\t/**\n\t * Load bean definitions from the database via the given SQL string.\n\t * @param sql the SQL query to use for loading bean definitions.\n\t * The first three columns must be bean name, property name and value.\n\t * Any join and any other columns are permitted: for example,\n\t * {@code SELECT BEAN_NAME, PROPERTY, VALUE FROM CONFIG WHERE CONFIG.APP_ID = 1}\n\t * It's also possible to perform a join. Column names are not significant --\n\t * only the ordering of these first three columns.\n\t */",
            "\t/**\n\t * 通过给定 SQL 从数据库加载 Bean 定义。\n\t * @param sql 用于加载 Bean 定义的 SQL 查询。\n\t * 前三列必须为 bean 名称、属性名和属性值。\n\t * 允许任意 join 及其他列，例如\n\t * {@code SELECT BEAN_NAME, PROPERTY, VALUE FROM CONFIG WHERE CONFIG.APP_ID = 1}。\n\t * 也可执行 join。列名不重要，仅前三列顺序有意义。\n\t */",
        ),
    ],
    "JdbcDaoSupport.java": [
        (
            "/**\n * Convenient superclass for JDBC-based data access objects.\n *\n * <p>Requires a {@link javax.sql.DataSource} to be set, providing a\n * {@link org.springframework.jdbc.core.JdbcTemplate} based on it to\n * subclasses through the {@link #getJdbcTemplate()} method.\n *\n * <p>This base class is mainly intended for JdbcTemplate usage but can\n * also be used when working with a Connection directly or when using\n * {@code org.springframework.jdbc.object} operation objects.\n *\n * @author Juergen Hoeller\n * @since 28.07.2003\n * @see #setDataSource\n * @see #getJdbcTemplate\n * @see org.springframework.jdbc.core.JdbcTemplate\n * @deprecated as of 7.0, in favor of direct injection of {@link JdbcTemplate}\n * or {@link org.springframework.jdbc.core.simple.JdbcClient}\n */",
            "/**\n * 基于 JDBC 的数据访问对象的便捷超类。\n *\n * <p>需要设置 {@link javax.sql.DataSource}，并通过 {@link #getJdbcTemplate()}\n * 向子类提供基于它的 {@link org.springframework.jdbc.core.JdbcTemplate}。\n *\n * <p>该基类主要用于 JdbcTemplate 场景，也可在直接使用 Connection\n * 或使用 {@code org.springframework.jdbc.object} 操作对象时使用。\n *\n * @author Juergen Hoeller\n * @since 28.07.2003\n * @see #setDataSource\n * @see #getJdbcTemplate\n * @see org.springframework.jdbc.core.JdbcTemplate\n * @deprecated 自 7.0 起弃用，建议直接注入 {@link JdbcTemplate}\n * 或 {@link org.springframework.jdbc.core.simple.JdbcClient}\n */",
        ),
        (
            "\t/**\n\t * Set the JDBC DataSource to be used by this DAO.\n\t */",
            "\t/**\n\t * 设置本 DAO 使用的 JDBC DataSource。\n\t */",
        ),
        (
            "\t/**\n\t * Create a JdbcTemplate for the given DataSource.\n\t * Only invoked if populating the DAO with a DataSource reference!\n\t * <p>Can be overridden in subclasses to provide a JdbcTemplate instance\n\t * with different configuration, or a custom JdbcTemplate subclass.\n\t * @param dataSource the JDBC DataSource to create a JdbcTemplate for\n\t * @return the new JdbcTemplate instance\n\t * @see #setDataSource\n\t */",
            "\t/**\n\t * 为给定 DataSource 创建 JdbcTemplate。\n\t * 仅在通过 DataSource 引用填充 DAO 时调用。\n\t * <p>子类可覆盖以提供不同配置或自定义 JdbcTemplate 子类实例。\n\t * @param dataSource 要为其创建 JdbcTemplate 的 JDBC DataSource\n\t * @return 新的 JdbcTemplate 实例\n\t * @see #setDataSource\n\t */",
        ),
        (
            "\t/**\n\t * Return the JDBC DataSource used by this DAO.\n\t */",
            "\t/**\n\t * 返回本 DAO 使用的 JDBC DataSource。\n\t */",
        ),
        (
            "\t/**\n\t * Set the JdbcTemplate for this DAO explicitly,\n\t * as an alternative to specifying a DataSource.\n\t */",
            "\t/**\n\t * 显式设置本 DAO 的 JdbcTemplate，作为指定 DataSource 的替代方式。\n\t */",
        ),
        (
            "\t/**\n\t * Return the JdbcTemplate for this DAO,\n\t * pre-initialized with the DataSource or set explicitly.\n\t */",
            "\t/**\n\t * 返回本 DAO 的 JdbcTemplate，\n\t * 已通过 DataSource 预初始化或显式设置。\n\t */",
        ),
        (
            "\t/**\n\t * Initialize the template-based configuration of this DAO.\n\t * Called after a new JdbcTemplate has been set, either directly\n\t * or through a DataSource.\n\t * <p>This implementation is empty. Subclasses may override this\n\t * to configure further objects based on the JdbcTemplate.\n\t * @see #getJdbcTemplate()\n\t */",
            "\t/**\n\t * 初始化本 DAO 基于模板的配置。\n\t * 在直接设置或通过 DataSource 设置新 JdbcTemplate 后调用。\n\t * <p>本实现为空。子类可覆盖以基于 JdbcTemplate 配置更多对象。\n\t * @see #getJdbcTemplate()\n\t */",
        ),
        (
            "\t/**\n\t * Return the SQLExceptionTranslator of this DAO's JdbcTemplate,\n\t * for translating SQLExceptions in custom JDBC access code.\n\t * @see org.springframework.jdbc.core.JdbcTemplate#getExceptionTranslator()\n\t */",
            "\t/**\n\t * 返回本 DAO 的 JdbcTemplate 的 SQLExceptionTranslator，\n\t * 用于在自定义 JDBC 访问代码中翻译 SQLException。\n\t * @see org.springframework.jdbc.core.JdbcTemplate#getExceptionTranslator()\n\t */",
        ),
        (
            "\t/**\n\t * Get a JDBC Connection, either from the current transaction or a new one.\n\t * @return the JDBC Connection\n\t * @throws CannotGetJdbcConnectionException if the attempt to get a Connection failed\n\t * @see org.springframework.jdbc.datasource.DataSourceUtils#getConnection(javax.sql.DataSource)\n\t */",
            "\t/**\n\t * 获取 JDBC Connection，来自当前事务或新建连接。\n\t * @return JDBC Connection\n\t * @throws CannotGetJdbcConnectionException 获取 Connection 失败时\n\t * @see org.springframework.jdbc.datasource.DataSourceUtils#getConnection(javax.sql.DataSource)\n\t */",
        ),
        (
            "\t/**\n\t * Close the given JDBC Connection, created via this DAO's DataSource,\n\t * if it isn't bound to the thread.\n\t * @param con the Connection to close\n\t * @see org.springframework.jdbc.datasource.DataSourceUtils#releaseConnection\n\t */",
            "\t/**\n\t * 关闭通过本 DAO 的 DataSource 创建的 JDBC Connection（若未绑定到线程）。\n\t * @param con 要关闭的 Connection\n\t * @see org.springframework.jdbc.datasource.DataSourceUtils#releaseConnection\n\t */",
        ),
    ],
    "SqlBinaryValue.java": [
        (
            "/**\n * Object to represent a binary parameter value for an SQL statement, for example,\n * a binary stream for a BLOB or a LONGVARBINARY or PostgreSQL BYTEA column.\n *\n * <p>Designed for use with {@link org.springframework.jdbc.core.JdbcTemplate}\n * as well as {@link org.springframework.jdbc.core.simple.JdbcClient}, to be\n * passed in as a parameter value wrapping the target content value.\n *\n * <p>Can be combined with {@link org.springframework.jdbc.core.SqlParameterValue}\n * for specifying an SQL type, for example,\n * {@code new SqlParameterValue(Types.BLOB, new SqlBinaryValue(myContent))}.\n * With most database drivers, the type hint is not actually necessary.\n *\n * <p>Note: Only specify {@code Types.BLOB} in case of an actual BLOB, preferring\n * {@code Types.LONGVARBINARY} otherwise. With PostgreSQL, {@code Types.ARRAY}\n * has to be specified for BYTEA columns, rather than {@code Types.BLOB}. This\n * is in contrast to {@link SqlLobValue} where byte array handling was lenient.\n *\n * @author Juergen Hoeller\n * @since 6.1.4\n * @see SqlCharacterValue\n * @see org.springframework.jdbc.core.SqlParameterValue\n */",
            "/**\n * 表示 SQL 语句二进制参数值的对象，例如 BLOB、LONGVARBINARY\n * 或 PostgreSQL BYTEA 列的二进制流。\n *\n * <p>设计用于 {@link org.springframework.jdbc.core.JdbcTemplate} 和\n * {@link org.springframework.jdbc.core.simple.JdbcClient}，\n * 作为包装目标内容的参数值传入。\n *\n * <p>可与 {@link org.springframework.jdbc.core.SqlParameterValue} 组合以指定 SQL 类型，例如\n * {@code new SqlParameterValue(Types.BLOB, new SqlBinaryValue(myContent))}。\n * 多数数据库驱动实际上不需要类型提示。\n *\n * <p>注意：仅在实际 BLOB 时使用 {@code Types.BLOB}，否则优先 {@code Types.LONGVARBINARY}。\n * PostgreSQL 的 BYTEA 列应指定 {@code Types.ARRAY} 而非 {@code Types.BLOB}。\n * 这与 {@link SqlLobValue} 对字节数组的宽松处理不同。\n *\n * @author Juergen Hoeller\n * @since 6.1.4\n * @see SqlCharacterValue\n * @see org.springframework.jdbc.core.SqlParameterValue\n */",
        ),
        (
            "\t/**\n\t * Create a new {@code SqlBinaryValue} for the given content.\n\t * @param bytes the content as a byte array\n\t */",
            "\t/**\n\t * 为给定内容创建新的 {@code SqlBinaryValue}。\n\t * @param bytes 字节数组形式的内容\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@code SqlBinaryValue} for the given content.\n\t * @param stream the content stream\n\t * @param length the length of the content (or -1 if undetermined)\n\t */",
            "\t/**\n\t * 为给定内容创建新的 {@code SqlBinaryValue}。\n\t * @param stream 内容流\n\t * @param length 内容长度（未知时为 -1）\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@code SqlBinaryValue} for the given content.\n\t * <p>Consider specifying a {@link Resource} with content length support\n\t * when available: {@link SqlBinaryValue#SqlBinaryValue(Resource)}.\n\t * @param resource the resource to obtain a content stream from\n\t * @param length the length of the content (or -1 if undetermined)\n\t */",
            "\t/**\n\t * 为给定内容创建新的 {@code SqlBinaryValue}。\n\t * <p>若可用，建议使用支持 contentLength 的 {@link Resource}：\n\t * {@link SqlBinaryValue#SqlBinaryValue(Resource)}。\n\t * @param resource 用于获取内容流的资源\n\t * @param length 内容长度（未知时为 -1）\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@code SqlBinaryValue} for the given content.\n\t * <p>The length will get derived from {@link Resource#contentLength()}.\n\t * @param resource the resource to obtain a content stream from\n\t */",
            "\t/**\n\t * 为给定内容创建新的 {@code SqlBinaryValue}。\n\t * <p>长度将从 {@link Resource#contentLength()} 推导。\n\t * @param resource 用于获取内容流的资源\n\t */",
        ),
    ],
    "SqlCharacterValue.java": [
        (
            "/**\n * Object to represent a character-based parameter value for an SQL statement,\n * for example, a character stream for a CLOB/NCLOB or a LONGVARCHAR column.\n *\n * <p>Designed for use with {@link org.springframework.jdbc.core.JdbcTemplate}\n * as well as {@link org.springframework.jdbc.core.simple.JdbcClient}, to be\n * passed in as a parameter value wrapping the target content value.\n *\n * <p>Can be combined with {@link org.springframework.jdbc.core.SqlParameterValue}\n * for specifying an SQL type, for example,\n * {@code new SqlParameterValue(Types.CLOB, new SqlCharacterValue(myContent))}.\n * With most database drivers, the type hint is not actually necessary.\n *\n * <p>Note: Only specify {@code Types.CLOB} in case of an actual CLOB, preferring\n * {@code Types.LONGVARCHAR} otherwise. This is in contrast to {@link SqlLobValue}\n * where char sequence handling was lenient.\n *\n * @author Juergen Hoeller\n * @since 6.1.4\n * @see SqlBinaryValue\n * @see org.springframework.jdbc.core.SqlParameterValue\n */",
            "/**\n * 表示 SQL 语句字符型参数值的对象，例如 CLOB/NCLOB 或 LONGVARCHAR 列的字符流。\n *\n * <p>设计用于 {@link org.springframework.jdbc.core.JdbcTemplate} 和\n * {@link org.springframework.jdbc.core.simple.JdbcClient}，\n * 作为包装目标内容的参数值传入。\n *\n * <p>可与 {@link org.springframework.jdbc.core.SqlParameterValue} 组合以指定 SQL 类型，例如\n * {@code new SqlParameterValue(Types.CLOB, new SqlCharacterValue(myContent))}。\n * 多数数据库驱动实际上不需要类型提示。\n *\n * <p>注意：仅在实际 CLOB 时使用 {@code Types.CLOB}，否则优先 {@code Types.LONGVARCHAR}。\n * 这与 {@link SqlLobValue} 对字符序列的宽松处理不同。\n *\n * @author Juergen Hoeller\n * @since 6.1.4\n * @see SqlBinaryValue\n * @see org.springframework.jdbc.core.SqlParameterValue\n */",
        ),
        (
            "\t/**\n\t * Create a new CLOB value with the given content string.\n\t * @param string the content as a String or other CharSequence\n\t */",
            "\t/**\n\t * 使用给定内容字符串创建新的 CLOB 值。\n\t * @param string String 或其他 CharSequence 形式的内容\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@code SqlCharacterValue} for the given content.\n\t * @param characters the content as a character array\n\t */",
            "\t/**\n\t * 为给定内容创建新的 {@code SqlCharacterValue}。\n\t * @param characters 字符数组形式的内容\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@code SqlCharacterValue} for the given content.\n\t * @param reader the content reader\n\t * @param length the length of the content (or -1 if undetermined)\n\t */",
            "\t/**\n\t * 为给定内容创建新的 {@code SqlCharacterValue}。\n\t * @param reader 内容 Reader\n\t * @param length 内容长度（未知时为 -1）\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@code SqlCharacterValue} for the given content.\n\t * @param asciiStream the content as ASCII stream\n\t * @param length the length of the content (or -1 if undetermined)\n\t */",
            "\t/**\n\t * 为给定内容创建新的 {@code SqlCharacterValue}。\n\t * @param asciiStream ASCII 流形式的内容\n\t * @param length 内容长度（未知时为 -1）\n\t */",
        ),
    ],
    "SqlLobValue.java": [
        (
            "/**\n * Object to represent an SQL BLOB/CLOB value parameter. BLOBs can either be an\n * InputStream or a byte array. CLOBs can be in the form of a Reader, InputStream,\n * or String. Each CLOB/BLOB value will be stored together with its length.\n * The type is based on which constructor is used. Instances of this class are\n * stateful and immutable: use them and discard them.\n *\n * <p><b>NOTE: As of 6.1.4, this class is effectively superseded by\n * {@link SqlBinaryValue} and {@link SqlCharacterValue} which are capable of\n * modern BLOB/CLOB handling while also handling LONGVARBINARY/LONGVARCHAR.</b>\n * The only reason to keep using this class is a custom {@link LobHandler}.\n *\n * <p>This class holds a reference to a {@link LobCreator} that must be closed after\n * the update has completed. This is done via a call to the {@link #cleanup()} method.\n * All handling of the {@code LobCreator} is done by the framework classes that use it -\n * no need to set or close the {@code LobCreator} for end users of this class.\n *\n * <p>A usage example:\n *\n * <pre class=\"code\">JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);  // reusable object\n * LobHandler lobHandler = new DefaultLobHandler();  // reusable object\n *\n * jdbcTemplate.update(\n *     \"INSERT INTO imagedb (image_name, content, description) VALUES (?, ?, ?)\",\n *     new Object[] {\n *       name,\n *       new SqlLobValue(contentStream, contentLength, lobHandler),\n *       new SqlLobValue(description, lobHandler)\n *     },\n *     new int[] {Types.VARCHAR, Types.BLOB, Types.CLOB});\n * </pre>\n *\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @since 1.1\n * @see org.springframework.jdbc.support.lob.LobHandler\n * @see org.springframework.jdbc.support.lob.LobCreator\n * @see org.springframework.jdbc.core.JdbcTemplate#update(String, Object[], int[])\n * @see org.springframework.jdbc.object.SqlUpdate#update(Object[])\n * @see org.springframework.jdbc.object.StoredProcedure#execute(java.util.Map)\n * @deprecated as of 6.2, in favor of {@link SqlBinaryValue} and {@link SqlCharacterValue}\n */",
            "/**\n * 表示 SQL BLOB/CLOB 参数值的对象。BLOB 可为 InputStream 或字节数组；\n * CLOB 可为 Reader、InputStream 或 String。每个 CLOB/BLOB 值与其长度一并存储。\n * 类型取决于使用的构造函数。本类实例有状态且不可变：用完即弃。\n *\n * <p><b>注意：自 6.1.4 起，本类实质上已被 {@link SqlBinaryValue} 和 {@link SqlCharacterValue} 取代，\n * 后者支持现代 BLOB/CLOB 处理，同时兼容 LONGVARBINARY/LONGVARCHAR。</b>\n * 继续使用本类的唯一理由是自定义 {@link LobHandler}。\n *\n * <p>本类持有 {@link LobCreator} 引用，更新完成后须通过 {@link #cleanup()} 关闭。\n * 框架类会处理 {@code LobCreator} 的全部生命周期，\n * 本类使用者无需手动设置或关闭 {@code LobCreator}。\n *\n * <p>使用示例：\n *\n * <pre class=\"code\">JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);  // 可复用对象\n * LobHandler lobHandler = new DefaultLobHandler();  // 可复用对象\n *\n * jdbcTemplate.update(\n *     \"INSERT INTO imagedb (image_name, content, description) VALUES (?, ?, ?)\",\n *     new Object[] {\n *       name,\n *       new SqlLobValue(contentStream, contentLength, lobHandler),\n *       new SqlLobValue(description, lobHandler)\n *     },\n *     new int[] {Types.VARCHAR, Types.BLOB, Types.CLOB});\n * </pre>\n *\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @since 1.1\n * @see org.springframework.jdbc.support.lob.LobHandler\n * @see org.springframework.jdbc.support.lob.LobCreator\n * @see org.springframework.jdbc.core.JdbcTemplate#update(String, Object[], int[])\n * @see org.springframework.jdbc.object.SqlUpdate#update(Object[])\n * @see org.springframework.jdbc.object.StoredProcedure#execute(java.util.Map)\n * @deprecated 自 6.2 起弃用，建议使用 {@link SqlBinaryValue} 和 {@link SqlCharacterValue}\n */",
        ),
        (
            "\t/**\n\t * Reference to the LobCreator - so we can close it once the update is done.\n\t */",
            "\t/**\n\t * LobCreator 引用，以便更新完成后关闭。\n\t */",
        ),
        (
            "\t/**\n\t * Create a new BLOB value with the given byte array,\n\t * using a DefaultLobHandler.\n\t * @param bytes the byte array containing the BLOB value\n\t * @see org.springframework.jdbc.support.lob.DefaultLobHandler\n\t */",
            "\t/**\n\t * 使用给定字节数组创建新的 BLOB 值，底层使用 DefaultLobHandler。\n\t * @param bytes 包含 BLOB 值的字节数组\n\t * @see org.springframework.jdbc.support.lob.DefaultLobHandler\n\t */",
        ),
        (
            "\t/**\n\t * Create a new BLOB value with the given byte array.\n\t * @param bytes the byte array containing the BLOB value\n\t * @param lobHandler the LobHandler to be used\n\t */",
            "\t/**\n\t * 使用给定字节数组创建新的 BLOB 值。\n\t * @param bytes 包含 BLOB 值的字节数组\n\t * @param lobHandler 要使用的 LobHandler\n\t */",
        ),
        (
            "\t/**\n\t * Create a new CLOB value with the given content string,\n\t * using a DefaultLobHandler.\n\t * @param content the String containing the CLOB value\n\t * @see org.springframework.jdbc.support.lob.DefaultLobHandler\n\t */",
            "\t/**\n\t * 使用给定内容字符串创建新的 CLOB 值，底层使用 DefaultLobHandler。\n\t * @param content 包含 CLOB 值的 String\n\t * @see org.springframework.jdbc.support.lob.DefaultLobHandler\n\t */",
        ),
        (
            "\t/**\n\t * Create a new CLOB value with the given content string.\n\t * @param content the String containing the CLOB value\n\t * @param lobHandler the LobHandler to be used\n\t */",
            "\t/**\n\t * 使用给定内容字符串创建新的 CLOB 值。\n\t * @param content 包含 CLOB 值的 String\n\t * @param lobHandler 要使用的 LobHandler\n\t */",
        ),
        (
            "\t/**\n\t * Create a new BLOB/CLOB value with the given stream,\n\t * using a DefaultLobHandler.\n\t * @param stream the stream containing the LOB value\n\t * @param length the length of the LOB value\n\t * @see org.springframework.jdbc.support.lob.DefaultLobHandler\n\t */",
            "\t/**\n\t * 使用给定流创建新的 BLOB/CLOB 值，底层使用 DefaultLobHandler。\n\t * @param stream 包含 LOB 值的流\n\t * @param length LOB 值长度\n\t * @see org.springframework.jdbc.support.lob.DefaultLobHandler\n\t */",
        ),
        (
            "\t/**\n\t * Create a new BLOB/CLOB value with the given stream.\n\t * @param stream the stream containing the LOB value\n\t * @param length the length of the LOB value\n\t * @param lobHandler the LobHandler to be used\n\t */",
            "\t/**\n\t * 使用给定流创建新的 BLOB/CLOB 值。\n\t * @param stream 包含 LOB 值的流\n\t * @param length LOB 值长度\n\t * @param lobHandler 要使用的 LobHandler\n\t */",
        ),
        (
            "\t/**\n\t * Create a new CLOB value with the given character stream,\n\t * using a DefaultLobHandler.\n\t * @param reader the character stream containing the CLOB value\n\t * @param length the length of the CLOB value\n\t * @see org.springframework.jdbc.support.lob.DefaultLobHandler\n\t */",
            "\t/**\n\t * 使用给定字符流创建新的 CLOB 值，底层使用 DefaultLobHandler。\n\t * @param reader 包含 CLOB 值的字符流\n\t * @param length CLOB 值长度\n\t * @see org.springframework.jdbc.support.lob.DefaultLobHandler\n\t */",
        ),
        (
            "\t/**\n\t * Create a new CLOB value with the given character stream.\n\t * @param reader the character stream containing the CLOB value\n\t * @param length the length of the CLOB value\n\t * @param lobHandler the LobHandler to be used\n\t */",
            "\t/**\n\t * 使用给定字符流创建新的 CLOB 值。\n\t * @param reader 包含 CLOB 值的字符流\n\t * @param length CLOB 值长度\n\t * @param lobHandler 要使用的 LobHandler\n\t */",
        ),
        (
            "\t/**\n\t * Set the specified content via the LobCreator.\n\t */",
            "\t/**\n\t * 通过 LobCreator 设置指定内容。\n\t */",
        ),
        (
            "\t/**\n\t * Close the LobCreator.\n\t */",
            "\t/**\n\t * 关闭 LobCreator。\n\t */",
        ),
    ],
}
