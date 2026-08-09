"""Chinese JavaDoc replacements for springframework wave18b TransactionSynchronization*."""

TX_SYNC_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "TransactionSynchronization.java": [
        (
            "/**\n * Interface for transaction synchronization callbacks.\n * Supported by AbstractPlatformTransactionManager.\n *\n * <p>TransactionSynchronization implementations can implement the Ordered interface\n * to influence their execution order. A synchronization that does not implement the\n * Ordered interface is appended to the end of the synchronization chain.\n *\n * <p>System synchronizations performed by Spring itself use specific order values,\n * allowing for fine-grained interaction with their execution order (if necessary).\n *\n * <p>Implements the {@link Ordered} interface to enable the execution order of\n * synchronizations to be controlled declaratively. The default {@link #getOrder()\n * order} is {@link Ordered#LOWEST_PRECEDENCE}, indicating late execution; return\n * a lower value for earlier execution.\n *\n * @author Juergen Hoeller\n * @since 02.06.2003\n * @see TransactionSynchronizationManager\n * @see AbstractPlatformTransactionManager\n * @see org.springframework.jdbc.datasource.DataSourceUtils#CONNECTION_SYNCHRONIZATION_ORDER\n */",
            "/**\n * 事务同步回调接口。\n * 由 AbstractPlatformTransactionManager 支持。\n *\n * <p>TransactionSynchronization 实现可实现 Ordered 接口\n * 以影响执行顺序。未实现 Ordered 的同步会追加到同步链末尾。\n *\n * <p>Spring 自身执行的系统同步使用特定顺序值，\n * 必要时可精细控制其执行顺序。\n *\n * <p>实现 {@link Ordered} 接口，以便声明式控制同步执行顺序。\n * 默认 {@link #getOrder() order} 为 {@link Ordered#LOWEST_PRECEDENCE}，\n * 表示较晚执行；返回更小值可更早执行。\n *\n * @author Juergen Hoeller\n * @since 02.06.2003\n * @see TransactionSynchronizationManager\n * @see AbstractPlatformTransactionManager\n * @see org.springframework.jdbc.datasource.DataSourceUtils#CONNECTION_SYNCHRONIZATION_ORDER\n */",
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
            "\t/**\n\t * Return the execution order for this transaction synchronization.\n\t * <p>Default is {@link Ordered#LOWEST_PRECEDENCE}.\n\t */",
            "\t/**\n\t * 返回此事务同步的执行顺序。\n\t * <p>默认为 {@link Ordered#LOWEST_PRECEDENCE}。\n\t */",
        ),
        (
            "\t/**\n\t * Suspend this synchronization.\n\t * Supposed to unbind resources from TransactionSynchronizationManager if managing any.\n\t * @see TransactionSynchronizationManager#unbindResource\n\t */",
            "\t/**\n\t * 挂起此同步。\n\t * 若管理资源，应将其从 TransactionSynchronizationManager 解绑。\n\t * @see TransactionSynchronizationManager#unbindResource\n\t */",
        ),
        (
            "\t/**\n\t * Resume this synchronization.\n\t * Supposed to rebind resources to TransactionSynchronizationManager if managing any.\n\t * @see TransactionSynchronizationManager#bindResource\n\t */",
            "\t/**\n\t * 恢复此同步。\n\t * 若管理资源，应将其重新绑定到 TransactionSynchronizationManager。\n\t * @see TransactionSynchronizationManager#bindResource\n\t */",
        ),
        (
            "\t/**\n\t * Flush the underlying session to the datastore, if applicable:\n\t * for example, a Hibernate/JPA session.\n\t * @see org.springframework.transaction.TransactionStatus#flush()\n\t */",
            "\t/**\n\t * 若适用，将底层 Session flush 到数据存储：\n\t * 例如 Hibernate/JPA Session。\n\t * @see org.springframework.transaction.TransactionStatus#flush()\n\t */",
        ),
        (
            "\t/**\n\t * Invoked on creation of a new savepoint, either when a nested transaction\n\t * is started against an existing transaction or on a programmatic savepoint\n\t * via {@link org.springframework.transaction.TransactionStatus}.\n\t * <p>This synchronization callback is invoked right <i>after</i> the creation\n\t * of the resource savepoint, with the given savepoint object already active.\n\t * @param savepoint the associated savepoint object (primarily as a key for\n\t * identifying the savepoint but also castable to the resource savepoint type)\n\t * @since 6.2\n\t * @see org.springframework.transaction.SavepointManager#createSavepoint\n\t * @see org.springframework.transaction.TransactionDefinition#PROPAGATION_NESTED\n\t */",
            "\t/**\n\t * 在创建新保存点时调用，\n\t * 既可能是在现有事务上启动嵌套事务，\n\t * 也可能是通过 {@link org.springframework.transaction.TransactionStatus} 编程式创建保存点。\n\t * <p>此同步回调在资源保存点创建<i>之后</i>立即调用，\n\t * 此时给定保存点对象已生效。\n\t * @param savepoint 关联的保存点对象（主要用作标识保存点的键，\n\t * 也可转型为资源保存点类型）\n\t * @since 6.2\n\t * @see org.springframework.transaction.SavepointManager#createSavepoint\n\t * @see org.springframework.transaction.TransactionDefinition#PROPAGATION_NESTED\n\t */",
        ),
        (
            "\t/**\n\t * Invoked in case of a rollback to the previously created savepoint.\n\t * <p>This synchronization callback is invoked right <i>before</i> the rollback\n\t * of the resource savepoint, with the given savepoint object still active.\n\t * @param savepoint the associated savepoint object (primarily as a key for\n\t * identifying the savepoint but also castable to the resource savepoint type)\n\t * @since 6.2\n\t * @see #savepoint\n\t * @see org.springframework.transaction.SavepointManager#rollbackToSavepoint\n\t */",
            "\t/**\n\t * 回滚到先前创建的保存点时调用。\n\t * <p>此同步回调在资源保存点回滚<i>之前</i>立即调用，\n\t * 此时给定保存点对象仍有效。\n\t * @param savepoint 关联的保存点对象（主要用作标识保存点的键，\n\t * 也可转型为资源保存点类型）\n\t * @since 6.2\n\t * @see #savepoint\n\t * @see org.springframework.transaction.SavepointManager#rollbackToSavepoint\n\t */",
        ),
        (
            "\t/**\n\t * Invoked before transaction commit (before \"beforeCompletion\").\n\t * Can, for example, flush transactional O/R Mapping sessions to the database.\n\t * <p>This callback does <i>not</i> mean that the transaction will actually be committed.\n\t * A rollback decision can still occur after this method has been called. This callback\n\t * is rather meant to perform work that's only relevant if a commit still has a chance\n\t * to happen, such as flushing SQL statements to the database.\n\t * <p>Note that exceptions will get propagated to the commit caller and cause a\n\t * rollback of the transaction.\n\t * @param readOnly whether the transaction is defined as read-only transaction\n\t * @throws RuntimeException in case of errors; will be <b>propagated to the caller</b>\n\t * (note: do not throw TransactionException subclasses here!)\n\t * @see #beforeCompletion\n\t */",
            "\t/**\n\t * 在事务提交前调用（在 \"beforeCompletion\" 之前）。\n\t * 例如可将事务性 O/R Mapping Session flush 到数据库。\n\t * <p>此回调<i>并不</i>表示事务一定会提交。\n\t * 调用此方法后仍可能决定回滚。此回调旨在执行\n\t * 仅在仍有可能提交时才有意义的工作，\n\t * 例如将 SQL 语句 flush 到数据库。\n\t * <p>注意：异常会传播给提交调用方并导致事务回滚。\n\t * @param readOnly 事务是否定义为只读事务\n\t * @throws RuntimeException 出错时；将<b>传播给调用方</b>\n\t * （注意：此处不要抛出 TransactionException 子类！）\n\t * @see #beforeCompletion\n\t */",
        ),
        (
            "\t/**\n\t * Invoked before transaction commit/rollback.\n\t * Can perform resource cleanup <i>before</i> transaction completion.\n\t * <p>This method will be invoked after {@code beforeCommit}, even when\n\t * {@code beforeCommit} threw an exception. This callback allows for\n\t * closing resources before transaction completion, for any outcome.\n\t * @throws RuntimeException in case of errors; will be <b>logged but not propagated</b>\n\t * (note: do not throw TransactionException subclasses here!)\n\t * @see #beforeCommit\n\t * @see #afterCompletion\n\t */",
            "\t/**\n\t * 在事务提交/回滚前调用。\n\t * 可在事务完成<i>之前</i>执行资源清理。\n\t * <p>即使 {@code beforeCommit} 抛出异常，\n\t * 此方法仍会在其之后调用。此回调允许在事务完成前\n\t * 关闭资源，无论最终结果如何。\n\t * @throws RuntimeException 出错时；将<b>记录日志但不传播</b>\n\t * （注意：此处不要抛出 TransactionException 子类！）\n\t * @see #beforeCommit\n\t * @see #afterCompletion\n\t */",
        ),
        (
            "\t/**\n\t * Invoked after transaction commit. Can perform further operations right\n\t * <i>after</i> the main transaction has <i>successfully</i> committed.\n\t * <p>Can, for example, commit further operations that are supposed to follow on a successful\n\t * commit of the main transaction, like confirmation messages or emails.\n\t * <p><b>NOTE:</b> The transaction will have been committed already, but the\n\t * transactional resources might still be active and accessible. As a consequence,\n\t * any data access code triggered at this point will still \"participate\" in the\n\t * original transaction, allowing to perform some cleanup (with no commit following\n\t * anymore!), unless it explicitly declares that it needs to run in a separate\n\t * transaction. Hence: <b>Use {@code PROPAGATION_REQUIRES_NEW} for any\n\t * transactional operation that is called from here.</b>\n\t * @throws RuntimeException in case of errors; will be <b>propagated to the caller</b>\n\t * (note: do not throw TransactionException subclasses here!)\n\t */",
            "\t/**\n\t * 在事务提交后调用。可在主事务<i>成功</i>提交<i>之后</i>立即执行进一步操作。\n\t * <p>例如可提交主事务成功提交后应执行的后续操作，\n\t * 如确认消息或邮件。\n\t * <p><b>注意：</b>事务已提交，但事务资源可能仍活跃且可访问。\n\t * 因此，此时触发的任何数据访问代码仍会「参与」原始事务，\n\t * 允许执行一些清理（之后不再有提交！），\n\t * 除非它显式声明需要在独立事务中运行。\n\t * 因此：<b>从此处调用的任何事务操作请使用 {@code PROPAGATION_REQUIRES_NEW}。</b>\n\t * @throws RuntimeException 出错时；将<b>传播给调用方</b>\n\t * （注意：此处不要抛出 TransactionException 子类！）\n\t */",
        ),
        (
            "\t/**\n\t * Invoked after transaction commit/rollback.\n\t * Can perform resource cleanup <i>after</i> transaction completion.\n\t * <p><b>NOTE:</b> The transaction will have been committed or rolled back already,\n\t * but the transactional resources might still be active and accessible. As a\n\t * consequence, any data access code triggered at this point will still \"participate\"\n\t * in the original transaction, allowing to perform some cleanup (with no commit\n\t * following anymore!), unless it explicitly declares that it needs to run in a\n\t * separate transaction. Hence: <b>Use {@code PROPAGATION_REQUIRES_NEW}\n\t * for any transactional operation that is called from here.</b>\n\t * @param status completion status according to the {@code STATUS_*} constants\n\t * @throws RuntimeException in case of errors; will be <b>logged but not propagated</b>\n\t * (note: do not throw TransactionException subclasses here!)\n\t * @see #STATUS_COMMITTED\n\t * @see #STATUS_ROLLED_BACK\n\t * @see #STATUS_UNKNOWN\n\t * @see #beforeCompletion\n\t */",
            "\t/**\n\t * 在事务提交/回滚后调用。\n\t * 可在事务完成<i>之后</i>执行资源清理。\n\t * <p><b>注意：</b>事务已提交或回滚，但事务资源可能仍活跃且可访问。\n\t * 因此，此时触发的任何数据访问代码仍会「参与」原始事务，\n\t * 允许执行一些清理（之后不再有提交！），\n\t * 除非它显式声明需要在独立事务中运行。\n\t * 因此：<b>从此处调用的任何事务操作请使用 {@code PROPAGATION_REQUIRES_NEW}。</b>\n\t * @param status 根据 {@code STATUS_*} 常量表示的完成状态\n\t * @throws RuntimeException 出错时；将<b>记录日志但不传播</b>\n\t * （注意：此处不要抛出 TransactionException 子类！）\n\t * @see #STATUS_COMMITTED\n\t * @see #STATUS_ROLLED_BACK\n\t * @see #STATUS_UNKNOWN\n\t * @see #beforeCompletion\n\t */",
        ),
    ],
    "TransactionSynchronizationUtils.java": [
        (
            "/**\n * Utility methods for triggering specific {@link TransactionSynchronization}\n * callback methods on all currently registered synchronizations.\n *\n * @author Juergen Hoeller\n * @since 2.0\n * @see TransactionSynchronization\n * @see TransactionSynchronizationManager#getSynchronizations()\n */",
            "/**\n * 用于在所有当前已注册同步上触发特定 {@link TransactionSynchronization}\n * 回调方法的工具方法。\n *\n * @author Juergen Hoeller\n * @since 2.0\n * @see TransactionSynchronization\n * @see TransactionSynchronizationManager#getSynchronizations()\n */",
        ),
        (
            "\t/**\n\t * Check whether the given resource transaction manager refers to the given\n\t * (underlying) resource factory.\n\t * @see ResourceTransactionManager#getResourceFactory()\n\t * @see InfrastructureProxy#getWrappedObject()\n\t */",
            "\t/**\n\t * 检查给定资源事务管理器是否指向给定（底层）资源工厂。\n\t * @see ResourceTransactionManager#getResourceFactory()\n\t * @see InfrastructureProxy#getWrappedObject()\n\t */",
        ),
        (
            "\t/**\n\t * Unwrap the given resource handle if necessary; otherwise return\n\t * the given handle as-is.\n\t * @since 5.3.4\n\t * @see InfrastructureProxy#getWrappedObject()\n\t */",
            "\t/**\n\t * 必要时解包给定资源句柄；否则原样返回。\n\t * @since 5.3.4\n\t * @see InfrastructureProxy#getWrappedObject()\n\t */",
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
            "\t/**\n\t * Trigger {@code flush} callbacks on all currently registered synchronizations.\n\t * @throws RuntimeException if thrown by a {@code flush} callback\n\t * @see TransactionSynchronization#flush()\n\t */",
            "\t/**\n\t * 在所有当前已注册同步上触发 {@code flush} 回调。\n\t * @throws RuntimeException 若 {@code flush} 回调抛出\n\t * @see TransactionSynchronization#flush()\n\t */",
        ),
        (
            "\t/**\n\t * Trigger {@code flush} callbacks on all currently registered synchronizations.\n\t * @throws RuntimeException if thrown by a {@code savepoint} callback\n\t * @since 6.2\n\t * @see TransactionSynchronization#savepoint\n\t */",
            "\t/**\n\t * 在所有当前已注册同步上触发 {@code savepoint} 回调。\n\t * @throws RuntimeException 若 {@code savepoint} 回调抛出\n\t * @since 6.2\n\t * @see TransactionSynchronization#savepoint\n\t */",
        ),
        (
            "\t/**\n\t * Trigger {@code flush} callbacks on all currently registered synchronizations.\n\t * @throws RuntimeException if thrown by a {@code savepointRollback} callback\n\t * @since 6.2\n\t * @see TransactionSynchronization#savepointRollback\n\t */",
            "\t/**\n\t * 在所有当前已注册同步上触发 {@code savepointRollback} 回调。\n\t * @throws RuntimeException 若 {@code savepointRollback} 回调抛出\n\t * @since 6.2\n\t * @see TransactionSynchronization#savepointRollback\n\t */",
        ),
        (
            "\t/**\n\t * Trigger {@code beforeCommit} callbacks on all currently registered synchronizations.\n\t * @param readOnly whether the transaction is defined as read-only transaction\n\t * @throws RuntimeException if thrown by a {@code beforeCommit} callback\n\t * @see TransactionSynchronization#beforeCommit(boolean)\n\t */",
            "\t/**\n\t * 在所有当前已注册同步上触发 {@code beforeCommit} 回调。\n\t * @param readOnly 事务是否定义为只读事务\n\t * @throws RuntimeException 若 {@code beforeCommit} 回调抛出\n\t * @see TransactionSynchronization#beforeCommit(boolean)\n\t */",
        ),
        (
            "\t/**\n\t * Trigger {@code beforeCompletion} callbacks on all currently registered synchronizations.\n\t * @see TransactionSynchronization#beforeCompletion()\n\t */",
            "\t/**\n\t * 在所有当前已注册同步上触发 {@code beforeCompletion} 回调。\n\t * @see TransactionSynchronization#beforeCompletion()\n\t */",
        ),
        (
            "\t/**\n\t * Trigger {@code afterCommit} callbacks on all currently registered synchronizations.\n\t * @throws RuntimeException if thrown by a {@code afterCommit} callback\n\t * @see TransactionSynchronizationManager#getSynchronizations()\n\t * @see TransactionSynchronization#afterCommit()\n\t */",
            "\t/**\n\t * 在所有当前已注册同步上触发 {@code afterCommit} 回调。\n\t * @throws RuntimeException 若 {@code afterCommit} 回调抛出\n\t * @see TransactionSynchronizationManager#getSynchronizations()\n\t * @see TransactionSynchronization#afterCommit()\n\t */",
        ),
        (
            "\t/**\n\t * Actually invoke the {@code afterCommit} methods of the\n\t * given Spring TransactionSynchronization objects.\n\t * @param synchronizations a List of TransactionSynchronization objects\n\t * @see TransactionSynchronization#afterCommit()\n\t */",
            "\t/**\n\t * 实际调用给定 Spring TransactionSynchronization 对象的\n\t * {@code afterCommit} 方法。\n\t * @param synchronizations TransactionSynchronization 对象列表\n\t * @see TransactionSynchronization#afterCommit()\n\t */",
        ),
        (
            "\t/**\n\t * Trigger {@code afterCompletion} callbacks on all currently registered synchronizations.\n\t * @param completionStatus the completion status according to the\n\t * constants in the TransactionSynchronization interface\n\t * @see TransactionSynchronizationManager#getSynchronizations()\n\t * @see TransactionSynchronization#afterCompletion(int)\n\t * @see TransactionSynchronization#STATUS_COMMITTED\n\t * @see TransactionSynchronization#STATUS_ROLLED_BACK\n\t * @see TransactionSynchronization#STATUS_UNKNOWN\n\t */",
            "\t/**\n\t * 在所有当前已注册同步上触发 {@code afterCompletion} 回调。\n\t * @param completionStatus 根据 TransactionSynchronization 接口中\n\t * 常量表示的完成状态\n\t * @see TransactionSynchronizationManager#getSynchronizations()\n\t * @see TransactionSynchronization#afterCompletion(int)\n\t * @see TransactionSynchronization#STATUS_COMMITTED\n\t * @see TransactionSynchronization#STATUS_ROLLED_BACK\n\t * @see TransactionSynchronization#STATUS_UNKNOWN\n\t */",
        ),
        (
            "\t/**\n\t * Actually invoke the {@code afterCompletion} methods of the\n\t * given Spring TransactionSynchronization objects.\n\t * @param synchronizations a List of TransactionSynchronization objects\n\t * @param completionStatus the completion status according to the\n\t * constants in the TransactionSynchronization interface\n\t * @see TransactionSynchronization#afterCompletion(int)\n\t * @see TransactionSynchronization#STATUS_COMMITTED\n\t * @see TransactionSynchronization#STATUS_ROLLED_BACK\n\t * @see TransactionSynchronization#STATUS_UNKNOWN\n\t */",
            "\t/**\n\t * 实际调用给定 Spring TransactionSynchronization 对象的\n\t * {@code afterCompletion} 方法。\n\t * @param synchronizations TransactionSynchronization 对象列表\n\t * @param completionStatus 根据 TransactionSynchronization 接口中\n\t * 常量表示的完成状态\n\t * @see TransactionSynchronization#afterCompletion(int)\n\t * @see TransactionSynchronization#STATUS_COMMITTED\n\t * @see TransactionSynchronization#STATUS_ROLLED_BACK\n\t * @see TransactionSynchronization#STATUS_UNKNOWN\n\t */",
        ),
        (
            "\t/**\n\t * Inner class to avoid hard-coded dependency on AOP module.\n\t */",
            "\t/**\n\t * 内部类，避免对 AOP 模块的硬编码依赖。\n\t */",
        ),
    ],
    "TransactionTemplate.java": [
        (
            "/**\n * Template class that simplifies programmatic transaction demarcation and\n * transaction exception handling.\n *\n * <p>The central method is {@link #execute}, supporting transactional code that\n * implements the {@link TransactionCallback} interface. This template handles\n * the transaction lifecycle and possible exceptions such that neither the\n * TransactionCallback implementation nor the calling code needs to explicitly\n * handle transactions.\n *\n * <p>Typical usage: Allows for writing low-level data access objects that use\n * resources such as JDBC DataSources but are not transaction-aware themselves.\n * Instead, they can implicitly participate in transactions handled by higher-level\n * application services utilizing this class, making calls to the low-level\n * services via an inner-class callback object.\n *\n * <p>Can be used within a service implementation via direct instantiation with\n * a transaction manager reference, or get prepared in an application context\n * and passed to services as bean reference. Note: The transaction manager should\n * always be configured as bean in the application context: in the first case given\n * to the service directly, in the second case given to the prepared template.\n *\n * <p>Supports setting the propagation behavior and the isolation level by name,\n * for convenient configuration in context definitions.\n *\n * @author Juergen Hoeller\n * @since 17.03.2003\n * @see #execute\n * @see #setTransactionManager\n * @see org.springframework.transaction.PlatformTransactionManager\n */",
            "/**\n * 简化编程式事务边界与事务异常处理的模板类。\n *\n * <p>核心方法是 {@link #execute}，支持实现 {@link TransactionCallback} 接口\n * 的事务代码。此模板处理事务生命周期及可能出现的异常，\n * 使 TransactionCallback 实现与调用代码均无需显式处理事务。\n *\n * <p>典型用法：编写使用 JDBC DataSource 等资源、\n * 但自身无事务感知的底层数据访问对象。\n * 它们可通过内部类回调对象调用底层服务，\n * 隐式参与使用此类的高层应用服务所管理的事务。\n *\n * <p>可在服务实现中直接实例化并传入事务管理器引用使用，\n * 或在应用上下文中配置后以 Bean 引用传给服务。\n * 注意：事务管理器应始终在应用上下文中配置为 Bean：\n * 第一种情况直接交给服务，第二种情况交给已配置的模板。\n *\n * <p>支持按名称设置传播行为与隔离级别，\n * 便于在上下文定义中配置。\n *\n * @author Juergen Hoeller\n * @since 17.03.2003\n * @see #execute\n * @see #setTransactionManager\n * @see org.springframework.transaction.PlatformTransactionManager\n */",
        ),
        (
            "\t/** Logger available to subclasses. */",
            "\t/** 子类可用的 Logger。 */",
        ),
        (
            "\t/**\n\t * Construct a new TransactionTemplate for bean usage.\n\t * <p>Note: The PlatformTransactionManager needs to be set before\n\t * any {@code execute} calls.\n\t * @see #setTransactionManager\n\t */",
            "\t/**\n\t * 构造供 Bean 使用的 TransactionTemplate。\n\t * <p>注意：在任何 {@code execute} 调用前须设置 PlatformTransactionManager。\n\t * @see #setTransactionManager\n\t */",
        ),
        (
            "\t/**\n\t * Construct a new TransactionTemplate using the given transaction manager.\n\t * @param transactionManager the transaction management strategy to be used\n\t */",
            "\t/**\n\t * 使用给定事务管理器构造 TransactionTemplate。\n\t * @param transactionManager 要使用的事务管理策略\n\t */",
        ),
        (
            "\t/**\n\t * Construct a new TransactionTemplate using the given transaction manager,\n\t * taking its default settings from the given transaction definition.\n\t * @param transactionManager the transaction management strategy to be used\n\t * @param transactionDefinition the transaction definition to copy the\n\t * default settings from. Local properties can still be set to change values.\n\t */",
            "\t/**\n\t * 使用给定事务管理器构造 TransactionTemplate，\n\t * 默认设置取自给定事务定义。\n\t * @param transactionManager 要使用的事务管理策略\n\t * @param transactionDefinition 复制默认设置来源的事务定义。\n\t * 仍可通过本地属性覆盖值。\n\t */",
        ),
        (
            "\t/**\n\t * Set the transaction management strategy to be used.\n\t */",
            "\t/**\n\t * 设置要使用的事务管理策略。\n\t */",
        ),
        (
            "\t/**\n\t * Return the transaction management strategy to be used.\n\t */",
            "\t/**\n\t * 返回要使用的事务管理策略。\n\t */",
        ),
        (
            "\t\t\t\t// Transactional code threw application exception -> rollback",
            "\t\t\t\t// 事务代码抛出应用异常 -> 回滚",
        ),
        (
            "\t\t\t\t// Transactional code threw unexpected exception -> rollback",
            "\t\t\t\t// 事务代码抛出意外异常 -> 回滚",
        ),
        (
            "\t/**\n\t * Perform a rollback, handling rollback exceptions properly.\n\t * @param status object representing the transaction\n\t * @param ex the thrown application exception or error\n\t * @throws TransactionException in case of a rollback error\n\t */",
            "\t/**\n\t * 执行回滚，并正确处理回滚异常。\n\t * @param status 表示事务的对象\n\t * @param ex 抛出的应用异常或错误\n\t * @throws TransactionException 回滚出错时\n\t */",
        ),
    ],
}
