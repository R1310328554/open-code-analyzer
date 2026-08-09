"""Chinese JavaDoc replacements for springframework wave21b ReflectiveMethodInvocation [7]."""

REFLECTIVE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "ReflectiveMethodInvocation.java": [
        (
            "/**\n * Spring's implementation of the AOP Alliance\n * {@link org.aopalliance.intercept.MethodInvocation} interface,\n * implementing the extended\n * {@link org.springframework.aop.ProxyMethodInvocation} interface.\n *\n * <p>Invokes the target object using reflection. Subclasses can override the\n * {@link #invokeJoinpoint()} method to change this behavior, so this is also\n * a useful base class for more specialized MethodInvocation implementations.\n *\n * <p>It is possible to clone an invocation, to invoke {@link #proceed()}\n * repeatedly (once per clone), using the {@link #invocableClone()} method.\n * It is also possible to attach custom attributes to the invocation,\n * using the {@link #setUserAttribute} / {@link #getUserAttribute} methods.\n *\n * <p><b>NOTE:</b> This class is considered internal and should not be\n * directly accessed. The sole reason for it being public is compatibility\n * with existing framework integrations (for example, Pitchfork). For any other\n * purposes, use the {@link ProxyMethodInvocation} interface instead.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @author Adrian Colyer\n * @see #invokeJoinpoint\n * @see #proceed\n * @see #invocableClone\n * @see #setUserAttribute\n * @see #getUserAttribute\n */",
            "/**\n * Spring 对 AOP Alliance\n * {@link org.aopalliance.intercept.MethodInvocation} 接口的实现，\n * 同时实现扩展的 {@link org.springframework.aop.ProxyMethodInvocation} 接口。\n *\n * <p>通过反射调用目标对象。子类可覆盖 {@link #invokeJoinpoint()} 方法\n * 改变此行为，因此也是更专用 MethodInvocation 实现的有用基类。\n *\n * <p>可使用 {@link #invocableClone()} 克隆调用，\n * 对每个克隆重复调用 {@link #proceed()}。\n * 也可通过 {@link #setUserAttribute} / {@link #getUserAttribute}\n * 为调用附加自定义属性。\n *\n * <p><b>注意：</b> 本类视为内部类，不应直接访问。\n * 公开的唯一原因是与现有框架集成（如 Pitchfork）兼容。\n * 其他用途请使用 {@link ProxyMethodInvocation} 接口。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @author Adrian Colyer\n * @see #invokeJoinpoint\n * @see #proceed\n * @see #invocableClone\n * @see #setUserAttribute\n * @see #getUserAttribute\n */",
        ),
        (
            "\t/**\n\t * Lazily initialized map of user-specific attributes for this invocation.\n\t */",
            "\t/**\n\t * 本调用用户特定属性的延迟初始化映射。\n\t */",
        ),
        (
            "\t/**\n\t * List of MethodInterceptor and InterceptorAndDynamicMethodMatcher\n\t * that need dynamic checks.\n\t */",
            "\t/**\n\t * 需要动态检查的 MethodInterceptor 与 InterceptorAndDynamicMethodMatcher 列表。\n\t */",
        ),
        (
            "\t/**\n\t * Index from 0 of the current interceptor we're invoking.\n\t * -1 until we invoke: then the current interceptor.\n\t */",
            "\t/**\n\t * 当前正在调用的拦截器索引（从 0 起）。\n\t * 调用前为 -1；调用后为当前拦截器索引。\n\t */",
        ),
        (
            "\t/**\n\t * Construct a new ReflectiveMethodInvocation with the given arguments.\n\t * @param proxy the proxy object that the invocation was made on\n\t * @param target the target object to invoke\n\t * @param method the method to invoke\n\t * @param arguments the arguments to invoke the method with\n\t * @param targetClass the target class, for MethodMatcher invocations\n\t * @param interceptorsAndDynamicMethodMatchers interceptors that should be applied,\n\t * along with any InterceptorAndDynamicMethodMatchers that need evaluation at runtime.\n\t * MethodMatchers included in this struct must already have been found to have matched\n\t * as far as was possibly statically. Passing an array might be about 10% faster,\n\t * but would complicate the code. And it would work only for static pointcuts.\n\t */",
            "\t/**\n\t * 以给定参数构造新的 ReflectiveMethodInvocation。\n\t * @param proxy 发起调用的代理对象\n\t * @param target 要调用的目标对象\n\t * @param method 要调用的方法\n\t * @param arguments 方法调用参数\n\t * @param targetClass 目标类，用于 MethodMatcher 调用\n\t * @param interceptorsAndDynamicMethodMatchers 应应用的拦截器，\n\t * 以及运行时需评估的 InterceptorAndDynamicMethodMatcher。\n\t * 此结构中包含的 MethodMatcher 必须已在静态范围内尽可能完成匹配。\n\t * 传递数组可能快约 10%，但会增加代码复杂度，且仅适用于静态切入点。\n\t */",
        ),
        (
            "\t/**\n\t * Return the method invoked on the proxied interface.\n\t * May or may not correspond with a method invoked on an underlying\n\t * implementation of that interface.\n\t */",
            "\t/**\n\t * 返回在代理接口上调用的方法。\n\t * 可能与底层实现类上调用的方法不一致。\n\t */",
        ),
        (
            "\t\t// We start with an index of -1 and increment early.",
            "\t\t// 从索引 -1 开始，并提前递增。",
        ),
        (
            "\t\t\t// Evaluate dynamic method matcher here: static part will already have\n\t\t\t// been evaluated and found to match.",
            "\t\t\t// 在此评估动态方法匹配器：静态部分已评估并确认匹配。",
        ),
        (
            "\t\t\t\t// Dynamic matching failed.\n\t\t\t\t// Skip this interceptor and invoke the next in the chain.",
            "\t\t\t\t// 动态匹配失败。\n\t\t\t\t// 跳过本拦截器，调用链中下一个。",
        ),
        (
            "\t\t\t// It's an interceptor, so we just invoke it: The pointcut will have\n\t\t\t// been evaluated statically before this object was constructed.",
            "\t\t\t// 为拦截器，直接调用：切入点在本对象构造前已静态评估。",
        ),
        (
            "\t/**\n\t * Invoke the joinpoint using reflection.\n\t * Subclasses can override this to use custom invocation.\n\t * @return the return value of the joinpoint\n\t * @throws Throwable if invoking the joinpoint resulted in an exception\n\t */",
            "\t/**\n\t * 通过反射调用连接点。\n\t * 子类可覆盖以使用自定义调用方式。\n\t * @return 连接点返回值\n\t * @throws Throwable 若调用连接点导致异常\n\t */",
        ),
        (
            "\t/**\n\t * This implementation returns a shallow copy of this invocation object,\n\t * including an independent copy of the original arguments array.\n\t * <p>We want a shallow copy in this case: We want to use the same interceptor\n\t * chain and other object references, but we want an independent value for the\n\t * current interceptor index.\n\t * @see java.lang.Object#clone()\n\t */",
            "\t/**\n\t * 本实现返回本调用对象的浅拷贝，\n\t * 包含原始参数数组的独立副本。\n\t * <p>此处需要浅拷贝：使用相同拦截器链及其他对象引用，\n\t * 但当前拦截器索引须为独立值。\n\t * @see java.lang.Object#clone()\n\t */",
        ),
        (
            "\t\t\t// Build an independent copy of the arguments array.",
            "\t\t\t// 构建参数数组的独立副本。",
        ),
        (
            "\t/**\n\t * This implementation returns a shallow copy of this invocation object,\n\t * using the given arguments array for the clone.\n\t * <p>We want a shallow copy in this case: We want to use the same interceptor\n\t * chain and other object references, but we want an independent value for the\n\t * current interceptor index.\n\t * @see java.lang.Object#clone()\n\t */",
            "\t/**\n\t * 本实现返回本调用对象的浅拷贝，\n\t * 克隆使用给定参数数组。\n\t * <p>此处需要浅拷贝：使用相同拦截器链及其他对象引用，\n\t * 但当前拦截器索引须为独立值。\n\t * @see java.lang.Object#clone()\n\t */",
        ),
        (
            "\t\t// Force initialization of the user attributes Map,\n\t\t// for having a shared Map reference in the clone.",
            "\t\t// 强制初始化用户属性 Map，\n\t\t// 以便克隆共享 Map 引用。",
        ),
        (
            "\t\t// Create the MethodInvocation clone.",
            "\t\t// 创建 MethodInvocation 克隆。",
        ),
        (
            "\t/**\n\t * Return user attributes associated with this invocation.\n\t * This method provides an invocation-bound alternative to a ThreadLocal.\n\t * <p>This map is initialized lazily and is not used in the AOP framework itself.\n\t * @return any user attributes associated with this invocation\n\t * (never {@code null})\n\t */",
            "\t/**\n\t * 返回与本调用关联的用户属性。\n\t * 本方法提供调用绑定的 ThreadLocal 替代方案。\n\t * <p>此 Map 延迟初始化，AOP 框架本身不使用。\n\t * @return 与本调用关联的用户属性（永不为 {@code null}）\n\t */",
        ),
        (
            "\t\t// Don't do toString on target, it may be proxied.",
            "\t\t// 不对 target 做 toString，它可能被代理。",
        ),
    ],
}
