"""Chinese JavaDoc replacements for springframework wave18a reactive tx [10:16]."""

REACTIVE_B_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "TransactionSynchronization.java": [
        (
            "/**\n * Interface for reactive transaction synchronization callbacks.\n * Supported by {@link AbstractReactiveTransactionManager}.\n *\n * <p>TransactionSynchronization implementations can implement the\n * {@link org.springframework.core.Ordered} interface to influence their execution order.\n * A synchronization that does not implement the {@link org.springframework.core.Ordered}\n * interface is appended to the end of the synchronization chain.\n *\n * <p>System synchronizations performed by Spring itself use specific order values,\n * allowing for fine-grained interaction with their execution order (if necessary).\n *\n * @author Mark Paluch\n * @author Juergen Hoeller\n * @since 5.2\n * @see TransactionSynchronizationManager\n * @see AbstractReactiveTransactionManager\n */",
            "/**\n * 响应式事务同步回调接口。\n * 由 {@link AbstractReactiveTransactionManager} 支持。\n *\n * <p>TransactionSynchronization 实现可实现 {@link org.springframework.core.Ordered} 接口\n * 以影响执行顺序。未实现 {@link org.springframework.core.Ordered} 接口的同步\n * 将追加到同步链末尾。\n *\n * <p>Spring 自身执行的系统同步使用特定顺序值，\n * 必要时可精细控制其执行顺序。\n *\n * @author Mark Paluch\n * @author Juergen Hoeller\n * @since 5.2\n * @see TransactionSynchronizationManager\n * @see AbstractReactiveTransactionManager\n */",
        ),
        (
            "\t/** Completion status in case of proper commit. */",
            "\t/** 正常提交时的完成状态。 */",
        ),
        (
            "\t/** Completion status in case of proper rollback. */",
            "\t/** 正常回滚时的完成状态。 */",
        ),
        (
            "\t/** Completion status in case of heuristic mixed completion or system errors. */",
            "\t/** 启发式混合完成或系统错误时的完成状态。 */",
        ),
        (
            "\t/**\n\t * Suspend this synchronization.\n\t * Supposed to unbind resources from TransactionSynchronizationManager if managing any.\n\t * @see TransactionSynchronizationManager#unbindResource\n\t */",
            "\t/**\n\t * 挂起本同步。\n\t * 若管理资源，应从 TransactionSynchronizationManager 解绑资源。\n\t * @see TransactionSynchronizationManager#unbindResource\n\t */",
        ),
        (
            "\t/**\n\t * Resume this synchronization.\n\t * Supposed to rebind resources to TransactionSynchronizationManager if managing any.\n\t * @see TransactionSynchronizationManager#bindResource\n\t */",
            "\t/**\n\t * 恢复本同步。\n\t * 若管理资源，应重新绑定资源到 TransactionSynchronizationManager。\n\t * @see TransactionSynchronizationManager#bindResource\n\t */",
        ),
        (
            "\t/**\n\t * Invoked before transaction commit (before \"beforeCompletion\").\n\t * <p>This callback does <i>not</i> mean that the transaction will actually be committed.\n\t * A rollback decision can still occur after this method has been called. This callback\n\t * is rather meant to perform work that's only relevant if a commit still has a chance\n\t * to happen, such as flushing SQL statements to the database.\n\t * <p>Note that exceptions will get propagated to the commit caller and cause a\n\t * rollback of the transaction.\n\t * @param readOnly whether the transaction is defined as read-only transaction\n\t * @throws RuntimeException in case of errors; will be <b>propagated to the caller</b>\n\t * (note: do not throw TransactionException subclasses here!)\n\t * @see #beforeCompletion\n\t */",
            "\t/**\n\t * 在事务提交前调用（在 \"beforeCompletion\" 之前）。\n\t * <p>本回调<i>不</i>表示事务一定会提交。\n\t * 调用本方法后仍可能决定回滚。本回调用于执行仅在仍可能提交时\n\t * 才有意义的工作，例如将 SQL 语句 flush 到数据库。\n\t * <p>注意，异常将传播给提交调用方并导致事务回滚。\n\t * @param readOnly 事务是否定义为只读\n\t * @throws RuntimeException 发生错误时；将<b>传播给调用方</b>\n\t * （注意：不要在此抛出 TransactionException 子类！）\n\t * @see #beforeCompletion\n\t */",
        ),
        (
            "\t/**\n\t * Invoked before transaction commit/rollback.\n\t * Can perform resource cleanup <i>before</i> transaction completion.\n\t * <p>This method will be invoked after {@code beforeCommit}, even when\n\t * {@code beforeCommit} threw an exception. This callback allows for\n\t * closing resources before transaction completion, for any outcome.\n\t * @throws RuntimeException in case of errors; will be <b>logged but not propagated</b>\n\t * (note: do not throw TransactionException subclasses here!)\n\t * @see #beforeCommit\n\t * @see #afterCompletion\n\t */",
            "\t/**\n\t * 在事务提交/回滚前调用。\n\t * 可在事务完成<i>前</i>执行资源清理。\n\t * <p>即使 {@code beforeCommit} 抛出异常，本方法也会在 {@code beforeCommit} 之后调用。\n\t * 本回调允许在任意结果下于事务完成前关闭资源。\n\t * @throws RuntimeException 发生错误时；将<b>记录但不传播</b>\n\t * （注意：不要在此抛出 TransactionException 子类！）\n\t * @see #beforeCommit\n\t * @see #afterCompletion\n\t */",
        ),
        (
            "\t/**\n\t * Invoked after transaction commit. Can perform further operations right\n\t * <i>after</i> the main transaction has <i>successfully</i> committed.\n\t * <p>Can, for example, commit further operations that are supposed to follow on a successful\n\t * commit of the main transaction, like confirmation messages or emails.\n\t * <p><b>NOTE:</b> The transaction will have been committed already, but the\n\t * transactional resources might still be active and accessible. As a consequence,\n\t * any data access code triggered at this point will still \"participate\" in the\n\t * original transaction, allowing to perform some cleanup (with no commit following\n\t * anymore!), unless it explicitly declares that it needs to run in a separate\n\t * transaction. Hence: <b>Use {@code PROPAGATION_REQUIRES_NEW} for any\n\t * transactional operation that is called from here.</b>\n\t * @throws RuntimeException in case of errors; will be <b>propagated to the caller</b>\n\t * (note: do not throw TransactionException subclasses here!)\n\t */",
            "\t/**\n\t * 在事务提交后调用。可在主事务<i>成功</i>提交<i>后</i>立即执行进一步操作。\n\t * <p>例如，可提交主事务成功提交后应执行的后续操作，如确认消息或邮件。\n\t * <p><b>注意：</b>事务已提交，但事务资源可能仍活动且可访问。\n\t * 因此，此触发的任何数据访问代码仍将 \"参与\" 原事务，\n\t * 允许执行一些清理（之后不再有提交！），除非显式声明需在独立事务中运行。\n\t * 因此：<b>从此处调用的任何事务操作请使用 {@code PROPAGATION_REQUIRES_NEW}。</b>\n\t * @throws RuntimeException 发生错误时；将<b>传播给调用方</b>\n\t * （注意：不要在此抛出 TransactionException 子类！）\n\t */",
        ),
        (
            "\t/**\n\t * Invoked after transaction commit/rollback.\n\t * Can perform resource cleanup <i>after</i> transaction completion.\n\t * <p><b>NOTE:</b> The transaction will have been committed or rolled back already,\n\t * but the transactional resources might still be active and accessible. As a\n\t * consequence, any data access code triggered at this point will still \"participate\"\n\t * in the original transaction, allowing to perform some cleanup (with no commit\n\t * following anymore!), unless it explicitly declares that it needs to run in a\n\t * separate transaction. Hence: <b>Use {@code PROPAGATION_REQUIRES_NEW}\n\t * for any transactional operation that is called from here.</b>\n\t * @param status completion status according to the {@code STATUS_*} constants\n\t * @throws RuntimeException in case of errors; will be <b>logged but not propagated</b>\n\t * (note: do not throw TransactionException subclasses here!)\n\t * @see #STATUS_COMMITTED\n\t * @see #STATUS_ROLLED_BACK\n\t * @see #STATUS_UNKNOWN\n\t * @see #beforeCompletion\n\t */",
            "\t/**\n\t * 在事务提交/回滚后调用。\n\t * 可在事务完成<i>后</i>执行资源清理。\n\t * <p><b>注意：</b>事务已提交或回滚，但事务资源可能仍活动且可访问。\n\t * 因此，此触发的任何数据访问代码仍将 \"参与\" 原事务，\n\t * 允许执行一些清理（之后不再有提交！），除非显式声明需在独立事务中运行。\n\t * 因此：<b>从此处调用的任何事务操作请使用 {@code PROPAGATION_REQUIRES_NEW}。</b>\n\t * @param status 根据 {@code STATUS_*} 常量的完成状态\n\t * @throws RuntimeException 发生错误时；将<b>记录但不传播</b>\n\t * （注意：不要在此抛出 TransactionException 子类！）\n\t * @see #STATUS_COMMITTED\n\t * @see #STATUS_ROLLED_BACK\n\t * @see #STATUS_UNKNOWN\n\t * @see #beforeCompletion\n\t */",
        ),
    ],
    "TransactionSynchronizationManager.java": [
        (
            "/**\n * Central delegate that manages resources and transaction synchronizations per\n * subscriber context. To be used by resource management code but not by typical\n * application code.\n *\n * <p>Supports one resource per key without overwriting, that is, a resource needs\n * to be removed before a new one can be set for the same key.\n * Supports a list of transaction synchronizations if synchronization is active.\n *\n * <p>Resource management code should check for context-bound resources, for example,\n * database connections, via {@code getResource}. Such code is normally not\n * supposed to bind resources to units of work, as this is the responsibility\n * of transaction managers. A further option is to lazily bind on first use if\n * transaction synchronization is active, for performing transactions that span\n * an arbitrary number of resources.\n *\n * <p>Transaction synchronization must be activated and deactivated by a transaction\n * manager via {@link #initSynchronization()} and {@link #clearSynchronization()}.\n * This is automatically supported by {@link AbstractReactiveTransactionManager},\n * and thus by all standard Spring transaction managers.\n *\n * <p>Resource management code should only register synchronizations when this\n * manager is active, which can be checked via {@link #isSynchronizationActive};\n * it should perform immediate resource cleanup else. If transaction synchronization\n * isn't active, there is either no current transaction, or the transaction manager\n * doesn't support transaction synchronization.\n *\n * <p>Synchronization is for example used to always return the same resources within\n * a transaction, for example, a database connection for any given connection factory.\n *\n * @author Mark Paluch\n * @author Juergen Hoeller\n * @since 5.2\n * @see #isSynchronizationActive\n * @see #registerSynchronization\n * @see TransactionSynchronization\n */",
            "/**\n * 按订阅者上下文管理资源与事务同步的中心委托。\n * 供资源管理代码使用，非典型应用代码。\n *\n * <p>每个键支持一个资源且不可覆盖，即同一键需先移除资源才能设置新资源。\n * 若同步已激活，支持事务同步列表。\n *\n * <p>资源管理代码应通过 {@code getResource} 检查上下文绑定资源（如数据库连接）。\n * 此类代码通常不应将资源绑定到工作单元，这是事务管理器的职责。\n * 另一选择是在事务同步激活时首次使用时惰性绑定，以执行跨任意数量资源的事务。\n *\n * <p>事务同步须由事务管理器通过 {@link #initSynchronization()} 和\n * {@link #clearSynchronization()} 激活与停用。\n * {@link AbstractReactiveTransactionManager} 自动支持，因此所有标准 Spring 事务管理器均支持。\n *\n * <p>资源管理代码仅在本管理器激活时注册同步，可通过 {@link #isSynchronizationActive} 检查；\n * 否则应立即清理资源。若事务同步未激活，要么无当前事务，要么事务管理器不支持事务同步。\n *\n * <p>同步例如用于在事务内始终返回相同资源，\n * 如对给定连接工厂始终返回同一数据库连接。\n *\n * @author Mark Paluch\n * @author Juergen Hoeller\n * @since 5.2\n * @see #isSynchronizationActive\n * @see #registerSynchronization\n * @see TransactionSynchronization\n */",
        ),
        (
            "\t/**\n\t * Get the {@link TransactionSynchronizationManager} that is associated with\n\t * the current transaction context.\n\t * <p>Mainly intended for code that wants to bind resources or synchronizations.\n\t * @throws NoTransactionException if the transaction info cannot be found &mdash;\n\t * for example, because the method was invoked outside a managed transaction\n\t */",
            "\t/**\n\t * 获取与当前事务上下文关联的 {@link TransactionSynchronizationManager}。\n\t * <p>主要供需要绑定资源或同步的代码使用。\n\t * @throws NoTransactionException 若找不到事务信息——\n\t * 例如因在受管事务外调用方法\n\t */",
        ),
        (
            "\t/**\n\t * Check if there is a resource for the given key bound to the current context.\n\t * @param key the key to check (usually the resource factory)\n\t * @return if there is a value bound to the current context\n\t */",
            "\t/**\n\t * 检查当前上下文是否绑定了给定键的资源。\n\t * @param key 要检查的键（通常为资源工厂）\n\t * @return 当前上下文是否有绑定值\n\t */",
        ),
        (
            "\t/**\n\t * Retrieve a resource for the given key that is bound to the current context.\n\t * @param key the key to check (usually the resource factory)\n\t * @return a value bound to the current context (usually the active\n\t * resource object), or {@code null} if none\n\t */",
            "\t/**\n\t * 获取绑定到当前上下文的给定键资源。\n\t * @param key 要检查的键（通常为资源工厂）\n\t * @return 绑定到当前上下文的值（通常为活动资源对象），无则为 {@code null}\n\t */",
        ),
        (
            "\t/**\n\t * Actually check the value of the resource that is bound for the given key.\n\t */",
            "\t/**\n\t * 实际检查给定键绑定资源的值。\n\t */",
        ),
        (
            "\t/**\n\t * Bind the given resource for the given key to the current context.\n\t * @param key the key to bind the value to (usually the resource factory)\n\t * @param value the value to bind (usually the active resource object)\n\t * @throws IllegalStateException if there is already a value bound to the context\n\t */",
            "\t/**\n\t * 将给定资源以给定键绑定到当前上下文。\n\t * @param key 绑定值的键（通常为资源工厂）\n\t * @param value 要绑定的值（通常为活动资源对象）\n\t * @throws IllegalStateException 若上下文已有绑定值\n\t */",
        ),
        (
            "\t/**\n\t * Unbind a resource for the given key from the current context.\n\t * @param key the key to unbind (usually the resource factory)\n\t * @return the previously bound value (usually the active resource object)\n\t * @throws IllegalStateException if there is no value bound to the context\n\t */",
            "\t/**\n\t * 从当前上下文解绑给定键的资源。\n\t * @param key 要解绑的键（通常为资源工厂）\n\t * @return 先前绑定的值（通常为活动资源对象）\n\t * @throws IllegalStateException 若上下文无绑定值\n\t */",
        ),
        (
            "\t/**\n\t * Unbind a resource for the given key from the current context.\n\t * @param key the key to unbind (usually the resource factory)\n\t * @return the previously bound value, or {@code null} if none bound\n\t */",
            "\t/**\n\t * 从当前上下文解绑给定键的资源。\n\t * @param key 要解绑的键（通常为资源工厂）\n\t * @return 先前绑定的值，无绑定则为 {@code null}\n\t */",
        ),
        (
            "\t/**\n\t * Actually remove the value of the resource that is bound for the given key.\n\t */",
            "\t/**\n\t * 实际移除给定键绑定资源的值。\n\t */",
        ),
        (
            "\t//-------------------------------------------------------------------------\n\t// Management of transaction synchronizations\n\t//-------------------------------------------------------------------------",
            "\t//-------------------------------------------------------------------------\n\t// 事务同步管理\n\t//-------------------------------------------------------------------------",
        ),
        (
            "\t/**\n\t * Return if transaction synchronization is active for the current context.\n\t * Can be called before register to avoid unnecessary instance creation.\n\t * @see #registerSynchronization\n\t */",
            "\t/**\n\t * 返回当前上下文的事务同步是否激活。\n\t * 可在注册前调用以避免不必要的实例创建。\n\t * @see #registerSynchronization\n\t */",
        ),
        (
            "\t/**\n\t * Activate transaction synchronization for the current context.\n\t * Called by a transaction manager on transaction begin.\n\t * @throws IllegalStateException if synchronization is already active\n\t */",
            "\t/**\n\t * 为当前上下文激活事务同步。\n\t * 由事务管理器在事务开始时调用。\n\t * @throws IllegalStateException 若同步已激活\n\t */",
        ),
        (
            "\t/**\n\t * Register a new transaction synchronization for the current context.\n\t * Typically called by resource management code.\n\t * <p>Note that synchronizations can implement the\n\t * {@link org.springframework.core.Ordered} interface.\n\t * They will be executed in an order according to their order value (if any).\n\t * @param synchronization the synchronization object to register\n\t * @throws IllegalStateException if transaction synchronization is not active\n\t * @see org.springframework.core.Ordered\n\t */",
            "\t/**\n\t * 为当前上下文注册新事务同步。\n\t * 通常由资源管理代码调用。\n\t * <p>注意，同步可实现 {@link org.springframework.core.Ordered} 接口，\n\t * 将按 order 值（若有）顺序执行。\n\t * @param synchronization 要注册的同步对象\n\t * @throws IllegalStateException 若事务同步未激活\n\t * @see org.springframework.core.Ordered\n\t */",
        ),
        (
            "\t/**\n\t * Return an unmodifiable snapshot list of all registered synchronizations\n\t * for the current context.\n\t * @return unmodifiable List of TransactionSynchronization instances\n\t * @throws IllegalStateException if synchronization is not active\n\t * @see TransactionSynchronization\n\t */",
            "\t/**\n\t * 返回当前上下文所有已注册同步的不可修改快照列表。\n\t * @return TransactionSynchronization 实例的不可修改 List\n\t * @throws IllegalStateException 若同步未激活\n\t * @see TransactionSynchronization\n\t */",
        ),
        (
            "\t\t// Return unmodifiable snapshot, to avoid ConcurrentModificationExceptions\n\t\t// while iterating and invoking synchronization callbacks that in turn\n\t\t// might register further synchronizations.",
            "\t\t// 返回不可修改快照，避免在迭代并调用同步回调时\n\t\t// 发生 ConcurrentModificationException（回调可能注册更多同步）。",
        ),
        (
            "\t\t\t// Sort lazily here, not in registerSynchronization.",
            "\t\t\t// 在此惰性排序，而非在 registerSynchronization 中。",
        ),
        (
            "\t/**\n\t * Deactivate transaction synchronization for the current context.\n\t * Called by the transaction manager on transaction cleanup.\n\t * @throws IllegalStateException if synchronization is not active\n\t */",
            "\t/**\n\t * 为当前上下文停用事务同步。\n\t * 由事务管理器在事务清理时调用。\n\t * @throws IllegalStateException 若同步未激活\n\t */",
        ),
        (
            "\t//-------------------------------------------------------------------------\n\t// Exposure of transaction characteristics\n\t//-------------------------------------------------------------------------",
            "\t//-------------------------------------------------------------------------\n\t// 暴露事务特性\n\t//-------------------------------------------------------------------------",
        ),
        (
            "\t/**\n\t * Expose the name of the current transaction, if any.\n\t * Called by the transaction manager on transaction begin and on cleanup.\n\t * @param name the name of the transaction, or {@code null} to reset it\n\t * @see org.springframework.transaction.TransactionDefinition#getName()\n\t */",
            "\t/**\n\t * 暴露当前事务名称（若有）。\n\t * 由事务管理器在事务开始和清理时调用。\n\t * @param name 事务名称，或 {@code null} 重置\n\t * @see org.springframework.transaction.TransactionDefinition#getName()\n\t */",
        ),
        (
            "\t/**\n\t * Return the name of the current transaction, or {@code null} if none set.\n\t * To be called by resource management code for optimizations per use case,\n\t * for example to optimize fetch strategies for specific named transactions.\n\t * @see org.springframework.transaction.TransactionDefinition#getName()\n\t */",
            "\t/**\n\t * 返回当前事务名称，未设置则为 {@code null}。\n\t * 供资源管理代码按用例优化，例如为特定命名事务优化 fetch 策略。\n\t * @see org.springframework.transaction.TransactionDefinition#getName()\n\t */",
        ),
        (
            "\t/**\n\t * Expose a read-only flag for the current transaction.\n\t * Called by the transaction manager on transaction begin and on cleanup.\n\t * @param readOnly {@code true} to mark the current transaction\n\t * as read-only; {@code false} to reset such a read-only marker\n\t * @see org.springframework.transaction.TransactionDefinition#isReadOnly()\n\t */",
            "\t/**\n\t * 暴露当前事务的只读标志。\n\t * 由事务管理器在事务开始和清理时调用。\n\t * @param readOnly {@code true} 将当前事务标记为只读；{@code false} 重置只读标记\n\t * @see org.springframework.transaction.TransactionDefinition#isReadOnly()\n\t */",
        ),
        (
            "\t/**\n\t * Return whether the current transaction is marked as read-only.\n\t * To be called by resource management code when preparing a newly\n\t * created resource.\n\t * <p>Note that transaction synchronizations receive the read-only flag\n\t * as argument for the {@code beforeCommit} callback, to be able\n\t * to suppress change detection on commit. The present method is meant\n\t * to be used for earlier read-only checks.\n\t * @see org.springframework.transaction.TransactionDefinition#isReadOnly()\n\t * @see TransactionSynchronization#beforeCommit(boolean)\n\t */",
            "\t/**\n\t * 返回当前事务是否标记为只读。\n\t * 供资源管理代码在准备新创建资源时调用。\n\t * <p>注意，事务同步在 {@code beforeCommit} 回调中接收只读标志，\n\t * 以便在提交时抑制变更检测。本方法用于更早的只读检查。\n\t * @see org.springframework.transaction.TransactionDefinition#isReadOnly()\n\t * @see TransactionSynchronization#beforeCommit(boolean)\n\t */",
        ),
        (
            "\t/**\n\t * Expose an isolation level for the current transaction.\n\t * Called by the transaction manager on transaction begin and on cleanup.\n\t * @param isolationLevel the isolation level to expose, according to the\n\t * R2DBC Connection constants (equivalent to the corresponding Spring\n\t * TransactionDefinition constants), or {@code null} to reset it\n\t * @see org.springframework.transaction.TransactionDefinition#ISOLATION_READ_UNCOMMITTED\n\t * @see org.springframework.transaction.TransactionDefinition#ISOLATION_READ_COMMITTED\n\t * @see org.springframework.transaction.TransactionDefinition#ISOLATION_REPEATABLE_READ\n\t * @see org.springframework.transaction.TransactionDefinition#ISOLATION_SERIALIZABLE\n\t * @see org.springframework.transaction.TransactionDefinition#getIsolationLevel()\n\t */",
            "\t/**\n\t * 暴露当前事务的隔离级别。\n\t * 由事务管理器在事务开始和清理时调用。\n\t * @param isolationLevel 要暴露的隔离级别，按 R2DBC Connection 常量\n\t * （等同于相应 Spring TransactionDefinition 常量），或 {@code null} 重置\n\t * @see org.springframework.transaction.TransactionDefinition#ISOLATION_READ_UNCOMMITTED\n\t * @see org.springframework.transaction.TransactionDefinition#ISOLATION_READ_COMMITTED\n\t * @see org.springframework.transaction.TransactionDefinition#ISOLATION_REPEATABLE_READ\n\t * @see org.springframework.transaction.TransactionDefinition#ISOLATION_SERIALIZABLE\n\t * @see org.springframework.transaction.TransactionDefinition#getIsolationLevel()\n\t */",
        ),
        (
            "\t/**\n\t * Return the isolation level for the current transaction, if any.\n\t * To be called by resource management code when preparing a newly\n\t * created resource (for example, a R2DBC Connection).\n\t * @return the currently exposed isolation level, according to the\n\t * R2DBC Connection constants (equivalent to the corresponding Spring\n\t * TransactionDefinition constants), or {@code null} if none\n\t * @see org.springframework.transaction.TransactionDefinition#ISOLATION_READ_UNCOMMITTED\n\t * @see org.springframework.transaction.TransactionDefinition#ISOLATION_READ_COMMITTED\n\t * @see org.springframework.transaction.TransactionDefinition#ISOLATION_REPEATABLE_READ\n\t * @see org.springframework.transaction.TransactionDefinition#ISOLATION_SERIALIZABLE\n\t * @see org.springframework.transaction.TransactionDefinition#getIsolationLevel()\n\t */",
            "\t/**\n\t * 返回当前事务的隔离级别（若有）。\n\t * 供资源管理代码在准备新创建资源（如 R2DBC Connection）时调用。\n\t * @return 当前暴露的隔离级别，按 R2DBC Connection 常量\n\t * （等同于相应 Spring TransactionDefinition 常量），无则为 {@code null}\n\t * @see org.springframework.transaction.TransactionDefinition#ISOLATION_READ_UNCOMMITTED\n\t * @see org.springframework.transaction.TransactionDefinition#ISOLATION_READ_COMMITTED\n\t * @see org.springframework.transaction.TransactionDefinition#ISOLATION_REPEATABLE_READ\n\t * @see org.springframework.transaction.TransactionDefinition#ISOLATION_SERIALIZABLE\n\t * @see org.springframework.transaction.TransactionDefinition#getIsolationLevel()\n\t */",
        ),
        (
            "\t/**\n\t * Expose whether there currently is an actual transaction active.\n\t * Called by the transaction manager on transaction begin and on cleanup.\n\t * @param active {@code true} to mark the current context as being associated\n\t * with an actual transaction; {@code false} to reset that marker\n\t */",
            "\t/**\n\t * 暴露当前是否有实际活动事务。\n\t * 由事务管理器在事务开始和清理时调用。\n\t * @param active {@code true} 将当前上下文标记为关联实际事务；{@code false} 重置该标记\n\t */",
        ),
        (
            "\t/**\n\t * Return whether there currently is an actual transaction active.\n\t * This indicates whether the current context is associated with an actual\n\t * transaction rather than just with active transaction synchronization.\n\t * <p>To be called by resource management code that wants to differentiate\n\t * between active transaction synchronization (with or without a backing\n\t * resource transaction; also on PROPAGATION_SUPPORTS) and an actual\n\t * transaction being active (with a backing resource transaction;\n\t * on PROPAGATION_REQUIRED, PROPAGATION_REQUIRES_NEW, etc).\n\t * @see #isSynchronizationActive()\n\t */",
            "\t/**\n\t * 返回当前是否有实际活动事务。\n\t * 表示当前上下文是否关联实际事务，而非仅关联活动事务同步。\n\t * <p>供资源管理代码区分活动事务同步（有无底层资源事务；\n\t * 在 PROPAGATION_SUPPORTS 下也有）与实际活动事务（有底层资源事务；\n\t * 在 PROPAGATION_REQUIRED、PROPAGATION_REQUIRES_NEW 等下）。\n\t * @see #isSynchronizationActive()\n\t */",
        ),
        (
            "\t/**\n\t * Clear the entire transaction synchronization state:\n\t * registered synchronizations as well as the various transaction characteristics.\n\t * @see #clearSynchronization()\n\t * @see #setCurrentTransactionName\n\t * @see #setCurrentTransactionReadOnly\n\t * @see #setCurrentTransactionIsolationLevel\n\t * @see #setActualTransactionActive\n\t */",
            "\t/**\n\t * 清除整个事务同步状态：\n\t * 已注册同步以及各种事务特性。\n\t * @see #clearSynchronization()\n\t * @see #setCurrentTransactionName\n\t * @see #setCurrentTransactionReadOnly\n\t * @see #setCurrentTransactionIsolationLevel\n\t * @see #setActualTransactionActive\n\t */",
        ),
    ],
    "TransactionSynchronizationUtils.java": [
        (
            "/**\n * Utility methods for triggering specific {@link TransactionSynchronization}\n * callback methods on all currently registered synchronizations.\n *\n * @author Mark Paluch\n * @author Juergen Hoeller\n * @since 5.2\n * @see TransactionSynchronization\n * @see TransactionSynchronizationManager#getSynchronizations()\n */",
            "/**\n * 在所有当前已注册同步上触发特定 {@link TransactionSynchronization} 回调方法的工具类。\n *\n * @author Mark Paluch\n * @author Juergen Hoeller\n * @since 5.2\n * @see TransactionSynchronization\n * @see TransactionSynchronizationManager#getSynchronizations()\n */",
        ),
        (
            "\t/**\n\t * Unwrap the given resource handle if necessary; otherwise return\n\t * the given handle as-is.\n\t * @see InfrastructureProxy#getWrappedObject()\n\t */",
            "\t/**\n\t * 必要时解包给定资源句柄；否则原样返回。\n\t * @see InfrastructureProxy#getWrappedObject()\n\t */",
        ),
        (
            "\t\t// unwrap infrastructure proxy",
            "\t\t// 解包基础设施代理",
        ),
        (
            "\t\t\t// now unwrap scoped proxy",
            "\t\t\t// 再解包作用域代理",
        ),
        (
            "\t/**\n\t * Actually invoke the {@code triggerBeforeCommit} methods of the\n\t * given Spring TransactionSynchronization objects.\n\t * @param synchronizations a List of TransactionSynchronization objects\n\t * @see TransactionSynchronization#beforeCommit(boolean)\n\t */",
            "\t/**\n\t * 实际调用给定 Spring TransactionSynchronization 对象的 {@code triggerBeforeCommit} 方法。\n\t * @param synchronizations TransactionSynchronization 对象列表\n\t * @see TransactionSynchronization#beforeCommit(boolean)\n\t */",
        ),
        (
            "\t/**\n\t * Actually invoke the {@code beforeCompletion} methods of the\n\t * given Spring TransactionSynchronization objects.\n\t * @param synchronizations a List of TransactionSynchronization objects\n\t * @see TransactionSynchronization#beforeCompletion()\n\t */",
            "\t/**\n\t * 实际调用给定 Spring TransactionSynchronization 对象的 {@code beforeCompletion} 方法。\n\t * @param synchronizations TransactionSynchronization 对象列表\n\t * @see TransactionSynchronization#beforeCompletion()\n\t */",
        ),
        (
            "\t/**\n\t * Actually invoke the {@code afterCommit} methods of the\n\t * given Spring TransactionSynchronization objects.\n\t * @param synchronizations a List of TransactionSynchronization objects\n\t * @see TransactionSynchronization#afterCommit()\n\t */",
            "\t/**\n\t * 实际调用给定 Spring TransactionSynchronization 对象的 {@code afterCommit} 方法。\n\t * @param synchronizations TransactionSynchronization 对象列表\n\t * @see TransactionSynchronization#afterCommit()\n\t */",
        ),
        (
            "\t/**\n\t * Actually invoke the {@code afterCompletion} methods of the\n\t * given Spring TransactionSynchronization objects.\n\t * @param synchronizations a List of TransactionSynchronization objects\n\t * @param completionStatus the completion status according to the\n\t * constants in the TransactionSynchronization interface\n\t * @see TransactionSynchronization#afterCompletion(int)\n\t * @see TransactionSynchronization#STATUS_COMMITTED\n\t * @see TransactionSynchronization#STATUS_ROLLED_BACK\n\t * @see TransactionSynchronization#STATUS_UNKNOWN\n\t */",
            "\t/**\n\t * 实际调用给定 Spring TransactionSynchronization 对象的 {@code afterCompletion} 方法。\n\t * @param synchronizations TransactionSynchronization 对象列表\n\t * @param completionStatus 按 TransactionSynchronization 接口常量的完成状态\n\t * @see TransactionSynchronization#afterCompletion(int)\n\t * @see TransactionSynchronization#STATUS_COMMITTED\n\t * @see TransactionSynchronization#STATUS_ROLLED_BACK\n\t * @see TransactionSynchronization#STATUS_UNKNOWN\n\t */",
        ),
        (
            "\t/**\n\t * Inner class to avoid hard-coded dependency on AOP module.\n\t */",
            "\t/**\n\t * 内部类，避免对 AOP 模块的硬编码依赖。\n\t */",
        ),
    ],
    "TransactionalEventPublisher.java": [
        (
            "/**\n * A delegate for publishing transactional events in a reactive setup.\n * Includes the current Reactor-managed {@link TransactionContext} as\n * a source object for every {@link ApplicationEvent} to be published.\n *\n * <p>This delegate is just a convenience. The current {@link TransactionContext}\n * can be directly included as the event source as well, and then published\n * through an {@link ApplicationEventPublisher} such as the Spring\n * {@link org.springframework.context.ApplicationContext}:\n *\n * <pre class=\"code\">\n * TransactionContextManager.currentContext()\n *     .map(source -> new PayloadApplicationEvent&lt;&gt;(source, \"myPayload\"))\n *     .doOnSuccess(this.eventPublisher::publishEvent)\n * </pre>\n *\n * @author Juergen Hoeller\n * @since 6.1\n * @see #publishEvent(Function)\n * @see #publishEvent(Object)\n * @see ApplicationEventPublisher\n */",
            "/**\n * 在响应式环境中发布事务事件的委托类。\n * 将当前 Reactor 管理的 {@link TransactionContext} 作为\n * 每个待发布 {@link ApplicationEvent} 的源对象。\n *\n * <p>本委托仅为便利。当前 {@link TransactionContext} 也可直接作为事件源，\n * 然后通过 {@link ApplicationEventPublisher}（如 Spring\n * {@link org.springframework.context.ApplicationContext}）发布：\n *\n * <pre class=\"code\">\n * TransactionContextManager.currentContext()\n *     .map(source -> new PayloadApplicationEvent&lt;&gt;(source, \"myPayload\"))\n *     .doOnSuccess(this.eventPublisher::publishEvent)\n * </pre>\n *\n * @author Juergen Hoeller\n * @since 6.1\n * @see #publishEvent(Function)\n * @see #publishEvent(Object)\n * @see ApplicationEventPublisher\n */",
        ),
        (
            "\t/**\n\t * Create a new delegate for publishing transactional events in a reactive setup.\n\t * @param eventPublisher the actual event publisher to use,\n\t * typically a Spring {@link org.springframework.context.ApplicationContext}\n\t */",
            "\t/**\n\t * 创建用于在响应式环境中发布事务事件的新委托。\n\t * @param eventPublisher 实际使用的事件发布器，\n\t * 通常为 Spring {@link org.springframework.context.ApplicationContext}\n\t */",
        ),
        (
            "\t/**\n\t * Publish an event created through the given function which maps the transaction\n\t * source object (the {@link TransactionContext}) to the event instance.\n\t * @param eventCreationFunction a function mapping the source object to the event instance,\n\t * for example, {@code source -> new PayloadApplicationEvent&lt;&gt;(source, \"myPayload\")}\n\t * @return the Reactor {@link Mono} for the transactional event publication\n\t */",
            "\t/**\n\t * 发布由给定函数创建的事件，该函数将事务源对象（{@link TransactionContext}）映射为事件实例。\n\t * @param eventCreationFunction 将源对象映射为事件实例的函数，\n\t * 例如 {@code source -> new PayloadApplicationEvent&lt;&gt;(source, \"myPayload\")}\n\t * @return 事务事件发布的 Reactor {@link Mono}\n\t */",
        ),
        (
            "\t/**\n\t * Publish an event created for the given payload.\n\t * @param payload the payload to publish as an event\n\t * @return the Reactor {@link Mono} for the transactional event publication\n\t */",
            "\t/**\n\t * 发布为给定 payload 创建的事件。\n\t * @param payload 作为事件发布的 payload\n\t * @return 事务事件发布的 Reactor {@link Mono}\n\t */",
        ),
    ],
    "TransactionalOperator.java": [
        (
            "/**\n * Operator class that simplifies programmatic transaction demarcation and\n * transaction exception handling.\n *\n * <p>The central method is {@link #transactional}, supporting transactional wrapping\n * of functional sequences code that. This operator handles the transaction lifecycle\n * and possible exceptions such that neither the ReactiveTransactionCallback\n * implementation nor the calling code needs to explicitly handle transactions.\n *\n * <p>Typical usage: Allows for writing low-level data access objects that use\n * resources such as database connections but are not transaction-aware themselves.\n * Instead, they can implicitly participate in transactions handled by higher-level\n * application services utilizing this class, making calls to the low-level\n * services via an inner-class callback object.\n *\n * <p><strong>Note:</strong> Transactional Publishers should avoid Subscription\n * cancellation. See the\n * <a href=\"https://docs.spring.io/spring/docs/current/spring-framework-reference/data-access.html#tx-prog-operator-cancel\">Cancel Signals</a>\n * section of the Spring Framework reference for more details.\n *\n * @author Mark Paluch\n * @author Juergen Hoeller\n * @author Enric Sala\n * @since 5.2\n * @see #execute\n * @see ReactiveTransactionManager\n */",
            "/**\n * 简化编程式事务边界与事务异常处理的操作符类。\n *\n * <p>核心方法是 {@link #transactional}，支持对函数式序列代码进行事务包装。\n * 本操作符处理事务生命周期与可能的异常，\n * 使 ReactiveTransactionCallback 实现和调用代码均无需显式处理事务。\n *\n * <p>典型用法：编写使用数据库连接等资源但自身不感知事务的底层数据访问对象。\n * 它们可通过使用本类的高层应用服务处理的事务隐式参与，\n * 通过内部类回调对象调用底层服务。\n *\n * <p><strong>注意：</strong>事务 Publisher 应避免 Subscription 取消。\n * 详见 Spring Framework 参考中的\n * <a href=\"https://docs.spring.io/spring/docs/current/spring-framework-reference/data-access.html#tx-prog-operator-cancel\">Cancel Signals</a> 章节。\n *\n * @author Mark Paluch\n * @author Juergen Hoeller\n * @author Enric Sala\n * @since 5.2\n * @see #execute\n * @see ReactiveTransactionManager\n */",
        ),
        (
            "\t/**\n\t * Wrap the functional sequence specified by the given Flux within a transaction.\n\t * @param flux the Flux that should be executed within the transaction\n\t * @return a result publisher returned by the callback, or {@code null} if none\n\t * @throws TransactionException in case of initialization, rollback, or system errors\n\t * @throws RuntimeException if thrown by the TransactionCallback\n\t */",
            "\t/**\n\t * 将给定 Flux 指定的函数式序列包装在事务内。\n\t * @param flux 应在事务内执行的 Flux\n\t * @return 回调返回的结果 Publisher，无则为 {@code null}\n\t * @throws TransactionException 初始化、回滚或系统错误时\n\t * @throws RuntimeException 若 TransactionCallback 抛出\n\t */",
        ),
        (
            "\t/**\n\t * Wrap the functional sequence specified by the given Mono within a transaction.\n\t * @param mono the Mono that should be executed within the transaction\n\t * @return a result publisher returned by the callback\n\t * @throws TransactionException in case of initialization, rollback, or system errors\n\t * @throws RuntimeException if thrown by the TransactionCallback\n\t */",
            "\t/**\n\t * 将给定 Mono 指定的函数式序列包装在事务内。\n\t * @param mono 应在事务内执行的 Mono\n\t * @return 回调返回的结果 Publisher\n\t * @throws TransactionException 初始化、回滚或系统错误时\n\t * @throws RuntimeException 若 TransactionCallback 抛出\n\t */",
        ),
        (
            "\t/**\n\t * Execute the action specified by the given callback object within a transaction.\n\t * <p>Allows for returning a result object created within the transaction, that is,\n\t * a domain object or a collection of domain objects. A RuntimeException thrown\n\t * by the callback is treated as a fatal exception that enforces a rollback.\n\t * Such an exception gets propagated to the caller of the template.\n\t * @param action the callback object that specifies the transactional action\n\t * @return a result object returned by the callback\n\t * @throws TransactionException in case of initialization, rollback, or system errors\n\t * @throws RuntimeException if thrown by the TransactionCallback\n\t */",
            "\t/**\n\t * 在事务内执行给定回调对象指定的操作。\n\t * <p>允许返回事务内创建的结果对象，即领域对象或领域对象集合。\n\t * 回调抛出的 RuntimeException 视为强制回滚的致命异常，\n\t * 并传播给模板调用方。\n\t * @param action 指定事务操作的回调对象\n\t * @return 回调返回的结果对象\n\t * @throws TransactionException 初始化、回滚或系统错误时\n\t * @throws RuntimeException 若 TransactionCallback 抛出\n\t */",
        ),
        (
            "\t// Static builder methods",
            "\t// 静态构建方法",
        ),
        (
            "\t/**\n\t * Create a new {@link TransactionalOperator} using {@link ReactiveTransactionManager},\n\t * using a default transaction.\n\t * @param transactionManager the transaction management strategy to be used\n\t * @return the transactional operator\n\t */",
            "\t/**\n\t * 使用 {@link ReactiveTransactionManager} 创建新的 {@link TransactionalOperator}，\n\t * 使用默认事务。\n\t * @param transactionManager 要使用的事务管理策略\n\t * @return 事务操作符\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@link TransactionalOperator} using {@link ReactiveTransactionManager}\n\t * and {@link TransactionDefinition}.\n\t * @param transactionManager the transaction management strategy to be used\n\t * @param transactionDefinition the transaction definition to apply\n\t * @return the transactional operator\n\t */",
            "\t/**\n\t * 使用 {@link ReactiveTransactionManager} 和 {@link TransactionDefinition}\n\t * 创建新的 {@link TransactionalOperator}。\n\t * @param transactionManager 要使用的事务管理策略\n\t * @param transactionDefinition 要应用的事务定义\n\t * @return 事务操作符\n\t */",
        ),
    ],
    "TransactionalOperatorImpl.java": [
        (
            "/**\n * Operator class that simplifies programmatic transaction demarcation and\n * transaction exception handling.\n *\n * @author Mark Paluch\n * @author Juergen Hoeller\n * @author Enric Sala\n * @since 5.2\n * @see #execute\n * @see ReactiveTransactionManager\n */",
            "/**\n * 简化编程式事务边界与事务异常处理的操作符类。\n *\n * @author Mark Paluch\n * @author Juergen Hoeller\n * @author Enric Sala\n * @since 5.2\n * @see #execute\n * @see ReactiveTransactionManager\n */",
        ),
        (
            "\t/**\n\t * Construct a new TransactionTemplate using the given transaction manager,\n\t * taking its default settings from the given transaction definition.\n\t * @param transactionManager the transaction management strategy to be used\n\t * @param transactionDefinition the transaction definition to copy the\n\t * default settings from. Local properties can still be set to change values.\n\t */",
            "\t/**\n\t * 使用给定事务管理器构造新 TransactionTemplate，\n\t * 从给定事务定义复制默认设置。仍可设置本地属性以更改值。\n\t * @param transactionManager 要使用的事务管理策略\n\t * @param transactionDefinition 复制默认设置来源的事务定义\n\t */",
        ),
        (
            "\t/**\n\t * Return the transaction management strategy to be used.\n\t */",
            "\t/**\n\t * 返回要使用的事务管理策略。\n\t */",
        ),
        (
            "\t/**\n\t * Perform a rollback, handling rollback exceptions properly.\n\t * @param status object representing the transaction\n\t * @param ex the thrown application exception or error\n\t * @throws TransactionException in case of a rollback error\n\t */",
            "\t/**\n\t * 执行回滚，正确处理回滚异常。\n\t * @param status 表示事务的对象\n\t * @param ex 抛出的应用异常或错误\n\t * @throws TransactionException 回滚错误时\n\t */",
        ),
        (
            "\t/**\n\t * Unwrap the cause of a throwable, if produced by a failure\n\t * during the async resource cleanup in {@link Flux#usingWhen}.\n\t * @param ex the throwable to try to unwrap\n\t */",
            "\t/**\n\t * 若由 {@link Flux#usingWhen} 中异步资源清理失败产生，解包 throwable 的原因。\n\t * @param ex 要尝试解包的 throwable\n\t */",
        ),
    ],
}
