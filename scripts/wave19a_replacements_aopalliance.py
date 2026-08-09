"""Chinese JavaDoc replacements for springframework wave19a AOP Alliance intercept [0:6]."""

AOPALLIANCE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "ConstructorInvocation.java": [
        (
            "/**\n * Description of an invocation to a constructor, given to an\n * interceptor upon constructor-call.\n *\n * <p>A constructor invocation is a joinpoint and can be intercepted\n * by a constructor interceptor.\n *\n * @author Rod Johnson\n * @see ConstructorInterceptor\n */",
            "/**\n * 构造函数调用的描述，在构造函数被调用时传递给拦截器。\n *\n * <p>构造函数调用是一种连接点，可由构造函数拦截器拦截。\n *\n * @author Rod Johnson\n * @see ConstructorInterceptor\n */",
        ),
        (
            "\t/**\n\t * Get the constructor being called.\n\t * <p>This method is a friendly implementation of the\n\t * {@link Joinpoint#getStaticPart()} method (same result).\n\t * @return the constructor being called\n\t */",
            "\t/**\n\t * 获取被调用的构造函数。\n\t * <p>该方法是 {@link Joinpoint#getStaticPart()} 的友好封装（结果相同）。\n\t * @return 被调用的构造函数\n\t */",
        ),
    ],
    "Interceptor.java": [
        (
            "/**\n * This interface represents a generic interceptor.\n *\n * <p>A generic interceptor can intercept runtime events that occur\n * within a base program. Those events are materialized by (reified\n * in) joinpoints. Runtime joinpoints can be invocations, field\n * access, exceptions...\n *\n * <p>This interface is not used directly. Use the sub-interfaces\n * to intercept specific events. For instance, the following class\n * implements some specific interceptors in order to implement a\n * debugger:\n *\n * <pre class=code>\n * class DebuggingInterceptor implements MethodInterceptor,\n *     ConstructorInterceptor {\n *\n *   Object invoke(MethodInvocation i) throws Throwable {\n *     debug(i.getMethod(), i.getThis(), i.getArgs());\n *     return i.proceed();\n *   }\n *\n *   Object construct(ConstructorInvocation i) throws Throwable {\n *     debug(i.getConstructor(), i.getThis(), i.getArgs());\n *     return i.proceed();\n *   }\n *\n *   void debug(AccessibleObject ao, Object this, Object value) {\n *     ...\n *   }\n * }\n * </pre>\n *\n * @author Rod Johnson\n * @see Joinpoint\n */",
            "/**\n * 表示通用拦截器的接口。\n *\n * <p>通用拦截器可拦截基本程序中发生的运行时事件。\n * 这些事件由连接点具体化（reify）。运行时连接点可以是调用、字段访问、异常等。\n *\n * <p>本接口不直接使用。请通过子接口拦截特定事件。例如，\n * 以下类实现若干特定拦截器以构建调试器：\n *\n * <pre class=code>\n * class DebuggingInterceptor implements MethodInterceptor,\n *     ConstructorInterceptor {\n *\n *   Object invoke(MethodInvocation i) throws Throwable {\n *     debug(i.getMethod(), i.getThis(), i.getArgs());\n *     return i.proceed();\n *   }\n *\n *   Object construct(ConstructorInvocation i) throws Throwable {\n *     debug(i.getConstructor(), i.getThis(), i.getArgs());\n *     return i.proceed();\n *   }\n *\n *   void debug(AccessibleObject ao, Object this, Object value) {\n *     ...\n *   }\n * }\n * </pre>\n *\n * @author Rod Johnson\n * @see Joinpoint\n */",
        ),
    ],
    "Invocation.java": [
        (
            "/**\n * This interface represents an invocation in the program.\n *\n * <p>An invocation is a joinpoint and can be intercepted by an\n * interceptor.\n *\n * @author Rod Johnson\n */",
            "/**\n * 表示程序中一次调用的接口。\n *\n * <p>调用是一种连接点，可由拦截器拦截。\n *\n * @author Rod Johnson\n */",
        ),
        (
            "\t/**\n\t * Get the arguments as an array object.\n\t * It is possible to change element values within this\n\t * array to change the arguments.\n\t * @return the argument of the invocation\n\t */",
            "\t/**\n\t * 以数组形式获取参数。\n\t * 可修改数组中的元素值以改变实际参数。\n\t * @return 调用的参数\n\t */",
        ),
    ],
    "Joinpoint.java": [
        (
            "/**\n * This interface represents a generic runtime joinpoint (in the AOP\n * terminology).\n *\n * <p>A runtime joinpoint is an <i>event</i> that occurs on a static\n * joinpoint (i.e. a location in a program). For instance, an\n * invocation is the runtime joinpoint on a method (static joinpoint).\n * The static part of a given joinpoint can be generically retrieved\n * using the {@link #getStaticPart()} method.\n *\n * <p>In the context of an interception framework, a runtime joinpoint\n * is then the reification of an access to an accessible object (a\n * method, a constructor, a field), i.e. the static part of the\n * joinpoint. It is passed to the interceptors that are installed on\n * the static joinpoint.\n *\n * @author Rod Johnson\n * @see Interceptor\n */",
            "/**\n * 表示通用运行时连接点的接口（AOP 术语）。\n *\n * <p>运行时连接点是发生在静态连接点（即程序中的某个位置）上的<i>事件</i>。\n * 例如，调用是方法上的运行时连接点（静态连接点）。\n * 给定连接点的静态部分可通过 {@link #getStaticPart()} 方法获取。\n *\n * <p>在拦截框架的上下文中，运行时连接点是对可访问对象\n * （方法、构造函数、字段）访问的具体化，即连接点的静态部分。\n * 它会传递给安装在静态连接点上的拦截器。\n *\n * @author Rod Johnson\n * @see Interceptor\n */",
        ),
        (
            "\t/**\n\t * Proceed to the next interceptor in the chain.\n\t * <p>The implementation and the semantics of this method depends\n\t * on the actual joinpoint type (see the children interfaces).\n\t * @return see the children interfaces' proceed definition\n\t * @throws Throwable if the joinpoint throws an exception\n\t */",
            "\t/**\n\t * 继续执行拦截器链中的下一个拦截器。\n\t * <p>本方法的实现与语义取决于实际连接点类型（见子接口）。\n\t * @return 见各子接口对 proceed 的定义\n\t * @throws Throwable 若连接点抛出异常\n\t */",
        ),
        (
            "\t/**\n\t * Return the object that holds the current joinpoint's static part.\n\t * <p>For instance, the target object for an invocation.\n\t * @return the object (can be null if the accessible object is static)\n\t */",
            "\t/**\n\t * 返回持有当前连接点静态部分的对象。\n\t * <p>例如，调用场景下的目标对象。\n\t * @return 对象（若可访问对象为 static 则可能为 null）\n\t */",
        ),
        (
            "\t/**\n\t * Return the static part of this joinpoint.\n\t * <p>The static part is an accessible object on which a chain of\n\t * interceptors is installed.\n\t */",
            "\t/**\n\t * 返回本连接点的静态部分。\n\t * <p>静态部分是可访问对象，其上安装了一串拦截器。\n\t */",
        ),
    ],
    "MethodInterceptor.java": [
        (
            "/**\n * Intercepts calls on an interface on its way to the target. These\n * are nested \"on top\" of the target.\n *\n * <p>The user should implement the {@link #invoke(MethodInvocation)}\n * method to modify the original behavior. For example, the following class\n * implements a tracing interceptor (traces all the calls on the\n * intercepted method(s)):\n *\n * <pre class=code>\n * class TracingInterceptor implements MethodInterceptor {\n *   Object invoke(MethodInvocation i) throws Throwable {\n *     System.out.println(\"method \"+i.getMethod()+\" is called on \"+\n *                        i.getThis()+\" with args \"+i.getArguments());\n *     Object ret=i.proceed();\n *     System.out.println(\"method \"+i.getMethod()+\" returns \"+ret);\n *     return ret;\n *   }\n * }\n * </pre>\n *\n * @author Rod Johnson\n */",
            "/**\n * 拦截通往目标对象途中对接口方法的调用，拦截器嵌套在目标之上。\n *\n * <p>用户应实现 {@link #invoke(MethodInvocation)} 以修改原始行为。\n * 例如，以下类实现跟踪拦截器（跟踪被拦截方法上的所有调用）：\n *\n * <pre class=code>\n * class TracingInterceptor implements MethodInterceptor {\n *   Object invoke(MethodInvocation i) throws Throwable {\n *     System.out.println(\"method \"+i.getMethod()+\" is called on \"+\n *                        i.getThis()+\" with args \"+i.getArguments());\n *     Object ret=i.proceed();\n *     System.out.println(\"method \"+i.getMethod()+\" returns \"+ret);\n *     return ret;\n *   }\n * }\n * </pre>\n *\n * @author Rod Johnson\n */",
        ),
        (
            "\t/**\n\t * Implement this method to perform extra treatments before and\n\t * after the invocation. Polite implementations would certainly\n\t * like to invoke {@link Joinpoint#proceed()}.\n\t * @param invocation the method invocation joinpoint\n\t * @return the result of the call to {@link Joinpoint#proceed()};\n\t * might be intercepted by the interceptor\n\t * @throws Throwable if the interceptors or the target object\n\t * throws an exception\n\t */",
            "\t/**\n\t * 实现本方法以在调用前后执行额外处理。\n\t * 规范的实现应调用 {@link Joinpoint#proceed()}。\n\t * @param invocation 方法调用连接点\n\t * @return 调用 {@link Joinpoint#proceed()} 的结果；可能被拦截器改写\n\t * @throws Throwable 若拦截器或目标对象抛出异常\n\t */",
        ),
    ],
    "MethodInvocation.java": [
        (
            "/**\n * Description of an invocation to a method, given to an interceptor\n * upon method-call.\n *\n * <p>A method invocation is a joinpoint and can be intercepted by a\n * method interceptor.\n *\n * @author Rod Johnson\n * @see MethodInterceptor\n */",
            "/**\n * 方法调用的描述，在方法被调用时传递给拦截器。\n *\n * <p>方法调用是一种连接点，可由方法拦截器拦截。\n *\n * @author Rod Johnson\n * @see MethodInterceptor\n */",
        ),
        (
            "\t/**\n\t * Get the method being called.\n\t * <p>This method is a friendly implementation of the\n\t * {@link Joinpoint#getStaticPart()} method (same result).\n\t * @return the method being called\n\t */",
            "\t/**\n\t * 获取被调用的方法。\n\t * <p>该方法是 {@link Joinpoint#getStaticPart()} 的友好封装（结果相同）。\n\t * @return 被调用的方法\n\t */",
        ),
    ],
}
