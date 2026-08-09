#!/usr/bin/env python3
"""Chinese-annotate RxJava 4.0.0-alpha-21 wave-10a Flowable* [0:15]."""
from __future__ import annotations

import json
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
WAVE10A_FILE = Path("/tmp/rxjava_w10a.txt")
BATCH_FILES = [
    ln.strip()
    for ln in WAVE10A_FILE.read_text(encoding="utf-8").splitlines()
    if ln.strip()
]

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "FlowableElementAtSingle.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class FlowableElementAtSingle",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 将上游第 index 个元素作为 {@link Single} 发射；\n"
            " * 不足时按 defaultValue 或 onError({@link NoSuchElementException}) 处理。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class FlowableElementAtSingle",
        ),
        (
            "    public FlowableElementAtSingle(Flowable<T> source, long index, T defaultValue) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param index 目标索引（0 起）\n"
            "     * @param defaultValue 元素不足时的默认值（null 则 onError）\n"
            "     */\n"
            "    public FlowableElementAtSingle(Flowable<T> source, long index, T defaultValue) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(SingleObserver<? super T> observer) {",
            "    /** 订阅 source 并在到达 index 时 onSuccess 目标元素。 */\n"
            "    @Override\n    protected void subscribeActual(SingleObserver<? super T> observer) {",
        ),
        (
            "    @Override\n    public Flowable<T> fuseToFlowable() {",
            "    /** 融合为 {@link FlowableElementAt} 以支持背压。 */\n"
            "    @Override\n    public Flowable<T> fuseToFlowable() {",
        ),
        (
            "    static final class ElementAtSubscriber<T> implements FlowableSubscriber<T>, Disposable {",
            "    /** 计数上游元素并在 index 处 cancel 并 onSuccess。 */\n"
            "    static final class ElementAtSubscriber<T> implements FlowableSubscriber<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** count 达 index 时 cancel 上游并 onSuccess(t)。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** 未达 index 时按 defaultValue 或 NoSuchElementException 完成。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
    ],
    "FlowableEmpty.java": [
        (
            "/**\n * A source Flowable that signals an onSubscribe() + onComplete() only.\n */",
            "/**\n"
            " * 仅发出 onSubscribe 与 onComplete 的空 {@link Flowable} 源。\n"
            " */",
        ),
        (
            "    @Override\n    public void subscribeActual(Subscriber<? super Object> s) {",
            "    /** 通过 {@link EmptySubscription#complete} 立即完成。 */\n"
            "    @Override\n    public void subscribeActual(Subscriber<? super Object> s) {",
        ),
        (
            "        return null; // null scalar is interpreted as being empty",
            "        return null; // null 标量值表示空序列",
        ),
    ],
    "FlowableError.java": [
        (
            "import io.reactivex.rxjava4.internal.util.ExceptionHelper;\n\npublic final class FlowableError",
            "import io.reactivex.rxjava4.internal.util.ExceptionHelper;\n\n"
            "/**\n"
            " * 订阅时立即以 {@link Supplier} 提供的 {@link Throwable} 终止的 {@link Flowable}。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class FlowableError",
        ),
        (
            "    public FlowableError(Supplier<? extends Throwable> errorSupplier) {",
            "    /** @param errorSupplier 提供终止错误的 Supplier */\n"
            "    public FlowableError(Supplier<? extends Throwable> errorSupplier) {",
        ),
        (
            "    @Override\n    public void subscribeActual(Subscriber<? super T> s) {",
            "    /** 调用 errorSupplier 并通过 EmptySubscription 转发错误。 */\n"
            "    @Override\n    public void subscribeActual(Subscriber<? super T> s) {",
        ),
    ],
    "FlowableFilter.java": [
        (
            "import io.reactivex.rxjava4.operators.QueueSubscription;\n\npublic final class FlowableFilter",
            "import io.reactivex.rxjava4.operators.QueueSubscription;\n\n"
            "/**\n"
            " * 仅向下游转发满足 {@link Predicate} 的上游元素。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class FlowableFilter",
        ),
        (
            "    public FlowableFilter(Flowable<T> source, Predicate<? super T> predicate) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param predicate 过滤谓词\n"
            "     */\n"
            "    public FlowableFilter(Flowable<T> source, Predicate<? super T> predicate) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> s) {",
            "    /** ConditionalSubscriber 走融合路径，否则使用 FilterSubscriber。 */\n"
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> s) {",
        ),
        (
            "    static final class FilterSubscriber<T> extends BasicFuseableSubscriber<T, T>\n    implements ConditionalSubscriber<T> {",
            "    /** 对非 Conditional 下游应用 predicate 过滤。 */\n"
            "    static final class FilterSubscriber<T> extends BasicFuseableSubscriber<T, T>\n    implements ConditionalSubscriber<T> {",
        ),
        (
            "        @Override\n        public boolean tryOnNext(T t) {",
            "        /** predicate 为 true 时转发元素；融合模式下直接转发 null。 */\n"
            "        @Override\n        public boolean tryOnNext(T t) {",
        ),
        (
            "        @Nullable\n        @Override\n        public T poll() throws Throwable {",
            "        /** 从队列 poll 直至 predicate 匹配或队列为空。 */\n"
            "        @Nullable\n        @Override\n        public T poll() throws Throwable {",
        ),
        (
            "    static final class FilterConditionalSubscriber<T> extends BasicFuseableConditionalSubscriber<T, T> {",
            "    /** 对 ConditionalSubscriber 下游应用 predicate 过滤。 */\n"
            "    static final class FilterConditionalSubscriber<T> extends BasicFuseableConditionalSubscriber<T, T> {",
        ),
    ],
    "FlowableFlatMapCompletable.java": [
        (
            "/**\n * Maps a sequence of values into CompletableSources and awaits their termination.\n * @param <T> the value type\n */",
            "/**\n"
            " * 将上游每个元素映射为 {@link CompletableSource} 并等待其全部终止；\n"
            " * 不向 downstream 发射元素。\n"
            " * @param <T> 上游元素类型\n"
            " */",
        ),
        (
            "    public FlowableFlatMapCompletable(Flowable<T> source,\n            Function<? super T, ? extends CompletableSource> mapper, boolean delayErrors,\n            int maxConcurrency) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param mapper 将元素映射为 CompletableSource 的函数\n"
            "     * @param delayErrors 为 true 时收集所有错误后再终止\n"
            "     * @param maxConcurrency 最大并行 inner 订阅数\n"
            "     */\n"
            "    public FlowableFlatMapCompletable(Flowable<T> source,\n            Function<? super T, ? extends CompletableSource> mapper, boolean delayErrors,\n            int maxConcurrency) {",
        ),
        (
            "    static final class FlatMapCompletableMainSubscriber<T> extends BasicIntQueueSubscription<T>\n    implements FlowableSubscriber<T> {",
            "    /** 并行订阅 inner Completable 并在全部完成后终止 downstream。 */\n"
            "    static final class FlatMapCompletableMainSubscriber<T> extends BasicIntQueueSubscription<T>\n    implements FlowableSubscriber<T> {",
        ),
        (
            "            // ignored, no values emitted",
            "            // 忽略：不向 downstream 发射元素",
        ),
        (
            "            return null; // always empty",
            "            return null; // 始终为空",
        ),
        (
            "            return true; // always empty",
            "            return true; // 始终为空",
        ),
        (
            "            // nothing to clear",
            "            // 无内容可清空",
        ),
        (
            "        void innerComplete(InnerConsumer inner) {",
            "        /** inner 完成时从 set 移除并递减活跃计数。 */\n"
            "        void innerComplete(InnerConsumer inner) {",
        ),
        (
            "        void innerError(InnerConsumer inner, Throwable e) {",
            "        /** inner 出错时从 set 移除并转发错误。 */\n"
            "        void innerError(InnerConsumer inner, Throwable e) {",
        ),
        (
            "        final class InnerConsumer extends AtomicReference<Disposable> implements CompletableObserver, Disposable {",
            "        /** 订阅单个 inner Completable 并报告完成/错误。 */\n"
            "        final class InnerConsumer extends AtomicReference<Disposable> implements CompletableObserver, Disposable {",
        ),
    ],
    "FlowableFlatMapCompletableCompletable.java": [
        (
            "/**\n * Maps a sequence of values into CompletableSources and awaits their termination.\n * @param <T> the value type\n */",
            "/**\n"
            " * 将上游每个元素映射为 {@link CompletableSource} 并等待其全部终止，\n"
            " * 以 {@link Completable} 形式暴露。\n"
            " * @param <T> 上游元素类型\n"
            " */",
        ),
        (
            "    @Override\n    public Flowable<T> fuseToFlowable() {",
            "    /** 融合为 {@link FlowableFlatMapCompletable} 以支持背压。 */\n"
            "    @Override\n    public Flowable<T> fuseToFlowable() {",
        ),
        (
            "    static final class FlatMapCompletableMainSubscriber<T> extends AtomicInteger\n    implements FlowableSubscriber<T>, Disposable {",
            "    /** 并行订阅 inner Completable 并在全部完成后通知 CompletableObserver。 */\n"
            "    static final class FlatMapCompletableMainSubscriber<T> extends AtomicInteger\n    implements FlowableSubscriber<T>, Disposable {",
        ),
        (
            "        void innerComplete(InnerObserver inner) {",
            "        /** inner 完成时从 set 移除并递减活跃计数。 */\n"
            "        void innerComplete(InnerObserver inner) {",
        ),
        (
            "        void innerError(InnerObserver inner, Throwable e) {",
            "        /** inner 出错时从 set 移除并转发错误。 */\n"
            "        void innerError(InnerObserver inner, Throwable e) {",
        ),
        (
            "        final class InnerObserver extends AtomicReference<Disposable> implements CompletableObserver, Disposable {",
            "        /** 订阅单个 inner Completable 并报告完成/错误。 */\n"
            "        final class InnerObserver extends AtomicReference<Disposable> implements CompletableObserver, Disposable {",
        ),
    ],
    "FlowableFlatMapMaybePublisher.java": [
        (
            "/**\n * Maps upstream values into MaybeSources and merges their signals into one sequence.\n * @param <T> the source value type\n * @param <R> the result value type\n */",
            "/**\n"
            " * 将上游每个元素映射为 {@link MaybeSource} 并合并其信号为单一序列。\n"
            " * @param <T> 上游元素类型\n"
            " * @param <R> 输出元素类型\n"
            " */",
        ),
        (
            "    public FlowableFlatMapMaybePublisher(Publisher<T> source, Function<? super T, ? extends MaybeSource<? extends R>> mapper,\n            boolean delayError, int maxConcurrency) {",
            "    /**\n"
            "     * @param source 上游 Publisher\n"
            "     * @param mapper 将元素映射为 MaybeSource 的函数\n"
            "     * @param delayError 为 true 时延迟报告错误\n"
            "     * @param maxConcurrency 最大并行 inner 订阅数\n"
            "     */\n"
            "    public FlowableFlatMapMaybePublisher(Publisher<T> source, Function<? super T, ? extends MaybeSource<? extends R>> mapper,\n            boolean delayError, int maxConcurrency) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Subscriber<? super R> s) {",
            "    /** 复用 {@link FlowableFlatMapMaybe.FlatMapMaybeSubscriber} 订阅 source。 */\n"
            "    @Override\n    protected void subscribeActual(Subscriber<? super R> s) {",
        ),
    ],
    "FlowableFlatMapSingle.java": [
        (
            "/**\n * Maps upstream values into SingleSources and merges their signals into one sequence.\n * @param <T> the source value type\n * @param <R> the result value type\n */",
            "/**\n"
            " * 将上游每个元素映射为 {@link SingleSource} 并合并其成功值为单一序列。\n"
            " * @param <T> 上游元素类型\n"
            " * @param <R> 输出元素类型\n"
            " */",
        ),
        (
            "    public FlowableFlatMapSingle(Flowable<T> source, Function<? super T, ? extends SingleSource<? extends R>> mapper,\n            boolean delayError, int maxConcurrency) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param mapper 将元素映射为 SingleSource 的函数\n"
            "     * @param delayError 为 true 时延迟报告错误\n"
            "     * @param maxConcurrency 最大并行 inner 订阅数\n"
            "     */\n"
            "    public FlowableFlatMapSingle(Flowable<T> source, Function<? super T, ? extends SingleSource<? extends R>> mapper,\n            boolean delayError, int maxConcurrency) {",
        ),
        (
            "    static final class FlatMapSingleSubscriber<T, R>\n    extends AtomicInteger\n    implements FlowableSubscriber<T>, Subscription {",
            "    /** 并行订阅 inner Single 并按背压 drain 合并成功值。 */\n"
            "    static final class FlatMapSingleSubscriber<T, R>\n    extends AtomicInteger\n    implements FlowableSubscriber<T>, Subscription {",
        ),
        (
            "        void innerSuccess(InnerObserver inner, R value) {",
            "        /** inner 成功时将 value 入队或直接 onNext，并递减 active 计数。 */\n"
            "        void innerSuccess(InnerObserver inner, R value) {",
        ),
        (
            "        void innerError(InnerObserver inner, Throwable e) {",
            "        /** inner 出错时记录错误并按 delayErrors 决定是否取消上游。 */\n"
            "        void innerError(InnerObserver inner, Throwable e) {",
        ),
        (
            "        void drain() {",
            "        /** 触发 drainLoop 合并队列中的成功值。 */\n"
            "        void drain() {",
        ),
        (
            "        void drainLoop() {",
            "        /** 按 requested 背压从队列 poll 并 onNext，全部完成时终止。 */\n"
            "        void drainLoop() {",
        ),
        (
            "        final class InnerObserver extends AtomicReference<Disposable>\n        implements SingleObserver<R>, Disposable {",
            "        /** 订阅单个 inner Single 并报告成功/错误。 */\n"
            "        final class InnerObserver extends AtomicReference<Disposable>\n        implements SingleObserver<R>, Disposable {",
        ),
    ],
    "FlowableFlatMapSinglePublisher.java": [
        (
            "/**\n * Maps upstream values into SingleSources and merges their signals into one sequence.\n * @param <T> the source value type\n * @param <R> the result value type\n */",
            "/**\n"
            " * 将上游每个元素映射为 {@link SingleSource} 并合并其成功值为单一序列；\n"
            " * 适用于任意 {@link Publisher} 源。\n"
            " * @param <T> 上游元素类型\n"
            " * @param <R> 输出元素类型\n"
            " */",
        ),
        (
            "    public FlowableFlatMapSinglePublisher(Publisher<T> source, Function<? super T, ? extends SingleSource<? extends R>> mapper,\n            boolean delayError, int maxConcurrency) {",
            "    /**\n"
            "     * @param source 上游 Publisher\n"
            "     * @param mapper 将元素映射为 SingleSource 的函数\n"
            "     * @param delayError 为 true 时延迟报告错误\n"
            "     * @param maxConcurrency 最大并行 inner 订阅数\n"
            "     */\n"
            "    public FlowableFlatMapSinglePublisher(Publisher<T> source, Function<? super T, ? extends SingleSource<? extends R>> mapper,\n            boolean delayError, int maxConcurrency) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Subscriber<? super R> s) {",
            "    /** 复用 {@link FlowableFlatMapSingle.FlatMapSingleSubscriber} 订阅 source。 */\n"
            "    @Override\n    protected void subscribeActual(Subscriber<? super R> s) {",
        ),
    ],
    "FlowableFromAction.java": [
        (
            "/**\n * Executes an {@link Action} and signals its exception or completes normally.\n *\n * @param <T> the value type\n * @since 3.0.0\n */",
            "/**\n"
            " * 执行 {@link Action} 并在正常完成时 onComplete，异常时 onError。\n"
            " *\n"
            " * @param <T> 元素类型\n"
            " * @since 3.0.0\n"
            " */",
        ),
        (
            "    public FlowableFromAction(Action action) {",
            "    /** @param action 要执行的 Action */\n"
            "    public FlowableFromAction(Action action) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> subscriber) {",
            "    /** 运行 action 并转发完成或错误；不发射元素。 */\n"
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> subscriber) {",
        ),
        (
            "        return null; // considered as onComplete()",
            "        return null; // 视为 onComplete()",
        ),
    ],
    "FlowableFromArray.java": [
        (
            "import java.util.Objects;\n\npublic final class FlowableFromArray",
            "import java.util.Objects;\n\n"
            "/**\n"
            " * 按顺序发出数组中各元素的 {@link Flowable}。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class FlowableFromArray",
        ),
        (
            "    public FlowableFromArray(T[] array) {",
            "    /** @param array 要发出的元素数组 */\n"
            "    public FlowableFromArray(T[] array) {",
        ),
        (
            "    abstract static class BaseArraySubscription<T> extends BasicQueueSubscription<T> {",
            "    /** 数组订阅基类：支持 SYNC 融合与背压 request。 */\n"
            "    abstract static class BaseArraySubscription<T> extends BasicQueueSubscription<T> {",
        ),
        (
            "        abstract void fastPath();",
            "        /** 无背压限制时一次性发出剩余元素。 */\n"
            "        abstract void fastPath();",
        ),
        (
            "        abstract void slowPath(long r);",
            "        /** 按 request 数量逐批发出元素。 */\n"
            "        abstract void slowPath(long r);",
        ),
        (
            "    static final class ArraySubscription<T> extends BaseArraySubscription<T> {",
            "    /** 向普通 Subscriber 按序发出数组元素。 */\n"
            "    static final class ArraySubscription<T> extends BaseArraySubscription<T> {",
        ),
        (
            "    static final class ArrayConditionalSubscription<T> extends BaseArraySubscription<T> {",
            "    /** 向 ConditionalSubscriber 按序发出数组元素。 */\n"
            "    static final class ArrayConditionalSubscription<T> extends BaseArraySubscription<T> {",
        ),
    ],
    "FlowableFromCallable.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class FlowableFromCallable",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 调用 {@link Callable} 并将其返回值作为唯一元素发出。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class FlowableFromCallable",
        ),
        (
            "    public FlowableFromCallable(Callable<? extends T> callable) {",
            "    /** @param callable 提供单个元素的 Callable */\n"
            "    public FlowableFromCallable(Callable<? extends T> callable) {",
        ),
        (
            "    @Override\n    public void subscribeActual(Subscriber<? super T> s) {",
            "    /** 调用 callable 并通过 DeferredScalarSubscription 发出结果。 */\n"
            "    @Override\n    public void subscribeActual(Subscriber<? super T> s) {",
        ),
        (
            "    @Override\n    public T get() throws Throwable {",
            "    /** 直接调用 callable 并返回结果（Supplier 接口）。 */\n"
            "    @Override\n    public T get() throws Throwable {",
        ),
    ],
    "FlowableFromCompletable.java": [
        (
            "/**\n * Wrap a Completable into a Flowable.\n *\n * @param <T> the value type\n * @since 3.0.0\n */",
            "/**\n"
            " * 将 {@link CompletableSource} 包装为 {@link Flowable}；\n"
            " * 完成时 onComplete，出错时 onError，不发射元素。\n"
            " *\n"
            " * @param <T> 元素类型\n"
            " * @since 3.0.0\n"
            " */",
        ),
        (
            "    public FlowableFromCompletable(CompletableSource source) {",
            "    /** @param source 上游 CompletableSource */\n"
            "    public FlowableFromCompletable(CompletableSource source) {",
        ),
        (
            "    @Override\n    public CompletableSource source() {",
            "    /** 返回上游 CompletableSource。 */\n"
            "    @Override\n    public CompletableSource source() {",
        ),
        (
            "    public static final class FromCompletableObserver<T>\n    extends AbstractEmptyQueueFuseable<T>\n    implements CompletableObserver {",
            "    /** 将 Completable 终止事件转发给 Flowable Subscriber。 */\n"
            "    public static final class FromCompletableObserver<T>\n    extends AbstractEmptyQueueFuseable<T>\n    implements CompletableObserver {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** Completable 完成时通知 downstream onComplete。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
        (
            "        @Override\n        public void onError(Throwable e) {",
            "        /** Completable 出错时转发 onError。 */\n"
            "        @Override\n        public void onError(Throwable e) {",
        ),
    ],
    "FlowableFromFuture.java": [
        (
            "import io.reactivex.rxjava4.internal.util.ExceptionHelper;\n\npublic final class FlowableFromFuture",
            "import io.reactivex.rxjava4.internal.util.ExceptionHelper;\n\n"
            "/**\n"
            " * 阻塞等待 {@link Future} 完成并将其结果作为唯一元素发出。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class FlowableFromFuture",
        ),
        (
            "    public FlowableFromFuture(Future<? extends T> future, long timeout, TimeUnit unit) {",
            "    /**\n"
            "     * @param future 要等待的 Future\n"
            "     * @param timeout 超时时间（unit 为 null 时不限时）\n"
            "     * @param unit 超时时间单位\n"
            "     */\n"
            "    public FlowableFromFuture(Future<? extends T> future, long timeout, TimeUnit unit) {",
        ),
        (
            "    @Override\n    public void subscribeActual(Subscriber<? super T> s) {",
            "    /** 调用 future.get 并通过 DeferredScalarSubscription 发出结果。 */\n"
            "    @Override\n    public void subscribeActual(Subscriber<? super T> s) {",
        ),
    ],
    "FlowableFromObservable.java": [
        (
            "import io.reactivex.rxjava4.disposables.Disposable;\n\npublic final class FlowableFromObservable",
            "import io.reactivex.rxjava4.disposables.Disposable;\n\n"
            "/**\n"
            " * 将 {@link ObservableSource} 适配为 {@link Flowable}；\n"
            " * request 被忽略（Observable 无背压）。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class FlowableFromObservable",
        ),
        (
            "    public FlowableFromObservable(ObservableSource<T> upstream) {",
            "    /** @param upstream 上游 ObservableSource */\n"
            "    public FlowableFromObservable(ObservableSource<T> upstream) {",
        ),
        (
            "    static final class SubscriberObserver<T> implements Observer<T>, Subscription {",
            "    /** 将 Observer 事件桥接到 Flowable Subscriber。 */\n"
            "    static final class SubscriberObserver<T> implements Observer<T>, Subscription {",
        ),
        (
            "            // no backpressure so nothing we can do about this",
            "            // Observable 无背压，request 被忽略",
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
    subprocess.run(
        [
            sys.executable,
            str(ROOT / "scripts/mark_batch_done.py"),
            "--project",
            "rxjava",
            "--version",
            "4.0.0-alpha-21",
            "--note",
            "wave10a Flowable* [0:15]",
            *files,
        ],
        check=True,
    )
    batch_path = QUEUE / "batch.json"
    batch = json.loads(batch_path.read_text(encoding="utf-8"))
    done_path = QUEUE / "done.txt"
    pending_path = QUEUE / "pending.txt"
    file_set = set(files)
    batch["files"] = [f for f in batch.get("files", []) if f not in file_set]
    batch["done"] = len([ln for ln in done_path.read_text(encoding="utf-8").splitlines() if ln.strip()])
    batch["remaining_pending"] = len(
        [ln for ln in pending_path.read_text(encoding="utf-8").splitlines() if ln.strip()]
    )
    batch_path.write_text(json.dumps(batch, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def main() -> int:
    failures: list[str] = []
    ok = 0
    for rel in BATCH_FILES:
        name = Path(rel).name
        src = ORIGINAL / rel
        dst = ANALYZED / rel
        if not src.exists():
            failures.append(f"MISSING original: {rel}")
            continue
        reps = FILE_REPLACEMENTS.get(name, [])
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
    if ok == len(BATCH_FILES) and not failures:
        mark_queue_done(BATCH_FILES)
        print(f"Marked {ok} files done in queue")
    print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False, indent=2))
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
