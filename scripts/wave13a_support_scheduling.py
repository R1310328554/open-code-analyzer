"""Chinese JavaDoc replacements for springframework wave13a scheduling support [0:9]."""

SUPPORT_SCHEDULING_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "NoOpTaskScheduler.java": [
        (
            "/**\n * A basic, no operation {@link TaskScheduler} implementation suitable\n * for disabling scheduling, typically used for test setups.\n *\n * <p>Will accept any scheduling request but never actually execute it.\n *\n * @author Juergen Hoeller\n * @since 6.1.3\n */",
            "/**\n * 基础的空操作 {@link TaskScheduler} 实现，适用于禁用调度，\n * 通常用于测试环境配置。\n *\n * <p>接受任何调度请求，但从不实际执行。\n *\n * @author Juergen Hoeller\n * @since 6.1.3\n */",
        ),
    ],
    "PeriodicTrigger.java": [
        (
            "/**\n * A trigger for periodic task execution. The period may be applied as either\n * fixed-rate or fixed-delay, and an initial delay value may also be configured.\n * The default initial delay is 0, and the default behavior is fixed-delay\n * (i.e. the interval between successive executions is measured from each\n * <i>completion</i> time). To measure the interval between the\n * scheduled <i>start</i> time of each execution instead, set the\n * 'fixedRate' property to {@code true}.\n *\n * <p>Note that the TaskScheduler interface already defines methods for scheduling\n * tasks at fixed-rate or with fixed-delay. Both also support an optional value\n * for the initial delay. Those methods should be used directly whenever\n * possible. The value of this Trigger implementation is that it can be used\n * within components that rely on the Trigger abstraction. For example, it may\n * be convenient to allow periodic triggers, cron-based triggers, and even\n * custom Trigger implementations to be used interchangeably.\n *\n * @author Mark Fisher\n * @since 3.0\n */",
            "/**\n * 用于周期性任务执行的触发器。周期可按固定速率或固定延迟应用，\n * 也可配置初始延迟。默认初始延迟为 0，默认行为为固定延迟\n *（即连续两次执行之间的间隔从每次<i>完成</i>时刻起算）。\n * 若要从各次调度的<i>开始</i>时刻起算间隔，\n * 将 {@code fixedRate} 属性设为 {@code true}。\n *\n * <p>注意：{@link TaskScheduler} 接口已定义固定速率与固定延迟的调度方法，\n * 且均支持可选的初始延迟。应尽可能直接使用这些方法。\n * 本 {@link Trigger} 实现的价值在于可在依赖 Trigger 抽象层的组件中使用，\n * 例如便于周期触发器、cron 触发器乃至自定义 Trigger 实现互换使用。\n *\n * @author Mark Fisher\n * @since 3.0\n */",
        ),
        (
            "\t/**\n\t * Create a trigger with the given period in milliseconds.\n\t * @deprecated as of 6.0, in favor on {@link #PeriodicTrigger(Duration)}\n\t */",
            "\t/**\n\t * 以给定毫秒周期创建触发器。\n\t * @deprecated 自 6.0 起，请改用 {@link #PeriodicTrigger(Duration)}\n\t */",
        ),
        (
            "\t/**\n\t * Create a trigger with the given period and time unit. The time unit will\n\t * apply not only to the period but also to any 'initialDelay' value, if\n\t * configured on this Trigger later via {@link #setInitialDelay(long)}.\n\t * @deprecated as of 6.0, in favor on {@link #PeriodicTrigger(Duration)}\n\t */",
            "\t/**\n\t * 以给定周期与时间单位创建触发器。该时间单位不仅作用于周期，\n\t * 也作用于后续通过 {@link #setInitialDelay(long)} 配置的 initialDelay。\n\t * @deprecated 自 6.0 起，请改用 {@link #PeriodicTrigger(Duration)}\n\t */",
        ),
        (
            "\t/**\n\t * Create a trigger with the given period as a duration.\n\t * @since 6.0\n\t */",
            "\t/**\n\t * 以给定 {@link Duration} 周期创建触发器。\n\t * @since 6.0\n\t */",
        ),
        (
            "\t/**\n\t * Return this trigger's period.\n\t * @since 5.0.2\n\t * @deprecated as of 6.0, in favor on {@link #getPeriodDuration()}\n\t */",
            "\t/**\n\t * 返回本触发器的周期。\n\t * @since 5.0.2\n\t * @deprecated 自 6.0 起，请改用 {@link #getPeriodDuration()}\n\t */",
        ),
        (
            "\t/**\n\t * Return this trigger's period.\n\t * @since 6.0\n\t */",
            "\t/**\n\t * 返回本触发器的周期。\n\t * @since 6.0\n\t */",
        ),
        (
            "\t/**\n\t * Return this trigger's time unit (milliseconds by default).\n\t * @since 5.0.2\n\t * @deprecated as of 6.0, with no direct replacement\n\t */",
            "\t/**\n\t * 返回本触发器的时间单位（默认为毫秒）。\n\t * @since 5.0.2\n\t * @deprecated 自 6.0 起，无直接替代方法\n\t */",
        ),
        (
            "\t/**\n\t * Specify the delay for the initial execution. It will be evaluated in\n\t * terms of this trigger's {@link TimeUnit}. If no time unit was explicitly\n\t * provided upon instantiation, the default is milliseconds.\n\t * @deprecated as of 6.0, in favor of {@link #setInitialDelay(Duration)}\n\t */",
            "\t/**\n\t * 指定首次执行的延迟，按本触发器的 {@link TimeUnit} 解释。\n\t * 若实例化时未显式提供时间单位，则默认为毫秒。\n\t * @deprecated 自 6.0 起，请改用 {@link #setInitialDelay(Duration)}\n\t */",
        ),
        (
            "\t/**\n\t * Specify the delay for the initial execution.\n\t * @since 6.0\n\t */",
            "\t/**\n\t * 指定首次执行的延迟。\n\t * @since 6.0\n\t */",
        ),
        (
            "\t/**\n\t * Return the initial delay, or 0 if none.\n\t * @since 5.0.2\n\t * @deprecated as of 6.0, in favor on {@link #getInitialDelayDuration()}\n\t */",
            "\t/**\n\t * 返回初始延迟；若无则返回 0。\n\t * @since 5.0.2\n\t * @deprecated 自 6.0 起，请改用 {@link #getInitialDelayDuration()}\n\t */",
        ),
        (
            "\t/**\n\t * Return the initial delay, or {@code null} if none.\n\t * @since 6.0\n\t */",
            "\t/**\n\t * 返回初始延迟；若无则返回 {@code null}。\n\t * @since 6.0\n\t */",
        ),
        (
            "\t/**\n\t * Specify whether the periodic interval should be measured between the\n\t * scheduled start times rather than between actual completion times.\n\t * The latter, \"fixed delay\" behavior, is the default.\n\t */",
            "\t/**\n\t * 指定周期间隔是否按各次调度的开始时间（而非实际完成时间）计量。\n\t * 后者即“固定延迟”行为，为默认方式。\n\t */",
        ),
        (
            "\t/**\n\t * Return whether this trigger uses fixed rate ({@code true}) or\n\t * fixed delay ({@code false}) behavior.\n\t * @since 5.0.2\n\t */",
            "\t/**\n\t * 返回本触发器是否使用固定速率（{@code true}）或固定延迟（{@code false}）。\n\t * @since 5.0.2\n\t */",
        ),
        (
            "\t/**\n\t * Returns the time after which a task should run again.\n\t */",
            "\t/**\n\t * 返回任务下次应执行的时间。\n\t */",
        ),
    ],
    "ScheduledMethodRunnable.java": [
        (
            "/**\n * Variant of {@link MethodInvokingRunnable} meant to be used for processing\n * of no-arg scheduled methods. Propagates user exceptions to the caller,\n * assuming that an error strategy for Runnables is in place.\n *\n * @author Juergen Hoeller\n * @author Brian Clozel\n * @since 3.0.6\n * @see org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor\n */",
            "/**\n * {@link MethodInvokingRunnable} 的变体，用于处理无参调度方法。\n * 将用户异常传播给调用方，前提是已为 Runnable 配置错误处理策略。\n *\n * @author Juergen Hoeller\n * @author Brian Clozel\n * @since 3.0.6\n * @see org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor\n */",
        ),
        (
            "\t/**\n\t * Create a {@code ScheduledMethodRunnable} for the given target instance,\n\t * calling the specified method.\n\t * @param target the target instance to call the method on\n\t * @param method the target method to call\n\t * @param qualifier a qualifier associated with this Runnable,\n\t * for example, for determining a scheduler to run this scheduled method on\n\t * @param observationRegistrySupplier a supplier for the observation registry to use\n\t * @since 6.1\n\t */",
            "\t/**\n\t * 为给定目标实例创建 {@code ScheduledMethodRunnable}，调用指定方法。\n\t * @param target 要调用方法的目标实例\n\t * @param method 要调用的目标方法\n\t * @param qualifier 与本 Runnable 关联的限定符，\n\t * 例如用于确定运行该调度方法的调度器\n\t * @param observationRegistrySupplier 观测注册表的供应器\n\t * @since 6.1\n\t */",
        ),
        (
            "\t/**\n\t * Create a {@code ScheduledMethodRunnable} for the given target instance,\n\t * calling the specified method.\n\t * @param target the target instance to call the method on\n\t * @param method the target method to call\n\t */",
            "\t/**\n\t * 为给定目标实例创建 {@code ScheduledMethodRunnable}，调用指定方法。\n\t * @param target 要调用方法的目标实例\n\t * @param method 要调用的目标方法\n\t */",
        ),
        (
            "\t/**\n\t * Create a {@code ScheduledMethodRunnable} for the given target instance,\n\t * calling the specified method by name.\n\t * @param target the target instance to call the method on\n\t * @param methodName the name of the target method\n\t * @throws NoSuchMethodException if the specified method does not exist\n\t */",
            "\t/**\n\t * 为给定目标实例创建 {@code ScheduledMethodRunnable}，按名称调用指定方法。\n\t * @param target 要调用方法的目标实例\n\t * @param methodName 目标方法名\n\t * @throws NoSuchMethodException 若指定方法不存在\n\t */",
        ),
        (
            "\t/**\n\t * Return the target instance to call the method on.\n\t */",
            "\t/**\n\t * 返回要调用方法的目标实例。\n\t */",
        ),
        (
            "\t/**\n\t * Return the target method to call.\n\t */",
            "\t/**\n\t * 返回要调用的目标方法。\n\t */",
        ),
    ],
    "ScheduledTaskObservationContext.java": [
        (
            "/**\n * Context that holds information for observation metadata collection during the\n * {@link ScheduledTaskObservationDocumentation#TASKS_SCHEDULED_EXECUTION execution of scheduled tasks}.\n *\n * @author Brian Clozel\n * @since 6.1\n */",
            "/**\n * 在{@link ScheduledTaskObservationDocumentation#TASKS_SCHEDULED_EXECUTION 调度任务执行}\n * 期间保存观测元数据收集信息的上下文。\n *\n * @author Brian Clozel\n * @since 6.1\n */",
        ),
        (
            "\t/**\n\t * Create a new observation context for a task, given the target object\n\t * and the method to be called.\n\t * @param target the target object that is called for task execution\n\t * @param method the method that is called for task execution\n\t */",
            "\t/**\n\t * 根据目标对象与待调用方法，为任务创建新的观测上下文。\n\t * @param target 任务执行时调用的目标对象\n\t * @param method 任务执行时调用的方法\n\t */",
        ),
        (
            "\t/**\n\t * Return the type of the target object.\n\t */",
            "\t/**\n\t * 返回目标对象的类型。\n\t */",
        ),
        (
            "\t/**\n\t * Return the method that is called for task execution.\n\t */",
            "\t/**\n\t * 返回任务执行时调用的方法。\n\t */",
        ),
        (
            "\t/**\n\t * Return whether the task execution is complete.\n\t * <p>If an observation has ended and the task is not complete, this means\n\t * that an {@link #getError() error} was raised or that the task execution got cancelled\n\t * during its execution.\n\t */",
            "\t/**\n\t * 返回任务执行是否已完成。\n\t * <p>若观测已结束而任务未完成，表示执行期间发生了\n\t * {@link #getError() 错误}或任务被取消。\n\t */",
        ),
        (
            "\t/**\n\t * Set whether the task execution has completed.\n\t */",
            "\t/**\n\t * 设置任务执行是否已完成。\n\t */",
        ),
    ],
    "ScheduledTaskObservationConvention.java": [
        (
            "/**\n * Interface for an {@link ObservationConvention} for\n * {@link ScheduledTaskObservationDocumentation#TASKS_SCHEDULED_EXECUTION scheduled task executions}.\n *\n * @author Brian Clozel\n * @since 6.1\n */",
            "/**\n * 用于{@link ScheduledTaskObservationDocumentation#TASKS_SCHEDULED_EXECUTION 调度任务执行}\n * 的 {@link ObservationConvention} 接口。\n *\n * @author Brian Clozel\n * @since 6.1\n */",
        ),
    ],
    "ScheduledTaskObservationDocumentation.java": [
        (
            "/**\n * Documented {@link io.micrometer.common.KeyValue KeyValues} for the observations on\n * executions of {@link org.springframework.scheduling.annotation.Scheduled scheduled tasks}\n *\n * <p>This class is used by automated tools to document KeyValues attached to the\n * {@code @Scheduled} observations.\n *\n * @author Brian Clozel\n * @since 6.1\n */",
            "/**\n * 针对 {@link org.springframework.scheduling.annotation.Scheduled 调度任务}\n * 执行观测的已文档化 {@link io.micrometer.common.KeyValue KeyValue}。\n *\n * <p>本类供自动化工具记录附加于 {@code @Scheduled} 观测的 KeyValue。\n *\n * @author Brian Clozel\n * @since 6.1\n */",
        ),
        (
            "\t/**\n\t * Observations on executions of {@link org.springframework.scheduling.annotation.Scheduled} tasks.\n\t */",
            "\t/**\n\t * 对 {@link org.springframework.scheduling.annotation.Scheduled} 任务执行的观测。\n\t */",
        ),
        (
            "\t\t/**\n\t\t * Name of the method that is executed for the scheduled task.\n\t\t */",
            "\t\t/**\n\t\t * 调度任务所执行方法的名称。\n\t\t */",
        ),
        (
            "\t\t/**\n\t\t * {@link Class#getCanonicalName() Canonical name} of the target type that owns the scheduled method.\n\t\t */",
            "\t\t/**\n\t\t * 拥有调度方法的目标类型的 {@link Class#getCanonicalName() 规范名称}。\n\t\t */",
        ),
        (
            "\t\t/**\n\t\t * Name of the exception thrown during task execution, or {@value KeyValue#NONE_VALUE} if no exception was thrown.\n\t\t */",
            "\t\t/**\n\t\t * 任务执行期间抛出的异常名称；若未抛出异常则为 {@value KeyValue#NONE_VALUE}。\n\t\t */",
        ),
        (
            "\t\t/**\n\t\t * Outcome of the scheduled task execution.\n\t\t */",
            "\t\t/**\n\t\t * 调度任务执行的结果。\n\t\t */",
        ),
    ],
    "SimpleTriggerContext.java": [
        (
            "/**\n * Simple data holder implementation of the {@link TriggerContext} interface.\n *\n * @author Juergen Hoeller\n * @since 3.0\n */",
            "/**\n * {@link TriggerContext} 接口的简单数据持有者实现。\n *\n * @author Juergen Hoeller\n * @since 3.0\n */",
        ),
        (
            "\t/**\n\t * Create a SimpleTriggerContext with all time values set to {@code null},\n\t * exposing the system clock for the default time zone.\n\t */",
            "\t/**\n\t * 创建所有时间值均为 {@code null} 的 SimpleTriggerContext，\n\t * 使用默认时区的系统时钟。\n\t */",
        ),
        (
            "\t/**\n\t * Create a SimpleTriggerContext with the given time values,\n\t * exposing the system clock for the default time zone.\n\t * @param lastScheduledExecutionTime last <i>scheduled</i> execution time\n\t * @param lastActualExecutionTime last <i>actual</i> execution time\n\t * @param lastCompletionTime last completion time\n\t * @deprecated as of 6.0, in favor of {@link #SimpleTriggerContext(Instant, Instant, Instant)}\n\t */",
            "\t/**\n\t * 以给定时间值创建 SimpleTriggerContext，使用默认时区的系统时钟。\n\t * @param lastScheduledExecutionTime 上次<i>计划</i>执行时间\n\t * @param lastActualExecutionTime 上次<i>实际</i>执行时间\n\t * @param lastCompletionTime 上次完成时间\n\t * @deprecated 自 6.0 起，请改用 {@link #SimpleTriggerContext(Instant, Instant, Instant)}\n\t */",
        ),
        (
            "\t/**\n\t * Create a SimpleTriggerContext with the given time values,\n\t * exposing the system clock for the default time zone.\n\t * @param lastScheduledExecution last <i>scheduled</i> execution time\n\t * @param lastActualExecution last <i>actual</i> execution time\n\t * @param lastCompletion last completion time\n\t */",
            "\t/**\n\t * 以给定时间值创建 SimpleTriggerContext，使用默认时区的系统时钟。\n\t * @param lastScheduledExecution 上次<i>计划</i>执行时间\n\t * @param lastActualExecution 上次<i>实际</i>执行时间\n\t * @param lastCompletion 上次完成时间\n\t */",
        ),
        (
            "\t/**\n\t * Create a SimpleTriggerContext with all time values set to {@code null},\n\t * exposing the given clock.\n\t * @param clock the clock to use for trigger calculation\n\t * @since 5.3\n\t * @see #update(Instant, Instant, Instant)\n\t */",
            "\t/**\n\t * 创建所有时间值均为 {@code null} 的 SimpleTriggerContext，使用给定时钟。\n\t * @param clock 用于触发器计算的时钟\n\t * @since 5.3\n\t * @see #update(Instant, Instant, Instant)\n\t */",
        ),
        (
            "\t/**\n\t * Update this holder's state with the latest time values.\n \t * @param lastScheduledExecutionTime last <i>scheduled</i> execution time\n\t * @param lastActualExecutionTime last <i>actual</i> execution time\n\t * @param lastCompletionTime last completion time\n\t * @deprecated as of 6.0, in favor of {@link #update(Instant, Instant, Instant)}\n\t */",
            "\t/**\n\t * 以最新时间值更新本持有者的状态。\n \t * @param lastScheduledExecutionTime 上次<i>计划</i>执行时间\n\t * @param lastActualExecutionTime 上次<i>实际</i>执行时间\n\t * @param lastCompletionTime 上次完成时间\n\t * @deprecated 自 6.0 起，请改用 {@link #update(Instant, Instant, Instant)}\n\t */",
        ),
        (
            "\t/**\n\t * Update this holder's state with the latest time values.\n \t * @param lastScheduledExecution last <i>scheduled</i> execution time\n\t * @param lastActualExecution last <i>actual</i> execution time\n\t * @param lastCompletion last completion time\n\t */",
            "\t/**\n\t * 以最新时间值更新本持有者的状态。\n \t * @param lastScheduledExecution 上次<i>计划</i>执行时间\n\t * @param lastActualExecution 上次<i>实际</i>执行时间\n\t * @param lastCompletion 上次完成时间\n\t */",
        ),
    ],
    "TaskUtils.java": [
        (
            "/**\n * Utility methods for decorating tasks with error handling.\n *\n * <p><b>NOTE:</b> This class is intended for internal use by Spring's scheduler\n * implementations. It is only public so that it may be accessed from impl classes\n * within other packages. It is <i>not</i> intended for general use.\n *\n * @author Mark Fisher\n * @author Juergen Hoeller\n * @since 3.0\n */",
            "/**\n * 为任务装饰错误处理逻辑的实用方法。\n *\n * <p><b>注意：</b>本类供 Spring 调度器实现内部使用。\n * 仅因其他包中的实现类需要访问而公开，<i>不</i>面向一般用途。\n *\n * @author Mark Fisher\n * @author Juergen Hoeller\n * @since 3.0\n */",
        ),
        (
            "\t/**\n\t * An ErrorHandler strategy that will log the Exception but perform\n\t * no further handling. This will suppress the error so that\n\t * subsequent executions of the task will not be prevented.\n\t */",
            "\t/**\n\t * 记录异常但不作进一步处理的 ErrorHandler 策略。\n\t * 会抑制错误，以免阻止任务的后续执行。\n\t */",
        ),
        (
            "\t/**\n\t * An ErrorHandler strategy that will log at error level and then\n\t * re-throw the Exception. Note: this will typically prevent subsequent\n\t * execution of a scheduled task.\n\t */",
            "\t/**\n\t * 以 error 级别记录并重新抛出异常的 ErrorHandler 策略。\n\t * 注意：这通常会阻止调度任务的后续执行。\n\t */",
        ),
        (
            "\t/**\n\t * Decorate the task for error handling. If the provided {@link ErrorHandler}\n\t * is not {@code null}, it will be used. Otherwise, repeating tasks will have\n\t * errors suppressed by default whereas one-shot tasks will have errors\n\t * propagated by default since those errors may be expected through the\n\t * returned {@link Future}. In both cases, the errors will be logged.\n\t */",
            "\t/**\n\t * 为任务装饰错误处理。若提供的 {@link ErrorHandler} 非 {@code null} 则使用之；\n\t * 否则重复任务默认抑制错误，一次性任务默认传播错误\n\t *（因其错误可能通过返回的 {@link Future} 被预期）。两种情况下均会记录错误。\n\t */",
        ),
        (
            "\t/**\n\t * Return the default {@link ErrorHandler} implementation based on the boolean\n\t * value indicating whether the task will be repeating or not. For repeating tasks\n\t * it will suppress errors, but for one-time tasks it will propagate. In both\n\t * cases, the error will be logged.\n\t */",
            "\t/**\n\t * 根据任务是否重复，返回默认 {@link ErrorHandler} 实现。\n\t * 重复任务抑制错误，一次性任务传播错误；两种情况下均会记录错误。\n\t */",
        ),
        (
            "\t/**\n\t * An {@link ErrorHandler} implementation that logs the Throwable at error\n \t * level. It does not perform any additional error handling. This can be\n \t * useful when suppression of errors is the intended behavior.\n\t */",
            "\t/**\n\t * 在 error 级别记录 Throwable 的 {@link ErrorHandler} 实现，\n \t * 不作额外错误处理。适用于有意抑制错误的场景。\n\t */",
        ),
        (
            "\t/**\n\t * An {@link ErrorHandler} implementation that logs the Throwable at error\n\t * level and then propagates it.\n\t */",
            "\t/**\n\t * 在 error 级别记录 Throwable 并传播的 {@link ErrorHandler} 实现。\n\t */",
        ),
    ],
}
