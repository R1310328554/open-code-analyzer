"""Chinese JavaDoc replacements for springframework wave24a spring-jdbc exceptions."""

EXCEPTIONS_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "JdbcUpdateAffectedIncorrectNumberOfRowsException.java": [
        (
            "/**\n * Exception thrown when a JDBC update affects an unexpected number of rows.\n * Typically, we expect an update to affect a single row, meaning it is an\n * error if it affects multiple rows.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n */",
            "/**\n * 当 JDBC 更新影响意外行数时抛出的异常。\n * 通常期望更新只影响一行；若影响多行则视为错误。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n */",
        ),
        (
            "\t/** Number of rows that should have been affected. */",
            "\t/** 本应受影响的行数。 */",
        ),
        (
            "\t/** Number of rows that actually were affected. */",
            "\t/** 实际受影响的行数。 */",
        ),
        (
            "\t/**\n\t * Constructor for JdbcUpdateAffectedIncorrectNumberOfRowsException.\n\t * @param sql the SQL we were trying to execute\n\t * @param expected the expected number of rows affected\n\t * @param actual the actual number of rows affected\n\t */",
            "\t/**\n\t * JdbcUpdateAffectedIncorrectNumberOfRowsException 构造器。\n\t * @param sql 尝试执行的 SQL\n\t * @param expected 期望受影响行数\n\t * @param actual 实际受影响行数\n\t */",
        ),
        (
            "\t/**\n\t * Return the number of rows that should have been affected.\n\t */",
            "\t/**\n\t * 返回本应受影响的行数。\n\t */",
        ),
        (
            "\t/**\n\t * Return the number of rows that have actually been affected.\n\t */",
            "\t/**\n\t * 返回实际受影响的行数。\n\t */",
        ),
    ],
    "LobRetrievalFailureException.java": [
        (
            "/**\n * Exception to be thrown when a LOB could not be retrieved.\n *\n * @author Juergen Hoeller\n * @since 1.0.2\n * @deprecated as of 6.2 along with {@link org.springframework.jdbc.support.lob.LobHandler}\n */",
            "/**\n * 无法检索 LOB 时抛出的异常。\n *\n * @author Juergen Hoeller\n * @since 1.0.2\n * @deprecated 自 6.2 起随 {@link org.springframework.jdbc.support.lob.LobHandler} 一并弃用\n */",
        ),
        (
            "\t/**\n\t * Constructor for LobRetrievalFailureException.\n\t * @param msg the detail message\n\t */",
            "\t/**\n\t * LobRetrievalFailureException 构造器。\n\t * @param msg 详细消息\n\t */",
        ),
        (
            "\t/**\n\t * Constructor for LobRetrievalFailureException.\n\t * @param msg the detail message\n\t * @param ex the root cause IOException\n\t */",
            "\t/**\n\t * LobRetrievalFailureException 构造器。\n\t * @param msg 详细消息\n\t * @param ex 根因 IOException\n\t */",
        ),
    ],
    "SQLWarningException.java": [
        (
            "/**\n * Exception thrown when we're not ignoring {@link java.sql.SQLWarning SQLWarnings}.\n *\n * <p>If an SQLWarning is reported, the operation completed, so we will need\n * to explicitly roll it back if we're not happy when looking at the warning.\n * We might choose to ignore (and log) the warning, or to wrap and throw it\n * in the shape of this SQLWarningException instead.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @see org.springframework.jdbc.core.JdbcTemplate#setIgnoreWarnings\n */",
            "/**\n * 未忽略 {@link java.sql.SQLWarning SQLWarning} 时抛出的异常。\n *\n * <p>若报告 SQLWarning，操作已完成；若对警告不满意，\n * 需显式回滚。可选择忽略（并记录）警告，\n * 或将其包装为本 SQLWarningException 抛出。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @see org.springframework.jdbc.core.JdbcTemplate#setIgnoreWarnings\n */",
        ),
        (
            "\t/**\n\t * Constructor for SQLWarningException.\n\t * @param msg the detail message\n\t * @param ex the JDBC warning\n\t */",
            "\t/**\n\t * SQLWarningException 构造器。\n\t * @param msg 详细消息\n\t * @param ex JDBC 警告\n\t */",
        ),
        (
            "\t/**\n\t * Return the underlying {@link SQLWarning}.\n\t * @since 5.3.29\n\t */",
            "\t/**\n\t * 返回底层 {@link SQLWarning}。\n\t * @since 5.3.29\n\t */",
        ),
        (
            "\t/**\n\t * Return the underlying {@link SQLWarning}.\n\t * @deprecated as of 5.3.29, in favor of {@link #getSQLWarning()}\n\t */",
            "\t/**\n\t * 返回底层 {@link SQLWarning}。\n\t * @deprecated 自 5.3.29 起，请改用 {@link #getSQLWarning()}\n\t */",
        ),
    ],
    "UncategorizedSQLException.java": [
        (
            "/**\n * Exception thrown when we can't classify an SQLException into\n * one of our generic data access exceptions.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n */",
            "/**\n * 无法将 SQLException 归类为通用数据访问异常时抛出。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n */",
        ),
        (
            "\t/** SQL that led to the problem. */",
            "\t/** 导致问题的 SQL。 */",
        ),
        (
            "\t/**\n\t * Constructor for UncategorizedSQLException.\n\t * @param task name of current task\n\t * @param sql the offending SQL statement\n\t * @param ex the root cause\n\t */",
            "\t/**\n\t * UncategorizedSQLException 构造器。\n\t * @param task 当前任务名称\n\t * @param sql 有问题的 SQL 语句\n\t * @param ex 根因\n\t */",
        ),
        (
            "\t/**\n\t * Return the underlying SQLException.\n\t */",
            "\t/**\n\t * 返回底层 SQLException。\n\t */",
        ),
        (
            "\t/**\n\t * Return the SQL that led to the problem (if known).\n\t */",
            "\t/**\n\t * 返回导致问题的 SQL（若已知）。\n\t */",
        ),
    ],
}
