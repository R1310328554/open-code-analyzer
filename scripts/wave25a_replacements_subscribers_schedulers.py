"""Replacement tuples for RxJava wave-25a schedulers and internal subscribers [0:15]."""

SUBSCRIBERS_SCHEDULERS_W25A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "SingleScheduler.java": [
        (
            "/**\n * A scheduler with a shared, single threaded underlying ScheduledExecutorService.\n * @since 2.0\n */",
            "/**\n"
            " * 共享单线程 {@link ScheduledExecutorService} 的 Scheduler。\n"
            " * 所有 {@link Worker} 与 direct 调度任务复用同一底层 executor。\n"
            " * @since 2.0\n"
            " */",
        ),
        (
            "    /** The name of the system property for setting the thread priority for this Scheduler. */",
            "    /** 设置本 Scheduler 线程优先级的系统属性键。 */",
        ),
        (
            "    /**\n     * Constructs a SingleScheduler with the given ThreadFactory and prepares the\n     * single scheduler thread.\n     * @param threadFactory thread factory to use for creating worker threads. Note that this takes precedence over any\n     *                      system properties for configuring new thread creation. Cannot be null.\n     */",
            "    /**\n"
            "     * 使用给定 {@link ThreadFactory} 构造并准备单线程 executor。\n"
            "     * @param threadFactory 创建 Worker 线程的工厂；优先于相关系统属性；不可为 null\n"
            "     */",
        ),
        (
            "    static final class ScheduledWorker extends Scheduler.Worker {",
            "    /** 绑定共享 executor 的 Worker；任务由 {@link CompositeDisposable} 统一管理。 */\n"
            "    static final class ScheduledWorker extends Scheduler.Worker {",
        ),
        (
            "        @NonNull\n        @Override\n        public Disposable schedule(@NonNull Runnable run, long delay, @NonNull TimeUnit unit) {",
            "        /** 在共享 executor 上调度任务；dispose 后返回 {@link EmptyDisposable#INSTANCE}。 */\n"
            "        @NonNull\n        @Override\n        public Disposable schedule(@NonNull Runnable run, long delay, @NonNull TimeUnit unit) {",
        ),
        (
            "        @Override\n        public void dispose() {",
            "        /** 标记 disposed 并 dispose 所有已登记任务。 */\n"
            "        @Override\n        public void dispose() {",
        ),
    ],
    "TrampolineScheduler.java": [
        (
            "/**\n * Schedules work on the current thread but does not execute immediately. Work is put in a queue and executed\n * after the current unit of work is completed.\n */",
            "/**\n"
            " * 在当前线程上调度任务，但不立即执行；任务入队并在当前工作单元完成后按序运行。\n"
            " * 单例 {@link #instance()} 供全局 trampoline 调度使用。\n"
            " */",
        ),
        (
            "    public static TrampolineScheduler instance() {",
            "    /** @return 全局 TrampolineScheduler 单例 */\n"
            "    public static TrampolineScheduler instance() {",
        ),
        (
            "    static final class TrampolineWorker extends Scheduler.Worker {",
            "    /** 基于优先级队列在当前线程串行执行任务。 */\n"
            "    static final class TrampolineWorker extends Scheduler.Worker {",
        ),
        (
            "                // queue wasn't empty, a parent is already processing so we just add to the end of the queue",
            "                // 队列非空，已有父级在处理，本任务仅入队等待",
        ),
        (
            "        final int count; // In case if time between enqueueing took less than 1ms",
            "        final int count; // 入队间隔不足 1ms 时用于稳定排序",
        ),
        (
            "    static final class SleepingRunnable implements Runnable {",
            "    /** 若执行时间未到则 sleep，到期后在 worker 未 dispose 时运行任务。 */\n"
            "    static final class SleepingRunnable implements Runnable {",
        ),
    ],
    "BasicFuseableConditionalSubscriber.java": [
        (
            "/**\n * Base class for a fuseable intermediate subscriber.\n * @param <T> the upstream value type\n * @param <R> the downstream value type\n */",
            "/**\n"
            " * 可融合（fuseable）中间 subscriber 的基类。\n"
            " * @param <T> 上游值类型\n"
            " * @param <R> 下游值类型\n"
            " */",
        ),
        (
            "    /** The downstream subscriber. */",
            "    /** 下游 subscriber。 */",
        ),
        (
            "    /** The upstream subscription. */",
            "    /** 上游 subscription。 */",
        ),
        (
            "    /** The upstream's QueueSubscription if not null. */",
            "    /** 上游的 QueueSubscription（非 null 时）。 */",
        ),
        (
            "    /** Flag indicating no further onXXX event should be accepted. */",
            "    /** 标志：不再接受 onXXX 事件。 */",
        ),
        (
            "    /** Holds the established fusion mode of the upstream. */",
            "    /** 保存上游已建立的融合模式。 */",
        ),
        (
            "    /**\n     * Construct a BasicFuseableSubscriber by wrapping the given subscriber.\n     * @param downstream the subscriber, not null (not verified)\n     */",
            "    /**\n"
            "     * 通过包装给定 subscriber 构造 BasicFuseableConditionalSubscriber。\n"
            "     * @param downstream subscriber，不可为 null（未校验）\n"
            "     */",
        ),
        (
            "    /**\n     * Override this to perform actions before the call {@code actual.onSubscribe(this)} happens.\n     * @return true if onSubscribe should continue with the call\n     */",
            "    /**\n"
            "     * 在调用 {@code actual.onSubscribe(this)} 之前执行操作，可覆盖本方法。\n"
            "     * @return 若应继续 onSubscribe 调用则为 true\n"
            "     */",
        ),
        (
            "    /**\n     * Override this to perform actions after the call to {@code actual.onSubscribe(this)} happened.\n     */",
            "    /**\n"
            "     * 在 {@code actual.onSubscribe(this)} 调用完成后执行操作，可覆盖本方法。\n"
            "     */",
        ),
        (
            "    /**\n     * Rethrows the throwable if it is a fatal exception or calls {@link #onError(Throwable)}.\n     * @param t the throwable to rethrow or signal to the actual subscriber\n     */",
            "    /**\n"
            "     * 若为致命异常则重新抛出，否则调用 {@link #onError(Throwable)}。\n"
            "     * @param t 要重新抛出或向实际 subscriber 发出的异常\n"
            "     */",
        ),
        (
            "    /**\n     * Calls the upstream's QueueSubscription.requestFusion with the mode and\n     * saves the established mode in {@link #sourceMode} if that mode doesn't\n     * have the {@link QueueSubscription#BOUNDARY} flag set.\n     * <p>\n     * If the upstream doesn't support fusion ({@link #qs} is null), the method\n     * returns {@link QueueSubscription#NONE}.\n     * @param mode the fusion mode requested\n     * @return the established fusion mode\n     */",
            "    /**\n"
            "     * 以给定 mode 调用上游 QueueSubscription.requestFusion，\n"
            "     * 若该 mode 未设置 {@link QueueSubscription#BOUNDARY} 标志，\n"
            "     * 则将已建立的模式保存到 {@link #sourceMode}。\n"
            "     * <p>\n"
            "     * 若上游不支持融合（{@link #qs} 为 null），返回 {@link QueueSubscription#NONE}。\n"
            "     * @param mode 请求的融合模式\n"
            "     * @return 已建立的融合模式\n"
            "     */",
        ),
    ],
    "BasicFuseableSubscriber.java": [
        (
            "/**\n * Base class for a fuseable intermediate subscriber.\n * @param <T> the upstream value type\n * @param <R> the downstream value type\n */",
            "/**\n"
            " * 可融合（fuseable）中间 subscriber 的基类。\n"
            " * @param <T> 上游值类型\n"
            " * @param <R> 下游值类型\n"
            " */",
        ),
        (
            "    /** The downstream subscriber. */",
            "    /** 下游 subscriber。 */",
        ),
        (
            "    /** The upstream subscription. */",
            "    /** 上游 subscription。 */",
        ),
        (
            "    /** The upstream's QueueSubscription if not null. */",
            "    /** 上游的 QueueSubscription（非 null 时）。 */",
        ),
        (
            "    /** Flag indicating no further onXXX event should be accepted. */",
            "    /** 标志：不再接受 onXXX 事件。 */",
        ),
        (
            "    /** Holds the established fusion mode of the upstream. */",
            "    /** 保存上游已建立的融合模式。 */",
        ),
        (
            "    /**\n     * Construct a BasicFuseableSubscriber by wrapping the given subscriber.\n     * @param downstream the subscriber, not null (not verified)\n     */",
            "    /**\n"
            "     * 通过包装给定 subscriber 构造 BasicFuseableSubscriber。\n"
            "     * @param downstream subscriber，不可为 null（未校验）\n"
            "     */",
        ),
        (
            "    /**\n     * Override this to perform actions before the call {@code actual.onSubscribe(this)} happens.\n     * @return true if onSubscribe should continue with the call\n     */",
            "    /**\n"
            "     * 在调用 {@code actual.onSubscribe(this)} 之前执行操作，可覆盖本方法。\n"
            "     * @return 若应继续 onSubscribe 调用则为 true\n"
            "     */",
        ),
        (
            "    /**\n     * Override this to perform actions after the call to {@code actual.onSubscribe(this)} happened.\n     */",
            "    /**\n"
            "     * 在 {@code actual.onSubscribe(this)} 调用完成后执行操作，可覆盖本方法。\n"
            "     */",
        ),
        (
            "    /**\n     * Rethrows the throwable if it is a fatal exception or calls {@link #onError(Throwable)}.\n     * @param t the throwable to rethrow or signal to the actual subscriber\n     */",
            "    /**\n"
            "     * 若为致命异常则重新抛出，否则调用 {@link #onError(Throwable)}。\n"
            "     * @param t 要重新抛出或向实际 subscriber 发出的异常\n"
            "     */",
        ),
        (
            "    /**\n     * Calls the upstream's QueueSubscription.requestFusion with the mode and\n     * saves the established mode in {@link #sourceMode} if that mode doesn't\n     * have the {@link QueueSubscription#BOUNDARY} flag set.\n     * <p>\n     * If the upstream doesn't support fusion ({@link #qs} is null), the method\n     * returns {@link QueueSubscription#NONE}.\n     * @param mode the fusion mode requested\n     * @return the established fusion mode\n     */",
            "    /**\n"
            "     * 以给定 mode 调用上游 QueueSubscription.requestFusion，\n"
            "     * 若该 mode 未设置 {@link QueueSubscription#BOUNDARY} 标志，\n"
            "     * 则将已建立的模式保存到 {@link #sourceMode}。\n"
            "     * <p>\n"
            "     * 若上游不支持融合（{@link #qs} 为 null），返回 {@link QueueSubscription#NONE}。\n"
            "     * @param mode 请求的融合模式\n"
            "     * @return 已建立的融合模式\n"
            "     */",
        ),
    ],
    "BlockingBaseSubscriber.java": [
        (
            "public abstract class BlockingBaseSubscriber<T> extends CountDownLatch",
            "/**\n"
            " * 使用 {@link CountDownLatch} 等待终止的阻塞 subscriber 基类。\n"
            " *\n"
            " * @param <T> 值类型\n"
            " */\n"
            "public abstract class BlockingBaseSubscriber<T> extends CountDownLatch",
        ),
        (
            "    /**\n     * Block until the first value arrives and return it, otherwise\n     * return null for an empty source and rethrow any exception.\n     * @return the first value or null if the source is empty\n     */",
            "    /**\n"
            "     * 阻塞直到首个值到达并返回；若源为空则返回 null，\n"
            "     * 若有异常则重新抛出。\n"
            "     * @return 首个值，或源为空时返回 null\n"
            "     */",
        ),
        (
            "    @Override\n    public final void onSubscribe(Subscription s) {",
            "    /** 验证 subscription 并请求 {@code Long.MAX_VALUE}；取消时同步 cancel 上游。 */\n"
            "    @Override\n    public final void onSubscribe(Subscription s) {",
        ),
    ],
    "BlockingFirstSubscriber.java": [
        (
            "/**\n * Blocks until the upstream signals its first value or completes.\n *\n * @param <T> the value type\n */",
            "/**\n"
            " * 阻塞直到上游发出首个值或完成。\n"
            " *\n"
            " * @param <T> 值类型\n"
            " */",
        ),
        (
            "    @Override\n    public void onNext(T t) {",
            "    /** 保存首个值、取消上游并 countDown。 */\n"
            "    @Override\n    public void onNext(T t) {",
        ),
        (
            "    @Override\n    public void onError(Throwable t) {",
            "    /** 尚无值时记录 error；已有值则经 RxJavaPlugins 上报额外错误。 */\n"
            "    @Override\n    public void onError(Throwable t) {",
        ),
    ],
    "BlockingLastSubscriber.java": [
        (
            "/**\n * Blocks until the upstream signals its last value or completes.\n *\n * @param <T> the value type\n */",
            "/**\n"
            " * 阻塞直到上游发出最后一个值或完成。\n"
            " *\n"
            " * @param <T> 值类型\n"
            " */",
        ),
        (
            "    @Override\n    public void onNext(T t) {",
            "    /** 持续覆盖 value 以保留最后一个元素。 */\n"
            "    @Override\n    public void onNext(T t) {",
        ),
        (
            "    @Override\n    public void onError(Throwable t) {",
            "    /** 清空 value、记录 error 并 countDown。 */\n"
            "    @Override\n    public void onError(Throwable t) {",
        ),
    ],
    "BlockingSubscriber.java": [
        (
            "public final class BlockingSubscriber<T> extends AtomicReference<Subscription> implements FlowableSubscriber<T>, Subscription {",
            "/**\n"
            " * 将 Reactive Streams 通知编码入阻塞 {@link Queue} 的 Subscriber，\n"
            " * 同时实现 {@link Subscription} 以组合取消与背压。\n"
            " *\n"
            " * @param <T> 值类型\n"
            " */\n"
            "public final class BlockingSubscriber<T> extends AtomicReference<Subscription> implements FlowableSubscriber<T>, Subscription {",
        ),
        (
            "    public static final Object TERMINATED = new Object();",
            "    /** 取消时入队的哨兵对象，唤醒阻塞消费者。 */\n"
            "    public static final Object TERMINATED = new Object();",
        ),
        (
            "    public BlockingSubscriber(Queue<Object> queue) {",
            "    /** @param queue 接收 NotificationLite 编码通知的队列 */\n"
            "    public BlockingSubscriber(Queue<Object> queue) {",
        ),
        (
            "    public boolean isCancelled() {",
            "    /** @return 若 subscription 已取消则为 true */\n"
            "    public boolean isCancelled() {",
        ),
    ],
    "BoundedSubscriber.java": [
        (
            "public final class BoundedSubscriber<T> extends AtomicReference<Subscription>",
            "/**\n"
            " * 带预取缓冲的 lambda {@link FlowableSubscriber}；\n"
            " * 消费达到 limit 时向上游补 request，实现有界背压。\n"
            " *\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class BoundedSubscriber<T> extends AtomicReference<Subscription>",
        ),
        (
            "    public BoundedSubscriber(Consumer<? super T> onNext, Consumer<? super Throwable> onError,\n                            Action onComplete, Consumer<? super Subscription> onSubscribe, int bufferSize) {",
            "    /**\n"
            "     * @param onNext 下一项回调\n"
            "     * @param onError 错误回调\n"
            "     * @param onComplete 完成回调\n"
            "     * @param onSubscribe 订阅回调\n"
            "     * @param bufferSize 预取缓冲大小\n"
            "     */\n"
            "    public BoundedSubscriber(Consumer<? super T> onNext, Consumer<? super Throwable> onError,\n"
            "                            Action onComplete, Consumer<? super Subscription> onSubscribe, int bufferSize) {",
        ),
        (
            "    @Override\n    public boolean hasCustomOnError() {",
            "    /** 若 onError 不是默认的 ON_ERROR_MISSING 则返回 true。 */\n"
            "    @Override\n    public boolean hasCustomOnError() {",
        ),
    ],
    "DeferredScalarSubscriber.java": [
        (
            "/**\n * A subscriber, extending a DeferredScalarSubscription,\n *  that is unbounded-in and can generate 0 or 1 resulting value.\n * @param <T> the input value type\n * @param <R> the output value type\n */",
            "/**\n"
            " * 继承 {@link DeferredScalarSubscription} 的 subscriber：\n"
            " * 无界请求上游，可生成 0 或 1 个结果值。\n"
            " * @param <T> 输入值类型\n"
            " * @param <R> 输出值类型\n"
            " */",
        ),
        (
            "    /** The upstream subscription. */",
            "    /** 上游 subscription。 */",
        ),
        (
            "    /** Can indicate if there was at least on onNext call. */",
            "    /** 是否至少收到一次 onNext。 */",
        ),
        (
            "    /**\n     * Creates a DeferredScalarSubscriber instance and wraps a downstream Subscriber.\n     * @param downstream the downstream subscriber, not null (not verified)\n     */",
            "    /**\n"
            "     * 创建 DeferredScalarSubscriber 实例并包装下游 Subscriber。\n"
            "     * @param downstream 下游 subscriber，非 null（未校验）\n"
            "     */",
        ),
        (
            "    @Override\n    public void cancel() {",
            "    /** 取消自身并 cancel 上游 subscription。 */\n"
            "    @Override\n    public void cancel() {",
        ),
    ],
    "DisposableAutoReleaseSubscriber.java": [
        (
            "/**\n * Wraps lambda callbacks and when the upstream terminates or this subscriber gets disposed,\n * removes itself from a {@link io.reactivex.rxjava4.disposables.CompositeDisposable}.\n * <p>History: 0.18.0 @ RxJavaExtensions\n * @param <T> the element type consumed\n * @since 3.1.0\n */",
            "/**\n"
            " * 包装 lambda 回调；上游终止或本 subscriber 被 dispose 时，\n"
            " * 从 {@link io.reactivex.rxjava4.disposables.CompositeDisposable} 中移除自身。\n"
            " * <p>History: 0.18.0 @ RxJavaExtensions\n"
            " * @param <T> 消费的元素类型\n"
            " * @since 3.1.0\n"
            " */",
        ),
        (
            "    public DisposableAutoReleaseSubscriber(\n            DisposableContainer composite,\n            Consumer<? super T> onNext,\n            Consumer<? super Throwable> onError,\n            Action onComplete\n    ) {",
            "    /**\n"
            "     * @param composite 要从中移除自身的复合容器\n"
            "     * @param onNext 下一项回调\n"
            "     * @param onError 错误回调\n"
            "     * @param onComplete 完成回调\n"
            "     */\n"
            "    public DisposableAutoReleaseSubscriber(\n"
            "            DisposableContainer composite,\n"
            "            Consumer<? super T> onNext,\n"
            "            Consumer<? super Throwable> onError,\n"
            "            Action onComplete\n    ) {",
        ),
        (
            "    void removeSelf() {",
            "    /** 从 CompositeDisposable 中删除自身引用。 */\n"
            "    void removeSelf() {",
        ),
        (
            "    @Override\n    public void onSubscribe(Subscription s) {",
            "    /** 设置 subscription 并无界 request 上游。 */\n"
            "    @Override\n    public void onSubscribe(Subscription s) {",
        ),
    ],
    "ForEachWhileSubscriber.java": [
        (
            "public final class ForEachWhileSubscriber<T>",
            "/**\n"
            " * 对每个上游元素调用 {@link Predicate}；当 predicate 返回 false 或序列结束时 dispose 并触发完成。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class ForEachWhileSubscriber<T>",
        ),
        (
            "    public ForEachWhileSubscriber(Predicate<? super T> onNext,\n            Consumer<? super Throwable> onError, Action onComplete) {",
            "    /**\n"
            "     * @param onNext 对每个元素执行的 predicate\n"
            "     * @param onError 错误回调\n"
            "     * @param onComplete 完成回调\n"
            "     */\n"
            "    public ForEachWhileSubscriber(Predicate<? super T> onNext,\n"
            "            Consumer<? super Throwable> onError, Action onComplete) {",
        ),
        (
            "    @Override\n    public void onNext(T t) {",
            "    /** 测试元素；若 predicate 返回 false 则 dispose 并调用 onComplete。 */\n"
            "    @Override\n    public void onNext(T t) {",
        ),
    ],
    "FutureSubscriber.java": [
        (
            "/**\n * A Subscriber + Future that expects exactly one upstream value and provides it\n * via the (blocking) Future API.\n *\n * @param <T> the value type\n */",
            "/**\n"
            " * 同时实现 Subscriber 与 Future，期望恰好一个上游值，\n"
            " * 并通过（阻塞）Future API 提供该值。\n"
            " *\n"
            " * @param <T> 值类型\n"
            " */",
        ),
        (
            "    @Override\n    public void onNext(T t) {",
            "    /** 接收唯一元素；若收到多个元素则 cancel 上游并报告错误。 */\n"
            "    @Override\n    public void onNext(T t) {",
        ),
        (
            "    @Override\n    public void cancel() {\n        // ignoring as `this` means a finished Subscription only",
            "    /** 忽略：终止后 `this` 仅表示已完成的 Subscription。 */\n"
            "    @Override\n    public void cancel() {\n        // ignoring as `this` means a finished Subscription only",
        ),
        (
            "    @Override\n    public void request(long n) {\n        // ignoring as `this` means a finished Subscription only",
            "    /** 忽略：终止后 `this` 仅表示已完成的 Subscription。 */\n"
            "    @Override\n    public void request(long n) {\n        // ignoring as `this` means a finished Subscription only",
        ),
    ],
    "InnerQueuedSubscriber.java": [
        (
            "/**\n * Subscriber that can fuse with the upstream and calls a support interface\n * whenever an event is available.\n *\n * @param <T> the value type\n */",
            "/**\n"
            " * 可与上游融合的 Subscriber，在有事件可用时回调支持接口。\n"
            " *\n"
            " * @param <T> 值类型\n"
            " */",
        ),
        (
            "    public InnerQueuedSubscriber(InnerQueuedSubscriberSupport<T> parent, int prefetch) {",
            "    /**\n"
            "     * @param parent 父级支持接口\n"
            "     * @param prefetch 预取数量\n"
            "     */\n"
            "    public InnerQueuedSubscriber(InnerQueuedSubscriberSupport<T> parent, int prefetch) {",
        ),
        (
            "    @Override\n    public void onSubscribe(Subscription s) {",
            "    /** 尝试融合；SYNC 模式直接 innerComplete，否则创建队列并预取。 */\n"
            "    @Override\n    public void onSubscribe(Subscription s) {",
        ),
        (
            "    public boolean isDone() {",
            "    /** 若内部序列已完成则返回 true。 */\n"
            "    public boolean isDone() {",
        ),
        (
            "    public void setDone() {",
            "    /** 将内部序列标记为已完成。 */\n"
            "    public void setDone() {",
        ),
        (
            "    public SimpleQueue<T> queue() {",
            "    /** @return 内部队列 */\n"
            "    public SimpleQueue<T> queue() {",
        ),
    ],
    "InnerQueuedSubscriberSupport.java": [
        (
            "/**\n * Interface to allow the InnerQueuedSubscriber to call back a parent\n * with signals.\n *\n * @param <T> the value type\n */",
            "/**\n"
            " * 允许 InnerQueuedSubscriber 向父级回传信号的接口。\n"
            " *\n"
            " * @param <T> 值类型\n"
            " */",
        ),
        (
            "    void innerNext(InnerQueuedSubscriber<T> inner, T value);",
            "    /** 内部 subscriber 收到下一项时回调。 */\n"
            "    void innerNext(InnerQueuedSubscriber<T> inner, T value);",
        ),
        (
            "    void innerError(InnerQueuedSubscriber<T> inner, Throwable e);",
            "    /** 内部 subscriber 收到错误时回调。 */\n"
            "    void innerError(InnerQueuedSubscriber<T> inner, Throwable e);",
        ),
        (
            "    void innerComplete(InnerQueuedSubscriber<T> inner);",
            "    /** 内部 subscriber 完成时回调。 */\n"
            "    void innerComplete(InnerQueuedSubscriber<T> inner);",
        ),
        (
            "    void drain();",
            "    /** 触发队列排空（drain）逻辑。 */\n"
            "    void drain();",
        ),
    ],
}
