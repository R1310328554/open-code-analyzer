#!/usr/bin/env python3
"""Chinese-annotate RxJava 4.0.0-alpha-21 wave-10b Flowable operators [15:30]."""
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
WAVE10B_FILE = Path("/tmp/rxjava_w10b.txt")
BATCH_FILES = [
    ln.strip()
    for ln in WAVE10B_FILE.read_text(encoding="utf-8").splitlines()
    if ln.strip()
]

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "FlowableFromPublisher.java": [
        (
            "import io.reactivex.rxjava4.core.Flowable;\n\npublic final class FlowableFromPublisher",
            "import io.reactivex.rxjava4.core.Flowable;\n\n"
            "/**\n"
            " * 将 {@link Publisher} 包装为 {@link Flowable}，订阅时直接委托上游 Publisher。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class FlowableFromPublisher",
        ),
        (
            "    public FlowableFromPublisher(Publisher<? extends T> publisher) {",
            "    /** @param publisher 上游 Publisher */\n"
            "    public FlowableFromPublisher(Publisher<? extends T> publisher) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> s) {",
            "    /** 将下游 Subscriber 直接订阅到 wrapped Publisher。 */\n"
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> s) {",
        ),
    ],
    "FlowableFromRunnable.java": [
        (
            "/**\n * Executes an {@link Runnable} and signals its exception or completes normally.\n *\n * @param <T> the value type\n * @since 3.0.0\n */",
            "/**\n * 执行 {@link Runnable}，异常时 onError，正常完成时 onComplete。\n *\n * @param <T> 元素类型\n * @since 3.0.0\n */",
        ),
        (
            "    public FlowableFromRunnable(Runnable run) {",
            "    /** @param run 订阅时执行的 Runnable */\n"
            "    public FlowableFromRunnable(Runnable run) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> subscriber) {",
            "    /** 创建可取消 Subscription，执行 run 并通知完成或错误。 */\n"
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> subscriber) {",
        ),
        (
            "        return null; // considered as onComplete()",
            "        return null; // 视为 onComplete()",
        ),
        (
            "    @Override\n    public T get() throws Throwable {",
            "    /** 执行 run 并返回 null（表示正常完成）。 */\n"
            "    @Override\n    public T get() throws Throwable {",
        ),
    ],
    "FlowableFromSupplier.java": [
        (
            "/**\n * Call a Supplier for each incoming Subscriber and signal the returned value or the thrown exception.\n * @param <T> the value type and element type returned by the supplier and the flow\n * @since 3.0.0\n */",
            "/**\n * 每次订阅时调用 {@link Supplier}，发射返回值或传播抛出的异常。\n"
            " * @param <T> Supplier 返回值及流元素类型\n"
            " * @since 3.0.0\n */",
        ),
        (
            "    public FlowableFromSupplier(Supplier<? extends T> supplier) {",
            "    /** @param supplier 每次订阅时调用的 Supplier */\n"
            "    public FlowableFromSupplier(Supplier<? extends T> supplier) {",
        ),
        (
            "    @Override\n    public void subscribeActual(Subscriber<? super T> s) {",
            "    /** 调用 supplier 并通过 DeferredScalarSubscription 发射单值。 */\n"
            "    @Override\n    public void subscribeActual(Subscriber<? super T> s) {",
        ),
        (
            "    @Override\n    public T get() throws Throwable {",
            "    /** 直接调用 supplier 并校验非 null。 */\n"
            "    @Override\n    public T get() throws Throwable {",
        ),
    ],
    "FlowableGenerate.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class FlowableGenerate",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 基于可变状态与 {@link BiFunction} 生成器按需生成元素，支持背压。\n"
            " * @param <T> 生成元素类型\n"
            " * @param <S> 状态类型\n"
            " */\n"
            "public final class FlowableGenerate",
        ),
        (
            "    public FlowableGenerate(Supplier<S> stateSupplier, BiFunction<S, Emitter<T>, S> generator,\n            Consumer<? super S> disposeState) {",
            "    /**\n"
            "     * @param stateSupplier 初始状态 Supplier\n"
            "     * @param generator 接收状态与 {@link Emitter} 并返回下一状态的生成函数\n"
            "     * @param disposeState 取消或终止时清理状态的回调\n"
            "     */\n"
            "    public FlowableGenerate(Supplier<S> stateSupplier, BiFunction<S, Emitter<T>, S> generator,\n            Consumer<? super S> disposeState) {",
        ),
        (
            "    @Override\n    public void subscribeActual(Subscriber<? super T> s) {",
            "    /** 获取初始状态并安装 GeneratorSubscription。 */\n"
            "    @Override\n    public void subscribeActual(Subscriber<? super T> s) {",
        ),
        (
            "    static final class GeneratorSubscription<T, S>\n    extends AtomicLong\n    implements Emitter<T>, Subscription {",
            "    /** 背压感知的生成器 Subscription，实现 {@link Emitter} 回调接口。 */\n"
            "    static final class GeneratorSubscription<T, S>\n    extends AtomicLong\n    implements Emitter<T>, Subscription {",
        ),
        (
            "        @Override\n        public void request(long n) {",
            "        /** 按请求量循环调用 generator 直至满足背压或终止。 */\n"
            "        @Override\n        public void request(long n) {",
        ),
        (
            "        private void dispose(S s) {",
            "        /** 调用 disposeState 清理状态，异常路由至 RxJavaPlugins。 */\n"
            "        private void dispose(S s) {",
        ),
        (
            "                // if there are no running requests, just dispose the state",
            "                // 若无进行中的 request，直接清理状态",
        ),
        (
            "        @Override\n        public void cancel() {",
            "        /** 标记取消；无活跃 request 时立即 dispose 状态。 */\n"
            "        @Override\n        public void cancel() {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** 每轮 generator 至多一次 onNext；null 或重复调用报错。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
        (
            "        @Override\n        public void onError(Throwable t) {",
            "        /** 终止序列并向下游传播错误。 */\n"
            "        @Override\n        public void onError(Throwable t) {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** 正常终止并通知下游 onComplete。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
    ],
    "FlowableHide.java": [
        (
            "/**\n * Hides the identity of the wrapped Flowable and its Subscription.\n * @param <T> the value type\n *\n * @since 2.0\n */",
            "/**\n * 隐藏被包装 {@link Flowable} 及其 {@link Subscription} 的身份，防止下游识别上游类型。\n"
            " * @param <T> 元素类型\n *\n * @since 2.0\n */",
        ),
        (
            "    public FlowableHide(Flowable<T> source) {",
            "    /** @param source 上游 Flowable */\n"
            "    public FlowableHide(Flowable<T> source) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> s) {",
            "    /** 以 HideSubscriber 中继事件，向下游暴露自身 Subscription。 */\n"
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> s) {",
        ),
        (
            "    static final class HideSubscriber<T> implements FlowableSubscriber<T>, Subscription {",
            "    /** 中继 request/cancel 与 onXxx 事件的中继 Subscriber。 */\n"
            "    static final class HideSubscriber<T> implements FlowableSubscriber<T>, Subscription {",
        ),
    ],
    "FlowableIgnoreElements.java": [
        (
            "import io.reactivex.rxjava4.operators.QueueSubscription;\n\npublic final class FlowableIgnoreElements",
            "import io.reactivex.rxjava4.operators.QueueSubscription;\n\n"
            "/**\n"
            " * 消费上游所有元素但不向下游发射，仅传递 onError/onComplete。\n"
            " * @param <T> 上游元素类型\n"
            " */\n"
            "public final class FlowableIgnoreElements",
        ),
        (
            "    public FlowableIgnoreElements(Flowable<T> source) {",
            "    /** @param source 上游 Flowable */\n"
            "    public FlowableIgnoreElements(Flowable<T> source) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(final Subscriber<? super T> t) {",
            "    /** 订阅 IgnoreElementsSubscriber 并请求 Long.MAX_VALUE。 */\n"
            "    @Override\n    protected void subscribeActual(final Subscriber<? super T> t) {",
        ),
        (
            "    static final class IgnoreElementsSubscriber<T> implements FlowableSubscriber<T>, QueueSubscription<T> {",
            "    /** 丢弃 onNext 的空 QueueSubscription，支持异步融合。 */\n"
            "    static final class IgnoreElementsSubscriber<T> implements FlowableSubscriber<T>, QueueSubscription<T> {",
        ),
        (
            "            // deliberately ignored",
            "            // 有意忽略元素",
        ),
        (
            "            return null; // empty, always",
            "            return null; // 始终为空",
        ),
        (
            "            // always empty",
            "            // 始终为空",
        ),
        (
            "            // never emits a value",
            "            // 从不发射元素",
        ),
    ],
    "FlowableIgnoreElementsCompletable.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class FlowableIgnoreElementsCompletable",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 忽略 {@link Flowable} 全部元素，仅以 {@link Completable} 形式传递终止信号。\n"
            " * @param <T> 上游元素类型\n"
            " */\n"
            "public final class FlowableIgnoreElementsCompletable",
        ),
        (
            "    public FlowableIgnoreElementsCompletable(Flowable<T> source) {",
            "    /** @param source 上游 Flowable */\n"
            "    public FlowableIgnoreElementsCompletable(Flowable<T> source) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(final CompletableObserver t) {",
            "    /** 订阅内部 IgnoreElementsSubscriber 并消费全部元素。 */\n"
            "    @Override\n    protected void subscribeActual(final CompletableObserver t) {",
        ),
        (
            "    @Override\n    public Flowable<T> fuseToFlowable() {",
            "    /** 融合回 {@link FlowableIgnoreElements} 算子链。 */\n"
            "    @Override\n    public Flowable<T> fuseToFlowable() {",
        ),
        (
            "    static final class IgnoreElementsSubscriber<T> implements FlowableSubscriber<T>, Disposable {",
            "    /** 丢弃元素的 Completable 内部 subscriber。 */\n"
            "    static final class IgnoreElementsSubscriber<T> implements FlowableSubscriber<T>, Disposable {",
        ),
        (
            "            // deliberately ignored",
            "            // 有意忽略元素",
        ),
    ],
    "FlowableInternalHelper.java": [
        (
            "/**\n * Helper utility class to support Flowable with inner classes.\n */",
            "/**\n * 辅助 {@link Flowable} 内部类与算子链的工具类。\n */",
        ),
        (
            "    /** Utility class. */",
            "    /** 工具类，禁止实例化。 */",
        ),
        (
            "    record SimpleGenerator<T, S>(Consumer<Emitter<T>> consumer) implements BiFunction<S, Emitter<T>, S> {",
            "    /** 忽略状态、仅调用 consumer 的简单生成器 record。 */\n"
            "    record SimpleGenerator<T, S>(Consumer<Emitter<T>> consumer) implements BiFunction<S, Emitter<T>, S> {",
        ),
        (
            "    public static <T, S> BiFunction<S, Emitter<T>, S> simpleGenerator(Consumer<Emitter<T>> consumer) {",
            "    /** 包装仅使用 {@link Emitter} 的 {@link Consumer} 为生成器 BiFunction。 */\n"
            "    public static <T, S> BiFunction<S, Emitter<T>, S> simpleGenerator(Consumer<Emitter<T>> consumer) {",
        ),
        (
            "    record SimpleBiGenerator<T, S>(BiConsumer<S, Emitter<T>> consumer) implements BiFunction<S, Emitter<T>, S> {",
            "    /** 接收状态与 Emitter 的简单双参数生成器 record。 */\n"
            "    record SimpleBiGenerator<T, S>(BiConsumer<S, Emitter<T>> consumer) implements BiFunction<S, Emitter<T>, S> {",
        ),
        (
            "    public static <T, S> BiFunction<S, Emitter<T>, S> simpleBiGenerator(BiConsumer<S, Emitter<T>> consumer) {",
            "    /** 包装 {@link BiConsumer} 为生成器 BiFunction。 */\n"
            "    public static <T, S> BiFunction<S, Emitter<T>, S> simpleBiGenerator(BiConsumer<S, Emitter<T>> consumer) {",
        ),
        (
            "    record ItemDelayFunction<T, U>(",
            "    /** 为每个元素启动延迟 Publisher 并在其完成后重发原值的 Function。 */\n"
            "    record ItemDelayFunction<T, U>(",
        ),
        (
            "    public static <T, U> Function<T, Publisher<T>> itemDelay(final Function<? super T, ? extends Publisher<U>> itemDelay) {",
            "    /** 创建 per-item 延迟 Function，延迟 Publisher 完成后再发射原元素。 */\n"
            "    public static <T, U> Function<T, Publisher<T>> itemDelay(final Function<? super T, ? extends Publisher<U>> itemDelay) {",
        ),
        (
            "    record SubscriberOnNext<T>(Subscriber<T> subscriber) implements Consumer<T> {",
            "    /** 将 {@link Consumer} 委托为 Subscriber.onNext。 */\n"
            "    record SubscriberOnNext<T>(Subscriber<T> subscriber) implements Consumer<T> {",
        ),
        (
            "    record SubscriberOnError<T>(Subscriber<T> subscriber) implements Consumer<Throwable> {",
            "    /** 将 {@link Consumer} 委托为 Subscriber.onError。 */\n"
            "    record SubscriberOnError<T>(Subscriber<T> subscriber) implements Consumer<Throwable> {",
        ),
        (
            "    record SubscriberOnComplete<T>(Subscriber<T> subscriber) implements Action {",
            "    /** 将 {@link Action} 委托为 Subscriber.onComplete。 */\n"
            "    record SubscriberOnComplete<T>(Subscriber<T> subscriber) implements Action {",
        ),
        (
            "    public static <T> Consumer<T> subscriberOnNext(Subscriber<T> subscriber) {",
            "    /** 返回调用 subscriber.onNext 的 Consumer。 */\n"
            "    public static <T> Consumer<T> subscriberOnNext(Subscriber<T> subscriber) {",
        ),
        (
            "    public static <T> Consumer<Throwable> subscriberOnError(Subscriber<T> subscriber) {",
            "    /** 返回调用 subscriber.onError 的 Consumer。 */\n"
            "    public static <T> Consumer<Throwable> subscriberOnError(Subscriber<T> subscriber) {",
        ),
        (
            "    public static <T> Action subscriberOnComplete(Subscriber<T> subscriber) {",
            "    /** 返回调用 subscriber.onComplete 的 Action。 */\n"
            "    public static <T> Action subscriberOnComplete(Subscriber<T> subscriber) {",
        ),
        (
            "    static final class FlatMapWithCombinerInner<U, R, T> implements Function<U, R> {",
            "    /** flatMapWithCombiner 内部：将 combiner 与外层元素 T 绑定。 */\n"
            "    static final class FlatMapWithCombinerInner<U, R, T> implements Function<U, R> {",
        ),
        (
            "    static final class FlatMapWithCombinerOuter<T, R, U> implements Function<T, Publisher<R>> {",
            "    /** flatMapWithCombiner 外层：mapper 后接 MapPublisher 与 combiner。 */\n"
            "    static final class FlatMapWithCombinerOuter<T, R, U> implements Function<T, Publisher<R>> {",
        ),
        (
            "    public static <T, U, R> Function<T, Publisher<R>> flatMapWithCombiner(",
            "    /** 创建 flatMap + combiner 组合 Function。 */\n"
            "    public static <T, U, R> Function<T, Publisher<R>> flatMapWithCombiner(",
        ),
        (
            "    static final class FlatMapIntoIterable<T, U> implements Function<T, Publisher<U>> {",
            "    /** 将 T 映射为 Iterable 并包装为 FlowableFromIterable 的 Function。 */\n"
            "    static final class FlatMapIntoIterable<T, U> implements Function<T, Publisher<U>> {",
        ),
        (
            "    public static <T, U> Function<T, Publisher<U>> flatMapIntoIterable(final Function<? super T, ? extends Iterable<? extends U>> mapper) {",
            "    /** 创建 flatMapIntoIterable 用的 mapper Function。 */\n"
            "    public static <T, U> Function<T, Publisher<U>> flatMapIntoIterable(final Function<? super T, ? extends Iterable<? extends U>> mapper) {",
        ),
        (
            "    public static <T> Supplier<ConnectableFlowable<T>> replaySupplier(final Flowable<T> parent) {",
            "    /** 无参 replay 的 ConnectableFlowable Supplier。 */\n"
            "    public static <T> Supplier<ConnectableFlowable<T>> replaySupplier(final Flowable<T> parent) {",
        ),
        (
            "    public static <T> Supplier<ConnectableFlowable<T>> replaySupplier(final Flowable<T> parent, final int bufferSize, boolean eagerTruncate) {",
            "    /** 带 bufferSize 的 replay Supplier。 */\n"
            "    public static <T> Supplier<ConnectableFlowable<T>> replaySupplier(final Flowable<T> parent, final int bufferSize, boolean eagerTruncate) {",
        ),
        (
            "    public static <T> Supplier<ConnectableFlowable<T>> replaySupplier(final Flowable<T> parent,\n            final int bufferSize, final long time, final TimeUnit unit, final Scheduler scheduler, boolean eagerTruncate) {",
            "    /** 带 bufferSize 与时间窗口的 replay Supplier。 */\n"
            "    public static <T> Supplier<ConnectableFlowable<T>> replaySupplier(final Flowable<T> parent,\n            final int bufferSize, final long time, final TimeUnit unit, final Scheduler scheduler, boolean eagerTruncate) {",
        ),
        (
            "    public static <T> Supplier<ConnectableFlowable<T>> replaySupplier(final Flowable<T> parent,\n            final long time, final TimeUnit unit, final Scheduler scheduler, boolean eagerTruncate) {",
            "    /** 仅按时间窗口 replay 的 Supplier。 */\n"
            "    public static <T> Supplier<ConnectableFlowable<T>> replaySupplier(final Flowable<T> parent,\n            final long time, final TimeUnit unit, final Scheduler scheduler, boolean eagerTruncate) {",
        ),
        (
            "    public enum RequestMax implements Consumer<Subscription> {",
            "    /** 对 Subscription 请求 Long.MAX_VALUE 的单例 Consumer。 */\n"
            "    public enum RequestMax implements Consumer<Subscription> {",
        ),
        (
            "    record ReplaySupplier<T>(Flowable<T> parent) implements Supplier<ConnectableFlowable<T>> {",
            "    /** 调用 parent.replay() 的 Supplier record。 */\n"
            "    record ReplaySupplier<T>(Flowable<T> parent) implements Supplier<ConnectableFlowable<T>> {",
        ),
        (
            "    record BufferedReplaySupplier<T>(Flowable<T> parent, int bufferSize,\n                                     boolean eagerTruncate) implements Supplier<ConnectableFlowable<T>> {",
            "    /** 调用 parent.replay(bufferSize, eagerTruncate) 的 Supplier record。 */\n"
            "    record BufferedReplaySupplier<T>(Flowable<T> parent, int bufferSize,\n                                     boolean eagerTruncate) implements Supplier<ConnectableFlowable<T>> {",
        ),
        (
            "    record BufferedTimedReplay<T>(Flowable<T> parent, int bufferSize, long time, TimeUnit unit, Scheduler scheduler,\n                                  boolean eagerTruncate) implements Supplier<ConnectableFlowable<T>> {",
            "    /** 带缓冲与时间窗口的 replay Supplier record。 */\n"
            "    record BufferedTimedReplay<T>(Flowable<T> parent, int bufferSize, long time, TimeUnit unit, Scheduler scheduler,\n                                  boolean eagerTruncate) implements Supplier<ConnectableFlowable<T>> {",
        ),
        (
            "    static final class TimedReplay<T> implements Supplier<ConnectableFlowable<T>> {",
            "    /** 按时间窗口 replay 的 Supplier 实现。 */\n"
            "    static final class TimedReplay<T> implements Supplier<ConnectableFlowable<T>> {",
        ),
    ],
    "FlowableInterval.java": [
        (
            "import io.reactivex.rxjava4.internal.util.BackpressureHelper;\n\npublic final class FlowableInterval",
            "import io.reactivex.rxjava4.internal.util.BackpressureHelper;\n\n"
            "/**\n"
            " * 在指定 {@link Scheduler} 上按固定周期发射递增 Long 计数（0, 1, 2, …）。\n"
            " */\n"
            "public final class FlowableInterval",
        ),
        (
            "    public FlowableInterval(long initialDelay, long period, TimeUnit unit, Scheduler scheduler) {",
            "    /**\n"
            "     * @param initialDelay 首次发射前的延迟\n"
            "     * @param period 后续发射间隔\n"
            "     * @param unit 时间单位\n"
            "     * @param scheduler 执行周期任务的 Scheduler\n"
            "     */\n"
            "    public FlowableInterval(long initialDelay, long period, TimeUnit unit, Scheduler scheduler) {",
        ),
        (
            "    @Override\n    public void subscribeActual(Subscriber<? super Long> s) {",
            "    /** 创建 IntervalSubscriber 并按 Trampoline 与否选择调度方式。 */\n"
            "    @Override\n    public void subscribeActual(Subscriber<? super Long> s) {",
        ),
        (
            "    static final class IntervalSubscriber extends AtomicLong\n    implements Subscription, Runnable {",
            "    /** 背压感知的周期计数 Subscription。 */\n"
            "    static final class IntervalSubscriber extends AtomicLong\n    implements Subscription, Runnable {",
        ),
        (
            "        @Override\n        public void run() {",
            "        /** 有背压许可时发射 count 并递增，否则 MissingBackpressureException。 */\n"
            "        @Override\n        public void run() {",
        ),
    ],
    "FlowableIntervalRange.java": [
        (
            "import io.reactivex.rxjava4.internal.util.BackpressureHelper;\n\npublic final class FlowableIntervalRange",
            "import io.reactivex.rxjava4.internal.util.BackpressureHelper;\n\n"
            "/**\n"
            " * 在指定 Scheduler 上按周期发射 [start, end] 闭区间内的 Long 序列。\n"
            " */\n"
            "public final class FlowableIntervalRange",
        ),
        (
            "    public FlowableIntervalRange(long start, long end, long initialDelay, long period, TimeUnit unit, Scheduler scheduler) {",
            "    /**\n"
            "     * @param start 起始计数值（含）\n"
            "     * @param end 结束计数值（含）\n"
            "     * @param initialDelay 首次发射前延迟\n"
            "     * @param period 发射间隔\n"
            "     * @param unit 时间单位\n"
            "     * @param scheduler 调度器\n"
            "     */\n"
            "    public FlowableIntervalRange(long start, long end, long initialDelay, long period, TimeUnit unit, Scheduler scheduler) {",
        ),
        (
            "    @Override\n    public void subscribeActual(Subscriber<? super Long> s) {",
            "    /** 创建 IntervalRangeSubscriber 并启动周期调度。 */\n"
            "    @Override\n    public void subscribeActual(Subscriber<? super Long> s) {",
        ),
        (
            "    static final class IntervalRangeSubscriber extends AtomicLong\n    implements Subscription, Runnable {",
            "    /** 有限范围周期计数的背压感知 Subscription。 */\n"
            "    static final class IntervalRangeSubscriber extends AtomicLong\n    implements Subscription, Runnable {",
        ),
        (
            "        @Override\n        public void run() {",
            "        /** 发射当前 count，到达 end 时 onComplete 并 dispose。 */\n"
            "        @Override\n        public void run() {",
        ),
    ],
    "FlowableJust.java": [
        (
            "/**\n * Represents a constant scalar value.\n * @param <T> the value type\n */",
            "/**\n * 表示恒定标量值的 {@link Flowable}，订阅时发射单个元素。\n"
            " * @param <T> 元素类型\n */",
        ),
        (
            "    public FlowableJust(final T value) {",
            "    /** @param value 要发射的恒定值 */\n"
            "    public FlowableJust(final T value) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> s) {",
            "    /** 以 ScalarSubscription 向下游发射单值。 */\n"
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> s) {",
        ),
        (
            "    @Override\n    public T get() {",
            "    /** 返回包装的标量值。 */\n"
            "    @Override\n    public T get() {",
        ),
    ],
    "FlowableLastMaybe.java": [
        (
            "/**\n * Consumes the source Publisher and emits its last item or completes.\n * \n * @param <T> the value type\n */",
            "/**\n * 消费上游 {@link Publisher} 并发射最后一项；若无元素则 onComplete。\n * \n * @param <T> 元素类型\n */",
        ),
        (
            "    public FlowableLastMaybe(Publisher<T> source) {",
            "    /** @param source 上游 Publisher */\n"
            "    public FlowableLastMaybe(Publisher<T> source) {",
        ),
        (
            "    // TODO fuse back to Flowable",
            "    // TODO 融合回 Flowable",
        ),
        (
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
            "    /** 请求全部元素并保留最后一项。 */\n"
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
        ),
        (
            "    static final class LastSubscriber<T> implements FlowableSubscriber<T>, Disposable {",
            "    /** 跟踪最后一项并在 onComplete 时 onSuccess 或 onComplete。 */\n"
            "    static final class LastSubscriber<T> implements FlowableSubscriber<T>, Disposable {",
        ),
    ],
    "FlowableLastSingle.java": [
        (
            "/**\n * Consumes the source Publisher and emits its last item or the defaultItem\n * if empty.\n * \n * @param <T> the value type\n */",
            "/**\n * 消费上游 {@link Publisher} 并发射最后一项；空流时发射 defaultItem 或 NoSuchElementException。\n * \n * @param <T> 元素类型\n */",
        ),
        (
            "    public FlowableLastSingle(Publisher<T> source, T defaultItem) {",
            "    /**\n"
            "     * @param source 上游 Publisher\n"
            "     * @param defaultItem 空流时的默认值（可为 null）\n"
            "     */\n"
            "    public FlowableLastSingle(Publisher<T> source, T defaultItem) {",
        ),
        (
            "    // TODO fuse back to Flowable",
            "    // TODO 融合回 Flowable",
        ),
        (
            "    @Override\n    protected void subscribeActual(SingleObserver<? super T> observer) {",
            "    /** 请求全部元素，onComplete 时发射最后一项或默认值。 */\n"
            "    @Override\n    protected void subscribeActual(SingleObserver<? super T> observer) {",
        ),
        (
            "    static final class LastSubscriber<T> implements FlowableSubscriber<T>, Disposable {",
            "    /** 保留最后一项并在终止时通知 SingleObserver。 */\n"
            "    static final class LastSubscriber<T> implements FlowableSubscriber<T>, Disposable {",
        ),
    ],
    "FlowableLift.java": [
        (
            "/**\n * Allows lifting operators into a chain of Publishers.\n *\n * <p>By having a concrete Publisher as lift, operator fusing can now identify\n * both the source and the operation inside it via casting, unlike the lambda version of this.\n *\n * @param <T> the upstream value type\n * @param <R> the downstream parameter type\n */",
            "/**\n * 将自定义算子提升（lift）进 Publisher 链。\n *\n * <p>使用具体 Publisher 作为 lift 载体后，算子融合可通过类型转换识别源与内部操作，\n * 优于基于 lambda 的实现。\n *\n * @param <T> 上游元素类型\n * @param <R> 下游元素类型\n */",
        ),
        (
            "    /** The actual operator. */",
            "    /** 实际 lift 算子。 */",
        ),
        (
            "    public FlowableLift(Flowable<T> source, FlowableOperator<? extends R, ? super T> operator) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param operator 将下游 Subscriber 转换为上游 Subscriber 的算子\n"
            "     */\n"
            "    public FlowableLift(Flowable<T> source, FlowableOperator<? extends R, ? super T> operator) {",
        ),
        (
            "    @Override\n    public void subscribeActual(Subscriber<? super R> s) {",
            "    /** 应用 operator 得到上游 Subscriber 并订阅 source。 */\n"
            "    @Override\n    public void subscribeActual(Subscriber<? super R> s) {",
        ),
        (
            "            // can't call onError because no way to know if a Subscription has been set or not\n            // can't call onSubscribe because the call might have set a Subscription already",
            "            // 无法调用 onError，因不确定 Subscription 是否已设置\n            // 无法调用 onSubscribe，因 apply 可能已设置 Subscription",
        ),
    ],
    "FlowableMap.java": [
        (
            "import java.util.Objects;\n\npublic final class FlowableMap",
            "import java.util.Objects;\n\n"
            "/**\n"
            " * 对上游每个元素应用 {@link Function} 映射为新类型并向下游发射。\n"
            " * @param <T> 上游元素类型\n"
            " * @param <U> 映射后元素类型\n"
            " */\n"
            "public final class FlowableMap",
        ),
        (
            "    public FlowableMap(Flowable<T> source, Function<? super T, ? extends U> mapper) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param mapper 元素映射函数（返回值不可为 null）\n"
            "     */\n"
            "    public FlowableMap(Flowable<T> source, Function<? super T, ? extends U> mapper) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Subscriber<? super U> s) {",
            "    /** 按下游类型选择 MapSubscriber 或 MapConditionalSubscriber。 */\n"
            "    @Override\n    protected void subscribeActual(Subscriber<? super U> s) {",
        ),
        (
            "    static final class MapSubscriber<T, U> extends BasicFuseableSubscriber<T, U> {",
            "    /** 标准 map 融合 subscriber。 */\n"
            "    static final class MapSubscriber<T, U> extends BasicFuseableSubscriber<T, U> {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** 应用 mapper 并向下游 onNext；融合模式下转发 null 占位。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
        (
            "    static final class MapConditionalSubscriber<T, U> extends BasicFuseableConditionalSubscriber<T, U> {",
            "    /** 支持 {@link ConditionalSubscriber} 的 map subscriber。 */\n"
            "    static final class MapConditionalSubscriber<T, U> extends BasicFuseableConditionalSubscriber<T, U> {",
        ),
        (
            "        @Override\n        public boolean tryOnNext(T t) {",
            "        /** 映射后调用 downstream.tryOnNext。 */\n"
            "        @Override\n        public boolean tryOnNext(T t) {",
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
            "wave10b Flowable* [15:30]",
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
        print(f"Marked {ok} files done in queue (note=wave10b)")
    print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False, indent=2))
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
