"""Chinese JavaDoc replacements for springframework wave20b aspectj annotation [1:7]."""

ANNOTATION_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "LazySingletonAspectInstanceFactoryDecorator.java": [
        (
            "/**\n * Decorator to cause a {@link MetadataAwareAspectInstanceFactory} to instantiate only once.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @since 2.0\n */",
            "/**\n * 装饰器，使 {@link MetadataAwareAspectInstanceFactory} 仅实例化一次。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @since 2.0\n */",
        ),
        (
            "\t/**\n\t * Create a new lazily initializing decorator for the given AspectInstanceFactory.\n\t * @param maaif the MetadataAwareAspectInstanceFactory to decorate\n\t */",
            "\t/**\n\t * 为给定 AspectInstanceFactory 创建新的延迟初始化装饰器。\n\t * @param maaif 要装饰的 MetadataAwareAspectInstanceFactory\n\t */",
        ),
        (
            "\t@Override\n\tpublic Object getAspectInstance() {",
            "\t/**\n\t * 获取切面实例，首次调用时延迟创建并缓存。\n\t */\n\t@Override\n\tpublic Object getAspectInstance() {",
        ),
        (
            "\tpublic boolean isMaterialized() {",
            "\t/**\n\t * 返回切面实例是否已物化（已创建）。\n\t */\n\tpublic boolean isMaterialized() {",
        ),
        (
            "\t@Override\n\tpublic String toString() {",
            "\t/**\n\t * 返回本装饰器的字符串表示。\n\t */\n\t@Override\n\tpublic String toString() {",
        ),
    ],
    "MetadataAwareAspectInstanceFactory.java": [
        (
            "/**\n * Subinterface of {@link org.springframework.aop.aspectj.AspectInstanceFactory}\n * that returns {@link AspectMetadata} associated with AspectJ-annotated classes.\n *\n * @author Rod Johnson\n * @since 2.0\n * @see AspectMetadata\n * @see org.aspectj.lang.reflect.AjType\n */",
            "/**\n * {@link org.springframework.aop.aspectj.AspectInstanceFactory} 的子接口，\n * 返回与 AspectJ 注解类关联的 {@link AspectMetadata}。\n *\n * @author Rod Johnson\n * @since 2.0\n * @see AspectMetadata\n * @see org.aspectj.lang.reflect.AjType\n */",
        ),
        (
            "\t/**\n\t * Get the AspectJ AspectMetadata for this factory's aspect.\n\t * @return the aspect metadata\n\t */",
            "\t/**\n\t * 获取本工厂所管理切面的 AspectJ AspectMetadata。\n\t * @return 切面元数据\n\t */",
        ),
        (
            "\t/**\n\t * Get the best possible creation mutex for this factory.\n\t * @return the mutex object (may be {@code null} for no mutex to use)\n\t * @since 4.3\n\t */",
            "\t/**\n\t * 获取本工厂尽可能最佳的创建互斥体。\n\t * @return 互斥对象（可为 {@code null} 表示不使用互斥）\n\t * @since 4.3\n\t */",
        ),
    ],
    "NotAnAtAspectException.java": [
        (
            "/**\n * Extension of AopConfigException thrown when trying to perform\n * an advisor generation operation on a class that is not an\n * AspectJ annotation-style aspect.\n *\n * @author Rod Johnson\n * @since 2.0\n */",
            "/**\n * 对非 AspectJ 注解风格切面类执行 Advisor 生成操作时抛出的\n * {@link org.springframework.aop.framework.AopConfigException} 扩展。\n *\n * @author Rod Johnson\n * @since 2.0\n */",
        ),
        (
            "\t/**\n\t * Create a new NotAnAtAspectException for the given class.\n\t * @param nonAspectClass the offending class\n\t */",
            "\t/**\n\t * 为给定类创建新的 NotAnAtAspectException。\n\t * @param nonAspectClass 违规的类\n\t */",
        ),
        (
            "\t/**\n\t * Returns the offending class.\n\t */",
            "\t/**\n\t * 返回违规的类。\n\t */",
        ),
    ],
    "PrototypeAspectInstanceFactory.java": [
        (
            "/**\n * {@link org.springframework.aop.aspectj.AspectInstanceFactory} backed by a\n * {@link BeanFactory}-provided prototype, enforcing prototype semantics.\n *\n * <p>Note that this may instantiate multiple times, which probably won't give the\n * semantics you expect. Use a {@link LazySingletonAspectInstanceFactoryDecorator}\n * to wrap this to ensure only one new aspect comes back.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @since 2.0\n * @see org.springframework.beans.factory.BeanFactory\n * @see LazySingletonAspectInstanceFactoryDecorator\n */",
            "/**\n * 由 {@link BeanFactory} 提供的 prototype Bean 支持的\n * {@link org.springframework.aop.aspectj.AspectInstanceFactory}，强制 prototype 语义。\n *\n * <p>注意，这可能多次实例化，通常无法得到预期语义。\n * 可用 {@link LazySingletonAspectInstanceFactoryDecorator} 包装，\n * 确保仅返回一个新切面实例。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @since 2.0\n * @see org.springframework.beans.factory.BeanFactory\n * @see LazySingletonAspectInstanceFactoryDecorator\n */",
        ),
        (
            "\t/**\n\t * Create a PrototypeAspectInstanceFactory. AspectJ will be called to\n\t * introspect to create AJType metadata using the type returned for the\n\t * given bean name from the BeanFactory.\n\t * @param beanFactory the BeanFactory to obtain instance(s) from\n\t * @param name the name of the bean\n\t */",
            "\t/**\n\t * 创建 PrototypeAspectInstanceFactory。AspectJ 将内省\n\t * BeanFactory 中给定 Bean 名称对应的类型以创建 AJType 元数据。\n\t * @param beanFactory 获取实例的 BeanFactory\n\t * @param name Bean 名称\n\t */",
        ),
    ],
    "ReflectiveAspectJAdvisorFactory.java": [
        (
            "/**\n * Factory that can create Spring AOP Advisors given AspectJ classes from\n * classes honoring AspectJ's annotation syntax, using reflection to invoke the\n * corresponding advice methods.\n *\n * @author Rod Johnson\n * @author Adrian Colyer\n * @author Juergen Hoeller\n * @author Ramnivas Laddad\n * @author Phillip Webb\n * @author Sam Brannen\n * @since 2.0\n */",
            "/**\n * 工厂：针对遵循 AspectJ 注解语法的类，\n * 通过反射调用对应通知方法，创建 Spring AOP Advisor。\n *\n * @author Rod Johnson\n * @author Adrian Colyer\n * @author Juergen Hoeller\n * @author Ramnivas Laddad\n * @author Phillip Webb\n * @author Sam Brannen\n * @since 2.0\n */",
        ),
        (
            "\t// Exclude @Pointcut methods",
            "\t// 排除 @Pointcut 方法",
        ),
        (
            "\t\t// Note: although @After is ordered before @AfterReturning and @AfterThrowing,\n\t\t// an @After advice method will actually be invoked after @AfterReturning and\n\t\t// @AfterThrowing methods due to the fact that AspectJAfterAdvice.invoke(MethodInvocation)\n\t\t// invokes proceed() in a `try` block and only invokes the @After advice method\n\t\t// in a corresponding `finally` block.",
            "\t\t// 注意：虽然 @After 排序在 @AfterReturning 与 @AfterThrowing 之前，\n\t\t// 但 @After 通知方法实际在 @AfterReturning 与 @AfterThrowing 之后调用，\n\t\t// 因为 AspectJAfterAdvice.invoke(MethodInvocation) 在 `try` 块中调用 proceed()，\n\t\t// 仅在对应 `finally` 块中调用 @After 通知方法。",
        ),
        (
            "\t/**\n\t * Create a new {@code ReflectiveAspectJAdvisorFactory}.\n\t */",
            "\t/**\n\t * 创建新的 {@code ReflectiveAspectJAdvisorFactory}。\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@code ReflectiveAspectJAdvisorFactory}, propagating the given\n\t * {@link BeanFactory} to the created {@link AspectJExpressionPointcut} instances,\n\t * for bean pointcut handling as well as consistent {@link ClassLoader} resolution.\n\t * @param beanFactory the BeanFactory to propagate (may be {@code null})\n\t * @since 4.3.6\n\t * @see AspectJExpressionPointcut#setBeanFactory\n\t * @see org.springframework.beans.factory.config.ConfigurableBeanFactory#getBeanClassLoader()\n\t */",
            "\t/**\n\t * 创建新的 {@code ReflectiveAspectJAdvisorFactory}，\n\t * 将给定 {@link BeanFactory} 传播到创建的 {@link AspectJExpressionPointcut} 实例，\n\t * 用于 Bean 切入点处理及一致的 {@link ClassLoader} 解析。\n\t * @param beanFactory 要传播的 BeanFactory（可为 {@code null}）\n\t * @since 4.3.6\n\t * @see AspectJExpressionPointcut#setBeanFactory\n\t * @see org.springframework.beans.factory.config.ConfigurableBeanFactory#getBeanClassLoader()\n\t */",
        ),
        (
            "\t\t// We need to wrap the MetadataAwareAspectInstanceFactory with a decorator\n\t\t// so that it will only instantiate once.",
            "\t\t// 需用装饰器包装 MetadataAwareAspectInstanceFactory，使其仅实例化一次。",
        ),
        (
            "\t\t\t\t// Prior to Spring Framework 5.2.7, advisors.size() was supplied as the declarationOrderInAspect\n\t\t\t\t// to getAdvisor(...) to represent the \"current position\" in the declared methods list.\n\t\t\t\t// However, since Java 7 the \"current position\" is not valid since the JDK no longer\n\t\t\t\t// returns declared methods in the order in which they are declared in the source code.\n\t\t\t\t// Thus, we now hard code the declarationOrderInAspect to 0 for all advice methods\n\t\t\t\t// discovered via reflection in order to support reliable advice ordering across JVM launches.\n\t\t\t\t// Specifically, a value of 0 aligns with the default value used in\n\t\t\t\t// AspectJPrecedenceComparator.getAspectDeclarationOrder(Advisor).",
            "\t\t\t\t// Spring Framework 5.2.7 之前，advisors.size() 作为 declarationOrderInAspect\n\t\t\t\t// 传入 getAdvisor(...)，表示声明方法列表中的「当前位置」。\n\t\t\t\t// 但自 Java 7 起 JDK 不再按源码声明顺序返回 declared 方法，\n\t\t\t\t// 「当前位置」已不可靠。因此现在对反射发现的所有通知方法\n\t\t\t\t// 将 declarationOrderInAspect 硬编码为 0，\n\t\t\t\t// 以支持跨 JVM 启动的可靠通知排序。\n\t\t\t\t// 具体地，0 与 AspectJPrecedenceComparator.getAspectDeclarationOrder(Advisor) 的默认值一致。",
        ),
        (
            "\t\t// If it's a per target aspect, emit the dummy instantiating aspect.",
            "\t\t// 若为 per target 切面，发出用于实例化的虚拟 Advisor。",
        ),
        (
            "\t\t// Find introduction fields.",
            "\t\t// 查找引介字段。",
        ),
        (
            "\t/**\n\t * Build a {@link org.springframework.aop.aspectj.DeclareParentsAdvisor}\n\t * for the given introduction field.\n\t * <p>Resulting Advisors will need to be evaluated for targets.\n\t * @param introductionField the field to introspect\n\t * @return the Advisor instance, or {@code null} if not an Advisor\n\t */",
            "\t/**\n\t * 为给定引介字段构建 {@link org.springframework.aop.aspectj.DeclareParentsAdvisor}。\n\t * <p>生成的 Advisor 需针对目标进行评估。\n\t * @param introductionField 要内省的字段\n\t * @return Advisor 实例，若非 Advisor 则返回 {@code null}\n\t */",
        ),
        (
            "\t\t\t// Not an introduction field",
            "\t\t\t// 非引介字段",
        ),
        (
            "\t\t// If we get here, we know we have an AspectJ method.\n\t\t// Check that it's an AspectJ-annotated class",
            "\t\t// 执行至此说明是 AspectJ 方法。\n\t\t// 检查是否为 AspectJ 注解类",
        ),
        (
            "\t\t// Now to configure the advice...",
            "\t\t// 配置通知...",
        ),
        (
            "\t/**\n\t * Synthetic advisor that instantiates the aspect.\n\t * Triggered by per-clause pointcut on non-singleton aspect.\n\t * The advice has no effect.\n\t */",
            "\t/**\n\t * 用于实例化切面的合成 Advisor。\n\t * 由非单例切面上的 per-clause 切入点触发。\n\t * 该通知本身无实际效果。\n\t */",
        ),
    ],
    "SimpleMetadataAwareAspectInstanceFactory.java": [
        (
            "/**\n * Implementation of {@link MetadataAwareAspectInstanceFactory} that\n * creates a new instance of the specified aspect class for every\n * {@link #getAspectInstance()} call.\n *\n * @author Juergen Hoeller\n * @since 2.0.4\n */",
            "/**\n * {@link MetadataAwareAspectInstanceFactory} 的实现，\n * 每次 {@link #getAspectInstance()} 调用都创建指定切面类的新实例。\n *\n * @author Juergen Hoeller\n * @since 2.0.4\n */",
        ),
        (
            "\t/**\n\t * Create a new SimpleMetadataAwareAspectInstanceFactory for the given aspect class.\n\t * @param aspectClass the aspect class\n\t * @param aspectName the aspect name\n\t */",
            "\t/**\n\t * 为给定切面类创建新的 SimpleMetadataAwareAspectInstanceFactory。\n\t * @param aspectClass 切面类\n\t * @param aspectName 切面名称\n\t */",
        ),
    ],
    "SingletonMetadataAwareAspectInstanceFactory.java": [
        (
            "/**\n * Implementation of {@link MetadataAwareAspectInstanceFactory} that is backed\n * by a specified singleton object, returning the same instance for every\n * {@link #getAspectInstance()} call.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @since 2.0\n * @see SimpleMetadataAwareAspectInstanceFactory\n */",
            "/**\n * {@link MetadataAwareAspectInstanceFactory} 的实现，\n * 由指定单例对象支持，每次 {@link #getAspectInstance()} 调用返回同一实例。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @since 2.0\n * @see SimpleMetadataAwareAspectInstanceFactory\n */",
        ),
        (
            "\t/**\n\t * Create a new SingletonMetadataAwareAspectInstanceFactory for the given aspect.\n\t * @param aspectInstance the singleton aspect instance\n\t * @param aspectName the name of the aspect\n\t */",
            "\t/**\n\t * 为给定切面创建新的 SingletonMetadataAwareAspectInstanceFactory。\n\t * @param aspectInstance 单例切面实例\n\t * @param aspectName 切面名称\n\t */",
        ),
    ],
}
