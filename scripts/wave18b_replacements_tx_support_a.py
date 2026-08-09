"""Chinese JavaDoc replacements for springframework wave18b tx.support [0:10]."""

TX_SUPPORT_A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "DelegatingTransactionDefinition.java": [
        (
            "/**\n * {@link TransactionDefinition} implementation that delegates all calls to a given target\n * {@link TransactionDefinition} instance. Abstract because it is meant to be subclassed,\n * with subclasses overriding specific methods that are not supposed to simply delegate\n * to the target instance.\n *\n * @author Juergen Hoeller\n * @since 3.0\n */",
            "/**\n * 将所有调用委托给给定目标 {@link TransactionDefinition} 实例的\n * {@link TransactionDefinition} 实现。抽象类，供子类继承；\n * 子类可覆盖不应简单委托给目标实例的特定方法。\n *\n * @author Juergen Hoeller\n * @since 3.0\n */",
        ),
        (
            "\t/**\n\t * Create a DelegatingTransactionAttribute for the given target attribute.\n\t * @param targetDefinition the target TransactionAttribute to delegate to\n\t */",
            "\t/**\n\t * 为给定目标属性创建 DelegatingTransactionAttribute。\n\t * @param targetDefinition 要委托的目标 TransactionAttribute\n\t */",
        ),
    ],
    "ResourceHolder.java": [
        (
            "/**\n * Generic interface to be implemented by resource holders.\n * Allows Spring's transaction infrastructure to introspect\n * and reset the holder when necessary.\n *\n * @author Juergen Hoeller\n * @since 2.5.5\n * @see ResourceHolderSupport\n * @see ResourceHolderSynchronization\n */",
            "/**\n * 资源持有者应实现的通用接口。\n * 允许 Spring 事务基础设施在必要时内省并重置持有者。\n *\n * @author Juergen Hoeller\n * @since 2.5.5\n * @see ResourceHolderSupport\n * @see ResourceHolderSynchronization\n */",
        ),
        (
            "\t/**\n\t * Reset the transactional state of this holder.\n\t */",
            "\t/**\n\t * 重置此持有者的事务状态。\n\t */",
        ),
        (
            "\t/**\n\t * Notify this holder that it has been unbound from transaction synchronization.\n\t */",
            "\t/**\n\t * 通知此持有者已从事务同步中解绑。\n\t */",
        ),
        (
            "\t/**\n\t * Determine whether this holder is considered as 'void',\n\t * i.e. as a leftover from a previous thread.\n\t */",
            "\t/**\n\t * 判断此持有者是否被视为「无效」，\n\t * 即是否为上一线程遗留的对象。\n\t */",
        ),
    ],
    "ResourceHolderSupport.java": [
        (
            "/**\n * Convenient base class for resource holders.\n *\n * <p>Features rollback-only support for participating transactions.\n * Can expire after a certain number of seconds or milliseconds\n * in order to determine a transactional timeout.\n *\n * @author Juergen Hoeller\n * @since 02.02.2004\n * @see org.springframework.jdbc.datasource.DataSourceTransactionManager#doBegin\n * @see org.springframework.jdbc.datasource.DataSourceUtils#applyTransactionTimeout\n */",
            "/**\n * 资源持有者的便捷基类。\n *\n * <p>支持参与事务的 rollback-only 标记。\n * 可在指定秒数或毫秒数后过期，用于判定事务超时。\n *\n * @author Juergen Hoeller\n * @since 02.02.2004\n * @see org.springframework.jdbc.datasource.DataSourceTransactionManager#doBegin\n * @see org.springframework.jdbc.datasource.DataSourceUtils#applyTransactionTimeout\n */",
        ),
        (
            "\t/**\n\t * Mark the resource as synchronized with a transaction.\n\t */",
            "\t/**\n\t * 将资源标记为已与事务同步。\n\t */",
        ),
        (
            "\t/**\n\t * Return whether the resource is synchronized with a transaction.\n\t */",
            "\t/**\n\t * 返回资源是否已与事务同步。\n\t */",
        ),
        (
            "\t/**\n\t * Mark the resource transaction as rollback-only.\n\t */",
            "\t/**\n\t * 将资源事务标记为 rollback-only。\n\t */",
        ),
        (
            "\t/**\n\t * Reset the rollback-only status for this resource transaction.\n\t * <p>Only really intended to be called after custom rollback steps which\n\t * keep the original resource in action, for example, in case of a savepoint.\n\t * @since 5.0\n\t * @see org.springframework.transaction.SavepointManager#rollbackToSavepoint\n\t */",
            "\t/**\n\t * 重置此资源事务的 rollback-only 状态。\n\t * <p>主要供保留原始资源继续使用的自定义回滚步骤之后调用，\n\t * 例如保存点场景。\n\t * @since 5.0\n\t * @see org.springframework.transaction.SavepointManager#rollbackToSavepoint\n\t */",
        ),
        (
            "\t/**\n\t * Return whether the resource transaction is marked as rollback-only.\n\t */",
            "\t/**\n\t * 返回资源事务是否已标记为 rollback-only。\n\t */",
        ),
        (
            "\t/**\n\t * Set the timeout for this object in seconds.\n\t * @param seconds number of seconds until expiration\n\t */",
            "\t/**\n\t * 以秒为单位设置此对象的超时时间。\n\t * @param seconds 距离过期的秒数\n\t */",
        ),
        (
            "\t/**\n\t * Set the timeout for this object in milliseconds.\n\t * @param millis number of milliseconds until expiration\n\t */",
            "\t/**\n\t * 以毫秒为单位设置此对象的超时时间。\n\t * @param millis 距离过期的毫秒数\n\t */",
        ),
        (
            "\t/**\n\t * Return whether this object has an associated timeout.\n\t */",
            "\t/**\n\t * 返回此对象是否关联了超时时间。\n\t */",
        ),
        (
            "\t/**\n\t * Return the expiration deadline of this object.\n\t * @return the deadline as Date object\n\t */",
            "\t/**\n\t * 返回此对象的过期截止时间。\n\t * @return 作为 Date 对象的截止时间\n\t */",
        ),
        (
            "\t/**\n\t * Return the time to live for this object in seconds.\n\t * Rounds up eagerly, for example, 9.00001 still to 10.\n\t * @return number of seconds until expiration\n\t * @throws TransactionTimedOutException if the deadline has already been reached\n\t */",
            "\t/**\n\t * 返回此对象的剩余存活时间（秒）。\n\t * 向上取整，例如 9.00001 仍取 10。\n\t * @return 距离过期的秒数\n\t * @throws TransactionTimedOutException 若截止时间已过\n\t */",
        ),
        (
            "\t/**\n\t * Return the time to live for this object in milliseconds.\n\t * @return number of milliseconds until expiration\n\t * @throws TransactionTimedOutException if the deadline has already been reached\n\t */",
            "\t/**\n\t * 返回此对象的剩余存活时间（毫秒）。\n\t * @return 距离过期的毫秒数\n\t * @throws TransactionTimedOutException 若截止时间已过\n\t */",
        ),
        (
            "\t/**\n\t * Set the transaction rollback-only if the deadline has been reached,\n\t * and throw a TransactionTimedOutException.\n\t */",
            "\t/**\n\t * 若截止时间已到，将事务标记为 rollback-only 并抛出 TransactionTimedOutException。\n\t */",
        ),
        (
            "\t/**\n\t * Increase the reference count by one because the holder has been requested\n\t * (i.e. someone requested the resource held by it).\n\t */",
            "\t/**\n\t * 因持有者被请求（即有人请求其持有的资源）而将引用计数加一。\n\t */",
        ),
        (
            "\t/**\n\t * Decrease the reference count by one because the holder has been released\n\t * (i.e. someone released the resource held by it).\n\t */",
            "\t/**\n\t * 因持有者被释放（即有人释放其持有的资源）而将引用计数减一。\n\t */",
        ),
        (
            "\t/**\n\t * Return whether there are still open references to this holder.\n\t */",
            "\t/**\n\t * 返回此持有者是否仍有未关闭的引用。\n\t */",
        ),
        (
            "\t/**\n\t * Clear the transactional state of this resource holder.\n\t */",
            "\t/**\n\t * 清除此资源持有者的事务状态。\n\t */",
        ),
        (
            "\t/**\n\t * Reset this resource holder - transactional state as well as reference count.\n\t */",
            "\t/**\n\t * 重置此资源持有者——包括事务状态与引用计数。\n\t */",
        ),
    ],
    "ResourceHolderSynchronization.java": [
        (
            "/**\n * {@link TransactionSynchronization} implementation that manages a\n * {@link ResourceHolder} bound through {@link TransactionSynchronizationManager}.\n *\n * @author Juergen Hoeller\n * @since 2.5.5\n * @param <H> the resource holder type\n * @param <K> the resource key type\n */",
            "/**\n * 通过 {@link TransactionSynchronizationManager} 管理已绑定\n * {@link ResourceHolder} 的 {@link TransactionSynchronization} 实现。\n *\n * @author Juergen Hoeller\n * @since 2.5.5\n * @param <H> 资源持有者类型\n * @param <K> 资源键类型\n */",
        ),
        (
            "\t/**\n\t * Create a new ResourceHolderSynchronization for the given holder.\n\t * @param resourceHolder the ResourceHolder to manage\n\t * @param resourceKey the key to bind the ResourceHolder for\n\t * @see TransactionSynchronizationManager#bindResource\n\t */",
            "\t/**\n\t * 为给定持有者创建新的 ResourceHolderSynchronization。\n\t * @param resourceHolder 要管理的 ResourceHolder\n\t * @param resourceKey 绑定 ResourceHolder 所用的键\n\t * @see TransactionSynchronizationManager#bindResource\n\t */",
        ),
        (
            "\t\t\t\t// The thread-bound resource holder might not be available anymore,\n\t\t\t\t// since afterCompletion might get called from a different thread.",
            "\t\t\t\t// 线程绑定的资源持有者可能已不可用，\n\t\t\t\t// 因为 afterCompletion 可能由不同线程调用。",
        ),
        (
            "\t\t\t// Probably a pre-bound resource...",
            "\t\t\t// 可能是预先绑定的资源……",
        ),
        (
            "\t/**\n\t * Return whether this holder should be unbound at completion\n\t * (or should rather be left bound to the thread after the transaction).\n\t * <p>The default implementation returns {@code true}.\n\t */",
            "\t/**\n\t * 返回此持有者是否应在完成时解绑\n\t * （或在事务结束后仍保留在线程上）。\n\t * <p>默认实现返回 {@code true}。\n\t */",
        ),
        (
            "\t/**\n\t * Return whether this holder's resource should be released before\n\t * transaction completion ({@code true}) or rather after\n\t * transaction completion ({@code false}).\n\t * <p>Note that resources will only be released when they are\n\t * unbound from the thread ({@link #shouldUnbindAtCompletion()}).\n\t * <p>The default implementation returns {@code true}.\n\t * @see #releaseResource\n\t */",
            "\t/**\n\t * 返回此持有者的资源是否应在事务完成前释放（{@code true}）\n\t * 或在事务完成后释放（{@code false}）。\n\t * <p>注意：仅当资源从线程解绑时才会释放\n\t * （{@link #shouldUnbindAtCompletion()}）。\n\t * <p>默认实现返回 {@code true}。\n\t * @see #releaseResource\n\t */",
        ),
        (
            "\t/**\n\t * Return whether this holder's resource should be released after\n\t * transaction completion ({@code true}).\n\t * <p>The default implementation returns {@code !shouldReleaseBeforeCompletion()},\n\t * releasing after completion if no attempt was made before completion.\n\t * @see #releaseResource\n\t */",
            "\t/**\n\t * 返回此持有者的资源是否应在事务完成后释放（{@code true}）。\n\t * <p>默认实现返回 {@code !shouldReleaseBeforeCompletion()}，\n\t * 若完成前未尝试释放则在完成后释放。\n\t * @see #releaseResource\n\t */",
        ),
        (
            "\t/**\n\t * Flush callback for the given resource holder.\n\t * @param resourceHolder the resource holder to flush\n\t */",
            "\t/**\n\t * 给定资源持有者的 flush 回调。\n\t * @param resourceHolder 要 flush 的资源持有者\n\t */",
        ),
        (
            "\t/**\n\t * After-commit callback for the given resource holder.\n\t * Only called when the resource hasn't been released yet\n\t * ({@link #shouldReleaseBeforeCompletion()}).\n\t * @param resourceHolder the resource holder to process\n\t */",
            "\t/**\n\t * 给定资源持有者的 after-commit 回调。\n\t * 仅在资源尚未释放时调用\n\t * （{@link #shouldReleaseBeforeCompletion()}）。\n\t * @param resourceHolder 要处理的资源持有者\n\t */",
        ),
        (
            "\t/**\n\t * Release the given resource (after it has been unbound from the thread).\n\t * @param resourceHolder the resource holder to process\n\t * @param resourceKey the key that the ResourceHolder was bound for\n\t */",
            "\t/**\n\t * 释放给定资源（在从线程解绑之后）。\n\t * @param resourceHolder 要处理的资源持有者\n\t * @param resourceKey ResourceHolder 绑定所用的键\n\t */",
        ),
        (
            "\t/**\n\t * Perform a cleanup on the given resource (which is left bound to the thread).\n\t * @param resourceHolder the resource holder to process\n\t * @param resourceKey the key that the ResourceHolder was bound for\n\t * @param committed whether the transaction has committed ({@code true})\n\t * or rolled back ({@code false})\n\t */",
            "\t/**\n\t * 对给定资源执行清理（资源仍保留在线程绑定中）。\n\t * @param resourceHolder 要处理的资源持有者\n\t * @param resourceKey ResourceHolder 绑定所用的键\n\t * @param committed 事务是否已提交（{@code true}）或已回滚（{@code false}）\n\t */",
        ),
    ],
    "ResourceTransactionDefinition.java": [
        (
            "/**\n * Extended variant of {@link TransactionDefinition}, indicating a resource transaction\n * and in particular whether the transactional resource is ready for local optimizations.\n *\n * @author Juergen Hoeller\n * @since 5.1\n * @see ResourceTransactionManager\n */",
            "/**\n * {@link TransactionDefinition} 的扩展变体，表示资源事务，\n * 并特别指出事务资源是否可进行本地优化。\n *\n * @author Juergen Hoeller\n * @since 5.1\n * @see ResourceTransactionManager\n */",
        ),
        (
            "\t/**\n\t * Determine whether the transactional resource is ready for local optimizations.\n\t * @return {@code true} if the resource is known to be entirely transaction-local,\n\t * not affecting any operations outside the scope of the current transaction\n\t * @see #isReadOnly()\n\t */",
            "\t/**\n\t * 判断事务资源是否可进行本地优化。\n\t * @return 若资源已知完全局限于当前事务、不影响事务范围外任何操作则返回 {@code true}\n\t * @see #isReadOnly()\n\t */",
        ),
    ],
    "ResourceTransactionManager.java": [
        (
            "/**\n * Extension of the {@link org.springframework.transaction.PlatformTransactionManager}\n * interface, indicating a native resource transaction manager, operating on a single\n * target resource. Such transaction managers differ from JTA transaction managers in\n * that they do not use XA transaction enlistment for an open number of resources but\n * rather focus on leveraging the native power and simplicity of a single target resource.\n *\n * <p>This interface is mainly used for abstract introspection of a transaction manager,\n * giving clients a hint on what kind of transaction manager they have been given\n * and on what concrete resource the transaction manager is operating on.\n *\n * @author Juergen Hoeller\n * @since 2.0.4\n * @see TransactionSynchronizationManager\n */",
            "/**\n * {@link org.springframework.transaction.PlatformTransactionManager} 接口的扩展，\n * 表示在单一目标资源上运行的原生资源事务管理器。\n * 此类事务管理器与 JTA 事务管理器的区别在于：\n * 不使用 XA 事务登记任意数量的资源，\n * 而是专注于利用单一目标资源的原生能力与简洁性。\n *\n * <p>此接口主要用于对事务管理器进行抽象内省，\n * 向客户端提示其获得的事务管理器类型\n * 以及事务管理器所操作的具体资源。\n *\n * @author Juergen Hoeller\n * @since 2.0.4\n * @see TransactionSynchronizationManager\n */",
        ),
        (
            "\t/**\n\t * Return the resource factory that this transaction manager operates on,\n\t * for example, a JDBC DataSource or a JMS ConnectionFactory.\n\t * <p>This target resource factory is usually used as resource key for\n\t * {@link TransactionSynchronizationManager}'s resource bindings per thread.\n\t * @return the target resource factory (never {@code null})\n\t * @see TransactionSynchronizationManager#bindResource\n\t * @see TransactionSynchronizationManager#getResource\n\t */",
            "\t/**\n\t * 返回此事务管理器所操作的资源工厂，\n\t * 例如 JDBC DataSource 或 JMS ConnectionFactory。\n\t * <p>该目标资源工厂通常用作\n\t * {@link TransactionSynchronizationManager} 按线程绑定资源的键。\n\t * @return 目标资源工厂（永不为 {@code null}）\n\t * @see TransactionSynchronizationManager#bindResource\n\t * @see TransactionSynchronizationManager#getResource\n\t */",
        ),
    ],
    "SimpleTransactionScope.java": [
        (
            "/**\n * A simple transaction-backed {@link Scope} implementation, delegating to\n * {@link TransactionSynchronizationManager}'s resource binding mechanism.\n *\n * <p><b>NOTE:</b> Like {@link org.springframework.context.support.SimpleThreadScope},\n * this transaction scope is not registered by default in common contexts. Instead,\n * you need to explicitly assign it to a scope key in your setup, either through\n * {@link org.springframework.beans.factory.config.ConfigurableBeanFactory#registerScope}\n * or through a {@link org.springframework.beans.factory.config.CustomScopeConfigurer} bean.\n *\n * @author Juergen Hoeller\n * @since 4.2\n * @see org.springframework.context.support.SimpleThreadScope\n * @see org.springframework.beans.factory.config.ConfigurableBeanFactory#registerScope\n * @see org.springframework.beans.factory.config.CustomScopeConfigurer\n */",
            "/**\n * 简单的基于事务的 {@link Scope} 实现，\n * 委托给 {@link TransactionSynchronizationManager} 的资源绑定机制。\n *\n * <p><b>注意：</b>与 {@link org.springframework.context.support.SimpleThreadScope} 类似，\n * 此事务作用域在常见上下文中默认未注册。\n * 需在配置中显式将其分配给某个作用域键，\n * 可通过 {@link org.springframework.beans.factory.config.ConfigurableBeanFactory#registerScope}\n * 或 {@link org.springframework.beans.factory.config.CustomScopeConfigurer} Bean 完成。\n *\n * @author Juergen Hoeller\n * @since 4.2\n * @see org.springframework.context.support.SimpleThreadScope\n * @see org.springframework.beans.factory.config.ConfigurableBeanFactory#registerScope\n * @see org.springframework.beans.factory.config.CustomScopeConfigurer\n */",
        ),
        (
            "\t\t// NOTE: Do NOT modify the following to use Map::computeIfAbsent. For details,\n\t\t// see https://github.com/spring-projects/spring-framework/issues/25801.",
            "\t\t// 注意：请勿将以下代码改为使用 Map::computeIfAbsent。详情见\n\t\t// https://github.com/spring-projects/spring-framework/issues/25801。",
        ),
        (
            "\t/**\n\t * Holder for scoped objects.\n\t */",
            "\t/**\n\t * 作用域对象的持有者。\n\t */",
        ),
    ],
    "SimpleTransactionStatus.java": [
        (
            "/**\n * A simple {@link org.springframework.transaction.TransactionStatus}\n * implementation. Derives from {@link AbstractTransactionStatus} and\n * adds an explicit {@link #isNewTransaction() \"newTransaction\"} flag.\n *\n * <p>This class is not used by any of Spring's pre-built\n * {@link org.springframework.transaction.PlatformTransactionManager}\n * implementations. It is mainly provided as a start for custom transaction\n * manager implementations and as a static mock for testing transactional\n * code (either as part of a mock {@code PlatformTransactionManager} or\n * as argument passed into a {@link TransactionCallback} to be tested).\n *\n * @author Juergen Hoeller\n * @since 1.2.3\n * @see TransactionCallback#doInTransaction\n */",
            "/**\n * 简单的 {@link org.springframework.transaction.TransactionStatus} 实现。\n * 继承 {@link AbstractTransactionStatus} 并添加显式的\n * {@link #isNewTransaction() \"newTransaction\"} 标志。\n *\n * <p>Spring 预置的 {@link org.springframework.transaction.PlatformTransactionManager}\n * 实现均未使用此类。它主要供自定义事务管理器实现起步，\n * 以及作为测试事务代码的静态模拟\n * （作为模拟 {@code PlatformTransactionManager} 的一部分，\n * 或作为传入待测 {@link TransactionCallback} 的参数）。\n *\n * @author Juergen Hoeller\n * @since 1.2.3\n * @see TransactionCallback#doInTransaction\n */",
        ),
        (
            "\t/**\n\t * Create a new {@code SimpleTransactionStatus} instance,\n\t * indicating a new transaction.\n\t */",
            "\t/**\n\t * 创建新的 {@code SimpleTransactionStatus} 实例，\n\t * 表示新事务。\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@code SimpleTransactionStatus} instance.\n\t * @param newTransaction whether to indicate a new transaction\n\t */",
            "\t/**\n\t * 创建新的 {@code SimpleTransactionStatus} 实例。\n\t * @param newTransaction 是否表示新事务\n\t */",
        ),
    ],
}
