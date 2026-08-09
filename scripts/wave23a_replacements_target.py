"""Chinese JavaDoc replacements for springframework wave23a target source [19]."""

TARGET_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "AbstractBeanFactoryBasedTargetSource.java": [
        (
            "/**\n * Base class for {@link org.springframework.aop.TargetSource} implementations\n * that are based on a Spring {@link org.springframework.beans.factory.BeanFactory},\n * delegating to Spring-managed bean instances.\n *\n * <p>Subclasses can create prototype instances or lazily access a\n * singleton target, for example. See {@link LazyInitTargetSource} and\n * {@link AbstractPrototypeBasedTargetSource}'s subclasses for concrete strategies.\n *\n * <p>BeanFactory-based TargetSources are serializable. This involves\n * disconnecting the current target and turning into a {@link SingletonTargetSource}.\n *\n * @author Juergen Hoeller\n * @author Rod Johnson\n * @since 1.1.4\n * @see org.springframework.beans.factory.BeanFactory#getBean\n * @see LazyInitTargetSource\n * @see PrototypeTargetSource\n * @see ThreadLocalTargetSource\n * @see CommonsPool2TargetSource\n */",
            "/**\n * 基于 Spring {@link org.springframework.beans.factory.BeanFactory} 的\n * {@link org.springframework.aop.TargetSource} 实现基类，\n * 委托给 Spring 管理的 bean 实例。\n *\n * <p>子类可创建原型实例或延迟访问单例目标等。\n * 具体策略见 {@link LazyInitTargetSource} 及\n * {@link AbstractPrototypeBasedTargetSource} 的子类。\n *\n * <p>基于 BeanFactory 的 TargetSource 可序列化。\n * 这涉及断开当前目标并转为 {@link SingletonTargetSource}。\n *\n * @author Juergen Hoeller\n * @author Rod Johnson\n * @since 1.1.4\n * @see org.springframework.beans.factory.BeanFactory#getBean\n * @see LazyInitTargetSource\n * @see PrototypeTargetSource\n * @see ThreadLocalTargetSource\n * @see CommonsPool2TargetSource\n */",
        ),
        (
            "\t/** use serialVersionUID from Spring 1.2.7 for interoperability. */",
            "\t/** 使用 Spring 1.2.7 的 serialVersionUID 以保持互操作性。 */",
        ),
        (
            "\t/** Logger available to subclasses. */",
            "\t/** 子类可用的 Logger。 */",
        ),
        (
            "\t/** Name of the target bean we will create on each invocation. */",
            "\t/** 每次调用时将创建的目标 bean 名称。 */",
        ),
        (
            "\t/** Class of the target. */",
            "\t/** 目标的类。 */",
        ),
        (
            "\t/**\n\t * BeanFactory that owns this TargetSource. We need to hold onto this\n\t * reference so that we can create new prototype instances as necessary.\n\t */",
            "\t/**\n\t * 拥有本 TargetSource 的 BeanFactory。\n\t * 须持有此引用以便必要时创建新原型实例。\n\t */",
        ),
        (
            "\t/**\n\t * Set the name of the target bean in the factory.\n\t * <p>The target bean should not be a singleton, else the same instance will\n\t * always be obtained from the factory, resulting in the same behavior as\n\t * provided by {@link SingletonTargetSource}.\n\t * @param targetBeanName name of the target bean in the BeanFactory\n\t * that owns this interceptor\n\t * @see SingletonTargetSource\n\t */",
            "\t/**\n\t * 设置工厂中目标 bean 的名称。\n\t * <p>目标 bean 不应为单例，否则始终从工厂获得同一实例，\n\t * 行为与 {@link SingletonTargetSource} 相同。\n\t * @param targetBeanName 拥有本拦截器的 BeanFactory 中目标 bean 的名称\n\t * @see SingletonTargetSource\n\t */",
        ),
        (
            "\t/**\n\t * Return the name of the target bean in the factory.\n\t */",
            "\t/**\n\t * 返回工厂中目标 bean 的名称。\n\t */",
        ),
        (
            "\t/**\n\t * Specify the target class explicitly, to avoid any kind of access to the\n\t * target bean (for example, to avoid initialization of a FactoryBean instance).\n\t * <p>Default is to detect the type automatically, through a {@code getType}\n\t * call on the BeanFactory (or even a full {@code getBean} call as fallback).\n\t */",
            "\t/**\n\t * 显式指定目标类，避免任何形式访问目标 bean\n\t * （例如避免初始化 FactoryBean 实例）。\n\t * <p>默认通过 BeanFactory 的 {@code getType} 调用\n\t * （或作为回退的完整 {@code getBean} 调用）自动检测类型。\n\t */",
        ),
        (
            "\t/**\n\t * Set the owning BeanFactory. We need to save a reference so that we can\n\t * use the {@code getBean} method on every invocation.\n\t */",
            "\t/**\n\t * 设置所属的 BeanFactory。\n\t * 须保存引用以便每次调用时使用 {@code getBean} 方法。\n\t */",
        ),
        (
            "\t/**\n\t * Return the owning BeanFactory.\n\t */",
            "\t/**\n\t * 返回所属的 BeanFactory。\n\t */",
        ),
        (
            "\t\t\t// Full check within synchronization, entering the BeanFactory interaction algorithm only once...",
            "\t\t\t// 在同步块内完整检查，仅一次进入 BeanFactory 交互算法...",
        ),
        (
            "\t\t\t\t// Determine type of the target bean.",
            "\t\t\t\t// 确定目标 bean 的类型。",
        ),
        (
            "\t/**\n\t * Copy configuration from the other AbstractBeanFactoryBasedTargetSource object.\n\t * Subclasses should override this if they wish to expose it.\n\t * @param other object to copy configuration from\n\t */",
            "\t/**\n\t * 从其他 AbstractBeanFactoryBasedTargetSource 对象复制配置。\n\t * 若子类希望暴露此方法，应覆盖。\n\t * @param other 要复制配置的对象\n\t */",
        ),
    ],
}
