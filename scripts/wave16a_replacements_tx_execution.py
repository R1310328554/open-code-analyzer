"""Chinese JavaDoc replacements for springframework wave16a transaction execution interfaces [10:19]."""

TX_EXECUTION_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "ConfigurableTransactionManager.java": [
        (
            "/**\n * Common configuration interface for transaction manager implementations.\n * Provides registration facilities for {@link TransactionExecutionListener}.\n *\n * @author Juergen Hoeller\n * @since 6.1\n * @see PlatformTransactionManager\n * @see ReactiveTransactionManager\n */",
            "/**\n * 事务管理器实现的通用配置接口。\n * 提供 {@link TransactionExecutionListener} 的注册能力。\n *\n * @author Juergen Hoeller\n * @since 6.1\n * @see PlatformTransactionManager\n * @see ReactiveTransactionManager\n */",
        ),
        (
            "\t/**\n\t * Set the transaction execution listeners for begin/commit/rollback callbacks\n\t * from this transaction manager.\n\t * @see #addListener\n\t */",
            "\t/**\n\t * 设置本事务管理器的 begin/commit/rollback 回调\n\t * 所用的事务执行监听器。\n\t * @see #addListener\n\t */",
        ),
        (
            "\t/**\n\t * Return the registered transaction execution listeners for this transaction manager.\n\t * @see #setTransactionExecutionListeners\n\t */",
            "\t/**\n\t * 返回本事务管理器已注册的事务执行监听器。\n\t * @see #setTransactionExecutionListeners\n\t */",
        ),
        (
            "\t/**\n\t * Conveniently register the given listener for begin/commit/rollback callbacks\n\t * from this transaction manager.\n\t * @see #getTransactionExecutionListeners()\n\t */",
            "\t/**\n\t * 便捷注册给定监听器，用于本事务管理器的\n\t * begin/commit/rollback 回调。\n\t * @see #getTransactionExecutionListeners()\n\t */",
        ),
    ],
    "ReactiveTransaction.java": [
        (
            "/**\n * Representation of an ongoing {@link ReactiveTransactionManager} transaction.\n * This is currently a marker interface extending {@link TransactionExecution}\n * but may acquire further methods in a future revision.\n *\n * <p>Transactional code can use this to retrieve status information,\n * and to programmatically request a rollback (instead of throwing\n * an exception that causes an implicit rollback).\n *\n * @author Mark Paluch\n * @author Juergen Hoeller\n * @since 5.2\n * @see #setRollbackOnly()\n * @see ReactiveTransactionManager#getReactiveTransaction\n * @see org.springframework.transaction.reactive.TransactionCallback#doInTransaction\n */",
            "/**\n * 进行中的 {@link ReactiveTransactionManager} 事务的表示。\n * 当前为扩展 {@link TransactionExecution} 的标记接口，\n * 未来版本可能增加更多方法。\n *\n * <p>事务代码可用其获取状态信息，\n * 并以编程方式请求回滚（而非抛出导致隐式回滚的异常）。\n *\n * @author Mark Paluch\n * @author Juergen Hoeller\n * @since 5.2\n * @see #setRollbackOnly()\n * @see ReactiveTransactionManager#getReactiveTransaction\n * @see org.springframework.transaction.reactive.TransactionCallback#doInTransaction\n */",
        ),
    ],
    "TransactionExecution.java": [
        (
            "/**\n * Common representation of the current state of a transaction.\n * Serves as base interface for {@link TransactionStatus} as well as\n * {@link ReactiveTransaction}, and as of 6.1 also as transaction\n * representation for {@link TransactionExecutionListener}.\n *\n * @author Juergen Hoeller\n * @since 5.2\n */",
            "/**\n * 当前事务状态的通用表示。\n * 作为 {@link TransactionStatus} 与 {@link ReactiveTransaction} 的基接口，\n * 自 6.1 起也作为 {@link TransactionExecutionListener} 的事务表示。\n *\n * @author Juergen Hoeller\n * @since 5.2\n */",
        ),
        (
            "\t/**\n\t * Return the defined name of the transaction (possibly an empty String).\n\t * <p>In case of Spring's declarative transactions, the exposed name will be\n\t * the {@code fully-qualified class name + \".\" + method name} (by default).\n\t * <p>The default implementation returns an empty String.\n\t * @since 6.1\n\t * @see TransactionDefinition#getName()\n\t */",
            "\t/**\n\t * 返回事务的定义名称（可能为空字符串）。\n\t * <p>对于 Spring 声明式事务，暴露的名称默认为\n\t * {@code 全限定类名 + \".\" + 方法名}。\n\t * <p>默认实现返回空字符串。\n\t * @since 6.1\n\t * @see TransactionDefinition#getName()\n\t */",
        ),
        (
            "\t/**\n\t * Return whether there is an actual transaction active: this is meant to cover\n\t * a new transaction as well as participation in an existing transaction, only\n\t * returning {@code false} when not running in an actual transaction at all.\n\t * <p>The default implementation returns {@code true}.\n\t * @since 6.1\n\t * @see #isNewTransaction()\n\t * @see #isNested()\n\t * @see #isReadOnly()\n\t */",
            "\t/**\n\t * 返回是否存在实际活动事务：涵盖新事务及参与现有事务，\n\t * 仅当完全未运行于实际事务中时返回 {@code false}。\n\t * <p>默认实现返回 {@code true}。\n\t * @since 6.1\n\t * @see #isNewTransaction()\n\t * @see #isNested()\n\t * @see #isReadOnly()\n\t */",
        ),
        (
            "\t/**\n\t * Return whether the transaction manager considers the present transaction\n\t * as new; otherwise participating in an existing transaction, or potentially\n\t * not running in an actual transaction in the first place.\n\t * <p>This is primarily here for transaction manager state handling.\n\t * Prefer the use of {@link #hasTransaction()} for application purposes\n\t * since this is usually semantically appropriate.\n\t * <p>The \"new\" status can be transaction manager specific, for example, returning\n\t * {@code true} for an actual nested transaction but potentially {@code false}\n\t * for a savepoint-based nested transaction scope if the savepoint management\n\t * is explicitly exposed (such as on {@link TransactionStatus}). A combined\n\t * check for any kind of nested execution is provided by {@link #isNested()}.\n\t * <p>The default implementation returns {@code true}.\n\t * @see #hasTransaction()\n\t * @see #isNested()\n\t * @see TransactionStatus#hasSavepoint()\n\t */",
            "\t/**\n\t * 返回事务管理器是否将当前事务视为新事务；\n\t * 否则为参与现有事务，或可能根本未运行于实际事务中。\n\t * <p>主要用于事务管理器状态处理。\n\t * 应用层更宜使用 {@link #hasTransaction()}，语义通常更合适。\n\t * <p>\"新\" 状态可能因事务管理器而异，例如实际嵌套事务返回 {@code true}，\n\t * 但若显式暴露保存点管理（如 {@link TransactionStatus}），\n\t * 基于保存点的嵌套事务范围可能返回 {@code false}。\n\t * {@link #isNested()} 提供对任意嵌套执行的联合检查。\n\t * <p>默认实现返回 {@code true}。\n\t * @see #hasTransaction()\n\t * @see #isNested()\n\t * @see TransactionStatus#hasSavepoint()\n\t */",
        ),
        (
            "\t/**\n\t * Return if this transaction executes in a nested fashion within another.\n\t * <p>The default implementation returns {@code false}.\n\t * @since 6.1\n\t * @see #hasTransaction()\n\t * @see #isNewTransaction()\n\t * @see TransactionDefinition#PROPAGATION_NESTED\n\t */",
            "\t/**\n\t * 返回本事务是否以嵌套方式在另一事务内执行。\n\t * <p>默认实现返回 {@code false}。\n\t * @since 6.1\n\t * @see #hasTransaction()\n\t * @see #isNewTransaction()\n\t * @see TransactionDefinition#PROPAGATION_NESTED\n\t */",
        ),
        (
            "\t/**\n\t * Return if this transaction is defined as read-only transaction.\n\t * <p>The default implementation returns {@code false}.\n\t * @since 6.1\n\t * @see TransactionDefinition#isReadOnly()\n\t */",
            "\t/**\n\t * 返回本事务是否定义为只读事务。\n\t * <p>默认实现返回 {@code false}。\n\t * @since 6.1\n\t * @see TransactionDefinition#isReadOnly()\n\t */",
        ),
        (
            "\t/**\n\t * Set the transaction rollback-only. This instructs the transaction manager\n\t * that the only possible outcome of the transaction may be a rollback, as\n\t * alternative to throwing an exception which would in turn trigger a rollback.\n\t * <p>The default implementation throws an UnsupportedOperationException.\n\t * @see #isRollbackOnly()\n\t */",
            "\t/**\n\t * 将事务设为仅回滚。通知事务管理器事务唯一可能结果为回滚，\n\t * 作为抛出异常触发回滚的替代方式。\n\t * <p>默认实现抛出 UnsupportedOperationException。\n\t * @see #isRollbackOnly()\n\t */",
        ),
        (
            "\t/**\n\t * Return whether the transaction has been marked as rollback-only\n\t * (either by the application or by the transaction infrastructure).\n\t * <p>The default implementation returns {@code false}.\n\t * @see #setRollbackOnly()\n\t */",
            "\t/**\n\t * 返回事务是否已被标记为仅回滚\n\t * （由应用或事务基础设施标记）。\n\t * <p>默认实现返回 {@code false}。\n\t * @see #setRollbackOnly()\n\t */",
        ),
        (
            "\t/**\n\t * Return whether this transaction is completed, that is,\n\t * whether it has already been committed or rolled back.\n\t * <p>The default implementation returns {@code false}.\n\t */",
            "\t/**\n\t * 返回本事务是否已完成，\n\t * 即是否已提交或已回滚。\n\t * <p>默认实现返回 {@code false}。\n\t */",
        ),
    ],
    "TransactionExecutionListener.java": [
        (
            "/**\n * Callback interface for stateless listening to transaction creation/completion steps\n * in a transaction manager. This is primarily meant for observation and statistics;\n * consider stateful transaction synchronizations for resource management purposes.\n *\n * <p>In contrast to synchronizations, the transaction execution listener contract is\n * commonly supported for thread-bound transactions as well as reactive transactions.\n * The callback-provided {@link TransactionExecution} object will be either a\n * {@link TransactionStatus} (for a {@link PlatformTransactionManager} transaction) or\n * a {@link ReactiveTransaction} (for a {@link ReactiveTransactionManager} transaction).\n *\n * @author Juergen Hoeller\n * @since 6.1\n * @see ConfigurableTransactionManager#addListener\n * @see org.springframework.transaction.support.TransactionSynchronizationManager#registerSynchronization\n * @see org.springframework.transaction.reactive.TransactionSynchronizationManager#registerSynchronization\n */",
            "/**\n * 无状态监听事务管理器中事务创建/完成步骤的回调接口。\n * 主要用于观测与统计；资源管理请考虑有状态事务同步。\n *\n * <p>与同步机制不同，事务执行监听器契约通常同时支持\n * 线程绑定事务与响应式事务。\n * 回调提供的 {@link TransactionExecution} 对象或为\n * {@link TransactionStatus}（{@link PlatformTransactionManager} 事务），\n * 或为 {@link ReactiveTransaction}（{@link ReactiveTransactionManager} 事务）。\n *\n * @author Juergen Hoeller\n * @since 6.1\n * @see ConfigurableTransactionManager#addListener\n * @see org.springframework.transaction.support.TransactionSynchronizationManager#registerSynchronization\n * @see org.springframework.transaction.reactive.TransactionSynchronizationManager#registerSynchronization\n */",
        ),
        (
            "\t/**\n\t * Callback before the transaction begin step.\n\t * @param transaction the current transaction\n\t */",
            "\t/**\n\t * 事务 begin 步骤之前的回调。\n\t * @param transaction 当前事务\n\t */",
        ),
        (
            "\t/**\n\t * Callback after the transaction begin step.\n\t * @param transaction the current transaction\n\t * @param beginFailure an exception occurring during begin\n\t * (or {@code null} after a successful begin step)\n\t */",
            "\t/**\n\t * 事务 begin 步骤之后的回调。\n\t * @param transaction 当前事务\n\t * @param beginFailure begin 期间发生的异常\n\t * （成功 begin 后为 {@code null}）\n\t */",
        ),
        (
            "\t/**\n\t * Callback before the transaction commit step.\n\t * @param transaction the current transaction\n\t */",
            "\t/**\n\t * 事务 commit 步骤之前的回调。\n\t * @param transaction 当前事务\n\t */",
        ),
        (
            "\t/**\n\t * Callback after the transaction commit step.\n\t * @param transaction the current transaction\n\t * @param commitFailure an exception occurring during commit\n\t * (or {@code null} after a successful commit step)\n\t */",
            "\t/**\n\t * 事务 commit 步骤之后的回调。\n\t * @param transaction 当前事务\n\t * @param commitFailure commit 期间发生的异常\n\t * （成功 commit 后为 {@code null}）\n\t */",
        ),
        (
            "\t/**\n\t * Callback before the transaction rollback step.\n\t * @param transaction the current transaction\n\t */",
            "\t/**\n\t * 事务 rollback 步骤之前的回调。\n\t * @param transaction 当前事务\n\t */",
        ),
        (
            "\t/**\n\t * Callback after the transaction rollback step.\n\t * @param transaction the current transaction\n\t * @param rollbackFailure an exception occurring during rollback\n\t * (or {@code null} after a successful rollback step)\n\t */",
            "\t/**\n\t * 事务 rollback 步骤之后的回调。\n\t * @param transaction 当前事务\n\t * @param rollbackFailure rollback 期间发生的异常\n\t * （成功 rollback 后为 {@code null}）\n\t */",
        ),
    ],
    "TransactionStatus.java": [
        (
            "/**\n * Representation of an ongoing {@link PlatformTransactionManager} transaction.\n * Extends the common {@link TransactionExecution} interface.\n *\n * <p>Transactional code can use this to retrieve status information,\n * and to programmatically request a rollback (instead of throwing\n * an exception that causes an implicit rollback).\n *\n * <p>Includes the {@link SavepointManager} interface to provide access\n * to savepoint management facilities. Note that savepoint management\n * is only available if supported by the underlying transaction manager.\n *\n * @author Juergen Hoeller\n * @since 27.03.2003\n * @see #setRollbackOnly()\n * @see PlatformTransactionManager#getTransaction\n * @see org.springframework.transaction.support.TransactionCallback#doInTransaction\n * @see org.springframework.transaction.interceptor.TransactionInterceptor#currentTransactionStatus()\n */",
            "/**\n * 进行中的 {@link PlatformTransactionManager} 事务的表示。\n * 扩展通用 {@link TransactionExecution} 接口。\n *\n * <p>事务代码可用其获取状态信息，\n * 并以编程方式请求回滚（而非抛出导致隐式回滚的异常）。\n *\n * <p>包含 {@link SavepointManager} 接口以提供保存点管理能力。\n * 注意，仅当底层事务管理器支持时保存点管理才可用。\n *\n * @author Juergen Hoeller\n * @since 27.03.2003\n * @see #setRollbackOnly()\n * @see PlatformTransactionManager#getTransaction\n * @see org.springframework.transaction.support.TransactionCallback#doInTransaction\n * @see org.springframework.transaction.interceptor.TransactionInterceptor#currentTransactionStatus()\n */",
        ),
        (
            "\t/**\n\t * Return whether this transaction internally carries a savepoint,\n\t * that is, has been created as nested transaction based on a savepoint.\n\t * <p>This method is mainly here for diagnostic purposes, alongside\n\t * {@link #isNewTransaction()}. For programmatic handling of custom\n\t * savepoints, use the operations provided by {@link SavepointManager}.\n\t * <p>The default implementation returns {@code false}.\n\t * @see #isNewTransaction()\n\t * @see #createSavepoint()\n\t * @see #rollbackToSavepoint(Object)\n\t * @see #releaseSavepoint(Object)\n\t */",
            "\t/**\n\t * 返回本事务内部是否携带保存点，\n\t * 即是否基于保存点创建为嵌套事务。\n\t * <p>本方法主要用于诊断，与 {@link #isNewTransaction()} 配合使用。\n\t * 编程式处理自定义保存点请使用 {@link SavepointManager} 提供的操作。\n\t * <p>默认实现返回 {@code false}。\n\t * @see #isNewTransaction()\n\t * @see #createSavepoint()\n\t * @see #rollbackToSavepoint(Object)\n\t * @see #releaseSavepoint(Object)\n\t */",
        ),
        (
            "\t/**\n\t * Flush the underlying session to the datastore, if applicable:\n\t * for example, all affected Hibernate/JPA sessions.\n\t * <p>This is effectively just a hint and may be a no-op if the underlying\n\t * transaction manager does not have a flush concept. A flush signal may\n\t * get applied to the primary resource or to transaction synchronizations,\n\t * depending on the underlying resource.\n\t * <p>The default implementation is empty, considering flush as a no-op.\n\t */",
            "\t/**\n\t * 若适用，将底层会话刷新到数据存储：\n\t * 例如所有受影响的 Hibernate/JPA 会话。\n\t * <p>这实际上只是提示；若底层事务管理器无 flush 概念可能为空操作。\n\t * flush 信号可能应用于主资源或事务同步，取决于底层资源。\n\t * <p>默认实现为空，将 flush 视为空操作。\n\t */",
        ),
    ],
    "SavepointManager.java": [
        (
            "/**\n * Interface that specifies an API to programmatically manage transaction\n * savepoints in a generic fashion. Extended by TransactionStatus to\n * expose savepoint management functionality for a specific transaction.\n *\n * <p>Note that savepoints can only work within an active transaction.\n * Just use this programmatic savepoint handling for advanced needs;\n * else, a subtransaction with PROPAGATION_NESTED is preferable.\n *\n * <p>This interface is inspired by JDBC's Savepoint mechanism\n * but is independent of any specific persistence technology.\n *\n * @author Juergen Hoeller\n * @since 1.1\n * @see TransactionStatus\n * @see TransactionDefinition#PROPAGATION_NESTED\n * @see java.sql.Savepoint\n */",
            "/**\n * 以通用方式编程管理事务保存点的 API 接口。\n * 由 TransactionStatus 扩展，为特定事务暴露保存点管理功能。\n *\n * <p>注意，保存点仅在活动事务内有效。\n * 仅在高级需求时使用本编程式保存点处理；\n * 否则更宜使用 PROPAGATION_NESTED 的子事务。\n *\n * <p>本接口受 JDBC Savepoint 机制启发，\n * 但不依赖任何特定持久化技术。\n *\n * @author Juergen Hoeller\n * @since 1.1\n * @see TransactionStatus\n * @see TransactionDefinition#PROPAGATION_NESTED\n * @see java.sql.Savepoint\n */",
        ),
        (
            "\t/**\n\t * Create a new savepoint. You can roll back to a specific savepoint\n\t * via {@code rollbackToSavepoint}, and explicitly release a savepoint\n\t * that you don't need anymore via {@code releaseSavepoint}.\n\t * <p>Note that most transaction managers will automatically release\n\t * savepoints at transaction completion.\n\t * @return a savepoint object, to be passed into\n\t * {@link #rollbackToSavepoint} or {@link #releaseSavepoint}\n\t * @throws NestedTransactionNotSupportedException if the underlying\n\t * transaction does not support savepoints\n\t * @throws TransactionException if the savepoint could not be created,\n\t * for example because the transaction is not in an appropriate state\n\t * @see java.sql.Connection#setSavepoint\n\t */",
            "\t/**\n\t * 创建新保存点。可通过 {@code rollbackToSavepoint} 回滚到特定保存点，\n\t * 通过 {@code releaseSavepoint} 显式释放不再需要的保存点。\n\t * <p>注意，多数事务管理器会在事务完成时自动释放保存点。\n\t * @return 保存点对象，传入 {@link #rollbackToSavepoint} 或 {@link #releaseSavepoint}\n\t * @throws NestedTransactionNotSupportedException 底层事务不支持保存点时\n\t * @throws TransactionException 无法创建保存点时，\n\t * 例如事务状态不合适\n\t * @see java.sql.Connection#setSavepoint\n\t */",
        ),
        (
            "\t/**\n\t * Roll back to the given savepoint.\n\t * <p>The savepoint will <i>not</i> be automatically released afterwards.\n\t * You may explicitly call {@link #releaseSavepoint(Object)} or rely on\n\t * automatic release on transaction completion.\n\t * @param savepoint the savepoint to roll back to\n\t * @throws NestedTransactionNotSupportedException if the underlying\n\t * transaction does not support savepoints\n\t * @throws TransactionException if the rollback failed\n\t * @see java.sql.Connection#rollback(java.sql.Savepoint)\n\t */",
            "\t/**\n\t * 回滚到给定保存点。\n\t * <p>保存点之后<i>不会</i>自动释放。\n\t * 可显式调用 {@link #releaseSavepoint(Object)} 或依赖事务完成时的自动释放。\n\t * @param savepoint 要回滚到的保存点\n\t * @throws NestedTransactionNotSupportedException 底层事务不支持保存点时\n\t * @throws TransactionException 回滚失败时\n\t * @see java.sql.Connection#rollback(java.sql.Savepoint)\n\t */",
        ),
        (
            "\t/**\n\t * Explicitly release the given savepoint.\n\t * <p>Note that most transaction managers will automatically release\n\t * savepoints on transaction completion.\n\t * <p>Implementations should fail as silently as possible if proper\n\t * resource cleanup will eventually happen at transaction completion.\n\t * @param savepoint the savepoint to release\n\t * @throws NestedTransactionNotSupportedException if the underlying\n\t * transaction does not support savepoints\n\t * @throws TransactionException if the release failed\n\t * @see java.sql.Connection#releaseSavepoint\n\t */",
            "\t/**\n\t * 显式释放给定保存点。\n\t * <p>注意，多数事务管理器会在事务完成时自动释放保存点。\n\t * <p>若事务完成时最终会正确清理资源，\n\t * 实现应尽可能静默失败。\n\t * @param savepoint 要释放的保存点\n\t * @throws NestedTransactionNotSupportedException 底层事务不支持保存点时\n\t * @throws TransactionException 释放失败时\n\t * @see java.sql.Connection#releaseSavepoint\n\t */",
        ),
    ],
}
