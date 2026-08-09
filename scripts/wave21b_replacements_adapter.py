"""Chinese JavaDoc replacements for springframework wave21b adapter package [8:19]."""

ADAPTER_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "AdvisorAdapter.java": [
        (
            "/**\n * Interface allowing extension to the Spring AOP framework to allow\n * handling of new Advisors and Advice types.\n *\n * <p>Implementing objects can create AOP Alliance Interceptors from\n * custom advice types, enabling these advice types to be used\n * in the Spring AOP framework, which uses interception under the covers.\n *\n * <p>There is no need for most Spring users to implement this interface;\n * do so only if you need to introduce more Advisor or Advice types to Spring.\n *\n * @author Rod Johnson\n */",
            "/**\n * 允许扩展 Spring AOP 框架以处理新 Advisor 与 Advice 类型的接口。\n *\n * <p>实现类可将自定义 Advice 类型转换为 AOP Alliance 拦截器，\n * 使这些 Advice 类型可在 Spring AOP 框架（底层基于拦截）中使用。\n *\n * <p>大多数 Spring 用户无需实现此接口；\n * 仅当需要向 Spring 引入更多 Advisor 或 Advice 类型时才需实现。\n *\n * @author Rod Johnson\n */",
        ),
        (
            "\t/**\n\t * Does this adapter understand this advice object? Is it valid to\n\t * invoke the {@code getInterceptors} method with an Advisor that\n\t * contains this advice as an argument?\n\t * @param advice an Advice such as a BeforeAdvice\n\t * @return whether this adapter understands the given advice object\n\t * @see #getInterceptor(org.springframework.aop.Advisor)\n\t * @see org.springframework.aop.BeforeAdvice\n\t */",
            "\t/**\n\t * 本适配器是否理解该 Advice 对象？\n\t * 是否可传入包含此 Advice 的 Advisor 调用 {@code getInterceptors} 方法？\n\t * @param advice 如 BeforeAdvice 之类的 Advice\n\t * @return 本适配器是否理解给定 Advice 对象\n\t * @see #getInterceptor(org.springframework.aop.Advisor)\n\t * @see org.springframework.aop.BeforeAdvice\n\t */",
        ),
        (
            "\t/**\n\t * Return an AOP Alliance MethodInterceptor exposing the behavior of\n\t * the given advice to an interception-based AOP framework.\n\t * <p>Don't worry about any Pointcut contained in the Advisor;\n\t * the AOP framework will take care of checking the pointcut.\n\t * @param advisor the Advisor. The supportsAdvice() method must have\n\t * returned true on this object\n\t * @return an AOP Alliance interceptor for this Advisor. There's\n\t * no need to cache instances for efficiency, as the AOP framework\n\t * caches advice chains.\n\t */",
            "\t/**\n\t * 返回 AOP Alliance MethodInterceptor，将给定 Advice 的行为\n\t * 暴露给基于拦截的 AOP 框架。\n\t * <p>无需关心 Advisor 中的 Pointcut；\n\t * AOP 框架会负责检查切入点。\n\t * @param advisor Advisor；supportsAdvice() 对此对象必须已返回 true\n\t * @return 本 Advisor 对应的 AOP Alliance 拦截器。\n\t * 无需为效率缓存实例，AOP 框架会缓存 Advice 链。\n\t */",
        ),
    ],
    "AdvisorAdapterRegistrationManager.java": [
        (
            "/**\n * BeanPostProcessor that registers {@link AdvisorAdapter} beans in the BeanFactory with\n * an {@link AdvisorAdapterRegistry} (by default the {@link GlobalAdvisorAdapterRegistry}).\n *\n * <p>The only requirement for it to work is that it needs to be defined\n * in application context along with \"non-native\" Spring AdvisorAdapters\n * that need to be \"recognized\" by Spring's AOP framework.\n *\n * @author Dmitriy Kopylenko\n * @author Juergen Hoeller\n * @since 27.02.2004\n * @see #setAdvisorAdapterRegistry\n * @see AdvisorAdapter\n */",
            "/**\n * BeanPostProcessor：将 {@link AdvisorAdapter} Bean 注册到\n * {@link AdvisorAdapterRegistry}（默认为 {@link GlobalAdvisorAdapterRegistry}）。\n *\n * <p>生效的唯一要求是：须在应用上下文中定义，\n * 并与需被 Spring AOP 框架「识别」的非原生 Spring AdvisorAdapter 共存。\n *\n * @author Dmitriy Kopylenko\n * @author Juergen Hoeller\n * @since 27.02.2004\n * @see #setAdvisorAdapterRegistry\n * @see AdvisorAdapter\n */",
        ),
        (
            "\t/**\n\t * Specify the AdvisorAdapterRegistry to register AdvisorAdapter beans with.\n\t * Default is the global AdvisorAdapterRegistry.\n\t * @see GlobalAdvisorAdapterRegistry\n\t */",
            "\t/**\n\t * 指定用于注册 AdvisorAdapter Bean 的 AdvisorAdapterRegistry。\n\t * 默认为全局 AdvisorAdapterRegistry。\n\t * @see GlobalAdvisorAdapterRegistry\n\t */",
        ),
    ],
    "AdvisorAdapterRegistry.java": [
        (
            "/**\n * Interface for registries of Advisor adapters.\n *\n * <p><i>This is an SPI interface, not to be implemented by any Spring user.</i>\n *\n * @author Rod Johnson\n * @author Rob Harrop\n */",
            "/**\n * Advisor 适配器注册表接口。\n *\n * <p><i>此为 SPI 接口，Spring 用户不应实现。</i>\n *\n * @author Rod Johnson\n * @author Rob Harrop\n */",
        ),
        (
            "\t/**\n\t * Return an {@link Advisor} wrapping the given advice.\n\t * <p>Should by default at least support\n\t * {@link org.aopalliance.intercept.MethodInterceptor},\n\t * {@link org.springframework.aop.MethodBeforeAdvice},\n\t * {@link org.springframework.aop.AfterReturningAdvice},\n\t * {@link org.springframework.aop.ThrowsAdvice}.\n\t * @param advice an object that should be an advice\n\t * @return an Advisor wrapping the given advice (never {@code null};\n\t * if the advice parameter is an Advisor, it is to be returned as-is)\n\t * @throws UnknownAdviceTypeException if no registered advisor adapter\n\t * can wrap the supposed advice\n\t */",
            "\t/**\n\t * 返回包装给定 Advice 的 {@link Advisor}。\n\t * <p>默认至少应支持\n\t * {@link org.aopalliance.intercept.MethodInterceptor}、\n\t * {@link org.springframework.aop.MethodBeforeAdvice}、\n\t * {@link org.springframework.aop.AfterReturningAdvice}、\n\t * {@link org.springframework.aop.ThrowsAdvice}。\n\t * @param advice 应为 Advice 的对象\n\t * @return 包装给定 Advice 的 Advisor（永不为 {@code null}；\n\t * 若 advice 参数本身为 Advisor，则原样返回）\n\t * @throws UnknownAdviceTypeException 若无已注册适配器可包装该 Advice\n\t */",
        ),
        (
            "\t/**\n\t * Return an array of AOP Alliance MethodInterceptors to allow use of the\n\t * given Advisor in an interception-based framework.\n\t * <p>Don't worry about the pointcut associated with the {@link Advisor}, if it is\n\t * a {@link org.springframework.aop.PointcutAdvisor}: just return an interceptor.\n\t * @param advisor the Advisor to find an interceptor for\n\t * @return an array of MethodInterceptors to expose this Advisor's behavior\n\t * @throws UnknownAdviceTypeException if the Advisor type is\n\t * not understood by any registered AdvisorAdapter\n\t */",
            "\t/**\n\t * 返回 AOP Alliance MethodInterceptor 数组，\n\t * 使给定 Advisor 可用于基于拦截的框架。\n\t * <p>无需关心 {@link Advisor} 关联的切入点（若为\n\t * {@link org.springframework.aop.PointcutAdvisor}）：直接返回拦截器即可。\n\t * @param advisor 要查找拦截器的 Advisor\n\t * @return 暴露本 Advisor 行为的 MethodInterceptor 数组\n\t * @throws UnknownAdviceTypeException 若无已注册 AdvisorAdapter 理解该 Advisor 类型\n\t */",
        ),
        (
            "\t/**\n\t * Register the given {@link AdvisorAdapter}. Note that it is not necessary to register\n\t * adapters for an AOP Alliance Interceptors or Spring Advices: these must be\n\t * automatically recognized by an {@code AdvisorAdapterRegistry} implementation.\n\t * @param adapter an AdvisorAdapter that understands particular Advisor or Advice types\n\t */",
            "\t/**\n\t * 注册给定 {@link AdvisorAdapter}。\n\t * 注意：AOP Alliance Interceptor 或 Spring Advice 无需注册适配器；\n\t * {@code AdvisorAdapterRegistry} 实现必须自动识别它们。\n\t * @param adapter 理解特定 Advisor 或 Advice 类型的 AdvisorAdapter\n\t */",
        ),
    ],
    "AfterReturningAdviceAdapter.java": [
        (
            "/**\n * Adapter to enable {@link org.springframework.aop.AfterReturningAdvice}\n * to be used in the Spring AOP framework.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n */",
            "/**\n * 适配器：使 {@link org.springframework.aop.AfterReturningAdvice}\n * 可在 Spring AOP 框架中使用。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n */",
        ),
    ],
    "AfterReturningAdviceInterceptor.java": [
        (
            "/**\n * Interceptor to wrap an {@link org.springframework.aop.AfterReturningAdvice}.\n * Used internally by the AOP framework; application developers should not need\n * to use this class directly.\n *\n * @author Rod Johnson\n * @see MethodBeforeAdviceInterceptor\n * @see ThrowsAdviceInterceptor\n */",
            "/**\n * 包装 {@link org.springframework.aop.AfterReturningAdvice} 的拦截器。\n * AOP 框架内部使用；应用开发者通常无需直接使用本类。\n *\n * @author Rod Johnson\n * @see MethodBeforeAdviceInterceptor\n * @see ThrowsAdviceInterceptor\n */",
        ),
        (
            "\t/**\n\t * Create a new AfterReturningAdviceInterceptor for the given advice.\n\t * @param advice the AfterReturningAdvice to wrap\n\t */",
            "\t/**\n\t * 为给定 Advice 创建新的 AfterReturningAdviceInterceptor。\n\t * @param advice 要包装的 AfterReturningAdvice\n\t */",
        ),
    ],
    "DefaultAdvisorAdapterRegistry.java": [
        (
            "/**\n * Default implementation of the {@link AdvisorAdapterRegistry} interface.\n * Supports {@link org.aopalliance.intercept.MethodInterceptor},\n * {@link org.springframework.aop.MethodBeforeAdvice},\n * {@link org.springframework.aop.AfterReturningAdvice},\n * {@link org.springframework.aop.ThrowsAdvice}.\n *\n * @author Rod Johnson\n * @author Rob Harrop\n * @author Juergen Hoeller\n */",
            "/**\n * {@link AdvisorAdapterRegistry} 接口的默认实现。\n * 支持 {@link org.aopalliance.intercept.MethodInterceptor}、\n * {@link org.springframework.aop.MethodBeforeAdvice}、\n * {@link org.springframework.aop.AfterReturningAdvice}、\n * {@link org.springframework.aop.ThrowsAdvice}。\n *\n * @author Rod Johnson\n * @author Rob Harrop\n * @author Juergen Hoeller\n */",
        ),
        (
            "\t/**\n\t * Create a new DefaultAdvisorAdapterRegistry, registering well-known adapters.\n\t */",
            "\t/**\n\t * 创建新的 DefaultAdvisorAdapterRegistry，并注册已知适配器。\n\t */",
        ),
        (
            "\t\t\t// So well-known it doesn't even need an adapter.",
            "\t\t\t// 过于常见，甚至无需适配器。",
        ),
        (
            "\t\t\t// Check that it is supported.",
            "\t\t\t// 检查是否受支持。",
        ),
    ],
    "GlobalAdvisorAdapterRegistry.java": [
        (
            "/**\n * Singleton to publish a shared DefaultAdvisorAdapterRegistry instance.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @author Phillip Webb\n * @see DefaultAdvisorAdapterRegistry\n */",
            "/**\n * 单例：发布共享的 DefaultAdvisorAdapterRegistry 实例。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @author Phillip Webb\n * @see DefaultAdvisorAdapterRegistry\n */",
        ),
        (
            "\t/**\n\t * Keep track of a single instance so we can return it to classes that request it.\n\t */",
            "\t/**\n\t * 维护单一实例，以便返回给请求的类。\n\t */",
        ),
        (
            "\t/**\n\t * Return the singleton {@link DefaultAdvisorAdapterRegistry} instance.\n\t */",
            "\t/**\n\t * 返回单例 {@link DefaultAdvisorAdapterRegistry} 实例。\n\t */",
        ),
        (
            "\t/**\n\t * Reset the singleton {@link DefaultAdvisorAdapterRegistry}, removing any\n\t * {@link AdvisorAdapterRegistry#registerAdvisorAdapter(AdvisorAdapter) registered}\n\t * adapters.\n\t */",
            "\t/**\n\t * 重置单例 {@link DefaultAdvisorAdapterRegistry}，\n\t * 移除所有 {@link AdvisorAdapterRegistry#registerAdvisorAdapter(AdvisorAdapter) 已注册}\n\t * 的适配器。\n\t */",
        ),
    ],
    "MethodBeforeAdviceAdapter.java": [
        (
            "/**\n * Adapter to enable {@link org.springframework.aop.MethodBeforeAdvice}\n * to be used in the Spring AOP framework.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n */",
            "/**\n * 适配器：使 {@link org.springframework.aop.MethodBeforeAdvice}\n * 可在 Spring AOP 框架中使用。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n */",
        ),
    ],
    "MethodBeforeAdviceInterceptor.java": [
        (
            "/**\n * Interceptor to wrap a {@link MethodBeforeAdvice}.\n * <p>Used internally by the AOP framework; application developers should not\n * need to use this class directly.\n *\n * @author Rod Johnson\n * @see AfterReturningAdviceInterceptor\n * @see ThrowsAdviceInterceptor\n */",
            "/**\n * 包装 {@link MethodBeforeAdvice} 的拦截器。\n * <p>AOP 框架内部使用；应用开发者通常无需直接使用本类。\n *\n * @author Rod Johnson\n * @see AfterReturningAdviceInterceptor\n * @see ThrowsAdviceInterceptor\n */",
        ),
        (
            "\t/**\n\t * Create a new MethodBeforeAdviceInterceptor for the given advice.\n\t * @param advice the MethodBeforeAdvice to wrap\n\t */",
            "\t/**\n\t * 为给定 Advice 创建新的 MethodBeforeAdviceInterceptor。\n\t * @param advice 要包装的 MethodBeforeAdvice\n\t */",
        ),
    ],
    "ThrowsAdviceAdapter.java": [
        (
            "/**\n * Adapter to enable {@link org.springframework.aop.ThrowsAdvice} to be used\n * in the Spring AOP framework.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n */",
            "/**\n * 适配器：使 {@link org.springframework.aop.ThrowsAdvice}\n * 可在 Spring AOP 框架中使用。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n */",
        ),
    ],
    "ThrowsAdviceInterceptor.java": [
        (
            "/**\n * Interceptor to wrap an after-throwing advice.\n *\n * <p>The signatures on handler methods on the {@code ThrowsAdvice}\n * implementation method argument must be of the form:<br>\n *\n * {@code void afterThrowing([Method, args, target], ThrowableSubclass);}\n *\n * <p>Only the last argument is required.\n *\n * <p>Some examples of valid methods would be:\n *\n * <pre class=\"code\">public void afterThrowing(Exception ex)</pre>\n * <pre class=\"code\">public void afterThrowing(RemoteException)</pre>\n * <pre class=\"code\">public void afterThrowing(Method method, Object[] args, Object target, Exception ex)</pre>\n * <pre class=\"code\">public void afterThrowing(Method method, Object[] args, Object target, ServletException ex)</pre>\n *\n * <p>This is a framework class that need not be used directly by Spring users.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @see MethodBeforeAdviceInterceptor\n * @see AfterReturningAdviceInterceptor\n */",
            "/**\n * 包装异常抛出后 Advice 的拦截器。\n *\n * <p>{@code ThrowsAdvice} 实现中处理方法的签名须为：<br>\n *\n * {@code void afterThrowing([Method, args, target], ThrowableSubclass);}\n *\n * <p>仅最后一个参数为必需。\n *\n * <p>有效方法示例：\n *\n * <pre class=\"code\">public void afterThrowing(Exception ex)</pre>\n * <pre class=\"code\">public void afterThrowing(RemoteException)</pre>\n * <pre class=\"code\">public void afterThrowing(Method method, Object[] args, Object target, Exception ex)</pre>\n * <pre class=\"code\">public void afterThrowing(Method method, Object[] args, Object target, ServletException ex)</pre>\n *\n * <p>框架内部类，Spring 用户无需直接使用。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @see MethodBeforeAdviceInterceptor\n * @see AfterReturningAdviceInterceptor\n */",
        ),
        (
            "\t/** Methods on throws advice, keyed by exception class. */",
            "\t/** 异常抛出 Advice 上的方法，按异常类索引。 */",
        ),
        (
            "\t/**\n\t * Create a new ThrowsAdviceInterceptor for the given ThrowsAdvice.\n\t * @param throwsAdvice the advice object that defines the exception handler methods\n\t * (usually a {@link org.springframework.aop.ThrowsAdvice} implementation)\n\t */",
            "\t/**\n\t * 为给定 ThrowsAdvice 创建新的 ThrowsAdviceInterceptor。\n\t * @param throwsAdvice 定义异常处理方法的对象\n\t *（通常为 {@link org.springframework.aop.ThrowsAdvice} 实现）\n\t */",
        ),
        (
            "\t\t\t\t\t// just a Throwable parameter",
            "\t\t\t\t\t// 仅一个 Throwable 参数",
        ),
        (
            "\t\t\t\t\t// Method, Object[], target, throwable",
            "\t\t\t\t\t// Method、Object[]、target、throwable",
        ),
        (
            "\t\t\t\t// An exception handler to register...",
            "\t\t\t\t// 待注册的异常处理器...",
        ),
        (
            "\t/**\n\t * Return the number of handler methods in this advice.\n\t */",
            "\t/**\n\t * 返回本 Advice 中处理方法的数量。\n\t */",
        ),
        (
            "\t/**\n\t * Determine the exception handle method for the given exception.\n\t * @param exception the exception thrown\n\t * @return a handler for the given exception type, or {@code null} if none found\n\t */",
            "\t/**\n\t * 确定给定异常对应的异常处理方法。\n\t * @param exception 抛出的异常\n\t * @return 给定异常类型的处理器，未找到则返回 {@code null}\n\t */",
        ),
    ],
    "UnknownAdviceTypeException.java": [
        (
            "/**\n * Exception thrown when an attempt is made to use an unsupported\n * Advisor or Advice type.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @see org.aopalliance.aop.Advice\n * @see org.springframework.aop.Advisor\n */",
            "/**\n * 尝试使用不受支持的 Advisor 或 Advice 类型时抛出的异常。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @see org.aopalliance.aop.Advice\n * @see org.springframework.aop.Advisor\n */",
        ),
        (
            "\t/**\n\t * Create a new UnknownAdviceTypeException for the given advice object.\n\t * Will create a message text that says that the object is neither a\n\t * subinterface of Advice nor an Advisor.\n\t * @param advice the advice object of unknown type\n\t */",
            "\t/**\n\t * 为给定 Advice 对象创建新的 UnknownAdviceTypeException。\n\t * 消息文本说明该对象既非 Advice 子接口，也非 Advisor。\n\t * @param advice 未知类型的 Advice 对象\n\t */",
        ),
        (
            "\t/**\n\t * Create a new UnknownAdviceTypeException with the given message.\n\t * @param message the message text\n\t */",
            "\t/**\n\t * 以给定消息创建新的 UnknownAdviceTypeException。\n\t * @param message 消息文本\n\t */",
        ),
    ],
}
