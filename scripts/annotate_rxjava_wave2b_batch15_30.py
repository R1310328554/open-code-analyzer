#!/usr/bin/env python3
"""Chinese-annotate RxJava 4.0.0-alpha-21 wave-2b [15:30]."""
from __future__ import annotations

import importlib.util
import json
import re
import shutil
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "rxjava/4.0.0-alpha-21"
ORIGINAL = VER / "original"
ANALYZED = VER / "analyzed"
QUEUE = VER / "_reports/class-queue"
BATCH_FILES = json.loads((QUEUE / "batch.json").read_text(encoding="utf-8"))["files"][15:30]


def _load_large() -> dict[str, list[tuple[str, str]]]:
    path = Path(__file__).with_name("annotate_rxjava_wave2b_large.py")
    spec = importlib.util.spec_from_file_location("rxjava_wave2b_large", path)
    if spec is None or spec.loader is None:
        return {}
    mod = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(mod)
    return getattr(mod, "LARGE_REPLACEMENTS", {})


FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
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
    "SingleConverter.java": [
        (
            "/**\n * Convenience interface and callback used by the {@link Single#to} operator to turn a {@link Single} into another\n * value fluently.\n * <p>History: 2.1.7 - experimental\n * @param <T> the upstream type\n * @param <R> the output type\n * @since 2.2\n */",
            "/**\n * {@link Single#to} 算子使用的便捷接口与回调，用于将 {@link Single} 流畅地转换为其他值。\n * <p>History: 2.1.7 - experimental\n * @param <T> 上游类型\n * @param <R> 输出类型\n * @since 2.2\n */",
        ),
        (
            "    /**\n     * Applies a function to the upstream {@link Single} and returns a converted value of type {@code R}.\n     *\n     * @param upstream the upstream {@code Single} instance\n     * @return the converted value\n     */",
            "    /**\n     * 对上游 {@link Single} 应用函数并返回类型为 {@code R} 的转换值。\n     *\n     * @param upstream 上游 {@code Single} 实例\n     * @return 转换后的值\n     */",
        ),
    ],
    "SingleEmitter.java": [
        (
            "/**\n * Abstraction over an RxJava {@link SingleObserver} that allows associating\n * a resource with it.\n * <p>\n * All methods are safe to call from multiple threads, but note that there is no guarantee\n * whose terminal event will win and get delivered to the downstream.\n * <p>\n * Calling {@link #onSuccess(Object)} multiple times has no effect.\n * Calling {@link #onError(Throwable)} multiple times or after {@code onSuccess} will route the\n * exception into the global error handler via {@link io.reactivex.rxjava4.plugins.RxJavaPlugins#onError(Throwable)}.\n * <p>\n * The emitter allows the registration of a single resource, in the form of a {@link Disposable}\n * or {@link Cancellable} via {@link #setDisposable(Disposable)} or {@link #setCancellable(Cancellable)}\n * respectively. The emitter implementations will dispose/cancel this instance when the\n * downstream cancels the flow or after the event generator logic calls {@link #onSuccess(Object)},\n * {@link #onError(Throwable)}, or when {@link #tryOnError(Throwable)} succeeds.\n * <p>\n * Only one {@code Disposable} or {@code Cancellable} object can be associated with the emitter at\n * a time. Calling either {@code set} method will dispose/cancel any previous object. If there\n * is a need for handling multiple resources, one can create a {@link io.reactivex.rxjava4.disposables.CompositeDisposable}\n * and associate that with the emitter instead.\n * <p>\n * The {@link Cancellable} is logically equivalent to {@code Disposable} but allows using cleanup logic that can\n * throw a checked exception (such as many {@code close()} methods on Java IO components). Since\n * the release of resources happens after the terminal events have been delivered or the sequence gets\n * cancelled, exceptions throw within {@code Cancellable} are routed to the global error handler via\n * {@link io.reactivex.rxjava4.plugins.RxJavaPlugins#onError(Throwable)}.\n *\n * @param <T> the value type to emit\n */",
            "/**\n * RxJava {@link SingleObserver} 的抽象，允许关联资源。\n * <p>\n * 所有方法可从多线程安全调用，但无法保证哪个终端事件会胜出并交付给下游。\n * <p>\n * 多次调用 {@link #onSuccess(Object)} 无效果。\n * 多次调用 {@link #onError(Throwable)} 或在 {@code onSuccess} 之后调用，\n * 会将异常通过 {@link io.reactivex.rxjava4.plugins.RxJavaPlugins#onError(Throwable)} 路由到全局错误处理器。\n * <p>\n * emitter 允许通过 {@link #setDisposable(Disposable)} 或 {@link #setCancellable(Cancellable)}\n * 分别注册单个 {@link Disposable} 或 {@link Cancellable} 资源。当下游取消流，或事件生成逻辑调用\n * {@link #onSuccess(Object)}、{@link #onError(Throwable)}，或 {@link #tryOnError(Throwable)} 成功后，\n * emitter 实现将 dispose/cancel 该实例。\n * <p>\n * 同一时刻 emitter 只能关联一个 {@code Disposable} 或 {@code Cancellable} 对象。\n * 调用任一 {@code set} 方法会 dispose/cancel 先前的对象。若需管理多个资源，\n * 可创建 {@link io.reactivex.rxjava4.disposables.CompositeDisposable} 并关联到 emitter。\n * <p>\n * {@link Cancellable} 在逻辑上等价于 {@code Disposable}，但允许使用可能抛出 checked 异常的清理逻辑\n *（例如许多 Java IO 组件的 {@code close()} 方法）。资源释放在终端事件交付后或序列取消后进行，\n * {@code Cancellable} 内抛出的异常将通过 {@link io.reactivex.rxjava4.plugins.RxJavaPlugins#onError(Throwable)}\n * 路由到全局错误处理器。\n *\n * @param <T> 发射的值类型\n */",
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
            "    /**\n     * Sets a {@link Disposable} on this emitter; any previous {@code Disposable}\n     * or {@link Cancellable} will be disposed/cancelled.\n     * <p>This method is thread-safe.\n     * @param d the {@code Disposable}, {@code null} is allowed\n     */",
            "    /**\n     * 在本 emitter 上设置 {@link Disposable}；任何先前的 {@code Disposable}\n     * 或 {@link Cancellable} 将被 dispose/cancel。\n     * <p>本方法为线程安全。\n     * @param d {@code Disposable}，允许为 {@code null}\n     */",
        ),
        (
            "    /**\n     * Sets a Cancellable on this emitter; any previous {@link Disposable}\n     * or {@link Cancellable} will be disposed/cancelled.\n     * <p>This method is thread-safe.\n     * @param c the {@code Cancellable} resource, {@code null} is allowed\n     */",
            "    /**\n     * 在本 emitter 上设置 Cancellable；任何先前的 {@link Disposable}\n     * 或 {@link Cancellable} 将被 dispose/cancel。\n     * <p>本方法为线程安全。\n     * @param c {@code Cancellable} 资源，允许为 {@code null}\n     */",
        ),
        (
            "    /**\n     * Returns true if the downstream disposed the sequence or the\n     * emitter was terminated via {@link #onSuccess(Object)}, {@link #onError(Throwable)},\n     * or a successful {@link #tryOnError(Throwable)}.\n     * <p>This method is thread-safe.\n     * @return true if the downstream disposed the sequence or the emitter was terminated\n     */",
            "    /**\n     * 若下游已 dispose 序列，或通过 {@link #onSuccess(Object)}、{@link #onError(Throwable)}\n     * 或成功的 {@link #tryOnError(Throwable)} 终止 emitter，则返回 true。\n     * <p>本方法为线程安全。\n     * @return 若下游已 dispose 序列或 emitter 已终止则为 true\n     */",
        ),
        (
            "    /**\n     * Attempts to emit the specified {@link Throwable} error if the downstream\n     * hasn't cancelled the sequence or is otherwise terminated, returning false\n     * if the emission is not allowed to happen due to lifecycle restrictions.\n     * <p>\n     * Unlike {@link #onError(Throwable)}, the {@link io.reactivex.rxjava4.plugins.RxJavaPlugins#onError(Throwable) RxjavaPlugins.onError}\n     * is not called if the error could not be delivered.\n     * <p>History: 2.1.1 - experimental\n     * @param t the throwable error to signal if possible\n     * @return true if successful, false if the downstream is not able to accept further\n     * events\n     * @since 2.2\n     */",
            "    /**\n     * 若下游未取消序列且未以其他方式终止，尝试发射指定 {@link Throwable} 错误；\n     * 若因生命周期限制不允许发射则返回 false。\n     * <p>\n     * 与 {@link #onError(Throwable)} 不同，若错误无法交付，不会调用\n     * {@link io.reactivex.rxjava4.plugins.RxJavaPlugins#onError(Throwable) RxjavaPlugins.onError}。\n     * <p>History: 2.1.1 - experimental\n     * @param t 若可能则发射的 throwable 错误\n     * @return 成功则为 true；若下游无法接受更多事件则为 false\n     * @since 2.2\n     */",
        ),
    ],
    "SingleObserver.java": [
        (
            "/**\n * Provides a mechanism for receiving push-based notification of a single value or an error.\n * <p>\n * When a {@code SingleObserver} is subscribed to a {@link SingleSource} through the {@link SingleSource#subscribe(SingleObserver)} method,\n * the {@code SingleSource} calls {@link #onSubscribe(Disposable)}  with a {@link Disposable} that allows\n * disposing the sequence at any time. A well-behaved\n * {@code SingleSource} will call a {@code SingleObserver}'s {@link #onSuccess(Object)} method exactly once or the {@code SingleObserver}'s\n * {@link #onError} method exactly once as they are considered mutually exclusive <strong>terminal signals</strong>.\n * <p>\n * Calling the {@code SingleObserver}'s method must happen in a serialized fashion, that is, they must not\n * be invoked concurrently by multiple threads in an overlapping fashion and the invocation pattern must\n * adhere to the following protocol:\n * <pre><code>    onSubscribe (onSuccess | onError)?</code></pre>\n * <p>\n * Subscribing a {@code SingleObserver} to multiple {@code SingleSource}s is not recommended. If such reuse\n * happens, it is the duty of the {@code SingleObserver} implementation to be ready to receive multiple calls to\n * its methods and ensure proper concurrent behavior of its business logic.\n * <p>\n * Calling {@link #onSubscribe(Disposable)}, {@link #onSuccess(Object)} or {@link #onError(Throwable)} with a\n * {@code null} argument is forbidden.\n * <p>\n * The implementations of the {@code onXXX} methods should avoid throwing runtime exceptions other than the following cases:\n * <ul>\n * <li>If the argument is {@code null}, the methods can throw a {@code NullPointerException}.\n * Note though that RxJava prevents {@code null}s to enter into the flow and thus there is generally no\n * need to check for nulls in flows assembled from standard sources and intermediate operators.\n * </li>\n * <li>If there is a fatal error (such as {@code VirtualMachineError}).</li>\n * </ul>\n * @see <a href=\"http://reactivex.io/documentation/observable.html\">ReactiveX documentation: Observable</a>\n * @param <T>\n *          the type of item the SingleObserver expects to observe\n * @since 2.0\n */",
            "/**\n * 提供接收单个值或错误的 push 式通知机制。\n * <p>\n * 当 {@code SingleObserver} 通过 {@link SingleSource#subscribe(SingleObserver)} 订阅 {@link SingleSource} 时，\n * {@code SingleSource} 会调用 {@link #onSubscribe(Disposable)} 并传入 {@link Disposable}，允许随时 dispose 序列。\n * 行为良好的 {@code SingleSource} 会恰好一次调用 {@code SingleObserver} 的 {@link #onSuccess(Object)}，\n * 或恰好一次调用 {@code SingleObserver} 的 {@link #onError}，二者被视为互斥的 <strong>终端信号</strong>。\n * <p>\n * 调用 {@code SingleObserver} 的方法必须串行进行，即不可被多线程以重叠方式并发调用，\n * 且调用模式须遵循以下协议：\n * <pre><code>    onSubscribe (onSuccess | onError)?</code></pre>\n * <p>\n * 不建议将同一 {@code SingleObserver} 订阅多个 {@code SingleSource}。若发生此类复用，\n * {@code SingleObserver} 实现者有责任准备好接收多次方法调用，并确保其业务逻辑的并发行为正确。\n * <p>\n * 禁止以 {@code null} 参数调用 {@link #onSubscribe(Disposable)}、{@link #onSuccess(Object)} 或 {@link #onError(Throwable)}。\n * <p>\n * {@code onXXX} 方法的实现应避免抛出运行时异常，以下情况除外：\n * <ul>\n * <li>若参数为 {@code null}，方法可抛出 {@code NullPointerException}。\n * 但 RxJava 会阻止 {@code null} 进入流，因此由标准源与中间算子组装的流通常无需检查 null。\n * </li>\n * <li>若发生致命错误（如 {@code VirtualMachineError}）。</li>\n * </ul>\n * @see <a href=\"http://reactivex.io/documentation/observable.html\">ReactiveX documentation: Observable</a>\n * @param <T>\n *          SingleObserver 期望观察的元素类型\n * @since 2.0\n */",
        ),
        (
            "    /**\n     * Provides the {@link SingleObserver} with the means of cancelling (disposing) the\n     * connection (channel) with the Single in both\n     * synchronous (from within {@code onSubscribe(Disposable)} itself) and asynchronous manner.\n     * @param d the Disposable instance whose {@link Disposable#dispose()} can\n     * be called anytime to cancel the connection\n     * @since 2.0\n     */",
            "    /**\n     * 为 {@link SingleObserver} 提供以同步（在 {@code onSubscribe(Disposable)} 内）或异步方式\n     * 取消（dispose）与 Single 连接（通道）的手段。\n     * @param d 可随时调用 {@link Disposable#dispose()} 以取消连接的 Disposable 实例\n     * @since 2.0\n     */",
        ),
        (
            "    /**\n     * Notifies the {@link SingleObserver} with a single item and that the {@link Single} has finished sending\n     * push-based notifications.\n     * <p>\n     * The {@code Single} will not call this method if it calls {@link #onError}.\n     *\n     * @param t\n     *          the item emitted by the {@code Single}\n     */",
            "    /**\n     * 向 {@link SingleObserver} 通知单个元素，并表明 {@link Single} 已完成 push 式通知的发送。\n     * <p>\n     * 若 {@code Single} 调用了 {@link #onError}，则不会调用本方法。\n     *\n     * @param t\n     *          {@code Single} 发射的元素\n     */",
        ),
        (
            "    /**\n     * Notifies the {@link SingleObserver} that the {@link Single} has experienced an error condition.\n     * <p>\n     * If the {@code Single} calls this method, it will not thereafter call {@link #onSuccess}.\n     *\n     * @param e\n     *          the exception encountered by the {@code Single}\n     */",
            "    /**\n     * 通知 {@link SingleObserver}：{@link Single} 遇到错误。\n     * <p>\n     * 若 {@code Single} 调用本方法，此后不会再调用 {@link #onSuccess}。\n     *\n     * @param e\n     *          {@code Single} 遇到的异常\n     */",
        ),
    ],
    "SingleOnSubscribe.java": [
        (
            "/**\n * A functional interface that has a {@code subscribe()} method that receives\n * a {@link SingleEmitter} instance that allows pushing\n * an event in a cancellation-safe manner.\n *\n * @param <T> the value type pushed\n */",
            "/**\n * 具有 {@code subscribe()} 方法的函数式接口，接收 {@link SingleEmitter} 实例，\n * 以取消安全的方式推送事件。\n *\n * @param <T> 推送的值类型\n */",
        ),
        (
            "    /**\n     * Called for each {@link SingleObserver} that subscribes.\n     * @param emitter the safe emitter instance, never {@code null}\n     * @throws Throwable on error\n     */",
            "    /**\n     * 对每个订阅的 {@link SingleObserver} 调用。\n     * @param emitter 安全的 emitter 实例，永不为 {@code null}\n     * @throws Throwable 出错时抛出\n     */",
        ),
    ],
    "SingleOperator.java": [
        (
            "/**\n * Interface to map/wrap a downstream {@link SingleObserver} to an upstream {@code SingleObserver}.\n *\n * @param <Downstream> the value type of the downstream\n * @param <Upstream> the value type of the upstream\n */",
            "/**\n * 将下游 {@link SingleObserver} 映射/包装为上游 {@code SingleObserver} 的接口。\n *\n * @param <Downstream> 下游值类型\n * @param <Upstream> 上游值类型\n */",
        ),
        (
            "    /**\n     * Applies a function to the child {@link SingleObserver} and returns a new parent {@code SingleObserver}.\n     * @param observer the child {@code SingleObserver} instance\n     * @return the parent {@code SingleObserver} instance\n     * @throws Throwable on failure\n     */",
            "    /**\n     * 对子 {@link SingleObserver} 应用函数并返回新的父 {@code SingleObserver}。\n     * @param observer 子 {@code SingleObserver} 实例\n     * @return 父 {@code SingleObserver} 实例\n     * @throws Throwable 失败时抛出\n     */",
        ),
    ],
    "SingleSource.java": [
        (
            "/**\n * Represents a basic {@link Single} source base interface,\n * consumable via an {@link SingleObserver}.\n * <p>\n * This class also serves the base type for custom operators wrapped into\n * Single via {@link Single#create(SingleOnSubscribe)}.\n *\n * @param <T> the element type\n * @since 2.0\n */",
            "/**\n * 表示可通过 {@link SingleObserver} 消费的基本 {@link Single} 源基础接口。\n * <p>\n * 本接口也是通过 {@link Single#create(SingleOnSubscribe)} 包装为 Single 的自定义算子的基础类型。\n *\n * @param <T> 元素类型\n * @since 2.0\n */",
        ),
        (
            "    /**\n     * Subscribes the given {@link SingleObserver} to this {@link SingleSource} instance.\n     * @param observer the {@code SingleObserver}, not {@code null}\n     * @throws NullPointerException if {@code observer} is {@code null}\n     */",
            "    /**\n     * 将给定 {@link SingleObserver} 订阅到此 {@link SingleSource} 实例。\n     * @param observer {@code SingleObserver}，不可为 {@code null}\n     * @throws NullPointerException 若 {@code observer} 为 {@code null}\n     */",
        ),
    ],
    "SingleTransformer.java": [
        (
            "/**\n * Interface to compose {@link Single}s.\n *\n * @param <Upstream> the upstream value type\n * @param <Downstream> the downstream value type\n */",
            "/**\n * 组合 {@link Single} 的接口。\n *\n * @param <Upstream> 上游值类型\n * @param <Downstream> 下游值类型\n */",
        ),
        (
            "    /**\n     * Applies a function to the upstream {@link Single} and returns a {@link SingleSource} with\n     * optionally different element type.\n     * @param upstream the upstream {@code Single} instance\n     * @return the transformed {@code SingleSource} instance\n     */",
            "    /**\n     * 对上游 {@link Single} 应用函数并返回元素类型可能不同的 {@link SingleSource}。\n     * @param upstream 上游 {@code Single} 实例\n     * @return 转换后的 {@code SingleSource} 实例\n     */",
        ),
    ],
    "StreamProcessor.java": [
        (
            "/**\n * A {@link Processor}-like interface combining the {@code Streamable} interface and the\n * {@link StreamSink} interface to establish a push-pull bridge based on {@link CompletionStage}-based\n * asynchronous processing and dispatching of values and errors.\n * @param <In> the element type of the input side\n * @param <Out> the element type of the output side\n * @since 4.0.0\n */",
            "/**\n * 类 {@link Processor} 接口，结合 {@code Streamable} 与 {@link StreamSink} 接口，\n * 基于 {@link CompletionStage} 的异步处理与值/错误分发建立 push-pull 桥接。\n * @param <In> 输入侧元素类型\n * @param <Out> 输出侧元素类型\n * @since 4.0.0\n */",
        ),
        (
            "    /**\n     * Returns {@code true} if this {@link StreamProcessor} has {@link Streamer}s.\n     * @return {@code true} if this {@link StreamProcessor} has {@link Streamer}s.\n     */",
            "    /**\n     * 若本 {@link StreamProcessor} 拥有 {@link Streamer} 则返回 {@code true}。\n     * @return 若本 {@link StreamProcessor} 拥有 {@link Streamer} 则为 {@code true}\n     */",
        ),
        (
            "    /**\n     * Returns the current number of {@link Streamer}s subscribed to this {@link StreamProcessor}\n     * @return the current number of {@link Streamer}s subscribed to this {@link StreamProcessor}\n     */",
            "    /**\n     * 返回当前订阅本 {@link StreamProcessor} 的 {@link Streamer} 数量。\n     * @return 当前订阅本 {@link StreamProcessor} 的 {@link Streamer} 数量\n     */",
        ),
        (
            "    /**\n     * Returns {@code true} if this {@code StreamProcessor} was completed normally via {@link #finish(Throwable)}.\n     * @return {@code true} if this {@code StreamProcessor} was completed normally via {@link #finish(Throwable)}.\n     */",
            "    /**\n     * 若本 {@code StreamProcessor} 通过 {@link #finish(Throwable)} 正常完成则返回 {@code true}。\n     * @return 若本 {@code StreamProcessor} 通过 {@link #finish(Throwable)} 正常完成则为 {@code true}\n     */",
        ),
        (
            "    /**\n     * Returns {@code true} if this {@code StreamProcessor} was completed with a {@link Throwable} via {@link #finish(Throwable)}.\n     * @return {@code true} if this {@code StreamProcessor} was completed with a {@link Throwable} via {@link #finish(Throwable)}.\n     */",
            "    /**\n     * 若本 {@code StreamProcessor} 通过 {@link #finish(Throwable)} 以 {@link Throwable} 完成则返回 {@code true}。\n     * @return 若本 {@code StreamProcessor} 通过 {@link #finish(Throwable)} 以 {@link Throwable} 完成则为 {@code true}\n     */",
        ),
        (
            "    /**\n     * Returns the terminal {@link Throwable} if this {@code StreamProcessor} was completed\n     * with a {@code Throwable} via {@link #finish(Throwable)}.\n     * @return the {@link Throwable} if any\n     */",
            "    /**\n     * 若本 {@code StreamProcessor} 通过 {@link #finish(Throwable)} 以 {@code Throwable} 完成，\n     * 则返回终端 {@link Throwable}。\n     * @return 若存在则为 {@link Throwable}\n     */",
        ),
    ],
    "StreamableConverter.java": [
        (
            "/**\n * Convenience interface and callback used by the {@link Streamable#to} operator to turn an {@link Streamable} into another\n * value fluently.\n * @param <T> the upstream type\n * @param <R> the output type\n * @since 4.0.0\n */",
            "/**\n * {@link Streamable#to} 算子使用的便捷接口与回调，用于将 {@link Streamable} 流畅地转换为其他值。\n * @param <T> 上游类型\n * @param <R> 输出类型\n * @since 4.0.0\n */",
        ),
        (
            "    /**\n     * Applies a function to the upstream {@link Streamable} and returns a converted value of type {@code R}.\n     *\n     * @param upstream the upstream {@code Streamable} instance\n     * @return the converted value\n     */",
            "    /**\n     * 对上游 {@link Streamable} 应用函数并返回类型为 {@code R} 的转换值。\n     *\n     * @param upstream 上游 {@code Streamable} 实例\n     * @return 转换后的值\n     */",
        ),
    ],
    "StreamableOperator.java": [
        (
            "/**\n * Interface to map/wrap an upstream {@link Streamer} to an downstream {@code Streamer}.\n *\n * @param <T> the value type of the upstream\n * @param <R> the value type of the downstream\n * @since 4.0.0\n */",
            "/**\n * 将上游 {@link Streamer} 映射/包装为下游 {@code Streamer} 的接口。\n *\n * @param <T> 上游值类型\n * @param <R> 下游值类型\n * @since 4.0.0\n */",
        ),
        (
            "    /**\n     * Applies a function to the upstream {@link Streamer} and returns a new downstream {@code Streamer}.\n     * @param container the {@link StreamerCancellation} handling the cancellation propagation for the downstream\n     * @param streamer the upstream {@code Streamer} instance\n     * @return the downstream {@code Streamer} instance\n     * @throws Throwable on failure\n     */",
            "    /**\n     * 对上游 {@link Streamer} 应用函数并返回新的下游 {@code Streamer}。\n     * @param container 处理下游取消传播的 {@link StreamerCancellation}\n     * @param streamer 上游 {@code Streamer} 实例\n     * @return 下游 {@code Streamer} 实例\n     * @throws Throwable 失败时抛出\n     */",
        ),
    ],
    "VirtualEmitter.java": [
        (
            "/**\n * Interface handed to user code in {@link Flowable#virtualCreate(VirtualGenerator, java.util.concurrent.ExecutorService)} callback.\n * @param <T> the element type to emit\n * @since 4.0.0\n */",
            "/**\n * 在 {@link Flowable#virtualCreate(VirtualGenerator, java.util.concurrent.ExecutorService)} 回调中交给用户代码的接口。\n * @param <T> 发射的元素类型\n * @since 4.0.0\n */",
        ),
        (
            "    /**\n     * Signal the next item\n     * @param item the item to signal\n     * @throws Throwable an arbitrary exception if the downstream cancelled\n     */",
            "    /**\n     * 发射下一个元素。\n     * @param item 要发射的元素\n     * @throws Throwable 若下游已取消则可抛出任意异常\n     */",
        ),
        (
            "    /**\n     * Returns a disposable container to relay cancellation notifications while awaiting the run.\n     * @return a new Disposable Container instance\n     */",
            "    /**\n     * 返回 disposable 容器，用于在 await 运行期间转发取消通知。\n     * @return 新的 Disposable 容器实例\n     */",
        ),
    ],
}


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


def main() -> int:
    all_replacements = {**FILE_REPLACEMENTS, **_load_large()}
    failures: list[str] = []
    ok = 0
    for rel in BATCH_FILES:
        name = Path(rel).name
        src = ORIGINAL / rel
        dst = ANALYZED / rel
        if not src.exists():
            failures.append(f"MISSING original: {rel}")
            continue
        reps = all_replacements.get(name, [])
        if not reps:
            failures.append(f"NO_REPLACEMENTS: {rel}")
            continue
        dst.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(src, dst)
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
    if not failures:
        mark_queue_done(BATCH_FILES)
        print("Marked 15 files done in queue")
    print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False, indent=2))
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
