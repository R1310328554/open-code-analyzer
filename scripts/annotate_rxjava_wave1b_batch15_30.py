#!/usr/bin/env python3
"""Chinese-annotate RxJava 4.0.0-alpha-21 wave-1b [15:30]."""
from __future__ import annotations

import importlib.util
import json
import re
import shutil
import sys
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "rxjava/4.0.0-alpha-21"
ORIGINAL = VER / "original"
ANALYZED = VER / "analyzed"
QUEUE = VER / "_reports/class-queue"
BATCH_FILES = json.loads((QUEUE / "batch.json").read_text(encoding="utf-8"))["files"][15:30]


def _load_large() -> dict[str, list[tuple[str, str]]]:
    path = Path(__file__).with_name("annotate_rxjava_wave1b_large.py")
    spec = importlib.util.spec_from_file_location("rxjava_large", path)
    if spec is None or spec.loader is None:
        return {}
    mod = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(mod)
    return getattr(mod, "LARGE_REPLACEMENTS", {})


FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "CompletableOperator.java": [
        (
            "/**\n * Interface to map/wrap a downstream observer to an upstream observer.\n */",
            "/**\n * 将下游 observer 映射/包装为上游 observer 的接口。\n */",
        ),
        (
            "    /**\n     * Applies a function to the child {@link CompletableObserver} and returns a new parent {@code CompletableObserver}.\n     * @param observer the child {@code CompletableObserver} instance\n     * @return the parent {@code CompletableObserver} instance\n     * @throws Throwable on failure\n     */",
            "    /**\n     * 对子 {@link CompletableObserver} 应用函数并返回新的父 {@code CompletableObserver}。\n     * @param observer 子 {@code CompletableObserver} 实例\n     * @return 父 {@code CompletableObserver} 实例\n     * @throws Throwable 失败时抛出\n     */",
        ),
    ],
    "CompletableSource.java": [
        (
            "/**\n * Represents a basic {@link Completable} source base interface,\n * consumable via an {@link CompletableObserver}.\n *\n * @since 2.0\n */",
            "/**\n * 表示可通过 {@link CompletableObserver} 消费的基本 {@link Completable} 源基础接口。\n *\n * @since 2.0\n */",
        ),
        (
            "    /**\n     * Subscribes the given {@link CompletableObserver} to this {@code CompletableSource} instance.\n     * @param observer the {@code CompletableObserver}, not {@code null}\n     * @throws NullPointerException if {@code observer} is {@code null}\n     */",
            "    /**\n     * 将给定 {@link CompletableObserver} 订阅到此 {@code CompletableSource} 实例。\n     * @param observer {@code CompletableObserver}，不可为 {@code null}\n     * @throws NullPointerException 若 {@code observer} 为 {@code null}\n     */",
        ),
    ],
    "CompletableTransformer.java": [
        (
            "/**\n * Convenience interface and callback used by the compose operator to turn a {@link Completable} into another\n * {@code Completable} fluently.\n */",
            "/**\n * compose 算子使用的便捷接口与回调，用于将 {@link Completable} 流畅地转换为另一个 {@code Completable}。\n */",
        ),
        (
            "    /**\n     * Applies a function to the upstream {@link Completable} and returns a {@link CompletableSource}.\n     * @param upstream the upstream {@code Completable} instance\n     * @return the transformed {@code CompletableSource} instance\n     */",
            "    /**\n     * 对上游 {@link Completable} 应用函数并返回 {@link CompletableSource}。\n     * @param upstream 上游 {@code Completable} 实例\n     * @return 转换后的 {@code CompletableSource} 实例\n     */",
        ),
    ],
    "CompletionStageDisposable.java": [
        (
            "/**\n * Consist of a terminal stage and a disposable to be able to cancel a sequence.\n * @param <T> the return and element type of the various stages\n * @since 4.0.0\n */",
            "/**\n * 由终端 stage 与 disposable 组成，用于取消序列。\n * @param <T> 各 stage 的返回与元素类型\n * @since 4.0.0\n */",
        ),
        (
            "    /**\n     * Construct an instance with parameters\n     * @param stage the stage to be awaited\n     * @param disposable the disposable to cancel asynchronously\n     */",
            "    /**\n     * 使用参数构造实例。\n     * @param stage 待 await 的 stage\n     * @param disposable 用于异步取消的 disposable\n     */",
        ),
        (
            "    /**\n     * Await the completion of the current stage.\n     * <p>\n     * Rethrows any original unchecked exceptions as is.\n     * @throws CancellationException if the computation was cancelled\n     * @throws CompletionException if the original exception was a checked exception\n     */",
            "    /**\n     * 等待当前 stage 完成。\n     * <p>\n     * 原样重新抛出原始 unchecked 异常。\n     * @throws CancellationException 若计算被取消\n     * @throws CompletionException 若原始异常为 checked 异常\n     */",
        ),
        (
            "    /**\n     * Indicate this instance is deliberately not awaiting its stage.\n     */",
            "    /**\n     * 表明本实例故意不 await 其 stage。\n     */",
        ),
        (
            "    /**\n     * Set an allocator tracer callback to track where CompletionStageDisposables are leaking.\n     * @param callback the callback to call when a new trace is being established\n     */",
            "    /**\n     * 设置分配追踪回调，用于追踪 CompletionStageDisposable 泄漏位置。\n     * @param callback 建立新追踪时调用的回调\n     */",
        ),
        (
            "    /**\n     * Returns the current allocation stacktrace capturing consumer.\n     * @return the current allocation stacktrace capturing consumer.\n     */",
            "    /**\n     * 返回当前捕获分配堆栈的 consumer。\n     * @return 当前捕获分配堆栈的 consumer\n     */",
        ),
        (
            "    /***\n     * Returns the associated completion stage value.\n     * @return the associated completion stage value.\n     */",
            "    /***\n     * 返回关联的 completion stage 值。\n     * @return 关联的 completion stage 值\n     */",
        ),
        (
            "    /**\n     * Returns the associated disposable value.\n     * @return the associated disposable value.\n     */",
            "    /**\n     * 返回关联的 disposable 值。\n     * @return 关联的 disposable 值\n     */",
        ),
    ],
    "Emitter.java": [
        (
            "/**\n * Base interface for emitting signals in a push-fashion in various generator-like source\n * operators (create, generate).\n * <p>\n * Note that the {@link Emitter#onNext}, {@link Emitter#onError} and\n * {@link Emitter#onComplete} methods provided to the function via the {@link Emitter} instance should be called synchronously,\n * never concurrently. Calling them from multiple threads is not supported and leads to an\n * undefined behavior.\n *\n * @param <T> the value type emitted\n */",
            "/**\n * 各类生成器式源算子（create、generate）中以 push 方式发射信号的基础接口。\n * <p>\n * 注意：通过 {@link Emitter} 实例提供给函数的 {@link Emitter#onNext}、{@link Emitter#onError}\n * 与 {@link Emitter#onComplete} 方法应同步调用，不可并发调用。\n * 从多线程调用不受支持，会导致未定义行为。\n *\n * @param <T> 发射的值类型\n */",
        ),
        (
            "    /**\n     * Signal a normal value.\n     * @param value the value to signal, not {@code null}\n     */",
            "    /**\n     * 发射正常值。\n     * @param value 要发射的值，不可为 {@code null}\n     */",
        ),
        (
            "    /**\n     * Signal a {@link Throwable} exception.\n     * @param error the {@code Throwable} to signal, not {@code null}\n     */",
            "    /**\n     * 发射 {@link Throwable} 异常。\n     * @param error 要发射的 {@code Throwable}，不可为 {@code null}\n     */",
        ),
        (
            "    /**\n     * Signal a completion.\n     */",
            "    /**\n     * 发射完成信号。\n     */",
        ),
    ],
    "ErrorMode.java": [
        (
            "/**\n * Indicates when an error from the any of the involved sources should be handled.\n * <p>\n * Usually appears with {@code concat} and {@code concatMap} operators where the outer and inner source(s)\n * may error out in the middle of streaming and the user would like to finish the current source before\n * cancelling the rest and signaling the error(s) to the consumers.\n * @since 4.0.0\n */",
            "/**\n * 指示应何时处理任一参与源产生的错误。\n * <p>\n * 通常与 {@code concat}、{@code concatMap} 算子配合使用：外层与内层源可能在流式传输中途出错，\n * 用户希望在取消其余源并向消费者发出错误信号之前，先完成当前源。\n * @since 4.0.0\n */",
        ),
        (
            "    /** Report the error immediately, cancelling the active sources. */",
            "    /** 立即报告错误并取消活跃源。 */",
        ),
        (
            "    /** Report error after an inner source terminated. */",
            "    /** 在内层源终止后报告错误。 */",
        ),
        (
            "    /** Report the error after all sources terminated. */",
            "    /** 在所有源终止后报告错误。 */",
        ),
    ],
    "FlowableConverter.java": [
        (
            "/**\n * Convenience interface and callback used by the {@link Flowable#to} operator to turn a {@link Flowable} into another\n * value fluently.\n * <p>History: 2.1.7 - experimental\n * @param <T> the upstream type\n * @param <R> the output type\n * @since 2.2\n */",
            "/**\n * {@link Flowable#to} 算子使用的便捷接口与回调，用于将 {@link Flowable} 流畅地转换为其他值。\n * <p>History: 2.1.7 - experimental\n * @param <T> 上游类型\n * @param <R> 输出类型\n * @since 2.2\n */",
        ),
        (
            "    /**\n     * Applies a function to the upstream {@link Flowable} and returns a converted value of type {@code R}.\n     *\n     * @param upstream the upstream {@code Flowable} instance\n     * @return the converted value\n     */",
            "    /**\n     * 对上游 {@link Flowable} 应用函数并返回类型为 {@code R} 的转换值。\n     *\n     * @param upstream 上游 {@code Flowable} 实例\n     * @return 转换后的值\n     */",
        ),
    ],
    "FlowableEmitter.java": [
        (
            "/**\n * Abstraction over a Reactive Streams {@link java.util.concurrent.Flow.Subscriber} that allows associating\n * a resource with it and exposes the current number of downstream\n * requested amount.\n * <p>\n * The {@link #onNext(Object)}, {@link #onError(Throwable)}, {@link #tryOnError(Throwable)}\n * and {@link #onComplete()} methods should be called in a sequential manner, just like\n * the {@link java.util.concurrent.Flow.Subscriber Subscriber}'s methods.\n * Use the {@code FlowableEmitter} the {@link #serialize()} method returns instead of the original\n * {@code FlowableEmitter} instance provided by the generator routine if you want to ensure this.\n * The other methods are thread-safe.\n * <p>\n * The emitter allows the registration of a single resource, in the form of a {@link Disposable}\n * or {@link Cancellable} via {@link #setDisposable(Disposable)} or {@link #setCancellable(Cancellable)}\n * respectively. The emitter implementations will dispose/cancel this instance when the\n * downstream cancels the flow or after the event generator logic calls {@link #onError(Throwable)},\n * {@link #onComplete()} or when {@link #tryOnError(Throwable)} succeeds.\n * <p>\n * Only one {@code Disposable} or {@code Cancellable} object can be associated with the emitter at\n * a time. Calling either {@code set} method will dispose/cancel any previous object. If there\n * is a need for handling multiple resources, one can create a {@link io.reactivex.rxjava4.disposables.CompositeDisposable}\n * and associate that with the emitter instead.\n * <p>\n * The {@link Cancellable} is logically equivalent to {@code Disposable} but allows using cleanup logic that can\n * throw a checked exception (such as many {@code close()} methods on Java IO components). Since\n * the release of resources happens after the terminal events have been delivered or the sequence gets\n * cancelled, exceptions throw within {@code Cancellable} are routed to the global error handler via\n * {@link io.reactivex.rxjava4.plugins.RxJavaPlugins#onError(Throwable)}.\n *\n * @param <T> the value type to emit\n */",
            "/**\n * Reactive Streams {@link java.util.concurrent.Flow.Subscriber} 的抽象，允许关联资源\n * 并暴露下游当前请求数量。\n * <p>\n * {@link #onNext(Object)}、{@link #onError(Throwable)}、{@link #tryOnError(Throwable)}\n * 与 {@link #onComplete()} 方法应像 {@link java.util.concurrent.Flow.Subscriber Subscriber}\n * 的方法一样顺序调用。若要保证这一点，请使用 {@link #serialize()} 返回的 {@code FlowableEmitter}\n * 而非生成器例程提供的原始 {@code FlowableEmitter} 实例。其他方法为线程安全。\n * <p>\n * emitter 允许通过 {@link #setDisposable(Disposable)} 或 {@link #setCancellable(Cancellable)}\n * 分别注册单个 {@link Disposable} 或 {@link Cancellable} 资源。当下游取消流，或事件生成逻辑调用\n * {@link #onError(Throwable)}、{@link #onComplete()}，或 {@link #tryOnError(Throwable)} 成功后，\n * emitter 实现将 dispose/cancel 该实例。\n * <p>\n * 同一时刻 emitter 只能关联一个 {@code Disposable} 或 {@code Cancellable} 对象。\n * 调用任一 {@code set} 方法会 dispose/cancel 先前的对象。若需管理多个资源，\n * 可创建 {@link io.reactivex.rxjava4.disposables.CompositeDisposable} 并关联到 emitter。\n * <p>\n * {@link Cancellable} 在逻辑上等价于 {@code Disposable}，但允许使用可能抛出 checked 异常的清理逻辑\n *（例如许多 Java IO 组件的 {@code close()} 方法）。由于资源释放在终端事件交付后或序列取消后进行，\n * {@code Cancellable} 内抛出的异常将通过 {@link io.reactivex.rxjava4.plugins.RxJavaPlugins#onError(Throwable)}\n * 路由到全局错误处理器。\n *\n * @param <T> 发射的值类型\n */",
        ),
        (
            "    /**\n     * Sets a Disposable on this emitter; any previous {@link Disposable}\n     * or {@link Cancellable} will be disposed/cancelled.\n     * <p>This method is thread-safe.\n     * @param d the disposable, {@code null} is allowed\n     */",
            "    /**\n     * 在本 emitter 上设置 Disposable；任何先前的 {@link Disposable}\n     * 或 {@link Cancellable} 将被 dispose/cancel。\n     * <p>本方法为线程安全。\n     * @param d disposable，允许为 {@code null}\n     */",
        ),
        (
            "    /**\n     * Sets a {@link Cancellable} on this emitter; any previous {@link Disposable}\n     * or {@code Cancellable} will be disposed/cancelled.\n     * <p>This method is thread-safe.\n     * @param c the {@code Cancellable} resource, {@code null} is allowed\n     */",
            "    /**\n     * 在本 emitter 上设置 {@link Cancellable}；任何先前的 {@link Disposable}\n     * 或 {@code Cancellable} 将被 dispose/cancel。\n     * <p>本方法为线程安全。\n     * @param c {@code Cancellable} 资源，允许为 {@code null}\n     */",
        ),
        (
            "    /**\n     * The current outstanding request amount.\n     * <p>This method is thread-safe.\n     * @return the current outstanding request amount\n     */",
            "    /**\n     * 当前未完成的请求数量。\n     * <p>本方法为线程安全。\n     * @return 当前未完成的请求数量\n     */",
        ),
        (
            "    /**\n     * Returns true if the downstream cancelled the sequence or the\n     * emitter was terminated via {@link #onError(Throwable)}, {@link #onComplete} or a\n     * successful {@link #tryOnError(Throwable)}.\n     * <p>This method is thread-safe.\n     * @return true if the downstream cancelled the sequence or the emitter was terminated\n     */",
            "    /**\n     * 若下游已取消序列，或通过 {@link #onError(Throwable)}、{@link #onComplete}\n     * 或成功的 {@link #tryOnError(Throwable)} 终止 emitter，则返回 true。\n     * <p>本方法为线程安全。\n     * @return 若下游已取消序列或 emitter 已终止则为 true\n     */",
        ),
        (
            "    /**\n     * Ensures that calls to {@code onNext}, {@code onError} and {@code onComplete} are properly serialized.\n     * @return the serialized {@link FlowableEmitter}\n     */",
            "    /**\n     * 确保对 {@code onNext}、{@code onError} 与 {@code onComplete} 的调用被正确串行化。\n     * @return 串行化后的 {@link FlowableEmitter}\n     */",
        ),
        (
            "    /**\n     * Attempts to emit the specified {@link Throwable} error if the downstream\n     * hasn't cancelled the sequence or is otherwise terminated, returning false\n     * if the emission is not allowed to happen due to lifecycle restrictions.\n     * <p>\n     * Unlike {@link #onError(Throwable)}, the {@link io.reactivex.rxjava4.plugins.RxJavaPlugins#onError(Throwable) RxjavaPlugins.onError}\n     * is not called if the error could not be delivered.\n     * <p>History: 2.1.1 - experimental\n     * @param t the throwable error to signal if possible\n     * @return true if successful, false if the downstream is not able to accept further\n     * events\n     * @since 2.2\n     */",
            "    /**\n     * 若下游未取消序列且未以其他方式终止，尝试发射指定 {@link Throwable} 错误；\n     * 若因生命周期限制不允许发射则返回 false。\n     * <p>\n     * 与 {@link #onError(Throwable)} 不同，若错误无法交付，不会调用\n     * {@link io.reactivex.rxjava4.plugins.RxJavaPlugins#onError(Throwable) RxjavaPlugins.onError}。\n     * <p>History: 2.1.1 - experimental\n     * @param t 若可能则发射的 throwable 错误\n     * @return 成功则为 true；若下游无法接受更多事件则为 false\n     * @since 2.2\n     */",
        ),
    ],
    "FlowableOnSubscribe.java": [
        (
            "/**\n * A functional interface that has a {@code subscribe()} method that receives\n * a {@link FlowableEmitter} instance that allows pushing\n * events in a backpressure-safe and cancellation-safe manner.\n *\n * @param <T> the value type pushed\n */",
            "/**\n * 具有 {@code subscribe()} 方法的函数式接口，接收 {@link FlowableEmitter} 实例，\n * 以背压安全且取消安全的方式推送事件。\n *\n * @param <T> 推送的值类型\n */",
        ),
        (
            "    /**\n     * Called for each {@link java.util.concurrent.Flow.Subscriber Subscriber} that subscribes.\n     * @param emitter the safe emitter instance, never {@code null}\n     * @throws Throwable on error\n     */",
            "    /**\n     * 对每个订阅的 {@link java.util.concurrent.Flow.Subscriber Subscriber} 调用。\n     * @param emitter 安全的 emitter 实例，永不为 {@code null}\n     * @throws Throwable 出错时抛出\n     */",
        ),
    ],
    "FlowableOperator.java": [
        (
            "/**\n * Interface to map/wrap a downstream {@link Subscriber} to an upstream {@code Subscriber}.\n *\n * @param <Downstream> the value type of the downstream\n * @param <Upstream> the value type of the upstream\n */",
            "/**\n * 将下游 {@link Subscriber} 映射/包装为上游 {@code Subscriber} 的接口。\n *\n * @param <Downstream> 下游值类型\n * @param <Upstream> 上游值类型\n */",
        ),
        (
            "    /**\n     * Applies a function to the child {@link Subscriber} and returns a new parent {@code Subscriber}.\n     * @param subscriber the child {@code Subscriber} instance\n     * @return the parent {@code Subscriber} instance\n     * @throws Throwable on failure\n     */",
            "    /**\n     * 对子 {@link Subscriber} 应用函数并返回新的父 {@code Subscriber}。\n     * @param subscriber 子 {@code Subscriber} 实例\n     * @return 父 {@code Subscriber} 实例\n     * @throws Throwable 失败时抛出\n     */",
        ),
    ],
    "FlowableSubscriber.java": [
        (
            "/**\n * Represents a Reactive-Streams inspired {@link Subscriber} that is RxJava 4 only\n * and weakens the Reactive Streams rules <a href='https://github.com/reactive-streams/reactive-streams-jvm#1.3'>§1.3</a>\n * and <a href='https://github.com/reactive-streams/reactive-streams-jvm#3.9'>§3.9</a> of the specification\n * for gaining performance.\n *\n * <p>History: 2.0.7 - experimental; 2.1 - beta\n * @param <T> the value type\n * @since 2.2\n */",
            "/**\n * 表示受 Reactive Streams 启发的 {@link Subscriber}，仅用于 RxJava 4，\n * 为提升性能而放宽规范 <a href='https://github.com/reactive-streams/reactive-streams-jvm#1.3'>§1.3</a>\n * 与 <a href='https://github.com/reactive-streams/reactive-streams-jvm#3.9'>§3.9</a>。\n *\n * <p>History: 2.0.7 - experimental; 2.1 - beta\n * @param <T> 值类型\n * @since 2.2\n */",
        ),
        (
            "    /**\n     * Implementors of this method should make sure everything that needs\n     * to be visible in {@link #onNext(Object)} is established before\n     * calling {@link Subscription#request(long)}. In practice this means\n     * no initialization should happen after the {@code request()} call and\n     * additional behavior is thread safe in respect to {@code onNext}.\n     *\n     * {@inheritDoc}\n     */",
            "    /**\n     * 本方法的实现者应确保在调用 {@link Subscription#request(long)} 之前，\n     * {@link #onNext(Object)} 中需要可见的一切均已就绪。实践中这意味着\n     * 在 {@code request()} 调用之后不应再进行初始化，\n     * 且相对于 {@code onNext} 的附加行为应为线程安全。\n     *\n     * {@inheritDoc}\n     */",
        ),
    ],
    "FlowableTransformer.java": [
        (
            "/**\n * Interface to compose {@link Flowable}s.\n *\n * @param <Upstream> the upstream value type\n * @param <Downstream> the downstream value type\n */",
            "/**\n * 组合 {@link Flowable} 的接口。\n *\n * @param <Upstream> 上游值类型\n * @param <Downstream> 下游值类型\n */",
        ),
        (
            "    /**\n     * Applies a function to the upstream {@link Flowable} and returns a {@link Publisher} with\n     * optionally different element type.\n     * @param upstream the upstream {@code Flowable} instance\n     * @return the transformed {@code Publisher} instance\n     */",
            "    /**\n     * 对上游 {@link Flowable} 应用函数并返回元素类型可能不同的 {@link Publisher}。\n     * @param upstream 上游 {@code Flowable} 实例\n     * @return 转换后的 {@code Publisher} 实例\n     */",
        ),
    ],
    "GroupedFlowable.java": [
        (
            "/**\n * A {@link Flowable} that has been grouped by key, the value of which can be obtained with {@link #getKey()}.\n * <p>\n * <em>Note:</em> A {@link GroupedFlowable} will cache the items it is to emit until such time as it\n * is subscribed to. For this reason, in order to avoid memory leaks, you should not simply ignore those\n * {@code GroupedFlowable}s that do not concern you. Instead, you can signal to them that they\n * may discard their buffers by applying an operator like {@link Flowable#take take}{@code (0)} to them.\n *\n * @param <K>\n *            the type of the key\n * @param <T>\n *            the type of the items emitted by the {@code GroupedFlowable}\n * @see Flowable#groupBy(io.reactivex.rxjava4.functions.Function)\n * @see <a href=\"http://reactivex.io/documentation/operators/groupby.html\">ReactiveX documentation: GroupBy</a>\n */",
            "/**\n * 按 key 分组的 {@link Flowable}，可通过 {@link #getKey()} 获取 key 值。\n * <p>\n * <em>注意：</em>{@link GroupedFlowable} 会缓存待发射的元素，直至被订阅。\n * 因此为避免内存泄漏，不应简单忽略不关心的 {@code GroupedFlowable}。\n * 可对其应用如 {@link Flowable#take take}{@code (0)} 等算子，通知其可丢弃缓冲区。\n *\n * @param <K>\n *            key 的类型\n * @param <T>\n *            {@code GroupedFlowable} 发射元素的类型\n * @see Flowable#groupBy(io.reactivex.rxjava4.functions.Function)\n * @see <a href=\"http://reactivex.io/documentation/operators/groupby.html\">ReactiveX documentation: GroupBy</a>\n */",
        ),
        (
            "    /**\n     * Constructs a GroupedFlowable with the given key.\n     * @param key the key\n     */",
            "    /**\n     * 使用给定 key 构造 GroupedFlowable。\n     * @param key key\n     */",
        ),
        (
            "    /**\n     * Returns the key that identifies the group of items emitted by this {@code GroupedFlowable}.\n     *\n     * @return the key that the items emitted by this {@code GroupedFlowable} were grouped by\n     */",
            "    /**\n     * 返回标识本 {@code GroupedFlowable} 所发射元素分组的 key。\n     *\n     * @return 本 {@code GroupedFlowable} 发射元素所依据的分组 key\n     */",
        ),
    ],
}


def apply_replacements(text: str, replacements: list[tuple[str, str]]) -> str:
    for old, new in replacements:
        if old not in text:
            raise ValueError(f"Pattern not found:\n{old[:120]}...")
        text = text.replace(old, new, 1)
    return text


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
    print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False, indent=2))
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
