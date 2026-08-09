"""Chinese JavaDoc replacements for springframework wave23a method matchers [3:10]."""

MATCHER_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "DynamicMethodMatcher.java": [
        (
            "/**\n * Convenient abstract superclass for dynamic method matchers,\n * which do care about arguments at runtime.\n *\n * @author Rod Johnson\n */",
            "/**\n * 动态方法匹配器的便捷抽象超类，\n * 在运行时会考虑参数。\n *\n * @author Rod Johnson\n */",
        ),
        (
            "\t/**\n\t * Can override to add preconditions for dynamic matching. This implementation\n\t * always returns true.\n\t */",
            "\t/**\n\t * 可覆盖以添加动态匹配的前置条件。\n\t * 本实现始终返回 true。\n\t */",
        ),
    ],
    "DynamicMethodMatcherPointcut.java": [
        (
            "/**\n * Convenient superclass when we want to force subclasses to\n * implement MethodMatcher interface, but subclasses\n * will want to be pointcuts. The getClassFilter() method can\n * be overridden to customize ClassFilter behavior as well.\n *\n * @author Rod Johnson\n */",
            "/**\n * 便捷超类：强制子类实现 MethodMatcher 接口，\n * 同时子类本身作为切入点。\n * 可覆盖 getClassFilter() 方法以自定义 ClassFilter 行为。\n *\n * @author Rod Johnson\n */",
        ),
    ],
    "ExpressionPointcut.java": [
        (
            "/**\n * Interface to be implemented by pointcuts that use String expressions.\n *\n * @author Rob Harrop\n * @since 2.0\n */",
            "/**\n * 使用字符串表达式的切入点应实现的接口。\n *\n * @author Rob Harrop\n * @since 2.0\n */",
        ),
        (
            "\t/**\n\t * Return the String expression for this pointcut.\n\t */",
            "\t/**\n\t * 返回本切入点的字符串表达式。\n\t */",
        ),
    ],
    "StaticMethodMatcher.java": [
        (
            "/**\n * Convenient abstract superclass for static method matchers, which don't care\n * about arguments at runtime.\n *\n * @author Rod Johnson\n */",
            "/**\n * 静态方法匹配器的便捷抽象超类，\n * 在运行时不考虑参数。\n *\n * @author Rod Johnson\n */",
        ),
        (
            "\t\t// should never be invoked because isRuntime() returns false",
            "\t\t// 不应被调用，因 isRuntime() 返回 false",
        ),
    ],
    "StaticMethodMatcherPointcut.java": [
        (
            "/**\n * Convenient superclass when we want to force subclasses to implement the\n * {@link MethodMatcher} interface but subclasses will want to be pointcuts.\n *\n * <p>The {@link #setClassFilter \"classFilter\"} property can be set to customize\n * {@link ClassFilter} behavior. The default is {@link ClassFilter#TRUE}.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n */",
            "/**\n * 便捷超类：强制子类实现 {@link MethodMatcher} 接口，\n * 同时子类本身作为切入点。\n *\n * <p>可设置 {@link #setClassFilter \"classFilter\"} 属性以自定义\n * {@link ClassFilter} 行为。默认为 {@link ClassFilter#TRUE}。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n */",
        ),
        (
            "\t/**\n\t * Set the {@link ClassFilter} to use for this pointcut.\n\t * Default is {@link ClassFilter#TRUE}.\n\t */",
            "\t/**\n\t * 设置本切入点使用的 {@link ClassFilter}。\n\t * 默认为 {@link ClassFilter#TRUE}。\n\t */",
        ),
    ],
    "StaticMethodMatcherPointcutAdvisor.java": [
        (
            "/**\n * Convenient base class for Advisors that are also static pointcuts.\n * Serializable if Advice and subclass are.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n */",
            "/**\n * 同时作为静态切入点的 Advisor 的便捷基类。\n * 若 Advice 与子类可序列化，则本类也可序列化。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n */",
        ),
        (
            "\t/**\n\t * Create a new StaticMethodMatcherPointcutAdvisor,\n\t * expecting bean-style configuration.\n\t * @see #setAdvice\n\t */",
            "\t/**\n\t * 创建新的 StaticMethodMatcherPointcutAdvisor，\n\t * 预期使用 bean 风格配置。\n\t * @see #setAdvice\n\t */",
        ),
        (
            "\t/**\n\t * Create a new StaticMethodMatcherPointcutAdvisor for the given advice.\n\t * @param advice the Advice to use\n\t */",
            "\t/**\n\t * 为给定 advice 创建新的 StaticMethodMatcherPointcutAdvisor。\n\t * @param advice 要使用的 Advice\n\t */",
        ),
    ],
    "RootClassFilter.java": [
        (
            "/**\n * Simple ClassFilter implementation that passes classes (and optionally subclasses).\n *\n * @author Rod Johnson\n * @author Sam Brannen\n */",
            "/**\n * 简单的 ClassFilter 实现，匹配类（及可选子类）。\n *\n * @author Rod Johnson\n * @author Sam Brannen\n */",
        ),
    ],
}
