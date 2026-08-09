"""Chinese JavaDoc replacements for springframework wave19b aop [7:10]."""

AOP_B_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "ThrowsAdvice.java": [
        (
            "/**\n * Tag interface for throws advice.\n *\n * <p>There are not any methods on this interface, as methods are invoked by\n * reflection. Implementing classes must implement methods of the form:\n *\n * <pre class=\"code\">void afterThrowing([Method, args, target], ThrowableSubclass);</pre>\n *\n * <p>Some examples of valid methods would be:\n *\n * <pre class=\"code\">public void afterThrowing(Exception ex)</pre>\n * <pre class=\"code\">public void afterThrowing(RemoteException ex)</pre>\n * <pre class=\"code\">public void afterThrowing(Method method, Object[] args, Object target, Exception ex)</pre>\n * <pre class=\"code\">public void afterThrowing(Method method, Object[] args, Object target, ServletException ex)</pre>\n *\n * <p>The first three arguments are optional, and only useful if we want further\n * information about the joinpoint, as in AspectJ <b>after-throwing</b> advice.\n *\n * <p><b>Note:</b> If a throws-advice method throws an exception itself, it will\n * override the original exception (i.e. change the exception thrown to the user).\n * The overriding exception will typically be a RuntimeException; this is compatible\n * with any method signature. However, if a throws-advice method throws a checked\n * exception, it will have to match the declared exceptions of the target method\n * and is hence to some degree coupled to specific target method signatures.\n * <b>Do not throw an undeclared checked exception that is incompatible with\n * the target method's signature!</b>\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @see AfterReturningAdvice\n * @see MethodBeforeAdvice\n */",
            "/**\n * 异常抛出通知的标记接口。\n *\n * <p>本接口不含任何方法，方法通过反射调用。\n * 实现类须实现如下形式的方法：\n *\n * <pre class=\"code\">void afterThrowing([Method, args, target], ThrowableSubclass);</pre>\n *\n * <p>有效方法示例：\n *\n * <pre class=\"code\">public void afterThrowing(Exception ex)</pre>\n * <pre class=\"code\">public void afterThrowing(RemoteException ex)</pre>\n * <pre class=\"code\">public void afterThrowing(Method method, Object[] args, Object target, Exception ex)</pre>\n * <pre class=\"code\">public void afterThrowing(Method method, Object[] args, Object target, ServletException ex)</pre>\n *\n * <p>前三个参数可选，仅在需要连接点额外信息时有用，\n * 类似 AspectJ 的 <b>after-throwing</b> 通知。\n *\n * <p><b>注意：</b>若 throws-advice 方法自身抛出异常，\n * 将覆盖原始异常（即改变抛给调用者的异常）。\n * 覆盖异常通常为 RuntimeException，与任意方法签名兼容。\n * 但若 throws-advice 方法抛出受检异常，\n * 则须与目标方法声明的异常匹配，\n * 因此在一定程度上与特定目标方法签名耦合。\n * <b>切勿抛出与目标方法签名不兼容的未声明受检异常！</b>\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @see AfterReturningAdvice\n * @see MethodBeforeAdvice\n */",
        ),
    ],
    "TrueClassFilter.java": [
        (
            "/**\n * Canonical ClassFilter instance that matches all classes.\n *\n * @author Rod Johnson\n */",
            "/**\n * 匹配所有类的规范 ClassFilter 实例。\n *\n * @author Rod Johnson\n */",
        ),
        (
            "\t/**\n\t * Enforce Singleton pattern.\n\t */",
            "\t/**\n\t * 强制单例模式。\n\t */",
        ),
        (
            "\t/**\n\t * Required to support serialization. Replaces with canonical\n\t * instance on deserialization, protecting Singleton pattern.\n\t * Alternative to overriding {@code equals()}.\n\t */",
            "\t/**\n\t * 支持序列化所需。反序列化时替换为规范实例，\n\t * 保护单例模式。可替代重写 {@code equals()}。\n\t */",
        ),
    ],
    "TrueMethodMatcher.java": [
        (
            "/**\n * Canonical MethodMatcher instance that matches all methods.\n *\n * @author Rod Johnson\n */",
            "/**\n * 匹配所有方法的规范 MethodMatcher 实例。\n *\n * @author Rod Johnson\n */",
        ),
        (
            "\t/**\n\t * Enforce Singleton pattern.\n\t */",
            "\t/**\n\t * 强制单例模式。\n\t */",
        ),
        (
            "\t\t// Should never be invoked as isRuntime returns false.",
            "\t\t// isRuntime 返回 false，不应调用此方法。",
        ),
        (
            "\t/**\n\t * Required to support serialization. Replaces with canonical\n\t * instance on deserialization, protecting Singleton pattern.\n\t * Alternative to overriding {@code equals()}.\n\t */",
            "\t/**\n\t * 支持序列化所需。反序列化时替换为规范实例，\n\t * 保护单例模式。可替代重写 {@code equals()}。\n\t */",
        ),
    ],
    "TruePointcut.java": [
        (
            "/**\n * Canonical Pointcut instance that always matches.\n *\n * @author Rod Johnson\n */",
            "/**\n * 始终匹配的规范 Pointcut 实例。\n *\n * @author Rod Johnson\n */",
        ),
        (
            "\t/**\n\t * Enforce Singleton pattern.\n\t */",
            "\t/**\n\t * 强制单例模式。\n\t */",
        ),
        (
            "\t/**\n\t * Required to support serialization. Replaces with canonical\n\t * instance on deserialization, protecting Singleton pattern.\n\t * Alternative to overriding {@code equals()}.\n\t */",
            "\t/**\n\t * 支持序列化所需。反序列化时替换为规范实例，\n\t * 保护单例模式。可替代重写 {@code equals()}。\n\t */",
        ),
    ],
}
