"""Wave 15b [20:40] Chinese JavaDoc replacements — dao exception classes."""

EXCEPTIONS_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "PermissionDeniedDataAccessException.java": [
        (
            "/**\n * Exception thrown when the underlying resource denied a permission\n * to access a specific element, such as a specific database table.\n *\n * @author Juergen Hoeller\n * @since 2.0\n */",
            "/**\n * 当底层资源拒绝访问特定元素（例如特定数据库表）的权限时抛出的异常。\n *\n * @author Juergen Hoeller\n * @since 2.0\n */",
        ),
        (
            "\t/**\n\t * Constructor for PermissionDeniedDataAccessException.\n\t * @param msg the detail message\n\t * @param cause the root cause from the underlying data access API,\n\t * such as JDBC\n\t */",
            "\t/**\n\t * PermissionDeniedDataAccessException 的构造方法。\n\t * @param msg 详细消息\n\t * @param cause 来自底层数据访问 API（如 JDBC）的根因\n\t */",
        ),
    ],
    "PessimisticLockingFailureException.java": [
        (
            "/**\n * Exception thrown on a pessimistic locking violation.\n * Thrown by Spring's SQLException translation mechanism\n * if a corresponding database error is encountered.\n *\n * <p>Serves as a superclass for more specific exceptions, for example,\n * {@link CannotAcquireLockException}. However, it is generally\n * recommended to handle {@code PessimisticLockingFailureException}\n * itself instead of relying on specific exception subclasses.\n *\n * @author Thomas Risberg\n * @since 1.2\n * @see OptimisticLockingFailureException\n */",
            "/**\n * 发生悲观锁冲突时抛出的异常。\n * 若遇到对应的数据库错误，由 Spring 的 SQLException 转换机制抛出。\n *\n * <p>作为更具体异常（例如 {@link CannotAcquireLockException}）的父类。\n * 但通常建议直接处理 {@code PessimisticLockingFailureException}，\n * 而非依赖特定异常子类。\n *\n * @author Thomas Risberg\n * @since 1.2\n * @see OptimisticLockingFailureException\n */",
        ),
        (
            "\t/**\n\t * Constructor for PessimisticLockingFailureException.\n\t * @param msg the detail message\n\t */",
            "\t/**\n\t * PessimisticLockingFailureException 的构造方法。\n\t * @param msg 详细消息\n\t */",
        ),
        (
            "\t/**\n\t * Constructor for PessimisticLockingFailureException.\n\t * @param msg the detail message\n\t * @param cause the root cause from the data access API in use\n\t */",
            "\t/**\n\t * PessimisticLockingFailureException 的构造方法。\n\t * @param msg 详细消息\n\t * @param cause 所用数据访问 API 的根因\n\t */",
        ),
    ],
    "QueryTimeoutException.java": [
        (
            "/**\n * Exception to be thrown on a query timeout. This could have different causes depending on\n * the database API in use but most likely thrown after the database interrupts or stops\n * the processing of a query before it has completed.\n *\n * <p>This exception can be thrown by user code trapping the native database exception or\n * by exception translation.\n *\n * @author Thomas Risberg\n * @since 3.1\n */",
            "/**\n * 查询超时时抛出的异常。具体原因取决于所用数据库 API，\n * 最可能是在数据库中断或停止尚未完成的查询处理时抛出。\n *\n * <p>本异常可由捕获原生数据库异常的用户代码抛出，\n * 也可由异常转换机制抛出。\n *\n * @author Thomas Risberg\n * @since 3.1\n */",
        ),
        (
            "\t/**\n\t * Constructor for QueryTimeoutException.\n\t * @param msg the detail message\n\t */",
            "\t/**\n\t * QueryTimeoutException 的构造方法。\n\t * @param msg 详细消息\n\t */",
        ),
        (
            "\t/**\n\t * Constructor for QueryTimeoutException.\n\t * @param msg the detail message\n\t * @param cause the root cause from the data access API in use\n\t */",
            "\t/**\n\t * QueryTimeoutException 的构造方法。\n\t * @param msg 详细消息\n\t * @param cause 所用数据访问 API 的根因\n\t */",
        ),
    ],
    "RecoverableDataAccessException.java": [
        (
            "/**\n * Data access exception thrown when a previously failed operation might be able\n * to succeed if the application performs some recovery steps and retries the entire\n * transaction or in the case of a distributed transaction, the transaction branch.\n * At a minimum, the recovery operation must include closing the current connection\n * and getting a new connection.\n *\n * @author Thomas Risberg\n * @since 2.5\n * @see java.sql.SQLRecoverableException\n */",
            "/**\n * 当先前失败的操作在应用执行某些恢复步骤并重试整个事务\n *（分布式事务情况下为事务分支）后可能成功时抛出的数据访问异常。\n * 恢复操作至少须包括关闭当前连接并获取新连接。\n *\n * @author Thomas Risberg\n * @since 2.5\n * @see java.sql.SQLRecoverableException\n */",
        ),
        (
            "\t/**\n\t * Constructor for RecoverableDataAccessException.\n\t * @param msg the detail message\n\t */",
            "\t/**\n\t * RecoverableDataAccessException 的构造方法。\n\t * @param msg 详细消息\n\t */",
        ),
        (
            "\t/**\n\t * Constructor for RecoverableDataAccessException.\n\t * @param msg the detail message\n\t * @param cause the root cause (usually from using an underlying\n\t * data access API such as JDBC)\n\t */",
            "\t/**\n\t * RecoverableDataAccessException 的构造方法。\n\t * @param msg 详细消息\n\t * @param cause 根因（通常来自底层数据访问 API，如 JDBC）\n\t */",
        ),
    ],
    "TransientDataAccessException.java": [
        (
            "/**\n * Root of the hierarchy of data access exceptions that are considered transient -\n * where a previously failed operation might be able to succeed when the operation\n * is retried without any intervention by application-level functionality.\n *\n * @author Thomas Risberg\n * @since 2.5\n * @see java.sql.SQLTransientException\n */",
            "/**\n * 被视为瞬态的数据访问异常层次结构的根类——\n * 先前失败的操作在无需应用层干预、仅重试操作时可能成功。\n *\n * @author Thomas Risberg\n * @since 2.5\n * @see java.sql.SQLTransientException\n */",
        ),
        (
            "\t/**\n\t * Constructor for TransientDataAccessException.\n\t * @param msg the detail message\n\t */",
            "\t/**\n\t * TransientDataAccessException 的构造方法。\n\t * @param msg 详细消息\n\t */",
        ),
        (
            "\t/**\n\t * Constructor for TransientDataAccessException.\n\t * @param msg the detail message\n\t * @param cause the root cause (usually from using an underlying\n\t * data access API such as JDBC)\n\t */",
            "\t/**\n\t * TransientDataAccessException 的构造方法。\n\t * @param msg 详细消息\n\t * @param cause 根因（通常来自底层数据访问 API，如 JDBC）\n\t */",
        ),
    ],
    "TransientDataAccessResourceException.java": [
        (
            "/**\n * Data access exception thrown when a resource fails temporarily\n * and the operation can be retried.\n *\n * @author Thomas Risberg\n * @since 2.5\n * @see java.sql.SQLTransientConnectionException\n */",
            "/**\n * 当资源暂时失败且操作可重试时抛出的数据访问异常。\n *\n * @author Thomas Risberg\n * @since 2.5\n * @see java.sql.SQLTransientConnectionException\n */",
        ),
        (
            "\t/**\n\t * Constructor for TransientDataAccessResourceException.\n\t * @param msg the detail message\n\t */",
            "\t/**\n\t * TransientDataAccessResourceException 的构造方法。\n\t * @param msg 详细消息\n\t */",
        ),
        (
            "\t/**\n\t * Constructor for TransientDataAccessResourceException.\n\t * @param msg the detail message\n\t * @param cause the root cause from the data access API in use\n\t */",
            "\t/**\n\t * TransientDataAccessResourceException 的构造方法。\n\t * @param msg 详细消息\n\t * @param cause 所用数据访问 API 的根因\n\t */",
        ),
    ],
    "TypeMismatchDataAccessException.java": [
        (
            "/**\n * Exception thrown on mismatch between Java type and database type:\n * for example on an attempt to set an object of the wrong type\n * in an RDBMS column.\n *\n * @author Rod Johnson\n */",
            "/**\n * Java 类型与数据库类型不匹配时抛出的异常：\n * 例如尝试在 RDBMS 列中设置错误类型的对象。\n *\n * @author Rod Johnson\n */",
        ),
        (
            "\t/**\n\t * Constructor for TypeMismatchDataAccessException.\n\t * @param msg the detail message\n\t */",
            "\t/**\n\t * TypeMismatchDataAccessException 的构造方法。\n\t * @param msg 详细消息\n\t */",
        ),
        (
            "\t/**\n\t * Constructor for TypeMismatchDataAccessException.\n\t * @param msg the detail message\n\t * @param cause the root cause from the data access API in use\n\t */",
            "\t/**\n\t * TypeMismatchDataAccessException 的构造方法。\n\t * @param msg 详细消息\n\t * @param cause 所用数据访问 API 的根因\n\t */",
        ),
    ],
    "UncategorizedDataAccessException.java": [
        (
            "/**\n * Normal superclass when we can't distinguish anything more specific\n * than \"something went wrong with the underlying resource\": for example,\n * an SQLException from JDBC we can't pinpoint more precisely.\n *\n * @author Rod Johnson\n */",
            "/**\n * 当无法区分比“底层资源出现问题”更具体的情况时的常规父类：\n * 例如无法更精确定位的 JDBC SQLException。\n *\n * @author Rod Johnson\n */",
        ),
        (
            "\t/**\n\t * Constructor for UncategorizedDataAccessException.\n\t * @param msg the detail message\n\t * @param cause the exception thrown by underlying data access API\n\t */",
            "\t/**\n\t * UncategorizedDataAccessException 的构造方法。\n\t * @param msg 详细消息\n\t * @param cause 底层数据访问 API 抛出的异常\n\t */",
        ),
    ],
}
