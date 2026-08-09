"""Chinese JavaDoc replacements for springframework wave21b proxy framework [1:6]."""

PROXY_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "JdkDynamicAopProxy.java": [
        (
            "/**\n * JDK-based {@link AopProxy} implementation for the Spring AOP framework,\n * based on JDK {@link java.lang.reflect.Proxy dynamic proxies}.\n *\n * <p>Creates a dynamic proxy, implementing the interfaces exposed by\n * the AopProxy. Dynamic proxies <i>cannot</i> be used to proxy methods\n * defined in classes, rather than interfaces.\n *\n * <p>Objects of this type should be obtained through proxy factories,\n * configured by an {@link AdvisedSupport} class. This class is internal\n * to Spring's AOP framework and need not be used directly by client code.\n *\n * <p>Proxies created using this class will be thread-safe if the\n * underlying (target) class is thread-safe.\n *\n * <p>Proxies are serializable so long as all Advisors (including Advices\n * and Pointcuts) and the TargetSource are serializable.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @author Rob Harrop\n * @author Dave Syer\n * @author Sergey Tsypanov\n * @author Sebastien Deleuze\n * @see java.lang.reflect.Proxy\n * @see AdvisedSupport\n * @see ProxyFactory\n */",
            "/**\n * Spring AOP 框架基于 JDK {@link java.lang.reflect.Proxy 动态代理}\n * 的 {@link AopProxy} 实现。\n *\n * <p>创建动态代理，实现 AopProxy 暴露的接口。\n * 动态代理<i>不能</i>用于代理类中定义的方法，仅适用于接口。\n *\n * <p>此类对象应通过由 {@link AdvisedSupport} 配置的代理工厂获取。\n * 本类为 Spring AOP 框架内部类，客户端代码无需直接使用。\n *\n * <p>若底层（目标）类线程安全，则本类创建的代理也线程安全。\n *\n * <p>当所有 Advisor（含 Advice 与 Pointcut）及 TargetSource 可序列化时，\n * 代理可序列化。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @author Rob Harrop\n * @author Dave Syer\n * @author Sergey Tsypanov\n * @author Sebastien Deleuze\n * @see java.lang.reflect.Proxy\n * @see AdvisedSupport\n * @see ProxyFactory\n */",
        ),
        (
            "\t/** use serialVersionUID from Spring 1.2 for interoperability. */",
            "\t/** 使用 Spring 1.2 的 serialVersionUID 以保持互操作性。 */",
        ),
        (
            "\t/** We use a static Log to avoid serialization issues. */",
            "\t/** 使用 static Log 以避免序列化问题。 */",
        ),
        (
            "\t/** Config used to configure this proxy. */",
            "\t/** 用于配置本代理的配置对象。 */",
        ),
        (
            "\t/** Cached in {@link AdvisedSupport#proxyMetadataCache}. */",
            "\t/** 缓存在 {@link AdvisedSupport#proxyMetadataCache} 中。 */",
        ),
        (
            "\t/**\n\t * Construct a new JdkDynamicAopProxy for the given AOP configuration.\n\t * @param config the AOP configuration as AdvisedSupport object\n\t * @throws AopConfigException if the config is invalid. We try to throw an informative\n\t * exception in this case, rather than let a mysterious failure happen later.\n\t */",
            "\t/**\n\t * 为给定 AOP 配置构造新的 JdkDynamicAopProxy。\n\t * @param config 作为 AdvisedSupport 对象的 AOP 配置\n\t * @throws AopConfigException 若配置无效；\n\t * 此时抛出明确异常，而非稍后出现难以排查的失败。\n\t */",
        ),
        (
            "\t\t// Initialize ProxiedInterfacesCache if not cached already",
            "\t\t// 若尚未缓存，则初始化 ProxiedInterfacesCache",
        ),
        (
            "\t/**\n\t * Determine whether the JDK bootstrap or platform loader has been suggested ->\n\t * use higher-level loader which can see Spring infrastructure classes instead.\n\t */",
            "\t/**\n\t * 判断是否建议使用 JDK 引导或平台类加载器 ->\n\t * 改用能加载 Spring 基础设施类的更高级类加载器。\n\t */",
        ),
        (
            "\t\t\t// JDK bootstrap loader -> use spring-aop ClassLoader instead.",
            "\t\t\t// JDK 引导类加载器 -> 改用 spring-aop ClassLoader。",
        ),
        (
            "\t\t\t// Potentially the JDK platform loader on JDK 9+",
            "\t\t\t// 可能是 JDK 9+ 上的 JDK 平台类加载器",
        ),
        (
            "\t\t\t\t\t// Suggested ClassLoader is ancestor of spring-aop ClassLoader\n\t\t\t\t\t// -> use spring-aop ClassLoader itself instead.",
            "\t\t\t\t\t// 建议的 ClassLoader 是 spring-aop ClassLoader 的祖先\n\t\t\t\t\t// -> 改用 spring-aop ClassLoader 本身。",
        ),
        (
            "\t\t// Regular case: use suggested ClassLoader as-is.",
            "\t\t// 常规情况：直接使用建议的 ClassLoader。",
        ),
        (
            "\t/**\n\t * Implementation of {@code InvocationHandler.invoke}.\n\t * <p>Callers will see exactly the exception thrown by the target,\n\t * unless a hook method throws an exception.\n\t */",
            "\t/**\n\t * {@code InvocationHandler.invoke} 的实现。\n\t * <p>调用方将看到目标抛出的确切异常，\n\t * 除非钩子方法抛出异常。\n\t */",
        ),
        (
            "\t\t\t\t// The target does not implement the equals(Object) method itself.",
            "\t\t\t\t// 目标本身未实现 equals(Object) 方法。",
        ),
        (
            "\t\t\t\t// The target does not implement the hashCode() method itself.",
            "\t\t\t\t// 目标本身未实现 hashCode() 方法。",
        ),
        (
            "\t\t\t\t// There is only getDecoratedClass() declared -> dispatch to proxy config.",
            "\t\t\t\t// 仅声明 getDecoratedClass() -> 分派到代理配置。",
        ),
        (
            "\t\t\t\t// Service invocations on ProxyConfig with the proxy config...",
            "\t\t\t\t// 在 ProxyConfig 上以服务方式调用代理配置...",
        ),
        (
            "\t\t\t\t// Make invocation available if necessary.",
            "\t\t\t\t// 必要时使调用可用。",
        ),
        (
            "\t\t\t// Get as late as possible to minimize the time we \"own\" the target,\n\t\t\t// in case it comes from a pool.",
            "\t\t\t// 尽可能晚获取目标，以缩短「持有」目标的时间（可能来自对象池）。",
        ),
        (
            "\t\t\t// Get the interception chain for this method.",
            "\t\t\t// 获取本方法的拦截链。",
        ),
        (
            "\t\t\t// Check whether we have any advice. If we don't, we can fall back on direct\n\t\t\t// reflective invocation of the target, and avoid creating a MethodInvocation.",
            "\t\t\t// 检查是否有 Advice。若无，可直接反射调用目标，避免创建 MethodInvocation。",
        ),
        (
            "\t\t\t\t// We can skip creating a MethodInvocation: just invoke the target directly\n\t\t\t\t// Note that the final invoker must be an InvokerInterceptor so we know it does\n\t\t\t\t// nothing but a reflective operation on the target, and no hot swapping or fancy proxying.",
            "\t\t\t\t// 可跳过创建 MethodInvocation：直接调用目标\n\t\t\t\t// 注意最终调用者必须是 InvokerInterceptor，\n\t\t\t\t// 仅对目标做反射操作，无热替换或复杂代理。",
        ),
        (
            "\t\t\t\t// We need to create a method invocation...",
            "\t\t\t\t// 需要创建方法调用...",
        ),
        (
            "\t\t\t\t// Proceed to the joinpoint through the interceptor chain.",
            "\t\t\t\t// 通过拦截器链继续执行连接点。",
        ),
        (
            "\t\t\t// Massage return value if necessary.",
            "\t\t\t// 必要时调整返回值。",
        ),
        (
            "\t\t\t\t// Special case: it returned \"this\" and the return type of the method\n\t\t\t\t// is type-compatible. Note that we can't help if the target sets\n\t\t\t\t// a reference to itself in another returned object.",
            "\t\t\t\t// 特殊情况：返回 \"this\" 且方法返回类型兼容。\n\t\t\t\t// 注意：若目标在另一返回对象中设置自引用，则无法处理。",
        ),
        (
            "\t\t\t\t// Must have come from TargetSource.",
            "\t\t\t\t// 必定来自 TargetSource。",
        ),
        (
            "\t\t\t\t// Restore old proxy.",
            "\t\t\t\t// 恢复旧代理。",
        ),
        (
            "\t/**\n\t * Equality means interfaces, advisors and TargetSource are equal.\n\t * <p>The compared object may be a JdkDynamicAopProxy instance itself\n\t * or a dynamic proxy wrapping a JdkDynamicAopProxy instance.\n\t */",
            "\t/**\n\t * 相等性指接口、Advisor 与 TargetSource 均相等。\n\t * <p>比较对象可能是 JdkDynamicAopProxy 实例本身，\n\t * 或包装 JdkDynamicAopProxy 的动态代理。\n\t */",
        ),
        (
            "\t\t\t// Not a valid comparison...",
            "\t\t\t// 无效比较...",
        ),
        (
            "\t\t// If we get here, otherProxy is the other AopProxy.",
            "\t\t// 执行至此，otherProxy 为另一 AopProxy。",
        ),
        (
            "\t/**\n\t * Proxy uses the hash code of the TargetSource.\n\t */",
            "\t/**\n\t * 代理使用 TargetSource 的哈希码。\n\t */",
        ),
        (
            "\t//---------------------------------------------------------------------\n\t// Serialization support\n\t//---------------------------------------------------------------------",
            "\t//---------------------------------------------------------------------\n\t// 序列化支持\n\t//---------------------------------------------------------------------",
        ),
        (
            "\t\t// Rely on default serialization; just initialize state after deserialization.",
            "\t\t// 依赖默认序列化；反序列化后仅初始化状态。",
        ),
        (
            "\t\t// Initialize transient fields.",
            "\t\t// 初始化 transient 字段。",
        ),
        (
            "\t/**\n\t * Holder for the complete proxied interfaces and derived metadata,\n\t * to be cached in {@link AdvisedSupport#proxyMetadataCache}.\n\t * @since 6.1.3\n\t */",
            "\t/**\n\t * 完整被代理接口及派生元数据的持有者，\n\t * 缓存在 {@link AdvisedSupport#proxyMetadataCache} 中。\n\t * @since 6.1.3\n\t */",
        ),
        (
            "\t\t\t// Find any {@link #equals} or {@link #hashCode} method that may be defined\n\t\t\t// on the supplied set of interfaces.",
            "\t\t\t// 在提供的接口集合中查找可能定义的 {@link #equals} 或 {@link #hashCode} 方法。",
        ),
    ],
    "ObjenesisCglibAopProxy.java": [
        (
            "/**\n * Objenesis-based extension of {@link CglibAopProxy} to create proxy instances\n * without invoking the constructor of the class. Used by default.\n *\n * @author Oliver Gierke\n * @author Juergen Hoeller\n * @since 4.0\n */",
            "/**\n * 基于 Objenesis 的 {@link CglibAopProxy} 扩展，\n * 创建代理实例时不调用类构造器。默认使用。\n *\n * @author Oliver Gierke\n * @author Juergen Hoeller\n * @since 4.0\n */",
        ),
        (
            "\t/**\n\t * Create a new ObjenesisCglibAopProxy for the given AOP configuration.\n\t * @param config the AOP configuration as AdvisedSupport object\n\t */",
            "\t/**\n\t * 为给定 AOP 配置创建新的 ObjenesisCglibAopProxy。\n\t * @param config 作为 AdvisedSupport 对象的 AOP 配置\n\t */",
        ),
        (
            "\t\t\t// Regular instantiation via default constructor...",
            "\t\t\t// 通过默认构造器的常规实例化...",
        ),
    ],
    "ProxyConfig.java": [
        (
            "/**\n * Convenience superclass for configuration used in creating proxies,\n * to ensure that all proxy creators have consistent properties.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @see AdvisedSupport\n */",
            "/**\n * 创建代理所用配置的便捷超类，\n * 确保所有代理创建器属性一致。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @see AdvisedSupport\n */",
        ),
        (
            "\t/** use serialVersionUID from Spring 1.2 for interoperability. */",
            "\t/** 使用 Spring 1.2 的 serialVersionUID 以保持互操作性。 */",
        ),
        (
            "\t/**\n\t * Set whether to proxy the target class directly, instead of just proxying\n\t * specific interfaces. Default is \"false\".\n\t * <p>Set this to \"true\" to force proxying for the TargetSource's exposed\n\t * target class. If that target class is an interface, a JDK proxy will be\n\t * created for the given interface. If that target class is any other class,\n\t * a CGLIB proxy will be created for the given class.\n\t * <p>Note: Depending on the configuration of the concrete proxy factory,\n\t * the proxy-target-class behavior will also be applied if no interfaces\n\t * have been specified (and no interface autodetection is activated).\n\t * @see org.springframework.aop.TargetSource#getTargetClass()\n\t */",
            "\t/**\n\t * 设置是否直接代理目标类，而非仅代理特定接口。默认为 \"false\"。\n\t * <p>设为 \"true\" 可强制代理 TargetSource 暴露的目标类。\n\t * 若目标类为接口，则为该接口创建 JDK 代理；\n\t * 若为其他类，则为该类创建 CGLIB 代理。\n\t * <p>注意：取决于具体代理工厂配置，\n\t * 若未指定接口（且未启用接口自动检测），\n\t * 也会应用 proxy-target-class 行为。\n\t * @see org.springframework.aop.TargetSource#getTargetClass()\n\t */",
        ),
        (
            "\t/**\n\t * Return whether to proxy the target class directly as well as any interfaces.\n\t */",
            "\t/**\n\t * 返回是否直接代理目标类（以及任意接口）。\n\t */",
        ),
        (
            "\t/**\n\t * Set whether proxies should perform aggressive optimizations.\n\t * The exact meaning of \"aggressive optimizations\" will differ\n\t * between proxies, but there is usually some tradeoff.\n\t * Default is \"false\".\n\t * <p>With Spring's current proxy options, this flag effectively\n\t * enforces CGLIB proxies (similar to {@link #setProxyTargetClass})\n\t * but without any class validation checks (for final methods etc).\n\t */",
            "\t/**\n\t * 设置代理是否执行激进优化。\n\t * 「激进优化」的具体含义因代理类型而异，通常存在权衡。默认为 \"false\"。\n\t * <p>在 Spring 当前代理选项下，此标志等效于强制 CGLIB 代理\n\t *（类似 {@link #setProxyTargetClass}），\n\t * 但不进行类校验（如 final 方法等）。\n\t */",
        ),
        (
            "\t/**\n\t * Return whether proxies should perform aggressive optimizations.\n\t */",
            "\t/**\n\t * 返回代理是否执行激进优化。\n\t */",
        ),
        (
            "\t/**\n\t * Set whether proxies created by this configuration should be prevented\n\t * from being cast to {@link Advised} to query proxy status.\n\t * <p>Default is \"false\", meaning that any AOP proxy can be cast to\n\t * {@link Advised}.\n\t */",
            "\t/**\n\t * 设置本配置创建的代理是否禁止强制转换为 {@link Advised} 以查询代理状态。\n\t * <p>默认为 \"false\"，表示任意 AOP 代理可转换为 {@link Advised}。\n\t */",
        ),
        (
            "\t/**\n\t * Return whether proxies created by this configuration should be\n\t * prevented from being cast to {@link Advised}.\n\t */",
            "\t/**\n\t * 返回本配置创建的代理是否禁止转换为 {@link Advised}。\n\t */",
        ),
        (
            "\t/**\n\t * Set whether the proxy should be exposed by the AOP framework as a\n\t * ThreadLocal for retrieval via the AopContext class. This is useful\n\t * if an advised object needs to call another advised method on itself.\n\t * (If it uses {@code this}, the invocation will not be advised).\n\t * <p>Default is \"false\", in order to avoid unnecessary extra interception.\n\t * This means that no guarantees are provided that AopContext access will\n\t * work consistently within any method of the advised object.\n\t */",
            "\t/**\n\t * 设置 AOP 框架是否通过 ThreadLocal 暴露代理，\n\t * 以便通过 AopContext 类获取。当被通知对象需调用自身另一被通知方法时有用\n\t *（若使用 {@code this}，调用不会被通知）。\n\t * <p>默认为 \"false\"，以避免不必要的额外拦截。\n\t * 这意味着不保证在被通知对象的任意方法内\n\t * AopContext 访问始终一致可用。\n\t */",
        ),
        (
            "\t/**\n\t * Return whether the AOP proxy will expose the AOP proxy for\n\t * each invocation.\n\t */",
            "\t/**\n\t * 返回 AOP 代理是否在每次调用时暴露自身。\n\t */",
        ),
        (
            "\t/**\n\t * Set whether this config should be frozen.\n\t * <p>When a config is frozen, no advice changes can be made. This is\n\t * useful for optimization, and useful when we don't want callers to\n\t * be able to manipulate configuration after casting to Advised.\n\t */",
            "\t/**\n\t * 设置本配置是否应冻结。\n\t * <p>配置冻结后不可更改 Advice。有利于优化，\n\t * 也用于防止调用者在转换为 Advised 后修改配置。\n\t */",
        ),
        (
            "\t/**\n\t * Return whether the config is frozen, and no advice changes can be made.\n\t */",
            "\t/**\n\t * 返回配置是否已冻结（不可更改 Advice）。\n\t */",
        ),
        (
            "\t/**\n\t * Copy configuration from the other config object.\n\t * @param other object to copy configuration from\n\t */",
            "\t/**\n\t * 从其他配置对象复制配置。\n\t * @param other 要复制配置的来源对象\n\t */",
        ),
        (
            "\t/**\n\t * Copy default settings from the other config object,\n\t * for settings that have not been locally set.\n\t * @param other object to copy configuration from\n\t * @since 7.0\n\t */",
            "\t/**\n\t * 从其他配置对象复制默认设置，\n\t * 仅针对本地未设置的项。\n\t * @param other 要复制配置的来源对象\n\t * @since 7.0\n\t */",
        ),
    ],
    "ProxyCreatorSupport.java": [
        (
            "/**\n * Base class for proxy factories.\n * Provides convenient access to a configurable AopProxyFactory.\n *\n * @author Juergen Hoeller\n * @since 2.0.3\n * @see #createAopProxy()\n */",
            "/**\n * 代理工厂基类。\n * 提供对可配置 AopProxyFactory 的便捷访问。\n *\n * @author Juergen Hoeller\n * @since 2.0.3\n * @see #createAopProxy()\n */",
        ),
        (
            "\t/** Set to true when the first AOP proxy has been created. */",
            "\t/** 创建首个 AOP 代理时设为 true。 */",
        ),
        (
            "\t/**\n\t * Create a new ProxyCreatorSupport instance.\n\t */",
            "\t/**\n\t * 创建新的 ProxyCreatorSupport 实例。\n\t */",
        ),
        (
            "\t/**\n\t * Create a new ProxyCreatorSupport instance.\n\t * @param aopProxyFactory the AopProxyFactory to use\n\t */",
            "\t/**\n\t * 创建新的 ProxyCreatorSupport 实例。\n\t * @param aopProxyFactory 要使用的 AopProxyFactory\n\t */",
        ),
        (
            "\t/**\n\t * Customize the AopProxyFactory, allowing different strategies\n\t * to be dropped in without changing the core framework.\n\t * <p>Default is {@link DefaultAopProxyFactory}, using dynamic JDK\n\t * proxies or CGLIB proxies based on the requirements.\n\t */",
            "\t/**\n\t * 自定义 AopProxyFactory，允许在不改动核心框架的情况下\n\t * 插入不同策略。\n\t * <p>默认为 {@link DefaultAopProxyFactory}，\n\t * 根据需求使用动态 JDK 或 CGLIB 代理。\n\t */",
        ),
        (
            "\t/**\n\t * Return the AopProxyFactory that this ProxyConfig uses.\n\t */",
            "\t/**\n\t * 返回本 ProxyConfig 使用的 AopProxyFactory。\n\t */",
        ),
        (
            "\t/**\n\t * Add the given AdvisedSupportListener to this proxy configuration.\n\t * @param listener the listener to register\n\t */",
            "\t/**\n\t * 向本代理配置添加给定 AdvisedSupportListener。\n\t * @param listener 要注册的监听器\n\t */",
        ),
        (
            "\t/**\n\t * Remove the given AdvisedSupportListener from this proxy configuration.\n\t * @param listener the listener to remove\n\t */",
            "\t/**\n\t * 从本代理配置移除给定 AdvisedSupportListener。\n\t * @param listener 要移除的监听器\n\t */",
        ),
        (
            "\t/**\n\t * Subclasses should call this to get a new AOP proxy. They should <b>not</b>\n\t * create an AOP proxy with {@code this} as an argument.\n\t */",
            "\t/**\n\t * 子类应调用此方法获取新 AOP 代理。\n\t * 子类<b>不应</b>以 {@code this} 为参数创建 AOP 代理。\n\t */",
        ),
        (
            "\t/**\n\t * Activate this proxy configuration.\n\t * @see AdvisedSupportListener#activated\n\t */",
            "\t/**\n\t * 激活本代理配置。\n\t * @see AdvisedSupportListener#activated\n\t */",
        ),
        (
            "\t/**\n\t * Propagate advice change event to all AdvisedSupportListeners.\n\t * @see AdvisedSupportListener#adviceChanged\n\t */",
            "\t/**\n\t * 将 Advice 变更事件传播给所有 AdvisedSupportListener。\n\t * @see AdvisedSupportListener#adviceChanged\n\t */",
        ),
        (
            "\t/**\n\t * Subclasses can call this to check whether any AOP proxies have been created yet.\n\t */",
            "\t/**\n\t * 子类可调用此方法检查是否已创建任何 AOP 代理。\n\t */",
        ),
    ],
    "ProxyFactory.java": [
        (
            "/**\n * Factory for AOP proxies for programmatic use, rather than via declarative\n * setup in a bean factory. This class provides a simple way of obtaining\n * and configuring AOP proxy instances in custom user code.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @author Rob Harrop\n * @since 14.03.2003\n */",
            "/**\n * 用于编程式（而非 Bean 工厂声明式配置）创建 AOP 代理的工厂。\n * 本类提供在用户代码中获取并配置 AOP 代理实例的简便方式。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @author Rob Harrop\n * @since 14.03.2003\n */",
        ),
        (
            "\t/**\n\t * Create a new ProxyFactory.\n\t */",
            "\t/**\n\t * 创建新的 ProxyFactory。\n\t */",
        ),
        (
            "\t/**\n\t * Create a new ProxyFactory.\n\t * <p>Will proxy all interfaces that the given target implements.\n\t * @param target the target object to be proxied\n\t */",
            "\t/**\n\t * 创建新的 ProxyFactory。\n\t * <p>将代理给定目标实现的所有接口。\n\t * @param target 要代理的目标对象\n\t */",
        ),
        (
            "\t/**\n\t * Create a new ProxyFactory.\n\t * <p>No target, only interfaces. Must add interceptors.\n\t * @param proxyInterfaces the interfaces that the proxy should implement\n\t */",
            "\t/**\n\t * 创建新的 ProxyFactory。\n\t * <p>无目标，仅接口。须添加拦截器。\n\t * @param proxyInterfaces 代理应实现的接口\n\t */",
        ),
        (
            "\t/**\n\t * Create a new ProxyFactory for the given interface and interceptor.\n\t * <p>Convenience method for creating a proxy for a single interceptor,\n\t * assuming that the interceptor handles all calls itself rather than\n\t * delegating to a target, like in the case of remoting proxies.\n\t * @param proxyInterface the interface that the proxy should implement\n\t * @param interceptor the interceptor that the proxy should invoke\n\t */",
            "\t/**\n\t * 为给定接口与拦截器创建新的 ProxyFactory。\n\t * <p>便捷方法：为单一拦截器创建代理，\n\t * 假设拦截器自行处理所有调用而非委托目标（如远程代理场景）。\n\t * @param proxyInterface 代理应实现的接口\n\t * @param interceptor 代理应调用的拦截器\n\t */",
        ),
        (
            "\t/**\n\t * Create a ProxyFactory for the specified {@code TargetSource},\n\t * making the proxy implement the specified interface.\n\t * @param proxyInterface the interface that the proxy should implement\n\t * @param targetSource the TargetSource that the proxy should invoke\n\t */",
            "\t/**\n\t * 为指定 {@code TargetSource} 创建 ProxyFactory，\n\t * 使代理实现指定接口。\n\t * @param proxyInterface 代理应实现的接口\n\t * @param targetSource 代理应调用的 TargetSource\n\t */",
        ),
        (
            "\t/**\n\t * Create a new proxy according to the settings in this factory.\n\t * <p>Can be called repeatedly. Effect will vary if we've added\n\t * or removed interfaces. Can add and remove interceptors.\n\t * <p>Uses a default class loader: Usually, the thread context class loader\n\t * (if necessary for proxy creation).\n\t * @return the proxy object\n\t */",
            "\t/**\n\t * 根据本工厂设置创建新代理。\n\t * <p>可重复调用。增删接口时效果不同。可增删拦截器。\n\t * <p>使用默认类加载器：通常为线程上下文类加载器\n\t *（代理创建需要时）。\n\t * @return 代理对象\n\t */",
        ),
        (
            "\t/**\n\t * Create a new proxy according to the settings in this factory.\n\t * <p>Can be called repeatedly. Effect will vary if we've added\n\t * or removed interfaces. Can add and remove interceptors.\n\t * <p>Uses the given class loader (if necessary for proxy creation).\n\t * @param classLoader the class loader to create the proxy with\n\t * (or {@code null} for the low-level proxy facility's default)\n\t * @return the proxy object\n\t */",
            "\t/**\n\t * 根据本工厂设置创建新代理。\n\t * <p>可重复调用。增删接口时效果不同。可增删拦截器。\n\t * <p>使用给定类加载器（代理创建需要时）。\n\t * @param classLoader 创建代理所用的类加载器\n\t *（或 {@code null} 表示底层代理设施的默认值）\n\t * @return 代理对象\n\t */",
        ),
        (
            "\t/**\n\t * Determine the proxy class according to the settings in this factory.\n\t * @param classLoader the class loader to create the proxy class with\n\t * (or {@code null} for the low-level proxy facility's default)\n\t * @return the proxy class\n\t * @since 6.0\n\t */",
            "\t/**\n\t * 根据本工厂设置确定代理类。\n\t * @param classLoader 创建代理类所用的类加载器\n\t *（或 {@code null} 表示底层代理设施的默认值）\n\t * @return 代理类\n\t * @since 6.0\n\t */",
        ),
        (
            "\t/**\n\t * Create a new proxy for the given interface and interceptor.\n\t * <p>Convenience method for creating a proxy for a single interceptor,\n\t * assuming that the interceptor handles all calls itself rather than\n\t * delegating to a target, like in the case of remoting proxies.\n\t * @param proxyInterface the interface that the proxy should implement\n\t * @param interceptor the interceptor that the proxy should invoke\n\t * @return the proxy object\n\t * @see #ProxyFactory(Class, org.aopalliance.intercept.Interceptor)\n\t */",
            "\t/**\n\t * 为给定接口与拦截器创建新代理。\n\t * <p>便捷方法：为单一拦截器创建代理，\n\t * 假设拦截器自行处理所有调用而非委托目标（如远程代理场景）。\n\t * @param proxyInterface 代理应实现的接口\n\t * @param interceptor 代理应调用的拦截器\n\t * @return 代理对象\n\t * @see #ProxyFactory(Class, org.aopalliance.intercept.Interceptor)\n\t */",
        ),
        (
            "\t/**\n\t * Create a proxy for the specified {@code TargetSource},\n\t * implementing the specified interface.\n\t * @param proxyInterface the interface that the proxy should implement\n\t * @param targetSource the TargetSource that the proxy should invoke\n\t * @return the proxy object\n\t * @see #ProxyFactory(Class, org.springframework.aop.TargetSource)\n\t */",
            "\t/**\n\t * 为指定 {@code TargetSource} 创建代理，实现指定接口。\n\t * @param proxyInterface 代理应实现的接口\n\t * @param targetSource 代理应调用的 TargetSource\n\t * @return 代理对象\n\t * @see #ProxyFactory(Class, org.springframework.aop.TargetSource)\n\t */",
        ),
        (
            "\t/**\n\t * Create a proxy for the specified {@code TargetSource} that extends\n\t * the target class of the {@code TargetSource}.\n\t * @param targetSource the TargetSource that the proxy should invoke\n\t * @return the proxy object\n\t */",
            "\t/**\n\t * 为指定 {@code TargetSource} 创建代理，\n\t * 扩展 {@code TargetSource} 的目标类。\n\t * @param targetSource 代理应调用的 TargetSource\n\t * @return 代理对象\n\t */",
        ),
    ],
    "ProxyProcessorSupport.java": [
        (
            "/**\n * Base class with common functionality for proxy processors, in particular\n * ClassLoader management and the {@link #evaluateProxyInterfaces} algorithm.\n *\n * @author Juergen Hoeller\n * @since 4.1\n * @see AbstractAdvisingBeanPostProcessor\n * @see org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator\n */",
            "/**\n * 代理处理器通用功能基类，\n * 尤其包含 ClassLoader 管理与 {@link #evaluateProxyInterfaces} 算法。\n *\n * @author Juergen Hoeller\n * @since 4.1\n * @see AbstractAdvisingBeanPostProcessor\n * @see org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator\n */",
        ),
        (
            "\t/**\n\t * This should run after all other processors, so that it can just add\n\t * an advisor to existing proxies rather than double-proxy.\n\t */",
            "\t/**\n\t * 应在所有其他处理器之后运行，\n\t * 以便向现有代理添加 Advisor 而非双重代理。\n\t */",
        ),
        (
            "\t/**\n\t * Set the ordering which will apply to this processor's implementation\n\t * of {@link Ordered}, used when applying multiple processors.\n\t * <p>The default value is {@code Ordered.LOWEST_PRECEDENCE}, meaning non-ordered.\n\t * @param order the ordering value\n\t */",
            "\t/**\n\t * 设置本处理器 {@link Ordered} 实现的排序值，\n\t * 用于应用多个处理器时。\n\t * <p>默认值为 {@code Ordered.LOWEST_PRECEDENCE}，表示无序。\n\t * @param order 排序值\n\t */",
        ),
        (
            "\t/**\n\t * Set the ClassLoader to generate the proxy class in.\n\t * <p>Default is the bean ClassLoader, i.e. the ClassLoader used by the containing\n\t * {@link org.springframework.beans.factory.BeanFactory} for loading all bean classes.\n\t * This can be overridden here for specific proxies.\n\t */",
            "\t/**\n\t * 设置生成代理类所用的 ClassLoader。\n\t * <p>默认为 Bean ClassLoader，即包含的\n\t * {@link org.springframework.beans.factory.BeanFactory} 加载所有 Bean 类所用的 ClassLoader。\n\t * 可在此为特定代理覆盖。\n\t */",
        ),
        (
            "\t/**\n\t * Return the configured proxy ClassLoader for this processor.\n\t */",
            "\t/**\n\t * 返回本处理器配置的代理 ClassLoader。\n\t */",
        ),
        (
            "\t/**\n\t * Check the interfaces on the given bean class and apply them to the {@link ProxyFactory},\n\t * if appropriate.\n\t * <p>Calls {@link #isConfigurationCallbackInterface} and {@link #isInternalLanguageInterface}\n\t * to filter for reasonable proxy interfaces, falling back to a target-class proxy otherwise.\n\t * @param beanClass the class of the bean\n\t * @param proxyFactory the ProxyFactory for the bean\n\t */",
            "\t/**\n\t * 检查给定 Bean 类的接口，并在适当时应用到 {@link ProxyFactory}。\n\t * <p>调用 {@link #isConfigurationCallbackInterface} 与 {@link #isInternalLanguageInterface}\n\t * 过滤合理的代理接口，否则回退到目标类代理。\n\t * @param beanClass Bean 的类\n\t * @param proxyFactory Bean 的 ProxyFactory\n\t */",
        ),
        (
            "\t\t\t// Must allow for introductions; can't just set interfaces to the target's interfaces only.",
            "\t\t\t// 须允许引介；不能仅将接口设为目标接口。",
        ),
        (
            "\t/**\n\t * Determine whether the given interface is just a container callback and\n\t * therefore not to be considered as a reasonable proxy interface.\n\t * <p>If no reasonable proxy interface is found for a given bean, it will get\n\t * proxied with its full target class, assuming that as the user's intention.\n\t * @param ifc the interface to check\n\t * @return whether the given interface is just a container callback\n\t */",
            "\t/**\n\t * 判断给定接口是否仅为容器回调，\n\t * 因此不应视为合理的代理接口。\n\t * <p>若给定 Bean 找不到合理代理接口，\n\t * 将以其完整目标类代理，假定此为用户的意图。\n\t * @param ifc 要检查的接口\n\t * @return 给定接口是否仅为容器回调\n\t */",
        ),
        (
            "\t/**\n\t * Determine whether the given interface is a well-known internal language interface\n\t * and therefore not to be considered as a reasonable proxy interface.\n\t * <p>If no reasonable proxy interface is found for a given bean, it will get\n\t * proxied with its full target class, assuming that as the user's intention.\n\t * @param ifc the interface to check\n\t * @return whether the given interface is an internal language interface\n\t */",
            "\t/**\n\t * 判断给定接口是否为已知内部语言接口，\n\t * 因此不应视为合理的代理接口。\n\t * <p>若给定 Bean 找不到合理代理接口，\n\t * 将以其完整目标类代理，假定此为用户的意图。\n\t * @param ifc 要检查的接口\n\t * @return 给定接口是否为内部语言接口\n\t */",
        ),
    ],
}
