LARGE_B_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "ScheduledExecutorTask.java": [
        (
            "/**\n * JavaBean that describes a scheduled executor task, consisting of the\n * {@link Runnable} and a delay plus period. The period needs to be specified;\n * there is no point in a default for it.\n *\n * <p>The {@link java.util.concurrent.ScheduledExecutorService} does not offer\n * more sophisticated scheduling options such as cron expressions.\n * Consider using {@link ThreadPoolTaskScheduler} for such needs.\n *\n * <p>Note that the {@link java.util.concurrent.ScheduledExecutorService} mechanism\n * uses a {@link Runnable} instance that is shared between repeated executions,\n * in contrast to Quartz which creates a new Job instance for each execution.\n *\n * @author Juergen Hoeller\n * @since 2.0\n * @see java.util.concurrent.ScheduledExecutorService#scheduleWithFixedDelay(java.lang.Runnable, long, long, java.util.concurrent.TimeUnit)\n * @see java.util.concurrent.ScheduledExecutorService#scheduleAtFixedRate(java.lang.Runnable, long, long, java.util.concurrent.TimeUnit)\n */",
            "/**\n * 描述定时执行器任务的 JavaBean，包含 {@link Runnable} 以及延迟与周期。\n * 必须指定周期；为其设置默认值没有意义。\n *\n * <p>{@link java.util.concurrent.ScheduledExecutorService} 不提供\n * cron 表达式等更复杂的调度选项。\n * 此类需求请考虑 {@link ThreadPoolTaskScheduler}。\n *\n * <p>注意 {@link java.util.concurrent.ScheduledExecutorService} 机制\n * 在重复执行间共享同一 {@link Runnable} 实例，\n * 与 Quartz 每次执行创建新 Job 实例不同。\n *\n * @author Juergen Hoeller\n * @since 2.0\n * @see java.util.concurrent.ScheduledExecutorService#scheduleWithFixedDelay(java.lang.Runnable, long, long, java.util.concurrent.TimeUnit)\n * @see java.util.concurrent.ScheduledExecutorService#scheduleAtFixedRate(java.lang.Runnable, long, long, java.util.concurrent.TimeUnit)\n */",
        ),
        (
            "\t/**\n\t * Create a new ScheduledExecutorTask,\n\t * to be populated via bean properties.\n\t * @see #setDelay\n\t * @see #setPeriod\n\t * @see #setFixedRate\n\t */",
            "\t/**\n\t * 创建新的 ScheduledExecutorTask，\n\t * 通过 Bean 属性填充。\n\t * @see #setDelay\n\t * @see #setPeriod\n\t * @see #setFixedRate\n\t */",
        ),
        (
            "\t/**\n\t * Create a new ScheduledExecutorTask, with default\n\t * one-time execution without delay.\n\t * @param executorTask the Runnable to schedule\n\t */",
            "\t/**\n\t * 创建新的 ScheduledExecutorTask，\n\t * 默认无延迟的一次性执行。\n\t * @param executorTask 要调度的 Runnable\n\t */",
        ),
        (
            "\t/**\n\t * Create a new ScheduledExecutorTask, with default\n\t * one-time execution with the given delay.\n\t * @param executorTask the Runnable to schedule\n\t * @param delay the delay before starting the task for the first time (ms)\n\t */",
            "\t/**\n\t * 创建新的 ScheduledExecutorTask，\n\t * 默认带给定延迟的一次性执行。\n\t * @param executorTask 要调度的 Runnable\n\t * @param delay 首次启动任务前的延迟（毫秒）\n\t */",
        ),
        (
            "\t/**\n\t * Create a new ScheduledExecutorTask.\n\t * @param executorTask the Runnable to schedule\n\t * @param delay the delay before starting the task for the first time (ms)\n\t * @param period the period between repeated task executions (ms)\n\t * @param fixedRate whether to schedule as fixed-rate execution\n\t */",
            "\t/**\n\t * 创建新的 ScheduledExecutorTask。\n\t * @param executorTask 要调度的 Runnable\n\t * @param delay 首次启动任务前的延迟（毫秒）\n\t * @param period 重复任务执行间隔（毫秒）\n\t * @param fixedRate 是否按固定速率调度\n\t */",
        ),
        (
            "\t/**\n\t * Set the Runnable to schedule as executor task.\n\t */",
            "\t/**\n\t * 设置作为执行器任务调度的 Runnable。\n\t */",
        ),
        (
            "\t/**\n\t * Return the Runnable to schedule as executor task.\n\t */",
            "\t/**\n\t * 返回作为执行器任务调度的 Runnable。\n\t */",
        ),
        (
            "\t/**\n\t * Set the delay before starting the task for the first time,\n\t * in milliseconds. Default is 0, immediately starting the\n\t * task after successful scheduling.\n\t */",
            "\t/**\n\t * 设置首次启动任务前的延迟（毫秒）。\n\t * 默认为 0，调度成功后立即启动任务。\n\t */",
        ),
        (
            "\t/**\n\t * Return the delay before starting the job for the first time.\n\t */",
            "\t/**\n\t * 返回首次启动任务前的延迟。\n\t */",
        ),
        (
            "\t/**\n\t * Set the period between repeated task executions, in milliseconds.\n\t * <p>Default is -1, leading to one-time execution. In case of a positive value,\n\t * the task will be executed repeatedly, with the given interval in-between executions.\n\t * <p>Note that the semantics of the period value vary between fixed-rate and\n\t * fixed-delay execution.\n\t * <p><b>Note:</b> A period of 0 (for example as fixed delay) is <i>not</i> supported,\n\t * simply because {@code java.util.concurrent.ScheduledExecutorService} itself\n\t * does not support it. Hence a value of 0 will be treated as one-time execution;\n\t * however, that value should never be specified explicitly in the first place!\n\t * @see #setFixedRate\n\t * @see #isOneTimeTask()\n\t * @see java.util.concurrent.ScheduledExecutorService#scheduleWithFixedDelay(Runnable, long, long, java.util.concurrent.TimeUnit)\n\t */",
            "\t/**\n\t * 设置重复任务执行间隔（毫秒）。\n\t * <p>默认为 -1，表示一次性执行。若为正值，\n\t * 任务将按给定间隔重复执行。\n\t * <p>注意 period 值在固定速率与固定延迟执行中语义不同。\n\t * <p><b>注意：</b>period 为 0（例如固定延迟）<i>不</i>受支持，\n\t * 因为 {@code java.util.concurrent.ScheduledExecutorService} 本身不支持。\n\t * 因此 0 将被视为一次性执行；\n\t * 但根本不应显式指定该值！\n\t * @see #setFixedRate\n\t * @see #isOneTimeTask()\n\t * @see java.util.concurrent.ScheduledExecutorService#scheduleWithFixedDelay(Runnable, long, long, TimeUnit)\n\t */",
        ),
        (
            "\t/**\n\t * Return the period between repeated task executions.\n\t */",
            "\t/**\n\t * 返回重复任务执行间隔。\n\t */",
        ),
        (
            "\t/**\n\t * Is this task only ever going to execute once?\n\t * @return {@code true} if this task is only ever going to execute once\n\t * @see #getPeriod()\n\t */",
            "\t/**\n\t * 本任务是否仅执行一次？\n\t * @return 若本任务仅执行一次则返回 {@code true}\n\t * @see #getPeriod()\n\t */",
        ),
        (
            "\t/**\n\t * Specify the time unit for the delay and period values.\n\t * Default is milliseconds ({@code TimeUnit.MILLISECONDS}).\n\t * @see java.util.concurrent.TimeUnit#MILLISECONDS\n\t * @see java.util.concurrent.TimeUnit#SECONDS\n\t */",
            "\t/**\n\t * 指定 delay 与 period 值的时间单位。\n\t * 默认为毫秒 ({@code TimeUnit.MILLISECONDS})。\n\t * @see java.util.concurrent.TimeUnit#MILLISECONDS\n\t * @see java.util.concurrent.TimeUnit#SECONDS\n\t */",
        ),
        (
            "\t/**\n\t * Return the time unit for the delay and period values.\n\t */",
            "\t/**\n\t * 返回 delay 与 period 值的时间单位。\n\t */",
        ),
        (
            "\t/**\n\t * Set whether to schedule as fixed-rate execution, rather than\n\t * fixed-delay execution. Default is \"false\", that is, fixed delay.\n\t * <p>See ScheduledExecutorService javadoc for details on those execution modes.\n\t * @see java.util.concurrent.ScheduledExecutorService#scheduleWithFixedDelay(java.lang.Runnable, long, long, java.util.concurrent.TimeUnit)\n\t * @see java.util.concurrent.ScheduledExecutorService#scheduleAtFixedRate(java.lang.Runnable, long, long, java.util.concurrent.TimeUnit)\n\t */",
            "\t/**\n\t * 设置是否按固定速率而非固定延迟调度。\n\t * 默认为 \"false\"，即固定延迟。\n\t * <p>执行模式详情见 ScheduledExecutorService Javadoc。\n\t * @see java.util.concurrent.ScheduledExecutorService#scheduleWithFixedDelay(java.lang.Runnable, long, long, java.util.concurrent.TimeUnit)\n\t * @see java.util.concurrent.ScheduledExecutorService#scheduleAtFixedRate(java.lang.Runnable, long, long, java.util.concurrent.TimeUnit)\n\t */",
        ),
        (
            "\t/**\n\t * Return whether to schedule as fixed-rate execution.\n\t */",
            "\t/**\n\t * 返回是否按固定速率调度。\n\t */",
        ),
    ],
    "ThreadPoolExecutorFactoryBean.java": [
        (
            "/**\n * JavaBean that allows for configuring a {@link java.util.concurrent.ThreadPoolExecutor}\n * in bean style (through its \"corePoolSize\", \"maxPoolSize\", \"keepAliveSeconds\",\n * \"queueCapacity\" properties) and exposing it as a bean reference of its native\n * {@link java.util.concurrent.ExecutorService} type.\n *\n * <p>The default configuration is a core pool size of 1, with unlimited max pool size\n * and unlimited queue capacity. This is roughly equivalent to\n * {@link java.util.concurrent.Executors#newSingleThreadExecutor()}, sharing a single\n * thread for all tasks. Setting {@link #setQueueCapacity \"queueCapacity\"} to 0 mimics\n * {@link java.util.concurrent.Executors#newCachedThreadPool()}, with immediate scaling\n * of threads in the pool to a potentially very high number. Consider also setting a\n * {@link #setMaxPoolSize \"maxPoolSize\"} at that point, as well as possibly a higher\n * {@link #setCorePoolSize \"corePoolSize\"} (see also the\n * {@link #setAllowCoreThreadTimeOut \"allowCoreThreadTimeOut\"} mode of scaling).\n *\n * <p>For an alternative, you may set up a {@link ThreadPoolExecutor} instance directly\n * using constructor injection, or use a factory method definition that points to the\n * {@link java.util.concurrent.Executors} class.\n * <b>This is strongly recommended in particular for common {@code @Bean} methods in\n * configuration classes, where this {@code FactoryBean} variant would force you to\n * return the {@code FactoryBean} type instead of the actual {@code Executor} type.</b>\n *\n * <p>If you need a timing-based {@link java.util.concurrent.ScheduledExecutorService}\n * instead, consider {@link ScheduledExecutorFactoryBean}.\n\n * @author Juergen Hoeller\n * @since 3.0\n * @see java.util.concurrent.ExecutorService\n * @see java.util.concurrent.Executors\n * @see java.util.concurrent.ThreadPoolExecutor\n */",
            "/**\n * 允许以 Bean 风格（通过 \"corePoolSize\"、\"maxPoolSize\"、\"keepAliveSeconds\"、\n * \"queueCapacity\" 属性）配置 {@link java.util.concurrent.ThreadPoolExecutor}，\n * 并将其原生 {@link java.util.concurrent.ExecutorService} 类型暴露为 Bean 引用的 JavaBean。\n *\n * <p>默认配置为核心池大小 1、无界最大池大小与无界队列容量。\n * 大致等价于 {@link java.util.concurrent.Executors#newSingleThreadExecutor()}，\n * 所有任务共享单线程。将 {@link #setQueueCapacity \"queueCapacity\"} 设为 0\n * 可模拟 {@link java.util.concurrent.Executors#newCachedThreadPool()}，\n * 池中线程可立即扩展至可能很高的数量。此时建议同时设置\n * {@link #setMaxPoolSize \"maxPoolSize\"}，以及可能更高的\n * {@link #setCorePoolSize \"corePoolSize\"}（另见\n * {@link #setAllowCoreThreadTimeOut \"allowCoreThreadTimeOut\"} 扩展模式）。\n *\n * <p>也可通过构造器注入直接配置 {@link ThreadPoolExecutor}，\n * 或使用指向 {@link java.util.concurrent.Executors} 的工厂方法定义。\n * <b>配置类中常见 {@code @Bean} 方法尤其推荐后者，\n * 因本 {@code FactoryBean} 变体会强制返回 {@code FactoryBean} 类型\n * 而非实际 {@code Executor} 类型。</b>\n *\n * <p>若需要基于时间的 {@link java.util.concurrent.ScheduledExecutorService}，\n * 请考虑 {@link ScheduledExecutorFactoryBean}。\n\n * @author Juergen Hoeller\n * @since 3.0\n * @see java.util.concurrent.ExecutorService\n * @see java.util.concurrent.Executors\n * @see java.util.concurrent.ThreadPoolExecutor\n */",
        ),
        (
            "\t/**\n\t * Set the ThreadPoolExecutor's core pool size.\n\t * Default is 1.\n\t */",
            "\t/**\n\t * 设置 ThreadPoolExecutor 的核心池大小。\n\t * 默认为 1。\n\t */",
        ),
        (
            "\t/**\n\t * Set the ThreadPoolExecutor's maximum pool size.\n\t * Default is {@code Integer.MAX_VALUE}.\n\t */",
            "\t/**\n\t * 设置 ThreadPoolExecutor 的最大池大小。\n\t * 默认为 {@code Integer.MAX_VALUE}。\n\t */",
        ),
        (
            "\t/**\n\t * Set the ThreadPoolExecutor's keep-alive seconds.\n\t * Default is 60.\n\t */",
            "\t/**\n\t * 设置 ThreadPoolExecutor 的 keep-alive 秒数。\n\t * 默认为 60。\n\t */",
        ),
        (
            "\t/**\n\t * Set the capacity for the ThreadPoolExecutor's BlockingQueue.\n\t * Default is {@code Integer.MAX_VALUE}.\n\t * <p>Any positive value will lead to a LinkedBlockingQueue instance;\n\t * any other value will lead to a SynchronousQueue instance.\n\t * @see java.util.concurrent.LinkedBlockingQueue\n\t * @see java.util.concurrent.SynchronousQueue\n\t */",
            "\t/**\n\t * 设置 ThreadPoolExecutor 的 BlockingQueue 容量。\n\t * 默认为 {@code Integer.MAX_VALUE}。\n\t * <p>任意正值将创建 LinkedBlockingQueue 实例；\n\t * 其他值将创建 SynchronousQueue 实例。\n\t * @see java.util.concurrent.LinkedBlockingQueue\n\t * @see java.util.concurrent.SynchronousQueue\n\t */",
        ),
        (
            "\t/**\n\t * Specify whether to allow core threads to time out. This enables dynamic\n\t * growing and shrinking even in combination with a non-zero queue (since\n\t * the max pool size will only grow once the queue is full).\n\t * <p>Default is \"false\".\n\t * @see java.util.concurrent.ThreadPoolExecutor#allowCoreThreadTimeOut(boolean)\n\t */",
            "\t/**\n\t * 指定是否允许核心线程超时。即使队列非空也可动态扩缩\n\t *（最大池大小仅在队列满后才增长）。\n\t * <p>默认为 \"false\"。\n\t * @see java.util.concurrent.ThreadPoolExecutor#allowCoreThreadTimeOut(boolean)\n\t */",
        ),
        (
            "\t/**\n\t * Specify whether to start all core threads, causing them to idly wait for work.\n\t * <p>Default is \"false\".\n\t * @since 5.3.14\n\t * @see java.util.concurrent.ThreadPoolExecutor#prestartAllCoreThreads\n\t */",
            "\t/**\n\t * 指定是否启动所有核心线程，使其空闲等待任务。\n\t * <p>默认为 \"false\"。\n\t * @since 5.3.14\n\t * @see java.util.concurrent.ThreadPoolExecutor#prestartAllCoreThreads\n\t */",
        ),
        (
            "\t/**\n\t * Specify whether to initiate an early shutdown signal on context close,\n\t * disposing all idle threads and rejecting further task submissions.\n\t * <p>Default is \"false\".\n\t * See {@link ThreadPoolTaskExecutor#setStrictEarlyShutdown} for details.\n\t * @since 6.1.4\n\t * @see #initiateShutdown()\n\t */",
            "\t/**\n\t * 指定上下文关闭时是否发出提前关闭信号，\n\t * 释放所有空闲线程并拒绝后续任务提交。\n\t * <p>默认为 \"false\"。\n\t * 详情见 {@link ThreadPoolTaskExecutor#setStrictEarlyShutdown}。\n\t * @since 6.1.4\n\t * @see #initiateShutdown()\n\t */",
        ),
        (
            "\t/**\n\t * Specify whether this FactoryBean should expose an unconfigurable\n\t * decorator for the created executor.\n\t * <p>Default is \"false\", exposing the raw executor as bean reference.\n\t * Switch this flag to \"true\" to strictly prevent clients from\n\t * modifying the executor's configuration.\n\t * @see java.util.concurrent.Executors#unconfigurableExecutorService\n\t */",
            "\t/**\n\t * 指定本 FactoryBean 是否应为创建的执行器暴露不可配置装饰器。\n\t * <p>默认为 \"false\"，将原始执行器作为 Bean 引用暴露。\n\t * 设为 \"true\" 可严格禁止客户端修改执行器配置。\n\t * @see java.util.concurrent.Executors#unconfigurableExecutorService\n\t */",
        ),
        (
            "\t/**\n\t * Create a new instance of {@link ThreadPoolExecutor} or a subclass thereof.\n\t * <p>The default implementation creates a standard {@link ThreadPoolExecutor}.\n\t * Can be overridden to provide custom {@link ThreadPoolExecutor} subclasses.\n\t * @param corePoolSize the specified core pool size\n\t * @param maxPoolSize the specified maximum pool size\n\t * @param keepAliveSeconds the specified keep-alive time in seconds\n\t * @param queue the BlockingQueue to use\n\t * @param threadFactory the ThreadFactory to use\n\t * @param rejectedExecutionHandler the RejectedExecutionHandler to use\n\t * @return a new ThreadPoolExecutor instance\n\t * @see #afterPropertiesSet()\n\t */",
            "\t/**\n\t * 创建新的 {@link ThreadPoolExecutor} 或其子类实例。\n\t * <p>默认实现创建标准 {@link ThreadPoolExecutor}。\n\t * 可覆盖以提供自定义 {@link ThreadPoolExecutor} 子类。\n\t * @param corePoolSize 指定核心池大小\n\t * @param maxPoolSize 指定最大池大小\n\t * @param keepAliveSeconds 指定 keep-alive 时间（秒）\n\t * @param queue 使用的 BlockingQueue\n\t * @param threadFactory 使用的 ThreadFactory\n\t * @param rejectedExecutionHandler 使用的 RejectedExecutionHandler\n\t * @return 新的 ThreadPoolExecutor 实例\n\t * @see #afterPropertiesSet()\n\t */",
        ),
        (
            "\t/**\n\t * Create the BlockingQueue to use for the ThreadPoolExecutor.\n\t * <p>A LinkedBlockingQueue instance will be created for a positive\n\t * capacity value; a SynchronousQueue else.\n\t * @param queueCapacity the specified queue capacity\n\t * @return the BlockingQueue instance\n\t * @see java.util.concurrent.LinkedBlockingQueue\n\t * @see java.util.concurrent.SynchronousQueue\n\t */",
            "\t/**\n\t * 创建 ThreadPoolExecutor 使用的 BlockingQueue。\n\t * <p>容量为正值时创建 LinkedBlockingQueue 实例；否则创建 SynchronousQueue。\n\t * @param queueCapacity 指定队列容量\n\t * @return BlockingQueue 实例\n\t * @see java.util.concurrent.LinkedBlockingQueue\n\t * @see java.util.concurrent.SynchronousQueue\n\t */",
        ),
    ],
}
