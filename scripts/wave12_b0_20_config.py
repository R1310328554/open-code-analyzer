CONFIG_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "AnnotationDrivenBeanDefinitionParser.java": [
        (
            "/**\n * Parser for the 'annotation-driven' element of the 'task' namespace.\n *\n * @author Mark Fisher\n * @author Juergen Hoeller\n * @author Ramnivas Laddad\n * @author Chris Beams\n * @author Stephane Nicoll\n * @since 3.0\n */",
            "/**\n * 'task' 命名空间中 'annotation-driven' 元素的解析器。\n *\n * @author Mark Fisher\n * @author Juergen Hoeller\n * @author Ramnivas Laddad\n * @author Chris Beams\n * @author Stephane Nicoll\n * @since 3.0\n */",
        ),
    ],
    "ContextLifecycleScheduledTaskRegistrar.java": [
        (
            "/**\n * {@link ScheduledTaskRegistrar} subclass which redirects the actual scheduling\n * of tasks to the {@link #afterSingletonsInstantiated()} callback (as of 4.1.2).\n *\n * @author Juergen Hoeller\n * @since 3.2.1\n */",
            "/**\n * 将实际任务调度重定向到 {@link #afterSingletonsInstantiated()} 回调的\n * {@link ScheduledTaskRegistrar} 子类（自 4.1.2 起）。\n *\n * @author Juergen Hoeller\n * @since 3.2.1\n */",
        ),
    ],
    "CronTask.java": [
        (
            "/**\n * {@link TriggerTask} implementation defining a {@code Runnable} to be executed according\n * to a {@linkplain org.springframework.scheduling.support.CronExpression#parse(String)\n * standard cron expression}.\n *\n * @author Chris Beams\n * @since 3.2\n * @see org.springframework.scheduling.annotation.Scheduled#cron()\n * @see ScheduledTaskRegistrar#addCronTask(CronTask)\n */",
            "/**\n * 定义按 {@linkplain org.springframework.scheduling.support.CronExpression#parse(String)\n * 标准 cron 表达式} 执行的 {@code Runnable} 的 {@link TriggerTask} 实现。\n *\n * @author Chris Beams\n * @since 3.2\n * @see org.springframework.scheduling.annotation.Scheduled#cron()\n * @see ScheduledTaskRegistrar#addCronTask(CronTask)\n */",
        ),
        (
            "\t/**\n\t * Create a new {@code CronTask}.\n\t * @param runnable the underlying task to execute\n\t * @param expression the cron expression defining when the task should be executed\n\t */",
            "\t/**\n\t * 创建新的 {@code CronTask}。\n\t * @param runnable 要执行的底层任务\n\t * @param expression 定义任务执行时机的 cron 表达式\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@code CronTask}.\n\t * @param runnable the underlying task to execute\n\t * @param cronTrigger the cron trigger defining when the task should be executed\n\t */",
            "\t/**\n\t * 创建新的 {@code CronTask}。\n\t * @param runnable 要执行的底层任务\n\t * @param cronTrigger 定义任务执行时机的 cron 触发器\n\t */",
        ),
        (
            "\t/**\n\t * Return the cron expression defining when the task should be executed.\n\t */",
            "\t/**\n\t * 返回定义任务执行时机的 cron 表达式。\n\t */",
        ),
    ],
    "DelayedTask.java": [
        (
            "/**\n * {@link Task} implementation defining a {@code Runnable} with an initial delay.\n *\n * @author Juergen Hoeller\n * @since 6.1\n */",
            "/**\n * 定义带初始延迟的 {@code Runnable} 的 {@link Task} 实现。\n *\n * @author Juergen Hoeller\n * @since 6.1\n */",
        ),
        (
            "\t/**\n\t * Create a new {@code DelayedTask}.\n\t * @param runnable the underlying task to execute\n\t * @param initialDelay the initial delay before execution of the task\n\t */",
            "\t/**\n\t * 创建新的 {@code DelayedTask}。\n\t * @param runnable 要执行的底层任务\n\t * @param initialDelay 任务执行前的初始延迟\n\t */",
        ),
        (
            "\t/**\n\t * Copy constructor.\n\t */",
            "\t/**\n\t * 拷贝构造函数。\n\t */",
        ),
        (
            "\t/**\n\t * Return the initial delay before first execution of the task.\n\t */",
            "\t/**\n\t * 返回任务首次执行前的初始延迟。\n\t */",
        ),
    ],
    "ExecutorBeanDefinitionParser.java": [
        (
            "/**\n * Parser for the 'executor' element of the 'task' namespace.\n *\n * @author Mark Fisher\n * @author Juergen Hoeller\n * @since 3.0\n */",
            "/**\n * 'task' 命名空间中 'executor' 元素的解析器。\n *\n * @author Mark Fisher\n * @author Juergen Hoeller\n * @since 3.0\n */",
        ),
    ],
    "FixedDelayTask.java": [
        (
            "/**\n * Specialization of {@link IntervalTask} for fixed-delay semantics.\n *\n * @author Juergen Hoeller\n * @author Arjen Poutsma\n * @since 5.0.2\n * @see org.springframework.scheduling.annotation.Scheduled#fixedDelay()\n * @see ScheduledTaskRegistrar#addFixedDelayTask(IntervalTask)\n */",
            "/**\n * 用于固定延迟语义的 {@link IntervalTask} 特化。\n *\n * @author Juergen Hoeller\n * @author Arjen Poutsma\n * @since 5.0.2\n * @see org.springframework.scheduling.annotation.Scheduled#fixedDelay()\n * @see ScheduledTaskRegistrar#addFixedDelayTask(IntervalTask)\n */",
        ),
        (
            "\t/**\n\t * Create a new {@code FixedDelayTask}.\n\t * @param runnable the underlying task to execute\n\t * @param interval how often in milliseconds the task should be executed\n\t * @param initialDelay the initial delay before first execution of the task\n\t * @deprecated as of 6.0, in favor on {@link #FixedDelayTask(Runnable, Duration, Duration)}\n\t */",
            "\t/**\n\t * 创建新的 {@code FixedDelayTask}。\n\t * @param runnable 要执行的底层任务\n\t * @param interval 任务执行间隔（毫秒）\n\t * @param initialDelay 任务首次执行前的初始延迟\n\t * @deprecated 自 6.0 起，请改用 {@link #FixedDelayTask(Runnable, Duration, Duration)}\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@code FixedDelayTask}.\n\t * @param runnable the underlying task to execute\n\t * @param interval how often the task should be executed\n\t * @param initialDelay the initial delay before first execution of the task\n\t * @since 6.0\n\t */",
            "\t/**\n\t * 创建新的 {@code FixedDelayTask}。\n\t * @param runnable 要执行的底层任务\n\t * @param interval 任务执行间隔\n\t * @param initialDelay 任务首次执行前的初始延迟\n\t * @since 6.0\n\t */",
        ),
    ],
    "FixedRateTask.java": [
        (
            "/**\n * Specialization of {@link IntervalTask} for fixed-rate semantics.\n *\n * @author Juergen Hoeller\n * @author Arjen Poutsma\n * @since 5.0.2\n * @see org.springframework.scheduling.annotation.Scheduled#fixedRate()\n * @see ScheduledTaskRegistrar#addFixedRateTask(IntervalTask)\n */",
            "/**\n * 用于固定速率语义的 {@link IntervalTask} 特化。\n *\n * @author Juergen Hoeller\n * @author Arjen Poutsma\n * @since 5.0.2\n * @see org.springframework.scheduling.annotation.Scheduled#fixedRate()\n * @see ScheduledTaskRegistrar#addFixedRateTask(IntervalTask)\n */",
        ),
        (
            "\t/**\n\t * Create a new {@code FixedRateTask}.\n\t * @param runnable the underlying task to execute\n\t * @param interval how often in milliseconds the task should be executed\n\t * @param initialDelay the initial delay before first execution of the task\n\t * @deprecated as of 6.0, in favor on {@link #FixedRateTask(Runnable, Duration, Duration)}\n\t */",
            "\t/**\n\t * 创建新的 {@code FixedRateTask}。\n\t * @param runnable 要执行的底层任务\n\t * @param interval 任务执行间隔（毫秒）\n\t * @param initialDelay 任务首次执行前的初始延迟\n\t * @deprecated 自 6.0 起，请改用 {@link #FixedRateTask(Runnable, Duration, Duration)}\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@code FixedRateTask}.\n\t * @param runnable the underlying task to execute\n\t * @param interval how often the task should be executed\n\t * @param initialDelay the initial delay before first execution of the task\n\t * @since 6.0\n\t */",
            "\t/**\n\t * 创建新的 {@code FixedRateTask}。\n\t * @param runnable 要执行的底层任务\n\t * @param interval 任务执行间隔\n\t * @param initialDelay 任务首次执行前的初始延迟\n\t * @since 6.0\n\t */",
        ),
    ],
    "IntervalTask.java": [
        (
            "/**\n * {@link Task} implementation defining a {@code Runnable} to be executed at a given\n * millisecond interval which may be treated as fixed-rate or fixed-delay depending on\n * context.\n *\n * @author Chris Beams\n * @author Arjen Poutsma\n * @since 3.2\n * @see ScheduledTaskRegistrar#addFixedRateTask(IntervalTask)\n * @see ScheduledTaskRegistrar#addFixedDelayTask(IntervalTask)\n */",
            "/**\n * 定义在指定毫秒间隔执行的 {@code Runnable} 的 {@link Task} 实现，\n * 根据上下文可视为固定速率或固定延迟。\n *\n * @author Chris Beams\n * @author Arjen Poutsma\n * @since 3.2\n * @see ScheduledTaskRegistrar#addFixedRateTask(IntervalTask)\n * @see ScheduledTaskRegistrar#addFixedDelayTask(IntervalTask)\n */",
        ),
        (
            "\t/**\n\t * Create a new {@code IntervalTask}.\n\t * @param runnable the underlying task to execute\n\t * @param interval how often in milliseconds the task should be executed\n\t * @param initialDelay the initial delay before first execution of the task\n\t * @deprecated as of 6.0, in favor on {@link #IntervalTask(Runnable, Duration, Duration)}\n\t */",
            "\t/**\n\t * 创建新的 {@code IntervalTask}。\n\t * @param runnable 要执行的底层任务\n\t * @param interval 任务执行间隔（毫秒）\n\t * @param initialDelay 任务首次执行前的初始延迟\n\t * @deprecated 自 6.0 起，请改用 {@link #IntervalTask(Runnable, Duration, Duration)}\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@code IntervalTask} with no initial delay.\n\t * @param runnable the underlying task to execute\n\t * @param interval how often in milliseconds the task should be executed\n\t * @deprecated as of 6.0, in favor on {@link #IntervalTask(Runnable, Duration)}\n\t */",
            "\t/**\n\t * 创建无初始延迟的新 {@code IntervalTask}。\n\t * @param runnable 要执行的底层任务\n\t * @param interval 任务执行间隔（毫秒）\n\t * @deprecated 自 6.0 起，请改用 {@link #IntervalTask(Runnable, Duration)}\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@code IntervalTask} with no initial delay.\n\t * @param runnable the underlying task to execute\n\t * @param interval how often the task should be executed\n\t * @since 6.0\n\t */",
            "\t/**\n\t * 创建无初始延迟的新 {@code IntervalTask}。\n\t * @param runnable 要执行的底层任务\n\t * @param interval 任务执行间隔\n\t * @since 6.0\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@code IntervalTask}.\n\t * @param runnable the underlying task to execute\n\t * @param interval how often the task should be executed\n\t * @param initialDelay the initial delay before first execution of the task\n\t * @since 6.0\n\t */",
            "\t/**\n\t * 创建新的 {@code IntervalTask}。\n\t * @param runnable 要执行的底层任务\n\t * @param interval 任务执行间隔\n\t * @param initialDelay 任务首次执行前的初始延迟\n\t * @since 6.0\n\t */",
        ),
        (
            "\t/**\n\t * Copy constructor.\n\t */",
            "\t/**\n\t * 拷贝构造函数。\n\t */",
        ),
        (
            "\t/**\n\t * Return how often in milliseconds the task should be executed.\n\t * @deprecated as of 6.0, in favor of {@link #getIntervalDuration()}\n\t */",
            "\t/**\n\t * 返回任务执行间隔（毫秒）。\n\t * @deprecated 自 6.0 起，请改用 {@link #getIntervalDuration()}\n\t */",
        ),
        (
            "\t/**\n\t * Return how often the task should be executed.\n\t * @since 6.0\n\t */",
            "\t/**\n\t * 返回任务执行间隔。\n\t * @since 6.0\n\t */",
        ),
        (
            "\t/**\n\t * Return the initial delay before first execution of the task.\n\t * @deprecated as of 6.0, in favor of {@link #getInitialDelayDuration()}\n\t */",
            "\t/**\n\t * 返回任务首次执行前的初始延迟。\n\t * @deprecated 自 6.0 起，请改用 {@link #getInitialDelayDuration()}\n\t */",
        ),
        (
            "\t/**\n\t * Return the initial delay before first execution of the task.\n\t * @since 6.0\n\t */",
            "\t/**\n\t * 返回任务首次执行前的初始延迟。\n\t * @since 6.0\n\t */",
        ),
    ],
}
