"""Chinese JavaDoc replacements for springframework wave19b aop [1:6]."""

AOP_A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "PointcutAdvisor.java": [
        (
            "/**\n * Superinterface for all Advisors that are driven by a pointcut.\n * This covers nearly all advisors except introduction advisors,\n * for which method-level matching doesn't apply.\n *\n * @author Rod Johnson\n */",
            "/**\n * 由切入点驱动的所有 Advisor 的超级接口。\n * 几乎涵盖除引介 Advisor 以外的全部 Advisor，\n * 引介 Advisor 不适用方法级匹配。\n *\n * @author Rod Johnson\n */",
        ),
        (
            "\t/**\n\t * Get the Pointcut that drives this advisor.\n\t */",
            "\t/**\n\t * 获取驱动本 Advisor 的切入点。\n\t */",
        ),
    ],
    "ProxyMethodInvocation.java": [
        (
            "/**\n * Extension of the AOP Alliance {@link org.aopalliance.intercept.MethodInvocation}\n * interface, allowing access to the proxy that the method invocation was made through.\n *\n * <p>Useful to be able to substitute return values with the proxy,\n * if necessary, for example if the invocation target returned itself.\n *\n * @author Juergen Hoeller\n * @author Adrian Colyer\n * @since 1.1.3\n * @see org.springframework.aop.framework.ReflectiveMethodInvocation\n * @see org.springframework.aop.support.DelegatingIntroductionInterceptor\n */",
            "/**\n * AOP Alliance {@link org.aopalliance.intercept.MethodInvocation} 接口的扩展，\n * 允许访问本次方法调用所经过的代理。\n *\n * <p>必要时可用代理替换返回值，\n * 例如调用目标返回自身时。\n *\n * @author Juergen Hoeller\n * @author Adrian Colyer\n * @since 1.1.3\n * @see org.springframework.aop.framework.ReflectiveMethodInvocation\n * @see org.springframework.aop.support.DelegatingIntroductionInterceptor\n */",
        ),
        (
            "\t/**\n\t * Return the proxy that this method invocation was made through.\n\t * @return the original proxy object\n\t */",
            "\t/**\n\t * 返回本次方法调用所经过的代理。\n\t * @return 原始代理对象\n\t */",
        ),
        (
            "\t/**\n\t * Create a clone of this object. If cloning is done before {@code proceed()}\n\t * is invoked on this object, {@code proceed()} can be invoked once per clone\n\t * to invoke the joinpoint (and the rest of the advice chain) more than once.\n\t * @return an invocable clone of this invocation.\n\t * {@code proceed()} can be called once per clone.\n\t */",
            "\t/**\n\t * 创建本对象的克隆。若在调用 {@code proceed()} 之前完成克隆，\n\t * 则每个克隆可各调用一次 {@code proceed()}，\n\t * 从而多次执行连接点（及后续通知链）。\n\t * @return 本调用的可执行克隆；每个克隆可调用一次 {@code proceed()}。\n\t */",
        ),
        (
            "\t/**\n\t * Create a clone of this object. If cloning is done before {@code proceed()}\n\t * is invoked on this object, {@code proceed()} can be invoked once per clone\n\t * to invoke the joinpoint (and the rest of the advice chain) more than once.\n\t * @param arguments the arguments that the cloned invocation is supposed to use,\n\t * overriding the original arguments\n\t * @return an invocable clone of this invocation.\n\t * {@code proceed()} can be called once per clone.\n\t */",
            "\t/**\n\t * 创建本对象的克隆。若在调用 {@code proceed()} 之前完成克隆，\n\t * 则每个克隆可各调用一次 {@code proceed()}，\n\t * 从而多次执行连接点（及后续通知链）。\n\t * @param arguments 克隆调用应使用的参数，覆盖原始参数\n\t * @return 本调用的可执行克隆；每个克隆可调用一次 {@code proceed()}。\n\t */",
        ),
        (
            "\t/**\n\t * Set the arguments to be used on subsequent invocations in any advice\n\t * in this chain.\n\t * @param arguments the argument array\n\t */",
            "\t/**\n\t * 设置本链中后续任意通知调用将使用的参数。\n\t * @param arguments 参数数组\n\t */",
        ),
        (
            "\t/**\n\t * Add the specified user attribute with the given value to this invocation.\n\t * <p>Such attributes are not used within the AOP framework itself. They are\n\t * just kept as part of the invocation object, for use in special interceptors.\n\t * @param key the name of the attribute\n\t * @param value the value of the attribute, or {@code null} to reset it\n\t */",
            "\t/**\n\t * 向本调用添加指定名称与值的用户属性。\n\t * <p>此类属性不在 AOP 框架内部使用，\n\t * 仅作为调用对象的一部分保留，供特殊拦截器使用。\n\t * @param key 属性名称\n\t * @param value 属性值，或 {@code null} 表示重置\n\t */",
        ),
        (
            "\t/**\n\t * Return the value of the specified user attribute.\n\t * @param key the name of the attribute\n\t * @return the value of the attribute, or {@code null} if not set\n\t * @see #setUserAttribute\n\t */",
            "\t/**\n\t * 返回指定用户属性的值。\n\t * @param key 属性名称\n\t * @return 属性值，未设置时返回 {@code null}\n\t * @see #setUserAttribute\n\t */",
        ),
    ],
    "RawTargetAccess.java": [
        (
            "/**\n * Marker for AOP proxy interfaces (in particular: introduction interfaces)\n * that explicitly intend to return the raw target object (which would normally\n * get replaced with the proxy object when returned from a method invocation).\n *\n * <p>Note that this is a marker interface in the style of {@link java.io.Serializable},\n * semantically applying to a declared interface rather than to the full class\n * of a concrete object. In other words, this marker applies to a particular\n * interface only (typically an introduction interface that does not serve\n * as the primary interface of an AOP proxy), and hence does not affect\n * other interfaces that a concrete AOP proxy may implement.\n *\n * @author Juergen Hoeller\n * @since 2.0.5\n * @see org.springframework.aop.scope.ScopedObject\n */",
            "/**\n * 标记 AOP 代理接口（尤其是引介接口），\n * 明确表示要返回原始目标对象\n * （方法调用返回时通常会被替换为代理对象）。\n *\n * <p>本接口是 {@link java.io.Serializable} 风格的标记接口，\n * 语义上作用于声明的接口，而非具体对象的完整类。\n * 换言之，该标记仅适用于特定接口\n * （通常是不作为 AOP 代理主接口的引介接口），\n * 因此不影响具体 AOP 代理可能实现的其他接口。\n *\n * @author Juergen Hoeller\n * @since 2.0.5\n * @see org.springframework.aop.scope.ScopedObject\n */",
        ),
    ],
    "SpringProxy.java": [
        (
            "/**\n * Marker interface implemented by all AOP proxies. Used to detect\n * whether objects are Spring-generated proxies.\n *\n * @author Rob Harrop\n * @since 2.0.1\n * @see org.springframework.aop.support.AopUtils#isAopProxy(Object)\n */",
            "/**\n * 所有 AOP 代理实现的标记接口。\n * 用于检测对象是否为 Spring 生成的代理。\n *\n * @author Rob Harrop\n * @since 2.0.1\n * @see org.springframework.aop.support.AopUtils#isAopProxy(Object)\n */",
        ),
    ],
    "TargetClassAware.java": [
        (
            "/**\n * Minimal interface for exposing the target class behind a proxy.\n *\n * <p>Implemented by AOP proxy objects and proxy factories\n * (via {@link org.springframework.aop.framework.Advised})\n * as well as by {@link TargetSource TargetSources}.\n *\n * @author Juergen Hoeller\n * @since 2.0.3\n * @see org.springframework.aop.support.AopUtils#getTargetClass(Object)\n */",
            "/**\n * 用于暴露代理背后目标类的最小接口。\n *\n * <p>由 AOP 代理对象与代理工厂\n * （通过 {@link org.springframework.aop.framework.Advised}）\n * 以及 {@link TargetSource TargetSources} 实现。\n *\n * @author Juergen Hoeller\n * @since 2.0.3\n * @see org.springframework.aop.support.AopUtils#getTargetClass(Object)\n */",
        ),
        (
            "\t/**\n\t * Return the target class behind the implementing object\n\t * (typically a proxy configuration or an actual proxy).\n\t * @return the target Class, or {@code null} if not known\n\t */",
            "\t/**\n\t * 返回实现对象背后的目标类\n\t * （通常是代理配置或实际代理）。\n\t * @return 目标 Class，未知时返回 {@code null}\n\t */",
        ),
    ],
    "TargetSource.java": [
        (
            "/**\n * A {@code TargetSource} is used to obtain the current \"target\" of\n * an AOP invocation, which will be invoked via reflection if no around\n * advice chooses to end the interceptor chain itself.\n *\n * <p>If a {@code TargetSource} is \"static\", it will always return\n * the same target, allowing optimizations in the AOP framework. Dynamic\n * target sources can support pooling, hot swapping, etc.\n *\n * <p>Application developers don't usually need to work with\n * {@code TargetSources} directly: this is an AOP framework interface.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n */",
            "/**\n * {@code TargetSource} 用于获取 AOP 调用的当前「目标」对象；\n * 若没有环绕通知自行终止拦截器链，\n * 该目标将通过反射被调用。\n *\n * <p>若 {@code TargetSource} 为「静态」，则始终返回同一目标，\n * 便于 AOP 框架优化。动态 TargetSource 可支持池化、热替换等。\n *\n * <p>应用开发者通常无需直接使用 {@code TargetSource}：\n * 这是 AOP 框架接口。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n */",
        ),
        (
            "\t/**\n\t * Return the type of targets returned by this {@link TargetSource}.\n\t * <p>Can return {@code null}, although certain usages of a {@code TargetSource}\n\t * might just work with a predetermined target class.\n\t * @return the type of targets returned by this {@link TargetSource}\n\t */",
            "\t/**\n\t * 返回本 {@link TargetSource} 所返回目标的类型。\n\t * <p>可返回 {@code null}，\n\t * 尽管某些 {@code TargetSource} 用法可能仅适用于预定目标类。\n\t * @return 本 {@link TargetSource} 所返回目标的类型\n\t */",
        ),
        (
            "\t/**\n\t * Will all calls to {@link #getTarget()} return the same object?\n\t * <p>In that case, there will be no need to invoke {@link #releaseTarget(Object)},\n\t * and the AOP framework can cache the return value of {@link #getTarget()}.\n\t * <p>The default implementation returns {@code false}.\n\t * @return {@code true} if the target is immutable\n\t * @see #getTarget\n\t */",
            "\t/**\n\t * 对 {@link #getTarget()} 的所有调用是否都返回同一对象？\n\t * <p>若是，则无需调用 {@link #releaseTarget(Object)}，\n\t * AOP 框架可缓存 {@link #getTarget()} 的返回值。\n\t * <p>默认实现返回 {@code false}。\n\t * @return 若目标不可变则返回 {@code true}\n\t * @see #getTarget\n\t */",
        ),
        (
            "\t/**\n\t * Return a target instance. Invoked immediately before the\n\t * AOP framework calls the \"target\" of an AOP method invocation.\n\t * @return the target object which contains the joinpoint,\n\t * or {@code null} if there is no actual target instance\n\t * @throws Exception if the target object can't be resolved\n\t */",
            "\t/**\n\t * 返回目标实例。在 AOP 框架调用 AOP 方法调用的「目标」之前立即调用。\n\t * @return 包含连接点的目标对象；若无实际目标实例则返回 {@code null}\n\t * @throws Exception 若无法解析目标对象\n\t */",
        ),
        (
            "\t/**\n\t * Release the given target object obtained from the\n\t * {@link #getTarget()} method, if any.\n\t * <p>The default implementation is empty.\n\t * @param target object obtained from a call to {@link #getTarget()}\n\t * @throws Exception if the object can't be released\n\t */",
            "\t/**\n\t * 释放通过 {@link #getTarget()} 获取的目标对象（若有）。\n\t * <p>默认实现为空。\n\t * @param target 调用 {@link #getTarget()} 获得的对象\n\t * @throws Exception 若无法释放该对象\n\t */",
        ),
    ],
}
