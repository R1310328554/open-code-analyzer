"""Chinese JavaDoc replacements for springframework wave18a transaction.support [16:20]."""

SUPPORT_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "AbstractTransactionStatus.java": [
        (
            "/**\n * Abstract base implementation of the\n * {@link org.springframework.transaction.TransactionStatus} interface.\n *\n * <p>Pre-implements the handling of local rollback-only and completed flags, and\n * delegation to an underlying {@link org.springframework.transaction.SavepointManager}.\n * Also offers the option of a holding a savepoint within the transaction.\n *\n * <p>Does not assume any specific internal transaction handling, such as an\n * underlying transaction object, and no transaction synchronization mechanism.\n *\n * @author Juergen Hoeller\n * @since 1.2.3\n * @see #setRollbackOnly()\n * @see #isRollbackOnly()\n * @see #setCompleted()\n * @see #isCompleted()\n * @see #getSavepointManager()\n * @see SimpleTransactionStatus\n * @see DefaultTransactionStatus\n */",
            "/**\n * {@link org.springframework.transaction.TransactionStatus} 接口的抽象基类实现。\n *\n * <p>预实现本地 rollback-only 与 completed 标志的处理，\n * 以及对底层 {@link org.springframework.transaction.SavepointManager} 的委托。\n * 还提供在事务内持有保存点的选项。\n *\n * <p>不假定任何特定内部事务处理（如底层事务对象）及事务同步机制。\n *\n * @author Juergen Hoeller\n * @since 1.2.3\n * @see #setRollbackOnly()\n * @see #isRollbackOnly()\n * @see #setCompleted()\n * @see #isCompleted()\n * @see #getSavepointManager()\n * @see SimpleTransactionStatus\n * @see DefaultTransactionStatus\n */",
        ),
        (
            "\t//---------------------------------------------------------------------\n\t// Implementation of TransactionExecution\n\t//---------------------------------------------------------------------",
            "\t//---------------------------------------------------------------------\n\t// TransactionExecution 实现\n\t//---------------------------------------------------------------------",
        ),
        (
            "\t/**\n\t * Determine the rollback-only flag via checking both the local rollback-only flag\n\t * of this TransactionStatus and the global rollback-only flag of the underlying\n\t * transaction, if any.\n\t * @see #isLocalRollbackOnly()\n\t * @see #isGlobalRollbackOnly()\n\t */",
            "\t/**\n\t * 通过检查本 TransactionStatus 的本地 rollback-only 标志\n\t * 以及底层事务（若有）的全局 rollback-only 标志来确定 rollback-only。\n\t * @see #isLocalRollbackOnly()\n\t * @see #isGlobalRollbackOnly()\n\t */",
        ),
        (
            "\t/**\n\t * Determine the rollback-only flag via checking this TransactionStatus.\n\t * <p>Will only return \"true\" if the application called {@code setRollbackOnly}\n\t * on this TransactionStatus object.\n\t */",
            "\t/**\n\t * 通过检查本 TransactionStatus 确定 rollback-only 标志。\n\t * <p>仅当应用在本 TransactionStatus 对象上调用 {@code setRollbackOnly} 时才返回 \"true\"。\n\t */",
        ),
        (
            "\t/**\n\t * Template method for determining the global rollback-only flag of the\n\t * underlying transaction, if any.\n\t * <p>This implementation always returns {@code false}.\n\t */",
            "\t/**\n\t * 确定底层事务（若有）全局 rollback-only 标志的模板方法。\n\t * <p>本实现始终返回 {@code false}。\n\t */",
        ),
        (
            "\t/**\n\t * Mark this transaction as completed, that is, committed or rolled back.\n\t */",
            "\t/**\n\t * 将本事务标记为已完成，即已提交或已回滚。\n\t */",
        ),
        (
            "\t//---------------------------------------------------------------------\n\t// Handling of current savepoint state\n\t//---------------------------------------------------------------------",
            "\t//---------------------------------------------------------------------\n\t// 当前保存点状态处理\n\t//---------------------------------------------------------------------",
        ),
        (
            "\t/**\n\t * Set a savepoint for this transaction. Useful for PROPAGATION_NESTED.\n\t * @see org.springframework.transaction.TransactionDefinition#PROPAGATION_NESTED\n\t */",
            "\t/**\n\t * 为本事务设置保存点。适用于 PROPAGATION_NESTED。\n\t * @see org.springframework.transaction.TransactionDefinition#PROPAGATION_NESTED\n\t */",
        ),
        (
            "\t/**\n\t * Get the savepoint for this transaction, if any.\n\t */",
            "\t/**\n\t * 获取本事务的保存点（若有）。\n\t */",
        ),
        (
            "\t/**\n\t * Create a savepoint and hold it for the transaction.\n\t * @throws org.springframework.transaction.NestedTransactionNotSupportedException\n\t * if the underlying transaction does not support savepoints\n\t * @see SavepointManager#createSavepoint\n\t */",
            "\t/**\n\t * 创建保存点并为事务持有。\n\t * @throws org.springframework.transaction.NestedTransactionNotSupportedException\n\t * 若底层事务不支持保存点\n\t * @see SavepointManager#createSavepoint\n\t */",
        ),
        (
            "\t/**\n\t * Roll back to the savepoint that is held for the transaction\n\t * and release the savepoint right afterwards.\n\t * @see SavepointManager#rollbackToSavepoint\n\t * @see SavepointManager#releaseSavepoint\n\t */",
            "\t/**\n\t * 回滚到为事务持有的保存点，并随后立即释放保存点。\n\t * @see SavepointManager#rollbackToSavepoint\n\t * @see SavepointManager#releaseSavepoint\n\t */",
        ),
        (
            "\t/**\n\t * Release the savepoint that is held for the transaction.\n\t * @see SavepointManager#releaseSavepoint\n\t */",
            "\t/**\n\t * 释放为事务持有的保存点。\n\t * @see SavepointManager#releaseSavepoint\n\t */",
        ),
        (
            "\t//---------------------------------------------------------------------\n\t// Implementation of SavepointManager\n\t//---------------------------------------------------------------------",
            "\t//---------------------------------------------------------------------\n\t// SavepointManager 实现\n\t//---------------------------------------------------------------------",
        ),
        (
            "\t/**\n\t * This implementation delegates to a SavepointManager for the\n\t * underlying transaction, if possible.\n\t * @see #getSavepointManager()\n\t * @see SavepointManager#createSavepoint()\n\t */",
            "\t/**\n\t * 本实现尽可能委托底层事务的 SavepointManager。\n\t * @see #getSavepointManager()\n\t * @see SavepointManager#createSavepoint()\n\t */",
        ),
        (
            "\t/**\n\t * This implementation delegates to a SavepointManager for the\n\t * underlying transaction, if possible.\n\t * @see #getSavepointManager()\n\t * @see SavepointManager#rollbackToSavepoint(Object)\n\t */",
            "\t/**\n\t * 本实现尽可能委托底层事务的 SavepointManager。\n\t * @see #getSavepointManager()\n\t * @see SavepointManager#rollbackToSavepoint(Object)\n\t */",
        ),
        (
            "\t/**\n\t * This implementation delegates to a SavepointManager for the\n\t * underlying transaction, if possible.\n\t * @see #getSavepointManager()\n\t * @see SavepointManager#releaseSavepoint(Object)\n\t */",
            "\t/**\n\t * 本实现尽可能委托底层事务的 SavepointManager。\n\t * @see #getSavepointManager()\n\t * @see SavepointManager#releaseSavepoint(Object)\n\t */",
        ),
        (
            "\t/**\n\t * Return a SavepointManager for the underlying transaction, if possible.\n\t * <p>Default implementation always throws a NestedTransactionNotSupportedException.\n\t * @throws org.springframework.transaction.NestedTransactionNotSupportedException\n\t * if the underlying transaction does not support savepoints\n\t */",
            "\t/**\n\t * 返回底层事务的 SavepointManager（若可能）。\n\t * <p>默认实现始终抛出 NestedTransactionNotSupportedException。\n\t * @throws org.springframework.transaction.NestedTransactionNotSupportedException\n\t * 若底层事务不支持保存点\n\t */",
        ),
    ],
    "CallbackPreferringPlatformTransactionManager.java": [
        (
            "/**\n * Extension of the {@link org.springframework.transaction.PlatformTransactionManager}\n * interface, exposing a method for executing a given callback within a transaction.\n *\n * <p>Implementors of this interface automatically express a preference for\n * callbacks over programmatic {@code getTransaction}, {@code commit}\n * and {@code rollback} calls. Calling code may check whether a given\n * transaction manager implements this interface to choose to prepare a\n * callback instead of explicit transaction demarcation control.\n *\n * <p>Spring's {@link TransactionTemplate} and\n * {@link org.springframework.transaction.interceptor.TransactionInterceptor}\n * detect and use this PlatformTransactionManager variant automatically.\n *\n * @author Juergen Hoeller\n * @since 2.0\n * @see TransactionTemplate\n * @see org.springframework.transaction.interceptor.TransactionInterceptor\n */",
            "/**\n * {@link org.springframework.transaction.PlatformTransactionManager} 接口的扩展，\n * 暴露在给定回调内于事务中执行的方法。\n *\n * <p>本接口实现者自动表达对回调而非编程式 {@code getTransaction}、\n * {@code commit} 和 {@code rollback} 调用的偏好。\n * 调用代码可检查给定事务管理器是否实现本接口，\n * 以选择准备回调而非显式事务边界控制。\n *\n * <p>Spring 的 {@link TransactionTemplate} 和\n * {@link org.springframework.transaction.interceptor.TransactionInterceptor}\n * 会自动检测并使用本 PlatformTransactionManager 变体。\n *\n * @author Juergen Hoeller\n * @since 2.0\n * @see TransactionTemplate\n * @see org.springframework.transaction.interceptor.TransactionInterceptor\n */",
        ),
        (
            "\t/**\n\t * Execute the action specified by the given callback object within a transaction.\n\t * <p>Allows for returning a result object created within the transaction, that is,\n\t * a domain object or a collection of domain objects. A RuntimeException thrown\n\t * by the callback is treated as a fatal exception that enforces a rollback.\n\t * Such an exception gets propagated to the caller of the template.\n\t * @param definition the definition for the transaction to wrap the callback in\n\t * @param callback the callback object that specifies the transactional action\n\t * @return a result object returned by the callback, or {@code null} if none\n\t * @throws TransactionException in case of initialization, rollback, or system errors\n\t * @throws RuntimeException if thrown by the TransactionCallback\n\t */",
            "\t/**\n\t * 在事务内执行给定回调对象指定的操作。\n\t * <p>允许返回事务内创建的结果对象，即领域对象或领域对象集合。\n\t * 回调抛出的 RuntimeException 视为强制回滚的致命异常，\n\t * 并传播给模板调用方。\n\t * @param definition 包装回调的事务定义\n\t * @param callback 指定事务操作的回调对象\n\t * @return 回调返回的结果对象，无则为 {@code null}\n\t * @throws TransactionException 初始化、回滚或系统错误时\n\t * @throws RuntimeException 若 TransactionCallback 抛出\n\t */",
        ),
    ],
    "DefaultTransactionDefinition.java": [
        (
            "/**\n * Default implementation of the {@link TransactionDefinition} interface,\n * offering bean-style configuration and sensible default values\n * (PROPAGATION_REQUIRED, ISOLATION_DEFAULT, TIMEOUT_DEFAULT, readOnly=false).\n *\n * <p>Base class for both {@link TransactionTemplate} and\n * {@link org.springframework.transaction.interceptor.DefaultTransactionAttribute}.\n *\n * @author Juergen Hoeller\n * @author Sam Brannen\n * @since 08.05.2003\n */",
            "/**\n * {@link TransactionDefinition} 接口的默认实现，\n * 提供 bean 风格配置与合理默认值\n * （PROPAGATION_REQUIRED、ISOLATION_DEFAULT、TIMEOUT_DEFAULT、readOnly=false）。\n *\n * <p>是 {@link TransactionTemplate} 和\n * {@link org.springframework.transaction.interceptor.DefaultTransactionAttribute} 的基类。\n *\n * @author Juergen Hoeller\n * @author Sam Brannen\n * @since 08.05.2003\n */",
        ),
        (
            "\t/** Prefix for the propagation constants defined in TransactionDefinition. */",
            "\t/** TransactionDefinition 中传播常量的前缀。 */",
        ),
        (
            "\t/** Prefix for the isolation constants defined in TransactionDefinition. */",
            "\t/** TransactionDefinition 中隔离常量的前缀。 */",
        ),
        (
            "\t/** Prefix for transaction timeout values in description strings. */",
            "\t/** 描述字符串中事务超时值的前缀。 */",
        ),
        (
            "\t/** Marker for read-only transactions in description strings. */",
            "\t/** 描述字符串中只读事务的标记。 */",
        ),
        (
            "\t/**\n\t * Map of constant names to constant values for the propagation constants\n\t * defined in {@link TransactionDefinition}.\n\t */",
            "\t/**\n\t * {@link TransactionDefinition} 中传播常量的名称到值映射。\n\t */",
        ),
        (
            "\t/**\n\t * Map of constant names to constant values for the isolation constants\n\t * defined in {@link TransactionDefinition}.\n\t */",
            "\t/**\n\t * {@link TransactionDefinition} 中隔离常量的名称到值映射。\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@code DefaultTransactionDefinition} with default settings.\n\t * Can be modified through bean property setters.\n\t * @see #setPropagationBehavior\n\t * @see #setIsolationLevel\n\t * @see #setTimeout\n\t * @see #setReadOnly\n\t * @see #setName\n\t */",
            "\t/**\n\t * 使用默认设置创建新的 {@code DefaultTransactionDefinition}。\n\t * 可通过 bean 属性 setter 修改。\n\t * @see #setPropagationBehavior\n\t * @see #setIsolationLevel\n\t * @see #setTimeout\n\t * @see #setReadOnly\n\t * @see #setName\n\t */",
        ),
        (
            "\t/**\n\t * Copy constructor. Definition can be modified through bean property setters.\n\t * @see #setPropagationBehavior\n\t * @see #setIsolationLevel\n\t * @see #setTimeout\n\t * @see #setReadOnly\n\t * @see #setName\n\t */",
            "\t/**\n\t * 拷贝构造函数。定义可通过 bean 属性 setter 修改。\n\t * @see #setPropagationBehavior\n\t * @see #setIsolationLevel\n\t * @see #setTimeout\n\t * @see #setReadOnly\n\t * @see #setName\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@code DefaultTransactionDefinition} with the given\n\t * propagation behavior. Can be modified through bean property setters.\n\t * @param propagationBehavior one of the propagation constants in the\n\t * TransactionDefinition interface\n\t * @see #setIsolationLevel\n\t * @see #setTimeout\n\t * @see #setReadOnly\n\t */",
            "\t/**\n\t * 使用给定传播行为创建新的 {@code DefaultTransactionDefinition}。\n\t * 可通过 bean 属性 setter 修改。\n\t * @param propagationBehavior TransactionDefinition 接口中的传播常量之一\n\t * @see #setIsolationLevel\n\t * @see #setTimeout\n\t * @see #setReadOnly\n\t */",
        ),
        (
            "\t/**\n\t * Set the propagation behavior by the name of the corresponding constant in\n\t * {@link TransactionDefinition} &mdash; for example, {@code \"PROPAGATION_REQUIRED\"}.\n\t * @param constantName name of the constant\n\t * @throws IllegalArgumentException if the supplied value is not resolvable\n\t * to one of the {@code PROPAGATION_} constants or is {@code null}\n\t * @see #setPropagationBehavior\n\t * @see #PROPAGATION_REQUIRED\n\t */",
            "\t/**\n\t * 通过 {@link TransactionDefinition} 中对应常量名称设置传播行为\n\t * ——例如 {@code \"PROPAGATION_REQUIRED\"}。\n\t * @param constantName 常量名称\n\t * @throws IllegalArgumentException 若提供的值无法解析为 {@code PROPAGATION_} 常量之一或为 {@code null}\n\t * @see #setPropagationBehavior\n\t * @see #PROPAGATION_REQUIRED\n\t */",
        ),
        (
            "\t/**\n\t * Set the propagation behavior. Must be one of the propagation constants\n\t * in the TransactionDefinition interface. Default is PROPAGATION_REQUIRED.\n\t * <p>Exclusively designed for use with {@link #PROPAGATION_REQUIRED} or\n\t * {@link #PROPAGATION_REQUIRES_NEW} since it only applies to newly started\n\t * transactions. Consider switching the \"validateExistingTransaction\" flag to\n\t * \"true\" on your transaction manager if you'd like isolation level declarations\n\t * to get rejected when participating in an existing transaction with a different\n\t * isolation level.\n\t * <p>Note that a transaction manager that does not support custom isolation levels\n\t * will throw an exception when given any other level than {@link #ISOLATION_DEFAULT}.\n\t * @throws IllegalArgumentException if the supplied value is not one of the\n\t * {@code PROPAGATION_} constants\n\t * @see #PROPAGATION_REQUIRED\n\t */",
            "\t/**\n\t * 设置传播行为。必须是 TransactionDefinition 接口中的传播常量之一。默认为 PROPAGATION_REQUIRED。\n\t * <p>专用于 {@link #PROPAGATION_REQUIRED} 或 {@link #PROPAGATION_REQUIRES_NEW}，\n\t * 因为仅适用于新启动的事务。若希望参与具有不同隔离级别的现有事务时\n\t * 拒绝隔离级别声明，可将事务管理器的 \"validateExistingTransaction\" 标志设为 \"true\"。\n\t * <p>不支持自定义隔离级别的事务管理器在收到非 {@link #ISOLATION_DEFAULT} 级别时将抛出异常。\n\t * @throws IllegalArgumentException 若提供的值不是 {@code PROPAGATION_} 常量之一\n\t * @see #PROPAGATION_REQUIRED\n\t */",
        ),
        (
            "\t/**\n\t * Set the isolation level by the name of the corresponding constant in\n\t * {@link TransactionDefinition} &mdash; for example, {@code \"ISOLATION_DEFAULT\"}.\n\t * @param constantName name of the constant\n\t * @throws IllegalArgumentException if the supplied value is not resolvable\n\t * to one of the {@code ISOLATION_} constants or is {@code null}\n\t * @see #setIsolationLevel\n\t * @see #ISOLATION_DEFAULT\n\t */",
            "\t/**\n\t * 通过 {@link TransactionDefinition} 中对应常量名称设置隔离级别\n\t * ——例如 {@code \"ISOLATION_DEFAULT\"}。\n\t * @param constantName 常量名称\n\t * @throws IllegalArgumentException 若提供的值无法解析为 {@code ISOLATION_} 常量之一或为 {@code null}\n\t * @see #setIsolationLevel\n\t * @see #ISOLATION_DEFAULT\n\t */",
        ),
        (
            "\t/**\n\t * Set the isolation level. Must be one of the isolation constants\n\t * in the TransactionDefinition interface. Default is ISOLATION_DEFAULT.\n\t * <p>Exclusively designed for use with {@link #PROPAGATION_REQUIRED} or\n\t * {@link #PROPAGATION_REQUIRES_NEW} since it only applies to newly started\n\t * transactions. Consider switching the \"validateExistingTransaction\" flag to\n\t * \"true\" on your transaction manager if you'd like isolation level declarations\n\t * to get rejected when participating in an existing transaction with a different\n\t * isolation level.\n\t * <p>Note that a transaction manager that does not support custom isolation levels\n\t * will throw an exception when given any other level than {@link #ISOLATION_DEFAULT}.\n\t * @throws IllegalArgumentException if the supplied value is not one of the\n\t * {@code ISOLATION_} constants\n\t * @see #ISOLATION_DEFAULT\n\t */",
            "\t/**\n\t * 设置隔离级别。必须是 TransactionDefinition 接口中的隔离常量之一。默认为 ISOLATION_DEFAULT。\n\t * <p>专用于 {@link #PROPAGATION_REQUIRED} 或 {@link #PROPAGATION_REQUIRES_NEW}，\n\t * 因为仅适用于新启动的事务。若希望参与具有不同隔离级别的现有事务时\n\t * 拒绝隔离级别声明，可将事务管理器的 \"validateExistingTransaction\" 标志设为 \"true\"。\n\t * <p>不支持自定义隔离级别的事务管理器在收到非 {@link #ISOLATION_DEFAULT} 级别时将抛出异常。\n\t * @throws IllegalArgumentException 若提供的值不是 {@code ISOLATION_} 常量之一\n\t * @see #ISOLATION_DEFAULT\n\t */",
        ),
        (
            "\t/**\n\t * Set the timeout to apply, as number of seconds.\n\t * Default is TIMEOUT_DEFAULT (-1).\n\t * <p>Exclusively designed for use with {@link #PROPAGATION_REQUIRED} or\n\t * {@link #PROPAGATION_REQUIRES_NEW} since it only applies to newly started\n\t * transactions.\n\t * <p>Note that a transaction manager that does not support timeouts will throw\n\t * an exception when given any other timeout than {@link #TIMEOUT_DEFAULT}.\n\t * @see #TIMEOUT_DEFAULT\n\t */",
            "\t/**\n\t * 设置要应用的超时（秒数）。\n\t * 默认为 TIMEOUT_DEFAULT（-1）。\n\t * <p>专用于 {@link #PROPAGATION_REQUIRED} 或 {@link #PROPAGATION_REQUIRES_NEW}，\n\t * 因为仅适用于新启动的事务。\n\t * <p>不支持超时的事务管理器在收到非 {@link #TIMEOUT_DEFAULT} 超时时将抛出异常。\n\t * @see #TIMEOUT_DEFAULT\n\t */",
        ),
        (
            "\t/**\n\t * Set whether to optimize as read-only transaction.\n\t * Default is \"false\".\n\t * <p>The read-only flag applies to any transaction context, whether backed\n\t * by an actual resource transaction ({@link #PROPAGATION_REQUIRED}/\n\t * {@link #PROPAGATION_REQUIRES_NEW}) or operating non-transactionally at\n\t * the resource level ({@link #PROPAGATION_SUPPORTS}). In the latter case,\n\t * the flag will only apply to managed resources within the application,\n\t * such as a Hibernate {@code Session}.\n\t * <p>This just serves as a hint for the actual transaction subsystem;\n\t * it will <i>not necessarily</i> cause failure of write access attempts.\n\t * A transaction manager which cannot interpret the read-only hint will\n\t * <i>not</i> throw an exception when asked for a read-only transaction.\n\t */",
            "\t/**\n\t * 设置是否作为只读事务优化。默认为 \"false\"。\n\t * <p>只读标志适用于任意事务上下文，无论由实际资源事务\n\t * （{@link #PROPAGATION_REQUIRED}/{@link #PROPAGATION_REQUIRES_NEW}）支持，\n\t * 或在资源层非事务运行（{@link #PROPAGATION_SUPPORTS}）。\n\t * 后者情况下，该标志仅适用于应用内受管资源，如 Hibernate {@code Session}。\n\t * <p>这仅作为实际事务子系统的提示；<i>不必然</i>导致写访问失败。\n\t * 无法解释只读提示的事务管理器在请求只读事务时<i>不会</i>抛出异常。\n\t */",
        ),
        (
            "\t/**\n\t * Set the name of this transaction. Default is none.\n\t * <p>This will be used as transaction name to be shown in a\n\t * transaction monitor, if applicable (for example, WebLogic's).\n\t */",
            "\t/**\n\t * 设置本事务名称。默认无。\n\t * <p>若适用（例如 WebLogic），将作为事务监视器中显示的事务名称。\n\t */",
        ),
        (
            "\t/**\n\t * This implementation compares the {@code toString()} results.\n\t * @see #toString()\n\t */",
            "\t/**\n\t * 本实现比较 {@code toString()} 结果。\n\t * @see #toString()\n\t */",
        ),
        (
            "\t/**\n\t * This implementation returns {@code toString()}'s hash code.\n\t * @see #toString()\n\t */",
            "\t/**\n\t * 本实现返回 {@code toString()} 的哈希码。\n\t * @see #toString()\n\t */",
        ),
        (
            "\t/**\n\t * Return an identifying description for this transaction definition.\n\t * <p>The format matches the one used by\n\t * {@link org.springframework.transaction.interceptor.TransactionAttributeEditor},\n\t * to be able to feed {@code toString} results into bean properties of type\n\t * {@link org.springframework.transaction.interceptor.TransactionAttribute}.\n\t * <p>Has to be overridden in subclasses for correct {@code equals}\n\t * and {@code hashCode} behavior. Alternatively, {@link #equals}\n\t * and {@link #hashCode} can be overridden themselves.\n\t * @see #getDefinitionDescription()\n\t * @see org.springframework.transaction.interceptor.TransactionAttributeEditor\n\t */",
            "\t/**\n\t * 返回本事务定义的标识描述。\n\t * <p>格式与 {@link org.springframework.transaction.interceptor.TransactionAttributeEditor} 使用的格式一致，\n\t * 以便将 {@code toString} 结果填入类型为\n\t * {@link org.springframework.transaction.interceptor.TransactionAttribute} 的 bean 属性。\n\t * <p>子类须覆盖以实现正确的 {@code equals} 和 {@code hashCode} 行为。\n\t * 也可直接覆盖 {@link #equals} 和 {@link #hashCode}。\n\t * @see #getDefinitionDescription()\n\t * @see org.springframework.transaction.interceptor.TransactionAttributeEditor\n\t */",
        ),
        (
            "\t/**\n\t * Return an identifying description for this transaction definition.\n\t * <p>Available to subclasses, for inclusion in their {@code toString()} result.\n\t */",
            "\t/**\n\t * 返回本事务定义的标识描述。\n\t * <p>供子类在其 {@code toString()} 结果中使用。\n\t */",
        ),
    ],
    "DefaultTransactionStatus.java": [
        (
            "/**\n * Default implementation of the {@link org.springframework.transaction.TransactionStatus}\n * interface, used by {@link AbstractPlatformTransactionManager}. Based on the concept\n * of an underlying \"transaction object\".\n *\n * <p>Holds all status information that {@link AbstractPlatformTransactionManager}\n * needs internally, including a generic transaction object determined by the\n * concrete transaction manager implementation.\n *\n * <p>Supports delegating savepoint-related methods to a transaction object\n * that implements the {@link SavepointManager} interface.\n *\n * <p><b>NOTE:</b> This is <i>not</i> intended for use with other PlatformTransactionManager\n * implementations, in particular not for mock transaction managers in testing environments.\n * Use the alternative {@link SimpleTransactionStatus} class or a mock for the plain\n * {@link org.springframework.transaction.TransactionStatus} interface instead.\n *\n * @author Juergen Hoeller\n * @since 19.01.2004\n * @see AbstractPlatformTransactionManager\n * @see org.springframework.transaction.SavepointManager\n * @see #getTransaction\n * @see #createSavepoint\n * @see #rollbackToSavepoint\n * @see #releaseSavepoint\n * @see SimpleTransactionStatus\n */",
            "/**\n * {@link org.springframework.transaction.TransactionStatus} 接口的默认实现，\n * 由 {@link AbstractPlatformTransactionManager} 使用。基于底层 \"事务对象\" 概念。\n *\n * <p>持有 {@link AbstractPlatformTransactionManager} 内部所需的全部状态信息，\n * 包括由具体事务管理器实现确定的通用事务对象。\n *\n * <p>支持将保存点相关方法委托给实现 {@link SavepointManager} 接口的事务对象。\n *\n * <p><b>注意：</b>本类<i>不</i>供其他 PlatformTransactionManager 实现使用，\n * 尤其不用于测试环境中的 mock 事务管理器。\n * 请改用 {@link SimpleTransactionStatus} 类或普通\n * {@link org.springframework.transaction.TransactionStatus} 接口的 mock。\n *\n * @author Juergen Hoeller\n * @since 19.01.2004\n * @see AbstractPlatformTransactionManager\n * @see org.springframework.transaction.SavepointManager\n * @see #getTransaction\n * @see #createSavepoint\n * @see #rollbackToSavepoint\n * @see #releaseSavepoint\n * @see SimpleTransactionStatus\n */",
        ),
        (
            "\t/**\n\t * Create a new {@code DefaultTransactionStatus} instance.\n\t * @param transactionName the defined name of the transaction\n\t * @param transaction underlying transaction object that can hold state\n\t * for the internal transaction implementation\n\t * @param newTransaction if the transaction is new, otherwise participating\n\t * in an existing transaction\n\t * @param newSynchronization if a new transaction synchronization has been\n\t * opened for the given transaction\n\t * @param readOnly whether the transaction is marked as read-only\n\t * @param debug should debug logging be enabled for the handling of this transaction?\n\t * Caching it in here can prevent repeated calls to ask the logging system whether\n\t * debug logging should be enabled.\n\t * @param suspendedResources a holder for resources that have been suspended\n\t * for this transaction, if any\n\t * @since 6.1\n\t */",
            "\t/**\n\t * 创建新的 {@code DefaultTransactionStatus} 实例。\n\t * @param transactionName 定义的事务名称\n\t * @param transaction 可为内部事务实现保存状态的底层事务对象\n\t * @param newTransaction 若为新事务则为 true，否则为参与现有事务\n\t * @param newSynchronization 若为给定事务开启了新事务同步则为 true\n\t * @param readOnly 事务是否标记为只读\n\t * @param debug 是否为本事务处理启用 debug 日志？\n\t * 在此缓存可避免反复查询日志系统是否启用 debug。\n\t * @param suspendedResources 为本事务挂起的资源的持有者（若有）\n\t * @since 6.1\n\t */",
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
            "\t/**\n\t * Return whether the progress of this transaction is debugged. This is used by\n\t * {@link AbstractPlatformTransactionManager} as an optimization, to prevent repeated\n\t * calls to {@code logger.isDebugEnabled()}. Not really intended for client code.\n\t */",
            "\t/**\n\t * 返回是否 debug 本事务进度。{@link AbstractPlatformTransactionManager} 用作优化，\n\t * 避免反复调用 {@code logger.isDebugEnabled()}。通常不供客户端代码使用。\n\t */",
        ),
        (
            "\t/**\n\t * Return the holder for resources that have been suspended for this transaction,\n\t * if any.\n\t */",
            "\t/**\n\t * 返回为本事务挂起的资源的持有者（若有）。\n\t */",
        ),
        (
            "\t//---------------------------------------------------------------------\n\t// Enable functionality through underlying transaction object\n\t//---------------------------------------------------------------------",
            "\t//---------------------------------------------------------------------\n\t// 通过底层事务对象启用功能\n\t//---------------------------------------------------------------------",
        ),
        (
            "\t/**\n\t * Determine the rollback-only flag via checking the transaction object, provided\n\t * that the latter implements the {@link SmartTransactionObject} interface.\n\t * <p>Will return {@code true} if the global transaction itself has been marked\n\t * rollback-only by the transaction coordinator, for example in case of a timeout.\n\t * @see SmartTransactionObject#isRollbackOnly()\n\t */",
            "\t/**\n\t * 通过检查事务对象确定 rollback-only 标志（前提是其 implements {@link SmartTransactionObject}）。\n\t * <p>若全局事务本身已被事务协调器标记 rollback-only（例如超时），将返回 {@code true}。\n\t * @see SmartTransactionObject#isRollbackOnly()\n\t */",
        ),
        (
            "\t/**\n\t * This implementation exposes the {@link SavepointManager} interface\n\t * of the underlying transaction object, if any.\n\t * @throws NestedTransactionNotSupportedException if savepoints are not supported\n\t * @see #isTransactionSavepointManager()\n\t */",
            "\t/**\n\t * 本实现暴露底层事务对象（若有）的 {@link SavepointManager} 接口。\n\t * @throws NestedTransactionNotSupportedException 若不支持保存点\n\t * @see #isTransactionSavepointManager()\n\t */",
        ),
        (
            "\t/**\n\t * Return whether the underlying transaction implements the {@link SavepointManager}\n\t * interface and therefore supports savepoints.\n\t * @see #getTransaction()\n\t * @see #getSavepointManager()\n\t */",
            "\t/**\n\t * 返回底层事务是否实现 {@link SavepointManager} 接口从而支持保存点。\n\t * @see #getTransaction()\n\t * @see #getSavepointManager()\n\t */",
        ),
        (
            "\t/**\n\t * Delegate the flushing to the transaction object, provided that the latter\n\t * implements the {@link SmartTransactionObject} interface.\n\t * @see SmartTransactionObject#flush()\n\t */",
            "\t/**\n\t * 将 flush 委托给事务对象（前提是其 implements {@link SmartTransactionObject}）。\n\t * @see SmartTransactionObject#flush()\n\t */",
        ),
    ],
}
