#!/usr/bin/env python3
"""Chinese-annotate RxJava 4.0.0-alpha-21 wave-2a [0:15]."""
from __future__ import annotations

import json
import re
import shutil
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "rxjava/4.0.0-alpha-21"
ORIGINAL = VER / "original"
ANALYZED = VER / "analyzed"
QUEUE = VER / "_reports/class-queue"
BATCH_FILES = json.loads((QUEUE / "batch.json").read_text(encoding="utf-8"))["files"][0:15]

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "GroupedObservable.java": [
        (
            "/**\n * An {@link Observable} that has been grouped by key, the value of which can be obtained with {@link #getKey()}.\n * <p>\n * <em>Note:</em> A {@link GroupedObservable} will cache the items it is to emit until such time as it\n * is subscribed to. For this reason, in order to avoid memory leaks, you should not simply ignore those\n * {@code GroupedObservable}s that do not concern you. Instead, you can signal to them that they\n * may discard their buffers by applying an operator like {@link Observable#take take}{@code (0)} to them.\n *\n * @param <K>\n *            the type of the key\n * @param <T>\n *            the type of the items emitted by the {@code GroupedObservable}\n * @see Observable#groupBy(io.reactivex.rxjava4.functions.Function)\n * @see <a href=\"http://reactivex.io/documentation/operators/groupby.html\">ReactiveX documentation: GroupBy</a>\n */",
            "/**\n * 按 key 分组的 {@link Observable}，可通过 {@link #getKey()} 获取 key 值。\n * <p>\n * <em>注意：</em>{@link GroupedObservable} 会缓存待发射的元素，直至被订阅。\n * 因此为避免内存泄漏，不应简单忽略不关心的 {@code GroupedObservable}。\n * 可对其应用如 {@link Observable#take take}{@code (0)} 等算子，通知其可丢弃缓冲区。\n *\n * @param <K>\n *            key 的类型\n * @param <T>\n *            {@code GroupedObservable} 发射元素的类型\n * @see Observable#groupBy(io.reactivex.rxjava4.functions.Function)\n * @see <a href=\"http://reactivex.io/documentation/operators/groupby.html\">ReactiveX documentation: GroupBy</a>\n */",
        ),
        (
            "    /**\n     * Constructs a GroupedObservable with the given key.\n     * @param key the key\n     */",
            "    /**\n     * 使用给定 key 构造 GroupedObservable。\n     * @param key key\n     */",
        ),
        (
            "    /**\n     * Returns the key that identifies the group of items emitted by this {@code GroupedObservable}.\n     *\n     * @return the key that the items emitted by this {@code GroupedObservable} were grouped by\n     */",
            "    /**\n     * 返回标识本 {@code GroupedObservable} 所发射元素分组的 key。\n     *\n     * @return 本 {@code GroupedObservable} 发射元素所依据的分组 key\n     */",
        ),
    ],
    "GroupedStreamable.java": [
        (
            "/**\n * An {@link Streamable} that has been grouped by key, the value of which can be obtained with {@link #getKey()}.\n * @param <K>\n *            the type of the key, can be null\n * @param <T>\n *            the type of the items emitted by the {@code GroupedStreamable}\n * @see Streamable#groupBy(io.reactivex.rxjava4.functions.Function)\n * @see <a href=\"http://reactivex.io/documentation/operators/groupby.html\">ReactiveX documentation: GroupBy</a>\n * @since 4.0.0\n */",
            "/**\n * 按 key 分组的 {@link Streamable}，可通过 {@link #getKey()} 获取 key 值。\n * @param <K>\n *            key 的类型，可为 null\n * @param <T>\n *            {@code GroupedStreamable} 发射元素的类型\n * @see Streamable#groupBy(io.reactivex.rxjava4.functions.Function)\n * @see <a href=\"http://reactivex.io/documentation/operators/groupby.html\">ReactiveX documentation: GroupBy</a>\n * @since 4.0.0\n */",
        ),
        (
            "    /**\n     * Constructs a GroupedStreamable with the given key.\n     * @param key the key\n     */",
            "    /**\n     * 使用给定 key 构造 GroupedStreamable。\n     * @param key key\n     */",
        ),
        (
            "    /**\n     * Returns the key that identifies the group of items emitted by this {@code GroupedStreamable}.\n     *\n     * @return the key that the items emitted by this {@code GroupedStreamable} were grouped by\n     */",
            "    /**\n     * 返回标识本 {@code GroupedStreamable} 所发射元素分组的 key。\n     *\n     * @return 本 {@code GroupedStreamable} 发射元素所依据的分组 key\n     */",
        ),
    ],
    "MaybeConverter.java": [
        (
            "/**\n * Convenience interface and callback used by the {@link Maybe#to} operator to turn a {@link Maybe} into another\n * value fluently.\n * <p>History: 2.1.7 - experimental\n * @param <T> the upstream type\n * @param <R> the output type\n * @since 2.2\n */",
            "/**\n * {@link Maybe#to} 算子使用的便捷接口与回调，用于将 {@link Maybe} 流畅地转换为其他值。\n * <p>History: 2.1.7 - experimental\n * @param <T> 上游类型\n * @param <R> 输出类型\n * @since 2.2\n */",
        ),
        (
            "    /**\n     * Applies a function to the upstream {@link Maybe} and returns a converted value of type {@code R}.\n     *\n     * @param upstream the upstream {@code Maybe} instance\n     * @return the converted value\n     */",
            "    /**\n     * 对上游 {@link Maybe} 应用函数并返回类型为 {@code R} 的转换值。\n     *\n     * @param upstream 上游 {@code Maybe} 实例\n     * @return 转换后的值\n     */",
        ),
    ],
    "MaybeEmitter.java": [
        (
            "/**\n * Abstraction over an RxJava {@link MaybeObserver} that allows associating\n * a resource with it.\n * <p>\n * All methods are safe to call from multiple threads, but note that there is no guarantee\n * whose terminal event will win and get delivered to the downstream.\n * <p>\n * Calling {@link #onSuccess(Object)} or {@link #onComplete()} multiple times has no effect.\n * Calling {@link #onError(Throwable)} multiple times or after the other two will route the\n * exception into the global error handler via {@link io.reactivex.rxjava4.plugins.RxJavaPlugins#onError(Throwable)}.\n * <p>\n * The emitter allows the registration of a single resource, in the form of a {@link Disposable}\n * or {@link Cancellable} via {@link #setDisposable(Disposable)} or {@link #setCancellable(Cancellable)}\n * respectively. The emitter implementations will dispose/cancel this instance when the\n * downstream cancels the flow or after the event generator logic calls {@link #onSuccess(Object)},\n * {@link #onError(Throwable)}, {@link #onComplete()} or when {@link #tryOnError(Throwable)} succeeds.\n * <p>\n * Only one {@code Disposable} or {@code Cancellable} object can be associated with the emitter at\n * a time. Calling either {@code set} method will dispose/cancel any previous object. If there\n * is a need for handling multiple resources, one can create a {@link io.reactivex.rxjava4.disposables.CompositeDisposable}\n * and associate that with the emitter instead.\n * <p>\n * The {@link Cancellable} is logically equivalent to {@code Disposable} but allows using cleanup logic that can\n * throw a checked exception (such as many {@code close()} methods on Java IO components). Since\n * the release of resources happens after the terminal events have been delivered or the sequence gets\n * cancelled, exceptions throw within {@code Cancellable} are routed to the global error handler via\n * {@link io.reactivex.rxjava4.plugins.RxJavaPlugins#onError(Throwable)}.\n *\n * @param <T> the value type to emit\n */",
            "/**\n * RxJava {@link MaybeObserver} 的抽象，允许关联资源。\n * <p>\n * 所有方法均可从多线程安全调用，但无法保证哪个终止事件会胜出并交付给下游。\n * <p>\n * 多次调用 {@link #onSuccess(Object)} 或 {@link #onComplete()} 无效果。\n * 多次调用 {@link #onError(Throwable)} 或在上述两者之后调用，会将异常通过\n * {@link io.reactivex.rxjava4.plugins.RxJavaPlugins#onError(Throwable)} 路由到全局错误处理器。\n * <p>\n * emitter 允许通过 {@link #setDisposable(Disposable)} 或 {@link #setCancellable(Cancellable)}\n * 分别注册单个 {@link Disposable} 或 {@link Cancellable} 资源。当下游取消流，或事件生成逻辑调用\n * {@link #onSuccess(Object)}、{@link #onError(Throwable)}、{@link #onComplete()}，\n * 或 {@link #tryOnError(Throwable)} 成功后，emitter 实现将 dispose/cancel 该实例。\n * <p>\n * 同一时刻 emitter 只能关联一个 {@code Disposable} 或 {@code Cancellable} 对象。\n * 调用任一 {@code set} 方法会 dispose/cancel 先前的对象。若需管理多个资源，\n * 可创建 {@link io.reactivex.rxjava4.disposables.CompositeDisposable} 并关联到 emitter。\n * <p>\n * {@link Cancellable} 在逻辑上等价于 {@code Disposable}，但允许使用可能抛出 checked 异常的清理逻辑\n *（例如许多 Java IO 组件的 {@code close()} 方法）。由于资源释放在终端事件交付后或序列取消后进行，\n * {@code Cancellable} 内抛出的异常将通过 {@link io.reactivex.rxjava4.plugins.RxJavaPlugins#onError(Throwable)}\n * 路由到全局错误处理器。\n *\n * @param <T> 发射的值类型\n */",
        ),
        (
            "    /**\n     * Signal a success value.\n     * @param t the value, not null\n     */",
            "    /**\n     * 发射成功值。\n     * @param t 值，不可为 null\n     */",
        ),
        (
            "    /**\n     * Signal an exception.\n     * @param t the exception, not {@code null}\n     */",
            "    /**\n     * 发射异常。\n     * @param t 异常，不可为 {@code null}\n     */",
        ),
        (
            "    /**\n     * Signal the completion.\n     */",
            "    /**\n     * 发射完成信号。\n     */",
        ),
        (
            "    /**\n     * Sets a {@link Disposable} on this emitter; any previous {@code Disposable}\n     * or {@link Cancellable} will be disposed/cancelled.\n     * <p>This method is thread-safe.\n     * @param d the disposable, {@code null} is allowed\n     */",
            "    /**\n     * 在本 emitter 上设置 {@link Disposable}；任何先前的 {@code Disposable}\n     * 或 {@link Cancellable} 将被 dispose/cancel。\n     * <p>本方法为线程安全。\n     * @param d disposable，允许为 {@code null}\n     */",
        ),
        (
            "    /**\n     * Sets a {@link Cancellable} on this emitter; any previous {@link Disposable}\n     * or {@code Cancellable} will be disposed/cancelled.\n     * <p>This method is thread-safe.\n     * @param c the {@code Cancellable} resource, {@code null} is allowed\n     */",
            "    /**\n     * 在本 emitter 上设置 {@link Cancellable}；任何先前的 {@link Disposable}\n     * 或 {@code Cancellable} 将被 dispose/cancel。\n     * <p>本方法为线程安全。\n     * @param c {@code Cancellable} 资源，允许为 {@code null}\n     */",
        ),
        (
            "    /**\n     * Returns true if the downstream disposed the sequence or the\n     * emitter was terminated via {@link #onSuccess(Object)}, {@link #onError(Throwable)},\n     * {@link #onComplete} or a\n     * successful {@link #tryOnError(Throwable)}.\n     * <p>This method is thread-safe.\n     * @return true if the downstream disposed the sequence or the emitter was terminated\n     */",
            "    /**\n     * 若下游已 dispose 序列，或通过 {@link #onSuccess(Object)}、{@link #onError(Throwable)}、\n     * {@link #onComplete} 或成功的 {@link #tryOnError(Throwable)} 终止 emitter，则返回 true。\n     * <p>本方法为线程安全。\n     * @return 若下游已 dispose 序列或 emitter 已终止则为 true\n     */",
        ),
        (
            "    /**\n     * Attempts to emit the specified {@link Throwable} error if the downstream\n     * hasn't cancelled the sequence or is otherwise terminated, returning false\n     * if the emission is not allowed to happen due to lifecycle restrictions.\n     * <p>\n     * Unlike {@link #onError(Throwable)}, the {@link io.reactivex.rxjava4.plugins.RxJavaPlugins#onError(Throwable) RxjavaPlugins.onError}\n     * is not called if the error could not be delivered.\n     * <p>History: 2.1.1 - experimental\n     * @param t the {@code Throwable} error to signal if possible\n     * @return true if successful, false if the downstream is not able to accept further\n     * events\n     * @since 2.2\n     */",
            "    /**\n     * 若下游未取消序列且未以其他方式终止，尝试发射指定 {@link Throwable} 错误；\n     * 若因生命周期限制不允许发射则返回 false。\n     * <p>\n     * 与 {@link #onError(Throwable)} 不同，若错误无法交付，不会调用\n     * {@link io.reactivex.rxjava4.plugins.RxJavaPlugins#onError(Throwable) RxjavaPlugins.onError}。\n     * <p>History: 2.1.1 - experimental\n     * @param t 若可能则发射的 {@code Throwable} 错误\n     * @return 成功则为 true；若下游无法接受更多事件则为 false\n     * @since 2.2\n     */",
        ),
    ],
    "MaybeObserver.java": [
        (
            "/**\n * Provides a mechanism for receiving push-based notification of a single value, an error or completion without any value.\n * <p>\n * When a {@code MaybeObserver} is subscribed to a {@link MaybeSource} through the {@link MaybeSource#subscribe(MaybeObserver)} method,\n * the {@code MaybeSource} calls {@link #onSubscribe(Disposable)}  with a {@link Disposable} that allows\n * disposing the sequence at any time. A well-behaved\n * {@code MaybeSource} will call a {@code MaybeObserver}'s {@link #onSuccess(Object)}, {@link #onError(Throwable)}\n * or {@link #onComplete()} method exactly once as they are considered mutually exclusive <strong>terminal signals</strong>.\n * <p>\n * Calling the {@code MaybeObserver}'s method must happen in a serialized fashion, that is, they must not\n * be invoked concurrently by multiple threads in an overlapping fashion and the invocation pattern must\n * adhere to the following protocol:\n * <pre><code>    onSubscribe (onSuccess | onError | onComplete)?</code></pre>\n * <p>\n * Note that unlike with the {@code Observable} protocol, {@link #onComplete()} is not called after the success item has been\n * signaled via {@link #onSuccess(Object)}.\n * <p>\n * Subscribing a {@code MaybeObserver} to multiple {@code MaybeSource}s is not recommended. If such reuse\n * happens, it is the duty of the {@code MaybeObserver} implementation to be ready to receive multiple calls to\n * its methods and ensure proper concurrent behavior of its business logic.\n * <p>\n * Calling {@link #onSubscribe(Disposable)}, {@link #onSuccess(Object)} or {@link #onError(Throwable)} with a\n * {@code null} argument is forbidden.\n * <p>\n * The implementations of the {@code onXXX} methods should avoid throwing runtime exceptions other than the following cases:\n * <ul>\n * <li>If the argument is {@code null}, the methods can throw a {@code NullPointerException}.\n * Note though that RxJava prevents {@code null}s to enter into the flow and thus there is generally no\n * need to check for nulls in flows assembled from standard sources and intermediate operators.\n * </li>\n * <li>If there is a fatal error (such as {@code VirtualMachineError}).</li>\n * </ul>\n * @see <a href=\"http://reactivex.io/documentation/observable.html\">ReactiveX documentation: Observable</a>\n * @param <T>\n *          the type of item the MaybeObserver expects to observe\n * @since 2.0\n */",
            "/**\n * 提供接收单个值、错误或无值完成推送通知的机制。\n * <p>\n * 当 {@code MaybeObserver} 通过 {@link MaybeSource#subscribe(MaybeObserver)} 订阅\n * {@link MaybeSource} 时，{@code MaybeSource} 会调用 {@link #onSubscribe(Disposable)}，\n * 传入可在任意时刻取消序列的 {@link Disposable}。\n * 行为良好的 {@code MaybeSource} 会恰好调用一次 {@link #onSuccess(Object)}、\n * {@link #onError(Throwable)} 或 {@link #onComplete()}，三者互斥，视为<strong>终止信号</strong>。\n * <p>\n * 对 {@code MaybeObserver} 方法的调用须串行化：不得多线程重叠调用，且须遵循下列协议：\n * <pre><code>    onSubscribe (onSuccess | onError | onComplete)?</code></pre>\n * <p>\n * 注意：与 {@code Observable} 协议不同，通过 {@link #onSuccess(Object)} 发出成功项后\n * 不会调用 {@link #onComplete()}。\n * <p>\n * 不建议同一 {@code MaybeObserver} 订阅多个 {@code MaybeSource}。\n * 若确需复用，实现须能处理多次回调并保证业务逻辑的并发正确性。\n * <p>\n * 以 {@code null} 参数调用 {@link #onSubscribe(Disposable)}、{@link #onSuccess(Object)}\n * 或 {@link #onError(Throwable)} 是禁止的。\n * <p>\n * {@code onXXX} 方法实现应避免抛出运行时异常，下列情况除外：\n * <ul>\n * <li>参数为 {@code null} 时可抛出 {@code NullPointerException}。\n * 注意 RxJava 会阻止 {@code null} 进入流，因此由标准源与中间算子组装的流通常无需空值检查。\n * </li>\n * <li>发生致命错误（如 {@code VirtualMachineError}）时。</li>\n * </ul>\n * @see <a href=\"http://reactivex.io/documentation/observable.html\">ReactiveX documentation: Observable</a>\n * @param <T>\n *          {@code MaybeObserver} 期望观察的元素类型\n * @since 2.0\n */",
        ),
        (
            "    /**\n     * Provides the {@link MaybeObserver} with the means of cancelling (disposing) the\n     * connection (channel) with the {@link Maybe} in both\n     * synchronous (from within {@code onSubscribe(Disposable)} itself) and asynchronous manner.\n     * @param d the {@link Disposable} instance whose {@link Disposable#dispose()} can\n     * be called anytime to cancel the connection\n     */",
            "    /**\n     * 为 {@link MaybeObserver} 提供同步（在 {@code onSubscribe(Disposable)} 内）\n     * 与异步取消（dispose）与 {@link Maybe} 连接（通道）的手段。\n     * @param d 可随时调用 {@link Disposable#dispose()} 以取消连接的 {@link Disposable} 实例\n     */",
        ),
        (
            "    /**\n     * Notifies the {@link MaybeObserver} with one item and that the {@link Maybe} has finished sending\n     * push-based notifications.\n     * <p>\n     * The {@link Maybe} will not call this method if it calls {@link #onError}.\n     *\n     * @param t\n     *          the item emitted by the {@code Maybe}\n     */",
            "    /**\n     * 向 {@link MaybeObserver} 通知一个元素，并表明 {@link Maybe} 已完成推送通知。\n     * <p>\n     * 若 {@link Maybe} 调用了 {@link #onError}，则不会调用本方法。\n     *\n     * @param t\n     *          {@code Maybe} 发射的元素\n     */",
        ),
        (
            "    /**\n     * Notifies the {@link MaybeObserver} that the {@link Maybe} has experienced an error condition.\n     * <p>\n     * If the {@link Maybe} calls this method, it will not thereafter call {@link #onSuccess}.\n     *\n     * @param e\n     *          the exception encountered by the {@code Maybe}\n     */",
            "    /**\n     * 通知 {@link MaybeObserver} {@link Maybe} 遇到错误。\n     * <p>\n     * 若 {@link Maybe} 调用本方法，则此后不会调用 {@link #onSuccess}。\n     *\n     * @param e\n     *          {@code Maybe} 遇到的异常\n     */",
        ),
        (
            "    /**\n     * Called once the deferred computation completes normally.\n     */",
            "    /**\n     * 延迟计算正常完成时调用一次。\n     */",
        ),
    ],
    "MaybeOnSubscribe.java": [
        (
            "/**\n * A functional interface that has a {@code subscribe()} method that receives\n * a {@link MaybeEmitter} instance that allows pushing\n * an event in a cancellation-safe manner.\n *\n * @param <T> the value type pushed\n */",
            "/**\n * 具有 {@code subscribe()} 方法的函数式接口，接收 {@link MaybeEmitter} 实例，\n * 以取消安全的方式推送事件。\n *\n * @param <T> 推送的值类型\n */",
        ),
        (
            "    /**\n     * Called for each {@link MaybeObserver} that subscribes.\n     * @param emitter the safe emitter instance, never {@code null}\n     * @throws Throwable on error\n     */",
            "    /**\n     * 对每个订阅的 {@link MaybeObserver} 调用。\n     * @param emitter 安全的 emitter 实例，永不为 {@code null}\n     * @throws Throwable 出错时抛出\n     */",
        ),
    ],
    "MaybeOperator.java": [
        (
            "/**\n * Interface to map/wrap a downstream {@link MaybeObserver} to an upstream {@code MaybeObserver}.\n *\n * @param <Downstream> the value type of the downstream\n * @param <Upstream> the value type of the upstream\n */",
            "/**\n * 将下游 {@link MaybeObserver} 映射/包装为上游 {@code MaybeObserver} 的接口。\n *\n * @param <Downstream> 下游值类型\n * @param <Upstream> 上游值类型\n */",
        ),
        (
            "    /**\n     * Applies a function to the child {@link MaybeObserver} and returns a new parent {@code MaybeObserver}.\n     * @param observer the child {@code MaybeObserver} instance\n     * @return the parent {@code MaybeObserver} instance\n     * @throws Throwable on failure\n     */",
            "    /**\n     * 对子 {@link MaybeObserver} 应用函数并返回新的父 {@code MaybeObserver}。\n     * @param observer 子 {@code MaybeObserver} 实例\n     * @return 父 {@code MaybeObserver} 实例\n     * @throws Throwable 失败时抛出\n     */",
        ),
    ],
    "MaybeSource.java": [
        (
            "/**\n * Represents a basic {@link Maybe} source base interface,\n * consumable via an {@link MaybeObserver}.\n * <p>\n * This class also serves the base type for custom operators wrapped into\n * Maybe via {@link Maybe#create(MaybeOnSubscribe)}.\n *\n * @param <T> the element type\n * @since 2.0\n */",
            "/**\n * 表示可通过 {@link MaybeObserver} 消费的基本 {@link Maybe} 源基础接口。\n * <p>\n * 本接口也是通过 {@link Maybe#create(MaybeOnSubscribe)} 包装为 Maybe 的\n * 自定义算子的基础类型。\n *\n * @param <T> 元素类型\n * @since 2.0\n */",
        ),
        (
            "    /**\n     * Subscribes the given {@link MaybeObserver} to this {@link MaybeSource} instance.\n     * @param observer the {@code MaybeObserver}, not {@code null}\n     * @throws NullPointerException if {@code observer} is {@code null}\n     */",
            "    /**\n     * 将给定 {@link MaybeObserver} 订阅到此 {@link MaybeSource} 实例。\n     * @param observer {@code MaybeObserver}，不可为 {@code null}\n     * @throws NullPointerException 若 {@code observer} 为 {@code null}\n     */",
        ),
    ],
    "MaybeTransformer.java": [
        (
            "/**\n * Interface to compose {@link Maybe}s.\n *\n * @param <Upstream> the upstream value type\n * @param <Downstream> the downstream value type\n */",
            "/**\n * 组合 {@link Maybe} 的接口。\n *\n * @param <Upstream> 上游值类型\n * @param <Downstream> 下游值类型\n */",
        ),
        (
            "    /**\n     * Applies a function to the upstream {@link Maybe} and returns a {@link MaybeSource} with\n     * optionally different element type.\n     * @param upstream the upstream {@code Maybe} instance\n     * @return the transformed {@code MaybeSource} instance\n     */",
            "    /**\n     * 对上游 {@link Maybe} 应用函数并返回元素类型可能不同的 {@link MaybeSource}。\n     * @param upstream 上游 {@code Maybe} 实例\n     * @return 转换后的 {@code MaybeSource} 实例\n     */",
        ),
    ],
    "Notification.java": [
        (
            "/**\n * Represents the reactive signal types: {@code onNext}, {@code onError} and {@code onComplete} and\n * holds their parameter values (a value, a {@link Throwable}, nothing).\n * @param <T> the value type\n */",
            "/**\n * 表示 reactive 信号类型：{@code onNext}、{@code onError} 与 {@code onComplete}，\n * 并持有其参数值（一个值、{@link Throwable} 或空）。\n * @param <T> 值类型\n */",
        ),
        (
            "    /** Not meant to be implemented externally.\n     * @param value the value to carry around in the notification, not {@code null}\n     */",
            "    /** 不供外部实现。\n     * @param value notification 中携带的值，不可为 {@code null}\n     */",
        ),
        (
            "    /**\n     * Returns true if this notification is an {@code onComplete} signal.\n     * @return true if this notification is an {@code onComplete} signal\n     */",
            "    /**\n     * 若本 notification 为 {@code onComplete} 信号则返回 true。\n     * @return 若本 notification 为 {@code onComplete} 信号则为 true\n     */",
        ),
        (
            "    /**\n     * Returns true if this notification is an {@code onError} signal and\n     * {@link #getError()} returns the contained {@link Throwable}.\n     * @return true if this notification is an {@code onError} signal\n     * @see #getError()\n     */",
            "    /**\n     * 若本 notification 为 {@code onError} 信号且 {@link #getError()} 返回所含 {@link Throwable} 则返回 true。\n     * @return 若本 notification 为 {@code onError} 信号则为 true\n     * @see #getError()\n     */",
        ),
        (
            "    /**\n     * Returns true if this notification is an {@code onNext} signal and\n     * {@link #getValue()} returns the contained value.\n     * @return true if this notification is an {@code onNext} signal\n     * @see #getValue()\n     */",
            "    /**\n     * 若本 notification 为 {@code onNext} 信号且 {@link #getValue()} 返回所含值则返回 true。\n     * @return 若本 notification 为 {@code onNext} 信号则为 true\n     * @see #getValue()\n     */",
        ),
        (
            "    /**\n     * Returns the contained value if this notification is an {@code onNext}\n     * signal, null otherwise.\n     * @return the value contained or null\n     * @see #isOnNext()\n     */",
            "    /**\n     * 若本 notification 为 {@code onNext} 信号则返回所含值，否则返回 null。\n     * @return 所含值或 null\n     * @see #isOnNext()\n     */",
        ),
        (
            "    /**\n     * Returns the container {@link Throwable} error if this notification is an {@code onError}\n     * signal, null otherwise.\n     * @return the {@code Throwable} error contained or {@code null}\n     * @see #isOnError()\n     */",
            "    /**\n     * 若本 notification 为 {@code onError} 信号则返回所含 {@link Throwable} 错误，否则返回 null。\n     * @return 所含 {@code Throwable} 错误或 {@code null}\n     * @see #isOnError()\n     */",
        ),
        (
            "    /**\n     * Constructs an onNext notification containing the given value.\n     * @param <T> the value type\n     * @param value the value to carry around in the notification, not {@code null}\n     * @return the new Notification instance\n     * @throws NullPointerException if value is {@code null}\n     */",
            "    /**\n     * 构造包含给定值的 onNext notification。\n     * @param <T> 值类型\n     * @param value notification 中携带的值，不可为 {@code null}\n     * @return 新的 Notification 实例\n     * @throws NullPointerException 若 value 为 {@code null}\n     */",
        ),
        (
            "    /**\n     * Constructs an onError notification containing the error.\n     * @param <T> the value type\n     * @param error the error Throwable to carry around in the notification, not null\n     * @return the new Notification instance\n     * @throws NullPointerException if error is {@code null}\n     */",
            "    /**\n     * 构造包含错误的 onError notification。\n     * @param <T> 值类型\n     * @param error notification 中携带的错误 Throwable，不可为 null\n     * @return 新的 Notification 实例\n     * @throws NullPointerException 若 error 为 {@code null}\n     */",
        ),
        (
            "    /**\n     * Returns the empty and stateless shared instance of a notification representing\n     * an {@code onComplete} signal.\n     * @param <T> the target value type\n     * @return the shared Notification instance representing an {@code onComplete} signal\n     */",
            "    /**\n     * 返回表示 {@code onComplete} 信号的无状态共享空 notification 实例。\n     * @param <T> 目标值类型\n     * @return 表示 {@code onComplete} 信号的共享 Notification 实例\n     */",
        ),
        (
            "    /** The singleton instance for createOnComplete. */",
            "    /** createOnComplete 的单例实例。 */",
        ),
    ],
    "ObservableConverter.java": [
        (
            "/**\n * Convenience interface and callback used by the {@link Observable#to} operator to turn an {@link Observable} into another\n * value fluently.\n * <p>History: 2.1.7 - experimental\n * @param <T> the upstream type\n * @param <R> the output type\n * @since 2.2\n */",
            "/**\n * {@link Observable#to} 算子使用的便捷接口与回调，用于将 {@link Observable} 流畅地转换为其他值。\n * <p>History: 2.1.7 - experimental\n * @param <T> 上游类型\n * @param <R> 输出类型\n * @since 2.2\n */",
        ),
        (
            "    /**\n     * Applies a function to the upstream {@link Observable} and returns a converted value of type {@code R}.\n     *\n     * @param upstream the upstream {@code Observable} instance\n     * @return the converted value\n     */",
            "    /**\n     * 对上游 {@link Observable} 应用函数并返回类型为 {@code R} 的转换值。\n     *\n     * @param upstream 上游 {@code Observable} 实例\n     * @return 转换后的值\n     */",
        ),
    ],
    "ObservableEmitter.java": [
        (
            "/**\n * Abstraction over an RxJava {@link Observer} that allows associating\n * a resource with it.\n * <p>\n * The {@link #onNext(Object)}, {@link #onError(Throwable)}, {@link #tryOnError(Throwable)}\n * and {@link #onComplete()} methods should be called in a sequential manner, just like the\n * {@link Observer}'s methods should be.\n * Use the {@code ObservableEmitter} the {@link #serialize()} method returns instead of the original\n * {@code ObservableEmitter} instance provided by the generator routine if you want to ensure this.\n * The other methods are thread-safe.\n * <p>\n * The emitter allows the registration of a single resource, in the form of a {@link Disposable}\n * or {@link Cancellable} via {@link #setDisposable(Disposable)} or {@link #setCancellable(Cancellable)}\n * respectively. The emitter implementations will dispose/cancel this instance when the\n * downstream cancels the flow or after the event generator logic calls {@link #onError(Throwable)},\n * {@link #onComplete()} or when {@link #tryOnError(Throwable)} succeeds.\n * <p>\n * Only one {@code Disposable} or {@code Cancellable} object can be associated with the emitter at\n * a time. Calling either {@code set} method will dispose/cancel any previous object. If there\n * is a need for handling multiple resources, one can create a {@link io.reactivex.rxjava4.disposables.CompositeDisposable}\n * and associate that with the emitter instead.\n * <p>\n * The {@link Cancellable} is logically equivalent to {@code Disposable} but allows using cleanup logic that can\n * throw a checked exception (such as many {@code close()} methods on Java IO components). Since\n * the release of resources happens after the terminal events have been delivered or the sequence gets\n * cancelled, exceptions throw within {@code Cancellable} are routed to the global error handler via\n * {@link io.reactivex.rxjava4.plugins.RxJavaPlugins#onError(Throwable)}.\n *\n * @param <T> the value type to emit\n */",
            "/**\n * RxJava {@link Observer} 的抽象，允许关联资源。\n * <p>\n * {@link #onNext(Object)}、{@link #onError(Throwable)}、{@link #tryOnError(Throwable)}\n * 与 {@link #onComplete()} 方法应像 {@link Observer} 的方法一样顺序调用。\n * 若要保证这一点，请使用 {@link #serialize()} 返回的 {@code ObservableEmitter}\n * 而非生成器例程提供的原始 {@code ObservableEmitter} 实例。其他方法为线程安全。\n * <p>\n * emitter 允许通过 {@link #setDisposable(Disposable)} 或 {@link #setCancellable(Cancellable)}\n * 分别注册单个 {@link Disposable} 或 {@link Cancellable} 资源。当下游取消流，或事件生成逻辑调用\n * {@link #onError(Throwable)}、{@link #onComplete()}，或 {@link #tryOnError(Throwable)} 成功后，\n * emitter 实现将 dispose/cancel 该实例。\n * <p>\n * 同一时刻 emitter 只能关联一个 {@code Disposable} 或 {@code Cancellable} 对象。\n * 调用任一 {@code set} 方法会 dispose/cancel 先前的对象。若需管理多个资源，\n * 可创建 {@link io.reactivex.rxjava4.disposables.CompositeDisposable} 并关联到 emitter。\n * <p>\n * {@link Cancellable} 在逻辑上等价于 {@code Disposable}，但允许使用可能抛出 checked 异常的清理逻辑\n *（例如许多 Java IO 组件的 {@code close()} 方法）。由于资源释放在终端事件交付后或序列取消后进行，\n * {@code Cancellable} 内抛出的异常将通过 {@link io.reactivex.rxjava4.plugins.RxJavaPlugins#onError(Throwable)}\n * 路由到全局错误处理器。\n *\n * @param <T> 发射的值类型\n */",
        ),
        (
            "    /**\n     * Sets a {@link Disposable} on this emitter; any previous {@code Disposable}\n     * or {@link Cancellable} will be disposed/cancelled.\n     * <p>This method is thread-safe.\n     * @param d the {@code Disposable}, {@code null} is allowed\n     */",
            "    /**\n     * 在本 emitter 上设置 {@link Disposable}；任何先前的 {@code Disposable}\n     * 或 {@link Cancellable} 将被 dispose/cancel。\n     * <p>本方法为线程安全。\n     * @param d {@code Disposable}，允许为 {@code null}\n     */",
        ),
        (
            "    /**\n     * Sets a {@link Cancellable} on this emitter; any previous {@link Disposable}\n     * or {@code Cancellable} will be disposed/cancelled.\n     * <p>This method is thread-safe.\n     * @param c the {@code Cancellable} resource, {@code null} is allowed\n     */",
            "    /**\n     * 在本 emitter 上设置 {@link Cancellable}；任何先前的 {@link Disposable}\n     * 或 {@code Cancellable} 将被 dispose/cancel。\n     * <p>本方法为线程安全。\n     * @param c {@code Cancellable} 资源，允许为 {@code null}\n     */",
        ),
        (
            "    /**\n     * Returns true if the downstream disposed the sequence or the\n     * emitter was terminated via {@link #onError(Throwable)}, {@link #onComplete} or a\n     * successful {@link #tryOnError(Throwable)}.\n     * <p>This method is thread-safe.\n     * @return true if the downstream disposed the sequence or the emitter was terminated\n     */",
            "    /**\n     * 若下游已 dispose 序列，或通过 {@link #onError(Throwable)}、{@link #onComplete}\n     * 或成功的 {@link #tryOnError(Throwable)} 终止 emitter，则返回 true。\n     * <p>本方法为线程安全。\n     * @return 若下游已 dispose 序列或 emitter 已终止则为 true\n     */",
        ),
        (
            "    /**\n     * Ensures that calls to {@code onNext}, {@code onError} and {@code onComplete} are properly serialized.\n     * @return the serialized {@link ObservableEmitter}\n     */",
            "    /**\n     * 确保对 {@code onNext}、{@code onError} 与 {@code onComplete} 的调用被正确串行化。\n     * @return 串行化后的 {@link ObservableEmitter}\n     */",
        ),
        (
            "    /**\n     * Attempts to emit the specified {@link Throwable} error if the downstream\n     * hasn't cancelled the sequence or is otherwise terminated, returning false\n     * if the emission is not allowed to happen due to lifecycle restrictions.\n     * <p>\n     * Unlike {@link #onError(Throwable)}, the {@link io.reactivex.rxjava4.plugins.RxJavaPlugins#onError(Throwable) RxjavaPlugins.onError}\n     * is not called if the error could not be delivered.\n     * <p>History: 2.1.1 - experimental\n     * @param t the {@code Throwable} error to signal if possible\n     * @return true if successful, false if the downstream is not able to accept further\n     * events\n     * @since 2.2\n     */",
            "    /**\n     * 若下游未取消序列且未以其他方式终止，尝试发射指定 {@link Throwable} 错误；\n     * 若因生命周期限制不允许发射则返回 false。\n     * <p>\n     * 与 {@link #onError(Throwable)} 不同，若错误无法交付，不会调用\n     * {@link io.reactivex.rxjava4.plugins.RxJavaPlugins#onError(Throwable) RxjavaPlugins.onError}。\n     * <p>History: 2.1.1 - experimental\n     * @param t 若可能则发射的 {@code Throwable} 错误\n     * @return 成功则为 true；若下游无法接受更多事件则为 false\n     * @since 2.2\n     */",
        ),
    ],
    "ObservableOnSubscribe.java": [
        (
            "/**\n * A functional interface that has a {@code subscribe()} method that receives\n * an {@link ObservableEmitter} instance that allows pushing\n * events in a cancellation-safe manner.\n *\n * @param <T> the value type pushed\n */",
            "/**\n * 具有 {@code subscribe()} 方法的函数式接口，接收 {@link ObservableEmitter} 实例，\n * 以取消安全的方式推送事件。\n *\n * @param <T> 推送的值类型\n */",
        ),
        (
            "    /**\n     * Called for each {@link Observer} that subscribes.\n     * @param emitter the safe emitter instance, never {@code null}\n     * @throws Throwable on error\n     */",
            "    /**\n     * 对每个订阅的 {@link Observer} 调用。\n     * @param emitter 安全的 emitter 实例，永不为 {@code null}\n     * @throws Throwable 出错时抛出\n     */",
        ),
    ],
    "ObservableOperator.java": [
        (
            "/**\n * Interface to map/wrap a downstream {@link Observer} to an upstream {@code Observer}.\n *\n * @param <Downstream> the value type of the downstream\n * @param <Upstream> the value type of the upstream\n */",
            "/**\n * 将下游 {@link Observer} 映射/包装为上游 {@code Observer} 的接口。\n *\n * @param <Downstream> 下游值类型\n * @param <Upstream> 上游值类型\n */",
        ),
        (
            "    /**\n     * Applies a function to the child {@link Observer} and returns a new parent {@code Observer}.\n     * @param observer the child {@code Observer} instance\n     * @return the parent {@code Observer} instance\n     * @throws Throwable on failure\n     */",
            "    /**\n     * 对子 {@link Observer} 应用函数并返回新的父 {@code Observer}。\n     * @param observer 子 {@code Observer} 实例\n     * @return 父 {@code Observer} 实例\n     * @throws Throwable 失败时抛出\n     */",
        ),
    ],
    "ObservableSource.java": [
        (
            "/**\n * Represents a basic, non-backpressured {@link Observable} source base interface,\n * consumable via an {@link Observer}.\n *\n * @param <T> the element type\n * @since 2.0\n */",
            "/**\n * 表示可通过 {@link Observer} 消费的基本、无背压 {@link Observable} 源基础接口。\n *\n * @param <T> 元素类型\n * @since 2.0\n */",
        ),
        (
            "    /**\n     * Subscribes the given {@link Observer} to this {@link ObservableSource} instance.\n     * @param observer the {@code Observer}, not {@code null}\n     * @throws NullPointerException if {@code observer} is {@code null}\n     */",
            "    /**\n     * 将给定 {@link Observer} 订阅到此 {@link ObservableSource} 实例。\n     * @param observer {@code Observer}，不可为 {@code null}\n     * @throws NullPointerException 若 {@code observer} 为 {@code null}\n     */",
        ),
    ],
    "ObservableTransformer.java": [
        (
            "/**\n * Interface to compose {@link Observable}s.\n *\n * @param <Upstream> the upstream value type\n * @param <Downstream> the downstream value type\n */",
            "/**\n * 组合 {@link Observable} 的接口。\n *\n * @param <Upstream> 上游值类型\n * @param <Downstream> 下游值类型\n */",
        ),
        (
            "    /**\n     * Applies a function to the upstream {@link Observable} and returns an {@link ObservableSource} with\n     * optionally different element type.\n     * @param upstream the upstream {@code Observable} instance\n     * @return the transformed {@code ObservableSource} instance\n     */",
            "    /**\n     * 对上游 {@link Observable} 应用函数并返回元素类型可能不同的 {@link ObservableSource}。\n     * @param upstream 上游 {@code Observable} 实例\n     * @return 转换后的 {@code ObservableSource} 实例\n     */",
        ),
    ],
}


def has_chinese(text: str) -> bool:
    return bool(re.search(r"[\u4e00-\u9fff]", text))


def apply_replacements(text: str, replacements: list[tuple[str, str]]) -> str:
    for old, new in replacements:
        if old not in text:
            raise ValueError(f"Pattern not found:\n{old[:120]}...")
        text = text.replace(old, new, 1)
    return text


def mark_queue_done(files: list[str]) -> None:
    done_path = QUEUE / "done.txt"
    pending_path = QUEUE / "pending.txt"
    done = [ln.strip() for ln in done_path.read_text(encoding="utf-8").splitlines() if ln.strip()]
    pending = [ln.strip() for ln in pending_path.read_text(encoding="utf-8").splitlines() if ln.strip()]
    done_set = set(done)
    pending_set = set(pending)
    for rel in files:
        if rel not in done_set:
            done.append(rel)
            done_set.add(rel)
        pending_set.discard(rel)
    done_path.write_text(("\n".join(done) + ("\n" if done else "")), encoding="utf-8")
    pending = [ln for ln in pending if ln in pending_set]
    pending_path.write_text(("\n".join(pending) + ("\n" if pending else "")), encoding="utf-8")
    batch_path = QUEUE / "batch.json"
    batch = json.loads(batch_path.read_text(encoding="utf-8"))
    batch["done"] = len(done)
    batch["remaining_pending"] = len(pending)
    batch_path.write_text(json.dumps(batch, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def annotate_file(rel: str) -> None:
    name = Path(rel).name
    src = ORIGINAL / rel
    dst = ANALYZED / rel
    if not src.exists():
        raise FileNotFoundError(f"missing original: {rel}")
    reps = FILE_REPLACEMENTS.get(name, [])
    if not reps:
        raise ValueError(f"NO_REPLACEMENTS: {rel}")
    dst.parent.mkdir(parents=True, exist_ok=True)
    if not dst.exists() or not has_chinese(dst.read_text(encoding="utf-8")):
        shutil.copy2(src, dst)
    text = dst.read_text(encoding="utf-8")
    text = apply_replacements(text, reps)
    cn = len(re.findall(r"[\u4e00-\u9fff]", text))
    lic = "Licensed under the Apache License" in text
    if cn < 10 or not lic:
        raise ValueError(f"VALIDATION cn={cn} lic={lic}: {rel}")
    dst.write_text(text, encoding="utf-8")


def main() -> int:
    failures: list[str] = []
    ok = 0
    for rel in BATCH_FILES:
        try:
            annotate_file(rel)
            ok += 1
            print(f"OK {rel}")
        except Exception as e:
            failures.append(f"{rel}: {e}")
            print(f"FAIL {rel}: {e}")
    if not failures:
        mark_queue_done(BATCH_FILES)
        print(f"Marked {len(BATCH_FILES)} files done in queue")
    print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False, indent=2))
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
