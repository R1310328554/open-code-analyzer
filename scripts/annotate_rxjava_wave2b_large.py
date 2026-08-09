"""Large-file JavaDoc replacements for RxJava wave-2b [15:30]."""

OBSERVER: list[tuple[str, str]] = [
    (
        "/**\n * Provides a mechanism for receiving push-based notifications.\n * <p>\n * When an {@code Observer} is subscribed to an {@link ObservableSource} through the {@link ObservableSource#subscribe(Observer)} method,\n * the {@code ObservableSource} calls {@link #onSubscribe(Disposable)}  with a {@link Disposable} that allows\n * disposing the sequence at any time, then the\n * {@code ObservableSource} may call the Observer's {@link #onNext} method any number of times\n * to provide notifications. A well-behaved\n * {@code ObservableSource} will call an {@code Observer}'s {@link #onComplete} method exactly once or the {@code Observer}'s\n * {@link #onError} method exactly once.\n * <p>\n * Calling the {@code Observer}'s method must happen in a serialized fashion, that is, they must not\n * be invoked concurrently by multiple threads in an overlapping fashion and the invocation pattern must\n * adhere to the following protocol:\n * <pre><code>    onSubscribe onNext* (onError | onComplete)?</code></pre>\n * <p>\n * Subscribing an {@code Observer} to multiple {@code ObservableSource}s is not recommended. If such reuse\n * happens, it is the duty of the {@code Observer} implementation to be ready to receive multiple calls to\n * its methods and ensure proper concurrent behavior of its business logic.\n * <p>\n * Calling {@link #onSubscribe(Disposable)}, {@link #onNext(Object)} or {@link #onError(Throwable)} with a\n * {@code null} argument is forbidden.\n * <p>\n * The implementations of the {@code onXXX} methods should avoid throwing runtime exceptions other than the following cases\n * (see <a href=\"https://github.com/reactive-streams/reactive-streams-jvm#2.13\">Rule 2.13</a> of the Reactive Streams specification):\n * <ul>\n * <li>If the argument is {@code null}, the methods can throw a {@code NullPointerException}.\n * Note though that RxJava prevents {@code null}s to enter into the flow and thus there is generally no\n * need to check for nulls in flows assembled from standard sources and intermediate operators.\n * </li>\n * <li>If there is a fatal error (such as {@code VirtualMachineError}).</li>\n * </ul>\n * <p>\n * Violating Rule 2.13 results in undefined flow behavior. Generally, the following can happen:\n * <ul>\n * <li>An upstream operator turns it into an {@link #onError} call.</li>\n * <li>If the flow is synchronous, the {@link ObservableSource#subscribe(Observer)} throws instead of returning normally.</li>\n * <li>If the flow is asynchronous, the exception propagates up to the component ({@link Scheduler} or {@link java.util.concurrent.Executor})\n * providing the asynchronous boundary the code is running and either routes the exception to the global\n * {@link io.reactivex.rxjava4.plugins.RxJavaPlugins#onError(Throwable)} handler or the current thread's\n * {@link java.lang.Thread.UncaughtExceptionHandler#uncaughtException(Thread, Throwable)} handler.</li>\n * </ul>\n * From the {@code Observable}'s perspective, an {@code Observer} is the end consumer thus it is the {@code Observer}'s\n * responsibility to handle the error case and signal it \"further down\". This means unreliable code in the {@code onXXX}\n * methods should be wrapped into `try-catch`es, specifically in {@link #onError(Throwable)} or {@link #onComplete()}, and handled there\n * (for example, by logging it or presenting the user with an error dialog). However, if the error would be thrown from\n * {@link #onNext(Object)}, <a href=\"https://github.com/reactive-streams/reactive-streams-jvm#2.13\">Rule 2.13</a> mandates\n * the implementation calls {@link Disposable#dispose()} and signals the exception in a way that is adequate to the target context,\n * for example, by calling {@link #onError(Throwable)} on the same {@code Observer} instance.\n * <p>\n * If, for some reason, the {@code Observer} won't follow Rule 2.13, the {@link Observable#safeSubscribe(Observer)} can wrap it\n * with the necessary safeguards and route exceptions thrown from {@code onNext} into {@code onError} and route exceptions thrown\n * from {@code onError} and {@code onComplete} into the global error handler via {@link io.reactivex.rxjava4.plugins.RxJavaPlugins#onError(Throwable)}.\n * @see <a href=\"http://reactivex.io/documentation/observable.html\">ReactiveX documentation: Observable</a>\n * @param <T>\n *          the type of item the Observer expects to observe\n */",
        "/**\n * 提供接收 push 式通知的机制。\n * <p>\n * 当 {@code Observer} 通过 {@link ObservableSource#subscribe(Observer)} 方法订阅 {@link ObservableSource} 时，\n * {@code ObservableSource} 会调用 {@link #onSubscribe(Disposable)} 并传入 {@link Disposable}，\n * 允许随时 dispose 序列；随后 {@code ObservableSource} 可任意次数调用 Observer 的 {@link #onNext} 方法\n * 以提供通知。行为良好的 {@code ObservableSource} 会恰好一次调用 {@code Observer} 的 {@link #onComplete}，\n * 或恰好一次调用 {@code Observer} 的 {@link #onError}。\n * <p>\n * 调用 {@code Observer} 的方法必须串行进行，即不可被多线程以重叠方式并发调用，\n * 且调用模式须遵循以下协议：\n * <pre><code>    onSubscribe onNext* (onError | onComplete)?</code></pre>\n * <p>\n * 不建议将同一 {@code Observer} 订阅多个 {@code ObservableSource}。若发生此类复用，\n * {@code Observer} 实现者有责任准备好接收多次方法调用，并确保其业务逻辑的并发行为正确。\n * <p>\n * 禁止以 {@code null} 参数调用 {@link #onSubscribe(Disposable)}、{@link #onNext(Object)} 或 {@link #onError(Throwable)}。\n * <p>\n * {@code onXXX} 方法的实现应避免抛出运行时异常，以下情况除外\n *（参见 Reactive Streams 规范 <a href=\"https://github.com/reactive-streams/reactive-streams-jvm#2.13\">Rule 2.13</a>）：\n * <ul>\n * <li>若参数为 {@code null}，方法可抛出 {@code NullPointerException}。\n * 但 RxJava 会阻止 {@code null} 进入流，因此由标准源与中间算子组装的流通常无需检查 null。\n * </li>\n * <li>若发生致命错误（如 {@code VirtualMachineError}）。</li>\n * </ul>\n * <p>\n * 违反 Rule 2.13 会导致未定义的流行为。通常可能出现以下情况：\n * <ul>\n * <li>上游算子将其转为 {@link #onError} 调用。</li>\n * <li>若流为同步，{@link ObservableSource#subscribe(Observer)} 会抛出异常而非正常返回。</li>\n * <li>若流为异步，异常会向上传播至提供异步边界的组件（{@link Scheduler} 或 {@link java.util.concurrent.Executor}），\n * 并将异常路由到全局 {@link io.reactivex.rxjava4.plugins.RxJavaPlugins#onError(Throwable)} 处理器，\n * 或当前线程的 {@link java.lang.Thread.UncaughtExceptionHandler#uncaughtException(Thread, Throwable)} 处理器。</li>\n * </ul>\n * 从 {@code Observable} 的角度看，{@code Observer} 是终端消费者，因此处理错误并向下游传递信号是 {@code Observer} 的责任。\n * 这意味着 {@code onXXX} 方法中不可靠的代码应包在 `try-catch` 中，尤其在 {@link #onError(Throwable)} 或 {@link #onComplete()} 中处理\n *（例如记录日志或向用户展示错误对话框）。但若错误从 {@link #onNext(Object)} 抛出，\n * <a href=\"https://github.com/reactive-streams/reactive-streams-jvm#2.13\">Rule 2.13</a> 要求实现调用 {@link Disposable#dispose()}，\n * 并以适合目标上下文的方式发出异常信号，例如在同一 {@code Observer} 实例上调用 {@link #onError(Throwable)}。\n * <p>\n * 若 {@code Observer} 因某种原因无法遵循 Rule 2.13，{@link Observable#safeSubscribe(Observer)} 可为其添加必要防护，\n * 将 {@code onNext} 抛出的异常路由到 {@code onError}，并将 {@code onError} 与 {@code onComplete} 抛出的异常\n * 通过 {@link io.reactivex.rxjava4.plugins.RxJavaPlugins#onError(Throwable)} 路由到全局错误处理器。\n * @see <a href=\"http://reactivex.io/documentation/observable.html\">ReactiveX documentation: Observable</a>\n * @param <T>\n *          Observer 期望观察的元素类型\n */",
    ),
    (
        "    /**\n     * Provides the {@link Observer} with the means of cancelling (disposing) the\n     * connection (channel) with the {@link Observable} in both\n     * synchronous (from within {@link #onNext(Object)}) and asynchronous manner.\n     * @param d the {@link Disposable} instance whose {@link Disposable#dispose()} can\n     * be called anytime to cancel the connection\n     * @since 2.0\n     */",
        "    /**\n     * 为 {@link Observer} 提供以同步（在 {@link #onNext(Object)} 内）或异步方式\n     * 取消（dispose）与 {@link Observable} 连接（通道）的手段。\n     * @param d 可随时调用 {@link Disposable#dispose()} 以取消连接的 {@link Disposable} 实例\n     * @since 2.0\n     */",
    ),
    (
        "    /**\n     * Provides the {@link Observer} with a new item to observe.\n     * <p>\n     * The {@link Observable} may call this method 0 or more times.\n     * <p>\n     * The {@code Observable} will not call this method again after it calls either {@link #onComplete} or\n     * {@link #onError}.\n     *\n     * @param t\n     *          the item emitted by the Observable\n     */",
        "    /**\n     * 向 {@link Observer} 提供新的待观察元素。\n     * <p>\n     * {@link Observable} 可调用本方法 0 次或多次。\n     * <p>\n     * {@code Observable} 在调用 {@link #onComplete} 或 {@link #onError} 之后不会再调用本方法。\n     *\n     * @param t\n     *          Observable 发射的元素\n     */",
    ),
    (
        "    /**\n     * Notifies the {@link Observer} that the {@link Observable} has experienced an error condition.\n     * <p>\n     * If the {@code Observable} calls this method, it will not thereafter call {@link #onNext} or\n     * {@link #onComplete}.\n     *\n     * @param e\n     *          the exception encountered by the Observable\n     */",
        "    /**\n     * 通知 {@link Observer}：{@link Observable} 遇到错误。\n     * <p>\n     * 若 {@code Observable} 调用本方法，此后不会再调用 {@link #onNext} 或 {@link #onComplete}。\n     *\n     * @param e\n     *          Observable 遇到的异常\n     */",
    ),
    (
        "    /**\n     * Notifies the {@link Observer} that the {@link Observable} has finished sending push-based notifications.\n     * <p>\n     * The {@code Observable} will not call this method if it calls {@link #onError}.\n     */",
        "    /**\n     * 通知 {@link Observer}：{@link Observable} 已完成 push 式通知的发送。\n     * <p>\n     * 若 {@code Observable} 调用了 {@link #onError}，则不会调用本方法。\n     */",
    ),
]

STREAM_SINK: list[tuple[str, str]] = [
    (
        "/**\n * An interface to submit items and terminal events to a consumer that indacates when the processing of\n * said item or terminal event has completed, similar to how {@link Subscriber} can receive events.\n * <p>\n * The general contract is to call {@link #next(Object)} zero or more times, then\n * call {@link #finish(Throwable)} at most once, all in a non-overlapping fashion and only if the\n * returned {@link CompletionStage} has completed in some fashion.\n * @param <T> the item type to be offered\n * @since 4.0.0\n */",
        "/**\n * 向消费者提交元素与终端事件的接口；消费者会指示该元素或终端事件的处理何时完成，\n * 类似 {@link Subscriber} 接收事件的方式。\n * <p>\n * 一般约定：以非重叠方式调用 {@link #next(Object)} 零次或多次，\n * 然后至多调用一次 {@link #finish(Throwable)}，且仅在返回的 {@link CompletionStage} 以某种方式完成之后。\n * @param <T> 待提交的元素类型\n * @since 4.0.0\n */",
    ),
    (
        "    /**\n     * Offer the next item.\n     * @param item the item being offered\n     * @return a {@link CompletionStage} that completes with {@code true} if the value was successfully consumed,\n     *         {@code false} if the value was rejected or exceptionally on error\n     */",
        "    /**\n     * 提交下一个元素。\n     * @param item 待提交的元素\n     * @return 若值被成功消费则完成为 {@code true} 的 {@link CompletionStage}；\n     *         若值被拒绝或在错误时以异常完成则为 {@code false}\n     */",
    ),
    (
        "    /**\n     * Offer the final, terminal event.\n     * @param throwable the optional throwable to signal error, {@code null} to signal normal completion\n     * @return a {@link CompletionStage} that completes with {@code null} if the call succeeded\n     *         or exceptionally on error\n     */",
        "    /**\n     * 提交最终终端事件。\n     * @param throwable 可选的用于发出错误信号的 throwable，{@code null} 表示正常完成\n     * @return 若调用成功则完成为 {@code null} 的 {@link CompletionStage}，或在错误时以异常完成\n     */",
    ),
    (
        "    /**\n     * Offers the given item and then awaits its consumption in a blocking fashion.\n     * @param item the item being offered\n     * @return true if the item was accepted, false if not\n     * @throws CancellationException if there was a cancellation issued\n     * @throws CompletionException if the upstream failed\n     */",
        "    /**\n     * 提交给定元素并以阻塞方式等待其被消费。\n     * @param item 待提交的元素\n     * @return 若元素被接受则为 true，否则为 false\n     * @throws CancellationException 若发生取消\n     * @throws CompletionException 若上游失败\n     */",
    ),
    (
        "    /**\n     * Offer the final, terminal event and then awaits its consumption in a blocking fashion.\n     * @param throwable the optional throwable to signal error, {@code null} to signal normal completion\n     * @throws CancellationException if there was a cancellation issued\n     * @throws CompletionException if the upstream failed\n     */",
        "    /**\n     * 提交最终终端事件并以阻塞方式等待其被消费。\n     * @param throwable 可选的用于发出错误信号的 throwable，{@code null} 表示正常完成\n     * @throws CancellationException 若发生取消\n     * @throws CompletionException 若上游失败\n     */",
    ),
    (
        "    /**\n     * Returns the {@link DisposableContainer} to use to detect if the consumer has indicated no more\n     * items it is willing to accept.\n     * <p>\n     * The default implementation returns a fresh {@link CompositeDisposable}.\n     * @return the {@code DisposableContainer}\n     */",
        "    /**\n     * 返回用于检测消费者是否已表示不再接受更多元素的 {@link DisposableContainer}。\n     * <p>\n     * 默认实现返回新的 {@link CompositeDisposable}。\n     * @return {@code DisposableContainer}\n     */",
    ),
    (
        "    /**\n     * Returns a new {@link StreamSink} that returns the given {@link DisposableStreamerCancellation}\n     * in its {@link #cancellation()}, allowing overriding the cancellation management\n     * of this {@code StreamSink}\n     * @param cancellation the {@link DisposableStreamerCancellation} to use as cancellation management\n     * @return the new {@code StreamSink} instance\n     * @throws NullPointerException if {@code cancellation} is {@code null}\n     */",
        "    /**\n     * 返回新的 {@link StreamSink}，其 {@link #cancellation()} 返回给定 {@link DisposableStreamerCancellation}，\n     * 允许覆盖本 {@code StreamSink} 的取消管理。\n     * @param cancellation 用作取消管理的 {@link DisposableStreamerCancellation}\n     * @return 新的 {@code StreamSink} 实例\n     * @throws NullPointerException 若 {@code cancellation} 为 {@code null}\n     */",
    ),
    (
        "    /**\n     * Creates a {@link StreamSink} via lambda callbacks for {@link #next(Object)} and\n     * {@link #finish(Throwable)}.\n     * <p>\n     * Non-fatal exceptions thrown by the callbacks are turned into failed\n     * {@link CompletableFuture#failedFuture(Throwable)}s.\n     * @param <T> the element type of the stream\n     * @param onNext the callback for the {@code next} method\n     * @param onFinish the callback for the {@code finish} method\n     * @return the new {@link StreamSink} instance\n     */",
        "    /**\n     * 通过 {@link #next(Object)} 与 {@link #finish(Throwable)} 的 lambda 回调创建 {@link StreamSink}。\n     * <p>\n     * 回调抛出的非致命异常会转为失败的 {@link CompletableFuture#failedFuture(Throwable)}。\n     * @param <T> 流的元素类型\n     * @param onNext {@code next} 方法的回调\n     * @param onFinish {@code finish} 方法的回调\n     * @return 新的 {@link StreamSink} 实例\n     */",
    ),
]

STREAMER: list[tuple[str, str]] = [
    (
        "/// A realized stream which can then be consumed asynchronously in steps.\n/// Think of it as the `IAsyncEnumerator` from C# ported to the clumsy Java world.\n///\n/// The `Streamer` methods must be invoked sequentially and non-overlappingly, similar to the\n/// <a href='https://github.com/reactive-streams/reactive-streams-jvm#1.3'>Reactive Streams rule §1.3</a>.\n///\n/// For an optimized synchronous operation, please consider using the {@link #NEXT_TRUE}, {@link #NEXT_FALSE}\n/// and {@link #FINISHED} constant CompletionStages.\n/// @param <T> the element type.\n/// @since 4.0.0",
        "/// 已实现的可分步异步消费的流。\n/// 可将其视为移植到 Java 的 C# `IAsyncEnumerator`。\n///\n/// `Streamer` 方法必须像\n/// <a href='https://github.com/reactive-streams/reactive-streams-jvm#1.3'>Reactive Streams 规则 §1.3</a> 一样顺序、非重叠地调用。\n///\n/// 为优化同步操作，请考虑使用 {@link #NEXT_TRUE}、{@link #NEXT_FALSE}\n/// 与 {@link #FINISHED} 常量 CompletionStage。\n/// @param <T> 元素类型\n/// @since 4.0.0",
    ),
    (
        "    /**\n     * Determine if there are more elements available from the source.\n     * @return a `CompletionStage` with 3 outcomes\n     * <ul>\n     * <li>`true` indicates there is an item available for consumption via {@link #current()}\n     * <li>`false` indicates there are no more items available\n     * <li>`Throwable` indicates there was an upstream error\n     * </ul>\n     */",
        "    /**\n     * 判断源是否还有更多可用元素。\n     * @return 具有 3 种结果的 `CompletionStage`\n     * <ul>\n     * <li>`true` 表示可通过 {@link #current()} 消费一个元素\n     * <li>`false` 表示没有更多可用元素\n     * <li>`Throwable` 表示上游发生错误\n     * </ul>\n     */",
    ),
    (
        "    /**\n     * Returns the currently available item synchronously if the previous call to [#next()] yielded `true`.\n     * Calling it during an ongoing [#next()] or [#finish()] call, or beyond the lifecycle of the `Streamer`\n     * is an undefined behavior. It may yield `null` or throw.\n     * @return the current item\n     * @throws NoSuchElementException if there are no items to return\n     */",
        "    /**\n     * 若前一次 [#next()] 调用返回 `true`，则同步返回当前可用元素。\n     * 在 [#next()] 或 [#finish()] 调用进行中，或在 `Streamer` 生命周期之外调用本方法属于未定义行为，可能返回 `null` 或抛出异常。\n     * @return 当前元素\n     * @throws NoSuchElementException 若无元素可返回\n     */",
    ),
    (
        "    /**\n     * Finish the sequence once all processing has been done to it either via exhaustion or via cancellation.\n     * <p>\n     * Usually involves resource cleanup, so this method must be always called.\n     * <p>\n     * If the cleanup crashes and the [#next()] crashed too, the cleanup `Throwable` will be added as suppressed\n     * to the main crash `Throwable` from `next`.\n     *\n     * @return a `CompletionStage` that completes when the resource cleanup completes normally or exceptionally\n     */",
        "    /**\n     * 在通过耗尽或取消完成全部处理后结束序列。\n     * <p>\n     * 通常涉及资源清理，因此必须始终调用本方法。\n     * <p>\n     * 若清理崩溃且 [#next()] 也崩溃，清理产生的 `Throwable` 将作为 suppressed 异常\n     * 附加到 `next` 产生的主崩溃 `Throwable` 上。\n     *\n     * @return 在资源清理正常或异常完成时完成的 `CompletionStage`\n     */",
    ),
    (
        "    /**\n     * Convenience method to blockingly await the CompletionStage returned by the {@link #next()} method.\n     * @return true if there are more items, false if no more items are coming, or crashes\n     */",
        "    /**\n     * 便捷方法：阻塞等待 {@link #next()} 返回的 CompletionStage。\n     * @return 若还有更多元素则为 true，若无更多元素则为 false，或在出错时崩溃\n     */",
    ),
    (
        "    /**\n     * Convenience method to blockingly await the CompletionStage returned by the {@link #finish()} method.\n     */",
        "    /**\n     * 便捷方法：阻塞等待 {@link #finish()} 返回的 CompletionStage。\n     */",
    ),
    (
        "    /**\n     * Convenience method to await the completion of a boolean stage, optimized\n     * for handling {@link #NEXT_TRUE} and {@link #NEXT_FALSE} directly.\n     * @param stage the stage to await\n     * @return the result of the stage\n     */",
        "    /**\n     * 便捷方法：等待 boolean stage 完成，针对 {@link #NEXT_TRUE} 与 {@link #NEXT_FALSE} 做了直接处理优化。\n     * @param stage 待等待的 stage\n     * @return stage 的结果\n     */",
    ),
    (
        "    /**\n     * Convenience method to await the completion of a stage, optimized\n     * for handling {@link #FINISHED} directly.\n     * @param stage the stage to await\n     */",
        "    /**\n     * 便捷方法：等待 stage 完成，针对 {@link #FINISHED} 做了直接处理优化。\n     * @param stage 待等待的 stage\n     */",
    ),
    (
        "    /**\n     * Use this constant in {@link #next()} to indicate\n     * the next value is available, synchronously.\n     */",
        "    /**\n     * 在 {@link #next()} 中使用本常量，表示下一个值同步可用。\n     */",
    ),
    (
        "    /**\n     * Use this constant in {@link #next()} to indicate\n     * no more values will be available, synchronously.\n     */",
        "    /**\n     * 在 {@link #next()} 中使用本常量，表示同步地没有更多可用值。\n     */",
    ),
    (
        "    /**\n     * Use this constant in {@link #finish()} to indicate\n     * the cleanup was done synchronously.\n     */",
        "    /**\n     * 在 {@link #finish()} 中使用本常量，表示清理已同步完成。\n     */",
    ),
]

LARGE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "Observer.java": OBSERVER,
    "StreamSink.java": STREAM_SINK,
    "Streamer.java": STREAMER,
}
