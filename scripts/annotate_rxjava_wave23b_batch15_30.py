#!/usr/bin/env python3
"""Chinese-annotate RxJava 4.0.0-alpha-21 wave-23b Single + Streamable operators [15:30]."""
from __future__ import annotations

import json
import os
import re
import shutil
import subprocess
import sys
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "rxjava/4.0.0-alpha-21"
ORIGINAL = VER / "original"
ANALYZED = VER / "analyzed"
QUEUE = VER / "_reports/class-queue"
SCRIPTS = ROOT / "scripts"
WAVE23B_FILE = Path("/tmp/rxjava_w23b.txt")
SCRIPT_NAME = "annotate_rxjava_wave23b_batch15_30.py"
BATCH_FILES = [
    ln.strip()
    for ln in WAVE23B_FILE.read_text(encoding="utf-8").splitlines()
    if ln.strip()
]

GUARD_FILES = [
    VER
    / "analyzed/src/main/java/io/reactivex/rxjava4/internal/operators/flowable/FlowableSamplePublisher.java",
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/TransactionDefinition.java",
]

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "SingleTimeout.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class SingleTimeout<T> extends Single<T> {",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 为上游 Single 设置超时：超时前 onSuccess/onError 正常转发；\n"
            " * 超时后无 other 则 TimeoutException，有 other 则订阅备用 SingleSource。\n"
            " *\n * @param <T> 元素类型\n"
            " */"
            "\npublic final class SingleTimeout<T> extends Single<T> {",
        ),
        (
            "    public SingleTimeout(SingleSource<T> source, long timeout, TimeUnit unit, Scheduler scheduler,\n                         SingleSource<? extends T> other) {",
            "    /**\n"
            "     * @param source 上游 SingleSource\n"
            "     * @param timeout 超时时长\n"
            "     * @param unit 时间单位\n"
            "     * @param scheduler 调度超时任务的 Scheduler\n"
            "     * @param other 超时后的备用 SingleSource（可为 null）\n"
            "     */\n"
            "    public SingleTimeout(SingleSource<T> source, long timeout, TimeUnit unit, Scheduler scheduler,\n                         SingleSource<? extends T> other) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(final SingleObserver<? super T> observer) {",
            "    /** 订阅 TimeoutMainObserver 并 scheduleDirect 超时任务。 */\n"
            "    @Override\n    protected void subscribeActual(final SingleObserver<? super T> observer) {",
        ),
        (
            "    static final class TimeoutMainObserver<T> extends AtomicReference<Disposable>\n    implements SingleObserver<T>, Runnable, Disposable {",
            "    /** 主 Observer：成功/错误时取消定时任务；run() 触发超时逻辑。 */\n"
            "    static final class TimeoutMainObserver<T> extends AtomicReference<Disposable>\n    implements SingleObserver<T>, Runnable, Disposable {",
        ),
        (
            "        static final class TimeoutFallbackObserver<T> extends AtomicReference<Disposable>\n        implements SingleObserver<T> {",
            "        /** 备用 SingleSource 的 Observer：转发 onSuccess/onError。 */\n"
            "        static final class TimeoutFallbackObserver<T> extends AtomicReference<Disposable>\n        implements SingleObserver<T> {",
        ),
        (
            "        @Override\n        public void run() {",
            "        /** 超时触发：dispose 主订阅后 onError 或订阅 other fallback。 */\n"
            "        @Override\n        public void run() {",
        ),
        (
            "        @Override\n        public void onSuccess(T t) {",
            "        /** 成功时 CAS 置 DISPOSED、取消 task 并 downstream.onSuccess。 */\n"
            "        @Override\n        public void onSuccess(T t) {",
        ),
        (
            "        @Override\n        public void onError(Throwable e) {",
            "        /** 错误时取消 task 并转发；已终止则 RxJavaPlugins.onError。 */\n"
            "        @Override\n        public void onError(Throwable e) {",
        ),
    ],
    "SingleTimer.java": [
        (
            "/**\n * Signals a {@code 0L} after the specified delay.\n */",
            "/**\n"
            " * 在指定 delay 后于 scheduler 上发射 {@code 0L}。\n"
            " */",
        ),
        (
            "    public SingleTimer(long delay, TimeUnit unit, Scheduler scheduler) {",
            "    /**\n"
            "     * @param delay 延迟时长\n"
            "     * @param unit 时间单位\n"
            "     * @param scheduler 执行定时任务的 Scheduler\n"
            "     */\n"
            "    public SingleTimer(long delay, TimeUnit unit, Scheduler scheduler) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(final SingleObserver<? super Long> observer) {",
            "    /** 创建 TimerDisposable 并 scheduleDirect 延迟任务。 */\n"
            "    @Override\n    protected void subscribeActual(final SingleObserver<? super Long> observer) {",
        ),
        (
            "    static final class TimerDisposable extends AtomicReference<Disposable> implements Disposable, Runnable {",
            "    /** 定时 Disposable：run() 时 downstream.onSuccess(0L)。 */\n"
            "    static final class TimerDisposable extends AtomicReference<Disposable> implements Disposable, Runnable {",
        ),
        (
            "        @Override\n        public void run() {",
            "        /** 延迟到期后发射 0L。 */\n"
            "        @Override\n        public void run() {",
        ),
    ],
    "SingleToFlowable.java": [
        (
            "/**\n * Wraps a Single and exposes it as a Flowable.\n *\n * @param <T> the value type\n */",
            "/**\n"
            " * 将 Single 包装为 Flowable：成功时发射单元素后 onComplete，\n"
            " * 错误时 onError（DeferredScalarSubscription 背压语义）。\n"
            " *\n * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    public SingleToFlowable(SingleSource<? extends T> source) {",
            "    /** @param source 上游 SingleSource */\n"
            "    public SingleToFlowable(SingleSource<? extends T> source) {",
        ),
        (
            "    @Override\n    public void subscribeActual(final Subscriber<? super T> s) {",
            "    /** 订阅 SingleToFlowableObserver 将 Single 信号转为 Flowable。 */\n"
            "    @Override\n    public void subscribeActual(final Subscriber<? super T> s) {",
        ),
        (
            "    static final class SingleToFlowableObserver<T> extends DeferredScalarSubscription<T>\n    implements SingleObserver<T> {",
            "    /** Single→Flowable 适配：onSuccess 时 complete(value)，cancel 时 dispose upstream。 */\n"
            "    static final class SingleToFlowableObserver<T> extends DeferredScalarSubscription<T>\n    implements SingleObserver<T> {",
        ),
        (
            "        @Override\n        public void onSuccess(T value) {",
            "        /** 调用 DeferredScalarSubscription.complete 发射单元素。 */\n"
            "        @Override\n        public void onSuccess(T value) {",
        ),
    ],
    "SingleToObservable.java": [
        (
            "/**\n * Wraps a Single and exposes it as an Observable.\n *\n * @param <T> the value type\n */",
            "/**\n"
            " * 将 Single 包装为 Observable：成功时 onNext 后 onComplete，\n"
            " * 错误时 onError（DeferredScalarDisposable 语义）。\n"
            " *\n * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    public SingleToObservable(SingleSource<? extends T> source) {",
            "    /** @param source 上游 SingleSource */\n"
            "    public SingleToObservable(SingleSource<? extends T> source) {",
        ),
        (
            "    @Override\n    public void subscribeActual(final Observer<? super T> observer) {",
            "    /** 订阅 create(observer) 返回的 SingleObserver。 */\n"
            "    @Override\n    public void subscribeActual(final Observer<? super T> observer) {",
        ),
        (
            "    /**\n     * Creates a {@link SingleObserver} wrapper around a {@link Observer}.\n     * <p>History: 2.0.1 - experimental\n     * @param <T> the value type\n     * @param downstream the downstream {@code Observer} to talk to\n     * @return the new SingleObserver instance\n     * @since 2.2\n     */",
            "    /**\n"
            "     * 为 {@link Observer} 创建 {@link SingleObserver} 包装。\n"
            "     * <p>History: 2.0.1 - experimental\n"
            "     * @param <T> 元素类型\n"
            "     * @param downstream 下游 {@code Observer}\n"
            "     * @return 新的 SingleObserver 实例\n"
            "     * @since 2.2\n"
            "     */",
        ),
        (
            "    static final class SingleToObservableObserver<T>\n    extends DeferredScalarDisposable<T>\n    implements SingleObserver<T> {",
            "    /** Single→Observable 适配：onSuccess 时 complete，dispose 时释放 upstream。 */\n"
            "    static final class SingleToObservableObserver<T>\n    extends DeferredScalarDisposable<T>\n    implements SingleObserver<T> {",
        ),
        (
            "        @Override\n        public void onSuccess(T value) {",
            "        /** 调用 DeferredScalarDisposable.complete 发射单元素。 */\n"
            "        @Override\n        public void onSuccess(T value) {",
        ),
    ],
    "SingleUnsubscribeOn.java": [
        (
            "/**\n * Makes sure dispose() call from downstream happens on the specified scheduler.\n * \n * @param <T> the value type\n */",
            "/**\n"
            " * 确保下游 dispose() 在指定 scheduler 上执行 upstream.dispose()，\n"
            " * 避免在错误线程取消订阅。\n"
            " * \n * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    public SingleUnsubscribeOn(SingleSource<T> source, Scheduler scheduler) {",
            "    /**\n"
            "     * @param source 上游 SingleSource\n"
            "     * @param scheduler 执行 dispose 的 Scheduler\n"
            "     */\n"
            "    public SingleUnsubscribeOn(SingleSource<T> source, Scheduler scheduler) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(SingleObserver<? super T> observer) {",
            "    /** 订阅 UnsubscribeOnSingleObserver 包装 dispose 调度。 */\n"
            "    @Override\n    protected void subscribeActual(SingleObserver<? super T> observer) {",
        ),
        (
            "    static final class UnsubscribeOnSingleObserver<T> extends AtomicReference<Disposable>\n    implements SingleObserver<T>, Disposable, Runnable {",
            "    /** dispose 时 scheduleDirect(this) 在 scheduler 上执行 ds.dispose()。 */\n"
            "    static final class UnsubscribeOnSingleObserver<T> extends AtomicReference<Disposable>\n    implements SingleObserver<T>, Disposable, Runnable {",
        ),
        (
            "        @Override\n        public void dispose() {",
            "        /** 缓存 upstream Disposable 并在 scheduler 上异步 dispose。 */\n"
            "        @Override\n        public void dispose() {",
        ),
        (
            "        @Override\n        public void run() {",
            "        /** scheduler 线程上执行 ds.dispose()。 */\n"
            "        @Override\n        public void run() {",
        ),
    ],
    "SingleUsing.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class SingleUsing<T, U> extends Single<T> {",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 资源管理模式：resourceSupplier 获取资源 U，\n"
            " * singleFunction 生成 SingleSource，终止时 disposer 释放资源；\n"
            " * eager 控制资源在信号前还是后释放。\n"
            " *\n * @param <T> Single 结果类型\n"
            " * @param <U> 资源类型\n"
            " */"
            "\npublic final class SingleUsing<T, U> extends Single<T> {",
        ),
        (
            "    public SingleUsing(Supplier<U> resourceSupplier,\n                       Function<? super U, ? extends SingleSource<? extends T>> singleFunction,\n                       Consumer<? super U> disposer,\n                       boolean eager) {",
            "    /**\n"
            "     * @param resourceSupplier 提供资源 U 的 Supplier\n"
            "     * @param singleFunction 将资源映射为 SingleSource 的函数\n"
            "     * @param disposer 释放资源的 Consumer\n"
            "     * @param eager true 时在 onSuccess/onError 前释放资源\n"
            "     */\n"
            "    public SingleUsing(Supplier<U> resourceSupplier,\n                       Function<? super U, ? extends SingleSource<? extends T>> singleFunction,\n                       Consumer<? super U> disposer,\n                       boolean eager) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(final SingleObserver<? super T> observer) {",
            "    /** 获取资源、应用 singleFunction 并订阅 UsingSingleObserver。 */\n"
            "    @Override\n    protected void subscribeActual(final SingleObserver<? super T> observer) {",
        ),
        (
            "    static final class UsingSingleObserver<T, U> extends\n    AtomicReference<Object> implements SingleObserver<T>, Disposable {",
            "    /** 持有资源引用：按 eager 标志在信号前后调用 disposer。 */\n"
            "    static final class UsingSingleObserver<T, U> extends\n    AtomicReference<Object> implements SingleObserver<T>, Disposable {",
        ),
        (
            "        @Override\n        public void dispose() {",
            "        /** eager 时先 disposeResource 再 upstream.dispose，否则相反。 */\n"
            "        @Override\n        public void dispose() {",
        ),
        (
            "        @SuppressWarnings(\"unchecked\")\n        @Override\n        public void onSuccess(T value) {",
            "        /** eager 时先释放资源再 onSuccess；否则先 onSuccess 再释放。 */\n"
            "        @SuppressWarnings(\"unchecked\")\n        @Override\n        public void onSuccess(T value) {",
        ),
        (
            "        @SuppressWarnings(\"unchecked\")\n        @Override\n        public void onError(Throwable e) {",
            "        /** eager 时先释放资源再 onError；disposer 异常合并为 CompositeException。 */\n"
            "        @SuppressWarnings(\"unchecked\")\n        @Override\n        public void onError(Throwable e) {",
        ),
        (
            "        @SuppressWarnings(\"unchecked\")\n        void disposeResource() {",
            "        /** getAndSet 取出资源并 disposer.accept；异常走 RxJavaPlugins.onError。 */\n"
            "        @SuppressWarnings(\"unchecked\")\n        void disposeResource() {",
        ),
    ],
    "SingleZipArray.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class SingleZipArray<T, R> extends Single<R> {",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 并行订阅 SingleSource 数组，全部 onSuccess 后\n"
            " * 以 Object[] 调用 zipper 合并为 R 并发射。\n"
            " *\n * @param <T> 各上游元素类型\n"
            " * @param <R> 合并结果类型\n"
            " */"
            "\npublic final class SingleZipArray<T, R> extends Single<R> {",
        ),
        (
            "    public SingleZipArray(SingleSource<? extends T>[] sources, Function<? super Object[], ? extends R> zipper) {",
            "    /**\n"
            "     * @param sources 上游 SingleSource 数组\n"
            "     * @param zipper 将 Object[] 合并为 R 的函数\n"
            "     */\n"
            "    public SingleZipArray(SingleSource<? extends T>[] sources, Function<? super Object[], ? extends R> zipper) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(SingleObserver<? super R> observer) {",
            "    /** n==1 时走 MapSingleObserver；否则 ZipCoordinator 并行订阅。 */\n"
            "    @Override\n    protected void subscribeActual(SingleObserver<? super R> observer) {",
        ),
        (
            "    static final class ZipCoordinator<T, R> extends AtomicInteger implements Disposable {",
            "    /** 计数器初始为 n：各 innerSuccess 递减，归零时 zipper.apply 并 onSuccess。 */\n"
            "    static final class ZipCoordinator<T, R> extends AtomicInteger implements Disposable {",
        ),
        (
            "        void innerSuccess(T value, int index) {",
            "        /** 写入 values[index]；decrementAndGet==0 时 zipper 合并发射。 */\n"
            "        void innerSuccess(T value, int index) {",
        ),
        (
            "        void innerError(Throwable ex, int index) {",
            "        /** 首错时 disposeExcept 其余上游并 onError。 */\n"
            "        void innerError(Throwable ex, int index) {",
        ),
        (
            "    static final class ZipSingleObserver<T>\n    extends AtomicReference<Disposable>\n    implements SingleObserver<T> {",
            "    /** 单路 zip Observer：onSuccess/onError 委托 ZipCoordinator。 */\n"
            "    static final class ZipSingleObserver<T>\n    extends AtomicReference<Disposable>\n    implements SingleObserver<T> {",
        ),
    ],
    "SingleZipIterable.java": [
        (
            "import io.reactivex.rxjava4.internal.operators.single.SingleZipArray.ZipCoordinator;\n\npublic final class SingleZipIterable<T, R> extends Single<R> {",
            "import io.reactivex.rxjava4.internal.operators.single.SingleZipArray.ZipCoordinator;\n\n"
            "/**\n"
            " * 将 Iterable 中的 SingleSource 收集为数组后 zip：\n"
            " * 全部 onSuccess 后以 zipper 合并为 R。\n"
            " *\n * @param <T> 各上游元素类型\n"
            " * @param <R> 合并结果类型\n"
            " */"
            "\npublic final class SingleZipIterable<T, R> extends Single<R> {",
        ),
        (
            "    public SingleZipIterable(Iterable<? extends SingleSource<? extends T>> sources, Function<? super Object[], ? extends R> zipper) {",
            "    /**\n"
            "     * @param sources 上游 SingleSource 的 Iterable\n"
            "     * @param zipper 将 Object[] 合并为 R 的函数\n"
            "     */\n"
            "    public SingleZipIterable(Iterable<? extends SingleSource<? extends T>> sources, Function<? super Object[], ? extends R> zipper) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(SingleObserver<? super R> observer) {",
            "    /** 迭代收集 sources 到数组，复用 ZipCoordinator 并行订阅。 */\n"
            "    @Override\n    protected void subscribeActual(SingleObserver<? super R> observer) {",
        ),
    ],
    "IsEnumerableStreamable.java": [
        (
            "/// Marker interface to indicate a [Streamable] source will produce\n/// an [EnumerableSource]-enabled [Streamer] and thus enables\n/// optimizations and operator fusion during assembly time.\n/// @param <T> the element type of the `Streamable`\n/// @since 4.0.0\npublic interface IsEnumerableStreamable<T> extends Streamable<T> {",
            "/// 标记接口：表示 [Streamable] 源将产生支持 [EnumerableSource] 的 [Streamer]，\n"
            "/// 从而在组装阶段启用优化与算子融合。\n"
            "/// @param <T> `Streamable` 的元素类型\n"
            "/// @since 4.0.0\n"
            "public interface IsEnumerableStreamable<T> extends Streamable<T> {",
        ),
    ],
    "IsIndexableStreamable.java": [
        (
            "/// Marker interface to indicate a [Streamable] source will produce\n/// an [IndexableSource]-enabled [Streamer] and thus enables\n/// optimizations and operator fusion during assembly time.\n/// @param <T> the element type of the `Streamable`\n/// @since 4.0.0\npublic interface IsIndexableStreamable<T> extends Streamable<T> {",
            "/// 标记接口：表示 [Streamable] 源将产生支持 [IndexableSource] 的 [Streamer]，\n"
            "/// 从而在组装阶段启用优化与算子融合。\n"
            "/// @param <T> `Streamable` 的元素类型\n"
            "/// @since 4.0.0\n"
            "public interface IsIndexableStreamable<T> extends Streamable<T> {",
        ),
    ],
    "IsSynchronousStreamable.java": [
        (
            "/// Marker interface to indicate a [Streamable] source will produce\n/// both an [IndexableSource] and an [EnumerableSource] capable\n/// [Streamer].\n/// @param <T> the element type of the `Streamable`\n/// @since 4.0.0\npublic interface IsSynchronousStreamable<T> extends IsIndexableStreamable<T>, IsEnumerableStreamable<T> {",
            "/// 标记接口：表示 [Streamable] 源将产生同时支持 [IndexableSource]\n"
            "/// 与 [EnumerableSource] 的 [Streamer]（同步可枚举/可索引）。\n"
            "/// @param <T> `Streamable` 的元素类型\n"
            "/// @since 4.0.0\n"
            "public interface IsSynchronousStreamable<T> extends IsIndexableStreamable<T>, IsEnumerableStreamable<T> {",
        ),
    ],
    "StageResumable.java": [
        (
            "/**\n * Represents a reusable ping-pong style notification exchange where\n * one use/thread can signal {@link #ready()} to wake up another use/thread\n * on a {@link #await()} call.\n * @param <T> the element type of the notification pass-around\n * @since 4.0.0\n */",
            "/**\n"
            " * 可复用的乒乓式通知交换：一方调用 {@link #ready()} 完成 CompletableFuture，\n"
            " * 唤醒另一方在 {@link #await()} 上等待的线程。\n"
            " * @param <T> 通知传递的元素类型\n"
            " * @since 4.0.0\n"
            " */",
        ),
        (
            "    /**\n     * When the producer has arranged the item transfer via some field or queue,\n     * call this method and call {@link CompletableFuture#complete(Object)}\n     * or {@link CompletableFuture#completeExceptionally(Throwable)} to\n     * signal resumption for any current or upcoming {@link #await()} caller.\n     * @return the {@code CompletableFuture} to complete in some way\n     */",
            "    /**\n"
            "     * 生产者通过字段或队列准备好数据后调用；\n"
            "     * 对返回的 CompletableFuture 调用 complete/completeExceptionally\n"
            "     * 以唤醒当前或后续 {@link #await()} 调用方。\n"
            "     * @return 待完成的 {@code CompletableFuture}\n"
            "     */",
        ),
        (
            "    /**\n     * When the consumer is ready to receive an item, call this method\n     * and apply a continuation function, such as {@link CompletableFuture#whenComplete(BiConsumer)}\n     * to it to handle the signal and process any external data made ready.\n     * @return the {@code CompletableFuture} to observe a completion value or exception\n     */",
            "    /**\n"
            "     * 消费者准备接收元素时调用；\n"
            "     * 返回 whenComplete(this) 包装的 CompletableFuture 以观察完成信号。\n"
            "     * @return 待观察完成值或异常的 {@code CompletableFuture}\n"
            "     */",
        ),
        (
            "    /// Used to clear any waiting [CompletableFuture] when the await finishes\n    /// no concern to users and should not be called.\n    /// @param t the completion value if any, ignored\n    /// @param u the exception if any, ignored\n    @Override\n    public void accept(T t, Throwable u) {",
            "    /// await 完成时清除等待中的 [CompletableFuture]；用户无需调用。\n"
            "    /// @param t 完成值（若有），忽略\n"
            "    /// @param u 异常（若有），忽略\n"
            "    @Override\n    public void accept(T t, Throwable u) {",
        ),
    ],
    "StreamSinkLambda.java": [
        (
            "/**\n * Creates a {@link StreamSink} via lambda callbacks for {@link #next(Object)} and\n * {@link #finish(Throwable)}.\n * @param <T> the element type of the stream\n * @param onNext the callback for the {@code next} method\n * @param onFinish the callback for the {@code finish} method\n * @since 4.0.0\n */",
            "/**\n"
            " * 通过 onNext/onFinish 两个 lambda 回调创建 {@link StreamSink}。\n"
            " * @param <T> 流元素类型\n"
            " * @param onNext {@code next} 方法的回调\n"
            " * @param onFinish {@code finish} 方法的回调\n"
            " * @since 4.0.0\n"
            " */",
        ),
        (
            "    @Override\n    public @NonNull CompletionStage<Boolean> next(@NonNull T item) {",
            "    /** 调用 onNext.apply；异常或 null 返回 failedStage。 */\n"
            "    @Override\n    public @NonNull CompletionStage<Boolean> next(@NonNull T item) {",
        ),
        (
            "    @Override\n    public @NonNull CompletionStage<Void> finish(@Nullable Throwable throwable) {",
            "    /** 调用 onFinish.apply；异常时 addSuppressed 原 throwable 并 failedStage。 */\n"
            "    @Override\n    public @NonNull CompletionStage<Void> finish(@Nullable Throwable throwable) {",
        ),
    ],
    "StreamSinkWithCancellation.java": [
        (
            "/**\n * Wraps a {@link StreamSink} and uses the given {@link DisposableStreamerCancellation} to be\n * returned via {@link #cancellation()}.\n * @param <T> the element type of the stream\n * @param downstream the {@code StreamSink} to relay events to\n * @param cancellation the {@code DisposableContainer} to be used for indicating cancellation\n * @since 4.0.0\n */",
            "/**\n"
            " * 包装 {@link StreamSink}，并通过 {@link #cancellation()} 返回\n"
            " * 指定的 {@link DisposableStreamerCancellation}。\n"
            " * @param <T> 流元素类型\n"
            " * @param downstream 转发事件的 {@code StreamSink}\n"
            " * @param cancellation 用于表示取消的 {@code DisposableStreamerCancellation}\n"
            " * @since 4.0.0\n"
            " */",
        ),
        (
            "    @Override\n    @NonNull\n    public CompletionStage<Boolean> next(@NonNull T item) {",
            "    /** 委托 downstream.next(item)。 */\n"
            "    @Override\n    @NonNull\n    public CompletionStage<Boolean> next(@NonNull T item) {",
        ),
        (
            "    @Override\n    @NonNull\n    public CompletionStage<Void> finish(@Nullable Throwable throwable) {",
            "    /** 委托 downstream.finish(throwable)。 */\n"
            "    @Override\n    @NonNull\n    public CompletionStage<Void> finish(@Nullable Throwable throwable) {",
        ),
    ],
    "StreamableBlocking.java": [
        (
            "public record StreamableBlocking() {",
            "/** Streamable 阻塞消费工具：blockingFirst/blockingLast 同步取首/末元素。 */\n"
            "public record StreamableBlocking() {",
        ),
        (
            "    /**\n     * Consumes the first item and finishes the {@link Streamable},\n     * throwing {@link NoSuchElementException} if the source is empty.\n     * @param <T> the element type\n     * @param source the source {@code Streamable}\n     * @return the first item\n     * @throws RuntimeException if the source signals an unchecked exception\n     * @throws CompletionException if the source signals a checked exception\n     */",
            "    /**\n"
            "     * 阻塞消费首个元素并 awaitFinish；空序列抛 NoSuchElementException。\n"
            "     * @param <T> 元素类型\n"
            "     * @param source 源 {@code Streamable}\n"
            "     * @return 首个元素\n"
            "     * @throws RuntimeException 上游抛出 unchecked 异常时\n"
            "     * @throws CompletionException 上游抛出 checked 异常时\n"
            "     */",
        ),
        (
            "    /**\n     * Consumes the first item and finishes the {@link Streamable},\n     * throwing {@link NoSuchElementException} if the source is empty.\n     * @param <T> the element type\n     * @param source the source {@code Streamable}\n     * @param cancellation the external cancellation manager\n     * @return the first item\n     * @throws RuntimeException if the source signals an unchecked exception\n     * @throws CompletionException if the source signals a checked exception\n     */",
            "    /**\n"
            "     * 带外部 cancellation 的 blockingFirst：awaitNext 取首元素后 awaitFinish。\n"
            "     * @param <T> 元素类型\n"
            "     * @param source 源 {@code Streamable}\n"
            "     * @param cancellation 外部取消管理器\n"
            "     * @return 首个元素\n"
            "     * @throws RuntimeException 上游抛出 unchecked 异常时\n"
            "     * @throws CompletionException 上游抛出 checked 异常时\n"
            "     */",
        ),
        (
            "    /**\n     * Consumes all upstream items and returns the very last or throws\n     * a {@link NoSuchElementException}.\n     * @param <T> the element type\n     * @param source the source sequence\n     * @return the very last value\n     * @throws RuntimeException if the source signals an unchecked exception\n     * @throws CompletionException if the source signals a checked exception\n     */",
            "    /**\n"
            "     * 阻塞消费全部元素并返回最后一个；空序列抛 NoSuchElementException。\n"
            "     * @param <T> 元素类型\n"
            "     * @param source 源序列\n"
            "     * @return 最后一个元素\n"
            "     * @throws RuntimeException 上游抛出 unchecked 异常时\n"
            "     * @throws CompletionException 上游抛出 checked 异常时\n"
            "     */",
        ),
        (
            "    /**\n     * Consumes all upstream items and returns the very last or throws\n     * a {@link NoSuchElementException}.\n     * @param <T> the element type\n     * @param source the source sequence\n     * @param cancellation the external cancellation manager\n     * @return the very last value\n     * @throws RuntimeException if the source signals an unchecked exception\n     * @throws CompletionException if the source signals a checked exception\n     */",
            "    /**\n"
            "     * 带 cancellation 的 blockingLast：while(awaitNext) 更新 result 后 awaitFinish。\n"
            "     * @param <T> 元素类型\n"
            "     * @param source 源序列\n"
            "     * @param cancellation 外部取消管理器\n"
            "     * @return 最后一个元素\n"
            "     * @throws RuntimeException 上游抛出 unchecked 异常时\n"
            "     * @throws CompletionException 上游抛出 checked 异常时\n"
            "     */",
        ),
        (
            "    public static <T> T blockingFirst(Streamable<T> source, StreamerCancellation cancellation) {",
            "    /** awaitNext 取 current()，合并 next/finish 异常后返回或抛 NoSuchElementException。 */\n"
            "    public static <T> T blockingFirst(Streamable<T> source, StreamerCancellation cancellation) {",
        ),
        (
            "    public static <T> T blockingLast(Streamable<T> source, StreamerCancellation cancellation) {",
            "    /** while(awaitNext) 循环更新 result，awaitFinish 后返回末元素。 */\n"
            "    public static <T> T blockingLast(Streamable<T> source, StreamerCancellation cancellation) {",
        ),
    ],
}


def has_chinese(text: str) -> bool:
    return bool(re.search(r"[\u4e00-\u9fff]", text))


def tree_guard(env: dict[str, str] | None = None) -> int:
    tracked = len(
        subprocess.check_output(["git", "-C", str(ROOT), "ls-files"], env=env).splitlines()
    )
    if tracked < 50000:
        raise RuntimeError(f"tree guard failed: tracked={tracked} (expected >=50000)")
    for path in GUARD_FILES:
        if not path.exists():
            raise RuntimeError(f"guard file missing: {path}")
        text = path.read_text(encoding="utf-8")
        if env is not None:
            rel = str(path.relative_to(ROOT))
            try:
                text = subprocess.check_output(
                    ["git", "-C", str(ROOT), "show", f":{rel}"], env=env, text=True
                )
            except subprocess.CalledProcessError:
                pass
        if not has_chinese(text):
            raise RuntimeError(f"guard file lacks Chinese: {path}")
    return tracked


def apply_replacements(text: str, replacements: list[tuple[str, str]]) -> str:
    for old, new in replacements:
        if old not in text:
            raise ValueError(f"Pattern not found:\n{old[:120]}...")
        text = text.replace(old, new, 1)
    return text


def annotate_file(rel: str) -> None:
    name = Path(rel).name
    src = ORIGINAL / rel
    dst = ANALYZED / rel
    reps = FILE_REPLACEMENTS.get(name, [])
    if not reps:
        raise ValueError(f"NO_REPLACEMENTS: {rel}")
    dst.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(src, dst)
    text = apply_replacements(dst.read_text(encoding="utf-8"), reps)
    cn = len(re.findall(r"[\u4e00-\u9fff]", text))
    if cn < 10 or "Licensed under the Apache License" not in text:
        raise ValueError(f"VALIDATION cn={cn}: {rel}")
    dst.write_text(text, encoding="utf-8")


def isolated_index_commit(
    message: str, paths: list[str], base_ref: str = "origin/main"
) -> tuple[str, int]:
    index_file = Path("/tmp/git-index-rxjava-w23b")
    env = os.environ.copy()
    env["GIT_INDEX_FILE"] = str(index_file)
    base = subprocess.check_output(
        ["git", "-C", str(ROOT), "rev-parse", base_ref], text=True, env=env
    ).strip()
    subprocess.run(["git", "-C", str(ROOT), "read-tree", base], env=env, check=True)
    subprocess.run(["git", "-C", str(ROOT), "add", "--", *paths], env=env, check=True)
    tree_count = tree_guard(env)
    tree = subprocess.check_output(
        ["git", "-C", str(ROOT), "write-tree"], env=env, text=True
    ).strip()
    commit = subprocess.check_output(
        ["git", "-C", str(ROOT), "commit-tree", tree, "-p", base, "-m", message],
        text=True,
        env=env,
    ).strip()
    subprocess.run(
        ["git", "-C", str(ROOT), "update-ref", "refs/heads/main", commit], check=True
    )
    index_file.unlink(missing_ok=True)
    return commit, tree_count


def confirm_chinese_on_origin() -> dict[str, bool]:
    subprocess.run(["git", "-C", str(ROOT), "fetch", "origin", "main"], check=True)
    result: dict[str, bool] = {}
    for rel in BATCH_FILES:
        blob = subprocess.check_output(
            [
                "git",
                "-C",
                str(ROOT),
                "show",
                f"origin/main:rxjava/4.0.0-alpha-21/analyzed/{rel}",
            ],
            text=True,
        )
        result[rel] = has_chinese(blob)
    return result


def main() -> int:
    failures: list[str] = []
    for rel in BATCH_FILES:
        try:
            annotate_file(rel)
            print(f"OK {rel}")
        except Exception as e:
            failures.append(f"{rel}: {e}")
            print(f"FAIL {rel}: {e}")
    if failures:
        print(json.dumps({"ok": 0, "failures": failures}, ensure_ascii=False, indent=2))
        return 1

    analyzed_paths = [f"rxjava/4.0.0-alpha-21/analyzed/{rel}" for rel in BATCH_FILES]
    script_path = f"scripts/{SCRIPT_NAME}"
    sha, tree_count = isolated_index_commit(
        "rxjava 4.0.0-alpha-21: Chinese-annotate wave 23b [15:30]",
        [*analyzed_paths, script_path],
    )
    subprocess.run(["git", "-C", str(ROOT), "push", "-u", "origin", "main"], check=True)

    subprocess.run(
        [
            sys.executable,
            str(SCRIPTS / "mark_batch_done.py"),
            "--project",
            "rxjava",
            "--version",
            "4.0.0-alpha-21",
            "--note",
            "wave23b",
            *BATCH_FILES,
        ],
        check=True,
    )
    queue_paths = [
        "rxjava/4.0.0-alpha-21/_reports/class-queue/done.txt",
        "rxjava/4.0.0-alpha-21/_reports/class-queue/pending.txt",
        "rxjava/4.0.0-alpha-21/_reports/class-queue/batch.json",
        "rxjava/4.0.0-alpha-21/_reports/class-queue/worker.log",
    ]
    queue_sha, _ = isolated_index_commit(
        "queue: mark rxjava 4.0.0-alpha-21 wave23b done",
        queue_paths,
        base_ref="HEAD",
    )
    subprocess.run(["git", "-C", str(ROOT), "push", "origin", "main"], check=True)

    done_total = len(
        [ln for ln in (QUEUE / "done.txt").read_text(encoding="utf-8").splitlines() if ln.strip()]
    )
    pending_total = len(
        [
            ln
            for ln in (QUEUE / "pending.txt").read_text(encoding="utf-8").splitlines()
            if ln.strip()
        ]
    )
    chinese = confirm_chinese_on_origin()
    print(
        json.dumps(
            {
                "sha": sha,
                "queue_sha": queue_sha,
                "tree_count": tree_count,
                "done": done_total,
                "pending": pending_total,
                "chinese_confirmed": chinese,
                "all_chinese": all(chinese.values()),
            },
            ensure_ascii=False,
            indent=2,
        )
    )
    return 0 if all(chinese.values()) else 1


if __name__ == "__main__":
    raise SystemExit(main())
