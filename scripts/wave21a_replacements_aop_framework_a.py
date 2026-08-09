"""Chinese JavaDoc replacements for springframework wave21a AOP framework [5:20] part A."""

AOP_FRAMEWORK_A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "AbstractAdvisingBeanPostProcessor.java": [
        (
            "/**\n * Base class for {@link BeanPostProcessor} implementations that apply a\n * Spring AOP {@link Advisor} to specific beans.\n *\n * @author Juergen Hoeller\n * @since 3.2\n */",
            "/**\n * 将 Spring AOP {@link Advisor} 应用于特定 Bean 的\n * {@link BeanPostProcessor} 实现基类。\n *\n * @author Juergen Hoeller\n * @since 3.2\n */",
        ),
        (
            "\t/**\n\t * Set whether this post-processor's advisor is supposed to apply before\n\t * existing advisors when encountering a pre-advised object.\n\t * <p>Default is \"false\", applying the advisor after existing advisors, i.e.\n\t * as close as possible to the target method. Switch this to \"true\" in order\n\t * for this post-processor's advisor to wrap existing advisors as well.\n\t * <p>Note: Check the concrete post-processor's javadoc whether it possibly\n\t * changes this flag by default, depending on the nature of its advisor.\n\t */",
            "\t/**\n\t * 设置遇到已预通知对象时，本后置处理器的通知器是否应排在现有通知器之前。\n\t * <p>默认为 \"false\"，即在现有通知器之后应用，尽量靠近目标方法。\n\t * 设为 \"true\" 时，本后置处理器的通知器也会包裹现有通知器。\n\t * <p>注意：请查阅具体后置处理器的 JavaDoc，其可能根据通知器性质默认修改此标志。\n\t */",
        ),
        (
            "\t\t\t// Use original ClassLoader if bean class not locally loaded in overriding class loader",
            "\t\t\t// 若 Bean 类未在覆盖类加载器中本地加载，则使用原始 ClassLoader",
        ),
        (
            "\t\t\t// Ignore AOP infrastructure such as scoped proxies.",
            "\t\t\t// 忽略 AOP 基础设施，例如作用域代理。",
        ),
        (
            "\t\t\t\t// Add our local Advisor to the existing proxy's Advisor chain.",
            "\t\t\t\t// 将本地通知器加入现有代理的通知器链。",
        ),
        (
            "\t\t\t\t\t// No target, leave last Advisor in place and add new Advisor right before.",
            "\t\t\t\t\t// 无目标对象，保留末尾通知器，并在其前插入新通知器。",
        ),
        (
            "\t\t// No proxy needed.",
            "\t\t// 无需代理。",
        ),
        (
            "\t/**\n\t * Check whether the given bean is eligible for advising with this\n\t * post-processor's {@link Advisor}.\n\t * <p>Delegates to {@link #isEligible(Class)} for target class checking.\n\t * Can be overridden, for example, to specifically exclude certain beans by name.\n\t * <p>Note: Only called for regular bean instances but not for existing\n\t * proxy instances which implement {@link Advised} and allow for adding\n\t * the local {@link Advisor} to the existing proxy's {@link Advisor} chain.\n\t * For the latter, {@link #isEligible(Class)} is being called directly,\n\t * with the actual target class behind the existing proxy (as determined\n\t * by {@link AopUtils#getTargetClass(Object)}).\n\t * @param bean the bean instance\n\t * @param beanName the name of the bean\n\t * @see #isEligible(Class)\n\t */",
            "\t/**\n\t * 检查给定 Bean 是否适合用本后置处理器的 {@link Advisor} 进行通知。\n\t * <p>目标类检查委托给 {@link #isEligible(Class)}。\n\t * 可覆盖，例如按名称排除特定 Bean。\n\t * <p>注意：仅对普通 Bean 实例调用，不对已实现 {@link Advised}、\n\t * 允许将本地 {@link Advisor} 加入现有代理 {@link Advisor} 链的代理实例调用。\n\t * 后者直接调用 {@link #isEligible(Class)}，\n\t * 使用现有代理背后的实际目标类（由 {@link AopUtils#getTargetClass(Object)} 确定）。\n\t * @param bean Bean 实例\n\t * @param beanName Bean 名称\n\t * @see #isEligible(Class)\n\t */",
        ),
        (
            "\t/**\n\t * Check whether the given class is eligible for advising with this\n\t * post-processor's {@link Advisor}.\n\t * <p>Implements caching of {@code canApply} results per bean target class.\n\t * @param targetClass the class to check against\n\t * @see AopUtils#canApply(Advisor, Class)\n\t */",
            "\t/**\n\t * 检查给定类是否适合用本后置处理器的 {@link Advisor} 进行通知。\n\t * <p>按 Bean 目标类缓存 {@code canApply} 结果。\n\t * @param targetClass 待检查的类\n\t * @see AopUtils#canApply(Advisor, Class)\n\t */",
        ),
        (
            "\t/**\n\t * Prepare a {@link ProxyFactory} for the given bean.\n\t * <p>Subclasses may customize the handling of the target instance and in\n\t * particular the exposure of the target class. The default introspection\n\t * of interfaces for non-target-class proxies and the configured advisor\n\t * will be applied afterwards; {@link #customizeProxyFactory} allows for\n\t * late customizations of those parts right before proxy creation.\n\t * @param bean the bean instance to create a proxy for\n\t * @param beanName the corresponding bean name\n\t * @return the ProxyFactory, initialized with this processor's\n\t * {@link ProxyConfig} settings and the specified bean\n\t * @since 4.2.3\n\t * @see #customizeProxyFactory\n\t */",
            "\t/**\n\t * 为给定 Bean 准备 {@link ProxyFactory}。\n\t * <p>子类可定制目标实例处理，尤其是目标类的暴露方式。\n\t * 之后将应用非 target-class 代理的默认接口内省及已配置的通知器；\n\t * {@link #customizeProxyFactory} 可在创建代理前对这些部分做最后定制。\n\t * @param bean 待创建代理的 Bean 实例\n\t * @param beanName 对应的 Bean 名称\n\t * @return 已用本处理器的 {@link ProxyConfig} 设置及指定 Bean 初始化的 ProxyFactory\n\t * @since 4.2.3\n\t * @see #customizeProxyFactory\n\t */",
        ),
        (
            "\t/**\n\t * Subclasses may choose to implement this: for example,\n\t * to change the interfaces exposed.\n\t * <p>The default implementation is empty.\n\t * @param proxyFactory the ProxyFactory that is already configured with\n\t * target, advisor and interfaces and will be used to create the proxy\n\t * immediately after this method returns\n\t * @since 4.2.3\n\t * @see #prepareProxyFactory\n\t */",
            "\t/**\n\t * 子类可选择实现本方法，例如修改暴露的接口。\n\t * <p>默认实现为空。\n\t * @param proxyFactory 已配置目标、通知器与接口、\n\t * 本方法返回后将立即用于创建代理的 ProxyFactory\n\t * @since 4.2.3\n\t * @see #prepareProxyFactory\n\t */",
        ),
    ],
    "AbstractSingletonProxyFactoryBean.java": [
        (
            "/**\n * Convenient superclass for {@link FactoryBean} types that produce singleton-scoped\n * proxy objects.\n *\n * <p>Manages pre- and post-interceptors (references, rather than\n * interceptor names, as in {@link ProxyFactoryBean}) and provides\n * consistent interface management.\n *\n * @author Juergen Hoeller\n * @since 2.0\n */",
            "/**\n * 生产单例作用域代理对象的 {@link FactoryBean} 类型的便捷超类。\n *\n * <p>管理前置与后置拦截器（引用形式，而非 {@link ProxyFactoryBean} 中的拦截器名称），\n * 并提供一致的接口管理。\n *\n * @author Juergen Hoeller\n * @since 2.0\n */",
        ),
        (
            "\t/** Default is global AdvisorAdapterRegistry. */",
            "\t/** 默认为全局 AdvisorAdapterRegistry。 */",
        ),
        (
            "\t/**\n\t * Set the target object, that is, the bean to be wrapped with a transactional proxy.\n\t * <p>The target may be any object, in which case a SingletonTargetSource will\n\t * be created. If it is a TargetSource, no wrapper TargetSource is created:\n\t * This enables the use of a pooling or prototype TargetSource etc.\n\t * @see org.springframework.aop.TargetSource\n\t * @see org.springframework.aop.target.SingletonTargetSource\n\t * @see org.springframework.aop.target.LazyInitTargetSource\n\t * @see org.springframework.aop.target.PrototypeTargetSource\n\t * @see org.springframework.aop.target.CommonsPool2TargetSource\n\t */",
            "\t/**\n\t * 设置目标对象，即待包装为事务代理的 Bean。\n\t * <p>目标可以是任意对象，此时会创建 SingletonTargetSource。\n\t * 若本身是 TargetSource，则不再包装：从而可使用池化或 prototype TargetSource 等。\n\t * @see org.springframework.aop.TargetSource\n\t * @see org.springframework.aop.target.SingletonTargetSource\n\t * @see org.springframework.aop.target.LazyInitTargetSource\n\t * @see org.springframework.aop.target.PrototypeTargetSource\n\t * @see org.springframework.aop.target.CommonsPool2TargetSource\n\t */",
        ),
        (
            "\t/**\n\t * Specify the set of interfaces being proxied.\n\t * <p>If not specified (the default), the AOP infrastructure works\n\t * out which interfaces need proxying by analyzing the target,\n\t * proxying all the interfaces that the target object implements.\n\t */",
            "\t/**\n\t * 指定被代理的接口集合。\n\t * <p>未指定（默认）时，AOP 基础设施通过分析目标对象\n\t * 确定需代理的接口，代理目标实现的所有接口。\n\t */",
        ),
        (
            "\t/**\n\t * Set additional interceptors (or advisors) to be applied before the\n\t * implicit transaction interceptor, for example, a PerformanceMonitorInterceptor.\n\t * <p>You may specify any AOP Alliance MethodInterceptors or other\n\t * Spring AOP Advices, as well as Spring AOP Advisors.\n\t * @see org.springframework.aop.interceptor.PerformanceMonitorInterceptor\n\t */",
            "\t/**\n\t * 设置隐式事务拦截器之前应用的额外拦截器（或通知器），\n\t * 例如 PerformanceMonitorInterceptor。\n\t * <p>可指定任意 AOP Alliance MethodInterceptor、其他 Spring AOP Advice\n\t * 或 Spring AOP Advisor。\n\t * @see org.springframework.aop.interceptor.PerformanceMonitorInterceptor\n\t */",
        ),
        (
            "\t/**\n\t * Set additional interceptors (or advisors) to be applied after the\n\t * implicit transaction interceptor.\n\t * <p>You may specify any AOP Alliance MethodInterceptors or other\n\t * Spring AOP Advices, as well as Spring AOP Advisors.\n\t */",
            "\t/**\n\t * 设置隐式事务拦截器之后应用的额外拦截器（或通知器）。\n\t * <p>可指定任意 AOP Alliance MethodInterceptor、其他 Spring AOP Advice\n\t * 或 Spring AOP Advisor。\n\t */",
        ),
        (
            "\t/**\n\t * Specify the AdvisorAdapterRegistry to use.\n\t * Default is the global AdvisorAdapterRegistry.\n\t * @see org.springframework.aop.framework.adapter.GlobalAdvisorAdapterRegistry\n\t */",
            "\t/**\n\t * 指定使用的 AdvisorAdapterRegistry。\n\t * 默认为全局 AdvisorAdapterRegistry。\n\t * @see org.springframework.aop.framework.adapter.GlobalAdvisorAdapterRegistry\n\t */",
        ),
        (
            "\t/**\n\t * Set the ClassLoader to generate the proxy class in.\n\t * <p>Default is the bean ClassLoader, i.e. the ClassLoader used by the\n\t * containing BeanFactory for loading all bean classes. This can be\n\t * overridden here for specific proxies.\n\t */",
            "\t/**\n\t * 设置生成代理类所用的 ClassLoader。\n\t * <p>默认为 Bean 的 ClassLoader，即容器 BeanFactory 加载所有 Bean 类所用的 ClassLoader。\n\t * 可在此为特定代理覆盖。\n\t */",
        ),
        (
            "\t\t// Add the main interceptor (typically an Advisor).",
            "\t\t// 添加主拦截器（通常为 Advisor）。",
        ),
        (
            "\t\t\t// Rely on AOP infrastructure to tell us what interfaces to proxy.",
            "\t\t\t// 依赖 AOP 基础设施确定需代理的接口。",
        ),
        (
            "\t/**\n\t * Determine a TargetSource for the given target (or TargetSource).\n\t * @param target the target. If this is an implementation of TargetSource it is\n\t * used as our TargetSource; otherwise it is wrapped in a SingletonTargetSource.\n\t * @return a TargetSource for this object\n\t */",
            "\t/**\n\t * 为给定目标（或 TargetSource）确定 TargetSource。\n\t * @param target 目标对象。若为实现 TargetSource 则直接使用；\n\t * 否则包装为 SingletonTargetSource。\n\t * @return 该对象的 TargetSource\n\t */",
        ),
        (
            "\t/**\n\t * A hook for subclasses to post-process the {@link ProxyFactory}\n\t * before creating the proxy instance with it.\n\t * @param proxyFactory the AOP ProxyFactory about to be used\n\t * @since 4.2\n\t */",
            "\t/**\n\t * 供子类在创建代理实例前对 {@link ProxyFactory} 进行后处理的钩子。\n\t * @param proxyFactory 即将使用的 AOP ProxyFactory\n\t * @since 4.2\n\t */",
        ),
        (
            "\t/**\n\t * Create the \"main\" interceptor for this proxy factory bean.\n\t * Typically an Advisor, but can also be any type of Advice.\n\t * <p>Pre-interceptors will be applied before, post-interceptors\n\t * will be applied after this interceptor.\n\t */",
            "\t/**\n\t * 为本代理工厂 Bean 创建「主」拦截器。\n\t * 通常为 Advisor，也可为任意类型的 Advice。\n\t * <p>前置拦截器在其之前应用，后置拦截器在其之后应用。\n\t */",
        ),
    ],
    "AdvisedSupportListener.java": [
        (
            "/**\n * Listener to be registered on {@link ProxyCreatorSupport} objects\n * Allows for receiving callbacks on activation and change of advice.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @see ProxyCreatorSupport#addListener\n */",
            "/**\n * 注册在 {@link ProxyCreatorSupport} 对象上的监听器，\n * 可在激活及 advice 变更时接收回调。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @see ProxyCreatorSupport#addListener\n */",
        ),
        (
            "\t/**\n\t * Invoked when the first proxy is created.\n\t * @param advised the AdvisedSupport object\n\t */",
            "\t/**\n\t * 创建第一个代理时调用。\n\t * @param advised AdvisedSupport 对象\n\t */",
        ),
        (
            "\t/**\n\t * Invoked when advice is changed after a proxy is created.\n\t * @param advised the AdvisedSupport object\n\t */",
            "\t/**\n\t * 代理创建后 advice 发生变更时调用。\n\t * @param advised AdvisedSupport 对象\n\t */",
        ),
    ],
    "AdvisorChainFactory.java": [
        (
            "/**\n * Factory interface for advisor chains.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n */",
            "/**\n * 通知器链的工厂接口。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n */",
        ),
        (
            "\t/**\n\t * Determine a list of {@link org.aopalliance.intercept.MethodInterceptor} objects\n\t * for the given advisor chain configuration.\n\t * @param config the AOP configuration in the form of an Advised object\n\t * @param method the proxied method\n\t * @param targetClass the target class (may be {@code null} to indicate a proxy without\n\t * target object, in which case the method's declaring class is the next best option)\n\t * @return a List of MethodInterceptors (may also include InterceptorAndDynamicMethodMatchers)\n\t */",
            "\t/**\n\t * 为给定通知器链配置确定 {@link org.aopalliance.intercept.MethodInterceptor} 列表。\n\t * @param config 以 Advised 对象形式表示的 AOP 配置\n\t * @param method 被代理的方法\n\t * @param targetClass 目标类（可为 {@code null} 表示无目标对象的代理，\n\t * 此时方法的声明类为次优选择）\n\t * @return MethodInterceptor 列表（也可能包含 InterceptorAndDynamicMethodMatcher）\n\t */",
        ),
    ],
    "AopConfigException.java": [
        (
            "/**\n * Exception that gets thrown on illegal AOP configuration arguments.\n *\n * @author Rod Johnson\n * @since 13.03.2003\n */",
            "/**\n * 非法 AOP 配置参数时抛出的异常。\n *\n * @author Rod Johnson\n * @since 13.03.2003\n */",
        ),
        (
            "\t/**\n\t * Constructor for AopConfigException.\n\t * @param msg the detail message\n\t */",
            "\t/**\n\t * 构造 AopConfigException。\n\t * @param msg 详细消息\n\t */",
        ),
        (
            "\t/**\n\t * Constructor for AopConfigException.\n\t * @param msg the detail message\n\t * @param cause the root cause\n\t */",
            "\t/**\n\t * 构造 AopConfigException。\n\t * @param msg 详细消息\n\t * @param cause 根本原因\n\t */",
        ),
    ],
    "AopContext.java": [
        (
            "/**\n * Class containing static methods used to obtain information about the current AOP invocation.\n *\n * <p>The {@code currentProxy()} method is usable if the AOP framework is configured to\n * expose the current proxy (not the default). It returns the AOP proxy in use. Target objects\n * or advice can use this to make advised calls, in the same way as {@code getEJBObject()}\n * can be used in EJBs. They can also use it to find advice configuration.\n *\n * <p>Spring's AOP framework does not expose proxies by default, as there is a performance cost\n * in doing so.\n *\n * <p>The functionality in this class might be used by a target object that needed access\n * to resources on the invocation. However, this approach should not be used when there is\n * a reasonable alternative, as it makes application code dependent on usage under AOP and\n * the Spring AOP framework in particular.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @since 13.03.2003\n */",
            "/**\n * 包含用于获取当前 AOP 调用信息的静态方法的类。\n *\n * <p>若 AOP 框架配置为暴露当前代理（非默认），则 {@code currentProxy()} 可用。\n * 它返回正在使用的 AOP 代理。目标对象或 advice 可借此发起带通知的调用，\n * 类似 EJB 中的 {@code getEJBObject()}，也可用于查找 advice 配置。\n *\n * <p>Spring AOP 默认不暴露代理，因存在性能开销。\n *\n * <p>需要访问调用上下文中资源的目标对象可能使用本类功能。\n * 但有合理替代方案时不应使用，以免应用代码依赖 AOP 及 Spring AOP 框架。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @since 13.03.2003\n */",
        ),
        (
            "\t/**\n\t * ThreadLocal holder for AOP proxy associated with this thread.\n\t * Will contain {@code null} unless the \"exposeProxy\" property on\n\t * the controlling proxy configuration has been set to \"true\".\n\t * @see ProxyConfig#setExposeProxy\n\t */",
            "\t/**\n\t * 与本线程关联的 AOP 代理的 ThreadLocal 持有者。\n\t * 除非控制代理配置的 \"exposeProxy\" 属性设为 \"true\"，否则为 {@code null}。\n\t * @see ProxyConfig#setExposeProxy\n\t */",
        ),
        (
            "\t/**\n\t * Try to return the current AOP proxy. This method is usable only if the\n\t * calling method has been invoked via AOP, and the AOP framework has been set\n\t * to expose proxies. Otherwise, this method will throw an IllegalStateException.\n\t * @return the current AOP proxy (never returns {@code null})\n\t * @throws IllegalStateException if the proxy cannot be found, because the\n\t * method was invoked outside an AOP invocation context, or because the\n\t * AOP framework has not been configured to expose the proxy\n\t */",
            "\t/**\n\t * 尝试返回当前 AOP 代理。仅当调用方法通过 AOP 调用且框架已配置暴露代理时可用。\n\t * 否则抛出 IllegalStateException。\n\t * @return 当前 AOP 代理（永不返回 {@code null}）\n\t * @throws IllegalStateException 若找不到代理，因方法在 AOP 调用上下文外调用，\n\t * 或 AOP 框架未配置暴露代理\n\t */",
        ),
        (
            "\t/**\n\t * Make the given proxy available via the {@code currentProxy()} method.\n\t * <p>Note that the caller should be careful to keep the old value as appropriate.\n\t * @param proxy the proxy to expose (or {@code null} to reset it)\n\t * @return the old proxy, which may be {@code null} if none was bound\n\t * @see #currentProxy()\n\t */",
            "\t/**\n\t * 通过 {@code currentProxy()} 方法使给定代理可用。\n\t * <p>注意：调用方应妥善保留旧值。\n\t * @param proxy 要暴露的代理（或 {@code null} 以重置）\n\t * @return 旧代理，未绑定时可为 {@code null}\n\t * @see #currentProxy()\n\t */",
        ),
    ],
    "AopInfrastructureBean.java": [
        (
            "/**\n * Marker interface that indicates a bean that is part of Spring's\n * AOP infrastructure. In particular, this implies that any such bean\n * is not subject to auto-proxying, even if a pointcut would match.\n *\n * @author Juergen Hoeller\n * @since 2.0.3\n * @see org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator\n * @see org.springframework.aop.scope.ScopedProxyFactoryBean\n */",
            "/**\n * 标记属于 Spring AOP 基础设施的 Bean 的接口。\n * 特别地，此类 Bean 不会自动代理，即使切入点匹配亦然。\n *\n * @author Juergen Hoeller\n * @since 2.0.3\n * @see org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator\n * @see org.springframework.aop.scope.ScopedProxyFactoryBean\n */",
        ),
    ],
    "AopProxy.java": [
        (
            "/**\n * Delegate interface for a configured AOP proxy, allowing for the creation\n * of actual proxy objects.\n *\n * <p>Out-of-the-box implementations are available for JDK dynamic proxies\n * and for CGLIB proxies, as applied by {@link DefaultAopProxyFactory}.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @see DefaultAopProxyFactory\n */",
            "/**\n * 已配置 AOP 代理的委托接口，用于创建实际代理对象。\n *\n * <p>{@link DefaultAopProxyFactory} 提供 JDK 动态代理与 CGLIB 代理的开箱即用实现。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @see DefaultAopProxyFactory\n */",
        ),
        (
            "\t/**\n\t * Create a new proxy object.\n\t * <p>Uses the AopProxy's default class loader (if necessary for proxy creation):\n\t * usually, the thread context class loader.\n\t * @return the new proxy object (never {@code null})\n\t * @see Thread#getContextClassLoader()\n\t */",
            "\t/**\n\t * 创建新代理对象。\n\t * <p>使用 AopProxy 的默认类加载器（创建代理必要时）：通常为线程上下文类加载器。\n\t * @return 新代理对象（永不返回 {@code null}）\n\t * @see Thread#getContextClassLoader()\n\t */",
        ),
        (
            "\t/**\n\t * Create a new proxy object.\n\t * <p>Uses the given class loader (if necessary for proxy creation).\n\t * {@code null} will simply be passed down and thus lead to the low-level\n\t * proxy facility's default, which is usually different from the default chosen\n\t * by the AopProxy implementation's {@link #getProxy()} method.\n\t * @param classLoader the class loader to create the proxy with\n\t * (or {@code null} for the low-level proxy facility's default)\n\t * @return the new proxy object (never {@code null})\n\t */",
            "\t/**\n\t * 创建新代理对象。\n\t * <p>使用给定类加载器（创建代理必要时）。\n\t * {@code null} 会向下传递，从而使用底层代理设施的默认值，\n\t * 通常与 AopProxy 实现 {@link #getProxy()} 方法的默认选择不同。\n\t * @param classLoader 创建代理所用的类加载器\n\t * （或 {@code null} 使用底层代理设施默认值）\n\t * @return 新代理对象（永不返回 {@code null}）\n\t */",
        ),
        (
            "\t/**\n\t * Determine the proxy class.\n\t * @param classLoader the class loader to create the proxy class with\n\t * (or {@code null} for the low-level proxy facility's default)\n\t * @return the proxy class\n\t * @since 6.0\n\t */",
            "\t/**\n\t * 确定代理类。\n\t * @param classLoader 创建代理类所用的类加载器\n\t * （或 {@code null} 使用底层代理设施默认值）\n\t * @return 代理类\n\t * @since 6.0\n\t */",
        ),
    ],
    "AopProxyFactory.java": [
        (
            "/**\n * Interface to be implemented by factories that are able to create\n * AOP proxies based on {@link AdvisedSupport} configuration objects.\n *\n * <p>Proxies should observe the following contract:\n * <ul>\n * <li>They should implement all interfaces that the configuration\n * indicates should be proxied.\n * <li>They should implement the {@link Advised} interface.\n * <li>They should implement the equals method to compare proxied\n * interfaces, advice, and target.\n * <li>They should be serializable if all advisors and target\n * are serializable.\n * <li>They should be thread-safe if advisors and target\n * are thread-safe.\n * </ul>\n *\n * <p>Proxies may or may not allow advice changes to be made.\n * If they do not permit advice changes (for example, because\n * the configuration was frozen) a proxy should throw an\n * {@link AopConfigException} on an attempted advice change.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n */",
            "/**\n * 能基于 {@link AdvisedSupport} 配置对象创建 AOP 代理的工厂接口。\n *\n * <p>代理应遵守以下约定：\n * <ul>\n * <li>实现配置指示应代理的所有接口。\n * <li>实现 {@link Advised} 接口。\n * <li>实现 equals 方法以比较被代理接口、advice 与目标。\n * <li>若所有通知器与目标可序列化，则代理也应可序列化。\n * <li>若通知器与目标线程安全，则代理也应线程安全。\n * </ul>\n *\n * <p>代理可能允许或不允许修改 advice。\n * 若不允许（例如配置已冻结），尝试修改 advice 时应抛出 {@link AopConfigException}。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n */",
        ),
        (
            "\t/**\n\t * Create an {@link AopProxy} for the given AOP configuration.\n\t * @param config the AOP configuration in the form of an\n\t * AdvisedSupport object\n\t * @return the corresponding AOP proxy\n\t * @throws AopConfigException if the configuration is invalid\n\t */",
            "\t/**\n\t * 为给定 AOP 配置创建 {@link AopProxy}。\n\t * @param config 以 AdvisedSupport 对象形式表示的 AOP 配置\n\t * @return 对应的 AOP 代理\n\t * @throws AopConfigException 若配置无效\n\t */",
        ),
    ],
    "CoroutinesUtils.java": [
        (
            "/**\n * Package-visible class designed to avoid a hard dependency on Kotlin and Coroutines dependency at runtime.\n *\n * @author Sebastien Deleuze\n * @since 6.1\n */",
            "/**\n * 包可见类，用于避免运行时对 Kotlin 与 Coroutines 的硬依赖。\n *\n * @author Sebastien Deleuze\n * @since 6.1\n */",
        ),
    ],
    "DefaultAdvisorChainFactory.java": [
        (
            "/**\n * A simple but definitive way of working out an advice chain for a Method,\n * given an {@link Advised} object. Always rebuilds each advice chain;\n * caching can be provided by subclasses.\n *\n * @author Juergen Hoeller\n * @author Rod Johnson\n * @author Adrian Colyer\n * @since 2.0.3\n */",
            "/**\n * 给定 {@link Advised} 对象，为 Method 确定 advice 链的简洁而明确的方式。\n * 始终重建每条 advice 链；子类可提供缓存。\n *\n * @author Juergen Hoeller\n * @author Rod Johnson\n * @author Adrian Colyer\n * @since 2.0.3\n */",
        ),
        (
            "\t/**\n\t * Singleton instance of this class.\n\t * @since 6.0.10\n\t */",
            "\t/**\n\t * 本类的单例实例。\n\t * @since 6.0.10\n\t */",
        ),
        (
            "\t\t// This is somewhat tricky... We have to process introductions first,\n\t\t// but we need to preserve order in the ultimate list.",
            "\t\t// 此处略复杂：须先处理引介，但又要保持最终列表中的顺序。",
        ),
        (
            "\t\t\t\t// Add it conditionally.",
            "\t\t\t\t// 按条件添加。",
        ),
        (
            "\t\t\t\t\t\t\t// Creating a new object instance in the getInterceptors() method\n\t\t\t\t\t\t\t// isn't a problem as we normally cache created chains.",
            "\t\t\t\t\t\t\t// 在 getInterceptors() 中创建新对象实例通常无妨，\n\t\t\t\t\t\t\t// 因为我们一般会缓存已创建的链。",
        ),
        (
            "\t/**\n\t * Determine whether the Advisors contain matching introductions.\n\t */",
            "\t/**\n\t * 判断通知器是否包含匹配的引介。\n\t */",
        ),
    ],
    "DefaultAopProxyFactory.java": [
        (
            "/**\n * Default {@link AopProxyFactory} implementation, creating either a CGLIB proxy\n * or a JDK dynamic proxy.\n *\n * <p>Creates a CGLIB proxy if one the following is true for a given\n * {@link AdvisedSupport} instance:\n * <ul>\n * <li>the {@code optimize} flag is set\n * <li>the {@code proxyTargetClass} flag is set\n * <li>no proxy interfaces have been specified\n * </ul>\n *\n * <p>In general, specify {@code proxyTargetClass} to enforce a CGLIB proxy,\n * or specify one or more interfaces to use a JDK dynamic proxy.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @author Sebastien Deleuze\n * @author Sam Brannen\n * @since 12.03.2004\n * @see AdvisedSupport#setOptimize\n * @see AdvisedSupport#setProxyTargetClass\n * @see AdvisedSupport#setInterfaces\n */",
            "/**\n * 默认 {@link AopProxyFactory} 实现，创建 CGLIB 代理或 JDK 动态代理。\n *\n * <p>对给定 {@link AdvisedSupport} 实例，若以下任一为真则创建 CGLIB 代理：\n * <ul>\n * <li>设置了 {@code optimize} 标志\n * <li>设置了 {@code proxyTargetClass} 标志\n * <li>未指定代理接口\n * </ul>\n *\n * <p>通常，指定 {@code proxyTargetClass} 强制 CGLIB 代理，\n * 或指定一个或多个接口以使用 JDK 动态代理。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @author Sebastien Deleuze\n * @author Sam Brannen\n * @since 12.03.2004\n * @see AdvisedSupport#setOptimize\n * @see AdvisedSupport#setProxyTargetClass\n * @see AdvisedSupport#setInterfaces\n */",
        ),
        (
            "\t/**\n\t * Singleton instance of this class.\n\t * @since 6.0.10\n\t */",
            "\t/**\n\t * 本类的单例实例。\n\t * @since 6.0.10\n\t */",
        ),
    ],
    "InterceptorAndDynamicMethodMatcher.java": [
        (
            "/**\n * Internal framework record, combining a {@link MethodInterceptor} instance\n * with a {@link MethodMatcher} for use as an element in the advisor chain.\n *\n * @author Rod Johnson\n * @author Sam Brannen\n * @param interceptor the {@code MethodInterceptor}\n * @param matcher the {@code MethodMatcher}\n */",
            "/**\n * 内部框架 record，将 {@link MethodInterceptor} 实例与\n * {@link MethodMatcher} 组合，作为通知器链中的元素。\n *\n * @author Rod Johnson\n * @author Sam Brannen\n * @param interceptor {@code MethodInterceptor}\n * @param matcher {@code MethodMatcher}\n */",
        ),
    ],
}
