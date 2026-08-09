"""Chinese JavaDoc replacements for springframework wave23a MethodMatchers.java."""

METHOD_MATCHERS_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "MethodMatchers.java": [
        (
            "/**\n * Static utility methods for composing {@link MethodMatcher MethodMatchers}.\n *\n * <p>A MethodMatcher may be evaluated statically (based on method and target\n * class) or need further evaluation dynamically (based on arguments at the\n * time of method invocation).\n *\n * @author Rod Johnson\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @author Sam Brannen\n * @since 11.11.2003\n * @see ClassFilters\n * @see Pointcuts\n */",
            "/**\n * 组合 {@link MethodMatcher MethodMatcher} 的静态工具方法。\n *\n * <p>MethodMatcher 可静态评估（基于方法和目标类），\n * 或需动态进一步评估（基于方法调用时的参数）。\n *\n * @author Rod Johnson\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @author Sam Brannen\n * @since 11.11.2003\n * @see ClassFilters\n * @see Pointcuts\n */",
        ),
        (
            "\t/**\n\t * Match all methods that <i>either</i> (or both) of the given MethodMatchers matches.\n\t * @param mm1 the first MethodMatcher\n\t * @param mm2 the second MethodMatcher\n\t * @return a distinct MethodMatcher that matches all methods that either\n\t * of the given MethodMatchers matches\n\t */",
            "\t/**\n\t * 匹配<i>任一</i>（或两者）给定 MethodMatcher 匹配的所有方法。\n\t * @param mm1 第一个 MethodMatcher\n\t * @param mm2 第二个 MethodMatcher\n\t * @return 匹配任一给定 MethodMatcher 的所有方法的独立 MethodMatcher\n\t */",
        ),
        (
            "\t/**\n\t * Match all methods that <i>either</i> (or both) of the given MethodMatchers matches.\n\t * @param mm1 the first MethodMatcher\n\t * @param cf1 the corresponding ClassFilter for the first MethodMatcher\n\t * @param mm2 the second MethodMatcher\n\t * @param cf2 the corresponding ClassFilter for the second MethodMatcher\n\t * @return a distinct MethodMatcher that matches all methods that either\n\t * of the given MethodMatchers matches\n\t */",
            "\t/**\n\t * 匹配<i>任一</i>（或两者）给定 MethodMatcher 匹配的所有方法。\n\t * @param mm1 第一个 MethodMatcher\n\t * @param cf1 第一个 MethodMatcher 对应的 ClassFilter\n\t * @param mm2 第二个 MethodMatcher\n\t * @param cf2 第二个 MethodMatcher 对应的 ClassFilter\n\t * @return 匹配任一给定 MethodMatcher 的所有方法的独立 MethodMatcher\n\t */",
        ),
        (
            "\t/**\n\t * Match all methods that <i>both</i> of the given MethodMatchers match.\n\t * @param mm1 the first MethodMatcher\n\t * @param mm2 the second MethodMatcher\n\t * @return a distinct MethodMatcher that matches all methods that both\n\t * of the given MethodMatchers match\n\t */",
            "\t/**\n\t * 匹配<i>两个</i>给定 MethodMatcher 均匹配的所有方法。\n\t * @param mm1 第一个 MethodMatcher\n\t * @param mm2 第二个 MethodMatcher\n\t * @return 匹配两个给定 MethodMatcher 的所有方法的独立 MethodMatcher\n\t */",
        ),
        (
            "\t/**\n\t * Return a method matcher that represents the logical negation of the specified\n\t * matcher instance.\n\t * @param methodMatcher the {@link MethodMatcher} to negate\n\t * @return a matcher that represents the logical negation of the specified matcher\n\t * @since 6.1\n\t */",
            "\t/**\n\t * 返回表示指定匹配器逻辑取反的方法匹配器。\n\t * @param methodMatcher 要取反的 {@link MethodMatcher}\n\t * @return 表示指定匹配器逻辑取反的匹配器\n\t * @since 6.1\n\t */",
        ),
        (
            "\t/**\n\t * Apply the given MethodMatcher to the given Method, supporting an\n\t * {@link org.springframework.aop.IntroductionAwareMethodMatcher}\n\t * (if applicable).\n\t * @param mm the MethodMatcher to apply (may be an IntroductionAwareMethodMatcher)\n\t * @param method the candidate method\n\t * @param targetClass the target class\n\t * @param hasIntroductions {@code true} if the object on whose behalf we are\n\t * asking is the subject on one or more introductions; {@code false} otherwise\n\t * @return whether this method matches statically\n\t */",
            "\t/**\n\t * 将给定 MethodMatcher 应用于给定 Method，\n\t * 支持 {@link org.springframework.aop.IntroductionAwareMethodMatcher}（若适用）。\n\t * @param mm 要应用的 MethodMatcher（可为 IntroductionAwareMethodMatcher）\n\t * @param method 候选方法\n\t * @param targetClass 目标类\n\t * @param hasIntroductions 若代表的对象是一个或多个引入的主体则为 {@code true}；\n\t * 否则为 {@code false}\n\t * @return 该方法是否静态匹配\n\t */",
        ),
        (
            "\t/**\n\t * MethodMatcher implementation for a union of two given MethodMatchers.\n\t */",
            "\t/**\n\t * 两个给定 MethodMatcher 并集的 MethodMatcher 实现。\n\t */",
        ),
        (
            "\t/**\n\t * MethodMatcher implementation for a union of two given MethodMatchers\n\t * of which at least one is an IntroductionAwareMethodMatcher.\n\t * @since 5.1\n\t */",
            "\t/**\n\t * 两个给定 MethodMatcher 并集的 MethodMatcher 实现，\n\t * 其中至少一个是 IntroductionAwareMethodMatcher。\n\t * @since 5.1\n\t */",
        ),
        (
            "\t/**\n\t * MethodMatcher implementation for a union of two given MethodMatchers,\n\t * supporting an associated ClassFilter per MethodMatcher.\n\t */",
            "\t/**\n\t * 两个给定 MethodMatcher 并集的 MethodMatcher 实现，\n\t * 每个 MethodMatcher 支持关联的 ClassFilter。\n\t */",
        ),
        (
            "\t\t\t// Allow for matching with regular UnionMethodMatcher by providing same hash...",
            "\t\t\t// 提供相同 hash 以与常规 UnionMethodMatcher 匹配...",
        ),
        (
            "\t/**\n\t * MethodMatcher implementation for a union of two given MethodMatchers\n\t * of which at least one is an IntroductionAwareMethodMatcher,\n\t * supporting an associated ClassFilter per MethodMatcher.\n\t * @since 5.1\n\t */",
            "\t/**\n\t * 两个给定 MethodMatcher 并集的 MethodMatcher 实现，\n\t * 其中至少一个是 IntroductionAwareMethodMatcher，\n\t * 每个 MethodMatcher 支持关联的 ClassFilter。\n\t * @since 5.1\n\t */",
        ),
        (
            "\t/**\n\t * MethodMatcher implementation for an intersection of two given MethodMatchers.\n\t */",
            "\t/**\n\t * 两个给定 MethodMatcher 交集的 MethodMatcher 实现。\n\t */",
        ),
        (
            "\t\t\t// Because a dynamic intersection may be composed of a static and dynamic part,\n\t\t\t// we must avoid calling the 3-arg matches method on a dynamic matcher, as\n\t\t\t// it will probably be an unsupported operation.",
            "\t\t\t// 动态交集可能由静态和动态部分组成，\n\t\t\t// 须避免对动态匹配器调用三参数 matches 方法，\n\t\t\t// 因其可能是不支持的操作。",
        ),
        (
            "\t/**\n\t * MethodMatcher implementation for an intersection of two given MethodMatchers\n\t * of which at least one is an IntroductionAwareMethodMatcher.\n\t * @since 5.1\n\t */",
            "\t/**\n\t * 两个给定 MethodMatcher 交集的 MethodMatcher 实现，\n\t * 其中至少一个是 IntroductionAwareMethodMatcher。\n\t * @since 5.1\n\t */",
        ),
    ],
}
