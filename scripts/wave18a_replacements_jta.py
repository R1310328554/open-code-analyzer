"""Chinese JavaDoc replacements for springframework wave18a JTA adapters [0:4]."""

JTA_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "SpringJtaSynchronizationAdapter.java": [
        (
            "/**\n * Adapter that implements the JTA {@link jakarta.transaction.Synchronization}\n * interface delegating to an underlying Spring\n * {@link org.springframework.transaction.support.TransactionSynchronization}.\n *\n * <p>Useful for synchronizing Spring resource management code with plain\n * JTA / EJB CMT transactions, despite the original code being built for\n * Spring transaction synchronization.\n *\n * @author Juergen Hoeller\n * @since 2.0\n * @see jakarta.transaction.Transaction#registerSynchronization\n * @see org.springframework.transaction.support.TransactionSynchronization\n */",
            "/**\n * 实现 JTA {@link jakarta.transaction.Synchronization} 接口的适配器，\n * 委托给底层 Spring {@link org.springframework.transaction.support.TransactionSynchronization}。\n *\n * <p>尽管原始代码面向 Spring 事务同步构建，\n * 本适配器仍可用于将 Spring 资源管理代码与纯 JTA / EJB CMT 事务同步。\n *\n * @author Juergen Hoeller\n * @since 2.0\n * @see jakarta.transaction.Transaction#registerSynchronization\n * @see org.springframework.transaction.support.TransactionSynchronization\n */",
        ),
        (
            "\t/**\n\t * Create a new SpringJtaSynchronizationAdapter for the given Spring\n\t * TransactionSynchronization and JTA TransactionManager.\n\t * @param springSynchronization the Spring TransactionSynchronization to delegate to\n\t */",
            "\t/**\n\t * 为给定 Spring TransactionSynchronization 创建新的 SpringJtaSynchronizationAdapter。\n\t * @param springSynchronization 要委托的 Spring TransactionSynchronization\n\t */",
        ),
        (
            "\t/**\n\t * Create a new SpringJtaSynchronizationAdapter for the given Spring\n\t * TransactionSynchronization and JTA TransactionManager.\n\t * @param springSynchronization the Spring TransactionSynchronization to delegate to\n\t * @param jtaUserTransaction the JTA UserTransaction to use for rollback-only\n\t * setting in case of an exception thrown in {@code beforeCompletion}\n\t * @deprecated as of 6.0.12 since JTA 1.1+ requires implicit rollback-only setting\n\t * in case of an exception thrown in {@code beforeCompletion}, so the regular\n\t * {@link #SpringJtaSynchronizationAdapter(TransactionSynchronization)} constructor\n\t * is sufficient for all scenarios\n\t */",
            "\t/**\n\t * 为给定 Spring TransactionSynchronization 和 JTA UserTransaction 创建新的 SpringJtaSynchronizationAdapter。\n\t * @param springSynchronization 要委托的 Spring TransactionSynchronization\n\t * @param jtaUserTransaction 在 {@code beforeCompletion} 抛出异常时用于设置 rollback-only 的 JTA UserTransaction\n\t * @deprecated 自 6.0.12 起弃用，因 JTA 1.1+ 要求在 {@code beforeCompletion} 抛出异常时\n\t * 隐式设置 rollback-only，常规 {@link #SpringJtaSynchronizationAdapter(TransactionSynchronization)} 构造函数\n\t * 已足以覆盖所有场景\n\t */",
        ),
        (
            "\t/**\n\t * Create a new SpringJtaSynchronizationAdapter for the given Spring\n\t * TransactionSynchronization and JTA TransactionManager.\n\t * @param springSynchronization the Spring TransactionSynchronization to delegate to\n\t * @param jtaTransactionManager the JTA TransactionManager to use for rollback-only\n\t * setting in case of an exception thrown in {@code beforeCompletion}\n\t * @deprecated as of 6.0.12 since JTA 1.1+ requires implicit rollback-only setting\n\t * in case of an exception thrown in {@code beforeCompletion}, so the regular\n\t * {@link #SpringJtaSynchronizationAdapter(TransactionSynchronization)} constructor\n\t * is sufficient for all scenarios\n\t */",
            "\t/**\n\t * 为给定 Spring TransactionSynchronization 和 JTA TransactionManager 创建新的 SpringJtaSynchronizationAdapter。\n\t * @param springSynchronization 要委托的 Spring TransactionSynchronization\n\t * @param jtaTransactionManager 在 {@code beforeCompletion} 抛出异常时用于设置 rollback-only 的 JTA TransactionManager\n\t * @deprecated 自 6.0.12 起弃用，因 JTA 1.1+ 要求在 {@code beforeCompletion} 抛出异常时\n\t * 隐式设置 rollback-only，常规 {@link #SpringJtaSynchronizationAdapter(TransactionSynchronization)} 构造函数\n\t * 已足以覆盖所有场景\n\t */",
        ),
        (
            "\t/**\n\t * JTA {@code beforeCompletion} callback: just invoked before commit.\n\t * <p>In case of an exception, the JTA transaction will be marked as rollback-only.\n\t * @see org.springframework.transaction.support.TransactionSynchronization#beforeCommit\n\t */",
            "\t/**\n\t * JTA {@code beforeCompletion} 回调：在提交前调用。\n\t * <p>若发生异常，JTA 事务将被标记为 rollback-only。\n\t * @see org.springframework.transaction.support.TransactionSynchronization#beforeCommit\n\t */",
        ),
        (
            "\t\t\t// Process Spring's beforeCompletion early, in order to avoid issues\n\t\t\t// with strict JTA implementations that issue warnings when doing JDBC\n\t\t\t// operations after transaction completion (for example, Connection.getWarnings).",
            "\t\t\t// 提前处理 Spring 的 beforeCompletion，以避免严格 JTA 实现在事务完成后\n\t\t\t// 执行 JDBC 操作（例如 Connection.getWarnings）时发出警告的问题。",
        ),
        (
            "\t/**\n\t * Set the underlying JTA transaction to rollback-only.\n\t */",
            "\t/**\n\t * 将底层 JTA 事务设置为 rollback-only。\n\t */",
        ),
        (
            "\t\t\t\t// Probably Hibernate's WebSphereExtendedJTATransactionLookup pseudo JTA stuff...",
            "\t\t\t\t// 可能是 Hibernate 的 WebSphereExtendedJTATransactionLookup 伪 JTA 实现...",
        ),
        (
            "\t/**\n\t * JTA {@code afterCompletion} callback: invoked after commit/rollback.\n\t * <p>Needs to invoke the Spring synchronization's {@code beforeCompletion}\n\t * at this late stage in case of a rollback, since there is no corresponding\n\t * callback with JTA.\n\t * @see org.springframework.transaction.support.TransactionSynchronization#beforeCompletion\n\t * @see org.springframework.transaction.support.TransactionSynchronization#afterCompletion\n\t */",
            "\t/**\n\t * JTA {@code afterCompletion} 回调：在提交/回滚后调用。\n\t * <p>若发生回滚，需在此阶段调用 Spring 同步的 {@code beforeCompletion}，\n\t * 因为 JTA 没有对应的回调。\n\t * @see org.springframework.transaction.support.TransactionSynchronization#beforeCompletion\n\t * @see org.springframework.transaction.support.TransactionSynchronization#afterCompletion\n\t */",
        ),
        (
            "\t\t\t// beforeCompletion not called before (probably because of JTA rollback).\n\t\t\t// Perform the cleanup here.",
            "\t\t\t// 之前未调用 beforeCompletion（可能因 JTA 回滚）。\n\t\t\t// 在此执行清理。",
        ),
        (
            "\t\t// Call afterCompletion with the appropriate status indication.",
            "\t\t// 以适当的状态指示调用 afterCompletion。",
        ),
    ],
    "TransactionFactory.java": [
        (
            "/**\n * Strategy interface for creating JTA {@link jakarta.transaction.Transaction}\n * objects based on specified transactional characteristics.\n *\n * <p>The default implementation, {@link SimpleTransactionFactory}, simply\n * wraps a standard JTA {@link jakarta.transaction.TransactionManager}.\n * This strategy interface allows for more sophisticated implementations\n * that adapt to vendor-specific JTA extensions.\n *\n * @author Juergen Hoeller\n * @since 2.5\n * @see jakarta.transaction.TransactionManager#getTransaction()\n * @see SimpleTransactionFactory\n * @see JtaTransactionManager\n */",
            "/**\n * 根据指定事务特性创建 JTA {@link jakarta.transaction.Transaction} 对象的策略接口。\n *\n * <p>默认实现 {@link SimpleTransactionFactory} 简单包装标准 JTA\n * {@link jakarta.transaction.TransactionManager}。\n * 本策略接口允许更复杂的实现以适配厂商特定 JTA 扩展。\n *\n * @author Juergen Hoeller\n * @since 2.5\n * @see jakarta.transaction.TransactionManager#getTransaction()\n * @see SimpleTransactionFactory\n * @see JtaTransactionManager\n */",
        ),
        (
            "\t/**\n\t * Create an active Transaction object based on the given name and timeout.\n\t * @param name the transaction name (may be {@code null})\n\t * @param timeout the transaction timeout (may be -1 for the default timeout)\n\t * @return the active Transaction object (never {@code null})\n\t * @throws NotSupportedException if the transaction manager does not support\n\t * a transaction of the specified type\n\t * @throws SystemException if the transaction manager failed to create the\n\t * transaction\n\t */",
            "\t/**\n\t * 根据给定名称和超时创建活动 Transaction 对象。\n\t * @param name 事务名称（可为 {@code null}）\n\t * @param timeout 事务超时（-1 表示默认超时）\n\t * @return 活动 Transaction 对象（永不为 {@code null}）\n\t * @throws NotSupportedException 若事务管理器不支持指定类型的事务\n\t * @throws SystemException 若事务管理器创建事务失败\n\t */",
        ),
        (
            "\t/**\n\t * Determine whether the underlying transaction manager supports XA transactions\n\t * managed by a resource adapter (i.e. without explicit XA resource enlistment).\n\t * <p>Typically {@code false}. Checked by\n\t * {@link org.springframework.jca.endpoint.AbstractMessageEndpointFactory}\n\t * in order to differentiate between invalid configuration and valid\n\t * ResourceAdapter-managed transactions.\n\t * @see jakarta.resource.spi.ResourceAdapter#endpointActivation\n\t * @see jakarta.resource.spi.endpoint.MessageEndpointFactory#isDeliveryTransacted\n\t */",
            "\t/**\n\t * 判断底层事务管理器是否支持由资源适配器管理的 XA 事务\n\t * （即无需显式登记 XA 资源）。\n\t * <p>通常为 {@code false}。由\n\t * {@link org.springframework.jca.endpoint.AbstractMessageEndpointFactory} 检查，\n\t * 以区分无效配置与有效的 ResourceAdapter 管理事务。\n\t * @see jakarta.resource.spi.ResourceAdapter#endpointActivation\n\t * @see jakarta.resource.spi.endpoint.MessageEndpointFactory#isDeliveryTransacted\n\t */",
        ),
    ],
    "UserTransactionAdapter.java": [
        (
            "/**\n * Adapter for a JTA UserTransaction handle, taking a JTA\n * {@link jakarta.transaction.TransactionManager} reference and creating\n * a JTA {@link jakarta.transaction.UserTransaction} handle for it.\n *\n * <p>The JTA UserTransaction interface is an exact subset of the JTA\n * TransactionManager interface. Unfortunately, it does not serve as\n * super-interface of TransactionManager, though, which requires an\n * adapter such as this class to be used when intending to talk to\n * a TransactionManager handle through the UserTransaction interface.\n *\n * <p>Used internally by Spring's {@link JtaTransactionManager} for certain\n * scenarios. Not intended for direct use in application code.\n *\n * @author Juergen Hoeller\n * @since 1.1.5\n */",
            "/**\n * JTA UserTransaction 句柄的适配器，接受 JTA\n * {@link jakarta.transaction.TransactionManager} 引用并为其创建\n * JTA {@link jakarta.transaction.UserTransaction} 句柄。\n *\n * <p>JTA UserTransaction 接口是 JTA TransactionManager 接口的精确子集。\n * 遗憾的是它并非 TransactionManager 的超接口，\n * 因此需要通过本类这样的适配器，才能以 UserTransaction 接口与 TransactionManager 句柄交互。\n *\n * <p>由 Spring 的 {@link JtaTransactionManager} 在特定场景内部使用。\n * 不供应用代码直接使用。\n *\n * @author Juergen Hoeller\n * @since 1.1.5\n */",
        ),
        (
            "\t/**\n\t * Create a new UserTransactionAdapter for the given TransactionManager.\n\t * @param transactionManager the JTA TransactionManager to wrap\n\t */",
            "\t/**\n\t * 为给定 TransactionManager 创建新的 UserTransactionAdapter。\n\t * @param transactionManager 要包装的 JTA TransactionManager\n\t */",
        ),
        (
            "\t/**\n\t * Return the JTA TransactionManager that this adapter delegates to.\n\t */",
            "\t/**\n\t * 返回本适配器委托的 JTA TransactionManager。\n\t */",
        ),
    ],
    "WebLogicJtaTransactionManager.java": [
        (
            "/**\n * Special {@link JtaTransactionManager} variant for Oracle WebLogic 15.1.1 and higher.\n * Supports the full power of Spring's transaction definitions on WebLogic's\n * transaction coordinator, <i>beyond standard JTA</i>: transaction names,\n * per-transaction isolation levels, and proper resuming of transactions in all cases.\n *\n * <p>Uses WebLogic's special {@code begin(name)} method to start a JTA transaction,\n * in order to make <b>Spring-driven transactions visible in WebLogic's transaction\n * monitor</b>. In case of Spring's declarative transactions, the exposed name will\n * (by default) be the fully-qualified class name + \".\" + method name.\n *\n * <p>Supports a <b>per-transaction isolation level</b> through WebLogic's corresponding\n * JTA transaction property \"ISOLATION LEVEL\". This will apply the specified isolation\n * level (e.g. ISOLATION_SERIALIZABLE) to all JDBC Connections that participate in the\n * given transaction.\n *\n * <p>Invokes WebLogic's special {@code forceResume} method if standard JTA resume\n * failed, to <b>also resume if the target transaction was marked rollback-only</b>.\n * If you're not relying on this feature of transaction suspension in the first\n * place, Spring's standard JtaTransactionManager will behave properly too.\n *\n * <p>By default, the JTA UserTransaction and TransactionManager handles are\n * fetched directly from WebLogic's {@code TransactionHelper}. This can be\n * overridden by specifying \"userTransaction\"/\"userTransactionName\" and\n * \"transactionManager\"/\"transactionManagerName\", passing in existing handles\n * or specifying corresponding JNDI locations to look up.\n *\n * <p>Note: This class was initially removed as of Spring Framework 6.0 but then\n * brought back after the WebLogic 15.1.1 release which finally delivers Jakarta EE 9\n * compatibility. As of Spring Framework 6.2.16, it is available again for manual\n * configuration - as a replacement for the standard {@link JtaTransactionManager}.\n *\n * @author Juergen Hoeller\n * @since 6.2.16\n * @see org.springframework.transaction.TransactionDefinition#getName()\n * @see org.springframework.transaction.TransactionDefinition#getIsolationLevel()\n */",
            "/**\n * 适用于 Oracle WebLogic 15.1.1 及更高版本的特殊 {@link JtaTransactionManager} 变体。\n * 在 WebLogic 事务协调器上支持 Spring 事务定义的全部能力，<i>超越标准 JTA</i>：\n * 事务名称、每事务隔离级别，以及在所有情况下正确恢复事务。\n *\n * <p>使用 WebLogic 特殊的 {@code begin(name)} 方法启动 JTA 事务，\n * 以便<b>使 Spring 驱动的事务在 WebLogic 事务监视器中可见</b>。\n * 对于 Spring 声明式事务，暴露的名称（默认）为全限定类名 + \".\" + 方法名。\n *\n * <p>通过 WebLogic 对应的 JTA 事务属性 \"ISOLATION LEVEL\" 支持<b>每事务隔离级别</b>。\n * 这将把指定隔离级别（如 ISOLATION_SERIALIZABLE）应用于参与该事务的所有 JDBC Connection。\n *\n * <p>若标准 JTA resume 失败，则调用 WebLogic 特殊的 {@code forceResume} 方法，\n * 以<b>在目标事务被标记 rollback-only 时也能恢复</b>。\n * 若本就不依赖事务挂起的此特性，Spring 标准 JtaTransactionManager 也能正常工作。\n *\n * <p>默认情况下，JTA UserTransaction 和 TransactionManager 句柄\n * 直接从 WebLogic 的 {@code TransactionHelper} 获取。\n * 可通过指定 \"userTransaction\"/\"userTransactionName\" 和\n * \"transactionManager\"/\"transactionManagerName\" 覆盖，传入现有句柄\n * 或指定相应 JNDI 查找位置。\n *\n * <p>注意：本类在 Spring Framework 6.0 中曾移除，\n * 在 WebLogic 15.1.1 发布（最终提供 Jakarta EE 9 兼容性）后重新引入。\n * 自 Spring Framework 6.2.16 起，可再次手动配置——作为标准 {@link JtaTransactionManager} 的替代。\n *\n * @author Juergen Hoeller\n * @since 6.2.16\n * @see org.springframework.transaction.TransactionDefinition#getName()\n * @see org.springframework.transaction.TransactionDefinition#getIsolationLevel()\n */",
        ),
        (
            "\t\t\t// Obtain WebLogic ClientTransactionManager interface.",
            "\t\t\t// 获取 WebLogic ClientTransactionManager 接口。",
        ),
        (
            "\t\t// Apply transaction name (if any) to WebLogic transaction.",
            "\t\t// 将事务名称（若有）应用到 WebLogic 事务。",
        ),
        (
            "\t\t\t// No WebLogic UserTransaction available or no transaction name specified\n\t\t\t// -> standard JTA begin call.",
            "\t\t\t// 无 WebLogic UserTransaction 或未指定事务名称\n\t\t\t// -> 使用标准 JTA begin 调用。",
        ),
        (
            "\t\t// Specify isolation level, if any, through corresponding WebLogic transaction property.",
            "\t\t// 若有隔离级别，通过相应 WebLogic 事务属性指定。",
        ),
        (
            "\t\t\t// No name specified - standard JTA is sufficient.",
            "\t\t\t// 未指定名称 - 标准 JTA 已足够。",
        ),
    ],
}
