"""Chinese JavaDoc replacements for springframework wave22b advisor support [9:13,18:20]."""

ADVISOR_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "AbstractBeanFactoryPointcutAdvisor.java": [
        (
            "/**\n * Abstract BeanFactory-based PointcutAdvisor that allows for any Advice\n * to be configured as reference to an Advice bean in a BeanFactory.\n *\n * <p>Specifying the name of an advice bean instead of the advice object itself\n * (if running within a BeanFactory) increases loose coupling at initialization time,\n * in order to not initialize the advice object until the pointcut actually matches.\n *\n * @author Juergen Hoeller\n * @since 2.0.2\n * @see #setAdviceBeanName\n * @see DefaultBeanFactoryPointcutAdvisor\n */",
            "/**\n * 基于 BeanFactory 的抽象 PointcutAdvisor，\n * 允许将任意 Advice 配置为 BeanFactory 中 Advice Bean 的引用。\n *\n * <p>在 BeanFactory 环境中指定 advice Bean 名称而非 advice 对象本身，\n * 可在初始化时提高松耦合，直到切入点实际匹配时才初始化 advice 对象。\n *\n * @author Juergen Hoeller\n * @since 2.0.2\n * @see #setAdviceBeanName\n * @see DefaultBeanFactoryPointcutAdvisor\n */",
        ),
        (
            "\t/**\n\t * Specify the name of the advice bean that this advisor should refer to.\n\t * <p>An instance of the specified bean will be obtained on first access\n\t * of this advisor's advice. This advisor will only ever obtain at most one\n\t * single instance of the advice bean, caching the instance for the lifetime\n\t * of the advisor.\n\t * @see #getAdvice()\n\t */",
            "\t/**\n\t * 指定本 advisor 应引用的 advice Bean 名称。\n\t * <p>首次访问本 advisor 的 advice 时将获取指定 Bean 的实例。\n\t * 本 advisor 最多只获取一个 advice Bean 实例，\n\t * 并在 advisor 生命周期内缓存该实例。\n\t * @see #getAdvice()\n\t */",
        ),
        (
            "\t/**\n\t * Return the name of the advice bean that this advisor refers to, if any.\n\t */",
            "\t/**\n\t * 返回本 advisor 引用的 advice Bean 名称（若有）。\n\t */",
        ),
        (
            "\t/**\n\t * Specify a particular instance of the target advice directly,\n\t * avoiding lazy resolution in {@link #getAdvice()}.\n\t * @since 3.1\n\t */",
            "\t/**\n\t * 直接指定目标 advice 的特定实例，\n\t * 避免在 {@link #getAdvice()} 中进行懒解析。\n\t * @since 3.1\n\t */",
        ),
        (
            "\t\t\t// Rely on singleton semantics provided by the factory.",
            "\t\t\t// 依赖工厂提供的单例语义。",
        ),
        (
            "\t\t\t// No singleton guarantees from the factory -> let's lock locally.",
            "\t\t\t// 工厂无单例保证 -> 在本地加锁。",
        ),
        (
            "\t//---------------------------------------------------------------------\n\t// Serialization support\n\t//---------------------------------------------------------------------",
            "\t//---------------------------------------------------------------------\n\t// 序列化支持\n\t//---------------------------------------------------------------------",
        ),
        (
            "\t\t// Rely on default serialization, just initialize state after deserialization.",
            "\t\t// 依赖默认序列化，反序列化后仅初始化状态。",
        ),
        (
            "\t\t// Initialize transient fields.",
            "\t\t// 初始化 transient 字段。",
        ),
    ],
    "AbstractExpressionPointcut.java": [
        (
            "/**\n * Abstract superclass for expression pointcuts,\n * offering location and expression properties.\n *\n * @author Rod Johnson\n * @author Rob Harrop\n * @since 2.0\n * @see #setLocation\n * @see #setExpression\n */",
            "/**\n * 表达式切入点的抽象超类，\n * 提供 location 与 expression 属性。\n *\n * @author Rod Johnson\n * @author Rob Harrop\n * @since 2.0\n * @see #setLocation\n * @see #setExpression\n */",
        ),
        (
            "\t/**\n\t * Set the location for debugging.\n\t */",
            "\t/**\n\t * 设置用于调试的 location。\n\t */",
        ),
        (
            "\t/**\n\t * Return location information about the pointcut expression\n\t * if available. This is useful in debugging.\n\t * @return location information as a human-readable String,\n\t * or {@code null} if none is available\n\t */",
            "\t/**\n\t * 返回切入点表达式的 location 信息（若有）。\n\t * 便于调试。\n\t * @return 可读的 location 字符串，\n\t * 若无则返回 {@code null}\n\t */",
        ),
        (
            "\t\t\t// Fill in location information if possible.",
            "\t\t\t// 尽可能补充 location 信息。",
        ),
        (
            "\t/**\n\t * Called when a new pointcut expression is set.\n\t * The expression should be parsed at this point if possible.\n\t * <p>This implementation is empty.\n\t * @param expression the expression to set\n\t * @throws IllegalArgumentException if the expression is invalid\n\t * @see #setExpression\n\t */",
            "\t/**\n\t * 设置新切入点表达式时调用。\n\t * 若可能，应在此解析表达式。\n\t * <p>本实现为空。\n\t * @param expression 要设置的表达式\n\t * @throws IllegalArgumentException 若表达式无效\n\t * @see #setExpression\n\t */",
        ),
        (
            "\t/**\n\t * Return this pointcut's expression.\n\t */",
            "\t/**\n\t * 返回本切入点的表达式。\n\t */",
        ),
    ],
    "AbstractGenericPointcutAdvisor.java": [
        (
            "/**\n * Abstract generic {@link org.springframework.aop.PointcutAdvisor}\n * that allows for any {@link Advice} to be configured.\n *\n * @author Juergen Hoeller\n * @since 2.0\n * @see #setAdvice\n * @see DefaultPointcutAdvisor\n */",
            "/**\n * 抽象通用 {@link org.springframework.aop.PointcutAdvisor}，\n * 允许配置任意 {@link Advice}。\n *\n * @author Juergen Hoeller\n * @since 2.0\n * @see #setAdvice\n * @see DefaultPointcutAdvisor\n */",
        ),
        (
            "\t/**\n\t * Specify the advice that this advisor should apply.\n\t */",
            "\t/**\n\t * 指定本 advisor 应应用的 advice。\n\t */",
        ),
    ],
    "AbstractPointcutAdvisor.java": [
        (
            "/**\n * Abstract base class for {@link org.springframework.aop.PointcutAdvisor}\n * implementations. Can be subclassed for returning a specific pointcut/advice\n * or a freely configurable pointcut/advice.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @since 1.1.2\n * @see AbstractGenericPointcutAdvisor\n */",
            "/**\n * {@link org.springframework.aop.PointcutAdvisor} 实现的抽象基类。\n * 可子类化以返回特定切入点/advice，或自由配置切入点/advice。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @since 1.1.2\n * @see AbstractGenericPointcutAdvisor\n */",
        ),
    ],
    "AbstractRegexpMethodPointcut.java": [
        (
            "/**\n * Abstract base regular expression pointcut bean. JavaBean properties are:\n * <ul>\n * <li>pattern: regular expression for the fully-qualified method names to match.\n * The exact regexp syntax will depend on the subclass (for example, Perl5 regular expressions)\n * <li>patterns: alternative property taking a String array of patterns.\n * The result will be the union of these patterns.\n * </ul>\n *\n * <p>Note: the regular expressions must be a match. For example,\n * {@code .*get.*} will match com.mycom.Foo.getBar().\n * {@code get.*} will not.\n *\n * <p>This base class is serializable. Subclasses should declare all fields transient;\n * the {@link #initPatternRepresentation} method will be invoked again on deserialization.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @author Rob Harrop\n * @since 1.1\n * @see JdkRegexpMethodPointcut\n */",
            "/**\n * 正则表达式切入点 Bean 的抽象基类。JavaBean 属性包括：\n * <ul>\n * <li>pattern：匹配全限定方法名的正则表达式。\n * 具体 regexp 语法取决于子类（例如 Perl5 正则）\n * <li>patterns：接受 String 数组的替代属性。\n * 结果为这些模式的并集。\n * </ul>\n *\n * <p>注意：正则表达式必须完全匹配。例如\n * {@code .*get.*} 可匹配 com.mycom.Foo.getBar()，\n * 而 {@code get.*} 不行。\n *\n * <p>本基类可序列化。子类应将所有字段声明为 transient；\n * 反序列化时会再次调用 {@link #initPatternRepresentation}。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @author Rob Harrop\n * @since 1.1\n * @see JdkRegexpMethodPointcut\n */",
        ),
        (
            "\t/**\n\t * Regular expressions to match.\n\t */",
            "\t/**\n\t * 用于匹配的正则表达式。\n\t */",
        ),
        (
            "\t/**\n\t * Regular expressions <strong>not</strong> to match.\n\t */",
            "\t/**\n\t * <strong>不</strong>匹配的正则表达式。\n\t */",
        ),
        (
            "\t/**\n\t * Convenience method when we have only a single pattern.\n\t * Use either this method or {@link #setPatterns}, not both.\n\t * @see #setPatterns\n\t */",
            "\t/**\n\t * 仅有一个模式时的便捷方法。\n\t * 使用本方法或 {@link #setPatterns} 之一，不可同时使用。\n\t * @see #setPatterns\n\t */",
        ),
        (
            "\t/**\n\t * Set the regular expressions defining methods to match.\n\t * Matching will be the union of all these; if any match, the pointcut matches.\n\t * @see #setPattern\n\t */",
            "\t/**\n\t * 设置定义待匹配方法的正则表达式。\n\t * 匹配结果为所有模式的并集；任一匹配则切入点匹配。\n\t * @see #setPattern\n\t */",
        ),
        (
            "\t/**\n\t * Return the regular expressions for method matching.\n\t */",
            "\t/**\n\t * 返回用于方法匹配的正则表达式。\n\t */",
        ),
        (
            "\t/**\n\t * Convenience method when we have only a single exclusion pattern.\n\t * Use either this method or {@link #setExcludedPatterns}, not both.\n\t * @see #setExcludedPatterns\n\t */",
            "\t/**\n\t * 仅有一个排除模式时的便捷方法。\n\t * 使用本方法或 {@link #setExcludedPatterns} 之一，不可同时使用。\n\t * @see #setExcludedPatterns\n\t */",
        ),
        (
            "\t/**\n\t * Set the regular expressions defining methods to match for exclusion.\n\t * Matching will be the union of all these; if any match, the pointcut matches.\n\t * @see #setExcludedPattern\n\t */",
            "\t/**\n\t * 设置用于排除匹配的方法正则表达式。\n\t * 匹配结果为所有模式的并集；任一匹配则切入点匹配。\n\t * @see #setExcludedPattern\n\t */",
        ),
        (
            "\t/**\n\t * Returns the regular expressions for exclusion matching.\n\t */",
            "\t/**\n\t * 返回用于排除匹配的正则表达式。\n\t */",
        ),
        (
            "\t/**\n\t * Try to match the regular expression against the fully qualified name\n\t * of the target class as well as against the method's declaring class,\n\t * plus the name of the method.\n\t */",
            "\t/**\n\t * 尝试将正则表达式与目标类的全限定名、\n\t * 方法声明类及方法名进行匹配。\n\t */",
        ),
        (
            "\t/**\n\t * Match the specified candidate against the configured patterns.\n\t * @param signatureString \"java.lang.Object.hashCode\" style signature\n\t * @return whether the candidate matches at least one of the specified patterns\n\t */",
            "\t/**\n\t * 将指定候选与已配置的模式匹配。\n\t * @param signatureString \"java.lang.Object.hashCode\" 风格的签名\n\t * @return 候选是否匹配至少一个指定模式\n\t */",
        ),
        (
            "\t/**\n\t * Subclasses must implement this to initialize regexp pointcuts.\n\t * Can be invoked multiple times.\n\t * <p>This method will be invoked from the {@link #setPatterns} method,\n\t * and also on deserialization.\n\t * @param patterns the patterns to initialize\n\t * @throws IllegalArgumentException in case of an invalid pattern\n\t */",
            "\t/**\n\t * 子类必须实现此方法以初始化 regexp 切入点。\n\t * 可被多次调用。\n\t * <p>由 {@link #setPatterns} 调用，反序列化时也会调用。\n\t * @param patterns 要初始化的模式\n\t * @throws IllegalArgumentException 若模式无效\n\t */",
        ),
        (
            "\t/**\n\t * Subclasses must implement this to initialize regexp pointcuts.\n\t * Can be invoked multiple times.\n\t * <p>This method will be invoked from the {@link #setExcludedPatterns} method,\n\t * and also on deserialization.\n\t * @param patterns the patterns to initialize\n\t * @throws IllegalArgumentException in case of an invalid pattern\n\t */",
            "\t/**\n\t * 子类必须实现此方法以初始化 regexp 切入点。\n\t * 可被多次调用。\n\t * <p>由 {@link #setExcludedPatterns} 调用，反序列化时也会调用。\n\t * @param patterns 要初始化的模式\n\t * @throws IllegalArgumentException 若模式无效\n\t */",
        ),
        (
            "\t/**\n\t * Does the pattern at the given index match the given String?\n\t * @param pattern the {@code String} pattern to match\n\t * @param patternIndex index of pattern (starting from 0)\n\t * @return {@code true} if there is a match, {@code false} otherwise\n\t */",
            "\t/**\n\t * 给定索引处的模式是否匹配给定字符串？\n\t * @param pattern 要匹配的 {@code String} 模式\n\t * @param patternIndex 模式索引（从 0 起）\n\t * @return 匹配则 {@code true}，否则 {@code false}\n\t */",
        ),
        (
            "\t/**\n\t * Does the exclusion pattern at the given index match the given String?\n\t * @param pattern the {@code String} pattern to match\n\t * @param patternIndex index of pattern (starting from 0)\n\t * @return {@code true} if there is a match, {@code false} otherwise\n\t */",
            "\t/**\n\t * 给定索引处的排除模式是否匹配给定字符串？\n\t * @param pattern 要匹配的 {@code String} 模式\n\t * @param patternIndex 模式索引（从 0 起）\n\t * @return 匹配则 {@code true}，否则 {@code false}\n\t */",
        ),
    ],
    "DefaultBeanFactoryPointcutAdvisor.java": [
        (
            "/**\n * Concrete BeanFactory-based PointcutAdvisor that allows for any Advice\n * to be configured as reference to an Advice bean in the BeanFactory,\n * as well as the Pointcut to be configured through a bean property.\n *\n * <p>Specifying the name of an advice bean instead of the advice object itself\n * (if running within a BeanFactory) increases loose coupling at initialization time,\n * in order to not initialize the advice object until the pointcut actually matches.\n *\n * @author Juergen Hoeller\n * @since 2.0.2\n * @see #setPointcut\n * @see #setAdviceBeanName\n */",
            "/**\n * 基于 BeanFactory 的具体 PointcutAdvisor，\n * 允许将任意 Advice 配置为 BeanFactory 中 Advice Bean 的引用，\n * 并通过 Bean 属性配置 Pointcut。\n *\n * <p>在 BeanFactory 环境中指定 advice Bean 名称而非 advice 对象本身，\n * 可在初始化时提高松耦合，直到切入点实际匹配时才初始化 advice 对象。\n *\n * @author Juergen Hoeller\n * @since 2.0.2\n * @see #setPointcut\n * @see #setAdviceBeanName\n */",
        ),
        (
            "\t/**\n\t * Specify the pointcut targeting the advice.\n\t * <p>Default is {@code Pointcut.TRUE}.\n\t * @see #setAdviceBeanName\n\t */",
            "\t/**\n\t * 指定针对 advice 的切入点。\n\t * <p>默认为 {@code Pointcut.TRUE}。\n\t * @see #setAdviceBeanName\n\t */",
        ),
    ],
    "DefaultIntroductionAdvisor.java": [
        (
            "/**\n * Simple {@link org.springframework.aop.IntroductionAdvisor} implementation\n * that by default applies to any class.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @since 11.11.2003\n */",
            "/**\n * 简单的 {@link org.springframework.aop.IntroductionAdvisor} 实现，\n * 默认适用于任意类。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @since 11.11.2003\n */",
        ),
        (
            "\t/**\n\t * Create a DefaultIntroductionAdvisor for the given advice.\n\t * @param advice the Advice to apply (may implement the\n\t * {@link org.springframework.aop.IntroductionInfo} interface)\n\t * @see #addInterface\n\t */",
            "\t/**\n\t * 为给定 advice 创建 DefaultIntroductionAdvisor。\n\t * @param advice 要应用的 Advice（可实现\n\t * {@link org.springframework.aop.IntroductionInfo} 接口）\n\t * @see #addInterface\n\t */",
        ),
        (
            "\t/**\n\t * Create a DefaultIntroductionAdvisor for the given advice.\n\t * @param advice the Advice to apply\n\t * @param introductionInfo the IntroductionInfo that describes\n\t * the interface to introduce (may be {@code null})\n\t */",
            "\t/**\n\t * 为给定 advice 创建 DefaultIntroductionAdvisor。\n\t * @param advice 要应用的 Advice\n\t * @param introductionInfo 描述要引入接口的 IntroductionInfo（可为 {@code null}）\n\t */",
        ),
        (
            "\t/**\n\t * Create a DefaultIntroductionAdvisor for the given advice.\n\t * @param advice the Advice to apply\n\t * @param ifc the interface to introduce\n\t */",
            "\t/**\n\t * 为给定 advice 创建 DefaultIntroductionAdvisor。\n\t * @param advice 要应用的 Advice\n\t * @param ifc 要引入的接口\n\t */",
        ),
        (
            "\t/**\n\t * Add the specified interface to the list of interfaces to introduce.\n\t * @param ifc the interface to introduce\n\t */",
            "\t/**\n\t * 将指定接口加入待引入接口列表。\n\t * @param ifc 要引入的接口\n\t */",
        ),
    ],
    "DefaultPointcutAdvisor.java": [
        (
            "/**\n * Convenient Pointcut-driven Advisor implementation.\n *\n * <p>This is the most commonly used Advisor implementation. It can be used\n * with any pointcut and advice type, except for introductions. There is\n * normally no need to subclass this class, or to implement custom Advisors.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @see #setPointcut\n * @see #setAdvice\n */",
            "/**\n * 便捷的 Pointcut 驱动 Advisor 实现。\n *\n * <p>这是最常用的 Advisor 实现。可与任意切入点和 advice 类型配合使用，\n * 引入（introduction）除外。通常无需子类化本类或实现自定义 Advisor。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @see #setPointcut\n * @see #setAdvice\n */",
        ),
        (
            "\t/**\n\t * Create an empty DefaultPointcutAdvisor.\n\t * <p>Advice must be set before using setter methods.\n\t * Pointcut will normally be set also, but defaults to {@code Pointcut.TRUE}.\n\t */",
            "\t/**\n\t * 创建空的 DefaultPointcutAdvisor。\n\t * <p>使用 setter 前须设置 Advice。\n\t * 通常也会设置 Pointcut，默认 {@code Pointcut.TRUE}。\n\t */",
        ),
        (
            "\t/**\n\t * Create a DefaultPointcutAdvisor that matches all methods.\n\t * <p>{@code Pointcut.TRUE} will be used as Pointcut.\n\t * @param advice the Advice to use\n\t */",
            "\t/**\n\t * 创建匹配所有方法的 DefaultPointcutAdvisor。\n\t * <p>使用 {@code Pointcut.TRUE} 作为 Pointcut。\n\t * @param advice 要使用的 Advice\n\t */",
        ),
        (
            "\t/**\n\t * Create a DefaultPointcutAdvisor, specifying Pointcut and Advice.\n\t * @param pointcut the Pointcut targeting the Advice\n\t * @param advice the Advice to run when Pointcut matches\n\t */",
            "\t/**\n\t * 创建 DefaultPointcutAdvisor，指定 Pointcut 与 Advice。\n\t * @param pointcut 针对 Advice 的 Pointcut\n\t * @param advice 切入点匹配时执行的 Advice\n\t */",
        ),
        (
            "\t/**\n\t * Specify the pointcut targeting the advice.\n\t * <p>Default is {@code Pointcut.TRUE}.\n\t * @see #setAdvice\n\t */",
            "\t/**\n\t * 指定针对 advice 的切入点。\n\t * <p>默认为 {@code Pointcut.TRUE}。\n\t * @see #setAdvice\n\t */",
        ),
    ],
}
