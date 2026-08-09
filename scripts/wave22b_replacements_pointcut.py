"""Chinese JavaDoc replacements for springframework wave22b pointcut/support utils [14:17]."""

POINTCUT_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "AopUtils.java": [
        (
            "/**\n * Utility methods for AOP support code.\n *\n * <p>Mainly for internal use within Spring's AOP support.\n *\n * <p>See {@link org.springframework.aop.framework.AopProxyUtils} for a\n * collection of framework-specific AOP utility methods which depend\n * on internals of Spring's AOP framework implementation.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @author Rob Harrop\n * @author Sebastien Deleuze\n * @see org.springframework.aop.framework.AopProxyUtils\n */",
            "/**\n * AOP 支持代码的工具方法。\n *\n * <p>主要供 Spring AOP 支持内部使用。\n *\n * <p>依赖 Spring AOP 框架内部实现的框架专用 AOP 工具方法见\n * {@link org.springframework.aop.framework.AopProxyUtils}。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @author Rob Harrop\n * @author Sebastien Deleuze\n * @see org.springframework.aop.framework.AopProxyUtils\n */",
        ),
        (
            "\t/**\n\t * Check whether the given object is a JDK dynamic proxy or a CGLIB proxy.\n\t * <p>This method additionally checks if the given object is an instance\n\t * of {@link SpringProxy}.\n\t * @param object the object to check\n\t * @see #isJdkDynamicProxy\n\t * @see #isCglibProxy\n\t */",
            "\t/**\n\t * 检查给定对象是否为 JDK 动态代理或 CGLIB 代理。\n\t * <p>本方法还会检查给定对象是否为 {@link SpringProxy} 的实例。\n\t * @param object 待检查的对象\n\t * @see #isJdkDynamicProxy\n\t * @see #isCglibProxy\n\t */",
        ),
        (
            "\t/**\n\t * Check whether the given object is a JDK dynamic proxy.\n\t * <p>This method goes beyond the implementation of\n\t * {@link Proxy#isProxyClass(Class)} by additionally checking if the\n\t * given object is an instance of {@link SpringProxy}.\n\t * @param object the object to check\n\t * @see java.lang.reflect.Proxy#isProxyClass\n\t */",
            "\t/**\n\t * 检查给定对象是否为 JDK 动态代理。\n\t * <p>本方法在 {@link Proxy#isProxyClass(Class)} 基础上，\n\t * 额外检查给定对象是否为 {@link SpringProxy} 的实例。\n\t * @param object 待检查的对象\n\t * @see java.lang.reflect.Proxy#isProxyClass\n\t */",
        ),
        (
            "\t/**\n\t * Check whether the given object is a CGLIB proxy.\n\t * <p>This method goes beyond the implementation of\n\t * {@link ClassUtils#isCglibProxy(Object)} by additionally checking if\n\t * the given object is an instance of {@link SpringProxy}.\n\t * @param object the object to check\n\t * @see ClassUtils#isCglibProxy(Object)\n\t */",
            "\t/**\n\t * 检查给定对象是否为 CGLIB 代理。\n\t * <p>本方法在 {@link ClassUtils#isCglibProxy(Object)} 基础上，\n\t * 额外检查给定对象是否为 {@link SpringProxy} 的实例。\n\t * @param object 待检查的对象\n\t * @see ClassUtils#isCglibProxy(Object)\n\t */",
        ),
        (
            "\t/**\n\t * Determine the target class of the given bean instance which might be an AOP proxy.\n\t * <p>Returns the target class for an AOP proxy or the plain class otherwise.\n\t * @param candidate the instance to check (might be an AOP proxy)\n\t * @return the target class (or the plain class of the given object as fallback;\n\t * never {@code null})\n\t * @see org.springframework.aop.TargetClassAware#getTargetClass()\n\t * @see org.springframework.aop.framework.AopProxyUtils#ultimateTargetClass(Object)\n\t */",
            "\t/**\n\t * 确定给定 Bean 实例（可能是 AOP 代理）的目标类。\n\t * <p>对 AOP 代理返回目标类，否则返回普通类。\n\t * @param candidate 待检查的实例（可能是 AOP 代理）\n\t * @return 目标类（或作为回退的给定对象普通类；\n\t * 永不为 {@code null}）\n\t * @see org.springframework.aop.TargetClassAware#getTargetClass()\n\t * @see org.springframework.aop.framework.AopProxyUtils#ultimateTargetClass(Object)\n\t */",
        ),
        (
            "\t/**\n\t * Select an invocable method on the target type: either the given method itself\n\t * if actually exposed on the target type, or otherwise a corresponding method\n\t * on one of the target type's interfaces or on the target type itself.\n\t * @param method the method to check\n\t * @param targetType the target type to search methods on (typically an AOP proxy)\n\t * @return a corresponding invocable method on the target type\n\t * @throws IllegalStateException if the given method is not invocable on the given\n\t * target type (typically due to a proxy mismatch)\n\t * @since 4.3\n\t * @see MethodIntrospector#selectInvocableMethod(Method, Class)\n\t */",
            "\t/**\n\t * 在目标类型上选择可调用方法：若给定方法在目标类型上实际暴露则直接使用，\n\t * 否则在目标类型的接口或目标类型本身上查找对应方法。\n\t * @param method 待检查的方法\n\t * @param targetType 搜索方法的目标类型（通常为 AOP 代理）\n\t * @return 目标类型上对应的可调用方法\n\t * @throws IllegalStateException 若给定方法在目标类型上不可调用\n\t * （通常因代理不匹配）\n\t * @since 4.3\n\t * @see MethodIntrospector#selectInvocableMethod(Method, Class)\n\t */",
        ),
        (
            "\t/**\n\t * Determine whether the given method is an \"equals\" method.\n\t * @see java.lang.Object#equals\n\t */",
            "\t/**\n\t * 判断给定方法是否为 \"equals\" 方法。\n\t * @see java.lang.Object#equals\n\t */",
        ),
        (
            "\t/**\n\t * Determine whether the given method is a \"hashCode\" method.\n\t * @see java.lang.Object#hashCode\n\t */",
            "\t/**\n\t * 判断给定方法是否为 \"hashCode\" 方法。\n\t * @see java.lang.Object#hashCode\n\t */",
        ),
        (
            "\t/**\n\t * Determine whether the given method is a \"toString\" method.\n\t * @see java.lang.Object#toString()\n\t */",
            "\t/**\n\t * 判断给定方法是否为 \"toString\" 方法。\n\t * @see java.lang.Object#toString()\n\t */",
        ),
        (
            "\t/**\n\t * Determine whether the given method is a \"finalize\" method.\n\t * @see java.lang.Object#finalize()\n\t */",
            "\t/**\n\t * 判断给定方法是否为 \"finalize\" 方法。\n\t * @see java.lang.Object#finalize()\n\t */",
        ),
        (
            "\t/**\n\t * Given a method, which may come from an interface, and a target class used\n\t * in the current AOP invocation, find the corresponding target method if there\n\t * is one. For example, the method may be {@code IFoo.bar()} and the target class\n\t * may be {@code DefaultFoo}. In this case, the method may be\n\t * {@code DefaultFoo.bar()}. This enables attributes on that method to be found.\n\t * <p><b>NOTE:</b> In contrast to {@link org.springframework.util.ClassUtils#getMostSpecificMethod},\n\t * this method resolves bridge methods in order to retrieve attributes from\n\t * the <i>original</i> method definition.\n\t * @param method the method to be invoked, which may come from an interface\n\t * @param targetClass the target class for the current invocation\n\t * (can be {@code null} or may not even implement the method)\n\t * @return the specific target method, or the original method if the\n\t * {@code targetClass} does not implement it\n\t * @see org.springframework.util.ClassUtils#getMostSpecificMethod\n\t * @see org.springframework.core.BridgeMethodResolver#getMostSpecificMethod\n\t */",
            "\t/**\n\t * 给定可能来自接口的方法及当前 AOP 调用使用的目标类，\n\t * 查找对应的目标方法（若存在）。\n\t * 例如方法可能是 {@code IFoo.bar()}，目标类可能是 {@code DefaultFoo}，\n\t * 此时方法可能是 {@code DefaultFoo.bar()}。\n\t * 从而可找到该方法上的属性。\n\t * <p><b>注意：</b>与 {@link org.springframework.util.ClassUtils#getMostSpecificMethod} 不同，\n\t * 本方法解析桥接方法，以从<i>原始</i>方法定义获取属性。\n\t * @param method 待调用的方法，可能来自接口\n\t * @param targetClass 当前调用的目标类\n\t * （可为 {@code null} 或可能未实现该方法）\n\t * @return 具体目标方法；若 {@code targetClass} 未实现则返回原方法\n\t * @see org.springframework.util.ClassUtils#getMostSpecificMethod\n\t * @see org.springframework.core.BridgeMethodResolver#getMostSpecificMethod\n\t */",
        ),
        (
            "\t/**\n\t * Can the given pointcut apply at all on the given class?\n\t * <p>This is an important test as it can be used to optimize\n\t * out a pointcut for a class.\n\t * @param pc the static or dynamic pointcut to check\n\t * @param targetClass the class to test\n\t * @return whether the pointcut can apply on any method\n\t */",
            "\t/**\n\t * 给定切入点是否能在给定类上应用？\n\t * <p>这是重要测试，可用于对类优化掉切入点。\n\t * @param pc 待检查的静态或动态切入点\n\t * @param targetClass 待测试的类\n\t * @return 切入点是否可应用于任意方法\n\t */",
        ),
        (
            "\t/**\n\t * Can the given pointcut apply at all on the given class?\n\t * <p>This is an important test as it can be used to optimize\n\t * out a pointcut for a class.\n\t * @param pc the static or dynamic pointcut to check\n\t * @param targetClass the class to test\n\t * @param hasIntroductions whether the advisor chain\n\t * for this bean includes any introductions\n\t * @return whether the pointcut can apply on any method\n\t */",
            "\t/**\n\t * 给定切入点是否能在给定类上应用？\n\t * <p>这是重要测试，可用于对类优化掉切入点。\n\t * @param pc 待检查的静态或动态切入点\n\t * @param targetClass 待测试的类\n\t * @param hasIntroductions 本 Bean 的 advisor 链是否包含引入\n\t * @return 切入点是否可应用于任意方法\n\t */",
        ),
        (
            "\t\t\t// No need to iterate the methods if we're matching any method anyway...",
            "\t\t\t// 若本就会匹配任意方法，则无需遍历方法...",
        ),
        (
            "\t/**\n\t * Can the given advisor apply at all on the given class?\n\t * This is an important test as it can be used to optimize\n\t * out an advisor for a class.\n\t * @param advisor the advisor to check\n\t * @param targetClass class we're testing\n\t * @return whether the pointcut can apply on any method\n\t */",
            "\t/**\n\t * 给定 advisor 是否能在给定类上应用？\n\t * 这是重要测试，可用于对类优化掉 advisor。\n\t * @param advisor 待检查的 advisor\n\t * @param targetClass 待测试的类\n\t * @return 切入点是否可应用于任意方法\n\t */",
        ),
        (
            "\t/**\n\t * Can the given advisor apply at all on the given class?\n\t * <p>This is an important test as it can be used to optimize out an advisor for a class.\n\t * This version also takes into account introductions (for IntroductionAwareMethodMatchers).\n\t * @param advisor the advisor to check\n\t * @param targetClass class we're testing\n\t * @param hasIntroductions whether the advisor chain for this bean includes\n\t * any introductions\n\t * @return whether the pointcut can apply on any method\n\t */",
            "\t/**\n\t * 给定 advisor 是否能在给定类上应用？\n\t * <p>这是重要测试，可用于对类优化掉 advisor。\n\t * 本版本还考虑引入（用于 IntroductionAwareMethodMatcher）。\n\t * @param advisor 待检查的 advisor\n\t * @param targetClass 待测试的类\n\t * @param hasIntroductions 本 Bean 的 advisor 链是否包含引入\n\t * @return 切入点是否可应用于任意方法\n\t */",
        ),
        (
            "\t\t\t// It doesn't have a pointcut so we assume it applies.",
            "\t\t\t// 无切入点，假定可应用。",
        ),
        (
            "\t/**\n\t * Determine the sublist of the {@code candidateAdvisors} list\n\t * that is applicable to the given class.\n\t * @param candidateAdvisors the Advisors to evaluate\n\t * @param clazz the target class\n\t * @return sublist of Advisors that can apply to an object of the given class\n\t * (may be the incoming List as-is)\n\t */",
            "\t/**\n\t * 确定 {@code candidateAdvisors} 列表中适用于给定类的子列表。\n\t * @param candidateAdvisors 待评估的 Advisor\n\t * @param clazz 目标类\n\t * @return 可应用于给定类对象的 Advisor 子列表\n\t * （可能是原 List 本身）\n\t */",
        ),
        (
            "\t\t\t\t// already processed",
            "\t\t\t\t// 已处理",
        ),
        (
            "\t/**\n\t * Invoke the given target via reflection, as part of an AOP method invocation.\n\t * @param target the target object\n\t * @param method the method to invoke\n\t * @param args the arguments for the method\n\t * @return the invocation result, if any\n\t * @throws Throwable if thrown by the target method\n\t * @throws org.springframework.aop.AopInvocationException in case of a reflection error\n\t */",
            "\t/**\n\t * 作为 AOP 方法调用的一部分，通过反射调用给定目标。\n\t * @param target 目标对象\n\t * @param method 要调用的方法\n\t * @param args 方法参数\n\t * @return 调用结果（若有）\n\t * @throws Throwable 若目标方法抛出\n\t * @throws org.springframework.aop.AopInvocationException 若反射出错\n\t */",
        ),
        (
            "\t\t// Use reflection to invoke the method.",
            "\t\t// 使用反射调用方法。",
        ),
        (
            "\t\t\t// Invoked method threw a checked exception.\n\t\t\t// We must rethrow it. The client won't see the interceptor.",
            "\t\t\t// 被调用方法抛出受检异常。\n\t\t\t// 必须重新抛出；客户端不会看到拦截器。",
        ),
        (
            "\t/**\n\t * Inner class to avoid a hard dependency on Kotlin at runtime.\n\t */",
            "\t/**\n\t * 内部类，避免运行时对 Kotlin 的硬依赖。\n\t */",
        ),
    ],
    "ClassFilters.java": [
        (
            "/**\n * Static utility methods for composing {@link ClassFilter ClassFilters}.\n *\n * @author Rod Johnson\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @author Sam Brannen\n * @since 11.11.2003\n * @see MethodMatchers\n * @see Pointcuts\n */",
            "/**\n * 组合 {@link ClassFilter ClassFilter} 的静态工具方法。\n *\n * @author Rod Johnson\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @author Sam Brannen\n * @since 11.11.2003\n * @see MethodMatchers\n * @see Pointcuts\n */",
        ),
        (
            "\t/**\n\t * Match all classes that <i>either</i> (or both) of the given ClassFilters matches.\n\t * @param cf1 the first ClassFilter\n\t * @param cf2 the second ClassFilter\n\t * @return a distinct ClassFilter that matches all classes that either\n\t * of the given ClassFilter matches\n\t */",
            "\t/**\n\t * 匹配给定 ClassFilter 中<i>任一</i>（或两者）匹配的类。\n\t * @param cf1 第一个 ClassFilter\n\t * @param cf2 第二个 ClassFilter\n\t * @return 匹配任一给定 ClassFilter 的独立 ClassFilter\n\t */",
        ),
        (
            "\t/**\n\t * Match all classes that <i>either</i> (or all) of the given ClassFilters matches.\n\t * @param classFilters the ClassFilters to match\n\t * @return a distinct ClassFilter that matches all classes that either\n\t * of the given ClassFilter matches\n\t */",
            "\t/**\n\t * 匹配给定 ClassFilter 中<i>任一</i>（或全部）匹配的类。\n\t * @param classFilters 待匹配的 ClassFilter\n\t * @return 匹配任一给定 ClassFilter 的独立 ClassFilter\n\t */",
        ),
        (
            "\t/**\n\t * Match all classes that <i>both</i> of the given ClassFilters match.\n\t * @param cf1 the first ClassFilter\n\t * @param cf2 the second ClassFilter\n\t * @return a distinct ClassFilter that matches all classes that both\n\t * of the given ClassFilter match\n\t */",
            "\t/**\n\t * 匹配给定 ClassFilter <i>两者都</i>匹配的类。\n\t * @param cf1 第一个 ClassFilter\n\t * @param cf2 第二个 ClassFilter\n\t * @return 匹配两个给定 ClassFilter 的独立 ClassFilter\n\t */",
        ),
        (
            "\t/**\n\t * Match all classes that <i>all</i> of the given ClassFilters match.\n\t * @param classFilters the ClassFilters to match\n\t * @return a distinct ClassFilter that matches all classes that both\n\t * of the given ClassFilter match\n\t */",
            "\t/**\n\t * 匹配给定 ClassFilter <i>全部</i>都匹配的类。\n\t * @param classFilters 待匹配的 ClassFilter\n\t * @return 匹配所有给定 ClassFilter 的独立 ClassFilter\n\t */",
        ),
        (
            "\t/**\n\t * Return a class filter that represents the logical negation of the specified\n\t * filter instance.\n\t * @param classFilter the {@link ClassFilter} to negate\n\t * @return a filter that represents the logical negation of the specified filter\n\t * @since 6.1\n\t */",
            "\t/**\n\t * 返回表示指定过滤器实例逻辑取反的类过滤器。\n\t * @param classFilter 要取反的 {@link ClassFilter}\n\t * @return 表示指定过滤器逻辑取反的过滤器\n\t * @since 6.1\n\t */",
        ),
        (
            "\t/**\n\t * ClassFilter implementation for a union of the given ClassFilters.\n\t */",
            "\t/**\n\t * 给定 ClassFilter 并集的 ClassFilter 实现。\n\t */",
        ),
        (
            "\t/**\n\t * ClassFilter implementation for an intersection of the given ClassFilters.\n\t */",
            "\t/**\n\t * 给定 ClassFilter 交集的 ClassFilter 实现。\n\t */",
        ),
        (
            "\t/**\n\t * ClassFilter implementation for a logical negation of the given ClassFilter.\n\t */",
            "\t/**\n\t * 给定 ClassFilter 逻辑取反的 ClassFilter 实现。\n\t */",
        ),
    ],
    "ComposablePointcut.java": [
        (
            "/**\n * Convenient class for building up pointcuts.\n *\n * <p>All methods return {@code ComposablePointcut}, so we can use concise idioms\n * like in the following example.\n *\n * <pre class=\"code\">Pointcut pc = new ComposablePointcut()\n *                      .union(classFilter)\n *                      .intersection(methodMatcher)\n *                      .intersection(pointcut);</pre>\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @author Rob Harrop\n * @since 11.11.2003\n * @see Pointcuts\n */",
            "/**\n * 构建切入点的便捷类。\n *\n * <p>所有方法返回 {@code ComposablePointcut}，可使用如下简洁写法：\n *\n * <pre class=\"code\">Pointcut pc = new ComposablePointcut()\n *                      .union(classFilter)\n *                      .intersection(methodMatcher)\n *                      .intersection(pointcut);</pre>\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @author Rob Harrop\n * @since 11.11.2003\n * @see Pointcuts\n */",
        ),
        (
            "\t/** use serialVersionUID from Spring 1.2 for interoperability. */",
            "\t/** 使用 Spring 1.2 的 serialVersionUID 以保持互操作性。 */",
        ),
        (
            "\t/**\n\t * Create a default ComposablePointcut, with {@code ClassFilter.TRUE}\n\t * and {@code MethodMatcher.TRUE}.\n\t */",
            "\t/**\n\t * 创建默认 ComposablePointcut，\n\t * 使用 {@code ClassFilter.TRUE} 与 {@code MethodMatcher.TRUE}。\n\t */",
        ),
        (
            "\t/**\n\t * Create a ComposablePointcut based on the given Pointcut.\n\t * @param pointcut the original Pointcut\n\t */",
            "\t/**\n\t * 基于给定 Pointcut 创建 ComposablePointcut。\n\t * @param pointcut 原始 Pointcut\n\t */",
        ),
        (
            "\t/**\n\t * Create a ComposablePointcut for the given ClassFilter,\n\t * with {@code MethodMatcher.TRUE}.\n\t * @param classFilter the ClassFilter to use\n\t */",
            "\t/**\n\t * 为给定 ClassFilter 创建 ComposablePointcut，\n\t * 使用 {@code MethodMatcher.TRUE}。\n\t * @param classFilter 要使用的 ClassFilter\n\t */",
        ),
        (
            "\t/**\n\t * Create a ComposablePointcut for the given MethodMatcher,\n\t * with {@code ClassFilter.TRUE}.\n\t * @param methodMatcher the MethodMatcher to use\n\t */",
            "\t/**\n\t * 为给定 MethodMatcher 创建 ComposablePointcut，\n\t * 使用 {@code ClassFilter.TRUE}。\n\t * @param methodMatcher 要使用的 MethodMatcher\n\t */",
        ),
        (
            "\t/**\n\t * Create a ComposablePointcut for the given ClassFilter and MethodMatcher.\n\t * @param classFilter the ClassFilter to use\n\t * @param methodMatcher the MethodMatcher to use\n\t */",
            "\t/**\n\t * 为给定 ClassFilter 与 MethodMatcher 创建 ComposablePointcut。\n\t * @param classFilter 要使用的 ClassFilter\n\t * @param methodMatcher 要使用的 MethodMatcher\n\t */",
        ),
        (
            "\t/**\n\t * Apply a union with the given ClassFilter.\n\t * @param other the ClassFilter to apply a union with\n\t * @return this composable pointcut (for call chaining)\n\t */",
            "\t/**\n\t * 与给定 ClassFilter 求并。\n\t * @param other 要求并的 ClassFilter\n\t * @return 本可组合切入点（用于链式调用）\n\t */",
        ),
        (
            "\t/**\n\t * Apply an intersection with the given ClassFilter.\n\t * @param other the ClassFilter to apply an intersection with\n\t * @return this composable pointcut (for call chaining)\n\t */",
            "\t/**\n\t * 与给定 ClassFilter 求交。\n\t * @param other 要求交的 ClassFilter\n\t * @return 本可组合切入点（用于链式调用）\n\t */",
        ),
        (
            "\t/**\n\t * Apply a union with the given MethodMatcher.\n\t * @param other the MethodMatcher to apply a union with\n\t * @return this composable pointcut (for call chaining)\n\t */",
            "\t/**\n\t * 与给定 MethodMatcher 求并。\n\t * @param other 要求并的 MethodMatcher\n\t * @return 本可组合切入点（用于链式调用）\n\t */",
        ),
        (
            "\t/**\n\t * Apply an intersection with the given MethodMatcher.\n\t * @param other the MethodMatcher to apply an intersection with\n\t * @return this composable pointcut (for call chaining)\n\t */",
            "\t/**\n\t * 与给定 MethodMatcher 求交。\n\t * @param other 要求交的 MethodMatcher\n\t * @return 本可组合切入点（用于链式调用）\n\t */",
        ),
        (
            "\t/**\n\t * Apply a union with the given Pointcut.\n\t * <p>Note that for a Pointcut union, methods will only match if their\n\t * original ClassFilter (from the originating Pointcut) matches as well.\n\t * MethodMatchers and ClassFilters from different Pointcuts will never\n\t * get interleaved with each other.\n\t * @param other the Pointcut to apply a union with\n\t * @return this composable pointcut (for call chaining)\n\t */",
            "\t/**\n\t * 与给定 Pointcut 求并。\n\t * <p>注意：Pointcut 并集下，方法仅当其原始 ClassFilter\n\t * （来自源 Pointcut）也匹配时才匹配。\n\t * 不同 Pointcut 的 MethodMatcher 与 ClassFilter 不会相互交错。\n\t * @param other 要求并的 Pointcut\n\t * @return 本可组合切入点（用于链式调用）\n\t */",
        ),
        (
            "\t/**\n\t * Apply an intersection with the given Pointcut.\n\t * @param other the Pointcut to apply an intersection with\n\t * @return this composable pointcut (for call chaining)\n\t */",
            "\t/**\n\t * 与给定 Pointcut 求交。\n\t * @param other 要求交的 Pointcut\n\t * @return 本可组合切入点（用于链式调用）\n\t */",
        ),
    ],
    "ControlFlowPointcut.java": [
        (
            "/**\n * Pointcut and method matcher for use as a simple <b>cflow</b>-style pointcut.\n *\n * <p>Each configured method name pattern can be an exact method name or a\n * pattern (see {@link #isMatch(String, String)} for details on the supported\n * pattern styles).\n *\n * <p>Note that evaluating such pointcuts is 10-15 times slower than evaluating\n * normal pointcuts, but they are useful in some cases.\n *\n * @author Rod Johnson\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @author Sam Brannen\n * @see NameMatchMethodPointcut\n * @see JdkRegexpMethodPointcut\n */",
            "/**\n * 用作简单 <b>cflow</b> 风格切入点的切入点与方法匹配器。\n *\n * <p>每个配置的方法名模式可为精确方法名或模式\n * （支持的模式风格见 {@link #isMatch(String, String)}）。\n *\n * <p>注意：评估此类切入点比普通切入点慢 10–15 倍，\n * 但在某些场景下很有用。\n *\n * @author Rod Johnson\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @author Sam Brannen\n * @see NameMatchMethodPointcut\n * @see JdkRegexpMethodPointcut\n */",
        ),
        (
            "\t/**\n\t * The class against which to match.\n\t * @since 6.1\n\t */",
            "\t/**\n\t * 用于匹配的类。\n\t * @since 6.1\n\t */",
        ),
        (
            "\t/**\n\t * An immutable list of distinct method name patterns against which to match.\n\t * @since 6.1\n\t */",
            "\t/**\n\t * 用于匹配的不重复方法名模式不可变列表。\n\t * @since 6.1\n\t */",
        ),
        (
            "\t/**\n\t * Construct a new pointcut that matches all control flows below the given class.\n\t * @param clazz the class\n\t */",
            "\t/**\n\t * 构造匹配给定类下所有控制流的新切入点。\n\t * @param clazz 类\n\t */",
        ),
        (
            "\t/**\n\t * Construct a new pointcut that matches all calls below a method matching\n\t * the given method name pattern in the given class.\n\t * <p>If no method name pattern is given, the pointcut matches all control flows\n\t * below the given class.\n\t * @param clazz the class\n\t * @param methodNamePattern the method name pattern (may be {@code null})\n\t */",
            "\t/**\n\t * 构造匹配给定类中符合方法名模式的方法下所有调用的新切入点。\n\t * <p>若未给定方法名模式，则匹配给定类下所有控制流。\n\t * @param clazz 类\n\t * @param methodNamePattern 方法名模式（可为 {@code null}）\n\t */",
        ),
        (
            "\t/**\n\t * Construct a new pointcut that matches all calls below a method matching\n\t * one of the given method name patterns in the given class.\n\t * <p>If no method name pattern is given, the pointcut matches all control flows\n\t * below the given class.\n\t * @param clazz the class\n\t * @param methodNamePatterns the method name patterns (potentially empty)\n\t * @since 6.1\n\t */\n\tpublic ControlFlowPointcut(Class<?> clazz, String... methodNamePatterns) {",
            "\t/**\n\t * 构造匹配给定类中符合任一方法名模式的方法下所有调用的新切入点。\n\t * <p>若未给定方法名模式，则匹配给定类下所有控制流。\n\t * @param clazz 类\n\t * @param methodNamePatterns 方法名模式（可能为空）\n\t * @since 6.1\n\t */\n\tpublic ControlFlowPointcut(Class<?> clazz, String... methodNamePatterns) {",
        ),
        (
            "\t/**\n\t * Construct a new pointcut that matches all calls below a method matching\n\t * one of the given method name patterns in the given class.\n\t * <p>If no method name pattern is given, the pointcut matches all control flows\n\t * below the given class.\n\t * @param clazz the class\n\t * @param methodNamePatterns the method name patterns (potentially empty)\n\t * @since 6.1\n\t */\n\tpublic ControlFlowPointcut(Class<?> clazz, List<String> methodNamePatterns) {",
            "\t/**\n\t * 构造匹配给定类中符合任一方法名模式的方法下所有调用的新切入点。\n\t * <p>若未给定方法名模式，则匹配给定类下所有控制流。\n\t * @param clazz 类\n\t * @param methodNamePatterns 方法名模式列表（可能为空）\n\t * @since 6.1\n\t */\n\tpublic ControlFlowPointcut(Class<?> clazz, List<String> methodNamePatterns) {",
        ),
        (
            "\t/**\n\t * Subclasses can override this for greater filtering (and performance).\n\t * <p>The default implementation always returns {@code true}.\n\t */",
            "\t/**\n\t * 子类可覆盖以实现更强过滤（及更好性能）。\n\t * <p>默认实现始终返回 {@code true}。\n\t */",
        ),
        (
            "\t/**\n\t * Subclasses can override this if it's possible to filter out some candidate classes.\n\t * <p>The default implementation always returns {@code true}.\n\t */",
            "\t/**\n\t * 若可过滤部分候选类，子类可覆盖。\n\t * <p>默认实现始终返回 {@code true}。\n\t */",
        ),
        (
            "\t/**\n\t * Get the number of times {@link #matches(Method, Class, Object...)} has been\n\t * evaluated.\n\t * <p>Useful for optimization and testing purposes.\n\t */",
            "\t/**\n\t * 获取 {@link #matches(Method, Class, Object...)} 被评估的次数。\n\t * <p>便于优化与测试。\n\t */",
        ),
        (
            "\t/**\n\t * Increment the {@link #getEvaluations() evaluation count}.\n\t * @since 6.1\n\t * @see #matches(Method, Class, Object...)\n\t */",
            "\t/**\n\t * 递增 {@link #getEvaluations() 评估计数}。\n\t * @since 6.1\n\t * @see #matches(Method, Class, Object...)\n\t */",
        ),
        (
            "\t/**\n\t * Determine if the given method name matches the method name pattern at the\n\t * specified index.\n\t * <p>This method is invoked by {@link #matches(Method, Class, Object...)}.\n\t * <p>The default implementation retrieves the method name pattern from\n\t * {@link #methodNamePatterns} and delegates to {@link #isMatch(String, String)}.\n\t * <p>Can be overridden in subclasses &mdash; for example, to support\n\t * regular expressions.\n\t * @param methodName the method name to check\n\t * @param patternIndex the index of the method name pattern\n\t * @return {@code true} if the method name matches the pattern at the specified\n\t * index\n\t * @since 6.1\n\t * @see #methodNamePatterns\n\t * @see #isMatch(String, String)\n\t * @see #matches(Method, Class, Object...)\n\t */",
            "\t/**\n\t * 判断给定方法名是否匹配指定索引处的方法名模式。\n\t * <p>由 {@link #matches(Method, Class, Object...)} 调用。\n\t * <p>默认实现从 {@link #methodNamePatterns} 取模式，\n\t * 并委托给 {@link #isMatch(String, String)}。\n\t * <p>子类可覆盖，例如以支持正则表达式。\n\t * @param methodName 待检查的方法名\n\t * @param patternIndex 方法名模式索引\n\t * @return 方法名匹配指定索引处模式则 {@code true}\n\t * @since 6.1\n\t * @see #methodNamePatterns\n\t * @see #isMatch(String, String)\n\t * @see #matches(Method, Class, Object...)\n\t */",
        ),
        (
            "\t/**\n\t * Determine if the given method name matches the method name pattern.\n\t * <p>This method is invoked by {@link #isMatch(String, int)}.\n\t * <p>The default implementation checks for direct equality as well as\n\t * {@code xxx*}, {@code *xxx}, {@code *xxx*}, and {@code xxx*yyy} matches.\n\t * <p>Can be overridden in subclasses &mdash; for example, to support a\n\t * different style of simple pattern matching.\n\t * @param methodName the method name to check\n\t * @param methodNamePattern the method name pattern\n\t * @return {@code true} if the method name matches the pattern\n\t * @since 6.1\n\t * @see #isMatch(String, int)\n\t * @see PatternMatchUtils#simpleMatch(String, String)\n\t */",
            "\t/**\n\t * 判断给定方法名是否匹配方法名模式。\n\t * <p>由 {@link #isMatch(String, int)} 调用。\n\t * <p>默认实现检查直接相等及\n\t * {@code xxx*}、{@code *xxx}、{@code *xxx*}、{@code xxx*yyy} 匹配。\n\t * <p>子类可覆盖，例如以支持不同风格的简单模式匹配。\n\t * @param methodName 待检查的方法名\n\t * @param methodNamePattern 方法名模式\n\t * @return 方法名匹配模式则 {@code true}\n\t * @since 6.1\n\t * @see #isMatch(String, int)\n\t * @see PatternMatchUtils#simpleMatch(String, String)\n\t */",
        ),
    ],
}
