"""Chinese JavaDoc replacements for springframework wave20a aspectj core [1:10]."""

ASPECTJ_CORE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "AspectJProxyUtils.java": [
        (
            "/**\n * Utility methods for working with AspectJ proxies.\n *\n * @author Rod Johnson\n * @author Ramnivas Laddad\n * @author Juergen Hoeller\n * @since 2.0\n */",
            "/**\n * 操作 AspectJ 代理的工具方法。\n *\n * @author Rod Johnson\n * @author Ramnivas Laddad\n * @author Juergen Hoeller\n * @since 2.0\n */",
        ),
        (
            "\t/**\n\t * Add special advisors if necessary to work with a proxy chain that contains AspectJ advisors:\n\t * concretely, {@link ExposeInvocationInterceptor} at the beginning of the list.\n\t * <p>This will expose the current Spring AOP invocation (necessary for some AspectJ pointcut\n\t * matching) and make available the current AspectJ JoinPoint. The call will have no effect\n\t * if there are no AspectJ advisors in the advisor chain.\n\t * @param advisors the advisors available\n\t * @return {@code true} if an {@link ExposeInvocationInterceptor} was added to the list,\n\t * otherwise {@code false}\n\t */",
            "\t/**\n\t * 若代理链包含 AspectJ 通知器，则在必要时添加特殊通知器：\n\t * 具体而言，在列表开头加入 {@link ExposeInvocationInterceptor}。\n\t * <p>这将暴露当前 Spring AOP 调用（部分 AspectJ 切点匹配所需），\n\t * 并使当前 AspectJ JoinPoint 可用。若通知器链中无 AspectJ 通知器，\n\t * 本调用无效果。\n\t * @param advisors 可用的通知器列表\n\t * @return 若向列表添加了 {@link ExposeInvocationInterceptor} 则为 {@code true}，\n\t * 否则为 {@code false}\n\t */",
        ),
        (
            "\t\t// Don't add advisors to an empty list; may indicate that proxying is just not required",
            "\t\t// 勿向空列表添加通知器；可能表示根本不需要代理",
        ),
        (
            "\t\t\t\t// Be careful not to get the Advice without a guard, as this might eagerly\n\t\t\t\t// instantiate a non-singleton AspectJ aspect...",
            "\t\t\t\t// 获取 Advice 前须加防护，否则可能过早实例化非单例 AspectJ 切面...",
        ),
        (
            "\t/**\n\t * Determine whether the given Advisor contains an AspectJ advice.\n\t * @param advisor the Advisor to check\n\t */",
            "\t/**\n\t * 判断给定通知器是否包含 AspectJ 通知。\n\t * @param advisor 待检查的通知器\n\t */",
        ),
    ],
    "AspectJWeaverMessageHandler.java": [
        (
            "/**\n * Implementation of AspectJ's {@link IMessageHandler} interface that\n * routes AspectJ weaving messages through the same logging system as the\n * regular Spring messages.\n *\n * <p>Pass the option...\n *\n * <p><code class=\"code\">-XmessageHandlerClass:org.springframework.aop.aspectj.AspectJWeaverMessageHandler</code>\n *\n * <p>to the weaver; for example, specifying the following in a\n * \"{@code META-INF/aop.xml} file:\n *\n * <p><code class=\"code\">&lt;weaver options=\"...\"/&gt;</code>\n *\n * @author Adrian Colyer\n * @author Juergen Hoeller\n * @since 2.0\n */",
            "/**\n * AspectJ {@link IMessageHandler} 接口的实现，\n * 将 AspectJ 织入消息路由至与常规 Spring 消息相同的日志系统。\n *\n * <p>向织入器传递选项...\n *\n * <p><code class=\"code\">-XmessageHandlerClass:org.springframework.aop.aspectj.AspectJWeaverMessageHandler</code>\n *\n * <p>例如在 \"{@code META-INF/aop.xml} 文件中指定：\n *\n * <p><code class=\"code\">&lt;weaver options=\"...\"/&gt;</code>\n *\n * @author Adrian Colyer\n * @author Juergen Hoeller\n * @since 2.0\n */",
        ),
        (
            "\t\t// We want to see everything, and allow configuration of log levels dynamically.",
            "\t\t// 希望看到所有消息，并允许动态配置日志级别。",
        ),
        (
            "\t@Override\n\tpublic void dontIgnore(Kind messageKind) {\n\t\t// We weren't ignoring anything anyway...\n\t}",
            "\t@Override\n\tpublic void dontIgnore(Kind messageKind) {\n\t\t// 本来就没有忽略任何消息...\n\t}",
        ),
        (
            "\t@Override\n\tpublic void ignore(Kind kind) {\n\t\t// We weren't ignoring anything anyway...\n\t}",
            "\t@Override\n\tpublic void ignore(Kind kind) {\n\t\t// 本来就没有忽略任何消息...\n\t}",
        ),
    ],
    "DeclareParentsAdvisor.java": [
        (
            "/**\n * Introduction advisor delegating to the given object.\n * Implements AspectJ annotation-style behavior for the DeclareParents annotation.\n *\n * @author Rod Johnson\n * @author Ramnivas Laddad\n * @since 2.0\n */",
            "/**\n * 委托给给定对象的引介通知器。\n * 为 DeclareParents 注解实现 AspectJ 注解风格行为。\n *\n * @author Rod Johnson\n * @author Ramnivas Laddad\n * @since 2.0\n */",
        ),
        (
            "\t/**\n\t * Create a new advisor for this DeclareParents field.\n\t * @param interfaceType static field defining the introduction\n\t * @param typePattern type pattern the introduction is restricted to\n\t * @param defaultImpl the default implementation class\n\t */",
            "\t/**\n\t * 为本 DeclareParents 字段创建新通知器。\n\t * @param interfaceType 定义引介的静态字段\n\t * @param typePattern 引介所限制的类型模式\n\t * @param defaultImpl 默认实现类\n\t */",
        ),
        (
            "\t/**\n\t * Create a new advisor for this DeclareParents field.\n\t * @param interfaceType static field defining the introduction\n\t * @param typePattern type pattern the introduction is restricted to\n\t * @param delegateRef the delegate implementation object\n\t */",
            "\t/**\n\t * 为本 DeclareParents 字段创建新通知器。\n\t * @param interfaceType 定义引介的静态字段\n\t * @param typePattern 引介所限制的类型模式\n\t * @param delegateRef 委托实现对象\n\t */",
        ),
        (
            "\t/**\n\t * Private constructor to share common code between impl-based delegate and reference-based delegate\n\t * (cannot use method such as init() to share common code, due the use of final fields).\n\t * @param interfaceType static field defining the introduction\n\t * @param typePattern type pattern the introduction is restricted to\n\t * @param interceptor the delegation advice as {@link IntroductionInterceptor}\n\t */",
            "\t/**\n\t * 在基于实现类委托与基于引用委托之间共享公共代码的私有构造器\n\t * （因使用 final 字段，无法通过 init() 等方法共享公共代码）。\n\t * @param interfaceType 定义引介的静态字段\n\t * @param typePattern 引介所限制的类型模式\n\t * @param interceptor 作为 {@link IntroductionInterceptor} 的委托通知\n\t */",
        ),
        (
            "\t\t// Excludes methods implemented.",
            "\t\t// 排除已实现该接口的类。",
        ),
    ],
    "InstantiationModelAwarePointcutAdvisor.java": [
        (
            "/**\n * Interface to be implemented by Spring AOP Advisors wrapping AspectJ\n * aspects that may have a lazy initialization strategy. For example,\n * a perThis instantiation model would mean lazy initialization of the advice.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @since 2.0\n */",
            "/**\n * 由封装 AspectJ 切面的 Spring AOP 通知器实现的接口，\n * 这些切面可能采用延迟初始化策略。例如 perThis 实例化模型\n * 意味着通知的延迟初始化。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @since 2.0\n */",
        ),
        (
            "\t/**\n\t * Return whether this advisor is lazily initializing its underlying advice.\n\t */",
            "\t/**\n\t * 返回本通知器是否延迟初始化其底层通知。\n\t */",
        ),
        (
            "\t/**\n\t * Return whether this advisor has already instantiated its advice.\n\t */",
            "\t/**\n\t * 返回本通知器是否已实例化其通知。\n\t */",
        ),
    ],
    "ShadowMatchUtils.java": [
        (
            "/**\n * Internal {@link ShadowMatch} utilities.\n *\n * @author Stephane Nicoll\n * @author Juergen Hoeller\n * @since 6.2\n */",
            "/**\n * 内部 {@link ShadowMatch} 工具类。\n *\n * @author Stephane Nicoll\n * @author Juergen Hoeller\n * @since 6.2\n */",
        ),
        (
            "\t/**\n\t * Find a {@link ShadowMatch} for the specified key.\n\t * @param key the key to use\n\t * @return the {@code ShadowMatch} to use for the specified key,\n\t * or {@code null} if none found\n\t */",
            "\t/**\n\t * 查找指定键对应的 {@link ShadowMatch}。\n\t * @param key 使用的键\n\t * @return 指定键对应的 {@code ShadowMatch}，\n\t * 未找到时返回 {@code null}\n\t */",
        ),
        (
            "\t/**\n\t * Associate the {@link ShadowMatch} with the specified key.\n\t * If an entry already exists, the given {@code shadowMatch} is ignored.\n\t * @param key the key to use\n\t * @param shadowMatch the shadow match to use for this key\n\t * if none already exists\n\t * @return the shadow match to use for the specified key\n\t */",
            "\t/**\n\t * 将 {@link ShadowMatch} 与指定键关联。\n\t * 若条目已存在，则忽略给定的 {@code shadowMatch}。\n\t * @param key 使用的键\n\t * @param shadowMatch 该键尚无条目时使用的 shadow match\n\t * @return 指定键应使用的 shadow match\n\t */",
        ),
        (
            "\t/**\n\t * Clear the cache of computed {@link ShadowMatch} instances.\n\t */",
            "\t/**\n\t * 清空已计算 {@link ShadowMatch} 实例的缓存。\n\t */",
        ),
    ],
    "SimpleAspectInstanceFactory.java": [
        (
            "/**\n * Implementation of {@link AspectInstanceFactory} that creates a new instance\n * of the specified aspect class for every {@link #getAspectInstance()} call.\n *\n * @author Juergen Hoeller\n * @since 2.0.4\n */",
            "/**\n * {@link AspectInstanceFactory} 的实现，\n * 每次调用 {@link #getAspectInstance()} 时创建指定切面类的新实例。\n *\n * @author Juergen Hoeller\n * @since 2.0.4\n */",
        ),
        (
            "\t/**\n\t * Create a new SimpleAspectInstanceFactory for the given aspect class.\n\t * @param aspectClass the aspect class\n\t */",
            "\t/**\n\t * 为给定切面类创建新的 SimpleAspectInstanceFactory。\n\t * @param aspectClass 切面类\n\t */",
        ),
        (
            "\t/**\n\t * Return the specified aspect class (never {@code null}).\n\t */",
            "\t/**\n\t * 返回指定的切面类（永不为 {@code null}）。\n\t */",
        ),
        (
            "\t/**\n\t * Determine the order for this factory's aspect instance,\n\t * either an instance-specific order expressed through implementing\n\t * the {@link org.springframework.core.Ordered} interface,\n\t * or a fallback order.\n\t * @see org.springframework.core.Ordered\n\t * @see #getOrderForAspectClass\n\t */",
            "\t/**\n\t * 确定本工厂切面实例的顺序：\n\t * 要么通过实现 {@link org.springframework.core.Ordered} 接口表达的实例级顺序，\n\t * 要么为回退顺序。\n\t * @see org.springframework.core.Ordered\n\t * @see #getOrderForAspectClass\n\t */",
        ),
        (
            "\t/**\n\t * Determine a fallback order for the case that the aspect instance\n\t * does not express an instance-specific order through implementing\n\t * the {@link org.springframework.core.Ordered} interface.\n\t * <p>The default implementation simply returns {@code Ordered.LOWEST_PRECEDENCE}.\n\t * @param aspectClass the aspect class\n\t */",
            "\t/**\n\t * 在切面实例未通过实现 {@link org.springframework.core.Ordered} 接口\n\t * 表达实例级顺序时，确定回退顺序。\n\t * <p>默认实现直接返回 {@code Ordered.LOWEST_PRECEDENCE}。\n\t * @param aspectClass 切面类\n\t */",
        ),
    ],
    "SingletonAspectInstanceFactory.java": [
        (
            "/**\n * Implementation of {@link AspectInstanceFactory} that is backed by a\n * specified singleton object, returning the same instance for every\n * {@link #getAspectInstance()} call.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @since 2.0\n * @see SimpleAspectInstanceFactory\n */",
            "/**\n * {@link AspectInstanceFactory} 的实现，\n * 由指定单例对象支持，每次 {@link #getAspectInstance()} 调用返回同一实例。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @since 2.0\n * @see SimpleAspectInstanceFactory\n */",
        ),
        (
            "\t/**\n\t * Create a new SingletonAspectInstanceFactory for the given aspect instance.\n\t * @param aspectInstance the singleton aspect instance\n\t */",
            "\t/**\n\t * 为给定切面实例创建新的 SingletonAspectInstanceFactory。\n\t * @param aspectInstance 单例切面实例\n\t */",
        ),
        (
            "\t/**\n\t * Determine the order for this factory's aspect instance,\n\t * either an instance-specific order expressed through implementing\n\t * the {@link org.springframework.core.Ordered} interface,\n\t * or a fallback order.\n\t * @see org.springframework.core.Ordered\n\t * @see #getOrderForAspectClass\n\t */",
            "\t/**\n\t * 确定本工厂切面实例的顺序：\n\t * 要么通过实现 {@link org.springframework.core.Ordered} 接口表达的实例级顺序，\n\t * 要么为回退顺序。\n\t * @see org.springframework.core.Ordered\n\t * @see #getOrderForAspectClass\n\t */",
        ),
        (
            "\t/**\n\t * Determine a fallback order for the case that the aspect instance\n\t * does not express an instance-specific order through implementing\n\t * the {@link org.springframework.core.Ordered} interface.\n\t * <p>The default implementation simply returns {@code Ordered.LOWEST_PRECEDENCE}.\n\t * @param aspectClass the aspect class\n\t */",
            "\t/**\n\t * 在切面实例未通过实现 {@link org.springframework.core.Ordered} 接口\n\t * 表达实例级顺序时，确定回退顺序。\n\t * <p>默认实现直接返回 {@code Ordered.LOWEST_PRECEDENCE}。\n\t * @param aspectClass 切面类\n\t */",
        ),
    ],
    "TypePatternClassFilter.java": [
        (
            "/**\n * Spring AOP {@link ClassFilter} implementation using AspectJ type matching.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @author Sam Brannen\n * @since 2.0\n */",
            "/**\n * 使用 AspectJ 类型匹配的 Spring AOP {@link ClassFilter} 实现。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @author Sam Brannen\n * @since 2.0\n */",
        ),
        (
            "\t/**\n\t * Creates a new instance of the {@link TypePatternClassFilter} class.\n\t * <p>This is the JavaBean constructor; be sure to set the\n\t * {@link #setTypePattern(String) typePattern} property, else a\n\t * no doubt fatal {@link IllegalStateException} will be thrown\n\t * when the {@link #matches(Class)} method is first invoked.\n\t */",
            "\t/**\n\t * 创建 {@link TypePatternClassFilter} 的新实例。\n\t * <p>这是 JavaBean 构造器；务必设置\n\t * {@link #setTypePattern(String) typePattern} 属性，\n\t * 否则首次调用 {@link #matches(Class)} 时将抛出\n\t * 几乎必然的 {@link IllegalStateException}。\n\t */",
        ),
        (
            "\t/**\n\t * Create a fully configured {@link TypePatternClassFilter} using the\n\t * given type pattern.\n\t * @param typePattern the type pattern that AspectJ weaver should parse\n\t */",
            "\t/**\n\t * 使用给定类型模式创建完全配置的 {@link TypePatternClassFilter}。\n\t * @param typePattern AspectJ 织入器应解析的类型模式\n\t */",
        ),
        (
            "\t/**\n\t * Set the AspectJ type pattern to match.\n\t * <p>Examples include:\n\t * <code class=\"code\">\n\t * org.springframework.beans.*\n\t * </code>\n\t * This will match any class or interface in the given package.\n\t * <code class=\"code\">\n\t * org.springframework.beans.ITestBean+\n\t * </code>\n\t * This will match the {@code ITestBean} interface and any class\n\t * that implements it.\n\t * <p>These conventions are established by AspectJ, not Spring AOP.\n\t * @param typePattern the type pattern that AspectJ weaver should parse\n\t */",
            "\t/**\n\t * 设置要匹配的 AspectJ 类型模式。\n\t * <p>示例包括：\n\t * <code class=\"code\">\n\t * org.springframework.beans.*\n\t * </code>\n\t * 将匹配给定包中的任意类或接口。\n\t * <code class=\"code\">\n\t * org.springframework.beans.ITestBean+\n\t * </code>\n\t * 将匹配 {@code ITestBean} 接口及其实现类。\n\t * <p>这些约定由 AspectJ 而非 Spring AOP 定义。\n\t * @param typePattern AspectJ 织入器应解析的类型模式\n\t */",
        ),
        (
            "\t/**\n\t * Return the AspectJ type pattern to match.\n\t */",
            "\t/**\n\t * 返回要匹配的 AspectJ 类型模式。\n\t */",
        ),
        (
            "\t/**\n\t * Should the pointcut apply to the given interface or target class?\n\t * @param clazz candidate target class\n\t * @return whether the advice should apply to this candidate target class\n\t * @throws IllegalStateException if no {@link #setTypePattern(String)} has been set\n\t */",
            "\t/**\n\t * 切点是否应作用于给定接口或目标类？\n\t * @param clazz 候选目标类\n\t * @return 通知是否应作用于该候选目标类\n\t * @throws IllegalStateException 若未调用 {@link #setTypePattern(String)}\n\t */",
        ),
        (
            "\t/**\n\t * If a type pattern has been specified in XML, the user cannot\n\t * write {@code and} as \"&&\" (though &amp;&amp; will work).\n\t * We also allow {@code and} between two sub-expressions.\n\t * <p>This method converts back to {@code &&} for the AspectJ pointcut parser.\n\t */",
            "\t/**\n\t * 若在 XML 中指定类型模式，用户不能将 {@code and} 写为 \"&&\"（\n\t * 但 &amp;&amp; 可用）。也允许在两个子表达式之间使用 {@code and}。\n\t * <p>本方法将其转回 {@code &&} 供 AspectJ 切点解析器使用。\n\t */",
        ),
    ],
    "MethodInvocationProceedingJoinPoint.java": [
        (
            "/**\n * An implementation of the AspectJ {@link ProceedingJoinPoint} interface\n * wrapping an AOP Alliance {@link org.aopalliance.intercept.MethodInvocation}.\n *\n * <p><b>Note</b>: The {@code getThis()} method returns the current Spring AOP proxy.\n * The {@code getTarget()} method returns the current Spring AOP target (which may be\n * {@code null} if there is no target instance) as a plain POJO without any advice.\n * <b>If you want to call the object and have the advice take effect, use {@code getThis()}.</b>\n * A common example is casting the object to an introduced interface in the implementation of\n * an introduction. There is no such distinction between target and proxy in AspectJ itself.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @author Adrian Colyer\n * @author Ramnivas Laddad\n * @since 2.0\n */",
            "/**\n * AspectJ {@link ProceedingJoinPoint} 接口的实现，\n * 封装 AOP Alliance {@link org.aopalliance.intercept.MethodInvocation}。\n *\n * <p><b>注意</b>：{@code getThis()} 返回当前 Spring AOP 代理。\n * {@code getTarget()} 返回当前 Spring AOP 目标（无目标实例时可为 {@code null}），\n * 为不带任何通知的普通 POJO。\n * <b>若需调用对象并使通知生效，请使用 {@code getThis()}。</b>\n * 常见示例是在引介实现中将对象转型为引介接口。\n * AspectJ 本身并无 target 与 proxy 的此类区分。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @author Adrian Colyer\n * @author Ramnivas Laddad\n * @since 2.0\n */",
        ),
        (
            "\t/** Lazily initialized signature object. */",
            "\t/** 延迟初始化的签名对象。 */",
        ),
        (
            "\t/** Lazily initialized source location object. */",
            "\t/** 延迟初始化的源码位置对象。 */",
        ),
        (
            "\t/**\n\t * Create a new MethodInvocationProceedingJoinPoint, wrapping the given\n\t * Spring ProxyMethodInvocation object.\n\t * @param methodInvocation the Spring ProxyMethodInvocation object\n\t */",
            "\t/**\n\t * 创建新的 MethodInvocationProceedingJoinPoint，\n\t * 封装给定 Spring ProxyMethodInvocation 对象。\n\t * @param methodInvocation Spring ProxyMethodInvocation 对象\n\t */",
        ),
        (
            "\t/**\n\t * Returns the Spring AOP proxy. Cannot be {@code null}.\n\t */",
            "\t/**\n\t * 返回 Spring AOP 代理。不可为 {@code null}。\n\t */",
        ),
        (
            "\t/**\n\t * Returns the Spring AOP target. May be {@code null} if there is no target.\n\t */",
            "\t/**\n\t * 返回 Spring AOP 目标。无目标时可为 {@code null}。\n\t */",
        ),
        (
            "\t\t// TODO: It's just an adapter but returning 0 might still have side effects...",
            "\t\t// TODO：仅为适配器，但返回 0 仍可能有副作用...",
        ),
        (
            "\t/**\n\t * Lazily initialized MethodSignature.\n\t */",
            "\t/**\n\t * 延迟初始化的 MethodSignature。\n\t */",
        ),
        (
            "\t/**\n\t * Lazily initialized SourceLocation.\n\t */",
            "\t/**\n\t * 延迟初始化的 SourceLocation。\n\t */",
        ),
    ],
    "RuntimeTestWalker.java": [
        (
            "/**\n * This class encapsulates some AspectJ internal knowledge that should be\n * pushed back into the AspectJ project in a future release.\n *\n * <p>It relies on implementation specific knowledge in AspectJ to break\n * encapsulation and do something AspectJ was not designed to do: query\n * the types of runtime tests that will be performed. The code here should\n * migrate to {@code ShadowMatch.getVariablesInvolvedInRuntimeTest()}\n * or some similar operation.\n *\n * <p>See <a href=\"https://bugs.eclipse.org/bugs/show_bug.cgi?id=151593\">Bug 151593</a>\n *\n * @author Adrian Colyer\n * @author Ramnivas Laddad\n * @since 2.0\n */",
            "/**\n * 本类封装一些 AspectJ 内部知识，\n * 未来应回馈至 AspectJ 项目。\n *\n * <p>它依赖 AspectJ 实现细节打破封装，\n * 执行 AspectJ 未设计的功能：查询将执行的运行时测试类型。\n * 此处代码应迁移至 {@code ShadowMatch.getVariablesInvolvedInRuntimeTest()}\n * 或类似操作。\n *\n * <p>参见 <a href=\"https://bugs.eclipse.org/bugs/show_bug.cgi?id=151593\">Bug 151593</a>\n *\n * @author Adrian Colyer\n * @author Ramnivas Laddad\n * @since 2.0\n */",
        ),
        (
            "\t/**\n\t * If the test uses any of the this, target, at_this, at_target, and at_annotation vars,\n\t * then it tests subtype sensitive vars.\n\t */",
            "\t/**\n\t * 若测试使用 this、target、at_this、at_target 或 at_annotation 变量，\n\t * 则测试子类型敏感变量。\n\t */",
        ),
        (
            "\t\t\t\t// Don't use ResolvedType.isAssignableFrom() as it won't be aware of (Spring) mixins",
            "\t\t\t\t// 勿用 ResolvedType.isAssignableFrom()，它无法感知（Spring）混入",
        ),
        (
            "\t/**\n\t * Check if residue of target(TYPE) kind. See SPR-3783 for more details.\n\t */",
            "\t/**\n\t * 检查是否为 target(TYPE) 类型的 residue。详见 SPR-3783。\n\t */",
        ),
        (
            "\t/**\n\t * Check if residue of this(TYPE) kind. See SPR-2979 for more details.\n\t */",
            "\t/**\n\t * 检查是否为 this(TYPE) 类型的 residue。详见 SPR-2979。\n\t */",
        ),
        (
            "\t\t// TODO: Optimization: Process only if this() specifies a type and not an identifier.",
            "\t\t// TODO：优化：仅当 this() 指定类型而非标识符时处理。",
        ),
        (
            "\t\t\t// If you thought things were bad before, now we sink to new levels of horror...",
            "\t\t\t// 若以为之前已够糟，现在则坠入更深的深渊...",
        ),
    ],
}
