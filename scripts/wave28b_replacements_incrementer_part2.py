"""Additional wave28b incrementer replacements (larger classes)."""

INCREMENTER_PART2: dict[str, list[tuple[str, str]]] = {
    "MySQLMaxValueIncrementer.java": [
        (
            "/**\n * {@link DataFieldMaxValueIncrementer} that increments the maximum value of a given MySQL table\n * with the equivalent of an auto-increment column. Note: If you use this class, your MySQL\n * key column should <i>NOT</i> be auto-increment, as the sequence table does the job.\n *\n * <p>The sequence is kept in a table; there should be one sequence table per\n * table that needs an auto-generated key. The storage engine used by the sequence table\n * can be MYISAM or INNODB since the sequences are allocated using a separate connection\n * without being affected by any other transactions that might be in progress.\n *\n * <p>Example:\n *\n * <pre class=\"code\">\n * create table tab (id int unsigned not null primary key, text varchar(100));\n * create table tab_sequence (value int not null);\n * insert into tab_sequence values(0);</pre>\n *\n * <p>If {@code cacheSize} is set, the intermediate values are served without querying the\n * database. If the server or your application is stopped or crashes or a transaction\n * is rolled back, the unused values will never be served. The maximum hole size in\n * numbering is consequently the value of {@code cacheSize}.\n *\n * <p>It is possible to avoid acquiring a new connection for the incrementer by setting the\n * \"useNewConnection\" property to false. In this case you <i>MUST</i> use a non-transactional\n * storage engine like MYISAM when defining the incrementer table.\n *\n * <p>Note that {@code MySQLMaxValueIncrementer} is compatible with\n * <a href=\"https://dev.mysql.com/doc/refman/8.0/en/mysql-tips.html#safe-updates\">MySQL safe updates mode</a>.\n *\n * @author Jean-Pierre Pawlak\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @author Sam Brannen\n */",
            "/**\n * 递增给定 MySQL 表中相当于自增列的最大值的 {@link DataFieldMaxValueIncrementer}。\n * 注意：使用本类时，MySQL 主键列<i>不应</i>设为 auto-increment，序列表负责生成键值。\n *\n * <p>序列保存在一张表中；每个需要自动生成主键的表应有一张对应的序列表。\n * 序列表可使用 MYISAM 或 INNODB 存储引擎，因为序列通过独立连接分配，\n * 不受其他进行中事务的影响。\n *\n * <p>示例：\n *\n * <pre class=\"code\">\n * create table tab (id int unsigned not null primary key, text varchar(100));\n * create table tab_sequence (value int not null);\n * insert into tab_sequence values(0);</pre>\n *\n * <p>若设置了 {@code cacheSize}，中间值将不经查询数据库直接分配。\n * 若服务器或应用停止、崩溃，或事务回滚，未使用的值将永远不会被分配。\n * 因此编号中可能出现的最大空洞大小等于 {@code cacheSize} 的值。\n *\n * <p>可将 \"useNewConnection\" 属性设为 false 以避免为递增器获取新连接。\n * 此时<i>必须</i>为序列表使用 MYISAM 等非事务性存储引擎。\n *\n * <p>{@code MySQLMaxValueIncrementer} 兼容\n * <a href=\"https://dev.mysql.com/doc/refman/8.0/en/mysql-tips.html#safe-updates\">MySQL 安全更新模式</a>。\n *\n * @author Jean-Pierre Pawlak\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @author Sam Brannen\n */",
        ),
        (
            "/** The SQL string for retrieving the new sequence value. */",
            "/** 用于检索新序列值的 SQL 字符串。 */",
        ),
        (
            "/** The next id to serve. */",
            "/** 下一个待分配的 id。 */",
        ),
        (
            "/** The max id to serve. */",
            "/** 当前可分配的最大 id。 */",
        ),
        (
            "/** Whether to use a new connection for the incrementer. */",
            "/** 是否为递增器使用新连接。 */",
        ),
        (
            "\t/**\n\t * Default constructor for bean property style usage.\n\t * @see #setDataSource\n\t * @see #setIncrementerName\n\t * @see #setColumnName\n\t */",
            "\t/**\n\t * 允许作为 JavaBean 使用的默认构造器。\n\t * @see #setDataSource\n\t * @see #setIncrementerName\n\t * @see #setColumnName\n\t */",
        ),
        (
            "\t/**\n\t * Convenience constructor.\n\t * @param dataSource the DataSource to use\n\t * @param incrementerName the name of the sequence table to use\n\t * @param columnName the name of the column in the sequence table to use\n\t */",
            "\t/**\n\t * 便捷构造器。\n\t * @param dataSource 要使用的 DataSource\n\t * @param incrementerName 要使用的序列表名\n\t * @param columnName 序列表中要使用的列名\n\t */",
        ),
        (
            "\t/**\n\t * Set whether to use a new connection for the incrementer.\n\t * <p>{@code true} is necessary to support transactional storage engines,\n\t * using an isolated separate transaction for the increment operation.\n\t * {@code false} is sufficient if the storage engine of the sequence table\n\t * is non-transactional (like MYISAM), avoiding the effort of acquiring an\n\t * extra {@code Connection} for the increment operation.\n\t * <p>Default is {@code true}.\n\t * @since 4.3.6\n\t * @see DataSource#getConnection()\n\t */",
            "\t/**\n\t * 设置是否为递增器使用新连接。\n\t * <p>{@code true} 用于支持事务性存储引擎，递增操作在隔离的独立事务中执行。\n\t * 若序列表使用 MYISAM 等非事务性存储引擎，{@code false} 即可，\n\t * 无需为递增操作额外获取 {@code Connection}。\n\t * <p>默认为 {@code true}。\n\t * @since 4.3.6\n\t * @see DataSource#getConnection()\n\t */",
        ),
        (
            "\t\t\t/*\n\t\t\t* If useNewConnection is true, then we obtain a non-managed connection so our modifications\n\t\t\t* are handled in a separate transaction. If it is false, then we use the current transaction's\n\t\t\t* connection relying on the use of a non-transactional storage engine like MYISAM for the\n\t\t\t* incrementer table. We also use straight JDBC code because we need to make sure that the insert\n\t\t\t* and select are performed on the same connection (otherwise we can't be sure that last_insert_id()\n\t\t\t* returned the correct value).\n\t\t\t*/",
            "\t\t\t/*\n\t\t\t* 若 useNewConnection 为 true，则获取非托管连接，使修改在独立事务中处理。\n\t\t\t* 若为 false，则使用当前事务的连接，依赖 MYISAM 等非事务性存储引擎的序列表。\n\t\t\t* 同时使用原生 JDBC 代码，确保 insert 与 select 在同一连接上执行\n\t\t\t* （否则无法保证 last_insert_id() 返回正确值）。\n\t\t\t*/",
        ),
        (
            "\t\t\t\t// Increment the sequence column...",
            "\t\t\t\t// 递增序列列...",
        ),
        (
            "\t\t\t\t// Retrieve the new max of the sequence column...",
            "\t\t\t\t// 检索序列列的新最大值...",
        ),
    ],
    "SqlServerMaxValueIncrementer.java": [
        (
            "/**\n * {@link DataFieldMaxValueIncrementer} that increments the maximum value of a given SQL Server table\n * with the equivalent of an auto-increment column. Note: If you use this class, your table key\n * column should <i>NOT</i> be defined as an IDENTITY column, as the sequence table does the job.\n *\n * <p>This class is intended to be used with Microsoft SQL Server.\n *\n * <p>The sequence is kept in a table. There should be one sequence table per\n * table that needs an auto-generated key.\n *\n * <p>Example:\n *\n * <pre class=\"code\">create table tab (id int not null primary key, text varchar(100))\n * create table tab_sequence (id bigint identity)\n * insert into tab_sequence default values</pre>\n *\n * If \"cacheSize\" is set, the intermediate values are served without querying the\n * database. If the server or your application is stopped or crashes or a transaction\n * is rolled back, the unused values will never be served. The maximum hole size in\n * numbering is consequently the value of cacheSize.\n *\n * <b>HINT:</b> Since Microsoft SQL Server supports the JDBC {@code getGeneratedKeys}\n * method, it is recommended to use IDENTITY columns directly in the tables and then\n * use a {@link org.springframework.jdbc.core.simple.SimpleJdbcInsert} or a\n * {@link org.springframework.jdbc.support.KeyHolder} when calling the\n * {@code update(PreparedStatementCreator psc, KeyHolder generatedKeyHolder)}\n * method of the {@link org.springframework.jdbc.core.JdbcTemplate}.\n *\n * <p>Thanks to Preben Nilsson for the suggestion!\n *\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @since 2.5.5\n */",
            "/**\n * 递增给定 SQL Server 表中相当于自增列的最大值的 {@link DataFieldMaxValueIncrementer}。\n * 注意：使用本类时，表主键列<i>不应</i>定义为 IDENTITY 列，序列表负责生成键值。\n *\n * <p>本类适用于 Microsoft SQL Server。\n *\n * <p>序列保存在一张表中；每个需要自动生成主键的表应有一张对应的序列表。\n *\n * <p>示例：\n *\n * <pre class=\"code\">create table tab (id int not null primary key, text varchar(100))\n * create table tab_sequence (id bigint identity)\n * insert into tab_sequence default values</pre>\n *\n * 若设置了 \"cacheSize\"，中间值将不经查询数据库直接分配。\n * 若服务器或应用停止、崩溃，或事务回滚，未使用的值将永远不会被分配。\n * 因此编号中可能出现的最大空洞大小等于 cacheSize 的值。\n *\n * <b>HINT:</b> 由于 Microsoft SQL Server 支持 JDBC {@code getGeneratedKeys} 方法，\n * 建议在表中直接使用 IDENTITY 列，并在调用 {@link org.springframework.jdbc.core.JdbcTemplate} 的\n * {@code update(PreparedStatementCreator psc, KeyHolder generatedKeyHolder)} 方法时\n * 使用 {@link org.springframework.jdbc.core.simple.SimpleJdbcInsert} 或\n * {@link org.springframework.jdbc.support.KeyHolder}。\n *\n * <p>感谢 Preben Nilsson 的建议！\n *\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @since 2.5.5\n */",
        ),
        (
            "\t/**\n\t * Default constructor for bean property style usage.\n\t * @see #setDataSource\n\t * @see #setIncrementerName\n\t * @see #setColumnName\n\t */",
            "\t/**\n\t * 允许作为 JavaBean 使用的默认构造器。\n\t * @see #setDataSource\n\t * @see #setIncrementerName\n\t * @see #setColumnName\n\t */",
        ),
        (
            "\t/**\n\t * Convenience constructor.\n\t * @param dataSource the DataSource to use\n\t * @param incrementerName the name of the sequence/table to use\n\t * @param columnName the name of the column in the sequence table to use\n\t */",
            "\t/**\n\t * 便捷构造器。\n\t * @param dataSource 要使用的 DataSource\n\t * @param incrementerName 要使用的序列/表名\n\t * @param columnName 序列表中要使用的列名\n\t */",
        ),
    ],
    "SybaseMaxValueIncrementer.java": [
        (
            "/**\n * {@link DataFieldMaxValueIncrementer} that increments the maximum value of a given Sybase table\n * with the equivalent of an auto-increment column. Note: If you use this class, your table key\n * column should <i>NOT</i> be defined as an IDENTITY column, as the sequence table does the job.\n *\n * <p>This class is intended to be used with Sybase Adaptive Server.\n *\n * <p>The sequence is kept in a table. There should be one sequence table per\n * table that needs an auto-generated key.\n *\n * <p>Example:\n *\n * <pre class=\"code\">create table tab (id int not null primary key, text varchar(100))\n * create table tab_sequence (id bigint identity)\n * insert into tab_sequence values()</pre>\n *\n * If \"cacheSize\" is set, the intermediate values are served without querying the\n * database. If the server or your application is stopped or crashes or a transaction\n * is rolled back, the unused values will never be served. The maximum hole size in\n * numbering is consequently the value of cacheSize.\n *\n * <b>HINT:</b> Since Sybase Adaptive Server supports the JDBC {@code getGeneratedKeys}\n * method, it is recommended to use IDENTITY columns directly in the tables and then\n * use a {@link org.springframework.jdbc.core.simple.SimpleJdbcInsert} or use a\n * {@link org.springframework.jdbc.support.KeyHolder} when calling the\n * {@code update(PreparedStatementCreator psc, KeyHolder generatedKeyHolder)}\n * method of the {@link org.springframework.jdbc.core.JdbcTemplate}.\n *\n * <p>Thanks to Yinwei Liu for the suggestion!\n *\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @since 2.5.5\n */",
            "/**\n * 递增给定 Sybase 表中相当于自增列的最大值的 {@link DataFieldMaxValueIncrementer}。\n * 注意：使用本类时，表主键列<i>不应</i>定义为 IDENTITY 列，序列表负责生成键值。\n *\n * <p>本类适用于 Sybase Adaptive Server。\n *\n * <p>序列保存在一张表中；每个需要自动生成主键的表应有一张对应的序列表。\n *\n * <p>示例：\n *\n * <pre class=\"code\">create table tab (id int not null primary key, text varchar(100))\n * create table tab_sequence (id bigint identity)\n * insert into tab_sequence values()</pre>\n *\n * 若设置了 \"cacheSize\"，中间值将不经查询数据库直接分配。\n * 若服务器或应用停止、崩溃，或事务回滚，未使用的值将永远不会被分配。\n * 因此编号中可能出现的最大空洞大小等于 cacheSize 的值。\n *\n * <b>HINT:</b> 由于 Sybase Adaptive Server 支持 JDBC {@code getGeneratedKeys} 方法，\n * 建议在表中直接使用 IDENTITY 列，并在调用 {@link org.springframework.jdbc.core.JdbcTemplate} 的\n * {@code update(PreparedStatementCreator psc, KeyHolder generatedKeyHolder)} 方法时\n * 使用 {@link org.springframework.jdbc.core.simple.SimpleJdbcInsert} 或\n * {@link org.springframework.jdbc.support.KeyHolder}。\n *\n * <p>感谢 Yinwei Liu 的建议！\n *\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @since 2.5.5\n */",
        ),
        (
            "\t/**\n\t * Default constructor for bean property style usage.\n\t * @see #setDataSource\n\t * @see #setIncrementerName\n\t * @see #setColumnName\n\t */",
            "\t/**\n\t * 允许作为 JavaBean 使用的默认构造器。\n\t * @see #setDataSource\n\t * @see #setIncrementerName\n\t * @see #setColumnName\n\t */",
        ),
        (
            "\t/**\n\t * Convenience constructor.\n\t * @param dataSource the DataSource to use\n\t * @param incrementerName the name of the sequence/table to use\n\t * @param columnName the name of the column in the sequence table to use\n\t */",
            "\t/**\n\t * 便捷构造器。\n\t * @param dataSource 要使用的 DataSource\n\t * @param incrementerName 要使用的序列/表名\n\t * @param columnName 序列表中要使用的列名\n\t */",
        ),
    ],
    "SybaseAnywhereMaxValueIncrementer.java": [
        (
            "/**\n * {@link DataFieldMaxValueIncrementer} that increments the maximum value of a given Sybase table\n * with the equivalent of an auto-increment column. Note: If you use this class, your table key\n * column should <i>NOT</i> be defined as an IDENTITY column, as the sequence table does the job.\n *\n * <p>This class is intended to be used with Sybase Anywhere.\n *\n * <p>The sequence is kept in a table. There should be one sequence table per\n * table that needs an auto-generated key.\n *\n * <p>Example:\n *\n * <pre class=\"code\">create table tab (id int not null primary key, text varchar(100))\n * create table tab_sequence (id bigint identity)\n * insert into tab_sequence values(DEFAULT)</pre>\n *\n * If \"cacheSize\" is set, the intermediate values are served without querying the\n * database. If the server or your application is stopped or crashes or a transaction\n * is rolled back, the unused values will never be served. The maximum hole size in\n * numbering is consequently the value of cacheSize.\n *\n * <b>HINT:</b> Since Sybase Anywhere supports the JDBC {@code getGeneratedKeys}\n * method, it is recommended to use IDENTITY columns directly in the tables and then\n * use a {@link org.springframework.jdbc.core.simple.SimpleJdbcInsert} or use a\n * {@link org.springframework.jdbc.support.KeyHolder} when calling the\n * {@code update(PreparedStatementCreator psc, KeyHolder generatedKeyHolder)}\n * method of the {@link org.springframework.jdbc.core.JdbcTemplate}.\n *\n * <p>Thanks to Tarald Saxi Stormark for the suggestion!\n *\n * @author Thomas Risberg\n * @since 3.0.5\n */",
            "/**\n * 递增给定 Sybase 表中相当于自增列的最大值的 {@link DataFieldMaxValueIncrementer}。\n * 注意：使用本类时，表主键列<i>不应</i>定义为 IDENTITY 列，序列表负责生成键值。\n *\n * <p>本类适用于 Sybase Anywhere。\n *\n * <p>序列保存在一张表中；每个需要自动生成主键的表应有一张对应的序列表。\n *\n * <p>示例：\n *\n * <pre class=\"code\">create table tab (id int not null primary key, text varchar(100))\n * create table tab_sequence (id bigint identity)\n * insert into tab_sequence values(DEFAULT)</pre>\n *\n * 若设置了 \"cacheSize\"，中间值将不经查询数据库直接分配。\n * 若服务器或应用停止、崩溃，或事务回滚，未使用的值将永远不会被分配。\n * 因此编号中可能出现的最大空洞大小等于 cacheSize 的值。\n *\n * <b>HINT:</b> 由于 Sybase Anywhere 支持 JDBC {@code getGeneratedKeys} 方法，\n * 建议在表中直接使用 IDENTITY 列，并在调用 {@link org.springframework.jdbc.core.JdbcTemplate} 的\n * {@code update(PreparedStatementCreator psc, KeyHolder generatedKeyHolder)} 方法时\n * 使用 {@link org.springframework.jdbc.core.simple.SimpleJdbcInsert} 或\n * {@link org.springframework.jdbc.support.KeyHolder}。\n *\n * <p>感谢 Tarald Saxi Stormark 的建议！\n *\n * @author Thomas Risberg\n * @since 3.0.5\n */",
        ),
        (
            "\t/**\n\t * Default constructor for bean property style usage.\n\t * @see #setDataSource\n\t * @see #setIncrementerName\n\t * @see #setColumnName\n\t */",
            "\t/**\n\t * 允许作为 JavaBean 使用的默认构造器。\n\t * @see #setDataSource\n\t * @see #setIncrementerName\n\t * @see #setColumnName\n\t */",
        ),
        (
            "\t/**\n\t * Convenience constructor.\n\t * @param dataSource the DataSource to use\n\t * @param incrementerName the name of the sequence/table to use\n\t * @param columnName the name of the column in the sequence table to use\n\t */",
            "\t/**\n\t * 便捷构造器。\n\t * @param dataSource 要使用的 DataSource\n\t * @param incrementerName 要使用的序列/表名\n\t * @param columnName 序列表中要使用的列名\n\t */",
        ),
    ],
}
