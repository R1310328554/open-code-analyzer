"""Chinese JavaDoc replacements for springframework wave15a dao exceptions [2:20]."""

DAO_EXCEPTIONS_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "DataAccessException.java": [
        (
            "/**\n * Root of the hierarchy of data access exceptions discussed in\n * <a href=\"https://www.amazon.com/exec/obidos/tg/detail/-/0764543857/\">Expert One-On-One J2EE Design and Development</a>.\n * Please see Chapter 9 of this book for detailed discussion of the\n * motivation for this package.\n *\n * <p>This exception hierarchy aims to let user code find and handle the\n * kind of error encountered without knowing the details of the particular\n * data access API in use (for example, JDBC). Thus, it is possible to react to an\n * optimistic locking failure without knowing that JDBC is being used.\n *\n * <p>As this class is a runtime exception, there is no need for user code\n * to catch it or subclasses if any error is to be considered fatal\n * (the usual case).\n *\n * @author Rod Johnson\n */",
            "/**\n * 数据访问异常层次结构的根类，详见\n * <a href=\"https://www.amazon.com/exec/obidos/tg/detail/-/0764543857/\">Expert One-On-One J2EE Design and Development</a>。\n * 该书第 9 章对本包的设计动机有详细论述。\n *\n * <p>本异常层次结构使用户代码无需了解具体数据访问 API（如 JDBC）的细节，\n * 即可识别并处理所遇错误类型。例如，可在不知底层使用 JDBC 的情况下\n * 响应乐观锁失败。\n *\n * <p>本类为运行时异常；若将任何错误视为致命（通常情况），\n * 用户代码无需捕获本类或其子类。\n *\n * @author Rod Johnson\n */",
        ),
        (
            "\t/**\n\t * Constructor for DataAccessException.\n\t * @param msg the detail message\n\t */",
            "\t/**\n\t * DataAccessException 构造函数。\n\t * @param msg 详细消息\n\t */",
        ),
        (
            "\t/**\n\t * Constructor for DataAccessException.\n\t * @param msg the detail message\n\t * @param cause the root cause (usually from using an underlying\n\t * data access API such as JDBC)\n\t */",
            "\t/**\n\t * DataAccessException 构造函数。\n\t * @param msg 详细消息\n\t * @param cause 根因（通常来自底层数据访问 API，如 JDBC）\n\t */",
        ),
    ],
    "ConcurrencyFailureException.java": [
        (
            "/**\n * Exception thrown on various data access concurrency failures.\n *\n * <p>This exception provides subclasses for specific types of failure,\n * in particular optimistic locking versus pessimistic locking.\n *\n * @author Thomas Risberg\n * @since 1.1\n * @see OptimisticLockingFailureException\n * @see PessimisticLockingFailureException\n */",
            "/**\n * 各类数据访问并发失败时抛出的异常。\n *\n * <p>本异常提供针对具体失败类型的子类，\n * 尤其是乐观锁与悲观锁相关失败。\n *\n * @author Thomas Risberg\n * @since 1.1\n * @see OptimisticLockingFailureException\n * @see PessimisticLockingFailureException\n */",
        ),
        (
            "\t/**\n\t * Constructor for ConcurrencyFailureException.\n\t * @param msg the detail message\n\t */",
            "\t/**\n\t * ConcurrencyFailureException 构造函数。\n\t * @param msg 详细消息\n\t */",
        ),
        (
            "\t/**\n\t * Constructor for ConcurrencyFailureException.\n\t * @param msg the detail message\n\t * @param cause the root cause from the data access API in use\n\t */",
            "\t/**\n\t * ConcurrencyFailureException 构造函数。\n\t * @param msg 详细消息\n\t * @param cause 所用数据访问 API 的根因\n\t */",
        ),
    ],
    "CannotAcquireLockException.java": [
        (
            "/**\n * Exception thrown on failure to acquire a lock during an update,\n * for example during a \"select for update\" statement.\n *\n * <p>Consider handling the general {@link PessimisticLockingFailureException}\n * instead, semantically including a wider range of locking-related failures.\n *\n * @author Rod Johnson\n */",
            "/**\n * 更新过程中获取锁失败时抛出，\n * 例如在 \"select for update\" 语句执行期间。\n *\n * <p>建议改为处理通用的 {@link PessimisticLockingFailureException}，\n * 其语义涵盖更广的锁相关失败。\n *\n * @author Rod Johnson\n */",
        ),
        (
            "\t/**\n\t * Constructor for CannotAcquireLockException.\n\t * @param msg the detail message\n\t */",
            "\t/**\n\t * CannotAcquireLockException 构造函数。\n\t * @param msg 详细消息\n\t */",
        ),
        (
            "\t/**\n\t * Constructor for CannotAcquireLockException.\n\t * @param msg the detail message\n\t * @param cause the root cause from the data access API in use\n\t */",
            "\t/**\n\t * CannotAcquireLockException 构造函数。\n\t * @param msg 详细消息\n\t * @param cause 所用数据访问 API 的根因\n\t */",
        ),
    ],
    "CannotSerializeTransactionException.java": [
        (
            "/**\n * Exception thrown on failure to complete a transaction in serialized mode\n * due to update conflicts.\n *\n * <p>Consider handling the general {@link PessimisticLockingFailureException}\n * instead, semantically including a wider range of locking-related failures.\n *\n * @author Rod Johnson\n * @deprecated as of 6.0.3, in favor of\n * {@link PessimisticLockingFailureException}/{@link CannotAcquireLockException}\n */",
            "/**\n * 因更新冲突导致无法在串行化模式下完成事务时抛出。\n *\n * <p>建议改为处理通用的 {@link PessimisticLockingFailureException}，\n * 其语义涵盖更广的锁相关失败。\n *\n * @author Rod Johnson\n * @deprecated as of 6.0.3, in favor of\n * {@link PessimisticLockingFailureException}/{@link CannotAcquireLockException}\n */",
        ),
        (
            "\t/**\n\t * Constructor for CannotSerializeTransactionException.\n\t * @param msg the detail message\n\t */",
            "\t/**\n\t * CannotSerializeTransactionException 构造函数。\n\t * @param msg 详细消息\n\t */",
        ),
        (
            "\t/**\n\t * Constructor for CannotSerializeTransactionException.\n\t * @param msg the detail message\n\t * @param cause the root cause from the data access API in use\n\t */",
            "\t/**\n\t * CannotSerializeTransactionException 构造函数。\n\t * @param msg 详细消息\n\t * @param cause 所用数据访问 API 的根因\n\t */",
        ),
    ],
    "CleanupFailureDataAccessException.java": [
        (
            "/**\n * Exception thrown when we couldn't clean up after a data access operation,\n * but the actual operation went OK.\n *\n * <p>For example, this exception or a subclass might be thrown if a JDBC\n * Connection couldn't be closed after it had been used successfully.\n *\n * <p>Note that data access code might perform resources cleanup in a\n * {@code finally} block and therefore log cleanup failure rather than rethrow it,\n * to keep the original data access exception, if any.\n *\n * @author Rod Johnson\n * @deprecated as of 6.0.3 since it is not in use within core JDBC/ORM support\n */",
            "/**\n * 数据访问操作本身成功，但后续清理失败时抛出。\n *\n * <p>例如，JDBC Connection 使用成功后无法关闭时，\n * 可能抛出本异常或其子类。\n *\n * <p>注意：数据访问代码可能在 {@code finally} 块中执行资源清理，\n * 因此可能记录清理失败而非重新抛出，以保留原始数据访问异常（若有）。\n *\n * @author Rod Johnson\n * @deprecated as of 6.0.3 since it is not in use within core JDBC/ORM support\n */",
        ),
        (
            "\t/**\n\t * Constructor for CleanupFailureDataAccessException.\n\t * @param msg the detail message\n\t * @param cause the root cause from the underlying data access API,\n\t * such as JDBC\n\t */",
            "\t/**\n\t * CleanupFailureDataAccessException 构造函数。\n\t * @param msg 详细消息\n\t * @param cause 底层数据访问 API（如 JDBC）的根因\n\t */",
        ),
    ],
    "DataAccessResourceFailureException.java": [
        (
            "/**\n * Data access exception thrown when a resource fails completely:\n * for example, if we can't connect to a database using JDBC.\n *\n * @author Rod Johnson\n * @author Thomas Risberg\n */",
            "/**\n * 资源完全不可用时抛出的数据访问异常，\n * 例如无法通过 JDBC 连接数据库。\n *\n * @author Rod Johnson\n * @author Thomas Risberg\n */",
        ),
        (
            "\t/**\n\t * Constructor for DataAccessResourceFailureException.\n\t * @param msg the detail message\n\t */",
            "\t/**\n\t * DataAccessResourceFailureException 构造函数。\n\t * @param msg 详细消息\n\t */",
        ),
        (
            "\t/**\n\t * Constructor for DataAccessResourceFailureException.\n\t * @param msg the detail message\n\t * @param cause the root cause from the data access API in use\n\t */",
            "\t/**\n\t * DataAccessResourceFailureException 构造函数。\n\t * @param msg 详细消息\n\t * @param cause 所用数据访问 API 的根因\n\t */",
        ),
    ],
    "DataIntegrityViolationException.java": [
        (
            "/**\n * Exception thrown when an attempt to execute an SQL statement fails to map\n * the given data, typically but no limited to an insert or update data\n * results in violation of an integrity constraint. Note that this\n * is not purely a relational concept; integrity constraints such\n * as unique primary keys are required by most database types.\n *\n * <p>Serves as a superclass for more specific exceptions, for example,\n * {@link DuplicateKeyException}. However, it is generally\n * recommended to handle {@code DataIntegrityViolationException}\n * itself instead of relying on specific exception subclasses.\n *\n * @author Rod Johnson\n */",
            "/**\n * 执行 SQL 语句时无法正确映射给定数据而抛出，\n * 通常（但不限于）指 insert 或 update 违反完整性约束。\n * 注意：这并非纯粹的关系型概念；\n * 唯一主键等完整性约束为大多数数据库类型所要求。\n *\n * <p>作为更具体异常（如 {@link DuplicateKeyException}）的超类。\n * 但通常建议直接处理 {@code DataIntegrityViolationException}，\n * 而非依赖特定异常子类。\n *\n * @author Rod Johnson\n */",
        ),
        (
            "\t/**\n\t * Constructor for DataIntegrityViolationException.\n\t * @param msg the detail message\n\t */",
            "\t/**\n\t * DataIntegrityViolationException 构造函数。\n\t * @param msg 详细消息\n\t */",
        ),
        (
            "\t/**\n\t * Constructor for DataIntegrityViolationException.\n\t * @param msg the detail message\n\t * @param cause the root cause from the data access API in use\n\t */",
            "\t/**\n\t * DataIntegrityViolationException 构造函数。\n\t * @param msg 详细消息\n\t * @param cause 所用数据访问 API 的根因\n\t */",
        ),
    ],
    "DataRetrievalFailureException.java": [
        (
            "/**\n * Exception thrown if certain expected data could not be retrieved, for example,\n * when looking up specific data via a known identifier. This exception\n * will be thrown either by O/R mapping tools or by DAO implementations.\n *\n * @author Juergen Hoeller\n * @since 13.10.2003\n */",
            "/**\n * 无法检索到预期数据时抛出，例如通过已知标识符查找特定数据失败。\n * 本异常由 O/R 映射工具或 DAO 实现抛出。\n *\n * @author Juergen Hoeller\n * @since 13.10.2003\n */",
        ),
        (
            "\t/**\n\t * Constructor for DataRetrievalFailureException.\n\t * @param msg the detail message\n\t */",
            "\t/**\n\t * DataRetrievalFailureException 构造函数。\n\t * @param msg 详细消息\n\t */",
        ),
        (
            "\t/**\n\t * Constructor for DataRetrievalFailureException.\n\t * @param msg the detail message\n\t * @param cause the root cause from the data access API in use\n\t */",
            "\t/**\n\t * DataRetrievalFailureException 构造函数。\n\t * @param msg 详细消息\n\t * @param cause 所用数据访问 API 的根因\n\t */",
        ),
    ],
    "DeadlockLoserDataAccessException.java": [
        (
            "/**\n * Generic exception thrown when the current process was\n * a deadlock loser, and its transaction rolled back.\n *\n * <p>Consider handling the general {@link PessimisticLockingFailureException}\n * instead, semantically including a wider range of locking-related failures.\n *\n * @author Rod Johnson\n * @deprecated as of 6.0.3, in favor of\n * {@link PessimisticLockingFailureException}/{@link CannotAcquireLockException}\n */",
            "/**\n * 当前进程成为死锁牺牲品且事务已回滚时抛出的通用异常。\n *\n * <p>建议改为处理通用的 {@link PessimisticLockingFailureException}，\n * 其语义涵盖更广的锁相关失败。\n *\n * @author Rod Johnson\n * @deprecated as of 6.0.3, in favor of\n * {@link PessimisticLockingFailureException}/{@link CannotAcquireLockException}\n */",
        ),
        (
            "\t/**\n\t * Constructor for DeadlockLoserDataAccessException.\n\t * @param msg the detail message\n\t * @param cause the root cause from the data access API in use\n\t */",
            "\t/**\n\t * DeadlockLoserDataAccessException 构造函数。\n\t * @param msg 详细消息\n\t * @param cause 所用数据访问 API 的根因\n\t */",
        ),
    ],
    "DuplicateKeyException.java": [
        (
            "/**\n * Exception thrown when an attempt to insert or update data\n * results in violation of a primary key or unique constraint.\n * Note that this is not necessarily a purely relational concept;\n * unique primary keys are required by most database types.\n *\n * <p>Consider handling the general {@link DataIntegrityViolationException}\n * instead, semantically including a wider range of constraint violations.\n *\n * @author Thomas Risberg\n */",
            "/**\n * insert 或 update 违反主键或唯一约束时抛出。\n * 注意：这并非必然为纯粹的关系型概念；\n * 唯一主键为大多数数据库类型所要求。\n *\n * <p>建议改为处理通用的 {@link DataIntegrityViolationException}，\n * 其语义涵盖更广的约束违反。\n *\n * @author Thomas Risberg\n */",
        ),
        (
            "\t/**\n\t * Constructor for DuplicateKeyException.\n\t * @param msg the detail message\n\t */",
            "\t/**\n\t * DuplicateKeyException 构造函数。\n\t * @param msg 详细消息\n\t */",
        ),
        (
            "\t/**\n\t * Constructor for DuplicateKeyException.\n\t * @param msg the detail message\n\t * @param cause the root cause from the data access API in use\n\t */",
            "\t/**\n\t * DuplicateKeyException 构造函数。\n\t * @param msg 详细消息\n\t * @param cause 所用数据访问 API 的根因\n\t */",
        ),
    ],
    "EmptyResultDataAccessException.java": [
        (
            "/**\n * Data access exception thrown when a result was expected to have at least\n * one row (or element) but zero rows (or elements) were actually returned.\n *\n * @author Juergen Hoeller\n * @since 2.0\n * @see IncorrectResultSizeDataAccessException\n */",
            "/**\n * 预期结果至少包含一行（或一个元素），但实际返回零行（或零元素）时抛出的数据访问异常。\n *\n * @author Juergen Hoeller\n * @since 2.0\n * @see IncorrectResultSizeDataAccessException\n */",
        ),
        (
            "\t/**\n\t * Constructor for EmptyResultDataAccessException.\n\t * @param expectedSize the expected result size\n\t */",
            "\t/**\n\t * EmptyResultDataAccessException 构造函数。\n\t * @param expectedSize 预期结果大小\n\t */",
        ),
        (
            "\t/**\n\t * Constructor for EmptyResultDataAccessException.\n\t * @param msg the detail message\n\t * @param expectedSize the expected result size\n\t */",
            "\t/**\n\t * EmptyResultDataAccessException 构造函数。\n\t * @param msg 详细消息\n\t * @param expectedSize 预期结果大小\n\t */",
        ),
        (
            "\t/**\n\t * Constructor for EmptyResultDataAccessException.\n\t * @param msg the detail message\n\t * @param expectedSize the expected result size\n\t * @param ex the wrapped exception\n\t */",
            "\t/**\n\t * EmptyResultDataAccessException 构造函数。\n\t * @param msg 详细消息\n\t * @param expectedSize 预期结果大小\n\t * @param ex 被包装的异常\n\t */",
        ),
    ],
    "IncorrectResultSizeDataAccessException.java": [
        (
            "/**\n * Data access exception thrown when a result was not of the expected size,\n * for example when expecting a single row but getting 0 or more than 1 rows.\n *\n * @author Juergen Hoeller\n * @author Chris Beams\n * @since 1.0.2\n * @see EmptyResultDataAccessException\n */",
            "/**\n * 结果大小与预期不符时抛出的数据访问异常，\n * 例如预期单行却得到 0 行或多于 1 行。\n *\n * @author Juergen Hoeller\n * @author Chris Beams\n * @since 1.0.2\n * @see EmptyResultDataAccessException\n */",
        ),
        (
            "\t/**\n\t * Constructor for IncorrectResultSizeDataAccessException.\n\t * @param expectedSize the expected result size\n\t */",
            "\t/**\n\t * IncorrectResultSizeDataAccessException 构造函数。\n\t * @param expectedSize 预期结果大小\n\t */",
        ),
        (
            "\t/**\n\t * Constructor for IncorrectResultSizeDataAccessException.\n\t * @param expectedSize the expected result size\n\t * @param actualSize the actual result size (or -1 if unknown)\n\t */",
            "\t/**\n\t * IncorrectResultSizeDataAccessException 构造函数。\n\t * @param expectedSize 预期结果大小\n\t * @param actualSize 实际结果大小（未知时为 -1）\n\t */",
        ),
        (
            "\t/**\n\t * Constructor for IncorrectResultSizeDataAccessException.\n\t * @param msg the detail message\n\t * @param expectedSize the expected result size\n\t */",
            "\t/**\n\t * IncorrectResultSizeDataAccessException 构造函数。\n\t * @param msg 详细消息\n\t * @param expectedSize 预期结果大小\n\t */",
        ),
        (
            "\t/**\n\t * Constructor for IncorrectResultSizeDataAccessException.\n\t * @param msg the detail message\n\t * @param expectedSize the expected result size\n\t * @param ex the wrapped exception\n\t */",
            "\t/**\n\t * IncorrectResultSizeDataAccessException 构造函数。\n\t * @param msg 详细消息\n\t * @param expectedSize 预期结果大小\n\t * @param ex 被包装的异常\n\t */",
        ),
        (
            "\t/**\n\t * Constructor for IncorrectResultSizeDataAccessException.\n\t * @param msg the detail message\n\t * @param expectedSize the expected result size\n\t * @param actualSize the actual result size (or -1 if unknown)\n\t */",
            "\t/**\n\t * IncorrectResultSizeDataAccessException 构造函数。\n\t * @param msg 详细消息\n\t * @param expectedSize 预期结果大小\n\t * @param actualSize 实际结果大小（未知时为 -1）\n\t */",
        ),
        (
            "\t/**\n\t * Constructor for IncorrectResultSizeDataAccessException.\n\t * @param msg the detail message\n\t * @param expectedSize the expected result size\n\t * @param actualSize the actual result size (or -1 if unknown)\n\t * @param ex the wrapped exception\n\t */",
            "\t/**\n\t * IncorrectResultSizeDataAccessException 构造函数。\n\t * @param msg 详细消息\n\t * @param expectedSize 预期结果大小\n\t * @param actualSize 实际结果大小（未知时为 -1）\n\t * @param ex 被包装的异常\n\t */",
        ),
        (
            "\t/**\n\t * Return the expected result size.\n\t */",
            "\t/**\n\t * 返回预期结果大小。\n\t */",
        ),
        (
            "\t/**\n\t * Return the actual result size (or -1 if unknown).\n\t */",
            "\t/**\n\t * 返回实际结果大小（未知时为 -1）。\n\t */",
        ),
    ],
    "IncorrectUpdateSemanticsDataAccessException.java": [
        (
            "/**\n * Data access exception thrown when something unintended appears to have\n * happened with an update, but the transaction hasn't already been rolled back.\n * Thrown, for example, when we wanted to update 1 row in an RDBMS but actually\n * updated 3.\n *\n * @author Rod Johnson\n */",
            "/**\n * update 操作出现非预期情况但事务尚未回滚时抛出的数据访问异常。\n * 例如，在 RDBMS 中预期更新 1 行却实际更新了 3 行。\n *\n * @author Rod Johnson\n */",
        ),
        (
            "\t/**\n\t * Constructor for IncorrectUpdateSemanticsDataAccessException.\n\t * @param msg the detail message\n\t */",
            "\t/**\n\t * IncorrectUpdateSemanticsDataAccessException 构造函数。\n\t * @param msg 详细消息\n\t */",
        ),
        (
            "\t/**\n\t * Constructor for IncorrectUpdateSemanticsDataAccessException.\n\t * @param msg the detail message\n\t * @param cause the root cause from the underlying API, such as JDBC\n\t */",
            "\t/**\n\t * IncorrectUpdateSemanticsDataAccessException 构造函数。\n\t * @param msg 详细消息\n\t * @param cause 底层 API（如 JDBC）的根因\n\t */",
        ),
        (
            "\t/**\n\t * Return whether data was updated.\n\t * If this method returns {@code false}, there is nothing to roll back.\n\t * <p>The default implementation always returns {@code true}.\n\t * This can be overridden in subclasses.\n\t */",
            "\t/**\n\t * 返回数据是否已被更新。\n\t * 若返回 {@code false}，则无需回滚。\n\t * <p>默认实现始终返回 {@code true}；\n\t * 子类可覆盖。\n\t */",
        ),
    ],
    "InvalidDataAccessApiUsageException.java": [
        (
            "/**\n * Exception thrown on incorrect usage of the API, such as failing to\n * \"compile\" a query object that needed compilation before execution.\n *\n * <p>This represents a problem in our Java data access framework,\n * not the underlying data access infrastructure.\n *\n * @author Rod Johnson\n */",
            "/**\n * API 使用不当（如未在执行前“编译”需编译的查询对象）时抛出。\n *\n * <p>表示 Java 数据访问框架层面的问题，\n * 而非底层数据访问基础设施的问题。\n *\n * @author Rod Johnson\n */",
        ),
        (
            "\t/**\n\t * Constructor for InvalidDataAccessApiUsageException.\n\t * @param msg the detail message\n\t */",
            "\t/**\n\t * InvalidDataAccessApiUsageException 构造函数。\n\t * @param msg 详细消息\n\t */",
        ),
        (
            "\t/**\n\t * Constructor for InvalidDataAccessApiUsageException.\n\t * @param msg the detail message\n\t * @param cause the root cause from the data access API in use\n\t */",
            "\t/**\n\t * InvalidDataAccessApiUsageException 构造函数。\n\t * @param msg 详细消息\n\t * @param cause 所用数据访问 API 的根因\n\t */",
        ),
    ],
    "InvalidDataAccessResourceUsageException.java": [
        (
            "/**\n * Root for exceptions thrown when we use a data access resource incorrectly.\n * Thrown for example on specifying bad SQL when using a RDBMS.\n * Resource-specific subclasses are supplied by concrete data access packages.\n *\n * @author Rod Johnson\n */",
            "/**\n * 不正确使用数据访问资源时抛出的异常的根类。\n * 例如，在 RDBMS 中指定错误 SQL 时抛出。\n * 具体数据访问包提供针对特定资源的子类。\n *\n * @author Rod Johnson\n */",
        ),
        (
            "\t/**\n\t * Constructor for InvalidDataAccessResourceUsageException.\n\t * @param msg the detail message\n\t */",
            "\t/**\n\t * InvalidDataAccessResourceUsageException 构造函数。\n\t * @param msg 详细消息\n\t */",
        ),
        (
            "\t/**\n\t * Constructor for InvalidDataAccessResourceUsageException.\n\t * @param msg the detail message\n\t * @param cause the root cause from the data access API in use\n\t */",
            "\t/**\n\t * InvalidDataAccessResourceUsageException 构造函数。\n\t * @param msg 详细消息\n\t * @param cause 所用数据访问 API 的根因\n\t */",
        ),
    ],
    "NonTransientDataAccessException.java": [
        (
            "/**\n * Root of the hierarchy of data access exceptions that are considered non-transient -\n * where a retry of the same operation would fail unless the cause of the Exception\n * is corrected.\n *\n * @author Thomas Risberg\n * @since 2.5\n * @see java.sql.SQLNonTransientException\n */",
            "/**\n * 被视为非瞬态的数据访问异常层次结构的根类——\n * 除非修正异常原因，否则重试相同操作仍会失败。\n *\n * @author Thomas Risberg\n * @since 2.5\n * @see java.sql.SQLNonTransientException\n */",
        ),
        (
            "\t/**\n\t * Constructor for NonTransientDataAccessException.\n\t * @param msg the detail message\n\t */",
            "\t/**\n\t * NonTransientDataAccessException 构造函数。\n\t * @param msg 详细消息\n\t */",
        ),
        (
            "\t/**\n\t * Constructor for NonTransientDataAccessException.\n\t * @param msg the detail message\n\t * @param cause the root cause (usually from using an underlying\n\t * data access API such as JDBC)\n\t */",
            "\t/**\n\t * NonTransientDataAccessException 构造函数。\n\t * @param msg 详细消息\n\t * @param cause 根因（通常来自底层数据访问 API，如 JDBC）\n\t */",
        ),
    ],
    "NonTransientDataAccessResourceException.java": [
        (
            "/**\n * Data access exception thrown when a resource fails completely and the failure is permanent.\n *\n * @author Thomas Risberg\n * @since 2.5\n * @see java.sql.SQLNonTransientConnectionException\n */",
            "/**\n * 资源完全不可用且失败为永久性时抛出的数据访问异常。\n *\n * @author Thomas Risberg\n * @since 2.5\n * @see java.sql.SQLNonTransientConnectionException\n */",
        ),
        (
            "\t/**\n\t * Constructor for NonTransientDataAccessResourceException.\n\t * @param msg the detail message\n\t */",
            "\t/**\n\t * NonTransientDataAccessResourceException 构造函数。\n\t * @param msg 详细消息\n\t */",
        ),
        (
            "\t/**\n\t * Constructor for NonTransientDataAccessResourceException.\n\t * @param msg the detail message\n\t * @param cause the root cause from the data access API in use\n\t */",
            "\t/**\n\t * NonTransientDataAccessResourceException 构造函数。\n\t * @param msg 详细消息\n\t * @param cause 所用数据访问 API 的根因\n\t */",
        ),
    ],
    "OptimisticLockingFailureException.java": [
        (
            "/**\n * Exception thrown on an optimistic locking violation.\n *\n * <p>This exception will be thrown either by O/R mapping tools\n * or by custom DAO implementations. Optimistic locking failure\n * is typically <i>not</i> detected by the database itself.\n *\n * @author Rod Johnson\n * @see PessimisticLockingFailureException\n */",
            "/**\n * 发生乐观锁冲突时抛出。\n *\n * <p>本异常由 O/R 映射工具或自定义 DAO 实现抛出。\n * 乐观锁失败通常<i>不会</i>由数据库本身检测。\n *\n * @author Rod Johnson\n * @see PessimisticLockingFailureException\n */",
        ),
        (
            "\t/**\n\t * Constructor for OptimisticLockingFailureException.\n\t * @param msg the detail message\n\t */",
            "\t/**\n\t * OptimisticLockingFailureException 构造函数。\n\t * @param msg 详细消息\n\t */",
        ),
        (
            "\t/**\n\t * Constructor for OptimisticLockingFailureException.\n\t * @param msg the detail message\n\t * @param cause the root cause from the data access API in use\n\t */",
            "\t/**\n\t * OptimisticLockingFailureException 构造函数。\n\t * @param msg 详细消息\n\t * @param cause 所用数据访问 API 的根因\n\t */",
        ),
    ],
}