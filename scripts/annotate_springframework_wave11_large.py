"""Large-file JavaDoc replacements for wave-11 batch [20:40]."""

LARGE_FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "EnableAsync.java": [
        (
            "/**\n * Enables Spring's asynchronous method execution capability, similar to functionality\n * found in Spring's {@code <task:*>} XML namespace.",
            "/**\n * 启用 Spring 异步方法执行能力，类似于 Spring {@code <task:*>} XML 命名空间中的功能。",
        ),
        (
            " * <p>To be used together with @{@link Configuration Configuration} classes as follows,\n * enabling annotation-driven async processing for an entire Spring application context:",
            " * <p>与 @{@link Configuration Configuration} 类配合使用，\n * 为整个 Spring 应用上下文启用注解驱动的异步处理：",
        ),
        (
            " * {@code MyAsyncBean} is a user-defined type with one or more methods annotated with\n * either Spring's {@code @Async} annotation, the EJB 3.1 {@code @jakarta.ejb.Asynchronous}\n * annotation, or any custom annotation specified via the {@link #annotation} attribute.\n * The aspect is added transparently for any registered bean, for instance via this\n * configuration:",
            " * {@code MyAsyncBean} 为用户定义类型，其一个或多个方法标注 Spring {@code @Async}、\n * EJB 3.1 {@code @jakarta.ejb.Asynchronous} 或通过 {@link #annotation} 属性指定的自定义注解。\n * 切面对任何已注册 Bean 透明添加，例如通过以下配置：",
        ),
        (
            " * <p>By default, Spring will be searching for an associated thread pool definition:\n * either a unique {@link org.springframework.core.task.TaskExecutor} bean in the context,\n * or an {@link java.util.concurrent.Executor} bean named \"taskExecutor\" otherwise. If\n * neither of the two is resolvable, a {@link org.springframework.core.task.SimpleAsyncTaskExecutor}\n * will be used to process async method invocations. Besides, annotated methods having a\n * {@code void} return type cannot transmit any exception back to the caller. By default,\n * such uncaught exceptions are only logged.",
            " * <p>默认情况下，Spring 将查找关联的线程池定义：\n * 上下文中唯一的 {@link org.springframework.core.task.TaskExecutor} Bean，\n * 否则名为 \"taskExecutor\" 的 {@link java.util.concurrent.Executor} Bean。\n * 若两者均不可解析，将使用 {@link org.springframework.core.task.SimpleAsyncTaskExecutor}\n * 处理异步方法调用。此外，{@code void} 返回类型的标注方法无法将异常传回调用方，\n * 默认仅记录此类未捕获异常。",
        ),
        (
            " * <p>To customize all this, implement {@link AsyncConfigurer} and provide:\n * <ul>\n * <li>your own {@link java.util.concurrent.Executor Executor} through the\n * {@link AsyncConfigurer#getAsyncExecutor getAsyncExecutor()} method, and</li>\n * <li>your own {@link org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler\n * AsyncUncaughtExceptionHandler} through the {@link AsyncConfigurer#getAsyncUncaughtExceptionHandler\n * getAsyncUncaughtExceptionHandler()}\n * method.</li>\n * </ul>",
            " * <p>要自定义上述行为，实现 {@link AsyncConfigurer} 并提供：\n * <ul>\n * <li>通过 {@link AsyncConfigurer#getAsyncExecutor getAsyncExecutor()} 方法\n * 提供自定义 {@link java.util.concurrent.Executor Executor}，以及</li>\n * <li>通过 {@link AsyncConfigurer#getAsyncUncaughtExceptionHandler\n * getAsyncUncaughtExceptionHandler()} 方法提供自定义\n * {@link org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler\n * AsyncUncaughtExceptionHandler}。</li>\n * </ul>",
        ),
        (
            " * <p><b>NOTE: {@link AsyncConfigurer} configuration classes get initialized early\n * in the application context bootstrap. If you need any dependencies on other beans\n * there, make sure to declare them 'lazy' as far as possible in order to let them\n * go through other post-processors as well.</b>",
            " * <p><b>注意：{@link AsyncConfigurer} 配置类在应用上下文引导阶段较早初始化。\n * 若需依赖其他 Bean，请尽可能声明为 lazy，以便它们也能经过其他后处理器。</b>",
        ),
        (
            " * <p>If only one item needs to be customized, {@code null} can be returned to\n * keep the default settings.",
            " * <p>若只需自定义其中一项，可返回 {@code null} 以保持默认设置。",
        ),
        (
            " * <p>Note: In the above example the {@code ThreadPoolTaskExecutor} is not a fully managed\n * Spring bean. Add the {@code @Bean} annotation to the {@code getAsyncExecutor()} method\n * if you want a fully managed bean. In such circumstances it is no longer necessary to\n * manually call the {@code executor.initialize()} method as this will be invoked\n * automatically when the bean is initialized.",
            " * <p>注意：上例中 {@code ThreadPoolTaskExecutor} 并非完全受管的 Spring Bean。\n * 若需要完全受管 Bean，请在 {@code getAsyncExecutor()} 方法上添加 {@code @Bean} 注解。\n * 此时无需手动调用 {@code executor.initialize()}，Bean 初始化时将自动调用。",
        ),
        (
            " * <p>For reference, the example above can be compared to the following Spring XML\n * configuration:",
            " * <p>作为参考，上例可与以下 Spring XML 配置对比：",
        ),
        (
            " * The above XML-based and JavaConfig-based examples are equivalent except for the\n * setting of the <em>thread name prefix</em> of the {@code Executor}; this is because\n * the {@code <task:executor>} element does not expose such an attribute. This\n * demonstrates how the JavaConfig-based approach allows for maximum configurability\n * through direct access to the actual component.",
            " * 上述基于 XML 与 JavaConfig 的示例等价，仅 {@code Executor} 的<em>线程名前缀</em>设置不同；\n * 因为 {@code <task:executor>} 元素未暴露该属性。\n * 这展示了 JavaConfig 方式通过直接访问实际组件实现最大可配置性。",
        ),
        (
            " * <p>The {@link #mode} attribute controls how advice is applied: If the mode is\n * {@link AdviceMode#PROXY} (the default), then the other attributes control the behavior\n * of the proxying. Please note that proxy mode allows for interception of calls through\n * the proxy only; local calls within the same class cannot get intercepted that way.",
            " * <p>{@link #mode} 属性控制通知如何应用：若模式为 {@link AdviceMode#PROXY}（默认），\n * 则其他属性控制代理行为。请注意代理模式仅拦截通过代理的调用；\n * 同类内部本地调用无法被拦截。",
        ),
        (
            " * <p>Note that if the {@linkplain #mode} is set to {@link AdviceMode#ASPECTJ}, then the\n * value of the {@link #proxyTargetClass} attribute will be ignored. Note also that in\n * this case the {@code spring-aspects} module JAR must be present on the classpath, with\n * compile-time weaving or load-time weaving applying the aspect to the affected classes.\n * There is no proxy involved in such a scenario; local calls will be intercepted as well.",
            " * <p>若 {@linkplain #mode} 设为 {@link AdviceMode#ASPECTJ}，\n * 则 {@link #proxyTargetClass} 属性值将被忽略。\n * 此时类路径上须有 {@code spring-aspects} 模块 JAR，\n * 并通过编译时或加载时织入将切面应用于受影响类。\n * 此场景不涉及代理，本地调用也会被拦截。",
        ),
        (
            " * <p><b>Note: {@code @EnableAsync} applies to its local application context only,\n * allowing for selective activation at different levels.</b> Please redeclare\n * {@code @EnableAsync} in each individual context, for example, the common root web\n * application context and any separate {@code DispatcherServlet} application contexts,\n * if you need to apply its behavior at multiple levels.",
            " * <p><b>注意：{@code @EnableAsync} 仅作用于其本地应用上下文，\n * 允许在不同层级选择性启用。</b> 若需在多个层级应用其行为，\n * 请在各独立上下文中重新声明 {@code @EnableAsync}，\n * 例如公共根 Web 应用上下文及独立的 {@code DispatcherServlet} 应用上下文。",
        ),
        (
            "\t/**\n\t * Indicate the 'async' annotation type to be detected at either class\n\t * or method level.\n\t * <p>By default, both Spring's @{@link Async} annotation and the EJB 3.1\n\t * {@code @jakarta.ejb.Asynchronous} annotation will be detected.\n\t * <p>This attribute exists so that developers can provide their own\n\t * custom annotation type to indicate that a method (or all methods of\n\t * a given class) should be invoked asynchronously.\n\t */",
            "\t/**\n\t * 指定在类或方法级别检测的“异步”注解类型。\n\t * <p>默认检测 Spring @{@link Async} 注解及 EJB 3.1 {@code @jakarta.ejb.Asynchronous} 注解。\n\t * <p>此属性供开发者提供自定义注解类型，\n\t * 以指示方法（或给定类的全部方法）应异步调用。\n\t */",
        ),
        (
            "\t/**\n\t * Indicate whether subclass-based (CGLIB) proxies are to be created as opposed\n\t * to standard Java interface-based proxies.\n\t * <p><strong>Applicable only if the {@link #mode} is set to {@link AdviceMode#PROXY}</strong>.\n\t * <p>The default is {@code false}.\n\t * <p>Note that setting this attribute to {@code true} will only affect\n\t * {@link AsyncAnnotationBeanPostProcessor}.\n\t * <p>It is usually recommendable to rely on a global default proxy configuration\n\t * instead, with specific proxy requirements for certain beans expressed through\n\t * a {@link org.springframework.context.annotation.Proxyable} annotation on\n\t * the affected bean classes.\n\t * @see org.springframework.aop.config.AopConfigUtils#forceAutoProxyCreatorToUseClassProxying\n\t */",
            "\t/**\n\t * 指示是否创建基于子类（CGLIB）的代理，而非标准 Java 接口代理。\n\t * <p><strong>仅当 {@link #mode} 设为 {@link AdviceMode#PROXY} 时适用。</strong>\n\t * <p>默认为 {@code false}。\n\t * <p>将此属性设为 {@code true} 仅影响 {@link AsyncAnnotationBeanPostProcessor}。\n\t * <p>通常建议依赖全局默认代理配置，\n\t * 对特定 Bean 的代理需求通过受影响 Bean 类上的\n\t * {@link org.springframework.context.annotation.Proxyable} 注解表达。\n\t * @see org.springframework.aop.config.AopConfigUtils#forceAutoProxyCreatorToUseClassProxying\n\t */",
        ),
        (
            "\t/**\n\t * Indicate how async advice should be applied.\n\t * <p><b>The default is {@link AdviceMode#PROXY}.</b>\n\t * Please note that proxy mode allows for interception of calls through the proxy\n\t * only. Local calls within the same class cannot get intercepted that way; an\n\t * {@link Async} annotation on such a method within a local call will be ignored\n\t * since Spring's interceptor does not even kick in for such a runtime scenario.\n\t * For a more advanced mode of interception, consider switching this to\n\t * {@link AdviceMode#ASPECTJ}.\n\t */",
            "\t/**\n\t * 指示如何应用异步通知。\n\t * <p><b>默认为 {@link AdviceMode#PROXY}。</b>\n\t * 请注意代理模式仅拦截通过代理的调用；同类本地调用无法被拦截，\n\t * 本地调用中此类方法上的 {@link Async} 注解将被忽略，\n\t * 因为 Spring 拦截器在此运行时场景下不会生效。\n\t * 如需更高级的拦截模式，可考虑切换为 {@link AdviceMode#ASPECTJ}。\n\t */",
        ),
        (
            "\t/**\n\t * Indicate the order in which the {@link AsyncAnnotationBeanPostProcessor}\n\t * should be applied.\n\t * <p>The default is {@link Ordered#LOWEST_PRECEDENCE} in order to run\n\t * after all other post-processors, so that it can add an advisor to\n\t * existing proxies rather than double-proxy.\n\t */",
            "\t/**\n\t * 指示 {@link AsyncAnnotationBeanPostProcessor} 的应用顺序。\n\t * <p>默认为 {@link Ordered#LOWEST_PRECEDENCE}，以便在所有其他后处理器之后运行，\n\t * 从而向现有代理添加 Advisor 而非双重代理。\n\t */",
        ),
    ],
    "EnableScheduling.java": [
        (
            "/**\n * Enables Spring's scheduled task execution capability, similar to\n * functionality found in Spring's {@code <task:*>} XML namespace. To be used\n * on {@link Configuration @Configuration} classes as follows:",
            "/**\n * 启用 Spring 定时任务执行能力，类似于 Spring {@code <task:*>} XML 命名空间中的功能。\n * 在 {@link Configuration @Configuration} 类上按如下方式使用：",
        ),
        (
            " * <p>This enables detection of {@link Scheduled @Scheduled} annotations on any\n * Spring-managed bean in the container. For example, given a class {@code MyTask}:",
            " * <p>这将在容器中任何 Spring 管理的 Bean 上检测 {@link Scheduled @Scheduled} 注解。\n * 例如给定类 {@code MyTask}：",
        ),
        (
            " * <p>the following configuration would ensure that {@code MyTask.work()} is called\n * once every 1000 ms:",
            " * <p>以下配置将确保每 1000 ms 调用一次 {@code MyTask.work()}：",
        ),
        (
            " * <p>Alternatively, if {@code MyTask} were annotated with {@code @Component}, the\n * following configuration would ensure that its {@code @Scheduled} method is\n * invoked at the desired interval:",
            " * <p>或者，若 {@code MyTask} 标注 {@code @Component}，\n * 以下配置将确保其 {@code @Scheduled} 方法按期望间隔调用：",
        ),
        (
            " * <p>Methods annotated with {@code @Scheduled} may even be declared directly within\n * {@code @Configuration} classes:",
            " * <p>标注 {@code @Scheduled} 的方法甚至可直接在 {@code @Configuration} 类中声明：",
        ),
        (
            " * <p>By default, Spring will search for an associated scheduler definition: either\n * a unique {@link org.springframework.scheduling.TaskScheduler} bean in the context,\n * or a {@code TaskScheduler} bean named \"taskScheduler\" otherwise; the same lookup\n * will also be performed for a {@link java.util.concurrent.ScheduledExecutorService}\n * bean. If neither of the two is resolvable, a local single-threaded default\n * scheduler will be created and used within the registrar.",
            " * <p>默认情况下，Spring 将查找关联的调度器定义：\n * 上下文中唯一的 {@link org.springframework.scheduling.TaskScheduler} Bean，\n * 否则名为 \"taskScheduler\" 的 {@code TaskScheduler} Bean；\n * 对 {@link java.util.concurrent.ScheduledExecutorService} Bean 也执行相同查找。\n * 若两者均不可解析，将在注册器内创建并使用本地单线程默认调度器。",
        ),
        (
            " * <p>When more control is desired, a {@code @Configuration} class may implement\n * {@link SchedulingConfigurer}. This allows access to the underlying\n * {@link ScheduledTaskRegistrar} instance. For example, the following example\n * demonstrates how to customize the {@link Executor} used to execute scheduled\n * tasks:",
            " * <p>需要更多控制时，{@code @Configuration} 类可实现 {@link SchedulingConfigurer}。\n * 这允许访问底层 {@link ScheduledTaskRegistrar} 实例。\n * 例如以下示例演示如何自定义执行定时任务的 {@link Executor}：",
        ),
        (
            " * <p>Note in the example above the use of {@code @Bean(destroyMethod=\"shutdown\")}.\n * This ensures that the task executor is properly shut down when the Spring\n * application context itself is closed.",
            " * <p>注意上例使用 {@code @Bean(destroyMethod=\"shutdown\")}，\n * 确保 Spring 应用上下文关闭时任务执行器正确关闭。",
        ),
        (
            " * <p>Implementing {@code SchedulingConfigurer} also allows for fine-grained\n * control over task registration via the {@code ScheduledTaskRegistrar}.\n * For example, the following configures the execution of a particular bean\n * method per a custom {@code Trigger} implementation:",
            " * <p>实现 {@code SchedulingConfigurer} 还可通过 {@code ScheduledTaskRegistrar}\n * 精细控制任务注册。例如以下配置按自定义 {@code Trigger} 实现\n * 执行特定 Bean 方法：",
        ),
        (
            " * <p>For reference, the example above can be compared to the following Spring XML\n * configuration:",
            " * <p>作为参考，上例可与以下 Spring XML 配置对比：",
        ),
        (
            " * <p>The examples are equivalent save that in XML a <em>fixed-rate</em> period is used\n * instead of a custom <em>{@code Trigger}</em> implementation; this is because the\n * {@code task:} namespace {@code scheduled} cannot easily expose such support. This is\n * but one demonstration how the code-based approach allows for maximum configurability\n * through direct access to the actual component.",
            " * <p>示例等价，仅 XML 使用<em>固定速率</em>周期而非自定义<em>{@code Trigger}</em> 实现；\n * 因为 {@code task:} 命名空间的 {@code scheduled} 不易暴露此类支持。\n * 这展示了基于代码的方式通过直接访问实际组件实现最大可配置性。",
        ),
        (
            " * <p><b>Note: {@code @EnableScheduling} applies to its local application context only,\n * allowing for selective scheduling of beans at different levels.</b> Please redeclare\n * {@code @EnableScheduling} in each individual context, for example, the common root web\n * application context and any separate {@code DispatcherServlet} application contexts,\n * if you need to apply its behavior at multiple levels.",
            " * <p><b>注意：{@code @EnableScheduling} 仅作用于其本地应用上下文，\n * 允许在不同层级选择性调度 Bean。</b> 若需在多个层级应用其行为，\n * 请在各独立上下文中重新声明 {@code @EnableScheduling}，\n * 例如公共根 Web 应用上下文及独立的 {@code DispatcherServlet} 应用上下文。",
        ),
    ],
    "Scheduled.java": [
        (
            "/**\n * Annotation that marks a method to be scheduled. For periodic tasks, exactly one\n * of the {@link #cron}, {@link #fixedDelay}, or {@link #fixedRate} attributes\n * must be specified, and additionally an optional {@link #initialDelay}.\n * For a one-time task, it is sufficient to just specify an {@link #initialDelay}.",
            "/**\n * 标记方法为定时任务的注解。对于周期性任务，须指定 {@link #cron}、\n * {@link #fixedDelay} 或 {@link #fixedRate} 之一，并可额外指定 {@link #initialDelay}。\n * 对于一次性任务，仅指定 {@link #initialDelay} 即可。",
        ),
        (
            " * <p>The annotated method must not accept arguments. It will typically have\n * a {@code void} return type; if not, the returned value will be ignored\n * when called through the scheduler.",
            " * <p>标注方法不得接受参数。通常返回类型为 {@code void}；\n * 否则通过调度器调用时将忽略返回值。",
        ),
        (
            " * <p>Methods that return a reactive {@code Publisher} or a type which can be adapted\n * to {@code Publisher} by the default {@code ReactiveAdapterRegistry} are supported.\n * The {@code Publisher} must support multiple subsequent subscriptions. The returned\n * {@code Publisher} is only produced once, and the scheduling infrastructure then\n * periodically subscribes to it according to configuration. Values emitted by\n * the publisher are ignored. Errors are logged at {@code WARN} level, which\n * doesn't prevent further iterations. If a fixed delay is configured, the\n * subscription is blocked in order to respect the fixed delay semantics.",
            " * <p>支持返回响应式 {@code Publisher} 或可经默认 {@code ReactiveAdapterRegistry}\n * 适配为 {@code Publisher} 类型的方法。{@code Publisher} 须支持多次后续订阅。\n * 返回的 {@code Publisher} 仅产生一次，调度基础设施随后按配置周期性订阅。\n * 发布者发出的值被忽略。错误以 {@code WARN} 级别记录，不阻止后续迭代。\n * 若配置固定延迟，订阅将被阻塞以遵守固定延迟语义。",
        ),
        (
            " * <p>Kotlin suspending functions are also supported, provided the coroutine-reactor\n * bridge ({@code kotlinx.coroutine.reactor}) is present at runtime. This bridge is\n * used to adapt the suspending function to a {@code Publisher} which is treated\n * the same way as in the reactive method case (see above).",
            " * <p>也支持 Kotlin 挂起函数，前提是运行时存在协程-reactor 桥接\n * （{@code kotlinx.coroutine.reactor}）。该桥接将挂起函数适配为 {@code Publisher}，\n * 处理方式与响应式方法情况相同（见上文）。",
        ),
        (
            " * <p>Processing of {@code @Scheduled} annotations is performed by registering a\n * {@link ScheduledAnnotationBeanPostProcessor}. This can be done manually or,\n * more conveniently, through the {@code <task:annotation-driven/>} XML element\n * or {@link EnableScheduling @EnableScheduling} annotation.",
            " * <p>{@code @Scheduled} 注解的处理通过注册 {@link ScheduledAnnotationBeanPostProcessor} 完成。\n * 可手动注册，或更方便地通过 {@code <task:annotation-driven/>} XML 元素\n * 或 {@link EnableScheduling @EnableScheduling} 注解。",
        ),
        (
            " * <p>This annotation can be used as a <em>{@linkplain Repeatable repeatable}</em>\n * annotation. If several scheduled declarations are found on the same method,\n * each of them will be processed independently, with a separate trigger firing\n * for each of them. As a consequence, such co-located schedules may overlap\n * and execute multiple times in parallel or in immediate succession.",
            " * <p>本注解可作为<em>{@linkplain Repeatable 可重复}</em>注解使用。\n * 若同一方法上存在多个定时声明，将独立处理，各自触发独立触发器。\n * 因此此类共存调度可能重叠，并行或连续多次执行。",
        ),
        (
            " * <p>This annotation may be used as a <em>meta-annotation</em> to create custom\n * <em>composed annotations</em> with attribute overrides.",
            " * <p>本注解可作为<em>元注解</em>创建带属性覆盖的自定义<em>组合注解</em>。",
        ),
        (
            "\t/**\n\t * A special cron expression value that indicates a disabled trigger: {@value}.\n\t * <p>This is primarily meant for use with <code>${...}</code> placeholders,\n\t * allowing for external disabling of corresponding scheduled methods.\n\t * @since 5.1\n\t * @see ScheduledTaskRegistrar#CRON_DISABLED\n\t */",
            "\t/**\n\t * 表示禁用触发器的特殊 cron 表达式值：{@value}。\n\t * <p>主要用于 <code>${...}</code> 占位符，\n\t * 允许从外部禁用对应定时方法。\n\t * @since 5.1\n\t * @see ScheduledTaskRegistrar#CRON_DISABLED\n\t */",
        ),
        (
            "\t/**\n\t * A cron-like expression, extending the usual UN*X definition to include triggers\n\t * on the second, minute, hour, day of month, month, and day of week.",
            "\t/**\n\t * 类 cron 表达式，扩展常规 UN*X 定义，包含秒、分、时、日、月、周触发。",
        ),
        (
            "\t * <p>For example, {@code \"0 * * * * MON-FRI\"} means once per minute on weekdays\n\t * (at the top of the minute - the 0th second).",
            "\t * <p>例如 {@code \"0 * * * * MON-FRI\"} 表示工作日每分钟一次（整分第 0 秒）。",
        ),
        (
            "\t * <p>The fields read from left to right are interpreted as follows.",
            "\t * <p>从左到右各字段含义如下。",
        ),
        (
            "\t * <p>The special value {@link #CRON_DISABLED \"-\"} indicates a disabled cron\n\t * trigger, primarily meant for externally specified values resolved by a\n\t * <code>${...}</code> placeholder.\n\t * @return an expression that can be parsed to a cron schedule",
            "\t * <p>特殊值 {@link #CRON_DISABLED \"-\"} 表示禁用的 cron 触发器，\n\t * 主要用于由 <code>${...}</code> 占位符解析的外部指定值。\n\t * @return 可解析为 cron 调度的表达式",
        ),
        (
            "\t/**\n\t * A time zone for which the cron expression will be resolved. By default, this\n\t * attribute is the empty String (i.e. the scheduler's time zone will be used).\n\t * @return a zone id accepted by {@link java.util.TimeZone#getTimeZone(String)},\n\t * or an empty String to indicate the scheduler's default time zone",
            "\t/**\n\t * cron 表达式解析使用的时区。默认为空字符串（即使用调度器时区）。\n\t * @return {@link java.util.TimeZone#getTimeZone(String)} 接受的区域 ID，\n\t * 或空字符串表示调度器默认时区",
        ),
        (
            "\t/**\n\t * Execute the annotated method with a fixed period between invocations.\n\t * <p>The time unit is milliseconds by default but can be overridden via\n\t * {@link #timeUnit}.\n\t * @return the period\n\t */",
            "\t/**\n\t * 以固定周期执行标注方法（两次调用间隔固定）。\n\t * <p>时间单位默认为毫秒，可通过 {@link #timeUnit} 覆盖。\n\t * @return 周期\n\t */",
        ),
        (
            "\t/**\n\t * Execute the annotated method with a fixed period between invocations.\n\t * <p>The duration String can be in several formats:",
            "\t/**\n\t * 以固定周期执行标注方法。\n\t * <p>持续时间字符串可为多种格式：",
        ),
        (
            "\t * @return the period as a String value &mdash; for example a placeholder,\n\t * or a {@link org.springframework.format.annotation.DurationFormat.Style#ISO8601 java.time.Duration} compliant value\n\t * or a {@link org.springframework.format.annotation.DurationFormat.Style#SIMPLE simple format} compliant value\n\t * @since 3.2.2\n\t * @see #fixedRate()\n\t */",
            "\t * @return 周期字符串值 &mdash; 例如占位符、\n\t * {@link org.springframework.format.annotation.DurationFormat.Style#ISO8601 java.time.Duration} 兼容值\n\t * 或 {@link org.springframework.format.annotation.DurationFormat.Style#SIMPLE 简单格式}兼容值\n\t * @since 3.2.2\n\t * @see #fixedRate()\n\t */",
        ),
        (
            "\t/**\n\t * Execute the annotated method with a fixed period between the end of the\n\t * last invocation and the start of the next.\n\t * <p>The time unit is milliseconds by default but can be overridden via\n\t * {@link #timeUnit}.\n\t * <p><b>NOTE: With virtual threads, fixed rates and cron triggers are recommended\n\t * over fixed delays.</b> Fixed-delay tasks operate on a single scheduler thread\n\t * with {@link org.springframework.scheduling.concurrent.SimpleAsyncTaskScheduler}.\n\t * @return the delay\n\t */",
            "\t/**\n\t * 在上次调用结束与下次调用开始之间以固定延迟执行标注方法。\n\t * <p>时间单位默认为毫秒，可通过 {@link #timeUnit} 覆盖。\n\t * <p><b>注意：使用虚拟线程时，推荐固定速率与 cron 触发器而非固定延迟。</b>\n\t * 固定延迟任务在 {@link org.springframework.scheduling.concurrent.SimpleAsyncTaskScheduler}\n\t * 的单调度器线程上运行。\n\t * @return 延迟\n\t */",
        ),
        (
            "\t * @return the delay as a String value &mdash; for example a placeholder,\n\t * or a {@link org.springframework.format.annotation.DurationFormat.Style#ISO8601 java.time.Duration} compliant value\n\t * or a {@link org.springframework.format.annotation.DurationFormat.Style#SIMPLE simple format} compliant value\n\t * @since 3.2.2\n\t * @see #fixedDelay()\n\t */",
            "\t * @return 延迟字符串值 &mdash; 例如占位符、\n\t * {@link org.springframework.format.annotation.DurationFormat.Style#ISO8601 java.time.Duration} 兼容值\n\t * 或 {@link org.springframework.format.annotation.DurationFormat.Style#SIMPLE 简单格式}兼容值\n\t * @since 3.2.2\n\t * @see #fixedDelay()\n\t */",
        ),
        (
            "\t/**\n\t * Number of units of time to delay before the first execution of a\n\t * {@link #fixedRate} or {@link #fixedDelay} task.\n\t * <p>The time unit is milliseconds by default but can be overridden via\n\t * {@link #timeUnit}.\n\t * @return the initial\n\t * @since 3.2\n\t */",
            "\t/**\n\t * {@link #fixedRate} 或 {@link #fixedDelay} 任务首次执行前的延迟时间单位数。\n\t * <p>时间单位默认为毫秒，可通过 {@link #timeUnit} 覆盖。\n\t * @return 初始延迟\n\t * @since 3.2\n\t */",
        ),
        (
            "\t * @return the initial delay as a String value &mdash; for example a placeholder,\n\t * or a {@link org.springframework.format.annotation.DurationFormat.Style#ISO8601 java.time.Duration} compliant value\n\t * or a {@link org.springframework.format.annotation.DurationFormat.Style#SIMPLE simple format} compliant value\n\t * @since 3.2.2\n\t * @see #initialDelay()\n\t */",
            "\t * @return 初始延迟字符串值 &mdash; 例如占位符、\n\t * {@link org.springframework.format.annotation.DurationFormat.Style#ISO8601 java.time.Duration} 兼容值\n\t * 或 {@link org.springframework.format.annotation.DurationFormat.Style#SIMPLE 简单格式}兼容值\n\t * @since 3.2.2\n\t * @see #initialDelay()\n\t */",
        ),
        (
            "\t/**\n\t * The {@link TimeUnit} to use for {@link #fixedDelay}, {@link #fixedDelayString},\n\t * {@link #fixedRate}, {@link #fixedRateString}, {@link #initialDelay}, and\n\t * {@link #initialDelayString}.\n\t * <p>The default is {@link TimeUnit#MILLISECONDS}.\n\t * <p>This attribute is ignored for {@linkplain #cron() cron expressions}\n\t * and for {@link java.time.Duration} values supplied via {@link #fixedDelayString},\n\t * {@link #fixedRateString}, or {@link #initialDelayString}.\n\t * @return the {@code TimeUnit} to use\n\t * @since 5.3.10\n\t */",
            "\t/**\n\t * {@link #fixedDelay}、{@link #fixedDelayString}、{@link #fixedRate}、\n\t * {@link #fixedRateString}、{@link #initialDelay} 及 {@link #initialDelayString}\n\t * 使用的 {@link TimeUnit}。\n\t * <p>默认为 {@link TimeUnit#MILLISECONDS}。\n\t * <p>对 {@linkplain #cron() cron 表达式}及通过 {@link #fixedDelayString}、\n\t * {@link #fixedRateString} 或 {@link #initialDelayString} 提供的\n\t * {@link java.time.Duration} 值，本属性被忽略。\n\t * @return 使用的 {@code TimeUnit}\n\t * @since 5.3.10\n\t */",
        ),
        (
            "\t/**\n\t * A qualifier for determining a scheduler to run this scheduled method on.\n\t * <p>Defaults to an empty String, suggesting the default scheduler.\n\t * <p>May be used to determine the target scheduler to be used,\n\t * matching the qualifier value (or the bean name) of a specific\n\t * {@link org.springframework.scheduling.TaskScheduler} or\n\t * {@link java.util.concurrent.ScheduledExecutorService} bean definition.\n\t * @since 6.1\n\t * @see org.springframework.scheduling.SchedulingAwareRunnable#getQualifier()\n\t */",
            "\t/**\n\t * 确定运行此定时方法的调度器的限定符。\n\t * <p>默认为空字符串，表示默认调度器。\n\t * <p>可用于确定目标调度器，\n\t * 匹配特定 {@link org.springframework.scheduling.TaskScheduler} 或\n\t * {@link java.util.concurrent.ScheduledExecutorService} Bean 定义的限定符值（或 Bean 名称）。\n\t * @since 6.1\n\t * @see org.springframework.scheduling.SchedulingAwareRunnable#getQualifier()\n\t */",
        ),
    ],
    "ScheduledAnnotationReactiveSupport.java": [
        (
            "/**\n * Helper class for @{@link ScheduledAnnotationBeanPostProcessor} to support reactive\n * cases without a dependency on optional classes.\n *\n * @author Simon Baslé\n * @author Brian Clozel\n * @since 6.1\n */",
            "/**\n * 供 @{@link ScheduledAnnotationBeanPostProcessor} 使用的辅助类，\n * 在不依赖可选类的情况下支持响应式场景。\n *\n * @author Simon Baslé\n * @author Brian Clozel\n * @since 6.1\n */",
        ),
        (
            "\t/**\n\t * Checks that if the method is reactive, it can be scheduled. Methods are considered\n\t * eligible for reactive scheduling if they either return an instance of a type that\n\t * can be converted to {@code Publisher} or are a Kotlin suspending function.\n\t * If the method doesn't match these criteria, this check returns {@code false}.\n\t * <p>For scheduling of Kotlin suspending functions, the Coroutine-Reactor bridge\n\t * {@code kotlinx.coroutines.reactor} must be present at runtime (in order to invoke\n\t * suspending functions as a {@code Publisher}). Provided that is the case, this\n\t * method returns {@code true}. Otherwise, it throws an {@code IllegalStateException}.\n\t * @throws IllegalStateException if the method is reactive but Reactor and/or the\n\t * Kotlin coroutines bridge are not present at runtime\n\t */",
            "\t/**\n\t * 检查响应式方法是否可调度。若方法返回可转换为 {@code Publisher} 的类型\n\t * 或为 Kotlin 挂起函数，则视为符合响应式调度条件。\n\t * 若不符合，返回 {@code false}。\n\t * <p>调度 Kotlin 挂起函数时，运行时须存在 Coroutine-Reactor 桥接\n\t * {@code kotlinx.coroutines.reactor}（以 {@code Publisher} 形式调用挂起函数）。\n\t * 满足条件时返回 {@code true}，否则抛出 {@code IllegalStateException}。\n\t * @throws IllegalStateException 方法为响应式但运行时缺少 Reactor 和/或 Kotlin 协程桥接\n\t */",
        ),
        (
            "\t/**\n\t * Create a {@link Runnable} for the Scheduled infrastructure, allowing for scheduled\n\t * subscription to the publisher produced by a reactive method.\n\t * <p>Note that the reactive method is invoked once, but the resulting {@code Publisher}\n\t * is subscribed to repeatedly, once per each invocation of the {@code Runnable}.\n\t * <p>In the case of a fixed-delay configuration, the subscription inside the\n\t * {@link Runnable} is turned into a blocking call in order to maintain fixed-delay\n\t * semantics (i.e. the task blocks until completion of the Publisher, and the\n\t * delay is applied until the next iteration).\n\t */",
            "\t/**\n\t * 为定时基础设施创建 {@link Runnable}，允许对响应式方法产生的 Publisher 进行定时订阅。\n\t * <p>响应式方法仅调用一次，但产生的 {@code Publisher} 在每次 {@code Runnable}\n\t * 调用时重复订阅。\n\t * <p>固定延迟配置下，{@link Runnable} 内的订阅转为阻塞调用以维持固定延迟语义\n\t * （任务阻塞直至 Publisher 完成，延迟应用于下次迭代前）。\n\t */",
        ),
        (
            "\t/**\n\t * Turn the invocation of the provided {@code Method} into a {@code Publisher},\n\t * either by reflectively invoking it and converting the result to a {@code Publisher}\n\t * via {@link ReactiveAdapterRegistry} or by converting a Kotlin suspending function\n\t * into a {@code Publisher} via {@link CoroutinesUtils}.\n\t * <p>The {@link #isReactive(Method)} check is a precondition to calling this method.\n\t * If Reactor is present at runtime, the {@code Publisher} is additionally converted\n\t * to a {@code Flux} with a checkpoint String, allowing for better debugging.\n\t */",
            "\t/**\n\t * 将给定 {@code Method} 的调用转为 {@code Publisher}：\n\t * 反射调用并通过 {@link ReactiveAdapterRegistry} 转换结果，\n\t * 或通过 {@link CoroutinesUtils} 将 Kotlin 挂起函数转为 {@code Publisher}。\n\t * <p>调用本方法前须通过 {@link #isReactive(Method)} 检查。\n\t * 若运行时存在 Reactor，{@code Publisher}  additionally 转为带 checkpoint 字符串的\n\t * {@code Flux}，便于调试。\n\t */",
        ),
        (
            "\t/**\n\t * Utility implementation of {@code Runnable} that subscribes to a {@code Publisher}\n\t * or subscribes-then-blocks if {@code shouldBlock} is set to {@code true}.\n\t */",
            "\t/**\n\t * 订阅 {@code Publisher} 的 {@code Runnable} 工具实现；\n\t * 若 {@code shouldBlock} 为 {@code true} 则订阅后阻塞。\n\t */",
        ),
        (
            "\t/**\n\t * A {@code Subscriber} which keeps track of its {@code Subscription} and exposes the\n\t * capacity to cancel the subscription as a {@code Runnable}. Can optionally support\n\t * blocking if a {@code CountDownLatch} is supplied during construction.\n\t */",
            "\t/**\n\t * 跟踪其 {@code Subscription} 并将取消订阅能力暴露为 {@code Runnable} 的\n\t * {@code Subscriber}。构造时提供 {@code CountDownLatch} 时可选择支持阻塞。\n\t */",
        ),
    ],
    "ConcurrentTaskExecutor.java": [
        (
            "/**\n * Adapter that takes a {@code java.util.concurrent.Executor} and exposes\n * a Spring {@link org.springframework.core.task.TaskExecutor} for it.\n * Also detects an extended {@code java.util.concurrent.ExecutorService}, adapting\n * the {@link org.springframework.core.task.AsyncTaskExecutor} interface accordingly.",
            "/**\n * 接受 {@code java.util.concurrent.Executor} 并为其暴露\n * Spring {@link org.springframework.core.task.TaskExecutor} 的适配器。\n * 也检测扩展的 {@code java.util.concurrent.ExecutorService}，\n * 相应适配 {@link org.springframework.core.task.AsyncTaskExecutor} 接口。",
        ),
        (
            " * <p>Autodetects a JSR-236 {@link jakarta.enterprise.concurrent.ManagedExecutorService}\n * in order to expose {@link jakarta.enterprise.concurrent.ManagedTask} adapters for it,\n * exposing a long-running hint based on {@link SchedulingAwareRunnable} and an identity\n * name based on the given Runnable/Callable's {@code toString()}. For JSR-236 style\n * lookup in a Jakarta EE environment, consider using {@link DefaultManagedTaskExecutor}.",
            " * <p>自动检测 JSR-236 {@link jakarta.enterprise.concurrent.ManagedExecutorService}，\n * 为其暴露 {@link jakarta.enterprise.concurrent.ManagedTask} 适配器，\n * 基于 {@link SchedulingAwareRunnable} 提供 long-running 提示，\n * 基于给定 Runnable/Callable 的 {@code toString()} 提供 identity 名称。\n * 在 Jakarta EE 环境中进行 JSR-236 风格查找，请考虑 {@link DefaultManagedTaskExecutor}。",
        ),
        (
            " * <p>Note that there is a pre-built {@link ThreadPoolTaskExecutor} that allows\n * for defining a {@link java.util.concurrent.ThreadPoolExecutor} in bean style,\n * exposing it as a Spring {@link org.springframework.core.task.TaskExecutor} directly.\n * This is a convenient alternative to a raw ThreadPoolExecutor definition with\n * a separate definition of the present adapter class.",
            " * <p>注意存在预构建的 {@link ThreadPoolTaskExecutor}，\n * 允许以 Bean 风格定义 {@link java.util.concurrent.ThreadPoolExecutor}，\n * 直接暴露为 Spring {@link org.springframework.core.task.TaskExecutor}。\n * 这比原始 ThreadPoolExecutor 定义加单独本适配器类定义更方便。",
        ),
        (
            "\t/**\n\t * Create a new ConcurrentTaskExecutor, using a single thread executor as default.\n\t * @see java.util.concurrent.Executors#newSingleThreadExecutor()\n\t * @deprecated in favor of {@link #ConcurrentTaskExecutor(Executor)} with an\n\t * externally provided Executor\n\t */",
            "\t/**\n\t * 创建新的 ConcurrentTaskExecutor，默认使用单线程执行器。\n\t * @see java.util.concurrent.Executors#newSingleThreadExecutor()\n\t * @deprecated 请使用带外部提供 Executor 的 {@link #ConcurrentTaskExecutor(Executor)}\n\t */",
        ),
        (
            "\t/**\n\t * Create a new ConcurrentTaskExecutor, using the given {@link java.util.concurrent.Executor}.\n\t * <p>Autodetects a JSR-236 {@link jakarta.enterprise.concurrent.ManagedExecutorService}\n\t * in order to expose {@link jakarta.enterprise.concurrent.ManagedTask} adapters for it.\n\t * @param executor the {@link java.util.concurrent.Executor} to delegate to\n\t */",
            "\t/**\n\t * 使用给定 {@link java.util.concurrent.Executor} 创建新的 ConcurrentTaskExecutor。\n\t * <p>自动检测 JSR-236 {@link jakarta.enterprise.concurrent.ManagedExecutorService}，\n\t * 为其暴露 {@link jakarta.enterprise.concurrent.ManagedTask} 适配器。\n\t * @param executor 委托的 {@link java.util.concurrent.Executor}\n\t */",
        ),
        (
            "\t/**\n\t * Specify the {@link java.util.concurrent.Executor} to delegate to.\n\t * <p>Autodetects a JSR-236 {@link jakarta.enterprise.concurrent.ManagedExecutorService}\n\t * in order to expose {@link jakarta.enterprise.concurrent.ManagedTask} adapters for it.\n\t */",
            "\t/**\n\t * 指定委托的 {@link java.util.concurrent.Executor}。\n\t * <p>自动检测 JSR-236 {@link jakarta.enterprise.concurrent.ManagedExecutorService}，\n\t * 为其暴露 {@link jakarta.enterprise.concurrent.ManagedTask} 适配器。\n\t */",
        ),
        (
            "\t/**\n\t * Return the {@link java.util.concurrent.Executor} that this adapter delegates to.\n\t */",
            "\t/**\n\t * 返回本适配器委托的 {@link java.util.concurrent.Executor}。\n\t */",
        ),
        (
            "\t/**\n\t * Specify a custom {@link TaskDecorator} to be applied to any {@link Runnable}\n\t * about to be executed.\n\t * <p>Note that such a decorator is not necessarily being applied to the\n\t * user-supplied {@code Runnable}/{@code Callable} but rather to the actual\n\t * execution callback (which may be a wrapper around the user-supplied task).\n\t * <p>The primary use case is to set some execution context around the task's\n\t * invocation, or to provide some monitoring/statistics for task execution.\n\t * @since 4.3\n\t */",
            "\t/**\n\t * 指定应用于即将执行的 {@link Runnable} 的自定义 {@link TaskDecorator}。\n\t * <p>注意装饰器不一定应用于用户提供的 {@code Runnable}/{@code Callable}，\n\t * 而是实际执行回调（可能是用户任务的包装）。\n\t * <p>主要用例是在任务调用周围设置执行上下文，或提供任务执行监控/统计。\n\t * @since 4.3\n\t */",
        ),
        (
            "\t/**\n\t * TaskExecutorAdapter subclass that wraps all provided Runnables and Callables\n\t * with a JSR-236 ManagedTask, exposing a long-running hint based on\n\t * {@link SchedulingAwareRunnable} and an identity name based on the task's\n\t * {@code toString()} representation.\n\t */",
            "\t/**\n\t * TaskExecutorAdapter 子类，将所有提供的 Runnable 与 Callable\n\t * 包装为 JSR-236 ManagedTask，基于 {@link SchedulingAwareRunnable} 提供 long-running 提示，\n\t * 基于任务 {@code toString()} 表示提供 identity 名称。\n\t */",
        ),
        (
            "\t/**\n\t * Delegate that wraps a given Runnable/Callable  with a JSR-236 ManagedTask,\n\t * exposing a long-running hint based on {@link SchedulingAwareRunnable}\n\t * and a given identity name.\n\t */",
            "\t/**\n\t * 将给定 Runnable/Callable 包装为 JSR-236 ManagedTask 的委托，\n\t * 基于 {@link SchedulingAwareRunnable} 提供 long-running 提示及给定 identity 名称。\n\t */",
        ),
    ],
    "ConcurrentTaskScheduler.java": [
        (
            "/**\n * Adapter that takes a {@code java.util.concurrent.ScheduledExecutorService} and\n * exposes a Spring {@link org.springframework.scheduling.TaskScheduler} for it.\n * Extends {@link ConcurrentTaskExecutor} in order to implement the\n * {@link org.springframework.scheduling.SchedulingTaskExecutor} interface as well.",
            "/**\n * 接受 {@code java.util.concurrent.ScheduledExecutorService} 并为其暴露\n * Spring {@link org.springframework.scheduling.TaskScheduler} 的适配器。\n * 扩展 {@link ConcurrentTaskExecutor} 以同时实现\n * {@link org.springframework.scheduling.SchedulingTaskExecutor} 接口。",
        ),
        (
            " * <p>Autodetects a JSR-236 {@link jakarta.enterprise.concurrent.ManagedScheduledExecutorService}\n * in order to use it for trigger-based scheduling if possible, instead of Spring's\n * local trigger management which ends up delegating to regular delay-based scheduling\n * against the {@code java.util.concurrent.ScheduledExecutorService} API. For JSR-236 style\n * lookup in a Jakarta EE environment, consider using {@link DefaultManagedTaskScheduler}.",
            " * <p>自动检测 JSR-236 {@link jakarta.enterprise.concurrent.ManagedScheduledExecutorService}，\n * 若可能则用于基于 Trigger 的调度，而非 Spring 本地 Trigger 管理\n * （最终委托给 {@code java.util.concurrent.ScheduledExecutorService} API 的常规延迟调度）。\n * 在 Jakarta EE 环境中进行 JSR-236 风格查找，请考虑 {@link DefaultManagedTaskScheduler}。",
        ),
        (
            " * <p>Note that there is a pre-built {@link ThreadPoolTaskScheduler} that allows for\n * defining a {@link java.util.concurrent.ScheduledThreadPoolExecutor} in bean style,\n * exposing it as a Spring {@link org.springframework.scheduling.TaskScheduler} directly.\n * This is a convenient alternative to a raw ScheduledThreadPoolExecutor definition with\n * a separate definition of the present adapter class.",
            " * <p>注意存在预构建的 {@link ThreadPoolTaskScheduler}，\n * 允许以 Bean 风格定义 {@link java.util.concurrent.ScheduledThreadPoolExecutor}，\n * 直接暴露为 Spring {@link org.springframework.scheduling.TaskScheduler}。\n * 这比原始 ScheduledThreadPoolExecutor 定义加单独本适配器类定义更方便。",
        ),
        (
            "\t/**\n\t * Create a new ConcurrentTaskScheduler,\n\t * using a single thread executor as default.\n\t * @see java.util.concurrent.Executors#newSingleThreadScheduledExecutor()\n\t * @deprecated in favor of {@link #ConcurrentTaskScheduler(ScheduledExecutorService)}\n\t * with an externally provided Executor\n\t */",
            "\t/**\n\t * 创建新的 ConcurrentTaskScheduler，默认使用单线程执行器。\n\t * @see java.util.concurrent.Executors#newSingleThreadScheduledExecutor()\n\t * @deprecated 请使用带外部提供 Executor 的\n\t * {@link #ConcurrentTaskScheduler(ScheduledExecutorService)}\n\t */",
        ),
        (
            "\t/**\n\t * Create a new ConcurrentTaskScheduler, using the given\n\t * {@link java.util.concurrent.ScheduledExecutorService} as shared delegate.\n\t * <p>Autodetects a JSR-236 {@link jakarta.enterprise.concurrent.ManagedScheduledExecutorService}\n\t * in order to use it for trigger-based scheduling if possible,\n\t * instead of Spring's local trigger management.\n\t * @param scheduledExecutor the {@link java.util.concurrent.ScheduledExecutorService}\n\t * to delegate to for {@link org.springframework.scheduling.SchedulingTaskExecutor}\n\t * as well as {@link TaskScheduler} invocations\n\t */",
            "\t/**\n\t * 使用给定 {@link java.util.concurrent.ScheduledExecutorService} 作为共享委托\n\t * 创建新的 ConcurrentTaskScheduler。\n\t * <p>自动检测 JSR-236 {@link jakarta.enterprise.concurrent.ManagedScheduledExecutorService}，\n\t * 若可能则用于基于 Trigger 的调度，而非 Spring 本地 Trigger 管理。\n\t * @param scheduledExecutor 委托的 {@link java.util.concurrent.ScheduledExecutorService}，\n\t * 用于 {@link org.springframework.scheduling.SchedulingTaskExecutor} 及 {@link TaskScheduler} 调用\n\t */",
        ),
        (
            "\t/**\n\t * Create a new ConcurrentTaskScheduler, using the given {@link java.util.concurrent.Executor}\n\t * and {@link java.util.concurrent.ScheduledExecutorService} as delegates.\n\t * <p>Autodetects a JSR-236 {@link jakarta.enterprise.concurrent.ManagedScheduledExecutorService}\n\t * in order to use it for trigger-based scheduling if possible,\n\t * instead of Spring's local trigger management.\n\t * @param concurrentExecutor the {@link java.util.concurrent.Executor} to delegate to\n\t * for {@link org.springframework.scheduling.SchedulingTaskExecutor} invocations\n\t * @param scheduledExecutor the {@link java.util.concurrent.ScheduledExecutorService}\n\t * to delegate to for {@link TaskScheduler} invocations\n\t */",
            "\t/**\n\t * 使用给定 {@link java.util.concurrent.Executor} 与\n\t * {@link java.util.concurrent.ScheduledExecutorService} 作为委托创建新的 ConcurrentTaskScheduler。\n\t * <p>自动检测 JSR-236 {@link jakarta.enterprise.concurrent.ManagedScheduledExecutorService}，\n\t * 若可能则用于基于 Trigger 的调度。\n\t * @param concurrentExecutor 委托的 {@link java.util.concurrent.Executor}，\n\t * 用于 {@link org.springframework.scheduling.SchedulingTaskExecutor} 调用\n\t * @param scheduledExecutor 委托的 {@link java.util.concurrent.ScheduledExecutorService}，\n\t * 用于 {@link TaskScheduler} 调用\n\t */",
        ),
        (
            "\t/**\n\t * Specify the {@link java.util.concurrent.ScheduledExecutorService} to delegate to.\n\t * <p>Autodetects a JSR-236 {@link jakarta.enterprise.concurrent.ManagedScheduledExecutorService}\n\t * in order to use it for trigger-based scheduling if possible,\n\t * instead of Spring's local trigger management.\n\t * <p>Note: This will only apply to {@link TaskScheduler} invocations.\n\t * If you want the given executor to apply to\n\t * {@link org.springframework.scheduling.SchedulingTaskExecutor} invocations\n\t * as well, pass the same executor reference to {@link #setConcurrentExecutor}.\n\t * @see #setConcurrentExecutor\n\t */",
            "\t/**\n\t * 指定委托的 {@link java.util.concurrent.ScheduledExecutorService}。\n\t * <p>自动检测 JSR-236 {@link jakarta.enterprise.concurrent.ManagedScheduledExecutorService}，\n\t * 若可能则用于基于 Trigger 的调度。\n\t * <p>注意：这仅适用于 {@link TaskScheduler} 调用。\n\t * 若希望给定执行器也应用于 {@link org.springframework.scheduling.SchedulingTaskExecutor} 调用，\n\t * 请将同一执行器引用传给 {@link #setConcurrentExecutor}。\n\t * @see #setConcurrentExecutor\n\t */",
        ),
        (
            "\t/**\n\t * Provide an {@link ErrorHandler} strategy.\n\t */",
            "\t/**\n\t * 提供 {@link ErrorHandler} 策略。\n\t */",
        ),
        (
            "\t/**\n\t * Set the clock to use for scheduling purposes.\n\t * <p>The default clock is the system clock for the default time zone.\n\t * @since 5.3\n\t * @see Clock#systemDefaultZone()\n\t */",
            "\t/**\n\t * 设置调度使用的时钟。\n\t * <p>默认为默认时区的系统时钟。\n\t * @since 5.3\n\t * @see Clock#systemDefaultZone()\n\t */",
        ),
        (
            "\t/**\n\t * Delegate that adapts a Spring Trigger to a JSR-236 Trigger.\n\t * Separated into an inner class in order to avoid a hard dependency on the JSR-236 API.\n\t */",
            "\t/**\n\t * 将 Spring Trigger 适配为 JSR-236 Trigger 的委托。\n\t * 分离为内部类以避免对 JSR-236 API 的硬依赖。\n\t */",
        ),
    ],
}
