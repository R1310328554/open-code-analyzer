"""Wave 16b [20:40] Chinese JavaDoc replacements — transaction exception classes."""

TX_EXCEPTIONS_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "TransactionSuspensionNotSupportedException.java": [
        (
            "/**\n * Exception thrown when attempting to suspend an existing transaction\n * but transaction suspension is not supported by the underlying backend.\n *\n * @author Juergen Hoeller\n * @since 1.1\n */",
            "/**\n * 尝试挂起现有事务但底层后端不支持事务挂起时抛出的异常。\n *\n * @author Juergen Hoeller\n * @since 1.1\n */",
        ),
        (
            "\t/**\n\t * Constructor for TransactionSuspensionNotSupportedException.\n\t * @param msg the detail message\n\t */",
            "\t/**\n\t * TransactionSuspensionNotSupportedException 的构造方法。\n\t * @param msg 详细消息\n\t */",
        ),
        (
            "\t/**\n\t * Constructor for TransactionSuspensionNotSupportedException.\n\t * @param msg the detail message\n\t * @param cause the root cause from the transaction API in use\n\t */",
            "\t/**\n\t * TransactionSuspensionNotSupportedException 的构造方法。\n\t * @param msg 详细消息\n\t * @param cause 所用事务 API 的根因\n\t */",
        ),
    ],
    "TransactionSystemException.java": [
        (
            "/**\n * Exception thrown when a general transaction system error is encountered,\n * like on commit or rollback.\n *\n * @author Juergen Hoeller\n * @since 24.03.2003\n */",
            "/**\n * 遇到一般性事务系统错误（例如提交或回滚时）时抛出的异常。\n *\n * @author Juergen Hoeller\n * @since 24.03.2003\n */",
        ),
        (
            "\t/**\n\t * Constructor for TransactionSystemException.\n\t * @param msg the detail message\n\t */",
            "\t/**\n\t * TransactionSystemException 的构造方法。\n\t * @param msg 详细消息\n\t */",
        ),
        (
            "\t/**\n\t * Constructor for TransactionSystemException.\n\t * @param msg the detail message\n\t * @param cause the root cause from the transaction API in use\n\t */",
            "\t/**\n\t * TransactionSystemException 的构造方法。\n\t * @param msg 详细消息\n\t * @param cause 所用事务 API 的根因\n\t */",
        ),
        (
            "\t/**\n\t * Set an application exception that was thrown before this transaction exception,\n\t * preserving the original exception despite the overriding TransactionSystemException.\n\t * @param ex the application exception\n\t * @throws IllegalStateException if this TransactionSystemException already holds an\n\t * application exception\n\t */",
            "\t/**\n\t * 设置在此事务异常之前抛出的应用异常，\n\t * 尽管覆盖了 TransactionSystemException，仍保留原始异常。\n\t * @param ex 应用异常\n\t * @throws IllegalStateException 若此 TransactionSystemException 已持有应用异常\n\t */",
        ),
        (
            "\t/**\n\t * Return the application exception that was thrown before this transaction exception,\n\t * if any.\n\t * @return the application exception, or {@code null} if none set\n\t */",
            "\t/**\n\t * 返回在此事务异常之前抛出的应用异常（若有）。\n\t * @return 应用异常，未设置则返回 {@code null}\n\t */",
        ),
        (
            "\t/**\n\t * Return the exception that was the first to be thrown within the failed transaction:\n\t * i.e. the application exception, if any, or the TransactionSystemException's own cause.\n\t * @return the original exception, or {@code null} if there was none\n\t */",
            "\t/**\n\t * 返回失败事务中首先抛出的异常：\n\t * 即应用异常（若有），否则为 TransactionSystemException 自身的 cause。\n\t * @return 原始异常，若无则返回 {@code null}\n\t */",
        ),
    ],
    "TransactionTimedOutException.java": [
        (
            "/**\n * Exception to be thrown when a transaction has timed out.\n *\n * <p>Thrown by Spring's local transaction strategies if the deadline\n * for a transaction has been reached when an operation is attempted,\n * according to the timeout specified for the given transaction.\n *\n * <p>Beyond such checks before each transactional operation, Spring's\n * local transaction strategies will also pass appropriate timeout values\n * to resource operations (for example to JDBC Statements, letting the JDBC\n * driver respect the timeout). Such operations will usually throw native\n * resource exceptions (for example, JDBC SQLExceptions) if their operation\n * timeout has been exceeded, to be converted to Spring's DataAccessException\n * in the respective DAO (which might use Spring's JdbcTemplate, for example).\n *\n * <p>In a JTA environment, it is up to the JTA transaction coordinator\n * to apply transaction timeouts. Usually, the corresponding JTA-aware\n * connection pool will perform timeout checks and throw corresponding\n * native resource exceptions (for example, JDBC SQLExceptions).\n *\n * @author Juergen Hoeller\n * @since 1.1.5\n * @see org.springframework.transaction.support.ResourceHolderSupport#getTimeToLiveInMillis\n * @see java.sql.Statement#setQueryTimeout\n * @see java.sql.SQLException\n */",
            "/**\n * 当事务超时时抛出的异常。\n *\n * <p>若在给定事务指定的超时时间内尝试操作时已达截止时间，\n * Spring 本地事务策略会抛出此异常。\n *\n * <p>除每次事务操作前的此类检查外，Spring 本地事务策略\n * 还会将适当的超时值传递给资源操作（例如 JDBC Statement，\n * 让 JDBC 驱动遵守超时）。若操作超时，此类操作通常会抛出\n * 原生资源异常（例如 JDBC SQLException），\n * 在相应 DAO 中转换为 Spring 的 DataAccessException\n *（例如使用 Spring JdbcTemplate 时）。\n *\n * <p>在 JTA 环境中，由 JTA 事务协调器应用事务超时。\n * 通常相应的 JTA 感知连接池会执行超时检查并抛出\n * 对应的原生资源异常（例如 JDBC SQLException）。\n *\n * @author Juergen Hoeller\n * @since 1.1.5\n * @see org.springframework.transaction.support.ResourceHolderSupport#getTimeToLiveInMillis\n * @see java.sql.Statement#setQueryTimeout\n * @see java.sql.SQLException\n */",
        ),
        (
            "\t/**\n\t * Constructor for TransactionTimedOutException.\n\t * @param msg the detail message\n\t */",
            "\t/**\n\t * TransactionTimedOutException 的构造方法。\n\t * @param msg 详细消息\n\t */",
        ),
        (
            "\t/**\n\t * Constructor for TransactionTimedOutException.\n\t * @param msg the detail message\n\t * @param cause the root cause from the transaction API in use\n\t */",
            "\t/**\n\t * TransactionTimedOutException 的构造方法。\n\t * @param msg 详细消息\n\t * @param cause 所用事务 API 的根因\n\t */",
        ),
    ],
    "TransactionUsageException.java": [
        (
            "/**\n * Superclass for exceptions caused by inappropriate usage of\n * a Spring transaction API.\n *\n * @author Rod Johnson\n * @since 22.03.2003\n */",
            "/**\n * 因不当使用 Spring 事务 API 而引发的异常的父类。\n *\n * @author Rod Johnson\n * @since 22.03.2003\n */",
        ),
        (
            "\t/**\n\t * Constructor for TransactionUsageException.\n\t * @param msg the detail message\n\t */",
            "\t/**\n\t * TransactionUsageException 的构造方法。\n\t * @param msg 详细消息\n\t */",
        ),
        (
            "\t/**\n\t * Constructor for TransactionUsageException.\n\t * @param msg the detail message\n\t * @param cause the root cause from the transaction API in use\n\t */",
            "\t/**\n\t * TransactionUsageException 的构造方法。\n\t * @param msg 详细消息\n\t * @param cause 所用事务 API 的根因\n\t */",
        ),
    ],
    "UnexpectedRollbackException.java": [
        (
            "/**\n * Thrown when an attempt to commit a transaction resulted\n * in an unexpected rollback.\n *\n * @author Rod Johnson\n * @since 17.03.2003\n */",
            "/**\n * 尝试提交事务却导致意外回滚时抛出。\n *\n * @author Rod Johnson\n * @since 17.03.2003\n */",
        ),
        (
            "\t/**\n\t * Constructor for UnexpectedRollbackException.\n\t * @param msg the detail message\n\t */",
            "\t/**\n\t * UnexpectedRollbackException 的构造方法。\n\t * @param msg 详细消息\n\t */",
        ),
        (
            "\t/**\n\t * Constructor for UnexpectedRollbackException.\n\t * @param msg the detail message\n\t * @param cause the root cause from the transaction API in use\n\t */",
            "\t/**\n\t * UnexpectedRollbackException 的构造方法。\n\t * @param msg 详细消息\n\t * @param cause 所用事务 API 的根因\n\t */",
        ),
    ],
}
