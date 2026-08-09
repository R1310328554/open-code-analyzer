"""Chinese JavaDoc replacements for springframework wave20a aspectj annotation [11:20]."""

ASPECTJ_ANNOTATION_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "AbstractAspectJAdvisorFactory.java": [
        (
            "/**\n * Abstract base class for factories that can create Spring AOP Advisors\n * given AspectJ classes from classes honoring the AspectJ 5 annotation syntax.\n *\n * <p>This class handles annotation parsing and validation functionality.\n * It does not actually generate Spring AOP Advisors, which is deferred to subclasses.\n *\n * @author Rod Johnson\n * @author Adrian Colyer\n * @author Juergen Hoeller\n * @author Sam Brannen\n * @since 2.0\n */",
            "/**\n * 可依据遵循 AspectJ 5 注解语法的类创建 Spring AOP 通知器的工厂抽象基类。\n *\n * <p>本类处理注解解析与校验功能。\n * 实际生成 Spring AOP 通知器的工作由子类完成。\n *\n * @author Rod Johnson\n * @author Adrian Colyer\n * @author Juergen Hoeller\n * @author Sam Brannen\n * @since 2.0\n */",
        ),
        (
            "\t/**\n\t * System property that instructs Spring to ignore ajc-compiled aspects\n\t * for Spring AOP proxying, restoring traditional Spring behavior for\n\t * scenarios where both weaving and AspectJ auto-proxying are enabled.\n\t * <p>The default is \"false\". Consider switching this to \"true\" if you\n\t * encounter double execution of your aspects in a given build setup.\n\t * Note that we recommend restructuring your AspectJ configuration to\n\t * avoid such double exposure of an AspectJ aspect to begin with.\n\t * @since 6.1.15\n\t */",
            "\t/**\n\t * 指示 Spring 在 Spring AOP 代理时忽略 ajc 编译切面的系统属性，\n\t * 在同时启用织入与 AspectJ 自动代理的场景下恢复传统 Spring 行为。\n\t * <p>默认为 \"false\"。若在给定构建配置中遇到切面重复执行，\n\t * 可考虑设为 \"true\"。\n\t * 仍建议重构 AspectJ 配置，从源头避免切面被双重暴露。\n\t * @since 6.1.15\n\t */",
        ),
        (
            "\t/** Logger available to subclasses. */",
            "\t/** 子类可用的日志记录器。 */",
        ),
        (
            "\t/**\n\t * Find and return the first AspectJ annotation on the given method\n\t * (there <i>should</i> only be one anyway...).\n\t */",
            "\t/**\n\t * 查找并返回给定方法上的第一个 AspectJ 注解\n\t * （理论上<i>应</i>只有一个...）。\n\t */",
        ),
        (
            "\t/**\n\t * Enum for AspectJ annotation types.\n\t * @see AspectJAnnotation#getAnnotationType()\n\t */",
            "\t/**\n\t * AspectJ 注解类型枚举。\n\t * @see AspectJAnnotation#getAnnotationType()\n\t */",
        ),
        (
            "\t/**\n\t * Class modeling an AspectJ annotation, exposing its type enumeration and\n\t * pointcut String.\n\t */",
            "\t/**\n\t * 建模 AspectJ 注解的类，暴露其类型枚举与切点字符串。\n\t */",
        ),
        (
            "\t/**\n\t * ParameterNameDiscoverer implementation that analyzes the arg names\n\t * specified at the AspectJ annotation level.\n\t */",
            "\t/**\n\t * 分析 AspectJ 注解级指定参数名的 ParameterNameDiscoverer 实现。\n\t */",
        ),
    ],
    "AnnotationAwareAspectJAutoProxyCreator.java": [
        (
            "/**\n * {@link AspectJAwareAdvisorAutoProxyCreator} subclass that processes all AspectJ\n * annotation aspects in the current application context, as well as Spring Advisors.\n *\n * <p>Any AspectJ annotated classes will automatically be recognized, and their\n * advice applied if Spring AOP's proxy-based model is capable of applying it.\n * This covers method execution joinpoints.\n *\n * <p>If the &lt;aop:include&gt; element is used, only @AspectJ beans with names matched by\n * an include pattern will be considered as defining aspects to use for Spring auto-proxying.\n *\n * <p>Processing of Spring Advisors follows the rules established in\n * {@link org.springframework.aop.framework.autoproxy.AbstractAdvisorAutoProxyCreator}.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @since 2.0\n * @see org.springframework.aop.aspectj.annotation.AspectJAdvisorFactory\n */",
            "/**\n * 处理当前应用上下文中所有 AspectJ 注解切面及 Spring 通知器的\n * {@link AspectJAwareAdvisorAutoProxyCreator} 子类。\n *\n * <p>任意带 AspectJ 注解的类将被自动识别，\n * 若 Spring AOP 基于代理的模型能应用其通知则予以应用。\n * 这涵盖方法执行连接点。\n *\n * <p>若使用 &lt;aop:include&gt; 元素，\n * 仅名称匹配 include 模式的 @AspectJ Bean 才被视为用于 Spring 自动代理的切面定义。\n *\n * <p>Spring 通知器的处理遵循\n * {@link org.springframework.aop.framework.autoproxy.AbstractAdvisorAutoProxyCreator} 中的规则。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @since 2.0\n * @see org.springframework.aop.aspectj.annotation.AspectJAdvisorFactory\n */",
        ),
        (
            "\t/**\n\t * Set a list of regex patterns, matching eligible @AspectJ bean names.\n\t * <p>Default is to consider all @AspectJ beans as eligible.\n\t */",
            "\t/**\n\t * 设置正则模式列表，匹配符合条件的 @AspectJ Bean 名称。\n\t * <p>默认将所有 @AspectJ Bean 视为符合条件。\n\t */",
        ),
        (
            "\t\t// Add all the Spring advisors found according to superclass rules.",
            "\t\t// 按超类规则添加找到的所有 Spring 通知器。",
        ),
        (
            "\t\t// Build Advisors for all AspectJ aspects in the bean factory.",
            "\t\t// 为 Bean 工厂中所有 AspectJ 切面构建通知器。",
        ),
        (
            "\t\t// Previously we setProxyTargetClass(true) in the constructor, but that has too\n\t\t// broad an impact. Instead we now override isInfrastructureClass to avoid proxying\n\t\t// aspects. I'm not entirely happy with that as there is no good reason not\n\t\t// to advise aspects, except that it causes advice invocation to go through a\n\t\t// proxy, and if the aspect implements, for example, the Ordered interface it will be\n\t\t// proxied by that interface and fail at runtime as the advice method is not\n\t\t// defined on the interface. We could potentially relax the restriction about\n\t\t// not advising aspects in the future.",
            "\t\t// 此前在构造器中 setProxyTargetClass(true)，但影响过广。\n\t\t// 现改为覆盖 isInfrastructureClass 以避免代理切面。\n\t\t// 对此并不完全满意——并非没有充分理由去通知切面，\n\t\t// 只是会导致通知调用经代理进行；若切面实现 Ordered 等接口，\n\t\t// 将按该接口代理并在运行时失败，因接口上未定义通知方法。\n\t\t// 未来或可放宽不通知切面的限制。",
        ),
        (
            "\t/**\n\t * Check whether the given aspect bean is eligible for auto-proxying.\n\t * <p>If no &lt;aop:include&gt; elements were used then \"includePatterns\" will be\n\t * {@code null} and all beans are included. If \"includePatterns\" is non-null,\n\t * then one of the patterns must match.\n\t */",
            "\t/**\n\t * 检查给定切面 Bean 是否符合自动代理条件。\n\t * <p>若未使用 &lt;aop:include&gt; 元素，则 \"includePatterns\" 为\n\t * {@code null}，所有 Bean 均包含。\n\t * 若 \"includePatterns\" 非 null，则须匹配其中一个模式。\n\t */",
        ),
        (
            "\t/**\n\t * Subclass of BeanFactoryAspectJAdvisorsBuilderAdapter that delegates to\n\t * surrounding AnnotationAwareAspectJAutoProxyCreator facilities.\n\t */",
            "\t/**\n\t * BeanFactoryAspectJAdvisorsBuilderAdapter 的子类，\n\t * 委托给外围 AnnotationAwareAspectJAutoProxyCreator 设施。\n\t */",
        ),
    ],
    "AspectJAdvisorBeanRegistrationAotProcessor.java": [
        (
            "/**\n * An AOT {@link BeanRegistrationAotProcessor} that detects the presence of\n * classes compiled with AspectJ and adds the related required field hints.\n *\n * @author Sebastien Deleuze\n * @since 6.1\n */",
            "/**\n * AOT {@link BeanRegistrationAotProcessor}，\n * 检测由 AspectJ 编译的类是否存在，并添加相关必填字段提示。\n *\n * @author Sebastien Deleuze\n * @since 6.1\n */",
        ),
    ],
    "AspectJAdvisorFactory.java": [
        (
            "/**\n * Interface for factories that can create Spring AOP Advisors from classes\n * annotated with AspectJ annotation syntax.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @since 2.0\n * @see AspectMetadata\n * @see org.aspectj.lang.reflect.AjTypeSystem\n */",
            "/**\n * 可从带 AspectJ 注解语法的类创建 Spring AOP 通知器的工厂接口。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @since 2.0\n * @see AspectMetadata\n * @see org.aspectj.lang.reflect.AjTypeSystem\n */",
        ),
        (
            "\t/**\n\t * Determine whether the given class is an aspect, as reported\n\t * by AspectJ's {@link org.aspectj.lang.reflect.AjTypeSystem}.\n\t * <p>Will simply return {@code false} if the supposed aspect is\n\t * invalid (such as an extension of a concrete aspect class).\n\t * Will return true for some aspects that Spring AOP cannot process,\n\t * such as those with unsupported instantiation models.\n\t * Use the {@link #validate} method to handle these cases if necessary.\n\t * @param clazz the supposed annotation-style AspectJ class\n\t * @return whether this class is recognized by AspectJ as an aspect class\n\t */",
            "\t/**\n\t * 判断给定类是否为切面，\n\t * 依据 AspectJ {@link org.aspectj.lang.reflect.AjTypeSystem} 的报告。\n\t * <p>若所谓切面无效（如具体切面类的扩展），直接返回 {@code false}。\n\t * 对 Spring AOP 无法处理的切面（如不支持的实例化模型）也可能返回 true。\n\t * 必要时使用 {@link #validate} 处理这些情况。\n\t * @param clazz 假定的注解风格 AspectJ 类\n\t * @return 该类是否被 AspectJ 识别为切面类\n\t */",
        ),
        (
            "\t/**\n\t * Is the given class a valid AspectJ aspect class?\n\t * @param aspectClass the supposed AspectJ annotation-style class to validate\n\t * @throws AopConfigException if the class is an invalid aspect\n\t * (which can never be legal)\n\t * @throws NotAnAtAspectException if the class is not an aspect at all\n\t * (which may or may not be legal, depending on the context)\n\t */",
            "\t/**\n\t * 给定类是否为有效的 AspectJ 切面类？\n\t * @param aspectClass 待校验的假定 AspectJ 注解风格类\n\t * @throws AopConfigException 若类为无效切面（永不可合法）\n\t * @throws NotAnAtAspectException 若类根本不是切面\n\t * （是否合法取决于上下文）\n\t */",
        ),
        (
            "\t/**\n\t * Build Spring AOP Advisors for all annotated At-AspectJ methods\n\t * on the specified aspect instance.\n\t * @param aspectInstanceFactory the aspect instance factory\n\t * (not the aspect instance itself in order to avoid eager instantiation)\n\t * @return a list of advisors for this class\n\t */",
            "\t/**\n\t * 为指定切面实例上所有带 At-AspectJ 注解的方法\n\t * 构建 Spring AOP 通知器。\n\t * @param aspectInstanceFactory 切面实例工厂\n\t * （非切面实例本身，以避免过早实例化）\n\t * @return 本类的通知器列表\n\t */",
        ),
        (
            "\t/**\n\t * Build a Spring AOP Advisor for the given AspectJ advice method.\n\t * @param candidateAdviceMethod the candidate advice method\n\t * @param aspectInstanceFactory the aspect instance factory\n\t * @param declarationOrder the declaration order within the aspect\n\t * @param aspectName the name of the aspect\n\t * @return {@code null} if the method is not an AspectJ advice method\n\t * or if it is a pointcut that will be used by other advice but will not\n\t * create a Spring advice in its own right\n\t */",
            "\t/**\n\t * 为给定 AspectJ 通知方法构建 Spring AOP 通知器。\n\t * @param candidateAdviceMethod 候选通知方法\n\t * @param aspectInstanceFactory 切面实例工厂\n\t * @param declarationOrder 切面内的声明顺序\n\t * @param aspectName 切面名称\n\t * @return 若方法不是 AspectJ 通知方法，\n\t * 或为供其他通知使用但不单独创建 Spring 通知的切点，则返回 {@code null}\n\t */",
        ),
        (
            "\t/**\n\t * Build a Spring AOP Advice for the given AspectJ advice method.\n\t * @param candidateAdviceMethod the candidate advice method\n\t * @param expressionPointcut the AspectJ expression pointcut\n\t * @param aspectInstanceFactory the aspect instance factory\n\t * @param declarationOrder the declaration order within the aspect\n\t * @param aspectName the name of the aspect\n\t * @return {@code null} if the method is not an AspectJ advice method\n\t * or if it is a pointcut that will be used by other advice but will not\n\t * create a Spring advice in its own right\n\t * @see org.springframework.aop.aspectj.AspectJAroundAdvice\n\t * @see org.springframework.aop.aspectj.AspectJMethodBeforeAdvice\n\t * @see org.springframework.aop.aspectj.AspectJAfterAdvice\n\t * @see org.springframework.aop.aspectj.AspectJAfterReturningAdvice\n\t * @see org.springframework.aop.aspectj.AspectJAfterThrowingAdvice\n\t */",
            "\t/**\n\t * 为给定 AspectJ 通知方法构建 Spring AOP Advice。\n\t * @param candidateAdviceMethod 候选通知方法\n\t * @param expressionPointcut AspectJ 表达式切点\n\t * @param aspectInstanceFactory 切面实例工厂\n\t * @param declarationOrder 切面内的声明顺序\n\t * @param aspectName 切面名称\n\t * @return 若方法不是 AspectJ 通知方法，\n\t * 或为供其他通知使用但不单独创建 Spring 通知的切点，则返回 {@code null}\n\t * @see org.springframework.aop.aspectj.AspectJAroundAdvice\n\t * @see org.springframework.aop.aspectj.AspectJMethodBeforeAdvice\n\t * @see org.springframework.aop.aspectj.AspectJAfterAdvice\n\t * @see org.springframework.aop.aspectj.AspectJAfterReturningAdvice\n\t * @see org.springframework.aop.aspectj.AspectJAfterThrowingAdvice\n\t */",
        ),
    ],
    "AspectJBeanFactoryInitializationAotProcessor.java": [
        (
            "/**\n * {@link BeanFactoryInitializationAotProcessor} implementation responsible for registering\n * hints for AOP advices.\n *\n * @author Sebastien Deleuze\n * @author Stephane Nicoll\n * @since 6.0.11\n */",
            "/**\n * 负责注册 AOP 通知提示的\n * {@link BeanFactoryInitializationAotProcessor} 实现。\n *\n * @author Sebastien Deleuze\n * @author Stephane Nicoll\n * @since 6.0.11\n */",
        ),
        (
            "\t/**\n\t * Inner class to avoid a hard dependency on AspectJ at runtime.\n\t */",
            "\t/**\n\t * 内部类，避免运行时对 AspectJ 的硬依赖。\n\t */",
        ),
    ],
    "AspectJProxyFactory.java": [
        (
            "/**\n * AspectJ-based proxy factory, allowing for programmatic building\n * of proxies which include AspectJ aspects (code style as well\n * annotation style).\n *\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @author Ramnivas Laddad\n * @since 2.0\n * @see #addAspect(Object)\n * @see #addAspect(Class)\n * @see #getProxy()\n * @see #getProxy(ClassLoader)\n * @see org.springframework.aop.framework.ProxyFactory\n */",
            "/**\n * 基于 AspectJ 的代理工厂，支持编程式构建\n * 包含 AspectJ 切面（代码风格与注解风格）的代理。\n *\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @author Ramnivas Laddad\n * @since 2.0\n * @see #addAspect(Object)\n * @see #addAspect(Class)\n * @see #getProxy()\n * @see #getProxy(ClassLoader)\n * @see org.springframework.aop.framework.ProxyFactory\n */",
        ),
        (
            "\t/** Cache for singleton aspect instances. */",
            "\t/** 单例切面实例缓存。 */",
        ),
        (
            "\t/**\n\t * Create a new AspectJProxyFactory.\n\t */",
            "\t/**\n\t * 创建新的 AspectJProxyFactory。\n\t */",
        ),
        (
            "\t/**\n\t * Create a new AspectJProxyFactory.\n\t * <p>Will proxy all interfaces that the given target implements.\n\t * @param target the target object to be proxied\n\t */",
            "\t/**\n\t * 创建新的 AspectJProxyFactory。\n\t * <p>将代理给定目标实现的所有接口。\n\t * @param target 待代理的目标对象\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@code AspectJProxyFactory}.\n\t * No target, only interfaces. Must add interceptors.\n\t */",
            "\t/**\n\t * 创建新的 {@code AspectJProxyFactory}。\n\t * 无目标，仅接口。须添加拦截器。\n\t */",
        ),
        (
            "\t/**\n\t * Add the supplied aspect instance to the chain. The type of the aspect instance\n\t * supplied must be a singleton aspect. True singleton lifecycle is not honored when\n\t * using this method - the caller is responsible for managing the lifecycle of any\n\t * aspects added in this way.\n\t * @param aspectInstance the AspectJ aspect instance\n\t */",
            "\t/**\n\t * 将所供切面实例添加到链中。所供切面实例类型须为单例切面。\n\t * 使用本方法时不遵守真正的单例生命周期——\n\t * 调用方负责管理以此方式添加的切面的生命周期。\n\t * @param aspectInstance AspectJ 切面实例\n\t */",
        ),
        (
            "\t/**\n\t * Add an aspect of the supplied type to the end of the advice chain.\n\t * @param aspectClass the AspectJ aspect class\n\t */",
            "\t/**\n\t * 将所供类型的切面添加到通知链末尾。\n\t * @param aspectClass AspectJ 切面类\n\t */",
        ),
        (
            "\t/**\n\t * Add all {@link Advisor Advisors} from the supplied {@link MetadataAwareAspectInstanceFactory}\n\t * to the current chain. Exposes any special purpose {@link Advisor Advisors} if needed.\n\t * @see AspectJProxyUtils#makeAdvisorChainAspectJCapableIfNecessary(List)\n\t */",
            "\t/**\n\t * 将所供 {@link MetadataAwareAspectInstanceFactory} 中的全部\n\t * {@link Advisor Advisors} 添加到当前链。\n\t * 必要时暴露特殊用途的 {@link Advisor Advisors}。\n\t * @see AspectJProxyUtils#makeAdvisorChainAspectJCapableIfNecessary(List)\n\t */",
        ),
        (
            "\t/**\n\t * Create an {@link AspectMetadata} instance for the supplied aspect type.\n\t */",
            "\t/**\n\t * 为所供切面类型创建 {@link AspectMetadata} 实例。\n\t */",
        ),
        (
            "\t/**\n\t * Create a {@link MetadataAwareAspectInstanceFactory} for the supplied aspect type. If the aspect type\n\t * has no per clause, then a {@link SingletonMetadataAwareAspectInstanceFactory} is returned, otherwise\n\t * a {@link PrototypeAspectInstanceFactory} is returned.\n\t */",
            "\t/**\n\t * 为所供切面类型创建 {@link MetadataAwareAspectInstanceFactory}。\n\t * 若切面类型无 per 子句，返回 {@link SingletonMetadataAwareAspectInstanceFactory}，\n\t * 否则返回 {@link PrototypeAspectInstanceFactory}。\n\t */",
        ),
        (
            "\t\t\t// Create a shared aspect instance.",
            "\t\t\t// 创建共享切面实例。",
        ),
        (
            "\t\t\t// Create a factory for independent aspect instances.",
            "\t\t\t// 创建独立切面实例的工厂。",
        ),
        (
            "\t/**\n\t * Get the singleton aspect instance for the supplied aspect type.\n\t * An instance is created if one cannot be found in the instance cache.\n\t */",
            "\t/**\n\t * 获取所供切面类型的单例切面实例。\n\t * 若实例缓存中找不到则创建新实例。\n\t */",
        ),
        (
            "\t/**\n\t * Create a new proxy according to the settings in this factory.\n\t * <p>Can be called repeatedly. Effect will vary if we've added\n\t * or removed interfaces. Can add and remove interceptors.\n\t * <p>Uses a default class loader: Usually, the thread context class loader\n\t * (if necessary for proxy creation).\n\t * @return the new proxy\n\t */",
            "\t/**\n\t * 根据本工厂中的设置创建新代理。\n\t * <p>可重复调用。增删接口时效果会变化。可增删拦截器。\n\t * <p>使用默认类加载器：通常为线程上下文类加载器\n\t * （代理创建需要时）。\n\t * @return 新代理\n\t */",
        ),
        (
            "\t/**\n\t * Create a new proxy according to the settings in this factory.\n\t * <p>Can be called repeatedly. Effect will vary if we've added\n\t * or removed interfaces. Can add and remove interceptors.\n\t * <p>Uses the given class loader (if necessary for proxy creation).\n\t * @param classLoader the class loader to create the proxy with\n\t * @return the new proxy\n\t */",
            "\t/**\n\t * 根据本工厂中的设置创建新代理。\n\t * <p>可重复调用。增删接口时效果会变化。可增删拦截器。\n\t * <p>使用给定类加载器（代理创建需要时）。\n\t * @param classLoader 用于创建代理的类加载器\n\t * @return 新代理\n\t */",
        ),
    ],
    "AspectMetadata.java": [
        (
            "/**\n * Metadata for an AspectJ aspect class, with an additional Spring AOP pointcut\n * for the per clause.\n *\n * <p>Uses AspectJ 5 AJType reflection API, enabling us to work with different\n * AspectJ instantiation models such as \"singleton\", \"pertarget\" and \"perthis\".\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @since 2.0\n * @see org.springframework.aop.aspectj.AspectJExpressionPointcut\n */",
            "/**\n * AspectJ 切面类的元数据，另含 per 子句对应的 Spring AOP 切点。\n *\n * <p>使用 AspectJ 5 AJType 反射 API，\n * 支持 singleton、pertarget、perthis 等不同 AspectJ 实例化模型。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @since 2.0\n * @see org.springframework.aop.aspectj.AspectJExpressionPointcut\n */",
        ),
        (
            "\t/**\n\t * The name of this aspect as defined to Spring (the bean name) -\n\t * allows us to determine if two pieces of advice come from the\n\t * same aspect and hence their relative precedence.\n\t */",
            "\t/**\n\t * 本切面在 Spring 中的名称（Bean 名称）——\n\t * 用于判断两条通知是否来自同一切面，从而确定相对优先级。\n\t */",
        ),
        (
            "\t/**\n\t * The aspect class, stored separately for re-resolution of the\n\t * corresponding AjType on deserialization.\n\t */",
            "\t/**\n\t * 切面类，单独存储以便反序列化时重新解析对应 AjType。\n\t */",
        ),
        (
            "\t/**\n\t * AspectJ reflection information.\n\t * <p>Re-resolved on deserialization since it isn't serializable itself.\n\t */",
            "\t/**\n\t * AspectJ 反射信息。\n\t * <p>反序列化时重新解析，因其本身不可序列化。\n\t */",
        ),
        (
            "\t/**\n\t * Spring AOP pointcut corresponding to the per clause of the\n\t * aspect. Will be the {@code Pointcut.TRUE} canonical instance in the\n\t * case of a singleton, otherwise an AspectJExpressionPointcut.\n\t */",
            "\t/**\n\t * 切面对应 per 子句的 Spring AOP 切点。\n\t * 单例时为 {@code Pointcut.TRUE} 规范实例，\n\t * 否则为 AspectJExpressionPointcut。\n\t */",
        ),
        (
            "\t/**\n\t * Create a new AspectMetadata instance for the given aspect class.\n\t * @param aspectClass the aspect class\n\t * @param aspectName the name of the aspect\n\t */",
            "\t/**\n\t * 为给定切面类创建新的 AspectMetadata 实例。\n\t * @param aspectClass 切面类\n\t * @param aspectName 切面名称\n\t */",
        ),
        (
            "\t\t\t\t// Works with a type pattern",
            "\t\t\t\t// 使用类型模式",
        ),
        (
            "\t/**\n\t * Extract contents from String of form {@code pertarget(contents)}.\n\t */",
            "\t/**\n\t * 从 {@code pertarget(contents)} 形式的字符串中提取内容。\n\t */",
        ),
        (
            "\t/**\n\t * Return AspectJ reflection information.\n\t */",
            "\t/**\n\t * 返回 AspectJ 反射信息。\n\t */",
        ),
        (
            "\t/**\n\t * Return the aspect class.\n\t */",
            "\t/**\n\t * 返回切面类。\n\t */",
        ),
        (
            "\t/**\n\t * Return the aspect name.\n\t */",
            "\t/**\n\t * 返回切面名称。\n\t */",
        ),
        (
            "\t/**\n\t * Return a Spring pointcut expression for a singleton aspect.\n\t * (for example, {@code Pointcut.TRUE} if it's a singleton).\n\t */",
            "\t/**\n\t * 返回单例切面的 Spring 切点表达式\n\t * （例如单例时为 {@code Pointcut.TRUE}）。\n\t */",
        ),
        (
            "\t/**\n\t * Return whether the aspect is defined as \"perthis\" or \"pertarget\".\n\t */",
            "\t/**\n\t * 返回切面是否定义为 \"perthis\" 或 \"pertarget\"。\n\t */",
        ),
        (
            "\t/**\n\t * Return whether the aspect is defined as \"pertypewithin\".\n\t */",
            "\t/**\n\t * 返回切面是否定义为 \"pertypewithin\"。\n\t */",
        ),
        (
            "\t/**\n\t * Return whether the aspect needs to be lazily instantiated.\n\t */",
            "\t/**\n\t * 返回切面是否需要延迟实例化。\n\t */",
        ),
    ],
    "BeanFactoryAspectInstanceFactory.java": [
        (
            "/**\n * {@link org.springframework.aop.aspectj.AspectInstanceFactory} implementation\n * backed by a Spring {@link org.springframework.beans.factory.BeanFactory}.\n *\n * <p>Note that this may instantiate multiple times if using a prototype,\n * which probably won't give the semantics you expect.\n * Use a {@link LazySingletonAspectInstanceFactoryDecorator}\n * to wrap this to ensure only one new aspect comes back.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @since 2.0\n * @see org.springframework.beans.factory.BeanFactory\n * @see LazySingletonAspectInstanceFactoryDecorator\n */",
            "/**\n * 由 Spring {@link org.springframework.beans.factory.BeanFactory} 支持的\n * {@link org.springframework.aop.aspectj.AspectInstanceFactory} 实现。\n *\n * <p>若使用 prototype 作用域，可能多次实例化，\n * 语义可能不符合预期。\n * 请用 {@link LazySingletonAspectInstanceFactoryDecorator} 包装，\n * 确保只返回一个新切面。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @since 2.0\n * @see org.springframework.beans.factory.BeanFactory\n * @see LazySingletonAspectInstanceFactoryDecorator\n */",
        ),
        (
            "\t/**\n\t * Create a BeanFactoryAspectInstanceFactory. AspectJ will be called to\n\t * introspect to create AJType metadata using the type returned for the\n\t * given bean name from the BeanFactory.\n\t * @param beanFactory the BeanFactory to obtain instance(s) from\n\t * @param name the name of the bean\n\t */",
            "\t/**\n\t * 创建 BeanFactoryAspectInstanceFactory。\n\t * AspectJ 将内省 BeanFactory 为给定 Bean 名称返回的类型以创建 AJType 元数据。\n\t * @param beanFactory 获取实例的 BeanFactory\n\t * @param name Bean 名称\n\t */",
        ),
        (
            "\t/**\n\t * Create a BeanFactoryAspectInstanceFactory, providing a type that AspectJ should\n\t * introspect to create AJType metadata. Use if the BeanFactory may consider the type\n\t * to be a subclass (as when using CGLIB), and the information should relate to a superclass.\n\t * @param beanFactory the BeanFactory to obtain instance(s) from\n\t * @param name the name of the bean\n\t * @param type the type that should be introspected by AspectJ\n\t * ({@code null} indicates resolution through {@link BeanFactory#getType} via the bean name)\n\t */",
            "\t/**\n\t * 创建 BeanFactoryAspectInstanceFactory，提供 AspectJ 应内省以创建 AJType 元数据的类型。\n\t * 当 BeanFactory 可能将类型视为子类（如使用 CGLIB）且信息应关联超类时使用。\n\t * @param beanFactory 获取实例的 BeanFactory\n\t * @param name Bean 名称\n\t * @param type AspectJ 应内省的类型\n\t * （{@code null} 表示通过 Bean 名称经 {@link BeanFactory#getType} 解析）\n\t */",
        ),
        (
            "\t\t\t// Rely on singleton semantics provided by the factory -> no local lock.",
            "\t\t\t// 依赖工厂提供的单例语义 -> 无需本地锁。",
        ),
        (
            "\t\t\t// No singleton guarantees from the factory -> let's lock locally.",
            "\t\t\t// 工厂无单例保证 -> 本地加锁。",
        ),
        (
            "\t/**\n\t * Determine the order for this factory's target aspect, either\n\t * an instance-specific order expressed through implementing the\n\t * {@link org.springframework.core.Ordered} interface (only\n\t * checked for singleton beans), or an order expressed through the\n\t * {@link org.springframework.core.annotation.Order} annotation\n\t * at the class level.\n\t * @see org.springframework.core.Ordered\n\t * @see org.springframework.core.annotation.Order\n\t */",
            "\t/**\n\t * 确定本工厂目标切面的顺序：\n\t * 要么通过实现 {@link org.springframework.core.Ordered} 接口表达的实例级顺序\n\t * （仅检查单例 Bean），\n\t * 要么通过类级 {@link org.springframework.core.annotation.Order} 注解表达。\n\t * @see org.springframework.core.Ordered\n\t * @see org.springframework.core.annotation.Order\n\t */",
        ),
        (
            "\t\t\t\t\t// Not actually implementing Ordered -> possibly a NullBean.",
            "\t\t\t\t\t// 实际未实现 Ordered -> 可能是 NullBean。",
        ),
    ],
    "BeanFactoryAspectJAdvisorsBuilder.java": [
        (
            "/**\n * Helper for retrieving @AspectJ beans from a BeanFactory and building\n * Spring Advisors based on them, for use with auto-proxying.\n *\n * @author Juergen Hoeller\n * @since 2.0.2\n * @see AnnotationAwareAspectJAutoProxyCreator\n */",
            "/**\n * 从 BeanFactory 检索 @AspectJ Bean 并据此构建 Spring 通知器的辅助类，\n * 供自动代理使用。\n *\n * @author Juergen Hoeller\n * @since 2.0.2\n * @see AnnotationAwareAspectJAutoProxyCreator\n */",
        ),
        (
            "\t/**\n\t * Create a new BeanFactoryAspectJAdvisorsBuilder for the given BeanFactory.\n\t * @param beanFactory the ListableBeanFactory to scan\n\t */",
            "\t/**\n\t * 为给定 BeanFactory 创建新的 BeanFactoryAspectJAdvisorsBuilder。\n\t * @param beanFactory 待扫描的 ListableBeanFactory\n\t */",
        ),
        (
            "\t/**\n\t * Create a new BeanFactoryAspectJAdvisorsBuilder for the given BeanFactory.\n\t * @param beanFactory the ListableBeanFactory to scan\n\t * @param advisorFactory the AspectJAdvisorFactory to build each Advisor with\n\t */",
            "\t/**\n\t * 为给定 BeanFactory 创建新的 BeanFactoryAspectJAdvisorsBuilder。\n\t * @param beanFactory 待扫描的 ListableBeanFactory\n\t * @param advisorFactory 用于构建各通知器的 AspectJAdvisorFactory\n\t */",
        ),
        (
            "\t/**\n\t * Look for AspectJ-annotated aspect beans in the current bean factory,\n\t * and return to a list of Spring AOP Advisors representing them.\n\t * <p>Creates a Spring Advisor for each AspectJ advice method.\n\t * @return the list of {@link org.springframework.aop.Advisor} beans\n\t * @see #isEligibleBean\n\t */",
            "\t/**\n\t * 在当前 Bean 工厂中查找带 AspectJ 注解的切面 Bean，\n\t * 返回表示它们的 Spring AOP 通知器列表。\n\t * <p>为每个 AspectJ 通知方法创建 Spring 通知器。\n\t * @return {@link org.springframework.aop.Advisor} Bean 列表\n\t * @see #isEligibleBean\n\t */",
        ),
        (
            "\t\t\t\t\t\t// We must be careful not to instantiate beans eagerly as in this case they\n\t\t\t\t\t\t// would be cached by the Spring container but would not have been weaved.",
            "\t\t\t\t\t\t// 须谨慎避免过早实例化 Bean，否则会被 Spring 容器缓存但未织入。",
        ),
        (
            "\t\t\t\t\t\t\t\t\t// Per target or per this.",
            "\t\t\t\t\t\t\t\t\t// per target 或 per this。",
        ),
        (
            "\t/**\n\t * Return whether the aspect bean with the given name is eligible.\n\t * @param beanName the name of the aspect bean\n\t * @return whether the bean is eligible\n\t */",
            "\t/**\n\t * 返回给定名称的切面 Bean 是否符合条件。\n\t * @param beanName 切面 Bean 名称\n\t * @return Bean 是否符合条件\n\t */",
        ),
    ],
    "InstantiationModelAwarePointcutAdvisorImpl.java": [
        (
            "/**\n * Internal implementation of AspectJPointcutAdvisor.\n *\n * <p>Note that there will be one instance of this advisor for each target method.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @author Sam Brannen\n * @since 2.0\n */",
            "/**\n * AspectJPointcutAdvisor 的内部实现。\n *\n * <p>注意：每个目标方法对应本通知器的一个实例。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @author Sam Brannen\n * @since 2.0\n */",
        ),
        (
            "\t\t\t// Static part of the pointcut is a lazy type.",
            "\t\t\t// 切点的静态部分为延迟类型。",
        ),
        (
            "\t\t\t// Make it dynamic: must mutate from pre-instantiation to post-instantiation state.",
            "\t\t\t// 使其动态化：须从实例化前状态变为实例化后状态。",
        ),
        (
            "\t\t\t// If it's not a dynamic pointcut, it may be optimized out",
            "\t\t\t// 若非动态切点，可能被优化掉",
        ),
        (
            "\t\t\t// by the Spring AOP infrastructure after the first evaluation.",
            "\t\t\t// 在首次求值后由 Spring AOP 基础设施优化。",
        ),
        (
            "\t\t\t// A singleton aspect.",
            "\t\t\t// 单例切面。",
        ),
        (
            "\t/**\n\t * The pointcut for Spring AOP to use.\n\t * Actual behavior of the pointcut will change depending on the state of the advice.\n\t */",
            "\t/**\n\t * Spring AOP 使用的切点。\n\t * 切点实际行为随通知状态而变化。\n\t */",
        ),
        (
            "\t/**\n\t * Lazily instantiate advice if necessary.\n\t */",
            "\t/**\n\t * 必要时延迟实例化通知。\n\t */",
        ),
        (
            "\t/**\n\t * This is only of interest for Spring AOP: AspectJ instantiation semantics\n\t * are much richer. In AspectJ terminology, all a return of {@code true}\n\t * means here is that the aspect is not a SINGLETON.\n\t */",
            "\t/**\n\t * 仅对 Spring AOP 有意义：AspectJ 实例化语义丰富得多。\n\t * 在 AspectJ 术语中，此处返回 {@code true} 仅表示切面不是 SINGLETON。\n\t */",
        ),
        (
            "\t/**\n\t * Return the AspectJ AspectMetadata for this advisor.\n\t */",
            "\t/**\n\t * 返回本通知器的 AspectJ AspectMetadata。\n\t */",
        ),
        (
            "\t/**\n\t * Duplicates some logic from getAdvice, but importantly does not force\n\t * creation of the advice.\n\t */",
            "\t/**\n\t * 复制 getAdvice 的部分逻辑，但关键是不强制创建通知。\n\t */",
        ),
        (
            "\t/**\n\t * Pointcut implementation that changes its behavior when the advice is instantiated.\n\t * Note that this is a <i>dynamic</i> pointcut; otherwise it might be optimized out\n\t * if it does not at first match statically.\n\t */",
            "\t/**\n\t * 在通知实例化时改变行为的切点实现。\n\t * 注意：这是<i>动态</i>切点；否则若首次未静态匹配可能被优化掉。\n\t */",
        ),
        (
            "\t\t\t// We're either instantiated and matching on declared pointcut,\n\t\t\t// or uninstantiated matching on either pointcut...",
            "\t\t\t// 已实例化时在声明切点上匹配，\n\t\t\t// 未实例化时在任一切点上匹配...",
        ),
        (
            "\t\t\t// This can match only on declared pointcut.",
            "\t\t\t// 仅能在声明切点上匹配。",
        ),
        (
            "\t\t\t// For equivalence, we only need to compare the preInstantiationPointcut fields since\n\t\t\t// they include the declaredPointcut fields. In addition, we should not compare the\n\t\t\t// aspectInstanceFactory fields since LazySingletonAspectInstanceFactoryDecorator does\n\t\t\t// not implement equals().",
            "\t\t\t// 等价性比较只需 preInstantiationPointcut 字段，\n\t\t\t// 因其已包含 declaredPointcut 字段。\n\t\t\t// 此外不应比较 aspectInstanceFactory 字段，\n\t\t\t// 因 LazySingletonAspectInstanceFactoryDecorator 未实现 equals()。",
        ),
    ],
}
