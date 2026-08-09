"""Chinese JavaDoc replacements for springframework wave17a tx event [8:15]."""

TX_EVENT_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "TransactionPhase.java": [
        (
            "/**\n * The phase in which a transactional event listener applies.\n *\n * @author Stephane Nicoll\n * @author Juergen Hoeller\n * @author Sam Brannen\n * @since 4.2\n * @see TransactionalEventListener#phase()\n * @see TransactionalApplicationListener#getTransactionPhase()\n * @see TransactionalApplicationListener#forPayload(TransactionPhase, Consumer)\n */",
            "/**\n * 事务事件监听器适用的事务阶段。\n *\n * @author Stephane Nicoll\n * @author Juergen Hoeller\n * @author Sam Brannen\n * @since 4.2\n * @see TransactionalEventListener#phase()\n * @see TransactionalApplicationListener#getTransactionPhase()\n * @see TransactionalApplicationListener#forPayload(TransactionPhase, Consumer)\n */",
        ),
        (
            "\t/**\n\t * Handle the event before transaction commit.\n\t * @see TransactionSynchronization#beforeCommit(boolean)\n\t */",
            "\t/**\n\t * 在事务提交前处理事件。\n\t * @see TransactionSynchronization#beforeCommit(boolean)\n\t */",
        ),
        (
            "\t/**\n\t * Handle the event after the commit has completed successfully.\n\t * <p>Note: This is a specialization of {@link #AFTER_COMPLETION} and therefore\n\t * executes in the same sequence of events as {@code AFTER_COMPLETION}\n\t * (and not in {@link TransactionSynchronization#afterCommit()}).\n\t * <p>Interactions with the underlying transactional resource will not be\n\t * committed in this phase. See\n\t * {@link TransactionSynchronization#afterCompletion(int)} for details.\n\t * @see TransactionSynchronization#afterCompletion(int)\n\t * @see TransactionSynchronization#STATUS_COMMITTED\n\t */",
            "\t/**\n\t * 在提交成功完成后处理事件。\n\t * <p>注意：这是 {@link #AFTER_COMPLETION} 的特化，因此\n\t * 与 {@code AFTER_COMPLETION} 在同一事件序列中执行\n\t * （而非在 {@link TransactionSynchronization#afterCommit()} 中）。\n\t * <p>此阶段与底层事务资源的交互不会被提交。详见\n\t * {@link TransactionSynchronization#afterCompletion(int)}。\n\t * @see TransactionSynchronization#afterCompletion(int)\n\t * @see TransactionSynchronization#STATUS_COMMITTED\n\t */",
        ),
        (
            "\t/**\n\t * Handle the event if the transaction has rolled back.\n\t * <p>Note: This is a specialization of {@link #AFTER_COMPLETION} and therefore\n\t * executes in the same sequence of events as {@code AFTER_COMPLETION}.\n\t * <p>Interactions with the underlying transactional resource will not be\n\t * committed in this phase. See\n\t * {@link TransactionSynchronization#afterCompletion(int)} for details.\n\t * @see TransactionSynchronization#afterCompletion(int)\n\t * @see TransactionSynchronization#STATUS_ROLLED_BACK\n\t */",
            "\t/**\n\t * 若事务已回滚则处理事件。\n\t * <p>注意：这是 {@link #AFTER_COMPLETION} 的特化，因此\n\t * 与 {@code AFTER_COMPLETION} 在同一事件序列中执行。\n\t * <p>此阶段与底层事务资源的交互不会被提交。详见\n\t * {@link TransactionSynchronization#afterCompletion(int)}。\n\t * @see TransactionSynchronization#afterCompletion(int)\n\t * @see TransactionSynchronization#STATUS_ROLLED_BACK\n\t */",
        ),
        (
            "\t/**\n\t * Handle the event after the transaction has completed.\n\t * <p>For more fine-grained events, use {@link #AFTER_COMMIT} or\n\t * {@link #AFTER_ROLLBACK} to intercept transaction commit\n\t * or rollback, respectively.\n\t * <p>Interactions with the underlying transactional resource will not be\n\t * committed in this phase. See\n\t * {@link TransactionSynchronization#afterCompletion(int)} for details.\n\t * @see TransactionSynchronization#afterCompletion(int)\n\t */",
            "\t/**\n\t * 在事务完成后处理事件。\n\t * <p>若需更细粒度的事件，分别使用 {@link #AFTER_COMMIT} 或\n\t * {@link #AFTER_ROLLBACK} 拦截事务提交或回滚。\n\t * <p>此阶段与底层事务资源的交互不会被提交。详见\n\t * {@link TransactionSynchronization#afterCompletion(int)}。\n\t * @see TransactionSynchronization#afterCompletion(int)\n\t */",
        ),
    ],
    "TransactionalApplicationListener.java": [
        (
            "/**\n * An {@link ApplicationListener} that is invoked according to a {@link TransactionPhase}.\n * This is a programmatic equivalent of the {@link TransactionalEventListener} annotation.\n *\n * <p>Adding {@link org.springframework.core.Ordered} to your listener implementation\n * allows you to prioritize that listener amongst other listeners running before or after\n * transaction completion.\n *\n * <p>As of 6.1, transactional event listeners can work with thread-bound transactions managed\n * by a {@link org.springframework.transaction.PlatformTransactionManager} as well as reactive\n * transactions managed by a {@link org.springframework.transaction.ReactiveTransactionManager}.\n * For the former, listeners are guaranteed to see the current thread-bound transaction.\n * Since the latter uses the Reactor context instead of thread-local variables, the transaction\n * context needs to be included in the published event instance as the event source:\n * see {@link org.springframework.transaction.reactive.TransactionalEventPublisher}.\n *\n * @author Juergen Hoeller\n * @author Oliver Drotbohm\n * @since 5.3\n * @param <E> the specific {@code ApplicationEvent} subclass to listen to\n * @see TransactionalEventListener\n * @see TransactionalApplicationListenerAdapter\n * @see #forPayload\n */",
            "/**\n * 根据 {@link TransactionPhase} 调用的 {@link ApplicationListener}。\n * 这是 {@link TransactionalEventListener} 注解的编程式等价物。\n *\n * <p>在监听器实现上添加 {@link org.springframework.core.Ordered}\n * 可让该监听器在事务完成前后运行的其他监听器中优先执行。\n *\n * <p>自 6.1 起，事务事件监听器可与由\n * {@link org.springframework.transaction.PlatformTransactionManager} 管理的线程绑定事务\n * 以及由 {@link org.springframework.transaction.ReactiveTransactionManager} 管理的响应式事务配合工作。\n * 对于前者，监听器保证能看到当前线程绑定的事务。\n * 由于后者使用 Reactor 上下文而非线程局部变量，\n * 事务上下文需作为事件源包含在发布的事件实例中：\n * 参见 {@link org.springframework.transaction.reactive.TransactionalEventPublisher}。\n *\n * @author Juergen Hoeller\n * @author Oliver Drotbohm\n * @since 5.3\n * @param <E> 要监听的特定 {@code ApplicationEvent} 子类\n * @see TransactionalEventListener\n * @see TransactionalApplicationListenerAdapter\n * @see #forPayload\n */",
        ),
        (
            "\t/**\n\t * Return the execution order within transaction synchronizations.\n\t * <p>Default is {@link Ordered#LOWEST_PRECEDENCE}.\n\t * @see org.springframework.transaction.support.TransactionSynchronization#getOrder()\n\t */",
            "\t/**\n\t * 返回事务同步内的执行顺序。\n\t * <p>默认为 {@link Ordered#LOWEST_PRECEDENCE}。\n\t * @see org.springframework.transaction.support.TransactionSynchronization#getOrder()\n\t */",
        ),
        (
            "\t/**\n\t * Transaction-synchronized listeners do not support asynchronous execution,\n\t * only their target listener ({@link #processEvent}) potentially does.\n\t * @since 6.1\n\t */",
            "\t/**\n\t * 事务同步监听器不支持异步执行，\n\t * 仅其目标监听器（{@link #processEvent}）可能支持。\n\t * @since 6.1\n\t */",
        ),
        (
            "\t/**\n\t * Return an identifier for the listener to be able to refer to it individually.\n\t * <p>It might be necessary for specific completion callback implementations\n\t * to provide a specific id, whereas for other scenarios an empty String\n\t * (as the common default value) is acceptable as well.\n\t * @see org.springframework.context.event.SmartApplicationListener#getListenerId()\n\t * @see TransactionalEventListener#id\n\t * @see #addCallback\n\t */",
            "\t/**\n\t * 返回监听器标识符以便单独引用。\n\t * <p>特定完成回调实现可能需要提供特定 id，\n\t * 其他场景下空字符串（常见默认值）也可接受。\n\t * @see org.springframework.context.event.SmartApplicationListener#getListenerId()\n\t * @see TransactionalEventListener#id\n\t * @see #addCallback\n\t */",
        ),
        (
            "\t/**\n\t * Return the {@link TransactionPhase} in which the listener will be invoked.\n\t * <p>The default phase is {@link TransactionPhase#AFTER_COMMIT}.\n\t */",
            "\t/**\n\t * 返回监听器将被调用的事务 {@link TransactionPhase}。\n\t * <p>默认阶段为 {@link TransactionPhase#AFTER_COMMIT}。\n\t */",
        ),
        (
            "\t/**\n\t * Add a callback to be invoked on processing within transaction synchronization,\n\t * i.e. when {@link #processEvent} is being triggered during actual transactions.\n\t * @param callback the synchronization callback to apply\n\t */",
            "\t/**\n\t * 添加在事务同步内处理时调用的回调，\n\t * 即在真实事务期间触发 {@link #processEvent} 时。\n\t * @param callback 要应用的同步回调\n\t */",
        ),
        (
            "\t/**\n\t * Immediately process the given {@link ApplicationEvent}. In contrast to\n\t * {@link #onApplicationEvent(ApplicationEvent)}, a call to this method will\n\t * directly process the given event without deferring it to the associated\n\t * {@link #getTransactionPhase() transaction phase}.\n\t * @param event the event to process through the target listener implementation\n\t */",
            "\t/**\n\t * 立即处理给定的 {@link ApplicationEvent}。与\n\t * {@link #onApplicationEvent(ApplicationEvent)} 不同，调用此方法将\n\t * 直接处理给定事件，而非推迟到关联的\n\t * {@link #getTransactionPhase() 事务阶段}。\n\t * @param event 通过目标监听器实现处理的事件\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@code TransactionalApplicationListener} for the given payload consumer,\n\t * to be applied in the default phase {@link TransactionPhase#AFTER_COMMIT}.\n\t * @param consumer the event payload consumer\n\t * @param <T> the type of the event payload\n\t * @return a corresponding {@code TransactionalApplicationListener} instance\n\t * @see PayloadApplicationEvent#getPayload()\n\t * @see TransactionalApplicationListenerAdapter\n\t */",
            "\t/**\n\t * 为给定 payload 消费者创建新的 {@code TransactionalApplicationListener}，\n\t * 在默认阶段 {@link TransactionPhase#AFTER_COMMIT} 中应用。\n\t * @param consumer 事件 payload 消费者\n\t * @param <T> 事件 payload 的类型\n\t * @return 对应的 {@code TransactionalApplicationListener} 实例\n\t * @see PayloadApplicationEvent#getPayload()\n\t * @see TransactionalApplicationListenerAdapter\n\t */",
        ),
        (
            "\t/**\n\t * Create a new {@code TransactionalApplicationListener} for the given payload consumer.\n\t * @param phase the transaction phase in which to invoke the listener\n\t * @param consumer the event payload consumer\n\t * @param <T> the type of the event payload\n\t * @return a corresponding {@code TransactionalApplicationListener} instance\n\t * @see PayloadApplicationEvent#getPayload()\n\t * @see TransactionalApplicationListenerAdapter\n\t */",
            "\t/**\n\t * 为给定 payload 消费者创建新的 {@code TransactionalApplicationListener}。\n\t * @param phase 调用监听器的事务阶段\n\t * @param consumer 事件 payload 消费者\n\t * @param <T> 事件 payload 的类型\n\t * @return 对应的 {@code TransactionalApplicationListener} 实例\n\t * @see PayloadApplicationEvent#getPayload()\n\t * @see TransactionalApplicationListenerAdapter\n\t */",
        ),
        (
            "\t/**\n\t * Callback to be invoked on synchronization-driven event processing,\n\t * wrapping the target listener invocation ({@link #processEvent}).\n\t *\n\t * @see #addCallback\n\t * @see #processEvent\n\t */",
            "\t/**\n\t * 在同步驱动的事件处理时调用的回调，\n\t * 包装目标监听器调用（{@link #processEvent}）。\n\t *\n\t * @see #addCallback\n\t * @see #processEvent\n\t */",
        ),
        (
            "\t\t/**\n\t\t * Called before transactional event listener invocation.\n\t\t * @param event the event that transaction synchronization is about to process\n\t\t */",
            "\t\t/**\n\t\t * 在事务事件监听器调用前调用。\n\t\t * @param event 事务同步即将处理的事件\n\t\t */",
        ),
        (
            "\t\t/**\n\t\t * Called after a transactional event listener invocation.\n\t\t * @param event the event that transaction synchronization finished processing\n\t\t * @param ex an exception that occurred during listener invocation, if any\n\t\t */",
            "\t\t/**\n\t\t * 在事务事件监听器调用后调用。\n\t\t * @param event 事务同步已完成处理的事件\n\t\t * @param ex 监听器调用期间发生的异常（若有）\n\t\t */",
        ),
    ],
    "TransactionalApplicationListenerAdapter.java": [
        (
            "/**\n * {@link TransactionalApplicationListener} adapter that delegates the processing of\n * an event to a target {@link ApplicationListener} instance. Supports the exact\n * same features as any regular {@link ApplicationListener} but is aware of the\n * transactional context of the event publisher.\n *\n * <p>For simple {@link org.springframework.context.PayloadApplicationEvent} handling,\n * consider the {@link TransactionalApplicationListener#forPayload} factory methods\n * as a convenient alternative to custom usage of this adapter class.\n *\n * @author Juergen Hoeller\n * @since 5.3\n * @param <E> the specific {@code ApplicationEvent} subclass to listen to\n * @see TransactionalApplicationListener\n * @see TransactionalEventListener\n * @see TransactionalApplicationListenerMethodAdapter\n */",
            "/**\n * 将事件处理委托给目标 {@link ApplicationListener} 实例的\n * {@link TransactionalApplicationListener} 适配器。支持与任何常规\n * {@link ApplicationListener} 完全相同的功能，但感知事件发布者的事务上下文。\n *\n * <p>对于简单的 {@link org.springframework.context.PayloadApplicationEvent} 处理，\n * 可考虑 {@link TransactionalApplicationListener#forPayload} 工厂方法\n * 作为自定义使用此适配器类的便捷替代方案。\n *\n * @author Juergen Hoeller\n * @since 5.3\n * @param <E> 要监听的特定 {@code ApplicationEvent} 子类\n * @see TransactionalApplicationListener\n * @see TransactionalEventListener\n * @see TransactionalApplicationListenerMethodAdapter\n */",
        ),
        (
            "\t/**\n\t * Construct a new TransactionalApplicationListenerAdapter.\n\t * @param targetListener the actual listener to invoke in the specified transaction phase\n\t * @see #setTransactionPhase\n\t * @see TransactionalApplicationListener#forPayload\n\t */",
            "\t/**\n\t * 构造新的 TransactionalApplicationListenerAdapter。\n\t * @param targetListener 在指定事务阶段调用的实际监听器\n\t * @see #setTransactionPhase\n\t * @see TransactionalApplicationListener#forPayload\n\t */",
        ),
        (
            "\t/**\n\t * Specify the synchronization order for the listener.\n\t */",
            "\t/**\n\t * 指定监听器的同步顺序。\n\t */",
        ),
        (
            "\t/**\n\t * Return the synchronization order for the listener.\n\t */",
            "\t/**\n\t * 返回监听器的同步顺序。\n\t */",
        ),
        (
            "\t/**\n\t * Specify the transaction phase to invoke the listener in.\n\t * <p>The default is {@link TransactionPhase#AFTER_COMMIT}.\n\t */",
            "\t/**\n\t * 指定调用监听器的事务阶段。\n\t * <p>默认为 {@link TransactionPhase#AFTER_COMMIT}。\n\t */",
        ),
        (
            "\t/**\n\t * Return the transaction phase to invoke the listener in.\n\t */",
            "\t/**\n\t * 返回调用监听器的事务阶段。\n\t */",
        ),
        (
            "\t/**\n\t * Specify an id to identify the listener with.\n\t * <p>The default is an empty String.\n\t */",
            "\t/**\n\t * 指定用于标识监听器的 id。\n\t * <p>默认为空字符串。\n\t */",
        ),
        (
            "\t/**\n\t * Return an id to identify the listener with.\n\t */",
            "\t/**\n\t * 返回用于标识监听器的 id。\n\t */",
        ),
    ],
    "TransactionalApplicationListenerMethodAdapter.java": [
        (
            "/**\n * {@link GenericApplicationListener} adapter that delegates the processing of\n * an event to a {@link TransactionalEventListener} annotated method. Supports\n * the exact same features as any regular {@link EventListener} annotated method\n * but is aware of the transactional context of the event publisher.\n *\n * <p>Processing of {@link TransactionalEventListener} is enabled automatically\n * when Spring's transaction management is enabled. For other cases, registering\n * a bean of type {@link TransactionalEventListenerFactory} is required.\n *\n * @author Stephane Nicoll\n * @author Juergen Hoeller\n * @since 5.3\n * @see TransactionalEventListener\n * @see TransactionalApplicationListener\n * @see TransactionalApplicationListenerAdapter\n */",
            "/**\n * 将事件处理委托给 {@link TransactionalEventListener} 注解方法的\n * {@link GenericApplicationListener} 适配器。支持与任何常规\n * {@link EventListener} 注解方法完全相同的功能，但感知事件发布者的事务上下文。\n *\n * <p>启用 Spring 事务管理时，{@link TransactionalEventListener} 的处理会自动启用。\n * 其他情况下需注册 {@link TransactionalEventListenerFactory} 类型的 Bean。\n *\n * @author Stephane Nicoll\n * @author Juergen Hoeller\n * @since 5.3\n * @see TransactionalEventListener\n * @see TransactionalApplicationListener\n * @see TransactionalApplicationListenerAdapter\n */",
        ),
        (
            "\t/**\n\t * Construct a new TransactionalApplicationListenerMethodAdapter.\n\t * @param beanName the name of the bean to invoke the listener method on\n\t * @param targetClass the target class that the method is declared on\n\t * @param method the listener method to invoke\n\t */",
            "\t/**\n\t * 构造新的 TransactionalApplicationListenerMethodAdapter。\n\t * @param beanName 要调用监听器方法的 Bean 名称\n\t * @param targetClass 声明该方法的目标类\n\t * @param method 要调用的监听器方法\n\t */",
        ),
        (
            "\t\t\t// No transactional event execution at all",
            "\t\t\t// 完全无事务事件执行",
        ),
    ],
    "TransactionalApplicationListenerSynchronization.java": [
        (
            "/**\n * {@code TransactionSynchronization} implementations for event processing with a\n * {@link TransactionalApplicationListener}.\n *\n * @author Juergen Hoeller\n * @since 5.3\n * @param <E> the specific {@code ApplicationEvent} subclass to listen to\n */",
            "/**\n * 与 {@link TransactionalApplicationListener} 配合进行事件处理的\n * {@code TransactionSynchronization} 实现。\n *\n * @author Juergen Hoeller\n * @since 5.3\n * @param <E> 要监听的特定 {@code ApplicationEvent} 子类\n */",
        ),
    ],
    "TransactionalEventListener.java": [
        (
            "/**\n * An {@link EventListener} that is invoked according to a {@link TransactionPhase}.\n * This is an annotation-based equivalent of {@link TransactionalApplicationListener}.\n *\n * <p>If the event is not published within an active transaction, the event is discarded\n * unless the {@link #fallbackExecution} flag is explicitly set. If a transaction is\n * running, the event is handled according to its {@code TransactionPhase}.\n *\n * <p>Adding {@link org.springframework.core.annotation.Order @Order} to your annotated\n * method allows you to prioritize that listener amongst other listeners running before\n * or after transaction completion.\n *\n * <p>As of 6.1, transactional event listeners can work with thread-bound transactions managed\n * by a {@link org.springframework.transaction.PlatformTransactionManager} as well as reactive\n * transactions managed by a {@link org.springframework.transaction.ReactiveTransactionManager}.\n * For the former, listeners are guaranteed to see the current thread-bound transaction.\n * Since the latter uses the Reactor context instead of thread-local variables, the transaction\n * context needs to be included in the published event instance as the event source:\n * see {@link org.springframework.transaction.reactive.TransactionalEventPublisher}.\n *\n * <p><strong>WARNING:</strong> if the {@code TransactionPhase} is set to\n * {@link TransactionPhase#AFTER_COMMIT AFTER_COMMIT} (the default),\n * {@link TransactionPhase#AFTER_ROLLBACK AFTER_ROLLBACK}, or\n * {@link TransactionPhase#AFTER_COMPLETION AFTER_COMPLETION}, the transaction will\n * have been committed or rolled back already, but the transactional resources might\n * still be active and accessible. As a consequence, any data access code triggered\n * at this point will still \"participate\" in the original transaction, but changes\n * will not be committed to the transactional resource. See\n * {@link org.springframework.transaction.support.TransactionSynchronization#afterCompletion(int)\n * TransactionSynchronization.afterCompletion(int)} for details.\n *\n * @author Stephane Nicoll\n * @author Sam Brannen\n * @author Oliver Drotbohm\n * @since 4.2\n * @see TransactionalApplicationListener\n * @see TransactionalApplicationListenerMethodAdapter\n */",
            "/**\n * 根据 {@link TransactionPhase} 调用的 {@link EventListener}。\n * 这是 {@link TransactionalApplicationListener} 的基于注解的等价物。\n *\n * <p>若事件未在活动事务内发布，除非显式设置 {@link #fallbackExecution} 标志，\n * 否则事件将被丢弃。若有事务运行，则按 {@code TransactionPhase} 处理事件。\n *\n * <p>在注解方法上添加 {@link org.springframework.core.annotation.Order @Order}\n * 可让该监听器在事务完成前后运行的其他监听器中优先执行。\n *\n * <p>自 6.1 起，事务事件监听器可与由\n * {@link org.springframework.transaction.PlatformTransactionManager} 管理的线程绑定事务\n * 以及由 {@link org.springframework.transaction.ReactiveTransactionManager} 管理的响应式事务配合工作。\n * 对于前者，监听器保证能看到当前线程绑定的事务。\n * 由于后者使用 Reactor 上下文而非线程局部变量，\n * 事务上下文需作为事件源包含在发布的事件实例中：\n * 参见 {@link org.springframework.transaction.reactive.TransactionalEventPublisher}。\n *\n * <p><strong>警告：</strong>若 {@code TransactionPhase} 设为\n * {@link TransactionPhase#AFTER_COMMIT AFTER_COMMIT}（默认）、\n * {@link TransactionPhase#AFTER_ROLLBACK AFTER_ROLLBACK} 或\n * {@link TransactionPhase#AFTER_COMPLETION AFTER_COMPLETION}，\n * 事务已提交或回滚，但事务资源可能仍活跃且可访问。\n * 因此，此阶段触发的任何数据访问代码仍将 \"参与\" 原始事务，\n * 但变更不会提交到事务资源。详见\n * {@link org.springframework.transaction.support.TransactionSynchronization#afterCompletion(int)\n * TransactionSynchronization.afterCompletion(int)}。\n *\n * @author Stephane Nicoll\n * @author Sam Brannen\n * @author Oliver Drotbohm\n * @since 4.2\n * @see TransactionalApplicationListener\n * @see TransactionalApplicationListenerMethodAdapter\n */",
        ),
        (
            "\t/**\n\t * Phase to bind the handling of an event to.\n\t * <p>The default phase is {@link TransactionPhase#AFTER_COMMIT}.\n\t * <p>If no transaction is in progress, the event is not processed at\n\t * all unless {@link #fallbackExecution} has been enabled explicitly.\n\t */",
            "\t/**\n\t * 绑定事件处理的事务阶段。\n\t * <p>默认阶段为 {@link TransactionPhase#AFTER_COMMIT}。\n\t * <p>若无事务进行中，除非显式启用 {@link #fallbackExecution}，\n\t * 否则事件完全不会被处理。\n\t */",
        ),
        (
            "\t/**\n\t * Alias for {@link #classes}.\n\t */",
            "\t/**\n\t * {@link #classes} 的别名。\n\t */",
        ),
        (
            "\t/**\n\t * The event classes that this listener handles.\n\t * <p>If this attribute is specified with a single value, the annotated\n\t * method may optionally accept a single parameter. However, if this\n\t * attribute is specified with multiple values, the annotated method\n\t * must <em>not</em> declare any parameters.\n\t */",
            "\t/**\n\t * 此监听器处理的事件类。\n\t * <p>若此属性以单个值指定，注解方法可选择接受单个参数。\n\t * 但若以多个值指定，注解方法<em>不得</em>声明任何参数。\n\t */",
        ),
        (
            "\t/**\n\t * Spring Expression Language (SpEL) attribute used for making the event\n\t * handling conditional.\n\t * <p>The default is {@code \"\"}, meaning the event is always handled.\n\t * @see EventListener#condition\n\t */",
            "\t/**\n\t * 用于使事件处理条件化的 Spring 表达式语言（SpEL）属性。\n\t * <p>默认为 {@code \"\"}，表示始终处理事件。\n\t * @see EventListener#condition\n\t */",
        ),
        (
            "\t/**\n\t * Whether the event should be handled if no transaction is running.\n\t * @see EventListener#defaultExecution()\n\t */",
            "\t/**\n\t * 若无事务运行，是否应处理事件。\n\t * @see EventListener#defaultExecution()\n\t */",
        ),
        (
            "\t/**\n\t * An optional identifier for the listener, defaulting to the fully-qualified\n\t * signature of the declaring method (for example, \"mypackage.MyClass.myMethod()\").\n\t * @since 5.3\n\t * @see EventListener#id\n\t * @see TransactionalApplicationListener#getListenerId()\n\t */",
            "\t/**\n\t * 监听器的可选标识符，默认为声明方法的全限定签名\n\t * （例如 \"mypackage.MyClass.myMethod()\"）。\n\t * @since 5.3\n\t * @see EventListener#id\n\t * @see TransactionalApplicationListener#getListenerId()\n\t */",
        ),
    ],
    "TransactionalEventListenerFactory.java": [
        (
            "/**\n * {@link EventListenerFactory} implementation that handles {@link TransactionalEventListener}\n * annotated methods.\n *\n * @author Stephane Nicoll\n * @since 4.2\n * @see TransactionalApplicationListenerMethodAdapter\n */",
            "/**\n * 处理 {@link TransactionalEventListener} 注解方法的\n * {@link EventListenerFactory} 实现。\n * 为带 {@link TransactionalEventListener} 的方法创建\n * {@link TransactionalApplicationListenerMethodAdapter} 监听器。\n *\n * @author Stephane Nicoll\n * @since 4.2\n * @see TransactionalApplicationListenerMethodAdapter\n */",
        ),
        (
            "\tprivate int order = 50;\n\n\n\tpublic void setOrder(int order) {",
            "\t/** 工厂在事件监听器处理器链中的顺序。 */\n\tprivate int order = 50;\n\n\n\t/** 设置工厂顺序。 */\n\tpublic void setOrder(int order) {",
        ),
    ],
}
