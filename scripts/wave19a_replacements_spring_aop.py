"""Chinese JavaDoc replacements for springframework wave19a Spring AOP core [7:20]."""

SPRING_AOP_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "Advisor.java": [
        (
            "/**\n * Base interface holding AOP <b>advice</b> (action to take at a joinpoint)\n * and a filter determining the applicability of the advice (such as\n * a pointcut). <i>This interface is not for use by Spring users, but to\n * allow for commonality in support for different types of advice.</i>\n *\n * <p>Spring AOP is based around <b>around advice</b> delivered via method\n * <b>interception</b>, compliant with the AOP Alliance interception API.\n * The Advisor interface allows support for different types of advice,\n * such as <b>before</b> and <b>after</b> advice, which need not be\n * implemented using interception.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n */",
            "/**\n * 持有 AOP <b>advice</b>（在连接点处执行的操作）以及判定 advice 适用性的过滤器\n * （例如切入点）的基础接口。<i>本接口不供 Spring 用户直接使用，\n * 而是为了在不同类型的 advice 支持之间提供共性。</i>\n *\n * <p>Spring AOP 基于通过方法 <b>interception</b> 传递的 <b>around advice</b>，\n * 符合 AOP Alliance 拦截 API。Advisor 接口也支持 before、after 等\n * 无需通过 interception 实现的 advice 类型。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n */",
        ),
        (
            "\t/**\n\t * Common placeholder for an empty {@code Advice} to be returned from\n\t * {@link #getAdvice()} if no proper advice has been configured (yet).\n\t * @since 5.0\n\t */",
            "\t/**\n\t * 当尚未配置有效 advice 时，{@link #getAdvice()} 返回的空 {@code Advice} 占位符。\n\t * @since 5.0\n\t */",
        ),
        (
            "\t/**\n\t * Return the advice part of this aspect. An advice may be an\n\t * interceptor, a before advice, a throws advice, etc.\n\t * @return the advice that should apply if the pointcut matches\n\t * @see org.aopalliance.intercept.MethodInterceptor\n\t * @see BeforeAdvice\n\t * @see ThrowsAdvice\n\t * @see AfterReturningAdvice\n\t */",
            "\t/**\n\t * 返回该切面的 advice 部分。advice 可以是拦截器、前置 advice、抛出 advice 等。\n\t * @return 若切入点匹配则应生效的 advice\n\t * @see org.aopalliance.intercept.MethodInterceptor\n\t * @see BeforeAdvice\n\t * @see ThrowsAdvice\n\t * @see AfterReturningAdvice\n\t */",
        ),
        (
            "\t/**\n\t * Return whether this advice is associated with a particular instance\n\t * (for example, creating a mixin) or shared with all instances of\n\t * the advised class obtained from the same Spring bean factory.\n\t * <p><b>Note that this method is not currently used by the framework.</b>\n\t * Typical Advisor implementations always return {@code true}.\n\t * Use singleton/prototype bean definitions or appropriate programmatic\n\t * proxy creation to ensure that Advisors have the correct lifecycle model.\n\t * <p>As of 6.0.10, the default implementation returns {@code true}.\n\t * @return whether this advice is associated with a particular target instance\n\t */",
            "\t/**\n\t * 返回该 advice 是否与特定实例关联（例如创建 mixin），\n\t * 还是与同一 Spring Bean 工厂产出的被通知类的所有实例共享。\n\t * <p><b>注意：框架当前未使用本方法。</b>\n\t * 典型 Advisor 实现始终返回 {@code true}。\n\t * 请通过 singleton/prototype Bean 定义或合适的编程式代理创建，\n\t * 确保 Advisor 具有正确的生命周期模型。\n\t * <p>自 6.0.10 起，默认实现返回 {@code true}。\n\t * @return 该 advice 是否与特定目标实例关联\n\t */",
        ),
    ],
    "AfterAdvice.java": [
        (
            "/**\n * Common marker interface for after advice,\n * such as {@link AfterReturningAdvice} and {@link ThrowsAdvice}.\n *\n * @author Juergen Hoeller\n * @since 2.0.3\n * @see BeforeAdvice\n */",
            "/**\n * 后置 advice 的通用标记接口，\n * 例如 {@link AfterReturningAdvice} 与 {@link ThrowsAdvice}。\n *\n * @author Juergen Hoeller\n * @since 2.0.3\n * @see BeforeAdvice\n */",
        ),
    ],
    "AfterReturningAdvice.java": [
        (
            "/**\n * After returning advice is invoked only on normal method return, not if an\n * exception is thrown. Such advice can see the return value, but cannot change it.\n *\n * @author Rod Johnson\n * @see MethodBeforeAdvice\n * @see ThrowsAdvice\n */",
            "/**\n * 方法正常返回时触发的后置 advice，抛出异常时不会调用。\n * 此类 advice 可读取返回值，但无法修改。\n *\n * @author Rod Johnson\n * @see MethodBeforeAdvice\n * @see ThrowsAdvice\n */",
        ),
        (
            "\t/**\n\t * Callback after a given method successfully returned.\n\t * @param returnValue the value returned by the method, if any\n\t * @param method the method being invoked\n\t * @param args the arguments to the method\n\t * @param target the target of the method invocation. May be {@code null}.\n\t * @throws Throwable if this object wishes to abort the call.\n\t * Any exception thrown will be returned to the caller if it's\n\t * allowed by the method signature. Otherwise the exception\n\t * will be wrapped as a runtime exception.\n\t */",
            "\t/**\n\t * 给定方法成功返回后的回调。\n\t * @param returnValue 方法返回值（若有）\n\t * @param method 被调用的方法\n\t * @param args 方法参数\n\t * @param target 方法调用的目标对象，可为 {@code null}\n\t * @throws Throwable 若本对象希望中止调用。\n\t * 若方法签名允许，抛出的异常会返回给调用方；否则会被包装为运行时异常。\n\t */",
        ),
    ],
    "AopInvocationException.java": [
        (
            "/**\n * Exception that gets thrown when an AOP invocation failed\n * because of misconfiguration or unexpected runtime issues.\n *\n * @author Juergen Hoeller\n * @since 2.0\n */",
            "/**\n * 因配置错误或意外运行时问题导致 AOP 调用失败时抛出的异常。\n *\n * @author Juergen Hoeller\n * @since 2.0\n */",
        ),
        (
            "\t/**\n\t * Constructor for AopInvocationException.\n\t * @param msg the detail message\n\t */",
            "\t/**\n\t * 构造 AopInvocationException。\n\t * @param msg 详细消息\n\t */",
        ),
        (
            "\t/**\n\t * Constructor for AopInvocationException.\n\t * @param msg the detail message\n\t * @param cause the root cause\n\t */",
            "\t/**\n\t * 构造 AopInvocationException。\n\t * @param msg 详细消息\n\t * @param cause 根本原因\n\t */",
        ),
    ],
    "BeforeAdvice.java": [
        (
            "/**\n * Common marker interface for before advice, such as {@link MethodBeforeAdvice}.\n *\n * <p>Spring supports only method before advice. Although this is unlikely to change,\n * this API is designed to allow field advice in future if desired.\n *\n * @author Rod Johnson\n * @see AfterAdvice\n */",
            "/**\n * 前置 advice 的通用标记接口，例如 {@link MethodBeforeAdvice}。\n *\n * <p>Spring 目前仅支持方法前置 advice。尽管这一限制短期内不太可能改变，\n * 本 API 仍预留扩展空间，以便将来按需支持字段 advice。\n *\n * @author Rod Johnson\n * @see AfterAdvice\n */",
        ),
    ],
    "ClassFilter.java": [
        (
            "/**\n * Filter that restricts matching of a pointcut or introduction to a given set\n * of target classes.\n *\n * <p>Can be used as part of a {@link Pointcut} or for the entire targeting of\n * an {@link IntroductionAdvisor}.\n *\n * <p><strong>WARNING</strong>: Concrete implementations of this interface must\n * provide proper implementations of {@link Object#equals(Object)},\n * {@link Object#hashCode()}, and {@link Object#toString()} in order to allow the\n * filter to be used in caching scenarios &mdash; for example, in proxies generated\n * by CGLIB. As of Spring Framework 6.0.13, the {@code toString()} implementation\n * must generate a unique string representation that aligns with the logic used\n * to implement {@code equals()}. See concrete implementations of this interface\n * within the framework for examples.\n *\n * @author Rod Johnson\n * @author Sam Brannen\n * @see Pointcut\n * @see MethodMatcher\n */",
            "/**\n * 将切入点或引介的匹配范围限制在给定目标类集合上的过滤器。\n *\n * <p>可作为 {@link Pointcut} 的一部分，或用于 {@link IntroductionAdvisor} 的整体目标选择。\n *\n * <p><strong>警告</strong>：本接口的具体实现必须正确实现\n * {@link Object#equals(Object)}、{@link Object#hashCode()} 与 {@link Object#toString()}，\n * 以便在缓存场景（例如 CGLIB 生成的代理）中使用。\n * 自 Spring Framework 6.0.13 起，{@code toString()} 必须生成与 {@code equals()} 逻辑一致的唯一字符串表示。\n * 可参考框架内本接口的具体实现示例。\n *\n * @author Rod Johnson\n * @author Sam Brannen\n * @see Pointcut\n * @see MethodMatcher\n */",
        ),
        (
            "\t/**\n\t * Should the pointcut apply to the given interface or target class?\n\t * @param clazz the candidate target class\n\t * @return whether the advice should apply to the given target class\n\t */",
            "\t/**\n\t * 切入点是否应作用于给定接口或目标类？\n\t * @param clazz 候选目标类\n\t * @return advice 是否应作用于该目标类\n\t */",
        ),
        (
            "\t/**\n\t * Canonical instance of a {@code ClassFilter} that matches all classes.\n\t */",
            "\t/**\n\t * 匹配所有类的 {@code ClassFilter} 规范实例。\n\t */",
        ),
    ],
    "DynamicIntroductionAdvice.java": [
        (
            "/**\n * Subinterface of AOP Alliance Advice that allows additional interfaces\n * to be implemented by an Advice, and available via a proxy using that\n * interceptor. This is a fundamental AOP concept called <b>introduction</b>.\n *\n * <p>Introductions are often <b>mixins</b>, enabling the building of composite\n * objects that can achieve many of the goals of multiple inheritance in Java.\n *\n * <p>Compared to {@link IntroductionInfo}, this interface allows an advice to\n * implement a range of interfaces that is not necessarily known in advance.\n * Thus an {@link IntroductionAdvisor} can be used to specify which interfaces\n * will be exposed in an advised object.\n *\n * @author Rod Johnson\n * @since 1.1.1\n * @see IntroductionInfo\n * @see IntroductionAdvisor\n */",
            "/**\n * AOP Alliance Advice 的子接口，允许 Advice 实现额外接口，\n * 并通过使用该拦截器的代理对外提供。这是 AOP 的基本概念 <b>introduction</b>（引介）。\n *\n * <p>引介通常是 <b>mixins</b>，用于构建复合对象，\n * 在 Java 中实现多重继承的许多目标。\n *\n * <p>与 {@link IntroductionInfo} 相比，本接口允许 advice 实现\n * 事先未必确定的一组接口。因此可用 {@link IntroductionAdvisor}\n * 指定被通知对象将暴露哪些接口。\n *\n * @author Rod Johnson\n * @since 1.1.1\n * @see IntroductionInfo\n * @see IntroductionAdvisor\n */",
        ),
        (
            "\t/**\n\t * Does this introduction advice implement the given interface?\n\t * @param intf the interface to check\n\t * @return whether the advice implements the specified interface\n\t */",
            "\t/**\n\t * 该引介 advice 是否实现了给定接口？\n\t * @param intf 待检查的接口\n\t * @return advice 是否实现指定接口\n\t */",
        ),
    ],
    "IntroductionAdvisor.java": [
        (
            "/**\n * Superinterface for advisors that perform one or more AOP <b>introductions</b>.\n *\n * <p>This interface cannot be implemented directly; subinterfaces must\n * provide the advice type implementing the introduction.\n *\n * <p>Introduction is the implementation of additional interfaces\n * (not implemented by a target) via AOP advice.\n *\n * @author Rod Johnson\n * @since 04.04.2003\n * @see IntroductionInterceptor\n */",
            "/**\n * 执行一个或多个 AOP <b>introduction</b>（引介）的 Advisor 超接口。\n *\n * <p>本接口不能直接实现；子接口必须提供实现引介的 advice 类型。\n *\n * <p>引介是通过 AOP advice 为目标实现其原本不具备的额外接口。\n *\n * @author Rod Johnson\n * @since 04.04.2003\n * @see IntroductionInterceptor\n */",
        ),
        (
            "\t/**\n\t * Return the filter determining which target classes this introduction\n\t * should apply to.\n\t * <p>This represents the class part of a pointcut. Note that method\n\t * matching doesn't make sense to introductions.\n\t * @return the class filter\n\t */",
            "\t/**\n\t * 返回决定该引介应作用于哪些目标类的过滤器。\n\t * <p>这对应切入点的类匹配部分。注意引介不涉及方法匹配。\n\t * @return 类过滤器\n\t */",
        ),
        (
            "\t/**\n\t * Can the advised interfaces be implemented by the introduction advice?\n\t * Invoked before adding an IntroductionAdvisor.\n\t * @throws IllegalArgumentException if the advised interfaces can't be\n\t * implemented by the introduction advice\n\t */",
            "\t/**\n\t * 被通知接口能否由引介 advice 实现？\n\t * 在添加 IntroductionAdvisor 之前调用。\n\t * @throws IllegalArgumentException 若被通知接口无法由引介 advice 实现\n\t */",
        ),
    ],
    "IntroductionAwareMethodMatcher.java": [
        (
            "/**\n * A specialized type of {@link MethodMatcher} that takes into account introductions\n * when matching methods. If there are no introductions on the target class,\n * a method matcher may be able to optimize matching more effectively for example.\n *\n * @author Adrian Colyer\n * @since 2.0\n */",
            "/**\n * 在匹配方法时考虑引介的 {@link MethodMatcher} 特化类型。\n * 例如，若目标类上没有引介，方法匹配器可能更高效地优化匹配。\n *\n * @author Adrian Colyer\n * @since 2.0\n */",
        ),
        (
            "\t/**\n\t * Perform static checking whether the given method matches. This may be invoked\n\t * instead of the 2-arg {@link #matches(java.lang.reflect.Method, Class)} method\n\t * if the caller supports the extended IntroductionAwareMethodMatcher interface.\n\t * @param method the candidate method\n\t * @param targetClass the target class\n\t * @param hasIntroductions {@code true} if the object on whose behalf we are\n\t * asking is the subject on one or more introductions; {@code false} otherwise\n\t * @return whether this method matches statically\n\t */",
            "\t/**\n\t * 静态检查给定方法是否匹配。若调用方支持扩展的 IntroductionAwareMethodMatcher 接口，\n\t * 可调用本方法替代两参数 {@link #matches(java.lang.reflect.Method, Class)}。\n\t * @param method 候选方法\n\t * @param targetClass 目标类\n\t * @param hasIntroductions 若被询问对象是一个或多个引介的主体则为 {@code true}，否则为 {@code false}\n\t * @return 该方法是否静态匹配\n\t */",
        ),
    ],
    "IntroductionInfo.java": [
        (
            "/**\n * Interface supplying the information necessary to describe an introduction.\n *\n * <p>{@link IntroductionAdvisor IntroductionAdvisors} must implement this\n * interface. If an {@link org.aopalliance.aop.Advice} implements this,\n * it may be used as an introduction without an {@link IntroductionAdvisor}.\n * In this case, the advice is self-describing, providing not only the\n * necessary behavior, but describing the interfaces it introduces.\n *\n * @author Rod Johnson\n * @since 1.1.1\n */",
            "/**\n * 提供描述引介所需信息的接口。\n *\n * <p>{@link IntroductionAdvisor IntroductionAdvisors} 必须实现本接口。\n * 若 {@link org.aopalliance.aop.Advice} 实现了本接口，\n * 则可在没有 {@link IntroductionAdvisor} 的情况下作为引介使用。\n * 此时 advice 自描述：既提供必要行为，也声明其引介的接口。\n *\n * @author Rod Johnson\n * @since 1.1.1\n */",
        ),
        (
            "\t/**\n\t * Return the additional interfaces introduced by this Advisor or Advice.\n\t * @return the introduced interfaces\n\t */",
            "\t/**\n\t * 返回本 Advisor 或 Advice 引介的额外接口。\n\t * @return 被引介的接口\n\t */",
        ),
    ],
    "IntroductionInterceptor.java": [
        (
            "/**\n * Subinterface of AOP Alliance MethodInterceptor that allows additional interfaces\n * to be implemented by the interceptor, and available via a proxy using that\n * interceptor. This is a fundamental AOP concept called <b>introduction</b>.\n *\n * <p>Introductions are often <b>mixins</b>, enabling the building of composite\n * objects that can achieve many of the goals of multiple inheritance in Java.\n *\n * @author Rod Johnson\n * @see DynamicIntroductionAdvice\n */",
            "/**\n * AOP Alliance MethodInterceptor 的子接口，允许拦截器实现额外接口，\n * 并通过使用该拦截器的代理对外提供。这是 AOP 的基本概念 <b>introduction</b>（引介）。\n *\n * <p>引介通常是 <b>mixins</b>，用于构建复合对象，\n * 在 Java 中实现多重继承的许多目标。\n *\n * @author Rod Johnson\n * @see DynamicIntroductionAdvice\n */",
        ),
    ],
    "MethodBeforeAdvice.java": [
        (
            "/**\n * Advice invoked before a method is invoked. Such advices cannot\n * prevent the method call proceeding, unless they throw a Throwable.\n *\n * @author Rod Johnson\n * @see AfterReturningAdvice\n * @see ThrowsAdvice\n */",
            "/**\n * 在方法调用前触发的 advice。除非抛出 Throwable，\n * 否则无法阻止方法继续执行。\n *\n * @author Rod Johnson\n * @see AfterReturningAdvice\n * @see ThrowsAdvice\n */",
        ),
        (
            "\t/**\n\t * Callback before a given method is invoked.\n\t * @param method the method being invoked\n\t * @param args the arguments to the method\n\t * @param target the target of the method invocation. May be {@code null}.\n\t * @throws Throwable if this object wishes to abort the call.\n\t * Any exception thrown will be returned to the caller if it's\n\t * allowed by the method signature. Otherwise the exception\n\t * will be wrapped as a runtime exception.\n\t */",
            "\t/**\n\t * 给定方法被调用前的回调。\n\t * @param method 被调用的方法\n\t * @param args 方法参数\n\t * @param target 方法调用的目标对象，可为 {@code null}\n\t * @throws Throwable 若本对象希望中止调用。\n\t * 若方法签名允许，抛出的异常会返回给调用方；否则会被包装为运行时异常。\n\t */",
        ),
    ],
    "MethodMatcher.java": [
        (
            "/**\n * Part of a {@link Pointcut}: Checks whether the target method is eligible for advice.\n *\n * <p>A {@code MethodMatcher} may be evaluated <b>statically</b> or at <b>runtime</b>\n * (dynamically). Static matching involves a method and (possibly) method attributes.\n * Dynamic matching also makes arguments for a particular call available, and any\n * effects of running previous advice applying to the joinpoint.\n *\n * <p>If an implementation returns {@code false} from its {@link #isRuntime()}\n * method, evaluation can be performed statically, and the result will be the same\n * for all invocations of this method, whatever their arguments. This means that\n * if the {@link #isRuntime()} method returns {@code false}, the 3-arg\n * {@link #matches(Method, Class, Object[])} method will never be invoked.\n *\n * <p>If an implementation returns {@code true} from its 2-arg\n * {@link #matches(Method, Class)} method and its {@link #isRuntime()} method\n * returns {@code true}, the 3-arg {@link #matches(Method, Class, Object[])}\n * method will be invoked <i>immediately before each potential execution of the\n * related advice</i> to decide whether the advice should run. All previous advice,\n * such as earlier interceptors in an interceptor chain, will have run, so any\n * state changes they have produced in parameters or {@code ThreadLocal} state will\n * be available at the time of evaluation.\n *\n * <p><strong>WARNING</strong>: Concrete implementations of this interface must\n * provide proper implementations of {@link Object#equals(Object)},\n * {@link Object#hashCode()}, and {@link Object#toString()} in order to allow the\n * matcher to be used in caching scenarios &mdash; for example, in proxies generated\n * by CGLIB. As of Spring Framework 6.0.13, the {@code toString()} implementation\n * must generate a unique string representation that aligns with the logic used\n * to implement {@code equals()}. See concrete implementations of this interface\n * within the framework for examples.\n *\n * @author Rod Johnson\n * @author Sam Brannen\n * @since 11.11.2003\n * @see Pointcut\n * @see ClassFilter\n */",
            "/**\n * {@link Pointcut} 的组成部分：检查目标方法是否符合 advice 条件。\n *\n * <p>{@code MethodMatcher} 可在<b>静态</b>或<b>运行时</b>（动态）评估。\n * 静态匹配涉及方法及（可能的）方法属性；动态匹配还会提供具体调用的参数，\n * 以及先前作用于该连接点的 advice 所产生的影响。\n *\n * <p>若实现的 {@link #isRuntime()} 返回 {@code false}，\n * 则可静态评估，且无论参数如何，对该方法的所有调用结果相同。\n * 这意味着 {@link #isRuntime()} 为 {@code false} 时，\n * 三参数 {@link #matches(Method, Class, Object[])} 永远不会被调用。\n *\n * <p>若两参数 {@link #matches(Method, Class)} 返回 {@code true}\n * 且 {@link #isRuntime()} 返回 {@code true}，则三参数\n * {@link #matches(Method, Class, Object[])} 会在<i>每次可能执行相关 advice 之前</i>被调用，\n * 以决定是否运行 advice。此前所有 advice（例如拦截器链中较早的拦截器）均已执行，\n * 因此它们在参数或 {@code ThreadLocal} 状态中产生的变更在评估时可用。\n *\n * <p><strong>警告</strong>：本接口的具体实现必须正确实现\n * {@link Object#equals(Object)}、{@link Object#hashCode()} 与 {@link Object#toString()}，\n * 以便在缓存场景（例如 CGLIB 生成的代理）中使用。\n * 自 Spring Framework 6.0.13 起，{@code toString()} 必须生成与 {@code equals()} 逻辑一致的唯一字符串表示。\n * 可参考框架内本接口的具体实现示例。\n *\n * @author Rod Johnson\n * @author Sam Brannen\n * @since 11.11.2003\n * @see Pointcut\n * @see ClassFilter\n */",
        ),
        (
            "\t/**\n\t * Perform static checking to determine whether the given method matches.\n\t * <p>If this method returns {@code false} or if {@link #isRuntime()}\n\t * returns {@code false}, no runtime check (i.e. no\n\t * {@link #matches(Method, Class, Object[])} call) will be made.\n\t * @param method the candidate method\n\t * @param targetClass the target class\n\t * @return whether this method matches statically\n\t */",
            "\t/**\n\t * 静态检查给定方法是否匹配。\n\t * <p>若本方法返回 {@code false}，或 {@link #isRuntime()} 返回 {@code false}，\n\t * 则不会进行运行时检查（即不会调用 {@link #matches(Method, Class, Object[])}）。\n\t * @param method 候选方法\n\t * @param targetClass 目标类\n\t * @return 该方法是否静态匹配\n\t */",
        ),
        (
            "\t/**\n\t * Is this {@code MethodMatcher} dynamic, that is, must a final check be made\n\t * via the {@link #matches(Method, Class, Object[])} method at runtime even\n\t * if {@link #matches(Method, Class)} returns {@code true}?\n\t * <p>Can be invoked when an AOP proxy is created, and need not be invoked\n\t * again before each method invocation.\n\t * @return whether a runtime match via {@link #matches(Method, Class, Object[])}\n\t * is required if static matching passed\n\t */",
            "\t/**\n\t * 本 {@code MethodMatcher} 是否为动态匹配，即即使 {@link #matches(Method, Class)}\n\t * 返回 {@code true}，是否仍须在运行时通过 {@link #matches(Method, Class, Object[])} 做最终检查？\n\t * <p>可在创建 AOP 代理时调用，无需在每次方法调用前再次调用。\n\t * @return 若静态匹配通过，是否仍需运行时匹配\n\t */",
        ),
        (
            "\t/**\n\t * Check whether there is a runtime (dynamic) match for this method, which\n\t * must have matched statically.\n\t * <p>This method is invoked only if {@link #matches(Method, Class)} returns\n\t * {@code true} for the given method and target class, and if\n\t * {@link #isRuntime()} returns {@code true}.\n\t * <p>Invoked immediately before potential running of the advice, after any\n\t * advice earlier in the advice chain has run.\n\t * @param method the candidate method\n\t * @param targetClass the target class\n\t * @param args arguments to the method\n\t * @return whether there's a runtime match\n\t * @see #matches(Method, Class)\n\t */",
            "\t/**\n\t * 检查该方法是否存在运行时（动态）匹配；该方法必须已通过静态匹配。\n\t * <p>仅当 {@link #matches(Method, Class)} 对给定方法与目标类返回 {@code true}\n\t * 且 {@link #isRuntime()} 返回 {@code true} 时调用。\n\t * <p>在 advice 链中较早的 advice 执行完毕后、可能运行 advice 之前立即调用。\n\t * @param method 候选方法\n\t * @param targetClass 目标类\n\t * @param args 方法参数\n\t * @return 是否存在运行时匹配\n\t * @see #matches(Method, Class)\n\t */",
        ),
        (
            "\t/**\n\t * Canonical instance of a {@code MethodMatcher} that matches all methods.\n\t */",
            "\t/**\n\t * 匹配所有方法的 {@code MethodMatcher} 规范实例。\n\t */",
        ),
    ],
    "Pointcut.java": [
        (
            "/**\n * Core Spring pointcut abstraction.\n *\n * <p>A pointcut is composed of a {@link ClassFilter} and a {@link MethodMatcher}.\n * Both these basic terms and a Pointcut itself can be combined to build up combinations\n * (for example, through {@link org.springframework.aop.support.ComposablePointcut}).\n *\n * @author Rod Johnson\n * @see ClassFilter\n * @see MethodMatcher\n * @see org.springframework.aop.support.Pointcuts\n * @see org.springframework.aop.support.ClassFilters\n * @see org.springframework.aop.support.MethodMatchers\n */",
            "/**\n * Spring 切入点核心抽象。\n *\n * <p>切入点由 {@link ClassFilter} 与 {@link MethodMatcher} 组成。\n * 这两个基本组件以及 Pointcut 本身均可组合构建更复杂的切入点\n * （例如通过 {@link org.springframework.aop.support.ComposablePointcut}）。\n *\n * @author Rod Johnson\n * @see ClassFilter\n * @see MethodMatcher\n * @see org.springframework.aop.support.Pointcuts\n * @see org.springframework.aop.support.ClassFilters\n * @see org.springframework.aop.support.MethodMatchers\n */",
        ),
        (
            "\t/**\n\t * Return the ClassFilter for this pointcut.\n\t * @return the ClassFilter (never {@code null})\n\t */",
            "\t/**\n\t * 返回本切入点的 ClassFilter。\n\t * @return ClassFilter（永不为 {@code null}）\n\t */",
        ),
        (
            "\t/**\n\t * Return the MethodMatcher for this pointcut.\n\t * @return the MethodMatcher (never {@code null})\n\t */",
            "\t/**\n\t * 返回本切入点的 MethodMatcher。\n\t * @return MethodMatcher（永不为 {@code null}）\n\t */",
        ),
        (
            "\t/**\n\t * Canonical Pointcut instance that always matches.\n\t */",
            "\t/**\n\t * 始终匹配的 Pointcut 规范实例。\n\t */",
        ),
    ],
}
