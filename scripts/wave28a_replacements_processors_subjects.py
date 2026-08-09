"""Chinese JavaDoc replacements for RxJava wave28a processors/subjects/schedulers [0:15]."""

PROCESSORS_SUBJECTS_W28A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "src/main/java/io/reactivex/rxjava4/processors/FlowableProcessor.java": [
        (
            '/**\n * Represents a Subscriber and a Flowable (Publisher) at the same time, allowing\n * multicasting events from a single source to multiple child Subscribers.\n * <p>All methods except the onSubscribe, onNext, onError and onComplete are thread-safe.\n * Use {@link #toSerialized()} to make these methods thread-safe as well.\n *\n * @param <T> the item value type\n */',
            '/**\n * 同时表示 Subscriber 与 Flowable（Publisher），可将单一源的事件多播给多个子 Subscriber。\n * <p>除 onSubscribe、onNext、onError、onComplete 外，其余方法均为线程安全；\n * 可调用 {@link #toSerialized()} 使这些方法也线程安全。\n *\n * @param <T> 元素值类型\n */',
        ),
        (
            '/**\n     * Returns true if the FlowableProcessor has subscribers.\n     * <p>The method is thread-safe.\n     * @return true if the FlowableProcessor has subscribers\n     */',
            '/**\n     * 若 FlowableProcessor 当前有订阅者则返回 true。\n     * <p>本方法线程安全。\n     * @return 有订阅者时为 true\n     */',
        ),
        (
            '/**\n     * Returns true if the FlowableProcessor has reached a terminal state through an error event.\n     * <p>The method is thread-safe.\n     * @return true if the FlowableProcessor has reached a terminal state through an error event\n     * @see #getThrowable()\n     * @see #hasComplete()\n     */',
            '/**\n     * 若 FlowableProcessor 已通过 error 事件进入终止状态则返回 true。\n     * <p>本方法线程安全。\n     * @return 因 error 终止时为 true\n     * @see #getThrowable()\n     * @see #hasComplete()\n     */',
        ),
        (
            '/**\n     * Returns true if the FlowableProcessor has reached a terminal state through a complete event.\n     * <p>The method is thread-safe.\n     * @return true if the FlowableProcessor has reached a terminal state through a complete event\n     * @see #hasThrowable()\n     */',
            '/**\n     * 若 FlowableProcessor 已通过 complete 事件进入终止状态则返回 true。\n     * <p>本方法线程安全。\n     * @return 因 complete 终止时为 true\n     * @see #hasThrowable()\n     */',
        ),
        (
            "/**\n     * Returns the error that caused the FlowableProcessor to terminate or null if the FlowableProcessor\n     * hasn't terminated yet.\n     * <p>The method is thread-safe.\n     * @return the error that caused the FlowableProcessor to terminate or null if the FlowableProcessor\n     * hasn't terminated yet\n     */",
            '/**\n     * 返回导致 FlowableProcessor 终止的错误；尚未终止时返回 null。\n     * <p>本方法线程安全。\n     * @return 终止错误，或尚未终止时为 null\n     */',
        ),
        (
            '/**\n     * Wraps this FlowableProcessor and serializes the calls to the onSubscribe, onNext, onError and\n     * onComplete methods, making them thread-safe.\n     * <p>The method is thread-safe.\n     * @return the wrapped and serialized FlowableProcessor\n     */',
            '/**\n     * 包装本 FlowableProcessor，串行化 onSubscribe、onNext、onError、onComplete 调用，使其线程安全。\n     * <p>本方法线程安全。\n     * @return 包装后的串行化 FlowableProcessor\n     */',
        ),
    ],
    "src/main/java/io/reactivex/rxjava4/processors/SerializedProcessor.java": [
        (
            '/**\n * Serializes calls to the Subscriber methods.\n * <p>All other Publisher and Subject methods are thread-safe by design.\n *\n * @param <T> the item value type\n */',
            '/**\n * 串行化对 Subscriber 方法的调用。\n * <p>其余 Publisher 与 Subject 方法按设计已为线程安全。\n *\n * @param <T> 元素值类型\n */',
        ),
        (
            '/** The actual subscriber to serialize Subscriber calls to. */',
            '/** 实际接收串行化 Subscriber 调用的处理器。 */',
        ),
        (
            '/** Indicates an emission is going on, guarded by this. */',
            '/** 表示正在发射，由本对象监视器保护。 */',
        ),
        (
            '/** If not null, it holds the missed NotificationLite events. */',
            '/** 非 null 时保存错过的 NotificationLite 事件。 */',
        ),
        (
            '/** Indicates a terminal event has been received and all further events will be dropped. */',
            '/** 已收到终止事件，后续事件将被丢弃。 */',
        ),
        (
            '/**\n     * Constructor that wraps an actual subject.\n     * @param actual the subject wrapped\n     */',
            '/**\n     * 包装实际 subject 的构造器。\n     * @param actual 被包装的 subject\n     */',
        ),
        (
            '/** Loops until all notifications in the queue has been processed. */',
            '/** 循环处理队列中所有通知直至清空。 */',
        ),
    ],
    "src/main/java/io/reactivex/rxjava4/processors/package-info.java": [
        (
            '/**\n * Classes representing so-called hot backpressure-aware sources, aka <strong>processors</strong>,\n * that implement the {@link io.reactivex.rxjava4.processors.FlowableProcessor FlowableProcessor} class,\n * the Reactive Streams {@link java.util.concurrent.Flow.Processor Processor} interface\n * to allow forms of multicasting events to one or more subscribers as well as consuming another\n * Reactive Streams {@link java.util.concurrent.Flow.Publisher Publisher}.\n * <p>\n * Available processor implementations:\n * <br>\n * <ul>\n *     <li>{@link io.reactivex.rxjava4.processors.AsyncProcessor AsyncProcessor} - replays the very last item</li>\n *     <li>{@link io.reactivex.rxjava4.processors.BehaviorProcessor BehaviorProcessor} - remembers the latest item</li>\n *     <li>{@link io.reactivex.rxjava4.processors.MulticastProcessor MulticastProcessor} - coordinates its source with its consumers</li>\n *     <li>{@link io.reactivex.rxjava4.processors.PublishProcessor PublishProcessor} - dispatches items to current consumers</li>\n *     <li>{@link io.reactivex.rxjava4.processors.ReplayProcessor ReplayProcessor} - remembers some or all items and replays them to consumers</li>\n *     <li>{@link io.reactivex.rxjava4.processors.UnicastProcessor UnicastProcessor} - remembers or relays items to a single consumer</li>\n * </ul>\n * <p>\n * The non-backpressured variants of the {@code FlowableProcessor} class are called\n * {@link io.reactivex.rxjava4.subjects.Subject}s and reside in the {@code io.reactivex.subjects} package.\n * @see io.reactivex.rxjava4.subjects\n */',
            '/**\n * 表示所谓热、背压感知源（<strong>processor</strong>）的类：实现\n * {@link io.reactivex.rxjava4.processors.FlowableProcessor FlowableProcessor}，\n * 即 Reactive Streams {@link java.util.concurrent.Flow.Processor Processor} 接口，\n * 支持向一个或多个订阅者多播，也可消费另一 Reactive Streams {@link java.util.concurrent.Flow.Publisher Publisher}。\n * <p>\n * 可用 processor 实现：\n * <br>\n * <ul>\n *     <li>{@link io.reactivex.rxjava4.processors.AsyncProcessor AsyncProcessor} — 重放最后一项</li>\n *     <li>{@link io.reactivex.rxjava4.processors.BehaviorProcessor BehaviorProcessor} — 记住最新一项</li>\n *     <li>{@link io.reactivex.rxjava4.processors.MulticastProcessor MulticastProcessor} — 协调源与消费者</li>\n *     <li>{@link io.reactivex.rxjava4.processors.PublishProcessor PublishProcessor} — 向当前消费者分发</li>\n *     <li>{@link io.reactivex.rxjava4.processors.ReplayProcessor ReplayProcessor} — 记住部分或全部项并重放</li>\n *     <li>{@link io.reactivex.rxjava4.processors.UnicastProcessor UnicastProcessor} — 向单一消费者记住或转发</li>\n * </ul>\n * <p>\n * {@code FlowableProcessor} 的无背压变体称为 {@link io.reactivex.rxjava4.subjects.Subject}，\n * 位于 {@code io.reactivex.subjects} 包。\n * @see io.reactivex.rxjava4.subjects\n */',
        ),
    ],
    "src/main/java/io/reactivex/rxjava4/schedulers/BlockingScheduler.java": [
        (
            '/**\n * Holds onto a blocking scheduler instance and provides access to its {@link #execute()}\n * method and a way to obtain a pure {@link Scheduler} instance to be used as parameter.\n * <p>\n * <strong>Implementation note</strong><br>\n *           No need to instantiate this record by client applications, it serves as a way to\n *           give access to the {@code Scheduler} interface as well as the blocking-specific\n *           {@link #execute()} methods.\n * @param backingScheduler the scheduler instance\n * @since 4.0.0\n */',
            '/**\n * 持有阻塞调度器实例，提供 {@link #execute()} 访问，以及获取纯 {@link Scheduler} 视图供传参。\n * <p>\n * <strong>实现说明</strong><br>\n *           客户端无需实例化本 record；它同时暴露 {@code Scheduler} 接口与阻塞专用 {@link #execute()}。\n * @param backingScheduler 底层调度器实例\n * @since 4.0.0\n */',
        ),
        (
            '/**\n     * Returns the Scheduler view to submit tasks to or use it as a parameter.\n     * @return the Scheduler view of the underlying blocking current thread scheduler.\n     */',
            '/**\n     * 返回用于提交任务或作为参数的 Scheduler 视图。\n     * @return 底层阻塞当前线程调度器的 Scheduler 视图\n     */',
        ),
        (
            '/**\n     * Begin executing the blocking event loop without any initial action.\n     * <p>\n     * This method will block until the {@link Scheduler#shutdown()} is invoked.\n     * @see #execute(Action)\n     */',
            '/**\n     * 无初始动作启动阻塞事件循环。\n     * <p>\n     * 本方法阻塞直至调用 {@link Scheduler#shutdown()}。\n     * @see #execute(Action)\n     */',
        ),
        (
            "/**\n     * Begin executing the blocking event loop with the given initial action\n     * (usually contain the rest of the 'main' method).\n     * <p>\n     * This method will block until the {@link Scheduler#shutdown()} is invoked.\n     * @param action the action to execute\n     */",
            '/**\n     * 以给定初始动作（通常含 main 其余逻辑）启动阻塞事件循环。\n     * <p>\n     * 本方法阻塞直至调用 {@link Scheduler#shutdown()}。\n     * @param action 要执行的动作\n     */',
        ),
        (
            '/**\n     * Shuts down the underlying blocking current thread scheduler\n     */',
            '/**\n     * 关闭底层阻塞当前线程调度器\n     */',
        ),
    ],
    "src/main/java/io/reactivex/rxjava4/schedulers/SchedulerRunnableIntrospection.java": [
        (
            '/**\n * Interface to indicate the implementor class wraps a {@code Runnable} that can\n * be accessed via {@link #getWrappedRunnable()}.\n * <p>\n * You can check if a {@link Runnable} task submitted to a {@link io.reactivex.rxjava4.core.Scheduler Scheduler} (or its\n * {@link io.reactivex.rxjava4.core.Scheduler.Worker Scheduler.Worker}) implements this interface and unwrap the\n * original {@code Runnable} instance. This could help to avoid hooking the same underlying {@code Runnable}\n * task in a custom {@link RxJavaPlugins#onSchedule(Runnable)} hook set via\n * the {@link RxJavaPlugins#setScheduleHandler(Function)} method multiple times due to internal delegation\n * of the default {@code Scheduler.scheduleDirect} or {@code Scheduler.Worker.schedule} methods.\n * <p>History: 2.1.7 - experimental\n * @since 2.2\n */',
            '/**\n * 表明实现类包装了可通过 {@link #getWrappedRunnable()} 访问的 {@code Runnable}。\n * <p>\n * 可检查提交到 {@link io.reactivex.rxjava4.core.Scheduler Scheduler}\n * （或其 {@link io.reactivex.rxjava4.core.Scheduler.Worker Scheduler.Worker}）的 {@link Runnable}\n * 是否实现本接口以解包原始任务，避免在自定义\n * {@link RxJavaPlugins#onSchedule(Runnable)} 钩子中因内部委托而重复包装同一底层任务。\n * <p>History: 2.1.7 - experimental\n * @since 2.2\n */',
        ),
        (
            '/**\n     * Returns the wrapped action.\n     *\n     * @return the wrapped action. Cannot be null.\n     */',
            '/**\n     * 返回被包装的动作。\n     *\n     * @return 被包装的动作，不可为 null\n     */',
        ),
    ],
    "src/main/java/io/reactivex/rxjava4/schedulers/TestScheduler.java": [
        (
            '/**\n * A special, non thread-safe scheduler for testing operators that require\n * a scheduler without introducing real concurrency and allows manually advancing\n * a virtual time.\n * <p>\n * By default, the tasks submitted via the various {@code schedule} methods are not\n * wrapped by the {@link RxJavaPlugins#onSchedule(Runnable)} hook. To enable this behavior,\n * create a {@code TestScheduler} via {@link #TestScheduler(boolean)} or {@link #TestScheduler(long, TimeUnit, boolean)}.\n */',
            '/**\n * 专用于测试的、非线程安全调度器：在不引入真实并发的前提下满足算子对调度器的需求，\n * 并允许手动推进虚拟时间。\n * <p>\n * 默认情况下，经各 {@code schedule} 方法提交的任务不会经 {@link RxJavaPlugins#onSchedule(Runnable)} 包装；\n * 若需启用，请通过 {@link #TestScheduler(boolean)} 或 {@link #TestScheduler(long, TimeUnit, boolean)} 创建。\n */',
        ),
        (
            '/** The ordered queue for the runnable tasks. */',
            '/** 按序存放 Runnable 任务的队列。 */',
        ),
        (
            '/** Use the {@link RxJavaPlugins#onSchedule(Runnable)} hook when scheduling tasks. */',
            '/** 调度任务时是否使用 {@link RxJavaPlugins#onSchedule(Runnable)} 钩子。 */',
        ),
        (
            '/** The per-scheduler global order counter. */',
            '/** 本调度器内全局顺序计数器。 */',
        ),
        (
            '/**\n     * Creates a new TestScheduler with initial virtual time of zero.\n     */',
            '/**\n     * 创建虚拟时间初始为零的 TestScheduler。\n     */',
        ),
        (
            '/**\n     * Creates a new TestScheduler with the option to use the\n     * {@link RxJavaPlugins#onSchedule(Runnable)} hook when scheduling tasks.\n     * <p>History: 3.0.10 - experimental\n     * @param useOnScheduleHook if {@code true}, the tasks submitted to this\n     *                          TestScheduler is wrapped via the\n     *                          {@link RxJavaPlugins#onSchedule(Runnable)} hook\n     * @since 3.1.0\n     */',
            '/**\n     * 创建 TestScheduler，可选是否在调度时使用 {@link RxJavaPlugins#onSchedule(Runnable)} 包装任务。\n     * <p>History: 3.0.10 - experimental\n     * @param useOnScheduleHook 为 {@code true} 时经 {@link RxJavaPlugins#onSchedule(Runnable)} 包装提交任务\n     * @since 3.1.0\n     */',
        ),
        (
            "/**\n     * Creates a new TestScheduler with the specified initial virtual time.\n     *\n     * @param delayTime\n     *          the point in time to move the Scheduler's clock to\n     * @param unit\n     *          the units of time that {@code delayTime} is expressed in\n     */",
            '/**\n     * 以指定初始虚拟时间创建 TestScheduler。\n     *\n     * @param delayTime 调度器时钟要移动到的时刻\n     * @param unit {@code delayTime} 的时间单位\n     */',
        ),
        (
            "/**\n     * Creates a new TestScheduler with the specified initial virtual time\n     * and with the option to use the\n     * {@link RxJavaPlugins#onSchedule(Runnable)} hook when scheduling tasks.\n     * <p>History: 3.0.10 - experimental\n     * @param delayTime\n     *          the point in time to move the Scheduler's clock to\n     * @param unit\n     *          the units of time that {@code delayTime} is expressed in\n     * @param useOnScheduleHook if {@code true}, the tasks submitted to this\n     *                          TestScheduler is wrapped via the\n     *                          {@link RxJavaPlugins#onSchedule(Runnable)} hook\n     * @since 3.1.0\n     */",
            '/**\n     * 以指定初始虚拟时间创建 TestScheduler，并可选是否使用\n     * {@link RxJavaPlugins#onSchedule(Runnable)} 包装任务。\n     * <p>History: 3.0.10 - experimental\n     * @param delayTime 调度器时钟要移动到的时刻\n     * @param unit {@code delayTime} 的时间单位\n     * @param useOnScheduleHook 为 {@code true} 时经 {@link RxJavaPlugins#onSchedule(Runnable)} 包装提交任务\n     * @since 3.1.0\n     */',
        ),
        (
            '/**\n     * @param count for differentiating tasks at same time\n     */',
            '/**\n     * @param count 区分同一时刻任务的序号\n     */',
        ),
        (
            "/**\n     * Moves the Scheduler's clock forward by a specified amount of time.\n     *\n     * @param delayTime\n     *          the amount of time to move the Scheduler's clock forward\n     * @param unit\n     *          the units of time that {@code delayTime} is expressed in\n     */",
            '/**\n     * 将调度器时钟向前推进指定时长。\n     *\n     * @param delayTime 推进量\n     * @param unit {@code delayTime} 的时间单位\n     */',
        ),
        (
            "/**\n     * Moves the Scheduler's clock to a particular moment in time.\n     *\n     * @param delayTime\n     *          the point in time to move the Scheduler's clock to\n     * @param unit\n     *          the units of time that {@code delayTime} is expressed in\n     */",
            '/**\n     * 将调度器时钟移动到指定时刻。\n     *\n     * @param delayTime 目标时刻\n     * @param unit {@code delayTime} 的时间单位\n     */',
        ),
        (
            "/**\n     * Triggers any actions that have not yet been triggered and that are scheduled to be triggered at or\n     * before this Scheduler's present time.\n     */",
            '/**\n     * 触发所有尚未执行、且计划在本调度器当前时刻或之前执行的动作。\n     */',
        ),
    ],
    "src/main/java/io/reactivex/rxjava4/schedulers/Timed.java": [
        (
            '/**\n * Holds onto a value along with time information.\n *\n * @param <T> the value type\n * @param value the item to store\n * @param time the time value\n * @param unit the unit of time\n * @since 4.0.0\n */',
            '/**\n * 保存值及其时间信息。\n *\n * @param <T> 值类型\n * @param value 要保存的项\n * @param time 时间值\n * @param unit 时间单位\n * @since 4.0.0\n */',
        ),
        (
            '/**\n     * Constructs a {@code Timed} instance with the given value and time information.\n     *\n     * @param value the value to hold\n     * @param time  the time to hold\n     * @param unit  the time unit, not null\n     * @throws NullPointerException if {@code value} or {@code unit} is {@code null}\n     */',
            '/**\n     * 以给定值与时间信息构造 {@code Timed}。\n     *\n     * @param value 要保存的值\n     * @param time  要保存的时间\n     * @param unit  时间单位，不可为 null\n     * @throws NullPointerException 若 {@code value} 或 {@code unit} 为 {@code null}\n     */',
        ),
        (
            '/**\n     * Returns the contained time value in the time unit specified.\n     *\n     * @param unit the time unit\n     * @return the converted time\n     */',
            '/**\n     * 以指定时间单位返回所含时间值。\n     *\n     * @param unit 时间单位\n     * @return 转换后的时间\n     */',
        ),
    ],
    "src/main/java/io/reactivex/rxjava4/schedulers/package-info.java": [
        (
            '/**\n * Contains notably the factory class of {@link io.reactivex.rxjava4.schedulers.Schedulers Schedulers} providing methods for\n * retrieving the standard scheduler instances, the {@link io.reactivex.rxjava4.schedulers.TestScheduler TestScheduler} for testing flows\n * with scheduling in a controlled manner and the class {@link io.reactivex.rxjava4.schedulers.Timed Timed} that can hold\n * a value and a timestamp associated with it.\n */',
            '/**\n * 本包包含 {@link io.reactivex.rxjava4.schedulers.Schedulers Schedulers} 工厂类（获取标准调度器实例）、\n * 用于可控测试调度的 {@link io.reactivex.rxjava4.schedulers.TestScheduler TestScheduler}，\n * 以及可同时保存值与时间戳的 {@link io.reactivex.rxjava4.schedulers.Timed Timed}。\n */',
        ),
    ],
    "src/main/java/io/reactivex/rxjava4/subjects/AsyncSubject.java": [
        (
            '/**\n * A Subject that emits the very last value followed by a completion event or the received error to Observers.\n * <p>\n * <img width="640" height="239" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/AsyncSubject.png" alt="">\n * <p>\n * This subject does not have a public constructor by design; a new empty instance of this\n * {@code AsyncSubject} can be created via the {@link #create()} method.\n * <p>\n * Since a {@code Subject} is conceptionally derived from the {@code Processor} type in the Reactive Streams specification,\n * {@code null}s are not allowed (<a href="https://github.com/reactive-streams/reactive-streams-jvm#2.13">Rule 2.13</a>)\n * as parameters to {@link #onNext(Object)} and {@link #onError(Throwable)}. Such calls will result in a\n * {@link NullPointerException} being thrown and the subject\'s state is not changed.\n * <p>\n * Since an {@code AsyncSubject} is an {@link io.reactivex.rxjava4.core.Observable}, it does not support backpressure.\n * <p>\n * When this {@code AsyncSubject} is terminated via {@link #onError(Throwable)}, the\n * last observed item (if any) is cleared and late {@link io.reactivex.rxjava4.core.Observer}s only receive\n * the {@code onError} event.\n * <p>\n * The {@code AsyncSubject} caches the latest item internally and it emits this item only when {@code onComplete} is called.\n * Therefore, it is not recommended to use this {@code Subject} with infinite or never-completing sources.\n * <p>\n * Even though {@code AsyncSubject} implements the {@code Observer} interface, calling\n * {@code onSubscribe} is not required (<a href="https://github.com/reactive-streams/reactive-streams-jvm#2.12">Rule 2.12</a>)\n * if the subject is used as a standalone source. However, calling {@code onSubscribe}\n * after the {@code AsyncSubject} reached its terminal state will result in the\n * given {@code Disposable} being disposed immediately.\n * <p>\n * Calling {@link #onNext(Object)}, {@link #onError(Throwable)} and {@link #onComplete()}\n * is required to be serialized (called from the same thread or called non-overlappingly from different threads\n * through external means of serialization). The {@link #toSerialized()} method available to all {@code Subject}s\n * provides such serialization and also protects against reentrance (i.e., when a downstream {@code Observer}\n * consuming this subject also wants to call {@link #onNext(Object)} on this subject recursively).\n * The implementation of onXXX methods are technically thread-safe but non-serialized calls\n * to them may lead to undefined state in the currently subscribed Observers.\n * <p>\n * This {@code AsyncSubject} supports the standard state-peeking methods {@link #hasComplete()}, {@link #hasThrowable()},\n * {@link #getThrowable()} and {@link #hasObservers()} as well as means to read the very last observed value -\n * after this {@code AsyncSubject} has been completed - in a non-blocking and thread-safe\n * manner via {@link #hasValue()} or {@link #getValue()}.\n * <dl>\n *  <dt><b>Scheduler:</b></dt>\n *  <dd>{@code AsyncSubject} does not operate by default on a particular {@link io.reactivex.rxjava4.core.Scheduler} and\n *  the {@code Observer}s get notified on the thread where the terminating {@code onError} or {@code onComplete}\n *  methods were invoked.</dd>\n *  <dt><b>Error handling:</b></dt>\n *  <dd>When the {@link #onError(Throwable)} is called, the {@code AsyncSubject} enters into a terminal state\n *  and emits the same {@code Throwable} instance to the last set of {@code Observer}s. During this emission,\n *  if one or more {@code Observer}s dispose their respective {@code Disposable}s, the\n *  {@code Throwable} is delivered to the global error handler via\n *  {@link io.reactivex.rxjava4.plugins.RxJavaPlugins#onError(Throwable)} (multiple times if multiple {@code Observer}s\n *  cancel at once).\n *  If there were no {@code Observer}s subscribed to this {@code AsyncSubject} when the {@code onError()}\n *  was called, the global error handler is not invoked.\n *  </dd>\n * </dl>\n * <p>\n * Example usage:\n * <pre><code>\n * AsyncSubject&lt;Object&gt; subject = AsyncSubject.create();\n * \n * TestObserver&lt;Object&gt; to1 = subject.test();\n *\n * to1.assertEmpty();\n *\n * subject.onNext(1);\n *\n * // AsyncSubject only emits when onComplete was called.\n * to1.assertEmpty();\n *\n * subject.onNext(2);\n * subject.onComplete();\n *\n * // onComplete triggers the emission of the last cached item and the onComplete event.\n * to1.assertResult(2);\n *\n * TestObserver&lt;Object&gt; to2 = subject.test();\n *\n * // late Observers receive the last cached item too\n * to2.assertResult(2);\n * </code></pre>\n * @param <T> the value type\n */',
            '/**\n * Subject：onComplete 时向 Observer 发射最后一项，或 onError 时发射错误。\n * <p>\n * <img width="640" height="239" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/AsyncSubject.png" alt="">\n * <p>\n * 通过 {@link #create()} 创建；onNext/onError 禁止 null（Reactive Streams 规则 2.13）。\n * <p>\n * 作为 {@link io.reactivex.rxjava4.core.Observable} 不支持背压；内部只缓存最新一项，仅在 onComplete 时发射，\n * 不宜用于无限或永不完成的源。onError 会清除最后一项，晚到 Observer 仅收到 onError。\n * <p>\n * onSubscribe 在独立作源时非必需（规则 2.12）；终止后调用会立即 dispose Disposable。\n * onNext/onError/onComplete 须串行调用，可用 {@link #toSerialized()} 防重入。\n * <p>\n * 支持 hasComplete/hasThrowable/getValue 等状态查询；默认不在特定 {@link io.reactivex.rxjava4.core.Scheduler} 上运行。\n * onError 时向当前 Observer 集合同一 Throwable 实例；取消订阅可能导致 RxJavaPlugins.onError 多次调用。\n * <dl>\n *  <dt><b>Scheduler:</b></dt>\n *  <dd>终止 onError/onComplete 所在线程通知 Observer。</dd>\n *  <dt><b>Error handling:</b></dt>\n *  <dd>无 Observer 时 onError 不触发全局错误处理器。</dd>\n * </dl>\n * @param <T> 值类型\n */',
        ),
        (
            '/** Write before updating subscribers, read after reading subscribers as TERMINATED. */',
            '/** 在 subscribers 置 TERMINATED 前写入，读后可见。 */',
        ),
        (
            '/** Write before updating subscribers, read after reading subscribers as TERMINATED. */',
            '/** 在 subscribers 置 TERMINATED 前写入，读后可见。 */',
        ),
        (
            '/**\n     * Creates a new AsyncProcessor.\n     * @param <T> the value type to be received and emitted\n     * @return the new AsyncProcessor instance\n     */',
            '/**\n     * 创建新的 AsyncSubject。\n     * @param <T> 接收与发射的值类型\n     * @return 新的 AsyncSubject 实例\n     */',
        ),
        (
            '/**\n     * Constructs an AsyncSubject.\n     * @since 2.0\n     */',
            '/**\n     * 构造 AsyncSubject。\n     * @since 2.0\n     */',
        ),
        (
            '/**\n     * Tries to add the given subscriber to the subscribers array atomically\n     * or returns false if the subject has terminated.\n     * @param ps the subscriber to add\n     * @return true if successful, false if the subject has terminated\n     */',
            '/**\n     * 尝试将给定订阅者原子加入 subscribers 数组；subject 已终止则返回 false。\n     * @param ps 要添加的订阅者\n     * @return 成功为 true，已终止为 false\n     */',
        ),
        (
            '/**\n     * Atomically removes the given subscriber if it is subscribed to the subject.\n     * @param ps the subject to remove\n     */',
            '/**\n     * 若已订阅则从 subject 原子移除给定订阅者。\n     * @param ps 要移除的订阅者\n     */',
        ),
        (
            '/**\n     * Returns true if the subject has any value.\n     * <p>The method is thread-safe.\n     * @return true if the subject has any value\n     */',
            '/**\n     * 若 subject 持有任意值则返回 true。\n     * <p>本方法线程安全。\n     * @return 有值时为 true\n     */',
        ),
        (
            '/**\n     * Returns a single value the Subject currently has or null if no such value exists.\n     * <p>The method is thread-safe.\n     * @return a single value the Subject currently has or null if no such value exists\n     */',
            '/**\n     * 返回 Subject 当前持有的单一值；无值时返回 null。\n     * <p>本方法线程安全。\n     * @return 当前值或 null\n     */',
        ),
    ],
    "src/main/java/io/reactivex/rxjava4/subjects/CompletableSubject.java": [
        (
            '/**\n * Represents a hot Completable-like source and consumer of events similar to Subjects.\n * <p>\n * <img width="640" height="243" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/CompletableSubject.png" alt="">\n * <p>\n * This subject does not have a public constructor by design; a new non-terminated instance of this\n * {@code CompletableSubject} can be created via the {@link #create()} method.\n * <p>\n * Since the {@code CompletableSubject} is conceptionally derived from the {@code Processor} type in the Reactive Streams specification,\n * {@code null}s are not allowed (<a href="https://github.com/reactive-streams/reactive-streams-jvm#2.13">Rule 2.13</a>)\n * as parameters to {@link #onError(Throwable)}.\n * <p>\n * Even though {@code CompletableSubject} implements the {@code CompletableObserver} interface, calling\n * {@code onSubscribe} is not required (<a href="https://github.com/reactive-streams/reactive-streams-jvm#2.12">Rule 2.12</a>)\n * if the subject is used as a standalone source. However, calling {@code onSubscribe}\n * after the {@code CompletableSubject} reached its terminal state will result in the\n * given {@code Disposable} being disposed immediately.\n * <p>\n * All methods are thread safe. Calling {@link #onComplete()} multiple\n * times has no effect. Calling {@link #onError(Throwable)} multiple times relays the {@code Throwable} to\n * the {@link io.reactivex.rxjava4.plugins.RxJavaPlugins#onError(Throwable)} global error handler.\n * <p>\n * This {@code CompletableSubject} supports the standard state-peeking methods {@link #hasComplete()},\n * {@link #hasThrowable()}, {@link #getThrowable()} and {@link #hasObservers()}.\n * <dl>\n *  <dt><b>Scheduler:</b></dt>\n *  <dd>{@code CompletableSubject} does not operate by default on a particular {@link io.reactivex.rxjava4.core.Scheduler} and\n *  the {@code CompletableObserver}s get notified on the thread where the terminating {@code onError} or {@code onComplete}\n *  methods were invoked.</dd>\n *  <dt><b>Error handling:</b></dt>\n *  <dd>When the {@link #onError(Throwable)} is called, the {@code CompletableSubject} enters into a terminal state\n *  and emits the same {@code Throwable} instance to the last set of {@code CompletableObserver}s. During this emission,\n *  if one or more {@code CompletableObserver}s dispose their respective {@code Disposable}s, the\n *  {@code Throwable} is delivered to the global error handler via\n *  {@link io.reactivex.rxjava4.plugins.RxJavaPlugins#onError(Throwable)} (multiple times if multiple {@code CompletableObserver}s\n *  cancel at once).\n *  If there were no {@code CompletableObserver}s subscribed to this {@code CompletableSubject} when the {@code onError()}\n *  was called, the global error handler is not invoked.\n *  </dd>\n * </dl>\n * <p>\n * Example usage:\n * <pre><code>\n * CompletableSubject subject = CompletableSubject.create();\n *\n * TestObserver&lt;Void&gt; to1 = subject.test();\n *\n * // a fresh CompletableSubject is empty\n * to1.assertEmpty();\n *\n * subject.onComplete();\n *\n * // a CompletableSubject is always void of items\n * to1.assertResult();\n *\n * TestObserver&lt;Void&gt; to2 = subject.test()\n *\n * // late CompletableObservers receive the terminal event\n * to2.assertResult();\n * </code></pre>\n * <p>History: 2.0.5 - experimental\n * @since 2.1\n */',
            '/**\n * 类似 Subject 的热 Completable 式事件源与消费者。\n * <p>\n * <img width="640" height="243" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/CompletableSubject.png" alt="">\n * <p>\n * 通过 {@link #create()} 创建；{@link #onError(Throwable)} 禁止 null。\n * onSubscribe 独立作源时非必需；终止后调用会立即 dispose Disposable。\n * 所有方法线程安全；重复 onComplete 无效果，重复 onError 交由 RxJavaPlugins 处理。\n * <p>\n * 支持 hasComplete/hasThrowable/getThrowable/hasObservers；默认不在特定 {@link io.reactivex.rxjava4.core.Scheduler} 上运行。\n * <dl>\n *  <dt><b>Scheduler:</b></dt>\n *  <dd>终止事件在调用线程通知 CompletableObserver。</dd>\n *  <dt><b>Error handling:</b></dt>\n *  <dd>无 Observer 时 onError 不触发全局错误处理器。</dd>\n * </dl>\n * <p>History: 2.0.5 - experimental\n * @since 2.1\n */',
        ),
        (
            '/**\n     * Creates a fresh CompletableSubject.\n     * @return the new CompletableSubject instance\n     */',
            '/**\n     * 创建新的 CompletableSubject。\n     * @return 新的 CompletableSubject 实例\n     */',
        ),
        (
            '/**\n     * Returns the terminal error if this CompletableSubject has been terminated with an error, null otherwise.\n     * @return the terminal error or null if not terminated or not with an error\n     */',
            '/**\n     * 若 CompletableSubject 以 error 终止则返回该错误，否则 null。\n     * @return 终止错误或 null\n     */',
        ),
        (
            '/**\n     * Returns true if this CompletableSubject has been terminated with an error.\n     * @return true if this CompletableSubject has been terminated with an error\n     */',
            '/**\n     * 若 CompletableSubject 以 error 终止则返回 true。\n     * @return 以 error 终止时为 true\n     */',
        ),
        (
            '/**\n     * Returns true if this CompletableSubject has been completed.\n     * @return true if this CompletableSubject has been completed\n     */',
            '/**\n     * 若 CompletableSubject 已完成则返回 true。\n     * @return 已完成时为 true\n     */',
        ),
        (
            '/**\n     * Returns true if this CompletableSubject has observers.\n     * @return true if this CompletableSubject has observers\n     */',
            '/**\n     * 若 CompletableSubject 有 Observer 则返回 true。\n     * @return 有 Observer 时为 true\n     */',
        ),
        (
            '/**\n     * Returns the number of current observers.\n     * @return the number of current observers\n     */',
            '/**\n     * 返回当前 Observer 数量。\n     * @return 当前 Observer 数量\n     */',
        ),
    ],
    "src/main/java/io/reactivex/rxjava4/subjects/MaybeSubject.java": [
        (
            '/**\n * Represents a hot Maybe-like source and consumer of events similar to Subjects.\n * <p>\n * <img width="640" height="164" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/MaybeSubject.png" alt="">\n * <p>\n * This subject does not have a public constructor by design; a new non-terminated instance of this\n * {@code MaybeSubject} can be created via the {@link #create()} method.\n * <p>\n * Since the {@code MaybeSubject} is conceptionally derived from the {@code Processor} type in the Reactive Streams specification,\n * {@code null}s are not allowed (<a href="https://github.com/reactive-streams/reactive-streams-jvm#2.13">Rule 2.13</a>)\n * as parameters to  {@link #onSuccess(Object)} and {@link #onError(Throwable)}. Such calls will result in a\n * {@link NullPointerException} being thrown and the subject\'s state is not changed.\n * <p>\n * Since a {@code MaybeSubject} is a {@link io.reactivex.rxjava4.core.Maybe}, calling {@code onSuccess}, {@code onError}\n * or {@code onComplete} will move this {@code MaybeSubject} into its terminal state atomically.\n * <p>\n * All methods are thread safe. Calling {@link #onSuccess(Object)} or {@link #onComplete()} multiple\n * times has no effect. Calling {@link #onError(Throwable)} multiple times relays the {@code Throwable} to\n * the {@link io.reactivex.rxjava4.plugins.RxJavaPlugins#onError(Throwable)} global error handler.\n * <p>\n * Even though {@code MaybeSubject} implements the {@code MaybeObserver} interface, calling\n * {@code onSubscribe} is not required (<a href="https://github.com/reactive-streams/reactive-streams-jvm#2.12">Rule 2.12</a>)\n * if the subject is used as a standalone source. However, calling {@code onSubscribe}\n * after the {@code MaybeSubject} reached its terminal state will result in the\n * given {@code Disposable} being disposed immediately.\n * <p>\n * This {@code MaybeSubject} supports the standard state-peeking methods {@link #hasComplete()}, {@link #hasThrowable()},\n * {@link #getThrowable()} and {@link #hasObservers()} as well as means to read any success item in a non-blocking\n * and thread-safe manner via {@link #hasValue()} and {@link #getValue()}.\n * <p>\n * The {@code MaybeSubject} does not support clearing its cached {@code onSuccess} value.\n * <dl>\n *  <dt><b>Scheduler:</b></dt>\n *  <dd>{@code MaybeSubject} does not operate by default on a particular {@link io.reactivex.rxjava4.core.Scheduler} and\n *  the {@code MaybeObserver}s get notified on the thread where the terminating {@code onSuccess}, {@code onError} or {@code onComplete}\n *  methods were invoked.</dd>\n *  <dt><b>Error handling:</b></dt>\n *  <dd>When the {@link #onError(Throwable)} is called, the {@code MaybeSubject} enters into a terminal state\n *  and emits the same {@code Throwable} instance to the last set of {@code MaybeObserver}s. During this emission,\n *  if one or more {@code MaybeObserver}s dispose their respective {@code Disposable}s, the\n *  {@code Throwable} is delivered to the global error handler via\n *  {@link io.reactivex.rxjava4.plugins.RxJavaPlugins#onError(Throwable)} (multiple times if multiple {@code MaybeObserver}s\n *  cancel at once).\n *  If there were no {@code MaybeObserver}s subscribed to this {@code MaybeSubject} when the {@code onError()}\n *  was called, the global error handler is not invoked.\n *  </dd>\n * </dl>\n * <p>\n * Example usage:\n * <pre><code>\n * MaybeSubject&lt;Integer&gt; subject1 = MaybeSubject.create();\n *\n * TestObserver&lt;Integer&gt; to1 = subject1.test();\n *\n * // MaybeSubjects are empty by default\n * to1.assertEmpty();\n *\n * subject1.onSuccess(1);\n *\n * // onSuccess is a terminal event with MaybeSubjects\n * // TestObserver converts onSuccess into onNext + onComplete\n * to1.assertResult(1);\n *\n * TestObserver&lt;Integer&gt; to2 = subject1.test();\n *\n * // late Observers receive the terminal signal (onSuccess) too\n * to2.assertResult(1);\n *\n * // -----------------------------------------------------\n *\n * MaybeSubject&lt;Integer&gt; subject2 = MaybeSubject.create();\n *\n * TestObserver&lt;Integer&gt; to3 = subject2.test();\n *\n * subject2.onComplete();\n *\n * // a completed MaybeSubject completes its MaybeObservers\n * to3.assertResult();\n *\n * TestObserver&lt;Integer&gt; to4 = subject1.test();\n *\n * // late Observers receive the terminal signal (onComplete) too\n * to4.assertResult();\n * </code></pre>\n * <p>History: 2.0.5 - experimental\n * @param <T> the value type received and emitted\n * @since 2.1\n */',
            '/**\n * 类似 Subject 的热 Maybe 式事件源与消费者。\n * <p>\n * <img width="640" height="164" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/MaybeSubject.png" alt="">\n * <p>\n * 通过 {@link #create()} 创建；onSuccess/onError 禁止 null。\n * onSuccess/onError/onComplete 任一调用即原子进入终止态；方法均线程安全。\n * 不支持清除已缓存的 onSuccess 值；支持 hasValue/getValue 等状态查询。\n * <dl>\n *  <dt><b>Scheduler:</b></dt>\n *  <dd>终止信号在调用线程通知 MaybeObserver。</dd>\n *  <dt><b>Error handling:</b></dt>\n *  <dd>无 Observer 时 onError 不触发全局错误处理器。</dd>\n * </dl>\n * <p>History: 2.0.5 - experimental\n * @param <T> 接收与发射的值类型\n * @since 2.1\n */',
        ),
        (
            '/**\n     * Creates a fresh MaybeSubject.\n     * @param <T> the value type received and emitted\n     * @return the new MaybeSubject instance\n     */',
            '/**\n     * 创建新的 MaybeSubject。\n     * @param <T> 接收与发射的值类型\n     * @return 新的 MaybeSubject 实例\n     */',
        ),
        (
            '/**\n     * Returns the success value if this MaybeSubject was terminated with a success value.\n     * @return the success value or null\n     */',
            '/**\n     * 若 MaybeSubject 以 success 终止则返回成功值。\n     * @return 成功值或 null\n     */',
        ),
        (
            '/**\n     * Returns true if this MaybeSubject was terminated with a success value.\n     * @return true if this MaybeSubject was terminated with a success value\n     */',
            '/**\n     * 若 MaybeSubject 以 success 终止则返回 true。\n     * @return 以 success 终止时为 true\n     */',
        ),
        (
            '/**\n     * Returns the terminal error if this MaybeSubject has been terminated with an error, null otherwise.\n     * @return the terminal error or null if not terminated or not with an error\n     */',
            '/**\n     * 若 MaybeSubject 以 error 终止则返回该错误，否则 null。\n     * @return 终止错误或 null\n     */',
        ),
        (
            '/**\n     * Returns true if this MaybeSubject has been terminated with an error.\n     * @return true if this MaybeSubject has been terminated with an error\n     */',
            '/**\n     * 若 MaybeSubject 以 error 终止则返回 true。\n     * @return 以 error 终止时为 true\n     */',
        ),
        (
            '/**\n     * Returns true if this MaybeSubject has been completed.\n     * @return true if this MaybeSubject has been completed\n     */',
            '/**\n     * 若 MaybeSubject 已完成（无 success 无 error）则返回 true。\n     * @return 已完成时为 true\n     */',
        ),
        (
            '/**\n     * Returns true if this MaybeSubject has observers.\n     * @return true if this MaybeSubject has observers\n     */',
            '/**\n     * 若 MaybeSubject 有 Observer 则返回 true。\n     * @return 有 Observer 时为 true\n     */',
        ),
        (
            '/**\n     * Returns the number of current observers.\n     * @return the number of current observers\n     */',
            '/**\n     * 返回当前 Observer 数量。\n     * @return 当前 Observer 数量\n     */',
        ),
    ],
    "src/main/java/io/reactivex/rxjava4/subjects/PublishSubject.java": [
        (
            '/**\n * A Subject that emits (multicasts) items to currently subscribed {@link Observer}s and terminal events to current\n * or late {@code Observer}s.\n * <p>\n * <img width="640" height="281" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/PublishSubject.png" alt="">\n * <p>\n * This subject does not have a public constructor by design; a new empty instance of this\n * {@code PublishSubject} can be created via the {@link #create()} method.\n * <p>\n * Since a {@code Subject} is conceptionally derived from the {@code Processor} type in the Reactive Streams specification,\n * {@code null}s are not allowed (<a href="https://github.com/reactive-streams/reactive-streams-jvm#2.13">Rule 2.13</a>) as\n * parameters to {@link #onNext(Object)} and {@link #onError(Throwable)}. Such calls will result in a\n * {@link NullPointerException} being thrown and the subject\'s state is not changed.\n * <p>\n * Since a {@code PublishSubject} is an {@link io.reactivex.rxjava4.core.Observable}, it does not support backpressure.\n * <p>\n * When this {@code PublishSubject} is terminated via {@link #onError(Throwable)} or {@link #onComplete()},\n * late {@link io.reactivex.rxjava4.core.Observer}s only receive the respective terminal event.\n * <p>\n * Unlike a {@link BehaviorSubject}, a {@code PublishSubject} doesn\'t retain/cache items, therefore, a new\n * {@code Observer} won\'t receive any past items.\n * <p>\n * Even though {@code PublishSubject} implements the {@code Observer} interface, calling\n * {@code onSubscribe} is not required (<a href="https://github.com/reactive-streams/reactive-streams-jvm#2.12">Rule 2.12</a>)\n * if the subject is used as a standalone source. However, calling {@code onSubscribe}\n * after the {@code PublishSubject} reached its terminal state will result in the\n * given {@code Disposable} being disposed immediately.\n * <p>\n * Calling {@link #onNext(Object)}, {@link #onError(Throwable)} and {@link #onComplete()}\n * is required to be serialized (called from the same thread or called non-overlappingly from different threads\n * through external means of serialization). The {@link #toSerialized()} method available to all {@code Subject}s\n * provides such serialization and also protects against reentrance (i.e., when a downstream {@code Observer}\n * consuming this subject also wants to call {@link #onNext(Object)} on this subject recursively).\n * <p>\n * This {@code PublishSubject} supports the standard state-peeking methods {@link #hasComplete()}, {@link #hasThrowable()},\n * {@link #getThrowable()} and {@link #hasObservers()}.\n * <dl>\n *  <dt><b>Scheduler:</b></dt>\n *  <dd>{@code PublishSubject} does not operate by default on a particular {@link io.reactivex.rxjava4.core.Scheduler} and\n *  the {@code Observer}s get notified on the thread the respective {@code onXXX} methods were invoked.</dd>\n *  <dt><b>Error handling:</b></dt>\n *  <dd>When the {@link #onError(Throwable)} is called, the {@code PublishSubject} enters into a terminal state\n *  and emits the same {@code Throwable} instance to the last set of {@code Observer}s. During this emission,\n *  if one or more {@code Observer}s dispose their respective {@code Disposable}s, the\n *  {@code Throwable} is delivered to the global error handler via\n *  {@link io.reactivex.rxjava4.plugins.RxJavaPlugins#onError(Throwable)} (multiple times if multiple {@code Observer}s\n *  cancel at once).\n *  If there were no {@code Observer}s subscribed to this {@code PublishSubject} when the {@code onError()}\n *  was called, the global error handler is not invoked.\n *  </dd>\n * </dl>\n * <p>\n * Example usage:\n * <pre> {@code\n\n    PublishSubject<Object> subject = PublishSubject.create();\n    // observer1 will receive all onNext and onComplete events\n    subject.subscribe(observer1);\n    subject.onNext("one");\n    subject.onNext("two");\n    // observer2 will only receive "three" and onComplete\n    subject.subscribe(observer2);\n    subject.onNext("three");\n    subject.onComplete();\n\n    // late Observers only receive the terminal event\n    subject.test().assertEmpty();\n    } </pre>\n *\n * @param <T>\n *          the type of items observed and emitted by the Subject\n */',
            '/**\n * Subject：向当前订阅的 {@link Observer} 多播项，向当前或晚到 Observer 发送终止事件。\n * <p>\n * <img width="640" height="281" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/PublishSubject.png" alt="">\n * <p>\n * 通过 {@link #create()} 创建；onNext/onError 禁止 null；作为 Observable 不支持背压，不缓存历史项。\n * 终止后晚到 Observer 仅收到对应终止事件。onXXX 须串行，可用 {@link #toSerialized()}。\n * <dl>\n *  <dt><b>Scheduler:</b></dt>\n *  <dd>在各自 onXXX 调用线程通知 Observer。</dd>\n *  <dt><b>Error handling:</b></dt>\n *  <dd>无 Observer 时 onError 不触发全局错误处理器。</dd>\n * </dl>\n *\n * @param <T> Subject 观察与发射的项类型\n */',
        ),
        (
            '/** The terminated indicator for the subscribers array. */',
            '/** subscribers 数组的已终止标记。 */',
        ),
        (
            '/** An empty subscribers array to avoid allocating it all the time. */',
            '/** 空 subscribers 数组，避免重复分配。 */',
        ),
        (
            '/** The array of currently subscribed subscribers. */',
            '/** 当前已订阅的 subscribers 数组。 */',
        ),
        (
            '/** The error, write before terminating and read after checking subscribers. */',
            '/** 错误；终止前写入，检查 subscribers 后读取。 */',
        ),
        (
            '/**\n     * Constructs a PublishSubject.\n     * @param <T> the value type\n     * @return the new PublishSubject\n     */',
            '/**\n     * 构造 PublishSubject。\n     * @param <T> 值类型\n     * @return 新的 PublishSubject\n     */',
        ),
        (
            '/**\n     * Constructs a PublishSubject.\n     * @since 2.0\n     */',
            '/**\n     * 构造 PublishSubject。\n     * @since 2.0\n     */',
        ),
        (
            '/**\n     * Tries to add the given subscriber to the subscribers array atomically\n     * or returns false if the subject has terminated.\n     * @param ps the subscriber to add\n     * @return true if successful, false if the subject has terminated\n     */',
            '/**\n     * 尝试将给定订阅者原子加入 subscribers 数组；subject 已终止则返回 false。\n     * @param ps 要添加的订阅者\n     * @return 成功为 true，已终止为 false\n     */',
        ),
        (
            '/**\n     * Atomically removes the given subscriber if it is subscribed to the subject.\n     * @param ps the subject to remove\n     */',
            '/**\n     * 若已订阅则从 subject 原子移除给定订阅者。\n     * @param ps 要移除的订阅者\n     */',
        ),
        (
            '/**\n     * Wraps the actual subscriber, tracks its requests and makes cancellation\n     * to remove itself from the current subscribers array.\n     *\n     * @param <T> the value type\n     */',
            '/**\n     * 包装实际订阅者，跟踪请求，取消时从 subscribers 数组移除自身。\n     *\n     * @param <T> 值类型\n     */',
        ),
        (
            '/** The actual subscriber. */',
            '/** 实际订阅者。 */',
        ),
        (
            '/** The subject state. */',
            '/** subject 状态。 */',
        ),
        (
            '/**\n         * Constructs a PublishSubscriber, wraps the actual subscriber and the state.\n         * @param actual the actual subscriber\n         * @param parent the parent PublishProcessor\n         */',
            '/**\n         * 构造 PublishDisposable，包装实际订阅者与父 subject。\n         * @param actual 实际订阅者\n         * @param parent 父 PublishSubject\n         */',
        ),
    ],
    "src/main/java/io/reactivex/rxjava4/subjects/SerializedSubject.java": [
        (
            '/**\n * Serializes calls to the Observer methods.\n * <p>All other Observable and Subject methods are thread-safe by design.\n *\n * @param <T> the item value type\n */',
            '/**\n * 串行化对 Observer 方法的调用。\n * <p>其余 Observable 与 Subject 方法按设计已为线程安全。\n *\n * @param <T> 元素值类型\n */',
        ),
        (
            '/** The actual subscriber to serialize Subscriber calls to. */',
            '/** 实际接收串行化 Subscriber 调用的 subject。 */',
        ),
        (
            '/** Indicates an emission is going on, guarded by this. */',
            '/** 表示正在发射，由本对象监视器保护。 */',
        ),
        (
            '/** If not null, it holds the missed NotificationLite events. */',
            '/** 非 null 时保存错过的 NotificationLite 事件。 */',
        ),
        (
            '/** Indicates a terminal event has been received and all further events will be dropped. */',
            '/** 已收到终止事件，后续事件将被丢弃。 */',
        ),
        (
            '/**\n     * Constructor that wraps an actual subject.\n     * @param actual the subject wrapped\n     */',
            '/**\n     * 包装实际 subject 的构造器。\n     * @param actual 被包装的 subject\n     */',
        ),
        (
            '/** Loops until all notifications in the queue has been processed. */',
            '/** 循环处理队列中所有通知直至清空。 */',
        ),
    ],
    "src/main/java/io/reactivex/rxjava4/subjects/SingleSubject.java": [
        (
            '/**\n * Represents a hot Single-like source and consumer of events similar to Subjects.\n * <p>\n * <img width="640" height="236" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/SingleSubject.png" alt="">\n * <p>\n * This subject does not have a public constructor by design; a new non-terminated instance of this\n * {@code SingleSubject} can be created via the {@link #create()} method.\n * <p>\n * Since the {@code SingleSubject} is conceptionally derived from the {@code Processor} type in the Reactive Streams specification,\n * {@code null}s are not allowed (<a href="https://github.com/reactive-streams/reactive-streams-jvm#2.13">Rule 2.13</a>)\n * as parameters to  {@link #onSuccess(Object)} and {@link #onError(Throwable)}. Such calls will result in a\n * {@link NullPointerException} being thrown and the subject\'s state is not changed.\n * <p>\n * Since a {@code SingleSubject} is a {@link io.reactivex.rxjava4.core.Single}, calling {@code onSuccess} or {@code onError}\n * will move this {@code SingleSubject} into its terminal state atomically.\n * <p>\n * All methods are thread safe. Calling {@link #onSuccess(Object)} multiple\n * times has no effect. Calling {@link #onError(Throwable)} multiple times relays the {@code Throwable} to\n * the {@link io.reactivex.rxjava4.plugins.RxJavaPlugins#onError(Throwable)} global error handler.\n * <p>\n * Even though {@code SingleSubject} implements the {@code SingleObserver} interface, calling\n * {@code onSubscribe} is not required (<a href="https://github.com/reactive-streams/reactive-streams-jvm#2.12">Rule 2.12</a>)\n * if the subject is used as a standalone source. However, calling {@code onSubscribe}\n * after the {@code SingleSubject} reached its terminal state will result in the\n * given {@code Disposable} being disposed immediately.\n * <p>\n * This {@code SingleSubject} supports the standard state-peeking methods {@link #hasThrowable()},\n * {@link #getThrowable()} and {@link #hasObservers()} as well as means to read any success item in a non-blocking\n * and thread-safe manner via {@link #hasValue()} and {@link #getValue()}.\n * <p>\n * The {@code SingleSubject} does not support clearing its cached {@code onSuccess} value.\n * <dl>\n *  <dt><b>Scheduler:</b></dt>\n *  <dd>{@code SingleSubject} does not operate by default on a particular {@link io.reactivex.rxjava4.core.Scheduler} and\n *  the {@code SingleObserver}s get notified on the thread where the terminating {@code onSuccess} or {@code onError}\n *  methods were invoked.</dd>\n *  <dt><b>Error handling:</b></dt>\n *  <dd>When the {@link #onError(Throwable)} is called, the {@code SingleSubject} enters into a terminal state\n *  and emits the same {@code Throwable} instance to the last set of {@code SingleObserver}s. During this emission,\n *  if one or more {@code SingleObserver}s dispose their respective {@code Disposable}s, the\n *  {@code Throwable} is delivered to the global error handler via\n *  {@link io.reactivex.rxjava4.plugins.RxJavaPlugins#onError(Throwable)} (multiple times if multiple {@code SingleObserver}s\n *  cancel at once).\n *  If there were no {@code SingleObserver}s subscribed to this {@code SingleSubject} when the {@code onError()}\n *  was called, the global error handler is not invoked.\n *  </dd>\n * </dl>\n * <p>\n * Example usage:\n * <pre><code>\n * SingleSubject&lt;Integer&gt; subject1 = SingleSubject.create();\n * \n * TestObserver&lt;Integer&gt; to1 = subject1.test();\n * \n * // SingleSubjects are empty by default\n * to1.assertEmpty();\n * \n * subject1.onSuccess(1);\n * \n * // onSuccess is a terminal event with SingleSubjects\n * // TestObserver converts onSuccess into onNext + onComplete\n * to1.assertResult(1);\n *\n * TestObserver&lt;Integer&gt; to2 = subject1.test();\n * \n * // late Observers receive the terminal signal (onSuccess) too\n * to2.assertResult(1);\n * </code></pre>\n * <p>History: 2.0.5 - experimental\n * @param <T> the value type received and emitted\n * @since 2.1\n */',
            '/**\n * 类似 Subject 的热 Single 式事件源与消费者。\n * <p>\n * <img width="640" height="236" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/SingleSubject.png" alt="">\n * <p>\n * 通过 {@link #create()} 创建；onSuccess/onError 禁止 null；任一调用即原子终止。\n * 方法均线程安全；不支持清除 onSuccess 缓存；支持 hasValue/getValue 等状态查询。\n * <dl>\n *  <dt><b>Scheduler:</b></dt>\n *  <dd>onSuccess/onError 调用线程通知 SingleObserver。</dd>\n *  <dt><b>Error handling:</b></dt>\n *  <dd>无 Observer 时 onError 不触发全局错误处理器。</dd>\n * </dl>\n * <p>History: 2.0.5 - experimental\n * @param <T> 接收与发射的值类型\n * @since 2.1\n */',
        ),
        (
            '/**\n     * Creates a fresh SingleSubject.\n     * @param <T> the value type received and emitted\n     * @return the new SingleSubject instance\n     */',
            '/**\n     * 创建新的 SingleSubject。\n     * @param <T> 接收与发射的值类型\n     * @return 新的 SingleSubject 实例\n     */',
        ),
        (
            '/**\n     * Returns the success value if this SingleSubject was terminated with a success value.\n     * @return the success value or null\n     */',
            '/**\n     * 若 SingleSubject 以 success 终止则返回成功值。\n     * @return 成功值或 null\n     */',
        ),
        (
            '/**\n     * Returns true if this SingleSubject was terminated with a success value.\n     * @return true if this SingleSubject was terminated with a success value\n     */',
            '/**\n     * 若 SingleSubject 以 success 终止则返回 true。\n     * @return 以 success 终止时为 true\n     */',
        ),
        (
            '/**\n     * Returns the terminal error if this SingleSubject has been terminated with an error, null otherwise.\n     * @return the terminal error or null if not terminated or not with an error\n     */',
            '/**\n     * 若 SingleSubject 以 error 终止则返回该错误，否则 null。\n     * @return 终止错误或 null\n     */',
        ),
        (
            '/**\n     * Returns true if this SingleSubject has been terminated with an error.\n     * @return true if this SingleSubject has been terminated with an error\n     */',
            '/**\n     * 若 SingleSubject 以 error 终止则返回 true。\n     * @return 以 error 终止时为 true\n     */',
        ),
        (
            '/**\n     * Returns true if this SingleSubject has observers.\n     * @return true if this SingleSubject has observers\n     */',
            '/**\n     * 若 SingleSubject 有 Observer 则返回 true。\n     * @return 有 Observer 时为 true\n     */',
        ),
        (
            '/**\n     * Returns the number of current observers.\n     * @return the number of current observers\n     */',
            '/**\n     * 返回当前 Observer 数量。\n     * @return 当前 Observer 数量\n     */',
        ),
    ],
    "src/main/java/io/reactivex/rxjava4/subjects/Subject.java": [
        (
            '/**\n * Represents an {@link Observer} and an {@link Observable} at the same time, allowing\n * multicasting events from a single source to multiple child {@code Observer}s.\n * <p>\n * All methods except the {@link #onSubscribe(io.reactivex.rxjava4.disposables.Disposable)}, {@link #onNext(Object)},\n * {@link #onError(Throwable)} and {@link #onComplete()} are thread-safe.\n * Use {@link #toSerialized()} to make these methods thread-safe as well.\n *\n * @param <T> the item value type\n */',
            '/**\n * 同时表示 {@link Observer} 与 {@link Observable}，可将单一源的事件多播给多个子 Observer。\n * <p>\n * 除 {@link #onSubscribe(io.reactivex.rxjava4.disposables.Disposable)}、{@link #onNext(Object)}、\n * {@link #onError(Throwable)}、{@link #onComplete()} 外，其余方法均为线程安全；\n * 可调用 {@link #toSerialized()} 使这些方法也线程安全。\n *\n * @param <T> 元素值类型\n */',
        ),
        (
            '/**\n     * Returns true if the subject has any Observers.\n     * <p>The method is thread-safe.\n     * @return true if the subject has any Observers\n     */',
            '/**\n     * 若 subject 有任意 Observer 则返回 true。\n     * <p>本方法线程安全。\n     * @return 有 Observer 时为 true\n     */',
        ),
        (
            '/**\n     * Returns true if the subject has reached a terminal state through an error event.\n     * <p>The method is thread-safe.\n     * @return true if the subject has reached a terminal state through an error event\n     * @see #getThrowable()\n     * @see #hasComplete()\n     */',
            '/**\n     * 若 subject 已通过 error 事件进入终止状态则返回 true。\n     * <p>本方法线程安全。\n     * @return 因 error 终止时为 true\n     * @see #getThrowable()\n     * @see #hasComplete()\n     */',
        ),
        (
            '/**\n     * Returns true if the subject has reached a terminal state through a complete event.\n     * <p>The method is thread-safe.\n     * @return true if the subject has reached a terminal state through a complete event\n     * @see #hasThrowable()\n     */',
            '/**\n     * 若 subject 已通过 complete 事件进入终止状态则返回 true。\n     * <p>本方法线程安全。\n     * @return 因 complete 终止时为 true\n     * @see #hasThrowable()\n     */',
        ),
        (
            "/**\n     * Returns the error that caused the Subject to terminate or null if the Subject\n     * hasn't terminated yet.\n     * <p>The method is thread-safe.\n     * @return the error that caused the Subject to terminate or null if the Subject\n     * hasn't terminated yet\n     */",
            '/**\n     * 返回导致 Subject 终止的错误；尚未终止时返回 null。\n     * <p>本方法线程安全。\n     * @return 终止错误，或尚未终止时为 null\n     */',
        ),
        (
            '/**\n     * Wraps this Subject and serializes the calls to the onSubscribe, onNext, onError and\n     * onComplete methods, making them thread-safe.\n     * <p>The method is thread-safe.\n     * @return the wrapped and serialized subject\n     */',
            '/**\n     * 包装本 Subject，串行化 onSubscribe、onNext、onError、onComplete 调用，使其线程安全。\n     * <p>本方法线程安全。\n     * @return 包装后的串行化 subject\n     */',
        ),
    ],
}
