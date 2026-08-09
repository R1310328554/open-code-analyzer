LARGE_C_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "ThreadPoolTaskExecutor.java": [
        (
            "/**\n * JavaBean that allows for configuring a {@link java.util.concurrent.ThreadPoolExecutor}\n * in bean style (through its \"corePoolSize\", \"maxPoolSize\", \"keepAliveSeconds\", \"queueCapacity\"\n * properties) and exposing it as a Spring {@link org.springframework.core.task.TaskExecutor}.\n * This class is also well suited for management and monitoring (for example, through JMX),\n * providing several useful attributes: \"corePoolSize\", \"maxPoolSize\", \"keepAliveSeconds\"\n * (all supporting updates at runtime); \"poolSize\", \"activeCount\" (for introspection only).\n *\n * <p>The default configuration is a core pool size of 1, with unlimited max pool size\n * and unlimited queue capacity. This is roughly equivalent to\n * {@link java.util.concurrent.Executors#newSingleThreadExecutor()}, sharing a single\n * thread for all tasks. Setting {@link #setQueueCapacity \"queueCapacity\"} to 0 mimics\n * {@link java.util.concurrent.Executors#newCachedThreadPool()}, with immediate scaling\n * of threads in the pool to a potentially very high number. Consider also setting a\n * {@link #setMaxPoolSize \"maxPoolSize\"} at that point, as well as possibly a higher\n * {@link #setCorePoolSize \"corePoolSize\"} (see also the\n * {@link #setAllowCoreThreadTimeOut \"allowCoreThreadTimeOut\"} mode of scaling).\n *\n * <p><b>NOTE:</b> This class implements Spring's\n * {@link org.springframework.core.task.TaskExecutor} interface as well as the\n * {@link java.util.concurrent.Executor} interface, with the former being the primary\n * interface, the other just serving as secondary convenience. For this reason, the\n * exception handling follows the TaskExecutor contract rather than the Executor contract,\n * in particular regarding the {@link org.springframework.core.task.TaskRejectedException}.\n *\n * <p>For an alternative, you may set up a ThreadPoolExecutor instance directly using\n * constructor injection, or use a factory method definition that points to the\n * {@link java.util.concurrent.Executors} class. To expose such a raw Executor as a\n * Spring {@link org.springframework.core.task.TaskExecutor}, simply wrap it with a\n * {@link org.springframework.scheduling.concurrent.ConcurrentTaskExecutor} adapter.\n *\n * @author Juergen Hoeller\n * @author Rémy Guihard\n * @author Sam Brannen\n * @since 2.0\n * @see org.springframework.core.task.TaskExecutor\n * @see java.util.concurrent.ThreadPoolExecutor\n * @see ThreadPoolExecutorFactoryBean\n * @see ConcurrentTaskExecutor\n */",
            "/**\n * 允许以 Bean 风格（通过 \"corePoolSize\"、\"maxPoolSize\"、\"keepAliveSeconds\"、\n * \"queueCapacity\" 属性）配置 {@link java.util.concurrent.ThreadPoolExecutor}，\n * 并将其作为 Spring {@link org.springframework.core.task.TaskExecutor} 暴露的 JavaBean。\n * 本类也适合管理与监控（例如通过 JMX），\n * 提供若干有用属性：\"corePoolSize\"、\"maxPoolSize\"、\"keepAliveSeconds\"\n *（均支持运行时更新）；\"poolSize\"、\"activeCount\"（仅用于自省）。\n *\n * <p>默认配置为核心池大小 1、无界最大池大小与无界队列容量。\n * 大致等价于 {@link java.util.concurrent.Executors#newSingleThreadExecutor()}，\n * 所有任务共享单线程。将 {@link #setQueueCapacity \"queueCapacity\"} 设为 0\n * 可模拟 {@link java.util.concurrent.Executors#newCachedThreadPool()}，\n * 池中线程可立即扩展至可能很高的数量。此时建议同时设置\n * {@link #setMaxPoolSize \"maxPoolSize\"}，以及可能更高的\n * {@link #setCorePoolSize \"corePoolSize\"}（另见\n * {@link #setAllowCoreThreadTimeOut \"allowCoreThreadTimeOut\"} 扩展模式）。\n *\n * <p><b>注意：</b>本类实现 Spring 的\n * {@link org.springframework.core.task.TaskExecutor} 接口以及\n * {@link java.util.concurrent.Executor} 接口，以前者为主接口，\n * 后者仅为辅助便利。因此异常处理遵循 TaskExecutor 契约而非 Executor 契约，\n * 尤其涉及 {@link org.springframework.core.task.TaskRejectedException}。\n *\n * <p>也可通过构造器注入直接配置 ThreadPoolExecutor，\n * 或使用指向 {@link java.util.concurrent.Executors} 的工厂方法定义。\n * 若要将此类原始 Executor 作为 Spring {@link org.springframework.core.task.TaskExecutor} 暴露，\n * 只需用 {@link org.springframework.scheduling.concurrent.ConcurrentTaskExecutor} 适配器包装即可。\n *\n * @author Juergen Hoeller\n * @author Rémy Guihard\n * @author Sam Brannen\n * @since 2.0\n * @see org.springframework.core.task.TaskExecutor\n * @see java.util.concurrent.ThreadPoolExecutor\n * @see ThreadPoolExecutorFactoryBean\n * @see ConcurrentTaskExecutor\n */",
        ),
        (
            "\t/**\n\t * Set the ThreadPoolExecutor's core pool size.\n\t * Default is 1.\n\t * <p><b>This setting can be modified at runtime, for example through JMX.</b>\n\t */",
            "\t/**\n\t * 设置 ThreadPoolExecutor 的核心池大小。\n\t * 默认为 1。\n\t * <p><b>此设置可在运行时修改，例如通过 JMX。</b>\n\t */",
        ),
        (
            "\t/**\n\t * Return the ThreadPoolExecutor's core pool size.\n\t */",
            "\t/**\n\t * 返回 ThreadPoolExecutor 的核心池大小。\n\t */",
        ),
        (
            "\t/**\n\t * Set the ThreadPoolExecutor's maximum pool size.\n\t * Default is {@code Integer.MAX_VALUE}.\n\t * <p><b>This setting can be modified at runtime, for example through JMX.</b>\n\t */",
            "\t/**\n\t * 设置 ThreadPoolExecutor 的最大池大小。\n\t * 默认为 {@code Integer.MAX_VALUE}。\n\t * <p><b>此设置可在运行时修改，例如通过 JMX。</b>\n\t */",
        ),
        (
            "\t/**\n\t * Return the ThreadPoolExecutor's maximum pool size.\n\t */",
            "\t/**\n\t * 返回 ThreadPoolExecutor 的最大池大小。\n\t */",
        ),
        (
            "\t/**\n\t * Set the ThreadPoolExecutor's keep-alive seconds.\n\t * <p>Default is 60.\n\t * <p><b>This setting can be modified at runtime, for example through JMX.</b>\n\t */",
            "\t/**\n\t * 设置 ThreadPoolExecutor 的 keep-alive 秒数。\n\t * <p>默认为 60。\n\t * <p><b>此设置可在运行时修改，例如通过 JMX。</b>\n\t */",
        ),
        (
            "\t/**\n\t * Return the ThreadPoolExecutor's keep-alive seconds.\n\t */",
            "\t/**\n\t * 返回 ThreadPoolExecutor 的 keep-alive 秒数。\n\t */",
        ),
        (
            "\t/**\n\t * Set the capacity for the ThreadPoolExecutor's BlockingQueue.\n\t * <p>Default is {@code Integer.MAX_VALUE}.\n\t * <p>Any positive value will lead to a LinkedBlockingQueue instance;\n\t * any other value will lead to a SynchronousQueue instance.\n\t * @see java.util.concurrent.LinkedBlockingQueue\n\t * @see java.util.concurrent.SynchronousQueue\n\t */",
            "\t/**\n\t * 设置 ThreadPoolExecutor 的 BlockingQueue 容量。\n\t * <p>默认为 {@code Integer.MAX_VALUE}。\n\t * <p>任意正值将创建 LinkedBlockingQueue 实例；\n\t * 其他值将创建 SynchronousQueue 实例。\n\t * @see java.util.concurrent.LinkedBlockingQueue\n\t * @see java.util.concurrent.SynchronousQueue\n\t */",
        ),
        (
            "\t/**\n\t * Return the capacity for the ThreadPoolExecutor's BlockingQueue.\n\t * @since 5.3.21\n\t * @see #setQueueCapacity(int)\n\t */",
            "\t/**\n\t * 返回 ThreadPoolExecutor 的 BlockingQueue 容量。\n\t * @since 5.3.21\n\t * @see #setQueueCapacity(int)\n\t */",
        ),
        (
            "\t/**\n\t * Specify whether to allow core threads to time out. This enables dynamic\n\t * growing and shrinking even in combination with a non-zero queue (since\n\t * the max pool size will only grow once the queue is full).\n\t * <p>Default is \"false\".\n\t * @see java.util.concurrent.ThreadPoolExecutor#allowCoreThreadTimeOut(boolean)\n\t */",
            "\t/**\n\t * 指定是否允许核心线程超时。即使队列非空也可动态扩缩\n\t *（最大池大小仅在队列满后才增长）。\n\t * <p>默认为 \"false\"。\n\t * @see java.util.concurrent.ThreadPoolExecutor#allowCoreThreadTimeOut(boolean)\n\t */",
        ),
        (
            "\t/**\n\t * Specify whether to start all core threads, causing them to idly wait for work.\n\t * <p>Default is \"false\", starting threads and adding them to the pool on demand.\n\t * @since 5.3.14\n\t * @see java.util.concurrent.ThreadPoolExecutor#prestartAllCoreThreads\n\t */",
            "\t/**\n\t * 指定是否启动所有核心线程，使其空闲等待任务。\n\t * <p>默认为 \"false\"，按需启动线程并加入池。\n\t * @since 5.3.14\n\t * @see java.util.concurrent.ThreadPoolExecutor#prestartAllCoreThreads\n\t */",
        ),
        (
            "\t/**\n\t * Specify whether to initiate an early shutdown signal on context close,\n\t * disposing all idle threads and rejecting further task submissions.\n\t * <p>By default, existing tasks will be allowed to complete within the\n\t * coordinated lifecycle stop phase in any case. This setting just controls\n\t * whether an explicit {@link ThreadPoolExecutor#shutdown()} call will be\n\t * triggered on context close, rejecting task submissions after that point.\n\t * <p>As of 6.1.4, the default is \"false\", leniently allowing for late tasks\n\t * to arrive after context close, still participating in the lifecycle stop\n\t * phase. Note that this differs from {@link #setAcceptTasksAfterContextClose}\n\t * which completely bypasses the coordinated lifecycle stop phase, with no\n\t * explicit waiting for the completion of existing tasks at all.\n\t * <p>Switch this to \"true\" for a strict early shutdown signal analogous to\n\t * the 6.1-established default behavior of {@link ThreadPoolTaskScheduler}.\n\t * Note that the related flags {@link #setAcceptTasksAfterContextClose} and\n\t * {@link #setWaitForTasksToCompleteOnShutdown} will override this setting,\n\t * leading to a late shutdown without a coordinated lifecycle stop phase.\n\t * @since 6.1.4\n\t * @see #initiateShutdown()\n\t */",
            "\t/**\n\t * 指定上下文关闭时是否发出提前关闭信号，\n\t * 释放所有空闲线程并拒绝后续任务提交。\n\t * <p>默认情况下，现有任务仍可在协调的生命周期停止阶段内完成。\n\t * 本设置仅控制上下文关闭时是否触发显式 {@link ThreadPoolExecutor#shutdown()} 调用，\n\t * 此后拒绝任务提交。\n\t * <p>自 6.1.4 起，默认为 \"false\"，宽松允许上下文关闭后仍有迟到的任务到达，\n\t * 仍参与生命周期停止阶段。注意这与 {@link #setAcceptTasksAfterContextClose} 不同，\n\t * 后者完全绕过协调的生命周期停止阶段，根本不显式等待现有任务完成。\n\t * <p>设为 \"true\" 可获得与 {@link ThreadPoolTaskScheduler}\n\t * 6.1 起默认行为类似的严格提前关闭信号。\n\t * 注意相关标志 {@link #setAcceptTasksAfterContextClose} 与\n\t * {@link #setWaitForTasksToCompleteOnShutdown} 将覆盖本设置，\n\t * 导致无协调生命周期停止阶段的延迟关闭。\n\t * @since 6.1.4\n\t * @see #initiateShutdown()\n\t */",
        ),
        (
            "\t/**\n\t * Specify a custom {@link TaskDecorator} to be applied to any {@link Runnable}\n\t * about to be executed.\n\t * <p>Note that such a decorator is not necessarily being applied to the\n\t * user-supplied {@code Runnable}/{@code Callable} but rather to the actual\n\t * execution callback (which may be a wrapper around the user-supplied task).\n\t * <p>The primary use case is to set some execution context around the task's\n\t * invocation, or to provide some monitoring/statistics for task execution.\n\t * <p><b>NOTE:</b> Exception handling in {@code TaskDecorator} implementations\n\t * is limited to plain {@code Runnable} execution via {@code execute} calls.\n\t * In case of {@code #submit} calls, the exposed {@code Runnable} will be a\n\t * {@code FutureTask} which does not propagate any exceptions; you might\n\t * have to cast it and call {@code Future#get} to evaluate exceptions.\n\t * See the {@code ThreadPoolExecutor#afterExecute} javadoc for an example\n\t * of how to access exceptions in such a {@code Future} case.\n\t * @since 4.3\n\t */",
            "\t/**\n\t * 指定应用于即将执行的任意 {@link Runnable} 的自定义 {@link TaskDecorator}。\n\t * <p>注意此类装饰器未必应用于用户提供的 {@code Runnable}/{@code Callable}，\n\t * 而是应用于实际执行回调（可能是用户任务的包装）。\n\t * <p>主要用例是在任务调用周围设置执行上下文，\n\t * 或为任务执行提供监控/统计。\n\t * <p><b>注意：</b>{@code TaskDecorator} 实现中的异常处理\n\t * 限于通过 {@code execute} 调用的普通 {@code Runnable} 执行。\n\t * 对于 {@code #submit} 调用，暴露的 {@code Runnable} 将是\n\t * 不传播任何异常的 {@code FutureTask}；\n\t * 可能需要强制转换并调用 {@code Future#get} 以评估异常。\n\t * 此类 {@code Future} 场景下如何访问异常，\n\t * 见 {@code ThreadPoolExecutor#afterExecute} Javadoc 示例。\n\t * @since 4.3\n\t */",
        ),
        (
            "\t/**\n\t * Note: This method exposes an {@link ExecutorService} to its base class\n\t * but stores the actual {@link ThreadPoolExecutor} handle internally.\n\t * Do not override this method for replacing the executor, rather just for\n\t * decorating its {@code ExecutorService} handle or storing custom state.\n\t */",
            "\t/**\n\t * 注意：本方法向基类暴露 {@link ExecutorService}，\n\t * 但内部保存实际 {@link ThreadPoolExecutor} 句柄。\n\t * 不要为替换执行器而覆盖本方法，仅用于装饰其 {@code ExecutorService} 句柄或保存自定义状态。\n\t */",
        ),
        (
            "\t/**\n\t * Create the BlockingQueue to use for the ThreadPoolExecutor.\n\t * <p>A LinkedBlockingQueue instance will be created for a positive\n\t * capacity value; a SynchronousQueue otherwise.\n\t * @param queueCapacity the specified queue capacity\n\t * @return the BlockingQueue instance\n\t * @see java.util.concurrent.LinkedBlockingQueue\n\t * @see java.util.concurrent.SynchronousQueue\n\t */",
            "\t/**\n\t * 创建 ThreadPoolExecutor 使用的 BlockingQueue。\n\t * <p>容量为正值时创建 LinkedBlockingQueue 实例；否则创建 SynchronousQueue。\n\t * @param queueCapacity 指定队列容量\n\t * @return BlockingQueue 实例\n\t * @see java.util.concurrent.LinkedBlockingQueue\n\t * @see java.util.concurrent.SynchronousQueue\n\t */",
        ),
        (
            "\t/**\n\t * Return the underlying ThreadPoolExecutor for native access.\n\t * @return the underlying ThreadPoolExecutor (never {@code null})\n\t * @throws IllegalStateException if the ThreadPoolTaskExecutor hasn't been initialized yet\n\t */",
            "\t/**\n\t * 返回底层 ThreadPoolExecutor 以供原生访问。\n\t * @return 底层 ThreadPoolExecutor（永不为 {@code null}）\n\t * @throws IllegalStateException 若 ThreadPoolTaskExecutor 尚未初始化\n\t */",
        ),
        (
            "\t/**\n\t * Return the current pool size.\n\t * @see java.util.concurrent.ThreadPoolExecutor#getPoolSize()\n\t */",
            "\t/**\n\t * 返回当前池大小。\n\t * @see java.util.concurrent.ThreadPoolExecutor#getPoolSize()\n\t */",
        ),
        (
            "\t/**\n\t * Return the current queue size.\n\t * @since 5.3.21\n\t * @see java.util.concurrent.ThreadPoolExecutor#getQueue()\n\t */",
            "\t/**\n\t * 返回当前队列大小。\n\t * @since 5.3.21\n\t * @see java.util.concurrent.ThreadPoolExecutor#getQueue()\n\t */",
        ),
        (
            "\t/**\n\t * Return the number of currently active threads.\n\t * @see java.util.concurrent.ThreadPoolExecutor#getActiveCount()\n\t */",
            "\t/**\n\t * 返回当前活动线程数。\n\t * @see java.util.concurrent.ThreadPoolExecutor#getActiveCount()\n\t */",
        ),
    ],
    "ThreadPoolTaskScheduler.java": [
        (
            "/**\n * A standard implementation of Spring's {@link TaskScheduler} interface, wrapping\n * a native {@link java.util.concurrent.ScheduledThreadPoolExecutor} and providing\n * all applicable configuration options for it. The default number of scheduler\n * threads is 1; a higher number can be configured through {@link #setPoolSize}.\n *\n * <p>This is Spring's traditional scheduler variant, staying as close as possible to\n * {@link java.util.concurrent.ScheduledExecutorService} semantics. Task execution happens\n * on the scheduler thread(s) rather than on separate execution threads. As a consequence,\n * a {@link ScheduledFuture} handle (for example, from {@link #schedule(Runnable, Instant)})\n * represents the actual completion of the provided task (or series of repeated tasks).\n *\n * @author Juergen Hoeller\n * @author Mark Fisher\n * @since 3.0\n * @see #setPoolSize\n * @see #setRemoveOnCancelPolicy\n * @see #setContinueExistingPeriodicTasksAfterShutdownPolicy\n * @see #setExecuteExistingDelayedTasksAfterShutdownPolicy\n * @see #setThreadFactory\n * @see #setErrorHandler\n * @see ThreadPoolTaskExecutor\n * @see SimpleAsyncTaskScheduler\n */",
            "/**\n * Spring {@link TaskScheduler} 接口的标准实现，\n * 包装原生 {@link java.util.concurrent.ScheduledThreadPoolExecutor} 并为其提供\n * 所有适用的配置选项。默认调度线程数为 1；\n * 可通过 {@link #setPoolSize} 配置更高数量。\n *\n * <p>这是 Spring 的传统调度器变体，尽可能贴近\n * {@link java.util.concurrent.ScheduledExecutorService} 语义。\n * 任务执行发生在调度线程上而非独立执行线程。\n * 因此 {@link ScheduledFuture} 句柄（例如来自 {@link #schedule(Runnable, Instant)}）\n * 表示所提供任务（或一系列重复任务）的实际完成。\n *\n * @author Juergen Hoeller\n * @author Mark Fisher\n * @since 3.0\n * @see #setPoolSize\n * @see #setRemoveOnCancelPolicy\n * @see #setContinueExistingPeriodicTasksAfterShutdownPolicy\n * @see #setExecuteExistingDelayedTasksAfterShutdownPolicy\n * @see #setThreadFactory\n * @see #setErrorHandler\n * @see ThreadPoolTaskExecutor\n * @see SimpleAsyncTaskScheduler\n */",
        ),
        (
            "\t/**\n\t * Set the ScheduledExecutorService's pool size.\n\t * Default is 1.\n\t * <p><b>This setting can be modified at runtime, for example through JMX.</b>\n\t */",
            "\t/**\n\t * 设置 ScheduledExecutorService 的池大小。\n\t * 默认为 1。\n\t * <p><b>此设置可在运行时修改，例如通过 JMX。</b>\n\t */",
        ),
        (
            "\t/**\n\t * Set the remove-on-cancel mode on {@link ScheduledThreadPoolExecutor}.\n\t * <p>Default is {@code false}. If set to {@code true}, the target executor will be\n\t * switched into remove-on-cancel mode (if possible).\n\t * <p><b>This setting can be modified at runtime, for example through JMX.</b>\n\t * @see ScheduledThreadPoolExecutor#setRemoveOnCancelPolicy\n\t */",
            "\t/**\n\t * 在 {@link ScheduledThreadPoolExecutor} 上设置 cancel 时移除模式。\n\t * <p>默认为 {@code false}。设为 {@code true} 时，\n\t * 目标执行器将切换为 remove-on-cancel 模式（若可能）。\n\t * <p><b>此设置可在运行时修改，例如通过 JMX。</b>\n\t * @see ScheduledThreadPoolExecutor#setRemoveOnCancelPolicy\n\t */",
        ),
        (
            "\t/**\n\t * Set whether to continue existing periodic tasks even when this executor has been shutdown.\n\t * <p>Default is {@code false}. If set to {@code true}, the target executor will be\n\t * switched into continuing periodic tasks (if possible).\n\t * <p><b>This setting can be modified at runtime, for example through JMX.</b>\n\t * @since 5.3.9\n\t * @see ScheduledThreadPoolExecutor#setContinueExistingPeriodicTasksAfterShutdownPolicy\n\t */",
            "\t/**\n\t * 设置执行器 shutdown 后是否继续现有周期性任务。\n\t * <p>默认为 {@code false}。设为 {@code true} 时，\n\t * 目标执行器将切换为继续周期性任务（若可能）。\n\t * <p><b>此设置可在运行时修改，例如通过 JMX。</b>\n\t * @since 5.3.9\n\t * @see ScheduledThreadPoolExecutor#setContinueExistingPeriodicTasksAfterShutdownPolicy\n\t */",
        ),
        (
            "\t/**\n\t * Set whether to execute existing delayed tasks even when this executor has been shutdown.\n\t * <p>Default is {@code true}. If set to {@code false}, the target executor will be\n\t * switched into dropping remaining tasks (if possible).\n\t * <p><b>This setting can be modified at runtime, for example through JMX.</b>\n\t * @since 5.3.9\n\t * @see ScheduledThreadPoolExecutor#setExecuteExistingDelayedTasksAfterShutdownPolicy\n\t */",
            "\t/**\n\t * 设置执行器 shutdown 后是否执行现有延迟任务。\n\t * <p>默认为 {@code true}。设为 {@code false} 时，\n\t * 目标执行器将切换为丢弃剩余任务（若可能）。\n\t * <p><b>此设置可在运行时修改，例如通过 JMX。</b>\n\t * @since 5.3.9\n\t * @see ScheduledThreadPoolExecutor#setExecuteExistingDelayedTasksAfterShutdownPolicy\n\t */",
        ),
        (
            "\t/**\n\t * Specify a custom {@link TaskDecorator} to be applied to any {@link Runnable}\n\t * about to be executed.\n\t * <p>Note that such a decorator is not being applied to the user-supplied\n\t * {@code Runnable}/{@code Callable} but rather to the scheduled execution\n\t * callback (a wrapper around the user-supplied task).\n\t * <p>The primary use case is to set some execution context around the task's\n\t * invocation, or to provide some monitoring/statistics for task execution.\n\t * @since 6.2\n\t */",
            "\t/**\n\t * 指定应用于即将执行的任意 {@link Runnable} 的自定义 {@link TaskDecorator}。\n\t * <p>注意此类装饰器不应用于用户提供的 {@code Runnable}/{@code Callable}，\n\t * 而是应用于调度执行回调（用户任务的包装）。\n\t * <p>主要用例是在任务调用周围设置执行上下文，\n\t * 或为任务执行提供监控/统计。\n\t * @since 6.2\n\t */",
        ),
        (
            "\t/**\n\t * Set a custom {@link ErrorHandler} strategy.\n\t */",
            "\t/**\n\t * 设置自定义 {@link ErrorHandler} 策略。\n\t */",
        ),
        (
            "\t/**\n\t * Set the clock to use for scheduling purposes.\n\t * <p>The default clock is the system clock for the default time zone.\n\t * @since 5.3\n\t * @see Clock#systemDefaultZone()\n\t */",
            "\t/**\n\t * 设置用于调度目的的时钟。\n\t * <p>默认时钟为默认时区的系统时钟。\n\t * @since 5.3\n\t * @see Clock#systemDefaultZone()\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@link ScheduledExecutorService} instance.\n\t * <p>The default implementation creates a {@link ScheduledThreadPoolExecutor}.\n\t * Can be overridden in subclasses to provide custom {@link ScheduledExecutorService} instances.\n\t * @param poolSize the specified pool size\n\t * @param threadFactory the ThreadFactory to use\n\t * @param rejectedExecutionHandler the RejectedExecutionHandler to use\n\t * @return a new ScheduledExecutorService instance\n\t * @see #afterPropertiesSet()\n\t * @see java.util.concurrent.ScheduledThreadPoolExecutor\n\t */",
            "\t/**\n\t * 创建新的 {@link ScheduledExecutorService} 实例。\n\t * <p>默认实现创建 {@link ScheduledThreadPoolExecutor}。\n\t * 子类可覆盖以提供自定义 {@link ScheduledExecutorService} 实例。\n\t * @param poolSize 指定池大小\n\t * @param threadFactory 使用的 ThreadFactory\n\t * @param rejectedExecutionHandler 使用的 RejectedExecutionHandler\n\t * @return 新的 ScheduledExecutorService 实例\n\t * @see #afterPropertiesSet()\n\t * @see java.util.concurrent.ScheduledThreadPoolExecutor\n\t */",
        ),
        (
            "\t/**\n\t * Return the underlying ScheduledExecutorService for native access.\n\t * @return the underlying ScheduledExecutorService (never {@code null})\n\t * @throws IllegalStateException if the ThreadPoolTaskScheduler hasn't been initialized yet\n\t */",
            "\t/**\n\t * 返回底层 ScheduledExecutorService 以供原生访问。\n\t * @return 底层 ScheduledExecutorService（永不为 {@code null}）\n\t * @throws IllegalStateException 若 ThreadPoolTaskScheduler 尚未初始化\n\t */",
        ),
        (
            "\t/**\n\t * Return the underlying ScheduledThreadPoolExecutor, if available.\n\t * @return the underlying ScheduledExecutorService (never {@code null})\n\t * @throws IllegalStateException if the ThreadPoolTaskScheduler hasn't been initialized yet\n\t * or if the underlying ScheduledExecutorService isn't a ScheduledThreadPoolExecutor\n\t * @see #getScheduledExecutor()\n\t */",
            "\t/**\n\t * 返回底层 ScheduledThreadPoolExecutor（若可用）。\n\t * @return 底层 ScheduledExecutorService（永不为 {@code null}）\n\t * @throws IllegalStateException 若 ThreadPoolTaskScheduler 尚未初始化，\n\t * 或底层 ScheduledExecutorService 不是 ScheduledThreadPoolExecutor\n\t * @see #getScheduledExecutor()\n\t */",
        ),
        (
            "\t/**\n\t * Return the current pool size.\n\t * <p>Requires an underlying {@link ScheduledThreadPoolExecutor}.\n\t * @see #getScheduledThreadPoolExecutor()\n\t * @see java.util.concurrent.ScheduledThreadPoolExecutor#getPoolSize()\n\t */",
            "\t/**\n\t * 返回当前池大小。\n\t * <p>需要底层 {@link ScheduledThreadPoolExecutor}。\n\t * @see #getScheduledThreadPoolExecutor()\n\t * @see java.util.concurrent.ScheduledThreadPoolExecutor#getPoolSize()\n\t */",
        ),
        (
            "\t/**\n\t * Return the number of currently active threads.\n\t * <p>Requires an underlying {@link ScheduledThreadPoolExecutor}.\n\t * @see #getScheduledThreadPoolExecutor()\n\t * @see java.util.concurrent.ScheduledThreadPoolExecutor#getActiveCount()\n\t */",
            "\t/**\n\t * 返回当前活动线程数。\n\t * <p>需要底层 {@link ScheduledThreadPoolExecutor}。\n\t * @see #getScheduledThreadPoolExecutor()\n\t * @see java.util.concurrent.ScheduledThreadPoolExecutor#getActiveCount()\n\t */",
        ),
        (
            "\t/**\n\t * Return the current setting for the remove-on-cancel mode.\n\t * <p>Requires an underlying {@link ScheduledThreadPoolExecutor}.\n\t * @deprecated in favor of direct {@link #getScheduledThreadPoolExecutor()} access\n\t */",
            "\t/**\n\t * 返回 cancel 时移除模式的当前设置。\n\t * <p>需要底层 {@link ScheduledThreadPoolExecutor}。\n\t * @deprecated 请直接使用 {@link #getScheduledThreadPoolExecutor()} 访问\n\t */",
        ),
    ],
    "SimpleAsyncTaskScheduler.java": [
        (
            "/**\n * A simple implementation of Spring's {@link TaskScheduler} interface, using\n * a single scheduler thread and executing every scheduled task in an individual\n * separate thread. This is an attractive choice with virtual threads on JDK 21,\n * expecting common usage with {@link #setVirtualThreads setVirtualThreads(true)}.\n *\n * <p><b>NOTE: Scheduling with a fixed delay enforces execution on a single\n * scheduler thread, in order to provide traditional fixed-delay semantics!</b>\n * Prefer the use of fixed rates or cron triggers instead which are a better fit\n * with this thread-per-task scheduler variant.\n *\n * <p>Supports a graceful shutdown through {@link #setTaskTerminationTimeout},\n * at the expense of task tracking overhead per execution thread at runtime.\n * Supports limiting concurrent threads through {@link #setConcurrencyLimit}.\n * By default, the number of concurrent task executions is unlimited.\n * This allows for dynamic concurrency of scheduled task executions, in contrast\n * to {@link ThreadPoolTaskScheduler} which requires a fixed pool size.\n *\n * <p><b>NOTE: This implementation does not reuse threads!</b> Consider a\n * thread-pooling TaskScheduler implementation instead, in particular for\n * scheduling a large number of short-lived tasks. Alternatively, on JDK 21,\n * consider setting {@link #setVirtualThreads} to {@code true}.\n *\n * <p>Extends {@link SimpleAsyncTaskExecutor} and can serve as a fully capable\n * replacement for it, for example, as a single shared instance serving as a\n * {@link org.springframework.core.task.TaskExecutor} as well as a {@link TaskScheduler}.\n * This is generally not the case with other executor/scheduler implementations\n * which tend to have specific constraints for the scheduler thread pool,\n * requiring a separate thread pool for general executor purposes in practice.\n *\n * <p><b>NOTE: This scheduler variant does not track the actual completion of tasks\n * but rather just the hand-off to an execution thread.</b> As a consequence,\n * a {@link ScheduledFuture} handle (for example, from {@link #schedule(Runnable, Instant)})\n * represents that hand-off rather than the actual completion of the provided task\n * (or series of repeated tasks). Also, this scheduler participates in lifecycle\n * management to a limited degree only, stopping trigger firing and fixed-delay\n * task execution but not stopping the execution of handed-off tasks.\n *\n * <p>As an alternative to the built-in thread-per-task capability, this scheduler\n * can also be configured with a separate target executor for scheduled task\n * execution through {@link #setTargetTaskExecutor}: for example, pointing to a shared\n * {@link ThreadPoolTaskExecutor} bean. This is still rather different from a\n * {@link ThreadPoolTaskScheduler} setup since it always uses a single scheduler\n * thread while dynamically dispatching to the target thread pool which may have\n * a dynamic core/max pool size range, participating in a shared concurrency limit.\n *\n * @author Juergen Hoeller\n * @since 6.1\n * @see #setVirtualThreads\n * @see #setTaskTerminationTimeout\n * @see #setConcurrencyLimit\n * @see SimpleAsyncTaskExecutor\n * @see ThreadPoolTaskScheduler\n */",
            "/**\n * Spring {@link TaskScheduler} 接口的简单实现，\n * 使用单个调度线程并在各自独立线程中执行每个定时任务。\n * 在 JDK 21 虚拟线程场景下是颇具吸引力的选择，\n * 预期常见用法为 {@link #setVirtualThreads setVirtualThreads(true)}。\n *\n * <p><b>注意：固定延迟调度强制在单个调度线程上执行，\n * 以提供传统固定延迟语义！</b>\n * 更推荐使用固定速率或 cron 触发器，\n * 它们更适合这种每任务一线程的调度器变体。\n *\n * <p>通过 {@link #setTaskTerminationTimeout} 支持优雅关闭，\n * 代价是运行时每个执行线程的任务跟踪开销。\n * 通过 {@link #setConcurrencyLimit} 支持限制并发线程数。\n * 默认并发任务执行数无限制。\n * 这允许定时任务执行的动态并发，\n * 与需要固定池大小的 {@link ThreadPoolTaskScheduler} 形成对比。\n *\n * <p><b>注意：本实现不重用线程！</b>请考虑基于线程池的 TaskScheduler 实现，\n * 尤其用于调度大量短生命周期任务。或在 JDK 21 上\n * 考虑将 {@link #setVirtualThreads} 设为 {@code true}。\n *\n * <p>继承 {@link SimpleAsyncTaskExecutor}，可完全替代它，\n * 例如作为同时充当 {@link org.springframework.core.task.TaskExecutor}\n * 与 {@link TaskScheduler} 的单一共享实例。\n * 其他执行器/调度器实现通常对调度线程池有特定约束，\n * 实践中往往需要单独线程池用于一般执行目的，本类一般并非如此。\n *\n * <p><b>注意：本调度器变体不跟踪任务的实际完成，\n * 而仅跟踪移交给执行线程。</b>因此\n * {@link ScheduledFuture} 句柄（例如来自 {@link #schedule(Runnable, Instant)}）\n * 表示该移交而非所提供任务（或一系列重复任务）的实际完成。\n * 此外，本调度器仅有限参与生命周期管理，\n * 停止触发器触发与固定延迟任务执行，但不停止已移交任务的执行。\n *\n * <p>作为内置每任务一线程能力的替代，\n * 本调度器也可通过 {@link #setTargetTaskExecutor} 配置\n * 用于定时任务执行的独立目标执行器：例如指向共享\n * {@link ThreadPoolTaskExecutor} Bean。这与\n * {@link ThreadPoolTaskScheduler} 配置仍相当不同，\n * 因其始终使用单个调度线程，\n * 同时动态分派到可能具有动态 core/max 池大小范围、\n * 参与共享并发限制的目标线程池。\n *\n * @author Juergen Hoeller\n * @since 6.1\n * @see #setVirtualThreads\n * @see #setTaskTerminationTimeout\n * @see #setConcurrencyLimit\n * @see SimpleAsyncTaskExecutor\n * @see ThreadPoolTaskScheduler\n */",
        ),
        (
            "\t/**\n\t * The default phase for an executor {@link SmartLifecycle}: {@code Integer.MAX_VALUE / 2}.\n\t * @since 6.2\n\t * @see #getPhase()\n\t * @see ExecutorConfigurationSupport#DEFAULT_PHASE\n\t */",
            "\t/**\n\t * 执行器 {@link SmartLifecycle} 的默认阶段：{@code Integer.MAX_VALUE / 2}。\n\t * @since 6.2\n\t * @see #getPhase()\n\t * @see ExecutorConfigurationSupport#DEFAULT_PHASE\n\t */",
        ),
        (
            "\t/**\n\t * Provide an {@link ErrorHandler} strategy.\n\t * @since 6.2\n\t */",
            "\t/**\n\t * 提供 {@link ErrorHandler} 策略。\n\t * @since 6.2\n\t */",
        ),
        (
            "\t/**\n\t * Set the clock to use for scheduling purposes.\n\t * <p>The default clock is the system clock for the default time zone.\n\t * @see Clock#systemDefaultZone()\n\t */",
            "\t/**\n\t * 设置用于调度目的的时钟。\n\t * <p>默认时钟为默认时区的系统时钟。\n\t * @see Clock#systemDefaultZone()\n\t */",
        ),
        (
            "\t/**\n\t * Specify the lifecycle phase for pausing and resuming this executor.\n\t * The default is {@link #DEFAULT_PHASE}.\n\t * @see SmartLifecycle#getPhase()\n\t */",
            "\t/**\n\t * 指定暂停与恢复本执行器的生命周期阶段。\n\t * 默认为 {@link #DEFAULT_PHASE}。\n\t * @see SmartLifecycle#getPhase()\n\t */",
        ),
        (
            "\t/**\n\t * Return the lifecycle phase for pausing and resuming this executor.\n\t * @see #setPhase\n\t */",
            "\t/**\n\t * 返回暂停与恢复本执行器的生命周期阶段。\n\t * @see #setPhase\n\t */",
        ),
        (
            "\t/**\n\t * Specify a custom target {@link Executor} to delegate to for\n\t * the individual execution of scheduled tasks. This can for example\n\t * be set to a separate thread pool for executing scheduled tasks,\n\t * whereas this scheduler keeps using its single scheduler thread.\n\t * <p>If not set, the regular {@link SimpleAsyncTaskExecutor}\n\t * arrangements kicks in with a new thread per task.\n\t */",
            "\t/**\n\t * 指定用于定时任务各自执行时委托的自定义目标 {@link Executor}。\n\t * 例如可设为执行定时任务的独立线程池，\n\t * 而本调度器仍使用其单个调度线程。\n\t * <p>若未设置，则启用常规 {@link SimpleAsyncTaskExecutor}\n\t * 安排，每个任务新建线程。\n\t */",
        ),
    ],
}
