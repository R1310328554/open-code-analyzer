#!/usr/bin/env python3
"""Chinese-annotate Spring Framework 7.0.8 wave-12 batch [20:40]."""
from __future__ import annotations

import importlib.util
import json
import re
import shutil
import sys
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "springframework/7.0.8"
ORIGINAL = VER / "original"
ANALYZED = VER / "analyzed"
QUEUE = VER / "_reports/class-queue"
BATCH_FILES = json.loads((QUEUE / "batch.json").read_text())["files"][20:40]

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "OneTimeTask.java": [
        (
            "/**\n * {@link Task} implementation defining a {@code Runnable} with an initial delay.\n *\n * @author Juergen Hoeller\n * @since 6.1\n * @see ScheduledTaskRegistrar#addOneTimeTask(DelayedTask)\n */",
            "/**\n * 定义带初始延迟的 {@code Runnable} 的 {@link Task} 实现。\n *\n * @author Juergen Hoeller\n * @since 6.1\n * @see ScheduledTaskRegistrar#addOneTimeTask(DelayedTask)\n */",
        ),
        (
            "\t/**\n\t * Create a new {@code DelayedTask}.\n\t * @param runnable the underlying task to execute\n\t * @param initialDelay the initial delay before execution of the task\n\t */",
            "\t/**\n\t * 创建新的 {@code DelayedTask}。\n\t * @param runnable 要执行的底层任务\n\t * @param initialDelay 任务执行前的初始延迟\n\t */",
        ),
    ],
    "ScheduledTask.java": [
        (
            "/**\n * A representation of a scheduled task at runtime,\n * used as a return value for scheduling methods.\n *\n * @author Juergen Hoeller\n * @author Brian Clozel\n * @since 4.3\n * @see ScheduledTaskRegistrar#scheduleCronTask(CronTask)\n * @see ScheduledTaskRegistrar#scheduleFixedRateTask(FixedRateTask)\n * @see ScheduledTaskRegistrar#scheduleFixedDelayTask(FixedDelayTask)\n * @see ScheduledFuture\n */",
            "/**\n * 运行时定时任务的表示，用作调度方法的返回值。\n *\n * @author Juergen Hoeller\n * @author Brian Clozel\n * @since 4.3\n * @see ScheduledTaskRegistrar#scheduleCronTask(CronTask)\n * @see ScheduledTaskRegistrar#scheduleFixedRateTask(FixedRateTask)\n * @see ScheduledTaskRegistrar#scheduleFixedDelayTask(FixedDelayTask)\n * @see ScheduledFuture\n */",
        ),
        (
            "\t/**\n\t * Return the underlying task (typically a {@link CronTask},\n\t * {@link FixedRateTask} or {@link FixedDelayTask}).\n\t * @since 5.0.2\n\t */",
            "\t/**\n\t * 返回底层任务（通常为 {@link CronTask}、\n\t * {@link FixedRateTask} 或 {@link FixedDelayTask}）。\n\t * @since 5.0.2\n\t */",
        ),
        (
            "\t/**\n\t * Trigger cancellation of this scheduled task.\n\t * <p>This variant will force interruption of the task if still running.\n\t * @see #cancel(boolean)\n\t */",
            "\t/**\n\t * 触发取消本定时任务。\n\t * <p>若任务仍在运行，此变体将强制中断。\n\t * @see #cancel(boolean)\n\t */",
        ),
        (
            "\t/**\n\t * Trigger cancellation of this scheduled task.\n\t * @param mayInterruptIfRunning whether to force interruption of the task\n\t * if still running (specify {@code false} to allow the task to complete)\n\t * @since 5.3.18\n\t * @see ScheduledFuture#cancel(boolean)\n\t */",
            "\t/**\n\t * 触发取消本定时任务。\n\t * @param mayInterruptIfRunning 若任务仍在运行是否强制中断\n\t * （指定 {@code false} 允许任务完成）\n\t * @since 5.3.18\n\t * @see ScheduledFuture#cancel(boolean)\n\t */",
        ),
        (
            "\t/**\n\t * Return the next scheduled execution of the task, or {@code null}\n\t * if the task has been cancelled or no new execution is scheduled.\n\t * @since 6.2\n\t */",
            "\t/**\n\t * 返回任务下次计划执行时间，若任务已取消或未安排新执行则返回 {@code null}。\n\t * @since 6.2\n\t */",
        ),
    ],
    "ScheduledTaskHolder.java": [
        (
            "/**\n * Common interface for exposing locally scheduled tasks.\n *\n * @author Juergen Hoeller\n * @since 5.0.2\n * @see ScheduledTaskRegistrar\n * @see org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor\n */",
            "/**\n * 暴露本地定时任务的通用接口。\n *\n * @author Juergen Hoeller\n * @since 5.0.2\n * @see ScheduledTaskRegistrar\n * @see org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor\n */",
        ),
        (
            "\t/**\n\t * Return an overview of the tasks that have been scheduled by this instance.\n\t */",
            "\t/**\n\t * 返回本实例已调度任务的概览。\n\t */",
        ),
    ],
    "ScheduledTasksBeanDefinitionParser.java": [
        (
            "/**\n * Parser for the 'scheduled-tasks' element of the scheduling namespace.\n *\n * @author Mark Fisher\n * @author Chris Beams\n * @since 3.0\n */",
            "/**\n * 调度命名空间中 'scheduled-tasks' 元素的解析器。\n *\n * @author Mark Fisher\n * @author Chris Beams\n * @since 3.0\n */",
        ),
    ],
    "SchedulerBeanDefinitionParser.java": [
        (
            "/**\n * Parser for the 'scheduler' element of the 'task' namespace.\n *\n * @author Mark Fisher\n * @since 3.0\n */",
            "/**\n * 'task' 命名空间中 'scheduler' 元素的解析器。\n *\n * @author Mark Fisher\n * @since 3.0\n */",
        ),
    ],
    "Task.java": [
        (
            "/**\n * Holder class defining a {@code Runnable} to be executed as a task, typically at a\n * scheduled time or interval. See subclass hierarchy for various scheduling approaches.\n *\n * @author Chris Beams\n * @author Juergen Hoeller\n * @author Brian Clozel\n * @since 3.2\n */",
            "/**\n * 定义作为任务执行的 {@code Runnable} 的持有者类，\n * 通常在计划时间或间隔执行。各种调度方式见子类层次结构。\n *\n * @author Chris Beams\n * @author Juergen Hoeller\n * @author Brian Clozel\n * @since 3.2\n */",
        ),
        (
            "\t/**\n\t * Create a new {@code Task}.\n\t * @param runnable the underlying task to execute\n\t */",
            "\t/**\n\t * 创建新的 {@code Task}。\n\t * @param runnable 要执行的底层任务\n\t */",
        ),
        (
            "\t/**\n\t * Return a {@link Runnable} that executes the underlying task.\n\t * <p>Note, this does not necessarily return the {@link Task#Task(Runnable) original runnable}\n\t * as it can be wrapped by the Framework for additional support.\n\t */",
            "\t/**\n\t * 返回执行底层任务的 {@link Runnable}。\n\t * <p>注意，未必返回 {@link Task#Task(Runnable) 原始 runnable}，\n\t * 框架可能为其包装以提供额外支持。\n\t */",
        ),
        (
            "\t/**\n\t * Return the outcome of the last task execution.\n\t * @since 6.2\n\t */",
            "\t/**\n\t * 返回上次任务执行的结果。\n\t * @since 6.2\n\t */",
        ),
    ],
    "TaskExecutionOutcome.java": [
        (
            "/**\n * Outcome of a {@link Task} execution.\n *\n * @author Brian Clozel\n * @since 6.2\n * @param executionTime the instant when the task execution started, or\n * {@code null} if the task has not started\n * @param status the {@link Status} of the execution outcome\n * @param throwable the exception thrown from the task execution, if any\n */",
            "/**\n * {@link Task} 执行的结果。\n *\n * @author Brian Clozel\n * @since 6.2\n * @param executionTime 任务开始执行的瞬间，未开始则为 {@code null}\n * @param status 执行结果的 {@link Status}\n * @param throwable 任务执行抛出的异常（若有）\n */",
        ),
        (
            "\t/**\n\t * Status of the task execution outcome.\n\t */",
            "\t/**\n\t * 任务执行结果的状态。\n\t */",
        ),
        (
            "\t\t/**\n\t\t * The task has not been executed so far.\n\t\t */",
            "\t\t/**\n\t\t * 任务尚未执行。\n\t\t */",
        ),
        (
            "\t\t/**\n\t\t * The task execution has been started and is ongoing.\n\t\t */",
            "\t\t/**\n\t\t * 任务执行已开始且进行中。\n\t\t */",
        ),
        (
            "\t\t/**\n\t\t * The task execution finished successfully.\n\t\t */",
            "\t\t/**\n\t\t * 任务执行已成功完成。\n\t\t */",
        ),
        (
            "\t\t/**\n\t\t * The task execution finished with an error.\n\t\t */",
            "\t\t/**\n\t\t * 任务执行以错误结束。\n\t\t */",
        ),
    ],
    "TaskExecutorFactoryBean.java": [
        (
            "/**\n * {@link FactoryBean} for creating {@link ThreadPoolTaskExecutor} instances,\n * primarily used behind the XML task namespace.\n *\n * @author Mark Fisher\n * @author Juergen Hoeller\n * @since 3.0\n */",
            "/**\n * 创建 {@link ThreadPoolTaskExecutor} 实例的 {@link FactoryBean}，\n * 主要用于 XML task 命名空间背后。\n *\n * @author Mark Fisher\n * @author Juergen Hoeller\n * @since 3.0\n */",
        ),
    ],
    "TaskManagementConfigUtils.java": [
        (
            "/**\n * Configuration constants for internal sharing across subpackages.\n *\n * @author Juergen Hoeller\n * @since 4.1\n */",
            "/**\n * 子包间内部共享的配置常量。\n *\n * @author Juergen Hoeller\n * @since 4.1\n */",
        ),
        (
            "\t/**\n\t * The bean name of the internally managed Scheduled annotation processor.\n\t */",
            "\t/**\n\t * 内部管理的 Scheduled 注解处理器的 Bean 名称。\n\t */",
        ),
        (
            "\t/**\n\t * The bean name of the internally managed Async annotation processor.\n\t */",
            "\t/**\n\t * 内部管理的 Async 注解处理器的 Bean 名称。\n\t */",
        ),
        (
            "\t/**\n\t * The bean name of the internally managed AspectJ async execution aspect.\n\t */",
            "\t/**\n\t * 内部管理的 AspectJ 异步执行切面的 Bean 名称。\n\t */",
        ),
    ],
    "TaskNamespaceHandler.java": [
        (
            "/**\n * {@code NamespaceHandler} for the 'task' namespace.\n *\n * @author Mark Fisher\n * @since 3.0\n */",
            "/**\n * 'task' 命名空间的 {@code NamespaceHandler}，\n * 注册 annotation-driven、executor、scheduled-tasks 和 scheduler 等 XML 元素解析器。\n *\n * @author Mark Fisher\n * @since 3.0\n */",
        ),
    ],
    "TaskSchedulerRouter.java": [
        (
            "/**\n * A routing implementation of the {@link TaskScheduler} interface,\n * delegating to a target scheduler based on an identified qualifier\n * or using a default scheduler otherwise.\n *\n * @author Juergen Hoeller\n * @since 6.1\n * @see SchedulingAwareRunnable#getQualifier()\n */",
            "/**\n * {@link TaskScheduler} 接口的路由实现，\n * 根据识别的限定符委托给目标调度器，否则使用默认调度器。\n *\n * @author Juergen Hoeller\n * @since 6.1\n * @see SchedulingAwareRunnable#getQualifier()\n */",
        ),
        (
            "\t/**\n\t * The default name of the {@link TaskScheduler} bean to pick up: {@value}.\n\t * <p>Note that the initial lookup happens by type; this is just the fallback\n\t * in case of multiple scheduler beans found in the context.\n\t */",
            "\t/**\n\t * 要选取的 {@link TaskScheduler} Bean 默认名称：{@value}。\n\t * <p>初始查找按类型进行；此名称仅作为上下文中存在多个调度器 Bean 时的回退。\n\t */",
        ),
        (
            "\t/**\n\t * The bean name for this router, or the bean name of the containing\n\t * bean if the router instance is internally held.\n\t */",
            "\t/**\n\t * 本路由器的 Bean 名称，或路由器实例内部持有时所属 Bean 的名称。\n\t */",
        ),
        (
            "\t/**\n\t * The bean factory for scheduler lookups.\n\t */",
            "\t/**\n\t * 用于调度器查找的 Bean 工厂。\n\t */",
        ),
        (
            "\t/**\n\t * Destroy the local default executor, if any.\n\t */",
            "\t/**\n\t * 销毁本地默认执行器（若有）。\n\t */",
        ),
    ],
    "TriggerTask.java": [
        (
            "/**\n * {@link Task} implementation defining a {@code Runnable} to be executed\n * according to a given {@link Trigger}.\n *\n * @author Chris Beams\n * @since 3.2\n * @see ScheduledTaskRegistrar#addTriggerTask(TriggerTask)\n * @see org.springframework.scheduling.TaskScheduler#schedule(Runnable, Trigger)\n */",
            "/**\n * 定义根据给定 {@link Trigger} 执行的 {@code Runnable} 的 {@link Task} 实现。\n *\n * @author Chris Beams\n * @since 3.2\n * @see ScheduledTaskRegistrar#addTriggerTask(TriggerTask)\n * @see org.springframework.scheduling.TaskScheduler#schedule(Runnable, Trigger)\n */",
        ),
        (
            "\t/**\n\t * Create a new {@link TriggerTask}.\n\t * @param runnable the underlying task to execute\n\t * @param trigger specifies when the task should be executed\n\t */",
            "\t/**\n\t * 创建新的 {@link TriggerTask}。\n\t * @param runnable 要执行的底层任务\n\t * @param trigger 指定任务执行时机\n\t */",
        ),
        (
            "\t/**\n\t * Return the associated trigger.\n\t */",
            "\t/**\n\t * 返回关联的触发器。\n\t */",
        ),
    ],
    "BitsCronField.java": [
        (
            "/**\n * Efficient bitwise-operator extension of {@link CronField}.\n * Created using the {@code parse*} methods.\n *\n * @author Arjen Poutsma\n * @author Juergen Hoeller\n * @since 5.3\n */",
            "/**\n * {@link CronField} 的高效位运算扩展。\n * 通过 {@code parse*} 方法创建。\n *\n * @author Arjen Poutsma\n * @author Juergen Hoeller\n * @since 5.3\n */",
        ),
        (
            "\t/**\n\t * Return a {@code BitsCronField} enabled for 0 nanoseconds.\n\t */",
            "\t/**\n\t * 返回启用 0 纳秒的 {@code BitsCronField}。\n\t */",
        ),
        (
            "\t/**\n\t * Parse the given value into a seconds {@code BitsCronField}, the first entry of a cron expression.\n\t */",
            "\t/**\n\t * 将给定值解析为秒 {@code BitsCronField}，即 cron 表达式的第一项。\n\t */",
        ),
        (
            "\t/**\n\t * Parse the given value into a minutes {@code BitsCronField}, the second entry of a cron expression.\n\t */",
            "\t/**\n\t * 将给定值解析为分 {@code BitsCronField}，即 cron 表达式的第二项。\n\t */",
        ),
        (
            "\t/**\n\t * Parse the given value into an hours {@code BitsCronField}, the third entry of a cron expression.\n\t */",
            "\t/**\n\t * 将给定值解析为时 {@code BitsCronField}，即 cron 表达式的第三项。\n\t */",
        ),
        (
            "\t/**\n\t * Parse the given value into a days of months {@code BitsCronField}, the fourth entry of a cron expression.\n\t */",
            "\t/**\n\t * 将给定值解析为日 {@code BitsCronField}，即 cron 表达式的第四项。\n\t */",
        ),
        (
            "\t/**\n\t * Parse the given value into a month {@code BitsCronField}, the fifth entry of a cron expression.\n\t */",
            "\t/**\n\t * 将给定值解析为月 {@code BitsCronField}，即 cron 表达式的第五项。\n\t */",
        ),
        (
            "\t/**\n\t * Parse the given value into a days of week {@code BitsCronField}, the sixth entry of a cron expression.\n\t */",
            "\t/**\n\t * 将给定值解析为星期几 {@code BitsCronField}，即 cron 表达式的第六项。\n\t */",
        ),
    ],
    "CompositeCronField.java": [
        (
            "/**\n * Extension of {@link CronField} that wraps an array of cron fields.\n *\n * @author Arjen Poutsma\n * @since 5.3.3\n */",
            "/**\n * 包装 cron 字段数组的 {@link CronField} 扩展。\n *\n * @author Arjen Poutsma\n * @since 5.3.3\n */",
        ),
        (
            "\t/**\n\t * Composes the given fields into a {@link CronField}.\n\t */",
            "\t/**\n\t * 将给定字段组合为 {@link CronField}。\n\t */",
        ),
    ],
    "CronTrigger.java": [
        (
            "/**\n * {@link Trigger} implementation for cron expressions. Wraps a\n * {@link CronExpression} which parses according to common crontab conventions.\n *\n * <p>Supports a Quartz day-of-month/week field with an L/# expression. Follows\n * common cron conventions in every other respect, including 0-6 for SUN-SAT\n * (plus 7 for SUN as well). Note that Quartz deviates from the day-of-week\n * convention in cron through 1-7 for SUN-SAT whereas Spring strictly follows\n * cron even in combination with the optional Quartz-specific L/# expressions.\n *\n * @author Juergen Hoeller\n * @author Arjen Poutsma\n * @since 3.0\n * @see CronExpression\n */",
            "/**\n * cron 表达式的 {@link Trigger} 实现，包装按常见 crontab 约定解析的\n * {@link CronExpression}。\n *\n * <p>支持带 L/# 表达式的 Quartz 日/周字段。其余方面遵循常见 cron 约定，\n * 包括 SUN-SAT 使用 0-6（SUN 也可用 7）。\n * 注意 Quartz 在 cron 中对星期几使用 1-7 表示 SUN-SAT，\n * 而 Spring 即使结合可选 Quartz 专有 L/# 表达式也严格遵循 cron 约定。\n *\n * @author Juergen Hoeller\n * @author Arjen Poutsma\n * @since 3.0\n * @see CronExpression\n */",
        ),
        (
            "\t/**\n\t * Build a {@code CronTrigger} from the pattern provided in the default time zone.\n\t * <p>This is equivalent to the {@link CronTrigger#forLenientExecution} factory\n\t * method. Original trigger firings may be skipped if the previous task is still\n\t * running; if this is not desirable, consider {@link CronTrigger#forFixedExecution}.\n\t * @param expression a space-separated list of time fields, following cron\n\t * expression conventions\n\t * @see CronTrigger#forLenientExecution\n\t * @see CronTrigger#forFixedExecution\n\t */",
            "\t/**\n\t * 使用默认时区中的模式构建 {@code CronTrigger}。\n\t * <p>等价于 {@link CronTrigger#forLenientExecution} 工厂方法。\n\t * 若前一任务仍在运行，可能跳过原定触发；若不希望如此，请考虑 {@link CronTrigger#forFixedExecution}。\n\t * @param expression 遵循 cron 表达式约定的空格分隔时间字段列表\n\t * @see CronTrigger#forLenientExecution\n\t * @see CronTrigger#forFixedExecution\n\t */",
        ),
        (
            "\t/**\n\t * Build a {@code CronTrigger} from the pattern provided in the given time zone,\n\t * with the same lenient execution as {@link CronTrigger#CronTrigger(String)}.\n\t * <p>Note that such explicit time zone customization is usually not necessary,\n\t * using {@link org.springframework.scheduling.TaskScheduler#getClock()} instead.\n\t * @param expression a space-separated list of time fields, following cron\n\t * expression conventions\n\t * @param timeZone a time zone in which the trigger times will be generated\n\t */",
            "\t/**\n\t * 使用给定 TimeZone 中的模式构建 {@code CronTrigger}，\n\t * 执行策略与 {@link CronTrigger#CronTrigger(String)} 相同（宽松模式）。\n\t * <p>通常无需显式自定义时区，应使用 {@link org.springframework.scheduling.TaskScheduler#getClock()}。\n\t * @param expression 遵循 cron 表达式约定的空格分隔时间字段列表\n\t * @param timeZone 生成触发时间的时区\n\t */",
        ),
        (
            "\t/**\n\t * Build a {@code CronTrigger} from the pattern provided in the given time zone,\n\t * with the same lenient execution as {@link CronTrigger#CronTrigger(String)}.\n\t * <p>Note that such explicit time zone customization is usually not necessary,\n\t * using {@link org.springframework.scheduling.TaskScheduler#getClock()} instead.\n\t * @param expression a space-separated list of time fields, following cron\n\t * expression conventions\n\t * @param zoneId a time zone in which the trigger times will be generated\n\t * @since 5.3\n\t * @see CronExpression#parse(String)\n\t */",
            "\t/**\n\t * 使用给定 ZoneId 中的模式构建 {@code CronTrigger}，\n\t * 执行策略与 {@link CronTrigger#CronTrigger(String)} 相同（宽松模式）。\n\t * <p>通常无需显式自定义时区，应使用 {@link org.springframework.scheduling.TaskScheduler#getClock()}。\n\t * @param expression 遵循 cron 表达式约定的空格分隔时间字段列表\n\t * @param zoneId 生成触发时间的时区\n\t * @since 5.3\n\t * @see CronExpression#parse(String)\n\t */",
        ),
        (
            "\t/**\n\t * Return the cron pattern that this trigger has been built with.\n\t */",
            "\t/**\n\t * 返回构建本触发器时使用的 cron 模式。\n\t */",
        ),
        (
            "\t/**\n\t * Determine the next execution time according to the given trigger context.\n\t * <p>Next execution times are calculated based on the\n\t * {@linkplain TriggerContext#lastCompletion completion time} of the\n\t * previous execution; therefore, overlapping executions won't occur.\n\t */",
            "\t/**\n\t * 根据给定触发器上下文确定下次执行时间。\n\t * <p>下次执行时间基于上次执行的\n\t * {@linkplain TriggerContext#lastCompletion 完成时间}计算，因此不会重叠执行。\n\t */",
        ),
        (
            "\t/**\n\t * Create a {@link CronTrigger} for lenient execution, to be rescheduled\n\t * after every task based on the completion time.\n\t * <p>This variant does not make up for missed trigger firings if the\n\t * associated task has taken too long. As a consequence, original trigger\n\t * firings may be skipped if the previous task is still running.\n\t * <p>This is equivalent to the regular {@link CronTrigger} constructor.\n\t * Note that lenient execution is scheduler-dependent: it may skip trigger\n\t * firings with long-running tasks on a thread pool while executing at\n\t * {@link #forFixedExecution}-like precision with new threads per task.\n\t * @param expression a space-separated list of time fields, following cron\n\t * expression conventions\n\t * @since 6.1.3\n\t * @see #resumeLenientExecution\n\t */",
            "\t/**\n\t * 创建宽松执行的 {@link CronTrigger}，每次任务完成后按完成时间重新调度。\n\t * <p>若关联任务耗时过长，本变体不会补发错过的触发。\n\t * 因此若前一任务仍在运行，可能跳过原定触发。\n\t * <p>等价于常规 {@link CronTrigger} 构造函数。\n\t * 注意宽松执行依赖调度器：线程池上长任务可能跳过触发，\n\t * 而每任务新线程时可能接近 {@link #forFixedExecution} 精度。\n\t * @param expression 遵循 cron 表达式约定的空格分隔时间字段列表\n\t * @since 6.1.3\n\t * @see #resumeLenientExecution\n\t */",
        ),
        (
            "\t/**\n\t * Create a {@link CronTrigger} for lenient execution, to be rescheduled\n\t * after every task based on the completion time.\n\t * <p>This variant does not make up for missed trigger firings if the\n\t * associated task has taken too long. As a consequence, original trigger\n\t * firings may be skipped if the previous task is still running.\n\t * @param expression a space-separated list of time fields, following cron\n\t * expression conventions\n\t * @param resumptionTimestamp the timestamp to resume from (the last-known\n\t * completion timestamp), with the new trigger calculated from there and\n\t * possibly immediately firing (but only once, every subsequent calculation\n\t * will start from the completion time of that first resumed trigger)\n\t * @since 6.1.3\n\t * @see #forLenientExecution\n\t */",
            "\t/**\n\t * 创建宽松执行的 {@link CronTrigger}，每次任务完成后按完成时间重新调度。\n\t * <p>若关联任务耗时过长，本变体不会补发错过的触发。\n\t * 因此若前一任务仍在运行，可能跳过原定触发。\n\t * @param expression 遵循 cron 表达式约定的空格分隔时间字段列表\n\t * @param resumptionTimestamp 恢复起点时间戳（上次已知完成时间），\n\t * 从此计算新触发并可能立即触发（仅一次，后续计算均从该首次恢复触发的完成时间开始）\n\t * @since 6.1.3\n\t * @see #forLenientExecution\n\t */",
        ),
        (
            "\t/**\n\t * Create a {@link CronTrigger} for fixed execution, to be rescheduled\n\t * after every task based on the last scheduled time.\n\t * <p>This variant makes up for missed trigger firings if the associated task\n\t * has taken too long, scheduling a task for every original trigger firing.\n\t * Such follow-up tasks may execute late but will never be skipped.\n\t * <p>Immediate versus late execution in case of long-running tasks may\n\t * be scheduler-dependent but the guarantee to never skip a task is portable.\n\t * @param expression a space-separated list of time fields, following cron\n\t * expression conventions\n\t * @since 6.1.3\n\t * @see #resumeFixedExecution\n\t */",
            "\t/**\n\t * 创建固定执行的 {@link CronTrigger}，每次任务完成后按上次计划时间重新调度。\n\t * <p>若关联任务耗时过长，本变体会补发错过的触发，为每个原定触发安排任务。\n\t * 后续任务可能延迟执行但绝不会被跳过。\n\t * <p>长任务情况下立即或延迟执行可能依赖调度器，但不跳过任务的保证可移植。\n\t * @param expression 遵循 cron 表达式约定的空格分隔时间字段列表\n\t * @since 6.1.3\n\t * @see #resumeFixedExecution\n\t */",
        ),
        (
            "\t/**\n\t * Create a {@link CronTrigger} for fixed execution, to be rescheduled\n\t * after every task based on the last scheduled time.\n\t * <p>This variant makes up for missed trigger firings if the associated task\n\t * has taken too long, scheduling a task for every original trigger firing.\n\t * Such follow-up tasks may execute late but will never be skipped.\n\t * @param expression a space-separated list of time fields, following cron\n\t * expression conventions\n\t * @param resumptionTimestamp the timestamp to resume from (the last-known\n\t * scheduled timestamp), with every trigger in-between immediately firing\n\t * to make up for every execution that would have happened in the meantime\n\t * @since 6.1.3\n\t * @see #forFixedExecution\n\t */",
            "\t/**\n\t * 创建固定执行的 {@link CronTrigger}，每次任务完成后按上次计划时间重新调度。\n\t * <p>若关联任务耗时过长，本变体会补发错过的触发，为每个原定触发安排任务。\n\t * 后续任务可能延迟执行但绝不会被跳过。\n\t * @param expression 遵循 cron 表达式约定的空格分隔时间字段列表\n\t * @param resumptionTimestamp 恢复起点时间戳（上次已知计划时间），\n\t * 其间每个触发立即补发，以弥补期间本应发生的每次执行\n\t * @since 6.1.3\n\t * @see #forFixedExecution\n\t */",
        ),
    ],
    "DefaultScheduledTaskObservationConvention.java": [
        (
            "/**\n * Default implementation for {@link ScheduledTaskObservationConvention}.\n * @author Brian Clozel\n * @since 6.1\n */",
            "/**\n * {@link ScheduledTaskObservationConvention} 的默认实现，\n * 为定时任务执行提供 Micrometer 观测约定。\n * @author Brian Clozel\n * @since 6.1\n */",
        ),
    ],
    "DelegatingErrorHandlingRunnable.java": [
        (
            "/**\n * Runnable wrapper that catches any exception or error thrown from its\n * delegate Runnable and allows an {@link ErrorHandler} to handle it.\n *\n * @author Juergen Hoeller\n * @author Mark Fisher\n * @since 3.0\n */",
            "/**\n * 捕获委托 {@code Runnable} 抛出的任何异常或错误，\n * 并允许 {@link ErrorHandler} 处理的 Runnable 包装器。\n *\n * @author Juergen Hoeller\n * @author Mark Fisher\n * @since 3.0\n */",
        ),
        (
            "\t/**\n\t * Create a new DelegatingErrorHandlingRunnable.\n\t * @param delegate the Runnable implementation to delegate to\n\t * @param errorHandler the ErrorHandler for handling any exceptions\n\t */",
            "\t/**\n\t * 创建新的 DelegatingErrorHandlingRunnable。\n\t * @param delegate 要委托的 Runnable 实现\n\t * @param errorHandler 处理异常的 ErrorHandler\n\t */",
        ),
    ],
    "MethodInvokingRunnable.java": [
        (
            "/**\n * Adapter that implements the {@link Runnable} interface as a configurable\n * method invocation based on Spring's MethodInvoker.\n *\n * <p>Inherits common configuration properties from\n * {@link org.springframework.util.MethodInvoker}.\n *\n * @author Juergen Hoeller\n * @since 1.2.4\n * @see java.util.concurrent.Executor#execute(Runnable)\n */",
            "/**\n * 基于 Spring MethodInvoker 的可配置方法调用、实现 {@link Runnable} 接口的适配器。\n *\n * <p>继承 {@link org.springframework.util.MethodInvoker} 的通用配置属性。\n *\n * @author Juergen Hoeller\n * @since 1.2.4\n * @see java.util.concurrent.Executor#execute(Runnable)\n */",
        ),
        (
            "\t/**\n\t * Build a message for an invocation failure exception.\n\t * @return the error message, including the target method name etc\n\t */",
            "\t/**\n\t * 构建方法调用失败异常的消息。\n\t * @return 错误消息，包含目标方法名等信息\n\t */",
        ),
    ],
}

_large_spec = importlib.util.spec_from_file_location(
    "wave12_large",
    ROOT / "scripts/annotate_springframework_wave12_large.py",
)
_large_mod = importlib.util.module_from_spec(_large_spec)
assert _large_spec.loader is not None
_large_spec.loader.exec_module(_large_mod)
FILE_REPLACEMENTS.update(_large_mod.LARGE_FILE_REPLACEMENTS)


def apply_replacements(text: str, replacements: list[tuple[str, str]]) -> str:
    for old, new in replacements:
        if old not in text:
            raise ValueError(f"Pattern not found:\n{old[:120]}...")
        text = text.replace(old, new, 1)
    return text


def mark_batch_done(batch: list[str]) -> None:
    done_path = QUEUE / "done.txt"
    done = [ln.strip() for ln in done_path.read_text(encoding="utf-8").splitlines() if ln.strip()]
    done_set = set(done)
    for rel in batch:
        if rel not in done_set:
            done.append(rel)
            done_set.add(rel)
    done_path.write_text("\n".join(done) + "\n", encoding="utf-8")


def main() -> int:
    failures: list[str] = []
    ok = 0
    for rel in BATCH_FILES:
        name = Path(rel).name
        src = ORIGINAL / rel
        dst = ANALYZED / rel
        if not src.exists():
            failures.append(f"MISSING original: {rel}")
            continue
        dst.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(src, dst)
        reps = FILE_REPLACEMENTS.get(name, [])
        if not reps:
            failures.append(f"NO_REPLACEMENTS: {rel}")
            continue
        try:
            text = dst.read_text(encoding="utf-8")
            text = apply_replacements(text, reps)
            cn = len(re.findall(r"[\u4e00-\u9fff]", text))
            lic = "Licensed under the Apache License" in text
            if cn < 10 or not lic:
                failures.append(f"VALIDATION cn={cn} lic={lic}: {rel}")
                continue
            dst.write_text(text, encoding="utf-8")
            ok += 1
            print(f"OK cn={cn} {rel}")
        except Exception as e:
            failures.append(f"{rel}: {e}")
            print(f"FAIL {rel}: {e}")
    if ok == len(BATCH_FILES) and not failures:
        mark_batch_done(BATCH_FILES)
        print(f"Marked {ok} files done")
    print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False, indent=2))
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
