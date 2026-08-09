"""Chinese JavaDoc replacements for springframework wave22b interceptors [1:3]."""

INTERCEPTOR_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "PerformanceMonitorInterceptor.java": [
        (
            "/**\n * Simple AOP Alliance {@code MethodInterceptor} for performance monitoring.\n * This interceptor has no effect on the intercepted method call.\n *\n * <p>Uses a {@code StopWatch} for the actual performance measuring.\n *\n * @author Rod Johnson\n * @author Dmitriy Kopylenko\n * @author Rob Harrop\n * @see org.springframework.util.StopWatch\n */",
            "/**\n * 用于性能监控的简单 AOP Alliance {@code MethodInterceptor}。\n * 该拦截器不会改变被拦截方法调用的行为。\n *\n * <p>使用 {@code StopWatch} 进行实际性能测量。\n *\n * @author Rod Johnson\n * @author Dmitriy Kopylenko\n * @author Rob Harrop\n * @see org.springframework.util.StopWatch\n */",
        ),
        (
            "\t/**\n\t * Create a new PerformanceMonitorInterceptor with a static logger.\n\t */",
            "\t/**\n\t * 使用静态 logger 创建新的 PerformanceMonitorInterceptor。\n\t */",
        ),
        (
            "\t/**\n\t * Create a new PerformanceMonitorInterceptor with a dynamic or static logger,\n\t * according to the given flag.\n\t * @param useDynamicLogger whether to use a dynamic logger or a static logger\n\t * @see #setUseDynamicLogger\n\t */",
            "\t/**\n\t * 根据给定标志，使用动态或静态 logger 创建新的 PerformanceMonitorInterceptor。\n\t * @param useDynamicLogger 是否使用动态 logger 而非静态 logger\n\t * @see #setUseDynamicLogger\n\t */",
        ),
    ],
    "SimpleAsyncUncaughtExceptionHandler.java": [
        (
            "/**\n * A default {@link AsyncUncaughtExceptionHandler} that simply logs the exception.\n *\n * @author Stephane Nicoll\n * @author Juergen Hoeller\n * @since 4.1\n */",
            "/**\n * 默认 {@link AsyncUncaughtExceptionHandler}，仅记录异常日志。\n *\n * @author Stephane Nicoll\n * @author Juergen Hoeller\n * @since 4.1\n */",
        ),
        (
            "\tprivate static final Log logger = LogFactory.getLog(SimpleAsyncUncaughtExceptionHandler.class);",
            "\t/** 用于记录异步未捕获异常的日志器。 */\n\tprivate static final Log logger = LogFactory.getLog(SimpleAsyncUncaughtExceptionHandler.class);",
        ),
    ],
    "SimpleTraceInterceptor.java": [
        (
            "/**\n * Simple AOP Alliance {@code MethodInterceptor} that can be introduced\n * in a chain to display verbose trace information about intercepted method\n * invocations, with method entry and method exit info.\n *\n * <p>Consider using {@code CustomizableTraceInterceptor} for more\n * advanced needs.\n *\n * @author Dmitriy Kopylenko\n * @author Juergen Hoeller\n * @since 1.2\n * @see CustomizableTraceInterceptor\n */",
            "/**\n * 简单的 AOP Alliance {@code MethodInterceptor}，\n * 可加入拦截器链以输出被拦截方法调用的详细跟踪信息，\n * 包括方法进入与退出信息。\n *\n * <p>若有更高级需求，可考虑使用 {@code CustomizableTraceInterceptor}。\n *\n * @author Dmitriy Kopylenko\n * @author Juergen Hoeller\n * @since 1.2\n * @see CustomizableTraceInterceptor\n */",
        ),
        (
            "\t/**\n\t * Create a new SimpleTraceInterceptor with a static logger.\n\t */",
            "\t/**\n\t * 使用静态 logger 创建新的 SimpleTraceInterceptor。\n\t */",
        ),
        (
            "\t/**\n\t * Create a new SimpleTraceInterceptor with dynamic or static logger,\n\t * according to the given flag.\n\t * @param useDynamicLogger whether to use a dynamic logger or a static logger\n\t * @see #setUseDynamicLogger\n\t */",
            "\t/**\n\t * 根据给定标志，使用动态或静态 logger 创建新的 SimpleTraceInterceptor。\n\t * @param useDynamicLogger 是否使用动态 logger 而非静态 logger\n\t * @see #setUseDynamicLogger\n\t */",
        ),
        (
            "\t/**\n\t * Return a description for the given method invocation.\n\t * @param invocation the invocation to describe\n\t * @return the description\n\t */",
            "\t/**\n\t * 返回给定方法调用的描述。\n\t * @param invocation 待描述的方法调用\n\t * @return 描述字符串\n\t */",
        ),
    ],
}
