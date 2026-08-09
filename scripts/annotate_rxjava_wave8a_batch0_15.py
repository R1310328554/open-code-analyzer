#!/usr/bin/env python3
"""Chinese-annotate RxJava 4.0.0-alpha-21 wave-8a Completable* [0:15]."""
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
WAVE8A_FILE = Path("/tmp/rxjava_w8a.txt")
BATCH_FILES = [
    ln.strip()
    for ln in WAVE8A_FILE.read_text(encoding="utf-8").splitlines()
    if ln.strip()
]

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "CompletableMaterialize.java": [
        (
            "/**\n * Turn the signal types of a Completable source into a single Notification of\n * equal kind.\n * <p>History: 2.2.4 - experimental\n *\n * @param <T> the element type of the source\n * @since 3.0.0\n */",
            "/**\n * 将 {@link Completable} 源的信号类型转换为同类型的单个 {@link Notification}。\n * <p>History: 2.2.4 - experimental\n *\n * @param <T> 源元素类型\n * @since 3.0.0\n */",
        ),
        (
            "    public CompletableMaterialize(Completable source) {",
            "    /** @param source 要物化的 Completable 源 */\n"
            "    public CompletableMaterialize(Completable source) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(SingleObserver<? super Notification<T>> observer) {",
            "    /** 订阅 source 并将终止事件包装为 Notification 发出。 */\n"
            "    @Override\n    protected void subscribeActual(SingleObserver<? super Notification<T>> observer) {",
        ),
    ],
    "CompletableMerge.java": [
        (
            "import io.reactivex.rxjava4.internal.util.AtomicThrowable;\n\npublic final class CompletableMerge extends Completable {",
            "import io.reactivex.rxjava4.internal.util.AtomicThrowable;\n\n"
            "/**\n"
            " * 合并 {@link Publisher} 发出的多个 {@link CompletableSource}，\n"
            " * 受 maxConcurrency 限制并行订阅数量。\n"
            " */\n"
            "public final class CompletableMerge extends Completable {",
        ),
        (
            "    public CompletableMerge(Publisher<? extends CompletableSource> source, int maxConcurrency, boolean delayErrors) {",
            "    /**\n"
            "     * @param source 发出 CompletableSource 的 Publisher\n"
            "     * @param maxConcurrency 最大并行内部源数量\n"
            "     * @param delayErrors 为 true 时收集所有错误后再终止\n"
            "     */\n"
            "    public CompletableMerge(Publisher<? extends CompletableSource> source, int maxConcurrency, boolean delayErrors) {",
        ),
        (
            "    @Override\n    public void subscribeActual(CompletableObserver observer) {",
            "    /** 订阅 source Publisher 并合并各 CompletableSource。 */\n"
            "    @Override\n    public void subscribeActual(CompletableObserver observer) {",
        ),
        (
            "    static final class CompletableMergeSubscriber",
            "    /** 从 Publisher 取源并管理并行内部订阅的内部 subscriber。 */\n"
            "    static final class CompletableMergeSubscriber",
        ),
        (
            "        @Override\n        public void onNext(CompletableSource t) {",
            "        /** 订阅新到达的 CompletableSource 并加入合并集合。 */\n"
            "        @Override\n        public void onNext(CompletableSource t) {",
        ),
        (
            "        void innerError(MergeInnerObserver inner, Throwable t) {",
            "        /** 处理内部源错误；按 delayErrors 策略终止或继续。 */\n"
            "        void innerError(MergeInnerObserver inner, Throwable t) {",
        ),
        (
            "        void innerComplete(MergeInnerObserver inner) {",
            "        /** 内部源完成；全部完成后通知下游。 */\n"
            "        void innerComplete(MergeInnerObserver inner) {",
        ),
        (
            "        final class MergeInnerObserver",
            "        /** 表示单个被合并 CompletableSource 的内部 observer。 */\n"
            "        final class MergeInnerObserver",
        ),
    ],
    "CompletableMergeArray.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class CompletableMergeArray extends Completable {",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 并行合并 {@link CompletableSource} 数组中的所有源；\n"
            " * 全部正常完成后才通知下游完成，首个错误立即终止其余源。\n"
            " */\n"
            "public final class CompletableMergeArray extends Completable {",
        ),
        (
            "    public CompletableMergeArray(CompletableSource[] sources) {",
            "    /** @param sources 要并行合并的 CompletableSource 数组 */\n"
            "    public CompletableMergeArray(CompletableSource[] sources) {",
        ),
        (
            "    @Override\n    public void subscribeActual(final CompletableObserver observer) {",
            "    /** 订阅数组中所有 CompletableSource 并共享完成计数。 */\n"
            "    @Override\n    public void subscribeActual(final CompletableObserver observer) {",
        ),
        (
            "    static final class InnerCompletableObserver extends AtomicInteger implements CompletableObserver, Disposable {",
            "    /** 共享完成计数并在全部完成或首个错误时通知下游。 */\n"
            "    static final class InnerCompletableObserver extends AtomicInteger implements CompletableObserver, Disposable {",
        ),
        (
            "        @Override\n        public void onError(Throwable e) {",
            "        /** 取消所有内部源并转发首个错误。 */\n"
            "        @Override\n        public void onError(Throwable e) {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** 递减计数；全部完成后通知下游完成。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
    ],
    "CompletableMergeArrayDelayError.java": [
        (
            "import io.reactivex.rxjava4.internal.util.AtomicThrowable;\n\npublic final class CompletableMergeArrayDelayError extends Completable {",
            "import io.reactivex.rxjava4.internal.util.AtomicThrowable;\n\n"
            "/**\n"
            " * 并行合并 {@link CompletableSource} 数组并延迟错误：\n"
            " * 收集所有错误，全部源终止后一次性报告。\n"
            " */\n"
            "public final class CompletableMergeArrayDelayError extends Completable {",
        ),
        (
            "    public CompletableMergeArrayDelayError(CompletableSource[] sources) {",
            "    /** @param sources 要并行合并的 CompletableSource 数组 */\n"
            "    public CompletableMergeArrayDelayError(CompletableSource[] sources) {",
        ),
        (
            "    @Override\n    public void subscribeActual(final CompletableObserver observer) {",
            "    /** 订阅数组中所有源并延迟聚合错误。 */\n"
            "    @Override\n    public void subscribeActual(final CompletableObserver observer) {",
        ),
        (
            "    record TryTerminateAndReportDisposable(AtomicThrowable errors) implements Disposable {",
            "    /** dispose 时尝试终止并上报未交付的错误。 */\n"
            "    record TryTerminateAndReportDisposable(AtomicThrowable errors) implements Disposable {",
        ),
        (
            "    record MergeInnerCompletableObserver(CompletableObserver downstream, CompositeDisposable set,",
            "    /** 单个内部源的 observer；完成或错误时递减 wip 并尝试终止。 */\n"
            "    record MergeInnerCompletableObserver(CompletableObserver downstream, CompositeDisposable set,",
        ),
        (
            "        void tryTerminate() {",
            "        /** wip 归零时向 downstream 发出聚合的终止事件。 */\n"
            "        void tryTerminate() {",
        ),
    ],
    "CompletableMergeDelayErrorIterable.java": [
        (
            "import io.reactivex.rxjava4.internal.util.AtomicThrowable;\n\npublic final class CompletableMergeDelayErrorIterable extends Completable {",
            "import io.reactivex.rxjava4.internal.util.AtomicThrowable;\n\n"
            "/**\n"
            " * 并行合并 {@link Iterable} 中的 {@link CompletableSource} 并延迟错误：\n"
            " * 收集所有错误，全部源终止后一次性报告。\n"
            " */\n"
            "public final class CompletableMergeDelayErrorIterable extends Completable {",
        ),
        (
            "    public CompletableMergeDelayErrorIterable(Iterable<? extends CompletableSource> sources) {",
            "    /** @param sources 要并行合并的 CompletableSource 可迭代对象 */\n"
            "    public CompletableMergeDelayErrorIterable(Iterable<? extends CompletableSource> sources) {",
        ),
        (
            "    @Override\n    public void subscribeActual(final CompletableObserver observer) {",
            "    /** 迭代 sources 并并行订阅各 CompletableSource，延迟聚合错误。 */\n"
            "    @Override\n    public void subscribeActual(final CompletableObserver observer) {",
        ),
    ],
    "CompletableMergeIterable.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class CompletableMergeIterable extends Completable {",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 并行合并 {@link Iterable} 中的所有 {@link CompletableSource}；\n"
            " * 全部正常完成后才通知下游完成，首个错误立即终止其余源。\n"
            " */\n"
            "public final class CompletableMergeIterable extends Completable {",
        ),
        (
            "    public CompletableMergeIterable(Iterable<? extends CompletableSource> sources) {",
            "    /** @param sources 要并行合并的 CompletableSource 可迭代对象 */\n"
            "    public CompletableMergeIterable(Iterable<? extends CompletableSource> sources) {",
        ),
        (
            "    @Override\n    public void subscribeActual(final CompletableObserver observer) {",
            "    /** 迭代 sources 并并行订阅各 CompletableSource。 */\n"
            "    @Override\n    public void subscribeActual(final CompletableObserver observer) {",
        ),
        (
            "    static final class MergeCompletableObserver extends AtomicBoolean implements CompletableObserver, Disposable {",
            "    /** 共享完成计数并在全部完成或首个错误时通知下游。 */\n"
            "    static final class MergeCompletableObserver extends AtomicBoolean implements CompletableObserver, Disposable {",
        ),
        (
            "        @Override\n        public void onError(Throwable e) {",
            "        /** 取消所有内部源并转发首个错误。 */\n"
            "        @Override\n        public void onError(Throwable e) {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** 递减 wip；全部完成后通知下游完成。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
    ],
    "CompletableNever.java": [
        (
            "import io.reactivex.rxjava4.internal.disposables.EmptyDisposable;\n\npublic final class CompletableNever extends Completable {",
            "import io.reactivex.rxjava4.internal.disposables.EmptyDisposable;\n\n"
            "/**\n"
            " * 永不终止的 {@link Completable} 单例；\n"
            " * 订阅者仅收到 onSubscribe 且 disposable 不可取消。\n"
            " */\n"
            "public final class CompletableNever extends Completable {",
        ),
        (
            "    @Override\n    protected void subscribeActual(CompletableObserver observer) {",
            "    /** 向下游发出 EmptyDisposable.NEVER，永不完成也不报错。 */\n"
            "    @Override\n    protected void subscribeActual(CompletableObserver observer) {",
        ),
    ],
    "CompletableObserveOn.java": [
        (
            "import io.reactivex.rxjava4.internal.disposables.DisposableHelper;\n\npublic final class CompletableObserveOn extends Completable {",
            "import io.reactivex.rxjava4.internal.disposables.DisposableHelper;\n\n"
            "/**\n"
            " * 在指定 {@link Scheduler} 上调度上游 {@link CompletableSource}\n"
            " * 终止事件（完成或错误）后再通知下游。\n"
            " */\n"
            "public final class CompletableObserveOn extends Completable {",
        ),
        (
            "    public CompletableObserveOn(CompletableSource source, Scheduler scheduler) {",
            "    /**\n"
            "     * @param source 上游 CompletableSource\n"
            "     * @param scheduler 执行下游通知的 Scheduler\n"
            "     */\n"
            "    public CompletableObserveOn(CompletableSource source, Scheduler scheduler) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(final CompletableObserver observer) {",
            "    /** 订阅 source 并在 scheduler 上转发终止事件。 */\n"
            "    @Override\n    protected void subscribeActual(final CompletableObserver observer) {",
        ),
        (
            "    static final class ObserveOnCompletableObserver",
            "    /** 缓存终止事件并在 scheduler 上异步通知 downstream 的内部 observer。 */\n"
            "    static final class ObserveOnCompletableObserver",
        ),
        (
            "        @Override\n        public void run() {",
            "        /** 在 scheduler 线程上向 downstream 发出缓存的错误或完成。 */\n"
            "        @Override\n        public void run() {",
        ),
    ],
    "CompletableOnErrorComplete.java": [
        (
            "import io.reactivex.rxjava4.functions.Predicate;\n\npublic final class CompletableOnErrorComplete extends Completable {",
            "import io.reactivex.rxjava4.functions.Predicate;\n\n"
            "/**\n"
            " * 订阅上游 {@link CompletableSource}；当错误满足 {@link Predicate} 时\n"
            " * 转为正常完成而非转发错误。\n"
            " */\n"
            "public final class CompletableOnErrorComplete extends Completable {",
        ),
        (
            "    public CompletableOnErrorComplete(CompletableSource source, Predicate<? super Throwable> predicate) {",
            "    /**\n"
            "     * @param source 上游 CompletableSource\n"
            "     * @param predicate 返回 true 时将错误转为完成\n"
            "     */\n"
            "    public CompletableOnErrorComplete(CompletableSource source, Predicate<? super Throwable> predicate) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(final CompletableObserver observer) {",
            "    /** 订阅 source 并按 predicate 过滤错误。 */\n"
            "    @Override\n    protected void subscribeActual(final CompletableObserver observer) {",
        ),
        (
            "    static final class OnError implements CompletableObserver {",
            "    /** 根据 predicate 决定转发错误或转为完成的内部 observer。 */\n"
            "    static final class OnError implements CompletableObserver {",
        ),
        (
            "        @Override\n        public void onError(Throwable e) {",
            "        /** predicate 为 true 时通知完成，否则转发错误。 */\n"
            "        @Override\n        public void onError(Throwable e) {",
        ),
    ],
    "CompletableOnErrorReturn.java": [
        (
            "/**\n * Returns a value generated via a function if the main source signals an onError.\n * @param <T> the value type\n * @since 3.0.0\n */",
            "/**\n * 当主源发出 onError 时，通过函数生成并返回一个值。\n * @param <T> 值类型\n * @since 3.0.0\n */",
        ),
        (
            "    public CompletableOnErrorReturn(CompletableSource source,\n            Function<? super Throwable, ? extends T> valueSupplier) {",
            "    /**\n"
            "     * @param source 上游 CompletableSource\n"
            "     * @param valueSupplier 错误发生时生成替代值的函数\n"
            "     */\n"
            "    public CompletableOnErrorReturn(CompletableSource source,\n            Function<? super Throwable, ? extends T> valueSupplier) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
            "    /** 订阅 source；错误时调用 valueSupplier 并发出 onSuccess。 */\n"
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
        ),
        (
            "    static final class OnErrorReturnMaybeObserver<T> implements CompletableObserver, Disposable {",
            "    /** 错误时将 supplier 返回值作为 Maybe 成功值转发的内部 observer。 */\n"
            "    static final class OnErrorReturnMaybeObserver<T> implements CompletableObserver, Disposable {",
        ),
        (
            "        @Override\n        public void onError(Throwable e) {",
            "        /** 调用 itemSupplier 并将结果作为 onSuccess 转发。 */\n"
            "        @Override\n        public void onError(Throwable e) {",
        ),
    ],
    "CompletablePeek.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class CompletablePeek extends Completable {",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 在 {@link CompletableSource} 生命周期各阶段调用副作用回调\n"
            " * （onSubscribe、onError、onComplete、onTerminate 等）。\n"
            " */\n"
            "public final class CompletablePeek extends Completable {",
        ),
        (
            "    public CompletablePeek(CompletableSource source, Consumer<? super Disposable> onSubscribe,\n                           Consumer<? super Throwable> onError,\n                           Action onComplete,\n                           Action onTerminate,\n                           Action onAfterTerminate,\n                           Action onDispose) {",
            "    /**\n"
            "     * @param source 上游 CompletableSource\n"
            "     * @param onSubscribe 订阅时回调\n"
            "     * @param onError 错误时回调\n"
            "     * @param onComplete 完成时回调\n"
            "     * @param onTerminate 终止（完成或错误）时回调\n"
            "     * @param onAfterTerminate 终止事件转发给下游后回调\n"
            "     * @param onDispose dispose 时回调\n"
            "     */\n"
            "    public CompletablePeek(CompletableSource source, Consumer<? super Disposable> onSubscribe,\n                           Consumer<? super Throwable> onError,\n                           Action onComplete,\n                           Action onTerminate,\n                           Action onAfterTerminate,\n                           Action onDispose) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(final CompletableObserver observer) {",
            "    /** 订阅 source 并在各生命周期点触发回调。 */\n"
            "    @Override\n    protected void subscribeActual(final CompletableObserver observer) {",
        ),
        (
            "    final class CompletableObserverImplementation implements CompletableObserver, Disposable {",
            "    /** 包装 downstream 并在各事件点调用 peek 回调的内部 observer。 */\n"
            "    final class CompletableObserverImplementation implements CompletableObserver, Disposable {",
        ),
        (
            "        void doAfter() {",
            "        /** 终止后调用 onAfterTerminate 回调。 */\n"
            "        void doAfter() {",
        ),
    ],
    "CompletableResumeNext.java": [
        (
            "import io.reactivex.rxjava4.internal.disposables.DisposableHelper;\n\npublic final class CompletableResumeNext extends Completable {",
            "import io.reactivex.rxjava4.internal.disposables.DisposableHelper;\n\n"
            "/**\n"
            " * 上游 {@link CompletableSource} 出错时，通过 {@link Function} 映射为\n"
            " * 备用 CompletableSource 并继续订阅。\n"
            " */\n"
            "public final class CompletableResumeNext extends Completable {",
        ),
        (
            "    public CompletableResumeNext(CompletableSource source,\n            Function<? super Throwable, ? extends CompletableSource> errorMapper) {",
            "    /**\n"
            "     * @param source 上游 CompletableSource\n"
            "     * @param errorMapper 将错误映射为备用 CompletableSource 的函数\n"
            "     */\n"
            "    public CompletableResumeNext(CompletableSource source,\n            Function<? super Throwable, ? extends CompletableSource> errorMapper) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(final CompletableObserver observer) {",
            "    /** 订阅 source；错误时通过 errorMapper 恢复并订阅备用源。 */\n"
            "    @Override\n    protected void subscribeActual(final CompletableObserver observer) {",
        ),
        (
            "    static final class ResumeNextObserver",
            "    /** 出错时映射并订阅备用 CompletableSource 的内部 observer。 */\n"
            "    static final class ResumeNextObserver",
        ),
        (
            "        @Override\n        public void onError(Throwable e) {",
            "        /** 首次错误时调用 errorMapper 并订阅返回的 CompletableSource。 */\n"
            "        @Override\n        public void onError(Throwable e) {",
        ),
    ],
    "CompletableSubscribeOn.java": [
        (
            "import io.reactivex.rxjava4.internal.disposables.*;\n\npublic final class CompletableSubscribeOn extends Completable {",
            "import io.reactivex.rxjava4.internal.disposables.*;\n\n"
            "/**\n"
            " * 在指定 {@link Scheduler} 上异步订阅上游 {@link CompletableSource}。\n"
            " */\n"
            "public final class CompletableSubscribeOn extends Completable {",
        ),
        (
            "    public CompletableSubscribeOn(CompletableSource source, Scheduler scheduler) {",
            "    /**\n"
            "     * @param source 上游 CompletableSource\n"
            "     * @param scheduler 执行订阅的 Scheduler\n"
            "     */\n"
            "    public CompletableSubscribeOn(CompletableSource source, Scheduler scheduler) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(final CompletableObserver observer) {",
            "    /** 在 scheduler 上调度对 source 的订阅。 */\n"
            "    @Override\n    protected void subscribeActual(final CompletableObserver observer) {",
        ),
        (
            "    static final class SubscribeOnObserver",
            "    /** 在 scheduler 线程上订阅 source 并转发事件的内部 observer。 */\n"
            "    static final class SubscribeOnObserver",
        ),
        (
            "        @Override\n        public void run() {",
            "        /** 在 scheduler 线程上订阅 source。 */\n"
            "        @Override\n        public void run() {",
        ),
    ],
    "CompletableTakeUntilCompletable.java": [
        (
            "/**\n * Terminates the sequence if either the main or the other Completable terminate.\n * <p>History: 2.1.17 - experimental\n * @since 2.2\n */",
            "/**\n * 当主源或其他 {@link CompletableSource} 任一终止时结束序列。\n * <p>History: 2.1.17 - experimental\n * @since 2.2\n */",
        ),
        (
            "    public CompletableTakeUntilCompletable(Completable source,\n            CompletableSource other) {",
            "    /**\n"
            "     * @param source 主 Completable 源\n"
            "     * @param other 触发提前终止的其他 CompletableSource\n"
            "     */\n"
            "    public CompletableTakeUntilCompletable(Completable source,\n            CompletableSource other) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(CompletableObserver observer) {",
            "    /** 同时订阅 source 与 other；任一终止即结束并取消另一方。 */\n"
            "    @Override\n    protected void subscribeActual(CompletableObserver observer) {",
        ),
        (
            "    static final class TakeUntilMainObserver extends AtomicReference<Disposable>",
            "    /** 监听主源终止并在 other 终止时提前完成的内部 observer。 */\n"
            "    static final class TakeUntilMainObserver extends AtomicReference<Disposable>",
        ),
        (
            "        void innerComplete() {",
            "        /** other 完成时取消主源并通知 downstream 完成。 */\n"
            "        void innerComplete() {",
        ),
        (
            "        static final class OtherObserver extends AtomicReference<Disposable>",
            "        /** 监听 other CompletableSource 终止事件的内部 observer。 */\n"
            "        static final class OtherObserver extends AtomicReference<Disposable>",
        ),
    ],
    "CompletableTimeout.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class CompletableTimeout extends Completable {",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 若上游 {@link CompletableSource} 在指定时长内未终止，\n"
            " * 则触发超时；可选订阅备用 CompletableSource 或发出 TimeoutException。\n"
            " */\n"
            "public final class CompletableTimeout extends Completable {",
        ),
        (
            "    public CompletableTimeout(CompletableSource source, long timeout,\n                              TimeUnit unit, Scheduler scheduler, CompletableSource other) {",
            "    /**\n"
            "     * @param source 上游 CompletableSource\n"
            "     * @param timeout 超时时长\n"
            "     * @param unit 时间单位\n"
            "     * @param scheduler 执行超时计时的 Scheduler\n"
            "     * @param other 超时时的备用 CompletableSource，可为 null\n"
            "     */\n"
            "    public CompletableTimeout(CompletableSource source, long timeout,\n                              TimeUnit unit, Scheduler scheduler, CompletableSource other) {",
        ),
        (
            "    @Override\n    public void subscribeActual(final CompletableObserver observer) {",
            "    /** 订阅 source 并启动超时计时器。 */\n"
            "    @Override\n    public void subscribeActual(final CompletableObserver observer) {",
        ),
        (
            "    static final class TimeOutObserver implements CompletableObserver {",
            "    /** 监听 source 终止并在超时前完成或报错时取消计时器。 */\n"
            "    static final class TimeOutObserver implements CompletableObserver {",
        ),
        (
            "    final class DisposeTask implements Runnable {",
            "    /** 超时到期时取消 source 并触发备用源或 TimeoutException。 */\n"
            "    final class DisposeTask implements Runnable {",
        ),
        (
            "        final class DisposeObserver implements CompletableObserver {",
            "        /** 超时后订阅备用 CompletableSource 的内部 observer。 */\n"
            "        final class DisposeObserver implements CompletableObserver {",
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
            "wave8a Completable* [0:15]",
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
