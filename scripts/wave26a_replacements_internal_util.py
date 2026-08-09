"""Chinese JavaDoc replacements for RxJava wave26a internal/util classes [0:15]."""

INTERNAL_UTIL_W26A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "AppendOnlyLinkedArrayList.java": [
        (
            "/**\n * A linked-array-list implementation that only supports appending and consumption.\n *\n * @param <T> the value type\n */",
            "/**\n * 仅支持追加与消费的链式数组列表实现。\n *\n * @param <T> 值类型\n */",
        ),
        (
            "    /**\n     * Constructs an empty list with a per-link capacity.\n     * @param capacity the capacity of each link\n     */",
            "    /**\n     * 构造空列表，并指定每个链段的容量。\n     * @param capacity 每个链段的容量\n     */",
        ),
        (
            "    /**\n     * Append a non-null value to the list.\n     * <p>Don't add null to the list!\n     * @param value the value to append\n     */",
            "    /**\n     * 向列表追加非 null 值。\n     * <p>请勿向列表添加 null！\n     * @param value 要追加的值\n     */",
        ),
        (
            "    /**\n     * Set a value as the first element of the list.\n     * @param value the value to set\n     */",
            "    /**\n     * 将值设为列表首元素。\n     * @param value 要设置的值\n     */",
        ),
        (
            "    /**\n     * Predicate interface suppressing the exception.\n     *\n     * @param <T> the value type\n     */",
            "    /**\n     * 不抛出受检异常的谓词接口。\n     *\n     * @param <T> 值类型\n     */",
        ),
        (
            "    /**\n     * Loops over all elements of the array until a null element is encountered or\n     * the given predicate returns true.\n     * @param consumer the consumer of values that returns true if the forEach should terminate\n     */",
            "    /**\n     * 遍历数组元素，直到遇到 null 或给定谓词返回 true。\n     * @param consumer 值消费者；返回 true 时终止 forEach\n     */",
        ),
        (
            "    /**\n     * Interprets the contents as NotificationLite objects and calls\n     * the appropriate Subscriber method.\n     * \n     * @param <U> the target type\n     * @param subscriber the subscriber to emit the events to\n     * @return true if a terminal event has been reached\n     */",
            "    /**\n     * 将内容解释为 NotificationLite 对象并调用相应 Subscriber 方法。\n     * \n     * @param <U> 目标类型\n     * @param subscriber 接收事件的 subscriber\n     * @return 若已到达终止事件则为 true\n     */",
        ),
        (
            "    /**\n     * Interprets the contents as NotificationLite objects and calls\n     * the appropriate Observer method.\n     * \n     * @param <U> the target type\n     * @param observer the observer to emit the events to\n     * @return true if a terminal event has been reached\n     */",
            "    /**\n     * 将内容解释为 NotificationLite 对象并调用相应 Observer 方法。\n     * \n     * @param <U> 目标类型\n     * @param observer 接收事件的 observer\n     * @return 若已到达终止事件则为 true\n     */",
        ),
        (
            "    /**\n     * Loops over all elements of the array until a null element is encountered or\n     * the given predicate returns true.\n     * @param <S> the extra state type\n     * @param state the extra state passed into the consumer\n     * @param consumer the consumer of values that returns true if the forEach should terminate\n     * @throws Throwable if the predicate throws\n     */",
            "    /**\n     * 遍历数组元素，直到遇到 null 或给定谓词返回 true。\n     * @param <S> 额外状态类型\n     * @param state 传入消费者的额外状态\n     * @param consumer 值消费者；返回 true 时终止 forEach\n     * @throws Throwable 谓词抛出时\n     */",
        ),
    ],
    "ArrayListSupplier.java": [
        (
            "public enum ArrayListSupplier implements Supplier<List<Object>>, Function<Object, List<Object>> {",
            "/**\n * 提供空 {@link ArrayList} 的 {@link Supplier} 与 {@link Function} 单例。\n */\npublic enum ArrayListSupplier implements Supplier<List<Object>>, Function<Object, List<Object>> {",
        ),
        (
            "    @SuppressWarnings({ \"unchecked\", \"rawtypes\" })\n    public static <T> Supplier<List<T>> asSupplier() {",
            "    /** 返回创建空 {@link ArrayList} 的 {@link Supplier} 单例。 */\n    @SuppressWarnings({ \"unchecked\", \"rawtypes\" })\n    public static <T> Supplier<List<T>> asSupplier() {",
        ),
        (
            "    @SuppressWarnings({ \"unchecked\", \"rawtypes\" })\n    public static <T, O> Function<O, List<T>> asFunction() {",
            "    /** 返回忽略输入并创建空 {@link ArrayList} 的 {@link Function} 单例。 */\n    @SuppressWarnings({ \"unchecked\", \"rawtypes\" })\n    public static <T, O> Function<O, List<T>> asFunction() {",
        ),
        (
            "    @Override\n    public List<Object> get() {",
            "    /** @return 新的空 {@link ArrayList} */\n    @Override\n    public List<Object> get() {",
        ),
        (
            "    @Override public List<Object> apply(Object o) {",
            "    /** @return 新的空 {@link ArrayList}（忽略参数） */\n    @Override public List<Object> apply(Object o) {",
        ),
    ],
    "AtomicThrowable.java": [
        (
            "/**\n * Atomic container for {@link Throwable}s including combining and having a\n * terminal state via ExceptionHelper.\n * <p>\n * Watch out for the leaked AtomicReference methods!\n */",
            "/**\n * {@link Throwable} 的原子容器，支持合并异常并通过 ExceptionHelper 进入终止态。\n * <p>\n * 注意：AtomicReference 的公开方法可能泄漏内部状态！\n */",
        ),
        (
            "    /**\n     * Atomically adds a Throwable to this container (combining with a previous Throwable is necessary).\n     * @param t the throwable to add\n     * @return true if successful, false if the container has been terminated\n     */",
            "    /**\n     * 原子地向容器添加 Throwable（若已有异常则合并）。\n     * @param t 要添加的异常\n     * @return 成功为 true；容器已终止为 false\n     */",
        ),
        (
            "    /**\n     * Atomically adds a Throwable to this container (combining with a previous Throwable is necessary)\n     * or reports the error the global error handler and no changes are made.\n     * @param t the throwable to add\n     * @return true if successful, false if the container has been terminated\n     */",
            "    /**\n     * 原子添加 Throwable，或在容器已终止时将错误上报全局处理器且不修改容器。\n     * @param t 要添加的异常\n     * @return 成功为 true；容器已终止为 false\n     */",
        ),
        (
            "    /**\n     * Atomically terminate the container and return the contents of the last\n     * non-terminal Throwable of it.\n     * @return the last Throwable\n     */",
            "    /**\n     * 原子终止容器并返回其中最后一个非终止 Throwable。\n     * @return 最后的 Throwable\n     */",
        ),
        (
            "    /**\n     * Tries to terminate this atomic throwable (by swapping in the TERMINATED indicator)\n     * and calls {@link RxJavaPlugins#onError(Throwable)} if there was a non-null, non-indicator\n     * exception contained within before.\n     * @since 3.0.0\n     */",
            "    /**\n     * 尝试终止本原子 Throwable（写入 TERMINATED 标记），\n     * 若此前存有非 null 且非标记异常则调用 {@link RxJavaPlugins#onError(Throwable)}。\n     * @since 3.0.0\n     */",
        ),
        (
            "    /**\n     * Tries to terminate this atomic throwable (by swapping in the TERMINATED indicator)\n     * and notifies the consumer if there was no error (onComplete) or there was a\n     * non-null, non-indicator exception contained before (onError).\n     * If there was a terminated indicator, the consumer is not signaled.\n     * @param consumer the consumer to notify\n     */",
            "    /**\n     * 尝试终止本原子 Throwable 并通知 consumer：\n     * 无错误时 onComplete，有非标记异常时 onError；已终止则不通知。\n     * @param consumer 要通知的 consumer\n     */",
        ),
        (
            "    public void tryTerminateConsumer(Observer<?> consumer) {",
            "    /** 终止并通知 {@link Observer}（逻辑同 Subscriber 版本）。 */\n    public void tryTerminateConsumer(Observer<?> consumer) {",
        ),
        (
            "    public void tryTerminateConsumer(MaybeObserver<?> consumer) {",
            "    /** 终止并通知 {@link MaybeObserver}。 */\n    public void tryTerminateConsumer(MaybeObserver<?> consumer) {",
        ),
        (
            "    /**\n     * Tries to terminate this atomic throwable (by swapping in the TERMINATED indicator)\n     * and notifies the consumer if there was a\n     * non-null, non-indicator exception contained before (onError).\n     * If there was a terminated indicator, the consumer is not signaled.\n     * @param consumer the consumer to notify\n     */",
            "    /**\n     * 尝试终止并通知 consumer：仅在有非标记异常时 onError；已终止则不通知。\n     * @param consumer 要通知的 consumer\n     */",
        ),
        (
            "    public void tryTerminateConsumer(CompletableObserver consumer) {",
            "    /** 终止并通知 {@link CompletableObserver}。 */\n    public void tryTerminateConsumer(CompletableObserver consumer) {",
        ),
        (
            "    public void tryTerminateConsumer(Emitter<?> consumer) {",
            "    /** 终止并通知 {@link Emitter}。 */\n    public void tryTerminateConsumer(Emitter<?> consumer) {",
        ),
    ],
    "BackpressureHelper.java": [
        (
            "/**\n * Utility class to help with backpressure-related operations such as request aggregation.\n */",
            "/**\n * 背压相关工具类，用于请求聚合等操作。\n */",
        ),
        (
            "    /** Utility class. */",
            "    /** 工具类，禁止实例化。 */",
        ),
        (
            "    /**\n     * Adds two long values and caps the sum at {@link Long#MAX_VALUE}.\n     * @param aValue the first value\n     * @param bValue the second value\n     * @return the sum capped at {@link Long#MAX_VALUE}\n     */",
            "    /**\n     * 两 long 相加，结果上限为 {@link Long#MAX_VALUE}。\n     * @param aValue 第一个值\n     * @param bValue 第二个值\n     * @return 上限截断后的和\n     */",
        ),
        (
            "    /**\n     * Multiplies two long values and caps the product at {@link Long#MAX_VALUE}.\n     * @param aValue the first value\n     * @param bValue the second value\n     * @return the product capped at {@link Long#MAX_VALUE}\n     */",
            "    /**\n     * 两 long 相乘，结果上限为 {@link Long#MAX_VALUE}。\n     * @param aValue 第一个值\n     * @param bValue 第二个值\n     * @return 上限截断后的积\n     */",
        ),
        (
            "    /**\n     * Atomically adds the positive value n to the requested value in the {@link AtomicLong} and\n     * caps the result at {@link Long#MAX_VALUE} and returns the previous value.\n     * @param requested the {@code AtomicLong} holding the current requested value\n     * @param n the value to add, must be positive (not verified)\n     * @return the original value before the add\n     */",
            "    /**\n     * 原子地将正数 n 加到 {@link AtomicLong} 请求量上，结果上限 {@link Long#MAX_VALUE}，返回加之前的值。\n     * @param requested 持有当前请求量的 {@code AtomicLong}\n     * @param n 要加的值，应为正数（未校验）\n     * @return 加法前的原值\n     */",
        ),
        (
            "    /**\n     * Atomically adds the positive value n to the requested value in the {@link AtomicLong} and\n     * caps the result at {@link Long#MAX_VALUE} and returns the previous value and\n     * considers {@link Long#MIN_VALUE} as a cancel indication (no addition then).\n     * @param requested the {@code AtomicLong} holding the current requested value\n     * @param n the value to add, must be positive (not verified)\n     * @return the original value before the add\n     */",
            "    /**\n     * 原子加请求量，将 {@link Long#MIN_VALUE} 视为取消标记（不再累加）。\n     * @param requested 持有当前请求量的 {@code AtomicLong}\n     * @param n 要加的值，应为正数（未校验）\n     * @return 加法前的原值\n     */",
        ),
        (
            "    /**\n     * Atomically subtract the given number (positive, not validated) from the target field unless it contains {@link Long#MAX_VALUE}.\n     * @param requested the target field holding the current requested amount\n     * @param n the produced element count, positive (not validated)\n     * @return the new amount\n     */",
            "    /**\n     * 原子地从目标字段减去已生产数量（除非当前为 {@link Long#MAX_VALUE}）。\n     * @param requested 持有当前请求量的目标字段\n     * @param n 已生产元素数，应为正数（未校验）\n     * @return 更新后的请求量\n     */",
        ),
        (
            "    /**\n     * Atomically subtract the given number (positive, not validated) from the target field if\n     * it doesn't contain {@link Long#MIN_VALUE} (indicating some cancelled state) or {@link Long#MAX_VALUE} (unbounded mode).\n     * @param requested the target field holding the current requested amount\n     * @param n the produced element count, positive (not validated)\n     * @return the new amount\n     */",
            "    /**\n     * 原子减已生产数量；若当前为 {@link Long#MIN_VALUE}（已取消）或 {@link Long#MAX_VALUE}（无界）则不修改。\n     * @param requested 持有当前请求量的目标字段\n     * @param n 已生产元素数，应为正数（未校验）\n     * @return 更新后的请求量\n     */",
        ),
    ],
    "BlockingHelper.java": [
        (
            "/**\n * Utility methods for helping common blocking operations.\n */",
            "/**\n * 常见阻塞操作的辅助工具方法。\n */",
        ),
        (
            "    /** Utility class. */",
            "    /** 工具类，禁止实例化。 */",
        ),
        (
            "    public static void awaitForComplete(CountDownLatch latch, Disposable subscription) {",
            "    /**\n     * 阻塞等待 latch 完成；若已同步完成则直接返回。\n     * 中断时 dispose 订阅并重新设置中断标志。\n     * @param latch 完成信号 latch\n     * @param subscription 中断时要 dispose 的订阅\n     */\n    public static void awaitForComplete(CountDownLatch latch, Disposable subscription) {",
        ),
        (
            "            // Synchronous observable completes before awaiting for it.\n            // Skip await so InterruptedException will never be thrown.",
            "            // 同步 Observable 在 await 前已完成，跳过等待以免抛出 InterruptedException",
        ),
        (
            "        // block until the subscription completes and then return",
            "        // 阻塞直到订阅完成",
        ),
        (
            "            // set the interrupted flag again so callers can still get it\n            // for more information see https://github.com/ReactiveX/RxJava/pull/147#issuecomment-13624780",
            "            // 重新设置中断标志，便于调用方感知",
        ),
        (
            "            // using Runtime so it is not checked",
            "            // 使用 RuntimeException 以免受检",
        ),
        (
            "    /**\n     * Checks if the {@code failOnNonBlockingScheduler} plugin setting is enabled and the current\n     * thread is a Scheduler sensitive to blocking operators.\n     * @throws IllegalStateException if the {@code failOnNonBlockingScheduler} and the current thread is sensitive to blocking\n     */",
            "    /**\n     * 检查 {@code failOnNonBlockingScheduler} 插件是否启用且当前线程属于不支持阻塞的 Scheduler。\n     * @throws IllegalStateException 在不支持阻塞的 Scheduler 线程上尝试阻塞时\n     */",
        ),
    ],
    "BlockingIgnoringReceiver.java": [
        (
            "/**\n * Stores an incoming Throwable (if any) and counts itself down.\n */",
            "/**\n * 保存收到的 Throwable（若有）并 countDown 自身。\n */",
        ),
        (
            "    public Throwable error;",
            "    /** 收到的错误，若无则为 null。 */\n    public Throwable error;",
        ),
        (
            "    public BlockingIgnoringReceiver() {",
            "    /** 构造 count 为 1 的接收器。 */\n    public BlockingIgnoringReceiver() {",
        ),
        (
            "    @Override\n    public void accept(Throwable e) {",
            "    /** 保存异常并 countDown。 */\n    @Override\n    public void accept(Throwable e) {",
        ),
        (
            "    @Override\n    public void run() {",
            "    /** 正常完成时 countDown。 */\n    @Override\n    public void run() {",
        ),
    ],
    "ConnectConsumer.java": [
        (
            "/**\n * Store the Disposable received from the connection.\n */",
            "/**\n * 保存 connect 回调收到的 {@link Disposable}。\n */",
        ),
        (
            "    public Disposable disposable;",
            "    /** connect 时收到的 Disposable。 */\n    public Disposable disposable;",
        ),
        (
            "    @Override\n    public void accept(Disposable t) {",
            "    /** 保存传入的 Disposable。 */\n    @Override\n    public void accept(Disposable t) {",
        ),
    ],
    "EmptyComponent.java": [
        (
            "/**\n * Singleton implementing many interfaces as empty.\n */",
            "/**\n * 以空实现同时充当多种接口的单例占位组件。\n */",
        ),
        (
            "    @SuppressWarnings(\"unchecked\")\n    public static <T> Subscriber<T> asSubscriber() {",
            "    /** @return 作为 {@link Subscriber} 使用的单例 */\n    @SuppressWarnings(\"unchecked\")\n    public static <T> Subscriber<T> asSubscriber() {",
        ),
        (
            "    @SuppressWarnings(\"unchecked\")\n    public static <T> Observer<T> asObserver() {",
            "    /** @return 作为 {@link Observer} 使用的单例 */\n    @SuppressWarnings(\"unchecked\")\n    public static <T> Observer<T> asObserver() {",
        ),
        (
            "        // deliberately no-op",
            "        // 故意空操作",
        ),
    ],
    "EndConsumerHelper.java": [
        (
            "/**\n * Utility class to help report multiple subscriptions with the same\n * consumer type instead of the internal \"Disposable already set!\" message\n * that is practically reserved for internal operators and indicate bugs in them.\n */",
            "/**\n * 辅助报告同一 consumer 类型的重复订阅，\n * 替代内部 \"Disposable already set!\" 消息（该消息主要留给内部算子 bug）。\n */",
        ),
        (
            "    /**\n     * Utility class.\n     */",
            "    /**\n     * 工具类，禁止实例化。\n     */",
        ),
        (
            "    /**\n     * Ensures that the upstream Disposable is null and returns true, otherwise\n     * disposes the next Disposable and if the upstream is not the shared\n     * disposed instance, reports a ProtocolViolationException due to\n     * multiple subscribe attempts.\n     * @param upstream the upstream current value\n     * @param next the Disposable to check for nullness and dispose if necessary\n     * @param observer the class of the consumer to have a personalized\n     * error message if the upstream already contains a non-cancelled Disposable.\n     * @return true if successful, false if the upstream was non-null\n     */",
            "    /**\n     * 确保上游 Disposable 为 null；否则 dispose next 并在非共享 disposed 实例时报告重复订阅。\n     * @param upstream 上游当前值\n     * @param next 待设置的 Disposable\n     * @param observer consumer 类，用于个性化错误消息\n     * @return 上游为 null 时 true\n     */",
        ),
        (
            "    /**\n     * Atomically updates the target upstream AtomicReference from null to the non-null\n     * next Disposable, otherwise disposes next and reports a ProtocolViolationException\n     * if the AtomicReference doesn't contain the shared disposed indicator.\n     * @param upstream the target AtomicReference to update\n     * @param next the Disposable to set on it atomically\n     * @param observer the class of the consumer to have a personalized\n     * error message if the upstream already contains a non-cancelled Disposable.\n     * @return true if successful, false if the content of the AtomicReference was non-null\n     */",
            "    /**\n     * 原子地将 null 更新为 next Disposable；失败则 dispose next 并报告协议违规。\n     * @param upstream 目标 AtomicReference\n     * @param next 要设置的 Disposable\n     * @param observer consumer 类，用于个性化错误消息\n     * @return CAS 成功为 true\n     */",
        ),
        (
            "    /**\n     * Ensures that the upstream Subscription is null and returns true, otherwise\n     * cancels the next Subscription and if the upstream is not the shared\n     * cancelled instance, reports a ProtocolViolationException due to\n     * multiple subscribe attempts.\n     * @param upstream the upstream current value\n     * @param next the Subscription to check for nullness and cancel if necessary\n     * @param subscriber the class of the consumer to have a personalized\n     * error message if the upstream already contains a non-cancelled Subscription.\n     * @return true if successful, false if the upstream was non-null\n     */",
            "    /**\n     * 确保上游 Subscription 为 null；否则 cancel next 并在非共享 cancelled 实例时报告重复订阅。\n     * @param upstream 上游当前值\n     * @param next 待设置的 Subscription\n     * @param subscriber consumer 类，用于个性化错误消息\n     * @return 上游为 null 时 true\n     */",
        ),
        (
            "    /**\n     * Atomically updates the target upstream AtomicReference from null to the non-null\n     * next Subscription, otherwise cancels next and reports a ProtocolViolationException\n     * if the AtomicReference doesn't contain the shared cancelled indicator.\n     * @param upstream the target AtomicReference to update\n     * @param next the Subscription to set on it atomically\n     * @param subscriber the class of the consumer to have a personalized\n     * error message if the upstream already contains a non-cancelled Subscription.\n     * @return true if successful, false if the content of the AtomicReference was non-null\n     */",
            "    /**\n     * 原子地将 null 更新为 next Subscription；失败则 cancel next 并报告协议违规。\n     * @param upstream 目标 AtomicReference\n     * @param next 要设置的 Subscription\n     * @param subscriber consumer 类，用于个性化错误消息\n     * @return CAS 成功为 true\n     */",
        ),
        (
            "    /**\n     * Builds the error message with the consumer class.\n     * @param consumer the class of the consumer\n     * @return the error message string\n     */",
            "    /**\n     * 根据 consumer 类名构建错误消息。\n     * @param consumer consumer 类名\n     * @return 错误消息字符串\n     */",
        ),
        (
            "    /**\n     * Report a ProtocolViolationException with a personalized message referencing\n     * the simple type name of the consumer class and report it via\n     * RxJavaPlugins.onError.\n     * @param consumer the class of the consumer\n     */",
            "    /**\n     * 报告带个性化消息的 {@link ProtocolViolationException}，并通过 RxJavaPlugins.onError 上报。\n     * @param consumer consumer 类\n     */",
        ),
    ],
    "ExceptionHelper.java": [
        (
            "/**\n * Terminal atomics for Throwable containers.\n */",
            "/**\n * Throwable 容器的终止态原子操作工具。\n */",
        ),
        (
            "    /** Utility class. */",
            "    /** 工具类，禁止实例化。 */",
        ),
        (
            "    /**\n     * If the provided Throwable is an Error this method\n     * throws it, otherwise returns a CompletionException wrapping the error\n     * if that error is a checked exception.\n     * @param error the error to wrap or throw\n     * @return the (wrapped) error\n     */",
            "    /**\n     * 若 error 为 Error 则直接抛出；否则对受检异常包装为 CompletionException 后返回。\n     * @param error 要包装或抛出的错误\n     * @return 包装后的错误\n     */",
        ),
        (
            "    /**\n     * Unwraps a {@link CompletionException} and rethrows its {@link Error}\n     * or {@link RuntimeException} inside it, or returns it as is if\n     * the {@code CompletionException} holds a checked exception.\n     * @param error the error to unwrap and rethrow its cause if possible\n     * @return the {@code error} if it has a checked exception cause\n     * @since 4.0.0\n     */",
            "    /**\n     * 解包 {@link CompletionException} 并重新抛出其中的 {@link Error} 或 {@link RuntimeException}；\n     * 若 cause 为受检异常则原样返回。\n     * @param error 要解包的错误\n     * @return cause 为受检异常时返回原 error\n     * @since 4.0.0\n     */",
        ),
        (
            "    /**\n     * A singleton instance of a Throwable indicating a terminal state for exceptions,\n     * don't leak this.\n     */",
            "    /**\n     * 表示异常容器终止态的单例 Throwable，请勿泄漏。\n     */",
        ),
        (
            "    /**\n     * Returns a flattened list of Throwables from tree-like CompositeException chain.\n     * @param t the starting throwable\n     * @return the list of Throwables flattened in a depth-first manner\n     */",
            "    /**\n     * 将树状 CompositeException 链深度优先展平为 Throwable 列表。\n     * @param t 起始异常\n     * @return 展平后的异常列表\n     */",
        ),
        (
            "    /**\n     * Workaround for Java 6 not supporting throwing a final Throwable from a catch block.\n     * @param <E> the generic exception type\n     * @param e the Throwable error to return or throw\n     * @return the Throwable e if it is a subclass of Exception\n     * @throws E the generic exception thrown\n     */",
            "    /**\n     * Java 6 无法在 catch 中抛出 final Throwable 的变通方法。\n     * @param <E> 泛型异常类型\n     * @param e 要返回或抛出的 Throwable\n     * @return 若 e 为 Exception 子类则返回 e\n     * @throws E 否则抛出\n     */",
        ),
        (
            "    public static String timeoutMessage(long timeout, TimeUnit unit) {",
            "    /** 构建超时终止消息。 */\n    public static String timeoutMessage(long timeout, TimeUnit unit) {",
        ),
        (
            "    static final class Termination extends Throwable {",
            "    /** 终止态占位异常，不填充堆栈。 */\n    static final class Termination extends Throwable {",
        ),
        (
            "    /**\n     * Composes a String with a null warning message.\n     * @param prefix the prefix to add to the message.\n     * @return the composed String\n     * @since 3.0.0\n     */",
            "    /**\n     * 拼接含 null 警告的消息字符串。\n     * @param prefix 消息前缀\n     * @return 拼接后的字符串\n     * @since 3.0.0\n     */",
        ),
        (
            "    /**\n     * Creates a NullPointerException with a composed message via {@link #nullWarning(String)}.\n     * @param prefix the prefix to add to the message.\n     * @return the composed String\n     * @since 3.0.0\n     */",
            "    /**\n     * 通过 {@link #nullWarning(String)} 创建 NullPointerException。\n     * @param prefix 消息前缀\n     * @return NullPointerException\n     * @since 3.0.0\n     */",
        ),
        (
            "    /**\n     * Similar to Objects.requireNonNull but composes the error message via\n     * {@link #nullWarning(String)}.\n     * @param <T> the value type\n     * @param value the value to check\n     * @param prefix the prefix to the error message\n     * @return the value\n     * @throws NullPointerException if value is null\n     * @since 3.0.0\n     */",
            "    /**\n     * 类似 Objects.requireNonNull，但错误消息由 {@link #nullWarning(String)} 拼接。\n     * @param <T> 值类型\n     * @param value 待检查的值\n     * @param prefix 错误消息前缀\n     * @return value\n     * @throws NullPointerException value 为 null 时\n     * @since 3.0.0\n     */",
        ),
        (
            "    /**\n     * Unwraps both throwables if they are wrapped into a {@link CompletionException},\n     * then if both are present, add {@code b} as suppressed to {@code a}\n     * and return a; return b otherwise\n     * @param main the first throwable\n     * @param secondary the second throwable\n     * @return the unwrapped and combined throwable or null if both where\n     */",
            "    /**\n     * 解包两个可能为 {@link CompletionException} 的异常；若均非 null 则将 b 压入 a 的 suppressed 并返回 a。\n     * @param main 第一个异常\n     * @param secondary 第二个异常\n     * @return 解包合并后的异常，或 null\n     */",
        ),
        (
            "    /**\n     * Unwraps the given {@link CompletionException}.\n     * @param t the possible throwable to unwrap\n     * @return the unwrapped Throwable\n     */",
            "    /**\n     * 解包给定 {@link CompletionException}。\n     * @param t 可能需解包的 Throwable\n     * @return 解包后的 Throwable\n     */",
        ),
    ],
    "HalfSerializer.java": [
        (
            "/**\n * Utility methods to perform half-serialization: a form of serialization\n * where onNext is guaranteed to be called from a single thread but\n * onError or onComplete may be called from any threads.\n */",
            "/**\n * 半序列化工具：保证 onNext 单线程调用，\n * 而 onError/onComplete 可能来自任意线程。\n */",
        ),
        (
            "    /** Utility class. */",
            "    /** 工具类，禁止实例化。 */",
        ),
        (
            "    /**\n     * Emits the given value if possible and terminates if there was an onComplete or onError\n     * while emitting, drops the value otherwise.\n     * @param <T> the value type\n     * @param subscriber the target Subscriber to emit to\n     * @param value the value to emit\n     * @param wip the serialization work-in-progress counter/indicator\n     * @param errors the holder of Throwables\n     * @return true if the operation succeeded, false if there sequence completed\n     */",
            "    /**\n     * 若可则发射给定值；发射期间若已 onComplete/onError 则终止，否则丢弃该值。\n     * @param <T> 值类型\n     * @param subscriber 目标 Subscriber\n     * @param value 要发射的值\n     * @param wip 序列化进行中计数/标志\n     * @param errors Throwable 容器\n     * @return 操作成功为 true；序列已完成为 false\n     */",
        ),
        (
            "    /**\n     * Emits the given exception if possible or adds it to the given error container to\n     * be emitted by a concurrent onNext if one is running.\n     * Undeliverable exceptions are sent to the RxJavaPlugins.onError.\n     * @param subscriber the target Subscriber to emit to\n     * @param ex the Throwable to emit\n     * @param wip the serialization work-in-progress counter/indicator\n     * @param errors the holder of Throwables\n     */",
            "    /**\n     * 若可则发射异常，否则加入错误容器由并发 onNext 稍后发射。\n     * 无法投递的异常交给 RxJavaPlugins.onError。\n     * @param subscriber 目标 Subscriber\n     * @param ex 要发射的 Throwable\n     * @param wip 序列化进行中计数/标志\n     * @param errors Throwable 容器\n     */",
        ),
        (
            "    /**\n     * Emits an onComplete signal or an onError signal with the given error or indicates\n     * the concurrently running onNext should do that.\n     * @param subscriber the target Subscriber to emit to\n     * @param wip the serialization work-in-progress counter/indicator\n     * @param errors the holder of Throwables\n     */",
            "    /**\n     * 发射 onComplete 或 onError，或由并发 onNext 负责终止。\n     * @param subscriber 目标 Subscriber\n     * @param wip 序列化进行中计数/标志\n     * @param errors Throwable 容器\n     */",
        ),
        (
            "    /**\n     * Emits the given value if possible and terminates if there was an onComplete or onError\n     * while emitting, drops the value otherwise.\n     * @param <T> the value type\n     * @param observer the target Observer to emit to\n     * @param value the value to emit\n     * @param wip the serialization work-in-progress counter/indicator\n     * @param errors the holder of Throwables\n     */",
            "    /**\n     * Observer 版 onNext：逻辑同 Subscriber 版本。\n     * @param <T> 值类型\n     * @param observer 目标 Observer\n     * @param value 要发射的值\n     * @param wip 序列化进行中计数/标志\n     * @param errors Throwable 容器\n     */",
        ),
        (
            "    /**\n     * Emits the given exception if possible or adds it to the given error container to\n     * be emitted by a concurrent onNext if one is running.\n     * Undeliverable exceptions are sent to the RxJavaPlugins.onError.\n     * @param observer the target Subscriber to emit to\n     * @param ex the Throwable to emit\n     * @param wip the serialization work-in-progress counter/indicator\n     * @param errors the holder of Throwables\n     */",
            "    /**\n     * Observer 版 onError：逻辑同 Subscriber 版本。\n     * @param observer 目标 Observer\n     * @param ex 要发射的 Throwable\n     * @param wip 序列化进行中计数/标志\n     * @param errors Throwable 容器\n     */",
        ),
        (
            "    /**\n     * Emits an onComplete signal or an onError signal with the given error or indicates\n     * the concurrently running onNext should do that.\n     * @param observer the target Subscriber to emit to\n     * @param wip the serialization work-in-progress counter/indicator\n     * @param errors the holder of Throwables\n     */",
            "    /**\n     * Observer 版 onComplete：逻辑同 Subscriber 版本。\n     * @param observer 目标 Observer\n     * @param wip 序列化进行中计数/标志\n     * @param errors Throwable 容器\n     */",
        ),
    ],
    "HashMapSupplier.java": [
        (
            "public enum HashMapSupplier implements Supplier<Map<Object, Object>> {",
            "/**\n * 提供空 {@link HashMap} 的 {@link Supplier} 单例。\n */\npublic enum HashMapSupplier implements Supplier<Map<Object, Object>> {",
        ),
        (
            "    @SuppressWarnings({ \"unchecked\", \"rawtypes\" })\n    public static <K, V> Supplier<Map<K, V>> asSupplier() {",
            "    /** @return 创建空 {@link HashMap} 的 {@link Supplier} 单例 */\n    @SuppressWarnings({ \"unchecked\", \"rawtypes\" })\n    public static <K, V> Supplier<Map<K, V>> asSupplier() {",
        ),
        (
            "    @Override public Map<Object, Object> get() {",
            "    /** @return 新的空 {@link HashMap} */\n    @Override public Map<Object, Object> get() {",
        ),
    ],
    "LinkedArrayList.java": [
        (
            "/**\n * A list implementation which combines an ArrayList with a LinkedList to\n * avoid copying values when the capacity needs to be increased.\n * <p>\n * The class is non-final to allow embedding it directly and thus saving on object allocation.\n */",
            "/**\n * 结合 ArrayList 与 LinkedList 的列表实现，扩容时避免复制已有元素。\n * <p>\n * 类非 final，便于内嵌以减少对象分配。\n */",
        ),
        (
            "    /** The capacity of each array segment. */",
            "    /** 每个数组段的容量。 */",
        ),
        (
            "    /**\n     * Contains the head of the linked array list if not null. The\n     * length is always capacityHint + 1 and the last element is an Object[] pointing\n     * to the next element of the linked array list.\n     */",
            "    /**\n     * 非 null 时为链式数组列表头；长度恒为 capacityHint + 1，\n     * 末元素为指向下一段的 Object[]。\n     */",
        ),
        (
            "    /** The tail array where new elements will be added. */",
            "    /** 当前尾段，新元素追加于此。 */",
        ),
        (
            "    /**\n     * The total size of the list; written after elements have been added (release) and\n     * when read, the value indicates how many elements can be safely read (acquire).\n     */",
            "    /**\n     * 列表总大小；写入在追加之后（release），\n     * 读取时表示可安全读取的元素数（acquire）。\n     */",
        ),
        (
            "    /** The next available slot in the current tail. */",
            "    /** 当前尾段下一个可用槽位。 */",
        ),
        (
            "    /**\n     * Constructor with the capacity hint of each array segment.\n     * @param capacityHint the expected number of elements to hold (can grow beyond that)\n     */",
            "    /**\n     * 指定每段容量提示的构造函数。\n     * @param capacityHint 预期元素数（可超出）\n     */",
        ),
        (
            "    /**\n     * Adds a new element to this list.\n     * @param o the object to add, nulls are accepted\n     */",
            "    /**\n     * 向列表添加元素。\n     * @param o 要添加的对象，允许 null\n     */",
        ),
        (
            "        // if no value yet, create the first array",
            "        // 尚无元素，创建首段数组",
        ),
        (
            "        // if the tail is full, create a new tail and link",
            "        // 尾段已满，创建新尾段并链接",
        ),
        (
            "    /**\n     * Returns the head buffer segment or null if the list is empty.\n     * @return the head object array\n     */",
            "    /**\n     * 返回头段缓冲区；列表为空时为 null。\n     * @return 头 Object 数组\n     */",
        ),
        (
            "    /**\n     * Returns the total size of the list.\n     * @return the total size of the list\n     */",
            "    /**\n     * 返回列表总大小。\n     * @return 列表总大小\n     */",
        ),
    ],
    "ListAddBiConsumer.java": [
        (
            "@SuppressWarnings(\"rawtypes\")\npublic enum ListAddBiConsumer implements BiFunction<List, Object, List> {",
            "/**\n * 将元素追加到 {@link List} 并返回同一列表的 {@link BiFunction} 单例。\n */\n@SuppressWarnings(\"rawtypes\")\npublic enum ListAddBiConsumer implements BiFunction<List, Object, List> {",
        ),
        (
            "    @SuppressWarnings(\"unchecked\")\n    public static <T> BiFunction<List<T>, T, List<T>> instance() {",
            "    /** @return ListAddBiConsumer 单例 */\n    @SuppressWarnings(\"unchecked\")\n    public static <T> BiFunction<List<T>, T, List<T>> instance() {",
        ),
        (
            "    @SuppressWarnings(\"unchecked\")\n    @Override\n    public List apply(List t1, Object t2) {",
            "    /** 将 t2 追加到 t1 并返回 t1。 */\n    @SuppressWarnings(\"unchecked\")\n    @Override\n    public List apply(List t1, Object t2) {",
        ),
    ],
    "MergerBiFunction.java": [
        (
            "/**\n * A BiFunction that merges two Lists into a new list.\n * @param <T> the value type\n */",
            "/**\n * 将两个 List 按比较器归并为新列表的 {@link BiFunction}。\n * @param <T> 值类型\n */",
        ),
        (
            "    public MergerBiFunction(Comparator<? super T> comparator) {",
            "    /**\n     * @param comparator 归并时使用的比较器\n     */\n    public MergerBiFunction(Comparator<? super T> comparator) {",
        ),
        (
            "            if (comparator.compare(s1, s2) < 0) { // s1 comes before s2",
            "            if (comparator.compare(s1, s2) < 0) { // s1 排在 s2 前",
        ),
    ],
}
