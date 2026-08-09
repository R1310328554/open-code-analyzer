"""Chinese JavaDoc replacements for springframework wave28b incrementer classes."""

INCREMENTER_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "HsqlSequenceMaxValueIncrementer.java": [
        (
            "/**\n * {@link DataFieldMaxValueIncrementer} that retrieves the next value\n * of a given HSQL sequence.\n *\n * <p>Thanks to Guillaume Bilodeau for the suggestion!\n *\n * <p><b>NOTE:</b> This is an alternative to using a regular table to support\n * generating unique keys that was necessary in previous versions of HSQL.\n *\n * @author Thomas Risberg\n * @since 2.5\n * @see HsqlMaxValueIncrementer\n */",
            "/**\n * 检索给定 HSQL 序列下一个值的 {@link DataFieldMaxValueIncrementer}。\n *\n * <p>感谢 Guillaume Bilodeau 的建议！\n *\n * <p><b>NOTE:</b> 这是使用常规表生成唯一键的替代方案，\n * 在旧版 HSQL 中曾需要这种方式。\n *\n * @author Thomas Risberg\n * @since 2.5\n * @see HsqlMaxValueIncrementer\n */",
        ),
        (
            "\t/**\n\t * Default constructor for bean property style usage.\n\t * @see #setDataSource\n\t * @see #setIncrementerName\n\t */",
            "\t/**\n\t * 允许作为 JavaBean 使用的默认构造器。\n\t * @see #setDataSource\n\t * @see #setIncrementerName\n\t */",
        ),
        (
            "\t/**\n\t * Convenience constructor.\n\t * @param dataSource the DataSource to use\n\t * @param incrementerName the name of the sequence/table to use\n\t */",
            "\t/**\n\t * 便捷构造器。\n\t * @param dataSource 要使用的 DataSource\n\t * @param incrementerName 要使用的序列/表名\n\t */",
        ),
    ],
    "MariaDBSequenceMaxValueIncrementer.java": [
        (
            "/**\n * {@link DataFieldMaxValueIncrementer} that retrieves the next value of a given\n * MariaDB sequence.\n *\n * @author Mahmoud Ben Hassine\n * @since 6.0\n */",
            "/**\n * 检索给定 MariaDB 序列下一个值的 {@link DataFieldMaxValueIncrementer}。\n *\n * @author Mahmoud Ben Hassine\n * @since 6.0\n */",
        ),
        (
            "\t/**\n\t * Default constructor for bean property style usage.\n\t * @see #setDataSource\n\t * @see #setIncrementerName\n\t */",
            "\t/**\n\t * 允许作为 JavaBean 使用的默认构造器。\n\t * @see #setDataSource\n\t * @see #setIncrementerName\n\t */",
        ),
        (
            "\t/**\n\t * Convenience constructor.\n\t * @param dataSource the DataSource to use\n\t * @param incrementerName the name of the sequence to use\n\t */",
            "\t/**\n\t * 便捷构造器。\n\t * @param dataSource 要使用的 DataSource\n\t * @param incrementerName 要使用的序列名\n\t */",
        ),
    ],
    "MySQLIdentityColumnMaxValueIncrementer.java": [
        (
            "/**\n * {@link DataFieldMaxValueIncrementer} that increments the maximum counter value of an\n * auto-increment column of a given MySQL table.\n *\n * <p>The sequence is kept in a table. The storage engine used by the sequence table must be\n * InnoDB in MySQL 8.0 or later since the current maximum auto-increment counter is required to be\n * persisted across restarts of the database server.\n *\n * <p>Example:\n *\n * <pre class=\"code\">\n * create table tab_sequence (`id` bigint unsigned primary key auto_increment);</pre>\n *\n * <p>If {@code cacheSize} is set, the intermediate values are served without querying the\n * database. If the server or your application is stopped or crashes or a transaction\n * is rolled back, the unused values will never be served. The maximum hole size in\n * numbering is consequently the value of {@code cacheSize}.\n *\n * @author Henning Pöttker\n * @since 6.1.2\n */",
            "/**\n * 递增给定 MySQL 表自增列最大计数值的 {@link DataFieldMaxValueIncrementer}。\n *\n * <p>序列保存在一张表中。MySQL 8.0 及更高版本中，序列表必须使用 InnoDB 存储引擎，\n * 因为当前最大自增计数器需要在数据库服务器重启后持久保留。\n *\n * <p>示例：\n *\n * <pre class=\"code\">\n * create table tab_sequence (`id` bigint unsigned primary key auto_increment);</pre>\n *\n * <p>若设置了 {@code cacheSize}，中间值将不经查询数据库直接分配。\n * 若服务器或应用停止、崩溃，或事务回滚，未使用的值将永远不会被分配。\n * 因此编号中可能出现的最大空洞大小等于 {@code cacheSize} 的值。\n *\n * @author Henning Pöttker\n * @since 6.1.2\n */",
        ),
        (
            "\t/**\n\t * Default constructor for bean property style usage.\n\t * @see #setDataSource\n\t * @see #setIncrementerName\n\t * @see #setColumnName\n\t */",
            "\t/**\n\t * 允许作为 JavaBean 使用的默认构造器。\n\t * @see #setDataSource\n\t * @see #setIncrementerName\n\t * @see #setColumnName\n\t */",
        ),
        (
            "\t/**\n\t * Convenience constructor.\n\t * @param dataSource the DataSource to use\n\t * @param incrementerName the name of the sequence table to use\n\t * @param columnName the name of the column in the sequence table to use\n\t */",
            "\t/**\n\t * 便捷构造器。\n\t * @param dataSource 要使用的 DataSource\n\t * @param incrementerName 要使用的序列表名\n\t * @param columnName 序列表中要使用的列名\n\t */",
        ),
    ],
    "OracleSequenceMaxValueIncrementer.java": [
        (
            "/**\n * {@link DataFieldMaxValueIncrementer} that retrieves the next value\n * of a given Oracle sequence.\n *\n * @author Dmitriy Kopylenko\n * @author Thomas Risberg\n * @author Juergen Hoeller\n */",
            "/**\n * 检索给定 Oracle 序列下一个值的 {@link DataFieldMaxValueIncrementer}。\n *\n * @author Dmitriy Kopylenko\n * @author Thomas Risberg\n * @author Juergen Hoeller\n */",
        ),
        (
            "\t/**\n\t * Default constructor for bean property style usage.\n\t * @see #setDataSource\n\t * @see #setIncrementerName\n\t */",
            "\t/**\n\t * 允许作为 JavaBean 使用的默认构造器。\n\t * @see #setDataSource\n\t * @see #setIncrementerName\n\t */",
        ),
        (
            "\t/**\n\t * Convenience constructor.\n\t * @param dataSource the DataSource to use\n\t * @param incrementerName the name of the sequence/table to use\n\t */",
            "\t/**\n\t * 便捷构造器。\n\t * @param dataSource 要使用的 DataSource\n\t * @param incrementerName 要使用的序列/表名\n\t */",
        ),
    ],
    "PostgresSequenceMaxValueIncrementer.java": [
        (
            "/**\n * {@link DataFieldMaxValueIncrementer} that retrieves the next value\n * of a given PostgreSQL sequence.\n *\n * <p>Thanks to Tomislav Urban for the suggestion!\n *\n * @author Juergen Hoeller\n * @since 4.3.15\n */",
            "/**\n * 检索给定 PostgreSQL 序列下一个值的 {@link DataFieldMaxValueIncrementer}。\n *\n * <p>感谢 Tomislav Urban 的建议！\n *\n * @author Juergen Hoeller\n * @since 4.3.15\n */",
        ),
        (
            "\t/**\n\t * Default constructor for bean property style usage.\n\t * @see #setDataSource\n\t * @see #setIncrementerName\n\t */",
            "\t/**\n\t * 允许作为 JavaBean 使用的默认构造器。\n\t * @see #setDataSource\n\t * @see #setIncrementerName\n\t */",
        ),
        (
            "\t/**\n\t * Convenience constructor.\n\t * @param dataSource the DataSource to use\n\t * @param incrementerName the name of the sequence/table to use\n\t */",
            "\t/**\n\t * 便捷构造器。\n\t * @param dataSource 要使用的 DataSource\n\t * @param incrementerName 要使用的序列/表名\n\t */",
        ),
    ],
    "SqlServerSequenceMaxValueIncrementer.java": [
        (
            "/**\n * {@link DataFieldMaxValueIncrementer} that retrieves the next value of a given\n * SQL Server sequence.\n *\n * @author Mahmoud Ben Hassine\n * @since 6.0\n */",
            "/**\n * 检索给定 SQL Server 序列下一个值的 {@link DataFieldMaxValueIncrementer}。\n *\n * @author Mahmoud Ben Hassine\n * @since 6.0\n */",
        ),
        (
            "\t/**\n\t * Default constructor for bean property style usage.\n\t * @see #setDataSource\n\t * @see #setIncrementerName\n\t */",
            "\t/**\n\t * 允许作为 JavaBean 使用的默认构造器。\n\t * @see #setDataSource\n\t * @see #setIncrementerName\n\t */",
        ),
        (
            "\t/**\n\t * Convenience constructor.\n\t * @param dataSource the DataSource to use\n\t * @param incrementerName the name of the sequence to use\n\t */",
            "\t/**\n\t * 便捷构造器。\n\t * @param dataSource 要使用的 DataSource\n\t * @param incrementerName 要使用的序列名\n\t */",
        ),
    ],
    "SqliteMaxValueIncrementer.java": [
        (
            "/**\n * {@link DataFieldMaxValueIncrementer} that increments the maximum value of a given table with\n * the equivalent of an auto-increment column, using an SQLite {@code select max(rowid)} query.\n *\n * @author Luke Taylor\n * @author Juergen Hoeller\n * @since 7.0\n */",
            "/**\n * 使用 SQLite {@code select max(rowid)} 查询，\n * 递增给定表中相当于自增列的最大值的 {@link DataFieldMaxValueIncrementer}。\n *\n * @author Luke Taylor\n * @author Juergen Hoeller\n * @since 7.0\n */",
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
