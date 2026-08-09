"""Chinese JavaDoc replacements for springframework wave23a pointcuts and advisors [7:13]."""

POINTCUT_ADVISOR_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "JdkRegexpMethodPointcut.java": [
        (
            "/**\n * Regular expression pointcut based on the {@code java.util.regex} package.\n * Supports the following JavaBean properties:\n * <ul>\n * <li>pattern: regular expression for the fully-qualified method names to match\n * <li>patterns: alternative property taking a String array of patterns. The result will\n * be the union of these patterns.\n * </ul>\n *\n * <p>Note: the regular expressions must be a match. For example,\n * {@code .*get.*} will match com.mycom.Foo.getBar().\n * {@code get.*} will not.\n *\n * @author Dmitriy Kopylenko\n * @author Rob Harrop\n * @since 1.1\n */",
            "/**\n * 基于 {@code java.util.regex} 包的正则表达式切入点。\n * 支持以下 JavaBean 属性：\n * <ul>\n * <li>pattern：匹配完全限定方法名的正则表达式\n * <li>patterns：接受 String 数组的替代属性，结果为这些模式的并集\n * </ul>\n *\n * <p>注意：正则表达式必须完全匹配。例如\n * {@code .*get.*} 可匹配 com.mycom.Foo.getBar()，\n * {@code get.*} 则不行。\n *\n * @author Dmitriy Kopylenko\n * @author Rob Harrop\n * @since 1.1\n */",
        ),
        (
            "\t/**\n\t * Compiled form of the patterns.\n\t */",
            "\t/**\n\t * 模式的编译形式。\n\t */",
        ),
        (
            "\t/**\n\t * Compiled form of the exclusion patterns.\n\t */",
            "\t/**\n\t * 排除模式的编译形式。\n\t */",
        ),
        (
            "\t/**\n\t * Initialize {@link Pattern Patterns} from the supplied {@code String[]}.\n\t */",
            "\t/**\n\t * 从提供的 {@code String[]} 初始化 {@link Pattern Patterns}。\n\t */",
        ),
        (
            "\t/**\n\t * Initialize exclusion {@link Pattern Patterns} from the supplied {@code String[]}.\n\t */",
            "\t/**\n\t * 从提供的 {@code String[]} 初始化排除 {@link Pattern Patterns}。\n\t */",
        ),
        (
            "\t/**\n\t * Returns {@code true} if the {@link Pattern} at index {@code patternIndex}\n\t * matches the supplied candidate {@code String}.\n\t */",
            "\t/**\n\t * 若索引 {@code patternIndex} 处的 {@link Pattern} 匹配\n\t * 提供的候选 {@code String}，则返回 {@code true}。\n\t */",
        ),
        (
            "\t/**\n\t * Returns {@code true} if the exclusion {@link Pattern} at index {@code patternIndex}\n\t * matches the supplied candidate {@code String}.\n\t */",
            "\t/**\n\t * 若索引 {@code patternIndex} 处的排除 {@link Pattern} 匹配\n\t * 提供的候选 {@code String}，则返回 {@code true}。\n\t */",
        ),
        (
            "\t/**\n\t * Compiles the supplied {@code String[]} into an array of\n\t * {@link Pattern} objects and returns that array.\n\t */",
            "\t/**\n\t * 将提供的 {@code String[]} 编译为 {@link Pattern} 对象数组并返回。\n\t */",
        ),
    ],
    "NameMatchMethodPointcut.java": [
        (
            "/**\n * Pointcut bean for simple method name matches, as an alternative to regular\n * expression patterns.\n *\n * <p>Each configured method name can be an exact method name or a method name\n * pattern (see {@link #isMatch(String, String)} for details on the supported\n * pattern styles).\n *\n * <p>Does not handle overloaded methods: all methods with a given name will be eligible.\n *\n * @author Juergen Hoeller\n * @author Rod Johnson\n * @author Rob Harrop\n * @author Sam Brannen\n * @since 11.02.2004\n * @see #isMatch\n * @see JdkRegexpMethodPointcut\n */",
            "/**\n * 简单方法名匹配的切入点 bean，作为正则表达式模式的替代。\n *\n * <p>每个配置的方法名可为精确方法名或方法名模式\n * （支持的模式风格见 {@link #isMatch(String, String)}）。\n *\n * <p>不处理重载方法：给定名称的所有方法均符合条件。\n *\n * @author Juergen Hoeller\n * @author Rod Johnson\n * @author Rob Harrop\n * @author Sam Brannen\n * @since 11.02.2004\n * @see #isMatch\n * @see JdkRegexpMethodPointcut\n */",
        ),
        (
            "\t/**\n\t * Convenience method for configuring a single method name pattern.\n\t * <p>Use either this method or {@link #setMappedNames(String...)}, but not both.\n\t * @see #setMappedNames\n\t */",
            "\t/**\n\t * 配置单个方法名模式的便捷方法。\n\t * <p>使用本方法或 {@link #setMappedNames(String...)} 之一，不可同时使用。\n\t * @see #setMappedNames\n\t */",
        ),
        (
            "\t/**\n\t * Set the method name patterns defining methods to match.\n\t * <p>Matching will be the union of all these; if any match, the pointcut matches.\n\t * @see #setMappedName(String)\n\t */",
            "\t/**\n\t * 设置定义要匹配方法的方法名模式。\n\t * <p>匹配为所有模式的并集；任一匹配则切入点匹配。\n\t * @see #setMappedName(String)\n\t */",
        ),
        (
            "\t/**\n\t * Add another method name pattern, in addition to those already configured.\n\t * <p>Like the \"set\" methods, this method is for use when configuring proxies,\n\t * before a proxy is used.\n\t * <p><b>NOTE:</b> This method does not work after the proxy is in use, since\n\t * advice chains will be cached.\n\t * @param mappedNamePattern the additional method name pattern\n\t * @return this pointcut to allow for method chaining\n\t * @see #setMappedNames(String...)\n\t * @see #setMappedName(String)\n\t */",
            "\t/**\n\t * 在已配置模式之外再添加一个方法名模式。\n\t * <p>与 \"set\" 方法类似，本方法用于配置代理、代理使用前。\n\t * <p><b>注意：</b>代理使用后本方法无效，因 advice 链会被缓存。\n\t * @param mappedNamePattern 额外的方法名模式\n\t * @return 本切入点，支持方法链式调用\n\t * @see #setMappedNames(String...)\n\t * @see #setMappedName(String)\n\t */",
        ),
        (
            "\t/**\n\t * Determine if the given method name matches the mapped name pattern.\n\t * <p>The default implementation checks for {@code xxx*}, {@code *xxx},\n\t * {@code *xxx*}, and {@code xxx*yyy} matches, as well as direct equality.\n\t * <p>Can be overridden in subclasses.\n\t * @param methodName the method name to check\n\t * @param mappedNamePattern the method name pattern\n\t * @return {@code true} if the method name matches the pattern\n\t * @see PatternMatchUtils#simpleMatch(String, String)\n\t */",
            "\t/**\n\t * 判断给定方法名是否匹配映射名模式。\n\t * <p>默认实现检查 {@code xxx*}、{@code *xxx}、\n\t * {@code *xxx*}、{@code xxx*yyy} 匹配及直接相等。\n\t * <p>子类可覆盖。\n\t * @param methodName 待检查的方法名\n\t * @param mappedNamePattern 方法名模式\n\t * @return 方法名是否匹配模式\n\t * @see PatternMatchUtils#simpleMatch(String, String)\n\t */",
        ),
    ],
    "NameMatchMethodPointcutAdvisor.java": [
        (
            "/**\n * Convenient class for name-match method pointcuts that hold an Advice,\n * making them an Advisor.\n *\n * @author Juergen Hoeller\n * @author Rob Harrop\n * @see NameMatchMethodPointcut\n */",
            "/**\n * 持有 Advice 的方法名匹配切入点的便捷类，\n * 使其成为 Advisor。\n *\n * @author Juergen Hoeller\n * @author Rob Harrop\n * @see NameMatchMethodPointcut\n */",
        ),
        (
            "\t/**\n\t * Set the {@link ClassFilter} to use for this pointcut.\n\t * Default is {@link ClassFilter#TRUE}.\n\t * @see NameMatchMethodPointcut#setClassFilter\n\t */",
            "\t/**\n\t * 设置本切入点使用的 {@link ClassFilter}。\n\t * 默认为 {@link ClassFilter#TRUE}。\n\t * @see NameMatchMethodPointcut#setClassFilter\n\t */",
        ),
        (
            "\t/**\n\t * Convenience method when we have only a single method name to match.\n\t * Use either this method or {@code setMappedNames}, not both.\n\t * @see #setMappedNames\n\t * @see NameMatchMethodPointcut#setMappedName\n\t */",
            "\t/**\n\t * 仅匹配单个方法名时的便捷方法。\n\t * 使用本方法或 {@code setMappedNames} 之一，不可同时使用。\n\t * @see #setMappedNames\n\t * @see NameMatchMethodPointcut#setMappedName\n\t */",
        ),
        (
            "\t/**\n\t * Set the method names defining methods to match.\n\t * Matching will be the union of all these; if any match,\n\t * the pointcut matches.\n\t * @see NameMatchMethodPointcut#setMappedNames\n\t */",
            "\t/**\n\t * 设置定义要匹配方法的方法名。\n\t * 匹配为所有名称的并集；任一匹配则切入点匹配。\n\t * @see NameMatchMethodPointcut#setMappedNames\n\t */",
        ),
        (
            "\t/**\n\t * Add another eligible method name, in addition to those already named.\n\t * Like the set methods, this method is for use when configuring proxies,\n\t * before a proxy is used.\n\t * @param name the name of the additional method that will match\n\t * @return this pointcut to allow for multiple additions in one line\n\t * @see NameMatchMethodPointcut#addMethodName\n\t */",
            "\t/**\n\t * 在已命名方法之外再添加一个符合条件的方法名。\n\t * 与 set 方法类似，本方法用于配置代理、代理使用前。\n\t * @param name 将匹配的额外方法名\n\t * @return 本切入点，支持一行内多次添加\n\t * @see NameMatchMethodPointcut#addMethodName\n\t */",
        ),
    ],
    "RegexpMethodPointcutAdvisor.java": [
        (
            "/**\n * Convenient class for regexp method pointcuts that hold an Advice,\n * making them an {@link org.springframework.aop.Advisor}.\n *\n * <p>Configure this class using the \"pattern\" and \"patterns\"\n * pass-through properties. These are analogous to the pattern\n * and patterns properties of {@link AbstractRegexpMethodPointcut}.\n *\n * <p>Can delegate to any {@link AbstractRegexpMethodPointcut} subclass.\n * By default, {@link JdkRegexpMethodPointcut} will be used. To choose\n * a specific one, override the {@link #createPointcut} method.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @see #setPattern\n * @see #setPatterns\n * @see JdkRegexpMethodPointcut\n */",
            "/**\n * 持有 Advice 的正则表达式方法切入点的便捷类，\n * 使其成为 {@link org.springframework.aop.Advisor}。\n *\n * <p>使用 \"pattern\" 和 \"patterns\" 透传属性配置本类。\n * 这些属性与 {@link AbstractRegexpMethodPointcut} 的 pattern\n * 和 patterns 属性类似。\n *\n * <p>可委托给任意 {@link AbstractRegexpMethodPointcut} 子类。\n * 默认使用 {@link JdkRegexpMethodPointcut}。\n * 要选择特定实现，覆盖 {@link #createPointcut} 方法。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @see #setPattern\n * @see #setPatterns\n * @see JdkRegexpMethodPointcut\n */",
        ),
        (
            "\t/**\n\t * Create an empty RegexpMethodPointcutAdvisor.\n\t * @see #setPattern\n\t * @see #setPatterns\n\t * @see #setAdvice\n\t */",
            "\t/**\n\t * 创建空的 RegexpMethodPointcutAdvisor。\n\t * @see #setPattern\n\t * @see #setPatterns\n\t * @see #setAdvice\n\t */",
        ),
        (
            "\t/**\n\t * Create a RegexpMethodPointcutAdvisor for the given advice.\n\t * The pattern still needs to be specified afterwards.\n\t * @param advice the advice to use\n\t * @see #setPattern\n\t * @see #setPatterns\n\t */",
            "\t/**\n\t * 为给定 advice 创建 RegexpMethodPointcutAdvisor。\n\t * 之后仍需指定 pattern。\n\t * @param advice 要使用的 advice\n\t * @see #setPattern\n\t * @see #setPatterns\n\t */",
        ),
        (
            "\t/**\n\t * Create a RegexpMethodPointcutAdvisor for the given advice.\n\t * @param pattern the pattern to use\n\t * @param advice the advice to use\n\t */",
            "\t/**\n\t * 为给定 advice 创建 RegexpMethodPointcutAdvisor。\n\t * @param pattern 要使用的 pattern\n\t * @param advice 要使用的 advice\n\t */",
        ),
        (
            "\t/**\n\t * Create a RegexpMethodPointcutAdvisor for the given advice.\n\t * @param patterns the patterns to use\n\t * @param advice the advice to use\n\t */",
            "\t/**\n\t * 为给定 advice 创建 RegexpMethodPointcutAdvisor。\n\t * @param patterns 要使用的 patterns\n\t * @param advice 要使用的 advice\n\t */",
        ),
        (
            "\t/**\n\t * Set the regular expression defining methods to match.\n\t * <p>Use either this method or {@link #setPatterns}, not both.\n\t * @see #setPatterns\n\t */",
            "\t/**\n\t * 设置定义要匹配方法的正则表达式。\n\t * <p>使用本方法或 {@link #setPatterns} 之一，不可同时使用。\n\t * @see #setPatterns\n\t */",
        ),
        (
            "\t/**\n\t * Set the regular expressions defining methods to match.\n\t * To be passed through to the pointcut implementation.\n\t * <p>Matching will be the union of all these; if any of the\n\t * patterns matches, the pointcut matches.\n\t * @see AbstractRegexpMethodPointcut#setPatterns\n\t */",
            "\t/**\n\t * 设置定义要匹配方法的正则表达式。\n\t * 将透传给切入点实现。\n\t * <p>匹配为所有模式的并集；任一模式匹配则切入点匹配。\n\t * @see AbstractRegexpMethodPointcut#setPatterns\n\t */",
        ),
        (
            "\t/**\n\t * Initialize the singleton Pointcut held within this Advisor.\n\t */",
            "\t/**\n\t * 初始化本 Advisor 内持有的单例 Pointcut。\n\t */",
        ),
        (
            "\t/**\n\t * Create the actual pointcut: By default, a {@link JdkRegexpMethodPointcut}\n\t * will be used.\n\t * @return the Pointcut instance (never {@code null})\n\t */",
            "\t/**\n\t * 创建实际切入点：默认使用 {@link JdkRegexpMethodPointcut}。\n\t * @return Pointcut 实例（永不为 {@code null}）\n\t */",
        ),
        (
            "\t/**\n\t * Empty class used for a serializable monitor object.\n\t */",
            "\t/**\n\t * 用于可序列化监视器对象的空类。\n\t */",
        ),
    ],
    "Pointcuts.java": [
        (
            "/**\n * Pointcut constants for matching getters and setters,\n * and static methods useful for manipulating and evaluating pointcuts.\n *\n * <p>These methods are particularly useful for composing pointcuts\n * using the union and intersection methods.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n */",
            "/**\n * 匹配 getter 和 setter 的切入点常量，\n * 以及操作和评估切入点的静态方法。\n *\n * <p>这些方法在使用 union 和 intersection 方法组合切入点时特别有用。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n */",
        ),
        (
            "\t/** Pointcut matching all bean property setters, in any class. */",
            "\t/** 匹配任意类中所有 bean 属性 setter 的切入点。 */",
        ),
        (
            "\t/** Pointcut matching all bean property getters, in any class. */",
            "\t/** 匹配任意类中所有 bean 属性 getter 的切入点。 */",
        ),
        (
            "\t/**\n\t * Match all methods that <b>either</b> (or both) of the given pointcuts matches.\n\t * @param pc1 the first Pointcut\n\t * @param pc2 the second Pointcut\n\t * @return a distinct Pointcut that matches all methods that either\n\t * of the given Pointcuts matches\n\t */",
            "\t/**\n\t * 匹配<b>任一</b>（或两者）给定切入点匹配的所有方法。\n\t * @param pc1 第一个 Pointcut\n\t * @param pc2 第二个 Pointcut\n\t * @return 匹配任一给定 Pointcut 的所有方法的独立 Pointcut\n\t */",
        ),
        (
            "\t/**\n\t * Match all methods that <b>both</b> the given pointcuts match.\n\t * @param pc1 the first Pointcut\n\t * @param pc2 the second Pointcut\n\t * @return a distinct Pointcut that matches all methods that both\n\t * of the given Pointcuts match\n\t */",
            "\t/**\n\t * 匹配<b>两个</b>给定切入点均匹配的所有方法。\n\t * @param pc1 第一个 Pointcut\n\t * @param pc2 第二个 Pointcut\n\t * @return 匹配两个给定 Pointcut 的所有方法的独立 Pointcut\n\t */",
        ),
        (
            "\t/**\n\t * Perform the least expensive check for a pointcut match.\n\t * @param pointcut the pointcut to match\n\t * @param method the candidate method\n\t * @param targetClass the target class\n\t * @param args arguments to the method\n\t * @return whether there's a runtime match\n\t */",
            "\t/**\n\t * 执行开销最小的切入点匹配检查。\n\t * @param pointcut 待匹配的切入点\n\t * @param method 候选方法\n\t * @param targetClass 目标类\n\t * @param args 方法参数\n\t * @return 是否存在运行时匹配\n\t */",
        ),
        (
            "\t\t\t// Only check if it gets past first hurdle.",
            "\t\t\t// 仅当通过第一关时才继续检查。",
        ),
        (
            "\t\t\t\t// We may need additional runtime (argument) check.",
            "\t\t\t\t// 可能需要额外的运行时（参数）检查。",
        ),
        (
            "\t/**\n\t * Pointcut implementation that matches bean property setters.\n\t */",
            "\t/**\n\t * 匹配 bean 属性 setter 的切入点实现。\n\t */",
        ),
        (
            "\t/**\n\t * Pointcut implementation that matches bean property getters.\n\t */",
            "\t/**\n\t * 匹配 bean 属性 getter 的切入点实现。\n\t */",
        ),
    ],
}
