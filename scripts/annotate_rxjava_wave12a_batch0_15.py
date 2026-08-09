#!/usr/bin/env python3
"""Chinese-annotate RxJava 4.0.0-alpha-21 wave-12a Flowable* [0:15]."""
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
WAVE12A_FILE = Path("/tmp/rxjava_w12a.txt")
BATCH_FILES = [
    ln.strip()
    for ln in WAVE12A_FILE.read_text(encoding="utf-8").splitlines()
    if ln.strip()
]

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "FlowableSamplePublisher.java": [
        (
            "import io.reactivex.rxjava4.subscribers.SerializedSubscriber;\n\npublic final class FlowableSamplePublisher",
            "import io.reactivex.rxjava4.subscribers.SerializedSubscriber;\n\n"
            "/**\n"
            " * 由另一 {@link Publisher} 的 onNext 触发，采样上游最新值并向下游发射。\n"
            " * @param <T> 上游元素类型\n"
            " */\n"
            "public final class FlowableSamplePublisher",
        ),
        (
            "    public FlowableSamplePublisher(Publisher<T> source, Publisher<?> other, boolean emitLast) {",
            "    /**\n"
            "     * @param source 被采样的上游 Publisher\n"
            "     * @param other 采样触发源（其 onNext 触发一次采样）\n"
            "     * @param emitLast 上游完成时是否发射最后一个缓存样本\n"
            "     */\n"
            "    public FlowableSamplePublisher(Publisher<T> source, Publisher<?> other, boolean emitLast) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> s) {",
            "    /** 按 emitLast 选择是否在上游结束时发射最后一个样本值。 */\n"
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> s) {",
        ),
        (
            "    abstract static class SamplePublisherSubscriber<T> extends AtomicReference<T> implements FlowableSubscriber<T>, Subscription {",
            "    /** 缓存上游最新值，由采样源 onNext 触发 emit 的 subscriber 基类。 */\n"
            "    abstract static class SamplePublisherSubscriber<T> extends AtomicReference<T> implements FlowableSubscriber<T>, Subscription {",
        ),
        (
            "        @Override\n        public void onSubscribe(Subscription s) {",
            "        /** 订阅上游并启动采样源；上游以 MAX 请求。 */\n"
            "        @Override\n        public void onSubscribe(Subscription s) {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** 以 lazySet 缓存上游最新样本值。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
        (
            "        void emit() {",
            "        /** 取出并发射缓存样本；无背压则 MissingBackpressureException。 */\n"
            "        void emit() {",
        ),
        (
            "    record SamplerSubscriber<T>(SamplePublisherSubscriber<T> parent) implements FlowableSubscriber<Object> {",
            "    /** 采样触发源 subscriber：onNext 时调用 parent.run()。 */\n"
            "    record SamplerSubscriber<T>(SamplePublisherSubscriber<T> parent) implements FlowableSubscriber<Object> {",
        ),
        (
            "    static final class SampleMainNoLast<T> extends SamplePublisherSubscriber<T> {",
            "    /** emitLast=false：上游完成时不发射最后一个缓存样本。 */\n"
            "    static final class SampleMainNoLast<T> extends SamplePublisherSubscriber<T> {",
        ),
        (
            "    static final class SampleMainEmitLast<T> extends SamplePublisherSubscriber<T> {",
            "    /** emitLast=true：上游完成时发射最后一个缓存样本后再 onComplete。 */\n"
            "    static final class SampleMainEmitLast<T> extends SamplePublisherSubscriber<T> {",
        ),
    ],
    "FlowableSampleTimed.java": [
        (
            "import io.reactivex.rxjava4.subscribers.SerializedSubscriber;\n\npublic final class FlowableSampleTimed",
            "import io.reactivex.rxjava4.subscribers.SerializedSubscriber;\n\n"
            "/**\n"
            " * 按固定时间间隔定时采样上游最新值并向下游发射。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class FlowableSampleTimed",
        ),
        (
            "    public FlowableSampleTimed(Flowable<T> source, long period, TimeUnit unit, Scheduler scheduler, boolean emitLast, Consumer<? super T> onDropped) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param period 采样周期\n"
            "     * @param unit 时间单位\n"
            "     * @param scheduler 调度器\n"
            "     * @param emitLast 上游完成时是否发射最后一个缓存样本\n"
            "     * @param onDropped 被新样本覆盖的旧值回调（可为 null）\n"
            "     */\n"
            "    public FlowableSampleTimed(Flowable<T> source, long period, TimeUnit unit, Scheduler scheduler, boolean emitLast, Consumer<? super T> onDropped) {",
        ),
        (
            "    abstract static class SampleTimedSubscriber<T> extends AtomicReference<T> implements FlowableSubscriber<T>, Subscription, Runnable {",
            "    /** 定时器驱动采样：缓存最新值并按 period 周期 emit。 */\n"
            "    abstract static class SampleTimedSubscriber<T> extends AtomicReference<T> implements FlowableSubscriber<T>, Subscription, Runnable {",
        ),
        (
            "        @Override\n        public void onSubscribe(Subscription s) {",
            "        /** 启动周期定时器；上游以 MAX 请求。 */\n"
            "        @Override\n        public void onSubscribe(Subscription s) {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** 缓存最新值；若有被覆盖的旧值则调用 onDropped。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
        (
            "        void emit() {",
            "        /** 取出并发射缓存样本；无背压则 MissingBackpressureException。 */\n"
            "        void emit() {",
        ),
        (
            "    static final class SampleTimedNoLast<T> extends SampleTimedSubscriber<T> {",
            "    /** emitLast=false：上游完成时不发射最后一个缓存样本。 */\n"
            "    static final class SampleTimedNoLast<T> extends SampleTimedSubscriber<T> {",
        ),
        (
            "    static final class SampleTimedEmitLast<T> extends SampleTimedSubscriber<T> {",
            "    /** emitLast=true：上游完成时发射最后一个缓存样本后再 onComplete。 */\n"
            "    static final class SampleTimedEmitLast<T> extends SampleTimedSubscriber<T> {",
        ),
    ],
    "FlowableScalarXMap.java": [
        (
            "/**\n * Utility classes to work with scalar-sourced XMap operators (where X == { flat, concat, switch }).\n */",
            "/**\n"
            " * 处理标量源 XMap 算子（X 为 flat、concat、switch）的工具类。\n"
            " */",
        ),
        (
            "    /** Utility class. */",
            "    /** 工具类，禁止实例化。 */",
        ),
        (
            "    /**\n"
            "     * Tries to subscribe to a possible Supplier source's mapped Publisher.\n"
            "     * @param <T> the input value type\n"
            "     * @param <R> the output value type\n"
            "     * @param source the source Publisher\n"
            "     * @param subscriber the subscriber\n"
            "     * @param mapper the function mapping a scalar value into a Publisher\n"
            "     * @return true if successful, false if the caller should continue with the regular path.\n"
            "     */",
            "    /**\n"
            "     * 尝试订阅可能为 {@link Supplier} 的源经 mapper 映射后的 {@link Publisher}。\n"
            "     * @param <T> 输入值类型\n"
            "     * @param <R> 输出值类型\n"
            "     * @param source 源 Publisher\n"
            "     * @param subscriber 下游 subscriber\n"
            "     * @param mapper 将标量值映射为 Publisher 的函数\n"
            "     * @return 成功处理标量路径时 true，否则调用方应走常规路径\n"
            "     */",
        ),
        (
            "    /**\n"
            "     * Maps a scalar value into a Publisher and emits its values.\n"
            "     *\n"
            "     * @param <T> the scalar value type\n"
            "     * @param <U> the output value type\n"
            "     * @param value the scalar value to map\n"
            "     * @param mapper the function that gets the scalar value and should return\n"
            "     * a Publisher that gets streamed\n"
            "     * @return the new Flowable instance\n"
            "     */",
            "    /**\n"
            "     * 将标量值映射为 {@link Publisher} 并发射其元素。\n"
            "     *\n"
            "     * @param <T> 标量值类型\n"
            "     * @param <U> 输出元素类型\n"
            "     * @param value 待映射的标量值\n"
            "     * @param mapper 接收标量值并返回待订阅 Publisher 的函数\n"
            "     * @return 新的 {@link Flowable} 实例\n"
            "     */",
        ),
        (
            "    /**\n"
            "     * Maps a scalar value to a Publisher and subscribes to it.\n"
            "     *\n"
            "     * @param <T> the scalar value type\n"
            "     * @param <R> the mapped Publisher's element type.\n"
            "     */",
            "    /**\n"
            "     * 将标量值映射为 {@link Publisher} 并订阅。\n"
            "     *\n"
            "     * @param <T> 标量值类型\n"
            "     * @param <R> 映射后 Publisher 的元素类型\n"
            "     */",
        ),
        (
            "        @SuppressWarnings(\"unchecked\")\n        @Override\n        public void subscribeActual(Subscriber<? super R> s) {",
            "        /** 应用 mapper；若结果为 Supplier 则走标量订阅，否则直接 subscribe。 */\n"
            "        @SuppressWarnings(\"unchecked\")\n        @Override\n        public void subscribeActual(Subscriber<? super R> s) {",
        ),
    ],
    "FlowableScan.java": [
        (
            "import java.util.Objects;\n\npublic final class FlowableScan",
            "import java.util.Objects;\n\n"
            "/**\n"
            " * 累积 scan：首元素直接发射，后续用 {@link BiFunction} 累积并发射。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class FlowableScan",
        ),
        (
            "    public FlowableScan(Flowable<T> source, BiFunction<T, T, T> accumulator) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param accumulator 累积函数（前一累积值, 当前元素）→ 新累积值\n"
            "     */\n"
            "    public FlowableScan(Flowable<T> source, BiFunction<T, T, T> accumulator) {",
        ),
        (
            "    static final class ScanSubscriber<T> implements FlowableSubscriber<T>, Subscription {",
            "    /** 维护累积值 value，首元素直接发射，其后 apply accumulator。 */\n"
            "    static final class ScanSubscriber<T> implements FlowableSubscriber<T>, Subscription {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** value 为 null 时直接发射首元素，否则累积后发射。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
    ],
    "FlowableScanSeed.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class FlowableScanSeed",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 带种子值的 scan：先发射 seed，再按 accumulator 累积并支持背压。\n"
            " * @param <T> 上游元素类型\n"
            " * @param <R> 累积结果类型\n"
            " */\n"
            "public final class FlowableScanSeed",
        ),
        (
            "    public FlowableScanSeed(Flowable<T> source, Supplier<R> seedSupplier, BiFunction<R, ? super T, R> accumulator) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param seedSupplier 提供初始种子值的 Supplier\n"
            "     * @param accumulator 累积函数（累积值, 当前元素）→ 新累积值\n"
            "     */\n"
            "    public FlowableScanSeed(Flowable<T> source, Supplier<R> seedSupplier, BiFunction<R, ? super T, R> accumulator) {",
        ),
        (
            "    static final class ScanSeedSubscriber<T, R>\n    extends AtomicInteger\n    implements FlowableSubscriber<T>, Subscription {",
            "    /** 队列缓冲累积结果，drain 循环处理背压与上游 prefetch。 */\n"
            "    static final class ScanSeedSubscriber<T, R>\n    extends AtomicInteger\n    implements FlowableSubscriber<T>, Subscription {",
        ),
        (
            "        void drain() {",
            "        /** 从队列 poll 累积值并向下游发射，按需向上游 request。 */\n"
            "        void drain() {",
        ),
    ],
    "FlowableSequenceEqual.java": [
        (
            "import io.reactivex.rxjava4.operators.SpscArrayQueue;\n\npublic final class FlowableSequenceEqual",
            "import io.reactivex.rxjava4.operators.SpscArrayQueue;\n\n"
            "/**\n"
            " * 逐元素比较两个 {@link Publisher} 序列是否相等，结果为 {@code Boolean}。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class FlowableSequenceEqual",
        ),
        (
            "    public FlowableSequenceEqual(Publisher<? extends T> first, Publisher<? extends T> second,\n            BiPredicate<? super T, ? super T> comparer, int prefetch) {",
            "    /**\n"
            "     * @param first 第一个序列\n"
            "     * @param second 第二个序列\n"
            "     * @param comparer 逐元素比较谓词\n"
            "     * @param prefetch 预取缓冲大小\n"
            "     */\n"
            "    public FlowableSequenceEqual(Publisher<? extends T> first, Publisher<? extends T> second,\n            BiPredicate<? super T, ? super T> comparer, int prefetch) {",
        ),
        (
            "    /**\n     * Provides callbacks for the EqualSubscribers.\n     */",
            "    /** 为 EqualSubscriber 提供 drain 与错误回调的协调接口。 */",
        ),
        (
            "    static final class EqualCoordinator<T> extends DeferredScalarSubscription<Boolean>\n    implements EqualCoordinatorHelper {",
            "    /** 协调两个 EqualSubscriber，配对 poll 并逐对比较。 */\n"
            "    static final class EqualCoordinator<T> extends DeferredScalarSubscription<Boolean>\n    implements EqualCoordinatorHelper {",
        ),
        (
            "        @Override\n        public void drain() {",
            "        /** 从两队列配对 poll 元素并用 comparer 比较；不等或长度不同则 complete(false)。 */\n"
            "        @Override\n        public void drain() {",
        ),
        (
            "    static final class EqualSubscriber<T>\n    extends AtomicReference<Subscription>\n    implements FlowableSubscriber<T> {",
            "    /** 单侧序列 subscriber：缓冲元素并通知 coordinator drain。 */\n"
            "    static final class EqualSubscriber<T>\n    extends AtomicReference<Subscription>\n    implements FlowableSubscriber<T> {",
        ),
    ],
    "FlowableSequenceEqualSingle.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class FlowableSequenceEqualSingle",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 逐元素比较两个 {@link Publisher} 序列是否相等，结果为 {@link Single}&lt;Boolean&gt;。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class FlowableSequenceEqualSingle",
        ),
        (
            "    @Override\n    public Flowable<Boolean> fuseToFlowable() {",
            "    /** 融合为 {@link FlowableSequenceEqual}。 */\n"
            "    @Override\n    public Flowable<Boolean> fuseToFlowable() {",
        ),
        (
            "    static final class EqualCoordinator<T>\n    extends AtomicInteger\n    implements Disposable, EqualCoordinatorHelper {",
            "    /** Single 版协调器：比较完成后 onSuccess(true/false)。 */\n"
            "    static final class EqualCoordinator<T>\n    extends AtomicInteger\n    implements Disposable, EqualCoordinatorHelper {",
        ),
        (
            "        @Override\n        public void drain() {",
            "        /** 配对 poll 并比较；全部相等 onSuccess(true)，否则 onSuccess(false)。 */\n"
            "        @Override\n        public void drain() {",
        ),
    ],
    "FlowableSerialized.java": [
        (
            "import io.reactivex.rxjava4.subscribers.SerializedSubscriber;\n\npublic final class FlowableSerialized",
            "import io.reactivex.rxjava4.subscribers.SerializedSubscriber;\n\n"
            "/**\n"
            " * 序列化下游事件，保证 onNext/onError/onComplete 不会并发交错。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class FlowableSerialized",
        ),
        (
            "    public FlowableSerialized(Flowable<T> source) {",
            "    /** @param source 上游 Flowable */\n"
            "    public FlowableSerialized(Flowable<T> source) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> s) {",
            "    /** 用 {@link SerializedSubscriber} 包装下游以保证事件串行。 */\n"
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> s) {",
        ),
    ],
    "FlowableSingle.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class FlowableSingle",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 期望上游恰好一个元素；多于一个则 onError，为空则按 defaultValue 或 failOnEmpty 处理。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class FlowableSingle",
        ),
        (
            "    public FlowableSingle(Flowable<T> source, T defaultValue, boolean failOnEmpty) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param defaultValue 无元素时的默认值\n"
            "     * @param failOnEmpty 无元素且无默认值时是否 onError({@link NoSuchElementException})\n"
            "     */\n"
            "    public FlowableSingle(Flowable<T> source, T defaultValue, boolean failOnEmpty) {",
        ),
        (
            "    static final class SingleElementSubscriber<T> extends DeferredScalarSubscription<T>\n    implements FlowableSubscriber<T> {",
            "    /** 收集唯一元素；第二个 onNext 触发 IllegalArgumentException。 */\n"
            "    static final class SingleElementSubscriber<T> extends DeferredScalarSubscription<T>\n    implements FlowableSubscriber<T> {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** 无元素时用 defaultValue；仍为空则 failOnEmpty 决定 onError 或 onComplete。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
    ],
    "FlowableSingleMaybe.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class FlowableSingleMaybe",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 将上游 0 或 1 个元素转为 {@link Maybe}；多于一个则 onError。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class FlowableSingleMaybe",
        ),
        (
            "    @Override\n    public Flowable<T> fuseToFlowable() {",
            "    /** 融合为 {@link FlowableSingle}（无 defaultValue，failOnEmpty=false）。 */\n"
            "    @Override\n    public Flowable<T> fuseToFlowable() {",
        ),
        (
            "    static final class SingleElementSubscriber<T>\n    implements FlowableSubscriber<T>, Disposable {",
            "    /** 0 元素 onComplete，1 元素 onSuccess，多于 1 个 onError。 */\n"
            "    static final class SingleElementSubscriber<T>\n    implements FlowableSubscriber<T>, Disposable {",
        ),
    ],
    "FlowableSingleSingle.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class FlowableSingleSingle",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 将上游 0 或 1 个元素转为 {@link Single}；多于一个则 onError，无元素则 defaultValue 或 onError。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class FlowableSingleSingle",
        ),
        (
            "    public FlowableSingleSingle(Flowable<T> source, T defaultValue) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param defaultValue 无元素时的默认值（null 则 onError）\n"
            "     */\n"
            "    public FlowableSingleSingle(Flowable<T> source, T defaultValue) {",
        ),
        (
            "    @Override\n    public Flowable<T> fuseToFlowable() {",
            "    /** 融合为 {@link FlowableSingle}（failOnEmpty=true）。 */\n"
            "    @Override\n    public Flowable<T> fuseToFlowable() {",
        ),
        (
            "    static final class SingleElementSubscriber<T>\n    implements FlowableSubscriber<T>, Disposable {",
            "    /** 收集唯一元素；无元素时用 defaultValue 或 NoSuchElementException。 */\n"
            "    static final class SingleElementSubscriber<T>\n    implements FlowableSubscriber<T>, Disposable {",
        ),
    ],
    "FlowableSkip.java": [
        (
            "import io.reactivex.rxjava4.internal.subscriptions.SubscriptionHelper;\n\npublic final class FlowableSkip",
            "import io.reactivex.rxjava4.internal.subscriptions.SubscriptionHelper;\n\n"
            "/**\n"
            " * 跳过上游前 n 个元素，之后原样转发。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class FlowableSkip",
        ),
        (
            "    public FlowableSkip(Flowable<T> source, long n) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param n 要跳过的元素个数\n"
            "     */\n"
            "    public FlowableSkip(Flowable<T> source, long n) {",
        ),
        (
            "    static final class SkipSubscriber<T> implements FlowableSubscriber<T>, Subscription {",
            "    /** 递减 remaining 计数，为 0 后开始转发 onNext。 */\n"
            "    static final class SkipSubscriber<T> implements FlowableSubscriber<T>, Subscription {",
        ),
        (
            "        @Override\n        public void onSubscribe(Subscription s) {",
            "        /** 向 upstream 预 request remaining 个以跳过前 n 个元素。 */\n"
            "        @Override\n        public void onSubscribe(Subscription s) {",
        ),
    ],
    "FlowableSkipLast.java": [
        (
            "import io.reactivex.rxjava4.internal.subscriptions.SubscriptionHelper;\n\npublic final class FlowableSkipLast",
            "import io.reactivex.rxjava4.internal.subscriptions.SubscriptionHelper;\n\n"
            "/**\n"
            " * 跳过序列末尾 skip 个元素（滑动窗口：队列满后才发射队首）。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class FlowableSkipLast",
        ),
        (
            "    public FlowableSkipLast(Flowable<T> source, int skip) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param skip 末尾要跳过的元素个数\n"
            "     */\n"
            "    public FlowableSkipLast(Flowable<T> source, int skip) {",
        ),
        (
            "    static final class SkipLastSubscriber<T> extends ArrayDeque<T> implements FlowableSubscriber<T>, Subscription {",
            "    /** 固定大小 deque：满时 poll 队首发射，否则向上游 request(1)。 */\n"
            "    static final class SkipLastSubscriber<T> extends ArrayDeque<T> implements FlowableSubscriber<T>, Subscription {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** 队列满时发射队首；始终 offer 新元素。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
    ],
    "FlowableSkipLastTimed.java": [
        (
            "import io.reactivex.rxjava4.operators.SpscLinkedArrayQueue;\n\npublic final class FlowableSkipLastTimed",
            "import io.reactivex.rxjava4.operators.SpscLinkedArrayQueue;\n\n"
            "/**\n"
            " * 跳过最近 time 时间窗口内的元素，仅发射更早的元素。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class FlowableSkipLastTimed",
        ),
        (
            "    public FlowableSkipLastTimed(Flowable<T> source, long time, TimeUnit unit, Scheduler scheduler, int bufferSize, boolean delayError) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param time 时间窗口长度\n"
            "     * @param unit 时间单位\n"
            "     * @param scheduler 用于时间戳的调度器\n"
            "     * @param bufferSize 时间戳队列缓冲大小\n"
            "     * @param delayError 是否在队列排空后再转发 onError\n"
            "     */\n"
            "    public FlowableSkipLastTimed(Flowable<T> source, long time, TimeUnit unit, Scheduler scheduler, int bufferSize, boolean delayError) {",
        ),
        (
            "    static final class SkipLastTimedSubscriber<T> extends AtomicInteger implements FlowableSubscriber<T>, Subscription {",
            "    /** 带时间戳队列：仅 emit 时间戳早于 now-time 的元素。 */\n"
            "    static final class SkipLastTimedSubscriber<T> extends AtomicInteger implements FlowableSubscriber<T>, Subscription {",
        ),
        (
            "        void drain() {",
            "        /** 从队列取出已超出时间窗口的元素并向下游发射。 */\n"
            "        void drain() {",
        ),
        (
            "        boolean checkTerminated(boolean d, boolean empty, Subscriber<? super T> a, boolean delayError) {",
            "        /** 按 delayError 策略在 done/empty 时转发 onError 或 onComplete。 */\n"
            "        boolean checkTerminated(boolean d, boolean empty, Subscriber<? super T> a, boolean delayError) {",
        ),
    ],
    "FlowableSkipUntil.java": [
        (
            "import io.reactivex.rxjava4.operators.ConditionalSubscriber;\n\npublic final class FlowableSkipUntil",
            "import io.reactivex.rxjava4.operators.ConditionalSubscriber;\n\n"
            "/**\n"
            " * 在 other 发出首个信号前跳过上游所有元素，之后原样转发。\n"
            " * @param <T> 上游元素类型\n"
            " * @param <U> 门控信号类型\n"
            " */\n"
            "public final class FlowableSkipUntil",
        ),
        (
            "    public FlowableSkipUntil(Flowable<T> source, Publisher<U> other) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param other 门控 Publisher（其 onNext/onComplete 打开 gate）\n"
            "     */\n"
            "    public FlowableSkipUntil(Flowable<T> source, Publisher<U> other) {",
        ),
        (
            "    static final class SkipUntilMainSubscriber<T> extends AtomicInteger\n    implements ConditionalSubscriber<T>, Subscription {",
            "    /** gate 打开前丢弃上游元素；打开后通过 HalfSerializer 转发。 */\n"
            "    static final class SkipUntilMainSubscriber<T> extends AtomicInteger\n    implements ConditionalSubscriber<T>, Subscription {",
        ),
        (
            "        @Override\n        public boolean tryOnNext(T t) {",
            "        /** gate 为 true 时转发元素；否则返回 false 并 request(1) 丢弃。 */\n"
            "        @Override\n        public boolean tryOnNext(T t) {",
        ),
        (
            "        final class OtherSubscriber extends AtomicReference<Subscription>\n        implements FlowableSubscriber<Object> {",
            "        /** 门控 subscriber：onNext/onComplete 时将 gate 置 true。 */\n"
            "        final class OtherSubscriber extends AtomicReference<Subscription>\n        implements FlowableSubscriber<Object> {",
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
            "wave12a Flowable* [0:15]",
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
