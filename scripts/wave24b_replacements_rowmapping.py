"""Chinese JavaDoc replacements for springframework wave24b row mapping classes."""

ROWMAPPING_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "RowCountCallbackHandler.java": [
        (
            "/**\n * Implementation of RowCallbackHandler. Convenient superclass for callback handlers.\n * An instance can only be used once.\n *\n * <p>We can either use this on its own (for example, in a test case, to ensure\n * that our result sets have valid dimensions), or use it as a superclass\n * for callback handlers that actually do something, and will benefit\n * from the dimension information it provides.\n *\n * <p>A usage example with JdbcTemplate:\n *\n * <pre class=\"code\">JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);  // reusable object\n *\n * RowCountCallbackHandler countCallback = new RowCountCallbackHandler();  // not reusable\n * jdbcTemplate.query(\"select * from user\", countCallback);\n * int rowCount = countCallback.getRowCount();</pre>\n *\n * @author Rod Johnson\n * @since May 3, 2001\n */",
            "/**\n * RowCallbackHandler 的实现。回调处理器的便捷超类。\n * 每个实例只能使用一次。\n *\n * <p>可单独使用（例如在测试用例中验证结果集维度），\n * 也可作为实际执行操作的回调处理器的超类，\n * 以利用其提供的维度信息。\n *\n * <p>与 JdbcTemplate 配合使用的示例：\n *\n * <pre class=\"code\">JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);  // 可重用对象\n *\n * RowCountCallbackHandler countCallback = new RowCountCallbackHandler();  // 不可重用\n * jdbcTemplate.query(\"select * from user\", countCallback);\n * int rowCount = countCallback.getRowCount();</pre>\n *\n * @author Rod Johnson\n * @since May 3, 2001\n */",
        ),
        (
            "\t/** Rows we've seen so far. */",
            "\t/** 目前已处理的行数。 */",
        ),
        (
            "\t/** Columns we've seen so far. */",
            "\t/** 目前已知的列数。 */",
        ),
        (
            "\t/**\n\t * Indexed from 0. Type (as in java.sql.Types) for the columns\n\t * as returned by ResultSetMetaData object.\n\t */",
            "\t/**\n\t * 从 0 开始索引。ResultSetMetaData 返回的各列类型\n\t * （对应 java.sql.Types 常量）。\n\t */",
        ),
        (
            "\t/**\n\t * Indexed from 0. Column name as returned by ResultSetMetaData object.\n\t */",
            "\t/**\n\t * 从 0 开始索引。ResultSetMetaData 返回的列名。\n\t */",
        ),
        (
            "\t/**\n\t * Implementation of ResultSetCallbackHandler.\n\t * Work out column size if this is the first row, otherwise just count rows.\n\t * <p>Subclasses can perform custom extraction or processing\n\t * by overriding the {@code processRow(ResultSet, int)} method.\n\t * @see #processRow(java.sql.ResultSet, int)\n\t */",
            "\t/**\n\t * ResultSetCallbackHandler 的实现。\n\t * 若是第一行则确定列数，否则仅计数行数。\n\t * <p>子类可覆盖 {@code processRow(ResultSet, int)} 方法\n\t * 执行自定义提取或处理。\n\t * @see #processRow(java.sql.ResultSet, int)\n\t */",
        ),
        (
            "\t\t\t// could also get column names",
            "\t\t\t// 也可获取列名",
        ),
        (
            "\t/**\n\t * Subclasses may override this to perform custom extraction\n\t * or processing. This class's implementation does nothing.\n\t * @param rs the ResultSet to extract data from. This method is\n\t * invoked for each row\n\t * @param rowNum number of the current row (starting from 0)\n\t */",
            "\t/**\n\t * 子类可覆盖本方法执行自定义提取或处理。\n\t * 本类实现为空操作。\n\t * @param rs 要提取数据的 ResultSet。每行调用一次\n\t * @param rowNum 当前行号（从 0 开始）\n\t */",
        ),
        (
            "\t/**\n\t * Return the types of the columns as java.sql.Types constants\n\t * Valid after processRow is invoked the first time.\n\t * @return the types of the columns as java.sql.Types constants.\n\t * <b>Indexed from 0 to n-1.</b>\n\t */",
            "\t/**\n\t * 以 java.sql.Types 常量返回各列类型。\n\t * 首次调用 processRow 后有效。\n\t * @return 各列的 java.sql.Types 常量。\n\t * <b>从 0 到 n-1 索引。</b>\n\t */",
        ),
        (
            "\t/**\n\t * Return the names of the columns.\n\t * Valid after processRow is invoked the first time.\n\t * @return the names of the columns.\n\t * <b>Indexed from 0 to n-1.</b>\n\t */",
            "\t/**\n\t * 返回各列名称。\n\t * 首次调用 processRow 后有效。\n\t * @return 各列名称。\n\t * <b>从 0 到 n-1 索引。</b>\n\t */",
        ),
        (
            "\t/**\n\t * Return the row count of this ResultSet.\n\t * Only valid after processing is complete\n\t * @return the number of rows in this ResultSet\n\t */",
            "\t/**\n\t * 返回此 ResultSet 的行数。\n\t * 仅在处理完成后有效。\n\t * @return 此 ResultSet 中的行数\n\t */",
        ),
        (
            "\t/**\n\t * Return the number of columns in this result set.\n\t * Valid once we've seen the first row,\n\t * so subclasses can use it during processing\n\t * @return the number of columns in this result set\n\t */",
            "\t/**\n\t * 返回此结果集的列数。\n\t * 看到第一行后有效，子类可在处理过程中使用。\n\t * @return 此结果集的列数\n\t */",
        ),
    ],
    "RowMapperResultSetExtractor.java": [
        (
            "/**\n * Adapter implementation of the ResultSetExtractor interface that delegates\n * to a RowMapper which is supposed to create an object for each row.\n * Each object is added to the results List of this ResultSetExtractor.\n *\n * <p>Useful for the typical case of one object per row in the database table.\n * The number of entries in the results list will match the number of rows.\n *\n * <p>Note that a RowMapper object is typically stateless and thus reusable;\n * just the RowMapperResultSetExtractor adapter is stateful.\n *\n * <p>A usage example with JdbcTemplate:\n *\n * <pre class=\"code\">JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);  // reusable object\n * RowMapper rowMapper = new UserRowMapper();  // reusable object\n *\n * List allUsers = (List) jdbcTemplate.query(\n *     \"select * from user\",\n *     new RowMapperResultSetExtractor(rowMapper, 10));\n *\n * User user = (User) jdbcTemplate.queryForObject(\n *     \"select * from user where id=?\", new Object[] {id},\n *     new RowMapperResultSetExtractor(rowMapper, 1));</pre>\n *\n * <p>Alternatively, consider subclassing MappingSqlQuery from the {@code jdbc.object}\n * package: Instead of working with separate JdbcTemplate and RowMapper objects,\n * you can have executable query objects (containing row-mapping logic) there.\n *\n * @author Juergen Hoeller\n * @author Yanming Zhou\n * @since 1.0.2\n * @param <T> the result element type\n * @see RowMapper\n * @see JdbcTemplate\n * @see org.springframework.jdbc.object.MappingSqlQuery\n */",
            "/**\n * ResultSetExtractor 接口的适配器实现，委托给 RowMapper，\n * 由 RowMapper 为每行创建一个对象。\n * 每个对象添加到本 ResultSetExtractor 的结果 List 中。\n *\n * <p>适用于数据库表每行对应一个对象的典型场景。\n * 结果列表的条目数与行数一致。\n *\n * <p>注意 RowMapper 通常是无状态的，因此可重用；\n * 仅 RowMapperResultSetExtractor 适配器是有状态的。\n *\n * <p>与 JdbcTemplate 配合使用的示例：\n *\n * <pre class=\"code\">JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);  // 可重用对象\n * RowMapper rowMapper = new UserRowMapper();  // 可重用对象\n *\n * List allUsers = (List) jdbcTemplate.query(\n *     \"select * from user\",\n *     new RowMapperResultSetExtractor(rowMapper, 10));\n *\n * User user = (User) jdbcTemplate.queryForObject(\n *     \"select * from user where id=?\", new Object[] {id},\n *     new RowMapperResultSetExtractor(rowMapper, 1));</pre>\n *\n * <p>或者，考虑从 {@code jdbc.object} 包子类化 MappingSqlQuery：\n * 可以该方式构建包含行映射逻辑的可执行查询对象，\n * 而非分别使用 JdbcTemplate 和 RowMapper。\n *\n * @author Juergen Hoeller\n * @author Yanming Zhou\n * @since 1.0.2\n * @param <T> 结果元素类型\n * @see RowMapper\n * @see JdbcTemplate\n * @see org.springframework.jdbc.object.MappingSqlQuery\n */",
        ),
        (
            "\t/**\n\t * Create a new RowMapperResultSetExtractor.\n\t * @param rowMapper the RowMapper which creates an object for each row\n\t */",
            "\t/**\n\t * 创建新的 RowMapperResultSetExtractor。\n\t * @param rowMapper 为每行创建对象的 RowMapper\n\t */",
        ),
        (
            "\t/**\n\t * Create a new RowMapperResultSetExtractor.\n\t * @param rowMapper the RowMapper which creates an object for each row\n\t * @param rowsExpected the number of expected rows\n\t * (just used for optimized collection handling)\n\t */",
            "\t/**\n\t * 创建新的 RowMapperResultSetExtractor。\n\t * @param rowMapper 为每行创建对象的 RowMapper\n\t * @param rowsExpected 预期行数（仅用于优化集合处理）\n\t */",
        ),
        (
            "\t/**\n\t * Create a new RowMapperResultSetExtractor.\n\t * @param rowMapper the RowMapper which creates an object for each row\n\t * @param rowsExpected the number of expected rows\n\t * (just used for optimized collection handling)\n\t * @param maxRows the number of max rows (or -1 for the driver's default)\n\t * @since 7.0\n\t */",
            "\t/**\n\t * 创建新的 RowMapperResultSetExtractor。\n\t * @param rowMapper 为每行创建对象的 RowMapper\n\t * @param rowsExpected 预期行数（仅用于优化集合处理）\n\t * @param maxRows 最大行数（或 -1 表示使用驱动默认值）\n\t * @since 7.0\n\t */",
        ),
    ],
    "SimplePropertyRowMapper.java": [
        (
            "/**\n * {@link RowMapper} implementation that converts a row into a new instance\n * of the specified mapped target class. The mapped target class must be a\n * top-level class or {@code static} nested class, and it may expose either a\n * <em>data class</em> constructor with named parameters corresponding to column\n * names or classic bean property setter methods with property names corresponding\n * to column names or fields with corresponding field names.\n *\n * <p>When combining a data class constructor with setter methods, any property\n * mapped successfully via a constructor argument will not be mapped additionally\n * via a corresponding setter method or field mapping. This means that constructor\n * arguments take precedence over property setter methods which in turn take\n * precedence over direct field mappings.\n *\n * <p>To facilitate mapping between columns and properties that don't have matching\n * names, try using underscore-separated column aliases in the SQL statement like\n * {@code \"select fname as first_name from customer\"}, where {@code first_name}\n * can be mapped to a {@code setFirstName(String)} method in the target class.\n *\n * <p>This is a flexible alternative to {@link DataClassRowMapper} and\n * {@link BeanPropertyRowMapper} for scenarios where no specific customization\n * and no pre-defined property mappings are needed.\n *\n * <p>In terms of its fallback property discovery algorithm, this class is similar to\n * {@link org.springframework.jdbc.core.namedparam.SimplePropertySqlParameterSource}\n * and is similarly used for {@link org.springframework.jdbc.core.simple.JdbcClient}.\n *\n * @author Juergen Hoeller\n * @since 6.1\n * @param <T> the result type\n * @see DataClassRowMapper\n * @see BeanPropertyRowMapper\n * @see org.springframework.jdbc.core.simple.JdbcClient.StatementSpec#query(Class)\n * @see org.springframework.jdbc.core.namedparam.SimplePropertySqlParameterSource\n */",
            "/**\n * {@link RowMapper} 实现，将一行转换为指定映射目标类的新实例。\n * 映射目标类须为顶层类或 {@code static} 嵌套类，\n * 可暴露与列名对应命名参数的<em>数据类</em>构造器，\n * 或与列名对应的经典 Bean 属性 setter 方法，\n * 或与字段名对应的字段。\n *\n * <p>组合数据类构造器与 setter 方法时，\n * 已通过构造器参数成功映射的属性不会再通过\n * 对应 setter 方法或字段映射。\n * 即构造器参数优先于属性 setter，setter 优先于直接字段映射。\n *\n * <p>为便于映射列名与属性名不匹配的情况，\n * 可在 SQL 中使用下划线分隔的列别名，如\n * {@code \"select fname as first_name from customer\"}，\n * 其中 {@code first_name} 可映射到目标类的 {@code setFirstName(String)} 方法。\n *\n * <p>这是 {@link DataClassRowMapper} 和 {@link BeanPropertyRowMapper} 的灵活替代，\n * 适用于无需特定定制和预定义属性映射的场景。\n *\n * <p>其回退属性发现算法与\n * {@link org.springframework.jdbc.core.namedparam.SimplePropertySqlParameterSource} 类似，\n * 同样用于 {@link org.springframework.jdbc.core.simple.JdbcClient}。\n *\n * @author Juergen Hoeller\n * @since 6.1\n * @param <T> 结果类型\n * @see DataClassRowMapper\n * @see BeanPropertyRowMapper\n * @see org.springframework.jdbc.core.simple.JdbcClient.StatementSpec#query(Class)\n * @see org.springframework.jdbc.core.namedparam.SimplePropertySqlParameterSource\n */",
        ),
        (
            "\t/**\n\t * Create a new {@code SimplePropertyRowMapper}.\n\t * @param mappedClass the class that each row should be mapped to\n\t */",
            "\t/**\n\t * 创建新的 {@code SimplePropertyRowMapper}。\n\t * @param mappedClass 每行应映射到的类\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@code SimplePropertyRowMapper}.\n\t * @param mappedClass the class that each row should be mapped to\n\t * @param conversionService a {@link ConversionService} for binding\n\t * JDBC values to bean properties\n\t */",
            "\t/**\n\t * 创建新的 {@code SimplePropertyRowMapper}。\n\t * @param mappedClass 每行应映射到的类\n\t * @param conversionService 用于将 JDBC 值绑定到 Bean 属性的 {@link ConversionService}\n\t */",
        ),
        (
            "\t\t\t\t// Try direct name match first",
            "\t\t\t\t// 先尝试直接名称匹配",
        ),
        (
            "\t\t\t\t// Try underscored name match instead",
            "\t\t\t\t// 否则尝试下划线名称匹配",
        ),
        (
            "\t\t\t// Try direct match first",
            "\t\t\t// 先尝试直接匹配",
        ),
        (
            "\t\t\t// Try de-underscored match instead",
            "\t\t\t// 否则尝试去下划线匹配",
        ),
        (
            "\t\t\t// Fallback: case-insensitive match",
            "\t\t\t// 回退：不区分大小写匹配",
        ),
    ],
    "SingleColumnRowMapper.java": [
        (
            "/**\n * {@link RowMapper} implementation that converts a single column into a single\n * result value per row. Expects to operate on a {@code java.sql.ResultSet}\n * that just contains a single column.\n *\n * <p>The type of the result value for each row can be specified. The value\n * for the single column will be extracted from the {@code ResultSet}\n * and converted into the specified target type.\n *\n * @author Juergen Hoeller\n * @author Kazuki Shimizu\n * @since 1.2\n * @param <T> the result type\n * @see JdbcTemplate#queryForList(String, Class)\n * @see JdbcTemplate#queryForObject(String, Class)\n */",
            "/**\n * {@link RowMapper} 实现，将单列转换为每行一个结果值。\n * 期望操作的 {@code java.sql.ResultSet} 仅包含单列。\n *\n * <p>可指定每行结果值的类型。单列值将从 {@code ResultSet} 提取\n * 并转换为指定的目标类型。\n *\n * @author Juergen Hoeller\n * @author Kazuki Shimizu\n * @since 1.2\n * @param <T> 结果类型\n * @see JdbcTemplate#queryForList(String, Class)\n * @see JdbcTemplate#queryForObject(String, Class)\n */",
        ),
        (
            "\t/**\n\t * Create a new {@code SingleColumnRowMapper} for bean-style configuration.\n\t * @see #setRequiredType\n\t */",
            "\t/**\n\t * 创建新的 {@code SingleColumnRowMapper}，用于 Bean 风格配置。\n\t * @see #setRequiredType\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@code SingleColumnRowMapper}.\n\t * @param requiredType the type that each result object is expected to match\n\t */",
            "\t/**\n\t * 创建新的 {@code SingleColumnRowMapper}。\n\t * @param requiredType 每个结果对象期望匹配的类型\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@code SingleColumnRowMapper}.\n\t * @param requiredType the type that each result object is expected to match\n\t * @param conversionService a {@link ConversionService} for converting a fetched value\n\t * @since 7.0\n\t */",
            "\t/**\n\t * 创建新的 {@code SingleColumnRowMapper}。\n\t * @param requiredType 每个结果对象期望匹配的类型\n\t * @param conversionService 用于转换获取值的 {@link ConversionService}\n\t * @since 7.0\n\t */",
        ),
        (
            "\t/**\n\t * Set the type that each result object is expected to match.\n\t * <p>If not specified, the column value will be exposed as\n\t * returned by the JDBC driver.\n\t */",
            "\t/**\n\t * 设置每个结果对象期望匹配的类型。\n\t * <p>若未指定，列值将按 JDBC 驱动返回的形式暴露。\n\t */",
        ),
        (
            "\t/**\n\t * Set a {@link ConversionService} for converting a fetched value.\n\t * <p>Default is the {@link DefaultConversionService}.\n\t * @since 5.0.4\n\t * @see DefaultConversionService#getSharedInstance()\n\t */",
            "\t/**\n\t * 设置用于转换获取值的 {@link ConversionService}。\n\t * <p>默认为 {@link DefaultConversionService}。\n\t * @since 5.0.4\n\t * @see DefaultConversionService#getSharedInstance()\n\t */",
        ),
        (
            "\t/**\n\t * Extract a value for the single column in the current row.\n\t * <p>Validates that there is only one column selected,\n\t * then delegates to {@code getColumnValue()} and also\n\t * {@code convertValueToRequiredType}, if necessary.\n\t * @see java.sql.ResultSetMetaData#getColumnCount()\n\t * @see #getColumnValue(java.sql.ResultSet, int, Class)\n\t * @see #convertValueToRequiredType(Object, Class)\n\t */",
            "\t/**\n\t * 提取当前行单列的值。\n\t * <p>验证仅选择了一列，然后委托 {@code getColumnValue()}，\n\t * 必要时还调用 {@code convertValueToRequiredType}。\n\t * @see java.sql.ResultSetMetaData#getColumnCount()\n\t * @see #getColumnValue(java.sql.ResultSet, int, Class)\n\t * @see #convertValueToRequiredType(Object, Class)\n\t */",
        ),
        (
            "\t\t// Validate column count.",
            "\t\t// 验证列数。",
        ),
        (
            "\t\t// Extract column value from JDBC ResultSet.",
            "\t\t// 从 JDBC ResultSet 提取列值。",
        ),
        (
            "\t\t\t// Extracted value does not match already: try to convert it.",
            "\t\t\t// 提取的值类型不匹配：尝试转换。",
        ),
        (
            "\t/**\n\t * Retrieve a JDBC object value for the specified column.\n\t * <p>The default implementation calls\n\t * {@link JdbcUtils#getResultSetValue(java.sql.ResultSet, int, Class)}.\n\t * If no required type has been specified, this method delegates to\n\t * {@code getColumnValue(rs, index)}, which basically calls\n\t * {@code ResultSet.getObject(index)} but applies some additional\n\t * default conversion to appropriate value types.\n\t * @param rs is the ResultSet holding the data\n\t * @param index is the column index\n\t * @param requiredType the type that each result object is expected to match\n\t * (or {@code null} if none specified)\n\t * @return the Object value\n\t * @throws SQLException in case of extraction failure\n\t * @see org.springframework.jdbc.support.JdbcUtils#getResultSetValue(java.sql.ResultSet, int, Class)\n\t * @see #getColumnValue(java.sql.ResultSet, int)\n\t */",
            "\t/**\n\t * 获取指定列的 JDBC 对象值。\n\t * <p>默认实现调用\n\t * {@link JdbcUtils#getResultSetValue(java.sql.ResultSet, int, Class)}。\n\t * 若未指定 requiredType，则委托 {@code getColumnValue(rs, index)}，\n\t * 基本调用 {@code ResultSet.getObject(index)}，\n\t * 但会应用额外的默认转换到合适的值类型。\n\t * @param rs 持有数据的 ResultSet\n\t * @param index 列索引\n\t * @param requiredType 每个结果对象期望匹配的类型\n\t * （未指定则为 {@code null}）\n\t * @return 对象值\n\t * @throws SQLException 提取失败时\n\t * @see org.springframework.jdbc.support.JdbcUtils#getResultSetValue(java.sql.ResultSet, int, Class)\n\t * @see #getColumnValue(java.sql.ResultSet, int)\n\t */",
        ),
        (
            "\t\t\t// No required type specified -> perform default extraction.",
            "\t\t\t// 未指定 requiredType -> 执行默认提取。",
        ),
        (
            "\t/**\n\t * Retrieve a JDBC object value for the specified column, using the most\n\t * appropriate value type. Called if no required type has been specified.\n\t * <p>The default implementation delegates to {@code JdbcUtils.getResultSetValue()},\n\t * which uses the {@code ResultSet.getObject(index)} method. Additionally,\n\t * it includes a \"hack\" to get around Oracle returning a non-standard object for\n\t * their TIMESTAMP datatype. See the {@code JdbcUtils#getResultSetValue()}\n\t * javadoc for details.\n\t * @param rs is the ResultSet holding the data\n\t * @param index is the column index\n\t * @return the Object value\n\t * @throws SQLException in case of extraction failure\n\t * @see org.springframework.jdbc.support.JdbcUtils#getResultSetValue(java.sql.ResultSet, int)\n\t */",
            "\t/**\n\t * 获取指定列的 JDBC 对象值，使用最合适的值类型。\n\t * 未指定 requiredType 时调用。\n\t * <p>默认实现委托 {@code JdbcUtils.getResultSetValue()}，\n\t * 使用 {@code ResultSet.getObject(index)} 方法。\n\t * 此外还包含处理 Oracle TIMESTAMP 返回非标准对象的变通方案。\n\t * 详情参见 {@code JdbcUtils#getResultSetValue()} 的 javadoc。\n\t * @param rs 持有数据的 ResultSet\n\t * @param index 列索引\n\t * @return 对象值\n\t * @throws SQLException 提取失败时\n\t * @see org.springframework.jdbc.support.JdbcUtils#getResultSetValue(java.sql.ResultSet, int)\n\t */",
        ),
        (
            "\t/**\n\t * Convert the given column value to the specified required type.\n\t * Only called if the extracted column value does not match already.\n\t * <p>If the required type is String, the value will simply get stringified\n\t * via {@code toString()}. In case of a Number, the value will be\n\t * converted into a Number, either through number conversion or through\n\t * String parsing (depending on the value type). Otherwise, the value will\n\t * be converted to a required type using the {@link ConversionService}.\n\t * @param value the column value as extracted from {@code getColumnValue()}\n\t * (never {@code null})\n\t * @param requiredType the type that each result object is expected to match\n\t * (never {@code null})\n\t * @return the converted value\n\t * @see #getColumnValue(java.sql.ResultSet, int, Class)\n\t */",
            "\t/**\n\t * 将给定的列值转换为指定的 requiredType。\n\t * 仅在提取的列值类型不匹配时调用。\n\t * <p>若 requiredType 为 String，通过 {@code toString()} 字符串化。\n\t * 若为 Number，通过数值转换或字符串解析（取决于值类型）转换为 Number。\n\t * 否则通过 {@link ConversionService} 转换为 requiredType。\n\t * @param value 从 {@code getColumnValue()} 提取的列值（永不为 {@code null}）\n\t * @param requiredType 每个结果对象期望匹配的类型（永不为 {@code null}）\n\t * @return 转换后的值\n\t * @see #getColumnValue(java.sql.ResultSet, int, Class)\n\t */",
        ),
        (
            "\t\t\t\t// Convert original Number to target Number class.",
            "\t\t\t\t// 将原始 Number 转换为目标 Number 类。",
        ),
        (
            "\t\t\t\t// Convert stringified value to target Number class.",
            "\t\t\t\t// 将字符串化的值转换为目标 Number 类。",
        ),
        (
            "\t/**\n\t * Static factory method to create a new {@code SingleColumnRowMapper}.\n\t * @param requiredType the type that each result object is expected to match\n\t * @since 4.1\n\t * @see #newInstance(Class, ConversionService)\n\t */",
            "\t/**\n\t * 创建新 {@code SingleColumnRowMapper} 的静态工厂方法。\n\t * @param requiredType 每个结果对象期望匹配的类型\n\t * @since 4.1\n\t * @see #newInstance(Class, ConversionService)\n\t */",
        ),
        (
            "\t/**\n\t * Static factory method to create a new {@code SingleColumnRowMapper}.\n\t * @param requiredType the type that each result object is expected to match\n\t * @param conversionService the {@link ConversionService} for converting a\n\t * fetched value, or {@code null} for none\n\t * @since 5.0.4\n\t * @see #newInstance(Class)\n\t * @see #setConversionService\n\t */",
            "\t/**\n\t * 创建新 {@code SingleColumnRowMapper} 的静态工厂方法。\n\t * @param requiredType 每个结果对象期望匹配的类型\n\t * @param conversionService 用于转换获取值的 {@link ConversionService}，\n\t * 或 {@code null} 表示不使用\n\t * @since 5.0.4\n\t * @see #newInstance(Class)\n\t * @see #setConversionService\n\t */",
        ),
    ],
}
