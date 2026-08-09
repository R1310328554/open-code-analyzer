#!/usr/bin/env python3
"""Chinese-annotate RxJava 4.0.0-alpha-21 wave-13a Flowable/Maybe operators [0:15]."""
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
WAVE13A_FILE = Path("/tmp/rxjava_w13a.txt")
BATCH_FILES = [
    ln.strip()
    for ln in WAVE13A_FILE.read_text(encoding="utf-8").splitlines()
    if ln.strip()
]

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "FlowableTimeoutTimed.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class FlowableTimeoutTimed",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 若相邻两次 onNext 间隔超过 {@code timeout}，则触发超时；\n"
            " * {@code other} 非 null 时切换到备用 Publisher，否则 onError {@link TimeoutException}。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class FlowableTimeoutTimed",
        ),
        (
            "    public FlowableTimeoutTimed(Flowable<T> source,\n            long timeout, TimeUnit unit, Scheduler scheduler, Publisher<? extends T> other) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param timeout 相邻元素最大间隔\n"
            "     * @param unit 时间单位\n"
            "     * @param scheduler 调度超时任务\n"
            "     * @param other 超时时的备用 Publisher（可为 null）\n"
            "     */\n"
            "    public FlowableTimeoutTimed(Flowable<T> source,\n            long timeout, TimeUnit unit, Scheduler scheduler, Publisher<? extends T> other) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> s) {",
            "    /** 按 other 是否为 null 选择 TimeoutSubscriber 或 TimeoutFallbackSubscriber。 */\n"
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> s) {",
        ),
        (
            "    static final class TimeoutSubscriber<T> extends AtomicLong\n    implements FlowableSubscriber<T>, Subscription, TimeoutSupport {",
            "    /** 无 fallback：超时 onError {@link TimeoutException}。 */\n"
            "    static final class TimeoutSubscriber<T> extends AtomicLong\n    implements FlowableSubscriber<T>, Subscription, TimeoutSupport {",
        ),
        (
            "        void startTimeout(long nextIndex) {",
            "        /** 在 Worker 上 schedule 下一次超时检查。 */\n"
            "        void startTimeout(long nextIndex) {",
        ),
        (
            "        @Override\n        public void onTimeout(long idx) {",
            "        /** 索引 idx 超时：cancel 上游并 onError。 */\n"
            "        @Override\n        public void onTimeout(long idx) {",
        ),
        (
            "    record TimeoutTask(long idx, TimeoutSupport parent) implements Runnable {",
            "    /** 超时任务：通知 parent.onTimeout(idx)。 */\n"
            "    record TimeoutTask(long idx, TimeoutSupport parent) implements Runnable {",
        ),
        (
            "    static final class TimeoutFallbackSubscriber<T> extends SubscriptionArbiter\n    implements FlowableSubscriber<T>, TimeoutSupport {",
            "    /** 超时时切换到 fallback Publisher（SubscriptionArbiter）。 */\n"
            "    static final class TimeoutFallbackSubscriber<T> extends SubscriptionArbiter\n    implements FlowableSubscriber<T>, TimeoutSupport {",
        ),
        (
            "        @Override\n        public void onTimeout(long idx) {",
            "        /** 超时后 produced 已消费项并订阅 fallback。 */\n"
            "        @Override\n        public void onTimeout(long idx) {",
        ),
        (
            "    record FallbackSubscriber<T>(Subscriber<? super T> downstream,\n                                 SubscriptionArbiter arbiter) implements FlowableSubscriber<T> {",
            "    /** fallback 序列订阅者，经 arbiter 转发。 */\n"
            "    record FallbackSubscriber<T>(Subscriber<? super T> downstream,\n                                 SubscriptionArbiter arbiter) implements FlowableSubscriber<T> {",
        ),
        (
            "    interface TimeoutSupport {",
            "    /** 超时回调接口。 */\n"
            "    interface TimeoutSupport {",
        ),
    ],
    "FlowableTimer.java": [
        (
            "import io.reactivex.rxjava4.internal.subscriptions.SubscriptionHelper;\n\npublic final class FlowableTimer",
            "import io.reactivex.rxjava4.internal.subscriptions.SubscriptionHelper;\n\n"
            "/**\n"
            " * 延迟 {@code delay} 后向下游发射单个 {@code 0L} 并 onComplete。\n"
            " * 若到时仍未 request 则 onError {@link MissingBackpressureException}。\n"
            " */\n"
            "public final class FlowableTimer",
        ),
        (
            "    public FlowableTimer(long delay, TimeUnit unit, Scheduler scheduler) {",
            "    /**\n"
            "     * @param delay 延迟时长\n"
            "     * @param unit 时间单位\n"
            "     * @param scheduler 执行定时任务的 Scheduler\n"
            "     */\n"
            "    public FlowableTimer(long delay, TimeUnit unit, Scheduler scheduler) {",
        ),
        (
            "    @Override\n    public void subscribeActual(Subscriber<? super Long> s) {",
            "    /** 创建 TimerSubscriber 并在 Scheduler 上 scheduleDirect。 */\n"
            "    @Override\n    public void subscribeActual(Subscriber<? super Long> s) {",
        ),
        (
            "    static final class TimerSubscriber extends AtomicReference<Disposable>\n    implements Subscription, Runnable {",
            "    /** 持有 Disposable；run 时检查 requested 再发射 0L。 */\n"
            "    static final class TimerSubscriber extends AtomicReference<Disposable>\n    implements Subscription, Runnable {",
        ),
        (
            "        @Override\n        public void run() {",
            "        /** 已 request 则 onNext(0L)+onComplete，否则 MissingBackpressureException。 */\n"
            "        @Override\n        public void run() {",
        ),
    ],
    "FlowableToList.java": [
        (
            "import io.reactivex.rxjava4.internal.util.ExceptionHelper;\n\npublic final class FlowableToList",
            "import io.reactivex.rxjava4.internal.util.ExceptionHelper;\n\n"
            "/**\n"
            " * 将上游所有元素收集到 {@link Collection}，\n"
            " * 上游 onComplete 时以单个集合向下游发射。\n"
            " * @param <T> 元素类型\n"
            " * @param <U> 集合类型\n"
            " */\n"
            "public final class FlowableToList",
        ),
        (
            "    public FlowableToList(Flowable<T> source, Supplier<U> collectionSupplier) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param collectionSupplier 创建空集合的 Supplier\n"
            "     */\n"
            "    public FlowableToList(Flowable<T> source, Supplier<U> collectionSupplier) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Subscriber<? super U> s) {",
            "    /** 获取集合实例并订阅 ToListSubscriber。 */\n"
            "    @Override\n    protected void subscribeActual(Subscriber<? super U> s) {",
        ),
        (
            "    static final class ToListSubscriber<T, U extends Collection<? super T>>\n    extends DeferredScalarSubscription<U>\n    implements FlowableSubscriber<T> {",
            "    /** request(Long.MAX_VALUE) 收集元素；onComplete 时 complete(collection)。 */\n"
            "    static final class ToListSubscriber<T, U extends Collection<? super T>>\n    extends DeferredScalarSubscription<U>\n    implements FlowableSubscriber<T> {",
        ),
    ],
    "FlowableToListSingle.java": [
        (
            "public final class FlowableToListSingle<T, U extends Collection<? super T>> extends Single<U> implements FuseToFlowable<U> {",
            "/**\n"
            " * 将 Flowable 全部元素收集为 {@link Single} 结果（默认 {@link ArrayList}）。\n"
            " * @param <T> 元素类型\n"
            " * @param <U> 集合类型\n"
            " */\n"
            "public final class FlowableToListSingle<T, U extends Collection<? super T>> extends Single<U> implements FuseToFlowable<U> {",
        ),
        (
            "    @SuppressWarnings(\"unchecked\")\n    public FlowableToListSingle(Flowable<T> source) {",
            "    /** 使用默认 ArrayList Supplier。 */\n"
            "    @SuppressWarnings(\"unchecked\")\n    public FlowableToListSingle(Flowable<T> source) {",
        ),
        (
            "    public FlowableToListSingle(Flowable<T> source, Supplier<U> collectionSupplier) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param collectionSupplier 创建空集合的 Supplier\n"
            "     */\n"
            "    public FlowableToListSingle(Flowable<T> source, Supplier<U> collectionSupplier) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(SingleObserver<? super U> observer) {",
            "    /** 收集完成后 onSuccess(collection)。 */\n"
            "    @Override\n    protected void subscribeActual(SingleObserver<? super U> observer) {",
        ),
        (
            "    @Override\n    public Flowable<U> fuseToFlowable() {",
            "    /** 转为 {@link FlowableToList} 以便 fuse。 */\n"
            "    @Override\n    public Flowable<U> fuseToFlowable() {",
        ),
        (
            "    static final class ToListSubscriber<T, U extends Collection<? super T>>\n    implements FlowableSubscriber<T>, Disposable {",
            "    /** request 全部元素；onComplete 时 onSuccess。 */\n"
            "    static final class ToListSubscriber<T, U extends Collection<? super T>>\n    implements FlowableSubscriber<T>, Disposable {",
        ),
    ],
    "FlowableUnsubscribeOn.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class FlowableUnsubscribeOn",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 在指定 {@link Scheduler} 上异步执行 upstream.cancel()，\n"
            " * 避免 cancel 阻塞调用线程。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class FlowableUnsubscribeOn",
        ),
        (
            "    public FlowableUnsubscribeOn(Flowable<T> source, Scheduler scheduler) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param scheduler 执行 cancel 的 Scheduler\n"
            "     */\n"
            "    public FlowableUnsubscribeOn(Flowable<T> source, Scheduler scheduler) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> s) {",
            "    /** 包装为 UnsubscribeSubscriber。 */\n"
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> s) {",
        ),
        (
            "    static final class UnsubscribeSubscriber<T> extends AtomicBoolean implements FlowableSubscriber<T>, Subscription {",
            "    /** cancel 时 scheduleDirect 执行 upstream.cancel()。 */\n"
            "    static final class UnsubscribeSubscriber<T> extends AtomicBoolean implements FlowableSubscriber<T>, Subscription {",
        ),
        (
            "        @Override\n        public void cancel() {",
            "        /** CAS 置位后 schedule Cancellation 任务。 */\n"
            "        @Override\n        public void cancel() {",
        ),
        (
            "        final class Cancellation implements Runnable {",
            "        /** 在 Scheduler 线程 cancel 上游。 */\n"
            "        final class Cancellation implements Runnable {",
        ),
    ],
    "FlowableUsing.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class FlowableUsing",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 获取资源 D，由 sourceSupplier 创建 Publisher 并订阅；\n"
            " * 终止或 cancel 时通过 disposer 释放资源。\n"
            " * @param <T> 元素类型\n"
            " * @param <D> 资源类型\n"
            " */\n"
            "public final class FlowableUsing",
        ),
        (
            "    public FlowableUsing(Supplier<? extends D> resourceSupplier,\n            Function<? super D, ? extends Publisher<? extends T>> sourceSupplier,\n            Consumer<? super D> disposer,\n            boolean eager) {",
            "    /**\n"
            "     * @param resourceSupplier 创建资源\n"
            "     * @param sourceSupplier 由资源创建 Publisher\n"
            "     * @param disposer 释放资源\n"
            "     * @param eager true 时在 onError/onComplete 前先 dispose\n"
            "     */\n"
            "    public FlowableUsing(Supplier<? extends D> resourceSupplier,\n            Function<? super D, ? extends Publisher<? extends T>> sourceSupplier,\n            Consumer<? super D> disposer,\n            boolean eager) {",
        ),
        (
            "    @Override\n    public void subscribeActual(Subscriber<? super T> s) {",
            "    /** 获取资源与 Publisher，失败时 dispose 并 error。 */\n"
            "    @Override\n    public void subscribeActual(Subscriber<? super T> s) {",
        ),
        (
            "    static final class UsingSubscriber<T, D> extends AtomicBoolean implements FlowableSubscriber<T>, Subscription {",
            "    /** 按 eager 标志决定 dispose 时机。 */\n"
            "    static final class UsingSubscriber<T, D> extends AtomicBoolean implements FlowableSubscriber<T>, Subscription {",
        ),
        (
            "        void disposeResource() {",
            "        /** CAS 一次性调用 disposer；异常走 RxJavaPlugins.onError。 */\n"
            "        void disposeResource() {",
        ),
        (
            "                    // can't call actual.onError unless it is serialized, which is expensive",
            "                    // 非 eager 路径无法安全调用 downstream.onError，仅上报插件",
        ),
    ],
    "FlowableWindowBoundary.java": [
        (
            "import io.reactivex.rxjava4.subscribers.DisposableSubscriber;\n\npublic final class FlowableWindowBoundary",
            "import io.reactivex.rxjava4.subscribers.DisposableSubscriber;\n\n"
            "/**\n"
            " * 按边界 Publisher {@code other} 的信号打开新窗口，\n"
            " * 每个窗口为独立 {@link Flowable}（UnicastProcessor）。\n"
            " * @param <T> 主序列元素类型\n"
            " * @param <B> 边界信号类型\n"
            " */\n"
            "public final class FlowableWindowBoundary",
        ),
        (
            "    public FlowableWindowBoundary(Flowable<T> source, Publisher<B> other, int capacityHint) {",
            "    /**\n"
            "     * @param source 主序列 Flowable\n"
            "     * @param other 边界信号 Publisher\n"
            "     * @param capacityHint 每个 UnicastProcessor 容量提示\n"
            "     */\n"
            "    public FlowableWindowBoundary(Flowable<T> source, Publisher<B> other, int capacityHint) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Subscriber<? super Flowable<T>> subscriber) {",
            "    /** 订阅边界与主序列，drain 驱动窗口开关。 */\n"
            "    @Override\n    protected void subscribeActual(Subscriber<? super Flowable<T>> subscriber) {",
        ),
        (
            "    static final class WindowBoundaryMainSubscriber<T, B>\n    extends AtomicInteger\n    implements FlowableSubscriber<T>, Subscription, Runnable {",
            "    /** MpscLinkedQueue 缓冲元素与 NEXT_WINDOW 标记；drain 发射窗口 Flowable。 */\n"
            "    static final class WindowBoundaryMainSubscriber<T, B>\n    extends AtomicInteger\n    implements FlowableSubscriber<T>, Subscription, Runnable {",
        ),
        (
            "        static final Object NEXT_WINDOW = new Object();",
            "        /** 队列中标记应打开新窗口。 */\n"
            "        static final Object NEXT_WINDOW = new Object();",
        ),
        (
            "        @Override\n        public void run() {",
            "        /** 窗口 Flowable 终止时递减 windows，为 0 则 cancel 上游。 */\n"
            "        @Override\n        public void run() {",
        ),
        (
            "        void drain() {",
            "        /** 从队列取元素或开新窗口；背压不足时 MissingBackpressureException。 */\n"
            "        void drain() {",
        ),
        (
            "    static final class WindowBoundaryInnerSubscriber<T, B> extends DisposableSubscriber<B> {",
            "    /** 边界 onNext 触发 innerNext 打开新窗口。 */\n"
            "    static final class WindowBoundaryInnerSubscriber<T, B> extends DisposableSubscriber<B> {",
        ),
    ],
    "FlowableWindowSubscribeIntercept.java": [
        (
            "/**\n * Wrapper for a FlowableProcessor that detects an incoming subscriber.\n * @param <T> the element type of the flow.\n * @since 3.0.0\n */",
            "/**\n"
            " * 包装 {@link FlowableProcessor}，检测是否有订阅者接入窗口。\n"
            " * @param <T> 元素类型\n"
            " * @since 3.0.0\n"
            " */",
        ),
        (
            "    FlowableWindowSubscribeIntercept(FlowableProcessor<T> source) {",
            "    /** @param source 窗口底层 Processor */\n"
            "    FlowableWindowSubscribeIntercept(FlowableProcessor<T> source) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> s) {",
            "    /** 订阅 window 并将 once 置 true。 */\n"
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> s) {",
        ),
        (
            "    boolean tryAbandon() {",
            "    /** 若无订阅者则 CAS 标记放弃并返回 true。 */\n"
            "    boolean tryAbandon() {",
        ),
    ],
    "FlowableWithLatestFrom.java": [
        (
            "import io.reactivex.rxjava4.subscribers.SerializedSubscriber;\n\npublic final class FlowableWithLatestFrom",
            "import io.reactivex.rxjava4.subscribers.SerializedSubscriber;\n\n"
            "/**\n"
            " * 主序列每个元素与 other 序列最新值经 combiner 合并后发射。\n"
            " * other 尚无值时跳过该主序列元素并 request(1)。\n"
            " * @param <T> 主序列类型\n"
            " * @param <U> other 序列类型\n"
            " * @param <R> 合并结果类型\n"
            " */\n"
            "public final class FlowableWithLatestFrom",
        ),
        (
            "    public FlowableWithLatestFrom(Flowable<T> source, BiFunction<? super T, ? super U, ? extends R> combiner, Publisher<? extends U> other) {",
            "    /**\n"
            "     * @param source 主序列 Flowable\n"
            "     * @param combiner 合并函数\n"
            "     * @param other 提供最新值的 Publisher\n"
            "     */\n"
            "    public FlowableWithLatestFrom(Flowable<T> source, BiFunction<? super T, ? super U, ? extends R> combiner, Publisher<? extends U> other) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Subscriber<? super R> s) {",
            "    /** SerializedSubscriber 包装；先订阅 other 再订阅 source。 */\n"
            "    @Override\n    protected void subscribeActual(Subscriber<? super R> s) {",
        ),
        (
            "    static final class WithLatestFromSubscriber<T, U, R> extends AtomicReference<U>\n    implements ConditionalSubscriber<T>, Subscription {",
            "    /** AtomicReference 存 other 最新值；tryOnNext 有值才合并。 */\n"
            "    static final class WithLatestFromSubscriber<T, U, R> extends AtomicReference<U>\n    implements ConditionalSubscriber<T>, Subscription {",
        ),
        (
            "    final class FlowableWithLatestSubscriber implements FlowableSubscriber<U> {",
            "    /** 订阅 other 并 lazySet 最新值。 */\n"
            "    final class FlowableWithLatestSubscriber implements FlowableSubscriber<U> {",
        ),
        (
            "            // nothing to do, the wlf will complete on its own pace",
            "            // other 完成不影响主序列，由 wlf 自行终止",
        ),
    ],
    "FlowableWithLatestFromMany.java": [
        (
            "/**\n * Combines a main sequence of values with the latest from multiple other sequences via\n * a selector function.\n *\n * @param <T> the main sequence's type\n * @param <R> the output type\n */",
            "/**\n"
            " * 主序列每个元素与多个 other 序列各自最新值，\n"
            " * 经 combiner 合并为数组后映射为 R。\n"
            " *\n"
            " * @param <T> 主序列元素类型\n"
            " * @param <R> 输出类型\n"
            " */",
        ),
        (
            "    public FlowableWithLatestFromMany(@NonNull Flowable<T> source, @NonNull Publisher<?>[] otherArray, Function<? super Object[], R> combiner) {",
            "    /**\n"
            "     * @param source 主序列 Flowable\n"
            "     * @param otherArray 其他 Publisher 数组\n"
            "     * @param combiner 合并 Object[] 的函数\n"
            "     */\n"
            "    public FlowableWithLatestFromMany(@NonNull Flowable<T> source, @NonNull Publisher<?>[] otherArray, Function<? super Object[], R> combiner) {",
        ),
        (
            "    public FlowableWithLatestFromMany(@NonNull Flowable<T> source, @NonNull Iterable<? extends Publisher<?>> otherIterable, @NonNull Function<? super Object[], R> combiner) {",
            "    /**\n"
            "     * @param source 主序列 Flowable\n"
            "     * @param otherIterable 其他 Publisher 可迭代集合\n"
            "     * @param combiner 合并 Object[] 的函数\n"
            "     */\n"
            "    public FlowableWithLatestFromMany(@NonNull Flowable<T> source, @NonNull Iterable<? extends Publisher<?>> otherIterable, @NonNull Function<? super Object[], R> combiner) {",
        ),
        (
            "    static final class WithLatestFromSubscriber<T, R>\n    extends AtomicInteger\n    implements ConditionalSubscriber<T>, Subscription {",
            "    /** values 数组存各 other 最新值；全部就绪才 combiner。 */\n"
            "    static final class WithLatestFromSubscriber<T, R>\n    extends AtomicInteger\n    implements ConditionalSubscriber<T>, Subscription {",
        ),
        (
            "                    // somebody hasn't signaled yet, skip this T",
            "                    // 某 other 尚无值，跳过当前 T",
        ),
        (
            "    static final class WithLatestInnerSubscriber\n    extends AtomicReference<Subscription>\n    implements FlowableSubscriber<Object> {",
            "    /** 订阅单个 other 并将 onNext 写入 values[index]。 */\n"
            "    static final class WithLatestInnerSubscriber\n    extends AtomicReference<Subscription>\n    implements FlowableSubscriber<Object> {",
        ),
        (
            "    final class SingletonArrayFunc implements Function<T, R> {",
            "    /** other 为空时退化为单元素数组 combiner。 */\n"
            "    final class SingletonArrayFunc implements Function<T, R> {",
        ),
    ],
    "FlowableZipIterable.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class FlowableZipIterable",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 将上游每个元素与 {@link Iterable} 迭代器当前项经 zipper 合并；\n"
            " * 迭代器耗尽时 onComplete。\n"
            " * @param <T> 上游元素类型\n"
            " * @param <U> Iterable 元素类型\n"
            " * @param <V> 合并结果类型\n"
            " */\n"
            "public final class FlowableZipIterable",
        ),
        (
            "    public FlowableZipIterable(\n            Flowable<T> source,\n            Iterable<U> other, BiFunction<? super T, ? super U, ? extends V> zipper) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param other 与上游 zip 的 Iterable\n"
            "     * @param zipper 二元合并函数\n"
            "     */\n"
            "    public FlowableZipIterable(\n            Flowable<T> source,\n            Iterable<U> other, BiFunction<? super T, ? super U, ? extends V> zipper) {",
        ),
        (
            "    @Override\n    public void subscribeActual(Subscriber<? super V> t) {",
            "    /** 校验 iterator 非空且有元素后订阅 ZipIterableSubscriber。 */\n"
            "    @Override\n    public void subscribeActual(Subscriber<? super V> t) {",
        ),
        (
            "    static final class ZipIterableSubscriber<T, U, V> implements FlowableSubscriber<T>, Subscription {",
            "    /** 每 onNext 取 iterator.next() 经 zipper 合并；无下一项则 complete。 */\n"
            "    static final class ZipIterableSubscriber<T, U, V> implements FlowableSubscriber<T>, Subscription {",
        ),
        (
            "        void fail(Throwable e) {",
            "        /** 标记 done、cancel 上游并 onError。 */\n"
            "        void fail(Throwable e) {",
        ),
    ],
    "AbstractMaybeWithUpstream.java": [
        (
            "/**\n * Abstract base class for intermediate Maybe operators that take an upstream MaybeSource.\n *\n * @param <T> the source value type\n * @param <R> the output value type\n */",
            "/**\n"
            " * 接受上游 {@link MaybeSource} 的中间 Maybe 算子抽象基类。\n"
            " *\n"
            " * @param <T> 上游值类型\n"
            " * @param <R> 输出值类型\n"
            " */",
        ),
        (
            "    AbstractMaybeWithUpstream(MaybeSource<T> source) {",
            "    /** @param source 上游 MaybeSource */\n"
            "    AbstractMaybeWithUpstream(MaybeSource<T> source) {",
        ),
        (
            "    @Override\n    public final MaybeSource<T> source() {",
            "    /** 返回上游 MaybeSource。 */\n"
            "    @Override\n    public final MaybeSource<T> source() {",
        ),
    ],
    "MaybeAmb.java": [
        (
            "/**\n * Signals the event of the first MaybeSource that signals.\n *\n * @param <T> the value type emitted\n */",
            "/**\n"
            " * 订阅多个 {@link MaybeSource}，仅转发首个发出事件的源。\n"
            " *\n"
            " * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    public MaybeAmb(MaybeSource<? extends T>[] sources, Iterable<? extends MaybeSource<? extends T>> sourcesIterable) {",
            "    /**\n"
            "     * @param sources Maybe 源数组（与 sourcesIterable 二选一）\n"
            "     * @param sourcesIterable Maybe 源可迭代集合\n"
            "     */\n"
            "    public MaybeAmb(MaybeSource<? extends T>[] sources, Iterable<? extends MaybeSource<? extends T>> sourcesIterable) {",
        ),
        (
            "    @Override\n    @SuppressWarnings(\"unchecked\")\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
            "    /** 全部订阅；AtomicBoolean winner 保证仅首个事件向下游。 */\n"
            "    @Override\n    @SuppressWarnings(\"unchecked\")\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
        ),
        (
            "    static final class AmbMaybeObserver<T>\n    implements MaybeObserver<T> {",
            "    /** CAS winner 后 dispose 其余源并转发事件。 */\n"
            "    static final class AmbMaybeObserver<T>\n    implements MaybeObserver<T> {",
        ),
    ],
    "MaybeCache.java": [
        (
            "/**\n * Consumes the source once and replays its signal to any current or future MaybeObservers.\n *\n * @param <T> the value type\n */",
            "/**\n"
            " * 仅订阅上游一次，将 onSuccess/onError/onComplete 缓存并重放给后续观察者。\n"
            " *\n"
            " * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    @SuppressWarnings(\"unchecked\")\n    public MaybeCache(MaybeSource<T> source) {",
            "    /** @param source 待缓存的 MaybeSource */\n"
            "    @SuppressWarnings(\"unchecked\")\n    public MaybeCache(MaybeSource<T> source) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
            "    /** 首个订阅者触发 source 订阅；已终止则直接重放缓存。 */\n"
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
        ),
        (
            "        // deliberately ignored",
            "        // 作为 source 订阅者，忽略上游 Disposable",
        ),
        (
            "    boolean add(CacheDisposable<T> inner) {",
            "    /** CAS 将观察者加入 observers 数组。 */\n"
            "    boolean add(CacheDisposable<T> inner) {",
        ),
        (
            "    static final class CacheDisposable<T>\n    extends AtomicReference<MaybeCache<T>>\n    implements Disposable {",
            "    /** dispose 时从 MaybeCache 移除自身。 */\n"
            "    static final class CacheDisposable<T>\n    extends AtomicReference<MaybeCache<T>>\n    implements Disposable {",
        ),
    ],
    "MaybeCallbackObserver.java": [
        (
            "/**\n * MaybeObserver that delegates the onSuccess, onError and onComplete method calls to callbacks.\n *\n * @param <T> the value type\n */",
            "/**\n"
            " * 将 onSuccess、onError、onComplete 委托给对应回调的 {@link MaybeObserver}。\n"
            " *\n"
            " * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    public MaybeCallbackObserver(Consumer<? super T> onSuccess, Consumer<? super Throwable> onError,\n            Action onComplete) {",
            "    /**\n"
            "     * @param onSuccess 成功回调\n"
            "     * @param onError 错误回调\n"
            "     * @param onComplete 完成回调\n"
            "     */\n"
            "    public MaybeCallbackObserver(Consumer<? super T> onSuccess, Consumer<? super Throwable> onError,\n            Action onComplete) {",
        ),
        (
            "    @Override\n    public void onSuccess(T value) {",
            "    /** dispose 后调用 onSuccess 回调。 */\n"
            "    @Override\n    public void onSuccess(T value) {",
        ),
        (
            "    @Override\n    public boolean hasCustomOnError() {",
            "    /** 是否非默认 ON_ERROR_MISSING 回调。 */\n"
            "    @Override\n    public boolean hasCustomOnError() {",
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
            "wave13a Flowable*/Maybe* [0:15]",
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
        print(f"Marked {ok} files done in queue (note=wave13a)")
    print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False, indent=2))
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
