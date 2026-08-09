"""Chinese JavaDoc replacements for springframework wave17a tx interceptor [15:20]."""

TX_INTERCEPTOR_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "AbstractFallbackTransactionAttributeSource.java": [
        (
            "/**\n * Abstract implementation of {@link TransactionAttributeSource} that caches\n * attributes for methods and implements a fallback policy: 1. specific target\n * method; 2. target class; 3. declaring method; 4. declaring class/interface.\n *\n * <p>Defaults to using the target class's transaction attribute if none is\n * associated with the target method. Any transaction attribute associated with\n * the target method completely overrides a class transaction attribute.\n * If none found on the target class, the interface that the invoked method\n * has been called through (in case of a JDK proxy) will be checked.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @since 1.1\n */",
            "/**\n * {@link TransactionAttributeSource} 的抽象实现，缓存方法属性\n * 并实现回退策略：1. 特定目标方法；2. 目标类；3. 声明方法；4. 声明类/接口。\n *\n * <p>若目标方法未关联事务属性，默认使用目标类的事务属性。\n * 目标方法关联的任何事务属性完全覆盖类级事务属性。\n * 若目标类上未找到，将检查调用方法所经过的接口（JDK 代理情况下）。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @since 1.1\n */",
        ),
        (
            "\t/**\n\t * Canonical value held in cache to indicate no transaction attribute was\n\t * found for this method, and we don't need to look again.\n\t */",
            "\t/**\n\t * 缓存中持有的规范值，表示未找到此方法的事务属性，\n\t * 且无需再次查找。\n\t */",
        ),
        (
            "\t/**\n\t * Logger available to subclasses.\n\t * <p>As this base class is not marked Serializable, the logger will be recreated\n\t * after serialization - provided that the concrete subclass is Serializable.\n\t */",
            "\t/**\n\t * 供子类使用的日志记录器。\n\t * <p>由于此基类未标记 Serializable，序列化后日志记录器将重新创建\n\t * ——前提是具体子类可序列化。\n\t */",
        ),
        (
            "\t/**\n\t * Cache of TransactionAttributes, keyed by method on a specific target class.\n\t * <p>As this base class is not marked Serializable, the cache will be recreated\n\t * after serialization - provided that the concrete subclass is Serializable.\n\t */",
            "\t/**\n\t * TransactionAttribute 缓存，以特定目标类上的方法为键。\n\t * <p>由于此基类未标记 Serializable，序列化后缓存将重新创建\n\t * ——前提是具体子类可序列化。\n\t */",
        ),
        (
            "\t/**\n\t * Determine the transaction attribute for this method invocation.\n\t * <p>Defaults to the class's transaction attribute if no method attribute is found.\n\t * @param method the method for the current invocation (never {@code null})\n\t * @param targetClass the target class for this invocation (can be {@code null})\n\t * @param cacheNull whether {@code null} results should be cached as well\n\t * @return a TransactionAttribute for this method, or {@code null} if the method\n\t * is not transactional\n\t */",
            "\t/**\n\t * 确定此方法调用的事务属性。\n\t * <p>若未找到方法属性，默认使用类的事务属性。\n\t * @param method 当前调用的方法（永不为 {@code null}）\n\t * @param targetClass 此调用的目标类（可为 {@code null}）\n\t * @param cacheNull 是否也应缓存 {@code null} 结果\n\t * @return 此方法的事务属性，若方法非事务性则为 {@code null}\n\t */",
        ),
        (
            "\t/**\n\t * Determine a cache key for the given method and target class.\n\t * <p>Must not produce same key for overloaded methods.\n\t * Must produce same key for different instances of the same method.\n\t * @param method the method (never {@code null})\n\t * @param targetClass the target class (may be {@code null})\n\t * @return the cache key (never {@code null})\n\t */",
            "\t/**\n\t * 为给定方法和目标类确定缓存键。\n\t * <p>不得为重载方法产生相同键。\n\t * 必须为同一方法的不同实例产生相同键。\n\t * @param method 方法（永不为 {@code null}）\n\t * @param targetClass 目标类（可为 {@code null}）\n\t * @return 缓存键（永不为 {@code null}）\n\t */",
        ),
        (
            "\t/**\n\t * Same signature as {@link #getTransactionAttribute}, but doesn't cache the result.\n\t * {@link #getTransactionAttribute} is effectively a caching decorator for this method.\n\t * <p>As of 4.1.8, this method can be overridden.\n\t * @since 4.1.8\n\t * @see #getTransactionAttribute\n\t */",
            "\t/**\n\t * 与 {@link #getTransactionAttribute} 签名相同，但不缓存结果。\n\t * {@link #getTransactionAttribute} 实际上是此方法带缓存的装饰器。\n\t * <p>自 4.1.8 起，此方法可被覆盖。\n\t * @since 4.1.8\n\t * @see #getTransactionAttribute\n\t */",
        ),
        (
            "\t\t// Don't allow non-public methods, as configured.",
            "\t\t// 按配置不允许非 public 方法。",
        ),
        (
            "\t\t// Skip setBeanFactory method on BeanFactoryAware.",
            "\t\t// 跳过 BeanFactoryAware 上的 setBeanFactory 方法。",
        ),
        (
            "\t\t// The method may be on an interface, but we need attributes from the target class.",
            "\t\t// 方法可能在接口上，但我们需要来自目标类的属性。",
        ),
        (
            "\t\t// If the target class is null, the method will be unchanged.",
            "\t\t// 若目标类为 null，方法将保持不变。",
        ),
        (
            "\t\t// First try is the method in the target class.",
            "\t\t// 首先尝试目标类中的方法。",
        ),
        (
            "\t\t// Second try is the transaction attribute on the target class.",
            "\t\t// 其次尝试目标类上的事务属性。",
        ),
        (
            "\t\t\t// Fallback is to look at the original method.",
            "\t\t\t// 回退为查看原始方法。",
        ),
        (
            "\t\t\t// Last fallback is the class of the original method.",
            "\t\t\t// 最后回退为原始方法的类。",
        ),
        (
            "\t/**\n\t * Subclasses need to implement this to return the transaction attribute for the\n\t * given class, if any.\n\t * @param clazz the class to retrieve the attribute for\n\t * @return all transaction attribute associated with this class, or {@code null} if none\n\t */",
            "\t/**\n\t * 子类需实现此方法以返回给定类的事务属性（若有）。\n\t * @param clazz 要检索属性的类\n\t * @return 与此类关联的全部事务属性，若无则为 {@code null}\n\t */",
        ),
        (
            "\t/**\n\t * Subclasses need to implement this to return the transaction attribute for the\n\t * given method, if any.\n\t * @param method the method to retrieve the attribute for\n\t * @return all transaction attribute associated with this method, or {@code null} if none\n\t */",
            "\t/**\n\t * 子类需实现此方法以返回给定方法的事务属性（若有）。\n\t * @param method 要检索属性的方法\n\t * @return 与此方法关联的全部事务属性，若无则为 {@code null}\n\t */",
        ),
        (
            "\t/**\n\t * Should only public methods be allowed to have transactional semantics?\n\t * <p>The default implementation returns {@code false}.\n\t */",
            "\t/**\n\t * 是否仅允许 public 方法具有事务语义？\n\t * <p>默认实现返回 {@code false}。\n\t */",
        ),
    ],
    "BeanFactoryTransactionAttributeSourceAdvisor.java": [
        (
            "/**\n * Advisor driven by a {@link TransactionAttributeSource}, used to include\n * a transaction advice bean for methods that are transactional.\n *\n * @author Juergen Hoeller\n * @since 2.5.5\n * @see #setAdviceBeanName\n * @see TransactionInterceptor\n * @see TransactionAttributeSourceAdvisor\n */",
            "/**\n * 由 {@link TransactionAttributeSource} 驱动的 Advisor，\n * 用于为具有事务性的方法包含事务 advice Bean。\n *\n * @author Juergen Hoeller\n * @since 2.5.5\n * @see #setAdviceBeanName\n * @see TransactionInterceptor\n * @see TransactionAttributeSourceAdvisor\n */",
        ),
        (
            "\t/**\n\t * Set the transaction attribute source which is used to find transaction\n\t * attributes. This should usually be identical to the source reference\n\t * set on the transaction interceptor itself.\n\t * @see TransactionInterceptor#setTransactionAttributeSource\n\t */",
            "\t/**\n\t * 设置用于查找事务属性的事务属性源。\n\t * 通常应与事务拦截器本身设置的源引用相同。\n\t * @see TransactionInterceptor#setTransactionAttributeSource\n\t */",
        ),
        (
            "\t/**\n\t * Set the {@link ClassFilter} to use for this pointcut.\n\t * Default is {@link ClassFilter#TRUE}.\n\t */",
            "\t/**\n\t * 设置此切入点使用的 {@link ClassFilter}。\n\t * 默认为 {@link ClassFilter#TRUE}。\n\t */",
        ),
    ],
    "CompositeTransactionAttributeSource.java": [
        (
            "/**\n * Composite {@link TransactionAttributeSource} implementation that iterates\n * over a given array of {@link TransactionAttributeSource} instances.\n *\n * @author Juergen Hoeller\n * @since 2.0\n */",
            "/**\n * 遍历给定 {@link TransactionAttributeSource} 实例数组的\n * 组合式 {@link TransactionAttributeSource} 实现。\n *\n * @author Juergen Hoeller\n * @since 2.0\n */",
        ),
        (
            "\t/**\n\t * Create a new CompositeTransactionAttributeSource for the given sources.\n\t * @param transactionAttributeSources the TransactionAttributeSource instances to combine\n\t */",
            "\t/**\n\t * 为给定源创建新的 CompositeTransactionAttributeSource。\n\t * @param transactionAttributeSources 要组合的 TransactionAttributeSource 实例\n\t */",
        ),
        (
            "\t/**\n\t * Return the TransactionAttributeSource instances that this\n\t * CompositeTransactionAttributeSource combines.\n\t */",
            "\t/**\n\t * 返回此 CompositeTransactionAttributeSource 组合的\n\t * TransactionAttributeSource 实例。\n\t */",
        ),
    ],
    "DefaultTransactionAttribute.java": [
        (
            "/**\n * Spring's common transaction attribute implementation.\n * Rolls back on runtime, but not checked, exceptions by default.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @author Mark Paluch\n * @since 16.03.2003\n */",
            "/**\n * Spring 通用事务属性实现。\n * 默认在运行时异常时回滚，受检异常时不回滚。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @author Mark Paluch\n * @since 16.03.2003\n */",
        ),
        (
            "\t/**\n\t * Create a new {@code DefaultTransactionAttribute} with default settings.\n\t * Can be modified through bean property setters.\n\t * @see #setPropagationBehavior\n\t * @see #setIsolationLevel\n\t * @see #setTimeout\n\t * @see #setReadOnly\n\t * @see #setName\n\t */",
            "\t/**\n\t * 以默认设置创建新的 {@code DefaultTransactionAttribute}。\n\t * 可通过 Bean 属性 setter 修改。\n\t * @see #setPropagationBehavior\n\t * @see #setIsolationLevel\n\t * @see #setTimeout\n\t * @see #setReadOnly\n\t * @see #setName\n\t */",
        ),
        (
            "\t/**\n\t * Copy constructor. Definition can be modified through bean property setters.\n\t * @see #setPropagationBehavior\n\t * @see #setIsolationLevel\n\t * @see #setTimeout\n\t * @see #setReadOnly\n\t * @see #setName\n\t */",
            "\t/**\n\t * 拷贝构造函数。定义可通过 Bean 属性 setter 修改。\n\t * @see #setPropagationBehavior\n\t * @see #setIsolationLevel\n\t * @see #setTimeout\n\t * @see #setReadOnly\n\t * @see #setName\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@code DefaultTransactionAttribute} with the given\n\t * propagation behavior. Can be modified through bean property setters.\n\t * @param propagationBehavior one of the propagation constants in the\n\t * TransactionDefinition interface\n\t * @see #setIsolationLevel\n\t * @see #setTimeout\n\t * @see #setReadOnly\n\t */",
            "\t/**\n\t * 以给定传播行为创建新的 {@code DefaultTransactionAttribute}。\n\t * 可通过 Bean 属性 setter 修改。\n\t * @param propagationBehavior TransactionDefinition 接口中的传播常量之一\n\t * @see #setIsolationLevel\n\t * @see #setTimeout\n\t * @see #setReadOnly\n\t */",
        ),
        (
            "\t/**\n\t * Set a descriptor for this transaction attribute,\n\t * for example, indicating where the attribute is applying.\n\t * @since 4.3.4\n\t */",
            "\t/**\n\t * 设置本事务属性的描述符，\n\t * 例如指示属性适用的位置。\n\t * @since 4.3.4\n\t */",
        ),
        (
            "\t/**\n\t * Return a descriptor for this transaction attribute,\n\t * or {@code null} if none.\n\t * @since 4.3.4\n\t */",
            "\t/**\n\t * 返回本事务属性的描述符，\n\t * 若无则为 {@code null}。\n\t * @since 4.3.4\n\t */",
        ),
        (
            "\t/**\n\t * Set the timeout to apply, if any,\n\t * as a String value that resolves to a number of seconds.\n\t * @since 5.3\n\t * @see #setTimeout\n\t * @see #resolveAttributeStrings\n\t */",
            "\t/**\n\t * 设置要应用的超时（若有），\n\t * 为解析为秒数的字符串值。\n\t * @since 5.3\n\t * @see #setTimeout\n\t * @see #resolveAttributeStrings\n\t */",
        ),
        (
            "\t/**\n\t * Return the timeout to apply, if any,\n\t * as a String value that resolves to a number of seconds.\n\t * @since 5.3\n\t * @see #getTimeout\n\t * @see #resolveAttributeStrings\n\t */",
            "\t/**\n\t * 返回要应用的超时（若有），\n\t * 为解析为秒数的字符串值。\n\t * @since 5.3\n\t * @see #getTimeout\n\t * @see #resolveAttributeStrings\n\t */",
        ),
        (
            "\t/**\n\t * Associate a qualifier value with this transaction attribute.\n\t * <p>This may be used for choosing a corresponding transaction manager\n\t * to process this specific transaction.\n\t * @since 3.0\n\t * @see #resolveAttributeStrings\n\t */",
            "\t/**\n\t * 将限定符值与本事务属性关联。\n\t * <p>可用于选择相应的事务管理器处理此特定事务。\n\t * @since 3.0\n\t * @see #resolveAttributeStrings\n\t */",
        ),
        (
            "\t/**\n\t * Return a qualifier value associated with this transaction attribute.\n\t * @since 3.0\n\t */",
            "\t/**\n\t * 返回与本事务属性关联的限定符值。\n\t * @since 3.0\n\t */",
        ),
        (
            "\t/**\n\t * Associate one or more labels with this transaction attribute.\n\t * <p>This may be used for applying specific transactional behavior\n\t * or follow a purely descriptive nature.\n\t * @since 5.3\n\t * @see #resolveAttributeStrings\n\t */",
            "\t/**\n\t * 将一个或多个标签与本事务属性关联。\n\t * <p>可用于应用特定事务行为或纯描述用途。\n\t * @since 5.3\n\t * @see #resolveAttributeStrings\n\t */",
        ),
        (
            "\t/**\n\t * The default behavior is as with EJB: rollback on unchecked exception\n\t * ({@link RuntimeException}), assuming an unexpected outcome outside any\n\t * business rules. Additionally, we also attempt to rollback on {@link Error} which\n\t * is clearly an unexpected outcome as well. By contrast, a checked exception is\n\t * considered a business exception and therefore a regular expected outcome of the\n\t * transactional business method, i.e. a kind of alternative return value which\n\t * still allows for regular completion of resource operations.\n\t * <p>This is largely consistent with TransactionTemplate's default behavior,\n\t * except that TransactionTemplate also rolls back on undeclared checked exceptions\n\t * (a corner case). For declarative transactions, we expect checked exceptions to be\n\t * intentionally declared as business exceptions, leading to a commit by default.\n\t * @see org.springframework.transaction.support.TransactionTemplate#execute\n\t */",
            "\t/**\n\t * 默认行为与 EJB 相同：在未检查异常（{@link RuntimeException}）时回滚，\n\t * 假定超出任何业务规则的意外结果。此外，我们也尝试在 {@link Error} 时回滚，\n\t * 这同样是明确的意外结果。相比之下，受检异常被视为业务异常，\n\t * 因此是事务性业务方法的常规预期结果，\n\t * 即一种仍允许资源操作正常完成的替代返回值。\n\t * <p>这与 TransactionTemplate 的默认行为大体一致，\n\t * 但 TransactionTemplate 也会在未声明的受检异常时回滚（边界情况）。\n\t * 对于声明式事务，我们预期受检异常被有意声明为业务异常，默认导致提交。\n\t * @see org.springframework.transaction.support.TransactionTemplate#execute\n\t */",
        ),
        (
            "\t/**\n\t * Resolve attribute values that are defined as resolvable Strings:\n\t * {@link #setTimeoutString}, {@link #setQualifier}, {@link #setLabels}.\n\t * This is typically used for resolving \"${...}\" placeholders.\n\t * @param resolver the embedded value resolver to apply, if any\n\t * @since 5.3\n\t */",
            "\t/**\n\t * 解析定义为可解析字符串的属性值：\n\t * {@link #setTimeoutString}、{@link #setQualifier}、{@link #setLabels}。\n\t * 通常用于解析 \"${...}\" 占位符。\n\t * @param resolver 要应用的内嵌值解析器（若有）\n\t * @since 5.3\n\t */",
        ),
        (
            "\t/**\n\t * Return an identifying description for this transaction attribute.\n\t * <p>Available to subclasses, for inclusion in their {@code toString()} result.\n\t */",
            "\t/**\n\t * 返回本事务属性的标识描述。\n\t * <p>供子类使用，以包含在其 {@code toString()} 结果中。\n\t */",
        ),
    ],
    "DelegatingTransactionAttribute.java": [
        (
            "/**\n * {@link TransactionAttribute} implementation that delegates all calls to a given target\n * {@link TransactionAttribute} instance. Abstract because it is meant to be subclassed,\n * with subclasses overriding specific methods that are not supposed to simply delegate\n * to the target instance.\n *\n * @author Juergen Hoeller\n * @author Mark Paluch\n * @since 1.2\n */",
            "/**\n * 将所有调用委托给给定目标 {@link TransactionAttribute} 实例的\n * {@link TransactionAttribute} 实现。为抽象类，旨在被继承，\n * 子类覆盖不应简单委托给目标实例的特定方法。\n *\n * @author Juergen Hoeller\n * @author Mark Paluch\n * @since 1.2\n */",
        ),
        (
            "\t/**\n\t * Create a DelegatingTransactionAttribute for the given target attribute.\n\t * @param targetAttribute the target TransactionAttribute to delegate to\n\t */",
            "\t/**\n\t * 为给定目标属性创建 DelegatingTransactionAttribute。\n\t * @param targetAttribute 要委托的目标 TransactionAttribute\n\t */",
        ),
    ],
}
