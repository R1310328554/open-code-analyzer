"""Chinese JavaDoc replacements for springframework wave23b spring-jdbc exceptions."""

JDBC_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "BadSqlGrammarException.java": [
        (
            "/**\n * Exception thrown when SQL specified is invalid. Such exceptions always have\n * a {@code java.sql.SQLException} root cause.\n *\n * <p>It would be possible to have subclasses for no such table, no such column etc.\n * A custom SQLExceptionTranslator could create such more specific exceptions,\n * without affecting code using this class.\n *\n * @author Rod Johnson\n * @see InvalidResultSetAccessException\n */",
            "/**\n * 当指定 SQL 无效时抛出的异常。此类异常始终以\n * {@code java.sql.SQLException} 为根因。\n *\n * <p>可为「无此表」「无此列」等定义子类。\n * 自定义 SQLExceptionTranslator 可创建更具体的异常，\n * 而不影响使用本类的代码。\n *\n * @author Rod Johnson\n * @see InvalidResultSetAccessException\n */",
        ),
        (
            "\t/**\n\t * Constructor for BadSqlGrammarException.\n\t * @param task name of current task\n\t * @param sql the offending SQL statement\n\t * @param ex the root cause\n\t */",
            "\t/**\n\t * BadSqlGrammarException 构造器。\n\t * @param task 当前任务名称\n\t * @param sql 有问题的 SQL 语句\n\t * @param ex 根因\n\t */",
        ),
        (
            "\t/**\n\t * Return the wrapped SQLException.\n\t */",
            "\t/**\n\t * 返回包装的 SQLException。\n\t */",
        ),
        (
            "\t/**\n\t * Return the SQL that caused the problem.\n\t */",
            "\t/**\n\t * 返回导致问题的 SQL。\n\t */",
        ),
    ],
    "CannotGetJdbcConnectionException.java": [
        (
            "/**\n * Fatal exception thrown when we can't connect to an RDBMS using JDBC.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n */",
            "/**\n * 无法通过 JDBC 连接 RDBMS 时抛出的致命异常。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n */",
        ),
        (
            "\t/**\n\t * Constructor for {@code CannotGetJdbcConnectionException}.\n\t * @param msg the detail message\n\t * @since 5.0\n\t */",
            "\t/**\n\t * {@code CannotGetJdbcConnectionException} 构造器。\n\t * @param msg 详细消息\n\t * @since 5.0\n\t */",
        ),
        (
            "\t/**\n\t * Constructor for {@code CannotGetJdbcConnectionException}.\n\t * @param msg the detail message\n\t * @param ex the root cause SQLException\n\t */",
            "\t/**\n\t * {@code CannotGetJdbcConnectionException} 构造器。\n\t * @param msg 详细消息\n\t * @param ex 根因 SQLException\n\t */",
        ),
        (
            "\t/**\n\t * Constructor for {@code CannotGetJdbcConnectionException}.\n\t * @param msg the detail message\n\t * @param ex the root cause IllegalStateException\n\t * @since 5.3.22\n\t */",
            "\t/**\n\t * {@code CannotGetJdbcConnectionException} 构造器。\n\t * @param msg 详细消息\n\t * @param ex 根因 IllegalStateException\n\t * @since 5.3.22\n\t */",
        ),
    ],
    "IncorrectResultSetColumnCountException.java": [
        (
            "/**\n * Data access exception thrown when a result set did not have the correct column count,\n * for example when expecting a single column but getting 0 or more than 1 column.\n *\n * @author Juergen Hoeller\n * @since 2.0\n * @see org.springframework.dao.IncorrectResultSizeDataAccessException\n */",
            "/**\n * 当结果集列数不正确时抛出的数据访问异常，\n * 例如期望单列却得到 0 列或多于 1 列。\n *\n * @author Juergen Hoeller\n * @since 2.0\n * @see org.springframework.dao.IncorrectResultSizeDataAccessException\n */",
        ),
        (
            "\t/**\n\t * Constructor for IncorrectResultSetColumnCountException.\n\t * @param expectedCount the expected column count\n\t * @param actualCount the actual column count\n\t */",
            "\t/**\n\t * IncorrectResultSetColumnCountException 构造器。\n\t * @param expectedCount 期望列数\n\t * @param actualCount 实际列数\n\t */",
        ),
        (
            "\t/**\n\t * Constructor for IncorrectResultCountDataAccessException.\n\t * @param msg the detail message\n\t * @param expectedCount the expected column count\n\t * @param actualCount the actual column count\n\t */",
            "\t/**\n\t * IncorrectResultCountDataAccessException 构造器。\n\t * @param msg 详细消息\n\t * @param expectedCount 期望列数\n\t * @param actualCount 实际列数\n\t */",
        ),
        (
            "\t/**\n\t * Return the expected column count.\n\t */",
            "\t/**\n\t * 返回期望列数。\n\t */",
        ),
        (
            "\t/**\n\t * Return the actual column count.\n\t */",
            "\t/**\n\t * 返回实际列数。\n\t */",
        ),
    ],
    "InvalidResultSetAccessException.java": [
        (
            "/**\n * Exception thrown when a ResultSet has been accessed in an invalid fashion.\n * Such exceptions always have a {@code java.sql.SQLException} root cause.\n *\n * <p>This typically happens when an invalid ResultSet column index or name\n * has been specified. Also thrown by disconnected SqlRowSets.\n *\n * @author Juergen Hoeller\n * @since 1.2\n * @see BadSqlGrammarException\n * @see org.springframework.jdbc.support.rowset.SqlRowSet\n */",
            "/**\n * 以无效方式访问 ResultSet 时抛出的异常。\n * 此类异常始终以 {@code java.sql.SQLException} 为根因。\n *\n * <p>通常因指定无效的 ResultSet 列索引或列名导致。\n * 断开连接的 SqlRowSet 也会抛出。\n *\n * @author Juergen Hoeller\n * @since 1.2\n * @see BadSqlGrammarException\n * @see org.springframework.jdbc.support.rowset.SqlRowSet\n */",
        ),
        (
            "\t/**\n\t * Constructor for InvalidResultSetAccessException.\n\t * @param task name of current task\n\t * @param sql the offending SQL statement\n\t * @param ex the root cause\n\t */",
            "\t/**\n\t * InvalidResultSetAccessException 构造器。\n\t * @param task 当前任务名称\n\t * @param sql 有问题的 SQL 语句\n\t * @param ex 根因\n\t */",
        ),
        (
            "\t/**\n\t * Constructor for InvalidResultSetAccessException.\n\t * @param ex the root cause\n\t */",
            "\t/**\n\t * InvalidResultSetAccessException 构造器。\n\t * @param ex 根因\n\t */",
        ),
        (
            "\t/**\n\t * Return the wrapped SQLException.\n\t */",
            "\t/**\n\t * 返回包装的 SQLException。\n\t */",
        ),
        (
            "\t/**\n\t * Return the SQL that caused the problem.\n\t * @return the offending SQL, if known\n\t */",
            "\t/**\n\t * 返回导致问题的 SQL。\n\t * @return 有问题的 SQL（若已知）\n\t */",
        ),
    ],
}
