"""Chinese JavaDoc replacements for springframework wave18b tx.support [10:17]."""

TX_SUPPORT_B_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "SmartTransactionObject.java": [
        (
            "/**\n * Interface to be implemented by transaction objects that are able to\n * return an internal rollback-only marker, typically from another\n * transaction that has participated and marked it as rollback-only.\n *\n * <p>Autodetected by {@link DefaultTransactionStatus} in order to always\n * return a current rollbackOnly flag even if not resulting from the current\n * TransactionStatus.\n *\n * @author Juergen Hoeller\n * @since 1.1\n * @see DefaultTransactionStatus#isGlobalRollbackOnly()\n */",
            "/**\n * 由能够返回内部 rollback-only 标记的事务对象实现的接口，\n * 该标记通常来自已参与并将之标记为 rollback-only 的其他事务。\n *\n * <p>由 {@link DefaultTransactionStatus} 自动检测，\n * 以便即使非当前 TransactionStatus 所致也能始终返回当前的 rollbackOnly 标志。\n *\n * @author Juergen Hoeller\n * @since 1.1\n * @see DefaultTransactionStatus#isGlobalRollbackOnly()\n */",
        ),
        (
            "\t/**\n\t * Return whether the transaction is internally marked as rollback-only.\n\t * Can, for example, check the JTA UserTransaction.\n\t * <p>The default implementation returns {@code false}.\n\t * @see jakarta.transaction.UserTransaction#getStatus\n\t * @see jakarta.transaction.Status#STATUS_MARKED_ROLLBACK\n\t */",
            "\t/**\n\t * 返回事务是否在内部被标记为 rollback-only。\n\t * 例如可检查 JTA UserTransaction。\n\t * <p>默认实现返回 {@code false}。\n\t * @see jakarta.transaction.UserTransaction#getStatus\n\t * @see jakarta.transaction.Status#STATUS_MARKED_ROLLBACK\n\t */",
        ),
        (
            "\t/**\n\t * Flush the underlying sessions to the datastore, if applicable:\n\t * for example, all affected Hibernate/JPA sessions.\n\t * <p>The default implementation is empty, considering flush as a no-op.\n\t */",
            "\t/**\n\t * 若适用，将底层 Session flush 到数据存储：\n\t * 例如所有受影响的 Hibernate/JPA Session。\n\t * <p>默认实现为空，将 flush 视为无操作。\n\t */",
        ),
    ],
    "TransactionCallback.java": [
        (
            "/**\n * Callback interface for transactional code. Used with {@link TransactionTemplate}'s\n * {@code execute} method, often as anonymous class within a method implementation.\n *\n * <p>Typically used to assemble various calls to transaction-unaware data access\n * services into a higher-level service method with transaction demarcation. As an\n * alternative, consider the use of declarative transaction demarcation (for example, through\n * Spring's {@link org.springframework.transaction.annotation.Transactional} annotation).\n *\n * @author Juergen Hoeller\n * @since 17.03.2003\n * @param <T> the result type\n * @see TransactionTemplate\n * @see CallbackPreferringPlatformTransactionManager\n */",
            "/**\n * 事务代码的回调接口。与 {@link TransactionTemplate} 的\n * {@code execute} 方法配合使用，常在方法实现中以匿名类形式出现。\n *\n * <p>通常用于将多个对无事务感知数据访问服务的调用\n * 组装到带事务边界的高层服务方法中。\n * 也可考虑声明式事务边界（例如通过 Spring 的\n * {@link org.springframework.transaction.annotation.Transactional} 注解）。\n *\n * @author Juergen Hoeller\n * @since 17.03.2003\n * @param <T> 结果类型\n * @see TransactionTemplate\n * @see CallbackPreferringPlatformTransactionManager\n */",
        ),
        (
            "\t/**\n\t * Gets called by {@link TransactionTemplate#execute} within a transactional context.\n\t * Does not need to care about transactions itself, although it can retrieve and\n\t * influence the status of the current transaction via the given status object,\n\t * for example, setting rollback-only.\n\t * <p>Allows for returning a result object created within the transaction, i.e. a\n\t * domain object or a collection of domain objects. A RuntimeException thrown by the\n\t * callback is treated as application exception that enforces a rollback. Any such\n\t * exception will be propagated to the caller of the template, unless there is a\n\t * problem rolling back, in which case a TransactionException will be thrown.\n\t * @param status associated transaction status\n\t * @return a result object, or {@code null}\n\t * @see TransactionTemplate#execute\n\t * @see CallbackPreferringPlatformTransactionManager#execute\n\t */",
            "\t/**\n\t * 在事务上下文中由 {@link TransactionTemplate#execute} 调用。\n\t * 本身无需关心事务，但可通过给定 status 对象获取并影响当前事务状态，\n\t * 例如设置 rollback-only。\n\t * <p>允许返回在事务内创建的结果对象，即领域对象或领域对象集合。\n\t * 回调抛出的 RuntimeException 视为强制回滚的应用异常。\n\t * 此类异常会传播给模板调用方，除非回滚出现问题，\n\t * 此时将抛出 TransactionException。\n\t * @param status 关联的事务状态\n\t * @return 结果对象，或 {@code null}\n\t * @see TransactionTemplate#execute\n\t * @see CallbackPreferringPlatformTransactionManager#execute\n\t */",
        ),
    ],
    "TransactionCallbackWithoutResult.java": [
        (
            "/**\n * Simple convenience class for TransactionCallback implementation.\n * Allows for implementing a doInTransaction version without result,\n * i.e. without the need for a return statement.\n *\n * @author Juergen Hoeller\n * @since 28.03.2003\n * @see TransactionTemplate\n * @deprecated as of 7.0, superseded by {@link TransactionOperations#executeWithoutResult(Consumer)}\n */",
            "/**\n * TransactionCallback 实现的简单便捷类。\n * 允许实现无返回值的 doInTransaction 版本，\n * 即无需 return 语句。\n *\n * @author Juergen Hoeller\n * @since 28.03.2003\n * @see TransactionTemplate\n * @deprecated 自 7.0 起，由 {@link TransactionOperations#executeWithoutResult(Consumer)} 取代\n */",
        ),
        (
            "\t/**\n\t * Gets called by {@code TransactionTemplate.execute} within a transactional\n\t * context. Does not need to care about transactions itself, although it can retrieve\n\t * and influence the status of the current transaction via the given status object,\n\t * for example, setting rollback-only.\n\t * <p>A RuntimeException thrown by the callback is treated as application\n\t * exception that enforces a rollback. An exception gets propagated to the\n\t * caller of the template.\n\t * <p>Note when using JTA: JTA transactions only work with transactional\n\t * JNDI resources, so implementations need to use such resources if they\n\t * want transaction support.\n\t * @param status associated transaction status\n\t * @see TransactionTemplate#execute\n\t */",
            "\t/**\n\t * 在事务上下文中由 {@code TransactionTemplate.execute} 调用。\n\t * 本身无需关心事务，但可通过给定 status 对象获取并影响当前事务状态，\n\t * 例如设置 rollback-only。\n\t * <p>回调抛出的 RuntimeException 视为强制回滚的应用异常，\n\t * 异常会传播给模板调用方。\n\t * <p>使用 JTA 时注意：JTA 事务仅对事务性 JNDI 资源有效，\n\t * 若需要事务支持，实现须使用此类资源。\n\t * @param status 关联的事务状态\n\t * @see TransactionTemplate#execute\n\t */",
        ),
    ],
    "TransactionOperations.java": [
        (
            "/**\n * Interface specifying basic transaction execution operations.\n * Implemented by {@link TransactionTemplate}. Not often used directly,\n * but a useful option to enhance testability, as it can easily be\n * mocked or stubbed.\n *\n * @author Juergen Hoeller\n * @since 2.0.4\n */",
            "/**\n * 指定基本事务执行操作的接口。\n * 由 {@link TransactionTemplate} 实现。不常直接使用，\n * 但有助于提升可测试性，因为可轻松 mock 或 stub。\n *\n * @author Juergen Hoeller\n * @since 2.0.4\n */",
        ),
        (
            "\t/**\n\t * Execute the action specified by the given callback object within a transaction.\n\t * <p>Allows for returning a result object created within the transaction, that is,\n\t * a domain object or a collection of domain objects. A RuntimeException thrown\n\t * by the callback is treated as a fatal exception that enforces a rollback.\n\t * Such an exception gets propagated to the caller of the template.\n\t * @param action the callback object that specifies the transactional action\n\t * @return a result object returned by the callback, or {@code null} if none\n\t * @throws TransactionException in case of initialization, rollback, or system errors\n\t * @throws RuntimeException if thrown by the TransactionCallback\n\t * @see #executeWithoutResult(Consumer)\n\t */",
            "\t/**\n\t * 在事务中执行给定回调对象指定操作。\n\t * <p>允许返回在事务内创建的结果对象，\n\t * 即领域对象或领域对象集合。回调抛出的 RuntimeException\n\t * 视为强制回滚的致命异常，并传播给模板调用方。\n\t * @param action 指定事务操作的回调对象\n\t * @return 回调返回的结果对象，若无则 {@code null}\n\t * @throws TransactionException 初始化、回滚或系统错误时\n\t * @throws RuntimeException 若 TransactionCallback 抛出\n\t * @see #executeWithoutResult(Consumer)\n\t */",
        ),
        (
            "\t/**\n\t * Execute the action specified by the given {@link Runnable} within a transaction.\n\t * <p>If you need to return an object from the callback or access the\n\t * {@link org.springframework.transaction.TransactionStatus} from within the callback,\n\t * use {@link #execute(TransactionCallback)} instead.\n\t * <p>This variant is analogous to using a {@link TransactionCallbackWithoutResult}\n\t * but with a simplified signature for common cases and conveniently usable with\n\t * lambda expressions.\n\t * @param action the Runnable that specifies the transactional action\n\t * @throws TransactionException in case of initialization, rollback, or system errors\n\t * @throws RuntimeException if thrown by the Runnable\n\t * @since 5.2\n\t * @see #execute(TransactionCallback)\n\t * @see TransactionCallbackWithoutResult\n\t */",
            "\t/**\n\t * 在事务中执行给定 {@link Runnable} 指定的操作。\n\t * <p>若需从回调返回值或在回调内访问\n\t * {@link org.springframework.transaction.TransactionStatus}，\n\t * 请改用 {@link #execute(TransactionCallback)}。\n\t * <p>此变体类似使用 {@link TransactionCallbackWithoutResult}，\n\t * 但针对常见场景简化签名，便于配合 lambda 表达式。\n\t * @param action 指定事务操作的 Runnable\n\t * @throws TransactionException 初始化、回滚或系统错误时\n\t * @throws RuntimeException 若 Runnable 抛出\n\t * @since 5.2\n\t * @see #execute(TransactionCallback)\n\t * @see TransactionCallbackWithoutResult\n\t */",
        ),
        (
            "\t/**\n\t * Return an implementation of the {@code TransactionOperations} interface which\n\t * executes a given {@link TransactionCallback} without an actual transaction.\n\t * <p>Useful for testing: The behavior is equivalent to running with a\n\t * transaction manager with no actual transaction (PROPAGATION_SUPPORTS)\n\t * and no synchronization (SYNCHRONIZATION_NEVER).\n\t * <p>For a {@link TransactionOperations} implementation with actual\n\t * transaction processing, use {@link TransactionTemplate} with an appropriate\n\t * {@link org.springframework.transaction.PlatformTransactionManager}.\n\t * @since 5.2\n\t * @see org.springframework.transaction.TransactionDefinition#PROPAGATION_SUPPORTS\n\t * @see AbstractPlatformTransactionManager#SYNCHRONIZATION_NEVER\n\t * @see TransactionTemplate\n\t */",
            "\t/**\n\t * 返回 {@code TransactionOperations} 接口的实现，\n\t * 在无实际事务的情况下执行给定 {@link TransactionCallback}。\n\t * <p>适用于测试：行为等价于使用无实际事务\n\t * （PROPAGATION_SUPPORTS）且无同步（SYNCHRONIZATION_NEVER）的事务管理器。\n\t * <p>若需带实际事务处理的 {@link TransactionOperations} 实现，\n\t * 请使用 {@link TransactionTemplate} 配合适当的\n\t * {@link org.springframework.transaction.PlatformTransactionManager}。\n\t * @since 5.2\n\t * @see org.springframework.transaction.TransactionDefinition#PROPAGATION_SUPPORTS\n\t * @see AbstractPlatformTransactionManager#SYNCHRONIZATION_NEVER\n\t * @see TransactionTemplate\n\t */",
        ),
    ],
    "TransactionSynchronizationAdapter.java": [
        (
            "/**\n * Simple {@link TransactionSynchronization} adapter containing empty\n * method implementations, for easier overriding of single methods.\n *\n * <p>Also implements the {@link Ordered} interface to enable the execution\n * order of synchronizations to be controlled declaratively. The default\n * {@link #getOrder() order} is {@link Ordered#LOWEST_PRECEDENCE}, indicating\n * late execution; return a lower value for earlier execution.\n *\n * @author Juergen Hoeller\n * @since 22.01.2004\n * @deprecated as of 5.3, in favor of the default methods on the\n * {@link TransactionSynchronization} interface\n */",
            "/**\n * 简单的 {@link TransactionSynchronization} 适配器，\n * 包含空方法实现，便于单独覆盖某个方法。\n *\n * <p>同时实现 {@link Ordered} 接口，\n * 以便声明式控制同步的执行顺序。\n * 默认 {@link #getOrder() order} 为 {@link Ordered#LOWEST_PRECEDENCE}，\n * 表示较晚执行；返回更小值可更早执行。\n *\n * @author Juergen Hoeller\n * @since 22.01.2004\n * @deprecated 自 5.3 起，推荐使用 {@link TransactionSynchronization} 接口上的默认方法\n */",
        ),
    ],
    "WithoutTransactionOperations.java": [
        (
            "/**\n * A {@link TransactionOperations} implementation which executes a given\n * {@link TransactionCallback} without an actual transaction.\n *\n * @author Juergen Hoeller\n * @since 5.2\n * @see TransactionOperations#withoutTransaction()\n */",
            "/**\n * 在无实际事务的情况下执行给定 {@link TransactionCallback} 的\n * {@link TransactionOperations} 实现。\n *\n * @author Juergen Hoeller\n * @since 5.2\n * @see TransactionOperations#withoutTransaction()\n */",
        ),
    ],
}
