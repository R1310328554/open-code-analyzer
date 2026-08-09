#!/usr/bin/env python3
"""Chinese-annotate RxJava 4.0.0-alpha-21 wave-12b Flowable operators [15:30]."""
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
WAVE12B_FILE = Path("/tmp/rxjava_w12b.txt")
BATCH_FILES = [
    ln.strip()
    for ln in WAVE12B_FILE.read_text(encoding="utf-8").splitlines()
    if ln.strip()
]

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "FlowableSkipWhile.java": [
        (
            "import io.reactivex.rxjava4.internal.subscriptions.SubscriptionHelper;\n\npublic final class FlowableSkipWhile",
            "import io.reactivex.rxjava4.internal.subscriptions.SubscriptionHelper;\n\n"
            "/**\n"
            " * 跳过 {@link Predicate} 为 true 的连续前缀元素，\n"
            " * 从首个 false 元素起向下游转发。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class FlowableSkipWhile",
        ),
        (
            "    public FlowableSkipWhile(Flowable<T> source, Predicate<? super T> predicate) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param predicate 为 true 时跳过该元素\n"
            "     */\n"
            "    public FlowableSkipWhile(Flowable<T> source, Predicate<? super T> predicate) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> s) {",
            "    /** 订阅 SkipWhileSubscriber。 */\n"
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> s) {",
        ),
        (
            "    static final class SkipWhileSubscriber<T> implements FlowableSubscriber<T>, Subscription {",
            "    /** 跳过阶段 request(1) 逐元素测试；结束后透传 onNext。 */\n"
            "    static final class SkipWhileSubscriber<T> implements FlowableSubscriber<T>, Subscription {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** predicate 为 true 则丢弃并 request(1)；false 起转发。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
    ],
    "FlowableSubscribeOn.java": [
        (
            "/**\n * Subscribes to the source Flowable on the specified Scheduler and makes\n * sure downstream requests are scheduled there as well.\n *\n * @param <T> the value type emitted\n */",
            "/**\n"
            " * 在指定 {@link Scheduler} 上订阅上游 {@link Flowable}，\n"
            " * 并可选择将下游 request 也调度到该 Scheduler。\n"
            " *\n * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    public FlowableSubscribeOn(Flowable<T> source, Scheduler scheduler, boolean nonScheduledRequests) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param scheduler 订阅与（可选）request 所在 Scheduler\n"
            "     * @param nonScheduledRequests true 时 request 可在调用线程直接执行\n"
            "     */\n"
            "    public FlowableSubscribeOn(Flowable<T> source, Scheduler scheduler, boolean nonScheduledRequests) {",
        ),
        (
            "    @Override\n    public void subscribeActual(final Subscriber<? super T> s) {",
            "    /** 创建 Worker 并在其上 schedule 上游订阅。 */\n"
            "    @Override\n    public void subscribeActual(final Subscriber<? super T> s) {",
        ),
        (
            "    static final class SubscribeOnSubscriber<T> extends AtomicReference<Thread>\n    implements FlowableSubscriber<T>, Subscription, Runnable {",
            "    /** 在 Worker 线程订阅上游；request 可跨线程调度。 */\n"
            "    static final class SubscribeOnSubscriber<T> extends AtomicReference<Thread>\n    implements FlowableSubscriber<T>, Subscription, Runnable {",
        ),
        (
            "        @Override\n        public void run() {",
            "        /** Worker 任务：记录线程并订阅 source。 */\n"
            "        @Override\n        public void run() {",
        ),
        (
            "        void requestUpstream(final long n, final Subscription s) {",
            "        /** 按 nonScheduledRequests 决定 request 是否 schedule 到 Worker。 */\n"
            "        void requestUpstream(final long n, final Subscription s) {",
        ),
        (
            "        record Request(Subscription upstream, long n) implements Runnable {",
            "        /** 在 Worker 上执行 upstream.request(n)。 */\n"
            "        record Request(Subscription upstream, long n) implements Runnable {",
        ),
    ],
    "FlowableSwitchIfEmpty.java": [
        (
            "import io.reactivex.rxjava4.internal.subscriptions.SubscriptionArbiter;\n\npublic final class FlowableSwitchIfEmpty",
            "import io.reactivex.rxjava4.internal.subscriptions.SubscriptionArbiter;\n\n"
            "/**\n"
            " * 上游 onComplete 且未发射任何元素时，切换到 {@code other} Publisher。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class FlowableSwitchIfEmpty",
        ),
        (
            "    public FlowableSwitchIfEmpty(Flowable<T> source, Publisher<? extends T> other) {",
            "    /**\n"
            "     * @param source 主序列 Flowable\n"
            "     * @param other 主序列为空时订阅的备用 Publisher\n"
            "     */\n"
            "    public FlowableSwitchIfEmpty(Flowable<T> source, Publisher<? extends T> other) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> s) {",
            "    /** 用 SubscriptionArbiter 协调主序列与备用序列。 */\n"
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> s) {",
        ),
        (
            "    static final class SwitchIfEmptySubscriber<T> implements FlowableSubscriber<T> {",
            "    /** empty 标志：无 onNext 时 onComplete 触发订阅 other。 */\n"
            "    static final class SwitchIfEmptySubscriber<T> implements FlowableSubscriber<T> {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** 仍为空则订阅 other，否则正常 onComplete。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
    ],
    "FlowableTake.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class FlowableTake",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 仅向下游转发前 n 个元素，随后 cancel 上游并 onComplete。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class FlowableTake",
        ),
        (
            "    public FlowableTake(Flowable<T> source, long n) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param n 最多发射的元素个数\n"
            "     */\n"
            "    public FlowableTake(Flowable<T> source, long n) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> s) {",
            "    /** 订阅 TakeSubscriber。 */\n"
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> s) {",
        ),
        (
            "    static final class TakeSubscriber<T>\n    extends AtomicLong\n    implements FlowableSubscriber<T>, Subscription {",
            "    /** 维护 remaining 计数；request 不超过 remaining。 */\n"
            "    static final class TakeSubscriber<T>\n    extends AtomicLong\n    implements FlowableSubscriber<T>, Subscription {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** 递减 remaining；归零时 cancel 并完成。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
        (
            "        @Override\n        public void request(long n) {",
            "        /** 将请求量限制在 remaining 以内转发上游。 */\n"
            "        @Override\n        public void request(long n) {",
        ),
    ],
    "FlowableTakeLast.java": [
        (
            "import io.reactivex.rxjava4.internal.util.BackpressureHelper;\n\npublic final class FlowableTakeLast",
            "import io.reactivex.rxjava4.internal.util.BackpressureHelper;\n\n"
            "/**\n"
            " * 缓存上游最后 count 个元素，上游 onComplete 后按背压依次发射。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class FlowableTakeLast",
        ),
        (
            "    public FlowableTakeLast(Flowable<T> source, int count) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param count 保留的末尾元素个数\n"
            "     */\n"
            "    public FlowableTakeLast(Flowable<T> source, int count) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> s) {",
            "    /** 订阅 TakeLastSubscriber（ArrayDeque 环形缓冲）。 */\n"
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> s) {",
        ),
        (
            "    static final class TakeLastSubscriber<T> extends ArrayDeque<T> implements FlowableSubscriber<T>, Subscription {",
            "    /** 以 deque 滑动窗口缓存；done 后 drain 发射。 */\n"
            "    static final class TakeLastSubscriber<T> extends ArrayDeque<T> implements FlowableSubscriber<T>, Subscription {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** 队列满时 poll 队首再 offer 新元素。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
        (
            "        void drain() {",
            "        /** 上游完成后按 requested 从 deque poll 并 onNext。 */\n"
            "        void drain() {",
        ),
    ],
    "FlowableTakeLastOne.java": [
        (
            "import java.io.Serial;\n\npublic final class FlowableTakeLastOne",
            "import java.io.Serial;\n\n"
            "/**\n"
            " * 仅保留并发射上游最后一个元素（{@link DeferredScalarSubscription}）。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class FlowableTakeLastOne",
        ),
        (
            "    public FlowableTakeLastOne(Flowable<T> source) {",
            "    /** @param source 上游 Flowable */\n"
            "    public FlowableTakeLastOne(Flowable<T> source) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> s) {",
            "    /** 订阅 TakeLastOneSubscriber。 */\n"
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> s) {",
        ),
        (
            "    static final class TakeLastOneSubscriber<T> extends DeferredScalarSubscription<T>\n    implements FlowableSubscriber<T> {",
            "    /** 每次 onNext 覆盖 value；onComplete 时 complete 或空完成。 */\n"
            "    static final class TakeLastOneSubscriber<T> extends DeferredScalarSubscription<T>\n    implements FlowableSubscriber<T> {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** 有缓存值则 complete(v)，否则 onComplete。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
    ],
    "FlowableTakeLastTimed.java": [
        (
            "import io.reactivex.rxjava4.operators.SpscLinkedArrayQueue;\n\npublic final class FlowableTakeLastTimed",
            "import io.reactivex.rxjava4.operators.SpscLinkedArrayQueue;\n\n"
            "/**\n"
            " * 按时间窗口与数量限制缓存元素，上游终止后发射仍有效的末尾项。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class FlowableTakeLastTimed",
        ),
        (
            "    public FlowableTakeLastTimed(Flowable<T> source,\n            long count, long time, TimeUnit unit, Scheduler scheduler,\n            int bufferSize, boolean delayError) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param count 最大保留元素数（Long.MAX_VALUE 为不限）\n"
            "     * @param time 时间窗口长度\n"
            "     * @param unit 时间单位\n"
            "     * @param scheduler 提供 now 时间戳\n"
            "     * @param bufferSize 内部队列容量\n"
            "     * @param delayError true 时先发射缓存再 onError\n"
            "     */\n"
            "    public FlowableTakeLastTimed(Flowable<T> source,\n            long count, long time, TimeUnit unit, Scheduler scheduler,\n            int bufferSize, boolean delayError) {",
        ),
        (
            "    static final class TakeLastTimedSubscriber<T> extends AtomicInteger implements FlowableSubscriber<T>, Subscription {",
            "    /** Spsc 队列存 (timestamp, value)；done 后 drain。 */\n"
            "    static final class TakeLastTimedSubscriber<T> extends AtomicInteger implements FlowableSubscriber<T>, Subscription {",
        ),
        (
            "        void trim(long now, SpscLinkedArrayQueue<Object> q) {",
            "        /** 移除超出时间窗口或超过 count 的队首元素对。 */\n"
            "        void trim(long now, SpscLinkedArrayQueue<Object> q) {",
        ),
        (
            "        void drain() {",
            "        /** 按背压从队列取出 value 发射；处理 delayError 终止路径。 */\n"
            "        void drain() {",
        ),
        (
            "        boolean checkTerminated(boolean empty, Subscriber<? super T> a, boolean delayError) {",
            "        /** 取消/错误/完成时的统一终止判断。 */\n"
            "        boolean checkTerminated(boolean empty, Subscriber<? super T> a, boolean delayError) {",
        ),
    ],
    "FlowableTakePublisher.java": [
        (
            "/**\n * Take with a generic Publisher source.\n * <p>History: 2.0.7 - experimental\n * @param <T> the value type\n * @since 2.1\n */",
            "/**\n"
            " * 对任意 {@link Publisher} 源应用 take(limit)，复用 {@link TakeSubscriber}。\n"
            " * <p>History: 2.0.7 - experimental\n"
            " * @param <T> 元素类型\n"
            " * @since 2.1\n"
            " */",
        ),
        (
            "    public FlowableTakePublisher(Publisher<T> source, long limit) {",
            "    /**\n"
            "     * @param source 上游 Publisher\n"
            "     * @param limit 最多发射的元素个数\n"
            "     */\n"
            "    public FlowableTakePublisher(Publisher<T> source, long limit) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> s) {",
            "    /** 委托 {@link FlowableTake.TakeSubscriber} 限制元素数。 */\n"
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> s) {",
        ),
    ],
    "FlowableTakeUntil.java": [
        (
            "import io.reactivex.rxjava4.internal.util.*;\n\npublic final class FlowableTakeUntil",
            "import io.reactivex.rxjava4.internal.util.*;\n\n"
            "/**\n"
            " * 转发主序列元素直至 {@code other} 发出任意信号（onNext/onComplete/onError）。\n"
            " * @param <T> 主序列元素类型\n"
            " * @param <U> other 序列元素类型\n"
            " */\n"
            "public final class FlowableTakeUntil",
        ),
        (
            "    public FlowableTakeUntil(Flowable<T> source, Publisher<? extends U> other) {",
            "    /**\n"
            "     * @param source 主序列 Flowable\n"
            "     * @param other 触发终止的 Publisher\n"
            "     */\n"
            "    public FlowableTakeUntil(Flowable<T> source, Publisher<? extends U> other) {",
        ),
        (
            "    static final class TakeUntilMainSubscriber<T> extends AtomicInteger implements FlowableSubscriber<T>, Subscription {",
            "    /** HalfSerializer 协调主/other 两路终止。 */\n"
            "    static final class TakeUntilMainSubscriber<T> extends AtomicInteger implements FlowableSubscriber<T>, Subscription {",
        ),
        (
            "        final class OtherSubscriber extends AtomicReference<Subscription> implements FlowableSubscriber<Object> {",
            "        /** other 任一路径终止时 cancel 主序列并完成下游。 */\n"
            "        final class OtherSubscriber extends AtomicReference<Subscription> implements FlowableSubscriber<Object> {",
        ),
        (
            "            @Override\n            public void onNext(Object t) {",
            "            /** other onNext 即视为终止信号。 */\n"
            "            @Override\n            public void onNext(Object t) {",
        ),
    ],
    "FlowableTakeUntilPredicate.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class FlowableTakeUntilPredicate",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 发射元素直至 {@link Predicate} 对某元素返回 true（含该元素），随后 onComplete。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class FlowableTakeUntilPredicate",
        ),
        (
            "    public FlowableTakeUntilPredicate(Flowable<T> source, Predicate<? super T> predicate) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param predicate 为 true 时终止（该元素已发射）\n"
            "     */\n"
            "    public FlowableTakeUntilPredicate(Flowable<T> source, Predicate<? super T> predicate) {",
        ),
        (
            "    static final class InnerSubscriber<T> implements FlowableSubscriber<T>, Subscription {",
            "    /** 先 onNext 再测 predicate；true 则 cancel 并完成。 */\n"
            "    static final class InnerSubscriber<T> implements FlowableSubscriber<T>, Subscription {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** 发射后若 predicate 为 true 则结束序列。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
    ],
    "FlowableTakeWhile.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class FlowableTakeWhile",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 仅发射 {@link Predicate} 为 true 的连续前缀；\n"
            " * 遇首个 false 时 cancel 上游并 onComplete（不发射该元素）。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class FlowableTakeWhile",
        ),
        (
            "    public FlowableTakeWhile(Flowable<T> source, Predicate<? super T> predicate) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param predicate 为 true 时继续发射\n"
            "     */\n"
            "    public FlowableTakeWhile(Flowable<T> source, Predicate<? super T> predicate) {",
        ),
        (
            "    static final class TakeWhileSubscriber<T> implements FlowableSubscriber<T>, Subscription {",
            "    /** predicate false 时丢弃当前元素并结束。 */\n"
            "    static final class TakeWhileSubscriber<T> implements FlowableSubscriber<T>, Subscription {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** 测试 predicate；false 则 cancel 并完成。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
    ],
    "FlowableThrottleFirstTimed.java": [
        (
            "import io.reactivex.rxjava4.subscribers.SerializedSubscriber;\n\npublic final class FlowableThrottleFirstTimed",
            "import io.reactivex.rxjava4.subscribers.SerializedSubscriber;\n\n"
            "/**\n"
            " * 在 timeout 窗口内仅发射首个元素（节流），窗口结束后重置。\n"
            " * 窗口内其余元素可选经 {@link Consumer} onDropped 处理。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class FlowableThrottleFirstTimed",
        ),
        (
            "    public FlowableThrottleFirstTimed(Flowable<T> source,\n                                      long timeout,\n                                      TimeUnit unit,\n                                      Scheduler scheduler,\n                                      Consumer<? super T> onDropped) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param timeout 节流窗口时长\n"
            "     * @param unit 时间单位\n"
            "     * @param scheduler 调度 timer\n"
            "     * @param onDropped 窗口内被丢弃元素的回调（可为 null）\n"
            "     */\n"
            "    public FlowableThrottleFirstTimed(Flowable<T> source,\n                                      long timeout,\n                                      TimeUnit unit,\n                                      Scheduler scheduler,\n                                      Consumer<? super T> onDropped) {",
        ),
        (
            "    static final class DebounceTimedSubscriber<T>\n    extends AtomicLong\n    implements FlowableSubscriber<T>, Subscription, Runnable {",
            "    /** gate 控制窗口；timer 到期 run() 重置 gate。 */\n"
            "    static final class DebounceTimedSubscriber<T>\n    extends AtomicLong\n    implements FlowableSubscriber<T>, Subscription, Runnable {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** gate 关闭时发射并启动 timer；否则调用 onDropped。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
        (
            "        @Override\n        public void run() {",
            "        /** timer 到期，打开 gate 允许下一元素。 */\n"
            "        @Override\n        public void run() {",
        ),
    ],
    "FlowableThrottleLatest.java": [
        (
            "/**\n * Emits the next or latest item when the given time elapses.\n * <p>\n * The operator emits the next item, then starts a timer. When the timer fires,\n * it tries to emit the latest item from upstream. If there was no upstream item,\n * in the meantime, the next upstream item is emitted immediately and the\n * timed process repeats.\n * <p>History: 2.1.14 - experimental\n * @param <T> the upstream and downstream value type\n * @since 2.2\n */",
            "/**\n"
            " * 在给定时间间隔内发射下一项或最新项：先发射一项并启动 timer，\n"
            " * timer 触发时发射 upstream 最新值；期间无新项则下一项立即发射并重复。\n"
            " * <p>History: 2.1.14 - experimental\n"
            " * @param <T> 上下游元素类型\n"
            " * @since 2.2\n"
            " */",
        ),
        (
            "    public FlowableThrottleLatest(Flowable<T> source,\n            long timeout, TimeUnit unit,\n            Scheduler scheduler,\n            boolean emitLast,\n            Consumer<? super T> onDropped) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param timeout 节流间隔\n"
            "     * @param unit 时间单位\n"
            "     * @param scheduler 调度 timer\n"
            "     * @param emitLast 完成时是否发射最新缓存项\n"
            "     * @param onDropped 被覆盖/丢弃元素的回调\n"
            "     */\n"
            "    public FlowableThrottleLatest(Flowable<T> source,\n            long timeout, TimeUnit unit,\n            Scheduler scheduler,\n            boolean emitLast,\n            Consumer<? super T> onDropped) {",
        ),
        (
            "    static final class ThrottleLatestSubscriber<T>\n    extends AtomicInteger\n    implements FlowableSubscriber<T>, Subscription, Runnable {",
            "    /** latest 存最新值；timerRunning/timerFired 协调节流节奏。 */\n"
            "    static final class ThrottleLatestSubscriber<T>\n    extends AtomicInteger\n    implements FlowableSubscriber<T>, Subscription, Runnable {",
        ),
        (
            "        void drain() {",
            "        /** 按 requested 发射 latest；管理 timer 与终止路径。 */\n"
            "        void drain() {",
        ),
        (
            "        void tryDropAndSignalMBE(T valueToDrop) {",
            "        /** 背压不足时 onDropped 后 signal MissingBackpressureException。 */\n"
            "        void tryDropAndSignalMBE(T valueToDrop) {",
        ),
    ],
    "FlowableTimeInterval.java": [
        (
            "import io.reactivex.rxjava4.schedulers.Timed;\n\npublic final class FlowableTimeInterval",
            "import io.reactivex.rxjava4.schedulers.Timed;\n\n"
            "/**\n"
            " * 为每个上游元素包装 {@link Timed}，value 为距上一元素的间隔时间。\n"
            " * @param <T> 上游元素类型\n"
            " */\n"
            "public final class FlowableTimeInterval",
        ),
        (
            "    public FlowableTimeInterval(Flowable<T> source, TimeUnit unit, Scheduler scheduler) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param unit 时间间隔单位\n"
            "     * @param scheduler 提供 now 时间戳\n"
            "     */\n"
            "    public FlowableTimeInterval(Flowable<T> source, TimeUnit unit, Scheduler scheduler) {",
        ),
        (
            "    static final class TimeIntervalSubscriber<T> implements FlowableSubscriber<T>, Subscription {",
            "    /** 记录 lastTime，onNext 时计算 delta 并发射 Timed。 */\n"
            "    static final class TimeIntervalSubscriber<T> implements FlowableSubscriber<T>, Subscription {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** 计算 now - lastTime 并更新 lastTime。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
    ],
    "FlowableTimeout.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class FlowableTimeout",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 若 {@code firstTimeoutIndicator} 或 per-item {@code itemTimeoutIndicator}\n"
            " * 在对应索引前发出信号则超时；可选 {@code other} 作为 fallback 序列。\n"
            " * @param <T> 主序列元素类型\n"
            " * @param <U> 首次超时指示类型\n"
            " * @param <V> 逐项超时指示类型\n"
            " */\n"
            "public final class FlowableTimeout",
        ),
        (
            "    public FlowableTimeout(\n            Flowable<T> source,\n            Publisher<U> firstTimeoutIndicator,\n            Function<? super T, ? extends Publisher<V>> itemTimeoutIndicator,\n            Publisher<? extends T> other) {",
            "    /**\n"
            "     * @param source 主序列 Flowable\n"
            "     * @param firstTimeoutIndicator 订阅后首个超时窗口（可为 null）\n"
            "     * @param itemTimeoutIndicator 每个元素对应的超时 Publisher\n"
            "     * @param other 超时时的备用 Publisher（null 则 onError TimeoutException）\n"
            "     */\n"
            "    public FlowableTimeout(\n            Flowable<T> source,\n            Publisher<U> firstTimeoutIndicator,\n            Function<? super T, ? extends Publisher<V>> itemTimeoutIndicator,\n            Publisher<? extends T> other) {",
        ),
        (
            "    interface TimeoutSelectorSupport extends TimeoutSupport {",
            "    /** 扩展 {@link TimeoutSupport}，支持超时指示器 onError 路径。 */\n"
            "    interface TimeoutSelectorSupport extends TimeoutSupport {",
        ),
        (
            "    static final class TimeoutSubscriber<T> extends AtomicLong\n    implements FlowableSubscriber<T>, Subscription, TimeoutSelectorSupport {",
            "    /** 无 fallback：超时 signal {@link TimeoutException}。 */\n"
            "    static final class TimeoutSubscriber<T> extends AtomicLong\n    implements FlowableSubscriber<T>, Subscription, TimeoutSelectorSupport {",
        ),
        (
            "        void startFirstTimeout(Publisher<?> firstTimeoutIndicator) {",
            "        /** 订阅首次超时指示器（idx=0）。 */\n"
            "        void startFirstTimeout(Publisher<?> firstTimeoutIndicator) {",
        ),
        (
            "        @Override\n        public void onTimeout(long idx) {",
            "        /** 索引 idx 超时：cancel 上游并 onError。 */\n"
            "        @Override\n        public void onTimeout(long idx) {",
        ),
        (
            "    static final class TimeoutFallbackSubscriber<T> extends SubscriptionArbiter\n    implements FlowableSubscriber<T>, TimeoutSelectorSupport {",
            "    /** 超时时切换到 fallback Publisher（SubscriptionArbiter）。 */\n"
            "    static final class TimeoutFallbackSubscriber<T> extends SubscriptionArbiter\n    implements FlowableSubscriber<T>, TimeoutSelectorSupport {",
        ),
        (
            "        @Override\n        public void onTimeout(long idx) {",
            "        /** 超时后 produced 已消费项并订阅 fallback。 */\n"
            "        @Override\n        public void onTimeout(long idx) {",
        ),
        (
            "    static final class TimeoutConsumer extends AtomicReference<Subscription>\n    implements FlowableSubscriber<Object>, Disposable {",
            "    /** 订阅超时指示 Publisher；任一路径终止即通知 parent。 */\n"
            "    static final class TimeoutConsumer extends AtomicReference<Subscription>\n    implements FlowableSubscriber<Object>, Disposable {",
        ),
        (
            "        @Override\n        public void onNext(Object t) {",
            "        /** 收到 onNext 视为超时并 cancel 自身。 */\n"
            "        @Override\n        public void onNext(Object t) {",
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
            "wave12b Flowable* [15:30]",
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
        print(f"Marked {ok} files done in queue (note=wave12b)")
    print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False, indent=2))
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
