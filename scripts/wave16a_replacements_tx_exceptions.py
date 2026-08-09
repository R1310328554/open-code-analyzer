"""Chinese JavaDoc replacements for springframework wave16a transaction exceptions [1:10]."""

TX_EXCEPTIONS_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "TransactionException.java": [
        (
            "/**\n * Superclass for all transaction exceptions.\n *\n * @author Rod Johnson\n * @since 17.03.2003\n */",
            "/**\n * 所有事务异常的父类。\n *\n * @author Rod Johnson\n * @since 17.03.2003\n */",
        ),
        (
            "\t/**\n\t * Constructor for TransactionException.\n\t * @param msg the detail message\n\t */",
            "\t/**\n\t * TransactionException 构造函数。\n\t * @param msg 详细消息\n\t */",
        ),
        (
            "\t/**\n\t * Constructor for TransactionException.\n\t * @param msg the detail message\n\t * @param cause the root cause from the transaction API in use\n\t */",
            "\t/**\n\t * TransactionException 构造函数。\n\t * @param msg 详细消息\n\t * @param cause 所用事务 API 的根因\n\t */",
        ),
    ],
    "CannotCreateTransactionException.java": [
        (
            "/**\n * Exception thrown when a transaction can't be created using an\n * underlying transaction API such as JTA.\n *\n * @author Rod Johnson\n * @since 17.03.2003\n */",
            "/**\n * 使用底层事务 API（如 JTA）无法创建事务时抛出。\n *\n * @author Rod Johnson\n * @since 17.03.2003\n */",
        ),
        (
            "\t/**\n\t * Constructor for CannotCreateTransactionException.\n\t * @param msg the detail message\n\t */",
            "\t/**\n\t * CannotCreateTransactionException 构造函数。\n\t * @param msg 详细消息\n\t */",
        ),
        (
            "\t/**\n\t * Constructor for CannotCreateTransactionException.\n\t * @param msg the detail message\n\t * @param cause the root cause from the transaction API in use\n\t */",
            "\t/**\n\t * CannotCreateTransactionException 构造函数。\n\t * @param msg 详细消息\n\t * @param cause 所用事务 API 的根因\n\t */",
        ),
    ],
    "HeuristicCompletionException.java": [
        (
            "/**\n * Exception that represents a transaction failure caused by a heuristic\n * decision on the side of the transaction coordinator.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @since 17.03.2003\n */",
            "/**\n * 表示事务协调器启发式决策导致的事务失败异常。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @since 17.03.2003\n */",
        ),
        (
            "\t/**\n\t * Unknown outcome state.\n\t */",
            "\t/**\n\t * 未知结果状态。\n\t */",
        ),
        (
            "\t/**\n\t * Committed outcome state.\n\t */",
            "\t/**\n\t * 已提交结果状态。\n\t */",
        ),
        (
            "\t/**\n\t * Rolledback outcome state.\n\t */",
            "\t/**\n\t * 已回滚结果状态。\n\t */",
        ),
        (
            "\t/**\n\t * Mixed outcome state.\n\t */",
            "\t/**\n\t * 混合结果状态。\n\t */",
        ),
        (
            "\t/**\n\t * The outcome state of the transaction: have some or all resources been committed?\n\t */",
            "\t/**\n\t * 事务的结果状态：部分或全部资源是否已提交？\n\t */",
        ),
        (
            "\t/**\n\t * Constructor for HeuristicCompletionException.\n\t * @param outcomeState the outcome state of the transaction\n\t * @param cause the root cause from the transaction API in use\n\t */",
            "\t/**\n\t * HeuristicCompletionException 构造函数。\n\t * @param outcomeState 事务的结果状态\n\t * @param cause 所用事务 API 的根因\n\t */",
        ),
        (
            "\t/**\n\t * Return the outcome state of the transaction state,\n\t * as one of the constants in this class.\n\t * @see #STATE_UNKNOWN\n\t * @see #STATE_COMMITTED\n\t * @see #STATE_ROLLED_BACK\n\t * @see #STATE_MIXED\n\t */",
            "\t/**\n\t * 返回事务状态的结果状态，\n\t * 为本类常量之一。\n\t * @see #STATE_UNKNOWN\n\t * @see #STATE_COMMITTED\n\t * @see #STATE_ROLLED_BACK\n\t * @see #STATE_MIXED\n\t */",
        ),
    ],
    "IllegalTransactionStateException.java": [
        (
            "/**\n * Exception thrown when the existence or non-existence of a transaction\n * amounts to an illegal state according to the transaction propagation\n * behavior that applies.\n *\n * @author Juergen Hoeller\n * @since 21.01.2004\n */",
            "/**\n * 根据适用的事务传播行为，事务存在或不存在\n * 构成非法状态时抛出。\n *\n * @author Juergen Hoeller\n * @since 21.01.2004\n */",
        ),
        (
            "\t/**\n\t * Constructor for IllegalTransactionStateException.\n\t * @param msg the detail message\n\t */",
            "\t/**\n\t * IllegalTransactionStateException 构造函数。\n\t * @param msg 详细消息\n\t */",
        ),
        (
            "\t/**\n\t * Constructor for IllegalTransactionStateException.\n\t * @param msg the detail message\n\t * @param cause the root cause from the transaction API in use\n\t */",
            "\t/**\n\t * IllegalTransactionStateException 构造函数。\n\t * @param msg 详细消息\n\t * @param cause 所用事务 API 的根因\n\t */",
        ),
    ],
    "InvalidIsolationLevelException.java": [
        (
            "/**\n * Exception that gets thrown when an invalid isolation level is specified,\n * i.e. an isolation level that the transaction manager implementation\n * doesn't support.\n *\n * @author Juergen Hoeller\n * @since 12.05.2003\n */",
            "/**\n * 指定无效隔离级别时抛出，\n * 即事务管理器实现不支持的隔离级别。\n *\n * @author Juergen Hoeller\n * @since 12.05.2003\n */",
        ),
        (
            "\t/**\n\t * Constructor for InvalidIsolationLevelException.\n\t * @param msg the detail message\n\t */",
            "\t/**\n\t * InvalidIsolationLevelException 构造函数。\n\t * @param msg 详细消息\n\t */",
        ),
    ],
    "InvalidTimeoutException.java": [
        (
            "/**\n * Exception that gets thrown when an invalid timeout is specified,\n * that is, the specified timeout valid is out of range or the\n * transaction manager implementation doesn't support timeouts.\n *\n * @author Juergen Hoeller\n * @since 12.05.2003\n */",
            "/**\n * 指定无效超时时抛出，\n * 即指定超时值超出范围或事务管理器实现不支持超时。\n *\n * @author Juergen Hoeller\n * @since 12.05.2003\n */",
        ),
        (
            "\t/**\n\t * Constructor for InvalidTimeoutException.\n\t * @param msg the detail message\n\t * @param timeout the invalid timeout value\n\t */",
            "\t/**\n\t * InvalidTimeoutException 构造函数。\n\t * @param msg 详细消息\n\t * @param timeout 无效的超时值\n\t */",
        ),
        (
            "\t/**\n\t * Return the invalid timeout value.\n\t */",
            "\t/**\n\t * 返回无效的超时值。\n\t */",
        ),
    ],
    "NestedTransactionNotSupportedException.java": [
        (
            "/**\n * Exception thrown when attempting to work with a nested transaction\n * but nested transactions are not supported by the underlying backend.\n *\n * @author Juergen Hoeller\n * @since 1.1\n */",
            "/**\n * 尝试使用嵌套事务但底层后端不支持嵌套事务时抛出。\n *\n * @author Juergen Hoeller\n * @since 1.1\n */",
        ),
        (
            "\t/**\n\t * Constructor for NestedTransactionNotSupportedException.\n\t * @param msg the detail message\n\t */",
            "\t/**\n\t * NestedTransactionNotSupportedException 构造函数。\n\t * @param msg 详细消息\n\t */",
        ),
        (
            "\t/**\n\t * Constructor for NestedTransactionNotSupportedException.\n\t * @param msg the detail message\n\t * @param cause the root cause from the transaction API in use\n\t */",
            "\t/**\n\t * NestedTransactionNotSupportedException 构造函数。\n\t * @param msg 详细消息\n\t * @param cause 所用事务 API 的根因\n\t */",
        ),
    ],
    "NoTransactionException.java": [
        (
            "/**\n * Exception thrown when an operation is attempted that\n * relies on an existing transaction (such as setting\n * rollback status) and there is no existing transaction.\n * This represents an illegal usage of the transaction API.\n *\n * @author Rod Johnson\n * @since 17.03.2003\n */",
            "/**\n * 尝试执行依赖现有事务的操作（如设置回滚状态）\n * 但不存在现有事务时抛出。\n * 表示对事务 API 的非法使用。\n *\n * @author Rod Johnson\n * @since 17.03.2003\n */",
        ),
        (
            "\t/**\n\t * Constructor for NoTransactionException.\n\t * @param msg the detail message\n\t */",
            "\t/**\n\t * NoTransactionException 构造函数。\n\t * @param msg 详细消息\n\t */",
        ),
        (
            "\t/**\n\t * Constructor for NoTransactionException.\n\t * @param msg the detail message\n\t * @param cause the root cause from the transaction API in use\n\t */",
            "\t/**\n\t * NoTransactionException 构造函数。\n\t * @param msg 详细消息\n\t * @param cause 所用事务 API 的根因\n\t */",
        ),
    ],
    "StaticTransactionDefinition.java": [
        (
            "/**\n * A static unmodifiable transaction definition.\n *\n * @author Juergen Hoeller\n * @since 5.2\n * @see TransactionDefinition#withDefaults()\n */",
            "/**\n * 静态不可修改的事务定义。\n *\n * @author Juergen Hoeller\n * @since 5.2\n * @see TransactionDefinition#withDefaults()\n */",
        ),
    ],
    "TransactionManager.java": [
        (
            "/**\n * Marker interface for Spring transaction manager implementations,\n * either traditional or reactive.\n *\n * @author Juergen Hoeller\n * @since 5.2\n * @see PlatformTransactionManager\n * @see ReactiveTransactionManager\n */",
            "/**\n * Spring 事务管理器实现的标记接口，\n * 可为传统或响应式。\n *\n * @author Juergen Hoeller\n * @since 5.2\n * @see PlatformTransactionManager\n * @see ReactiveTransactionManager\n */",
        ),
    ],
}
