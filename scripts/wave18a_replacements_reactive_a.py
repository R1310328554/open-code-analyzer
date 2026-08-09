"""Chinese JavaDoc replacements for springframework wave18a reactive tx [4:10]."""

REACTIVE_A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "GenericReactiveTransaction.java": [
        (
            "/**\n * Default implementation of the {@link ReactiveTransaction} interface,\n * used by {@link AbstractReactiveTransactionManager}. Based on the concept\n * of an underlying \"transaction object\".\n *\n * <p>Holds all status information that {@link AbstractReactiveTransactionManager}\n * needs internally, including a generic transaction object determined by the\n * concrete transaction manager implementation.\n *\n * <p><b>NOTE:</b> This is <i>not</i> intended for use with other ReactiveTransactionManager\n * implementations, in particular not for mock transaction managers in testing environments.\n *\n * @author Mark Paluch\n * @author Juergen Hoeller\n * @since 5.2\n * @see AbstractReactiveTransactionManager\n * @see #getTransaction\n */",
            "/**\n * {@link ReactiveTransaction} 接口的默认实现，\n * 由 {@link AbstractReactiveTransactionManager} 使用。基于底层 \"事务对象\" 概念。\n *\n * <p>持有 {@link AbstractReactiveTransactionManager} 内部所需的全部状态信息，\n * 包括由具体事务管理器实现确定的通用事务对象。\n *\n * <p><b>注意：</b>本类<i>不</i>供其他 ReactiveTransactionManager 实现使用，\n * 尤其不用于测试环境中的 mock 事务管理器。\n *\n * @author Mark Paluch\n * @author Juergen Hoeller\n * @since 5.2\n * @see AbstractReactiveTransactionManager\n * @see #getTransaction\n */",
        ),
        (
            "\t/**\n\t * Create a new {@code DefaultReactiveTransactionStatus} instance.\n\t * @param transactionName the defined name of the transaction\n\t * @param transaction underlying transaction object that can hold state\n\t * for the internal transaction implementation\n\t * @param newTransaction if the transaction is new, otherwise participating\n\t * in an existing transaction\n\t * @param newSynchronization if a new transaction synchronization has been\n\t * opened for the given transaction\n\t * @param readOnly whether the transaction is marked as read-only\n\t * @param debug should debug logging be enabled for the handling of this transaction?\n\t * Caching it in here can prevent repeated calls to ask the logging system whether\n\t * debug logging should be enabled.\n\t * @param suspendedResources a holder for resources that have been suspended\n\t * for this transaction, if any\n\t * @since 6.1\n\t */",
            "\t/**\n\t * 创建新的 {@code DefaultReactiveTransactionStatus} 实例。\n\t * @param transactionName 定义的事务名称\n\t * @param transaction 可为内部事务实现保存状态的底层事务对象\n\t * @param newTransaction 若为新事务则为 true，否则为参与现有事务\n\t * @param newSynchronization 若为给定事务开启了新事务同步则为 true\n\t * @param readOnly 事务是否标记为只读\n\t * @param debug 是否为本事务处理启用 debug 日志？\n\t * 在此缓存可避免反复查询日志系统是否启用 debug。\n\t * @param suspendedResources 为本事务挂起的资源的持有者（若有）\n\t * @since 6.1\n\t */",
        ),
        (
            "\t/**\n\t * Return the underlying transaction object.\n\t * @throws IllegalStateException if no transaction is active\n\t */",
            "\t/**\n\t * 返回底层事务对象。\n\t * @throws IllegalStateException 若无活动事务\n\t */",
        ),
        (
            "\t/**\n\t * Return if a new transaction synchronization has been opened for this transaction.\n\t */",
            "\t/**\n\t * 返回是否已为该事务开启新事务同步。\n\t */",
        ),
        (
            "\t/**\n\t * Return whether the progress of this transaction is debugged. This is used by\n\t * {@link AbstractReactiveTransactionManager} as an optimization, to prevent repeated\n\t * calls to {@code logger.isDebugEnabled()}. Not really intended for client code.\n\t */",
            "\t/**\n\t * 返回是否 debug 本事务进度。{@link AbstractReactiveTransactionManager} 用作优化，\n\t * 避免反复调用 {@code logger.isDebugEnabled()}。通常不供客户端代码使用。\n\t */",
        ),
        (
            "\t/**\n\t * Return the holder for resources that have been suspended for this transaction,\n\t * if any.\n\t */",
            "\t/**\n\t * 返回为本事务挂起的资源的持有者（若有）。\n\t */",
        ),
        (
            "\t/**\n\t * Determine the rollback-only flag via checking this ReactiveTransactionStatus.\n\t * <p>Will only return \"true\" if the application called {@code setRollbackOnly}\n\t * on this TransactionStatus object.\n\t */",
            "\t/**\n\t * 通过检查本 ReactiveTransactionStatus 确定 rollback-only 标志。\n\t * <p>仅当应用在本 TransactionStatus 对象上调用 {@code setRollbackOnly} 时才返回 \"true\"。\n\t */",
        ),
        (
            "\t/**\n\t * Mark this transaction as completed, that is, committed or rolled back.\n\t */",
            "\t/**\n\t * 将本事务标记为已完成，即已提交或已回滚。\n\t */",
        ),
    ],
    "ReactiveResourceSynchronization.java": [
        (
            "/**\n * {@link TransactionSynchronization} implementation that manages a\n * resource object bound through {@link TransactionSynchronizationManager}.\n *\n * @author Mark Paluch\n * @author Juergen Hoeller\n * @since 5.2\n * @param <O> the resource holder type\n * @param <K> the resource key type\n */",
            "/**\n * 管理通过 {@link TransactionSynchronizationManager} 绑定的资源对象的\n * {@link TransactionSynchronization} 实现。\n *\n * @author Mark Paluch\n * @author Juergen Hoeller\n * @since 5.2\n * @param <O> 资源持有者类型\n * @param <K> 资源键类型\n */",
        ),
        (
            "\t/**\n\t * Create a new ReactiveResourceSynchronization for the given holder.\n\t * @param resourceObject the resource object to manage\n\t * @param resourceKey the key to bind the resource object for\n\t * @param synchronizationManager the synchronization manager bound to the current transaction\n\t * @see TransactionSynchronizationManager#bindResource\n\t */",
            "\t/**\n\t * 为给定持有者创建新的 ReactiveResourceSynchronization。\n\t * @param resourceObject 要管理的资源对象\n\t * @param resourceKey 绑定资源对象的键\n\t * @param synchronizationManager 绑定到当前事务的同步管理器\n\t * @see TransactionSynchronizationManager#bindResource\n\t */",
        ),
        (
            "\t\t\t\t\t// The thread-bound resource holder might not be available anymore,\n\t\t\t\t\t// since afterCompletion might get called from a different thread.",
            "\t\t\t\t\t// 线程绑定的资源持有者可能已不可用，\n\t\t\t\t\t// 因为 afterCompletion 可能从不同线程调用。",
        ),
        (
            "\t\t\t\t// Probably a pre-bound resource...",
            "\t\t\t\t// 可能是预绑定的资源...",
        ),
        (
            "\t/**\n\t * Return whether this holder should be unbound at completion\n\t * (or should rather be left bound to the thread after the transaction).\n\t * <p>The default implementation returns {@code true}.\n\t */",
            "\t/**\n\t * 返回本持有者是否应在完成时解绑\n\t * （或事务后仍保留绑定到线程）。\n\t * <p>默认实现返回 {@code true}。\n\t */",
        ),
        (
            "\t/**\n\t * Return whether this holder's resource should be released before\n\t * transaction completion ({@code true}) or rather after\n\t * transaction completion ({@code false}).\n\t * <p>Note that resources will only be released when they are\n\t * unbound from the thread ({@link #shouldUnbindAtCompletion()}).\n\t * <p>The default implementation returns {@code true}.\n\t * @see #releaseResource\n\t */",
            "\t/**\n\t * 返回本持有者的资源是否应在事务完成前释放（{@code true}）\n\t * 或在事务完成后释放（{@code false}）。\n\t * <p>注意，资源仅在与线程解绑时（{@link #shouldUnbindAtCompletion()}）才会释放。\n\t * <p>默认实现返回 {@code true}。\n\t * @see #releaseResource\n\t */",
        ),
        (
            "\t/**\n\t * Return whether this holder's resource should be released after\n\t * transaction completion ({@code true}).\n\t * <p>The default implementation returns {@code !shouldReleaseBeforeCompletion()},\n\t * releasing after completion if no attempt was made before completion.\n\t * @see #releaseResource\n\t */",
            "\t/**\n\t * 返回本持有者的资源是否应在事务完成后释放（{@code true}）。\n\t * <p>默认实现返回 {@code !shouldReleaseBeforeCompletion()}，\n\t * 若完成前未尝试释放则在完成后释放。\n\t * @see #releaseResource\n\t */",
        ),
        (
            "\t/**\n\t * After-commit callback for the given resource holder.\n\t * Only called when the resource hasn't been released yet\n\t * ({@link #shouldReleaseBeforeCompletion()}).\n\t * @param resourceHolder the resource holder to process\n\t */",
            "\t/**\n\t * 给定资源持有者的提交后回调。\n\t * 仅在资源尚未释放时调用（{@link #shouldReleaseBeforeCompletion()}）。\n\t * @param resourceHolder 要处理的资源持有者\n\t */",
        ),
        (
            "\t/**\n\t * Release the given resource (after it has been unbound from the thread).\n\t * @param resourceHolder the resource holder to process\n\t * @param resourceKey the key that the resource object was bound for\n\t */",
            "\t/**\n\t * 释放给定资源（在与线程解绑之后）。\n\t * @param resourceHolder 要处理的资源持有者\n\t * @param resourceKey 资源对象绑定的键\n\t */",
        ),
        (
            "\t/**\n\t * Perform a cleanup on the given resource (which is left bound to the thread).\n\t * @param resourceHolder the resource holder to process\n\t * @param resourceKey the key that the resource object was bound for\n\t * @param committed whether the transaction has committed ({@code true})\n\t * or rolled back ({@code false})\n\t */",
            "\t/**\n\t * 对给定资源执行清理（资源仍绑定到线程）。\n\t * @param resourceHolder 要处理的资源持有者\n\t * @param resourceKey 资源对象绑定的键\n\t * @param committed 事务是否已提交（{@code true}）或已回滚（{@code false}）\n\t */",
        ),
    ],
    "TransactionCallback.java": [
        (
            "/**\n * Callback interface for reactive transactional code. Used with {@link TransactionalOperator}'s\n * {@code execute} method, often as anonymous class within a method implementation.\n *\n * <p>Typically used to assemble various calls to transaction-unaware data access\n * services into a higher-level service method with transaction demarcation. As an\n * alternative, consider the use of declarative transaction demarcation (for example, through\n * Spring's {@link org.springframework.transaction.annotation.Transactional} annotation).\n *\n * @author Mark Paluch\n * @author Juergen Hoeller\n * @since 5.2\n * @param <T> the result type\n * @see TransactionalOperator\n */",
            "/**\n * 响应式事务代码的回调接口。与 {@link TransactionalOperator} 的\n * {@code execute} 方法配合使用，常在方法实现中作为匿名类。\n *\n * <p>通常用于将多个对事务无感知的数据访问服务调用\n * 组装到带事务边界的高层服务方法中。也可考虑声明式事务边界\n * （例如通过 Spring 的 {@link org.springframework.transaction.annotation.Transactional} 注解）。\n *\n * @author Mark Paluch\n * @author Juergen Hoeller\n * @since 5.2\n * @param <T> 结果类型\n * @see TransactionalOperator\n */",
        ),
        (
            "\t/**\n\t * Gets called by {@link TransactionalOperator} within a transactional context.\n\t * Does not need to care about transactions itself, although it can retrieve and\n\t * influence the status of the current transaction via the given status object,\n\t * for example, setting rollback-only.\n\t * @param status associated transaction status\n\t * @return a result publisher\n\t * @see TransactionalOperator#transactional\n\t */",
            "\t/**\n\t * 在事务上下文中由 {@link TransactionalOperator} 调用。\n\t * 无需自行管理事务，但可通过给定状态对象获取并影响当前事务状态，\n\t * 例如设置 rollback-only。\n\t * @param status 关联的事务状态\n\t * @return 结果 Publisher\n\t * @see TransactionalOperator#transactional\n\t */",
        ),
    ],
    "TransactionContext.java": [
        (
            "/**\n * Mutable transaction context that encapsulates transactional synchronizations and\n * resources in the scope of a single transaction. Transaction context is typically\n * held by an outer {@link TransactionContextHolder} or referenced directly within\n * from the subscriber context.\n *\n * @author Mark Paluch\n * @author Juergen Hoeller\n * @since 5.2\n * @see TransactionContextManager\n * @see reactor.util.context.Context\n */",
            "/**\n * 可变事务上下文，封装单个事务范围内的同步与资源。\n * 事务上下文通常由外层 {@link TransactionContextHolder} 持有，\n * 或直接在订阅者上下文中引用。\n *\n * @author Mark Paluch\n * @author Juergen Hoeller\n * @since 5.2\n * @see TransactionContextManager\n * @see reactor.util.context.Context\n */",
        ),
    ],
    "TransactionContextHolder.java": [
        (
            "/**\n * Mutable holder for reactive transaction {@link TransactionContext contexts}.\n * This holder keeps references to individual {@link TransactionContext}s.\n *\n * @author Mark Paluch\n * @author Juergen Hoeller\n * @since 5.2\n * @see TransactionContext\n */",
            "/**\n * 响应式事务 {@link TransactionContext 上下文} 的可变持有者。\n * 本持有者保存对各个 {@link TransactionContext} 的引用。\n *\n * @author Mark Paluch\n * @author Juergen Hoeller\n * @since 5.2\n * @see TransactionContext\n */",
        ),
        (
            "\t/**\n\t * Return the current {@link TransactionContext}.\n\t * @throws NoTransactionException if no transaction is ongoing\n\t */",
            "\t/**\n\t * 返回当前 {@link TransactionContext}。\n\t * @throws NoTransactionException 若无进行中的事务\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@link TransactionContext}.\n\t */",
            "\t/**\n\t * 创建新的 {@link TransactionContext}。\n\t */",
        ),
        (
            "\t/**\n\t * Check whether the holder has a {@link TransactionContext}.\n\t * @return {@literal true} if a {@link TransactionContext} is associated\n\t */",
            "\t/**\n\t * 检查持有者是否有关联的 {@link TransactionContext}。\n\t * @return 若有关联的 {@link TransactionContext} 则为 {@literal true}\n\t */",
        ),
    ],
    "TransactionContextManager.java": [
        (
            "/**\n * Delegate to register and obtain transactional contexts.\n *\n * <p>Typically used by components that intercept or orchestrate transactional flows\n * such as AOP interceptors or transactional operators.\n *\n * @author Mark Paluch\n * @since 5.2\n * @see TransactionSynchronization\n */",
            "/**\n * 注册和获取事务上下文的委托类。\n *\n * <p>通常由拦截或编排事务流的组件使用，\n * 如 AOP 拦截器或事务操作符。\n *\n * @author Mark Paluch\n * @since 5.2\n * @see TransactionSynchronization\n */",
        ),
        (
            "\t/**\n\t * Obtain the current {@link TransactionContext} from the subscriber context or the\n\t * transactional context holder. Context retrieval fails with NoTransactionException\n\t * if no context or context holder is registered.\n\t * @return the current {@link TransactionContext}\n\t * @throws NoTransactionException if no TransactionContext was found in the\n\t * subscriber context or no context found in a holder\n\t */",
            "\t/**\n\t * 从订阅者上下文或事务上下文持有者获取当前 {@link TransactionContext}。\n\t * 若未注册上下文或上下文持有者，获取将失败并抛出 NoTransactionException。\n\t * @return 当前 {@link TransactionContext}\n\t * @throws NoTransactionException 若订阅者上下文中未找到 TransactionContext\n\t * 或持有者中无上下文\n\t */",
        ),
        (
            "\t/**\n\t * Create a {@link TransactionContext} and register it in the subscriber {@link Context}.\n\t * @return functional context registration.\n\t * @throws IllegalStateException if a transaction context is already associated.\n\t * @see Mono#contextWrite(Function)\n\t * @see Flux#contextWrite(Function)\n\t */",
            "\t/**\n\t * 创建 {@link TransactionContext} 并在订阅者 {@link Context} 中注册。\n\t * @return 函数式上下文注册。\n\t * @throws IllegalStateException 若已关联事务上下文。\n\t * @see Mono#contextWrite(Function)\n\t * @see Flux#contextWrite(Function)\n\t */",
        ),
        (
            "\t/**\n\t * Return a {@link Function} to create or associate a new {@link TransactionContext}.\n\t * Interaction with transactional resources through\n\t * {@link TransactionSynchronizationManager} requires a TransactionContext\n\t * to be registered in the subscriber context.\n\t * @return functional context registration.\n\t */",
            "\t/**\n\t * 返回用于创建或关联新 {@link TransactionContext} 的 {@link Function}。\n\t * 通过 {@link TransactionSynchronizationManager} 与事务资源交互\n\t * 需要在订阅者上下文中注册 TransactionContext。\n\t * @return 函数式上下文注册。\n\t */",
        ),
        (
            "\t/**\n\t * Return a {@link Function} to create or associate a new\n\t * {@link TransactionContextHolder}. Creation and release of transactions\n\t * within a reactive flow requires a mutable holder that follows a top to\n\t * down execution scheme. Reactor's subscriber context follows a down to top\n\t * approach regarding mutation visibility.\n\t * @return functional context registration.\n\t */",
            "\t/**\n\t * 返回用于创建或关联新 {@link TransactionContextHolder} 的 {@link Function}。\n\t * 响应式流内事务的创建与释放需要遵循自上而下执行方案的可变持有者。\n\t * Reactor 订阅者上下文在变更可见性上采用自下而上方式。\n\t * @return 函数式上下文注册。\n\t */",
        ),
        (
            "\t/**\n\t * Stackless variant of {@link NoTransactionException} for reactive flows.\n\t */",
            "\t/**\n\t * 用于响应式流的 {@link NoTransactionException} 无堆栈变体。\n\t */",
        ),
        (
            "\t\t\t// stackless exception",
            "\t\t\t// 无堆栈异常",
        ),
    ],
}
