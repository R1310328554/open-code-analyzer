"""Chinese JavaDoc replacements for springframework wave22b scope package [4:8]."""

SCOPE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "DefaultScopedObject.java": [
        (
            "/**\n * Default implementation of the {@link ScopedObject} interface.\n *\n * <p>Simply delegates the calls to the underlying\n * {@link ConfigurableBeanFactory bean factory}\n * ({@link ConfigurableBeanFactory#getBean(String)}/\n * {@link ConfigurableBeanFactory#destroyScopedBean(String)}).\n *\n * @author Juergen Hoeller\n * @since 2.0\n * @see org.springframework.beans.factory.BeanFactory#getBean\n * @see org.springframework.beans.factory.config.ConfigurableBeanFactory#destroyScopedBean\n */",
            "/**\n * {@link ScopedObject} 接口的默认实现。\n *\n * <p>简单地将调用委托给底层\n * {@link ConfigurableBeanFactory Bean 工厂}\n * （{@link ConfigurableBeanFactory#getBean(String)}/\n * {@link ConfigurableBeanFactory#destroyScopedBean(String)}）。\n *\n * @author Juergen Hoeller\n * @since 2.0\n * @see org.springframework.beans.factory.BeanFactory#getBean\n * @see org.springframework.beans.factory.config.ConfigurableBeanFactory#destroyScopedBean\n */",
        ),
        (
            "\t/**\n\t * Creates a new instance of the {@link DefaultScopedObject} class.\n\t * @param beanFactory the {@link ConfigurableBeanFactory} that holds the scoped target object\n\t * @param targetBeanName the name of the target bean\n\t */",
            "\t/**\n\t * 创建 {@link DefaultScopedObject} 的新实例。\n\t * @param beanFactory 持有作用域目标对象的 {@link ConfigurableBeanFactory}\n\t * @param targetBeanName 目标 Bean 的名称\n\t */",
        ),
    ],
    "ScopedObject.java": [
        (
            "/**\n * An AOP introduction interface for scoped objects.\n *\n * <p>Objects created from the {@link ScopedProxyFactoryBean} can be cast\n * to this interface, enabling access to the raw target object\n * and programmatic removal of the target object.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @since 2.0\n * @see ScopedProxyFactoryBean\n */",
            "/**\n * 作用域对象的 AOP 引入接口。\n *\n * <p>由 {@link ScopedProxyFactoryBean} 创建的对象可转型为本接口，\n * 从而访问原始目标对象并以编程方式移除目标对象。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @since 2.0\n * @see ScopedProxyFactoryBean\n */",
        ),
        (
            "\t/**\n\t * Return the current target object behind this scoped object proxy,\n\t * in its raw form (as stored in the target scope).\n\t * <p>The raw target object can for example be passed to persistence\n\t * providers which would not be able to handle the scoped proxy object.\n\t * @return the current target object behind this scoped object proxy\n\t */",
            "\t/**\n\t * 返回本作用域对象代理背后的当前目标对象，\n\t * 以原始形式（即目标作用域中存储的形式）。\n\t * <p>例如，原始目标对象可传给无法处理作用域代理对象的持久化提供者。\n\t * @return 本作用域对象代理背后的当前目标对象\n\t */",
        ),
        (
            "\t/**\n\t * Remove this object from its target scope, for example from\n\t * the backing session.\n\t * <p>Note that no further calls may be made to the scoped object\n\t * afterwards (at least within the current thread, that is, with\n\t * the exact same target object in the target scope).\n\t */",
            "\t/**\n\t * 从目标作用域（例如底层 session）中移除此对象。\n\t * <p>注意：之后不得再调用该作用域对象\n\t * （至少在当前线程内、目标作用域中仍是同一目标对象时如此）。\n\t */",
        ),
    ],
    "ScopedProxyBeanRegistrationAotProcessor.java": [
        (
            "/**\n * {@link BeanRegistrationAotProcessor} for {@link ScopedProxyFactoryBean}.\n *\n * @author Stephane Nicoll\n * @author Phillip Webb\n * @since 6.0\n */",
            "/**\n * 用于 {@link ScopedProxyFactoryBean} 的 {@link BeanRegistrationAotProcessor}。\n *\n * @author Stephane Nicoll\n * @author Phillip Webb\n * @since 6.0\n */",
        ),
        (
            "\t\t\t\t\tmethod.addJavadoc(\"Create the scoped proxy bean instance for '$L'.\",",
            "\t\t\t\t\tmethod.addJavadoc(\"为 '$L' 创建作用域代理 Bean 实例。\",",
        ),
    ],
    "ScopedProxyFactoryBean.java": [
        (
            "/**\n * Convenient proxy factory bean for scoped objects.\n *\n * <p>Proxies created using this factory bean are thread-safe singletons\n * and may be injected into shared objects, with transparent scoping behavior.\n *\n * <p>Proxies returned by this class implement the {@link ScopedObject} interface.\n * This presently allows for removing the corresponding object from the scope,\n * seamlessly creating a new instance in the scope on next access.\n *\n * <p>Please note that the proxies created by this factory are\n * <i>class-based</i> proxies by default. This can be customized\n * through switching the \"proxyTargetClass\" property to \"false\".\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @since 2.0\n * @see #setProxyTargetClass\n */",
            "/**\n * 作用域对象的便捷代理工厂 Bean。\n *\n * <p>本工厂 Bean 创建的代理为线程安全的单例，\n * 可注入共享对象，作用域行为对调用方透明。\n *\n * <p>本类返回的代理实现 {@link ScopedObject} 接口。\n * 当前支持从作用域中移除对应对象，\n * 下次访问时在作用域内无缝创建新实例。\n *\n * <p>请注意，本工厂默认创建<i>基于类</i>的代理。\n * 可将 \"proxyTargetClass\" 属性设为 \"false\" 进行定制。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @since 2.0\n * @see #setProxyTargetClass\n */",
        ),
        (
            "\t/** The TargetSource that manages scoping. */",
            "\t/** 管理作用域的 TargetSource。 */",
        ),
        (
            "\t/** The name of the target bean. */",
            "\t/** 目标 Bean 的名称。 */",
        ),
        (
            "\t/** The cached singleton proxy. */",
            "\t/** 缓存的单例代理。 */",
        ),
        (
            "\t/**\n\t * Create a new ScopedProxyFactoryBean instance.\n\t */",
            "\t/**\n\t * 创建新的 ScopedProxyFactoryBean 实例。\n\t */",
        ),
        (
            "\t/**\n\t * Set the name of the bean that is to be scoped.\n\t */",
            "\t/**\n\t * 设置要放入作用域的 Bean 名称。\n\t */",
        ),
        (
            "\t\t// Add an introduction that implements only the methods on ScopedObject.",
            "\t\t// 添加仅实现 ScopedObject 上方法的引入。",
        ),
        (
            "\t\t// Add the AopInfrastructureBean marker to indicate that the scoped proxy\n\t\t// itself is not subject to auto-proxying! Only its target bean is.",
            "\t\t// 添加 AopInfrastructureBean 标记，表明作用域代理本身\n\t\t// 不会自动代理！仅其目标 Bean 会。",
        ),
    ],
    "ScopedProxyUtils.java": [
        (
            "/**\n * Utility class for creating a scoped proxy.\n *\n * <p>Used by ScopedProxyBeanDefinitionDecorator and ClassPathBeanDefinitionScanner.\n *\n * @author Mark Fisher\n * @author Juergen Hoeller\n * @author Rob Harrop\n * @author Sam Brannen\n * @since 2.5\n */",
            "/**\n * 创建作用域代理的工具类。\n *\n * <p>供 ScopedProxyBeanDefinitionDecorator 与 ClassPathBeanDefinitionScanner 使用。\n *\n * @author Mark Fisher\n * @author Juergen Hoeller\n * @author Rob Harrop\n * @author Sam Brannen\n * @since 2.5\n */",
        ),
        (
            "\t/**\n\t * Generate a scoped proxy for the supplied target bean, registering the target\n\t * bean with an internal name and setting 'targetBeanName' on the scoped proxy.\n\t * @param definition the original bean definition\n\t * @param registry the bean definition registry\n\t * @param proxyTargetClass whether to create a target class proxy\n\t * @return the scoped proxy definition\n\t * @see #getTargetBeanName(String)\n\t * @see #getOriginalBeanName(String)\n\t */",
            "\t/**\n\t * 为给定目标 Bean 生成作用域代理，\n\t * 以内部名称注册目标 Bean，并在作用域代理上设置 'targetBeanName'。\n\t * @param definition 原始 Bean 定义\n\t * @param registry Bean 定义注册表\n\t * @param proxyTargetClass 是否创建目标类代理\n\t * @return 作用域代理定义\n\t * @see #getTargetBeanName(String)\n\t * @see #getOriginalBeanName(String)\n\t */",
        ),
        (
            "\t\t// Create a scoped proxy definition for the original bean name,\n\t\t// \"hiding\" the target bean in an internal target definition.",
            "\t\t// 为原始 Bean 名称创建作用域代理定义，\n\t\t// 将目标 Bean「隐藏」在内部目标定义中。",
        ),
        (
            "\t\t\t// ScopedProxyFactoryBean's \"proxyTargetClass\" default is TRUE, so we don't need to set it explicitly here.",
            "\t\t\t// ScopedProxyFactoryBean 的 \"proxyTargetClass\" 默认为 TRUE，此处无需显式设置。",
        ),
        (
            "\t\t// Copy autowire settings from original bean definition.",
            "\t\t// 从原始 Bean 定义复制自动装配设置。",
        ),
        (
            "\t\t// The target bean should be ignored in favor of the scoped proxy.",
            "\t\t// 目标 Bean 应被忽略，优先使用作用域代理。",
        ),
        (
            "\t\t// Register the target bean as separate bean in the factory.",
            "\t\t// 在工厂中将目标 Bean 注册为独立 Bean。",
        ),
        (
            "\t\t// Return the scoped proxy definition as primary bean definition\n\t\t// (potentially an inner bean).",
            "\t\t// 将作用域代理定义作为主 Bean 定义返回\n\t\t// （可能是内部 Bean）。",
        ),
        (
            "\t/**\n\t * Generate the bean name that is used within the scoped proxy to reference the target bean.\n\t * @param originalBeanName the original name of bean\n\t * @return the generated bean to be used to reference the target bean\n\t * @see #getOriginalBeanName(String)\n\t */",
            "\t/**\n\t * 生成作用域代理内用于引用目标 Bean 的 Bean 名称。\n\t * @param originalBeanName Bean 的原始名称\n\t * @return 用于引用目标 Bean 的生成名称\n\t * @see #getOriginalBeanName(String)\n\t */",
        ),
        (
            "\t/**\n\t * Get the original bean name for the provided {@linkplain #getTargetBeanName\n\t * target bean name}.\n\t * @param targetBeanName the target bean name for the scoped proxy\n\t * @return the original bean name\n\t * @throws IllegalArgumentException if the supplied bean name does not refer\n\t * to the target of a scoped proxy\n\t * @since 5.1.10\n\t * @see #getTargetBeanName(String)\n\t * @see #isScopedTarget(String)\n\t */",
            "\t/**\n\t * 根据提供的 {@linkplain #getTargetBeanName 目标 Bean 名称}\n\t * 获取原始 Bean 名称。\n\t * @param targetBeanName 作用域代理的目标 Bean 名称\n\t * @return 原始 Bean 名称\n\t * @throws IllegalArgumentException 若给定名称不指向作用域代理的目标\n\t * @since 5.1.10\n\t * @see #getTargetBeanName(String)\n\t * @see #isScopedTarget(String)\n\t */",
        ),
        (
            "\t/**\n\t * Determine if the {@code beanName} is the name of a bean that references\n\t * the target bean within a scoped proxy.\n\t * @since 4.1.4\n\t */",
            "\t/**\n\t * 判断 {@code beanName} 是否为作用域代理内引用目标 Bean 的名称。\n\t * @since 4.1.4\n\t */",
        ),
    ],
}
