"""Chinese JavaDoc replacements for springframework wave18b aopalliance [17:20]."""

AOPALLIANCE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "Advice.java": [
        (
            "/**\n * Tag interface for Advice. Implementations can be any type\n * of advice, such as Interceptors.\n *\n * @author Rod Johnson\n * @version $Id: Advice.java,v 1.1 2004/03/19 17:02:16 johnsonr Exp $\n */",
            "/**\n * Advice 的标记接口。实现可以是任意类型的通知，\n * 例如 Interceptor。\n *\n * @author Rod Johnson\n * @version $Id: Advice.java,v 1.1 2004/03/19 17:02:16 johnsonr Exp $\n */",
        ),
    ],
    "AspectException.java": [
        (
            "/**\n * Superclass for all AOP infrastructure exceptions.\n * Unchecked, as such exceptions are fatal and end user\n * code shouldn't be forced to catch them.\n *\n * @author Rod Johnson\n * @author Bob Lee\n * @author Juergen Hoeller\n */",
            "/**\n * 所有 AOP 基础设施异常的父类。\n * 为非受检异常，因为此类异常是致命的，\n * 不应强制最终用户代码捕获。\n *\n * @author Rod Johnson\n * @author Bob Lee\n * @author Juergen Hoeller\n */",
        ),
        (
            "\t/**\n\t * Constructor for AspectException.\n\t * @param message the exception message\n\t */",
            "\t/**\n\t * AspectException 构造函数。\n\t * @param message 异常消息\n\t */",
        ),
        (
            "\t/**\n\t * Constructor for AspectException.\n\t * @param message the exception message\n\t * @param cause the root cause, if any\n\t */",
            "\t/**\n\t * AspectException 构造函数。\n\t * @param message 异常消息\n\t * @param cause 根因（若有）\n\t */",
        ),
    ],
    "ConstructorInterceptor.java": [
        (
            "/**\n * Intercepts the construction of a new object.\n *\n * <p>The user should implement the {@link\n * #construct(ConstructorInvocation)} method to modify the original\n * behavior. For example, the following class implements a singleton\n * interceptor (allows only one unique instance for the intercepted\n * class):\n *\n * <pre class=code>\n * class DebuggingInterceptor implements ConstructorInterceptor {\n *   Object instance=null;\n *\n *   Object construct(ConstructorInvocation i) throws Throwable {\n *     if(instance==null) {\n *       return instance=i.proceed();\n *     } else {\n *       throw new Exception(\"singleton does not allow multiple instance\");\n *     }\n *   }\n * }\n * </pre>\n *\n * @author Rod Johnson\n */",
            "/**\n * 拦截新对象的构造过程。\n *\n * <p>用户应实现 {@link #construct(ConstructorInvocation)} 方法\n * 以修改原始行为。例如，以下类实现单例拦截器\n * （被拦截类仅允许一个唯一实例）：\n *\n * <pre class=code>\n * class DebuggingInterceptor implements ConstructorInterceptor {\n *   Object instance=null;\n *\n *   Object construct(ConstructorInvocation i) throws Throwable {\n *     if(instance==null) {\n *       return instance=i.proceed();\n *     } else {\n *       throw new Exception(\"singleton does not allow multiple instance\");\n *     }\n *   }\n * }\n * </pre>\n *\n * @author Rod Johnson\n */",
        ),
        (
            "\t/**\n\t * Implement this method to perform extra treatments before and\n\t * after the construction of a new object. Polite implementations\n\t * would certainly like to invoke {@link Joinpoint#proceed()}.\n\t * @param invocation the construction joinpoint\n\t * @return the newly created object, which is also the result of\n\t * the call to {@link Joinpoint#proceed()}; might be replaced by\n\t * the interceptor\n\t * @throws Throwable if the interceptors or the target object\n\t * throws an exception\n\t */",
            "\t/**\n\t * 实现此方法以在新对象构造前后执行额外处理。\n\t * 规范的实现通常会调用 {@link Joinpoint#proceed()}。\n\t * @param invocation 构造连接点\n\t * @return 新创建的对象，也是调用 {@link Joinpoint#proceed()} 的结果；\n\t * 拦截器可替换该对象\n\t * @throws Throwable 若拦截器或目标对象抛出异常\n\t */",
        ),
    ],
}
