SMALL_CONCURRENT_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "DefaultManagedTaskScheduler.java": [
        (
            "/**\n * JNDI-based variant of {@link ConcurrentTaskScheduler}, performing a default lookup for\n * JSR-236's \"java:comp/DefaultManagedScheduledExecutorService\" in a Jakarta EE environment.\n * Expected to be exposed as a bean, in particular as the default lookup happens in the\n * standard {@link InitializingBean#afterPropertiesSet()} callback.\n *\n * <p>Note: This class is not strictly JSR-236 based; it can work with any regular\n * {@link java.util.concurrent.ScheduledExecutorService} that can be found in JNDI.\n * The actual adapting to {@link jakarta.enterprise.concurrent.ManagedScheduledExecutorService}\n * happens in the base class {@link ConcurrentTaskScheduler} itself.\n *\n * @author Juergen Hoeller\n * @since 4.0\n * @see jakarta.enterprise.concurrent.ManagedScheduledExecutorService\n */",
            "/**\n * 基于 JNDI 的 {@link ConcurrentTaskScheduler} 变体，\n * 在 Jakarta EE 环境中默认查找 JSR-236 的 \"java:comp/DefaultManagedScheduledExecutorService\"。\n * 预期作为 Bean 暴露，尤其默认查找发生在标准\n * {@link InitializingBean#afterPropertiesSet()} 回调中。\n *\n * <p>注意：本类并非严格基于 JSR-236；可与 JNDI 中找到的任意常规\n * {@link java.util.concurrent.ScheduledExecutorService} 配合工作。\n * 实际适配 {@link jakarta.enterprise.concurrent.ManagedScheduledExecutorService}\n * 在基类 {@link ConcurrentTaskScheduler} 自身中完成。\n *\n * @author Juergen Hoeller\n * @since 4.0\n * @see jakarta.enterprise.concurrent.ManagedScheduledExecutorService\n */",
        ),
        (
            "\t/**\n\t * Set the JNDI template to use for JNDI lookups.\n\t * @see org.springframework.jndi.JndiAccessor#setJndiTemplate\n\t */",
            "\t/**\n\t * 设置用于 JNDI 查找的 JNDI 模板。\n\t * @see org.springframework.jndi.JndiAccessor#setJndiTemplate\n\t */",
        ),
        (
            "\t/**\n\t * Set the JNDI environment to use for JNDI lookups.\n\t * @see org.springframework.jndi.JndiAccessor#setJndiEnvironment\n\t */",
            "\t/**\n\t * 设置用于 JNDI 查找的 JNDI 环境。\n\t * @see org.springframework.jndi.JndiAccessor#setJndiEnvironment\n\t */",
        ),
        (
            "\t/**\n\t * Set whether the lookup occurs in a Jakarta EE container, i.e. if the prefix\n\t * \"java:comp/env/\" needs to be added if the JNDI name doesn't already\n\t * contain it. PersistenceAnnotationBeanPostProcessor's default is \"true\".\n\t * @see org.springframework.jndi.JndiLocatorSupport#setResourceRef\n\t */",
            "\t/**\n\t * 设置查找是否发生在 Jakarta EE 容器中，即若 JNDI 名称尚未包含前缀\n\t * \"java:comp/env/\" 是否需要添加。PersistenceAnnotationBeanPostProcessor 默认为 \"true\"。\n\t * @see org.springframework.jndi.JndiLocatorSupport#setResourceRef\n\t */",
        ),
        (
            "\t/**\n\t * Specify a JNDI name of the {@link java.util.concurrent.Executor} to delegate to,\n\t * replacing the default JNDI name \"java:comp/DefaultManagedScheduledExecutorService\".\n\t * <p>This can either be a fully qualified JNDI name, or the JNDI name relative\n\t * to the current environment naming context if \"resourceRef\" is set to \"true\".\n\t * @see #setConcurrentExecutor\n\t * @see #setResourceRef\n\t */",
            "\t/**\n\t * 指定要委托的 {@link java.util.concurrent.Executor} 的 JNDI 名称，\n\t * 替换默认 JNDI 名称 \"java:comp/DefaultManagedScheduledExecutorService\"。\n\t * <p>可以是完全限定 JNDI 名称，或在 \"resourceRef\" 为 \"true\" 时\n\t * 相对于当前环境命名上下文的 JNDI 名称。\n\t * @see #setConcurrentExecutor\n\t * @see #setResourceRef\n\t */",
        ),
    ],
    "DelegatingErrorHandlingCallable.java": [
        (
            "/**\n * {@link Callable} adapter for an {@link ErrorHandler}.\n *\n * @author Juergen Hoeller\n * @since 6.2\n * @param <V> the value type\n */",
            "/**\n * 面向 {@link ErrorHandler} 的 {@link Callable} 适配器，\n * 在调用失败时将异常委托给错误处理器。\n *\n * @author Juergen Hoeller\n * @since 6.2\n * @param <V> 值类型\n */",
        ),
    ],
    "ExecutorLifecycleDelegate.java": [
        (
            "/**\n * An internal delegate for common {@link ExecutorService} lifecycle management\n * with pause/resume support.\n *\n * @author Juergen Hoeller\n * @since 6.1\n * @see ExecutorConfigurationSupport\n * @see SimpleAsyncTaskScheduler\n */",
            "/**\n * 带暂停/恢复支持的通用 {@link ExecutorService} 生命周期管理内部委托。\n *\n * @author Juergen Hoeller\n * @since 6.1\n * @see ExecutorConfigurationSupport\n * @see SimpleAsyncTaskScheduler\n */",
        ),
    ],
    "ForkJoinPoolFactoryBean.java": [
        (
            "/**\n * A Spring {@link FactoryBean} that builds and exposes a preconfigured {@link ForkJoinPool}.\n *\n * @author Juergen Hoeller\n * @since 3.1\n */",
            "/**\n * 构建并暴露预配置 {@link ForkJoinPool} 的 Spring {@link FactoryBean}。\n *\n * @author Juergen Hoeller\n * @since 3.1\n */",
        ),
        (
            "\t/**\n\t * Set whether to expose Java's 'common' {@link ForkJoinPool}.\n\t * <p>Default is {@code false} , creating a local {@link ForkJoinPool} instance\n\t * based on the {@link #setParallelism parallelism},\n\t * {@link #setThreadFactory threadFactory},\n\t * {@link #setUncaughtExceptionHandler uncaughtExceptionHandler}, and\n\t * {@link #setAsyncMode asyncMode} properties on this FactoryBean.\n\t * <p><b>NOTE:</b> Setting this flag to {@code true} effectively ignores all other\n\t * properties on this FactoryBean, reusing the shared common JDK {@link ForkJoinPool}\n\t * instead. This is a fine choice but does remove the application's ability\n\t * to customize ForkJoinPool behavior, in particular the use of custom threads.\n\t * @since 3.2\n\t * @see java.util.concurrent.ForkJoinPool#commonPool()\n\t */",
            "\t/**\n\t * 设置是否暴露 Java 的 'common' {@link ForkJoinPool}。\n\t * <p>默认为 {@code false}，基于本 FactoryBean 的\n\t * {@link #setParallelism parallelism}、\n\t * {@link #setThreadFactory threadFactory}、\n\t * {@link #setUncaughtExceptionHandler uncaughtExceptionHandler} 与\n\t * {@link #setAsyncMode asyncMode} 属性创建本地 {@link ForkJoinPool} 实例。\n\t * <p><b>注意：</b>将此标志设为 {@code true} 将有效忽略本 FactoryBean 的所有其他属性，\n\t * 改为复用共享的 JDK common {@link ForkJoinPool}。\n\t * 这是合理选择，但会移除应用自定义 ForkJoinPool 行为的能力，\n\t * 尤其无法使用自定义线程。\n\t * @since 3.2\n\t * @see java.util.concurrent.ForkJoinPool#commonPool()\n\t */",
        ),
        (
            "\t/**\n\t * Specify the parallelism level. Default is {@link Runtime#availableProcessors()}.\n\t */",
            "\t/**\n\t * 指定并行级别。默认为 {@link Runtime#availableProcessors()}。\n\t */",
        ),
        (
            "\t/**\n\t * Set the factory for creating new ForkJoinWorkerThreads.\n\t * Default is {@link ForkJoinPool#defaultForkJoinWorkerThreadFactory}.\n\t */",
            "\t/**\n\t * 设置创建新 ForkJoinWorkerThread 的工厂。\n\t * 默认为 {@link ForkJoinPool#defaultForkJoinWorkerThreadFactory}。\n\t */",
        ),
        (
            "\t/**\n\t * Set the handler for internal worker threads that terminate due to unrecoverable errors\n\t * encountered while executing tasks. Default is none.\n\t */",
            "\t/**\n\t * 设置因执行任务时遇到不可恢复错误而终止的内部工作线程的处理程序。默认为无。\n\t */",
        ),
        (
            "\t/**\n\t * Specify whether to establish a local first-in-first-out scheduling mode for forked tasks\n\t * that are never joined. This mode (asyncMode = {@code true}) may be more appropriate\n\t * than the default locally stack-based mode in applications in which worker threads only\n\t * process event-style asynchronous tasks. Default is {@code false}.\n\t */",
            "\t/**\n\t * 指定是否为永不 join 的分叉任务建立本地先进先出调度模式。\n\t * 在工作线程仅处理事件式异步任务的应用中，\n\t * 此模式 (asyncMode = {@code true}) 可能比默认本地栈模式更合适。默认为 {@code false}。\n\t */",
        ),
        (
            "\t/**\n\t * Set the maximum number of seconds that this ForkJoinPool is supposed to block\n\t * on shutdown in order to wait for remaining tasks to complete their execution\n\t * before the rest of the container continues to shut down. This is particularly\n\t * useful if your remaining tasks are likely to need access to other resources\n\t * that are also managed by the container.\n\t * <p>By default, this ForkJoinPool won't wait for the termination of tasks at all.\n\t * It will continue to fully execute all ongoing tasks as well as all remaining\n\t * tasks in the queue, in parallel to the rest of the container shutting down.\n\t * In contrast, if you specify an await-termination period using this property,\n\t * this executor will wait for the given time (max) for the termination of tasks.\n\t * <p>Note that this feature works for the {@link #setCommonPool \"commonPool\"}\n\t * mode as well. The underlying ForkJoinPool won't actually terminate in that\n\t * case but will wait for all tasks to terminate.\n\t * @see java.util.concurrent.ForkJoinPool#shutdown()\n\t * @see java.util.concurrent.ForkJoinPool#awaitTermination\n\t */",
            "\t/**\n\t * 设置本 ForkJoinPool 在 shutdown 时最多阻塞的秒数，\n\t * 以等待剩余任务完成执行，然后容器其余部分继续关闭。\n\t * 若剩余任务可能需要访问容器管理的其他资源，这尤其有用。\n\t * <p>默认情况下，本 ForkJoinPool 完全不等待任务终止。\n\t * 它将与容器其余部分并行关闭，\n\t * 继续完全执行所有进行中任务及队列中剩余任务。\n\t * 相反，若通过本属性指定 await-termination 周期，\n\t * 本执行器将最多等待给定时间以待任务终止。\n\t * <p>注意此特性对 {@link #setCommonPool \"commonPool\"} 模式同样有效。\n\t * 此时底层 ForkJoinPool 不会真正终止，但会等待所有任务终止。\n\t * @see java.util.concurrent.ForkJoinPool#shutdown()\n\t * @see java.util.concurrent.ForkJoinPool#awaitTermination\n\t */",
        ),
    ],
    "ReschedulingRunnable.java": [
        (
            "/**\n * Internal adapter that reschedules an underlying {@link Runnable} according\n * to the next execution time suggested by a given {@link Trigger}.\n *\n * <p>Necessary because a native {@link ScheduledExecutorService} supports\n * delay-driven execution only. The flexibility of the {@link Trigger} interface\n * will be translated onto a delay for the next execution time (repeatedly).\n *\n * @author Juergen Hoeller\n * @author Mark Fisher\n * @since 3.0\n */",
            "/**\n * 根据给定 {@link Trigger} 建议的下次执行时间\n * 重新调度底层 {@link Runnable} 的内部适配器。\n *\n * <p>原生 {@link ScheduledExecutorService} 仅支持延迟驱动执行，因此需要本类。\n * {@link Trigger} 接口的灵活性将（反复）转换为下次执行时间的延迟。\n *\n * @author Juergen Hoeller\n * @author Mark Fisher\n * @since 3.0\n */",
        ),
    ],
}
