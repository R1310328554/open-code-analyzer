"""Chinese JavaDoc replacements for springframework wave22a interceptor [11:20]."""

INTERCEPTOR_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "AbstractMonitoringInterceptor.java": [
        (
            "/**\n * Base class for monitoring interceptors, such as performance monitors.\n * Provides configurable \"prefix and \"suffix\" properties that help to\n * classify/group performance monitoring results.\n *\n * <p>In their {@link #invokeUnderTrace} implementation, subclasses should call the\n * {@link #createInvocationTraceName} method to create a name for the given trace,\n * including information about the method invocation along with a prefix/suffix.\n *\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @since 1.2.7\n * @see #setPrefix\n * @see #setSuffix\n * @see #createInvocationTraceName\n */",
            "/**\n * 监控拦截器（如性能监控器）的基类。\n * 提供可配置的 \"prefix\" 和 \"suffix\" 属性，\n * 用于分类/分组性能监控结果。\n *\n * <p>在 {@link #invokeUnderTrace} 实现中，子类应调用\n * {@link #createInvocationTraceName} 方法为给定跟踪创建名称，\n * 包含方法调用信息及前缀/后缀。\n *\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @since 1.2.7\n * @see #setPrefix\n * @see #setSuffix\n * @see #createInvocationTraceName\n */",
        ),
        (
            "\t/**\n\t * Set the text that will get appended to the trace data.\n\t * <p>Default is none.\n\t */",
            "\t/**\n\t * 设置追加到跟踪数据后的文本。\n\t * <p>默认为无。\n\t */",
        ),
        (
            "\t/**\n\t * Return the text that will get appended to the trace data.\n\t */",
            "\t/**\n\t * 返回追加到跟踪数据后的文本。\n\t */",
        ),
        (
            "\t/**\n\t * Set the text that will get prepended to the trace data.\n\t * <p>Default is none.\n\t */",
            "\t/**\n\t * 设置前置到跟踪数据前的文本。\n\t * <p>默认为无。\n\t */",
        ),
        (
            "\t/**\n\t * Return the text that will get prepended to the trace data.\n\t */",
            "\t/**\n\t * 返回前置到跟踪数据前的文本。\n\t */",
        ),
        (
            "\t/**\n\t * Set whether to log the invocation on the target class, if applicable\n\t * (i.e. if the method is actually delegated to the target class).\n\t * <p>Default is \"false\", logging the invocation based on the proxy\n\t * interface/class name.\n\t */",
            "\t/**\n\t * 设置是否记录目标类上的调用（若适用，\n\t * 即方法实际委托给目标类）。\n\t * <p>默认为 \"false\"，基于代理接口/类名记录调用。\n\t */",
        ),
        (
            "\t/**\n\t * Create a {@code String} name for the given {@code MethodInvocation}\n\t * that can be used for trace/logging purposes. This name is made up of the\n\t * configured prefix, followed by the fully-qualified name of the method being\n\t * invoked, followed by the configured suffix.\n\t * @see #setPrefix\n\t * @see #setSuffix\n\t */",
            "\t/**\n\t * 为给定 {@code MethodInvocation} 创建可用于跟踪/日志的 {@code String} 名称。\n\t * 该名称由配置的前缀、被调用方法的全限定名及配置的后缀组成。\n\t * @see #setPrefix\n\t * @see #setSuffix\n\t */",
        ),
    ],
    "AbstractTraceInterceptor.java": [
        (
            "/**\n * Base {@code MethodInterceptor} implementation for tracing.\n *\n * <p>By default, log messages are written to the log for the interceptor class,\n * not the class which is being intercepted. Setting the {@code useDynamicLogger}\n * bean property to {@code true} causes all log messages to be written to\n * the {@code Log} for the target class being intercepted.\n *\n * <p>Subclasses must implement the {@code invokeUnderTrace} method, which\n * is invoked by this class ONLY when a particular invocation SHOULD be traced.\n * Subclasses should write to the {@code Log} instance provided.\n *\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @since 1.2\n * @see #setUseDynamicLogger\n * @see #invokeUnderTrace(org.aopalliance.intercept.MethodInvocation, org.apache.commons.logging.Log)\n */",
            "/**\n * 用于跟踪的 {@code MethodInterceptor} 基类实现。\n *\n * <p>默认情况下，日志消息写入拦截器类的日志，\n * 而非被拦截类的日志。将 {@code useDynamicLogger} Bean 属性\n * 设为 {@code true} 时，所有日志消息写入被拦截目标类的 {@code Log}。\n *\n * <p>子类必须实现 {@code invokeUnderTrace} 方法，\n * 本类仅在特定调用应被跟踪时才调用它。\n * 子类应写入提供的 {@code Log} 实例。\n *\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @since 1.2\n * @see #setUseDynamicLogger\n * @see #invokeUnderTrace(org.aopalliance.intercept.MethodInvocation, org.apache.commons.logging.Log)\n */",
        ),
        (
            "\t/**\n\t * The default {@code Log} instance used to write trace messages.\n\t * This instance is mapped to the implementing {@code Class}.\n\t */",
            "\t/**\n\t * 用于写入跟踪消息的默认 {@code Log} 实例。\n\t * 该实例映射到实现 {@code Class}。\n\t */",
        ),
        (
            "\t/**\n\t * Indicates whether proxy class names should be hidden when using dynamic loggers.\n\t * @see #setUseDynamicLogger\n\t */",
            "\t/**\n\t * 使用动态 Logger 时是否隐藏代理类名。\n\t * @see #setUseDynamicLogger\n\t */",
        ),
        (
            "\t/**\n\t * Indicates whether to pass an exception to the logger.\n\t * @see #writeToLog(Log, String, Throwable)\n\t */",
            "\t/**\n\t * 是否将异常传递给 Logger。\n\t * @see #writeToLog(Log, String, Throwable)\n\t */",
        ),
        (
            "\t/**\n\t * Set whether to use a dynamic logger or a static logger.\n\t * Default is a static logger for this trace interceptor.\n\t * <p>Used to determine which {@code Log} instance should be used to write\n\t * log messages for a particular method invocation: a dynamic one for the\n\t * {@code Class} getting called, or a static one for the {@code Class}\n\t * of the trace interceptor.\n\t * <p><b>NOTE:</b> Specify either this property or \"loggerName\", not both.\n\t * @see #getLoggerForInvocation(org.aopalliance.intercept.MethodInvocation)\n\t */",
            "\t/**\n\t * 设置使用动态 Logger 还是静态 Logger。\n\t * 默认为本跟踪拦截器的静态 Logger。\n\t * <p>用于确定特定方法调用应使用哪个 {@code Log} 实例写入日志：\n\t * 被调用 {@code Class} 的动态 Logger，或跟踪拦截器 {@code Class} 的静态 Logger。\n\t * <p><b>注意：</b>请指定此属性或 \"loggerName\" 之一，不可同时指定。\n\t * @see #getLoggerForInvocation(org.aopalliance.intercept.MethodInvocation)\n\t */",
        ),
        (
            "\t/**\n\t * Set the name of the logger to use. The name will be passed to the\n\t * underlying logger implementation through Commons Logging, getting\n\t * interpreted as log category according to the logger's configuration.\n\t * <p>This can be specified to not log into the category of a class\n\t * (whether this interceptor's class or the class getting called)\n\t * but rather into a specific named category.\n\t * <p><b>NOTE:</b> Specify either this property or \"useDynamicLogger\", not both.\n\t * @see org.apache.commons.logging.LogFactory#getLog(String)\n\t * @see java.util.logging.Logger#getLogger(String)\n\t */",
            "\t/**\n\t * 设置要使用的 Logger 名称。名称通过 Commons Logging 传递给底层 Logger 实现，\n\t * 根据 Logger 配置解释为日志类别。\n\t * <p>可指定不写入类类别（无论是本拦截器类还是被调用类），\n\t * 而是写入特定命名类别。\n\t * <p><b>注意：</b>请指定此属性或 \"useDynamicLogger\" 之一，不可同时指定。\n\t * @see org.apache.commons.logging.LogFactory#getLog(String)\n\t * @see java.util.logging.Logger#getLogger(String)\n\t */",
        ),
        (
            "\t/**\n\t * Set to \"true\" to have {@link #setUseDynamicLogger dynamic loggers} hide\n\t * proxy class names wherever possible. Default is \"false\".\n\t */",
            "\t/**\n\t * 设为 \"true\" 时，{@link #setUseDynamicLogger 动态 Logger} 尽可能隐藏代理类名。\n\t * 默认为 \"false\"。\n\t */",
        ),
        (
            "\t/**\n\t * Set whether to pass an exception to the logger, suggesting inclusion\n\t * of its stack trace into the log. Default is \"true\"; set this to \"false\"\n\t * in order to reduce the log output to just the trace message (which may\n\t * include the exception class name and exception message, if applicable).\n\t * @since 4.3.10\n\t */",
            "\t/**\n\t * 设置是否将异常传递给 Logger，建议将其堆栈跟踪写入日志。\n\t * 默认为 \"true\"；设为 \"false\" 可将日志输出缩减为仅跟踪消息\n\t * （可能包含异常类名和异常消息）。\n\t * @since 4.3.10\n\t */",
        ),
        (
            "\t/**\n\t * Determines whether logging is enabled for the particular {@code MethodInvocation}.\n\t * If not, the method invocation proceeds as normal, otherwise the method invocation is passed\n\t * to the {@code invokeUnderTrace} method for handling.\n\t * @see #invokeUnderTrace(org.aopalliance.intercept.MethodInvocation, org.apache.commons.logging.Log)\n\t */",
            "\t/**\n\t * 判断特定 {@code MethodInvocation} 是否启用日志。\n\t * 若否，方法调用正常进行；否则将方法调用传递给 {@code invokeUnderTrace} 处理。\n\t * @see #invokeUnderTrace(org.aopalliance.intercept.MethodInvocation, org.apache.commons.logging.Log)\n\t */",
        ),
        (
            "\t/**\n\t * Return the appropriate {@code Log} instance to use for the given\n\t * {@code MethodInvocation}. If the {@code useDynamicLogger} flag\n\t * is set, the {@code Log} instance will be for the target class of the\n\t * {@code MethodInvocation}, otherwise the {@code Log} will be the\n\t * default static logger.\n\t * @param invocation the {@code MethodInvocation} being traced\n\t * @return the {@code Log} instance to use\n\t * @see #setUseDynamicLogger\n\t */",
            "\t/**\n\t * 返回给定 {@code MethodInvocation} 应使用的合适 {@code Log} 实例。\n\t * 若设置了 {@code useDynamicLogger} 标志，{@code Log} 实例\n\t * 对应 {@code MethodInvocation} 的目标类；否则为默认静态 Logger。\n\t * @param invocation 正在跟踪的 {@code MethodInvocation}\n\t * @return 要使用的 {@code Log} 实例\n\t * @see #setUseDynamicLogger\n\t */",
        ),
        (
            "\t/**\n\t * Determine the class to use for logging purposes.\n\t * @param target the target object to introspect\n\t * @return the target class for the given object\n\t * @see #setHideProxyClassNames\n\t */",
            "\t/**\n\t * 确定用于日志目的的类。\n\t * @param target 要内省的目标对象\n\t * @return 给定对象的目标类\n\t * @see #setHideProxyClassNames\n\t */",
        ),
        (
            "\t/**\n\t * Determine whether the interceptor should kick in, that is,\n\t * whether the {@code invokeUnderTrace} method should be called.\n\t * <p>Default behavior is to check whether the given {@code Log}\n\t * instance is enabled. Subclasses can override this to apply the\n\t * interceptor in other cases as well.\n\t * @param invocation the {@code MethodInvocation} being traced\n\t * @param logger the {@code Log} instance to check\n\t * @see #invokeUnderTrace\n\t * @see #isLogEnabled\n\t */",
            "\t/**\n\t * 判断拦截器是否应生效，即是否应调用 {@code invokeUnderTrace} 方法。\n\t * <p>默认行为是检查给定 {@code Log} 实例是否启用。\n\t * 子类可覆盖以在其他情况下也应用拦截器。\n\t * @param invocation 正在跟踪的 {@code MethodInvocation}\n\t * @param logger 要检查的 {@code Log} 实例\n\t * @see #invokeUnderTrace\n\t * @see #isLogEnabled\n\t */",
        ),
        (
            "\t/**\n\t * Determine whether the given {@link Log} instance is enabled.\n\t * <p>Default is {@code true} when the \"trace\" level is enabled.\n\t * Subclasses can override this to change the level under which 'tracing' occurs.\n\t * @param logger the {@code Log} instance to check\n\t */",
            "\t/**\n\t * 判断给定 {@link Log} 实例是否启用。\n\t * <p>默认在 \"trace\" 级别启用时为 {@code true}。\n\t * 子类可覆盖以更改发生「跟踪」的级别。\n\t * @param logger 要检查的 {@code Log} 实例\n\t */",
        ),
        (
            "\t/**\n\t * Write the supplied trace message to the supplied {@code Log} instance.\n\t * <p>To be called by {@link #invokeUnderTrace} for enter/exit messages.\n\t * <p>Delegates to {@link #writeToLog(Log, String, Throwable)} as the\n\t * ultimate delegate that controls the underlying logger invocation.\n\t * @since 4.3.10\n\t * @see #writeToLog(Log, String, Throwable)\n\t */",
            "\t/**\n\t * 将提供的跟踪消息写入提供的 {@code Log} 实例。\n\t * <p>由 {@link #invokeUnderTrace} 调用以处理进入/退出消息。\n\t * <p>委托给 {@link #writeToLog(Log, String, Throwable)} 作为\n\t * 控制底层 Logger 调用的最终委托。\n\t * @since 4.3.10\n\t * @see #writeToLog(Log, String, Throwable)\n\t */",
        ),
        (
            "\t/**\n\t * Write the supplied trace message and {@link Throwable} to the\n\t * supplied {@code Log} instance.\n\t * <p>To be called by {@link #invokeUnderTrace} for enter/exit outcomes,\n\t * potentially including an exception. Note that an exception's stack trace\n\t * won't get logged when {@link #setLogExceptionStackTrace} is \"false\".\n\t * <p>By default messages are written at {@code TRACE} level. Subclasses\n\t * can override this method to control which level the message is written\n\t * at, typically also overriding {@link #isLogEnabled} accordingly.\n\t * @since 4.3.10\n\t * @see #setLogExceptionStackTrace\n\t * @see #isLogEnabled\n\t */",
            "\t/**\n\t * 将提供的跟踪消息和 {@link Throwable} 写入提供的 {@code Log} 实例。\n\t * <p>由 {@link #invokeUnderTrace} 调用以处理进入/退出结果，\n\t * 可能包含异常。注意当 {@link #setLogExceptionStackTrace} 为 \"false\" 时\n\t * 不会记录异常堆栈跟踪。\n\t * <p>默认以 {@code TRACE} 级别写入消息。子类可覆盖以控制写入级别，\n\t * 通常也相应覆盖 {@link #isLogEnabled}。\n\t * @since 4.3.10\n\t * @see #setLogExceptionStackTrace\n\t * @see #isLogEnabled\n\t */",
        ),
        (
            "\t/**\n\t * Subclasses must override this method to perform any tracing around the\n\t * supplied {@code MethodInvocation}. Subclasses are responsible for\n\t * ensuring that the {@code MethodInvocation} actually executes by\n\t * calling {@code MethodInvocation.proceed()}.\n\t * <p>By default, the passed-in {@code Log} instance will have log level\n\t * \"trace\" enabled. Subclasses do not have to check for this again, unless\n\t * they overwrite the {@code isInterceptorEnabled} method to modify\n\t * the default behavior, and may delegate to {@code writeToLog} for actual\n\t * messages to be written.\n\t * @param logger the {@code Log} to write trace messages to\n\t * @return the result of the call to {@code MethodInvocation.proceed()}\n\t * @throws Throwable if the call to {@code MethodInvocation.proceed()}\n\t * encountered any errors\n\t * @see #isLogEnabled\n\t * @see #writeToLog(Log, String)\n\t * @see #writeToLog(Log, String, Throwable)\n\t */",
            "\t/**\n\t * 子类必须覆盖本方法以对提供的 {@code MethodInvocation} 执行跟踪。\n\t * 子类负责通过调用 {@code MethodInvocation.proceed()} 确保\n\t * {@code MethodInvocation} 实际执行。\n\t * <p>默认情况下，传入的 {@code Log} 实例已启用 \"trace\" 级别。\n\t * 子类无需再次检查，除非覆盖 {@code isInterceptorEnabled} 修改默认行为，\n\t * 并可委托 {@code writeToLog} 写入实际消息。\n\t * @param logger 写入跟踪消息的 {@code Log}\n\t * @return 对 {@code MethodInvocation.proceed()} 调用的结果\n\t * @throws Throwable 若 {@code MethodInvocation.proceed()} 调用遇到错误\n\t * @see #isLogEnabled\n\t * @see #writeToLog(Log, String)\n\t * @see #writeToLog(Log, String, Throwable)\n\t */",
        ),
    ],
    "AsyncExecutionAspectSupport.java": [
        (
            "/**\n * Base class for asynchronous method execution aspects, such as\n * {@code org.springframework.scheduling.annotation.AnnotationAsyncExecutionInterceptor}\n * or {@code org.springframework.scheduling.aspectj.AnnotationAsyncExecutionAspect}.\n *\n * <p>Provides support for <i>executor qualification</i> on a method-by-method basis.\n * {@code AsyncExecutionAspectSupport} objects must be constructed with a default {@code\n * Executor}, but each individual method may further qualify a specific {@code Executor}\n * bean to be used when executing it, for example, through an annotation attribute.\n *\n * @author Chris Beams\n * @author Juergen Hoeller\n * @author Stephane Nicoll\n * @author He Bo\n * @author Sebastien Deleuze\n * @since 3.1.2\n */",
            "/**\n * 异步方法执行切面的基类，例如\n * {@code org.springframework.scheduling.annotation.AnnotationAsyncExecutionInterceptor}\n * 或 {@code org.springframework.scheduling.aspectj.AnnotationAsyncExecutionAspect}。\n *\n * <p>支持按方法逐方法进行<i>执行器限定</i>。\n * {@code AsyncExecutionAspectSupport} 对象必须以默认 {@code Executor} 构造，\n * 但每个方法可进一步限定执行时使用的特定 {@code Executor} Bean，\n * 例如通过注解属性。\n *\n * @author Chris Beams\n * @author Juergen Hoeller\n * @author Stephane Nicoll\n * @author He Bo\n * @author Sebastien Deleuze\n * @since 3.1.2\n */",
        ),
        (
            "\t/**\n\t * The default name of the {@link TaskExecutor} bean to pick up: \"taskExecutor\".\n\t * <p>Note that the initial lookup happens by type; this is just the fallback\n\t * in case of multiple executor beans found in the context.\n\t * @since 4.2.6\n\t */",
            "\t/**\n\t * 要选取的 {@link TaskExecutor} Bean 的默认名称：\"taskExecutor\"。\n\t * <p>注意初始查找按类型进行；这只是在上下文中找到多个执行器 Bean 时的回退。\n\t * @since 4.2.6\n\t */",
        ),
        (
            "\t/**\n\t * Create a new instance with a default {@link AsyncUncaughtExceptionHandler}.\n\t * @param defaultExecutor the {@code Executor} (typically a Spring {@code AsyncTaskExecutor}\n\t * or {@link java.util.concurrent.ExecutorService}) to delegate to, unless a more specific\n\t * executor has been requested via a qualifier on the async method, in which case the\n\t * executor will be looked up at invocation time against the enclosing bean factory\n\t */",
            "\t/**\n\t * 使用默认 {@link AsyncUncaughtExceptionHandler} 创建新实例。\n\t * @param defaultExecutor 要委托的 {@code Executor}（通常是 Spring {@code AsyncTaskExecutor}\n\t * 或 {@link java.util.concurrent.ExecutorService}），除非异步方法上的限定符\n\t * 请求了更具体的执行器，此时将在调用时从 enclosing BeanFactory 查找\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@link AsyncExecutionAspectSupport} with the given exception handler.\n\t * @param defaultExecutor the {@code Executor} (typically a Spring {@code AsyncTaskExecutor}\n\t * or {@link java.util.concurrent.ExecutorService}) to delegate to, unless a more specific\n\t * executor has been requested via a qualifier on the async method, in which case the\n\t * executor will be looked up at invocation time against the enclosing bean factory\n\t * @param exceptionHandler the {@link AsyncUncaughtExceptionHandler} to use\n\t */",
            "\t/**\n\t * 使用给定异常处理器创建新的 {@link AsyncExecutionAspectSupport}。\n\t * @param defaultExecutor 要委托的 {@code Executor}（通常是 Spring {@code AsyncTaskExecutor}\n\t * 或 {@link java.util.concurrent.ExecutorService}），除非异步方法上的限定符\n\t * 请求了更具体的执行器，此时将在调用时从 enclosing BeanFactory 查找\n\t * @param exceptionHandler 要使用的 {@link AsyncUncaughtExceptionHandler}\n\t */",
        ),
        (
            "\t/**\n\t * Configure this aspect with the given executor and exception handler suppliers,\n\t * applying the corresponding default if a supplier is not resolvable.\n\t * @since 5.1\n\t */",
            "\t/**\n\t * 使用给定执行器和异常处理器 Supplier 配置本切面，\n\t * 若 Supplier 不可解析则应用对应默认值。\n\t * @since 5.1\n\t */",
        ),
        (
            "\t/**\n\t * Supply the executor to be used when executing async methods.\n\t * @param defaultExecutor the {@code Executor} (typically a Spring {@code AsyncTaskExecutor}\n\t * or {@link java.util.concurrent.ExecutorService}) to delegate to, unless a more specific\n\t * executor has been requested via a qualifier on the async method, in which case the\n\t * executor will be looked up at invocation time against the enclosing bean factory\n\t * @see #getExecutorQualifier(Method)\n\t * @see #setBeanFactory(BeanFactory)\n\t * @see #getDefaultExecutor(BeanFactory)\n\t */",
            "\t/**\n\t * 提供执行异步方法时使用的执行器。\n\t * @param defaultExecutor 要委托的 {@code Executor}（通常是 Spring {@code AsyncTaskExecutor}\n\t * 或 {@link java.util.concurrent.ExecutorService}），除非异步方法上的限定符\n\t * 请求了更具体的执行器，此时将在调用时从 enclosing BeanFactory 查找\n\t * @see #getExecutorQualifier(Method)\n\t * @see #setBeanFactory(BeanFactory)\n\t * @see #getDefaultExecutor(BeanFactory)\n\t */",
        ),
        (
            "\t/**\n\t * Supply the {@link AsyncUncaughtExceptionHandler} to use to handle exceptions\n\t * thrown by invoking asynchronous methods with a {@code void} return type.\n\t */",
            "\t/**\n\t * 提供用于处理调用 {@code void} 返回类型异步方法时\n\t * 抛出异常的 {@link AsyncUncaughtExceptionHandler}。\n\t */",
        ),
        (
            "\t/**\n\t * Set the {@link BeanFactory} to be used when looking up executors by qualifier\n\t * or when relying on the default executor lookup algorithm.\n\t * @see #findQualifiedExecutor(BeanFactory, String)\n\t * @see #getDefaultExecutor(BeanFactory)\n\t */",
            "\t/**\n\t * 设置按限定符查找执行器或依赖默认执行器查找算法时\n\t * 使用的 {@link BeanFactory}。\n\t * @see #findQualifiedExecutor(BeanFactory, String)\n\t * @see #getDefaultExecutor(BeanFactory)\n\t */",
        ),
        (
            "\t/**\n\t * Determine the specific executor to use when executing the given method.\n\t * @return the executor to use (or {@code null}, but just if no default executor is available)\n\t */",
            "\t/**\n\t * 确定执行给定方法时使用的特定执行器。\n\t * @return 要使用的执行器（或 {@code null}，但仅在没有默认执行器时）\n\t */",
        ),
        (
            "\t/**\n\t * Return the qualifier or bean name of the executor to be used when executing the\n\t * given async method, typically specified in the form of an annotation attribute.\n\t * <p>Returning an empty string or {@code null} indicates that no specific executor has\n\t * been specified and that the {@linkplain #setExecutor(Executor) default executor}\n\t * should be used.\n\t * @param method the method to inspect for executor qualifier metadata\n\t * @return the qualifier if specified, otherwise empty String or {@code null}\n\t * @see #determineAsyncExecutor(Method)\n\t * @see #findQualifiedExecutor(BeanFactory, String)\n\t */",
            "\t/**\n\t * 返回执行给定异步方法时要使用的执行器限定符或 Bean 名称，\n\t * 通常以注解属性形式指定。\n\t * <p>返回空字符串或 {@code null} 表示未指定特定执行器，\n\t * 应使用 {@linkplain #setExecutor(Executor) 默认执行器}。\n\t * @param method 要检查执行器限定符元数据的方法\n\t * @return 若指定则返回限定符，否则空 String 或 {@code null}\n\t * @see #determineAsyncExecutor(Method)\n\t * @see #findQualifiedExecutor(BeanFactory, String)\n\t */",
        ),
        (
            "\t/**\n\t * Retrieve a target executor for the given qualifier.\n\t * @param qualifier the qualifier to resolve\n\t * @return the target executor, or {@code null} if none available\n\t * @since 4.2.6\n\t * @see #getExecutorQualifier(Method)\n\t */",
            "\t/**\n\t * 检索给定限定符的目标执行器。\n\t * @param qualifier 要解析的限定符\n\t * @return 目标执行器，若无则 {@code null}\n\t * @since 4.2.6\n\t * @see #getExecutorQualifier(Method)\n\t */",
        ),
        (
            "\t/**\n\t * Retrieve or build a default executor for this advice instance.\n\t * <p>An executor returned from here will be cached for further use.\n\t * <p>The default implementation searches for a unique {@link TaskExecutor} bean\n\t * in the context, or for an {@link Executor} bean named \"taskExecutor\" otherwise.\n\t * If neither of the two is resolvable, this implementation will return {@code null}.\n\t * @param beanFactory the BeanFactory to use for a default executor lookup\n\t * @return the default executor, or {@code null} if none available\n\t * @since 4.2.6\n\t * @see #findQualifiedExecutor(BeanFactory, String)\n\t * @see #DEFAULT_TASK_EXECUTOR_BEAN_NAME\n\t */",
            "\t/**\n\t * 检索或构建本 advice 实例的默认执行器。\n\t * <p>从此处返回的执行器将被缓存以供后续使用。\n\t * <p>默认实现搜索上下文中唯一的 {@link TaskExecutor} Bean，\n\t * 否则查找名为 \"taskExecutor\" 的 {@link Executor} Bean。\n\t * 若两者均不可解析，本实现返回 {@code null}。\n\t * @param beanFactory 用于默认执行器查找的 BeanFactory\n\t * @return 默认执行器，若无则 {@code null}\n\t * @since 4.2.6\n\t * @see #findQualifiedExecutor(BeanFactory, String)\n\t * @see #DEFAULT_TASK_EXECUTOR_BEAN_NAME\n\t */",
        ),
        (
            "\t\t\t\t// Search for TaskExecutor bean... not plain Executor since that would\n\t\t\t\t// match with ScheduledExecutorService as well, which is unusable for\n\t\t\t\t// our purposes here. TaskExecutor is more clearly designed for it.",
            "\t\t\t\t// 搜索 TaskExecutor Bean... 而非普通 Executor，\n\t\t\t\t// 因为后者也会匹配 ScheduledExecutorService，\n\t\t\t\t// 对我们此处用途不可用。TaskExecutor 设计更明确。",
        ),
        (
            "\t\t\t\t// Giving up -> either using local default executor or none at all...",
            "\t\t\t\t// 放弃 -> 要么使用本地默认执行器，要么完全没有...",
        ),
        (
            "\t/**\n\t * Delegate for actually executing the given task with the chosen executor.\n\t * @param task the task to execute\n\t * @param executor the chosen executor\n\t * @param returnType the declared return type (potentially a {@link Future} variant)\n\t * @return the execution result (potentially a corresponding {@link Future} handle)\n\t */",
            "\t/**\n\t * 实际使用所选执行器执行给定任务的委托。\n\t * @param task 要执行的任务\n\t * @param executor 所选执行器\n\t * @param returnType 声明的返回类型（可能是 {@link Future} 变体）\n\t * @return 执行结果（可能是对应的 {@link Future} 句柄）\n\t */",
        ),
        (
            "\t/**\n\t * Handles a fatal error thrown while asynchronously invoking the specified\n\t * {@link Method}.\n\t * <p>If the return type of the method is a {@link Future} object, the original\n\t * exception can be propagated by just throwing it at the higher level. However,\n\t * for all other cases, the exception will not be transmitted back to the client.\n\t * In that later case, the current {@link AsyncUncaughtExceptionHandler} will be\n\t * used to manage such exception.\n\t * @param ex the exception to handle\n\t * @param method the method that was invoked\n\t * @param params the parameters used to invoke the method\n\t */",
            "\t/**\n\t * 处理异步调用指定 {@link Method} 时抛出的致命错误。\n\t * <p>若方法返回类型为 {@link Future} 对象，\n\t * 可通过在更高层直接抛出原始异常来传播。\n\t * 但在其他情况下，异常不会传回客户端。\n\t * 后一种情况下，当前 {@link AsyncUncaughtExceptionHandler} 将用于处理此类异常。\n\t * @param ex 要处理的异常\n\t * @param method 被调用的方法\n\t * @param params 用于调用方法的参数\n\t */",
        ),
        (
            "\t\t\t// Could not transmit the exception to the caller with default executor",
            "\t\t\t// 无法使用默认执行器将异常传递给调用者",
        ),
    ],
    "AsyncExecutionInterceptor.java": [
        (
            "/**\n * AOP Alliance {@code MethodInterceptor} that processes method invocations\n * asynchronously, using a given {@link org.springframework.core.task.AsyncTaskExecutor}.\n * Typically used with the {@link org.springframework.scheduling.annotation.Async} annotation.\n *\n * <p>In terms of target method signatures, any parameter types are supported.\n * However, the return type is constrained to either {@code void} or\n * {@code java.util.concurrent.Future}. In the latter case, the Future handle\n * returned from the proxy will be an actual asynchronous Future that can be used\n * to track the result of the asynchronous method execution. However, since the\n * target method needs to implement the same signature, it will have to return\n * a temporary Future handle that just passes the return value through\n * (like Spring's {@link org.springframework.scheduling.annotation.AsyncResult}\n * or EJB's {@code jakarta.ejb.AsyncResult}).\n *\n * <p>When the return type is {@code java.util.concurrent.Future}, any exception thrown\n * during the execution can be accessed and managed by the caller. With {@code void}\n * return type however, such exceptions cannot be transmitted back. In that case an\n * {@link AsyncUncaughtExceptionHandler} can be registered to process such exceptions.\n *\n * <p>Note: the {@code AnnotationAsyncExecutionInterceptor} subclass is preferred\n * due to its support for executor qualification in conjunction with Spring's\n * {@code @Async} annotation.\n *\n * @author Juergen Hoeller\n * @author Chris Beams\n * @author Stephane Nicoll\n * @since 3.0\n * @see org.springframework.scheduling.annotation.Async\n * @see org.springframework.scheduling.annotation.AsyncAnnotationAdvisor\n * @see org.springframework.scheduling.annotation.AnnotationAsyncExecutionInterceptor\n */",
            "/**\n * AOP Alliance {@code MethodInterceptor}，使用给定\n * {@link org.springframework.core.task.AsyncTaskExecutor} 异步处理方法调用。\n * 通常与 {@link org.springframework.scheduling.annotation.Async} 注解配合使用。\n *\n * <p>就目标方法签名而言，支持任意参数类型。\n * 但返回类型限制为 {@code void} 或 {@code java.util.concurrent.Future}。\n * 后者情况下，代理返回的 Future 句柄是可用于跟踪异步方法执行结果的真实异步 Future。\n * 但由于目标方法需实现相同签名，它必须返回仅传递返回值的临时 Future 句柄\n * （如 Spring 的 {@link org.springframework.scheduling.annotation.AsyncResult}\n * 或 EJB 的 {@code jakarta.ejb.AsyncResult}）。\n *\n * <p>当返回类型为 {@code java.util.concurrent.Future} 时，\n * 执行期间抛出的任何异常可由调用者访问和管理。\n * 但 {@code void} 返回类型时此类异常无法传回。\n * 此时可注册 {@link AsyncUncaughtExceptionHandler} 处理此类异常。\n *\n * <p>注意：{@code AnnotationAsyncExecutionInterceptor} 子类更优，\n * 因其支持与 Spring {@code @Async} 注解配合的执行器限定。\n *\n * @author Juergen Hoeller\n * @author Chris Beams\n * @author Stephane Nicoll\n * @since 3.0\n * @see org.springframework.scheduling.annotation.Async\n * @see org.springframework.scheduling.annotation.AsyncAnnotationAdvisor\n * @see org.springframework.scheduling.annotation.AnnotationAsyncExecutionInterceptor\n */",
        ),
        (
            "\t/**\n\t * Create a new instance with a default {@link AsyncUncaughtExceptionHandler}.\n\t * @param defaultExecutor the {@link Executor} (typically a Spring {@link AsyncTaskExecutor}\n\t * or {@link java.util.concurrent.ExecutorService}) to delegate to; a local\n\t * executor for this interceptor will be built otherwise\n\t */",
            "\t/**\n\t * 使用默认 {@link AsyncUncaughtExceptionHandler} 创建新实例。\n\t * @param defaultExecutor 要委托的 {@link Executor}（通常是 Spring {@link AsyncTaskExecutor}\n\t * 或 {@link java.util.concurrent.ExecutorService}）；否则将为本拦截器构建本地执行器\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@code AsyncExecutionInterceptor}.\n\t * @param defaultExecutor the {@link Executor} (typically a Spring {@link AsyncTaskExecutor}\n\t * or {@link java.util.concurrent.ExecutorService}) to delegate to; a local\n\t * executor for this interceptor will be built otherwise\n\t * @param exceptionHandler the {@link AsyncUncaughtExceptionHandler} to use\n\t */",
            "\t/**\n\t * 创建新的 {@code AsyncExecutionInterceptor}。\n\t * @param defaultExecutor 要委托的 {@link Executor}（通常是 Spring {@link AsyncTaskExecutor}\n\t * 或 {@link java.util.concurrent.ExecutorService}）；否则将为本拦截器构建本地执行器\n\t * @param exceptionHandler 要使用的 {@link AsyncUncaughtExceptionHandler}\n\t */",
        ),
        (
            "\t/**\n\t * Intercept the given method invocation, submit the actual calling of the method to\n\t * the correct task executor and return immediately to the caller.\n\t * @param invocation the method to intercept and make asynchronous\n\t * @return {@link Future} if the original method returns {@code Future}; {@code null}\n\t * otherwise.\n\t */",
            "\t/**\n\t * 拦截给定方法调用，将方法的实际调用提交给正确的任务执行器并立即返回调用者。\n\t * @param invocation 要拦截并异步化的方法\n\t * @return 若原方法返回 {@code Future} 则为 {@link Future}；否则 {@code null}\n\t */",
        ),
        (
            "\t/**\n\t * Get the qualifier for a specific executor to use when executing the given\n\t * method.\n\t * <p>The default implementation of this method is effectively a no-op.\n\t * <p>Subclasses may override this method to provide support for extracting\n\t * qualifier information &mdash; for example, via an annotation on the given\n\t * method.\n\t * @return always {@code null}\n\t * @since 3.1.2\n\t * @see #determineAsyncExecutor(Method)\n\t */",
            "\t/**\n\t * 获取执行给定方法时要使用的特定执行器的限定符。\n\t * <p>本方法的默认实现实际上为空操作。\n\t * <p>子类可覆盖以支持提取限定符信息——\n\t * 例如通过给定方法上的注解。\n\t * @return 始终 {@code null}\n\t * @since 3.1.2\n\t * @see #determineAsyncExecutor(Method)\n\t */",
        ),
        (
            "\t/**\n\t * This implementation searches for a unique {@link org.springframework.core.task.TaskExecutor}\n\t * bean in the context, or for an {@link Executor} bean named \"taskExecutor\" otherwise.\n\t * If neither of the two is resolvable (for example, if no {@code BeanFactory} was configured at all),\n\t * this implementation falls back to a newly created {@link SimpleAsyncTaskExecutor} instance\n\t * for local use if no default could be found.\n\t * @see #DEFAULT_TASK_EXECUTOR_BEAN_NAME\n\t */",
            "\t/**\n\t * 本实现搜索上下文中唯一的 {@link org.springframework.core.task.TaskExecutor} Bean，\n\t * 否则查找名为 \"taskExecutor\" 的 {@link Executor} Bean。\n\t * 若两者均不可解析（例如完全未配置 {@code BeanFactory}），\n\t * 且找不到默认执行器，则回退到新创建的 {@link SimpleAsyncTaskExecutor} 实例供本地使用。\n\t * @see #DEFAULT_TASK_EXECUTOR_BEAN_NAME\n\t */",
        ),
    ],
    "AsyncUncaughtExceptionHandler.java": [
        (
            "/**\n * A strategy for handling uncaught exceptions thrown from asynchronous methods.\n *\n * <p>An asynchronous method usually returns a {@link java.util.concurrent.Future}\n * instance that gives access to the underlying exception. When the method does\n * not provide that return type, this handler can be used to manage such\n * uncaught exceptions.\n *\n * @author Stephane Nicoll\n * @since 4.1\n */",
            "/**\n * 处理异步方法抛出的未捕获异常的策略。\n *\n * <p>异步方法通常返回 {@link java.util.concurrent.Future} 实例，\n * 可访问底层异常。当方法不提供该返回类型时，\n * 可使用本处理器管理此类未捕获异常。\n *\n * @author Stephane Nicoll\n * @since 4.1\n */",
        ),
        (
            "\t/**\n\t * Handle the given uncaught exception thrown from an asynchronous method.\n\t * @param ex the exception thrown from the asynchronous method\n\t * @param method the asynchronous method\n\t * @param params the parameters used to invoke the method\n\t */",
            "\t/**\n\t * 处理异步方法抛出的给定未捕获异常。\n\t * @param ex 异步方法抛出的异常\n\t * @param method 异步方法\n\t * @param params 用于调用方法的参数\n\t */",
        ),
    ],
    "ConcurrencyThrottleInterceptor.java": [
        (
            "/**\n * Interceptor that throttles concurrent access, blocking invocations\n * if a specified concurrency limit is reached.\n *\n * <p>Can be applied to methods of local services that involve heavy use\n * of system resources, in a scenario where it is more efficient to\n * throttle concurrency for a specific service rather than restrict\n * the entire thread pool (for example, the web container's thread pool).\n *\n * <p>The default concurrency limit of this interceptor is 1.\n * Specify the \"concurrencyLimit\" bean property to change this value.\n *\n * @author Juergen Hoeller\n * @since 11.02.2004\n * @see #setConcurrencyLimit\n */",
            "/**\n * 限制并发访问的拦截器，达到指定并发上限时阻塞调用。\n *\n * <p>可应用于涉及大量系统资源的本地服务方法，\n * 在针对特定服务限制并发比限制整个线程池\n * （例如 Web 容器线程池）更高效的场景中。\n *\n * <p>本拦截器默认并发上限为 1。\n * 通过 \"concurrencyLimit\" Bean 属性更改此值。\n *\n * @author Juergen Hoeller\n * @since 11.02.2004\n * @see #setConcurrencyLimit\n */",
        ),
        (
            "\t/**\n\t * Create a default {@code ConcurrencyThrottleInterceptor}\n\t * with concurrency limit 1.\n\t */",
            "\t/**\n\t * 创建并发上限为 1 的默认 {@code ConcurrencyThrottleInterceptor}。\n\t */",
        ),
        (
            "\t/**\n\t * Create a {@code ConcurrencyThrottleInterceptor}\n\t * with the given concurrency limit.\n\t * @since 7.0\n\t */",
            "\t/**\n\t * 使用给定并发上限创建 {@code ConcurrencyThrottleInterceptor}。\n\t * @since 7.0\n\t */",
        ),
    ],
    "DebugInterceptor.java": [
        (
            "/**\n * AOP Alliance {@code MethodInterceptor} that can be introduced in a chain\n * to display verbose information about intercepted invocations to the logger.\n *\n * <p>Logs full invocation details on method entry and method exit,\n * including invocation arguments and invocation count. This is only\n * intended for debugging purposes; use {@code SimpleTraceInterceptor}\n * or {@code CustomizableTraceInterceptor} for pure tracing purposes.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @see SimpleTraceInterceptor\n * @see CustomizableTraceInterceptor\n */",
            "/**\n * 可引入拦截器链以向 Logger 显示被拦截调用详细信息的\n * AOP Alliance {@code MethodInterceptor}。\n *\n * <p>在方法进入和退出时记录完整调用细节，\n * 包括调用参数和调用计数。仅用于调试目的；\n * 纯跟踪目的请使用 {@code SimpleTraceInterceptor}\n * 或 {@code CustomizableTraceInterceptor}。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @see SimpleTraceInterceptor\n * @see CustomizableTraceInterceptor\n */",
        ),
        (
            "\t/**\n\t * Create a new DebugInterceptor with a static logger.\n\t */",
            "\t/**\n\t * 使用静态 Logger 创建新的 DebugInterceptor。\n\t */",
        ),
        (
            "\t/**\n\t * Create a new DebugInterceptor with dynamic or static logger,\n\t * according to the given flag.\n\t * @param useDynamicLogger whether to use a dynamic logger or a static logger\n\t * @see #setUseDynamicLogger\n\t */",
            "\t/**\n\t * 根据给定标志使用动态或静态 Logger 创建新的 DebugInterceptor。\n\t * @param useDynamicLogger 是否使用动态 Logger 或静态 Logger\n\t * @see #setUseDynamicLogger\n\t */",
        ),
        (
            "\t/**\n\t * Return the number of times this interceptor has been invoked.\n\t */",
            "\t/**\n\t * 返回本拦截器被调用的次数。\n\t */",
        ),
        (
            "\t/**\n\t * Reset the invocation count to zero.\n\t */",
            "\t/**\n\t * 将调用计数重置为零。\n\t */",
        ),
    ],
    "ExposeBeanNameAdvisors.java": [
        (
            "/**\n * Convenient methods for creating advisors that may be used when autoproxying beans\n * created with the Spring IoC container, binding the bean name to the current\n * invocation. May support a {@code bean()} pointcut designator with AspectJ.\n *\n * <p>Typically used in Spring auto-proxying, where the bean name is known\n * at proxy creation time.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @since 2.0\n * @see org.springframework.beans.factory.NamedBean\n */",
            "/**\n * 创建 Advisor 的便捷方法，可用于自动代理 Spring IoC 容器创建的 Bean，\n * 将 Bean 名称绑定到当前调用。可支持 AspectJ 的 {@code bean()} 切入点指示符。\n *\n * <p>通常用于 Spring 自动代理，此时 Bean 名称在代理创建时已知。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @since 2.0\n * @see org.springframework.beans.factory.NamedBean\n */",
        ),
        (
            "\t/**\n\t * Binding for the bean name of the bean which is currently being invoked\n\t * in the ReflectiveMethodInvocation userAttributes Map.\n\t */",
            "\t/**\n\t * ReflectiveMethodInvocation userAttributes Map 中\n\t * 当前被调用 Bean 名称的绑定键。\n\t */",
        ),
        (
            "\t/**\n\t * Find the bean name for the current invocation. Assumes that an ExposeBeanNameAdvisor\n\t * has been included in the interceptor chain, and that the invocation is exposed\n\t * with ExposeInvocationInterceptor.\n\t * @return the bean name (never {@code null})\n\t * @throws IllegalStateException if the bean name has not been exposed\n\t */",
            "\t/**\n\t * 查找当前调用的 Bean 名称。假设拦截器链中已包含 ExposeBeanNameAdvisor，\n\t * 且调用已通过 ExposeInvocationInterceptor 暴露。\n\t * @return Bean 名称（永不为 {@code null}）\n\t * @throws IllegalStateException 若 Bean 名称尚未暴露\n\t */",
        ),
        (
            "\t/**\n\t * Find the bean name for the given invocation. Assumes that an ExposeBeanNameAdvisor\n\t * has been included in the interceptor chain.\n\t * @param mi the MethodInvocation that should contain the bean name as an attribute\n\t * @return the bean name (never {@code null})\n\t * @throws IllegalStateException if the bean name has not been exposed\n\t */",
            "\t/**\n\t * 查找给定调用的 Bean 名称。假设拦截器链中已包含 ExposeBeanNameAdvisor。\n\t * @param mi 应包含 Bean 名称作为属性的 MethodInvocation\n\t * @return Bean 名称（永不为 {@code null}）\n\t * @throws IllegalStateException 若 Bean 名称尚未暴露\n\t */",
        ),
        (
            "\t/**\n\t * Create a new advisor that will expose the given bean name,\n\t * with no introduction.\n\t * @param beanName bean name to expose\n\t */",
            "\t/**\n\t * 创建将暴露给定 Bean 名称的新 Advisor，无 Introduction。\n\t * @param beanName 要暴露的 Bean 名称\n\t */",
        ),
        (
            "\t/**\n\t * Create a new advisor that will expose the given bean name, introducing\n\t * the NamedBean interface to make the bean name accessible without forcing\n\t * the target object to be aware of this Spring IoC concept.\n\t * @param beanName the bean name to expose\n\t */",
            "\t/**\n\t * 创建将暴露给定 Bean 名称的新 Advisor，引入 NamedBean 接口\n\t * 使 Bean 名称可访问，而无需强制目标对象感知此 Spring IoC 概念。\n\t * @param beanName 要暴露的 Bean 名称\n\t */",
        ),
        (
            "\t/**\n\t * Interceptor that exposes the specified bean name as invocation attribute.\n\t */",
            "\t/**\n\t * 将指定 Bean 名称作为调用属性暴露的拦截器。\n\t */",
        ),
        (
            "\t/**\n\t * Introduction that exposes the specified bean name as invocation attribute.\n\t */",
            "\t/**\n\t * 将指定 Bean 名称作为调用属性暴露的 Introduction。\n\t */",
        ),
    ],
    "ExposeInvocationInterceptor.java": [
        (
            "/**\n * Interceptor that exposes the current {@link org.aopalliance.intercept.MethodInvocation}\n * as a thread-local object. We occasionally need to do this; for example, when a pointcut\n * (for example, an AspectJ expression pointcut) needs to know the full invocation context.\n *\n * <p>Don't use this interceptor unless this is really necessary. Target objects should\n * not normally know about Spring AOP, as this creates a dependency on Spring API.\n * Target objects should be plain POJOs as far as possible.\n *\n * <p>If used, this interceptor will normally be the first in the interceptor chain.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n */",
            "/**\n * 将当前 {@link org.aopalliance.intercept.MethodInvocation}\n * 作为线程本地对象暴露的拦截器。我们偶尔需要这样做；\n * 例如当切入点（如 AspectJ 表达式切入点）需要了解完整调用上下文时。\n *\n * <p>除非确实必要，否则不要使用本拦截器。目标对象通常不应了解 Spring AOP，\n * 因为这会产生对 Spring API 的依赖。目标对象应尽可能为普通 POJO。\n *\n * <p>若使用，本拦截器通常应位于拦截器链首位。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n */",
        ),
        (
            "\t/** Singleton instance of this class. */",
            "\t/** 本类的单例实例。 */",
        ),
        (
            "\t/**\n\t * Singleton advisor for this class. Use in preference to INSTANCE when using\n\t * Spring AOP, as it prevents the need to create a new Advisor to wrap the instance.\n\t */",
            "\t/**\n\t * 本类的单例 Advisor。使用 Spring AOP 时优先于 INSTANCE，\n\t * 避免需要创建新 Advisor 包装实例。\n\t */",
        ),
        (
            "\t/**\n\t * Return the AOP Alliance MethodInvocation object associated with the current invocation.\n\t * @return the invocation object associated with the current invocation\n\t * @throws IllegalStateException if there is no AOP invocation in progress,\n\t * or if the ExposeInvocationInterceptor was not added to this interceptor chain\n\t */",
            "\t/**\n\t * 返回与当前调用关联的 AOP Alliance MethodInvocation 对象。\n\t * @return 与当前调用关联的调用对象\n\t * @throws IllegalStateException 若无 AOP 调用进行中，\n\t * 或 ExposeInvocationInterceptor 未添加到本拦截器链\n\t */",
        ),
        (
            "\t/**\n\t * Ensures that only the canonical instance can be created.\n\t */",
            "\t/**\n\t * 确保只能创建规范实例。\n\t */",
        ),
        (
            "\t/**\n\t * Required to support serialization. Replaces with canonical instance\n\t * on deserialization, protecting Singleton pattern.\n\t * <p>Alternative to overriding the {@code equals} method.\n\t */",
            "\t/**\n\t * 支持序列化所需。反序列化时替换为规范实例，保护单例模式。\n\t * <p>覆盖 {@code equals} 方法的替代方案。\n\t */",
        ),
    ],
}
