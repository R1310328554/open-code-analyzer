"""Chinese JavaDoc replacements for springframework wave28a incrementer classes."""

INCREMENTER_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "AbstractColumnMaxValueIncrementer.java": [
        (
            "/**\n * Abstract base class for {@link DataFieldMaxValueIncrementer} implementations that use\n * a column in a custom sequence table. Subclasses need to provide the specific handling\n * of that table in their {@link #getNextKey()} implementation.\n *\n * @author Juergen Hoeller\n * @since 2.5.3\n */",
            "/**\n * 使用自定义序列表中列的 {@link DataFieldMaxValueIncrementer} 实现的抽象基类。\n * 子类须在 {@link #getNextKey()} 中提供对该表的具体处理。\n *\n * @author Juergen Hoeller\n * @since 2.5.3\n */",
        ),
        (
            "\t/** The name of the column for this sequence. */",
            "\t/** 本序列使用的列名。 */",
        ),
        (
            "\t/** The number of keys buffered in a cache. */",
            "\t/** 缓存中缓冲的键数量。 */",
        ),
        (
            "\t/**\n\t * Default constructor for bean property style usage.\n\t * @see #setDataSource\n\t * @see #setIncrementerName\n\t * @see #setColumnName\n\t */",
            "\t/**\n\t * Bean 属性风格使用的默认构造器。\n\t * @see #setDataSource\n\t * @see #setIncrementerName\n\t * @see #setColumnName\n\t */",
        ),
        (
            "\t/**\n\t * Convenience constructor.\n\t * @param dataSource the DataSource to use\n\t * @param incrementerName the name of the sequence/table to use\n\t * @param columnName the name of the column in the sequence table to use\n\t */",
            "\t/**\n\t * 便捷构造器。\n\t * @param dataSource 要使用的 DataSource\n\t * @param incrementerName 要使用的序列/表名\n\t * @param columnName 序列表中要使用的列名\n\t */",
        ),
        (
            "\t/**\n\t * Set the name of the column in the sequence table.\n\t */",
            "\t/**\n\t * 设置序列表中的列名。\n\t */",
        ),
        (
            "\t/**\n\t * Return the name of the column in the sequence table.\n\t */",
            "\t/**\n\t * 返回序列表中的列名。\n\t */",
        ),
        (
            "\t/**\n\t * Set the number of buffered keys.\n\t */",
            "\t/**\n\t * 设置缓冲键数量。\n\t */",
        ),
        (
            "\t/**\n\t * Return the number of buffered keys.\n\t */",
            "\t/**\n\t * 返回缓冲键数量。\n\t */",
        ),
    ],
    "AbstractDataFieldMaxValueIncrementer.java": [
        (
            "/**\n * Base implementation of {@link DataFieldMaxValueIncrementer} that delegates\n * to a single {@link #getNextKey} template method that returns a {@code long}.\n * Uses longs for String values, padding with zeroes if required.\n *\n * @author Dmitriy Kopylenko\n * @author Juergen Hoeller\n * @author Jean-Pierre Pawlak\n * @author Juergen Hoeller\n */",
            "/**\n * {@link DataFieldMaxValueIncrementer} 的基础实现，委托给返回 {@code long} 的\n * {@link #getNextKey} 模板方法。String 值基于 long，必要时前补零。\n *\n * @author Dmitriy Kopylenko\n * @author Juergen Hoeller\n * @author Jean-Pierre Pawlak\n * @author Juergen Hoeller\n */",
        ),
        (
            "\t/** The name of the sequence/table containing the sequence. */",
            "\t/** 包含序列的序列/表名。 */",
        ),
        (
            "\t/** The length to which a string result should be prepended with zeroes. */",
            "\t/** String 结果前补零的目标长度。 */",
        ),
        (
            "\t/**\n\t * Default constructor for bean property style usage.\n\t * @see #setDataSource\n\t * @see #setIncrementerName\n\t */",
            "\t/**\n\t * Bean 属性风格使用的默认构造器。\n\t * @see #setDataSource\n\t * @see #setIncrementerName\n\t */",
        ),
        (
            "\t/**\n\t * Convenience constructor.\n\t * @param dataSource the DataSource to use\n\t * @param incrementerName the name of the sequence/table to use\n\t */",
            "\t/**\n\t * 便捷构造器。\n\t * @param dataSource 要使用的 DataSource\n\t * @param incrementerName 要使用的序列/表名\n\t */",
        ),
        (
            "\t/**\n\t * Set the data source to retrieve the value from.\n\t */",
            "\t/**\n\t * 设置用于获取值的数据源。\n\t */",
        ),
        (
            "\t/**\n\t * Return the data source to retrieve the value from.\n\t */",
            "\t/**\n\t * 返回用于获取值的数据源。\n\t */",
        ),
        (
            "\t/**\n\t * Set the name of the sequence/table.\n\t */",
            "\t/**\n\t * 设置序列/表名。\n\t */",
        ),
        (
            "\t/**\n\t * Return the name of the sequence/table.\n\t */",
            "\t/**\n\t * 返回序列/表名。\n\t */",
        ),
        (
            "\t/**\n\t * Set the padding length, i.e. the length to which a string result\n\t * should be prepended with zeroes.\n\t */",
            "\t/**\n\t * 设置填充长度，即 String 结果前补零的目标长度。\n\t */",
        ),
        (
            "\t/**\n\t * Return the padding length for String values.\n\t */",
            "\t/**\n\t * 返回 String 值的填充长度。\n\t */",
        ),
        (
            "\t/**\n\t * Determine the next key to use, as a long.\n\t * @return the key to use as a long. It will eventually be converted later\n\t * in another format by the public concrete methods of this class.\n\t */",
            "\t/**\n\t * 确定下一个要使用的键（long 形式）。\n\t * @return 以 long 表示的键，后续由本类 public 具体方法转换为其他格式。\n\t */",
        ),
    ],
    "AbstractIdentityColumnMaxValueIncrementer.java": [
        (
            "/**\n * Abstract base class for {@link DataFieldMaxValueIncrementer} implementations\n * which are based on identity columns in a sequence-like table.\n *\n * @author Juergen Hoeller\n * @author Thomas Risberg\n * @since 4.1.2\n */",
            "/**\n * 基于类序列表中 identity 列的 {@link DataFieldMaxValueIncrementer} 实现的抽象基类。\n *\n * @author Juergen Hoeller\n * @author Thomas Risberg\n * @since 4.1.2\n */",
        ),
        (
            "\t/** The current cache of values. */",
            "\t/** 当前值缓存。 */",
        ),
        (
            "\t/** The next id to serve from the value cache. */",
            "\t/** 从值缓存中提供的下一个 id 索引。 */",
        ),
        (
            "\t/**\n\t * Default constructor for bean property style usage.\n\t * @see #setDataSource\n\t * @see #setIncrementerName\n\t * @see #setColumnName\n\t */",
            "\t/**\n\t * Bean 属性风格使用的默认构造器。\n\t * @see #setDataSource\n\t * @see #setIncrementerName\n\t * @see #setColumnName\n\t */",
        ),
        (
            "\t/**\n\t * Specify whether to delete the entire range below the current maximum key value\n\t * ({@code false} - the default), or the specifically generated values ({@code true}).\n\t * The former mode will use a where range clause whereas the latter will use an in\n\t * clause starting with the lowest value minus 1, just preserving the maximum value.\n\t */",
            "\t/**\n\t * 指定是删除当前最大键值以下的整个范围（{@code false}，默认），\n\t * 还是仅删除本次生成的值（{@code true}）。\n\t * 前者使用 where 范围子句，后者使用 in 子句（从最小值减 1 起），仅保留最大值。\n\t */",
        ),
        (
            "\t/**\n\t * Return whether to delete the entire range below the current maximum key value\n\t * ({@code false} - the default), or the specifically generated values ({@code true}).\n\t */",
            "\t/**\n\t * 返回是否删除当前最大键值以下的整个范围（{@code false}，默认），\n\t * 还是仅删除本次生成的值（{@code true}）。\n\t */",
        ),
        (
            "\t\t\t/*\n\t\t\t* Need to use straight JDBC code because we need to make sure that the insert and select\n\t\t\t* are performed on the same connection (otherwise we can't be sure that @@identity\n\t\t\t* returns the correct value)\n\t\t\t*/",
            "\t\t\t/*\n\t\t\t* 须使用原生 JDBC，确保 insert 与 select 在同一连接上执行\n\t\t\t* （否则无法保证 @@identity 返回正确值）\n\t\t\t*/",
        ),
        (
            "\t/**\n\t * Statement to use to increment the \"sequence\" value.\n\t * @return the SQL statement to use\n\t */",
            "\t/**\n\t * 用于递增\"序列\"值的语句。\n\t * @return 要使用的 SQL 语句\n\t */",
        ),
        (
            "\t/**\n\t * Statement to use to obtain the current identity value.\n\t * @return the SQL statement to use\n\t */",
            "\t/**\n\t * 用于获取当前 identity 值的语句。\n\t * @return 要使用的 SQL 语句\n\t */",
        ),
        (
            "\t/**\n\t * Statement to use to clean up \"sequence\" values.\n\t * <p>The default implementation either deletes the entire range below\n\t * the current maximum value, or the specifically generated values\n\t * (starting with the lowest minus 1, just preserving the maximum value)\n\t * - according to the {@link #isDeleteSpecificValues()} setting.\n\t * @param values the currently generated key values\n\t * (the number of values corresponds to {@link #getCacheSize()})\n\t * @return the SQL statement to use\n\t */",
            "\t/**\n\t * 用于清理\"序列\"值的语句。\n\t * <p>默认实现根据 {@link #isDeleteSpecificValues()} 设置，\n\t * 删除当前最大值以下的整个范围，或本次生成的值（从最小值减 1 起，保留最大值）。\n\t * @param values 当前生成的键值（数量对应 {@link #getCacheSize()}）\n\t * @return 要使用的 SQL 语句\n\t */",
        ),
    ],
    "AbstractSequenceMaxValueIncrementer.java": [
        (
            "/**\n * Abstract base class for {@link DataFieldMaxValueIncrementer} implementations that use\n * a database sequence. Subclasses need to provide the database-specific SQL to use.\n *\n * @author Juergen Hoeller\n * @since 26.02.2004\n * @see #getSequenceQuery\n */",
            "/**\n * 使用数据库序列的 {@link DataFieldMaxValueIncrementer} 实现的抽象基类。\n * 子类须提供数据库特定的 SQL。\n *\n * @author Juergen Hoeller\n * @since 26.02.2004\n * @see #getSequenceQuery\n */",
        ),
        (
            "\t/**\n\t * Default constructor for bean property style usage.\n\t * @see #setDataSource\n\t * @see #setIncrementerName\n\t */",
            "\t/**\n\t * Bean 属性风格使用的默认构造器。\n\t * @see #setDataSource\n\t * @see #setIncrementerName\n\t */",
        ),
        (
            "\t/**\n\t * Convenience constructor.\n\t * @param dataSource the DataSource to use\n\t * @param incrementerName the name of the sequence/table to use\n\t */",
            "\t/**\n\t * 便捷构造器。\n\t * @param dataSource 要使用的 DataSource\n\t * @param incrementerName 要使用的序列/表名\n\t */",
        ),
        (
            "\t/**\n\t * Executes the SQL as specified by {@link #getSequenceQuery()}.\n\t */",
            "\t/**\n\t * 执行 {@link #getSequenceQuery()} 指定的 SQL。\n\t */",
        ),
        (
            "\t/**\n\t * Return the database-specific query to use for retrieving a sequence value.\n\t * <p>The provided SQL is supposed to result in a single row with a single\n\t * column that allows for extracting a {@code long} value.\n\t */",
            "\t/**\n\t * 返回用于获取序列值的数据库特定查询。\n\t * <p>SQL 应返回单行单列，以便提取 {@code long} 值。\n\t */",
        ),
    ],
    "DataFieldMaxValueIncrementer.java": [
        (
            "/**\n * Interface that defines contract of incrementing any data store field's\n * maximum value. Works much like a sequence number generator.\n *\n * <p>Typical implementations may use standard SQL, native RDBMS sequences\n * or Stored Procedures to do the job.\n *\n * @author Dmitriy Kopylenko\n * @author Jean-Pierre Pawlak\n * @author Juergen Hoeller\n */",
            "/**\n * 定义递增数据存储字段最大值的契约接口，类似序列号生成器。\n *\n * <p>典型实现可使用标准 SQL、原生 RDBMS 序列或存储过程。\n *\n * @author Dmitriy Kopylenko\n * @author Jean-Pierre Pawlak\n * @author Juergen Hoeller\n */",
        ),
        (
            "\t/**\n\t * Increment the data store field's max value as int.\n\t * @return int next data store value such as <b>max + 1</b>\n\t * @throws org.springframework.dao.DataAccessException in case of errors\n\t */",
            "\t/**\n\t * 以 int 递增数据存储字段的最大值。\n\t * @return 下一个数据存储值，如 <b>max + 1</b>\n\t * @throws org.springframework.dao.DataAccessException 出错时\n\t */",
        ),
        (
            "\t/**\n\t * Increment the data store field's max value as long.\n\t * @return int next data store value such as <b>max + 1</b>\n\t * @throws org.springframework.dao.DataAccessException in case of errors\n\t */",
            "\t/**\n\t * 以 long 递增数据存储字段的最大值。\n\t * @return 下一个数据存储值，如 <b>max + 1</b>\n\t * @throws org.springframework.dao.DataAccessException 出错时\n\t */",
        ),
        (
            "\t/**\n\t * Increment the data store field's max value as String.\n\t * @return next data store value such as <b>max + 1</b>\n\t * @throws org.springframework.dao.DataAccessException in case of errors\n\t */",
            "\t/**\n\t * 以 String 递增数据存储字段的最大值。\n\t * @return 下一个数据存储值，如 <b>max + 1</b>\n\t * @throws org.springframework.dao.DataAccessException 出错时\n\t */",
        ),
    ],
    "Db2LuwMaxValueIncrementer.java": [
        (
            "/**\n * {@link DataFieldMaxValueIncrementer} that retrieves the next value\n * of a given sequence on DB2 LUW (for Linux, Unix and Windows).\n *\n * <p>Thanks to Mark MacMahon for the suggestion!\n *\n * @author Juergen Hoeller\n * @since 4.3.15\n * @see Db2MainframeMaxValueIncrementer\n */",
            "/**\n * 在 DB2 LUW（Linux、Unix 和 Windows）上获取给定序列下一值的 {@link DataFieldMaxValueIncrementer}。\n *\n * <p>感谢 Mark MacMahon 的建议！\n *\n * @author Juergen Hoeller\n * @since 4.3.15\n * @see Db2MainframeMaxValueIncrementer\n */",
        ),
        (
            "\t/**\n\t * Default constructor for bean property style usage.\n\t * @see #setDataSource\n\t * @see #setIncrementerName\n\t */",
            "\t/**\n\t * Bean 属性风格使用的默认构造器。\n\t * @see #setDataSource\n\t * @see #setIncrementerName\n\t */",
        ),
        (
            "\t/**\n\t * Convenience constructor.\n\t * @param dataSource the DataSource to use\n\t * @param incrementerName the name of the sequence/table to use\n\t */",
            "\t/**\n\t * 便捷构造器。\n\t * @param dataSource 要使用的 DataSource\n\t * @param incrementerName 要使用的序列/表名\n\t */",
        ),
    ],
    "Db2MainframeMaxValueIncrementer.java": [
        (
            "/**\n * {@link DataFieldMaxValueIncrementer} that retrieves the next value\n * of a given sequence on DB2 for the mainframe (z/OS, DB2/390, DB2/400).\n *\n * <p>Thanks to Jens Eickmeyer for the suggestion!\n *\n * @author Juergen Hoeller\n * @since 4.3.15\n * @see Db2LuwMaxValueIncrementer\n */",
            "/**\n * 在大型机 DB2（z/OS、DB2/390、DB2/400）上获取给定序列下一值的 {@link DataFieldMaxValueIncrementer}。\n *\n * <p>感谢 Jens Eickmeyer 的建议！\n *\n * @author Juergen Hoeller\n * @since 4.3.15\n * @see Db2LuwMaxValueIncrementer\n */",
        ),
        (
            "\t/**\n\t * Default constructor for bean property style usage.\n\t * @see #setDataSource\n\t * @see #setIncrementerName\n\t */",
            "\t/**\n\t * Bean 属性风格使用的默认构造器。\n\t * @see #setDataSource\n\t * @see #setIncrementerName\n\t */",
        ),
        (
            "\t/**\n\t * Convenience constructor.\n\t * @param dataSource the DataSource to use\n\t * @param incrementerName the name of the sequence/table to use\n\t */",
            "\t/**\n\t * 便捷构造器。\n\t * @param dataSource 要使用的 DataSource\n\t * @param incrementerName 要使用的序列/表名\n\t */",
        ),
    ],
    "DerbyMaxValueIncrementer.java": [
        (
            "/**\n * {@link DataFieldMaxValueIncrementer} that increments the maximum value of a given Derby table\n * with the equivalent of an auto-increment column. Note: If you use this class, your Derby key\n * column should <i>NOT</i> be defined as an IDENTITY column, as the sequence table does the job.\n *\n * <p>The sequence is kept in a table. There should be one sequence table per\n * table that needs an auto-generated key.\n *\n * <p>Derby requires an additional column to be used for the insert since it is impossible\n * to insert a null into the identity column and have the value generated.  This is solved by\n * providing the name of a dummy column that also must be created in the sequence table.\n *\n * <p>Example:\n *\n * <pre class=\"code\">create table tab (id int not null primary key, text varchar(100));\n * create table tab_sequence (value int generated always as identity, dummy char(1));\n * insert into tab_sequence (dummy) values(null);</pre>\n *\n * If \"cacheSize\" is set, the intermediate values are served without querying the\n * database. If the server or your application is stopped or crashes or a transaction\n * is rolled back, the unused values will never be served. The maximum hole size in\n * numbering is consequently the value of cacheSize.\n *\n * <b>HINT:</b> Since Derby supports the JDBC {@code getGeneratedKeys} method,\n * it is recommended to use IDENTITY columns directly in the tables and then utilize\n * a {@link org.springframework.jdbc.support.KeyHolder} when calling the\n * {@code update(PreparedStatementCreator psc, KeyHolder generatedKeyHolder)}\n * method of the {@link org.springframework.jdbc.core.JdbcTemplate}.\n *\n * <p>Thanks to Endre Stolsvik for the suggestion!\n *\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @since 2.5\n */",
            "/**\n * 递增给定 Derby 表最大值的 {@link DataFieldMaxValueIncrementer}，等效于自增列。\n * 注意：使用本类时，Derby 键列<i>不应</i>定义为 IDENTITY 列，序列表负责生成键。\n *\n * <p>序列保存在表中，每个需要自动生成键的表应有一个序列表。\n *\n * <p>Derby 插入时需要额外列，因为无法向 identity 列插入 null 并生成值。\n * 通过在序列表中创建 dummy 列并在插入时使用其名称解决。\n *\n * <p>示例：\n *\n * <pre class=\"code\">create table tab (id int not null primary key, text varchar(100));\n * create table tab_sequence (value int generated always as identity, dummy char(1));\n * insert into tab_sequence (dummy) values(null);</pre>\n *\n * 若设置 \"cacheSize\"，中间值无需查询数据库即可提供。\n * 若服务器或应用停止、崩溃或事务回滚，未使用的值将永远不会被使用，\n * 因此编号最大空洞即为 cacheSize 的值。\n *\n * <b>提示：</b>Derby 支持 JDBC {@code getGeneratedKeys} 方法，\n * 建议在表中直接使用 IDENTITY 列，并在调用 {@link org.springframework.jdbc.core.JdbcTemplate}\n * 的 {@code update(PreparedStatementCreator psc, KeyHolder generatedKeyHolder)} 时使用\n * {@link org.springframework.jdbc.support.KeyHolder}。\n *\n * <p>感谢 Endre Stolsvik 的建议！\n *\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @since 2.5\n */",
        ),
        (
            "\t/** The default for dummy name. */",
            "\t/** dummy 列名的默认值。 */",
        ),
        (
            "\t/** The name of the dummy column used for inserts. */",
            "\t/** 插入时使用的 dummy 列名。 */",
        ),
        (
            "\t/**\n\t * Default constructor for bean property style usage.\n\t * @see #setDataSource\n\t * @see #setIncrementerName\n\t * @see #setColumnName\n\t */",
            "\t/**\n\t * Bean 属性风格使用的默认构造器。\n\t * @see #setDataSource\n\t * @see #setIncrementerName\n\t * @see #setColumnName\n\t */",
        ),
        (
            "\t/**\n\t * Convenience constructor.\n\t * @param dataSource the DataSource to use\n\t * @param incrementerName the name of the sequence/table to use\n\t * @param columnName the name of the column in the sequence table to use\n\t */",
            "\t/**\n\t * 便捷构造器。\n\t * @param dataSource 要使用的 DataSource\n\t * @param incrementerName 要使用的序列/表名\n\t * @param columnName 序列表中要使用的列名\n\t */",
        ),
        (
            "\t/**\n\t * Convenience constructor.\n\t * @param dataSource the DataSource to use\n\t * @param incrementerName the name of the sequence/table to use\n\t * @param columnName the name of the column in the sequence table to use\n\t * @param dummyName the name of the dummy column used for inserts\n\t */",
            "\t/**\n\t * 便捷构造器。\n\t * @param dataSource 要使用的 DataSource\n\t * @param incrementerName 要使用的序列/表名\n\t * @param columnName 序列表中要使用的列名\n\t * @param dummyName 插入时使用的 dummy 列名\n\t */",
        ),
        (
            "\t/**\n\t * Set the name of the dummy column.\n\t */",
            "\t/**\n\t * 设置 dummy 列名。\n\t */",
        ),
        (
            "\t/**\n\t * Return the name of the dummy column.\n\t */",
            "\t/**\n\t * 返回 dummy 列名。\n\t */",
        ),
    ],
    "H2SequenceMaxValueIncrementer.java": [
        (
            "/**\n * {@link DataFieldMaxValueIncrementer} that retrieves the next value\n * of a given H2 sequence.\n *\n * @author Thomas Risberg\n * @author Henning Pöttker\n * @since 2.5\n */",
            "/**\n * 获取给定 H2 序列下一值的 {@link DataFieldMaxValueIncrementer}。\n *\n * @author Thomas Risberg\n * @author Henning Pöttker\n * @since 2.5\n */",
        ),
        (
            "\t/**\n\t * Default constructor for bean property style usage.\n\t * @see #setDataSource\n\t * @see #setIncrementerName\n\t */",
            "\t/**\n\t * Bean 属性风格使用的默认构造器。\n\t * @see #setDataSource\n\t * @see #setIncrementerName\n\t */",
        ),
        (
            "\t/**\n\t * Convenience constructor.\n\t * @param dataSource the DataSource to use\n\t * @param incrementerName the name of the sequence/table to use\n\t */",
            "\t/**\n\t * 便捷构造器。\n\t * @param dataSource 要使用的 DataSource\n\t * @param incrementerName 要使用的序列/表名\n\t */",
        ),
    ],
    "HanaSequenceMaxValueIncrementer.java": [
        (
            "/**\n * {@link DataFieldMaxValueIncrementer} that retrieves the next value\n * of a given SAP HANA sequence.\n *\n * @author Jonathan Bregler\n * @author Juergen Hoeller\n * @since 4.3.15\n */",
            "/**\n * 获取给定 SAP HANA 序列下一值的 {@link DataFieldMaxValueIncrementer}。\n *\n * @author Jonathan Bregler\n * @author Juergen Hoeller\n * @since 4.3.15\n */",
        ),
        (
            "\t/**\n\t * Default constructor for bean property style usage.\n\t * @see #setDataSource\n\t * @see #setIncrementerName\n\t */",
            "\t/**\n\t * Bean 属性风格使用的默认构造器。\n\t * @see #setDataSource\n\t * @see #setIncrementerName\n\t */",
        ),
        (
            "\t/**\n\t * Convenience constructor.\n\t * @param dataSource the DataSource to use\n\t * @param incrementerName the name of the sequence/table to use\n\t */",
            "\t/**\n\t * 便捷构造器。\n\t * @param dataSource 要使用的 DataSource\n\t * @param incrementerName 要使用的序列/表名\n\t */",
        ),
    ],
    "HsqlMaxValueIncrementer.java": [
        (
            "/**\n * {@link DataFieldMaxValueIncrementer} that increments the maximum value of a given HSQL table\n * with the equivalent of an auto-increment column. Note: If you use this class, your HSQL\n * key column should <i>NOT</i> be auto-increment, as the sequence table does the job.\n *\n * <p>The sequence is kept in a table. There should be one sequence table per\n * table that needs an auto-generated key.\n *\n * <p>Example:\n *\n * <pre class=\"code\">create table tab (id int not null primary key, text varchar(100));\n * create table tab_sequence (value identity);\n * insert into tab_sequence values(0);</pre>\n *\n * If \"cacheSize\" is set, the intermediate values are served without querying the\n * database. If the server or your application is stopped or crashes or a transaction\n * is rolled back, the unused values will never be served. The maximum hole size in\n * numbering is consequently the value of cacheSize.\n *\n * <p><b>NOTE:</b> HSQL now supports sequences and you should consider using them instead:\n * {@link HsqlSequenceMaxValueIncrementer}\n *\n * @author Jean-Pierre Pawlak\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @see HsqlSequenceMaxValueIncrementer\n */",
            "/**\n * 递增给定 HSQL 表最大值的 {@link DataFieldMaxValueIncrementer}，等效于自增列。\n * 注意：使用本类时，HSQL 键列<i>不应</i>设为 auto-increment，序列表负责生成键。\n *\n * <p>序列保存在表中，每个需要自动生成键的表应有一个序列表。\n *\n * <p>示例：\n *\n * <pre class=\"code\">create table tab (id int not null primary key, text varchar(100));\n * create table tab_sequence (value identity);\n * insert into tab_sequence values(0);</pre>\n *\n * 若设置 \"cacheSize\"，中间值无需查询数据库即可提供。\n * 若服务器或应用停止、崩溃或事务回滚，未使用的值将永远不会被使用，\n * 因此编号最大空洞即为 cacheSize 的值。\n *\n * <p><b>注意：</b>HSQL 现已支持序列，建议改用 {@link HsqlSequenceMaxValueIncrementer}。\n *\n * @author Jean-Pierre Pawlak\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @see HsqlSequenceMaxValueIncrementer\n */",
        ),
        (
            "\t/**\n\t * Default constructor for bean property style usage.\n\t * @see #setDataSource\n\t * @see #setIncrementerName\n\t * @see #setColumnName\n\t */",
            "\t/**\n\t * Bean 属性风格使用的默认构造器。\n\t * @see #setDataSource\n\t * @see #setIncrementerName\n\t * @see #setColumnName\n\t */",
        ),
        (
            "\t/**\n\t * Convenience constructor.\n\t * @param dataSource the DataSource to use\n\t * @param incrementerName the name of the sequence/table to use\n\t * @param columnName the name of the column in the sequence table to use\n\t */",
            "\t/**\n\t * 便捷构造器。\n\t * @param dataSource 要使用的 DataSource\n\t * @param incrementerName 要使用的序列/表名\n\t * @param columnName 序列表中要使用的列名\n\t */",
        ),
    ],
}
