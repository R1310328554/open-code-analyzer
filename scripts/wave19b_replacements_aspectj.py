"""Chinese JavaDoc replacements for springframework wave19b aspectj [11:20]."""

ASPECTJ_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "AspectInstanceFactory.java": [
        (
            "/**\n * Interface implemented to provide an instance of an AspectJ aspect.\n * Decouples from Spring's bean factory.\n *\n * <p>Extends the {@link org.springframework.core.Ordered} interface\n * to express an order value for the underlying aspect in a chain.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @since 2.0\n * @see org.springframework.beans.factory.BeanFactory#getBean\n */",
            "/**\n * 提供 AspectJ 切面实例的接口。\n * 与 Spring Bean 工厂解耦。\n *\n * <p>扩展 {@link org.springframework.core.Ordered} 接口，\n * 为链中的底层切面表达顺序值。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @since 2.0\n * @see org.springframework.beans.factory.BeanFactory#getBean\n */",
        ),
        (
            "\t/**\n\t * Create an instance of this factory's aspect.\n\t * @return the aspect instance (never {@code null})\n\t */",
            "\t/**\n\t * 创建本工厂切面的一个实例。\n\t * @return 切面实例（永不为 {@code null}）\n\t */",
        ),
        (
            "\t/**\n\t * Expose the aspect class loader that this factory uses.\n\t * @return the aspect class loader (or {@code null} for the bootstrap loader)\n\t * @see org.springframework.util.ClassUtils#getDefaultClassLoader()\n\t */",
            "\t/**\n\t * 暴露本工厂使用的切面类加载器。\n\t * @return 切面类加载器（引导类加载器时为 {@code null}）\n\t * @see org.springframework.util.ClassUtils#getDefaultClassLoader()\n\t */",
        ),
    ],
    "AspectJAfterAdvice.java": [
        (
            "/**\n * Spring AOP advice wrapping an AspectJ after advice method.\n *\n * @author Rod Johnson\n * @since 2.0\n */",
            "/**\n * 封装 AspectJ after 通知方法的 Spring AOP 通知。\n * 在连接点执行完毕后于 {@code finally} 块中调用切面方法。\n *\n * @author Rod Johnson\n * @since 2.0\n */",
        ),
    ],
    "AspectJAfterReturningAdvice.java": [
        (
            "/**\n * Spring AOP advice wrapping an AspectJ after-returning advice method.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @author Ramnivas Laddad\n * @since 2.0\n */",
            "/**\n * 封装 AspectJ after-returning 通知方法的 Spring AOP 通知。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @author Ramnivas Laddad\n * @since 2.0\n */",
        ),
        (
            "\t/**\n\t * Following AspectJ semantics, if a returning clause was specified, then the\n\t * advice is only invoked if the returned value is an instance of the given\n\t * returning type and generic type parameters, if any, match the assignment\n\t * rules. If the returning type is Object, the advice is *always* invoked.\n\t * @param returnValue the return value of the target method\n\t * @return whether to invoke the advice method for the given return value\n\t */",
            "\t/**\n\t * 按 AspectJ 语义，若指定了 returning 子句，\n\t * 则仅当返回值是给定返回类型的实例，\n\t * 且泛型类型参数（若有）符合赋值规则时才调用通知。\n\t * 若返回类型为 Object，则<b>始终</b>调用通知。\n\t * @param returnValue 目标方法的返回值\n\t * @return 是否针对该返回值调用通知方法\n\t */",
        ),
        (
            "\t\t// If we aren't dealing with a raw type, check if generic parameters are assignable.",
            "\t\t// 若非原始类型，检查泛型参数是否可赋值。",
        ),
        (
            "\t/**\n\t * Following AspectJ semantics, if a return value is null (or return type is void),\n\t * then the return type of target method should be used to determine whether advice\n\t * is invoked or not. Also, even if the return type is void, if the type of argument\n\t * declared in the advice method is Object, then the advice must still get invoked.\n\t * @param type the type of argument declared in advice method\n\t * @param method the advice method\n\t * @param returnValue the return value of the target method\n\t * @return whether to invoke the advice method for the given return value and type\n\t */",
            "\t/**\n\t * 按 AspectJ 语义，若返回值为 null（或返回类型为 void），\n\t * 应使用目标方法的返回类型判断是否调用通知。\n\t * 此外，即使返回类型为 void，\n\t * 若通知方法中声明的参数类型为 Object，仍须调用通知。\n\t * @param type 通知方法中声明的参数类型\n\t * @param method 通知方法\n\t * @param returnValue 目标方法的返回值\n\t * @return 是否针对该返回值与类型调用通知方法\n\t */",
        ),
    ],
    "AspectJAfterThrowingAdvice.java": [
        (
            "/**\n * Spring AOP advice wrapping an AspectJ after-throwing advice method.\n *\n * @author Rod Johnson\n * @since 2.0\n */",
            "/**\n * 封装 AspectJ after-throwing 通知方法的 Spring AOP 通知。\n *\n * @author Rod Johnson\n * @since 2.0\n */",
        ),
        (
            "\t/**\n\t * In AspectJ semantics, after throwing advice that specifies a throwing clause\n\t * is only invoked if the thrown exception is a subtype of the given throwing type.\n\t */",
            "\t/**\n\t * 按 AspectJ 语义，指定了 throwing 子句的 after-throwing 通知\n\t * 仅当抛出的异常是给定 throwing 类型的子类型时才调用。\n\t */",
        ),
    ],
    "AspectJAopUtils.java": [
        (
            "/**\n * Utility methods for dealing with AspectJ advisors.\n *\n * @author Adrian Colyer\n * @author Juergen Hoeller\n * @since 2.0\n */",
            "/**\n * 处理 AspectJ Advisor 的工具方法。\n *\n * @author Adrian Colyer\n * @author Juergen Hoeller\n * @since 2.0\n */",
        ),
        (
            "\t/**\n\t * Return {@code true} if the advisor is a form of before advice.\n\t */",
            "\t/**\n\t * 若 Advisor 属于 before 通知形式则返回 {@code true}。\n\t */",
        ),
        (
            "\t/**\n\t * Return {@code true} if the advisor is a form of after advice.\n\t */",
            "\t/**\n\t * 若 Advisor 属于 after 通知形式则返回 {@code true}。\n\t */",
        ),
        (
            "\t/**\n\t * Return the AspectJPrecedenceInformation provided by this advisor or its advice.\n\t * If neither the advisor nor the advice have precedence information, this method\n\t * will return {@code null}.\n\t */",
            "\t/**\n\t * 返回本 Advisor 或其 Advice 提供的 AspectJPrecedenceInformation。\n\t * 若 Advisor 与 Advice 均无优先级信息，则返回 {@code null}。\n\t */",
        ),
    ],
    "AspectJAroundAdvice.java": [
        (
            "/**\n * Spring AOP around advice (MethodInterceptor) that wraps\n * an AspectJ advice method. Exposes ProceedingJoinPoint.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @since 2.0\n */",
            "/**\n * 封装 AspectJ 通知方法的 Spring AOP 环绕通知（MethodInterceptor）。\n * 暴露 ProceedingJoinPoint。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @since 2.0\n */",
        ),
        (
            "\t/**\n\t * Return the ProceedingJoinPoint for the current invocation,\n\t * instantiating it lazily if it hasn't been bound to the thread already.\n\t * @param rmi the current Spring AOP ReflectiveMethodInvocation,\n\t * which we'll use for attribute binding\n\t * @return the ProceedingJoinPoint to make available to advice methods\n\t */",
            "\t/**\n\t * 返回当前调用的 ProceedingJoinPoint；\n\t * 若尚未绑定到线程则延迟实例化。\n\t * @param rmi 当前 Spring AOP ReflectiveMethodInvocation，用于属性绑定\n\t * @return 供通知方法使用的 ProceedingJoinPoint\n\t */",
        ),
    ],
    "AspectJExpressionPointcutAdvisor.java": [
        (
            "/**\n * Spring AOP Advisor that can be used for any AspectJ pointcut expression.\n *\n * @author Rob Harrop\n * @since 2.0\n */",
            "/**\n * 可用于任意 AspectJ 切入点表达式的 Spring AOP Advisor。\n *\n * @author Rob Harrop\n * @since 2.0\n */",
        ),
    ],
    "AspectJMethodBeforeAdvice.java": [
        (
            "/**\n * Spring AOP advice that wraps an AspectJ before method.\n *\n * @author Rod Johnson\n * @author Adrian Colyer\n * @since 2.0\n */",
            "/**\n * 封装 AspectJ before 方法的 Spring AOP 通知。\n * 在目标方法执行之前调用切面中的前置通知方法。\n *\n * @author Rod Johnson\n * @author Adrian Colyer\n * @since 2.0\n */",
        ),
    ],
    "AspectJPointcutAdvisor.java": [
        (
            "/**\n * {@code AspectJPointcutAdvisor} adapts an {@link AbstractAspectJAdvice} to the\n * {@link PointcutAdvisor} interface.\n *\n * @author Adrian Colyer\n * @author Juergen Hoeller\n * @since 2.0\n */",
            "/**\n * {@code AspectJPointcutAdvisor} 将 {@link AbstractAspectJAdvice}\n * 适配为 {@link PointcutAdvisor} 接口。\n *\n * @author Adrian Colyer\n * @author Juergen Hoeller\n * @since 2.0\n */",
        ),
        (
            "\t/**\n\t * Create a new AspectJPointcutAdvisor for the given advice.\n\t * @param advice the AbstractAspectJAdvice to wrap\n\t */",
            "\t/**\n\t * 为给定 advice 创建新的 AspectJPointcutAdvisor。\n\t * @param advice 要封装的 AbstractAspectJAdvice\n\t */",
        ),
        (
            "\t/**\n\t * Return the name of the aspect (bean) in which the advice was declared.\n\t * @since 4.3.15\n\t * @see AbstractAspectJAdvice#getAspectName()\n\t */",
            "\t/**\n\t * 返回声明该通知的切面（bean）名称。\n\t * @since 4.3.15\n\t * @see AbstractAspectJAdvice#getAspectName()\n\t */",
        ),
    ],
    "AspectJPrecedenceInformation.java": [
        (
            "/**\n * Interface to be implemented by types that can supply the information\n * needed to sort advice/advisors by AspectJ's precedence rules.\n *\n * @author Adrian Colyer\n * @since 2.0\n * @see org.springframework.aop.aspectj.autoproxy.AspectJPrecedenceComparator\n */",
            "/**\n * 由能提供按 AspectJ 优先级规则排序 advice/advisor 所需信息的类型实现。\n *\n * @author Adrian Colyer\n * @since 2.0\n * @see org.springframework.aop.aspectj.autoproxy.AspectJPrecedenceComparator\n */",
        ),
        (
            "\t// Implementation note:\n\t// We need the level of indirection this interface provides as otherwise the\n\t// AspectJPrecedenceComparator must ask an Advisor for its Advice in all cases\n\t// in order to sort advisors. This causes problems with the\n\t// InstantiationModelAwarePointcutAdvisor which needs to delay creating\n\t// its advice for aspects with non-singleton instantiation models.",
            "\t// 实现说明：\n\t// 需要本接口提供的间接层，否则 AspectJPrecedenceComparator\n\t// 在所有情况下都须向 Advisor 索取 Advice 才能排序。\n\t// 这会导致 InstantiationModelAwarePointcutAdvisor 出现问题，\n\t// 该 Advisor 须为非单例实例化模型的切面延迟创建 advice。",
        ),
        (
            "\t/**\n\t * Return the name of the aspect (bean) in which the advice was declared.\n\t */",
            "\t/**\n\t * 返回声明该通知的切面（bean）名称。\n\t */",
        ),
        (
            "\t/**\n\t * Return the declaration order of the advice member within the aspect.\n\t */",
            "\t/**\n\t * 返回通知成员在切面内的声明顺序。\n\t */",
        ),
        (
            "\t/**\n\t * Return whether this is a before advice.\n\t */",
            "\t/**\n\t * 返回是否为 before 通知。\n\t */",
        ),
        (
            "\t/**\n\t * Return whether this is an after advice.\n\t */",
            "\t/**\n\t * 返回是否为 after 通知。\n\t */",
        ),
    ],
}
