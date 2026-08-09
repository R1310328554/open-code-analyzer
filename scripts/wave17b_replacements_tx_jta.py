"""Chinese JavaDoc replacements for springframework wave17b JTA helpers [17:20]."""

TX_JTA_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "JtaAfterCompletionSynchronization.java": [
        (
            "/**\n * Adapter for a JTA Synchronization, invoking the {@code afterCommit} /\n * {@code afterCompletion} callbacks of Spring {@link TransactionSynchronization}\n * objects callbacks after the outer JTA transaction has completed.\n * Applied when participating in an existing (non-Spring) JTA transaction.\n *\n * @author Juergen Hoeller\n * @since 2.0\n * @see TransactionSynchronization#afterCommit\n * @see TransactionSynchronization#afterCompletion\n */",
            "/**\n * JTA Synchronization 的适配器，在外层 JTA 事务完成后调用\n * Spring {@link TransactionSynchronization} 对象的 {@code afterCommit} /\n * {@code afterCompletion} 回调。\n * 在参与现有（非 Spring）JTA 事务时使用。\n *\n * @author Juergen Hoeller\n * @since 2.0\n * @see TransactionSynchronization#afterCommit\n * @see TransactionSynchronization#afterCompletion\n */",
        ),
        (
            "\t/**\n\t * Create a new JtaAfterCompletionSynchronization for the given synchronization objects.\n\t * @param synchronizations the List of TransactionSynchronization objects\n\t * @see org.springframework.transaction.support.TransactionSynchronization\n\t */",
            "\t/**\n\t * 为给定同步对象创建新的 JtaAfterCompletionSynchronization。\n\t * @param synchronizations TransactionSynchronization 对象列表\n\t * @see org.springframework.transaction.support.TransactionSynchronization\n\t */",
        ),
    ],
    "JtaTransactionObject.java": [
        (
            "/**\n * JTA transaction object, representing a {@link jakarta.transaction.UserTransaction}.\n * Used as transaction object by Spring's {@link JtaTransactionManager}.\n *\n * <p>Note: This is an SPI class, not intended to be used by applications.\n *\n * @author Juergen Hoeller\n * @since 1.1\n * @see JtaTransactionManager\n * @see jakarta.transaction.UserTransaction\n */",
            "/**\n * JTA 事务对象，表示 {@link jakarta.transaction.UserTransaction}。\n * 由 Spring 的 {@link JtaTransactionManager} 用作事务对象。\n *\n * <p>注意：这是 SPI 类，不供应用程序直接使用。\n *\n * @author Juergen Hoeller\n * @since 1.1\n * @see JtaTransactionManager\n * @see jakarta.transaction.UserTransaction\n */",
        ),
        (
            "\t/**\n\t * Create a new JtaTransactionObject for the given JTA UserTransaction.\n\t * @param userTransaction the JTA UserTransaction for the current transaction\n\t * (either a shared object or retrieved through a fresh per-transaction lookup)\n\t */",
            "\t/**\n\t * 为给定 JTA UserTransaction 创建新的 JtaTransactionObject。\n\t * @param userTransaction 当前事务的 JTA UserTransaction\n\t * （共享对象或通过每次事务的新查找获得）\n\t */",
        ),
        (
            "\t/**\n\t * Return the JTA UserTransaction object for the current transaction.\n\t */",
            "\t/**\n\t * 返回当前事务的 JTA UserTransaction 对象。\n\t */",
        ),
        (
            "\t/**\n\t * This implementation checks the UserTransaction's rollback-only flag.\n\t */",
            "\t/**\n\t * 本实现检查 UserTransaction 的 rollback-only 标志。\n\t */",
        ),
        (
            "\t/**\n\t * This implementation triggers flush callbacks,\n\t * assuming that they will flush all affected ORM sessions.\n\t * @see org.springframework.transaction.support.TransactionSynchronization#flush()\n\t */",
            "\t/**\n\t * 本实现触发 flush 回调，\n\t * 假定它们将刷新所有受影响的 ORM Session。\n\t * @see org.springframework.transaction.support.TransactionSynchronization#flush()\n\t */",
        ),
    ],
    "ManagedTransactionAdapter.java": [
        (
            "/**\n * Adapter for a managed JTA Transaction handle, taking a JTA\n * {@link jakarta.transaction.TransactionManager} reference and creating\n * a JTA {@link jakarta.transaction.Transaction} handle for it.\n *\n * @author Juergen Hoeller\n * @since 3.0.2\n */",
            "/**\n * 受管 JTA Transaction 句柄的适配器，接受 JTA\n * {@link jakarta.transaction.TransactionManager} 引用并为其创建\n * JTA {@link jakarta.transaction.Transaction} 句柄。\n *\n * @author Juergen Hoeller\n * @since 3.0.2\n */",
        ),
        (
            "\t/**\n\t * Create a new ManagedTransactionAdapter for the given TransactionManager.\n\t * @param transactionManager the JTA TransactionManager to wrap\n\t */",
            "\t/**\n\t * 为给定 TransactionManager 创建新的 ManagedTransactionAdapter。\n\t * @param transactionManager 要包装的 JTA TransactionManager\n\t */",
        ),
        (
            "\t/**\n\t * Return the JTA TransactionManager that this adapter delegates to.\n\t */",
            "\t/**\n\t * 返回本适配器委托的 JTA TransactionManager。\n\t */",
        ),
    ],
    "SimpleTransactionFactory.java": [
        (
            "/**\n * Default implementation of the {@link TransactionFactory} strategy interface,\n * simply wrapping a standard JTA {@link jakarta.transaction.TransactionManager}.\n *\n * <p>Does not support transaction names; simply ignores any specified name.\n *\n * @author Juergen Hoeller\n * @since 2.5\n * @see jakarta.transaction.TransactionManager#setTransactionTimeout(int)\n * @see jakarta.transaction.TransactionManager#begin()\n * @see jakarta.transaction.TransactionManager#getTransaction()\n */",
            "/**\n * {@link TransactionFactory} 策略接口的默认实现，\n * 简单包装标准 JTA {@link jakarta.transaction.TransactionManager}。\n *\n * <p>不支持事务名称；直接忽略任何指定名称。\n *\n * @author Juergen Hoeller\n * @since 2.5\n * @see jakarta.transaction.TransactionManager#setTransactionTimeout(int)\n * @see jakarta.transaction.TransactionManager#begin()\n * @see jakarta.transaction.TransactionManager#getTransaction()\n */",
        ),
        (
            "\t/**\n\t * Create a new SimpleTransactionFactory for the given TransactionManager.\n\t * @param transactionManager the JTA TransactionManager to wrap\n\t */",
            "\t/**\n\t * 为给定 TransactionManager 创建新的 SimpleTransactionFactory。\n\t * @param transactionManager 要包装的 JTA TransactionManager\n\t */",
        ),
    ],
}
