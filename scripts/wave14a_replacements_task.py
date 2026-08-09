"""Chinese JavaDoc replacements for Spring Boot wave14a task executor/scheduler builders."""

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "SimpleAsyncTaskExecutorCustomizer.java": [
        (
            "/**\n * Callback interface that can be used to customize a {@link SimpleAsyncTaskExecutor}.\n *\n * @author Stephane Nicoll\n * @author Moritz Halbritter\n * @since 3.2.0\n * @see SimpleAsyncTaskExecutorBuilder\n */",
            "/**\n * 用于自定义 {@link SimpleAsyncTaskExecutor} 的回调接口。\n *\n * @author Stephane Nicoll\n * @author Moritz Halbritter\n * @since 3.2.0\n * @see SimpleAsyncTaskExecutorBuilder\n */",
        ),
        (
            "\t/**\n\t * Callback to customize a {@link SimpleAsyncTaskExecutor} instance.\n\t * @param taskExecutor the task executor to customize\n\t */",
            "\t/**\n\t * 自定义 {@link SimpleAsyncTaskExecutor} 实例的回调。\n\t *\n\t * @param taskExecutor the task executor to customize 待自定义的任务执行器\n\t */",
        ),
    ],
    "SimpleAsyncTaskSchedulerCustomizer.java": [
        (
            "/**\n * Callback interface that can be used to customize a {@link SimpleAsyncTaskScheduler}.\n *\n * @author Moritz Halbritter\n * @since 3.2.0\n */",
            "/**\n * 用于自定义 {@link SimpleAsyncTaskScheduler} 的回调接口。\n *\n * @author Moritz Halbritter\n * @since 3.2.0\n */",
        ),
        (
            "\t/**\n\t * Callback to customize a {@link SimpleAsyncTaskScheduler} instance.\n\t * @param taskScheduler the task scheduler to customize\n\t */",
            "\t/**\n\t * 自定义 {@link SimpleAsyncTaskScheduler} 实例的回调。\n\t *\n\t * @param taskScheduler the task scheduler to customize 待自定义的任务调度器\n\t */",
        ),
    ],
    "ThreadPoolTaskExecutorCustomizer.java": [
        (
            "/**\n * Callback interface that can be used to customize a {@link ThreadPoolTaskExecutor}.\n *\n * @author Stephane Nicoll\n * @since 3.2.0\n * @see ThreadPoolTaskExecutorBuilder\n */",
            "/**\n * 用于自定义 {@link ThreadPoolTaskExecutor} 的回调接口。\n *\n * @author Stephane Nicoll\n * @since 3.2.0\n * @see ThreadPoolTaskExecutorBuilder\n */",
        ),
        (
            "\t/**\n\t * Callback to customize a {@link ThreadPoolTaskExecutor} instance.\n\t * @param taskExecutor the task executor to customize\n\t */",
            "\t/**\n\t * 自定义 {@link ThreadPoolTaskExecutor} 实例的回调。\n\t *\n\t * @param taskExecutor the task executor to customize 待自定义的任务执行器\n\t */",
        ),
    ],
    "ThreadPoolTaskSchedulerCustomizer.java": [
        (
            "/**\n * Callback interface that can be used to customize a {@link ThreadPoolTaskScheduler}.\n *\n * @author Stephane Nicoll\n * @since 3.2.0\n */",
            "/**\n * 用于自定义 {@link ThreadPoolTaskScheduler} 的回调接口。\n *\n * @author Stephane Nicoll\n * @since 3.2.0\n */",
        ),
        (
            "\t/**\n\t * Callback to customize a {@link ThreadPoolTaskScheduler} instance.\n\t * @param taskScheduler the task scheduler to customize\n\t */",
            "\t/**\n\t * 自定义 {@link ThreadPoolTaskScheduler} 实例的回调。\n\t *\n\t * @param taskScheduler the task scheduler to customize 待自定义的任务调度器\n\t */",
        ),
    ],
}

# Builder class replacements appended below in same dict
FILE_REPLACEMENTS.update({
    "SimpleAsyncTaskExecutorBuilder.java": [
        (
            "/**\n * Builder that can be used to configure and create a {@link SimpleAsyncTaskExecutor}.\n * Provides convenience methods to set common {@link SimpleAsyncTaskExecutor} settings and\n * register {@link #taskDecorator(TaskDecorator)}). For advanced configuration, consider\n * using {@link SimpleAsyncTaskExecutorCustomizer}.\n * <p>\n * In a typical auto-configured Spring Boot application this builder is available as a\n * bean and can be injected whenever a {@link SimpleAsyncTaskExecutor} is needed.\n *\n * @author Stephane Nicoll\n * @author Filip Hrisafov\n * @author Moritz Halbritter\n * @author Yanming Zhou\n * @since 3.2.0\n */",
            "/**\n * 用于配置并创建 {@link SimpleAsyncTaskExecutor} 的 Builder。\n * 提供便捷方法设置常用 {@link SimpleAsyncTaskExecutor} 参数并注册\n * {@link #taskDecorator(TaskDecorator)}。高级配置可使用 {@link SimpleAsyncTaskExecutorCustomizer}。\n * <p>\n * 在典型的 Spring Boot 自动配置应用中，此 Builder 作为 Bean 可用，\n * 需要 {@link SimpleAsyncTaskExecutor} 时可注入。\n *\n * @author Stephane Nicoll\n * @author Filip Hrisafov\n * @author Moritz Halbritter\n * @author Yanming Zhou\n * @since 3.2.0\n */",
        ),
        (
            "\t/**\n\t * Set the prefix to use for the names of newly created threads.\n\t * @param threadNamePrefix the thread name prefix to set\n\t * @return a new builder instance\n\t */",
            "\t/**\n\t * 设置新建线程名称的前缀。\n\t *\n\t * @param threadNamePrefix the thread name prefix to set 线程名前缀\n\t * @return a new builder instance 新的 Builder 实例\n\t */",
        ),
        (
            "\t/**\n\t * Set whether to use virtual threads.\n\t * @param virtualThreads whether to use virtual threads\n\t * @return a new builder instance\n\t */",
            "\t/**\n\t * 设置是否使用虚拟线程。\n\t *\n\t * @param virtualThreads whether to use virtual threads 是否使用虚拟线程\n\t * @return a new builder instance 新的 Builder 实例\n\t */",
        ),
        (
            "\t/**\n\t * Set whether to cancel remaining tasks on close. By default {@code false} not\n\t * tracking active threads at all or just interrupting any remaining threads that\n\t * still have not finished after the specified\n\t * {@link #taskTerminationTimeout(Duration) taskTerminationTimeout}. Switch this to\n\t * {@code true} for immediate interruption on close, either in combination with a\n\t * subsequent termination timeout or without any waiting at all, depending on whether\n\t * a {@code taskTerminationTimeout} has been specified as well.\n\t * @param cancelRemainingTasksOnClose whether to cancel remaining tasks on close\n\t * @return a new builder instance\n\t * @since 4.0.0\n\t */",
            "\t/**\n\t * 设置关闭时是否取消剩余任务。默认 {@code false}：不跟踪活动线程，\n\t * 或在 {@link #taskTerminationTimeout(Duration) taskTerminationTimeout} 超时后中断未完成线程。\n\t * 设为 {@code true} 则在关闭时立即中断，可配合或不配合终止超时。\n\t *\n\t * @param cancelRemainingTasksOnClose whether to cancel remaining tasks on close 关闭时是否取消剩余任务\n\t * @return a new builder instance 新的 Builder 实例\n\t * @since 4.0.0\n\t */",
        ),
        (
            "\t/**\n\t * Set whether to reject tasks when the concurrency limit has been reached. By default\n\t * {@code false} to block the caller until the submission can be accepted. Switch to\n\t * {@code true} for immediate rejection instead.\n\t * @param rejectTasksWhenLimitReached whether to reject tasks when the concurrency\n\t * limit has been reached\n\t * @return a new builder instance\n\t * @since 3.5.0\n\t */",
            "\t/**\n\t * 设置达到并发上限时是否拒绝任务。默认 {@code false} 阻塞调用方直至可接受提交；\n\t * 设为 {@code true} 则立即拒绝。\n\t *\n\t * @param rejectTasksWhenLimitReached whether to reject tasks when the concurrency\n\t * limit has been reached 达到并发上限时是否拒绝任务\n\t * @return a new builder instance 新的 Builder 实例\n\t * @since 3.5.0\n\t */",
        ),
        (
            "\t/**\n\t * Set the concurrency limit.\n\t * @param concurrencyLimit the concurrency limit\n\t * @return a new builder instance\n\t */",
            "\t/**\n\t * 设置并发上限。\n\t *\n\t * @param concurrencyLimit the concurrency limit 并发上限\n\t * @return a new builder instance 新的 Builder 实例\n\t */",
        ),
        (
            "\t/**\n\t * Set the {@link TaskDecorator} to use or {@code null} to not use any.\n\t * @param taskDecorator the task decorator to use\n\t * @return a new builder instance\n\t */",
            "\t/**\n\t * 设置要使用的 {@link TaskDecorator}；{@code null} 表示不使用。\n\t *\n\t * @param taskDecorator the task decorator to use 任务装饰器\n\t * @return a new builder instance 新的 Builder 实例\n\t */",
        ),
        (
            "\t/**\n\t * Set the task termination timeout.\n\t * @param taskTerminationTimeout the task termination timeout\n\t * @return a new builder instance\n\t * @since 3.2.1\n\t */",
            "\t/**\n\t * 设置任务终止超时时间。\n\t *\n\t * @param taskTerminationTimeout the task termination timeout 任务终止超时\n\t * @return a new builder instance 新的 Builder 实例\n\t * @since 3.2.1\n\t */",
        ),
        (
            "\t/**\n\t * Set the {@link SimpleAsyncTaskExecutorCustomizer customizers} that should be\n\t * applied to the {@link SimpleAsyncTaskExecutor}. Customizers are applied in the\n\t * order that they were added after builder configuration has been applied. Setting\n\t * this value will replace any previously configured customizers.\n\t * @param customizers the customizers to set\n\t * @return a new builder instance\n\t * @see #additionalCustomizers(SimpleAsyncTaskExecutorCustomizer...)\n\t */",
            "\t/**\n\t * 设置应应用于 {@link SimpleAsyncTaskExecutor} 的 {@link SimpleAsyncTaskExecutorCustomizer customizers}。\n\t * 在 Builder 配置应用后按添加顺序执行；设置此值会替换先前配置的全部 customizer。\n\t *\n\t * @param customizers the customizers to set 要设置的 customizer\n\t * @return a new builder instance 新的 Builder 实例\n\t * @see #additionalCustomizers(SimpleAsyncTaskExecutorCustomizer...)\n\t */",
        ),
        (
            "\t/**\n\t * Set the {@link SimpleAsyncTaskExecutorCustomizer customizers} that should be\n\t * applied to the {@link SimpleAsyncTaskExecutor}. Customizers are applied in the\n\t * order that they were added after builder configuration has been applied. Setting\n\t * this value will replace any previously configured customizers.\n\t * @param customizers the customizers to set\n\t * @return a new builder instance\n\t * @see #additionalCustomizers(Iterable)\n\t */",
            "\t/**\n\t * 设置应应用于 {@link SimpleAsyncTaskExecutor} 的 {@link SimpleAsyncTaskExecutorCustomizer customizers}。\n\t * 在 Builder 配置应用后按添加顺序执行；设置此值会替换先前配置的全部 customizer。\n\t *\n\t * @param customizers the customizers to set 要设置的 customizer\n\t * @return a new builder instance 新的 Builder 实例\n\t * @see #additionalCustomizers(Iterable)\n\t */",
        ),
        (
            "\t/**\n\t * Add {@link SimpleAsyncTaskExecutorCustomizer customizers} that should be applied to\n\t * the {@link SimpleAsyncTaskExecutor}. Customizers are applied in the order that they\n\t * were added after builder configuration has been applied.\n\t * @param customizers the customizers to add\n\t * @return a new builder instance\n\t * @see #customizers(SimpleAsyncTaskExecutorCustomizer...)\n\t */",
            "\t/**\n\t * 追加应应用于 {@link SimpleAsyncTaskExecutor} 的 {@link SimpleAsyncTaskExecutorCustomizer customizers}。\n\t * 在 Builder 配置应用后按添加顺序执行。\n\t *\n\t * @param customizers the customizers to add 要追加的 customizer\n\t * @return a new builder instance 新的 Builder 实例\n\t * @see #customizers(SimpleAsyncTaskExecutorCustomizer...)\n\t */",
        ),
        (
            "\t/**\n\t * Add {@link SimpleAsyncTaskExecutorCustomizer customizers} that should be applied to\n\t * the {@link SimpleAsyncTaskExecutor}. Customizers are applied in the order that they\n\t * were added after builder configuration has been applied.\n\t * @param customizers the customizers to add\n\t * @return a new builder instance\n\t * @see #customizers(Iterable)\n\t */",
            "\t/**\n\t * 追加应应用于 {@link SimpleAsyncTaskExecutor} 的 {@link SimpleAsyncTaskExecutorCustomizer customizers}。\n\t * 在 Builder 配置应用后按添加顺序执行。\n\t *\n\t * @param customizers the customizers to add 要追加的 customizer\n\t * @return a new builder instance 新的 Builder 实例\n\t * @see #customizers(Iterable)\n\t */",
        ),
        (
            "\t/**\n\t * Build a new {@link SimpleAsyncTaskExecutor} instance and configure it using this\n\t * builder.\n\t * @return a configured {@link SimpleAsyncTaskExecutor} instance.\n\t * @see #build(Class)\n\t * @see #configure(SimpleAsyncTaskExecutor)\n\t */",
            "\t/**\n\t * 构建新的 {@link SimpleAsyncTaskExecutor} 实例并用本 Builder 配置。\n\t *\n\t * @return a configured {@link SimpleAsyncTaskExecutor} instance. 已配置的实例\n\t * @see #build(Class)\n\t * @see #configure(SimpleAsyncTaskExecutor)\n\t */",
        ),
        (
            "\t/**\n\t * Build a new {@link SimpleAsyncTaskExecutor} instance of the specified type and\n\t * configure it using this builder.\n\t * @param <T> the type of task executor\n\t * @param taskExecutorClass the template type to create\n\t * @return a configured {@link SimpleAsyncTaskExecutor} instance.\n\t * @see #build()\n\t * @see #configure(SimpleAsyncTaskExecutor)\n\t */",
            "\t/**\n\t * 构建指定类型的 {@link SimpleAsyncTaskExecutor} 实例并用本 Builder 配置。\n\t *\n\t * @param <T> the type of task executor 任务执行器类型\n\t * @param taskExecutorClass the template type to create 要实例化的类型\n\t * @return a configured {@link SimpleAsyncTaskExecutor} instance. 已配置的实例\n\t * @see #build()\n\t * @see #configure(SimpleAsyncTaskExecutor)\n\t */",
        ),
        (
            "\t/**\n\t * Configure the provided {@link SimpleAsyncTaskExecutor} instance using this builder.\n\t * @param <T> the type of task executor\n\t * @param taskExecutor the {@link SimpleAsyncTaskExecutor} to configure\n\t * @return the task executor instance\n\t * @see #build()\n\t * @see #build(Class)\n\t */",
            "\t/**\n\t * 使用本 Builder 配置给定的 {@link SimpleAsyncTaskExecutor} 实例。\n\t *\n\t * @param <T> the type of task executor 任务执行器类型\n\t * @param taskExecutor the {@link SimpleAsyncTaskExecutor} to configure 待配置的执行器\n\t * @return the task executor instance 任务执行器实例\n\t * @see #build()\n\t * @see #build(Class)\n\t */",
        ),
    ],
    "SimpleAsyncTaskSchedulerBuilder.java": [
        (
            "/**\n * Builder that can be used to configure and create a {@link SimpleAsyncTaskScheduler}.\n * Provides convenience methods to set common {@link SimpleAsyncTaskScheduler} settings.\n * For advanced configuration, consider using {@link SimpleAsyncTaskSchedulerCustomizer}.\n * <p>\n * In a typical auto-configured Spring Boot application this builder is available as a\n * bean and can be injected whenever a {@link SimpleAsyncTaskScheduler} is needed.\n *\n * @author Stephane Nicoll\n * @author Moritz Halbritter\n * @since 3.2.0\n */",
            "/**\n * 用于配置并创建 {@link SimpleAsyncTaskScheduler} 的 Builder。\n * 提供便捷方法设置常用 {@link SimpleAsyncTaskScheduler} 参数。\n * 高级配置可使用 {@link SimpleAsyncTaskSchedulerCustomizer}。\n * <p>\n * 在典型的 Spring Boot 自动配置应用中，此 Builder 作为 Bean 可用，\n * 需要 {@link SimpleAsyncTaskScheduler} 时可注入。\n *\n * @author Stephane Nicoll\n * @author Moritz Halbritter\n * @since 3.2.0\n */",
        ),
        (
            "\t/**\n\t * Set the prefix to use for the names of newly created threads.\n\t * @param threadNamePrefix the thread name prefix to set\n\t * @return a new builder instance\n\t */",
            "\t/**\n\t * 设置新建线程名称的前缀。\n\t *\n\t * @param threadNamePrefix the thread name prefix to set 线程名前缀\n\t * @return a new builder instance 新的 Builder 实例\n\t */",
        ),
        (
            "\t/**\n\t * Set the concurrency limit.\n\t * @param concurrencyLimit the concurrency limit\n\t * @return a new builder instance\n\t */",
            "\t/**\n\t * 设置并发上限。\n\t *\n\t * @param concurrencyLimit the concurrency limit 并发上限\n\t * @return a new builder instance 新的 Builder 实例\n\t */",
        ),
        (
            "\t/**\n\t * Set whether to use virtual threads.\n\t * @param virtualThreads whether to use virtual threads\n\t * @return a new builder instance\n\t */",
            "\t/**\n\t * 设置是否使用虚拟线程。\n\t *\n\t * @param virtualThreads whether to use virtual threads 是否使用虚拟线程\n\t * @return a new builder instance 新的 Builder 实例\n\t */",
        ),
        (
            "\t/**\n\t * Set the task termination timeout.\n\t * @param taskTerminationTimeout the task termination timeout\n\t * @return a new builder instance\n\t * @since 3.2.1\n\t */",
            "\t/**\n\t * 设置任务终止超时时间。\n\t *\n\t * @param taskTerminationTimeout the task termination timeout 任务终止超时\n\t * @return a new builder instance 新的 Builder 实例\n\t * @since 3.2.1\n\t */",
        ),
        (
            "\t/**\n\t * Set the task decorator to be used by the {@link SimpleAsyncTaskScheduler}.\n\t * @param taskDecorator the task decorator to set\n\t * @return a new builder instance\n\t * @since 3.5.0\n\t */",
            "\t/**\n\t * 设置 {@link SimpleAsyncTaskScheduler} 使用的任务装饰器。\n\t *\n\t * @param taskDecorator the task decorator to set 任务装饰器\n\t * @return a new builder instance 新的 Builder 实例\n\t * @since 3.5.0\n\t */",
        ),
        (
            "\t/**\n\t * Set the {@link SimpleAsyncTaskSchedulerCustomizer customizers} that should be\n\t * applied to the {@link SimpleAsyncTaskScheduler}. Customizers are applied in the\n\t * order that they were added after builder configuration has been applied. Setting\n\t * this value will replace any previously configured customizers.\n\t * @param customizers the customizers to set\n\t * @return a new builder instance\n\t * @see #additionalCustomizers(SimpleAsyncTaskSchedulerCustomizer...)\n\t */",
            "\t/**\n\t * 设置应应用于 {@link SimpleAsyncTaskScheduler} 的 {@link SimpleAsyncTaskSchedulerCustomizer customizers}。\n\t * 在 Builder 配置应用后按添加顺序执行；设置此值会替换先前配置的全部 customizer。\n\t *\n\t * @param customizers the customizers to set 要设置的 customizer\n\t * @return a new builder instance 新的 Builder 实例\n\t * @see #additionalCustomizers(SimpleAsyncTaskSchedulerCustomizer...)\n\t */",
        ),
        (
            "\t/**\n\t * Set the {@link SimpleAsyncTaskSchedulerCustomizer customizers} that should be\n\t * applied to the {@link SimpleAsyncTaskScheduler}. Customizers are applied in the\n\t * order that they were added after builder configuration has been applied. Setting\n\t * this value will replace any previously configured customizers.\n\t * @param customizers the customizers to set\n\t * @return a new builder instance\n\t * @see #additionalCustomizers(Iterable)\n\t */",
            "\t/**\n\t * 设置应应用于 {@link SimpleAsyncTaskScheduler} 的 {@link SimpleAsyncTaskSchedulerCustomizer customizers}。\n\t * 在 Builder 配置应用后按添加顺序执行；设置此值会替换先前配置的全部 customizer。\n\t *\n\t * @param customizers the customizers to set 要设置的 customizer\n\t * @return a new builder instance 新的 Builder 实例\n\t * @see #additionalCustomizers(Iterable)\n\t */",
        ),
        (
            "\t/**\n\t * Add {@link SimpleAsyncTaskSchedulerCustomizer customizers} that should be applied\n\t * to the {@link SimpleAsyncTaskScheduler}. Customizers are applied in the order that\n\t * they were added after builder configuration has been applied.\n\t * @param customizers the customizers to add\n\t * @return a new builder instance\n\t * @see #customizers(SimpleAsyncTaskSchedulerCustomizer...)\n\t */",
            "\t/**\n\t * 追加应应用于 {@link SimpleAsyncTaskScheduler} 的 {@link SimpleAsyncTaskSchedulerCustomizer customizers}。\n\t * 在 Builder 配置应用后按添加顺序执行。\n\t *\n\t * @param customizers the customizers to add 要追加的 customizer\n\t * @return a new builder instance 新的 Builder 实例\n\t * @see #customizers(SimpleAsyncTaskSchedulerCustomizer...)\n\t */",
        ),
        (
            "\t/**\n\t * Add {@link SimpleAsyncTaskSchedulerCustomizer customizers} that should be applied\n\t * to the {@link SimpleAsyncTaskScheduler}. Customizers are applied in the order that\n\t * they were added after builder configuration has been applied.\n\t * @param customizers the customizers to add\n\t * @return a new builder instance\n\t * @see #customizers(Iterable)\n\t */",
            "\t/**\n\t * 追加应应用于 {@link SimpleAsyncTaskScheduler} 的 {@link SimpleAsyncTaskSchedulerCustomizer customizers}。\n\t * 在 Builder 配置应用后按添加顺序执行。\n\t *\n\t * @param customizers the customizers to add 要追加的 customizer\n\t * @return a new builder instance 新的 Builder 实例\n\t * @see #customizers(Iterable)\n\t */",
        ),
        (
            "\t/**\n\t * Build a new {@link SimpleAsyncTaskScheduler} instance and configure it using this\n\t * builder.\n\t * @return a configured {@link SimpleAsyncTaskScheduler} instance.\n\t * @see #configure(SimpleAsyncTaskScheduler)\n\t */",
            "\t/**\n\t * 构建新的 {@link SimpleAsyncTaskScheduler} 实例并用本 Builder 配置。\n\t *\n\t * @return a configured {@link SimpleAsyncTaskScheduler} instance. 已配置的实例\n\t * @see #configure(SimpleAsyncTaskScheduler)\n\t */",
        ),
        (
            "\t/**\n\t * Configure the provided {@link SimpleAsyncTaskScheduler} instance using this\n\t * builder.\n\t * @param <T> the type of task scheduler\n\t * @param taskScheduler the {@link SimpleAsyncTaskScheduler} to configure\n\t * @return the task scheduler instance\n\t * @see #build()\n\t */",
            "\t/**\n\t * 使用本 Builder 配置给定的 {@link SimpleAsyncTaskScheduler} 实例。\n\t *\n\t * @param <T> the type of task scheduler 任务调度器类型\n\t * @param taskScheduler the {@link SimpleAsyncTaskScheduler} to configure 待配置的调度器\n\t * @return the task scheduler instance 任务调度器实例\n\t * @see #build()\n\t */",
        ),
    ],
    "ThreadPoolTaskExecutorBuilder.java": [
        (
            "/**\n * Builder that can be used to configure and create a {@link ThreadPoolTaskExecutor}.\n * Provides convenience methods to set common {@link ThreadPoolTaskExecutor} settings and\n * register {@link #taskDecorator(TaskDecorator)}). For advanced configuration, consider\n * using {@link ThreadPoolTaskExecutorCustomizer}.\n * <p>\n * In a typical auto-configured Spring Boot application this builder is available as a\n * bean and can be injected whenever a {@link ThreadPoolTaskExecutor} is needed.\n *\n * @author Stephane Nicoll\n * @author Filip Hrisafov\n * @author Yanming Zhou\n * @since 3.2.0\n */",
            "/**\n * 用于配置并创建 {@link ThreadPoolTaskExecutor} 的 Builder。\n * 提供便捷方法设置常用 {@link ThreadPoolTaskExecutor} 参数并注册\n * {@link #taskDecorator(TaskDecorator)}。高级配置可使用 {@link ThreadPoolTaskExecutorCustomizer}。\n * <p>\n * 在典型的 Spring Boot 自动配置应用中，此 Builder 作为 Bean 可用，\n * 需要 {@link ThreadPoolTaskExecutor} 时可注入。\n *\n * @author Stephane Nicoll\n * @author Filip Hrisafov\n * @author Yanming Zhou\n * @since 3.2.0\n */",
        ),
        (
            "\t/**\n\t * Set the capacity of the queue. An unbounded capacity does not increase the pool and\n\t * therefore ignores {@link #maxPoolSize(int) maxPoolSize}.\n\t * @param queueCapacity the queue capacity to set\n\t * @return a new builder instance\n\t */",
            "\t/**\n\t * 设置队列容量。无界队列不会扩展线程池，因此忽略 {@link #maxPoolSize(int) maxPoolSize}。\n\t *\n\t * @param queueCapacity the queue capacity to set 队列容量\n\t * @return a new builder instance 新的 Builder 实例\n\t */",
        ),
        (
            "\t/**\n\t * Set the core number of threads. Effectively that maximum number of threads as long\n\t * as the queue is not full.\n\t * <p>\n\t * Core threads can grow and shrink if {@link #allowCoreThreadTimeOut(boolean)} is\n\t * enabled.\n\t * @param corePoolSize the core pool size to set\n\t * @return a new builder instance\n\t */",
            "\t/**\n\t * 设置核心线程数。在队列未满时，这实际上也是最大线程数。\n\t * <p>\n\t * 若启用 {@link #allowCoreThreadTimeOut(boolean)}，核心线程可动态增减。\n\t *\n\t * @param corePoolSize the core pool size to set 核心池大小\n\t * @return a new builder instance 新的 Builder 实例\n\t */",
        ),
        (
            "\t/**\n\t * Set the maximum allowed number of threads. When the {@link #queueCapacity(int)\n\t * queue} is full, the pool can expand up to that size to accommodate the load.\n\t * <p>\n\t * If the {@link #queueCapacity(int) queue capacity} is unbounded, this setting is\n\t * ignored.\n\t * @param maxPoolSize the max pool size to set\n\t * @return a new builder instance\n\t */",
            "\t/**\n\t * 设置允许的最大线程数。当 {@link #queueCapacity(int) queue} 已满时，\n\t * 线程池可扩展至此规模以应对负载。\n\t * <p>\n\t * 若 {@link #queueCapacity(int) queue capacity} 无界，则忽略此设置。\n\t *\n\t * @param maxPoolSize the max pool size to set 最大池大小\n\t * @return a new builder instance 新的 Builder 实例\n\t */",
        ),
        (
            "\t/**\n\t * Set whether core threads are allowed to time out. When enabled, this enables\n\t * dynamic growing and shrinking of the pool.\n\t * @param allowCoreThreadTimeOut if core threads are allowed to time out\n\t * @return a new builder instance\n\t */",
            "\t/**\n\t * 设置是否允许核心线程超时。启用后线程池可动态扩缩。\n\t *\n\t * @param allowCoreThreadTimeOut if core threads are allowed to time out 是否允许核心线程超时\n\t * @return a new builder instance 新的 Builder 实例\n\t */",
        ),
        (
            "\t/**\n\t * Set the time limit for which threads may remain idle before being terminated.\n\t * @param keepAlive the keep alive to set\n\t * @return a new builder instance\n\t */",
            "\t/**\n\t * 设置线程在终止前可保持空闲的最长时间。\n\t *\n\t * @param keepAlive the keep alive to set 保活时间\n\t * @return a new builder instance 新的 Builder 实例\n\t */",
        ),
        (
            "\t/**\n\t * Set whether to accept further tasks after the application context close phase has\n\t * begun.\n\t * @param acceptTasksAfterContextClose whether to accept further tasks after the\n\t * application context close phase has begun\n\t * @return a new builder instance\n\t * @since 3.3.0\n\t */",
            "\t/**\n\t * 设置应用上下文关闭阶段开始后是否仍接受新任务。\n\t *\n\t * @param acceptTasksAfterContextClose whether to accept further tasks after the\n\t * application context close phase has begun 关闭阶段开始后是否接受任务\n\t * @return a new builder instance 新的 Builder 实例\n\t * @since 3.3.0\n\t */",
        ),
        (
            "\t/**\n\t * Set whether the executor should wait for scheduled tasks to complete on shutdown,\n\t * not interrupting running tasks and executing all tasks in the queue.\n\t * @param awaitTermination whether the executor needs to wait for the tasks to\n\t * complete on shutdown\n\t * @return a new builder instance\n\t * @see #awaitTerminationPeriod(Duration)\n\t */",
            "\t/**\n\t * 设置关闭时执行器是否等待已调度任务完成，\n\t * 不中断运行中任务并执行队列中全部任务。\n\t *\n\t * @param awaitTermination whether the executor needs to wait for the tasks to\n\t * complete on shutdown 关闭时是否等待任务完成\n\t * @return a new builder instance 新的 Builder 实例\n\t * @see #awaitTerminationPeriod(Duration)\n\t */",
        ),
        (
            "\t/**\n\t * Set the maximum time the executor is supposed to block on shutdown. When set, the\n\t * executor blocks on shutdown in order to wait for remaining tasks to complete their\n\t * execution before the rest of the container continues to shut down. This is\n\t * particularly useful if your remaining tasks are likely to need access to other\n\t * resources that are also managed by the container.\n\t * @param awaitTerminationPeriod the await termination period to set\n\t * @return a new builder instance\n\t */",
            "\t/**\n\t * 设置关闭时执行器最长阻塞等待时间。设置后，执行器在关闭时会阻塞，\n\t * 等待剩余任务完成后再继续容器关闭流程。\n\t * 当剩余任务可能依赖容器管理的其他资源时尤其有用。\n\t *\n\t * @param awaitTerminationPeriod the await termination period to set 关闭等待时长\n\t * @return a new builder instance 新的 Builder 实例\n\t */",
        ),
        (
            "\t/**\n\t * Set the prefix to use for the names of newly created threads.\n\t * @param threadNamePrefix the thread name prefix to set\n\t * @return a new builder instance\n\t */",
            "\t/**\n\t * 设置新建线程名称的前缀。\n\t *\n\t * @param threadNamePrefix the thread name prefix to set 线程名前缀\n\t * @return a new builder instance 新的 Builder 实例\n\t */",
        ),
        (
            "\t/**\n\t * Set the {@link TaskDecorator} to use or {@code null} to not use any.\n\t * @param taskDecorator the task decorator to use\n\t * @return a new builder instance\n\t */",
            "\t/**\n\t * 设置要使用的 {@link TaskDecorator}；{@code null} 表示不使用。\n\t *\n\t * @param taskDecorator the task decorator to use 任务装饰器\n\t * @return a new builder instance 新的 Builder 实例\n\t */",
        ),
        (
            "\t/**\n\t * Set the {@link ThreadPoolTaskExecutorCustomizer ThreadPoolTaskExecutorCustomizers}\n\t * that should be applied to the {@link ThreadPoolTaskExecutor}. Customizers are\n\t * applied in the order that they were added after builder configuration has been\n\t * applied. Setting this value will replace any previously configured customizers.\n\t * @param customizers the customizers to set\n\t * @return a new builder instance\n\t * @see #additionalCustomizers(ThreadPoolTaskExecutorCustomizer...)\n\t */",
            "\t/**\n\t * 设置应应用于 {@link ThreadPoolTaskExecutor} 的 {@link ThreadPoolTaskExecutorCustomizer ThreadPoolTaskExecutorCustomizers}。\n\t * 在 Builder 配置应用后按添加顺序执行；设置此值会替换先前配置的全部 customizer。\n\t *\n\t * @param customizers the customizers to set 要设置的 customizer\n\t * @return a new builder instance 新的 Builder 实例\n\t * @see #additionalCustomizers(ThreadPoolTaskExecutorCustomizer...)\n\t */",
        ),
        (
            "\t/**\n\t * Add {@link ThreadPoolTaskExecutorCustomizer ThreadPoolTaskExecutorCustomizers} that\n\t * should be applied to the {@link ThreadPoolTaskExecutor}. Customizers are applied in\n\t * the order that they were added after builder configuration has been applied.\n\t * @param customizers the customizers to add\n\t * @return a new builder instance\n\t * @see #customizers(ThreadPoolTaskExecutorCustomizer...)\n\t */",
            "\t/**\n\t * 追加应应用于 {@link ThreadPoolTaskExecutor} 的 {@link ThreadPoolTaskExecutorCustomizer ThreadPoolTaskExecutorCustomizers}。\n\t * 在 Builder 配置应用后按添加顺序执行。\n\t *\n\t * @param customizers the customizers to add 要追加的 customizer\n\t * @return a new builder instance 新的 Builder 实例\n\t * @see #customizers(ThreadPoolTaskExecutorCustomizer...)\n\t */",
        ),
        (
            "\t/**\n\t * Add {@link ThreadPoolTaskExecutorCustomizer ThreadPoolTaskExecutorCustomizers} that\n\t * should be applied to the {@link ThreadPoolTaskExecutor}. Customizers are applied in\n\t * the order that they were added after builder configuration has been applied.\n\t * @param customizers the customizers to add\n\t * @return a new builder instance\n\t * @see #customizers(ThreadPoolTaskExecutorCustomizer...)\n\t */",
            "\t/**\n\t * 追加应应用于 {@link ThreadPoolTaskExecutor} 的 {@link ThreadPoolTaskExecutorCustomizer ThreadPoolTaskExecutorCustomizers}。\n\t * 在 Builder 配置应用后按添加顺序执行。\n\t *\n\t * @param customizers the customizers to add 要追加的 customizer\n\t * @return a new builder instance 新的 Builder 实例\n\t * @see #customizers(ThreadPoolTaskExecutorCustomizer...)\n\t */",
        ),
        (
            "\t/**\n\t * Build a new {@link ThreadPoolTaskExecutor} instance and configure it using this\n\t * builder.\n\t * @return a configured {@link ThreadPoolTaskExecutor} instance.\n\t * @see #build(Class)\n\t * @see #configure(ThreadPoolTaskExecutor)\n\t */",
            "\t/**\n\t * 构建新的 {@link ThreadPoolTaskExecutor} 实例并用本 Builder 配置。\n\t *\n\t * @return a configured {@link ThreadPoolTaskExecutor} instance. 已配置的实例\n\t * @see #build(Class)\n\t * @see #configure(ThreadPoolTaskExecutor)\n\t */",
        ),
        (
            "\t/**\n\t * Build a new {@link ThreadPoolTaskExecutor} instance of the specified type and\n\t * configure it using this builder.\n\t * @param <T> the type of task executor\n\t * @param taskExecutorClass the template type to create\n\t * @return a configured {@link ThreadPoolTaskExecutor} instance.\n\t * @see #build()\n\t * @see #configure(ThreadPoolTaskExecutor)\n\t */",
            "\t/**\n\t * 构建指定类型的 {@link ThreadPoolTaskExecutor} 实例并用本 Builder 配置。\n\t *\n\t * @param <T> the type of task executor 任务执行器类型\n\t * @param taskExecutorClass the template type to create 要实例化的类型\n\t * @return a configured {@link ThreadPoolTaskExecutor} instance. 已配置的实例\n\t * @see #build()\n\t * @see #configure(ThreadPoolTaskExecutor)\n\t */",
        ),
        (
            "\t/**\n\t * Configure the provided {@link ThreadPoolTaskExecutor} instance using this builder.\n\t * @param <T> the type of task executor\n\t * @param taskExecutor the {@link ThreadPoolTaskExecutor} to configure\n\t * @return the task executor instance\n\t * @see #build()\n\t * @see #build(Class)\n\t */",
            "\t/**\n\t * 使用本 Builder 配置给定的 {@link ThreadPoolTaskExecutor} 实例。\n\t *\n\t * @param <T> the type of task executor 任务执行器类型\n\t * @param taskExecutor the {@link ThreadPoolTaskExecutor} to configure 待配置的执行器\n\t * @return the task executor instance 任务执行器实例\n\t * @see #build()\n\t * @see #build(Class)\n\t */",
        ),
    ],
    "ThreadPoolTaskSchedulerBuilder.java": [
        (
            "/**\n * Builder that can be used to configure and create a {@link ThreadPoolTaskScheduler}.\n * Provides convenience methods to set common {@link ThreadPoolTaskScheduler} settings.\n * For advanced configuration, consider using {@link ThreadPoolTaskSchedulerCustomizer}.\n * <p>\n * In a typical auto-configured Spring Boot application this builder is available as a\n * bean and can be injected whenever a {@link ThreadPoolTaskScheduler} is needed.\n *\n * @author Stephane Nicoll\n * @since 3.2.0\n */",
            "/**\n * 用于配置并创建 {@link ThreadPoolTaskScheduler} 的 Builder。\n * 提供便捷方法设置常用 {@link ThreadPoolTaskScheduler} 参数。\n * 高级配置可使用 {@link ThreadPoolTaskSchedulerCustomizer}。\n * <p>\n * 在典型的 Spring Boot 自动配置应用中，此 Builder 作为 Bean 可用，\n * 需要 {@link ThreadPoolTaskScheduler} 时可注入。\n *\n * @author Stephane Nicoll\n * @since 3.2.0\n */",
        ),
        (
            "\t/**\n\t * Set the maximum allowed number of threads.\n\t * @param poolSize the pool size to set\n\t * @return a new builder instance\n\t */",
            "\t/**\n\t * 设置允许的最大线程数。\n\t *\n\t * @param poolSize the pool size to set 池大小\n\t * @return a new builder instance 新的 Builder 实例\n\t */",
        ),
        (
            "\t/**\n\t * Set whether the executor should wait for scheduled tasks to complete on shutdown,\n\t * not interrupting running tasks and executing all tasks in the queue.\n\t * @param awaitTermination whether the executor needs to wait for the tasks to\n\t * complete on shutdown\n\t * @return a new builder instance\n\t * @see #awaitTerminationPeriod(Duration)\n\t */",
            "\t/**\n\t * 设置关闭时执行器是否等待已调度任务完成，\n\t * 不中断运行中任务并执行队列中全部任务。\n\t *\n\t * @param awaitTermination whether the executor needs to wait for the tasks to\n\t * complete on shutdown 关闭时是否等待任务完成\n\t * @return a new builder instance 新的 Builder 实例\n\t * @see #awaitTerminationPeriod(Duration)\n\t */",
        ),
        (
            "\t/**\n\t * Set the maximum time the executor is supposed to block on shutdown. When set, the\n\t * executor blocks on shutdown in order to wait for remaining tasks to complete their\n\t * execution before the rest of the container continues to shut down. This is\n\t * particularly useful if your remaining tasks are likely to need access to other\n\t * resources that are also managed by the container.\n\t * @param awaitTerminationPeriod the await termination period to set\n\t * @return a new builder instance\n\t */",
            "\t/**\n\t * 设置关闭时执行器最长阻塞等待时间。设置后，执行器在关闭时会阻塞，\n\t * 等待剩余任务完成后再继续容器关闭流程。\n\t * 当剩余任务可能依赖容器管理的其他资源时尤其有用。\n\t *\n\t * @param awaitTerminationPeriod the await termination period to set 关闭等待时长\n\t * @return a new builder instance 新的 Builder 实例\n\t */",
        ),
        (
            "\t/**\n\t * Set the prefix to use for the names of newly created threads.\n\t * @param threadNamePrefix the thread name prefix to set\n\t * @return a new builder instance\n\t */",
            "\t/**\n\t * 设置新建线程名称的前缀。\n\t *\n\t * @param threadNamePrefix the thread name prefix to set 线程名前缀\n\t * @return a new builder instance 新的 Builder 实例\n\t */",
        ),
        (
            "\t/**\n\t * Set the {@link TaskDecorator} to be applied to the {@link ThreadPoolTaskScheduler}.\n\t * @param taskDecorator the task decorator to set\n\t * @return a new builder instance\n\t * @since 3.5.0\n\t */",
            "\t/**\n\t * 设置应用于 {@link ThreadPoolTaskScheduler} 的 {@link TaskDecorator}。\n\t *\n\t * @param taskDecorator the task decorator to set 任务装饰器\n\t * @return a new builder instance 新的 Builder 实例\n\t * @since 3.5.0\n\t */",
        ),
        (
            "\t/**\n\t * Set the {@link ThreadPoolTaskSchedulerCustomizer\n\t * threadPoolTaskSchedulerCustomizers} that should be applied to the\n\t * {@link ThreadPoolTaskScheduler}. Customizers are applied in the order that they\n\t * were added after builder configuration has been applied. Setting this value will\n\t * replace any previously configured customizers.\n\t * @param customizers the customizers to set\n\t * @return a new builder instance\n\t * @see #additionalCustomizers(ThreadPoolTaskSchedulerCustomizer...)\n\t */",
            "\t/**\n\t * 设置应应用于 {@link ThreadPoolTaskScheduler} 的 {@link ThreadPoolTaskSchedulerCustomizer threadPoolTaskSchedulerCustomizers}。\n\t * 在 Builder 配置应用后按添加顺序执行；设置此值会替换先前配置的全部 customizer。\n\t *\n\t * @param customizers the customizers to set 要设置的 customizer\n\t * @return a new builder instance 新的 Builder 实例\n\t * @see #additionalCustomizers(ThreadPoolTaskSchedulerCustomizer...)\n\t */",
        ),
        (
            "\t/**\n\t * Set the {@link ThreadPoolTaskSchedulerCustomizer\n\t * threadPoolTaskSchedulerCustomizers} that should be applied to the\n\t * {@link ThreadPoolTaskScheduler}. Customizers are applied in the order that they\n\t * were added after builder configuration has been applied. Setting this value will\n\t * replace any previously configured customizers.\n\t * @param customizers the customizers to set\n\t * @return a new builder instance\n\t * @see #additionalCustomizers(ThreadPoolTaskSchedulerCustomizer...)\n\t */",
            "\t/**\n\t * 设置应应用于 {@link ThreadPoolTaskScheduler} 的 {@link ThreadPoolTaskSchedulerCustomizer threadPoolTaskSchedulerCustomizers}。\n\t * 在 Builder 配置应用后按添加顺序执行；设置此值会替换先前配置的全部 customizer。\n\t *\n\t * @param customizers the customizers to set 要设置的 customizer\n\t * @return a new builder instance 新的 Builder 实例\n\t * @see #additionalCustomizers(ThreadPoolTaskSchedulerCustomizer...)\n\t */",
        ),
        (
            "\t/**\n\t * Add {@link ThreadPoolTaskSchedulerCustomizer threadPoolTaskSchedulerCustomizers}\n\t * that should be applied to the {@link ThreadPoolTaskScheduler}. Customizers are\n\t * applied in the order that they were added after builder configuration has been\n\t * applied.\n\t * @param customizers the customizers to add\n\t * @return a new builder instance\n\t * @see #customizers(ThreadPoolTaskSchedulerCustomizer...)\n\t */",
            "\t/**\n\t * 追加应应用于 {@link ThreadPoolTaskScheduler} 的 {@link ThreadPoolTaskSchedulerCustomizer threadPoolTaskSchedulerCustomizers}。\n\t * 在 Builder 配置应用后按添加顺序执行。\n\t *\n\t * @param customizers the customizers to add 要追加的 customizer\n\t * @return a new builder instance 新的 Builder 实例\n\t * @see #customizers(ThreadPoolTaskSchedulerCustomizer...)\n\t */",
        ),
        (
            "\t/**\n\t * Add {@link ThreadPoolTaskSchedulerCustomizer threadPoolTaskSchedulerCustomizers}\n\t * that should be applied to the {@link ThreadPoolTaskScheduler}. Customizers are\n\t * applied in the order that they were added after builder configuration has been\n\t * applied.\n\t * @param customizers the customizers to add\n\t * @return a new builder instance\n\t * @see #customizers(ThreadPoolTaskSchedulerCustomizer...)\n\t */",
            "\t/**\n\t * 追加应应用于 {@link ThreadPoolTaskScheduler} 的 {@link ThreadPoolTaskSchedulerCustomizer threadPoolTaskSchedulerCustomizers}。\n\t * 在 Builder 配置应用后按添加顺序执行。\n\t *\n\t * @param customizers the customizers to add 要追加的 customizer\n\t * @return a new builder instance 新的 Builder 实例\n\t * @see #customizers(ThreadPoolTaskSchedulerCustomizer...)\n\t */",
        ),
        (
            "\t/**\n\t * Build a new {@link ThreadPoolTaskScheduler} instance and configure it using this\n\t * builder.\n\t * @return a configured {@link ThreadPoolTaskScheduler} instance.\n\t * @see #configure(ThreadPoolTaskScheduler)\n\t */",
            "\t/**\n\t * 构建新的 {@link ThreadPoolTaskScheduler} 实例并用本 Builder 配置。\n\t *\n\t * @return a configured {@link ThreadPoolTaskScheduler} instance. 已配置的实例\n\t * @see #configure(ThreadPoolTaskScheduler)\n\t */",
        ),
        (
            "\t/**\n\t * Configure the provided {@link ThreadPoolTaskScheduler} instance using this builder.\n\t * @param <T> the type of task scheduler\n\t * @param taskScheduler the {@link ThreadPoolTaskScheduler} to configure\n\t * @return the task scheduler instance\n\t * @see #build()\n\t */",
            "\t/**\n\t * 使用本 Builder 配置给定的 {@link ThreadPoolTaskScheduler} 实例。\n\t *\n\t * @param <T> the type of task scheduler 任务调度器类型\n\t * @param taskScheduler the {@link ThreadPoolTaskScheduler} to configure 待配置的调度器\n\t * @return the task scheduler instance 任务调度器实例\n\t * @see #build()\n\t */",
        ),
    ],
})
